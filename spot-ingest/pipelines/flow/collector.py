#!/bin/env python

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import time
import logging
import os
import json
from multiprocessing import Process
from common.utils import Util
from common import hdfs_client as hdfs
from common.hdfs_client import HdfsException
from common.file_collector import FileWatcher
from multiprocessing import Pool


class Collector(object):

    def __init__(self, hdfs_app_path, kafkaproducer, conf_type):
        
        self._initialize_members(hdfs_app_path, kafkaproducer, conf_type)

    def _initialize_members(self, hdfs_app_path, kafkaproducer, conf_type):

        # getting parameters.
        self._logger = logging.getLogger('SPOT.INGEST.FLOW')
        self._hdfs_app_path = hdfs_app_path
        self._producer = kafkaproducer

        # get script path
        self._script_path = os.path.dirname(os.path.abspath(__file__))

        # read flow configuration.
        conf_file = "{0}/ingest_conf.json".format(os.path.dirname(os.path.dirname(self._script_path)))
        conf = json.loads(open(conf_file).read())
        self._conf = conf["pipelines"][conf_type]

        # set configuration.
        self._collector_path = self._conf['collector_path']        
        self._dsource = 'flow'
        self._hdfs_root_path = "{0}/{1}".format(hdfs_app_path, self._dsource)

        self._supported_files = self._conf['supported_files']

        # create collector watcher
        self._watcher = FileWatcher(self._collector_path,self._supported_files)
        
        # Multiprocessing. 
        self._processes = conf["collector_processes"]
        self._ingestion_interval = conf["ingestion_interval"]
        self._pool = Pool(processes=self._processes)
        # TODO: review re-use of hdfs.client
        self._hdfs_client = hdfs.get_client()

    def start(self):

        self._logger.info("Starting FLOW ingest") 
        self._watcher.start()
            
        try:
            while True:                
                self._ingest_files_pool()              
                time.sleep(self._ingestion_interval)
        except KeyboardInterrupt:
            self._logger.info("Stopping FLOW collector...")  
            Util.remove_kafka_topic(self._producer.Zookeeper, self._producer.Topic, self._logger)
            self._watcher.stop()
            self._pool.terminate()
            self._pool.close()            
            self._pool.join()
            SystemExit("Ingest finished...")

    def _ingest_files_pool(self):            
       
        if self._watcher.HasFiles:
            
            for x in range(0, self._processes):
                self._logger.info('processes: {0}'.format(self._processes))
                new_file = self._watcher.GetNextFile()
                if self._processes <= 1:
                    _ingest_file(
                                 new_file,
                                 self._hdfs_root_path,
                                 self._producer,
                                 self._producer.Topic
                                 )
                else:
                    result = self._pool.apply_async(_ingest_file, args=(
                        new_file,
                        self._hdfs_root_path,
                        self._producer,
                        self._producer.Topic
                    ))
                    # result.get()  # to debug add try and catch.
                if not self._watcher.HasFiles:
                    break
        return True


def _ingest_file(new_file, hdfs_root_path, producer, topic):

    logger = logging.getLogger('SPOT.INGEST.FLOW.{0}'.format(os.getpid()))

    try:

        # get file name and date.
        file_name_parts = new_file.split('/')
        file_name = file_name_parts[len(file_name_parts)-1]
        file_date = file_name.split('.')[1]
        file_date_path = file_date[0:8]
        file_date_hour = file_date[8:10]

        # hdfs path with timestamp.
        hdfs_path = "{0}/binary/{1}/{2}".format(hdfs_root_path, file_date_path, file_date_hour)
        hdfs_file = "{0}/{1}".format(hdfs_path, file_name)

        try:
            if len(hdfs.list_dir(hdfs_path)) == 0:
                logger.info('creating directory: ' + hdfs_path)
                hdfs.mkdir(hdfs_path)
            logger.info('uploading file to hdfs: ' + hdfs_file)
            result = hdfs.upload_file(hdfs_path, new_file)
            if not result:
                logger.error('File failed to upload: ' + hdfs_file)
                raise HdfsException
            else:
                rm_file = "rm {0}".format(new_file)
                logger.info("Removing files from local staging: {0}".format(rm_file))
                Util.execute_cmd(rm_file, logger)

        except HdfsException as err:
            logger.error('Exception: ' + err.exception)
            logger.info('Check Hdfs Connection settings and server health')

    except Exception as err:
        logger.error("There was a problem, Exception: {0}".format(err))

        # create event for workers to process the file.
        # logger.info("Sending file to worker number: {0}".format(partition))
    try:
        producer.SendMessage(hdfs_file, topic)
        logger.info("File {0} has been successfully sent to Kafka Topic to: {1}".format(hdfs_file, topic))
    except Exception as err:
        logger.info("File {0} failed to be sent to Kafka Topic to: {1}".format(hdfs_file, topic))
        logger.error("Error: {0}".format(err))

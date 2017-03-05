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
from common.file_collector import FileWatcher
from multiprocessing import Pool
from common.kafka_client import KafkaTopic

class Collector(object):

    def __init__(self,hdfs_app_path,kafka_topic,conf_type):
        
        self._initialize_members(hdfs_app_path,kafka_topic,conf_type)

    def _initialize_members(self,hdfs_app_path,kafka_topic,conf_type):
  
        # getting parameters.
        self._logger = logging.getLogger('SPOT.INGEST.FLOW')
        self._hdfs_app_path = hdfs_app_path
        self._kafka_topic = kafka_topic

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

    def start(self):

        self._logger.info("Starting FLOW ingest") 
        self._watcher.start()
            
        try:
            while True:                
                self._ingest_files_pool()              
                time.sleep(self._ingestion_interval)
        except KeyboardInterrupt:
            self._logger.info("Stopping FLOW collector...")  
            Util.remove_kafka_topic(self._kafka_topic.Zookeeper,self._kafka_topic.Topic,self._logger)          
            self._watcher.stop()
            self._pool.terminate()
            self._pool.close()            
            self._pool.join()
            SystemExit("Ingest finished...")
    

    def _ingest_files_pool(self):            
       
        if self._watcher.HasFiles:
            
            for x in range(0,self._processes):
                file = self._watcher.GetNextFile()
                resutl = self._pool.apply_async(ingest_file,args=(file,self._kafka_topic.Partition,self._hdfs_root_path ,self._kafka_topic.Topic,self._kafka_topic.BootstrapServers,))
                #resutl.get() # to debug add try and catch.
                if  not self._watcher.HasFiles: break    
        return True
    


def ingest_file(file,partition,hdfs_root_path,topic,kafka_servers):

        logger = logging.getLogger('SPOT.INGEST.FLOW.{0}'.format(os.getpid()))

        try:

            # get file name and date.
            file_name_parts = file.split('/')
            file_name = file_name_parts[len(file_name_parts)-1]
            file_date = file_name.split('.')[1]

            file_date_path = file_date[0:8]
            file_date_hour = file_date[8:10]

            # hdfs path with timestamp.
            hdfs_path = "{0}/binary/{1}/{2}".format(hdfs_root_path,file_date_path,file_date_hour)
            Util.creat_hdfs_folder(hdfs_path,logger)

            # load to hdfs.
            hdfs_file = "{0}/{1}".format(hdfs_path,file_name)
            Util.load_to_hdfs(file,hdfs_file,logger)

            # create event for workers to process the file.
            logger.info("Sending file to worker number: {0}".format(partition))
            KafkaTopic.SendMessage(hdfs_file,kafka_servers,topic,partition)    
            logger.info("File {0} has been successfully sent to Kafka Topic to: {1}".format(file,topic))

        except Exception as err:
            logger.error("There was a problem, please check the following error message:{0}".format(err.message))
            logger.error("Exception: {0}".format(err))

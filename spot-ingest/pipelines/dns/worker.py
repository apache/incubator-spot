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

import logging
import datetime
import subprocess
import json
import os
from multiprocessing import Process
from common.utils import Util


class Worker(object):

    def __init__(self,db_name,hdfs_app_path,kafka_consumer,conf_type,processes=None):
        
        self._initialize_members(db_name,hdfs_app_path,kafka_consumer,conf_type)

    def _initialize_members(self,db_name,hdfs_app_path,kafka_consumer,conf_type):

        # get logger instance.
        self._logger = Util.get_logger('SPOT.INGEST.WRK.DNS')

        self._db_name = db_name
        self._hdfs_app_path = hdfs_app_path

        # read proxy configuration.
        self._script_path = os.path.dirname(os.path.abspath(__file__))
        conf_file = "{0}/ingest_conf.json".format(os.path.dirname(os.path.dirname(self._script_path)))
        conf = json.loads(open(conf_file).read())
        self._conf = conf["pipelines"][conf_type] 

        self._process_opt = self._conf['process_opt']
        self._local_staging = self._conf['local_staging']
        self.kafka_consumer = kafka_consumer

    def start(self):

        self._logger.info("Listening topic:{0}".format(self.kafka_consumer.Topic))
        for message in self.kafka_consumer.start():
            self._new_file(message.value)

    def _new_file(self,file):

        self._logger.info("-------------------------------------- New File received --------------------------------------")
        self._logger.info("File: {0} ".format(file))        
        p = Process(target=self._process_new_file, args=(file,))
        p.start() 
        p.join()

    def _process_new_file(self,file):

        # get file from hdfs
        get_file_cmd = "hadoop fs -get {0} {1}.".format(file,self._local_staging)
        self._logger.info("Getting file from hdfs: {0}".format(get_file_cmd))
        Util.execute_cmd(get_file_cmd,self._logger)

        # get file name and date
        file_name_parts = file.split('/')
        file_name = file_name_parts[len(file_name_parts)-1]

        binary_hour = file_name_parts[len(file_name_parts)-2]
        binary_date_path = file_name_parts[len(file_name_parts)-3]
        binary_year = binary_date_path[0:4]
        binary_month = binary_date_path[4:6]
        binary_day = binary_date_path[6:8]

        # build process cmd.
        process_cmd = "tshark -r {0}{1} {2} > {0}{1}.csv".format(self._local_staging,file_name,self._process_opt)
        self._logger.info("Processing file: {0}".format(process_cmd))
        Util.execute_cmd(process_cmd,self._logger)

        # create hdfs staging.
        hdfs_path = "{0}/dns".format(self._hdfs_app_path)
        staging_timestamp = datetime.datetime.now().strftime('%M%S%f')[:-4]
        hdfs_staging_path =  "{0}/stage/{1}".format(hdfs_path,staging_timestamp)
        create_staging_cmd = "hadoop fs -mkdir -p {0}".format(hdfs_staging_path)
        self._logger.info("Creating staging: {0}".format(create_staging_cmd))
        Util.execute_cmd(create_staging_cmd,self._logger)

        # move to stage.
        mv_to_staging ="hadoop fs -moveFromLocal {0}{1}.csv {2}/.".format(self._local_staging,file_name,hdfs_staging_path)
        self._logger.info("Moving data to staging: {0}".format(mv_to_staging))
        Util.execute_cmd(mv_to_staging,self._logger)

        #load to avro
        load_to_avro_cmd = "hive -hiveconf dbname={0} -hiveconf y={1} -hiveconf m={2} -hiveconf d={3} -hiveconf h={4} -hiveconf data_location='{5}' -f pipelines/dns/load_dns_avro_parquet.hql".format(self._db_name,binary_year,binary_month,binary_day,binary_hour,hdfs_staging_path)

        self._logger.info("Loading data to hive: {0}".format(load_to_avro_cmd))
        Util.execute_cmd(load_to_avro_cmd,self._logger)

        # remove from hdfs staging
        rm_hdfs_staging_cmd = "hadoop fs -rm -R -skipTrash {0}".format(hdfs_staging_path)
        self._logger.info("Removing staging path: {0}".format(rm_hdfs_staging_cmd))
        Util.execute_cmd(rm_hdfs_staging_cmd,self._logger)

        # remove from local staging.
        rm_local_staging = "rm {0}{1}".format(self._local_staging,file_name)
        self._logger.info("Removing files from local staging: {0}".format(rm_local_staging))
        Util.execute_cmd(rm_local_staging,self._logger)

        self._logger.info("File {0} was successfully processed.".format(file_name))

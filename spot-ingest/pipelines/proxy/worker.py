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

import os
import logging
import json

from common.utils import Util


class Worker(object):

    def __init__(self,db_name,hdfs_app_path,kafka_consumer,conf_type,processes):

        self._initialize_members(db_name,hdfs_app_path,kafka_consumer,conf_type,processes)

    def _initialize_members(self,db_name,hdfs_app_path,kafka_consumer,conf_type,processes):
        
        # get logger instance.
        self._logger = Util.get_logger('SPOT.INGEST.WRK.PROXY')

        self._db_name = db_name
        self._hdfs_app_path = hdfs_app_path
        self._kafka_consumer = kafka_consumer

        # read proxy configuration.
        self._script_path = os.path.dirname(os.path.abspath(__file__))
        conf_file = "{0}/ingest_conf.json".format(os.path.dirname(os.path.dirname(self._script_path)))
        conf = json.loads(open(conf_file).read())
        self._spark_conf  = conf["spark-streaming"]
        self._conf = conf["pipelines"][conf_type]
        self._processes = processes

    def start(self):

        self._logger.info("Creating Spark Job for topic: {0}".format(self._kafka_consumer.Topic))                

        # parser
        parser = self._conf["parser"]

        #spark conf
        diver_memory = self._spark_conf["driver_memory"]
        num_exec = self._spark_conf["spark_exec"]
        exec_memory = self._spark_conf["spark_executor_memory"]
        exec_cores = self._spark_conf["spark_executor_cores"]
        batch_size = self._spark_conf["spark_batch_size"]
        
        jar_path = os.path.dirname(os.path.dirname(self._script_path))
        # spark job command.          
        spark_job_cmd = ("spark-submit --master yarn "
                        "--driver-memory {0} "
                        "--num-executors {1} "
                        "--conf spark.executor.memory={2} "
                        "--conf spark.executor.cores={3} "
                        "--jars {4}/common/spark-streaming-kafka-0-8-assembly_2.11-2.0.0.jar "
                        "{5}/{6} "
                        "-zk {7} "
                        "-t {8} "
                        "-db {9} "
                        "-dt {10} "
                        "-w {11} "
                        "-bs {12}".format(diver_memory,num_exec,exec_memory,exec_cores,jar_path,self._script_path,parser,self._kafka_consumer.ZookeperServer,self._kafka_consumer.Topic,self._db_name,"proxy",self._processes,batch_size))
        
        # start spark job.
        Util.execute_cmd(spark_job_cmd,self._logger)

        

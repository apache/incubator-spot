#!/bin/env python
import os
import logging
import json

from oni.utils import Util


class Worker(object):

    def __init__(self,db_name,hdfs_app_path,kafka_consumer,conf_type,processes):

        self._initialize_members(db_name,hdfs_app_path,kafka_consumer,conf_type,processes)

    def _initialize_members(self,db_name,hdfs_app_path,kafka_consumer,conf_type,processes):
        
        # get logger instance.
        self._logger = Util.get_logger('ONI.INGEST.WRK.PROXY')

        self._db_name = db_name
        self._hdfs_app_path = hdfs_app_path
        self._kafka_consumer = kafka_consumer

        # read proxy configuration.
        self._script_path = os.path.dirname(os.path.abspath(__file__))
        conf_file = "{0}/ingest_conf.json".format(os.path.dirname(os.path.dirname(self._script_path)))
        conf = json.loads(open(conf_file).read())
        self._conf = conf["pipelines"][conf_type]
        self._processes = processes

    def start(self):

        self._logger.info("Creating Spark Job for topic: {0}".format(self._kafka_consumer.Topic))                

        # parser
        parser = self._conf["parser"]
        

        # spark job command.
        spark_job_cmd = ("spark-submit --master yarn "
                        "--jars {0}/oni/spark-streaming-kafka-0-8-assembly_2.11-2.0.0.jar "
                        "{1}/{2} " 
                        "-zk {3} " 
                        "-t {4} "
                        "-db {5} "
                        "-dt {6} " 
                        "-w {7}".format(os.path.dirname(os.path.dirname(self._script_path)),self._script_path,parser,self._kafka_consumer.ZookeperServer,self._kafka_consumer.Topic,self._db_name,"proxy",self._processes))

        
        # start spark job.
        Util.execute_cmd(spark_job_cmd,self._logger)

        

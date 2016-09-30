#!/bin/env python

import time
import logging
import os
import json
from multiprocessing import Process
from oni.utils import Util, NewFileEvent


class Collector(object):

    def __init__(self,hdfs_app_path,kafka_topic,conf_type):
        
        self._initialize_members(hdfs_app_path,kafka_topic,conf_type)

    def _initialize_members(self,hdfs_app_path,kafka_topic,conf_type):
  
        # getting parameters.
        self._logger = logging.getLogger('ONI.INGEST.FLOW')
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

        # initialize message broker client.
        self.kafka_topic = kafka_topic

        # create collector watcher
        self._watcher =  Util.create_watcher(self._collector_path,NewFileEvent(self),self._logger)

    def start(self):

        self._logger.info("Starting FLOW ingest")
        self._logger.info("Watching: {0}".format(self._collector_path))
        self._watcher.start()

        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            self._logger.info("Stopping FLOW collector...")
            self._watcher.stop()
            self._watcher.join()

            # remove kafka topic
            Util.remove_kafka_topic(self._kafka_topic.Zookeeper,self._kafka_topic.Topic,self._logger)

    def new_file_detected(self,file):

        self._logger.info("-------------------------------------- New File detected --------------------------------------")
        self._logger.info("File: {0}".format(file))

        # validate file extension.
        if not  ".current" in file:

            self._logger.info("Sending new file to kafka; topic: {0}".format(self._kafka_topic.Topic))
            partition = self._kafka_topic.Partition
            p = Process(target=self._ingest_file,args=(file,partition,))
            p.start()
            p.join()

    def _ingest_file(self,file,partition):

        # get file name and date.
        file_name_parts = file.split('/')
        file_name = file_name_parts[len(file_name_parts)-1]
        file_date = file_name.split('.')[1]

        file_date_path = file_date[0:8]
        file_date_hour = file_date[8:10]

        # hdfs path with timestamp.
        hdfs_path = "{0}/binary/{1}/{2}".format(self._hdfs_root_path,file_date_path,file_date_hour)
        Util.creat_hdfs_folder(hdfs_path,self._logger)

        # load to hdfs.
        hdfs_file = "{0}/{1}".format(hdfs_path,file_name)
        Util.load_to_hdfs(file,hdfs_file,self._logger)

        # create event for workers to process the file.
        self._logger.info("Sending file to worker number: {0}".format(partition))
        self.kafka_topic.send_message(hdfs_file,partition)

        self._logger.info("File {0} has been successfully sent to Kafka Topic to: {1}".format(file,self._kafka_topic.Topic))


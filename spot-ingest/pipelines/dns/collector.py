#/bin/env python

import time
import os
import subprocess
import json
import logging
from multiprocessing import Process
from oni.utils import Util, NewFileEvent

class Collector(object):

    def __init__(self,hdfs_app_path,kafka_topic,conf_type):
        
        self._initialize_members(hdfs_app_path,kafka_topic,conf_type)

    def _initialize_members(self,hdfs_app_path,kafka_topic,conf_type):
    
        # getting parameters.
        self._logger = logging.getLogger('ONI.INGEST.DNS')
        self._hdfs_app_path = hdfs_app_path
        self._kafka_topic = kafka_topic

        # get script path
        self._script_path = os.path.dirname(os.path.abspath(__file__))

        # read dns configuration.
        conf_file = "{0}/ingest_conf.json".format(os.path.dirname(os.path.dirname(self._script_path)))
        conf = json.loads(open(conf_file).read())
        self._conf = conf["pipelines"][conf_type]

        # set configuration.
        self._collector_path = self._conf['collector_path']        
        self._dsource = 'dns'
        self._hdfs_root_path = "{0}/{1}".format(hdfs_app_path, self._dsource)

        # set configuration.
        self._pkt_num = self._conf['pkt_num']
        self._pcap_split_staging = self._conf['pcap_split_staging']

        # initialize message broker client.
        self.kafka_topic = kafka_topic

        # create collector watcher
        self._watcher =  Util.create_watcher(self._collector_path,NewFileEvent(self),self._logger)

    def start(self):

        self._logger.info("Starting DNS ingest")
        self._logger.info("Watching: {0}".format(self._collector_path))
        self._watcher.start()

        try:
            while True:
                time.sleep(1)
        except KeyboardInterrupt:
            self._logger.info("Stopping DNS collector...")
            self._watcher.stop()
            self._watcher.join()

            # remove kafka topic
            Util.remove_kafka_topic(self._kafka_topic.Zookeeper,self._kafka_topic.Topic,self._logger)

    def new_file_detected(self,file):

        if not  ".current" in file and file.endswith(".pcap"):
            self._logger.info("-------------------------------------- New File detected --------------------------------------")
            self._logger.info("File: {0}".format(file))


            # create new process for the new file.
            partition = self._kafka_topic.Partition
            p = Process(target=self._ingest_file, args=(file,partition,))
            p.start()
            p.join()

    def _ingest_file(self,file,partition):

        # get file name and date.
        org_file = file
        file_name_parts = file.split('/')
        file_name = file_name_parts[len(file_name_parts)-1]

        # split file.
        name = file_name.split('.')[0]
        split_cmd="editcap -c {0} {1} {2}/{3}_oni.pcap".format(self._pkt_num,file,self._pcap_split_staging,name)
        self._logger.info("Splitting file: {0}".format(split_cmd))
        Util.execute_cmd(split_cmd,self._logger)

        for currdir,subdir,files in os.walk(self._pcap_split_staging):
            for file in files:
                if file.endswith(".pcap") and "{0}_oni".format(name) in file:

                        # get timestamp from the file name to build hdfs path.
                        file_date = file.split('.')[0]
                        pcap_hour = file_date[-6:-4]
                        pcap_date_path = file_date[-14:-6]

                        # hdfs path with timestamp.
                        hdfs_path = "{0}/binary/{1}/{2}".format(self._hdfs_root_path,pcap_date_path,pcap_hour)

 			            # create hdfs path.
                        Util.creat_hdfs_folder(hdfs_path,self._logger)

  			            # load file to hdfs.
                        hadoop_pcap_file = "{0}/{1}".format(hdfs_path,file)
                        Util.load_to_hdfs(os.path.join(currdir,file),hadoop_pcap_file,self._logger)

                        # create event for workers to process the file.
                        self._logger.info( "Sending split file to worker number: {0}".format(partition))
                        self._kafka_topic.send_message(hadoop_pcap_file,partition)
                        self._logger.info("File {0} has been successfully sent to Kafka Topic to: {1}".format(file,self._kafka_topic.Topic))


        self._logger.info("Removing file: {0}".format(org_file))
        rm_big_file = "rm {0}".format(org_file)
        Util.execute_cmd(rm_big_file,self._logger)


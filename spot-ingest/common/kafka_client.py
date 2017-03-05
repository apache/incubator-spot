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
import os
from common.utils import Util
from kafka import KafkaProducer
from kafka import KafkaConsumer as KC
from kafka.partitioner.roundrobin import RoundRobinPartitioner
from kafka.common import TopicPartition

class KafkaTopic(object):


    def __init__(self,topic,server,port,zk_server,zk_port,partitions):

        self._initialize_members(topic,server,port,zk_server,zk_port,partitions)

    def _initialize_members(self,topic,server,port,zk_server,zk_port,partitions):

        # get logger isinstance
        self._logger = logging.getLogger("SPOT.INGEST.KAFKA")

        # kafka requirements
        self._server = server
        self._port = port
        self._zk_server = zk_server
        self._zk_port = zk_port
        self._topic = topic
        self._num_of_partitions = partitions
        self._partitions = []
        self._partitioner = None

        # create topic with partitions
        self._create_topic()

    def _create_topic(self):

        self._logger.info("Creating topic: {0} with {1} parititions".format(self._topic,self._num_of_partitions))     

        # Create partitions for the workers.
        self._partitions = [ TopicPartition(self._topic,p) for p in range(int(self._num_of_partitions))]        

        # create partitioner
        self._partitioner = RoundRobinPartitioner(self._partitions)
        
        # get script path 
        zk_conf = "{0}:{1}".format(self._zk_server,self._zk_port)
        create_topic_cmd = "{0}/kafka_topic.sh create {1} {2} {3}".format(os.path.dirname(os.path.abspath(__file__)),self._topic,zk_conf,self._num_of_partitions)

        # execute create topic cmd
        Util.execute_cmd(create_topic_cmd,self._logger)

    def send_message(self,message,topic_partition):

        self._logger.info("Sending message to: Topic: {0} Partition:{1}".format(self._topic,topic_partition))
        kafka_brokers = '{0}:{1}'.format(self._server,self._port)             
        producer = KafkaProducer(bootstrap_servers=[kafka_brokers],api_version_auto_timeout_ms=3600000)
        future = producer.send(self._topic,message,partition=topic_partition)
        producer.flush(timeout=3600000)
        producer.close()
    
    @classmethod
    def SendMessage(cls,message,kafka_servers,topic,partition=0):
        producer = KafkaProducer(bootstrap_servers=kafka_servers,api_version_auto_timeout_ms=3600000)
        future = producer.send(topic,message,partition=partition)
        producer.flush(timeout=3600000)
        producer.close()  

    @property
    def Topic(self):
        return self._topic
    
    @property
    def Partition(self):        
        return self._partitioner.partition(self._topic).partition

    @property
    def Zookeeper(self):
        zk = "{0}:{1}".format(self._zk_server,self._zk_port)
        return zk

    @property
    def BootstrapServers(self):
        servers = "{0}:{1}".format(self._server,self._port) 
        return servers


class KafkaConsumer(object):
    
    def __init__(self,topic,server,port,zk_server,zk_port,partition):

        self._initialize_members(topic,server,port,zk_server,zk_port,partition)

    def _initialize_members(self,topic,server,port,zk_server,zk_port,partition):

        self._topic = topic
        self._server = server
        self._port = port
        self._zk_server = zk_server
        self._zk_port = zk_port
        self._id = partition

    def start(self):
        
        kafka_brokers = '{0}:{1}'.format(self._server,self._port)
        consumer =  KC(bootstrap_servers=[kafka_brokers],group_id=self._topic)
        partition = [TopicPartition(self._topic,int(self._id))]
        consumer.assign(partitions=partition)
        consumer.poll()
        return consumer

    @property
    def Topic(self):
        return self._topic

    @property
    def ZookeperServer(self):
        return "{0}:{1}".format(self._zk_server,self._zk_port)

    

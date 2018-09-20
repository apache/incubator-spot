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
import sys
from common.utils import Util
from confluent_kafka import Producer
from confluent_kafka import Consumer
import common.configurator as config


class KafkaProducer(object):

    def __init__(self, topic, server, port, zk_server, zk_port, partitions):

        self._initialize_members(topic, server, port, zk_server, zk_port, partitions)

    def _initialize_members(self, topic, server, port, zk_server, zk_port, partitions):

        # get logger isinstance
        self._logger = logging.getLogger("SPOT.INGEST.KafkaProducer")

        # kafka requirements
        self._server = server
        self._port = port
        self._zk_server = zk_server
        self._zk_port = zk_port
        self._topic = topic
        self._num_of_partitions = partitions
        self._partitions = []
        self._partitioner = None
        self._kafka_brokers = '{0}:{1}'.format(self._server, self._port)

        # create topic with partitions
        self._create_topic()

        self._kafka_conf = self._producer_config(self._kafka_brokers)

        self._p = Producer(**self._kafka_conf)

    def _producer_config(self, server):
        # type: (str) -> dict
        """Returns a configuration dictionary containing optional values"""

        connection_conf = {
            'bootstrap.servers': server,
        }

        if os.environ.get('KAFKA_DEBUG'):
            connection_conf.update({'debug': 'all'})

        if config.kerberos_enabled():
            self._logger.info('Kerberos enabled')
            principal, keytab, sasl_mech, security_proto = config.kerberos()
            connection_conf.update({
                'sasl.mechanisms': sasl_mech,
                'security.protocol': security_proto,
                'sasl.kerberos.principal': principal,
                'sasl.kerberos.keytab': keytab,
                'sasl.kerberos.min.time.before.relogin': 6000
            })

            sn = os.environ.get('KAFKA_SERVICE_NAME')
            if sn:
                self._logger.info('Setting Kerberos service name: ' + sn)
                connection_conf.update({'sasl.kerberos.service.name': sn})

            kinit_cmd = os.environ.get('KAFKA_KINIT')
            if kinit_cmd:
                self._logger.info('using kinit command: ' + kinit_cmd)
                connection_conf.update({'sasl.kerberos.kinit.cmd': kinit_cmd})
            else:
                # Using -S %{sasl.kerberos.service.name}/%{broker.name} causes the ticket cache to refresh
                # resulting in authentication errors for other services
                connection_conf.update({
                    'sasl.kerberos.kinit.cmd': 'kinit -S "%{sasl.kerberos.service.name}/%{broker.name}" -k -t "%{sasl.kerberos.keytab}" %{sasl.kerberos.principal}'
                })

        if config.ssl_enabled():
            self._logger.info('Using SSL connection settings')
            ssl_verify, ca_location, cert, key = config.ssl()
            connection_conf.update({
                'ssl.certificate.location': cert,
                'ssl.ca.location': ca_location,
                'ssl.key.location': key
            })

        return connection_conf

    def _create_topic(self):

        self._logger.info("Creating topic: {0} with {1} parititions".format(self._topic, self._num_of_partitions))
        
        # get script path 
        zk_conf = "{0}:{1}".format(self._zk_server, self._zk_port)
        create_topic_cmd = "{0}/kafka_topic.sh create {1} {2} {3}".format(
            os.path.dirname(os.path.abspath(__file__)),
            self._topic,
            zk_conf,
            self._num_of_partitions
        )

        # execute create topic cmd
        Util.execute_cmd(create_topic_cmd, self._logger)

    def SendMessage(self, message, topic):
        p = self._p
        p.produce(topic, message.encode('utf-8'), callback=self._delivery_callback)
        p.poll(0)
        p.flush(timeout=3600000)

    @classmethod
    def _delivery_callback(cls, err, msg):
        if err:
            sys.stderr.write('%% Message failed delivery: %s\n' % err)
        else:
            sys.stderr.write('%% Message delivered to %s [%d]\n' %
                             (msg.topic(), msg.partition()))

    @property
    def Topic(self):
        return self._topic
    
    @property
    def Partition(self):        
        return self._partitioner.partition(self._topic).partition

    @property
    def Zookeeper(self):
        zk = "{0}:{1}".format(self._zk_server, self._zk_port)
        return zk

    @property
    def BootstrapServers(self):
        servers = "{0}:{1}".format(self._server, self._port)
        return servers


class KafkaConsumer(object):
    
    def __init__(self, topic, server, port, zk_server, zk_port, partition):

        self._initialize_members(topic, server, port, zk_server, zk_port, partition)

    def _initialize_members(self, topic, server, port, zk_server, zk_port, partition):

        self._logger = logging.getLogger("SPOT.INGEST.KafkaConsumer")

        self._topic = topic
        self._server = server
        self._port = port
        self._zk_server = zk_server
        self._zk_port = zk_port
        self._id = partition
        self._kafka_brokers = '{0}:{1}'.format(self._server, self._port)
        self._kafka_conf = self._consumer_config(self._id, self._kafka_brokers)

    def _consumer_config(self, groupid, server):
        # type: (dict) -> dict
        """Returns a configuration dictionary containing optional values"""

        connection_conf = {
            'bootstrap.servers': server,
            'group.id': groupid,
        }

        if config.kerberos_enabled():
            self._logger.info('Kerberos enabled')
            principal, keytab, sasl_mech, security_proto = config.kerberos()
            connection_conf.update({
                'sasl.mechanisms': sasl_mech,
                'security.protocol': security_proto,
                'sasl.kerberos.principal': principal,
                'sasl.kerberos.keytab': keytab,
                'sasl.kerberos.min.time.before.relogin': 6000,
                'default.topic.config': {
                    'auto.commit.enable': 'true',
                    'auto.commit.interval.ms': '60000',
                    'auto.offset.reset': 'smallest'}
            })

            sn = os.environ.get('KAFKA_SERVICE_NAME')
            if sn:
                self._logger.info('Setting Kerberos service name: ' + sn)
                connection_conf.update({'sasl.kerberos.service.name': sn})

            kinit_cmd = os.environ.get('KAFKA_KINIT')
            if kinit_cmd:
                self._logger.info('using kinit command: ' + kinit_cmd)
                connection_conf.update({'sasl.kerberos.kinit.cmd': kinit_cmd})
            else:
                # Using -S %{sasl.kerberos.service.name}/%{broker.name} causes the ticket cache to refresh
                # resulting in authentication errors for other services
                connection_conf.update({
                    'sasl.kerberos.kinit.cmd': 'kinit -k -t "%{sasl.kerberos.keytab}" %{sasl.kerberos.principal}'
                })

        if config.ssl_enabled():
            self._logger.info('Using SSL connection settings')
            ssl_verify, ca_location, cert, key = config.ssl()
            connection_conf.update({
                'ssl.certificate.location': cert,
                'ssl.ca.location': ca_location,
                'ssl.key.location': key
            })

        return connection_conf

    def start(self):

        consumer = Consumer(**self._kafka_conf)
        consumer.subscribe([self._topic])
        return consumer

    @property
    def Topic(self):
        return self._topic

    @property
    def ZookeperServer(self):
        return "{0}:{1}".format(self._zk_server, self._zk_port)

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

import argparse
import os
import json
import sys
import datetime
from common.utils import Util
from common.kerberos import Kerberos
import common.configurator as Config
from common.kafka_client import KafkaProducer

# get master configuration.
SCRIPT_PATH = os.path.dirname(os.path.abspath(__file__))
CONF_FILE = "{0}/ingest_conf.json".format(SCRIPT_PATH)
MASTER_CONF = json.loads(open(CONF_FILE).read())


def main():

    # input Parameters
    parser = argparse.ArgumentParser(description="Master Collector Ingest Daemon")
    parser.add_argument('-t', '--type', dest='type', required=True,
                        help='Type of data that will be ingested (Pipeline Configuration)',
                        metavar='')
    parser.add_argument('-w', '--workers', dest='workers_num',
                        required=True, help='Number of workers for the ingest process',
                        metavar='')
    parser.add_argument('-id', '--ingestId', dest='ingest_id',
                        required=False, help='Ingest ID', metavar='')
    args = parser.parse_args()

    # start collector based on data source type.
    start_collector(args.type, args.workers_num, args.ingest_id)


def start_collector(type, workers_num, id=None):

    # generate ingest id
    ingest_id = str(datetime.datetime.time(datetime.datetime.now())).replace(":", "_").replace(".", "_")

    # create logger.
    logger = Util.get_logger("SPOT.INGEST")

    # validate the given configuration exists in ingest_conf.json.
    if not type in MASTER_CONF["pipelines"]:
        logger.error("'{0}' type is not a valid configuration.".format(type))
        sys.exit(1)

    # validate the type is a valid module.
    if not Util.validate_data_source(MASTER_CONF["pipelines"][type]["type"]):
        logger.error("'{0}' type is not configured. Please check you ingest conf file".format(MASTER_CONF["pipelines"][type]["type"]))
        sys.exit(1)

    # validate if kerberos authentication is required.
    if Config.kerberos_enabled():
        kb = Kerberos()
        kb.authenticate()

    # kafka server info.
    logger.info("Initializing kafka instance")
    k_server = MASTER_CONF["kafka"]['kafka_server']
    k_port = MASTER_CONF["kafka"]['kafka_port']

    # required zookeeper info.
    zk_server = MASTER_CONF["kafka"]['zookeper_server']
    zk_port = MASTER_CONF["kafka"]['zookeper_port']
         
    topic = "{0}".format(type,ingest_id) if not id else id
    producer = KafkaProducer(topic, k_server, k_port, zk_server, zk_port, workers_num)

    # create a collector instance based on data source type.
    logger.info("Starting {0} ingest instance".format(topic))
    module = __import__("pipelines.{0}.collector".
                        format(MASTER_CONF["pipelines"][type]["type"]),
                        fromlist=['Collector'])

    # start collector.
    ingest_collector = module.Collector(MASTER_CONF['hdfs_app_path'], producer, type)
    ingest_collector.start()


if __name__ == '__main__':
    main()

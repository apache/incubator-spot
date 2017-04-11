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
import logging
import sys
from common.utils import Util
from common.kerberos import Kerberos
from common.kafka_client import KafkaConsumer

script_path = os.path.dirname(os.path.abspath(__file__))
conf_file = "{0}/ingest_conf.json".format(script_path)
worker_conf = json.loads(open (conf_file).read())

def main():

    # input parameters
    parser = argparse.ArgumentParser(description="Worker Ingest Framework")
    parser.add_argument('-t','--type',dest='type',required=True,help='Type of data that will be ingested (Pipeline Configuration)',metavar='')
    parser.add_argument('-i','--id',dest='id',required=True,help='Worker Id, this is needed to sync Kafka and Ingest framework (Partition Number)',metavar='')
    parser.add_argument('-top','--topic',dest='topic',required=True,help='Topic to read from.',metavar="")
    parser.add_argument('-p','--processingParallelism',dest='processes',required=False,help='Processing Parallelism',metavar="")
    args = parser.parse_args()

    # start worker based on the type.
    start_worker(args.type,args.topic,args.id,args.processes)


def start_worker(type,topic,id,processes=None):

    logger = Util.get_logger("SPOT.INGEST.WORKER")

    # validate the given configuration exists in ingest_conf.json.
    if not type in worker_conf["pipelines"]:
        logger.error("'{0}' type is not a valid configuration.".format(type));
        sys.exit(1)

    # validate the type is a valid module.
    if not Util.validate_data_source(worker_conf["pipelines"][type]["type"]):
        logger.error("The provided data source {0} is not valid".format(type));sys.exit(1)

    # validate if kerberos authentication is requiered.
    if os.getenv('KRB_AUTH'):
        kb = Kerberos()
        kb.authenticate()

    # create a worker instance based on the data source type.
    module = __import__("pipelines.{0}.worker".format(worker_conf["pipelines"][type]["type"]),fromlist=['Worker'])

    # kafka server info.
    logger.info("Initializing kafka instance")
    k_server = worker_conf["kafka"]['kafka_server']
    k_port = worker_conf["kafka"]['kafka_port']

    # required zookeeper info.
    zk_server = worker_conf["kafka"]['zookeper_server']
    zk_port = worker_conf["kafka"]['zookeper_port']
    topic = topic

    # create kafka consumer.
    kafka_consumer = KafkaConsumer(topic,k_server,k_port,zk_server,zk_port,id)

    # start worker.
    db_name = worker_conf['dbname']
    app_path = worker_conf['hdfs_app_path']
    ingest_worker = module.Worker(db_name,app_path,kafka_consumer,type,processes)
    ingest_worker.start()

if __name__=='__main__':
    main()


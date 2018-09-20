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

import sys
import subprocess
import datetime
import logging
import os
import json
from multiprocessing import Process
from common.utils import Util
from common import hive_engine
from common import hdfs_client as hdfs
from confluent_kafka import KafkaError, KafkaException


class Worker(object):

    def __init__(self, db_name, hdfs_app_path, kafka_consumer, conf_type, processes=None):
        self._initialize_members(db_name, hdfs_app_path, kafka_consumer, conf_type)

    def _initialize_members(self, db_name, hdfs_app_path, kafka_consumer, conf_type):

        # get logger instance.
        self._logger = Util.get_logger('SPOT.INGEST.WRK.FLOW')

        self._db_name = db_name
        self._hdfs_app_path = hdfs_app_path

        # read proxy configuration.
        self._script_path = os.path.dirname(os.path.abspath(__file__))
        conf_file = "{0}/ingest_conf.json".format(os.path.dirname(os.path.dirname(self._script_path)))
        conf = json.loads(open(conf_file).read())
        self._conf = conf["pipelines"][conf_type]
        self._id = "spot-{0}-worker".format(conf_type)

        self._process_opt = self._conf['process_opt']
        self._local_staging = self._conf['local_staging']
        self.kafka_consumer = kafka_consumer

        # self._cursor = hive_engine.create_connection()
        self._cursor = hive_engine

    def start(self):

        self._logger.info("Listening topic:{0}".format(self.kafka_consumer.Topic))
        consumer = self.kafka_consumer.start()
        try:
            while True:
                message = consumer.poll(timeout=1.0)
                if message is None:
                    continue
                if not message.error():
                    self._new_file(message.value().decode('utf-8'))
                elif message.error():
                    if message.error().code() == KafkaError._PARTITION_EOF:
                        continue
                    elif message.error:
                        raise KafkaException(message.error())

        except KeyboardInterrupt:
            sys.stderr.write('%% Aborted by user\n')

        consumer.close()

    def _new_file(self, nf):

        self._logger.info(
            "-------------------------------------- New File received --------------------------------------"
        )
        self._logger.info("File: {0} ".format(nf))

        p = Process(target=self._process_new_file, args=(nf, ))
        p.start()
        p.join()
        
    def _process_new_file(self, nf):

        # get file name and date
        file_name_parts = nf.split('/')
        file_name = file_name_parts[len(file_name_parts)-1]
        nf_path = nf.rstrip(file_name)
        flow_date = file_name.split('.')[1]
        flow_year = flow_date[0:4]
        flow_month = flow_date[4:6]
        flow_day = flow_date[6:8]
        flow_hour = flow_date[8:10]

        # get file from hdfs
        if hdfs.file_exists(nf_path, file_name):
            self._logger.info("Getting file from hdfs: {0}".format(nf))
            hdfs.download_file(nf, self._local_staging)
        else:
            self._logger.info("file: {0} not found".format(nf))
            # TODO: error handling

        # build process cmd.
        sf = "{0}{1}.csv".format(self._local_staging,file_name)
        process_cmd = "nfdump -o csv -r {0}{1} {2} > {3}".format(self._local_staging, file_name, self._process_opt, sf)
        self._logger.info("Processing file: {0}".format(process_cmd))
        Util.execute_cmd(process_cmd,self._logger)

        # create hdfs staging.
        hdfs_path = "{0}/flow".format(self._hdfs_app_path)
        staging_timestamp = datetime.datetime.now().strftime('%M%S%f')[:-4]
        hdfs_staging_path = "{0}/stage/{1}".format(hdfs_path,staging_timestamp)
        self._logger.info("Creating staging: {0}".format(hdfs_staging_path))
        hdfs.mkdir(hdfs_staging_path)

        # move to stage.
        local_file = "{0}{1}.csv".format(self._local_staging, file_name)
        self._logger.info("Moving data to staging: {0}".format(hdfs_staging_path))
        hdfs.upload_file(hdfs_staging_path, local_file)

        # load with impyla
        drop_table = "DROP TABLE IF EXISTS {0}.flow_tmp".format(self._db_name)
        self._logger.info( "Dropping temp table: {0}".format(drop_table))
        self._cursor.execute_query(drop_table)

        create_external = ("\n"
                           "CREATE EXTERNAL TABLE {0}.flow_tmp (\n"
                           "  treceived STRING,\n"
                           "  tryear INT,\n"
                           "  trmonth INT,\n"
                           "  trday INT,\n"
                           "  trhour INT,\n"
                           "  trminute INT,\n"
                           "  trsec INT,\n"
                           "  tdur FLOAT,\n"
                           "  sip  STRING,\n"
                           "  dip STRING,\n"
                           "  sport INT,\n"
                           "  dport INT,\n"
                           "  proto STRING,\n"
                           "  flag STRING,\n"
                           "  fwd INT,\n"
                           "  stos INT,\n"
                           "  ipkt BIGINT,\n"
                           "  ibyt BIGINT,\n"
                           "  opkt BIGINT,\n"
                           "  obyt BIGINT,\n"
                           "  input INT,\n"
                           "  output INT,\n"
                           "  sas INT,\n"
                           "  das INT,\n"
                           "  dtos INT,\n"
                           "  dir INT,\n"
                           "  rip STRING\n"
                           "  )\n"
                           "  ROW FORMAT DELIMITED FIELDS TERMINATED BY ','\n"
                           "  STORED AS TEXTFILE\n"
                           "  LOCATION '{1}'\n"
                           "  TBLPROPERTIES ('avro.schema.literal'='{{\n"
                           "  \"type\":   \"record\"\n"
                           "  , \"name\":   \"RawFlowRecord\"\n"
                           "  , \"namespace\" : \"com.cloudera.accelerators.flows.avro\"\n"
                           "  , \"fields\": [\n"
                           "      {{\"name\": \"treceived\",             \"type\":[\"string\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"tryear\",              \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"trmonth\",             \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"trday\",               \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"trhour\",              \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"trminute\",            \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"trsec\",               \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"tdur\",                \"type\":[\"float\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"sip\",                \"type\":[\"string\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"sport\",                 \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"dip\",                \"type\":[\"string\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"dport\",                 \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"proto\",              \"type\":[\"string\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"flag\",               \"type\":[\"string\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"fwd\",                   \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"stos\",                  \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"ipkt\",               \"type\":[\"bigint\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"ibytt\",              \"type\":[\"bigint\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"opkt\",               \"type\":[\"bigint\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"obyt\",               \"type\":[\"bigint\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"input\",                 \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"output\",                \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"sas\",                   \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"das\",                   \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"dtos\",                  \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"dir\",                   \"type\":[\"int\",   \"null\"]}}\n"
                           "      ,  {{\"name\": \"rip\",                \"type\":[\"string\",   \"null\"]}}\n"
                           "      ]\n"
                           "}}')\n"
                           ).format(self._db_name, hdfs_staging_path)
        self._logger.info( "Creating external table: {0}".format(create_external))
        self._cursor.execute_query(create_external)

        insert_into_table = """
        INSERT INTO TABLE {0}.flow
        PARTITION (y={1}, m={2}, d={3}, h={4})
        SELECT   treceived,  unix_timestamp(treceived) AS unix_tstamp, tryear,  trmonth, trday,  trhour,  trminute,  trsec,
          tdur,  sip, dip, sport, dport,  proto,  flag,  fwd,  stos,  ipkt,  ibyt,  opkt,  obyt,  input,  output,
          sas,  das,  dtos,  dir,  rip
        FROM {0}.flow_tmp
        """.format(self._db_name, flow_year, flow_month, flow_day, flow_hour)
        self._logger.info( "Loading data to {0}: {1}"
                           .format(self._db_name, insert_into_table)
                           )
        self._cursor.execute_query(insert_into_table)

        # remove from hdfs staging
        self._logger.info("Removing staging path: {0}".format(hdfs_staging_path))
        hdfs.delete_folder(hdfs_staging_path)

        # remove from local staging.
        rm_local_staging = "rm {0}{1}".format(self._local_staging,file_name)
        self._logger.info("Removing files from local staging: {0}".format(rm_local_staging))
        Util.execute_cmd(rm_local_staging,self._logger)

        rm_local_staging = "rm {0}".format(sf)
        self._logger.info("Removing files from local staging: {0}".format(rm_local_staging))
        Util.execute_cmd(rm_local_staging,self._logger)

        self._logger.info("File {0} was successfully processed.".format(file_name))

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
import sys
import subprocess
import logging


class util(object):
    
    @classmethod
    def create_hdfs_folder(cls,hdfs_path,logger):
        hadoop_create_folder="hadoop fs -mkdir -p {0}".format(hdfs_path)
        logger.info("SPOT.Utils: Creating hdfs folder: {0}".format(hadoop_create_folder))
        subprocess.call(hadoop_create_folder,shell=True)

    @classmethod
    def load_to_hdfs(cls,file_local_path,file_hdfs_path,logger):
        # move file to hdfs.
        load_to_hadoop_script = "hadoop fs -moveFromLocal {0} {1}".format(file_local_path,file_hdfs_path)
        logger.info("SPOT.Utils: Loading file to hdfs: {0}".format(load_to_hadoop_script))
        subprocess.call(load_to_hadoop_script,shell=True)

    @classmethod
    def get_logger(cls,logger_name,create_file=False):

        # create logger for prd_ci
        log = logging.getLogger(logger_name)
        log.setLevel(level=logging.INFO)

        # create formatter and add it to the handlers
        formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

        if create_file:
                # create file handler for logger.
                fh = logging.FileHandler('SPOT.log')
                fh.setLevel(level=logging.DEBUG)
                fh.setFormatter(formatter)
        # reate console handler for logger.
        ch = logging.StreamHandler()
        ch.setLevel(level=logging.DEBUG)
        ch.setFormatter(formatter)

        # add handlers to logger.
        if create_file:
            log.addHandler(fh)

        log.addHandler(ch)
        return  log

    @classmethod
    def execute_cmd(cls,command,logger):

        try:
            logger.info("SPOT.Utils: Executing: {0}".format(command))
            subprocess.call(command,shell=True)

        except subprocess.CalledProcessError as e:
            logger.error("SPOT.Utils: There was an error executing: {0}".format(e.cmd))
            sys.exit(1)

    @classmethod
    def execute_hive_cmd(cls,command,logger):

        try:
            logger.info("SPOT.Utils: Executing hive command: hive -e \"{0}\"".format(command))
            subprocess.call("hive -e \"{0}\"".format(command),shell=True)

        except subprocess.CalledProcessError as e:
            logger.error("SPOT.Utils: There was an error executing: {0}".format(e.cmd))
            sys.exit(1)


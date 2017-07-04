#!/bin/bash

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

# parse and validate arguments

DSOURCE=$1
RAWDATA_PATH=$2

# read in variables (except for date) from etc/.conf file

source /etc/spot.conf

# third argument if present will override default TOL from conf file

TOL=1.1
MAXRESULTS=20

HPATH=${HUSER}/${DSOURCE}/test/scored_results
# prepare parameters pipeline stages

# pass the user domain designation if not empty

if [ ! -z $USER_DOMAIN ] ; then
    USER_DOMAIN_CMD="--userdomain $USER_DOMAIN"
else
    USER_DOMAIN_CMD=''
fi

FEEDBACK_PATH=${HPATH}/feedback/ml_feedback.csv

HDFS_SCORED_CONNECTS=${HPATH}/scores

hdfs dfs -rm -R -f ${HDFS_SCORED_CONNECTS}

time spark-submit --class "org.apache.spot.SuspiciousConnects" \
  --master yarn \
  --driver-memory ${SPK_DRIVER_MEM} \
  --conf spark.driver.maxResultSize=${SPK_DRIVER_MAX_RESULTS} \
  --conf spark.driver.maxPermSize=512m \
  --conf spark.driver.cores=1 \
  --conf spark.dynamicAllocation.enabled=true \
  --conf spark.dynamicAllocation.minExecutors=0 \
  --conf spark.dynamicAllocation.maxExecutors=${SPK_EXEC} \
  --conf spark.executor.cores=${SPK_EXEC_CORES} \
  --conf spark.executor.memory=${SPK_EXEC_MEM} \
  --conf spark.sql.autoBroadcastJoinThreshold=${SPK_AUTO_BRDCST_JOIN_THR} \
  --conf "spark.executor.extraJavaOptions=-XX:MaxPermSize=512M -XX:PermSize=512M" \
  --conf spark.shuffle.io.preferDirectBufs=false    \
  --conf spark.kryoserializer.buffer.max=512m \
  --conf spark.shuffle.service.enabled=true \
  --conf spark.yarn.am.waitTime=1000000 \
  --conf spark.yarn.driver.memoryOverhead=${SPK_DRIVER_MEM_OVERHEAD} \
  --conf spark.yarn.executor.memoryOverhead=${SPK_EXEC_MEM_OVERHEAD} target/scala-2.11/spot-ml-assembly-1.1.jar \
  --analysis ${DSOURCE} \
  --input ${RAWDATA_PATH}  \
  --dupfactor ${DUPFACTOR} \
  --feedback ${FEEDBACK_PATH} \
  --ldatopiccount ${TOPIC_COUNT} \
  --scored ${HDFS_SCORED_CONNECTS} \
  --threshold ${TOL} \
  --maxresults ${MAXRESULTS} \
  --ldamaxiterations 20 \
  --ldaalpha ${LDA_ALPHA} \
  --ldabeta ${LDA_BETA} \
  --ldaoptimizer ${LDA_OPTIMIZER} \
  --precision ${PRECISION} \
  $USER_DOMAIN_CMD
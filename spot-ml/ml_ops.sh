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

FDATE=$1
DSOURCE=$2

YR=${FDATE:0:4}
MH=${FDATE:4:2}
DY=${FDATE:6:2}

if [[ "${#FDATE}" != "8" || -z "${DSOURCE}" ]]; then
    echo "ml_ops.sh syntax error"
    echo "Please run ml_ops.sh again with the correct syntax:"
    echo "./ml_ops.sh YYYYMMDD TYPE [MAX RESULTS] [TOL]"
    echo "for example:"
    echo "./ml_ops.sh 20160122 dns 1000 1e-6"
    echo "./ml_ops.sh 20160122 flow"
    echo "./ml_ops.sh 20160122 proxy 100"
    exit
fi

# read in variables (except for date) from etc/.conf file
# note: FDATE and DSOURCE *must* be defined prior sourcing this conf file

source /etc/spot.conf

# third argument if present will override default TOL from conf file

if [ -n "$3" ]; then TOL=$3 ; fi

if [ -n "$4" ]; then
    MAXRESULTS=$4
else
    MAXRESULTS=-1
fi

# prepare parameters pipeline stages

if [ "$DSOURCE" == "flow" ]; then
    RAWDATA_PATH=${FLOW_PATH}
elif [ "$DSOURCE" == "dns" ]; then
    RAWDATA_PATH=${DNS_PATH}
else
    RAWDATA_PATH=${PROXY_PATH}
fi

if [ "$DSOURCE" == "flow" ]; then
    DATA_TABLE=${FLOW_TABLE}
elif [ "$DSOURCE" == "dns" ]; then
    DATA_TABLE=${DNS_TABLE}
else
    DATA_TABLE=${PROXY_TABLE}
fi

# pass the user domain designation if not empty

if [ ! -z $USER_DOMAIN ] ; then
    USER_DOMAIN_CMD="--userdomain $USER_DOMAIN"
else
    USER_DOMAIN_CMD=''
fi

FEEDBACK_PATH=${LPATH}/${DSOURCE}_scores.csv

HDFS_SCORED_CONNECTS=${HPATH}/scores

LDA_OUTPUT_DIR=${DSOURCE}/${FDATE}

mkdir -p ${LPATH}
rm -f ${LPATH}/*.{dat,beta,gamma,other,pkl} # protect the flow_scores.csv file

hdfs dfs -rm -R -f ${HDFS_SCORED_CONNECTS}

time spark-submit --class "org.apache.spot.SuspiciousConnects" \
  --master yarn \
  --driver-memory ${SPK_DRIVER_MEM} \
  --conf spark.driver.maxResultSize=${SPK_DRIVER_MAX_RESULTS} \
  --conf spark.driver.maxPermSize=512m \
  --conf spark.dynamicAllocation.enabled=true \
  --conf spark.dynamicAllocation.maxExecutors=${SPK_EXEC} \
  --conf spark.executor.cores=${SPK_EXEC_CORES} \
  --conf spark.executor.memory=${SPK_EXEC_MEM} \
  --conf spark.sql.autoBroadcastJoinThreshold=${SPK_AUTO_BRDCST_JOIN_THR} \
  --conf "spark.executor.extraJavaOptions=-XX:MaxPermSize=512M -XX:PermSize=512M" \
  --conf spark.kryoserializer.buffer.max=512m \
  --conf spark.yarn.am.waitTime=100s \
  --conf spark.yarn.am.memoryOverhead=${SPK_DRIVER_MEM_OVERHEAD} \
  --conf spark.yarn.executor.memoryOverhead=${SPK_EXEC_MEM_OVERHEAD} target/scala-2.11/spot-ml-assembly-1.1.jar \
  --analysis ${DSOURCE} \
  --input ${RAWDATA_PATH}  \
  --database ${DBNAME} \
  --datatable ${DATA_TABLE} \
  --year ${YR} \
  --month ${MH} \
  --day ${DY} \
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


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

#-----------------------------------------------------------------------------------
# Validate parameters.
#-----------------------------------------------------------------------------------

INGEST_CONF=$1
WORKERS_NUM=${2}
TIME_ZONE=${3:-UTC}

if [ -z $INGEST_CONF  ]; then

    echo "Please provide an ingest type (pipeline configuration)"
    exit 1

fi

if [ -z $WORKERS_NUM ];then

    echo "Please provide the number of workers"
    exit 1

fi

#-----------------------------------------------------------------------------------
# Validate type (ingest conf).
#-----------------------------------------------------------------------------------
CONF_FILE="ingest_conf.json"
CONF_ING=`python -c "import json,sys;obj=json.loads(open('ingest_conf.json').read());print obj['pipelines']['${INGEST_CONF}'];"`
TYPE=`python -c "import json,sys;obj=json.loads(open('ingest_conf.json').read());print obj['pipelines']['${INGEST_CONF}']['type'];"`

if [ -z  "$CONF_ING" ]; then
    echo "Provided type is not part of ${CONF_FILE}"
    exit 1

fi

#-----------------------------------------------------------------------------------
# Create screens for Master and Worker.
#-----------------------------------------------------------------------------------

INGEST_DATE=`date +"%H_%M_%S"`

screen -d -m -S SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE}  -s /bin/bash
screen -S SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE} -X setenv TZ ${TIME_ZONE}
screen -dr  SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE} -X screen -t Master sh -c "python master_collector.py -t ${INGEST_CONF} -w ${WORKERS_NUM} -id SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE}; echo 'Closing Master...'; sleep 432000"

echo "Creating master collector"; sleep 2

if [ $WORKERS_NUM -gt 0 ]; then
	w=0

    if [ $TYPE == "proxy" ]; 
    then
        echo "Creating worker_${w}"
        screen -dr SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE} -X screen -t Worker_$w sh -c "python worker.py -t ${INGEST_CONF} -i ${w} -top SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE} -p ${WORKERS_NUM}; echo 'Closing worker...'; sleep 432000"
    
    else
        while [  $w -le  $((WORKERS_NUM-1)) ]; 
	    do
            echo "Creating worker_${w}"
		    screen -dr SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE}  -X screen -t Worker_$w sh -c "python worker.py -t ${INGEST_CONF} -i ${w} -top SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE}; echo 'Closing worker...'; sleep 432000"
		    let w=w+1
            sleep 2
	    done

    fi
  
fi

#-----------------------------------------------------------------------------------
# show outputs.
#-----------------------------------------------------------------------------------
echo "Background ingest process is running: SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE}"
echo "To rejoin the session use: screen -x SPOT-INGEST-${INGEST_CONF}-${INGEST_DATE}"
echo 'To switch between workers and master use: crtl a + "'


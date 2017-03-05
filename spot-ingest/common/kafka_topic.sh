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

ACTION=$1
TOPIC=$2
ZK=$3
PARTITIONS=$4

#-----------------------------------------------------------------------------------
# Validate parameters.
#-----------------------------------------------------------------------------------

USAGE="Usage:\n ${0} create topic_name zk_ip:zk_port num_of_partitions \n ${0} delete topic_name zk_ip:zk_port"

if [  -z $ACTION  ] || [ -z $TOPIC ]  ; then
    echo -e $USAGE
    exit 1
fi

#-----------------------------------------------------------------------------------
# Create/Delete topic.
#-----------------------------------------------------------------------------------

case "$ACTION" in 

    create)
        if [ -z $PARTITIONS ] || [ -z $ZK ]; then

            echo -e $USAGE
            exit 1           
        fi
        kafka-topics --create --partitions $PARTITIONS --replication-factor 1 --topic $TOPIC --zookeeper $ZK
        ;;

    delete)        
        if [  -z $ZK ]; then
            echo -e $USAGE
            exit 1
        fi
        kafka-topics --delete --topic $TOPIC --zookeeper $ZK        
        ;;

    *)
        echo -e $USAGE      
        exit 1    
esac


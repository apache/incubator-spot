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

DSOURCES=('flow' 'dns' 'proxy')
DFOLDERS=('binary' 'hive' 'stage')
source /etc/spot.conf

#
# creating HDFS user's folder
#
hadoop fs -mkdir ${HUSER}
hadoop fs -chown ${USER}:supergroup ${HUSER}

for d in "${DSOURCES[@]}" 
do 
	echo "creating /$d"
	hadoop fs -mkdir ${HUSER}/$d 
	for f in "${DFOLDERS[@]}" 
	do 
		echo "creating $d/$f"
		hadoop fs -mkdir ${HUSER}/$d/$f
	done
done

#
# create hive tables
#
#configure / create catalog
hive -e "CREATE DATABASE ${DBNAME}"

for d in "${DSOURCES[@]}" 
do 
	hive -hiveconf huser=${HUSER} -hiveconf dbname=${DBNAME} -f create_${d}_avro_parquet.hql
done



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
DFOLDERS=('binary' 
'stage'
'hive'
'hive/oa'
'hive/oa/chords'
'hive/oa/edge'
'hive/oa/summary'
'hive/oa/suspicious'
'hive/oa/storyboard'
'hive/oa/threat_investigation'
'hive/oa/timeline'
'hive/oa/dendro'
'hive/oa/threat_dendro'
)

# Sourcing spot configuration variables
source /etc/spot.conf

# Creating HDFS user's folder
sudo -u hdfs hdfs dfs -mkdir ${HUSER}
sudo -u hdfs hdfs dfs -chown ${USER}:supergroup ${HUSER}
sudo -u hdfs hdfs dfs -chmod 775 ${HUSER}

# Creating HDFS paths for each use case
for d in "${DSOURCES[@]}" 
do 
	echo "creating /$d"
	hdfs dfs -mkdir ${HUSER}/$d 
	for f in "${DFOLDERS[@]}" 
	do 
		echo "creating $d/$f"
		hdfs dfs -mkdir ${HUSER}/$d/$f
	done

	# Modifying permission on HDFS folders to allow Impala to read/write
	hdfs dfs -chmod -R 775 ${HUSER}/$d
	sudo -u hdfs hdfs dfs -setfacl -R -m user:impala:rwx ${HUSER}/$d
	sudo -u hdfs hdfs dfs -setfacl -R -m user:${USER}:rwx ${HUSER}/$d
done

# Creating Spot Database
impala-shell -i ${IMPALA_DEM} -q "CREATE DATABASE IF NOT EXISTS ${DBNAME};"

# Creating Impala tables
for d in "${DSOURCES[@]}" 
do 
	impala-shell -i ${IMPALA_DEM} --var=huser=${HUSER} --var=dbname=${DBNAME} -c -f create_${d}_parquet.hql
done


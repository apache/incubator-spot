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

DSOURCES=('odm')
DFOLDERS=(
'event' 
'user_context'
'endpoint_context'
'network_context'
'threat_intelligence_context'
'vulnerability_context'
)

# Sourcing ODM Spot configuration variables
source /etc/spot.conf

# Creating HDFS user's folder
sudo -u hdfs hdfs dfs -mkdir ${HUSER}
sudo -u hdfs hdfs dfs -chown ${USER}:supergroup ${HUSER}
sudo -u hdfs hdfs dfs -chmod 775 ${HUSER}

# Creating HDFS paths for each use case
for d in "${DSOURCES[@]}" 
do 
	echo "creating /$d"
	sudo -u hdfs hdfs dfs -mkdir ${HUSER}/$d 

	for f in "${DFOLDERS[@]}" 
	do 
		echo "creating $d/$f"
		sudo -u hdfs hdfs dfs -mkdir ${HUSER}/$d/$f
	done

	# Modifying permission on HDFS folders to allow Impala to read/write
	sudo -u hdfs hdfs dfs -chmod -R 775 ${HUSER}/$d
	sudo -u hdfs hdfs dfs -setfacl -R -m user:impala:rwx ${HUSER}/$d
	sudo -u hdfs hdfs dfs -setfacl -R -m user:${USER}:rwx ${HUSER}/$d
done

# Creating Spot Database
impala-shell -i ${IMPALA_DEM} -q "CREATE DATABASE IF NOT EXISTS ${DBNAME};"

# Creating ODM Impala tables
for d in "${DSOURCES[@]}" 
do 
    for f in "${DFOLDERS[@]}" 
	do 
        echo "Creating ODM Impala table..."
		echo "impala-shell -i ${IMPALA_DEM} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} -c -f create_${f}_pqt.sql"
        
        impala-shell -i ${IMPALA_DEM} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} -c -f create_${f}_pqt.sql
	done
done
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

# Instructions
#   To execute this script, run ./odm_setup with a format type (pqt, avro) as an argument.
#
#   i.e. ./odm_setup pqt
#   
#   NOTE: At this time only Parquet and Avro storage formats are supported for the ODM tables.

# Check the format argument and make sure its supported
format=$1
if [ "$format" != "pqt" ] && [ "$format" != "avro" ] ; then
    echo "Format argument '$format' is not supported. Only Parquet and Avro are supported data storage formats. Use 'pqt' or 'avro'  instead (i.e. ./odm_setup pqt)."
    exit 0
fi

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
    
    # Create Avro schemas directory on HDFS if Avro storage is selected
    if [ "$format" == "avro" ] ; then
        echo "creating /$d/schema"
        sudo -u hdfs hdfs dfs -mkdir ${HUSER}/$d/schema
    fi

	for f in "${DFOLDERS[@]}" 
	do 
		echo "creating $d/$f"
		sudo -u hdfs hdfs dfs -mkdir ${HUSER}/$d/$f
	done

	# Modifying permission on HDFS folders to allow Impala to read/write
    echo "modifying permissions recursively on ${HUSER}/$d"
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
        #If desired storage format is parquet, create ODM as Parquet tables
        if [ "$format" == "pqt" ] ; then
            echo "Creating ODM Impala Parquet table ${f}..."
            echo "impala-shell -i ${IMPALA_DEM} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} -c -f create_${f}_pqt.sql"
            
            impala-shell -i ${IMPALA_DEM} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} -c -f create_${f}_pqt.sql
        fi
        # If desired storage format is "avro", create ODM as Avro tables with Avro schemas
        if [ "$format" == "avro" ] ; then
            echo "Adding ${f} Avro schema to ${HUSER}/$d/schema ..."
            echo "sudo -u ${USER} hdfs dfs -put -f $f.avsc ${HUSER}/$d/schema/$f.avsc"
            
            sudo -u ${USER} hdfs dfs -put -f $f.avsc ${HUSER}/$d/schema/$f.avsc
        
            echo "Creating ODM Impala Avro table ${f}..."
            echo "impala-shell -i ${IMPALA_DEM} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} --var=ODM_AVRO_URL=hdfs://${HUSER}/${d}/schema/${f}.avsc -c -f create_${f}_avro.sql"
        
            impala-shell -i ${IMPALA_DEM} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} --var=ODM_AVRO_URL=hdfs://${HUSER}/${d}/schema/${f}.avsc -c -f create_${f}_avro.sql
        fi
	done
done
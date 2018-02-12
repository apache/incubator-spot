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


set -e

function log() {
    # General logger for the ODM setup script that prints any input provided to it
    printf "hdfs_setup.sh:\n $1\n"
}

function safe_mkdir() {
    # 1. Takes the hdfs command options and a directory
    # 2. Checks for the directory before trying to create it and keeps the script from creating existing directories

    local hdfs_cmd=$1
    local dir=$2
    if $(hdfs dfs -test -d ${dir}); then
        log "${dir} already exists"
    else
        log "running mkdir on ${dir}"
        ${hdfs_cmd} dfs -mkdir ${dir}
    fi
}

SPOTCONF="/etc/spot.conf"
DSOURCES=('odm')
DFOLDERS=(
'event' 
'user_context'
'endpoint_context'
'network_context'
'threat_intelligence_context'
'vulnerability_context'
)

# Check input argument options
for arg in "$@"; do
    case $arg in
        "--no-sudo")
            log "not using sudo"
            no_sudo=true
            shift
            ;;
        "-c")
            shift
            SPOTCONF=$1
            log "Spot Configuration file: ${SPOTCONF}"
            shift
            ;;
        "-f")
            shift
            format=$1
            shift
            ;;
    esac
done

# Check the format argument and make sure its supported
if [ "$format" != "pqt" ] && [ "$format" != "avro" ] ; then
    log "Format argument '$format' is not supported. Only Parquet and Avro are supported data storage formats. Use 'pqt' or 'avro'  instead (i.e. ./odm_setup pqt)."
    exit 1
fi

# Sourcing spot configuration variables
log "Sourcing ${SPOTCONF}\n"
source $SPOTCONF

# Check no-sudo argument and set the proper hdfs command to run our create table statements later
if [[ ${no_sudo} == "true" ]]; then
    hdfs_cmd="hdfs"

    if [[ ! -z "${HADOOP_USER_NAME}" ]]; then
        log "HADOOP_USER_NAME: ${HADOOP_USER_NAME}"
    else
        log "setting HADOOP_USER_NAME to hdfs"
        HADOOP_USER_NAME=hdfs
    fi
else
    hdfs_cmd="sudo -u hdfs hdfs"
fi

# Creating HDFS user's folder
log "creating ${HUSER}"
safe_mkdir ${hdfs_cmd} ${HUSER}
${hdfs_cmd} dfs -chown ${USER}:supergroup ${HUSER}
${hdfs_cmd} dfs -chmod 775 ${HUSER}

# Creating HDFS paths for each use case
for d in "${DSOURCES[@]}" 
do 
	log "creating /$d"
	safe_mkdir hdfs ${HUSER}/$d
    
    # Create Avro schemas directory on HDFS if Avro storage is selected
    if [ "$format" == "avro" ] ; then
        log "creating ${HUSER}/$d/schema"
        safe_mkdir ${hdfs_cmd} ${HUSER}/$d/schema
    fi

	for f in "${DFOLDERS[@]}" 
	do 
		log "creating ${HUSER}/$d/$f"
		safe_mkdir ${hdfs_cmd} ${HUSER}/$d/$f
	done

	# Modifying permission on HDFS folders to allow Impala to read/write
    log "modifying permissions recursively on ${HUSER}/$d"
	hdfs dfs -chmod -R 775 ${HUSER}/$d
	${hdfs_cmd} dfs -setfacl -R -m user:impala:rwx ${HUSER}/$d
	${hdfs_cmd} dfs -setfacl -R -m user:${USER}:rwx ${HUSER}/$d
done

# Check if Kerberos is enabled, and create the proper impala-shell configuration and arguments to be used when creating the ODM tables

log "Using Impala as execution engine."
impala_db_shell="impala-shell -i ${IMPALA_DEM}"
log "${impala_db_shell}"

if [[ ${KERBEROS} == "true" ]]; then
    log "Kerberos enabled. Modifying Impala Shell arguments"
    impala_db_shell="${impala_db_shell} -k"
    log "${impala_db_shell}"
fi

# Creating Spot Database

log "CREATE DATABASE IF NOT EXISTS ${DBNAME};"
${impala_db_shell} "CREATE DATABASE IF NOT EXISTS ${DBNAME}";

# Creating ODM Impala tables

for d in "${DSOURCES[@]}" 
do 
    for f in "${DFOLDERS[@]}" 
	do 
        # If desired storage format is parquet, create ODM as Parquet tables

        if [ "$format" == "pqt" ] ; then
            log "Creating ODM Impala Parquet table ${f}..."
            log "${impala_db_shell} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} -c -f create_${f}_pqt.sql"

            ${impala_db_shell} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} -c -f create_${f}_pqt.sql
        fi

        # If desired storage format is avro, create ODM as Avro tables with Avro schemas

        if [ "$format" == "avro" ] ; then
            log "Adding ${f} Avro schema to ${HUSER}/$d/schema ..."
            log "${hdfs_cmd} dfs -put -f $f.avsc ${HUSER}/$d/schema/$f.avsc"
            
            ${hdfs_cmd} dfs -put -f $f.avsc ${HUSER}/$d/schema/$f.avsc
        
            log "Creating ODM Impala Avro table ${f}..."
            log "${impala_db_shell} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} --var=ODM_AVRO_URL=hdfs://${HUSER}/${d}/schema/${f}.avsc -c -f create_${f}_avro.sql"
        
            ${impala_db_shell} --var=ODM_DBNAME=${DBNAME} --var=ODM_TABLENAME=${f} --var=ODM_LOCATION=${HUSER}/${d}/${f} --var=ODM_AVRO_URL=hdfs://${HUSER}/${d}/schema/${f}.avsc -c -f create_${f}_avro.sql
        fi
	done
done
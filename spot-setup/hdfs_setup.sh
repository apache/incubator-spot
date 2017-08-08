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

set -e

function log() {
printf "hdfs_setup.sh:\n $1\n"
}

SPOTCONF="/etc/spot.conf"
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


# input options
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
    esac
done

# Sourcing spot configuration variables
log "Sourcing ${SPOTCONF}\n"
source $SPOTCONF

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

db_engine=$(echo ${DBENGINE} | tr '[:upper:]' '[:lower:]')

case ${db_engine} in
    impala)
    db_shell="impala-shell -i ${IMPALA_DEM}"
    db_query="${db_shell} -q"
    db_script="${db_shell} --var=huser=${HUSER} --var=dbname=${DBNAME} -c -f"
    ;;
    hive)
    db_shell="hive"
    db_query="${db_shell} -e"
    db_script="${db_shell} -hiveconf huser=${HUSER} -hiveconf dbname=${DBNAME} -f"
    ;;
    *)
    log "$DBENGINE not compatible"
    exit 1
    ;;
esac

# Creating HDFS user's folder
${hdfs_cmd} dfs -mkdir ${HUSER}
${hdfs_cmd} dfs -chown ${USER}:supergroup ${HUSER}
${hdfs_cmd} dfs -chmod 775 ${HUSER}

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
	${hdfs_cmd} dfs -setfacl -R -m user:${db_engine}:rwx ${HUSER}/$d
	${hdfs_cmd} dfs -setfacl -R -m user:${USER}:rwx ${HUSER}/$d
done

# Creating Spot Database
 ${db_query} "CREATE DATABASE IF NOT EXISTS ${DBNAME}";


# Creating Impala tables
for d in "${DSOURCES[@]}" 
do 
	${db_script} "./${db_engine}/create_${d}_parquet.hql"
done
#!/usr/bin/env bash

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

help() {
    echo -n "
   Initialize folders and databases for Spot in Hadoop.

 Options:
  --no-sudo         Do not use sudo with hdfs commands.
  -c                Specify config file (default = /etc/spot.conf)
  -d                Override databases
  -h, --help        Display this help and exit
" 
exit 0
}

function log() {
printf "hdfs_setup.sh:\\n %s\\n\\n" "$1"
}

function safe_mkdir() {
        # takes the hdfs command options and a directory
        # checks for the directory before trying to create it
        # keeps the script from existing on existing folders
        local hdfs_cmd=$1
        local dir=$2
        if hdfs dfs -test -d "${dir}"; then
            log "${dir} already exists"
        else
            log "running mkdir on ${dir}"
            ${hdfs_cmd} dfs -mkdir "${dir}"
        fi
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
        "-d")
            shift
            db_override=$1
            shift
            ;;
        "-h"|"--help")
            help
            ;;
    esac
done

# Sourcing spot configuration variables
log "Sourcing ${SPOTCONF}"
source "$SPOTCONF"

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

if [[ -z "${db_override}" ]]; then
        DBENGINE=$(echo "${DBENGINE}" | tr '[:upper:]' '[:lower:]')
        log "setting database engine to ${DBENGINE}"
else
        DBENGINE=$(echo "${db_override}" | tr '[:upper:]' '[:lower:]')
        log "setting database engine to $db_override"
fi

case ${DBENGINE} in
    impala)
        db_shell="impala-shell -i ${IMPALA_DEM}"
        if [[ ${KERBEROS} == "true" ]]; then
            db_shell="${db_shell} -k"
        fi
        db_query="${db_shell} -q"
        db_script="${db_shell} --var=huser=${HUSER} --var=dbname=${DBNAME} -c -f"
        ;;
    hive)
        if [[ ${no_sudo} == "true" ]]; then
            db_shell="hive"
        else
            db_shell="sudo -u hive hive"
        fi
        db_query="${db_shell} -e"
        db_script="${db_shell} -hiveconf huser=${HUSER} -hiveconf dbname=${DBNAME} -f"
        ;;
    beeline)
        db_shell="beeline -u jdbc:${JDBC_URL}"
        db_query="${db_shell} -e"
        db_script="${db_shell} --hivevar huser=${HUSER} --hivevar dbname=${DBNAME} -f"
        ;;
    *)
        log "DBENGINE not compatible or not set in spot.conf: DBENGINE--> ${DBENGINE:-empty}"
        exit 1
        ;;
esac

# Creating HDFS user's folder
safe_mkdir "${hdfs_cmd}" "${HUSER}"
${hdfs_cmd} dfs -chown "${USER}":supergroup "${HUSER}"
${hdfs_cmd} dfs -chmod 775 "${HUSER}"

# Creating HDFS paths for each use case
for d in "${DSOURCES[@]}" 
do
	echo "creating /$d"
	safe_mkdir "${hdfs_cmd}" "${HUSER}/$d"
	for f in "${DFOLDERS[@]}" 
	do 
		echo "creating $d/$f"
		safe_mkdir "${hdfs_cmd}" "${HUSER}/$d/$f"
	done

	# Modifying permission on HDFS folders to allow Impala to read/write
	${hdfs_cmd} dfs -chmod -R 775 "${HUSER}"/"$d"
	${hdfs_cmd} dfs -setfacl -R -m user:"${db_override}":rwx "${HUSER}"/"$d"
	${hdfs_cmd} dfs -setfacl -R -m user:"${USER}":rwx "${HUSER}"/"$d"
done


# Creating Spot Database
log "Creating Spot Database"
${db_query} "CREATE DATABASE IF NOT EXISTS ${DBNAME}";


# Creating tables
log "Creating Database tables"
for d in "${DSOURCES[@]}" 
do
	${db_script} "./${DBENGINE}/create_${d}_parquet.hql"
done

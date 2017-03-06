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

if [ $# -eq 1 ]
then
	SUPERGROUP=$1
else
	SUPERGROUP="supergroup"
fi
  
DSOURCES=('flow' 'dns' 'proxy')
DFOLDERS=('binary' 'hive' 'stage')

if [ ! -e "/etc/spot.conf" ]
then
	echo "Please, copy spot.conf under /etc/spot.conf and edit it before running this script"
	exit 11
fi

if ! groups | grep -c "\b${SUPERGROUP}\b" &>/dev/null
then
	echo "Current user doesn't belong to hdfs supergroup '${SUPERGROUP}'"
	echo "If you've another group configured, please run this script as $0 supergroupname"
	exit 12
fi

# Adapted to get it ready for INI file format ignoring sections. Don't add the same var name under 2 different sections
# without adapting this 1st!
source <(grep "=" /etc/spot.conf| sed "s/[#;].*//")
if [ $? -ne 0 ]
then
	echo "There's some error with /etc/spot.conf file, please double check it"
	exit 13
fi

if [ "${KRB_AUTH}" = "true" ]
then
	if [ '!' -x "${KINITPATH}" ]
	then
		echo "Wrong '${KINITPATH}' or not executable"
		exit 14
	elif [ '!' -s "${KEYTABPATH}" ]
	then
		echo "Wrong '${KEYTABPATH}'"
		exit 15
	elif [ -z "${KRB_USER}" ]
	then
		echo "No Kerberos User provided '${KRB_USER}'"
		exit 16
	else
		${KINITPATH} ${KINITOPTS} -k -t ${KEYTABPATH} ${KRB_USER}
		if [ $? -ne 0 ]
		then
			echo "Failed to get ticket from Kerberos:"
			echo "${KINITPATH} ${KINITOPTS} -k -t ${KEYTABPATH} ${KRB_USER}"
			exit 17
		fi
	fi

fi
#
# creating HDFS user's folder
#
hadoop fs -mkdir ${HUSER}
if [ $? -ne 0 ]
then
	echo "unable to create hdfs folder '${HUSER}', check your hdfs permissions or remove it before running this script again"
	exit 18
fi


for d in "${DSOURCES[@]}" 
do 
	echo "creating ${HUSER}/$d"
	hadoop fs -mkdir ${HUSER}/$d 
	for f in "${DFOLDERS[@]}" 
	do 
		echo "creating ${HUSER}/$d/$f"
		hadoop fs -mkdir ${HUSER}/$d/$f
	done
done
# Do it recursive
hadoop fs -chown -R ${USER}:supergroup ${HUSER}

#
# create hive tables
#
#configure / create catalog
hive -e "CREATE DATABASE ${DBNAME}"
if [ $? -ne 0 ]
then
	echo "Error creating HIVE Database '${DBNAME}', does it exist?"
	echo "Please drop it and remove ${HUSER} before running this setup again"
	exit 19
fi

for d in "${DSOURCES[@]}" 
do 
	hive -hiveconf huser=${HUSER} -hiveconf dbname=${DBNAME} -f create_${d}_avro_parquet.hql
	if [ $? -ne 0 ]
	then
		echo "Problem importing table '${d}', please check"
		exit 20
	fi
done

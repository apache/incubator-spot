#!/bin/bash

DSOURCES=('flow' 'dns' 'proxy')
DFOLDERS=('binary' 'hive' 'stage')
source /etc/duxbay.conf

#
# creating HDFS user's folder
#
sudo -u hdfs hadoop fs -mkdir ${HUSER}
sudo -u hdfs hadoop fs -chown ${USER}:supergroup ${HUSER}

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



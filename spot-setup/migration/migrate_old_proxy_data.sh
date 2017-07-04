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

OLD_DATA_PATH=$1
STAGING_DB=$2
HDFS_STAGING_PATH=$3
DEST_DB=$4
IMPALA_DEM=$5

# Execution example:
#./migrate_old_proxy_data.sh '/home/spot/spot-csv-data' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'

hadoop fs -mkdir $HDFS_STAGING_PATH
hadoop fs -mkdir $HDFS_STAGING_PATH/proxy/
hadoop fs -mkdir $HDFS_STAGING_PATH/proxy/scores/
hadoop fs -mkdir $HDFS_STAGING_PATH/proxy/edge/
hadoop fs -mkdir $HDFS_STAGING_PATH/proxy/summary/
hadoop fs -mkdir $HDFS_STAGING_PATH/proxy/storyboard/
hadoop fs -mkdir $HDFS_STAGING_PATH/proxy/timeline/
hdfs dfs -setfacl -R -m user:impala:rwx $HDFS_STAGING_PATH


# Creating Staging tables in Impala
impala-shell -i ${IMPALA_DEM} --var=hpath=${HDFS_STAGING_PATH} --var=dbname=${STAGING_DB} -c -f create_proxy_migration_tables.hql


# Proxy Ingest Summary
echo "Processing proxy Ingest Summary"

ing_sum_path=$OLD_DATA_PATH/proxy/ingest_summary/is_??????.csv

for file in $ing_sum_path
do 
  echo $file
  ./import_ingest_summary.py "${file}" "${STAGING_DB}" 'proxy_ingest_summary_tmp' "${DEST_DB}" 'proxy_ingest_summary'
done


DAYS=$OLD_DATA_PATH/proxy/2*

for dir in $DAYS
do

  day="$(basename $dir)"
  echo "Processing day $day ..."
  y=${day:0:4}
  m=$(expr ${day:4:2} + 0)
  d=$(expr ${day:6:2} + 0)
  echo $y $m $d $d2
  echo $dir


  ## proxy Scores and proxy_threat_investigation
  echo "Processing proxy Scores"
  if [ -f $dir/proxy_scores.tsv ]
  then
    command="LOAD DATA LOCAL INPATH '$dir/proxy_scores.tsv' OVERWRITE INTO TABLE $STAGING_DB.proxy_scores_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.proxy_scores PARTITION (y=$y, m=$m, d=$d) 
select tdate, time, clientip, host, reqmethod, useragent, resconttype, duration, username, webcat, referer, respcode, uriport, uripath, uriquery, serverip, scbytes, csbytes, fulluri, word, ml_score, uri_rep, respcode_name, network_context 
from $STAGING_DB.proxy_scores_tmp;"

    echo $command
    hive -e "$command"

    echo "Processing proxy Threat Investigation"
    command="INSERT INTO $DEST_DB.proxy_threat_investigation PARTITION (y=$y, m=$m, d=$d) 
select tdate, fulluri, uri_sev
from $STAGING_DB.proxy_scores_tmp
where uri_sev > 0;"
    echo $command
    hive -e "$command"

  fi


  ## proxy Edge
  echo "Processing proxy Edge"
  edge_files=`ls $dir/edge*.tsv`
  #echo $edge_files
  if [ ! -z "$edge_files" ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/edge*.tsv' OVERWRITE INTO TABLE $STAGING_DB.proxy_edge_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.proxy_edge PARTITION (y=$y, m=$m, d=$d) 
select tdate, time, clientip, host, webcat, respcode, reqmethod, useragent, resconttype, referer, uriport, serverip, scbytes, csbytes, fulluri, 0, ''
from $STAGING_DB.proxy_edge_tmp;"
    echo $command
    hive -e "$command"

  fi
  

  ##proxy_storyboard
  echo "Processing proxy Storyboard"
  if [ -f $dir/threats.csv ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/threats.csv' OVERWRITE INTO TABLE $STAGING_DB.proxy_storyboard_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.proxy_storyboard PARTITION (y=$y, m=$m, d=$d) 
select p_threat, title, text
from $STAGING_DB.proxy_storyboard_tmp;"
    echo $command
    hive -e "$command"

  fi

  ##proxy_timeline
  echo "Processing proxy Timeline"
  timeline_files=`ls $dir/timeline*.tsv`
  
  if [ ! -z "$timeline_files" ]
  then
    for file in $timeline_files
    do

      echo $file
      ./import_proxy_timeline.py "${file}" "${STAGING_DB}" 'proxy_timeline_tmp' "${DEST_DB}" 'proxy_timeline' "${y}-${m}-${d}"

    done
  fi
done


# Dropping staging tables
impala-shell -i ${IMPALA_DEM} --var=dbname=${STAGING_DB} -c -f drop_proxy_migration_tables.hql

# Removing staging tables' path in HDFS
hadoop fs -rm -r $HDFS_STAGING_PATH/proxy/

# Moving CSV data to backup folder
mkdir $OLD_DATA_PATH/backup/
mv $OLD_DATA_PATH/proxy/ $OLD_DATA_PATH/backup/

# Invalidating metadata in Impala to refresh tables content
impala-shell -i ${IMPALA_DEM} -q "INVALIDATE METADATA;"



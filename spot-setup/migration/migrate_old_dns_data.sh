#!/bin/bash
OLD_DATA_PATH=$1
STAGING_DB=$2
HDFS_STAGING_PATH=$3
DEST_DB=$4
IMPALA_DEM=$5

# Execution example:
#./migrate_old_dns_data.sh '/home/spot/spot-csv-data' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'

# OLD_DATA_PATH='/home/spot/spot-csv-data'
# STAGING_DB='spot_migration'
# HDFS_STAGING_PATH='/user/spot/spot_migration/'
# DEST_DB='migrated'
# IMPALA_DEM='node01'


hadoop fs -mkdir $HDFS_STAGING_PATH
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/scores/
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/dendro/
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/edge/
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/summary/
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/storyboard/
hadoop fs -mkdir $HDFS_STAGING_PATH/dns/threat_dendro/
hdfs dfs -setfacl -R -m user:impala:rwx $HDFS_STAGING_PATH

#Creating Staging tables in Impala
impala-shell -i ${IMPALA_DEM} --var=hpath=${HDFS_STAGING_PATH} --var=dbname=${STAGING_DB} -c -f create_dns_migration_tables.hql

DAYS=$OLD_DATA_PATH/dns/*

for dir in $DAYS
do
  #break
  #echo $dir
  day="$(basename $dir)"
  #echo $day
  echo "Processing day $day ..."
  y=${day:0:4}
  m=$(expr ${day:4:2} + 0)
  d=$(expr ${day:6:2} + 0)
  echo $y $m $d $d2
  echo $dir


  ## dns Scores and dns_threat_investigation
  echo "Processing dns Scores"
  if [ -f $dir/dns_scores.csv ]
  then
    command="LOAD DATA LOCAL INPATH '$dir/dns_scores.csv' OVERWRITE INTO TABLE $STAGING_DB.dns_scores_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.dns_scores PARTITION (y=$y, m=$m, d=$d) 
select frame_time, unix_tstamp, frame_len, ip_dst, dns_qry_name, dns_qry_class, dns_qry_type, dns_qry_rcode, ml_score, tld, query_rep, hh, dns_qry_class_name, dns_qry_type_name, dns_qry_rcode_name, network_context
from $STAGING_DB.dns_scores_tmp;"
    echo $command
    hive -e "$command"

    echo "Processing dns Threat Investigation"
    command="INSERT INTO $DEST_DB.dns_threat_investigation PARTITION (y=$y, m=$m, d=$d) 
select unix_tstamp, ip_dst, dns_qry_name, ip_sev, dns_sev
from $STAGING_DB.dns_scores_tmp
where ip_sev > 0 or dns_sev > 0;"
    echo $command
    hive -e "$command"

  fi

  ## dns Dendro
  echo "Processing dns Dendro"
  dendro_files=`ls $dir/dendro*.csv`
  #echo $dendro_files
  if [ ! -z "$dendro_files" ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/dendro*.csv' OVERWRITE INTO TABLE $STAGING_DB.dns_dendro_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.dns_dendro PARTITION (y=$y, m=$m, d=$d) 
select unix_timestamp('$y${day:4:2}${day:6:2}', 'yyyyMMMdd'), dns_a, dns_qry_name, ip_dst
from $STAGING_DB.dns_dendro_tmp;"
    echo $command 
    hive -e "$command"

  fi

  ## dns Edge
  echo "Processing dns Edge"
  edge_files=`ls $dir/edge*.csv`
  #echo $edge_files
  if [ ! -z "$edge_files" ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/edge*.csv' OVERWRITE INTO TABLE $STAGING_DB.dns_edge_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.dns_edge PARTITION (y=$y, m=$m, d=$d) 
select unix_timestamp(regexp_replace(frame_time, '\"', ''), 'MMMMM dd yyyy H:mm:ss.SSS z'), frame_len, ip_dst, ip_src, dns_qry_name, dns_qry_class, dns_qry_type, dns_qry_rcode, dns_a, 0, '', '', '', ''
from $STAGING_DB.dns_edge_tmp;"
    echo $command
    hive -e "$command"

  fi

  ##dns_ingest_summary

  ##dns_storyboard
  echo "Processing dns Storyboard"
  if [ -f $dir/threats.csv ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/threats.csv' OVERWRITE INTO TABLE $STAGING_DB.dns_storyboard_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.dns_storyboard PARTITION (y=$y, m=$m, d=$d) 
select ip_threat, dns_threat, title, text from $STAGING_DB.dns_storyboard_tmp;"
    echo $command
    hive -e "$command"

  fi

  ##dns_threat_dendro
  echo "Processing dns Threat Dendro"
  threat_dendro_files=`ls $dir/threat-dendro*.csv`
  #echo $threat_dendro_files
  if [ ! -z "$threat_dendro_files" ]
  then
    for file in $threat_dendro_files
    do
      #echo $file
      filename="$(basename $file)"
      ip="${filename%.tsv}"
      ip="${ip#threat-dendro-}"
      echo $filename $ip

      command="LOAD DATA LOCAL INPATH '$file' OVERWRITE INTO TABLE $STAGING_DB.dns_threat_dendro_tmp;"
      echo $command
      hive -e "$command"

      command="INSERT INTO $DEST_DB.dns_threat_dendro PARTITION (y=$y, m=$m, d=$d) 
select '$ip', total, dns_qry_name, ip_dst
from $STAGING_DB.dns_threat_dendro_tmp;"
      echo $command
      hive -e "$command"

    done
  fi
done

impala-shell -i ${IMPALA_DEM} -q "INVALIDATE METADATA;"



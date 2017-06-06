#!/bin/bash
OLD_DATA_PATH=$1
STAGING_DB=$2
HDFS_STAGING_PATH=$3
DEST_DB=$4
IMPALA_DEM=$5

# Execution example:
#./migrate_old_flow_data.sh '/home/spot/spot-csv-data' 'spot_migration' '/user/duxbury/spot_migration/' 'migrated' 'node01'

# OLD_DATA_PATH='/home/spot/spot-csv-data'
# STAGING_DB='spot_migration'
# HDFS_STAGING_PATH='/user/spot/spot_migration/'
# DEST_DB='migrated'
# IMPALA_DEM='node01'


hadoop fs -mkdir $HDFS_STAGING_PATH
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/scores/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/chords/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/edge/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/summary/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/storyboard/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/threat_investigation/
hadoop fs -mkdir $HDFS_STAGING_PATH/flow/timeline/
hdfs dfs -setfacl -R -m user:impala:rwx $HDFS_STAGING_PATH

#Creating Staging tables in Impala
impala-shell -i ${IMPALA_DEM} --var=hpath=${HDFS_STAGING_PATH} --var=dbname=${STAGING_DB} -c -f create_flow_migration_tables.hql


# ##flow_ingest_summary
# echo "Processing Flow Ingest Summary"

# if [ -f $OLD_DATA_PATH/flow/ingest_summary/is-*.csv ]
#   then
#     command="LOAD DATA LOCAL INPATH '$OLD_DATA_PATH/flow/ingest_summary/is-*.csv' OVERWRITE INTO TABLE $STAGING_DB.flow_ingest_summary_tmp;"
#     echo $command
#     hive -e "$command"

#     command="INSERT INTO $DEST_DB.flow_ingest_summary PARTITION (y=$y, m=$m, d=$d) 
# select tdate, total
# from $STAGING_DB.flow_ingest_summary_tmp;"
#     echo $command
#     hive -e "$command"

#   fi


DAYS=$OLD_DATA_PATH/flow/*

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


  ## Flow Scores and flow_threat_investigation
  echo "Processing Flow Scores"
  if [ -f $dir/flow_scores.csv ]
  then
    command="LOAD DATA LOCAL INPATH '$dir/flow_scores.csv' OVERWRITE INTO TABLE $STAGING_DB.flow_scores_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.flow_scores PARTITION (y=$y, m=$m, d=$d) 
select tstart,srcip,dstip,sport,dport,proto,ipkt,ibyt,0,0,lda_score,rank,srcIpInternal,destIpInternal,srcGeo,dstGeo,srcDomain,dstDomain,srcIP_rep,dstIP_rep 
from $STAGING_DB.flow_scores_tmp;"
    echo $command
    hive -e "$command"

    echo "Processing Flow Threat Investigation"
    command="INSERT INTO $DEST_DB.flow_threat_investigation PARTITION (y=$y, m=$m, d=$d) 
select tstart,srcip,dstip,sport,dport,sev
from $STAGING_DB.flow_scores_tmp
where sev > 0;"
    echo $command
    hive -e "$command"

  fi

  ## Flow Chords
  echo "Processing Flow Chords"
  chord_files=`ls $dir/chord*.tsv`
  #echo $chord_files
  if [ ! -z "$chord_files" ]
  then
    for file in $chord_files
    do
      #echo $file
      filename="$(basename $file)"
      ip="${filename%.tsv}"
      ip="${ip#chord-}"
      ip="${ip//_/.}"
      echo $filename $ip

      command="LOAD DATA LOCAL INPATH '$file' OVERWRITE INTO TABLE $STAGING_DB.flow_chords_tmp;"
      echo $command
      hive -e "$command"

      command="INSERT INTO $DEST_DB.flow_chords PARTITION (y=$y, m=$m, d=$d) 
  select '$ip', srcip, dstip, ibyt, ipkt from $STAGING_DB.flow_chords_tmp;"
      echo $command
      hive -e "$command"

    done
  fi

  ## Flow Edge
  echo "Processing Flow Edge"
  edge_files=`ls $dir/edge*.tsv`
  #echo $edge_files
  if [ ! -z "$edge_files" ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/edge*.tsv' OVERWRITE INTO TABLE $STAGING_DB.flow_edge_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.flow_edge PARTITION (y=$y, m=$m, d=$d) 
select tstart, srcip, dstip, sport, dport, proto, flags, tos, ibyt, ipkt, input, output, rip, obyt, opkt, 0, 0
from $STAGING_DB.flow_edge_tmp
where srcip is not NULL;"
    echo $command
    hive -e "$command"

  fi

  ##flow_storyboard
  echo "Processing Flow Storyboard"
  if [ -f $dir/threats.csv ]
  then

    command="LOAD DATA LOCAL INPATH '$dir/threats.csv' OVERWRITE INTO TABLE $STAGING_DB.flow_storyboard_tmp;"
    echo $command
    hive -e "$command"

    command="INSERT INTO $DEST_DB.flow_storyboard PARTITION (y=$y, m=$m, d=$d) 
select ip_threat, title, text from $STAGING_DB.flow_storyboard_tmp;"
    echo $command
    hive -e "$command"

  fi

  ##flow_timeline
  echo "Processing Flow Timeline"
  timeline_files=`ls $dir/sbdet*.tsv`
  #echo $timeline_files
  if [ ! -z "$timeline_files" ]
  then
    for file in $timeline_files
    do
      #echo $file
      filename="$(basename $file)"
      ip="${filename%.tsv}"
      ip="${ip#sbdet-}"
      echo $filename $ip

      command="LOAD DATA LOCAL INPATH '$file' OVERWRITE INTO TABLE $STAGING_DB.flow_timeline_tmp;"
      echo $command
      hive -e "$command"

      command="INSERT INTO $DEST_DB.flow_timeline PARTITION (y=$y, m=$m, d=$d) 
  select '$ip', tstart, tend, srcip, dstip, proto, sport, dport, ipkt, ibyt from $STAGING_DB.flow_timeline_tmp;"
      echo $command
      hive -e "$command"

    done
  fi
done

impala-shell -i ${IMPALA_DEM} -q "INVALIDATE METADATA;"



#@/bin/bash
SOURCE_TYPE=$1

ROOT_DIR=.
NEW_FILES_DIR=$ROOT_DIR/$SOURCE_TYPE/new
PROCESSED_FILES_DIR=$ROOT_DIR/$SOURCE_TYPE/stage
FLUME_SPOOL_DIR=$ROOT_DIR/$SOURCE_TYPE/flume_spool

for src_type in dns flow proxy; do for flume_dir in new stage flume_spool; do mkdir -p $src_type/$flume_dir; done; done

# start the flume agent in the background
flume-ng agent --conf-file spot_flume_${SOURCE_TYPE}.conf --name a1 > $ROOT_DIR/spot_flume_${SOURCE_TYPE}.log 2>&1 &
FLUME_PID=$!
trap "kill -9 $FLUME_PID; exit 1" SIGINT SIGTERM

while true
do
  for fname in `ls $NEW_FILES_DIR`
  do
    if [ $SOURCE_TYPE = "dns" ]
    then
      tshark -r $NEW_FILES_DIR/$fname -E separator=, -E header=y -E occurrence=f -T fields -e frame.time -e frame.time_epoch -e frame.len -e ip.src -e ip.dst -e dns.resp.name -e dns.resp.type -e dns.resp.class -e dns.flags.rcode -e dns.a 'dns.flags.response == 1' > $PROCESSED_FILES_DIR/dns.log
    elif [ $SOURCE_TYPE = "flow" ]
    then
      nfdump -r $NEW_FILES_DIR/$fname -o csv > $PROCESSED_FILES_DIR/flow.log
    elif [ $SOURCE_TYPE = "proxy" ] 
    then
      unzip $NEW_FILES_DIR/$fname -d $PROCESSED_FILES_DIR
    else
      echo "USAGE:  process_files.sh <source_type> (valid source types:  \"proxy\", \"dns\", \"flow\")"
      exit 1
    fi 
    mv $PROCESSED_FILES_DIR/*.log $FLUME_SPOOL_DIR
    rm -f $NEW_FILES_DIR/$fname
  done
  rm -f $FLUME_SPOOL_DIR/*.COMPLETED
  sleep 5
done

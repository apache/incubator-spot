#!/bin/bash

# parse and validate arguments

FDATE=$1
DSOURCE=$2

YR=${FDATE:0:4}
MH=${FDATE:4:2}
DY=${FDATE:6:2}

if [[ "${#FDATE}" != "8" || -z "${DSOURCE}" ]]; then
    echo "ml_ops.sh syntax error"
    echo "Please run ml_ops.sh again with the correct syntax:"
    echo "./ml_ops.sh YYYYMMDD TYPE [TOL]"
    echo "for example:"
    echo "./ml_ops.sh 20160122 dns 1e-6"
    echo "./ml_ops.sh 20160122 flow"
    exit
fi


# read in variables (except for date) from etc/.conf file
# note: FDATE and DSOURCE *must* be defined prior sourcing this conf file

source /etc/spot.conf

# third argument if present will override default TOL from conf file

if [ -n "$3" ]; then TOL=$3 ; fi

if [ -n "$4" ]; then
    MAXRESULTS=$4
else
    MAXRESULTS=-1
fi

# prepare parameters pipeline stages

if [ "$DSOURCE" == "flow" ]; then
    RAWDATA_PATH=${FLOW_PATH}
elif [ "$DSOURCE" == "dns" ]; then
    RAWDATA_PATH=${DNS_PATH}
else
    RAWDATA_PATH=${PROXY_PATH}
fi

FEEDBACK_PATH=${LPATH}/${DSOURCE}_scores.csv
DUPFACTOR=1000

PREPROCESS_STEP=${DSOURCE}_pre_lda
POSTPROCESS_STEP=${DSOURCE}_post_lda

HDFS_WORDCOUNTS=${HPATH}/word_counts

# paths for intermediate files
HDFS_DOCRESULTS=${HPATH}/doc_results.csv
LOCAL_DOCRESULTS=${LPATH}/doc_results.csv

HDFS_WORDRESULTS=${HPATH}/word_results.csv
LOCAL_WORDRESULTS=${LPATH}/word_results.csv

HDFS_SCORED_CONNECTS=${HPATH}/scores
HDFS_MODEL=${HPATH}/model

LDA_OUTPUT_DIR=${DSOURCE}/${FDATE}

TOPIC_COUNT=20

nodes=${NODES[0]}
for n in "${NODES[@]:1}" ; do nodes+=",${n}"; done

hdfs dfs -rm -R -f ${HDFS_WORDCOUNTS}
wait

mkdir -p ${LPATH}
rm -f ${LPATH}/*.{dat,beta,gamma,other,pkl} # protect the flow_scores.csv file

hdfs dfs -rm -R -f ${HDFS_SCORED_CONNECTS}

# Add -p <command> to execute pre MPI command.
# Pre MPI command can be configured in /etc/spot.conf
# In this script, after the line after --mpicmd ${MPI_CMD} add:
# --mpiprep ${MPI_PREP_CMD}

${MPI_PREP_CMD}

time spark-submit --class "org.apache.spot.SuspiciousConnects" \
  --master yarn-client \
  --driver-memory ${SPK_DRIVER_MEM} \
  --num-executors ${SPK_EXEC} \
  --conf spark.driver.maxResultSize=${SPK_DRIVER_MAX_RESULTS} \
  --conf spark.driver.maxPermSize=512m \
  --conf spark.dynamicAllocation.enabled=true \
  --conf spark.executor.cores=${SPK_EXEC_CORES} \
  --conf spark.executor.memory=${SPK_EXEC_MEM} \
  --conf "spark.executor.extraJavaOptions=-XX:MaxPermSize=512M -XX:PermSize=512M" \
  --conf spark.kryoserializer.buffer.max=512m \
  --conf spark.yarn.am.waitTime=100s \
  --conf spark.yarn.am.memoryOverhead=${SPK_DRIVER_MEM_OVERHEAD} \
  --conf spark.yarn.executor.memoryOverhead=${SPAK_EXEC_MEM_OVERHEAD} target/scala-2.10/spot-ml-assembly-1.1.jar \
  --analysis ${DSOURCE} \
  --input ${RAWDATA_PATH}  \
  --dupfactor ${DUPFACTOR} \
  --feedback ${FEEDBACK_PATH} \
  --model ${LPATH}/model.dat \
  --topicdoc ${LPATH}/final.gamma \
  --topicword ${LPATH}/final.beta \
  --lpath ${LPATH} \
  --ldapath ${LDAPATH} \
  --luser ${LUSER} \
  --mpicmd ${MPI_CMD}  \
  --proccount ${PROCESS_COUNT} \
  --topiccount ${TOPIC_COUNT} \
  --nodes ${nodes} \
  --scored ${HDFS_SCORED_CONNECTS} \
  --tempmodel ${HDFS_MODEL} \
  --threshold ${TOL} \
  --maxresults ${MAXRESULTS}

wait

# move results to hdfs.
cd ${LPATH}
hadoop fs -getmerge ${HDFS_SCORED_CONNECTS}/part-* ${DSOURCE}_results.csv && hadoop fs -moveFromLocal \
    ${DSOURCE}_results.csv  ${HDFS_SCORED_CONNECTS}/${DSOURCE}_results.csv
#!/bin/bash
SOURCE_TYPE=$1
ENVELOPE_JAR=envelope/build/envelope/target/envelope-*.jar

if [ "$SOURCE_TYPE" = "dns" ] || [ "$SOURCE_TYPE" = "flow" ] || [ "$SOURCE_TYPE" = "proxy" ]
then
  SPARK_KAFKA_VERSION=0.10 spark2-submit $ENVELOPE_JAR workers/spot_${SOURCE_TYPE}.conf >> spot_${SOURCE_TYPE}_ingest_spark_driver.log 2>&1 &
else
  echo "USAGE:  start_ingest.sh <source_type> (valid source types:  \"proxy\", \"dns\", \"flow\")"
fi

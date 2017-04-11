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

import argparse
import re
import shlex

from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.streaming.kafka import KafkaUtils
from pyspark.sql import HiveContext
from pyspark.sql.types import *

rex_date = re.compile("\d{4}-\d{2}-\d{2}")

proxy_schema = StructType([
                                    StructField("p_date", StringType(), True),
                                    StructField("p_time", StringType(), True),
                                    StructField("clientip", StringType(), True),
                                    StructField("host", StringType(), True),
                                    StructField("reqmethod", StringType(), True),
                                    StructField("useragent", StringType(), True),
                                    StructField("resconttype", StringType(), True),
                                    StructField("duration", IntegerType(), True),
                                    StructField("username", StringType(), True),
                                    StructField("authgroup", StringType(), True),
                                    StructField("exceptionid", StringType(), True),
                                    StructField("filterresult", StringType(), True),
                                    StructField("webcat", StringType(), True),
                                    StructField("referer", StringType(), True),
                                    StructField("respcode", StringType(), True),
                                    StructField("action", StringType(), True),
                                    StructField("urischeme", StringType(), True),
                                    StructField("uriport", StringType(), True),
                                    StructField("uripath", StringType(), True),
                                    StructField("uriquery", StringType(), True),
                                    StructField("uriextension", StringType(), True),
                                    StructField("serverip", StringType(), True),
                                    StructField("scbytes", IntegerType(), True),
                                    StructField("csbytes", IntegerType(), True),
                                    StructField("virusid", StringType(), True),
                                    StructField("bcappname", StringType(), True),
                                    StructField("bcappoper", StringType(), True),
                                    StructField("fulluri", StringType(), True),
                                    StructField("y", StringType(), True),
                                    StructField("m", StringType(), True),
                                    StructField("d", StringType(), True),
                                    StructField("h", StringType(), True)])

def main():
    
    # input Parameters
    parser = argparse.ArgumentParser(description="Bluecoat Parser")
    parser.add_argument('-zk','--zookeeper',dest='zk',required=True,help='Zookeeper IP and port (i.e. 10.0.0.1:2181)',metavar='')
    parser.add_argument('-t','--topic',dest='topic',required=True,help='Topic to listen for Spark Streaming',metavar='')
    parser.add_argument('-db','--database',dest='db',required=True,help='Hive database whete the data will be ingested',metavar='')
    parser.add_argument('-dt','--db-table',dest='db_table',required=True,help='Hive table whete the data will be ingested',metavar='')
    parser.add_argument('-w','--num_of_workers',dest='num_of_workers',required=True,help='Num of workers for Parallelism in Data Processing',metavar='')
    parser.add_argument('-bs','--batch-size',dest='batch_size',required=True,help='Batch Size (Milliseconds)',metavar='')
    args = parser.parse_args()

    # start collector based on data source type.
    bluecoat_parse(args.zk,args.topic,args.db,args.db_table,args.num_of_workers,args.batch_size)

def spot_decoder(s):

    if s is None:
        return None
    return s

def split_log_entry(line):

    lex = shlex.shlex(line)
    lex.quotes = '"'
    lex.whitespace_split = True
    lex.commenters = ''
    return list(lex)

def proxy_parser(proxy_fields):
    
    proxy_parsed_data = []

    if len(proxy_fields) > 1:

        # create full URI.
        proxy_uri_path =  proxy_fields[17] if  len(proxy_fields[17]) > 1 else ""
        proxy_uri_qry =  proxy_fields[18] if  len(proxy_fields[18]) > 1 else ""
        full_uri= "{0}{1}{2}".format(proxy_fields[15],proxy_uri_path,proxy_uri_qry)
        date = proxy_fields[0].split('-')
        year =  date[0]
        month = date[1].zfill(2)
        day = date[2].zfill(2)
        hour = proxy_fields[1].split(":")[0].zfill(2)
        # re-order fields. 
        proxy_parsed_data = [proxy_fields[0],proxy_fields[1],proxy_fields[3],proxy_fields[15],proxy_fields[12],proxy_fields[20],proxy_fields[13],int(proxy_fields[2]),proxy_fields[4],
        proxy_fields[5],proxy_fields[6],proxy_fields[7],proxy_fields[8],proxy_fields[9],proxy_fields[10],proxy_fields[11],proxy_fields[14],proxy_fields[16],proxy_fields[17],proxy_fields[18],
        proxy_fields[19],proxy_fields[21],int(proxy_fields[22]),int(proxy_fields[23]),proxy_fields[24],proxy_fields[25],proxy_fields[26],full_uri,year,month,day,hour ]

    return proxy_parsed_data


def save_data(rdd,sqc,db,db_table,topic):

    if not rdd.isEmpty():

        df = sqc.createDataFrame(rdd,proxy_schema)        
        sqc.setConf("hive.exec.dynamic.partition", "true")
        sqc.setConf("hive.exec.dynamic.partition.mode", "nonstrict")
        hive_table = "{0}.{1}".format(db,db_table)
        df.write.saveAsTable(hive_table,format="parquet",mode="append",partitionBy=('y','m','d','h'))

    else:
        print("------------------------LISTENING KAFKA TOPIC:{0}------------------------".format(topic))

def bluecoat_parse(zk,topic,db,db_table,num_of_workers,batch_size):
    
    app_name = topic
    wrks = int(num_of_workers)

    # create spark context
    sc = SparkContext(appName=app_name)
    ssc = StreamingContext(sc,int(batch_size))
    sqc = HiveContext(sc)

    tp_stream = KafkaUtils.createStream(ssc, zk, app_name, {topic: wrks}, keyDecoder=spot_decoder, valueDecoder=spot_decoder)

    proxy_data = tp_stream.map(lambda row: row[1]).flatMap(lambda row: row.split("\n")).filter(lambda row: rex_date.match(row)).map(lambda row: row.strip("\n").strip("\r").replace("\t", " ").replace("  ", " ")).map(lambda row:  split_log_entry(row)).map(lambda row: proxy_parser(row))
    saved_data = proxy_data.foreachRDD(lambda row: save_data(row,sqc,db,db_table,topic))
    ssc.start();
    ssc.awaitTermination()


if __name__ =='__main__':
    main()

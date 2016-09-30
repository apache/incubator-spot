
import argparse
import re
import shlex

from pyspark import SparkContext
from pyspark.streaming import StreamingContext
from pyspark.streaming.kafka import KafkaUtils
from pyspark.sql import HiveContext
from pyspark.sql.types import *

rex_date = re.compile("\d{4}-\d{2}-\d{2}")

def main():
    
    # input Parameters
    parser = argparse.ArgumentParser(description="Bro Parser")
    parser.add_argument('-zk','--zookeeper',dest='zk',required=True,help='Zookeeper IP and port (i.e. 10.0.0.1:2181)',metavar='')
    parser.add_argument('-t','--topic',dest='topic',required=True,help='Topic to listen for Spark Streaming',metavar='')
    parser.add_argument('-db','--database',dest='db',required=True,help='Hive database whete the data will be ingested',metavar='')
    parser.add_argument('-dt','--db-table',dest='db_table',required=True,help='Hive table whete the data will be ingested',metavar='')
    parser.add_argument('-w','--num_of_workers',dest='num_of_workers',required=True,help='Num of workers for Parallelism in Data Processing',metavar='')
    args = parser.parse_args()

    # start collector based on data source type.
    bro_parse(args.zk,args.topic,args.db,args.db_table,args.num_of_workers)


def oni_decoder(s):
    
    if s is None:
        return None
    return s

def split_log_entry(line):
    
    lex = shlex.shlex(line)
    lex.quotes = '"'
    lex.whitespace_split = True
    lex.commenters = ''
    return list(lex)

def proxy_parser(pl):
    
    proxy_parsed_data = []
    proxy_log_rows = pl.split("\n")

    for proxy_log in proxy_log_rows:

        if rex_date.match(proxy_log):
        
            # clean up the proxy log register.
            line = proxy_log.strip("\n").strip("\r") 
            line = line.replace("\t", " ").replace("  ", " ")
            proxy_fields = split_log_entry(line) 
	
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
            proxy_parsed_data.append((proxy_fields[0],proxy_fields[1],proxy_fields[3],proxy_fields[15],proxy_fields[12],proxy_fields[20],proxy_fields[13],int(proxy_fields[2]),proxy_fields[4],
            proxy_fields[5],proxy_fields[6],proxy_fields[7],proxy_fields[8],proxy_fields[9],proxy_fields[10],proxy_fields[11],proxy_fields[14],proxy_fields[16],proxy_fields[17],proxy_fields[18],
            proxy_fields[19],proxy_fields[21],int(proxy_fields[22]),int(proxy_fields[23]),proxy_fields[24],proxy_fields[25],proxy_fields[26],full_uri,year,month,day,hour))

    return proxy_parsed_data

def save_to_hive(rdd,sqc,db,db_table,topic):
    
    if not rdd.isEmpty(): 

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


        df = sqc.createDataFrame(rdd.collect()[0],proxy_schema)
        df.show()
        sqc.setConf("hive.exec.dynamic.partition", "true")
        sqc.setConf("hive.exec.dynamic.partition.mode", "nonstrict")

        hive_table = "{0}.{1}".format(db,db_table)         
        df.write.saveAsTable(hive_table,format="parquet",mode="append",partitionBy=('y','m','d','h'))       

    else:        
        print("------------------------LISTENING KAFKA TOPIC:{0}------------------------".format(topic))

def bro_parse(zk,topic,db,db_table,num_of_workers):
    
    app_name = "ONI-INGEST-{0}".format(topic)
    wrks = int(num_of_workers)

 	# create spark context
    sc = SparkContext(appName=app_name)
    ssc = StreamingContext(sc,1)
    sqc = HiveContext(sc)

    # create DStream for each topic partition.
    topic_dstreams = [ KafkaUtils.createStream(ssc, zk, app_name, {topic: 1}, keyDecoder=oni_decoder, valueDecoder=oni_decoder) for _ in range (wrks)  ] 
    tp_stream = ssc.union(*topic_dstreams)

    # Parallelism in Data Processing
    #processingDStream = tp_stream(wrks)

    # parse the RDD content.
    proxy_logs = tp_stream.map(lambda x: proxy_parser(x[1]))

    # save RDD into hive .
    proxy_logs.foreachRDD(lambda x: save_to_hive(x,sqc,db,db_table,topic))

    ssc.start()
    ssc.awaitTermination()

if __name__ =='__main__':
    main()
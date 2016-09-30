SET hiveconf:huser;
SET hiveconf:dbname;

CREATE EXTERNAL TABLE IF NOT EXISTS ${hiveconf:dbname}.dns (
 frame_time STRING,
 unix_tstamp BIGINT,
 frame_len INT,
 ip_dst STRING,
 ip_src STRING,
 dns_qry_name STRING,
 dns_qry_class STRING,
 dns_qry_type INT,
 dns_qry_rcode INT,
 dns_a STRING
)
PARTITIONED BY (y INT, m INT, d INT, h int)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS PARQUET
LOCATION '${hiveconf:huser}/dns/hive'
TBLPROPERTIES ('avro.schema.literal'='{
    "type":   "record"
  , "name":   "DnsRecord"
  , "namespace" : "com.cloudera.accelerators.dns.avro"
  , "fields": [
        {"name": "frame_time",                  "type":["string",   "null"]}
     ,  {"name": "unix_tstamp",                    "type":["bigint",   "null"]}
     ,  {"name": "frame_len",                    "type":["int",   "null"]}
     ,  {"name": "ip_dst",                    "type":["string",   "null"]}
     ,  {"name": "ip_src",                    "type":["string",   "null"]}
     ,  {"name": "dns_qry_name",              "type":["string",   "null"]}
     ,  {"name": "dns_qry_class",             "type":["string",   "null"]}
     ,  {"name": "dns_qry_type",              "type":["int",   "null"]}
     ,  {"name": "dns_qry_rcode",             "type":["int",   "null"]}
     ,  {"name": "dns_a",                 "type":["string",   "null"]}
  ]
}');


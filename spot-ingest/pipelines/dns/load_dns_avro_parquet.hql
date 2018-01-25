-- Licensed to the Apache Software Foundation (ASF) under one or more
-- contributor license agreements.  See the NOTICE file distributed with
-- this work for additional information regarding copyright ownership.
-- The ASF licenses this file to You under the Apache License, Version 2.0
-- (the "License"); you may not use this file except in compliance with
-- the License.  You may obtain a copy of the License at

--    http://www.apache.org/licenses/LICENSE-2.0

-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.

SET hiveconf:data_location;
SET hiveconf:y;
SET hiveconf:m;
SET hiveconf:d;
SET hiveconf:h;
SET hiveconf:dbname;


DROP TABLE IF EXISTS ${hiveconf:dbname}.dns_tmp
;


CREATE EXTERNAL TABLE ${hiveconf:dbname}.dns_tmp (
frame_day STRING,
frame_time STRING,
unix_tstamp BIGINT,
frame_len INT,
ip_src STRING,
ip_dst STRING,
dns_qry_name STRING,
dns_qry_type INT,
dns_qry_class STRING,
dns_qry_rcode INT,
dns_a STRING  
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION '${hiveconf:data_location}'
TBLPROPERTIES ('avro.schema.literal'='{
    "type":   "record"
  , "name":   "RawDnsRecord"
  , "namespace" : "com.cloudera.accelerators.dns.avro"
  , "fields": [
        {"name": "frame_day",   "type":["string", "null"]}
     ,  {"name": "frame_time",  "type":["string", "null"]}
     ,  {"name": "unix_tstamp", "type":["bigint", "null"]}
     ,  {"name": "frame_len",        "type":["int", "null"]}
     ,  {"name": "ip_src",         "type":["string", "null"]}
     ,  {"name": "ip_dst",         "type":["string", "null"]}
     ,  {"name": "dns_qry_name",   "type":["string", "null"]}
     ,  {"name": "dns_qry_type",   "type":["int", "null"]}
     ,  {"name": "dns_qry_class",  "type":["string", "null"]}
     ,  {"name": "dns_qry_rcode",  "type":["int", "null"]}
     ,  {"name": "dns_a",       "type":["string", "null"]}
  ]
}')
;


INSERT INTO TABLE ${hiveconf:dbname}.dns
PARTITION (y=${hiveconf:y}, m=${hiveconf:m}, d=${hiveconf:d}, h=${hiveconf:h})
SELECT   CONCAT(frame_day , frame_time) as treceived, unix_tstamp, frame_len, ip_dst, ip_src, dns_qry_name, dns_qry_class,dns_qry_type, dns_qry_rcode, dns_a 
FROM ${hiveconf:dbname}.dns_tmp
;

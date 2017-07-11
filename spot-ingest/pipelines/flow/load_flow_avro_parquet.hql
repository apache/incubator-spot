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


DROP TABLE IF EXISTS ${hiveconf:dbname}.flow_tmp
;


CREATE EXTERNAL TABLE ${hiveconf:dbname}.flow_tmp (
  treceived STRING,
  tryear INT,
  trmonth INT,
  trday INT,
  trhour INT,
  trminute INT,
  trsec INT,
  tdur FLOAT,
  sip  STRING,
 dip STRING,
 sport INT,
  dport INT,
  proto STRING,
  flag STRING,
  fwd INT,
  stos INT,
  ipkt BIGINT,
  ibyt BIGINT,
  opkt BIGINT,
  obyt BIGINT,
  input INT,
  output INT,
  sas INT,
  das INT,
  dtos INT,
  dir INT,
  rip STRING
 )
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS TEXTFILE
LOCATION '${hiveconf:data_location}'
TBLPROPERTIES ('avro.schema.literal'='{
    "type":   "record"
  , "name":   "RawFlowRecord"
  , "namespace" : "com.cloudera.accelerators.flows.avro"
  , "fields": [
        {"name": "treceived",                  "type":["string",   "null"]}
     ,  {"name": "tryear",                    "type":["float",   "null"]}
     ,  {"name": "trmonth",                    "type":["float",   "null"]}
     ,  {"name": "trday",                    "type":["float",   "null"]}
     ,  {"name": "trhour",                    "type":["float",   "null"]}
     ,  {"name": "trminute",                    "type":["float",   "null"]}
     ,  {"name": "trsec",                    "type":["float",   "null"]}
     ,  {"name": "tdur",                    "type":["float",   "null"]}
     ,  {"name": "sip",              "type":["string",   "null"]}
     ,  {"name": "sport",                 "type":["int",   "null"]}
     ,  {"name": "dip",         "type":["string",   "null"]}
     ,  {"name": "dport",        "type":["int",   "null"]}
     ,  {"name": "proto",            "type":["string",   "null"]}
     ,  {"name": "flag",            "type":["string",   "null"]}
     ,  {"name": "fwd",                 "type":["int",   "null"]}
     ,  {"name": "stos",                 "type":["int",   "null"]}
     ,  {"name": "ipkt",                 "type":["bigint",   "null"]}
     ,  {"name": "ibytt",                 "type":["bigint",   "null"]}
     ,  {"name": "opkt",                 "type":["bigint",   "null"]}
     ,  {"name": "obyt",                 "type":["bigint",   "null"]}
     ,  {"name": "input",                 "type":["int",   "null"]}
     ,  {"name": "output",                 "type":["int",   "null"]}
     ,  {"name": "sas",                 "type":["int",   "null"]}
     ,  {"name": "das",                 "type":["int",   "null"]}
     ,  {"name": "dtos",                 "type":["int",   "null"]}
     ,  {"name": "dir",                 "type":["int",   "null"]}
     ,  {"name": "rip",                    "type":["string",   "null"]}
  ]
}')
;

INSERT INTO TABLE ${hiveconf:dbname}.flow
PARTITION (y=${hiveconf:y}, m=${hiveconf:m}, d=${hiveconf:d}, h=${hiveconf:h})
SELECT   treceived,  unix_timestamp(treceived) AS unix_tstamp, tryear,  trmonth, trday,  trhour,  trminute,  trsec,
  tdur,  sip, dip, sport, dport,  proto,  flag,  fwd,  stos,  ipkt,  ibyt,  opkt,  obyt,  input,  output,
  sas,  das,  dtos,  dir,  rip
 FROM ${hiveconf:dbname}.flow_tmp
;


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


CREATE DATABASE IF NOT EXISTS ${var:dbname};


DROP TABLE IF EXISTS ${var:dbname}.proxy_edge_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.proxy_edge_tmp ( 
tdate STRING,
time STRING, 
clientip STRING, 
host STRING, 
webcat STRING, 
respcode STRING, 
reqmethod STRING,
useragent STRING,
resconttype STRING,
referer STRING,
uriport STRING,
serverip STRING, 
scbytes INT, 
csbytes INT, 
fulluri STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '${var:hpath}/proxy/edge'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.proxy_ingest_summary_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.proxy_ingest_summary_tmp ( 
tdate STRING,
total BIGINT 
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/proxy/summary'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.proxy_scores_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.proxy_scores_tmp ( 
tdate STRING,
time STRING, 
clientip STRING, 
host STRING, 
reqmethod STRING,
useragent STRING,
resconttype STRING,
duration INT,
username STRING, 
webcat STRING, 
referer STRING,
respcode INT,
uriport INT, 
uripath STRING,
uriquery STRING, 
serverip STRING, 
scbytes INT, 
csbytes INT, 
fulluri STRING,
word STRING, 
ml_score FLOAT,
uri_rep STRING,
uri_sev INT,
respcode_name STRING,
network_context STRING,
score_hash STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '${var:hpath}/proxy/scores'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.proxy_storyboard_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.proxy_storyboard_tmp ( 
p_threat STRING, 
title STRING,
text STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '${var:hpath}/proxy/storyboard'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.proxy_timeline_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.proxy_timeline_tmp ( 
tstart STRING, 
tend STRING, 
duration BIGINT, 
clientip STRING, 
respcode STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '${var:hpath}/proxy/timeline'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.proxy_iana_rcode_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.proxy_iana_rcode_tmp ( 
respcode STRING, 
respcode_name STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/proxy/iana_rcode'
TBLPROPERTIES ('skip.header.line.count'='1');




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


DROP TABLE IF EXISTS ${var:dbname}.flow_scores_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.flow_scores_tmp (
sev int, 
tstart STRING, 
srcip STRING,
dstip STRING,
sport INT, 
dport INT, 
proto STRING,
ipkt INT,
ibyt INT,
opkt INT,
obyt INT,
score FLOAT,
rank INT,
srcIpInternal INT,
destIpInternal INT,
srcGeo STRING, 
dstGeo STRING, 
srcDomain STRING, 
dstDomain STRING, 
srcIP_rep STRING,
dstIP_rep STRING 
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/flow/scores/'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.flow_chords_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.flow_chords_tmp (
srcip STRING,
dstip STRING,
ibyt BIGINT, 
ipkt BIGINT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '${var:hpath}/flow/chords'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.flow_edge_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.flow_edge_tmp (
tstart STRING, 
srcip STRING,
dstip STRING,
sport INT, 
dport INT, 
proto STRING,
flags STRING,
tos INT, 
ibyt BIGINT, 
ipkt BIGINT, 
input BIGINT,
output BIGINT, 
rip STRING,
obyt BIGINT, 
opkt BIGINT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '${var:hpath}/flow/edge'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.flow_ingest_summary_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.flow_ingest_summary_tmp (
tdate STRING,
total BIGINT 
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/flow/summary'
TBLPROPERTIES ('skip.header.line.count'='1');

DROP TABLE IF EXISTS ${var:dbname}.flow_storyboard_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.flow_storyboard_tmp (
ip_threat STRING,
title STRING,
text STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '${var:hpath}/flow/storyboard'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.flow_timeline_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.flow_timeline_tmp (
tstart STRING, 
tend STRING, 
srcip STRING,
dstip STRING,
proto STRING,
sport INT,
dport INT, 
ipkt BIGINT, 
ibyt BIGINT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
LOCATION '${var:hpath}/flow/timeline'
TBLPROPERTIES ('skip.header.line.count'='1');



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


CREATE EXTERNAL TABLE IF NOT EXISTS ${var:dbname}.flow (
treceived STRING,
unix_tstamp BIGINT,
tryear INT,
trmonth INT,
trday INT,
trhour INT,
trminute INT,
trsec INT,
tdur FLOAT,
sip STRING,
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
PARTITIONED BY (
y SMALLINT,
m TINYINT,
d TINYINT,
h TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive';


CREATE EXTERNAL TABLE ${var:dbname}.flow_chords (
ip_threat STRING,
srcip STRING,
dstip STRING,
ibyt BIGINT, 
ipkt BIGINT
)
PARTITIONED BY (
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive/oa/chords';


CREATE EXTERNAL TABLE ${var:dbname}.flow_edge (
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
opkt BIGINT, 
hh INT,
mn INT 
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive/oa/edge';


CREATE EXTERNAL TABLE ${var:dbname}.flow_ingest_summary (
tdate STRING,
total BIGINT 
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive/oa/summary';


CREATE EXTERNAL TABLE ${var:dbname}.flow_scores (
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
ml_score FLOAT,
rank INT,
srcip_INTernal INT,
dstip_INTernal INT,
src_geoloc STRING, 
dst_geoloc STRING, 
src_domain STRING, 
dst_domain STRING, 
src_rep STRING,
dst_rep STRING 
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive/oa/suspicious';


CREATE EXTERNAL TABLE ${var:dbname}.flow_storyboard (
ip_threat STRING,
title STRING,
text STRING
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive/oa/storyboard';


CREATE EXTERNAL TABLE ${var:dbname}.flow_threat_investigation ( 
tstart STRING,
srcip STRING, 
dstip STRING, 
srcport INT,
dstport INT,
score INT 
) 
PARTITIONED BY (
y SMALLINT,
m TINYINT,
d TINYINT
) 
STORED AS PARQUET 
LOCATION '${var:huser}/flow/hive/oa/threat_investigation';


CREATE EXTERNAL TABLE ${var:dbname}.flow_timeline (
ip_threat STRING,
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
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/flow/hive/oa/timeline';

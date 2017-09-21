
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


CREATE EXTERNAL TABLE IF NOT EXISTS ${var:dbname}.proxy (
p_date STRING,
p_time STRING,
clientip STRING,
host STRING,
reqmethod STRING,
useragent STRING,
resconttype STRING,
duration INT,
username STRING,
authgroup STRING,
exceptionid STRING,
filterresult STRING,
webcat STRING,
referer STRING,
respcode STRING,
action STRING,
urischeme STRING,
uriport STRING,
uripath STRING,
uriquery STRING,
uriextension STRING,
serverip STRING,
scbytes INT,
csbytes INT,
virusid STRING,
bcappname STRING,
bcappoper STRING,
fulluri STRING
)
PARTITIONED BY (
y STRING,
m STRING,
d STRING,
h STRING
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive';


CREATE EXTERNAL TABLE ${var:dbname}.proxy_edge ( 
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
fulluri STRING,
hh INT,
respcode_name STRING 
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive/oa/edge';


CREATE EXTERNAL TABLE ${var:dbname}.proxy_ingest_summary ( 
tdate STRING,
total BIGINT 
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive/oa/summary';


CREATE EXTERNAL TABLE ${var:dbname}.proxy_scores ( 
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
respcode_name STRING,
network_context STRING 
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive/oa/suspicious';


CREATE EXTERNAL TABLE ${var:dbname}.proxy_storyboard ( 
p_threat STRING, 
title STRING,
text STRING
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive/oa/storyboard';


CREATE EXTERNAL TABLE ${var:dbname}.proxy_threat_investigation ( 
tdate STRING,
fulluri STRING,
uri_sev INT
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive/oa/threat_investigation';


CREATE EXTERNAL TABLE ${var:dbname}.proxy_timeline ( 
p_threat STRING, 
tstart STRING, 
tend STRING, 
duration BIGINT, 
clientip STRING, 
respcode STRING, 
respcodename STRING
)
PARTITIONED BY ( 
y SMALLINT,
m TINYINT,
d TINYINT
)
STORED AS PARQUET
LOCATION '${var:huser}/proxy/hive/oa/timeline';

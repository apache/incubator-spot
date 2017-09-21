
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


DROP TABLE IF EXISTS ${var:dbname}.dns_dendro_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.dns_dendro_tmp (
dns_a STRING,
dns_qry_name STRING,
ip_dst STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/dns/dendro'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.dns_edge_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.dns_edge_tmp ( 
frame_time STRING,
frame_len STRING,
ip_dst STRING,
ip_src STRING,
dns_qry_name STRING,
dns_qry_class STRING,
dns_qry_type STRING,
dns_qry_rcode STRING,
dns_a STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/dns/edge'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.dns_ingest_summary_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.dns_ingest_summary_tmp ( 
tdate STRING,
total BIGINT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/dns/summary'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.dns_scores_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.dns_scores_tmp ( 
frame_time STRING,
frame_len BIGINT,
ip_dst STRING, 
dns_qry_name STRING, 
dns_qry_class STRING,
dns_qry_type INT,
dns_qry_rcode INT, 
ml_score FLOAT,
tld STRING,
query_rep STRING,
hh INT,
ip_sev INT,
dns_sev INT,
dns_qry_class_name STRING, 
dns_qry_type_name STRING,
dns_qry_rcode_name STRING, 
network_context STRING,
unix_tstamp BIGINT
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/dns/scores'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.dns_storyboard_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.dns_storyboard_tmp ( 
ip_threat STRING,
dns_threat STRING, 
title STRING,
text STRING
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '|'
LOCATION '${var:hpath}/dns/storyboard'
TBLPROPERTIES ('skip.header.line.count'='1');


DROP TABLE IF EXISTS ${var:dbname}.dns_threat_dendro_tmp;

CREATE EXTERNAL TABLE ${var:dbname}.dns_threat_dendro_tmp (
total BIGINT,
dns_qry_name STRING, 
ip_dst STRING,
sev int
)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
LOCATION '${var:hpath}/dns/threat_dendro'
TBLPROPERTIES ('skip.header.line.count'='1');


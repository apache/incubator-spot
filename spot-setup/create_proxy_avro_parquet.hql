SET hiveconf:huser;
SET hiveconf:dbname;

CREATE EXTERNAL TABLE IF NOT EXISTS ${hiveconf:dbname}.proxy (
p_date                string,
p_time                string,
clientip              string,
host                  string,
reqmethod             string,
useragent             string,
resconttype           string,
duration              int,
username              string,
authgroup             string,
exceptionid           string,
filterresult          string,
webcat                string,
referer               string,
respcode              string,
action                string,
urischeme             string,
uriport               string,
uripath               string,
uriquery              string,
uriextension          string,
serverip              string,
scbytes               int,
csbytes               int,
virusid               string,
bcappname             string,
bcappoper             string,
fulluri               string
)
PARTITIONED BY (
y string, 
m string, 
d string, 
h string)
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
STORED AS PARQUET
LOCATION '${hiveconf:huser}/proxy/hive'
TBLPROPERTIES ('avro.schema.literal'='{
    "type":   "record"
  , "name":   "ProxyRecord"
  , "namespace" : "com.cloudera.accelerators.proxy.avro"
  , "fields": [
     {"name": "p_date", "type":["string",  "null"]}
    , {"name": "p_time", "type":["string",  "null"]}
    , {"name": "clientip", "type":["string",  "null"]}
    , {"name": "host", "type":["string",  "null"]}
    , {"name": "reqmethod", "type":["string",  "null"]}
    , {"name": "useragent", "type":["string",  "null"]}
    , {"name": "resconttype", "type":["string",  "null"]}
    , {"name": "duration", "type":["int",  "null"]}
    , {"name": "username",  "type":["string",  "null"]}
    , {"name": "authgroup", "type":["string",  "null"]}
    , {"name": "exceptionid", "type":["string",  "null"]}
    , {"name": "filterresult", "type":["string",  "null"]}
    , {"name": "webcat", "type":["string",  "null"]}
    , {"name": "referer", "type":["string",  "null"]}
    , {"name": "respcode", "type":["string",  "null"]}
    , {"name": "action", "type":["string",  "null"]}
    , {"name": "urischeme", "type":["string",  "null"]}
    , {"name": "uriport", "type":["string",  "null"]}
    , {"name": "uripath", "type":["string",  "null"]}
    , {"name": "uriquery", "type":["string",  "null"]}
    , {"name": "uriextension", "type":["string",  "null"]}
    , {"name": "serverip", "type":["string",  "null"]}
    , {"name": "scbytes", "type":["int",  "null"]}
    , {"name": "csbytes", "type":["int",  "null"]}
    , {"name": "virusid", "type":["string",  "null"]}
    , {"name": "bcappname", "type":["string",  "null"]}
    , {"name": "bcappoper", "type":["string",  "null"]}
    , {"name": "fulluri", "type":["string",  "null"]}
  ]
}');

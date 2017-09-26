
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

DROP TABLE IF EXISTS ${VAR:ODM_DBNAME}.${VAR:ODM_TABLENAME};
CREATE EXTERNAL TABLE IF NOT EXISTS ${VAR:ODM_DBNAME}.${VAR:ODM_TABLENAME} (
dvc_time bigint,
user_created bigint,
user_changed bigint,
user_last_logon bigint,
user_logon_count int,
user_last_reset bigint,
user_expiration bigint,
user_img binary,
user_id string,
user_name string,
user_name_first string,
user_name_middle string,
user_name_last string,
user_name_mgr string,
user_phone string,
user_email string,
user_code string,
user_loc string,
user_departm string,
user_dn string,
user_ou string,
user_empid string,
user_title string,
user_groups array<string>,
dvc_type string,
dvc_vendor string,
user_risk float,
dvc_version string,
additional_attrs map<string,string>)
STORED AS PARQUET
LOCATION '${VAR:ODM_LOCATION}'
;
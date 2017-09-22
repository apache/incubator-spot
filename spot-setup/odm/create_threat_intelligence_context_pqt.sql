
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
ti_source string,
ti_provider_id string,
ti_indicator_id string,
ti_indicator_desc string,
ti_date_added bigint,
ti_date_modified bigint,
ti_risk_impact string,
ti_severity string,
ti_category string,
ti_campaign_name string,
ti_deployed_location array<string>,
ti_associated_incidents string,
ti_adversarial_identification_group string,
ti_adversarial_identification_tactics string,
ti_adversarial_identification_reports string,
ti_phase string,
ti_indicator_cve string,
ti_indicator_ip4 array<bigint>,
ti_indicator_ip4_str array<string>,
ti_indicator_ip6 array<bigint>,
ti_indicator_ip6_str array<string>,
ti_indicator_domain string,
ti_indicator_hostname string,
ti_indicator_email array<string>,
ti_indicator_url array<string>,
ti_indicator_uri array<string>,
ti_indicator_file_hash string,
ti_indicator_file_path string,
ti_indicator_mutex string,
ti_indicator_md5 string,
ti_indicator_sha1 string,
ti_indicator_sha256 string,
ti_indicator_device_path string,
ti_indicator_drive string,
ti_indicator_file_name string,
ti_indicator_file_extension string,
ti_indicator_file_size string,
ti_indicator_file_created bigint,
ti_indicator_file_accessed bigint,
ti_indicator_file_changed bigint,
ti_indicator_file_entropy string,
ti_indicator_file_attributes array<string>,
ti_indicator_user_name string,
ti_indicator_security_id string,
ti_indicator_pe_info array<string>,
ti_indicator_pe_type array<string>,
ti_indicator_strings array<string>)
STORED AS PARQUET
LOCATION '${VAR:ODM_LOCATION}'
;
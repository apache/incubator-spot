
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
net_domain_name string,
net_registry_domain_id string,
net_registrar_whois_server string,
net_registrar_url string,
net_update_date bigint,
net_creation_date bigint,
net_registrar_registration_expiration_date bigint,
net_registrar string,
net_registrar_iana_id string,
net_registrar_abuse_contact_email string,
net_registrar_abuse_contact_phone string,
net_domain_status string,
net_registry_registrant_id string,
net_registrant_name string,
net_registrant_organization string,
net_registrant_street string,
net_registrant_city string,
net_registrant_state string,
net_registrant_post_code string,
net_registrant_country string,
net_registrant_phone string,
net_registrant_email string,
net_registry_admin_id string,
net_name_servers string,
net_dnssec string,
net_risk float)
STORED AS PARQUET
LOCATION '${VAR:ODM_LOCATION}'
;
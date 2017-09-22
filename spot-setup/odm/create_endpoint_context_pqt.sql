
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
end_ip4 bigint,
end_ip4_str string,
end_ip6 bigint,
end_ip6_str string,
end_os string,
end_os_version string,
end_os_sp string,
end_tz string,
end_hotfixes array<string>,
end_disks array<string>,
end_removeables array<string>,
end_nics array<string>,
end_drivers array<string>,
end_users array<string>,
end_host string,
end_mac string,
end_owner string,
end_vulns array<string>,
end_loc string,
end_departm string,
end_company string,
end_regs array<string>,
end_svcs array<string>,
end_procs array<string>,
end_criticality string,
end_apps array<string>,
end_desc string,
dvc_type string,
dvc_vendor string,
dvc_version string,
end_architecture string,
end_uuid string,
end_risk float,
end_memtotal int,
additional_attrs map<string,string>)
STORED AS PARQUET
LOCATION '${VAR:ODM_LOCATION}'
;
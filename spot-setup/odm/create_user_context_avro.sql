DROP TABLE IF EXISTS ${VAR:ODM_DBNAME}.${VAR:ODM_TABLENAME};
CREATE EXTERNAL TABLE IF NOT EXISTS ${VAR:ODM_DBNAME}.${VAR:ODM_TABLENAME} (
dvc_time bigint,
user_created bigint,
user_changed bigint,
user_last_logon bigint,
user_logon_count int,
user_last_reset bigint,
user_expiration bigint,
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
user_img string,
additional_attrs map<string,string>)
STORED AS AVRO
LOCATION '${VAR:ODM_LOCATION}'
TBLPROPERTIES ('avro.schema.url'='${VAR:ODM_AVRO_URL}')
;
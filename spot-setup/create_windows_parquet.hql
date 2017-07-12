SET hiveconf:huser;
SET hiveconf:dbname;

CREATE EXTERNAL TABLE IF NOT EXISTS ${hiveconf:dbname}.windows_view_partition (
	event_time string,
	event_id string,
	event_description string,

	subject_domainname string,
	subject_logonid string,
	subject_username string,
	subject_usersid string,

	target_domainname string,
	target_logonid string,
	target_sid string,
	target_username string,
	newtarget_username string,

	member_name string,
	member_sid string,

	object_name string,
	object_server string,
	object_type string,
	handle_id string,
	logon_type string,

	process_id string,
	process_name string,
	ip_address string,
	ip_port string,

	privilege_list string,
	workstation_name string,
	status string,
	accesses string
)
PARTITIONED BY (
	year string,
	month string,
	day string
)
STORED AS PARQUET
LOCATION '${hiveconf:huser}/sstech/windows_view_parquet_partition';

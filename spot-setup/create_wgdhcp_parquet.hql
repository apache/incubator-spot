SET hiveconf:huser;
SET hiveconf:dbname;

CREATE EXTERNAL TABLE IF NOT EXISTS ${hiveconf:dbname}.wgdhcp_view_partition (
	event_time string,
	sid string,
	sn string,
	tag_id string,
	raw_id string,
	event_id string,
	ip string,
	mac string,
	workstation string,
	iface string,
	msg string
)
PARTITIONED BY (
	year string,
	month string,
	day string
)
STORED AS PARQUET
LOCATION '${hiveconf:huser}/sstech/wgdhcp_view_parquet_partition';


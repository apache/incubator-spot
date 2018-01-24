SET hiveconf:huser;
SET hiveconf:dbname;

CREATE EXTERNAL TABLE IF NOT EXISTS ${hiveconf:dbname}.wgtraffic_view_partition (
	event_time string,
	sid string,
	cluster string,
	sn string,
	tag_id string,
	raw_id string,
	disp string,
	direction string,
	pri string,
	policy string,
	protocol string,
	src_ip string,
	src_port string,
	dst_ip string,
	dst_port string,
	src_ip_nat string,
	src_port_nat string,
	dst_ip_nat string,
	dst_port_nat string,
	src_intf string,
	dst_intf string,
	rc string,
	pckt_len string,
	ttl string,
	pr_info string,
	proxy_act string,
	alarm_name string,
	alarm_type string,
	alarm_id string,
	info_1 string,
	info_2 string,
	info_3 string,
	info_4 string,
	info_5 string,
	info_6 string,
	log_type string,
	msg string,
	bucket string
)
PARTITIONED BY (
	year string,
	month string,
	day string
)
STORED AS PARQUET
LOCATION '${hiveconf:huser}/wgtraffic_view_partition';


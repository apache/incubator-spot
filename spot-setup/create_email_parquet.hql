SET hiveconf:huser;
SET hiveconf:dbname;

CREATE EXTERNAL TABLE IF NOT EXISTS ${hiveconf:dbname}.email_view_partition (
	event_time string,
	event_name string,
	result_status string,
	source_type string,
	source_host string,
	destination_host string,
	source_user string,
	source_ip string,
	destination_ip string,
	attachment_size string,
	attachment_type string,
	sender string,
	receiver string,
	no_of_receivers string,
	subject string,
	no_of_attachments string,
	email_size string,
	userid string
)
PARTITIONED BY (
	year string,
	month string,
	day string
)
STORED AS PARQUET
LOCATION '${hiveconf:huser}/email_view_parquet_partition';


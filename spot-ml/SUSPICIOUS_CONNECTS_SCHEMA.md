# Data Schema for the Spot Suspicious Connects Analyses



Three kinds of network logs can be analyzed by the Spot Suspicious Connects analyses:  Netflow records, DNS queries, and proxy logs.

Data from these logs is expected to have been stored in parquet files on HDFS with column structure extending the following schema.


## Netflow Data

- trhour: The hour of the day in which the flow was logged, Integer.
- sip: The source IP address of the flow, String.
- dip: The destination IP address of the flow, String.
- sport: The source port of the flow, Integer
- dport: The destination port of the flow, Integer
- proto: The protocol used by the flow, String
- ipkt:  The flow's packet count, Long.
- ibyt:  The flow's byte count, Long.

## DNS Data

- frame_time:  Frame timestamp of query, String. 
- unix_tstamp:  Unix timestamp of query, String.
- frame_len: Frame length, Integer.
- ip_dst: IP address of client making query, String.
- dns_qry_name: Name of DNS query, String.
- dns_qry_class: Class of DNS query, String.
- dns_qry_type: Type of DNS query, Integer.
- dns_qry_rcode: DNS query response code, Integer.

## Proxy Data


- p_date: Date of the query, String.
- p_time: Time of the query, String.  (FORMAT?) 
- clientip: IP address of client making proxy request, String.                          
- host: Host of request, String.
- reqmethod: Request method, String.                                    
- useragent: User agent, String.                                      
- resconttype: Response content type, String.                                                                           
- respcode: Response code, String.                                                                      
- fulluri: Full URI of request, String.




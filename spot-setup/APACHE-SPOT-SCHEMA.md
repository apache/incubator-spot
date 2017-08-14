# Apache Spot Schema 

This document is to centralize a place where users can read information about Proxy, DNS and flow schema. From this document users with their own ingest can implement a module without using spot-ingest, or they can compare them. User can do that creating a data set with the expected columns by pipeline.

- [Proxy](#proxy)

    Attributes and rules.
    * [Proxy Schema for spot-ingest](#proxy-schema-for-spot-ingest) 
    * [Proxy Schema for spot-ml](#proxy-schema-for-spot-ml)
    * [Proxy Schema for spot-oa](#proxy-schema-for-spot-oa)
    * [Proxy Schema for spot-ui](#proxy-schema-for-spot-ui)

- [Flow (spot-nfdump)](#flow-spot-nfdump)

    Attributes and rules.
    * [Flow Schema for spot-ingest](#Flow-schema-for-spot-ingest) 
    * [Flow Schema for spot-ml](#flow-schema-for-spot-ml)
    * [Flow Schema for spot-oa](#flow-schema-for-spot-oa)
    * [Flow Schema for spot-ui](#flow-schema-for-spot-ui)

- [DNS](#dns)

    Attributes and rules.
    * [DNS Schema for spot-ingest](#dns-schema-for-spot-ingest) 
    * [DNS Schema for spot-ml](#dns-schema-for-spot-ml)
    * [DNS Schema for spot-oa](#dns-schema-for-spot-oa)
    * [DNS Schema for spot-ui](#dns-schema-for-spot-ui)

## Proxy
The table shows the list of attributes used in proxy. The columns indicated with field (:white_check_mark:) are used by the pipeline.  

|Spot Field Name  |Type    |Description                                  |Original Field Name  |Format      |Spot-ingest       |Spot-ml           |Spot-oa           |Spot-ui           | 
|---------------- |--------|---------------------------------------------|---------------------|------------|------------------|------------------|------------------|------------------|
| p_date          | string | Date for the connection                     |        date         | yyyy-mm-dd |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| p_time	      | string | Time for the connection	                 |        time	       |  hh:MM:SS  |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| clientip        | string |IP address of the client sending the request |        c-ip	       | ip address	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| host        	  | string |Hostname from the client's request URL	     |       cs-host	   |    text	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| reqmethod	      | string |Request method used from client to appliance (HTTP Method - GET, POST, CONNECT) |	cs-method | 	text |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| useragent	      | string |Browser Type	                             | cs(User-Agent)	   |quoted text	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| resconttype	  | string |Content-type (Ex. text/html, image/xml)	     |rs(Content-Type) 	   | text	    |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| duration	      |  int   |Duration of the connection	                 |time-taken	       |numerical	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| username	      |string  |Client Username	                             |cs-username	       |text	    |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| authgroup   	  |string  |Client Authentication Group	                 |cs-auth-group 	   |text	    |:white_check_mark:|	-	    |     -    |       -  |
| exceptionid	  |string  |Identifier of the exception resolved (empty if the transaction has not been terminated) |	x-exception-id 	| text	|:white_check_mark:|- | - |     -         |	
| filterresult    |string  |Content filtering result: Denied, Proxied or Observed | sc-filter-result | text |:white_check_mark:|     -        |        -        |      -   |			
| webcat	      |string  |All content categories of the request URL	 |cs-categories        |quoted text	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| referer	      |string  |Request header: Referer %S s-sitename The service type used to | cs(Referer) | url |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| respcode	      |string  |Protocol status code from appliance to client (HTTP Response Codes) | sc-status | numerical |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| action	      |string  |What type of action did the Appliance take to process this request; possible values include ALLOWED, DENIED, FAILED, SERVER_ERROR|s-action |text |:white_check_mark:| -| -|- | 			
| urischeme	      |string  |Scheme of the original URL requested	     |cs-uri-scheme 	   |text	    |:white_check_mark:|      -       |     -           |      -          |		
| uriport	      |string  |Port from the original URL requested	     |cs-uri-port 	       |numerical	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| uripath	      |string  |Path of the original URL requested without query |cs-uri-path 	   |text	    |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| uriquery	      |string  |Query from the original URL requested	     |cs-uri-query	       |text	    |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| uriextension	  |string  |Document extension from the original URL requested |cs-uri-extension |text	    |:white_check_mark:|      -       |     -           |      -          |		
| serverip	      |string  |IP address of the appliance on which the client established its connection |s-ip  |ip address |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| scbytes	      |int	   |Number of bytes sent from appliance to client|sc-bytes             |numerical	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| csbytes	      |int	   |Number of bytes sent from client to appliance|cs-bytes 	           |numerical	|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| virusid	      |string  |x-virus-id 	                                 |x-virus-id 	       |text	    |:white_check_mark:|    -         |    -            |       -         |		
| bcappname	      |string  |x-bluecoat-application-name 	             |x-bluecoat-application-name |quoted text |:white_check_mark:|  -        |  -              |    -            |			
| bcappoper	      |string  |x-bluecoat-application-operation	         |x-bluecoat-application-operation |quoted text |:white_check_mark:|-     |   -             |    -            |			
|fulluri	      |string  |Full URI concatenated from cs-host, cs-uri-path, cs-uri-query fields |it does not exist, it is calculated during ingest |text |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| word 	          |string  |      -                					     |           -          |   -       |     -            |  -               |:white_check_mark:|      -          |
| ml_score	      |float   |				-	                         |          -           |      -    |        -         |   -              |:white_check_mark:|  -              |
| respcode_name   |string  |IANA translation for the response code column|                -     |     -     |      -           |       -          |:white_check_mark:|:white_check_mark:|
| uri_rep	      |string  |Reputation value according to Threat intelligence services| 	-   |	-		|        -         |  -               |:white_check_mark:|:white_check_mark:|
| network_context |string  |User defined value					         |              -       |     -     |       -          |     -            |:white_check_mark:|:white_check_mark:| 


## Flow (spot-nfdump) 
The table shows the list of attributes used in flow. The columns indicated with field (:white_check_mark:) are used by the pipeline.  

|Spot Field Name  |Type    |Description                                  |Original NFDUMP Field Name           |Format                   |Spot-ingest       |Spot-ml           |Spot-oa           |Spot-ui           | 
|---------------- |--------|---------------------------------------------|-------------------------------------|-------------------------|------------------|------------------|------------------|------------------|
| treceived  	  | string | Time the flow was received by the collector | tr	                               |YYYY-mm-DD HH:MM:SS      |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| unix_tstamp	  | bigint | treceived epoch time	                     |it is calculated by ingest hql script|number (1471431305)      |:white_check_mark:| -                | -                | -                |                     
| tryear     	  | int    | treceived year 	                         |it is calculated by spot-nfdump	   |numerical                |:white_check_mark:|:white_check_mark:| -                | -                |
| trmonth    	  | int    | treceived month	                         |it is calculated by spot-nfdump	   |numerical                |:white_check_mark:|:white_check_mark:| -                | -                |
| trday      	  | int    | treceived day	                             |it is calculated by spot-nfdump	   |numerical                |:white_check_mark:|:white_check_mark:| -                | -                |
| trhour     	  | int    | treceived hour	                             |it is calculated by spot-nfdump	   |numerical                |:white_check_mark:|:white_check_mark:| -                | -                |
| trminute   	  | int    | treceived minute	                         |it is calculated by spot-nfdump	   |numerical                |:white_check_mark:|:white_check_mark:| -                | -                |
| trsec      	  | int    | treceived seconds	                         |it is calculated by spot-nfdump	   |numerical                |:white_check_mark:|:white_check_mark:| -                | -                |
| tdur       	  | float  | Duration	                                 | td	                               |xx.xx (18.04400062561035)|:white_check_mark:|:white_check_mark:| -                | -                |
| sip        	  | string | Source IP Address          	             | sa	                               |ip address dotted decimal|:white_check_mark:|:white_check_mark:| -                | -                |
| dip        	  | string | Destination IP Address	                     | da	                               |ip address dotted decimal|:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| sport      	  | int    | Source Port	                             | sap	                               |numerical                |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| dport      	  | int    | Destination Port	                         | dap	                               |numerical                |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| proto      	  | string | Protocol	                                 | pr	                               |text (UDP, TCP, etc)     |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| flag       	  | string | TCP Flags	                                 | flg	                               |dotted flag representation (.A....)|:white_check_mark:| -      |:white_check_mark:|:white_check_mark:|   
| fwd        	  | int    | Forwarding Status	                         | fwd	                               |numerical                |:white_check_mark:| -                | -                | -                |
| stos       	  | int    | Source Tos (DSCP)	                         | stos	                               |numerical                |:white_check_mark:| -                |:white_check_mark:|:white_check_mark:|
| ipkt       	  | bigint | Input Packets	                             | ipkt	                               |numerical                |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| ibyt       	  | bigint | Input Bytes	                             | ibyt	                               |numerical                |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| opkt       	  | bigint | Output Packets	                             | opkt	                               |numerical                |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| obyt       	  | bigint | Output Bytes	                             | obyt	                               |numerical                |:white_check_mark:|:white_check_mark:|:white_check_mark:|:white_check_mark:|
| input      	  | int    | Input interface SNMP number	             | in	                               |numerical                |:white_check_mark:| -                |:white_check_mark:|:white_check_mark:| 
| output     	  | int    | Output interface SNMP number	             | out	                               |numerical                |:white_check_mark:| -                |:white_check_mark:|:white_check_mark:|
| sas        	  | int    | Source AS number	                         | sas	                               |numerical                |:white_check_mark:| -                | -                | -                |
| das        	  | int    | Destination AS number	                     | das	                               |numerical                |:white_check_mark:| -                | -                | -                |
| dtos       	  | int    | Destination Tos (DSCP)	                     | dtos	                               |numerical                |:white_check_mark:| -                | -                | -                |  
| dir        	  | int    | direction	                                 | dir	                               |numerical (0,1)          |:white_check_mark:| -                | -                | -                | 
| rip        	  | string | Router IP	                                 | ra	                               |ip address dotted decimal|:white_check_mark:| -                |:white_check_mark:|:white_check_mark:|
| ML_score	      |float   | Score assigned by ML - Produced by ML		 |                                     |numerical			     |                  |                  |:white_check_mark:|                  |	
| rank	          | int    | Rank number based on the order of ML_score values - Produced by OA |		       |numerical				 |                  | -                | -                |:white_check_mark:|
| srcip_internal  |int     | Boolean value to identify an internal source IP - Produced by OA |                |                         | -                | -                | -                |:white_check_mark:|
| dstip_internal  |int     | Boolean value to identify an internal destination IP - Produced by OA |		   |                         | -                | -                | -			      |:white_check_mark:|
| src_geoloc	  |string  | Lat & Long values of the source IP - Produced by OA |						       |                         | -                | -                | -                |:white_check_mark:|
| dst_geoloc	  |string  |Lat & Long values of the destination IP - Produced by OA |						   |                         | -                | -                | -                |:white_check_mark:|
| src_domain	  |string  |Domain assigned to the source IP - Produced by OA |                                |						 | -                | -                | -                |:white_check_mark:|
| dst_domain	  |string  |Domain assigned to the destination IP - Produced by OA |						   |                         | -                | -                | -                |:white_check_mark:|
| src_rep	      |string  |Collection of reputation values assigned to the source IP from different TI services - Produced by OA | |    | -                | -				   | -                |:white_check_mark:|
| dst_rep	      |string  |Collection of reputation values  assigned to the destination IP from different TI services - Produced by OA||| -                | -                | -         		  |:white_check_mark:|


## DNS 
The table shows the list of attributes used in DNS. The columns indicated with field (:white_check_mark:) are used by the pipeline.  

|Spot Field Name  |Type    |Description                                  |Original NFDUMP Field Name           |Format                   |Spot-ingest       |Spot-ml           |Spot-oa           |Spot-ui           | 
|---------------- |--------|---------------------------------------------|-------------------------------------|-------------------------|------------------|------------------|------------------|------------------|
| frame_time   	  |string  |Tshark Frame Time received	                 |frame.time	          |Ex. Jan  4 2017 04:41:06.337519000 UTC|:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |		
| unix_tstamp  	  |bigint  |Tshark Frame Time received epoch format      |frame.time_epoch	                   |numerical (1483504866)	 |:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |	
| frame_len    	  |int     |Tshark Frame Length	                         |frame.len	                           |numerical	             |:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |		
| ip_dst       	  |string  |Tshark IP destination (Client IP)	         |ip.dst	                           |ip address dotted decimal|:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |		
| ip_src       	  |string  |Tshark IP source (DNS Server IP)	         |ip.src	                           |ip address dotted decimal|-                 | -                |:white_check_mark:| -                |  				
| dns_qry_name 	  |string  |Tshark DNS Query Name	                     |dns.qry.name	                       |text	                 |:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |		
| dns_qry_class	  |string  |Tshark DNS Query Class	                     |dns.qry.class	                       |hexadecimal (0x00000001) |:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |		
| dns_qry_type 	  |int     |Tshark DNS Query Type	                     |dns.qry.type	                       |numerical	             |:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |		
| dns_qry_rcode	  |int     |Tshark DNS Query Response Code	             |dns.flags.rcode	                   |numerical	             |:white_check_mark:|:white_check_mark:|:white_check_mark:| -                |	
| dns_a        	  |string  |Tshark DNS Query A Record	                 |dns.a	text				           |                         | -                | -                | -                |:white_check_mark:|
|ML_score	      |float   |Produced by ML                               |                                     |                         | -                | -                |:white_check_mark:|:white_check_mark:| 
|tld	          |string  |Top level domain obtained from query name column - Produced by OA |                |                         | -                | -                | -                |:white_check_mark:|
|query_rep	      |string  |Collection of reputation values assigned to the destination IP from different TI services - Produced by OA|| | -                | -                | -                |:white_check_mark:|
|hh	              |int     |Obtained from frame time column - Produced by OA |                                 |                         | -                | -                | -                |:white_check_mark:|
|dns_qry_class_name|string |Translation for the query class code - Produced by OA |                            |                         | -                | -                | -                |:white_check_mark:|
|dns_qry_type_name|string  |Translation for the query type code - Produced by OA |                             |                         | -                | -                | -                |:white_check_mark:|
|dns_qry_rcode_name|string |Translation for the query response code - Produced by OA |                         |                         | -                | -                | -                |:white_check_mark:|
|network_context  |string  |Value to identify the destination IP as internal to the network - Produced by OA | |                         | -                | -                | -                |:white_check_mark:|


### Proxy Schema for spot-ingest
The table shows proxy schema attributes and the rules used specifically for ingest.

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| p_date          | -                   | -                      |
| p_time          | -                   | -                      |
| clientip        | -                   | -                      |
| host            | -                   | -                      |
| reqmethod       | -                   | -                      |
| useragent       | -                   | -                      |
| resconttype     | -                   | -                      |
| duration        | -                   | -                      |
| username        | -                   | -                      |
| authgroup       | -                   | -                      | 
| exceptionid     | -                   | -                      |
| filterresult    | -                   | -                      | 
| webcat          | -                   | -                      |
| referer         | -                   | -                      |
| respcode        | -                   | -                      |
| action          | -                   | -                      |
| urischeme       | -                   | -                      |
| uriport         | -                   | -                      |
| uripath         | -                   | -                      |    
| uriquery        | -                   | -                      |
| uriextension    | -                   | -                      |
| serverip        | -                   | -                      |
| scbytes         | -                   | -                      |
| csbytes         | -                   | -                      |
| virusid         | -                   | -                      | 
| bcappname       | -                   | -                      |
| bcappoper       | -                   | -                      |
| fulluri         | -                   | produced by ingest     |


### Proxy Schema for spot-ml
The table shows proxy schema attributes and the rules used specifically for machine learning (ml).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| p_date          | Can't be null       | -                      |
| p_time          | Can't be null       | -                      |
| clientip        | Can't be null       | -                      |
| host            | Can't be null       | -                      |
| reqmethod       | -                   | -                      |
| useragent       | -                   | Null will be replaced with "-" |
| resconttype     | -                   | Null will be replaced with "-" |
| duration        | -                   | -                      |
| username        | -                   | -                      |
| webcat          | -                   | -                      |
|referer          | -                   | -                      |
|respcode         | -                   | -                      |
|uriport          | -                   | -                      |
|uripath          | -                   | -                      |    
|uriquery         | -                   | -                      |
|serverip         | -                   | -                      |
|scbytes          | -                   | -                      |
|csbytes          | -                   | -                      |
|fulluri          | Can't be null       | -                      |


### Proxy Schema for spot-oa
The table shows proxy schema attributes and the rules used specifically for operation analytics (oa).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| p_date          | -                   | -                      |
| p_time          | -                   | -                      |
| clientip        | -                   | -                      |
| host            | -                   | -                      |
| reqmethod       | -                   | -                      |
| useragent       | -                   | -                      |
| resconttype     | -                   | -                      |
| duration        | -                   | -                      |
| username        | -                   | -                      |
| webcat          | -                   | -                      |
|referer          | -                   | -                      |
|respcode         | -                   | -                      |
|uriport          | -                   | -                      |
|uripath          | -                   | -                      |    
|uriquery         | -                   | -                      |
|serverip         | -                   | -                      |
|scbytes          | -                   | -                      |
|csbytes          | -                   | -                      |
|fulluri          | -                   | -                      |
| word            | -                   | -                      |
| ml_score        | -                   | -                      |
| respcode_name   | -                   | Produced by OA         |
| uri_rep         | -                   | Produced by OA         |
| network_context | -                   | Produced by OA         |


### Proxy Schema for spot-ui
The table shows proxy schema attributes and the rules used specifically for user interface (ui).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| p_date          | -                   | -                      |
| p_time          | -                   | -                      |
| clientip        | -                   | -                      |
| host            | -                   | -                      |
| reqmethod       | -                   | -                      |
| useragent       | -                   | -                      |
| resconttype     | -                   | -                      |
| duration        | -                   | -                      |
| username        | -                   | -                      |
| webcat          | -                   | -                      |
|referer          | -                   | -                      |
|respcode         | -                   | -                      |
|uriport          | -                   | -                      |
|uripath          | -                   | -                      |    
|uriquery         | -                   | -                      |
|serverip         | -                   | -                      |
|scbytes          | -                   | -                      |
|csbytes          | -                   | -                      |
|fulluri          | -                   | -                      |
| respcode_name   | -                   | Optional               |
| uri_rep         | -                   | Optional               |
| network_context | -                   | Optional               |


### Flow Schema for spot-ingest
The table shows flow schema attributes and the rules used specifically for ingest.

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| treceived  	  | -	                | -                      |
| unix_tstamp	  | -	                | produced by ingest     |
| tryear     	  | -	                | produced by spot-nfdump|
| trmonth    	  | -	                | produced by spot-nfdump|
| trday      	  | - 	                | produced by spot-nfdump|
| trhour     	  | -	                | produced by spot-nfdump|
| trminute   	  | -	                | produced by spot-nfdump|
| trsec      	  | -	                | produced by spot-nfdump|
| tdur       	  | -	                | -                      |
| sip        	  | -	                | -                      |
| dip        	  | -	                | -                      |
| sport      	  | -	                | -                      |
| dport      	  | -	                | -                      |
| proto      	  | -	                | -                      |
| flag       	  | -	                | -                      |
| fwd        	  | -	                | -                      |
| stos       	  | -	                | -                      |
| ipkt       	  | -	                | -                      |
| ibyt       	  | -	                | -                      |
| opkt       	  | -	                | -                      |
| obyt       	  | -	                | -                      |
| input      	  | -	                | -                      |
| output     	  | -	                | -                      |
| sas        	  | -	                | -                      |
| das        	  | -	                | -                      |
| dtos       	  | -	                | -                      |  
| dir        	  | -	                | -                      |
| rip        	  | -	                | -                      |


### Flow Schema for spot-ml
The table shows flow schema attributes and the rules used specifically for machine learning (ml).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| treceived  	  | Can't be null	    | -                      |
| tryear     	  |	-                   | -                      |
| trmonth    	  | -                   | -	                     |
| trday      	  |	-                   | -                      |
| trhour     	  | Should be a number between 0 and 23| -	     |
| trminute   	  | Should be a number between 0 and 59| -	     |
| trsec      	  | Should be a number between 0 and 59| -	     |
| tdur       	  | -                   | - 	                 |
| sip        	  | Can't be null	    | -                      |
| dip        	  | Can't be null	    | -                      |
| sport      	  | shlould be grater or equal to 0| - 	         |
| dport      	  | shlould be grater or equal to 0| -           |
| proto      	  | -                   | -	                     |
| ipkt       	  | shlould be grater or equal to 0| -	         |
| ibyt       	  | shlould be grater or equal to 0| -           |
| opkt       	  |	-                   | -                      |
| obyt       	  |	-                   | -                      |


### Flow Schema for spot-oa
The table shows flow schema attributes and the rules used specifically for operation analytics (oa).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| treceived    	  | -	                | -                      |
| sip             | -	                | -                      |
| dip             | -	                | -                      |
| sport           | -	                | -                      |
| dport           | -	                | -                      |
| proto           | -	                | -                      |
| flag            | -	                | -                      |
| stos            | -	                | -                      |
| ipkt            | -	                | -                      |
| ibyt            | -	                | -                      |
| opkt            | -	                | -                      |
| obyt            | -	                | -                      |
| input           | -	                | -                      |
| output          | -	                | -                      |
| rip             | -	                | -                      |
| ML_score        | -	                | -                      |



### Flow Schema for spot-ui
The table shows flow schema attributes and the rules used specifically for user interface (ui).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| treceived       | -	                | -                      |
| sip             | -	                | -                      |
| dip             | -	                | -                      | 
| sport           | -	                | -                      |
| dport           | -	                | -                      |
| proto           | -	                | -                      |
| flag            | -	                | -                      |
| stos            | -	                | -                      |
| ipkt            | -	                | -                      |
| ibyt            | -	                | -                      |
| opkt            | -	                | -                      |
| obyt            | -	                | -                      |
| input           | -	                | -                      |
| output          | -	                | -                      |
| rip             | -	                | -                      |
| rank            | -	                | -                      |
| srcip_internal  | -	                | -                      |
| dstip_internal  | -	                | -                      |
| src_geoloc      | -	                | -                      |
| dst_geoloc      | -	                | -                      |
| src_domain      | -	                | -                      |
| dst_domain      | -	                | -                      |
| src_rep         | -	                | -                      |
| dst_rep         | -	                | -                      |


### DNS Schema for spot-ingest
The table shows DNS schema attributes and the rules used specifically for ingest.

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| frame_time   	  | -	                | -                      |
| unix_tstamp  	  | -	                | -                      |
| frame_len    	  | -	                | -                      | 
| ip_dst       	  | -	                | -                      |
| ip_src       	  | -	                | -                      |
| dns_qry_name 	  | -	                | -                      |
| dns_qry_class	  | -	                | -                      |
| dns_qry_type 	  | -	                | -                      |
| dns_qry_rcode	  | -	                | -                      |
| dns_a        	  | -	                | -                      |


### DNS Schema for spot-ml
The table shows DNS schema attributes and the rules used specifically for machine learning (ml).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| frame_time   	  | Can't be null, empty string or "-"	                               |-|
| unix_tstamp  	  | Should be a number equal or greater than 0	                       |-|
| frame_len    	  | Should be a number equal or greater than 0	                       |-|
| ip_dst       	  | Can't be null, neither empty string or "-"	                       |-|
| dns_qry_name 	  | Can't be null, neither empty string or "-"	                       |-|
| dns_qry_class	  | If dns_qry_type and dns_qry_rcode are null, this one can't be null |-|
| dns_qry_type 	  | If dns_qry_class and dns_qry_rcode are null, this can't be null	   |-|
| dns_qry_rcode	  | If dns_qry_class and dns_qry_type are null, this can't be null	   |-|


### DNS Schema for spot-oa
The table shows DNS schema attributes and the rules used specifically for operation analytics (oa).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| frame_time      | -	                | -                      |
| unix_tstamp     | -	                | -                      |
| frame_len       | -	                | -                      |
| ip_dst          | -	                | -                      |
| ip_src          | -	                | -                      |
| dns_qry_name    | -	                | -                      |
| dns_qry_class   | -	                | -                      |
| dns_qry_type    | -	                | -                      |
| dns_qry_rcode   | -	                | -                      |
| ML_score        | -	                | -                      |


### DNS Schema for spot-ui
The table shows DNS schema attributes and the rules used specifically for user interface (ui).

| Spot field name | Rules               | Comments               |
|-----------------|---------------------|------------------------| 
| dns_a           | -	                | -                      |
| ML_score        | -	                | -                      |
| tld             | -	                | -                      |
| query_rep       | -	                | -                      |
| hh              | -	                | -                      |
| dns_qry_class_name | -                | -                      |
| dns_qry_type_name| -	                | -                      |
| dns_qry_rcode_name | -                | -                      |
| network_context | -	                | -                      |
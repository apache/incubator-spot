# Apache Spot Schema 

This document is to centralize a place where users can read information about Proxy, DNS and Netflow schema.

## Proxy

|                 |        |                                             |                     |            |             |Required for|Required for|Required for|
|-----------------|--------|---------------------------------------------|---------------------|------------|-------------|-------------|------------------|------------------|
|**Spot Field Name**|**Type**|**Description**                            |**Original Field Name**|**Format**|**Spot-ingest**|**Spot-ml**|**Spot-oa**       |**Spot-ui**       | 
| p_date          | string | Date for the connection                     |        date         | yyyy-mm-dd |   required  |Can't be null|:white_check_mark:|:white_check_mark:|
| p_time	      | string | Time for the connection	                 |        time	       |  hh:MM:SS  |	required  |Can't be null|:white_check_mark:|:white_check_mark:|
| clientip        | string |IP address of the client sending the request |        c-ip	       | ip address	|   required  |Can't be null|:white_check_mark:|:white_check_mark:|
| host        	  | string |Hostname from the client's request URL	     |       cs-host	   |    text	|   required  |Can't be null|:white_check_mark:|:white_check_mark:|
| reqmethod	      | string |Request method used from client to appliance (HTTP Method - GET, POST, CONNECT) |	cs-method | 	text |	required |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| useragent	      | string |Browser Type	                             | cs(User-Agent)	   |quoted text	|required 	  |Can be null but null will be replaced with "-"|:white_check_mark:|:white_check_mark:|
| resconttype	  | string |Content-type (Ex. text/html, image/xml)	     |rs(Content-Type) 	   | text	    |required	  |Can be null but null will be replaced with "-"|:white_check_mark:|:white_check_mark:|
| duration	      |  int   |Duration of the connection	                 |time-taken	       |numerical	|required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| username	      |string  |Client Username	                             |cs-username	       |text	    |required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| authgroup   	  |string  |Client Authentication Group	                 |cs-auth-group 	   |text	    |required	  |	-	    |     -    |       -  |
| exceptionid	  |string  |Identifier of the exception resolved (empty if the transaction has not been terminated) |	x-exception-id 	| text	| required |- | - |     -         |	
| filterresult    |string  |Content filtering result: Denied, Proxied or Observed | sc-filter-result | text | required    |     -    |      -   |      -   |			
| webcat	      |string  |All content categories of the request URL	 |cs-categories        |quoted text	| required    |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| referer	      |string  |Request header: Referer %S s-sitename The service type used to | cs(Referer) | url | required |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| respcode	      |string  |Protocol status code from appliance to client (HTTP Response Codes) | sc-status | numerical |required |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| action	      |string  |What type of action did the Appliance take to process this request; possible values include ALLOWED, DENIED, FAILED, SERVER_ERROR|s-action |text |required | -| -|- | 			
| urischeme	      |string  |Scheme of the original URL requested	     |cs-uri-scheme 	   |text	    |required	  |      -   |     -    |      -   |		
| uriport	      |string  |Port from the original URL requested	     |cs-uri-port 	       |numerical	|required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| uripath	      |string  |Path of the original URL requested without query |cs-uri-path 	   |text	    |required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| uriquery	      |string  |Query from the original URL requested	     |cs-uri-query	       |text	    |required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| uriextension	  |string  |Document extension from the original URL requested |cs-uri-extension |text	    |required	  |      -   |     -    |      -   |		
| serverip	      |string  |IP address of the appliance on which the client established its connection |s-ip  |ip address |required |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| scbytes	      |int	   |Number of bytes sent from appliance to client|sc-bytes             |numerical	|required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| csbytes	      |int	   |Number of bytes sent from client to appliance|cs-bytes 	           |numerical	|required	  |:white_check_mark:|:white_check_mark:|:white_check_mark:|
| virusid	      |string  |x-virus-id 	                                 |x-virus-id 	       |text	    |required	  |    -     |    -     |       -  |		
| bcappname	      |string  |x-bluecoat-application-name 	             |x-bluecoat-application-name |quoted text |required |  -   |  -       |    -     |			
| bcappoper	      |string  |x-bluecoat-application-operation	         |x-bluecoat-application-operation |quoted text |required |- |   -     |    -     |			
|fulluri	      |string  |Full URI concatenated from cs-host, cs-uri-path, cs-uri-query fields |it does not exist, it is calculated during ingest |text |produced by ingest |Can't be null|:white_check_mark:|:white_check_mark:|
| word 	          |string  |      -                					     |           -          |   -         |     -        |  -       |:white_check_mark:|      -   |
| ml_score	      |float   |				-	                         |          -           |      -      |        -     |   -      |:white_check_mark:|  -       |
| respcode_name   |string  |IANA translation for the response code column|                -     |     -       |      -       |       -  |*Produced by OA | Optional |
| uri_rep	      |string  |Reputation value according to Threat intelligence services| 	-   |	-		|        -     |  -       |*Produced by OA | Optional |
| network_context |string  |User defined value					         |              -       |     -       |       -      |     -    |*Produced by OA | Optional | 


## DNS

|                 |        |                                             |                     |            |             |             |                  |                  |
|-----------------|--------|---------------------------------------------|---------------------|------------|-------------|-------------|------------------|------------------|
|**Spot Field Name**|**Type**|**Description**                            |**Original Field Name**|**Format**|**Spot-ingest**|**Spot-ml**|**Spot-oa**       |**Spot-ui**       | 
|                 |        |                                             |                     |            |             |             |                  |                  |

## Netflow

|                 |        |                                             |                     |            |             |             |                  |                  |
|-----------------|--------|---------------------------------------------|---------------------|------------|-------------|-------------|------------------|------------------|
|**Spot Field Name**|**Type**|**Description**                            |**Original Field Name**|**Format**|**Spot-ingest**|**Spot-ml**|**Spot-oa**       |**Spot-ui**       | 
|                 |        |                                             |                     |            |             |             |                  |                  |
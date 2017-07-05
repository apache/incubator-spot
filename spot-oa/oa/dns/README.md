# DNS

DNS sub-module extracts and transforms DNS (Domain Name Service) data already ranked by spot-ml and will load it into impala tables for the presentation layer.

## DNS Components

### dns_oa.py

DNS spot-oa main script executes the following steps:


		1. Creates the right folder structure to store the data and the ipython notebooks. This is: 
		
			data: data/dns/<date>/
			ipython Notebooks: ipynb/dns/<date>/
		
		2. Creates a copy of the notebooks templates into the ipython Notebooks path and renames them removing the "_master" part from the name.

		3. Gets the dns_results.csv from the HDFS location according to the selected date, and copies it back to the corresponding data path.

		4. Reads a given number of rows from the results file.

		5. Gets the top level domain out of the dns_qry_name, and adds it in the new column 'tld'.

		6. Checks reputation for the query_name of each connection.

		7. Adds two new columns for the severity of the query_name and the client ip of each connection.

		8. Adds a new column with the hour part of the frame_time.

		9. Translates the 'dns_query_class', 'dns_query_type','dns_query_rcode' to human readable text according to the IANA specification. The translated values are stored in the dns_qry_class_name, dns_qry_type_name, dns_qry_rcode_name columns, respectively.

		10. Adds Network Context.

		11. Saves results to the dns_scores table.

    	12. Generates details and dendrogram diagram data. These details include information about aditional connections to display the details table in the UI.


**Dependencies**

- [Python 2.7](https://www.python.org/download/releases/2.7/) should be installed in the node running Proxy OA.  
- [TLD 0.7.6](https://pypi.python.org/pypi/tld/0.7.6)

	The following modules are already included but some of them require configuration. See the following sections for more information. 
- [components/iana](/spot-oa/oa/components#IANA-iana)
- [components/data](/spot-oa/oa/components#data)
- [components/nc](/spot-oa/oa/components#network-context-nc)
- [components/reputation](/spot-oa/oa/components/reputation)
- dns_conf.json 
 
    
**Prerequisites**

Before running DNS OA users need to configure components for the first time. It is important to mention that configuring these components make them work for other data sources as Flow and Proxy.  

- Configure database engine
- Configure required dependencies
- Configure GTI services
- Configure IANA service
- Configure Network Context service
- Configure Geolocation 
- Generate ML results for DNS
  

**Output**

- DNS suspicious connections. _dns\_scores_ table.

Main results for Flow OA. Main results file for DNS OA. The data stored in this table is limited by the number of rows the user selected when running [oa/start_oa.py](/spot-oa/oa/INSTALL.md#usage).
  
		0.frame_time: string		
		1.unix_tstamp: bigint		
		2.ip_dst: string		
		3.dns_qry_name: string		
		4.dns_qry_class: string		
		5.dns_qry_type: int		
		6.dns_qry_rcode: int
		7.score: double	
		8.tld: string		
		9.query_rep: string		
		10.hh: string	
		11.dns_qry_class_name: string		
		12.dns_qry_type_name: string		
		13.dns_qry_rcode_name: string		
		14.network_context: string	 


- DNS details _dns\_scores_ table.  

One file for each source IP. This file includes information about all the queries made to a particular DNS query name. The number of retrieved rows is limited by the value of "\_details\_limit" parameter
 
		0.unix_tstamp bigint 
		1.dns_a string
		2.dns_qry_name string
		3.ip_dst string 


- DNS Details: _dns\_dendro_ table.  

One file for each DNS query name for each hour of the day. This file contains details for each
connection between DNS and source IP.
 
		0.unix_tstamp bigint
    	1.frame_len bigint
    	2.ip_dst string
    	3.ip_src string
    	4.ns_qry_name string
    	5.dns_qry_class string
    	6.dns_qry_type int
    	7.dns_qry_rcode int
    	8.dns_a string
    	9.hh int
    	10.dns_qry_class_name string
    	11.dns_qry_type_name string
    	12.dns_qry_rcode_name string
    	13.network_context string


- DNS Ingest summary. _dns\_ingest\_summary_ table.

This table is populated with the number of connections ingested by minute during that day.

        Table schema:
        0. tdate:      string
        1. total:      bigint 
 

### dns_conf.json
This file is part of the initial configuration for the DNS pipeline. It will contain mapped all the columns included in the _dns\_edge_ and _dns\_dendro_ tables.

This file contains three main arrays:

	-  dns_results_fields: Reference of the column name and indexes in the dns_results.csv file.	 
	-  dns_score_fields:  Reference of the column name and indexes in the _dns\_edge_ table.
	-  add_reputation: According to the dns_results.csv file, this is the column index of the value which will be evaluated using the reputation services.


### ipynb_templates 
Templates for iPython notebooks.
After OA process completes, a copy of each iPython notebook is going to be copied to the ipynb/\<pipeline>/\<date> path. 
With these iPython notebooks user will be able to perform further analysis and score connections. User can also
experiment adding or modifying the code. 
If a new functionality is required for the ipython notebook, the templates need to be modified to include the functionality for new executions.
For further reference on how to work with these notebooks, you can read:   
[Threat_Investigation.ipynb](/spot-oa/oa/dns/ipynb_templates/ThreatInvestigation.md)


### Reset scored connections
To reset all scored connections for a day, a specific cell with a preloaded function is included in the Advanced Mode Notebook. The cell is commented to avoid accidental executions, but is properly labeled.
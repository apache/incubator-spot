# PROXY

Proxy sub-module will extract and transform Proxy data already ranked by spot-ml and will load it into impala tables for the presentation layer.

## Proxy Components

### proxy_oa.py

Proxy spot-oa main script executes the following steps:

		1. Creates the right folder structure to store the data and the ipython notebooks. This is: 
		
			data: data/proxy/<date>/
			ipython Notebooks: ipynb/proxy/<date>/
		
		2. Creates a copy of the notebooks templates into the ipython Notebooks path and renames them removing the "_master" part from the name.
		
		3. Gets the proxy_results.csv from the HDFS location according to the selected date, and copies it back to the corresponding data path.
		 
		4. Reads a given number of rows from the results file.
		 
		5. Checks reputation for the full uri of each connection.
		 
		6. Adds a new column for the severity of each connection.
		 
		7. Translates the 'response code' to human readable text according to the IANA specification. The translated values are stored in the respcode_name column.
		 
		8. Add Network Context.
		
		9. Creates a hash for every full_uri + clientip pair to use as filename.  
		 
		10. Saves the data in the _proxy_\scores_ table. 
		
    	12. Collects information about additional connections to display the details table in the UI.


**Dependencies**

- [Python 2.7](https://www.python.org/download/releases/2.7/) should be installed in the node running Proxy OA. 

	The following modules are already included but some of them require configuration. Please refer to the _components_ documentation for more information. 
- [components/iana](/spot-oa/oa/components#IANA-iana)
- [components/data](/spot-oa/oa/components#data)
- [components/nc](/spot-oa/oa/components#network-context-nc)
- [components/reputation](/spot-oa/oa/components/reputation)
- proxy_conf.json

**Prerequisites**

Before running Proxy OA, users need to configure components for the first time. It is important to mention that configuring these components make them work for other data sources as Flow and DNS.  

- Configure database engine
- Configure Reputation services
- Configure IANA service
- Configure Network Context service
- Generate ML results for Proxy

**Output**

- Proxy suspicious connections. _proxy\_scores_ table.

Main results file for Proxy OA. The data stored in this table is limited by the number of rows the user selected when running [oa/start_oa.py](/spot-oa/oa/INSTALL.md#usage).
 
		0.tdate string
		1.time string
		2.clientip string
		3.host string
		4.reqmethod string
		5.useragent string
		6.resconttype string
		7.duration int
		8.username string
		9.webcat string
		10.referer string
		11.respcode string
		12.uriport string
		13.uripath string
		14.uriquery string
		15.serverip string
		16.scbytes int
		17.csbytes int
		18.fulluri string
		19.word string
		20.ml_score Float
		21.uri_rep string
		22.respcode_name string
		23.network_context string 


- Proxy details. _proxy\_edge_ table.

A query will be executed for each fulluri + clientip connection for each hour of the day.
 
		0.tdate STRING
		1.time STRING
		2.clientIp STRING
		3.host string
		4.webcat string
		5.respcode string
		6.reqmethod string
		7.useragent string
		8.resconttype string
		9.referer string
		10.uriport string
		11.serverip string
		12.scbytes int
		13.csbytes int
		14.fulluri string
		15.hh int
		16.respcode_name string


- Proxy Ingest summary. _proxy\_ingest\_summary_ table.

This table is populated with the number of connections ingested by minute during that day.

        Table schema:
        0. tdate:      string
        1. total:      bigint 


### proxy_conf.json
This file is part of the initial configuration for the proxy pipeline It will contain mapped all the columns included in the proxy_results.csv and proxy tables.

This file contains three main arrays:

	-  proxy_results_fields: Reference of the column name and indexes in the proxy_results.csv file.	 
	-  proxy_score_fields:  Reference of the column name and indexes in the proxy_scores.tsv file.	
	-  add_reputation: According to the proxy_scores.tsv file, this is the column index of the value which will be evaluated using the reputation services.


### ipynb_templates
After OA process completes, a copy of each iPython notebook is going to be copied to the ipynb/\<pipeline>/\<date> path. 
With these iPython notebooks user will be able to perform further analysis and score connections. User can also
experiment adding or modifying the code. 
If a new functionality is required for the ipython notebook, the templates need to be modified to include the functionality for new executions.
For further reference on how to work with these notebooks, you can read:   
[Threat_Investigation.ipynb](/spot-oa/oa/proxy/ipynb_templates/ThreatInvestigation.md)


### Reset scored connections
To reset all scored connections for a day, a specific cell with a preloaded function is included in the Advanced Mode Notebook. The cell is commented to avoid accidental executions, but is properly labeled.

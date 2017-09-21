# **Flow OA**
 
Flow sub-module extracts and transforms Flow data already ranked by spot-ml and will load into impala tables for presentation the layer.

## **Flow OA Components**

### flow_oa.py
Flow spot-oa main script executes the following steps:

    1. Creates required folder structure if does not exist for output files. This is: 
		
                data: data/flow/<date>/
                ipython Notebooks: ipynb/flow/<date>/

    2. Creates a copy of iPython notebooks out of templates in ipynb_templates folder into output folder.

    3. Reads Flow spot-ml results for a given date and loads only the requested limit.

    4. Add network context to source and destination IPs.

    5. Add geolocation to source and destination IPs.

    6. Stores transformed data in the selected database.

    7. Generates details and chord diagram data. These details include information about aditional connections and some additional information to draw chord diagrams in the UI.

**Dependencies**

- [Python 2.7](https://www.python.org/download/releases/2.7/) should be installed in the node running Flow OA.

The following files and modules are already included but some of them require configuration. See the following sections for more information:
- [components/iana](/spot-oa/oa/components#IANA-iana)
- [components/data](/spot-oa/oa/components#data)
- [components/nc](/spot-oa/oa/components#network-context-nc)
- [components/reputation](/spot-oa/oa/components/reputation)
- flow_config.json

The following files are not included:
- [context/iploc.csv](/spot-oa/oa/context)
- [context/ipranges.csv](/spot-oa/oa/context)

**Prerequisites**

Before running Flow OA users need to configure components for the first time. It is important to mention that configuring these components make them work for other data sources as DNS and Proxy.  

- Configure database engine
- Configure GTI services
- Configure IANA service
- Configure Network Context service
- Configure Geolocation service
- Generate ML results for Flow  

**Output**

- Flow suspicious connections. _flow\_scores_ table.  

Main results for Flow OA. The data stored in this table is limited by the number of rows the user selected when running [oa/start_oa.py](/spot-oa/oa/INSTALL.md#usage).
       
        Table schema:
        0.   tstart:         string
        1.   srcip:          string
        2.   dstip:          string
        3.   sport:          int
        4.   dport:          int
        5.   proto:          string
        6.   ipkt:           int
        7.   ibyt:           int
        8.   opkt:           int
        9.   obyt:           int
        10.  score:          float
        11.  rank:           int
        12.  srcip_internal:  bit
        13.  destip_internal: bit
        15.  src_geoloc:         string
        16.  dst_geoloc:         string
        17.  src_domain:      string
        18.  dst_domain:      string
        19.  src_rep:      string
        20.  dst_rep:      string

-  Flow details. _flow\_edge_ table.

A query will be executed for each suspicious connection detected, to find the details for each connection occurred during the same specific minute between given source IP and destination IP.

        Table schema:
        0.  tstart:     string
        1.  srcip:      string
        2.  dstip:      string
        3.  sport:      int
        4.  dport:      int
        5.  proto:      string
        6.  flags:      string
        7.  tos:        int
        8.  ibyt:       bigint
        9.  ipkt:       bigint
        10.  pkts:      bigint
        11. input:      int
        12. output:     int
        13. rip:        string
        14. obyt:       bigint
        15. opkt:       bigint
        16. hh:         int
        17. md:         int         

- Flow Chord Diagrams.  _flow\_chords_ table.

A query will be executed for each distinct client ip that has connections to 2 or more other suspicious IP. This query will retrieve the sum of input packets and bytes transferred between the client ip and every other suspicious IP it connected to.

        Table schema:
        0. ip_threat:  string
        1. srcip:      string
        2. dstip:      string
        3. ibyt:       bigint
        4. ipkt:       bigint


- Flow Ingest summary. _flow\_ingest\_summary_ table.

This table is populated with the number of connections ingested by minute during that day.

        Table schema:
        0. tdate:      string
        1. total:      bigint 


### flow_config.json

Flow spot-oa configuration. Contains columns name and index for input and output files.
This Json file contains 3 main arrays:
   
    - flow_results_fields: list of column name and index of ML flow_results.csv file. Flow OA uses this mapping to reference columns by name.
    - column_indexes_filter: the list of indices to take out of flow_results_fields for the OA process. 
    - flow_score_fields: list of column name and index for flow_scores.csv. After the OA process completes more columns are added.
    

### ipynb_templates
Templates for iPython notebooks.
After OA process completes, a copy of each iPython notebook is going to be copied to the ipynb/\<pipeline>/\<date> path. 
With these iPython notebooks user will be able to perform further analysis and score connections. User can also
experiment adding or modifying the code. 
If a new functionality is required for the ipython notebook, the templates need to be modified to include the functionality for posterior executions.
For further reference on how to work with this notebook, you can read:  
- [Threat Investigation Notebook](/spot-oa/oa/flow/ipynb_templates/ThreatInvestigation.md)


### Reset scored connections
To reset all scored connections for a day, a specific cell with a preloaded function is included in the Advanced Mode Notebook. The cell is commented to avoid accidental executions, but is properly labeled.
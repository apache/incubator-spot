# **Flow OA**
 
Flow sub-module extracts and transforms Flow data already ranked by oni-ml and will load into csv files for presentation layer.

## **Flow OA Components**

### flow_oa.py
Flow oni-oa main script executes the following steps:

    1. Creates required folder structure if does not exist for output files. This is: 
		
                data: data/flow/<date>/
                ipython Notebooks: ipynb/flow/<date>/

    2. Creates a copy of iPython notebooks out of templates in ipynb_templates folder into output folder.
    3. Reads Flow oni-ml results for a given date and loads only the requested limit.
    4. Add network context to source and destination IPs.
    5. Add geolocation to source and destination IPs.
    6. Saves transformed data into a new csv file, this file is called flow_scores.csv.
    7. Creates details, and chord diagram files. These details include information about each suspicious connection and some additional information
       to draw chord diagrams.

**Dependencies**

- [Python 2.7](https://www.python.org/download/releases/2.7/) should be installed in the node running Flow OA.

The following files and modules are already included but some of them require configuration. See the following sections for more information:
- [components/iana](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components#IANA-iana)
- [components/data](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components#data)
- [components/nc](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components#network-context-nc)
- [components/reputation](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components/reputation)
- flow_config.json

The following files are not included:
- [context/iploc.csv](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/context)
- [context/ipranges.csv](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/context)

**Prerequisites**

Before running Flow OA users need to configure components for the first time. It is important to mention that configuring these components make them work for other data sources as DNS and Proxy.  

- Configure database engine
- Configure GTI services
- Configure IANA service
- Configure Network Context service
- Configure Geolocation service
- Generate ML results for Flow  

**Output**

- flow_scores.csv. Main results file for Flow OA. This file will contain suspicious connects information and it's limited to the number of rows the user selected when running [oa/start_oa.py](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/INSTALL.md#usage).
       
        Schema with zero-indexed columns:
        0.   sev:            int
        1.   tstart:         string
        2.   srcIP:          string
        3.   dstIP:          string
        4.   sport:          int
        5.   dport:          int
        6.   proto:          string
        7.   flag:           string
        8.   ipkt:           bigint
        9.   ibyt:           bigint
        10.  lda_score:      double
        11.  rank:           int
        12.  srcIpInternal:  bit
        13.  destIpInternal: bit
        14.  srcGeo:         string
        15.  dstGeo:         string
        16.  srcDomain:      string
        17.  dstDomain:      string
        18.  srcIP_rep:      string
        29.  dstIP_rep:      string
       
- flow_scores_bu.csv. Backup file for flow_scores.csv in case user needs to roll back the scoring or the changes made during analysis. Schema it's same as flow_scores.csv.

- edge-\<source IP>-\<destination IP>-\<HH>-\<MM>.tsv. Edge files. One for each suspicious connection containing the details for each comunication occurred during the same specific minute between source IP and destination IP.

        Schema with zero-indexed columns:
        0.  tstart:     string
        1.  srcip:      string
        2.  dstip:      string
        3.  sport:      int
        4.  dport:      int
        5.  proto:      string
        6.  flags:      string
        7.  tos:        int
        8.  bytes:      bigint
        9.  pkts:       bigint
        10. input:      int
        11. output:     int
        12. rip:        string

- chord-\<client ip>.tsv. Chord files. One for each distinct client ip. These files contain details of packets and data transferred between the client ip and every other IP it connected to.

        Schema with zero-indexed columns:
        0.  srcip:      string
        1.  dstip:      string
        2.  maxbyte:    bigint
        3.  avgbyte:    double
        4.  maxpkt:     bigint
        5.  avgpkt:     double
        
### flow_config.json

Flow oni-oa configuration. Contains columns name and index for input and output files.
This Json file contains 3 main arrays:
   
    - flow_results_fields: list of column name and index of ML flow_results.csv file. Flow OA uses this mapping to reference columns by name.
    - column_indexes_filter: the list of indices to take out of flow_results_fields for OA process. 
    - flow_score_fields: list of column name and index for flow_scores.csv. After the OA process completes more columns are added.
        


### ipynb_templates
Templates for iPython notebooks.
After OA process completes, a copy of each iPython notebook is going to be copied to the ipynb/\<pipeline>/\<date> path. 
With these iPython notebooks user will be able to perform further analysis and score connections. User can also
experiment adding or modifying the code. 
If a new functionality is required for the ipython notebook, the templates need to be modified to include the functionality for new executions.
For further reference on how to work with these notebooks, you can read:  
- [Edge Notebook](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/flow/ipynb_templates/EdgeNotebook.md)
- [Threat Investigation Notebook](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/flow/ipynb_templates/ThreatInvestigation.md)
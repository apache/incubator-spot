#Flow Threat Investigation Notebook

###Dependencies  
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip) 

The following python modules will have to be imported for the notebook to work correctly:  

        import struct, socket
        import numpy as np
        import linecache, bisect
        import csv
        import operator
        import json
        import os
        import ipywidgets as widgets # For jupyter/ipython >= 1.4
        from IPython.html import widgets
        from IPython.display import display, Javascript, clear_output


##Pre-requisites  
- Execution of the oni-oa process for Flow
- Score a set connections at the Edge Investigation Notebook 
- Correct setup the duxbay.conf file. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration) 
- Include a comma separated network context file. **Optional** [Schema](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components/README.md#network-context-nc)
- Include a geolocation database file. [Schema](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components/README.md#geoloc)   


##Additional Configuration
`top_results` - This value defines the number of rows that will be displayed onscreen after the expanded search. 
This also affects the number of IPs that will appear in the Timeline chart.


##Data source  
The whole process in this notebook depends entirely on the existence of the scored _flow_scores.csv_ file, which is generated at the OA process, and scored at the Edge Investigation Notebook.

**Input files**  
All these paths should be relative to the main OA path.    
Schema for these files can be found here:

[flow_scores.csv](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/oa/flow)  
[iploc.csv](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components/README.md#geoloc)  
[networkcontext_1.csv](https://github.com/Open-Network-Insight/oni-oa/blob/1.1/oa/components/README.md#network-context-nc)  
  

        data/flow/<date>/flow_scores.csv  
        context/iploc.csv
        context/networkcontext_1.csv
 

**Output files**  
- threats.csv : Pipe separated file containing the comments saved by the user. This file is updated every time the user adds comments for a new threat. 
        
        Schema with zero-indexed columns:
        
        0.ip: string
        1.title: string
        2.description: string
 
- sbdet-\<ip>.tsv : Tab separated file, this file lists all the client IP's that connected to the IP under investigation, including: 
the duration of the connection, response code and exact date and time of each the connection.  
        Schema with zero-indexed columns:
        
        0.tstart: string
        1.tend: string
        2.srcip	: string
        3.dstip : string
        4.proto : string
        5.sport : string
        6.dport : string
        7.pkts : string
        8.bytes : string


- globe-\<ip>.json : Json file including the geolocation of all the suspicious connections related to the IP under investigation. 
                Schema:

                {
                        "destips": [{
                                "geometry": {
                                        "type": "Point",
                                        "coordinates": <[Lat, Long] values from the geolocation database>
                                },
                                "type": "Feature",
                                "properties": {
                                        "ip": "<dst IP>",
                                        "type": <1 for Inbound, 2 for Outbound, 3 for Two way>,
                                        "location": "<Host name provided by the geolocation db>"
                                },
                                ......
                        }
                        }],
                        "type": "FeatureCollection",
                        "sourceips": [{
                                "geometry": {
                                        "type": "Point",
                                        "coordinates": <[Lat, Long] values from the geolocation database>
                                },
                                "type": "Feature",
                                "properties": {
                                        "ip": "<src ip>",
                                        "type": <1 for Inbound, 2 for Outbound, 3 for Two way>,
                                        "location": "<Host name provided by the geolocation db>"
                                },
                                ......
                        }]
                }
 

- stats-\<ip>.json: Json file containing the count of connections of each kind made to/from the suspicious IP.
                Schema:

                {
                        "size": <total of connections>,
                        "name": "<Name of suspicious IP, according to the network context",
                        "children": [{
                                "size": <Total number of Inbound connections>,
                                "name": "Inbound Only", 
                                "children": 
                                        [{
                                                "name": "<Context name>",
                                                "size": <Number of connections>
                                        }, ...
                                        ]
                                },
                                {"size": <Total number of Outbound connections>,
                                 "name": "Outbound Only", 
                                 "children": 
                                        [{
                                                "name": "<Context name>",
                                                "size": <Number of connections>
                                        }, ...
                                        ]
                                }, 
                                {"size": <Total number of Two way connections>,
                                 "name": "two way",
                                 "children":
                                        [{
                                                "name": "<Context name>",
                                                "size": <Number of connections>
                                        }, ...
                                        ]
                                }]
                        }
  

 - threat-dendro-\<ip>.json : Json file including the breakdown of the connections performed by the suspicious IP.  

                Schema: 

                {"time": "date in YYYYMMDD format>",
                 "name": "<suspicious IP>",
                 "children": [{
                        "impact": 0,
                        "name": "<Type of connections>", 
                        "children": [
                                <Individual connections named after the network context>
                                ]
                        }]
                }

**HDFS tables consumed**  

                flow
   

##FUNCTIONS  

**Widget configuration**

This is not a function, but more like global code to set up styles and widgets to format the output of the notebook.   

`start_investigation():` - This function cleans the notebook from previous executions, then loops through the _flow_scores.csv_ file to get the 'srcIp' and 'dstIP' values from connections scored as high risk (sev = 1), ignoring IPs
already saved in the _threats.csv_ file. 

`display_controls():` - This function will display the ipython widgets with the listbox of high risk IP's and the "Search" button.

`search_ip()` - This function is triggered by the onclick event of the "Search" button after selecting an IP from the listbox. This will perform a query to the _flow_ table to find all connections involving the selected IP.
 The results are stored in the _ir-\<ip>.tsv_ file. If the file is not empty, this will immediately execute the following functions:  
 - get_in_out_and_twoway_conns()
 - add_geospatial_info()
 - add_network_context() 
 - display_threat_box()

`get_in_out_and_twoway_conns():` - With the data from the _ir-\<ip>.tsv_ file, this function will loop through each connection and store it into one of three dictionaries:
- All unique ‘inbound’ connected IP's (Where the internal sought IP appears only as destination, or the opposite if the IP is external)  
- All unique ‘outbound’ connected IP's (Where the internal sought IP appears only as source, or the opposite if the IP is external)
- All unique ‘two way’ connected IP's (Where the sought IP appears as both source and destination)
This function will also call the _get_top_bytes()_ and _get_top_conns()_ functions to create sub dictionaries for each type of connection. 

`display_threat_box(anchor):` - Displays the widgets for "Title", "Comments" textboxes and the "Save" button on the notebook, so the user can add comments related to the threat and save them to continue with the analysis.  

`add_network_context()` - This function depends on the existence of the _networkcontext\_1.csv_ file, otherwise this step will be skipped.
This function will loop through all dictionaries updating each IP with its context depending on the ranges defined in the networkcontext.

`add_geospatial_info()` - This function depends on the existence of the _iploc.csv_ file. This will read through the dictionaries created, looking for every IP and updating its geolocation data according to the iploc database. If the iploc file doesn't exist, this function will be skipped.

`save_threat_summary()` - This function is triggered by the onclick event of the "Save" button. Removes the widgets and cleans the notebook from previous executions, removes the selected value from the listbox widget and 
 executes each of the following functions to create the data source files for the storyboard:
- generate_attack_map_file()
- generate_stats()
- generate_dendro()
- details_inbound()
- add_threat() 

`generate_attack_map_file(ip, inbound, outbound, twoway): `- This function depends on the existence of the _iploc.csv_ file. Using the geospatial info previously added to the dictionaries, this function will create the _globe.json_ file. If the iploc file doesn't exist, this function will be skipped.

`generate_stats(ip, inbound, outbound, twoway, threat_name):` - This function reads through each of the dictionaries to group the connections by type. The results are stored in the _stats-&lt;ip&gt;.json_ file. 

`generate_dendro(ip, inbound, outbound, twoway, date):` - This function groups the results from all three dictionaries into a json file, adding additionals level if the dictionaries include network context for each IP. 
The results are stored in the _threat-dendro-&lt;ip&gt;.json_ file.

`details_inbound(ip, inbound):` -  This function executes a query to the _flow_ table looking for all additional information between the shought IP (threat) and the IP's in the 'top_n' dictionaries. The results will be stored in the _sbdet-&lt;ip&gt;.tsv_ file.
 
`add_threat(ip,threat_title):`- Creates or updates the _threats.csv_ file, appending the IP and Title from the web form. This will serve as the menu for the Story Board.

`get_top_bytes(conns_dict, top):` - Orders a dictionary descendent by number of bytes, returns a dictionary with the top 'n' values. This dictionary will be printed onscreen, listing the most active connections first.   

`get_top_conns(conns_dict, top):` - Orders a dictionary descendent by number of connections executed, returns a dictionary with the top 'n' values. This dictionary will be printed onscreen, listing the most active connections first.   

`file_is_empty(path):` - Performs a validation to check the file of a size to determine if it is empty.
 
`removeWidget(index):` - Javascript function that removes a specific widget from the notebook.
 
`get_ctx_name(full_context): ` **Deprecated**    

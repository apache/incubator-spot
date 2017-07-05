# Flow Threat Investigation Notebook

### Dependencies  
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip) 

The following python modules will have to be imported for the notebook to work correctly:  

        import datetime
        import struct, socket
        import numpy as np
        import linecache, bisect
        import csv
        import operator
        import json
        import os
        import pandas as pd
        import ipywidgets as widgets # For jupyter/ipython >= 1.4
        from IPython.html import widgets
        from IPython.display import display, Javascript, clear_output


## Pre-requisites  
- Execution of the spot-oa process for Flow
- Correct installation of the UI [Read more](/ui/INSTALL.md)
- Score a set connections at the Edge Investigation Notebook 
- Correct setup the spot.conf file. [Read more](/wiki/Edit%20Solution%20Configuration) 
- Include a comma separated network context file. **Optional** [Schema](/spot-oa/oa/components/README.md#network-context-nc)
- Include a geolocation database file.  **Optional** [Schema](/spot-oa/oa/components/README.md#geoloc)   


## Additional Configuration inside the notebook
`top_results` - This value defines the number of rows that will be displayed onscreen after the expanded search. 
This also affects the number of IPs that will appear in the Timeline chart.


## Data source  
Data should exists in the following tables:
        *flow*
        *flow_threat_investigation*


**Input files**  
All these paths should be relative to the main OA path.    
Schema for these files can be found here:
 
[iploc.csv](/spot-oa/oa/components/README.md#geoloc)  
[networkcontext_1.csv](/spot-oa/oa/components/README.md#network-context-nc)  
  
 
        context/iploc.csv
        context/networkcontext_1.csv
 

**Output**  
The following tables will be populated after the threat investigation process:
        *flow_storyboard*
        *flow_timeline*

The following files will be created and stored in HDFS.
 
        globe-\<ip>.json
        stats-\<ip>.json:
        threat-dendro-\<ip>.json

## FUNCTIONS  

**Widget configuration**

This is not a function, but more like global code to set up styles and widgets to format the output of the notebook.   

`start_investigation():` - This function cleans the notebook from previous executions, and calls the *threats* query to get the source and destination IP's previously scored as high risk. 

`display_controls(threat_list):` - This function will display the ipython widgets with the listbox of high risk IP's and the "Search" button.

`search_ip()` - This function is triggered by the onclick event of the "Search" button after selecting an IP from the listbox. This calls the graphql *threat / details* query to find additional connections involving the selected IP. 

`get_in_out_and_twoway_conns():` - With the data from the previous method, this function will loop through each connection and store it into one of three dictionaries:
- All unique ‘inbound’ connected IP's (Where the internal sought IP appears only as destination, or the opposite if the IP is external)  
- All unique ‘outbound’ connected IP's (Where the internal sought IP appears only as source, or the opposite if the IP is external)
- All unique ‘two way’ connected IP's (Where the sought IP appears as both source and destination)
This function will also call the _get_top_bytes()_ and _get_top_conns()_ functions to create sub dictionaries for each type of connection and use those to generate the storyboard.
To aid on the analysis, this function displays four html tables each containing the following data:
- Top n source IP by connections
- Top n source IP per bytes transfered
- Top n destination IP by connections
- Top n destination IP per bytes transfered


`display_threat_box(ip):` - Displays the widgets for "Title", "Comments" textboxes and the "Save" button on the notebook, so the user can add comments related to the threat and save them to continue with the analysis.  

`save_threat_summary()` - This function is triggered by the onclick event of the "Save" button. Removes the widgets and cleans the notebook from previous executions, removes the selected value from the listbox widget and executes the *createStoryboard* mutation to save the data for the storyboard.

`display_results(cols, dataframe, top)` - 
*cols*: List of columns to display from the dataframe
*dataframe*: Dataframe to display
*top*: Number of top rows to display.
This function will create a formatted html table to display the provided dataframe.

`get_top_bytes(conns_dict, top):` - Orders a dictionary descendent by number of bytes, returns a dictionary with the top 'n' values. This dictionary will be printed onscreen, listing the most active connections first.   

`get_top_conns(conns_dict, top):` - Orders a dictionary descendent by number of connections executed, returns a dictionary with the top 'n' values. This dictionary will be printed onscreen, listing the most active connections first.   

`removeWidget(index):` - Javascript function that removes a specific widget from the notebook.

#DNS Threat Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip)

The following python modules will have to be imported for the notebook to work correctly:

        import struct, socket
        import numpy as np 
        import csv, json 
        import os 
        import urllib2 
        import datetime
        import operator
        import itertools
        import ipywidgets as widgets # For jupyter/ipython >= 1.4
        from IPython.html import widgets
        from IPython.display import display, Javascript, clear_output

##Pre-requisites  
- Execution of the oni-oa process for DNS 
- Score a set connections in the Edge Investigation Notebook
- Correct setup of the duxbay.conf file. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration) 


##Additional Configuration  
`top_results` - This value defines the number of rows that will be displayed onscreen after the expanded search. 


##Data source 
The whole process in this notebook depends entirely on the existence of the scored `dns_scores.csv` file, which is generated at the OA process, and scored at the Edge Investigation Notebook.
 
**Input files**
All these paths should be relative to the main OA path.       
Schema for these files can be found [here](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/oa/DNS)   

        data/dns/<date>/dns_scores.csv  

**Output files**

- threats.csv : Pipe separated file containing the comments saved by the user. This file is updated every time the user adds comments for a new threat. 
        
        Schema with zero-indexed columns:
        
        0.ip_dst : string
        0.dns_qry_name : string
        1.title: string
        2.description: string

- threat-dendro-\<anchor>.csv : Comma separated file generated in base of the results from the expanded 
search query. This file includes a list of connections involving the DNS or IP selected from the list. 
These results are limited to the day under analysis. 

        
        Schema with zero-indexed columns:

        0.total: int  
        1.dns_qry_name: string
        2.ip_dst: string
        3.sev: int


**HDFS tables consumed**  

        dns

##FUNCTIONS  

**Widget configuration**
This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`start_investigation():` - This function cleans the notebook from previous executions, then calls the data_loader() function to obtain the data and afterwards displays the corresponding widgets

`data_loader():` - This function loads the _dns_scores.csv_ file into a csv dictionary reader to find all `ip_dst` values where `ip_sev` = 1, and the `dns_qry_name` where `dns_sev` = 1, merging both 
lists into a dictionary to populate the 'Suspicious DNS' listbox, through the _fill_list()_ function.

`display_controls(ip_list):` - This function will only display the main widget box, containing:
- "Suspicious URI" listbox
- "Search" button
- Container for the "Threat summary" and "Title" text boxes
- Container for the "Top N results" HTML table

`fill_list(list_control,source):` - This function populates a listbox widget with the given data dictionary and appends an empty item at the top with the value '--Select--' (Just for visualization  sake)

`search_ip(b):` - This function is triggered by the onclick event of the "Search" button. This will get the selected value from the listbox and perform a query to the _dns_ table to retrieve all comunication involving that IP/Domain during the day with any other IPs or Domains. 
The output of the query will automatically be stored in the _threat-dendro-&lt;threat&gt;.csv_ file.  
Afterwards it will read through the output file to display the HTML table, and the results displayed will be limited by the value set in the _top_results_ variable, 
ordered by amount of connections, listing the most active connections first.
Here the "display_threat_box()" function will be invoqued. 

`display_threat_box(ip):` - Generates and displays the widgets for "Title" and "Comments" textboxes and the "Save" button on the notebook.

`save_threat_summary(b):` - This function is triggered by the _onclick_ event on the 'Save' button.
 This will take the contents of the form and create/update the _threats.csv_ file.
 
`file_is_empty(path):` - Performs a validation to check the file size to determine if it is empty.
 
`removeWidget(index):` - Javascript function that removes a specific widget from the notebook. 
#PROXY Threat Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip)

The following python modules will have to be imported for the notebook to work correctly:  

        import struct, socket
        import csv, json 
        import os 
        import datetime
        import operator
        import itertools
        import md5
        from collections import defaultdict 
        import ipywidgets as widgets # For jupyter/ipython >= 1.4
        from IPython.html import widgets
        from IPython.display import display, Javascript, clear_output


##Pre-requisites  
- Execution of the spot-oa process for Proxy
- Score a set connections at the Edge Investigation Notebook
- Correct setup of the spot.conf file. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration) 


##Additional Configuration
`top_results` - This value defines the number of rows that will be displayed onscreen after the expanded search. 
This also affects the number of IPs that will appear in the Timeline chart.

##Data
The whole process in this notebook depends entirely on the existence of the scored _proxy_scores.tsv_ file, which is generated at the OA process, and scored at the Edge Investigation Notebook.

**Input files**
Schema for these files can be found [here](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/oa/proxy)

        ~/spot-oa/data/proxy/<date>/proxy_scores.tsv  

**Output files**  
- threats.csv : Pipe separated file containing the comments saved by the user. This file is updated every time the user adds comments for a new threat. 
        
        Schema with zero-indexed columns:
        
        0.hash: string
        1.title: string
        2.description: string

- incident-progression-\<anchor hash>.json : Json file generated in base of the results from the expanded 
search. This file includes a list of all requests performed to and from the URI under analysis, as well as the request methods used and the response content type. 
These results are limited to the day under analysis. 
this file will serve as datasource for the Incident Progression chart at the storyboard.
        
        Schema with zero-indexed columns:

        {
            'fulluri':<URI under investigation>, 
            'requests': [{
                'clientip':<client IP>,
                'referer':<referer for the URI under analysis>,
                'reqmethod':<method used to connect to the URI>,
                'resconttype':<content type of the response>
                }, ...
                ],
            'referer_for':[
                         <List of unique URIs refered by the URI under investigation> 
            ]
        }

- timeline-\<anchor hash>.tsv : Tab separated file, this file lists all the client IP's that connected to the URI under investigation, including: 
the duration of the connection, response code and exact date and time of the connections.

        Schema with zero-indexed columns:
        
        0.tstart: string
        1.tend: string
        2.duration: string
        3.clientip: string
        4.respcode: string
 
- es-\<anchor hash>.tsv : (Expanded Search). Tab separated file, this is formed with the results from the Expanded Search query. Includes all connections where the investigated URI matches the `referer` or the `full_uri` columns.  


**HDFS tables consumed**

        proxy


##Functions  
**Widget configuration**

This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`start_investigation():` - This function cleans the notebook from previous executions, then calls the data_loader() function to obtain the data and afterwards displays the corresponding widgets

`data_loader():` - This function loads the source _proxy_scores.tsv_ file into a csv dictionary reader to create a list with all disctinct `full_uri` values 
where `uri_sev` = 1. This function will also read through the _threats.tsv_ file to discard all URIs that have already been investigated. 
  
`fill_list(list_control,source):` - This function populates a listbox widget with the given data list and appends an empty item at the top with the value '--Select--' (Just for visualization  sake)

`display_controls():` - This function will only display the main widget box, containing:
- "Suspicious URI" listbox
- "Search" button
- Container for the "Threat summary" and "Title" text boxes
- Container for the "Top N results" HTML table

`search_ip(b):` - This function is triggered by the _onclick_ event of the "Search" button.
This will get the selected value from the listbox and perform a query to the _proxy_ table to retrieve all comunication involving the selected URI.
Using MD5 algorythm, the URI will be hashed and use it in the name of the output files (anchor hash)
The output of the query will automatically fill the es-/<anchor hash>.tsv file. 
Afterwards it will read through the output file to display the HTML table, this will be limited to the value set in the _top_results_ variable. At the same time, four dictionaries will be filled:
- clientips
- reqmethods * 
- rescontype *
- referred *

\* These dictionaries won't be limited by the `top_results` value, but will later be used to fill the _incident-progression-\.json_ file.  
This function will also display the 'Threat summary' and 'title' textboxes, along with the 'Save' button.

`save_threat_summary(b):` - This function is triggered by the _onclick_ event on the 'Save' button.
 This will take the contents of the form and create/update the _threats.csv_ file.
 
`file_is_empty(path):` - Performs a validation to check the file size to determine if it is empty.
 
`removeWidget(index):` - Javascript function that removes a specific widget from the notebook. 
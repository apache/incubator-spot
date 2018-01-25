# DNS Threat Investigation Notebook

### Dependencies
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

## Pre-requisites  
- Execution of the spot-oa process for DNS 
- Correct installation of the UI [Read more](/ui/INSTALL.md)
- Score a set connections at the Edge Investigation Notebook 
- Correct setup the spot.conf file. [Read more](/wiki/Edit%20Solution%20Configuration) 

## Additional Configuration  
`top_results` - This value defines the number of rows that will be displayed onscreen after the expanded search. 

## Data source 
Data should exists in the following tables:
        *dns*
        *dns_threat_investigation*

**Output**
The following tables will be populated after the threat investigation process:
        *dns_storyboard*
        *dns_threat_dendro*

## FUNCTIONS  

### **Widget configuration**
This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`start_investigation():` - This function cleans the notebook from previous executions.

`data_loader():` - , then calls the *threats* query to get the `ip_dst` and `dns_qry_name` values previously scored as high risk, merging both lists into a single dictionary to populate the 'Suspicious DNS' listbox, through the _fill_list()_ function.

`display_controls(ip_list):` - This function will only display the main widget box, containing:
- "Suspicious URI" listbox
- "Search" button
- Container for the "Threat summary" and "Title" text boxes
- Container for the "Top N results" HTML table

`fill_list(list_control,source):` - This function populates a listbox widget with the given data dictionary and appends an empty item at the top with the value '--Select--' (Just for visualization sake)

`search_ip(b):` - This function is triggered by the onclick event of the "Search" button. This calls the graphql *threat / details* query to find additional connections involving the selected IP or query name. 
The results will be displayed in the HTML table, ordered by amount of connections, listing the most active connections first.
Here the "display_threat_box()" function will be invoqued. 

`display_threat_box(ip):` - Generates and displays the widgets for "Title" and "Comments" textboxes and the "Save" button on the notebook.

`save_threat_summary(b):` - This function is triggered by the _onclick_ event on the 'Save' button.
 This will take the contents of the form and create/update the _threats.csv_ file.

`removeWidget(index):` - Javascript function that removes a specific widget from the notebook. 
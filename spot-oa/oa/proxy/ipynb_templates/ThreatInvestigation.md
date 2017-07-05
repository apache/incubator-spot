# PROXY Threat Investigation Notebook

### Dependencies
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


## Pre-requisites  
- Execution of the spot-oa process for Proxy
- Correct installation of the UI [Read more](/ui/INSTALL.md)
- Score a set connections at the Edge Investigation Notebook 
- Correct setup of the spot.conf file. [Read more](/wiki/Edit%20Solution%20Configuration) 


## Additional Configuration
`top_results` - This value defines the number of rows that will be displayed onscreen after the expanded search. 
This also affects the number of IPs that will appear in the Timeline chart.

##Data source
Data should exists in the following tables:
        *proxy*
        *proxy_threat_investigation*


**Output**  
The following tables will be populated after the threat investigation process:
        *proxy_storyboard*
        *proxy_timeline*

The following files will be created and stored in HDFS.

        incident-progression-\<anchor hash>.json

## Functions  
**Widget configuration**

This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`start_investigation():` - This function cleans the notebook from previous executions, then calls the data_loader() function to obtain the data and afterwards displays the corresponding widgets

`data_loader():` - This function lcalls the *threats* query to get the source and destination IP's previously scored as high risk to create a list with all distinct `full_uri` values.

`fill_list(list_control,source):` - This function populates a listbox widget with the given data list and appends an empty item at the top with the value '--Select--' (Just for visualization  sake)

`display_controls():` - This function will only display the main widget box, containing:
- "Suspicious URI" listbox
- "Search" button
- Container for the "Threat summary" and "Title" text boxes
- Container for the "Top N results" HTML table

`search_ip(b):` - This function is triggered by the _onclick_ event of the "Search" button.
This calls the graphql *threat / details* query to find additional connections involving the selected full uri. 
Afterwards it will read through the output file to display the HTML table, this will be limited to the value set in the _top_results_ variable. At the same time, four dictionaries will be filled:
- clientips
- reqmethods * 
- rescontype *
- referred *

\* These dictionaries won't be limited by the `top_results` value, but will later be used to fill the _incident-progression-\.json_ file.  
This function will also display the 'Threat summary' and 'title' textboxes, along with the 'Save' button.

`save_threat_summary(b):` - This function is triggered by the _onclick_ event on the 'Save' button.
Removes the widgets and cleans the notebook from previous executions, removes the selected value from the listbox widget and executes the *createStoryboard* mutation to save the data for the storyboard.
 
`removeWidget(index):` - Javascript function that removes a specific widget from the notebook. 
#Flow Edge Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip)
- [pandas](http://pandas.pydata.org/)

The following python modules will be imported for the notebook to work correctly:    

        import datetime
        import struct, socket
        import shutil
        import numpy as np
        import pandas as pd
        import linecache, bisect
        import csv, json
        import operator
        import os, time, subprocess 
        from collections import OrderedDict
        import ipywidgets #For jupyter/ipython >= 1.4  
        from IPython.html import widgets #For jupyter/ipython < 1.4  
        from IPython.display import display, Javascript, clear_output   


###Pre-requisites
- Execute hdfs_setup.sh script to create OA tables and setup permissions
- Correct setup the spot.conf file. [Read more](http://spot.incubator.apache.org/doc/#configuration)
- Execution of the spot-oa process for Flow
- Correct installation of the UI [Read more](/ui/INSTALL.md)


##Additional Configuration inside the notebook
`coff` - This value defines the max number of records used to populate the listbox widgets. This value is set by default on 250.
`nwloc` - File name of the custom network context.  


###Data source
The whole process in this notebook depends entirely on the existence of `flow_scores` table in the database.  
The data is manipulated through the graphql api also included in the repository.


**Input**  
The data to be processed should be stored in the following tables:

        flow_scores
        flow


**Output**
The following tables will be populated after the scoring process:
        flow_threat_investigation


##Functions 
 
`data_loader():` - This function calls the graphql api query *suspicious* to list all suspicious unscored connections, creating separated lists for:
- Source IP
- Destination IP
- Source port
- Destination port  

Each of these lists will populate a listbox widget and then they will be displayed  to help the user narrow down the selection and score more specific connections by combining the values from the lists.   

`apply_css_to_select(select)` - Defines a set of css styles that can be applied to any listbox widget. 

`update_sconnects(b):` -   
This function is executed on the onclick event of the ‘Assign’ button. The notebook will first try to get the value from the 'Quick IP Scoring' textbox ignoring the selections from the listboxes; in case the textbox is empty, it will then
 get the selected values from each of the listboxes and append them to a temporary list. 

`savesort(b):` - This event is triggered by the 'Save' button, and executes javascript functions to refresh the data on all the panels in Suspicious Connects.  
This function calls the *score* mutation which updates the score for the selected values in the database.

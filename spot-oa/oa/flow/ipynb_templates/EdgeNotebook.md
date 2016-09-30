#Flow Edge Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip)
- [pandas](http://pandas.pydata.org/)

The following python modules will be imported for the notebook to work correctly:    

        import struct, socket
        import shutil
        import numpy as np
        import pandas as pd
        import linecache, bisect
        import csv
        import operator
        import os, time, subprocess
        import ipywidgets #For jupyter/ipython >= 1.4  
        from IPython.html import widgets #For jupyter/ipython < 1.4  
        from IPython.display import display, HTML, clear_output, Javascript   


###Pre-requisites
- Execution of the oni-oa process for Flow
- Correct setup the duxbay.conf file. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration)
- Have a public key created between the current UI node and the ML node. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Configure%20User%20Accounts#configure-user-accounts)


##Additional Configuration
`coff` - This value defines the max number of records used to populate the listbox widgets. This value is set by default on 250.
`nwloc` - File name of the custom network context.  

###Data source
The whole process in this notebook depends entirely on the existence of `flow_scores.csv` file, which is generated at the OA process at the path.  
The data is directly manipulated on the .csv files, so a `flow_scores_bu.csv` on the same path is created as a backup to allow the user to restore the original data at any point, 
and this can be performed executing the last cell on the notebook with the following command:  

        !cp $sconnectbu $sconnect


**Input files**  
All these paths should be relative to the main OA path.    
Schema for these files can be found [here](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/oa/flow)

        data/flow/<date>/flow_scores.csv
        data/flow/<date>/flow_scores_bu.csv

**Temporary Files**

        data/flow/<date>/flow_scores.csv.tmp

**Output files**

        data/flow/<date>/flow_scores.csv (Updated with severity values)
        data/flow/<date>/flow_scores_fb.csv (File with scored connections that will be used for ML feedback)

##Functions 
 
`displaythis():` - This function reads the `flow_scores.csv` file to list all suspicious unscored connections, creating separated lists for:
- Source IP
- Destination IP
- Source port
- Destination port  

Each of these lists will populate a listbox widget and then they will be displayed  to help the user narrow down the selection and score more specific connections by combining the values from the lists.   

`apply_css_to_select(select)` - Defines a set of css styles that can be applied to any listbox widget. 

`update_sconnects(list_control,source):` -   
This function is executed on the onclick event of the ‘Assign’ button. The notebook will first try to get the value from the 'Quick IP Scoring' textbox ignoring the selections from the listboxes; in case the textbox is empty, it will then
 get the selected values from each of the listboxes to look them up in the `flow_scores.csv` file. 
A binary search on the file is then performed:  
- The value in the 'Quick IP Scoring' textbox, will be compared against the `ip_src` and `ip_dst` columns; if either column is a match, the `sev` column will be updated with the value selected from the radiobutton list. 
- The column `srcIP` will be compared against the 'Source IP' selected value.  
- The column `dstIP` will be compared against the 'Dest IP' selected value. 
- The column `sport` will be compared against the 'Src Port' selected value.
- The column `dport` will be compared against the 'Dst Port' selected value.  

Every row will be then appended to the `flow_scores.csv.tmp` file, which will replace the original `flow_scores.csv` at the end of the process.
The scored rows will also be appended to the `flow_scores_fb.csv` file, which will later be used for the ML feedback.   

`set_rules():` - Predefined function where the user can define custom rules to be initally applied to the dataset. By default this function is commented out.

`apply_rules(rops,rvals,risk):` - This function applies the rules defined by `set_rules()` and updates the `flow_scores.csv` file following a similar process to the `update_sconnects()` function. By default this function is commented out.

`attack_heuristics():` - This function is executed at the start, and loads the data from `flow_scores.csv` into a pandas dataframe grouped by `srcIp` column,
to then print only those IP's that connect to more than 20 other different IP's. By default this function is commented out.

`savesort(b):` - This event is triggered by the 'Save' button, and executes javascript functions to refresh the data on all the panels in Suspicious Connects.  
This function also reorders the _flow_scores.csv_ file by moving all scored connections to the end of the file and sorting the remaining connections by `lda_score` column.    
Finally, removes the widget panel and reloads it again to update the results, removing the need of a manual refresh, and calls the `ml_feedback():` function.    

`ml_feedback():` - A shell script is executed, transferring thru secure copy the _flow_scores_fb.csv_ file into ML Master node, where the destination path is defined at the duxbay.conf file.
   
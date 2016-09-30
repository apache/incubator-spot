#DNS Edge Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip) 

The following python modules will be imported for the notebook to work correctly:    

        import urllib2  
        import json  
        import os  
        import csv  
        import ipywidgets #For jupyter/ipython >= 1.4  
        from IPython.html import widgets # For jupyter/ipython < 1.4  
        from IPython.display import display, HTML, clear_output, Javascript   
        import datetime  
        import subprocess 


###Pre-requisites
- Execution of the oni-oa process for DNS
- Correct setup the duxbay.conf file. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration) 
- Have a public key authentication between the current UI node and the ML node. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Configure%20User%20Accounts#configure-user-accounts)


##Data source

The whole process in this notebook depends entirely on the existence of `dns_scores.csv` file, which is generated at the OA process.  
The data is directly manipulated on the .csv files, so a `dns_scores_bu.csv` is created as a backup to allow the user to restore the original data at any point, 
and this can be performed executing the last cell on the notebook with the following command:  

        !cp $sconnectbu $sconnect


**Input files**  
All these paths should be relative to the main OA path.    
Schema for these files can be found [here](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/oa/dns)

        data/dns/<date>/dns_scores.csv  
        data/dns/<date>/dns_scores_bu.csv

**Temporary Files**

        data/dns/<date>/score_tmp.csv

**Output files**

        data/dns/<date>/dns_scores.csv  (Updated with severity values)
        data/dns/<date>/dns_scores_fb.csv (File with scored connections that will be used for ML feedback)

###Functions
**Widget configuration**
This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`data_loader():` - This function loads the source file into a csv dictionary reader with all suspicious unscored connections, creating separated lists for 
the 'client_ip' and 'dns_qry_name'.
 Also displays the widgets for the listboxes, textbox, radiobutton list and the 'Score' and 'Save' buttons.  
  
`fill_list(list_control,source):` - This function loads the given dictionary into a listbox and appends an empty item at the top with the value '--Select--' (Just for design sake)

` assign_score(b):` - This function is executed on the onclick event of the ‘Score’ button. The system will first try to get the value from the 'Quick search' textbox ignoring the selections from the listboxes; in case the textbox is empty, it will then
 get the selected values from the 'Client IP' and 'Query' listboxes to then search through the dns_scores.csv file to find matching values. 
A linear search on the file is then performed:  
The value in the 'Quick Scoring' textbox, will be compared against the `dns_qry_name` column. Partial matches will be considered as a positive match and the `dns_sev` column will be updated to the value selected from the radiobutton list.   
The column `ip_dst` will be compared against the 'Client IP' selected value; if a match is found, the `ip_sev` column will be updated to the value selected from the radiobutton list.   
The column `dns_qry_name` will be compared against the 'Query' selected value; if a match is found, the `dns_sev` column will be updated to the value selected from the radiobutton list.     
Every row will be appended to the `dns_scores_tmp.csv` file. This file will replace the original `dns_scores.csv` at the end of the process.  

Only the scored rows will also be appended to the `dns_scores_fb.csv` file, which will later be used for the ML feedback.

`save(b):` - This event is triggered by the 'Save' button, and executes javascript functions to refresh the data on all the panels in Suspicious Connects. Since the data source file has been updated, the scored connections will be removed from all
the panels, since those panels will only display connections where the `dns_sev` value is zero.
This function also removes the widget panel and reloads it again to update the results, removing the need of a manual refresh, and calls the `ml_feedback():` function.

`ml_feedback():` - A shell script is executed, transferring thru secure copy the _proxy_scores_fb.csv_ file into ML Master node, where the destination path is defined at the duxbay.conf file.

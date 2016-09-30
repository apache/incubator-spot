#PROXY Edge Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip)

The following python modules will be imported for the notebook to work correctly:    

        import urllib2
        import json
        import os
        import csv    
        import datetime  
        import subprocess 
        import ipywidgets #For jupyter/ipython >= 1.4  
        from IPython.html import widgets # For jupyter/ipython < 1.4  
        from IPython.display import display, HTML, clear_output, Javascript


###Pre-requisites
- Execution of the oni-oa process for Proxy
- Correct setup the duxbay.conf file [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration)
- Have a public key created between the current UI node and the ML node. [Read more](https://github.com/Open-Network-Insight/open-network-insight/wiki/Configure%20User%20Accounts#configure-user-accounts)


###Data
The whole process in this notebook depends entirely on the existence of `proxy_scores.tsv` file, which is generated at the OA process.  
The data is directly manipulated on the .tsv files, so a `proxy_scores_bu.tsv` is created as a backup to allow the user to restore the original data at any point, 
and this can be performed executing the last cell on the notebook with the following command.

        !cp $sconnectbu $sconnect


**Input files**
All these paths should be relative to the main OA path.    
Schema for these files can be found [here](https://github.com/Open-Network-Insight/oni-oa/tree/1.1/oa/proxy)

        data/proxy/<date>/proxy_scores.tsv  
        data/proxy/<date>/proxy_scores_bu.tsv

**Temporary Files**

        data/proxy/<date>/proxy_scores_tmp.tsv

**Output files**

        data/proxy/<date>/proxy_scores.tsv (Updated with severity values)
        data/proxy/<date>/proxy_scores_fb.csv (File with scored connections that will be used for ML feedback)


###Functions
**Widget configuration**
This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`data_loader():` - This function loads the source file into a csv dictionary reader to create a list with all disctinct full_uri values. 
  
`fill_list(list_control,source):` - This function loads the given dictionary into a listbox and appends an empty item at the top with the value '--Select--' (Just for design sake)
   
` assign_score(b):` - This event is executed when the user clicks the 'Score' button. 
If the 'Quick scoring' textbox is not empty, the notebook will read that value and ignore any selection made in the listbox, otherwise the sought value will be obtained from the listbox.
A linear search will be performed in the `proxy_scores.tsv` file to find all `full_uri` values matching the sought .
In every matching row found, the `uri_sev` value will be updated according to the 'Rating' value selected in the radio button list. 
All of the rows will then be appended to the `proxy_scores_tmp.tsv` file. At the end of this process, this file will replace the original `proxy_scores.tsv`.  

Only the scored rows will also be appended to the `proxy_scores_fb.csv` file, which will later be used for the ML feedback.

`save(b):` -This event is triggered by the 'Save' button, first it will remove the widget area and call the `load_data()` function to start the loading process again, this will 
refresh the listbox removing all scored URIs.
A javascript function is also executed to refresh the other panels in the suspicious connects page removing the need of a manual refresh.
Afterwards the `ml_feedback()` function will be invoqued. 

`ml_feedback():` - A shell script is executed, transferring thru secure copy the _proxy_scores_fb.csv_ file into ML Master node, where the destination path is defined at the duxbay.conf file.
   
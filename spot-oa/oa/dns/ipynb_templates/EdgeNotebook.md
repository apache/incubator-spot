#DNS Edge Investigation Notebook

###Dependencies
- [iPython == 3.2.1](https://ipython.org/ipython-doc/3/index.html)
- [Python 2.7.6](https://www.python.org/download/releases/2.7.6/)
- [ipywidgets 5.1.1](https://ipywidgets.readthedocs.io/en/latest/user_install.html#with-pip) 

The following python modules will be imported for the notebook to work correctly:    

        import urllib2
        import json
        import os 
        import datetime  
        import subprocess 
        import ipywidgets #For jupyter/ipython >= 1.4  
        from IPython.html import widgets # For jupyter/ipython < 1.4  
        from IPython.display import display, HTML, clear_output, Javascript   
        

###Pre-requisites
- Execute hdfs_setup.sh script to create OA tables and setup permissions
- Correct setup the spot.conf file. [Read more](http://spot.incubator.apache.org/doc/#configuration)
- Execution of the spot-oa process for Flow
- Correct installation of the UI [Read more](/ui/INSTALL.md)


##Data source
The whole process in this notebook depends entirely on the existence of `dns_scores` table in the database.  
The data is manipulated through the graphql api also included in the repository.


**Input files**  
The data to be processed should be stored in the following tables:

        dns_scores
        dns


**Output**
The following tables will be populated after the scoring process:
        dns_threat_investigation


###Functions
**Widget configuration**
This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`data_loader():` - This function calls the graphql api query *suspicious* to list all suspicious unscored connections, creating separated lists for 
the 'client_ip' and 'dns_qry_name'.
 Also displays the widgets for the listboxes, textbox, radiobutton list and the 'Score' and 'Save' buttons.  
  
`fill_list(list_control,source):` - This function loads the given dictionary into a listbox widget

` assign_score(b):` - This function is executed on the onclick event of the ‘Score’ button. The system will first try to get the value from the 'Quick search' textbox ignoring the selections from the listboxes; in case the textbox is empty, it will then get the selected values from the 'Client IP' and 'Query' listboxes to append them to a temporary list. 

`save(b):` - This event is triggered by the 'Save' button, and executes javascript functions to refresh the data on all the panels in Suspicious Connects. This function calls the *score* mutation which updates the score for the selected values in the database.

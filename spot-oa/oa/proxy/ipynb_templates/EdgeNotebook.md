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
- Execute hdfs_setup.sh script to create OA tables and setup permissions
- Correct setup the spot.conf file [Read more](/wiki/Edit%20Solution%20Configuration)
- Execution of the spot-oa process for Proxy
- Correct installation of the UI [Read more](/ui/INSTALL.md)


###Data source 
The whole process in this notebook depends entirely on the existence of `proxy_scores` table in the database, which is generated at the OA process.  
The data is manipulated through the graphql api also included in the repository.

**Input**  
The data to be processed should be stored in the following tables:

        proxy_scores
        proxy

**Output**
The following tables will be populated after the scoring process:
        proxy_threat_investigation


###Functions
**Widget configuration**
This is not a function, but more like global code to set up styles and widgets to format the output of the notebook. 

`data_loader():` - - This function calls the graphql api query *suspicious* to list all suspicious unscored connections.
  
`fill_list(list_control,source):` - This function loads the given dictionary into a listbox and appends an empty item at the top with the value '--Select--' (Just for design sake)
   
` assign_score(b):` - This event is executed when the user clicks the 'Score' button. 
If the 'Quick scoring' textbox is not empty, the notebook will read that value and ignore any selection made in the listbox, otherwise the sought value will be obtained from the listbox and will append each value to a temporary list. 

`save(b):` -This event is triggered by the 'Save' button, first it will remove the widget area and call the `load_data()` function to start the loading process again, this will refresh the listbox removing all scored URIs.
This function calls the *score* mutation which updates the score for the selected values in the database.
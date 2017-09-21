# Apache Spot Operational Analytics

Apache Spot Operational Analytics (OA) is a set of python modules and utilities with routines to extract and transform data, loading the results into output files.
OA represents the last step before users can score connections and analyze data in the UI.

The three supported data types are Flow, DNS and Proxy. For more information about the type of information and insights
that can be found for each data source please visit the official Apache Spot [docs](http://spot.apache.org/doc/).

OA scripts are very similar for the different data types supported however the code is divided into 3 
main modules due to differences in the data model and what context information is required for each data type.


## Folder Structure

        components  ->      Set of utilities prepared to provide context to raw data and 
                            connect to impala or hive data base.
        dns         ->      DNS OA module to process and transform DNS spot-ml results returning DNS
                            data to be presented in the UI.
        flow        ->      Flow OA module to process and transform Flow spot-ml results returning Flow
                            data to be presented in the UI.
        proxy       ->      Proxy OA module to process and transform Proxy spot-ml results returning Proxy
                            data to be presented in the UI.
        start_oa.py ->      Main Operational Analytics script. Given a set of parameters, this script can
                            process and transform data for any of the supported data types.
        utils.py    ->      Miscellaneous utilities required by start_oa.py to load data, load logger, etc.
        
More information about oa/flow in [README.md](/spot-oa/oa/flow)

More information about oa/dns in [README.md](/spot-oa/oa/dns)

More information about oa/proxy in [README.md](/spot-oa/oa/proxy)

More information about oa/components in [README.md](/spot-oa/oa/components)

## Operational Analytics prerequisites

In order to execute this process there are a few prerequisites:

 1. Python 2.7.
 2.  Components configuration. To find about how to configure each of the extra components included in this project
        visit oa/components/[README.md](/spot-oa/oa/components).
        These components are required to add context or extract additional information that is going to complement your 
        original data. Each of these components are independent from each other. Based on the data type some components are 
        required or not.
 3. spot-ml results. Operational Analytics works and transforms Machine Learning results. The implementation of Machine Learning
        in this project is through [spot-ml](/spot-ml/). Although the Operational Analytics
         is prepared to read csv files and there is not a direct dependency between spot-oa and spot-ml, it's highly recommended
         to have these two pieces set up together.
         If users want to implement their own machine learning piece to detect suspicious connections they need to refer
         to each data type module to know more about input format and schema.
 4. spot-setup project installed. Spot-setup project contains scripts to install hive database and the main configuration
        file.
 

## Operational Analytics installation and usage
#### Installation
 
 OA installation consists of the configuration of extra modules or components and creation of a set of files.
 Depending on the data type that is going to be processed some components are required and other components are not.
 If users are planning to analyze the three data types supported (Flow, DNS and Proxy) then all components should be configured.

 1. Clone project spot-oa from [github repository](/spot-oa) in your /home/\<user> directory 
    or if it was already cloned go to spot-oa folder.
    
 2. Add context files. Context files should go into spot-oa/context folder and they should contain network and geo localization context.  
 For more information on context files go to [spot-oa/context/README.md](/spot-oa/README.md) 
    
    2.1. Add a file ipranges.csv: Ip ranges file is used by OA when running data type Flow. 
         It should contain a list of ip ranges and the label for the given range, example:
            
            10.0.0.1,10.255.255.255,Internal
            
    2.2. Add a file iplocs.csv: Ip localization file used by OA when running data type Flow. 
         Create a csv file with ip ranges in integer format and give the coordinates for each range.
     
    2.3. Add a file networkcontext.csv: Ip names file is used by OA when running data type DNS and Proxy. This file
         should contains two columns, one for Ip the other for the name, example: 
    
            10.192.180.150, AnyMachine
            10.192.1.1,     MySystem
            
            
 3. The spot-setup project contains scripts to install the hive database and also includes the main configuration file for this tool.
     The main file is called spot.conf which contains different variables that the user can set up to customize their installation. Some variables
     must be updated in order to have spot-ml and spot-oa working.
     
     To run the OA process it's required to install spot-setup. If it's already installed just make sure the following configuration are set up in spot.conf file.
    
        LUSER: represents the home folder for the user in the Machine Learning node. It's used to know where to return feedback.
        HUSER: represents the HDFS folder. It's used to know from where to get Machine Learning results.
        IMPALA_DEM: represents the node running Impala daemon. It's needed to execute Impala queries in the OA process.
        DBNAME: Hive database, the name is required for OA to execute queries against this database.
        LPATH: represents the local path where the feedback is going to be sent, it actually works with LUSER.
    
 4. Configure components. Components are python modules included in this project that add context and details to the data 
    being analyzed. There are five components and while not all components are required to every data type, it's recommended to
    configure all of them in case new data types are analyzed in the future.
    For more details about how to configure each component go to [spot-oa/oa/components/README.md](/spot-oa/oa/components/README.md).
    
 #### Usage
 
 OA process is triggered with the execution of start_oa.py. This Python script will execute the OA process
  for only one data type (Flow, DNS or Proxy) at a time. If users need to process multiple data types at the same time, multiple
  instances of the same script needs to be executed. 
  
  To execute Operational Analytics in Apache Spot go to folder spot-oa/oa and execute the following command:
        
        [solution-user@edge-server] python2.7 start_oa.py -d YYYYMMDD -t [flow, dns, proxy] -l <integer>
    
  Example
  
        [oniuser@node03] python2.7 start_oa.py -d 20160827 -t flow -l 3000
        
  Parameters
 
        -d Date for the machine learning results that are going to be processed. Format should be YYYY (year in 4 digits)
           MM for month (2 digits) and DD for day (2 digits).
        -t Data type to be analyzed. Accepted parameters are flow, dns and proxy. Each one of these values will trigger the 
           OA process for the corresponding data type.
        -l Data limit. Usually ML results contains thousands of records. With "Data limit" OA will process top K results. 

 The execution time of OA varies based on the number of records being processed and the data type.
 Depending on the number of records being processed and the data type, OA can take long or short time to execute.
 When the process completes you can go to spot-oa/data/\<data type> folder and check the results.
 
 For more information on each data type and output files go to each [spot-oa/oa/flow](/spot-oa/oa/dns), 
 [spot-oa/oa/dns](/spot-oa/oa/flow) or [spot-oa/oa/proxy](/spot-oa/oa/proxy)
                        
                            




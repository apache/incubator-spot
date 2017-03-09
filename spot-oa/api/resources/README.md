

# API Resources 


API Resources are the backend methods used by GraphQL to perform CRUD operations to Apache Spot (incubating) like score connections, performa a threat investigation, generate a storyboard, etc.

**Classes:**

* Resources/flow.py
* Resources/dns
* Resources/proxy
* Resources/configurator
* Resources/hdfs_client
* Resources/impala_engine.py



## **Configuration Required (spot.conf):**

API Resources use [WebHDFS REST API] (https://hadoop.apache.org/docs/r1.0.4/webhdfs.html) and Impala API, based on that some new configuration is required.

**_Keys in HDFS section:_**

* **NAME_NODE:** this key is required to setup the name node (full DNS domain or IP) to get connected to WebHDFS REST API.
* **WEB_PORT:** Web port to WebHDFS REST API (default=50070)

**_Keys in Impala section:_**

* **IMPALA_DEM:** This key has been there since the last release ,but now that we spot uses an API to get connected you need to either put the impala daemon full DNS or Server IP.
* **IMPALA_PORT:** Port on which HiveServer2 client requests are served by Impala Daemons.

## **Prerequisites:**

#### Python:
* setuptools
* thrift==0.9.3
* impyla
* hdfs

**NOTE: all this requirements are already part of requiremets.txt file, you dont need to install the python prerequisites manually.**

#### Hadoop:

* Impala.
* WebHDFS REST API.







# API Resources 


API Resources are the backend methods used by GraphQL to perform CRUD operations to Apache Spot (incubating) like score connections, perform threat investigation, generate a storyboard, etc.

**Classes:**

* resources/flow.py
* resources/dns
* resources/proxy
* resources/configurator
* resources/hdfs_client
* resources/impala_engine.py



## **Configuration Required (spot.conf):**
API Resources use [WebHDFS REST API] (https://hadoop.apache.org/docs/r1.0.4/webhdfs.html), based on that spot.conf has been updated to include new KEYS (o variables o entries)

**_Keys in HDFS section:_**

* **NAME_NODE:** this key is required to setup the name node (full DNS domain or IP) to get connected to WebHDFS REST API.
* **WEB_PORT:** Web port for WebHDFS REST API (default=50070)

**_Keys in Impala section:_**

* **IMPALA_DEM:** This key was part of previous release. Now that spot uses an API to connect, you need to either put the impala daemon full DNS or Server IP Address.
* **IMPALA_PORT:** Port on which HiveServer2 client requests are served by Impala Daemons.

## **Prerequisites:**

#### Python:
* setuptools
* thrift==0.9.3
* impyla
* hdfs

**NOTE:** all these requirements are already part of requirements.txt file, you don't need to install the python prerequisites manually. For more information go to [install python requirements](../../README.md)

#### Hadoop:

* Impala.
* WebHDFS REST API.





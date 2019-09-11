Spot Ingest Framework
======
Ingest data is captured or transferred into the Hadoop cluster, where they are transformed and loaded into solution data stores.

![Ingest Framework](../docs/SPOT_Ingest_Framework1_1.png)

## Getting Started

### Prerequisites
* [spot-setup](../spot-setup)
* [kafka-python](https://github.com/dpkp/kafka-python)
* [spot-nfdump](https://github.com/Open-Network-Insight/spot-nfdump)
* tshark
  * [download](https://www.wireshark.org/download.html)
  * [how to install](https://github.com/Open-Network-Insight/open-network-insight/wiki/Install%20Ingest%20Prerequisites)
* [watchdog](http://pythonhosted.org/watchdog/)
* [spark-streaming-kafka-0-8-assembly_2.11](http://search.maven.org/#search|ga|1|a%3A%22spark-streaming-kafka-0-8-assembly_2.11%22%20AND%20v%3A%222.0.0%22)	 
* Ingest user with sudo privileges (i.e. spot). This user will execute all the processes in the Ingest Framework also this user needs to have access to hdfs solution path (i.e. /user/spot/).

### Install
1. Install Python dependencies `pip install -r requirements.txt` 

Optional:
2. the sasl python package requires the following:
   * Centos: `yum install cyrus-sasl-devel`
   * Debian/Ubuntu: `apt-get install libsasl2-dev`
3. install Python dependencies for Kerberos `pip install -r kerberos-requirements.txt`

### Configure Kafka
**Adding Kafka Service:**

Ingest framework needs Kafka to work in real-time streaming. Add Kafka service using Cloudera Manager. If you are using a Cloudera Manager version < 5.4.1 you will need to add the kafka parcel manually.

Ingest module uses a default configuration for the message size (900000 bytes), if you modify this size in the ingest configuration file you will need to modify the following configuration properties in kafka:

* message.max.bytes
* replica.fetch.max.bytes

### Spark-Streaming Kafka support.
Download the following jar file: [spark-streaming-kafka-0-8-assembly_2.11](http://search.maven.org/#search|ga|1|a%3A%22spark-streaming-kafka-0-8-assembly_2.11%22%20AND%20v%3A%222.0.0%22). This jar adds support for Spark Streaming + Kafka and needs to be downloaded on the following path : **spot-ingest/common** (with the same name)

### Getting Started

**Required Roles**

The following roles are required in all the nodes where the Ingest Framework will be running.
* [HDFS gateway (i.e. Edge Server)](https://hadoop.apache.org/docs/r2.4.1/hadoop-project-dist/hadoop-hdfs/HdfsNfsGateway.html)
* Kafka Broker

**Ingest Configuration:**

The file **ingest_conf.json** contains all the required configuration to start the ingest module
*  **dbname:** Name of HIVE database where all the ingested data will be stored in avro-parquet format.
*  **hdfs_app_path:** Application path in HDFS where the pipelines will be stored (i.e /user/_application_user_/). 
*  **kafka:** Kafka and Zookeeper server information required to create/listen topics and partitions.
*  **collector_processes:** Ingest framework uses  multiprocessing to collect files (different from workers), this configuration key defines the numbers of collector processes to use.
*  **spark-streaming:** Proxy pipeline uses spark streaming to ingest data, this configuration is required to setup the spark application for more details please check : [how to configure spark](https://github.com/Open-Network-Insight/open-network-insight/blob/spot/spot-ml/SPARKCONF.md)
*  **pipelines:** In this section you can add multiple configurations for either the same pipeline or different pipelines. The configuration name must be lowercase without spaces (i.e. flow_internals).

**Configuration example:**

     "dbname" : "database name",
     "hdfs_app_path" : "hdfs application path",
     "collector_processes":5,
     "ingestion_interval":1,
     "spark-streaming":{
                "driver_memory":"",
                "spark_exec":"",
                "spark_executor_memory":"",
                "spark_executor_cores":"",
                "spark_batch_size":""
      },
      "kafka":{
            "kafka_server":"kafka ip",
            "kafka_port":"kafka port",
            "zookeper_server":"zk ip",
            "zookeper_port":"zk port",
            "message_size":900000
        },
      "pipelines":{
      
         "flow_internals":{
              "type":"flow",
              "collector_path":"/path_to_flow_collector",
              "local_staging":"/tmp/",
              "process_opt":""
          },
          "flow_externals":{
              "type":"flow",
              "collector_path":"/path_to_flow_collector",
              "local_staging":"/tmp/",
              "process_opt":""
          },
          "dns_server_1":{
              "type":"dns",
              "collector_path":"/path_to_dns_collector",
              "local_staging":"/tmp/",
              "pkt_num":"650000",
              "pcap_split_staging":"/tmp",    
              "process_opt":"-E separator=, -E header=y -E occurrence=f -T fields -e frame.time -e frame.time_epoch -e frame.len -e ip.src -e ip.dst -e dns.resp.name -e dns.resp.type -e dns.resp.class -e dns.flags.rcode -e dns.a 'dns.flags.response == 1'"
          }

**Starting the Ingest**

_Running in a Standalone Mode:_

    bash start_standalone_ingest.sh "pipeline_configuration" "num of workers"
    
Following the previous configuration example starting ingest module in a stand alone mode will look like:

    bash start_standalone_ingest.sh flow_internals 4

_Running in a Cluster Mode:_

**Running Master:** Master needs to be run in the same server where the collector path is.

    python master_collector.py -t "pipeline_configuration" -w "number of workers"
    
**Running Workers:** Worker needs to be executed in a server where the required processing program installed (i.e. nfdump), also the worker needs to be identified with a specific id, this id needs to start with 0.

example:

1. worker_0,  id = 0 
2. worker_1 , id = 1

This "id" is required to attach the worker with the kafka partition.

    python worker.py -t "pipeline_configuration" -i "id of the worker (starts with 0)" --topic "my_topic"


## Ingestion using Spark Streaming
A new functionality is now available where the Distributed Collector transmits to Kafka cluster already processed files in a comma-separated (CSV) output format. Each row of the CSV corresponds to a table row in the Hive database. As a result, the Streaming Listener consumes batches of CSV messages from the Kafka cluster and registers them in Hive database.

**Distributed Collector**
<br />
The role of the Distributed Collector is similar, as it processes the data before transmission. Distributed Collector tracks a directory backwards for newly created files. When a file is detected, it converts it into CSV format and stores the output in the local staging area. Following to that, reads the CSV file line-by-line and creates smaller chunks of bytes. The size of each chunk depends on the maximum request size allowed by Kafka. Finally, it serializes each chunk into an Avro-encoded format and publishes them to Kafka cluster.<br />
Due to its architecture, Distributed Collector can run **on an edge node** of the Big Data infrastructure as well as **on a remote host** (proxy server, vNSF, etc).<br />
In addition, option `--skip-conversion` has been added. When this option is enabled, Distributed Collector expects already processed files in the CSV format. Hence, when it detects one, it does not apply any transformation; just splits it into chunks and transmits to the Kafka cluster.<br />
This option is also useful, when a segment failed to transmit to the Kafka cluster. By default, Distributed Collector stores the failed segment in CSV format under the local staging area. Then, using `--skip-conversion` option could be reloaded and sent to the Kafka cluster.<br />
Distributed Collector publishes to Apache Kafka only the CSV-converted file, and not the original one. The binary file remains to the local filesystem of the current host.

**Streaming Listener**
<br />
In contrary, Streaming Listener can only run on the central infrastructure. Its ability is to listen to a specific Kafka topic and consumes incoming messages. Streaming data is divided into batches (according to a time interval). These batches are deserialized by the Listener, according to the supported Avro schema, parsed and registered in the corresponding table of Hive.


### Configuration
Both Distributed Collector and Streaming Listener use the same configuration file as the original Spot Ingest flavour. The only addition is under `kafka` section:

    "kafka":{
        "kafka_server":"kafka ip",
        "kafka_port":"kafka port",
        "zookeper_server":"zk ip",
        "zookeper_port":"zk port",
        "message_size":900000,
        "max_request_size": 1048576
    },

The `max_request_size` defines the maximum size of the chunks that are sent to Kafka cluster. If it is not set, then the default value that will be used is 1MB.

Moreover, the list of the supported files must be provided as regular expressions. For instance, to support a filename like `nfcapd.20171103140000`, you have to set:

    "supported_files" :["nfcapd.*"],
or

    "supported_files": ["nfcapd.[0-9]{14}"],


### Installation
Installation requires a user with `sudo` privileges. Enter `spot-ingest` directory and run:
<br />`./install_DC.sh`

If you prefer to install the Distributed Collector on a remote host, just copy `spot-ingest` folder to the remote host and run the above installation file. It is important to mention that the remote host should have access to the Kafka cluster to work properly.


### Getting Started

**Start Distributed Collector**<br />
Enable the virtual environment
<br />`source venv/bin/activate`

and check the usage message of the Distributed Collector.

    python collector.py --help
    usage: Distributed Collector Daemon of Apache Spot [-h] [-c] [-l]
                                                       [--skip-conversion] --topic
                                                       TOPIC -t {dns,flow,proxy}

    optional arguments:
      -h, --help            show this help message and exit
      -c , --config-file    path of configuration file
      -l , --log-level      determine the level of the logger
      --skip-conversion     no transformation will be applied to the data; useful
                            for importing CSV files

    mandatory arguments:
      --topic TOPIC         name of topic where the messages will be published
      -t {dns,flow,proxy}, --type {dns,flow,proxy}
                            type of data that will be collected

    END

By default, it loads `ingest_conf.json` file. Using `-c , --config-file` option you can override it and use another.

Distributed Collector does not create a new topic, so you have to pass an existing one.

To start Distributed Collector:<br />
`python collector.py -t "pipeline_configuration" --topic "my_topic"`

Some examples are given below:<br />
1. `python collector.py -t flow --topic SPOT-INGEST-TEST-TOPIC`<br />
2. `python collector.py -t flow --topic SPOT-INGEST-TEST-TOPIC --config-file /tmp/another_ingest_conf.json`<br />
3. `python collector.py -t proxy --topic SPOT-PROXY-TOPIC --log-level DEBUG`<br />

**Start Streaming Listener**<br />
Print usage message and check the available options.

    python start_listener.py --help
    usage: Start Spark Job for Streaming Listener Daemon [-h] [-c] [-d] [-g] [-m]
                                                     [-n] [-r] -p PARTITIONS
                                                     -t {dns,flow,proxy}
                                                     --topic TOPIC

    optional arguments:
      -h, --help            show this help message and exit
      -c , --config-file    path of configuration file
      -d , --deploy-mode    Whether to launch the driver program locally
                            ("client") or on one of the worker machines inside the
                            cluster ("cluster")
      -g , --group-id       name of the consumer group to join for dynamic
                            partition assignment
      -l , --log-level      determine the level of the logger
      -m , --master         spark://host:port, mesos://host:port, yarn, or local
      -n , --app-name       name of the Spark Job to display on the cluster web UI
      -r , --redirect-spark-logs 
                            redirect output of spark to specific file

    mandatory arguments:
      -p PARTITIONS, --partitions PARTITIONS
                            number of partitions to consume; each partition is
                            consumed in its own thread
      -t {dns,flow,proxy}, --type {dns,flow,proxy}
                            type of the data that will be ingested
      --topic TOPIC         topic to listen for new messages

    END

By default, it loads `ingest_conf.json` file. Using `-c , --config-file` option you can override it and use another.

Streaming Listener uses `spark-streaming` parameters from the configuration file:

    "spark-streaming":{
        "driver_memory":"",
        "spark_exec":"",
        "spark_executor_memory":"",
        "spark_executor_cores":"",
        "spark_batch_size":""

The `spark_batch_size` is the time interval (in seconds) at which streaming data will be divided into batches. The default value is 30 seconds.

You can apply a Spark job on local, client or in cluster mode (using `-m , --master` and `-d , --deploy-mode` options).

Additionaly, you can isolate the logs from Spark, using the option `-r , --redirect-spark-logs`. This is usefull in case of debugging.

To start Streaming Listener:<br />
`python start_listener.py -t "pipeline_configuration" --topic "my_topic" -p "number of partitions to consume"`

Some examples are given below:<br />
1. `python start_listener.py -t flow --topic SPOT-INGEST-TOPIC -p 3 -g CUSTOM-GROUP-ID -n myApplication`<br />
2. `python start_listener.py -t flow --topic SPOT-INGEST-TOPIC -p 1 --master yarn --deploy-mode cluster`<br />
3. `python start_listener.py -t dns --topic SPOT-INGEST-DNS-TEST-TOPIC -p 4 --redirect-spark-logs /tmp/StreamingListener_Spark.log`<br />

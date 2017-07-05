# The ml_ops.sh script

ml_ops.sh is a helper script for invoking the suspicious connects analysis in the common case of its use in Spot.

It loads environment variables from `/etc/spot.conf`, parses some arguments from the CLI, invokes the Suspicious Connects analysis from
the spot-ml jar stored in `target/scala-2.10/spot-ml-assembly-1.1.jar`.  


## Usage

The ml_ops script is meant to be used as part of a full spot deployment. Data is expected to reside in the locations used by
Spot ingest and in a schema extending the [schema used by the suspicious connects analyses](SUSPICIOUS_CONNECTS_SCHEMA.md).

To run a suspicious connects analysis, execute the  `ml_ops.sh` from the directory where the spot-ml jar is 
stored in the relative path `target/scala-2.10/spot-ml-assembly-1.1.jar`

```
./ml_ops.sh YYYMMDD <type> <suspicion threshold> <max results>
```

* **YYYYMMDD** The date of the collected data that has been ingested and stored into the Spot's data repository. Eg., 20170129

* **type** is one is of flow, proxy or dns.

* **suspicion threshold** is an optional floating-point value in the range [0,1]. All records that receive a  score strictly greater than
the threshold are not reported.  It defaults to 1 (no filter).

* **max results** is an optional integer value greater than or equal to 0. If provided, results are ordered by score from least to greatest and 
the _max results_ smallest are returned in an ascending list. If not provided, all results with a score below the given threshold are returned in an ascending list.

## Example Usage
 
```
./ml_ops.sh 19731231 flow 1e-20 200
```

If the max results returned argument is not provided, all results with scores below the threshold will be returned, for example:
```
./ml_ops.sh 20150101 dns 1e-4
```

As the maximum probability of an event is 1, a threshold of 1 can be used to select a fixed number of most suspicious items regardless of their exact scores:
```
./ml_ops.sh 20150101 proxy 1 2000
```
## ml_ops.sh output

Final results are stored in the following file on HDFS
Depending on which data source is analyzed, 
spot-ml output will be found under the ``HPATH`` at one of

     $HPATH/dns/scored_results/YYYYMMDD/scores/dns_results.csv
     $HPATH/proxy/scored_results/YYYYMMDD/scores/results.csv
     $HPATH/flow/scored_results/YYYYMMDD/scores/flow_results.csv


It is a csv file in which network events annotated with estimated probabilities and sorted in ascending order.

## Parameters taken from the /etc/spot.conf file

The ml_ops.sh script takes its values for the following parameters from the /etc/spot.conf file:

* **All spark settings** Among them driver memory, number of executors, spark.driever.MaxResultSize, etc.
* **Paths to storage locations for Spot ingested data**
* **USER_DOMAIN** The domain name for the network being analyzed. Used to denote "internal" URLs during proxy and dns analyses.
* **TOPIC_COUNT** Number of topics used for the topic modelling at the heart of the Suspicious Connects anomaly detection. Roughly, the analysis attempts to generate TOPIC_COUNT many profiles of common traffic in the cluster.
* **DUPFACTOR** Used to downgrade the threat level of records similar to those marked as non-threatening by the feedback function of Spot UI. DUPFACTOR inflate the frequency of such records to make them appear less anomalous. A DUPFACTOR of 1 has no effect, and a DUPFACTOR of 1000 increases the frequency of the connection's pattern by a factor of 1000, increasing its estimated probability accordingly.
* **TOL** The default value for the _suspicion threshold_ described above. In particular: If no third argument is provided to ml_ops.sh, the suspicion threshold is filled in with the TOL value from /etc/spot.conf. If a third argument is provided to ml_ops.sh, that is the suspicion threshold used.
* **LDA_OPTIMIZER** The LDA implementation to use. Set equal to "em" to execute LDA using EMLDAOptimizer or "online" to
 use OnlineLDAOptimizer. See [LDA Spark documentation for more information.](https://spark.apache.org/docs/2.1.0/mllib-clustering.html#latent-dirichlet-allocation-lda)
* **LDA_ALPHA** Document concentration. See [LDA Spark documentation for more information.](https://spark.apache.org/docs/2.1.0/mllib-clustering.html#latent-dirichlet-allocation-lda)
* **LDA_BETA** Topic concentration. See [LDA Spark documentation for more information.](https://spark.apache.org/docs/2.1.0/mllib-clustering.html#latent-dirichlet-allocation-lda)


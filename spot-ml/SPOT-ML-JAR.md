# The spot-ml jar


The spot-ml jar contains one main routine and it is in the class `SuspiciousConnects`; 
it is by submitting this class to Spark that the Suspicious Connects analyses are invoked.



# Command line arguments


* **analysis**  The analysis to perform. One of  flow, proxy, dns
* **input** Path to data on HDFS. Data is expected to be stored in parquet with schema consistent with 
[schema used by the suspicious connects analyses](SUSPICIOUS_CONNECTS_SCHEMA.md).
* **feedback**  Local path of file containing feedback scores.
* **dupfactor** Duplication factor controlling how to down rate non-threatening events from the feedback file.
* **ldatopiccount** Number of topics in the topic model.
* **userdomain** The user domain of the network being analyzed.
* **scored** The HDFS path where results will be stored.
* **threshold** Threshold for determination of anomalies. Records with scores above the threshold will not be returned.
* **maxresults** Maximum number of record to return. If -1, all records are returned. Results are filtered by the threshold and sorted and the most suspicious (lowest score) records are returned first.
* **delimiter** Separation character used for CSVs containing most suspicious results.
* **prgseed** Seed for the pseudorandom generator used in topic modelling.
* **ldamaxiteration** Maximum number of iterations to execute the LDA topic modelling procedure.
* **ldaalpha** Document concentration for LDA, default 1.02
* **ldabeta** Topic concentration for LDA, default 1.001

# Example


<pre><code>

spark-submit --class "org.apache.spot.SuspiciousConnects"  target/scala-2.10/spot-ml-assembly-1.1.jar \
  --analysis proxy \
  --input /user/spot/proxy/hive/y=2016/m=11/d=11/ \
  --dupfactor 1000 \
  --feedback /home/spot/ml/proxy/20161111/feedback.csv \
  --ldatopiccount 20 \
  --scored /user/spot/proxy/scored_results/20161111/scores \
  --threshold 1e-6 \
  --maxresults 2000 \
  --ldamaxiterations 20 

</code></pre>


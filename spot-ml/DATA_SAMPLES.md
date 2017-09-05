
# DNS Labeled Data Sets

An IXIA BreakingPoint box to simulate both normal and attack (DNS tunnelling) DNS traffic. The resulting pcaps were obtained and fields relevant to Spot injested (both original pcaps and injested parquet files are available in Amazon-S3). The attacks and the normal activity can be differentiated due to codes that were inserted into the Transaction ID field(upon ingestion the field is: ‘dns_id’) which identifies either the fact that the traffic was normal or identifies the specific dns tunneling activity being used.  We provide the schema for the injested pcap data, location and specifications of the data within Amazon-S3, and how to interpret the ‘dns_id’ codes.

Spot (using version #####fill in here###.) was run on these datasets with ten repetitions each.  We provide the Area Under the Curve (AUC) value related to how well the attacks were detected. We also provide the rank distributions for the various attacks within the dataset, with a rank of 1 meaning the entry was found to be the most suspicious entry out of all other entries.


## Schema For Ingested Data (same for both data sets)

The schema for this DNS data has one additional field, ‘dns_id’, over what is usually used for DNS data in Spot. The schema is as follows:


| name         | type      |
|--------------|:---------:|
| frame_time   | string    |
| unix_tstamp  | bigint    |


## Transaction ID Interpretations (same for both data sets)
The following provides interpretations for the values of the transaction ID field, ‘dns_id’. Each value indicates that either the data row was taken from a packet capture of simulated normal DNS traffic, or from a packet capture of a particular type of simulated attack.

Within BreakingPoint, Transaction IDs are represented as a decimal number. However, tshark dissect the transaction id in its hexadecimal representation in the format contained within parenthesis below.
Within Apache Spot only responses from DNS servers are ingested (since the response packet contains the query made by the client and the response from the server in the same packet)








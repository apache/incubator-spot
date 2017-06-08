# spot-ml

Machine learning routines for Apache Spot (incubating).

At present, spot-ml contains routines for performing *suspicious connections* analyses on netflow, DNS or proxy data gathered from a network. These
analyses consume a (possibly very lage) collection of network events and produces a list of the events that considered to be the least probable (most suspicious).

spot-ml is designed to be run as a component of Spot. It relies on the ingest component of Spot to collect and load
netflow and DNS records, and spot-ml will try to load data to the operational analytics component of Spot.  It is suggested that when experimenting with spot-ml, you do so as a part of the unified Spot system: Please see [the Spot wiki]

The remaining instructions in this README file treat spot-ml in a stand-alone fashion that might be helpful for customizing and troubleshooting the
component.

## Prepare data for input 

Load data for consumption by spot-ml by running [spot-ingest].

The data format and location where the data is stored differs for netflow and DNS analyses.

**Netflow Data**

Netflow data for the year YEAR, month  MONTH, and day DAY is stored in in a Parquet table  at `HUSER/flow/csv/y=YEAR/m=MONTH/d=DAY/*` according to
 the following schema:

- time: String
- year: Double
- month: Double
- day: Double
- hour: Double
- minute: Double
- second: Double
- time of duration: Double
- source IP: String
- destination IP: String
- source port: Double
- dport: Double
- proto: String
- flag: String
- fwd: Double
- stos: Double
- ipkt: Double.
- ibyt: Double
- opkt: Double
- obyt: Double
- input: Double
- output: Double
- sas: String
- das: Sring
- dtos: String
- dir: String
- rip: String

**DNS Data**

DNS data for the year YEAR, month MONTH and day DAY is stored in Parquet at `HUSER/dns/hive/y=YEAR/m=MONTH/d=DAY/` using the following schema:

- frame_time: STRING
- unix_tstamp: BIGINT
- frame_len: INT3. ip_dst: STRING
- ip_src: STRING
- dns_qry_name: STRING
- dns_qry_class: STRING
- dns_qry_type: INT
- dns_qry_rcode: INT
- dns_a: STRING

**PROXY DATA**
Proxy data for the year YEAR, month MONTH and day DAY is stored in Parquet at `HUSER/dns/hive/y=YEAR/m=MONTH/d=DAY/` using the following schema:

- p_date: STRING
- p_time: STRING  
- clientip: STRING                           
- host: STRING    
- reqmethod: STRING                                    
- useragent: STRING                                      
- resconttype: STRING                                      
- duration: INT                                         
- username: STRING                                      
- authgroup: STRING                                      
- exceptionid: STRING                                      
- filterresult: STRING                                      
- webcat: STRING                                      
- referer: STRING                                      
- respcode: STRING                                      
- action: STRING                                      
- urischeme: STRING                                      
- uriport: STRING                                      
- uripath: STRING                                      
- uriquery: STRING                                      
- uriextension: STRING                                      
- serverip: STRING                                      
- scbytes: INT                                         
- csbytes: INT                                         
- virusid: STRING                                      
- bcappname: STRING                                      
- bcappoper: STRING                                      
- fulluri: STRING



### Run a suspicious connects analysis

To run a suspicious connects analysis, execute the  `ml_ops.sh` script in the ml directory of the MLNODE.
```
./ml_ops.sh YYYMMDD <type> <suspicion threshold> <max results returned>
```


For example:  
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


### spot-ml output

Final results are stored in the following file on HDFS.

Depending on which data source is analyzed, 
spot-ml output will be found under the ``HPATH`` at one of

     $HPATH/dns/scored_results/YYYYMMDD/scores/dns_results.csv
     $HPATH/proxy/scored_results/YYYYMMDD/scores/results.csv
     $HPATH/flow/scored_results/YYYYMMDD/scores/flow_results.csv


It is a csv file in which network events annotated with estimated probabilities and sorted in ascending order.

## User Feedback

The spot front end allows users to mark individual logged events as high, medium or low risk. 

The risk score is stored as a 1 for high risk, 2 for medium risk and 3 for low risk.

At present, the scores of events similar to low risk items are inflated by the model, and nothing (at present) changes events flagged medium or high risk.


This information is stored in a tab-separated text file stored on HDFS at:


	/user/<user_name>/<data source>/scored_results/<date>/feedback/ml_feedback.csv


## Licensing

spot-ml is licensed under Apache Version 2.0

## Contributing

Create a pull request and contact the maintainers.

## Issues

Report issues at the [Spot issues page].

## Maintainers

[Ricardo Barona](https://github.com/rabarona)

[Nathan Segerlind](https://github.com/NathanSegerlind)

[Everardo Lopez Sandoval](https://github.com/EverLoSa)

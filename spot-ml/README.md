# spot-ml

Machine learning routines for Apache Spot (incubating).

spot-ml contains routines for performing *suspicious connections* analyses on netflow, DNS or proxy data gathered from a network. These
analyses consume collections of network events and produce lists of the events that are considered to be the least probable (most suspicious).

The suspicious connects analysis assigns to each record a score in the range 0 to 1, with smaller scores being viewed as more suspicious or anomalous than larger scores.

These routines are contained in a jar file   and there is a shell script ml_ops.sh for a simplified the invocation of the analyses.


* [jar documentation here](SPOT-ML-JAR.md)
* [ml_ops.sh documentation here](ML_OPS.md) 

## Configure the /etc/spot.conf file

If using spot-ml as part of the integrated spot solution (or if you simply wish to use the ml_ops.sh script to invoke the suspicious connects analysis), 
the /etc/spot.conf file must be correctly configured.

## Prepare data for input 

Whether suspicious connects is called by ml_ops.sh or through the ml-ops jar, data must be in the [schema used by the suspicious connects analyses](SUSPICIOUS_CONNECTS_SCHEMA.md).  Ingesting data via the Spot ingest tools will store data in an appropriate schema.


### Data locations for ml_ops.sh

ml_ops.sh expects data to be in particular locations. The environment variable `HUSER` defined in /etc/spot.conf is the Hadoop user executing the analysis.

- Netflow data for the year YEAR, month  MONTH, and day DAY is stored in in a Parquet table  at `HUSER/flow/csv/y=YEAR/m=MONTH/d=DAY/*` 

- DNS data for the year YEAR, month MONTH and day DAY is stored in Parquet at `HUSER/dns/hive/y=YEAR/m=MONTH/d=DAY/`

- Proxy data for the year YEAR, month MONTH and day DAY is stored in Parquet at `HUSER/dns/hive/y=YEAR/m=MONTH/d=DAY/` 


Ingesting data with the Spot ingestion tools (and a shared /etc/spot.conf file) will save data in these locations.


### Run a suspicious connects analysis

To run a suspicious connects analysis, execute the  `ml_ops.sh` script in the ml directory of the MLNODE.
```
./ml_ops.sh YYYMMDD <type> <suspicion threshold> <max results returned>
```


For example:  
```
./ml_ops.sh 19731231 flow 1e-20 200
```

You should have a list of the 200 most suspicious flow events from 

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

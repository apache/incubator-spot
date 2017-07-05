# spot-ml

Machine learning routines for Apache Spot (incubating).

## Prerequisites, Installation and Configuration

Install and configure spot-ml as a part of the Spot project, per the instruction at
[the Spot wiki].

The spot-ml routines must be built into a jar stored at `target/scala-2.10/spot-ml-assembly-1.1.jar` on the ML node. This requires Scala 2.10 or later to be installed on the system building the jar. To build the jar, from the top-level of the spot-ml repo, execute the command `sbt assembly`.

Names and language that we will use from the configuration variables for Spot (that are set in the file [spot.conf])

- MLNODE The node from which the spot-ml routines are invoked
- HUSER An HDFS user path that will be the base path for the solution; this is usually the same user that you created to run the solution
- HPATH Location for storing intermediate results of the analysis on HDFS.
- USER_DOMAIN Web domain associated to the user's network (for the DNS suspicious connects analysis). For example: USER_DOMAIN='intel'.

### Prepare data for input 

Load data for consumption by spot-ml by running [spot-ingest].


## Run a suspicious connects analysis

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

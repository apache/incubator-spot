# spot-setup

## Intended Audience

This document is intended for any developer or sysadmin in learning the technical aspects or in contributing to the setup installation process of the Apache Spot solution.

## Getting Started

This information will help you to get started on contributing to the Apache Spot Setup repository. For information about installing and running Apache Spot go to our [Installation Guide](http://spot.apache.org/doc/).

Spot-setup contains the scripts to setup HDFS for Apache Spot solution. It will create the folder and database structure needed to run Apache Spot on HDFS and Impala respectively. Spot-setup is a component of Apache Spot and is executed in the initial configuration after Linux user creation and before Ingest installation.

## Prerequisites

To collaborate and run spot-setup, it is required the following prerequisites:
- A running Hadoop cluster
- Linux user account created in all nodes with sudo privileges
- Enabling HDFS Access Control Lists

## General Description

The main script in the repository is **hdfs_setup.sh** which is responsible of loading environment variables, creating folders in Hadoop for the different use cases (flow, DNS or Proxy), create the Impala database, and finally execute Impala query scripts that creates Impala tables needed to access netflow, dns and proxy data.

## Environment Variables

**spot.conf** is the file storing the variables needed during the installation process including node assignment, User interface, Machine Learning and Ingest gateway nodes.

This file also contains sources desired to be installed as part of Apache Spot, general paths for HDFS folders, and local paths in the Linux filesystem for the user as well as for machine learning, ipython, lda and ingest processes.

To read more about these variables, please review the [documentation](http://spot.incubator.apache.org/doc/#configuration).

## Database Query Scripts

spot-setup contains a script per use case, as of today, there is a table creation script for each DNS, flow and Proxy data.

These HQL scripts are intended to be executed as a Impala statement and must comply HQL standards.

We create tables using Parquet format to get a faster query performance. This format is an industry standard and you can find more information about it on:
- Parquet is a columnar storage format - https://parquet.apache.org/

To get to parquet format we need a staging table to store CSV data temporarily for Flow and DNS. Then, run an Impala query statement to insert these text-formatted records into the parquet table. Impala will manage to convert the text data into the desired format. The staging table must be cleaned after loading data to parquet table for the next batch cycle. For Flow and DNS, a set of a staging (CSV) and a final (parquet) tables are needed for each data entity. For Proxy, only the parquet table is needed.

#### Flow Tables
- flow
- flow_tmp
- flow_chords
- flow_edge
- flow_ingest_summary
- flow_scores
- flow_storyboard
- flow_threat_investigation
- flow_timeline

#### DNS Tables
- dns
- dns_tmp
- dns_dendro
- dns_edge
- dns_ingest_summary
- dns_scores
- dns_storyboard
- dns_threat_dendro
- dns_threat_investigation

#### Proxy Tables
- proxy
- proxy_edge
- proxy_ingest_summary
- proxy_scores
- proxy_storyboard
- proxy_threat_investigation
- proxy_timeline


## Migrating OA Data to Spot 1.0

Please review migration documentation [here](migration/README.md).

## Licensing

spot-setup is licensed under Apache Version 2.0

## Contributing

Create a pull request and contact the maintainers.

## Issues

- Create an [issue](https://issues.apache.org/jira/browse/SPOT-20?jql=project%20%3D%20SPOT).
- Go to our Slack [channel](https://apachespot.slack.com/messages/general).

## Maintainers

- [Moises Valdovinos](https://github.com/moy8011)
- [Everardo Lopez Sandoval](https://github.com/EverLoSa)


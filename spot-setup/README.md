# oni-setup

## Intended Audience

This document is intended for any developer or sysadmin interested in technical aspects or in contributing to the setup installation process of the ONI solution. 

## Getting Started

This information will help you to get started on contributing to the ONI Setup repository. For information about installing and running ONI go to our [Installation Guide](https://github.com/Open-Network-Insight/open-network-insight/wiki/Installation%20&%20Configuration%20Guides).

oni-setup contains the scripts to setup HDFS for ONI solution. It will create the folder and database structure needed to run ONI on HDFS and HIVE respectively. Oni-setup is a component of Open-Network-Insight and is executed in the initial configuration after Linux user creation and before Ingest installation.

## Prerequisites

To collaborate and run oni-setup, it is required the following:
- A running Hadoop cluster
- Linux user account created in all nodes with sudo privileges

## General Description

The main script in the repository is **hdfs_setup.sh** which is responsible of loading environment variables, creating folders in Hadoop for the different use cases (flow, DNS or Proxy), create the Hive database, and finally execute hive query scripts that creates Hive tables needed to access netflow, dns and proxy data.

## Environment Variables

**duxbay.conf** is the file storing the variables needed during the installation process including node assignment, User interface, Machine Learning and Ingest gateway nodes.

This file also contains sources desired to be installed as part of ONI, general paths for HDFS folders, Kerberos information and local paths in the Linux filesystem for the user as well as for machine learning, ipython, lda and ingest processes.

To read more about these variables, please review the [wiki] (https://github.com/Open-Network-Insight/open-network-insight/wiki/Edit%20Solution%20Configuration).

## Database Query Scripts

oni-setup contains a script per use case, as of today, there is a tables creation script for each DNS, flow and Proxy data.

These HQL scripts are intended to be executed as a Hive statement and must comply HQL standards. 

We want to create tables in Avro/Parquet format to get a faster query performance. This format is an industry standard and you can find more information about it on:
- Avro is a data serialization system - https://avro.apache.org/
- Parquet is a columnar storage format - https://parquet.apache.org/

To get to Avro/parquet format we need a staging table to store CSV data temporarily for Flow and DNS. Then, run a Hive query statement to insert these text-formatted records into the Avro/parquet table. Hive will manage to convert the text data into the desired format. The staging table must be cleaned after loading data to Avro/parquet table for the next batch cycle. For Flow and DNS, a set of a staging (CSV) and a final (Avro/parquet) tables are needed for each data entity. For Proxy, only the Avro/parquet table is needed.

#### Flow Tables
- flow - Avro/parquet final table to store flow records
- flow_tmp - Text table to store temporarily flow records in CSV format

#### DNS Tables
- dns - Avro/parquet final table to store DNS records
- dns_tmp - Text table to store temporarily DNS records in CSV format

#### Proxy Tables
- proxy - Avro/parquet final table to store Proxy records

## Licensing

oni-setup is licensed under Apache Version 2.0

## Contributing 

Create a pull request and contact the maintainers.

## Issues

Report issues at the OpenNetworkInsight [issues] (https://github.com/Open-Network-Insight/open-network-insight/issues) page.
 
## Maintainers

- [Moises Valdovinos] (https://github.com/moy8011)
- [Everardo Lopez Sandoval] (https://github.com/EverLoSa)  


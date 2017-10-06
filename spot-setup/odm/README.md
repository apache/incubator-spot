# spot-setup/odm - Open Data Model (ODM) Setup

## Intended Audience

This document is intended for any developer or sysadmin in learning the technical aspects or in contributing to the setup installation process of the Apache Spot solution.

## Getting Started

This information will help you to get started on contributing to the ODM component of the Apache Spot Setup repository.
For information about installing and running Apache Spot go to our [Installation Guide](http://spot.apache.org/doc/).
spot-setup/odm contains the scripts to setup HDFS for the Apache Spot Open Data Model (ODM).
It will create the folder and database structure needed to run Apache Spot on HDFS and HIVE respectively.
spot-setup/odm is a component of Apache Spot and is executed in the initial configuration after Linux user creation and before Ingest installation.

## Prerequisites

To collaborate and run spot-setup, it is required the following prerequisites:
- A running Hadoop cluster
- Linux user account created in all nodes with sudo privileges
- HDFS ACLs must be enabled (dfs.namenode.acls.enabled=true)

## General Description

The main script in the repository is **odm_setup.sh** which is responsible of loading environment variables,
creating folders in Hadoop for the ODM data (event, user_context, endpoint_context, network_context, threat_intelligence_context, vulnerability_context),
create the Hive database,
and finally execute hive query scripts that creates Hive tables needed to access Apache Spot data through the ODM.

## Environment Variables

**spot.conf** is the file storing the variables needed during the installation process including node assignment, User interface, Machine Learning and Ingest gateway nodes.
This file also contains sources desired to be installed as part of Apache Spot, general paths for HDFS folders, Kerberos information and local paths in the Linux filesystem for the user as well as for machine learning, ipython, lda and ingest processes.
To read more about these variables, please review the [wiki](http://spot.incubator.apache.org/doc/#configuration).

By default, **odm_setup.sh** expects **spot.conf** to be located in the **/etc** directory on the node. An example spot.conf file is provided in the spot-setup parent directory to help you get started.

## ODM Database Query Scripts

spot-setup/odm contains a script per each table as specified in the Apache Spot Open Data Model (ODM) document.
These HQL scripts are intended to be executed as a Hive statement and must comply HQL standards.

We want to create tables in Avro/Parquet format to get a faster query performance. This format is an industry standard and you can find more information about it on:
- Avro is a data serialization system - https://avro.apache.org/
- Parquet is a columnar storage format - https://parquet.apache.org/

The ODM database query scripts are referenced and executed as part of running the **odm_setup.sh** script.
Data is meant to be ingested directly into the ODM directories that are tied to the external tables created from the script.

#### ODM Tables
- event - includes event logs from common data sources used to detect threats and includes network flows, operating system logs, IPS/IDS logs, firewall logs, proxy logs, web logs, DLP logs, etc.
- user_context - includes information from user and identity management systems.
- endpoint_context - includes information about endpoint systems (servers, workstations, routers, switches, etc.) and can be sourced from asset management systems, vulnerability scanners, and endpoint management/detection/response systems
- threat_intelligence_context - includes contextual information about URLs, domains, websites, files and others.
- network_context - includes information about the network, which can be gleaned from Whois servers, asset databases and other similar data sources.
- vulnerabilty_context - includes information about vulnerabilities present on endpoint systems that can be sourced from vulnerability scanners and management systems.

## Licensing

spot-setup/odm is licensed under Apache Version 2.0

## Contributing

Create a pull request and contact the maintainers.

## Issues

Report issues at the Apache Spot [issues](https://issues.apache.org/jira/projects/SPOT/issues) page.


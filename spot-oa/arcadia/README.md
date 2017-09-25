# spot-oa/arcadia

## Arcadia Dashboards for Apache Spot

This document describes the steps required to setup and use Arcadia Dashboards as a visualization layer on the Apache Spot Open Data Model (ODM)

## Getting Started

Contained in this directory is a configuration file (spot_app.json) that includes several sample dashboards for end users to setup and use with the Apache Spot ODM.

#### Included Dashboards

- User Activity Summary - Provides summary level view of activity related to users in your environment.
- Endpoint Activity Summary - Provides a summary level view of activity related to endpoints in your environment.
- Vulnerabilities - Provides a contextual view of vulnerabilities and details related to endpoints in your environment.

End users are encouraged to customize these dashboards for their own purposes and use-cases.

## Prerequisites

To setup and configure the Arcadia Dashboards, you must have the following:
- A running Hadoop cluster with Impala installed and configured
- Linux user account created in all nodes with sudo privileges
- Installation and setup of the Open Data Model (ODM) directories and schema (see spot-setup/odm)
- Download of Arcadia Instant (https://www.arcadiadata.com/product/instant/)

Having the ODM directories populated with data or sample data is also recommended for visualizations to load properly.

## Setup Instructions

Following the completion of the pre-requisites and starting Arcadia Instant, you can perform the following steps to import and configure the Apache Spot dashboards contained in the **spot_app.json** file.
1. In the Arcadia Instant Control Panel, click  "Go" to launch a browser window.
2. Click the "Data" tab, and then click "New Connection"
3. Select "Impala" as the connection type, and configure the connection to connect to the Impala daemon on your running Hadoop cluster.
Click the "Advanced" tab if you need to configure LDAP or Kerberos authentication for your connection.
Also make sure "Result cache" is enabled in the "Cache" tab of your connection.
4. Click the "Test" button to make sure you connection is working and then "Connect" to exit.
5. Click your newly setup connection and look for a button that looks like an ellipsis (...).
6. Click the ellipsis button, and then click "Import Visual Artifacts"
7. Choose the **spot_app.json** file to upload.
8. Click "Accept and Import".
9. Presto! You should now have live dashboards connected to the Apache Spot (ODM) tables.

## Sources for Sample Data

If you haven't landed any data in the ODM directories yet and would like to demo the Arcadia Dashboards,
you can complete the following steps to download sample data within your running Hadoop cluster.
Also note that the dependencies in the Enviornment Variables section below.

**OBLIGITORY DISCLAIMER**: DO NOT RUN THIS SCRIPT IF YOU ALREADY HAVE PRODUCTION DATA POPULATING THE ODM DIRECTORIES.

There is a safe guard in the odm_sample_setup.sh script to prevent overwrites of data in the ODM directories,
but its best to avoid this step altogether if you know data is already landing in those directories.

#### Environment Variables

The **odm_sample_setup.sh** script is dependent on the **spot.conf** file being installed (see spot-setup/odm),
which is intended to be located in the /etc directory by default.

#### Sample Data Setup Steps

1. run wget http://get.arcadiadata.com.s3.amazonaws.com/spot/sample/odm_sample_setup.sh to retrieve the sample data setup script.
2. run chmod +x odm_sample_setup.sh to make the script executable.
3. run ./odm_sample_setup.sh to retrieve, store, and make sample data available in your ODM tables.

## Licensing

spot-setup is licensed under Apache Version 2.0

## Contributing

Create a pull request and contact the maintainers.

## Issues

Report issues at the Apache Spot [issues] (https://issues.apache.org/jira/projects/SPOT/issues) page.

## Maintainers

- [Tadd Wood] (https://github.com/TaddWood)


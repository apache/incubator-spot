## OA Data Migration to Spot 1.0

This document is intended for any developer or sysadmin who wants to migrate their existing OA data to Spot 1.0. In previous Spot releases, OA data was stored in CSV files in a given location in the server used for OA (specified in spot.conf during original installation). In Spot 1.0, OA data is stored in Impala tables. The purposes of these scripts are to migrate independently each use case (flow, proxy and dns) from those CSV files into the new Impala tables.

This migration process is optional and only for those users who want to keep usable their OA data (scores, edges, chords, dendros, ingest summaries, storyboards, threat investigations and timelines) generated in the previous Spot version. 

### Requirements

- You must run first new Spot setup installation to have the new tables created in Impala. 
- You must log in to the OA server and run these scripts from there.
- You must run these scripts from the spot-setup/migration folder in the new Spot 1.0 location

### CSV to Impala Tables Mapping

**Flow**

CSV File | Impala Table
---------|-------
flow_scores.csv | flow_scores
chord-*.tsv  | flow_chords 
edge-*.tsv | flow_edge
is_*.csv | flow_ingest_summary
threats.csv | flow_storyboard
flow_scores.csv (only scored values) | flow_threat_investigation
sbdet-*.tsv | flow_timeline

**DNS**

CSV File | Impala Table
---------|-------
flow_scores.csv | dns_scores
edge-*.csv | dns_edge
dendro-*.csv | dns_dendro
threat-dendro-*.csv | dns_threat_dendro
is_*.csv | dns_ingest_summary
threats.csv | dns_storyboard
dns_scores.csv (only scored values) | dns_threat_investigation

**Proxy**

CSV File | Impala Table
---------|-------
edge-*.csv | proxy_edge
is_*.csv | proxy_ingest_summary
proxy_scores.csv | proxy_scores
threats.csv | proxy_storyboard
proxy_scores.csv (only scored values) | proxy_threat_investigation
timeline-*.csv | proxy_timeline

### Data Flow

There is a launch and single script that will migrate all specified pipelines. This process will read each of the CSV from the existing location and import data to Impala tables accordingly, creating first a staging database and tables to load the records in the CSV and then insert that data into the new Spot 1.0 tables. You must execute this migration process from the server where old Spot release is located. You may provide one pipeline or all (flow, dns and proxy) according to your needs and your existing data. At the end of each script, the old data pipeline folder will be moved from the original location to a backup folder. Staging tables and their respective HDFS paths will be removed.

### Execution

```python
./migrate_to_spot_1_0.py PIPELINES OLD_OA_PATH STAGING_DB_NAME STAGING_DB_HDFS_PATH NEW_SPOT_IMPALA_DB IMPALA_DAEMON
```

where variables mean:
- **PIPELINES** - Comma-separated list of the pipelines to be migrated 
- **OLD_OA_PATH** - Path to the old Spot-OA release directory in the local filesystem 
- **STAGING_DB_NAME** - Name of the staging database to be created to temporarily store these records
- **STAGING_DB_HDFS_PATH** - HDFS path of the staging database to be created to temporarily store these records
- **NEW_SPOT_IMPALA_DB** - Database name of the Spot 1.0 Impala tables. Use the same as in the spot.conf when the new Spot release was installed 
- **IMPALA_DAEMON** - Choose an Impala daemon to be used to run scripts' queries.

Example:
```python
./migrate_to_spot_1_0.py 'flow,dns,proxy' '/home/spotuser/incubator-spot_old/spot-oa' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'
```

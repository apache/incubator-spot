#!/bin/env python

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

import os
import sys
import subprocess
import fnmatch
import re
import pandas as pd
import datetime
from utilities import util

old_oa_path=sys.argv[1]
staging_db=sys.argv[2]
hdfs_staging_path=sys.argv[3]
dest_db = sys.argv[4]
impala_daemon = sys.argv[5]


# Execution example:
#./migrate_old_proxy_data.py '/home/spotuser/incubator-spot_old/spot-oa' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'

def main():
  log = util.get_logger('SPOT.MIGRATE.PROXY')

  cur_path = os.path.dirname(os.path.realpath(__file__))
  new_spot_path = os.path.split(os.path.split(cur_path)[0])[0]
  new_oa_path = '{0}/spot-oa'.format(new_spot_path)
  log.info('New Spot OA path: {0}'.format(new_oa_path))
  old_spot_path = os.path.split(old_oa_path)[0]


  log.info("Creating HDFS paths for Impala tables")
  util.create_hdfs_folder('{0}/proxy/scores'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/proxy/edge'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/proxy/summary'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/proxy/storyboard'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/proxy/timeline'.format(hdfs_staging_path),log)
  util.execute_cmd('hdfs dfs -setfacl -R -m user:impala:rwx {0}'.format(hdfs_staging_path),log)


  log.info("Creating Staging tables in Impala")
  util.execute_cmd('impala-shell -i {0} --var=hpath={1} --var=dbname={2} -c -f create_proxy_migration_tables.hql'.format(impala_daemon, hdfs_staging_path, staging_db),log)


  ## proxy Ingest Summary
  log.info('Processing proxy Ingest Summary')
  ing_sum_path='{0}/data/proxy/ingest_summary/'.format(old_oa_path)
  pattern='is_??????.csv'
  staging_table_name = 'proxy_ingest_summary_tmp'
  dest_table_name = 'proxy_ingest_summary'

  if os.path.exists(ing_sum_path):
    for file in fnmatch.filter(os.listdir(ing_sum_path), pattern):
      log.info('Processing file: {0}'.format(file))

      filepath='{0}{1}'.format(ing_sum_path, file)
      df = pd.read_csv(filepath)

      s = df.iloc[:,0]
      l_dates = list(s.unique())
      l_dates = map(lambda x: x[0:10].strip(), l_dates)
      l_dates = filter(lambda x: re.match('\d{4}[-/]\d{2}[-/]\d{1}', x), l_dates)
      s_dates = set(l_dates)

      for date_str in s_dates:
        dt = datetime.datetime.strptime(date_str, '%Y-%m-%d')
        log.info('Processing day: {0} {1} {2} {3}'.format(date_str, dt.year, dt.month, dt.day))

        records = df[df['date'].str.contains(date_str)]
        filename = "ingest_summary_{0}{1}{2}.csv".format(dt.year, dt.month, dt.day)
        records.to_csv(filename, index=False)

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT tdate, total FROM {5}.{6}".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)

        os.remove(filename)


  ## Iterating days 
  days_path='{0}/data/proxy/'.format(old_oa_path)

  if os.path.exists(days_path):
    for day_folder in fnmatch.filter(os.listdir(days_path), '2*'):
        
      print day_folder
      dt = datetime.datetime.strptime(day_folder, '%Y%m%d')
      log.info('Processing day: {0} {1} {2} {3}'.format(day_folder, dt.year, dt.month, dt.day))
      full_day_path = '{0}{1}'.format(days_path,day_folder)


      ## proxy Scores and proxy Threat Investigation
      filename = '{0}/proxy_scores.tsv'.format(full_day_path) 
      if os.path.isfile(filename):

        log.info("Processing Proxy Scores")
        staging_table_name = 'proxy_scores_tmp'
        dest_table_name = 'proxy_scores'

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT tdate, time, clientip, host, reqmethod, useragent, resconttype, duration, username, webcat, referer, respcode, uriport, uripath, uriquery, serverip, scbytes, csbytes, fulluri, word, ml_score, uri_rep, respcode_name, network_context FROM {5}.{6}".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)

        log.info("Processing Proxy Threat Investigation")
        staging_table_name = 'proxy_scores_tmp'
        dest_table_name = 'proxy_threat_investigation'

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT tdate, fulluri, uri_sev FROM {5}.{6} WHERE uri_sev > 0;".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)


      ## proxy Edge
      log.info("Processing Proxy Edge")
      staging_table_name = 'proxy_edge_tmp'
      dest_table_name = 'proxy_edge'
      pattern = 'edge*.tsv'
      edge_files = fnmatch.filter(os.listdir(full_day_path), pattern)
      filename = '{0}/{1}'.format(full_day_path, pattern)

      if len(edge_files) > 0:

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT tdate, time, clientip, host, webcat, respcode, reqmethod, useragent, resconttype, referer, uriport, serverip, scbytes, csbytes, fulluri, 0, '' FROM {5}.{6};".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)


      ##proxy_storyboard
      log.info("Processing Proxy Storyboard")
      staging_table_name = 'proxy_storyboard_tmp'
      dest_table_name = 'proxy_storyboard'
      filename = '{0}/threats.csv'.format(full_day_path)

      if os.path.isfile(filename):

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT p_threat, title, text FROM {5}.{6};".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)


      ##proxy Timeline
      log.info("Processing Proxy Timeline")
      staging_table_name = 'proxy_timeline_tmp'
      dest_table_name = 'proxy_timeline'

      for filename in fnmatch.filter(os.listdir(full_day_path), 'timeline*.tsv'):
        print filename

        hash_code = re.findall("timeline-(\S+).tsv", filename)[0]
        extsearch_path = "{0}/es-{1}.csv".format(full_day_path, hash_code)
        log.info('File: {0}  Hash: {1}  Extended Search file: {2}'.format(filename, hash_code, extsearch_path))

        if os.path.isfile('{0}'.format(extsearch_path)):

          log.info("Getting Full URI from extended search file")
          es_df = pd.read_csv(extsearch_path, sep='\t')
          fulluri = es_df.iloc[0]['fulluri']
          log.info('Full URI found: {0}'.format(fulluri))

          # Load timeline to staging table
          load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
          util.execute_hive_cmd(load_cmd, log)

          # Insert into new table from staging table and adding FullURI value
          insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT '{5}', tstart, tend, duration, clientip, respcode, '' FROM {6}.{7} where cast(tstart as timestamp) is not null;".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, fulluri, staging_db, staging_table_name)
          util.execute_hive_cmd(insert_cmd, log)

        else:
            print "Extended search file {0} doesn't exist. Timeline on file {1} can't be processed".format(extsearch_path, timeline_path)



  log.info("Dropping staging tables")
  util.execute_cmd('impala-shell -i {0} --var=dbname={1} -c -f drop_proxy_migration_tables.hql'.format(impala_daemon, staging_db),log)

  log.info("Removing staging tables' path in HDFS")
  util.execute_cmd('hadoop fs -rm -r {0}/proxy/'.format(hdfs_staging_path),log)

  log.info("Moving CSV data to backup folder")
  util.execute_cmd('mkdir {0}/data/backup/'.format(old_oa_path),log)
  util.execute_cmd('mv {0}/data/proxy/ {0}/data/backup/'.format(old_oa_path),log)

  log.info("Invalidating metadata in Impala to refresh tables content")
  util.execute_cmd('impala-shell -i {0} -q "INVALIDATE METADATA;"'.format(impala_daemon),log)


  log.info("Copying advanced mode notebooks to each existing day in new Spot location")
  for folder in os.listdir('{0}/ipynb/proxy/'.format(new_oa_path)):
    util.execute_cmd('cp {0}/oa/proxy/ipynb_templates/Advanced_Mode_master.ipynb {1}/ipynb/proxy/{2}/Advanced_Mode.ipynb'.format(old_oa_path, new_oa_path, folder),log)

  log.info("Copying threat investigation ipynb template to each existing day in new Spot location")
  for folder in os.listdir('{0}/ipynb/proxy/'.format(new_oa_path)):
    util.execute_cmd('cp {0}/oa/proxy/ipynb_templates/Threat_Investigation_master.ipynb {1}/ipynb/proxy/{2}/Threat_Investigation.ipynb'.format(old_oa_path, new_oa_path, folder),log)



if __name__=='__main__':
  main()

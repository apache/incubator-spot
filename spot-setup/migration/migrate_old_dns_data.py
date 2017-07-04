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
#./migrate_old_dns_data.py '/home/spotuser/incubator-spot_old/spot-oa' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'

def main():
  log = util.get_logger('SPOT.MIGRATE.DNS')

  cur_path = os.path.dirname(os.path.realpath(__file__))
  new_spot_path = os.path.split(os.path.split(cur_path)[0])[0]
  new_oa_path = '{0}/spot-oa'.format(new_spot_path)
  log.info('New Spot OA path: {0}'.format(new_oa_path))
  old_spot_path = os.path.split(old_oa_path)[0]


  log.info("Creating HDFS paths for Impala tables")
  util.create_hdfs_folder('{0}/dns/scores'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/dns/dendro'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/dns/edge'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/dns/summary'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/dns/storyboard'.format(hdfs_staging_path),log)
  util.create_hdfs_folder('{0}/dns/threat_dendro'.format(hdfs_staging_path),log)
  util.execute_cmd('hdfs dfs -setfacl -R -m user:impala:rwx {0}'.format(hdfs_staging_path),log)


  log.info("Creating Staging tables in Impala")
  util.execute_cmd('impala-shell -i {0} --var=hpath={1} --var=dbname={2} -c -f create_dns_migration_tables.hql'.format(impala_daemon, hdfs_staging_path, staging_db),log)


  ## dns Ingest Summary
  log.info('Processing Dns Ingest Summary')
  ing_sum_path='{0}/data/dns/ingest_summary/'.format(old_oa_path)
  pattern='is_??????.csv'
  staging_table_name = 'dns_ingest_summary_tmp'
  dest_table_name = 'dns_ingest_summary'

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
  days_path='{0}/data/dns/'.format(old_oa_path)

  if os.path.exists(days_path):
    for day_folder in fnmatch.filter(os.listdir(days_path), '2*'):
        
      print day_folder
      dt = datetime.datetime.strptime(day_folder, '%Y%m%d')
      log.info('Processing day: {0} {1} {2} {3}'.format(day_folder, dt.year, dt.month, dt.day))
      full_day_path = '{0}{1}'.format(days_path,day_folder)


      ## dns Scores and dns Threat Investigation
      filename = '{0}/dns_scores.csv'.format(full_day_path) 
      if os.path.isfile(filename):

        log.info("Processing Dns Scores")
        staging_table_name = 'dns_scores_tmp'
        dest_table_name = 'dns_scores'

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT frame_time, unix_tstamp, frame_len, ip_dst, dns_qry_name, dns_qry_class, dns_qry_type, dns_qry_rcode, ml_score, tld, query_rep, hh, dns_qry_class_name, dns_qry_type_name, dns_qry_rcode_name, network_context FROM {5}.{6}".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)

        log.info("Processing dns Threat Investigation")
        staging_table_name = 'dns_scores_tmp'
        dest_table_name = 'dns_threat_investigation'

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT unix_tstamp, ip_dst, dns_qry_name, ip_sev, dns_sev FROM {5}.{6} WHERE ip_sev > 0 or dns_sev > 0;".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)

        
      # dns Dendro
      log.info("Processing Dns Dendro")
      staging_table_name = 'dns_dendro_tmp'
      dest_table_name = 'dns_dendro'
      pattern = 'dendro*.csv'
      dendro_files = fnmatch.filter(os.listdir(full_day_path), pattern)
      filename = '{0}/{1}'.format(full_day_path, pattern)

      if len(dendro_files) > 0:

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT unix_timestamp('{5}', 'yyyyMMMdd'), dns_a, dns_qry_name, ip_dst FROM {6}.{7};".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, day_folder, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)


      ## dns Edge
      log.info("Processing Dns Edge")
      staging_table_name = 'dns_edge_tmp'
      dest_table_name = 'dns_edge'
      pattern = 'edge*.csv'
      edge_files = fnmatch.filter(os.listdir(full_day_path), pattern)

      for file in edge_files:

        parts = (re.findall("edge-(\S+).csv", file)[0]).split('_')
        hh = int(parts[-2])
        mn = int(parts[-1])

        log.info("Processing File: {0} with HH: {1} and MN: {2}".format(file, hh, mn))

        log.info("Removing double quotes File: {0}".format(file))
        fixed_file = '{0}.fixed'.format(file)
        sed_cmd = "sed 's/\"//g' {0}/{1} > {0}/{2}".format(full_day_path, file, fixed_file)
        util.execute_cmd(sed_cmd, log)
        filename = '{0}/{1}'.format(full_day_path, fixed_file)

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT unix_timestamp(frame_time, 'MMMMM dd yyyy H:mm:ss.SSS z'), frame_len, ip_dst, ip_src, dns_qry_name, '', '0', '0', dns_a, {5}, dns_qry_class, dns_qry_type, dns_qry_rcode, '0' FROM {6}.{7};".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, hh, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)

        os.remove(filename)


      ##dns_storyboard
      log.info("Processing Dns Storyboard")
      staging_table_name = 'dns_storyboard_tmp'
      dest_table_name = 'dns_storyboard'
      filename = '{0}/threats.csv'.format(full_day_path)

      if os.path.isfile(filename):

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT ip_threat, dns_threat, title, text FROM {5}.{6};".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)


      # dns Threat Dendro
      log.info("Processing Dns Threat Dendro")
      staging_table_name = 'dns_threat_dendro_tmp'
      dest_table_name = 'dns_threat_dendro'
      pattern = 'threat-dendro*.csv'
      threat_dendro_files = fnmatch.filter(os.listdir(full_day_path), pattern)
      filename = '{0}/{1}'.format(full_day_path, pattern)

      for file in threat_dendro_files:

        ip = re.findall("threat-dendro-(\S+).csv", file)[0]
        log.info("Processing File: {0} with IP:{1}".format(file, ip))
        filename = '{0}/{1}'.format(full_day_path, file)

        load_cmd = "LOAD DATA LOCAL INPATH '{0}' OVERWRITE INTO TABLE {1}.{2};".format(filename, staging_db, staging_table_name)
        util.execute_hive_cmd(load_cmd, log)

        insert_cmd = "INSERT INTO {0}.{1} PARTITION (y={2}, m={3}, d={4}) SELECT '{5}', total, dns_qry_name, ip_dst FROM {6}.{7} WHERE dns_qry_name is not null;".format(dest_db, dest_table_name, dt.year, dt.month, dt.day, ip, staging_db, staging_table_name)
        util.execute_hive_cmd(insert_cmd, log)



  log.info("Dropping staging tables")
  util.execute_cmd('impala-shell -i {0} --var=dbname={1} -c -f drop_dns_migration_tables.hql'.format(impala_daemon, staging_db),log)

  log.info("Removing staging tables' path in HDFS")
  util.execute_cmd('hadoop fs -rm -r {0}/dns/'.format(hdfs_staging_path),log)

  log.info("Moving CSV data to backup folder")
  util.execute_cmd('mkdir {0}/data/backup/'.format(old_oa_path),log)
  util.execute_cmd('cp -r {0}/data/dns/ {0}/data/backup/'.format(old_oa_path),log)
  util.execute_cmd('rm -r {0}/data/dns/'.format(old_oa_path),log)


  log.info("Invalidating metadata in Impala to refresh tables content")
  util.execute_cmd('impala-shell -i {0} -q "INVALIDATE METADATA;"'.format(impala_daemon),log)


  log.info("Creating ipynb template structure and copying advanced mode and threat investigation ipynb templates for each pre-existing day in the new Spot location")
  ipynb_pipeline_path = '{0}/ipynb/dns/'.format(old_oa_path)
  if os.path.exists(ipynb_pipeline_path):
    for folder in os.listdir(ipynb_pipeline_path):
      log.info("Creating ipynb dns folders in new Spot locaiton: {0}".format(folder))
      util.execute_cmd('mkdir -p {0}/ipynb/dns/{1}/'.format(new_oa_path, folder),log)
      log.info("Copying advanced mode ipynb template")
      util.execute_cmd('cp {0}/oa/dns/ipynb_templates/Advanced_Mode_master.ipynb {0}/ipynb/dns/{1}/Advanced_Mode.ipynb'.format(new_oa_path, folder),log)
      log.info("Copying threat investigation ipynb template")
      util.execute_cmd('cp {0}/oa/dns/ipynb_templates/Threat_Investigation_master.ipynb {0}/ipynb/dns/{1}/Threat_Investigation.ipynb'.format(new_oa_path, folder),log)



if __name__=='__main__':
  main()

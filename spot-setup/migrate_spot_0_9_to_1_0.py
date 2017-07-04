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
from migration.utilities import util

pipelines = sys.argv[1]
old_oa_path = sys.argv[2]
staging_db = sys.argv[3]
hdfs_staging_path = sys.argv[4]
dest_db = sys.argv[5]
impala_daemon = sys.argv[6]


# Execution example:
#./migrate_old_data.py 'flow,dns,proxy' '/home/spotuser/incubator-spot_old/spot-oa' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'

def main():
  log = util.get_logger('SPOT.MIGRATE')

  cur_path = os.path.dirname(os.path.realpath(__file__))

  global old_oa_path 
  old_oa_path = old_oa_path.rstrip('/')
  old_spot_path = os.path.split(old_oa_path)[0]
  new_spot_path = os.path.split(cur_path)[0]
  new_oa_path = '{0}/spot-oa'.format(new_spot_path)
  
  log.info('Old Spot path: {0}'.format(old_spot_path))
  log.info('Old OA path: {0}'.format(old_oa_path))
  log.info('New Spot path: {0}'.format(new_spot_path))
  log.info('New OA path: {0}'.format(new_oa_path))


  log.info("Executing hdfs_setup.sh for initial setup") 
  util.execute_cmd('./hdfs_setup.sh',log)

  log.info("Updating /etc/spot.conf with new changes in Spot 1.0") 
  util.execute_cmd("migration/spot_conf_migration.py '/etc/spot.conf' '{0}/spot.conf'".format(cur_path),log)
  
  log.info("Copy ingest_conf.json file from old folder code to new Spot location") 
  util.execute_cmd('cp {0}/spot-ingest/ingest_conf.json {1}/spot-ingest/ingest_conf.json'.format(old_spot_path, new_spot_path),log)

  log.info("Copy Spot OA configuration files to new Spot location") 
  util.execute_cmd('cp {0}/oa/components/iana/iana_config.json {1}/oa/components/iana/iana_config.json'.format(old_oa_path, new_oa_path),log)
  util.execute_cmd('cp {0}/oa/components/reputation/reputation_config.json {1}/oa/components/reputation/reputation_config.json'.format(old_oa_path, new_oa_path),log)
  util.execute_cmd('cp {0}/oa/components/nc/nc_config.json {1}/oa/components/nc/nc_config.json'.format(old_oa_path, new_oa_path),log)
  util.execute_cmd('cp {0}/oa/components/data/engine.json {1}/oa/components/data/engine.json'.format(old_oa_path, new_oa_path),log)
  
  log.info("Copy Spot OA context files to new Spot location") 
  util.execute_cmd('cp {0}/context/iploc.csv {1}/context/iploc.csv'.format(old_oa_path, new_oa_path),log)
  util.execute_cmd('cp {0}/context/ipranges.csv {1}/context/ipranges.csv'.format(old_oa_path, new_oa_path),log)
  util.execute_cmd('cp {0}/context/networkcontext_1.csv {1}/context/networkcontext_1.csv'.format(old_oa_path, new_oa_path),log)

  log.info("Install browserify and uglifyjs using npm")
  os.chdir('{0}/ui'.format(new_oa_path))
  util.execute_cmd('sudo -H -E npm install -g browserify uglifyjs',log)
  util.execute_cmd('npm install',log)

  log.info("Install python-devel using yum")
  util.execute_cmd('sudo yum install python-devel -y',log)

  log.info("Install requirements using pip")
  os.chdir('{0}'.format(new_oa_path))
  util.execute_cmd('sudo -H -E pip install -r requirements.txt',log)


  log.info("Migrate data from pipelines")
  l_pipelines = pipelines.split(',')
  valid_pipelines = ('flow', 'dns', 'proxy')
  if len(l_pipelines) > 0:
    for pipe in l_pipelines:
      if pipe in valid_pipelines:
        log.info("Migrating {0} old data to new spot release".format(pipe))
        os.chdir('{0}/spot-setup/migration'.format(new_spot_path))
        util.execute_cmd("python migrate_old_{0}_data.py '{1}' '{2}' '{3}' '{4}' '{5}' ".format(pipe, old_oa_path, staging_db, hdfs_staging_path, dest_db, impala_daemon),log)
      else:
        log.error("Pipeline {0} is not valid. Valid pipelines are flow, dns or proxy".format(pipe))
  else:
    log.error("Pipeline arguments must be separated by commas")


if __name__=='__main__':
  main()

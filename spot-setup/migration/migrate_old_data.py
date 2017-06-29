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
from utilities import util

pipelines=sys.argv[1]
old_oa_path=sys.argv[2]
staging_db=sys.argv[3]
hdfs_staging_path=sys.argv[4]
dest_db = sys.argv[5]
impala_daemon = sys.argv[6]


# Execution example:
#./migrate_old_proxy_data.py '/home/spotuser/incubator-spot_old/spot-oa' 'spot_migration' '/user/spotuser/spot_migration/' 'migrated' 'node01'

def main():
  log = util.get_logger('SPOT.MIGRATE')

  cur_path = os.path.dirname(os.path.realpath(__file__))
  new_spot_path = os.path.split(os.path.split(cur_path)[0])[0]
  new_oa_path = '{0}/spot-oa'.format(new_spot_path)
  log.info('New Spot OA path: {0}'.format(new_oa_path))
  old_spot_path = os.path.split(old_oa_path)[0]


  log.info("Copy ingest_conf.json file from old folder code to new Spot location") 
  util.execute_cmd('cp {0}/spot-ingest/ingest_conf.json {1}/spot-ingest/ingest_conf.json'.format(old_spot_path, new_spot_path),log)

  log.info("Copy Spot OA configuration files to new Spot location") 
  util.execute_cmd('cp {0}/spot-oa/oa/components/iana/iana_config.json {1}/spot-oa/oa/components/iana/iana_config.json'.format(old_spot_path, new_spot_path),log)
  util.execute_cmd('cp {0}/spot-oa/oa/components/reputation/reputation_config.json {1}/spot-oa/oa/components/reputation/reputation_config.json'.format(old_spot_path, new_spot_path),log)
  util.execute_cmd('cp {0}/spot-oa/oa/components/nc/nc_config.json {1}/spot-oa/oa/components/nc/nc_config.json'.format(old_spot_path, new_spot_path),log)
  util.execute_cmd('cp {0}/spot-oa/oa/components/data/engine.json {1}/spot-oa/oa/components/data/engine.json'.format(old_spot_path, new_spot_path),log)
  
  log.info("Copy Spot OA context files to new Spot location") 
  util.execute_cmd('cp {0}/spot-oa/context/iploc.csv {1}/spot-oa/context/iploc.csv'.format(old_spot_path, new_spot_path),log)
  util.execute_cmd('cp {0}/spot-oa/context/ipranges.csv {1}/spot-oa/context/ipranges.csv'.format(old_spot_path, new_spot_path),log)

  log.info("Install browserify and uglifyjs using npm")
  os.chdir('{0}/ui'.format(new_oa_path))
  util.execute_cmd('sudo -H -E npm install -g browserify uglifyjs',log)

  log.info("Install requirements using pip")
  os.chdir('{0}'.format(new_oa_path))
  util.execute_cmd('sudo pip install -r requirements.txt',log)



  l_pipelines = pipelines.split(',')
  valid_pipelines = ('flow', 'dns', 'proxy')
  if len(l_pipelines) > 0:
    for pipe in l_pipelines:
      if pipe in valid_pipelines:
        os.chdir('{0}/spot-setup/migration'.format(new_spot_path))
        util.execute_cmd("python migrate_old_{0}_data.py '{1}' '{2}' '{3}' '{4}' '{5}' ".format(pipe, old_oa_path, staging_db, hdfs_staging_path, dest_db, impala_daemon),log)
      else:
        log.error("Pipeline {0} is not valid. Valid pipelines are flow, dns or proxy".format(pipe))
  else:
    log.error("Pipelines argument must be separated by commas")


if __name__=='__main__':
  main()

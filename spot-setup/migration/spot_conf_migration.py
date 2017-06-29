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

import ConfigParser
import sys
import os
from shutil import copyfile
from utilities import util


def main():

    if len(sys.argv[1:]) < 2:
        print "Please provide paths to: old_spot.conf , new_spot.conf"
        sys.exit(1)
        
    old_conf_file = sys.argv[1]
    new_conf_file = sys.argv[2]

    log = util.get_logger('SPOT.MIGRATE.CONF')

    old_path = os.path.dirname(os.path.realpath(old_conf_file))

    # create backup for the current configuration file.
    log.info("Create a backup of /etc/spot.conf before changing it") 
    util.execute_cmd('sudo cp {0} {1}/spot.conf.bkp_0_9'.format(old_conf_file, old_path),log)

    # create configuration objects.
    old_config = ConfigParser.ConfigParser()
    current_config = ConfigParser.ConfigParser()
    new_config = ConfigParser.ConfigParser()
    
    old_config.readfp(SecHead(open(old_conf_file)))
    current_config.readfp(SecHead(open(new_conf_file)))

    # create the new conf file.
    new_config.add_section('conf')
    for (k,v) in current_config.items("conf"):      
        if old_config.has_option('conf',k):
            new_config.set('conf',k, old_config.get('conf',k))
        else:
            new_config.set('conf',k,v)    
   
    new_path = os.path.dirname(os.path.realpath(new_conf_file)) 
    updated_conf_file = '{0}/spot.conf.new'.format(new_path)
    log.info("Generating merged configuration file in {0}".format(updated_conf_file)) 
    formatter(updated_conf_file,new_config)

    log.info("Updating original spot.conf with new and migrated variables and values") 
    util.execute_cmd('sudo cp {0} {1}/spot.conf'.format(updated_conf_file, old_path),log)
    util.execute_cmd('sudo chmod 0755 {0}/spot.conf'.format(old_path),log)


def formatter(conf_file,conf):

    with open(conf_file,'wb') as file:

        file.write("# node configuration\n")
        file.write("{0}={1}\n".format('UINODE',conf.get('conf','uinode')))
        file.write("{0}={1}\n".format('MLNODE',conf.get('conf','MLNODE')))
        file.write("{0}={1}\n".format('GWNODE',conf.get('conf','GWNODE')))
        file.write("{0}={1}\n".format('DBNAME',conf.get('conf','DBNAME')))

        file.write("# hdfs - base user and data source config\n")
        file.write("{0}={1}\n".format('HUSER',conf.get('conf','HUSER')))
        file.write("{0}={1}\n".format('NAME_NODE',conf.get('conf','NAME_NODE')))
        file.write("{0}={1}\n".format('WEB_PORT',conf.get('conf','WEB_PORT')))
        file.write("{0}={1}\n".format('DNS_PATH',conf.get('conf','DNS_PATH')))
        file.write("{0}={1}\n".format('PROXY_PATH',conf.get('conf','PROXY_PATH')))
        file.write("{0}={1}\n".format('FLOW_PATH',conf.get('conf','FLOW_PATH')))
        file.write("{0}={1}\n".format('HPATH',conf.get('conf','HPATH')))

        file.write("# impala config\n")
        file.write("{0}={1}\n".format('IMPALA_DEM',conf.get('conf','IMPALA_DEM')))
        file.write("{0}={1}\n".format('IMPALA_PORT',conf.get('conf','IMPALA_PORT')))

        file.write("# local fs base user and data source config\n")
        file.write("{0}={1}\n".format('LUSER',conf.get('conf','LUSER')))
        file.write("{0}={1}\n".format('LPATH',conf.get('conf','LPATH')))
        file.write("{0}={1}\n".format('RPATH',conf.get('conf','RPATH')))
        file.write("{0}={1}\n".format('LIPATH',conf.get('conf','LIPATH')))

        file.write("# suspicious connects config\n")
        file.write("{0}={1}\n".format('USER_DOMAIN',conf.get('conf','USER_DOMAIN')))
        file.write("{0}={1}\n".format('SPK_EXEC',conf.get('conf','SPK_EXEC')))
        file.write("{0}={1}\n".format('SPK_EXEC_MEM',conf.get('conf','SPK_EXEC_MEM')))
        file.write("{0}={1}\n".format('SPK_DRIVER_MEM',conf.get('conf','SPK_DRIVER_MEM')))
        file.write("{0}={1}\n".format('SPK_DRIVER_MAX_RESULTS',conf.get('conf','SPK_DRIVER_MAX_RESULTS')))
        file.write("{0}={1}\n".format('SPK_EXEC_CORES',conf.get('conf','SPK_EXEC_CORES')))
        file.write("{0}={1}\n".format('SPK_DRIVER_MEM_OVERHEAD',conf.get('conf','SPK_DRIVER_MEM_OVERHEAD')))
        file.write("{0}={1}\n".format('SPK_EXEC_MEM_OVERHEAD',conf.get('conf','SPK_EXEC_MEM_OVERHEAD')))
        file.write("{0}={1}\n".format('SPK_AUTO_BRDCST_JOIN_THR',conf.get('conf','SPK_AUTO_BRDCST_JOIN_THR')))
        file.write("{0}={1}\n".format('LDA_OPTIMIZER',conf.get('conf','LDA_OPTIMIZER')))
        file.write("{0}={1}\n".format('LDA_ALPHA',conf.get('conf','LDA_ALPHA')))
        file.write("{0}={1}\n".format('LDA_BETA',conf.get('conf','LDA_BETA')))
        file.write("{0}={1}\n".format('PRECISION',conf.get('conf','PRECISION')))
        file.write("{0}={1}\n".format('TOL',conf.get('conf','TOL')))
        file.write("{0}={1}\n".format('TOPIC_COUNT',conf.get('conf','TOPIC_COUNT')))
        file.write("{0}={1}\n".format('DUPFACTOR',conf.get('conf','DUPFACTOR')))

class SecHead(object):

    def __init__(self, fp):
         self.fp = fp
         self.sechead = '[conf]\n'

    def readline(self):
        if self.sechead:
            try:
                return self.sechead
            finally:
                self.sechead = None
        else:
            return self.fp.readline()

if __name__ == '__main__':
    main()



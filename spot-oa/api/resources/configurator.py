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
import os

def configuration():

    conf_file = "/etc/spot.conf"
    config = ConfigParser.ConfigParser()
    config.readfp(SecHead(open(conf_file)))
    return config

def db():
    conf = configuration()
    return conf.get('conf', 'DBNAME').replace("'","").replace('"','')

def impala():
    conf = configuration()
    return conf.get('conf', 'IMPALA_DEM'),conf.get('conf', 'IMPALA_PORT')

def hdfs():
    conf = configuration()
    name_node = conf.get('conf',"NAME_NODE")
    web_port = conf.get('conf',"WEB_PORT")
    hdfs_user = conf.get('conf',"HUSER")
    hdfs_user = hdfs_user.split("/")[-1].replace("'","").replace('"','')
    return name_node,web_port,hdfs_user

def spot():
    conf = configuration()
    return conf.get('conf',"HUSER").replace("'","").replace('"','')

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

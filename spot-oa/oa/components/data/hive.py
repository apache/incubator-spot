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

from subprocess import check_output

class Engine(object):

    def __init__(self,db,conf, pipeline):
        self._db = db
        self._pipeline = pipeline

    def query(self,query,output_file=None, delimiter=','):
        hive_config = "set mapred.max.split.size=1073741824;set hive.exec.reducers.max=10;set hive.cli.print.header=true;"
        
        del_format = "| sed 's/[\t]/{0}/g'".format(delimiter)
        if output_file: 
            hive_cmd = "hive -S -e \"{0} {1}\" {2} | sed '/INFO\|WARNING\|DEBUG/d' > {3}".format(hive_config,query,del_format,output_file)
        else:
            hive_cmd = "hive -S -e \"{0} {1}\"".format(hive_config,query)
        
        check_output(hive_cmd,shell=True)

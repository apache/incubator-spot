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
   
        self._daemon_node = conf['impala_daemon']
        self._db = db
        self._pipeline = pipeline
        impala_cmd = "impala-shell -i {0} --quiet -q 'INVALIDATE METADATA {1}.{2}'".format(self._daemon_node,self._db, self._pipeline)
        check_output(impala_cmd,shell=True)
    
        impala_cmd = "impala-shell -i {0} --quiet -q 'REFRESH {1}.{2}'".format(self._daemon_node,self._db, self._pipeline)
        check_output(impala_cmd,shell=True)

    def query(self,query,output_file=None,delimiter=","):

        if output_file:
            impala_cmd = "impala-shell -i {0} --quiet --print_header -B --output_delimiter='{1}' -q \"{2}\" -o {3}".format(self._daemon_node,delimiter,query,output_file)
        else:
            impala_cmd = "impala-shell -i {0} --quiet --print_header -B --output_delimiter='{1}' -q \"{2}\"".format(self._daemon_node,delimiter,query)

        check_output(impala_cmd,shell=True)

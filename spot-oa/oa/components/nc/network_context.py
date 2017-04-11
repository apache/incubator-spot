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

import csv
import os
import logging

NC = "network_context"

class NetworkContext(object):

	def __init__(self,config,logger=None):
    		
		self._nc_file_path = None		
		self._logger = logging.getLogger('OA.NC')  if logger else Util.get_logger('OA.NC',create_file=False)
					
		if NC in config:
			self._nc_file_path = config[NC]

		self._nc_dict = {}
		self._init_dicts()
    
	def _init_dicts(self):    
		if os.path.isfile(self._nc_file_path):    			
			with open(self._nc_file_path) as nc_file:
				csv_reader = csv.reader(nc_file)
				csv_reader.next()
				nc_rows = list(csv_reader)
				if len(nc_rows) > 0:
					self._nc_dict = dict([(x[0], x[1]) if len(x) > 2 else ("-", "-") for x in nc_rows])
    
	def get_nc(self, key):
		if key in self._nc_dict:
			return self._nc_dict[key]
		else:
			return ""

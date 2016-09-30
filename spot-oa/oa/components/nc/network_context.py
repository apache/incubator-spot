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
				self._nc_dict = dict([(x[0],x[1]) for x in nc_rows])
    
	def get_nc(self, key):
		if key in self._nc_dict:
			return self._nc_dict[key]
		else:
			return ""

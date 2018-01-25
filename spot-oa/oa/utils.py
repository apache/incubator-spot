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

import logging
import os
import subprocess
import csv
import sys
import ConfigParser

class Util(object):
	
	@classmethod
	def get_logger(cls,logger_name,create_file=False):
		

		# create logger for prd_ci
		log = logging.getLogger(logger_name)
		log.setLevel(level=logging.INFO)
		
		# create formatter and add it to the handlers
		formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
		
		if create_file:
				# create file handler for logger.
				fh = logging.FileHandler('oa.log')
				fh.setLevel(level=logging.DEBUG)
				fh.setFormatter(formatter)
		# reate console handler for logger.
		ch = logging.StreamHandler()
		ch.setLevel(level=logging.DEBUG)
		ch.setFormatter(formatter)

		# add handlers to logger.
		if create_file:
			log.addHandler(fh)

		log.addHandler(ch)
		return  log

	@classmethod
	def get_spot_conf(cls):
		
		conf_file = "/etc/spot.conf"
		config = ConfigParser.ConfigParser()
		config.readfp(SecHead(open(conf_file)))	

		return config
	
	@classmethod
	def create_oa_folders(cls,type,date):		

		# create date and ingest summary folder structure if they don't' exist.
		root_path = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
		data_type_folder = "{0}/data/{1}/{2}"
		if not os.path.isdir(data_type_folder.format(root_path,type,date)): os.makedirs(data_type_folder.format(root_path,type,date))
		if not os.path.isdir(data_type_folder.format(root_path,type,"ingest_summary")): os.makedirs(data_type_folder.format(root_path,type,"ingest_summary"))

		# create ipynb folders.
		ipynb_folder = "{0}/ipynb/{1}/{2}".format(root_path,type,date)
		if not os.path.isdir(ipynb_folder): os.makedirs(ipynb_folder)

		# retun path to folders.
		data_path = data_type_folder.format(root_path,type,date)
		ingest_path = data_type_folder.format(root_path,type,"ingest_summary")		
		return data_path,ingest_path,ipynb_folder
	
	@classmethod
	def get_ml_results_form_hdfs(cls,hdfs_file_path,local_path):

		# get results from hdfs.
		get_results_cmd = "hadoop fs -get {0} {1}/.".format(hdfs_file_path,local_path)
		subprocess.call(get_results_cmd,shell=True)
		return get_results_cmd

	@classmethod
	def read_results(cls,file,limit, delimiter=','):
		
		# read csv results.
		result_rows = []
		with open(file, 'rb') as results_file:
			csv_reader = csv.reader(results_file, delimiter = delimiter)
			for i in range(0, int(limit)):
				try:
					row = csv_reader.next()
				except StopIteration:
					return result_rows
				result_rows.append(row)
		return result_rows

	@classmethod
	def ip_to_int(self,ip):
		
		try:
			o = map(int, ip.split('.'))
			res = (16777216 * o[0]) + (65536 * o[1]) + (256 * o[2]) + o[3]
			return res    

		except ValueError:
			return None
	
	
	@classmethod
	def create_csv_file(cls,full_path_file,content,delimiter=','):   
		with open(full_path_file, 'w+') as u_file:
			writer = csv.writer(u_file, quoting=csv.QUOTE_NONE, delimiter=delimiter)
			writer.writerows(content)


	@classmethod
    	def cast_val(self,value):
       	    try: 
            	val = int(value) 
            except:
            	try:
                    val = float(value) 
            	except:
                    val = str(value) 
            return val    


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

class ProgressBar(object):

	def __init__(self,total,prefix='',sufix='',decimals=2,barlength=60):

		self._total = total
		self._prefix = prefix
		self._sufix = sufix
		self._decimals = decimals
		self._bar_length = barlength
		self._auto_iteration_status = 0

	def start(self):

		self._move_progress_bar(0)
	
	def update(self,iterator):
		
		self._move_progress_bar(iterator)

	def auto_update(self):

		self._auto_iteration_status += 1		
		self._move_progress_bar(self._auto_iteration_status)
	
	def _move_progress_bar(self,iteration):

		filledLength    = int(round(self._bar_length * iteration / float(self._total)))
		percents        = round(100.00 * (iteration / float(self._total)), self._decimals)
		bar             = '#' * filledLength + '-' * (self._bar_length - filledLength)	
		sys.stdout.write("{0} [{1}] {2}% {3}\r".format(self._prefix, bar, percents, self._sufix))		
		sys.stdout.flush()
		
		if iteration == self._total:print("\n")

		
	


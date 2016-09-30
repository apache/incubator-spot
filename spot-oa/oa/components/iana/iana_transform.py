import csv
import os
import logging
from utils import Util

COL_RCODE = 'dns_qry_rcode'
COL_QTYPE = 'dns_qry_type'
COL_CLASS = 'dns_qry_class' 
COL_PRESP = 'proxy_http_rcode' 

class IanaTransform(object):

    def __init__(self,config,logger=None):

        self._logger = logging.getLogger('OA.IANA')  if logger else Util.get_logger('OA.IANA',create_file=False)        
        if COL_CLASS in config:
            self._qclass_file_path = config[COL_CLASS]
        if COL_QTYPE in config:
            self._qtype_file_path = config[COL_QTYPE]
        if COL_RCODE in config:
            self._rcode_file_path = config[COL_RCODE]
        if COL_PRESP in config:
            self._http_rcode_file_path = config[COL_PRESP]

        self._qclass_dict = {}
        self._qtype_dict = {}
        self._rcode_dict = {} 
        self._http_rcode_dict = {}
        self._init_dicts()
        

    def _init_dicts(self):
        if os.path.isfile(self._qclass_file_path):
            with open(self._qclass_file_path, 'rb') as qclass_file:
                csv_reader = csv.reader(qclass_file)
                csv_reader.next()
                qclass_rows = list(csv_reader)
                d1 = dict([(x[0],x[2]) for x in qclass_rows])
                d2 = dict([(x[1],x[2]) for x in qclass_rows])
                self._qclass_dict.update(d1)
                self._qclass_dict.update(d2)
        if os.path.isfile(self._qtype_file_path):
            with open(self._qtype_file_path, 'rb') as qtype_file:
                csv_reader = csv.reader(qtype_file)
                csv_reader.next()
                qtype_rows = list(csv_reader)
                self._qtype_dict = dict([(x[1],x[0]) for x in qtype_rows])
        if os.path.isfile(self._rcode_file_path):
            with open(self._rcode_file_path) as rcode_file:
                csv_reader = csv.reader(rcode_file)
                csv_reader.next()
                rcode_rows = list(csv_reader)
                self._rcode_dict = dict([(x[0],x[1]) for x in rcode_rows])
        if os.path.isfile(self._http_rcode_file_path):
            with open(self._http_rcode_file_path) as http_resp_code:
                csv_reader = csv.reader(http_resp_code)
                csv_reader.next()
                presp_rows = list(csv_reader)
                self._http_rcode_dict = dict([(x[0],x[1]) for x in presp_rows])
               


    def get_name(self,key,column):  
        if column == COL_CLASS:
            if key in self._qclass_dict:
                return self._qclass_dict[key]
            else:
                return key
        if column == COL_QTYPE:
            if key in self._qtype_dict:
                return self._qtype_dict[key]
            else:
                return key
        if column == COL_RCODE:
            if key in self._rcode_dict:
                return self._rcode_dict[key]
            else:
                return key
        if column == COL_PRESP: 
            if key in self._http_rcode_dict:
                return self._http_rcode_dict[key]
            else:
                return key


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
import json
import shutil
import sys
import datetime
import csv, math
from tld import get_tld
import api.resources.impala_engine as impala
import api.resources.hdfs_client as HDFSClient
from collections import OrderedDict
from utils import Util
from components.data.data import Data
from components.iana.iana_transform import IanaTransform
from components.nc.network_context import NetworkContext 
from multiprocessing import Process
import pandas as pd 
import time

class OA(object):
    
    def __init__(self,date,limit=500,logger=None):

        self._initialize_members(date,limit,logger)

    def _initialize_members(self,date,limit,logger):
        
        # get logger if exists. if not, create new instance.
        self._logger = logging.getLogger('OA.DNS') if logger else Util.get_logger('OA.DNS',create_file=False)

        # initialize required parameters.
        self._scrtip_path = os.path.dirname(os.path.abspath(__file__))
        self._date = date
        self._table_name = "dns"
        self._dns_results = []
        self._limit = limit
        self._data_path = None
        self._ipynb_path = None
        self._ingest_summary_path = None
        self._dns_scores = []
        self._dns_scores_headers = []
        self._results_delimiter = '\t'
        self._details_limit = 250

        # get app configuration.
        self._spot_conf = Util.get_spot_conf()

        # get scores fields conf
        conf_file = "{0}/dns_conf.json".format(self._scrtip_path)
        self._conf = json.loads(open (conf_file).read(),object_pairs_hook=OrderedDict)

        # initialize data engine
        self._db = self._spot_conf.get('conf', 'DBNAME').replace("'", "").replace('"', '')


    def start(self):

        ####################
        start = time.time()
        ####################

        self._clear_previous_executions()
        self._create_folder_structure()
        self._add_ipynb()
        self._get_dns_results()
        self._add_tld_column()
        self._add_reputation()
        self._add_hh_column()
        self._add_iana()
        self._add_network_context()
        self._create_dns_scores()
        self._get_oa_details()
        self._ingest_summary()

        ##################
        end = time.time()
        print(end - start)
        ##################


    def _clear_previous_executions(self):

        self._logger.info("Cleaning data from previous executions for the day")
        yr = self._date[:4]
        mn = self._date[4:6]
        dy = self._date[6:]
        table_schema = []
        HUSER = self._spot_conf.get('conf', 'HUSER').replace("'", "").replace('"', '')
        table_schema=['suspicious', 'edge', 'dendro', 'threat_dendro', 'threat_investigation', 'storyboard', 'summary' ]

        for path in table_schema:
            HDFSClient.delete_folder("{0}/{1}/hive/oa/{2}/y={3}/m={4}/d={5}".format(HUSER,self._table_name,path,yr,int(mn),int(dy)),user="impala")
        impala.execute_query("invalidate metadata")

        #removes Feedback file
        HDFSClient.delete_folder("{0}/{1}/scored_results/{2}{3}{4}/feedback/ml_feedback.csv".format(HUSER,self._table_name,yr,mn,dy))
        #removes json files from the storyboard
        HDFSClient.delete_folder("{0}/{1}/oa/{2}/{3}/{4}/{5}".format(HUSER,self._table_name,"storyboard",yr,mn,dy))


    def _create_folder_structure(self):

        # create date folder structure if it does not exist.
        self._logger.info("Creating folder structure for OA (data and ipynb)")       
        self._data_path,self._ingest_summary_path,self._ipynb_path = Util.create_oa_folders("dns",self._date)
    

    def _add_ipynb(self):

        if os.path.isdir(self._ipynb_path):

            self._logger.info("Adding advanced mode IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Advanced_Mode_master.ipynb".format(self._scrtip_path),"{0}/Advanced_Mode.ipynb".format(self._ipynb_path))

            self._logger.info("Adding threat investigation IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Threat_Investigation_master.ipynb".format(self._scrtip_path),"{0}/Threat_Investigation.ipynb".format(self._ipynb_path))

        else:
            self._logger.error("There was a problem adding the IPython Notebooks, please check the directory exists.")


    def _get_dns_results(self):

        self._logger.info("Getting {0} Machine Learning Results from HDFS".format(self._date))
        dns_results = "{0}/dns_results.csv".format(self._data_path)

        # get hdfs path from conf file.
        HUSER = self._spot_conf.get('conf', 'HUSER').replace("'", "").replace('"', '')
        hdfs_path = "{0}/dns/scored_results/{1}/scores/dns_results.csv".format(HUSER,self._date)

        # get results file from hdfs.
        get_command = Util.get_ml_results_form_hdfs(hdfs_path,self._data_path)
        self._logger.info("{0}".format(get_command))

        if os.path.isfile(dns_results):

            # read number of results based in the limit specified.
            self._logger.info("Reading {0} dns results file: {1}".format(self._date,dns_results))
            self._dns_results = Util.read_results(dns_results,self._limit,self._results_delimiter)[:]
            if len(self._dns_results) == 0: self._logger.error("There are not dns results.");sys.exit(1)

        else:
            self._logger.error("There was an error getting ML results from HDFS")
            sys.exit(1)

        # add dns content.
        self._dns_scores = [ conn[:]  for conn in self._dns_results][:]


    def _move_time_stamp(self,dns_data):
        
        # return dns_data_ordered
        return dns_data        


    def _create_dns_scores(self):
        
        # get date parameters.
        yr = self._date[:4]
        mn = self._date[4:6]
        dy = self._date[6:]
        value_string = ""

        dns_scores_final = self._move_time_stamp(self._dns_scores)
        self._dns_scores = dns_scores_final

        for row in dns_scores_final:
            value_string += str(tuple(Util.cast_val(item) for item in row)) + ","

        load_into_impala = ("""
             INSERT INTO {0}.dns_scores partition(y={2}, m={3}, d={4}) VALUES {1}
        """).format(self._db, value_string[:-1], yr, mn, dy)
        impala.execute_query(load_into_impala)


    def _add_tld_column(self):
        qry_name_col = self._conf['dns_results_fields']['dns_qry_name'] 
        self._dns_scores = [conn + [ get_tld("http://" + str(conn[qry_name_col]), fail_silently=True) if "http://" not in str(conn[qry_name_col]) else get_tld(str(conn[qry_name_col]), fail_silently=True)] for conn in self._dns_scores ]


    def _add_reputation(self):

        # read configuration.
        reputation_conf_file = "{0}/components/reputation/reputation_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        self._logger.info("Reading reputation configuration file: {0}".format(reputation_conf_file))
        rep_conf = json.loads(open(reputation_conf_file).read())

        # initialize reputation services.
        self._rep_services = []
        self._logger.info("Initializing reputation services.")
        for service in rep_conf:               
            config = rep_conf[service]
            module = __import__("components.reputation.{0}.{0}".format(service), fromlist=['Reputation'])
            self._rep_services.append(module.Reputation(config,self._logger))
                
        # get columns for reputation.
        rep_cols = {}
        indexes =  [ int(value) for key, value in self._conf["add_reputation"].items()]  
        self._logger.info("Getting columns to add reputation based on config file: dns_conf.json".format())
        for index in indexes:
            col_list = []
            for conn in self._dns_scores:
                col_list.append(conn[index])            
            rep_cols[index] = list(set(col_list))

        # get reputation per column.
        self._logger.info("Getting reputation for each service in config")        
        rep_services_results = []
 
        if self._rep_services :
            for key,value in rep_cols.items():
                rep_services_results = [ rep_service.check(None,value) for rep_service in self._rep_services]
                rep_results = {}            
                for result in rep_services_results:            
                    rep_results = {k: "{0}::{1}".format(rep_results.get(k, ""), result.get(k, "")).strip('::') for k in set(rep_results) | set(result)}

                if rep_results:
                    self._dns_scores = [ conn + [ rep_results[conn[key]] ]   for conn in self._dns_scores  ]
                else:
                    self._dns_scores = [ conn + [""]   for conn in self._dns_scores  ]
        else:
            self._dns_scores = [ conn + [""]   for conn in self._dns_scores  ]


    def _add_hh_column(self):

        # add hh value column.
        dns_date_index = self._conf["dns_results_fields"]["frame_time"]
        self._dns_scores = [conn + [ filter(None,conn[dns_date_index].split(" "))[3].split(":")[0]] for conn in self._dns_scores  ]


    def _add_iana(self):
        iana_conf_file = "{0}/components/iana/iana_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(iana_conf_file):
            iana_config  = json.loads(open(iana_conf_file).read())
            dns_iana = IanaTransform(iana_config["IANA"])

            dns_qry_class_index = self._conf["dns_results_fields"]["dns_qry_class"]
            dns_qry_type_index = self._conf["dns_results_fields"]["dns_qry_type"]
            dns_qry_rcode_index = self._conf["dns_results_fields"]["dns_qry_rcode"]
            self._dns_scores = [ conn + [ dns_iana.get_name(conn[dns_qry_class_index],"dns_qry_class")] + [dns_iana.get_name(conn[dns_qry_type_index],"dns_qry_type")] + [dns_iana.get_name(conn[dns_qry_rcode_index],"dns_qry_rcode")] for conn in self._dns_scores ]
            
        else:            
            self._dns_scores = [ conn + ["","",""] for conn in self._dns_scores ] 


    def _add_network_context(self):
        nc_conf_file = "{0}/components/nc/nc_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(nc_conf_file):
            nc_conf = json.loads(open(nc_conf_file).read())["NC"]
            dns_nc = NetworkContext(nc_conf,self._logger)
            ip_dst_index = self._conf["dns_results_fields"]["ip_dst"]
            self._dns_scores = [ conn + [dns_nc.get_nc(conn[ip_dst_index])] for conn in self._dns_scores ]
        else:
            self._dns_scores = [ conn + [0] for conn in self._dns_scores ]


    def _get_oa_details(self):
        
        self._logger.info("Getting OA DNS suspicious details/dendro diagram")
        # start suspicious connects details process.
        p_sp = Process(target=self._get_suspicious_details)
        p_sp.start()

        # start chord diagram process.            
        p_dn = Process(target=self._get_dns_dendrogram)
        p_dn.start()

        p_sp.join()
        p_dn.join()


    def _get_suspicious_details(self):

        iana_conf_file = "{0}/components/iana/iana_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(iana_conf_file):
            iana_config  = json.loads(open(iana_conf_file).read())
            dns_iana = IanaTransform(iana_config["IANA"])
        
        for conn in self._dns_scores:

            timestamp = conn[self._conf["dns_score_fields"]["unix_tstamp"]]
            full_date = datetime.datetime.utcfromtimestamp(int(timestamp)).strftime('%Y-%m-%d %H:%M:%S')

            date = full_date.split(" ")[0].split("-")
            # get date parameters.
            yr = date[0]
            mn = date[1]
            dy = date[2]
            time = full_date.split(" ")[1].split(":")
            hh = int(time[0])

            dns_qry_name = conn[self._conf["dns_score_fields"]["dns_qry_name"]]
            self._get_dns_details(dns_qry_name,yr,mn,dy,hh,dns_iana)


    def _get_dns_details(self,dns_qry_name,year,month,day,hh,dns_iana):
        value_string = ""
        query_to_load =("""
            SELECT unix_tstamp,frame_len,ip_dst,ip_src,dns_qry_name,dns_qry_class,dns_qry_type,dns_qry_rcode,dns_a,h as hh
            FROM {0}.{1} WHERE y={2} AND m={3} AND d={4} AND dns_qry_name LIKE '%{5}%' AND h={6} LIMIT {7};
        """).format(self._db,self._table_name,year,month,day,dns_qry_name,hh,self._details_limit)

        try:
             dns_details = impala.execute_query(query_to_load)
        except:
            self._logger.info("WARNING. Details couldn't be retreived for {0}, skipping this step".format(dns_qry_name))
        else:
        # add IANA to results.
            update_rows = []
            if dns_iana:
                self._logger.info("Adding IANA translation to details results")

                dns_details = [ conn + (dns_iana.get_name(str(conn[5]),"dns_qry_class"),dns_iana.get_name(str(conn[6]),"dns_qry_type"),dns_iana.get_name(str(conn[7]),"dns_qry_rcode")) for conn in dns_details ]
            else:
                self._logger.info("WARNING: NO IANA configured.")
                dns_details = [ conn + ("","","") for conn in dns_details ]

            nc_conf_file = "{0}/components/nc/nc_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            if os.path.isfile(nc_conf_file):
                nc_conf = json.loads(open(nc_conf_file).read())["NC"]
                dns_nc = NetworkContext(nc_conf,self._logger)
                dns_details = [ conn + (dns_nc.get_nc(conn[2]),) for conn in dns_details ]
            else:
                dns_details = [ conn + (0,) for conn in dns_details ]

            for row in dns_details:
                value_string += str(tuple(item for item in row)) + ","

            if value_string != "":
                
                query_to_insert=("""
                    INSERT INTO {0}.dns_edge PARTITION (y={1}, m={2}, d={3}) VALUES ({4});
                """).format(self._db,year, month, day,  value_string[:-1])

                impala.execute_query(query_to_insert)


    def _get_dns_dendrogram(self):

        for conn in self._dns_scores:
            timestamp = conn[self._conf["dns_score_fields"]["unix_tstamp"]]
            full_date = datetime.datetime.utcfromtimestamp(int(timestamp)).strftime('%Y-%m-%d %H:%M:%S')

            date = full_date.split(" ")[0].split("-")
            # get date parameters.

            yr = date[0]
            mn = date[1]
            dy = date[2]
            ip_dst=conn[self._conf["dns_score_fields"]["ip_dst"]]

            query_to_load = ("""
                INSERT INTO TABLE {0}.dns_dendro PARTITION (y={2}, m={3},d={4})
                SELECT unix_tstamp, dns_a, dns_qry_name, ip_dst
                FROM (SELECT unix_tstamp, susp.ip_dst, susp.dns_qry_name, susp.dns_a
                    FROM {0}.{1} as susp WHERE susp.y={2} AND susp.m={3} AND susp.d={4} AND susp.ip_dst='{5}'
                LIMIT {6}) AS tmp GROUP BY dns_a, dns_qry_name, ip_dst, unix_tstamp
            """).format(self._db,self._table_name,yr,mn,dy,ip_dst,self._details_limit)

            impala.execute_query(query_to_load)

        
    def _ingest_summary(self):
        # get date parameters.
        yr = self._date[:4]
        mn = self._date[4:6]
        dy = self._date[6:]

        self._logger.info("Getting ingest summary data for the day")
        
        ingest_summary_cols = ["date","total"]		
        result_rows = []        
        df_filtered =  pd.DataFrame()

        query_to_load = ("""
            SELECT frame_time, COUNT(*) as total FROM {0}.{1}
            WHERE y={2} AND m={3} AND d={4} AND unix_tstamp IS NOT NULL
            AND frame_time IS NOT NULL AND frame_len IS NOT NULL
            AND dns_qry_name IS NOT NULL AND ip_src IS NOT NULL
            AND (dns_qry_class IS NOT NULL AND dns_qry_type IS NOT NULL
            AND dns_qry_rcode IS NOT NULL ) GROUP BY frame_time;
        """).format(self._db,self._table_name, yr, mn, dy)

        results = impala.execute_query_as_list(query_to_load)
        df = pd.DataFrame(results)

        # Forms a new dataframe splitting the minutes from the time column
        df_new = pd.DataFrame([["{0}-{1}-{2} {3}:{4}".format(yr, mn, dy,\
            val['frame_time'].replace("  "," ").split(" ")[3].split(":")[0].zfill(2),\
            val['frame_time'].replace("  "," ").split(" ")[3].split(":")[1].zfill(2)),\
            int(val['total']) if not math.isnan(val['total']) else 0 ] for key,val in df.iterrows()],columns = ingest_summary_cols)

        #Groups the data by minute
        sf = df_new.groupby(by=['date'])['total'].sum()
        df_per_min = pd.DataFrame({'date':sf.index, 'total':sf.values})

        df_final = df_filtered.append(df_per_min, ignore_index=True).to_records(False,False)

        if len(df_final) > 0:
            query_to_insert=("""
                INSERT INTO {0}.dns_ingest_summary PARTITION (y={1}, m={2}, d={3}) VALUES {4};
            """).format(self._db, yr, mn, dy, tuple(df_final))
            impala.execute_query(query_to_insert)
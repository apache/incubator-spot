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
        #self._table_name = "dns"
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
        self._table_name = self._spot_conf.get('conf', 'DNS_TABLE')
        self._engine = Data(self._db,self._table_name ,self._logger) 


    def start(self):

        ####################
        start = time.time()
        ####################

        self._create_folder_structure()
        self._add_ipynb()
        self._get_dns_results()
        self._add_tld_column()
        self._add_reputation()
        self._add_hh_and_severity()
        self._add_iana()
        self._add_network_context()
        self._create_dns_scores_csv()
        self._get_oa_details()
        self._ingest_summary()

        ##################
        end = time.time()
        print(end - start)
        ##################

    def _create_folder_structure(self):

        # create date folder structure if it does not exist.
        self._logger.info("Creating folder structure for OA (data and ipynb)")       
        self._data_path,self._ingest_summary_path,self._ipynb_path = Util.create_oa_folders("dns",self._date)
    
    def _add_ipynb(self):

        if os.path.isdir(self._ipynb_path):

            self._logger.info("Adding edge investigation IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Edge_Investigation_master.ipynb".format(self._scrtip_path),"{0}/Edge_Investigation.ipynb".format(self._ipynb_path))

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

         # validate files exists
        if os.path.isfile(dns_results):

            # read number of results based in the limit specified.
            self._logger.info("Reading {0} dns results file: {1}".format(self._date,dns_results))
            self._dns_results = Util.read_results(dns_results,self._limit,self._results_delimiter)[:]
            if len(self._dns_results) == 0: self._logger.error("There are not flow results.");sys.exit(1)

        else:
            self._logger.error("There was an error getting ML results from HDFS")
            sys.exit(1)

        # add headers.        
        self._logger.info("Adding headers")
        self._dns_scores_headers = [  str(key) for (key,value) in self._conf['dns_score_fields'].items() ]

        # add dns content.
        self._dns_scores = [ conn[:]  for conn in self._dns_results][:]       

    def _move_time_stamp(self,dns_data):
        
        for dns in dns_data:
            time_stamp = dns[1]
            dns.remove(time_stamp)
            dns.append(time_stamp)
        
        return dns_data        

    def _create_dns_scores_csv(self):
        
        dns_scores_csv = "{0}/dns_scores.csv".format(self._data_path)
        dns_scores_final =  self._move_time_stamp(self._dns_scores)
        dns_scores_final.insert(0,self._dns_scores_headers)
        Util.create_csv_file(dns_scores_csv,dns_scores_final)   

        # create bk file
        dns_scores_bu_csv = "{0}/dns_scores_bu.csv".format(self._data_path)
        Util.create_csv_file(dns_scores_bu_csv,dns_scores_final)     


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

                self._dns_scores = [ conn + [ rep_results[conn[key]] ]   for conn in self._dns_scores  ]
        else:
            self._dns_scores = [ conn + [""]   for conn in self._dns_scores  ]



    def _add_hh_and_severity(self):

        # add hh value and sev columns.
        dns_date_index = self._conf["dns_results_fields"]["frame_time"]
        self._dns_scores = [conn + [ filter(None,conn[dns_date_index].split(" "))[3].split(":")[0]] + [0] + [0] for conn in self._dns_scores  ]


    def _add_iana(self):

        iana_conf_file = "{0}/components/iana/iana_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(iana_conf_file):
            iana_config  = json.loads(open(iana_conf_file).read())
            dns_iana = IanaTransform(iana_config["IANA"])

            dns_qry_class_index = self._conf["dns_results_fields"]["dns_qry_class"]
            dns_qry_type_index = self._conf["dns_results_fields"]["dns_qry_type"]
            dns_qry_rcode_index = self._conf["dns_results_fields"]["dns_qry_rcode"]
            self._dns_scores = [ conn + [ dns_iana.get_name(conn[dns_qry_class_index],"dns_qry_class")] + [dns_iana.get_name(conn[dns_qry_type_index],"dns_qry_type")] + [ dns_iana.get_name(conn[dns_qry_rcode_index],"dns_qry_rcode") ] for conn in self._dns_scores ]
            
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
            self._dns_scores = [ conn + [""] for conn in self._dns_scores ]


    def _get_oa_details(self):
        
        self._logger.info("Getting OA DNS suspicious details/chord diagram")       
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
            # get data to query
            date=conn[self._conf["dns_score_fields"]["frame_time"]].split(" ")
            date = filter(None,date)

            if len(date) == 5:
                year=date[2]
                month=datetime.datetime.strptime(date[0], '%b').strftime('%m')
                day=date[1]                
                hh=conn[self._conf["dns_score_fields"]["hh"]]
                dns_qry_name = conn[self._conf["dns_score_fields"]["dns_qry_name"]]
                self._get_dns_details(dns_qry_name,year,month,day,hh,dns_iana)

    def _get_dns_details(self,dns_qry_name,year,month,day,hh,dns_iana):
                    
        limit = self._details_limit
        edge_file ="{0}/edge-{1}_{2}_00.csv".format(self._data_path,dns_qry_name.replace("/","-"),hh)
        edge_tmp  ="{0}/edge-{1}_{2}_00.tmp".format(self._data_path,dns_qry_name.replace("/","-"),hh)

        if not os.path.isfile(edge_file):
    
            dns_qry = ("SELECT frame_time,frame_len,ip_dst,ip_src,dns_qry_name,dns_qry_class,dns_qry_type,dns_qry_rcode,dns_a FROM {0}.{1} WHERE y={2} AND m={3} AND d={4} AND dns_qry_name LIKE '%{5}%' AND h={6} LIMIT {7};").format(self._db,self._table_name,year,month,day,dns_qry_name,hh,limit)
            
            # execute query
	    try:
                self._engine.query(dns_qry,edge_tmp)
            except:
		self._logger.error("ERROR. Edge file couldn't be created for {0}, skipping this step".format(dns_qry_name))
            else:
            # add IANA to results.
                if dns_iana:
                    update_rows = []
                    self._logger.info("Adding IANA translation to details results")
                    with open(edge_tmp) as dns_details_csv:
                        rows = csv.reader(dns_details_csv, delimiter=',', quotechar='|')
                        try:
                            next(rows)
                            update_rows = [[conn[0]] + [conn[1]] + [conn[2]] + [conn[3]] + [conn[4]] + [dns_iana.get_name(conn[5],"dns_qry_class")] + [dns_iana.get_name(conn[6],"dns_qry_type")] + [dns_iana.get_name(conn[7],"dns_qry_rcode")] + [conn[8]] for conn in rows]
                            update_rows = filter(None, update_rows)
                            header = [ "frame_time", "frame_len", "ip_dst","ip_src","dns_qry_name","dns_qry_class_name","dns_qry_type_name","dns_qry_rcode_name","dns_a" ]
                            update_rows.insert(0,header)
                        except IndexError:
                            pass

                else:
                    self._logger.info("WARNING: NO IANA configured.")

                    # create edge file.
                self._logger.info("Creating edge file:{0}".format(edge_file))
                with open(edge_file,'wb') as dns_details_edge:
                    writer = csv.writer(dns_details_edge, quoting=csv.QUOTE_ALL)
                    if update_rows:
                        writer.writerows(update_rows)
                    else:            
                        shutil.copy(edge_tmp,edge_file)           
                
                os.remove(edge_tmp)


    def _get_dns_dendrogram(self):
        limit = self._details_limit
        for conn in self._dns_scores:            
            date=conn[self._conf["dns_score_fields"]["frame_time"]].split(" ")
            date = filter(None,date)

            if len(date) == 5:
                year=date[2]
                month=datetime.datetime.strptime(date[0], '%b').strftime('%m')
                day=date[1]
                ip_dst=conn[self._conf["dns_score_fields"]["ip_dst"]]
                self._get_dendro(self._db,self._table_name,ip_dst,year,month,day, limit)


    def _get_dendro(self,db,table,ip_dst,year,month,day,limit):

        dendro_file = "{0}/dendro-{1}.csv".format(self._data_path,ip_dst)
        if not os.path.isfile(dendro_file):
            dndro_qry = ("SELECT dns_a, dns_qry_name, ip_dst FROM (SELECT susp.ip_dst, susp.dns_qry_name, susp.dns_a FROM {0}.{1} as susp WHERE susp.y={2} AND susp.m={3} AND susp.d={4} AND susp.ip_dst='{5}' LIMIT {6}) AS tmp GROUP BY dns_a, dns_qry_name, ip_dst").format(db,table,year,month,day,ip_dst,limit)
            # execute query
            self._engine.query(dndro_qry,dendro_file)

        
    def _ingest_summary(self):
        # get date parameters.
        yr = self._date[:4]
        mn = self._date[4:6]
        dy = self._date[6:]

        self._logger.info("Getting ingest summary data for the day")
        
        ingest_summary_cols = ["date","total"]		
        result_rows = []        
        df_filtered =  pd.DataFrame()

        ingest_summary_file = "{0}/is_{1}{2}.csv".format(self._ingest_summary_path,yr,mn)			
        ingest_summary_tmp = "{0}.tmp".format(ingest_summary_file)

        if os.path.isfile(ingest_summary_file):
        	df = pd.read_csv(ingest_summary_file, delimiter=',')
            #discards previous rows from the same date
        	df_filtered = df[df['date'].str.contains("{0}-{1}-{2}".format(yr, mn, dy)) == False] 
        else:
        	df = pd.DataFrame()
            
        # get ingest summary.
        ingest_summary_qry = ("SELECT frame_time, COUNT(*) as total "
                                    " FROM {0}.{1}"
                                    " WHERE y={2} AND m={3} AND d={4} "
                                    " AND unix_tstamp IS NOT NULL AND frame_time IS NOT NULL"
                                    " AND frame_len IS NOT NULL AND dns_qry_name IS NOT NULL"
                                    " AND ip_src IS NOT NULL " 
                                    " AND (dns_qry_class IS NOT NULL AND dns_qry_type IS NOT NULL AND dns_qry_rcode IS NOT NULL ) "
                                    " GROUP BY frame_time;") 

        ingest_summary_qry = ingest_summary_qry.format(self._db,self._table_name, yr, mn, dy)
        
        results_file = "{0}/results_{1}.csv".format(self._ingest_summary_path,self._date)
        self._engine.query(ingest_summary_qry,output_file=results_file,delimiter=",")


        if os.path.isfile(results_file):        
            df_results = pd.read_csv(results_file, delimiter=',') 

            # Forms a new dataframe splitting the minutes from the time column
            df_new = pd.DataFrame([["{0}-{1}-{2} {3}:{4}".format(yr, mn, dy,val['frame_time'].split(" ")[3].split(":")[0].zfill(2),val['frame_time'].split(" ")[3].split(":")[1].zfill(2)), int(val['total']) if not math.isnan(val['total']) else 0 ] for key,val in df_results.iterrows()],columns = ingest_summary_cols)
    
            #Groups the data by minute 
            sf = df_new.groupby(by=['date'])['total'].sum()
        
            df_per_min = pd.DataFrame({'date':sf.index, 'total':sf.values})
            
            df_final = df_filtered.append(df_per_min, ignore_index=True)
            df_final.to_csv(ingest_summary_tmp,sep=',', index=False)

            os.remove(results_file)
            os.rename(ingest_summary_tmp,ingest_summary_file)
        else:
            self._logger.info("No data found for the ingest summary")
        

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
import shutil
import os
import sys
import json
import numpy as np
import linecache, bisect
import csv
import pandas as pd

from collections import OrderedDict
from multiprocessing import Process
from utils import Util,ProgressBar
from components.data.data import Data
from components.geoloc.geoloc import GeoLocalization
from components.reputation.gti import gti

import time


class OA(object):

    def __init__(self,date,limit=500,logger=None):       
       
       self._initialize_members(date,limit,logger)
       
    def _initialize_members(self,date,limit,logger):
        
        # get logger if exists. if not, create new instance.
        self._logger = logging.getLogger('OA.Flow') if logger else Util.get_logger('OA.Flow',create_file=False)

        # initialize required parameters.
        self._scrtip_path = os.path.dirname(os.path.abspath(__file__))
        self._date = date
        #self._table_name = "flow"
        self._flow_results = []
        self._limit = limit
        self._data_path = None
        self._ipynb_path = None
        self._ingest_summary_path = None
        self._flow_scores = []
        self._results_delimiter = '\t'

        # get app configuration.
        self._spot_conf = Util.get_spot_conf()

        # get scores fields conf
        conf_file = "{0}/flow_conf.json".format(self._scrtip_path)
        self._conf = json.loads(open (conf_file).read(),object_pairs_hook=OrderedDict)     
 
        # initialize data engine
        self._db = self._spot_conf.get('conf', 'DBNAME').replace("'", "").replace('"', '')
        self._table_name = self._spot_conf.get('conf', 'FLOW_TABLE')
        self._engine = Data(self._db, self._table_name,self._logger)
                      
    def start(self):       
        
        ####################
        start = time.time()
        ####################

        self._create_folder_structure()
        self._add_ipynb()  
        self._get_flow_results()
        self._add_network_context()
        self._add_geo_localization()
        self._add_reputation()        
        self._create_flow_scores_csv()
        self._get_oa_details()
        self._ingest_summary()

        ##################
        end = time.time()
        print(end - start)
        ##################
       
    def _create_folder_structure(self):

        # create date folder structure if it does not exist.
        self._logger.info("Creating folder structure for OA (data and ipynb)")       
        self._data_path,self._ingest_summary_path,self._ipynb_path = Util.create_oa_folders("flow",self._date)

    def _add_ipynb(self):     

        if os.path.isdir(self._ipynb_path):

            self._logger.info("Adding edge investigation IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Edge_Investigation_master.ipynb".format(self._scrtip_path),"{0}/Edge_Investigation.ipynb".format(self._ipynb_path))

            self._logger.info("Adding threat investigation IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Threat_Investigation_master.ipynb".format(self._scrtip_path),"{0}/Threat_Investigation.ipynb".format(self._ipynb_path))

        else:
            self._logger.error("There was a problem adding the IPython Notebooks, please check the directory exists.")
            
    def _get_flow_results(self):
               
        self._logger.info("Getting {0} Machine Learning Results from HDFS".format(self._date))
        flow_results = "{0}/flow_results.csv".format(self._data_path)

        # get hdfs path from conf file 
        HUSER = self._spot_conf.get('conf', 'HUSER').replace("'", "").replace('"', '')
        hdfs_path = "{0}/flow/scored_results/{1}/scores/flow_results.csv".format(HUSER,self._date)
               
        # get results file from hdfs
        get_command = Util.get_ml_results_form_hdfs(hdfs_path,self._data_path)
        self._logger.info("{0}".format(get_command))

        # valdiate files exists
        if os.path.isfile(flow_results):

            # read number of results based in the limit specified.
            self._logger.info("Reading {0} flow results file: {1}".format(self._date,flow_results))
            self._flow_results = Util.read_results(flow_results,self._limit,self._results_delimiter)
            if len(self._flow_results) == 0: self._logger.error("There are not flow results.");sys.exit(1)

        else:
            self._logger.error("There was an error getting ML results from HDFS")
            sys.exit(1)

        # add headers.        
        self._logger.info("Adding headers based on configuration file: score_fields.json")
        self._flow_scores = [ [ str(key) for (key,value) in self._conf['flow_score_fields'].items()] ]

        # filter results add sev and rank.
        self._logger.info("Filtering required columns based on configuration")
        self._flow_scores.extend([ [0] +  [ conn[i] for i in self._conf['column_indexes_filter'] ] + [n] for n, conn in enumerate(self._flow_results) ])
     
    def _create_flow_scores_csv(self):

        flow_scores_csv = "{0}/flow_scores.csv".format(self._data_path)
        Util.create_csv_file(flow_scores_csv,self._flow_scores)

        # create bk file
        flow_scores_bu_csv = "{0}/flow_scores_bu.csv".format(self._data_path)
        Util.create_csv_file(flow_scores_bu_csv,self._flow_scores)  

    def _add_network_context(self):

        # use ipranges to see if the IPs are internals.         
        ip_ranges_file = "{0}/context/ipranges.csv".format(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

        # add new headers (srcIpInternal/destIpInternal).
        self._logger.info("Adding network context headers")
        flow_headers = self._flow_scores[0]
        flow_headers.extend(["srcIpInternal","destIpInternal"])

        # add values to srcIpInternal and destIpInternal.
        flow_scores = iter(self._flow_scores)
        next(flow_scores)

        if os.path.isfile(ip_ranges_file):

            self._logger.info("Start adding network context...")

            # get ranges from configuration file.
            self._logger.info("Reading network context file: {0}".format(ip_ranges_file))
            with open(ip_ranges_file, 'rb') as f:
                nc_ranges = [ map(Util.ip_to_int,line.strip('\n').split(',')) for line in f ]

            # get src and dst IPs
            src_ip_index = self._conf["flow_score_fields"]["srcIP"]
            dst_ip_index = self._conf["flow_score_fields"]["dstIP"]              
            
            # add networkcontext per connection.
            ip_internal_ranges = filter(None,nc_ranges)     
            self._logger.info("Adding networkcontext to suspicious connections.")
            self._flow_scores = [ conn + [ self._is_ip_internal(conn[src_ip_index],ip_internal_ranges)]+[ self._is_ip_internal(conn[dst_ip_index],ip_internal_ranges)] for conn in flow_scores]
           
        else:

            self._flow_scores = [ conn + ["",""] for conn in flow_scores ]            
            self._logger.info("WARNING: Network context was not added because the file ipranges.csv does not exist.")
        
        self._flow_scores.insert(0,flow_headers)

    def _is_ip_internal(self,ip, ranges):
        result = 0
        for row in ranges:
            if Util.ip_to_int(ip) >= row[0] and Util.ip_to_int(ip) <= row[1]: 
                result = 1
                break
        return result

        
    def _add_geo_localization(self):

        # use ipranges to see if the IPs are internals.         
        iploc_file = "{0}/context/iploc.csv".format(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

        # add new headers (srcIpInternal/destIpInternal).     
        self._logger.info("Adding geo localization headers")
        flow_headers = self._flow_scores[0]
        flow_headers.extend(["srcGeo","dstGeo","srcDomain","dstDomain"]) 

        # add values to srcIpInternal and destIpInternal.
        flow_scores = iter(self._flow_scores)
        next(flow_scores)

        if os.path.isfile(iploc_file):

            self._logger.info("Initializing geo localization component")
            geo = GeoLocalization(iploc_file,self._logger)
            
            src_ip_index = self._conf["flow_score_fields"]["srcIP"]
            dst_ip_index = self._conf["flow_score_fields"]["dstIP"] 

            self._logger.info("Adding geo localization...")
            self._flow_scores = []
            for conn in flow_scores:

                # get geo localizastin for src ip
                self._logger.debug("Searching geo for src ip {0}".format(conn[src_ip_index]))
                src_geo_dict = geo.get_ip_geo_localization(conn[src_ip_index])

                # get goe localization for dst ip.
                self._logger.debug("Searching geo for dst ip {0}".format(conn[dst_ip_index]))
                dst_geo_dict = geo.get_ip_geo_localization(conn[dst_ip_index])

                # adding columns to the current connection list.
                conn.extend([src_geo_dict["geo_loc"],dst_geo_dict["geo_loc"],src_geo_dict["domain"],dst_geo_dict["domain"]])
                self._flow_scores.extend([conn])                
        else:

            self._flow_scores = [ conn + ["","","",""] for conn in flow_scores ]   
            self._logger.info("WARNING: IP location was not added because the file {0} does not exist.".format(iploc_file))

        self._flow_scores.insert(0,flow_headers)       

    def _add_reputation(self):
        
        reputation_conf_file = "{0}/components/reputation/reputation_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        
        # add new headers (gtiSrcRep/gtiDstRep).
        self._logger.info("Adding reputation headers")
        flow_headers_rep = self._flow_scores[0]
        flow_headers_rep.extend(["srcIP_rep","dstIP_rep"])
        
        # read configuration.
        self._logger.info("Reading reputation configuration file: {0}".format(reputation_conf_file))
        rep_conf = json.loads(open(reputation_conf_file).read())
 
        if "gti" in rep_conf and os.path.isfile(rep_conf['gti']['refclient']):
            rep_conf = rep_conf['gti']
            # initialize gti module.
            self._logger.info("Initializing GTI component")
            flow_gti = gti.Reputation(rep_conf,self._logger)

            # get all src ips.
            src_ip_index = self._conf["flow_score_fields"]["srcIP"]
            dst_ip_index = self._conf["flow_score_fields"]["dstIP"]

            self._logger.info("Getting GTI reputation for src IPs")
            flow_scores_src = iter(self._flow_scores)
            next(flow_scores_src)

            # getting reputation for src IPs
            src_ips = [ conn[src_ip_index] for conn in flow_scores_src ]            
            src_rep_results = flow_gti.check(src_ips)

            self._logger.info("Getting GTI reputation for dst IPs")
            flow_scores_dst = iter(self._flow_scores)
            next(flow_scores_dst)

            # getting reputation for dst IPs            
            dst_ips = [  conn[dst_ip_index] for conn in flow_scores_dst ]
            dst_rep_results = flow_gti.check(dst_ips)

            flow_scores_final = iter(self._flow_scores)
            next(flow_scores_final)

            self._flow_scores = []
            flow_scores = [conn + [src_rep_results[conn[src_ip_index]]] + [dst_rep_results[conn[dst_ip_index]]]  for conn in  flow_scores_final ]
            self._flow_scores = flow_scores           
            
        else:
            # add values to gtiSrcRep and gtiDstRep.
            flow_scores = iter(self._flow_scores)
            next(flow_scores)

            self._flow_scores = [ conn + ["",""] for conn in flow_scores ]   
            self._logger.info("WARNING: IP reputation was not added. No refclient configured")  


        self._flow_scores.insert(0,flow_headers_rep)       

    def _get_oa_details(self):

        self._logger.info("Getting OA Flow suspicious details/chord diagram")
        # start suspicious connects details process.
        p_sp = Process(target=self._get_suspicious_details)
        p_sp.start()

        # start chord diagram process.
        p_ch = Process(target=self._get_chord_details)
        p_ch.start()

        p_sp.join()
        p_ch.join()

	
    def _get_suspicious_details(self,bar=None):
        
        # skip header
        sp_connections = iter(self._flow_scores)
        next(sp_connections)
      
        # loop connections.
        connections_added = [] 
        for conn in sp_connections:
            
            # validate if the connection's details are not already extracted.            
            if conn in connections_added:
                continue
            else:
                connections_added.append(conn)
           
            src_ip_index = self._conf["flow_score_fields"]["srcIP"]
            dst_ip_index = self._conf["flow_score_fields"]["dstIP"]

            # get src ip 
            sip = conn[src_ip_index]
            # get dst ip
            dip = conn[dst_ip_index]

            # get hour and date  (i.e. 2014-07-08 10:10:40)
            date_array = conn[1].split(' ')
            date_array_1 = date_array[0].split('-')
            date_array_2 = date_array[1].split(':')

            yr = date_array_1[0]                   
            dy = date_array_1[2]
            mh = date_array_1[1]

            hr = date_array_2[0]
            mm = date_array_2[1]
        
            # connection details query.
            sp_query = ("SELECT treceived as tstart,sip as srcip,dip as dstip,sport as sport,dport as dport,proto as proto,flag as flags,stos as TOS,ibyt as ibytes,ipkt as ipkts,input as input, output as output,rip as rip, obyt as obytes, opkt as opkts from {0}.{1} where ((sip='{2}' AND dip='{3}') or (sip='{3}' AND dip='{2}')) AND y={8} AND m={4} AND d={5} AND h={6} AND trminute={7} order by tstart limit 100")
                 
            # sp query.
            sp_query = sp_query.format(self._db,self._table_name,sip,dip,mh,dy,hr,mm,yr)

            # output file.
            edge_file = "{0}/edge-{1}-{2}-{3}-{4}.tsv".format(self._data_path,sip.replace(".","_"),dip.replace(".","_"),hr,mm)

            # execute query
            self._engine.query(sp_query,output_file=edge_file,delimiter="\\t")
    
    def _get_chord_details(self,bar=None):

         # skip header
        sp_connections = iter(self._flow_scores)
        next(sp_connections) 

        src_ip_index = self._conf["flow_score_fields"]["srcIP"]
        dst_ip_index = self._conf["flow_score_fields"]["dstIP"] 

        # get date parameters.
        yr = self._date[:4]
        mn = self._date[4:6]
        dy = self._date[6:]

        # get number of times each IP appears.
        srcdict = {}
        for conn in sp_connections:
            if conn[src_ip_index] in srcdict:srcdict[conn[src_ip_index]] += 1 
            else:srcdict[conn[src_ip_index]] = 1
            if conn[dst_ip_index] in srcdict:srcdict[conn[dst_ip_index]] += 1
            else:srcdict[conn[dst_ip_index]] = 1
        
        for (ip,n) in srcdict.items():            
            if n > 1:
                ip_list = []                
                sp_connections = iter(self._flow_scores)
                next(sp_connections)
                for row in sp_connections:                    
                    if ip == row[2] : ip_list.append(row[3])
                    if ip == row[3] :ip_list.append(row[2])    
                ips = list(set(ip_list))
             
                if len(ips) > 1:
                    ips_filter = (",".join(str("'{0}'".format(ip)) for ip in ips))
                    chord_file = "{0}/chord-{1}.tsv".format(self._data_path,ip.replace(".","_"))                     
                    ch_query = ("SELECT sip as srcip, dip as dstip, SUM(ibyt) as ibytes, SUM(ipkt) as ipkts from {0}.{1} where y={2} and m={3} \
                        and d={4} and ( (sip='{5}' and dip IN({6})) or (sip IN({6}) and dip='{5}') ) group by sip,dip")
                    self._engine.query(ch_query.format(self._db,self._table_name,yr,mn,dy,ip,ips_filter),chord_file,delimiter="\\t")

     
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
            df = pd.read_csv(ingest_summary_file, delimiter=',',names=ingest_summary_cols, skiprows=1)
            df_filtered = df[df['date'].str.contains("{0}-{1}-{2}".format(yr, mn, dy)) == False] 
        else:
            df = pd.DataFrame()
        
        # get ingest summary.           
        ingest_summary_qry = ("SELECT tryear, trmonth, trday, trhour, trminute, COUNT(*) total"
                            " FROM {0}.{1} "
                            " WHERE "
                            " y={2} "
                            " AND m={3} "
                            " AND d={4} "
                            " AND unix_tstamp IS NOT NULL AND sip IS NOT NULL "
                            " AND sport IS NOT NULL AND dip IS NOT NULL "
                            " AND dport IS NOT NULL AND ibyt IS NOT NULL "
                            " AND ipkt IS NOT NULL "
                            " GROUP BY tryear, trmonth, trday, trhour, trminute;")


        ingest_summary_qry = ingest_summary_qry.format(self._db,self._table_name, yr, mn, dy)
        results_file = "{0}/results_{1}.csv".format(self._ingest_summary_path,self._date)
        self._engine.query(ingest_summary_qry,output_file=results_file,delimiter=",")

        if os.path.isfile(results_file):
            result_rows = pd.read_csv(results_file, delimiter=',') 

            df_new = pd.DataFrame([["{0}-{1}-{2} {3}:{4}".format(yr, mn, dy, str(val['trhour']).zfill(2), str(val['trminute']).zfill(2)), int(val[5])] for key,val in result_rows.iterrows()],columns = ingest_summary_cols)						

            df_filtered = df_filtered.append(df_new, ignore_index=True)
            df_filtered.to_csv(ingest_summary_tmp,sep=',', index=False)

            os.remove(results_file)
            os.rename(ingest_summary_tmp,ingest_summary_file)
        else:
            self._logger.info("No data found for the ingest summary")

        

import logging
import os
import json
import shutil
import sys
import datetime
import csv
from collections import OrderedDict
from utils import Util
from components.data.data import Data
from components.iana.iana_transform import IanaTransform
from components.nc.network_context import NetworkContext
from multiprocessing import Process

import time
import md5


class OA(object):

    def __init__(self,date,limit=500,logger=None):

        self._initialize_members(date,limit,logger)

    def _initialize_members(self,date,limit,logger):

        # get logger if exists. if not, create new instance.
        self._logger = logging.getLogger('OA.PROXY') if logger else Util.get_logger('OA.PROXY',create_file=False)

        # initialize required parameters.
        self._scrtip_path = os.path.dirname(os.path.abspath(__file__))
        self._date = date
        self._table_name = "proxy"
        self._proxy_results = []
        self._limit = limit
        self._data_path = None
        self._ipynb_path = None
        self._ingest_summary_path = None
        self._proxy_scores = []
        self._proxy_scores_headers = []
        self._proxy_extra_columns = []
        self._results_delimiter = '\t'

        # get app configuration.
        self._spot_conf = Util.get_spot_conf()

        # get scores fields conf
        conf_file = "{0}/proxy_conf.json".format(self._scrtip_path)
        self._conf = json.loads(open (conf_file).read(),object_pairs_hook=OrderedDict)

        # initialize data engine
        self._db = self._spot_conf.get('conf', 'DBNAME').replace("'", "").replace('"', '')
        self._engine = Data(self._db, self._table_name,self._logger)


    def start(self):

        ####################
        start = time.time()
        ####################

        self._create_folder_structure()
        self._add_ipynb()
        self._get_proxy_results()
        self._add_reputation()
        self._add_severity()
        self._add_iana()
        self._add_network_context()
        self._add_hash()
        self._create_proxy_scores_csv()
        self._get_oa_details()


        ##################
        end = time.time()
        print(end - start)
        ##################

    def _create_folder_structure(self):

        # create date folder structure if it does not exist.
        self._logger.info("Creating folder structure for OA (data and ipynb)")
        self._data_path,self._ingest_summary_path,self._ipynb_path = Util.create_oa_folders("proxy",self._date)


    def _add_ipynb(self):

        if os.path.isdir(self._ipynb_path):

            self._logger.info("Adding edge investigation IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Edge_Investigation_master.ipynb".format(self._scrtip_path),"{0}/Edge_Investigation.ipynb".format(self._ipynb_path))

            self._logger.info("Adding threat investigation IPython Notebook")
            shutil.copy("{0}/ipynb_templates/Threat_Investigation_master.ipynb".format(self._scrtip_path),"{0}/Threat_Investigation.ipynb".format(self._ipynb_path))

        else:
            self._logger.error("There was a problem adding the IPython Notebooks, please check the directory exists.")


    def _get_proxy_results(self):

        self._logger.info("Getting {0} Machine Learning Results from HDFS".format(self._date))
        proxy_results = "{0}/proxy_results.csv".format(self._data_path)

        # get hdfs path from conf file.
        HUSER = self._spot_conf.get('conf', 'HUSER').replace("'", "").replace('"', '')
        hdfs_path = "{0}/proxy/scored_results/{1}/scores/proxy_results.csv".format(HUSER,self._date)

        # get results file from hdfs.
        get_command = Util.get_ml_results_form_hdfs(hdfs_path,self._data_path)
        self._logger.info("{0}".format(get_command))

         # valdiate files exists
        if os.path.isfile(proxy_results):

            # read number of results based in the limit specified.
            self._logger.info("Reading {0} proxy results file: {1}".format(self._date,proxy_results))
            self._proxy_results = Util.read_results(proxy_results,self._limit,self._results_delimiter)[:]
            if len(self._proxy_results) == 0: self._logger.error("There are not proxy results.");sys.exit(1)
        else:
            self._logger.error("There was an error getting ML results from HDFS")
            sys.exit(1)

        # add headers.
        self._logger.info("Adding headers")
        self._proxy_scores_headers = [  str(key) for (key,value) in self._conf['proxy_score_fields'].items() ]

        self._proxy_scores = self._proxy_results[:]


    def _create_proxy_scores_csv(self):

        proxy_scores_csv = "{0}/proxy_scores.tsv".format(self._data_path)
        proxy_scores_final = self._proxy_scores[:];
        proxy_scores_final.insert(0,self._proxy_scores_headers)
        Util.create_csv_file(proxy_scores_csv,proxy_scores_final, self._results_delimiter)

        # create bk file
        proxy_scores_bu_csv = "{0}/proxy_scores_bu.tsv".format(self._data_path)
        Util.create_csv_file(proxy_scores_bu_csv,proxy_scores_final, self._results_delimiter)


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
        self._logger.info("Getting columns to add reputation based on config file: proxy_conf.json".format())
        for index in indexes:
            col_list = []
            for conn in self._proxy_scores:
                col_list.append(conn[index])
            rep_cols[index] = list(set(col_list))

        # get reputation per column.
        self._logger.info("Getting reputation for each service in config")
        rep_services_results = []
        for key,value in rep_cols.items():
            rep_services_results = [ rep_service.check(None,value,True) for rep_service in self._rep_services]
            rep_results = {}

            for result in rep_services_results:
                rep_results = {k: "{0}::{1}".format(rep_results.get(k, ""), result.get(k, "")).strip('::') for k in set(rep_results) | set(result)}

            self._proxy_scores = [ conn + [ rep_results[conn[key]] ]   for conn in self._proxy_scores  ]

    def _add_severity(self):
        # Add severity column
        self._proxy_scores = [conn + [0] for conn in self._proxy_scores]


    def _add_iana(self):

        iana_conf_file = "{0}/components/iana/iana_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(iana_conf_file):
            iana_config  = json.loads(open(iana_conf_file).read())
            proxy_iana = IanaTransform(iana_config["IANA"])
            proxy_rcode_index = self._conf["proxy_score_fields"]["respcode"]
            self._proxy_scores = [ conn + [ proxy_iana.get_name(conn[proxy_rcode_index],"proxy_http_rcode")] for conn in self._proxy_scores ]
        else:
            self._proxy_scores = [ conn + [""] for conn in self._proxy_scores ]


    def _add_network_context(self):

        nc_conf_file = "{0}/components/nc/nc_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(nc_conf_file):
            nc_conf = json.loads(open(nc_conf_file).read())["NC"]
            proxy_nc = NetworkContext(nc_conf,self._logger)
            ip_dst_index = self._conf["proxy_score_fields"]["clientip"]
            self._proxy_scores = [ conn + [proxy_nc.get_nc(conn[ip_dst_index])] for conn in self._proxy_scores ]

        else:
            self._proxy_scores = [ conn + [""] for conn in self._proxy_scores ]


    def _add_hash(self):
        #A hash string is generated to be used as the file name for the edge files.
        #These fields are used for the hash creation, so this combination of values is treated as
        #a 'unique' connection
        cip_index = self._conf["proxy_score_fields"]["clientip"]
        uri_index = self._conf["proxy_score_fields"]["fulluri"]
        tme_index = self._conf["proxy_score_fields"]["p_time"]

        self._proxy_scores = [conn + [str( md5.new(str(conn[cip_index]) + str(conn[uri_index])).hexdigest() + str((conn[tme_index].split(":"))[0]) )] for conn in self._proxy_scores]


    def _get_oa_details(self):

        self._logger.info("Getting OA Proxy suspicious details")
        # start suspicious connects details process.
        p_sp = Process(target=self._get_suspicious_details)
        p_sp.start()

        # p_sp.join()

    def _get_suspicious_details(self):
        hash_list = []
        iana_conf_file = "{0}/components/iana/iana_config.json".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
        if os.path.isfile(iana_conf_file):
            iana_config  = json.loads(open(iana_conf_file).read())
            proxy_iana = IanaTransform(iana_config["IANA"])

        for conn in self._proxy_scores:
            conn_hash = conn[self._conf["proxy_score_fields"]["hash"]]
            if conn_hash not in hash_list:
                hash_list.append(conn_hash)
                clientip = conn[self._conf["proxy_score_fields"]["clientip"]]
                fulluri = conn[self._conf["proxy_score_fields"]["fulluri"]]
                date=conn[self._conf["proxy_score_fields"]["p_date"]].split('-')
                if len(date) == 3:
                    year=date[0]
                    month=date[1].zfill(2)
                    day=date[2].zfill(2)
                    hh=(conn[self._conf["proxy_score_fields"]["p_time"]].split(":"))[0]
                    self._get_proxy_details(fulluri,clientip,conn_hash,year,month,day,hh,proxy_iana)


    def _get_proxy_details(self,fulluri,clientip,conn_hash,year,month,day,hh,proxy_iana):

        limit = 250
        output_delimiter = '\t'
        edge_file ="{0}/edge-{1}-{2}.tsv".format(self._data_path,clientip,conn_hash)
        edge_tmp  ="{0}/edge-{1}-{2}.tmp".format(self._data_path,clientip,conn_hash)

        if not os.path.isfile(edge_file):
            proxy_qry = ("SELECT p_date, p_time, clientip, host, webcat, respcode, reqmethod, useragent, resconttype, \
                referer, uriport, serverip, scbytes, csbytes, fulluri FROM {0}.{1} WHERE y=\'{2}\' AND m=\'{3}\' AND d=\'{4}\' AND \
                h=\'{5}\' AND fulluri =\'{6}\' AND clientip = \'{7}\' LIMIT {8};").format(self._db,self._table_name, year,month,day,hh,fulluri,clientip,limit)

            # execute query
            self._engine.query(proxy_qry,edge_tmp,output_delimiter)
            # add IANA to results.
            self._logger.info("Adding IANA translation to details results")
            with open(edge_tmp) as proxy_details_csv:
                rows = csv.reader(proxy_details_csv, delimiter=output_delimiter,quotechar='"')
                next(proxy_details_csv)
                update_rows = [[conn[0]] + [conn[1]] + [conn[2]] + [conn[3]] + [conn[4]] + [proxy_iana.get_name(conn[5],"proxy_http_rcode") if proxy_iana else conn[5]] + [conn[6]] + [conn[7]] + [conn[8]] + [conn[9]] + [conn[10]] + [conn[11]] + [conn[12]] + [conn[13]] + [conn[14]] if len(conn) > 0 else [] for conn in rows]
                update_rows = filter(None, update_rows)
                header = ["p_date","p_time","clientip","host","webcat","respcode","reqmethod","useragent","resconttype","referer","uriport","serverip","scbytes","csbytes","fulluri"]
                update_rows.insert(0,header)

		# due an issue with the output of the query.
		update_rows = [ [ w.replace('"','') for w in l ] for l in update_rows ]
	

            # create edge file.
            self._logger.info("Creating edge file:{0}".format(edge_file))
            with open(edge_file,'wb') as proxy_details_edge:
                writer = csv.writer(proxy_details_edge, quoting=csv.QUOTE_NONE, delimiter=output_delimiter)
                if update_rows:
                    writer.writerows(update_rows)
                else:
                    shutil.copy(edge_tmp,edge_file)

            try:
                os.remove(edge_tmp)
            except OSError:
                pass


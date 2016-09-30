from utils import Util
import json
import os
import logging
import inspect

class Data(object):

    def __init__(self,db, pipeline,logger=None):

        # get logger if exists. if not, create new instance.
        self._logger = logging.getLogger('OA.DATA')  if logger else Util.get_logger('OA.DATA',create_file=False)       
        self._initialize_engine(db, pipeline)

    def _initialize_engine(self,db, pipeline):

        # read engine configuration.
        data_conf_file = "{0}/engine.json".format(os.path.dirname(os.path.abspath(__file__)))

        self._logger.info("Reading data component configuration: {0}".format(data_conf_file))
        self._engine_conf = json.loads(open (data_conf_file).read())       
        self._engine_name = self._engine_conf["oa_data_engine"]

        # import configured data engine.
        self._logger.info("Initializating {0} instance".format(self._engine_name))        
        module = __import__("components.data.{0}".format(self._engine_name),fromlist=['Engine'])

        # start data engine with configuration.
        self._engine = module.Engine(db,self._engine_conf[self._engine_name], pipeline)

    def query(self,query,output_file=None,delimiter=","):

        self._logger.debug("Executing query: {0}".format(query)) 
        self._engine.query(query,output_file,delimiter)
     
        
       


        

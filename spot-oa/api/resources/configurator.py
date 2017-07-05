import ConfigParser
import os

def configuration():

    conf_file = "/etc/spot.conf"
    config = ConfigParser.ConfigParser()
    config.readfp(SecHead(open(conf_file)))
    return config

def db():
    conf = configuration()
    return conf.get('conf', 'DBNAME').replace("'","").replace('"','')

def impala():
    conf = configuration()
    return conf.get('conf', 'IMPALA_DEM'),conf.get('conf', 'IMPALA_PORT')

def hdfs():
    conf = configuration()
    name_node = conf.get('conf',"NAME_NODE")
    web_port = conf.get('conf',"WEB_PORT")
    hdfs_user = conf.get('conf',"HUSER")
    hdfs_user = hdfs_user.split("/")[-1].replace("'","").replace('"','')
    return name_node,web_port,hdfs_user

def spot():
    conf = configuration()
    return conf.get('conf',"HUSER").replace("'","").replace('"','')

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

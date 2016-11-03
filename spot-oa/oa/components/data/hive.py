from subprocess import check_output

class Engine(object):

    def __init__(self,db,conf, pipeline):
        self._db = db
        self._pipeline = pipeline

    def query(self,query,output_file=None, delimiter=','):
        hive_config = "set mapred.max.split.size=1073741824;set hive.exec.reducers.max=10;set hive.cli.print.header=true; "

        if output_file:
            hive_cmd = "hive -S -e \"{0} {1}\" | sed 's/[\t]/,/g' | sed '/INFO\|WARNING\|DEBUG/d' > {2}".format(hive_config,query,output_file)
        else:
            hive_cmd = "hive -S -e \"{0} {1}\"".format(hive_config,delimiter,query)
        
        check_output(hive_cmd,shell=True)

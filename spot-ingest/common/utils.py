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

'''
    Internal subroutines for e.g. executing commands and initializing
:class:`logging.Logger` objects.
'''

import logging
import os
import subprocess
import sys

from logging.handlers   import RotatingFileHandler
#from watchdog.observers import Observer
#from watchdog.events    import FileSystemEventHandler


class Util(object):
    '''
        Utility methods.
    '''

    @classmethod
    def call(cls, cmd, shell=False):
        '''
            Run command with arguments, wait to complete and return ``True`` on success.

        :param cls: The class as implicit first argument.
        :param cmd: Command string to be executed.
        :returns  : ``True`` on success, otherwise ``None``.
        :rtype    : ``bool``
        '''
        logger = logging.getLogger('SPOT.INGEST.COMMON.UTIL')
        logger.debug('Execute command: {0}'.format(cmd))

        try:
            subprocess.call(cmd, shell=shell)
            return True

        except Exception as exc:
            logger.error('[{0}] {1}'.format(exc.__class__.__name__, exc.message))

    @classmethod
    def get_logger(cls, name, level='INFO', filepath=None, fmt=None):
        '''
            Return a new logger or an existing one, if any.

        :param cls     : The class as implicit first argument.
        :param name    : Return a logger with the specified name.
        :param filepath: Path of the file, where logs will be saved. If it is not set,
                         redirects the log stream to `sys.stdout`.
        :param fmt     : A format string for the message as a whole, as well as a format
                         string for the date/time portion of the message.
                         Default: '%(asctime)s %(levelname)-8s %(name)-34s %(message)s'
        :rtype         : :class:`logging.Logger`
        '''
        logger = logging.getLogger(name)
        # .............................if logger already exists, return it
        if logger.handlers: return logger

        if filepath:
            # .........................rotate log file (1 rotation per 512KB
            handler  = RotatingFileHandler(filepath, maxBytes=524288, backupCount=8)
        else:
            handler  = logging.StreamHandler(sys.stdout)

        fmt = fmt or '%(asctime)s %(levelname)-8s %(name)-34s %(message)s'
        handler.setFormatter(logging.Formatter(fmt))

        try:
            logger.setLevel(getattr(logging, level.upper() if level else 'INFO'))
        except: logger.setLevel(logging.INFO)

        logger.addHandler(handler)
        return logger

    @classmethod
    def popen(cls, cmd, cwd=None, raises=False):
        '''
            Execute the given command string in a new process. Send data to stdin and
        read data from stdout and stderr, until end-of-file is reached.

        :param cls     : The class as implicit first argument.
        :param cwd     : If it is set, then the child's current directory will be change
                         to `cwd` before it is executed.
        :param raises  : If ``True`` and stderr has data, it raises an ``OSError`` exception.
        :returns       : The output of the given command; pair of (stdout, stderr).
        :rtype         : ``tuple``
        :raises OSError: If `raises` and stderr has data.
        '''
        parser   = lambda x: [] if x == '' else [y.strip() for y in x.strip().split('\n')]
        process  = subprocess.Popen(cmd, shell=True, universal_newlines=True,
                    stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        out, err = process.communicate()

        # .............................trim lines and remove the empty ones
        _stdout  = [x for x in parser(out) if bool(x)]
        _stderr  = [x for x in parser(err) if bool(x)]

        if _stderr and raises:
            raise OSError('\n'.join(_stderr))

        return _stdout, _stderr

    @classmethod
    def remove_kafka_topic(cls,zk,topic,logger):
        rm_kafka_topic = "kafka-topics --delete --zookeeper {0} --topic {1}".format(zk,topic)
        try:
            logger.info("SPOT.Utils: Executing: {0}".format(rm_kafka_topic))
            subprocess.call(rm_kafka_topic,shell=True)

        except subprocess.CalledProcessError as e:
            logger.error("SPOT.Utils: There was an error executing: {0}".format(e.cmd))
            sys.exit(1)

    @classmethod
    def validate_parameter(cls,parameter,message,logger):
        if parameter == None or parameter == "":
            logger.error(message)
            sys.exit(1)

    @classmethod
    def creat_hdfs_folder(cls,hdfs_path,logger):
        hadoop_create_folder="hadoop fs -mkdir -p {0}".format(hdfs_path)
        logger.info("SPOT.Utils: Creating hdfs folder: {0}".format(hadoop_create_folder))
        subprocess.call(hadoop_create_folder,shell=True)

    @classmethod
    def load_to_hdfs(cls,file_local_path,file_hdfs_path,logger):
        # move file to hdfs.
        load_to_hadoop_script = "hadoop fs -moveFromLocal {0} {1}".format(file_local_path,file_hdfs_path)
        logger.info("SPOT.Utils: Loading file to hdfs: {0}".format(load_to_hadoop_script))
        subprocess.call(load_to_hadoop_script,shell=True)

    @classmethod
    def create_watcher(cls,collector_path,new_file,logger):
        from watchdog.observers import Observer
        logger.info("Creating collector watcher")
        event_handler = new_file
        observer = Observer()
        observer.schedule(event_handler,collector_path)

        return observer

    @classmethod
    def execute_cmd(cls,command,logger):

        try:
            logger.info("SPOT.Utils: Executing: {0}".format(command))
            subprocess.call(command,shell=True)

        except subprocess.CalledProcessError as e:
            logger.error("SPOT.Utils: There was an error executing: {0}".format(e.cmd))
            sys.exit(1)

    @classmethod
    def validate_data_source(cls,pipeline_type):
        dirs = os.walk("{0}/pipelines/".format(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))).next()[1]
        is_type_ok = True if pipeline_type in dirs else False
        return is_type_ok


#class NewFileEvent(FileSystemEventHandler):
#
#    pipeline_instance = None
#    def __init__(self,pipeline_instance):
#        self.pipeline_instance = pipeline_instance
#
#    def on_moved(self,event):
#        if not event.is_directory:
#            self.pipeline_instance.new_file_detected(event.dest_path)
#
#    def on_created(self,event):
#        if not event.is_directory:
#            self.pipeline_instance.new_file_detected(event.src_path)

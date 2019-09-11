#!/usr/bin/env python
# -*- coding: utf-8 -*-
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
    Start Spark Job to listen a Kafka topic and register consumed segments into Hive.
'''

import json
import os
import pipelines
import sys

from argparse        import ArgumentParser
from common.kerberos import Kerberos
from common.utils    import Util

def spark_job(script_file, **kwargs):
    '''
        Run given script file by appling it as a Spark Job.
    '''
    spark_job  = 'spark2-submit --master {0}'.format(kwargs.pop('master'))
    spark_job += ' --deploy-mode {0}'.format(kwargs.pop('deploy_mode'))
    spark_job += ' --py-files {0}'.format(kwargs.pop('py_files'))

    if 'driver_memory' in kwargs.keys():
        spark_job += ' --driver-memory {0}'.format(kwargs.pop('driver_memory'))

    if 'spark_exec' in kwargs.keys():
        spark_job += ' --num-executors {0}'.format(kwargs.pop('spark_exec'))

    if 'spark_executor_memory' in kwargs.keys():
        spark_job += ' --conf spark.executor.memory={0}'.format(kwargs.pop('spark_executor_memory'))

    if 'spark_executor_cores' in kwargs.keys():
        spark_job += ' --conf spark.executor.cores={0}'.format(kwargs.pop('spark_executor_cores'))

    spark_job += ' {0}'.format(os.path.abspath(script_file))

    if 'spark_batch_size' in kwargs.keys():
        spark_job += ' -b {0}'.format(kwargs.pop('spark_batch_size'))

    spark_job += ' -d {0}'.format(kwargs.pop('database'))

    if kwargs['group_id'] is not None:
        spark_job += ' -g {0}'.format(kwargs.pop('group_id'))

    spark_job += ' -l {0}'.format(kwargs.pop('log_level'))

    if kwargs['app_name'] is not None:
        spark_job += ' -n {0}'.format(kwargs.pop('app_name'))

    spark_job += ' -p {0}'.format(kwargs.pop('partitions'))
    spark_job += ' -t {0}'.format(kwargs.pop('type'))
    spark_job += ' --topic {0}'.format(kwargs.pop('topic'))
    spark_job += ' --zkquorum {0}'.format(kwargs.pop('zkquorum'))

    if kwargs['redirect_spark_logs'] is not None:
        spark_job += ' 2>{0}'.format(kwargs.pop('redirect_spark_logs'))

    try: Util.call(spark_job, True)
    except Exception as exc:
        sys.stderr.write('Failed to submit Spark Job!\n')
        sys.stderr.write('[{0}] {1}\n\n'.format(exc.__class__.__name__, exc.message))
        sys.exit(2)

def main():
    '''
        Main command-line entry point.
    '''
    state = {}

    try:
        args = parse_args()
        conf = json.loads(args.config_file.read())

        # .............................check kerberos authentication
        if os.getenv('KRB_AUTH'):
            kb = Kerberos()
            kb.authenticate()

        state.update(**args.__dict__)

        # .............................add Spark Streaming parameters
        for key in conf['spark-streaming'].keys():
            if conf['spark-streaming'][key] == None:
                continue

            if isinstance(conf['spark-streaming'][key], basestring):
                conf['spark-streaming'][key] = conf['spark-streaming'][key].strip()

                if bool(conf['spark-streaming'][key]):
                    state[key] = conf['spark-streaming'][key]
                continue
            state[key] = conf['spark-streaming'][key]

        # .............................add files to place on the PYTHONPATH
        state['py_files'] = ','.join([os.path.abspath(os.path.join('dist', x)) for x in os.listdir('dist')])

        # .............................add database name
        state['database'] = conf['dbname']

        # .............................add zookeeper's connection string
        state['zkquorum'] = '{0}:{1}'.format(conf['kafka']['zookeper_server'],
                                        conf['kafka']['zookeper_port'])

        spark_job('common/listener.py', **state)

    except SystemExit: raise
    except:
        sys.excepthook(*sys.exc_info())
        sys.exit(1)

def parse_args():
    '''
        Parse command-line options found in 'args' (default: sys.argv[1:]).

    :returns: On success, a namedtuple of Values instances.
    '''
    parser   = ArgumentParser('Start Spark Job for Streaming Listener Daemon', epilog='END')
    required = parser.add_argument_group('mandatory arguments')

    # .................................state optional arguments
    parser.add_argument('-c', '--config-file',
        default='ingest_conf.json',
        type=file,
        help='path of configuration file',
        metavar='')

    parser.add_argument('-d', '--deploy-mode',
        default='client',
        help='Whether to launch the driver program locally ("client") or on one of the '
            'worker machines inside the cluster ("cluster")',
        metavar='')

    parser.add_argument('-g', '--group-id',
        help='name of the consumer group to join for dynamic partition assignment',
        metavar='')

    parser.add_argument('-l', '--log-level',
        default='INFO',
        help='determine the level of the logger',
        metavar='')

    parser.add_argument('-m', '--master',
        default='yarn',
        help='spark://host:port, mesos://host:port, yarn, or local',
        metavar='')

    parser.add_argument('-n', '--app-name',
        help='name of the Spark Job to display on the cluster web UI',
        metavar='')

    parser.add_argument('-r', '--redirect-spark-logs',
        help='redirect output of spark to specific file',
        metavar='')

    # .................................state mandatory arguments
    required.add_argument('-p', '--partitions',
        required=True,
        help='number of partitions to consume; each partition is consumed in its own thread')

    required.add_argument('-t', '--type',
        choices=pipelines.__all__,
        required=True,
        help='type of the data that will be ingested')

    required.add_argument('--topic',
        required=True,
        help='topic to listen for new messages')

    return parser.parse_args()

if __name__ == '__main__': main()

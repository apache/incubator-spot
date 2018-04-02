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
    Spark job to listen to a topic of the Kafka cluster, consume incoming segments and
saving the to Hive table.
'''

import logging
import sys

from argparse          import ArgumentParser
from pyspark           import SparkContext
from pyspark.sql       import HiveContext
from pyspark.streaming import StreamingContext
from utils             import Util

def streaming_listener(**kwargs):
    '''
        Initialize the Spark job.
    '''
    Util.get_logger('SPOT.INGEST', kwargs.pop('log_level'))

    logger  = logging.getLogger('SPOT.INGEST.COMMON.LISTENER')
    logger.info('Initializing Spark Streaming Listener...')

    dbtable = '{0}.{1}'.format(kwargs.pop('database'), kwargs['type'])
    topic   = kwargs.pop('topic')

    sc      = SparkContext(appName=kwargs['app_name'] or topic)
    logger.info('Connect to Spark Cluster as job "{0}" and broadcast variables on it.'
        .format(kwargs.pop('app_name') or topic))
    ssc     = StreamingContext(sc, batchDuration=kwargs['batch_duration'])
    logger.info('Streaming data will be divided into batches of {0} seconds.'
        .format(kwargs.pop('batch_duration')))
    hsc     = HiveContext(sc)
    logger.info('Read Hive\'s configuration to integrate with data stored in it.')

    import pipelines
    module  = getattr(pipelines, kwargs.pop('type'))
    stream  = module.StreamPipeline(ssc, kwargs.pop('zkquorum'),
                kwargs.pop('group_id') or topic, { topic: int(kwargs.pop('partitions')) })

    schema  = stream.schema
    segtype = stream.segtype

    stream.dstream\
        .map(lambda x: module.StreamPipeline.parse(x))\
        .filter(lambda x: bool(x))\
        .foreachRDD(lambda x: store(x, hsc, dbtable, topic, schema, segtype))

    ssc.start()
    logger.info('Start the execution of the streams.')
    ssc.awaitTermination()

def main():
    '''
        Main entry point for Spark Streaming Listener functionality.
    '''
    try: streaming_listener(**parse_args().__dict__)
    except SystemExit: raise
    except:
        sys.excepthook(*sys.exc_info())
        sys.exit(1)

def parse_args():
    '''
        Parse command-line options found in 'args' (default: sys.argv[1:]).

    :returns: On success, a namedtuple of Values instances.
    '''
    parser   = ArgumentParser('Streaming Listener Daemon of Spot Ingest Framework', epilog='END')
    required = parser.add_argument_group('mandatory arguments')

    # .................................state optional arguments
    parser.add_argument('-b', '--batch-duration',
        default=30,
        type=int,
        help='time interval (in seconds) at which streaming data will be divided into batches',
        metavar='')

    parser.add_argument('-g', '--group-id',
        help='name of the consumer group to join for dynamic partition assignment',
        metavar='')

    parser.add_argument('-l', '--log-level',
        default='INFO',
        help='determine the level of the logger',
        metavar='')

    parser.add_argument('-n', '--app-name',
        help='name of the Spark Job to display on the cluster web UI',
        metavar='')

    # .................................state mandatory arguments
    required.add_argument('-d', '--database',
        required=True,
        help='name of the database in Hive, where the data will be stored')

    required.add_argument('-p', '--partitions',
        required=True,
        help='number of partitions to consume; each partition is consumed in its own thread')

    required.add_argument('-t', '--type',
        required=True,
        help='type of the data that will be ingested')

    required.add_argument('--topic',
        required=True,
        help='topic to listen for new messages')

    required.add_argument('-z', '--zkquorum',
        required=True,
        help='the connection string for the zookeeper in the form \'host[:port]\'',
        metavar='')

    return parser.parse_args()

def store(rdd, hsc, dbtable, topic, schema=None, segtype='segments'):
    '''
        Interface for saving the content of the streaming :class:`DataFrame` out into
    Hive storage.

    :param rdd    : The content as a :class:`pyspark.RDD` of :class:`Row`.
    :param hsc    : A variant of Spark SQL that integrates with data stored in Hive.
    :param dbtable: The specified table in Hive database.
    :param topic  : Name of the topic to listen for incoming segments.
    :param schema : The schema of this :class:`DataFrame` as a
                    :class:`pyspark.sql.types.StructType`.
    :param segtype: The type of the received segments.
    '''
    logger = logging.getLogger('SPOT.INGEST.COMMON.LISTENER')

    if rdd.isEmpty():
        logger.info(' ---- LISTENING KAFKA TOPIC: {0} ---- '.format(topic))
        return

    hsc.setConf('hive.exec.dynamic.partition', 'true')
    hsc.setConf('hive.exec.dynamic.partition.mode', 'nonstrict')

    logger.info('Received {0} from topic. [Rows: {1}]'.format(segtype, rdd.count()))
    logger.info('Create distributed collection for partition "{0}".'
        .format(rdd.first()[0].replace('-', '').replace(' ', '')[:10]))

    df = hsc.createDataFrame(rdd, schema)
    df.write.format('parquet').mode('append').insertInto(dbtable)
    logger.info(' *** REGISTRATION COMPLETED *** ')

if __name__ == '__main__': main()

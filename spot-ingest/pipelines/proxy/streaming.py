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
    Methods to be used during the streaming process.
'''

import datetime
import shlex

def _analyzer(line):
    '''
        A lexical analyzer for simple shell-like syntaxes. Split given line into fields.

    :param line: Line to split.
    :returs    : List of fields.
    :rtype     : ``list``
    '''
    lex                  = shlex.shlex(line)
    lex.quotes           = '"'
    lex.whitespace_split = True
    lex.commenters       = ''

    return list(lex)


class StreamPipeline:
    '''
        Create an input stream that pulls proxy log messages from Kafka.

    :param ssc     : :class:`pyspark.streaming.context.StreamingContext` object.
    :param zkQuorum: Zookeeper quorum (host[:port],...)
    :param groupId : The group id for this consumer.
    :param topics  : Dictionary of topic -> numOfPartitions to consume. Each partition
                     is consumed in its own thread.
    '''

    def __init__(self, ssc, zkQuorum, groupId, topics):
        from common.serializer       import deserialize
        from pyspark.streaming.kafka import KafkaUtils

        self.__dstream = KafkaUtils.createStream(ssc, zkQuorum, groupId, topics,
                            keyDecoder=lambda x: x, valueDecoder=deserialize)

    @property
    def dstream(self):
        '''
            Return the schema of this :class:`DataFrame` as a
        :class:`pyspark.sql.types.StructType`.
        '''
        return self.__dstream\
            .map(lambda x: x[1])\
            .flatMap(lambda x: x)\
            .map(lambda x: _analyzer(x))

    @property
    def schema(self):
        '''
            Return the data type that represents a row from the received data list.
        '''
        from pyspark.sql.types import IntegerType, LongType, StringType, StructField, StructType

        return StructType(
            [
                StructField('p_date', StringType(), True),
                StructField('p_time', StringType(), True),
                StructField('clientip', StringType(), True),
                StructField('host', StringType(), True),
                StructField('reqmethod', StringType(), True),
                StructField('useragent', StringType(), True),
                StructField('resconttype', StringType(), True),
                StructField('duration', LongType(), True),
                StructField('username', StringType(), True),
                StructField('authgroup', StringType(), True),
                StructField('exceptionid', StringType(), True),
                StructField('filterresult', StringType(), True),
                StructField('webcat', StringType(), True),
                StructField('referer', StringType(), True),
                StructField('respcode', StringType(), True),
                StructField('action', StringType(), True),
                StructField('urischeme', StringType(), True),
                StructField('uriport', StringType(), True),
                StructField('uripath', StringType(), True),
                StructField('uriquery', StringType(), True),
                StructField('uriextension', StringType(), True),
                StructField('serverip', StringType(), True),
                StructField('scbytes', IntegerType(), True),
                StructField('csbytes', IntegerType(), True),
                StructField('virusid', StringType(), True),
                StructField('bcappname', StringType(), True),
                StructField('bcappoper', StringType(), True),
                StructField('fulluri', StringType(), True),
                StructField('y', StringType(), True),
                StructField('m', StringType(), True),
                StructField('d', StringType(), True),
                StructField('h', StringType(), True)
            ]
        )

    @property
    def segtype(self):
        '''
            Return the type of the received segments.
        '''
        return 'proxy-log segments'

    @staticmethod
    def parse(fields):
        '''
            Parsing and normalization of data in preparation for import.

        :param fields: Column fields of a row.
        :returns     : A list of typecast-ed fields, according to the table's schema.
        :rtype       : ``list``
        '''
        if len(fields) <= 1: return []

        dt       = datetime.datetime.strptime('{0} {1}'.format(fields[0], fields[1]),
                    '%Y-%m-%d %H:%M:%S')
        uripath  = fields[17] if len(fields[17]) > 1 else ''
        uriquery = fields[18] if len(fields[18]) > 1 else ''

        return [
            fields[0],
            fields[1],
            fields[3],
            fields[15],
            fields[12],
            fields[20],
            fields[13],
            long(fields[2]),
            fields[4],
            fields[5],
            fields[6],
            fields[7],
            fields[8],
            fields[9],
            fields[10],
            fields[11],
            fields[14],
            fields[16],
            fields[17],
            fields[18],
            fields[19],
            fields[21],
            int(fields[22]),
            int(fields[23]),
            fields[24],
            fields[25],
            fields[26],
            '{0}{1}{2}'.format(fields[15], uripath, uriquery),
            str(dt.year),
            str(dt.month).zfill(2),
            str(dt.day).zfill(2),
            str(dt.hour).zfill(2)
        ]

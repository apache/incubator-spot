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


class StreamPipeline:
    '''
        Create an input stream that pulls netflow messages from Kafka.

    :param ssc         : :class:`pyspark.streaming.context.StreamingContext` object.
    :param zkQuorum    : Zookeeper quorum (host[:port],...).
    :param groupId     : The group id for this consumer.
    :param topics      : Dictionary of topic -> numOfPartitions to consume. Each
                         partition is consumed in its own thread.
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
                .map(lambda x: x.split(','))

    @property
    def schema(self):
        '''
            Return the data type that represents a row from the received data list.
        '''
        from pyspark.sql.types import (FloatType, IntegerType, LongType,
                                       ShortType, StringType, StructField, StructType)

        return StructType(
            [
                StructField('treceived', StringType(), True),
                StructField('unix_tstamp', LongType(), True),
                StructField('tryear', IntegerType(), True),
                StructField('trmonth', IntegerType(), True),
                StructField('trday', IntegerType(), True),
                StructField('trhour', IntegerType(), True),
                StructField('trminute', IntegerType(), True),
                StructField('trsecond', IntegerType(), True),
                StructField('tdur', FloatType(), True),
                StructField('sip', StringType(), True),
                StructField('dip', StringType(), True),
                StructField('sport', IntegerType(), True),
                StructField('dport', IntegerType(), True),
                StructField('proto', StringType(), True),
                StructField('flag', StringType(), True),
                StructField('fwd', IntegerType(), True),
                StructField('stos', IntegerType(), True),
                StructField('ipkt', LongType(), True),
                StructField('ibyt', LongType(), True),
                StructField('opkt', LongType(), True),
                StructField('obyt', LongType(), True),
                StructField('input', IntegerType(), True),
                StructField('output', IntegerType(), True),
                StructField('sas', IntegerType(), True),
                StructField('das', IntegerType(), True),
                StructField('dtos', IntegerType(), True),
                StructField('dir', IntegerType(), True),
                StructField('rip', StringType(), True),
                StructField('y', ShortType(), True),
                StructField('m', ShortType(), True),
                StructField('d', ShortType(), True),
                StructField('h', ShortType(), True)
            ]
        )

    @property
    def segtype(self):
        '''
            Return the type of the received segments.
        '''
        return 'netflow segments'

    @staticmethod
    def parse(fields):
        '''
            Parsing and normalization of data in preparation for import.

        :param fields: Column fields of a row.
        :returns     : A list of typecast-ed fields, according to the table's schema.
        :rtype       : ``list``
        '''
        unix_tstamp = datetime.datetime.strptime(fields[0], '%Y-%m-%d %H:%M:%S')\
                        .strftime('%s')
        return [
            fields[0],
            long(unix_tstamp),
            int(fields[1]),
            int(fields[2]),
            int(fields[3]),
            int(fields[4]),
            int(fields[5]),
            int(fields[6]),
            float(fields[7]),
            fields[8],
            fields[9],
            int(fields[10]),
            int(fields[11]),
            fields[12],
            fields[13],
            int(fields[14]),
            int(fields[15]),
            long(fields[16]),
            long(fields[17]),
            long(fields[18]),
            long(fields[19]),
            int(fields[20]),
            int(fields[21]),
            int(fields[22]),
            int(fields[23]),
            int(fields[24]),
            int(fields[25]),
            fields[26],
            int(fields[1]),
            int(fields[2]),
            int(fields[3]),
            int(fields[4]),
        ]

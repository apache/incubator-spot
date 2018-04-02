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
        Create an input stream that pulls pcap messages from Kafka.

    :param ssc     : :class:`pyspark.streaming.context.StreamingContext` object.
    :param zkQuorum: Zookeeper quorum (host[:port],...).
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
            .map(lambda x: x.split(','))

    @property
    def schema(self):
        '''
            Return the data type that represents a row from the received data list.
        '''
        from pyspark.sql.types import (IntegerType, LongType, ShortType, StringType,
                                        StructField, StructType)

        return StructType(
            [
                StructField('frame_time', StringType(), True),
                StructField('unix_tstamp', LongType(), True),
                StructField('frame_len', IntegerType(), True),
                StructField('ip_dst', StringType(), True),
                StructField('ip_src', StringType(), True),
                StructField('dns_qry_name', StringType(), True),
                StructField('dns_qry_class', StringType(), True),
                StructField('dns_qry_type', IntegerType(), True),
                StructField('dns_qry_rcode', IntegerType(), True),
                StructField('dns_a', StringType(), True),
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
        return 'pcap segments'

    @staticmethod
    def parse(fields):
        '''
            Parsing and normalization of data in preparation for import.

        :param fields: Column fields of a row.
        :returns     : A list of typecast-ed fields, according to the table's schema.
        :rtype       : ``list``
        '''
        dt = datetime.datetime.fromtimestamp(float(fields[2]))

        return [
            '{0}, {1}'.format(fields[0], fields[1]),
            long(float(fields[2])),
            int(fields[3]),
            fields[5],
            fields[4],
            fields[6],
            fields[8],
            int(fields[7]),
            int(fields[9]),
            fields[10],
            dt.year,
            dt.month,
            dt.day,
            dt.hour
        ]

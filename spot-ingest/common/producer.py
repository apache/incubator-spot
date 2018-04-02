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
    Send ``list`` objects to Kafka cluster.
'''

import logging

from common.serializer import serialize
from kafka             import KafkaProducer
from kafka.errors      import MessageSizeTooLargeError


class Producer(KafkaProducer):
    '''
        A Kafka client that publishes records to the Kafka cluster.

        The producer is thread safe and sharing a single producer instance across
    threads will generally be faster than having multiple instances.

        Configuration parameters are described in more detail at
    https://kafka.apache.org/0100/configuration.html#producerconfigs
    '''

    def __init__(self, **kwargs):
        self._logger = logging.getLogger('SPOT.INGEST.PRODUCER')
        super(Producer, self).__init__(**kwargs)

    @property
    def max_request(self):
        '''
            Return the maximum size of a request.
        '''
        return self.config['max_request_size']

    def send_async(self, topic, value, key=None, partition=None, timestamp_ms=None):
        '''
            Publish a message to this topic and block until it is sent (or timeout).

        :param topic        : Topic where the messages will be published.
        :param value        : Message value - must be serializable to bytes.
        :param key          : A key associated with the messages - must be type bytes.
                              Can be used to determine which partition to send the
                              messages to. If partition is ``None`` (and producer's
                              partitioner config is left as default) then messages with
                              the same key will be delivered to the same partition (but
                              if key is not set, partition is chosen randomly).
        :param partition    : Optionally specify a partition. If not set, the partition
                              will be selected using the configured 'partitioner'.
        :param timestamp_ms : Epoch milliseconds (from Jan 1 1970 UTC) to use as the
                              message timestamp. Default: current time
        :rtype              : :class:`kafka.producer.future.RecordMetadata`
        :raises RuntimeError: If an error occurs while publishing the value message.
        '''
        assert bool(value), 'If value is ``None``, then it acts as \'delete\'.'

        try:
            rawbytes = serialize(value)
            if not rawbytes: raise RuntimeError

            _future  = self.send(topic, rawbytes.getvalue(), key, partition, timestamp_ms)
            meta     = _future.get(timeout=10)

            self._logger.debug('[Offset: {0}, Partition: {1}, Checksum: {2}]'
                .format(meta.offset, meta.partition, meta.checksum))

            return meta

        except MessageSizeTooLargeError:
            self._logger.error('The size of the message is greater than the maximum allowed. '
                '[Current size: {0} bytes]'.format(rawbytes.__sizeof__()))

        except Exception as exc:
            self._logger.error('[{0}] {1}'.format(exc.__class__.__name__, exc.message))

        raise RuntimeError

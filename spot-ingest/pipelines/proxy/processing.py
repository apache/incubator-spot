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
    Methods that will be used to process and prepare dns data, before being sent to
Kafka cluster.
'''

import logging
import re
import sys
import tempfile

from common.utils import Util
from datetime     import datetime

COMMAND = 'cp {0} {1} {2}'
EPOCH = datetime(1970, 1, 1)

def convert(logfile, tmpdir, opts='', prefix=None):
    '''
        Copy log file to the local staging area.

    :param logfile: Path of log file.
    :param tmpdir : Path of local staging area.
    :param opts   : A set of options for the `cp` command.
    :param prefix : If `prefix` is specified, the file name will begin with that;
                     otherwise, a default `prefix` is used.
    :returns      : Path of log file in local staging area.
    :rtype        : ``str``
    '''
    logger = logging.getLogger('SPOT.INGEST.PROXY.PROCESS')

    with tempfile.NamedTemporaryFile(prefix=prefix, dir=tmpdir, delete=False) as fp:
        command = COMMAND.format(opts, logfile, fp.name)

        logger.debug('Execute command: {0}'.format(command))
        Util.popen(command, raises=True)

        return fp.name

def prepare(logfile, max_req_size):
    '''
        Prepare text-formatted data for transmission through the Kafka cluster.

        This method takes a log file and groups it into segments, according to the
    pattern '%Y%m%d%h'. If the size of each segment is greater than the maximum size
    of a request, then divides each segment into smaller ones so that they can be
    transmitted.

    :param logfile     : Path of log file; result of `convert` method.
    :param max_req_size: The maximum size of a request.
    :returns           : A generator which yields the timestamp (in milliseconds) and a
                         list of lines from the log file.
    :rtype             : :class:`types.GeneratorType`
    :raises IOError    : If the given file has no any valid line.
    '''
    msg_list  = []
    msg_size  = segmentid = 0
    logger    = logging.getLogger('SPOT.INGEST.PROXY.PROCESS')
    partition = timestamp = None
    pattern   = re.compile('[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}')

    with open(logfile, 'r') as fp:
        for line in fp:
            value = line.strip()
            if not value: continue

            match = pattern.search(' '.join(value.split()[:2]))
            if not match: continue

            size  = sys.getsizeof(value)
            # .........................assume the first 13 characters of the `search`
            # result as the `partition`, e.g. '2018-03-20 09'
            if match.group()[:13] == partition and (msg_size + size) < max_req_size:
                msg_list.append(value)
                msg_size += size
                continue

            # .........................if the hour is different or the message size is
            # above the maximum, then yield existing list and continue with an empty one
            if timestamp:
                logger.debug('Yield segment-{0}: {1} lines, {2} bytes'.format(segmentid,
                    len(msg_list), msg_size))
                segmentid += 1

                yield (int(timestamp.total_seconds() * 1000), msg_list)

            msg_list  = [value]
            msg_size  = size
            partition = match.group()[:13]
            timestamp = datetime.strptime(match.group(), '%Y-%m-%d %H:%M:%S') - EPOCH

    # .................................send the last lines from the file. The check of
    # `timestamp` is in case the file is empty and `timestamp` is still ``None``
    if not timestamp:
        raise IOError('Text-converted file has no valid lines.')

    logger.debug('Yield segment-{0}: {1} lines, {2} bytes'.format(segmentid,
        len(msg_list), msg_size))

    yield (int(timestamp.total_seconds() * 1000), msg_list)

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
    Monitor a directory for new files, convert them to text output format, split them to
small chunks of bytes - according to the maximum size of the request -, serialize and
publish to Kafka cluster.
'''

import json
import logging
import os
import pipelines
import shutil
import signal
import sys
import time

from argparse            import ArgumentParser
from common.file_watcher import FileWatcher
from common.kerberos     import Kerberos
from common.producer     import Producer
from common.utils        import Util
from multiprocessing     import current_process, Pool
from tempfile            import mkdtemp


class DistributedCollector:
    '''
        Distributed Collector manages the process to collect and publish all the files to
    the Kafka cluster.

    :param datatype       : Type of data to be collected.
    :param topic          : Name of topic where the messages will be published.
    :param skip_conversion: If ``True``, then no transformation will be applied to the data.
    '''

    def __init__(self, datatype, topic, skip_conversion, **conf):
        self._logger          = logging.getLogger('SPOT.INGEST.COLLECTOR')
        self._logger.info('Initializing Distributed Collector process...')

        self._datatype        = datatype
        self._interval        = conf['ingestion_interval']
        self._isalive         = True
        self._process_opts    = conf['pipelines'][datatype]['process_opt']
        self._processes       = conf['collector_processes']
        self._producer_kwargs = conf['producer']
        self._skip_conversion = skip_conversion
        self._topic           = topic

        # .............................init FileObserver
        self.FileWatcher      = FileWatcher(**conf['file_watcher'])

        # .............................set up local staging area
        self._tmpdir          = mkdtemp(prefix='_DC.', dir=conf['pipelines'][datatype]['local_staging'])
        self._logger.info('Use directory "{0}" as local staging area.'.format(self._tmpdir))

        # .............................define a process pool object
        self._pool            = Pool(self._processes, _init_child, [self._tmpdir])
        self._logger.info('Master Collector will use {0} parallel processes.'
            .format(self._processes))

        signal.signal(signal.SIGUSR1, self.kill)
        self._logger.info('Initialization completed successfully!')

    def __del__(self):
        '''
            Called when the instance is about to be destroyed.
        '''
        if hasattr(self, '_tmpdir'):
            self._logger.info('Clean up temporary directory "{0}".'.format(self._tmpdir))
            shutil.rmtree(self._tmpdir)

    @property
    def isalive(self):
        '''
            Wait and return True if instance is still alive.
        '''
        time.sleep(self._interval)
        return self._isalive

    def kill(self):
        '''
            Receive signal for termination from an external process.
        '''
        self._logger.info('Receiving a kill signal from an external process...')
        self._isalive = False

    def start(self):
        '''
            Start Master Collector process.
        '''
        self._logger.info('Start "{0}" Collector!'.format(self._datatype.capitalize()))

        self.FileWatcher.start()
        self._logger.info('Signal the {0} thread to start.'.format(str(self.FileWatcher)))

        try:
            while self.isalive:
                files = [self.FileWatcher.dequeue for _ in range(self._processes)]
                for _file in [x for x in files if x]:
                    self._pool.apply_async(publish, args=(_file, self._tmpdir,
                        self._process_opts, self._datatype, self._topic, None,
                            self._skip_conversion), kwds=self._producer_kwargs)

        except KeyboardInterrupt: pass
        finally:
            self.FileWatcher.stop()
            self._pool.close()
            self._pool.join()
            self._logger.info('Stop "{0}" Collector!'.format(self._datatype.capitalize()))

    @classmethod
    def run(cls):
        '''
            Main command-line entry point.

        :param cls: The class as implicit first argument.
        '''
        try:
            args  = _parse_args()
            conf  = json.loads(args.config_file.read())

            # .........................set up logger
            Util.get_logger('SPOT', args.log_level)

            # .........................check kerberos authentication
            if os.getenv('KRB_AUTH'):
                kb = Kerberos()
                kb.authenticate()

            conf['producer'] = {
                'bootstrap_servers': ['{0}:{1}'
                    .format(conf['kafka']['kafka_server'], conf['kafka']['kafka_port'])]
            }

            conf['file_watcher'] = {
                'path': conf['pipelines'][args.type]['collector_path'],
                'supported_files': conf['pipelines'][args.type]['supported_files'],
                'recursive': True
            }

            # .........................migrate configs
            if not 'local_staging' in conf['pipelines'][args.type].keys():
                conf['pipelines'][args.type]['local_staging'] = '/tmp'

            if 'max_request_size' in conf['kafka'].keys():
                conf['producer']['max_request_size'] = conf['kafka']['max_request_size']

            if not 'process_opt' in conf['pipelines'][args.type].keys():
                conf['pipelines'][args.type]['process_opt'] = ''

            if 'recursive' in conf['pipelines'][args.type].keys():
                conf['file_watcher']['recursive'] = conf['pipelines'][args.type]['recursive']

            collector = cls(args.type, args.topic, args.skip_conversion, **conf)
            collector.start()

        except SystemExit: raise
        except:
            sys.excepthook(*sys.exc_info())
            sys,exit(1)

def publish(rawfile, tmpdir, opts, datatype, topic, partition, skip_conv, **kwargs):
    '''
        Publish human-readable formatted data to the Kafka cluster.

    :param rawfile  : Path of original raw file.
    :param tmpdir   : Path of local staging area.
    :param opts     : A set of options for the conversion.
    :param datatype : Type of data that will be ingested.
    :param topic    : Topic where the messages will be published.
    :param partition: The partition number where the messages will be delivered.
    :param skip_conv: Skip `convert` function.
    :param kwargs   : Configuration parameters to initialize the
                      :class:`collector.Producer` object.
    '''
    def send_async(segid, timestamp_ms, items):
        try:
            metadata = producer.send_async(topic, items, None, partition, timestamp_ms)

            logger.info('Published segment-{0} to Kafka cluster. [Topic: {1}, '
                'Partition: {2}]'.format(segid, metadata.topic, metadata.partition))

            return True
        except RuntimeError:
            logger.error('Failed to publish segment-{0} to Kafka cluster.'.format(segid))
            logger.info('Store segment-{0} to local staging area as "{1}".'
                .format(segid, _store_failed_segment(segid, items, filename, tmpdir)))

            return False

    filename  = os.path.basename(rawfile)
    proc_name = current_process().name
    logger    = logging.getLogger('SPOT.INGEST.{0}.{1}'.format(datatype.upper(), proc_name))
    logger.info('Processing raw file "{0}"...'.format(filename))

    proc_dir  = os.path.join(tmpdir, proc_name)
    allpassed = True

    try:
        module      = getattr(pipelines, datatype)
        producer    = Producer(**kwargs)

        if skip_conv:
            shutil.copy2(rawfile, tmpdir)
            staging = os.path.join(tmpdir, os.path.basename(rawfile))
        else:
            staging = module.convert(rawfile, proc_dir, opts, datatype + '_')

        logger.info('Load converted file to local staging area as "{0}".'
            .format(os.path.basename(staging)))

        partitioner = module.prepare(staging, producer.max_request)
        logger.info('Group lines of text-converted file and prepare to publish them.')

        for segmentid, datatuple in enumerate(partitioner):
            try: send_async(segmentid, *datatuple)
            except RuntimeError: allpassed = False

        os.remove(staging)
        logger.info('Remove CSV-converted file "{0}" from local staging area.'
            .format(staging))

    except IOError as ioe:
        logger.warning(ioe.message)
        return

    except Exception as exc:
        logger.error('[{0}] {1}'.format(exc.__class__.__name__, exc.message))
        return

    finally:
        if producer: producer.close()

    if allpassed:
        logger.info('All segments of "{0}" published successfully to Kafka cluster.'
            .format(filename))
        return

    logger.warning('One or more segment(s) of "{0}" were not published to Kafka cluster.'
        .format(filename))

def _init_child(tmpdir):
    '''
        Initialize new process from multiprocessing module's Pool.

    :param tmpdir: Path of local staging area.
    '''
    signal.signal(signal.SIGINT, signal.SIG_IGN)

    # .................................for each process, create a isolated temp folder
    proc_dir = os.path.join(tmpdir, current_process().name)

    if not os.path.isdir(proc_dir):
        os.mkdir(proc_dir)

def _parse_args():
    '''
        Parse command-line options found in 'args' (default: sys.argv[1:]).

    :returns: On success, a namedtuple of Values instances.
    '''
    parser   = ArgumentParser('Distributed Collector Daemon of Apache Spot', epilog='END')
    required = parser.add_argument_group('mandatory arguments')

    # .................................state optional arguments
    parser.add_argument('-c', '--config-file',
        default='ingest_conf.json',
        type=file,
        help='path of configuration file',
        metavar='')

    parser.add_argument('-l', '--log-level',
        default='INFO',
        help='determine the level of the logger',
        metavar='')

    parser.add_argument('--skip-conversion',
        action='store_true',
        default=False,
        help='no transformation will be applied to the data; useful for importing CSV files')

    # .................................state mandatory arguments
    required.add_argument('--topic',
        required=True,
        help='name of topic where the messages will be published')

    required.add_argument('-t', '--type',
        choices=pipelines.__all__,
        required=True,
        help='type of data that will be collected')

    return parser.parse_args()

def _store_failed_segment(id, items, filename, path):
    '''
        Store segment to the local staging area to try to send to Kafka cluster later.

    :param id      : The id of the current segment.
    :param items   : List of lines from the text-converted file.
    :param filename: The name of the original raw file.
    :param path    : Path of local staging area.
    :returns       : The name of the segment file in the local staging area.
    :rtype         : ``str``
    '''
    name = '{0}_segment-{1}.csv'.format(filename.replace('.', '_'), id)

    with open(os.path.join(path, name), 'w') as fp:
        [fp.write(x + '\n') for x in items]

    return name

if __name__ == '__main__': DistributedCollector.run()

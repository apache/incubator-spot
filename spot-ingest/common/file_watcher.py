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
    Monitor directory in local file system for new generated files.
'''

import logging
import re

from os.path            import basename
from watchdog.events    import FileSystemEventHandler
from watchdog.observers import Observer


class FileWatcher(Observer):
    '''
        An observer thread that monitors the given directory to detect new generated
    files. If the filenames match any pattern from the `supported_names` list, then they
    will be added to the queue.

    :param path           : Directory path that will be monitored.
    :param supported_files: List of regular expressions for supported filenames.
    :param recursive      : ``True`` if events will be emitted for sub-directories
                            traversed recursively; ``False`` otherwise.
    '''

    def __init__(self, path, supported_files, recursive):
        self._logger  = logging.getLogger('SPOT.INGEST.COMMON.FILE_WATCHER')
        self._queue   = []

        super(FileWatcher, self).__init__()

        self._logger.info('Schedule watching "{0}" directory.'.format(path))
        super(FileWatcher, self).schedule(NewFileEventHandler(self), path, recursive)

        self._regexs  = [re.compile(x) for x in supported_files]
        pattern_names = ', '.join(['"%s"' % x for x in supported_files])
        self._logger.info('Supported filenames: {0}'.format(pattern_names))

        self._logger.info('The search in sub-directories is {0}.'
            .format('enabled' if recursive else 'disabled'))

    def __str__(self):
        '''
            Return the "informal" string representation of the object.
        '''
        return '"{0}({1})"'.format(self.__class__.__name__, self.name)

    @property
    def dequeue(self):
        '''
            Return the next file from the queue, if any.
        '''
        return None if self._queue == [] else self._queue.pop()

    @property
    def is_empty(self):
        '''
            Return ``True`` if there is no file in the queue, otherwise ``False``.
        '''
        return self._queue == []

    def detect(self, newfile):
        '''
            Called when a new file is generated under the monitoring directory.

        :param newfile: Path to file created recently.
        '''
        self._logger.info(' -------- New File Detected! -------- ')

        filename = basename(newfile)
        # .............................check whether the filename is in the supported list
        if any([x.search(filename) for x in self._regexs]) or not self._regexs:
            self._queue.insert(0, newfile)
            self._logger.info('File "{0}" added to the queue.'.format(newfile))
            return

        self._logger.warning('Filename "%s" is not supported! Skip file...' % filename)

    def stop(self):
        '''
            Signals the current thread to stop and waits until it terminates. This blocks
        the calling thread until it terminates -- either normally or through an unhandled
        exception.

        :raises RuntimeError: If an attempt is made to join the current thread as that
                              would cause a deadlock. It is also an error to join() a
                              thread before it has been started and attemps to do so
                              raises the same exception.
        '''
        self._logger.info('Signal {0} thread to stop normally.'.format(str(self)))
        super(FileWatcher, self).stop()

        self._logger.info('Wait until the {0} thread terminates...'.format(str(self)))
        super(FileWatcher, self).join()

        while not self.is_empty:
            self._logger.debug('Drop "%s" from the queue.' % basename(self._queue.pop()))

        assert self.is_empty, 'Failed to clean the queue.'


class NewFileEventHandler(FileSystemEventHandler):
    '''
        An event handler instance that has appropriate event handling methods which
    will be called by the observer in response to file system events.

    :param instance: A :class:`watchdog.observers.inotify.InotifyObserver` object.
    '''

    def __init__(self, instance):
        self._instance = instance

    def on_created(self, event):
        '''
            Called when a file or directory is created.

        :param event: Event representing file/directory creation.
        '''
        if not event.is_directory:
            self._instance.detect(event.src_path)

    def on_moved(self, event):
        '''
            Called when a file or directory is moved or renamed.

        :param event: Event representing file/directory movement.
        '''
        if not event.is_directory:
            self._instance.detect(event.dest_path)

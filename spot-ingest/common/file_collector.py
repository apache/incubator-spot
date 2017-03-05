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

from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
import logging
from multiprocessing import Queue

class FileWatcher(object):


    def __init__(self,collector_path,supported_files):
        self._initialize_members(collector_path,supported_files)

    def _initialize_members(self,collector_path,supported_files):        

        # initializing observer.
        event_handler = NewFileEvent(self)
        self._observer = Observer()
        self._observer.schedule(event_handler,collector_path)

        self._collector_path = collector_path
        self._files_queue = Queue()
        self._supported_files = supported_files

        self._logger = logging.getLogger('SPOT.INGEST.WATCHER')    
        self._logger.info("Creating File watcher")
        self._logger.info("Supported Files: {0}".format(self._supported_files))

    def start(self):       
        
        self._logger.info("Watching: {0}".format(self._collector_path))        
        self._observer.start()

    def new_file_detected(self,file):

        self._logger.info("-------------------------------------- New File detected --------------------------------------")
        self._logger.info("File: {0}".format(file))

        # Validate the file is supported.        
        collected_file_parts = file.split("/")
        collected_file = collected_file_parts[len(collected_file_parts) -1 ]    
        if (collected_file.endswith(tuple(self._supported_files)) or collected_file.startswith(tuple(self._supported_files))  ) and not  ".current" in collected_file:                   
            self._files_queue.put(file)
            self._logger.info("File {0} added to the queue".format(file))                        
        else:
            self._logger.warning("File extension not supported: {0}".format(file))
            self._logger.warning("File won't be ingested")  

        self._logger.info("------------------------------------------------------------------------------------------------")

    def stop(self):
        self._logger.info("Stopping File Watcher")
        self._files_queue.close()
        while not self._files_queue.empty(): self._files_queue.get()      
        self._observer.stop()
        self._observer.join()

    def GetNextFile(self):
        return self._files_queue.get()      
    
    @property
    def HasFiles(self):
        return not self._files_queue.empty()

class NewFileEvent(FileSystemEventHandler):

    def __init__(self,watcher_instance):
        self.watcher_instance = watcher_instance

    def on_moved(self,event):        
        if not event.is_directory:            
            self.watcher_instance.new_file_detected(event.dest_path)

    def on_created(self,event):
        if not event.is_directory:            
            self.watcher_instance.new_file_detected(event.src_path)
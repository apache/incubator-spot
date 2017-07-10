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
from hdfs import InsecureClient
from hdfs.util import HdfsError
from json import dump
import api.resources.configurator as Config


def _get_client(user=None):
    hdfs_nm,hdfs_port,hdfs_user = Config.hdfs()
    client = InsecureClient('http://{0}:{1}'.format(hdfs_nm,hdfs_port), user= user if user else hdfs_user)
    return client

def get_file(hdfs_file):
    client = _get_client()
    with client.read(hdfs_file) as reader:
        results = reader.read()
        return results

def put_file_csv(hdfs_file_content,hdfs_path,hdfs_file_name,append_file=False,overwrite_file=False):
    
    try:
        client = _get_client()
        hdfs_full_name = "{0}/{1}".format(hdfs_path,hdfs_file_name)
        with client.write(hdfs_full_name,append=append_file,overwrite=overwrite_file) as writer:
            for item in hdfs_file_content:
                data = ','.join(str(d) for d in item)
                writer.write("{0}\n".format(data))
        return True
        
    except HdfsError:
        return False

def put_file_json(hdfs_file_content,hdfs_path,hdfs_file_name,append_file=False,overwrite_file=False):
    
    try:
        client = _get_client()
        hdfs_full_name = "{0}/{1}".format(hdfs_path,hdfs_file_name)
        with client.write(hdfs_full_name,append=append_file,overwrite=overwrite_file,encoding='utf-8') as writer:
	        dump(hdfs_file_content, writer)
        return True
    except HdfsError:
        return False
    

def delete_folder(hdfs_file,user=None):
    client = _get_client(user)
    client.delete(hdfs_file,recursive=True)

def list_dir(hdfs_path):
    try:
        client = _get_client()
        return client.list(hdfs_path)
    except HdfsError:
        return {}

def file_exists(hdfs_path,file_name):
    files = list_dir(hdfs_path)
    if str(file_name) in files:
	    return True
    else:
        return False

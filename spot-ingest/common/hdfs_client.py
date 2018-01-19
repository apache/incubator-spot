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
from hdfs import Client
from hdfs.ext.kerberos import KerberosClient
from requests import Session
from json import dump
from threading import Lock
import logging
import configurator as Config
from sys import stderr


class Progress(object):

    """Basic progress tracker callback."""

    def __init__(self, hdfs_path, nbytes):
        self._data = {}
        self._lock = Lock()
        self._hpath = hdfs_path
        self._nbytes = nbytes

    def __call__(self):
        with self._lock:
            if self._nbytes >= 0:
                self._data[self._hpath] = self._nbytes
            else:
                stderr.write('%s\n' % (sum(self._data.values()), ))


class SecureKerberosClient(KerberosClient):

    """A new client subclass for handling HTTPS connections with Kerberos.

    :param url: URL to namenode.
    :param cert: Local certificate. See `requests` documentation for details
      on how to use this.
    :param verify: Whether to check the host's certificate. WARNING: non production use only
    :param \*\*kwargs: Keyword arguments passed to the default `Client`
      constructor.

    """

    def __init__(self, url, mutual_auth, cert=None, verify='true', **kwargs):

        self._logger = logging.getLogger("SPOT.INGEST.HDFS_client")
        session = Session()

        if verify == 'true':
            self._logger.info('SSL verification enabled')
            session.verify = True
            if cert is not None:
                self._logger.info('SSL Cert: ' + cert)
                if ',' in cert:
                    session.cert = [path.strip() for path in cert.split(',')]
                else:
                    session.cert = cert
        elif verify == 'false':
            session.verify = False

        super(SecureKerberosClient, self).__init__(url, mutual_auth, session=session, **kwargs)


class HdfsException(HdfsError):
    def __init__(self, message):
        super(HdfsException, self).__init__(message)
        self.message = message


def get_client(user=None):
    # type: (object) -> Client

    logger = logging.getLogger('SPOT.INGEST.HDFS.get_client')
    hdfs_nm, hdfs_port, hdfs_user = Config.hdfs()
    conf = {'url': '{0}:{1}'.format(hdfs_nm, hdfs_port),
            'mutual_auth': 'OPTIONAL'
            }

    if Config.ssl_enabled():
        ssl_verify, ca_location, cert, key = Config.ssl()
        conf.update({'verify': ssl_verify.lower()})
        if cert:
            conf.update({'cert': cert})

    if Config.kerberos_enabled():
        # TODO: handle other conditions
        krb_conf = {'mutual_auth': 'OPTIONAL'}
        conf.update(krb_conf)

    # TODO: possible user parameter
    logger.info('Client conf:')
    for k,v in conf.iteritems():
        logger.info(k + ': ' + v)

    client = SecureKerberosClient(**conf)

    return client


def get_file(hdfs_file, client=None):
    if not client:
        client = get_client()

    with client.read(hdfs_file) as reader:
        results = reader.read()
        return results


def upload_file(hdfs_fp, local_fp, overwrite=False, client=None):
    if not client:
        client = get_client()

    try:
        result = client.upload(hdfs_fp, local_fp, overwrite=overwrite, progress=Progress)
        return result
    except HdfsError as err:
        return err


def download_file(hdfs_path, local_path, overwrite=False, client=None):
    if not client:
        client = get_client()

    try:
        client.download(hdfs_path, local_path, overwrite=overwrite)
        return True
    except HdfsError:
        return False


def mkdir(hdfs_path, client=None):
    if not client:
        client = get_client()

    try:
        client.makedirs(hdfs_path)
        return True
    except HdfsError:
        return False


def put_file_csv(hdfs_file_content,hdfs_path,hdfs_file_name,append_file=False,overwrite_file=False, client=None):
    if not client:
        client = get_client()

    try:
        hdfs_full_name = "{0}/{1}".format(hdfs_path,hdfs_file_name)
        with client.write(hdfs_full_name,append=append_file,overwrite=overwrite_file) as writer:
            for item in hdfs_file_content:
                data = ','.join(str(d) for d in item)
                writer.write("{0}\n".format(data))
        return True

    except HdfsError:
        return False


def put_file_json(hdfs_file_content,hdfs_path,hdfs_file_name,append_file=False,overwrite_file=False, client=None):
    if not client:
        client = get_client()

    try:
        hdfs_full_name = "{0}/{1}".format(hdfs_path,hdfs_file_name)
        with client.write(hdfs_full_name,append=append_file,overwrite=overwrite_file,encoding='utf-8') as writer:
            dump(hdfs_file_content, writer)
        return True
    except HdfsError:
        return False


def delete_folder(hdfs_file, user=None, client=None):
    if not client:
        client = get_client()

    try:
        client.delete(hdfs_file,recursive=True)
    except HdfsError:
        return False


def check_dir(hdfs_path, client=None):
    """
    Returns True if directory exists
    Returns False if directory does not exist
    : param hdfs_path: path to check
    : object client: hdfs client object for persistent connection
    """
    if not client:
        client = get_client()

    result = client.list(hdfs_path)
    if None not in result:
        return True
    else:
        return False


def list_dir(hdfs_path, client=None):
    if not client:
        client = get_client()

    try:
        return client.list(hdfs_path)
    except HdfsError:
        return {}


def file_exists(hdfs_path, file_name, client=None):
    if not client:
        client = get_client()

    files = list_dir(hdfs_path, client)
    if str(file_name) in files:
        return True
    else:
        return False

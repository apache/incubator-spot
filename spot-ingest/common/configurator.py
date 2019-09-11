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

import ConfigParser
from io import open


def configuration():

    config = ConfigParser.ConfigParser()

    try:
        conf = open("/etc/spot.conf", "r")
    except (OSError, IOError) as e:
        print("Error opening: spot.conf" + " error: " + e.errno)
        raise e

    config.readfp(SecHead(conf))
    return config


def db():
    return get_conf('DBNAME')


def impala():
    return get_conf('IMPALA_DEM'), get_conf('IMPALA_PORT')


def hdfs():
    return get_conf('NAME_NODE'), get_conf('WEB_PORT'), get_conf('HUSER').split("/")[-1]


def spot():
    return get_conf('HUSER')


def kerberos_enabled():
    enabled = get_conf('KERBEROS')
    if enabled.lower() == 'true':
        return True
    else:
        return False


def kerberos():
    if kerberos_enabled():
        return get_conf('PRINCIPAL'), get_conf('KEYTAB'), get_conf('SASL_MECH'), get_conf('SECURITY_PROTO')
    else:
        raise KeyError


def ssl_enabled():
    enabled = get_conf('SSL')
    if enabled.lower() == 'true':
        return True
    else:
        return False


def ssl():
    if ssl_enabled():
        return get_conf('SSL_VERIFY'), get_conf('CA_LOCATION'), get_conf('CERT'), get_conf('KEY')
    else:
        raise KeyError


def get_conf(key):
    conf = configuration()
    header = 'conf'
    result = conf.get(header, key)
    return result.replace("'", "").replace('"', '').encode('ascii', 'ignore')


class SecHead(object):
    def __init__(self, fp):
        self.fp = fp
        self.sechead = '[conf]\n'

    def readline(self):
        if self.sechead:
            try:
                return self.sechead
            finally:
                self.sechead = None
        else:
            return self.fp.readline()

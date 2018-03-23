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
    conf = configuration()
    return conf.get('conf', 'DBNAME').replace("'", "").replace('"', '')


def impala():
    conf = configuration()
    return conf.get('conf', 'IMPALA_DEM'), conf.get('conf', 'IMPALA_PORT')


def hive():
    conf = configuration()
    return conf.get('conf', 'HS2_HOST'), conf.get('conf', 'HS2_PORT')


def hdfs():
    conf = configuration()
    name_node = conf.get('conf',"NAME_NODE")
    web_port = conf.get('conf',"WEB_PORT")
    hdfs_user = conf.get('conf',"HUSER")
    hdfs_user = hdfs_user.split("/")[-1].replace("'", "").replace('"', '')
    return name_node,web_port,hdfs_user


def spot():
    conf = configuration()
    return conf.get('conf',"HUSER").replace("'", "").replace('"', '')


def kerberos_enabled():
    conf = configuration()
    enabled = conf.get('conf', 'KERBEROS').replace("'", "").replace('"', '')
    if enabled.lower() == 'true':
        return True
    else:
        return False


def kerberos():
    conf = configuration()
    if kerberos_enabled():
        principal = conf.get('conf', 'PRINCIPAL')
        keytab = conf.get('conf', 'KEYTAB')
        sasl_mech = conf.get('conf', 'SASL_MECH')
        security_proto = conf.get('conf', 'SECURITY_PROTO')
        return principal, keytab, sasl_mech, security_proto
    else:
        raise KeyError


def ssl_enabled():
    conf = configuration()
    enabled = conf.get('conf', 'SSL')
    if enabled.lower() == 'true':
        return True
    else:
        return False


def ssl():
    conf = configuration()
    if ssl_enabled():
        ssl_verify = conf.get('conf', 'SSL_VERIFY')
        ca_location = conf.get('conf', 'CA_LOCATION')
        cert = conf.get('conf', 'CERT')
        key = conf.get('conf', 'KEY')
        return ssl_verify, ca_location, cert, key
    else:
        raise KeyError


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

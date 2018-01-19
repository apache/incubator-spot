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
from impala.dbapi import connect
import common.configurator as config


def create_connection():

    host, port = config.hive()
    conf = {}

    # TODO: if using hive, kerberos service name must be changed, impyla sets 'impala' as default
    conf.update({'kerberos_service_name': 'hive'})

    if config.kerberos_enabled():
        principal, keytab, sasl_mech, security_proto = config.kerberos()
        conf.update({'auth_mechanism': 'GSSAPI',
                     })
    else:
        conf.update({'auth_mechanism': 'PLAIN',
                     })

    if config.ssl_enabled():
        ssl_verify, ca_location, cert, key = config.ssl()
        if ssl_verify.lower() == 'false':
            conf.update({'use_ssl': ssl_verify})
        else:
            conf.update({'ca_cert': cert,
                         'use_ssl': ssl_verify
                         })

    db = config.db()
    conn = connect(host=host, port=int(port), database=db, **conf)
    return conn.cursor()


def execute_query(query,fetch=False):

    impala_cursor = create_connection()
    impala_cursor.execute(query)

    return impala_cursor if not fetch else impala_cursor.fetchall()


def execute_query_as_list(query):

    query_results = execute_query(query)
    row_result = {}
    results = []

    for row in query_results:
        x=0
        for header in query_results.description:
            row_result[header[0]] = row[x]
            x +=1
        results.append(row_result)
        row_result = {}

    return results

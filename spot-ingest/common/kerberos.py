#!/bin/env python

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

import sys
import os
import common.configurator as config
from common.utils import Util


class Kerberos(object):
    def __init__(self):

        self._logger = Util.get_logger('SPOT.COMMON.KERBEROS')
        principal, keytab, sasl_mech, security_proto = config.kerberos()

        if os.getenv('KINITPATH'):
            self._kinit = os.getenv('KINITPATH')
        else:
            self._kinit = "kinit"

        self._kinitopts = os.getenv('KINITOPTS')
        self._keytab = "-kt {0}".format(keytab)
        self._krb_user = principal

        if self._kinit == None or self._keytab == None or self._krb_user == None:
            self._logger.error("Please verify kerberos configuration, some environment variables are missing.")
            sys.exit(1)

        if self._kinitopts is None:
            self._kinit_cmd = "{0} {1} {2}".format(self._kinit, self._keytab, self._krb_user)
        else:
            self._kinit_cmd = "{0} {1} {2} {3}".format(self._kinit, self._kinitopts, self._keytab, self._krb_user)

    def authenticate(self):

        Util.execute_cmd(self._kinit_cmd, self._logger)
        self._logger.info("Kerberos ticket obtained")

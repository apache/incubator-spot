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

import csv
import bisect
import linecache
import numpy
import logging
import os
from utils import Util


'''
    Geo Localization Service.
    Returns the geo localization and domain for a given ip address
    in the format of dictionary with two elements.
    i.e.
        {
            "geo_loc": "Delhi;New Delhi Vodafone Essar Limited GPRS Service"
            "domain":"vodafone.in"
        }
'''

class GeoLocalization(object):

    def __init__(self,file,logger=None):

        self._logger = logging.getLogger('OA.GEO') if logger else Util.get_logger('OA.GEO',create_file=False) 
        self._ip_localization_file = file
        self._ip_localization_ranges = self._get_low_ips_in_ranges()

    def _get_low_ips_in_ranges(self):

        self._logger.info("Reading GEO localization file: {0}".format(self._ip_localization_file))
        if os.path.isfile(self._ip_localization_file):
            return numpy.loadtxt(self._ip_localization_file, dtype=numpy.uint32, delimiter=',', usecols=[0], converters={0: lambda s: numpy.uint32(s.replace('"', ''))})
        else:
            self._logger.error("file: {0} does not exist".format(self._ip_localization_file))

    def get_ip_geo_localization(self, ip):

        self._logger.debug("Getting {0} geo localization ".format(ip))
        if ip.strip() != "" and ip is not None:

            result = linecache.getline(self._ip_localization_file, bisect.bisect(self._ip_localization_ranges, Util.ip_to_int(ip)))
            result.strip('\n')

            reader = csv.reader([result])
            row = reader.next()

            geo_loc = ";".join(row[4:6]) + " " + ";".join(row[8:9])            
            domain = row[9:10][0]

            result = {"geo_loc": geo_loc, "domain": domain}

        return result

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

import json
import urllib2
import urllib
import logging



class Reputation(object):
    def __init__(self,conf, logger=None):

        self._fb_app_id = conf['app_id']
        self._fb_app_secret = conf['app_secret']
        self._logger = logging.getLogger('OA.FB')  if logger else Util.get_logger('OA.FB',create_file=False) 

    def check(self, ips=None, urls=None, cat=False):
        self._logger.info("Threat-Exchange reputation check starts...")
        reputation_dict = {}
        data = []

        if ips is not None:
            values = ips
            qtype = 'IP_ADDRESS'
            getopt = 'GET_IP_'
        elif urls is not None:
            values = urls
            qtype = 'DOMAIN'
            getopt = 'GET_NAME_'
        else:
            self._logger.info("Need either an ip or an url to check reputation.")
            return reputation_dict

        for val in values:
            query_params = urllib.urlencode({
                'type': qtype,
                'text': val
            })

            indicator_request = {
                'method': 'GET',
                'name': "{0}{1}".format(getopt, val.replace('.', '_')),
                'relative_url': "/v2.4/threat_indicators?{0}".format(query_params)
            }

            descriptor_request = {
                'method': 'GET',
                'relative_url': '/v2.4/{result=' + getopt + val.replace('.', '_') + ':$.data.*.id}/descriptors'
            }

            data.append(indicator_request)
            data.append(descriptor_request)

            reputation_dict.update(self._request_reputation(data, val))
            data = []

        if len(data) > 0:
            reputation_dict.update(self._request_reputation(data))

        return reputation_dict

    def _request_reputation(self, data, name):
        reputation_dict = {}
        token = "{0}|{1}".format(self._fb_app_id, self._fb_app_secret)
        request_body = {
            'access_token': token,
            'batch': data
        }
        
        request_body = urllib.urlencode(request_body)

        url = "https://graph.facebook.com/"
        content_type = {'Content-Type': 'application/json'}
        request = urllib2.Request(url, request_body, content_type)

        try:
            str_response = urllib2.urlopen(request).read()
            response = json.loads(str_response)
        except urllib2.HTTPError as e:
            self._logger.info("Error calling ThreatExchange in module fb: " + e.message)
            reputation_dict[name] = self._get_reputation_label('UNKNOWN')
            return reputation_dict

        for row in response:
            if row is None:
                continue

            if row['code'] != 200:
                reputation_dict[name] = self._get_reputation_label('UNKNOWN')
                return reputation_dict
            if 'body' in row: 
                try:
                    row_response = json.loads(row['body']) 
                except ValueError as e:
                    self._logger.error("Error reading JSON body response in fb module: " + e.message)

                if 'data' in row_response and row_response['data'] != []: 
                    row_response_data = row_response['data']
                    name = row_response_data[0]['indicator']['indicator']
                    reputation_dict[name] = self._get_reputation_label(row_response_data[0]['status'])
                else:
                    reputation_dict[name] = self._get_reputation_label('UNKNOWN')
            else:
                reputation_dict[name] = self._get_reputation_label('UNKNOWN')

        return reputation_dict

    def _get_reputation_label(self,reputation):

        if reputation == 'UNKNOWN':
            return "fb:UNKNOWN:-1"
        elif reputation == 'NON_MALICIOUS':
            return "fb:NON_MALICIOUS:0"
        elif reputation == 'SUSPICIOUS':
            return "fb:SUSPICIOUS:2"
        elif reputation == 'MALICIOUS':
            return "fb:MALICIOUS:3"

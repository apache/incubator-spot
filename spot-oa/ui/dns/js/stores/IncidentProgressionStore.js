//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';
const QUERY_VAR = 'dnsQuery';
const CLIENT_IP_VAR = 'clientIp';

class IncidentProgressionStore extends ObservableGraphQLStore {
    constructor() {
        super();

        this.filterName = null;
    }

    getQuery() {
        return `
            query($date:SpotDateType, $dnsQuery: String, $clientIp:SpotIpType) {
                dns {
                    threat {
                        incidentProgression(date:$date, dnsQuery:$dnsQuery, clientIp:$clientIp) {
                            ...QueryFragment
                            ...ClientIpFragment
                        }
                    }
                }
            }

            fragment QueryFragment on DnsIncidentProgressionQueryType {
                dnsQuery
            }

            fragment ClientIpFragment on DnsIncidentProgressionClientIpType {
                clientIp
            }
        `;
    }

    unboxData(data) {
        return data.dns.threat.incidentProgression;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setFilter(name, value) {
        this.filterName = name==QUERY_VAR?name:CLIENT_IP_VAR;
        this.setVariable(this.filterName, value);
    }

    getFilterName() {
        return this.filterName;
    }

    getFilterValue() {
        return this.getVariable(this.filterName);
    }

    clearFilter() {
        this.unsetVariable(this.filterName);
        this.filterName = null;
    }
}

const ips = new IncidentProgressionStore();

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      ips.setDate(action.date);
      ips.clearFilter();
      ips.resetData();

      break;
    case SpotConstants.SELECT_COMMENT:
      ips.clearFilter();

      let filterName = QUERY_VAR in action.comment ? QUERY_VAR : CLIENT_IP_VAR;
      ips.setFilter(filterName, action.comment[filterName]);

      ips.sendQuery();

      break;
  }
});

module.exports = ips;

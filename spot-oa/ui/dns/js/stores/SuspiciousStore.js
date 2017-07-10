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
const SpotUtils = require('../../../js/utils/SpotUtils');

const ObservableWithHeadersGraphQLStore = require('../../../js/stores/ObservableWithHeadersGraphQLStore');

const DATE_VAR = 'date';
const IP_VAR = 'ipClient';
const DNS_VAR = 'dnsQuery';

const CHANGE_FILTER_EVENT = 'change_filter';
const HIGHLIGHT_THREAT_EVENT = 'hightlight_thread';
const UNHIGHLIGHT_THREAT_EVENT = 'unhightlight_thread';
const SELECT_THREAT_EVENT = 'select_treath';

class SuspiciousStore extends ObservableWithHeadersGraphQLStore {
    constructor() {
        super();

        this.headers = {
            frame_time: 'Timestamp',
            ip_dst: 'Client IP',
            dns_qry_name: 'Query',
            query_rep: ' ',
            dns_qry_class_name: 'Query Class',
            dns_qry_type_name: 'Query Type',
            dns_qry_rcode_name: 'Response Code'
        };

        this.ITERATOR = ['frame_time', 'ip_dst', 'dns_qry_name', 'query_rep', 'dns_qry_class_name', 'dns_qry_type_name', 'dns_qry_rcode_name'];

        this.highlightedThread = null;
        this.selectedThread = null;
    }

    getQuery() {
        return `
            query($date:SpotDateType!,$dnsQuery:String,$ipClient:SpotIpType) {
                dns {
                    suspicious(date: $date, dnsQuery:$dnsQuery, clientIp:$ipClient) {
                        unix_tstamp: unixTimestamp
                        dns_qry_type: dnsQueryType
                        frame_len: frameLength
                        dns_qry_type_name: dnsQueryTypeLabel
                        dns_sev: dnsQuerySev
                        ip_sev: clientIpSev
                        frame_time: frameTime
                        dns_qry_class_name: dnsQueryClassLabel
                        dns_qry_rcode: dnsQueryRcode
                        score
                        dns_qry_rcode_name: dnsQueryRcodeLabel
                        network_context: networkContext
                        tld
                        dns_qry_class: dnsQueryClass
                        ip_dst: clientIp
                        query_rep: dnsQueryRep
                        dns_qry_name: dnsQuery
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.dns.suspicious;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setFilter(filter) {
        if (filter==='') {
            this.unsetVariable(IP_VAR);
            this.unsetVariable(DNS_VAR);
        }
        else if (SpotUtils.IP_V4_REGEX.test(filter)) {
            this.unsetVariable(DNS_VAR);
            this.setVariable(IP_VAR, filter);
        }
        else {
            this.unsetVariable(IP_VAR);
            this.setVariable(DNS_VAR, filter);
        }

        this.notifyListeners(CHANGE_FILTER_EVENT);
    }

    getFilter() {
        return this.getVariable(IP_VAR) || this.getVariable(DNS_VAR) || '';
    }

    addChangeFilterListener(callback) {
        this.addListener(CHANGE_FILTER_EVENT, callback);
    }

    removeChangeFilterListener(callback) {
        this.removeListener(CHANGE_FILTER_EVENT, callback);
    }

    highlightThreat(threat) {
        this.highlightedThread = threat;
        this.notifyListeners(HIGHLIGHT_THREAT_EVENT);
    }

    getHighlightedThreat() {
        return this.highlightedThread;
    }

    unhighlightThreat() {
        this.highlightedThread = null;
        this.notifyListeners(UNHIGHLIGHT_THREAT_EVENT);
    }

    addThreatHighlightListener(callback) {
        this.addListener(HIGHLIGHT_THREAT_EVENT, callback);
    }

    removeThreatHighlightListener(callback) {
        this.removeListener(HIGHLIGHT_THREAT_EVENT, callback);
    }

    addThreatUnhighlightListener(callback) {
        this.addListener(UNHIGHLIGHT_THREAT_EVENT, callback);
    }

    removeThreatUnhighlightListener(callback) {
        this.removeListener(UNHIGHLIGHT_THREAT_EVENT, callback);
    }

    selectThreat(threat) {
        this.selectedThread = threat;
        this.notifyListeners(SELECT_THREAT_EVENT);
    }

    getSelectedThreat() {
        return this.selectedThread;
    }

    addThreatSelectListener(callback) {
        this.addListener(SELECT_THREAT_EVENT, callback);
    }

    removeThreatSelectListener(callback) {
        this.removeListener(SELECT_THREAT_EVENT, callback);
    }
}

const ss = new SuspiciousStore();

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_FILTER:
      ss.setFilter(action.filter);
      break;
    case SpotConstants.UPDATE_DATE:
      ss.setDate(action.date);
      break;
    case SpotConstants.RELOAD_SUSPICIOUS:
      ss.sendQuery();
      break;
    case SpotConstants.HIGHLIGHT_THREAT:
      ss.highlightThreat(action.threat);
      break;
    case SpotConstants.UNHIGHLIGHT_THREAT:
      ss.unhighlightThreat();
      break;
    case SpotConstants.SELECT_THREAT:
      ss.selectThreat(action.threat);
      break;
  }
});

module.exports = ss;

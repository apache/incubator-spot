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
const SpotConstants = require('../../../js/constants/SpotConstants');
const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotUtils = require('../../../js/utils/SpotUtils');

const ObservableWithHeadersGraphQLStore = require('../../../js/stores/ObservableWithHeadersGraphQLStore');

const DATE_VAR = 'date';
const URI_VAR = 'uri';
const CLIENT_IP_VAR = 'clientIp';

const CHANGE_FILTER_EVENT = 'change_filter';
const HIGHLIGHT_THREAT_EVENT = 'hightlight_thread';
const UNHIGHLIGHT_THREAT_EVENT = 'unhightlight_thread';
const SELECT_THREAT_EVENT = 'select_treath';

class SuspiciousStore extends ObservableWithHeadersGraphQLStore {
    constructor() {
        super();

        this.filterName = null;
        this.highlightedThread = null;
        this.selectedThread = null;

        this.headers = {
            datetime: 'Time',
            clientip: 'Client IP',
            host: 'Host',
            uri_rep: ' ',
            webcat: 'Web Category',
            respcode_name: 'Response Code'
        };

        this.ITERATOR = ['datetime', 'clientip', 'host', 'uri_rep', 'webcat', 'respcode_name'];
    }

    getQuery() {
        return `
            query($date:SpotDateType!,$uri:String,$clientIp:SpotIpType) {
                proxy {
                    suspicious(date: $date, uri:$uri,clientIp:$clientIp) {
                        uriport: uriPort
                        uripath: uriPath
                        uriquery: uriQuery
                        datetime
                        network_context: networkContext
                        duration
                        useragent: userAgent
                        uri_rep: uriRep
                        score
                        username
                        webcat: webCategory
                        resconttype: responseContentType
                        host
                        referer
                        csbytes: clientToServerBytes
                        fulluri: uri
                        serverip: serverIp
                        reqmethod: requestMethod
                        respcode: responseCode
                        clientip: clientIp
                        respcode_name: responseCodeLabel
                        scbytes: serverToClientBytes
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.proxy.suspicious;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setFilter(filter) {
        if (filter === '') {
            this.filterName = null;
            this.unsetVariable(URI_VAR);
            this.unsetVariable(CLIENT_IP_VAR);
        }
        else if (SpotUtils.IP_V4_REGEX.test(filter)) {
            this.unsetVariable(URI_VAR, filter);
            this.setVariable(CLIENT_IP_VAR, filter);
        }
        else {
            this.unsetVariable(CLIENT_IP_VAR);
            this.setVariable(URI_VAR, filter);
        }

        this.notifyListeners(CHANGE_FILTER_EVENT);
    }

    getFilter() {
        return this.getVariable(CLIENT_IP_VAR) || this.getVariable(URI_VAR) || '';
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

    addThreatHighlightListener(callback) {
        this.addListener(HIGHLIGHT_THREAT_EVENT, callback);
    }

    removeThreatHighlightListener(callback) {
        this.removeListener(HIGHLIGHT_THREAT_EVENT, callback);
    }

    unhighlightThreat() {
        this.highlightedThread = null;
        this.notifyListeners(UNHIGHLIGHT_THREAT_EVENT);
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

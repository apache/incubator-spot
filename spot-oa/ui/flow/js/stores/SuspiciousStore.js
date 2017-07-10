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

const ObservableWithHeadersGraphQLStore = require('../../../js/stores/ObservableWithHeadersGraphQLStore');

const CHANGE_FILTER_EVENT = 'change_filter';
const HIGHLIGHT_THREAT_EVENT = 'hightlight_thread';
const UNHIGHLIGHT_THREAT_EVENT = 'unhightlight_thread';
const SELECT_THREAT_EVENT = 'select_treath';

const DATE_VAR = 'date';
const IP_VAR = 'ip';

class SuspiciousStore extends ObservableWithHeadersGraphQLStore {
    constructor() {
        super();

        this.highlightedThread = null;
        this.selectedThread = null;

        this.headers = {
            rank: 'Rank',
            tstart: 'Time',
            srcIP: 'Source IP',
            srcIP_rep: ' ',
            dstIP: 'Destination IP',
            dstIP_rep: ' ',
            sport: 'Source Port',
            dport: 'Destination Port',
            proto: 'Protocol',
            ipkt: 'Input Packets',
            ibyt: 'Input Bytes',
            opkt: 'Output Packets',
            obyt: 'Output Bytes'
        };

        this.ITERATOR = ['rank', 'tstart', 'srcIP', 'srcIP_rep', 'dstIP', 'dstIP_rep', 'sport', 'dport', 'proto', 'ipkt', 'ibyt', 'opkt', 'obyt'];
    }

    getQuery() {
        return `
            query($date:SpotDateType!,$ip:SpotIpType) {
                flow {
                    suspicious(date: $date, ip:$ip) {
                        rank
                        tstart
                        srcIP: srcIp
                        sport: srcPort
                        srcIpInternal: srcIp_isInternal
                        srcIP_rep: srcIp_rep
                        srcGeo: srcIp_geoloc
                        srcDomain: srcIp_domain
                        dstIP: dstIp
                        dport: dstPort
                        destIpInternal: dstIp_isInternal
                        dstIP_rep: dstIp_rep
                        dstGeo: dstIp_geoloc
                        dstDomain: dstIp_domain
                        proto: protocol
                        ipkt: inPkts
                        ibyt: inBytes
                        opkt: outPkts
                        obyt: outBytes
                        score
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.suspicious;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setFilter(ip) {
        this.setVariable(IP_VAR, ip || '');
        this.notifyListeners(CHANGE_FILTER_EVENT);
    }

    getFilter() {
        return this.getVariable(IP_VAR);
    }

    addChangeFilterListener(callback) {
        this.addListener(CHANGE_FILTER_EVENT, callback);
    }

    removeChangeFilterListener(callback) {
        this.removeListener(CHANGE_FILTER_EVENT, callback);
    }


    highlightThreat (threat) {
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

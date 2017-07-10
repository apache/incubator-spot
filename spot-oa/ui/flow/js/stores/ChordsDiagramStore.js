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

const IP_VAR = 'ip';
const DATE_VAR = 'date';

class ChordsDiagramStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date: SpotDateType!, $ip: SpotIpType!) {
                flow {
                    ipDetails(date: $date, ip: $ip) {
                        srcip: srcIp
                        dstip: dstIp
                        ipkts: inPkts
                        ibytes: inBytes
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.ipDetails;
    }

    setIp(ip) {
      this.setVariable(IP_VAR, ip);
    }

    getIp() {
      this.getVariable(IP_VAR);
    }

    setDate(date) {
      this.setVariable(DATE_VAR, date);
    }
}

const cds = new ChordsDiagramStore();

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
        cds.setDate(action.date);
        break;
    case SpotConstants.SELECT_IP:
      cds.setIp(action.ip);
      break;
    case SpotConstants.RELOAD_SUSPICIOUS:
      cds.resetData();
      break;
    case SpotConstants.RELOAD_DETAILS_VISUAL:
      cds.sendQuery();
      break;
  }
});

module.exports = cds;

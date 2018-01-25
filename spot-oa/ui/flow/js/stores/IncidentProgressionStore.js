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
const IP_VAR = 'ip';

class IncidentProgressionStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!, $ip:SpotIpType!) {
                flow {
                    threat {
                        incidentProgression(date: $date, ip: $ip) {
                            name
                            children {
                                name
                                children {
                                    name
                                }
                            }
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.threat.incidentProgression;
    }

    setDate(date)
    {
        this.setVariable(DATE_VAR, date);
    }

    setIp(ip)
    {
        this.setVariable(IP_VAR, ip);
    }

    getIp()
    {
        return this.getVariable(IP_VAR);
    }
}

const ips = new IncidentProgressionStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            ips.setDate(action.date);
            break;
        case SpotConstants.RELOAD_COMMENTS:
            ips.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            ips.setIp(action.comment.ip);
            ips.sendQuery();

            break;
    }
});

module.exports = ips;

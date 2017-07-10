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
const URI_VAR = 'uri';

class IncidentProgressionStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!,$uri:String!) {
                proxy {
                    threat {
                        incidentProgression(date:$date,uri:$uri) {
                            requests {
                                reqmethod: requestMethod
                                clientip: clientIp
                                resconttype: responseContentType
                                referer
                            }
                            fulluri: uri
                            referer_for: refererFor
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.proxy.threat.incidentProgression;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setUri(uri) {
        this.setVariable(URI_VAR, uri);
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
            ips.setUri(action.comment.uri);
            ips.sendQuery();

            break;
    }
});

module.exports = ips;

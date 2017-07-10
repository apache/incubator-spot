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

const DATE_VAR = 'date';
const URI_VAR = 'uri';
const CLIENT_IP_VAR = 'clientIp';

class DetailsStore extends ObservableWithHeadersGraphQLStore {
    constructor() {
        super();

        this.headers = {
            datetime: 'Time',
            clientip: 'Client IP',
            host: 'Host',
            webcat: 'Web Category',
            respcode_name: 'Response Code',
            reqmethod: 'Request Method',
            useragent: 'User Agent',
            resconttype: 'MIME Type',
            referer: 'Referer',
            uriport: 'URI Port',
            serverip: 'Proxy IP',
            scbytes: 'Server Bytes',
            csbytes: 'Client Bytes',
            fulluri: 'Full URI'
        };

        this.ITERATOR = ['datetime', 'clientip', 'host', 'webcat', 'respcode_name', 'reqmethod', 'useragent', 'resconttype', 'referer', 'uriport', 'serverip', 'scbytes', 'csbytes', 'fulluri'];
    }

    getQuery() {
        return `
            query($date:SpotDateType!,$uri:String!,$clientIp:SpotIpType!) {
                proxy {
                    edgeDetails(date:$date,uri:$uri,clientIp:$clientIp) {
                        uriport: uriPort
                        webcat: webCategory
                        resconttype: responseContentType
                        datetime
                        host
                        referer
                        csbytes: clientToServerBytes
                        useragent: userAgent
                        fulluri: uri
                        serverip: serverIp
                        reqmethod: requestMethod
                        respcode: responseCode
                        respcode_name: responseCodeLabel
                        clientip: clientIp
                        scbytes: serverToClientBytes
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.proxy.edgeDetails;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setClientIp(clientIp) {
        this.setVariable(CLIENT_IP_VAR, clientIp);
    }

    setUri(uri) {
        this.setVariable(URI_VAR, uri);
    }
}

const ds = new DetailsStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            ds.setDate(action.date);
            break;
        case SpotConstants.SELECT_THREAT:
            ds.setClientIp(action.threat.clientip);
            ds.setUri(action.threat.fulluri);
            break;
        case SpotConstants.RELOAD_SUSPICIOUS:
            ds.resetData();
            break;
        case SpotConstants.RELOAD_DETAILS:
            ds.sendQuery();
            break;
    }
});

module.exports = ds;

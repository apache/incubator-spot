// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

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

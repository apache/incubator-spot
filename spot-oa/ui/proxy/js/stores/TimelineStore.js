// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';
const URI_VAR = 'uri';

class TimelineStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!,$uri:String!) {
                proxy {
                    threat {
                        timeline(date:$date, uri:$uri) {
                            duration
                            clientip: clientIp
                            tend: endDatetime
                            respcode: responseCode
                            tstart: startDatetime
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.proxy.threat.timeline;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    getDate() {
        return this.getVariable(DATE_VAR);
    }

    setUri(uri) {
        this.setVariable(URI_VAR, uri);
    }

    getUri() {
        return this.getVariable(URI_VAR);
    }
}

const ts = new TimelineStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            ts.setDate(action.date);
            break;
        case SpotConstants.RELOAD_COMMENTS:
            ts.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            ts.setUri(action.comment.uri);
            ts.sendQuery();
            break;
    }
});

module.exports = ts;

// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';
const IP_VAR = 'ip';

class TimelineStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!, $ip:SpotIpType!) {
                flow {
                    threat {
                        timeline(date: $date, ip: $ip) {
                            tstart
                            srcip: srcIp
                            sport: srcPort
                            dstip: dstIp
                            dport: dstPort
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.threat.timeline;
    }

    setDate(date){
        this.setVariable(DATE_VAR, date);
    }

    getDate() {
        return this.getVariable(DATE_VAR);
    }

    setIp(ip) {
        this.setVariable(IP_VAR, ip);
    }

    getIp () {
        return this.getVariable(IP_VAR);
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
            ts.setIp(action.comment.ip);

            ts.sendQuery();

            break;
    }
});

module.exports = ts;

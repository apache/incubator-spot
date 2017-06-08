// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';
const IP_VAR = 'ip';

class ImpactAnalysisStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!, $ip:SpotIpType!) {
                flow {
                    threat {
                        impactAnalysis(date: $date, ip: $ip) {
                            name
                            size
                            children {
                                name
                                size
                                children {
                                    name
                                    size
                                }
                            }
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.threat.impactAnalysis;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setIp(ip) {
        this.setVariable(IP_VAR, ip);
    }

    getIp() {
        return this.getVariable(IP_VAR);
    }
}

const ias = new ImpactAnalysisStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            ias.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            ias.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            ias.setIp(action.comment.ip);
            ias.sendQuery();

            break;
    }
});

module.exports = ias;

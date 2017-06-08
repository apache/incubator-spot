// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const d3 = require('d3');

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const NetflowConstants = require('../constants/NetflowConstants');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';
const IP_VAR = 'ip';
let WORLD_DATA = null;

class GlobeViewStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!, $ip:SpotIpType!) {
                flow {
                    threat {
                        geoLocalization(date: $date, ip: $ip) {
                                sourceips: srcIps {
                                geometry {
                                    coordinates
                                }
                                properties {
                                    ip
                                    type
                                    location
                                }
                            }
                            destips: dstIps {
                                geometry {
                                    coordinates
                                }
                                properties {
                                    ip
                                    type
                                    location
                                }
                            }
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.threat.geoLocalization;
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

    getWorldData() {
        return WORLD_DATA;
    }

    sendQuery() {
        if (WORLD_DATA instanceof Object) {
            super.sendQuery();
        }
        else if (WORLD_DATA===true) {
            // Do nothing, already loading world data
        }
        else {
            WORLD_DATA = true; // Signal world data is loading
            d3.json(NetflowConstants.API_WORLD_110M, (error, response) => {
                WORLD_DATA = response;
                super.sendQuery();
            });
        }
    }
}

const gvs = new GlobeViewStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            gvs.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            gvs.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            gvs.setIp(action.comment.ip);
            gvs.sendQuery();

            break;
    }
});

module.exports = gvs;

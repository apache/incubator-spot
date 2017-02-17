// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

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

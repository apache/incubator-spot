// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date'
const CLIENT_IP_VAR = 'ip';

class DendrogramStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!,$ip:SpotIpType!) {
                dns {
                    ipDetails(date:$date, ip:$ip) {
                        dns_qry_name: dnsQuery
                        dns_a: dnsQueryAnswers
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.dns.ipDetails;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setClientIp (clientIp) {
        this.setVariable(CLIENT_IP_VAR, clientIp);
    }

    unsetClientIp (clientIp) {
        this.unsetVariable(CLIENT_IP_VAR);
    }

    getClientIp() {
        return this.getVariable(CLIENT_IP_VAR);
    }
}

const ds = new DendrogramStore();

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      ds.setDate(action.date);
      break;
    case SpotConstants.SELECT_IP:
      ds.setClientIp(action.ip);
      break;
    case SpotConstants.RELOAD_SUSPICIOUS:
      ds.unsetClientIp();
      ds.resetData();
      break;
    case SpotConstants.RELOAD_DETAILS_VISUAL:
      ds.sendQuery();
      break;
  }
});

module.exports = ds;

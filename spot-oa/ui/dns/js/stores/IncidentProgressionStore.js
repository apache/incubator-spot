// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

var fields = ['title', 'summary'];
var filterName;

const DATE_VAR = 'date';

class IncidentProgressionStore extends ObservableGraphQLStore {
    static QUERY: 'dnsQuery'
    static CLIENT_IP: 'clientIp'

    constructor() {
        super();

        this.filterName = null;
    }

    getQuery() {
        return `
            query($date:SpotDateType, $dnsQuery: String, $clientIp:SpotIpType) {
                dns {
                    threat {
                        incidentProgression(date:$date, dnsQuery:$dnsQuery, clientIp:$clientIp) {
                            name
                            children {
                                name
                            }
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.dns.threat.incidentProgression;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }

    setFilter(name, value) {
        this.filterName = name==IncidentProgressionStore.QUERY?name:IncidentProgressionStore.CLIENT_IP;
        this.setVariable(this.filterName, value);
    }

    clearFilter() {
        this.unsetVariable(this.filterName);
        this.filterName = null;
    }
}

const ips = new IncidentProgressionStore();

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      ips.setDate(action.date);
      ips.clearFilter();
      ips.resetData();

      break;
    case SpotConstants.SELECT_COMMENT:
      ips.clearFilter();

      let filterName = IncidentProgressionStore.QUERY in action.comment ? IncidentProgressionStore.QUERY : IncidentProgressionStore.CLIENT_IP;
      ips.setFilter(filterName, action.comment[filterName]);

      ips.sendQuery();

      break;
  }
});

module.exports = ips;

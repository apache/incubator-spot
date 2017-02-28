// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';
const QUERY_VAR = 'dnsQuery';
const CLIENT_IP_VAR = 'clientIp';

class IncidentProgressionStore extends ObservableGraphQLStore {
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
                            ...QueryFragment
                            ...ClientIpFragment
                        }
                    }
                }
            }

            fragment QueryFragment on DnsIncidentProgressionQueryType {
                dnsQuery
            }

            fragment ClientIpFragment on DnsIncidentProgressionClientIpType {
                clientIp
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
        this.filterName = name==QUERY_VAR?name:CLIENT_IP_VAR;
        this.setVariable(this.filterName, value);
    }

    getFilterName() {
        return this.filterName;
    }

    getFilterValue() {
        return this.getVariable(this.filterName);
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

      let filterName = QUERY_VAR in action.comment ? QUERY_VAR : CLIENT_IP_VAR;
      ips.setFilter(filterName, action.comment[filterName]);

      ips.sendQuery();

      break;
  }
});

module.exports = ips;

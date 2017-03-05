// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotConstants = require('../../../js/constants/SpotConstants');
var DnsConstants = require('../constants/DnsConstants');
var RestStore = require('../../../js/stores/RestStore');
var assign = require('object-assign');

var SRC_IP_FILTER = 'ip_dst';

var DendrogramStore = assign(new RestStore(DnsConstants.API_VISUAL_DETAILS), {
  setDate: function (date)
  {
    this.setEndpoint(DnsConstants.API_VISUAL_DETAILS.replace('${date}', date.replace(/-/g, '')));
  },
  setSrcIp: function (srcIp)
  {
    this.setRestFilter(SRC_IP_FILTER, srcIp);
  },
  getSrcIp: function ()
  {
    return this.getRestFilter(SRC_IP_FILTER);
  },
  setData: function (data)
  {
    this._data = data;

    this.emitChangeData();
  }
});

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      DendrogramStore.setDate(action.date);
      break;
    case SpotConstants.SELECT_IP:
      DendrogramStore.setSrcIp(action.ip);
      break;
    case SpotConstants.RELOAD_SUSPICIOUS:
      DendrogramStore.setSrcIp('');
      DendrogramStore.resetData();
      break;
    case SpotConstants.RELOAD_DETAILS_VISUAL:
      DendrogramStore.reload();
      break;
  }
});

module.exports = DendrogramStore;

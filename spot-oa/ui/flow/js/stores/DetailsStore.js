// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var d3 = require('d3');
var assign = require('object-assign');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotConstants = require('../../../js/constants/SpotConstants');
var NetflowConstants = require('../constants/NetflowConstants');
var RestStore = require('../../../js/stores/RestStore');

var DATE_FILTER = 'date';
var SRC_IP_FILTER = 'src_ip';
var DST_IP_FILTER = 'dst_ip';
var TIME_FILTER = 'time';

var DetailsStore = assign(new RestStore(NetflowConstants.API_DETAILS), {
  _parser: d3.dsv('\t', 'text/plain'),
  errorMessages: {
    404: 'No details available'
  },
  headers: {
    tstart: 'Time',
    srcip: 'Source IP',
    dstip: 'Destination IP',
    sport: 'Source Port',
    dport: 'Destination Port',
    proto: 'Protocol',
    flags: 'Flags',
    tos: 'Type Of Service',
    ibytes: 'Input Bytes',
    ipkts: 'Input Packets',
    obytes:  'Output Bytes',
    opkts: 'Output Packets',
    rip: 'Router IP',
    input: 'Input iface',
    output: 'Output iface'
  },
  ITERATOR: ['tstart', 'srcip', 'dstip', 'sport', 'dport', 'proto', 'flags', 'tos', 'ibytes', 'ipkts', 'obytes', 'opkts', 'rip', 'input', 'output'],
  setDate: function (date)
  {
    this.setRestFilter(DATE_FILTER, date.replace(/-/g, ''));
  },
  setSrcIp: function (ip)
  {
    this.setRestFilter(SRC_IP_FILTER, ip.replace(/\./g, '_'));
  },
  setDstIp: function (ip)
  {
    this.setRestFilter(DST_IP_FILTER, ip.replace(/\./g, '_'));
  },
  setTime: function (time)
  {
    var timeParts = time.split(' ')[1].split(':');
    this.setRestFilter(TIME_FILTER, timeParts[0] + '-' + timeParts[1]);
  }
});

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      DetailsStore.setDate(action.date);
      break;
    case SpotConstants.SELECT_THREAT:
      DetailsStore.setSrcIp(action.threat.srcIP);
      DetailsStore.setDstIp(action.threat.dstIP);
      DetailsStore.setTime(action.threat.tstart);
      break;
    case SpotConstants.RELOAD_SUSPICIOUS:
      DetailsStore.resetData();
      break;
    case SpotConstants.RELOAD_DETAILS:
      DetailsStore.reload();
      break;
  }
});

module.exports = DetailsStore;

var d3 = require('d3');
var assign = require('object-assign');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
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
    bytes: 'Bytes',
    pkts: 'Packets',
    input: 'Input',
    output: 'Output'
  },
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

OniDispatcher.register(function (action) {
  switch (action.actionType) {
    case OniConstants.UPDATE_DATE:
      DetailsStore.setDate(action.date);
      break;
    case OniConstants.SELECT_THREAT:
      DetailsStore.setSrcIp(action.threat.srcIP);
      DetailsStore.setDstIp(action.threat.dstIP);
      DetailsStore.setTime(action.threat.tstart);
      break;
    case OniConstants.RELOAD_SUSPICIOUS:
      DetailsStore.resetData();
      break;
    case OniConstants.RELOAD_DETAILS:
      DetailsStore.reload();
      break;
  }
});

module.exports = DetailsStore;

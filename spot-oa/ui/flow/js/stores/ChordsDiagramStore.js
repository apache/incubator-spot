var d3 = require('d3');
var assign = require('object-assign');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var NetflowConstants = require('../constants/NetflowConstants');
var RestStore = require('../../../js/stores/RestStore');

var DATE_FILTER = 'date';
var IP_FILTER = 'ip';

var ChordsDiagramStore = assign(new RestStore(NetflowConstants.API_VISUAL_DETAILS), {
  _parser: d3.dsv('\t', 'text/plain'),
  errorMessages: {
    404: 'No details available'
  },
  headers: {
    // TODO: Add Headers
  },
  setDate: function (date)
  {
    this.setRestFilter(DATE_FILTER, date.replace(/-/g, ''));
  },
  setIp: function (ip)
  {
    this.setRestFilter(IP_FILTER, ip.replace(/\./g, '_'));
  },
  getIp: function ()
  {
    return this.getRestFilter(IP_FILTER);
  }
});

OniDispatcher.register(function (action) {
  switch (action.actionType) {
    case OniConstants.UPDATE_DATE:
      ChordsDiagramStore.setDate(action.date);
      break;
    case OniConstants.SELECT_IP:
      ChordsDiagramStore.setIp(action.ip);
      break;
    case OniConstants.RELOAD_DETAILS_VISUAL:
      ChordsDiagramStore.reload();
      break;
  }
});

module.exports = ChordsDiagramStore;

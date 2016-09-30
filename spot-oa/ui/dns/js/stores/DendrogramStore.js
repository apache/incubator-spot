var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var DnsConstants = require('../constants/DnsConstants');
var RestStore = require('../../../js/stores/RestStore');
var assign = require('object-assign');

var SRC_IP_FILTER = 'ip_dst';
var DATA_ROWS_LIMIT = 1000;

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
    this._data.data = this._data.data.slice(0, DATA_ROWS_LIMIT);

    this.emitChangeData();
  }
});

OniDispatcher.register(function (action) {
  switch (action.actionType) {
    case OniConstants.UPDATE_DATE:
      DendrogramStore.setDate(action.date);
      break;
    case OniConstants.SELECT_IP:
      DendrogramStore.setSrcIp(action.ip);
      break;
    case OniConstants.RELOAD_SUSPICIOUS:
      DendrogramStore.setSrcIp('');
      DendrogramStore.resetData();
      break;
    case OniConstants.RELOAD_DETAILS_VISUAL:
      DendrogramStore.reload();
      break;
  }
});

module.exports = DendrogramStore;

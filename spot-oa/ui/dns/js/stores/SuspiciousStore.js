var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var DnsConstants = require('../constants/DnsConstants');
var RestStore = require('../../../js/stores/RestStore');
var OniUtils = require('../../../js/utils/OniUtils');
var assign = require('object-assign');

var IP_FILTER = 'ip_dst';
var DNS_FILTER = 'dns_qry_name';

var CHANGE_FILTER_EVENT = 'change_filter';
var CHANGE_DATE_EVENT = 'change_date';
var HIGHLIGHT_THREAT_EVENT = 'hightlight_thread';
var UNHIGHLIGHT_THREAT_EVENT = 'unhightlight_thread';
var SELECT_THREAT_EVENT = 'select_treath';

var filter = '';
var filterName = '';
var date = '';
var highlightedThread = null;
var selectedThread = null;
var unfilteredData = null;

var SuspiciousStore = assign(new RestStore(DnsConstants.API_SUSPICIOUS), {
  errorMessages: {
    404: 'Please choose a different date, no data has been found'
  },
  headers: {
    frame_time: 'Timestamp',
    ip_dst: 'Client IP',
    dns_qry_name: 'Query',
    query_rep: ' ',
    dns_qry_class_name: 'Query Class',
    dns_qry_type_name: 'Query Type',
    dns_qry_rcode_name: 'Response Code'
  },
  ITERATOR: ['frame_time', 'ip_dst', 'dns_qry_name', 'query_rep', 'dns_qry_class_name', 'dns_qry_type_name', 'dns_qry_rcode_name'],
  getData: function ()
  {
    var state;

    if (!filter || !unfilteredData)
    {
        state = this._data;
    }
    else
    {
        state = assign(
            {},
            unfilteredData,
            {
                data: unfilteredData.data.filter(function (item)
                {
                    return filterName===IP_FILTER ? item[filterName]==filter : item[filterName].indexOf(filter)>=0;
                })
            }
        );
    }

    state.data = state.data.filter(function (item) {
        return item.dns_sev=="0";
    });

    if (state.data.length>OniConstants.MAX_SUSPICIOUS_ROWS) state.data = state.data.slice(0, OniConstants.MAX_SUSPICIOUS_ROWS);

    return state;
  },
  setData: function (data)
  {
    this._data = unfilteredData = data;

    this.emitChangeData();
  },
  setDate: function (date)
  {
    this.setEndpoint(DnsConstants.API_SUSPICIOUS.replace('${date}', date.replace(/-/g, '')));
  },
  setFilter: function (newFilter)
  {
    filter = newFilter;

    if (filter==='')
    {
      filterName = '';
      this.removeRestFilter(IP_FILTER);
      this.removeRestFilter(DNS_FILTER);
    }
    else if (OniUtils.IP_V4_REGEX.test(filter))
    {
      this.removeRestFilter(DNS_FILTER);
      this.setRestFilter(IP_FILTER, filter);
      filterName = IP_FILTER;
    }
    else
    {
      this.removeRestFilter(IP_FILTER);
      this.setRestFilter(DNS_FILTER, filter);
      filterName = DNS_FILTER;
    }

    this.emitChangeFilter();
  },
  getFilter: function (){
    return filter;
  },
  emitChangeFilter: function () {
    this.emit(CHANGE_FILTER_EVENT);
  },
  addChangeFilterListener: function (callback) {
    this.on(CHANGE_FILTER_EVENT, callback);
  },
  removeChangeFilterListener: function (callback) {
    this.removeListener(CHANGE_FILTER_EVENT, callback);
  },
  highlightThreat: function (threat)
  {
    highlightedThread = threat;
    this.emitHighlightThreat();
  },
  getHighlightedThreat: function ()
  {
    return highlightedThread;
  },
  addThreatHighlightListener: function (callback)
  {
    this.on(HIGHLIGHT_THREAT_EVENT, callback);
  },
  removeThreatHighlightListener: function (callback)
  {
    this.removeListener(HIGHLIGHT_THREAT_EVENT, callback);
  },
  emitHighlightThreat: function ()
  {
    this.emit(HIGHLIGHT_THREAT_EVENT);
  },
  unhighlightThreat: function ()
  {
    highlightedThread = null;
    this.emitUnhighlightThreat();
  },
  addThreatUnhighlightListener: function (callback)
  {
    this.on(UNHIGHLIGHT_THREAT_EVENT, callback);
  },
  removeThreatUnhighlightListener: function (callback)
  {
    this.removeListener(UNHIGHLIGHT_THREAT_EVENT, callback);
  },
  emitUnhighlightThreat: function ()
  {
    this.emit(UNHIGHLIGHT_THREAT_EVENT);
  },
  selectThreat: function (threat)
  {
    selectedThread = threat;
    this.emitThreatSelect();
  },
  getSelectedThreat: function ()
  {
    return selectedThread;
  },
  addThreatSelectListener: function (callback)
  {
    this.on(SELECT_THREAT_EVENT, callback);
  },
  removeThreatSelectListener: function (callback)
  {
    this.removeListener(SELECT_THREAT_EVENT, callback);
  },
  emitThreatSelect: function ()
  {
    this.emit(SELECT_THREAT_EVENT);
  }
});

OniDispatcher.register(function (action) {
  switch (action.actionType) {
    case OniConstants.UPDATE_FILTER:
      SuspiciousStore.setFilter(action.filter);
      break;
    case OniConstants.UPDATE_DATE:
      SuspiciousStore.setDate(action.date);
      break;
    case OniConstants.RELOAD_SUSPICIOUS:
      SuspiciousStore.reload();
      break;
    case OniConstants.HIGHLIGHT_THREAT:
      SuspiciousStore.highlightThreat(action.threat);
      break;
    case OniConstants.UNHIGHLIGHT_THREAT:
      SuspiciousStore.unhighlightThreat();
      break;
    case OniConstants.SELECT_THREAT:
      SuspiciousStore.selectThreat(action.threat);
      break;
  }
});

module.exports = SuspiciousStore;

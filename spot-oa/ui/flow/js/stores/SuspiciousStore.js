var assign = require('object-assign');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotConstants = require('../../../js/constants/SpotConstants');
var NetflowConstants = require('../constants/NetflowConstants');
var RestStore = require('../../../js/stores/RestStore');
var SpotUtils = require('../../../js/utils/SpotUtils');

var IP_FILTER = 'ip_dst';

var CHANGE_FILTER_EVENT = 'change_filter';
var HIGHLIGHT_THREAT_EVENT = 'hightlight_thread';
var UNHIGHLIGHT_THREAT_EVENT = 'unhightlight_thread';
var SELECT_THREAT_EVENT = 'select_treath';

var filter = '';
var highlightedThread = null;
var selectedThread = null;
var unfilteredData = null;

var SuspiciousStore = assign(new RestStore(NetflowConstants.API_SUSPICIOUS), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    headers: {
        rank: 'Rank',
        tstart: 'Time',
        srcIP: 'Source IP',
        srcIP_rep: ' ',
        dstIP: 'Destination IP',
        dstIP_rep: ' ',
        sport: 'Source Port',
        dport: 'Destination Port',
        proto: 'Protocol',
        ipkt: 'Input Packets',
        ibyt: 'Input Bytes',
        opkt: 'Output Packets',
        obyt: 'Output Bytes'
    },
    ITERATOR: ['rank', 'tstart', 'srcIP', 'srcIP_rep', 'dstIP', 'dstIP_rep', 'sport', 'dport', 'proto', 'ipkt', 'ibyt', 'opkt', 'obyt'],
    getData: function () {
        var state;

        if (!filter || !unfilteredData) {
            state = this._data;
        }
        else {
            state = assign(
                {},
                unfilteredData,
                {
                    data: unfilteredData.data.filter(function (item) {
                        return item['srcIP'] == filter || item['dstIP'] == filter;
                    })
                }
            );
        }

        state.data = state.data.filter(function (item) {
            return item.sev == '0';
        });

        if (state.data.length > SpotConstants.MAX_SUSPICIOUS_ROWS) state.data = state.data.slice(0, SpotConstants.MAX_SUSPICIOUS_ROWS);

        return state;
    },
    setData: function (data) {
        this._data = unfilteredData = data;

        this.emitChangeData();
    },
    setDate: function (date) {
        this.setEndpoint(NetflowConstants.API_SUSPICIOUS.replace('${date}', date.replace(/-/g, '')));
    },
    setFilter: function (newFilter) {
        filter = newFilter;

        this.emitChangeFilter();
    },
    getFilter: function () {
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
    highlightThreat: function (threat) {
        highlightedThread = threat;
        this.emitHighlightThreat();
    },
    getHighlightedThreat: function () {
        return highlightedThread;
    },
    addThreatHighlightListener: function (callback) {
        this.on(HIGHLIGHT_THREAT_EVENT, callback);
    },
    removeThreatHighlightListener: function (callback) {
        this.removeListener(HIGHLIGHT_THREAT_EVENT, callback);
    },
    emitHighlightThreat: function () {
        this.emit(HIGHLIGHT_THREAT_EVENT);
    },
    unhighlightThreat: function () {
        highlightedThread = null;
        this.emitUnhighlightThreat();
    },
    addThreatUnhighlightListener: function (callback) {
        this.on(UNHIGHLIGHT_THREAT_EVENT, callback);
    },
    removeThreatUnhighlightListener: function (callback) {
        this.removeListener(UNHIGHLIGHT_THREAT_EVENT, callback);
    },
    emitUnhighlightThreat: function () {
        this.emit(UNHIGHLIGHT_THREAT_EVENT);
    },
    selectThreat: function (threat) {
        selectedThread = threat;
        this.emitThreatSelect();
    },
    getSelectedThreat: function () {
        return selectedThread;
    },
    addThreatSelectListener: function (callback) {
        this.on(SELECT_THREAT_EVENT, callback);
    },
    removeThreatSelectListener: function (callback) {
        this.removeListener(SELECT_THREAT_EVENT, callback);
    },
    emitThreatSelect: function () {
        this.emit(SELECT_THREAT_EVENT);
    }
});

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_FILTER:
            SuspiciousStore.setFilter(action.filter);
            break;
        case SpotConstants.UPDATE_DATE:
            SuspiciousStore.setDate(action.date);
            break;
        case SpotConstants.RELOAD_SUSPICIOUS:
            SuspiciousStore.reload();
            break;
        case SpotConstants.HIGHLIGHT_THREAT:
            SuspiciousStore.highlightThreat(action.threat);
            break;
        case SpotConstants.UNHIGHLIGHT_THREAT:
            SuspiciousStore.unhighlightThreat();
            break;
        case SpotConstants.SELECT_THREAT:
            SuspiciousStore.selectThreat(action.threat);
            break;
    }
});

module.exports = SuspiciousStore;

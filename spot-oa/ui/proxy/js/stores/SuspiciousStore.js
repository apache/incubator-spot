// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var assign = require('object-assign');
var d3 = require('d3');

var SpotConstants = require('../../../js/constants/SpotConstants');
var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotUtils = require('../../../js/utils/SpotUtils');
var ProxyConstants = require('../constants/ProxyConstants');
var RestStore = require('../../../js/stores/RestStore');

var ALL_FILTER = 'all';
var URI_FILTER = 'fulluri';

var CHANGE_FILTER_EVENT = 'change_filter';
var HIGHLIGHT_THREAT_EVENT = 'hightlight_thread';
var UNHIGHLIGHT_THREAT_EVENT = 'unhightlight_thread';
var SELECT_THREAT_EVENT = 'select_treath';

var filterName = '';
var highlightedThread = null;
var selectedThread = null;

var SuspiciousStore = assign(new RestStore(ProxyConstants.API_SUSPICIOUS), {
    _parser: d3.tsv,
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    headers: {
        p_date: 'Time',
        clientip: 'Client IP',
        host: 'Host',
        uri_rep: ' ',
        webcat: 'Web Category',
        respcode_name: 'Response Code'
    },
    ITERATOR: ['p_date', 'clientip', 'host', 'uri_rep', 'webcat', 'respcode_name'],
    getData: function () {
        var state, filter;

        filter = this.getFilter();

        if (!filter || !this._data) {
            state = this._data || {data: []};
        }
        else {
            state = assign(
                {},
                this._data
            );

            if (state.data) {
                state.data = state.data.filter((item) => {
                    return filterName === URI_FILTER ?
                                                // Only look on URI field
                                                item[URI_FILTER].indexOf(filter) >= 0 :
                                                // Look on clientip and URI fields
                                                item.clientip == filter || item.fulluri.indexOf(filter) >= 0;
                });
            }
        }

        if (state.data) {
            state.data = state.data.filter(function (item) {
                return item.uri_sev == "0";
            });

            if (state.data.length > SpotConstants.MAX_SUSPICIOUS_ROWS) state.data = state.data.slice(0, SpotConstants.MAX_SUSPICIOUS_ROWS);
        }

        return state;
    },
    setData: function (data) {
        this._data = data;

        this.emitChangeData();
    },
    setDate: function (date) {
        this.setEndpoint(ProxyConstants.API_SUSPICIOUS.replace('${date}', date.replace(/-/g, '')));
    },
    setFilter: function (filter) {
        if (filter === '') {
            filterName = '';
            this.removeRestFilter(ALL_FILTER);
            this.removeRestFilter(URI_FILTER);
        }
        else if (SpotUtils.IP_V4_REGEX.test(filter)) {
            this.removeRestFilter(URI_FILTER);
            this.setRestFilter(ALL_FILTER, filter);
            filterName = ALL_FILTER;
        }
        else {
            this.removeRestFilter(ALL_FILTER);
            this.setRestFilter(URI_FILTER, filter);
            filterName = URI_FILTER;
        }

        this.emitChangeFilter();
    },
    getFilter: function () {
        return this.getRestFilter(filterName);
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

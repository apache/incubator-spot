var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var OniDispatcher = require('../dispatchers/OniDispatcher');
var OniConstants = require('../constants/OniConstants');

var CHANGE_DATE_EVENT = 'change_date';
var PANEL_EXPAND_EVENT = 'panel_expand';
var PANEL_RESTORE_EVENT = 'panel_restore';
var PANEL_TOGGLE_MODE_EVENT = 'panel_toggle_mode';

var date = '';

var OniStore = assign({}, EventEmitter.prototype, {
  setDate: function (newDate)
  {
    date = newDate;

    this.emitChangeDate();
  },
  getDate: function ()
  {
    return date;
  },
  emitChangeDate: function () {
    this.emit(CHANGE_DATE_EVENT);
  },
  addChangeDateListener: function (callback) {
    this.on(CHANGE_DATE_EVENT, callback);
  },
  removeChangeDateListener: function (callback) {
    this.removeListener(CHANGE_DATE_EVENT, callback);
  },
  addPanelExpandListener: function (callback)
  {
    this.on(PANEL_EXPAND_EVENT, callback);
  },
  removePanelExpandListener: function (callback)
  {
    this.removeListener(PANEL_EXPAND_EVENT, callback);
  },
  emitPanelExpand: function (panel)
  {
    this.emit(PANEL_EXPAND_EVENT, panel);
  },
  addPanelRestoreListener: function (callback)
  {
    this.on(PANEL_RESTORE_EVENT, callback);
  },
  removePanelRestoreListener: function (callback)
  {
    this.removeListener(PANEL_EXPAND_EVENT, callback);
  },
  emitPanelRestore: function (panel)
  {
    this.emit(PANEL_RESTORE_EVENT, panel);
  },
  addPanelToggleModeListener: function (callback)
  {
    this.on(PANEL_TOGGLE_MODE_EVENT, callback);
  },
  removePanelToggleModeListener: function (callback)
  {
    this.removeListener(PANEL_TOGGLE_MODE_EVENT, callback);
  },
  emitPanelToggleMode: function (panel, mode)
  {
    this.emit(PANEL_TOGGLE_MODE_EVENT, panel, mode);
  },
});

OniDispatcher.register(function (action) {
  switch (action.actionType) {
    case OniConstants.UPDATE_DATE:
      OniStore.setDate(action.date);
      break;
    case OniConstants.EXPAND_PANEL:
      OniStore.emitPanelExpand(action.panel);
      break;
    case OniConstants.RESTORE_PANEL:
      OniStore.emitPanelRestore(action.panel);
      break;
    case OniConstants.TOGGLE_MODE_PANEL:
      OniStore.emitPanelToggleMode(action.panel, action.mode);
      break;
  }
});

module.exports = OniStore;

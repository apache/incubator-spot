//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var SpotDispatcher = require('../dispatchers/SpotDispatcher');
var SpotConstants = require('../constants/SpotConstants');

var CHANGE_DATE_EVENT = 'change_date';
var PANEL_EXPAND_EVENT = 'panel_expand';
var PANEL_RESTORE_EVENT = 'panel_restore';
var PANEL_TOGGLE_MODE_EVENT = 'panel_toggle_mode';

var date = '';

var SpotStore = assign({}, EventEmitter.prototype, {
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
    this.removeListener(PANEL_RESTORE_EVENT, callback);
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

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      SpotStore.setDate(action.date);
      break;
    case SpotConstants.EXPAND_PANEL:
      SpotStore.emitPanelExpand(action.panel);
      break;
    case SpotConstants.RESTORE_PANEL:
      SpotStore.emitPanelRestore(action.panel);
      break;
    case SpotConstants.TOGGLE_MODE_PANEL:
      SpotStore.emitPanelToggleMode(action.panel, action.mode);
      break;
  }
});

module.exports = SpotStore;

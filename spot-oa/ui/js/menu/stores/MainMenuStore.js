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
const EventEmitter = require('events').EventEmitter;

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');
const Menu = require('../menu');

class MainMenuStore {
    constructor() {
        this.menu = Menu.menu;
        this.eventEmitter = new EventEmitter();
    }

    getMenu() {
      return this.menu;
    }

    addChangeMenuListener(callback) {
        this.eventEmitter.on('change', callback);
    }

    removeChangeMenuListener(callback) {
        this.eventEmitter.removeListener('change', callback);
    }

    notifyListeners() {
        this.eventEmitter.emit('change');
    }

}

const mm = new MainMenuStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.RELOAD_MENU:
            mm.notifyListeners();
            break;
    }
});

module.exports = mm;

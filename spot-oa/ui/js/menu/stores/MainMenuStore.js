// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.
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

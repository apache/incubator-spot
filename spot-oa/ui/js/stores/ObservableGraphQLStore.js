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

const GraphQLStore = require('./GraphQLStore');

const DATA_CHANGE_EVENT = 'data-change';

class ObervableGraphQLStore extends GraphQLStore {
    constructor() {
        super();
        this.eventEmitter = new EventEmitter();
    }

    addChangeDataListener(callback) {
        this.addListener(DATA_CHANGE_EVENT, callback);
    }

    removeChangeDataListener(callback) {
        this.removeListener(DATA_CHANGE_EVENT, callback);
    }

    addListener(eventName, callback) {
        this.eventEmitter.on(eventName, callback);
    }

    removeListener(eventName, callback) {
        this.eventEmitter.removeListener(eventName, callback);
    }

    notifyListeners(eventName) {
        this.eventEmitter.emit(eventName);
    }

    setData(data) {
        super.setData(data);
        this.notifyListeners(DATA_CHANGE_EVENT);
    }
}

module.exports = ObervableGraphQLStore;

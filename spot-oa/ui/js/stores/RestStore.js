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

var $ = require('jquery');
var assign = require('object-assign');
var d3 = require('d3');
var EventEmitter = require('events').EventEmitter;

var CHANGE_DATA_EVENT = 'change_data';

var RestStore = function (endpoint, data) {
    this.setEndpoint(endpoint);
    this._filters = {};
    this._data = assign({
            loading: false
        }, data
    );

    this._parser = d3.csv;
};

assign(RestStore.prototype, EventEmitter.prototype, {
    defaultErrorMessage: 'Oops, something went wrong!!',
    errorMessages: {},
    headers: {},
    setRestFilter: function (name, value) {
        this._filters[name] = value;
    },
    getRestFilter: function (name) {
        return this._filters[name];
    },
    removeRestFilter: function (name) {
        delete this._filters[name];
    },
    setEndpoint: function (endpoint) {
        this.endpoint = endpoint;
    },
    resetData: function () {
        this._data = {loading: false};

        this.emitChangeData();
    },
    setData: function (data) {
        this._data = data;

        this.emitChangeData();
    },
    getData: function () {
        return this._data;
    },
    emitChangeData: function () {
        this.emit(CHANGE_DATA_EVENT);
    },
    addChangeDataListener: function (callback) {
        this.on(CHANGE_DATA_EVENT, callback);
    },
    removeChangeDataListener: function (callback) {
        this.removeListener(CHANGE_DATA_EVENT, callback);
    },
    reload: function () {
        var url, name;

        this.setData({loading: true});

        url = this.endpoint;

        for (name in this._filters) {
            url = url.replace('${' + name + '}', this._filters[name]);
        }
        url = url.replace(/:/g, '_');

        $.ajax(url, {
            method: 'GET',
            context: this,
            contentType: 'application/csv',
            success: function (response) {
                var csv, headers, tmp;

                csv = this._parser.parseRows(response);

                response = {};

                if (!this._skipHeaders) {
                    headers = csv.shift();

                    csv = csv.map(function (row) {
                        var obj = {};

                        headers.forEach(function (name, idx) {
                            obj[name] = row[idx];
                        });

                        return obj;
                    });

                    tmp = headers;
                    headers = {};
                    tmp.forEach(function (name) {
                        headers[name] = this.headers[name] || name;
                    }.bind(this));
                }

                this.setData({
                    loading: false,
                    headers: headers,
                    data: csv
                });
            },
            error: function (response) {
                this.setData({
                    loading: false,
                    error: this.errorMessages[response.status] || this.defaultErrorMessage
                });
            }
        });
    }
});

module.exports = RestStore;

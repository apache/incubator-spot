var $ = require('jquery');
var assign = require('object-assign');
var d3 = require('d3');
var EventEmitter = require('events').EventEmitter;

var CHANGE_DATA_EVENT = 'change_data';

var RestStore = function (endpoint, data) {
    this.setEndpoint(endpoint);
    this._filters = {};
    this._data = assign({
            loading: false,
            headers: [],
            data: []
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
        this._data = {loading: false, headers: [], data: [], error: undefined};

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

        this.setData({loading: true, headers: [], data: [], error: undefined});

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
                    data: csv,
                    error: undefined
                });
            },
            error: function (response) {
                this.setData({
                    loading: false,
                    headers: [],
                    data: [],
                    error: this.errorMessages[response.status] || this.defaultErrorMessage
                });
            }
        });
    }
});

module.exports = RestStore;

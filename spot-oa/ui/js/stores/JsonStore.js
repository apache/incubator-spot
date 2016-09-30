var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var CHANGE_DATA_EVENT = 'change_data';

var JsonStore = function (endpoint) {
    this.setEndpoint(endpoint);
    this._filters = {};
    this._data = {loading: false};
};

assign(JsonStore.prototype, EventEmitter.prototype, {
    defaultErrorMessage: 'Oops, something went wrong!!',
    errorMessages: {},
    setRestFilter: function (name, value)
    {
    this._filters[name] = value;
    },
    getRestFilter: function (name)
    {
        return this._filters[name];
    },
    removeRestFilter: function (name)
    {
        delete this._filters[name];
    },
        setEndpoint: function (endpoint) {
            this.endpoint = endpoint;
    },
    resetData: function ()
    {
        this._data = {loading: false};

        this.emitChangeData();
    },
    setData: function (data)
    {
        this._data = data;
        this.emitChangeData();
    },
    getData: function ()
    {
        return this._data;
    },
    emitChangeData: function ()
    {
        this.emit(CHANGE_DATA_EVENT);
    },
    addChangeDataListener: function (callback) {
        this.on(CHANGE_DATA_EVENT, callback);
    },
    removeChangeDataListener: function (callback) {
        this.removeListener(CHANGE_DATA_EVENT, callback);
    },
    reload: function ()
    {
        var url, name;
        this.setData({loading: true});

        url = this.endpoint;

        for (name in this._filters)
        {
            url = url.replace('${'+name+'}', this._filters[name]);
        }
        url = url.replace(/:/g, '_');

        $.ajax(url, {
            method: 'GET',
            context: this,
            contentType: 'application/json',
            success: function (response) {

                this.setData({
                  loading: false,
                  data: response
                });
            },
            error: function (response)
            {
                this.setData({error: this.errorMessages[response.status] || this.defaultErrorMessage});
            }          
        });
    }
});

module.exports = JsonStore;

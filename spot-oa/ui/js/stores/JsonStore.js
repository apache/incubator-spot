const $ = require('jquery');

const RestStore = require('./RestStore');

const JsonStore = function () {
    RestStore.apply(this, arguments);
};

JsonStore.prototype = new RestStore();

JsonStore.prototype.reload = function ()
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
            this.setData({
                loading:false,
                error: this.errorMessages[response.status] || this.defaultErrorMessage
            });
        }
    });
};

module.exports = JsonStore;

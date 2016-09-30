var assign = require('object-assign');
var d3 = require('d3');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var ProxyConstants = require('../constants/ProxyConstants');
var RestStore = require('../../../js/stores/RestStore');

var CLIENT_IP_FILTER = 'clientip';
var HASH_FILTER = 'hash';

var DetailsStore = assign(new RestStore(ProxyConstants.API_DETAILS), {
    _parser: d3.dsv('\t', 'text/plain'),
    errorMessages: {
        404: 'No details available'
    },
    headers: {
        p_date: 'Time',
        clientip: 'Client IP',
        host: 'Host',
        webcat: 'Web Category',
        respcode_name: 'Response Code',
        reqmethod: 'Request Method',
        useragent: 'User Agent',
        resconttype: 'MIME Type',
        referer: 'Referer',
        uriport: 'URI Port',
        serverip: 'Proxy IP',
        scbytes: 'Server Bytes',
        csbytes: 'Client Bytes',
        fulluri: 'Full URI'
    },
    setDate: function (date) {
        this.setEndpoint(ProxyConstants.API_DETAILS.replace('${date}', date.replace(/-/g, '')));
    },
    setClientIp: function (clientIp) {
        this.setRestFilter(CLIENT_IP_FILTER, clientIp);
    },
    setHash: function (hash) {
        this.setRestFilter(HASH_FILTER, hash);
    }
});

OniDispatcher.register(function (action) {
    switch (action.actionType) {
        case OniConstants.UPDATE_DATE:
            DetailsStore.setDate(action.date);
            break;
        case OniConstants.SELECT_THREAT:
            DetailsStore.setClientIp(action.threat[CLIENT_IP_FILTER]);
            DetailsStore.setHash(action.threat[HASH_FILTER].replace(/\//g, '-'));
            break;
        case OniConstants.RELOAD_SUSPICIOUS:
            DetailsStore.resetData();
            break;
        case OniConstants.RELOAD_DETAILS:
            DetailsStore.reload();
            break;
    }
});

module.exports = DetailsStore;

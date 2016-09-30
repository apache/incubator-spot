var assign = require('object-assign');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var JsonStore = require('../../../js/stores/JsonStore');

var ProxyConstants = require('../constants/ProxyConstants');

var IncidentProgressionStore = assign(new JsonStore(ProxyConstants.API_INCIDENT_PROGRESSION), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date) {
        this.setEndpoint(ProxyConstants.API_INCIDENT_PROGRESSION.replace('${date}', date.replace(/-/g, '')));
    },
    setHash: function (hash) {
        this.setRestFilter('hash', hash);
    }
});

OniDispatcher.register(function (action) {
    switch (action.actionType) {
        case OniConstants.UPDATE_DATE:
            IncidentProgressionStore.setDate(action.date);

            break;
        case OniConstants.RELOAD_COMMENTS:
            IncidentProgressionStore.removeRestFilter('hash');
            IncidentProgressionStore.resetData();
            break;
        case OniConstants.SELECT_COMMENT:
            IncidentProgressionStore.setHash(action.comment.hash);
            IncidentProgressionStore.reload();

            break;
    }
});

module.exports = IncidentProgressionStore;


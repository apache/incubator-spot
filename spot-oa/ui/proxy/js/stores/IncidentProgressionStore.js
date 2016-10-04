var assign = require('object-assign');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotConstants = require('../../../js/constants/SpotConstants');
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

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            IncidentProgressionStore.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            IncidentProgressionStore.removeRestFilter('hash');
            IncidentProgressionStore.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            IncidentProgressionStore.setHash(action.comment.hash);
            IncidentProgressionStore.reload();

            break;
    }
});

module.exports = IncidentProgressionStore;

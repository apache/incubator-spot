const assign = require('object-assign');

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');
const FlowConstants = require('../constants/NetflowConstants');
const JsonStore = require('../../../js/stores/JsonStore');

const IP_FILTER_NAME = 'ip';

const IncidentProgressionStore = assign(new JsonStore(FlowConstants.API_INCIDENT_PROGRESSION), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_INCIDENT_PROGRESSION.replace('${date}', date.replace(/-/g, '')));
    },
    setIp: function (value)
    {
        this.setRestFilter(IP_FILTER_NAME, value);
    },
    getIp: function ()
    {
        return this.getRestFilter(IP_FILTER_NAME);
    }
});

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            IncidentProgressionStore.setDate(action.date);
            break;
        case SpotConstants.RELOAD_COMMENTS:
            IncidentProgressionStore.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            IncidentProgressionStore.setIp(action.comment.ip);
            IncidentProgressionStore.reload();

            break;
    }
});

module.exports = IncidentProgressionStore;

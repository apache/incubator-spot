const assign = require('object-assign');

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const FlowConstants = require('../constants/NetflowConstants');
const SpotConstants = require('../../../js/constants/SpotConstants');
const JsonStore = require('../../../js/stores/JsonStore');

const IP_FILTER_NAME = 'ip';

const ImpactAnalysisStore = assign(new JsonStore(FlowConstants.API_IMPACT_ANALYSIS), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_IMPACT_ANALYSIS.replace('${date}', date.replace(/-/g, '')));
    },
    setIp(value)
    {
        this.setRestFilter(IP_FILTER_NAME, value);
    },
    getIp()
    {
        this.getRestFilter(IP_FILTER_NAME);
    }
});

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            ImpactAnalysisStore.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            ImpactAnalysisStore.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            ImpactAnalysisStore.setIp(action.comment.ip);
            ImpactAnalysisStore.reload();

            break;
    }
});

module.exports = ImpactAnalysisStore;

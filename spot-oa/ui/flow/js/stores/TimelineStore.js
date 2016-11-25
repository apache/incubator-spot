const assign = require('object-assign');
const d3 = require('d3');

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const FlowConstants = require('../constants/NetflowConstants');
const SpotConstants = require('../../../js/constants/SpotConstants');
const RestStore = require('../../../js/stores/RestStore');

const IP_FILTER_NAME = 'ip';

const TimelineStore = assign(new RestStore(FlowConstants.API_TIMELINE), {
    _parser: d3.tsv,
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_TIMELINE.replace('${date}', date.replace(/-/g, '')));
        this._date = date;
    },
    getDate() {
        return this._date;
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
            TimelineStore.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            TimelineStore.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            TimelineStore.setIp(action.comment.ip);

            TimelineStore.reload();

            break;
    }
});

module.exports = TimelineStore;

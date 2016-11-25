var assign = require('object-assign');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var FlowConstants = require('../constants/NetflowConstants');
var SpotConstants = require('../../../js/constants/SpotConstants');
var JsonStore = require('../../../js/stores/JsonStore');

const IP_FILTER_NAME = 'ip';

var GlobeViewStore = assign(new JsonStore(FlowConstants.API_GLOBE_VIEW), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_GLOBE_VIEW.replace('${date}', date.replace(/-/g, '')));
    },
    setIp: function (value)
    {
        this.setRestFilter(IP_FILTER_NAME, value);
    },
    getIp: function ()
    {
        return this.getRestFilter(IP_FILTER_NAME);
    },
    setData: function (data)
    {
        this._data = data;

        this.emitChangeData();
    }
});

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            GlobeViewStore.setDate(action.date);

            break;
        case SpotConstants.RELOAD_COMMENTS:
            GlobeViewStore.resetData();
            break;
        case SpotConstants.SELECT_COMMENT:
            GlobeViewStore.setIp(action.comment.ip);
            GlobeViewStore.reload();

            break;
    }
});

module.exports = GlobeViewStore;

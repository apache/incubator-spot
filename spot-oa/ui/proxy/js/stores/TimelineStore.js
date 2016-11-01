const assign = require('object-assign');
const d3 = require('d3');

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const FlowConstants = require('../constants/ProxyConstants');
const SpotConstants = require('../../../js/constants/SpotConstants');
const RestStore = require('../../../js/stores/RestStore');

const FILTER_NAME = 'hash';

const TimelineStore = assign(new RestStore(FlowConstants.API_TIMELINE), {
    _parser: d3.tsv,
    _date:'',
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
    setFilter: function (value)
    {
        this.setRestFilter(FILTER_NAME, value);
    },
    getFilter: function ()
    {
        return this.getRestFilter(FILTER_NAME);
    },
    clearFilter: function ()
    {
       this.removeRestFilter(FILTER_NAME);
    }
});


SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            TimelineStore.setDate(action.date);

            break;
        case SpotConstants.SELECT_COMMENT:
            TimelineStore.setFilter(action.comment.hash);

            TimelineStore.reload();

            break;
    }
});

module.exports = TimelineStore;

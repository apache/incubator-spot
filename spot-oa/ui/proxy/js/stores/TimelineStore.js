var assign = require('object-assign');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var FlowConstants = require('../constants/ProxyConstants');
var SpotConstants = require('../../../js/constants/SpotConstants');
var RestStore = require('../../../js/stores/RestStore');

var fields = ['title', 'summary'];
var filterName;


var TimelineStore = assign(new RestStore(FlowConstants.API_TIMELINE), {
    _parser: d3.tsv,
    _sdate:'',
    _slegend:false,
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_TIMELINE.replace('${date}', date.replace(/-/g, '')));
        this._sdate = date;
    },
    setFilter: function (name, value)
    {
        filterName = name;
        this.setRestFilter('hash', value);
    },
    getFilterName: function ()
    {
        return filterName;
    },
    getFilterValue: function ()
    {
        return this.getRestFilter('hash');
    },
    clearFilter: function ()
    {
       this.removeRestFilter('hash');
    }
});


SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            TimelineStore.setDate(action.date);

            break;
        case SpotConstants.SELECT_COMMENT:
            var comment, filterParts, key;

            TimelineStore.clearFilter();

            comment = action.comment;

            filterParts = [];

            for (key in comment)
            {
                // Skip comment fields
                if (fields.indexOf(key)>=0) continue;

                if (comment[key])
                {
                    TimelineStore.setFilter(key, comment[key]);

                    break;
                }
            }

            TimelineStore.reload();

            break;
    }
});

module.exports = TimelineStore;

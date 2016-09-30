var assign = require('object-assign');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var FlowConstants = require('../constants/NetflowConstants');
var OniConstants = require('../../../js/constants/OniConstants');
var RestStore = require('../../../js/stores/JsonStore');

var fields = ['title', 'summary'];
var filterName;

var ImpactAnalysisStore = assign(new RestStore(FlowConstants.API_IMPACT_ANALYSIS), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_IMPACT_ANALYSIS.replace('${date}', date.replace(/-/g, '')));
    },
    setFilter: function (name, value)
    {
        filterName = name;
        this.setRestFilter('id', value);
    },
    getFilterName: function ()
    {
        return filterName;
    },
    getFilterValue: function ()
    {
        return this.getRestFilter('id');
    },
    clearFilter: function ()
    {
       this.removeRestFilter('id');
    }
});

OniDispatcher.register(function (action) {
    switch (action.actionType) {
        case OniConstants.UPDATE_DATE:
            ImpactAnalysisStore.setDate(action.date);

            break;
        case OniConstants.SELECT_COMMENT:
            var comment, filterParts, key;

            ImpactAnalysisStore.clearFilter();

            comment = action.comment;

            filterParts = [];

            for (key in comment)
            {
                // Skip comment fields
                if (fields.indexOf(key)>=0) continue;

                if (comment[key])
                {
                    ImpactAnalysisStore.setFilter(key, comment[key]);

                    break;
                }
            }

            ImpactAnalysisStore.reload();

            break;
    }
});

module.exports = ImpactAnalysisStore;


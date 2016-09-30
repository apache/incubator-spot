var assign = require('object-assign');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var FlowConstants = require('../constants/NetflowConstants');
var RestStore = require('../../../js/stores/JsonStore');

var fields = ['title', 'summary'];
var filterName;

var IncidentProgressionStore = assign(new RestStore(FlowConstants.API_INCIDENT_PROGRESSION), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_INCIDENT_PROGRESSION.replace('${date}', date.replace(/-/g, '')));
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
            IncidentProgressionStore.setDate(action.date);
            break;
        case OniConstants.SELECT_COMMENT:
            var comment, filterParts, key;

            IncidentProgressionStore.clearFilter();

            comment = action.comment;

            filterParts = [];

            for (key in comment)
            {
                // Skip comment fields
                if (fields.indexOf(key)>=0) continue;

                if (comment[key])
                {
                    IncidentProgressionStore.setFilter(key, comment[key]);

                    break;
                }
            }

            IncidentProgressionStore.reload();

            break;
    }
});

module.exports = IncidentProgressionStore;


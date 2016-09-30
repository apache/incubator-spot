var assign = require('object-assign');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var FlowConstants = require('../constants/NetflowConstants');
var OniConstants = require('../../../js/constants/OniConstants');
var JsonStore = require('../../../js/stores/JsonStore');

var fields = ['title', 'summary'];
var filterName;

var GlobeViewStore = assign(new JsonStore(FlowConstants.API_GLOBE_VIEW), {
    errorMessages: {
        404: 'Please choose a different date, no data has been found'
    },
    setDate: function (date)
    {
        this.setEndpoint(FlowConstants.API_GLOBE_VIEW.replace('${date}', date.replace(/-/g, '')));
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
    setData: function (data)
    {
        this._data = data;

        this.emitChangeData();
    },
    clearFilter: function ()
    {
       this.removeRestFilter('id');
    }
});

OniDispatcher.register(function (action) {
    switch (action.actionType) {
        case OniConstants.UPDATE_DATE:
            GlobeViewStore.setDate(action.date);

            break;
        case OniConstants.SELECT_COMMENT:
            var comment, filterParts, key;

            GlobeViewStore.clearFilter();

            comment = action.comment;

            filterParts = [];

            for (key in comment)
            {
                // Skip comment fields
                if (fields.indexOf(key)>=0) continue;

                if (comment[key])
                {
                    GlobeViewStore.setFilter(key, comment[key]);

                    break;
                }
            }

            GlobeViewStore.reload();

            break;
    }
});

module.exports = GlobeViewStore;


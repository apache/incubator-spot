var assign = require('object-assign');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotConstants = require('../../../js/constants/SpotConstants');
var DnsConstants = require('../constants/DnsConstants');
var RestStore = require('../../../js/stores/RestStore');

var fields = ['title', 'summary'];
var filterName;

var IncidentProgressionStore = assign(new RestStore(DnsConstants.API_INCIDENT_PROGRESSION), {
  errorMessages: {
    404: 'Please choose a different date, no data has been found'
  },
  setDate: function (date)
  {
    this.setEndpoint(DnsConstants.API_INCIDENT_PROGRESSION.replace('${date}', date.replace(/-/g, '')));
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

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      IncidentProgressionStore.setDate(action.date);

      break;
    case SpotConstants.RELOAD_COMMENTS:
      IncidentProgressionStore.clearFilter();
      IncidentProgressionStore.resetData();
      break;
    case SpotConstants.SELECT_COMMENT:
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

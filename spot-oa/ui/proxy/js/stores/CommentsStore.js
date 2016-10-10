var assign = require('object-assign');
var d3 = require('d3');

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var SpotConstants = require('../../../js/constants/SpotConstants');
var ProxyConstants = require('../constants/ProxyConstants');
var RestStore = require('../../../js/stores/RestStore');

var CommentsStore = assign(new RestStore(ProxyConstants.API_COMMENTS), {
  _parser: d3.dsv('|', 'text/plain'),
  errorMessages: {
    404: 'Please choose a different date, no comments have been found'
  },
  setDate: function (date)
  {
    this.setEndpoint(ProxyConstants.API_COMMENTS.replace('${date}', date.replace(/-/g, '')));
  }
});

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      CommentsStore.setDate(action.date);
      break;
    case SpotConstants.RELOAD_COMMENTS:
      CommentsStore.reload();
      break;
  }
});

module.exports = CommentsStore;

var SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
var NetflowConstants = require('../constants/NetflowConstants');

var InSumActions = {
  reloadSummary: function () {
    SpotDispatcher.dispatch({
      actionType: NetflowConstants.RELOAD_INGEST_SUMMARY
    });
  }
};

module.exports = InSumActions;

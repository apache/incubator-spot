var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var NetflowConstants = require('../constants/NetflowConstants');

var InSumActions = {
  reloadSummary: function () {
    OniDispatcher.dispatch({
      actionType: NetflowConstants.RELOAD_INGEST_SUMMARY
    });
  }
};

module.exports = InSumActions;

const SpotDispatcher = require('../dispatchers/SpotDispatcher');
const SpotConstants = require('../constants/SpotConstants');

const InSumActions = {
  reloadSummary: function () {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.RELOAD_INGEST_SUMMARY
    });
  }
};

module.exports = InSumActions;

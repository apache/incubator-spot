var SpotDispatcher = require('../dispatchers/SpotDispatcher');
var SpotActions = require('./SpotActions');
var SpotConstants = require('../constants/SpotConstants');
var SpotUtils = require('../utils/SpotUtils');

var EdInActions = {
  setFilter: function (filter)
  {
    SpotUtils.setUrlParam('filter', filter);

    SpotDispatcher.dispatch({
      actionType: SpotConstants.UPDATE_FILTER,
      filter: filter
    });
  },
  reloadSuspicious: function () {
    SpotActions.toggleMode(SpotConstants.DETAILS_PANEL, SpotConstants.DETAILS_MODE);
    SpotDispatcher.dispatch({
      actionType: SpotConstants.RELOAD_SUSPICIOUS
    });
  },
  reloadDetails: function () {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.RELOAD_DETAILS
    });
  },
  reloadVisualDetails: function () {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.RELOAD_DETAILS_VISUAL
    });
  },
  highlightThreat: function (id)
  {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.HIGHLIGHT_THREAT,
      threat: id
    });
  },
  unhighlightThreat: function ()
  {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.UNHIGHLIGHT_THREAT
    });
  },
  selectThreat: function (threat)
  {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.SELECT_THREAT,
      threat: threat
    });
  },
  selectIp: function (ip)
  {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.SELECT_IP,
      ip: ip
    });
  },
};

module.exports = EdInActions;

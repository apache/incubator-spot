var OniDispatcher = require('../dispatchers/OniDispatcher');
var OniActions = require('./OniActions');
var OniConstants = require('../constants/OniConstants');
var OniUtils = require('../utils/OniUtils');

var EdInActions = {
  setFilter: function (filter)
  {
    OniUtils.setUrlParam('filter', filter);

    OniDispatcher.dispatch({
      actionType: OniConstants.UPDATE_FILTER,
      filter: filter
    });
  },
  reloadSuspicious: function () {
    OniActions.toggleMode(OniConstants.DETAILS_PANEL, OniConstants.DETAILS_MODE);
    OniDispatcher.dispatch({
      actionType: OniConstants.RELOAD_SUSPICIOUS
    });
  },
  reloadDetails: function () {
    OniDispatcher.dispatch({
      actionType: OniConstants.RELOAD_DETAILS
    });
  },
  reloadVisualDetails: function () {
    OniDispatcher.dispatch({
      actionType: OniConstants.RELOAD_DETAILS_VISUAL
    });
  },
  highlightThreat: function (id)
  {
    OniDispatcher.dispatch({
      actionType: OniConstants.HIGHLIGHT_THREAT,
      threat: id
    });
  },
  unhighlightThreat: function ()
  {
    OniDispatcher.dispatch({
      actionType: OniConstants.UNHIGHLIGHT_THREAT
    });
  },
  selectThreat: function (threat)
  {
    OniDispatcher.dispatch({
      actionType: OniConstants.SELECT_THREAT,
      threat: threat
    });
  },
  selectIp: function (ip)
  {
    OniDispatcher.dispatch({
      actionType: OniConstants.SELECT_IP,
      ip: ip
    });
  },
};

module.exports = EdInActions;

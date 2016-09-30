var Dispatcher = require('flux').Dispatcher;

// Create dispatcher instance$
var OniDispatcher = new Dispatcher();

// Convenience method to handle dispatch requests$
OniDispatcher.handleServerAction = function(action) {
  this.dispatch({
    source: 'SERVER_ACTION',
    action: action
  });
}

OniDispatcher.handleAction = function(action) {
  this.dispatch({
    source: 'VIEW_ACTION',
    action: action
  });
}

module.exports = OniDispatcher;

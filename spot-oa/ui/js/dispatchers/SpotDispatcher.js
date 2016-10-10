var Dispatcher = require('flux').Dispatcher;

// Create dispatcher instance$
var SpotDispatcher = new Dispatcher();

// Convenience method to handle dispatch requests$
SpotDispatcher.handleServerAction = function(action) {
  this.dispatch({
    source: 'SERVER_ACTION',
    action: action
  });
}

SpotDispatcher.handleAction = function(action) {
  this.dispatch({
    source: 'VIEW_ACTION',
    action: action
  });
}

module.exports = SpotDispatcher;

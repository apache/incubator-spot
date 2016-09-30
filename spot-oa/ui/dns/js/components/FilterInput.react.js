var React = require('react');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var EdInActions = require('../../../js/actions/EdInActions');
var SuspiciousStore = require('../stores/SuspiciousStore');
var OniUtils = require('../../../js/utils/OniUtils');

var FilterInput = React.createClass({
  getInitialState: function ()
  {
    return {filter: ''};
  },
  componentDidMount: function ()
  {
    SuspiciousStore.addChangeFilterListener(this._onFilterChange);
  },
  componentWillUnmount: function ()
  {
    SuspiciousStore.removeChangeFilterListener(this._onFilterChange);
  },
  render: function ()
  {
    return (
      <input id={this.props.id} type="text" className="form-control" placeholder="0.0.0.0" autoFocus={true} onChange={this._onChange} value={this.state.filter} onKeyUp={this._onKeyUp} />
    );
  },
  _onKeyUp: function (e)
  {
    if (e.which==13) {
      EdInActions.reloadSuspicious();
    }
  },
  _onChange: function (e)
  {
    EdInActions.setFilter(e.target.value);
    this.setState({filter: e.target.value});
  },
  _onFilterChange: function ()
  {
    this.setState({filter: SuspiciousStore.getFilter()});
  }
});

module.exports = FilterInput;

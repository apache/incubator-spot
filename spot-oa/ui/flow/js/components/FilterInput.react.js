// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var EdInActions = require('../../../js/actions/EdInActions');
var SuspiciousStore = require('../stores/SuspiciousStore');
var SpotUtils = require('../../../js/utils/SpotUtils');

var FilterInput = React.createClass({
  getInitialState: function ()
  {
    return {filter: ''};
  },
  componentDidMount: function () {
    SuspiciousStore.addChangeFilterListener(this._onFilterChange);
  },
  componentWillUnmount: function () {
    SuspiciousStore.removeChangeFilterListener(this._onFilterChange);
  },
  render: function ()
  {
    var cssClasses;

    cssClasses = 'form-control';

    if (this.state.filter && !SpotUtils.IP_V4_REGEX.test(this.state.filter))
    {
      cssClasses += ' has-error';
    }

    return (
      <input id={this.props.id} type="text" className={cssClasses} placeholder="0.0.0.0" autoFocus={true} onChange={this._onChange} value={this.state.filter} onKeyUp={this._onKeyUp} />
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

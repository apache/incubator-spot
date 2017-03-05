// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var DetailsStore = require('../stores/DetailsStore');

var DetailsTablePanel = React.createClass({
  mixins: [GridPanelMixin],
  emptySetMessage: 'Please select one row from Suspicious Connects',
  getDefaultProps: function () {
      return {iterator: DetailsStore.ITERATOR};
  },
  componentDidMount: function ()
  {
    DetailsStore.addChangeDataListener(this._onChange);
  },
  componentWillUnmount: function ()
  {
    DetailsStore.removeChangeDataListener(this._onChange);
  },
  // Event handlers
  _onChange: function ()
  {
    this.replaceState(DetailsStore.getData());
  }
});

module.exports = DetailsTablePanel;

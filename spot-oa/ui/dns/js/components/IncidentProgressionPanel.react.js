// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const React = require('react');

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
const IncidentProgressionStore = require('../stores/IncidentProgressionStore');

const fieldMapper = {
    ip_dst: 'dns_qry_name',
    dns_qry_name: 'ip_dst'
};

const IncidentProgressionPanel = React.createClass({
  mixins: [ContentLoaderMixin, ChartMixin, DendrogramMixin],
  componentDidMount: function ()
  {
    IncidentProgressionStore.addChangeDataListener(this._onChange);
  },
  componentWillUnmount: function ()
  {
    IncidentProgressionStore.removeChangeDataListener(this._onChange);
  },
  _onChange: function ()
  {
    const storeData = IncidentProgressionStore.getData();
    const state = {loading: storeData.loading};

    if (!storeData.loading) {
        state.error = storeData.error;

        if (storeData.data) {
          state.data = {
              id: 'root',
              name: storeData.data.name
          };

          state.data.children = storeData.data.children.map(child => ({id:child.name, name:child.name}));
          state.leafNodes = state.data.children.length;
        }
    }

    this.replaceState(state);
  }
});

module.exports = IncidentProgressionPanel;

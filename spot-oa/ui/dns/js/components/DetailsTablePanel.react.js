// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var DetailsStore = require('../stores/DetailsStore');

var DetailsTablePanel = React.createClass({
  mixins: [GridPanelMixin],
  emptySetMessage: 'Please select one row from Suspicious DNS',
  getDefaultProps: function ()
  {
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
  _render_dns_qry_name_cell: function (query, item, idx)
  {
    return (
      <p key={'dns_qry_name_' + idx} className="spot-text-wrapper" data-toggle="tooltip">
        {query}
      </p>
    );
  },
  _render_dns_a_cell: function (answers)
  {
    answers = answers.map(function (answer, idx)
    {
        return (
            <div key={'answer_'+idx}>
                {answer}
            </div>
        );
    });

    return answers;
  },
  _onChange: function ()
  {
    this.replaceState(DetailsStore.getData());
  }
});

module.exports = DetailsTablePanel;

var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var DetailsStore = require('../stores/DetailsStore');
var OniUtils = require('../../../js/utils/OniUtils.js');

var DetailsTablePanel = React.createClass({
  mixins: [GridPanelMixin],
  emptySetMessage: 'Please select one row from Suspicious DNS',
  getInitialState: function ()
  {
    return DetailsStore.getData();
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
      <p key={'dns_qry_name_' + idx} className="oni-text-wrapper" data-toggle="tooltip">
        {query}
      </p>
    );
  },
  _render_dns_a_cell: function (answers)
  {
    answers = (answers || "").split('|');
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
  _render_dns_qry_class_cell: false,
  _render_dns_qry_rcode_cell: false,
  _render_dns_qry_type_cell: false,
  _onChange: function ()
  {
    this.setState(DetailsStore.getData());
  }
});

module.exports = DetailsTablePanel;

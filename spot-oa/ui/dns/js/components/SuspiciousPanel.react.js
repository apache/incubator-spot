var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var SuspiciousGridMixin = require('../../../js/components/SuspiciousGridMixin.react.js');
var OniUtils = require('../../../js/utils/OniUtils.js');
var SuspiciousStore = require('../stores/SuspiciousStore');

var SuspiciousPanel = React.createClass({
  mixins: [GridPanelMixin, SuspiciousGridMixin],
  store: SuspiciousStore,
  getInitialState: function () {
    return {iterator: SuspiciousStore.ITERATOR};
  },
  _render_dns_qry_name_cell: function (query, item, idx)
  {
    var reps, highestRep;

    reps = OniUtils.parseReputation(item.query_rep);
    highestRep = OniUtils.getHighestReputation(reps);

    return (
      <p key={'dns_qry_name_' + idx} className={'oni-text-wrapper text-' + OniUtils.CSS_RISK_CLASSES[highestRep]} data-toggle="tooltip">
        {query}
      </p>
    );
  },
  /**
    * Answers cell can have multiple pipe separated values. To allow
    * proper displaying of information lets place each answer inside
    * a block element let browsers decide how to display them.
    */
  _render_dns_a_cell: function (answers, item, idx)
  {
    var cellBody;

    answers = (answers || []).split('|');

    cellBody = answers.map(function (answer, i)
    {
      return (
        <div key={'answer_' + idx + '_' + i}>
          {answer}
        </div>
      );
    }.bind(this));

    return cellBody;
  },
  _render_ip_dst_cell: function (ip_dst, item, idx)
  {
    var ip_dst_info, iconClass;

    if (item.network_context)
    {
      ip_dst_info = (
        <span className={'fa fa-lg fa-info-circle text-primary'}
            data-container="body" data-toggle="popover" data-placement="right" data-content={item.network_context}>
        </span>
      );
    }

    return (
      <p key={'ip_dst_' + idx}>
        {ip_dst} {ip_dst_info}
      </p>
    );
  },
  _render_query_rep_cell: function (query_rep, item, idx) {
      var reps, highestRep, queryRep;

      reps = OniUtils.parseReputation(query_rep);
      highestRep = OniUtils.getHighestReputation(reps);

      queryRep = this._renderRepCell('dns_query_rep_' + idx,  reps);

      return (
          <p key={'dns_query_rep_' + idx} className={'query_rep text-' + OniUtils.CSS_RISK_CLASSES[highestRep]}>
              {queryRep}
          </p>
      );
  },
  // Hidden cells
  _render_dns_qry_class_cell: false,
  _render_dns_qry_type_cell: false,
  _render_dns_qry_rcode_cell: false,
  _render_dns_sev_cell: false,
  _render_domain_cell: false,
  _render_frame_len_cell: false,
  _render_hh_cell: false,
  _render_ip_sev_cell: false,
  _render_network_context_cell: false,
  _render_num_periods_cell: false,
  _render_query_length_cell: false,
  _render_resp_h_cell: false,
  _render_score_cell: false,
  _render_subdomain_cell: false,
  _render_subdomain_entropy_cell: false,
  _render_subdomain_length_cell: false,
  _render_top_domain_cell: false,
  _render_unix_tstamp_cell: false,
  _render_word_cell: false
});

module.exports = SuspiciousPanel;

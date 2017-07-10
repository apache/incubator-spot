//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

var React = require('react');

var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var SuspiciousGridMixin = require('../../../js/components/SuspiciousGridMixin.react.js');
var SpotUtils = require('../../../js/utils/SpotUtils.js');
var SuspiciousStore = require('../stores/SuspiciousStore');

var SuspiciousPanel = React.createClass({
  mixins: [GridPanelMixin, SuspiciousGridMixin],
  store: SuspiciousStore,
  getDefaultProps: function () {
    return {iterator: SuspiciousStore.ITERATOR};
  },
  _render_dns_qry_name_cell: function (query, item, idx)
  {
    var reps, highestRep;

    reps = SpotUtils.parseReputation(item.query_rep);
    highestRep = SpotUtils.getHighestReputation(reps);

    return (
      <p key={'dns_qry_name_' + idx} className={'spot-text-wrapper text-' + SpotUtils.CSS_RISK_CLASSES[highestRep]} data-toggle="tooltip">
        {query}
      </p>
    );
  },
  _render_ip_dst_cell: function (ip_dst, item, idx)
  {
    var ip_dst_info, iconClass;

    if (item.network_context)
    {
      ip_dst_info = (
        <span className={'fa fa-lg fa-info-circle text-info'}
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

      reps = SpotUtils.parseReputation(query_rep);
      highestRep = SpotUtils.getHighestReputation(reps);

      queryRep = this._renderRepCell('dns_query_rep_' + idx,  reps);

      return (
          <p key={'dns_query_rep_' + idx} className={'query_rep text-' + SpotUtils.CSS_RISK_CLASSES[highestRep]}>
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

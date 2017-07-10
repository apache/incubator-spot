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

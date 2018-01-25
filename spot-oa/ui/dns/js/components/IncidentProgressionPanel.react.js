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

const React = require('react');

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
const IncidentProgressionStore = require('../stores/IncidentProgressionStore');

const fieldMapper = {
    clientIp: 'dnsQuery',
    dnsQuery: 'clientIp'
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

        if (storeData.data && storeData.data.length) {
          let filterName = IncidentProgressionStore.getFilterName();

          state.data = {
            id: 'root',
            name: IncidentProgressionStore.getFilterValue(),
            children: []
          };

          state.leafNodes = 0;
          storeData.data.forEach((item) => {
            state.data.children.push({
              id: `node${++state.leafNodes}`,
              name: item[fieldMapper[filterName]]
            });
          });
        }
    }

    this.replaceState(state);
  }
});

module.exports = IncidentProgressionPanel;

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

var SpotConstants = require('../../../js/constants/SpotConstants');

var SpotStore = require('../../../js/stores/SpotStore');

var DetailsTablePanel = require('./DetailsTablePanel.react');
var DetailsDendrogramPanel = require('./DetailsDendrogramPanel.react');

var DetailsPanel = React.createClass({
  propTypes: {
    title: React.PropTypes.string.isRequired
  },
  getInitialState: function ()
  {
    return {};
  },
  componentDidMount: function ()
  {
    SpotStore.addPanelToggleModeListener(this._onToggleMode);
  },
  componentWillUnmount: function ()
  {
    SpotStore.removePanelToggleModeListener(this._onToggleMode);
  },
  render: function ()
  {
    if (this.state.mode === SpotConstants.VISUAL_DETAILS_MODE)
    {
      return (
        <DetailsDendrogramPanel className="dendrogram" />
      );
    }
    else
    {
      return (
        <div className="inner-container-box">
          <DetailsTablePanel />
        </div>
      );
    }
  },
  _onToggleMode: function (panel, mode)
  {
    if (panel!==this.props.title) return;

    this.setState({mode: mode});
  }
});

module.exports = DetailsPanel;

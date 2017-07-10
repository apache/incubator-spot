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

var SpotStore = require('../stores/SpotStore');

var PanelRow = React.createClass({
  propTypes: {
    maximized: React.PropTypes.bool
  },
  getDefaultProps: function ()
  {
    return {
      maximized: false
    }
  },
  getInitialState: function () {
    var state;

    state = {maximized: this.props.maximized, minimized: false, childrenTitles: []};

    React.Children.forEach(this.props.children, child => {
        state.childrenTitles.push(child.props.title);
    });

    return state;
  },
  componentDidMount: function ()
  {
      SpotStore.addPanelExpandListener(this._onChildExpanded);
      SpotStore.addPanelRestoreListener(this._onChildRestored);
  },
  render: function () {
    var cssCls = this.state.maximized ? 'spot-maximized' : this.state.minimized ? 'spot-minimized' : '';
    var sortable = {'order':1};

    return (
      <div id={this.props.title || ''} className={'spot-row row fit ' + cssCls} style={sortable}>
        {this.props.children}
      </div>
    );
  },
  componentWillUnmount: function ()
  {
    SpotStore.removePanelExpandListener(this._onChildExpanded);
    SpotStore.removePanelRestoreListener(this._onChildRestored);
  },
  _onChildExpanded: function (childTitle) {
    if (this.state.childrenTitles.indexOf(childTitle)>=0)
    {
        this.setState({maximized: true});
    }
    else
    {
        this.setState({minimized: true});
    }
  },
  _onChildRestored: function (childTitle) {
    if (this.state.childrenTitles.indexOf(childTitle)>=0)
    {
        this.setState({maximized: false});
    }
    else
    {
        this.setState({minimized: false});
    }
  }
});

module.exports = PanelRow;

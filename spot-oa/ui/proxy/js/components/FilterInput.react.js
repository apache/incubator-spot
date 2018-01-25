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

var EdInActions = require('../../../js/actions/EdInActions');
var SuspiciousStore = require('../stores/SuspiciousStore');

var FilterInput = React.createClass({
  getInitialState: function ()
  {
    return {filter: ''};
  },
  componentDidMount: function ()
  {
    SuspiciousStore.addChangeFilterListener(this._onFilterChange);
  },
  componentWillUnmount: function ()
  {
    SuspiciousStore.removeChangeFilterListener(this._onFilterChange);
  },
  render: function ()
  {
    return (
      <input id={this.props.id} type="text" className="form-control" placeholder="0.0.0.0" autoFocus={true} onChange={this._onChange} value={this.state.filter} onKeyUp={this._onKeyUp} />
    );
  },
  _onKeyUp: function (e)
  {
    if (e.which==13) {
      EdInActions.reloadSuspicious();
    }
  },
  _onChange: function (e)
  {
    EdInActions.setFilter(e.target.value);
    this.setState({filter: e.target.value});
  },
  _onFilterChange: function ()
  {
    this.setState({filter: SuspiciousStore.getFilter()});
  }
});

module.exports = FilterInput;

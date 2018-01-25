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

var SearchGlobalInput = React.createClass({
    getInitialState: function() {
      return {filter: ''}
    },
    render: function() {
      return(
        <div className={`col-md-${this.props.col} col-lg-${this.props.col} col-xs-12`}>
          <div className="inner-addon right-addon">
            <i className="glyphicon glyphicon-search"></i>
            <input className="form-control" type="text" placeholder="Quick scoring..." maxLength={this.props.maxlength || 15} id="globalTxt" autoFocus={false} onChange={this._onChange} value={this.state.filter} onKeyUp={this._onKeyUp} />
          </div>
        </div>
      );
    },
    // _onKeyUp: function (e)
    // {
    //   this.setState({filterBox: e.target.value})
    // },
    _onChange: function (e)
    {
      this.setState({filter: e.target.value})
    },
    _onFilterChange: function ()
    {
      console.log(e.target)
    }
});


module.exports = SearchGlobalInput;

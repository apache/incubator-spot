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
var SpotUtils = require('../../utils/SpotUtils');

var FilterSelectInput = React.createClass({
  getInitialState: function() {
    return {filterBox: ''}
  },
  render: function() {

    return(
      <div className={`col-md-${this.props.col} col-xs-12 col-sm-6 col-lg-${this.props.col}`}>
        <div className="inner-addon right-addon">
          <i className="glyphicon glyphicon-search"></i>
          <input className="form-control filter-select" type="text" maxLength="15" placeholder={this.props.nameBox} id={this.props.idInput} autoFocus={false} onChange={this._onChange} value={this.state.filterBox} />
        </div>
      </div>
    )
  },
  _onChange: function (e)
  {
    this.setState({filterBox: e.target.value});
    SpotUtils.filterTextOnSelect($(this.props.idSelect), e.target.value);
  },

});


module.exports = FilterSelectInput;

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

var SelectInput = React.createClass({
  propTypes: {
    title: React.PropTypes.string.isRequired
  },
  componentDidMount: function() {
    // $('.panel-body-container').height();
  },
  render: function() {

    var labelOptions = this.props.options.map((data, i) =>
      <option key={i} value={data}>{data}</option>
    );


    return(
      <div className={`text-left col-md-${this.props.col} col-xs-12 col-sm-6 col-lg-${this.props.col}`}>
        <select size="8" className="col-md-12 select-picker form-control" id={this.props.who} onChange={this.logChange}>
          <option value="" selected>- Select -</option>
          {labelOptions}
        </select>
      </div>
    );
  }
});


module.exports = SelectInput;

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

var RatingInput = React.createClass({
  render: function() {
    var radioOptions = this.props.data.map((obj, i) =>
    <label className="radio-inline">
      <input key={i} type="radio" name={obj.radioName} id="rating" value={obj.value} defaultChecked={obj.selected}/>{obj.name}
    </label>
    );

    return(
      <div className={`col-md-${this.props.col} col-lg-${this.props.col} col-xs-12`}>
        <div className="col-md-2 col-lg-2 col-xs-12">
          <label>Rating: </label>
        </div>
        <div className="col-md-10 col-lg-10 col-xs-12">
          {radioOptions}
        </div>
      </div>
    );
  }
});


module.exports = RatingInput;

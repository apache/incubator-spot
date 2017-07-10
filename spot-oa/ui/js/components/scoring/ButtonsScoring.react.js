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

var ButtonsScoring = React.createClass({
    getInitialState: function() {
      return {dataScored: []};
    },
    render: function() {

      let classNameCol, classNameBtn;

      if(this.props.action === 'reset') {
        classNameCol = `col-md-${this.props.col} col-xs-12 col-lg-${this.props.col} col-md-offset-3 col-lg-offset-3`;
        classNameBtn = "btn col-md-12 col-xs-12 col-lg-12";
      } else {
        classNameCol = `col-md-${this.props.col} col-xs-6 col-lg-${this.props.col}`;
        classNameBtn = "btn btn-primary col-md-12 col-xs-12 col-lg-12";
      }

      return(
        <div className={classNameCol}>
          <button className={classNameBtn} onClick={this.checkAction}>{this.props.name}</button>
        </div>
      );

    },
    checkAction: function() {
      this.props.onChange();
    }
});

module.exports = ButtonsScoring;

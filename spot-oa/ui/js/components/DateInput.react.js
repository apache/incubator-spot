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

var $ = require('jquery');
var React = require('react');
var ReactDOM = require('react-dom');

var SpotActions = require('../actions/SpotActions');
var SpotUtils = require('../utils/SpotUtils');

var DateInput = React.createClass({
    propTypes: {
        name: React.PropTypes.string,
        value: React.PropTypes.string,
        onChange: React.PropTypes.func
    },
    getDefaultProps: function () {
        return {
            name: 'date',
            value: null,
            onChange: null
        }
    },
    getInitialState: function () {
        return {date: this.props.value || SpotUtils.getCurrentDate(this.props.name)};
    },
    componentDidMount: function () {
        $(ReactDOM.findDOMNode(this)).datepicker({
            format: "yyyy-mm-dd",
            autoclose: true
        })
            .on("changeDate", this._onChange);
    },
    componentWillUnmount: function () {
        $(ReactDOM.findDOMNode(this)).datepicker('remove');
    },
    render: function () {
        return (
            <input id={this.props.id} name={this.props.name} placeholder="Data date" type="text"
                   className="form-control" value={this.state.date} readOnly/>
        );
    },
    _onChange: function (e) {
        var date = e.date;

        SpotActions.setDate(SpotUtils.getDateString(date), this.props.name);
        this.props.onChange && this.props.onChange.call(this, e);
    }
});

module.exports = DateInput;

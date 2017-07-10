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

const RadioPicker = React.createClass({
    propTypes: {
        id: React.PropTypes.string,
        name: React.PropTypes.string,
        options: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
        value: React.PropTypes.string
    },
    getDefaultProps() {
        return {
            id: null,
            name: null,
            value: null
        };
    },
    getInitialState() {
        const state = {};

        state.value = this.props.value || (this.props.options.length>0 ? this.props.options[0] : null);

        return state;
    },
    render() {
        const options = Object.keys(this.props.options).map(option => {
            return <option value={option} selected={this.state.value==option}>
                {this.props.options[option]}
            </option>;
        });

        return <select id={this.props.id} className="form-control" name={this.props.name} onChange={this.onChange}>
            {options}
        </select>;
    },
    onChange(e) {
        const value = e.target.value;
        this.setState({value});

        this.props.onChange && this.props.onChange(value);
    }
});

module.exports = RadioPicker;

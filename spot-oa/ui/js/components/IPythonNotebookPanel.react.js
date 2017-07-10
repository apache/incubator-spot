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

var SpotActions = require('../actions/SpotActions');
var SpotConstants = require('../constants/SpotConstants');
var SpotStore = require('../stores/SpotStore');
var easyModeS = true;

var IPythonNotebookPanel = React.createClass({
    propTypes: {
        title: React.PropTypes.string.isRequired,
        date: React.PropTypes.string.isRequired,
        ipynb: React.PropTypes.string.isRequired
    },
    statics: {
        createIPythonNotebookClosure: function (title, easyMode) {
            var closure; 
            
            easyMode = typeof easyMode=='undefined' ? true : typeof easyMode=='boolean' ? easyMode : !!easyMode;

            easyModeS = easyMode; 
            closure = {
                getTitle: function () {
                    return title;
                },
                toggleIPythonNotebookPanel: function () {
                    easyMode = !easyMode;

                    SpotActions.toggleMode(title, easyMode);

                    if (easyMode) {
                        SpotActions.restorePanel(title);
                    }
                    else {
                        SpotActions.expandPanel(title);
                    }
                },
                getButtons: function () {
                    var className = 'glyphicon-education';

                    if (easyMode) {
                        className = 'education';
                    }
                    else {
                        className = 'user';
                    }

                    return [
                        <button key="easyModeBtn" type="button" className="btn btn-default btn-xs pull-right hidden-xs hidden-sm"
                                onClick={closure.toggleIPythonNotebookPanel}>
                            <span className={'glyphicon glyphicon-' + className} aria-hidden="true"></span>
                        </button>
                    ];
                }
            };

            return closure;
        }
    },
    getInitialState: function () {
        return {date: this.props.date.replace(/-/g, ''), easyMode: easyModeS};
    },
    componentDidMount: function () {
        SpotStore.addChangeDateListener(this._onDateChange);
        SpotStore.addPanelToggleModeListener(this._onToggleMode);
    },
    componentWillUnmount: function () {
        SpotStore.removeChangeDateListener(this._onDateChange);
        SpotStore.removePanelToggleModeListener(this._onToggleMode);
    },
    render: function () {
        var ipynbPath;

        ipynbPath = this.props.ipynb.replace('${date}', this.state.date);

        return (
            <iframe
                name="nbView"
                className="nbView"
                src={SpotConstants.NOTEBOOKS_PATH + '/' + ipynbPath + (this.state.easyMode ? '#showEasyMode' : '#showNinjaMode') }>
            </iframe>
        );
    },
    _onDateChange: function () {
        var date = SpotStore.getDate().replace(/-/g, '');

        this.setState({date: date});
    },
    _onToggleMode: function (panel, mode) {
        if (panel==this.props.title)
        {
            this.setState({
                easyMode: mode
            });
        }
    }
});

module.exports = IPythonNotebookPanel;

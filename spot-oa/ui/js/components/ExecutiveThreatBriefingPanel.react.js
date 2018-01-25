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

var ContentLoaderMixin = require('./ContentLoaderMixin.react');
var StoryBoardActions = require('../actions/StoryboardActions');
var RestStore = require('../stores/RestStore');

var ExecutiveThreatBriefingPanel = React.createClass({
    mixins: [ContentLoaderMixin],
    propTypes: {
        store: React.PropTypes.instanceOf(RestStore).isRequired
    },
    getInitialState: function () {
        return {loading: true};
    },
    componentDidMount: function ()
    {
        this.props.store.addChangeDataListener(this._onChange);
    },
    componentWillUmount: function ()
    {
        this.props.store.removeChangeDataListener(this._onChange);
    },
    renderContent: function ()
    {
        var comments;

        comments = this.state.data.map(function (comment, idx) {
            return (
                <div key={'comment' + idx} className="spot-comment panel panel-default">
                    <div id={'comment' + idx + '_title'} className="panel-heading" role="tab">
                        <h4 className="panel-title text-center">
                            <a className="collapsed" role="button" data-toggle="collapse" data-parent="#spot-executive-threat-briefing" href={'#comment' + idx + '_summary'}
                               aria-expanded="false" aria-controls={'comment' + idx + '_summary'} onClick={() => {this._onSelect(comment)}}>
                                {comment.title}
                            </a>
                        </h4>
                    </div>
                    <div id={'comment' + idx + '_summary'} className="panel-collapse collapse" role="tabpanel"
                         aria-labelledby={'comment' + idx + '_title'}>
                        <div className="panel-body" dangerouslySetInnerHTML={{__html: (comment.summary || '').replace(/\\n/g, '<br />')}} />
                    </div>
                </div>
            );
        }.bind(this));

        return (
            <div id="spot-executive-threat-briefing" className="panel-group" role="tablist" aria-multiselectable="true">
                {comments}
            </div>
        );
    },
    _onChange: function ()
    {
        this.replaceState(this.props.store.getData());
    },
    _onSelect: function (comment)
    {
        StoryBoardActions.selectComment(comment);
    }
});

module.exports = ExecutiveThreatBriefingPanel;

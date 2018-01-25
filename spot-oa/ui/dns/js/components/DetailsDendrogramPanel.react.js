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

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
const DendrogramStore = require('../stores/DendrogramStore');

const DetailsDendrogramPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin, DendrogramMixin],
    componentDidMount: function()
    {
        DendrogramStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function ()
    {
        DendrogramStore.removeChangeDataListener(this._onChange);
    },
    _onChange: function ()
    {
        const storeData = DendrogramStore.getData();

        if (storeData.loading) {
            this.setState(storeData);
        }
        else {
            const state = {loading: false};

            state.data = {
                id: 'root',
                name: DendrogramStore.getClientIp(),
                children: []
            };

            let nodeId = 0;
            state.leafNodes = 0;
            storeData.data.forEach(function (item)
            {
                let answers;

                let childNode = {
                    id: `node${++nodeId}`,
                    name: item.dns_qry_name
                };
                state.data.children.push(childNode);

                answers = item.dns_a;

                if (answers.length) {
                    let childId = 0;
                    childNode.children = [];

                    answers.forEach((answer) => {
                        state.leafNodes++;
                        childNode.children.push({
                            id: `node${nodeId}.${++childId}`,
                            name: answer
                        });
                    });
                }
                else {
                    state.leafNodes++;
                }
            });

            this.setState(state);
        }
    }
});

module.exports = DetailsDendrogramPanel;

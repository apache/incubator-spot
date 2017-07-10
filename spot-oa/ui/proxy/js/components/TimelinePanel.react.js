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
const React = require('react') ;

const TimelineStore = require('../stores/TimelineStore');
const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const TimelineMixin = require('../../../js/components/TimelineMixin.react');

const TimelineChart = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin, TimelineMixin],
    componentDidMount: function ()
    {
        TimelineStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function ()
    {
        TimelineStore.removeChangeDataListener(this._onChange);
    },
    _onChange() {
        const storeData = TimelineStore.getData();

        let state = {loading: storeData.loading};
        if (storeData.error) {
            state.error = storeData.error;
        }
        else if (!storeData.loading && storeData.data) {
            state = this._getStateFromStoreData(storeData.data);
        }

        this.replaceState(state);
    },
    _getStateFromStoreData(data)
    {
        const state = {
            loading: false,
            date: TimelineStore.getDate(),
            data: {}
        };

        /*
            Build a state similar to:

            {
                loading: false,
                name: 'IP_OF_INTEREST',
                date: 'CURRENT_DATE',
                data: {
                    'FIRST_UNIQUE_IP': {
                        name: 'FIRST_UNIQUE_IP',
                        dates: {
                            'YYYY-MM-DD HH:MM'
                        },
                        ports: {
                            '80': 10,
                            '443': 1
                        }
                    }
                }
            }

            And then pop-up every object value and replace 'data' fields with an
            array of object values. Turn objects into arrays
        */

        data.forEach((item) => {
            const ip = item.clientip;
            if (!state.data[ip]) {
                state.data[ip] = {
                    name: ip,
                    dates: {},
                    respcodes: {}
                };
            }

            const date = item.tstart.substr(0, 16);
            if (!state.data[ip].dates[date]) {
                state.data[ip].dates[date] = new Date(date);
            }

            const respcode = +item.respcode;
            if (!state.data[ip].respcodes[respcode]) {
                state.data[ip].respcodes[respcode] = 0;
            }

            state.data[ip].respcodes[respcode]++;
        });

        state.data = Object.keys(state.data).map(ip => {
            // Looking at ip data
            state.data[ip].dates = Object.keys(state.data[ip].dates).map((date) => {
                // Looking at date data

                // Unwrap date data
                return state.data[ip].dates[date];
            });

            // Find the most referenced respcode
            state.data[ip].respcode = Object.keys(state.data[ip].respcodes).reduce((currentRespCode, respcode) => {
                if (!currentRespCode) return respcode;

                return state.data[ip].respcodes[currentRespCode]>=state.data[ip].respcodes[respcode] ? currentRespCode: respcode;
            }, null);

            // we have found the most common respcode, get rid of respcodes data
            delete state.data[ip].respcodes;

            // Unwrap ip data
            return state.data[ip];
        });

        return state;
    },
    getTooltipContent (eventData) {
        return `${eventData.context.name}: At ${eventData.date}, the most common response code was ${eventData.context.respcode}`;
    }
});

const TimelinePanel = React.createClass({
    render() {
        return (
            <TimelineChart>
                <div />
            </TimelineChart>
        );
    }
});

module.exports = TimelinePanel;

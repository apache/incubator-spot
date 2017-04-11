// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const React = require('react') ;

const ChartMixin = require('../../../js/components/ChartMixin.react');
const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const TimelineStore = require('../stores/TimelineStore');
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
                        data: {
                            'YYYY-MM-DD HH:MM': {
                                ip: 'FIRST_UNIQUE_IP',
                                date: 'YYYY-MM-DD HH:MM',
                                ports: {
                                    '80': 10,
                                    '443': 1
                                }
                            }
                        }
                    }
                }
            }

            And then pop-up every object value and replace 'data' fields with an
            array of object values. Turn objects into arrays
        */

        const skipedIp = TimelineStore.getIp();
        data.forEach(item => {
            [
                {ipField:'srcip', portField:'sport'},
                {ipField:'dstip', portField:'dport'}
            ].forEach(({ipField, portField}) => {
                const ip = item[ipField];

                if (ip==skipedIp) return;

                if (!state.data[ip]) {
                    state.data[ip] = {
                        name: ip,
                        dates: {},
                        ports: {}
                    };
                }

                const date = item.tstart.substr(0, 16);
                if (!state.data[ip].dates[date]) {
                    state.data[ip].dates[date] = new Date(date);
                }

                const port = item[portField];
                if (!state.data[ip].ports[port]) {
                    state.data[ip].ports[port]=0;
                }

                state.data[ip].ports[port]++;
            });
        });

        state.data = Object.keys(state.data).map(ip => {
            // Looking at ip data
            state.data[ip].dates = Object.keys(state.data[ip].dates).map((date) => {
                // Looking at date data

                // Unwrap date data
                return state.data[ip].dates[date];
            });

            // Find the most referenced port
            state.data[ip].port = Object.keys(state.data[ip].ports).reduce((currentPort, port) => {
                if (!currentPort) return port;

                return state.data[ip].ports[currentPort]>=state.data[ip].ports[port] ? currentPort: port;
            }, null);

            // we have found the most common ports, get rid of port data
            delete state.data[ip].ports;

            // Unwrap ip data
            return state.data[ip];
        });

        return state;
    },
    getTooltipContent (eventData) {
        return `${eventData.context.name}: At ${eventData.date}, the most used port was ${eventData.context.port}`;
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

const React = require('react');

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
const IncidentProgressionStore = require('../stores/IncidentProgressionStore');

const IncidentProgressionPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin, DendrogramMixin],
    componentDidMount: function ()
    {
        IncidentProgressionStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function ()
    {
        IncidentProgressionStore.removeChangeDataListener(this._onChange);
    },
    _onChange: function ()
    {
        const storeData = IncidentProgressionStore.getData();
        const state = {
            loading: storeData.loading
        };

        if (storeData.error) {
            state.error = storeData.error;
        }
        else if(!storeData.loading && storeData.data) {
            state.data = {
                id: 'root',
                name: IncidentProgressionStore.getIp()
            };

            state.leafNodes = storeData.data.children.length;
            state.data.children = storeData.data.children.map((item, idx) => {
                return {
                    id: `node${idx}`,
                    name: item['name'],
                    children: item['children']
                };
            });
        }

        this.replaceState(state);
    }
});

module.exports = IncidentProgressionPanel;

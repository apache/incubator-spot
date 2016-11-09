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
        const state = {loading: storeData.loading};

        state.data = {
            id: 'root',
            name: IncidentProgressionStore.getFilterValue(),
            children: []
        };

        if (!state.loading)
        {
            let filterName = IncidentProgressionStore.getFilterName();

            state.leafNodes = 0;
            storeData.data.children.forEach((item) => {
                state.data.children.push({
                    id: `node${++state.leafNodes}`,
                    name: item['name'],
                    children: item['children']
                });
            });
        }

        this.setState(state);
    }
});

module.exports = IncidentProgressionPanel;

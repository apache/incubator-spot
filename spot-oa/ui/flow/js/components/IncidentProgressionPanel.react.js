var React = require('react');

var DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
var IncidentProgressionStore = require('../stores/IncidentProgressionStore');
 
var IncidentProgressionPanel = React.createClass({ 
    mixins: [DendrogramMixin],
    componentDidMount: function ()
    {
        IncidentProgressionStore.addChangeDataListener(this._onChange);
        window.addEventListener('resize', this.buildGraph);
    },
    componentWillUnmount: function ()
    {
        IncidentProgressionStore.removeChangeDataListener(this._onChange);
        window.removeEventListener('resize', this.buildGraph);
    },
    _onChange: function ()
    {
        var state, filterName, root;

        state = IncidentProgressionStore.getData();

        root = {
            name: IncidentProgressionStore.getFilterValue(),
            children: []
        };

        if (!state.loading)
        {
            filterName = IncidentProgressionStore.getFilterName();

            state.data.children.forEach(function (item)
            {
                root.children.push({
                    name: item['name'],
                    children: item['children']
                });
            }.bind(this));
        }

        state.root = root;
        delete state.data;

        this.setState(state);
    } 
});

module.exports = IncidentProgressionPanel;

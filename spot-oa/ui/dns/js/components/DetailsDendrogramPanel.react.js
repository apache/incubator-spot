var React = require('react');

var DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
var DendrogramStore = require('../stores/DendrogramStore');

var DetailsDendrogramPanel = React.createClass({
    mixins: [DendrogramMixin],
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
        var state, root;

        state = DendrogramStore.getData();

        root = {
            name: DendrogramStore.getSrcIp(),
            children: []
        };

        if (!state.loading)
        {
            state.data.forEach(function (item)
            {
                var children;

                root.children.push(item);

                item.name = item.dns_qry_name;
                item.children = [];

                // TODO: API must return answers as JSON array
                children = item.dns_a.split('|');

                children.forEach(function (child_name)
                {
                    item.children.push({
                        name: child_name
                    });
                });
            });
        }

        state.root = root;
        delete state.data;

        this.setState(state);
    }
});

module.exports = DetailsDendrogramPanel;

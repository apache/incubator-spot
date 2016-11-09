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
                name: DendrogramStore.getSrcIp(),
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

                answers = item.dns_a.split('|');

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

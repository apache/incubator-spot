const React = require('react');

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const DendrogramMixin = require('../../../js/components/DendrogramMixin.react');
const IncidentProgressionStore = require('../stores/IncidentProgressionStore');

const fieldMapper = {
    ip_dst: 'dns_qry_name',
    dns_qry_name: 'ip_dst'
};

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

    if (!storeData.loading) {
        state.error = storeData.error;

        if (storeData.data.length) {
          let filterName = IncidentProgressionStore.getFilterName();

          state.data = {
            id: 'root',
            name: IncidentProgressionStore.getFilterValue(),
            children: []
          };

          state.leafNodes = 0;
          storeData.data.forEach((item) => {
            state.data.children.push({
              id: `node${++state.leafNodes}`,
              name: item[fieldMapper[filterName]]
            });
          });
        }
    }

    this.replaceState(state);
  }
});

module.exports = IncidentProgressionPanel;

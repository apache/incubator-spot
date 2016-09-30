var React = require('react') ;   
var TimelineStore = require('../stores/TimelineStore');
var TimelineMixin = require('../../../js/components/TimelineMixin.react');

var fieldMapper = {
    srcip:'clientip',
    port: 'respcode',
    tstart: 'tstart',   
    tend: 'tend' 
};

var TimelinePanel = React.createClass({  
    mixins: [TimelineMixin],
    componentDidMount: function ()
    {
        TimelineStore.addChangeDataListener(this._onChange); 
    },
    componentWillUnmount: function ()
    {
        TimelineStore.removeChangeDataListener(this._onChange); 
    },
    _onChange: function ()
    {
        var state, filterName, root;
        state = TimelineStore.getData();  
     
        root = {
            name: TimelineStore.getFilterValue(),
            date: '',
            legend:false, 
            children: []
        };

        if (!state.loading && !state.error)
        {
            filterName = TimelineStore.getFilterName();
            root.date = TimelineStore._sdate; 
            root.legend = TimelineStore._slegend;  

            state.data.forEach(function (item)
            {
                root.children.push({
                  srcip: item[fieldMapper['srcip']],
                  port: item[fieldMapper['port']],
                  tstart: item[fieldMapper['tstart']],
                  tend: item[fieldMapper['tend']]
                });

            }.bind(this));
        }

        state.root = root; 
        delete state.data; 

        this.setState(state);
    }
});

module.exports = TimelinePanel;

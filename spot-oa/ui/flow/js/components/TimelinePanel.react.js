var React = require('react') ;   
var TimelineStore = require('../stores/TimelineStore');
var TimelineMixin = require('../../../js/components/TimelineMixin.react');


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
            children: []
        };

        if (!state.loading && !state.error)
        {
            filterName = TimelineStore.getFilterName();
            root.date = TimelineStore._sdate;
            root.children = state.data;  
        }

        state.root = root;
        delete state.data;

        this.setState(state);
    } 
});

module.exports = TimelinePanel;

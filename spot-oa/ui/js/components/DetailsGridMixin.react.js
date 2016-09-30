var DetailsGridMixin = {
    emptySetMessage: 'Please select one row from Suspicious Frame',
    getInitialState: function ()
    {
        return this.store.getData();
    },
    componentDidMount: function ()
    {
        this.store.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function ()
    {
        this.store.removeChangeDataListener(this._onChange);
    },
    // Event handler
    _onChange: function ()
    {
        this.setState(this.store.getData());
    }
};

module.exports = DetailsGridMixin;

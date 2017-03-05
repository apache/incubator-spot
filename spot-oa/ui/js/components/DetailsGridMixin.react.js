// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

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
        this.replaceState(this.store.getData());
    }
};

module.exports = DetailsGridMixin;

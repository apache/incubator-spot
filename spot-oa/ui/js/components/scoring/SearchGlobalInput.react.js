// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var SearchGlobalInput = React.createClass({
    getInitialState: function() {
      return {filter: ''}
    },
    render: function() {
      return(
        <div className={`col-md-${this.props.col} col-lg-${this.props.col} col-xs-12`}>
          <div className="inner-addon right-addon">
            <i className="glyphicon glyphicon-search"></i>
            <input className="form-control" type="text" placeholder="Quick scoring..." maxLength={this.props.maxlength || 15} id="globalTxt" autoFocus={false} onChange={this._onChange} value={this.state.filter} onKeyUp={this._onKeyUp} />
          </div>
        </div>
      );
    },
    // _onKeyUp: function (e)
    // {
    //   this.setState({filterBox: e.target.value})
    // },
    _onChange: function (e)
    {
      this.setState({filter: e.target.value})
    },
    _onFilterChange: function ()
    {
      console.log(e.target)
    }
});


module.exports = SearchGlobalInput;

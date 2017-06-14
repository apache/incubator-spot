// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');
var SpotUtils = require('../../utils/SpotUtils');

var FilterSelectInput = React.createClass({
  getInitialState: function() {
    return {filterBox: ''}
  },
  render: function() {

    return(
      <div className={`col-md-${this.props.col} col-xs-12 col-sm-6 col-lg-${this.props.col}`}>
        <div className="inner-addon right-addon">
          <i className="glyphicon glyphicon-search"></i>
          <input className="form-control filter-select" type="text" maxLength="15" placeholder={this.props.nameBox} id={this.props.idInput} autoFocus={true} onChange={this._onChange} value={this.state.filterBox} />
        </div>
      </div>
    )
  },
  _onChange: function (e)
  {
    this.setState({filterBox: e.target.value});
    SpotUtils.filterTextOnSelect($(this.props.idSelect), e.target.value);
  },

});


module.exports = FilterSelectInput;

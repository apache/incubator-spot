// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var SelectInput = React.createClass({
  propTypes: {
    title: React.PropTypes.string.isRequired
  },
  componentDidMount: function() {
    // $('.panel-body-container').height();
  },
  render: function() {

    var labelOptions = this.props.options.map((data, i) =>
      <option key={i} value={data}>{data}</option>
    );


    return(
      <div className={`text-left col-md-${this.props.col} col-xs-12 col-sm-6 col-lg-${this.props.col}`}>
        <select size="8" className="col-md-12 select-picker form-control" id={this.props.who} onChange={this.logChange}>
          <option value="" selected>- Select -</option>
          {labelOptions}
        </select>
      </div>
    );
  }
});


module.exports = SelectInput;

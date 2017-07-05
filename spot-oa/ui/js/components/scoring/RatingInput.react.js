// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var RatingInput = React.createClass({
  render: function() {
    var radioOptions = this.props.data.map((obj, i) =>
    <label className="radio-inline">
      <input key={i} type="radio" name={obj.radioName} id="rating" value={obj.value} defaultChecked={obj.selected}/>{obj.name}
    </label>
    );

    return(
      <div className={`col-md-${this.props.col} col-lg-${this.props.col} col-xs-12`}>
        <div className="col-md-2 col-lg-2 col-xs-12">
          <label>Rating: </label>
        </div>
        <div className="col-md-10 col-lg-10 col-xs-12">
          {radioOptions}
        </div>
      </div>
    );
  }
});


module.exports = RatingInput;

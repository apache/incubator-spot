// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var ButtonsScoring = React.createClass({
    getInitialState: function() {
      return {dataScored: []};
    },
    render: function() {

      let classNameCol, classNameBtn;

      if(this.props.action === 'reset') {
        classNameCol = `col-md-${this.props.col} col-xs-12 col-lg-${this.props.col} col-md-offset-3 col-lg-offset-3`;
        classNameBtn = "btn col-md-12 col-xs-12 col-lg-12";
      } else {
        classNameCol = `col-md-${this.props.col} col-xs-6 col-lg-${this.props.col}`;
        classNameBtn = "btn btn-primary col-md-12 col-xs-12 col-lg-12";
      }

      return(
        <div className={classNameCol}>
          <button className={classNameBtn} onClick={this.checkAction}>{this.props.name}</button>
        </div>
      );

    },
    checkAction: function() {
      this.props.onChange();
    }
});

module.exports = ButtonsScoring;

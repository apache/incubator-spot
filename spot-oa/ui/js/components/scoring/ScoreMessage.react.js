// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var ScoreMessage = React.createClass({
    getInitialState : function() {
       return { showMe : false };
    },

    render: function() {
            return(
                <div className="text-center hidden" id={this.props.who}>
                    <label className="text-danger">"Please remember to click save button to save the scorring."</label>
                </div>
            );
     },

    checkAction: function(value) {
        this.setState({ showMe : value}) ;
    }
});

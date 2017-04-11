// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var React = require('react');

var ContentLoaderMixin = {
    renderError: function () {
        return (
            <div className="spot-content-loader">
                <div className="text-center text-danger">{this.state.error}</div>
            </div>
        );
    },
    renderContentLoader: function () {
        return (
            <div className="spot-content-loader">
                <div className="spot-loader">
                    Loading <span className="spinner"></span>
                </div>
            </div>
        );
    },
    render: function () {
        var state, content;

        state = this.state || {};

        if (state.error) {
            content = this.renderError();
        }
        else if (state.loading) {
            content = this.renderContentLoader();
        }
        else {
            content = this.renderContent();
        }

        return content;
    }
};

module.exports = ContentLoaderMixin;

//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

const React = require('react');
const $ = require('jquery');

const ChartMixin = {
    propTypes: {
        className: React.PropTypes.string
    },
    getDefaultProps: function() {
        return {
            className: 'spot-chart'
        };
    },
    componentWillUpdate() {
        window.removeEventListener('resize', this.draw);
        if (this.svg) {
            $(this.svg).off('parentUpdate');
        }
    },
    componentDidUpdate: function (prevProps, prevState)
    {
        var state;

        prevState = prevState || {};
        state = this.state || {};

        if (state.error || !state.data) return;

        if (!state.loading) {
            if (prevState.loading) {
                this.buildChart();
            }

            state.data && this.draw();
        }

        window.addEventListener('resize', this.draw);
        if (this.svg) {
            $(this.svg).on('parentUpdate', this.draw);
        }
    },
    renderContent() {
        const state = this.state || {};
        var chartContent;

        if (state.data) {
            if (this.props.children) {
                chartContent = this.props.children;
            }
            else {
                chartContent = <svg ref={e => this.svg=e} />;
            }
        }

        return (
            <div className={this.props.className}>
                {chartContent}
            </div>
        );
    }
};

module.exports = ChartMixin;

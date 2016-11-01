var React = require('react');

var ChartMixin = {
    propTypes: {
        className: React.PropTypes.string
    },
    getDefaultProps: function() {
        return {
            className: 'spot-chart'
        };
    },
    componentDidUpdate: function (prevProps, prevState)
    {
        var state;

        prevState = prevState || {};
        state = this.state || {};

        if (state.error) return;

        if (!state.loading) {
            if (prevState.loading) {
                this.buildChart();
            }

            state.data && this.draw();
        }
    },
    renderContent: function () {
        const state = this.state || {};
        var chartContent;

        if (state.data) {
            if (this.props.children) {
                chartContent = this.props.children;
            }
            else {
                chartContent = <svg ref={e => {this.svg=e?e.getDOMNode():e;}} />;
            }
        }

        return (
            <div className={this.props.className}>
                {chartContent}
            </div>
        );
    },
};

module.exports = ChartMixin;

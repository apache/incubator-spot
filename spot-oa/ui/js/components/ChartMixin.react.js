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

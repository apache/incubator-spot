const React = require('react');

const RadioPicker = React.createClass({
    propTypes: {
        id: React.PropTypes.string,
        name: React.PropTypes.string,
        options: React.PropTypes.arrayOf(React.PropTypes.string).isRequired,
        value: React.PropTypes.string
    },
    getDefaultProps() {
        return {
            id: null,
            name: null,
            value: null
        };
    },
    getInitialState() {
        const state = {};

        state.value = this.props.value || (this.props.options.length>0 ? this.props.options[0] : null);

        return state;
    },
    render() {
        const options = Object.keys(this.props.options).map(option => {
            return <option value={option} selected={this.state.value==option}>
                {this.props.options[option]}
            </option>;
        });

        return <select id={this.props.id} className="form-control" name={this.props.name} onChange={this.onChange}>
            {options}
        </select>;
    },
    onChange(e) {
        const value = e.target.value;
        this.setState({value});

        this.props.onChange && this.props.onChange(value);
    }
});

module.exports = RadioPicker;

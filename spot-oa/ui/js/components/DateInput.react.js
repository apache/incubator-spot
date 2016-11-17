var $ = require('jquery');
var React = require('react');
var ReactDOM = require('react-dom');

var SpotActions = require('../actions/SpotActions');
var SpotUtils = require('../utils/SpotUtils');

var DateInput = React.createClass({
    propTypes: {
        name: React.PropTypes.string,
        value: React.PropTypes.string,
        onChange: React.PropTypes.func
    },
    getDefaultProps: function () {
        return {
            name: 'date',
            value: null,
            onChange: null
        }
    },
    getInitialState: function () {
        return {date: this.props.value || SpotUtils.getCurrentDate(this.props.name)};
    },
    componentDidMount: function () {
        $(ReactDOM.findDOMNode(this)).datepicker({
            format: "yyyy-mm-dd",
            autoclose: true
        })
            .on("changeDate", this._onChange);
    },
    componentWillUnmount: function () {
        $(ReactDOM.findDOMNode(this)).datepicker('remove');
    },
    render: function () {
        return (
            <input id={this.props.id} name={this.props.name} placeholder="Data date" type="text"
                   className="form-control" value={this.state.date} readOnly/>
        );
    },
    _onChange: function (e) {
        var date = e.date;

        SpotActions.setDate(SpotUtils.getDateString(date), this.props.name);
        this.props.onChange && this.props.onChange.call(this, e);
    }
});

module.exports = DateInput;

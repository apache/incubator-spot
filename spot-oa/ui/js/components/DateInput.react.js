var $ = require('jquery');
var React = require('react');

var OniActions = require('../actions/OniActions');
var OniUtils = require('../utils/OniUtils');

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
        return {date: this.props.value || OniUtils.getCurrentDate(this.props.name)};
    },
    componentDidMount: function () {
        $(this.getDOMNode()).datepicker({
            format: "yyyy-mm-dd",
            autoclose: true
        })
            .on("changeDate", this._onChange);
    },
    componentWillUnmount: function () {
        $(this.getDOMNode()).datepicker('remove');
    },
    render: function () {
        return (
            <input id={this.props.id} name={this.props.name} placeholder="Data date" type="text"
                   className="form-control" value={this.state.date} readOnly/>
        );
    },
    _onChange: function (e) {
        var date = e.date;

        OniActions.setDate(OniUtils.getDateString(date), this.props.name);
        this.props.onChange && this.props.onChange.call(this, e);
    }
});

module.exports = DateInput;

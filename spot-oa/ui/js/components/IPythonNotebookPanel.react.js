var React = require('react');

var OniActions = require('../actions/OniActions');
var OniConstants = require('../constants/OniConstants');
var OniStore = require('../stores/OniStore');

var IPythonNotebookPanel = React.createClass({
    propTypes: {
        title: React.PropTypes.string.isRequired,
        date: React.PropTypes.string.isRequired,
        ipynb: React.PropTypes.string.isRequired
    },
    statics: {
        createIPythonNotebookClosure: function (title, easyMode) {
            var closure;

            easyMode = typeof easyMode=='undefined' ? true : typeof easyMode=='boolean' ? easyMode : !!easyMode;

            closure = {
                getTitle: function () {
                    return title;
                },
                toggleIPythonNotebookPanel: function () {
                    easyMode = !easyMode;

                    OniActions.toggleMode(title, easyMode);

                    if (easyMode) {
                        OniActions.restorePanel(title);
                    }
                    else {
                        OniActions.expandPanel(title);
                    }
                },
                getButtons: function () {
                    var className = 'glyphicon-education';

                    if (easyMode) {
                        className = 'education';
                    }
                    else {
                        className = 'user';
                    }

                    return [
                        <button key="easyModeBtn" type="button" className="btn btn-default btn-xs pull-right hidden-xs hidden-sm"
                                onClick={closure.toggleIPythonNotebookPanel}>
                            <span className={'glyphicon glyphicon-' + className} aria-hidden="true"></span>
                        </button>
                    ];
                }
            };

            return closure;
        }
    },
    getInitialState: function () {
        return {date: this.props.date.replace(/-/g, ''), easyMode: true};
    },
    componentDidMount: function () {
        OniStore.addChangeDateListener(this._onDateChange);
        OniStore.addPanelToggleModeListener(this._onToggleMode);
    },
    componentWillUnmount: function () {
        OniStore.removeChangeDateListener(this._onDateChange);
        OniStore.removePanelToggleModeListener(this._onToggleMode);
    },
    render: function () {
        var ipynbPath;

        ipynbPath = this.props.ipynb.replace('${date}', this.state.date);

        return (
            <iframe
                name="nbView"
                className="nbView"
                src={OniConstants.NOTEBOOKS_PATH + '/' + ipynbPath + (this.state.easyMode ? '#showEasyMode' : '#showNinjaMode') }>
            </iframe>
        );
    },
    _onDateChange: function () {
        var date = OniStore.getDate().replace(/-/g, '');

        this.setState({date: date});
    },
    _onToggleMode: function (panel, mode) {
        if (panel==this.props.title)
        {
            this.setState({
                easyMode: mode
            });
        }
    }
});

module.exports = IPythonNotebookPanel;

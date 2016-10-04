var React = require('react');

var SpotActions = require('../actions/SpotActions');
var SpotConstants = require('../constants/SpotConstants');
var SpotStore = require('../stores/SpotStore');

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

                    SpotActions.toggleMode(title, easyMode);

                    if (easyMode) {
                        SpotActions.restorePanel(title);
                    }
                    else {
                        SpotActions.expandPanel(title);
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
        SpotStore.addChangeDateListener(this._onDateChange);
        SpotStore.addPanelToggleModeListener(this._onToggleMode);
    },
    componentWillUnmount: function () {
        SpotStore.removeChangeDateListener(this._onDateChange);
        SpotStore.removePanelToggleModeListener(this._onToggleMode);
    },
    render: function () {
        var ipynbPath;

        ipynbPath = this.props.ipynb.replace('${date}', this.state.date);

        return (
            <iframe
                name="nbView"
                className="nbView"
                src={SpotConstants.NOTEBOOKS_PATH + '/' + ipynbPath + (this.state.easyMode ? '#showEasyMode' : '#showNinjaMode') }>
            </iframe>
        );
    },
    _onDateChange: function () {
        var date = SpotStore.getDate().replace(/-/g, '');

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

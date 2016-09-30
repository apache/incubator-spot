var React = require('react');
var assign = require('object-assign');

var OniActions = require('../actions/OniActions');
var OniStore = require('../stores/OniStore');

var Panel = React.createClass({
    propTypes: {
        title: React.PropTypes.string.isRequired,
        container: React.PropTypes.bool,
        header: React.PropTypes.bool,
        className: React.PropTypes.string,
        reloadable: React.PropTypes.bool,
        onReload: React.PropTypes.func,
        expandable: React.PropTypes.bool,
        extraButtons: React.PropTypes.oneOfType([React.PropTypes.func, React.PropTypes.arrayOf(React.PropTypes.node)])
    },
    getDefaultProps: function () {
        return {
            className: 'col-md-6',
            header: true
        }
    },
    getInitialState: function () {
        return {hidden: false, maximized: false, baseMode: true};
    },
    componentDidMount: function () {
        OniStore.addPanelExpandListener(this._onExpand);
        OniStore.addPanelRestoreListener(this._onRestore);
    },
    componentWillUnmount: function () {
        OniStore.removePanelExpandListener(this._onExpand);
        OniStore.removePanelRestoreListener(this._onRestore);
    },
    render: function () {
        var panelHeading, buttons, cssCls, containerCss;

        if (this.props.header) {
            buttons = [];
            if (this.props.reloadable) {
                buttons.push(
                    <li key="realoadBtn" className="refresh">
                        <button type="button" className="btn btn-default btn-xs pull-right"
                                onClick={this.props.onReload}>
                            <span className="glyphicon glyphicon-refresh" aria-hidden="true"></span>
                        </button>
                    </li>
                );
            }

            if (this.props.expandable) {
                if (this.state.maximized) {
                    buttons.push(
                        <li key="toggleBtn" className="resize-small hidden-xs hidden-sm">
                            <button className="btn btn-default btn-xs pull-right margin-side5"
                                    onClick={this._onToggleClick}>
                                <span className="glyphicon glyphicon-resize-small" aria-hidden="true"></span>
                            </button>
                        </li>
                    );
                }
                else {
                    buttons.push(
                        <li key="toggleBtn" className="fullscreen hidden-xs hidden-sm">
                            <button className="btn btn-default btn-xs pull-right margin-sides5"
                                    onClick={this._onToggleClick}>
                                <span className="glyphicon glyphicon-fullscreen" aria-hidden="true"></span>
                            </button>
                        </li>
                    );
                }
            }

            if (this.props.extraButtons) {
                if (typeof this.props.extraButtons=='function')
                {
                    buttons = buttons.concat(this.props.extraButtons());
                }
                else {
                    buttons = buttons.concat(this.props.extraButtons);
                }
            }
        }

        cssCls = this.state.maximized ? 'oni-maximized col-md-12' : this.state.minimized ? 'oni-minimized' : "";

        if (this.props.header) {
            panelHeading = (
                <div className="panel-heading">
                    <h3 className="panel-title pull-left src-only"><strong>{this.props.title}</strong></h3>
                    <ul className="panel-toolbar pull-right">
                        {buttons}
                    </ul>
                </div>
            );
        }

        containerCss = 'panel-body-container' + (this.props.container ? ' container-box' : '');

        return (
            <div className={'oni-frame ' + this.props.className + ' ' + cssCls}>
                <div className="oni-frame-content">
                    <div className={'panel panel-primary' + (this.props.header ? '' : ' no-heading')}>
                        {panelHeading}
                        <div className="panel-body">
                            <div className={containerCss}>
                                {this.props.children}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    },
    _onToggleClick: function () {
        if (this.state.maximized) {
            OniActions.restorePanel(this.props.title);
        }
        else {
            OniActions.expandPanel(this.props.title);
        }
    },
    _onExpand: function (panel) {
        if (this.props.title === panel) {
            this.setState({
                minimized: false,
                maximized: true
            });
        }
        else {
            this.setState({
                minimized: true,
                maximized: false
            });
        }
    },
    _onRestore: function () {
        this.setState({
            minimized: false,
            maximized: false
        });
    }
});

module.exports = Panel;

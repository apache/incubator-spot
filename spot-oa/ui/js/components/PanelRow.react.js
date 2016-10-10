var React = require('react');

var SpotStore = require('../stores/SpotStore');

var PanelRow = React.createClass({
  propTypes: {
    maximized: React.PropTypes.bool
  },
  getDefaultProps: function ()
  {
    return {
      maximized: false
    }
  },
  getInitialState: function () {
    var state;

    state = {maximized: this.props.maximized, minimized: false, childrenTitles: []};

    React.Children.forEach(this.props.children, child => {
        state.childrenTitles.push(child.props.title);
    });

    return state;
  },
  componentDidMount: function ()
  {
      SpotStore.addPanelExpandListener(this._onChildExpanded);
      SpotStore.addPanelRestoreListener(this._onChildRestored);
  },
  render: function () {
    var cssCls = this.state.maximized ? 'spot-maximized' : this.state.minimized ? 'spot-minimized' : '';

    return (
      <div className={'spot-row row ' + cssCls}>
        {this.props.children}
      </div>
    );
  },
  componentWillUnmount: function ()
  {
    SpotStore.removePanelExpandListener(this._onChildExpanded);
    SpotStore.removePanelRestoreListener(this._onChildRestored);
  },
  _onChildExpanded: function (childTitle) {
    if (this.state.childrenTitles.indexOf(childTitle)>=0)
    {
        this.setState({maximized: true});
    }
    else
    {
        this.setState({minimized: true});
    }
  },
  _onChildRestored: function (childTitle) {
    if (this.state.childrenTitles.indexOf(childTitle)>=0)
    {
        this.setState({maximized: false});
    }
    else
    {
        this.setState({minimized: false});
    }
  }
});

module.exports = PanelRow;

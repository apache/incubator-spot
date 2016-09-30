var React = require('react');

var OniConstants = require('../../../js/constants/OniConstants');

var OniStore = require('../../../js/stores/OniStore');

var DetailsTablePanel = require('./DetailsTablePanel.react');
var DetailsChordsPanel = require('./DetailsChordsPanel.react');

var DetailsPanel = React.createClass({
  propTypes: {
    title: React.PropTypes.string.isRequired
  },
  getInitialState: function ()
  {
    return {};
  },
  componentDidMount: function ()
  {
    OniStore.addPanelToggleModeListener(this._onToggleMode);
  },
  componentWillUnmount: function ()
  {
    OniStore.removePanelToggleModeListener(this._onToggleMode);
  },
  render: function ()
  {
    if (this.state.mode === OniConstants.VISUAL_DETAILS_MODE)
    {
      return (
        <DetailsChordsPanel />
      );
    }
    else
    {
      return (
        <div className="inner-container-box">
          <DetailsTablePanel />
        </div>
      );
    }
  },
  _onToggleMode: function (panel, mode)
  {
    if (panel!==this.props.title) return;

    this.setState({mode: mode});
  }
});

module.exports = DetailsPanel;

var React = require('react');

var SpotConstants = require('../../../js/constants/SpotConstants');

var SpotStore = require('../../../js/stores/SpotStore');

var DetailsTablePanel = require('./DetailsTablePanel.react');
var DetailsDendrogramPanel = require('./DetailsDendrogramPanel.react');

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
    SpotStore.addPanelToggleModeListener(this._onToggleMode);
  },
  componentWillUnmount: function ()
  {
    SpotStore.removePanelToggleModeListener(this._onToggleMode);
  },
  render: function ()
  {
    if (this.state.mode === SpotConstants.VISUAL_DETAILS_MODE)
    {
      return (
        <DetailsDendrogramPanel className="dendrogram" />
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

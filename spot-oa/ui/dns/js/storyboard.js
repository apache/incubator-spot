var React = require('react');

var SpotActions = require('../../js/actions/SpotActions');
var SpotConstants = require('../../js/constants/SpotConstants');
var SpotUtils = require('../../js/utils/SpotUtils');
var DateInput = require('../../js/components/DateInput.react');
var StoryboardActions = require('../../js/actions/StoryboardActions');

React.render(
  (
    <form className="form-inline">
      <div className="form-group">
        <label htmlFor="dataDatePicker">Data Date:</label>
        <div className="input-group input-group-xs">
          <DateInput id="dataDatePicker" onChange={StoryboardActions.reloadComments} />
          <div className="input-group-addon">
            <span className="glyphicon glyphicon-calendar" aria-hidden="true"></span>
          </div>
        </div>
      </div>
    </form>
  ),
  document.getElementById('nav_form')
);

var PanelRow = require('../../js/components/PanelRow.react');
var Panel = require('../../js/components/Panel.react');

var ExecutiveThreatBriefingPanel = require('../../js/components/ExecutiveThreatBriefingPanel.react');
var IncidentProgressionPanel = require('./components/IncidentProgressionPanel.react');

var CommentsStore = require('./stores/CommentsStore');

React.render(
  <div id="spot-content">
    <PanelRow>
      <Panel title={SpotConstants.COMMENTS_PANEL} expandable>
        <ExecutiveThreatBriefingPanel store={CommentsStore} />
      </Panel>
      <Panel title={SpotConstants.INCIDENT_PANEL} expandable>
        <IncidentProgressionPanel className="dendrogram" />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);

// Set search criteria
var date;

date = SpotUtils.getCurrentDate();

SpotActions.setDate(date);

// Make inital load
StoryboardActions.reloadComments();

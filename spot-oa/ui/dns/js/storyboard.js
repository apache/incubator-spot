var React = require('react');

var OniActions = require('../../js/actions/OniActions');
var OniConstants = require('../../js/constants/OniConstants');
var OniUtils = require('../../js/utils/OniUtils');
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
  <div id="oni-content">
    <PanelRow>
      <Panel title={OniConstants.COMMENTS_PANEL} expandable>
        <ExecutiveThreatBriefingPanel store={CommentsStore} />
      </Panel>
      <Panel title={OniConstants.INCIDENT_PANEL} expandable>
        <IncidentProgressionPanel />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('oni-content-wrapper')
);

// Set search criteria
var date;

date = OniUtils.getCurrentDate();

OniActions.setDate(date);

// Make inital load
StoryboardActions.reloadComments();

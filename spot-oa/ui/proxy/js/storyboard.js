require("babel-polyfill");
var React = require('react');

var DateInput = require('../../js/components/DateInput.react');
var SpotActions = require('../../js/actions/SpotActions');
var SpotConstants = require('../../js/constants/SpotConstants');
var SpotUtils = require('../../js/utils/SpotUtils');
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
var TimelinePanel = require('./components/TimelinePanel.react');

var CommentsStore = require('./stores/CommentsStore');

React.render(
  <div id="spot-content">
    <PanelRow maximized>
        <Panel className="col-md-4 spot-sidebar" title={SpotConstants.COMMENTS_PANEL} expandable>
            <ExecutiveThreatBriefingPanel store={CommentsStore} />
        </Panel>
        <Panel className="col-md-8 spot-stage" title={SpotConstants.INCIDENT_PANEL} container expandable>
            <IncidentProgressionPanel className="spot-incident-progression"/>
        </Panel>
        <Panel className="col-md-4 spot-sidebar timeline" title={SpotConstants.TIMELINE_PANEL} expandable>
            <TimelinePanel />
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

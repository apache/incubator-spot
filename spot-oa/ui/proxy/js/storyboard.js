require("babel-polyfill");
var React = require('react');

var DateInput = require('../../js/components/DateInput.react');
var OniActions = require('../../js/actions/OniActions');
var OniConstants = require('../../js/constants/OniConstants');
var OniUtils = require('../../js/utils/OniUtils');
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
  <div id="oni-content">
    <PanelRow maximized>
        <Panel className="col-md-4 oni-sidebar" title={OniConstants.COMMENTS_PANEL} expandable>
            <ExecutiveThreatBriefingPanel store={CommentsStore} />
        </Panel>
        <Panel className="col-md-8 oni-stage" title={OniConstants.INCIDENT_PANEL} container expandable>
            <IncidentProgressionPanel className="oni-incident-progression"/>
        </Panel>
        <Panel className="col-md-4 oni-sidebar sb_timeline" title={OniConstants.TIMELINE_PANEL} expandable>
            <TimelinePanel />
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

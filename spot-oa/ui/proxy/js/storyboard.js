require("babel-polyfill");
const React = require('react');
const ReactDOM = require('react-dom');

const DateInput = require('../../js/components/DateInput.react');
const SpotActions = require('../../js/actions/SpotActions');
const SpotConstants = require('../../js/constants/SpotConstants');
const SpotUtils = require('../../js/utils/SpotUtils');
const StoryboardActions = require('../../js/actions/StoryboardActions');

ReactDOM.render(
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

const PanelRow = require('../../js/components/PanelRow.react');
const Panel = require('../../js/components/Panel.react');

const ExecutiveThreatBriefingPanel = require('../../js/components/ExecutiveThreatBriefingPanel.react');
const IncidentProgressionPanel = require('./components/IncidentProgressionPanel.react');
const TimelinePanel = require('./components/TimelinePanel.react');

const CommentsStore = require('./stores/CommentsStore');

ReactDOM.render(
  <div id="spot-content">
    <PanelRow maximized>
        <Panel className="col-md-4 spot-sidebar" title={SpotConstants.COMMENTS_PANEL} expandable>
            <ExecutiveThreatBriefingPanel store={CommentsStore} />
        </Panel>
        <Panel className="col-md-8 spot-stage" title={SpotConstants.INCIDENT_PANEL} container expandable>
            <IncidentProgressionPanel className="spot-incident-progression" />
        </Panel>
        <Panel className="col-md-4 spot-sidebar timeline" title={SpotConstants.TIMELINE_PANEL} expandable>
            <TimelinePanel />
        </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);

// Set search criteria
const date = SpotUtils.getCurrentDate();

SpotActions.setDate(date);

// Make inital load
StoryboardActions.reloadComments();

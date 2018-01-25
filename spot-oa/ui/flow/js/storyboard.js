//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

const React = require('react');
const ReactDOM = require('react-dom');

const SpotConstants = require('../../js/constants/SpotConstants');
const SpotActions = require('../../js/actions/SpotActions');
const EdInActions = require('../../js/actions/EdInActions');
const StoryboardActions = require('../../js/actions/StoryboardActions');
const SpotUtils = require('../../js/utils/SpotUtils');

// Build and Render Toolbar
const DateInput = require('../../js/components/DateInput.react');
const MainMenu = require('../../js/menu/components/MainMenu.react');

function resetFilterAndReload()
{
  EdInActions.setFilter('');
  StoryboardActions.reloadComments();
};

ReactDOM.render(
  <MainMenu />,
  document.getElementById('main-menu')
);

ReactDOM.render(
  (
    <form className="form-inline">
      <div className="form-group">
        <label htmlFor="dataDatePicker" className="control-label">Data Date:</label>
        <div className="input-group input-group-xs">
          <DateInput id="dataDatePicker" onChange={resetFilterAndReload} />
          <div className="input-group-addon">
            <span className="glyphicon glyphicon-calendar" aria-hidden="true"></span>
          </div>
        </div>
      </div>
    </form>
  ),
  document.getElementById('nav_form')
);

// Build and Render Edge Investigation's panels
const PanelRow = require('../../js/components/PanelRow.react');
const Panel = require('../../js/components/Panel.react');

const ExecutiveThreatBriefingPanel = require('../../js/components/ExecutiveThreatBriefingPanel.react');
const IncidentProgressionPanel = require('./components/IncidentProgressionPanel.react');
const ImpactAnalysis = require('./components/ImpactAnalysisPanel.react');
const MapView = require('./components/GlobeViewPanel.react');
const TimelinePanel = require('./components/TimelinePanel.react');

const CommentsStore = require('./stores/CommentsStore');

ReactDOM.render(
  <div id="spot-content" className="storyboard">
  <PanelRow>
      <Panel title={SpotConstants.COMMENTS_PANEL} expandable className="col-md-6" >
          <ExecutiveThreatBriefingPanel store={CommentsStore} />
      </Panel>
      <Panel title={SpotConstants.INCIDENT_PANEL} expandable container className="col-md-6">
          <IncidentProgressionPanel className="dendrogram" />
      </Panel>
    </PanelRow>
    <PanelRow>
      <Panel title={SpotConstants.IMPACT_ANALYSIS_PANEL} expandable container className="col-md-4 sb_impact">
        <ImpactAnalysis />
      </Panel>
      <Panel title={SpotConstants.GLOBE_VIEW_PANEL} expandable container className="col-md-4 sb_globe_view">
          <MapView />
      </Panel>
      <Panel title={SpotConstants.TIMELINE_PANEL} expandable className="col-md-4 timeline" >
          <TimelinePanel />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);


const initial_filter = SpotUtils.getCurrentFilter();

// Set search criteria
SpotActions.setDate(SpotUtils.getCurrentDate());
initial_filter && StoryboardActions.setFilter(initial_filter);

// Load data
StoryboardActions.reloadComments();

// Create a hook to allow notebook iframe to reloadComments
window.iframeReloadHook = StoryboardActions.reloadComments;

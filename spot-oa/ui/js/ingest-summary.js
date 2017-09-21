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

const SpotActions = require('./actions/SpotActions');
const InSumActions = require('./actions/InSumActions');
const EdInActions = require('./actions/EdInActions');
const IngestSummaryStore = require('./stores/IngestSummaryStore');
const SpotConstants = require('./constants/SpotConstants');
const SpotUtils = require('./utils/SpotUtils');
const DateUtils = require('./utils/DateUtils');

// Build and Render Toolbar
const DateInput = require('./components/DateInput.react');
const OptionPicker = require('./components/OptionPicker.react');
const MainMenu = require('./menu/components/MainMenu.react');

// Find out period
var startDate, endDate, today;

today = new Date();
startDate = SpotUtils.getUrlParam(SpotConstants.START_DATE);
endDate = SpotUtils.getUrlParam(SpotConstants.END_DATE);

if (!startDate && endDate)
{
  startDate = DateUtils.formatDate(DateUtils.calcDate(DateUtils.parseDate(endDate), -7));
}
else if (startDate && !endDate)
{
  endDate = DateUtils.formatDate(DateUtils.calcDate(DateUtils.parseDate(startDate), 7));
}
else if (!startDate && !endDate)
{
  // Default endDate to today and startDate to 7 days before
  startDate = DateUtils.formatDate(DateUtils.calcDate(today, -7));
  endDate = DateUtils.formatDate(today);
}

// We got values for both dates, make use endDate is after startDate
if (endDate < startDate)
{
  // Use today var to switch dates
  today = startDate;
  startDate = endDate;
  endDate = today;
}
const PIPELINES = IngestSummaryStore.PIPELINES;
//check if pipeline is on URL, if not the first element of PIPELINES is taken
const DEFAULT_PIPELINE =  SpotUtils.getUrlParam('pipeline') || Object.keys(PIPELINES)[0];

const loadPipeline = function loadPipeline(pipeline) {
    SpotActions.setPipeline(pipeline);
    InSumActions.reloadSummary();
}

ReactDOM.render(
  <MainMenu />,
  document.getElementById('main-menu')
);

SpotActions.setDate(SpotUtils.getCurrentDate());

ReactDOM.render(
    <form className="form-inline">
        <div className="form-group">
            <label htmlFor="pipeline-picker">Source: </label>
            <div className="input-group input-group-xs">
                <OptionPicker
                    id="pipeline-picker"
                    options={PIPELINES}
                    value={DEFAULT_PIPELINE}
                    onChange={loadPipeline} />
            </div>
        </div>
        <div className="form-group">
            <label htmlFor="startDatePicker">Period:</label>
            <div className="input-group input-group-xs">
                <div className="input-group-addon">
                    <span className="glyphicon glyphicon-calendar" aria-hidden="true"></span>
                </div>
                <DateInput id="startDatePicker" name={SpotConstants.START_DATE} value={startDate}/>
            </div>
        </div>
        <div className="form-group">
            <label htmlFor="endDatePicker"> - </label>
            <div className="input-group input-group-xs">
                <DateInput id="endDatePicker" name={SpotConstants.END_DATE} value={endDate} />
                <div className="input-group-btn">
                    <button className="btn btn-default" type="button" title="Reload" onClick={InSumActions.reloadSummary}>
                        <span className="glyphicon glyphicon-repeat" aria-hidden="true"></span>
                    </button>
                </div>
            </div>
        </div>
    </form>,
    document.getElementById('nav_form')
);

// Build and Render Edge Investigation's panels
const PanelRow = require('./components/PanelRow.react');
const Panel = require('./components/Panel.react');
//
const IngestSummaryPanel = require('./components/IngestSummaryPanel.react');

ReactDOM.render(
  <div id="spot-content">
    <PanelRow maximized>
      <Panel title="Ingest Summary" container header={false} className="col-md-12">
        <IngestSummaryPanel className="is-chart" />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);



// Set period
SpotActions.setDate(startDate, SpotConstants.START_DATE);
SpotActions.setDate(endDate, SpotConstants.END_DATE);
SpotActions.setPipeline(DEFAULT_PIPELINE);

// Load data
loadPipeline(DEFAULT_PIPELINE);

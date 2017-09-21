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

const SpotActions = require('../../js/actions/SpotActions');
const SpotUtils = require('../../js/utils/SpotUtils');

const DateInput = require('../../js/components/DateInput.react');
const MainMenu = require('../../js/menu/components/MainMenu.react');

ReactDOM.render(
  <MainMenu />,
  document.getElementById('main-menu')
);

ReactDOM.render(
    (
      <form className="form-inline">
        <div className="form-group">
          <label htmlFor="dataDatePicker">Data Date:</label>
          <div className="input-group input-group-xs">
            <DateInput id="dataDatePicker" />
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
const IPythonNotebookPanel = require('../../js/components/IPythonNotebookPanel.react');

const ipynbClosure = IPythonNotebookPanel.createIPythonNotebookClosure('');

ReactDOM.render(
  <div id="spot-content">
    <PanelRow maximized>
      <Panel title={ipynbClosure.getTitle()} container className="col-md-12" extraButtons={ipynbClosure.getButtons}>
        <IPythonNotebookPanel title={ipynbClosure.getTitle()} date={SpotUtils.getCurrentDate()} ipynb="proxy/${date}/Threat_Investigation.ipynb" />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);
// Set search criteria
const date = SpotUtils.getCurrentDate();

SpotActions.setDate(date);

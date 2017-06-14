// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const React = require('react');
const ReactDOM = require('react-dom');

const SpotConstants = require('../../js/constants/SpotConstants');
const SpotActions = require('../../js/actions/SpotActions');
const EdInActions = require('../../js/actions/EdInActions');
const SpotUtils = require('../../js/utils/SpotUtils');

// Build and Render Toolbar
const FilterInput = require('./components/FilterInput.react');
const DateInput = require('../../js/components/DateInput.react');

function resetFilterAndReload() {
    EdInActions.setFilter('');
    EdInActions.reloadSuspicious();
};

function switchComponents () {
  SpotUtils.switchDivs(SpotConstants.DETAILS_PANEL, SpotConstants.SCORING_PANEL);
};

ReactDOM.render(
    (
        <form className="form-inline">
            <div className="form-group">
                <label htmlFor="uri_filter" className="control-label">IP/URI:</label>
                <div className="input-group input-group-xs">
                    <FilterInput id="uri_filter" />
                    <div className="input-group-btn">
                        <button className="btn btn-primary" type="button" id="btn_searchIp"
                                title="Enter an IP Address or Hostname and click the search button to filter the results."
                                onClick={EdInActions.reloadSuspicious}>
                            <span className="glyphicon glyphicon-search" aria-hidden="true"></span>
                        </button>
                    </div>
                </div>
            </div>
            <div className="form-group">
                <label htmlFor="dataDatePicker" className="control-label">Data Date:</label>
                <div className="input-group input-group-xs">
                    <DateInput id="dataDatePicker" onChange={resetFilterAndReload}/>
                    <div className="input-group-btn">
                        <button className="btn btn-default" type="button" title="Reset filter"
                                onClick={resetFilterAndReload}>
                            <span className="glyphicon glyphicon-repeat" aria-hidden="true"></span>
                        </button>
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

const SuspiciousPanel = require('./components/SuspiciousPanel.react');
const NetworkViewPanel = require('./components/NetworkViewPanel.react');
const ScoreNotebook = require('./components/ScoreNotebook.react');
const DetailsPanel = require('./components/DetailsPanel.react');

ReactDOM.render(
    <div id="spot-content">
        <PanelRow>
            <Panel title={SpotConstants.SUSPICIOUS_PANEL} expandable reloadable onReload={EdInActions.reloadSuspicious}>
                <SuspiciousPanel />
            </Panel>
            <Panel title={SpotConstants.NETVIEW_PANEL} container expandable reloadable
                   onReload={EdInActions.reloadSuspicious}>
                <NetworkViewPanel className="proxy-force" />
            </Panel>
        </PanelRow>
        <div className="sortable">
          <PanelRow title={SpotConstants.SCORING_PANEL}>
            <Panel title={SpotConstants.SCORING_PANEL} reloadable switchable onReload={EdInActions.reloadSuspicious} onSwitch={switchComponents} className="col-md-12">
              <ScoreNotebook />
            </Panel>
          </PanelRow>
          <PanelRow title={SpotConstants.DETAILS_PANEL}>
            <Panel title={SpotConstants.DETAILS_PANEL} container switchable expandable onSwitch={switchComponents} className="col-md-12">
              <DetailsPanel title={SpotConstants.DETAILS_PANEL} />
            </Panel>
        </PanelRow>
        </div>
    </div>,
    document.getElementById('spot-content-wrapper')
);

const initial_filter = SpotUtils.getCurrentFilter();

// Set search criteria
SpotActions.setDate(SpotUtils.getCurrentDate());
initial_filter && EdInActions.setFilter(initial_filter);

// Load data
EdInActions.reloadSuspicious();

// Create a hook to allow notebook iframe to reloadSuspicious
window.iframeReloadHook = EdInActions.reloadSuspicious;

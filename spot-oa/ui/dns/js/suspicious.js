const React = require('react');
const ReactDOM = require('react-dom');

const SpotConstants = require('../../js/constants/SpotConstants');
const SpotActions = require('../../js/actions/SpotActions');
const EdInActions = require('../../js/actions/EdInActions');
const SpotUtils = require('../../js/utils/SpotUtils');

// Build and Render Toolbar
const FilterInput = require('./components/FilterInput.react');
const DateInput = require('../../js/components/DateInput.react');

function resetFilterAndReload()
{
  EdInActions.setFilter('');
  EdInActions.reloadSuspicious();
};

ReactDOM.render(
  (
    <form className="form-inline">
      <div className="form-group">
        <label htmlFor="ip_filter" className="control-label">IP/DNS:</label>
        <div className="input-group input-group-xs">
          <FilterInput id="ip_filter" />
          <div className="input-group-btn">
            <button className="btn btn-primary" type="button" id="btn_searchIp" title="Enter an IP Address or Domain and click the search button to filter the results." onClick={EdInActions.reloadSuspicious}>
              <span className="glyphicon glyphicon-search" aria-hidden="true"></span>
            </button>
          </div>
        </div>
      </div>
      <div className="form-group">
        <label htmlFor="dataDatePicker" className="control-label">Data Date:</label>
        <div className="input-group input-group-xs">
          <DateInput id="dataDatePicker" onChange={resetFilterAndReload} />
          <div className="input-group-btn">
            <button className="btn btn-default" type="button" title="Reset filter" id="reset_ip_filter" onClick={resetFilterAndReload}>
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
const IPythonNotebookPanel = require('../../js/components/IPythonNotebookPanel.react');
const DetailsPanel = require('./components/DetailsPanel.react');

const ipynbClosure = IPythonNotebookPanel.createIPythonNotebookClosure(SpotConstants.NOTEBOOK_PANEL);

ReactDOM.render(
  <div id="spot-content">
    <PanelRow>
      <Panel title={SpotConstants.SUSPICIOUS_PANEL} expandable reloadable onReload={EdInActions.reloadSuspicious}>
        <SuspiciousPanel />
      </Panel>
      <Panel title={SpotConstants.NETVIEW_PANEL} container expandable reloadable onReload={EdInActions.reloadSuspicious}>
        <NetworkViewPanel />
      </Panel>
    </PanelRow>
    <PanelRow>
      <Panel title={ipynbClosure.getTitle()} container extraButtons={ipynbClosure.getButtons}>
        <IPythonNotebookPanel title={ipynbClosure.getTitle()} date={SpotUtils.getCurrentDate()} ipynb="dns/${date}/Edge_Investigation.ipynb" />
      </Panel>
      <Panel title={SpotConstants.DETAILS_PANEL} container expandable>
        <DetailsPanel title={SpotConstants.DETAILS_PANEL} />
      </Panel>
    </PanelRow>
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

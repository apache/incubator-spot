var React = require('react');

var OniConstants = require('../../js/constants/OniConstants');
var OniActions = require('../../js/actions/OniActions');
var EdInActions = require('../../js/actions/EdInActions');
var OniUtils = require('../../js/utils/OniUtils');

// Build and Render Toolbar
var FilterInput = require('./components/FilterInput.react');
var DateInput = require('../../js/components/DateInput.react');

function resetFilterAndReload() {
    EdInActions.setFilter('');
    EdInActions.reloadSuspicious();
};

React.render(
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
var PanelRow = require('../../js/components/PanelRow.react');
var Panel = require('../../js/components/Panel.react');

var SuspiciousPanel = require('./components/SuspiciousPanel.react');
var NetworkViewPanel = require('./components/NetworkViewPanel.react');
var IPythonNotebookPanel = require('../../js/components/IPythonNotebookPanel.react');
var DetailsPanel = require('./components/DetailsPanel.react');

var ipynbClosure = IPythonNotebookPanel.createIPythonNotebookClosure(OniConstants.NOTEBOOK_PANEL);

React.render(
    <div id="oni-content">
        <PanelRow>
            <Panel title={OniConstants.SUSPICIOUS_PANEL} expandable reloadable onReload={EdInActions.reloadSuspicious}>
                <SuspiciousPanel />
            </Panel>
            <Panel title={OniConstants.NETVIEW_PANEL} container expandable reloadable
                   onReload={EdInActions.reloadSuspicious}>
                <NetworkViewPanel />
            </Panel>
        </PanelRow>
        <PanelRow>
            <Panel title={ipynbClosure.getTitle()} container  extraButtons={ipynbClosure.getButtons}>
                <IPythonNotebookPanel title={ipynbClosure.getTitle()} date={OniUtils.getCurrentDate()} ipynb="proxy/${date}/Edge_Investigation.ipynb" />
            </Panel>
            <Panel title={OniConstants.DETAILS_PANEL} expandable>
                <DetailsPanel />
            </Panel>
        </PanelRow>
    </div>,
    document.getElementById('oni-content-wrapper')
);

var initial_filter = OniUtils.getCurrentFilter();

// Set search criteria
OniActions.setDate(OniUtils.getCurrentDate());
initial_filter && EdInActions.setFilter(initial_filter);

// Load data
EdInActions.reloadSuspicious();

// Create a hook to allow notebook iframe to reloadSuspicious
window.iframeReloadHook = EdInActions.reloadSuspicious;

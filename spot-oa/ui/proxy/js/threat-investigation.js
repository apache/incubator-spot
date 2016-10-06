var React = require('react');

var SpotActions = require('../../js/actions/SpotActions');
var SpotUtils = require('../../js/utils/SpotUtils');

var DateInput = require('../../js/components/DateInput.react');

React.render(
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

var PanelRow = require('../../js/components/PanelRow.react');
var Panel = require('../../js/components/Panel.react');
var IPythonNotebookPanel = require('../../js/components/IPythonNotebookPanel.react');

var ipynbClosure = IPythonNotebookPanel.createIPythonNotebookClosure('');

React.render(
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
var date;

date = SpotUtils.getCurrentDate();

SpotActions.setDate(date);

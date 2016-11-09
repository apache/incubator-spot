const React = require('react');
const ReactDOM = require('react-dom');

const SpotActions = require('../../js/actions/SpotActions');
const SpotUtils = require('../../js/utils/SpotUtils');

const DateInput = require('../../js/components/DateInput.react');

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
        <IPythonNotebookPanel title={ipynbClosure.getTitle()} date={SpotUtils.getCurrentDate()} ipynb="dns/${date}/Threat_Investigation.ipynb" />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);
// Set search criteria
const date = SpotUtils.getCurrentDate();

SpotActions.setDate(date);

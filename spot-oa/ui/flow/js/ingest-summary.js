const React = require('react');
const ReactDOM = require('react-dom');

const SpotActions = require('../../js/actions/SpotActions');
const InSumActions = require('./actions/InSumActions');
const NetflowConstants = require('./constants/NetflowConstants');
const SpotUtils = require('../../js/utils/SpotUtils');
const DateUtils = require('../../js/utils/DateUtils');

// Build and Render Toolbar
const DateInput = require('../../js/components/DateInput.react');

// Find out period
var startDate, endDate, today;

today = new Date();
startDate = SpotUtils.getUrlParam(NetflowConstants.START_DATE);
endDate = SpotUtils.getUrlParam(NetflowConstants.END_DATE);

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

ReactDOM.render(
  (
    <form className="form-inline">
      <div className="form-group">
        <label htmlFor="startDatePicker">Period:</label>
        <div className="input-group input-group-xs">
          <div className="input-group-addon">
            <span className="glyphicon glyphicon-calendar" aria-hidden="true"></span>
          </div>
          <DateInput id="startDatePicker" name={NetflowConstants.START_DATE} value={startDate}/>
        </div>
      </div>
      <div className="form-group">
        <label htmlFor="endDatePicker"> - </label>
        <div className="input-group input-group-xs">
          <DateInput id="endDatePicker" name={NetflowConstants.END_DATE} value={endDate} />
          <div className="input-group-btn">
            <button className="btn btn-default" type="button" title="Reload" onClick={InSumActions.reloadSummary}>
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
//
const IngestSummaryPanel = require('./components/IngestSummaryPanel.react');

ReactDOM.render(
  <div id="spot-content">
    <PanelRow maximized>
      <Panel title="Ingest Summary" container header={false} className="col-md-12">
        <IngestSummaryPanel id="spot-is" />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('spot-content-wrapper')
);

// Set period
SpotActions.setDate(startDate, NetflowConstants.START_DATE);
SpotActions.setDate(endDate, NetflowConstants.END_DATE);

// Load data
InSumActions.reloadSummary();

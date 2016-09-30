var React = require('react');

var OniActions = require('../../js/actions/OniActions');
var InSumActions = require('./actions/InSumActions');
var NetflowConstants = require('./constants/NetflowConstants');
var OniUtils = require('../../js/utils/OniUtils');
var DateUtils = require('../../js/utils/DateUtils');

// Build and Render Toolbar
var DateInput = require('../../js/components/DateInput.react');

// Find out period
var startDate, endDate, today;

today = new Date();
startDate = OniUtils.getUrlParam(NetflowConstants.START_DATE);
endDate = OniUtils.getUrlParam(NetflowConstants.END_DATE);

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

React.render(
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
var PanelRow = require('../../js/components/PanelRow.react');
var Panel = require('../../js/components/Panel.react');
//
var IngestSummaryPanel = require('./components/IngestSummaryPanel.react');

React.render(
  <div id="oni-content">
    <PanelRow maximized>
      <Panel title="Ingest Summary" container header={false} className="col-md-12">
        <IngestSummaryPanel id="oni-is" />
      </Panel>
    </PanelRow>
  </div>,
  document.getElementById('oni-content-wrapper')
);

// Set period
OniActions.setDate(startDate, NetflowConstants.START_DATE);
OniActions.setDate(endDate, NetflowConstants.END_DATE);

// Load data
InSumActions.reloadSummary();

var assign = require('object-assign');
var d3 = require('d3');

var OniDispatcher = require('../../../js/dispatchers/OniDispatcher');
var OniConstants = require('../../../js/constants/OniConstants');
var NetflowConstants = require('../constants/NetflowConstants');
var DateUtils = require('../../../js/utils/DateUtils');
var RestStore = require('../../../js/stores/RestStore');

var START_DATE_FILTER = NetflowConstants.START_DATE;
var END_DATE_FILTER = NetflowConstants.END_DATE;
var CURRENT_DATE_FILTER = 'current_date';

var requestQueue = [];
var requestErrors = [];

var IngestSummaryStore = assign(new RestStore(NetflowConstants.API_INGEST_SUMMARY), {
    errorMessages: {
        404: 'No details available'
    },
    setStartDate: function (date) {
        this.setRestFilter(START_DATE_FILTER, date);
    },
    getStartDate: function () {
        return this.getRestFilter(START_DATE_FILTER);
    },
    setEndDate: function (date) {
        this.setRestFilter(END_DATE_FILTER, date);
    },
    getEndDate: function () {
        return this.getRestFilter(END_DATE_FILTER);
    },
    /**
     *  Start asking the server for CSV data to create the chart
     **/
    requestSummary: function () {
        var startDate, endDate, date, delta, startRequests, i, month;

        startDate = DateUtils.parseDate(this.getRestFilter(START_DATE_FILTER));
        endDate = DateUtils.parseDate(this.getRestFilter(END_DATE_FILTER));

        // Find out how many request need to be made
        delta = (endDate.getFullYear() - startDate.getFullYear()) * 12 + (endDate.getMonth() - startDate.getMonth());

        startRequests = requestQueue.length == 0;

        // Go to first day in month
        date = new Date(startDate);
        date.setDate(1);

        // Queue date requests
        requestQueue.push(date);
        for (i = 1; i <= delta; i++) {
            requestQueue.push(DateUtils.calcDate(date, i, 'month'));
        }

        // dequeue is no request is running
        startRequests && this.dequeue();
    },
    dequeue: function () {
        var date, year, month;

        if (requestQueue.length == 0) return;

        date = requestQueue.shift();
        this.setRestFilter(CURRENT_DATE_FILTER, date);
        year = date.getFullYear();
        month = date.getMonth() + 1 + "";
        month = month.length == 1 ? "0" + month : month;

        this.setEndpoint(NetflowConstants.API_INGEST_SUMMARY.replace('${year}', year).replace('${month}', month));

        this.reload();
    },
    setData: function (data) {
        var startDate, endDate, date, dayFilter, parse;

        // Does the loading indicator needs to be displayed?
        if (data.loading) {
            if (!this._data.loading) {
                this._data = data;
                this.emitChangeData();
            }

            // Do nothing when loading is in progress
            return;
        }

        // Store errors for later usage
        if (data.error) {
            requestErrors.push(data);
        }
        else {
            parse = d3.time.format("%Y-%m-%d %H:%M").parse; // Date formatting parser
            startDate = DateUtils.parseDate(this.getRestFilter(START_DATE_FILTER));
            endDate = DateUtils.parseDate(this.getRestFilter(END_DATE_FILTER));
            date = DateUtils.parseDate(this.getRestFilter(CURRENT_DATE_FILTER));

            if (date.getFullYear() == startDate.getFullYear() && date.getMonth() == startDate.getMonth()) {
                dayFilter = startDate.getDate();
                data.data = data.data.filter(function (row) {
                    return DateUtils.parseDate(row.date, true).getDate() >= dayFilter
                });
            }

            if (date.getFullYear() == endDate.getFullYear() && date.getMonth() == endDate.getMonth()) {
                dayFilter = endDate.getDate();
                data.data = data.data.filter(function (row) {
                    return DateUtils.parseDate(row.date, true).getDate() <= dayFilter
                });
            }

            // Parse dates and numbers.
            data.data.forEach(function (d) {
                d.date = parse(d.date);
                d.flows = +d.flows;
            });

            // Sort the data by date ASC
            data.data.sort(function (a, b) {
                return a.date - b.date;
            });

            this._data.data.push(data.data);
        }

        this._data.loading = requestQueue.length > 0;

        if (!this._data.loading) {
            if (this._data.data.length==0) {
                // Broadcast first found error
                this._data = requestErrors[0];
            }
            this.emitChangeData();
        }
        else {
            setTimeout(this.dequeue.bind(this), 1);
        }
    }
});

OniDispatcher.register(function (action) {
    switch (action.actionType) {
        case OniConstants.UPDATE_DATE:
            switch (action.name) {
                case NetflowConstants.START_DATE:
                    IngestSummaryStore.setStartDate(action.date);
                    break;
                case NetflowConstants.END_DATE:
                    IngestSummaryStore.setEndDate(action.date);
                    break;
            }
            break;
        case NetflowConstants.RELOAD_INGEST_SUMMARY:
            IngestSummaryStore.requestSummary();
            break;
    }
});

module.exports = IngestSummaryStore;

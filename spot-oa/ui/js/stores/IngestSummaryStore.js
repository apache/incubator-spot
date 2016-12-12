const assign = require('object-assign');
const d3 = require('d3');

const SpotDispatcher = require('../dispatchers/SpotDispatcher');
const SpotConstants = require('../constants/SpotConstants');
const DateUtils = require('../utils/DateUtils');
const RestStore = require('../stores/RestStore');

const PIPELINE_FILTER = 'pipeline';
const CURRENT_YEAR_FILTER = 'year';
const CURRENT_MONTH_FILTER = 'month';

const requestQueue = [];
const requestErrors = [];

const IngestSummaryStore = assign(new RestStore(SpotConstants.API_INGEST_SUMMARY), {
    PIPELINES: {
        [SpotConstants.PIPELINE_NETFLOW]: 'Netflow',
        [SpotConstants.PIPELINE_DNS]: 'Dns',
        [SpotConstants.PIPELINE_PROXY]: 'Proxy'
    },
    errorMessages: {
        404: 'No details available'
    },
    setStartDate(date) {
        this._startDate = date;
    },
    getStartDate() {
        return this._startDate;
    },
    setEndDate(date) {
        this._endDate = date;
    },
    getEndDate() {
        return this._endDate;
    },
    setPipeline(pipeline) {
        this.setRestFilter(PIPELINE_FILTER, pipeline);
    },
    getPipeline() {
        return this.getRestFilter(PIPELINE_FILTER);
    },
    setCurrentDate(date) {
        this.setRestFilter(CURRENT_YEAR_FILTER, date.getFullYear())

        const month = date.getMonth() + 1 + "";
        this.setRestFilter(CURRENT_MONTH_FILTER, month.length==1 ? `0${month}`:month);

        this._currentDate = date;
    },
    getCurrentDate() {
        return this._currentDate;
    },
    /**
     *  Start asking the server for CSV data to create the chart
     **/
    requestSummary: function () {
        var startDate, endDate, date, delta, startRequests, i, month;

        startDate = DateUtils.parseDate(this.getStartDate());
        endDate = DateUtils.parseDate(this.getEndDate());

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
        if (requestQueue.length == 0) return;

        const date = requestQueue.shift();
        this.setCurrentDate(date);

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
        else if (data.data) {
            parse = d3.time.format("%Y-%m-%d %H:%M:%S%Z").parse; // Date formatting parser
            startDate = DateUtils.parseDate(this.getStartDate());
            endDate = DateUtils.parseDate(this.getEndDate());
            date = DateUtils.parseDate(this.getCurrentDate());

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
                d.date = parse(`${d.date}:00-0000`);
                d.total = +d.total;
            });

            // Sort the data by date ASC
            data.data.sort(function (a, b) {
                return a.date - b.date;
            });

            if (!this._data.data) this._data.data = [];

            this._data.data.push(data.data);
        }

        this._data.loading = requestQueue.length > 0;

        if (!this._data.loading) {
            if (this._data.data && this._data.data.length==0) {
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

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            switch (action.name) {
                case SpotConstants.START_DATE:
                    IngestSummaryStore.setStartDate(action.date);
                    break;
                case SpotConstants.END_DATE:
                    IngestSummaryStore.setEndDate(action.date);
                    break;
            }
            break;
        case SpotConstants.RELOAD_INGEST_SUMMARY:
            IngestSummaryStore.requestSummary();
            break;
    }
});

module.exports = IngestSummaryStore;

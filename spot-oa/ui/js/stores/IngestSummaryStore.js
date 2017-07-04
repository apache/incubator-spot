// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../dispatchers/SpotDispatcher');
const SpotConstants = require('../constants/SpotConstants');

const ObservableGraphQLStore = require('./ObservableGraphQLStore');

const START_DATE_VAR = 'startDate';
const END_DATE_VAR = 'endDate';

class IngestSummaryStore extends ObservableGraphQLStore {
    constructor() {
        super();

        this.pipeline = null;
        this.PIPELINES = {};
        this.PIPELINES[SpotConstants.PIPELINE_NETFLOW] = 'Netflow';
        this.PIPELINES[SpotConstants.PIPELINE_DNS] = 'Dns';
        this.PIPELINES[SpotConstants.PIPELINE_PROXY] = 'Proxy';
    }

    getQuery() {
        let pipeline = this.getPipeline() || '';

        return `
            query($startDate:SpotDateType!,$endDate:SpotDateType!) {
                ${pipeline} {
                    ingestSummary(startDate:$startDate, endDate:$endDate) {
                        date: datetime
                        total
                    }
                }
            }
        `;
    }

    unboxData(data) {
        let pipeline = this.getPipeline();
        let parser = parser = d3.time.format("%Y-%m-%d %H:%M:%S%Z").parse;

        let dataByMonth = {};
        data[pipeline].ingestSummary.forEach(record => {
            record = {total: record.total, date: parser(`${record.date}-0000`)};
            let month = record.date.toISOString().substr(0,7);

            if (!(month in dataByMonth)) dataByMonth[month] = [];
            dataByMonth[month].push(record);
        });

        return Object.keys(dataByMonth).map(month => dataByMonth[month].sort((a, b) => a.date - b.date));
    }

    setStartDate(date) {
        this.setVariable(START_DATE_VAR, date);
    }

    getStartDate() {
        return this.getVariable(START_DATE_VAR);
    }

    setEndDate(date) {
        this.setVariable(END_DATE_VAR, date);
    }

    getEndDate() {
        return this.getVariable(END_DATE_VAR);
    }

    setPipeline(pipeline) {
        this.pipeline = pipeline;
    }

    getPipeline() {
        return this.pipeline;
    }
}

const iss = new IngestSummaryStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.UPDATE_DATE:
            switch (action.name) {
                case SpotConstants.START_DATE:
                    iss.setStartDate(action.date);
                    break;
                case SpotConstants.END_DATE:
                    iss.setEndDate(action.date);
                    break;
            }
            break;
        case SpotConstants.RELOAD_INGEST_SUMMARY:
            iss.sendQuery();
            break;
        case SpotConstants.UPDATE_PIPELINE:
            iss.setPipeline(action.pipeline)
        break;
    }
});

module.exports = iss;

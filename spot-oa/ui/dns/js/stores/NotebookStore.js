// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');
const SpotConstants = require('../../../js/constants/SpotConstants');
const SuspiciousStore = require('./SuspiciousStore');

const ObservableWithHeadersGraphQLStore = require('../../../js/stores/ObservableWithHeadersGraphQLStore');

const SCORED_ELEMENTS = 'input';
const RESET_SCORED_ELEMENTS = 'date';

class NotebookStore extends ObservableWithHeadersGraphQLStore {
    constructor() {
        super();
        this.selectedQuery = 'scoreQuery';
        this.query = {
          scoreQuery: `
          mutation($input:[DnsScoreType!]!) {
            dns{
              score(input:$input)
                  {success}
            }
          }`,
          resetQuery: `
          mutation($date:SpotDateType!) {
                  dns{
                      resetScoredConnections(date:$date){
                      success
                  }
              }
          }`
        };
        this.completeClass = false;
    }

    getQuery() {
        return this.query[this.selectedQuery];
    }

    unboxData(data) {
        return data.dns.suspicious;
    }

    setScoredElements(scoredElements) {
        this.selectedQuery = 'scoreQuery';
        this.setVariable(SCORED_ELEMENTS, scoredElements);
        this.unsetVariables(RESET_SCORED_ELEMENTS);
    }

    resetScoredElements(date) {
        this.selectedQuery = 'resetQuery';
        this.setVariable(RESET_SCORED_ELEMENTS, date);
        this.unsetVariables(SCORED_ELEMENTS);
    }

    unsetVariables(variable) {
        this.unsetVariable(variable);
    }

    reloadElements() {
      SuspiciousStore.sendQuery();
    }

    changeCssCls() {
      this.completeClass = !this.completeClass;
    }


}

const ns = new NotebookStore();

SpotDispatcher.register(function (action) {
    switch (action.actionType) {
        case SpotConstants.SAVE_SCORED_ELEMENTS:
            ns.setScoredElements(action.scoredElems);
            ns.sendQuery();
            break;
        case SpotConstants.RESET_SCORED_ELEMENTS:
            ns.resetScoredElements(action.date);
            ns.sendQuery();
            break;
        case SpotConstants.RELOAD_SUSPICIOUS:
            ns.resetData();
            break;
        case SpotConstants.CHANGE_CSS_CLS:
            ns.changeCssCls();
            break;
    }
});

module.exports = ns;

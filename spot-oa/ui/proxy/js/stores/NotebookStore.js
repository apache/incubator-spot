//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

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
          mutation($input:[ProxyScoreInputType!]!) {
            proxy{
              score(input:$input)
                  {success}
            }
          }`,
          resetQuery: `
          mutation($date:SpotDateType!) {
                  proxy{
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
        return data.flow.suspicious;
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

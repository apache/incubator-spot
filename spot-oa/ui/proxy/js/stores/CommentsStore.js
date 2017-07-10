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

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';

class CommentsStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType!) {
                proxy {
                    threats {
                        comments(date:$date) {
                            uri
                            title
                            summary: text
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.proxy.threats.comments;
    }

    setDate(date) {
        this.setVariable(DATE_VAR, date);
    }
}

const cs = new CommentsStore();

SpotDispatcher.register(function (action) {
  switch (action.actionType) {
    case SpotConstants.UPDATE_DATE:
      cs.setDate(action.date);
      break;
    case SpotConstants.RELOAD_COMMENTS:
      cs.sendQuery();
      break;
  }
});

module.exports = cs;

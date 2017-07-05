// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotConstants = require('../../../js/constants/SpotConstants');
const SpotDispatcher = require('../../../js/dispatchers/SpotDispatcher');

const ObservableGraphQLStore = require('../../../js/stores/ObservableGraphQLStore');

const DATE_VAR = 'date';

class CommentsStore extends ObservableGraphQLStore {
    getQuery() {
        return `
            query($date:SpotDateType) {
                flow {
                    threats {
                        comments(date: $date) {
                            ip
                            title
                            summary: text
                        }
                    }
                }
            }
        `;
    }

    unboxData(data) {
        return data.flow.threats.comments;
    }

    setDate (date) {
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

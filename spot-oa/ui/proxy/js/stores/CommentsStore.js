// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

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

// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

const SpotDispatcher = require('../dispatchers/SpotDispatcher');
const SpotConstants = require('../constants/SpotConstants');

const InSumActions = {
  reloadSummary: function () {
    SpotDispatcher.dispatch({
      actionType: SpotConstants.RELOAD_INGEST_SUMMARY
    });
  }
};

module.exports = InSumActions;

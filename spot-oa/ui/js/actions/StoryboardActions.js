// Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0.

var SpotDispatcher = require('../dispatchers/SpotDispatcher');
var SpotConstants = require('../constants/SpotConstants');

var StoryBoardActions = {
    reloadComments: function ()
    {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.RELOAD_COMMENTS
        });
    },
    selectComment: function (comment)
    {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.SELECT_COMMENT,
            comment: comment
        });
    }
};

module.exports = StoryBoardActions;

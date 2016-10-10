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

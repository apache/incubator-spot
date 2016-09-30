var OniDispatcher = require('../dispatchers/OniDispatcher');
var OniConstants = require('../constants/OniConstants');

var StoryBoardActions = {
    reloadComments: function ()
    {
        OniDispatcher.dispatch({
            actionType: OniConstants.RELOAD_COMMENTS
        });
    },
    selectComment: function (comment)
    {
        OniDispatcher.dispatch({
            actionType: OniConstants.SELECT_COMMENT,
            comment: comment
        });
    }
};

module.exports = StoryBoardActions;

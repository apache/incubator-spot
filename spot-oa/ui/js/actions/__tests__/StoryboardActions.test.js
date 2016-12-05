jest.mock('../../dispatchers/SpotDispatcher');

const SpotConstants = require('../../constants/SpotConstants');
const SpotDispatcher = require('../../dispatchers/SpotDispatcher');
const StoryboardActions = require('../StoryboardActions');

test('reloadComments', () => {
    SpotDispatcher.dispatch.mockClear();

    StoryboardActions.reloadComments();

    expect(SpotDispatcher.dispatch).toHaveBeenCalled();
    expect(SpotDispatcher.dispatch.mock.calls[0][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].actionType).toBe(SpotConstants.RELOAD_COMMENTS);
});

test('selectComment', () => {
    SpotDispatcher.dispatch.mockClear();

    StoryboardActions.selectComment('aComment');

    expect(SpotDispatcher.dispatch).toHaveBeenCalled();
    expect(SpotDispatcher.dispatch.mock.calls[0][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].actionType).toBe(SpotConstants.SELECT_COMMENT);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].comment).toBe('aComment');
});

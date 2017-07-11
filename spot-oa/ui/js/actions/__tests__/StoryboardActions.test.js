// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License


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

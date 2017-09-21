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
jest.mock('../../utils/SpotUtils');

const $ = require('jquery');
const SpotActions = require('../SpotActions');
const SpotConstants = require('../../constants/SpotConstants');
const SpotDispatcher = require('../../dispatchers/SpotDispatcher');
const SpotUtils = require('../../utils/SpotUtils');

test('setDate (default)', () => {
    SpotDispatcher.dispatch.mockClear();

    const link = $('<a />').attr('data-href', '/foo/bar#date=${date}').appendTo($('body'));

    SpotActions.setDate('1985-01-12');

    expect(SpotUtils.setUrlParam).toHaveBeenCalled();
    expect(SpotUtils.setUrlParam.mock.calls[0][0]).toBe('date');
    expect(SpotUtils.setUrlParam.mock.calls[0][1]).toBe('1985-01-12');

    expect(SpotDispatcher.dispatch).toHaveBeenCalledTimes(2);
    expect(SpotDispatcher.dispatch.mock.calls[0][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].actionType).toBe(SpotConstants.UPDATE_FILTER);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].filter).toBe('');

    expect(SpotDispatcher.dispatch.mock.calls[1][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[1][0].actionType).toBe(SpotConstants.UPDATE_DATE);
    expect(SpotDispatcher.dispatch.mock.calls[1][0].date).toBe('1985-01-12');
    expect(SpotDispatcher.dispatch.mock.calls[1][0].name).toBe('date');

    expect(link.attr('href')).toBe('/foo/bar#date=1985-01-12');
});

test('setDate (custom name)', () => {
    SpotDispatcher.dispatch.mockClear();
    SpotUtils.setUrlParam.mockClear();

    const link = $('<a />').attr('data-href', '/foo/bar#start_date=${start_date}').appendTo($('body'));

    SpotActions.setDate('1985-01-12', 'start_date');

    expect(SpotUtils.setUrlParam).toHaveBeenCalled();
    expect(SpotUtils.setUrlParam.mock.calls[0][0]).toBe('start_date');
    expect(SpotUtils.setUrlParam.mock.calls[0][1]).toBe('1985-01-12');

    expect(SpotDispatcher.dispatch).toHaveBeenCalledTimes(2);
    expect(SpotDispatcher.dispatch.mock.calls[1][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[1][0].actionType).toBe(SpotConstants.UPDATE_DATE);
    expect(SpotDispatcher.dispatch.mock.calls[1][0].date).toBe('1985-01-12');
    expect(SpotDispatcher.dispatch.mock.calls[1][0].name).toBe('start_date');

    expect(link.attr('href')).toBe('/foo/bar#start_date=1985-01-12');
});

test('expandPanel', () => {
    SpotDispatcher.dispatch.mockClear();

    SpotActions.expandPanel('aPanel');

    expect(SpotDispatcher.dispatch).toHaveBeenCalled();
    expect(SpotDispatcher.dispatch.mock.calls[0][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].actionType).toBe(SpotConstants.EXPAND_PANEL);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].panel).toBe('aPanel');
});

test('restorePanel', () => {
    SpotDispatcher.dispatch.mockClear();

    SpotActions.restorePanel('aPanel');

    expect(SpotDispatcher.dispatch).toHaveBeenCalled();
    expect(SpotDispatcher.dispatch.mock.calls[0][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].actionType).toBe(SpotConstants.RESTORE_PANEL);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].panel).toBe('aPanel');
});

test('toggleMode', () => {
    SpotDispatcher.dispatch.mockClear();

    SpotActions.toggleMode('aPanel', 'aMode');

    expect(SpotDispatcher.dispatch).toHaveBeenCalled();
    expect(SpotDispatcher.dispatch.mock.calls[0][0]).toBeInstanceOf(Object);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].actionType).toBe(SpotConstants.TOGGLE_MODE_PANEL);
    expect(SpotDispatcher.dispatch.mock.calls[0][0].panel).toBe('aPanel');
    expect(SpotDispatcher.dispatch.mock.calls[0][0].mode).toBe('aMode');
});

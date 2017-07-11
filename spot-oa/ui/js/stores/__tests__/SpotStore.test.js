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


const SpotStore = require('../SpotStore');

test('Date handlers are working OK', () => {
    const handler = jest.fn();

    // MAke sure handlers are being called
    SpotStore.addChangeDateListener(handler);

    SpotStore.setDate('1985-01-12');
    expect(SpotStore.getDate()).toBe('1985-01-12');
    expect(handler).toHaveBeenCalledTimes(1);

    // Make sure removed handlers are not being called anymore
    SpotStore.removeChangeDateListener(handler);

    SpotStore.setDate('1985-01-12');
    expect(handler).toHaveBeenCalledTimes(1);
});

test('PanelExpand handlers are working OK', () => {
    const handler = jest.fn();

    // MAke sure handlers are being called
    SpotStore.addPanelExpandListener(handler);

    SpotStore.emitPanelExpand('Foo Panel');

    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenCalledWith('Foo Panel');

    // Make sure removed handlers are not being called anymore

    SpotStore.removePanelExpandListener(handler);

    SpotStore.emitPanelExpand('Foo Panel');
    expect(handler).toHaveBeenCalledTimes(1);
});

test('PanelRestore handlers are working OK', () => {
    const handler = jest.fn();

    // MAke sure handlers are being called
    SpotStore.addPanelRestoreListener(handler);

    SpotStore.emitPanelRestore('Foo Panel');

    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenCalledWith('Foo Panel');

    // Make sure removed handlers are not being called anymore

    SpotStore.removePanelRestoreListener(handler);

    SpotStore.emitPanelRestore('Foo Panel');
    expect(handler).toHaveBeenCalledTimes(1);
});

test('ToggleMode handlers are working OK', () => {
    const handler = jest.fn();

    // MAke sure handlers are being called
    SpotStore.addPanelToggleModeListener(handler);

    SpotStore.emitPanelToggleMode('Foo Panel', 'Unit Test Mode');

    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenCalledWith('Foo Panel', 'Unit Test Mode');

    // Make sure removed handlers are not being called anymore

    SpotStore.removePanelToggleModeListener(handler);

    SpotStore.emitPanelToggleMode('Foo Panel', 'Unit Test Mode');
    expect(handler).toHaveBeenCalledTimes(1);
});

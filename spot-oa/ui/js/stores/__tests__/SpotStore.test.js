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

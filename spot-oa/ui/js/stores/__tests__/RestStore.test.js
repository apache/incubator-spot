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


jest.mock('jquery');
const $ = require('jquery');

const Store = require('../RestStore');

test('Create instance. Empty data', () => {
    const instance = new Store('/my-endpoint');

    expect(instance.endpoint).toBe('/my-endpoint');

    const storeState = instance.getData();
    expect(storeState).toBeInstanceOf(Object);
    expect(storeState.loading).toBe(false);
    expect(storeState.headers).toBeUndefined();
    expect(storeState.data).toBeUndefined();
    expect(instance.defaultErrorMessage).toBe('Oops, something went wrong!!');
    expect(instance.errorMessages).toBeInstanceOf(Object);
    expect(Object.keys(instance.errorMessages).length).toBe(0);
    expect(instance.headers).toBeInstanceOf(Object);
    expect(Object.keys(instance.headers).length).toBe(0);
});

test('Create instance. Default to loading', () => {
    const instance = new Store('/my-endpoint', {
        loading: true
    });

    expect(instance.endpoint).toBe('/my-endpoint');

    const storeState = instance.getData();
    expect(storeState).toBeInstanceOf(Object);
    expect(storeState.loading).toBe(true);
    expect(storeState.headers).toBeUndefined();
    expect(storeState.data).toBeUndefined();
});

test('Create instance. Default to known data', () => {
    const instance = new Store('/my-endpoint', {
        headers: ['First', 'Second', 'Third'],
        data: [1 ,2, 3]
    });

    expect(instance.endpoint).toBe('/my-endpoint');

    const storeState = instance.getData();
    expect(storeState).toBeInstanceOf(Object);
    expect(storeState.loading).toBe(false);
    expect(storeState.headers).toBeInstanceOf(Object);
    expect(Object.keys(storeState.headers).length).toBe(3);
    expect(storeState.data).toBeInstanceOf(Array);
    expect(storeState.data.length).toBe(3);
});

test('Filters', () => {
    const instance = new Store('/my-endpoint');

    instance.setRestFilter('foo', 'bar');

    expect(instance.getRestFilter('foo')).toBe('bar');

    instance.removeRestFilter('foo');

    expect(instance.getRestFilter('foo')).toBeUndefined();
});

test('Endpoint', () => {
    const instance = new Store('/my-endpoint');

    instance.setEndpoint('/new-endpoint.json');

    expect(instance.endpoint).toBe('/new-endpoint.json');
});

test('Data', () => {
    const instance = new Store('/my-endpoint');

    instance.setData({
        loading: true,
        headers: {
            header1: 'Header 1',
            header2: 'Header 2'
        },
        data: [
            {header1: 'val1.1', header2: 'val1.2'},
            {header1: 'val2.1', header2: 'val2.2'}
        ]
    });

    let storeState = instance.getData();
    expect(storeState.loading).toBe(true);
    expect(Object.keys(storeState.headers).length).toBe(2);
    expect(storeState.data.length).toBe(2);

    instance.resetData();

    storeState = instance.getData();
    expect(storeState.loading).toBe(false);
    expect(storeState.headers).toBeUndefined();
    expect(storeState.data).toBeUndefined();
});

test('Data listeners', () => {
    const instance = new Store('/my-endpoint');

    const handler = jest.fn();

    instance.addChangeDataListener(handler);

    instance.resetData();
    expect(handler).toHaveBeenCalledTimes(1);

    instance.setData('Some data');
    expect(handler).toHaveBeenCalledTimes(2);

    instance.removeChangeDataListener(handler);

    instance.setData('More data');
    expect(handler).toHaveBeenCalledTimes(2);
});

test('Reload data (Success)', () => {
    const instance = new Store('/${date}/my-endpoint-${id}-${time}');

    const handler = jest.fn();

    instance.addChangeDataListener(handler);

    const okResponse = 'header1,header2,header3\ndata1-1,data1-2,data1-3\ndata2-1,data2-2,data2-3';
    $.ajax = jest.fn((url, options) => {
        options.success.call(options.context, okResponse);
    });

    instance.setRestFilter('date', '20161004');
    instance.setRestFilter('id', 'UUID');
    instance.setRestFilter('time', '22:00');
    instance.reload();

    expect($.ajax).toHaveBeenCalled();
    expect(handler).toHaveBeenCalledTimes(2);

    // Make sure server request is being made correctly
    expect($.ajax.mock.calls[0][0]).toBe('/20161004/my-endpoint-UUID-22_00');
    const reqParams = $.ajax.mock.calls[0][1];
    expect(reqParams.method).toBe('GET');
    expect(reqParams.context).toBe(instance);
    expect(reqParams.contentType).toBe('application/csv');

    // Testing server response
    const dataFromServer = instance.getData();

    expect(dataFromServer.loading).toBe(false);

    expect(dataFromServer.headers).toBeInstanceOf(Object);
    expect(Object.keys(dataFromServer.headers).length).toBe(3);
    expect(dataFromServer.headers.header1).toBe('header1');
    expect(dataFromServer.headers.header2).toBe('header2');
    expect(dataFromServer.headers.header3).toBe('header3');

    expect(dataFromServer.data).toBeInstanceOf(Array);
    expect(dataFromServer.data.length).toBe(2);

    expect(dataFromServer.data[0].header1).toBe('data1-1');
    expect(dataFromServer.data[0].header2).toBe('data1-2');
    expect(dataFromServer.data[0].header3).toBe('data1-3');

    expect(dataFromServer.data[1].header1).toBe('data2-1');
    expect(dataFromServer.data[1].header2).toBe('data2-2');
    expect(dataFromServer.data[1].header3).toBe('data2-3');
});

test('Reload data (Error)', () => {
    const instance = new Store('/${date}/my-endpoint-${id}-${time}');

    const handler = jest.fn();

    instance.addChangeDataListener(handler);

    const error = {status: 500};
    $.ajax = jest.fn((url, options) => {
        options.error.call(options.context, error);
    });

    instance.reload();

    expect(handler).toHaveBeenCalledTimes(2);

    // Testing server error
    const errorFromServer = instance.getData();

    expect(errorFromServer.loading).toBe(false);
    expect(errorFromServer.headers).toBeUndefined();
    expect(errorFromServer.data).toBeUndefined();
    expect(errorFromServer.error).toBe(instance.defaultErrorMessage);
});

test('Reload data (Custom Error)', () => {
    const instance = new Store('/${date}/my-endpoint-${id}-${time}');

    const customError = 'No data has been found';
    instance.errorMessages['404'] = customError;

    const error = {status: 404};
    $.ajax = jest.fn((url, options) => {
        options.error.call(options.context, error);
    });

    instance.reload();

    // Testing server error
    const errorFromServer = instance.getData();

    expect(errorFromServer.error).toBe(customError);
});

test('Reload data (Loading notification)', () => {
    const instance = new Store('/${date}/my-endpoint-${id}-${time}');

    instance.setData = jest.fn();

    const okResponse = 'header1,header2,header3\ndata1-1,data1-2,data1-3\ndata2-1,data2-2,data2-3';
    $.ajax = jest.fn((url, options) => {
        options.success.call(options.context, okResponse);
    });

    instance.reload();

    expect(instance.setData).toHaveBeenCalledTimes(2);
    expect(instance.setData.mock.calls[0][0].loading).toBe(true);
});

const d3 = require('d3');

test('Custom parser', () => {
    const instance = new Store('/custom-parser-endpoint');
    instance._parser = d3.dsv('|', 'text/plain');

    const handler = jest.fn();

    const okResponse = 'header1|header2|header3\ndata1-1|data1-2|data1-3\ndata2-1|data2-2|data2-3';
    $.ajax = jest.fn((url, options) => {
        options.success.call(options.context, okResponse);
    });

    instance.reload();

    expect($.ajax).toHaveBeenCalled();

    // Testing server response
    const dataFromServer = instance.getData();

    expect(dataFromServer.loading).toBe(false);

    expect(dataFromServer.headers).toBeInstanceOf(Object);
    expect(Object.keys(dataFromServer.headers).length).toBe(3);
    expect(dataFromServer.headers.header1).toBe('header1');
    expect(dataFromServer.headers.header2).toBe('header2');
    expect(dataFromServer.headers.header3).toBe('header3');

    expect(dataFromServer.data).toBeInstanceOf(Array);
    expect(dataFromServer.data.length).toBe(2);

    expect(dataFromServer.data[0].header1).toBe('data1-1');
    expect(dataFromServer.data[0].header2).toBe('data1-2');
    expect(dataFromServer.data[0].header3).toBe('data1-3');

    expect(dataFromServer.data[1].header1).toBe('data2-1');
    expect(dataFromServer.data[1].header2).toBe('data2-2');
    expect(dataFromServer.data[1].header3).toBe('data2-3');
});

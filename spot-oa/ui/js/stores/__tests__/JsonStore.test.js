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

const Store = require('../JsonStore');

test('Reload data (Success)', () => {
    const instance = new Store('/${date}/my-endpoint-${id}-${time}');

    const handler = jest.fn();

    instance.addChangeDataListener(handler);

    const okResponse = JSON.stringify({
        headers: {
            header1:"header1",
            header2:"header2",
            header3:"header3"
        },
        data:[
            {
                header1:"data1-1",
                header2:"data1-2",
                header3:"data1-3"
            },
            {
                header1:"data2-1",
                header2:"data2-2",
                header3:"data2-3"
            }
        ]
    });
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
    expect(reqParams.contentType).toBe('application/json');

    // Testing server response
    const dataFromServer = instance.getData();

    expect(dataFromServer).toBeInstanceOf(Object);
    expect(Object.keys(dataFromServer).length).toBe(2);
    expect(dataFromServer.loading).toBe(false);
    expect(dataFromServer.data).toBe(okResponse);
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

    expect(errorFromServer).toBeInstanceOf(Object);
    expect(Object.keys(errorFromServer).length).toBe(2);
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

//
// Licensed to the Apache Software Foundation (ASF) under one or more
// contributor license agreements.  See the NOTICE file distributed with
// this work for additional information regarding copyright ownership.
// The ASF licenses this file to You under the Apache License, Version 2.0
// (the "License"); you may not use this file except in compliance with
// the License.  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

const $ = require('jquery');

const RestStore = require('./RestStore');

const JsonStore = function () {
    RestStore.apply(this, arguments);
};

JsonStore.prototype = new RestStore();

JsonStore.prototype.reload = function ()
{
    var url, name;
    this.setData({loading: true});

    url = this.endpoint;

    for (name in this._filters)
    {
        url = url.replace('${'+name+'}', this._filters[name]);
    }
    url = url.replace(/:/g, '_');

    $.ajax(url, {
        method: 'GET',
        context: this,
        contentType: 'application/json',
        success: function (response) {
            this.setData({
              loading: false,
              data: response
            });
        },
        error: function (response)
        {
            this.setData({
                loading:false,
                error: this.errorMessages[response.status] || this.defaultErrorMessage
            });
        }
    });
};

module.exports = JsonStore;

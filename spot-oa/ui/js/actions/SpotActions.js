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

var $ = require('jquery');

var SpotDispatcher = require('../dispatchers/SpotDispatcher');
var SpotConstants = require('../constants/SpotConstants');
var SpotUtils = require('../utils/SpotUtils');

var SpotActions = {
    setDate: function (date, name) {
        var regex;

        name = name || 'date';
        SpotUtils.setUrlParam(name, date);

        // Update links to match date

        regex = new RegExp('\\${(?:[^}]+\\|)?' + name + '(?:\\|[^}]+)?}', 'g');
        $('a[data-href]').each(function () {
            var link = $(this);

            if (regex.test(link.data('href'))) {
                link.attr('href', link.data('href').replace(regex, date));
            }
        });

        SpotDispatcher.dispatch({
            actionType: SpotConstants.UPDATE_FILTER,
            filter: ''
        });

        SpotDispatcher.dispatch({
            actionType: SpotConstants.UPDATE_DATE,
            date: date,
            name: name
        });
    },
    setPipeline(pipeline) {
       SpotDispatcher.dispatch({
           actionType: SpotConstants.UPDATE_PIPELINE,
           pipeline
       });
   },
    expandPanel: function (panel) {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.EXPAND_PANEL,
            panel: panel
        });
    },
    restorePanel: function (panel) {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.RESTORE_PANEL,
            panel: panel
        });
    },
    toggleMode: function (panel, mode) {
        SpotDispatcher.dispatch({
            actionType: SpotConstants.TOGGLE_MODE_PANEL,
            panel: panel,
            mode: mode
        });
    }
};

module.exports = SpotActions;

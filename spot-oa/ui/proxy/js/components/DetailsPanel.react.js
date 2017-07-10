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

var React = require('react');

var DetailsGridMixin = require('../../../js/components/DetailsGridMixin.react');
var GridPanelMixin = require('../../../js/components/GridPanelMixin.react');
var DetailsStore = require('../stores/DetailsStore');

var DetailsPanel = React.createClass({
    mixins: [GridPanelMixin, DetailsGridMixin],
    store: DetailsStore,
    // Custom cells
    _render_host_cell(host) {
        return (
            <p className="spot-text-wrapper" data-toggle="tooltip">
                {host}
            </p>
        );
    },
    _render_useragent_cell(useragent) {
        return (
            <p className="spot-text-wrapper" data-toggle="tooltip">
                {useragent}
            </p>
        );
    },
    _render_referer_cell(referer) {
        return (
            <p className="spot-text-wrapper" data-toggle="tooltip">
                {referer}
            </p>
        );
    },
    _render_fulluri_cell(fulluri) {
        return (
            <p className="spot-text-wrapper" data-toggle="tooltip">
                {fulluri}
            </p>
        );
    },
    // Hidden cells
    _render_p_time_cell: false,
    _render_duration_cell: false,
    _render_username_cell: false,
    _render_authgroup_cell: false,
    _render_exceptionid_cell: false,
    _render_filterresult_cell: false,
    _render_respcode_cell: false,
    _render_action_cell: false,
    _render_urischeme_cell: false,
    _render_uripath_cell: false,
    _render_uriquery_cell: false,
    _render_uriextension_cell: false,
    _render_virusid_cell: false,
    _render_bcappname_cell: false,
    _render_bcappoper_cell: false,
    _render_sev_cell: false,
    _render_uri_rep_cell: false,
    _render_hash_cell: false
});

module.exports = DetailsPanel;

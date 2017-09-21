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

const React = require('react');

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const PolloNetworkViewMixin = require('../../../js/components/PolloNetworkViewMixin.react');

const SpotActions = require('../../../js/actions/SpotActions');
const EdInActions = require('../../../js/actions/EdInActions');
const SpotConstants = require('../../../js/constants/SpotConstants');

const SuspiciousStore = require('../stores/SuspiciousStore');

const SUSPICIOUS_QUERY_LENGTH = 64;

const ID_SEPARATOR = '-';
const TYPE_SEPARATOR = '::';

function createNodeId(type, id) {
    return `node${ID_SEPARATOR}${type}${TYPE_SEPARATOR}${id}`;
}

function getDataFromNodeId(id) {
    const rawData = id.split(ID_SEPARATOR)[1];
    const data = rawData.split(TYPE_SEPARATOR);

    return {
        type: data[0],
        [data[0]]: data[1]
    };
}

function createLinkId(tld, ip_dst) {
    return `link${ID_SEPARATOR}tld${TYPE_SEPARATOR}${tld}${ID_SEPARATOR}ip_dst${TYPE_SEPARATOR}${ip_dst}`;
}

function getNodesFromData(data) {
    const nodes = {};

    data.forEach((item) => {
        ['tld', 'ip_dst'].forEach((field) => {
            const id = createNodeId(field, item[field]);

            if (!(id in nodes)) {
                nodes[id] = {
                    id: id,
                    label: item[field],
                    internalIp: field==='tld' ? 1 : 0,
                    hits: 1
                };
            }
            else {
                nodes[id].hits++;
            }
        });
    });

    return nodes;
}

function getLinksFromData(data, nodes) {
    const links = {};

    data.forEach((item, idx) => {
        const id = createLinkId(item.tld, item.ip_dst);

        // Items where dns_qry_name is too long, are suspicious according to bussiness rules
        const score = item.dns_qry_name.length>SUSPICIOUS_QUERY_LENGTH ? Number.MAX_VALUE : -Math.log(item.score);

        if (!(id in links)) {
            links[id] = {
                id: id,
                source: nodes[createNodeId('tld', item.tld)],
                target: nodes[createNodeId('ip_dst', item.ip_dst)],
                score
            };
        }
        else {
            links[id].score  = Math.max(links[id].score, score)
        }
    });

    return links;
}

function getStateFromData({data}) {
    const nodes = getNodesFromData(data);
    const links = getLinksFromData(data, nodes);

    return {
        nodes: Object.keys(nodes).map(nodeId => nodes[nodeId]),
        links: Object.keys(links).map(linkId => links[linkId])
    };
}

const NetworkViewPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin, PolloNetworkViewMixin],
    componentDidMount() {
      SuspiciousStore.addChangeDataListener(this._onChange);
      SuspiciousStore.addThreatHighlightListener(this._onHighlight);
      SuspiciousStore.addThreatUnhighlightListener(this._onUnhighlight);
      SuspiciousStore.addThreatSelectListener(this._onSelect);
    },
    componentWillUmount() {
      SuspiciousStore.removeChangeDataListener(this._onChange);
      SuspiciousStore.removeThreatHighlightListener(this._onHighlight);
      SuspiciousStore.removeThreatUnhighlightListener(this._onUnhighlight);
      SuspiciousStore.removeThreatSelectListener(this._onSelect);
    },
    // render is inherited from Mixins
    _onChange() {
        const data = SuspiciousStore.getData();
        const state = {loading: data.loading};

        if (state.loading===false && data.data instanceof Array) {
            state.data = getStateFromData(data);
            state.data.maxNodes = SpotConstants.MAX_SUSPICIOUS_ROWS;
        }

        this.replaceState(state);
    },
    _onHighlight() {
        const threat = SuspiciousStore.getHighlightedThreat();

        this.highlightNodes([createNodeId('tld', threat.tld), createNodeId('ip_dst', threat.ip_dst)]);
        this.highlightEdge(createLinkId(threat.tld, threat.ip_dst));
    },
    _onUnhighlight() {
        this.unhighlight();
    },
    _onSelect() {
        const threat = SuspiciousStore.getSelectedThreat();

        this.selectNodes([createNodeId('tld', threat.tld), createNodeId('ip_dst', threat.ip_dst)]);
        this.selectEdge(createLinkId(threat.tld, threat.ip_dst));
    },
    _onClick(id) {
        const data = getDataFromNodeId(id);

        if (data.type=='tld') return;

        EdInActions.selectIp(data.ip_dst);
        SpotActions.toggleMode(SpotConstants.DETAILS_PANEL, SpotConstants.VISUAL_DETAILS_MODE);
        EdInActions.reloadVisualDetails();
    },
    _onContextualClick(id) {
      d3.event.preventDefault();

      const data = getDataFromNodeId(id);

      EdInActions.setFilter(data.tld || data.ip_dst);
      EdInActions.reloadSuspicious();
    }
});

module.exports = NetworkViewPanel;

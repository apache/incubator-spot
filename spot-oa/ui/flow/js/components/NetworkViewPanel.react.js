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

const ID_SEPARATOR = '-';

function createNodeId(id) {
    return `node${ID_SEPARATOR}${id}`;
}

function getIpFromNodeId(id) {
    return id.split(ID_SEPARATOR)[1];
}

function getLinkSource(sourceIp, destinationIp) {
    return sourceIp<=destinationIp ? sourceIp : destinationIp;
}

function getLinkDestination(sourceIp, destinationIp) {
    return sourceIp>destinationIp ? sourceIp : destinationIp;
}

function createLinkId(sourceIp, destinationIp) {
    const srcId = getLinkSource(sourceIp, destinationIp);
    const dstId = getLinkDestination(sourceIp, destinationIp);

    return `link${ID_SEPARATOR}${srcId}${ID_SEPARATOR}${dstId}`;
}

function getNodesFromData(data) {
    const nodes = {};
    const mapper = {srcIP: 'srcIpInternal', dstIP: 'destIpInternal'};

    data.forEach((item) => {
        ['srcIP', 'dstIP'].forEach((field) => {
            const id = createNodeId(item[field]);

            if (!(id in nodes)) {
                nodes[id] = {
                    id: id,
                    label: item[field],
                    internalIp: item[mapper[field]],
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

    data.forEach((item) => {
        const id = createLinkId(item.srcIP, item.dstIP);
        const score = -Math.log(item.score);

        if (!(id in links)) {
            links[id] = {
                id: id,
                source: nodes[createNodeId(getLinkSource(item.srcIP, item.dstIP))],
                target: nodes[createNodeId(getLinkDestination(item.srcIP, item.dstIP))],
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

        this.highlightNodes([createNodeId(threat.srcIP), createNodeId(threat.dstIP)]);
        this.highlightEdge(createLinkId(threat.srcIP, threat.dstIP));
    },
    _onUnhighlight() {
        this.unhighlight();
    },
    _onSelect() {
        const threat = SuspiciousStore.getSelectedThreat();

        this.selectNodes([createNodeId(threat.srcIP), createNodeId(threat.dstIP)]);
        this.selectEdge(createLinkId(threat.srcIP, threat.dstIP));
    },
    _onClick(id) {
        const ip = getIpFromNodeId(id);
        EdInActions.selectIp(ip);
        SpotActions.toggleMode(SpotConstants.DETAILS_PANEL, SpotConstants.VISUAL_DETAILS_MODE);
        EdInActions.reloadVisualDetails();
    },
    _onContextualClick(id) {
      d3.event.preventDefault();

      const ip = getIpFromNodeId(id);
      EdInActions.setFilter(ip);
      EdInActions.reloadSuspicious();
    }
});

module.exports = NetworkViewPanel;

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
const assign = require('object-assign');
const d3 = require('d3');
const React = require('react');

const EdInActions = require('../../../js/actions/EdInActions');
const SpotConstants = require('../../../js/constants/SpotConstants');
const SpotUtils = require('../../../js/utils/SpotUtils');
const SuspiciousStore = require('../stores/SuspiciousStore');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');

var NetworkViewPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin],
    componentDidMount: function()
    {
        SuspiciousStore.addChangeDataListener(this._onChange);
        SuspiciousStore.addThreatHighlightListener(this._onHighlight);
        SuspiciousStore.addThreatUnhighlightListener(this._onUnhighlight);
        SuspiciousStore.addThreatSelectListener(this._onSelect);
    },
    componentWillUnmount: function ()
    {
        SuspiciousStore.removeChangeDataListener(this._onChange);
        SuspiciousStore.removeThreatHighlightListener(this._onHighlight);
        SuspiciousStore.removeThreatUnhighlightListener(this._onUnhighlight);
        SuspiciousStore.removeThreatSelectListener(this._onSelect);
    },
    buildChart: function () {
        this.state.root.fixed = true;

        this.force = d3.layout.force()
            .charge(d => (d.root?-15:-10)*this.sizeScale(d.size))
            .on('tick', this.tick);

        const svgSel = d3.select(this.svg);

        const zoom = d3.behavior.zoom().on("zoom", this.onZoom);

        svgSel.call(zoom);

        this.canvas = svgSel.append('g');

        this.tip = d3.tip().attr('class', 'd3-tip').html(d => {
                                    var html;

                                    html = '<span class="d3-tip-label"><strong>' + d.type + ':</strong> ' + d.name + '</span>';
                                    if (d.tooltip) html = html + '<br /><br /><p class="d3-tip-message">' + d.tooltip + '</p>';

                                    return html;
                                });

        this.canvas.call(this.tip);

        $(this.svg).width('100%').height('100%');

        this.sizeScale = d3.scale.linear().domain([0, SpotConstants.MAX_SUSPICIOUS_ROWS]);
    },
    draw: function () {
        var nodes = this.flatten(this.state.root),
            links = d3.layout.tree().links(nodes),
            selectedThreat, ids, nodeEnter;

        let width = $(this.svg).width();
        let height = $(this.svg).height();

        // Center root node
        this.state.root.px = width / 2;
        this.state.root.py = height / 2;

        this.force
            .stop()
            .size([width-100, height-100])
            .start();

        // Tooltip margins
        this.tip.box = [height*.4, width*.8, height*.8, width*.4];

        this.sizeScale.range([4.5, width/10]);

        // Restart the force layout
        this.force
            .nodes(nodes)
            .links(links)
            .start();

        selectedThreat = SuspiciousStore.getSelectedThreat();

        ids = selectedThreat ? this._getThreatIdChain(selectedThreat) : [];

        // Update links
        this.link = this.canvas.selectAll('.edge')
            .data(links.filter((link) => link.target.visible), function(d) { return d.source.id + '-' + d.target.id; });

        // Insert new links
        this.link.enter().insert("line", ".node")
                                                .classed('edge', true)
                                                .classed('blink_me', d => ids.indexOf(d.target.id)>-1);

        // Delete old links
        this.link.exit().remove();

        // Update nodesw
        this.node = this.canvas.selectAll('.node, .proxy_node')
            .data(nodes.filter((node) => node.visible), function(d) { return d.id; });

        this.node.transition().select('circle')
            .attr("r", d => {
                return this.sizeScale( d.root || d.expanded ? 0 : d.size );
            });

        nodeEnter = this.node.enter().append('g');

        nodeEnter.filter(node => !node.root)
            .call(this.force.drag)
            .on('mousedown', d => {
                d3.event.stopPropagation();
            })
            .append("circle")
                .attr("r", d => this.sizeScale( d.root || d.expanded ? 0 : d.size ));

        nodeEnter.filter(node => node.root)
                                        .append('text')
                                                .classed('glyphicon', true)
                                                .attr('x', -10)
                                                .attr('y', 10)
                                                .text('\u002a');

        nodeEnter
            .attr('class', d => SpotUtils.CSS_RISK_CLASSES[d.rep])
            .classed('node', d => !d.root)
            .classed('proxy_node', d => d.root)
            .classed('blink_me', d => ids.indexOf(d.id)>-1)
            .classed('leaf', d => !d.children)
            .on("dblclick", this.onNodeDblClick)
            .on("contextmenu", (d, i) => {
                d3.event.preventDefault();

                if (!d.isDataFilter) return;

                this.tip.hide();

                EdInActions.setFilter(d.filter || d.name);
                EdInActions.reloadSuspicious();
            })
            .on("mouseover", d => {
                var direction = '';

                // Where should the tooltip be displayed?

                // Vertically
                if (d3.event.layerY<this.tip.box[0]) {
                    direction = 's';
                }
                else if (d3.event.layerY>this.tip.box[2]) {
                    direction = 'n';
                }

                // Horizontally
                if (d3.event.layerX>this.tip.box[1]) {
                    direction += 'w';
                }
                else if (d3.event.layerX<this.tip.box[3]) {
                    direction += 'e'
                }

                direction = direction || 'n';

                this.tip.direction(direction);
                this.tip.show.call(this, d);
            })
            .on("mouseout", () => {
                this.tip.hide();
            });

        // Delete old nodes
        this.node.exit().remove();
    },
    tick: function () {
        this.link
            .attr("x1", function(d) { return d.source.x; })
            .attr("y1", function(d) { return d.source.y; })
            .attr("x2", function(d) { return d.target.x; })
            .attr("y2", function(d) { return d.target.y; });

        this.node
            .attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
    },
    onZoom: function () {
        var translate, scale;

        translate = d3.event.translate;
        scale = d3.event.scale;

        this.canvas.attr("transform", "translate(" + translate + ")scale(" + scale + ")");
    },
    onNodeDblClick: function (node) {
        if (d3.event.defaultPrevented) return; // ignore drag

        if (!node.children) return;

        d3.event.preventDefault();
        d3.event.stopPropagation();

        node.expanded = !node.expanded;

        return node.root ? this.onRootDblClick(node) : this.onChildNodeDblClick(node);
    },
    // Root node click handler
    onRootDblClick: function (root)
    {
        function recurse(n) {
            if (!n.children) return;

            n.children.forEach(child => {
                // Direct children must remain visible
                child.visible = root==n || root.expanded;
                // Every child must remain collapsed
                child.expanded = root.expanded;

                recurse(child);
            });
        }

        recurse(root);

        this.draw();
    },
    // Root descendant click handler
    onChildNodeDblClick: function (node) {
        function recurse(n) {
            if (!n.children) return;

            n.children.forEach(child => {
                // Only direct children might be visible
                child.visible = node==n && node.expanded;
                // Collapse every node under root
                child.expanded = false;

                recurse(child);
            });
        }

        recurse(node);

        this.draw();
    },
    _onChange: function () {
        // Flatern data. In Store?
        var state, data, refs;

        state = assign({}, SuspiciousStore.getData());

        if (!state.loading && state.data)
        {
            data = {
                id: 'spot_proxy',
                name: 'Proxy',
                type: 'Root',
                tooltip: 'Double click to toggle child nodes',
                rep: -1,
                visible: state.data.length > 0 ? true : false,
                expanded: false,
                root: true
            };

            refs = {};

            state.data.forEach(function (item) {
                var rep, methodKey, hostKey, uriKey, clientKey, obj;

                if (item.host=='-') {
                    console.log('Skipping invalid URL: ' + item.fulluri);
                    return;
                }

                rep = SpotUtils.getHighestReputation(item.uri_rep);
                data.rep = Math.max(data.rep, rep);
                methodKey = item.reqmethod;
                if (refs[methodKey]===undefined) {
                    obj = {id: methodKey, name: item.reqmethod, type: 'Method', rep: rep, visible: true, expanded: false};

                    refs[methodKey] = obj;

                    data.children ? data.children.push(obj) : data.children = [obj];
                }
                else {
                    refs[methodKey].rep = Math.max(refs[methodKey].rep, rep);
                }

                hostKey = methodKey + item.host;
                if (refs[hostKey]===undefined) {
                    obj = {id: hostKey, name: item.host, type: 'Host', rep: rep, visible: false, expanded: false};

                    refs[hostKey] = obj;

                    refs[methodKey].children ? refs[methodKey].children.push(obj) : refs[methodKey].children = [obj];
                }
                else {
                    refs[hostKey].rep = Math.max(refs[hostKey].rep, rep);
                }

                uriKey = hostKey + item.uripath;
                if (refs[uriKey]===undefined) {
                    obj = {
                        id: uriKey,
                        name:  item.uripath,
                        type: 'Path',
                        rep: rep,
                        visible: false,
                        expanded: false,
                        isDataFilter: true,
                        filter: item.fulluri,
                        tooltip: 'Secondary click to use URI as filter'
                    };

                    refs[uriKey] = obj;

                    refs[hostKey].children ? refs[hostKey].children.push(obj) : refs[hostKey].children = [obj];
                }
                else {
                    refs[uriKey].rep = Math.max(refs[uriKey].rep, rep);
                }

                clientKey = uriKey + item.clientip;
                if (refs[clientKey]===undefined) {
                    obj = {
                        id: clientKey,
                        name:  item.clientip,
                        type: 'Ip',
                        rep: rep,
                        visible: false,
                        expanded: false,
                        isDataFilter: true,
                        tooltip: 'Secondary click to use IP as filter'
                    };

                    refs[clientKey] = obj;

                    obj.tooltip = '<strong>URI:</strong> ' + item.fulluri + '<br />';
                    item.useragent && item.useragent!='-' && ('<strong>User Agent: </strong>' + item.useragent + '<br />');
                    item.resconttype && item.resconttype!='-' && (obj.tooltip+= '<strong>MIME type: </strong>' + item.resconttype + '<br />');
                    item.username && item.username!='-' && (obj.tooltip+= '<strong>Username: </strong>' + item.username + '<br />');
                    item.referer && item.referer!='-' && (obj.tooltip+= '<strong>Referer: </strong>' + item.referer + '<br />');
                    item.respcode && item.respcode!='-' && (obj.tooltip+= `<strong>Response code: </strong>${item.respcode_name} (${item.respcode})<br />`);
                    obj.tooltip+= '<strong>SC bytes: </strong>' + item.scbytes + '<br />';
                    obj.tooltip+= '<strong>CS bytes: </strong>' + item.csbytes;

                    refs[uriKey].children ? refs[uriKey].children.push(obj) : refs[uriKey].children = [obj];
                }
                else {
                    refs[clientKey].rep = Math.max(refs[clientKey].rep, rep);
                }

                refs[clientKey].hits ? refs[clientKey].hits.push(item) : refs[clientKey].hits = [obj];
            });

            state.root = data;
        }

        this.replaceState(state);
    },
    flatten: function (root) {
        var nodes = [];

        function recurse(node) {
            if (node.children) {
                node.size = node.children.reduce(
                    function (p, n) {
                        n.parent = node;
                        return p + recurse(n);
                    },
                    0
                );
            } else {
                node.size = node.hits === undefined ? 0 : node.hits.length;
            }

            nodes.push(node);

            return node.size || 1;
        }

        root.size = recurse(root);

        return nodes;
    },
    _getThreatIdChain: function (threat) {
        var id;

        return [
            this.state.root.id,
            id=threat.reqmethod,
            id+=threat.host,
            id+=threat.uripath,
            id+=threat.clientip
        ];
    },
    _onHighlight: function () {
        var threat, ids;

        threat = SuspiciousStore.getHighlightedThreat();

        ids = this._getThreatIdChain(threat);

        d3.selectAll('.edge').filter(n => {
            return ids.indexOf(n.target.id)<0;
        }).classed('faded', true);

        d3.selectAll('.node').filter(n => ids.indexOf(n.id)<0).classed('faded', true);

    },
    _onUnhighlight: function () {
        d3.selectAll('.edge, .node').classed('faded', false);
    },
    _onSelect: function () {
        var threat, ids;

        threat = SuspiciousStore.getSelectedThreat();

        ids = this._getThreatIdChain(threat);

        d3.selectAll('.blink_me').classed('blink_me', false);
        d3.selectAll('.edge').filter(n => {
            return ids.indexOf(n.target.id)>-1;
        }).classed('blink_me', true);

        d3.selectAll('.node').filter(n => ids.indexOf(n.id)>-1).classed('blink_me', true);
    }
});

module.exports = NetworkViewPanel;

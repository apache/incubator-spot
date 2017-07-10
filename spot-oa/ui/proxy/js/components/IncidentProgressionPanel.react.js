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
const d3 = require('d3');
require('d3-tip')(d3);
const React = require('react');
const ReactDOM = require('react-dom');

const CategoryLayout = require('../../../js/utils/CategoryLayout');
const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');
const IncidentProgressionStore = require('../stores/IncidentProgressionStore');

const LEGEND_HEIGHT = 50;
const TRANSITION_DURATION  = 3000;
const NODE_RADIOUS = 10;

const IncidentProgressionPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin],
    componentDidMount: function () {
        IncidentProgressionStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function () {
        IncidentProgressionStore.removeChangeDataListener(this._onChange);
    },
    buildChart: function () {
        let element, width, height;

        element = $(ReactDOM.findDOMNode(this));

        width = element.width();
        height = element.height();

        this.d3Dispatch = d3.dispatch('scroll');
        this.d3Dispatch.on('scroll', this.onScroll);

        this.svgSel = d3.select(this.svg);

        this.canvas = this.svgSel.select('g');

        if (!this.canvas.node()) {
            this.canvas = this.svgSel.append('g').attr('transform', 'translate(0,' + LEGEND_HEIGHT + ')');

            this.svgSel.on('mousewheel', () => {
                this.d3Dispatch.scroll.call(this, d3.event);

                d3.event.stopPropagation();
                d3.event.preventDefault();
            });
        }

        // Create color scale
        this.colorScale = (() => {
            let colorScale = d3.scale.category20().domain(d3.range(20));

            return node => {
                return colorScale(this.state.nodesDomain[node.type]);
            };
        })();

        this.ipWindow = {start: 0, length: height * .1 / NODE_RADIOUS};

        this.upArrow = {id: 'upArrow', type: 'clientip', triangle: 'triangle-up', visible: false};
        this.downArrow = {id: 'downArrow', type: 'clientip', triangle: 'triangle-down', visible: false};

        // Remove old tips
        d3.selectAll('.d3-tip').remove();

        this.tip = d3.tip()
                .attr('class', 'd3-tip')
                .style('max-width', width+'px')
                .style('margin-right', '20px')
                .html(function (node) {
                    var html;

                    html = '<h5 class="d3-tip-label">' + node.tooltip.title + '</h5>';
                    html+= '<p class="d3-tip-message">' + node.tooltip.message + '</p>';

                    return html;
                })
                .offset(function (node) {
                    return [-10, 0];
                });

        this.canvas.call(this.tip);
    },
    _createLayout() {
        let element = $(ReactDOM.findDOMNode(this));
        let categories = ['referer', 'clientip', 'reqmethod', 'resconttype', 'fulluri', 'refered'];

        return new CategoryLayout()
                        .size([element.width(), element.height() - (LEGEND_HEIGHT*2)])
                        .category(function (node) { return node.type; })
                        .categories(categories)
                        .link((n1, n2) => {
                            let correct = n1.type=='fulluri' ? true : n2.type=='fulluri' ? false : categories.indexOf(n1.type)>categories.indexOf(n2.type);

                            return {
                                source: correct ? n1 : n2,
                                target: correct ? n2 : n1
                            };
                        });
    },
    draw: function () {
        let element, layout, nodes, links;

        // Make sure svg element takes all available space
        element = $(ReactDOM.findDOMNode(this));
        this.svgSel.style('width', element.width()).style('height', element.height());

        layout = this._createLayout();

        nodes = this.applyWindowToNodes(this.state.nodes);

        let windowEnd = this.ipWindow.start + this.ipWindow.length;
        let totalIps = this.state.nodes.reduce((total, node) => total + (node.type=='clientip'?1:0), 0);
        let atStart = this.ipWindow.start<=0;
        let atEnd = windowEnd>=totalIps;

        // Add dummy ip nodes to allocate space for scroll arrows
        this.upArrow.visible = !atStart;
        this.downArrow.visible = !atEnd;

        nodes.unshift(this.upArrow);
        nodes.push(this.downArrow);

        nodes = layout.nodes(nodes);
        links = layout.links(nodes);

        let legendNodes = this.layoutCategories(layout.categories(), layout.size());

        // Remove dummy ip nodes from main array and create a new array
        nodes.splice(nodes.indexOf(this.upArrow), 1);
        nodes.splice(nodes.indexOf(this.downArrow), 1);
        let scrollArrowNodes = [this.upArrow, this.downArrow];

        let transitions = {
            update: d3.transition('transition').duration(TRANSITION_DURATION),
            enter: d3.transition('enter').duration(TRANSITION_DURATION),
            exit: d3.transition('transition').duration(TRANSITION_DURATION)
        };

        this.drawNodes(transitions, nodes);
        this.drawScrollArrowNodes(transitions, scrollArrowNodes);
        this.drawLinks(transitions, links);
        this.drawChatLegend(transitions, legendNodes);
    },
    getRootLocation() {
        return [this.state.root.x, this.state.root.y];
    },
    getNodeLocation(n, removing) {
        if (n.type=='clientip') {
            let delta = !d3.event || !d3.event.deltaY ? -100 : removing ? -d3.event.deltaY : d3.event.deltaY;

            return [n.x, (delta<0 ? -LEGEND_HEIGHT-NODE_RADIOUS : $(ReactDOM.findDOMNode(this)).height()+NODE_RADIOUS)];
        }
        else {
            return this.getRootLocation();
        }
    },
    drawNodes(transitions, nodes) {
        let nodesUpdate = this.canvas.selectAll('.node').data(nodes, n => n.id);

        let nodesEnter = nodesUpdate.enter();
        let nodesExit = nodesUpdate.exit();

        // Add new nodes
        let nodeEnter = nodesEnter.insert('g', '.legend')
            .attr('id', n => n.id)
            .attr('class', n => n.type)
            .style('opacity', 0)
            .classed('node', true)
            .attr('transform', n => 'translate('+ this.getNodeLocation(n) +')');

        nodeEnter.append('circle')
            .classed('background', true)
            .attr('r', NODE_RADIOUS);

        nodeEnter.append('circle')
            .style('stroke', this.colorScale)
            .style('fill', this.colorScale)
                .attr('r', NODE_RADIOUS)
            .on('mouseenter', this.onMouseEntersNode)
            .on('mouseleave', this.onMouseLeavesNode);

        nodeEnter.append('text')
            .attr('y', n => n.type=='referer' || n.type=='refered' ? 30 :-20)
            .text(n => n.name);

        // Remove old nodes
        nodesExit.interrupt().transition(transitions.exit)
            .attr('transform', n => 'translate(' + this.getNodeLocation(n, true) + ')')
            .style('opacity', 0)
            .remove();

        // Update all nodes
        nodesUpdate.interrupt().transition(transitions.update)
            .style('opacity', 1)
            .attr('transform', n => 'translate('+n.x+','+n.y+')');
    },
    drawLinks(transitions, links) {
        let diagonal = d3.svg.diagonal();

        let linksUpdate = this.canvas.selectAll('.link').data(links, l => l.source.id+l.target.id);

        let linksEnter = linksUpdate.enter();
        let linksExit = linksUpdate.exit();

        // Add links to new nodes
        linksEnter.insert('path', '.node')
            .attr('id', l => l.source.id + l.target.id)
            .classed('link', true)
            .style('opacity', 0)
            .attr('d', diagonal);

        // Move links to final position
        linksUpdate.interrupt().transition(transitions.update)
            .style('opacity', 1)
            .attr('d', diagonal);

        linksExit.interrupt().transition(transitions.exit)
            .style('opacity', 0)
            .remove();
    },
    drawScrollArrowNodes(transitions, nodes) {
        let updateSel = this.canvas.selectAll('.arrow').data(nodes.filter(arrow => arrow.visible), arrow => arrow.id);

        let enterSel = updateSel.enter();
        let exitSel = updateSel.exit();

        updateSel.interrupt().transition(transitions.update)
            .attr('transform', arrow => 'translate(' + arrow.x + ',' + arrow.y + ')');

        enterSel.insert('g', '.node')
            .classed('arrow', true)
            .attr('transform', arrow => 'translate(' + arrow.x + ',' + arrow.y + ')')
            .on('mouseenter', this.startScrollInterval)
            .on('mouseleave', this.clearScrollInterval)
            .append('path')
                .attr('d', d3.svg.symbol().type(arrow => arrow.triangle));

        exitSel
            .each(this.clearScrollInterval)
            .remove();
    },
    drawChatLegend(transitions, legends) {
        let updateSel = this.canvas.selectAll('.legend').data(legends, legend => legend.id);
        let enterSel = updateSel.enter();
        let exitSel = updateSel.exit();

        updateSel.interrupt().transition(transitions.update)
            .attr('transform', legend => 'translate(' + legend.x + ',' + legend.y + ')');


        let legendEnter = enterSel.append('g')
            .classed('legend', true)
            .attr('transform', legend => 'translate(' + legend.x + ',' + legend.y + ')');

        legendEnter.append('rect')
            .attr('fill', this.colorScale)
            .attr('stroke', this.colorScale);

        legendEnter.append('text')
            .text(legend => {
                return legend.name;
            });

        exitSel.remove();
    },
    applyWindowToNodes(rawNodes) {
        let windowEnd = this.ipWindow.start + this.ipWindow.length;
        let totalIps = 0;
        let visibleIpNodes = rawNodes
                                    .filter(node => node.type=='clientip')
                                    .filter((ipNode, idx) => {
                                        totalIps++;

                                        return idx>=this.ipWindow.start && idx<windowEnd
                                    })
                                    .map(ipNode => ipNode.id);

        return rawNodes.filter(node => {
            return (
                    (node.type=='clientip' && visibleIpNodes.indexOf(node.id)>=0)
                    || node.type=='fulluri' || node.type=='refered'
                ) ? true : node.paths.some(path => {
                    return path.some(nodeId => visibleIpNodes.indexOf(nodeId)>=0);
                })
        });
    },
    layoutCategories(categories, size) {
        let labels = {
            refered: 'Referred',
            fulluri: 'Threat',
            resconttype: 'ContentType',
            reqmethod: 'Method',
            clientip: 'IP',
            referer: 'Referer'
        };

        let categoryWidth = size[0] / categories.length;

        let xOffset = (categoryWidth / 2) - 20;
        let yOffset = $(ReactDOM.findDOMNode(this)).height() - (LEGEND_HEIGHT*1.5);

        let nodes = categories.map((category, idx) => {
            return {
                id: category,
                name: labels[category],
                type: category,
                x: xOffset + (categoryWidth*idx) - 20,
                y: yOffset
            };
        });

        return nodes;
    },
    startScrollInterval(arrow) {
        arrow.interval = setInterval(this.fireSrollEvent, 500, arrow);
    },
    fireSrollEvent(arrow) {
        this.d3Dispatch.scroll.call(this, {deltaY: 200 * (/up$/.test(arrow.triangle) ? -1 : 1)});
    },
    clearScrollInterval(arrow) {
        arrow.interval && clearInterval(arrow.interval);
        arrow.interval && delete arrow.interval;
    },
    onMouseEntersNode: function (node) {
        // Do nothing on root node
        if (node==this.state.root) {
            this.tip.show.call(this, node);

            return;
        }

        this.canvas.selectAll('.node').classed('blur', n => n.type!='fulluri');
        this.canvas.selectAll('.link').classed('blur', true);

        function unblur(selector) {
            // Unblur node
            this.canvas.select(selector).classed('active', true).classed('blur', false);
        };

        node.paths.forEach(path => {
            let lastId = 'fulluri';
            path.forEach(function (id) {
                //if (!parent) return;
                unblur.call(this, '#'+id);
                unblur.call(this, '#'+lastId+id);

                lastId = id;
            }.bind(this));
        });
    },
    onMouseLeavesNode: function (node) {
        if (node==this.state.root) {
            this.tip.hide();

            return;
        }

        this.svgSel.selectAll('.node,.link').classed('blur', false);
        this.svgSel.selectAll('.active').classed('active', false);
    },
    onScroll(e) {
        let lastStart = this.ipWindow.start;

        this.ipWindow.start += e.deltaY/200;

        if (e.deltaY<0) {
            this.ipWindow.start = Math.floor(this.ipWindow.start);
        }
        else {
            this.ipWindow.start = Math.ceil(this.ipWindow.start);
        }

        if ((this.ipWindow.start + this.ipWindow.length)>this.state.ipCount) {
            this.ipWindow.start = this.state.ipCount-this.ipWindow.length;
        }

        if (this.ipWindow.start<0) this.ipWindow.start = 0;

        lastStart!=this.ipWindow.start && this.draw();
    },
    _getPathFromRequest(request, pathItems) {
        return [
            pathItems.reduce((str, item) => str + item + request[item], ''),
            pathItems.filter(item => item!='referer' || request.referer!='-').map(item => item + request[item])
        ];
    },
    _createNode(id, type, name) {
        return {
            id,
            type,
            name,
            links: {},
            paths: {}
        }
    },
    _addRequestNodes(nodes, requests) {
        let metaData = ['resconttype', 'reqmethod', 'clientip', 'referer'];

        requests.forEach((request, idx) => {
            let parentKey = 'fulluri';

            metaData.forEach(type => {
                let id = type + idx;
                let key = type + request[type];

                if (type=='referer' && request.referer=='-') return;

                if (!(key in nodes)) {
                    nodes[key] = this._createNode(id, type, request[type]);
                }

                let path = this._getPathFromRequest(request, metaData, nodes);
                nodes[key].paths[path[0]] = path[1];
                nodes[key].links[parentKey] = nodes[parentKey];
                nodes[parentKey].links[key] = nodes[key];

                parentKey = key;
            });
        });
    },
    _addReferedNodes(nodes, refered) {
        let parentId = 'fulluri';

        refered.forEach((refered_uri, idx) => {
            let id = 'refered' + idx;

            if (refered_uri=='-') return;

            nodes[id] = this._createNode(id, 'refered', refered_uri);

            nodes[id].paths[refered_uri] = [id];
            nodes[id].links[parentId] = nodes[parentId];
            nodes[parentId].links[id] = nodes[id];
        });
    },
    _onChange() {
        var state;

        state = IncidentProgressionStore.getData();

        if (state.data) {
            let nodes = {
                fulluri: state.root=this._createNode('fulluri', 'fulluri', 'Threat')
            };

            state.root.tooltip = {
                title: 'URI',
                message: state.data.fulluri
            };

            this._addRequestNodes(nodes, state.data.requests);
            this._addReferedNodes(nodes, state.data.referer_for);


            state.nodesDomain = {
                refered: 1,
                fulluri: 3,
                resconttype: 5,
                reqmethod: 15,
                clientip: 17,
                referer: 19
            };
            state.ipCount = 0;

            state.nodes = Object.keys(nodes).map((key, idx) => {
                let node = nodes[key];

                node.paths = Object.keys(node.paths).map(key => {
                    return node.paths[key].map(nodeKey => nodes[nodeKey].id);
                });
                node.type=='clientip' && state.ipCount++;

                return node;
            });

            this.setState(state);
        }
        else {
            this.replaceState(state);
        }
    }
});

module.exports = IncidentProgressionPanel;

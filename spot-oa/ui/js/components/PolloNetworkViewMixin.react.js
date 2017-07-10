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

const Base64 = require('js-base64').Base64;
const d3Interpolate = require('d3-interpolate');
const SpotUtils = require('../utils/SpotUtils');

function getRadious(area) {
    return Math.sqrt(area/Math.PI);
}

const PolloNetworkViewMixin = {
    buildChart() {
        const svgSel = d3.select(this.svg);

        // Enable zoom behaviour
        this.zoom = d3.behavior.zoom().on("zoom", this.onZoom);
        this.canvas = svgSel.append('g');
        svgSel.call(this.zoom);

        // Node related scales
        const hitsDomain = [1, this.state.data.maxNodes];
        const ipDomain = [0,1];
        this.nodeSizeScale = d3.scale.linear().domain(hitsDomain);
        this.chargeScale = d3.scale.linear().domain(hitsDomain);
        this.typeScale = d3.scale.ordinal().domain(ipDomain).range(["circle", "diamond"]);

        // Link related scales
        this.opacityScale = d3.scale.threshold().domain([13]).range([0.1, 1]);
        this.linkColorScale = d3.scale.linear()
                                      .domain([16, 13, 12, 2])
                                      .range([d3.hsl(214, 0.04, 0.34), d3.hsl(216, 0.02, 0.59), d3.hsl(216, 0.69, 0.84), d3.hsl(201, 0.1, 0.72)])
                                      .interpolate(d3Interpolate.interpolateCubehelix);

        // Create a new force layout
        this.force = d3.layout.force();

        // Add and update nodes and links on every tick event from the force layout
        this.force.on('tick', () => {
            if (this.state.loading) {
                this.force.stop();
                return;
            }

            this.drawLinks();
            this.drawNodes();
        });

        // Create a tooltip
        this.tooltip = d3.tip()
            .attr('class', 'd3-tip')
            .html(n => `${n.label}<br/> <small class="text-muted">Right click to apply this node as filter</small>`);

        this.canvas.call(this.tooltip);
    },
    onZoom: function () {
        const translate = d3.event.translate;
        const scale = d3.event.scale;

        this.canvas.attr('transform', `translate(${translate})scale(${scale})`);
    },
    draw() {
        const svgElement = $(this.svg);

        svgElement.width('100%').height('100%');

        const size = [svgElement.width(), svgElement.height()];
        /*
         * Calculate the max area a node can have.
         *
         * 1. Get the smaller value betwenn width and height of the canvas.
         * 2. Calculate the area of imaginary square whose edge length equals the previous value.
         * 3. Set maxNodeArea to the 40% of the available area of our imaginary square
         */
        const maxNodeArea = Math.pow(Math.min(size[0], size[1]), 2) * 0.1;

        // The biggest node we can have goes up to the 50% of maxNodeArea
        this.nodeSizeScale.range([maxNodeArea*0.01, maxNodeArea*0.5]);
        const sizeDomain = this.nodeSizeScale.domain();
        this.chargeScale.range([-this.nodeSizeScale(sizeDomain[0]), -this.nodeSizeScale(sizeDomain[1])])

        this.force
            .stop()
            .size(size)
            .charge(-1000)
            .linkDistance(l => {
                return [l.source, l.target].reduce((length, n) => {
                    return length + getRadious(this.nodeSizeScale(n.hits));
                }, 50);
            })
            .chargeDistance(getRadious(this.nodeSizeScale(this.nodeSizeScale.domain()[1])) * 4)
            .nodes(this.state.data.nodes)
            .links(this.state.data.links);

        this.force.start();

        setTimeout(() => {
            this.force.stop()
                .charge(n => {
                    return this.chargeScale(n.weight);
                })
                .start();
        }, 1000);

        // Tooltip margins
        this.tooltip.box = [size[1]*.4, size[0]*.8, size[1]*.8, size[0]*.4];
    },
    drawNodes(nodes) {
        this.nodesSel = {
            update: this.canvas.selectAll('.node').data(this.state.data.nodes, n => SpotUtils.encodeId(n.id))
        };
        this.nodesSel.enter = this.nodesSel.update.enter();

        // Add new nodes
        this.nodesSel.enter
            .append('path', '.edge')
            .classed('node', true)
            .classed('internal', n => n.internalIp=='1')
            .attr("id", n => SpotUtils.encodeId(n.id))
            .attr("d", d3.svg.symbol()
               .size(n => this.nodeSizeScale(n.hits))
               .type(n => this.typeScale(n.internalIp))
            )
            .call(this.force.drag)
            .on('mouseover', (n) => {
                var direction = '';

                // Decide where should the tooltip be displayed?

                // Vertically
                if (d3.event.layerY<this.tooltip.box[0]) {
                    direction = 's';
                }
                else if (d3.event.layerY>this.tooltip.box[2]) {
                    direction = 'n';
                }

                // Horizontally
                if (d3.event.layerX>this.tooltip.box[1]) {
                    direction += 'w';
                }
                else if (d3.event.layerX<this.tooltip.box[3]) {
                    direction += 'e'
                }

                direction = direction || 'n';

                this.tooltip.direction(direction);
                this.tooltip.show.call(this, n);
            })
            .on("mousedown", n => {
                d3.event.stopPropagation();
            })
            .on("click", n => {
                this._onClick(n.id);
            })
            .on("contextmenu", n => {
                this.tooltip.hide();

                this._onContextualClick(n.id);
            })
            .on('mouseout', () => {
                this.tooltip.hide();
            });

        this.nodesSel.update
            .attr('transform', n => `translate(${n.x},${n.y})`)
            .transition()
                .attr("d", d3.svg.symbol()
                   .size(n => this.nodeSizeScale(n.hits))
                   .type(n => this.typeScale(n.internalIp))
                );
    },
    drawLinks(links) {
        this.linksSel = {
            update: this.canvas.selectAll('.edge').data(this.state.data.links, l => SpotUtils.encodeId(l.id))
        };
        this.linksSel.enter = this.linksSel.update.enter();

        // Add new links
        this.linksSel.enter
             .append('line')
             .classed('edge', true)
             .attr("id", l => SpotUtils.encodeId(l.id))
             .style('stroke', l => this.linkColorScale(l.score))
             .style('stroke-opacity', l => this.opacityScale(l.score));

        // Update links
        this.linksSel.update
            .attr("x1", l => l.source.x)
            .attr("y1", l => l.source.y)
            .attr("x2", l => l.target.x)
            .attr("y2", l => l.target.y);
    },
    highlightNodes(ids) {
        ids = ids instanceof Array ? ids : [ids];

        this.canvas.selectAll('.node').classed('node-faded', true);

        ids.forEach(id => {
            id = SpotUtils.encodeId(id);
            this.canvas.select(`#${id}.node`).classed("node-faded", false);
        })
    },
    highlightEdge(id) {
        id = SpotUtils.encodeId(id);
        this.canvas.selectAll('.edge').classed('edge-faded', true);

        this.canvas.select(`#${id}.edge`)
            .classed('edge-faded', false)
            .classed('highlight', true);
    },
    unhighlight() {
        this.canvas.selectAll('.node').classed('node-faded', false);
        this.canvas.selectAll('.edge')
            .classed('edge-faded', false)
            .classed('highlight', false);
    },
    selectNodes(ids) {
        ids = ids instanceof Array ? ids : [ids];

        this.canvas.selectAll('.node.blink_me').classed('blink_me', false);

        ids.forEach((id) => {
            id = SpotUtils.encodeId(id);
            this.canvas.select(`#${id}.node`).classed('blink_me', true);
        });
    },
    selectEdge(id) {
        id = SpotUtils.encodeId(id);

        this.canvas.selectAll('.edge.blink_me,.edge.active').classed('blink_me', false).classed('active', false);

        this.canvas.selectAll('.edge')
            .classed('blink_me', false)
            .filter(`#${id}`)
                .classed('active', true)
                .classed('blink_me', true);
    }
};

module.exports = PolloNetworkViewMixin;

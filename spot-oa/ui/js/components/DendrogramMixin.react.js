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
const Base64 = require('js-base64').Base64;
var d3 = require('d3');
var React = require('react');
var ReactDOM = require('react-dom');
const SpotUtils = require('../utils/SpotUtils');

var DendrogramMixin = {
  buildChart ()
  {
    $(this.svg).width('100%');

    this.cluster = d3.layout.cluster();

    this.diagonal = d3.svg.diagonal()
                                .projection(function (d) {
                                    return [d.y, d.x]
                                });

    this.canvas = d3.select(this.svg).append('g')
                                        .attr('transform', 'translate(100,50)');
  },
  draw() {
      const svgElement = $(this.svg);

      let canvasWidth = svgElement.width();
      let canvasHeight = 100 + this.state.leafNodes * 20; // Make sure last magic number is at least twice the font size

      svgElement.height(canvasHeight);

      this.cluster.size([canvasHeight-100, canvasWidth-300]);

      const nodes = this.cluster.nodes(this.state.data);
      const links = this.cluster.links(nodes);

      this.drawLinks(links);
      this.drawNodes(nodes);
  },
  drawNodes(nodes) {
      const nodeSel = {};

      nodeSel.update = this.canvas.selectAll('.node').data(nodes, n => n.id);

      nodeSel.enter = nodeSel.update.enter();
      nodeSel.exit = nodeSel.update.exit();

      const nodeEl = nodeSel.enter.append('g')
                                .attr('class', n => `node depth_${n.depth}`);

      nodeEl.append('circle')
                        .attr('r', 4.5)
                        .attr('id', n => SpotUtils.encodeId(n.id))
                        .on('mouseover', function (n) {
                          d3.select(this)
                                        .style('cursor', 'pointer')
                                        .style('fill', '#C4D600');
                        })
                        .on('mouseout', function (d)
                        {
                          d3.select(this)
                                        .style('cursor', null)
                                        .style('fill', null);
                        });

        let y1 = 0;
        let y2 = 0;
        for(let x = 0; x < nodes.length; x++) {
          if(y1 === 0 && nodes[x].depth === 1) {
            y1 = nodes[x].y;
          }
          if(y2 === 0 && nodes[x].depth === 2) {
            y2 = nodes[x].y;
          }
          if(y1 !== 0 && y2 !== 0) {
            break;
          }
        }
        const foreignObject_width = ((y2 - y1) - 16 ) > 0 ? (y2 - y1) - 16 : '17%'; // 16 is the 1em given on the "x" (see below) but we need to take it from the last part of the line, 17% is the minimum width when there is no third node

        // foreignObject is not supported by IE
        nodeEl.append('foreignObject')
                          .attr('x', n => n.depth === 0 ? '-8em' : '1em') //<--- this is the 1em
                          .attr('y', -10)
                          .append('xhtml:div')
                            .html(n => n.name)
                            .filter(n => n.depth === 1)
                              .style('width','auto')
                              .attr({'class': 'spot-text-wrapper', 'data-toggle': 'tooltip'});

      nodeSel.update.selectAll('foreignObject')
                        .style('width', n => n.depth === 1 ? foreignObject_width : 'auto');

    nodeSel.update.attr('transform', n => `translate(${n.y},${n.x})`);

    nodeSel.exit.remove();
  },
  drawLinks(links) {
      const linkSel = {};

      linkSel.update = this.canvas.selectAll('.link')
                                  .data(links, l => `link-${l.source.id}-${l.target.id}`);
      linkSel.enter = linkSel.update.enter();
      linkSel.exit = linkSel.update.exit();

      linkSel.enter.append('path', '.node')
        .attr('class', 'link')
        .on('mouseover', function (l)
        {
            d3.select(this)
                .style('stroke-width', 2)
                .style('cursor', 'pointer')
                .style('stroke', '#ED1C24');

            d3.selectAll(`#${SpotUtils.encodeId(l.source.id)},#${SpotUtils.encodeId(l.target.id)}`)
                .style('fill', '#C4D600');
      })
      .on('mouseout', function (l)
      {
        d3.select(this)
                      .style('stroke-width', null)
                      .style('cursor', null)
                      .style('stroke', null);

        d3.selectAll(`#${SpotUtils.encodeId(l.source.id)},#${SpotUtils.encodeId(l.target.id)}`)
                        .style('fill', null);
      });

      linkSel.update.attr('d', this.diagonal);

      // Remove old nodes
      linkSel.exit.remove();
  }
};

module.exports = DendrogramMixin;

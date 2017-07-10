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
const ReactDOM = require('react-dom');
const d3 = require('d3');
const ChordsDiagramStore = require('../stores/ChordsDiagramStore');

function buildTooltip (d, input, output) {
    const p = d3.format(".2%");

    var tooltip;

    tooltip = `<h5><strong>${d.gname}</strong></h5>`;
    tooltip+= `<p>Got ${numberFormat(d.gvalue)} bytes. `;
    tooltip+= `${p(d.gvalue / d.mtotal)} of matrix total (${numberFormat(d.mtotal)})</p>`;

    var toInfo = '', fromInfo = '';

    input.forEach((bytes, i) => {
        if (bytes==0) return;

        fromInfo += `<li>${numberFormat(bytes)} bytes from ${this.state.data.map[i]}</li>`
    });
    output.forEach((bytes, i) => {
        if (bytes==0) return;

        toInfo += `<li>${numberFormat(bytes)} bytes to ${this.state.data.map[i]}</li>`
    });

    tooltip += '<div style="max-height: 100px; overflow-y: auto;">'
    fromInfo.length && (tooltip+= `<h5><strong>In</strong></h5><ul>${fromInfo}</ul>`);
    toInfo.length && (tooltip+= `<h5><strong>Out</strong></h5><ul>${toInfo}</ul>`);
    tooltip += '</div>';

    return tooltip;
}

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');

const colorScale = d3.scale.category20();
const numberFormat = d3.format(".3s");

const DetailsChordsPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin],
    componentDidMount()
    {
        ChordsDiagramStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount()
    {
        ChordsDiagramStore.removeChangeDataListener(this._onChange);
    },
    buildChart() {
        // generate chord layout
        this.chord = d3.layout.chord()
            .padding(.05)
            .sortSubgroups(d3.descending)
            .matrix(this.state.data.matrix);

        // Main SVG
        this.svgSel = d3.select(this.svg)
                        .attr('width', '100%')
                        .attr('height', '100%');

        this.zoom = d3.behavior.zoom().on('zoom', this.onZoom);
        this.svgSel.call(this.zoom);

        this.canvas = this.svgSel.append('g');

        const dragB = d3.behavior.drag().on('drag', this.drag).on('dragstart', this.dragStart);
        this.canvas.call(dragB);

        if (!this.tooltip) {
            this.tooltip = d3.tip()
                .attr('id', 'chords-tooltip')
                .attr('class', 'd3-tip')
                .html(({d, i}) => {
                    const ibytes = this.state.data.matrix[i];
                    const obytes = this.state.data.matrix.map(row => row[i]);

                    return buildTooltip.call(this, this.state.data.rdr(d), ibytes, obytes);
                });

            this.svgSel.call((selection) => {
                this.tooltip(selection);

                d3.select('#chords-tooltip').on('mouseleave', () => {
                    this.tooltip.hide();
                });
            });
        }

        // Create an arrow marker
        const defs = this.svgSel.append('defs');
        defs.append('marker')
            .attr('id', 'out-arrow-head')
            .attr('markerWidth', '9')
            .attr('markerHeight', '6')
            .attr('refX', '0')
            .attr('refY', '3')
            .attr('orient', 'auto')
            .attr('markerUnits', 'strokeWidth')
            .attr('viewBox', '0 0 18 3')
            .append('path')
                .attr('d', 'M0,0 L0,6 L9,3 z')
                .attr('fill', '#2ca02c');

        defs.append('marker')
            .attr('id', 'in-arrow-head')
            .attr('markerWidth', '9')
            .attr('markerHeight', '6')
            .attr('refX', '0')
            .attr('refY', '3')
            .attr('orient', 'auto')
            .attr('markerUnits', 'strokeWidth')
            .attr('viewBox', '0 0 18 3')
            .append('path')
                .attr('d', 'M0,0 L0,6 L9,3 z')
                .attr('fill', '#ff0000');
    },
    draw() {
        const $svg = $(this.svg);

        // Graph dimensions
        const width = $svg.width();
        const height = $svg.height();


        const transform = d3.transform();
        transform.translate = [width/2,height/2];

        this.zoom.translate(transform.translate);
        this.canvas.attr('transform', transform.toString());

        const innerRadius = Math.min(width, height) * .41; //.41 is a magic number for graph stilyng purposes
        const outerRadius = innerRadius * 1.1; //1.1 is a magic number for graph stilyng purposes

        // Tooltip metadata
        this.tooltip.target = [width/2, height/2];

        this.drawGroups(this.chord.groups(), innerRadius, outerRadius);
        this.drawChords(this.chord.chords(), innerRadius);
    },
    drawGroups(groups, innerRadius, outerRadius) {
        // Appending the chord paths
        const groupsSel = {};

        groupsSel.update = this.canvas.selectAll('g.group').data(groups);
        groupsSel.enter = groupsSel.update.enter();

        const allGroups = groupsSel.enter.append('g').attr('class', 'group');

        const component = this;

        allGroups.append('path')
                   .style('stroke', 'black')
                   .style('fill', d => colorScale(d.index))
                   .style('cursor', 'pointer')
                   .attr('d', d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius))
                   .on('mouseover', (d, i) => component.fade(0.1, i))
                   .on('mouseout', (d, i) => component.fade(1, i));

        const matrix = this.state.data.matrix;
        const rdr = this.state.data.rdr;
        const ip = this.state.data.ip;

        allGroups.each(function (d, i){
            const angle = (d.startAngle + d.endAngle) / 2;

            const g = d3.select(this).append('g')
                .attr('transform', `rotate(${angle * 180 / Math.PI - 90})`
                                    + `translate(${outerRadius * 1.1})`
                                    + (angle > Math.PI ? 'rotate(180)' : '')
                );

            // Hide some ips when they are more than 10
            if (matrix.length > 10)
            {
                g.classed('hidden', (d) => {
                    const _d = rdr(d);

                    // 1. Filter ips with less than 0.5%
                    // 2. Always display current threat
                    return _d.gvalue / _d.mtotal <= 0.005 && _d.gname != ip;
                });
            }

            const _d = rdr(d);
            const revert = angle > Math.PI;
            g.append('text')
                .attr('transform', `translate(${revert ? -20 : 20}, 0)`)
                .attr('dy', '.35em')
                .attr('text-anchor', revert ? 'end' : null)
                .style('font-family', 'helvetica, arial, sans-serif')
                .style('font-size', '12px')
                .style('cursor', 'pointer')
                .style('font-weight', _d.gname == ip ? '900' : 'normal')
                .text(_d.gname)
                .on('mouseover', () => {
                    const mouseToTheRight = d3.event.layerX>component.tooltip.target[0];
                    const mouseTotheBottom = d3.event.layerY>component.tooltip.target[1];

                    // Decide where should the tooltip be displayed?
                    var direction = mouseToTheRight ? 'w' : 'e';

                    component.tooltip.direction(direction);

                    component.tooltip.show({d, i});
                })
                .on('mouseout', () => {
                    if (d3.event.relatedTarget.id!=component.tooltip.attr('id'))
                    {
                        component.tooltip.hide();
                    }
                });

            const input = d.value>0;
            const output = matrix.some(row => {
                return row[d.index]>0;
            });

            const arrowG = g.append('g')
                            .attr('transform', 'scale(.5,.5)');

            const verticalOffset = output && input;

            if (output) {
                // Group has sent some data
                arrowG.append('line')
                    .attr('x1', revert ? -20 : 20)
                    .attr('y1', '0')
                    .attr('x2', '0')
                    .attr('y2', '0')
                    .attr('stroke', '#000')
                    .attr('stroke-width', '3')
                    .attr('transform', `translate(${revert?-5:5},${verticalOffset?-8:0})`)
                    .attr('marker-end', 'url(#out-arrow-head)');
            }

            if (input) {
                arrowG.append('line')
                    .attr('x1', '0')
                    .attr('y1', '0')
                    .attr('x2', revert ? -20 : 20)
                    .attr('y2', '0')
                    .attr('stroke', '#000')
                    .attr('stroke-width', '3')
                    .attr('transform', `translate(${revert?5:-5},${verticalOffset?8:0})`)
                    .attr('marker-end', 'url(#in-arrow-head)');
            }
        });
    },
    drawChords(chords, innerRadius) {
        //grouping and appending the Chords
        const chordsSel = {};

        chordsSel.update = this.canvas.selectAll('.chord path').data(chords);

        chordsSel.enter = chordsSel.update.enter();

        chordsSel.enter.append('g')
            .attr('class', 'chord')
            .on('mouseover', d => this.fade(0.1, d.source.index))
            .on('mouseout', d => this.fade(1))
            .append('path')
                .attr('d', d3.svg.chord().radius(innerRadius))
                .style('fill', d => {
                    return d.source.value>d.target.value ? colorScale(d.source.index) : colorScale(d.target.index);
                });
    },
    onZoom() {
        const transform = d3.transform(this.canvas.attr('transform'));

        transform.scale = d3.event.scale;
        transform.translate = d3.event.translate;

        this.canvas.attr('transform', transform.toString());
    },
    dragStart() {
        const transform = d3.transform(this.canvas.attr('transform'));

        // Coordinates with respect of translation point
        const x = d3.event.sourceEvent.layerX - transform.translate[0];
        const y = d3.event.sourceEvent.layerY - transform.translate[1];

        // Angle of calculated point
        var angle = Math.atan2(-y, x);

        // Translate to degrees
        angle = angle < 0 ? 2 * Math.PI + angle : angle;
        angle = angle * 180 / Math.PI;

        // Store value for future reference
        this.lastAngle = angle;
    },
    drag() {
        d3.event.sourceEvent.preventDefault();
        d3.event.sourceEvent.stopPropagation();
        d3.event.sourceEvent.stopImmediatePropagation();

        const transform = d3.transform(this.canvas.attr('transform'));

        // Coordinates with respect of translation point
        const x = d3.event.x - transform.translate[0];
        const y = d3.event.y - transform.translate[1];

        // Angle of calculated point
        var angle = Math.atan2(-y, x);

        // Translate to degrees
        angle = angle < 0 ? 2 * Math.PI + angle : angle;
        angle = angle * 180 / Math.PI;

        // Rotate diagram N degrees
        transform.rotate = transform.rotate + (this.lastAngle - angle);

        // Store value for future reference
        this.lastAngle = angle;

        this.canvas.attr('transform', transform.toString());
    },
    // Returns an event handler for fading a given chord group.
    fade(opacity, i) {
        this.canvas.selectAll(".chord path")
                                .filter((d) => d.source.index != i && d.target.index != i)
                                .transition()
                                .style("opacity", opacity);
    },
    _onChange() {
        const storeData = ChordsDiagramStore.getData();
        const state = {loading: storeData.loading};

        if (!storeData.loading && !storeData.error) {
            const mpr = chordMpr(storeData.data);

            mpr.addValuesToMap('srcip')
                .addValuesToMap('dstip')
                .setFilter(function (row, a, b) {
                    return (row.srcip === a.name && row.dstip === b.name)
                })
                .setAccessor(function (recs, a, b) {
                    return recs.reduce((total, rec) => {
                        return total + (+rec.ibytes);
                    }, 0);
                });

            const matrix = mpr.getMatrix();
            const map = mpr.getMap();

            const ipMap = Object.keys(map).sort((ip1, ip2) => map[ip1].index-map[ip2].index);

            state.data = {
                matrix,
                map: ipMap,
                rdr: chordRdr(matrix, map),
                ip: ChordsDiagramStore.getIp()
            };
        }

        this.replaceState(state);
    }
});

module.exports = DetailsChordsPanel;

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
const React = require('react');
const ReactDOM = require('react-dom');
const GlobeViewStore = require('../stores/GlobeViewStore');

const fillScale = d3.scale.ordinal()
    .domain(d3.range(4))
    .range(['#ffda00', '#ed1c24', '#000000', '#fdb813']);

const swoosh = d3.svg.line()
    .x((d) => d[0])
    .y(d => d[1])
    .interpolate('cardinal')
    .tension(0);

const location_along_arc = function location_along_arc(start, end, loc) {
  return d3.geo.interpolate(start, end)(loc);
}

const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');

const GlobeViewPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin],
    componentDidMount: function ()
    {
        GlobeViewStore.addChangeDataListener(this._onChange);

        d3.select(ReactDOM.findDOMNode(this))
            .on('mousemove', this.onMouseMove)
            .on('mouseup', this.onMouseUp);
    },
    componentWillUnmount: function ()
    {
        GlobeViewStore.removeChangeDataListener(this._onChange);

        d3.select(ReactDOM.findDOMNode(this))
            .on('mousemove', null)
            .on('mouseup', null);

        d3.select(this.svg).on('mousedown', null);
    },
    buildChart() {
        this.proj = d3.geo.orthographic().clipAngle(90);
        this.sky = d3.geo.orthographic().clipAngle(90);

        this.path = d3.geo.path().projection(this.proj).pointRadius(2);

        let d3svg = d3.select(this.svg)
            .style('width', '100%')
            .style('height', '100%')
            .on('mousedown', this.onMouseDown);

        this.createDefiniions();
        this.createMap();

        d3svg.append('g').attr('class', 'flyers');
        d3svg.append('g').attr('class', 'arcs');
        d3svg.append('g').attr('class', 'src-points');
        d3svg.append('g').attr('class', 'dst-points');
    },
    createDefiniions() {
        let d3svg = d3.select(this.svg);

        // Create definitions
        const defs = d3svg.append('defs');
        const ocean_fill = defs.append('radialGradient')
            .attr('id', 'ocean_fill')
            .attr('cx', '75%')
            .attr('cy', '25%');

        ocean_fill.append('stop')
            .attr('offset', '5%')
            .attr('stop-color', '#fff');
        ocean_fill.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', '#ababab');

        const globe_highlight = defs.append('radialGradient')
            .attr('id', 'globe_highlight')
            .attr('cx', '75%')
            .attr('cy', '25%');

        globe_highlight.append('stop')
            .attr('offset', '5%')
            .attr('stop-color', '#ffd')
            .attr('stop-opacity', '0.6');
        globe_highlight.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', '#ba9')
            .attr('stop-opacity', '0.2');

        const globe_shading = defs.append('radialGradient')
              .attr('id', 'globe_shading')
              .attr('cx', '55%')
              .attr('cy', '45%');

        globe_shading.append('stop')
            .attr('offset', '30%')
            .attr('stop-color', '#fff')
            .attr('stop-opacity', '0');
        globe_shading.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', '#505962')
            .attr('stop-opacity', '0.3');

        const drop_shadow = defs.append('radialGradient')
            .attr('id', 'drop_shadow')
            .attr('cx', '50%')
            .attr('cy', '50%');

        drop_shadow.append('stop')
            .attr('offset', '20%')
            .attr('stop-color', '#000')
            .attr('stop-opacity', '.5')
        drop_shadow.append('stop')
            .attr('offset', '100%')
            .attr('stop-color', '#000')
            .attr('stop-opacity', '0');
    },
    createMap() {
        let d3svg = d3.select(this.svg);

        this.ellipse = d3svg.append('ellipse')
            .attr('class', 'noclicks')
            .style('fill', 'url(#drop_shadow)');

        d3svg.append('circle')
            .attr('class', 'noclicks')
            .style('fill', 'url(#ocean_fill)');

        const worldData = GlobeViewStore.getWorldData();

        d3svg.append('path')
            .datum(topojson.object(worldData, worldData.objects.land))
            .attr('class', 'land noclicks');

        d3svg.append('circle')
            .attr('class', 'noclicks')
            .style('fill', 'url(#globe_highlight)');

        d3svg.append('circle')
            .attr('class', 'noclicks')
            .style('fill', 'url(#globe_shading)');
    },
    draw() {
        let $rootNode = $(ReactDOM.findDOMNode(this));

        const width = $rootNode.width();
        const height = $rootNode.height();

        this.proj
            .translate([width / 2, height / 2])
            .scale(Math.round(height/2.5)); // 2.5 is a magic number for styling purposes

        this.sky
            .translate([width / 1.75, height / 1.75])
            .scale(Math.round(height / 2.5));  // 2.5 is a magic number for styling purposes

        this.ellipse
            .attr('cx', width * 0.8)
            .attr('cy', height * 0.87) // Locate the Ellipse at 80% of the width and 87% of the height
            .attr('rx', this.proj.scale() * .90)
            .attr('ry', this.proj.scale() * .25);

        let d3svg = d3.select(this.svg);

        d3svg.selectAll('circle')
            .attr('cx', width / 2)
            .attr('cy', height / 2)
            .attr('r', () => this.proj.scale());

        this.drawLinks();
        this.drawArcLines();
        this.drawLocations();

        d3svg.selectAll('.land').attr('d', this.path);
    },
    drawLocations() {
        const d3SrcPoints = d3.select(this.svg).select('.src-points');

        const srcSel = {};
        srcSel.update = d3SrcPoints.selectAll('path').data(this.state.data.srcIps);
        srcSel.enter = srcSel.update.enter();
        srcSel.exit = srcSel.update.exit();

        srcSel.exit.remove();

        srcSel.enter.append('path').attr('class', 'point');

        srcSel.update
            .attr('d', this.path);

        const d3DstPoints = d3.select(this.svg).select('.dst-points');

        const dstSel = {};
        dstSel.update = d3DstPoints.selectAll('path').data(this.state.data.dstIps);
        dstSel.enter = dstSel.update.enter();
        dstSel.exit = dstSel.update.exit();

        dstSel.enter.append('path').attr('class', 'point');

        dstSel.exit.remove();

        dstSel.update
            .attr('d', this.path);
    },
    drawLinks() {
        const flyersCanvas = d3.select(this.svg).select('.flyers');
        const flyerSel = {};

        flyerSel.update = flyersCanvas.selectAll('path').data(this.state.data.links);
        flyerSel.enter = flyerSel.update.enter();
        flyerSel.exit = flyerSel.update.exit();

        flyerSel.enter.append('path')
            .attr('class', 'flyer')
            .style('stroke', d => fillScale(d.ltype));

        flyerSel.exit.remove();

        flyerSel.update
            .attr('d', d => swoosh(this.flying_arc(d)))
            .style('opacity', this.fade_at_edge);
    },
    drawArcLines() {
        const arcsCanvas = d3.select(this.svg).select('.arcs');
        const arcsSel = {};

        arcsSel.update = arcsCanvas.selectAll('path').data(this.state.data.arcLines);
        arcsSel.enter = arcsSel.update.enter();
        arcsSel.exit = arcsSel.update.exit();

        arcsSel.enter.append('path').attr('class', 'arc');

        arcsSel.exit.remove();

        arcsSel.update
            .attr('d', this.path)
            .style('opacity', this.fade_at_edge);
    },
    flying_arc(pts) {
        const source = pts.source;
        const target = pts.target;

        const mid = location_along_arc(source, target, 1);
        const result = [
            this.proj(source),
            this.sky(mid),
            this.proj(target)
        ];

        return result;
    },
    fade_at_edge(d) {
        const $svg = $(this.svg);
        const centerPos = this.proj.invert([$svg.width() / 2, $svg.height() / 2]);
        const arc = d3.geo.greatArc();

        let start, end;
        // function is called on 2 different data structures..
        if (d.source) {
            start = d.source,
            end = d.target;
        }
        else {
            start = d.geometry.coordinates[0];
            end = d.geometry.coordinates[1];
        }

        const start_dist = 1.87 - arc.distance({ source: start, target: centerPos });
        const end_dist = 1.87 - arc.distance({ source: end, target: centerPos });

        const fade = d3.scale.linear().domain([-.1, 0]).range([0, .1]);
        const dist = start_dist < end_dist ? start_dist : end_dist;

        return fade(dist)
    },
    // modified from http://bl.ocks.org/1392560
    onMouseDown() {
        this.m0 = [d3.event.pageX, d3.event.pageY];
        this.o0 = this.proj.rotate();
        d3.event.preventDefault();
    },
    onMouseMove() {
        if (this.m0) {
            let m1 = [d3.event.pageX, d3.event.pageY];
            let o1 = [this.o0[0] + (m1[0] - this.m0[0]) / 6, this.o0[1] + (this.m0[1] - m1[1]) / 6];

            o1[1] = o1[1] > 30 ? 30 :
                    o1[1] < -30 ? -30 :
                    o1[1];

            this.proj.rotate(o1);
            this.sky.rotate(o1);

            this.draw();
        }
    },
    onMouseUp() {
        this.onMouseMove();
        this.m0 = null;
    },
    // END of modifications from http://bl.ocks.org/1392560
    _onChange: function ()
    {
        const storeData = GlobeViewStore.getData();

        const state = {
            loading: storeData.loading
        };

        if (storeData.error) {
            state.error = storeData.error;
        }
        else if(!storeData.loading && storeData.data) {
            state.data = this.getStateFromData(storeData.data);
        }

        this.replaceState(state);
    },
    getStateFromData(data) {
        const state = {};

        state.name = GlobeViewStore.getIp();
        state.srcIps = data.sourceips;
        state.dstIps = data.destips;

        state.links = [];
        data.sourceips.forEach(function (a, j) {
            data.destips.forEach(function (b, k) {
                if (j == k) {
                    state.links.push({
                        source: a.geometry.coordinates,
                        target: b.geometry.coordinates,
                        ltype: a.properties.type
                    });
                }
            });
        });

        // build geoJSON features from links array
        state.arcLines = state.links.map(e => {
            return {
                type: 'Feature',
                geometry: {
                    type: 'LineString',
                    coordinates: [e.source, e.target]
                }
            }
        });

        return state;
    }
});

module.exports = GlobeViewPanel;

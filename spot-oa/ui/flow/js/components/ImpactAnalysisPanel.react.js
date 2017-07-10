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
const ImpactAnalysisStore = require('../stores/ImpactAnalysisStore');
const ContentLoaderMixin = require('../../../js/components/ContentLoaderMixin.react');
const ChartMixin = require('../../../js/components/ChartMixin.react');

const ImpactAnalysisPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin],
    componentDidMount: function ()
    {
        ImpactAnalysisStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function ()
    {
        ImpactAnalysisStore.removeChangeDataListener(this._onChange);
    },
    buildChart() {
        const svgSel = d3.select(this.svg);

        svgSel
            .attr('width', '100%')
            .attr('height', '100%')
            .on('click', () => {
                this.drawBars(this.state.data);
            });

        this.canvas = svgSel.append('g');

        this.canvas.append('g').attr('class', 'x axis');

        this.barCanvas = this.canvas.append('g').attr('transform', 'translate(0, 5)');

        // Layout our data
        d3.layout.partition()
            .value(d => d.size)
            .nodes(this.state.data);

        // Create scales
        this.xScale = d3.scale.linear().domain([0, this.state.data.value]).nice();
        this.colorScale = d3.scale.ordinal().range(['#0071c5', '#939598']); // bar color

        this.xAxis = d3.svg.axis().scale(this.xScale).orient('top');
    },
    draw() {
        const container = $(ReactDOM.findDOMNode(this));

        const m = [50, 50, 0, 100]; // top right bottom left
        const w = container.width() - m[1] - m[3]; // width
        const h = container.height() - m[0] - m[2]; // height

        this.xScale.range([0, w]);
        this.y = Math.round(h * 0.1); // bar height

        this.canvas.attr('transform', `translate(${m[3]},${m[0]})`);

        this.drawBars(this.state.data);
    },
    drawBars(root) {
        const duration = d3.event && d3.event.altKey ? 7500 : 750;
        const delay = duration / (root.children.length + 1);

        // Update the x-scale domain.
        this.xScale.domain([0, d3.max(root.children, d => d.value)]).nice();

        // Update the x-axis.
        this.canvas.select('.x.axis')
            .transition()
            .duration(duration)
            .call(this.xAxis);

        const barSel = {};

        barSel.update = this.barCanvas.selectAll('.bar').data(root.children, d => d.name);
        barSel.enter = barSel.update.enter();
        barSel.exit = barSel.update.exit();

        const exitTransition = barSel.exit.transition().duration(duration).delay((d, i) => (i+1) * delay)
        // Fade out text
        exitTransition.selectAll('text').style('fill-opacity', 0);
        // Shrink bar
        exitTransition.selectAll('rect').attr('width', '0');
        // Finally remove g element after transition has finished
        exitTransition.remove();

        const newBar = barSel.enter.append('g')
            .classed('bar', true)
            .attr('transform', (d, i) => `translate(0,${this.y * i * 1.2})`)
            .style('cursor', d => d.children ? 'pointer' : null)
            .on('click', d => {
                d3.event.stopPropagation();

                if (d.children && d.children.length)
                {
                    this.drawBars(d);
                }
            });

        newBar.append('text')
            .attr('x', -6)
            .attr('y', this.y / 2)
            .attr('dy', '.35em')
            .attr('text-anchor', 'end')
            .style('fill-opacity', 1e-6) // Fade In
            .text(d => d.name);

        newBar.append('rect')
            .attr('width', '0')
            .attr('height', this.y)
            .style('fill', (d) => this.colorScale(!!d.children));

        const enterTransition = barSel.update.transition().duration(duration).delay((d, i) => (i+1) * delay);

        enterTransition.selectAll('text').style('fill-opacity', 1);

        enterTransition.selectAll('rect').attr('width', d => this.xScale(d.value));
    },
    _onChange: function ()
    {
        const storeData = ImpactAnalysisStore.getData();

        const state = {loading: storeData.loading};

        if (storeData.error) {
            state.error = storeData.error;
        }
        else if(!storeData.loading && storeData.data) {
            state.data = {
                name: ImpactAnalysisStore.getIp(),
                size: storeData.data.children.length
            };

            state.data.children = storeData.data.children.map((item) => {
                return {
                    name: item.name,
                    size: item.size,
                    children: item.children
                };
            });
        }

        this.replaceState(state);
    }
});

module.exports = ImpactAnalysisPanel;

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

const ContentLoaderMixin = require('./ContentLoaderMixin.react');
const ChartMixin = require('./ChartMixin.react');
const DateUtils = require('../utils/DateUtils');

const IngestSummaryStore = require('../stores/IngestSummaryStore');

const MARGIN = [80, 50, 50, 100];
const TIME_FORMATER = d3.time.format('%Y-%m-%d %H:%M');
const NUMBER_FORMATER = d3.format(',d');

const IngestSummaryPanel = React.createClass({
    mixins: [ContentLoaderMixin, ChartMixin],
    buildChart() {
        // Scales
        this.xScale = d3.time.scale();
        this.yScale = d3.scale.linear();

        // Axis
        this.xAxis = d3.svg.axis().scale(this.xScale).orient('bottom'); // Time
        this.yAxis = d3.svg.axis().scale(this.yScale).orient('left'); // Totals

        // An area generator.
        this.area = d3.svg.area()
              .x(d => this.xScale(d.date))
              .y1(d => (isNaN(d.total) ? this.yScale(0) : this.yScale(d.total)));

        let d3svg = d3.select(this.svg);

        const d3header = d3svg.append('g').attr('class', 'header');

        d3header.append('text')
            .attr('transform', 'translate(0,15)')
            .html('Seeing data <tspan class="bold">from</tspan > <tspan class="min-date" /> <tspan class="bold"> to </tspan> <tspan class="max-date" />');

        d3header.append('text')
            .attr('transform', 'translate(0,30)')
            .html('<tspan class="bold">Total</tspan> records ingested: <tspan class="total" />');

        d3header.append('text')
            .attr('transform', 'translate(0,45)')
            .text('** Zoom in/out using mouse wheel or two fingers in track pad');
        d3header.append('text')
            .attr('transform', 'translate(0,60)')
            .text('** ** Move across the x-axis by clicking anywhere in the graph and dragging to left or right');

        this.updateLegends(this.state.minDate, this.state.maxDate, this.state.total);

        this.canvas = d3svg.append('g')
            .attr('transform', `translate(${MARGIN[3]},${MARGIN[0]})`);

        // Append the clipPath to avoid drawing not seen data
        this.clipRect = d3svg.append('defs')
            .append('clipPath')
                .attr('id', 'clip')
                .append('rect')
                    .attr('x',0)
                    .attr('y', 0);

        this.d3xAxis = this.canvas.append('g').attr('class', 'x axis');
        this.d3yAxis = this.canvas.append('g').attr('class', 'y axis');
        this.pipelineCanvas = this.canvas.append('g').attr('class', 'pipeline');

        this.d3zoom = d3.behavior.zoom()
            .scaleExtent([0.3, 2300]) // these are magic numbers to avoid the grap be zoomable in/out to the infinity
            .on('zoom', this.zoom)

        this.pipelineCanvas.call(this.d3zoom);

        this.pipelineColor = d3.scale.category10().domain(Object.keys(IngestSummaryStore.PIPELINES));
    },
    draw() {
        let $svg = $(this.svg);

        this.width = $svg.width() - MARGIN[1] - MARGIN[3];
        this.height = $svg.height() - MARGIN[0] - MARGIN[2];

        d3.select(this.svg).select('.header').attr('transform', `translate(${this.width/2},0)`);

        this.xScale.range([0, this.width]).domain([this.state.minDate, this.state.maxDate]);
        this.yScale.range([this.height, 0]).domain([0, this.state.maxTotal]);

        this.d3zoom.x(this.xScale)

        this.area.y0(this.height);

        this.clipRect
            .attr('width', this.width)
            .attr('height', this.height);

        this.d3xAxis.attr('transform', `translate(0,${this.height})`);

        this.drawPaths();
    },
    drawPaths() {
        this.d3xAxis.call(this.xAxis);
        this.d3yAxis.call(this.yAxis);

        let total = 0;
        const [minDate, maxDate] = this.xScale.domain();

        // Go to the first millisecond on dates
        minDate.setSeconds(0);minDate.setMilliseconds(0);
        maxDate.setSeconds(59);maxDate.setMilliseconds(0);

        const pipelineData = this.state.data.map(currentMonthData => {
            // Filter records outside current date range
            return currentMonthData.filter(record => {
                const included = record.date>=minDate && record.date<=maxDate;

                // Sum records included in range only
                if (included) total += record.total;

                return included;
            });
        }).filter(monthData => monthData.length>0); // Filter out empty months

        this.drawPipeline(pipelineData);

        this.updateLegends(minDate, maxDate, total);
    },
    drawPipeline(data) {
        const pipelineSel = {};

        pipelineSel.update = this.pipelineCanvas.selectAll('path.area').data(data);

        pipelineSel.enter = pipelineSel.update.enter();
        pipelineSel.exit = pipelineSel.update.exit();

        pipelineSel.enter.append('path')
            .attr('class', 'area')
            .style('fill', this.pipelineColor(IngestSummaryStore.getPipeline()));

        pipelineSel.update.attr('d', d => this.area(d));

        pipelineSel.exit.remove();
    },
    updateLegends(minDate, maxDate, total) {
        const minDateStr = TIME_FORMATER(minDate);
        const maxDateStr = TIME_FORMATER(maxDate);
        const totalStr = NUMBER_FORMATER(total);

        const d3header = d3.select(this.svg).select('.header');

        d3header.select('.min-date').text(minDateStr);
        d3header.select('.max-date').text(maxDateStr);
        d3header.select('.total').text(totalStr);
    },
    zoom() {
        if (d3.event.sourceEvent.type == 'wheel') {
            this.pipelineCanvas.classed('zoom-out', d3.event.sourceEvent.wheelDelta < 0);
            this.pipelineCanvas.classed('zoom-in', d3.event.sourceEvent.wheelDelta >= 0);
            this.pipelineCanvas.classed('e-resize', false);
      }
      else if (d3.event.sourceEvent.type == 'mousemove') {
        this.pipelineCanvas.classed('e-resize', true);
        this.pipelineCanvas.classed('zoom-out', false);
        this.pipelineCanvas.classed('zoom-in', false);
      }

      this.drawPaths();
  },
    componentDidMount() {
        IngestSummaryStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount() {
        IngestSummaryStore.removeChangeDataListener(this._onChange);
    },
    _onChange() {
        const storeData = IngestSummaryStore.getData();

        if (storeData.error) {
            this.replaceState({error: storeData.error});
        }
        else if (!storeData.loading && storeData.data) {
            this.replaceState(this._getStateFromData(storeData.data));
        }
        else {
            this.replaceState({loading: storeData.loading});
        }
    },
    _getStateFromData(data) {
        let total, maxTotal, minDate, maxDate;

        total = 0;
        maxTotal = 0;
        minDate = null;
        maxDate = null;

        data.forEach(function (monthData) {
          monthData.forEach(function (record) {
              minDate = d3.min([minDate, record.date]);
              maxDate = d3.max([maxDate, record.date]);
              maxTotal = d3.max([maxTotal, +record.total]);
              total += +record.total;
          });
        });

        !minDate && (minDate = DateUtils.parseDate(IngestSummaryStore.getStartDate()));
        !maxDate && (maxDate = DateUtils.parseDate(IngestSummaryStore.getEndDate()));

        return {
            loading: false,
            total,
            maxTotal,
            minDate,
            maxDate,
            data: data
        };
    }
});

module.exports = IngestSummaryPanel;

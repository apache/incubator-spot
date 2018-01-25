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

const colorScale = d3.scale.category10();

const locale = d3.locale({
    "decimal": ",",
    "thousands": " ",
    "grouping": [3],
    "dateTime": "%A %e %B %Y, %X",
    "date": "%d/%m/%Y",
    "time": "%H:%M:%S",
    "periods": ["AM", "PM"],
    "days": ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
    "shortDays": ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
    "months": ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
    "shortMonths": ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
});

/*
    TODO: Upgrade to EventDrops 2.0 or later

    At the momment EventDrops 2.0 is out but the version on npm has a bug that
    prevents event counter from being displayed.
 */

const TimelineMixin = {
    buildChart() {
        this.canvas = d3.select(ReactDOM.findDOMNode(this)).select('*').datum(this.state.data);

        const dataDate = this.state.date;
        const startTime = Date.parse(dataDate + " 00:00");
        const endTime = Date.parse(dataDate + " 23:59");

        this.eventDropsChart = d3.chart.eventDrops()
            .start(startTime)
            .end(endTime)
            .locale(locale)
            .axisFormat(xAxis => xAxis.ticks(5))
            .eventLineColor(e => colorScale(e.name));

        if (this.getTooltipContent) {
            // Create a tooltip
            this.tooltip = d3.tip()
                .attr('class', 'd3-tip')
                .html(d => this.getTooltipContent(d));

            this.eventDropsChart.eventHover((e) => {
                const eventData = d3.select(e).data()[0];
                // Get data from event's parent. Super relying on eventDrops implementation
                const parentData = d3.select($(e).parent().get(0)).data()[0];

                // Show tooltip
                const tooltipData = {
                    context: parentData,
                    date: eventData.toLocaleTimeString()
                };

                this.tooltip.show(tooltipData, e);
            });
        }
    },
    draw() {
        const rootNode = $(ReactDOM.findDOMNode(this));
        // Get current viewport width
        this.eventDropsChart.width(rootNode.width());

        // Create svg element and draw eventDropsChart
        this.canvas.call(this.eventDropsChart);
        // TODO: Find a better way to re-render childrens when panel toggles
        // Make sure we don't listen twice for the same event
        $('svg', rootNode).off('parentUpdate').on('parentUpdate', this.draw);

        // Add a tooltip
        if (this.getTooltipContent) {
            this.canvas.select('svg').call(this.tooltip);
        }

        /*
            EventDrops takes a very weird approach to handle de event hover
            event.

            We follow the same approach to try to hide our tooltip when mouse
            has leave an event
        */
        const tooltipRef = this.tooltip;
        d3.select(ReactDOM.findDOMNode(this)).select('rect.zoom')
            .on('mousemove.timeline', function () {
                const zoomRect = d3.select(this);

                zoomRect.attr('display', 'none');
                const el = document.elementFromPoint(d3.event.clientX, d3.event.clientY);
                zoomRect.attr('display', 'block');

                if (el.tagName !== 'circle') {
                    tooltipRef.hide();
                }
            })
            .on('mouseleave.timeline', function () {
                // Fast movements of the mouse point don't hide the tooltip :(
                // Use mouseleave as a last resort to hide our tooltip
                tooltipRef.hide();
            });
    }
};

module.exports = TimelineMixin;

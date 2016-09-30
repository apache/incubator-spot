var $ = require('jquery');
var d3 = require('d3');
var React = require('react');

var DateUtils = require('../../../js/utils/DateUtils');
var InSumActions = require('../actions/InSumActions');
var IngestSummaryStore = require('../stores/IngestSummaryStore');

function initialDraw() {
  var rootNode, format, x, y, xAxis, yAxis, area, svg, rect, total, minDate, maxDate, maxFlows, numberFormat;

  rootNode = d3.select(this.getDOMNode());

  // graph dimensions
  var m = [100, 50, 50, 80], // Margin
      w = $(rootNode.node()).width() - m[1] - m[3], // Width
      h = $(rootNode.node()).height() - m[0] - m[2]; // Height

  format = d3.time.format("%Y-%m-%d %H:%M");

  // Scales.
  x = d3.time.scale().range([0, w]); // get X function
  y = d3.scale.linear().range([h, 0]); // get Y function
  xAxis = d3.svg.axis().scale(x).orient("bottom"); // Get the X axis (Time)
  yAxis = d3.svg.axis().scale(y).orient("left"); // Get Y Axis (Netflows)

  // An area generator.
  area = d3.svg.area()
        .x(function (d) {
            return x(d.date);
        })
        .y0(h)
        .y1(function (d) {
            if (!isNaN(d.flows))
                return y(d.flows);
            else
                return y(0);
        });

  rootNode.select('svg').remove();

  // define the Main SVG
  svg = rootNode.select('#' + this.props.id + '-summary').append("svg")
    .attr("width", w + m[1] + m[3])
    .attr("height", h + m[0] + m[2])
        .append("g")
        .attr("transform", "translate(" + m[3] + "," + m[0] + ")")

  // Append the clipPath to avoid the Area overlapping
  svg.append("clipPath")
        .attr("id", "clip")
        .append("rect")
          .attr("x", x(0))
          .attr("y", y(1))
          .attr("width", x(1) - x(0))
          .attr("height", y(0) - y(1));

  // Append the Y Axis group
  svg.append("g")
    .attr("class", "y axis");

  // Append the X axis group
  svg.append("g")
    .attr("class", "x axis")
    .attr("transform", "translate(0," + h + ")");

  // Append a pane rect, which will help us to add the zoom functionality
  rect = svg.append("rect")
        .attr("class", "pane")
        .attr("width", w)
        .attr("height", h);

  this.state.data.forEach(function (dataSet)
  {
    var a;

    a = [{date: minDate}];
    a.push.apply(a, dataSet);
    minDate = d3.min(a, function (d) { return d.date; });
    a[0] = {date: maxDate, flows: maxFlows};
    maxDate = d3.max(a, function (d) { return d.date; });
    maxFlows = d3.max(a, function (d) { return d.flows; })
  });

  !minDate && (minDate = DateUtils.parseDate(IngestSummaryStore.getStartDate()));
  !maxDate && (maxDate = DateUtils.parseDate(IngestSummaryStore.getEndDate()));

  // bind the data to the X and Y generators
  x.domain([minDate, maxDate]);
  y.domain([0, maxFlows]);

  // Bind the data to our path element.
  svg.selectAll("path.area").data(this.state.data).enter().insert('path', 'g')
                                                .attr('class', 'area')
                                                .attr('clip-path', 'url(#clip)')
                                                .style('fill', '#0071c5')
                                                .attr('d', function (d) {
                                                    return area(d);
                                                });

  //Add the pane rect the zoom behavior
  rect.call(d3.behavior.zoom().x(x)
      .scaleExtent([0.3, 2300]) // these are magic numbers to avoid the grap be zoomable in/out to the infinity
      .on("zoom", zoom.bind(this)));

  function draw () {
    var total, minDate, maxDate, numberFormat;

    svg.select("g.x.axis").call(xAxis);
    svg.select("g.y.axis").call(yAxis);
    svg.selectAll("path.area").attr("d", function (d) { return area(d); });
    numberFormat = d3.format(",d"); // number formatter (comma separated number i.e. 100,000,000)

    rootNode.select('#' + this.props.id + '-range').html("Seeing total flows <strong>from:</strong> " + x.domain().map(format).join(" <strong>to:</strong> "));

    //Calculate the total flows between the displayed date range

    total = 0;
    minDate = x.domain()[0];
    maxDate = x.domain()[1];

    // Go to the first millisecond on dates
    minDate.setSeconds(0);minDate.setMilliseconds(0);
    maxDate.setSeconds(59);maxDate.setMilliseconds(0);

    svg.selectAll("path.area").data().forEach(function (pathData)
    {
      pathData.forEach(function (record)
      {
        // Discard records outside displayed date range
        if (record.date >= minDate && record.date <= maxDate) {
          total += +record.flows;
        }
      });
    });

    rootNode.select('#' + this.props.id + '-total').html("<strong>Total netflows in range:</strong> " + numberFormat(total));
  }

  /*
      Zoom event handler
  */
  function zoom() {
    if (d3.event.sourceEvent.type == "wheel") {
      if (d3.event.sourceEvent.wheelDelta < 0)
         rect.style("cursor", "zoom-out");
      else
         rect.style("cursor", "zoom-in");
    }
    else if (d3.event.sourceEvent.type == "mousemove") {
      rect.style("cursor", "e-resize");
    }

    draw.call(this);
  }

  draw.call(this);
}

var IngestSummaryPanel = React.createClass({
  propTypes: {
    id: React.PropTypes.string
  },
  getDefaultProperties: function () {
    return {
      id: 'oni-is'
    };
  },
  getInitialState: function ()
  {
    return {loading: true};
  },
  render:function()
  {
    var content;

    if (this.state.error)
    {
      content = (
        <div className="text-center text-danger">
          {this.state.error}
        </div>
      );
    }
    else if (this.state.loading)
    {
      content = (
        <div className="oni_loader">
            Loading <span className="spinner"></span>
        </div>
      );
    }
    else
    {
      content = (
        <div id={this.props.id} className="text-center">
          <div id={this.props.id + '-header'}>
            <p id={this.props.id + '-range'}></p>
            <p id={this.props.id + '-total'}></p>
            <p id={this.props.id + '-istructions'} className="small">** Zoom in/out using mouse wheel or two fingers in track pad <br /> ** Move across the x-axis by clicking anywhere in the graph and dragging to left or right</p>
          </div>
          <div id={this.props.id + '-summary'}></div>
        </div>
      );
    }

    return (
      <div>{content}</div>
    )
  },
  componentDidMount: function()
  {
    IngestSummaryStore.addChangeDataListener(this._onChange);
    window.addEventListener('resize', this.buildGraph);
  },
  componentWillUnmount: function ()
  {
    IngestSummaryStore.removeChangeDataListener(this._onChange);
    window.removeEventListener('resize', this.buildGraph);
  },
  componentDidUpdate: function ()
  {
    if (!this.state.loading && !this.state.error)
    {
      this.buildGraph();
    }
  },
  buildGraph: initialDraw,
  _onChange: function () {
    this.setState(IngestSummaryStore.getData());
  }
});

module.exports = IngestSummaryPanel;

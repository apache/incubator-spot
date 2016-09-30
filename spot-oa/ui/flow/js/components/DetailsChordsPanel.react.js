var React = require('react');
var d3 = require('d3');
var ChordsDiagramStore = require('../stores/ChordsDiagramStore');

/**
 *  Draws a chords diagram to display detailed information for a threat
 **/
function draw(matrix, mmap, ip) {
  // generate chord layout
  var chord = d3.layout.chord()
            .padding(.05)
            .sortSubgroups(d3.descending)
            .matrix(matrix);

  var rdr = chordRdr(matrix, mmap);

  // Graph dimensions
  var width = $(this.getDOMNode()).width(),
      height = $(this.getDOMNode()).height(),
      innerRadius = Math.min(width, height) * .41, //.41 is a magic number for graph stilyng purposes
      outerRadius = innerRadius * 1.1; //1.1 is a magic number for graph stilyng purposes

  var fill = d3.scale.ordinal()
                .domain(d3.range(4))
                .range(["#F3D54E", "#00AEEF", "#C4D600", "#FC4C02", "#FFA300"]);

  var dragB = d3.behavior.drag()
              .on('drag', drag.bind(this));

  //Clean the container div to re-draw the diagram
  d3.select(this.getDOMNode()).select('svg').remove();

  // Main SVG
  var svg = d3.select(this.getDOMNode()).append("svg")
            .attr("width", width)
            .attr("height", height)
          .append("g")
            .attr("transform", "translate(" + width / 2 + "," + height / 2 + ")")
            .call(dragB);

  // Tooltip generator
  var tooltip = d3.select(this.getDOMNode())
                    .append("div")
                    .classed('node-label', true);

  // Appending the chord paths
  var groups = svg.selectAll("g.group")
                            .data(chord.groups())
                            .enter().append("svg:g")
                              .attr("class", "group")
                              .on("mouseover", function (d, i) {
                                var chord = svg.selectAll(".chord path").data().filter(function (d) { return (d.source.index == i || d.target.index == i);});

                                tooltip.style("visibility", "visible");
                                tooltip.html(groupTip(rdr(d)) + chordTip(rdr(chord[0])));

                                svg.selectAll(".chord path")
                                                    .filter(function (d) { return d.source.index != i && d.target.index != i; })
                                                    .transition()
                                                    .style("opacity", 0.1);
                              })
                              .on('mousemove', function (d) {
                                if ((height*.5) > d3.event.layerY)
                                {
                                  tooltip.style('top', d3.event.layerY + 'px');
                                }
                                else
                                {
                                  tooltip.style('top', (d3.event.layerY-125) + 'px');
                                }

                                if ((width*.5) > d3.event.layerX)
                                {
                                  // Show tooltip to the right
                                  tooltip.style('left', (d3.event.layerX + 20) + 'px');
                                }
                                else
                                {
                                  // Show tooltip to the left
                                  tooltip.style('left', (d3.event.layerX - 450) + 'px');
                                }
                              })
                              .on("mouseout", function () {
                                tooltip.style("visibility", "hidden");

                                fade(svg, 1)();
                              });

  groups.append("svg:path")
           .style("stroke", "black")
           .style("fill", function (d) { return fill(d.index); })
           .style("cursor", "pointer")
           .attr("d", d3.svg.arc().innerRadius(innerRadius).outerRadius(outerRadius));

  //grouping and appending the Chords
  svg.append("g")
            .attr("class", "chord")
          .selectAll("path")
            .data(chord.chords())
          .enter().append("path")
            .attr("d", d3.svg.chord().radius(innerRadius))
            .style("fill", function (d) { return fill(d.target.index); })
            .style("opacity", 1);

  groups.append("svg:text")
           .each(function (d) { d.angle = (d.startAngle + d.endAngle) / 2; })
           .attr("dy", ".35em")
           .style("font-family", "helvetica, arial, sans-serif")
           .style("font-size", "12px")
           .style("cursor", "pointer")
           .style("font-weight", function (d) {
               var _d = rdr(d);
               if (_d.gname == ip) {
                   return "900";
               }
               return "normal";
           })
           .attr("text-anchor", function (d) { return d.angle > Math.PI ? "end" : null; })
           .attr("transform", function (d) {
               return "rotate(" + (d.angle * 180 / Math.PI - 90) + ")"
                   + "translate(" + (innerRadius * 1.15) + ")"
                   + (d.angle > Math.PI ? "rotate(180)" : "");
           })
           .text(function (d) {
               var _d = rdr(d);
               if (_d.gvalue / _d.mtotal > 0.005 || _d.gname == ip || matrix.length <= 10) {
                   return _d.gname;
               }
           });
}

// Returns an event handler for fading a given chord group.
function fade(svg, opacity, fnMouseover) {
  return function (d, i) {
    svg.selectAll(".chord path")
                            .filter(function (d) { return d.source.index != i && d.target.index != i; })
                            .transition()
                            .style("opacity", opacity);

    if (fnMouseover) {
      fnMouseover();
    }
  };
}

var numberFormat = d3.format(".3s");

function chordTip(d) {
  var p = d3.format(".4%"), q = d3.format(",.3r");

  return "<br/>Chord Info:<br/>"
                      + numberFormat(d.svalue) + " avg bytes from IP: "
                      + d.sname + " to IP: " + d.tname
                      + (d.sname === d.tname ? "" : ("<br/>while...<br/>"
                      + numberFormat(d.tvalue) + " avg bytes From IP: "
                      + d.tname + " to IP: " + d.sname));
}

function groupTip(d) {
  var p = d3.format(".4%"), q = d3.format(",.3r");

  return "Group Info:<br/>"
                        + d.gname + " : " + numberFormat(d.gvalue) + " Avg Bytes <br/>"
                        + p(d.gvalue / d.mtotal) + " of Matrix Total (" + numberFormat(d.mtotal) + ")";
}

function drag() {
  var width = $(this.getDOMNode()).width(),
      height = $(this.getDOMNode()).height(),
      x1 = width / 2,
      y1 = height / 2,
      x2 = d3.event.x,
      y2 = d3.event.y;

  var newAngle = Math.atan2(y2 - y1, x2 - x1) / (Math.PI / 180);

  d3.select(this.getDOMNode()).select("svg > g").attr("transform", "translate(" + width / 2 + "," + height / 2 + ") rotate(" + newAngle + ",0,0)");
}

var DetailsChordsPanel = React.createClass({
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
      content = '';
    }

    return (
      <div>{content}</div>
    )
  },
  componentDidMount: function()
  {
    ChordsDiagramStore.addChangeDataListener(this._onChange);
    window.addEventListener('resize', this.buildGraph);
  },
  componentWillUnmount: function ()
  {
    ChordsDiagramStore.removeChangeDataListener(this._onChange);
    window.removeEventListener('resize', this.buildGraph);
  },
  componentDidUpdate: function ()
  {
    if (!this.state.loading && !this.state.error)
    {
      this.buildGraph();
    }
  },
  buildGraph: function ()
  {
    var mpr, storeData;

    storeData = ChordsDiagramStore.getData()
    mpr = chordMpr(storeData.data);

    mpr.addValuesToMap('srcip')
                  .addValuesToMap('dstip')
                  .setFilter(function (row, a, b) {
                      return (row.srcip === a.name && row.dstip === b.name)
                  })
                  .setAccessor(function (recs, a, b) {
                      if (recs.length==0 || !recs[0]) {
                          return 0;
                      }
                      return +recs[0].maxbyte;
                  });

    draw.call(this, mpr.getMatrix(), mpr.getMap(), ChordsDiagramStore.getIp());
  },
  _onChange: function () {
    this.setState(ChordsDiagramStore.getData());
  }
});

module.exports = DetailsChordsPanel;

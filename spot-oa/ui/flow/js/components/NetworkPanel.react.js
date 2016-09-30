var React = require('react');

var d3 = require('d3');
var d3Interpolate = require('d3-interpolate');

var OniActions = require('../../../js/actions/OniActions');
var EdInActions = require('../../../js/actions/EdInActions');
var OniConstants = require('../../../js/constants/OniConstants');
var SuspiciousStore = require('../stores/SuspiciousStore');

/**
 *  Draw network view
 **/
function draw(selectedEdgeId, sourceIpNodeId, targetIpNodeId, data) {
  //Get the nodes from the data
  var data = this.state.data;

  var nodes = data.map(function (d) {
    return { ip: d.srcIP, isInternal: +d.srcIpInternal };
  })

  var nodes_2 = data.map(function (d) {
    return { ip: d.dstIP, isInternal: +d.destIpInternal };
  });

  nodes = getUniqueNodes(nodes.concat(nodes_2));

  // Get the edges from the data
  var edges = data.map(function (d) {
    var fSrc = -1, fDst = -1, i, n;

    // Find indexes for srcIP and dstIP in nodes array
    for (i=0, n=nodes.length ; i<n ; i++)
    {
      // Is this srcIP?
      if (nodes[i].ip == d.srcIP) fSrc = i;
      // Is this dstIP?
      if (nodes[i].ip == d.dstIP) fDst = i;

      // Stop looking as soon as we find both indexes
      if (fSrc>=0 && fDst>=0) break;
    }

    return {
      source: fSrc,
      target: fDst,
      weight: -Math.log(d.lda_score),
      id: "k" + d.srcIP.replace(/\./g, "_") + "-" + d.dstIP.replace(/\./g, "_")
    };
  });

  // Update the degree in the edges if the edge is a suspect
  edges.forEach(function (d) {
    nodes[d.source].degree = nodes[d.source].degree + 1 || 1;
    nodes[d.target].degree = nodes[d.target].degree + 1 || 1;
  });

  // define an opacity function
  var opacity = d3.scale.threshold()
                                    .domain([13])
                                    .range([0.1, 1]);

  // Color for edges
  var color = d3.scale.linear()
                                .domain([16, 13, 12, 2])
                                .range([d3.hsl(214, 0.04, 0.34), d3.hsl(216, 0.02, 0.59), d3.hsl(216, 0.69, 0.84), d3.hsl(201, 0.1, 0.72)])
                                .interpolate(d3Interpolate.interpolateCubehelix);

  // Graph dimensions
  var w = $(this.getDOMNode()).width(),
      h = $(this.getDOMNode()).height(),
      r = Math.round(w * 0.005), // 0.005 magic number for nodes styling purposes when expanding graph, radious is 0.5% of the #grap div
      rightMargin = w - r,
      bottomMargin = h - r,
      size = [w, h];

  //Main SVG container
  d3.select(this.getDOMNode()).select('svg').remove();

  var svg = d3.select(this.getDOMNode())
                .append('svg')
                    .attr('width', w)
                    .attr('height', h)
                .append('g');

  // Graph force
  var force = d3.layout.force()
                .linkDistance(70)
                .charge(-120)
                .chargeDistance(-Math.round(h * 0.55)) // 0.55 is a magic number for graph styling purposes charge is 55% of the grap height
                .size(size)
                .nodes(nodes)
                .links(edges);

  // Group and append the edges to the main SVG
  svg.append('g')
       .selectAll('.edge')
       .data(edges)
       .enter()
            .append('line')
            .classed('edge', true)
            .attr("id", function (d) { return d.id; })
            .style('stroke', function (d) { return color(d.weight); })
            .style('stroke-opacity', function (d) { return opacity(d.weight); });

  var edge = svg.selectAll('.edge');

  // Tooltip generator
  var tooltip = d3.select(this.getDOMNode())
                    .append("div")
                    .classed('node-label', true);

  // GROUP and append the nodes to the main SVG

  var node = svg.append('g')
                 .selectAll('.node')
                 .data(nodes.filter(function (d) { return d.degree > 0; }))
                 .data(nodes)
                 .enter()
                   .append('path')
                   .classed('node', true)
                   .attr("id", function (d) { return "n" + d.ip.replace(/\./g, "_"); })
                   .attr("d", d3.svg.symbol()
                        .size(function (d) { return (d.degree + r) * 20; })
                        .type(function (d) {
                            if (d.isInternal == 1)
                                return "diamond";
                            else
                                return "circle";
                        }))
                   .attr('fill', function (d) {
                       if (d.isInternal == 1)
                           return "#0071C5";
                       else
                           return "#fdb813";
                   })
                   .call(force.drag)
                   .on('mouseover', function (d) {
                       tooltip.html(d.ip + '<br/> <small class="text-muted">Right click to apply IP filter</small>')
                             .style('visibility', 'visible');
                   })
                   .on('mousemove', function (d) {
                      tooltip.style('top', d3.event.layerY + 'px');

                      if ((w*.5) > d3.event.layerX)
                      {
                        // Show tooltip to the right
                        tooltip.style('left', (d3.event.layerX + 20) + 'px');
                      }
                      else
                      {
                        // Show tooltip to the left
                        tooltip.style('left', (d3.event.layerX - 200) + 'px');
                      }
                   })
                   .on("click", nodeClick)
                   .on("contextmenu", nodeContextualClick)
                   .on('mouseout', function () { tooltip.style('visibility', 'hidden'); });

  // set the tick event listener for the force
  force.on('tick', function () {
    edge.attr("x1", function (d) { return d.source.x; })
            .attr("y1", function (d) { return d.source.y; })
            .attr("x2", function (d) { return d.target.x; })
            .attr("y2", function (d) { return d.target.y; });

    node.attr('transform', function (d) {
            d.x = Math.max(r, Math.min(rightMargin, d.x));
            d.y = Math.max(r, Math.min(bottomMargin, d.y));
            return 'translate(' + d.x + ',' + d.y + ')';
    });
  });

  force.start();

  // if the function params are not null then that means we have a selected edge and nodes and we need to add the blink animation to them
  if (this.state.selectedEdgeId && this.state.selectedSrcNodeId && this.state.selectedDstNodeId) {
    var selectedEdge = d3.select("#" + this.state.selectedEdgeId)
               .style("stroke", "#FDB813")
               .style("stroke-opacity", "1")
               .classed("blink_me", true)
               .classed("active", true);
    var parent = $("#" + selectedEdge.attr("id")).parent()

    selectedEdge.remove();

    parent.append(selectedEdge[0]);

    d3.select("#" + this.state.selectedSrcNodeId)
        .classed("blink_me", true);

    d3.select("#" + this.state.selectedDstNodeId)
          .classed("blink_me", true);
  }
}

/**
 *  Load Chord diagram on the node click
 **/
function nodeClick(d) {
  EdInActions.selectIp(d.ip);
  OniActions.toggleMode(OniConstants.DETAILS_PANEL, OniConstants.VISUAL_DETAILS_MODE);
  EdInActions.reloadVisualDetails();
}

function nodeContextualClick (d)
{
  d3.event.preventDefault();

  EdInActions.setFilter(d.ip);
  EdInActions.reloadSuspicious();
}

/**
 *  Get rid of duplicated nodes
 **/
function getUniqueNodes(nodes) {
  var a = [];

  for (var i = 0, j = nodes.length; i < j; i++) {
    if (a.filter(function (n) {return n.ip == nodes[i].ip }).length == 0) {
      a.push(nodes[i]);
    }
  }

  return a;
}

/**
 *Fades the non-highlighted edges and highlights the selected one. It gets triggered when the user hovers over one of the sconnects row
 **/
function highlightEdge(id) {
  d3.selectAll(".edge").classed("edge-faded", true);

  d3.selectAll(".node").classed("node-faded", true);

  d3.select("#" + id)
        .style("stroke", "#FDB813")
        .style("stroke-opacity", "1");

  d3.select("#" + id).classed("edge-faded", false);

  var sourceIpNode = "n" + d3.select("#" + id).data()[0].source.ip.replace(/\./g, "_");
  var targetIpNode = "n" + d3.select("#" + id).data()[0].target.ip.replace(/\./g, "_");

  d3.select("#" + sourceIpNode).classed("node-faded", false);
  d3.select("#" + targetIpNode).classed("node-faded", false);
}

function unhighlightEdge(id) {
  showFullGraphWithSelectedEdge();
}

/**
 *  Sets the blink_me class to the selected edge in the sconnects table. It also removes the faded classes from edges and nodes in the netflow view
 **/
function selectEdge(id) {
  d3.selectAll(".edge")
        .filter(".active")
        .classed("active", false)
        .classed("blink_me", false);

  d3.selectAll(".node")
        .filter(".blink_me")
        .classed("blink_me", false);

  var edge = d3.select("#" + id)
               .style("stroke", "#FDB813")
               .style("stroke-opacity", "1")
               .classed("blink_me", true)
               .classed("active", true);
  var parent = $("#" + edge.attr("id")).parent()
  edge.remove();
  parent.append(edge[0]);

  var sourceIpNode = "n" + d3.select("#" + id).data()[0].source.ip.replace(/\./g, "_");
  var targetIpNode = "n" + d3.select("#" + id).data()[0].target.ip.replace(/\./g, "_");

  d3.select("#" + sourceIpNode)
         .classed("blink_me", true);
  d3.select("#" + targetIpNode)
        .classed("blink_me", true);

  showFullGraphWithSelectedEdge();

}

/**
 *  Shows the graph without the faded clasess, If there's a selected node it adds the blink class to it
 **/
function showFullGraphWithSelectedEdge() {

  var color = d3.scale.linear()
                        .domain([16, 13, 12, 2])
                        .range([d3.hsl(214, 0.04, 0.34), d3.hsl(216, 0.02, 0.59), d3.hsl(216, 0.69, 0.84), d3.hsl(201, 0.1, 0.72)])
                        .interpolate(d3Interpolate.interpolateCubehelix);

  var opacity = d3.scale.threshold()
                        .domain([13])
                        .range([0.1, 1]);

  d3.selectAll(".edge")
        .filter("*:not(.active)")
        .style("stroke", function (d) { return color(d.weight) })
        .style("stroke-opacity", function (d) { return opacity(d.weight); });

  d3.selectAll(".edge").classed("edge-faded", false);
  d3.selectAll(".node").classed("node-faded", false);
}

var NetworkPanel = React.createClass({
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
    SuspiciousStore.addChangeDataListener(this._onChange);
    SuspiciousStore.addThreatHighlightListener(this._onHighlight);
    SuspiciousStore.addThreatUnhighlightListener(this._onUnhighlight);
    SuspiciousStore.addThreatSelectListener(this._onSelect);
    window.addEventListener('resize', this.buildGraph);
  },
  componentWillUnmount: function ()
  {
    SuspiciousStore.removeChangeDataListener(this._onChange);
    SuspiciousStore.removeThreatHighlightListener(this._onHighlight);
    SuspiciousStore.removeThreatUnhighlightListener(this._onUnhighlight);
    SuspiciousStore.removeThreatSelectListener(this._onSelect);
    window.removeEventListener('resize', this.buildGraph);
  },
  componentDidUpdate: function ()
  {
    if (!this.state.loading && !this.state.error)
    {
      this.buildGraph();
    }
  },
  buildGraph: draw,
  _onChange: function () {
    this.setState(SuspiciousStore.getData());
  },
  _onHighlight: function () {
    var threat, id;

    threat = SuspiciousStore.getHighlightedThreat();

    id = "k" + threat.srcIP.replace(/\./g, "_") + "-" + threat.dstIP.replace(/\./g, "_");

    highlightEdge(id);
  },
  _onUnhighlight: unhighlightEdge,
  _onSelect: function () {
    var threat, id;

    threat = SuspiciousStore.getSelectedThreat();

    id = "k" + threat.srcIP.replace(/\./g, "_") + "-" + threat.dstIP.replace(/\./g, "_");

    selectEdge(id);
  }
});

module.exports = NetworkPanel;

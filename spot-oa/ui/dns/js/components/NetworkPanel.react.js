var d3 = require('d3');
var d3Interpolate = require('d3-interpolate');
var React = require('react');

var OniActions = require('../../../js/actions/OniActions');
var EdInActions = require('../../../js/actions/EdInActions');
var OniConstants = require('../../../js/constants/OniConstants');
var SuspiciousStore = require('../stores/SuspiciousStore');
var OniUtils = require('../../../js/utils/OniUtils')

var topLevelDomains;

function getDnsNodeName (fullName)
{
  var name = fullName.split(".").length <=2 ? fullName : "";

  if(name == "")
  {
    var tldIndex = -1;

    topLevelDomains.forEach(function (d, i)
    {
      if (fullName.indexOf(d) > -1)
      {
        tldIndex = i;
        return;
      }
    });

    name = fullName.split('.').slice(tldIndex - 1, fullName.split('.').length).join('.');
  }

  return name;
}

function encodeNodeId(id)
{
  // Make sure id is a valid HTML Attribute value
  return encodeURIComponent(id).replace(/\.|%/g, "_");
}

function filterDataAndBuildGraph()
{
  var data = this.state.data;
  //Get the nodes from the data
  var nodes = data.map(function (d)
                       {
                          var top_domain = getDnsNodeName(d.dns_qry_name);
                          return { name: top_domain, isInternal: 1};
                        });

  var nodes_2 = data.map(function (d)
                         {
                            return { name: d.ip_dst, isInternal: 0};
                          });

  nodes = getUniqueNodes(nodes.concat(nodes_2));

  // Get the edges from the data
  var edges = data.map(function (d, i)
                        {
                            var nodeName, ii, n, source, target, id;

                            nodeName = getDnsNodeName(d.dns_qry_name);

                            source = -1;
                            target = -1;

                            // Look for first match in the array
                            for (ii=0, n=nodes.length ; ii<n ; ii++)
                            {
                                if (source==-1 && nodes[ii].name==nodeName)
                                {
                                    source = ii;
                                }

                                if (target==-1 && nodes[ii].name==d.ip_dst)
                                {
                                    target = ii;
                                }

                                if (source>=0 && target>=0) break;
                            }

                          id = nodeName + "-" + d.ip_dst;
                          id = encodeNodeId(id);

                          return {
                            source: source,
                            target: target,
                            weight: -Math.log(d.score),
                            id: "k" + id
                          };
                        });

  // Update the degree in the edges if the edge is a suspect
  edges.forEach(function (d)
                {
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

  // Color for nodes
  var nodeColor = d3.scale.ordinal()
                          .domain([10, 169, 172, 224, 239, 255])
                          .range([
                                  d3.rgb(237, 28, 36),
                                  d3.rgb(237, 78, 36),
                                  d3.rgb(237, 98, 36),
                                  d3.rgb(237, 138, 36),
                                  d3.rgb(237, 168, 36),
                                  d3.rgb(237, 198, 36)
                          ]);

  var linkStrength = d3.scale.threshold()
                        .domain([13])
                        .range([0.01, 1]);

  //Main SVG container
  var svg = d3.select(this.getDOMNode()).select('svg');

  if (svg.length>0) svg.remove();

  // Graph dimensions
  var w = $(this.getDOMNode()).width(),
      h = $(this.getDOMNode()).height(),
      size = [w, h],
      r = Math.round(w * 0.005); // 0.005 magic number for nodes styling purposes when expanding graph, radious is 0.5% of the #grap div

  svg = d3.select(this.getDOMNode()).append('svg')
                  .attr('width', w)
                  .attr('height', h)
                  .append('g');

  // Graph force
  var force = d3.layout.force()
                          .charge(-Math.round(w * 0.45)) // 0.45 is a magic number for graph styling purposes charge is 45% of the grap width
                          .linkDistance(Math.round(w * 0.081)) // 0.081 is a magic number for graph styling purposes linkDistance is 8.1% of the graph width
                          .gravity(.1)
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

  //Tooltip generator
  var tooltip = d3.select(this.getDOMNode())
                            .append("div")
                            .classed('node-label', true);

  // GROUP and append the nodes to the main SVG

  var node = svg.append('g')
                            .selectAll('.node')
                            .data(nodes.filter(function (d)
                            {
                              return d.degree > 0;
                            }))
                            .data(nodes)
                            .enter().append('path')
                                    .classed('node', true)
                                    .classed('dns-name', function (d)
                                    {
                                      if (d.isInternal == 1)
                                        return true;
                                      return false;
                                    })
                                    .attr("id", function (d) { return "n" + d.name.replace(/\./g, "_"); })
                                    .attr("d", d3.svg.symbol()
                                                              .size(function (d)
                                                              {
                                                                if (d.isInternal == 1 && d.name.length > 60)
                                                                  return (d.degree + r) * 40;
                                                                return (d.degree + r) * 20;
                                                              })
                                                              .type(function (d)
                                                              {
                                                                if (d.isInternal == 1)
                                                                  return "diamond";
                                                                else
                                                                  return "circle";
                                                              })
                                    )
                                    .attr('fill', function (d)
                                    {
                                      if (d.isInternal == 1)
                                        return "#0071C5";
                                      else
                                        return "#fdb813";
                                    })
                                    .call(force.drag)
                                    .on('mouseover', function (d)
                                    {
                                      var html = d.name;

                                      if (d.isInternal == 0)
                                      {
                                        html += '<br/> <span class="x-small text-muted">Right click to apply IP filter</span>';
                                      }
                                      else
                                      {
                                        html += '<br/> <span class="x-small text-muted">Right click to apply DNS filter</span>';
                                      }

                                      tooltip.html(html)
                                                        .style('visibility', 'visible');
                                    })
                                    .on('mousemove', function ()
                                    {
                                      tooltip.style('top', d3.event.layerY + 'px');

                                      if ((w*.5) > d3.event.layerX)
                                      {
                                        // Show tooltip to the right
                                        tooltip.style('left', (d3.event.layerX + 20) + 'px');
                                      }
                                      else
                                      {
                                        // Show tooltip to the left
                                        tooltip.style('left', (d3.event.layerX - 250) + 'px');
                                      }
                                    })
                                    .on("contextmenu", function (d, i)
                                    {
                                      tooltip.style('visibility', 'hidden');
                                      nodeContextualClick(d, i);
                                    })
                                    .on('mouseout', function () { tooltip.style('visibility', 'hidden'); });

  node.filter(function (d)
      {
        return !d.isInternal;
      })
      .on("click", nodeclick);

  svg.selectAll('.edge')
              .classed('suspicious', function (d) {
                  var suspicious = data.filter(function (item) {
                      return item.ip_dst == nodes[d.target].name && item.dns_qry_name.indexOf(nodes[d.source].name) > -1 && item.dns_qry_name.length > 64;
                  });

                  if (suspicious.length > 0) {
                      return true;
                  }

                  return false;
              })
              .style("stroke", function (d) {
                  var suspicious = data.filter(function (item) {
                      return item.ip_dst == nodes[d.target].name && item.dns_qry_name.indexOf(nodes[d.source].name) > -1 && item.dns_qry_name.length > 64;
                  });

                  if (suspicious.length > 0) {
                      return '#ed1c24';
                  }

                  return color(d.weight);
              });


  // set the tick event listener for the force
  force.on('tick', function () {
                edge.attr("x1", function (d) { return d.source.x; })
                    .attr("y1", function (d) { return d.source.y; })
                    .attr("x2", function (d) { return d.target.x; })
                    .attr("y2", function (d) { return d.target.y; });

                /*node.attr('cx', function (d) { return d.x = Math.max(r, Math.min(w - r, d.x)); })
                    .attr('cy', function (d) { return d.y = Math.max(r, Math.min(h - r, d.y)); });
                  */
                node.attr('transform', function (d) {
                    d.x = Math.max(r, Math.min(w - r, d.x));
                    d.y = Math.max(r, Math.min(h - r, d.y));
                    return 'translate(' + d.x + ',' + d.y + ')';
                });
  });

  force.start();

  // if the function params are not null then that means we have a selected edge and nodes and we need to add the blink animation to them
  if (this.state.selectedEdgeId && this.state.sourceIpNodeId & this.state.targetIpNodeId)
  {
    var selectedEdge = svg.select("#" + this.state.selectedEdgeId)
                                                     .style("stroke", "#FDB813")
                                                     .style("stroke-opacity", "1")
                                                     .classed("blink_me", true)
                                                     .classed("active", true);

    var parent = $(".force #" + selectedEdge.attr("id")).parent();

    selectedEdge.remove();

    parent.append(selectedEdge[0]);

    svg.select("#" + this.state.sourceIpNodeId)
                                  .classed("blink_me", true);

    svg.select("#" + this.state.targetIpNodeId)
                                  .classed("blink_me", true);

  }
}

//load Chord diagram on the node click
function nodeclick (d)
{
  EdInActions.selectIp(d.name);
  OniActions.toggleMode(OniConstants.DETAILS_PANEL, OniConstants.VISUAL_DETAILS_MODE);
  EdInActions.reloadVisualDetails();
}

function nodeContextualClick (d)
{
  d3.event.preventDefault();

  EdInActions.setFilter(d.name);
  EdInActions.reloadSuspicious();
}

/**
  * Fades the non-highlighted edges and highlights the selected one. It gets triggered when the user hovers over one of the sconnects row
  */
function highlightEdge(id)
{
  var chart = d3.select(this.getDOMNode());

  chart.selectAll(".edge").classed("edge-faded", true);

  chart.selectAll(".node").classed("node-faded", true);

  chart.select("#" + id)
                    .style("stroke", "#FDB813")
                    .style("stroke-opacity", "1");

  chart.select("#" + id).classed("edge-faded", false);

  var sourceIpNode = "n" + d3.select("#" + id).data()[0].source.name.replace(/\./g, "_");
  var targetIpNode = "n" + d3.select("#" + id).data()[0].target.name.replace(/\./g, "_");

  chart.select("#" + sourceIpNode).classed("node-faded", false);
  chart.select("#" + targetIpNode).classed("node-faded", false);
}

/**
  * Sets the blink_me class to the selected edge in the sconnects table. It also removes the faded classes from edges and nodes in the netflow view
  */
function selectEdge(id)
{
  var chart = d3.select(this.getDOMNode());

  chart.selectAll(".edge")
                      .filter(".active")
                      .classed("active", false)
                      .classed("blink_me", false);

  chart.selectAll(".node")
                      .filter(".blink_me")
                      .classed("blink_me", false);

  var edge = chart.select("#" + id)
                                .style("stroke", "#FDB813")
                                .style("stroke-opacity", "1")
                                .classed("blink_me", true)
                                .classed("active", true);

  var parent = $("#" + edge.attr("id"), this.getDOMNode()).parent();

  edge.remove();
  parent.append(edge[0]);

  var sourceIpNode = "n" + chart.select("#" + id).data()[0].source.name.replace(/\./g, "_");
  var targetIpNode = "n" + chart.select("#" + id).data()[0].target.name.replace(/\./g, "_");

  chart.select("#" + sourceIpNode)
                              .classed("blink_me", true);
  chart.select("#" + targetIpNode)
                              .classed("blink_me", true);

  showFullGraphWithSelectedEdge.call(this);
}

/**
  * Shows the graph without the faded clasess, If there's a selected node it adds the blink class to it
  */
function showFullGraphWithSelectedEdge()
{
  var chart = d3.select(this.getDOMNode());
  var color = d3.scale.linear()
                                .domain([16, 13, 12, 2])
                                .range([d3.hsl(214, 0.04, 0.34), d3.hsl(216, 0.02, 0.59), d3.hsl(216, 0.69, 0.84), d3.hsl(201, 0.1, 0.72)])
                                .interpolate(d3Interpolate.interpolateCubehelix);

  var opacity = d3.scale.threshold()
                                    .domain([13])
                                    .range([0.1, 1]);

  chart.selectAll(".edge")
                      .filter("*:not(.active)")
                      .style("stroke", function (d)
                      {
                        if ($("#" + d.id + ".suspicious").length > 0)
                        {
                            return "#ed1c24";
                        }

                        return color(d.weight);
                      })
                      .style("stroke-opacity", function (d) { return opacity(d.weight); });

  chart.selectAll(".edge").classed("edge-faded", false);
  chart.selectAll(".node").classed("node-faded", false);
}

function getUniqueNodes(nodes)
{
  var a = [];

  for (var i = 0, j = nodes.length; i < j; i++)
  {
    if (a.filter(function (n) { return n.name == nodes[i].name }).length == 0)
    {
      a.push(nodes[i]);
    }
  }

  return a;
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
    if (!this.state.loading)
    {
      this.buildGraph();
    }
  },
  buildGraph: function ()
  {
    if (!topLevelDomains)
    {
      d3.csv('domainList.csv', function (domains)
      {
        topLevelDomains = domains;
        filterDataAndBuildGraph.call(this);
      }.bind(this));
    }
    else
    {
      filterDataAndBuildGraph.call(this);
    }
  },
  _onChange: function ()
  {
    this.setState(SuspiciousStore.getData());
  },
  _onHighlight: function ()
  {
    var threat, id;

    threat = SuspiciousStore.getHighlightedThreat();

    id = getDnsNodeName(threat.dns_qry_name) + '-' + threat.ip_dst;
    id = 'k' + encodeNodeId(id);

    highlightEdge.call(this, id);
  },
  _onUnhighlight: showFullGraphWithSelectedEdge,
  _onSelect: function ()
  {
    var threat, id;

    threat = SuspiciousStore.getSelectedThreat();

    id = getDnsNodeName(threat.dns_qry_name) + '-' + threat.ip_dst;
    id = 'k' + encodeNodeId(id);

    selectEdge.call(this, id);
  }
});

module.exports = NetworkPanel;

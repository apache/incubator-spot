var $ = require('jquery');
var d3 = require('d3');
var React = require('react');

function setChildrenIds(root)
{
    root.children.forEach(function (child, i)
    {
        child.id = root.id + '_' + i;

        child.children instanceof Array && child.children.length>0 && setChildrenIds(child);
    });
}

function countLeafNodes(root)
{
    var count = 0;

    if (!(root.children instanceof Array)) return 0;

    root.children.forEach(function (child)
    {
        if (child.children && child.children.length>0)
        {
            count += countLeafNodes(child);
        }
        else
        {
            count++;
        }
    });

    return count;
}

function buildDendrogram (root)
{
    var width, height, cluster, diagonal, svg, nodes, leafNodes, node, links, link;

    // Build data tree structure

    root.id = 'root';
    setChildrenIds(root);

    leafNodes = countLeafNodes(root);

    // Substract scroll bar width
    width = $(this.getDOMNode().parentNode).width() - 20;
    height = 100 + leafNodes * 20; // Make sure last magic number is at least twice the font size

    cluster = d3.layout.cluster()
                                .size([height-100, width-300]);

    diagonal = d3.svg.diagonal()
                                .projection(function (d) {
                                    return [d.y, d.x]
                                });

    // Remove any old dendrogram
    // TODO: Update diagram instead of remove node
    d3.select(this.getDOMNode()).select('svg').remove();

    svg = d3.select(this.getDOMNode()).append('svg')
                                            .attr('width', width)
                                            .attr('height', height)
                                        .append('g')
                                            .attr('transform', 'translate(100,50)');

    // Build Dendrogram

    nodes = cluster.nodes(root);
    links = cluster.links(nodes);

    link = svg.selectAll('.link')
                                .data(links)
                                .enter().append('path')
                                                    .attr('class', 'link')
                                                    .attr('d', diagonal)
                                                    .on("mouseover", function (d)
                                                    {
                                                      d3.select(this)
                                                                    .style("stroke-width", 2)
                                                                    .style("cursor", "pointer")
                                                                    .style("stroke", "#ED1C24");

                                                      d3.select("#" + d.source.id)
                                                                                  .style("fill", "#C4D600");
                                                                                  //.attr("r", 4.5 * 2);

                                                      d3.select("#" + d.target.id)
                                                                                  .style("fill", "#C4D600");
                                                                                  //.attr("r", 4.5 * 2);
                                                    })
                                                    .on("mouseout", function (d)
                                                    {
                                                      d3.select(this)
                                                                    .style("stroke-width", null)
                                                                    .style("cursor", null)
                                                                    .style("stroke", null);

                                                      d3.select("#" + d.source.id)
                                                                                 .style("fill", null);

                                                      d3.select("#" + d.target.id)
                                                                                  .style("fill", null);
                                                    });

    node = svg.selectAll('.node')
                              .data(nodes)
                              .enter().append('g')
                                                  .attr('class', function (d) { return 'node depth_' + d.depth; })
                                                  .attr('transform', function (d) { return 'translate(' + d.y + ',' + d.x + ')'; });

    node.append('circle')
                      .attr('r', 4.5)
                      .attr("id", function (d) { return d.id;})
                      .on("mouseover", function (d)
                      {
                        d3.select(this)
                                      .style("cursor", "pointer")
                                      .style("fill", "#C4D600");
                      })
                      .on("mouseout", function (d)
                      {
                        d3.select(this)
                                      .style("cursor", null)
                                      .style("fill", null);
                      });

    node.append('text')
                    .attr('dx', function (d) { return d.depth===0 ? -10 : 10; })
                    .attr('dy', 3)
                    .style('text-anchor', function (d) { return d.depth===0 ? 'end' : 'start' })
                    .attr('fill', 'black')
                    .text(function (d) { return d.name; });
}

var DendrogramMixin = {
  getInitialState: function ()
  {
    return {loading: false, root: {}};
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
      <div className="dendrogram">{content}</div>
    )
  },
  componentDidUpdate: function ()
  {
    if (!this.state.loading && !this.state.error)
    {
      if (this.state.root.name===undefined)
      {
        d3.select(this.getDOMNode()).select('svg').remove();
      }
      else
      {
        buildDendrogram.call(this, this.state.root);
      }
    }
  }
};

module.exports = DendrogramMixin;

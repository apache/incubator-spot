var React = require('react');
var queue = require('d3-queue');
var GlobeViewStore = require('../stores/GlobeViewStore');

var m0, o0, fill, proj, sky, path, swoosh, links, svg, width, height, arcLines;
var dataset, container;

function buildGraph(root, ipsrc) {
    container = $(this.getDOMNode());
    container.html("");
    dataset = root;

     //Event Handlers
    d3.select(container[0])
       .on("mousemove", mousemove)
       .on("mouseup", mouseup);

    fill = d3.scale.ordinal()
           .domain(d3.range(4))
           .range(["#ffda00", "#ed1c24", "#000000", "#fdb813"]);

    width = container.width();
    height = container.height();

    proj = d3.geo.orthographic()
        .translate([width / 2, height / 2])
        .clipAngle(90)
        .scale(Math.round(height/2.5)); // 2.5 is a magic number for styling purposes

    sky = d3.geo.orthographic()
        .translate([width / 1.75, height / 1.75])
        .clipAngle(90)
        .scale(Math.round(height / 2.5));

    path = d3.geo.path().projection(proj).pointRadius(2);

    swoosh = d3.svg.line()
          .x(function (d) {
              return d[0]
          })
          .y(function (d) {
              return d[1]
          })
          .interpolate("cardinal")
          .tension(0);

    links = [];
    arcLines = [];

    svg = d3.select(container[0]).append("svg")
        .attr("width", width)
        .attr("height", height)
        .on("mousedown", mousedown);

    queue()
        .defer(d3.json, "../flow/world-110m.json")
        .defer(getRawData)
        .await(ready);

}

function getRawData(callback){
    callback(null,dataset.children);
}

function ready(error, world, places) {
    if (error != null && error != undefined) {
        container.html('<p class="lead text-danger"> Oops! looks like the data for this incident is missing. </p>');
        return;
    }
    var ocean_fill = svg.append("defs").append("radialGradient")
          .attr("id", "ocean_fill")
          .attr("cx", "75%")
          .attr("cy", "25%");
    ocean_fill.append("stop").attr("offset", "5%").attr("stop-color", "#fff");
    ocean_fill.append("stop").attr("offset", "100%").attr("stop-color", "#ababab");

    var globe_highlight = svg.append("defs").append("radialGradient")
        .attr("id", "globe_highlight")
        .attr("cx", "75%")
        .attr("cy", "25%");
    globe_highlight.append("stop")
        .attr("offset", "5%").attr("stop-color", "#ffd")
        .attr("stop-opacity", "0.6");
    globe_highlight.append("stop")
        .attr("offset", "100%").attr("stop-color", "#ba9")
        .attr("stop-opacity", "0.2");

    var globe_shading = svg.append("defs").append("radialGradient")
          .attr("id", "globe_shading")
          .attr("cx", "55%")
          .attr("cy", "45%");
    globe_shading.append("stop")
        .attr("offset", "30%").attr("stop-color", "#fff")
        .attr("stop-opacity", "0")
    globe_shading.append("stop")
        .attr("offset", "100%").attr("stop-color", "#505962")
        .attr("stop-opacity", "0.3")

    var drop_shadow = svg.append("defs")
        .append("radialGradient")
        .attr("id", "drop_shadow")
        .attr("cx", "50%")
        .attr("cy", "50%");
    drop_shadow.append("stop")
        .attr("offset", "20%").attr("stop-color", "#000")
        .attr("stop-opacity", ".5")
    drop_shadow.append("stop")
        .attr("offset", "100%").attr("stop-color", "#000")
        .attr("stop-opacity", "0")

    svg.append("ellipse")
        .attr("cx", width * 0.8).attr("cy", height * 0.87) // Locate the Ellipse at 80% of the width and 87% of the height
        .attr("rx", proj.scale() * .90)
        .attr("ry", proj.scale() * .25)
        .attr("class", "noclicks")
        .style("fill", "url(#drop_shadow)");

    svg.append("circle")
        .attr("cx", width / 2).attr("cy", height / 2)
        .attr("r", proj.scale())
        .attr("class", "noclicks")
        .style("fill", "url(#ocean_fill)");

    svg.append("path")
        .datum(topojson.object(world, world.objects.land))
        .attr("class", "land noclicks")
        .attr("d", path);

    svg.append("circle")
        .attr("cx", width / 2).attr("cy", height / 2)
        .attr("r", proj.scale())
        .attr("class", "noclicks")
        .style("fill", "url(#globe_highlight)");

    svg.append("circle")
        .attr("cx", width / 2).attr("cy", height / 2)
        .attr("r", proj.scale())
        .attr("class", "noclicks")
        .style("fill", "url(#globe_shading)");

    svg.append("g").attr("class", "points")
        .selectAll("text").data(places.sourceips)
        .enter().append("path")
        .attr("class", "point")
        .attr("d", path);

    svg.append("g").attr("class", "points")
        .selectAll("text").data(places.destips)
        .enter().append("path")
        .attr("class", "point")
        .attr("d", path);

    places.sourceips.forEach(function (a, j) {
        places.destips.forEach(function (b, k) {
            if (j == k) {
                links.push({
                    source: a.geometry.coordinates,
                    target: b.geometry.coordinates,
                    ltype: a.properties.type
                });
            }
        });
    });

    // build geoJSON features from links array
    links.forEach(function (e, i, a) {
        var feature = { "type": "Feature", "geometry": { "type": "LineString", "coordinates": [e.source, e.target] } }
        arcLines.push(feature)
    })

    svg.append("g").attr("class", "arcs")
        .selectAll("path").data(arcLines)
        .enter().append("path")
        .attr("class", "arc")
        .attr("d", path)

    svg.append("g").attr("class", "flyers")
        .selectAll("path").data(links)
        .enter().append("path")
        .attr("class", "flyer")
        .style("stroke", function (d) {
            return fill(d.ltype);
        })
        .attr("d", function (d) {
            return swoosh(flying_arc(d));
        })

    refresh();
}

function flying_arc(pts) {
    var source = pts.source,
        target = pts.target;

    var mid = location_along_arc(source, target, 1);
    var result = [proj(source),
                   sky(mid),
                   proj(target)]
    return result;
}

function refresh() {
    svg.selectAll(".land").attr("d", path);
    svg.selectAll(".point").attr("d", path);

    svg.selectAll(".arc").attr("d", path)
        .attr("opacity", function (d) {
            return fade_at_edge(d)
        })

    svg.selectAll(".flyer")
        .attr("d", function (d) {
            return swoosh(flying_arc(d));
        })
        .attr("opacity", function (d) {
            return fade_at_edge(d);
        })
}

function fade_at_edge(d) {
    var centerPos = proj.invert([width / 2, height / 2]),
        arc = d3.geo.greatArc(),
        start, end;
    // function is called on 2 different data structures..
    if (d.source) {
        start = d.source,
        end = d.target;
    }
    else {
        start = d.geometry.coordinates[0];
        end = d.geometry.coordinates[1];
    }

    var start_dist = 1.87 - arc.distance({ source: start, target: centerPos }), //1.57
        end_dist = 1.87 - arc.distance({ source: end, target: centerPos });

    var fade = d3.scale.linear().domain([-.1, 0]).range([0, .1])
    var dist = start_dist < end_dist ? start_dist : end_dist;

    return fade(dist)
}

function location_along_arc(start, end, loc) {
    var interpolator = d3.geo.interpolate(start, end);
    return interpolator(loc)
}

// modified from http://bl.ocks.org/1392560
function mousedown() {
    m0 = [d3.event.pageX, d3.event.pageY];
    o0 = proj.rotate();
    d3.event.preventDefault();
}
function mousemove() {
    if (m0) {
        var m1 = [d3.event.pageX, d3.event.pageY]
          , o1 = [o0[0] + (m1[0] - m0[0]) / 6, o0[1] + (m0[1] - m1[1]) / 6];
        o1[1] = o1[1] > 30 ? 30 :
                o1[1] < -30 ? -30 :
                o1[1];
        proj.rotate(o1);
        sky.rotate(o1);
        refresh();
    }
}
function mouseup() {
    if (m0) {
        mousemove();
        m0 = null;
    }
}


var GlobeViewPanel = React.createClass({
    componentDidMount: function ()
    {
        GlobeViewStore.addChangeDataListener(this._onChange);
    },
    componentWillUnmount: function ()
    {
        GlobeViewStore.removeChangeDataListener(this._onChange);
    },
    _onChange: function ()
    {
        var state, filterName, root;

        state = GlobeViewStore.getData();

        root = {
            name: GlobeViewStore.getFilterValue(),
            children: []
        };

        if (!state.loading)
        {
            filterName = GlobeViewStore.getFilterName();
            root.children = state.data;
        }

        state.root = root;
        delete state.data;

        this.setState(state);
    },
    getInitialState: function ()
    {
        return {loading: false};
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
    componentDidUpdate: function ()
    {
        if (!this.state.loading && !this.state.error)
        {

          buildGraph.call(this, this.state.root);
        }
    }
});

module.exports = GlobeViewPanel;

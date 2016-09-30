var $ = require('jquery');
var d3 = require('d3');
var React = require('react');

function buildGraph(root, ipsrc) {
    var legend = root.legend == undefined? true : root.legend;
    var chartPlaceholder = $(this.getDOMNode()).find("#graph");
    chartPlaceholder.html("");
    $(this.getDOMNode()).find("#legend").html("");
    var names = [];
    var data = [];
    var dataDate = root.date.split(' ')[0];
    var endTime = Date.parse(dataDate + " 23:59");

    var startTime = Date.parse(dataDate + " 00:00");
    var csvdata = root.children;

    names = [];

    csvdata.forEach(function (d) {
        if (names.indexOf(d.srcip) == -1) {
            names.push(d.srcip);
        }
        if (names.indexOf(d.dstip) == -1) {
            names.push(d.dstip);
        }
    });

    function createEvent(name) {
        var event = {};
        event.name = name;
        event.dates = [];
        event.ports = [];

        csvdata.filter(function (d) {
            if (d.srcip == name) {
                event.dates.push(parseddate(d.tstart));
                event.ports.push(parseInt(d.sport));
            }
            return;
        });

        return event;
    }

    function parseddate(sdate) {
        let dtpart = sdate.split(" ")
        let dpart = dtpart[0].split("-")
        let tpart = dtpart[1].split(":")
        //The 7 numbers specify the year, month, day, hour, minute, second, in that order:
        //2014-07-08 02:38:59
        return new Date(parseInt(dpart[0]), parseInt(dpart[1]) - 1, parseInt(dpart[2]), parseInt(tpart[0]) - 1, parseInt(tpart[1]) - 1, parseInt(tpart[2]) - 1);
    }

    for (var i = 0; i < names.length; i++) {
        var event = createEvent(names[i])
        if (event.dates.length > 0) {
            data.push(event);
        }
    }

    var color = d3.scale.category10();

    var locale = d3.locale({
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

    var width = chartPlaceholder.width()-5; // 5 is the magic number to avoid having horizontal scroll bar

    var graph = d3.chart.eventDrops()
        .start(new Date(startTime))
        .end(new Date(endTime))
        .locale(locale)
        .eventColor(function (datum) {
            return color(datum.ports);
        })
        .width(width-30)
        .margin({ top: 70, left: 140, bottom: 5, right: 5 })
        .axisFormat(function (xAxis) {
            xAxis.ticks(5);
        })
        .eventHover(function (el) {
            var series = el.parentNode.firstChild.innerHTML.replace(/\(\d+\)/g, "");
            var port = el.parentNode.__data__.ports[0];
            var timestamp = d3.select(el).data()[0];
            if (legend)
                $(this.getDOMNode()).find("#legend").html('Hovering [' + timestamp + '] <br /> in series "' + series + '" at port ' + port);
        }.bind(this)

        );

    var element = d3.select(chartPlaceholder[0]).append('div').datum(data);

    graph(element);
}


var TimelineMixin = {
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
            content = [<div id="graph" key="timeline_graph"></div>, <div id="legend" key="timeline_legend"></div>];
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
};

module.exports = TimelineMixin;

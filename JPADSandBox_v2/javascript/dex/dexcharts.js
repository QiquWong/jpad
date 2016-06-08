function Event(config)
{
  var props =
  {
		  
  }
}

function HorizontalLegend(config)
{
  // Default parameters.
  var p =
  {
    labels          : [ "A", "B", "C" ],
    parent          : null,
    xoffset         : 10,
    yoffset         : 20,
    cellWidth       : 30,
    cellHeight      : 20,
    tickLength      : 25,
    caption         : "Legend",
    color           : d3.scale.category20c(),
    captionFontSize : 14,
    captionXOffset  : 0,
    captionYOffset  : -6
  };

  // If we have parameters, override the defaults.
  if (config !== 'undefined')
  {
    for (var prop in config)
    {
      p[prop] = config[prop];
    }
  }
  
  function chart()
  {
    // Create our x scale
    var x = d3.scale.ordinal()
      .domain(p.labels)
      .range(d3.range(p.labels.length).map(function(i) { return i * p.cellWidth; }));

    // Create the x axis.
    var xAxis = d3.svg.axis()
      .scale(x)
      .orient("bottom")
      .tickSize(p.tickLength)
      .tickPadding(10)
      .tickValues(p.labels)
      .tickFormat(function(d) { return d; });

    // Append a graphics node to the supplied svg node.
    var g = p.parent.append("g")
      .attr("class", "key")
      .attr("transform", "translate(" + p.xoffset + "," + p.yoffset + ")");

    // Draw a colored rectangle for each ordinal range.
    g.selectAll("rect")
      .data(p.labels)
      .enter().append("rect")
      .attr("height", p.cellHeight)
      .attr("x", function(d, i) { return x(i); })
      .attr("width", function(d) { return p.cellWidth; })
      .style("fill", function(d, i)
      {
        return p.color(i);
      });

    // Add the caption.
    g.call(xAxis).append("text")
      .attr("class", "caption")
      .attr("y", p.captionYOffset)
      .attr("x", p.captionXOffset)
      .text(p.caption)
      .style("font-size", p.captionFontSize);

  };

  // Use this routine to retrieve and update attributes.
  chart.attr = function(name, value)
  {
	if (arguments.length == 1)
	{
	  return p[name];
	}
	else if (arguments.length == 2)
	{
	  p[name] = value;
	}
	return chart;
  }
	
  chart.update = function()
  {
  }

  return chart;
}

function VerticalLegend(config)
{
  // Default parameters.
  var p =
  {
    labels          : [ "A", "B", "C" ],
    parent          : null,
    xoffset         : 50,
    yoffset         : 30,
    cellWidth       : 30,
    cellHeight      : 20,
    tickLength      : 5,
    caption         : "Legend",
    color           : d3.scale.category20c(),
    captionFontSize : 14,
    captionXOffset  : -30,
    captionYOffset  : -20
  };

  // If we have parameters, override the defaults.
  if (config !== 'undefined')
  {
    for (var prop in config)
    {
      p[prop] = config[prop];
    }
  }
  
  function chart()
  {
    // Create our x scale
    var y = d3.scale.ordinal()
      .domain(p.labels)
      .range(d3.range(p.labels.length).map(function(i) { return i * p.cellHeight; }));

    // Create the x axis.
    var yAxis = d3.svg.axis()
      .scale(y)
      .orient("left")
      .tickSize(p.tickLength)
      .tickPadding(10)
      .tickValues(p.labels)
      .tickFormat(function(d) { return d; });

    // Append a graphics node to the supplied svg node.
    var g = p.parent.append("g")
      .attr("class", "key")
      .attr("transform", "translate(" + p.xoffset + "," + p.yoffset + ")");

    // Draw a colored rectangle for each ordinal range.
    g.selectAll("rect")
      .data(p.labels)
      .enter().append("rect")
      .attr("height", p.cellHeight)
      .attr("y", function(d, i) { return y(i); })
      .attr("width", p.cellWidth)
      .style("fill", function(d, i)
      {
        return p.color(i);
      });

      // Add the caption.
      g.call(yAxis).append("text")
        .attr("class", "caption")
        .attr("y", p.captionYOffset)
        .attr("x", p.captionXOffset)
        .text(p.caption)
        .style("font-size", p.captionFontSize);
  };

  // Use this routine to retrieve and update attributes.
  chart.attr = function(name, value)
  {
	if (arguments.length == 1)
	{
      return p[name];
	}
	else if (arguments.length == 2)
	{
      p[name] = value;
	}
	return chart;
  }
	
  chart.update = function()
  {
  }

  return chart;
}

////
//
// LineChart - This function will return a reusable LineChart with the
//             supplied configuration.
//
// parent     = A mandatory element indicating the parent node of this chart.
// labels     = The labels, names or headers for the data within this chart.
// data       = The data to be graphed in this chart.
// width      = The width in pixels of this chart.
// height     = The height in pixels of this chart.
// xi         = The index within the data array for the x axis.
// yi         = The index within the data array for the y axis.
// xoffset    = The x offset (relative to the parent) in pixels where we will
//              start rendering this chart.
// yoffset    = The y offset (relative to the parent) in pixels where we will
//              start rendering this chart.
//
////
function LineChart(config)
{
  // The event handler for mouse over.
  var mouseOverHandler;

  // Default parameters.
  var p =
  {
    parent          : null,
    labels          : [ "X", "Y" ],
    listeners       : [],
    data            : [[0,0],[1,1],[2,4],[3,9],[4,16]],
    width           : 600,
    height          : 400,
    xi              : 0,
    yi              : 1,
    xoffset         : 0,
    yoffset         : 0
  };

  // If we have user-defined parameters, override the defaults.
  if (config !== 'undefined')
  {
    for (var prop in config)
    {
      p[prop] = config[prop];
    }
  }

  // Render this chart.
  function chart()
  {
	// Use a linear scale for x, map the value range to the pixel range.
    var x = d3.scale.linear()
      .domain(d3.extent(p.data, function(d) { return +d[p.xi]; }))
      .range([0, p.width]);

    // Use a linear scale for y, map the value range to the pixel range.
    var y = d3.scale.linear()
      .domain(d3.extent(p.data, function(d) { return +d[p.yi]; }))
      .range([p.height, 0]);

    // Create the x axis at the bottom.
    var xAxis = d3.svg.axis()
      .scale(x)
      .orient("bottom");

    // Create the y axis to the left.
    var yAxis = d3.svg.axis()
      .scale(y)
      .orient("left");

    // Define a function to draw the line.
    var line = d3.svg.line()
      .x(function(d) { return x(+d[p.xi]); })
      .y(function(d) { return y(+d[p.yi]); });

    // Append a graphics node to the parent, all drawing will be relative
    // to the supplied offsets.  This encapsulating transform simplifies
    // the offsets within the child nodes.
    var chartContainer = p.parent.append("g")
      .attr("transform", "translate(" + p.xoffset + "," + p.yoffset + ")");

    // Draw the x axis.
    chartContainer.append("g")
      .attr("class", "x axis")
      .attr("transform", "translate(0," + p.height + ")")
      .call(xAxis);

    // Draw the y axis.
    chartContainer.append("g")
      .attr("class", "y axis")
      .call(yAxis)
      .append("text")
      .attr("transform", "rotate(-90)")
      .attr("y", 6)
      .attr("dy", ".71em")
      .style("text-anchor", "end")
      .text(p.labels[p.yi]);

    // Draw the line.
    chartContainer.append("path")
      .datum(p.data)
      .attr("class", "line")
      .attr("d", line);
    
    // We handle mouseover with transparent rectangles.  This will calculate
    // the width of each rectangle.
    var rectalWidth = x(p.data[1][p.xi]) - x(p.data[0][p.xi]);

    // Add the transparent rectangles for our mouseover events.
    chartContainer.selectAll("rect")
    .data(p.data.map(function(d) { return d; }))
    .enter().append("rect")
    .attr("class", "overlay")
    .attr("transform", function(d,i) { return "translate(" + x(d[p.xi]) + ",0)"; })
    .attr("opacity", 0.0)
    .attr("width", rectalWidth)
    .attr("height", p.height)
    .on("mouseover", function(d)
    {
      mouseOverHandler(d, true);
    });

    // This function handles the mouseover event.
    //
    // data will contain the row experiencing mouseover.
    // originator will be true if this is being called by the chart which
    //   is originating this event, false otherwise.  This is required to
    //   avoid recursion of listeners notifying originators.
    mouseOverHandler = function (data, originator)
    {
      // Remove any old circles.
      chartContainer.selectAll("circle").remove();
      chartContainer.selectAll("text").remove();
      
      // Draw a small red circle over the mouseover point.
      var circle = chartContainer.append("circle")
        .attr("fill", "red")
        .attr("r", 4)
        .attr("cx", x(data[p.xi]))
        .attr("cy", y(data[p.yi]));
   
      chartContainer.append("text")
        .attr("x", x(data[p.xi]))
        .attr("y", y(data[p.yi]) - 10)
        .attr("dy", ".35m")
        .style("font-size", 14)
        .attr("text-anchor", "top")
        .attr("fill", "black")
        .text(function(d) { return data[p.yi];});
      
      // If we're the originator of this event, notify our listeners to
      // update themselves in turn.
      if (originator)
      {
        for (var i=0; i<p.listeners.length; i++)
        {
          p.listeners[i].handleEvent(
            { type: "mouseover", data : data, originator: false});
        }
      }
    }
  }

  // This is the public on mouseover function which is visible to others.
  chart.handleEvent = function(event)
  {
    if (type == "mouseover")
    {
      mouseOverHandler(data, originator);
    }
  }

  // Use this routine to retrieve and update attributes.
  chart.attr = function(name, value)
  {
	// When no arguments are given, we return the current value of the
	// attribute back to the caller.
    if (arguments.length == 1)
    {
      return p[name];
    }
    // Given 2 arguments we set the name=value.
    else if (arguments.length == 2)
    {
      p[name] = value;
    }
    
    // Return the chart object back so we can chain the operations together.
    return chart;
  }

  // This routine supports the update operation for this chart.  This is
  // applicable when the chart should be partially updated.
  chart.update = function()
  {
  }

  // Return the instantiated chart object.
  return chart;
}

function ScatterPlot(config)
{
  // The event handler for mouse over.
  var mouseOverHandler;
  var mouseOutHandler;
  
  // Default parameters.
  var p =
  {
    parent           : null,
    labels           : [ "X", "Y" ],
    listeners        : [],
    data             : [[0,0],[1,1],[2,4],[3,9],[4,16]],
    width            : 600,
    height           : 400,
    margin           : { top: 20, right: 15, bottom: 60, left: 60 },
    selectedColor    : "red",
    unselectedColor  : "steelblue",
    unselectedRadius : 8,
    selectedRadius   : 8,
    xi               : 0,
    yi               : 1,
    xoffset          : 0,
    yoffset          : 0
  };

  // If we have user-defined parameters, override the defaults.
  if (config !== 'undefined')
  {
    for (var prop in config)
    {
      p[prop] = config[prop];
    }
  }

  // Render this chart.
  function chart()
  {
	var dimensions =
	{
	  width  : p.width - p.margin.left - p.margin.right,
      height : p.height - p.margin.top - p.margin.bottom
    };

    var x = d3.scale.linear()
              .domain([0, d3.max(p.data, function(d) { return d[0]; })])
              .range([0, dimensions.width]);
    
    var y = d3.scale.linear()
    	      .domain([0, d3.max(p.data, function(d) { return d[1]; })])
    	      .range([ dimensions.height, 0 ]);
    
    var chartContainer = p.parent.append('g')
	  .attr('transform', 'translate(' + p.margin.left + ',' + p.margin.top + ')')
	  .attr('width', dimensions.width)
	  .attr('height', dimensions.height)
	  .attr('class', 'main')   

    // draw the x axis
    var xAxis = d3.svg.axis()
      .scale(x)
      .orient('bottom');


    var brush = d3.svg.brush()
      .x(x)
      .y(y)
      .on("brushstart", brushstart)
      .on("brush", brushmove)
      .on("brushend", brushend);
    
    chartContainer.append('g')
      .attr('transform', 'translate(0,' + dimensions.height + ')')
      .attr('class', 'main axis date')
      .call(xAxis);

    // draw the y axis
    var yAxis = d3.svg.axis()
      .scale(y)
      .orient('left');

    chartContainer.append('g')
      .attr('transform', 'translate(0,0)')
      .attr('class', 'main axis date')
      .call(yAxis);

    var g = chartContainer.append("svg:g")
      .call(brush);
    
    var dots = g.selectAll("scatter-dots")
      .data(p.data)
      .enter().append("svg:circle")
        .attr("cx", function (d) { return x(d[0]); } )
        .attr("cy", function (d) { return y(d[1]); } )
        .attr("r", p.unselectedRadius)
        .style("fill", p.unselectedColor)
        .on("mouseover", function(d)
        {
          mouseOverHandler(this, d, true);
        })
        .on("mouseout", function(d)
        {
          mouseOutHandler(this, d, true);
        });

    // Brushing:
    function brushstart()
    {
      console.log("brush start()");
      console.log("brush empty? " + brush.empty());
      g.selectAll("circle")
        .attr("r", p.unselectedRadius)
        .style("fill", p.unselectedColor);
    }

    function brushmove(b)
    {
      console.log("brush move(" + brush.extent() + ")");
    }
    
    function brushend()
    {
      console.log("brush end()");
      var extent = brush.extent();
      
      var active = g.selectAll("circle").filter(function(d,i)
        {
    	  //console.dir(extent);
    	  //console.dir(d);
    	  if (d[0] >= extent[0][0] && d[0] <= extent[1][0] &&
   			  d[1] >= extent[0][1] && d[1] <= extent[1][1])
    	  {
    		return this;
    	  }
    	  return null;
    	}
      )
      .attr("r", p.selectedRadius)
      .style("fill", p.selectedColor);
      
      for (var i=0; i<p.listeners.length; i++)
      {
        p.listeners[i].handleEvent("brush", active, p.data, false);
      }
    }
    
	// This function handles the mouseover event.
   	//
   	// data will contain the row experiencing mouseover.
    // originator will be true if this is being called by the chart which
    //   is originating this event, false otherwise.  This is required to
    //   avoid recursion of listeners notifying originators.
    mouseOverHandler = function (node, data, originator)
    {
      //console.log("MOUSEOVER: node=" + node + ", data=" + data + ", originator=" + originator);    	      
      // If we're the originator of this event, notify our listeners to
      // update themselves in turn.
      
      // Pick yourself so you have access to all the D3 goodies you get
      // through selection.
      d3.select(node)
        .style("fill", p.selectedColor)
        .attr("r", p.selectedRadius);
      
      g.append("text")
      .attr("x", x(data[0]))
      .attr("y", y(data[1]) - 10)
      .attr("dy", ".35m")
      .style("font-size", 14)
      .attr("text-anchor", "top")
      .attr("fill", "black")
      .text(function(d) { return data[1];});
      
      if (originator)
      {
        for (var i=0; i<p.listeners.length; i++)
        {
          p.listeners[i].handleEvent("mouseover", node, data, false);
        }
      }
    }
    
    mouseOutHandler = function (node, data, originator)
    {
      //console.log("MOUSEOUT: node=" + node + ", data=" + data + ", originator=" + originator);    	      
      // If we're the originator of this event, notify our listeners to
      // update themselves in turn.

      g.selectAll("text").remove();
      d3.select(node)
        .style("fill", p.unselectedColor)
        .attr("r", p.unselectedRadius);

      if (originator)
      {
        for (var i=0; i<p.listeners.length; i++)
        {
          p.listeners[i].handleEvent("mouseout", node, data, false);
        }
      }
    }
  }

  // This is the public on mouseover function which is visible to others.
  chart.handleEvent = function(type, node, data, originator)
  {
    if (type == "mouseover")
    {
      mouseOverHandler(node, data, originator);
    }
    else if (type == "mouseout")
    {
      mouseOutHandler(node, data, originator);
    }
    else if (type == "brush")
    {
      brushHandler(node, data, originator);
    }
  }

  // Use this routine to retrieve and update attributes.
  chart.attr = function(name, value)
  {
    // When no arguments are given, we return the current value of the
    // attribute back to the caller.
    if (arguments.length == 1)
    {
      return p[name];
    }
    // Given 2 arguments we set the name=value.
    else if (arguments.length == 2)
    {
      p[name] = value;
    }
    
    // Return the chart object back so we can chain the operations together.
    return chart;
  }

  // This routine supports the update operation for this chart.  This is
  // applicable when the chart should be partially updated.
  chart.update = function()
  {
  }
  
  return chart;
}
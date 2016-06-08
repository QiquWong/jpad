/* Dex Utilities */
function colorToHex(color)
{
  if (color.substr(0, 1) === '#')
  {
    return color;
  }
  //console.log("COLOR: " + color)
  var digits = /rgb\((\d+),(\d+),(\d+)\)/.exec(color);
  //console.log("DIGITS: " + digits);
  var red = parseInt(digits[1]);
  var green = parseInt(digits[2]);
  var blue = parseInt(digits[3]);
    
  var rgb = blue | (green << 8) | (red << 16);
  return '#' + rgb.toString(16);
};

function isNumber(n)
{
  return !isNaN(parseFloat(n)) && isFinite(n);
}

function getHeader(data)
{
  return data[0];
}

function getColumn(data, columnNumber)
{
  var values = [];
  
  for (var i=1; i<data.length; i++)
  {
    values.push(data[i][columnNumber]);
  }
  
  return values;
}

function getNumericHeaders(data)
{
  var header = getHeader(data);
  var possibleNumeric = {};
  var i, j;
  var numericHeaders = [];

  for (i=0; i<header.length; i++)
  {
    possibleNumeric[header[i]] = true;
  }
  
  // Iterate thru the data, skip the header.
  for (i=1; i<data.length; i++)
  {
	for (j=0; j<data[i].length && j<header.length; j++)
	{
	  if (possibleNumeric[header[j]] && !isNumber(data[i][j]))
	  {
		possibleNumeric[header[j]] = false;
	  }
	}
  }
  
  for (i=0; i<header.length; i++)
  {
	if (possibleNumeric[header[i]])
	{
	  numericHeaders.push(header[i]);
	}
  }

  return numericHeaders;
}

function getNumericIndices(data)
{
  var header = getHeader(data);
  var possibleNumeric = {};
  var i, j;
  var numericIndices = [];

  for (i=0; i<header.length; i++)
  {
    possibleNumeric[header[i]] = true;
  }
  
  // Iterate thru the data, skip the header.
  for (i=1; i<data.length; i++)
  {
	for (j=0; j<data[i].length && j<header.length; j++)
	{
	  if (possibleNumeric[header[j]] && !isNumber(data[i][j]))
	  {
		possibleNumeric[header[j]] = false;
	  }
	}
  }
  
  for (i=0; i<header.length; i++)
  {
	if (possibleNumeric[header[i]])
	{
	  numericIndices.push(i);
	}
  }

  return numericIndices;
}

function isNumericColumn(data, columnNum)
{
  for (var i=1; i<data.length; i++)
  {
	if (!isNumber(data[i][columnNum]))
	{
	  return false;
	}
  }
  return true;
}

function getMax(data, columnNum)
{
  var maxValue = data[1][columnNum];

  if (isNumericColumn(data, columnNum))
  {
	maxValue = parseFloat(data[1][columnNum]);
	for (var i=2; i<data.length; i++)
    {
      if (maxValue < parseFloat(data[i][columnNum]))
      {
        maxValue = parseFloat(data[i][columnNum]);
      }
    }
  }
  else
  {
    for (var i=2; i<data.length; i++)
    {
      if (maxValue < data[i][columnNum])
      {
        maxValue = data[i][columnNum];
      }
    }
  }
  
  return maxValue;
}

function getMin(data, columnNum)
{
  var minValue = data[1][columnNum];

  if (isNumericColumn(data, columnNum))
  {
	minValue = parseFloat(data[1][columnNum]);
	for (var i=2; i<data.length; i++)
    {
      if (minValue > parseFloat(data[i][columnNum]))
      {
        minValue = parseFloat(data[i][columnNum]);
      }
    }
  }
  else
  {
    for (var i=2; i<data.length; i++)
    {
      if (minValue > data[i][columnNum])
      {
        minValue = data[i][columnNum];
      }
    }
  }
  
  return minValue;
}

function getJsonFromCsv(data)
{
  var header = getHeader(data);
  var i,j;
  var json = [];

  for (i=1;i<data.length;i++)
  {
	var row = {};
	for (j=0;j<data[i].length && j<header.length;j++)
	{
	  row[header[j]] = data[i][j];
	}
	json.push(row);
  }
  
  return json;
}

function getFill(colorScheme, numColors)
{
  if (colorScheme == "1")
  {
   return d3.scale.category10();
  }
  else if (colorScheme == "2")
  {
    return d3.scale.category20();
  }
  else if (colorScheme == "3")
  {
    return d3.scale.category20b();
  }
  else if (colorScheme == "4")
  {
    return d3.scale.category20c();
  }
  else if (colorScheme == "HiContrast")
  {
    return d3.scale.ordinal().range(colorbrewer[colorScheme][9]);
  }
  else if (colorScheme in colorbrewer)
  {
    //console.log("LENGTH: " + len);
    
    var effColors = Math.pow(2, Math.ceil(Math.log(numColors) / Math.log(2)));
    //console.log("EFF LENGTH: " + len);

    // Find the best cmap:
    if (effColors > 128)
    {
      effColors = 256;
    }

    for (var c=effColors; c >= 2; c--)
    {
      if (colorbrewer[colorScheme][c])
      {
        return d3.scale.ordinal().range(colorbrewer[colorScheme][c]);
      }
    }
    for (var c=effColors; c <= 256; c++)
    {
      if (colorbrewer[colorScheme][c])
      {
        return d3.scale.ordinal().range(colorbrewer[colorScheme][c]);
      }
    }
    return d3.scale.category20();
  }
  else
  {
    return d3.scale.category20();
  }
}

function OrdinalHorizontalLegend(config)
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

function csvToMapArray(header, data)
{
  var mapArray = [];
  
  for (var i=0; i<data.length; i++)
  {
	var row = {};
    for (var j=0; j<header.length; j++)
  	{
      row[header[j]] = data[i][j]
    }
    mapArray.push(row);
  }
  
  return mapArray;
}
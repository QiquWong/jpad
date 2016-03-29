package sandbox.adm;

import java.util.ArrayList;
import java.util.List;

import org.treez.javafxd3.d3.svg.SymbolType;
import sandbox.adm.javafxd3.test.D3Plotter;
import sandbox.adm.javafxd3.test.D3Plotter.Margin;

public class D3PlotterOptions {

	private Margin margin;

	private int widthGraph;
	private int heightGraph;

	private int widthPageSVG;
	private int heightPageSVG;

	private String graphBackgroundColor;
	private Double graphBackgroundOpacity;

	private double xtickPadding;
	private double ytickPadding;

	private String axisLineColor = "black";
	private String axisLineStrokeWidth = "3.0px";

	private String xGridLineColor;
	private String xGridLineDashArray;
	private String xGridLineStrokeWidth;

	private String yGridLineColor;
	private String yGridLineDashArray;
	private String yGridLineStrokeWidth;
	
	private boolean showGrid;
	private boolean showXGrid;
	private boolean showYGrid;

	private boolean autoRangeX;
	private boolean autoRangeY;
	double xMin;
	double xMax;
	double yMin;
	double yMax;

	private String title;
	private String xLabel;
	private String yLabel;
	
	private int symbolSize;
	private SymbolType symbolType;
	private boolean autoSymbolType;
	private List<SymbolType> listSymbolType;
	private String symbolStyle;
	private boolean autoSymbolStyle;
	private List<String> listSymbolStyle;

	private int lineSize;
	private String lineStyle;
	private boolean autoLineStyle;
	private List<String> listLineStyle;
	
	private boolean plotArea;
	private List<Boolean> listPlotArea;
	private boolean  autoPlotArea;
	private String areaStyle;
	private Double areaOpacity;

	private boolean showSymbols;

	private boolean showLegend;
	private boolean autoLegend;
	
	private List<String> listLegendItems;

	// TODO: make list of settings to be used for multi-line plots
	
	private List<Integer> listSymbolSize;
	private List<Integer> listLineSize;
	private List<String> listAreaStyle;

	private List<Boolean> listShowSymbols;
	
	// Builder pattern via a nested public static class

	public static class D3PlotterOptionsBuilder {

		// required parameters
		// ...

		// optional parameters ... defaults
		private Margin _margin = new D3Plotter.Margin(40, 20, 50, 60);  // t r b l

		private int _widthPageSVG = 700;
		private int _heightPageSVG = 600;

		private int _widthGraph = _widthPageSVG - _margin.left - _margin.right;
		private int _heightGraph = _heightPageSVG - _margin.top - _margin.bottom;

		private String _graphBackgroundColor = "lightblue";
		private Double _graphBackgroundOpacity = 0.8;

		private double _xtickPadding = 12.0;
		private double _ytickPadding = 8.0;

		private String _axisLineColor = "black";
		private String _axisLineStrokeWidth = "3.0px";

		private String _xGridLineColor = "gray";
		private String _xGridLineDashArray = "15,2";
		private String _xGridLineStrokeWidth = "0.8px";

		private String _yGridLineColor = "gray";
		private String _yGridLineDashArray = "15,2";
		private String _yGridLineStrokeWidth = "0.8px";

		private boolean _showGrid = true;
		private boolean _showXGrid = true;
		private boolean _showYGrid = true;
		
		private boolean _autoRangeX = true;
		private boolean _autoRangeY = true;
		double _xMin = 0.0;
		double _xMax = 1.0;
		double _yMin = 0.0;
		double _yMax = 1.0;

		private String _title = "This is the title";
		private String _xLabel = "x";
		private String _yLabel = "y";
		
		private int _symbolSize = 64;
		private SymbolType _symbolType = SymbolType.CIRCLE;
		private boolean _autoSymbolType = true;
		private List<SymbolType> _listSymbolType = new ArrayList<>();
		
		private String _symbolStyle = "fill:red; stroke:blue; stroke-width:2";
		private boolean _autoSymbolStyle = true;
		private List<String> _listSymbolStyle = new ArrayList<>();

		private int _lineSize = 4;
		private String _lineStyle = "fill:none; stroke:black; stroke-width:2";
		private boolean _autoLineStyle = true;
		private List<String> _listLineStyle = new ArrayList<>();
		
		private boolean _plotArea = false;
		private List<Boolean> _listPlotArea = new ArrayList<>();
		private boolean _autoPlotArea = true;
		private String _areaStyle = "fill:green;";
		private Double _areaOpacity = 0.8;

		private boolean _showSymbols = true;
		private boolean _showLegend = true;
		private boolean _autoLegend = true;

		private List<String> _legendItems = new ArrayList<>();

		private List<Integer> _listSymbolSize = new ArrayList<>();
		private List<Integer> _listLineSize = new ArrayList<>();
		private List<Double> _listAreaOpacity = new ArrayList<>();
		private List<String> _listAreaStyle = new ArrayList<>();
		private List<Boolean> _listShowSymbols = new ArrayList<>();
		
		public D3PlotterOptionsBuilder( /* required parameters here*/ ){
		}

		public D3PlotterOptionsBuilder margins(int t, int r, int b, int l) {
			_margin.top = t;
			_margin.right = r;
			_margin.bottom = b;
			_margin.left = l;
			_widthGraph = _widthPageSVG - _margin.left - _margin.right;
			_heightGraph = _heightPageSVG - _margin.top - _margin.bottom;
			return this;
		}

		public D3PlotterOptionsBuilder widthGraph(int w) {
			_widthGraph = w;
			_widthGraph = _widthPageSVG - _margin.left - _margin.right;
			return this;
		}
		public D3PlotterOptionsBuilder heightGraph(int h) {
			_heightGraph = h;
			_heightGraph = _heightPageSVG - _margin.top - _margin.bottom;
			return this;
		}

		public D3PlotterOptionsBuilder graphBackgroundColor(String col) {
			_graphBackgroundColor = col;
			return this;
		}

		public D3PlotterOptionsBuilder graphBackgroundOpacity(Double opacity) {
			_graphBackgroundOpacity = opacity;
			return this;
		}

		public D3PlotterOptionsBuilder xtickPadding(double xpadding) {
			_xtickPadding = xpadding;
			return this;
		}

		public D3PlotterOptionsBuilder ytickPadding(double ypadding) {
			_ytickPadding = ypadding;
			return this;
		}

		public D3PlotterOptionsBuilder axisLineColor(String val) {
			_axisLineColor = val;
			return this;
		}

		public D3PlotterOptionsBuilder axisLineStrokeWidth(String val) {
			_axisLineStrokeWidth = val;
			return this;
		}


		public D3PlotterOptionsBuilder xGridLineColor(String val) {
			_xGridLineColor = val;
			return this;
		}

		public D3PlotterOptionsBuilder xGridLineDashArray(String val) {
			_xGridLineDashArray = val;
			return this;
		}

		public D3PlotterOptionsBuilder xGridLineStrokeWidth(String val) {
			_xGridLineStrokeWidth = val;
			return this;
		}

		public D3PlotterOptionsBuilder yGridLineColor(String val) {
			_yGridLineColor = val;
			return this;
		}

		public D3PlotterOptionsBuilder yGridLineDashArray(String val) {
			_yGridLineDashArray = val;
			return this;
		}

		public D3PlotterOptionsBuilder yGridLineStrokeWidth(String val) {
			_yGridLineStrokeWidth = val;
			return this;
		}

		public D3PlotterOptionsBuilder showGrid(boolean b) {
			_showGrid = b;
			_showXGrid = b;
			_showYGrid = b;
			return this;
		}

		public D3PlotterOptionsBuilder showXGrid(boolean b) {
			_showXGrid = b;
			return this;
		}

		public D3PlotterOptionsBuilder showYGrid(boolean b) {
			_showYGrid = b;
			return this;
		}
		
		public D3PlotterOptionsBuilder title(String val) {
			_title = val;
			return this;
		}

		public D3PlotterOptionsBuilder xLabel(String val) {
			_xLabel = val;
			return this;
		}

		public D3PlotterOptionsBuilder yLabel(String val) {
			_yLabel = val;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolSize(int s) {
			_symbolSize = s;
			return this;
		}

		public D3PlotterOptionsBuilder symbolType(SymbolType t) {
			_symbolType = t;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolStyle(String style) {
			_symbolStyle = style;
			return this;
		}

		public D3PlotterOptionsBuilder lineSize(int s) {
			_lineSize = s;
			return this;
		}

		public D3PlotterOptionsBuilder lineStyle(String style) {
			_lineStyle = style;
			return this;
		}

		public D3PlotterOptionsBuilder areaStyle(String style) {
			_areaStyle = style;
			return this;
		}

		public D3PlotterOptionsBuilder areaOpacity(Double opacity) {
			_areaOpacity = opacity;
			return this;
		}

		public D3PlotterOptionsBuilder showSymbols(boolean val) {
			_showSymbols = val;
			return this;
		}
		public D3PlotterOptionsBuilder plotArea(boolean val) {
			_plotArea = val;
			return this;
		}
		public D3PlotterOptionsBuilder showLegend(boolean val) {
			_showLegend = val;
			return this;
		}
		public D3PlotterOptionsBuilder autoLegend(boolean val) {
			_autoLegend = val;
			return this;
		}

		public D3PlotterOptionsBuilder autoRangeX(boolean val) {
			_autoRangeX = val;
			return this;
		}
		public D3PlotterOptionsBuilder autoRangeY(boolean val) {
			_autoRangeY = val;
			return this;
		}
		public D3PlotterOptionsBuilder autoRange(boolean val) {
			_autoRangeX = val;
			_autoRangeY = val;
			return this;
		}

		public D3PlotterOptionsBuilder xMin(double val) {
			_autoRangeX = false;
			_xMin = val;
			return this;
		}

		public D3PlotterOptionsBuilder xMax(double val) {
			_autoRangeX = false;
			_xMax = val;
			return this;
		}

		public D3PlotterOptionsBuilder yMin(double val) {
			_autoRangeY = false;
			_yMin = val;
			return this;
		}

		public D3PlotterOptionsBuilder yMax(double val) {
			_autoRangeY = false;
			_yMax = val;
			return this;
		}

		public D3PlotterOptionsBuilder xRange(double x1, double x2) {
			_autoRangeX = false;
			_xMin = x1; _xMax = x2;
			return this;
		}

		public D3PlotterOptionsBuilder yRange(double y1, double y2) {
			_autoRangeY = false;
			_yMin = y1; _yMax = y2;
			return this;
		}

		public D3PlotterOptionsBuilder axesRange(double x1, double x2, double y1, double y2) {
			_autoRangeX = false;
			_autoRangeY = false;
			_xMin = x1; _xMax = x2;
			_yMin = y1; _yMax = y2;
			return this;
		}

		public D3PlotterOptionsBuilder plotAreas(
				Boolean... dos ) {
			for (Boolean b : dos)
				_listPlotArea.add(b);
			
			// if user passes strings, auto-legend feature is disabled
			_autoPlotArea = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder legendItems(
				String... items ) {
			for (String s : items)
				_legendItems.add(s);
			
			// if user passes strings, auto-legend feature is disabled
			_autoLegend = false;
			return this;
		}

		public D3PlotterOptionsBuilder symbolTypes(
				SymbolType... types ) {
			for (SymbolType t : types)
				_listSymbolType.add(t);
			
			// if user passes strings, auto-* feature is disabled
			_autoSymbolType = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolStyles(
				String... styles ) {
			for (String s : styles)
				_listSymbolStyle.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoSymbolStyle = false;
			return this;
		}

		public D3PlotterOptionsBuilder lineStyles(
				String... styles ) {
			for (String s : styles)
				_listLineStyle.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoLineStyle = false;
			return this;
		}
		
		public D3PlotterOptions build() {
			return new D3PlotterOptions(this);
		}

	} // end-of-builder

	private D3PlotterOptions(D3PlotterOptionsBuilder builder) {
		this.margin = builder._margin;

		this.widthGraph = builder._widthGraph;
		this.heightGraph = builder._heightGraph;

		this.widthPageSVG = builder._widthPageSVG;
		this.heightPageSVG = builder._heightPageSVG;

		this.graphBackgroundColor = builder._graphBackgroundColor;
		this.graphBackgroundOpacity = builder._graphBackgroundOpacity;

		this.xtickPadding = builder._xtickPadding;
		this.ytickPadding = builder._ytickPadding;

		this.axisLineColor = builder._axisLineColor;
		this.axisLineStrokeWidth = builder._axisLineStrokeWidth;

		this.xGridLineColor = builder._xGridLineColor;
		this.xGridLineDashArray = builder._xGridLineDashArray;
		this.xGridLineStrokeWidth = builder._xGridLineStrokeWidth;

		this.yGridLineColor = builder._yGridLineColor;
		this.yGridLineDashArray = builder._yGridLineDashArray;
		this.yGridLineStrokeWidth = builder._yGridLineStrokeWidth;

		this.showGrid = builder._showGrid;
		this.showXGrid = builder._showXGrid;
		this.showYGrid = builder._showYGrid;
		
		this.symbolSize = builder._symbolSize;
		this.symbolType = builder._symbolType;
		this.listSymbolType = builder._listSymbolType;
		this.autoSymbolType = builder._autoSymbolType;
		this.symbolStyle = builder._symbolStyle;
		this.autoSymbolStyle = builder._autoSymbolStyle;
		this.listSymbolStyle = builder._listSymbolStyle;

		this.lineSize = builder._lineSize;
		this.lineStyle = builder._lineStyle;
		this.autoLineStyle = builder._autoLineStyle;
		this.listLineStyle = builder._listLineStyle;

		this.autoPlotArea = builder._autoPlotArea;		
		this.listPlotArea = builder._listPlotArea;		
		
		this.areaStyle = builder._areaStyle;
		this.areaOpacity = builder._areaOpacity;

		this.showSymbols = builder._showSymbols;
		this.plotArea = builder._plotArea;
		this.showLegend = builder._showLegend;
		this.autoLegend = builder._autoLegend;

		this.autoRangeX = builder._autoRangeX;
		this.autoRangeY = builder._autoRangeY;
		this.xMin = builder._xMin;
		this.xMax = builder._xMax;
		this.yMin = builder._yMin;
		this.yMax = builder._yMax;

		this.title = builder._title;
		this.xLabel = builder._xLabel;
		this.yLabel = builder._yLabel;
		
		this.listLegendItems = builder._legendItems;

	}

	public Margin getMargin() {
		return margin;
	}


	public int getWidthGraph() {
		return widthGraph;
	}


	public int getHeightGraph() {
		return heightGraph;
	}


	public int getWidthPageSVG() {
		return widthPageSVG;
	}


	public int getHeightPageSVG() {
		return heightPageSVG;
	}

	public String getGraphBackgroundColor() {
		return graphBackgroundColor;
	}

	public Double getGraphBackgroundOpacity() {
		return graphBackgroundOpacity;
	}

	public double getXtickPadding() {
		return xtickPadding;
	}

	public double getYtickPadding() {
		return ytickPadding;
	}

	public String getXGridLineColor() {
		return xGridLineColor;
	}

	public String getAxisLineColor() {
		return axisLineColor;
	}

	public String getAxisLineStrokeWidth() {
		return axisLineStrokeWidth;
	}

	public String getXGridLineDashArray() {
		return xGridLineDashArray;
	}

	public String getXGridLineStrokeWidth() {
		return xGridLineStrokeWidth;
	}

	public String getYGridLineColor() {
		return yGridLineColor;
	}

	public String getYGridLineDashArray() {
		return yGridLineDashArray;
	}

	public String getYGridLineStrokeWidth() {
		return yGridLineStrokeWidth;
	}

	public boolean isShowGrid() {
		return showGrid;
	}
	
	public boolean isShowXGrid() {
		return showXGrid;
	}
	
	public boolean isShowYGrid() {
		return showYGrid;
	}	

	public String getTitle() {
		return title;
	}

	public String getXLabel() {
		return xLabel;
	}
	
	public String getYLabel() {
		return yLabel;
	}

	public int getSymbolSize() {
		return symbolSize;
	}

	public SymbolType getSymbolType() {
		return symbolType;
	}

	public boolean isAutoSymbolType() {
		return autoSymbolType;
	}	

	public List<SymbolType> getSymbolTypes() {
		return listSymbolType;
	}
	
	public String getSymbolStyle() {
		return symbolStyle;
	}

	public boolean isAutoSymbolStyle() {
		return autoSymbolStyle;
	}	
	
	public List<String> getSymbolStyles() {
		return listSymbolStyle;
	}

	public int getLineSize() {
		return lineSize;
	}


	public String getLineStyle() {
		return lineStyle;
	}

	public boolean isAutoLineStyle() {
		return autoLineStyle;
	}	

	public List<String> getLineStyles() {
		return listLineStyle;
	}
	
	public String getAreaStyle() {
		return areaStyle;
	}

	public Double getAreaOpacity() {
		return areaOpacity;
	}

	public boolean isShowSymbols() {
		return showSymbols;
	}


	public boolean isPlotArea() {
		return plotArea;
	}

	public boolean isAutoPlotArea() {
		return autoPlotArea;
	}
	
	public List<Boolean> getPlotAreas() {
		return listPlotArea;
	}
	
	public boolean isShowLegend() {
		return showLegend;
	}

	public boolean isAutoLegend() {
		return autoLegend;
	}
	
	public boolean isAutoRangeX() {
		return autoRangeX;
	}

	public boolean isAutoRangeY() {
		return autoRangeY;
	}

	public double getXMin() {
		return xMin;
	}
	public double getXMax() {
		return xMax;
	}
	public double getYMin() {
		return yMin;
	}
	public double getYMax() {
		return yMax;
	}

	public List<String> getLegendItems() {
		return listLegendItems;
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder()
				.append("D3Plotter options\n") //
				.append("\tMargins (t, r, b, l): " + margin.top + ", " + margin.right + ", " +  margin.bottom + ", " + margin.left + "\n") //  t r b l
				.append("\t(width, height) of graph: " + widthGraph + ", " + heightGraph + "\n") //
				.append("\t(width, height) of SVG page: " + widthPageSVG + ", " + heightPageSVG + "\n") //
				.append("\tgraph background color: \"" + graphBackgroundColor + "\"\n") //
				.append("\taxis line stroke (color): \"" + axisLineColor + "\"\n") //
				.append("\taxis line stroke width: \"" + axisLineStrokeWidth + "\"\n") //
				.append("\tx-axis grid line stroke (color): \"" + xGridLineColor + "\"\n") //
				.append("\tx-axis grid line stroke-width: \"" + xGridLineStrokeWidth + "\"\n") //
				.append("\tx-axis grid line dash-array: \"" + xGridLineDashArray + "\"\n") //
				.append("\ty-axis grid line stroke (color): \"" + yGridLineColor + "\"\n") //
				.append("\ty-axis grid line stroke-width: \"" + yGridLineStrokeWidth + "\"\n") //
				.append("\ty-axis grid line dash-array: \"" + yGridLineDashArray + "\"\n") //
				.append("\tshow grid: " + showGrid + "\n") //
				.append("\tshow x-grid: " + showXGrid + "\n") //
				.append("\tshow y-grid: " + showYGrid + "\n") //
				.append("\tx-axis labels padding: " + xtickPadding + "\n") //
				.append("\ty-axis labels padding: " + ytickPadding + "\n") //
				.append("\taxis auto-range x: " + autoRangeX + "\n") //
				.append("\taxis auto-range y: " + autoRangeY + "\n") //
				.append("\tPlot title: " + title + "\n") //
				.append("\tPlot x label: " + xLabel + "\n") //
				.append("\tPlot y label: " + yLabel + "\n") //
				.append("\tsymbol size: " + symbolSize + "\n") //
				.append("\tsymbol type: \"" + symbolType + "\"\n") //
				.append("\tauto symbol type: \"" + autoSymbolType + "\"\n") //
				.append("\tsymbol types: " + listSymbolType + "\n") //
				.append("\tsymbol style: \"" + symbolStyle + "\"\n") //
				.append("\tauto symbol style: \"" + autoSymbolStyle + "\"\n") //
				.append("\tsymbol styles: " + listSymbolStyle + "\n") //
				.append("\tline style: \"" + lineStyle + "\"\n") //
				.append("\tauto line style: \"" + autoLineStyle + "\"\n") //
				.append("\tline styles: " + listLineStyle + "\n") //
				.append("\tarea style: \"" + areaStyle + "\"\n") //
				.append("\tshow symbols: " + showSymbols + "\n") //
				.append("\tplot area: " + plotArea + "\n") //
				.append("\tauto plot area: \"" + autoPlotArea + "\"\n") //
				.append("\tplot areas: " + listPlotArea + "\n") //
				.append("\tshow legend: " + showLegend + "\n") //
				.append("\tauto legend: " + autoLegend + "\n") //
				.append("\tlegends items: " + listLegendItems + "\n") //
				;
		return sb.toString();
	}

}

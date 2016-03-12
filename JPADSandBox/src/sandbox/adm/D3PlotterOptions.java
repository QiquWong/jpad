package sandbox.adm;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.treez.javafxd3.d3.core.Selection;

import aircraft.components.liftingSurface.adm.Airfoil;
import aircraft.components.liftingSurface.adm.LiftingSurfacePanel;
import aircraft.components.liftingSurface.adm.LiftingSurfacePanel.LiftingSurfacePanelBuilder;
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

	private int symbolSize;
	private String symbolStyle;

	private int lineSize;
	private String lineStyle;

	private String areaStyle;
	private Double areaOpacity;

	private boolean showSymbols;
	private boolean plotArea;
	private boolean showLegend;

	private boolean autoRangeX;
	private boolean autoRangeY;
	double xMin;
	double xMax;
	double yMin;
	double yMax;
	
	List<String> legendItems;

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

		private String _yGridLineColor = "red";
		private String _yGridLineDashArray = "15,2";
		private String _yGridLineStrokeWidth = "0.8px";

		private int _symbolSize = 64;
		private String _symbolStyle = "fill:red; stroke:blue; stroke-width:2";

		private int _lineSize = 4;
		private String _lineStyle = "fill:none; stroke:red; stroke-width:2";

		private String _areaStyle = "fill:green;";
		private Double _areaOpacity = 0.8;

		private boolean _showSymbols = true;
		private boolean _plotArea = false;
		private boolean _showLegend = true;

		private boolean _autoRangeX = true;
		private boolean _autoRangeY = true;
		double _xMin = 0.0;
		double _xMax = 1.0;
		double _yMin = 0.0;
		double _yMax = 1.0;

		List<String> _legendItems = new ArrayList<>();

		
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

		public D3PlotterOptionsBuilder symbolSize(int s) {
			_symbolSize = s;
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
		
		public D3PlotterOptionsBuilder legendItems(
				String... items ) {
			for (String s : items)
				_legendItems.add(s);
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

		this.symbolSize = builder._symbolSize;
		this.symbolStyle = builder._symbolStyle;

		this.lineSize = builder._lineSize;
		this.lineStyle = builder._lineStyle;

		this.areaStyle = builder._areaStyle;
		this.areaOpacity = builder._areaOpacity;

		this.showSymbols = builder._showSymbols;
		this.plotArea = builder._plotArea;
		this.showLegend = builder._showLegend;

		this.autoRangeX = builder._autoRangeX;
		this.autoRangeY = builder._autoRangeY;
		this.xMin = builder._xMin;
		this.xMax = builder._xMax;
		this.yMin = builder._yMin;
		this.yMax = builder._yMax;
		
		this.legendItems = builder._legendItems;

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

	public int getSymbolSize() {
		return symbolSize;
	}


	public String getSymbolStyle() {
		return symbolStyle;
	}


	public int getLineSize() {
		return lineSize;
	}


	public String getLineStyle() {
		return lineStyle;
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

	public boolean isShowLegend() {
		return showLegend;
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
		return legendItems;
	}

	@Override public String toString() {
		return
				"D3Plotter options\n" //
				+ "\tMargins (t, r, b, l): " + margin.top + ", " + margin.right + ", " +  margin.bottom + ", " + margin.left + "\n" //  t r b l
				+ "\t(width, height) of graph: " + widthGraph + ", " + heightGraph + "\n" //
				+ "\t(width, height) of SVG page: " + widthPageSVG + ", " + heightPageSVG + "\n" //
				+ "\tgraph background color: \"" + graphBackgroundColor + "\"\n" //
				+ "\taxis line stroke (color): \"" + axisLineColor + "\"\n" //
				+ "\taxis line stroke width: \"" + axisLineStrokeWidth + "\"\n" //
				+ "\tx-axis grid line stroke (color): \"" + xGridLineColor + "\"\n" //
				+ "\tx-axis grid line stroke-width: \"" + xGridLineStrokeWidth + "\"\n" //
				+ "\tx-axis grid line dash-array: \"" + xGridLineDashArray + "\"\n" //
				+ "\ty-axis grid line stroke (color): \"" + yGridLineColor + "\"\n" //
				+ "\ty-axis grid line stroke-width: \"" + yGridLineStrokeWidth + "\"\n" //
				+ "\ty-axis grid line dash-array: \"" + yGridLineDashArray + "\"\n" //
				+ "\tx-axis labels padding: " + xtickPadding + "\n" //
				+ "\ty-axis labels padding: " + ytickPadding + "\n" //
				+ "\taxis auto-range x: " + autoRangeX + "\n" //
				+ "\taxis auto-range y: " + autoRangeY + "\n" //
				+ "\tsymbol size: " + symbolSize + "\n" //
				+ "\tsymbol style: \"" + symbolStyle + "\"\n" //
				+ "\tline style: \"" + lineStyle + "\"\n" //
				+ "\tarea style: \"" + areaStyle + "\"\n" //
				+ "\tshow symbols: " + showSymbols + "\n" //
				+ "\tplot area: " + plotArea + "\n" //
				+ "\tshow legend: " + showLegend + "\n" //
				;
	}

}

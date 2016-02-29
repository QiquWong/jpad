package sandbox.adm;

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

	private double xtickPadding;
	private double ytickPadding;

	private int symbolSize;
	private String symbolStyle;

	private int lineSize;
	private String lineStyle;

	private String areaStyle;

	private boolean showSymbols;
	private boolean plotArea;
	private boolean showLegend;

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

		private double _xtickPadding = 12.0;
		private double _ytickPadding = 8.0;

		private int _symbolSize = 64;
		private String _symbolStyle = "fill:red; stroke:blue; stroke-width:2";

		private int _lineSize = 4;
		private String _lineStyle = "fill:none; stroke:red; stroke-width:2";

		private String _areaStyle = "fill:green;";

		private boolean _showSymbols = true;
		private boolean _plotArea = false;
		private boolean _showLegend = true;

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

		public D3PlotterOptionsBuilder xtickPadding(double xpadding) {
			_xtickPadding = xpadding;
			return this;
		}

		public D3PlotterOptionsBuilder ytickPadding(double ypadding) {
			_ytickPadding = ypadding;
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

		this.xtickPadding = builder._xtickPadding;
		this.ytickPadding = builder._ytickPadding;

		this.symbolSize = builder._symbolSize;
		this.symbolStyle = builder._symbolStyle;

		this.lineSize = builder._lineSize;
		this.lineStyle = builder._lineStyle;

		this.areaStyle = builder._areaStyle;

		this.showSymbols = builder._showSymbols;
		this.plotArea = builder._plotArea;
		this.showLegend = builder._showLegend;

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


	public double getXtickPadding() {
		return xtickPadding;
	}


	public double getYtickPadding() {
		return ytickPadding;
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


	public boolean isShowSymbols() {
		return showSymbols;
	}


	public boolean isPlotArea() {
		return plotArea;
	}


	public boolean isShowLegend() {
		return showLegend;
	}




	@Override public String toString() {
		return
				"D3Plotter options\n" //
				+ "\tMargins (t, r, b, l): " + margin.top + ", " + margin.right + ", " +  margin.bottom + ", " + margin.left + "\n" //  t r b l
				+ "\t(width, height) of graph: " + widthGraph + ", " + heightGraph + "\n" //
				+ "\t(width, height) of SVG page: " + widthPageSVG + ", " + heightPageSVG + "\n" //
				+ "\tgraph background color: \"" + graphBackgroundColor + "\"\n" //
				+ "\tx-axis labels padding: " + xtickPadding + "\n" //
				+ "\ty-axis labels padding: " + ytickPadding + "\n" //
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

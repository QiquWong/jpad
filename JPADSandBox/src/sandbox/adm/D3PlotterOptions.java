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
	private List<Integer> listSymbolSize;
	private boolean autoSymbolSize;
	
	private SymbolType symbolType;
	private boolean autoSymbolType;
	private List<SymbolType> listSymbolType;
	private String symbolStyle;
	private boolean autoSymbolStyle;
	private List<String> listSymbolStyle;

	private int symbolSizeAux;
	private List<Integer> listSymbolSizeAux;
	private boolean autoSymbolSizeAux;
	
	private SymbolType symbolTypeAux;
	private boolean autoSymbolTypeAux;
	private List<SymbolType> listSymbolTypeAux;
	private String symbolStyleAux;
	private boolean autoSymbolStyleAux;
	private List<String> listSymbolStyleAux;
	
	
//	private int lineSize;
//	private List<Integer> listLineSize;
//	private boolean autoLineSize;
	private String lineStyle;
	private boolean autoLineStyle;
	private List<String> listLineStyle;

	private String lineStyleAux;
	private boolean autoLineStyleAux;
	private List<String> listLineStyleAux;
	
	private boolean plotArea;
	private List<Boolean> listPlotArea;
	private boolean  autoPlotArea;
	private String areaStyle;
	private boolean autoAreaStyle;
	private List<String> listAreaStyle;
	private Double areaOpacity;
	private List<Double> listAreaOpacity;
	private boolean autoAreaOpacity;

	private boolean showSymbols;
	private List<Boolean> listShowSymbols;
	private boolean autoShowSymbols;

	private boolean showSymbolsAux;
	private List<Boolean> listShowSymbolsAux;
	private boolean autoShowSymbolsAux;
	
	private boolean showLegend;
	private boolean autoLegend;
	
	private List<String> listLegendItems;

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
		private List<Integer> _listSymbolSize = new ArrayList<>();
		private boolean _autoSymbolSize = true;
		private SymbolType _symbolType = SymbolType.CIRCLE;
		private boolean _autoSymbolType = true;
		private List<SymbolType> _listSymbolType = new ArrayList<>();
		
		private String _symbolStyle = "fill:red; stroke:blue; stroke-width:2";
		private boolean _autoSymbolStyle = true;
		private List<String> _listSymbolStyle = new ArrayList<>();

		private int _symbolSizeAux = 64;
		private List<Integer> _listSymbolSizeAux = new ArrayList<>();
		private boolean _autoSymbolSizeAux = true;
		private SymbolType _symbolTypeAux = SymbolType.CIRCLE;
		private boolean _autoSymbolTypeAux = true;
		private List<SymbolType> _listSymbolTypeAux = new ArrayList<>();
		
		private String _symbolStyleAux = "fill:red; stroke:blue; stroke-width:2";
		private boolean _autoSymbolStyleAux = true;
		private List<String> _listSymbolStyleAux = new ArrayList<>();
		
		
//		private int _lineSize = 4;
//		private List<Integer> _listLineSize = new ArrayList<>();
//		private boolean _autoLineSize = true;
		private String _lineStyle = "fill:none; stroke:black; stroke-width:2";
		private boolean _autoLineStyle = true;
		private List<String> _listLineStyle = new ArrayList<>();

		private String _lineStyleAux = "fill:none; stroke:black; stroke-width:2";
		private boolean _autoLineStyleAux = true;
		private List<String> _listLineStyleAux = new ArrayList<>();
		
		private boolean _plotArea = false;
		private List<Boolean> _listPlotArea = new ArrayList<>();
		private boolean _autoPlotArea = true;
		private String _areaStyle = "fill:green;";
		private List<String> _listAreaStyle = new ArrayList<>();
		private boolean _autoAreaStyle = true;
		private Double _areaOpacity = 0.8;
		private List<Double> _listAreaOpacity = new ArrayList<>();
		private boolean _autoAreaOpacity = true;

		private boolean _showSymbols = true;
		private List<Boolean> _listShowSymbols = new ArrayList<>();
		private boolean _autoShowSymbols = true;

		private boolean _showSymbolsAux = true;
		private List<Boolean> _listShowSymbolsAux = new ArrayList<>();
		private boolean _autoShowSymbolsAux = true;
		
		private boolean _showLegend = true;
		private boolean _autoLegend = true;

		private List<String> _legendItems = new ArrayList<>();

		
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

		public D3PlotterOptionsBuilder widthSVG(int w) {
			_widthPageSVG = w;
			_widthGraph = _widthPageSVG - _margin.left - _margin.right;
			return this;
		}

		public D3PlotterOptionsBuilder heightSVG(int h) {
			_heightPageSVG = h;
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

		public D3PlotterOptionsBuilder symbolSizeAux(int s) {
			_symbolSizeAux = s;
			return this;
		}

		public D3PlotterOptionsBuilder symbolTypeAux(SymbolType t) {
			_symbolTypeAux = t;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolStyleAux(String style) {
			_symbolStyleAux = style;
			return this;
		}
		
		
//		public D3PlotterOptionsBuilder lineSize(int s) {
//			_lineSize = s;
//			return this;
//		}
//		public D3PlotterOptionsBuilder lineSizes(
//				Integer... sizes ) {
//			for (Integer s : sizes)
//				_listLineSize.add(s);
//			
//			// if user passes strings, auto-* feature is disabled
//			_autoLineSize = false;
//			return this;
//		}

		public D3PlotterOptionsBuilder lineStyle(String style) {
			_lineStyle = style;
			return this;
		}

		public D3PlotterOptionsBuilder lineStyleAux(String style) {
			_lineStyleAux = style;
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
			_listShowSymbols.add(val);
			return this;
		}		

		public D3PlotterOptionsBuilder showSymbolsAux(boolean val) {
			_showSymbolsAux = val;
			_listShowSymbolsAux.add(val);
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
			
			// if user passes strings, auto-* feature is disabled
			_autoLegend = false;
			return this;
		}

		public D3PlotterOptionsBuilder symbolSizes(
				Integer... sizes) {
			for (Integer s : sizes)
				_listSymbolSize.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoSymbolSize = false;
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

		public D3PlotterOptionsBuilder showSymbols(
				Boolean... dos ) {
			for (Boolean b : dos)
				_listShowSymbols.add(b);
			
			// if user passes strings, auto-legend feature is disabled
			_autoShowSymbols = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolSizesAux(
				Integer... sizes) {
			for (Integer s : sizes)
				_listSymbolSizeAux.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoSymbolSizeAux = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolTypesAux(
				SymbolType... types ) {
			for (SymbolType t : types)
				_listSymbolTypeAux.add(t);
			
			// if user passes strings, auto-* feature is disabled
			_autoSymbolTypeAux = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder symbolStylesAux(
				String... styles ) {
			for (String s : styles)
				_listSymbolStyleAux.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoSymbolStyleAux = false;
			return this;
		}

		public D3PlotterOptionsBuilder showSymbolsAux(
				Boolean... dos ) {
			for (Boolean b : dos)
				_listShowSymbolsAux.add(b);
			
			// if user passes strings, auto-legend feature is disabled
			_autoShowSymbolsAux = false;
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

		public D3PlotterOptionsBuilder lineStylesAux(
				String... styles ) {
			for (String s : styles)
				_listLineStyleAux.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoLineStyleAux = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder areaStyles(
				String... styles ) {
			for (String s : styles)
				_listAreaStyle.add(s);
			
			// if user passes strings, auto-* feature is disabled
			_autoAreaStyle = false;
			return this;
		}
		
		public D3PlotterOptionsBuilder areaOpacities(
				Double... opacities ) {
			for (Double o : opacities)
				_listAreaOpacity.add(o);
			
			// if user passes strings, auto-* feature is disabled
			_autoAreaOpacity = false;
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
		this.listSymbolSize = builder._listSymbolSize;
		this.autoSymbolSize = builder._autoSymbolSize;
		
		this.symbolType = builder._symbolType;
		this.listSymbolType = builder._listSymbolType;
		this.autoSymbolType = builder._autoSymbolType;
		this.symbolStyle = builder._symbolStyle;
		this.autoSymbolStyle = builder._autoSymbolStyle;
		this.listSymbolStyle = builder._listSymbolStyle;

		this.showSymbols = builder._showSymbols;
		this.listShowSymbols = builder._listShowSymbols;
		
		this.symbolSizeAux = builder._symbolSizeAux;
		this.listSymbolSizeAux = builder._listSymbolSizeAux;
		this.autoSymbolSizeAux = builder._autoSymbolSizeAux;
		
		this.symbolTypeAux = builder._symbolTypeAux;
		this.listSymbolTypeAux = builder._listSymbolTypeAux;
		this.autoSymbolTypeAux = builder._autoSymbolTypeAux;
		this.symbolStyleAux = builder._symbolStyleAux;
		this.autoSymbolStyleAux = builder._autoSymbolStyleAux;
		this.listSymbolStyleAux = builder._listSymbolStyleAux;

		this.showSymbolsAux = builder._showSymbolsAux;
		this.listShowSymbolsAux = builder._listShowSymbolsAux;
		
//		this.lineSize = builder._lineSize;
//		this.listLineSize = builder._listLineSize;
//		this.autoLineSize = builder._autoLineSize;
		
		this.lineStyle = builder._lineStyle;
		this.autoLineStyle = builder._autoLineStyle;
		this.listLineStyle = builder._listLineStyle;

		this.lineStyleAux = builder._lineStyleAux;
		this.autoLineStyleAux = builder._autoLineStyleAux;
		this.listLineStyleAux = builder._listLineStyleAux;
		
		this.plotArea = builder._plotArea;
		this.autoPlotArea = builder._autoPlotArea;		
		this.listPlotArea = builder._listPlotArea;		
		
		this.areaStyle = builder._areaStyle;
		this.autoAreaStyle = builder._autoAreaStyle;
		this.listAreaStyle = builder._listAreaStyle;
		this.areaOpacity = builder._areaOpacity;
		this.listAreaOpacity = builder._listAreaOpacity;
		this.autoAreaOpacity = builder._autoAreaOpacity;

		this.listLegendItems = builder._legendItems;
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

	public boolean isAutoShowSymbols() {
		return autoShowSymbols;
	}	

	public int getSymbolSize() {
		return symbolSize;
	}

	public boolean isAutoSymbolSize() {
		return autoSymbolSize;
	}	

	public List<Integer> getSymbolSizes() {
		return listSymbolSize;
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

	public boolean isShowSymbols() {
		return showSymbols;
	}

	public boolean isAutoSymbolStyle() {
		return autoSymbolStyle;
	}	

	public List<Boolean> getShowSymbols() {
		return listShowSymbols;
	}
	
	public List<String> getSymbolStyles() {
		return listSymbolStyle;
	}

	public boolean isAutoShowSymbolsAux() {
		return autoShowSymbolsAux;
	}	

	public int getSymbolSizeAux() {
		return symbolSizeAux;
	}

	public boolean isAutoSymbolSizeAux() {
		return autoSymbolSizeAux;
	}	

	public List<Integer> getSymbolSizesAux() {
		return listSymbolSizeAux;
	}
	
	public SymbolType getSymbolTypeAux() {
		return symbolTypeAux;
	}

	public boolean isAutoSymbolTypeAux() {
		return autoSymbolTypeAux;
	}	

	public List<SymbolType> getSymbolTypesAux() {
		return listSymbolTypeAux;
	}
	
	public String getSymbolStyleAux() {
		return symbolStyleAux;
	}

	public boolean isShowSymbolsAux() {
		return showSymbolsAux;
	}

	public boolean isAutoSymbolStyleAux() {
		return autoSymbolStyleAux;
	}	

	public List<Boolean> getShowSymbolsAux() {
		return listShowSymbolsAux;
	}
	
	public List<String> getSymbolStylesAux() {
		return listSymbolStyleAux;
	}
	
//	public int getLineSize() {
//		return lineSize;
//	}
//	public boolean isAutoLineSize() {
//		return autoLineSize;
//	}	
//	public List<Integer> getLineSizes() {
//		return listLineSize;
//	}

	public String getLineStyle() {
		return lineStyle;
	}

	public boolean isAutoLineStyle() {
		return autoLineStyle;
	}	

	public List<String> getLineStyles() {
		return listLineStyle;
	}

	public String getLineStyleAux() {
		return lineStyleAux;
	}

	public boolean isAutoLineStyleAux() {
		return autoLineStyleAux;
	}	

	public List<String> getLineStylesAux() {
		return listLineStyleAux;
	}
	
	public String getAreaStyle() {
		return areaStyle;
	}

	public List<String> getAreaStyles() {
		return listAreaStyle;
	}

	public boolean isAutoAreaStyle() {
		return autoAreaStyle;
	}	
	
	public Double getAreaOpacity() {
		return areaOpacity;
	}

	public List<Double> getAreaOpacities() {
		return listAreaOpacity;
	}

	public boolean isAutoAreaOpacity() {
		return autoAreaOpacity;
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
				.append("\tshow symbols: " + showSymbols + "\n") //
				.append("\tauto show symbols: " + autoShowSymbols + "\n") //
				.append("\tshow flags for symbols: " + listShowSymbols + "\n") //
				.append("\tsymbol size: " + symbolSize + "\n") //
				.append("\tauto symbol size: " + autoSymbolSize + "\n") //
				.append("\tsymbol sizes: " + listSymbolSize + "\n") //
				.append("\tsymbol type: " + symbolType + "\n") //
				.append("\tauto symbol type: " + autoSymbolType + "\n") //
				.append("\tsymbol types: " + listSymbolType + "\n") //
				.append("\tsymbol style: \"" + symbolStyle + "\"\n") //
				.append("\tauto symbol style: " + autoSymbolStyle + "\n") //
				.append("\tsymbol styles: " + listSymbolStyle + "\n") //

				.append("\tshow symbols aux: " + showSymbolsAux + "\n") //
				.append("\tauto show symbols aux: " + autoShowSymbolsAux + "\n") //
				.append("\tshow flags for symbols aux: " + listShowSymbolsAux + "\n") //
				.append("\tsymbol size aux: " + symbolSizeAux + "\n") //
				.append("\tauto symbol size aux: " + autoSymbolSizeAux + "\n") //
				.append("\tsymbol sizes aux: " + listSymbolSizeAux + "\n") //
				.append("\tsymbol type aux: " + symbolTypeAux + "\n") //
				.append("\tauto symbol type aux: " + autoSymbolTypeAux + "\n") //
				.append("\tsymbol types aux: " + listSymbolTypeAux + "\n") //
				.append("\tsymbol style aux: \"" + symbolStyleAux + "\"\n") //
				.append("\tauto symbol style aux: " + autoSymbolStyleAux + "\n") //
				.append("\tsymbol styles aux: " + listSymbolStyleAux + "\n") //
				
				.append("\tline style: \"" + lineStyle + "\"\n") //
				.append("\tauto line style: " + autoLineStyle + "\n") //
				.append("\tline styles: " + listLineStyle + "\n") //
//				.append("\tline size: " + lineSize + "\n") //
//				.append("\tauto line size: " + autoLineSize + "\n") //
//				.append("\tline sizes: " + listLineSize + "\n") //
				.append("\tline style aux: \"" + lineStyleAux + "\"\n") //
				.append("\tauto line style aux: " + autoLineStyleAux + "\n") //
				.append("\tline styles aux: " + listLineStyleAux + "\n") //
				.append("\tarea style: \"" + areaStyle + "\"\n") //
				.append("\tauto area style: " + autoAreaStyle + "\n") //
				.append("\tarea styles: " + listAreaStyle + "\n") //
				.append("\tarea opacity: " + areaOpacity + "\n") //
				.append("\tauto area opacity: " + autoAreaOpacity + "\n") //
				.append("\tarea opacities: " + listAreaOpacity + "\n") //
				.append("\tplot area: " + plotArea + "\n") //
				.append("\tauto plot area: " + autoPlotArea + "\n") //
				.append("\tplot areas: " + listPlotArea + "\n") //
				.append("\tshow legend: " + showLegend + "\n") //
				.append("\tauto legend: " + autoLegend + "\n") //
				.append("\tlegends items: " + listLegendItems + "\n") //
				;
		return sb.toString();
	}

}

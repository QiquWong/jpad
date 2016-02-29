package sandbox.adm;

import org.treez.javafxd3.d3.core.Selection;

import sandbox.adm.javafxd3.test.D3Plotter;
import sandbox.adm.javafxd3.test.D3Plotter.Margin;

public class D3PlotterOptions {

	public Margin margin = new D3Plotter.Margin(40, 20, 50, 60);

	public int widthGraph = 700;
	public int heightGraph = 600;

	public int widthPageSVG = widthGraph - margin.left - margin.right;
	public int heightPageSVG = heightGraph - margin.top - margin.bottom;

	public String graphBackground = "lightblue";

	public double xtickPadding = 12.0;
	public double ytickPadding = 8.0;

	public int symbolSize = 64;
	public String symbolStyle = "fill:red; stroke:blue; stroke-width:2";

	public String lineStyle = "fill:none; stroke:red; stroke-width:2";

	public String areaStyle = "fill:green;";

	public boolean showSymbols = true;
	public boolean plotArea = false;
	public boolean showLegend = true;


}

package GUI.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;

import Calculator.InputOutputTree;
import configuration.enumerations.ComponentEnum;
import graphics.D3Plotter;
import graphics.D3PlotterOptions;
import javafx.scene.Scene;
import javafx.scene.paint.Color;



public class D3PlotterClass {
	static D3Plotter d3Plotter;
	
	public static Scene createWingDesign(InputOutputTree theInputTree) {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> vY = theInputTree.getyDimensionalDistributionInput();
		int nY = vY.size();
		List<Amount<Length>> vChords = theInputTree.getChordDistribution();
		List<Amount<Length>> vXle = theInputTree.getxLEDistribution();
		
		Double[][] dataChordsVsY = new Double[nY][2];
		Double[][] dataXleVsY = new Double[nY][2];
		IntStream.range(0, nY)
		.forEach(i -> {
			dataChordsVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataChordsVsY[i][1] = vChords.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][1] = vXle.get(i).doubleValue(SI.METRE);
		});

		Double[][] dataTopView = theInputTree.getDiscretizedTopViewAsArray();
		

		Double[][] dataTopViewMirrored = new Double[dataTopView.length][dataTopView[0].length];
		for (int i=0; i<dataTopView.length; i++) { 
				dataTopViewMirrored[i][0] = -dataTopView[i][0];
				dataTopViewMirrored[i][1] = dataTopView[i][1];
		}

		//--------------------------------------------------
		
	
	int WIDTH = 400;
	int HEIGHT = 400;

	double xMaxTopView = 1.40*theInputTree.getSpan().divide(2).doubleValue(SI.METER);
	double xMinTopView = -1.40*theInputTree.getSpan().divide(2).doubleValue(SI.METER);
	double yMaxTopView = 1.40*theInputTree.getSpan().divide(2).doubleValue(SI.METER);
	double yMinTopView = -1.40*theInputTree.getSpan().divide(2).doubleValue(SI.METER);
	
	List<Double[][]> listDataArrayTopView = new ArrayList<Double[][]>();
	listDataArrayTopView.add(dataTopView);
	listDataArrayTopView.add(dataTopViewMirrored);
	
	D3PlotterOptions optionsTopView = new D3PlotterOptions.D3PlotterOptionsBuilder()
			.widthGraph(WIDTH).heightGraph(HEIGHT)
			.xRange(xMinTopView, xMaxTopView)
			.yRange(yMaxTopView, yMinTopView)
			.axisLineColor("darkblue").axisLineStrokeWidth("2px")
			.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
			.title("Wing representation - Top View")
			.xLabel("y (m)")
			.yLabel("x (m)")
			.showXGrid(true)
			.showYGrid(true)
			.symbolTypes(
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE,
					SymbolType.CIRCLE
					)
			.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2)
			.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
			.symbolStyles(
					"fill:blue; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2",
					"fill:cyan; stroke:darkblue; stroke-width:2"
					)
			.lineStyles(
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2",
					"fill:none; stroke:black; stroke-width:2"
					)
			.plotAreas(true,true,true,true,true,true,true,true,true,true,true)
			.areaStyles("fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;","fill:white;","fill:white;",
					"fill:yellow;","fill:yellow;","fill:orange;","fill:orange;")
			.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
			.showLegend(false)
			.build(); 
	

	d3Plotter = new D3Plotter(
			optionsTopView,
			listDataArrayTopView
			);
	//define d3 content as post loading hook
	Runnable postLoadingHook = () -> {

		//--------------------------------------------------
		// Create the D3 graph
		//--------------------------------------------------
		d3Plotter.createD3Content();
		
		//--------------------------------------------------
		// output

	}; // end-of-Runnable

	// create the Browser/D3
	//create browser
	JavaFxD3Browser browserTopView = d3Plotter.getBrowser(postLoadingHook, false);
	Scene sceneTopView = new Scene(browserTopView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
	return sceneTopView;
	}

	public D3Plotter getD3Plotter() {
		return d3Plotter;
	}

	public void setD3Plotter(D3Plotter d3Plotter) {
		this.d3Plotter = d3Plotter;
	}
}

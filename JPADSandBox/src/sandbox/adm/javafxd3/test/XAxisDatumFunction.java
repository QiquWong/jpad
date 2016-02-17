package sandbox.adm.javafxd3.test;

import com.github.javafxd3.d3.core.Value;
import com.github.javafxd3.d3.functions.DatumFunction;
import com.github.javafxd3.d3.scales.LinearScale;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class XAxisDatumFunction implements DatumFunction<Double> {
	
	//#region ATTRIBUTES
	
	private WebEngine webEngine;
	
	private LinearScale xScale;
	
	private double[] xData;
	
	//#end region
	
	//#region CONSTRUCTORS
	
	public XAxisDatumFunction(WebEngine webEngine, LinearScale xScale, double[] xData){
		this.webEngine=webEngine;
		this.xScale = xScale;
		this.xData = xData;
	}
	
	//#end region

	//#region METHODS
	
	@Override
	public Double apply(final Object context, final Object d, final int index) {
		
				
		Double x = xData[index];
		Value value = xScale.apply(x);
		Double result = value.asDouble();
		return result;		
	}
	
	//#end region
}

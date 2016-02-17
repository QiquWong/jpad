package sandbox.adm.javafxd3.test;

import com.github.javafxd3.d3.core.Value;
import com.github.javafxd3.d3.functions.DatumFunction;
import com.github.javafxd3.d3.scales.LinearScale;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class YAxisDatumFunction implements DatumFunction<Double> {
	
	//#region ATTRIBUTES
	
	private WebEngine webEngine;
	
	private LinearScale yScale;

	private double[] yData;
	
	//#end region
	
	//#region CONSTRUCTORS
	
	public YAxisDatumFunction(WebEngine webEngine, LinearScale yScale, double[] yData ){
		this.webEngine=webEngine;
		this.yScale = yScale;
		this.yData = yData;
	}
	
	//#end region

	//#region METHODS
	
	@Override
	public Double apply(final Object context, final Object d, final int index) {
		
//		JSObject datum = (JSObject) d;
//		Value inputValue = new Value(webEngine, datum);
//		
//		Double input = inputValue.asDouble();
//		Value value = yScale.apply(input);
//		Double result = value.asDouble();
//		return result;
		
		Double y = yData[index];
		Value value = yScale.apply(y);
		Double result = value.asDouble();
		return result;		
		
	}
	
	//#end region
}

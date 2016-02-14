package sandbox.adm.javafxd3.test;

import com.github.javafxd3.d3.core.Value;
import com.github.javafxd3.d3.functions.DatumFunction;

import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class XAccessorDatumFunction implements DatumFunction<Double> {

	//#region ATTRIBUTES

	private WebEngine webEngine;

	//#end region

	//#region CONSTRUCTORS

	public XAccessorDatumFunction(WebEngine webEngine) {
		this.webEngine = webEngine;
	}

	//#end region

	//#region METHODS	

	@Override
	public Double apply(Object context, Object d, int index) {

		JSObject datum = (JSObject) d;
		Value value = new Value(webEngine, datum);

		Double x = value.<CustomCoords> as(CustomCoords.class).x();
		return x;

	}

	public Double apply(String context, String d, int index) {
		return null;
	}

	//#end region
}
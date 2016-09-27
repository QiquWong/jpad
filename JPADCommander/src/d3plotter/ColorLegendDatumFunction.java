package d3plotter;

import java.util.Arrays;
import java.util.List;

import org.treez.javafxd3.d3.core.Value;
import org.treez.javafxd3.d3.functions.DatumFunction;
import org.treez.javafxd3.d3.wrapper.Element;

import javafx.scene.web.WebEngine;

public class ColorLegendDatumFunction implements DatumFunction<String> {

	//#region ATTRIBUTES

	private WebEngine webEngine;

	private List<String> colors;

	//#end region

	//#region CONSTRUCTORS

	public ColorLegendDatumFunction(WebEngine webEngine, List<String> colors){
		this.webEngine = webEngine;
		this.colors = colors;
	}

	//#end region

	//#region METHODS

	@Override
	public String apply(final Object context, final Object d, final int index) { //
		System.out.println("i: " + index + ", color: " + colors.get(index));
		return colors.get(index);
	}

	//#end region
}

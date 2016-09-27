package d3plotter;

import java.util.Arrays;
import java.util.List;

import org.treez.javafxd3.d3.functions.DatumFunction;

import javafx.scene.web.WebEngine;

public class TextLegendDatumFunction implements DatumFunction<String> {

	//#region ATTRIBUTES

	private WebEngine webEngine;

	private List<String> labels;

	//#end region

	//#region CONSTRUCTORS

	public TextLegendDatumFunction(WebEngine webEngine, List<String> labels){
		this.webEngine =webEngine;
		this.labels = labels;
	}

	//#end region

	//#region METHODS

	@Override
	public String apply(final Object context, final Object d, final int index) { //
		System.out.println("i: " + index + ", text: " + labels.get(index));
		return labels.get(index);
	}

	//#end region
}
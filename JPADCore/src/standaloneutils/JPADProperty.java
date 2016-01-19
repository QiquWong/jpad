package standaloneutils;

import java.io.Serializable;

import javax.measure.unit.Unit;

public class JPADProperty implements Serializable {

	public Double dValue;
	public Unit unit;
	public String description;
	public String path;
	public String latexSymbol;
	public String latexMacro;
	
	
	public JPADProperty() {
	}

	public JPADProperty(
			Double dValue,
			Unit unit,
			String description,
			String path,
			String latexSymbol,
			String latexMacro
			) {
		this.dValue = dValue;
		this.unit = unit;
		this.description = description;
		this.path = path;
		this.latexSymbol = latexSymbol;
		this.latexMacro = latexMacro;
		
	}

}

package standaloneutils;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

public class MyVariableToWrite {

	private String value, unit;

	public MyVariableToWrite(String value, String unit) {
		this.value = value;
		this.unit = unit;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	public static void writeListOfAmountAsExcelFormat(List<Amount<?>> list, String name) {
		
		System.out.print("\nname (" + list.get(0).getUnit() + ") --> [");
		for (int i=0; i<list.size(); i++){
		System.out.print(list.get(i).getEstimatedValue() + " , ");
		}
		System.out.print(" ]");
		
	}
	
}

package writers.xmlAdapters;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jscience.physics.amount.Amount;

/************************************************************************
 * This adapter class is used to marshal and unmarshal jscience Amount 
 * quantities using JAXB  
 * 
 * @author Vittorio Trifari
 *
 */

public class AmountAdapter extends XmlAdapter<String, Amount<Quantity>> {

	public String marshal(Amount<Quantity> value) throws Exception {
		if (null == value) {
			return null;
		}

		return String.valueOf(value.doubleValue(value.getUnit()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Amount<Quantity> unmarshal(String str) throws Exception {
		if(str == null || str.length() == 0) {
			return null;
		}

		String[] d = str.split(" ");
		Amount<Quantity> m = null;
		m = (Amount<Quantity>) Amount.valueOf(Double.parseDouble(d[0]), Unit.valueOf(d[1]));

		return m;
	}
}
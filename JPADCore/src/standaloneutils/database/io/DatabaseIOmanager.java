package standaloneutils.database.io;

import java.util.ArrayList;
import java.util.List;

import org.jscience.physics.amount.Amount;

/**
 * 
 * @author Lorenzo Attnasio
 *
 * @param <E>
 */
public class DatabaseIOmanager<E extends Enum<E>> {

	public static final String value = "value";
	public static final String description = "description";

	private List<E> tagList = new ArrayList<E>();
	private List<Amount> valueList = new ArrayList<Amount>();
	private List<String> stringList = new ArrayList<String>();
	private List<Object> valueObjList = new ArrayList<Object>();
	private List<String> descriptionList = new ArrayList<String>();
	
	public DatabaseIOmanager() {
		
	}
	
	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param tagName
	 * @param value
	 * @param description
	 */
	public void addElement(E tagName, Amount value, String description) {
		tagList.add(tagName);
		valueList.add(value);
		descriptionList.add(description);
	}
	
	/**
	 * Overload of the previous method that accepts input data as String. These have then to be 
	 * converted to the wanted type.
	 * 
	 * @author Vittorio Trifari & Manuela Ruocco
	 * 
	 * @param tagName
	 * @param valueString
	 * @param description
	 */
	public void addElement(E tagName, String valueString, String description) {
		tagList.add(tagName);
		stringList.add(valueString);
		descriptionList.add(description);
	}
	
//	/**
//	 * This method  allows to add object elements
//	 * 
//	 * @author Vincenzo Cusati
//	 * @param tagName
//	 * @param value
//	 * @param description
//	 */
//	public void addObjElement(E tagName, Object value, String description) {
//		tagList.add(tagName);
//		valueObjList.add(value);
//		descriptionList.add(description);
//	}
//	
//	/**
//	 * @author Vincenzo Cusati
//	 * @param tagName
//	 * @return
//	 */
//	public Object getObjValue(E tagName) {
//		return valueObjList.get(tagList.indexOf(tagName));
//	}
	
	public Amount getValue(E tagName) {
		return valueList.get(tagList.indexOf(tagName));
	}

	public void setValue(String tagName, Amount value) {
		valueList.set(tagList.indexOf(tagName), value);
	}

	public List<E> getTagList() {
		return tagList;
	}

	public List<Amount> getValueList() {
		return valueList;
	}

	public List<String> getDescriptionList() {
		return descriptionList;
	}

	public void setValueList(List<Amount> valueList) {
		this.valueList = valueList;
	}

//	public List<Object> getValueObjList() {
//		return valueObjList;
//	}
//
//	public void setValueObjList(List<Object> valueObjList) {
//		this.valueObjList = valueObjList;
//	}
}

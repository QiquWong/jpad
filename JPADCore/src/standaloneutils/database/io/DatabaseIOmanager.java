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

	// Component of the IOmanager in case of Amounts:
	private List<E> tagList = new ArrayList<E>();
	private List<Amount> valueList = new ArrayList<Amount>();
	private List<String> descriptionList = new ArrayList<String>();
	
	// Component of the IOmanager in case of Strings:
	private List<E> tagListString = new ArrayList<E>();
	private List<String> stringList = new ArrayList<String>();
	private List<String> descriptionListString = new ArrayList<String>();
	
	// Component of the IOmanager in case of Strings:
	private List<E> tagListListString = new ArrayList<E>();
	private List<List<String>> stringListList = new ArrayList<List<String>>();
	private List<String> descriptionListListString = new ArrayList<String>();
	
	// Component of the IOmanager in case of List<Double>:
	private List<E> tagListListDouble = new ArrayList<E>();
	private List<List<Double>> doubleListList = new ArrayList<List<Double>>();
	private List<String> descriptionListListDouble = new ArrayList<String>();
	
	// Component of the IOmanager in case of List<Double[]>:
	private List<E> tagVecListListDouble = new ArrayList<E>();
	private List<List<Double[]>> doubleVecListList = new ArrayList<List<Double[]>>();
	private List<String> descriptionVecListListDouble = new ArrayList<String>();
	
	// Component of the IOmanager in case of Objects:
	private List<E> tagObjList = new ArrayList<E>();
	private List<Object> valueObjList = new ArrayList<Object>();
	private List<String> descriptionObjList = new ArrayList<String>();
	
	
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
	 * Overload of the previous method that accepts input data as String.
	 * 
	 * @author Vittorio Trifari & Manuela Ruocco
	 * 
	 * @param tagName
	 * @param valueString
	 * @param description
	 */
	public void addElement(E tagName, String valueString, String description) {
		tagListString.add(tagName);
		stringList.add(valueString);
		descriptionListString.add(description);
	}
	
	/**
	 * Overload of the previous method that accepts input data as Lists of String.
	 * 
	 * @author Vittorio Trifari & Manuela Ruocco
	 * 
	 * @param tagName
	 * @param valueString
	 * @param description
	 */
	public void addElementStringList(E tagName, List<String> valueString, String description) {
		tagListString.add(tagName);
		stringListList.add(valueString);
		descriptionListString.add(description);
	}
	
	/**
	 * Overload of the previous method that accepts input data as Lists of Double.
	 * 
	 * @author Vittorio Trifari & Manuela Ruocco
	 * 
	 * @param tagName
	 * @param valueString
	 * @param description
	 */
	public void addElement(E tagName, List<Double> listOfValues, String description) {
		tagListListDouble.add(tagName);
		doubleListList.add(listOfValues);
		descriptionListListDouble.add(description);
	}
	
	/**
	 * Overload of the previous method that accepts input data as Lists of Double arrays.
	 * 
	 * @author Vittorio Trifari & Manuela Ruocco
	 * 
	 * @param tagName
	 * @param valueString
	 * @param description
	 */
	public void addElementDoubleArray(E tagName, List<Double[]> listOfDoubleVec, String description) {
		tagVecListListDouble.add(tagName);
		doubleVecListList.add(listOfDoubleVec);
		descriptionVecListListDouble.add(description);
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
	
	public List<String> getStringList() {
		return stringList;
	}

	public void setStringList(List<String> stringList) {
		this.stringList = stringList;
	}

	public List<List<String>> getStringListList() {
		return stringListList;
	}

	public void setStringListList(List<List<String>> stringListList) {
		this.stringListList = stringListList;
	}

	public List<List<Double>> getDoubleListList() {
		return doubleListList;
	}

	public void setDoubleListList(List<List<Double>> doubleListList) {
		this.doubleListList = doubleListList;
	}

	public List<List<Double[]>> getDoubleVecListList() {
		return doubleVecListList;
	}

	public void setDoubleVecListList(List<List<Double[]>> doubleVecListList) {
		this.doubleVecListList = doubleVecListList;
	}

	public List<E> getTagListString() {
		return tagListString;
	}

	public List<String> getDescriptionListString() {
		return descriptionListString;
	}

	public List<E> getTagListListString() {
		return tagListListString;
	}

	public List<String> getDescriptionListListString() {
		return descriptionListListString;
	}

	public List<E> getTagListListDouble() {
		return tagListListDouble;
	}

	public List<String> getDescriptionListListDouble() {
		return descriptionListListDouble;
	}

	public List<E> getTagVecListListDouble() {
		return tagVecListListDouble;
	}

	public List<String> getDescriptionVecListListDouble() {
		return descriptionVecListListDouble;
	}

	public List<E> getTagObjList() {
		return tagObjList;
	}

	public List<String> getDescriptionObjList() {
		return descriptionObjList;
	}

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

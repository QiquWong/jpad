package standaloneutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;

import aircraft.components.Aircraft;
import analyses.OperatingConditions;

public class JPADXmlReader {

	public enum Status {
		PARSED_OK,
		FILE_NOT_FOUND,
		STUCK,
		UNKNOWN,
		RESET;
	}

	private Status _status = Status.UNKNOWN;
	private String _xmlFilePath = "";
	private File _xmlFile;

	private Document _xmlDoc;

	private DocumentBuilder _builderDoc;
	private DocumentBuilderFactory _factoryBuilderDoc;
	private XPathFactory _xpathFactory;
	private XPath _xpath;
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;
	private String _xmlFileImport;

	/*
	 *  Constructor
	 *  @param filePath  file absolute path
	 */
	public JPADXmlReader(String filePath) {

		// Incorporates: reset() + init()
		this.open(filePath);

	}

	/**
	 * Builder used for importing an aircraft from xml file.
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param importFileName is the .xml file to read
	 */
	public JPADXmlReader(Aircraft aircraft, OperatingConditions conditions,
			String importFileName) {

		_theAircraft = aircraft;
		_theOperatingConditions = conditions;
		//		_factoryImport.setSchema(null);

		_xpathFactory = XPathFactory.newInstance();

		_xmlFileImport = importFileName;
		_xmlDoc = MyXMLReaderUtils.importDocument(importFileName);
	}

	/*
	 * Opens a file; sets inner status to Status.FILE_NOT_FOUND if fails
	 * @param filePath  file absolute path
	 */
	public void open(String filePath) {

		this.reset();

		_xmlFilePath = filePath;
		_xmlFile = new File(_xmlFilePath);

		if (
				_xmlFile.exists()
				&& !_xmlFile.isDirectory()
				) {
			System.out.println("File " + _xmlFile.getAbsolutePath() + " found.");
			System.out.println("Parsing ...");
			init();
		} else {
			System.err.println("Path '" + _xmlFilePath + "' not found or not a file.");
			_status = Status.FILE_NOT_FOUND;
		}
	}

	private void init() {

		_factoryBuilderDoc = DocumentBuilderFactory.newInstance();
		_factoryBuilderDoc.setNamespaceAware(true);
		_factoryBuilderDoc.setIgnoringComments(true);

		try {

			// Prepare a builder
			_builderDoc = _factoryBuilderDoc.newDocumentBuilder();
			// Finally, parse the file
			_xmlDoc = _builderDoc.parse(_xmlFilePath);

			System.out.println("File "+ _xmlFilePath + " parsed.");

			// Initialize XPath-related stuff
			_xpathFactory = XPathFactory.newInstance();
			_xpath = _xpathFactory.newXPath();

			_status = Status.PARSED_OK;

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
			_status = Status.STUCK;
		}
	}

	/*
	 * Reset all variables and sets status to Status.RESET
	 */
	public void reset() {
		_status = Status.RESET;
		_xmlFilePath = "";
		_xmlFile = null;
		_xmlDoc = null;
		_builderDoc = null;
		_factoryBuilderDoc = null;
		_xpathFactory = null;
		_xpath = null;		
	}

	/*
	 * Search first occurrence of a given attribute via XPath
	 * @param path       the XPath expression pointing to the XML tag
	 * @param sttribute  the attribute name
	 * @return           a string result; null if nothing found
	 */
	public String getXMLAttributeByPath(String path, String attribute) {
		if (this.isStatusOK()) {
			return MyXMLReaderUtils
					.getXMLPropertyByPath(_xmlDoc, _xpath, path + "/@" + attribute);
		} else {
			return null;
		}		
	}
	
	/*
	 * Search first occurrence of a given expression via XPath
	 * @param expression  the XPath expression
	 * @return            a string result; null if nothing found
	 */
	public String getXMLPropertyByPath(String expression) {
		if (this.isStatusOK()) {
			return MyXMLReaderUtils
					.getXMLPropertyByPath(_xmlDoc, _xpath, expression + "/text()");
		} else {
			return null;
		}		
	}

	/*
	 * Search all occurrence of a given expression via XPath
	 * @param expression  the XPath expression
	 * @return            a list of strings result; null if nothing found
	 */
	public List<String> getXMLPropertiesByPath(String expression) {
		if (this.isStatusOK()) {
			return MyXMLReaderUtils
					.getXMLPropertiesByPath(_xmlDoc, _xpath, expression + "/text()");
		} else {
			return null;
		}		
	}

	/*
	 * Get the quantity from XML path; unit attribute is mandatory; if search fails return null
	 * <p>
	 * Example:
	 *     <chord unit="cm">105</chord>
	 * <p>
	 * @param expression XPath expression
	 * @return           amount, dimensions according to unit attribute value 
	 */
	public Amount<?> getXMLAmountWithUnitByPath(String expression) {

		return MyXMLReaderUtils.getXMLAmountWithUnitByPath(_xmlDoc, _xpath, expression);

	}

	/*
	 * Get a length quantity from XML path; unit attribute is not mandatory, if not present
	 * the numeric value is assumed as SI.METRE ; if search fails return null
	 * <p>
	 * Examples:
	 *     <chord unit="cm">105</chord>
	 *     <chord >1.05</chord>
	 * <p>
	 * @param expression XPath expression
	 * @return           amount, dimensions according to unit attribute value 
	 */
	public Amount<Length> getXMLAmountLengthByPath(String expression) {

		return MyXMLReaderUtils.getXMLAmountLengthByPath(_xmlDoc, _xpath, expression);

	}

	public double[] getXMLAmountsLengthByPath(String expression) throws XPathExpressionException {

		return MyXMLReaderUtils.getXMLAmountsLengthByPath(_xmlDoc, _xpath, expression);

	}

	public Amount<Angle> getXMLAmountAngleByPath(String expression) {

		return MyXMLReaderUtils.getXMLAmountAngleByPath(_xmlDoc, _xpath, expression);

	}


	// TODO: implement similar functions, such as:
	// getXMLAmountSurfaceByPath
	// getXMLAmountVolumeByPath
	// getXMLAmountMassByPath
	// etc

	/*
	 * @return true if file is parsed OK
	 */
	public boolean isStatusOK() {
		return (_status == Status.PARSED_OK);
	}

	/*
	 * @return one of enumerated status codes (see standaloneutils.MyXMLReader.Status)
	 */
	public Status getStatus() {
		return _status;
	}


	/** 
	 * Read component (e.g., theFuselage) from file and initialize it
	 * The component is recognized through its unique id.
	 * 
	 * @author LA
	 * @param object the component which has to be initialized
	 * @param xmlFile
	 */
	public void importItemFromXMLById(Object object, String importFilenameWithPath, Integer ... lev) {

		if (object != null) {

			// Create XPath object
			XPath xpath = _xpathFactory.newXPath();
			Node node = null;
			//		Class<?> clazz = object.getClass();

			try {
				node = (Node) xpath.evaluate(
						"//*[@id='" + JPADGlobalData.getTheXmlTree().getIdAsString(object) + "']",
						_xmlDoc, 
						XPathConstants.NODE);

				// TODO: Remove this when debug is complete
				System.out.println("//*[@id='" + JPADGlobalData.getTheXmlTree().getIdAsString(object) + "']");

			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}

			Integer level;
			if (lev.length == 0) level = 2;
			else level = lev[0];

			if (node != null)
				System.out.println("Importing " + node.getNodeName() + "...");

			MyXMLReaderUtils.recursiveRead(_xmlDoc, object, level, xpath, node);
			//				_importDoc.getElementById(ADOPT_GUI.getTheXmlTree().getIdAsString(object)));
		} else {
			System.out.println("The object to be read has not been initialized");
		}
	}

	/**
	 * This static method reads arrays from xml. This method accepts a String as input that is the complete array. 
	 * 
	 * @author Manuela Ruocco
	 */
	public static List<String> readArrayFromXML(String inputString){

		List<String> outputStrings = new ArrayList<String>();
		String tempString = new String();
		int n, m;

		inputString = inputString.trim();

		int openParenthesisCheck = inputString.indexOf('[');

		if ( openParenthesisCheck == -1){
			inputString = "[" + inputString;
		}

		int closeParenthesisCheck = inputString.indexOf(']');

		if ( closeParenthesisCheck == -1){
			inputString = inputString + "]";
		}

		// First value
		boolean checkOnlyOneElement = false;

		n = inputString.indexOf(',');
		if ( n == -1){
			n = inputString.indexOf(';');
			if ( n == -1 ) {
				n = inputString.indexOf(']');
				checkOnlyOneElement = true;
			}
		}

		tempString = inputString.substring(1, n);
		tempString = tempString.trim();

		outputStrings.add(tempString);


		// Following values

		while ( (n!= -1) && (checkOnlyOneElement == false) ){

			m = n;
			tempString = new String();

			n = inputString.indexOf(',', m+1);
			if ( n == -1){
				n = inputString.indexOf(';', m+1);
			}
			if( n != -1){
				tempString = inputString.substring(m+1, n);}

			else{
				int k = inputString.indexOf(']');
				tempString = inputString.substring(m+1, k)	;
			}
			tempString = tempString.trim();

			outputStrings.add(tempString);
		}
		return outputStrings;
	}

	/**
	 * This method reads arrays of double from xml. 
	 * 
	 * @author Manuela Ruocco
	 */
	public List<Double> readArrayDoubleFromXMLSplit(String inputStringInitial){

		String inputString = this.getXMLPropertiesByPath(inputStringInitial).get(0);

		inputString = inputString.trim();
		List<Double> outputStrings = new ArrayList<>(); 
		int openParenthesisCheck = inputString.indexOf('[');
		int closeParenthesisCheck = inputString.indexOf(']');
		if ( openParenthesisCheck != -1){
			inputString = inputString.substring(openParenthesisCheck+1, inputString.length());
		}
		if ( closeParenthesisCheck != -1){
			inputString = inputString.substring(0, closeParenthesisCheck-1);
		}

		inputString = inputString.trim();
		String [] arraysString = null ;
		inputString = inputString.replaceAll(";", ",");
		arraysString = inputString.split(",");

		for(int i=0; i<arraysString.length; i++){
			outputStrings.add(Double.valueOf(arraysString[i].trim()));
		}

		return outputStrings;
	}



	public List<Double> readArrayDoubleFromXML(String inputStringInitial){

		String inputString = this.getXMLPropertiesByPath(inputStringInitial).get(0);

		List<Double> outputStrings = new ArrayList<Double>();
		String tempString = new String();
		Double tempDouble;
		int n, m;
		inputString = inputString.trim();

		int openParenthesisCheck = inputString.indexOf('[');

		if ( openParenthesisCheck == -1){
			inputString = "[" + inputString;
		}

		int closeParenthesisCheck = inputString.indexOf(']');

		if ( closeParenthesisCheck == -1){
			inputString = inputString + "]";
		}

		// First value
		boolean checkOnlyOneElement = false;

		n = inputString.indexOf(',');
		if ( n == -1){
			n = inputString.indexOf(';');
			if ( n == -1 ) {
				n = inputString.indexOf(']');
				checkOnlyOneElement = true;
			}
		}

		tempString = inputString.substring(1, n);
		tempDouble = Double.valueOf(tempString.trim());

		outputStrings.add(tempDouble);
		// Following values

		while ( (n!= -1) && (checkOnlyOneElement == false) ){

			m = n;
			tempString = new String();

			n = inputString.indexOf(',', m+1);
			if ( n == -1){
				n = inputString.indexOf(';', m+1);
			}
			if( n != -1){
				tempString = inputString.substring(m+1, n);}

			else{
				int k = inputString.indexOf(']');
				tempString = inputString.substring(m+1, k)	;
			}
			tempDouble = Double.valueOf(tempString.trim());

			outputStrings.add(tempDouble);
		}
		return outputStrings;
	}


	/**
	 * This method reads list of amount from xml . The amounts must have the same units.
	 * 
	 * @author Manuela Ruocco
	 */


	@SuppressWarnings({ "unchecked" })
	public <T extends Quantity> List<Amount<T>> readArrayofAmountFromXML(String inputStringInitial){

		String inputString = this.getXMLPropertiesByPath(inputStringInitial).get(0);

		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(_xmlDoc, _xpath, inputStringInitial + "/@unit");
		
		List<Amount<T>> outputList = new ArrayList<Amount<T>>();
		inputString = inputString.trim();

		int openParenthesisCheck = inputString.indexOf('[');
		int closeParenthesisCheck = inputString.indexOf(']');
		if ( openParenthesisCheck != -1){
			inputString = inputString.substring(openParenthesisCheck+1, inputString.length());
		}
		if ( closeParenthesisCheck != -1){
			inputString = inputString.substring(0, closeParenthesisCheck-1);
		}

		inputString = inputString.trim();

		String [] arraysString = null ;
		inputString = inputString.replaceAll(";", ",");
		arraysString = inputString.split(",");

		for(int i=0; i<arraysString.length; i++){
			
			Amount<?> tempAmount = null;
			
			if(unitStr.startsWith("1/", 0)) {
				Double value = Double.parseDouble(arraysString[i].trim());
				tempAmount =  Amount.valueOf(value, Unit.valueOf(unitStr).inverse());
			}
			else {
				Double value = Double.parseDouble(arraysString[i].trim());
				tempAmount =  Amount.valueOf(value, Unit.valueOf(unitStr));
			}
			
			outputList.add((Amount<T>) tempAmount);

		}
		return outputList;
	}

	@SuppressWarnings({ "unchecked" })
	public List<Amount<?>> readArrayofUnknownAmountFromXML(String inputStringInitial){

		String inputString = this.getXMLPropertiesByPath(inputStringInitial).get(0);

		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(_xmlDoc, _xpath, inputStringInitial + "/@unit");
		
		List<Amount<?>> outputList = new ArrayList<Amount<?>>();
		inputString = inputString.trim();

		int openParenthesisCheck = inputString.indexOf('[');
		int closeParenthesisCheck = inputString.indexOf(']');
		if ( openParenthesisCheck != -1){
			inputString = inputString.substring(openParenthesisCheck+1, inputString.length());
		}
		if ( closeParenthesisCheck != -1){
			inputString = inputString.substring(0, closeParenthesisCheck-1);
		}

		inputString = inputString.trim();

		String [] arraysString = null ;
		inputString = inputString.replaceAll(";", ",");
		arraysString = inputString.split(",");

		for(int i=0; i<arraysString.length; i++){
			
			Amount<?> tempAmount = null;
			
			if(unitStr.startsWith("1/", 0)) {
				Double value = Double.parseDouble(arraysString[i].trim());
				tempAmount =  Amount.valueOf(value, Unit.valueOf(unitStr.substring(2)).inverse());
			}
			else {
				Double value = Double.parseDouble(arraysString[i].trim());
				tempAmount =  Amount.valueOf(value, Unit.valueOf(unitStr));
			}
			
			outputList.add((Amount<?>) tempAmount);

		}
		return outputList;
	}
	
	public Document getXmlDoc() {
		return _xmlDoc;
	}

	public XPath getXpath() {
		return _xpath;
	}

}

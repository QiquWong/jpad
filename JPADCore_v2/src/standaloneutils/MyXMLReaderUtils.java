package standaloneutils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import configuration.MyConfiguration;
import configuration.enumerations.AirfoilEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;

public class MyXMLReaderUtils {

	/**
	 * Overloaded method: read specified object, e.g. theFuselage
	 *
	 * @param doc the parsed document
	 * @param object the object which has to be populated
	 * @param level the maximum inner level the method has to read from the xml
	 * @param xpath
	 * @param mainNode
	 * @param childNodeV
	 */
	public static void recursiveRead(
			Document doc,
			Object object,
			Integer level,
			XPath xpath,
			Node mainNode,
			Node ... childNodeV) {

		if (childNodeV == null) return;

		if (mainNode == null) {
			System.out.println("Something in " + object.getClass().getName() + " could not be read");
			return;
		}

		if (childNodeV.length == 0) {
			childNodeV = new Node[1];
			childNodeV[0] = mainNode;
		}

		recursiveReadCore(doc, object, level, xpath, mainNode, childNodeV);

	}

	public static void recursiveReadCore(
			Document doc,
			Object object,
			Integer level,
			XPath xpath,
			Node mainNode,
			Node ... childNodeV) {

		Node childNode;
		for(childNode = childNodeV[0].getFirstChild();
				childNode != null;
				childNode = childNode.getNextSibling()) {

			Element e = (Element) childNode;
			String lev = e.getAttribute("level");
			String id = e.getAttribute("id");

			if (lev.equals("")) lev = "0";

			//			if (Integer.parseInt(lev) > level) {
			if ((!id.equals(""))) {
				// Skip this node

			} else {
				//				System.out.println("--- importing: " + childNode.getNodeName());

				if (childNode.getFirstChild() != null
						&& !childNode.getFirstChild().hasChildNodes()){

					String variablePath = getElementXpath((Element) childNode);
					String source = getXMLPropertyByPath(doc, xpath, variablePath + "/@from");

					if(source != null
							&& !source.equals("")
							&& source.equals("input"))
						readAllNodes(doc, object, xpath, getElementXpath((Element) childNode), MyConfiguration.notInitializedWarning);

				} else {
					recursiveReadCore(doc, object, level, xpath, mainNode, childNode);
				}
			}
		}
	}

	public static int getElementIndex(Element original) {
		int count = 1;

		for (Node node = original.getPreviousSibling(); node != null;
				node = node.getPreviousSibling()) {
			if (node instanceof Element) {
				Element element = (Element) node;
				if (element.getTagName().equals(original.getTagName())) {
					count++;
				}
			}
		}

		return count;
	}

	/**
	 * Get the full path of an xml element, wherever it is
	 *
	 * @author Lorenzo Attanasio
	 * @param elt
	 * @return
	 */
	public static String getElementXpath(Element elt){
		String path = "";

		try{
			for (; elt != null; elt = (Element) elt.getParentNode()){
				int idx = getElementIndex(elt);
				String xname = elt.getNodeName().toString();
				path = "/" + xname + path;
			}
		}catch(Exception ee){
		}
		return path;
	}

	/**
	 * This method removes whitespace for easier xml navigation
	 *
	 * @author Lorenzo Attanasio
	 * @param e
	 */
	public static void removeWhitespaceAndCommentNodes(Element e) {
		NodeList children = e.getChildNodes();
		for (int i = children.getLength() - 1; i >= 0; i--) {
			Node child = children.item(i);
			if (child instanceof Text
					&& ((((Text) child).getData().trim().length() == 0)
							| ((Text) child).getData().contains("!--"))) {
				e.removeChild(child);
			}
			else if (child instanceof Element) {
				removeWhitespaceAndCommentNodes((Element) child);
			}
		}
	}

	/**
	 * Set variables with the values read from xml file.
	 * The method can read ONLY Integers, not primitive int.
	 *
	 * @author Lorenzo Attanasio
	 * @param targetObject the Object to read (e.g., theFuselage)
	 * @param xpath
	 * @param variablePath full variable path in xml document
	 * @param notInitializedWarning
	 *
	 */
	private static void readAllNodes(
			Document doc,
			Object targetObject,
			XPath xpath,
			String variablePath, String notInitializedWarning) {

		String value = getXMLPropertyByPath(doc, xpath, variablePath + "/text()");
		String unit = getXMLPropertyByPath(doc, xpath, variablePath + "/@unit");
		String source = getXMLPropertyByPath(doc, xpath, variablePath + "/@from");
		//		System.out.println("------------------" + variablePath);
		String varName = null;

		try {
			varName = getXMLPropertyByPath(doc, xpath, variablePath + "/@varName");
		} catch (Exception e) {
			System.out.println("VarName not found");
		}

		//		Method[] allMethods = object.getClass().getDeclaredMethods();
		//		Field[] allFields = object.getClass().getDeclaredFields();

		// Check if setter method exist for each variable
		// WARNING: the loop works only if the variable's declaring class
		// has a setVariableName method.
		//		for (Method method : allMethods) {
		//						if (method.getName().equals("set" + varName)){
		if (varName != null
				&& source != null
				&& !source.equals("")
				&& source.equals("input")) {
			//				System.out.println(method.getName());
			Field tempField = null;
			String[] valueArr;
			Double[] tempArrDouble;
			Integer[] tempArrInt;

			try {

				tempField = recursiveReadField(targetObject.getClass(), varName);

				if (tempField != null) {
					tempField.setAccessible(true);

					if (value.equals(notInitializedWarning)
							|| value.equals("")
							|| value.equals(" ")) {

						tempField.set(targetObject, null);
						System.out.println(varName + " in " + targetObject + " initialized to null");
						return;
					}

					// Check if string is an array
					if (value.startsWith("[") && value.endsWith("]")) {
						valueArr = value.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll(" ", "").split(",");

						tempArrDouble = new Double[valueArr.length];
						tempArrInt = new Integer[valueArr.length];

						for (int i = 0; i < valueArr.length; i++) {
							if (MyMiscUtils.isInteger(valueArr[0])) {
								tempArrInt[i] = Integer.parseInt(valueArr[i]);
							} else {
								tempArrDouble[i] = Double.parseDouble(valueArr[i]);
							}
						}

						if (MyMiscUtils.isInteger(valueArr[0])) {
							tempField.set(targetObject, tempArrInt);
						} else {
							tempField.set(targetObject, tempArrDouble);
						}

					} else {

						if (unit == null) // if "unit" attribute is not present, default to non-dimensional
							unit = "";

						// if variable is dimensionless its unit is ""
						if (unit.equals("")) {
							if (value.equals("true") | value.equals("false")){
								tempField.set(targetObject, Boolean.parseBoolean(value));

								// Fill enums from strings
							} else if (MyMiscUtils.isInEnum(value, EngineTypeEnum.class)) {
								tempField.set(targetObject, EngineTypeEnum.valueOf(value));
							} else if (MyMiscUtils.isInEnum(value, EngineMountingPositionEnum.class)) {
								tempField.set(targetObject, EngineMountingPositionEnum.valueOf(value));
							} else if (MyMiscUtils.isInEnum(value, AirfoilEnum.class)) {
								tempField.set(targetObject, AirfoilEnum.valueOf(value));
							} else if (MyMiscUtils.isInEnum(value, AirfoilTypeEnum.class)) {
								tempField.set(targetObject, AirfoilTypeEnum.valueOf(value));
							} else {

								// Check if value is integer
								try {
									tempField.set(targetObject, Integer.parseInt(value));
								} catch(NumberFormatException e) {
									// If value is not an integer parse it as a double
									tempField.set(targetObject, Double.parseDouble(value));
								}

							}
						}
						// Convert degree angle to radian
						else if (unit.equals(NonSI.DEGREE_ANGLE.toString())) {
							tempField.set(targetObject, Amount.valueOf(Math.toRadians(Double.parseDouble(value)), SI.RADIAN));

						} else {
							tempField.set(targetObject, Amount.valueOf(Double.parseDouble(value), Unit.valueOf(unit)));
						}

					}

					//					System.out.println(varName + " in " + targetObject.getClass().getName() + " read successfully");

				} else {
					System.out.println(varName + " in " + targetObject.getClass().getName() + " could not be read");
				}

			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read a field from a class or from
	 * its superclass(es) recursively
	 *
	 * @author Lorenzo Attanasio
	 * @param targetClass
	 * @param varName
	 * @return
	 */
	private static Field recursiveReadField(Class<?> targetClass, String varName) {

		Field tempField = null;
		Class<?> clazz = targetClass;
		//		if (!targetClass.equals(Object.class)) {

		while (tempField == null && clazz != null) {

			try {
				tempField = clazz.getDeclaredField(varName);

			} catch (NoSuchFieldException | SecurityException e) {
				//				e.printStackTrace();
				tempField = null;
				clazz = clazz.getSuperclass();
			}
		}

		return tempField;
	}

	public static Object deserializeObject(Object obj, String fileNameWithPath) {

		byte[] encodedBytes;
		String input = "";

		try {
			if (!fileNameWithPath.endsWith(".xml")) input = fileNameWithPath + ".xml";
			else input = fileNameWithPath;

			encodedBytes = Files.readAllBytes(Paths.get(input));
			String xml = new String(encodedBytes, StandardCharsets.UTF_8);
			XStream xstream = new XStream(new DomDriver());
			obj = xstream.fromXML(xml);
			System.out.println(obj.getClass().getName() + " de-serialization complete");
			return obj;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getXMLPropertyByPath(Document doc, XPath xpath, String expression) {

		try {

			XPathExpression expr =
					xpath.compile(expression);
			// evaluate expression result on XML document
			List<String> list_elements = new ArrayList<String>();
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++)
				list_elements.add(nodes.item(i).getNodeValue());

			if ( !list_elements.isEmpty() ) {
				return list_elements.get(0);

			} else {
				return null;
			}

		} catch (XPathExpressionException ex1) {

			ex1.printStackTrace();
			return null; // ??
		}
	} // end-of-getXMLPropertyByPath:

	public static NodeList getXMLNodeListByPath(Document doc, String expression) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr =
					xpath.compile(expression);
			// evaluate expression result on XML document
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			return nodes;
		} catch (XPathExpressionException ex1) {
			System.err.println("########################## MyXMLReaderUtils :: getXMLNodeListByPath");
			ex1.printStackTrace();
			return null; // ??
		}
	} // end-of-getXMLPropertyByPath:





	/*
	 * Get the list of property values for a given XPath expression
	 * @param document
	 * @param string expression
	 * @return list of properties (strings)
	 */
	public static List<String> getXMLPropertiesByPath(Document doc, XPath xpath, String expression) {
		try {

			XPathExpression expr =
					xpath.compile(expression);

			// evaluate expression result on XML document
			List<String> list_elements = new ArrayList<String>();
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++)
				list_elements.add(nodes.item(i).getNodeValue());

			return list_elements;

		} catch (XPathExpressionException ex1) {

			ex1.printStackTrace();
			return null; // ??
		}
	} // end-of-getXMLPropertiesByPath:

	/*
	 * Get the list of property values for a given XPath expression
	 * @param document
	 * @param string expression
	 * @return list of properties (strings)
	 */
	public static List<String> getXMLPropertiesByPath(Document doc, String expression) {
		try {

			// Once we have document object. We are ready to use XPath. Just create an xpath object using XPathFactory.
			// Create XPathFactory object
			XPathFactory xpathFactory = XPathFactory.newInstance();

			// Create XPath object
			XPath xpath = xpathFactory.newXPath();

			XPathExpression expr =
					xpath.compile(expression);


			// evaluate expression result on XML document
			List<String> list_elements = new ArrayList<>();
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++)
				list_elements.add(nodes.item(i).getNodeValue());

			return list_elements;

		} catch (XPathExpressionException ex1) {

			ex1.printStackTrace();
			return null; // ??
		}
	} // end-of-getXMLPropertiesByPath:


	/*
	 * Get the node list of property values for a given XPath expression
	 * @param document
	 * @param string expression
	 * @return list of nodes (NodeList)
	 */
	public static NodeList getXMLNodeListByPath(Document doc, XPath xpath, String expression) {
		try {

			XPathExpression expr =
					xpath.compile(expression);
			// evaluate expression result on XML document
			List<String> list_elements = new ArrayList<>();
			NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

			for (int i = 0; i < nodes.getLength(); i++)
				list_elements.add(nodes.item(i).getNodeValue());

			return nodes;

		} catch (XPathExpressionException ex1) {

			ex1.printStackTrace();
			return null; // ??
		}
	} // end-of-getXMLNodeListByPath:

	
	public static Double getXMLDoubleByPath(Document xmlDoc, XPath xpath, String expression) {
		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {
				Double value = Double.parseDouble(valueStr);
				return value;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.err.println("########################## MyXMLReaderUtils :: getXMLDoubleByPath");
				return null;
			}
		} else
			return null;
	}
	public static Double getXMLDoubleByPath(Document xmlDoc, String expression) {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		return MyXMLReaderUtils.getXMLDoubleByPath(xmlDoc, xpath, expression);
	}
	

	/*
	 * Get the quantity from XML path; unit attribute is mandatory; if search fails return null
	 * <p>
	 * Example:
	 *     <chord unit="cm">105</chord>
	 * <p>
	 * @param xmlDoc     the Document object
	 * @param xpath      the XPath object
	 * @param expression XPath expression
	 * @return           amount, dimensions according to unit attribute value
	 */
	public static Amount<?> getXMLAmountWithUnitByPath(Document xmlDoc, XPath xpath, String expression) {

		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals("")) && (unitStr != null)) {
			try {

				Double value = Double.parseDouble(valueStr);
				Amount<?> quantity = Amount.valueOf(value, Unit.valueOf(unitStr));

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

	public static Amount<?> getXMLAmountWithUnitByPath(Document xmlDoc, String expression) {

		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			XPathExpression expr =
					xpath.compile(expression);
			// evaluate expression result on XML document
			String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
			String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");
			
			if ((valueStr != null) && (!valueStr.equals("")) && (unitStr != null)) {

					Double value = Double.parseDouble(valueStr);
					Amount<?> quantity = Amount.valueOf(value, Unit.valueOf(unitStr));
					return quantity;
			} else
				return null;

		} catch (NumberFormatException | AmountException e) {
			e.printStackTrace();
			return null;
		} catch (XPathExpressionException ex1) {
				System.err.println("########################## MyXMLReaderUtils :: getXMLAmountWithUnitByPath");
				ex1.printStackTrace();
				return null; // ??
		}
	}
	
	/*
	 * Get a length quantity from XML path; unit attribute is not mandatory, if not present
	 * the numeric value is assumed as SI.METRE ; if search fails return null
	 * <p>
	 * Examples:
	 *     <chord unit="cm">105</chord>
	 *     <chord >1.05</chord>
	 * <p>
	 * @param xmlDoc     the Document object
	 * @param xpath      the XPath object
	 * @param expression XPath expression
	 * @return           amount, dimensions according to unit attribute value
	 */
	public static Amount<Length> getXMLAmountLengthByPath(Document xmlDoc, XPath xpath, String expression) {

		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {

				Double value = Double.parseDouble(valueStr);
				Amount<Length> quantity;
				if (unitStr != null)
					quantity = (Amount<Length>) Amount.valueOf(value, 1e-9, Unit.valueOf(unitStr));
				else
					quantity = Amount.valueOf(value, 1e-8, SI.METER);

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

	/*
	 * Get the list of length quantities from XML path; unit attribute is not mandatory, if not present
	 * the numeric value is assumed as SI.METRE ; if search fails return null
	 * <p>
	 * Examples:
	 *     <chord unit="cm">105</chord>
	 *     <chord >1.05</chord>
	 * <p>
	 * @param xmlDoc     the Document object
	 * @param xpath      the XPath object
	 * @param expression XPath expression
	 * @return           amounts, dimensions according to unit attribute value
	 */
	public static double[] getXMLAmountsLengthByPath(Document xmlDoc, XPath xpath, String expression) throws XPathExpressionException {

		XPathExpression expr = null;
		try {
			expr = xpath.compile(expression);

		} catch (XPathExpressionException e1) {

			e1.printStackTrace();
		}
		NodeList nodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);


		List<String> list_elements = MyXMLReaderUtils.getXMLPropertiesByPath(xmlDoc, xpath, expression + "/text()");
		List<String> list_value =  MyXMLReaderUtils.getXMLPropertiesByPath(xmlDoc, xpath, expression + "/@unit");


		double[] values = new double [nodes.getLength()];

		Amount<Length> quantity;
		for (int i = 0; i < nodes.getLength(); i++){

			if ((list_elements.get(i) != null) && (!list_value.get(i).equals(""))) {
				try {

					Double value = Double.parseDouble(list_elements.get(i)); //converte in double il valore


					if (list_value.get(i)!= null)

						quantity = (Amount<Length>) Amount.valueOf(value, Unit.valueOf(list_value.get(i)));
					//quantity= quantity.to(SI.METRE).getEstimatedValue();



					else
						quantity = Amount.valueOf(value, SI.METER);
					System.out.println("Wing Span number " + (i+1) + "=" + quantity); // FIN QUI VA PER LA PRIMA ITERAZIONE


					values [i]= quantity.to(SI.METRE).getEstimatedValue();


				} catch (NumberFormatException| AmountException e) {
					e.printStackTrace();

					values[i]=0;

				}	}
			else

				values[i]=0;}


		return values;  }

	/*
	 * Get a speed quantity from XML path; unit attribute is not mandatory, if not present
	 * the numeric value is assumed as SI.METRE_PER_SECOND ; if search fails return null
	 * <p>
	 * Examples:
	 *     <speed unit="kts">105</speed>
	 *     <speed >155</speed>
	 * <p>
	 * @param xmlDoc     the Document object
	 * @param xpath      the XPath object
	 * @param expression XPath expression
	 * @return           amount, dimensions according to unit attribute value
	 */
	public static Amount<Velocity> getXMLAmountVelocityByPath(Document xmlDoc, XPath xpath, String expression) {

		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {
				Double value = Double.parseDouble(valueStr);
				Amount<Velocity> quantity;
				if (unitStr != null) {
					switch (unitStr) {
					case "kts": case "KTS":
					case "knots": case "KNOTS":
						unitStr = "kn";
						break;
					}					
					quantity = (Amount<Velocity>) Amount.valueOf(value, 1e-9, Unit.valueOf(unitStr));
				} else
					quantity = Amount.valueOf(value, 1e-8, SI.METERS_PER_SECOND);

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	
	/*
	 * Get a time quantity from XML path; unit attribute is not mandatory, if not present
	 * the numeric value is assumed as SI.SECOND ; if search fails return null
	 * <p>
	 * Examples:
	 *     <time unit="min"> 5 </time>
	 *     <time> 0.005 </time>
	 * <p>
	 * @param xmlDoc     the Document object
	 * @param xpath      the XPath object
	 * @param expression XPath expression
	 * @return           amount, dimensions according to unit attribute value
	 */
	public static Amount<javax.measure.quantity.Duration> getXMLAmountTimeByPath(Document xmlDoc, XPath xpath, String expression) {

		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {
				Double value = Double.parseDouble(valueStr);
				Amount<javax.measure.quantity.Duration> quantity;
				if (unitStr != null) {
					switch (unitStr) {
					case "sec": case "SEC":
						unitStr = "s";
						break;
					}					
					quantity = (Amount<javax.measure.quantity.Duration>) Amount.valueOf(value, 1e-9, Unit.valueOf(unitStr));
				} else
					quantity = Amount.valueOf(value, 1e-8, SI.SECOND);

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

	
	public static Amount<Angle> getXMLAmountAngleByPath(Document xmlDoc, XPath xpath, String expression) {

		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {

				Double value = Double.parseDouble(valueStr);
				Amount<Angle> quantity;
				if (unitStr != null) {
					switch (unitStr) {
					case "deg":
					case "DEG":
					case "Deg":
						unitStr = "°";
						break;
					}
					quantity = (Amount<Angle>) Amount.valueOf(value, 1e-9, Unit.valueOf(unitStr));
				} else
					quantity = Amount.valueOf(value, 1e-9, SI.RADIAN);

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	public static Amount<Angle> getXMLAmountAngleByPath(Document xmlDoc, String expression) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			Amount<Angle> quantity = MyXMLReaderUtils.getXMLAmountAngleByPath(xmlDoc, xpath, expression);
			return quantity;
		} catch (NumberFormatException | AmountException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	@SuppressWarnings("unchecked")
	public static Amount<?> getXMLAmountOnePerSecondByPath(Document xmlDoc, XPath xpath, String expression) {

		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {

				Double value = Double.parseDouble(valueStr);
				Amount<?> quantity;
				if (unitStr != null) {
					switch (unitStr) {
					case "s^(-1)":
					case "sec^(-1)":
					case "rad/s":
					case "RAD/s":
					case "rad/sec":
					case "RAD/SEC":
						unitStr = "1/s";
						break;
					}
					quantity = Amount.valueOf(value, 1e-12, MyUnits.ONE_PER_SECOND);
				} else
					quantity = Amount.valueOf(value, 1e-12, MyUnits.ONE_PER_SECOND);

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}
	public static Amount<?> getXMLAmountOnePerSecondByPath(Document xmlDoc, String expression) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			Amount<?> quantity = MyXMLReaderUtils.getXMLAmountOnePerSecondByPath(xmlDoc, xpath, expression);
			return quantity;
			
		} catch (NumberFormatException | AmountException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Amount<?> getXMLAmountOnePerSecondSquaredByPath(Document xmlDoc, XPath xpath, String expression) {
		
		String valueStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/text()");
		String unitStr = MyXMLReaderUtils.getXMLPropertyByPath(xmlDoc, xpath, expression + "/@unit");

		if ((valueStr != null) && (!valueStr.equals(""))) {
			try {

				Double value = Double.parseDouble(valueStr);
				Amount<?> quantity;
				if (unitStr != null) {
					switch (unitStr) {
					case "s^(-2)":
					case "sec^(-2)":
					case "rad/s²":
					case "rad/s^(-2)":
					case "RAD/s^(-2)":
					case "rad/sec^(-2)":
					case "RAD/SEC^(-2)":
						unitStr = "1/s²";
						break;
					}
					quantity = Amount.valueOf(value, 1e-9, MyUnits.ONE_PER_SECOND_SQUARED);
				} else
					quantity = Amount.valueOf(value, 1e-9, MyUnits.ONE_PER_SECOND_SQUARED);

				return quantity;

			} catch (NumberFormatException | AmountException e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

	public static Amount<?> getXMLAmountOnePerSecondSquaredByPath(Document xmlDoc, String expression) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			Amount<?> quantity = MyXMLReaderUtils.getXMLAmountOnePerSecondSquaredByPath(xmlDoc, xpath, expression);
			return quantity;
			
		} catch (NumberFormatException | AmountException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	
	
	// TODO: implement similar functions, such as:
	// getXMLAmountSurfaceByPath
	// getXMLAmountVolumeByPath
	// getXMLAmountAngleByPath
	// getXMLAmountMassByPath
	// etc
	
	

	/**
	 * Group together actions needed to import an xml document
	 *
	 * @author Lorenzo Attanasio
	 * @param filenameWithPathAndExt
	 * @return parsed document
	 */
	public static Document importDocument(String filenameWithPathAndExt){

		if (filenameWithPathAndExt == null) return null;

		if (!filenameWithPathAndExt.endsWith(".xml")
				&& !filenameWithPathAndExt.endsWith(".XML"))
			filenameWithPathAndExt = filenameWithPathAndExt + ".xml";

		Document _parsedDoc = null;
		DocumentBuilder builderImport;
		DocumentBuilderFactory factoryImport = DocumentBuilderFactory.newInstance();
		factoryImport.setNamespaceAware(true);
		factoryImport.setIgnoringComments(true);

		try {
			builderImport = factoryImport.newDocumentBuilder();
			_parsedDoc = builderImport.parse(filenameWithPathAndExt);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

		removeWhitespaceAndCommentNodes(_parsedDoc.getDocumentElement());
		System.out.println("File "+ filenameWithPathAndExt + " parsed.");

		return _parsedDoc;
	}

}

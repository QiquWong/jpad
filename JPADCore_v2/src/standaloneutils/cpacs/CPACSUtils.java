package standaloneutils.cpacs;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import standaloneutils.MyXMLReaderUtils;

/** 
 * Utility functions for CPACS frile manipulation
 * This class cannot be instantiated
 * 
 * @author Agostino De Marco
 * @author Giuseppe Torre
 * 
 */

public final class CPACSUtils {

	/**
	 * Returns a string made up of rows, that displays a 2D array in a tabular format
	 * recognized by JSBSim; element (0,0) of input matrix is unused
	 * 
	 * @param matrix, a 2D array; element (0,0) is unused
	 * @param string, column separator in the tabular output
	 * @return string, tabular display of the input matrix 
	 */
	public static String matrixDoubleToJSBSimTable2D(double[][] matrix, String separator) {

		StringBuffer result = new StringBuffer();

		// iterate over the first dimension
		for (int i = 0; i < matrix.length; i++) {
			// iterate over the second dimension
			for(int j = 0; j < matrix[i].length; j++){
				if(i==0&&j==0) {
					result.append("	");
				}
				else {
					result.append(matrix[i][j]);
					result.append(separator);
				}
			}
			// remove the last separator
			result.setLength(result.length() - separator.length());
			// add a line break.
			result.append("\n");
		}
		return result.toString();
	}

	/**
	 * Returns a string made up of N rows and 2 columns, that displays a 2D array in a tabular format
	 * recognized by JSBSim
	 * 
	 * @param matrix, a 2D array, Nx2, where N is the number of matrix rows
	 * @param string, column separator in the tabular output
	 * @return string, tabular display of the input matrix 
	 */
	public static String matrixDoubleToJSBSimTableNx2(double[][] matrix) {

		StringBuffer result = new StringBuffer();

		// iterate over the first dimension
		for (int i = 0; i < matrix.length; i++) {
			// iterate over the second dimension
			for(int j = 0; j < matrix[i].length; j++){

				result.append(matrix[i][j]);
				result.append("	");
			}
			// remove the last separator
			// add a line break.
			result.append("\n");
		}
		return result.toString();
	}	

	public static Double getWingChord(Node wingNode) {
		System.out.println("Reading main wing root chord ...");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(wingNode, true);
			doc.appendChild(importedNode);
			
			// get the list of sections in wingNode
			NodeList sections = MyXMLReaderUtils.getXMLNodeListByPath(doc, "//sections/section");

			System.out.println("sections found: " + sections.getLength());

			if (sections.getLength() == 0)
				return null;
			
			// get the first section chord
			String wingChordString = MyXMLReaderUtils.getXMLPropertyByPath(
					sections.item(0),
					"//elements/element/transformation/scaling/x/text()");
			
			System.out.println("wingChordString: " + wingChordString);
			return Double.parseDouble(wingChordString);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Return the position of the wing described in the wingNode (node in the CPACS of the wing)
	 */
	public static Double[] getWingPosition(Node wingNode) {
		System.out.println("Reading main wing leading edge ...");
		Double[] wingLEPosition = new Double[3];
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(wingNode, true);
			doc.appendChild(importedNode);
			
			// get x position
			String wingLEXPosition = MyXMLReaderUtils.getXMLPropertyByPath(
					importedNode,
					"//transformation/translation/x/text()");			
			System.out.println("wing LEADING EDGE x: " + wingLEXPosition);
			wingLEPosition[0] = Double.parseDouble(wingLEXPosition);
			// get Y position
			String wingLEYPosition = MyXMLReaderUtils.getXMLPropertyByPath(
					importedNode,
					"//transformation/translation/y/text()");
			System.out.println("wing LEADING EDGE y: " + wingLEYPosition);
			wingLEPosition[1] = Double.parseDouble(wingLEYPosition);
			//get Z position
			String wingLEZPosition = MyXMLReaderUtils.getXMLPropertyByPath(
					importedNode,
					"//transformation/translation/z/text()");
			System.out.println("wing LEADING EDGE Z: " + wingLEZPosition);
			wingLEPosition[2] = Double.parseDouble(wingLEZPosition);
			return wingLEPosition;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	public static Double[] getVectorPositionNodeTank(Node node, int i) {
		System.out.println("Reading tank position...");
		Double[] vectorPosition = new Double[3];
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(node, true);
			doc.appendChild(importedNode);
			NodeList sections = MyXMLReaderUtils.getXMLNodeListByPath(doc, "//mFuel/fuelInTanks/fuelInTank");
//			MyXMLReaderUtils.getXMLAmountLengthByPath(doc, xpath, expression);
			// get the list of sections in wingNode
			System.out.println("Length = " + sections.getLength());
			
			for (int j = 0;j<sections.getLength();j++) {
				Node nodeSystem  = sections.item(j); // .getNodeValue();
				Element SystemElement = (Element) nodeSystem;
				String xpath = MyXMLReaderUtils.getElementXpath(SystemElement);
				String path = xpath + "/coG/x/text()";
				List<String> wingTankPositionXx =  MyXMLReaderUtils.getXMLPropertiesByPath(doc, path);
				System.out.println("Xpath is = " + xpath);
				System.out.println("Pippo is = " + wingTankPositionXx.size());
				
				
			}
				
			
			
			
			String wingTankPositionX = MyXMLReaderUtils.getXMLPropertyByPath(
					sections.item(i),
					"//coG/x/text()");
			System.out.println("wingTankPositionX: " + wingTankPositionX);
			vectorPosition[0] = Double.parseDouble(wingTankPositionX);
			
			String wingTankPositionY = MyXMLReaderUtils.getXMLPropertyByPath(
					sections.item(i),
					"//coG/y/text()");
			System.out.println("wingTankPositionY: " + wingTankPositionY);
			vectorPosition[1] = Double.parseDouble(wingTankPositionY);
			
			String wingTankPositionZ = MyXMLReaderUtils.getXMLPropertyByPath(
					sections.item(i),
					"//coG/z/text()");
			System.out.println("wingTankPositionZ: " + wingTankPositionZ);
			vectorPosition[2] = Double.parseDouble(wingTankPositionZ);
			
			
			return vectorPosition;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
}

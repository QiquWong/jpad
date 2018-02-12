package standaloneutils.cpacs;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.analysis.function.Tan;
import org.apache.commons.math3.util.MathArrays;
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
					result.append("\n \t");
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

	public static String matrixDoubleToJSBSimColumn(double[] vector) {

		StringBuffer result = new StringBuffer();

		// iterate over the first dimension
		for (int i = 0; i < vector.length; i++) {
			// iterate over the second dimension
					result.append(vector[i]);
					result.append("\n");
			}
		return result.toString();
	}
	
	public static String matrixDoubleToJSBSimRow(double[] vector) {

		StringBuffer result = new StringBuffer();

		// iterate over the first dimension
		for (int i = 0; i < vector.length; i++) {
			// iterate over the second dimension
					result.append(vector[i]);
					result.append("	");
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
			System.out.println("wing LEADING EDGE x: " + wingLEXPosition);  //TO DO ask if necessary to remove  getXMLPropertyByPath props[0] 
			wingLEPosition[0] = Double.parseDouble(wingLEXPosition);        //from getXMLPropertyByPath sysout
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
				Element systemElement = (Element) nodeSystem;
				String xpath = MyXMLReaderUtils.getElementXpath(systemElement);
				String path = xpath + "/coG/x/text()";
				List<String> wingTankPositionXx =  MyXMLReaderUtils.getXMLPropertiesByPath(doc, path);
				System.out.println("Xpath is = " + importedNode.getLocalName());
				System.out.println("Position is = " + wingTankPositionXx.size());
				
				
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
	
	/**
	 * This function allow to estimate the position of the landing gear relative to wing
	 * @param wingNode node of the CPACS where landing gear are attached
	 * @param eta eta position of the landing gear
	 * @param xsi xsi position of the landing gear
	 * @param wingSpan 
	 * @param relHeight height of relative height position of the attachment 
	 * @return x,y,z position of landing gear relative to wing
	 */
	public static Double[] getPositionRelativeToEtaAndCsi(Node wingNode, double eta,
			double xsi, double wingSpan, double relHeight) {
		Double [] coordinate = new Double[3];
		double y = eta*wingSpan/2;
		double x = 0;
		double z = 0;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(wingNode, true);
			doc.appendChild(importedNode);
			// get the list of sections in wingNode to read wing chord and element length
			NodeList sectionsChord = MyXMLReaderUtils.getXMLNodeListByPath(doc, "//sections/section");
			NodeList sectionsLength = MyXMLReaderUtils.getXMLNodeListByPath(doc, "//positionings/positioning");

			System.out.println("--------------------------------");
			System.out.println("Read chord length : ");
			System.out.println("chord found: " + sectionsChord.getLength());
			System.out.println("--------------------------------");
			if (sectionsChord.getLength() == 0 || sectionsLength.getLength() == 0)
				return null;

			//Read from prescribed wing the length of each chord
			Double[] wingChordVector = new Double [sectionsChord.getLength()];

			System.out.println("--------------------------------");
			System.out.println("Read element length : ");
			System.out.println("element found: " + sectionsLength.getLength());
			System.out.println("--------------------------------");
			for (int i=0;i<sectionsChord.getLength();i++) {
				String wingChordString = MyXMLReaderUtils.getXMLPropertyByPath(
						sectionsChord.item(i),
						"//elements/element/transformation/scaling/x/text()");
				wingChordVector[i] =  Double.parseDouble(wingChordString);

			}
			//Read from prescribed wing the length of segment
			Double[] wingElementLength = new Double [sectionsLength.getLength()-1];
			Double[] wingEtaVectorCheck = new Double [sectionsLength.getLength()-1];
			Double[] coefficient = new Double [sectionsLength.getLength()-1]; 
			double wingRootChord = 0;
			double wingTipChord = 0;
			double chordLength = 0;
			double height = 0;
			for (int i=0;i<sectionsLength.getLength()-1;i++) {
				String wingElementLengthString = MyXMLReaderUtils.getXMLPropertyByPath(
						sectionsLength.item(i+1),
						"//length/text()");
				String wingDihedralAngleString = wingElementLengthString = MyXMLReaderUtils.getXMLPropertyByPath(
						sectionsLength.item(i+1),
						"//dihedralAngle/text()");
				double wingDihedralAngleDouble = Double.parseDouble(wingDihedralAngleString);
				wingElementLength[i] =  Double.parseDouble(wingElementLengthString);
				wingRootChord = wingChordVector[i];
				wingTipChord = wingChordVector[i+1];
				coefficient[i] = (wingTipChord-wingRootChord)/wingElementLength[i];
				if (i==0) {
					wingEtaVectorCheck[i] = wingElementLength[i];
					if(y<wingEtaVectorCheck[i]) {
						chordLength = coefficient[i]*y+wingRootChord;
						x = xsi*chordLength; 
						z = y*Math.tan(Math.toRadians(wingDihedralAngleDouble));
					}
					height = wingElementLength[i]
							*Math.tan(Math.toRadians(wingDihedralAngleDouble));
				}
				else {
					wingEtaVectorCheck[i] = wingElementLength[i]+wingElementLength[i-1];
					if((y<wingEtaVectorCheck[i]) && (y>wingEtaVectorCheck[i-1])) {
						chordLength = coefficient[i]*y+wingRootChord;
						x = xsi*chordLength;
						z = height + y*Math.tan(Math.toRadians(wingDihedralAngleDouble));;
					}
					height = height + wingElementLength[i]
							*Math.tan(Math.toRadians(wingDihedralAngleDouble));
				}
				if (y==0) {
					x = xsi*wingChordVector[i];
				}
				System.out.println("wingEtaVectorCheck = "+ wingEtaVectorCheck[i]);
			}

			coordinate[0] = x;
			coordinate[1] = y;
			coordinate[2] = z-relHeight;// relHeight in the CPACS this value is positive
			return coordinate;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
		
	} 
	/**
	 * 
	 * @param arrayData
	 * @return
	 */
	public static double [] getDoubleArrayFromString(String [] arrayData) {
		double[] doubleArray = new double [arrayData.length];
		for (int i=0; i<arrayData.length;i++) {
			doubleArray[i] = Double.parseDouble(arrayData[i]);
		}
		
		return doubleArray;
	}
	/**
	 * 
	 * @param listData
	 * @return
	 */
	public static double [] getDoubleArrayFromStringList(List<String> listData) {
		String[] arrayData = listData.get(0).split(";");
		double[] doubleArray = new double [arrayData.length];
		for (int i=0; i<arrayData.length;i++) {
			doubleArray[i] = Double.parseDouble(arrayData[i]);
		}
		
		return doubleArray;
	}
	public static double [] shiftElementInTheAeroPerformanceMap(List<String> listAeroPerformanceMap, int alpha, int yaw) {
		double[] inputVector =  CPACSUtils.getDoubleArrayFromStringList(listAeroPerformanceMap);
		double[] outputVector =  CPACSUtils.getDoubleArrayFromStringList(listAeroPerformanceMap);
		int counter = 0;
		int flag = alpha*yaw - 1;
		int lastIndex = alpha*yaw - 1;
		int fistIndex = lastIndex - alpha + 1;
		int j = 0;
		while (counter != (inputVector.length - 1)) {
			for (int i = 0; i< inputVector.length; i++) {
				if ((i >= fistIndex)&&(i<=lastIndex)) {
					outputVector[j] = inputVector[i];
					j = j + 1;  
					
				}
			}
			if ((fistIndex - (flag - alpha*yaw + 1))  != 0 ) {
				fistIndex = fistIndex - alpha;
				lastIndex = lastIndex - alpha;
			}
			else {
				
				lastIndex = flag + alpha*yaw;
				fistIndex = lastIndex - alpha + 1;
				flag = flag +  alpha*yaw;
			}
			counter = counter + 1;
		}
		return outputVector;
	}
	
	public static double [] shiftElementInTheAeroPerformanceMapControlSurface(
			List<String> listAeroPerformanceMap, int alpha, int yaw, int delta) {
		double[] inputVector =  CPACSUtils.getDoubleArrayFromStringList(listAeroPerformanceMap);
		double[] outputVector = inputVector;
		int counter = 0;
		int flag = delta*alpha*yaw - 1;
		int lastIndex = delta*alpha*yaw - 1;
		int fistIndex = lastIndex - alpha*delta + 1;
		int j = 0;
		while (counter != (inputVector.length - 1)) {
			for (int i = 0; i< inputVector.length; i++) {
				if ((i >= fistIndex)&&(i<=lastIndex)) {
					outputVector[j] = inputVector[i];
					j = j + 1;  
					
				}
			}
			if ((fistIndex - (flag - delta*alpha*yaw + 1))  != 0 ) {
				fistIndex = fistIndex - delta*alpha;
				lastIndex = lastIndex - delta*alpha;
			}
			else {
				
				lastIndex = flag + delta*alpha*yaw;
				fistIndex = lastIndex - delta*alpha + 1;
				flag = flag +  delta*alpha*yaw;
			}
			counter = counter + 1;
		}
		return outputVector;
	}
		
}

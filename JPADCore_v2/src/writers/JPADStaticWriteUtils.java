package writers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisCrossBetween;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.AxisTickMark;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LayoutTarget;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.LineChartData;
import org.apache.poi.ss.usermodel.charts.LineChartSerie;
import org.apache.poi.ss.usermodel.charts.ScatterChartData;
import org.apache.poi.ss.usermodel.charts.ScatterChartSerie;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.azeckoski.reflectutils.ReflectUtils;
import org.jscience.physics.amount.Amount;
import org.omg.PortableInterceptor.NON_EXISTENT;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import aircraft.components.Aircraft;
import aircraft.components.LandingGears;
import aircraft.components.Systems;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator.MountingPosition;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.MethodEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple4;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXLSUtils;
import standaloneutils.customdata.MyArray;

public class JPADStaticWriteUtils {

	/** 
	 * Utility class useful to store results and 
	 * access them in a simple yet meaningful way
	 *  
	 * @author Lorenzo Attanasio
	 * @param <T> generic parameter
	 */
	public static class StoreResults<T> {

		private List<T> lis = new ArrayList<T>();

		public void setMean(T a) {
			this.lis.add(0, a);
		}

		public T getMean() {
			return this.lis.get(0);
		}

		public void setFilteredMean(T a) {
			this.lis.add(1, a);
		}

		public T getFilteredMean() {
			return this.lis.get(1);
		}

	}

	public static void logToConsole(String message) {
		System.out.println(message);
	}

	public static Amount<?> cloneAmount(Amount<?> valueToClone){
		return Amount.valueOf(valueToClone.getEstimatedValue(), valueToClone.getUnit());
	}

	public static void checkIfOutputDirExists(String dirName) {
		File theDir = new File(dirName);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: " + dirName);
			boolean result = false;
			try {
				theDir.mkdir();
				result = true;
			} catch(SecurityException se){
				//handle it
			}        
			if (result) {    
				System.out.println("DIR created");  
			}
		}
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param doc
	 * @param filenameWithPathAndExt
	 */
	public static void writeDocumentToXml(Document doc, String filenameWithPathAndExt) {
		try {
			//System.out.println(""+doc);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult((new File(filenameWithPathAndExt)));
			transformer.transform(source, result);
			System.out.println("Data successfully written to " + filenameWithPathAndExt);

		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public static void exportToCSV (
			List<Double[]> xList, 
			List<Double[]> yList,
			List<String> fileName,
			List<String> xListName,
			List<String> yListName,
			String outFileFolderPath 
			) {
		
		if(xList.size() != yList.size()) {
			System.err.println("X AND Y LISTS MUST HAVE THE SAME SIZE !!");
			return;
		}
		
		for (int i = 0; i < xList.size(); i++) {
		
			File outputFile = new File(outFileFolderPath + File.separator + fileName.get(i) + ".csv");
			
			if (outputFile.exists()) {
				try {
					System.out.println("\tDeleting the old .csv file ...");
					Files.delete(outputFile.toPath());
				} 
				catch (IOException e) {
					System.err.println(e + " (Unable to delete file)");
				}
			}
			
			try{
				
				System.out.println("\tCreating " + fileName.get(i) + ".csv file ... ");
				
				PrintWriter writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");
				writer.println(xListName.get(i) + ", " + yListName.get(i));

				if(xList.get(i).length != yList.get(i).length) {
					System.err.println("CORRESPONDING ELEMENTS OF THE TWO LISTS MUST HAVE THE SAME LENGTH");
					return;
				}

				for (int j = 0; j < xList.get(i).length; j++) {
					writer.println(
							String.format(
									Locale.ROOT,
									"%1$11.6f, %2$11.6f",
									xList.get(i)[j],
									yList.get(i)[j]
									)
							);
				}

				writer.close();


			} catch (Exception e) {
				System.err.format("Unable to write file %1$s\n", outputFile.getAbsolutePath());
			}
		}

	}

	/**
	 * Add an element nested inside a father element
	 * Manage the writing to the xls file as well
	 * 
	 * @author Lorenzo Attanasio
	 * @param doc
	 * @param sheet
	 * @param elementName
	 * @param father
	 * @return
	 */
	public static Element addSubElement(Document doc, Sheet sheet, String elementName, Element father) {

		sheet.createRow(sheet.getLastRowNum()+1).createCell((short) 0).setCellValue("");
		Cell cell0 = sheet.createRow(sheet.getLastRowNum()+1).createCell((short) 0);
		cell0.setCellValue("ANALYSIS");
		cell0.setCellStyle(MyXLSUtils.styleTitlesFirstCol);

		// --- Analysis results ---------------------------------------------------------------------
		Element analysis = doc.createElement(elementName);
		father.appendChild(analysis);

		return analysis;
	}

	/**
	 * @author Lorenzo Attanasio
	 * @param doc
	 * @param _sheet
	 * @param elementName
	 * @param father
	 * @return
	 */
	public static Element addElementToSubElement(Document doc, Sheet _sheet, String elementName, Element father) {

		Cell cell0 = _sheet.createRow(_sheet.getLastRowNum()+1).createCell((short) 0);
		cell0.setCellValue(" ");

		Cell cell1 = _sheet.createRow(_sheet.getLastRowNum()+1).createCell((short) 0);
		cell1.setCellValue(StringUtils.replace(elementName, "_", " "));
		cell1.setCellStyle(MyXLSUtils.styleSubtitles);

		Element element = doc.createElement(elementName); 
		father.appendChild(element);

		return element;
	}

	public static <T extends Quantity> MyArray unknownTypeArrayToMyArray(Object valueToWrite) {

		MyArray arrayToXls = new MyArray(Unit.ONE);

		if (valueToWrite instanceof Double[]){
			arrayToXls.setDouble((Double[])valueToWrite);
			valueToWrite = (Double[]) valueToWrite;

		} else if (valueToWrite instanceof double[]){
			arrayToXls.setDouble((double[])valueToWrite);
			valueToWrite = (double[]) valueToWrite;

		} else if (valueToWrite instanceof Integer[]) {
			arrayToXls.setInteger((Integer[])valueToWrite);
			valueToWrite = (Integer[]) valueToWrite;

		} else if(valueToWrite instanceof MyArray) {
			arrayToXls = (MyArray) valueToWrite;
			valueToWrite = (MyArray) valueToWrite;

		} else if (valueToWrite instanceof List){

			valueToWrite = (List<?>) valueToWrite;

			if ( ((List) valueToWrite).size() != 0 ) {

				if (((List) valueToWrite).get(0) instanceof Amount) { 

					arrayToXls.setAmountList((List<Amount<T>>) valueToWrite);
					List<BigDecimal> tempList = new ArrayList<BigDecimal>();
					MathContext mc = new MathContext(6);
					Amount<?> tempAmount = null;

					// Round list of Amount<?>
					for (int i=0; i < ((List)valueToWrite).size(); i++) {
						tempAmount = (Amount<?>) ((List)valueToWrite).get(i);
						tempList.add(BigDecimal.valueOf(
								tempAmount.getEstimatedValue()
								).round(mc));
					}
					arrayToXls.setUnit(tempAmount.getUnit());
					//					
					//					unit = tempAmount.getUnit().toString();
					//					value = tempList.toString();

				} else if (((List) valueToWrite).get(0) instanceof Double){
					arrayToXls.setList((List<Double>) valueToWrite);
				}

			} 
			//			else {
			//				value = ((List) valueToWrite).toString(); 
			//			}
		}

		return arrayToXls;
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param valueToWrite
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String[] prepareSingleVariableToWrite(Object valueToWrite, int...aboveBelow) {

		String value = "", unit = "";
		double roundingThreshold = 1e-2;
		int roundingAboveThreshold = 4;
		int roundingBelowThreshold = 6;
		if (aboveBelow.length > 0) {
			roundingAboveThreshold = aboveBelow[0];
			if (aboveBelow.length > 1)
				roundingBelowThreshold = aboveBelow[1];
		}
		

		if (valueToWrite != null && !valueToWrite.getClass().isArray() ) {
			if (valueToWrite.getClass() == double.class && !Double.isNaN((double)valueToWrite) && Double.isFinite((Double)valueToWrite)){

				if (((Double) valueToWrite) < roundingThreshold) {
					value = BigDecimal.valueOf((double) valueToWrite).setScale(roundingBelowThreshold, RoundingMode.HALF_UP).toString();						
				} else {
					value = BigDecimal.valueOf((double) valueToWrite).setScale(roundingAboveThreshold, RoundingMode.HALF_UP).toString();
				}

				unit = Unit.ONE.toString();
				valueToWrite = (double) valueToWrite;

			} else if(valueToWrite instanceof Double && !Double.isNaN((Double)valueToWrite) && Double.isFinite((Double)valueToWrite)) {

				if (((Double) valueToWrite) < roundingThreshold) {
					value = String.valueOf(BigDecimal.valueOf(((Double) valueToWrite)).setScale(roundingBelowThreshold, RoundingMode.HALF_UP));
				} else {
					value = String.valueOf(BigDecimal.valueOf(((Double) valueToWrite)).setScale(roundingAboveThreshold, RoundingMode.HALF_UP));
				}
				unit = Unit.ONE.toString();
				valueToWrite = (Double) valueToWrite;

			} else if(valueToWrite instanceof Amount<?>) {

				if (!Double.isNaN(((Amount) valueToWrite).getEstimatedValue())) {
					
					if (((Amount<?>) valueToWrite).getUnit().equals(SI.RADIAN)) {
						value = String.valueOf(
								BigDecimal.valueOf(
												((Amount<?>) valueToWrite).to(NonSI.DEGREE_ANGLE).getEstimatedValue()).setScale(4, RoundingMode.HALF_UP));	
						unit = ((Amount<?>) valueToWrite).to(NonSI.DEGREE_ANGLE).getUnit().toString();

					} else {
						// Check for necessary significant digits
						if (((Amount<?>) valueToWrite).getEstimatedValue() < roundingThreshold) {
							value = String.valueOf(
									BigDecimal.valueOf(((Amount<?>) valueToWrite).getEstimatedValue())
									.stripTrailingZeros()
									.setScale(roundingAboveThreshold, RoundingMode.HALF_UP));						
						} else {
							value = String.valueOf(
									BigDecimal.valueOf(((Amount<?>) valueToWrite).getEstimatedValue())
									.setScale(roundingAboveThreshold, RoundingMode.HALF_UP));
						}
						unit = ((Amount<?>) valueToWrite).getUnit().toString();
					}
				}

			} else if (valueToWrite instanceof Integer){
				value = ((Integer) valueToWrite).toString();
				valueToWrite = (Integer) valueToWrite;

			} else if (valueToWrite instanceof Boolean){
				valueToWrite = (Boolean) valueToWrite;
				value = valueToWrite.toString();

			} else if (valueToWrite.getClass().equals(boolean.class)){
				valueToWrite = (boolean) valueToWrite;
				value = valueToWrite.toString();

			} else if (valueToWrite instanceof Enum){
				valueToWrite = (Enum<?>) valueToWrite;
				value = ((Enum) valueToWrite).name().toString();

			} else if(valueToWrite instanceof ArrayList<?>){
				if (((ArrayList) valueToWrite).get(0) instanceof Amount<?>){
					List<Double> valueList = new ArrayList<>();
					List<Amount> amountList = new ArrayList<>();
					amountList = (ArrayList) valueToWrite;
					unit = amountList.get(0).getUnit().toString();
					for (int i=0; i<((ArrayList) valueToWrite).size() ; i++){
					valueList.add(i, amountList.get(i).getEstimatedValue() );
					}
					value = valueList.toString();
				}
				else {
					value = valueToWrite.toString();
				}
			}
		    else {
				value = valueToWrite.toString();
			}

		} else {
			value = MyConfiguration.notInitializedWarning;
		}

		String[] str = new String[2];
		str[0] = value;
		str[1] = unit;

		// April, 4, 2017 - Trifari, De Marco: strip [ and ], replace "," with ";"
//		str[0].replace("[", "").replace("]", "").replace(",", ";");
//		str[1].replace("[", "").replace("]", "").replace(",", ";");
		
		return str;
	}

	/**
	 * This method is an overload of the previous that accepts a string and the unit as input in order to write an array whit unit.
	 * 
	 * @author Manuela Ruocco
	 *
	 * @param valueToWrite
	 * @param unit
	 * @return
	 */
	public static String[] prepareSingleVariableToWrite(List<Amount> inputList, String unit) {

		String arrayToWrite = new String();
	
		arrayToWrite = " [ " ;
		for (int i=0; i<inputList.size() -1 ; i++){
			arrayToWrite = arrayToWrite + inputList.get(i).getEstimatedValue() + " , ";
		}
		arrayToWrite = arrayToWrite + inputList.get(inputList.size()-1).getEstimatedValue();
		arrayToWrite = arrayToWrite + " ]";
		
		String[] str = new String[2];
		str[0] = arrayToWrite;
		str[1] = unit;

		return str;
	}
	
	/**
	 * This method is an overload of the previous that accepts a string and the unit as input in order to write an array whit unit.
	 * 
	 * @author Manuela Ruocco
	 *
	 * @param valueToWrite
	 * @param unit
	 * @return
	 */

	public static String[] prepareSingleVariableToWriteCPACSFormat(Object inputList, String unit) {

		String arrayToWrite = new String();
		String[] str = new String[2];
		if (inputList instanceof double []){
			
			double [] inputListdouble = (double [] )inputList;
			
		arrayToWrite = "" ;
		for (int i=0; i<inputListdouble.length -1 ; i++){
			arrayToWrite = arrayToWrite + inputListdouble[i] + ";";
		}
		arrayToWrite = arrayToWrite + inputListdouble[inputListdouble.length-1];
		arrayToWrite = arrayToWrite + "";
		
		str[0] = arrayToWrite;
		str[1] = unit;
		}
		
		if (inputList instanceof Double[]){
			
			Double [] inputListdouble = (Double [] )inputList;
			
		arrayToWrite = "" ;
		for (int i=0; i<inputListdouble.length -1 ; i++){
			arrayToWrite = arrayToWrite + inputListdouble[i] + ";";
		}
		arrayToWrite = arrayToWrite + inputListdouble[inputListdouble.length-1];
		arrayToWrite = arrayToWrite + "";
		
		str[0] = arrayToWrite;
		str[1] = unit;
		}
		
   if (inputList instanceof List<?> ){
			
			List<Amount> inputListdouble = (List)inputList;
			
		arrayToWrite = "" ;
		for (int i=0; i<inputListdouble.size() -1 ; i++){
			arrayToWrite = arrayToWrite + inputListdouble.get(i).getEstimatedValue() + ";";
		}
		arrayToWrite = arrayToWrite + inputListdouble.get(inputListdouble.size()-1).getEstimatedValue();
		arrayToWrite = arrayToWrite + "";
		
		str[0] = arrayToWrite;
		str[1] = unit;
		}
		
		return str;
	}
	
	public static String[] prepareSingleVariableToWriteCPACSFormat(Object inputList) {

		String arrayToWrite = new String();
		String[] str = new String[2];
		if (inputList instanceof double [] 	){
			
			double [] inputListdouble = (double [] )inputList;
			
		arrayToWrite = "" ;
		for (int i=0; i<inputListdouble.length -1 ; i++){
			arrayToWrite = arrayToWrite + inputListdouble[i] + ";";
		}
		arrayToWrite = arrayToWrite + inputListdouble[inputListdouble.length-1];
		arrayToWrite = arrayToWrite + "";
		
		str[0] = arrayToWrite;
		str[1] = "";
		}
		
		if (inputList instanceof Double[]){
			
			Double [] inputListdouble = (Double [] )inputList;
			
		arrayToWrite = "" ;
		for (int i=0; i<inputListdouble.length -1 ; i++){
			arrayToWrite = arrayToWrite + inputListdouble[i] + ";";
		}
		arrayToWrite = arrayToWrite + inputListdouble[inputListdouble.length-1];
		arrayToWrite = arrayToWrite + "";
		
		str[0] = arrayToWrite;
		str[1] = "";
		}
		
         if (inputList instanceof List<?> ){
			
			List<Double> inputListdouble = (List)inputList;
			
		arrayToWrite = "" ;
		for (int i=0; i<inputListdouble.size() -1 ; i++){
			arrayToWrite = arrayToWrite + inputListdouble.get(i) + ";";
		}
		arrayToWrite = arrayToWrite + inputListdouble.get(inputListdouble.size()-1);
		arrayToWrite = arrayToWrite + "";
		
		str[0] = arrayToWrite;
		str[1] = "";
		}
		
		return str;
	}
	
	public static String[] prepareSingleVariableToWrite(String inputList, String unit) {
		
		String[] str = new String[2];
		str[0] = inputList;
		str[1] = unit;

		return str;
	}
	
	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param description
	 * @param valueToWrite
	 * @param listMyArray
	 * @param listDescription
	 * @param listUnit
	 * @param notInitializedWarning
	 * @return
	 */
	public static String[] prepareVariableToWrite(
			String description, Object valueToWrite, 
			List<MyArray> listMyArray, List<String> listDescription,
			List<String> listUnit, String notInitializedWarning){

		String[] str = new String[2];

		if (valueToWrite.getClass().isArray()) 
			str = prepareArrayToWrite(description, valueToWrite, listMyArray, listDescription, listUnit, notInitializedWarning);
		else
			str = prepareSingleVariableToWrite(valueToWrite);

		return str;
	}

	public static String[] prepareArrayToWrite(String description, Object valueToWrite, 
			List<MyArray> listMyArray, List<String> listDescription,
			List<String> listUnit, String notInitializedWarning) {

		MyArray arrayToXls;
		String unit = "";
		String value = notInitializedWarning;
		String[] str = new String[2];
		str[0] = value;
		str[1] = unit;

		if (valueToWrite == null) {
			valueToWrite = (String) notInitializedWarning;

		} else {

			if (valueToWrite.getClass().isArray() 
					&& listMyArray.size() != 0
					&& listDescription.size() != 0
					&& listUnit.size() != 0) {

				arrayToXls = unknownTypeArrayToMyArray(valueToWrite);
				listMyArray.add(arrayToXls);
				listDescription.add(description);
				listUnit.add(unit);
				value = arrayToXls.round(8).toString();
				str[0] = value;
				str[1] = arrayToXls.getUnit().toString();
			}
		}

		return str;
	}

	/**
	 * Add a new line in the xml/xls file with input or output data
	 * The method write the variable name in the xml file to eventually read it back.
	 * THIS PROCEDURE WORKS ONLY IF EACH VARIABLE IN THE SAME CLASS HAS A DIFFERENT
	 * MEMORY LOCATION.
	 * 
	 * This means an Integer must be initialized as:
	 * 
	 * Integer integer = new Integer(value);
	 * and not as:
	 * Integer integer = value;
	 * 
	 * otherwise if two Integer have the same value they will have the same memory location
	 * 
	 * @author Lorenzo Attanasio
	 * @param tagName description of the variable which has to be written
	 * @param valueToWrite value of the variable
	 * @param father father element in the xml tree 
	 * @param fatherObject the object in which the variable exists
	 * @param doc the (xml) document object
	 * @param sheet the xls sheet in which the variable has to be included
	 * @param variablesMap a map which contains all the variables in the current father object
	 * @param arraysList a list which contains all the array which will be ultimately written to file
	 * @param arraysDescription a list containing description for each array in arraysList 
	 * @param arraysUnit a list containing the units for each array in arraysList
	 * @param reflectUtilsInstance 
	 * @param notInitializedWarning
	 * @param writeToXls
	 * @param variableSource
	 */
	public static void writeNode(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Object fatherObject, Document doc, 
			Sheet sheet, 
			Multimap<Object, String> variablesMap, 
			List<MyArray> arraysList, List<String> arraysDescription, List<String> arraysUnit, 
			ReflectUtils reflectUtilsInstance, String notInitializedWarning, 
			boolean writeToXls, boolean input){

		List<Object> list = writeNode(tagName, valueToWrite, father, doc, arraysList, arraysDescription, arraysUnit, notInitializedWarning, input);

		if (input)
			writeVariableNameAsAttribute(valueToWrite, (Element) list.get(2), fatherObject, 
					variablesMap, reflectUtilsInstance);

		if (valueToWrite != null && !valueToWrite.getClass().isArray() 
				&& writeToXls == true && sheet != null) 
			writeSingleValueToXls(sheet, tagName, (String) list.get(0), (String) list.get(1));
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param tagName
	 * @param valueToWrite
	 * @param father
	 * @param doc
	 * @param arraysList
	 * @param arraysDescription
	 * @param arraysUnit
	 * @param notInitializedWarning
	 * @param input
	 * @return
	 */
	public static List<Object> writeNode(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc, 
			List<MyArray> arraysList, List<String> arraysDescription, List<String> arraysUnit,
			String notInitializedWarning, 
			boolean input) {

		if (!(valueToWrite == null) && valueToWrite.getClass().isArray()) 
			return writeArrayNode(tagName, valueToWrite, father, doc, arraysList, arraysDescription, arraysUnit, notInitializedWarning, input);
		else
			return writeSingleNode(tagName, valueToWrite, father, doc, input);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param tagName
	 * @param valueToWrite
	 * @param father
	 * @param doc
	 * @param notInitializedWarning
	 * @param input
	 * @return
	 */
	public static List<Object> writeSingleNode(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc, 
			boolean input) {

		String[] str = new String[2];

		str = prepareSingleVariableToWrite(valueToWrite);

		return finalizeNode(str, tagName, father, doc, input);
	}

	public static List<Object> writeSingleNode(
			String tagName, 
			List<Amount> valueToWrite, 
			Element father,
			Document doc, 
			boolean input,
			String unit) {

		String[] str = new String[2];

		str = prepareSingleVariableToWrite(valueToWrite, unit);

		return finalizeNode(str, tagName, father, doc, input);
	}
	
	public static List<Object> writeSingleNodeCPACSFormat(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc, 
			boolean input,
			String unit) {

		String[] str = new String[2];

		str = prepareSingleVariableToWriteCPACSFormat(valueToWrite, unit);

		return finalizeNode(str, tagName, father, doc, input);
	}
	
	public static List<Object> writeSingleNodeCPACSFormat(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc, 
			boolean input) {

		String[] str = new String[2];

		str = prepareSingleVariableToWriteCPACSFormat(valueToWrite);

		return finalizeNode(str, tagName, father, doc, input);
	}
	
	public static List<Object> writeSingleNode(
			String tagName, 
			String valueToWrite, 
			Element father,
			Document doc, 
			boolean input,
			String unit) {

		String[] str = new String[2];

		str = prepareSingleVariableToWrite(valueToWrite, unit);

		return finalizeNode(str, tagName, father, doc, input);
	}
	
	public static List<Object> writeSingleNode(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc) {

		return writeSingleNode(tagName, valueToWrite, father, doc, false);
	}
	
	/**
	 * 
	 * @author Manuela Ruocco
	 *
	 * overload 
	 * 
	 */
	
	public static List<Object> writeSingleNode(
			String tagName, 
			List<Amount> valueToWrite, 
			Element father,
			Document doc,
			String unit) {

		return writeSingleNode(tagName, valueToWrite, father, doc, false, unit);
	}
	
	public static List<Object> writeSingleNodeCPASCFormat(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc,
			String unit) {

		return writeSingleNodeCPACSFormat(tagName, valueToWrite, father, doc, false, unit);
	}
	
	public static List<Object> writeSingleNodeCPASCFormat(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc) {

		return writeSingleNodeCPACSFormat(tagName, valueToWrite, father, doc, false);
	}
	
	public static List<Object> writeSingleNode(
			String tagName, 
			String valueToWrite, 
			Element father,
			Document doc,
			String unit) {

		return writeSingleNode(tagName, valueToWrite, father, doc, false, unit);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param tagName
	 * @param valueToWrite
	 * @param father
	 * @param doc
	 * @param arraysList
	 * @param arraysDescription
	 * @param arraysUnit
	 * @param notInitializedWarning
	 * @param input
	 * @return
	 */
	public static List<Object> writeArrayNode(
			String tagName, 
			Object valueToWrite, 
			Element father,
			Document doc, 
			List<MyArray> arraysList, List<String> arraysDescription, List<String> arraysUnit,
			String notInitializedWarning, 
			boolean input) {

		String[] str = new String[2];

		if (valueToWrite.getClass().isArray()) 
			str = prepareArrayToWrite(tagName, valueToWrite, arraysList, arraysDescription, arraysUnit, notInitializedWarning);

		return finalizeNode(str, tagName, father, doc, input);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param str
	 * @param tagName
	 * @param father
	 * @param doc
	 * @param input
	 * @return
	 */
	private static List<Object> finalizeNode(String[] str,
			String tagName, 
			Element father,
			Document doc, 
			boolean input) {

		String value = str[0];
		String unit = str[1];

		Element element = doc.createElement(tagName.replace(" ", "").replace(" ", "_"));
		
		if (unit.length() != 0)
			element.setAttribute("unit", unit);
//		else
//			element.setAttribute("unit", "");
// April, 4, 2017 - modified to handle non-dimensional numbers without --> unit=""

		if (input) element.setAttribute("from", "input");

		element.appendChild(doc.createTextNode(value));
		father.appendChild(element);

		List<Object> list = new ArrayList<Object>();
		list.add(value);
		list.add(unit);
		list.add(element);

		return list;
	}


	/**
	 * 
	 * In Java, when the '==' operator is used to compare 2 objects, 
	 * it checks to see if the objects refer to the same place in memory. 
	 * In other words, it checks to see if the 2 object names are basically
	 * references to the same memory location.
	 * For example:
	 *
	 * String x = "hello";
	 * String y = new String(new char[] { 'h', 'e', 'l', 'l', 'o' });

	 * System.out.println(x == y); // false
	 * System.out.println(x.equals(y)); // true
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param valueToWrite
	 * @param element
	 * @param fatherObject
	 * @param variablesMap
	 * @param reflectUtilsInstance
	 */
	public static void writeVariableNameAsAttribute(Object valueToWrite, Element element, 
			Object fatherObject, Multimap<Object, String> variablesMap, 
			ReflectUtils reflectUtilsInstance) {

		if (valueToWrite == null 
				|| element == null
				|| valueToWrite instanceof String
				|| variablesMap == null
				|| fatherObject == null
				|| reflectUtilsInstance == null) { 
			return;
		}

		String[] variableToString = null;

		// Transform collection of potential variable name (more than one name
		// could correspond to the same value) in an array of strings
		variableToString = variablesMap.get(valueToWrite)
				.toArray(new String[variablesMap.get(valueToWrite).size()]);

		//			Field[] field = _objectToWrite.getClass().getDeclaredFields();

		// Search for correspondence variable <---> variable name
		for (int i=0; i < variablesMap.get(valueToWrite).size(); i++) {

			// Check if the value corresponding to variableToString[i] string
			// (variable name) has the same memory location of node
			if (reflectUtilsInstance.getFieldValue(fatherObject, variableToString[i]) == valueToWrite){
				element.setAttribute("varName", variableToString[i]);
			}
		}
	}

	/**
	 * Write single value to xls file
	 * 
	 * @author Lorenzo Attanasio
	 * 
	 * @param sheet
	 * @param description
	 * @param value
	 * @param unit
	 */
	public static void writeSingleValueToXls(Sheet sheet, String description, String value, String unit) {

		if (value == null
				|| sheet == null) return;

		// Create a row and put some cells in it. Rows are 0 based.
		Row row = sheet.createRow(sheet.getLastRowNum() + 1);

		// Create a cell and put a value in it.
		row.createCell(0).setCellValue(description.replace("_", " "));
		row.getCell(0).setCellStyle(MyXLSUtils.styleFirstColumn);

		row.createCell(1).setCellValue(unit);
		row.createCell(2).setCellValue(value);

		for (int i = 1; i < 3; i++){
			row.getCell(i).setCellStyle(MyXLSUtils.styleDefault);
		}
	}

	/** 
	 * Write arrays at the end of the xls
	 * 
	 * @param sheet TODO
	 * @param _xlsArraysDescription TODO
	 * @param xlsArraysList TODO
	 * @param xlsArraysUnit TODO
	 */
	public static void writeAllArraysToXls(Sheet sheet, 
			List<String> _xlsArraysDescription, List<MyArray> xlsArraysList, List<String> xlsArraysUnit) {

		if (xlsArraysList.size() != 0) {

			int startingRow = sheet.getLastRowNum() + 1;
			int currentRow = startingRow;

			sheet.createRow(currentRow).createCell(0).setCellValue(" ");
			currentRow++;

			Cell cellTitle = sheet.createRow(currentRow).createCell(0);
			cellTitle.setCellValue("Arrays");
			cellTitle.setCellStyle(MyXLSUtils.styleTitlesFirstCol);
			currentRow++;

			Row descriptionRow = sheet.createRow(currentRow);
			currentRow++;

			Row unitRow = sheet.createRow(currentRow);
			currentRow++;

			descriptionRow.createCell(0).setCellValue("Description");
			unitRow.createCell(0).setCellValue("Unit");

			for (int k = 0; k < xlsArraysList.size(); k++) {

				descriptionRow.createCell(k+1).setCellValue(_xlsArraysDescription.get(k).replace("_", " "));
				unitRow.createCell(k+1).setCellValue(xlsArraysUnit.get(k));

				for (int i=0; i < xlsArraysList.get(k).size(); i++) {

					if (sheet.getRow(currentRow + i) != null) {
						sheet.getRow(currentRow + i).createCell(k+1).setCellValue(xlsArraysList.get(k).get(i));
					} else {
						sheet.createRow(currentRow + i).createCell(k+1).setCellValue(xlsArraysList.get(k).get(i));
					}
				}
			}
		}
	}

	/** 
	 * Write arrays at the end of the xls
	 * 
	 * @param sheet 
	 * @param xlsArraysDescription 
	 * @param xlsArraysList 
	 */
	public static void writeAllArraysToXls(
			Sheet sheet, 
			List<String> _xlsArraysDescription, 
			List<MyArray> xlsArraysList) {

		if (xlsArraysList.size() != 0) {

			int startingRow = sheet.getLastRowNum() + 1;
			int currentRow = startingRow;

			sheet.createRow(currentRow).createCell(0).setCellValue(" ");
			currentRow++;

			Cell cellTitle = sheet.createRow(currentRow).createCell(0);
			cellTitle.setCellValue("Arrays");
			cellTitle.setCellStyle(MyXLSUtils.styleTitlesFirstCol);
			currentRow++;

			Row descriptionRow = sheet.createRow(currentRow);
			currentRow++;

			descriptionRow.createCell(0).setCellValue("Description");

			for (int k = 0; k < xlsArraysList.size(); k++) {

				descriptionRow.createCell(k+1).setCellValue(_xlsArraysDescription.get(k).replace("_", " "));

				for (int i=0; i < xlsArraysList.get(k).size(); i++) {

					if (sheet.getRow(currentRow + i) != null) {
						sheet.getRow(currentRow + i).createCell(k+1).setCellValue(xlsArraysList.get(k).get(i));
					} else {
						sheet.createRow(currentRow + i).createCell(k+1).setCellValue(xlsArraysList.get(k).get(i));
					}
				}
			}
		}
	}
	
	/**
	 * @author Vittorio Trifari
	 * 
	 * @param sheet
	 * @param dataSet
	 * @param columnDescription must be of the same size of the dataSet
	 * @param showLegend
	 */
	public static void createXLSChart (Sheet sheet, List<MyArray> dataSet, List<String> columnDescription, boolean showLegend) {
		
		List<Double> xAxisMaxValueList = dataSet.stream()
				.filter(x -> dataSet.indexOf(x)%2 == 0)
				.map(x -> Arrays.stream(x.toArray()).max().getAsDouble())
				.collect(Collectors.toList());
		double xAxisMaxValue = xAxisMaxValueList.stream().mapToDouble(x -> x).max().getAsDouble();
		
		List<Double> xAxisMinValueList = dataSet.stream()
				.filter(x -> dataSet.indexOf(x)%2 == 0)
				.map(x -> Arrays.stream(x.toArray()).min().getAsDouble())
				.collect(Collectors.toList());
		double xAxisMinValue = xAxisMinValueList.stream().mapToDouble(x -> x).min().getAsDouble();
		
		List<Double> yAxisMaxValueList = dataSet.stream()
				.filter(y -> dataSet.indexOf(y)%2 != 0)
				.map(y -> Arrays.stream(y.toArray()).max().getAsDouble())
				.collect(Collectors.toList());
		double yAxisMaxValue = yAxisMaxValueList.stream().mapToDouble(y -> y).max().getAsDouble();
		
		List<Double> yAxisMinValueList = dataSet.stream()
				.filter(y -> dataSet.indexOf(y)%2 != 0)
				.map(y -> Arrays.stream(y.toArray()).min().getAsDouble())
				.collect(Collectors.toList());
		double yAxisMinValue = yAxisMinValueList.stream().mapToDouble(y -> y).min().getAsDouble();
		
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, dataSet.size()+3, 2, dataSet.size()+13, 12);

        Chart chart = drawing.createChart(anchor);
        
        chart.getManualLayout().setHeightRatio(0.95);
        chart.getManualLayout().setWidthRatio(0.90);
        chart.getManualLayout().setTarget(LayoutTarget.OUTER);
        
        ChartLegend legend = null;
        if(showLegend == true){
        	legend = chart.getOrCreateLegend();
        	legend.setPosition(LegendPosition.TOP_RIGHT);
        }

        ScatterChartData data = chart.getChartDataFactory().createScatterChartData();
        
        ValueAxis bottomAxisValues = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxisValues = chart.getChartAxisFactory().createValueAxis(AxisPosition.BOTTOM);
        
        leftAxisValues.setMajorTickMark(AxisTickMark.IN);
        leftAxisValues.setMinorTickMark(AxisTickMark.IN);
        leftAxisValues.setMaximum(Math.round(1.1*yAxisMaxValue));
        leftAxisValues.setMinimum(Math.round(0.9*yAxisMinValue));
        leftAxisValues.setCrosses(AxisCrosses.AUTO_ZERO);
        
        bottomAxisValues.setCrosses(AxisCrosses.AUTO_ZERO);
        bottomAxisValues.setMajorTickMark(AxisTickMark.IN);
        bottomAxisValues.setMinorTickMark(AxisTickMark.IN);
        bottomAxisValues.setMaximum(Math.round(1.1*xAxisMaxValue));
        bottomAxisValues.setMinimum(Math.round(0.9*xAxisMinValue));
        bottomAxisValues.setCrosses(AxisCrosses.AUTO_ZERO);
        
        for(int i=1; i<dataSet.size(); i+=2) {
        	
            ChartDataSource<Number> x = DataSources.fromNumericCellRange(
            		sheet,
            		new CellRangeAddress(
            				4,
            				3+dataSet.get(i-1).size(),
            				i,
            				i
            				)
            		);
            ChartDataSource<Number> y = DataSources.fromNumericCellRange(
            		sheet,
            		new CellRangeAddress(
            				4,
            				3+dataSet.get(i).size(), 
            				i+1,
            				i+1
            				)
            		);
            data.addSerie(x, y).setTitle(columnDescription.get(i));
        }

        chart.plot(data, bottomAxisValues, leftAxisValues);
		
	}
	
	public static String createNewFolder(String path) {
		File folder = new File(path);
		try{
			if(folder.mkdir() && !folder.exists()) return path;
			else return path;

		} catch(Exception e){
			e.printStackTrace();
			return "";
		}
	}

	public static void serializeObject(Object obj, String path, String fileNameNoExt) {

		String input = "";
		if (!fileNameNoExt.endsWith(".xml")) input = fileNameNoExt + ".xml";
		else input = fileNameNoExt;
		
		if (!path.endsWith(File.separator)) path = path + File.separator;

		File file = new File(path + input);
		if (file.exists()) {
			file.delete();
			System.out.println("Old serialization file deleted");
		}
		
		if (file.isDirectory()) {
			System.out.println("Input is not a file");
			return;
		}

		XStream xstream = new XStream(new DomDriver("utf-8"));

		try {
			Writer writer = new FileWriter(path + input);
			//			xstream.setMode(XStream.NO_REFERENCES);
			xstream.toXML(obj, writer );

		} catch (IOException e) {
			e.printStackTrace();
		}

		//		Using JAXB
		//		try {
		//
		//			File file = new File(MyStaticObjects.dataDirectory + fileNameNoExt + ".xml");
		//
		//			JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
		//			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		//
		//			// output pretty printed
		//			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		//			jaxbMarshaller.marshal(obj, file);
		//
		//		} catch (JAXBException e) {
		//			e.printStackTrace();
		//		}
	}

	public static void serializeObject(Object obj, String fileNameWithPath) {
		File file = new File(fileNameWithPath);
		serializeObject(obj, file.getAbsolutePath().replace(file.getName(), ""), file.getName());
	}
	
	/**
	 * 
	 * @param aircraftName
	 * @param _workbookExport
	 * @param str
	 * @param createSheet
	 * @return
	 */
	public static Sheet commonOperations(String aircraftName, Workbook _workbookExport, String str, boolean createSheet) {
		Sheet sheet = null;

		if (createSheet == true) {
			if (sheet == null || !sheet.getSheetName().equals(str.replace("_", " ")))
				sheet = MyXLSUtils.createNewSheet(
						WordUtils.capitalizeFully(str.replace("_", " ")), 
						aircraftName, _workbookExport); 
		} 
		return sheet;
	}

	public static String getCurrentTimeStamp() {
		String dateStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String timeDateStamp = dateStamp + "T" + timeStamp;
		return timeDateStamp;
	}

	/** 
	 * Evaluate mean value; the method return a list with mean values evaluated in different ways.
	 * 
	 * @author Lorenzo Attanasio
	 * @param referenceValue
	 * @param mapOfEstimatedValues
	 * @param percentDifference
	 * @param threshold below threshold value the estimate 
	 * 		  is considered OK for evaluating filtered mean
	 * @return
	 */
	public static JPADStaticWriteUtils.StoreResults<Double> compareMethods(
			Object refVal, 
			Object map,
			Double[] percentDifference, 
			Double threshold) {

		Map <MethodEnum, Amount<?>> mapOfEstimatedValues = new TreeMap<MethodEnum, Amount<?>>();
		Amount<?> referenceValue = Amount.valueOf(0., Unit.ONE);
		int i=0, k=0;
		Double sum = 0.;
		Double filteredSum = 0.;
		Double max = 0., min = Double.MAX_VALUE;

		if (map instanceof Map) {
			mapOfEstimatedValues = (Map<MethodEnum, Amount<?>>) map;
		}

		if(refVal instanceof Amount<?>) {
			referenceValue = Amount.valueOf(
					((Amount<?>) refVal).getEstimatedValue(), 
					((Amount<?>) refVal).getUnit());
		}

		Unit<?> unit = referenceValue.getUnit();

		// Get minimum and maximum values
		for (Entry<MethodEnum, Amount<?>> entry : mapOfEstimatedValues.entrySet())
		{
			if (entry.getValue() != null) {
				if (entry.getValue().getEstimatedValue() > max) {
					max = entry.getValue().getEstimatedValue();
				}
				if (entry.getValue().getEstimatedValue() < min) {
					min = entry.getValue().getEstimatedValue();
				}
			}
		}

		//		max = Collections.max(massMap.values()).getEstimatedValue();
		//		min = Collections.min(massMap.values()).getEstimatedValue();

		for (Entry<MethodEnum, Amount<?>> entry : mapOfEstimatedValues.entrySet())
		{
			if (referenceValue != null && entry.getValue() != null) {

				percentDifference[i] = 100*(entry.getValue().getEstimatedValue() -
						referenceValue.getEstimatedValue())/
						referenceValue.getEstimatedValue();

				sum = sum + entry.getValue().getEstimatedValue();

				if(Math.abs(percentDifference[i]) <= threshold) {
					filteredSum = filteredSum + entry.getValue().getEstimatedValue();
					k++;
				}


			} else if (referenceValue == null) {
				sum = sum + entry.getValue().getEstimatedValue();
				filteredSum = sum;

			} else if (entry.getValue() == null) {
				percentDifference[i] = null;
			}

			i++;

		}

		if(k==0) {k=1;}

		JPADStaticWriteUtils.StoreResults<Double> results = new JPADStaticWriteUtils.StoreResults<Double>();

		// Evaluate mean value
		results.setMean(sum/(double)i);

		// Evaluate filtered mean value
		results.setFilteredMean(filteredSum/(double)k);

		return results;

	}

	public static <E extends Enum<E>> void writeDatabaseNode(
			String filenameWithPathAndExt, Document doc, Element rootElement, 
			List<E> tagList, List<Amount> valueList, List<String> descriptionList,
			String valueName, String descriptionName) {

		if (tagList.size() == 0
				|| tagList.size() != valueList.size() 
				|| tagList.size() != descriptionList.size()) return;

		Element father;

		for (int i=0; i<tagList.size(); i++) {
			father = doc.createElement(tagList.get(i).name());
			rootElement.appendChild(father);
			writeSingleNode(valueName, valueList.get(i), father, doc);
			writeSingleNode(descriptionName, descriptionList.get(i), father, doc);
		}

		writeDocumentToXml(doc, filenameWithPathAndExt);
	}

	public static void saveAircraftToXML(
			Aircraft theAircraft, 
			String outputFolderPath, 
			String aircraftDirName,
			AircraftSaveDirectives aircraftSaveDirectives) {
		
		//=======================================================================
		// Create subfolder structure
		
		// main out folder
		String aircraftDirPath = 
				JPADStaticWriteUtils.createNewFolder(outputFolderPath + aircraftDirName);
		
		// subfolders names
		List<String> subfolders = new ArrayList<String>(
			    Arrays.asList(
			    		"cabin_configurations",
			    		"engines",
			    		"fuselages",
			    		"landing_gears",
			    		"lifting_surfaces", 
			    		"nacelles", 
			    		"systems"
			    		)
			    );
		// create subfolders (if non existent)
		subfolders.stream()
			.forEach(sf -> JPADStaticWriteUtils.createNewFolder(aircraftDirPath + File.separator 
						+ sf + File.separator) 
			);
		
		JPADStaticWriteUtils.createNewFolder(
				aircraftDirPath + File.separator 
				+ "lifting_surfaces" + File.separator 
				+ "airfoils" + File.separator);

		//=======================================================================
		// create the main aircraft.xml

		// tuple: doc, file-name, component-type
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;

		List<
		Tuple4<Document, String, String, ComponentEnum>
		> listDocNameType = new ArrayList<>();
		
		// populate the tuple
		try {
			docBuilder = docFactory.newDocumentBuilder();


			listDocNameType.add(
					Tuple.of(
							docBuilder.newDocument(),
							aircraftDirPath + File.separator,
							aircraftSaveDirectives.getAircraftFileName(), 
							ComponentEnum.AIRCRAFT
							)
					);
			if (theAircraft.getWing() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator,
								aircraftSaveDirectives.getWingFileName(), 
								ComponentEnum.WING
								)
						);
			if (theAircraft.getHTail() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator, 
								aircraftSaveDirectives.getHTailFileName(), 
								ComponentEnum.HORIZONTAL_TAIL
								)
						);
			if (theAircraft.getVTail() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator,
								aircraftSaveDirectives.getVTailFileName(), 
								ComponentEnum.VERTICAL_TAIL
								)
						);
			if (theAircraft.getCanard() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator, 
								aircraftSaveDirectives.getCanardFileName(), 
								ComponentEnum.CANARD
								)
						);
			if (theAircraft.getFuselage() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "fuselages" + File.separator, 
								aircraftSaveDirectives.getFuselageFileName(), 
								ComponentEnum.FUSELAGE
								)
						);
			if (theAircraft.getNacelles() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "nacelles" + File.separator,
								aircraftSaveDirectives.getNacelleFileName(), 
								ComponentEnum.NACELLE
								)
						);
			if (theAircraft.getPowerPlant() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "engines" + File.separator, 
								aircraftSaveDirectives.getEngineFileName(), 
								ComponentEnum.ENGINE
								)
						);
			if (theAircraft.getLandingGears() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "landing_gears" + File.separator, 
								aircraftSaveDirectives.getLandingGearFileName(), 
								ComponentEnum.LANDING_GEAR
								)
						);
			if (theAircraft.getSystems() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "systems" + File.separator, 
								aircraftSaveDirectives.getSystemFileName(), 
								ComponentEnum.SYSTEMS
								)
						);
			if (theAircraft.getCabinConfiguration() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "cabin_configurations" + File.separator, 
								aircraftSaveDirectives.getCabinConfigurationFileName(), 
								ComponentEnum.CABIN_CONFIGURATION
								)
						);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		// TODO: manage airfoils
		
		// write the aircraft according to the above directives
		writeToXML(theAircraft, listDocNameType, aircraftSaveDirectives);
		
		
	}

	/*******************************************************************************************
	 * This method is in charge of writing all input data collected inside the object of the 
	 * OutputTree class on a XML file.
	 * 
	 * @author Vittorio Trifari
	 * @param aircraftSaveDirectives 
	 * 
	 * @param output object of the OutputTree class which holds all output data
	 */
	public static void writeToXML(
			Aircraft aircraft, List<Tuple4<Document, String, String, ComponentEnum>> listDocNameType, AircraftSaveDirectives aircraftSaveDirectives ) {
		
		// populate all the docs
		listDocNameType.stream()
			.forEach(tpl -> makeXmlTree(aircraft, tpl, aircraftSaveDirectives)
				);
		
		// write all the docs
		listDocNameType.stream()
			.forEach(tpl -> 
				JPADStaticWriteUtils.writeDocumentToXml(
						tpl._1(), // doc
						tpl._2()+tpl._3()) // file path
					);

	}
	
	/*******************************************************************************************
	 * This method defines the XML tree structure and fill it with results form the OutputTree
	 * object
	 * 
	 * @author Vittorio Trifari
	 * @param aircraftSaveDirectives 
	 */
	private static void makeXmlTree(Aircraft aircraft, Tuple4<Document, String, String, ComponentEnum> docNameType, AircraftSaveDirectives aircraftSaveDirectives) {
		switch (docNameType._4()) {
		case AIRCRAFT:
			makeXmlTreeAircraft(aircraft, docNameType._1(), aircraftSaveDirectives);
			break;
		case CABIN_CONFIGURATION:
			if (aircraft.getCabinConfiguration() != null)
				makeXmlTreeCabinConfiguration(aircraft, docNameType._1());
			break;
		case WING:
			if (aircraft.getWing() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case HORIZONTAL_TAIL:
			if (aircraft.getHTail() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case VERTICAL_TAIL:
			if (aircraft.getVTail() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case CANARD:
			if (aircraft.getCanard() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case FUSELAGE:
			// TODO
			break;
		case NACELLE:
			// TODO
			break;
		case ENGINE:
			// TODO
			break;
		case LANDING_GEAR:
			// TODO
			break;
		case SYSTEMS:
			// TODO
			break;
		default:
			break;
		}
	}

	private static void makeXmlTreeLiftingSurface(
			Aircraft aircraft, Document doc, AircraftSaveDirectives aircraftSaveDirectives, ComponentEnum type) {

		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);

		String liftingSurfaceTag = "";
		org.w3c.dom.Element liftingSurfaceElement = null;
		org.w3c.dom.Element globalDataElement = null;
		
		switch (type) {
		case WING:
			liftingSurfaceTag = "wing";
			// make wing
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "wing" 
					Tuple.of("id", aircraft.getWing().getId()),
					Tuple.of("mirrored", aircraft.getWing().getLiftingSurfaceCreator().isMirrored().toString()),
					Tuple.of("equivalent", aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWingFlag().toString())
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make wing/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - main_spar_non_dimensional_position
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "main_spar_non_dimensional_position",
						aircraft.getWing().getLiftingSurfaceCreator().getMainSparNonDimensionalPosition(), 
						4, 6, // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
						Tuple.of("type", "PERCENT_CHORD"),
						Tuple.of("ref_to", "LOCAL_CHORD")
				)
			);
			// global_data - secondary_spar_non_dimensional_position
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "secondary_spar_non_dimensional_position",
						aircraft.getWing().getLiftingSurfaceCreator().getSecondarySparNonDimensionalPosition(),
						4, 6, // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
						Tuple.of("type", "PERCENT_CHORD"),
						Tuple.of("ref_to", "LOCAL_CHORD")
				)
			);
			// global_data - composite_correction_factor
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "composite_correction_factor",
						aircraft.getWing().getLiftingSurfaceCreator().getCompositeCorrectionFactor(),
						4, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getWing().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			// global_data - winglet_height
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "winglet_height",
						aircraft.getWing().getLiftingSurfaceCreator().getWingletHeight(),
						4, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			if (aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWingFlag()) 
				liftingSurfaceElement.appendChild(
					createEquivalentWingElement(doc, 
							aircraft.getWing().getSurface(), 
							aircraft.getWing().getSpan(), 
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getNonDimensionalSpanStationKink(),
							aircraft.getWing().getLiftingSurfaceCreator().getTaperRatio(),
							aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getTwistGeometricAtTip().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(1).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE),
							Arrays.asList(new String[]{
									aircraft.getWing().getAirfoilList().get(0).getAirfoilCreator().getName()+".xml",
									aircraft.getWing().getAirfoilList().get(1).getAirfoilCreator().getName()+".xml",
									aircraft.getWing().getAirfoilList().get(2).getAirfoilCreator().getName()+".xml"
									})
							)
					// TODO: manage with AircraftSaveDirectives
				);
			// else write panels ???
			
			break;
			
		case HORIZONTAL_TAIL:
			liftingSurfaceTag = "horizontal_tail";
			// make horizontal_tail
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "horizontal_tail" 
					Tuple.of("id", aircraft.getHTail().getId()),
					Tuple.of("mirrored", aircraft.getHTail().getLiftingSurfaceCreator().isMirrored().toString())
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make horizontal_tail/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - composite_correction_factor
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "composite_correction_factor",
						aircraft.getHTail().getLiftingSurfaceCreator().getCompositeCorrectionFactor(),
						4, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getHTail().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			
			break;
		case VERTICAL_TAIL:
			liftingSurfaceTag = "vertical_tail";
			// make vertical_tail
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "vertical_tail" 
					Tuple.of("id", aircraft.getVTail().getId()),
					Tuple.of("mirrored", aircraft.getVTail().getLiftingSurfaceCreator().isMirrored().toString())
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make vertical_tail/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - composite_correction_factor
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "composite_correction_factor",
						aircraft.getVTail().getLiftingSurfaceCreator().getCompositeCorrectionFactor(),
						4, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getVTail().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			
			break;
		case CANARD:
			liftingSurfaceTag = "canard";
			// make canard
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "canard" 
					Tuple.of("id", aircraft.getCanard().getId()),
					Tuple.of("mirrored", aircraft.getCanard().getLiftingSurfaceCreator().isMirrored().toString())
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make vertical_tail/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - composite_correction_factor
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "composite_correction_factor",
						aircraft.getCanard().getLiftingSurfaceCreator().getCompositeCorrectionFactor(),
						4, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getCanard().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			
			break;

		default:
			break;
		}
		
		
	}

	private static void makeXmlTreeAircraft(Aircraft aircraft, Document doc, AircraftSaveDirectives aircraftSaveDirectives) {
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);
		
		// aircraft
		org.w3c.dom.Element aircraftElement = createXMLElementWithAttributes(doc, "aircraft", 
				Tuple.of("id", aircraft.getId()),
				Tuple.of("type", aircraft.getTypeVehicle().toString()),
				Tuple.of("regulations", aircraft.getRegulations().toString())
		);
		rootElement.appendChild(aircraftElement);
		
		// global_data
		org.w3c.dom.Element globalDataElement = doc.createElement("global_data");
		aircraftElement.appendChild(globalDataElement);
		// global_data - cabin_configuration
		globalDataElement.appendChild(
			createXMLElementWithAttributes(doc, "cabin_configuration", 
					Tuple.of("file", aircraftSaveDirectives.getCabinConfigurationFileName())
			)
		);
		
		// lifting_surfaceS
		org.w3c.dom.Element liftingSurfacesElement = doc.createElement("lifting_surfaces");
		
		// lifting_surface		
		List<LiftingSurface> liftingSurfacesList = aircraft.getComponentsList().stream()
			.filter(comp -> comp.getClass() == LiftingSurface.class)
				.map(comp -> (LiftingSurface) comp)
					.collect(Collectors.toList());
		
		List<String> liftingSurfacesFileNames = new ArrayList<>();
		liftingSurfacesList.stream()
			.forEach(ls -> {
				if(ls.getType().equals(ComponentEnum.WING))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getWingFileName());
				else if(ls.getType().equals(ComponentEnum.HORIZONTAL_TAIL))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getHTailFileName());
				else if(ls.getType().equals(ComponentEnum.VERTICAL_TAIL))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getVTailFileName());
				else if(ls.getType().equals(ComponentEnum.CANARD))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getCanardFileName());
			});
		
		liftingSurfacesList.stream()
			.forEach(ls -> 
				liftingSurfacesElement.appendChild(
						createLiftingSurfaceElement(doc, 
								ls.getType(),  
								liftingSurfacesFileNames.get(liftingSurfacesList.indexOf(ls)), 
								ls.getXApexConstructionAxes(),
								ls.getYApexConstructionAxes(),
								ls.getZApexConstructionAxes(),
								ls.getRiggingAngle())
						)
					);

		// append all kinds of lifting surfaces
		aircraftElement.appendChild(liftingSurfacesElement);
		
		// fuselageS
		org.w3c.dom.Element fuselagesElement = doc.createElement("fuselages");
		
		// fuselage
		aircraft.getComponentsList().stream()
			.filter(comp -> comp.getClass() == Fuselage.class)
				.map(comp -> (Fuselage) comp)
					.forEach(fus ->
					fuselagesElement.appendChild(
							createFuselageElement(doc, 
									aircraftSaveDirectives.getFuselageFileName(), 
									fus.getXApexConstructionAxes(),
									fus.getYApexConstructionAxes(),
									fus.getZApexConstructionAxes())
							)
						);
		
		
		// append all kinds of fuselages
		aircraftElement.appendChild(fuselagesElement);
		
		// power plant
		org.w3c.dom.Element powerPlantElement = doc.createElement("power_plant");
		
		// engine
		aircraft.getPowerPlant().getEngineList().stream()
		.forEach(e -> {
			powerPlantElement.appendChild(
					createEngineElement(doc, 
							aircraftSaveDirectives.getEngineFileName(), 
							aircraft.getPowerPlant().getEngineList().get(aircraft.getPowerPlant().getEngineList().indexOf(e)).getXApexConstructionAxes(),
							aircraft.getPowerPlant().getEngineList().get(aircraft.getPowerPlant().getEngineList().indexOf(e)).getYApexConstructionAxes(),
							aircraft.getPowerPlant().getEngineList().get(aircraft.getPowerPlant().getEngineList().indexOf(e)).getZApexConstructionAxes(),
							aircraft.getPowerPlant().getEngineList().get(aircraft.getPowerPlant().getEngineList().indexOf(e)).getTiltingAngle(),
							aircraft.getPowerPlant().getEngineList().get(aircraft.getPowerPlant().getEngineList().indexOf(e)).getMountingPosition()
							)
					);
		});
		
		// append all kinds of engines
		aircraftElement.appendChild(powerPlantElement);
		
		// nacelleS
		org.w3c.dom.Element nacellesElement = doc.createElement("nacelles");
		
		// engine
		aircraft.getNacelles().getNacellesList().stream()
		.forEach(e -> {
			nacellesElement.appendChild(
					createNacelleElement(doc, 
							aircraftSaveDirectives.getEngineFileName(), 
							aircraft.getNacelles().getNacellesList().get(aircraft.getNacelles().getNacellesList().indexOf(e)).getXApexConstructionAxes(),
							aircraft.getNacelles().getNacellesList().get(aircraft.getNacelles().getNacellesList().indexOf(e)).getYApexConstructionAxes(),
							aircraft.getNacelles().getNacellesList().get(aircraft.getNacelles().getNacellesList().indexOf(e)).getZApexConstructionAxes(),
							aircraft.getNacelles().getNacellesList().get(aircraft.getNacelles().getNacellesList().indexOf(e)).getMountingPosition()
							)
					);
		});
		
		// append all kinds of nacelles
		aircraftElement.appendChild(nacellesElement);
		
		// landing gearS
		org.w3c.dom.Element landingGearsElement = doc.createElement("landing_gears");
		
		aircraft.getComponentsList().stream()
		.filter(comp -> comp.getClass() == LandingGears.class)
			.map(comp -> (LandingGears) comp)
				.forEach(lg ->
				landingGearsElement.appendChild(
						createLandingGearElement(doc, 
								aircraftSaveDirectives.getLandingGearFileName(), 
								lg.getXApexConstructionAxes(),
								lg.getYApexConstructionAxes(),
								lg.getZApexConstructionAxes(),
								lg.getMountingPosition()
								)
						)
					);
	
		// append all kinds of landing gears
		aircraftElement.appendChild(landingGearsElement);
		
		// systemS
		org.w3c.dom.Element systemsElement = doc.createElement("systems");
		
		aircraft.getComponentsList().stream()
		.filter(comp -> comp.getClass() == Systems.class)
			.map(comp -> (Systems) comp)
				.forEach(sys ->
				systemsElement.appendChild(
						createSystemElement(doc, 
								aircraftSaveDirectives.getSystemFileName(), 
								sys.getXApexConstructionAxes(),
								sys.getYApexConstructionAxes(),
								sys.getZApexConstructionAxes()
								)
						)
					);
		
		// append all kinds of systems
		aircraftElement.appendChild(systemsElement);
		
		
	}
	
	private static void makeXmlTreeCabinConfiguration(Aircraft aircraft, Document doc) {
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);
		
		// configuration
		org.w3c.dom.Element cabinConfigurationElement = createXMLElementWithAttributes(doc, "configuration", 
				Tuple.of("id", aircraft.getCabinConfiguration().getId())
				);
		rootElement.appendChild(cabinConfigurationElement);
		
		// global_data
		org.w3c.dom.Element globalDataElement = doc.createElement("global_data");
		cabinConfigurationElement.appendChild(globalDataElement);

		// global_data - actual_passengers_number
		JPADStaticWriteUtils.writeSingleNode("actual_passengers_number", 
				aircraft.getCabinConfiguration().getNPax(), 
				globalDataElement, doc);
		
		// global_data - maximum_passengers_number
		JPADStaticWriteUtils.writeSingleNode("maximum_passengers_number", 
				aircraft.getCabinConfiguration().getMaxPax(), 
				globalDataElement, doc);
		
		// global_data - flight_crew_number
		JPADStaticWriteUtils.writeSingleNode("flight_crew_number", 
				aircraft.getCabinConfiguration().getCabinCrewNumber(), 
				globalDataElement, doc);
		
		// global_data - classes_number
		JPADStaticWriteUtils.writeSingleNode("classes_number", 
				aircraft.getCabinConfiguration().getClassesNumber(), 
				globalDataElement, doc);
		
		// global_data - classes_type
		JPADStaticWriteUtils.writeSingleNode("classes_type", 
				aircraft.getCabinConfiguration().getTypeList(),
				globalDataElement, doc);
		
		// global_data - aisles_number
		JPADStaticWriteUtils.writeSingleNode("aisles_number", 
				aircraft.getCabinConfiguration().getAislesNumber(),
				globalDataElement, doc);
		
		// global_data -  x_coordinates_first_row
		JPADStaticWriteUtils.writeSingleNode("x_coordinates_first_row", 
				aircraft.getCabinConfiguration().getXCoordinateFirstRow(),
				globalDataElement, doc);
		
		// global_data - seat_block_position 
		JPADStaticWriteUtils.writeSingleNode("seat_block_position", 
				aircraft.getCabinConfiguration().getPosition(),
				globalDataElement, doc);
		
		// global_data - missing_seat_row
		org.w3c.dom.Element missingSeatRowElement = doc.createElement("missing_seat_row");
		globalDataElement.appendChild(missingSeatRowElement);
		aircraft.getCabinConfiguration().getMissingSeatsRow().stream()
			.forEach( t -> 
			JPADStaticWriteUtils.writeSingleNode("value", 
					Arrays.asList(
							aircraft.getCabinConfiguration().getMissingSeatsRow().get(
									aircraft.getCabinConfiguration().getMissingSeatsRow().indexOf(t)
									)
							),
					missingSeatRowElement, doc)
					);
		
		// detailed_data
		org.w3c.dom.Element detailedDataElement = doc.createElement("detailed_data");
		cabinConfigurationElement.appendChild(detailedDataElement);
		
		// detailed_data - number_of_breaks_economy_class
		JPADStaticWriteUtils.writeSingleNode("number_of_breaks_economy_class", 
				aircraft.getCabinConfiguration().getNumberOfBreaksEconomyClass(), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_breaks_business_class
		JPADStaticWriteUtils.writeSingleNode("number_of_breaks_business_class", 
				aircraft.getCabinConfiguration().getNumberOfBreaksBusinessClass(), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_breaks_first_class
		JPADStaticWriteUtils.writeSingleNode("number_of_breaks_first_class", 
				aircraft.getCabinConfiguration().getNumberOfBreaksFirstClass(), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_rows_economy_class
		JPADStaticWriteUtils.writeSingleNode("number_of_rows_economy_class", 
				aircraft.getCabinConfiguration().getNumberOfRowsEconomyClass(), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_rows_business_class
		JPADStaticWriteUtils.writeSingleNode("number_of_rows_business_class", 
				aircraft.getCabinConfiguration().getNumberOfRowsBusinessClass(), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_rows_first_class
		JPADStaticWriteUtils.writeSingleNode("number_of_rows_first_class", 
				aircraft.getCabinConfiguration().getNumberOfRowsFirstClass(), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_columns_economy_class
		JPADStaticWriteUtils.writeSingleNode("number_of_columns_economy_class", 
				Arrays.asList(aircraft.getCabinConfiguration().getNumberOfColumnsEconomyClass()), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_columns_business_class
		JPADStaticWriteUtils.writeSingleNode("number_of_columns_business_class", 
				Arrays.asList(aircraft.getCabinConfiguration().getNumberOfColumnsBusinessClass()), 
				detailedDataElement, doc);
		
		// detailed_data - number_of_columns_first_class
		JPADStaticWriteUtils.writeSingleNode("number_of_columns_first_class", 
				Arrays.asList(aircraft.getCabinConfiguration().getNumberOfColumnsFirstClass()), 
				detailedDataElement, doc);
		
		// detailed_data - pitch_economy_class
		JPADStaticWriteUtils.writeSingleNode("pitch_economy_class", 
				aircraft.getCabinConfiguration().getPitchEconomyClass(), 
				detailedDataElement, doc);
		
		// detailed_data - pitch_business_class
		JPADStaticWriteUtils.writeSingleNode("pitch_business_class", 
				aircraft.getCabinConfiguration().getPitchBusinessClass(), 
				detailedDataElement, doc);
		
		// detailed_data - pitch_first_class
		JPADStaticWriteUtils.writeSingleNode("pitch_first_class", 
				aircraft.getCabinConfiguration().getPitchFirstClass(), 
				detailedDataElement, doc);
		
		// detailed_data - width_economy_class
		JPADStaticWriteUtils.writeSingleNode("width_economy_class", 
				aircraft.getCabinConfiguration().getWidthEconomyClass(), 
				detailedDataElement, doc);
		
		// detailed_data - width_business_class
		JPADStaticWriteUtils.writeSingleNode("width_business_class", 
				aircraft.getCabinConfiguration().getWidthBusinessClass(), 
				detailedDataElement, doc);
		
		// detailed_data - width_first_class
		JPADStaticWriteUtils.writeSingleNode("width_first_class", 
				aircraft.getCabinConfiguration().getWidthFirstClass(), 
				detailedDataElement, doc);
		
		// detailed_data - distance_from_wall_economy_class
		JPADStaticWriteUtils.writeSingleNode("distance_from_wall_economy_class", 
				aircraft.getCabinConfiguration().getDistanceFromWallEconomyClass(), 
				detailedDataElement, doc);
		
		// detailed_data - distance_from_wall_business_class
		JPADStaticWriteUtils.writeSingleNode("distance_from_wall_business_class", 
				aircraft.getCabinConfiguration().getDistanceFromWallBusinessClass(), 
				detailedDataElement, doc);
		
		// detailed_data - distance_from_wall_first_class
		JPADStaticWriteUtils.writeSingleNode("distance_from_wall_first_class", 
				aircraft.getCabinConfiguration().getDistanceFromWallFirstClass(), 
				detailedDataElement, doc);
		
		// reference_masses
		org.w3c.dom.Element referenceMassElement = doc.createElement("reference_masses");
		cabinConfigurationElement.appendChild(referenceMassElement);
		
		// reference_masses - mass_furnishings_and_equipment
		JPADStaticWriteUtils.writeSingleNode("mass_furnishings_and_equipment", 
				aircraft.getCabinConfiguration().getMassFurnishingsAndEquipmentReference(), 
				referenceMassElement, doc);
		
	} 
	
	private static void makeXmlTreeFuselage(Aircraft aircraft, Document doc) {
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);
		
		// configuration
		org.w3c.dom.Element fuselageElement = createXMLElementWithAttributes(doc, "fuselage", 
				Tuple.of("id", aircraft.getFuselage().getId()),
				Tuple.of("pressurized", aircraft.getFuselage().getFuselageCreator().getPressurized().toString())
				);
		rootElement.appendChild(fuselageElement);
		
		// global_data
		org.w3c.dom.Element globalDataElement = doc.createElement("global_data");
		fuselageElement.appendChild(globalDataElement);

		// global_data - deck_number
		JPADStaticWriteUtils.writeSingleNode("deck_number", 
				aircraft.getFuselage().getFuselageCreator().getDeckNumber(), 
				globalDataElement, doc);
		
		// global_data - length
		JPADStaticWriteUtils.writeSingleNode("length", 
				aircraft.getFuselage().getFuselageCreator().getLenF(), 
				globalDataElement, doc);
		
		// global_data - length
		JPADStaticWriteUtils.writeSingleNode("roughness", 
				aircraft.getFuselage().getFuselageCreator().getRoughness(), 
				globalDataElement, doc);

		// global_data
		org.w3c.dom.Element noseTrunkElement = doc.createElement("nose_trunk");
		fuselageElement.appendChild(noseTrunkElement);
		
		// global_data - length
		JPADStaticWriteUtils.writeSingleNode("length_ratio", 
				aircraft.getFuselage().getFuselageCreator().getLenRatioNF(), 
				globalDataElement, doc);

	} 
	
	@SafeVarargs
	public static org.w3c.dom.Element createXMLElementWithAttributes(Document doc, String elementName, 
			Tuple2<String,String>... attributeValueTuples) {
		org.w3c.dom.Element element = doc.createElement(elementName);
		Arrays.stream(attributeValueTuples)
			.forEach( tpl -> {
				org.w3c.dom.Attr a = doc.createAttribute(tpl._1());
				a.setValue(tpl._2());
				element.setAttributeNode(a);
			});
		return element;
	}

	@SafeVarargs
	public static org.w3c.dom.Element createXMLElementWithValueAndAttributes(Document doc, String elementName, Object valueToWrite,
			int above, int below,
			Tuple2<String,String>... attributeValueTuples) {
		org.w3c.dom.Element element = doc.createElement(elementName);
		Arrays.stream(attributeValueTuples)
			.forEach( tpl -> {
				org.w3c.dom.Attr a = doc.createAttribute(tpl._1());
				a.setValue(tpl._2());
				element.setAttributeNode(a);
			});
		
		String[] str = new String[2];
		str = prepareSingleVariableToWrite(valueToWrite, above, below);
		
		String value = str[0];
		String unit = str[1];

		if (unit.length() != 0)
			element.setAttribute("unit", unit);

		element.appendChild(doc.createTextNode(value));

		return element;
	}
	
	public static org.w3c.dom.Element createLiftingSurfaceElement(Document doc, 
			ComponentEnum componentEnum, 
			String fileName, 
			Amount<Length> x, Amount<Length> y, Amount<Length> z,
			Amount<Angle> riggingAngle) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				Stream.of(ComponentEnum.values())
					.filter( ce -> ce.equals(componentEnum))
					.findFirst().get().toString().toLowerCase(),
				Tuple.of("file", fileName)	
				);
		org.w3c.dom.Element pos = doc.createElement("position");
		element.appendChild(pos);
		JPADStaticWriteUtils.writeSingleNode("x", x, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("y", y, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("z", z, pos, doc);
		
		JPADStaticWriteUtils.writeSingleNode("rigging_angle", riggingAngle, element, doc);

		return element;
	}
	
	public static org.w3c.dom.Element createFuselageElement(Document doc, 
			String fileName, 
			Amount<Length> x, Amount<Length> y, Amount<Length> z) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"fuselage",
				Tuple.of("file", fileName)	
				);
		org.w3c.dom.Element pos = doc.createElement("position");
		element.appendChild(pos);
		JPADStaticWriteUtils.writeSingleNode("x", x, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("y", y, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("z", z, pos, doc);

		return element;
	}

	public static org.w3c.dom.Element createEngineElement(Document doc, 
			String fileName, 
			Amount<Length> x, Amount<Length> y, Amount<Length> z,
			Amount<Angle> tiltAngle,
			EngineMountingPositionEnum mountingPosition) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"engine",
				Tuple.of("file", fileName)	
				);
		org.w3c.dom.Element pos = doc.createElement("position");
		element.appendChild(pos);
		JPADStaticWriteUtils.writeSingleNode("x", x, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("y", y, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("z", z, pos, doc);

		JPADStaticWriteUtils.writeSingleNode("tilting_angle", tiltAngle, element, doc);
		JPADStaticWriteUtils.writeSingleNode("mounting_point", mountingPosition, element, doc);
		
		return element;
	}
	
	public static org.w3c.dom.Element createNacelleElement(Document doc, 
			String fileName, 
			Amount<Length> x, Amount<Length> y, Amount<Length> z,
			MountingPosition mountingPosition) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"nacelle",
				Tuple.of("file", fileName)	
				);
		org.w3c.dom.Element pos = doc.createElement("position");
		element.appendChild(pos);
		JPADStaticWriteUtils.writeSingleNode("x", x, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("y", y, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("z", z, pos, doc);

		JPADStaticWriteUtils.writeSingleNode("mounting_point", mountingPosition, element, doc);
		
		return element;
	}
	
	public static org.w3c.dom.Element createLandingGearElement(Document doc, 
			String fileName, 
			Amount<Length> x, Amount<Length> y, Amount<Length> z,
			aircraft.components.LandingGears.MountingPosition mountingPosition) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"nacelle",
				Tuple.of("file", fileName)	
				);
		org.w3c.dom.Element pos = doc.createElement("position");
		element.appendChild(pos);
		JPADStaticWriteUtils.writeSingleNode("x", x, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("y", y, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("z", z, pos, doc);

		JPADStaticWriteUtils.writeSingleNode("mounting_point", mountingPosition, element, doc);
		
		return element;
	}
	
	public static org.w3c.dom.Element createSystemElement(Document doc, 
			String fileName, 
			Amount<Length> x, Amount<Length> y, Amount<Length> z) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"nacelle",
				Tuple.of("file", fileName)	
				);
		org.w3c.dom.Element pos = doc.createElement("position");
		element.appendChild(pos);
		JPADStaticWriteUtils.writeSingleNode("x", x, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("y", y, pos, doc);
		JPADStaticWriteUtils.writeSingleNode("z", z, pos, doc);

		return element;
	}

	public static org.w3c.dom.Element createEquivalentWingElement(Document doc, 
			Amount<Area> surface, 
			Amount<Length> span, 
			Double taperRatio,
			Amount<Angle> sweepLeadingEdge,
			Amount<Angle> twistAtTip,
			Amount<Angle> dihedral,
			Double nonDimensionalSpanStationKink,
			Double taperRatioRealWing,
			Amount<Angle> twistAtKinkRealWing,
			Amount<Angle> sweepLeadingEdgeInnerPanel,
			Amount<Angle> sweepLeadingEdgeOuterPanel,
			List<String> airfoilFileNames
			) {
		
		if (3 != airfoilFileNames.size()) {
			System.err.println("createEquivalentWingElement :: airfoilFileNames list must contain 3 file names!");
			return null;
		} 
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"equivalent_wing"	
				);
		JPADStaticWriteUtils.writeSingleNode("surface", surface, element, doc);
		JPADStaticWriteUtils.writeSingleNode("span", span, element, doc);
		JPADStaticWriteUtils.writeSingleNode("taper_ratio", taperRatio, element, doc);
		JPADStaticWriteUtils.writeSingleNode("sweep_leading_edge", sweepLeadingEdge, element, doc);
		JPADStaticWriteUtils.writeSingleNode("twist_at_tip", twistAtTip, element, doc);
		JPADStaticWriteUtils.writeSingleNode("dihedral", dihedral, element, doc);
		JPADStaticWriteUtils.writeSingleNode("non_dimensional_span_station_kink", nonDimensionalSpanStationKink, element, doc);
		JPADStaticWriteUtils.writeSingleNode("taper_ratio_real_wing", taperRatioRealWing, element, doc);
		JPADStaticWriteUtils.writeSingleNode("twist_at_kink_real_wing", twistAtKinkRealWing, element, doc);
		JPADStaticWriteUtils.writeSingleNode("sweep_leading_edge_inner_panel", sweepLeadingEdgeInnerPanel, element, doc);
		JPADStaticWriteUtils.writeSingleNode("sweep_leading_edge_outer_panel", sweepLeadingEdgeOuterPanel, element, doc);

		org.w3c.dom.Element airfoils = createXMLElementWithAttributes(
				doc,
				"airfoils"	
				);
		element.appendChild(airfoils);
		airfoils.appendChild(
			JPADStaticWriteUtils.createXMLElementWithAttributes(doc, 
					"airfoil_root", Tuple.of("file", airfoilFileNames.get(0)))
				); 
		airfoils.appendChild(
			JPADStaticWriteUtils.createXMLElementWithAttributes(doc, 
					"airfoil_kink", Tuple.of("file", airfoilFileNames.get(1))) 
				); 
		airfoils.appendChild(
			JPADStaticWriteUtils.createXMLElementWithAttributes(doc, 
					"airfoil_tip", Tuple.of("file", airfoilFileNames.get(2))) 
				); 
		
		return element;
	}
	
}

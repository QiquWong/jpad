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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Quantity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.azeckoski.reflectutils.ReflectUtils;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import configuration.MyConfiguration;
import configuration.enumerations.MethodEnum;
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
					System.out.println("\tDelating the old .csv file ...\n");
					Files.delete(outputFile.toPath());
				} 
				catch (IOException e) {
					System.err.println(e + " (Unable to delete file)");
				}
			}
			
			try{
				
				System.out.println("\tCreating " + fileName.get(i) + ".csv file ... \n");
				
				PrintWriter writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");
				writer.println(fileName.get(i));
				writer.println(xListName.get(i) + " " + yListName.get(i));

				if(xList.get(i).length != yList.get(i).length) {
					System.err.println("CORRESPONDING ELEMENTS OF THE TWO LISTS MUST HAVE THE SAME LENGTH");
					return;
				}

				for (int j = 0; j < xList.size(); j++) {
					writer.println(
							String.format(
									Locale.ROOT,
									"%1$11.6f %2$11.6f",
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
	public static String[] prepareSingleVariableToWrite(Object valueToWrite) {

		String value = "", unit = "";
		double roundingThreshold = 1e-2;
		int roundingAboveThreshold = 4;
		int roundingBelowThreshold = 6;

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
		else
			element.setAttribute("unit", "");

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

	//		System.out.println("-----------" + MyReadUtils.getElementXpath(father) + "/" + description);

	//		serializeObject(valueToWrite, "test");
	//		System.out.println(xstream.toXML(_objectToWrite));

	//		try {
	//			JAXBContext context = JAXBContext.newInstance(_objectToWrite.getClass());
	//			Marshaller marshaller;
	//			marshaller = context.createMarshaller();
	//	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	//	        marshaller.marshal(_objectToWrite, System.out);
	//		} catch (JAXBException e) {
	//			e.printStackTrace();
	//		}


}

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
import java.util.Date;
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
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.AxisTickMark;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LayoutTarget;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.ScatterChartData;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.azeckoski.reflectutils.ReflectUtils;
import org.jscience.mathematics.vector.DimensionException;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.Multimap;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import aircraft.Aircraft;
import aircraft.components.LandingGears;
import aircraft.components.Systems;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.liftingSurface.creator.AsymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
import javaslang.Tuple;
import javaslang.Tuple2;
import javaslang.Tuple4;
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

	/**
	 * 
	 * @author Agostino De Marco
	 *
	 * @param doc
	 * @param file
	 */
	public static void writeDocumentToXml(Document doc, File file) {
		try {
			//System.out.println(""+doc);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);
			System.out.println("Data successfully written to " + file.getAbsolutePath());

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
			throw new DimensionException("X AND Y LISTS MUST HAVE THE SAME SIZE !!");
		}
		
		for (int i = 0; i < xList.size(); i++) {
		
			File outputFile = new File(outFileFolderPath + File.separator + fileName.get(i) + ".csv");
			
			if (outputFile.exists()) {
				try {
//					System.out.println("\n\tDeleting the old .csv file ...");
					Files.delete(outputFile.toPath());
				} 
				catch (IOException e) {
					System.err.println(e + " (Unable to delete file)");
				}
			}
			
			try{
				
//				System.out.println("\n\tCreating " + fileName.get(i) + ".csv file ... ");
				
				PrintWriter writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");
				writer.println(xListName.get(i) + ", " + yListName.get(i));

				if(xList.get(i).length != yList.get(i).length) {
					throw new DimensionException("CORRESPONDING ELEMENTS OF THE TWO LISTS MUST HAVE THE SAME LENGTH");
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

	public static void exportToCSV (
			List<List<Double[]>> valueList, 
			List<String> fileName,
			List<List<String>> labelsName,
			String outFileFolderPath 
			) {
		
		for(int i=0; i<valueList.size(); i++)
			for(int j=i+1; j<valueList.size(); j++)
				if(valueList.get(i).size() != valueList.get(j).size()) 
					throw new DimensionException("SOME LISTS DO NOT HAVE THE SAME SIZE !!");
				
		
		for (int i = 0; i < valueList.size(); i++) {
		
			File outputFile = new File(outFileFolderPath + File.separator + fileName.get(i) + ".csv");
			
			if (outputFile.exists()) {
				try {
					Files.delete(outputFile.toPath());
				} 
				catch (IOException e) {
					System.err.println(e + " (Unable to delete file)");
				}
			}
			
			try{

				PrintWriter writer = new PrintWriter(outputFile.getAbsolutePath(), "UTF-8");
				for(int j=0; j<valueList.get(i).size(); j++) {
					writer.print(labelsName.get(i).get(j) + ", ");
				}
				writer.println("");

				for(int j=0; j<valueList.get(i).size(); j++)
					for(int k=j+1; k<valueList.get(i).size(); k++)
						if(valueList.get(i).get(j).length != valueList.get(i).get(k).length) 
							throw new DimensionException("CORRESPONDING ELEMENTS OF THE TWO LISTS MUST HAVE THE SAME LENGTH");
				
				double[][] valuesArrays = new double[valueList.get(i).size()][];
				for(int j=0; j<valueList.get(i).size(); j++) {
					valuesArrays[j] = MyArrayUtils.convertToDoublePrimitive(valueList.get(i).get(j));
				}
				
				RealMatrix valuesMatrix = MatrixUtils.createRealMatrix(valuesArrays);
				RealMatrix transposedValuesMatrix = valuesMatrix.transpose();

				for(int j=0; j<transposedValuesMatrix.getRowDimension(); j++) {
					for(int k=0; k<transposedValuesMatrix.getColumnDimension(); k++) {
						writer.print(
								String.format(
										Locale.ROOT,
										"%.12f,	",
										transposedValuesMatrix.getData()[j][k]
										)
								);
					}
					writer.println();
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

		if(valueToWrite == null)
			valueToWrite = "";
		
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
			double[] percentDifference, 
			double threshold) {

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
				percentDifference[i] = 0.0;
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
				JPADStaticWriteUtils.createNewFolder(outputFolderPath);
		
		// subfolders names
		List<String> subfolders = new ArrayList<String>(
			    Arrays.asList(
			    		"cabin_configurations",
			    		"engines",
			    		"fuselages",
			    		"landing_gears",
			    		"lifting_surfaces", 
			    		"nacelles" 
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
							aircraftSaveDirectives.getAircraftFileName() + ".xml", 
							ComponentEnum.AIRCRAFT
							)
					);
			if (theAircraft.getWing() != null) {
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator,
								aircraftSaveDirectives.getWingFileName() + ".xml", 
								ComponentEnum.WING
								)
						);
				if (!theAircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().isEmpty())
					for(int i=0; i<theAircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().size(); i++)
					listDocNameType.add(
							Tuple.of(
									docBuilder.newDocument(),
									aircraftDirPath + File.separator + "lifting_surfaces" + File.separator + "airfoils" + File.separator,
									aircraftSaveDirectives.getWingAirfoilFileNames().get(i) + ".xml", 
									ComponentEnum.WING_AIRFOIL
									)
							);
			}
			
			if (theAircraft.getHTail() != null) {
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator, 
								aircraftSaveDirectives.getHTailFileName() + ".xml", 
								ComponentEnum.HORIZONTAL_TAIL
								)
						);
				for(int i=0; i<theAircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList().size(); i++)
					listDocNameType.add(
							Tuple.of(
									docBuilder.newDocument(),
									aircraftDirPath + File.separator + "lifting_surfaces" + File.separator + "airfoils" + File.separator,
									aircraftSaveDirectives.getHTailAirfoilFileNames().get(i) + ".xml", 
									ComponentEnum.HORIZONTAL_TAIL_AIRFOIL
									)
							);
				}
			if (theAircraft.getVTail() != null) {
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator,
								aircraftSaveDirectives.getVTailFileName() + ".xml", 
								ComponentEnum.VERTICAL_TAIL
								)
						);
				for(int i=0; i<theAircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().size(); i++)
					listDocNameType.add(
							Tuple.of(
									docBuilder.newDocument(),
									aircraftDirPath + File.separator + "lifting_surfaces" + File.separator + "airfoils" + File.separator,
									aircraftSaveDirectives.getVTailAirfoilFileNames().get(i) + ".xml", 
									ComponentEnum.VERTICAL_TAIL_AIRFOIL
									)
							);
			}
			if (theAircraft.getCanard() != null) {
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "lifting_surfaces" + File.separator, 
								aircraftSaveDirectives.getCanardFileName() + ".xml", 
								ComponentEnum.CANARD
								)
						);
				for(int i=0; i<theAircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList().size(); i++)
					listDocNameType.add(
							Tuple.of(
									docBuilder.newDocument(),
									aircraftDirPath + File.separator + "lifting_surfaces" + File.separator + "airfoils" + File.separator,
									aircraftSaveDirectives.getCanardAirfoilFileNames().get(i) + ".xml", 
									ComponentEnum.CANARD_AIRFOIL
									)
							);
			}
			if (theAircraft.getFuselage() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "fuselages" + File.separator, 
								aircraftSaveDirectives.getFuselageFileName() + ".xml", 
								ComponentEnum.FUSELAGE
								)
						);
			if (theAircraft.getNacelles() != null)
				theAircraft.getNacelles().getNacellesList().stream().forEach(
						nacelle -> listDocNameType.add(
								Tuple.of(
										docBuilder.newDocument(),
										aircraftDirPath + File.separator + "nacelles" + File.separator,
										aircraftSaveDirectives.getNacelleFileName().substring(
												0,
												aircraftSaveDirectives.getNacelleFileName().length()-4
												) 
										+ "_" 
										+ theAircraft.getNacelles().getNacellesList().indexOf(nacelle)
										+ ".xml", 
										ComponentEnum.NACELLE
										)
								)
						);
			if (theAircraft.getPowerPlant() != null)
				theAircraft.getPowerPlant().getEngineList().forEach(
						engine -> listDocNameType.add(
								Tuple.of(
										docBuilder.newDocument(),
										aircraftDirPath + File.separator + "engines" + File.separator, 
										aircraftSaveDirectives.getEngineFileName().substring(
												0,
												aircraftSaveDirectives.getEngineFileName().length()-4
												)
										+ "_"
										+ theAircraft.getPowerPlant().getEngineList().indexOf(engine)
										+ ".xml", 
										ComponentEnum.ENGINE
										)
								)
						);
			if (theAircraft.getLandingGears() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "landing_gears" + File.separator, 
								aircraftSaveDirectives.getLandingGearFileName() + ".xml", 
								ComponentEnum.LANDING_GEAR
								)
						);
			if (theAircraft.getCabinConfiguration() != null)
				listDocNameType.add(
						Tuple.of(
								docBuilder.newDocument(),
								aircraftDirPath + File.separator + "cabin_configurations" + File.separator, 
								aircraftSaveDirectives.getCabinConfigurationFileName() + ".xml", 
								ComponentEnum.CABIN_CONFIGURATION
								)
						);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
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
		case WING_AIRFOIL:
			if (aircraft.getWing() != null) {
				aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().stream().filter(
						airfoil -> airfoil.getName().equalsIgnoreCase(
								docNameType._3().substring(0, docNameType._3().length()-4)
								)
						).findFirst().ifPresent(
								airfoil -> makeXmlTreeAirfoil(
										airfoil,
										docNameType._1(),
										docNameType._4()
										)
								);
			}
			break;
		case HORIZONTAL_TAIL:
			if (aircraft.getHTail() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case HORIZONTAL_TAIL_AIRFOIL:
			if (aircraft.getHTail() != null) {
				aircraft.getHTail().getLiftingSurfaceCreator().getAirfoilList().stream().filter(
						airfoil -> airfoil.getName().equalsIgnoreCase(
								docNameType._3().substring(0, docNameType._3().length()-4)
								)
						).findFirst().ifPresent(
								airfoil -> makeXmlTreeAirfoil(
										airfoil,
										docNameType._1(),
										docNameType._4()
										)
								);
			}
			break;
		case VERTICAL_TAIL:
			if (aircraft.getVTail() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case VERTICAL_TAIL_AIRFOIL:
			if (aircraft.getVTail() != null) {
				aircraft.getVTail().getLiftingSurfaceCreator().getAirfoilList().stream().filter(
						airfoil -> airfoil.getName().equalsIgnoreCase(
								docNameType._3().substring(0, docNameType._3().length()-4)
								)
						).findFirst().ifPresent(
								airfoil -> makeXmlTreeAirfoil(
										airfoil,
										docNameType._1(),
										docNameType._4()
										)
								);
			}
			break;
		case CANARD:
			if (aircraft.getCanard() != null)
				makeXmlTreeLiftingSurface(aircraft, docNameType._1(), aircraftSaveDirectives, docNameType._4());
			break;
		case CANARD_AIRFOIL:
			if (aircraft.getCanard() != null) {
				aircraft.getCanard().getLiftingSurfaceCreator().getAirfoilList().stream().filter(
						airfoil -> airfoil.getName().equalsIgnoreCase(
								docNameType._3().substring(0, docNameType._3().length()-4)
								)
						).findFirst().ifPresent(
								airfoil -> makeXmlTreeAirfoil(
										airfoil,
										docNameType._1(),
										docNameType._4()
										)
								);
			}
			break;
		case FUSELAGE:
			if (aircraft.getFuselage() != null)
				makeXmlTreeFuselage(aircraft, docNameType._1());
			break;
		case NACELLE:
			if (aircraft.getNacelles() != null) {
				
				String nacelleFileName = aircraftSaveDirectives.getNacelleFileName().substring(
						0,
						aircraftSaveDirectives.getNacelleFileName().length()-4
						) + "_";
				
				int nacelleIndexString = nacelleFileName.length();
				int indexOfNacelle = Integer.valueOf(String.valueOf(docNameType._3().charAt(nacelleIndexString)));
				
				makeXmlTreeNacelle(aircraft, docNameType._1(), aircraftSaveDirectives, indexOfNacelle);
			}
			break;
		case ENGINE:
			if (aircraft.getPowerPlant() != null) {
				
				String engineFileName = aircraftSaveDirectives.getEngineFileName().substring(
						0,
						aircraftSaveDirectives.getEngineFileName().length()-4
						) + "_";
				
				int engineIndexString = engineFileName.length();
				int indexOfEngine = Integer.valueOf(String.valueOf(docNameType._3().charAt(engineIndexString)));
				
				makeXmlTreeEngine(aircraft, docNameType._1(), indexOfEngine);
			}
			break;
		case LANDING_GEAR:
			if (aircraft.getLandingGears() != null)
				makeXmlTreeLandingGear(aircraft, docNameType._1());
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
		org.w3c.dom.Element panelsElement = null;
		org.w3c.dom.Element symmetricFlapsElement = null;
		org.w3c.dom.Element slatsElement = null;
		org.w3c.dom.Element asymmetricFlapsElement = null;
		org.w3c.dom.Element spoilersElement = null;
		
		switch (type) {
		case WING:
			liftingSurfaceTag = "wing";
			// make wing
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "wing" 
					Tuple.of("id", aircraft.getWing().getLiftingSurfaceCreator().getId()),
					Tuple.of("mirrored", String.valueOf(aircraft.getWing().getLiftingSurfaceCreator().isMirrored())),
					Tuple.of("equivalent", String.valueOf(aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWingFlag()))
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make wing/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - main_spar_non_dimensional_position
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "main_spar_non_dimensional_position",
						aircraft.getWing().getLiftingSurfaceCreator().getMainSparDimensionlessPosition(), 
						4, 6, // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
						Tuple.of("type", "PERCENT_CHORD"),
						Tuple.of("ref_to", "LOCAL_CHORD")
				)
			);
			// global_data - secondary_spar_non_dimensional_position
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "secondary_spar_non_dimensional_position",
						aircraft.getWing().getLiftingSurfaceCreator().getSecondarySparDimensionlessPosition(),
						4, 6, // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
						Tuple.of("type", "PERCENT_CHORD"),
						Tuple.of("ref_to", "LOCAL_CHORD")
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
							aircraft.getWing().getLiftingSurfaceCreator().getSurfacePlanform(), 
							aircraft.getWing().getLiftingSurfaceCreator().getSpan(), 
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio(),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getDihedral().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getRealWingDimensionlessKinkPosition(),
							aircraft.getWing().getLiftingSurfaceCreator().getTaperRatio(),
							aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getTwistGeometricAtTip().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE),
							aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(1).getSweepLeadingEdge().to(NonSI.DEGREE_ANGLE),
							Arrays.asList(new String[]{
									aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().get(0).getName()+".xml",
									aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().get(1).getName()+".xml",
									aircraft.getWing().getLiftingSurfaceCreator().getAirfoilList().get(2).getName()+".xml"
									})
							)
						);

			else {
				
				panelsElement = doc.createElement("panels");
				liftingSurfaceElement.appendChild(panelsElement);
				
				for(int i=0; i<aircraft.getWing().getLiftingSurfaceCreator().getPanels().size(); i++) {

					Boolean isLinked = Boolean.FALSE;
					String linkedPanelName = "";
					if(i > 0) {
						isLinked = Boolean.TRUE;
						linkedPanelName = aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(i-1).getId();
					}

					panelsElement.appendChild(
							createLiftingSurfacePanelElement(
									doc,
									aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(i),
									isLinked,
									linkedPanelName
									)
							);
				}
			}
			
			if(!aircraft.getWing().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
				symmetricFlapsElement = doc.createElement("symmetric_flaps");
				liftingSurfaceElement.appendChild(symmetricFlapsElement);

				for(int i=0; i<aircraft.getWing().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
					symmetricFlapsElement.appendChild(
							createLiftingSurfaceSymmetricFlapsElement(
									doc,
									aircraft.getWing().getLiftingSurfaceCreator().getSymmetricFlaps().get(i)
									)
							);
				}
			}
			
			if(!aircraft.getWing().getLiftingSurfaceCreator().getSlats().isEmpty()) {
				slatsElement = doc.createElement("slats");
				liftingSurfaceElement.appendChild(slatsElement);

				for(int i=0; i<aircraft.getWing().getLiftingSurfaceCreator().getSlats().size(); i++) {
					slatsElement.appendChild(
							createLiftingSurfaceSlatsElement(
									doc,
									aircraft.getWing().getLiftingSurfaceCreator().getSlats().get(i)
									)
							);
				}
			}

			if(!aircraft.getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().isEmpty()) {
				asymmetricFlapsElement = doc.createElement("asymmetric_flaps");
				liftingSurfaceElement.appendChild(asymmetricFlapsElement);

				for(int i=0; i<aircraft.getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().size(); i++) {
					asymmetricFlapsElement.appendChild(
							createLiftingSurfaceAsymmetricFlapElement(
									doc,
									aircraft.getWing().getLiftingSurfaceCreator().getAsymmetricFlaps().get(i)
									)
							);
				}
			}

			if(!aircraft.getWing().getLiftingSurfaceCreator().getSpoilers().isEmpty()) {
				spoilersElement = doc.createElement("spoilers");
				liftingSurfaceElement.appendChild(spoilersElement);

				for(int i=0; i<aircraft.getWing().getLiftingSurfaceCreator().getSpoilers().size(); i++) {
					spoilersElement.appendChild(
							createLiftingSurfaceSpolierElement(
									doc,
									aircraft.getWing().getLiftingSurfaceCreator().getSpoilers().get(i)
									)
							);
				}
			}
			
			break;
			
		case HORIZONTAL_TAIL:
			liftingSurfaceTag = "horizontal_tail";
			// make horizontal_tail
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "horizontal_tail" 
					Tuple.of("id", aircraft.getHTail().getLiftingSurfaceCreator().getId()),
					Tuple.of("mirrored", String.valueOf(aircraft.getHTail().getLiftingSurfaceCreator().isMirrored()))
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make horizontal_tail/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getHTail().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			
			panelsElement = doc.createElement("panels");
			liftingSurfaceElement.appendChild(panelsElement);
			
			for(int i=0; i<aircraft.getHTail().getLiftingSurfaceCreator().getPanels().size(); i++) {

				Boolean isLinked = Boolean.FALSE;
				String linkedPanelName = "";
				if(i > 0) {
					isLinked = Boolean.TRUE;
					linkedPanelName = aircraft.getHTail().getLiftingSurfaceCreator().getPanels().get(i).getId();
				}

				panelsElement.appendChild(
						createLiftingSurfacePanelElement(
								doc,
								aircraft.getHTail().getLiftingSurfaceCreator().getPanels().get(i),
								isLinked,
								linkedPanelName
								)
						);
			}
			
			if(!aircraft.getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
				symmetricFlapsElement = doc.createElement("symmetric_flaps");
				liftingSurfaceElement.appendChild(symmetricFlapsElement);

				for(int i=0; i<aircraft.getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
					symmetricFlapsElement.appendChild(
							createLiftingSurfaceSymmetricFlapsElement(
									doc,
									aircraft.getHTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i)
									)
							);
				}
			}
			break;
		case VERTICAL_TAIL:
			liftingSurfaceTag = "vertical_tail";
			// make vertical_tail
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "vertical_tail" 
					Tuple.of("id", aircraft.getVTail().getLiftingSurfaceCreator().getId()),
					Tuple.of("mirrored", String.valueOf(aircraft.getVTail().getLiftingSurfaceCreator().isMirrored()))
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make vertical_tail/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getVTail().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			
			panelsElement = doc.createElement("panels");
			liftingSurfaceElement.appendChild(panelsElement);
			
			for(int i=0; i<aircraft.getVTail().getLiftingSurfaceCreator().getPanels().size(); i++) {

				Boolean isLinked = Boolean.FALSE;
				String linkedPanelName = "";
				if(i > 0) {
					isLinked = Boolean.TRUE;
					linkedPanelName = aircraft.getVTail().getLiftingSurfaceCreator().getPanels().get(i).getId();
				}

				panelsElement.appendChild(
						createLiftingSurfacePanelElement(
								doc,
								aircraft.getVTail().getLiftingSurfaceCreator().getPanels().get(i),
								isLinked,
								linkedPanelName
								)
						);
			}
			
			if(!aircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
				symmetricFlapsElement = doc.createElement("symmetric_flaps");
				liftingSurfaceElement.appendChild(symmetricFlapsElement);

				for(int i=0; i<aircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
					symmetricFlapsElement.appendChild(
							createLiftingSurfaceSymmetricFlapsElement(
									doc,
									aircraft.getVTail().getLiftingSurfaceCreator().getSymmetricFlaps().get(i)
									)
							);
				}
			}
			
			break;
		case CANARD:
			liftingSurfaceTag = "canard";
			// make canard
			liftingSurfaceElement = createXMLElementWithAttributes(doc, liftingSurfaceTag, // "canard" 
					Tuple.of("id", aircraft.getCanard().getLiftingSurfaceCreator().getId()),
					Tuple.of("mirrored", String.valueOf(aircraft.getCanard().getLiftingSurfaceCreator().isMirrored()))
			);
			rootElement.appendChild(liftingSurfaceElement);
			
			// make vertical_tail/global_data
			globalDataElement = doc.createElement("global_data");
			liftingSurfaceElement.appendChild(globalDataElement);
			// global_data - roughness
			globalDataElement.appendChild(
				createXMLElementWithValueAndAttributes(doc, "roughness",
						aircraft.getCanard().getLiftingSurfaceCreator().getRoughness(),
						8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
				)
			);
			
			panelsElement = doc.createElement("panels");
			liftingSurfaceElement.appendChild(panelsElement);
			
			for(int i=0; i<aircraft.getCanard().getLiftingSurfaceCreator().getPanels().size(); i++) {

				Boolean isLinked = Boolean.FALSE;
				String linkedPanelName = "";
				if(i > 0) {
					isLinked = Boolean.TRUE;
					linkedPanelName = aircraft.getCanard().getLiftingSurfaceCreator().getPanels().get(i).getId();
				}

				panelsElement.appendChild(
						createLiftingSurfacePanelElement(
								doc,
								aircraft.getCanard().getLiftingSurfaceCreator().getPanels().get(i),
								isLinked,
								linkedPanelName
								)
						);
			}
			
			if(!aircraft.getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().isEmpty()) {
				symmetricFlapsElement = doc.createElement("symmetric_flaps");
				liftingSurfaceElement.appendChild(symmetricFlapsElement);

				for(int i=0; i<aircraft.getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().size(); i++) {
					symmetricFlapsElement.appendChild(
							createLiftingSurfaceSymmetricFlapsElement(
									doc,
									aircraft.getCanard().getLiftingSurfaceCreator().getSymmetricFlaps().get(i)
									)
							);
				}
			}

			break;

		default:
			break;
		}
		
		
	}

	private static void makeXmlTreeAirfoil(Airfoil airfoil, Document doc, ComponentEnum componentType) {
		
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);
		
		// airfoil
		org.w3c.dom.Element airfoilElement = createXMLElementWithAttributes(doc, "airfoil", 
				Tuple.of("name", airfoil.getName()),
				Tuple.of("type", airfoil.getType().toString()),
				Tuple.of("family", airfoil.getFamily().toString())
				);
		rootElement.appendChild(airfoilElement);
		
		// geometry
		org.w3c.dom.Element geometryElement = doc.createElement("geometry");
		airfoilElement.appendChild(geometryElement);
		
		// geometry - thickness_to_chord_ratio_max
		JPADStaticWriteUtils.writeSingleNode("thickness_to_chord_ratio_max", 
				airfoil.getThicknessToChordRatio(), 
				geometryElement, doc);
		
		// geometry - radius_leading_edge_normalized
		JPADStaticWriteUtils.writeSingleNode("radius_leading_edge_normalized", 
				airfoil.getRadiusLeadingEdge(), 
				geometryElement, doc);
		
		// geometry - x_coordinates
		JPADStaticWriteUtils.writeSingleNode("x_coordinates", 
				airfoil.getXCoords(), 
				geometryElement, doc);
		
		// geometry - z_coordinates
		JPADStaticWriteUtils.writeSingleNode("z_coordinates", 
				airfoil.getZCoords(),
				geometryElement, doc);
		
		// aerodynamics
		org.w3c.dom.Element aerodynamicsElement = createXMLElementWithAttributes(doc, "aerodynamics", 
				Tuple.of("external_cl_curve", String.valueOf(airfoil.getClCurveFromFile())),
				Tuple.of("external_cd_curve", String.valueOf(airfoil.getCdCurveFromFile())),
				Tuple.of("external_cm_curve", String.valueOf(airfoil.getCmCurveFromFile()))
				);
		airfoilElement.appendChild(aerodynamicsElement);
		
		if(!airfoil.getClCurveFromFile()) {

			// aerodynamics - alpha_zero_lift
			JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift", 
					airfoil.getAlphaZeroLift(), 
					aerodynamicsElement, doc);

			// aerodynamics - alpha_end_linear_trait
			JPADStaticWriteUtils.writeSingleNode("alpha_end_linear_trait", 
					airfoil.getAlphaEndLinearTrait(), 
					aerodynamicsElement, doc);

			// aerodynamics - alpha_stall
			JPADStaticWriteUtils.writeSingleNode("alpha_stall", 
					airfoil.getAlphaStall(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cl_alpha_linear_trait
			JPADStaticWriteUtils.writeSingleNode("Cl_alpha_linear_trait", 
					airfoil.getClAlphaLinearTrait(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cl_at_alpha_zero
			JPADStaticWriteUtils.writeSingleNode("Cl_at_alpha_zero", 
					airfoil.getClAtAlphaZero(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cl_end_linear_trait
			JPADStaticWriteUtils.writeSingleNode("Cl_end_linear_trait", 
					airfoil.getClEndLinearTrait(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cl_max
			JPADStaticWriteUtils.writeSingleNode("Cl_max", 
					airfoil.getClMax(), 
					aerodynamicsElement, doc);

		}
		
		if(!airfoil.getCdCurveFromFile()) {
			// aerodynamics - Cd_min
			JPADStaticWriteUtils.writeSingleNode("Cd_min", 
					airfoil.getCdMin(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cl_at_Cdmin
			JPADStaticWriteUtils.writeSingleNode("Cl_at_Cdmin", 
					airfoil.getClAtCdMin(), 
					aerodynamicsElement, doc);

			// aerodynamics - laminar_bucket_semi_extension
			JPADStaticWriteUtils.writeSingleNode("laminar_bucket_semi_extension", 
					airfoil.getLaminarBucketSemiExtension(), 
					aerodynamicsElement, doc);

			// aerodynamics - laminar_bucket_depth
			JPADStaticWriteUtils.writeSingleNode("laminar_bucket_depth", 
					airfoil.getLaminarBucketDepth(), 
					aerodynamicsElement, doc);

			// aerodynamics - K_factor_drag_polar
			JPADStaticWriteUtils.writeSingleNode("K_factor_drag_polar", 
					airfoil.getKFactorDragPolar(), 
					aerodynamicsElement, doc);
		}

		if(!airfoil.getCmCurveFromFile()) {
			// aerodynamics - Cm_alpha_quarter_chord
			JPADStaticWriteUtils.writeSingleNode("Cm_alpha_quarter_chord", 
					airfoil.getCmAlphaQuarterChord(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cm_ac
			JPADStaticWriteUtils.writeSingleNode("Cm_ac", 
					airfoil.getCmAC(), 
					aerodynamicsElement, doc);

			// aerodynamics - Cm_ac_at_stall
			JPADStaticWriteUtils.writeSingleNode("Cm_ac_at_stall", 
					airfoil.getCmACAtStall(), 
					aerodynamicsElement, doc);
		}
		
		// aerodynamics - airfoil_curves
		org.w3c.dom.Element airfoilCurvesElement = null;
		if(airfoil.getClCurveFromFile()
				|| airfoil.getCdCurveFromFile()
				|| airfoil.getCmCurveFromFile()
				) {
			airfoilCurvesElement = doc.createElement("airfoil_curves");
			aerodynamicsElement.appendChild(airfoilCurvesElement);
		}
		
		// aerodynamics - airfoil_curves - Cl_curve
		if(airfoil.getClCurveFromFile()) {
			JPADStaticWriteUtils.writeSingleNode("Cl_curve", 
					airfoil.getClCurve(), 
					airfoilCurvesElement, doc);

			// aerodynamics - airfoil_curves - alpha_for_Cl_curve 
			JPADStaticWriteUtils.writeSingleNode("alpha_for_Cl_curve", 
					airfoil.getAlphaForClCurve(), 
					airfoilCurvesElement, doc);
		}
		
		// aerodynamics - airfoil_curves - Cd_curve
		if(airfoil.getCdCurveFromFile()) {
			JPADStaticWriteUtils.writeSingleNode("Cd_curve", 
					airfoil.getCdCurve(), 
					airfoilCurvesElement, doc);

			// aerodynamics - airfoil_curves - Cl_for_Cd_curve 
			JPADStaticWriteUtils.writeSingleNode("Cl_for_Cd_curve", 
					airfoil.getClForCdCurve(), 
					airfoilCurvesElement, doc);
		}

		// aerodynamics - airfoil_curves - Cm_curve 
		if(airfoil.getCmCurveFromFile()) {
			JPADStaticWriteUtils.writeSingleNode("Cm_curve", 
					airfoil.getCmCurve(), 
					airfoilCurvesElement, doc);

			// aerodynamics - airfoil_curves - Cl_for_Cm_curve 
			JPADStaticWriteUtils.writeSingleNode("Cl_for_Cm_curve", 
					airfoil.getClForCmCurve(), 
					airfoilCurvesElement, doc);
		}

		// aerodynamics - x_ac_normalized
		JPADStaticWriteUtils.writeSingleNode("x_ac_normalized", 
				airfoil.getXACNormalized(), 
				aerodynamicsElement, doc);
		
		// aerodynamics - mach_critical
		JPADStaticWriteUtils.writeSingleNode("mach_critical", 
				airfoil.getMachCritical(), 
				aerodynamicsElement, doc);
		
		// aerodynamics - x_transition_upper
		JPADStaticWriteUtils.writeSingleNode("x_transition_upper", 
				airfoil.getXTransitionUpper(), 
				aerodynamicsElement, doc);
		
		// aerodynamics - x_transition_lower
		JPADStaticWriteUtils.writeSingleNode("x_transition_lower", 
				airfoil.getXTransitionLower(), 
				aerodynamicsElement, doc);
		
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
					Tuple.of("file", aircraftSaveDirectives.getCabinConfigurationFileName() + ".xml")
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
				if(ls.getLiftingSurfaceCreator().getType().equals(ComponentEnum.WING))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getWingFileName() + ".xml");
				else if(ls.getLiftingSurfaceCreator().getType().equals(ComponentEnum.HORIZONTAL_TAIL))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getHTailFileName() + ".xml");
				else if(ls.getLiftingSurfaceCreator().getType().equals(ComponentEnum.VERTICAL_TAIL))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getVTailFileName() + ".xml");
				else if(ls.getLiftingSurfaceCreator().getType().equals(ComponentEnum.CANARD))
					liftingSurfacesFileNames.add(aircraftSaveDirectives.getCanardFileName() + ".xml");
			});
		
		liftingSurfacesList.stream()
			.forEach(ls -> 
				liftingSurfacesElement.appendChild(
						createLiftingSurfaceElement(doc, 
								ls.getLiftingSurfaceCreator().getType(),  
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
									aircraftSaveDirectives.getFuselageFileName() + ".xml", 
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
							aircraftSaveDirectives.getEngineFileName().substring(
									0,
									aircraftSaveDirectives.getEngineFileName().length()-4
									)  + "_" + aircraft.getPowerPlant().getEngineList().indexOf(e) + ".xml", 
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
		
		// nacelle
		aircraft.getNacelles().getNacellesList().stream()
		.forEach(e -> {
			nacellesElement.appendChild(
					createNacelleElement(doc, 
							aircraftSaveDirectives.getNacelleFileName().substring(
									0,
									aircraftSaveDirectives.getNacelleFileName().length()-4
									) + "_" + aircraft.getNacelles().getNacellesList().indexOf(e) + ".xml", 
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
		aircraft.getComponentsList().stream()
		.filter(comp -> comp.getClass() == LandingGears.class)
			.map(comp -> (LandingGears) comp)
				.forEach(lg ->
				aircraftElement.appendChild(
						createLandingGearElement(doc, 
								aircraftSaveDirectives.getLandingGearFileName() + ".xml", 
								lg.getXApexConstructionAxesMainGear(),
								lg.getYApexConstructionAxesMainGear(),
								lg.getZApexConstructionAxesMainGear(),
								lg.getMountingPosition()
								)
						)
					);
	
		// systemS
		aircraft.getComponentsList().stream()
		.filter(comp -> comp.getClass() == Systems.class)
			.map(comp -> (Systems) comp)
				.forEach(sys ->
				aircraftElement.appendChild(
						createXMLElementWithAttributes(doc, "systems", 
								Tuple.of("primary_electrical_systems_type", aircraft.getTheAircraftInterface().getPrimaryElectricSystemsType().toString())
						)
					)
				);
		
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
				aircraft.getCabinConfiguration().getActualPassengerNumber(), 
				globalDataElement, doc);
		
		// global_data - maximum_passengers_number
		JPADStaticWriteUtils.writeSingleNode("maximum_passengers_number", 
				aircraft.getCabinConfiguration().getMaximumPassengerNumber(), 
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
				aircraft.getCabinConfiguration().getClassesType(),
				globalDataElement, doc);
		
		// global_data - aisles_number
		JPADStaticWriteUtils.writeSingleNode("aisles_number", 
				aircraft.getCabinConfiguration().getAislesNumber(),
				globalDataElement, doc);
		
		// global_data -  x_coordinates_first_row
		JPADStaticWriteUtils.writeSingleNode("x_coordinates_first_row", 
				aircraft.getCabinConfiguration().getXCoordinatesFirstRow(),
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
		List<Integer> numberOfColumnsEconomyClass = new ArrayList<>();
		if(aircraft.getCabinConfiguration().getNumberOfColumnsEconomyClass() != null)
			numberOfColumnsEconomyClass = Arrays.asList(aircraft.getCabinConfiguration().getNumberOfColumnsEconomyClass());
		else
			numberOfColumnsEconomyClass.add(0);
		JPADStaticWriteUtils.writeSingleNode("number_of_columns_economy_class", 
				numberOfColumnsEconomyClass, 
				detailedDataElement, doc);
		
		// detailed_data - number_of_columns_business_class
		List<Integer> numberOfColumnsBusinessClass = new ArrayList<>();
		if(aircraft.getCabinConfiguration().getNumberOfColumnsBusinessClass() != null)
			numberOfColumnsBusinessClass = Arrays.asList(aircraft.getCabinConfiguration().getNumberOfColumnsBusinessClass());
		else
			numberOfColumnsBusinessClass.add(0);
		JPADStaticWriteUtils.writeSingleNode("number_of_columns_business_class", 
				numberOfColumnsBusinessClass, 
				detailedDataElement, doc);
		
		// detailed_data - number_of_columns_first_class
		List<Integer> numberOfColumnsFirstClass = new ArrayList<>();
		if(aircraft.getCabinConfiguration().getNumberOfColumnsFirstClass() != null)
			numberOfColumnsFirstClass = Arrays.asList(aircraft.getCabinConfiguration().getNumberOfColumnsFirstClass());
		else
			numberOfColumnsFirstClass.add(0);
		JPADStaticWriteUtils.writeSingleNode("number_of_columns_first_class", 
				numberOfColumnsFirstClass, 
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
		
		// fuselage
		org.w3c.dom.Element fuselageElement = createXMLElementWithAttributes(doc, "fuselage", 
				Tuple.of("id", aircraft.getFuselage().getFuselageCreator().getId()),
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
				aircraft.getFuselage().getFuselageCreator().getFuselageLength(), 
				globalDataElement, doc);
		
		// global_data - roughness
		JPADStaticWriteUtils.writeSingleNode("roughness", 
				aircraft.getFuselage().getFuselageCreator().getRoughness(), 
				globalDataElement, doc);

		// nose_trunk
		org.w3c.dom.Element noseTrunkElement = doc.createElement("nose_trunk");
		fuselageElement.appendChild(noseTrunkElement);
		
		// nose_trunk - length_ratio
		JPADStaticWriteUtils.writeSingleNode("length_ratio", 
				aircraft.getFuselage().getFuselageCreator().getNoseLengthRatio(), 
				noseTrunkElement, doc);
		
		// nose_trunk - fineness_ratio
		JPADStaticWriteUtils.writeSingleNode("fineness_ratio", 
				aircraft.getFuselage().getFuselageCreator().getNoseFinenessRatio(), 
				noseTrunkElement, doc);
		
		// nose_trunk - tip_height_offset
		JPADStaticWriteUtils.writeSingleNode("tip_height_offset", 
				aircraft.getFuselage().getFuselageCreator().getNoseTipOffset(), 
				noseTrunkElement, doc);
		
		// nose_trunk - dx_cap_percent
		JPADStaticWriteUtils.writeSingleNode("dx_cap_percent", 
				aircraft.getFuselage().getFuselageCreator().getNoseCapOffsetPercent(), 
				noseTrunkElement, doc);
		
		// nose_trunk - windshield_type
		JPADStaticWriteUtils.writeSingleNode("windshield_type", 
				aircraft.getFuselage().getFuselageCreator().getWindshieldType(), 
				noseTrunkElement, doc);
		
		// nose_trunk - windshield_width
		JPADStaticWriteUtils.writeSingleNode("windshield_width", 
				aircraft.getFuselage().getFuselageCreator().getWindshieldWidth(), 
				noseTrunkElement, doc);
		
		// nose_trunk - windshield_height
		JPADStaticWriteUtils.writeSingleNode("windshield_height", 
				aircraft.getFuselage().getFuselageCreator().getWindshieldHeight(), 
				noseTrunkElement, doc);
		
		// nose_trunk - mid_section_lower_to_total_height_ratio
		JPADStaticWriteUtils.writeSingleNode("mid_section_lower_to_total_height_ratio", 
				aircraft.getFuselage().getFuselageCreator().getSectionNoseMidLowerToTotalHeightRatio(), 
				noseTrunkElement, doc);
		
		// nose_trunk - mid_section_rho_upper
		JPADStaticWriteUtils.writeSingleNode("mid_section_rho_upper", 
				aircraft.getFuselage().getFuselageCreator().getSectionMidNoseRhoUpper(), 
				noseTrunkElement, doc);
		
		// nose_trunk - mid_section_rho_lower
		JPADStaticWriteUtils.writeSingleNode("mid_section_rho_lower", 
				aircraft.getFuselage().getFuselageCreator().getSectionMidNoseRhoLower(), 
				noseTrunkElement, doc);
		
		// cylindrical_trunk
		org.w3c.dom.Element cylindricalTrunkElement = doc.createElement("cylindrical_trunk");
		fuselageElement.appendChild(cylindricalTrunkElement);
		
		// cylindrical_trunk - length_ratio
		JPADStaticWriteUtils.writeSingleNode("length_ratio", 
				aircraft.getFuselage().getFuselageCreator().getCylinderLengthRatio(), 
				cylindricalTrunkElement, doc);
		
		// cylindrical_trunk - section_width
		JPADStaticWriteUtils.writeSingleNode("section_width", 
				aircraft.getFuselage().getFuselageCreator().getSectionCylinderWidth(), 
				cylindricalTrunkElement, doc);
		
		// cylindrical_trunk - section_height
		JPADStaticWriteUtils.writeSingleNode("section_height", 
				aircraft.getFuselage().getFuselageCreator().getSectionCylinderHeight(), 
				cylindricalTrunkElement, doc);
		
		// cylindrical_trunk - height_from_ground
		JPADStaticWriteUtils.writeSingleNode("height_from_ground", 
				aircraft.getFuselage().getFuselageCreator().getHeightFromGround(), 
				cylindricalTrunkElement, doc);
		
		// cylindrical_trunk - section_lower_to_total_height_ratio
		JPADStaticWriteUtils.writeSingleNode("section_lower_to_total_height_ratio", 
				aircraft.getFuselage().getFuselageCreator().getSectionCylinderLowerToTotalHeightRatio(), 
				cylindricalTrunkElement, doc);
		
		// cylindrical_trunk - section_rho_upper
		JPADStaticWriteUtils.writeSingleNode("section_rho_upper", 
				aircraft.getFuselage().getFuselageCreator().getSectionCylinderRhoUpper(), 
				cylindricalTrunkElement, doc);
		
		// cylindrical_trunk - section_rho_lower
		JPADStaticWriteUtils.writeSingleNode("section_rho_lower", 
				aircraft.getFuselage().getFuselageCreator().getSectionCylinderRhoLower(), 
				cylindricalTrunkElement, doc);
		
		// tail_trunk
		org.w3c.dom.Element tailTrunkElement = doc.createElement("tail_trunk");
		fuselageElement.appendChild(tailTrunkElement);
		
		// tail_trunk - tip_height_offset
		JPADStaticWriteUtils.writeSingleNode("tip_height_offset", 
				aircraft.getFuselage().getFuselageCreator().getTailTipOffset(), 
				tailTrunkElement, doc);
		
		// tail_trunk - dx_cap_percent
		JPADStaticWriteUtils.writeSingleNode("dx_cap_percent", 
				aircraft.getFuselage().getFuselageCreator().getTailCapOffsetPercent(), 
				tailTrunkElement, doc);
		
		// tail_trunk - mid_section_lower_to_total_height_ratio
		JPADStaticWriteUtils.writeSingleNode("mid_section_lower_to_total_height_ratio", 
				aircraft.getFuselage().getFuselageCreator().getSectionTailMidLowerToTotalHeightRatio(), 
				tailTrunkElement, doc);
		
		// tail_trunk - mid_section_rho_upper
		JPADStaticWriteUtils.writeSingleNode("mid_section_rho_upper", 
				aircraft.getFuselage().getFuselageCreator().getSectionMidTailRhoUpper(), 
				tailTrunkElement, doc);
		
		// tail_trunk - mid_section_rho_lower
		JPADStaticWriteUtils.writeSingleNode("mid_section_rho_lower", 
				aircraft.getFuselage().getFuselageCreator().getSectionMidTailRhoLower(), 
				tailTrunkElement, doc);
		
		if(!aircraft.getFuselage().getFuselageCreator().getSpoilers().isEmpty()) {
			
			// spoilers
			org.w3c.dom.Element spoilersElement = doc.createElement("spoilers");
			fuselageElement.appendChild(spoilersElement);
			
			aircraft.getFuselage().getFuselageCreator().getSpoilers().stream().forEach(
					sp -> {
						
						int spoilerIndex = aircraft.getFuselage().getFuselageCreator().getSpoilers().indexOf(sp);
						
						// spoiler
						org.w3c.dom.Element spoilersInnerElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
								doc,
								"spoiler",
								Tuple.of("id", String.valueOf(spoilerIndex))
								);
						spoilersElement.appendChild(spoilersInnerElement);
						
						// spoiler - inner_station_spanwise_position
						JPADStaticWriteUtils.writeSingleNode("inner_station_spanwise_position", 
								aircraft.getFuselage().getFuselageCreator().getSpoilers().get(spoilerIndex).getInnerStationSpanwisePosition(), 
								spoilersInnerElement, doc);
						
						// spoiler - outer_station_spanwise_position
						JPADStaticWriteUtils.writeSingleNode("outer_station_spanwise_position", 
								aircraft.getFuselage().getFuselageCreator().getSpoilers().get(spoilerIndex).getOuterStationSpanwisePosition(), 
								spoilersInnerElement, doc);
						
						// spoiler - inner_station_chordwise_position
						JPADStaticWriteUtils.writeSingleNode("inner_station_chordwise_position", 
								aircraft.getFuselage().getFuselageCreator().getSpoilers().get(spoilerIndex).getInnerStationChordwisePosition(), 
								spoilersInnerElement, doc);
						
						// spoiler - outer_station_chordwise_position
						JPADStaticWriteUtils.writeSingleNode("outer_station_chordwise_position", 
								aircraft.getFuselage().getFuselageCreator().getSpoilers().get(spoilerIndex).getOuterStationChordwisePosition(), 
								spoilersInnerElement, doc);
						
						// spoiler - min_deflection
						JPADStaticWriteUtils.writeSingleNode("min_deflection", 
								aircraft.getFuselage().getFuselageCreator().getSpoilers().get(spoilerIndex).getMinimumDeflection(), 
								spoilersInnerElement, doc);
						
						// spoiler - max_deflection
						JPADStaticWriteUtils.writeSingleNode("max_deflection", 
								aircraft.getFuselage().getFuselageCreator().getSpoilers().get(spoilerIndex).getMaximumDeflection(), 
								spoilersInnerElement, doc);
						
					});
		}
	} 
	
	private static void makeXmlTreeNacelle(Aircraft aircraft, Document doc, AircraftSaveDirectives aircraftSaveDirectives, int indexOfNacelle) {
		
		NacelleCreator nacelle = aircraft.getNacelles().getNacellesList().get(indexOfNacelle);
		
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);
		
		// nacelle
		org.w3c.dom.Element nacelleElement = createXMLElementWithAttributes(doc, "nacelle", 
				Tuple.of("id", nacelle.getId()),
				Tuple.of("engine", aircraftSaveDirectives.getEngineFileName().substring(
						0,
						aircraftSaveDirectives.getEngineFileName().length()-4
						) + "_" + indexOfNacelle + ".xml")
				);
		rootElement.appendChild(nacelleElement);
		
		// global_data
		org.w3c.dom.Element globalDataElement = doc.createElement("global_data");
		nacelleElement.appendChild(globalDataElement);
		
		// global_data - roughness
		globalDataElement.appendChild(
			createXMLElementWithValueAndAttributes(doc, "roughness",
					nacelle.getRoughness(),
					8, 6  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
			)
		);
		
		// geometry
		org.w3c.dom.Element geometryElement = doc.createElement("geometry");
		nacelleElement.appendChild(geometryElement);
		
		// geometry - length
		JPADStaticWriteUtils.writeSingleNode("length", 
				nacelle.getLength(), 
				geometryElement, doc);
		
		// geometry - maximum_diameter
		JPADStaticWriteUtils.writeSingleNode("maximum_diameter", 
				nacelle.getDiameterMax(), 
				geometryElement, doc);
		
		// geometry - k_inlet
		JPADStaticWriteUtils.writeSingleNode("k_inlet", 
				nacelle.getKInlet(), 
				geometryElement, doc);
		
		// geometry - k_outlet
		JPADStaticWriteUtils.writeSingleNode("k_outlet", 
				nacelle.getKOutlet(), 
				geometryElement, doc);
		
		// geometry - k_outlet
		JPADStaticWriteUtils.writeSingleNode("k_length", 
				nacelle.getKLength(), 
				geometryElement, doc);
		
		// geometry - k_diameter_outlet
		JPADStaticWriteUtils.writeSingleNode("k_diameter_outlet", 
				nacelle.getKDiameterOutlet(), 
				geometryElement, doc);
		
	}
	
	private static void makeXmlTreeEngine(Aircraft aircraft, Document doc, int indexOfEngine) {
		
		Engine engine = aircraft.getPowerPlant().getEngineList().get(indexOfEngine);
		
		EngineTypeEnum engineType = engine.getEngineType();
		
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);
		
		// engine
		org.w3c.dom.Element engineElement = createXMLElementWithAttributes(doc, "engine", 
				Tuple.of("id", engine.getId()),
				Tuple.of("type", engine.getEngineType().toString()),
				Tuple.of("database", engine.getEngineDatabaseName())
				);
		rootElement.appendChild(engineElement);
		
		// dimensions
		org.w3c.dom.Element dimensionsElement = doc.createElement("dimensions");
		engineElement.appendChild(dimensionsElement);
		
		// dimensions - length
		JPADStaticWriteUtils.writeSingleNode("length", 
				engine.getLength(), 
				dimensionsElement, doc);
		
		 if(engineType == EngineTypeEnum.TURBOPROP || engineType == EngineTypeEnum.PISTON) {

			 // dimensions - propeller_diameter
			 JPADStaticWriteUtils.writeSingleNode("propeller_diameter", 
					 engine.getPropellerDiameter(), 
					 dimensionsElement, doc);

		 }
		 
		 // specifications
		 org.w3c.dom.Element specificationsElement = doc.createElement("specifications");
		 engineElement.appendChild(specificationsElement); 
		 
		 // specifications - dry_mass
		 specificationsElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
						 doc,
						 "dry_mass",
						 engine.getDryMassPublicDomain(),
						 3, 6,  // above=6 : 1.0000001 -> 1.00000 ___ below=3 : 10333701 -> 10334000 
						 Tuple.of("calculate", "FALSE")
						 )
				 );
		 
		 if(engineType == EngineTypeEnum.TURBOFAN || engineType == EngineTypeEnum.TURBOJET) {
			
			 // specifications - static_thrust
			 JPADStaticWriteUtils.writeSingleNode("static_thrust", 
					 engine.getT0(), 
					 specificationsElement, doc);
			 
			 // specifications - by_pass_ratio
			 JPADStaticWriteUtils.writeSingleNode("by_pass_ratio", 
					 engine.getBPR(), 
					 specificationsElement, doc);
			 
		 }
		 
		 if(engineType == EngineTypeEnum.TURBOPROP || engineType == EngineTypeEnum.TURBOFAN || engineType == EngineTypeEnum.TURBOJET) { 
		
			 // specifications - number_of_compressor_stages
			 JPADStaticWriteUtils.writeSingleNode("number_of_compressor_stages", 
					 engine.getNumberOfCompressorStages(), 
					 specificationsElement, doc);
			 
			 // specifications - number_of_shafts
			 JPADStaticWriteUtils.writeSingleNode("number_of_shafts", 
					 engine.getNumberOfShafts(), 
					 specificationsElement, doc);
			 
			 // specifications - number_of_shafts
			 JPADStaticWriteUtils.writeSingleNode("overall_pressure_ratio", 
					 engine.getOverallPressureRatio(), 
					 specificationsElement, doc);
			 
		 }
		 
		 
		 if(engineType == EngineTypeEnum.TURBOPROP || engineType == EngineTypeEnum.PISTON) {
			 
			 // specifications - static_power
			 JPADStaticWriteUtils.writeSingleNode("static_power", 
					 engine.getP0(), 
					 specificationsElement, doc);
			 
			 // specifications - number_of_propeller_blades
			 JPADStaticWriteUtils.writeSingleNode("number_of_propeller_blades", 
					 engine.getNumberOfBlades(), 
					 specificationsElement, doc);
			 
			 // specifications - eta_propeller
			 JPADStaticWriteUtils.writeSingleNode("eta_propeller", 
					 engine.getEtaPropeller(), 
					 specificationsElement, doc);
			 
			 
		 }

	}
	
	private static void makeXmlTreeLandingGear(Aircraft aircraft, Document doc) {
		
		org.w3c.dom.Element rootElement = doc.createElement("jpad_config");
		doc.appendChild(rootElement);

		// landing gear
		org.w3c.dom.Element landingGearElement = createXMLElementWithAttributes(doc, "landing_gears", 
				Tuple.of("id", aircraft.getLandingGears().getId())
				);
		rootElement.appendChild(landingGearElement);

		// global_data
		org.w3c.dom.Element globalDataElement = doc.createElement("global_data");
		landingGearElement.appendChild(globalDataElement);
		
		// global_data - main_gear_legs_length
		JPADStaticWriteUtils.writeSingleNode("main_gear_legs_length", 
				aircraft.getLandingGears().getMainLegsLenght(), 
				globalDataElement, doc);
		
		// global_data - distance_between_wheels
				JPADStaticWriteUtils.writeSingleNode("distance_between_wheels", 
						aircraft.getLandingGears().getDistanceBetweenWheels(), 
						globalDataElement, doc);
		
		// global_data - number_of_frontal_wheels
		JPADStaticWriteUtils.writeSingleNode("number_of_frontal_wheels", 
				aircraft.getLandingGears().getNumberOfFrontalWheels(), 
				globalDataElement, doc);
		
		// global_data - number_of_rear_wheels
		JPADStaticWriteUtils.writeSingleNode("number_of_rear_wheels", 
				aircraft.getLandingGears().getNumberOfRearWheels(), 
				globalDataElement, doc);
		
		// frontal_wheels_data
		org.w3c.dom.Element frontalWheelsDataElement = doc.createElement("frontal_wheels_data");
		landingGearElement.appendChild(frontalWheelsDataElement);
		
		// frontal_wheels_data - wheel_heigtt
		JPADStaticWriteUtils.writeSingleNode("wheel_height", 
				aircraft.getLandingGears().getFrontalWheelsHeight(), 
				frontalWheelsDataElement, doc);
		
		// frontal_wheels_data - wheel_width
		JPADStaticWriteUtils.writeSingleNode("wheel_width", 
				aircraft.getLandingGears().getFrontalWheelsWidth(), 
				frontalWheelsDataElement, doc);
		
		// rear_wheels_data
		org.w3c.dom.Element rearWheelsDataElement = doc.createElement("rear_wheels_data");
		landingGearElement.appendChild(rearWheelsDataElement);
		
		// frontal_wheels_data - wheel_heigtt
		JPADStaticWriteUtils.writeSingleNode("wheel_height", 
				aircraft.getLandingGears().getRearWheelsHeight(), 
				rearWheelsDataElement, doc);
		
		// frontal_wheels_data - wheel_width
		JPADStaticWriteUtils.writeSingleNode("wheel_width", 
				aircraft.getLandingGears().getRearWheelsWidth(), 
				rearWheelsDataElement, doc);
		
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

	public static org.w3c.dom.Element createXMLElementWithAttributes(Document doc, String elementName, 
			List<Tuple2<String,String>> attributeValueTuples) {
		org.w3c.dom.Element element = doc.createElement(elementName);
		attributeValueTuples.stream()
			.forEach( tpl -> {
				org.w3c.dom.Attr a = doc.createAttribute(tpl._1());
				a.setValue(tpl._2());
				element.setAttributeNode(a);
			});
		return element;
	}
	
	@SafeVarargs
	public static org.w3c.dom.Element createXMLElementWithContentAndAttributes(Document doc, String elementName, String content,
			Tuple2<String,String>... attributeValueTuples) {
		org.w3c.dom.Element element = doc.createElement(elementName);
		Arrays.stream(attributeValueTuples)
			.forEach( tpl -> {
				org.w3c.dom.Attr a = doc.createAttribute(tpl._1());
				a.setValue(tpl._2());
				element.setAttributeNode(a);
			});

		element.appendChild(doc.createTextNode(content));

		return element;
	}

	public static org.w3c.dom.Element createXMLElementWithContentAndAttributes(Document doc, String elementName, String content,
			List<Tuple2<String,String>> attributeValueTuples) {
		org.w3c.dom.Element element = doc.createElement(elementName);
		attributeValueTuples.stream()
			.forEach( tpl -> {
				org.w3c.dom.Attr a = doc.createAttribute(tpl._1());
				a.setValue(tpl._2());
				element.setAttributeNode(a);
			});

		element.appendChild(doc.createTextNode(content));

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

	public static org.w3c.dom.Element createXMLElementWithValueAndAttributes(Document doc, String elementName, Object valueToWrite,
			int above, int below,
			List<Tuple2<String,String>> attributeValueTuples) {
		org.w3c.dom.Element element = doc.createElement(elementName);
		attributeValueTuples.stream()
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
	
	public static org.w3c.dom.Element createXMLElementWithValue(Document doc, String elementName, String value) {
		org.w3c.dom.Element element = doc.createElement(elementName);
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
			NacelleMountingPositionEnum mountingPosition) {
		
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
			LandingGearsMountingPositionEnum mountingPosition) {
		
		org.w3c.dom.Element element = createXMLElementWithAttributes(
				doc,
				"landing_gears",
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
				"systems",
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
	
	public static org.w3c.dom.Element createLiftingSurfacePanelElement(
			Document doc, 
			LiftingSurfacePanelCreator panel,
			Boolean isLinked,
			String linkedPanelName
			) {
				
		org.w3c.dom.Element element = null;
		if(isLinked)
			element = createXMLElementWithAttributes(
					doc,
					"panel",
					Tuple.of("id", panel.getId()),
					Tuple.of("linked_to", linkedPanelName)
					);
		else
			element = createXMLElementWithAttributes(
					doc,
					"panel",
					Tuple.of("id", panel.getId())	
					);
			
		
		JPADStaticWriteUtils.writeSingleNode("span", panel.getSpan(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("dihedral", panel.getDihedral(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("sweep_leading_edge", panel.getSweepLeadingEdge(), element, doc);
		
		if (!isLinked) {
			org.w3c.dom.Element innerSectionElement = createXMLElementWithAttributes(
					doc,
					"inner_section"
					);
			element.appendChild(innerSectionElement);
			JPADStaticWriteUtils.writeSingleNode("chord", panel.getChordRoot(), innerSectionElement, doc);
			innerSectionElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithAttributes(doc, "airfoil", Tuple.of("file", panel.getAirfoilRoot().getName() + ".xml"))
					); 
			JPADStaticWriteUtils.writeSingleNode("geometric_twist", 0.0, innerSectionElement, doc);
		}
		
		org.w3c.dom.Element outerSectionElement = createXMLElementWithAttributes(
				doc,
				"outer_section"
				);
		element.appendChild(outerSectionElement);
		JPADStaticWriteUtils.writeSingleNode("chord", panel.getChordTip(), outerSectionElement, doc);
		outerSectionElement.appendChild(
				JPADStaticWriteUtils.createXMLElementWithAttributes(doc, "airfoil", Tuple.of("file", panel.getAirfoilTip().getName() + ".xml"))
				);  
		JPADStaticWriteUtils.writeSingleNode("geometric_twist", panel.getTwistGeometricAtTip(), outerSectionElement, doc);

		return element;
	}
	
	public static org.w3c.dom.Element createLiftingSurfaceSymmetricFlapsElement(
			Document doc, 
			SymmetricFlapCreator flap
			) {
				
		org.w3c.dom.Element element = createXMLElementWithAttributes(
					doc,
					"symmetric_flap",
					Tuple.of("type", flap.getType().toString())	
					);
		JPADStaticWriteUtils.writeSingleNode("inner_station_spanwise_position", flap.getInnerStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_station_spanwise_position", flap.getOuterStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("inner_chord_ratio", flap.getInnerChordRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_chord_ratio", flap.getOuterChordRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("min_deflection", flap.getMinimumDeflection(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("max_deflection", flap.getMaximumDeflection(), element, doc);
		
		return element;
	}
	
	public static org.w3c.dom.Element createLiftingSurfaceSlatsElement(
			Document doc, 
			SlatCreator slat
			) {
				
		org.w3c.dom.Element element = createXMLElementWithAttributes(
					doc,
					"slat"
					);
		JPADStaticWriteUtils.writeSingleNode("min_deflection", slat.getMinimumDeflection(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("max_deflection", slat.getMaximumDeflection(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("inner_chord_ratio", slat.getInnerChordRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_chord_ratio", slat.getOuterChordRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("extension_ratio", slat.getExtensionRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("inner_station_spanwise_position", slat.getInnerStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_station_spanwise_position", slat.getOuterStationSpanwisePosition(), element, doc);
		
		return element;
	}
	
	public static org.w3c.dom.Element createLiftingSurfaceAsymmetricFlapElement(
			Document doc, 
			AsymmetricFlapCreator aileron
			) {
				
		org.w3c.dom.Element element = createXMLElementWithAttributes(
					doc,
					"asymmetric_flap",
					Tuple.of("type", aileron.getType().toString())	
					);
		JPADStaticWriteUtils.writeSingleNode("inner_chord_ratio", aileron.getInnerChordRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_chord_ratio", aileron.getOuterChordRatio(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("inner_station_spanwise_position", aileron.getInnerStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_station_spanwise_position", aileron.getOuterStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("min_deflection", aileron.getMinimumDeflection(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("max_deflection", aileron.getMaximumDeflection(), element, doc);
		
		return element;
	}
	
	public static org.w3c.dom.Element createLiftingSurfaceSpolierElement(
			Document doc, 
			SpoilerCreator spoiler
			) {
				
		org.w3c.dom.Element element = createXMLElementWithAttributes(
					doc,
					"spoiler"
					);
		JPADStaticWriteUtils.writeSingleNode("inner_station_spanwise_position", spoiler.getInnerStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_station_spanwise_position", spoiler.getOuterStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("inner_station_chordwise_position", spoiler.getInnerStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("outer_station_chordwise_position", spoiler.getOuterStationSpanwisePosition(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("min_deflection", spoiler.getMinimumDeflection(), element, doc);
		JPADStaticWriteUtils.writeSingleNode("max_deflection", spoiler.getMaximumDeflection(), element, doc);
		
		return element;
	}
	
}

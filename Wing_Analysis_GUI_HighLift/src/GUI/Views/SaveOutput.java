package GUI.Views;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.swing.JFileChooser;

import org.jscience.physics.amount.Amount;

import Calculator.InputOutputTree;
import Calculator.Reader;
import GUI.Main;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jxl.Workbook;
import jxl.biff.drawing.Chart;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import writers.JPADStaticWriteUtils;

public class SaveOutput {
	
	InputOutputTree theInputOutputTree;
	String outputDirectory;
	Stage thisStage;
	

	File outputFileSVG;
	File outputFilePNGload;
	File outputFilePNGlift;
	File outputFilePNGstallPath;
	File outputFileXML;
	File outputFileXLS;
	
	@FXML
	TextField outputDirText;
	
	@FXML
	CheckBox svg;
	
	@FXML
	CheckBox xml;
	
	@FXML
	CheckBox xls;
	
	@FXML
	CheckBox png;
	
	@FXML
	Button save;

	@FXML
	public void saveFiles() throws IOException, RowsExceededException, WriteException{

		if(svg.isSelected()){
			
	if(outputFileSVG.exists())
		outputFileSVG.delete();
		
	theInputOutputTree.getD3Plotter().saveSVG(outputFileSVG.getAbsolutePath());
		}
		
		if(xml.isSelected()){
			if(outputFileXML.exists())
				outputFileXML.delete();
			Reader theReader = new Reader();
			theReader.writeOutputToXML(theInputOutputTree, outputFileXML.getAbsolutePath());
		}
		
		if(xls.isSelected()){
			saveXLS();
		}
		
		if(png.isSelected()){
			savePNG();
		}
		thisStage.close();
	}
	
	@FXML
	private void chooseOutputFile() throws IOException{
		
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose Output Folder");
		chooser.setInitialDirectory(new File(MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)));
		outputDirectory = chooser.showDialog(null).getAbsolutePath()+File.separator+theInputOutputTree.getInputFile().getName().replaceAll(".xml","" );
		MyConfiguration.createNewFolder(
				outputDirectory
				);
		outputDirText.setText(outputDirectory);

		if (outputDirectory != null) {
		outputFileSVG = new File(outputDirectory + File.separator +  "wingTOPView_"+ theInputOutputTree.getInputFile().getName().replaceAll(".xml",".svg" ));
		outputFilePNGload = new File(outputDirectory + File.separator +  "Load_Distribution_"+ theInputOutputTree.getInputFile().getName().replaceAll(".xml",".png" ));
		outputFilePNGlift = new File(outputDirectory + File.separator +  "Lift_curve_"+ theInputOutputTree.getInputFile().getName().replaceAll(".xml",".png" ));
		outputFilePNGstallPath = new File(outputDirectory + File.separator +  "Stall_Path_" + theInputOutputTree.getInputFile().getName().replaceAll(".xml",".png" ));
		outputFileXML = new File(outputDirectory + File.separator +  "numerical_results_" + theInputOutputTree.getInputFile().getName().replaceAll(".xml",".xml" ));
		outputFileXLS = new File(outputDirectory + File.separator +  "numerical_results_"+ theInputOutputTree.getInputFile().getName().replaceAll(".xml",".xls" ));
		// CHECK IF THE TEXT FIELD IS NOT EMPTY ...
		save.disableProperty().bind(
				Bindings.isEmpty(outputDirText.textProperty())
				);

	}

	}
	
	private void savePNG(){
		
		String outputChartPath = 	MyConfiguration.createNewFolder(
				outputDirectory
				+ File.separator 
				+ "charts"
				+ File.separator
				);
		
		if(theInputOutputTree.getPerformLoadAnalysis() == true){
			
		List<Double[]> xVector = new ArrayList<Double[]>();
		List<Double[]> yVector = new ArrayList<Double[]>();
		List<String> legend  = new ArrayList<>(); 

		for (int i = 0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){
			yVector.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
					theInputOutputTree.getClDistributionCurves().get(i)));
		}
		
		for (int i = 0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){
			xVector.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
					theInputOutputTree.getyAdimensionalDistributionSemiSpan()));
		}
		
		for (int i = 0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){
			legend.add("alpha = " + theInputOutputTree.getAlphaArrayLiftDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE) + "(deg)");
		}
		
		double[][] xMatrix = new double[theInputOutputTree.getAlphaArrayLiftDistribution().size()][theInputOutputTree.getNumberOfPointSemispan()];
		double[][] yMatrix = new double[theInputOutputTree.getAlphaArrayLiftDistribution().size()][theInputOutputTree.getNumberOfPointSemispan()];
		String [] legendString = new String[theInputOutputTree.getAlphaArrayLiftDistribution().size()];
		
		for(int i=0; i<theInputOutputTree.getAlphaArrayLiftDistribution().size(); i++){
			xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVector.get(i));
			yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVector.get(i));
			legendString [i] = legend.get(i);
		}

		
		MyChartToFileUtils.plotNOCSV(
				xMatrix,
				yMatrix, 
				0., 
				1., 
				null, 
				null,
				"\\eta", 
				"C_l",
				"", 
				"", 
				legendString, 
				outputChartPath,
				"Lift_Coefficient_distribution");
		

		}
		
		
		if(theInputOutputTree.getPerformLiftAnalysis() == true){
		
		MyChartToFileUtils.plotNoLegend(
				MyArrayUtils.convertListOfAmountTodoubleArray(theInputOutputTree.getAlphaArrayLiftCurve()),
				MyArrayUtils.convertToDoublePrimitive(theInputOutputTree.getLiftCoefficientCurve()),
				null, 
				null, 
				null, 
				null, 
				"alpha",
				"CL",
				"deg", 
				"",
				outputChartPath,
				"Lift_Coefficient_curve",
				false
				);
		
		}
		
		if(theInputOutputTree.getPerformStallPathAnalysis() == true){
			
		List<Double[]> xVector = new ArrayList<Double[]>();
		List<Double[]> yVector = new ArrayList<Double[]>();
		List<String> legend  = new ArrayList<>(); 

			yVector.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
					theInputOutputTree.getClMaxAirfoils()));
			
			yVector.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
					theInputOutputTree.getClMaxStallPath()));
		
		
		for (int i = 0; i<2; i++){
			xVector.add(MyArrayUtils.convertListOfDoubleToDoubleArray(
					theInputOutputTree.getyAdimensionalDistributionSemiSpan()));
		}
		
		legend.add("cl max airfoils");
		legend.add("cl at alpha = " + theInputOutputTree.getAlphaMaxLinear().doubleValue(NonSI.DEGREE_ANGLE) + "(deg)");

		
		double[][] xMatrix = new double[2][theInputOutputTree.getNumberOfPointSemispan()];
		double[][] yMatrix = new double[2][theInputOutputTree.getNumberOfPointSemispan()];
		
		String [] legendString = new String[theInputOutputTree.getAlphaArrayLiftDistribution().size()];
		
		for(int i=0; i<2; i++){
			xMatrix[i] = MyArrayUtils.convertToDoublePrimitive(xVector.get(i));
			yMatrix[i] = MyArrayUtils.convertToDoublePrimitive(yVector.get(i));
			legendString [i] = legend.get(i);
		}

		
		MyChartToFileUtils.plotNOCSV(
				xMatrix,
				yMatrix, 
				0., 
				1., 
				null, 
				null,
				"\\eta", 
				"C_l",
				"", 
				"", 
				legendString, 
				outputChartPath,
				"Stall_Path");
		

		}
	}
	
	private void saveXLS() throws IOException, RowsExceededException, WriteException{
		
		String outputChartPath = 	MyConfiguration.createNewFolder(
				outputDirectory
				+ File.separator 
				+ "XlsFiles"
				+ File.separator
				);
		
		WritableWorkbook workbook = Workbook.createWorkbook(new File(outputChartPath + File.separator + "Results.xls"));

		// label format
		  WritableFont labelFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
		  labelFont.setColour(Colour.RED);
		  WritableCellFormat labelFormat = new WritableCellFormat(labelFont);
		  
		  //eta station format 
		  WritableFont etaFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
		  etaFont.setColour(Colour.BLUE);
		  WritableCellFormat etaFormat = new WritableCellFormat(etaFont);
		  
		int i=0;
		if(theInputOutputTree.getPerformLoadAnalysis()==true) {
			WritableSheet sheet = workbook.createSheet("Clean Load Analyses", i);
			Label label;
			jxl.write.Number number;
		    
			label = new Label(0, 0, "eta Stations"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
			number = new jxl.write.Number(0, k+1, theInputOutputTree.getyAdimensionalDistributionSemiSpan().get(k));					
			sheet.addCell(number);
			}
			
			for(int j=0; j<theInputOutputTree.getAlphaArrayLiftDistribution().size();j++) {
				label = new Label(j+1, 0, "Cl distribution at alpha " +
			theInputOutputTree.getAlphaArrayLiftDistribution().get(j).doubleValue(NonSI.DEGREE_ANGLE) + 
			" deg"); 
				label.setCellFormat(labelFormat);
				sheet.addCell(label); 
				for(int k=0; k<theInputOutputTree.getClDistributionCurves().get(j).size(); k++) {
				number = new jxl.write.Number(j+1, k+1, theInputOutputTree.getClDistributionCurves().get(j).get(k));
				sheet.addCell(number);
				}
			}
			i++;
		}
		
		if(theInputOutputTree.getPerformLiftAnalysis()==true || theInputOutputTree.getPerformHighLiftAnalysis() == true) {
			
			WritableSheet sheet = workbook.createSheet("Clean Configuration", i);
			Label label;
			jxl.write.Number number;
			 i++;
			 
			label = new Label(0, 0, "alpha wing (deg)"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getAlphaArrayLiftCurve().size(); k++) {
			number = new jxl.write.Number(0, k+1, theInputOutputTree.getAlphaArrayLiftCurve().get(k).doubleValue(NonSI.DEGREE_ANGLE));					
			sheet.addCell(number);
			}
			
			
			label = new Label(1, 0, "CL"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getLiftCoefficientCurve().size(); k++) {
			number = new jxl.write.Number(1, k+1, theInputOutputTree.getLiftCoefficientCurve().get(k));					
			sheet.addCell(number);
			}
			
			if(theInputOutputTree.getPerformStallPathAnalysis()==true) {
				
				label = new Label(4, 0, "eta station"); 
				label.setCellFormat(etaFormat);
				sheet.addCell(label);
				
				for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
					number = new jxl.write.Number(4, k+1, theInputOutputTree.getyAdimensionalDistributionSemiSpan().get(k));					
					sheet.addCell(number);
					}
				
				label = new Label(5, 0, "cl max airfoils"); 
				label.setCellFormat(etaFormat);
				sheet.addCell(label);
				
				for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
					number = new jxl.write.Number(5, k+1, theInputOutputTree.getClMaxAirfoils().get(k));					
					sheet.addCell(number);
					}
				
				label = new Label(6, 0, "cl max distribution"); 
				label.setCellFormat(etaFormat);
				sheet.addCell(label);
				
				for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
					number = new jxl.write.Number(6, k+1, theInputOutputTree.getClMaxStallPath().get(k));					
					sheet.addCell(number);
					}
				
			}
		}
		
		if(theInputOutputTree.getPerformHighLiftAnalysis()==true) {
			WritableSheet sheet = workbook.createSheet("High Lift Configuration", i);
			Label label;
			jxl.write.Number number;
			i++;
			
			label = new Label(0, 0, "alpha wing (deg)"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getAlphaArrayHighLiftCurve().size(); k++) {
			number = new jxl.write.Number(0, k+1, theInputOutputTree.getAlphaArrayHighLiftCurve().get(k).doubleValue(NonSI.DEGREE_ANGLE));					
			sheet.addCell(number);
			}
			
			
			label = new Label(1, 0, "CL"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getAlphaArrayHighLiftCurve().size(); k++) {
			number = new jxl.write.Number(1, k+1, theInputOutputTree.getLiftCoefficient3DCurveHighLift().get(k));					
			sheet.addCell(number);
	
			}
		}
		
		if(theInputOutputTree.getPerformHighLiftDistributionAnalysis() ==true) {
			WritableSheet sheet = workbook.createSheet("High Lift Distribution", i);
			Label label;
			jxl.write.Number number;
			jxl.write.Number numberTwo;
			i++;
			
			label = new Label(0, 0, "eta Stations"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
			number = new jxl.write.Number(0, k+1, theInputOutputTree.getyAdimensionalDistributionSemiSpan().get(k));					
			sheet.addCell(number);
			}
			
			
			label = new Label(1, 0, "Chord Distribution Old"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			
			label = new Label(2, 0, "Chord Distribution New"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
			number = new jxl.write.Number(1, k+1, theInputOutputTree.getChordDistributionSemiSpan().get(k).doubleValue(SI.METER));					
			numberTwo = new jxl.write.Number(2, k+1, theInputOutputTree.getNewChordDistributionMeter()[k]);					
			sheet.addCell(number);
			sheet.addCell(numberTwo);
			}

			label = new Label(3, 0, "Xle Distribution Old"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			
			label = new Label(4, 0, "Xle Distribution New"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
				number = new jxl.write.Number(3, k+1, theInputOutputTree.getxLEDistributionSemiSpan().get(k).doubleValue(SI.METER));					
				numberTwo = new jxl.write.Number(4, k+1, theInputOutputTree.getNewXLEDisributionMeter()[k]);					
				sheet.addCell(number);
				sheet.addCell(numberTwo);
			}
			
			label = new Label(5, 0, "Alpha zero lift Distribution Old"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			
			label = new Label(6, 0, "Alpha zero lift Distribution New"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
			number = new jxl.write.Number(5, k+1, theInputOutputTree.getAlphaZeroLiftDistributionSemiSpan().get(k).doubleValue(NonSI.DEGREE_ANGLE));					
			numberTwo = new jxl.write.Number(6, k+1, theInputOutputTree.getNewAlphaZeroLiftDistributionDeg()[k]);					
			sheet.addCell(number);
			sheet.addCell(numberTwo);
			}
			
			
			for(int j=0; j<theInputOutputTree.getAlphaArrayHighLiftDistribution().size();j++) {
				label = new Label(j+7, 0, "Cl distribution at alpha " +
			theInputOutputTree.getAlphaArrayHighLiftDistribution().get(j).doubleValue(NonSI.DEGREE_ANGLE) + 
			" deg"); 
				label.setCellFormat(labelFormat);
				sheet.addCell(label); 
				for(int k=0; k<theInputOutputTree.getClDistributionCurvesHighLift().get(j).size(); k++) {
				number = new jxl.write.Number(j+7, k+1, theInputOutputTree.getClDistributionCurvesHighLift().get(j).get(k));
				sheet.addCell(number);
				}
			}
			
			label = new Label(7+theInputOutputTree.getAlphaArrayHighLiftDistribution().size(), 0, "Alpha zero lift Distribution New"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			label = new Label(8+theInputOutputTree.getAlphaArrayHighLiftDistribution().size(), 0, "Alpha zero lift Distribution New"); 
			label.setCellFormat(etaFormat);
			sheet.addCell(label);
			
			for(int k=0 ; k<theInputOutputTree.getyAdimensionalDistributionSemiSpan().size(); k++) {
			number = new jxl.write.Number(7+theInputOutputTree.getAlphaArrayHighLiftDistribution().size(), k+1, theInputOutputTree.getAlphaArrayHighLiftDistribution().get(k).doubleValue(NonSI.DEGREE_ANGLE));					
			numberTwo = new jxl.write.Number(8+theInputOutputTree.getAlphaArrayHighLiftDistribution().size(), k+1, theInputOutputTree.getLiftCoefficient3DCurveHighLiftModified().get(k));					
			sheet.addCell(number);
			sheet.addCell(numberTwo);
			}
		}
		
	
		workbook.write();
		workbook.close();
//		Number number = new Number(3, 4, 3.1459); 
//		sheet.addCell(number);
	}
	
	public InputOutputTree getTheInputOutputTree() {
		return theInputOutputTree;
	}

	public void setTheInputOutputTree(InputOutputTree theInputOutputTree) {
		this.theInputOutputTree = theInputOutputTree;
	}

	public Stage getThisStage() {
		return thisStage;
	}

	public void setThisStage(Stage thisStage) {
		this.thisStage = thisStage;
	}


}

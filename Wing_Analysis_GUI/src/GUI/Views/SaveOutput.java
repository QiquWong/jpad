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
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
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
	RadioButton svg;
	
	@FXML
	RadioButton xml;
	
	@FXML
	RadioButton xls;
	
	@FXML
	RadioButton png;
	
	@FXML
	Button save;

	@FXML
	public void saveFiles() throws IOException{

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
		outputDirectory = chooser.showDialog(null).getAbsolutePath();
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

		
		MyChartToFileUtils.plot(
				xMatrix,
				yMatrix, 
				0., 
				1., 
				null, 
				null,
				"$\\eta$", 
				"$C_l$",
				"", 
				"", 
				legendString, 
				outputDirectory + "Charts" + File.separator,
				"Lift_Coefficient_distribution");
		

		}
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

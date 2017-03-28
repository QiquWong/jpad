package sandbox2.vt.postprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javaslang.Tuple;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class PostProcessorExcelController {

    private static final String DEFAULT_SEPARATOR = ",";
	
	private static boolean isPostProcessorInputFile(String pathToAircraftXML) {

		boolean isPostProcessorInputFile = false;
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		String pathToXML = PostProcessorExcelMain.getInputFilePathTextField().getText();
		if(pathToAircraftXML.endsWith(".xml")) {
			File inputFile = new File(pathToAircraftXML);
			if(inputFile.exists()) {
				JPADXmlReader reader = new JPADXmlReader(pathToXML);
				if(reader.getXmlDoc().getElementsByTagName("post_processor_excel_executable").getLength() > 0)
					isPostProcessorInputFile = true;
			}
		}
		// write again
		System.setOut(originalOut);
		
		return isPostProcessorInputFile;
	}
	
	@FXML
	public void chooseXMLInputFile(){
		
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open XML input file");
		chooser.setInitialDirectory(new File(MyConfiguration.getDir(FoldersEnum.INPUT_DIR)));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("XML File","*.xml"));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			PostProcessorExcelMain.getInputFilePathTextField().setText(file.getAbsolutePath());
			PostProcessorExcelMain.setInputFile(file);
			
			// CHECK IF THE TEXT FIELD IS NOT EMPTY ...
			PostProcessorExcelMain.getInputFileLoadButton().disableProperty().bind(
					Bindings.isEmpty(PostProcessorExcelMain.getInputFilePathTextField().textProperty())
					);
			
			// CHECK IF THE FILE IN TEXTFIELD IS A VALID FILE ...
	        final Tooltip warning = new Tooltip("WARNING : The selected file is not an valid input !!");
	        PostProcessorExcelMain.getInputFileLoadButton().setOnMouseEntered(new EventHandler<MouseEvent>() {
	        	
	        	@Override
	        	public void handle(MouseEvent event) {
	        		Point2D p = PostProcessorExcelMain.getInputFileLoadButton()
	        				.localToScreen(
	        						-2.5*PostProcessorExcelMain.getInputFileLoadButton().getLayoutBounds().getMaxX(),
	        						1.2*PostProcessorExcelMain.getInputFileLoadButton().getLayoutBounds().getMaxY()
	        						);
	        		if(!isPostProcessorInputFile(PostProcessorExcelMain.getInputFilePathTextField().getText())
	        				) {
	        			warning.show(PostProcessorExcelMain.getInputFileLoadButton(), p.getX(), p.getY());
	        		}
	        	}
	        });
	        PostProcessorExcelMain.getInputFileLoadButton().setOnMouseExited(new EventHandler<MouseEvent>() {
	        	
	        	@Override
	        	public void handle(MouseEvent event) {
	        		warning.hide();
	        	}
	        });
			
		}
	}
	
	private void chooseCSVFile(TextField csvFileTextField){
		
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open CSV file");
		chooser.setInitialDirectory(new File(MyConfiguration.getDir(FoldersEnum.INPUT_DIR)));
		chooser.getExtensionFilters().addAll(new ExtensionFilter("CSV File","*.csv"));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			csvFileTextField.setText(file.getAbsolutePath());
			PostProcessorExcelMain.getCsvFileList().add(file.getAbsolutePath());
		}
	}
	
	@FXML
	public void activateImportCSVFromFile(){
		
		PostProcessorExcelMain.getInputFilePathLabel().setDisable(false);
		PostProcessorExcelMain.getInputFilePathTextField().setDisable(false);
		PostProcessorExcelMain.getInputFilePathChooser().setDisable(false);
		
		PostProcessorExcelMain.getAddCSVButton().setDisable(true);
		PostProcessorExcelMain.getRemoveCSVButton().disableProperty().bind(
				Bindings.isEmpty(PostProcessorExcelMain.getCsvFileGridPane().getChildren())
				);
		
		PostProcessorExcelMain.getCsvFileGridPane().getChildren().clear();
		PostProcessorExcelMain.getConsoleTextArea().clear();
		PostProcessorExcelMain.getProgressBar().setProgress(0.0);
		PostProcessorExcelMain.getResultLabel().setText("");
		
	}
	
	@FXML
	public void activateManualCSV(){
		
		PostProcessorExcelMain.getInputFilePathLabel().setDisable(true);
		PostProcessorExcelMain.getInputFilePathTextField().setDisable(true);
		PostProcessorExcelMain.getInputFilePathTextField().clear();
		PostProcessorExcelMain.getInputFilePathChooser().setDisable(true);
		
		PostProcessorExcelMain.getAddCSVButton().setDisable(false);
		PostProcessorExcelMain.getRemoveCSVButton().disableProperty().bind(
				Bindings.isEmpty(PostProcessorExcelMain.getCsvFileGridPane().getChildren())
				);
		
		PostProcessorExcelMain.getCsvFileGridPane().getChildren().clear();
		PostProcessorExcelMain.getConsoleTextArea().clear();
	}
	
	@FXML
	public void addInputLine(){
		
		PostProcessorExcelMain.getCsvFileGridPane().getRowConstraints().get(0).setMinHeight(0);
		
		int currentRowsNumber = PostProcessorExcelMain.getCsvFileGridPane().getChildren().size(); 
		if(currentRowsNumber != 0)
			currentRowsNumber = PostProcessorExcelMain.getCsvFileGridPane().getChildren().size()/4;
		
		int holdOnlistSize = PostProcessorExcelMain.getCsvHoldOnList().size();
		
		Label csvFileLabel = new Label("CSV File");
		csvFileLabel.setMinWidth(Region.USE_PREF_SIZE);
		csvFileLabel.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				csvFileLabel,
				0,
				currentRowsNumber
				);
		GridPane.setHalignment(csvFileLabel, HPos.LEFT);
		
		TextField csvFileTextField = new TextField();
		csvFileTextField.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				csvFileTextField,
				1,
				currentRowsNumber
				);
		GridPane.setHalignment(csvFileTextField, HPos.LEFT);
		
		Button choiceButton = new Button("...");
		choiceButton.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				choiceButton,
				2,
				currentRowsNumber
				);
		GridPane.setHalignment(choiceButton, HPos.LEFT);
		
		choiceButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				chooseCSVFile(csvFileTextField);
			}
		});
		
		CheckBox holdOnCheckBox = new CheckBox("Hold on");
		holdOnCheckBox.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				holdOnCheckBox,
				3,
				currentRowsNumber
				);
		GridPane.setHalignment(holdOnCheckBox, HPos.LEFT);
		
		PostProcessorExcelMain.getCsvHoldOnList().add(holdOnlistSize, Boolean.FALSE);
		
		holdOnCheckBox.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				Boolean choice = Boolean.FALSE;

				if(holdOnCheckBox.isSelected()){
					choice = Boolean.TRUE;
				}

				if(!PostProcessorExcelMain.getCsvHoldOnList().isEmpty()
						&& PostProcessorExcelMain.getCsvHoldOnList().size() <= holdOnlistSize
								){
					PostProcessorExcelMain.getCsvHoldOnList().add(holdOnlistSize, choice);
				}
				else if(!PostProcessorExcelMain.getCsvHoldOnList().isEmpty()
						&& PostProcessorExcelMain.getCsvHoldOnList().size() > holdOnlistSize
								){
					PostProcessorExcelMain.getCsvHoldOnList().remove(holdOnlistSize);
					PostProcessorExcelMain.getCsvHoldOnList().add(holdOnlistSize, choice);
				}
			}
			
		});
		
		//.............................................................................
		// BINDING THE RUN BUTTON TO THE CSV FILE TEXTFIELDS ...
		List<TextField> textFields = PostProcessorExcelMain.getCsvFileGridPane().getChildren()
				.stream()
				.filter(x -> GridPane.getColumnIndex(x) == 1)
				.map(x -> (TextField) x)
				.collect(Collectors.toList());
		
		textFields
			.stream()
				.forEach(x -> PostProcessorExcelMain.getRunButton().disableProperty().bind(
							Bindings.isEmpty(x.textProperty())
							)
						);
		
		//.............................................................................
		// CHECK IF ALL THE SELECTED FILES ARE .csv ...
        final Tooltip warning = new Tooltip("WARNING : Some selected file are not a CSV !!");
        PostProcessorExcelMain.getRunButton().setOnMouseEntered(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		Point2D p = PostProcessorExcelMain.getRunButton()
        				.localToScreen(
        						-2.5*PostProcessorExcelMain.getRunButton().getLayoutBounds().getMaxX(),
        						1.2*PostProcessorExcelMain.getRunButton().getLayoutBounds().getMaxY()
        						);
        		
        		if(!PostProcessorExcelMain.getCsvFileGridPane().getChildren()
        				.stream()
        					.filter(x -> GridPane.getColumnIndex(x) == 1)
        						.map(x -> (TextField) x)
        							.allMatch(x -> x.getText().endsWith(".csv"))
        				) {
        			warning.show(PostProcessorExcelMain.getRunButton(), p.getX(), p.getY());
        		}
        	}
        });
        PostProcessorExcelMain.getRunButton().setOnMouseExited(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		warning.hide();
        	}
        });
		
	}
	
	@FXML
	public void removeLine() {
		
		for(int i=0; i<4; i++) 
			PostProcessorExcelMain.getCsvFileGridPane().getChildren().remove(
					PostProcessorExcelMain.getCsvFileGridPane().getChildren().size()-1
					);
		
		if(!PostProcessorExcelMain.getCsvFileList().isEmpty())
			PostProcessorExcelMain.getCsvFileList().remove(
					PostProcessorExcelMain.getCsvFileList().size()-1
					);

		if(!PostProcessorExcelMain.getCsvHoldOnList().isEmpty())
			PostProcessorExcelMain.getCsvHoldOnList().remove(
					PostProcessorExcelMain.getCsvHoldOnList().size()-1
					);
		
		//.............................................................................
		// BINDING THE RUN BUTTON TO THE CSV FILE TEXTFIELDS ...
		List<TextField> textFields = PostProcessorExcelMain.getCsvFileGridPane().getChildren()
				.stream()
				.filter(x -> GridPane.getColumnIndex(x) == 1)
				.map(x -> (TextField) x)
				.collect(Collectors.toList());
		
		textFields
			.stream()
				.forEach(x -> PostProcessorExcelMain.getRunButton().disableProperty().bind(
							Bindings.isEmpty(x.textProperty())
							)
						);
		
	}
	
	@FXML
	public void loadInputFile(){
		
		JPADXmlReader reader = new JPADXmlReader(PostProcessorExcelMain.getInputFile().getAbsolutePath());

		PostProcessorExcelMain.setCsvFileList(MyXMLReaderUtils
				.getXMLPropertiesByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@value")
				);

		PostProcessorExcelMain.setCsvHoldOnList(MyXMLReaderUtils
				.getXMLPropertiesByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@hold_on")
				.stream()
				.map(x -> Boolean.valueOf(x))
				.collect(Collectors.toList())
				); 

		PostProcessorExcelMain.getCsvFileList().stream().forEach( x -> {
			addInputLine();
			PostProcessorExcelMain.getCsvHoldOnList().remove(
					PostProcessorExcelMain.getCsvHoldOnList().size()-1
					);
			});
		
		PostProcessorExcelMain.getCsvFileGridPane().getChildren()
			.stream()
				.filter(x -> GridPane.getColumnIndex(x) == 1)
					.map(x -> (TextField) x)
						.forEach(x -> {
							PostProcessorExcelMain.getCsvFileList().stream().forEach(y -> x.setText(y));
						});
		
		PostProcessorExcelMain.getCsvFileGridPane();
		PostProcessorExcelMain.getCsvFileGridPane().getChildren()
		.stream()
			.filter(x -> GridPane.getColumnIndex(x) == 3)
				.map(x -> (CheckBox) x)
					.forEach(x -> x.setSelected(
							PostProcessorExcelMain.getCsvHoldOnList().get(
									GridPane.getRowIndex(x)
									)
							));
		
		PostProcessorExcelMain.getAddCSVButton().setDisable(false);
		PostProcessorExcelMain.getRemoveCSVButton().disableProperty().bind(
				Bindings.isEmpty(PostProcessorExcelMain.getCsvFileGridPane().getChildren())
				);
		
    }

	@FXML
	public void run() throws IOException {
		
		/******************************************************
		 *  TODO:
		 *  - SEE HOW TO BUILD THE CHART IN THE EXCEL
		 *  - MANAGE THE PROGRESS BAR HERE
		 *  - ADD REFRESH BUTTON AND INCREMENT FILE NAME TAG
		 *  
		 */
		
		PostProcessorExcelMain.getConsoleTextArea().clear();
		
		//.............................................................................
		// Reading the CSV files ...
		PostProcessorExcelMain.getCsvFileList().stream().forEach( x -> { 
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(
						new FileReader(new File(x))
						);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			PostProcessorExcelMain.getCsvFileInfo().add(
					Tuple.of(
							PostProcessorExcelMain.getCsvHoldOnList().get(PostProcessorExcelMain.getCsvFileList().indexOf(x)),
							reader.lines()
							.map(line -> line.trim())
							.map(line -> Arrays.asList(line.split(DEFAULT_SEPARATOR)))
							.collect(Collectors.toList())
							)
					);

			System.out.println("File #" + (PostProcessorExcelMain.getCsvFileList().indexOf(x) + 1) + " succesfully read");
		});

		PostProcessorExcelMain.getProgressBar().setProgress(0.2);
		
		//.............................................................................
		// Creating the Excel files structure ...
		Workbook wb;
		File outputFile = new File(PostProcessorExcelMain.getOutputFileNameWithPathAndExt() + ".xlsx");
		if (outputFile.exists()) { 
		   outputFile.delete();		
		   System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		PostProcessorExcelMain.getProgressBar().setProgress(0.4);
		
		//.............................................................................
		// Managing file to hold on ...
		List<List<List<String>>> csvHoldOnFileText = new ArrayList<>();
		PostProcessorExcelMain.getCsvFileInfo()
		.stream()
			.filter(x -> x._1 == Boolean.TRUE)
				.forEach(x -> csvHoldOnFileText.add(x._2));
		
		createXlsHoldOnSheet(wb, csvHoldOnFileText);
		
		PostProcessorExcelMain.getProgressBar().setProgress(0.6);
		
		//.............................................................................
		// Managing the remaining files ...
		PostProcessorExcelMain.getCsvFileInfo()
		.stream()
			.filter(x -> x._1 == Boolean.FALSE)
				.forEach(x -> createXlsSheet(wb, x._2));
		
		PostProcessorExcelMain.getProgressBar().setProgress(0.8);
		
		//.............................................................................
		// Creating the output file ...
		FileOutputStream fileOut = new FileOutputStream(PostProcessorExcelMain.getOutputFileNameWithPathAndExt() + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
		
		PostProcessorExcelMain.getProgressBar().setProgress(1.0);
		PostProcessorExcelMain.getResultLabel().setText("Done!");
	}
	
	private void createXlsSheet(Workbook wb, List<List<String>> csvFileText) {
		
		Sheet sheet = wb.createSheet();
		
		List<String> xlsColumnDescription = new ArrayList<String>();
		xlsColumnDescription.add(csvFileText.get(0).get(0));
		xlsColumnDescription.add(csvFileText.get(0).get(1));
		
		List<MyArray> xlsDataList = new ArrayList<MyArray>();
		xlsDataList.add(
				new MyArray(
						MyArrayUtils.convertListOfDoubleToDoubleArray(
								csvFileText.stream()
								.filter(x -> csvFileText.indexOf(x) > 0)
								.map(x -> Double.valueOf(x.get(0)))
								.collect(Collectors.toList())
								)
						)
				);
		xlsDataList.add(
				new MyArray(
						MyArrayUtils.convertListOfDoubleToDoubleArray(
								csvFileText.stream()
								.filter(x -> csvFileText.indexOf(x) > 0)
								.map(x -> Double.valueOf(x.get(1)))
								.collect(Collectors.toList())
								)
						)
				);
		
		JPADStaticWriteUtils.writeAllArraysToXls(
				sheet,
				xlsColumnDescription,
				xlsDataList
				);
		
	}
	
	private void createXlsHoldOnSheet(Workbook wb, List<List<List<String>>> csvFileTextList) {		
		
		Sheet sheet = wb.createSheet();
		
		List<String> xlsColumnDescription = new ArrayList<String>();
		csvFileTextList.stream()
			.forEach(x -> xlsColumnDescription.addAll(x.get(0)));
		
		List<MyArray> xlsDataList = new ArrayList<MyArray>();
		csvFileTextList.stream().forEach(x -> {
			xlsDataList.add(
					new MyArray(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									x.stream()
									.filter(s -> x.indexOf(s) > 0)
									.map(s -> Double.valueOf(s.get(0)))
									.collect(Collectors.toList())
									)
							)
					);
			xlsDataList.add(
					new MyArray(
							MyArrayUtils.convertListOfDoubleToDoubleArray(
									x.stream()
									.filter(s -> x.indexOf(s) > 0)
									.map(s -> Double.valueOf(s.get(1)))
									.collect(Collectors.toList())
									)
							)
					);
		});
		
		JPADStaticWriteUtils.writeAllArraysToXls(
				sheet,
				xlsColumnDescription,
				xlsDataList
				);
		
	}
	
	@FXML
	public void zoomConsole() {
		PostProcessorExcelMain.getCoreSplitPane().setDividerPositions(0.5);
	};
	
	@FXML
	public void zoomWorkAera() {
		PostProcessorExcelMain.getCoreSplitPane().setDividerPositions(0.8);
	};
	
}

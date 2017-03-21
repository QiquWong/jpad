package sandbox2.vt.postprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import javafx.beans.binding.Bindings;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class PostProcessorExcelController {

	public static boolean isPostProcessorInputFile(String pathToAircraftXML) {

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
	public void chooserWindow(){
		
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open File");
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
	
	@FXML
	public void activateImportCSVFromFile(){
		
		PostProcessorExcelMain.getInputFilePathLabel().setDisable(false);
		PostProcessorExcelMain.getInputFilePathTextField().setDisable(false);
		PostProcessorExcelMain.getInputFilePathChooser().setDisable(false);
		
		PostProcessorExcelMain.getAddCSVButton().setDisable(true);
		
		PostProcessorExcelMain.getCsvFileGridPane().getChildren().clear();
	}
	
	@FXML
	public void activateManualCSV(){
		
		PostProcessorExcelMain.getInputFilePathLabel().setDisable(true);
		PostProcessorExcelMain.getInputFilePathTextField().setDisable(true);
		PostProcessorExcelMain.getInputFilePathTextField().clear();
		PostProcessorExcelMain.getInputFilePathChooser().setDisable(true);
		
		PostProcessorExcelMain.getAddCSVButton().setDisable(false);
		
		PostProcessorExcelMain.getCsvFileGridPane().getChildren().clear();
		
	}
	
	@FXML
	public void addInputLine(){
		
		int currentRowsNumber = PostProcessorExcelMain.getCsvFileGridPane().getChildren().size(); 
		if(currentRowsNumber != 0)
			currentRowsNumber = PostProcessorExcelMain.getCsvFileGridPane().getChildren().size()/4;
		
		Label csvFileLabel = new Label("CSV File");
		csvFileLabel.setMinWidth(Region.USE_PREF_SIZE);
		csvFileLabel.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				csvFileLabel,
				0,
				currentRowsNumber
				);
		
		TextField csvFileTextField = new TextField();
		csvFileTextField.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				csvFileTextField,
				1,
				currentRowsNumber
				);
		
		Button choiceButton = new Button("...");
		choiceButton.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				choiceButton,
				2,
				currentRowsNumber
				);
		
		CheckBox holdOnCheckBox = new CheckBox("Hold on");
		holdOnCheckBox.setAlignment(Pos.TOP_LEFT);
		PostProcessorExcelMain.getCsvFileGridPane().add(
				holdOnCheckBox,
				3,
				currentRowsNumber
				);
		
		ColumnConstraints constraint1 = new ColumnConstraints();
		constraint1.setPercentWidth(0);
		ColumnConstraints constraint2 = new ColumnConstraints();
		constraint2.setPercentWidth(0);
		ColumnConstraints constraint3 = new ColumnConstraints();
		constraint3.setPercentWidth(0);
		ColumnConstraints constraint4 = new ColumnConstraints();
		constraint4.setPercentWidth(0);
		
		PostProcessorExcelMain.getCsvFileGridPane().getColumnConstraints().addAll(
				constraint1,
				constraint2,
				constraint3,
				constraint4
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

		PostProcessorExcelMain.getCsvFileList().stream().forEach( x -> addInputLine());

//		BufferedReader br = null;
//		String line = "";
//		String cvsSplitBy = ",";
//
//		for(int i=0; i<csvFileListProperty.size(); i++) {
//			try {
//
//				br = new BufferedReader(new FileReader(csvFileListProperty.get(i)));
//				while ((line = br.readLine()) != null) {
//
//					// use comma as separator
//					//
//				    // EXAMPLE:
//					// String[] country = line.split(cvsSplitBy);
//
//					// TODO: COMPLETE ME!!
//
//				}
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				if (br != null) {
//					try {
//						br.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
	}
	
}

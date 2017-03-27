package sandbox2.vt.postprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.apache.xpath.operations.Bool;

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
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class PostProcessorExcelController {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
	
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

		PostProcessorExcelMain.getCsvFileGridPane().getChildren()
			.stream()
				.filter(x -> GridPane.getColumnIndex(x) == 1)
					.map(x -> (TextField) x)
						.forEach(x -> {
							PostProcessorExcelMain.getCsvFileList().stream().forEach(y -> x.setText(y));
						});
		
		
		PostProcessorExcelMain.getCsvHoldOnList()
			.stream()
				.forEach(y -> {
					PostProcessorExcelMain.getCsvFileGridPane().getChildren()
						.stream()
							.filter(x -> GridPane.getColumnIndex(x) == 3)
								.map(x -> (CheckBox) x)
									.forEach(x -> x.setSelected(y));
					});
										
		
    }

	@FXML
	public void run() {
		
		// TODO : BIND THE RUN BUTTON TO THE CSV FILE TEXTFIELDS !!
		
		//.............................................................................
		// Reading the CSV ...
		PostProcessorExcelMain.getCsvFileList().stream().forEach(x -> {
			Scanner scanner = null;
			try {
				scanner = new Scanner(new File(x));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
	        while (scanner.hasNext()) {
	            List<String> line = parseLine(scanner.nextLine());
	            System.out.println(line.get(0) + ", " + line.get(1));
	        }
	        scanner.close();	
		});
		
	}
	
    private static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    @SuppressWarnings("null")
	private static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
		
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

package it.unina.daf.test.postprocessorexcel;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javaslang.Tuple2;

public class PostProcessorExcelMain extends Application {

	/////////////////////////////////////////////////////////
	// VARIABLE DECLARATION:
	private static String outputFileNameWithPath;
	private static File inputFile;
	private static File outputFile;
	private static int runIndex;
	
	private static List<String> csvFileList = new ArrayList<>();
	private static List<Boolean> csvHoldOnList = new ArrayList<>();
	private static List<Tuple2<Boolean, List<List<String>>>> csvFileInfo = new ArrayList<>();
	
	private static Stage primaryStage;
	
	private static BorderPane mainLayout;
	private static BorderPane coreBorderPane;
	private static ToolBar inputModeToolbar;
	private static ToolBar runAndStatusToolbar;
	private static ToolBar coreToolBar;
	private static SplitPane coreSplitPane;
	private static AnchorPane splitPaneUpperAnchorPane;
	private static AnchorPane splitPaneLowerAnchorPane;
	private static ScrollPane anchorPaneScrollPane;
	private static GridPane csvFileGridPane;
	private static TextArea consoleTextArea;
	
	private static Label inputFilePathLabel;
	private static TextField inputFilePathTextField;
	private static Button inputFilePathChooser;
	private static Button inputFileLoadButton;
	private static Button addCSVButton;
	private static Button removeCSVButton;
	private static Button runButton;
	private static Button refreshButton;
	private static Label resultLabel;
	private static ProgressBar progressBar;
	
	/////////////////////////////////////////////////////////
	// START:
	@Override
	public void start(Stage primaryStage) throws Exception {

		PostProcessorExcelMain.primaryStage = primaryStage;
		primaryStage.setTitle("JPADPostProcessorExcel - DAF - UNINA");
		primaryStage.setMinHeight(450);
		primaryStage.setMinWidth(800);
		showHome();
		
		MyConfiguration.initWorkingDirectoryTree(MyConfiguration.currentDirectoryString,
				MyConfiguration.inputDirectory, 
				MyConfiguration.outputDirectory
				);
		
		String outputFolderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + File.separator;
		outputFileNameWithPath = outputFolderPath + "PostProcessorExcel_Output";
	}

	/////////////////////////////////////////////////////////
	// METHODS:
	private void showHome() throws IOException{
			
		FXMLLoader loader = new FXMLLoader();  //Loads an object hierarchy from an XML document
		loader.setLocation(PostProcessorExcelMain.class.getResource("PostProcessorExcel.fxml"));
		mainLayout = loader.load();
		
		//....................................................
		// fetching all the windows components ...
		setInputModeToolbar(
				(ToolBar) getMainLayout().lookup("#inputModeToolbar")
				);
		setCoreBorderPane(
				(BorderPane) getMainLayout().lookup("#coreBorderPane")
				);
		setRunAndStatusToolbar(
				(ToolBar) getMainLayout().lookup("#runAndStatusToolbar")
				);
		setRunButton(
				(Button) getRunAndStatusToolbar().getItems().get(0)
				);
		setProgressBar(
				(ProgressBar) getRunAndStatusToolbar().getItems().get(1)
				);
		setResultLabel(
				(Label) getRunAndStatusToolbar().getItems().get(2)
				);
		setCoreToolBar(
				(ToolBar) getCoreBorderPane().lookup("#coreToolbar")
				);
		setCoreSplitPane(
				(SplitPane) getCoreBorderPane().lookup("#coreSplitPane")
				);
		setSplitPaneUpperAnchorPane(
				(AnchorPane) getCoreSplitPane().getItems().get(0)
				);
		setAnchorPaneScrollPane(
				(ScrollPane) getSplitPaneUpperAnchorPane().getChildren().get(0)
				);
		setSplitPaneLowerAnchorPane(
				(AnchorPane) getCoreSplitPane().getItems().get(1)
				);
		setConsoleTextArea(
				(TextArea) getSplitPaneLowerAnchorPane().getChildren().get(0)
				);
		setCsvFileGridPane(
				(GridPane) getAnchorPaneScrollPane().getContent().lookup("#csvGridPane")
				);
		setInputFilePathLabel(
				(Label) getCoreToolBar().getItems().get(0)
				);
		setInputFilePathTextField(
				(TextField) getCoreToolBar().getItems().get(1)
				);
		setInputFilePathChooser(
				(Button) getCoreToolBar().getItems().get(2)
				);
		setInputFileLoadButton(
				(Button) getCoreToolBar().getItems().get(3)
				);
		setAddCSVButton(
				(Button) getCoreToolBar().getItems().get(4)
				);
		setRemoveCSVButton(
				(Button) getCoreToolBar().getItems().get(5)
				);
		setRefreshButton(
				(Button) getInputModeToolbar().lookup("#refreshButton")
				);
		
		//....................................................
		// Redirecting console output to TextArea ...
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				Platform.runLater(() -> getConsoleTextArea().appendText(String.valueOf((char) b)));
			}
		};
		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
		
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	/////////////////////////////////////////////////////////
	// MAIN:
	public static void main(String[] args) {
		launch(args);
		
	}
	
	/////////////////////////////////////////////////////////
	// GETTERS & SETTERS:
	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		PostProcessorExcelMain.primaryStage = primaryStage;
	}

	public static BorderPane getMainLayout() {
		return mainLayout;
	}

	public static void setMainLayout(BorderPane mainLayout) {
		PostProcessorExcelMain.mainLayout = mainLayout;
	}

	public static GridPane getCsvFileGridPane() {
		return csvFileGridPane;
	}

	public static void setCsvFileGridPane(GridPane csvFileGridPane) {
		PostProcessorExcelMain.csvFileGridPane = csvFileGridPane;
	}

	public static Label getInputFilePathLabel() {
		return inputFilePathLabel;
	}

	public static void setInputFilePathLabel(Label inputFilePathLabel) {
		PostProcessorExcelMain.inputFilePathLabel = inputFilePathLabel;
	}

	public static TextField getInputFilePathTextField() {
		return inputFilePathTextField;
	}

	public static void setInputFilePathTextField(TextField inputFilePathTextField) {
		PostProcessorExcelMain.inputFilePathTextField = inputFilePathTextField;
	}

	public static Button getInputFilePathChooser() {
		return inputFilePathChooser;
	}

	public static void setInputFilePathChooser(Button inputFilePathChooser) {
		PostProcessorExcelMain.inputFilePathChooser = inputFilePathChooser;
	}

	public static Button getAddCSVButton() {
		return addCSVButton;
	}

	public static void setAddCSVButton(Button addCSVButton) {
		PostProcessorExcelMain.addCSVButton = addCSVButton;
	}

	public static Button getInputFileLoadButton() {
		return inputFileLoadButton;
	}

	public static void setInputFileLoadButton(Button inputFileLoadButton) {
		PostProcessorExcelMain.inputFileLoadButton = inputFileLoadButton;
	}

	public static ToolBar getInputModeToolbar() {
		return inputModeToolbar;
	}

	public static void setInputModeToolbar(ToolBar inputModeToolbar) {
		PostProcessorExcelMain.inputModeToolbar = inputModeToolbar;
	}

	public static BorderPane getCoreBorderPane() {
		return coreBorderPane;
	}

	public static void setCoreBorderPane(BorderPane coreBorderPane) {
		PostProcessorExcelMain.coreBorderPane = coreBorderPane;
	}

	public static ToolBar getRunAndStatusToolbar() {
		return runAndStatusToolbar;
	}

	public static void setRunAndStatusToolbar(ToolBar runAndStatusToolbar) {
		PostProcessorExcelMain.runAndStatusToolbar = runAndStatusToolbar;
	}

	public static ToolBar getCoreToolBar() {
		return coreToolBar;
	}

	public static void setCoreToolBar(ToolBar coreToolBar) {
		PostProcessorExcelMain.coreToolBar = coreToolBar;
	}

	public static File getInputFile() {
		return inputFile;
	}

	public static void setInputFile(File inputFile) {
		PostProcessorExcelMain.inputFile = inputFile;
	}

	public static List<String> getCsvFileList() {
		return csvFileList;
	}

	public static void setCsvFileList(List<String> csvFileList) {
		PostProcessorExcelMain.csvFileList = csvFileList;
	}

	public static List<Boolean> getCsvHoldOnList() {
		return csvHoldOnList;
	}

	public static void setCsvHoldOnList(List<Boolean> csvHoldOnList) {
		PostProcessorExcelMain.csvHoldOnList = csvHoldOnList;
	}

	public static SplitPane getCoreSplitPane() {
		return coreSplitPane;
	}

	public static void setCoreSplitPane(SplitPane coreSplitPane) {
		PostProcessorExcelMain.coreSplitPane = coreSplitPane;
	}

	public static AnchorPane getSplitPaneUpperAnchorPane() {
		return splitPaneUpperAnchorPane;
	}

	public static void setSplitPaneUpperAnchorPane(AnchorPane splitPaneAnchorPane) {
		PostProcessorExcelMain.splitPaneUpperAnchorPane = splitPaneAnchorPane;
	}

	public static ScrollPane getAnchorPaneScrollPane() {
		return anchorPaneScrollPane;
	}

	public static void setAnchorPaneScrollPane(ScrollPane anchorPaneScrollPane) {
		PostProcessorExcelMain.anchorPaneScrollPane = anchorPaneScrollPane;
	}

	public static TextArea getConsoleTextArea() {
		return consoleTextArea;
	}

	public static void setConsoleTextArea(TextArea consoleTextArea) {
		PostProcessorExcelMain.consoleTextArea = consoleTextArea;
	}

	public static AnchorPane getSplitPaneLowerAnchorPane() {
		return splitPaneLowerAnchorPane;
	}

	public static void setSplitPaneLowerAnchorPane(AnchorPane splitPaneLowerAnchorPane) {
		PostProcessorExcelMain.splitPaneLowerAnchorPane = splitPaneLowerAnchorPane;
	}

	public static Button getRunButton() {
		return runButton;
	}

	public static void setRunButton(Button runButton) {
		PostProcessorExcelMain.runButton = runButton;
	}

	public static List<Tuple2<Boolean, List<List<String>>>> getCsvFileInfo() {
		return csvFileInfo;
	}

	public static void setCsvFileInfo(List<Tuple2<Boolean, List<List<String>>>> csvFileInfo) {
		PostProcessorExcelMain.csvFileInfo = csvFileInfo;
	}

	public static String getOutputFileNameWithPath() {
		return outputFileNameWithPath;
	}

	public static void setOutputFileNameWithPath(String outputFileNameWithPath) {
		PostProcessorExcelMain.outputFileNameWithPath = outputFileNameWithPath;
	}
	
	public static Button getRemoveCSVButton() {
		return removeCSVButton;
	}

	public static void setRemoveCSVButton(Button removeCSVButton) {
		PostProcessorExcelMain.removeCSVButton = removeCSVButton;
	}

	public static ProgressBar getProgressBar() {
		return progressBar;
	}

	public static void setProgressBar(ProgressBar progressBar) {
		PostProcessorExcelMain.progressBar = progressBar;
	}

	public static Label getResultLabel() {
		return resultLabel;
	}

	public static void setResultLabel(Label resultLabel) {
		PostProcessorExcelMain.resultLabel = resultLabel;
	}

	public static int getRunIndex() {
		return runIndex;
	}

	public static void setRunIndex(int runIndex) {
		PostProcessorExcelMain.runIndex = runIndex;
	}

	public static Button getRefreshButton() {
		return refreshButton;
	}

	public static void setRefreshButton(Button refreshButton) {
		PostProcessorExcelMain.refreshButton = refreshButton;
	}

	public static File getOutputFile() {
		return outputFile;
	}

	public static void setOutputFile(File outputFile) {
		PostProcessorExcelMain.outputFile = outputFile;
	}

}

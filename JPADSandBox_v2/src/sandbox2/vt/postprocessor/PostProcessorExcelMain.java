package sandbox2.vt.postprocessor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import configuration.MyConfiguration;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class PostProcessorExcelMain extends Application {

	/////////////////////////////////////////////////////////
	// VARIABLE DECLARATION:
	private static File inputFile;
	
	private static List<String> csvFileList;
	private static List<Boolean> csvHoldOnList;
	
	private static Stage primaryStage;
	
	private static BorderPane mainLayout;
	private static BorderPane coreBorderPane;
	private static ToolBar inputModeToolbar;
	private static ToolBar runAndStatusToolbar;
	private static ToolBar coreToolBar;
	private static ScrollPane coreScrollPane;
	private static GridPane csvFileGridPane;
	
	private static Label inputFilePathLabel;
	private static TextField inputFilePathTextField;
	private static Button inputFilePathChooser;
	private static Button inputFileLoadButton;
	private static Button addCSVButton;
	
	/////////////////////////////////////////////////////////
	// START:
	@Override
	public void start(Stage primaryStage) throws Exception {

		PostProcessorExcelMain.primaryStage = primaryStage;
		primaryStage.setTitle("JPADPostProcessorExcel - DAF - UNINA");
		primaryStage.setMinHeight(450);
		primaryStage.setMinWidth(800);
		showHome();
		
		MyConfiguration.initWorkingDirectoryTree();
		
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
		setCoreToolBar(
				(ToolBar) getCoreBorderPane().lookup("#coreToolbar")
				);
		setCoreScrollPane(
				(ScrollPane) getCoreBorderPane().lookup("#coreScrollPane")
				);
		setCsvFileGridPane(
				(GridPane) getCoreScrollPane().getContent().lookup("#csvGridPane")
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
		
		//....................................................
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

	public static ScrollPane getCoreScrollPane() {
		return coreScrollPane;
	}

	public static void setCoreScrollPane(ScrollPane coreScrollPane) {
		PostProcessorExcelMain.coreScrollPane = coreScrollPane;
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

}

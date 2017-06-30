package jpadcommander.inputmanager;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import jpadcommander.Main;

public class InputManagerController {
	
	ObservableList<String> aircraftTypeList = FXCollections.observableArrayList(
			"JET",
			"FIGHTER",
			"BUSINESS_JET",
			"TURBOPROP", 
			"GENERAL_AVIATION",
			"COMMUTER",
			"ACROBATIC"
			);
	ObservableList<String> regulationsTypeList = FXCollections.observableArrayList(
			"FAR-23",
			"FAR-25"
			);
	ObservableList<String> windshieldTypeList = FXCollections.observableArrayList(
			"DOUBLE",
			"FLAT_FLUSH",
			"FLAT_PROTRUDING",
			"SINGLE_ROUND",
			"SINGLE_SHARP"
			);
	ObservableList<String> powerPlantMountingPositionTypeList = FXCollections.observableArrayList(
			"BURIED",
			"WING",
			"AFT_FUSELAGE",
			"HTAIL",
			"REAR_FUSELAGE"
			);
	ObservableList<String> nacelleMountingPositionTypeList = FXCollections.observableArrayList(
			"WING",
			"FUSELAGE",
			"HTAIL",
			"UNDERCARRIAGE_HOUSING"
			);
	ObservableList<String> landingGearsMountingPositionTypeList = FXCollections.observableArrayList(
			"FUSELAGE",
			"WING",
			"NACELLE"
			);
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox aircraftTypeChioseBox;
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox regulationsTypeChioseBox;
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox windshieldTypeChoiceBox;
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox;
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox;
	
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox landingGearsMountingPositionTypeChoiceBox;
	
	@FXML
	@SuppressWarnings("unchecked")
	private void initialize() {
		aircraftTypeChioseBox.setItems(aircraftTypeList);
		regulationsTypeChioseBox.setItems(regulationsTypeList);
		windshieldTypeChoiceBox.setItems(windshieldTypeList);
		powerPlantMountingPositionTypeChoiceBox.setItems(powerPlantMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox.setItems(nacelleMountingPositionTypeList);
		landingGearsMountingPositionTypeChoiceBox.setItems(landingGearsMountingPositionTypeList);
	}
	
	@FXML
	private void showInputManagerAircraftFromFileContent() throws IOException {
		
		//.......................................................................................
		// AIRCRAFT TAB FILEDS CAPTURE
		//.......................................................................................
		// get the content of Input-Aircraft-From-File
		Main.setMainInputManagerAircraftSubContentFieldsLayout(
				(BorderPane) Main.getMainInputManagerLayout().lookup("#mainInputManagerAircraftSubContentFields")
				);
		
		// get the pane of the front view
		Main.setAircraftFrontViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#FrontViewPane")
				);
		
		// get the pane of the side view
		Main.setAircraftSideViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#SideViewPane")
				);
		
		// get the pane of the top view
		Main.setAircraftTopViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#TopViewPane")
				);
		
		Main.showInputManagerAircraftFromFile();
		
		// get the text field for aircraft input file name
		Main.setTextFieldAircraftInputFile(
				(TextField) Main.getMainInputManagerAircraftFromFileToolbarLayout()
								.getItems()
									.get(0)
										.lookup("#textFieldAircraftInputFile")
				);
		
		// set the aircraft input file path in the input text filed if aircraft != null
		if(Main.getTheAircraft() != null) {
			Main.setTextFieldAircraftInputFile(
					(TextField) Main.getMainInputManagerAircraftFromFileToolbarLayout()
									.getItems()
										.get(0)
											.lookup("#textFieldAircraftInputFile")
					);
			Main.getTextFieldAircraftInputFile().setText(
					Main.getInputFileAbsolutePath()
					);
		}
		
		// get the load button from file
		Main.setLoadButtonFromFile(
				(Button) Main.getMainInputManagerAircraftFromFileToolbarLayout()
								.getItems()
									.get(0)
										.lookup("#loadButton")
				);
		
		// CHECK IF THE TEXT FIELD IS NOT EMPTY
		Main.getLoadButtonFromFile().disableProperty().bind(
				Bindings.isEmpty(Main.getTextFieldAircraftInputFile().textProperty())
				);
		
		// CHECK IF THE FILE IN TEXTFIELD IS AN AIRCRAFT
        final Tooltip warning = new Tooltip("WARNING : The selected file is not an aircraft !!");
        Main.getLoadButtonFromFile().setOnMouseEntered(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		Point2D p = Main.getLoadButtonFromFile()
        				.localToScreen(
        						-2.5*Main.getLoadButtonFromFile().getLayoutBounds().getMaxX(),
        						1.2*Main.getLoadButtonFromFile().getLayoutBounds().getMaxY()
        						);
        		if(!Main.isAircraftFile(Main.getTextFieldAircraftInputFile().getText())
        				) {
        			warning.show(Main.getLoadButtonFromFile(), p.getX(), p.getY());
        		}
        	}
        });
        Main.getLoadButtonFromFile().setOnMouseExited(new EventHandler<MouseEvent>() {
        	
        	@Override
        	public void handle(MouseEvent event) {
        		warning.hide();
        	}
        });
 
		////////////////////////////////////////////////////////////////////////////
		// TODO : SET TEXT FIELD INPUT FILE IF AIRCRAFT NOT NULL 				  //
        //		  CHECK IF AIRCRAFT IS FROM DEFAULT --> THEN CLEAR THE TEXT FIELD //
		////////////////////////////////////////////////////////////////////////////
        
		//.......................................................................................
		// FUSELAGE TAB
		//.......................................................................................

        // get the pane of the front view
		Main.setFuselageFrontViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageFrontViewPane")
				);
		
		// get the pane of the side view
		Main.setFuselageSideViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageSideViewPane")
				);
		
		// get the pane of the top view
		Main.setFuselageTopViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageTopViewPane")
				);
		
	}

	@SuppressWarnings("unchecked")
	@FXML
	private void showInputManagerAircraftDefaultContent() throws IOException {
		
		//.......................................................................................
		// AIRCRAFT TAB
		//.......................................................................................
		
		// get the content of Default-Aircraft
		Main.setMainInputManagerAircraftSubContentFieldsLayout(
				(BorderPane) Main.getMainInputManagerLayout().lookup("#mainInputManagerAircraftSubContentFields")
				);

		// get the pane of the front view
		Main.setAircraftFrontViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#FrontViewPane")
				);
		
		// get the pane of the side view
		Main.setAircraftSideViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#SideViewPane")
				);
		
		// get the pane of the top view
		Main.setAircraftTopViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#TopViewPane")
				);
		
		Main.showInputManagerAircraftDefault();
		
		// get the choice box for the default aircraft
		Main.setDefaultAircraftChoiseBox(
				(ChoiceBox<String>) Main.getMainInputManagerAircraftDefaultToolbarLayout()
								.getItems()
									.get(0)
										.lookup("#defaultAircraftChoiseBox")
				);
		
		// get the load button from file
		Main.setLoadButtonDefaultAircraft(
				(Button) Main.getMainInputManagerAircraftDefaultToolbarLayout()
								.getItems()
									.get(0)
										.lookup("#loadButtonDefaultAircraft")
				);
		
		// set the aircraft input file path in the input text filed if aircraft != null
		if(Main.getTheAircraft() != null) {
			Main.setDefaultAircraftChoiseBox(
					(ChoiceBox<String>) Main.getMainInputManagerAircraftDefaultToolbarLayout()
									.getItems()
										.get(0)
											.lookup("#defaultAircraftChoiseBox")
					);
			if(Main.getChoiceBoxSelectionDefaultAircraft() != null) {
				if(Main.getChoiceBoxSelectionDefaultAircraft().equals("ATR-72"))
					Main.getDefaultAircraftChoiceBox().getSelectionModel().select(0);
				else if(Main.getChoiceBoxSelectionDefaultAircraft().equals("B747-100B"))		
					Main.getDefaultAircraftChoiceBox().getSelectionModel().select(1);
				else if(Main.getChoiceBoxSelectionDefaultAircraft().equals("AGILE-DC1"))
					Main.getDefaultAircraftChoiceBox().getSelectionModel().select(2);
			}
		}

		// CHECK IF NO CHOICE BOX ITEM HAS BEEN SELECTED 
		Main.getLoadButtonDefaultAircraft().disableProperty().bind(
				Main.getDefaultAircraftChoiceBox().valueProperty().isNull()
				);
		
		//.......................................................................................
		// FUSELAGE TAB
		//.......................................................................................
		
        // get the pane of the front view
		Main.setFuselageFrontViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageFrontViewPane")
				);
		
		// get the pane of the side view
		Main.setFuselageSideViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageSideViewPane")
				);
		
		// get the pane of the top view
		Main.setFuselageTopViewPane(
				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageTopViewPane")
				);
	}

	@FXML
	private void zoomDataLogAndMessages() {
		
		Main.setAircraftViewsAndDataLogSplitPane(
				(SplitPane) Main.getMainInputManagerLayout().lookup("#aircraftViewsAndDataLogSplitPane")
				);
		
		Main.getAircraftViewsAndDataLogSplitPane().setDividerPositions(0.5);
		
	};
	

	@FXML
	private void zoomViews() {
		
		Main.setAircraftViewsAndDataLogSplitPane(
				(SplitPane) Main.getMainInputManagerLayout().lookup("#aircraftViewsAndDataLogSplitPane")
				);
		
		Main.getAircraftViewsAndDataLogSplitPane().setDividerPositions(0.9);
		
	};
	
	@FXML
	private void zoomDataLogAndMessagesFuselage() {
		
		Main.setFuselageViewsAndDataLogSplitPane(
				(SplitPane) Main.getMainInputManagerLayout().lookup("#fuselageViewsAndDataLogSplitPane")
				);
		
		Main.getFuselageViewsAndDataLogSplitPane().setDividerPositions(0.5);
		
	};
	

	@FXML
	private void zoomViewsFuselage() {
		
		Main.setFuselageViewsAndDataLogSplitPane(
				(SplitPane) Main.getMainInputManagerLayout().lookup("#fuselageViewsAndDataLogSplitPane")
				);
		
		Main.getFuselageViewsAndDataLogSplitPane().setDividerPositions(0.9);
		
	};
}

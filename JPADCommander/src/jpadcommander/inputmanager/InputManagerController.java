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
	ObservableList<String> fuselageAdjustCriteriaTypeList = FXCollections.observableArrayList(
			"ADJ_TOT_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS",
			"ADJ_TOT_LENGTH_CONST_FINENESS_RATIOS",	
			"ADJ_CYL_LENGTH",                            
			"ADJ_NOSE_LENGTH_CONST_TOT_LENGTH_DIAMETERS",
			"ADJ_NOSE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS",
			"ADJ_NOSE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS",
			"ADJ_TAILCONE_LENGTH_CONST_TOT_LENGTH_DIAMETERS",
			"ADJ_TAILCONE_LENGTH_CONST_LENGTH_RATIOS_DIAMETERS",
			"ADJ_TAILCONE_LENGTH_CONST_FINENESS_RATIOS_VAR_LENGTHS",
			"ADJ_FUS_LENGTH_CONST_FINENESS_RATIOS_VAR_DIAMETERS"
			);
	ObservableList<String> wingAdjustCriteriaTypeList = FXCollections.observableArrayList(
			"AR_SPAN_ROOTCHORD",
			"AR_SPAN_TIPCHORD",
			"AR_SPAN_TAPER",
			"AR_AREA_ROOTCHORD",
			"AR_AREA_TIPCHORD",
			"AR_AREA_TAPER", 
			"AR_ROOTCHORD_TIPCHORD",
			"AR_ROOTCHORD_TAPER",
			"AR_TIPCHORD_TAPER",
			"SPAN_AREA_ROOTCHORD",
			"SPAN_AREA_TIPCHORD",
			"SPAN_AREA_TAPER",
			"SPAN_ROOTCHORD_TIPCHORD", 
			"SPAN_ROOTCHORD_TAPER",
			"SPAN_TIPCHORD_TAPER",
			"AREA_ROOTCHORD_TIPCHORD",
			"AREA_ROOTCHORD_TAPER",
			"AREA_TIPCHORD_TAPER"
			);
	ObservableList<String> lengthUnitsList = FXCollections.observableArrayList("m","ft" );
	ObservableList<String> angleUnitsList = FXCollections.observableArrayList("°","rad" );
	
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
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox1;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox2;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox3;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox4;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox5;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantMountingPositionTypeChoiceBox6;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox1;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox2;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox3;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox4;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox5;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacellesMountingPositionTypeChoiceBox6;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox landingGearsMountingPositionTypeChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox wingXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox wingYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox wingZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox wingRiggingAngleUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox hTailXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox hTailYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox htailZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox hTailRiggingAngleUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox vTailXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox vTailYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox vTailZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox vTailRiggingAngleUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox canardXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox canardYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox canardZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox canardRiggingAngleUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox powerPlantTiltAngleUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacelleXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacelleYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox nacelleZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox landingGearsXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox landingGearsYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox landingGearsZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox systemsXUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox systemsYUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox systemsZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageLengthUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageNoseTipOffsetZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageWindshieldWidthUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageWindshieldHeightUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageCylinderSectionWidthUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageCylinderSectionHeightUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageHeightFromGroundUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageTailTipOffsetZUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageSpoilersXinUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageSpoilersXoutUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageSpoilersYinUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageSpoilersYoutUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageSpoilersDeltaMinUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageSpoilersDeltaMaxUnitChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox fuselageAdjustCriterionChoiceBox;
	@FXML
	@SuppressWarnings("rawtypes")
	private ChoiceBox wingAdjustCriterionChoiceBox;
	
	@FXML
	@SuppressWarnings("unchecked")
	private void initialize() {
		aircraftTypeChioseBox.setItems(aircraftTypeList);
		regulationsTypeChioseBox.setItems(regulationsTypeList);
		windshieldTypeChoiceBox.setItems(windshieldTypeList);
		powerPlantMountingPositionTypeChoiceBox1.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox2.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox3.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox4.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox5.setItems(powerPlantMountingPositionTypeList);
		powerPlantMountingPositionTypeChoiceBox6.setItems(powerPlantMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox1.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox2.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox3.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox4.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox5.setItems(nacelleMountingPositionTypeList);
		nacellesMountingPositionTypeChoiceBox6.setItems(nacelleMountingPositionTypeList);
		landingGearsMountingPositionTypeChoiceBox.setItems(landingGearsMountingPositionTypeList);
		fuselageAdjustCriterionChoiceBox.setItems(fuselageAdjustCriteriaTypeList);
//		wingAdjustCriterionChoiceBox.setItems(wingAdjustCriteriaTypeList);
		
		// Units 
		fuselageXUnitChoiceBox.setItems(lengthUnitsList);
		fuselageYUnitChoiceBox.setItems(lengthUnitsList);
		fuselageZUnitChoiceBox.setItems(lengthUnitsList);
		wingXUnitChoiceBox.setItems(lengthUnitsList);
		wingYUnitChoiceBox.setItems(lengthUnitsList);
		wingZUnitChoiceBox.setItems(lengthUnitsList);
		wingRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		hTailXUnitChoiceBox.setItems(lengthUnitsList);
		hTailYUnitChoiceBox.setItems(lengthUnitsList);
		htailZUnitChoiceBox.setItems(lengthUnitsList);
		hTailRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		vTailXUnitChoiceBox.setItems(lengthUnitsList);
		vTailYUnitChoiceBox.setItems(lengthUnitsList);
		vTailZUnitChoiceBox.setItems(lengthUnitsList);
		vTailRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		canardXUnitChoiceBox.setItems(lengthUnitsList);
		canardYUnitChoiceBox.setItems(lengthUnitsList);
		canardZUnitChoiceBox.setItems(lengthUnitsList);
		canardRiggingAngleUnitChoiceBox.setItems(angleUnitsList);
		powerPlantXUnitChoiceBox.setItems(lengthUnitsList);
		powerPlantYUnitChoiceBox.setItems(lengthUnitsList);
		powerPlantZUnitChoiceBox.setItems(lengthUnitsList);
		powerPlantTiltAngleUnitChoiceBox.setItems(angleUnitsList);
		nacelleXUnitChoiceBox.setItems(lengthUnitsList);
		nacelleYUnitChoiceBox.setItems(lengthUnitsList);
		nacelleZUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsXUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsYUnitChoiceBox.setItems(lengthUnitsList);
		landingGearsZUnitChoiceBox.setItems(lengthUnitsList);
		systemsXUnitChoiceBox.setItems(lengthUnitsList);
		systemsYUnitChoiceBox.setItems(lengthUnitsList);
		systemsZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageLengthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageNoseTipOffsetZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageWindshieldWidthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageWindshieldHeightUnitChoiceBox.setItems(lengthUnitsList);
		fuselageCylinderSectionWidthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageCylinderSectionHeightUnitChoiceBox.setItems(lengthUnitsList);
		fuselageHeightFromGroundUnitChoiceBox.setItems(lengthUnitsList);
		fuselageTailTipOffsetZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersXinUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersXoutUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersYinUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersYoutUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersDeltaMinUnitChoiceBox.setItems(angleUnitsList);
		fuselageSpoilersDeltaMaxUnitChoiceBox.setItems(angleUnitsList);
	}
	
	@FXML
	private void showInputManagerAircraftFromFileContent() throws IOException {
		
		Main.setIsAircraftFormFile(Boolean.TRUE);
		
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
		
		Main.setIsAircraftFormFile(Boolean.FALSE);
		
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

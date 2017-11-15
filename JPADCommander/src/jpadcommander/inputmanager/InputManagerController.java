package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.treez.javafxd3.d3.svg.SymbolType;
import org.treez.javafxd3.javafx.JavaFxD3Browser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.WindshieldTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import graphics.D3Plotter;
import graphics.D3PlotterOptions;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jpadcommander.Main;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

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
			"NONE",
			"ADJUST TOTAL LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"ADJUST TOTAL LENGTH, CONSTANT FINENESS-RATIOS",	
			"ADJUST CYLINDER LENGTH (streching)",                            
			"ADJUST NOSE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS",
			"ADJUST NOSE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"ADJUST NOSE LENGTH, CONSTANT FINENESS-RATIOS",
			"ADJUST TAILCONE LENGTH, CONSTANT TOTAL LENGTH AND DIAMETERS",
			"ADJUST TAILCONE LENGTH, CONSTANT LENGTH-RATIOS AND DIAMETERS",
			"ADJUST TAILCONE LENGTH, CONSTANT FINENESS-RATIOS",
			"ADJUST FUSELAGE LENGTH, CONSTANT FINENESS-RATIOS"
			);
	ObservableList<String> wingAdjustCriteriaTypeList = FXCollections.observableArrayList(
			"NONE",
			"FIXED AR, SPAN AND ROOT CHORD",
			"FIXED AR, SPAN AND TIP CHORD",
			"FIXED AR, SPAN AND TAPER-RATIO",
			"FIXED AR, AREA AND ROOT CHORD",
			"FIXED AR, AREA AND TIP CHORD",
			"FIXED AR, AREA AND TAPER-RATIO", 
			"FIXED AR, ROOT CHORD AND TIP CHORD",
			"FIXED AR, ROOT CHORD AND TAPER-RATIO",
			"FIXED AR, TIP CHORD AND TAPER-RATIO",
			"FIXED SPAN, AREA AND ROOT CHORD",
			"FIXED SPAN, AREA AND TIP CHORD",
			"FIXED SPAN, AREA AND TAPER-RATIO",
			"FIXED SPAN, ROOT CHORD AND TIP CHORD", 
			"FIXED SPAN, ROOT CHORD AND TAPER-RATIO",
			"FIXED SPAN, TIP CHORD AND TAPER-RATIO",
			"FIXED AREA, ROOT CHORD AND TIP CHORD",
			"FIXED AREA, ROOT CHORD AND TAPER-RATIO",
			"FIXED AREA, TIP CHORD AND TAPER-RATIO"
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
	private ChoiceBox fuselageRoughnessUnitChoiceBox;
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
		fuselageRoughnessUnitChoiceBox.setItems(lengthUnitsList);
		fuselageNoseTipOffsetZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageWindshieldWidthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageWindshieldHeightUnitChoiceBox.setItems(lengthUnitsList);
		fuselageCylinderSectionWidthUnitChoiceBox.setItems(lengthUnitsList);
		fuselageCylinderSectionHeightUnitChoiceBox.setItems(lengthUnitsList);
		fuselageHeightFromGroundUnitChoiceBox.setItems(lengthUnitsList);
		fuselageTailTipOffsetZUnitChoiceBox.setItems(lengthUnitsList);
		fuselageSpoilersDeltaMinUnitChoiceBox.setItems(angleUnitsList);
		fuselageSpoilersDeltaMaxUnitChoiceBox.setItems(angleUnitsList);
	}
	
//	@FXML
//	private void showInputManagerAircraftFromFileContent() throws IOException {
//		
//		Main.setIsAircraftFormFile(Boolean.TRUE);
//		Main.getProgressBar().setProgress(0.0);
//		Main.getStatusBar().setText("Ready!");
//		
//		//.......................................................................................
//		// AIRCRAFT TAB FILEDS CAPTURE
//		//.......................................................................................
//		// get the content of Input-Aircraft-From-File
//		Main.setMainInputManagerAircraftSubContentFieldsLayout(
//				(BorderPane) Main.getMainInputManagerLayout().lookup("#mainInputManagerAircraftSubContentFields")
//				);
//		
//		// get the pane of the front view
//		Main.setAircraftFrontViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#FrontViewPane")
//				);
//		
//		// get the pane of the side view
//		Main.setAircraftSideViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#SideViewPane")
//				);
//		
//		// get the pane of the top view
//		Main.setAircraftTopViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#TopViewPane")
//				);
//		
////		Main.showInputManagerAircraftFromFile();
////		
////		// get the text field for aircraft input file name
////		Main.setTextFieldAircraftInputFile(
////				(TextField) Main.getMainInputManagerAircraftFromFileToolbarLayout()
////								.getItems()
////									.get(0)
////										.lookup("#textFieldAircraftInputFile")
////				);
//		
//		// set the aircraft input file path in the input text filed if aircraft != null
////		if(Main.getTheAircraft() != null) {
////			Main.setTextFieldAircraftInputFile(
////					(TextField) Main.getMainInputManagerAircraftFromFileToolbarLayout()
////									.getItems()
////										.get(0)
////											.lookup("#textFieldAircraftInputFile")
////					);
////			Main.getTextFieldAircraftInputFile().setText(
////					Main.getInputFileAbsolutePath()
////					);
////		}
//		
//		// get the load button from file
//		Main.setLoadButtonFromFile(
//				(Button) Main.getMainInputManagerAircraftFromFileToolbarLayout()
//								.getItems()
//									.get(0)
//										.lookup("#loadButton")
//				);
//		
//		// CHECK IF THE TEXT FIELD IS NOT EMPTY
//		Main.getLoadButtonFromFile().disableProperty().bind(
//				Bindings.isEmpty(Main.getTextFieldAircraftInputFile().textProperty())
//				);
//		
//		// CHECK IF THE FILE IN TEXTFIELD IS AN AIRCRAFT
//        final Tooltip warning = new Tooltip("WARNING : The selected file is not an aircraft !!");
//        Main.getLoadButtonFromFile().setOnMouseEntered(new EventHandler<MouseEvent>() {
//        	
//        	@Override
//        	public void handle(MouseEvent event) {
//        		Point2D p = Main.getLoadButtonFromFile()
//        				.localToScreen(
//        						-2.5*Main.getLoadButtonFromFile().getLayoutBounds().getMaxX(),
//        						1.2*Main.getLoadButtonFromFile().getLayoutBounds().getMaxY()
//        						);
//        		if(!Main.isAircraftFile(Main.getTextFieldAircraftInputFile().getText())
//        				) {
//        			warning.show(Main.getLoadButtonFromFile(), p.getX(), p.getY());
//        		}
//        	}
//        });
//        Main.getLoadButtonFromFile().setOnMouseExited(new EventHandler<MouseEvent>() {
//        	
//        	@Override
//        	public void handle(MouseEvent event) {
//        		warning.hide();
//        	}
//        });
// 
//		//.......................................................................................
//		// FUSELAGE TAB
//		//.......................................................................................
//
//        // get the pane of the front view
//		Main.setFuselageFrontViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageFrontViewPane")
//				);
//		
//		// get the pane of the side view
//		Main.setFuselageSideViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageSideViewPane")
//				);
//		
//		// get the pane of the top view
//		Main.setFuselageTopViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageTopViewPane")
//				);
//		
//	}

//	@SuppressWarnings("unchecked")
//	@FXML
//	private void showInputManagerAircraftDefaultContent() throws IOException {
//		
//		Main.setIsAircraftFormFile(Boolean.FALSE);
//		Main.getProgressBar().setProgress(0.0);
//		Main.getStatusBar().setText("Ready!");
//		
//		//.......................................................................................
//		// AIRCRAFT TAB
//		//.......................................................................................
//		
//		// get the content of Default-Aircraft
//		Main.setMainInputManagerAircraftSubContentFieldsLayout(
//				(BorderPane) Main.getMainInputManagerLayout().lookup("#mainInputManagerAircraftSubContentFields")
//				);
//
//		// get the pane of the front view
//		Main.setAircraftFrontViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#FrontViewPane")
//				);
//		
//		// get the pane of the side view
//		Main.setAircraftSideViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#SideViewPane")
//				);
//		
//		// get the pane of the top view
//		Main.setAircraftTopViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#TopViewPane")
//				);
//		
//		Main.showInputManagerAircraftDefault();
//		
//		// get the choice box for the default aircraft
//		Main.setDefaultAircraftChoiseBox(
//				(ChoiceBox<String>) Main.getMainInputManagerAircraftDefaultToolbarLayout()
//								.getItems()
//									.get(0)
//										.lookup("#defaultAircraftChoiseBox")
//				);
//		
//		// get the load button from file
//		Main.setLoadButtonDefaultAircraft(
//				(Button) Main.getMainInputManagerAircraftDefaultToolbarLayout()
//								.getItems()
//									.get(0)
//										.lookup("#loadButtonDefaultAircraft")
//				);
//		
//		// set the aircraft input file path in the input text filed if aircraft != null
//		if(Main.getTheAircraft() != null) {
//			Main.setDefaultAircraftChoiseBox(
//					(ChoiceBox<String>) Main.getMainInputManagerAircraftDefaultToolbarLayout()
//									.getItems()
//										.get(0)
//											.lookup("#defaultAircraftChoiseBox")
//					);
//			if(Main.getChoiceBoxSelectionDefaultAircraft() != null) {
//				if(Main.getChoiceBoxSelectionDefaultAircraft().equals("ATR-72"))
//					Main.getDefaultAircraftChoiceBox().getSelectionModel().select(0);
//				else if(Main.getChoiceBoxSelectionDefaultAircraft().equals("B747-100B"))		
//					Main.getDefaultAircraftChoiceBox().getSelectionModel().select(1);
//				else if(Main.getChoiceBoxSelectionDefaultAircraft().equals("AGILE-DC1"))
//					Main.getDefaultAircraftChoiceBox().getSelectionModel().select(2);
//			}
//		}
//
//		// CHECK IF NO CHOICE BOX ITEM HAS BEEN SELECTED 
//		Main.getLoadButtonDefaultAircraft().disableProperty().bind(
//				Main.getDefaultAircraftChoiceBox().valueProperty().isNull()
//				);
//		
//		//.......................................................................................
//		// FUSELAGE TAB
//		//.......................................................................................
//		
//        // get the pane of the front view
//		Main.setFuselageFrontViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageFrontViewPane")
//				);
//		
//		// get the pane of the side view
//		Main.setFuselageSideViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageSideViewPane")
//				);
//		
//		// get the pane of the top view
//		Main.setFuselageTopViewPane(
//				(Pane) Main.getMainInputManagerLayout().lookup("#fuselageTopViewPane")
//				);
//	}

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
	
	@FXML
	private void newAircraft() throws IOException {
		
		//..................................................................................
		// INPUT DATA WARNING
		Stage inputDataWarning = new Stage();
		
		inputDataWarning.setTitle("New Aircraft Warning");
		inputDataWarning.initModality(Modality.WINDOW_MODAL);
		inputDataWarning.initOwner(Main.getPrimaryStage());

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManagerWarning.fxml"));
		BorderPane inputDataWarningBorderPane = loader.load();
		
		Scene scene = new Scene(inputDataWarningBorderPane);
        inputDataWarning.setScene(scene);
        inputDataWarning.sizeToScene();
        inputDataWarning.show();

        Button yesButton = (Button) inputDataWarningBorderPane.lookup("#warningYesButton");
        yesButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				newAircraftImplementation();
				inputDataWarning.close();
			}
        	
		});
        Button noButton = (Button) inputDataWarningBorderPane.lookup("#warningNoButton");
        noButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				 inputDataWarning.close();
			}
        	
		});
        
	}
	
	private void newAircraftImplementation() {
		
		//..................................................................................
		// PRELIMINARY OPERATIONS
		Main.getStatusBar().setText("Ready!");
		Main.getProgressBar().setProgress(0.0);

		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftInputFile().clear();
		else
			Main.getDefaultAircraftChoiceBox().getSelectionModel().clearSelection();
		
		//..................................................................................
		// AIRCRAFT
		Main.getChoiceBoxAircraftType().getSelectionModel().clearSelection();
		Main.getChoiceBoxRegulationsType().getSelectionModel().clearSelection();
		
		// cabin configuration
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftCabinConfiguration().clear();
		
		// fuselage
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftFuselageFile().clear();
		Main.getTextFieldAircraftFuselageX().clear();
		Main.getFuselageXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftFuselageY().clear();
		Main.getFuselageYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftFuselageZ().clear();
		Main.getFuselageZUnitChoiceBox().getSelectionModel().clearSelection();
		
		// wing
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftWingFile().clear();
		Main.getTextFieldAircraftWingX().clear();
		Main.getWingXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftWingY().clear();
		Main.getWingYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftWingZ().clear();
		Main.getWingZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftWingRiggingAngle().clear();
		Main.getWingRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// hTail
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftHorizontalTailFile().clear();
		Main.getTextFieldAircraftHorizontalTailX().clear();
		Main.gethTailXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftHorizontalTailY().clear();
		Main.gethTailYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftHorizontalTailZ().clear();
		Main.gethtailZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftHorizontalTailRiggingAngle().clear();
		Main.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// vTail
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftVerticalTailFile().clear();
		Main.getTextFieldAircraftVerticalTailX().clear();
		Main.getvTailXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftVerticalTailY().clear();
		Main.getvTailYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftVerticalTailZ().clear();
		Main.getvTailZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftVerticalTailRiggingAngle().clear();
		Main.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// canard
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftCanardFile().clear();
		Main.getTextFieldAircraftCanardX().clear();
		Main.getCanardXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftCanardY().clear();
		Main.getCanardYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftCanardZ().clear();
		Main.getCanardZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftCanardRiggingAngle().clear();
		Main.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// Power Plant
		Main.getPowerPlantXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getPowerPlantYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getPowerPlantZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getPowerPlantTiltAngleUnitChoiceBox().getSelectionModel().clearSelection();
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftEngineFileList().stream().forEach(t -> t.clear());
		Main.getTextFieldAircraftEngineXList().stream().forEach(t -> t.clear());
		Main.getTextFieldAircraftEngineYList().stream().forEach(t -> t.clear());
		Main.getTextFieldAircraftEngineZList().stream().forEach(t -> t.clear());
		Main.getChoiceBoxesAircraftEnginePositonList().stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		Main.getTextFieldAircraftEngineTiltList().stream().forEach(t -> t.clear());
		
		// Nacelle
		Main.getNacelleXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getNacelleYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getNacelleZUnitChoiceBox().getSelectionModel().clearSelection();
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftNacelleFileList().stream().forEach(t -> t.clear());
		Main.getTextFieldAircraftNacelleXList().stream().forEach(t -> t.clear());
		Main.getTextFieldAircraftNacelleYList().stream().forEach(t -> t.clear());
		Main.getTextFieldAircraftNacelleZList().stream().forEach(t -> t.clear());
		Main.getChoiceBoxesAircraftNacellePositonList().stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		
		// Landing Gears
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftLandingGearsFile().clear();
		Main.getTextFieldAircraftLandingGearsX().clear();
		Main.getLandingGearsXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftLandingGearsY().clear();
		Main.getLandingGearsYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftLandingGearsZ().clear();
		Main.getLandingGearsZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().clearSelection();
		
		// Systems
		if(Main.getIsAircraftFormFile())
			Main.getTextFieldAircraftSystemsFile().clear();
		Main.getTextFieldAircraftSystemsX().clear();
		Main.getSystemsXUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftSystemsY().clear();
		Main.getSystemsYUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldAircraftSystemsZ().clear();
		Main.getSystemsZUnitChoiceBox().getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		Main.getTextAreaAircraftConsoleOutput().clear();
		Main.getAircraftTopViewPane().getChildren().clear();
		Main.getAircraftSideViewPane().getChildren().clear();
		Main.getAircraftFrontViewPane().getChildren().clear();
		
		//..................................................................................
		// FUSELAGE
		Main.getFuselageAdjustCriterion().getSelectionModel().clearSelection();
		Main.getFuselageAdjustCriterion().setDisable(true);
		
		// Pressurized
		Main.getFuselagePressurizedCheckBox().setSelected(false);
		
		// Global Data
		Main.getTextFieldFuselageDeckNumber().clear();
		Main.getTextFieldFuselageLength().clear();
		Main.getFuselageLengthUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageSurfaceRoughness().clear();
		Main.getFuselageRoughnessUnitChoiceBox().getSelectionModel().clearSelection();
		
		// Nose Trunk
		Main.getTextFieldFuselageNoseLengthRatio().clear();
		Main.getTextFieldFuselageNoseTipOffset().clear();
		Main.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageNoseDxCap().clear();
		Main.getChoiceBoxFuselageNoseWindshieldType().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageNoseWindshieldHeight().clear();
		Main.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageNoseWindshieldWidth().clear();
		Main.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageNoseMidSectionHeight().clear();
		Main.getTextFieldFuselageNoseMidSectionRhoUpper().clear();
		Main.getTextFieldFuselageNoseMidSectionRhoLower().clear();

		// Cylindrical Trunk
		Main.getTextFieldFuselageCylinderLengthRatio().clear();
		Main.getTextFieldFuselageCylinderSectionWidth().clear();
		Main.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().clearSelection();		
		Main.getTextFieldFuselageCylinderSectionHeight().clear();
		Main.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageCylinderHeightFromGround().clear();
		Main.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageCylinderSectionHeightRatio().clear();
		Main.getTextFieldFuselageCylinderSectionRhoUpper().clear();
		Main.getTextFieldFuselageCylinderSectionRhoLower().clear();
		
		// Tail Trunk
		Main.getTextFieldFuselageTailTipOffset().clear();
		Main.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().clearSelection();
		Main.getTextFieldFuselageTailDxCap().clear();
		Main.getTextFieldFuselageTailMidSectionHeight().clear();
		Main.getTextFieldFuselageTailMidRhoUpper().clear();
		Main.getTextFieldFuselageTailMidRhoLower().clear();
		
		// Spoilers
		if(!Main.getTheAircraft().getFuselage().getSpoilers().isEmpty()) {
			Main.getFuselageSpoilersDeltaMaxUnitChoiceBox().getSelectionModel().clearSelection();
			Main.getFuselageSpoilersDeltaMinUnitChoiceBox().getSelectionModel().clearSelection();	
			Main.getTextFieldSpoilersXInboradList().stream().forEach(t -> t.clear());
			Main.getTextFieldSpoilersXOutboradList().stream().forEach(t -> t.clear());
			Main.getTextFieldSpoilersYInboradList().stream().forEach(t -> t.clear());
			Main.getTextFieldSpoilersYOutboradList().stream().forEach(t -> t.clear());
			Main.getTextFieldSpoilersMaxDeflectionList().stream().forEach(t -> t.clear());
			Main.getTextFieldSpoilersMinDeflectionList().stream().forEach(t -> t.clear());
		}
		
		// 3 View and TextArea
		Main.getTextAreaFuselageConsoleOutput().clear();
		Main.getFuselageTopViewPane().getChildren().clear();
		Main.getFuselageSideViewPane().getChildren().clear();
		Main.getFuselageFrontViewPane().getChildren().clear();
		
		// TODO: CONTINUE WITH WING, etc ...
		
		Main.setTheAircraft(null);
		Main.setIsAircraftFormFile(null);
		
	}
	
	FileChooser chooser;
	
	@FXML
	private void chooseAircraftFile() throws IOException {

		chooser = new FileChooser();
		chooser.setTitle("Open File");
		chooser.setInitialDirectory(new File(Main.getInputDirectoryPath()));
		File file = chooser.showOpenDialog(null);
		if (file != null) {
			// get full path and populate the text box
			Main.getTextFieldAircraftInputFile().setText(file.getAbsolutePath());
			Main.setInputFileAbsolutePath(file.getAbsolutePath());
		}
		
	}
	
	@FXML
	private void loadAircraftFile() throws IOException, InterruptedException {
	
		if(Main.getTheAircraft() != null)
			Main.setChoiseBoxSelectionDefaultAircraft(null);
		
		if(Main.isAircraftFile(Main.getTextFieldAircraftInputFile().getText()))
			try {
				loadAircraftFileImplementation();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void loadAircraftFileImplementation() throws IOException, InterruptedException {
		
		MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, Main.getDatabaseDirectoryPath());
		String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
		String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
		String highLiftDatabaseFileName = "HighLiftDatabase.h5";
		String fusDesDatabaseFilename = "FusDes_database.h5";
		String vedscDatabaseFilename = "VeDSC_database.h5";
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);
		FusDesDatabaseReader fusDesDatabaseReader = new FusDesDatabaseReader(databaseFolderPath, fusDesDatabaseFilename);
		VeDSCDatabaseReader veDSCDatabaseReader = new VeDSCDatabaseReader(databaseFolderPath, vedscDatabaseFilename);
		Main.getProgressBar().setProgress(0.1);
		
		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String dirAirfoils = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces" + File.separator + "airfoils";

		String pathToXML = Main.getTextFieldAircraftInputFile().getText(); 

		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		Main.setTheAircraft(Aircraft.importFromXML(
				pathToXML,
				dirLiftingSurfaces,
				dirFuselages,
				dirEngines,
				dirNacelles,
				dirLandingGears,
				dirSystems,
				dirCabinConfiguration,
				dirAirfoils,
				aeroDatabaseReader,
				highLiftDatabaseReader,
				fusDesDatabaseReader,
				veDSCDatabaseReader)
				);
		Main.getProgressBar().setProgress(0.2);

		// COMPONENTS LOG TO INTERFACE
		logAircraftFromFileToInterface();
		Main.getProgressBar().setProgress(0.3);
		logFuselageFromFileToInterface();
		Main.getProgressBar().setProgress(0.4);
		
		// COMPONENTS 3 VIEW CREATION
		//............................
		// aircraft
		createAircraftTopView();
		Main.getProgressBar().setProgress(0.5);
		createAircraftSideView();
		Main.getProgressBar().setProgress(0.6);
		createAircraftFrontView();
		Main.getProgressBar().setProgress(0.7);
		//............................
		// aircraft
		createFuselageTopView();
		Main.getProgressBar().setProgress(0.8);
		createFuselageSideView();
		Main.getProgressBar().setProgress(0.9);
		createFuselageFrontView();
		Main.getProgressBar().setProgress(1.0);
		
		// write again
		System.setOut(originalOut);
		
		Main.getStatusBar().setText("Aircraft loaded!");
		
	}

	public static void createAircraftTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY();

		Double[][] dataOutlineXYLeftCurve = new Double[nX1Left][2];
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			dataOutlineXYLeftCurve[i][1] = vX1Left.get(i).doubleValue(SI.METRE);
			dataOutlineXYLeftCurve[i][0] = vY1Left.get(i).doubleValue(SI.METRE);
		});

		// right curve, upperview
		List<Amount<Length>> vX2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY();

		Double[][] dataOutlineXYRightCurve = new Double[nX2Right][2];
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			dataOutlineXYRightCurve[i][1] = vX2Right.get(i).doubleValue(SI.METRE);
			dataOutlineXYRightCurve[i][0] = vY2Right.get(i).doubleValue(SI.METRE);
		});

		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> vY = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedYs();
		int nY = vY.size();
		List<Amount<Length>> vChords = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXle = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle();
		
		Double[][] dataChordsVsY = new Double[nY][2];
		Double[][] dataXleVsY = new Double[nY][2];
		IntStream.range(0, nY)
		.forEach(i -> {
			dataChordsVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataChordsVsY[i][1] = vChords.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][0] = vY.get(i).doubleValue(SI.METRE);
			dataXleVsY[i][1] = vXle.get(i).doubleValue(SI.METRE);
		});

		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		Double[][] dataTopView = new Double[dataTopViewIsolated.length][dataTopViewIsolated[0].length];
		for (int i=0; i<dataTopViewIsolated.length; i++) { 
			dataTopView[i][0] = dataTopViewIsolated[i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER);
			dataTopView[i][1] = dataTopViewIsolated[i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER);
		}
		
		Double[][] dataTopViewMirrored = new Double[dataTopView.length][dataTopView[0].length];
		for (int i=0; i<dataTopView.length; i++) { 
				dataTopViewMirrored[i][0] = -dataTopView[i][0];
				dataTopViewMirrored[i][1] = dataTopView[i][1];
		}

		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vYHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedYs();
		int nYHTail = vYHTail.size();
		List<Amount<Length>> vChordsHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedChords();
		List<Amount<Length>> vXleHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle();

		Double[][] dataChordsVsYHTail = new Double[nYHTail][2];
		Double[][] dataXleVsYHTail = new Double[nYHTail][2];
		IntStream.range(0, nYHTail)
		.forEach(i -> {
			dataChordsVsYHTail[i][0] = vYHTail.get(i).doubleValue(SI.METRE);
			dataChordsVsYHTail[i][1] = vChordsHTail.get(i).doubleValue(SI.METRE);
			dataXleVsYHTail[i][0] = vYHTail.get(i).doubleValue(SI.METRE);
			dataXleVsYHTail[i][1] = vXleHTail.get(i).doubleValue(SI.METRE);
		});

		Double[][] dataTopViewIsolatedHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

		Double[][] dataTopViewHTail = new Double[dataTopViewIsolatedHTail.length][dataTopViewIsolatedHTail[0].length];
		for (int i=0; i<dataTopViewIsolatedHTail.length; i++) { 
			dataTopViewHTail[i][0] = dataTopViewIsolatedHTail[i][0];
			dataTopViewHTail[i][1] = dataTopViewIsolatedHTail[i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER);
		}

		Double[][] dataTopViewMirroredHTail = new Double[dataTopViewHTail.length][dataTopViewHTail[0].length];
		for (int i=0; i<dataTopViewHTail.length; i++) { 
			dataTopViewMirroredHTail[i][0] = -dataTopViewHTail[i][0];
			dataTopViewMirroredHTail[i][1] = dataTopViewHTail[i][1];
		}

		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		Double[] vTailRootXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] vTailRootYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] vTailRootAirfoilPoints = new Double[vTailRootXCoordinates.length][2];
		for (int i=0; i<vTailRootAirfoilPoints.length; i++) {
			vTailRootAirfoilPoints[i][1] = (vTailRootXCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue()) + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue(); 
			vTailRootAirfoilPoints[i][0] = (vTailRootYCoordinates[i]*Main.getTheAircraft().getVTail().getChordRoot().getEstimatedValue());
		}
		
		int nPointsVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] vTailTipXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] vTailTipYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] vTailTipAirfoilPoints = new Double[vTailTipXCoordinates.length][2];
		for (int i=0; i<vTailTipAirfoilPoints.length; i++) {
			vTailTipAirfoilPoints[i][1] = (vTailTipXCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue()) 
										  + Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue()
										  + Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(); 
			vTailTipAirfoilPoints[i][0] = (vTailTipYCoordinates[i]*Main.getTheAircraft().getVTail().getChordTip().getEstimatedValue());
		}
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft();

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			}
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			}
			
			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
			dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYApexConstructionAxes()));
			
			
			Double[][] dataOutlineXZCurveNacelle = new Double[dataOutlineXZCurveNacelleX.size()][2];
			for(int j=0; j<dataOutlineXZCurveNacelleX.size(); j++) {
				dataOutlineXZCurveNacelle[j][1] = dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER);
				dataOutlineXZCurveNacelle[j][0] = dataOutlineXZCurveNacelleY.get(j).doubleValue(SI.METER);
			}
			
			nacellePointsList.add(dataOutlineXZCurveNacelle);
			
		}
		
		List<Double[][]> listDataArrayTopView = new ArrayList<Double[][]>();

		// wing
		listDataArrayTopView.add(dataTopView);
		listDataArrayTopView.add(dataTopViewMirrored);
		// hTail
		listDataArrayTopView.add(dataTopViewHTail);
		listDataArrayTopView.add(dataTopViewMirroredHTail);
		// fuselage
		listDataArrayTopView.add(dataOutlineXYLeftCurve);
		listDataArrayTopView.add(dataOutlineXYRightCurve);
		// vTail
		listDataArrayTopView.add(vTailRootAirfoilPoints);
		listDataArrayTopView.add(vTailTipAirfoilPoints);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArrayTopView.add(nacellePointsList.get(i));

		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
			
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsTopView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinTopView, xMaxTopView)
				.yRange(yMaxTopView, yMinTopView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Top View")
				.xLabel("y (m)")
				.yLabel("x (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,true,true,true,true)
				.areaStyles("fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;","fill:white;","fill:white;",
						"fill:yellow;","fill:yellow;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsTopView,
				listDataArrayTopView
				);
		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "AircraftTopView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists())
				outputFile.delete();
				
			d3Plotter.saveSVG(outputFilePathTopView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserTopView = d3Plotter.getBrowser(postLoadingHook, false);
		Scene sceneTopView = new Scene(browserTopView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getAircraftTopViewPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public static void createAircraftSideView() {
	
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurve = new Double[nX1Upper][2];
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			dataOutlineXZUpperCurve[i][0] = vX1Upper.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurve = new Double[nX2Lower][2];
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			dataOutlineXZLowerCurve[i][0] = vX2Lower.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[] wingRootXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] wingRootZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] wingRootAirfoilPoints = new Double[wingRootXCoordinates.length][2];
		for (int i=0; i<wingRootAirfoilPoints.length; i++) {
			wingRootAirfoilPoints[i][0] = (wingRootXCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue()) 
										  + Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue(); 
			wingRootAirfoilPoints[i][1] = (wingRootZCoordinates[i]*Main.getTheAircraft().getWing().getChordRoot().getEstimatedValue())
										  + Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue();
		}
		
		int nPointsWing = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] wingTipXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] wingTipZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] wingTipAirfoilPoints = new Double[wingTipXCoordinates.length][2];
		for (int i=0; i<wingTipAirfoilPoints.length; i++) {
			wingTipAirfoilPoints[i][0] = (wingTipXCoordinates[i]*Main.getTheAircraft().getWing().getChordTip().getEstimatedValue()) 
										  + Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue()
										  + Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(); 
			wingTipAirfoilPoints[i][1] = (wingTipZCoordinates[i]*Main.getTheAircraft().getWing().getChordTip().getEstimatedValue())
										  + Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		Double[] hTailRootXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getXCoords();
		Double[] hTailRootZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getAirfoilCreator().getZCoords();
		Double[][] hTailRootAirfoilPoints = new Double[hTailRootXCoordinates.length][2];
		for (int i=0; i<hTailRootAirfoilPoints.length; i++) {
			hTailRootAirfoilPoints[i][0] = (hTailRootXCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
										   + Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue(); 
			hTailRootAirfoilPoints[i][1] = (hTailRootZCoordinates[i]*Main.getTheAircraft().getHTail().getChordRoot().getEstimatedValue())
										   + Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue();
		}
		
		int nPointsHTail = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().size();
		Double[] hTailTipXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getXCoords();
		Double[] hTailTipZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getAirfoilCreator().getZCoords();
		Double[][] hTailTipAirfoilPoints = new Double[hTailTipXCoordinates.length][2];
		for (int i=0; i<hTailTipAirfoilPoints.length; i++) {
			hTailTipAirfoilPoints[i][0] = (hTailTipXCoordinates[i]*Main.getTheAircraft().getHTail().getChordTip().getEstimatedValue()) 
										  + Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue()
										  + Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(); 
			hTailTipAirfoilPoints[i][1] = (hTailTipZCoordinates[i]*Main.getTheAircraft().getHTail().getChordTip().getEstimatedValue())
										  + Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		for(int i=0; i<dataTopViewVTail.length; i++){
			dataTopViewVTail[i][0] += Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue();
			dataTopViewVTail[i][1] += Main.getTheAircraft().getVTail().getZApexConstructionAxes().getEstimatedValue();
		}
		
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();
		
		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
			
			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline();
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper();
			
			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower();

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			}
			
			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) 
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1)
						.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			}
			
			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXApexConstructionAxes()));
			dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0)
					.plus(Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZApexConstructionAxes()));
			
			
			Double[][] dataOutlineXZCurveNacelle = new Double[dataOutlineXZCurveNacelleX.size()][2];
			for(int j=0; j<dataOutlineXZCurveNacelleX.size(); j++) {
				dataOutlineXZCurveNacelle[j][0] = dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER);
				dataOutlineXZCurveNacelle[j][1] = dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER);
			}
			
			nacellePointsList.add(dataOutlineXZCurveNacelle);
			
		}
		
		//--------------------------------------------------
		// get data vectors from landing gears 
		//--------------------------------------------------
		Amount<Length> radius = Main.getTheAircraft().getLandingGears().getRearWheelsHeight().divide(2);
		Double[] wheelCenterPosition = new Double[] {
				Main.getTheAircraft().getLandingGears().getXApexConstructionAxes().doubleValue(SI.METER),
				Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
				- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
				- radius.doubleValue(SI.METER)
		};
		Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);
		
		Double[][] dataSideViewLandingGear = new Double[thetaArray.length][2];
		for(int i=0; i<thetaArray.length; i++) {
			
			dataSideViewLandingGear[i][0] = radius.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + wheelCenterPosition[0]; 
			dataSideViewLandingGear[i][1] = radius.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + wheelCenterPosition[1];
			
		}
		
		List<Double[][]> listDataArraySideView = new ArrayList<Double[][]>();

		// vTail
		listDataArraySideView.add(dataTopViewVTail);
		// fuselage
		listDataArraySideView.add(dataOutlineXZUpperCurve);
		listDataArraySideView.add(dataOutlineXZLowerCurve);
		// wing
		listDataArraySideView.add(wingRootAirfoilPoints);
		listDataArraySideView.add(wingTipAirfoilPoints);
		// hTail
		listDataArraySideView.add(hTailRootAirfoilPoints);
		listDataArraySideView.add(hTailTipAirfoilPoints);
		// landing gears
		listDataArraySideView.add(dataSideViewLandingGear);
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArraySideView.add(nacellePointsList.get(i));
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsSideView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinSideView, xMaxSideView)
				.yRange(yMinSideView, yMaxSideView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Side View")
				.xLabel("x (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,false,true,true,true,true,true,true)
				.areaStyles("fill:yellow;","fill:white;","fill:white;","fill:lightblue;","fill:lightblue;","fill:blue;","fill:blue;",
						"fill:black;","fill:orange;","fill:orange;","fill:orange;","fill:orange;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsSideView,
				listDataArraySideView
				);
		
		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathSideView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "AircraftSideView.svg";
			File outputFile = new File(outputFilePathSideView);
			if(outputFile.exists())
				outputFile.delete();
			d3Plotter.saveSVG(outputFilePathSideView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserSideView = d3Plotter.getBrowser(postLoadingHook, false);
		Scene sceneSideView = new Scene(browserSideView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getAircraftSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public static void createAircraftFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ();

		Double[][] dataSectionYZUpperCurve = new Double[nY1Upper][2];
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			dataSectionYZUpperCurve[i][0] = vY1Upper.get(i).doubleValue(SI.METRE);
			dataSectionYZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// section lower curve
		List<Amount<Length>> vY2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ();

		Double[][] dataSectionYZLowerCurve = new Double[nY2Lower][2];
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			dataSectionYZLowerCurve[i][0] = vY2Lower.get(i).doubleValue(SI.METRE);
			dataSectionYZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		List<Amount<Length>> wingBreakPointsYCoordinates = Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsWingTemp; i++)
			wingBreakPointsYCoordinates.add(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsWingTemp-i-1));
		int nYPointsWing = wingBreakPointsYCoordinates.size();
		
		List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getWing().getAirfoilList().size(); i++)
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getWing().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsWingTemp; i++) {
			wingThicknessZCoordinates.add(
					Amount.valueOf(
							(Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
									MyArrayUtils.getMin(Main.getTheAircraft().getWing().getAirfoilList().get(nYPointsWingTemp-i-1).getAirfoilCreator().getZCoords())),
							SI.METER
							)
					);
		}
		
		List<Amount<Angle>> dihedralList = new ArrayList<>();
		for (int i = 0; i < Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralList.add(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
					);
		}
		for (int i = 0; i < Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralList.add(
					Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
							Main.getTheAircraft().getWing().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
					);
		}
		
		Double[][] dataFrontViewWing = new Double[nYPointsWing][2];
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			dataFrontViewWing[i][0] = wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewWing[i][1] = wingThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
										.times(Math.sin(dihedralList.get(i)
												.doubleValue(SI.RADIAN))
												)
										)
							.doubleValue(SI.METRE);
		});
		
		Double[][] dataFrontViewWingMirrored = new Double[nYPointsWing][2];
		IntStream.range(0, nYPointsWing)
		.forEach(i -> {
			dataFrontViewWingMirrored[i][0] = -dataFrontViewWing[i][0];
			dataFrontViewWingMirrored[i][1] = dataFrontViewWing[i][1];
		});
		
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		List<Amount<Length>> hTailBreakPointsYCoordinates = Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailBreakPointsYCoordinates.add(Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsHTailTemp-i-1));
		int nYPointsHTail = hTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getHTail().getAirfoilList().size(); i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getHTail().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsHTailTemp; i++)
			hTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(Main.getTheAircraft().getHTail().getAirfoilList().get(nYPointsHTailTemp-i-1).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		
		List<Amount<Angle>> dihedralListHTail = new ArrayList<>();
		for (int i = 0; i < Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralListHTail.add(
					Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(i)
					);
		}
		for (int i = 0; i < Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size(); i++) {
			dihedralListHTail.add(
					Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().get(
							Main.getTheAircraft().getHTail().getLiftingSurfaceCreator().getDihedralsBreakPoints().size()-1-i)
					);
		}
		
		Double[][] dataFrontViewHTail = new Double[nYPointsHTail][2];
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			dataFrontViewHTail[i][0] = hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewHTail[i][1] = hTailThicknessZCoordinates.get(i)
					.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
					.plus(hTailBreakPointsYCoordinates.get(i)
									.times(Math.sin(dihedralListHTail.get(i)
											.doubleValue(SI.RADIAN))
											)
									)
						.doubleValue(SI.METRE);
		});
		
		Double[][] dataFrontViewHTailMirrored = new Double[nYPointsHTail][2];
		IntStream.range(0, nYPointsHTail)
		.forEach(i -> {
			dataFrontViewHTailMirrored[i][0] = -dataFrontViewHTail[i][0];
			dataFrontViewHTailMirrored[i][1] = dataFrontViewHTail[i][1];
		});
		
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		List<Amount<Length>> vTailBreakPointsYCoordinates = Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints();
		int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailBreakPointsYCoordinates.add(Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getYBreakPoints().get(nYPointsVTailTemp-i-1));
		int nYPointsVTail = vTailBreakPointsYCoordinates.size();
		
		List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
		for(int i=0; i<Main.getTheAircraft().getVTail().getAirfoilList().size(); i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
							MyArrayUtils.getMax(Main.getTheAircraft().getVTail().getAirfoilList().get(i).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		for(int i=0; i<nYPointsVTailTemp; i++)
			vTailThicknessZCoordinates.add(
					Amount.valueOf(
							Main.getTheAircraft().getVTail().getLiftingSurfaceCreator().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
							MyArrayUtils.getMin(Main.getTheAircraft().getVTail().getAirfoilList().get(nYPointsVTailTemp-i-1).getAirfoilCreator().getZCoords()),
							SI.METER
							)
					);
		
		Double[][] dataFrontViewVTail = new Double[nYPointsVTail][2];
		IntStream.range(0, nYPointsVTail)
		.forEach(i -> {
			dataFrontViewVTail[i][0] = vTailThicknessZCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE);
			dataFrontViewVTail[i][1] = vTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE);
		});
		
		//--------------------------------------------------
		// get data vectors from engine discretization
		//--------------------------------------------------
		List<Double[][]> nacellePointsList = new ArrayList<Double[][]>();

		for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 20);
			double[] yCoordinate = new double[angleArray.length];
			double[] zCoordinate = new double[angleArray.length];

			double radius = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getDiameterMax()
					.divide(2)
					.doubleValue(SI.METER);
			double y0 = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getYApexConstructionAxes()
					.doubleValue(SI.METER);

			double z0 = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getZApexConstructionAxes()
					.doubleValue(SI.METER);

			for(int j=0; j<angleArray.length; j++) {
				yCoordinate[j] = radius*Math.cos(angleArray[j]);
				zCoordinate[j] = radius*Math.sin(angleArray[j]);
			}

			Double[][] nacellePoints = new Double[yCoordinate.length][2];
			for (int j=0; j<yCoordinate.length; j++) {
				nacellePoints[j][0] = yCoordinate[j] + y0;
				nacellePoints[j][1] = zCoordinate[j] + z0;
			}

			nacellePointsList.add(nacellePoints);
		}
		
		//--------------------------------------------------
		// get data vectors from landing gears
		//--------------------------------------------------
		List<Double[][]> leftLandingGearsPointsList = new ArrayList<>();
		List<Double[][]> rightLandingGearsPointsList = new ArrayList<>();
		
		for(int i=0; i<Main.getTheAircraft().getLandingGears().getNumberOfRearWheels()/2; i++) {
			
			Double[][] leftLandingGearsPoints = new Double[5][2];
			Double[][] rightLandingGearsPoints = new Double[5][2];
			
			// landing gears X coordinates
			leftLandingGearsPoints[0][0] = Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER));
			leftLandingGearsPoints[1][0] = Main.getTheAircraft().getLandingGears()
					.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
					+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
					+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER));
			leftLandingGearsPoints[2][0] = leftLandingGearsPoints[1][0];
			leftLandingGearsPoints[3][0] = leftLandingGearsPoints[0][0];
			leftLandingGearsPoints[4][0] = leftLandingGearsPoints[0][0];

			rightLandingGearsPoints[0][0] = - leftLandingGearsPoints[0][0];
			rightLandingGearsPoints[1][0] = - leftLandingGearsPoints[1][0];
			rightLandingGearsPoints[2][0] = rightLandingGearsPoints[1][0];
			rightLandingGearsPoints[3][0] = rightLandingGearsPoints[0][0];
			rightLandingGearsPoints[4][0] = rightLandingGearsPoints[0][0];

			// landing gears Y coordinates
			leftLandingGearsPoints[0][1] = Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER);
			leftLandingGearsPoints[1][1] = leftLandingGearsPoints[0][1];
			leftLandingGearsPoints[2][1] = Main.getTheAircraft().getLandingGears().getZApexConstructionAxes().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER);
			leftLandingGearsPoints[3][1] = leftLandingGearsPoints[2][1];
			leftLandingGearsPoints[4][1] = leftLandingGearsPoints[0][1];

			rightLandingGearsPoints[0][1] = leftLandingGearsPoints[0][1];
			rightLandingGearsPoints[1][1] = leftLandingGearsPoints[1][1];
			rightLandingGearsPoints[2][1] = leftLandingGearsPoints[2][1];
			rightLandingGearsPoints[3][1] = leftLandingGearsPoints[3][1];
			rightLandingGearsPoints[4][1] = leftLandingGearsPoints[4][1];
		
			leftLandingGearsPointsList.add(leftLandingGearsPoints);
			rightLandingGearsPointsList.add(rightLandingGearsPoints);
		}
		
		
		List<Double[][]> listDataArrayFrontView = new ArrayList<Double[][]>();

		// hTail
		listDataArrayFrontView.add(dataFrontViewHTail);
		listDataArrayFrontView.add(dataFrontViewHTailMirrored);
		// vTail
		listDataArrayFrontView.add(dataFrontViewVTail);
		// wing
		listDataArrayFrontView.add(dataFrontViewWing);
		listDataArrayFrontView.add(dataFrontViewWingMirrored);
		// fuselage
		listDataArrayFrontView.add(dataSectionYZUpperCurve);
		listDataArrayFrontView.add(dataSectionYZLowerCurve);
		// landing gears
		for (int i=0; i<Main.getTheAircraft().getLandingGears().getNumberOfRearWheels()/2; i++) {
			listDataArrayFrontView.add(leftLandingGearsPointsList.get(i));
			listDataArrayFrontView.add(rightLandingGearsPointsList.get(i));
		}
		// nacelles
		for (int i=0; i<nacellePointsList.size(); i++)
			listDataArrayFrontView.add(nacellePointsList.get(i));
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsFrontView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(yMinFrontView, yMaxFrontView)
				.yRange(zMinFrontView, zMaxFrontView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Aircraft data representation - Front View")
				.xLabel("y (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2,2,2,2,2,2,2,2,2,2,2,2,2,2)
				.showSymbols(false,false,false,false,false,false,false,false,false,false,false,false,false,false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true,true,true,true,true,true,false,false,false,false,true,true,true,true)
				.areaStyles("fill:blue;","fill:blue;","fill:yellow;","fill:lightblue;","fill:lightblue;","fill:white;","fill:white;",
						"fill:black;","fill:black;","fill:black;","fill:black;","fill:orange;","fill:orange;","fill:orange;","fill:orange;")
				.areaOpacities(1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsFrontView,
				listDataArrayFrontView
				);

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathFrontView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "AircraftFrontView.svg";
			File outputFile = new File(outputFilePathFrontView);
			if(outputFile.exists())
				outputFile.delete();
			d3Plotter.saveSVG(outputFilePathFrontView);


		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserFrontView = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene sceneFrontView = new Scene(browserFrontView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getAircraftFrontViewPane().getChildren().add(sceneFrontView.getRoot());
	}
	
	public static void createFuselageTopView() {
	
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountX();
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideLCurveAmountY();

		Double[][] dataOutlineXYLeftCurve = new Double[nX1Left][2];
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			dataOutlineXYLeftCurve[i][1] = vX1Left.get(i).doubleValue(SI.METRE);
			dataOutlineXYLeftCurve[i][0] = vY1Left.get(i).doubleValue(SI.METRE);
		});

		// right curve, upperview
		List<Amount<Length>> vX2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountX();
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXYSideRCurveAmountY();

		Double[][] dataOutlineXYRightCurve = new Double[nX2Right][2];
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			dataOutlineXYRightCurve[i][1] = vX2Right.get(i).doubleValue(SI.METRE);
			dataOutlineXYRightCurve[i][0] = vY2Right.get(i).doubleValue(SI.METRE);
		});

		List<Double[][]> listDataArrayTopView = new ArrayList<Double[][]>();

		// fuselage
		listDataArrayTopView.add(dataOutlineXYLeftCurve);
		listDataArrayTopView.add(dataOutlineXYRightCurve);

		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
			
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsTopView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinTopView, xMaxTopView)
				.yRange(yMaxTopView, yMinTopView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Fuselage data representation - Top View")
				.xLabel("y (m)")
				.yLabel("x (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2)
				.showSymbols(false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true)
				.areaStyles("fill:white;","fill:white;")
				.areaOpacities(1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsTopView,
				listDataArrayTopView
				);
		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "FuselageTopView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists())
				outputFile.delete();
			d3Plotter.saveSVG(outputFilePathTopView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserTopView = d3Plotter.getBrowser(postLoadingHook, false);
		Scene sceneTopView = new Scene(browserTopView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getFuselageTopViewPane().getChildren().add(sceneTopView.getRoot());
	}

	public static void createFuselageSideView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountX();
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZUpperCurveAmountZ();

		Double[][] dataOutlineXZUpperCurve = new Double[nX1Upper][2];
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			dataOutlineXZUpperCurve[i][0] = vX1Upper.get(i).doubleValue(SI.METRE);
			dataOutlineXZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountX();
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getOutlineXZLowerCurveAmountZ();

		Double[][] dataOutlineXZLowerCurve = new Double[nX2Lower][2];
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			dataOutlineXZLowerCurve[i][0] = vX2Lower.get(i).doubleValue(SI.METRE);
			dataOutlineXZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		List<Double[][]> listDataArraySideView = new ArrayList<Double[][]>();

		// fuselage
		listDataArraySideView.add(dataOutlineXZUpperCurve);
		listDataArraySideView.add(dataOutlineXZLowerCurve);
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageCreator().getLenF().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsSideView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(xMinSideView, xMaxSideView)
				.yRange(yMinSideView, yMaxSideView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Fuselage data representation - Side View")
				.xLabel("x (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2)
				.showSymbols(false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:blue; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true)
				.areaStyles("fill:white;","fill:white;")
				.areaOpacities(1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsSideView,
				listDataArraySideView
				);
		
		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathSideView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "FuselageSideView.svg";
			File outputFile = new File(outputFilePathSideView);
			if(outputFile.exists())
				outputFile.delete();
			d3Plotter.saveSVG(outputFilePathSideView);

		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserSideView = d3Plotter.getBrowser(postLoadingHook, false);
		Scene sceneSideView = new Scene(
				browserSideView, 
				Main.getFuselageSideViewPane().getPrefWidth(), 
				Main.getFuselageSideViewPane().getPrefHeight(), 
				Color.web("#666970")
				);
		Main.getFuselageSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public static void createFuselageFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountY();
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionUpperCurveAmountZ();

		Double[][] dataSectionYZUpperCurve = new Double[nY1Upper][2];
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			dataSectionYZUpperCurve[i][0] = vY1Upper.get(i).doubleValue(SI.METRE);
			dataSectionYZUpperCurve[i][1] = vZ1Upper.get(i).doubleValue(SI.METRE);
		});

		// section lower curve
		List<Amount<Length>> vY2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountY();
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = Main.getTheAircraft().getFuselage().getFuselageCreator().getSectionLowerCurveAmountZ();

		Double[][] dataSectionYZLowerCurve = new Double[nY2Lower][2];
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			dataSectionYZLowerCurve[i][0] = vY2Lower.get(i).doubleValue(SI.METRE);
			dataSectionYZLowerCurve[i][1] = vZ2Lower.get(i).doubleValue(SI.METRE);
		});
		
		List<Double[][]> listDataArrayFrontView = new ArrayList<Double[][]>();

		// fuselage
		listDataArrayFrontView.add(dataSectionYZUpperCurve);
		listDataArrayFrontView.add(dataSectionYZLowerCurve);
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 700;
		int HEIGHT = 600;
		
		D3PlotterOptions optionsFrontView = new D3PlotterOptions.D3PlotterOptionsBuilder()
				.widthGraph(WIDTH).heightGraph(HEIGHT)
				.xRange(yMinFrontView, yMaxFrontView)
				.yRange(zMinFrontView, zMaxFrontView)
				.axisLineColor("darkblue").axisLineStrokeWidth("2px")
				.graphBackgroundColor("blue").graphBackgroundOpacity(0.05)
				.title("Fuselage data representation - Front View")
				.xLabel("y (m)")
				.yLabel("z (m)")
				.showXGrid(true)
				.showYGrid(true)
				.symbolTypes(
						SymbolType.CIRCLE,
						SymbolType.CIRCLE
						)
				.symbolSizes(2,2)
				.showSymbols(false,false) // NOTE: overloaded function
				.symbolStyles(
						"fill:cyan; stroke:darkblue; stroke-width:2",
						"fill:cyan; stroke:darkblue; stroke-width:2"
						)
				.lineStyles(
						"fill:none; stroke:black; stroke-width:2",
						"fill:none; stroke:black; stroke-width:2"
						)
				.plotAreas(true,true)
				.areaStyles("fill:white;","fill:white;")
				.areaOpacities(1.0,1.0)
				.showLegend(false)
				.build();
		
		D3Plotter d3Plotter = new D3Plotter(
				optionsFrontView,
				listDataArrayFrontView
				);

		//define d3 content as post loading hook
		Runnable postLoadingHook = () -> {

			//--------------------------------------------------
			// Create the D3 graph
			//--------------------------------------------------
			d3Plotter.createD3Content();
			
			//--------------------------------------------------
			// output
			String outputFilePathFrontView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "FuselageFrontView.svg";
			File outputFile = new File(outputFilePathFrontView);
			if(outputFile.exists())
				outputFile.delete();
			d3Plotter.saveSVG(outputFilePathFrontView);


		}; // end-of-Runnable

		// create the Browser/D3
		//create browser
		JavaFxD3Browser browserFrontView = d3Plotter.getBrowser(postLoadingHook, false);

		//create the scene
		Scene sceneFrontView = new Scene(browserFrontView, WIDTH+10, HEIGHT+10, Color.web("#666970"));
		Main.getFuselageFrontViewPane().getChildren().add(sceneFrontView.getRoot());
	}
	
	@SuppressWarnings("unchecked")
	public static void logAircraftFromFileToInterface() {

		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirSystems = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "systems";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String pathToXML = Main.getTextFieldAircraftInputFile().getText();

		// print the toString method of the aircraft inside the text area of the GUI ...
		if(Main.getTextAreaAircraftConsoleOutput() == null) 
			Main.setTextAreaAircraftConsoleOutput(
					(TextArea) Main.getMainInputManagerLayout().lookup("#output")
					);
		
		Main.getTextAreaAircraftConsoleOutput().setText(
				Main.getTheAircraft().toString()
				);

		// get the text field for aircraft input data
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		//---------------------------------------------------------------------------------
		// AIRCRAFT TYPE:
		if(Main.getChoiceBoxAircraftType() == null)
			Main.setChoiceBoxAircraftType(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftType")
					);
		
		String aircraftTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		
		if(aircraftTypeFileName != null) { 
			if(Main.getChoiceBoxAircraftType() != null) {
				if(aircraftTypeFileName.equalsIgnoreCase("JET"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(0);
				else if(aircraftTypeFileName.equalsIgnoreCase("FIGHTER"))		
					Main.getChoiceBoxAircraftType().getSelectionModel().select(1);
				else if(aircraftTypeFileName.equalsIgnoreCase("BUSINESS_JET"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(2);
				else if(aircraftTypeFileName.equalsIgnoreCase("TURBOPROP"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(3);
				else if(aircraftTypeFileName.equalsIgnoreCase("GENERAL_AVIATION"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(4);
				else if(aircraftTypeFileName.equalsIgnoreCase("COMMUTER"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(5);
				else if(aircraftTypeFileName.equalsIgnoreCase("ACROBATIC"))
					Main.getChoiceBoxAircraftType().getSelectionModel().select(6);
			}
		}
		
		//---------------------------------------------------------------------------------
		// REGULATIONS TYPE:
		if(Main.getChoiceBoxRegulationsType() == null)
			Main.setChoiceBoxRegulationsType(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxRegulationsType")
					);
		
		String regulationsTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@regulations");
		
		if(regulationsTypeFileName != null) { 
			if(Main.getChoiceBoxRegulationsType() != null) {
				if(regulationsTypeFileName.equalsIgnoreCase("FAR_23"))
					Main.getChoiceBoxRegulationsType().getSelectionModel().select(0);
				else if(regulationsTypeFileName.equalsIgnoreCase("FAR_25"))		
					Main.getChoiceBoxRegulationsType().getSelectionModel().select(1);
			}
		}
		
		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION:
		if(Main.getTextFieldAircraftCabinConfiguration() == null)
			Main.setTextFieldAircraftCabinConfiguration(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftCabinConfiguration")
					);

		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");

		if(cabinConfigrationFileName != null) 
			Main.getTextFieldAircraftCabinConfiguration().setText(
					dirCabinConfiguration 
					+ File.separator
					+ cabinConfigrationFileName
					);
		else
			Main.getTextFieldAircraftCabinConfiguration().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FUSELAGE:
		if(Main.getTextFieldAircraftFuselageFile() == null)
			Main.setTextFieldAircraftFuselageFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftFuselageFile")
					);

		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		if(fuselageFileName != null) 
			Main.getTextFieldAircraftFuselageFile().setText(
					dirFuselages 
					+ File.separator
					+ fuselageFileName
					);
		else
			Main.getTextFieldAircraftFuselageFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageX() == null)
			Main.setTextFieldAircraftFuselageX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftFuselageX")
					);
		if(Main.getFuselageXUnitChoiceBox() == null)
			Main.setFuselageXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldAircraftFuselageX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		
		else
			Main.getTextFieldAircraftFuselageX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageY() == null)
			Main.setTextFieldAircraftFuselageY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftFuselageY")
					);
		if(Main.getFuselageYUnitChoiceBox() == null)
			Main.setFuselageYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldAircraftFuselageY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftFuselageY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftFuselageZ() == null)
			Main.setTextFieldAircraftFuselageZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftFuselageZ")
					);
		if(Main.getFuselageZUnitChoiceBox() == null)
			Main.setFuselageZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldAircraftFuselageZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftFuselageZ().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// WING:
		if(Main.getTextFieldAircraftWingFile() == null)
			Main.setTextFieldAircraftWingFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftWingFile")
					);

		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		if(wingFileName != null)
			Main.getTextFieldAircraftWingFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ wingFileName
					);
		else
			Main.getTextFieldAircraftWingFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingX() == null)
			Main.setTextFieldAircraftWingX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftWingX")
					);
		if(Main.getWingXUnitChoiceBox() == null)
			Main.setWingXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#wingXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getWingXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getWingXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingY() == null)
			Main.setTextFieldAircraftWingY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftWingY")
					);
		if(Main.getWingYUnitChoiceBox() == null)
			Main.setWingYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#wingYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getWingYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getWingYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingZ() == null)
			Main.setTextFieldAircraftWingZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftWingZ")
					);
		if(Main.getWingZUnitChoiceBox() == null)
			Main.setWingZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#wingZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getWingZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getWingZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftWingRiggingAngle() == null)
			Main.setTextFieldAircraftWingRiggingAngle(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftWingRiggingAngle")
					);
		if(Main.getWingRiggingAngleUnitChoiceBox() == null)
			Main.setWingRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#wingRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getWing() != null) {
			
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftWingRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		if(Main.getTextFieldAircraftHorizontalTailFile() == null)
			Main.setTextFieldAircraftHorizontalTailFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftHTailFile")
					);

		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		if(hTailFileName != null)
			Main.getTextFieldAircraftHorizontalTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ hTailFileName
					);
		else
			Main.getTextFieldAircraftHorizontalTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailX() == null)
			Main.setTextFieldAircraftHorizontalTailX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftHTailX")
					);
		if(Main.gethTailXUnitChoiceBox() == null)
			Main.sethTailXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#hTailXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.gethTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.gethTailXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailY() == null)
			Main.setTextFieldAircraftHorizontalTailY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftHTailY")
					);
		if(Main.gethTailYUnitChoiceBox() == null)
			Main.sethTailYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#hTailYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.gethTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.gethTailYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailZ() == null)
			Main.setTextFieldAircraftHorizontalTailZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftHTailZ")
					);
		if(Main.gethtailZUnitChoiceBox() == null)
			Main.sethtailZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#hTailZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.gethtailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.gethtailZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftHorizontalTailRiggingAngle() == null)
			Main.setTextFieldAircraftHorizontalTailRiggingAngle(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftHTailRiggingAngle")
					);
		if(Main.gethTailRiggingAngleUnitChoiceBox() == null)
			Main.sethTailRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#hTailRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getHTail() != null) {
			
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		if(Main.getTextFieldAircraftVerticalTailFile() == null)
			Main.setTextFieldAircraftVerticalTailFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftVTailFile")
					);

		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		if(vTailFileName != null)
			Main.getTextFieldAircraftVerticalTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ vTailFileName
					);
		else
			Main.getTextFieldAircraftVerticalTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailX() == null)
			Main.setTextFieldAircraftVerticalTailX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftVTailX")
					);
		if(Main.getvTailXUnitChoiceBox() == null)
			Main.setvTailXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#vTailXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getvTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getvTailXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftVerticalTailX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailY() == null)
			Main.setTextFieldAircraftVerticalTailY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftVTailY")
					);
		if(Main.getvTailYUnitChoiceBox() == null)
			Main.setvTailYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#vTailYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getvTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getvTailYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftVerticalTailY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailZ() == null)
			Main.setTextFieldAircraftVerticalTailZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftVTailZ")
					);
		if(Main.getvTailZUnitChoiceBox() == null)
			Main.setvTailZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#vTailZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getvTailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getvTailZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftVerticalTailZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftVerticalTailRiggingAngle() == null)
			Main.setTextFieldAircraftVerticalTailRiggingAngle(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftVTailRiggingAngle")
					);
		if(Main.getvTailRiggingAngleUnitChoiceBox() == null)
			Main.setvTailRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#vTailRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getVTail() != null) {
			
			Main.getTextFieldAircraftVerticalTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftHorizontalTailRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// CANARD:
		if(Main.getTextFieldAircraftCanardFile() == null)
			Main.setTextFieldAircraftCanardFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftCanardFile")
					);

		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		if(canardFileName != null)
			Main.getTextFieldAircraftCanardFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ canardFileName
					);
		else
			Main.getTextFieldAircraftCanardFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardX() == null)
			Main.setTextFieldAircraftCanardX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftCanardX")
					);
		if(Main.getCanardXUnitChoiceBox() == null)
			Main.setCanardXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#canardXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getCanardXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getCanardXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardY() == null)
			Main.setTextFieldAircraftCanardY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftCanardY")
					);
		if(Main.getCanardYUnitChoiceBox() == null)
			Main.setCanardYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#canardYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getCanardYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getCanardYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardZ() == null)
			Main.setTextFieldAircraftCanardZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftCanardZ")
					);
		if(Main.getCanardZUnitChoiceBox() == null)
			Main.setCanardZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#canardZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getCanardZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getCanardZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftCanardRiggingAngle() == null)
			Main.setTextFieldAircraftCanardRiggingAngle(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftCanardRiggingAngle")
					);
		if(Main.getCanardRiggingAngleUnitChoiceBox() == null)
			Main.setCanardRiggingAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#canardRiggingAngleUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getCanard() != null) {
			
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				Main.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				Main.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftCanardRiggingAngle().setText(
					"NOT INITIALIZED"
					);
		//---------------------------------------------------------------------------------
		// POWER PLANT:
		if(Main.getTextFieldAircraftEngineFileList().isEmpty()) {
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineFile1")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineFile2")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineFile3")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineFile4")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineFile5")
					);
			Main.getTextFieldAircraftEngineFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineFile6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineXList().isEmpty()) {
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineX1")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineX2")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineX3")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineX4")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineX5")
					);
			Main.getTextFieldAircraftEngineXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineX6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineYList().isEmpty()) {
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineY1")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineY2")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineY3")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineY4")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineY5")
					);
			Main.getTextFieldAircraftEngineYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineY6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineZList().isEmpty()) {
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineZ1")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineZ2")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineZ3")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineZ4")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineZ5")
					);
			Main.getTextFieldAircraftEngineZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineZ6")
					);
		}
		//..........................................................................................................
		if(Main.getChoiceBoxesAircraftEnginePositonList().isEmpty()) {
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftEnginePosition1")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftEnginePosition2")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftEnginePosition3")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftEnginePosition4")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftEnginePosition5")
					);
			Main.getChoiceBoxesAircraftEnginePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftEnginePosition6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftEngineTiltList().isEmpty()) {
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineTilt1")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineTilt2")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineTilt3")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineTilt4")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineTilt5")
					);
			Main.getTextFieldAircraftEngineTiltList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftEngineTilt6")
					);
		}
		//..........................................................................................................
		if(Main.getPowerPlantXUnitChoiceBox() == null)
			Main.setPowerPlantXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#powerPlantXUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getPowerPlantXUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getPowerPlantXUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getPowerPlantYUnitChoiceBox() == null)
			Main.setPowerPlantYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#powerPlantYUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getPowerPlantYUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getPowerPlantYUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getPowerPlantZUnitChoiceBox() == null)
			Main.setPowerPlantZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#powerPlantZUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getPowerPlantZUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getPowerPlantZUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getPowerPlantTiltAngleUnitChoiceBox() == null)
			Main.setPowerPlantTiltAngleUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#powerPlantTiltingAngleUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getTiltingAngle().getUnit().toString().equalsIgnoreCase("deg"))
			Main.getPowerPlantTiltAngleUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getPowerPlant().getEngineList().get(0)
				.getTiltingAngle().getUnit().toString().equalsIgnoreCase("rad"))
			Main.getPowerPlantTiltAngleUnitChoiceBox().getSelectionModel().select(1);
		
		//..........................................................................................................
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");
		if(nodelistEngines != null) {
			for (int i = 0; i < nodelistEngines.getLength(); i++) {
				//..........................................................................................................
				Node nodeEngine  = nodelistEngines.item(i); 
				Element elementEngine = (Element) nodeEngine;
				if(elementEngine.getAttribute("file") != null)
					Main.getTextFieldAircraftEngineFileList().get(i).setText(
							dirEngines 
							+ File.separator
							+ elementEngine.getAttribute("file")	
							);
				else
					Main.getTextFieldAircraftEngineFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) 
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)

					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("BURIED")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("AFT_FUSELAGE")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(3);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("REAR_FUSELAGE")
							)
						Main.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(4);

				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getTiltingAngle()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftEngineTiltList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
		//---------------------------------------------------------------------------------
		// NACELLES:
		if(Main.getTextFieldAircraftNacelleFileList().isEmpty()) {
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleFile1")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleFile2")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleFile3")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleFile4")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleFile5")
					);
			Main.getTextFieldAircraftNacelleFileList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleFile6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftNacelleXList().isEmpty()) {
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleX1")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleX2")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleX3")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleX4")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleX5")
					);
			Main.getTextFieldAircraftNacelleXList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleX6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftNacelleYList().isEmpty()) {
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleY1")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleY2")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleY3")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleY4")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleY5")
					);
			Main.getTextFieldAircraftNacelleYList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleY6")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldAircraftNacelleZList().isEmpty()) {
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleZ1")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleZ2")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleZ3")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleZ4")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleZ5")
					);
			Main.getTextFieldAircraftNacelleZList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftNacelleZ6")
					);
		}
		//..........................................................................................................
		if(Main.getChoiceBoxesAircraftNacellePositonList().isEmpty()) {
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftNacellePosition1")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftNacellePosition2")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftNacellePosition3")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftNacellePosition4")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftNacellePosition5")
					);
			Main.getChoiceBoxesAircraftNacellePositonList().add(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftNacellePosition6")
					);
		}
		//..........................................................................................................
		if(Main.getNacelleXUnitChoiceBox() == null)
			Main.setNacelleXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#nacellesXUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getNacelleXUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getNacelleXUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getNacelleYUnitChoiceBox() == null)
			Main.setNacelleYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#nacellesYUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getNacelleYUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getNacelleYUnitChoiceBox().getSelectionModel().select(1);
		
		
		if(Main.getNacelleZUnitChoiceBox() == null)
			Main.setNacelleZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#nacelleZUnitChoiceBox")
					);
		if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
			Main.getNacelleZUnitChoiceBox().getSelectionModel().select(0);
		else if(Main.getTheAircraft()
				.getNacelles().getNacellesList().get(0)
				.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
			Main.getNacelleZUnitChoiceBox().getSelectionModel().select(1);
		
		//..........................................................................................................
		NodeList nodelistNacelles = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");
		if(nodelistNacelles != null) {
			for (int i = 0; i < nodelistNacelles.getLength(); i++) {
				//..........................................................................................................
				Node nodeNacelle  = nodelistNacelles.item(i); 
				Element elementNacelle = (Element) nodeNacelle;
				if(elementNacelle.getAttribute("file") != null)
					Main.getTextFieldAircraftNacelleFileList().get(i).setText(
							dirEngines 
							+ File.separator
							+ elementNacelle.getAttribute("file")	
							);
				else
					Main.getTextFieldAircraftNacelleFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftNacelleXList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftNacelleYList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
				else
					Main.getTextFieldAircraftNacelleZList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)
					
					if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("FUSELAGE")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("UNDERCARRIAGE_HOUSING")
							)
						Main.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(3);
					
			}
		}
		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		if(Main.getTextFieldAircraftLandingGearsFile() == null)
			Main.setTextFieldAircraftLandingGearsFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftLandingGearsFile")
					);

		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		if(landingGearsFileName != null) 
			Main.getTextFieldAircraftLandingGearsFile().setText(
					dirLandingGears 
					+ File.separator
					+ landingGearsFileName
					);
		else
			Main.getTextFieldAircraftLandingGearsFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsX() == null)
			Main.setTextFieldAircraftLandingGearsX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftLandingGearsX")
					);
		if(Main.getLandingGearsXUnitChoiceBox() == null)
			Main.setLandingGearsXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#landingGearsXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getLandingGears() != null) {
			
			Main.getTextFieldAircraftLandingGearsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getLandingGearsXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftLandingGearsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsY() == null)
			Main.setTextFieldAircraftLandingGearsY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftLandingGearsY")
					);
		if(Main.getLandingGearsYUnitChoiceBox() == null)
			Main.setLandingGearsYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#landingGearsYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getLandingGears() != null) {
			
			Main.getTextFieldAircraftLandingGearsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getLandingGearsYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftLandingGearsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftLandingGearsZ() == null)
			Main.setTextFieldAircraftLandingGearsZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftLandingGearsZ")
					);
		if(Main.getLandingGearsZUnitChoiceBox() == null)
			Main.setLandingGearsZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#landingGearsZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getLandingGears() != null) {
			
			Main.getTextFieldAircraftLandingGearsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getLandingGearsZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftLandingGearsZ().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getChoiceBoxAircraftLandingGearsPosition() == null)
			Main.setChoiceBoxAircraftLandingGearsPosition(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxAircraftLandingGearsPosition")
					);
		if(Main.getTheAircraft().getLandingGears() != null)
			
			if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("FUSELAGE")
					)
				Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("WING")
					)
				Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().select(1);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("NACELLE")
					)
				Main.getChoiceBoxAircraftLandingGearsPosition().getSelectionModel().select(2);
			
		//---------------------------------------------------------------------------------
		// SYSTEMS:
		if(Main.getTextFieldAircraftSystemsFile() == null)
			Main.setTextFieldAircraftSystemsFile(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftSystemsFile")
					);

		String systemsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//systems/@file");
		if(systemsFileName != null) 
			Main.getTextFieldAircraftSystemsFile().setText(
					dirSystems 
					+ File.separator
					+ systemsFileName
					);
		else
			Main.getTextFieldAircraftSystemsFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsX() == null)
			Main.setTextFieldAircraftSystemsX(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftSystemsX")
					);
		if(Main.getSystemsXUnitChoiceBox() == null)
			Main.setSystemsXUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#systemsXUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getSystems() != null) {
			
			Main.getTextFieldAircraftSystemsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getXApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getSystemsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getSystemsXUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftSystemsX().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsY() == null)
			Main.setTextFieldAircraftSystemsY(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftSystemsY")
					);
		if(Main.getSystemsYUnitChoiceBox() == null)
			Main.setSystemsYUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#systemsYUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getSystems() != null) {
			
			Main.getTextFieldAircraftSystemsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getYApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getSystemsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getSystemsYUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftSystemsY().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTextFieldAircraftSystemsZ() == null)
			Main.setTextFieldAircraftSystemsZ(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldAircraftSystemsZ")
					);
		if(Main.getSystemsZUnitChoiceBox() == null)
			Main.setSystemsZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#systemsZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getSystems() != null) {
			
			Main.getTextFieldAircraftSystemsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getSystems()
							.getZApexConstructionAxes()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
				Main.getSystemsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getSystems()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getSystemsZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldAircraftSystemsZ().setText(
					"NOT INITIALIZED"
					);
		
	}
	
	@SuppressWarnings("unchecked")
	public static void logFuselageFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		if(Main.getTextAreaFuselageConsoleOutput() == null)
			Main.setTextAreaFuselageConsoleOutput(
					(TextArea) Main.getMainInputManagerLayout().lookup("#FuselageOutput")
					);
		Main.getTextAreaFuselageConsoleOutput().setText(
				Main.getTheAircraft().getFuselage().getFuselageCreator().toString()
				);

		//---------------------------------------------------------------------------------
		// ADJUST CRITERION CHOICE BOX:
		if(Main.getFuselageAdjustCriterion() == null) {
			Main.setFuselageAdjustCriterion(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageAdjustCriterionChoiceBox")
					);
			
			if(Main.getTheAircraft() != null)
				Main.getFuselageAdjustCriterion().setDisable(false);
			
		}
		
		//---------------------------------------------------------------------------------
		// PRESSURIZED FLAG:
		if(Main.getFuselagePressurizedCheckBox() == null)
			Main.setFuselagePressurizedCheckBox(
					(CheckBox) Main.getMainInputManagerLayout().lookup("#fuselagePressurizedCheckBox")
					);

		if(Main.getFuselagePressurizedCheckBox() != null) 
			if(Main.getTheAircraft().getFuselage().getFuselageCreator().getPressurized() == Boolean.TRUE)
				Main.getFuselagePressurizedCheckBox().setSelected(true);
			
		
		//---------------------------------------------------------------------------------
		// DECK NUMBER:
		if(Main.getTextFieldFuselageDeckNumber() == null)
			Main.setTextFieldFuselageDeckNumber(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageDeckNumber")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageDeckNumber().setText(
					Integer.toString(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getDeckNumber()
							)
					);
		else
			Main.getTextFieldFuselageDeckNumber().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH:
		if(Main.getTextFieldFuselageLength() == null)
			Main.setTextFieldFuselageLength(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageLength")
					);
		if(Main.getFuselageLengthUnitChoiceBox() == null)
			Main.setFuselageLengthUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageLengthUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageLength().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getLenF()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getLenF().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageLengthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getLenF().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageLengthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageLength().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// SURFACE ROUGHNESS:
		if(Main.getTextFieldFuselageSurfaceRoughness() == null)
			Main.setTextFieldFuselageSurfaceRoughness(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldSurfaceRoughness")
					);
		if(Main.getFuselageRoughnessUnitChoiceBox() == null)
			Main.setFuselageRoughnessUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageRoughnessUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageSurfaceRoughness().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getRoughness()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageRoughnessUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageRoughnessUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageSurfaceRoughness().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH RATIO:
		if(Main.getTextFieldFuselageNoseLengthRatio() == null)
			Main.setTextFieldFuselageNoseLengthRatio(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseLengthRatio")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageNoseLengthRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getLenRatioNF()
					.toString()
					);
		else
			Main.getTextFieldFuselageNoseLengthRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE TIP OFFSET RATIO:
		if(Main.getTextFieldFuselageNoseTipOffset() == null)
			Main.setTextFieldFuselageNoseTipOffset(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseTipOffset")
					);
		if(Main.getFuselageNoseTipOffsetZUnitChoiceBox() == null)
			Main.setFuselageNoseTipOffsetZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageNoseTipOffsetZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageNoseTipOffset().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getHeightN()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightN().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightN().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageNoseTipOffset().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE DX CAP PERCENT:
		if(Main.getTextFieldFuselageNoseDxCap() == null)
			Main.setTextFieldFuselageNoseDxCap(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseDxCap")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageNoseDxCap().setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getDxNoseCapPercent()
							)
					);
		else
			Main.getTextFieldFuselageNoseDxCap().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD TYPE:
		if(Main.getChoiceBoxFuselageNoseWindshieldType() == null)
			Main.setChoiceBoxFuselageNoseWindshieldType(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#choiceBoxFuselageNoseWindshieldType")
					);

		if(Main.getTheAircraft().getFuselage() != null) { 
			if(Main.getChoiceBoxFuselageNoseWindshieldType() != null) {
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.DOUBLE)
					Main.getChoiceBoxFuselageNoseWindshieldType().getSelectionModel().select(0);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.FLAT_FLUSH)		
					Main.getChoiceBoxFuselageNoseWindshieldType().getSelectionModel().select(1);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.FLAT_PROTRUDING)
					Main.getChoiceBoxFuselageNoseWindshieldType().getSelectionModel().select(2);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.SINGLE_ROUND)
					Main.getChoiceBoxFuselageNoseWindshieldType().getSelectionModel().select(3);
				else if(Main.getTheAircraft().getFuselage().getFuselageCreator().getWindshieldType() == WindshieldTypeEnum.SINGLE_SHARP)
					Main.getChoiceBoxFuselageNoseWindshieldType().getSelectionModel().select(4);
			}
		}
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD WIDTH:
		if(Main.getTextFieldFuselageNoseWindshieldWidth() == null)
			Main.setTextFieldFuselageNoseWindshieldWidth(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseWindshieldWidth")
					);
		if(Main.getFuselageWindshieldWidthUnitChoiceBox() == null)
			Main.setFuselageWindshieldWidthUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageWindshieldWidthUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageNoseWindshieldWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getWindshieldWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageNoseWindshieldWidth().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD HEIGHT:
		if(Main.getTextFieldFuselageNoseWindshieldHeight() == null)
			Main.setTextFieldFuselageNoseWindshieldHeight(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseWindshieldHeight")
					);
		if(Main.getFuselageWindshieldHeightUnitChoiceBox() == null)
			Main.setFuselageWindshieldHeightUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageWindshieldHeightUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageNoseWindshieldHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getWindshieldHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageNoseWindshieldHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION HEIGHT TO TOTAL SECTION HEIGHT RATIO:
		if(Main.getTextFieldFuselageNoseMidSectionHeight() == null)
			Main.setTextFieldFuselageNoseMidSectionHeight(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseMidSectionHeight")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageNoseMidSectionHeight().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionNoseMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			Main.getTextFieldFuselageNoseMidSectionHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO UPPER:
		if(Main.getTextFieldFuselageNoseMidSectionRhoUpper() == null)
			Main.setTextFieldFuselageNoseMidSectionRhoUpper(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseMidSectionRhoUpper")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageNoseMidSectionRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidNoseRhoUpper()
					.toString()
					);
		else
			Main.getTextFieldFuselageNoseMidSectionRhoUpper().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO LOWER:
		if(Main.getTextFieldFuselageNoseMidSectionRhoLower() == null)
			Main.setTextFieldFuselageNoseMidSectionRhoLower(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageNoseMidSectionRhoLower")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageNoseMidSectionRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidNoseRhoLower()
					.toString()
					);
		else
			Main.getTextFieldFuselageNoseMidSectionRhoLower().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER LENGTH RATIO:
		if(Main.getTextFieldFuselageCylinderLengthRatio() == null)
			Main.setTextFieldFuselageCylinderLengthRatio(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderLengthRatio")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageCylinderLengthRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getLenRatioCF()
					.toString()
					);
		else
			Main.getTextFieldFuselageCylinderLengthRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION WIDTH:
		if(Main.getTextFieldFuselageCylinderSectionWidth() == null)
			Main.setTextFieldFuselageCylinderSectionWidth(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderSectionWidth")
					);
		if(Main.getFuselageCylinderSectionWidthUnitChoiceBox() == null)
			Main.setFuselageCylinderSectionWidthUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageCylinderSectionWidthUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageCylinderSectionWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getSectionCylinderWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageCylinderSectionWidth().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT:
		if(Main.getTextFieldFuselageCylinderSectionHeight() == null)
			Main.setTextFieldFuselageCylinderSectionHeight(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderSectionHeight")
					);
		if(Main.getFuselageCylinderSectionHeightUnitChoiceBox() == null)
			Main.setFuselageCylinderSectionHeightUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageCylinderSectionHeightUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageCylinderSectionHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getSectionCylinderHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageCylinderSectionHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT FROM GROUND:
		if(Main.getTextFieldFuselageCylinderHeightFromGround() == null)
			Main.setTextFieldFuselageCylinderHeightFromGround(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderHeightFromGround")
					);
		if(Main.getFuselageHeightFromGroundUnitChoiceBox() == null)
			Main.setFuselageHeightFromGroundUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageHeightFromGroundUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageCylinderHeightFromGround().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getHeightFromGround()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageCylinderHeightFromGround().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT TO TOTAL HEIGH RATIO:
		if(Main.getTextFieldFuselageCylinderSectionHeightRatio() == null)
			Main.setTextFieldFuselageCylinderSectionHeightRatio(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderSectionHeightRatio")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageCylinderSectionHeightRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionCylinderLowerToTotalHeightRatio()
					.toString()
					);
		else
			Main.getTextFieldFuselageCylinderSectionHeightRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO UPPER:
		if(Main.getTextFieldFuselageCylinderSectionRhoUpper() == null)
			Main.setTextFieldFuselageCylinderSectionRhoUpper(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderSectionRhoUpper")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageCylinderSectionRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionCylinderRhoUpper()
					.toString()
					);
		else
			Main.getTextFieldFuselageCylinderSectionRhoUpper().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO LOWER:
		if(Main.getTextFieldFuselageCylinderSectionRhoLower() == null)
			Main.setTextFieldFuselageCylinderSectionRhoLower(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageCylinderSectionRhoLower")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageCylinderSectionRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionCylinderRhoLower()
					.toString()
					);
		else
			Main.getTextFieldFuselageCylinderSectionRhoLower().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE TIP OFFSET:
		if(Main.getTextFieldFuselageTailTipOffset() == null)
			Main.setTextFieldFuselageTailTipOffset(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageTailTipOffset")
					);
		if(Main.getFuselageTailTipOffsetZUnitChoiceBox() == null)
			Main.setFuselageTailTipOffsetZUnitChoiceBox(
					(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageTailTipOffsetZUnitChoiceBox")
					);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			
			Main.getTextFieldFuselageTailTipOffset().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getHeightT()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightT().getUnit().toString().equalsIgnoreCase("m"))
				Main.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getHeightT().getUnit().toString().equalsIgnoreCase("ft"))
				Main.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			Main.getTextFieldFuselageTailTipOffset().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE DX CAP PERCENT:
		if(Main.getTextFieldFuselageTailDxCap() == null)
			Main.setTextFieldFuselageTailDxCap(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageTailDxCap")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageTailDxCap().setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							.getFuselageCreator()
							.getDxTailCapPercent()
							)
					);
		else
			Main.getTextFieldFuselageTailDxCap().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION HEIGHT TO TOTAL HEIGHT RATIO:
		if(Main.getTextFieldFuselageTailMidSectionHeight() == null)
			Main.setTextFieldFuselageTailMidSectionHeight(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageTailMidSectionHeight")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageTailMidSectionHeight().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionTailMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			Main.getTextFieldFuselageTailMidSectionHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO UPPER:
		if(Main.getTextFieldFuselageTailMidRhoUpper() == null)
			Main.setTextFieldFuselageTailMidRhoUpper(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageTailMidRhoUpper")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageTailMidRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidTailRhoUpper()
					.toString()
					);
		else
			Main.getTextFieldFuselageTailMidRhoUpper().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO LOWER:
		if(Main.getTextFieldFuselageTailMidRhoLower() == null)
			Main.setTextFieldFuselageTailMidRhoLower(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageTailMidRhoLower")
					);
		if(Main.getTheAircraft().getFuselage() != null)
			Main.getTextFieldFuselageTailMidRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					.getFuselageCreator()
					.getSectionMidTailRhoLower()
					.toString()
					);
		else
			Main.getTextFieldFuselageTailMidRhoLower().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// SPOLERS:
		if(Main.getTextFieldSpoilersXInboradList().isEmpty()) {
			Main.getTextFieldSpoilersXInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXin1")
					);
			Main.getTextFieldSpoilersXInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXin2")
					);
			Main.getTextFieldSpoilersXInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXin3")
					);
			Main.getTextFieldSpoilersXInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXin4")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldSpoilersXOutboradList().isEmpty()) {
			Main.getTextFieldSpoilersXOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXout1")
					);
			Main.getTextFieldSpoilersXOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXout2")
					);
			Main.getTextFieldSpoilersXOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXout3")
					);
			Main.getTextFieldSpoilersXOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerXout4")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldSpoilersYInboradList().isEmpty()) {
			Main.getTextFieldSpoilersYInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYin1")
					);
			Main.getTextFieldSpoilersYInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYin2")
					);
			Main.getTextFieldSpoilersYInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYin3")
					);
			Main.getTextFieldSpoilersYInboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYin4")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldSpoilersYOutboradList().isEmpty()) {
			Main.getTextFieldSpoilersYOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYout1")
					);
			Main.getTextFieldSpoilersYOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYout2")
					);
			Main.getTextFieldSpoilersYOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYout3")
					);
			Main.getTextFieldSpoilersYOutboradList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerYout4")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldSpoilersMinDeflectionList().isEmpty()) {
			Main.getTextFieldSpoilersMinDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMin1")
					);
			Main.getTextFieldSpoilersMinDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMin2")
					);
			Main.getTextFieldSpoilersMinDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMin3")
					);
			Main.getTextFieldSpoilersMinDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMin4")
					);
		}
		//..........................................................................................................
		if(Main.getTextFieldSpoilersMaxDeflectionList().isEmpty()) {
			Main.getTextFieldSpoilersMaxDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMax1")
					);
			Main.getTextFieldSpoilersMaxDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMax2")
					);
			Main.getTextFieldSpoilersMaxDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMax3")
					);
			Main.getTextFieldSpoilersMaxDeflectionList().add(
					(TextField) Main.getMainInputManagerLayout().lookup("#textFieldFuselageSpoilerDeltaMax4")
					);
		}
		//..........................................................................................................
		if(!Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().isEmpty()) {
			
			if(Main.getFuselageSpoilersDeltaMinUnitChoiceBox() == null)
				Main.setFuselageSpoilersDeltaMinUnitChoiceBox(
						(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageSpoilersDeltaMinUnitChoiceBox")
						);
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMinimumDeflection().toString().equalsIgnoreCase("°"))
				Main.getFuselageSpoilersDeltaMinUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMinimumDeflection().toString().equalsIgnoreCase("rad"))
				Main.getFuselageSpoilersDeltaMinUnitChoiceBox().getSelectionModel().select(1);


			if(Main.getFuselageSpoilersDeltaMaxUnitChoiceBox() == null)
				Main.setFuselageSpoilersDeltaMaxUnitChoiceBox(
						(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageSpoilersDeltaMaxUnitChoiceBox")
						);
			if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMaximumDeflection().toString().equalsIgnoreCase("°"))
				Main.getFuselageSpoilersDeltaMaxUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage().getFuselageCreator()
					.getSpoilers().get(0).getMaximumDeflection().toString().equalsIgnoreCase("rad"))
				Main.getFuselageSpoilersDeltaMaxUnitChoiceBox().getSelectionModel().select(1);

			//..........................................................................................................
			for (int i = 0; i < Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().size(); i++) {
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationChordwisePosition() != null)
					Main.getTextFieldSpoilersXInboradList().get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationChordwisePosition().toString()
							);
				else
					Main.getTextFieldSpoilersXInboradList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationChordwisePosition() != null)
					Main.getTextFieldSpoilersXOutboradList().get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationChordwisePosition().toString()
							);
				else
					Main.getTextFieldSpoilersXOutboradList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationSpanwisePosition() != null)
					Main.getTextFieldSpoilersYInboradList().get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationSpanwisePosition().toString()
							);
				else
					Main.getTextFieldSpoilersYInboradList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationSpanwisePosition() != null)
					Main.getTextFieldSpoilersYOutboradList().get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getOuterStationSpanwisePosition().toString()
							);
				else
					Main.getTextFieldSpoilersYOutboradList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationSpanwisePosition() != null)
					Main.getTextFieldSpoilersYInboradList().get(i).setText(
							Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getInnerStationSpanwisePosition().toString()
							);
				else
					Main.getTextFieldSpoilersYInboradList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMinimumDeflection() != null)
					Main.getTextFieldSpoilersMinDeflectionList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMinimumDeflection().getEstimatedValue()
									)
							);
				else
					Main.getTextFieldSpoilersMinDeflectionList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMaximumDeflection() != null)
					Main.getTextFieldSpoilersMaxDeflectionList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getFuselage().getFuselageCreator().getSpoilers().get(i).getMaximumDeflection().getEstimatedValue()
									)
							);
				else
					Main.getTextFieldSpoilersMaxDeflectionList().get(i).setText(
							"NOT INITIALIZED"
							);
			}
		}
	}
	
}

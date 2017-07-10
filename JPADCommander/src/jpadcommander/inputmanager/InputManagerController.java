package jpadcommander.inputmanager;

import java.io.IOException;

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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
	ObservableList<String> angleUnitsList = FXCollections.observableArrayList("�","rad" );
	
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
	
	@FXML
	private void showInputManagerAircraftFromFileContent() throws IOException {
		
		Main.setIsAircraftFormFile(Boolean.TRUE);
		Main.getProgressBar().setProgress(0.0);
		Main.getStatusBar().setText("Ready!");
		
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
		Main.getProgressBar().setProgress(0.0);
		Main.getStatusBar().setText("Ready!");
		
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
	
}

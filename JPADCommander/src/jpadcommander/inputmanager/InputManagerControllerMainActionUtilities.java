package jpadcommander.inputmanager;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import it.unina.daf.jpadcad.CADManager;
import it.unina.daf.jpadcad.ICADManager;
import it.unina.daf.jpadcad.enums.EngineCADComponentsEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.enums.XSpacingType;
import it.unina.daf.jpadcad.occ.OCCUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jpadcommander.Main;
import writers.AircraftSaveDirectives;
import writers.JPADStaticWriteUtils;

public class InputManagerControllerMainActionUtilities {

	//---------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	public InputManagerController theController;
	
	//---------------------------------------------------------------------------------
	// BUILDER
	public InputManagerControllerMainActionUtilities(InputManagerController controller) {
		
		this.theController = controller;
		
	}
	
	//---------------------------------------------------------------------------------
	// METHODS
	
	public void newAircraftImplementation() {
		
		//..................................................................................
		// PRELIMINARY OPERATIONS
		Main.getStatusBar().textProperty().unbind();
		Main.getProgressBar().progressProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getStatusBar().setText("Ready!");
		Main.getProgressBar().setProgress(0.0);
		Main.getTaskPercentage().setText("");

		theController.getTextFieldAircraftInputFile().clear();
		
		//..................................................................................
		// AIRCRAFT
		cleanAircraftData();
		//..................................................................................
		// FUSELAGE
		cleanFuselageData();
		//..................................................................................
		// CABIN CONFIGURATION
		cleanCabinConfigurationData();
		//..................................................................................
		// WING
		cleanWingData();
		//..................................................................................
		// HTAIL
		cleanHTailData();
		//..................................................................................
		// VTAIL
		cleanVTailData();
		//..................................................................................
		// CANARD
		cleanCanardData();
		//..................................................................................
		// NACELLE
		cleanNacelleData();
		//..................................................................................
		// POWER PLANT
		cleanPowerPlantData();
		//..................................................................................
		// LANDING GEARS
		cleanLandingGearsData();
		//..................................................................................
		// 3D VIEW
		clean3DViewData();
		
		Main.setTheAircraft(null);

		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
		ObjectProperty<CADManager> cadManager = new SimpleObjectProperty<>();
		ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();
		
		try {
			aircraftSavedFlag.set(Main.getAircraftSaved());
			theController.getSaveAircraftButton().disableProperty().bind(
					Bindings.equal(aircraftSavedFlag, true).or(Bindings.isNull(aircraft))
					);
		} catch (Exception e) {
			theController.getSaveAircraftButton().setDisable(true);
		}

		try {
			theController.getUpdateAircraftDataButton().disableProperty().bind(
					Bindings.isNull(aircraft)
					);
			theController.getUpdateAircraftDataFromFileComboBox().disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			theController.getUpdateAircraftDataButton().setDisable(true);
			theController.getUpdateAircraftDataFromFileComboBox().setDisable(true);
		}
		
		try {
			aircraft.set(Main.getTheAircraft());
			theController.getNewAircraftButton().disableProperty().bind(
					Bindings.isNull(aircraft)
					);
		} catch (Exception e) {
			theController.getNewAircraftButton().setDisable(true);
		}
		
		try {
			cadManager.set(Main.getTheCADManager());
			theController.getChooseCADConfigurationFileButton().disableProperty().bind(
					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
					);
			theController.getUpdateCAD3DViewButton().disableProperty().bind(
					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
					);
			theController.getSaveCADToFileButton().disableProperty().bind(
					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
					);
		} catch (Exception e) {
			theController.getChooseCADConfigurationFileButton().setDisable(true);
			theController.getUpdateCAD3DViewButton().setDisable(true);
			theController.getSaveCADToFileButton().setDisable(true);
		}
		
		theController.getInputManagerControllerSecondaryActionUtilities().cad3DViewFieldsUnbind();
		theController.getInputManagerControllerSecondaryActionUtilities().disableCAD3DViewFields(false);

	}

	private void cleanAircraftData() {
		
		theController.getAircraftTypeChoiceBox().getSelectionModel().clearSelection();
		theController.getRegulationsTypeChoiceBox().getSelectionModel().clearSelection();
		
		// cabin configuration
		theController.getTextFieldAircraftCabinConfigurationFile().clear();
		
		// fuselage
		theController.getTextFieldAircraftFuselageFile().clear();
		theController.getTextFieldAircraftFuselageX().clear();
		theController.getFuselageXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftFuselageY().clear();
		theController.getFuselageYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftFuselageZ().clear();
		theController.getFuselageZUnitChoiceBox().getSelectionModel().clearSelection();
		
		// wing
		theController.getTextFieldAircraftWingFile().clear();
		theController.getTextFieldAircraftWingX().clear();
		theController.getWingXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftWingY().clear();
		theController.getWingYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftWingZ().clear();
		theController.getWingZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftWingRiggingAngle().clear();
		theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// hTail
		theController.getTextFieldAircraftHTailFile().clear();
		theController.getTextFieldAircraftHTailX().clear();
		theController.gethTailXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftHTailY().clear();
		theController.gethTailYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftHTailZ().clear();
		theController.getHtailZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftHTailRiggingAngle().clear();
		theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// vTail
		theController.getTextFieldAircraftVTailFile().clear();
		theController.getTextFieldAircraftVTailX().clear();
		theController.getvTailXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftVTailY().clear();
		theController.getvTailYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftVTailZ().clear();
		theController.getvTailZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftVTailRiggingAngle().clear();
		theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// canard
		theController.getTextFieldAircraftCanardFile().clear();
		theController.getTextFieldAircraftCanardX().clear();
		theController.getCanardXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftCanardY().clear();
		theController.getCanardYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftCanardZ().clear();
		theController.getCanardZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftCanardRiggingAngle().clear();
		theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().clearSelection();
		
		// Power Plant
		theController.getTextFieldAircraftEngineFile1().clear();
		theController.getTextFieldAircraftEngineX1().clear();
		theController.getTextFieldAircraftEngineY1().clear();
		theController.getTextFieldAircraftEngineZ1().clear();
		theController.getTextFieldAircraftEngineTilt1().clear();
		theController.getEngineX1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEngineY1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEngineZ1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEngineTilt1AngleUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEngineMountingPositionTypeChoiceBox1().getSelectionModel().clearSelection();
		theController.getTextFieldsAircraftEngineFileList().stream().forEach(t -> t.clear());
		theController.getTextFieldAircraftEngineXList().stream().forEach(t -> t.clear());
		theController.getTextFieldAircraftEngineYList().stream().forEach(t -> t.clear());
		theController.getTextFieldAircraftEngineZList().stream().forEach(t -> t.clear());
		theController.getChoiceBoxesAircraftEnginePositonList().stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		theController.getTextFieldAircraftEngineTiltList().stream().forEach(t -> t.clear());
		theController.getChoiceBoxAircraftEngineXUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getChoiceBoxAircraftEngineYUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getChoiceBoxAircraftEngineZUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getChoiceBoxAircraftEngineTiltUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getTabPaneAircraftEngines().getTabs().remove(1, theController.getTabPaneAircraftEngines().getTabs().size());
		
		// Nacelle
		theController.getTextFieldAircraftNacelleFile1().clear();
		theController.getTextFieldAircraftNacelleX1().clear();
		theController.getTextFieldAircraftNacelleY1().clear();
		theController.getTextFieldAircraftNacelleZ1().clear();
		theController.getNacelleMountingPositionTypeChoiceBox1().getSelectionModel().clearSelection();
		theController.getNacelleX1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getNacelleY1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getNacelleZ1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldsAircraftNacelleFileList().stream().forEach(t -> t.clear());
		theController.getTextFieldAircraftNacelleXList().stream().forEach(t -> t.clear());
		theController.getTextFieldAircraftNacelleYList().stream().forEach(t -> t.clear());
		theController.getTextFieldAircraftNacelleZList().stream().forEach(t -> t.clear());
		theController.getChoiceBoxesAircraftNacellePositonList().stream().forEach(ep -> ep.getSelectionModel().clearSelection());
		theController.getChoiceBoxAircraftNacelleXUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getChoiceBoxAircraftNacelleYUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getChoiceBoxAircraftNacelleZUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		theController.getTabPaneAircraftNacelles().getTabs().remove(1, theController.getTabPaneAircraftNacelles().getTabs().size());
		
		// Landing Gears
		theController.getTextFieldAircraftLandingGearsFile().clear();
		theController.getTextFieldAircraftNoseLandingGearsX().clear();
		theController.getNoseLandingGearsXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftNoseLandingGearsY().clear();
		theController.getNoseLandingGearsYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftNoseLandingGearsZ().clear();
		theController.getNoseLandingGearsZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftMainLandingGearsX().clear();
		theController.getMainLandingGearsXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftMainLandingGearsY().clear();
		theController.getMainLandingGearsYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftMainLandingGearsZ().clear();
		theController.getMainLandingGearsZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().clearSelection();
		
		// Systems
		theController.getSystemsPrimaryElectricalTypeChoiceBox().getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		theController.getTextAreaAircraftConsoleOutput().clear();
		theController.getAircraftTopViewPane().getChildren().clear();
		theController.getAircraftSideViewPane().getChildren().clear();
		theController.getAircraftFrontViewPane().getChildren().clear();
		
	}
	
	private void cleanFuselageData() {
		
//		theController.getFuselageAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getFuselageAdjustCriterionChoiceBox().getSelectionModel().select(0);
		theController.getFuselageAdjustCriterionChoiceBox().setDisable(true);
		
		// Pressurized
		theController.getFuselagePressurizedCheckBox().setSelected(false);
		
		// Global Data
		theController.getTextFieldFuselageDeckNumber().clear();
		theController.getTextFieldFuselageLength().clear();
		theController.getFuselageLengthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageSurfaceRoughness().clear();
		theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().clearSelection();
		
		// Nose Trunk
		theController.getTextFieldFuselageNoseLengthRatio().clear();
		theController.getTextFieldFuselageNoseTipOffset().clear();
		theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageNoseDxCap().clear();
		theController.getWindshieldTypeChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageNoseWindshieldHeight().clear();
		theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageNoseWindshieldWidth().clear();
		theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageNoseMidSectionHeight().clear();
		theController.getTextFieldFuselageNoseMidSectionRhoUpper().clear();
		theController.getTextFieldFuselageNoseMidSectionRhoLower().clear();

		// Cylindrical Trunk
		theController.getTextFieldFuselageCylinderLengthRatio().clear();
		theController.getTextFieldFuselageCylinderSectionWidth().clear();
		theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().clearSelection();		
		theController.getTextFieldFuselageCylinderSectionHeight().clear();
		theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageCylinderHeightFromGround().clear();
		theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageCylinderSectionHeightRatio().clear();
		theController.getTextFieldFuselageCylinderSectionRhoUpper().clear();
		theController.getTextFieldFuselageCylinderSectionRhoLower().clear();
		
		// Tail Trunk
		theController.getTextFieldFuselageTailTipOffset().clear();
		theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldFuselageTailDxCap().clear();
		theController.getTextFieldFuselageTailMidSectionHeight().clear();
		theController.getTextFieldFuselageTailMidRhoUpper().clear();
		theController.getTextFieldFuselageTailMidRhoLower().clear();
		
		// Spoilers
		if(!Main.getTheAircraft().getFuselage().getSpoilers().isEmpty()) {
			theController.getFuselageSpoiler1DeltaMaxUnitChoiceBox().getSelectionModel().clearSelection();
			theController.getFuselageSpoiler1DeltaMinUnitChoiceBox().getSelectionModel().clearSelection();	
			theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().stream().forEach(t -> t.clear());
			theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().stream().forEach(t -> t.clear());
			theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().stream().forEach(t -> t.clear());
			theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().stream().forEach(t -> t.clear());
			theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().stream().forEach(t -> t.clear());
			theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().stream().forEach(t -> t.clear());
			theController.getTabPaneFuselageSpoilers().getTabs().remove(1, theController.getTabPaneFuselageSpoilers().getTabs().size());
		}

		// 3 View and TextArea
		theController.getTextAreaFuselageConsoleOutput().clear();
		theController.getFuselageTopViewPane().getChildren().clear();
		theController.getFuselageSideViewPane().getChildren().clear();
		theController.getFuselageFrontViewPane().getChildren().clear();

	}
	
	private void cleanCabinConfigurationData() {
		
		theController.getTextFieldActualPassengersNumber().clear();
		theController.getTextFieldFlightCrewNumber().clear();
		theController.getTextFieldClassesNumber().clear();
		theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().clearSelection();
		theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().clearSelection();
		theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().clearSelection();
		theController.getTextFieldAislesNumber().clear();
		theController.getTextFieldXCoordinateFirstRow().clear();
		theController.getTextFieldNumberOfBrakesEconomy().clear();
		theController.getTextFieldNumberOfBrakesBusiness().clear();
		theController.getTextFieldNumberOfBrakesFirst().clear();
		theController.getTextFieldNumberOfRowsEconomy().clear();
		theController.getTextFieldNumberOfRowsBusiness().clear();
		theController.getTextFieldNumberOfRowsFirst().clear();
		theController.getTextFieldNumberOfColumnsEconomy().clear();
		theController.getTextFieldNumberOfColumnsBusiness().clear();
		theController.getTextFieldNumberOfColumnsFirst().clear();
		theController.getTextFieldSeatsPitchEconomy().clear();
		theController.getTextFieldSeatsPitchBusiness().clear();
		theController.getTextFieldSeatsPitchFirst().clear();
		theController.getTextFieldSeatsWidthEconomy().clear();
		theController.getTextFieldSeatsWidthBusiness().clear();
		theController.getTextFieldSeatsWidthFirst().clear();
		theController.getTextFieldDistanceFromWallEconomy().clear();
		theController.getTextFieldDistanceFromWallBusiness().clear();
		theController.getTextFieldDistanceFromWallFirst().clear();
		theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		theController.getTextAreaCabinConfigurationConsoleOutput().clear();
		theController.getCabinConfigurationSeatMapPane().getChildren().clear();
		
	}
	
	private void cleanWingData() {
		
//		theController.getWingAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getWingAdjustCriterionChoiceBox().getSelectionModel().select(0);
		theController.getWingAdjustCriterionChoiceBox().setDisable(true);
		theController.getEquivalentWingCheckBox().setSelected(false);
		theController.getTextFieldWingMainSparAdimensionalPosition().clear();
		theController.getTextFieldWingSecondarySparAdimensionalPosition().clear();
		theController.getTextFieldWingRoughness().clear();
		theController.getTextFieldWingWingletHeight().clear();
		theController.getTextFieldEquivalentWingArea().clear();
		theController.getTextFieldEquivalentWingAspectRatio().clear();
		theController.getTextFieldEquivalentWingKinkPosition().clear();
		theController.getTextFieldEquivalentWingSweepLeadingEdge().clear();
		theController.getTextFieldEquivalentWingTwistAtTip().clear();
		theController.getTextFieldEquivalentWingDihedral().clear();
		theController.getTextFieldEquivalentWingTaperRatio().clear();
		theController.getTextFieldEquivalentWingRootXOffsetLE().clear();
		theController.getTextFieldEquivalentWingRootXOffsetTE().clear();
		theController.getTextFieldEquivalentWingAirfoilRootPath().clear();
		theController.getTextFieldEquivalentWingAirfoilKinkPath().clear();
		theController.getTextFieldEquivalentWingAirfoilTipPath().clear();
		theController.getWingRoughnessUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getWingMinimumDeflectionAngleAileron1UnitChoiceBox().getSelectionModel().clearSelection();
		theController.getWingMaximumDeflectionAngleAileron1UnitChoiceBox().getSelectionModel().clearSelection();
		
		// View and TextArea
		theController.getTextAreaWingConsoleOutput().clear();
		theController.getWingPlanformPane().getChildren().clear();
		theController.getEquivalentWingPane().getChildren().clear();
		for (int i=2; i < theController.getTabPaneWingViewAndAirfoils().getTabs().size(); i++)
			theController.getTabPaneWingViewAndAirfoils().getTabs().remove(i);
		
		// panels
		theController.getTextFieldWingSpanPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingSpanPanelList().size() > 1)
			theController.getTextFieldWingSpanPanelList().subList(1, theController.getTextFieldWingSpanPanelList().size()).clear();
		theController.getTextFieldWingSweepLEPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingSweepLEPanelList().size() > 1)
			theController.getTextFieldWingSweepLEPanelList().subList(1, theController.getTextFieldWingSweepLEPanelList().size()).clear();
		theController.getTextFieldWingDihedralPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingDihedralPanelList().size() > 1)
			theController.getTextFieldWingDihedralPanelList().subList(1, theController.getTextFieldWingDihedralPanelList().size()).clear();
		theController.getTextFieldWingInnerChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerChordPanelList().size() > 1)
			theController.getTextFieldWingInnerChordPanelList().subList(1, theController.getTextFieldWingInnerChordPanelList().size()).clear();
		theController.getTextFieldWingInnerTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerTwistPanelList().size() > 1)
			theController.getTextFieldWingInnerTwistPanelList().subList(1, theController.getTextFieldWingInnerTwistPanelList().size()).clear();
		theController.getTextFieldWingInnerAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerAirfoilPanelList().size() > 1)
			theController.getTextFieldWingInnerAirfoilPanelList().subList(1, theController.getTextFieldWingInnerAirfoilPanelList().size()).clear();
		theController.getTextFieldWingOuterChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterChordPanelList().size() > 1)
			theController.getTextFieldWingOuterChordPanelList().subList(1, theController.getTextFieldWingOuterChordPanelList().size()).clear();
		theController.getTextFieldWingOuterTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterTwistPanelList().size() > 1)
			theController.getTextFieldWingOuterTwistPanelList().subList(1, theController.getTextFieldWingOuterTwistPanelList().size()).clear();
		theController.getTextFieldWingOuterAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterAirfoilPanelList().size() > 1)
			theController.getTextFieldWingOuterAirfoilPanelList().subList(1, theController.getTextFieldWingOuterAirfoilPanelList().size()).clear();
		theController.getCheckBoxWingLinkedToPreviousPanelList().stream().forEach(cb -> cb.selectedProperty().set(false));
		if (theController.getCheckBoxWingLinkedToPreviousPanelList().size() > 0)
			theController.getCheckBoxWingLinkedToPreviousPanelList().subList(0, theController.getCheckBoxWingLinkedToPreviousPanelList().size()).clear();
		theController.getChoiceBoxWingSpanPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingSpanPanelUnitList().size() > 1)
			theController.getChoiceBoxWingSpanPanelUnitList().subList(1, theController.getChoiceBoxWingSpanPanelUnitList().size()).clear();
		theController.getChoiceBoxWingSweepLEPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingSweepLEPanelUnitList().size() > 1)
			theController.getChoiceBoxWingSweepLEPanelUnitList().subList(1, theController.getChoiceBoxWingSweepLEPanelUnitList().size()).clear();
		theController.getChoiceBoxWingDihedralPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingDihedralPanelUnitList().size() > 1)
			theController.getChoiceBoxWingDihedralPanelUnitList().subList(1, theController.getChoiceBoxWingDihedralPanelUnitList().size()).clear();
		theController.getChoiceBoxWingInnerChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingInnerChordPanelUnitList().size() > 1)
			theController.getChoiceBoxWingInnerChordPanelUnitList().subList(1, theController.getChoiceBoxWingInnerChordPanelUnitList().size()).clear();
		theController.getChoiceBoxWingInnerTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingInnerTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxWingInnerTwistPanelUnitList().subList(1, theController.getChoiceBoxWingInnerTwistPanelUnitList().size()).clear();
		theController.getChoiceBoxWingOuterChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingOuterChordPanelUnitList().size() > 1)
			theController.getChoiceBoxWingOuterChordPanelUnitList().subList(1, theController.getChoiceBoxWingOuterChordPanelUnitList().size()).clear();
		theController.getChoiceBoxWingOuterTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingOuterTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxWingOuterTwistPanelUnitList().subList(1, theController.getChoiceBoxWingOuterTwistPanelUnitList().size()).clear();
		if (theController.getDetailButtonWingInnerAirfoilList().size() > 1)
			theController.getDetailButtonWingInnerAirfoilList().subList(1, theController.getDetailButtonWingInnerAirfoilList().size()).clear();
		if (theController.getDetailButtonWingOuterAirfoilList().size() > 1)
			theController.getDetailButtonWingOuterAirfoilList().subList(1, theController.getDetailButtonWingOuterAirfoilList().size()).clear();
		theController.getTabPaneWingPanels().getTabs().remove(1, theController.getTabPaneWingPanels().getTabs().size());
		
		// flaps
		theController.getTextFieldWingInnerPositionFlapList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerPositionFlapList().size() > 1)
			theController.getTextFieldWingInnerPositionFlapList().subList(1, theController.getTextFieldWingInnerPositionFlapList().size()).clear();
		theController.getTextFieldWingOuterPositionFlapList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterPositionFlapList().size() > 1)
			theController.getTextFieldWingOuterPositionFlapList().subList(1, theController.getTextFieldWingOuterPositionFlapList().size()).clear();
		theController.getTextFieldWingInnerChordRatioFlapList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerChordRatioFlapList().size() > 1)
			theController.getTextFieldWingInnerChordRatioFlapList().subList(1, theController.getTextFieldWingInnerChordRatioFlapList().size()).clear();
		theController.getTextFieldWingOuterChordRatioFlapList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterChordRatioFlapList().size() > 1)
			theController.getTextFieldWingOuterChordRatioFlapList().subList(1, theController.getTextFieldWingOuterChordRatioFlapList().size()).clear();
		theController.getTextFieldWingMinimumDeflectionAngleFlapList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMinimumDeflectionAngleFlapList().size() > 1)
			theController.getTextFieldWingMinimumDeflectionAngleFlapList().subList(1, theController.getTextFieldWingMinimumDeflectionAngleFlapList().size()).clear();
		theController.getTextFieldWingMaximumDeflectionAngleFlapList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMaximumDeflectionAngleFlapList().size() > 1)
			theController.getTextFieldWingMaximumDeflectionAngleFlapList().subList(1, theController.getTextFieldWingMaximumDeflectionAngleFlapList().size()).clear();
		theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().size() > 1)
			theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().subList(1, theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().size()).clear();
		theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().size() > 1)
			theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().subList(1, theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().size()).clear();
		theController.getTabPaneWingFlaps().getTabs().remove(1, theController.getTabPaneWingFlaps().getTabs().size());
		
		// slats
		theController.getTextFieldWingInnerPositionSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerPositionSlatList().size() > 1)
			theController.getTextFieldWingInnerPositionSlatList().subList(1, theController.getTextFieldWingInnerPositionSlatList().size()).clear();
		theController.getTextFieldWingOuterPositionSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterPositionSlatList().size() > 1)
			theController.getTextFieldWingOuterPositionSlatList().subList(1, theController.getTextFieldWingOuterPositionSlatList().size()).clear();
		theController.getTextFieldWingInnerChordRatioSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerChordRatioSlatList().size() > 1)
			theController.getTextFieldWingInnerChordRatioSlatList().subList(1, theController.getTextFieldWingInnerChordRatioSlatList().size()).clear();
		theController.getTextFieldWingOuterChordRatioSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterChordRatioSlatList().size() > 1)
			theController.getTextFieldWingOuterChordRatioSlatList().subList(1, theController.getTextFieldWingOuterChordRatioSlatList().size()).clear();
		theController.getTextFieldWingExtensionRatioSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingExtensionRatioSlatList().size() > 1)
			theController.getTextFieldWingExtensionRatioSlatList().subList(1, theController.getTextFieldWingExtensionRatioSlatList().size()).clear();
		theController.getTextFieldWingMinimumDeflectionAngleSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMinimumDeflectionAngleSlatList().size() > 1)
			theController.getTextFieldWingMinimumDeflectionAngleSlatList().subList(1, theController.getTextFieldWingMinimumDeflectionAngleSlatList().size()).clear();
		theController.getTextFieldWingMaximumDeflectionAngleSlatList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMaximumDeflectionAngleSlatList().size() > 1)
			theController.getTextFieldWingMaximumDeflectionAngleSlatList().subList(1, theController.getTextFieldWingMaximumDeflectionAngleSlatList().size()).clear();
		theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().size() > 1)
			theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().subList(1, theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().size()).clear();
		theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().size() > 1)
			theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().subList(1, theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().size()).clear();
		theController.getTabPaneWingSlats().getTabs().remove(1, theController.getTabPaneWingSlats().getTabs().size());

		// ailerons
		theController.getTextFieldWingInnerPositionAileronList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerPositionAileronList().size() > 1)
			theController.getTextFieldWingInnerPositionAileronList().subList(1, theController.getTextFieldWingInnerPositionAileronList().size()).clear();
		theController.getTextFieldWingOuterPositionAileronList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterPositionAileronList().size() > 1)
			theController.getTextFieldWingOuterPositionAileronList().subList(1, theController.getTextFieldWingOuterPositionAileronList().size()).clear();
		theController.getTextFieldWingInnerChordRatioAileronList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerChordRatioAileronList().size() > 1)
			theController.getTextFieldWingInnerChordRatioAileronList().subList(1, theController.getTextFieldWingInnerChordRatioAileronList().size()).clear();
		theController.getTextFieldWingOuterChordRatioAileronList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterChordRatioAileronList().size() > 1)
			theController.getTextFieldWingOuterChordRatioAileronList().subList(1, theController.getTextFieldWingOuterChordRatioAileronList().size()).clear();
		theController.getTextFieldWingMinimumDeflectionAngleAileronList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMinimumDeflectionAngleAileronList().size() > 1)
			theController.getTextFieldWingMinimumDeflectionAngleAileronList().subList(1, theController.getTextFieldWingMinimumDeflectionAngleAileronList().size()).clear();
		theController.getTextFieldWingMaximumDeflectionAngleAileronList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMaximumDeflectionAngleAileronList().size() > 1)
			theController.getTextFieldWingMaximumDeflectionAngleAileronList().subList(1, theController.getTextFieldWingMaximumDeflectionAngleAileronList().size()).clear();
		theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().size() > 1)
			theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().subList(1, theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().size()).clear();
		theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().size() > 1)
			theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().subList(1, theController.getChoiceBoxWingMinimumDeflectionAngleAileronUnitList().size()).clear();
		theController.getTabPaneWingAilerons().getTabs().remove(1, theController.getTabPaneWingAilerons().getTabs().size());
		
		// spoilers
		theController.getTextFieldWingInnerSpanwisePositionSpoilerList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerSpanwisePositionSpoilerList().size() > 1)
			theController.getTextFieldWingInnerSpanwisePositionSpoilerList().subList(1, theController.getTextFieldWingInnerSpanwisePositionSpoilerList().size()).clear();
		theController.getTextFieldWingOuterSpanwisePositionSpoilerList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterSpanwisePositionSpoilerList().size() > 1)
			theController.getTextFieldWingOuterSpanwisePositionSpoilerList().subList(1, theController.getTextFieldWingOuterSpanwisePositionSpoilerList().size()).clear();
		theController.getTextFieldWingInnerChordwisePositionSpoilerList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingInnerChordwisePositionSpoilerList().size() > 1)
			theController.getTextFieldWingInnerChordwisePositionSpoilerList().subList(1, theController.getTextFieldWingInnerChordwisePositionSpoilerList().size()).clear();
		theController.getTextFieldWingOuterChordwisePositionSpoilerList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingOuterChordwisePositionSpoilerList().size() > 1)
			theController.getTextFieldWingOuterChordwisePositionSpoilerList().subList(1, theController.getTextFieldWingOuterChordwisePositionSpoilerList().size()).clear();
		theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().size() > 1)
			theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().subList(1, theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().size()).clear();
		theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().size() > 1)
			theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().subList(1, theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().size()).clear();
		theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().size() > 1)
			theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().subList(1, theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().size()).clear();
		theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().size() > 1)
			theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().subList(1, theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().size()).clear();
		theController.getTabPaneWingSpoilers().getTabs().remove(1, theController.getTabPaneWingSpoilers().getTabs().size());
		
	}
	
	private void cleanHTailData() {
		
//		theController.gethTailAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.gethTailAdjustCriterionChoiceBox().getSelectionModel().select(0);
		theController.gethTailAdjustCriterionChoiceBox().setDisable(true);
		theController.getTextFieldHTailMainSparAdimensionalPosition().clear();
		theController.getTextFieldHTailSecondarySparAdimensionalPosition().clear();
		theController.getTextFieldHTailRoughness().clear();
		theController.gethTailRoughnessUnitChoiceBox().getSelectionModel().clearSelection();
		
		// View and TextArea
		theController.getTextAreaHTailConsoleOutput().clear();
		theController.gethTailPlanformPane().getChildren().clear();
		for (int i=1; i < theController.getTabPaneHTailViewAndAirfoils().getTabs().size(); i++)
			theController.getTabPaneHTailViewAndAirfoils().getTabs().remove(i);
		
		// panels
		theController.getTextFieldHTailSpanPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailSpanPanelList().size() > 1)
			theController.getTextFieldHTailSpanPanelList().subList(1, theController.getTextFieldHTailSpanPanelList().size()).clear();
		theController.getTextFieldHTailSweepLEPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailSweepLEPanelList().size() > 1)
			theController.getTextFieldHTailSweepLEPanelList().subList(1, theController.getTextFieldHTailSweepLEPanelList().size()).clear();
		theController.getTextFieldHTailDihedralPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailDihedralPanelList().size() > 1)
			theController.getTextFieldHTailDihedralPanelList().subList(1, theController.getTextFieldHTailDihedralPanelList().size()).clear();
		theController.getTextFieldHTailInnerChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailInnerChordPanelList().size() > 1)
			theController.getTextFieldHTailInnerChordPanelList().subList(1, theController.getTextFieldHTailInnerChordPanelList().size()).clear();
		theController.getTextFieldHTailInnerTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailInnerTwistPanelList().size() > 1)
			theController.getTextFieldHTailInnerTwistPanelList().subList(1, theController.getTextFieldHTailInnerTwistPanelList().size()).clear();
		theController.getTextFieldHTailInnerAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailInnerAirfoilPanelList().size() > 1)
			theController.getTextFieldHTailInnerAirfoilPanelList().subList(1, theController.getTextFieldHTailInnerAirfoilPanelList().size()).clear();
		theController.getTextFieldHTailOuterChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailOuterChordPanelList().size() > 1)
			theController.getTextFieldHTailOuterChordPanelList().subList(1, theController.getTextFieldHTailOuterChordPanelList().size()).clear();
		theController.getTextFieldHTailOuterTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailOuterTwistPanelList().size() > 1)
			theController.getTextFieldHTailOuterTwistPanelList().subList(1, theController.getTextFieldHTailOuterTwistPanelList().size()).clear();
		theController.getTextFieldHTailOuterAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailOuterAirfoilPanelList().size() > 1)
			theController.getTextFieldHTailOuterAirfoilPanelList().subList(1, theController.getTextFieldHTailOuterAirfoilPanelList().size()).clear();
		theController.getCheckBoxHTailLinkedToPreviousPanelList().stream().forEach(cb -> cb.selectedProperty().set(false));
		if (theController.getCheckBoxHTailLinkedToPreviousPanelList().size() > 0)
			theController.getCheckBoxHTailLinkedToPreviousPanelList().subList(0, theController.getCheckBoxHTailLinkedToPreviousPanelList().size()).clear();
		theController.getChoiceBoxHTailSpanPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailSpanPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailSpanPanelUnitList().subList(1, theController.getChoiceBoxHTailSpanPanelUnitList().size()).clear();
		theController.getChoiceBoxHTailSweepLEPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailSweepLEPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailSweepLEPanelUnitList().subList(1, theController.getChoiceBoxHTailSweepLEPanelUnitList().size()).clear();
		theController.getChoiceBoxHTailDihedralPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailDihedralPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailDihedralPanelUnitList().subList(1, theController.getChoiceBoxHTailDihedralPanelUnitList().size()).clear();
		theController.getChoiceBoxHTailInnerChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailInnerChordPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailInnerChordPanelUnitList().subList(1, theController.getChoiceBoxHTailInnerChordPanelUnitList().size()).clear();
		theController.getChoiceBoxHTailInnerTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailInnerTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailInnerTwistPanelUnitList().subList(1, theController.getChoiceBoxHTailInnerTwistPanelUnitList().size()).clear();
		theController.getChoiceBoxHTailOuterChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailOuterChordPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailOuterChordPanelUnitList().subList(1, theController.getChoiceBoxHTailOuterChordPanelUnitList().size()).clear();
		theController.getChoiceBoxHTailOuterTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailOuterTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxHTailOuterTwistPanelUnitList().subList(1, theController.getChoiceBoxHTailOuterTwistPanelUnitList().size()).clear();
		if (theController.getDetailButtonHTailInnerAirfoilList().size() > 1)
			theController.getDetailButtonHTailInnerAirfoilList().subList(1, theController.getDetailButtonHTailInnerAirfoilList().size()).clear();
		if (theController.getDetailButtonHTailOuterAirfoilList().size() > 1)
			theController.getDetailButtonHTailOuterAirfoilList().subList(1, theController.getDetailButtonHTailOuterAirfoilList().size()).clear();
		theController.getTabPaneHTailPanels().getTabs().remove(1, theController.getTabPaneHTailPanels().getTabs().size());
		
		// elevators
		theController.getTextFieldHTailInnerPositionElevatorList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailInnerPositionElevatorList().size() > 1)
			theController.getTextFieldHTailInnerPositionElevatorList().subList(1, theController.getTextFieldHTailInnerPositionElevatorList().size()).clear();
		theController.getTextFieldHTailOuterPositionElevatorList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailOuterPositionElevatorList().size() > 1)
			theController.getTextFieldHTailOuterPositionElevatorList().subList(1, theController.getTextFieldHTailOuterPositionElevatorList().size()).clear();
		theController.getTextFieldHTailInnerChordRatioElevatorList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailInnerChordRatioElevatorList().size() > 1)
			theController.getTextFieldHTailInnerChordRatioElevatorList().subList(1, theController.getTextFieldHTailInnerChordRatioElevatorList().size()).clear();
		theController.getTextFieldHTailOuterChordRatioElevatorList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailOuterChordRatioElevatorList().size() > 1)
			theController.getTextFieldHTailOuterChordRatioElevatorList().subList(1, theController.getTextFieldHTailOuterChordRatioElevatorList().size()).clear();
		theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().size() > 1)
			theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().subList(1, theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().size()).clear();
		theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().size() > 1)
			theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().subList(1, theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().size()).clear();
		theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().size() > 1)
			theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().subList(1, theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().size()).clear();
		theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().size() > 1)
			theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().subList(1, theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().size()).clear();
		theController.getTabPaneHTailElevators().getTabs().remove(1, theController.getTabPaneHTailElevators().getTabs().size());
		
	}
	
	private void cleanVTailData() {
		
//		theController.getvTailAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getvTailAdjustCriterionChoiceBox().getSelectionModel().select(0);
		theController.getvTailAdjustCriterionChoiceBox().setDisable(true);
		theController.getTextFieldVTailMainSparAdimensionalPosition().clear();
		theController.getTextFieldVTailSecondarySparAdimensionalPosition().clear();
		theController.getTextFieldVTailRoughness().clear();
		theController.getvTailRoughnessUnitChoiceBox().getSelectionModel().clearSelection();
		
		// View and TextArea
		theController.getTextAreaVTailConsoleOutput().clear();
		theController.getvTailPlanformPane().getChildren().clear();
		for (int i=1; i < theController.getTabPaneVTailViewAndAirfoils().getTabs().size(); i++)
			theController.getTabPaneVTailViewAndAirfoils().getTabs().remove(i);
		
		// panels
		theController.getTextFieldVTailSpanPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailSpanPanelList().size() > 1)
			theController.getTextFieldVTailSpanPanelList().subList(1, theController.getTextFieldVTailSpanPanelList().size()).clear();
		theController.getTextFieldVTailSweepLEPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailSweepLEPanelList().size() > 1)
			theController.getTextFieldVTailSweepLEPanelList().subList(1, theController.getTextFieldVTailSweepLEPanelList().size()).clear();
		theController.getTextFieldVTailDihedralPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailDihedralPanelList().size() > 1)
			theController.getTextFieldVTailDihedralPanelList().subList(1, theController.getTextFieldVTailDihedralPanelList().size()).clear();
		theController.getTextFieldVTailInnerChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailInnerChordPanelList().size() > 1)
			theController.getTextFieldVTailInnerChordPanelList().subList(1, theController.getTextFieldVTailInnerChordPanelList().size()).clear();
		theController.getTextFieldVTailInnerTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailInnerTwistPanelList().size() > 1)
			theController.getTextFieldVTailInnerTwistPanelList().subList(1, theController.getTextFieldVTailInnerTwistPanelList().size()).clear();
		theController.getTextFieldVTailInnerAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailInnerAirfoilPanelList().size() > 1)
			theController.getTextFieldVTailInnerAirfoilPanelList().subList(1, theController.getTextFieldVTailInnerAirfoilPanelList().size()).clear();
		theController.getTextFieldVTailOuterChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailOuterChordPanelList().size() > 1)
			theController.getTextFieldVTailOuterChordPanelList().subList(1, theController.getTextFieldVTailOuterChordPanelList().size()).clear();
		theController.getTextFieldVTailOuterTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailOuterTwistPanelList().size() > 1)
			theController.getTextFieldVTailOuterTwistPanelList().subList(1, theController.getTextFieldVTailOuterTwistPanelList().size()).clear();
		theController.getTextFieldVTailOuterAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailOuterAirfoilPanelList().size() > 1)
			theController.getTextFieldVTailOuterAirfoilPanelList().subList(1, theController.getTextFieldVTailOuterAirfoilPanelList().size()).clear();
		theController.getCheckBoxVTailLinkedToPreviousPanelList().stream().forEach(cb -> cb.selectedProperty().set(false));
		if (theController.getCheckBoxVTailLinkedToPreviousPanelList().size() > 0)
			theController.getCheckBoxVTailLinkedToPreviousPanelList().subList(0, theController.getCheckBoxVTailLinkedToPreviousPanelList().size()).clear();
		theController.getChoiceBoxVTailSpanPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailSpanPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailSpanPanelUnitList().subList(1, theController.getChoiceBoxVTailSpanPanelUnitList().size()).clear();
		theController.getChoiceBoxVTailSweepLEPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailSweepLEPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailSweepLEPanelUnitList().subList(1, theController.getChoiceBoxVTailSweepLEPanelUnitList().size()).clear();
		theController.getChoiceBoxVTailDihedralPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailDihedralPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailDihedralPanelUnitList().subList(1, theController.getChoiceBoxVTailDihedralPanelUnitList().size()).clear();
		theController.getChoiceBoxVTailInnerChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailInnerChordPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailInnerChordPanelUnitList().subList(1, theController.getChoiceBoxVTailInnerChordPanelUnitList().size()).clear();
		theController.getChoiceBoxVTailInnerTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailInnerTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailInnerTwistPanelUnitList().subList(1, theController.getChoiceBoxVTailInnerTwistPanelUnitList().size()).clear();
		theController.getChoiceBoxVTailOuterChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailOuterChordPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailOuterChordPanelUnitList().subList(1, theController.getChoiceBoxVTailOuterChordPanelUnitList().size()).clear();
		theController.getChoiceBoxVTailOuterTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailOuterTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxVTailOuterTwistPanelUnitList().subList(1, theController.getChoiceBoxVTailOuterTwistPanelUnitList().size()).clear();
		if (theController.getDetailButtonVTailInnerAirfoilList().size() > 1)
			theController.getDetailButtonVTailInnerAirfoilList().subList(1, theController.getDetailButtonVTailInnerAirfoilList().size()).clear();
		if (theController.getDetailButtonVTailOuterAirfoilList().size() > 1)
			theController.getDetailButtonVTailOuterAirfoilList().subList(1, theController.getDetailButtonVTailOuterAirfoilList().size()).clear();
		theController.getTabPaneVTailPanels().getTabs().remove(1, theController.getTabPaneVTailPanels().getTabs().size());
		
		// Rudders
		theController.getTextFieldVTailInnerPositionRudderList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailInnerPositionRudderList().size() > 1)
			theController.getTextFieldVTailInnerPositionRudderList().subList(1, theController.getTextFieldVTailInnerPositionRudderList().size()).clear();
		theController.getTextFieldVTailOuterPositionRudderList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailOuterPositionRudderList().size() > 1)
			theController.getTextFieldVTailOuterPositionRudderList().subList(1, theController.getTextFieldVTailOuterPositionRudderList().size()).clear();
		theController.getTextFieldVTailInnerChordRatioRudderList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailInnerChordRatioRudderList().size() > 1)
			theController.getTextFieldVTailInnerChordRatioRudderList().subList(1, theController.getTextFieldVTailInnerChordRatioRudderList().size()).clear();
		theController.getTextFieldVTailOuterChordRatioRudderList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailOuterChordRatioRudderList().size() > 1)
			theController.getTextFieldVTailOuterChordRatioRudderList().subList(1, theController.getTextFieldVTailOuterChordRatioRudderList().size()).clear();
		theController.getTextFieldVTailMinimumDeflectionAngleRudderList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailMinimumDeflectionAngleRudderList().size() > 1)
			theController.getTextFieldVTailMinimumDeflectionAngleRudderList().subList(1, theController.getTextFieldVTailMinimumDeflectionAngleRudderList().size()).clear();
		theController.getTextFieldVTailMaximumDeflectionAngleRudderList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldVTailMaximumDeflectionAngleRudderList().size() > 1)
			theController.getTextFieldVTailMaximumDeflectionAngleRudderList().subList(1, theController.getTextFieldVTailMaximumDeflectionAngleRudderList().size()).clear();
		theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().size() > 1)
			theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().subList(1, theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().size()).clear();
		theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().size() > 1)
			theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().subList(1, theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().size()).clear();
		theController.getTabPaneVTailRudders().getTabs().remove(1, theController.getTabPaneVTailRudders().getTabs().size());
		
	}
	
	private void cleanCanardData() {
		
//		theController.getCanardAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getCanardAdjustCriterionChoiceBox().getSelectionModel().select(0);
		theController.getCanardAdjustCriterionChoiceBox().setDisable(true);
		theController.getTextFieldCanardMainSparAdimensionalPosition().clear();
		theController.getTextFieldCanardSecondarySparAdimensionalPosition().clear();
		theController.getTextFieldCanardRoughness().clear();
		theController.getCanardRoughnessUnitChoiceBox().getSelectionModel().clearSelection();
		
		// View and TextArea
		theController.getTextAreaCanardConsoleOutput().clear();
		theController.getCanardPlanformPane().getChildren().clear();
		for (int i=1; i < theController.getTabPaneCanardViewAndAirfoils().getTabs().size(); i++)
			theController.getTabPaneCanardViewAndAirfoils().getTabs().remove(i);
		
		// panels
		theController.getTextFieldCanardSpanPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardSpanPanelList().size() > 1)
			theController.getTextFieldCanardSpanPanelList().subList(1, theController.getTextFieldCanardSpanPanelList().size()).clear();
		theController.getTextFieldCanardSweepLEPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardSweepLEPanelList().size() > 1)
			theController.getTextFieldCanardSweepLEPanelList().subList(1, theController.getTextFieldCanardSweepLEPanelList().size()).clear();
		theController.getTextFieldCanardDihedralPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardDihedralPanelList().size() > 1)
			theController.getTextFieldCanardDihedralPanelList().subList(1, theController.getTextFieldCanardDihedralPanelList().size()).clear();
		theController.getTextFieldCanardInnerChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardInnerChordPanelList().size() > 1)
			theController.getTextFieldCanardInnerChordPanelList().subList(1, theController.getTextFieldCanardInnerChordPanelList().size()).clear();
		theController.getTextFieldCanardInnerTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardInnerTwistPanelList().size() > 1)
			theController.getTextFieldCanardInnerTwistPanelList().subList(1, theController.getTextFieldCanardInnerTwistPanelList().size()).clear();
		theController.getTextFieldCanardInnerAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardInnerAirfoilPanelList().size() > 1)
			theController.getTextFieldCanardInnerAirfoilPanelList().subList(1, theController.getTextFieldCanardInnerAirfoilPanelList().size()).clear();
		theController.getTextFieldCanardOuterChordPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardOuterChordPanelList().size() > 1)
			theController.getTextFieldCanardOuterChordPanelList().subList(1, theController.getTextFieldCanardOuterChordPanelList().size()).clear();
		theController.getTextFieldCanardOuterTwistPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardOuterTwistPanelList().size() > 1)
			theController.getTextFieldCanardOuterTwistPanelList().subList(1, theController.getTextFieldCanardOuterTwistPanelList().size()).clear();
		theController.getTextFieldCanardOuterAirfoilPanelList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardOuterAirfoilPanelList().size() > 1)
			theController.getTextFieldCanardOuterAirfoilPanelList().subList(1, theController.getTextFieldCanardOuterAirfoilPanelList().size()).clear();
		theController.getCheckBoxCanardLinkedToPreviousPanelList().stream().forEach(cb -> cb.selectedProperty().set(false));
		if (theController.getCheckBoxCanardLinkedToPreviousPanelList().size() > 0)
			theController.getCheckBoxCanardLinkedToPreviousPanelList().subList(0, theController.getCheckBoxCanardLinkedToPreviousPanelList().size()).clear();
		theController.getChoiceBoxCanardSpanPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardSpanPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardSpanPanelUnitList().subList(1, theController.getChoiceBoxCanardSpanPanelUnitList().size()).clear();
		theController.getChoiceBoxCanardSweepLEPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardSweepLEPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardSweepLEPanelUnitList().subList(1, theController.getChoiceBoxCanardSweepLEPanelUnitList().size()).clear();
		theController.getChoiceBoxCanardDihedralPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardDihedralPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardDihedralPanelUnitList().subList(1, theController.getChoiceBoxCanardDihedralPanelUnitList().size()).clear();
		theController.getChoiceBoxCanardInnerChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardInnerChordPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardInnerChordPanelUnitList().subList(1, theController.getChoiceBoxCanardInnerChordPanelUnitList().size()).clear();
		theController.getChoiceBoxCanardInnerTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardInnerTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardInnerTwistPanelUnitList().subList(1, theController.getChoiceBoxCanardInnerTwistPanelUnitList().size()).clear();
		theController.getChoiceBoxCanardOuterChordPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardOuterChordPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardOuterChordPanelUnitList().subList(1, theController.getChoiceBoxCanardOuterChordPanelUnitList().size()).clear();
		theController.getChoiceBoxCanardOuterTwistPanelUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardOuterTwistPanelUnitList().size() > 1)
			theController.getChoiceBoxCanardOuterTwistPanelUnitList().subList(1, theController.getChoiceBoxCanardOuterTwistPanelUnitList().size()).clear();
		if (theController.getDetailButtonCanardInnerAirfoilList().size() > 1)
			theController.getDetailButtonCanardInnerAirfoilList().subList(1, theController.getDetailButtonCanardInnerAirfoilList().size()).clear();
		if (theController.getDetailButtonCanardOuterAirfoilList().size() > 1)
			theController.getDetailButtonCanardOuterAirfoilList().subList(1, theController.getDetailButtonCanardOuterAirfoilList().size()).clear();
		theController.getTabPaneCanardPanels().getTabs().remove(1, theController.getTabPaneCanardPanels().getTabs().size());
		
		// Control Surfaces
		theController.getTextFieldCanardInnerPositionControlSurfaceList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardInnerPositionControlSurfaceList().size() > 1)
			theController.getTextFieldCanardInnerPositionControlSurfaceList().subList(1, theController.getTextFieldCanardInnerPositionControlSurfaceList().size()).clear();
		theController.getTextFieldCanardOuterPositionControlSurfaceList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardOuterPositionControlSurfaceList().size() > 1)
			theController.getTextFieldCanardOuterPositionControlSurfaceList().subList(1, theController.getTextFieldCanardOuterPositionControlSurfaceList().size()).clear();
		theController.getTextFieldCanardInnerChordRatioControlSurfaceList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardInnerChordRatioControlSurfaceList().size() > 1)
			theController.getTextFieldCanardInnerChordRatioControlSurfaceList().subList(1, theController.getTextFieldCanardInnerChordRatioControlSurfaceList().size()).clear();
		theController.getTextFieldCanardOuterChordRatioControlSurfaceList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardOuterChordRatioControlSurfaceList().size() > 1)
			theController.getTextFieldCanardOuterChordRatioControlSurfaceList().subList(1, theController.getTextFieldCanardOuterChordRatioControlSurfaceList().size()).clear();
		theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().size() > 1)
			theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().subList(1, theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().size()).clear();
		theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().stream().forEach(tf -> tf.clear());
		if (theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().size() > 1)
			theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().subList(1, theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().size()).clear();
		theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().size() > 1)
			theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().subList(1, theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().size()).clear();
		theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().stream().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().size() > 1)
			theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().subList(1, theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().size()).clear();
		theController.getTabPaneCanardControlSurfaces().getTabs().remove(1, theController.getTabPaneCanardControlSurfaces().getTabs().size());
		
	}
	
	private void cleanNacelleData() {
		
		theController.getTextFieldNacelleRoughness1().clear();
		theController.getNacelleRoughnessUnitChoiceBox1().getSelectionModel().clearSelection();
		theController.getTextFieldNacelleLength1().clear();
		theController.getNacelleLengthUnitChoiceBox1().getSelectionModel().clearSelection();
		theController.getTextFieldNacelleMaximumDiameter1().clear();
		theController.getNacelleMaximumDiameterUnitChoiceBox1().getSelectionModel().clearSelection();
		theController.getTextFieldNacelleKInlet1().clear();
		theController.getTextFieldNacelleKOutlet1().clear();
		theController.getTextFieldNacelleKLength1().clear();
		theController.getTextFieldNacelleKDiameterOutlet1().clear();
		
		theController.getTextFieldNacelleRoughnessList().subList(1, theController.getTextFieldNacelleRoughnessList().size()).clear();
		theController.getTextFieldNacelleLengthList().subList(1, theController.getTextFieldNacelleLengthList().size()).clear();
		theController.getTextFieldNacelleMaximumDiameterList().subList(1, theController.getTextFieldNacelleMaximumDiameterList().size()).clear();
		theController.getTextFieldNacelleKInletList().subList(1, theController.getTextFieldNacelleKInletList().size()).clear();
		theController.getTextFieldNacelleKOutletList().subList(1, theController.getTextFieldNacelleKOutletList().size()).clear();
		theController.getTextFieldNacelleKLengthList().subList(1, theController.getTextFieldNacelleKLengthList().size()).clear();
		theController.getTextFieldNacelleKDiameterOutletList().subList(1, theController.getTextFieldNacelleKDiameterOutletList().size()).clear();
		theController.getChoiceBoxNacelleRoughnessUnitList().subList(1, theController.getChoiceBoxNacelleRoughnessUnitList().size()).clear();
		theController.getChoiceBoxNacelleLengthUnitList().subList(1, theController.getChoiceBoxNacelleLengthUnitList().size()).clear();
		theController.getChoiceBoxNacelleMaximumDiameterUnitList().subList(1, theController.getChoiceBoxNacelleMaximumDiameterUnitList().size()).clear();
		theController.getNacelleEstimateDimesnsionButtonList().subList(1, theController.getNacelleEstimateDimesnsionButtonList().size()).clear();
		theController.getNacelleKInletInfoButtonList().subList(1, theController.getNacelleKInletInfoButtonList().size()).clear();
		theController.getNacelleKOutletInfoButtonList().subList(1, theController.getNacelleKOutletInfoButtonList().size()).clear();
		theController.getNacelleKLengthInfoButtonList().subList(1, theController.getNacelleKLengthInfoButtonList().size()).clear();
		theController.getNacelleKDiameterOutletInfoButtonList().subList(1, theController.getNacelleKDiameterOutletInfoButtonList().size()).clear();
		
		theController.getTabPaneNacelles().getTabs().remove(1, theController.getTabPaneNacelles().getTabs().size());
		
		// View and TextArea
		theController.getTextAreaNacelleConsoleOutput().clear();
		theController.getNacelle1TopViewPane().getChildren().clear();
		theController.getNacelle1SideViewPane().getChildren().clear();
		theController.getNacelle1FrontViewPane().getChildren().clear();
		theController.getNacelleTopViewPaneList().subList(1, theController.getNacelleTopViewPaneList().size()).clear();
		theController.getNacelleSideViewPaneList().subList(1, theController.getNacelleSideViewPaneList().size()).clear();
		theController.getNacelleFrontViewPaneList().subList(1, theController.getNacelleFrontViewPaneList().size()).clear();
		theController.getTabPaneNacellesTopViews().getTabs().remove(1, theController.getTabPaneNacellesTopViews().getTabs().size());
		theController.getTabPaneNacellesSideViews().getTabs().remove(1, theController.getTabPaneNacellesSideViews().getTabs().size());
		theController.getTabPaneNacellesFrontViews().getTabs().remove(1, theController.getTabPaneNacellesFrontViews().getTabs().size());
		
	}
	
	private void cleanPowerPlantData() {
		
		theController.getTextAreaPowerPlantConsoleOutput().clear();
		
		theController.getPowerPlantJetRadioButtonList().subList(1, theController.getTabPaneEngines().getTabs().size()).clear();
		theController.getPowerPlantTurbopropRadioButtonList().subList(1, theController.getTabPaneEngines().getTabs().size()).clear();
		theController.getPowerPlantPistonRadioButtonList().subList(1, theController.getTabPaneEngines().getTabs().size()).clear();
		theController.getPowerPlantToggleGropuList().subList(1, theController.getTabPaneEngines().getTabs().size()).clear();
		
		theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().clear();
		theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanLengthTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().clear();
		theController.getEngineTurbojetTurbofanDryMassTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().clear();
		theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().clear();
		theController.getEngineTurbojetTurbofanBPRTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().clear();
		theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().clear();
		
		theController.getEngineTurbopropTypeChoiceBoxMap().clear(); 
		theController.getEngineTurbopropDatabaseTextFieldMap().clear();
		theController.getEngineTurbopropLengthTextFieldMap().clear();
		theController.getEngineTurbopropLengthUnitChoiceBoxMap().clear();
		theController.getEngineTurbopropDryMassTextFieldMap().clear();
		theController.getEngineTurbopropDryMassUnitChoiceBoxMap().clear();
		theController.getEngineTurbopropStaticPowerTextFieldMap().clear();
		theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().clear();
		theController.getEngineTurbopropPropellerDiameterTextFieldMap().clear();
		theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().clear();
		theController.getEngineTurbopropNumberOfBladesTextFieldMap().clear();
		theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().clear();
		theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().clear();
		theController.getEngineTurbopropNumberOfShaftsTextFieldMap().clear();
		theController.getEngineTurbopropOverallPressureRatioTextFieldMap().clear();
		
		theController.getEnginePistonTypeChoiceBoxMap().clear(); 
		theController.getEnginePistonDatabaseTextFieldMap().clear();
		theController.getEnginePistonLengthTextFieldMap().clear();
		theController.getEnginePistonLengthUnitChoiceBoxMap().clear();
		theController.getEnginePistonDryMassTextFieldMap().clear();
		theController.getEnginePistonDryMassUnitChoiceBoxMap().clear();
		theController.getEnginePistonStaticPowerTextFieldMap().clear();
		theController.getEnginePistonStaticPowerUnitChoiceBoxMap().clear();
		theController.getEnginePistonPropellerDiameterTextFieldMap().clear();
		theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().clear();
		theController.getEnginePistonNumberOfBladesTextFieldMap().clear();
		theController.getEnginePistonPropellerEfficiencyTextFieldMap().clear();
		
		for(int i=0; i<theController.getTabPaneEngines().getTabs().size(); i++) {
			if (theController.getPowerPlantEngineTypePaneMap().get(i).containsKey(EngineTypeEnum.TURBOFAN)) 
				theController.getPowerPlantEngineTypePaneMap().get(i).get(EngineTypeEnum.TURBOFAN).getChildren().clear();
			if (theController.getPowerPlantEngineTypePaneMap().get(i).containsKey(EngineTypeEnum.TURBOPROP)) 
				theController.getPowerPlantEngineTypePaneMap().get(i).get(EngineTypeEnum.TURBOPROP).getChildren().clear();
			if (theController.getPowerPlantEngineTypePaneMap().get(i).containsKey(EngineTypeEnum.PISTON)) 
				theController.getPowerPlantEngineTypePaneMap().get(i).get(EngineTypeEnum.PISTON).getChildren().clear();
			theController.getPowerPlantPaneMap().remove(i);
		}
		
		theController.getPowerPlantJetRadioButtonList().get(0).setSelected(false);
		theController.getPowerPlantTurbopropRadioButtonList().get(0).setSelected(false);
		theController.getPowerPlantPistonRadioButtonList().get(0).setSelected(false);
		
		theController.getTabPaneEngines().getTabs().remove(1, theController.getTabPaneEngines().getTabs().size());
		
	}
	
	private void cleanLandingGearsData() {
		
		theController.getTextAreaLandingGearsConsoleOutput().clear();

		theController.getTextFieldLandingGearsMainLegLength().clear();
		theController.getTextFieldLandingGearsKMainLegLength().clear();
		theController.getTextFieldLandingGearsNumberOfFrontalWheels().clear();
		theController.getTextFieldLandingGearsNumberOfRearWheels().clear();
		theController.getTextFieldLandingGearsDistanceBetweenWheels().clear();
		theController.getTextFieldLandingGearsFrontalWheelsHeight().clear();
		theController.getTextFieldLandingGearsFrontalWheelsWidth().clear();
		theController.getTextFieldLandingGearsRearWheelsHeight().clear();
		theController.getTextFieldLandingGearsRearWheelsWidth().clear();
		
		theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().clearSelection();
		
	}
	
	private void clean3DViewData() {
		
		theController.getCAD3DViewPane().getChildren().clear();
		theController.getCADConfigurationInputFileTextField().clear();
		theController.getTextAreaCAD3DViewConsoleOutput().clear();
		
		theController.getExportCADWireframeCheckBox().setSelected(false);
		theController.getFileExtensionCADChoiceBox().getSelectionModel().clearSelection();
		
		theController.getGenerateFuselageCADCheckBox().setSelected(false);
		theController.getFuselageCADNumberNoseSectionsTextField().clear();
		theController.getFuselageCADNoseSpacingChoiceBox().getSelectionModel().clearSelection();
		theController.getFuselageCADNumberTailSectionsTextField().clear();
		theController.getFuselageCADTailSpacingChoiceBox().getSelectionModel().clearSelection();
		
		theController.getGenerateWingCADCheckBox().setSelected(false);
		theController.getWingCADTipTypeChoiceBox().getSelectionModel().clearSelection();
		theController.getWingletCADYOffsetFactorTextField().clear();
		theController.getWingletCADXOffsetFactorTextField().clear();
		theController.getWingletCADTaperRatioTextField().clear();
		
		theController.getGenerateHTailCADCheckBox().setSelected(false);
		theController.getHTailCADTipTypeChoiceBox().getSelectionModel().clearSelection();
		
		theController.getGenerateVTailCADCheckBox().setSelected(false);
		theController.getVTailCADTipTypeChoiceBox().getSelectionModel().clearSelection();
		
		theController.getGenerateCanardCADCheckBox().setSelected(false);
		theController.getCanardCADTipTypeChoiceBox().getSelectionModel().clearSelection();
		
		theController.getGenerateEnginesCADCheckBox().setSelected(false);
		theController.getEnginesCADNacelleTemplateFileTextFieldList().forEach(tf -> tf.clear());
		if (theController.getEnginesCADNacelleTemplateFileTextFieldList().size() > 1)
			theController.getEnginesCADNacelleTemplateFileTextFieldList().subList(1, theController.getEnginesCADNacelleTemplateFileTextFieldList().size()).clear();		
		theController.getEnginesCADBladeTemplateFileTextFieldList().forEach(tf -> tf.clear());
		if (theController.getEnginesCADBladeTemplateFileTextFieldList().size() > 1)
			theController.getEnginesCADBladeTemplateFileTextFieldList().subList(1, theController.getEnginesCADBladeTemplateFileTextFieldList().size()).clear();
		theController.getEnginesCADBladePitchAngleTextFieldList().forEach(tf -> tf.clear());
		if (theController.getEnginesCADBladePitchAngleTextFieldList().size() > 1) 
			theController.getEnginesCADBladePitchAngleTextFieldList().subList(1, theController.getEnginesCADBladePitchAngleTextFieldList().size()).clear();
		theController.getEnginesCADBladePitchAngleUnitList().forEach(cb -> cb.getSelectionModel().clearSelection());
		if (theController.getEnginesCADBladePitchAngleUnitList().size() > 1)
			theController.getEnginesCADBladePitchAngleUnitList().subList(1, theController.getEnginesCADBladePitchAngleUnitList().size()).clear();
		theController.getTabPaneCAD3DViewEngines().getTabs().remove(1, theController.getTabPaneCAD3DViewEngines().getTabs().size());
		
		theController.getGenerateWingFairingCADCheckBox().setSelected(false);
		theController.getWingFairingCADFrontLengthFactorTextField().clear();
		theController.getWingFairingCADBackLengthFactorTextField().clear();
		theController.getWingFairingCADWidthFactorTextField().clear();
		theController.getWingFairingCADHeightFactorTextField().clear();
		theController.getWingFairingCADHeightBelowReferenceFactorTextField().clear();
		theController.getWingFairingCADHeightAboveReferenceFactorTextField().clear();
		theController.getWingFairingCADFilletRadiusFactorTextField().clear();
		
		theController.getGenerateCanardFairingCADCheckBox().setSelected(false);
		theController.getCanardFairingCADFrontLengthFactorTextField().clear();
		theController.getCanardFairingCADBackLengthFactorTextField().clear();
		theController.getCanardFairingCADWidthFactorTextField().clear();
		theController.getCanardFairingCADHeightFactorTextField().clear();
		theController.getCanardFairingCADHeightBelowReferenceFactorTextField().clear();
		theController.getCanardFairingCADHeightAboveReferenceFactorTextField().clear();
		theController.getCanardFairingCADFilletRadiusFactorTextField().clear();
		
		Main.setTheCADManager(null);
		
	}
	
	@SuppressWarnings({ "rawtypes" })
	public void loadAircraftFileImplementation() throws IOException, InterruptedException {
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);
		
		if(Main.getTheAircraft() != null) {
			
			cleanAircraftData();
			cleanFuselageData();
			cleanCabinConfigurationData();
			cleanWingData();
			cleanHTailData();
			cleanVTailData();
			cleanCanardData();
			cleanNacelleData();
			cleanPowerPlantData();
			cleanLandingGearsData();
			clean3DViewData();
			
		}
		
		Service loadAircraftService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {
					
					@Override
					protected Object call() throws Exception {
						
						System.setOut(filterStream);
						
						int numberOfOperations = 29;
						double progressIncrement = 100/numberOfOperations;
						
						MyConfiguration.setDir(FoldersEnum.DATABASE_DIR, Main.getDatabaseDirectoryPath());
						String databaseFolderPath = MyConfiguration.getDir(FoldersEnum.DATABASE_DIR);
						String aerodynamicDatabaseFileName = "Aerodynamic_Database_Ultimate.h5";
						String highLiftDatabaseFileName = "HighLiftDatabase.h5";
						String fusDesDatabaseFilename = "FusDes_database.h5";
						String vedscDatabaseFilename = "VeDSC_database.h5";
						AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
								new AerodynamicDatabaseReader(
										databaseFolderPath,	aerodynamicDatabaseFileName
										),
								databaseFolderPath
								);
						HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
								new HighLiftDatabaseReader(
										databaseFolderPath,	highLiftDatabaseFileName
										),
								databaseFolderPath
								);
						FusDesDatabaseReader fusDesDatabaseReader = DatabaseManager.initializeFusDes(
								new FusDesDatabaseReader(
										databaseFolderPath,	fusDesDatabaseFilename
										),
								databaseFolderPath
								);
						VeDSCDatabaseReader veDSCDatabaseReader = DatabaseManager.initializeVeDSC(
								new VeDSCDatabaseReader(
										databaseFolderPath,	vedscDatabaseFilename
										),
								databaseFolderPath
								);
						updateProgress(1, numberOfOperations);
						updateMessage("Reading Database...");
						updateTitle(String.valueOf(progressIncrement) + "%");
						
						String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
						String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
						String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
						String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
						String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
						String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
						String dirAirfoils = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces" + File.separator + "airfoils";

						String pathToXML = theController.getTextFieldAircraftInputFile().getText(); 

						Main.setTheAircraft(
								Aircraft.importFromXML(
										pathToXML,
										dirLiftingSurfaces,
										dirFuselages,
										dirEngines,
										dirNacelles,
										dirLandingGears,
										dirCabinConfiguration,
										dirAirfoils,
										aeroDatabaseReader,
										highLiftDatabaseReader,
										fusDesDatabaseReader,
										veDSCDatabaseReader
										)
								);
						updateProgress(2, numberOfOperations);
						updateMessage("Creating Aircraft Object...");
						updateTitle(String.valueOf(progressIncrement*2) + "%");

						// COMPONENTS LOG TO INTERFACE
						if (Main.getTheAircraft() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logAircraftFromFileToInterface();
									
								}
							});
							updateProgress(3, numberOfOperations);
							updateMessage("Logging Aircraft Object Data...");
							updateTitle(String.valueOf(progressIncrement*3) + "%");
						}
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logFuselageFromFileToInterface();
									
								}
							});							
							updateProgress(4, numberOfOperations);
							updateMessage("Logging Fuselage Object Data...");
							updateTitle(String.valueOf(progressIncrement*4) + "%");
						}
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logCabinConfigutionFromFileToInterface();
									
								}
							});				
							updateProgress(5, numberOfOperations);
							updateMessage("Logging Cabin Configuration Object Data...");
							updateTitle(String.valueOf(progressIncrement*5) + "%");
						}
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logWingFromFileToInterface();
									
								}
							});				
							updateProgress(6, numberOfOperations);
							updateMessage("Logging Wing Object Data...");
							updateTitle(String.valueOf(progressIncrement*6) + "%");
						}
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logHTailFromFileToInterface();
									
								}
							});		
							updateProgress(7, numberOfOperations);
							updateMessage("Logging Horizontal Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*7) + "%");
						}
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logVTailFromFileToInterface();
									
								}
							});		
							updateProgress(8, numberOfOperations);
							updateMessage("Logging Vertical Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*8) + "%");
						}
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logCanardFromFileToInterface();
									
								}
							});		
							updateProgress(9, numberOfOperations);
							updateMessage("Logging Canard Object Data...");
							updateTitle(String.valueOf(progressIncrement*9) + "%");
						}
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logNacelleFromFileToInterface();
									
								}
							});	
							updateProgress(10, numberOfOperations);
							updateMessage("Logging Nacelle Object Data...");
							updateTitle(String.valueOf(progressIncrement*10) + "%");
						}
						if (Main.getTheAircraft().getPowerPlant() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logPowerPlantFromFileToInterface();
									
								}
							});	
							updateProgress(11, numberOfOperations);
							updateMessage("Logging Power Plant Object Data...");
							updateTitle(String.valueOf(progressIncrement*11) + "%");
						}
						if (Main.getTheAircraft().getLandingGears() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerLogUtilities().logLandingGearsFromFileToInterface();
									
								}
							});	
							updateProgress(12, numberOfOperations);
							updateMessage("Logging Landing Gears Object Data...");
							updateTitle(String.valueOf(progressIncrement*12) + "%");
						}
						//............................
						// COMPONENTS 3 VIEW CREATION
						//............................
						// aircraft
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								theController.getInputManagerControllerGraphicUtilities().createAircraftTopView();
								
							}
						});	
						updateProgress(13, numberOfOperations);
						updateMessage("Creating Aircraft Top View...");
						updateTitle(String.valueOf(progressIncrement*13) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								theController.getInputManagerControllerGraphicUtilities().createAircraftSideView();
								
							}
						});	
						updateProgress(14, numberOfOperations);
						updateMessage("Creating Aircraft Side View...");
						updateTitle(String.valueOf(progressIncrement*14) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								theController.getInputManagerControllerGraphicUtilities().createAircraftFrontView();
								
							}
						});	
						updateProgress(15, numberOfOperations);
						updateMessage("Creating Aircraft Front View...");
						updateTitle(String.valueOf(progressIncrement*15) + "%");
						//............................
						// fuselage
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createFuselageTopView();
									
								}
							});	
							updateProgress(16, numberOfOperations);
							updateMessage("Creating Fuselage Top View...");
							updateTitle(String.valueOf(progressIncrement*16) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createFuselageSideView();
									
								}
							});	
							updateProgress(17, numberOfOperations);
							updateMessage("Creating Fuselage Side View...");
							updateTitle(String.valueOf(progressIncrement*17) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createFuselageFrontView();
									
								}
							});	
							updateProgress(18, numberOfOperations);
							updateMessage("Creating Fuselage Front View...");
							updateTitle(String.valueOf(progressIncrement*18) + "%");
						}
						//............................
						// cabin configuration
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createSeatMap();
									
								}
							});	
							updateProgress(19, numberOfOperations);
							updateMessage("Creating Seat Map...");
							updateTitle(String.valueOf(progressIncrement*19) + "%");
						}
						//............................
						// wing
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createWingPlanformView();
									
								}
							});	
							updateProgress(20, numberOfOperations);
							updateMessage("Creating Wing Planform View...");
							updateTitle(String.valueOf(progressIncrement*20) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createEquivalentWingView();
									
								}
							});	
							updateProgress(21, numberOfOperations);
							updateMessage("Creating Equivalent Wing View...");
							updateTitle(String.valueOf(progressIncrement*21) + "%");
						}
						//............................
						// hTail
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createHTailPlanformView();
									
								}
							});	
							updateProgress(22, numberOfOperations);
							updateMessage("Creating HTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*22) + "%");
						}
						//............................
						// vTail
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createVTailPlanformView();
									
								}
							});	
							updateProgress(23, numberOfOperations);
							updateMessage("Creating VTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*23) + "%");
						}
						//............................
						// canard
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createCanardPlanformView();
									
								}
							});	
							updateProgress(24, numberOfOperations);
							updateMessage("Creating Canard Planform View...");
							updateTitle(String.valueOf(progressIncrement*24) + "%");
						}
						//............................
						// nacelle
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createNacelleTopView();
									
								}
							});	
							updateProgress(25, numberOfOperations);
							updateMessage("Creating Nacelles Top View...");
							updateTitle(String.valueOf(progressIncrement*25) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createNacelleSideView();
									
								}
							});	
							updateProgress(26, numberOfOperations);
							updateMessage("Creating Nacelles Side View...");
							updateTitle(String.valueOf(progressIncrement*26) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createNacelleFrontView();
									
								}
							});	
							updateProgress(27, numberOfOperations);
							updateMessage("Creating Nacelles FrontView...");
							updateTitle(String.valueOf(progressIncrement*27) + "%");	
						}
						//...................................
						// Update CAD 3D view engine tabs
						if (Main.getTheAircraft().getNacelles() != null && Main.getTheAircraft().getPowerPlant() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getInputManagerControllerSecondaryActionUtilities().addCADEnginesImplementation();
								}
							});
							updateProgress(28, numberOfOperations);
							updateMessage("Updating CAD engine tabs...");
							updateTitle(String.valueOf(progressIncrement*28) + "%");
						}

						updateProgress(29, numberOfOperations);
						updateMessage("Aircraft Loaded!");
						updateTitle(String.valueOf(100) + "%");
						
						//Block the thread for a short time, but be sure
			            //to check the InterruptedException for cancellation
			            try {
			                Thread.sleep(100);
			            } catch (InterruptedException interrupted) {
			                if (isCancelled()) {
			                    updateMessage("Cancelled");
			                    updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			                else {
			                	updateMessage("Terminated");
			                	updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			            }
						
			    		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
			    		ObjectProperty<CADManager> cadManager = new SimpleObjectProperty<>();
			    		ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();
			    		
			    		try {
			    			aircraftSavedFlag.set(Main.getAircraftSaved());
			    			theController.getSaveAircraftButton().disableProperty().bind(
			    					Bindings.equal(aircraftSavedFlag, true).or(Bindings.isNull(aircraft))
			    					);
			    		} catch (Exception e) {
			    			theController.getSaveAircraftButton().setDisable(true);
			    		}

			    		try {
			    			theController.getUpdateAircraftDataButton().disableProperty().bind(
			    					Bindings.isNull(aircraft)
			    					);
			    			theController.getUpdateAircraftDataFromFileComboBox().disableProperty().bind(
			    					Bindings.isNull(aircraft)
			    					);
			    		} catch (Exception e) {
			    			theController.getUpdateAircraftDataButton().setDisable(true);
			    			theController.getUpdateAircraftDataFromFileComboBox().setDisable(true);
			    		}
			    		
			    		try {
			    			aircraft.set(Main.getTheAircraft());
			    			theController.getNewAircraftButton().disableProperty().bind(
			    					Bindings.isNull(aircraft)
			    					);
			    		} catch (Exception e) {
			    			theController.getNewAircraftButton().setDisable(true);
			    		}
			    		
			    		try {
			    			cadManager.set(Main.getTheCADManager());
			    			theController.getChooseCADConfigurationFileButton().disableProperty().bind(
			    					Bindings.isNull(aircraft)
			    					);
			    			theController.getUpdateCAD3DViewButton().disableProperty().bind(
			    					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
			    					);
			    			theController.getSaveCADToFileButton().disableProperty().bind(
			    					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
			    					);
			    		} catch (Exception e) {
			    			theController.getChooseCADConfigurationFileButton().setDisable(true);
			    			theController.getUpdateCAD3DViewButton().setDisable(true);
			    			theController.getSaveCADToFileButton().setDisable(true);
			    		}
			    		
			    		theController.getInputManagerControllerSecondaryActionUtilities().cad3DViewFieldsDisableCheck();
			    		
						return null;
					}
					
				};
			}
		}; 
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().progressProperty().bind(loadAircraftService.progressProperty());
		Main.getStatusBar().textProperty().bind(loadAircraftService.messageProperty());
		Main.getTaskPercentage().textProperty().bind(loadAircraftService.titleProperty());
		
		loadAircraftService.start();
		
		// write again
		System.setOut(originalOut);
		
	}
	
	public void loadCADConfigurationFileImplementation() {
		
		Main.setTheCADManager(
				CADManager.importFromXML(
						Main.getCADConfigurationFileAbsolutePath(),
						Main.getTheAircraft()
						)
				);
		
		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
		ObjectProperty<CADManager> cadManager = new SimpleObjectProperty<>();
		
		aircraft.set(Main.getTheAircraft());
		cadManager.set(Main.getTheCADManager());
		
		try {
			theController.getUpdateCAD3DViewButton().disableProperty().bind(
					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
					); 
			theController.getSaveCADToFileButton().disableProperty().bind(
					Bindings.isNull(aircraft).or(Bindings.isNull(cadManager))
					);
		} catch (Exception e) {
			theController.getUpdateCAD3DViewButton().setDisable(true);
			theController.getSaveCADToFileButton().setDisable(true);
		}
		
		theController.getInputManagerControllerLogUtilities().logCADConfigurationFromFileToInterface();
		theController.getInputManagerControllerSecondaryActionUtilities().cad3DViewFieldsDisableCheck();
		
	}
	
	public void updateCAD3DViewImplementation() {
		
		boolean generateFuselage = (theController.getGenerateFuselageCADCheckBox().isDisabled()) ? 
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateFuselage() :
				theController.getGenerateFuselageCADCheckBox().isSelected();
				
		boolean generateWing = (theController.getGenerateWingCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateWing() :
				theController.getGenerateWingCADCheckBox().isSelected();
				
		boolean generateHTail = (theController.getGenerateHTailCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateHTail() :
				theController.getGenerateHTailCADCheckBox().isSelected();
				
		boolean generateVTail = (theController.getGenerateVTailCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateVTail() :
				theController.getGenerateVTailCADCheckBox().isSelected();
				
		boolean generateCanard = (theController.getGenerateCanardCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateCanard() :
				theController.getGenerateCanardCADCheckBox().isSelected();
				
		boolean generateEngines = (theController.getGenerateEnginesCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateEngines() :
				theController.getGenerateEnginesCADCheckBox().isSelected();
				
		boolean generateWingFairing = (theController.getGenerateWingFairingCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateWingFairing() :
				theController.getGenerateWingFairingCADCheckBox().isSelected();
				
		boolean generateCanardFairing = (theController.getGenerateCanardFairingCADCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getGenerateCanardFairing() :
				theController.getGenerateCanardFairingCADCheckBox().isSelected();
				
		int fuselageNoseSectionsNumber = (theController.getFuselageCADNumberNoseSectionsTextField().isDisabled() ||
				                          theController.getFuselageCADNumberNoseSectionsTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getNumberNoseTrunkSections() :
				Integer.valueOf(theController.getFuselageCADNumberNoseSectionsTextField().getText());
				
		int fuselageTailSectionsNumber = (theController.getFuselageCADNumberTailSectionsTextField().isDisabled() ||
										  theController.getFuselageCADNumberTailSectionsTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getNumberTailTrunkSections() :
				Integer.valueOf(theController.getFuselageCADNumberTailSectionsTextField().getText());
				
		XSpacingType fuselageNoseSpacing = (theController.getFuselageCADNoseSpacingChoiceBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeNoseTrunk() :
				XSpacingType.valueOf(theController.getFuselageCADNoseSpacingChoiceBox().getSelectionModel().getSelectedItem());
				
		XSpacingType fuselageTailSpacing = (theController.getFuselageCADTailSpacingChoiceBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getSpacingTypeTailTrunk() :
				XSpacingType.valueOf(theController.getFuselageCADTailSpacingChoiceBox().getSelectionModel().getSelectedItem());
				
		WingTipType wingTipType = (theController.getWingCADTipTypeChoiceBox().isDisabled()) ? 
				Main.getTheCADManager().getTheCADBuilderInterface().getWingTipType() :
				WingTipType.valueOf(theController.getWingCADTipTypeChoiceBox().getSelectionModel().getSelectedItem());
				
		double wingletYOffsetFactor = (theController.getWingletCADYOffsetFactorTextField().isDisabled() || 
									   theController.getWingletCADYOffsetFactorTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getWingletYOffsetFactor() :
				Double.valueOf(theController.getWingletCADYOffsetFactorTextField().getText());
				
		double wingletXOffsetFactor = (theController.getWingletCADXOffsetFactorTextField().isDisabled() || 
									   theController.getWingletCADXOffsetFactorTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getWingletXOffsetFactor() :
				Double.valueOf(theController.getWingletCADXOffsetFactorTextField().getText());
				
		double wingletTaperRatio = (theController.getWingletCADTaperRatioTextField().isDisabled() || 
				   					theController.getWingletCADTaperRatioTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getWingletTaperRatio() :
				Double.valueOf(theController.getWingletCADTaperRatioTextField().getText());
				
		WingTipType hTailTipType = (theController.getHTailCADTipTypeChoiceBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getHTailTipType() :
				WingTipType.valueOf(theController.getHTailCADTipTypeChoiceBox().getSelectionModel().getSelectedItem());
				
		WingTipType vTailTipType = (theController.getVTailCADTipTypeChoiceBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getVTailTipType() :
				WingTipType.valueOf(theController.getVTailCADTipTypeChoiceBox().getSelectionModel().getSelectedItem());
				
		WingTipType canardTipType = (theController.getCanardCADTipTypeChoiceBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getCanardTipType() :
				WingTipType.valueOf(theController.getCanardCADTipTypeChoiceBox().getSelectionModel().getSelectedItem());
				
		double wingFairingFrontLengthFactor = (theController.getWingFairingCADFrontLengthFactorTextField().isDisabled() || 
											   theController.getWingFairingCADFrontLengthFactorTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingFrontLengthFactor() :
				Double.valueOf(theController.getWingFairingCADFrontLengthFactorTextField().getText());
				
		double wingFairingBackLenghtFactor = (theController.getWingFairingCADFrontLengthFactorTextField().isDisabled() || 
				   							  theController.getWingFairingCADFrontLengthFactorTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingBackLengthFactor() :
                Double.valueOf(theController.getWingFairingCADBackLengthFactorTextField().getText());
				
		double wingFairingWidthFactor = (theController.getWingFairingCADWidthFactorTextField().isDisabled() || 
					  					 theController.getWingFairingCADWidthFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingWidthFactor() :
                Double.valueOf(theController.getWingFairingCADWidthFactorTextField().getText());
                
		double wingFairingHeightFactor = (theController.getWingFairingCADHeightFactorTextField().isDisabled() || 
					 					  theController.getWingFairingCADHeightFactorTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingHeightFactor() :
				Double.valueOf(theController.getWingFairingCADHeightFactorTextField().getText());
				
		double wingFairingHeightBelowReferenceFactor = (theController.getWingFairingCADHeightBelowReferenceFactorTextField().isDisabled() || 
				  										theController.getWingFairingCADHeightBelowReferenceFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingHeightBelowReferenceFactor() :
                Double.valueOf(theController.getWingFairingCADHeightBelowReferenceFactorTextField().getText());
                
		double wingFairingHeightAboveReferenceFactor = (theController.getWingFairingCADHeightAboveReferenceFactorTextField().isDisabled() || 
														theController.getWingFairingCADHeightAboveReferenceFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingHeightAboveReferenceFactor() :
                Double.valueOf(theController.getWingFairingCADHeightAboveReferenceFactorTextField().getText());
                
		double wingFairingFilletRadiusFactor = (theController.getWingFairingCADFilletRadiusFactorTextField().isDisabled() || 
												theController.getWingFairingCADFilletRadiusFactorTextField().getText().equals("")) ?
		        Main.getTheCADManager().getTheCADBuilderInterface().getWingFairingFilletRadiusFactor() :
		        Double.valueOf(theController.getWingFairingCADFilletRadiusFactorTextField().getText());
		        
		double canardFairingFrontLengthFactor = (theController.getCanardFairingCADFrontLengthFactorTextField().isDisabled() || 
				   								 theController.getCanardFairingCADFrontLengthFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingFrontLengthFactor() :
                Double.valueOf(theController.getCanardFairingCADFrontLengthFactorTextField().getText());
                
		double canardFairingBackLenghtFactor = (theController.getCanardFairingCADFrontLengthFactorTextField().isDisabled() || 
					  							theController.getCanardFairingCADFrontLengthFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingBackLengthFactor() :
                Double.valueOf(theController.getCanardFairingCADBackLengthFactorTextField().getText());
                
		double canardFairingWidthFactor = (theController.getCanardFairingCADWidthFactorTextField().isDisabled() || 
					 					   theController.getCanardFairingCADWidthFactorTextField().getText().equals("")) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingWidthFactor() :
				Double.valueOf(theController.getCanardFairingCADWidthFactorTextField().getText());
				
		double canardFairingHeightFactor = (theController.getCanardFairingCADHeightFactorTextField().isDisabled() || 
				  							theController.getCanardFairingCADHeightFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingHeightFactor() :
                Double.valueOf(theController.getCanardFairingCADHeightFactorTextField().getText());
                
		double canardFairingHeightBelowReferenceFactor = (theController.getCanardFairingCADHeightBelowReferenceFactorTextField().isDisabled() || 
														  theController.getCanardFairingCADHeightBelowReferenceFactorTextField().getText().equals("")) ?
                Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingHeightBelowReferenceFactor() :
                Double.valueOf(theController.getCanardFairingCADHeightBelowReferenceFactorTextField().getText());
                
		double canardFairingHeightAboveReferenceFactor = (theController.getCanardFairingCADHeightAboveReferenceFactorTextField().isDisabled() || 
														  theController.getCanardFairingCADHeightAboveReferenceFactorTextField().getText().equals("")) ?
		        Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingHeightAboveReferenceFactor() :
		        Double.valueOf(theController.getWingFairingCADHeightAboveReferenceFactorTextField().getText());
		        
		double canardFairingFilletRadiusFactor = (theController.getCanardFairingCADFilletRadiusFactorTextField().isDisabled() || 
												  theController.getCanardFairingCADFilletRadiusFactorTextField().getText().equals("")) ?
			    Main.getTheCADManager().getTheCADBuilderInterface().getCanardFairingFilletRadiusFactor() :
                Double.valueOf(theController.getCanardFairingCADFilletRadiusFactorTextField().getText());
			    
		List<Map<EngineCADComponentsEnum, String>> engineTemplatesFileMapList = new ArrayList<>();
		List<Amount<Angle>> propellerBladeAngleList = new ArrayList<>();
		for (int i = 0; i < theController.getTabPaneCAD3DViewEngines().getTabs().size(); i++) {
			
			String nacelleFile = (theController.getEnginesCADNacelleTemplateFileTextFieldList().get(i).isDisabled()) ? 
					"" : theController.getEnginesCADNacelleTemplateFileTextFieldList().get(i).getText();
			String bladeFile = (theController.getEnginesCADBladeTemplateFileTextFieldList().get(i).isDisabled()) ?
					"" : theController.getEnginesCADBladeTemplateFileTextFieldList().get(i).getText();
			
			HashMap<EngineCADComponentsEnum, String> engineTemplatesFileMap = new HashMap<>();
			engineTemplatesFileMap.put(EngineCADComponentsEnum.NACELLE, nacelleFile);
			engineTemplatesFileMap.put(EngineCADComponentsEnum.BLADE, bladeFile);
			engineTemplatesFileMapList.add(engineTemplatesFileMap);
			
			int bladeAngleUnitIndex = theController.getEnginesCADBladePitchAngleUnitList().get(i).getSelectionModel().getSelectedIndex();	
			String bladeAngleString = (theController.getEnginesCADBladePitchAngleTextFieldList().get(i).isDisabled()) ?
					"0.0" : theController.getEnginesCADBladePitchAngleTextFieldList().get(i).getText();
			if (bladeAngleUnitIndex == 0) {
				propellerBladeAngleList.add(Amount.valueOf(
						Double.valueOf(bladeAngleString), 
						NonSI.DEGREE_ANGLE
						));
			} else {
				propellerBladeAngleList.add(Amount.valueOf(
						Double.valueOf(bladeAngleString), 
						SI.RADIAN
						));
			}			
		}		
		
		FileExtension fileExtension = (theController.getFileExtensionCADChoiceBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getFileExtension() :
				FileExtension.valueOf(theController.getFileExtensionCADChoiceBox().getSelectionModel().getSelectedItem());
				
		boolean exportWireframe = (theController.getExportCADWireframeCheckBox().isDisabled()) ?
				Main.getTheCADManager().getTheCADBuilderInterface().getExportWireframe() :
				theController.getExportCADWireframeCheckBox().isSelected();
		
		ICADManager cadManagerInterface = new ICADManager.Builder()
				.setGenerateFuselage(generateFuselage)
				.setGenerateWing(generateWing)
				.setGenerateHTail(generateHTail)
				.setGenerateVTail(generateVTail)
				.setGenerateCanard(generateCanard)
				.setGenerateWingFairing(generateWingFairing)
				.setGenerateCanardFairing(generateCanardFairing)
				.setSpacingTypeNoseTrunk(fuselageNoseSpacing)
				.setNumberNoseTrunkSections(fuselageNoseSectionsNumber)
				.setSpacingTypeTailTrunk(fuselageTailSpacing)
				.setNumberTailTrunkSections(fuselageTailSectionsNumber)			
				.setWingTipType(wingTipType)
				.setWingletYOffsetFactor(wingletYOffsetFactor)
				.setWingletXOffsetFactor(wingletXOffsetFactor)
				.setWingletTaperRatio(wingletTaperRatio)
				.setHTailTipType(hTailTipType)
				.setVTailTipType(vTailTipType)
				.setCanardTipType(canardTipType)
				.setGenerateEngines(generateEngines)
				.addAllEngineTemplatesList(engineTemplatesFileMapList)
				.addAllPropellerBladePitchAngleList(propellerBladeAngleList)
				.setWingFairingFrontLengthFactor(wingFairingFrontLengthFactor)
				.setWingFairingBackLengthFactor(wingFairingBackLenghtFactor)
				.setWingFairingWidthFactor(wingFairingWidthFactor)
				.setWingFairingHeightFactor(wingFairingHeightFactor)
				.setWingFairingHeightBelowReferenceFactor(wingFairingHeightBelowReferenceFactor)
				.setWingFairingHeightAboveReferenceFactor(wingFairingHeightAboveReferenceFactor)
				.setWingFairingFilletRadiusFactor(wingFairingFilletRadiusFactor)
				.setCanardFairingFrontLengthFactor(canardFairingFrontLengthFactor)
				.setCanardFairingBackLengthFactor(canardFairingBackLenghtFactor)
				.setCanardFairingWidthFactor(canardFairingWidthFactor)
				.setCanardFairingHeightFactor(canardFairingHeightFactor)
				.setCanardFairingHeightBelowReferenceFactor(canardFairingHeightBelowReferenceFactor)
				.setCanardFairingHeightAboveReferenceFactor(canardFairingHeightAboveReferenceFactor)
				.setCanardFairingFilletRadiusFactor(canardFairingFilletRadiusFactor)
				.setExportToFile(false)
				.setFileExtension(fileExtension)
				.setExportWireframe(exportWireframe)
				.build();
		
		CADManager cadManager = new CADManager();
		cadManager.setTheCADBuilderInterface(cadManagerInterface);
		cadManager.setTheAircraft(Main.getTheAircraft());
		
		Main.setTheCADManager(cadManager);
		
		theController.getInputManagerControllerGraphicUtilities().createAircraft3DView();
		
	}
	
	public void saveCADToFileImplementation() {
		
		theController.setSaveCADFileChooser(new FileChooser());
		theController.getSaveCADFileChooser().setTitle("Save as ...");
		theController.getSaveCADFileChooser().setInitialDirectory(
				new File(
						Main.getOutputDirectoryPath() + 
						File.separator + 
						"CAD_output" + 
						File.separator
						)
				);
		
		File file = theController.getSaveCADFileChooser().showSaveDialog(null);	
		
		if (file != null) 
		
			OCCUtils.write(
					file.getParent() + File.separator + file.getName(), 
					Main.getTheCADManager().getTheCADBuilderInterface().getFileExtension(),
					Main.getTheCADManager().getTheAircraftShapes()
					);
		
	}
	
	@SuppressWarnings("rawtypes")
	public void saveAircraftToFileImplementation() throws IOException {
		
		theController.setSaveAircraftFileChooser(new FileChooser());
		theController.getSaveAircraftFileChooser().setTitle("Save as ...");
		theController.getSaveAircraftFileChooser().setInitialDirectory(
				new File(
						Main.getInputDirectoryPath() 
						+ File.separator 
						+ "Template_Aircraft"
						+ File.separator 
						)
				);

		File file = theController.getSaveAircraftFileChooser().showSaveDialog(null);
		
		Service saveAircraftService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {

					@Override
					protected Object call() throws Exception {

						updateProgress(1, 4);
						updateMessage("Saving Aircraft to File ...");
						updateTitle(String.valueOf(25) + "%");

						if (file != null) {

							List<String> wingAirfoilsName = new ArrayList<>();
							List<String> hTailAirfoilsName = new ArrayList<>();
							List<String> vTailAirfoilsName = new ArrayList<>();
							List<String> canardAirfoilsName = new ArrayList<>();

							if(Main.getTheAircraft().getWing() != null) 
								wingAirfoilsName.addAll(
										Main.getTheAircraft().getWing().getAirfoilList().stream()
										.map(a -> a.getName() + "_" + file.getName()  + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getHTail() != null) 
								hTailAirfoilsName.addAll(
										Main.getTheAircraft().getHTail().getAirfoilList().stream()
										.map(a -> a.getName() + "_" + file.getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getVTail() != null) 
								vTailAirfoilsName.addAll(
										Main.getTheAircraft().getVTail().getAirfoilList().stream()
										.map(a -> a.getName() + "_" + file.getName()  + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getCanard() != null) 
								canardAirfoilsName.addAll(
										Main.getTheAircraft().getCanard().getAirfoilList().stream()
										.map(a -> a.getName() + "_" + file.getName()  + ".xml")
										.collect(Collectors.toList())
										);

							updateProgress(2, 4);
							updateMessage("Creating airfoil file name lists ...");
							updateTitle(String.valueOf(50) + "%");
							
							AircraftSaveDirectives asd = new AircraftSaveDirectives
									.Builder("_" + file.getName())
									.setAircraftFileName("aircraft_" + file.getName())
									.addAllWingAirfoilFileNames(wingAirfoilsName)
									.addAllHTailAirfoilFileNames(hTailAirfoilsName)
									.addAllVTailAirfoilFileNames(vTailAirfoilsName)
									.addAllCanardAirfoilFileNames(canardAirfoilsName)
									.build();

							updateProgress(3, 4);
							updateMessage("Creating Aircraft Save Directives ...");
							updateTitle(String.valueOf(75) + "%");
							
							JPADStaticWriteUtils.saveAircraftToXML(
									Main.getTheAircraft(), 
									file.getParent() + File.separator, 
									file.getName(), 
									asd);

							updateProgress(4, 4);
							updateMessage("Aircraft saved!");
							updateTitle(String.valueOf(100) + "%");
							
							Main.setAircraftSaved(true);
							
							ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();

							try {
								aircraftSavedFlag.set(Main.getAircraftSaved());
								theController.getSaveAircraftButton().disableProperty().bind(
										Bindings.equal(aircraftSavedFlag, true)
										);
							} catch (Exception e) {
								theController.getSaveAircraftButton().setDisable(true);
							}
						}
						else 
							cancel();
						
						//Block the thread for a short time, but be sure
			            //to check the InterruptedException for cancellation
			            try {
			                Thread.sleep(100);
			            } catch (InterruptedException interrupted) {
			                if (isCancelled()) {
			                    updateMessage("Cancelled");
			                    updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			                else {
			                	updateMessage("Terminated");
			                	updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
			                    return null;
			                }
			            }
						
						return null;
					}
				};
			}
		};
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().progressProperty().bind(saveAircraftService.progressProperty());
		Main.getStatusBar().textProperty().bind(saveAircraftService.messageProperty());
		Main.getTaskPercentage().textProperty().bind(saveAircraftService.titleProperty());
		
		saveAircraftService.start();
		
	}
	
	@SuppressWarnings("rawtypes")
	public void updateAircraftDataImplementation() {
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// FUSELAGE ADJUST DIALOG
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (theController.getFuselageAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem() != null
				&& !theController.getFuselageAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem().equalsIgnoreCase("NONE")) {
			
			//................................................................................................
			// ASSIGNING TEXTFIELDS STYLE ...
			theController.getTextFieldFuselageLength().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldFuselageNoseLengthRatio().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldFuselageCylinderLengthRatio().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldFuselageCylinderSectionHeight().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldFuselageCylinderSectionWidth().setStyle(theController.getTextFieldAlertStyle());

			//................................................................................................
			// ADJUSTING MEASURES ...
			Stage fuselageAdjustCriterionDialog = new FuselageAdjustCriterionDialog(
					Main.getPrimaryStage(), 
					theController.getFuselageAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem()
					);
			fuselageAdjustCriterionDialog.sizeToScene();
			fuselageAdjustCriterionDialog.initStyle(StageStyle.UNDECORATED);
			fuselageAdjustCriterionDialog.showAndWait();

			//.................................................................................................
			// REMOVING TEXTFIELDS STYLE ...
			theController.getTextFieldFuselageLength().setStyle("");
			theController.getTextFieldFuselageNoseLengthRatio().setStyle("");
			theController.getTextFieldFuselageCylinderLengthRatio().setStyle("");
			theController.getTextFieldFuselageCylinderSectionHeight().setStyle("");
			theController.getTextFieldFuselageCylinderSectionWidth().setStyle("");

			//.................................................................................................
			// SETTING NEW MEASURE DATA TO TEXTFIELDS ...
			if (Main.getTheAircraft().getFuselage().getFuselageLength() != null) {
				theController.getTextFieldFuselageLength().setText(
						String.valueOf(Main.getTheAircraft().getFuselage().getFuselageLength().getEstimatedValue())
						);

				if (Main.getTheAircraft().getFuselage().getFuselageLength()
						.getUnit().toString().equalsIgnoreCase("m"))
					theController.getFuselageLengthUnitChoiceBox().getSelectionModel().select(0);
				else if (Main.getTheAircraft().getFuselage().getFuselageLength()
						.getUnit().toString().equalsIgnoreCase("ft"))
					theController.getFuselageLengthUnitChoiceBox().getSelectionModel().select(1);

			}

			if (Main.getTheAircraft().getFuselage().getNoseLengthRatio() != null) 
				theController.getTextFieldFuselageNoseLengthRatio().setText(
						String.valueOf(Main.getTheAircraft().getFuselage().getNoseLengthRatio())
						);
			if (Main.getTheAircraft().getFuselage().getCylinderLengthRatio() != null) 
				theController.getTextFieldFuselageCylinderLengthRatio().setText(
						String.valueOf(Main.getTheAircraft().getFuselage().getCylinderLengthRatio())
						);

			if (Main.getTheAircraft().getFuselage().getSectionCylinderHeight() != null) {
				theController.getTextFieldFuselageCylinderSectionHeight().setText(
						String.valueOf(Main.getTheAircraft().getFuselage().getSectionCylinderHeight().getEstimatedValue())
						);

				if (Main.getTheAircraft().getFuselage().getSectionCylinderHeight()
						.getUnit().toString().equalsIgnoreCase("m"))
					theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(0);
				else if (Main.getTheAircraft().getFuselage().getSectionCylinderHeight()
						.getUnit().toString().equalsIgnoreCase("ft"))
					theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(1);

			}

			if (Main.getTheAircraft().getFuselage().getSectionCylinderWidth() != null) {
				theController.getTextFieldFuselageCylinderSectionWidth().setText(
						String.valueOf(Main.getTheAircraft().getFuselage().getSectionCylinderWidth().getEstimatedValue())
						);

				if (Main.getTheAircraft().getFuselage().getSectionCylinderWidth()
						.getUnit().toString().equalsIgnoreCase("m"))
					theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(0);
				else if (Main.getTheAircraft().getFuselage().getSectionCylinderWidth()
						.getUnit().toString().equalsIgnoreCase("ft"))
					theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(1);

			}

		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// WING ADJUST DIALOG
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (theController.getWingAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem() != null
				&& !theController.getWingAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem().equalsIgnoreCase("NONE")) {
			
			//................................................................................................
			// ASSIGNING TEXTFIELDS STYLE ...
			theController.getTextFieldEquivalentWingArea().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldEquivalentWingAspectRatio().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldEquivalentWingSweepLeadingEdge().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldEquivalentWingTaperRatio().setStyle(theController.getTextFieldAlertStyle());
			theController.getTextFieldWingSpanPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldWingSweepLEPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldWingInnerChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldWingOuterChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));

			//................................................................................................
			// ADJUSTING MEASURES ...
			Stage wingAdjustCriterionDialog = new LiftingSurfaceAdjustCriterionDialog(
					Main.getPrimaryStage(), 
					theController.getWingAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem(),
					ComponentEnum.WING
					);
			wingAdjustCriterionDialog.sizeToScene();
			wingAdjustCriterionDialog.initStyle(StageStyle.UNDECORATED);
			wingAdjustCriterionDialog.showAndWait();

			//.................................................................................................
			// REMOVING TEXTFIELDS STYLE ...
			theController.getTextFieldEquivalentWingArea().setStyle("");
			theController.getTextFieldEquivalentWingAspectRatio().setStyle("");
			theController.getTextFieldEquivalentWingSweepLeadingEdge().setStyle("");
			theController.getTextFieldEquivalentWingTaperRatio().setStyle("");
			theController.getTextFieldWingSpanPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldWingSweepLEPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldWingInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldWingOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));

			//.................................................................................................
			// SETTING NEW MEASURE DATA TO TEXTFIELDS ...
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AREA:
			if (Main.getTheAircraft().getWing().getSurfacePlanform() != null) {
				theController.getTextFieldEquivalentWingArea().setText(
						String.valueOf(Main.getTheAircraft().getWing().getSurfacePlanform().getEstimatedValue())
						);

				if (Main.getTheAircraft().getWing().getSurfacePlanform()
						.getUnit().toString().equalsIgnoreCase("m²"))
					theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().select(0);
				else if (Main.getTheAircraft().getFuselage().getFuselageLength()
						.getUnit().toString().equalsIgnoreCase("ft²"))
					theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().select(1);

			}

			//---------------------------------------------------------------------------------
			// EQUIVALENT WING ASPECT RATIO:
			if (Main.getTheAircraft().getWing().getAspectRatio() != null) 
				theController.getTextFieldEquivalentWingAspectRatio().setText(
						String.valueOf(Main.getTheAircraft().getWing().getAspectRatio())
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING SWEEP LEADING EDGE:
			if (Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge() != null) {
				theController.getTextFieldEquivalentWingSweepLeadingEdge().setText(
						String.valueOf(Main.getTheAircraft().getWing().getEquivalentWing()
								.getPanels().get(0).getSweepLeadingEdge().getEstimatedValue()
								)
						);

				if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().select(1);

			}
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TAPER RATIO:
			if (Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio() != null) 
				theController.getTextFieldEquivalentWingTaperRatio().setText(
						String.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio())
						);
			
			for (int i=0; i<Main.getTheAircraft().getWing().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getWing().getPanels().get(i);
				
				theController.getTextFieldWingInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				theController.getTextFieldWingOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldWingSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxWingSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxWingSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldWingSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldWingInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxWingInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxWingInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldWingOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxWingOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxWingOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
			}
			
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// HORIZONTAL TAIL ADJUST DIALOG
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (theController.gethTailAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem() != null
				&& !theController.gethTailAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem().equalsIgnoreCase("NONE")) {
			
			//................................................................................................
			// ASSIGNING TEXTFIELDS STYLE ...
			theController.getTextFieldHTailSpanPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldHTailSweepLEPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldHTailInnerChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldHTailOuterChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));

			//................................................................................................
			// ADJUSTING MEASURES ...
			Stage hTailAdjustCriterionDialog = new LiftingSurfaceAdjustCriterionDialog(
					Main.getPrimaryStage(), 
					theController.gethTailAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem(),
					ComponentEnum.HORIZONTAL_TAIL
					);
			hTailAdjustCriterionDialog.sizeToScene();
			hTailAdjustCriterionDialog.initStyle(StageStyle.UNDECORATED);
			hTailAdjustCriterionDialog.showAndWait();

			//.................................................................................................
			// REMOVING TEXTFIELDS STYLE ...
			theController.getTextFieldHTailSpanPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldHTailSweepLEPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldHTailInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldHTailOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));

			//.................................................................................................
			// SETTING NEW MEASURE DATA TO TEXTFIELDS ...
			for (int i=0; i<Main.getTheAircraft().getHTail().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getHTail().getPanels().get(i);
				
				theController.getTextFieldHTailInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				theController.getTextFieldHTailOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldHTailSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxHTailSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxHTailSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldHTailSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldHTailInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxHTailInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxHTailInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldHTailOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxHTailOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxHTailOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
			}
			
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// VERTICAL TAIL ADJUST DIALOG
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (theController.getvTailAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem() != null
				&& !theController.getvTailAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem().equalsIgnoreCase("NONE")) {
			
			//................................................................................................
			// ASSIGNING TEXTFIELDS STYLE ...
			theController.getTextFieldVTailSpanPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldVTailSweepLEPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldVTailInnerChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldVTailOuterChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));

			//................................................................................................
			// ADJUSTING MEASURES ...
			Stage vTailAdjustCriterionDialog = new LiftingSurfaceAdjustCriterionDialog(
					Main.getPrimaryStage(), 
					theController.getvTailAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem(),
					ComponentEnum.VERTICAL_TAIL
					);
			vTailAdjustCriterionDialog.sizeToScene();
			vTailAdjustCriterionDialog.initStyle(StageStyle.UNDECORATED);
			vTailAdjustCriterionDialog.showAndWait();

			//.................................................................................................
			// REMOVING TEXTFIELDS STYLE ...
			theController.getTextFieldVTailSpanPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldVTailSweepLEPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldVTailInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldVTailOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));

			//.................................................................................................
			// SETTING NEW MEASURE DATA TO TEXTFIELDS ...
			for (int i=0; i<Main.getTheAircraft().getVTail().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getVTail().getPanels().get(i);
				
				theController.getTextFieldVTailInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				theController.getTextFieldVTailOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldVTailSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxVTailSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxVTailSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldVTailSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldVTailInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxVTailInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxVTailInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldVTailOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxVTailOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxVTailOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
			}
			
		}
		
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		// CANARD ADJUST DIALOG
		////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (theController.getCanardAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem() != null
				&& !theController.getCanardAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem().equalsIgnoreCase("NONE")) {
			
			//................................................................................................
			// ASSIGNING TEXTFIELDS STYLE ...
			theController.getTextFieldCanardSpanPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldCanardSweepLEPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldCanardInnerChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));
			theController.getTextFieldCanardOuterChordPanelList().stream().forEach(tf -> tf.setStyle(theController.getTextFieldAlertStyle()));

			//................................................................................................
			// ADJUSTING MEASURES ...
			Stage canardAdjustCriterionDialog = new LiftingSurfaceAdjustCriterionDialog(
					Main.getPrimaryStage(), 
					theController.getCanardAdjustCriterionChoiceBox().getSelectionModel().getSelectedItem(),
					ComponentEnum.CANARD
					);
			canardAdjustCriterionDialog.sizeToScene();
			canardAdjustCriterionDialog.initStyle(StageStyle.UNDECORATED);
			canardAdjustCriterionDialog.showAndWait();

			//.................................................................................................
			// REMOVING TEXTFIELDS STYLE ...
			theController.getTextFieldCanardSpanPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldCanardSweepLEPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldCanardInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
			theController.getTextFieldCanardOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));

			//.................................................................................................
			// SETTING NEW MEASURE DATA TO TEXTFIELDS ...
			for (int i=0; i<Main.getTheAircraft().getCanard().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getCanard().getPanels().get(i);
				
				theController.getTextFieldCanardInnerChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				theController.getTextFieldCanardOuterChordPanelList().stream().forEach(tf -> tf.setStyle(""));
				
				//---------------------------------------------------------------------------------
				// PANEL SPAN:
				if(currentPanel.getSpan() != null) {
					
					theController.getTextFieldCanardSpanPanelList().get(i).setText(
							String.valueOf(currentPanel.getSpan().getEstimatedValue())
							);
					
					if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxCanardSpanPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSpan().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxCanardSpanPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// SWEEP LE:
				if(currentPanel.getSweepLeadingEdge() != null) {
					
					theController.getTextFieldCanardSweepLEPanelList().get(i).setText(
							String.valueOf(currentPanel.getSweepLeadingEdge().getEstimatedValue())
							);
					
					if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardSweepLEPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardSweepLEPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// INNER CHORD (ROOT):
				if(currentPanel.getChordRoot() != null) {
					
					theController.getTextFieldCanardInnerChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordRoot().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxCanardInnerChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxCanardInnerChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD (TIP):
				if(currentPanel.getChordTip() != null) {
					
					theController.getTextFieldCanardOuterChordPanelList().get(i).setText(
							String.valueOf(currentPanel.getChordTip().getEstimatedValue())
							);
					
					if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxCanardOuterChordPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getChordRoot().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxCanardOuterChordPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				
			}
			
		}
		
		Service updateAircraftDataService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {

					@Override
					protected Object call() throws Exception {
						
						int numberOfOperations = 37;
						int progressIncrement = 100/numberOfOperations;
						
						// DATA UPDATE 
						/* TODO: MODIFY PRE-EXISTING AIRCRAFT COMPONENTS OBJECT INSTEAD OF BUILDING NEW ONE AND THEN SET */  
						updateProgress(0, numberOfOperations);
						updateMessage("Start Updating Aircraft Data ...");
						updateTitle(String.valueOf(0) + "%");
						
						updateProgress(1, numberOfOperations);
						updateMessage("Updating Fuselage Tab Data ...");
						updateTitle(String.valueOf(progressIncrement) + "%");
						if(!theController.isUpdateFuselageDataFromFile()) {
							if(Main.getTheAircraft().getFuselage() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateFuselageTabData();
							}
						}
						
						updateProgress(2, numberOfOperations);
						updateMessage("Updating Cabin Configuration Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*2) + "%");
						if(!theController.isUpdateCabinConfigurationDataFromFile()) {
							if(Main.getTheAircraft().getCabinConfiguration() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateCabinConfigurationTabData();
							}
						}
						
						updateProgress(3, numberOfOperations);
						updateMessage("Updating Wing Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*3) + "%");
						if(!theController.isUpdateWingDataFromFile()) {
							if(Main.getTheAircraft().getWing() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateWingTabData();
							}						
						}
						
						updateProgress(4, numberOfOperations);
						updateMessage("Updating Horizontal Tail Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*4) + "%");
						if(!theController.isUpdateHTailDataFromFile()) {
							if(Main.getTheAircraft().getHTail() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateHTailTabData();
							}						
						}
						
						updateProgress(5, numberOfOperations);
						updateMessage("Updating Vertical Tail Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*5) + "%");
						if(!theController.isUpdateVTailDataFromFile()) {
							if(Main.getTheAircraft().getVTail() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateVTailTabData();
							}
						}
						
						updateProgress(6, numberOfOperations);
						updateMessage("Updating Canard Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*6)+ "%");
						if(!theController.isUpdateCanardDataFromFile()) {					
							if(Main.getTheAircraft().getCanard() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateCanardTabData();
							}
						}						
						
						updateProgress(7, numberOfOperations);
						updateMessage("Updating Nacelles Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*7) + "%");
						if(!theController.isUpdateNacellesDataFromFile()) {
							if(Main.getTheAircraft().getNacelles() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateNacelleTabData();
							}
						}
						
						updateProgress(8, numberOfOperations);
						updateMessage("Updating Power Plant Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*8) + "%");
						if(!theController.isUpdatePowerPlantDataFromFile()) {
							if(Main.getTheAircraft().getPowerPlant() != null) {
								theController.getInputManagerControllerUpdateUtilites().updatePowerPlantTabData();
							}
						}
						
						updateProgress(9, numberOfOperations);
						updateMessage("Updating Landing Gears Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*9) + "%");
						if(!theController.isUpdateLandingGearsDataFromFile()) {
							if(Main.getTheAircraft().getLandingGears() != null) {
								theController.getInputManagerControllerUpdateUtilites().updateLandingGearsTabData();
							}
						}
						
						updateProgress(10, numberOfOperations);
						updateMessage("Updating Aircraft Tab Data ...");
						updateTitle(String.valueOf(progressIncrement*10) + "%");
						theController.getInputManagerControllerUpdateUtilites().updateAircraftTabData();
						
						updateProgress(11, numberOfOperations);
						updateMessage("Creating the Aircraft Object ...");
						updateTitle(String.valueOf(progressIncrement*11) + "%");
						theController.getInputManagerControllerUpdateUtilites().createAircraftObjectFromData();
						
						// COMPONENTS LOG TO INTERFACE
						if (Main.getTheAircraft() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaAircraftConsoleOutput().setText(Main.getTheAircraft().toString());
									
								}
							});
							updateProgress(12, numberOfOperations);
							updateMessage("Logging Aircraft Object Data...");
							updateTitle(String.valueOf(progressIncrement*12) + "%");
						}
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaFuselageConsoleOutput().setText(
											Main.getTheAircraft().getFuselage().toString()
											);
									
								}
							});							
							updateProgress(13, numberOfOperations);
							updateMessage("Logging Fuselage Object Data...");
							updateTitle(String.valueOf(progressIncrement*13) + "%");
						}
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaCabinConfigurationConsoleOutput().setText(
											Main.getTheAircraft().getCabinConfiguration().toString()
											);
									
								}
							});				
							updateProgress(14, numberOfOperations);
							updateMessage("Logging Cabin Configuration Object Data...");
							updateTitle(String.valueOf(progressIncrement*14) + "%");
						}
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaWingConsoleOutput().setText(
											Main.getTheAircraft().getWing().toString()
											);
									
								}
							});				
							updateProgress(15, numberOfOperations);
							updateMessage("Logging Wing Object Data...");
							updateTitle(String.valueOf(progressIncrement*15) + "%");
						}
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaHTailConsoleOutput().setText(
											Main.getTheAircraft().getHTail().toString()
											);
									
								}
							});		
							updateProgress(16, numberOfOperations);
							updateMessage("Logging Horizontal Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*16) + "%");
						}
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaVTailConsoleOutput().setText(
											Main.getTheAircraft().getVTail().toString()
											);
									
								}
							});		
							updateProgress(17, numberOfOperations);
							updateMessage("Logging Vertical Tail Object Data...");
							updateTitle(String.valueOf(progressIncrement*17) + "%");
						}
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaCanardConsoleOutput().setText(
											Main.getTheAircraft().getCanard().toString()
											);
									
								}
							});		
							updateProgress(18, numberOfOperations);
							updateMessage("Logging Canard Object Data...");
							updateTitle(String.valueOf(progressIncrement*18) + "%");
						}
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaNacelleConsoleOutput().setText(
											Main.getTheAircraft().getNacelles().toString()
											);
									
								}
							});	
							updateProgress(19, numberOfOperations);
							updateMessage("Logging Nacelle Object Data...");
							updateTitle(String.valueOf(progressIncrement*19) + "%");
						}
						if (Main.getTheAircraft().getPowerPlant() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaPowerPlantConsoleOutput().setText(
											Main.getTheAircraft().getPowerPlant().toString()
											);
									
								}
							});	
							updateProgress(20, numberOfOperations);
							updateMessage("Logging Power Plant Object Data...");
							updateTitle(String.valueOf(progressIncrement*20) + "%");
						}
						if (Main.getTheAircraft().getLandingGears() != null) {
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									theController.getTextAreaLandingGearsConsoleOutput().setText(
											Main.getTheAircraft().getLandingGears().toString()
											);
									
								}
							});	
							updateProgress(21, numberOfOperations);
							updateMessage("Logging Landing Gears Object Data...");
							updateTitle(String.valueOf(progressIncrement*21) + "%");
						}
						//............................
						// COMPONENTS 3 VIEW CREATION
						//............................
						// aircraft
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								theController.getInputManagerControllerGraphicUtilities().createAircraftTopView();
								
							}
						});	
						updateProgress(22, numberOfOperations);
						updateMessage("Creating Aircraft Top View...");
						updateTitle(String.valueOf(progressIncrement*22) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								theController.getInputManagerControllerGraphicUtilities().createAircraftSideView();
								
							}
						});	
						updateProgress(23, numberOfOperations);
						updateMessage("Creating Aircraft Side View...");
						updateTitle(String.valueOf(progressIncrement*23) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								theController.getInputManagerControllerGraphicUtilities().createAircraftFrontView();
								
							}
						});	
						updateProgress(24, numberOfOperations);
						updateMessage("Creating Aircraft Front View...");
						updateTitle(String.valueOf(progressIncrement*24) + "%");
						
						//............................
						// fuselage
						if (Main.getTheAircraft().getFuselage() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createFuselageTopView();

								}
							});	
							updateProgress(25, numberOfOperations);
							updateMessage("Creating Fuselage Top View...");
							updateTitle(String.valueOf(progressIncrement*25) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createFuselageSideView();

								}
							});	
							updateProgress(26, numberOfOperations);
							updateMessage("Creating Fuselage Side View...");
							updateTitle(String.valueOf(progressIncrement*26) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createFuselageFrontView();

								}
							});	
							updateProgress(27, numberOfOperations);
							updateMessage("Creating Fuselage Front View...");
							updateTitle(String.valueOf(progressIncrement*27) + "%");
						}
						//............................
						// cabin configuration
						if (Main.getTheAircraft().getCabinConfiguration() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createSeatMap();

								}
							});	
							updateProgress(28, numberOfOperations);
							updateMessage("Creating Seat Map...");
							updateTitle(String.valueOf(progressIncrement*28) + "%");
						}
						//............................
						// wing
						if (Main.getTheAircraft().getWing() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createWingPlanformView();

								}
							});	
							updateProgress(29, numberOfOperations);
							updateMessage("Creating Wing Planform View...");
							updateTitle(String.valueOf(progressIncrement*29) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createEquivalentWingView();

								}
							});	
							updateProgress(30, numberOfOperations);
							updateMessage("Creating Equivalent Wing View...");
							updateTitle(String.valueOf(progressIncrement*30) + "%");
						}
						//............................
						// hTail
						if (Main.getTheAircraft().getHTail() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createHTailPlanformView();

								}
							});	
							updateProgress(31, numberOfOperations);
							updateMessage("Creating HTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*31) + "%");
						}
						//............................
						// vTail
						if (Main.getTheAircraft().getVTail() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createVTailPlanformView();

								}
							});	
							updateProgress(32, numberOfOperations);
							updateMessage("Creating VTail Planform View...");
							updateTitle(String.valueOf(progressIncrement*32) + "%");
						}
						//............................
						// canard
						if (Main.getTheAircraft().getCanard() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createCanardPlanformView();

								}
							});	
							updateProgress(33, numberOfOperations);
							updateMessage("Creating Canard Planform View...");
							updateTitle(String.valueOf(progressIncrement*33) + "%");
						}
						//............................
						// nacelle
						if (Main.getTheAircraft().getNacelles() != null) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createNacelleTopView();

								}
							});	
							updateProgress(34, numberOfOperations);
							updateMessage("Creating Nacelles Top View...");
							updateTitle(String.valueOf(progressIncrement*34) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createNacelleSideView();

								}
							});	
							updateProgress(35, numberOfOperations);
							updateMessage("Creating Nacelles Side View...");
							updateTitle(String.valueOf(progressIncrement*35) + "%");
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									theController.getInputManagerControllerGraphicUtilities().createNacelleFrontView();

								}
							});	
							updateProgress(36, numberOfOperations);
							updateMessage("Creating Nacelles FrontView...");
							updateTitle(String.valueOf(progressIncrement*36) + "%");	
						}

						updateProgress(37, numberOfOperations);
						updateMessage("Aircraft Updated!");
						updateTitle(String.valueOf(100) + "%");

						//Block the thread for a short time, but be sure
						//to check the InterruptedException for cancellation
						try {
							Thread.sleep(100);
						} catch (InterruptedException interrupted) {
							if (isCancelled()) {
								updateMessage("Cancelled");
								updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
								return null;
							}
							else {
								updateMessage("Terminated");
								updateProgress(0, 4);
								updateTitle(String.valueOf(0) + "%");
								return null;
							}
						}

			            Main.setAircraftSaved(false);
						Main.setAircraftUpdated(true);
			            
						ObjectProperty<Boolean> aircraftSavedFlag = new SimpleObjectProperty<>();

						try {
							aircraftSavedFlag.set(Main.getAircraftSaved());
							theController.getSaveAircraftButton().disableProperty().bind(
									Bindings.equal(aircraftSavedFlag, true)
									);
						} catch (Exception e) {
							theController.getSaveAircraftButton().setDisable(true);
						}
						
						theController.getInputManagerControllerSecondaryActionUtilities().cad3DViewFieldsDisableCheck();
			            
						return null;
					}
				};
			}
		};
		
		Main.getProgressBar().progressProperty().unbind();
		Main.getStatusBar().textProperty().unbind();
		Main.getTaskPercentage().textProperty().unbind();
		
		Main.getProgressBar().progressProperty().bind(updateAircraftDataService.progressProperty());
		Main.getStatusBar().textProperty().bind(updateAircraftDataService.messageProperty());
		Main.getTaskPercentage().textProperty().bind(updateAircraftDataService.titleProperty());
		
		updateAircraftDataService.start();
		
	}
	
}

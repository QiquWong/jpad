package jpadcommander.inputmanager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.util.ShapeUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import aircraft.components.cabinconfiguration.ISeatBlock;
import aircraft.components.cabinconfiguration.SeatsBlock;
import aircraft.components.liftingSurface.creator.AsymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import calculators.geometry.FusNacGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.RelativePositionEnum;
import configuration.enumerations.WindshieldTypeEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import graphics.ChartCanvas;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javaslang.Tuple;
import javaslang.Tuple2;
import jpadcommander.Main;
import standaloneutils.GeometryCalc;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
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
		
		Main.setTheAircraft(null);

		ObjectProperty<Aircraft> aircraft = new SimpleObjectProperty<>();
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
		theController.getTextFieldAircraftLandingGearsX().clear();
		theController.getLandingGearsXUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftLandingGearsY().clear();
		theController.getLandingGearsYUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldAircraftLandingGearsZ().clear();
		theController.getLandingGearsZUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		theController.getTextAreaAircraftConsoleOutput().clear();
		theController.getAircraftTopViewPane().getChildren().clear();
		theController.getAircraftSideViewPane().getChildren().clear();
		theController.getAircraftFrontViewPane().getChildren().clear();
		
	}
	
	private void cleanFuselageData() {
		
		theController.getFuselageAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
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
		theController.getTextFieldMaximumPassengersNumber().clear();
		theController.getTextFieldFlightCrewNumber().clear();
		theController.getTextFieldClassesNumber().clear();
		theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().clearSelection();
		theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().clearSelection();
		theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().clearSelection();
		theController.getTextFieldAislesNumber().clear();
		theController.getTextFieldXCoordinateFirstRow().clear();
		theController.getTextFieldMissingSeatRow1().clear();
		theController.getTextFieldMissingSeatRow2().clear();
		theController.getTextFieldMissingSeatRow3().clear();
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
		theController.getTextFieldMassFurnishingsAndEquipment().clear();
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
		theController.getCabinConfigurationMassFurnishingsAndEquipmentUnitChoiceBox().getSelectionModel().clearSelection();
		
		// 3 View and TextArea
		theController.getTextAreaCabinConfigurationConsoleOutput().clear();
		theController.getCabinConfigurationSeatMapPane().getChildren().clear();
		
	}
	
	private void cleanWingData() {
		
		theController.getWingAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getEquivalentWingCheckBox().setSelected(false);
		theController.getTextFieldWingMainSparAdimensionalPosition().clear();
		theController.getTextFieldWingSecondarySparAdimensionalPosition().clear();
		theController.getTextFieldWingCompositeMassCorrectionFactor().clear();
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
		theController.getWingMinimumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getWingMaximumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().clearSelection();
		theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().clearSelection();
		
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
		theController.getTextFieldWingInnerPositionAileronLeft().clear();
		theController.getTextFieldWingInnerPositionAileronLeft().clear();
		theController.getTextFieldWingOuterPositionAileronLeft().clear();
		theController.getTextFieldWingInnerChordRatioAileronLeft().clear();
		theController.getTextFieldWingOuterChordRatioAileronLeft().clear();
		theController.getTextFieldWingMinimumDeflectionAngleAileronLeft().clear();
		theController.getTextFieldWingMaximumDeflectionAngleAileronLeft().clear();
		theController.getTextFieldWingInnerPositionAileronRight().clear();
		theController.getTextFieldWingOuterPositionAileronRight().clear();
		theController.getTextFieldWingInnerChordRatioAileronRight().clear();
		theController.getTextFieldWingOuterChordRatioAileronRight().clear();
		theController.getTextFieldWingMinimumDeflectionAngleAileronRight().clear();
		theController.getTextFieldWingMaximumDeflectionAngleAileronRight().clear();
		
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
		
		theController.gethTailAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldHTailCompositeMassCorrectionFactor().clear();
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
		
		theController.getvTailAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldVTailCompositeMassCorrectionFactor().clear();
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
		
		theController.getCanardAdjustCriterionChoiceBox().getSelectionModel().clearSelection();
		theController.getTextFieldCanardCompositeMassCorrectionFactor().clear();
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
	
	@SuppressWarnings({ "rawtypes" })
	public void loadAircraftFileImplementation() throws IOException, InterruptedException {
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
//		System.setOut(filterStream);
		
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
			
		}
		
		Service loadAircraftService = new Service() {

			@Override
			protected Task createTask() {
				return new Task() {
					
					@Override
					protected Object call() throws Exception {
						
						System.setOut(filterStream);
						
						int numberOfOperations = 28;
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
									logAircraftFromFileToInterface();
									
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
									logFuselageFromFileToInterface();
									
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
									logCabinConfigutionFromFileToInterface();
									
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
									logWingFromFileToInterface();
									
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
									logHTailFromFileToInterface();
									
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
									logVTailFromFileToInterface();
									
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
									logCanardFromFileToInterface();
									
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
									logNacelleFromFileToInterface();
									
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
									logPowerPlantFromFileToInterface();
									
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
									logLandingGearsFromFileToInterface();
									
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
								createAircraftTopView();
								
							}
						});	
						updateProgress(13, numberOfOperations);
						updateMessage("Creating Aircraft Top View...");
						updateTitle(String.valueOf(progressIncrement*13) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftSideView();
								
							}
						});	
						updateProgress(14, numberOfOperations);
						updateMessage("Creating Aircraft Side View...");
						updateTitle(String.valueOf(progressIncrement*14) + "%");
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								createAircraftFrontView();
								
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
									createFuselageTopView();
									
								}
							});	
							updateProgress(16, numberOfOperations);
							updateMessage("Creating Fuselage Top View...");
							updateTitle(String.valueOf(progressIncrement*16) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createFuselageSideView();
									
								}
							});	
							updateProgress(17, numberOfOperations);
							updateMessage("Creating Fuselage Side View...");
							updateTitle(String.valueOf(progressIncrement*17) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createFuselageFrontView();
									
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
									createSeatMap();
									
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
									createWingPlanformView();
									
								}
							});	
							updateProgress(20, numberOfOperations);
							updateMessage("Creating Wing Planform View...");
							updateTitle(String.valueOf(progressIncrement*20) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createEquivalentWingView();
									
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
									createHTailPlanformView();
									
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
									createVTailPlanformView();
									
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
									createCanardPlanformView();
									
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
									createNacelleTopView();
									
								}
							});	
							updateProgress(25, numberOfOperations);
							updateMessage("Creating Nacelles Top View...");
							updateTitle(String.valueOf(progressIncrement*25) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createNacelleSideView();
									
								}
							});	
							updateProgress(26, numberOfOperations);
							updateMessage("Creating Nacelles Side View...");
							updateTitle(String.valueOf(progressIncrement*26) + "%");
							Platform.runLater(new Runnable() {
								
								@Override
								public void run() {
									createNacelleFrontView();
									
								}
							});	
							updateProgress(27, numberOfOperations);
							updateMessage("Creating Nacelles FrontView...");
							updateTitle(String.valueOf(progressIncrement*27) + "%");	
						}

						updateProgress(28, numberOfOperations);
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
	
	public void createAircraftTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if(Main.getTheAircraft().getFuselage() != null) {
			// left curve, upperview
			List<Amount<Length>> vX1Left = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
			int nX1Left = vX1Left.size();
			List<Amount<Length>> vY1Left = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

			// right curve, upperview
			List<Amount<Length>> vX2Right = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));;
			int nX2Right = vX2Right.size();
			List<Amount<Length>> vY2Right = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));;

			IntStream.range(0, nX1Left)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Right)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingTopView = new XYSeries("Wing - Top View", false);
		
		if (Main.getTheAircraft().getWing() != null) {
			Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);

			IntStream.range(0, dataTopViewIsolated.length)
			.forEach(i -> {
				seriesWingTopView.add(
						dataTopViewIsolated[i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolated[i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolated.length)
			.forEach(i -> {
				seriesWingTopView.add(
						dataTopViewIsolated[dataTopViewIsolated.length-1-i][1] + Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
						-dataTopViewIsolated[dataTopViewIsolated.length-1-i][0] + Main.getTheAircraft().getWing().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailTopView = new XYSeries("HTail - Top View", false);
		
		if (Main.getTheAircraft().getHTail() != null) {
			Double[][] dataTopViewIsolatedHTail = Main.getTheAircraft().getHTail().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);

			IntStream.range(0, dataTopViewIsolatedHTail.length)
			.forEach(i -> {
				seriesHTailTopView.add(
						dataTopViewIsolatedHTail[i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedHTail[i][0] + Main.getTheAircraft().getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedHTail.length)
			.forEach(i -> {
				seriesHTailTopView.add(
						dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][1] + Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedHTail[dataTopViewIsolatedHTail.length-1-i][0] + Main.getTheAircraft().getHTail().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailRootAirfoilTopView = new XYSeries("VTail Root - Top View", false);
		XYSeries seriesVTailTipAirfoilTopView = new XYSeries("VTail Tip - Top View", false);
		
		if (Main.getTheAircraft().getVTail() != null) {

			double[] vTailRootXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getXCoords();
			double[] vTailRootYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(0).getZCoords();
			double[] vTailTipXCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getXCoords();
			double[] vTailTipYCoordinates = Main.getTheAircraft().getVTail().getAirfoilList().get(Main.getTheAircraft().getVTail().getAirfoilList().size()-1).getZCoords();
			int nPointsVTail = Main.getTheAircraft().getVTail().getDiscretizedXle().size();

			IntStream.range(0, vTailRootXCoordinates.length)
			.forEach(i -> {
				seriesVTailRootAirfoilTopView.add(
						(vTailRootXCoordinates[i]*Main.getTheAircraft().getVTail().getPanels()
								.get(Main.getTheAircraft().getVTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue(),
						(vTailRootYCoordinates[i]*Main.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						);
			});

			IntStream.range(0, vTailTipXCoordinates.length)
			.forEach(i -> {
				seriesVTailTipAirfoilTopView.add(
						(vTailTipXCoordinates[i]*Main.getTheAircraft().getVTail().getPanels()
								.get(Main.getTheAircraft().getVTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getVTail().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getVTail().getDiscretizedXle().get(nPointsVTail-1).getEstimatedValue(),
						(vTailTipYCoordinates[i]*Main.getTheAircraft().getVTail().getPanels()
								.get(Main.getTheAircraft().getVTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardTopView = new XYSeries("Canard - Top View", false);
		
		if (Main.getTheAircraft().getCanard() != null) {
			Double[][] dataTopViewIsolatedCanard = Main.getTheAircraft().getCanard().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);

			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[i][1] + Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewIsolatedCanard[i][0] + Main.getTheAircraft().getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
			IntStream.range(0, dataTopViewIsolatedCanard.length)
			.forEach(i -> {
				seriesCanardTopView.add(
						dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][1] + Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
						- dataTopViewIsolatedCanard[dataTopViewIsolatedCanard.length-1-i][0] + Main.getTheAircraft().getCanard().getYApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesTopViewList = new ArrayList<>();

		if(Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));;

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

				XYSeries seriesNacelleCruvesTopView = new XYSeries("Nacelle " + i + " XZ Curve - Top View", false);
				IntStream.range(0, dataOutlineXZCurveNacelleX.size())
				.forEach(j -> {
					seriesNacelleCruvesTopView.add(
							dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
							dataOutlineXZCurveNacelleY.get(j).doubleValue(SI.METER)
							);
				});
				
				seriesNacelleCruvesTopViewList.add(seriesNacelleCruvesTopView);

			}
		}

		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerTopViewList = new ArrayList<>();
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesTopView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesTopView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							+ Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesTopView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER)
							- Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerTopViewList.add(seriesPropellerCruvesTopView);
				}
			}
		}
		
		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
			
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentZList = new HashMap<>();
		if (Main.getTheAircraft().getFuselage() != null) 
			componentZList.put(
					Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
					+ Main.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2).doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (Main.getTheAircraft().getWing() != null) 
			componentZList.put(
					Main.getTheAircraft().getWing().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingTopView, Color.decode("#87CEFA"))
					); 
		if (Main.getTheAircraft().getHTail() != null) 
			componentZList.put(
					Main.getTheAircraft().getHTail().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailTopView, Color.decode("#00008B"))
					);
		if (Main.getTheAircraft().getCanard() != null) 
			componentZList.put(
					Main.getTheAircraft().getCanard().getZApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardTopView, Color.decode("#228B22"))
					);
		if (Main.getTheAircraft().getVTail() != null)
			if (Main.getTheAircraft().getFuselage() != null)
				componentZList.put(
						Main.getTheAircraft().getFuselage().getZApexConstructionAxes().doubleValue(SI.METER)
						+ Main.getTheAircraft().getFuselage().getSectionCylinderHeight().divide(2).doubleValue(SI.METER)
						+ 0.0001,
						Tuple.of(seriesVTailRootAirfoilTopView, Color.decode("#FFD700"))
						);
		if (Main.getTheAircraft().getVTail() != null)
			componentZList.put(
					Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER) 
					+ Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER), 
					Tuple.of(seriesVTailTipAirfoilTopView, Color.decode("#FFD700"))
					);
		if (Main.getTheAircraft().getNacelles() != null) 
			seriesNacelleCruvesTopViewList.stream().forEach(
					nac -> componentZList.put(
							Main.getTheAircraft().getNacelles().getNacellesList().get(
									seriesNacelleCruvesTopViewList.indexOf(nac)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.005
							+ seriesNacelleCruvesTopViewList.indexOf(nac)*0.005, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (Main.getTheAircraft().getPowerPlant() != null) 
			seriesPropellerTopViewList.stream().forEach(
					prop -> componentZList.put(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(
									seriesPropellerTopViewList.indexOf(prop)
									).getZApexConstructionAxes().doubleValue(SI.METER)
							+ 0.0015
							+ seriesPropellerTopViewList.indexOf(prop)*0.001, 
							Tuple.of(prop, Color.BLACK)
							)
					);
		
		Map<Double, Tuple2<XYSeries, Color>> componentZListSorted = 
				componentZList.entrySet().stream()
			    .sorted(Entry.comparingByKey(Comparator.reverseOrder()))
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		componentZListSorted.values().stream().forEach(t -> dataset.addSeries(t._1()));
		
		List<Color> colorList = new ArrayList<>();
		componentZListSorted.values().stream().forEach(t -> colorList.add(t._2()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Top View", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					colorList.get(i)
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "AircraftTopView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getAircraftTopViewPane().getChildren().clear();
		theController.getAircraftTopViewPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createAircraftSideView() {
	
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		
		if (Main.getTheAircraft().getFuselage() != null) {
			// upper curve, sideview
			List<Amount<Length>> vX1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
			int nX1Upper = vX1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// lower curve, sideview
			List<Amount<Length>> vX2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
			int nX2Lower = vX2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nX1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nX2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingRootAirfoil = new XYSeries("Wing Root - Side View", false);
		XYSeries seriesWingTipAirfoil = new XYSeries("Wing Tip - Side View", false);

		if (Main.getTheAircraft().getWing() != null) {
			double[] wingRootXCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getXCoords();
			double[] wingRootZCoordinates = Main.getTheAircraft().getWing().getAirfoilList().get(0).getZCoords();
			double[] wingTipXCoordinates = Main.getTheAircraft().getWing().getAirfoilList()
					.get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getXCoords();
			double[] wingTipZCoordinates = Main.getTheAircraft().getWing().getAirfoilList()
					.get(Main.getTheAircraft().getWing().getAirfoilList().size()-1).getZCoords();
			int nPointsWing = Main.getTheAircraft().getWing().getDiscretizedXle().size();

			IntStream.range(0, wingRootXCoordinates.length)
			.forEach(i -> {
				seriesWingRootAirfoil.add(
						(wingRootXCoordinates[i]*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().getEstimatedValue()) 
						+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue(),
						(wingRootZCoordinates[i]*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, wingTipXCoordinates.length)
			.forEach(i -> {
				seriesWingTipAirfoil.add(
						(wingTipXCoordinates[i]*Main.getTheAircraft().getWing().getPanels()
								.get(Main.getTheAircraft().getWing().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getWing().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getWing().getDiscretizedXle().get(nPointsWing-1).getEstimatedValue(),
						(wingTipZCoordinates[i]*Main.getTheAircraft().getWing().getPanels()
								.get(Main.getTheAircraft().getWing().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getWing().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailRootAirfoil = new XYSeries("HTail Root - Side View", false);
		XYSeries seriesHTailTipAirfoil = new XYSeries("HTail Tip - Side View", false);

		if (Main.getTheAircraft().getHTail() != null) {
			double[] hTailRootXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getXCoords();
			double[] hTailRootZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList().get(0).getZCoords();
			double[] hTailTipXCoordinates = Main.getTheAircraft().getHTail().getAirfoilList()
					.get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getXCoords();
			double[] hTailTipZCoordinates = Main.getTheAircraft().getHTail().getAirfoilList()
					.get(Main.getTheAircraft().getHTail().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = Main.getTheAircraft().getHTail().getDiscretizedXle().size();

			IntStream.range(0, hTailRootXCoordinates.length)
			.forEach(i -> {
				seriesHTailRootAirfoil.add(
						(hTailRootXCoordinates[i]*Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue(),
						(hTailRootZCoordinates[i]*Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, hTailTipXCoordinates.length)
			.forEach(i -> {
				seriesHTailTipAirfoil.add(
						(hTailTipXCoordinates[i]*Main.getTheAircraft().getHTail().getPanels()
								.get(Main.getTheAircraft().getHTail().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getHTail().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getHTail().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(hTailTipZCoordinates[i]*Main.getTheAircraft().getHTail().getPanels()
								.get(Main.getTheAircraft().getHTail().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getHTail().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardRootAirfoil = new XYSeries("Canard Root - Side View", false);
		XYSeries seriesCanardTipAirfoil = new XYSeries("Canard Tip - Side View", false);

		if (Main.getTheAircraft().getCanard() != null) {
			double[] canardRootXCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(0).getXCoords();
			double[] canardRootZCoordinates = Main.getTheAircraft().getCanard().getAirfoilList().get(0).getZCoords();
			double[] canardTipXCoordinates = Main.getTheAircraft().getCanard().getAirfoilList()
					.get(Main.getTheAircraft().getCanard().getAirfoilList().size()-1).getXCoords();
			double[] canardTipZCoordinates = Main.getTheAircraft().getCanard().getAirfoilList()
					.get(Main.getTheAircraft().getCanard().getAirfoilList().size()-1).getZCoords();
			int nPointsHTail = Main.getTheAircraft().getCanard().getDiscretizedXle().size();

			IntStream.range(0, canardRootXCoordinates.length)
			.forEach(i -> {
				seriesCanardRootAirfoil.add(
						(canardRootXCoordinates[i]*Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getXApexConstructionAxes().getEstimatedValue(),
						(canardRootZCoordinates[i]*Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});

			IntStream.range(0, canardTipXCoordinates.length)
			.forEach(i -> {
				seriesCanardTipAirfoil.add(
						(canardTipXCoordinates[i]*Main.getTheAircraft().getCanard().getPanels()
								.get(Main.getTheAircraft().getCanard().getPanels().size()-1).getChordTip().getEstimatedValue()) 
						+ Main.getTheAircraft().getCanard().getXApexConstructionAxes().getEstimatedValue()
						+ Main.getTheAircraft().getCanard().getDiscretizedXle().get(nPointsHTail-1).getEstimatedValue(),
						(canardTipZCoordinates[i]*Main.getTheAircraft().getCanard().getPanels()
								.get(Main.getTheAircraft().getCanard().getPanels().size()-1).getChordTip().getEstimatedValue())
						+ Main.getTheAircraft().getCanard().getZApexConstructionAxes().getEstimatedValue()
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailSideView = new XYSeries("VTail - Side View", false);

		if (Main.getTheAircraft().getVTail() != null) {
			Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);

			IntStream.range(0, dataTopViewVTail.length)
			.forEach(i -> {
				seriesVTailSideView.add(
						dataTopViewVTail[i][0] + Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
						dataTopViewVTail[i][1] + Main.getTheAircraft().getVTail().getZApexConstructionAxes().doubleValue(SI.METER)
						);
			});
		}

		//--------------------------------------------------
		// get data vectors from nacelle discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesSideViewList = new ArrayList<>();

		if (Main.getTheAircraft().getNacelles() != null) {
			for(int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				// upper curve, sideview
				List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
				int nacelleCurveXPoints = nacelleCurveX.size();
				List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

				// lower curve, sideview
				List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
				Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

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


				XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
				IntStream.range(0, dataOutlineXZCurveNacelleX.size())
				.forEach(j -> {
					seriesNacelleCruvesSideView.add(
							dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER),
							dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER)
							);
				});

				seriesNacelleCruvesSideViewList.add(seriesNacelleCruvesSideView);

			}
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerSideViewList = new ArrayList<>();
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					XYSeries seriesPropellerCruvesSideView = new XYSeries("Propeller " + i, false);
					seriesPropellerCruvesSideView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							+ Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);
					seriesPropellerCruvesSideView.add(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getXApexConstructionAxes().doubleValue(SI.METER),
							1.0015*Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
							- Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2).doubleValue(SI.METER)
							);

					seriesPropellerSideViewList.add(seriesPropellerCruvesSideView);
				}
			}
		}		
		//--------------------------------------------------
		// get data vectors from landing gears 
		//--------------------------------------------------
		XYSeries seriesLandingGearSideView = new XYSeries("Landing Gears - Side View", false);
		
		if (Main.getTheAircraft().getLandingGears() != null) {
			Amount<Length> radius = Main.getTheAircraft().getLandingGears().getRearWheelsHeight().divide(2);
			Double[] wheelCenterPosition = new Double[] {
					Main.getTheAircraft().getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER),
					Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
					- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
					- radius.doubleValue(SI.METER)
			};
			Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);

			IntStream.range(0, thetaArray.length)
			.forEach(i -> {
				seriesLandingGearSideView.add(
						radius.doubleValue(SI.METER)*Math.cos(thetaArray[i]) + wheelCenterPosition[0],
						radius.doubleValue(SI.METER)*Math.sin(thetaArray[i]) + wheelCenterPosition[1]
						);
			});
		}
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		if (Main.getTheAircraft().getPowerPlant() != null)
			seriesPropellerSideViewList.stream().forEach(
					prop -> seriesAndColorList.add(Tuple.of(prop, Color.BLACK))
					);
		if (Main.getTheAircraft().getNacelles() != null)
			seriesNacelleCruvesSideViewList.stream().forEach(
					nac -> seriesAndColorList.add(Tuple.of(nac, Color.decode("#FF7F50")))
					);
		if (Main.getTheAircraft().getWing() != null) {
			seriesAndColorList.add(Tuple.of(seriesWingRootAirfoil, Color.decode("#87CEFA")));
			seriesAndColorList.add(Tuple.of(seriesWingTipAirfoil, Color.decode("#87CEFA")));
		}
		if (Main.getTheAircraft().getHTail() != null) {
			seriesAndColorList.add(Tuple.of(seriesHTailRootAirfoil, Color.decode("#00008B")));
			seriesAndColorList.add(Tuple.of(seriesHTailTipAirfoil, Color.decode("#00008B")));
		}
		if (Main.getTheAircraft().getCanard() != null) {
			seriesAndColorList.add(Tuple.of(seriesCanardRootAirfoil, Color.decode("#228B22")));
			seriesAndColorList.add(Tuple.of(seriesCanardTipAirfoil, Color.decode("#228B22")));
		}
		if (Main.getTheAircraft().getLandingGears() != null) 
			seriesAndColorList.add(Tuple.of(seriesLandingGearSideView, Color.decode("#404040")));
		if (Main.getTheAircraft().getFuselage() != null)
			seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		if (Main.getTheAircraft().getVTail() != null)
			seriesAndColorList.add(Tuple.of(seriesVTailSideView, Color.decode("#FFD700")));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Side View", 
				"x (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMinSideView, xMaxSideView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapePropRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapePropRenderer.setDefaultShapesVisible(false);
		xyLineAndShapePropRenderer.setDefaultLinesVisible(true);
		xyLineAndShapePropRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i < Main.getTheAircraft().getPowerPlant().getEngineNumber()) {
				xyLineAndShapePropRenderer.setSeriesVisible(i, true);
				xyLineAndShapePropRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapePropRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
			else
				xyLineAndShapePropRenderer.setSeriesVisible(i, false);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}

		if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
				|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
			plot.setRenderer(2, xyLineAndShapeRenderer);
			plot.setDataset(2, dataset);
			plot.setRenderer(1, xyAreaRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyLineAndShapePropRenderer);
			plot.setDataset(0, dataset);
		}
		else {
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);
			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
		}

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "AircraftSideView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getAircraftSideViewPane().getChildren().clear();
		theController.getAircraftSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public void createAircraftFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		
		if (Main.getTheAircraft().getFuselage() != null) {
			// section upper curve
			List<Amount<Length>> vY1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
			int nY1Upper = vY1Upper.size();
			List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

			// section lower curve
			List<Amount<Length>> vY2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
			int nY2Lower = vY2Lower.size();
			List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
			Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

			IntStream.range(0, nY1Upper)
			.forEach(i -> {
				seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
			});
			IntStream.range(0, nY2Lower)
			.forEach(i -> {
				seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
			});
		}
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		XYSeries seriesWingFrontView = new XYSeries("Wing - Front View", false);

		if (Main.getTheAircraft().getWing() != null) {
			List<Amount<Length>> wingBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getWing().getYBreakPoints().stream().forEach(y -> wingBreakPointsYCoordinates.add(y));
			int nYPointsWingTemp = wingBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsWingTemp; i++)
				wingBreakPointsYCoordinates.add(Main.getTheAircraft().getWing().getYBreakPoints().get(nYPointsWingTemp-i-1));
			int nYPointsWing = wingBreakPointsYCoordinates.size();

			List<Amount<Length>> wingThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getWing().getAirfoilList().size(); i++)
				wingThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getWing().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getWing().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsWingTemp; i++) {
				wingThicknessZCoordinates.add(
						Amount.valueOf(
								(Main.getTheAircraft().getWing().getChordsBreakPoints().get(nYPointsWingTemp-i-1).doubleValue(SI.METER)*
										MyArrayUtils.getMin(Main.getTheAircraft().getWing().getAirfoilList().get(nYPointsWingTemp-i-1).getZCoords())),
								SI.METER
								)
						);
			}

			List<Amount<Angle>> dihedralList = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getWing().getDihedralsBreakPoints().size(); i++) {
				dihedralList.add(
						Main.getTheAircraft().getWing().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getWing().getDihedralsBreakPoints().size(); i++) {
				dihedralList.add(
						Main.getTheAircraft().getWing().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getWing().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsWing)
			.forEach(i -> {
				seriesWingFrontView.add(
						wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
						wingThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralList.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsWing)
			.forEach(i -> {
				seriesWingFrontView.add(
						-wingBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getWing().getYApexConstructionAxes()).doubleValue(SI.METRE),
						wingThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getWing().getZApexConstructionAxes())
						.plus(wingBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralList.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from hTail discretization
		//--------------------------------------------------
		XYSeries seriesHTailFrontView = new XYSeries("HTail - Front View", false);

		if (Main.getTheAircraft().getHTail() != null) {
			List<Amount<Length>> hTailBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getHTail().getYBreakPoints().stream().forEach(y -> hTailBreakPointsYCoordinates.add(y));
			int nYPointsHTailTemp = hTailBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsHTailTemp; i++)
				hTailBreakPointsYCoordinates.add(Main.getTheAircraft().getHTail().getYBreakPoints().get(nYPointsHTailTemp-i-1));
			int nYPointsHTail = hTailBreakPointsYCoordinates.size();

			List<Amount<Length>> hTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getHTail().getAirfoilList().size(); i++)
				hTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getHTail().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getHTail().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsHTailTemp; i++)
				hTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getHTail().getChordsBreakPoints().get(nYPointsHTailTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getHTail().getAirfoilList().get(nYPointsHTailTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListHTail = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getHTail().getDihedralsBreakPoints().size(); i++) {
				dihedralListHTail.add(
						Main.getTheAircraft().getHTail().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getHTail().getDihedralsBreakPoints().size(); i++) {
				dihedralListHTail.add(
						Main.getTheAircraft().getHTail().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getHTail().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsHTail)
			.forEach(i -> {
				seriesHTailFrontView.add(
						hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						hTailThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
						.plus(hTailBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListHTail.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsHTail)
			.forEach(i -> {
				seriesHTailFrontView.add(
						-hTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getHTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						hTailThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getHTail().getZApexConstructionAxes())
						.plus(hTailBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListHTail.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from canard discretization
		//--------------------------------------------------
		XYSeries seriesCanardFrontView = new XYSeries("Canard - Front View", false);

		if (Main.getTheAircraft().getCanard() != null) {
			List<Amount<Length>> canardBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getCanard().getYBreakPoints().stream().forEach(y -> canardBreakPointsYCoordinates.add(y));
			int nYPointsCanardTemp = canardBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardBreakPointsYCoordinates.add(Main.getTheAircraft().getCanard().getYBreakPoints().get(nYPointsCanardTemp-i-1));
			int nYPointsCanard = canardBreakPointsYCoordinates.size();

			List<Amount<Length>> canardThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getCanard().getAirfoilList().size(); i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getCanard().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getCanard().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsCanardTemp; i++)
				canardThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getCanard().getChordsBreakPoints().get(nYPointsCanardTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getCanard().getAirfoilList().get(nYPointsCanardTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			List<Amount<Angle>> dihedralListCanard = new ArrayList<>();
			for (int i = 0; i < Main.getTheAircraft().getCanard().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						Main.getTheAircraft().getCanard().getDihedralsBreakPoints().get(i)
						);
			}
			for (int i = 0; i < Main.getTheAircraft().getCanard().getDihedralsBreakPoints().size(); i++) {
				dihedralListCanard.add(
						Main.getTheAircraft().getCanard().getDihedralsBreakPoints().get(
								Main.getTheAircraft().getCanard().getDihedralsBreakPoints().size()-1-i)
						);
			}

			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						canardBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getCanard().getZApexConstructionAxes())
						.plus(canardBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListCanard.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
			IntStream.range(0, nYPointsCanard)
			.forEach(i -> {
				seriesCanardFrontView.add(
						-canardBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getCanard().getYApexConstructionAxes()).doubleValue(SI.METRE),
						canardThicknessZCoordinates.get(i)
						.plus(Main.getTheAircraft().getCanard().getZApexConstructionAxes())
						.plus(canardBreakPointsYCoordinates.get(i)
								.times(Math.sin(dihedralListCanard.get(i)
										.doubleValue(SI.RADIAN))
										)
								)
						.doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from vTail discretization
		//--------------------------------------------------
		XYSeries seriesVTailFrontView = new XYSeries("VTail - Front View", false);

		if (Main.getTheAircraft().getVTail() != null) {
			List<Amount<Length>> vTailBreakPointsYCoordinates = new ArrayList<>(); 
			Main.getTheAircraft().getVTail().getYBreakPoints().stream().forEach(y -> vTailBreakPointsYCoordinates.add(y));
			int nYPointsVTailTemp = vTailBreakPointsYCoordinates.size();
			for(int i=0; i<nYPointsVTailTemp; i++)
				vTailBreakPointsYCoordinates.add(Main.getTheAircraft().getVTail().getYBreakPoints().get(nYPointsVTailTemp-i-1));
			int nYPointsVTail = vTailBreakPointsYCoordinates.size();

			List<Amount<Length>> vTailThicknessZCoordinates = new ArrayList<Amount<Length>>();
			for(int i=0; i<Main.getTheAircraft().getVTail().getAirfoilList().size(); i++)
				vTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getVTail().getChordsBreakPoints().get(i).doubleValue(SI.METER)*
								MyArrayUtils.getMax(Main.getTheAircraft().getVTail().getAirfoilList().get(i).getZCoords()),
								SI.METER
								)
						);
			for(int i=0; i<nYPointsVTailTemp; i++)
				vTailThicknessZCoordinates.add(
						Amount.valueOf(
								Main.getTheAircraft().getVTail().getChordsBreakPoints().get(nYPointsVTailTemp-i-1).doubleValue(SI.METER)*
								MyArrayUtils.getMin(Main.getTheAircraft().getVTail().getAirfoilList().get(nYPointsVTailTemp-i-1).getZCoords()),
								SI.METER
								)
						);

			IntStream.range(0, nYPointsVTail)
			.forEach(i -> {
				seriesVTailFrontView.add(
						vTailThicknessZCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getYApexConstructionAxes()).doubleValue(SI.METRE),
						vTailBreakPointsYCoordinates.get(i).plus(Main.getTheAircraft().getVTail().getZApexConstructionAxes()).doubleValue(SI.METRE)
						);
			});
		}
		//--------------------------------------------------
		// get data vectors from nacelles discretization
		//--------------------------------------------------
		List<XYSeries> seriesNacelleCruvesFrontViewList = new ArrayList<>();

		if (Main.getTheAircraft().getNacelles() != null) {
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

				XYSeries seriesNacelleCruvesFrontView = new XYSeries("Nacelle " + i + " - Front View", false);
				IntStream.range(0, yCoordinate.length)
				.forEach(j -> {
					seriesNacelleCruvesFrontView.add(
							yCoordinate[j] + y0,
							zCoordinate[j] + z0
							);
				});

				seriesNacelleCruvesFrontViewList.add(seriesNacelleCruvesFrontView);
			}
		}
		//-------------------------------------------------------------------------------
		// get data vectors from power plant propellers (only for PISTON and TURBOPROP)
		//-------------------------------------------------------------------------------
		List<XYSeries> seriesPropellerFrontViewList = new ArrayList<>();
		
		if(Main.getTheAircraft().getPowerPlant() != null) {
			for(int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				if (Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.PISTON)
						|| Main.getTheAircraft().getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {
					
					Amount<Length> radius = Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getPropellerDiameter().divide(2);
					Double[] centerPosition = new Double[] {
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getYApexConstructionAxes().doubleValue(SI.METER),
							Main.getTheAircraft().getPowerPlant().getEngineList().get(i).getZApexConstructionAxes().doubleValue(SI.METER)
					};
					Double[] thetaArray = MyArrayUtils.linspaceDouble(0, 2*Math.PI, 360);
					
					XYSeries seriesPropellerCruvesFrontView = new XYSeries("Propeller " + i, false);
					IntStream.range(0, thetaArray.length)
					.forEach(j -> {
						seriesPropellerCruvesFrontView.add(
								radius.doubleValue(SI.METER)*Math.cos(thetaArray[j]) + centerPosition[0],
								radius.doubleValue(SI.METER)*Math.sin(thetaArray[j]) + centerPosition[1]
								);
					});

					seriesPropellerFrontViewList.add(seriesPropellerCruvesFrontView);
					
				}
			}
		}
		//--------------------------------------------------
		// get data vectors from landing gears
		//--------------------------------------------------
		List<XYSeries> serieLandingGearsCruvesFrontViewList = new ArrayList<>();

		if (Main.getTheAircraft().getLandingGears() != null) {
			for(int i=0; i<Main.getTheAircraft().getLandingGears().getNumberOfRearWheels()/2; i++) {

				XYSeries seriesLeftLandingGearCruvesFrontView = new XYSeries("Left Landing Gear " + i + " - Front View", false);
				seriesLeftLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesLeftLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesLeftLandingGearCruvesFrontView.add(
						Main.getTheAircraft().getLandingGears()
						.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
						+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				XYSeries seriesRightLandingGearCruvesFrontView = new XYSeries("Right Landing Gear " + i + " - Front View", false);
				seriesRightLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);
				seriesRightLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getRearWheelsHeight().doubleValue(SI.METER)
						);
				seriesRightLandingGearCruvesFrontView.add(
						- (Main.getTheAircraft().getLandingGears()
								.getDistanceBetweenWheels().divide(2).doubleValue(SI.METER)
								+ (i*1.1*Main.getTheAircraft().getLandingGears().getRearWheelsWidth().doubleValue(SI.METER))),
						Main.getTheAircraft().getLandingGears().getZApexConstructionAxesMainGear().doubleValue(SI.METER)
						- Main.getTheAircraft().getLandingGears().getMainLegsLenght().doubleValue(SI.METER)
						);

				serieLandingGearsCruvesFrontViewList.add(seriesLeftLandingGearCruvesFrontView);
				serieLandingGearsCruvesFrontViewList.add(seriesRightLandingGearCruvesFrontView);
			}
		}
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		Map<Double, Tuple2<XYSeries, Color>> componentXList = new HashMap<>();
		if (Main.getTheAircraft().getFuselage() != null) 
			componentXList.put(
					Main.getTheAircraft().getFuselage().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesFuselageCurve, Color.WHITE) 
					);
		if (Main.getTheAircraft().getWing() != null) 
			componentXList.put(
					Main.getTheAircraft().getWing().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesWingFrontView, Color.decode("#87CEFA"))
					); 
		if (Main.getTheAircraft().getHTail() != null) 
			componentXList.put(
					Main.getTheAircraft().getHTail().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesHTailFrontView, Color.decode("#00008B"))
					);
		if (Main.getTheAircraft().getCanard() != null) 
			componentXList.put(
					Main.getTheAircraft().getCanard().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesCanardFrontView, Color.decode("#228B22"))
					);
		if (Main.getTheAircraft().getVTail() != null)
			componentXList.put(
					Main.getTheAircraft().getVTail().getXApexConstructionAxes().doubleValue(SI.METER),
					Tuple.of(seriesVTailFrontView, Color.decode("#FFD700"))
					);
		if (Main.getTheAircraft().getNacelles() != null) 
			seriesNacelleCruvesFrontViewList.stream().forEach(
					nac -> componentXList.put(
							Main.getTheAircraft().getNacelles().getNacellesList().get(
									seriesNacelleCruvesFrontViewList.indexOf(nac)
									).getXApexConstructionAxes().doubleValue(SI.METER)
							+ 0.005
							+ seriesNacelleCruvesFrontViewList.indexOf(nac)*0.005, 
							Tuple.of(nac, Color.decode("#FF7F50"))
							)
					);
		if (Main.getTheAircraft().getPowerPlant() != null) 
			seriesPropellerFrontViewList.stream().forEach(
					prop -> componentXList.put(
							Main.getTheAircraft().getPowerPlant().getEngineList().get(
									seriesPropellerFrontViewList.indexOf(prop)
									).getXApexConstructionAxes().doubleValue(SI.METER)
							+ 0.0015
							+ seriesPropellerFrontViewList.indexOf(prop)*0.001, 
							Tuple.of(prop, Color.BLACK)
							)
					);
		if (Main.getTheAircraft().getLandingGears() != null) 
			serieLandingGearsCruvesFrontViewList.stream().forEach(
					lg -> componentXList.put(
							Main.getTheAircraft().getLandingGears().getXApexConstructionAxesMainGear().doubleValue(SI.METER)
							+ serieLandingGearsCruvesFrontViewList.indexOf(lg)*0.001, 
							Tuple.of(lg, Color.decode("#404040"))
							)
					);
		
		Map<Double, Tuple2<XYSeries, Color>> componentXListSorted = 
				componentXList.entrySet().stream()
			    .sorted(Entry.comparingByKey(Comparator.naturalOrder()))
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		componentXListSorted.values().stream().forEach(t -> dataset.addSeries(t._1()));
		
		List<Color> colorList = new ArrayList<>();
		componentXListSorted.values().stream().forEach(t -> colorList.add(t._2()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Aircraft data representation - Front View", 
				"y (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinFrontView, yMaxFrontView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					colorList.get(i)
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "AircraftFrontView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getAircraftFrontViewPane().getChildren().clear();
		theController.getAircraftFrontViewPane().getChildren().add(sceneFrontView.getRoot());
		
	}
	
	public void logAircraftFromFileToInterface() {

		String dirLiftingSurfaces = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "lifting_surfaces";
		String dirFuselages = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "fuselages";
		String dirEngines = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "engines";
		String dirNacelles = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "nacelles";
		String dirLandingGears = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "landing_gears";
		String dirCabinConfiguration = Main.getInputDirectoryPath() + File.separator + "Template_Aircraft" + File.separator + "cabin_configurations";
		String pathToXML = theController.getTextFieldAircraftInputFile().getText();

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaAircraftConsoleOutput().setText(
				Main.getTheAircraft().toString()
				);

		// get the text field for aircraft input data
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		//---------------------------------------------------------------------------------
		// AIRCRAFT TYPE:
		String aircraftTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		
		if(aircraftTypeFileName != null) { 
			if(theController.getAircraftTypeChoiceBox() != null) {
				if(aircraftTypeFileName.equalsIgnoreCase("JET"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(0);
				else if(aircraftTypeFileName.equalsIgnoreCase("FIGHTER"))		
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(1);
				else if(aircraftTypeFileName.equalsIgnoreCase("BUSINESS_JET"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(2);
				else if(aircraftTypeFileName.equalsIgnoreCase("TURBOPROP"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(3);
				else if(aircraftTypeFileName.equalsIgnoreCase("GENERAL_AVIATION"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(4);
				else if(aircraftTypeFileName.equalsIgnoreCase("COMMUTER"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(5);
				else if(aircraftTypeFileName.equalsIgnoreCase("ACROBATIC"))
					theController.getAircraftTypeChoiceBox().getSelectionModel().select(6);
			}
		}
		
		//---------------------------------------------------------------------------------
		// REGULATIONS TYPE:
		String regulationsTypeFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@regulations");
		
		if(regulationsTypeFileName != null) { 
			if(theController.getRegulationsTypeChoiceBox() != null) {
				if(regulationsTypeFileName.equalsIgnoreCase("FAR_23"))
					theController.getRegulationsTypeChoiceBox().getSelectionModel().select(0);
				else if(regulationsTypeFileName.equalsIgnoreCase("FAR_25"))		
					theController.getRegulationsTypeChoiceBox().getSelectionModel().select(1);
			}
		}
		
		//---------------------------------------------------------------------------------
		// CABIN CONFIGURATION:
		String cabinConfigrationFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//global_data/cabin_configuration/@file");

		if(theController.getTextFieldAircraftCabinConfigurationFile() != null) 
			theController.getTextFieldAircraftCabinConfigurationFile().setText(
					dirCabinConfiguration 
					+ File.separator
					+ cabinConfigrationFileName
					);
		else
			theController.getTextFieldAircraftCabinConfigurationFile().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FUSELAGE:
		String fuselageFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//fuselages/fuselage/@file");
		if(fuselageFileName != null) 
			theController.getTextFieldAircraftFuselageFile().setText(
					dirFuselages 
					+ File.separator
					+ fuselageFileName
					);
		else
			theController.getTextFieldAircraftFuselageFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			theController.getTextFieldAircraftFuselageX().setText(
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
				theController.getFuselageXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageXUnitChoiceBox().getSelectionModel().select(1);

		}

		else {
			theController.getTextFieldAircraftFuselageX().setText("0.0");
			theController.getFuselageXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			theController.getTextFieldAircraftFuselageY().setText(
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
				theController.getFuselageYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftFuselageY().setText("0.0");
			theController.getFuselageYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getFuselage() != null) {
			theController.getTextFieldAircraftFuselageZ().setText(
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
				theController.getFuselageZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftFuselageZ().setText("0.0");
			theController.getFuselageZUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// WING:
		String wingFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/wing/@file");
		if(wingFileName != null)
			theController.getTextFieldAircraftWingFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ wingFileName
					);
		else 
			theController.getTextFieldAircraftWingFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingX().setText(
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
				theController.getWingXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getWingXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingX().setText("0.0");
			theController.getWingXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingY().setText(
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
				theController.getWingYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getWingYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingY().setText("0.0");
			theController.getWingYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingZ().setText(
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
				theController.getWingZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getWingZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingZ().setText("0.0");
			theController.getWingZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getWing() != null) {

			theController.getTextFieldAircraftWingRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getWing()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getWing()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftWingRiggingAngle().setText("0.0");
			theController.getWingRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// HORIZONTAL TAIL:
		String hTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/horizontal_tail/@file");
		if(hTailFileName != null)
			theController.getTextFieldAircraftHTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ hTailFileName
					);
		else
			theController.getTextFieldAircraftHTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailX().setText(
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
				theController.gethTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.gethTailXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailX().setText("0.0");
			theController.gethTailXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailY().setText(
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
				theController.gethTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.gethTailYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailY().setText("0.0");
			theController.gethTailYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailZ().setText(
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
				theController.getHtailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getHtailZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailZ().setText("0.0");
			theController.getHtailZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getHTail() != null) {

			theController.getTextFieldAircraftHTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getHTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getHTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftHTailRiggingAngle().setText("0.0");
			theController.gethTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// VERTICAL TAIL:
		String vTailFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/vertical_tail/@file");
		if(vTailFileName != null)
			theController.getTextFieldAircraftVTailFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ vTailFileName
					);
		else
			theController.getTextFieldAircraftVTailFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailX().setText(
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
				theController.getvTailXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getvTailXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailX().setText("0.0");
			theController.getvTailXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailY().setText(
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
				theController.getvTailYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getvTailYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailY().setText("0.0");
			theController.getvTailYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailZ().setText(
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
				theController.getvTailZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getvTailZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailZ().setText("0.0");
			theController.getvTailZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getVTail() != null) {

			theController.getTextFieldAircraftVTailRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getVTail()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getVTail()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftVTailRiggingAngle().setText("0.0");
			theController.getvTailRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		//---------------------------------------------------------------------------------
		// CANARD:
		String canardFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//lifting_surfaces/canard/@file");
		if(canardFileName != null)
			theController.getTextFieldAircraftCanardFile().setText(
					dirLiftingSurfaces 
					+ File.separator
					+ canardFileName
					);
		else
			theController.getTextFieldAircraftCanardFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardX().setText(
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
				theController.getCanardXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getCanardXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardX().setText("0.0");
			theController.getCanardXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardY().setText(
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
				theController.getCanardYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getCanardYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardY().setText("0.0");
			theController.getCanardYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardZ().setText(
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
				theController.getCanardZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getCanardZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardZ().setText("0.0");
			theController.getCanardZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getCanard() != null) {

			theController.getTextFieldAircraftCanardRiggingAngle().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getCanard()
							.getRiggingAngle()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("°")
					|| Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("deg"))
				theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getCanard()
					.getRiggingAngle().getUnit().toString().equalsIgnoreCase("rad"))
				theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftCanardRiggingAngle().setText("0.0");
			theController.getCanardRiggingAngleUnitChoiceBox().getSelectionModel().select(0);
		}
		
		//---------------------------------------------------------------------------------
		// ENGINES NUMBER CHECK:
		if (Main.getTheAircraft().getPowerPlant().getEngineList().size() >= 
				theController.getTabPaneAircraftEngines().getTabs().size()) {
			
			int iStart = theController.getTabPaneAircraftEngines().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
				theController.getInputManagerControllerUtilities().addAircraftEngineImplementation();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER ENGINES:
		//---------------------------------------------------------------------------------
		NodeList nodelistEngines = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//power_plant/engine");
		if(nodelistEngines != null) {
			for (int i = 0; i < nodelistEngines.getLength(); i++) {
				//..........................................................................................................
				Node nodeEngine  = nodelistEngines.item(i); 
				Element elementEngine = (Element) nodeEngine;
				if(elementEngine.getAttribute("file") != null)
					theController.getTextFieldsAircraftEngineFileList().get(i).setText(
							dirEngines 
							+ File.separator
							+ elementEngine.getAttribute("file")	
							);
				else
					theController.getTextFieldsAircraftEngineFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftEngineXUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftEngineXUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineXList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineXUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftEngineYUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftEngineYUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineYList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineYUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftEngineZUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftEngineZUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineZList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineZUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null)

					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("BURIED")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("AFT_FUSELAGE")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(3);
					else if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("REAR_FUSELAGE")
							)
						theController.getChoiceBoxesAircraftEnginePositonList().get(i).getSelectionModel().select(4);

				//..........................................................................................................
				if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i) != null) {
					theController.getTextFieldAircraftEngineTiltList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
									.getTiltingAngle()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("°")
							|| Main.getTheAircraft().getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxAircraftEngineTiltUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getPowerPlant().getEngineList().get(i)
							.getTiltingAngle().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxAircraftEngineTiltUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftEngineTiltList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftEngineTiltUnitList().get(i).getSelectionModel().select(0);
				}
			}
		}

		//---------------------------------------------------------------------------------
		// NACELLE NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneAircraftNacelles().getTabs().size()) {
			
			int iStart = theController.getTabPaneAircraftNacelles().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++)
				theController.getInputManagerControllerUtilities().addAircraftNacelleImplementation();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER ENGINES:
		//---------------------------------------------------------------------------------
		NodeList nodelistNacelles = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//nacelles/nacelle");
		if(nodelistNacelles != null) {
			for (int i = 0; i < nodelistNacelles.getLength(); i++) {
				//..........................................................................................................
				Node nodeNacelle  = nodelistNacelles.item(i); 
				Element elementNacelle = (Element) nodeNacelle;
				if(elementNacelle.getAttribute("file") != null)
					theController.getTextFieldsAircraftNacelleFileList().get(i).setText(
							dirNacelles
							+ File.separator
							+ elementNacelle.getAttribute("file")	
							);
				else
					theController.getTextFieldsAircraftNacelleFileList().get(i).setText(
							"NOT INITIALIZED"
							);
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					theController.getTextFieldAircraftNacelleXList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getXApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftNacelleXUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftNacelleXUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftNacelleXList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftNacelleXUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					theController.getTextFieldAircraftNacelleYList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getYApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftNacelleYUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftNacelleYUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftNacelleYList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftNacelleYUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null) {
					theController.getTextFieldAircraftNacelleZList().get(i).setText(
							String.valueOf(
									Main.getTheAircraft().getNacelles().getNacellesList().get(i)
									.getZApexConstructionAxes()
									.getEstimatedValue()
									)
							);
					if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxAircraftNacelleZUnitList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft()
							.getNacelles().getNacellesList().get(i)
							.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxAircraftNacelleZUnitList().get(i).getSelectionModel().select(1);
				}
				else {
					theController.getTextFieldAircraftNacelleZList().get(i).setText("0.0");
					theController.getChoiceBoxAircraftNacelleZUnitList().get(i).getSelectionModel().select(0);
				}
				//..........................................................................................................
				if(Main.getTheAircraft().getNacelles().getNacellesList().get(i) != null)

					if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("WING")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(0);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("FUSELAGE")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(1);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("HTAIL")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(2);
					else if(Main.getTheAircraft().getNacelles().getNacellesList().get(i)
							.getMountingPosition().toString()
							.equalsIgnoreCase("UNDERCARRIAGE_HOUSING")
							)
						theController.getChoiceBoxesAircraftNacellePositonList().get(i).getSelectionModel().select(3);

			}
		}

		//---------------------------------------------------------------------------------
		// LANDING GEARS:
		String landingGearsFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//landing_gears/@file");
		if(landingGearsFileName != null) 
			theController.getTextFieldAircraftLandingGearsFile().setText(
					dirLandingGears 
					+ File.separator
					+ landingGearsFileName
					);
		else
			theController.getTextFieldAircraftLandingGearsFile().setText(
					"NOT INITIALIZED"
					);
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			theController.getTextFieldAircraftLandingGearsX().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getXApexConstructionAxesMainGear()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getXApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsXUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftLandingGearsX().setText("0.0");
			theController.getLandingGearsXUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			theController.getTextFieldAircraftLandingGearsY().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getYApexConstructionAxesMainGear()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getYApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsYUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftLandingGearsY().setText("0.0");
			theController.getLandingGearsYUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null) {

			theController.getTextFieldAircraftLandingGearsZ().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getZApexConstructionAxesMainGear()
							.getEstimatedValue()
							)
					);

			if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getLandingGears()
					.getZApexConstructionAxesMainGear().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsZUnitChoiceBox().getSelectionModel().select(1);

		}
		else {
			theController.getTextFieldAircraftLandingGearsZ().setText("0.0");
			theController.getLandingGearsZUnitChoiceBox().getSelectionModel().select(0);
		}
		//.................................................................................
		if(Main.getTheAircraft().getLandingGears() != null)

			if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("FUSELAGE")
					)
				theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("WING")
					)
				theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().select(1);
			else if(Main.getTheAircraft().getLandingGears()
					.getMountingPosition().toString()
					.equalsIgnoreCase("NACELLE")
					)
				theController.getLandingGearsMountingPositionTypeChoiceBox().getSelectionModel().select(2);

		//---------------------------------------------------------------------------------
		// SYSTEMS:
//		String systemsFileName =
//				MyXMLReaderUtils
//				.getXMLPropertyByPath(
//						reader.getXmlDoc(), reader.getXpath(),
//						"//systems/@file");
//		if(systemsFileName != null) 
//			theController.getTextFieldAircraftSystemsFile().setText(
//					dirSystems 
//					+ File.separator
//					+ systemsFileName
//					);
//		else
//			theController.getTextFieldAircraftSystemsFile().setText(
//					"NOT INITIALIZED"
//					);
//		//.................................................................................
//		if(Main.getTheAircraft().getSystems() != null) {
//
//			theController.getTextFieldAircraftSystemsX().setText(
//					String.valueOf(
//							Main.getTheAircraft()
//							.getSystems()
//							.getXApexConstructionAxes()
//							.getEstimatedValue()
//							)
//					);
//
//			if(Main.getTheAircraft()
//					.getSystems()
//					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
//				theController.getSystemsXUnitChoiceBox().getSelectionModel().select(0);
//			else if(Main.getTheAircraft()
//					.getSystems()
//					.getXApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
//				theController.getSystemsXUnitChoiceBox().getSelectionModel().select(1);
//
//		}
//		else {
//			theController.getTextFieldAircraftSystemsX().setText("0.0");
//			theController.getSystemsXUnitChoiceBox().getSelectionModel().select(0);
//		}
//		//.................................................................................
//		if(Main.getTheAircraft().getSystems() != null) {
//
//			theController.getTextFieldAircraftSystemsY().setText(
//					String.valueOf(
//							Main.getTheAircraft()
//							.getSystems()
//							.getYApexConstructionAxes()
//							.getEstimatedValue()
//							)
//					);
//
//			if(Main.getTheAircraft()
//					.getSystems()
//					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
//				theController.getSystemsYUnitChoiceBox().getSelectionModel().select(0);
//			else if(Main.getTheAircraft()
//					.getSystems()
//					.getYApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
//				theController.getSystemsYUnitChoiceBox().getSelectionModel().select(1);
//
//		}
//		else {
//			theController.getTextFieldAircraftSystemsY().setText("0.0");
//			theController.getSystemsYUnitChoiceBox().getSelectionModel().select(0);
//		}
//		//.................................................................................
//		if(Main.getTheAircraft().getSystems() != null) {
//
//			theController.getTextFieldAircraftSystemsZ().setText(
//					String.valueOf(
//							Main.getTheAircraft()
//							.getSystems()
//							.getZApexConstructionAxes()
//							.getEstimatedValue()
//							)
//					);
//
//			if(Main.getTheAircraft()
//					.getSystems()
//					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("m"))
//				theController.getSystemsZUnitChoiceBox().getSelectionModel().select(0);
//			else if(Main.getTheAircraft()
//					.getSystems()
//					.getZApexConstructionAxes().getUnit().toString().equalsIgnoreCase("ft"))
//				theController.getSystemsZUnitChoiceBox().getSelectionModel().select(1);
//
//		}
//		else {
//			theController.getTextFieldAircraftSystemsZ().setText("0.0");
//			theController.getSystemsZUnitChoiceBox().getSelectionModel().select(0);
//		}
	}
	
	public void createFuselageTopView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>();
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});

		double xMaxTopView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);

		int WIDTH = 650;
		int HEIGHT = 650;

		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Top View", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}

		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "FuselageTopView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getFuselageTopViewPane().getChildren().clear();
		theController.getFuselageTopViewPane().getChildren().add(sceneTopView.getRoot());
	}

	public void createFuselageSideView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// upper curve, sideview
		List<Amount<Length>> vX1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountX().stream().forEach(x -> vX1Upper.add(x));
		int nX1Upper = vX1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// lower curve, sideview
		List<Amount<Length>> vX2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountX().stream().forEach(x -> vX2Lower.add(x));
		int nX2Lower = vX2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXZLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Lower.get(vX2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double xMaxSideView = 1.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double xMinSideView = -0.20*Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMaxSideView = 1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMinSideView = -1.40*Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Side View", 
				"x (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMinSideView, xMaxSideView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinSideView, yMaxSideView);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "FuselageSideView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getFuselageSideViewPane().getChildren().clear();
		theController.getFuselageSideViewPane().getChildren().add(sceneSideView.getRoot());		
		
	}
	
	public void createFuselageFrontView() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// section upper curve
		List<Amount<Length>> vY1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountY().stream().forEach(y -> vY1Upper.add(y));
		int nY1Upper = vY1Upper.size();
		List<Amount<Length>> vZ1Upper = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionUpperCurveAmountZ().stream().forEach(z -> vZ1Upper.add(z));

		// section lower curve
		List<Amount<Length>> vY2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountY().stream().forEach(y -> vY2Lower.add(y));
		int nY2Lower = vY2Lower.size();
		List<Amount<Length>> vZ2Lower = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getSectionLowerCurveAmountZ().stream().forEach(z -> vZ2Lower.add(z));
		
		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Front View", false);
		IntStream.range(0, nY1Upper)
		.forEach(i -> {
			seriesFuselageCurve.add(vY1Upper.get(i).doubleValue(SI.METRE), vZ1Upper.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nY2Lower)
		.forEach(i -> {
			seriesFuselageCurve.add(vY2Lower.get(vY2Lower.size()-1-i).doubleValue(SI.METRE), vZ2Lower.get(vZ2Lower.size()-1-i).doubleValue(SI.METRE));
		});
		
		double yMaxFrontView = 1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double yMinFrontView = -1.20*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
		double zMaxFrontView = yMaxFrontView; 
		double zMinFrontView = yMinFrontView;
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));
		
		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));
		
		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Fuselage data representation - Front View", 
				"y (m)", 
				"z (m)",
				(XYDataset) dataset,
				PlotOrientation.VERTICAL,
                false, // legend
                true,  // tooltips
                false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinFrontView, yMaxFrontView);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(zMinFrontView, zMaxFrontView);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(0, xyAreaRenderer);
		plot.setDataset(0, dataset);
		plot.setRenderer(1, xyLineAndShapeRenderer);
		plot.setDataset(1, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "FuselageFrontView.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  
		
		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  
		
		//create browser
		Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10, javafx.scene.paint.Color.web("#666970"));
		theController.getFuselageFrontViewPane().getChildren().clear();
		theController.getFuselageFrontViewPane().getChildren().add(sceneFrontView.getRoot());
	}
	
	public void logFuselageFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaFuselageConsoleOutput().setText(
				Main.getTheAircraft().getFuselage().toString()
				);

		//---------------------------------------------------------------------------------
		// ADJUST CRITERION CHOICE BOX:
		if(Main.getTheAircraft() != null)
			theController.getFuselageAdjustCriterionChoiceBox().setDisable(false);
		
		//---------------------------------------------------------------------------------
		// PRESSURIZED FLAG: 
		if(Main.getTheAircraft().getFuselage().getPressurized().equals(Boolean.TRUE))
			theController.getFuselagePressurizedCheckBox().setSelected(true);
			
		//---------------------------------------------------------------------------------
		// DECK NUMBER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageDeckNumber().setText(
					Integer.toString(
							Main.getTheAircraft()
							.getFuselage()
							
							.getDeckNumber()
							)
					);
		else
			theController.getTextFieldFuselageDeckNumber().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageLength().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getFuselageLength()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getFuselageLength().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageLengthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getFuselageLength().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageLengthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageLength().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// SURFACE ROUGHNESS:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageSurfaceRoughness().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getRoughness()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageRoughnessUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageSurfaceRoughness().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// LENGTH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseLengthRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getNoseLengthRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseLengthRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE TIP OFFSET RATIO:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageNoseTipOffset().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getNoseTipOffset()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getNoseTipOffset().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getNoseTipOffset().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageNoseTipOffsetZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageNoseTipOffset().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE DX CAP PERCENT:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseDxCap().setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							
							.getNoseCapOffsetPercent()
							)
					);
		else
			theController.getTextFieldFuselageNoseDxCap().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD TYPE:
		if(Main.getTheAircraft().getFuselage() != null) { 
			if(theController.getWindshieldTypeChoiceBox() != null) {
				if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.DOUBLE)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.FLAT_FLUSH)		
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(1);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.FLAT_PROTRUDING)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(2);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.SINGLE_ROUND)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(3);
				else if(Main.getTheAircraft().getFuselage().getWindshieldType() == WindshieldTypeEnum.SINGLE_SHARP)
					theController.getWindshieldTypeChoiceBox().getSelectionModel().select(4);
			}
		}
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD WIDTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageNoseWindshieldWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getWindshieldWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldWidth().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageWindshieldWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageNoseWindshieldWidth().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// WINDSHIELD HEIGHT:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageNoseWindshieldHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getWindshieldHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getWindshieldHeight().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageWindshieldHeightUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageNoseWindshieldHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION HEIGHT TO TOTAL SECTION HEIGHT RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseMidSectionHeight().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionNoseMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseMidSectionHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseMidSectionRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidNoseRhoUpper()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseMidSectionRhoUpper().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NOSE MID-SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageNoseMidSectionRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidNoseRhoLower()
					.toString()
					);
		else
			theController.getTextFieldFuselageNoseMidSectionRhoLower().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER LENGTH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderLengthRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getCylinderLengthRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderLengthRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION WIDTH:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageCylinderSectionWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getSectionCylinderWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderWidth().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageCylinderSectionWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageCylinderSectionWidth().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageCylinderSectionHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getSectionCylinderHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getSectionCylinderHeight().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageCylinderSectionHeightUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageCylinderSectionHeight().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT FROM GROUND:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageCylinderHeightFromGround().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getHeightFromGround()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getHeightFromGround().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageHeightFromGroundUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageCylinderHeightFromGround().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION HEIGHT TO TOTAL HEIGH RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderSectionHeightRatio().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionCylinderLowerToTotalHeightRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderSectionHeightRatio().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderSectionRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionCylinderRhoUpper()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderSectionRhoUpper().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// CYLINDER SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageCylinderSectionRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionCylinderRhoLower()
					.toString()
					);
		else
			theController.getTextFieldFuselageCylinderSectionRhoLower().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE TIP OFFSET:
		if(Main.getTheAircraft().getFuselage() != null) {
			
			theController.getTextFieldFuselageTailTipOffset().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getFuselage()
							
							.getTailTipOffset()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft()
					.getFuselage()
					.getTailTipOffset().getUnit().toString().equalsIgnoreCase("m"))
				theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft()
					.getFuselage()
					.getTailTipOffset().getUnit().toString().equalsIgnoreCase("ft"))
				theController.getFuselageTailTipOffsetZUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldFuselageTailTipOffset().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// TAIL CONE DX CAP PERCENT:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailDxCap().setText(
					Double.toString(
							Main.getTheAircraft()
							.getFuselage()
							
							.getTailCapOffsetPercent()
							)
					);
		else
			theController.getTextFieldFuselageTailDxCap().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION HEIGHT TO TOTAL HEIGHT RATIO:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailMidSectionHeight().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionTailMidLowerToTotalHeightRatio()
					.toString()
					);
		else
			theController.getTextFieldFuselageTailMidSectionHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO UPPER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailMidRhoUpper().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidTailRhoUpper()
					.toString()
					);
		else
			theController.getTextFieldFuselageTailMidRhoUpper().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// TAIL CONE MID SECTION RHO LOWER:
		if(Main.getTheAircraft().getFuselage() != null)
			theController.getTextFieldFuselageTailMidRhoLower().setText(
					Main.getTheAircraft()
					.getFuselage()
					
					.getSectionMidTailRhoLower()
					.toString()
					);
		else
			theController.getTextFieldFuselageTailMidRhoLower().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// SPOILERS NUMBER CHECK:
		if (Main.getTheAircraft().getFuselage().getSpoilers().size() >= 
				theController.getTabPaneFuselageSpoilers().getTabs().size()) {
			
			int iStart = theController.getTabPaneFuselageSpoilers().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getFuselage().getSpoilers().size(); i++)
				theController.addFuselageSpoiler();
			
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER SPOILERS:
		for (int i=0; i<Main.getTheAircraft().getFuselage().getSpoilers().size(); i++) {
			
			SpoilerCreator currentSpoiler = Main.getTheAircraft().getFuselage().getSpoilers().get(i);
			
			//---------------------------------------------------------------------------------
			// INNER SPANWISE POSITION:
			if(Double.valueOf(currentSpoiler.getInnerStationSpanwisePosition()) != null) {
				theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getInnerStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageInnerSpanwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// OUTER SPANWISE POSITION:
			if(Double.valueOf(currentSpoiler.getOuterStationSpanwisePosition()) != null) {
				theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getOuterStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageOuterSpanwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// INNER CHORDWISE POSITION:
			if(Double.valueOf(currentSpoiler.getInnerStationChordwisePosition()) != null) {
				theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getInnerStationChordwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageInnerChordwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// OUTER CHORDWISE POSITION:
			if(Double.valueOf(currentSpoiler.getOuterStationChordwisePosition()) != null) {
				theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getOuterStationChordwisePosition())
						);
			}
			else
				theController.getTextFieldFuselageOuterChordwisePositionSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(currentSpoiler.getMinimumDeflection() != null) {
				
				theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getMinimumDeflection().getEstimatedValue())
						);
				
				if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
				else if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getChoiceBoxFuselageMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldFuselageMinimumDeflectionAngleSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(currentSpoiler.getMaximumDeflection() != null) {
				
				theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().get(i).setText(
						String.valueOf(currentSpoiler.getMaximumDeflection().getEstimatedValue())
						);
				
				if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
				else if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getChoiceBoxFuselageMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldFuselageMaximumDeflectionAngleSpoilerList().get(i).setText(
						"NOT INITIALIZED"
						);
			
		}
	}

	public void createSeatMap() {
		
		//--------------------------------------------------
		// get data vectors from fuselage discretization
		//--------------------------------------------------
		// left curve, upperview
		List<Amount<Length>> vX1Left = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountX().stream().forEach(x -> vX1Left.add(x));
		int nX1Left = vX1Left.size();
		List<Amount<Length>> vY1Left = new ArrayList<>();
		Main.getTheAircraft().getFuselage().getOutlineXYSideLCurveAmountY().stream().forEach(y -> vY1Left.add(y));

		// right curve, upperview
		List<Amount<Length>> vX2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountX().stream().forEach(x -> vX2Right.add(x));
		int nX2Right = vX2Right.size();
		List<Amount<Length>> vY2Right = new ArrayList<>(); 
		Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveAmountY().stream().forEach(y -> vY2Right.add(y));

		XYSeries seriesFuselageCurve = new XYSeries("Fuselage - Top View", false);
		IntStream.range(0, nX1Left)
		.forEach(i -> {
			seriesFuselageCurve.add(vX1Left.get(i).doubleValue(SI.METRE), vY1Left.get(i).doubleValue(SI.METRE));
		});
		IntStream.range(0, nX2Right)
		.forEach(i -> {
			seriesFuselageCurve.add(vX2Right.get(vX2Right.size()-1-i).doubleValue(SI.METRE), vY2Right.get(vY2Right.size()-1-i).doubleValue(SI.METRE));
		});

		//--------------------------------------------------
		// creating seat blocks outlines and seats (starting from FIRST class)
		//--------------------------------------------------
		List<SeatsBlock> seatBlockList = new ArrayList<>();
		List<XYSeries> seatBlockSeriesList = new ArrayList<>();
		List<XYSeries> seatsSeriesList = new ArrayList<>();
		
		Amount<Length> length = Amount.valueOf(0., SI.METER);
		Map<Integer, Amount<Length>> breaksMap = new HashMap<>();
		List<Map<Integer, Amount<Length>>> breaksMapList = new ArrayList<>();
		int classNumber = Main.getTheAircraft().getCabinConfiguration().getClassesNumber()-1;
		
		for (int i = 0; i < Main.getTheAircraft().getCabinConfiguration().getClassesNumber(); i++) {

			breaksMap.put(
					Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i), 
					Main.getTheAircraft().getCabinConfiguration().getWidthList().get(i)
					);
			breaksMapList.add(breaksMap);
			
			SeatsBlock seatsBlock = new SeatsBlock(
					new ISeatBlock.Builder()
					.setPosition(RelativePositionEnum.RIGHT)
					.setXStart(Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().plus(length))
					.setPitch(Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i))
					.setWidth(Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i))
					.setDistanceFromWall(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i))
					.putAllBreaksMap(breaksMapList.get(i))
					.setRowsNumber(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsList().get(classNumber-i))
					.setColumnsNumber(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i)[0])
//					.setMissingSeatRow(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRowList().get(classNumber-i))
					.setType(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(classNumber-i))					
					.build()
					);

			seatBlockList.add(seatsBlock);
			//........................................................................................................
			XYSeries seriesSeatBlock = new XYSeries("Seat Block " + i + " start", false);
			seriesSeatBlock.add(
					Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					- FusNacGeometryCalc.getWidthAtX(
							Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
							)/2
					+ Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
					);
			seriesSeatBlock.add(
					Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
					+ length.doubleValue(SI.METER),
					FusNacGeometryCalc.getWidthAtX(
							Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
							+ length.doubleValue(SI.METER),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
							Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
							)/2
					- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
					);
			//........................................................................................................
			Amount<Length> aisleWidth = Amount.valueOf(0.0, SI.METER);
			Amount<Length> currentYPosition = Amount.valueOf(0.0, SI.METER);
			Double breakLengthPitchFraction = 0.25;
			List<Integer> breakesPositionsIndexList = new ArrayList<>();
			for (int iBrake=0; iBrake<Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i); iBrake++) {
				Integer brekesInteval = Math.round(
						(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsList().get(classNumber-i)
						+ Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i))
						/ (Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksList().get(classNumber-i) + 1)
						);
				breakesPositionsIndexList.add((iBrake+1)*brekesInteval);
			}
			
			for(int j=0; j<Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i).length; j++) {
				if(j>0) {
					aisleWidth = Amount.valueOf( 
							(Main.getTheAircraft().getFuselage().getSectionCylinderWidth().doubleValue(SI.METER) 
									- (MyArrayUtils.sumArrayElements(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i))
											* Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER))
									- 2*Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER))
							/Main.getTheAircraft().getCabinConfiguration().getAislesNumber(),
							SI.METER
							);
					currentYPosition = Amount.valueOf(
							seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getYValue(),
							SI.METER
							);
				}
				for (int k = 0; k <Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsList().get(classNumber-i)[j]; k++) {
					
					int indexOfCurrentBrake = 10000;
					
					for (int r = 0;
							 r < Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsList().get(classNumber-i); 
							 r++) {
						
						XYSeries seriesSeats = new XYSeries("Column " + i + j + k + r, false);
						if(j>0) {
							int rowIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == rowIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										+ r * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										currentYPosition.doubleValue(SI.METER)
										- aisleWidth.doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
						}
						else {
							int columnIndex = r;
							if(breakesPositionsIndexList.stream().anyMatch(x -> x == columnIndex)) {
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ breakLengthPitchFraction * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								indexOfCurrentBrake = r;
							}
							else if (r > indexOfCurrentBrake)
								seriesSeats.add(
										seatsSeriesList.get(seatsSeriesList.size()-1).getDataItem(0).getXValue()
										+ Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
							else
								seriesSeats.add(
										Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METER)
										+ length.doubleValue(SI.METER)
										+ Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										+ r * Main.getTheAircraft().getCabinConfiguration().getPitchList().get(classNumber-i).doubleValue(SI.METER),
										Main.getTheAircraft().getFuselage().getSectionCylinderWidth().divide(2).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber-i).doubleValue(SI.METER)
										- Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).divide(2).doubleValue(SI.METER)
										- k * Main.getTheAircraft().getCabinConfiguration().getWidthList().get(classNumber-i).doubleValue(SI.METER)
										);
								
						}
						seatsSeriesList.add(seriesSeats);
						
					}
				}				
			}
		
			length = length.plus(seatsBlock.getLenghtOverall());
			seatBlockSeriesList.add(seriesSeatBlock);
			
		}
		
		XYSeries seriesSeatBlock = new XYSeries("Seat Block " + classNumber + " ending", false);
		seriesSeatBlock.add(
				Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				- FusNacGeometryCalc.getWidthAtX(
						Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
						)/2
				+ Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber).doubleValue(SI.METER)
				);
		seriesSeatBlock.add(
				Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
				+ length.doubleValue(SI.METER),
				FusNacGeometryCalc.getWidthAtX(
						Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow().doubleValue(SI.METRE)
						+ length.doubleValue(SI.METER),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveX(),
						Main.getTheAircraft().getFuselage().getOutlineXYSideRCurveY()
						)/2
				- Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallList().get(classNumber).doubleValue(SI.METER)
				);
		seatBlockSeriesList.add(seriesSeatBlock);
		
		double xMaxTopView = Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double xMinTopView = -Main.getTheAircraft().getFuselage().getFuselageLength().divide(2).doubleValue(SI.METRE);
		double yMaxTopView = Main.getTheAircraft().getFuselage().getFuselageLength().doubleValue(SI.METRE);
		double yMinTopView = 0.0;

		int WIDTH = 650;
		int HEIGHT = 650;

		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seatsSeriesList.stream().forEach(
				s -> seriesAndColorList.add(Tuple.of(s, Color.WHITE))
				);
		seatBlockSeriesList.stream().forEach(
				sb -> seriesAndColorList.add(Tuple.of(sb, Color.WHITE))
				);
		seriesAndColorList.add(Tuple.of(seriesFuselageCurve, Color.WHITE));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Seat Map representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i<seatsSeriesList.size()) {
				xyLineAndShapeRenderer.setSeriesLinesVisible(i, false);
				xyLineAndShapeRenderer.setSeriesShapesVisible(i, true);
				xyLineAndShapeRenderer.setSeriesShape(
						i,
						ShapeUtils.createDiamond(2.5f).getBounds()
						);
			}
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "SeatMap.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getCabinConfigurationSeatMapPane().getChildren().clear();
		theController.getCabinConfigurationSeatMapPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void logCabinConfigutionFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaCabinConfigurationConsoleOutput().setText(
				Main.getTheAircraft().getCabinConfiguration().toString()
				);

		if(Main.getTheAircraft().getCabinConfiguration() != null) {

			//---------------------------------------------------------------------------------
			// ACTUAL PASSENGERS NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getActualPassengerNumber()) != null) 
				theController.getTextFieldActualPassengersNumber().setText(
					Integer.toString(
							Main.getTheAircraft()
							.getCabinConfiguration()
							.getActualPassengerNumber()
							)
					);
			else
				theController.getTextFieldActualPassengersNumber().setText("0");
			//---------------------------------------------------------------------------------
			// MAXIMUM PASSENGERS NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getMaximumPassengerNumber()) != null)
				theController.getTextFieldMaximumPassengersNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getMaximumPassengerNumber()
								)
						);
			else
				theController.getTextFieldMaximumPassengersNumber().setText("0");
			//---------------------------------------------------------------------------------
			// FLIGHT CREW NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getFlightCrewNumber()) != null)
				theController.getTextFieldFlightCrewNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getFlightCrewNumber()
								)
						);
			else
				theController.getTextFieldFlightCrewNumber().setText("0");
			//---------------------------------------------------------------------------------
			// CLASSES NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getClassesNumber()) != null) 
				theController.getTextFieldClassesNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getClassesNumber()
								)
						);
			else
				theController.getTextFieldFlightCrewNumber().setText("0");
			//---------------------------------------------------------------------------------
			// CLASSES TYPE:
			if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType() != null) {
				
				for(int i=0; i<Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().size(); i++) {
					
					// CLASS 1
					if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.ECONOMY))
						theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(0);
					if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.BUSINESS))
						theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(1);
					if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.FIRST))
						theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(2);
					
					if (i==1) {
					
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(2);

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(2);
						
					}
					if (i==2) {
						
						// CLASS 1
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(0).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox1().getSelectionModel().select(2);

						// CLASS 2
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(1).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox2().getSelectionModel().select(2);
						
						// CLASS 3
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(2).equals(ClassTypeEnum.ECONOMY))
							theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().select(0);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(2).equals(ClassTypeEnum.BUSINESS))
							theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().select(1);
						if(Main.getTheAircraft().getCabinConfiguration().getTheCabinConfigurationBuilder().getClassesType().get(2).equals(ClassTypeEnum.FIRST))
							theController.getCabinConfigurationClassesTypeChoiceBox3().getSelectionModel().select(2);
						
					}
				}
			}
			
			//---------------------------------------------------------------------------------
			// AISLES NUMBER:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getAislesNumber()) != null)
				theController.getTextFieldAislesNumber().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getAislesNumber()
								)
						);
			else
				theController.getTextFieldAislesNumber().setText("0");
			//---------------------------------------------------------------------------------
			// X COORDINATE FIRST ROW:
			if(Main.getTheAircraft().getCabinConfiguration().getXCoordinatesFirstRow() != null) {
				
				theController.getTextFieldXCoordinateFirstRow().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getXCoordinatesFirstRow()
								.doubleValue(SI.METER)
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getXCoordinatesFirstRow().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getXCoordinatesFirstRow().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldXCoordinateFirstRow().setText("0.0");
				theController.getCabinConfigurationXCoordinateFirstRowUnitChoiceBox().getSelectionModel().select(0);
			}
			
//			//---------------------------------------------------------------------------------
//			// MISSING SEATS ROW:
//			if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow() != null) {
//				
//				for(int i=0; i<Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().size(); i++) {
//					
//					// CLASS 1
//					if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(0) != null)
//						theController.getTextFieldMissingSeatRow1().setText(
//							String.valueOf(
//									Arrays.toString(
//											Main.getTheAircraft()
//											.getCabinConfiguration()
//											.getMissingSeatsRow()
//											.get(0)
//											)
//									)
//							);
//					else
//						theController.getTextFieldMissingSeatRow1().setText("0");
//					
//					if (i==1) {
//					
//						// CLASS 1
//						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(0) != null)
//							theController.getTextFieldMissingSeatRow1().setText(
//								String.valueOf(
//										Arrays.toString(
//												Main.getTheAircraft()
//												.getCabinConfiguration()
//												.getMissingSeatsRow()
//												.get(0)
//												)
//										)
//								);
//						else
//							theController.getTextFieldMissingSeatRow1().setText("0");
//
//						// CLASS 2
//						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(1) != null)
//							theController.getTextFieldMissingSeatRow2().setText(
//								String.valueOf(
//										Arrays.toString(
//												Main.getTheAircraft()
//												.getCabinConfiguration()
//												.getMissingSeatsRow()
//												.get(1)
//												)
//										)
//								);
//						else
//							theController.getTextFieldMissingSeatRow2().setText("0");
//						
//					}
//					if (i==2) {
//						
//						// CLASS 1
//						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(0) != null)
//							theController.getTextFieldMissingSeatRow1().setText(
//								String.valueOf(
//										Arrays.toString(
//												Main.getTheAircraft()
//												.getCabinConfiguration()
//												.getMissingSeatsRow()
//												.get(0)
//												)
//										)
//								);
//						else
//							theController.getTextFieldMissingSeatRow1().setText("0");
//
//						// CLASS 2
//						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(1) != null)
//							theController.getTextFieldMissingSeatRow2().setText(
//								String.valueOf(
//										Arrays.toString(
//												Main.getTheAircraft()
//												.getCabinConfiguration()
//												.getMissingSeatsRow()
//												.get(1)
//												)
//										)
//								);
//						else
//							theController.getTextFieldMissingSeatRow2().setText("0");
//						
//						// CLASS 3
//						if(Main.getTheAircraft().getCabinConfiguration().getMissingSeatsRow().get(2) != null)
//							theController.getTextFieldMissingSeatRow3().setText(
//								String.valueOf(
//										Arrays.toString(
//												Main.getTheAircraft()
//												.getCabinConfiguration()
//												.getMissingSeatsRow()
//												.get(2)
//												)
//										)
//								);
//						else
//							theController.getTextFieldMissingSeatRow3().setText("0");
//					}
//				}
//			}

			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES ECONOMY:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksEconomyClass()) != null)
				theController.getTextFieldNumberOfBrakesEconomy().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksEconomyClass()
								)
						);
			else
				theController.getTextFieldNumberOfBrakesEconomy().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES BUSINESS:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksBusinessClass()) != null)
				theController.getTextFieldNumberOfBrakesBusiness().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksBusinessClass()
								)
						);
			else
				theController.getTextFieldNumberOfBrakesBusiness().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF BRAKES FIRST:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfBreaksFirstClass()) != null)
				theController.getTextFieldNumberOfBrakesFirst().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfBreaksFirstClass()
								)
						);
			else
				theController.getTextFieldNumberOfBrakesFirst().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS ECONOMY:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsEconomyClass()) != null)
				theController.getTextFieldNumberOfRowsEconomy().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsEconomyClass()
								)
						);
			else
				theController.getTextFieldNumberOfRowsEconomy().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS BUSINESS:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsBusinessClass()) != null)
				theController.getTextFieldNumberOfRowsBusiness().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsBusinessClass()
								)
						);
			else
				theController.getTextFieldNumberOfRowsBusiness().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF ROWS FIRST:
			if(Integer.valueOf(Main.getTheAircraft().getCabinConfiguration().getNumberOfRowsFirstClass()) != null)
				theController.getTextFieldNumberOfRowsFirst().setText(
						Integer.toString(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getNumberOfRowsFirstClass()
								)
						);
			else
				theController.getTextFieldNumberOfRowsFirst().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsEconomyClass() != null)
				theController.getTextFieldNumberOfColumnsEconomy().setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsEconomyClass()
										)
								)
						);
			else
				theController.getTextFieldNumberOfColumnsEconomy().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsBusinessClass() != null)
				theController.getTextFieldNumberOfColumnsBusiness().setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsBusinessClass()
										)
								)
						);
			else
				theController.getTextFieldNumberOfColumnsBusiness().setText("0");
			//---------------------------------------------------------------------------------
			// NUMBER OF COLUMNS FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getNumberOfColumnsFirstClass() != null)
				theController.getTextFieldNumberOfColumnsFirst().setText(
						String.valueOf(
								Arrays.toString(
										Main.getTheAircraft()
										.getCabinConfiguration()
										.getNumberOfColumnsFirstClass()
										)
								)
						);
			else
				theController.getTextFieldNumberOfColumnsFirst().setText("0");
			//---------------------------------------------------------------------------------
			// SEATS PITCH ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchEconomyClass() != null) {
				
				theController.getTextFieldSeatsPitchEconomy().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsPitchEconomy().setText("0.0");
				theController.getCabinConfigurationSeatsPitchEconomyUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS PITCH BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchBusinessClass() != null) {
				
				theController.getTextFieldSeatsPitchBusiness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsPitchBusiness().setText("0.0");
				theController.getCabinConfigurationSeatsPitchBusinessUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS PITCH FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getPitchFirstClass() != null) {
				
				theController.getTextFieldSeatsPitchFirst().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getPitchFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getPitchFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsPitchFirst().setText("0.0");
				theController.getCabinConfigurationSeatsPitchFirstUnitChoiceBox().getSelectionModel().select(0);
			}
			//---------------------------------------------------------------------------------
			// SEATS WIDTH ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthEconomyClass() != null) {
				
				theController.getTextFieldSeatsWidthEconomy().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsWidthEconomy().setText("0.0");
				theController.getCabinConfigurationSeatsWidthEconomyUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS WIDTH BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthBusinessClass() != null) {
				
				theController.getTextFieldSeatsWidthBusiness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsWidthBusiness().setText("0.0");
				theController.getCabinConfigurationSeatsWidthBusinessUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// SEATS WIDTH FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getWidthFirstClass() != null) {
				
				theController.getTextFieldSeatsWidthFirst().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getWidthFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getWidthFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldSeatsWidthFirst().setText("0.0");
				theController.getCabinConfigurationSeatsWidthFirstUnitChoiceBox().getSelectionModel().select(0);
			}
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL ECONOMY:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallEconomyClass() != null) {
				
				theController.getTextFieldDistanceFromWallEconomy().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallEconomyClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallEconomyClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallEconomyClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldDistanceFromWallEconomy().setText("0.0");
				theController.getCabinConfigurationDistanceFromWallEconomyUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL BUSINESS:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallBusinessClass() != null) {
				
				theController.getTextFieldDistanceFromWallBusiness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallBusinessClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallBusinessClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallBusinessClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldDistanceFromWallBusiness().setText("0.0");
				theController.getCabinConfigurationDistanceFromWallBusinessUnitChoiceBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// DISTANCE FROM WALL FIRST:
			if(Main.getTheAircraft().getCabinConfiguration().getDistanceFromWallFirstClass() != null) {
				
				theController.getTextFieldDistanceFromWallFirst().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCabinConfiguration()
								.getDistanceFromWallFirstClass()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallFirstClass().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCabinConfiguration()
						.getDistanceFromWallFirstClass().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else {
				theController.getTextFieldDistanceFromWallFirst().setText("0.0");
				theController.getCabinConfigurationDistanceFromWallFirstUnitChoiceBox().getSelectionModel().select(0);
			}
		}
	}
	
	public void createWingPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesWingTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from flaps
		//--------------------------------------------------
		List<XYSeries> seriesFlapsTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getWing().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getWing().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesFlapsTopView = new XYSeries("Flap" + i, false);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesFlapsTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesFlapsTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesFlapsTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesFlapsTopViewList.add(seriesFlapsTopView);
			}
		}
		//--------------------------------------------------
		// get data vectors from slats
		//--------------------------------------------------
		List<XYSeries> seriesSlatsTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getWing().getSlats().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getSlats().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSlats().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSlats().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getWing().getSlats().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getWing().getSlats().get(i).getOuterChordRatio();
				
				XYSeries seriesSlatTopView = new XYSeries("Slat" + i, false);
				seriesSlatTopView.add(
						xLELocalInnerChord,
						yIn
						);
				seriesSlatTopView.add(
						xLELocalInnerChord + (innerChordRatio*localChordInner),
						yIn
						);
				seriesSlatTopView.add(
						xLELocalOuterChord + (outerChordRatio*localChordOuter),
						yOut
						);
				seriesSlatTopView.add(
						xLELocalOuterChord,
						yOut
						);
				seriesSlatTopView.add(
						xLELocalInnerChord,
						yIn
						);

				seriesSlatsTopViewList.add(seriesSlatTopView);
			}
		}
		//--------------------------------------------------
		// get data vectors from ailerons
		//--------------------------------------------------
		XYSeries seriesAileronTopView = new XYSeries("Right Slat", false);
		
		if (!Main.getTheAircraft().getWing().getAsymmetricFlaps().isEmpty()) {
			
			double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
					Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getInnerStationSpanwisePosition()
					).doubleValue(SI.METER);
			double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
					Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getOuterStationSpanwisePosition()
					).doubleValue(SI.METER);

			double localChordInner = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
					yIn);
			double localChordOuter = GeometryCalc.getChordAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
					yOut);

			double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
					yIn);
			double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
					MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
					yOut);

			double innerChordRatio = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getInnerChordRatio();
			double outerChordRatio = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0).getOuterChordRatio();

			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
					yIn
					);
			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner),
					yIn
					);
			seriesAileronTopView.add(
					xLELocalOuterChord + (localChordOuter),
					yOut
					);
			seriesAileronTopView.add(
					xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
					yOut
					);
			seriesAileronTopView.add(
					xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
					yIn
					);

		}
		//--------------------------------------------------
		// get data vectors from spoilers
		//--------------------------------------------------
		List<XYSeries> seriesSpoilersTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getWing().getSpoilers().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getWing().getSpoilers().size(); i++) {
				
				double yIn = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSpoilers().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getWing().getSemiSpan().times(
						Main.getTheAircraft().getWing().getSpoilers().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getWing().getDiscretizedXle()),
						yOut);
				
				double innerChordwisePosition = Main.getTheAircraft().getWing().getSpoilers().get(i).getInnerStationChordwisePosition();
				double outerChordwisePosition = Main.getTheAircraft().getWing().getSpoilers().get(i).getOuterStationChordwisePosition();
				
				XYSeries seriesSpoilerTopView = new XYSeries("Spoiler" + i, false);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*innerChordwisePosition),
						yIn
						);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*outerChordwisePosition),
						yIn
						);
				seriesSpoilerTopView.add(
						xLELocalOuterChord + (localChordOuter*outerChordwisePosition),
						yOut
						);
				seriesSpoilerTopView.add(
						xLELocalOuterChord + (localChordOuter*innerChordwisePosition),
						yOut
						);
				seriesSpoilerTopView.add(
						xLELocalInnerChord + (localChordInner*innerChordwisePosition),
						yIn
						);

				seriesSpoilersTopViewList.add(seriesSpoilerTopView);
			}
		}
		
		double semispan = Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getWing().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getWing().getDiscretizedXle().get(
				Main.getTheAircraft().getWing().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesFlapsTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#778899")))
				);
		seriesSlatsTopViewList.stream().forEach(
				slat -> seriesAndColorList.add(Tuple.of(slat, Color.decode("#6495ED")))
				);
		seriesAndColorList.add(Tuple.of(seriesAileronTopView, Color.decode("#1E90FF")));
		seriesSpoilersTopViewList.stream().forEach(
				spoiler -> seriesAndColorList.add(Tuple.of(spoiler, Color.decode("#ADD8E6")))
				);
		seriesAndColorList.add(Tuple.of(seriesWingTopView, Color.decode("#87CEFA")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Wing Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "WingPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getWingPlanformPane().getChildren().clear();
		theController.getWingPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void createEquivalentWingView() {
		
		//--------------------------------------------------
		// get data vectors from wing discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getWing().getDiscretizedTopViewAsArray(ComponentEnum.WING);
		
		XYSeries seriesWingTopView = new XYSeries("Wing Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesWingTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesWingTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from equivalent wing discretization
		//--------------------------------------------------
		int nSec = Main.getTheAircraft().getWing().getDiscretizedXle().size();
		int nPanels = Main.getTheAircraft().getWing().getPanels().size();

		XYSeries seriesEquivalentWingTopView = new XYSeries("Equivalent Wing", false);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE(),
				0.0
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(nSec - 1).doubleValue(SI.METER),
				Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER)
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getDiscretizedXle().get(nSec - 1).plus(
						Main.getTheAircraft().getWing().getPanels().get(nPanels - 1).getChordTip()
						).doubleValue(SI.METER),
				Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER)
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
				- Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE(),
				0.0
				);
		seriesEquivalentWingTopView.add(
				Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE(),
				0.0
				);
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getWing().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getWing().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getWing().getDiscretizedXle().get(
				Main.getTheAircraft().getWing().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getWing().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
			
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesEquivalentWingTopView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesWingTopView, Color.decode("#87CEFA")));
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Wing Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		xyAreaRenderer.setDefaultSeriesVisible(false);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, true);
			xyAreaRenderer.setSeriesPaint(
					i,
					seriesAndColorList.get(i)._2()
					);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		xyLineAndShapeRenderer.setDrawSeriesLineAsPath(true);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==1 || i==2) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
			xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
			xyLineAndShapeRenderer.setSeriesStroke(
					i, 
					new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
					false
					);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "EquivalentWing.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getEquivalentWingPane().getChildren().clear();
		theController.getEquivalentWingPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void logWingFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaWingConsoleOutput().setText(
				Main.getTheAircraft().getWing().toString()
				+ "\n\n\n" + Main.getTheAircraft().getFuelTank().toString()
				);

		if(Main.getTheAircraft().getWing() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.getWingAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING FLAG: 
			if(Main.getTheAircraft().getWing().getEquivalentWingFlag() == true)
				theController.getEquivalentWingCheckBox().setSelected(true);

			//---------------------------------------------------------------------------------
			// MAIN SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getWing().getMainSparDimensionlessPosition()) != null) 
				theController.getTextFieldWingMainSparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								
								.getMainSparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldWingMainSparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// SECONDARY SPAR LOCATION:
			if(Double.valueOf(Main.getTheAircraft().getWing().getSecondarySparDimensionlessPosition()) != null) 
				theController.getTextFieldWingSecondarySparAdimensionalPosition().setText(
						Double.toString(
								Main.getTheAircraft()
								.getWing()
								
								.getSecondarySparDimensionlessPosition()
								)
						);
			else
				theController.getTextFieldWingSecondarySparAdimensionalPosition().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getWing().getRoughness() != null) {
				
				theController.getTextFieldWingRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.getWingRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getWingRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldWingRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// WINGLET HEIGHT:
			if(Main.getTheAircraft().getWing().getWingletHeight() != null) {
				
				theController.getTextFieldWingWingletHeight().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getWingletHeight()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing()
						.getWingletHeight().getUnit().toString().equalsIgnoreCase("m"))
					theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing()
						.getWingletHeight().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getWingWingletHeightUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldWingWingletHeight().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AREA:
			if(Main.getTheAircraft().getWing().getSurfacePlanform() != null) {
				
				theController.getTextFieldEquivalentWingArea().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getSurfacePlanform()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing()
						.getSurfacePlanform().getUnit().toString().equalsIgnoreCase("m²"))
					theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing()
						.getSurfacePlanform().getUnit().toString().equalsIgnoreCase("ft²"))
					theController.getEquivalentWingAreaUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingArea().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING ASPECT RATIO:
			if(Main.getTheAircraft().getWing().getAspectRatio() != null) {
				
				theController.getTextFieldEquivalentWingAspectRatio().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getAspectRatio()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAspectRatio().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING KINK POSITION:
			if(Double.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessKinkPosition()) != null) {
				
				theController.getTextFieldEquivalentWingKinkPosition().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getRealWingDimensionlessKinkPosition()
								)
						);
			}
			else 
				theController.getTextFieldEquivalentWingKinkPosition().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING SWEEP LE:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getSweepLeadingEdge() != null) {
				
				theController.getTextFieldEquivalentWingSweepLeadingEdge().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getSweepLeadingEdge()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getSweepLeadingEdge().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingSweepLEUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingSweepLeadingEdge().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TWIST AT TIP:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTwistGeometricAtTip() != null) {
				
				theController.getTextFieldEquivalentWingTwistAtTip().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getTwistGeometricAtTip()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getTwistAerodynamicAtTip().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingTwistAtTipUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingTwistAtTip().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING DIHEDRAL:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getDihedral() != null) {
				
				theController.getTextFieldEquivalentWingDihedral().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getDihedral()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("°")
						|| Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getWing().getEquivalentWing().getPanels().get(0)
						.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getEquivalentWingDihedralUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldEquivalentWingDihedral().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING TAPER RATIO:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getTaperRatio() != null) {
				
				theController.getTextFieldEquivalentWingTaperRatio().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getTaperRatio()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingTaperRatio().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING X OFFSET LE:
			if(Double.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordLE()) != null) {
				
				theController.getTextFieldEquivalentWingRootXOffsetLE().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getRealWingDimensionlessXOffsetRootChordLE()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingRootXOffsetLE().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// EQUIVALENT WING X OFFSET TE:
			if(Double.valueOf(Main.getTheAircraft().getWing().getEquivalentWing().getRealWingDimensionlessXOffsetRootChordTE()) != null) {
				
				theController.getTextFieldEquivalentWingRootXOffsetTE().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getRealWingDimensionlessXOffsetRootChordTE()
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingRootXOffsetTE().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL ROOT PATH:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getAirfoilRoot() != null) {
				
				theController.getTextFieldEquivalentWingAirfoilRootPath().setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getAirfoilRoot()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAirfoilRootPath().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL KINK PATH:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getEquivalentWingAirfoilKink() != null) {
				
				theController.getTextFieldEquivalentWingAirfoilKinkPath().setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getEquivalentWingAirfoilKink()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAirfoilKinkPath().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// EQUIVALENT WING AIRFOIL TIP PATH:
			if(Main.getTheAircraft().getWing().getEquivalentWing().getPanels().get(0).getAirfoilTip() != null) {
				
				theController.getTextFieldEquivalentWingAirfoilTipPath().setText(
						String.valueOf(
								Main.getInputDirectoryPath()
								+ File.separator
								+ "Template_Aircraft"
								+ File.separator
								+ "lifting_surfaces"
								+ File.separator
								+ "airfoils"
								+ File.separator
								+ Main.getTheAircraft()
								.getWing()
								
								.getEquivalentWing()
								.getPanels().get(0)
								.getAirfoilTip()
								.getName()
								+ ".xml"
								)
						);
			}
			else
				theController.getTextFieldEquivalentWingAirfoilTipPath().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getPanels().size() >= 
					theController.getTabPaneWingPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getPanels().size(); i++)
					theController.addWingPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getWing().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getWing().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxWingLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
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
				else
					theController.getTextFieldWingSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldWingSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldWingDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldWingInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldWingInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldWingInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getWing().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldWingInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldWingOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldWingOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldWingOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getWing().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldWingOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// FLAPS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getSymmetricFlaps().size() >= 
					theController.getTabPaneWingFlaps().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingFlaps().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getSymmetricFlaps().size(); i++)
					theController.addFlap();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER FLAPS:
			for (int i=0; i<Main.getTheAircraft().getWing().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentFlap = Main.getTheAircraft().getWing().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentFlap.getType() != null) {
					if(currentFlap.getType().equals(FlapTypeEnum.SINGLE_SLOTTED))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(0);
					else if(currentFlap.getType().equals(FlapTypeEnum.DOUBLE_SLOTTED))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(1);
					else if(currentFlap.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(2);
					else if(currentFlap.getType().equals(FlapTypeEnum.FOWLER))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(3);
					else if(currentFlap.getType().equals(FlapTypeEnum.OPTIMIZED_FOWLER))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(4);
					else if(currentFlap.getType().equals(FlapTypeEnum.TRIPLE_SLOTTED))
						theController.getChoiceBoxWingFlapTypeList().get(i).getSelectionModel().select(5);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentFlap.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerPositionFlapList().get(i).setText(
							String.valueOf(currentFlap.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerPositionFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentFlap.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterPositionFlapList().get(i).setText(
							String.valueOf(currentFlap.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterPositionFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentFlap.getInnerChordRatio()) != null) {
					theController.getTextFieldWingInnerChordRatioFlapList().get(i).setText(
							String.valueOf(currentFlap.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldWingInnerChordRatioFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentFlap.getOuterChordRatio())!= null) {
					theController.getTextFieldWingOuterChordRatioFlapList().get(i).setText(
							String.valueOf(currentFlap.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldWingOuterChordRatioFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentFlap.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleFlapList().get(i).setText(
							String.valueOf(currentFlap.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(0);
					else if(currentFlap.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentFlap.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleFlapList().get(i).setText(
							String.valueOf(currentFlap.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(0);
					else if(currentFlap.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleFlapUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleFlapList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// SLATS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getSlats().size() >= 
					theController.getTabPaneWingSlats().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingSlats().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getSlats().size(); i++)
					theController.addSlat();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER SLATS:
			for (int i=0; i<Main.getTheAircraft().getWing().getSlats().size(); i++) {
				
				SlatCreator currentSlat = Main.getTheAircraft().getWing().getSlats().get(i);
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentSlat.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerPositionSlatList().get(i).setText(
							String.valueOf(currentSlat.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerPositionSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentSlat.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterPositionSlatList().get(i).setText(
							String.valueOf(currentSlat.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterPositionSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentSlat.getInnerChordRatio()) != null) {
					theController.getTextFieldWingInnerChordRatioSlatList().get(i).setText(
							String.valueOf(currentSlat.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldWingInnerChordRatioSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentSlat.getOuterChordRatio()) != null) {
					theController.getTextFieldWingOuterChordRatioSlatList().get(i).setText(
							String.valueOf(currentSlat.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldWingOuterChordRatioSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// EXTENSION RATIO:
				if(Double.valueOf(currentSlat.getExtensionRatio()) != null) {
					theController.getTextFieldWingExtensionRatioSlatList().get(i).setText(
							String.valueOf(currentSlat.getExtensionRatio())
							);
				}
				else
					theController.getTextFieldWingExtensionRatioSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentSlat.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleSlatList().get(i).setText(
							String.valueOf(currentSlat.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(0);
					else if(currentSlat.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentSlat.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleSlatList().get(i).setText(
							String.valueOf(currentSlat.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(0);
					else if(currentSlat.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSlatUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleSlatList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// LEFT AILERONS:

			AsymmetricFlapCreator leftAileron = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(0);

			//---------------------------------------------------------------------------------
			// TYPE:
			if(leftAileron.getType() != null) {
				if(leftAileron.getType().equals(FlapTypeEnum.PLAIN))
					theController.getWingLeftAileronTypeChoichBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// INNER POSITION:
			if(Double.valueOf(leftAileron.getInnerStationSpanwisePosition()) != null) {
				theController.getTextFieldWingInnerPositionAileronLeft().setText(
						String.valueOf(leftAileron.getInnerStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldWingInnerPositionAileronLeft().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER POSITION:
			if(Double.valueOf(leftAileron.getOuterStationSpanwisePosition()) != null) {
				theController.getTextFieldWingOuterPositionAileronLeft().setText(
						String.valueOf(leftAileron.getOuterStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldWingOuterPositionAileronLeft().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// INNER CHORD RATIO:
			if(Double.valueOf(leftAileron.getInnerChordRatio()) != null) {
				theController.getTextFieldWingInnerChordRatioAileronLeft().setText(
						String.valueOf(leftAileron.getInnerChordRatio())
						);
			}
			else
				theController.getTextFieldWingInnerChordRatioAileronLeft().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER CHORD RATIO:
			if(Double.valueOf(leftAileron.getOuterChordRatio()) != null) {
				theController.getTextFieldWingOuterChordRatioAileronLeft().setText(
						String.valueOf(leftAileron.getOuterChordRatio())
						);
			}
			else
				theController.getTextFieldWingOuterChordRatioAileronLeft().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(leftAileron.getMinimumDeflection() != null) {

				theController.getTextFieldWingMinimumDeflectionAngleAileronLeft().setText(
						String.valueOf(leftAileron.getMinimumDeflection().getEstimatedValue())
						);

				if(leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getWingMinimumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().select(0);
				else if(leftAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getWingMinimumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().select(1);

			}
			else
				theController.getTextFieldWingMinimumDeflectionAngleAileronLeft().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(leftAileron.getMaximumDeflection() != null) {

				theController.getTextFieldWingMaximumDeflectionAngleAileronLeft().setText(
						String.valueOf(leftAileron.getMaximumDeflection().getEstimatedValue())
						);

				if(leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getWingMaximumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().select(0);
				else if(leftAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getWingMaximumDeflectionAngleAileronLeftUnitChoiceBox().getSelectionModel().select(1);

			}
			else
				theController.getTextFieldWingMaximumDeflectionAngleAileronLeft().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// RIGHT AILERONS:

			AsymmetricFlapCreator rightAileron = Main.getTheAircraft().getWing().getAsymmetricFlaps().get(1);

			//---------------------------------------------------------------------------------
			// TYPE:
			if(rightAileron.getType() != null) {
				if(rightAileron.getType().equals(FlapTypeEnum.PLAIN))
					theController.getWingRightAileronTypeChoichBox().getSelectionModel().select(0);
			}
			
			//---------------------------------------------------------------------------------
			// INNER POSITION:
			if(Double.valueOf(rightAileron.getInnerStationSpanwisePosition()) != null) {
				theController.getTextFieldWingInnerPositionAileronRight().setText(
						String.valueOf(rightAileron.getInnerStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldWingInnerPositionAileronRight().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER POSITION:
			if(Double.valueOf(rightAileron.getOuterStationSpanwisePosition()) != null) {
				theController.getTextFieldWingOuterPositionAileronRight().setText(
						String.valueOf(rightAileron.getOuterStationSpanwisePosition())
						);
			}
			else
				theController.getTextFieldWingOuterPositionAileronRight().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// INNER CHORD RATIO:
			if(Double.valueOf(rightAileron.getInnerChordRatio()) != null) {
				theController.getTextFieldWingInnerChordRatioAileronRight().setText(
						String.valueOf(rightAileron.getInnerChordRatio())
						);
			}
			else
				theController.getTextFieldWingInnerChordRatioAileronRight().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// OUTER CHORD RATIO:
			if(Double.valueOf(rightAileron.getOuterChordRatio()) != null) {
				theController.getTextFieldWingOuterChordRatioAileronRight().setText(
						String.valueOf(rightAileron.getOuterChordRatio())
						);
			}
			else
				theController.getTextFieldWingOuterChordRatioAileronRight().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MINIMUM DEFLECTION:
			if(rightAileron.getMinimumDeflection() != null) {

				theController.getTextFieldWingMinimumDeflectionAngleAileronRight().setText(
						String.valueOf(rightAileron.getMinimumDeflection().getEstimatedValue())
						);

				if(rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().select(0);
				else if(rightAileron.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getWingMinimumDeflectionAngleAileronRigthUnitChoiceBox().getSelectionModel().select(1);

			}
			else
				theController.getTextFieldWingMinimumDeflectionAngleAileronRight().setText(
						"NOT INITIALIZED"
						);

			//---------------------------------------------------------------------------------
			// MAXIMUM DEFLECTION:
			if(rightAileron.getMaximumDeflection() != null) {

				theController.getTextFieldWingMaximumDeflectionAngleAileronRight().setText(
						String.valueOf(rightAileron.getMaximumDeflection().getEstimatedValue())
						);

				if(rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
						|| rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
					theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().select(0);
				else if(rightAileron.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
					theController.getWingMaximumDeflectionAngleAileronRightUnitChoiceBox().getSelectionModel().select(1);

			}
			else
				theController.getTextFieldWingMaximumDeflectionAngleAileronRight().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// SPOILERS NUMBER CHECK:
			if (Main.getTheAircraft().getWing().getSpoilers().size() >= 
					theController.getTabPaneWingSpoilers().getTabs().size()) {
				
				int iStart = theController.getTabPaneWingSpoilers().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getWing().getSpoilers().size(); i++)
					theController.addSpoiler();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER SPOILERS:
			for (int i=0; i<Main.getTheAircraft().getWing().getSpoilers().size(); i++) {
				
				SpoilerCreator currentSpoiler = Main.getTheAircraft().getWing().getSpoilers().get(i);
				
				//---------------------------------------------------------------------------------
				// INNER SPANWISE POSITION:
				if(Double.valueOf(currentSpoiler.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldWingInnerSpanwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerSpanwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER SPANWISE POSITION:
				if(Double.valueOf(currentSpoiler.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldWingOuterSpanwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterSpanwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORDWISE POSITION:
				if(Double.valueOf(currentSpoiler.getInnerStationChordwisePosition()) != null) {
					theController.getTextFieldWingInnerChordwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getInnerStationChordwisePosition())
							);
				}
				else
					theController.getTextFieldWingInnerChordwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORDWISE POSITION:
				if(Double.valueOf(currentSpoiler.getOuterStationChordwisePosition()) != null) {
					theController.getTextFieldWingOuterChordwisePositionSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getOuterStationChordwisePosition())
							);
				}
				else
					theController.getTextFieldWingOuterChordwisePositionSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentSpoiler.getMinimumDeflection() != null) {
					
					theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
					else if(currentSpoiler.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMinimumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMinimumDeflectionAngleSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentSpoiler.getMaximumDeflection() != null) {
					
					theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().get(i).setText(
							String.valueOf(currentSpoiler.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(0);
					else if(currentSpoiler.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxWingMaximumDeflectionAngleSpoilerUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldWingMaximumDeflectionAngleSpoilerList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
		}
	}
	
	public void createHTailPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from HTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getHTail().getDiscretizedTopViewAsArray(ComponentEnum.HORIZONTAL_TAIL);
		
		XYSeries seriesHTailTopView = new XYSeries("HTail Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesHTailTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesHTailTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from flaps
		//--------------------------------------------------
		List<XYSeries> seriesElevatorsTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getHTail().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getHTail().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getHTail().getSemiSpan().times(
						Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getHTail().getSemiSpan().times(
						Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getHTail().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesElevatorTopView = new XYSeries("Elevator" + i, false);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesElevatorTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesElevatorTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesElevatorTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesElevatorsTopViewList.add(seriesElevatorTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getHTail().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getHTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getHTail().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getHTail().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getHTail().getDiscretizedXle().get(
				Main.getTheAircraft().getHTail().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getHTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getHTail().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesElevatorsTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#0066CC")))
				);
		seriesAndColorList.add(Tuple.of(seriesHTailTopView, Color.decode("#00008B")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Horizontal Tail Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
				xyAreaRenderer.setSeriesPaint(
						i,
						seriesAndColorList.get(i)._2()
						);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "HTailPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.gethTailPlanformPane().getChildren().clear();
		theController.gethTailPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void logHTailFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaHTailConsoleOutput().setText(
				Main.getTheAircraft().getHTail().toString()
				);

		if(Main.getTheAircraft().getHTail() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.gethTailAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getHTail().getRoughness() != null) {
				
				theController.getTextFieldHTailRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getHTail()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getHTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.gethTailRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getHTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.gethTailRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldHTailRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getHTail().getPanels().size() >= 
					theController.getTabPaneHTailPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneHTailPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getHTail().getPanels().size(); i++)
					theController.addHTailPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getHTail().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getHTail().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxHTailLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
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
				else
					theController.getTextFieldHTailSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldHTailSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldHTailDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldHTailInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldHTailInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldHTailInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getHTail().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldHTailInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldHTailOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldHTailOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldHTailOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getHTail().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldHTailOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// ELEVATORS NUMBER CHECK:
			if (Main.getTheAircraft().getHTail().getSymmetricFlaps().size() >= 
					theController.getTabPaneHTailElevators().getTabs().size()) {
				
				int iStart = theController.getTabPaneHTailElevators().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getHTail().getSymmetricFlaps().size(); i++)
					theController.addElevator();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER ELEVATORS:
			for (int i=0; i<Main.getTheAircraft().getHTail().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentElevator = Main.getTheAircraft().getHTail().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentElevator.getType() != null) {
					if(currentElevator.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxHTailElevatorTypeList().get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentElevator.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldHTailInnerPositionElevatorList().get(i).setText(
							String.valueOf(currentElevator.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldHTailInnerPositionElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentElevator.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldHTailOuterPositionElevatorList().get(i).setText(
							String.valueOf(currentElevator.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldHTailOuterPositionElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentElevator.getInnerChordRatio()) != null) {
					theController.getTextFieldHTailInnerChordRatioElevatorList().get(i).setText(
							String.valueOf(currentElevator.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldHTailInnerChordRatioElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentElevator.getOuterChordRatio()) != null) {
					theController.getTextFieldHTailOuterChordRatioElevatorList().get(i).setText(
							String.valueOf(currentElevator.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldHTailOuterChordRatioElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentElevator.getMinimumDeflection() != null) {
					
					theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().get(i).setText(
							String.valueOf(currentElevator.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(0);
					else if(currentElevator.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailMinimumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailMinimumDeflectionAngleElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentElevator.getMaximumDeflection() != null) {
					
					theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().get(i).setText(
							String.valueOf(currentElevator.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(0);
					else if(currentElevator.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxHTailMaximumDeflectionAngleElevatorUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldHTailMaximumDeflectionAngleElevatorList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	public void createVTailPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from VTail discretization
		//--------------------------------------------------
		Double[][] dataTopViewVTail = Main.getTheAircraft().getVTail().getDiscretizedTopViewAsArray(ComponentEnum.VERTICAL_TAIL);
		
		XYSeries seriesVTailTopView = new XYSeries("VTail", false);
		IntStream.range(0, dataTopViewVTail.length)
		.forEach(i -> {
			seriesVTailTopView.add(
					dataTopViewVTail[i][1],
					dataTopViewVTail[i][0]
					);
		});
		seriesVTailTopView.add(
				dataTopViewVTail[0][1],
				dataTopViewVTail[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from rudders
		//--------------------------------------------------
		List<XYSeries> seriesRudderTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getVTail().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getVTail().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getVTail().getSemiSpan().times(
						Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getVTail().getSemiSpan().times(
						Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getVTail().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesRudderTopView = new XYSeries("Rudder" + i, false);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio))
						);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner)
						);
				seriesRudderTopView.add(
						yOut,
						xLELocalOuterChord + (localChordOuter)
						);
				seriesRudderTopView.add(
						yOut,
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio))
						);
				seriesRudderTopView.add(
						yIn,
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio))
						);
			
				seriesRudderTopViewList.add(seriesRudderTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE),
				Main.getTheAircraft().getVTail().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getVTail().getMeanAerodynamicChord().doubleValue(SI.METRE)
				);
		
		double span = Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getVTail().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getVTail().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getVTail().getDiscretizedXle().get(
				Main.getTheAircraft().getVTail().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - span/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*span) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getVTail().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getVTail().getSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesRudderTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#FF8C00")))
				);
		seriesAndColorList.add(Tuple.of(seriesVTailTopView, Color.decode("#FFD700")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Vertical Tail Planform representation", 
				"z (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(xMinTopView, xMaxTopView);
		domain.setInverted(Boolean.FALSE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(yMinTopView, yMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
				xyAreaRenderer.setSeriesPaint(
						i,
						seriesAndColorList.get(i)._2()
						);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "VTailPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getvTailPlanformPane().getChildren().clear();
		theController.getvTailPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void logVTailFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaVTailConsoleOutput().setText(
				Main.getTheAircraft().getVTail().toString()
				);

		if(Main.getTheAircraft().getVTail() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.getvTailAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getVTail().getRoughness() != null) {
				
				theController.getTextFieldVTailRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getVTail()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getVTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.getvTailRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getVTail()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getvTailRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldVTailRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getVTail().getPanels().size() >= 
					theController.getTabPaneVTailPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneVTailPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getVTail().getPanels().size(); i++)
					theController.addVTailPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getVTail().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getVTail().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxVTailLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
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
				else
					theController.getTextFieldVTailSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldVTailSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldVTailDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldVTailInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldVTailInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldVTailInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getVTail().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldVTailInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldVTailOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldVTailOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldVTailOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getVTail().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldVTailOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// RudderS NUMBER CHECK:
			if (Main.getTheAircraft().getVTail().getSymmetricFlaps().size() >= 
					theController.getTabPaneVTailRudders().getTabs().size()) {
				
				int iStart = theController.getTabPaneVTailRudders().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getVTail().getSymmetricFlaps().size(); i++)
					theController.addRudder();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER RudderS:
			for (int i=0; i<Main.getTheAircraft().getVTail().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentRudder = Main.getTheAircraft().getVTail().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentRudder.getType() != null) {
					if(currentRudder.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxVTailRudderTypeList().get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentRudder.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldVTailInnerPositionRudderList().get(i).setText(
							String.valueOf(currentRudder.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldVTailInnerPositionRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentRudder.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldVTailOuterPositionRudderList().get(i).setText(
							String.valueOf(currentRudder.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldVTailOuterPositionRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentRudder.getInnerChordRatio()) != null) {
					theController.getTextFieldVTailInnerChordRatioRudderList().get(i).setText(
							String.valueOf(currentRudder.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldVTailInnerChordRatioRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentRudder.getOuterChordRatio()) != null) {
					theController.getTextFieldVTailOuterChordRatioRudderList().get(i).setText(
							String.valueOf(currentRudder.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldVTailOuterChordRatioRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentRudder.getMinimumDeflection() != null) {
					
					theController.getTextFieldVTailMinimumDeflectionAngleRudderList().get(i).setText(
							String.valueOf(currentRudder.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(0);
					else if(currentRudder.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailMinimumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailMinimumDeflectionAngleRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentRudder.getMaximumDeflection() != null) {
					
					theController.getTextFieldVTailMaximumDeflectionAngleRudderList().get(i).setText(
							String.valueOf(currentRudder.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxVTailMaximumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(0);
					else if(currentRudder.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxVTailMaximumDeflectionAngleRudderUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldVTailMaximumDeflectionAngleRudderList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	public void createCanardPlanformView() {
		
		//--------------------------------------------------
		// get data vectors from Canard discretization
		//--------------------------------------------------
		Double[][] dataTopViewIsolated = Main.getTheAircraft().getCanard().getDiscretizedTopViewAsArray(ComponentEnum.CANARD);
		
		XYSeries seriesCanardTopView = new XYSeries("Canard Planform", false);
		IntStream.range(0, dataTopViewIsolated.length)
		.forEach(i -> {
			seriesCanardTopView.add(
					dataTopViewIsolated[i][1],
					dataTopViewIsolated[i][0]
					);
		});
		seriesCanardTopView.add(
				dataTopViewIsolated[0][1],
				dataTopViewIsolated[0][0]
				);
		
		//--------------------------------------------------
		// get data vectors from control surface
		//--------------------------------------------------
		List<XYSeries> seriesControlSurfacesTopViewList = new ArrayList<>();
		if (!Main.getTheAircraft().getCanard().getSymmetricFlaps().isEmpty()) {
			for(int i=0; i<Main.getTheAircraft().getCanard().getSymmetricFlaps().size(); i++) {
				
				double yIn = Main.getTheAircraft().getCanard().getSemiSpan().times(
						Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getInnerStationSpanwisePosition()
						).doubleValue(SI.METER);
				double yOut = Main.getTheAircraft().getCanard().getSemiSpan().times(
						Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getOuterStationSpanwisePosition()
						).doubleValue(SI.METER);
				
				double localChordInner = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedChords()),
						yIn);
				double localChordOuter = GeometryCalc.getChordAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedChords()),
						yOut);
				
				double xLELocalInnerChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedXle()),
						yIn);
				double xLELocalOuterChord = GeometryCalc.getXLEAtYActual(
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedYs()),
						MyArrayUtils.convertListOfAmountTodoubleArray(Main.getTheAircraft().getCanard().getDiscretizedXle()),
						yOut);
				
				double innerChordRatio = Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getInnerChordRatio();
				double outerChordRatio = Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i).getOuterChordRatio();
				
				XYSeries seriesControlSurfaceTopView = new XYSeries("Control Surface" + i, false);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner),
						yIn
						);
				seriesControlSurfaceTopView.add(
						xLELocalOuterChord + (localChordOuter),
						yOut
						);
				seriesControlSurfaceTopView.add(
						xLELocalOuterChord + (localChordOuter*(1-outerChordRatio)),
						yOut
						);
				seriesControlSurfaceTopView.add(
						xLELocalInnerChord + (localChordInner*(1-innerChordRatio)),
						yIn
						);
			
				seriesControlSurfacesTopViewList.add(seriesControlSurfaceTopView);
			}
		}
		
		XYSeries seriesMeanAerodinamicChordView = new XYSeries("M.A.C.", false);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE),
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		seriesMeanAerodinamicChordView.add(
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeX().doubleValue(SI.METRE)
				+ Main.getTheAircraft().getCanard().getMeanAerodynamicChord().doubleValue(SI.METRE),
				Main.getTheAircraft().getCanard().getMeanAerodynamicChordLeadingEdgeY().doubleValue(SI.METRE)
				);
		
		double semispan = Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METER);
		double rootChord = Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().doubleValue(SI.METER);
		double tipChord = Main.getTheAircraft().getCanard().getPanels().get(0).getChordTip().doubleValue(SI.METER);
		double xLERoot = Main.getTheAircraft().getCanard().getDiscretizedXle().get(0).doubleValue(SI.METER);
		double xLETip = Main.getTheAircraft().getCanard().getDiscretizedXle().get(
				Main.getTheAircraft().getCanard().getDiscretizedXle().size()-1
				).doubleValue(SI.METER);
		double xTETip = xLETip + tipChord;
		
		double yMinTopView = -0.15*rootChord;
		double yMaxTopView = 1.15*xTETip;
		double xMinTopView = -((yMaxTopView - yMinTopView)/2 - semispan/2);
		double xMaxTopView = xMinTopView + yMaxTopView - yMinTopView;

		if ((yMaxTopView - yMinTopView) <= 1.1*semispan) {
			
			xMaxTopView = 1.05*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE);
			xMinTopView = -0.05*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE);
			yMinTopView = -0.55*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ 0.5*Main.getTheAircraft().getCanard().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
					+ ((xLETip - xLERoot)/4);
			yMaxTopView = 1.1*Main.getTheAircraft().getCanard().getSemiSpan().doubleValue(SI.METRE) 
					+ yMinTopView;
			
		}
		
		int WIDTH = 650;
		int HEIGHT = 650;
		
		//-------------------------------------------------------------------------------------
		// DATASET CRATION
		List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
		seriesAndColorList.add(Tuple.of(seriesMeanAerodinamicChordView, Color.BLACK));
		seriesControlSurfacesTopViewList.stream().forEach(
				flap -> seriesAndColorList.add(Tuple.of(flap, Color.decode("#32CD32")))
				);
		seriesAndColorList.add(Tuple.of(seriesCanardTopView, Color.decode("#228B22")));

		XYSeriesCollection dataset = new XYSeriesCollection();
		seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

		//-------------------------------------------------------------------------------------
		// CHART CRATION
		JFreeChart chart = ChartFactory.createXYAreaChart(
				"Canard Planform representation", 
				"y (m)", 
				"x (m)",
				(XYDataset) dataset,
				PlotOrientation.HORIZONTAL,
				false, // legend
				true,  // tooltips
				false  // urls
				);

		chart.setBackgroundPaint(Color.decode("#F5F5F5"));
		chart.setAntiAlias(true);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundAlpha(0.0f);
		plot.setBackgroundPaint(Color.decode("#F0F8FF"));
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domain.setRange(yMinTopView, yMaxTopView);
		domain.setInverted(Boolean.TRUE);
		NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
		range.setRange(xMinTopView, xMaxTopView);
		range.setInverted(Boolean.FALSE);

		XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
		xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
		xyAreaRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if(i==0)
				xyAreaRenderer.setSeriesVisible(i, false);
			else
				xyAreaRenderer.setSeriesPaint(
						i,
						seriesAndColorList.get(i)._2()
						);
		}
		XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
		xyLineAndShapeRenderer.setDefaultShapesVisible(false);
		xyLineAndShapeRenderer.setDefaultLinesVisible(true);
		xyLineAndShapeRenderer.setDefaultEntityRadius(6);
		for(int i=0; i<dataset.getSeries().size(); i++) {
			if (i==0) {
				xyLineAndShapeRenderer.setSeriesStroke(
						i,
						new BasicStroke(
								2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
								1.0f, new float[] {5.0f}, 1.0f));
			}
			else {
				xyLineAndShapeRenderer.setSeriesPaint(i, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						i, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}
		}
		
		plot.setRenderer(1, xyAreaRenderer);
		plot.setDataset(1, dataset);
		plot.setRenderer(0, xyLineAndShapeRenderer);
		plot.setDataset(0, dataset);

		//-------------------------------------------------------------------------------------
		// EXPORT TO SVG
		String outputFilePathTopView = Main.getOutputDirectoryPath() 
				+ File.separator 
				+ "CanardPlanform.svg";
		File outputFile = new File(outputFilePathTopView);
		if(outputFile.exists()) outputFile.delete();
		SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
		Rectangle r = new Rectangle(WIDTH, HEIGHT);
		chart.draw(g2, r);
		try {
			SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
		} catch (IOException e) {
			e.printStackTrace();
		}

		//-------------------------------------------------------------------------------------
		// PLOT TO PANE
		ChartCanvas canvas = new ChartCanvas(chart);
		StackPane stackPane = new StackPane(); 
		stackPane.getChildren().add(canvas);  

		// Bind canvas size to stack pane size. 
		canvas.widthProperty().bind(stackPane.widthProperty()); 
		canvas.heightProperty().bind(stackPane.heightProperty());  

		Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
		theController.getCanardPlanformPane().getChildren().clear();
		theController.getCanardPlanformPane().getChildren().add(sceneTopView.getRoot());
	}
	
	public void logCanardFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaCanardConsoleOutput().setText(
				Main.getTheAircraft().getCanard().toString()
				);

		if(Main.getTheAircraft().getCanard() != null) {

			//---------------------------------------------------------------------------------
			// ADJUST CRITERION CHOICE BOX:
			if(Main.getTheAircraft() != null)
				theController.getCanardAdjustCriterionChoiceBox().setDisable(false);
			
			//---------------------------------------------------------------------------------
			// ROUGHNESS:
			if(Main.getTheAircraft().getCanard().getRoughness() != null) {
				
				theController.getTextFieldCanardRoughness().setText(
						String.valueOf(
								Main.getTheAircraft()
								.getCanard()
								
								.getRoughness()
								.getEstimatedValue()
								)
						);
				
				if(Main.getTheAircraft()
						.getCanard()
						.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
					theController.getCanardRoughnessUnitChoiceBox().getSelectionModel().select(0);
				else if(Main.getTheAircraft()
						.getCanard()
						.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
					theController.getCanardRoughnessUnitChoiceBox().getSelectionModel().select(1);
				
			}
			else
				theController.getTextFieldCanardRoughness().setText(
						"NOT INITIALIZED"
						);
			
			//---------------------------------------------------------------------------------
			// PANELS NUMBER CHECK:
			if (Main.getTheAircraft().getCanard().getPanels().size() >= 
					theController.getTabPaneCanardPanels().getTabs().size()) {
				
				int iStart = theController.getTabPaneCanardPanels().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getCanard().getPanels().size(); i++)
					theController.addCanardPanel();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER PANELS:
			for (int i=0; i<Main.getTheAircraft().getCanard().getPanels().size(); i++) {
				
				LiftingSurfacePanelCreator currentPanel = Main.getTheAircraft().getCanard().getPanels().get(i);
				
				//---------------------------------------------------------------------------------
				// LINKED TO (from the second panel on):
				if(i>0)
					if(currentPanel.isLinked())
						theController.getCheckBoxCanardLinkedToPreviousPanelList().get(i-1).setSelected(true);
				
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
				else
					theController.getTextFieldCanardSpanPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldCanardSweepLEPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// DIHEDRAL:
				if(currentPanel.getDihedral() != null) {
					
					theController.getTextFieldCanardDihedralPanelList().get(i).setText(
							String.valueOf(currentPanel.getDihedral().getEstimatedValue())
							);
					
					if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardDihedralPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getDihedral().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardDihedralPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardDihedralPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldCanardInnerChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER TWIST (ROOT):
				if(currentPanel.getTwistGeometricRoot() != null) {
					
					theController.getTextFieldCanardInnerTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricRoot().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardInnerTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricRoot().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardInnerTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardInnerTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER AIRFOIL PATH (ROOT):
				if(currentPanel.getAirfoilRoot().getName() != null) {

					theController.getTextFieldCanardInnerAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getCanard().getPanels().get(i).getAirfoilRootPath()
							);
				}
				else
					theController.getTextFieldCanardInnerAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
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
				else
					theController.getTextFieldCanardOuterChordPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER TWIST (TIP):
				if(currentPanel.getTwistGeometricAtTip() != null) {
					
					theController.getTextFieldCanardOuterTwistPanelList().get(i).setText(
							String.valueOf(currentPanel.getTwistGeometricAtTip().getEstimatedValue())
							);
					
					if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("°")
							|| currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardOuterTwistPanelUnitList().get(i).getSelectionModel().select(0);
					else if(currentPanel.getTwistGeometricAtTip().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardOuterTwistPanelUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardOuterTwistPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER AIRFOIL PATH (TIP):
				if(currentPanel.getAirfoilTip().getName() != null) {

					theController.getTextFieldCanardOuterAirfoilPanelList().get(i).setText(
							Main.getTheAircraft().getCanard().getPanels().get(i).getAirfoilTipPath()
							);
				}
				else
					theController.getTextFieldCanardOuterAirfoilPanelList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
			
			//---------------------------------------------------------------------------------
			// ControlSurfaceS NUMBER CHECK:
			if (Main.getTheAircraft().getCanard().getSymmetricFlaps().size() >= 
					theController.getTabPaneCanardControlSurfaces().getTabs().size()) {
				
				int iStart = theController.getTabPaneCanardControlSurfaces().getTabs().size();
				
				for(int i=iStart; i<Main.getTheAircraft().getCanard().getSymmetricFlaps().size(); i++)
					theController.addControlSurface();
				
			}
			
			//---------------------------------------------------------------------------------
			// LOOP OVER ControlSurfaces:
			for (int i=0; i<Main.getTheAircraft().getCanard().getSymmetricFlaps().size(); i++) {
				
				SymmetricFlapCreator currentControlSurface = Main.getTheAircraft().getCanard().getSymmetricFlaps().get(i);
				
				//---------------------------------------------------------------------------------
				// TYPE:
				if(currentControlSurface.getType() != null) {
					if(currentControlSurface.getType().equals(FlapTypeEnum.PLAIN))
						theController.getChoiceBoxCanardControlSurfaceTypeList().get(i).getSelectionModel().select(0);
				}
				
				//---------------------------------------------------------------------------------
				// INNER POSITION:
				if(Double.valueOf(currentControlSurface.getInnerStationSpanwisePosition()) != null) {
					theController.getTextFieldCanardInnerPositionControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getInnerStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldCanardInnerPositionControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER POSITION:
				if(Double.valueOf(currentControlSurface.getOuterStationSpanwisePosition()) != null) {
					theController.getTextFieldCanardOuterPositionControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getOuterStationSpanwisePosition())
							);
				}
				else
					theController.getTextFieldCanardOuterPositionControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// INNER CHORD RATIO:
				if(Double.valueOf(currentControlSurface.getInnerChordRatio()) != null) {
					theController.getTextFieldCanardInnerChordRatioControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getInnerChordRatio())
							);
				}
				else
					theController.getTextFieldCanardInnerChordRatioControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// OUTER CHORD RATIO:
				if(Double.valueOf(currentControlSurface.getOuterChordRatio()) != null) {
					theController.getTextFieldCanardOuterChordRatioControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getOuterChordRatio())
							);
				}
				else
					theController.getTextFieldCanardOuterChordRatioControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MINIMUM DEFLECTION:
				if(currentControlSurface.getMinimumDeflection() != null) {
					
					theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getMinimumDeflection().getEstimatedValue())
							);
					
					if(currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(0);
					else if(currentControlSurface.getMinimumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardMinimumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(1);
					
				}
				else
					theController.getTextFieldCanardMinimumDeflectionAngleControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DEFLECTION:
				if(currentControlSurface.getMaximumDeflection() != null) {
					
					theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().get(i).setText(
							String.valueOf(currentControlSurface.getMaximumDeflection().getEstimatedValue())
							);
					
					if(currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("°")
							|| currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("deg"))
						theController.getChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(0);
					else if(currentControlSurface.getMaximumDeflection().getUnit().toString().equalsIgnoreCase("rad"))
						theController.getChoiceBoxCanardMaximumDeflectionAngleControlSurfaceUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldCanardMaximumDeflectionAngleControlSurfaceList().get(i).setText(
							"NOT INITIALIZED"
							);

			}
		}
	}

	public void createNacelleTopView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneNacellesTopViews().getTabs().size()) {
			
			int iStart = theController.getTabPaneNacellesTopViews().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane topView = new Pane();
				theController.getNacelleTopViewPaneList().add(topView);
				nacelleTab.setContent(topView);
				theController.getTabPaneNacellesTopViews().getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperY = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYRight().stream().forEach(y -> nacelleCurveUpperY.add(y));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerY = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getYCoordinatesOutlineXYLeft().stream().forEach(y -> nacelleCurveLowerY.add(y));

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleY = new ArrayList<>();

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j));
				dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(j));
			}

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1) );
				dataOutlineXZCurveNacelleY.add(nacelleCurveLowerY.get(nacelleCurveXPoints-j-1));
			}

			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0));
			dataOutlineXZCurveNacelleY.add(nacelleCurveUpperY.get(0));

			XYSeries seriesNacelleCruvesTopView = new XYSeries("Nacelle " + i + " XZ Curve - Top View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(k -> {
				seriesNacelleCruvesTopView.add(
						dataOutlineXZCurveNacelleX.get(k).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleY.get(k).doubleValue(SI.METER)
						);
			});

			double xMaxTopView = 1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinTopView = -1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxTopView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinTopView = -0.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			int WIDTH = 650;
			int HEIGHT = 650;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesTopView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Top View", 
					"y (m)", 
					"x (m)",
					(XYDataset) dataset,
					PlotOrientation.HORIZONTAL,
					false, // legend
					true,  // tooltips
					false  // urls
					);

			chart.setBackgroundPaint(Color.decode("#F5F5F5"));
			chart.setAntiAlias(true);

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundAlpha(0.0f);
			plot.setBackgroundPaint(Color.decode("#F0F8FF"));
			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.GRAY);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.GRAY);
			plot.setDomainPannable(true);
			plot.setRangePannable(true);

			NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
			domain.setRange(yMinTopView, yMaxTopView);
			domain.setInverted(Boolean.TRUE);
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(xMinTopView, xMaxTopView);
			range.setInverted(Boolean.FALSE);

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}

			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);

			//-------------------------------------------------------------------------------------
			// EXPORT TO SVG
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "Nacelle_" + (i+1) + "_TopView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists()) outputFile.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// PLOT TO PANE
			ChartCanvas canvas = new ChartCanvas(chart);
			StackPane stackPane = new StackPane(); 
			stackPane.getChildren().add(canvas);  

			// Bind canvas size to stack pane size. 
			canvas.widthProperty().bind(stackPane.widthProperty()); 
			canvas.heightProperty().bind(stackPane.heightProperty());  

			Scene sceneTopView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			theController.getNacelleTopViewPaneList().get(i).getChildren().clear();
			theController.getNacelleTopViewPaneList().get(i).getChildren().add(sceneTopView.getRoot());

		}
	}
	
	public void createNacelleSideView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneNacellesSideViews().getTabs().size()) {
			
			int iStart = theController.getTabPaneNacellesSideViews().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane sideView = new Pane();
				theController.getNacelleSideViewPaneList().add(sideView);
				nacelleTab.setContent(sideView);
				theController.getTabPaneNacellesSideViews().getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------

			// upper curve, sideview
			List<Amount<Length>> nacelleCurveX = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getXCoordinatesOutline().stream().forEach(x -> nacelleCurveX.add(x));
			int nacelleCurveXPoints = nacelleCurveX.size();
			List<Amount<Length>> nacelleCurveUpperZ = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZUpper().stream().forEach(z -> nacelleCurveUpperZ.add(z));

			// lower curve, sideview
			List<Amount<Length>> nacelleCurveLowerZ = new ArrayList<>(); 
			Main.getTheAircraft().getNacelles().getNacellesList().get(i).getZCoordinatesOutlineXZLower().stream().forEach(z -> nacelleCurveLowerZ.add(z));;

			List<Amount<Length>> dataOutlineXZCurveNacelleX = new ArrayList<>();
			List<Amount<Length>> dataOutlineXZCurveNacelleZ = new ArrayList<>();

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(j));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(j));
			}

			for(int j=0; j<nacelleCurveXPoints; j++) {
				dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(nacelleCurveXPoints-j-1));
				dataOutlineXZCurveNacelleZ.add(nacelleCurveLowerZ.get(nacelleCurveXPoints-j-1));
			}

			dataOutlineXZCurveNacelleX.add(nacelleCurveX.get(0));
			dataOutlineXZCurveNacelleZ.add(nacelleCurveUpperZ.get(0));

			XYSeries seriesNacelleCruvesSideView = new XYSeries("Nacelle " + i + " XY Curve - Side View", false);
			IntStream.range(0, dataOutlineXZCurveNacelleX.size())
			.forEach(j -> {
				seriesNacelleCruvesSideView.add(
						dataOutlineXZCurveNacelleZ.get(j).doubleValue(SI.METER),
						dataOutlineXZCurveNacelleX.get(j).doubleValue(SI.METER)
						);
			});

			double xMaxSideView = 1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double xMinSideView = -1.40*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().divide(2).doubleValue(SI.METRE);
			double yMaxSideView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);
			double yMinSideView = -0.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getLength().doubleValue(SI.METRE);

			int WIDTH = 650;
			int HEIGHT = 650;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesSideView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Side View", 
					"z (m)", 
					"x (m)",
					(XYDataset) dataset,
					PlotOrientation.HORIZONTAL,
					false, // legend
					true,  // tooltips
					false  // urls
					);

			chart.setBackgroundPaint(Color.decode("#F5F5F5"));
			chart.setAntiAlias(true);

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundAlpha(0.0f);
			plot.setBackgroundPaint(Color.decode("#F0F8FF"));
			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.GRAY);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.GRAY);
			plot.setDomainPannable(true);
			plot.setRangePannable(true);

			NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
			domain.setRange(xMinSideView, xMaxSideView);
			domain.setInverted(Boolean.FALSE);
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinSideView, yMaxSideView);
			range.setInverted(Boolean.FALSE);

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}

			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);

			//-------------------------------------------------------------------------------------
			// EXPORT TO SVG
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "Nacelle_" + (i+1) + "_SideView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists()) outputFile.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// PLOT TO PANE
			ChartCanvas canvas = new ChartCanvas(chart);
			StackPane stackPane = new StackPane(); 
			stackPane.getChildren().add(canvas);  

			// Bind canvas size to stack pane size. 
			canvas.widthProperty().bind(stackPane.widthProperty()); 
			canvas.heightProperty().bind(stackPane.heightProperty());  

			Scene sceneSideView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			theController.getNacelleSideViewPaneList().get(i).getChildren().clear();
			theController.getNacelleSideViewPaneList().get(i).getChildren().add(sceneSideView.getRoot());

		}
	}
	
	public void createNacelleFrontView() {

		//---------------------------------------------------------------------------------
		// NACELLES NUMBER CHECK:
		if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
				theController.getTabPaneNacellesFrontViews().getTabs().size()) {
			
			int iStart = theController.getTabPaneNacellesFrontViews().getTabs().size();
			
			for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {
				
				Tab nacelleTab = new Tab("Nacelle " + (i+1));
				Pane frontView = new Pane();
				theController.getNacelleFrontViewPaneList().add(frontView);
				nacelleTab.setContent(frontView);
				theController.getTabPaneNacellesFrontViews().getTabs().add(nacelleTab);
				
			}
		}
		
		//---------------------------------------------------------------------------------
		// LOOP OVER NACELLES:
		for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

			//--------------------------------------------------
			// get data vectors from nacelle discretization
			//--------------------------------------------------
			double[] angleArray = MyArrayUtils.linspace(0.0, 2*Math.PI, 100);
			double[] yCoordinate = new double[angleArray.length];
			double[] zCoordinate = new double[angleArray.length];

			double radius = Main.getTheAircraft().getNacelles()
					.getNacellesList().get(i)
					.getDiameterMax()
					.divide(2)
					.doubleValue(SI.METER);
			double y0 = 0.0;
			double z0 = 0.0;

			for(int j=0; j<angleArray.length; j++) {
				yCoordinate[j] = radius*Math.cos(angleArray[j]);
				zCoordinate[j] = radius*Math.sin(angleArray[j]);
			}

			XYSeries seriesNacelleCruvesFrontView = new XYSeries("Nacelle " + i + " - Front View", false);
			IntStream.range(0, yCoordinate.length)
			.forEach(j -> {
				seriesNacelleCruvesFrontView.add(
						yCoordinate[j] + y0,
						zCoordinate[j] + z0
						);
			});

			double yMaxFrontView = 1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double yMinFrontView = -1.20*Main.getTheAircraft().getNacelles().getNacellesList().get(i).getDiameterMax().doubleValue(SI.METRE);
			double xMaxFrontView = yMaxFrontView;
			double xMinFrontView = yMinFrontView;

			int WIDTH = 650;
			int HEIGHT = 650;

			//-------------------------------------------------------------------------------------
			// DATASET CRATION
			List<Tuple2<XYSeries, Color>> seriesAndColorList = new ArrayList<>();
			seriesAndColorList.add(Tuple.of(seriesNacelleCruvesFrontView, Color.decode("#FF7F50")));

			XYSeriesCollection dataset = new XYSeriesCollection();
			seriesAndColorList.stream().forEach(t -> dataset.addSeries(t._1()));

			//-------------------------------------------------------------------------------------
			// CHART CRATION
			JFreeChart chart = ChartFactory.createXYAreaChart(
					"Nacelle data representation - Front View", 
					"x (m)", 
					"z (m)",
					(XYDataset) dataset,
					PlotOrientation.HORIZONTAL,
					false, // legend
					true,  // tooltips
					false  // urls
					);

			chart.setBackgroundPaint(Color.decode("#F5F5F5"));
			chart.setAntiAlias(true);

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundAlpha(0.0f);
			plot.setBackgroundPaint(Color.decode("#F0F8FF"));
			plot.setDomainGridlinesVisible(true);
			plot.setDomainGridlinePaint(Color.GRAY);
			plot.setRangeGridlinesVisible(true);
			plot.setRangeGridlinePaint(Color.GRAY);
			plot.setDomainPannable(true);
			plot.setRangePannable(true);

			NumberAxis domain = (NumberAxis) chart.getXYPlot().getDomainAxis();
			domain.setRange(xMinFrontView, xMaxFrontView);
			domain.setInverted(Boolean.TRUE);
			NumberAxis range = (NumberAxis) chart.getXYPlot().getRangeAxis();
			range.setRange(yMinFrontView, yMaxFrontView);
			range.setInverted(Boolean.FALSE);

			XYAreaRenderer xyAreaRenderer = new XYAreaRenderer();
			xyAreaRenderer.setDefaultToolTipGenerator(new StandardXYToolTipGenerator());
			xyAreaRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyAreaRenderer.setSeriesPaint(
						j,
						seriesAndColorList.get(j)._2()
						);
			}
			XYLineAndShapeRenderer xyLineAndShapeRenderer = new XYLineAndShapeRenderer();
			xyLineAndShapeRenderer.setDefaultShapesVisible(false);
			xyLineAndShapeRenderer.setDefaultLinesVisible(true);
			xyLineAndShapeRenderer.setDefaultEntityRadius(6);
			for(int j=0; j<dataset.getSeries().size(); j++) {
				xyLineAndShapeRenderer.setSeriesPaint(j, Color.BLACK);
				xyLineAndShapeRenderer.setSeriesStroke(
						j, 
						new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND), 
						false
						);
			}

			plot.setRenderer(0, xyAreaRenderer);
			plot.setDataset(0, dataset);
			plot.setRenderer(1, xyLineAndShapeRenderer);
			plot.setDataset(1, dataset);

			//-------------------------------------------------------------------------------------
			// EXPORT TO SVG
			String outputFilePathTopView = Main.getOutputDirectoryPath() 
					+ File.separator 
					+ "Nacelle_" + (i+1) + "_FrontView.svg";
			File outputFile = new File(outputFilePathTopView);
			if(outputFile.exists()) outputFile.delete();
			SVGGraphics2D g2 = new SVGGraphics2D(WIDTH, HEIGHT);
			Rectangle r = new Rectangle(WIDTH, HEIGHT);
			chart.draw(g2, r);
			try {
				SVGUtils.writeToSVG(outputFile, g2.getSVGElement());
			} catch (IOException e) {
				e.printStackTrace();
			}

			//-------------------------------------------------------------------------------------
			// PLOT TO PANE
			ChartCanvas canvas = new ChartCanvas(chart);
			StackPane stackPane = new StackPane(); 
			stackPane.getChildren().add(canvas);  

			// Bind canvas size to stack pane size. 
			canvas.widthProperty().bind(stackPane.widthProperty()); 
			canvas.heightProperty().bind(stackPane.heightProperty());  

			Scene sceneFrontView = new Scene(stackPane, WIDTH+10, HEIGHT+10);
			theController.getNacelleFrontViewPaneList().get(i).getChildren().clear();
			theController.getNacelleFrontViewPaneList().get(i).getChildren().add(sceneFrontView.getRoot());

		}
	}
	
	public void logNacelleFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaNacelleConsoleOutput().setText(
				Main.getTheAircraft().getNacelles().toString()
				);

		if(Main.getTheAircraft().getNacelles() != null) {

			//---------------------------------------------------------------------------------
			// NACELLES NUMBER CHECK:
			if (Main.getTheAircraft().getNacelles().getNacellesList().size() >= 
					theController.getTabPaneNacelles().getTabs().size()) {

				int iStart = theController.getTabPaneNacelles().getTabs().size();

				for(int i=iStart; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++)
					theController.getInputManagerControllerUtilities().addNacelleImplementation();

			}

			//---------------------------------------------------------------------------------
			// LOOP OVER NACELLES:
			for (int i=0; i<Main.getTheAircraft().getNacelles().getNacellesList().size(); i++) {

				NacelleCreator currentNacelle = Main.getTheAircraft().getNacelles().getNacellesList().get(i);

				//---------------------------------------------------------------------------------
				// ROUGHNESS:
				if(currentNacelle.getRoughness() != null) {
					theController.getTextFieldNacelleRoughnessList().get(i).setText(
							String.valueOf(currentNacelle.getRoughness().getEstimatedValue()));

					if(currentNacelle.getRoughness().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxNacelleRoughnessUnitList().get(i).getSelectionModel().select(0);
					else if(currentNacelle.getRoughness().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxNacelleRoughnessUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldNacelleRoughnessList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// LENGTH:
				if(currentNacelle.getLength() != null) {
					theController.getTextFieldNacelleLengthList().get(i).setText(
							String.valueOf(currentNacelle.getLength().getEstimatedValue()));

					if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxNacelleLengthUnitList().get(i).getSelectionModel().select(0);
					else if(currentNacelle.getLength().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxNacelleLengthUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldNacelleLengthList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// MAXIMUM DIAMETER:
				if(currentNacelle.getDiameterMax() != null) {
					theController.getTextFieldNacelleMaximumDiameterList().get(i).setText(
							String.valueOf(currentNacelle.getDiameterMax().getEstimatedValue()));

					if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("m"))
						theController.getChoiceBoxNacelleMaximumDiameterUnitList().get(i).getSelectionModel().select(0);
					else if(currentNacelle.getDiameterMax().getUnit().toString().equalsIgnoreCase("ft"))
						theController.getChoiceBoxNacelleMaximumDiameterUnitList().get(i).getSelectionModel().select(1);

				}
				else
					theController.getTextFieldNacelleLengthList().get(i).setText(
							"NOT INITIALIZED"
							);

				//---------------------------------------------------------------------------------
				// K INLET:
				if(Double.valueOf(currentNacelle.getKInlet()) != null) {
					theController.getTextFieldNacelleKInletList().get(i).setText(
							String.valueOf(currentNacelle.getKInlet()));
				}
				else
					theController.getTextFieldNacelleKInletList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K OULET:
				if(Double.valueOf(currentNacelle.getKOutlet()) != null) {
					theController.getTextFieldNacelleKOutletList().get(i).setText(
							String.valueOf(currentNacelle.getKOutlet()));
				}
				else
					theController.getTextFieldNacelleKOutletList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K LENGTH:
				if(Double.valueOf(currentNacelle.getKLength()) != null) {
					theController.getTextFieldNacelleKLengthList().get(i).setText(
							String.valueOf(currentNacelle.getKLength()));
				}
				else
					theController.getTextFieldNacelleKLengthList().get(i).setText(
							"NOT INITIALIZED"
							);
				
				//---------------------------------------------------------------------------------
				// K DIAMETER OUTLET:
				if(Double.valueOf(currentNacelle.getKDiameterOutlet()) != null) {
					theController.getTextFieldNacelleKDiameterOutletList().get(i).setText(
							String.valueOf(currentNacelle.getKDiameterOutlet()));
				}
				else
					theController.getTextFieldNacelleKDiameterOutletList().get(i).setText(
							"NOT INITIALIZED"
							);
				
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void logPowerPlantFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaPowerPlantConsoleOutput().setText(
				Main.getTheAircraft().getPowerPlant().toString()
				);

		if(Main.getTheAircraft().getPowerPlant() != null) {

			//---------------------------------------------------------------------------------
			// ENGINES NUMBER CHECK:
			if (Main.getTheAircraft().getPowerPlant().getEngineList().size() >= 
					theController.getTabPaneEngines().getTabs().size()) {

				int iStart = theController.getTabPaneEngines().getTabs().size();

				for(int i=iStart; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++)
					theController.getInputManagerControllerUtilities().addEngineImplementation();

			}

			//---------------------------------------------------------------------------------
			// LOOP OVER ENGINES:
			for (int i=0; i<Main.getTheAircraft().getPowerPlant().getEngineList().size(); i++) {

				Engine currentEngine = Main.getTheAircraft().getPowerPlant().getEngineList().get(i);
				
				switch (currentEngine.getEngineType()) {
				case TURBOFAN:
					
					theController.getPowerPlantJetRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showTurbojetTurboFanDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC THRUST:
					if(currentEngine.getT0() != null) {
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getT0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("N"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("lbf"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE BPR:
					if(Double.valueOf(currentEngine.getBPR()) != null) 
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getBPR())
								);
					else
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				case TURBOJET:
					
					theController.getPowerPlantJetRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showTurbojetTurboFanDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEngineTurbojetTurbofanTypeChoiceBoxMap().get(i).getSelectionModel().select(1);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEngineTurbojetTurbofanDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbojetTurbofanLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEngineTurbojetTurbofanDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC THRUST:
					if(currentEngine.getT0() != null) {
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getT0().getEstimatedValue())
								);
						
						if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("N"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getT0().getUnit().toString().equalsIgnoreCase("lbf"))
							theController.getEngineTurbojetTurbofanStaticThrustUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbojetTurbofanStaticThrustTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE BPR:
					if(Double.valueOf(currentEngine.getBPR()) != null) 
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getBPR())
								);
					else
						theController.getEngineTurbojetTurbofanBPRTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfCompressorStagesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						theController.getEngineTurbojetTurbofanNumberOfShaftsTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						theController.getEngineTurbojetTurbofanOverallPressureRatioTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				case TURBOPROP:
					
					theController.getPowerPlantTurbopropRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showTurbopropDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEngineTurbopropTypeChoiceBoxMap().get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEngineTurbopropDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEngineTurbopropDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEngineTurbopropLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbopropLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbopropLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEngineTurbopropDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEngineTurbopropDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEngineTurbopropDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC POWER:
					if(currentEngine.getP0() != null) {
						theController.getEngineTurbopropStaticPowerTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getP0().getEstimatedValue())
								);
						
						if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("W"))
							theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("hp"))
							theController.getEngineTurbopropStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropStaticPowerTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER DIAMETER:
					if(currentEngine.getPropellerDiameter() != null) {
						theController.getEngineTurbopropPropellerDiameterTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getPropellerDiameter().getEstimatedValue())
								);
						
						if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEngineTurbopropPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEngineTurbopropPropellerDiameterTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF BLADES:
					if((Integer) currentEngine.getNumberOfBlades() != null) 
						theController.getEngineTurbopropNumberOfBladesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfBlades())
								);
					else
						theController.getEngineTurbopropNumberOfBladesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER EFFICIENCY:
					if((Double) currentEngine.getEtaPropeller() != null) 
						theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEtaPropeller())
								);
					else
						theController.getEngineTurbopropPropellerEfficiencyTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF COMPRESSOR STAGES:
					if((Integer) currentEngine.getNumberOfCompressorStages() != null) 
						theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfCompressorStages())
								);
					else
						theController.getEngineTurbopropNumberOfCompressorStagesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF SHAFTS:
					if((Integer) currentEngine.getNumberOfShafts() != null) 
						theController.getEngineTurbopropNumberOfShaftsTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfShafts())
								);
					else
						theController.getEngineTurbopropNumberOfShaftsTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE OVERALL PRESSURE RATIO:
					if((Double) currentEngine.getOverallPressureRatio() != null) 
						theController.getEngineTurbopropOverallPressureRatioTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getOverallPressureRatio())
								);
					else
						theController.getEngineTurbopropOverallPressureRatioTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
										
					break;
				case PISTON:
					
					theController.getPowerPlantPistonRadioButtonList().get(i).setSelected(true);
					theController.getInputManagerControllerUtilities().showPistonDataRadioButtonImplementation(i);
					
					//---------------------------------------------------------------------------------
					// ENGINE TYPE:
					if(currentEngine.getEngineType() != null) 
						theController.getEnginePistonTypeChoiceBoxMap().get(i).getSelectionModel().select(0);
					
					//---------------------------------------------------------------------------------
					// ENGINE DATABASE:
					if(currentEngine.getEngineDatabaseName() != null) {
						theController.getEnginePistonDatabaseTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEngineDatabaseName())
								);
					}
					else
						theController.getEnginePistonDatabaseTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE LENGTH:
					if(currentEngine.getLength() != null) {
						theController.getEnginePistonLengthTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getLength().getEstimatedValue())
								);
						
						if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEnginePistonLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getLength().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEnginePistonLengthUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonLengthTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE DRY MASS:
					if(currentEngine.getDryMassPublicDomain() != null) {
						theController.getEnginePistonDryMassTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getDryMassPublicDomain().getEstimatedValue())
								);
						
						if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("kg"))
							theController.getEnginePistonDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getDryMassPublicDomain().getUnit().toString().equalsIgnoreCase("lb"))
							theController.getEnginePistonDryMassUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonDryMassTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE STATIC POWER:
					if(currentEngine.getP0() != null) {
						theController.getEnginePistonStaticPowerTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getP0().getEstimatedValue())
								);
						
						if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("W"))
							theController.getEnginePistonStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getP0().getUnit().toString().equalsIgnoreCase("hp"))
							theController.getEnginePistonStaticPowerUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonStaticPowerTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER DIAMETER:
					if(currentEngine.getPropellerDiameter() != null) {
						theController.getEnginePistonPropellerDiameterTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getPropellerDiameter().getEstimatedValue())
								);
						
						if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("m"))
							theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(0);
						else if(currentEngine.getPropellerDiameter().getUnit().toString().equalsIgnoreCase("ft"))
							theController.getEnginePistonPropellerDiameterUnitChoiceBoxMap().get(i).getSelectionModel().select(1);
						
					}
					else
						theController.getEnginePistonPropellerDiameterTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE NUMBER OF BLADES:
					if((Integer) currentEngine.getNumberOfBlades() != null) 
						theController.getEnginePistonNumberOfBladesTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getNumberOfBlades())
								);
					else
						theController.getEnginePistonNumberOfBladesTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					//---------------------------------------------------------------------------------
					// ENGINE PROPELLER EFFICIENCY:
					if((Double) currentEngine.getEtaPropeller() != null) 
						theController.getEnginePistonPropellerEfficiencyTextFieldMap().get(i).setText(
								String.valueOf(currentEngine.getEtaPropeller())
								);
					else
						theController.getEnginePistonPropellerEfficiencyTextFieldMap().get(i).setText(
								"NOT INITIALIZED"
								);
					
					break;
				default:
					break;
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	public void logLandingGearsFromFileToInterface() {

		// print the toString method of the aircraft inside the text area of the GUI ...
		theController.getTextAreaLandingGearsConsoleOutput().setText(
				Main.getTheAircraft().getLandingGears().toString()
				);

		//---------------------------------------------------------------------------------
		// MAIN LEG LENGTH
		if(Main.getTheAircraft().getLandingGears().getMainLegsLenght() != null) {
			
			theController.getTextFieldLandingGearsMainLegLength().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getMainLegsLenght()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getMainLegsLenght()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getMainLegsLenght()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsMainLegLengthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsMainLegLength().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// DISTANCE BETWEEN WHEELS
		if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels() != null) {
			
			theController.getTextFieldLandingGearsDistanceBetweenWheels().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getDistanceBetweenWheels()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getDistanceBetweenWheels()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsDistanceBetweenWheelsUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsDistanceBetweenWheels().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NUMBER OF FRONTAL WHEELS 
		if((Integer) Main.getTheAircraft().getLandingGears().getNumberOfFrontalWheels() != null) {
			
			theController.getTextFieldLandingGearsNumberOfFrontalWheels().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getNumberOfFrontalWheels()
							)
					);
		}
		else
			theController.getTextFieldLandingGearsNumberOfFrontalWheels().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// NUMBER OF REAR WHEELS 
		if((Integer) Main.getTheAircraft().getLandingGears().getNumberOfRearWheels() != null) {
			
			theController.getTextFieldLandingGearsNumberOfRearWheels().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getNumberOfRearWheels()
							)
					);
		}
		else
			theController.getTextFieldLandingGearsNumberOfRearWheels().setText(
					"NOT INITIALIZED"
					);
		
		//---------------------------------------------------------------------------------
		// FRONTAL WHEELS HEIGHT
		if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight() != null) {
			
			theController.getTextFieldLandingGearsFrontalWheelsHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getFrontalWheelsHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getFrontalWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsFrontalWheelsHeigthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsFrontalWheelsHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// FRONTAL WHEELS WIDTH
		if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth() != null) {
			
			theController.getTextFieldLandingGearsFrontalWheelsWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getFrontalWheelsWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getFrontalWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsFrontalWheelsWidthUnitChoiceBox().getSelectionModel().select(1);

		}
		else
			theController.getTextFieldLandingGearsFrontalWheelsWidth().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Rear WHEELS HEIGHT
		if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight() != null) {

			theController.getTextFieldLandingGearsRearWheelsHeight().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getRearWheelsHeight()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getRearWheelsHeight()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsRearWheelsHeigthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsRearWheelsHeight().setText(
					"NOT INITIALIZED"
					);

		//---------------------------------------------------------------------------------
		// Rear WHEELS WIDTH
		if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth() != null) {
			
			theController.getTextFieldLandingGearsRearWheelsWidth().setText(
					String.valueOf(
							Main.getTheAircraft()
							.getLandingGears()
							.getRearWheelsWidth()
							.getEstimatedValue()
							)
					);
			
			if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("m"))
				theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().select(0);
			else if(Main.getTheAircraft().getLandingGears().getRearWheelsWidth()
					.getUnit().toString().equalsIgnoreCase("ft"))
				theController.getLandingGearsRearWheelsWidthUnitChoiceBox().getSelectionModel().select(1);
			
		}
		else
			theController.getTextFieldLandingGearsRearWheelsWidth().setText(
					"NOT INITIALIZED"
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
										.map(a -> a.getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getHTail() != null) 
								hTailAirfoilsName.addAll(
										Main.getTheAircraft().getHTail().getAirfoilList().stream()
										.map(a -> a.getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getVTail() != null) 
								vTailAirfoilsName.addAll(
										Main.getTheAircraft().getVTail().getAirfoilList().stream()
										.map(a -> a.getName() + ".xml")
										.collect(Collectors.toList())
										);
							if(Main.getTheAircraft().getCanard() != null) 
								canardAirfoilsName.addAll(
										Main.getTheAircraft().getCanard().getAirfoilList().stream()
										.map(a -> a.getName() + ".xml")
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
	
}

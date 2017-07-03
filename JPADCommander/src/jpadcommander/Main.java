/* 
 * References:
 * 
 *     https://www.youtube.com/channel/UClbVoA_U-OBFKx89y-TRofA/videos
 *     https://youtu.be/q5A-qW2eGKs
 *     
 */

package jpadcommander;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.StatusBar;

import aircraft.components.Aircraft;
import javafx.application.Application;
import javafx.concurrent.Worker.State;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import standaloneutils.JPADXmlReader;

public class Main extends Application {

	//-------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	//...........................................................................................
	// LAYOUTS:
	private static Stage primaryStage;
	private static BorderPane mainLayout;
	private static BorderPane mainInputManagerLayout;
	private static BorderPane mainInputManagerAircraftSubContentLayout;
	private static BorderPane mainInputManagerAircraftSubContentFieldsLayout;
	private static BorderPane mainInputManagerAircraftFromFileLayout;
	private static ToolBar mainInputManagerAircraftFromFileToolbarLayout;
	private static BorderPane mainInputManagerAircraftDefaultLayout;
	private static ToolBar mainInputManagerAircraftDefaultToolbarLayout;
	private static ToolBar actionButtonsToolbar;
	//...........................................................................................
	// FOLDER CONFIGURATION FIELDS:
	private static String inputDirectoryPath;
	private static String outputDirectoryPath;
	private static String databaseDirectoryPath;
	private static String inputFileAbsolutePath;
	private static Object choiseBoxSelectionDefaultAircraft;
	//...........................................................................................
	// STATUS BAR
	private static StatusBar statusBar;
	private static Button statusLight;
	private static State status;
	//...........................................................................................
	// ACTION BUTTONS
	private static Button setDataButton;
	private static Button updateAllButton;
	private static Button saveAircraftButton;
	//...........................................................................................
	// AIRCRAFT OBJECT
	private static Aircraft theAircraft;
	//...........................................................................................
	// AIRCRAFT TAB (INPUT):
	private static Boolean isAircraftFormFile;
	private static TextArea textAreaAircraftConsoleOutput;
	private static TextField textFieldAircraftInputFile;
	private static Button loadButtonFromFile;
	private static Button loadButtonDefaultAircraft;
	private static ChoiceBox<String> defaultAircraftChoiseBox;
	private static ChoiceBox<String> choiceBoxAircraftType;
	private static ChoiceBox<String> choiceBoxRegulationsType;
	private static TextField textFieldAircraftCabinConfiguration;
	private static TextField textFieldAircraftFuselageFile;
	private static TextField textFieldAircraftFuselageX;
	private static TextField textFieldAircraftFuselageY;
	private static TextField textFieldAircraftFuselageZ;
	private static TextField textFieldAircraftWingFile;
	private static TextField textFieldAircraftWingX;
	private static TextField textFieldAircraftWingY;
	private static TextField textFieldAircraftWingZ;
	private static TextField textFieldAircraftWingRiggingAngle;
	private static TextField textFieldAircraftHorizontalTailFile;
	private static TextField textFieldAircraftHorizontalTailX;
	private static TextField textFieldAircraftHorizontalTailY;
	private static TextField textFieldAircraftHorizontalTailZ;
	private static TextField textFieldAircraftHorizontalTailRiggingAngle;
	private static TextField textFieldAircraftVerticalTailFile;
	private static TextField textFieldAircraftVerticalTailX;
	private static TextField textFieldAircraftVerticalTailY;
	private static TextField textFieldAircraftVerticalTailZ;
	private static TextField textFieldAircraftVerticalTailRiggingAngle;
	private static TextField textFieldAircraftCanardFile;
	private static TextField textFieldAircraftCanardX;
	private static TextField textFieldAircraftCanardY;
	private static TextField textFieldAircraftCanardZ;
	private static TextField textFieldAircraftCanardRiggingAngle;
	private static List<TextField> textFieldAircraftEngineFileList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineXList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineYList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineZList = new ArrayList<>();
	private static List<ChoiceBox<String>> choiceBoxesAircraftEnginePositonList = new ArrayList<>();
	private static List<TextField> textFieldAircraftEngineTiltList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleFileList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleXList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleYList = new ArrayList<>();
	private static List<TextField> textFieldAircraftNacelleZList = new ArrayList<>();
	private static List<ChoiceBox<String>> choiceBoxesAircraftNacellePositonList = new ArrayList<>();
	private static TextField textFieldAircraftLandingGearsFile;
	private static TextField textFieldAircraftLandingGearsX;
	private static TextField textFieldAircraftLandingGearsY;
	private static TextField textFieldAircraftLandingGearsZ;
	private static ChoiceBox<String> choiceBoxAircraftLandingGearsPosition;
	private static TextField textFieldAircraftSystemsFile;
	private static TextField textFieldAircraftSystemsX;
	private static TextField textFieldAircraftSystemsY;
	private static TextField textFieldAircraftSystemsZ;
	private static Pane aircraftFrontViewPane;
	private static Pane aircraftSideViewPane;
	private static Pane aircraftTopViewPane;
	private static SplitPane aircraftViewsAndDataLogSplitPane;
	
	// units
	private static ChoiceBox<String> fuselageXUnitChoiceBox;
	private static ChoiceBox<String> fuselageYUnitChoiceBox;
	private static ChoiceBox<String> fuselageZUnitChoiceBox;
	private static ChoiceBox<String> wingXUnitChoiceBox;
	private static ChoiceBox<String> wingYUnitChoiceBox;
	private static ChoiceBox<String> wingZUnitChoiceBox;
	private static ChoiceBox<String> wingRiggingAngleUnitChoiceBox;
	private static ChoiceBox<String> hTailXUnitChoiceBox;
	private static ChoiceBox<String> hTailYUnitChoiceBox;
	private static ChoiceBox<String> htailZUnitChoiceBox;
	private static ChoiceBox<String> hTailTailRiggingAngleUnitChoiceBox;
	private static ChoiceBox<String> vTailXUnitChoiceBox;
	private static ChoiceBox<String> vTailYUnitChoiceBox;
	private static ChoiceBox<String> vTailZUnitChoiceBox;
	private static ChoiceBox<String> vTailRiggingAngleUnitChoiceBox;
	private static ChoiceBox<String> canardXUnitChoiceBox;
	private static ChoiceBox<String> canardYUnitChoiceBox;
	private static ChoiceBox<String> canardZUnitChoiceBox;
	private static ChoiceBox<String> canardRiggingAngleUnitChoiceBox;
	private static ChoiceBox<String> powerPlantXUnitChoiceBox;
	private static ChoiceBox<String> powerPlantYUnitChoiceBox;
	private static ChoiceBox<String> powerPlantZUnitChoiceBox;
	private static ChoiceBox<String> powerPlantTiltAngleUnitChoiceBox;
	private static ChoiceBox<String> nacelleXUnitChoiceBox;
	private static ChoiceBox<String> nacelleYUnitChoiceBox;
	private static ChoiceBox<String> nacelleZUnitChoiceBox;
	private static ChoiceBox<String> landingGearsXUnitChoiceBox;
	private static ChoiceBox<String> landingGearsYUnitChoiceBox;
	private static ChoiceBox<String> landingGearsZUnitChoiceBox;
	private static ChoiceBox<String> systemsXUnitChoiceBox;
	private static ChoiceBox<String> systemsYUnitChoiceBox;
	private static ChoiceBox<String> systemsZUnitChoiceBox;
	
	//...........................................................................................
	// FUSELAGE TAB (INPUT):
	private static ChoiceBox<String> fuselageAdjustCriterion;
	private static TextArea textAreaFuselageConsoleOutput;
	private static CheckBox fuselagePressurizedCheckBox;
	private static TextField textFieldFuselageDeckNumber;
	private static TextField textFieldFuselageLength;
	private static TextField textFieldFuselageSurfaceRoughness;
	private static TextField textFieldFuselageNoseLengthRatio;
	private static TextField textFieldFuselageNoseFinenessRatio;
	private static TextField textFieldFuselageNoseTipOffset;
	private static TextField textFieldFuselageNoseDxCap;
	private static ChoiceBox<String> choiceBoxFuselageNoseWindshieldType;
	private static TextField textFieldFuselageNoseWindshieldWidth;
	private static TextField textFieldFuselageNoseWindshieldHeight;
	private static TextField textFieldFuselageNoseMidSectionHeight;
	private static TextField textFieldFuselageNoseMidSectionRhoUpper;
	private static TextField textFieldFuselageNoseMidSectionRhoLower;
	private static TextField textFieldFuselageCylinderLengthRatio;
	private static TextField textFieldFuselageCylinderSectionWidth;
	private static TextField textFieldFuselageCylinderSectionHeight;
	private static TextField textFieldFuselageCylinderHeightFromGround;
	private static TextField textFieldFuselageCylinderSectionHeightRatio;
	private static TextField textFieldFuselageCylinderSectionRhoUpper;
	private static TextField textFieldFuselageCylinderSectionRhoLower;
	private static TextField textFieldFuselageTailTipOffset;
	private static TextField textFieldFuselageTailDxCap;
	private static TextField textFieldFuselageTailMidSectionHeight;
	private static TextField textFieldFuselageTailMidRhoUpper;
	private static TextField textFieldFuselageTailMidRhoLower;
	private static List<TextField> textFieldSpoilersXInboradList = new ArrayList<>();
	private static List<TextField> textFieldSpoilersXOutboradList = new ArrayList<>();
	
	// units
	private static ChoiceBox<String> fuselageLengthUnitChoiceBox;
	private static ChoiceBox<String> fuselageNoseTipOffsetZUnitChoiceBox;
	private static ChoiceBox<String> fuselageWindshieldWidthUnitChoiceBox;
	private static ChoiceBox<String> fuselageWindshieldHeightUnitChoiceBox;
	private static ChoiceBox<String> fuselageCylinderSectionWidthUnitChoiceBox;
	private static ChoiceBox<String> fuselageCylinderSectionHeightUnitChoiceBox;
	private static ChoiceBox<String> fuselageHeightFromGroundUnitChoiceBox;
	private static ChoiceBox<String> fuselageTailTipOffsetZUnitChoiceBox;
	private static ChoiceBox<String> fuselageSpoilersXinUnitChoiceBox;
	private static ChoiceBox<String> fuselageSpoilersXoutUnitChoiceBox;
	private static ChoiceBox<String> fuselageSpoilersYinUnitChoiceBox;
	private static ChoiceBox<String> fuselageSpoilersYoutUnitChoiceBox;
	private static ChoiceBox<String> fuselageSpoilersDeltaMinUnitChoiceBox;
	private static ChoiceBox<String> fuselageSpoilersDeltaMaxUnitChoiceBox;
	
	public static BorderPane getMainLayout() {
		return mainLayout;
	}

	public static void setMainLayout(BorderPane mainLayout) {
		Main.mainLayout = mainLayout;
	}

	public static ChoiceBox<String> getFuselageXUnitChoiceBox() {
		return fuselageXUnitChoiceBox;
	}

	public static void setFuselageXUnitChoiceBox(ChoiceBox<String> fuselageXUnitChoiceBox) {
		Main.fuselageXUnitChoiceBox = fuselageXUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageYUnitChoiceBox() {
		return fuselageYUnitChoiceBox;
	}

	public static void setFuselageYUnitChoiceBox(ChoiceBox<String> fuselageYUnitChoiceBox) {
		Main.fuselageYUnitChoiceBox = fuselageYUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageZUnitChoiceBox() {
		return fuselageZUnitChoiceBox;
	}

	public static void setFuselageZUnitChoiceBox(ChoiceBox<String> fuselageZUnitChoiceBox) {
		Main.fuselageZUnitChoiceBox = fuselageZUnitChoiceBox;
	}

	public static ChoiceBox<String> getWingXUnitChoiceBox() {
		return wingXUnitChoiceBox;
	}

	public static void setWingXUnitChoiceBox(ChoiceBox<String> wingXUnitChoiceBox) {
		Main.wingXUnitChoiceBox = wingXUnitChoiceBox;
	}

	public static ChoiceBox<String> getWingYUnitChoiceBox() {
		return wingYUnitChoiceBox;
	}

	public static void setWingYUnitChoiceBox(ChoiceBox<String> wingYUnitChoiceBox) {
		Main.wingYUnitChoiceBox = wingYUnitChoiceBox;
	}

	public static ChoiceBox<String> getWingZUnitChoiceBox() {
		return wingZUnitChoiceBox;
	}

	public static void setWingZUnitChoiceBox(ChoiceBox<String> wingZUnitChoiceBox) {
		Main.wingZUnitChoiceBox = wingZUnitChoiceBox;
	}

	public static ChoiceBox<String> getWingRiggingAngleUnitChoiceBox() {
		return wingRiggingAngleUnitChoiceBox;
	}

	public static void setWingRiggingAngleUnitChoiceBox(ChoiceBox<String> wingRiggingAngleUnitChoiceBox) {
		Main.wingRiggingAngleUnitChoiceBox = wingRiggingAngleUnitChoiceBox;
	}

	public static ChoiceBox<String> gethTailXUnitChoiceBox() {
		return hTailXUnitChoiceBox;
	}

	public static void sethTailXUnitChoiceBox(ChoiceBox<String> hTailXUnitChoiceBox) {
		Main.hTailXUnitChoiceBox = hTailXUnitChoiceBox;
	}

	public static ChoiceBox<String> gethTailYUnitChoiceBox() {
		return hTailYUnitChoiceBox;
	}

	public static void sethTailYUnitChoiceBox(ChoiceBox<String> hTailYUnitChoiceBox) {
		Main.hTailYUnitChoiceBox = hTailYUnitChoiceBox;
	}

	public static ChoiceBox<String> gethtailZUnitChoiceBox() {
		return htailZUnitChoiceBox;
	}

	public static void sethtailZUnitChoiceBox(ChoiceBox<String> htailZUnitChoiceBox) {
		Main.htailZUnitChoiceBox = htailZUnitChoiceBox;
	}

	public static ChoiceBox<String> getvTailXUnitChoiceBox() {
		return vTailXUnitChoiceBox;
	}

	public static void setvTailXUnitChoiceBox(ChoiceBox<String> vTailXUnitChoiceBox) {
		Main.vTailXUnitChoiceBox = vTailXUnitChoiceBox;
	}

	public static ChoiceBox<String> getvTailYUnitChoiceBox() {
		return vTailYUnitChoiceBox;
	}

	public static void setvTailYUnitChoiceBox(ChoiceBox<String> vTailYUnitChoiceBox) {
		Main.vTailYUnitChoiceBox = vTailYUnitChoiceBox;
	}

	public static ChoiceBox<String> getvTailZUnitChoiceBox() {
		return vTailZUnitChoiceBox;
	}

	public static void setvTailZUnitChoiceBox(ChoiceBox<String> vTailZUnitChoiceBox) {
		Main.vTailZUnitChoiceBox = vTailZUnitChoiceBox;
	}

	public static ChoiceBox<String> getvTailRiggingAngleUnitChoiceBox() {
		return vTailRiggingAngleUnitChoiceBox;
	}

	public static void setvTailRiggingAngleUnitChoiceBox(ChoiceBox<String> vTailRiggingAngleUnitChoiceBox) {
		Main.vTailRiggingAngleUnitChoiceBox = vTailRiggingAngleUnitChoiceBox;
	}

	public static ChoiceBox<String> getCanardXUnitChoiceBox() {
		return canardXUnitChoiceBox;
	}

	public static void setCanardXUnitChoiceBox(ChoiceBox<String> canardXUnitChoiceBox) {
		Main.canardXUnitChoiceBox = canardXUnitChoiceBox;
	}

	public static ChoiceBox<String> getCanardYUnitChoiceBox() {
		return canardYUnitChoiceBox;
	}

	public static void setCanardYUnitChoiceBox(ChoiceBox<String> canardYUnitChoiceBox) {
		Main.canardYUnitChoiceBox = canardYUnitChoiceBox;
	}

	public static ChoiceBox<String> getCanardZUnitChoiceBox() {
		return canardZUnitChoiceBox;
	}

	public static void setCanardZUnitChoiceBox(ChoiceBox<String> canardZUnitChoiceBox) {
		Main.canardZUnitChoiceBox = canardZUnitChoiceBox;
	}

	public static ChoiceBox<String> getCanardRiggingAngleUnitChoiceBox() {
		return canardRiggingAngleUnitChoiceBox;
	}

	public static void setCanardRiggingAngleUnitChoiceBox(ChoiceBox<String> canardRiggingAngleUnitChoiceBox) {
		Main.canardRiggingAngleUnitChoiceBox = canardRiggingAngleUnitChoiceBox;
	}

	public static ChoiceBox<String> getPowerPlantXUnitChoiceBox() {
		return powerPlantXUnitChoiceBox;
	}

	public static void setPowerPlantXUnitChoiceBox(ChoiceBox<String> powerPlantXUnitChoiceBox) {
		Main.powerPlantXUnitChoiceBox = powerPlantXUnitChoiceBox;
	}

	public static ChoiceBox<String> getPowerPlantYUnitChoiceBox() {
		return powerPlantYUnitChoiceBox;
	}

	public static void setPowerPlantYUnitChoiceBox(ChoiceBox<String> powerPlantYUnitChoiceBox) {
		Main.powerPlantYUnitChoiceBox = powerPlantYUnitChoiceBox;
	}

	public static ChoiceBox<String> getPowerPlantZUnitChoiceBox() {
		return powerPlantZUnitChoiceBox;
	}

	public static void setPowerPlantZUnitChoiceBox(ChoiceBox<String> powerPlantZUnitChoiceBox) {
		Main.powerPlantZUnitChoiceBox = powerPlantZUnitChoiceBox;
	}

	public static ChoiceBox<String> getPowerPlantTiltAngleUnitChoiceBox() {
		return powerPlantTiltAngleUnitChoiceBox;
	}

	public static void setPowerPlantTiltAngleUnitChoiceBox(ChoiceBox<String> powerPlantTiltAngleUnitChoiceBox) {
		Main.powerPlantTiltAngleUnitChoiceBox = powerPlantTiltAngleUnitChoiceBox;
	}

	public static ChoiceBox<String> getNacelleXUnitChoiceBox() {
		return nacelleXUnitChoiceBox;
	}

	public static void setNacelleXUnitChoiceBox(ChoiceBox<String> nacelleXUnitChoiceBox) {
		Main.nacelleXUnitChoiceBox = nacelleXUnitChoiceBox;
	}

	public static ChoiceBox<String> getNacelleYUnitChoiceBox() {
		return nacelleYUnitChoiceBox;
	}

	public static void setNacelleYUnitChoiceBox(ChoiceBox<String> nacelleYUnitChoiceBox) {
		Main.nacelleYUnitChoiceBox = nacelleYUnitChoiceBox;
	}

	public static ChoiceBox<String> getNacelleZUnitChoiceBox() {
		return nacelleZUnitChoiceBox;
	}

	public static void setNacelleZUnitChoiceBox(ChoiceBox<String> nacelleZUnitChoiceBox) {
		Main.nacelleZUnitChoiceBox = nacelleZUnitChoiceBox;
	}

	public static ChoiceBox<String> getLandingGearsXUnitChoiceBox() {
		return landingGearsXUnitChoiceBox;
	}

	public static void setLandingGearsXUnitChoiceBox(ChoiceBox<String> landingGearsXUnitChoiceBox) {
		Main.landingGearsXUnitChoiceBox = landingGearsXUnitChoiceBox;
	}

	public static ChoiceBox<String> getLandingGearsYUnitChoiceBox() {
		return landingGearsYUnitChoiceBox;
	}

	public static void setLandingGearsYUnitChoiceBox(ChoiceBox<String> landingGearsYUnitChoiceBox) {
		Main.landingGearsYUnitChoiceBox = landingGearsYUnitChoiceBox;
	}

	public static ChoiceBox<String> getLandingGearsZUnitChoiceBox() {
		return landingGearsZUnitChoiceBox;
	}

	public static void setLandingGearsZUnitChoiceBox(ChoiceBox<String> landingGearsZUnitChoiceBox) {
		Main.landingGearsZUnitChoiceBox = landingGearsZUnitChoiceBox;
	}

	public static ChoiceBox<String> getSystemsXUnitChoiceBox() {
		return systemsXUnitChoiceBox;
	}

	public static void setSystemsXUnitChoiceBox(ChoiceBox<String> systemsXUnitChoiceBox) {
		Main.systemsXUnitChoiceBox = systemsXUnitChoiceBox;
	}

	public static ChoiceBox<String> getSystemsYUnitChoiceBox() {
		return systemsYUnitChoiceBox;
	}

	public static void setSystemsYUnitChoiceBox(ChoiceBox<String> systemsYUnitChoiceBox) {
		Main.systemsYUnitChoiceBox = systemsYUnitChoiceBox;
	}

	public static ChoiceBox<String> getSystemsZUnitChoiceBox() {
		return systemsZUnitChoiceBox;
	}

	public static void setSystemsZUnitChoiceBox(ChoiceBox<String> systemsZUnitChoiceBox) {
		Main.systemsZUnitChoiceBox = systemsZUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageLengthUnitChoiceBox() {
		return fuselageLengthUnitChoiceBox;
	}

	public static void setFuselageLengthUnitChoiceBox(ChoiceBox<String> fuselageLengthUnitChoiceBox) {
		Main.fuselageLengthUnitChoiceBox = fuselageLengthUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageNoseTipOffsetZUnitChoiceBox() {
		return fuselageNoseTipOffsetZUnitChoiceBox;
	}

	public static void setFuselageNoseTipOffsetZUnitChoiceBox(ChoiceBox<String> fuselageNoseTipOffsetZUnitChoiceBox) {
		Main.fuselageNoseTipOffsetZUnitChoiceBox = fuselageNoseTipOffsetZUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageWindshieldWidthUnitChoiceBox() {
		return fuselageWindshieldWidthUnitChoiceBox;
	}

	public static void setFuselageWindshieldWidthUnitChoiceBox(ChoiceBox<String> fuselageWindshieldWidthUnitChoiceBox) {
		Main.fuselageWindshieldWidthUnitChoiceBox = fuselageWindshieldWidthUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageWindshieldHeightUnitChoiceBox() {
		return fuselageWindshieldHeightUnitChoiceBox;
	}

	public static void setFuselageWindshieldHeightUnitChoiceBox(ChoiceBox<String> fuselageWindshieldHeightUnitChoiceBox) {
		Main.fuselageWindshieldHeightUnitChoiceBox = fuselageWindshieldHeightUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageCylinderSectionWidthUnitChoiceBox() {
		return fuselageCylinderSectionWidthUnitChoiceBox;
	}

	public static void setFuselageCylinderSectionWidthUnitChoiceBox(
			ChoiceBox<String> fuselageCylinderSectionWidthUnitChoiceBox) {
		Main.fuselageCylinderSectionWidthUnitChoiceBox = fuselageCylinderSectionWidthUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageCylinderSectionHeightUnitChoiceBox() {
		return fuselageCylinderSectionHeightUnitChoiceBox;
	}

	public static void setFuselageCylinderSectionHeightUnitChoiceBox(
			ChoiceBox<String> fuselageCylinderSectionHeightUnitChoiceBox) {
		Main.fuselageCylinderSectionHeightUnitChoiceBox = fuselageCylinderSectionHeightUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageHeightFromGroundUnitChoiceBox() {
		return fuselageHeightFromGroundUnitChoiceBox;
	}

	public static void setFuselageHeightFromGroundUnitChoiceBox(ChoiceBox<String> fuselageHeightFromGroundUnitChoiceBox) {
		Main.fuselageHeightFromGroundUnitChoiceBox = fuselageHeightFromGroundUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageTailTipOffsetZUnitChoiceBox() {
		return fuselageTailTipOffsetZUnitChoiceBox;
	}

	public static void setFuselageTailTipOffsetZUnitChoiceBox(ChoiceBox<String> fuselageTailTipOffsetZUnitChoiceBox) {
		Main.fuselageTailTipOffsetZUnitChoiceBox = fuselageTailTipOffsetZUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageSpoilersXinUnitChoiceBox() {
		return fuselageSpoilersXinUnitChoiceBox;
	}

	public static void setFuselageSpoilersXinUnitChoiceBox(ChoiceBox<String> fuselageSpoilersXinUnitChoiceBox) {
		Main.fuselageSpoilersXinUnitChoiceBox = fuselageSpoilersXinUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageSpoilersXoutUnitChoiceBox() {
		return fuselageSpoilersXoutUnitChoiceBox;
	}

	public static void setFuselageSpoilersXoutUnitChoiceBox(ChoiceBox<String> fuselageSpoilersXoutUnitChoiceBox) {
		Main.fuselageSpoilersXoutUnitChoiceBox = fuselageSpoilersXoutUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageSpoilersYinUnitChoiceBox() {
		return fuselageSpoilersYinUnitChoiceBox;
	}

	public static void setFuselageSpoilersYinUnitChoiceBox(ChoiceBox<String> fuselageSpoilersYinUnitChoiceBox) {
		Main.fuselageSpoilersYinUnitChoiceBox = fuselageSpoilersYinUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageSpoilersYoutUnitChoiceBox() {
		return fuselageSpoilersYoutUnitChoiceBox;
	}

	public static void setFuselageSpoilersYoutUnitChoiceBox(ChoiceBox<String> fuselageSpoilersYoutUnitChoiceBox) {
		Main.fuselageSpoilersYoutUnitChoiceBox = fuselageSpoilersYoutUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageSpoilersDeltaMinUnitChoiceBox() {
		return fuselageSpoilersDeltaMinUnitChoiceBox;
	}

	public static void setFuselageSpoilersDeltaMinUnitChoiceBox(ChoiceBox<String> fuselageSpoilersDeltaMinUnitChoiceBox) {
		Main.fuselageSpoilersDeltaMinUnitChoiceBox = fuselageSpoilersDeltaMinUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageSpoilersDeltaMaxUnitChoiceBox() {
		return fuselageSpoilersDeltaMaxUnitChoiceBox;
	}

	public static void setFuselageSpoilersDeltaMaxUnitChoiceBox(ChoiceBox<String> fuselageSpoilersDeltaMaxUnitChoiceBox) {
		Main.fuselageSpoilersDeltaMaxUnitChoiceBox = fuselageSpoilersDeltaMaxUnitChoiceBox;
	}

	public static Object getChoiseBoxSelectionDefaultAircraft() {
		return choiseBoxSelectionDefaultAircraft;
	}

	public static Boolean getIsAircraftFormFile() {
		return isAircraftFormFile;
	}

	public static ChoiceBox<String> getDefaultAircraftChoiseBox() {
		return defaultAircraftChoiseBox;
	}

	public static void setPrimaryStage(Stage primaryStage) {
		Main.primaryStage = primaryStage;
	}

	public static void setMainInputManagerAircraftFromFileLayout(BorderPane mainInputManagerAircraftFromFileLayout) {
		Main.mainInputManagerAircraftFromFileLayout = mainInputManagerAircraftFromFileLayout;
	}

	public static void setMainInputManagerAircraftDefaultLayout(BorderPane mainInputManagerAircraftDefaultLayout) {
		Main.mainInputManagerAircraftDefaultLayout = mainInputManagerAircraftDefaultLayout;
	}

	private static List<TextField> textFieldSpoilersYInboradList = new ArrayList<>();
	private static List<TextField> textFieldSpoilersYOutboradList = new ArrayList<>();
	private static List<TextField> textFieldSpoilersMinDeflectionList = new ArrayList<>();
	private static List<TextField> textFieldSpoilersMaxDeflectionList = new ArrayList<>();
	private static Pane fuselageFrontViewPane;
	private static Pane fuselageSideViewPane;
	private static Pane fuselageTopViewPane;
	private static SplitPane fuselageViewsAndDataLogSplitPane;
	
	//-------------------------------------------------------------------------------------------
	// METHODS
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		
		
		Main.primaryStage = primaryStage;
		Main.primaryStage.setTitle("JPADCommander - UniNa - DAF");
		showMainView();
		Stage dialogConfig = new DialogConfig(primaryStage);
		dialogConfig.setTitle("JPADCommander Configuration - UniNa - DAF");
		dialogConfig.sizeToScene();
		dialogConfig.show();
		
		showMainItems();
		
	}
	
	public static boolean isAircraftFile(String pathToAircraftXML) {

		boolean isAircraftFile = false;
		
		final PrintStream originalOut = System.out;
		PrintStream filterStream = new PrintStream(new OutputStream() {
			public void write(int b) {
				// write nothing
			}
		});
		System.setOut(filterStream);

		String pathToXML = Main.getTextFieldAircraftInputFile().getText();
		if(pathToAircraftXML.endsWith(".xml")) {
			File inputFile = new File(pathToAircraftXML);
			if(inputFile.exists()) {
				JPADXmlReader reader = new JPADXmlReader(pathToXML);
				if(reader.getXmlDoc().getElementsByTagName("aircraft").getLength() > 0)
					isAircraftFile = true;
			}
		}
		// write again
		System.setOut(originalOut);
		
		return isAircraftFile;
	}
	
	public static void checkStatus(State status) {
		if(Main.getStatus().equals(State.RUNNING)) {
			Main.getStatusBar().setText("Running calculation ... "); 
			Main.getStatusLight().setBackground(
		        		new Background(
		        				new BackgroundFill(
		        						Color.ORANGE,
		        						new CornerRadii(2),
		        						new Insets(4)
		        						)
		        				)
		        		);
		}
		else if(Main.getStatus().equals(State.READY)) {
			Main.getStatusBar().setText("Ready");
			Main.getStatusLight().setBackground(
		        		new Background(
		        				new BackgroundFill(
		        						Color.LIGHTGREEN,
		        						new CornerRadii(2),
		        						new Insets(4)
		        						)
		        				)
		        		);
		}
	}
	
	private void showMainView() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainView.fxml"));
		Main.mainLayout = loader.load();
		Main.setStatusBar(new StatusBar());
		Main.setStatusLight(new Button());
        Main.getStatusLight().setPrefWidth(30.0);
        Main.getStatusBar().getRightItems().add(Main.getStatusLight());
        Main.setStatus(State.READY);
        checkStatus(Main.getStatus());
		Main.mainLayout.setBottom(Main.getStatusBar());
		Scene scene = new Scene(mainLayout);
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}

	public static void showMainItems() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("view/MainItems.fxml"));
		HBox mainItems = loader.load();
		mainLayout.setCenter(mainItems);
//		Image backgroundImage = new Image("file:images/Logo_DAF_Flat-Elevator.jpg");
//		ImageView imageView = new ImageView(backgroundImage);
//		mainItems.getChildren().add(imageView);
		primaryStage.show();
		
	}
	
	@SuppressWarnings("unchecked")
	public static void showInputManager() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManager.fxml"));
		mainInputManagerLayout = loader.load();

		Main.setFuselageAdjustCriterion(
				(ChoiceBox<String>) Main.getMainInputManagerLayout().lookup("#fuselageAdjustCriterionChoiceBox")
				);
		
		// get the action buttons toolbar
		Main.setActionButtonsToolbar(
				(ToolBar) Main.getMainInputManagerLayout()
				.lookup("#actionButtonsToolbar")
				);
		mainInputManagerLayout.setTop(actionButtonsToolbar);
		
		mainLayout.setCenter(mainInputManagerLayout);

		primaryStage.show();
		
//		if(Main.getTheAircraft() != null) {
//			
//			if(Main.isAircraftFormFile()) {
//				InputManagerAircraftFromFileController.logAircraftFromFileToInterface();
//			}
//			else {
//				InputManagerAircraftDefaultController.logAircraftDefaultToInterface();
//			}
//
//			InputManagerAircraftFromFileController.logFuselageFromFileToInterface();
//			// TODO: ADD OTHER WHEN AVAILABLE
//			
//			InputManagerAircraftFromFileController.createAircraftFrontView();
//			InputManagerAircraftFromFileController.createAircraftSideView();
//			InputManagerAircraftFromFileController.createAircraftTopView();
//			InputManagerAircraftFromFileController.createFuselageFrontView();
//			InputManagerAircraftFromFileController.createFuselageSideView();
//			InputManagerAircraftFromFileController.createFuselageTopView();
//			// TODO: ADD OTHER WHEN AVAILABLE
//			
//			
//		}
		
	}

	public static void showInputManagerAircraftFromFile() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManagerAircraftFromFile.fxml"));
		mainInputManagerAircraftFromFileLayout = loader.load();

		Main.setMainInputManagerAircraftFromFileToolbarLayout(
				(ToolBar) Main.getMainInputManagerAircraftFromFileLayout().lookup("#ImportAircraftFromFileToolbar")
				);
		mainInputManagerAircraftSubContentFieldsLayout.setTop(mainInputManagerAircraftFromFileToolbarLayout);
		
		primaryStage.show();
	}

	public static void showInputManagerAircraftDefault() throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Main.class.getResource("inputmanager/InputManagerAircraftDefault.fxml"));
		mainInputManagerAircraftDefaultLayout = loader.load();
		Main.setMainInputManagerAircraftDefaultToolbarLayout(
				(ToolBar) Main.getMainInputManagerAircraftDefaultLayout().lookup("#ImportAircraftDefaultToolbar")
				);
		mainInputManagerAircraftSubContentFieldsLayout.setTop(mainInputManagerAircraftDefaultToolbarLayout);
		primaryStage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	//-------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	public static TextField getTextFieldAircraftInputFile() {
		return textFieldAircraftInputFile;
	}

	public static BorderPane getMainInputManagerAircraftFromFileLayout() {
		return mainInputManagerAircraftFromFileLayout;
	}

	public static BorderPane getMainInputManagerAircraftDefaultLayout() {
		return mainInputManagerAircraftDefaultLayout;
	}
	
	public static void setTextFieldAircraftInputFile(TextField textFieldAircraftInputFile) {
		Main.textFieldAircraftInputFile = textFieldAircraftInputFile;
	}

	public static BorderPane getMainInputManagerLayout() {
		return mainInputManagerLayout;
	}

	public static void setMainInputManagerLayout(BorderPane mainInputManagerLayout) {
		Main.mainInputManagerLayout = mainInputManagerLayout;
	}

	public static BorderPane getMainInputManagerAircraftSubContentLayout() {
		return mainInputManagerAircraftSubContentLayout;
	}

	public static void setMainInputManagerAircraftSubContentLayout(BorderPane mainInputManagerAircraftSubContentLayout) {
		Main.mainInputManagerAircraftSubContentLayout = mainInputManagerAircraftSubContentLayout;
	}

	public static Stage getPrimaryStage() {
		return primaryStage;
	}

	public static String getInputDirectoryPath() {
		return inputDirectoryPath;
	}

	public static void setInputDirectoryPath(String inputDirectoryPath) {
		Main.inputDirectoryPath = inputDirectoryPath;
	}

	public static String getOutputDirectoryPath() {
		return outputDirectoryPath;
	}

	public static void setOutputDirectoryPath(String outputDirectoryPath) {
		Main.outputDirectoryPath = outputDirectoryPath;
	}

	public static String getDatabaseDirectoryPath() {
		return databaseDirectoryPath;
	}

	public static void setDatabaseDirectoryPath(String databaseDirectoryPath) {
		Main.databaseDirectoryPath = databaseDirectoryPath;
	}

	public static Aircraft getTheAircraft() {
		return theAircraft;
	}

	public static void setTheAircraft(Aircraft theAircraft) {
		Main.theAircraft = theAircraft;
	}
	
	public static TextField getTextFieldAircraftCabinConfiguration() {
		return textFieldAircraftCabinConfiguration;
	}

	public static void setTextFieldAircraftCabinConfiguration(TextField textFieldAircraftCabinConfiguration) {
		Main.textFieldAircraftCabinConfiguration = textFieldAircraftCabinConfiguration;
	}

	public static TextField getTextFieldAircraftFuselageFile() {
		return textFieldAircraftFuselageFile;
	}

	public static void setTextFieldAircraftFuselageFile(TextField textFieldAircraftFuselageFile) {
		Main.textFieldAircraftFuselageFile = textFieldAircraftFuselageFile;
	}

	public static TextField getTextFieldAircraftFuselageX() {
		return textFieldAircraftFuselageX;
	}

	public static void setTextFieldAircraftFuselageX(TextField textFieldAircraftFuselageX) {
		Main.textFieldAircraftFuselageX = textFieldAircraftFuselageX;
	}

	public static TextField getTextFieldAircraftFuselageY() {
		return textFieldAircraftFuselageY;
	}

	public static void setTextFieldAircraftFuselageY(TextField textFieldAircraftFuselageY) {
		Main.textFieldAircraftFuselageY = textFieldAircraftFuselageY;
	}

	public static TextField getTextFieldAircraftFuselageZ() {
		return textFieldAircraftFuselageZ;
	}

	public static void setTextFieldAircraftFuselageZ(TextField textFieldAircraftFuselageZ) {
		Main.textFieldAircraftFuselageZ = textFieldAircraftFuselageZ;
	}

	public static TextField getTextFieldAircraftWingFile() {
		return textFieldAircraftWingFile;
	}

	public static void setTextFieldAircraftWingFile(TextField textFieldAircraftWingFile) {
		Main.textFieldAircraftWingFile = textFieldAircraftWingFile;
	}

	public static TextField getTextFieldAircraftWingX() {
		return textFieldAircraftWingX;
	}

	public static void setTextFieldAircraftWingX(TextField textFieldAircraftWingX) {
		Main.textFieldAircraftWingX = textFieldAircraftWingX;
	}

	public static TextField getTextFieldAircraftWingY() {
		return textFieldAircraftWingY;
	}

	public static void setTextFieldAircraftWingY(TextField textFieldAircraftWingY) {
		Main.textFieldAircraftWingY = textFieldAircraftWingY;
	}

	public static TextField getTextFieldAircraftWingZ() {
		return textFieldAircraftWingZ;
	}

	public static void setTextFieldAircraftWingZ(TextField textFieldAircraftWingZ) {
		Main.textFieldAircraftWingZ = textFieldAircraftWingZ;
	}

	public static TextField getTextFieldAircraftWingRiggingAngle() {
		return textFieldAircraftWingRiggingAngle;
	}

	public static void setTextFieldAircraftWingRiggingAngle(TextField textFieldAircraftWingRiggingAngle) {
		Main.textFieldAircraftWingRiggingAngle = textFieldAircraftWingRiggingAngle;
	}

	public static TextField getTextFieldAircraftHorizontalTailFile() {
		return textFieldAircraftHorizontalTailFile;
	}

	public static void setTextFieldAircraftHorizontalTailFile(TextField textFieldAircraftHorizontalTailFile) {
		Main.textFieldAircraftHorizontalTailFile = textFieldAircraftHorizontalTailFile;
	}

	public static TextField getTextFieldAircraftHorizontalTailX() {
		return textFieldAircraftHorizontalTailX;
	}

	public static void setTextFieldAircraftHorizontalTailX(TextField textFieldAircraftHorizontalTailX) {
		Main.textFieldAircraftHorizontalTailX = textFieldAircraftHorizontalTailX;
	}

	public static TextField getTextFieldAircraftHorizontalTailY() {
		return textFieldAircraftHorizontalTailY;
	}

	public static void setTextFieldAircraftHorizontalTailY(TextField textFieldAircraftHorizontalTailY) {
		Main.textFieldAircraftHorizontalTailY = textFieldAircraftHorizontalTailY;
	}

	public static TextField getTextFieldAircraftHorizontalTailZ() {
		return textFieldAircraftHorizontalTailZ;
	}

	public static void setTextFieldAircraftHorizontalTailZ(TextField textFieldAircraftHorizontalTailZ) {
		Main.textFieldAircraftHorizontalTailZ = textFieldAircraftHorizontalTailZ;
	}

	public static TextField getTextFieldAircraftHorizontalTailRiggingAngle() {
		return textFieldAircraftHorizontalTailRiggingAngle;
	}

	public static void setTextFieldAircraftHorizontalTailRiggingAngle(
			TextField textFieldAircraftHorizontalTailRiggingAngle) {
		Main.textFieldAircraftHorizontalTailRiggingAngle = textFieldAircraftHorizontalTailRiggingAngle;
	}

	public static TextField getTextFieldAircraftVerticalTailFile() {
		return textFieldAircraftVerticalTailFile;
	}

	public static void setTextFieldAircraftVerticalTailFile(TextField textFieldAircraftVerticalTailFile) {
		Main.textFieldAircraftVerticalTailFile = textFieldAircraftVerticalTailFile;
	}

	public static TextField getTextFieldAircraftVerticalTailX() {
		return textFieldAircraftVerticalTailX;
	}

	public static void setTextFieldAircraftVerticalTailX(TextField textFieldAircraftVerticalTailX) {
		Main.textFieldAircraftVerticalTailX = textFieldAircraftVerticalTailX;
	}

	public static TextField getTextFieldAircraftVerticalTailY() {
		return textFieldAircraftVerticalTailY;
	}

	public static void setTextFieldAircraftVerticalTailY(TextField textFieldAircraftVerticalTailY) {
		Main.textFieldAircraftVerticalTailY = textFieldAircraftVerticalTailY;
	}

	public static TextField getTextFieldAircraftVerticalTailZ() {
		return textFieldAircraftVerticalTailZ;
	}

	public static void setTextFieldAircraftVerticalTailZ(TextField textFieldAircraftVerticalTailZ) {
		Main.textFieldAircraftVerticalTailZ = textFieldAircraftVerticalTailZ;
	}

	public static TextField getTextFieldAircraftVerticalTailRiggingAngle() {
		return textFieldAircraftVerticalTailRiggingAngle;
	}

	public static void setTextFieldAircraftVerticalTailRiggingAngle(TextField textFieldAircraftVerticalTailRiggingAngle) {
		Main.textFieldAircraftVerticalTailRiggingAngle = textFieldAircraftVerticalTailRiggingAngle;
	}

	public static TextField getTextFieldAircraftCanardFile() {
		return textFieldAircraftCanardFile;
	}

	public static void setTextFieldAircraftCanardFile(TextField textFieldAircraftCanardFile) {
		Main.textFieldAircraftCanardFile = textFieldAircraftCanardFile;
	}

	public static TextField getTextFieldAircraftCanardX() {
		return textFieldAircraftCanardX;
	}

	public static void setTextFieldAircraftCanardX(TextField textFieldAircraftCanardX) {
		Main.textFieldAircraftCanardX = textFieldAircraftCanardX;
	}

	public static TextField getTextFieldAircraftCanardY() {
		return textFieldAircraftCanardY;
	}

	public static void setTextFieldAircraftCanardY(TextField textFieldAircraftCanardY) {
		Main.textFieldAircraftCanardY = textFieldAircraftCanardY;
	}

	public static TextField getTextFieldAircraftCanardZ() {
		return textFieldAircraftCanardZ;
	}

	public static void setTextFieldAircraftCanardZ(TextField textFieldAircraftCanardZ) {
		Main.textFieldAircraftCanardZ = textFieldAircraftCanardZ;
	}

	public static TextField getTextFieldAircraftCanardRiggingAngle() {
		return textFieldAircraftCanardRiggingAngle;
	}

	public static void setTextFieldAircraftCanardRiggingAngle(TextField textFieldAircraftCanardRiggingAngle) {
		Main.textFieldAircraftCanardRiggingAngle = textFieldAircraftCanardRiggingAngle;
	}

	public static List<TextField> getTextFieldAircraftEngineFileList() {
		return textFieldAircraftEngineFileList;
	}

	public static void setTextFieldAircraftEngineFileList(List<TextField> textFieldAircraftEngineFileList) {
		Main.textFieldAircraftEngineFileList = textFieldAircraftEngineFileList;
	}

	public static List<TextField> getTextFieldAircraftEngineXList() {
		return textFieldAircraftEngineXList;
	}

	public static void setTextFieldAircraftEngineXList(List<TextField> textFieldAircraftEngineXList) {
		Main.textFieldAircraftEngineXList = textFieldAircraftEngineXList;
	}

	public static List<TextField> getTextFieldAircraftEngineYList() {
		return textFieldAircraftEngineYList;
	}

	public static void setTextFieldAircraftEngineYList(List<TextField> textFieldAircraftEngineYList) {
		Main.textFieldAircraftEngineYList = textFieldAircraftEngineYList;
	}

	public static List<TextField> getTextFieldAircraftEngineZList() {
		return textFieldAircraftEngineZList;
	}

	public static void setTextFieldAircraftEngineZList(List<TextField> textFieldAircraftEngineZList) {
		Main.textFieldAircraftEngineZList = textFieldAircraftEngineZList;
	}

	public static List<ChoiceBox<String>> getChoiceBoxesAircraftEnginePositonList() {
		return choiceBoxesAircraftEnginePositonList;
	}

	public static void setChoiceBoxesAircraftEnginePositonList(List<ChoiceBox<String>> textFieldAircraftEnginePositonList) {
		Main.choiceBoxesAircraftEnginePositonList = textFieldAircraftEnginePositonList;
	}

	public static List<TextField> getTextFieldAircraftEngineTiltList() {
		return textFieldAircraftEngineTiltList;
	}

	public static void setTextFieldAircraftEngineTiltList(List<TextField> textFieldAircraftEngineTiltList) {
		Main.textFieldAircraftEngineTiltList = textFieldAircraftEngineTiltList;
	}

	public static List<TextField> getTextFieldAircraftNacelleFileList() {
		return textFieldAircraftNacelleFileList;
	}

	public static void setTextFieldAircraftNacelleFileList(List<TextField> textFieldAircraftNacelleFileList) {
		Main.textFieldAircraftNacelleFileList = textFieldAircraftNacelleFileList;
	}

	public static List<TextField> getTextFieldAircraftNacelleXList() {
		return textFieldAircraftNacelleXList;
	}

	public static void setTextFieldAircraftNacelleXList(List<TextField> textFieldAircraftNacelleXList) {
		Main.textFieldAircraftNacelleXList = textFieldAircraftNacelleXList;
	}

	public static List<TextField> getTextFieldAircraftNacelleYList() {
		return textFieldAircraftNacelleYList;
	}

	public static void setTextFieldAircraftNacelleYList(List<TextField> textFieldAircraftNacelleYList) {
		Main.textFieldAircraftNacelleYList = textFieldAircraftNacelleYList;
	}

	public static List<TextField> getTextFieldAircraftNacelleZList() {
		return textFieldAircraftNacelleZList;
	}

	public static void setTextFieldAircraftNacelleZList(List<TextField> textFieldAircraftNacelleZList) {
		Main.textFieldAircraftNacelleZList = textFieldAircraftNacelleZList;
	}

	public static List<ChoiceBox<String>> getChoiceBoxesAircraftNacellePositonList() {
		return choiceBoxesAircraftNacellePositonList;
	}

	public static void setChoiceBoxesAircraftNacellePositonList(List<ChoiceBox<String>> textFieldAircraftNacellePositonList) {
		Main.choiceBoxesAircraftNacellePositonList = textFieldAircraftNacellePositonList;
	}

	public static TextField getTextFieldAircraftLandingGearsFile() {
		return textFieldAircraftLandingGearsFile;
	}

	public static void setTextFieldAircraftLandingGearsFile(TextField textFieldAircraftLandingGearsFile) {
		Main.textFieldAircraftLandingGearsFile = textFieldAircraftLandingGearsFile;
	}

	public static TextField getTextFieldAircraftLandingGearsX() {
		return textFieldAircraftLandingGearsX;
	}

	public static void setTextFieldAircraftLandingGearsX(TextField textFieldAircraftLandingGearsX) {
		Main.textFieldAircraftLandingGearsX = textFieldAircraftLandingGearsX;
	}

	public static TextField getTextFieldAircraftLandingGearsY() {
		return textFieldAircraftLandingGearsY;
	}

	public static void setTextFieldAircraftLandingGearsY(TextField textFieldAircraftLandingGearsY) {
		Main.textFieldAircraftLandingGearsY = textFieldAircraftLandingGearsY;
	}

	public static TextField getTextFieldAircraftLandingGearsZ() {
		return textFieldAircraftLandingGearsZ;
	}

	public static void setTextFieldAircraftLandingGearsZ(TextField textFieldAircraftLandingGearsZ) {
		Main.textFieldAircraftLandingGearsZ = textFieldAircraftLandingGearsZ;
	}

	public static ChoiceBox<String> getChoiceBoxAircraftLandingGearsPosition() {
		return choiceBoxAircraftLandingGearsPosition;
	}

	public static void setChoiceBoxAircraftLandingGearsPosition(ChoiceBox<String> textFieldAircraftLandingGearsPosition) {
		Main.choiceBoxAircraftLandingGearsPosition = textFieldAircraftLandingGearsPosition;
	}

	public static TextField getTextFieldAircraftSystemsFile() {
		return textFieldAircraftSystemsFile;
	}

	public static void setTextFieldAircraftSystemsFile(TextField textFieldAircraftSystemsFile) {
		Main.textFieldAircraftSystemsFile = textFieldAircraftSystemsFile;
	}

	public static TextField getTextFieldAircraftSystemsX() {
		return textFieldAircraftSystemsX;
	}

	public static void setTextFieldAircraftSystemsX(TextField textFieldAircraftSystemsX) {
		Main.textFieldAircraftSystemsX = textFieldAircraftSystemsX;
	}

	public static TextField getTextFieldAircraftSystemsY() {
		return textFieldAircraftSystemsY;
	}

	public static void setTextFieldAircraftSystemsY(TextField textFieldAircraftSystemsY) {
		Main.textFieldAircraftSystemsY = textFieldAircraftSystemsY;
	}

	public static TextField getTextFieldAircraftSystemsZ() {
		return textFieldAircraftSystemsZ;
	}

	public static void setTextFieldAircraftSystemsZ(TextField textFieldAircraftSystemsZ) {
		Main.textFieldAircraftSystemsZ = textFieldAircraftSystemsZ;
	}

	public static TextArea getTextAreaAircraftConsoleOutput() {
		return textAreaAircraftConsoleOutput;
	}

	public static void setTextAreaAircraftConsoleOutput(TextArea textAreaAircraftConsoleOutput) {
		Main.textAreaAircraftConsoleOutput = textAreaAircraftConsoleOutput;
	}

	public static StatusBar getStatusBar() {
		return statusBar;
	}

	public static void setStatusBar(StatusBar statusBar) {
		Main.statusBar = statusBar;
	}

	public static State getStatus() {
		return status;
	}

	public static void setStatus(State status) {
		Main.status = status;
	}

	public static Button getStatusLight() {
		return statusLight;
	}

	public static void setStatusLight(Button statusLight) {
		Main.statusLight = statusLight;
	}

	public static BorderPane getMainInputManagerAircraftSubContentFieldsLayout() {
		return mainInputManagerAircraftSubContentFieldsLayout;
	}

	public static void setMainInputManagerAircraftSubContentFieldsLayout(
			BorderPane mainInputManagerAircraftSubContentFieldsLayout) {
		Main.mainInputManagerAircraftSubContentFieldsLayout = mainInputManagerAircraftSubContentFieldsLayout;
	}

	public static ToolBar getMainInputManagerAircraftFromFileToolbarLayout() {
		return mainInputManagerAircraftFromFileToolbarLayout;
	}

	public static void setMainInputManagerAircraftFromFileToolbarLayout(
			ToolBar mainInputManagerAircraftFromFileToolbarLayout) {
		Main.mainInputManagerAircraftFromFileToolbarLayout = mainInputManagerAircraftFromFileToolbarLayout;
	}

	public static ToolBar getMainInputManagerAircraftDefaultToolbarLayout() {
		return mainInputManagerAircraftDefaultToolbarLayout;
	}

	public static void setMainInputManagerAircraftDefaultToolbarLayout(
			ToolBar mainInputManagerAircraftDefaultToolbarLayout) {
		Main.mainInputManagerAircraftDefaultToolbarLayout = mainInputManagerAircraftDefaultToolbarLayout;
	}

	public static Pane getAircraftFrontViewPane() {
		return aircraftFrontViewPane;
	}

	public static void setAircraftFrontViewPane(Pane aircraftFrontViewPane) {
		Main.aircraftFrontViewPane = aircraftFrontViewPane;
	}

	public static Pane getAircraftSideViewPane() {
		return aircraftSideViewPane;
	}

	public static void setAircraftSideViewPane(Pane aircraftSideViewPane) {
		Main.aircraftSideViewPane = aircraftSideViewPane;
	}

	public static Pane getAircraftTopViewPane() {
		return aircraftTopViewPane;
	}

	public static void setAircraftTopViewPane(Pane aircraftTopViewPane) {
		Main.aircraftTopViewPane = aircraftTopViewPane;
	}

	public static Button getLoadButtonFromFile() {
		return loadButtonFromFile;
	}

	public static void setLoadButtonFromFile(Button loadButtonFromFile) {
		Main.loadButtonFromFile = loadButtonFromFile;
	}

	public static Button getLoadButtonDefaultAircraft() {
		return loadButtonDefaultAircraft;
	}

	public static void setLoadButtonDefaultAircraft(Button loadButtonDefaultAircraft) {
		Main.loadButtonDefaultAircraft = loadButtonDefaultAircraft;
	}

	public static ChoiceBox<String> getDefaultAircraftChoiceBox() {
		return defaultAircraftChoiseBox;
	}

	public static void setDefaultAircraftChoiseBox(ChoiceBox<String> defaultAircraftChoiseBox) {
		Main.defaultAircraftChoiseBox = defaultAircraftChoiseBox;
	}

	public static String getInputFileAbsolutePath() {
		return inputFileAbsolutePath;
	}

	public static void setInputFileAbsolutePath(String inputFileAbsolutePath) {
		Main.inputFileAbsolutePath = inputFileAbsolutePath;
	}

	public static Object getChoiceBoxSelectionDefaultAircraft() {
		return choiseBoxSelectionDefaultAircraft;
	}

	public static void setChoiseBoxSelectionDefaultAircraft(Object choiseBoxSelectionDefaultAircraft) {
		Main.choiseBoxSelectionDefaultAircraft = choiseBoxSelectionDefaultAircraft;
	}

	public static ChoiceBox<String> getChoiceBoxAircraftType() {
		return choiceBoxAircraftType;
	}

	public static void setChoiceBoxAircraftType(ChoiceBox<String> choiceBoxAircraftType) {
		Main.choiceBoxAircraftType = choiceBoxAircraftType;
	}

	public static ChoiceBox<String> getChoiceBoxRegulationsType() {
		return choiceBoxRegulationsType;
	}

	public static void setChoiceBoxRegulationsType(ChoiceBox<String> choiceBoxRegulationsType) {
		Main.choiceBoxRegulationsType = choiceBoxRegulationsType;
	}

	public static SplitPane getAircraftViewsAndDataLogSplitPane() {
		return aircraftViewsAndDataLogSplitPane;
	}

	public static void setAircraftViewsAndDataLogSplitPane(SplitPane aircraftViewsAndDataLogSplitPane) {
		Main.aircraftViewsAndDataLogSplitPane = aircraftViewsAndDataLogSplitPane;
	}

	public static CheckBox getFuselagePressurizedCheckBox() {
		return fuselagePressurizedCheckBox;
	}

	public static void setFuselagePressurizedCheckBox(CheckBox fuselagePressurizedCheckBox) {
		Main.fuselagePressurizedCheckBox = fuselagePressurizedCheckBox;
	}

	public static TextField getTextFieldFuselageDeckNumber() {
		return textFieldFuselageDeckNumber;
	}

	public static void setTextFieldFuselageDeckNumber(TextField textFieldFuselageDeckNumber) {
		Main.textFieldFuselageDeckNumber = textFieldFuselageDeckNumber;
	}

	public static TextField getTextFieldFuselageLength() {
		return textFieldFuselageLength;
	}

	public static void setTextFieldFuselageLength(TextField textFieldFuselageLength) {
		Main.textFieldFuselageLength = textFieldFuselageLength;
	}

	public static TextField getTextFieldFuselageSurfaceRoughness() {
		return textFieldFuselageSurfaceRoughness;
	}

	public static void setTextFieldFuselageSurfaceRoughness(TextField textFieldSurfaceRoughness) {
		Main.textFieldFuselageSurfaceRoughness = textFieldSurfaceRoughness;
	}

	public static TextField getTextFieldFuselageNoseLengthRatio() {
		return textFieldFuselageNoseLengthRatio;
	}

	public static void setTextFieldFuselageNoseLengthRatio(TextField textFieldFuselageNoseLengthRatio) {
		Main.textFieldFuselageNoseLengthRatio = textFieldFuselageNoseLengthRatio;
	}

	public static TextField getTextFieldFuselageNoseFinenessRatio() {
		return textFieldFuselageNoseFinenessRatio;
	}

	public static void setTextFieldFuselageNoseFinenessRatio(TextField textFieldFuselageNoseFinenessRatio) {
		Main.textFieldFuselageNoseFinenessRatio = textFieldFuselageNoseFinenessRatio;
	}

	public static TextField getTextFieldFuselageNoseTipOffset() {
		return textFieldFuselageNoseTipOffset;
	}

	public static void setTextFieldFuselageNoseTipOffset(TextField textFieldFuselageNoseTipOffset) {
		Main.textFieldFuselageNoseTipOffset = textFieldFuselageNoseTipOffset;
	}

	public static TextField getTextFieldFuselageNoseDxCap() {
		return textFieldFuselageNoseDxCap;
	}

	public static void setTextFieldFuselageNoseDxCap(TextField textFieldFuselageNoseDxCap) {
		Main.textFieldFuselageNoseDxCap = textFieldFuselageNoseDxCap;
	}

	public static ChoiceBox<String> getChoiceBoxFuselageNoseWindshieldType() {
		return choiceBoxFuselageNoseWindshieldType;
	}

	public static void setChoiceBoxFuselageNoseWindshieldType(ChoiceBox<String> choiceBoxFuselageNoseWindshieldType) {
		Main.choiceBoxFuselageNoseWindshieldType = choiceBoxFuselageNoseWindshieldType;
	}

	public static TextField getTextFieldFuselageNoseWindshieldWidth() {
		return textFieldFuselageNoseWindshieldWidth;
	}

	public static void setTextFieldFuselageNoseWindshieldWidth(TextField textFieldFuselageNoseWindshieldWidth) {
		Main.textFieldFuselageNoseWindshieldWidth = textFieldFuselageNoseWindshieldWidth;
	}

	public static TextField getTextFieldFuselageNoseWindshieldHeight() {
		return textFieldFuselageNoseWindshieldHeight;
	}

	public static void setTextFieldFuselageNoseWindshieldHeight(TextField textFieldFuselageNoseWindshieldHeight) {
		Main.textFieldFuselageNoseWindshieldHeight = textFieldFuselageNoseWindshieldHeight;
	}

	public static TextField getTextFieldFuselageNoseMidSectionHeight() {
		return textFieldFuselageNoseMidSectionHeight;
	}

	public static void setTextFieldFuselageNoseMidSectionHeight(TextField textFieldFuselageNoseMidSectionHeight) {
		Main.textFieldFuselageNoseMidSectionHeight = textFieldFuselageNoseMidSectionHeight;
	}

	public static TextField getTextFieldFuselageNoseMidSectionRhoUpper() {
		return textFieldFuselageNoseMidSectionRhoUpper;
	}

	public static void setTextFieldFuselageNoseMidSectionRhoUpper(TextField textFieldFuselageNoseMidSectionRhoUpper) {
		Main.textFieldFuselageNoseMidSectionRhoUpper = textFieldFuselageNoseMidSectionRhoUpper;
	}

	public static TextField getTextFieldFuselageNoseMidSectionRhoLower() {
		return textFieldFuselageNoseMidSectionRhoLower;
	}

	public static void setTextFieldFuselageNoseMidSectionRhoLower(TextField textFieldFuselageNoseMidSectionRhoLower) {
		Main.textFieldFuselageNoseMidSectionRhoLower = textFieldFuselageNoseMidSectionRhoLower;
	}

	public static TextField getTextFieldFuselageCylinderLengthRatio() {
		return textFieldFuselageCylinderLengthRatio;
	}

	public static void setTextFieldFuselageCylinderLengthRatio(TextField textFieldFuselageCylinderLengthRatio) {
		Main.textFieldFuselageCylinderLengthRatio = textFieldFuselageCylinderLengthRatio;
	}

	public static TextField getTextFieldFuselageCylinderSectionWidth() {
		return textFieldFuselageCylinderSectionWidth;
	}

	public static void setTextFieldFuselageCylinderSectionWidth(TextField textFieldFuselageCylinderSectionWidth) {
		Main.textFieldFuselageCylinderSectionWidth = textFieldFuselageCylinderSectionWidth;
	}

	public static TextField getTextFieldFuselageCylinderSectionHeight() {
		return textFieldFuselageCylinderSectionHeight;
	}

	public static void setTextFieldFuselageCylinderSectionHeight(TextField textFieldFuselageCylinderSectionHeight) {
		Main.textFieldFuselageCylinderSectionHeight = textFieldFuselageCylinderSectionHeight;
	}

	public static TextField getTextFieldFuselageCylinderHeightFromGround() {
		return textFieldFuselageCylinderHeightFromGround;
	}

	public static void setTextFieldFuselageCylinderHeightFromGround(TextField textFieldFuselageCylinderHeightFromGround) {
		Main.textFieldFuselageCylinderHeightFromGround = textFieldFuselageCylinderHeightFromGround;
	}

	public static TextField getTextFieldFuselageCylinderSectionHeightRatio() {
		return textFieldFuselageCylinderSectionHeightRatio;
	}

	public static void setTextFieldFuselageCylinderSectionHeightRatio(
			TextField textFieldFuselageCylinderSectionHeightRatio) {
		Main.textFieldFuselageCylinderSectionHeightRatio = textFieldFuselageCylinderSectionHeightRatio;
	}

	public static TextField getTextFieldFuselageCylinderSectionRhoUpper() {
		return textFieldFuselageCylinderSectionRhoUpper;
	}

	public static void setTextFieldFuselageCylinderSectionRhoUpper(TextField textFieldFuselageCylinderSectionRhoUpper) {
		Main.textFieldFuselageCylinderSectionRhoUpper = textFieldFuselageCylinderSectionRhoUpper;
	}

	public static TextField getTextFieldFuselageCylinderSectionRhoLower() {
		return textFieldFuselageCylinderSectionRhoLower;
	}

	public static void setTextFieldFuselageCylinderSectionRhoLower(TextField textFieldFuselageCylinderSectionRhoLower) {
		Main.textFieldFuselageCylinderSectionRhoLower = textFieldFuselageCylinderSectionRhoLower;
	}

	public static TextField getTextFieldFuselageTailTipOffset() {
		return textFieldFuselageTailTipOffset;
	}

	public static void setTextFieldFuselageTailTipOffset(TextField textFieldFuselageTailTipOffset) {
		Main.textFieldFuselageTailTipOffset = textFieldFuselageTailTipOffset;
	}

	public static TextField getTextFieldFuselageTailDxCap() {
		return textFieldFuselageTailDxCap;
	}

	public static void setTextFieldFuselageTailDxCap(TextField textFieldFuselageTailDxCap) {
		Main.textFieldFuselageTailDxCap = textFieldFuselageTailDxCap;
	}

	public static TextField getTextFieldFuselageTailMidSectionHeight() {
		return textFieldFuselageTailMidSectionHeight;
	}

	public static void setTextFieldFuselageTailMidSectionHeight(TextField textFieldFuselageTailMidSectionHeight) {
		Main.textFieldFuselageTailMidSectionHeight = textFieldFuselageTailMidSectionHeight;
	}

	public static TextField getTextFieldFuselageTailMidRhoUpper() {
		return textFieldFuselageTailMidRhoUpper;
	}

	public static void setTextFieldFuselageTailMidRhoUpper(TextField textFieldFuselageTailMidRhoUpper) {
		Main.textFieldFuselageTailMidRhoUpper = textFieldFuselageTailMidRhoUpper;
	}

	public static TextField getTextFieldFuselageTailMidRhoLower() {
		return textFieldFuselageTailMidRhoLower;
	}

	public static void setTextFieldFuselageTailMidRhoLower(TextField textFieldFuselageTailMidRhoLower) {
		Main.textFieldFuselageTailMidRhoLower = textFieldFuselageTailMidRhoLower;
	}

	public static List<TextField> getTextFieldSpoilersXInboradList() {
		return textFieldSpoilersXInboradList;
	}

	public static void setTextFieldSpoilersXInboradList(List<TextField> textFieldSpoilersXInboradList) {
		Main.textFieldSpoilersXInboradList = textFieldSpoilersXInboradList;
	}

	public static List<TextField> getTextFieldSpoilersXOutboradList() {
		return textFieldSpoilersXOutboradList;
	}

	public static void setTextFieldSpoilersXOutboradList(List<TextField> textFieldSpoilersXOutboradList) {
		Main.textFieldSpoilersXOutboradList = textFieldSpoilersXOutboradList;
	}

	public static List<TextField> getTextFieldSpoilersYInboradList() {
		return textFieldSpoilersYInboradList;
	}

	public static void setTextFieldSpoilersYInboradList(List<TextField> textFieldSpoilersYInboradList) {
		Main.textFieldSpoilersYInboradList = textFieldSpoilersYInboradList;
	}

	public static List<TextField> getTextFieldSpoilersYOutboradList() {
		return textFieldSpoilersYOutboradList;
	}

	public static void setTextFieldSpoilersYOutboradList(List<TextField> textFieldSpoilersYOutboradList) {
		Main.textFieldSpoilersYOutboradList = textFieldSpoilersYOutboradList;
	}

	public static List<TextField> getTextFieldSpoilersMinDeflectionList() {
		return textFieldSpoilersMinDeflectionList;
	}

	public static void setTextFieldSpoilersMinDeflectionList(List<TextField> textFieldSpoilersMinDeflectionList) {
		Main.textFieldSpoilersMinDeflectionList = textFieldSpoilersMinDeflectionList;
	}

	public static List<TextField> getTextFieldSpoilersMaxDeflectionList() {
		return textFieldSpoilersMaxDeflectionList;
	}

	public static void setTextFieldSpoilersMaxDeflectionList(List<TextField> textFieldSpoilersMaxDeflectionList) {
		Main.textFieldSpoilersMaxDeflectionList = textFieldSpoilersMaxDeflectionList;
	}

	public static Pane getFuselageFrontViewPane() {
		return fuselageFrontViewPane;
	}

	public static void setFuselageFrontViewPane(Pane fuselageFrontViewPane) {
		Main.fuselageFrontViewPane = fuselageFrontViewPane;
	}

	public static Pane getFuselageSideViewPane() {
		return fuselageSideViewPane;
	}

	public static void setFuselageSideViewPane(Pane fuselageSideViewPane) {
		Main.fuselageSideViewPane = fuselageSideViewPane;
	}

	public static Pane getFuselageTopViewPane() {
		return fuselageTopViewPane;
	}

	public static void setFuselageTopViewPane(Pane fuselageTopViewPane) {
		Main.fuselageTopViewPane = fuselageTopViewPane;
	}

	public static SplitPane getFuselageViewsAndDataLogSplitPane() {
		return fuselageViewsAndDataLogSplitPane;
	}

	public static void setFuselageViewsAndDataLogSplitPane(SplitPane fuselageViewsAndDataLogSplitPane) {
		Main.fuselageViewsAndDataLogSplitPane = fuselageViewsAndDataLogSplitPane;
	}

	public static TextArea getTextAreaFuselageConsoleOutput() {
		return textAreaFuselageConsoleOutput;
	}

	public static void setTextAreaFuselageConsoleOutput(TextArea textAreaFuselageConsoleOutput) {
		Main.textAreaFuselageConsoleOutput = textAreaFuselageConsoleOutput;
	}

	public static Button getSetDataButton() {
		return setDataButton;
	}

	public static void setSetDataButton(Button setDataButton) {
		Main.setDataButton = setDataButton;
	}

	public static Button getUpdateAllButton() {
		return updateAllButton;
	}

	public static void setUpdateAllButton(Button updateAllButton) {
		Main.updateAllButton = updateAllButton;
	}

	public static Button getSaveAircraftButton() {
		return saveAircraftButton;
	}

	public static void setSaveAircraftButton(Button saveAircraftButton) {
		Main.saveAircraftButton = saveAircraftButton;
	}

	public static ToolBar getActionButtonsToolbar() {
		return actionButtonsToolbar;
	}

	public static void setActionButtonsToolbar(ToolBar actionButtonsToolbar) {
		Main.actionButtonsToolbar = actionButtonsToolbar;
	}

	public static Boolean isAircraftFormFile() {
		return isAircraftFormFile;
	}

	public static void setIsAircraftFormFile(Boolean isAircraftFormFile) {
		Main.isAircraftFormFile = isAircraftFormFile;
	}

	public static ChoiceBox<String> gethTailRiggingAngleUnitChoiceBox() {
		return hTailTailRiggingAngleUnitChoiceBox;
	}

	public static void sethTailRiggingAngleUnitChoiceBox(ChoiceBox<String> hTailTailRiggingAngleUnitChoiceBox) {
		Main.hTailTailRiggingAngleUnitChoiceBox = hTailTailRiggingAngleUnitChoiceBox;
	}

	public static ChoiceBox<String> getFuselageAdjustCriterion() {
		return fuselageAdjustCriterion;
	}

	public static void setFuselageAdjustCriterion(ChoiceBox<String> fuselageAdjustCriterion) {
		Main.fuselageAdjustCriterion = fuselageAdjustCriterion;
	}

}

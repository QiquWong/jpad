package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occfx.OCCFXForm;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftUtils.XSpacingType;
import javafx.event.EventHandler;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class CADManager {
	
	private ICADManager _theCADBuilderInterface;
	
	private Aircraft _theAircraft;
	private List<OCCShape> _theAircraftShapes = new ArrayList<>();
	private Map<CADComponentEnum, List<OCCShape>> _theAircraftSolidsMap = new HashMap<>();
	private double _importedPartsMaxDimension = 0.0;
	private double _importedPartsGeometricCenter = 0.0;
	
	// ------- JavaFX material ---------- //
	private Scene _theScene;
	private final Group _theRoot = new Group();
	private final OCCFXForm _theAxisGroup = new OCCFXForm();
	private final OCCFXForm _theAircraftGroup = new OCCFXForm();
	private final OCCFXForm _theWorld = new OCCFXForm();
	private final PerspectiveCamera _theCamera = new PerspectiveCamera(true);
	private final OCCFXForm _theCameraXform = new OCCFXForm();
	private final OCCFXForm _theCameraXform2 = new OCCFXForm();
	private final OCCFXForm _theCameraXform3 = new OCCFXForm();
	
	private static final double CAMERA_INITIAL_DISTANCE = -150;
	private static final double CAMERA_INITIAL_X_ANGLE = -45.0;
	private static final double CAMERA_INITIAL_Y_ANGLE = 210.0;
	private static final double CAMERA_NEAR_CLIP = 0.1;
	private static final double CAMERA_FAR_CLIP = 10000.0;
	private static final double AXIS_LENGTH = 1.0;
	private static final double CONTROL_MULTIPLIER = 0.1;
	private static final double SHIFT_MULTIPLIER = 10.0;
	private static final double MOUSE_SPEED = 0.1;
	private static final double ROTATION_SPEED = 2.0;
	private static final double TRACK_SPEED = 0.3;
	
	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;
	
	public static CADManager importFromXML(
			String pathToXML, 
			Aircraft theAircraft) {
		
		//---------------------------------------------------------------
		// PRELIMINARY OPERATIONS
		//---------------------------------------------------------------
		JPADXmlReader reader = new JPADXmlReader(pathToXML);
		
		System.out.println("=================================================");
		System.out.println("====== Reading CAD modeler configuration file ...");
		System.out.println("=================================================");
		
		Boolean generateFuselage;
		Boolean generateWing;
		Boolean generateHorizontal;
		Boolean generateVertical;
		Boolean generateCanard;
		Boolean generateWingFairing;
		Boolean generateCanardFairing;
		
		String generateFuselageString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//fuselage/@generate");
		
		String generateWingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing/@generate");
		
		String generateHorizontalString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//horizontal/@generate");
		
		String generateVerticalString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//vertical/@generate");
		
		String generateCanardString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard/@generate");
		
		String generateWingFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing_fairing/@generate");
		
		String generateCanardFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard_fairing/@generate");
		
		generateFuselage = (generateFuselageString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWing = (generateWingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateHorizontal = (generateHorizontalString.equalsIgnoreCase("TRUE")) ? true : false;
		generateVertical = (generateVerticalString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanard = (generateCanardString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWingFairing = (generateWingFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanardFairing = (generateCanardFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		
		//---------------------------------------------------------------
		// FUSELAGE CAD OPERATIONS
		//---------------------------------------------------------------
		
		// Initialize FUSELAGE CAD parameters
		double noseCapSectionFactor1 = 0.15;
		double noseCapSectionFactor2 = 1.0;
		int numberNoseCapSections = 3;
		int numberNoseTrunkSections = 7;
		XSpacingType spacingTypeNoseTrunk = XSpacingType.COSINUS;
		int numberTailTrunkSections = 7;
		XSpacingType spacingTypeTailTrunk = XSpacingType.COSINUS;
		double tailCapSectionFactor1 = 1.0;
		double tailCapSectionFactor2 = 0.15;
		int numberTailCapSections = 3;
		boolean exportFuselageSupportShapes = false;
		
		// Read FUSELAGE CAD parameters from the XML file
		if (generateFuselage && (theAircraft.getFuselage() != null)) { 
			
			String noseCapSectionFactor1String = reader.getXMLPropertyByPath("//fuselage/noseCapSectionFactor1");
			if (noseCapSectionFactor1String != null) 
				noseCapSectionFactor1 = Double.valueOf(noseCapSectionFactor1String);
			
			String noseCapSectionFactor2String = reader.getXMLPropertyByPath("//fuselage/noseCapSectionFactor2");
			if (noseCapSectionFactor2String != null) 
				noseCapSectionFactor2 = Double.valueOf(noseCapSectionFactor2String);
			
			String numberNoseCapSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseCapSections");
			if (numberNoseCapSectionsString != null) 
				numberNoseCapSections = Integer.valueOf(numberNoseCapSectionsString);
			
			String numberNoseTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseTrunkSections");
			if (numberNoseTrunkSectionsString != null) 
				numberNoseTrunkSections = Integer.valueOf(numberNoseTrunkSectionsString);
			
			String spacingTypeNoseTrunkString = reader.getXMLPropertyByPath("//fuselage/spacingTypeNoseTrunk");
			if (spacingTypeNoseTrunkString != null) 
				spacingTypeNoseTrunk = XSpacingType.valueOf(spacingTypeNoseTrunkString);
			
			String numberTailTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailTrunkSections");
			if (numberTailTrunkSectionsString != null) 
				numberTailTrunkSections = Integer.valueOf(numberTailTrunkSectionsString);
			
			String spacingTypeTailTrunkString = reader.getXMLPropertyByPath("//fuselage/spacingTypeTailTrunk");
			if (spacingTypeTailTrunkString != null) 
				spacingTypeTailTrunk = XSpacingType.valueOf(spacingTypeTailTrunkString);
			
			String tailCapSectionFactor1String = reader.getXMLPropertyByPath("//fuselage/tailCapSectionFactor1");
			if (tailCapSectionFactor1String != null) 
				tailCapSectionFactor1 = Double.valueOf(tailCapSectionFactor1String);
			
			String tailCapSectionFactor2String = reader.getXMLPropertyByPath("//fuselage/tailCapSectionFactor2");
			if (tailCapSectionFactor2String != null) 
				tailCapSectionFactor2 = Double.valueOf(tailCapSectionFactor2String);
			
			String numberTailCapSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailCapSections");
			if (numberTailCapSectionsString != null) 
				numberTailCapSections = Integer.valueOf(numberTailCapSectionsString);
			
		} else {
			
			generateFuselage = false;			
		}
		
		//---------------------------------------------------------------
		// WING CAD OPERATIONS
		//---------------------------------------------------------------
		
		// Initialize WING CAD parameters
		double wingTipTolerance = 1e-3;
		boolean exportWingSupportShapes = false;
		
		// Read wing CAD parameters from the xml file
		if (generateWing && (theAircraft.getWing() != null)) {
			
			String wingTipToleranceString = reader.getXMLPropertyByPath("//wing/tipTolerance");
			if (wingTipToleranceString != null)
				wingTipTolerance = Double.valueOf(wingTipToleranceString);
			
			String exportWingSupportShapesString = reader.getXMLPropertyByPath("//wing/exportSupportShapes");
			if (exportWingSupportShapesString != null) 
				exportWingSupportShapes = Boolean.parseBoolean(exportWingSupportShapesString);
			
		} else {
			
			generateWing = false;
		}
		
		//---------------------------------------------------------------
		// HORIZONTAL TAIL CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize HORIZONTAL TAIL CAD parameters
		double horizontalTipTolerance = 1e-3;
		boolean exportHorizontalSupportShapes = false;

		// Read horizontal tail CAD parameters from the XML file
		if (generateHorizontal && (theAircraft.getHTail() != null)) {
			
			String horizontalTipToleranceString = reader.getXMLPropertyByPath("//horizontal/tipTolerance");
			if (horizontalTipToleranceString != null)
				horizontalTipTolerance = Double.valueOf(horizontalTipToleranceString);
			
			String exportHorizontalSupportShapesString = reader.getXMLPropertyByPath("//horizontal/exportSupportShapes");
			if (exportHorizontalSupportShapesString != null) 
				exportHorizontalSupportShapes = Boolean.parseBoolean(exportHorizontalSupportShapesString);

		} else {
			
			generateHorizontal = false;
		}
		
		//---------------------------------------------------------------
		// VERTICAL TAIL CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize VERTICAL TAIL CAD parameters
		double verticalTipTolerance = 1e-3;
		boolean exportVerticalSupportShapes = false;

		// Read vertical tail CAD parameters from the XML file
		if (generateVertical && (theAircraft.getVTail() != null)) { 

			String verticalTipToleranceString = reader.getXMLPropertyByPath("//vertical/tipTolerance");
			if (verticalTipToleranceString != null)
				verticalTipTolerance = Double.valueOf(verticalTipToleranceString);
			
			String exportVerticalSupportShapesString = reader.getXMLPropertyByPath("//vertical/exportSupportShapes");
			if (exportVerticalSupportShapesString != null) 
				exportVerticalSupportShapes = Boolean.parseBoolean(exportVerticalSupportShapesString);
			
		} else {
			
			generateVertical = false;
		}
		
		//---------------------------------------------------------------
		// CANARD CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize CANARD CAD parameters
		double canardTipTolerance = 1e-3;
		boolean exportCanardSupportShapes = false;

		// Read vertical tail CAD parameters from the XML file
		if (generateCanard && (theAircraft.getCanard() != null)) { 

			String canardTipToleranceString = reader.getXMLPropertyByPath("//canard/tipTolerance");
			if (canardTipToleranceString != null)
				canardTipTolerance = Double.valueOf(canardTipToleranceString);
			
			String exportCanardSupportShapesString = reader.getXMLPropertyByPath("//canard/exportSupportShapes");
			if (exportCanardSupportShapesString != null) 
				exportCanardSupportShapes = Boolean.parseBoolean(exportCanardSupportShapesString);
			
		} else {
			
			generateCanard = false;
		}
		
		//---------------------------------------------------------------
		// WING/FUSELAGE FAIRING CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize WING FAIRING CAD parameters
		double wingFairingFrontLengthFactor = 1.25;
		double wingFairingBackLengthFactor = 1.25;
		double wingFairingSideSizeFactor = 0.25;
		double wingFairingHeightFactor = 0.25;
		double wingFairingHeightBelowContactFactor = 0.70;
		double wingFairingHeightAboveContactFactor = 0.10;
		double wingFairingFilletRadiusFactor = 0.60;

		// Read WING FAIRING CAD parameters from the XML file
		if (generateWingFairing && (theAircraft.getWing() != null) && (theAircraft.getFuselage() != null)) { 

			String wingFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/frontLengthFactor");
			if (wingFairingFrontLengthFactorString != null)
				wingFairingFrontLengthFactor = Double.valueOf(wingFairingFrontLengthFactorString);
			
			String wingFairingBackLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/backLengthFactor");
			if (wingFairingBackLengthFactorString != null)
				wingFairingBackLengthFactor = Double.valueOf(wingFairingBackLengthFactorString);
			
			String wingFairingSideSizeFactorString = reader.getXMLPropertyByPath("//wing_fairing/sideSizeFactor");
			if (wingFairingSideSizeFactorString != null)
				wingFairingSideSizeFactor = Double.valueOf(wingFairingSideSizeFactorString);
			
			String wingFairingHeightFactorString = reader.getXMLPropertyByPath("//wing_fairing/fairingHeightFactor");
			if (wingFairingHeightFactorString != null)
				wingFairingHeightFactor = Double.valueOf(wingFairingHeightFactorString);
			
			String wingFairingHeightBelowContactFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightBelowContactFactor");
			if (wingFairingHeightBelowContactFactorString != null)
				wingFairingHeightBelowContactFactor = Double.valueOf(wingFairingHeightBelowContactFactorString);
			
			String wingFairingHeightAboveContactFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightAboveContactFactor");
			if (wingFairingHeightAboveContactFactorString != null)
				wingFairingHeightAboveContactFactor = Double.valueOf(wingFairingHeightAboveContactFactorString);
			
			String wingFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//wing_fairing/filletRadiusFactor");
			if (wingFairingFilletRadiusFactorString != null)
				wingFairingFilletRadiusFactor = Double.valueOf(wingFairingFilletRadiusFactorString);
			
		} else {
			
			generateWingFairing = false;
		}
		
		//---------------------------------------------------------------
		// CANARD/FUSELAGE FAIRING CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize CANARD FAIRING CAD parameters
		double canardFairingFrontLengthFactor = 1.25;
		double canardFairingBackLengthFactor = 1.25;
		double canardFairingSideSizeFactor = 0.25;
		double canardFairingHeightFactor = 0.25;
		double canardFairingHeightBelowContactFactor = 0.70;
		double canardFairingHeightAboveContactFactor = 0.10;
		double canardFairingFilletRadiusFactor = 0.60;

		// Read CANARD FAIRING CAD parameters from the XML file
		if (generateCanardFairing && (theAircraft.getCanard() != null) && (theAircraft.getFuselage() != null)) { 

			String canardFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/frontLengthFactor");
			if (canardFairingFrontLengthFactorString != null)
				canardFairingFrontLengthFactor = Double.valueOf(canardFairingFrontLengthFactorString);
			
			String canardFairingBackLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/backLengthFactor");
			if (canardFairingBackLengthFactorString != null)
				canardFairingBackLengthFactor = Double.valueOf(canardFairingBackLengthFactorString);
			
			String canardFairingSideSizeFactorString = reader.getXMLPropertyByPath("//canard_fairing/sideSizeFactor");
			if (canardFairingSideSizeFactorString != null)
				canardFairingSideSizeFactor = Double.valueOf(canardFairingSideSizeFactorString);
			
			String canardFairingHeightFactorString = reader.getXMLPropertyByPath("//canard_fairing/fairingHeightFactor");
			if (canardFairingHeightFactorString != null)
				canardFairingHeightFactor = Double.valueOf(canardFairingHeightFactorString);
			
			String canardFairingHeightBelowContactFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightBelowContactFactor");
			if (canardFairingHeightBelowContactFactorString != null)
				canardFairingHeightBelowContactFactor = Double.valueOf(canardFairingHeightBelowContactFactorString);
			
			String canardFairingHeightAboveContactFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightAboveContactFactor");
			if (canardFairingHeightAboveContactFactorString != null)
				canardFairingHeightAboveContactFactor = Double.valueOf(canardFairingHeightAboveContactFactorString);
			
			String canardFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//canard_fairing/filletRadiusFactor");
			if (canardFairingFilletRadiusFactorString != null)
				canardFairingFilletRadiusFactor = Double.valueOf(canardFairingFilletRadiusFactorString);
			
		} else {
			
			generateCanardFairing = false;
		}
		
		// EXPORT TO FILE OPTIONS
		Boolean exportToFile;
		FileExtension fileExtension = FileExtension.STEP;

		String exportToFileString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@value");
		exportToFile = exportToFileString.equalsIgnoreCase("TRUE") ? true : false;

		if (exportToFile) {
			
			String fileExtensionString = reader.getXMLPropertyByPath("//export_to_file/file_format");
			fileExtension = FileExtension.valueOf(fileExtensionString);
			
		}		
		
		//---------------------------------------------------------------
		// GENERATE THE CAD MANAGER INTERFACE
		//---------------------------------------------------------------
		ICADManager theCADBuilderInterface = new ICADManager.Builder()
				.setGenerateFuselage(generateFuselage)
				.setGenerateWing(generateWing)
				.setGenerateHorizontal(generateHorizontal)
				.setGenerateVertical(generateVertical)
				.setGenerateCanard(generateCanard)
				.setGenerateWingFairing(generateWingFairing)
				.setGenerateCanardFairing(generateCanardFairing)
				.setNoseCapSectionFactor1(noseCapSectionFactor1)
				.setNoseCapSectionFactor2(noseCapSectionFactor2)
				.setNumberNoseCapSections(numberNoseCapSections)
				.setNumberNoseTrunkSections(numberNoseTrunkSections)
				.setSpacingTypeNoseTrunk(spacingTypeNoseTrunk)
				.setNumberTailTrunkSections(numberTailTrunkSections)
				.setSpacingTypeTailTrunk(spacingTypeTailTrunk)
				.setTailCapSectionFactor1(tailCapSectionFactor1)
				.setTailCapSectionFactor2(tailCapSectionFactor2)
				.setNumberTailCapSections(numberTailCapSections)
				.setExportFuselageSupportShapes(exportFuselageSupportShapes)
				.setWingTipTolerance(wingTipTolerance)
				.setExportWingSupportShapes(exportWingSupportShapes)
				.setHorizontalTipTolerance(horizontalTipTolerance)
				.setExportHorizontalSupportShapes(exportHorizontalSupportShapes)
				.setVerticalTipTolerance(verticalTipTolerance)
				.setExportVerticalSupportShapes(exportVerticalSupportShapes)
				.setCanardTipTolerance(canardTipTolerance)
				.setExportCanardSupportShapes(exportCanardSupportShapes)
				.setWingFairingFrontLengthFactor(wingFairingFrontLengthFactor)
				.setWingFairingBackLengthFactor(wingFairingBackLengthFactor)
				.setWingFairingSideSizeFactor(wingFairingSideSizeFactor)
				.setWingFairingHeightFactor(wingFairingHeightFactor)
				.setWingFairingHeightBelowContactFactor(wingFairingHeightBelowContactFactor)
				.setWingFairingHeightAboveContactFactor(wingFairingHeightAboveContactFactor)
				.setWingFairingFilletRadiusFactor(wingFairingFilletRadiusFactor)
				.setCanardFairingFrontLengthFactor(canardFairingFrontLengthFactor)
				.setCanardFairingBackLengthFactor(canardFairingBackLengthFactor)
				.setCanardFairingSideSizeFactor(canardFairingSideSizeFactor)
				.setCanardFairingHeightFactor(canardFairingHeightFactor)
				.setCanardFairingHeightBelowContactFactor(canardFairingHeightBelowContactFactor)
				.setCanardFairingHeightAboveContactFactor(canardFairingHeightAboveContactFactor)
				.setCanardFairingFilletRadiusFactor(canardFairingFilletRadiusFactor)
				.setExportToFile(exportToFile)
				.setFileExtension(fileExtension)
				.build();
		
		CADManager theCADManager = new CADManager();
		theCADManager.setTheCADBuilderInterface(theCADBuilderInterface);
		theCADManager.setTheAircraft(theAircraft);
		
		return theCADManager;
	}
	
	@Override
	public String toString() {
		
		StringBuilder stringBuilder = new StringBuilder()
				.append("\n\n\t--------------------------------------\n")
				.append("\tCAD generator input configuration\n")
				.append("\t--------------------------------------\n\n");
		
		stringBuilder.append("\t[Generate Fuselage CAD]: " + _theCADBuilderInterface.getGenerateFuselage() + ".\n");
		if (!_theCADBuilderInterface.getGenerateFuselage()) 		
			stringBuilder.append("\n");	
		else			
			stringBuilder.append("\tNose cap section factor #1: " + _theCADBuilderInterface.getNoseCapSectionFactor1() + ".\n")
						 .append("\tNose cap section factor #2: " + _theCADBuilderInterface.getNoseCapSectionFactor2() + ".\n")
						 .append("\tNumber of nose cap sections: " + _theCADBuilderInterface.getNumberNoseCapSections() + ".\n")
						 .append("\tNumber of nose trunk sections: " + _theCADBuilderInterface.getNumberNoseTrunkSections() + ".\n")
						 .append("\tNose trunk spacing: " + _theCADBuilderInterface.getSpacingTypeNoseTrunk().toString() + ".\n")
						 .append("\tNumber of tail trunk sections: " + _theCADBuilderInterface.getNumberTailTrunkSections() + ".\n")
						 .append("\tTail trunk spacing: " + _theCADBuilderInterface.getSpacingTypeTailTrunk().toString() + ".\n")
						 .append("\tTail cap section factor #1: " + _theCADBuilderInterface.getTailCapSectionFactor1() + ".\n")
						 .append("\tTail cap section factor #2: " + _theCADBuilderInterface.getTailCapSectionFactor2() + ".\n")
						 .append("\tNumber of tail cap sections: " + _theCADBuilderInterface.getNumberTailCapSections() + ".\n")
						 .append("\tSupport shapes export: " + _theCADBuilderInterface.getExportFuselageSupportShapes() + ".\n")
						 .append("\n");					 
		
		stringBuilder.append("\t[Generate Wing CAD]: " + _theCADBuilderInterface.getGenerateWing() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateWing())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip tolerance: " + _theCADBuilderInterface.getWingTipTolerance() + "\n")
						 .append("\tSupport shapes export: " + _theCADBuilderInterface.getExportWingSupportShapes() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Horizontal Tail CAD]: " + _theCADBuilderInterface.getGenerateHorizontal() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateHorizontal())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip tolerance: " + _theCADBuilderInterface.getHorizontalTipTolerance() + ".\n")
						 .append("\tSupport shapes export: " + _theCADBuilderInterface.getExportHorizontalSupportShapes() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Vertical Tail CAD]: " + _theCADBuilderInterface.getGenerateVertical() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateVertical())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip tolerance: " + _theCADBuilderInterface.getVerticalTipTolerance() + ".\n")
						 .append("\tSupport shapes export: " + _theCADBuilderInterface.getExportVerticalSupportShapes() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Canard CAD]: " + _theCADBuilderInterface.getGenerateCanard() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateCanard())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip tolerance: " + _theCADBuilderInterface.getCanardTipTolerance() + ".\n")
						 .append("\tSupport shapes export: " + _theCADBuilderInterface.getExportCanardSupportShapes() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Wing/Fuselage Fairing CAD]: " + _theCADBuilderInterface.getGenerateWingFairing() + ".\n");
		if (!_theCADBuilderInterface.getGenerateWingFairing()) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFront length factor: " + _theCADBuilderInterface.getWingFairingFrontLengthFactor() + ".\n")
						 .append("\tRear length factor: " + _theCADBuilderInterface.getWingFairingBackLengthFactor() + ".\n")
						 .append("\tSide size factor: " + _theCADBuilderInterface.getWingFairingSideSizeFactor() + ".\n")
						 .append("\tHeight factor: " + _theCADBuilderInterface.getWingFairingHeightFactor() + ".\n")
						 .append("\tHeight below contact factor: " + _theCADBuilderInterface.getWingFairingHeightBelowContactFactor() + ".\n")
						 .append("\tHeight above contact factor: " + _theCADBuilderInterface.getWingFairingHeightAboveContactFactor() + ".\n")
						 .append("\tFillet radius factor: " + _theCADBuilderInterface.getWingFairingFilletRadiusFactor() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Canard/Fuselage Fairing CAD]: " + _theCADBuilderInterface.getGenerateCanardFairing() + ".\n");
		if (!_theCADBuilderInterface.getGenerateCanardFairing()) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFront length factor: " + _theCADBuilderInterface.getCanardFairingFrontLengthFactor() + ".\n")
						 .append("\tRear length factor: " + _theCADBuilderInterface.getCanardFairingBackLengthFactor() + ".\n")
						 .append("\tSide size factor: " + _theCADBuilderInterface.getCanardFairingSideSizeFactor() + ".\n")
						 .append("\tHeight factor: " + _theCADBuilderInterface.getCanardFairingHeightFactor() + ".\n")
						 .append("\tHeight below contact factor: " + _theCADBuilderInterface.getCanardFairingHeightBelowContactFactor() + ".\n")
						 .append("\tHeight above contact factor: " + _theCADBuilderInterface.getCanardFairingHeightAboveContactFactor() + ".\n")
						 .append("\tFillet radius factor: " + _theCADBuilderInterface.getCanardFairingFilletRadiusFactor() + ".\n")
						 .append("\n");
				
		stringBuilder.append("\t[Export shapes to file]: " + _theCADBuilderInterface.getExportToFile() + ".\n");
		if (!_theCADBuilderInterface.getExportToFile())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFile format: " + _theCADBuilderInterface.getFileExtension().toString() + ".\n")
			  			 .append("\n");
			
		return stringBuilder.toString();
	}
	
	private void initializeCADMap() {
		
		if (_theCADBuilderInterface.getGenerateFuselage()) {
			_theAircraftSolidsMap.put(CADComponentEnum.FUSELAGE, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateWing()) {
			_theAircraftSolidsMap.put(CADComponentEnum.WING, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateHorizontal()) {
			_theAircraftSolidsMap.put(CADComponentEnum.HORIZONTAL, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateVertical()) {
			_theAircraftSolidsMap.put(CADComponentEnum.VERTICAL, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateCanard()) {
			_theAircraftSolidsMap.put(CADComponentEnum.CANARD, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateWingFairing()) {
			_theAircraftSolidsMap.put(CADComponentEnum.WING_FAIRING, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateCanardFairing()) {
			_theAircraftSolidsMap.put(CADComponentEnum.CANARD_FAIRING, new ArrayList<OCCShape>());
		}
	}
	
	public void generateCAD() {
		initializeCADMap();
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT CAD
		//---------------------------------------------------------------
		
		// FUSELAGE
		if (_theCADBuilderInterface.getGenerateFuselage()) {
			
			List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(
					_theAircraft.getFuselage(), 
					_theCADBuilderInterface.getNoseCapSectionFactor1(), 
					_theCADBuilderInterface.getNoseCapSectionFactor2(), 
					_theCADBuilderInterface.getNumberNoseCapSections(), 
					_theCADBuilderInterface.getNumberNoseTrunkSections(), 
					_theCADBuilderInterface.getSpacingTypeNoseTrunk(), 
					_theCADBuilderInterface.getNumberTailTrunkSections(), 
					_theCADBuilderInterface.getSpacingTypeTailTrunk(), 
					_theCADBuilderInterface.getTailCapSectionFactor1(), 
					_theCADBuilderInterface.getTailCapSectionFactor2(), 
					_theCADBuilderInterface.getNumberTailCapSections(), 
					true, 
					true, 
					_theCADBuilderInterface.getExportFuselageSupportShapes()
					);
			
			_theAircraftShapes.addAll(fuselageShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.FUSELAGE).addAll(AircraftUtils.getAircraftSolid(fuselageShapes));
		}
		
		// WING
		if (_theCADBuilderInterface.getGenerateWing()) {

			List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(
					_theAircraft.getWing(), 
					_theAircraft.getWing().getType(), 
					_theCADBuilderInterface.getWingTipTolerance(), 
					false, 
					true, 
					_theCADBuilderInterface.getExportWingSupportShapes()
					);

			_theAircraftShapes.addAll(wingShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.WING).addAll(AircraftUtils.getAircraftSolid(wingShapes));
		}
		
		// HORIZONTAL
		if (_theCADBuilderInterface.getGenerateHorizontal()) {

			List<OCCShape> horizontalShapes = AircraftUtils.getLiftingSurfaceCAD(
					_theAircraft.getHTail(), 
					_theAircraft.getHTail().getType(), 
					_theCADBuilderInterface.getHorizontalTipTolerance(), 
					false, 
					true, 
					_theCADBuilderInterface.getExportHorizontalSupportShapes()
					);

			_theAircraftShapes.addAll(horizontalShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.HORIZONTAL).addAll(AircraftUtils.getAircraftSolid(horizontalShapes));
		}
		
		// VERTICAL
		if (_theCADBuilderInterface.getGenerateVertical()) {

			List<OCCShape> verticalShapes = AircraftUtils.getLiftingSurfaceCAD(
					_theAircraft.getVTail(), 
					_theAircraft.getVTail().getType(), 
					_theCADBuilderInterface.getVerticalTipTolerance(), 
					false, 
					true, 
					_theCADBuilderInterface.getExportVerticalSupportShapes()
					);

			_theAircraftShapes.addAll(verticalShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.VERTICAL).addAll(AircraftUtils.getAircraftSolid(verticalShapes));
		}
		
		// CANARD
		if (_theCADBuilderInterface.getGenerateCanard()) {

			List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(
					_theAircraft.getCanard(), 
					_theAircraft.getCanard().getType(), 
					_theCADBuilderInterface.getCanardTipTolerance(), 
					false, 
					true, 
					_theCADBuilderInterface.getExportCanardSupportShapes()
					);

			_theAircraftShapes.addAll(canardShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.CANARD).addAll(AircraftUtils.getAircraftSolid(canardShapes));
		}
		
		// WING/FUSELAGE FAIRING 
		if (_theCADBuilderInterface.getGenerateWingFairing()) {
			
			List<OCCShape> wingFairingShapes = AircraftUtils.getFairingShapes(
					_theAircraft.getFuselage(), 
					_theAircraft.getWing(), 
					_theCADBuilderInterface.getWingFairingFrontLengthFactor(), 
					_theCADBuilderInterface.getWingFairingBackLengthFactor(), 
					_theCADBuilderInterface.getWingFairingSideSizeFactor(), 
					_theCADBuilderInterface.getWingFairingHeightFactor(), 
					_theCADBuilderInterface.getWingFairingHeightBelowContactFactor(), 
					_theCADBuilderInterface.getWingFairingHeightAboveContactFactor(), 
					_theCADBuilderInterface.getWingFairingFilletRadiusFactor()
					);
			
			_theAircraftShapes.addAll(wingFairingShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.WING_FAIRING).addAll(AircraftUtils.getAircraftSolid(wingFairingShapes));
		}
		
		// CANARD/FUSELAGE FAIRING
		if (_theCADBuilderInterface.getGenerateCanardFairing()) {
			
			List<OCCShape> canardFairingShapes = AircraftUtils.getFairingShapes(
					_theAircraft.getFuselage(), 
					_theAircraft.getCanard(), 
					_theCADBuilderInterface.getCanardFairingFrontLengthFactor(), 
					_theCADBuilderInterface.getCanardFairingBackLengthFactor(), 
					_theCADBuilderInterface.getCanardFairingSideSizeFactor(), 
					_theCADBuilderInterface.getCanardFairingHeightFactor(), 
					_theCADBuilderInterface.getCanardFairingHeightBelowContactFactor(), 
					_theCADBuilderInterface.getCanardFairingHeightAboveContactFactor(), 
					_theCADBuilderInterface.getCanardFairingFilletRadiusFactor()
					);
			
			_theAircraftShapes.addAll(canardFairingShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.CANARD_FAIRING).addAll(AircraftUtils.getAircraftSolid(canardFairingShapes));
		}
	}
	
	public void exportCAD(String outputFolderPath) {
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT CAD FILE
		//---------------------------------------------------------------	
		String outputFileAbsolutePath = outputFolderPath + _theAircraft.getId().replaceAll("\\s", "");
		
		AircraftUtils.getAircraftFile(
				_theAircraftShapes, 
				outputFileAbsolutePath, 
				_theCADBuilderInterface.getFileExtension());
		
	}
	
	public void generateScene() {
		
		_theRoot.getChildren().add(_theWorld);
		_theRoot.setDepthTest(DepthTest.ENABLE);
		
		buildCamera();
		buildAxes();
		buildAircraftGroup();
		
		_theScene = new Scene(_theRoot, 1024, 768, true);
		_theScene.setFill(new RadialGradient(225, 225, 300, 300, 500, false,
                CycleMethod.NO_CYCLE, new Stop[]
                { new Stop(0f, Color.LIGHTSKYBLUE),
                  new Stop(1f, Color.LIGHTBLUE) }));
		
		handleKeyboard(_theScene, _theRoot);
		handleMouse(_theScene, _theRoot);
	}
	
	private void buildAircraftGroup() {
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT XFORM
		//---------------------------------------------------------------	
		
		// Instantiate the XFORM
		OCCFXForm aircraftXform = new OCCFXForm();
		
		// Populate the XFORM
		List<MeshView> faces = new ArrayList<>();
		
		faces.addAll(generateTriangleMeshes().stream()
				.flatMap(List::stream)
				.map(m -> {
					MeshView face = new MeshView(m);
					face.setDrawMode(DrawMode.FILL);
					return face;
				})
				.collect(Collectors.toList()));
		
		aircraftXform.getChildren().addAll(faces);					
		_theAircraftGroup.getChildren().add(aircraftXform);
		_theWorld.getChildren().addAll(_theAircraftGroup);
	}
	
	private List<List<TriangleMesh>> generateTriangleMeshes() {
		
		//---------------------------------------------------------------
		// GENERATE A TRIANGLE MESH LIST FOR EACH CAD FACE
		//---------------------------------------------------------------	
		
		// Filter the solids
		List<OCCShape> solids = AircraftUtils.getAircraftSolid(_theAircraftShapes);
		
		// Extract the mesh
		List<List<TriangleMesh>> triangleMeshes = solids.stream()
				.map(s -> (new OCCFXMeshExtractor(s.getShape())).getFaces().stream()
						.map(f -> {
							OCCFXMeshExtractor.FaceData faceData = new OCCFXMeshExtractor.FaceData(f, true);
							faceData.load();
							return faceData.getTriangleMesh();
						})
						.collect(Collectors.toList()))
				.collect(Collectors.toList());
		
		return triangleMeshes;
	}
	
	private void buildCamera() {
		_theRoot.getChildren().add(_theCameraXform);
		_theCameraXform.getChildren().add(_theCameraXform2);
		_theCameraXform2.getChildren().add(_theCameraXform3);
		_theCameraXform3.getChildren().add(_theCamera);
		_theCameraXform3.setRotateZ(180.0);
		
		_theCamera.setNearClip(CAMERA_NEAR_CLIP);
		_theCamera.setFarClip(CAMERA_FAR_CLIP);
		_theCamera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
		_theCameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
		_theCameraXform.ry.setPivotX(_theAircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)/2.0); // TODO:
		_theCameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
		_theCameraXform.rx.setPivotX(_theAircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)/2.0);
	}
	
	private void buildAxes() {
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.DARKRED);
		redMaterial.setSpecularColor(Color.RED);
		
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);
		
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(Color.DARKBLUE);
		blueMaterial.setSpecularColor(Color.BLUE);
		
		final Box xAxis = new Box(AXIS_LENGTH, 0.1, 0.1);
		final Box yAxis = new Box(0.1, AXIS_LENGTH, 0.1);
		final Box zAxis = new Box(0.1, 0.1, AXIS_LENGTH);
		
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);
		
		_theAxisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		_theAxisGroup.setVisible(false);
		_theWorld.getChildren().addAll(_theAxisGroup);
	}
	
	private void handleMouse(Scene scene, final Node root) {
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();		
			}
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);
				
				double modifier = 1.0;
				
				if (me.isControlDown()) {
					modifier = CONTROL_MULTIPLIER;
				}
				if (me.isShiftDown()) {
					modifier = SHIFT_MULTIPLIER;
				}
				if (me.isPrimaryButtonDown()) {
					_theCameraXform.ry.setAngle(_theCameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);
					_theCameraXform.rx.setAngle(_theCameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);
				}
				else if (me.isSecondaryButtonDown()) {
					double z = _theCamera.getTranslateZ();
					double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
					_theCamera.setTranslateZ(newZ);
				}
				else if (me.isMiddleButtonDown()) {
					_theCameraXform2.t.setX(_theCameraXform2.t.getX() + mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);
					_theCameraXform2.t.setY(_theCameraXform2.t.getY() + mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);
				}
			}
		});
	}
	
	private void handleKeyboard(Scene scene, final Node root) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case A: // Side view
					_theCameraXform2.t.setX(0.0);
					_theCameraXform2.t.setY(0.0);
					_theCamera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
					_theCameraXform.ry.setAngle(-90.0);
					_theCameraXform.rx.setAngle(180.0);
					break;
				case S: // Up view
					_theCameraXform2.t.setX(0.0);
					_theCameraXform2.t.setY(0.0);
					_theCamera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
					_theCameraXform.ry.setAngle(0.0);
					_theCameraXform.rx.setAngle(180.0);
					break;
				case Z: // Reset camera
					_theCameraXform2.t.setX(0.0);
					_theCameraXform2.t.setY(0.0);
					_theCamera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
					_theCameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
					_theCameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
					break;
				case X: // Toggle axis visibility
					_theAxisGroup.setVisible(!_theAxisGroup.isVisible());
					break;
				case V: // Toggle aircraft group visibility
					_theAircraftGroup.setVisible(!_theAircraftGroup.isVisible());
					break;
				default:
					break;
				} 
			}
		});
	}
	
	public ICADManager getTheCADBuilderInterface() {
		return _theCADBuilderInterface;
	}
	
	public void setTheCADBuilderInterface(ICADManager theCADBuilderInterface) {
		this._theCADBuilderInterface = theCADBuilderInterface;
	}
	
	public Aircraft getTheAircraft() {
		return _theAircraft;
	}
	
	public void setTheAircraft(Aircraft theAircraft) {
		this._theAircraft = theAircraft;
	}
	
	public List<OCCShape> getTheAircraftShapes() {
		return _theAircraftShapes;
	}
	
	public Map<CADComponentEnum, List<OCCShape>> getTheAircraftSolidsMap() {
		return _theAircraftSolidsMap;
	}
	
	public void setTheAircraftShapes(List<OCCShape> theAircraftSolidParts) {
		this._theAircraftShapes = theAircraftSolidParts;
	}
	
	public double getImportedPartsMaxDimension() {
		return _importedPartsMaxDimension;
	}
	
	public double getImportedPartsGeometricCenter() {
		return _importedPartsGeometricCenter;
	}
	
	public Scene getTheFXScene() {
		return _theScene;
	}
	
	public Group getTheFXRoot() {
		return _theRoot;
	}
	
	public OCCFXForm getTheFXWorld() {
		return _theWorld;
	}
	
	public PerspectiveCamera getTheCamera() {
		return _theCamera;
	}
	
	public enum CADComponentEnum {
		FUSELAGE,
		WING,
		HORIZONTAL,
		VERTICAL,
		CANARD,
		WING_FAIRING,
		CANARD_FAIRING;
	}
	
}

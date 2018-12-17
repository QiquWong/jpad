package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import it.unina.daf.jpadcad.occ.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftUtils.XSpacingType;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class CADManager {
	
	private ICADManager _theCADBuilderInterface;
	
	private Aircraft _theAircraft;
	private List<OCCShape> _theAircraftSolidParts = new ArrayList<>();
	
	// ------- JavaFX material ---------- //
	private final Group root = new Group();
	private final Xform axisGroup = new Xform();
	private final Xform aircraftGroup = new Xform();
	private final Xform world = new Xform();
	private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private final Xform cameraXform = new Xform();
	private final Xform cameraXform2 = new Xform();
	private final Xform cameraXform3 = new Xform();
	
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
		
		// Read FUSELAGE CAD parameters from the xml file
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

		// Read horizontal tail CAD parameters from the xml file
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

		// Read vertical tail CAD parameters from the xml file
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

		// Read vertical tail CAD parameters from the xml file
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

		// Read CANARD FAIRING CAD parameters from the xml file
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
	
	public void generateCAD() {
		List<OCCShape> solids = new ArrayList<>();
		
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
			
			solids.addAll(fuselageShapes);
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

			solids.addAll(wingShapes);
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

			solids.addAll(horizontalShapes);
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

			solids.addAll(verticalShapes);
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

			solids.addAll(canardShapes);
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
			
			solids.addAll(wingFairingShapes);
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
			
			solids.addAll(canardFairingShapes);
		}
		
		_theAircraftSolidParts.addAll(solids);
	}
	
	public void exportCAD(String outputFolderPath) {
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT CAD FILE
		//---------------------------------------------------------------	
		String outputFileAbsolutePath = outputFolderPath + _theAircraft.getId().replaceAll("\\s", "");
		
		AircraftUtils.getAircraftFile(
				_theAircraftSolidParts, 
				outputFileAbsolutePath, 
				_theCADBuilderInterface.getFileExtension());
		
	}
	
	public void buildAircraftGroup() {
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT XFORM
		//---------------------------------------------------------------	
		
		// Instantiate the XFORM
		Xform aircraftXform = new Xform();
		
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
		aircraftGroup.getChildren().add(aircraftXform);
		world.getChildren().addAll(aircraftGroup);
	}
	
	private List<List<TriangleMesh>> generateTriangleMeshes() {
		
		//---------------------------------------------------------------
		// GENERATE A TRIANGLE MESH LIST FOR EACH CAD FACE
		//---------------------------------------------------------------	
		
		// Filter the solids
		List<OCCShape> solids = AircraftUtils.getAircraftSolid(_theAircraftSolidParts);
		
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
	
	public void buildCamera() {
		root.getChildren().add(cameraXform);
		cameraXform.getChildren().add(cameraXform2);
		cameraXform2.getChildren().add(cameraXform3);
		cameraXform3.getChildren().add(camera);
		cameraXform3.setRotateZ(180.0);
		
		camera.setNearClip(CAMERA_NEAR_CLIP);
		camera.setFarClip(CAMERA_FAR_CLIP);
		camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
		cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
		cameraXform.ry.setPivotX(_theAircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)/2.0); // TODO:
		cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
		cameraXform.rx.setPivotX(_theAircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)/2.0);
	}
	
	public void buildAxes() {
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
		
		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		axisGroup.setVisible(false);
		world.getChildren().addAll(axisGroup);
	}
	
	public void handleMouse(Scene scene, final Node root) {
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
					cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);
					cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);
				}
				else if (me.isSecondaryButtonDown()) {
					double z = camera.getTranslateZ();
					double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
					camera.setTranslateZ(newZ);
				}
				else if (me.isMiddleButtonDown()) {
					cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);
					cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);
				}
			}
		});
	}
	
	public void handleKeyboard(Scene scene, final Node root) {
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				switch (event.getCode()) {
				case Z:
					cameraXform2.t.setX(0.0);
					cameraXform2.t.setY(0.0);
					camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
					cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
					cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
					break;
				case X:
					axisGroup.setVisible(!axisGroup.isVisible());
					break;
				case V:
					aircraftGroup.setVisible(!aircraftGroup.isVisible());
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
	
	public List<OCCShape> getTheAircraftSolidParts() {
		return _theAircraftSolidParts;
	}
	
	public void setTheAircraftSolidParts(List<OCCShape> theAircraftSolidParts) {
		this._theAircraftSolidParts = theAircraftSolidParts;
	}
	
	public Group getTheFXRoot() {
		return root;
	}
	
	public Xform getTheFXWorld() {
		return world;
	}
	
	public PerspectiveCamera getTheCamera() {
		return camera;
	}
 	
	public class Xform extends Group {
		
		public Translate t  = new Translate();
		public Translate p  = new Translate();
		public Translate ip = new Translate();
		
		public Rotate rx = new Rotate();
		{ rx.setAxis(Rotate.X_AXIS); }		
		public Rotate ry = new Rotate();
		{ ry.setAxis(Rotate.Y_AXIS); }	
		public Rotate rz = new Rotate();
		{ rz.setAxis(Rotate.Z_AXIS); }
		
		public Scale s = new Scale();
		
		public Xform() {
			super();
			getTransforms().addAll(t, rz, ry, rx, s);
		}
		
		public Xform(RotateOrder rotateOrder) {
			
			switch (rotateOrder) {
			case XYZ:
				getTransforms().addAll(t, p, rz, ry, rx, s, ip);
				break;
			case XZY:
				getTransforms().addAll(t, p, ry, rz, rx, s, ip);
				break;
			case YXZ:
				getTransforms().addAll(t, p, rz, rx, ry, s, ip);
				break;
			case YZX:
				getTransforms().addAll(t, p, rx, rz, ry, s, ip);
				break;
			case ZXY:
				getTransforms().addAll(t, p, ry, rx, rz, s, ip);
				break;
			case ZYX:
				getTransforms().addAll(t, p, rx, ry, rz, s, ip);
				break;
			}
		}
		
		public void setTranslate(double x, double y, double z) {
			t.setX(x);
			t.setY(y);
			t.setZ(z);
		}
		
		public void setTranslate(double x, double y) {
			t.setX(x);
			t.setY(y);
		}
		
		public void setTx(double x) { t.setX(x); }
		public void setTy(double y) { t.setY(y); }
		public void setTz(double z) { t.setZ(z); }
		
		public void setRotate(double x, double y, double z) {
			rx.setAngle(x);
			ry.setAngle(y);
			rz.setAngle(z);
		}
		
		public void setRotateX(double x) { rx.setAngle(x); }
		public void setRotateY(double y) { ry.setAngle(y); }
		public void setRotateZ(double z) { rz.setAngle(z); }
		
		public void setScale(double scaleFactor) {
			s.setX(scaleFactor);
			s.setY(scaleFactor);
			s.setZ(scaleFactor);
		}
		
		public void setSx(double x) { s.setX(x); }
		public void setSy(double y) { s.setY(y); }
		public void setSz(double z) { s.setZ(z); }
		
		public void setPivot(double x, double y, double z) {
			p.setX(x);
			p.setY(y);
			p.setZ(z);
			ip.setX(-x);
			ip.setY(-y);
			ip.setZ(-z);
		}
		
		public void reset() {
			t.setX(0.0);
			t.setY(0.0);
			t.setZ(0.0);
			rx.setAngle(0.0);
			ry.setAngle(0.0);
			rz.setAngle(0.0);
			s.setX(0.0);
			s.setY(0.0);
			s.setZ(0.0);
			p.setX(0.0);
			p.setY(0.0);
			p.setZ(0.0);
			ip.setX(0.0);
			ip.setY(0.0);
			ip.setZ(0.0);
		}
		
		public void resetTSP() {
			t.setX(0.0);
			t.setY(0.0);
			t.setZ(0.0);
			s.setX(0.0);
			s.setY(0.0);
			s.setZ(0.0);
			p.setX(0.0);
			p.setY(0.0);
			p.setZ(0.0);
			ip.setX(0.0);
			ip.setY(0.0);
			ip.setZ(0.0);
		}
		
		public void debug() {
			System.out.println("t = (" + 
							   t.getX() + ", " + 
							   t.getY() + ", " + 
							   t.getZ() + ")  " + 
							   "r = (" + 
							   rx.getAngle() + ", " + 
							   ry.getAngle() + ", " + 
							   rz.getAngle() + ")  " + 
							   "s = (" + 
							   s.getX() + ", " + 
							   s.getY() + ", " + 
							   s.getZ() + ")  " +
							   "p = (" + 
							   p.getX() + ", " + 
							   p.getY() + ", " + 
							   p.getZ() + ")  " +
							   "ip = (" + 
							   ip.getX() + ", " + 
							   ip.getY() + ", " + 
							   ip.getZ() + ")");
		}
				
	}
	
	public enum RotateOrder {
		XYZ, XZY, YXZ, YZX, ZXY, ZYX
	}
	
}

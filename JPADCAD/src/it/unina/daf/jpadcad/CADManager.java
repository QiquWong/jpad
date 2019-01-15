package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.occfx.OCCFXForm;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.XSpacingType;
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
		Boolean generateHTail;
		Boolean generateVTail;
		Boolean generateCanard;
		Boolean generateWingFairing;
		Boolean generateCanardFairing;
		
		String generateFuselageString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//fuselage/@generate");
		
		String generateWingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing/@generate");
		
		String generateHTailString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//horizontal/@generate");
		
		String generateVTailString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//vertical/@generate");
		
		String generateCanardString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard/@generate");
		
		String generateWingFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//wing_fairing/@generate");
		
		String generateCanardFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//canard_fairing/@generate");
		
		generateFuselage = (generateFuselageString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWing = (generateWingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateHTail = (generateHTailString.equalsIgnoreCase("TRUE")) ? true : false;
		generateVTail = (generateVTailString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanard = (generateCanardString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWingFairing = (generateWingFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanardFairing = (generateCanardFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		
		//---------------------------------------------------------------
		// FUSELAGE CAD OPERATIONS
		//---------------------------------------------------------------
		
		// Initialize FUSELAGE CAD parameters
		int numberNoseTrunkSections = 7;
		XSpacingType spacingTypeNoseTrunk = XSpacingType.COSINUS;
		int numberTailTrunkSections = 7;
		XSpacingType spacingTypeTailTrunk = XSpacingType.COSINUS;
		
		// Read FUSELAGE CAD parameters from the XML file
		if (generateFuselage && (theAircraft.getFuselage() != null)) { 
			
			String numberNoseTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseTrunkSections");
			if (numberNoseTrunkSectionsString != null) 
				numberNoseTrunkSections = Integer.valueOf(numberNoseTrunkSectionsString);
			
			String spacingTypeNoseTrunkString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//fuselage/numberNoseTrunkSections/@spacing");
			if (spacingTypeNoseTrunkString != null && spacingTypeNoseTrunkString != "") 
				spacingTypeNoseTrunk = XSpacingType.valueOf(spacingTypeNoseTrunkString);
			
			String numberTailTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailTrunkSections");
			if (numberTailTrunkSectionsString != null) 
				numberTailTrunkSections = Integer.valueOf(numberTailTrunkSectionsString);
			
			String spacingTypeTailTrunkString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//fuselage/numberTailTrunkSections/@spacing");
			if (spacingTypeTailTrunkString != null && spacingTypeTailTrunkString != "") 
				spacingTypeTailTrunk = XSpacingType.valueOf(spacingTypeTailTrunkString);
			
		} else {
			
			generateFuselage = false;			
		}
		
		//---------------------------------------------------------------
		// WING CAD OPERATIONS
		//---------------------------------------------------------------
		
		// Initialize WING CAD parameters
		WingTipType wingTipType = WingTipType.CUTOFF;
		
		double wingletYOffsetFactor = 0.50;
		double wingletXOffsetFactor = 0.75;
		double wingletTaperRatio = 0.20;
		
		// Read WING CAD parameters from the XML file
		if (generateWing && (theAircraft.getWing() != null)) {
			
			String wingTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//wing/@tipType");
			if (wingTipTypeString != null && wingTipTypeString != "")
				wingTipType = WingTipType.valueOf(wingTipTypeString);
			
			if (wingTipType.equals(WingTipType.WINGLET)) {
				
				String wingletYOffsetFactorString = reader.getXMLPropertyByPath("//wing/wingletParams/yOffsetFactor");
				if (wingletYOffsetFactorString != null) 
					wingletYOffsetFactor = Double.valueOf(wingletYOffsetFactorString);
					
				String wingletXOffsetFactorString = reader.getXMLPropertyByPath("//wing/wingletParams/xOffsetFactor");
				if (wingletXOffsetFactorString != null) 
					wingletXOffsetFactor = Double.valueOf(wingletXOffsetFactorString);
				
				String wingletTaperRatioString = reader.getXMLPropertyByPath("//wing/wingletParams/taperRatio");
				if (wingletTaperRatioString != null) 
					wingletTaperRatio = Double.valueOf(wingletTaperRatioString);
								
			}		
			
		} else {
			
			generateWing = false;
		}
		
		//---------------------------------------------------------------
		// HORIZONTAL TAIL CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize HORIZONTAL TAIL CAD parameters
		WingTipType hTailTipType = WingTipType.CUTOFF;

		// Read HORIZONTAL TAIL CAD parameters from the XML file
		if (generateHTail && (theAircraft.getHTail() != null)) {
			
			String hTailTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//horizontal/@tipType");
			if (hTailTipTypeString != null && hTailTipTypeString != "")
				hTailTipType = WingTipType.valueOf(hTailTipTypeString);

		} else {
			
			generateHTail = false;
		}
		
		//---------------------------------------------------------------
		// VERTICAL TAIL CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize VERTICAL TAIL CAD parameters
		WingTipType vTailTipType = WingTipType.CUTOFF;

		// Read VERTICAL TAIL CAD parameters from the XML file
		if (generateVTail && (theAircraft.getVTail() != null)) { 

			String vTailTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//vertical/@tipType");
			if (vTailTipTypeString != null && vTailTipTypeString != "")
				vTailTipType = WingTipType.valueOf(vTailTipTypeString);
			
		} else {
			
			generateVTail = false;
		}
		
		//---------------------------------------------------------------
		// CANARD CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize CANARD CAD parameters
		WingTipType canardTipType = WingTipType.CUTOFF;
		
		// Read vertical tail CAD parameters from the XML file
		if (generateCanard && (theAircraft.getCanard() != null)) { 

			String canardTipTypeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//canard/@tipType");
			if (canardTipTypeString != null && canardTipTypeString != "")
				canardTipType = WingTipType.valueOf(canardTipTypeString);
			
		} else {
			
			generateCanard = false;
		}
		
		//---------------------------------------------------------------
		// WING-FUSELAGE FAIRING CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize WING FAIRING CAD parameters
		double wingFairingFrontLengthFactor = 1.00;
		double wingFairingBackLengthFactor = 1.00;
		double wingFairingWidthFactor = 0.75;
		double wingFairingHeightFactor = 0.15;
		double wingFairingHeightBelowReferenceFactor = 0.60;
		double wingFairingHeightAboveReferenceFactor = 0.45;
		double wingFairingFilletRadiusFactor = 0.80;

		// Read WING FAIRING CAD parameters from the XML file
		if (generateWingFairing && (theAircraft.getWing() != null) && (theAircraft.getFuselage() != null)) { 

			String wingFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/frontLengthFactor");
			if (wingFairingFrontLengthFactorString != null)
				wingFairingFrontLengthFactor = Double.valueOf(wingFairingFrontLengthFactorString);
			
			String wingFairingBackLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/backLengthFactor");
			if (wingFairingBackLengthFactorString != null)
				wingFairingBackLengthFactor = Double.valueOf(wingFairingBackLengthFactorString);
			
			String wingFairingWidthFactorString = reader.getXMLPropertyByPath("//wing_fairing/widthFactor");
			if (wingFairingWidthFactorString != null)
				wingFairingWidthFactor = Double.valueOf(wingFairingWidthFactorString);
			
			String wingFairingHeightFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightFactor");
			if (wingFairingHeightFactorString != null)
				wingFairingHeightFactor = Double.valueOf(wingFairingHeightFactorString);
			
			String wingFairingHeightBelowReferenceFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightBelowReferenceFactor");
			if (wingFairingHeightBelowReferenceFactorString != null)
				wingFairingHeightBelowReferenceFactor = Double.valueOf(wingFairingHeightBelowReferenceFactorString);
			
			String wingFairingHeightAboveReferenceFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightAboveReferenceFactor");
			if (wingFairingHeightAboveReferenceFactorString != null)
				wingFairingHeightAboveReferenceFactor = Double.valueOf(wingFairingHeightAboveReferenceFactorString);
			
			String wingFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//wing_fairing/filletRadiusFactor");
			if (wingFairingFilletRadiusFactorString != null)
				wingFairingFilletRadiusFactor = Double.valueOf(wingFairingFilletRadiusFactorString);
			
		} else {
			
			generateWingFairing = false;
		}
		
		//---------------------------------------------------------------
		// CANARD-FUSELAGE FAIRING CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize CANARD FAIRING CAD parameters
		double canardFairingFrontLengthFactor = 1.00;
		double canardFairingBackLengthFactor = 1.00;
		double canardFairingWidthFactor = 0.55;
		double canardFairingHeightFactor = 0.15;
		double canardFairingHeightBelowReferenceFactor = 0.70;
		double canardFairingHeightAboveReferenceFactor = 0.45;
		double canardFairingFilletRadiusFactor = 0.80;

		// Read CANARD FAIRING CAD parameters from the XML file
		if (generateCanardFairing && (theAircraft.getCanard() != null) && (theAircraft.getFuselage() != null)) { 

			String canardFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/frontLengthFactor");
			if (canardFairingFrontLengthFactorString != null)
				canardFairingFrontLengthFactor = Double.valueOf(canardFairingFrontLengthFactorString);
			
			String canardFairingBackLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/backLengthFactor");
			if (canardFairingBackLengthFactorString != null)
				canardFairingBackLengthFactor = Double.valueOf(canardFairingBackLengthFactorString);
			
			String canardFairingWidthFactorString = reader.getXMLPropertyByPath("//canard_fairing/widthFactor");
			if (canardFairingWidthFactorString != null)
				canardFairingWidthFactor = Double.valueOf(canardFairingWidthFactorString);
			
			String canardFairingHeightFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightFactor");
			if (canardFairingHeightFactorString != null)
				canardFairingHeightFactor = Double.valueOf(canardFairingHeightFactorString);
			
			String canardFairingHeightBelowReferenceFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightBelowReferenceFactor");
			if (canardFairingHeightBelowReferenceFactorString != null)
				canardFairingHeightBelowReferenceFactor = Double.valueOf(canardFairingHeightBelowReferenceFactorString);
			
			String canardFairingHeightAboveReferenceFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightAboveReferenceFactor");
			if (canardFairingHeightAboveReferenceFactorString != null)
				canardFairingHeightAboveReferenceFactor = Double.valueOf(canardFairingHeightAboveReferenceFactorString);
			
			String canardFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//canard_fairing/filletRadiusFactor");
			if (canardFairingFilletRadiusFactorString != null)
				canardFairingFilletRadiusFactor = Double.valueOf(canardFairingFilletRadiusFactorString);
			
		} else {
			
			generateCanardFairing = false;
		}
		
		// EXPORT TO FILE OPTIONS
		Boolean exportToFile;
		
		FileExtension fileExtension = FileExtension.STEP;
		Boolean exportWireframe = false;

		String exportToFileString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@value");
		exportToFile = exportToFileString.equalsIgnoreCase("TRUE") ? true : false;

		if (exportToFile) {
			
			String fileExtensionString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@format");
			if (fileExtensionString != null && fileExtensionString != "")
			fileExtension = FileExtension.valueOf(fileExtensionString);
			
			String exportWireframeString = MyXMLReaderUtils.getXMLPropertyByPath(
					reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@exportWireframe");
			exportWireframe = exportWireframeString.equalsIgnoreCase("TRUE") ? true : false;
			
		}		
		
		//---------------------------------------------------------------
		// GENERATE THE CAD MANAGER INTERFACE
		//---------------------------------------------------------------
		ICADManager theCADBuilderInterface = new ICADManager.Builder()
				.setGenerateFuselage(generateFuselage)
				.setGenerateWing(generateWing)
				.setGenerateHTail(generateHTail)
				.setGenerateVTail(generateVTail)
				.setGenerateCanard(generateCanard)
				.setGenerateWingFairing(generateWingFairing)
				.setGenerateCanardFairing(generateCanardFairing)
				.setSpacingTypeNoseTrunk(spacingTypeNoseTrunk)
				.setNumberNoseTrunkSections(numberNoseTrunkSections)
				.setSpacingTypeTailTrunk(spacingTypeTailTrunk)
				.setNumberTailTrunkSections(numberTailTrunkSections)			
				.setWingTipType(wingTipType)
				.setWingletYOffsetFactor(wingletYOffsetFactor)
				.setWingletXOffsetFactor(wingletXOffsetFactor)
				.setWingletTaperRatio(wingletTaperRatio)
				.setHTailTipType(hTailTipType)
				.setVTailTipType(vTailTipType)
				.setCanardTipType(canardTipType)
				.setWingFairingFrontLengthFactor(wingFairingFrontLengthFactor)
				.setWingFairingBackLengthFactor(wingFairingBackLengthFactor)
				.setWingFairingWidthFactor(wingFairingWidthFactor)
				.setWingFairingHeightFactor(wingFairingHeightFactor)
				.setWingFairingHeightBelowReferenceFactor(wingFairingHeightBelowReferenceFactor)
				.setWingFairingHeightAboveReferenceFactor(wingFairingHeightAboveReferenceFactor)
				.setWingFairingFilletRadiusFactor(wingFairingFilletRadiusFactor)
				.setCanardFairingFrontLengthFactor(canardFairingFrontLengthFactor)
				.setCanardFairingBackLengthFactor(canardFairingBackLengthFactor)
				.setCanardFairingWidthFactor(canardFairingWidthFactor)
				.setCanardFairingHeightFactor(canardFairingHeightFactor)
				.setCanardFairingHeightBelowReferenceFactor(canardFairingHeightBelowReferenceFactor)
				.setCanardFairingHeightAboveReferenceFactor(canardFairingHeightAboveReferenceFactor)
				.setCanardFairingFilletRadiusFactor(canardFairingFilletRadiusFactor)
				.setExportToFile(exportToFile)
				.setFileExtension(fileExtension)
				.setExportWireframe(exportWireframe)
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
			stringBuilder.append("\tNose trunk spacing: " + _theCADBuilderInterface.getSpacingTypeNoseTrunk().toString() + ".\n")
						 .append("\tNumber of nose trunk sections: " + _theCADBuilderInterface.getNumberNoseTrunkSections() + ".\n")
						 .append("\tTail trunk spacing: " + _theCADBuilderInterface.getSpacingTypeTailTrunk().toString() + ".\n")
						 .append("\tNumber of tail trunk sections: " + _theCADBuilderInterface.getNumberTailTrunkSections() + ".\n")					
						 .append("\n");					 
		
		stringBuilder.append("\t[Generate Wing CAD]: " + _theCADBuilderInterface.getGenerateWing() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateWing())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + _theCADBuilderInterface.getWingTipType().toString() + "\n");
		if (_theCADBuilderInterface.getWingTipType().equals(WingTipType.WINGLET))
			stringBuilder.append("\tWinglet Y offset factor: " + _theCADBuilderInterface.getWingletYOffsetFactor() + ".\n")
						 .append("\tWinglet X offset factor: " + _theCADBuilderInterface.getWingletXOffsetFactor() + ".\n")
						 .append("\tWinglet taper ratio: " + _theCADBuilderInterface.getWingletTaperRatio() + ".\n")
						 .append("\n");
		else 
			stringBuilder.append("\n");
		
		stringBuilder.append("\t[Generate Horizontal Tail CAD]: " + _theCADBuilderInterface.getGenerateHTail() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateHTail())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + _theCADBuilderInterface.getHTailTipType().toString() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Vertical Tail CAD]: " + _theCADBuilderInterface.getGenerateVTail() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateVTail())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + _theCADBuilderInterface.getVTailTipType().toString() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Canard CAD]: " + _theCADBuilderInterface.getGenerateCanard() + ".\n");	
		if (!_theCADBuilderInterface.getGenerateCanard())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tTip type: " + _theCADBuilderInterface.getCanardTipType().toString() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Wing-Fuselage Fairing CAD]: " + _theCADBuilderInterface.getGenerateWingFairing() + ".\n");
		if (!_theCADBuilderInterface.getGenerateWingFairing()) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFront length factor: " + _theCADBuilderInterface.getWingFairingFrontLengthFactor() + ".\n")
						 .append("\tBack length factor: " + _theCADBuilderInterface.getWingFairingBackLengthFactor() + ".\n")
						 .append("\tWidth factor: " + _theCADBuilderInterface.getWingFairingWidthFactor() + ".\n")
						 .append("\tHeight factor: " + _theCADBuilderInterface.getWingFairingHeightFactor() + ".\n")
						 .append("\tHeight below reference factor: " + _theCADBuilderInterface.getWingFairingHeightBelowReferenceFactor() + ".\n")
						 .append("\tHeight above reference factor: " + _theCADBuilderInterface.getWingFairingHeightAboveReferenceFactor() + ".\n")
						 .append("\tFillet radius factor: " + _theCADBuilderInterface.getWingFairingFilletRadiusFactor() + ".\n")
						 .append("\n");
		
		stringBuilder.append("\t[Generate Canard/Fuselage Fairing CAD]: " + _theCADBuilderInterface.getGenerateCanardFairing() + ".\n");
		if (!_theCADBuilderInterface.getGenerateCanardFairing()) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFront length factor: " + _theCADBuilderInterface.getCanardFairingFrontLengthFactor() + ".\n")
						 .append("\tRear length factor: " + _theCADBuilderInterface.getCanardFairingBackLengthFactor() + ".\n")
						 .append("\tWidth factor: " + _theCADBuilderInterface.getCanardFairingWidthFactor() + ".\n")
						 .append("\tHeight factor: " + _theCADBuilderInterface.getCanardFairingHeightFactor() + ".\n")
						 .append("\tHeight below reference factor: " + _theCADBuilderInterface.getCanardFairingHeightBelowReferenceFactor() + ".\n")
						 .append("\tHeight above reference factor: " + _theCADBuilderInterface.getCanardFairingHeightAboveReferenceFactor() + ".\n")
						 .append("\tFillet radius factor: " + _theCADBuilderInterface.getCanardFairingFilletRadiusFactor() + ".\n")
						 .append("\n");
				
		stringBuilder.append("\t[Export shapes to file]: " + _theCADBuilderInterface.getExportToFile() + ".\n");
		if (!_theCADBuilderInterface.getExportToFile())
			stringBuilder.append("\n");
		else
			stringBuilder.append("\tFile format: " + _theCADBuilderInterface.getFileExtension().toString() + ".\n")
						 .append("\tExport wireframe: " + _theCADBuilderInterface.getExportWireframe() + ".\n")
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
		
		if (_theCADBuilderInterface.getGenerateHTail()) {
			_theAircraftSolidsMap.put(CADComponentEnum.HORIZONTAL, new ArrayList<OCCShape>());
		}
		
		if (_theCADBuilderInterface.getGenerateVTail()) {
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
			
			List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
					_theAircraft.getFuselage(), 			
					_theCADBuilderInterface.getNumberNoseTrunkSections(), 
					_theCADBuilderInterface.getSpacingTypeNoseTrunk(), 
					_theCADBuilderInterface.getNumberTailTrunkSections(), 
					_theCADBuilderInterface.getSpacingTypeTailTrunk(), 
					_theCADBuilderInterface.getExportWireframe(), 
					false, 
					true
					);
			
			_theAircraftShapes.addAll(fuselageShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.FUSELAGE).addAll(AircraftCADUtils.filterAircraftPartSolids(fuselageShapes));
		}
		
		// WING
		if (_theCADBuilderInterface.getGenerateWing()) {

			List<OCCShape> wingShapes = new ArrayList<>();
			
			if (_theCADBuilderInterface.getWingTipType().equals(WingTipType.WINGLET)) 
				
				wingShapes.addAll(AircraftCADUtils.getLiftingSurfaceWingletCAD(
						_theAircraft.getWing(), 
						_theCADBuilderInterface.getWingletYOffsetFactor(), 
						_theCADBuilderInterface.getWingletXOffsetFactor(), 
						_theCADBuilderInterface.getWingletTaperRatio(), 
						_theCADBuilderInterface.getExportWireframe(), 
						false, 
						true
						));
			
			else
				
				wingShapes.addAll(AircraftCADUtils.getLiftingSurfaceCAD(
						_theAircraft.getWing(), 
						_theCADBuilderInterface.getWingTipType(), 
						_theCADBuilderInterface.getExportWireframe(), 
						false, 
						true
						));

			_theAircraftShapes.addAll(wingShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.WING).addAll(AircraftCADUtils.filterAircraftPartSolids(wingShapes));
		}
		
		// HORIZONTAL
		if (_theCADBuilderInterface.getGenerateHTail()) {

			List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
					_theAircraft.getHTail(), 
					_theCADBuilderInterface.getHTailTipType(), 
					_theCADBuilderInterface.getExportWireframe(), 
					false, 
					true
					);

			_theAircraftShapes.addAll(hTailShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.HORIZONTAL).addAll(AircraftCADUtils.filterAircraftPartSolids(hTailShapes));
		}
		
		// VERTICAL
		if (_theCADBuilderInterface.getGenerateVTail()) {

			List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
					_theAircraft.getVTail(), 
					_theCADBuilderInterface.getVTailTipType(), 
					_theCADBuilderInterface.getExportWireframe(), 
					false, 
					true
					);

			_theAircraftShapes.addAll(vTailShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.VERTICAL).addAll(AircraftCADUtils.filterAircraftPartSolids(vTailShapes));
		}
		
		// CANARD
		if (_theCADBuilderInterface.getGenerateCanard()) {

			List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
					_theAircraft.getCanard(), 
					_theCADBuilderInterface.getCanardTipType(), 
					_theCADBuilderInterface.getExportWireframe(), 
					false, 
					true
					);

			_theAircraftShapes.addAll(canardShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.CANARD).addAll(AircraftCADUtils.filterAircraftPartSolids(canardShapes));
		}
		
		// WING-FUSELAGE FAIRING 
		if (_theCADBuilderInterface.getGenerateWingFairing()) {
			
			List<OCCShape> wingFairingShapes = AircraftCADUtils.getFairingShapes(
					_theAircraft.getFuselage(), 
					_theAircraft.getWing(), 
					_theCADBuilderInterface.getWingFairingFrontLengthFactor(), 
					_theCADBuilderInterface.getWingFairingBackLengthFactor(), 
					_theCADBuilderInterface.getWingFairingWidthFactor(), 
					_theCADBuilderInterface.getWingFairingHeightFactor(), 
					_theCADBuilderInterface.getWingFairingHeightBelowReferenceFactor(), 
					_theCADBuilderInterface.getWingFairingHeightAboveReferenceFactor(), 
					_theCADBuilderInterface.getWingFairingFilletRadiusFactor(), 
					_theCADBuilderInterface.getExportWireframe(), 
					false, 
					true
					);
			
			_theAircraftShapes.addAll(wingFairingShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.WING_FAIRING).addAll(AircraftCADUtils.filterAircraftPartSolids(wingFairingShapes));
		}
		
		// CANARD/FUSELAGE FAIRING
		if (_theCADBuilderInterface.getGenerateCanardFairing()) {
			
			List<OCCShape> canardFairingShapes = AircraftCADUtils.getFairingShapes(
					_theAircraft.getFuselage(), 
					_theAircraft.getCanard(), 
					_theCADBuilderInterface.getCanardFairingFrontLengthFactor(), 
					_theCADBuilderInterface.getCanardFairingBackLengthFactor(), 
					_theCADBuilderInterface.getCanardFairingWidthFactor(), 
					_theCADBuilderInterface.getCanardFairingHeightFactor(), 
					_theCADBuilderInterface.getCanardFairingHeightBelowReferenceFactor(), 
					_theCADBuilderInterface.getCanardFairingHeightAboveReferenceFactor(), 
					_theCADBuilderInterface.getCanardFairingFilletRadiusFactor(), 
					_theCADBuilderInterface.getExportWireframe(), 
					false, 
					true
					);
			
			_theAircraftShapes.addAll(canardFairingShapes);
			_theAircraftSolidsMap.get(CADComponentEnum.CANARD_FAIRING).addAll(AircraftCADUtils.filterAircraftPartSolids(canardFairingShapes));
		}
	}
	
	public void exportCAD(String outputFolderPath) {
		
		exportCADImplementation(outputFolderPath + _theAircraft.getId().replaceAll("\\s", ""));	
	}
	
	public void exportCAD(String outputFolderPath, String fileName) {
		
		exportCADImplementation(outputFolderPath + fileName);
	}
	
	private void exportCADImplementation(String outputFileAbsolutePath) {
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT CAD FILE
		//---------------------------------------------------------------	
		OCCUtils.write(
				outputFileAbsolutePath, 
				_theCADBuilderInterface.getFileExtension(), 
				_theAircraftShapes);
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
		_theCameraXform.ry.setPivotX(_theAircraft.getFuselage().getFuselageLength().doubleValue(SI.METER)/2.0);
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

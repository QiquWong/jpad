package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import aircraft.Aircraft;
import it.unina.daf.jpadcad.occ.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftUtils.XSpacingType;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class CADManager {
	
	public ICADManager _theCADBuilderInterface;
	
	private Aircraft _theAircraft;
	private List<OCCShape> _theAircraftSolidParts;
	
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
				.append("\n\n\t-----------------------------\n")
				.append("\tCAD generator input configuration\n")
				.append("\t-----------------------------\n\n");
		
		stringBuilder.append("\tGenerate Fuselage CAD: " + _theCADBuilderInterface.getGenerateFuselage() + ".");
		if (!_theCADBuilderInterface.getGenerateFuselage()) 		
			stringBuilder.append("\n");	
		else			
			stringBuilder.append("Nose cap section factor #1: " + _theCADBuilderInterface.getNoseCapSectionFactor1() + ".")
						 .append("Nose cap section factor #2: " + _theCADBuilderInterface.getNoseCapSectionFactor2() + ".")
						 .append("Number of nose cap sections: " + _theCADBuilderInterface.getNumberNoseCapSections() + ".")
						 .append("Number of nose trunk sections: " + _theCADBuilderInterface.getNumberNoseTrunkSections() + ".")
						 .append("Nose trunk spacing: " + _theCADBuilderInterface.getSpacingTypeNoseTrunk().toString() + ".")
						 .append("Number of tail trunk sections: " + _theCADBuilderInterface.getNumberTailTrunkSections() + ".")
						 .append("Tail trunk spacing: " + _theCADBuilderInterface.getSpacingTypeTailTrunk().toString() + ".")
						 .append("Tail cap section factor #1: " + _theCADBuilderInterface.getTailCapSectionFactor1() + ".")
						 .append("Tail cap section factor #2: " + _theCADBuilderInterface.getTailCapSectionFactor2() + ".")
						 .append("Number of tail cap sections: " + _theCADBuilderInterface.getNumberTailCapSections() + ".")
						 .append("Support shapes export: " + _theCADBuilderInterface.getExportFuselageSupportShapes() + ".")
						 .append("\n");					 
		
		stringBuilder.append("\tGenerate Wing CAD: " + _theCADBuilderInterface.getGenerateWing() + ".");	
		if (!_theCADBuilderInterface.getGenerateWing())
			stringBuilder.append("\n");
		else
			stringBuilder.append("Tip tolerance: " + _theCADBuilderInterface.getWingTipTolerance() + ".")
						 .append("Support shapes export: " + _theCADBuilderInterface.getExportWingSupportShapes() + ".")
						 .append("\n");
		
		stringBuilder.append("\tGenerate Horizontal Tail CAD: " + _theCADBuilderInterface.getGenerateHorizontal() + ".");	
		if (!_theCADBuilderInterface.getGenerateHorizontal())
			stringBuilder.append("\n");
		else
			stringBuilder.append("Tip tolerance: " + _theCADBuilderInterface.getHorizontalTipTolerance() + ".")
						 .append("Support shapes export: " + _theCADBuilderInterface.getExportHorizontalSupportShapes() + ".")
						 .append("\n");
		
		stringBuilder.append("\tGenerate Vertical Tail CAD: " + _theCADBuilderInterface.getGenerateVertical() + ".");	
		if (!_theCADBuilderInterface.getGenerateVertical())
			stringBuilder.append("\n");
		else
			stringBuilder.append("Tip tolerance: " + _theCADBuilderInterface.getVerticalTipTolerance() + ".")
						 .append("Support shapes export: " + _theCADBuilderInterface.getExportVerticalSupportShapes() + ".")
						 .append("\n");
		
		stringBuilder.append("\tGenerate Canard CAD: " + _theCADBuilderInterface.getGenerateCanard() + ".");	
		if (!_theCADBuilderInterface.getGenerateCanard())
			stringBuilder.append("\n");
		else
			stringBuilder.append("Tip tolerance: " + _theCADBuilderInterface.getCanardTipTolerance() + ".")
						 .append("Support shapes export: " + _theCADBuilderInterface.getExportCanardSupportShapes() + ".")
						 .append("\n");
		
		stringBuilder.append("\tGenerate Wing/Fuselage Fairing CAD: " + _theCADBuilderInterface.getGenerateWingFairing() + ".");
		if (!_theCADBuilderInterface.getGenerateWingFairing()) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("Front length factor: " + _theCADBuilderInterface.getWingFairingFrontLengthFactor() + ".")
						 .append("Rear length factor: " + _theCADBuilderInterface.getWingFairingBackLengthFactor() + ".")
						 .append("Side size factor: " + _theCADBuilderInterface.getWingFairingSideSizeFactor() + ".")
						 .append("Height factor: " + _theCADBuilderInterface.getWingFairingHeightFactor() + ".")
						 .append("Height below contact factor: " + _theCADBuilderInterface.getWingFairingHeightBelowContactFactor() + ".")
						 .append("Height above contact factor: " + _theCADBuilderInterface.getWingFairingHeightAboveContactFactor() + ".")
						 .append("Fillet radius factor: " + _theCADBuilderInterface.getWingFairingFilletRadiusFactor() + ".")
						 .append("\n");
		
		stringBuilder.append("\tGenerate Canard/Fuselage Fairing CAD: " + _theCADBuilderInterface.getGenerateCanardFairing() + ".");
		if (!_theCADBuilderInterface.getGenerateCanardFairing()) 
			stringBuilder.append("\n");
		else
			stringBuilder.append("Front length factor: " + _theCADBuilderInterface.getCanardFairingFrontLengthFactor() + ".")
						 .append("Rear length factor: " + _theCADBuilderInterface.getCanardFairingBackLengthFactor() + ".")
						 .append("Side size factor: " + _theCADBuilderInterface.getCanardFairingSideSizeFactor() + ".")
						 .append("Height factor: " + _theCADBuilderInterface.getCanardFairingHeightFactor() + ".")
						 .append("Height below contact factor: " + _theCADBuilderInterface.getCanardFairingHeightBelowContactFactor() + ".")
						 .append("Height above contact factor: " + _theCADBuilderInterface.getCanardFairingHeightAboveContactFactor() + ".")
						 .append("Fillet radius factor: " + _theCADBuilderInterface.getCanardFairingFilletRadiusFactor() + ".")
						 .append("\n");
				
		stringBuilder.append("\tExport shapes to file: " + _theCADBuilderInterface.getExportToFile() + ".");
		if (!_theCADBuilderInterface.getExportToFile())
			stringBuilder.append("\n");
		else
			stringBuilder.append("File format: " + _theCADBuilderInterface.getFileExtension().toString() + ".")
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
					false, 
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
		AircraftUtils.getAircraftFile(
				_theAircraftSolidParts, 
				_theAircraft.getId().replaceAll("\\s", ""), 
				_theCADBuilderInterface.getFileExtension());
		
	}
	
	public Group generateFXGroup() {
		
		//---------------------------------------------------------------
		// GENERATE THE AIRCRAFT FX GROUP
		//---------------------------------------------------------------	
		
		// Instantiate the group
		Group aircraftComponents = new Group();
		aircraftComponents.setDepthTest(DepthTest.ENABLE);
		
		// Populate the group
		List<MeshView> faces = new ArrayList<>();
		
		faces.addAll(generateTriangleMeshes().stream()
				.flatMap(List::stream)
				.map(m -> {
					MeshView face = new MeshView(m);
					face.setDrawMode(DrawMode.FILL);
					return face;
				})
				.collect(Collectors.toList()));
		
		aircraftComponents.getChildren().addAll(faces);				
		
		return aircraftComponents;	
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
}

package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;

import aircraft.Aircraft;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.utils.AircraftUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftUtils.XSpacingType;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class CADManager {
	
	public ICADManager _theCADManagerInterface;
	
	public Aircraft _theAircraft;
	public List<OCCSolid> _theAircaftSolidParts;
	
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
		
		// Initialize fuselage CAD parameters
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
		
		// Read fuselage CAD parameters from the xml file
		if (generateFuselage && !(theAircraft.getFuselage() == null)) { 
			
			String noseCapSectionFactor1String = reader.getXMLPropertyByPath("//fuselage/noseCapSectionFactor1");
			if (!(noseCapSectionFactor1String == null)) 
				noseCapSectionFactor1 = Double.valueOf(noseCapSectionFactor1String);
			
			String noseCapSectionFactor2String = reader.getXMLPropertyByPath("//fuselage/noseCapSectionFactor2");
			if (!(noseCapSectionFactor2String == null)) 
				noseCapSectionFactor2 = Double.valueOf(noseCapSectionFactor2String);
			
			String numberNoseCapSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseCapSections");
			if (!(numberNoseCapSectionsString == null)) 
				numberNoseCapSections = Integer.valueOf(numberNoseCapSectionsString);
			
			String numberNoseTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberNoseTrunkSections");
			if (!(numberNoseTrunkSectionsString == null)) 
				numberNoseTrunkSections = Integer.valueOf(numberNoseTrunkSectionsString);
			
			String spacingTypeNoseTrunkString = reader.getXMLPropertyByPath("//fuselage/spacingTypeNoseTrunk");
			if (!(spacingTypeNoseTrunkString == null)) 
				spacingTypeNoseTrunk = XSpacingType.valueOf(spacingTypeNoseTrunkString);
			
			String numberTailTrunkSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailTrunkSections");
			if (!(numberTailTrunkSectionsString == null)) 
				numberTailTrunkSections = Integer.valueOf(numberTailTrunkSectionsString);
			
			String spacingTypeTailTrunkString = reader.getXMLPropertyByPath("//fuselage/spacingTypeTailTrunk");
			if (!(spacingTypeTailTrunkString == null)) 
				spacingTypeTailTrunk = XSpacingType.valueOf(spacingTypeTailTrunkString);
			
			String tailCapSectionFactor1String = reader.getXMLPropertyByPath("//fuselage/tailCapSectionFactor1");
			if (!(tailCapSectionFactor1String == null)) 
				tailCapSectionFactor1 = Double.valueOf(tailCapSectionFactor1String);
			
			String tailCapSectionFactor2String = reader.getXMLPropertyByPath("//fuselage/tailCapSectionFactor2");
			if (!(tailCapSectionFactor2String == null)) 
				tailCapSectionFactor2 = Double.valueOf(tailCapSectionFactor2String);
			
			String numberTailCapSectionsString = reader.getXMLPropertyByPath("//fuselage/numberTailCapSections");
			if (!(numberTailCapSectionsString == null)) 
				numberTailCapSections = Integer.valueOf(numberTailCapSectionsString);
			
		}
		
		//---------------------------------------------------------------
		// WING CAD OPERATIONS
		//---------------------------------------------------------------
		
		// Initialize wing CAD parameters
		double wingTipTolerance = 1e-3;
		boolean exportWingSupportShapes = false;
		
		// Read wing CAD parameters from the xml file
		if (generateWing && !(theAircraft.getWing() == null)) {
			
			String wingTipToleranceString = reader.getXMLPropertyByPath("//wing/tipTolerance");
			if (!(wingTipToleranceString == null))
				wingTipTolerance = Double.valueOf(wingTipToleranceString);
			
			String exportWingSupportShapesString = reader.getXMLPropertyByPath("//wing/exportSupportShapes");
			if (!(exportWingSupportShapesString == null)) 
				exportWingSupportShapes = Boolean.parseBoolean(exportWingSupportShapesString);
			
		}
		
		//---------------------------------------------------------------
		// HORIZONTAL TAIL CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize horizontal tail CAD parameters
		double horizontalTipTolerance = 1e-3;
		boolean exportHorizontalSupportShapes = false;

		// Read horizontal tail CAD parameters from the xml file
		if (generateHorizontal && !(theAircraft.getHTail() == null)) {
			
			String horizontalTipToleranceString = reader.getXMLPropertyByPath("//horizontal/tipTolerance");
			if (!(horizontalTipToleranceString == null))
				horizontalTipTolerance = Double.valueOf(horizontalTipToleranceString);
			
			String exportHorizontalSupportShapesString = reader.getXMLPropertyByPath("//horizontal/exportSupportShapes");
			if (!(exportHorizontalSupportShapesString == null)) 
				exportHorizontalSupportShapes = Boolean.parseBoolean(exportHorizontalSupportShapesString);

		}
		
		//---------------------------------------------------------------
		// VERTICAL TAIL CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize vertical tail CAD parameters
		double verticalTipTolerance = 1e-3;
		boolean exportVerticalSupportShapes = false;

		// Read vertical tail CAD parameters from the xml file
		if (generateVertical && !(theAircraft.getVTail() ==  null)) { 

			String verticalTipToleranceString = reader.getXMLPropertyByPath("//vertical/tipTolerance");
			if (!(verticalTipToleranceString == null))
				verticalTipTolerance = Double.valueOf(verticalTipToleranceString);
			
			String exportVerticalSupportShapesString = reader.getXMLPropertyByPath("//vertical/exportSupportShapes");
			if (!(exportVerticalSupportShapesString == null)) 
				exportVerticalSupportShapes = Boolean.parseBoolean(exportVerticalSupportShapesString);
			
		}
		
		//---------------------------------------------------------------
		// CANARD CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize canard CAD parameters
		double canardTipTolerance = 1e-3;
		boolean exportCanardSupportShapes = false;

		// Read vertical tail CAD parameters from the xml file
		if (generateCanard && !(theAircraft.getCanard() == null)) { 

			String canardTipToleranceString = reader.getXMLPropertyByPath("//canard/tipTolerance");
			if (!(canardTipToleranceString == null))
				canardTipTolerance = Double.valueOf(canardTipToleranceString);
			
			String exportCanardSupportShapesString = reader.getXMLPropertyByPath("//canard/exportSupportShapes");
			if (!(exportCanardSupportShapesString == null)) 
				exportCanardSupportShapes = Boolean.parseBoolean(exportCanardSupportShapesString);
			
		}
		
		//---------------------------------------------------------------
		// WING/FUSELAGE FAIRING CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize wing fairing CAD parameters
		double wingFairingFrontLengthFactor = 1.25;
		double wingFairingBackLengthFactor = 1.25;
		double wingFairingSideSizeFactor = 0.25;
		double wingFairingHeightFactor = 0.25;
		double wingFairingHeightBelowContactFactor = 0.70;
		double wingFairingHeightAboveContactFactor = 0.10;
		double wingFairingFilletRadiusFactor = 0.60;

		// Read wing fairing CAD parameters from the xml file
		if (generateWingFairing && !(theAircraft.getWing() == null) && !(theAircraft.getFuselage() == null)) { 

			String wingFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/frontLengthFactor");
			if (!(wingFairingFrontLengthFactorString == null))
				wingFairingFrontLengthFactor = Double.valueOf(wingFairingFrontLengthFactorString);
			
			String wingFairingBackLengthFactorString = reader.getXMLPropertyByPath("//wing_fairing/backLengthFactor");
			if (!(wingFairingBackLengthFactorString == null))
				wingFairingBackLengthFactor = Double.valueOf(wingFairingBackLengthFactorString);
			
			String wingFairingSideSizeFactorString = reader.getXMLPropertyByPath("//wing_fairing/sideSizeFactor");
			if (!(wingFairingSideSizeFactorString == null))
				wingFairingSideSizeFactor = Double.valueOf(wingFairingSideSizeFactorString);
			
			String wingFairingHeightFactorString = reader.getXMLPropertyByPath("//wing_fairing/fairingHeightFactor");
			if (!(wingFairingHeightFactorString == null))
				wingFairingHeightFactor = Double.valueOf(wingFairingHeightFactorString);
			
			String wingFairingHeightBelowContactFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightBelowContactFactor");
			if (!(wingFairingHeightBelowContactFactorString == null))
				wingFairingHeightBelowContactFactor = Double.valueOf(wingFairingHeightBelowContactFactorString);
			
			String wingFairingHeightAboveContactFactorString = reader.getXMLPropertyByPath("//wing_fairing/heightAboveContactFactor");
			if (!(wingFairingHeightAboveContactFactorString == null))
				wingFairingHeightAboveContactFactor = Double.valueOf(wingFairingHeightAboveContactFactorString);
			
			String wingFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//wing_fairing/filletRadiusFactor");
			if (!(wingFairingFilletRadiusFactorString == null))
				wingFairingFilletRadiusFactor = Double.valueOf(wingFairingFilletRadiusFactorString);
			
		}
		
		//---------------------------------------------------------------
		// CANARD/FUSELAGE FAIRING CAD OPERATIONS
		//---------------------------------------------------------------

		// Initialize canard fairing CAD parameters
		double canardFairingFrontLengthFactor = 1.25;
		double canardFairingBackLengthFactor = 1.25;
		double canardFairingSideSizeFactor = 0.25;
		double canardFairingHeightFactor = 0.25;
		double canardFairingHeightBelowContactFactor = 0.70;
		double canardFairingHeightAboveContactFactor = 0.10;
		double canardFairingFilletRadiusFactor = 0.60;

		// Read canard fairing CAD parameters from the xml file
		if (generateCanardFairing && !(theAircraft.getCanard() == null) && !(theAircraft.getFuselage() == null)) { 

			String canardFairingFrontLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/frontLengthFactor");
			if (!(canardFairingFrontLengthFactorString == null))
				canardFairingFrontLengthFactor = Double.valueOf(canardFairingFrontLengthFactorString);
			
			String canardFairingBackLengthFactorString = reader.getXMLPropertyByPath("//canard_fairing/backLengthFactor");
			if (!(canardFairingBackLengthFactorString == null))
				canardFairingBackLengthFactor = Double.valueOf(canardFairingBackLengthFactorString);
			
			String canardFairingSideSizeFactorString = reader.getXMLPropertyByPath("//canard_fairing/sideSizeFactor");
			if (!(canardFairingSideSizeFactorString == null))
				canardFairingSideSizeFactor = Double.valueOf(canardFairingSideSizeFactorString);
			
			String canardFairingHeightFactorString = reader.getXMLPropertyByPath("//canard_fairing/fairingHeightFactor");
			if (!(canardFairingHeightFactorString == null))
				canardFairingHeightFactor = Double.valueOf(canardFairingHeightFactorString);
			
			String canardFairingHeightBelowContactFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightBelowContactFactor");
			if (!(canardFairingHeightBelowContactFactorString == null))
				canardFairingHeightBelowContactFactor = Double.valueOf(canardFairingHeightBelowContactFactorString);
			
			String canardFairingHeightAboveContactFactorString = reader.getXMLPropertyByPath("//canard_fairing/heightAboveContactFactor");
			if (!(canardFairingHeightAboveContactFactorString == null))
				canardFairingHeightAboveContactFactor = Double.valueOf(canardFairingHeightAboveContactFactorString);
			
			String canardFairingFilletRadiusFactorString = reader.getXMLPropertyByPath("//canard_fairing/filletRadiusFactor");
			if (!(canardFairingFilletRadiusFactorString == null))
				canardFairingFilletRadiusFactor = Double.valueOf(canardFairingFilletRadiusFactorString);
			
		}
		
		// EXPORT TO FILE OPTIONS
		Boolean exportToFile;
		FileExtension fileExtension;

		String exportToFileString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "//export_to_file/@value");
		exportToFile = exportToFileString.equalsIgnoreCase("TRUE") ? true : false;

		String fileExtensionString = reader.getXMLPropertyByPath("//export_to_file/file_format");
		fileExtension = FileExtension.valueOf(fileExtensionString);
		
		//---------------------------------------------------------------
		// GENERATE THE CAD MANAGER INTERFACE
		//---------------------------------------------------------------
//		ICADManager theCADManagerInterface = new ICADManager.Builder()
		
		return new CADManager();
	}
	
	public List<OCCSolid> generateCAD(ICADManager theCADManagerInterface) {
		List<OCCSolid> solids = new ArrayList<>();
		
		return solids;
	}
	
//	public Scene generateFX(List<OCCSolid> theSolidParts) {
//		Scene scene = new Scene();
//		
//		return scene;	
//	}
	
	public CADManager() {
		
	}
	
	public CADManager (ICADManager theCADManagerInterface, Aircraft theAircraft) {
		this._theCADManagerInterface = theCADManagerInterface;
		this._theAircraft = theAircraft;
	}
	
}

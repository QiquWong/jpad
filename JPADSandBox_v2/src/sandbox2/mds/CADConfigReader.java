package sandbox2.mds;

import java.io.File;

import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

public class CADConfigReader {

	public static void main(String[] args) {
		
		File configCADXml = new File("src/sandbox2/mds/config_CAD.xml");
		
		if (configCADXml.exists())
			System.out.println("CAD configuration xml file absolute path: " + configCADXml.getAbsolutePath());
		else
			return;
		
		// Reading the xml file
		JPADXmlReader reader = new JPADXmlReader(configCADXml.getAbsolutePath());
		
		// Detect the parts that need to be rendered
		Boolean generateFuselage;
		Boolean generateWing;
		Boolean generateHorizontal;
		Boolean generateVertical;
		Boolean generateCanard;
		Boolean generateWingFairing;
		Boolean generateCanardFairing;
		
		String generateFuselageString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/fuselage/@generate");
		
		String generateWingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/wing/@generate");
		
		String generateHorizontalString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/horizontal/@generate");
		
		String generateVerticalString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/vertical/@generate");
		
		String generateCanardString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/canard/@generate");
		
		String generateWingFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/wing_fairing/@generate");
		
		String generateCanardFairingString = MyXMLReaderUtils.getXMLPropertyByPath(
				reader.getXmlDoc(), reader.getXpath(), "/canard_fairing/@generate");
		
		generateFuselage = (generateFuselageString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWing = (generateWingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateHorizontal = (generateHorizontalString.equalsIgnoreCase("TRUE")) ? true : false;
		generateVertical = (generateVerticalString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanard = (generateCanardString.equalsIgnoreCase("TRUE")) ? true : false;
		generateWingFairing = (generateWingFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		generateCanardFairing = (generateCanardFairingString.equalsIgnoreCase("TRUE")) ? true : false;
		
		// FUSELAGE	
		
		// Initialize fuselage CAD parameters
		double noseCapSectionFactor1;
		double noseCapSectionFactor2;
		int numberNoseCapSections;
		int numberNoseTrunkSections;
		XSpacingType spacingTypeNoseTrunk;
		int numberTailTrunkSections;
		XSpacingType spacingTypeTailTrunk;
		double tailCapSectionFactor1;
		double tailCapSectionFactor2;
		int numberTailCapSections;
		boolean exportFuselageSupportShapes;
		
		// Read fuselage CAD parameters from the xml file
		if (generateFuselage) { // TODO: Add another check in order to take into account 
								// whether the imported aircraft has such a component
			
		}
		
		// WING
		
		// Initialize wing CAD parameters
		double wingTipTolerance;
		boolean exportWingSupportShapes;
		
		// Read wing CAD parameters from the xml file
		if (generateWing) { // TODO: Add another check in order to take into account 
							// whether the imported aircraft has such a component
			
		}
		
		// HORIZONTAL

		// Initialize horizontal tail CAD parameters
		double horizontalTipTolerance;
		boolean exportHorizontalSupportShapes;

		// Read horizontal tail CAD parameters from the xml file
		if (generateHorizontal) { // TODO: Add another check in order to take into account 
								  // whether the imported aircraft has such a component

		}
		
		// VERTICAL

		// Initialize vertical tail CAD parameters
		double verticalTipTolerance;
		boolean exportVerticalSupportShapes;

		// Read vertical tail CAD parameters from the xml file
		if (generateVertical) { // TODO: Add another check in order to take into account 
								// whether the imported aircraft has such a component

		}
		
		// CANARD

		// Initialize canard CAD parameters
		double canardTipTolerance;
		boolean exportCanardSupportShapes;

		// Read vertical tail CAD parameters from the xml file
		if (generateCanard) { // TODO: Add another check in order to take into account 
							  // whether the imported aircraft has such a component

		}
		
		// WING-FUSELAGE FAIRING

		// Initialize wing fairing CAD parameters
		double wingFairingFrontLengthFactor;
		double wingFairingBackLengthfactor;
		double wingFairingSideSizeFactor;
		double wingFairingHeightFactor;
		double wingFairingHeightBelowContactFactor;
		double wingFairingHeightAboveContactFactor;
		double wingFairingFilletRadiusFactor;

		// Read wing fairing CAD parameters from the xml file
		if (generateWingFairing) { // TODO: Add another check in order to take into account 
			  					   // whether the imported aircraft has such a component

		}
		
		// CANARD-FUSELAGE FAIRING

		// Initialize canard fairing CAD parameters
		double canardFairingFrontLengthFactor;
		double canardFairingBackLengthfactor;
		double canardFairingSideSizeFactor;
		double canardFairingHeightFactor;
		double canardFairingHeightBelowContactFactor;
		double canardFairingHeightAboveContactFactor;
		double canardFairingFilletRadiusFactor;

		// Read canard fairing CAD parameters from the xml file
		if (generateCanardFairing) { // TODO: Add another check in order to take into account 
			  					     // whether the imported aircraft has such a component

		}
	}

	public enum XSpacingType {
		UNIFORM {
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.linspaceDouble(x1, x2, n);
				return xSpacing;
			}
		},
		COSINUS {
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.cosineSpaceDouble(x1, x2, n);
				return xSpacing;
			}
		},
		HALFCOSINUS1 { // finer spacing close to x1
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.halfCosine1SpaceDouble(x1, x2, n);
				return xSpacing;
			}
		}, 
		HALFCOSINUS2 { // finer spacing close to x2
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.halfCosine2SpaceDouble(x1, x2, n);
				return xSpacing;
			}
		}; 
		
		public abstract Double[] calculateSpacing(double x1, double x2, int n);
	}
}

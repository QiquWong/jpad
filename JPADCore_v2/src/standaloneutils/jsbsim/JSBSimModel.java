package standaloneutils.jsbsim;

import java.io.File;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import configuration.MyConfiguration;
import de.dlr.sc.tigl.TiglException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.cpacs.CPACSReader;
import standaloneutils.cpacs.CPACSUtils;

public class JSBSimModel {

	public static enum ReadStatus {
		OK,
		ERROR;
	}

	CPACSReader _cpacsReader;

	public JSBSimModel(CPACSReader reader) {
		_cpacsReader = reader;	
		init();
	}

	private void init() {
		// TODO: in case you need initializations at construction time
	}

	public CPACSReader getCpacsReader() {
		return _cpacsReader;
	}

	public Document getXmlDoc() {
		if (_cpacsReader != null) {
			if (_cpacsReader.getJpadXmlReader() != null)
				return _cpacsReader.getJpadXmlReader().getXmlDoc();
			else
				return null;
		}
		else
			return null;
	}
	
	public void appendToCPACSFile(File file) {

		// TODO implement CPACS file export function

		System.out.println("[JSBSimModel.appendToCPACSFile] --> not yet implemented.");

	}
	public void exportToJSBSimFile(File file) {

		// TODO implement JSBSim file export function

		System.out.println("[JSBSimModel.exportToJSBSimFile] --> not yet implemented.");

	}

	public JPADXmlReader getJpadXmlReader() {
		if (_cpacsReader != null)
			return _cpacsReader.getJpadXmlReader();
		else
			return null;
	}

	public void readVariablesFromCPACS() throws TiglException {
		System.out.println("--------------------------------");
		System.out.println("Start readVariablesFromCPACS :");
		System.out.println("--------------------------------");
		if (_cpacsReader == null)
			return;

		if (_cpacsReader.getJpadXmlReader() == null)
			return;
		
		JPADXmlReader jpadXmlReader = _cpacsReader.getJpadXmlReader();
		
		MyConfiguration.customizeAmountOutput(); // simple output format for Amount-s
		
		double[] GravityCenterPosition;

		GravityCenterPosition = _cpacsReader.getGravityCenterPosition();
		NodeList wingsNodes = _cpacsReader.getWingList();
		
		// Main wing
		String wingUID = jpadXmlReader.getXMLPropertyByPath(
				"/cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID");
		int wingIndex = _cpacsReader.getWingIndexZeroBased(wingUID);
		System.out.println("wingUID, idx: " + wingUID + ", " + wingIndex);

		// Horizontal tail
		String horizontalTailUID = jpadXmlReader.getXMLPropertyByPath(
				"/cpacs/toolspecific/UNINA_modules/input/wings/HorizTail/HorizTailUID");
		int horizontalTailIndex = _cpacsReader.getWingIndexZeroBased(horizontalTailUID);
		System.out.println("htailUID, idx: " + horizontalTailUID + ", " + horizontalTailIndex);
		
		// Vertical tail
		String verticalTailUID = jpadXmlReader.getXMLPropertyByPath(
				"/cpacs/toolspecific/UNINA_modules/input/wings/VerticalTail/VerticalTailUID");
		int verticalTailIndex = _cpacsReader.getWingIndexZeroBased(verticalTailUID); 
		System.out.println("vtailUID, idx: " + verticalTailUID + ", " + verticalTailIndex);
		
		//Start wing 
		Node wingNode = wingsNodes.item(wingIndex);
		Double[] wingPosition = CPACSUtils.getWingPosition(wingNode);
		double wingSurface = _cpacsReader.getWingReferenceArea(wingIndex);
		System.out.println("wingSurface: " + wingSurface);
//		double wingWettedArea = _cpacsReader.getWingWettedArea(wingUID);
//		System.out.println("wingWettedArea: " + wingWettedArea);

		Amount<Area> wingArea = Amount.valueOf(wingSurface,SI.SQUARE_METRE);
		System.out.println("wing area: " + wingArea);
		
		double wspan = _cpacsReader.getWingSpan(wingUID);
		Amount<Length> wingSpan = Amount.valueOf(wspan,SI.METRE);
		System.out.println("wing span: " + wingSpan);

		Double wrootchrd = CPACSUtils.getWingChord(wingNode);
		Amount<Length> wingRootChord = Amount.valueOf(wrootchrd,SI.METRE);
		System.out.println("wing root chord: " + wingRootChord);

		double macVectorWing[] = new double [4];
		macVectorWing = _cpacsReader.getWingMeanAerodynamicChord(wingUID);
		System.out.println("--------------------------------");
		System.out.println("MAC value = " + macVectorWing[0]);
		System.out.println("MACx value = " + macVectorWing[1]);
		System.out.println("MACy value = " + macVectorWing[2]);
		System.out.println("MACz value = " + macVectorWing[3]);
		System.out.println("--------------------------------");

		//Horizontal tail
		System.out.println("--------------------------------");
		System.out.println("Start HT");
		System.out.println("--------------------------------");
		Node horizontalTailNode = wingsNodes.item(horizontalTailIndex);
		Double[] horizontalTailPosition = CPACSUtils.getWingPosition(horizontalTailNode);
		double[] macVectorHT = new double [4];
		double horizontalSurface = _cpacsReader.getWingReferenceArea(horizontalTailIndex);
//		double horizontalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(horizontalTailUID,"x");
		macVectorHT = _cpacsReader.getWingMeanAerodynamicChord(horizontalTailUID);
		System.out.println("--------------------------------");
		System.out.println("MAC HT value = " + macVectorHT[0]);
		System.out.println("MACx HT value = " + macVectorHT[1]);
		System.out.println("MACy HT value = " + macVectorHT[2]);
		System.out.println("MACz HT value = " + macVectorHT[3]);
		System.out.println("--------------------------------");
		double horizontalTailAeroCenter = 
				macVectorHT[1] + (0.25)*macVectorHT[0];	
		System.out.println("--------------------------------");
		System.out.println("horizontalTailAeroCenter =  " + horizontalTailAeroCenter);
		System.out.println("--------------------------------");
		double horizontalTailArm = horizontalTailAeroCenter-GravityCenterPosition[0];


		//Vertical tail
		System.out.println("--------------------------------");
		System.out.println("Start VT");
		System.out.println("--------------------------------");
		Node verticalTailNode = wingsNodes.item(verticalTailIndex);
		Double[] verticalTailPosition = CPACSUtils.getWingPosition(verticalTailNode);
		double[] macVectorVT = new double [4];
		double verticalSurface = _cpacsReader.getWingReferenceArea(verticalTailIndex);
//		double horizontalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(horizontalTailUID,"x");
		macVectorVT = _cpacsReader.getWingMeanAerodynamicChord(verticalTailUID);
		System.out.println("--------------------------------");
		System.out.println("MAC VT value = " + macVectorVT[0]);
		System.out.println("MACx VT value = " + macVectorVT[1]);
		System.out.println("MACy VT value = " + macVectorVT[2]);
		System.out.println("MACz VT value = " + macVectorVT[3]);
		System.out.println("--------------------------------");
		double verticalTailAeroCenter = 
				macVectorVT[1] + (0.25)*macVectorVT[0];	
		System.out.println("--------------------------------");
		System.out.println("verticalTailAeroCenter =  " + verticalTailAeroCenter);
		System.out.println("--------------------------------");
		double verticalTailArm = verticalTailAeroCenter-GravityCenterPosition[0];

		//AERO REFERENCE
		double[] aeroReferenceCenter = new double [3];
		aeroReferenceCenter[0] = wingPosition[0] +
				macVectorWing[1] + (0.25)*macVectorWing[0];	
		aeroReferenceCenter[1] = 0;
		aeroReferenceCenter[2] =  wingPosition[2];

		//Eyepoint
		double[] eyePointReferenceCenter = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]");

		//Visual
		double[] visualReferenceCenter = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]");
			
		//Star Mass Balance -->Mass and Inertia
		String ixx = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxx");
		String iyy = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyy");
		String izz = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jzz");
		String ixy = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxy");
		String ixz = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxz");
		String iyz = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyz");
		String emptyWeight = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/mass");
		//		cpacs/vehicles/aircraft/model/landingGear/noseGears/noseGear/fuselageAttachment/translation ->Path in the CPACS of the nose gear
		//		cpacs/vehicles/aircraft/model/landingGear/mainGears/mainGear/wingAttachment/positioning ->Path in the CPACS of the main gear

		//GroundReaction
		NodeList landingGearList = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear");
		//Nose gear
		String noseGearXPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/x");		
		String noseGearYPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/y");	
		String noseGearZPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/z");	
		String noseGearAttribute = jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]", "type"); 
		String noseGearStatic_Friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/static_friction");
		String noseGearDynamic_Friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/dynamic_friction");
		String noseGearRolling_friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/rolling_friction");
		String noseGearSpring_coeff = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/spring_coeff"); //LBS/FT
		String noseGearDamping_coeff = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/damping_coeff");//LBS/FT
		String noseGearDamping_coeff_rebound = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/damping_coeff_rebound");//LBS/FT		

		String noseGearMax_steer = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/max_steer");//Deg
		String noseGearRetractable = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/retractable");//1->Retrectable 0 -> Fixed

		//Right Main

		Double etaRightLandingGear = Double.parseDouble(
				jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear/mainGears/"
				+ "mainGear/wingAttachment/positioning/eta"));
		Double csiRightLandingGear = Double.parseDouble(
				jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear/mainGears/"
				+ "mainGear/wingAttachment/positioning/xsi"));
		Double realHeightRightLandingGear = Double.parseDouble(
				jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear/mainGears/"
				+ "mainGear/wingAttachment/positioning/relHeight"));
		Double [] coordinateRightLandingGear = CPACSUtils.getPositionRelativeToEtaAndCsi
				(wingNode, etaRightLandingGear, csiRightLandingGear, wspan, realHeightRightLandingGear);
		double xRightLandingGear = coordinateRightLandingGear[0] + wingPosition[0];
		double yRightLandingGear = coordinateRightLandingGear[1] + wingPosition[1];
		double zRightLandingGear = coordinateRightLandingGear[2] + wingPosition[2];
		System.out.println("--------------------------------");
		System.out.println("X position of landing gear is : "+ xRightLandingGear );
		System.out.println("Y position of landing gear is : "+ yRightLandingGear );
		System.out.println("Z position of landing gear is : "+ zRightLandingGear );

		System.out.println("--------------------------------");

//		String rightMainXPosition = jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
//						+ "landingGear/contact["+2+"]/location/x");		
//		String rightMainYPosition = jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
//						+ "landingGear/contact["+2+"]/location/y");	
//		String rightMainZPosition = jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
//						+ "landingGear/contact["+2+"]/location/z");			
		String rightMainGearAttribute = jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]", "type");	
		String rightMainGearStatic_Friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/static_friction");
		String rightMainGearDynamic_Friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/dynamic_friction");
		String rightMainGearRolling_friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/rolling_friction");
		String rightMainGearSpring_coeff = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/spring_coeff"); //LBS/FT
		String rightMainGearDamping_coeff = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/damping_coeff");//LBS/FT
		String rightMainGearDamping_coeff_rebound = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/damping_coeff_rebound");//LBS/FT		

		String rightMainGearMax_steer = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/max_steer");//Deg
		String rightMainGearRetractable = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/retractable");//2->Retrectable 2 -> Fixed
		//Left Main
//		String leftMainXPosition = jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
//						+ "landingGear/contact["+1+"]/location/x");		
//		String leftMainYPosition = jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
//						+ "landingGear/contact["+1+"]/location/y");	
//		String leftMainZPosition = jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
//						+ "landingGear/contact["+1+"]/location/z");	
		double xLeftLandingGear = xRightLandingGear;
		double yLeftLandingGear = -yRightLandingGear;
		double realHeightLeftLandingGear = realHeightRightLandingGear ;
		String leftMainGearAttribute = jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]", "type");
		String leftMainGearStatic_Friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/static_friction");
		String leftMainGearDynamic_Friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/dynamic_friction");
		String leftMainGearRolling_friction = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/rolling_friction");
		String leftMainGearSpring_coeff = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/spring_coeff"); //LBS/FT
		String leftMainGearDamping_coeff = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/damping_coeff");//LBS/FT
		String leftMainGearDamping_coeff_rebound = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/damping_coeff_rebound");//LBS/FT		

		String leftMainGearMax_steer = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/max_steer");//Deg
		String leftMainGearRetractable = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/retractable");//1->Retrectable 1 -> Fixed


	

		//Propulsion
		//GeoData
		
		double[] enginePosition = _cpacsReader.getVectorPosition(
				"cpacs/vehicles/aircraft/model/engines/engine/transformation/translation");
		double[] engineRotation = _cpacsReader.getVectorPosition(
				"cpacs/vehicles/aircraft/model/engines/engine/transformation/rotation");
		//Propulsion Data -> need to write engine script in JSBSim
		String milthrust =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/milthrust");
		String bypassratio =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bypassratio");
		String tsfc =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/tsfc");
		String bleed =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bleed");
		String idlen1 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/idlen1");
		String idlen2 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/idlen2");
		String maxn1 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/maxn1");
		String maxn2 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/maxn2");
		String augmented =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/augmented");
		String injected =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/injected");
		List<Double> flightLevelEngine = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/flightLevel");
		List<Double> machNumberEngine = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/machNumber");
		List<Double> idleThrust = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/IdleThrust");
		//Building Table for engine
		double [][] idleThrustTable = new double [machNumberEngine.size()+1][flightLevelEngine.size()+1];
		int k = 0;
		for(int i= 0;i<machNumberEngine.size()+1;i++) {
			for (int j = 0;j<flightLevelEngine.size()+1;j++) {
				if ((i==0) && (j==0)) {
					idleThrustTable[i][j]=0;
				}
				if (j==0 && i !=0 ){
					idleThrustTable[i][j]=machNumberEngine.get(i-1);
				}
				if (i==0 && j !=0 ){
					idleThrustTable[i][j]=flightLevelEngine.get(j-1);
				}
				else {
					idleThrustTable[i][j]=idleThrust.get(k);
					if (k<flightLevelEngine.size()*machNumberEngine.size()-1) {
						k++;							
					}
				}
			}						
		}
		String idleThrustTableString = CPACSUtils.matrixDoubleToJSBSimTable2D(idleThrustTable, "	");

		//Build 2nd table for engine
		List<Double> milThrust = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/MilThrust");
		double [][] milThrustTable = new double [machNumberEngine.size()+1][flightLevelEngine.size()+1];
		k = 0;
		for(int i= 0;i<machNumberEngine.size()+1;i++) {
			for (int j = 0;j<flightLevelEngine.size()+1;j++) {
				if (i==0&&j==0) {
					milThrustTable[i][j]=0;
				}
				if (j==0 && i !=0 ){
					milThrustTable[i][j]=machNumberEngine.get(i-1);
				}
				if (i==0 && j !=0 ){
					milThrustTable[i][j]=flightLevelEngine.get(j-1);
				}
				else {
					milThrustTable[i][j]=idleThrust.get(k);
					if (k<flightLevelEngine.size()*machNumberEngine.size()-1) {
						k++;							
					}
				}
			}						
		}
		String milThrustTableString = CPACSUtils.matrixDoubleToJSBSimTable2D(milThrustTable, "	");

		//thruster
		String thrust00 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/thrust00");
		String opr00 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/opr00");
		String bpr00 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bpr00");
		String fpr00 =  jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/fpr00");

		//tank
		
		NodeList tankNumberElement = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/analyses/weightAndBalance/operationalCases/"
				+ "operationalCase[" + 1 + "]/mFuel/fuelInTanks/fuelInTank");
		NodeList tankPositionList =  MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/analyses/weightAndBalance/operationalCases/"
				+ "operationalCase");
		
		System.out.println("Tank Number is  " + tankNumberElement.getLength());
		System.out.println("Tank Position is  " + tankPositionList.getLength());

		double[][] tankMatrix = new double [tankNumberElement.getLength()][3];
		List<String> capacityTank = null;
//		List<String> contentTank = null;
		List<String> fuelTypeTank = null;
		
		for(int i= 0;i<tankNumberElement.getLength();i++) {
			
			Node node = tankPositionList.item(1);
			Double[] vectorTankPosition = CPACSUtils.getVectorPositionNodeTank(node, i);
//			Double[] vectorTankPosition = _cpacsReader.getVectorPositionNodeTank(1);	
			for (int j = 0;j<3;j++) {
//				tankMatrix[i][j] = vectorTankPosition[j];
//				List<Double>vectorTankCapacity= _cpacsReader.
//						getJpadXmlReader().readArrayDoubleFromXMLSplit(
//						"cpacs/vehicles/aircraft/model/analyses/weightAndBalance/"
//						+ "operationalCases/operationalCase[" + 1+ "]/mFuel/"
//						+ "fuelInTanks/fuelInTank[" + i + "]/mass");
//				String tankCapacity = String.valueOf(vectorTankCapacity.get(vectorTankCapacity.size())); 
//				capacityTank.add(tankCapacity);
				fuelTypeTank.add("cpacs/vehicles/aircraft/model/analyses/weightAndBalance/"
						+ "operationalCases/operationalCase[" + 1 + "]/mFuel/"
						+ "fuelInTanks/fuelInTank[" + i + "]/fuelUID");	
			}
		}						
		//Flight Control
//		List<String> systemCommadList = _cpacsReader.getSystemParameter(
//				"cpacs/vehicles/aircraft/model/systems/controlDistributors/controlDistributor");
		//TODO now is possible to have relative and absolute deflection of control surface, need integration with pilot command
		
		
		
		
		
		
		//Ground Effect

		List<Double> heightGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/Height");
		List<Double> kCLGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCL");
		List<Double> kCDGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCD");
		double[][] kCLMatrix = new double [kCLGroundEffect.size()][2];
		double[][] kCDMatrix = new double [kCLGroundEffect.size()][2];
		for (int i=0;i<kCLGroundEffect.size();i++) {
			kCLMatrix[i][0] = heightGroundEffect.get(i); //-> effect on lift coefficient
			kCLMatrix[i][1] = kCLGroundEffect.get(i); 
			kCDMatrix[i][0] = heightGroundEffect.get(i); //-> effect on drag coefficient
			kCDMatrix[i][1] = kCDGroundEffect.get(i);
		}
		String kCLTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(kCLMatrix);
		String kCDTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(kCDMatrix);

		//effect speed braker
		List<Double> kSpeedBreakerGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCLSB");
		List<Double> speedBreakerPosition = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/PositionSpeedBreaker");
		double[][] kSpeedBreakerMatrix = new double [kSpeedBreakerGroundEffect.size()][2];
		for (int i=0;i<kSpeedBreakerGroundEffect.size();i++) {
			kSpeedBreakerMatrix[i][0] = speedBreakerPosition.get(i); 
			kSpeedBreakerMatrix[i][1] = kSpeedBreakerGroundEffect.get(i); 
		}
		String kSpeedBreakerTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(kSpeedBreakerMatrix);

		//effect spoiler
		List<Double> kSpoilerGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCLSB");
		List<Double> spoilerPosition = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/PositionSpeedBreaker");
		double[][] kSpoilerMatrix = new double [kSpoilerGroundEffect.size()][2];
		for (int i=0;i<kSpoilerGroundEffect.size();i++) {
			kSpoilerMatrix[i][0] = spoilerPosition.get(i); 
			kSpoilerMatrix[i][1] = kSpoilerGroundEffect.get(i); 

		}
		String kSpoilerTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(kSpoilerMatrix);

		//Drag
		//1) CD0
		List<Double> alphaCD0 = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCLSB");
		List<Double> cD0Value = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/PositionSpeedBreaker");
		double[][] cD0Matrix = new double [cD0Value.size()][2];
		for (int i=0;i<cD0Value.size();i++) {
			cD0Matrix[i][0] = spoilerPosition.get(i); 
			cD0Matrix[i][1] = kSpoilerGroundEffect.get(i); 
		}
		String cD0Table = CPACSUtils.matrixDoubleToJSBSimTableNx2(cD0Matrix);
		//2) Mach Effect
		List<Double> machCDWave = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/Drag/CD_Mach/Mach");
		List<Double> cDWaveValue = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/Drag/CD_Mach/DeltaCD0");
		double[][] cDWaveMatrix = new double [cDWaveValue.size()][2];
		for (int i=0;i<cDWaveValue.size();i++) {
			cDWaveMatrix[i][0] = machCDWave.get(i); 
			cDWaveMatrix[i][1] = cDWaveValue.get(i); 
		}
		String CDWaveTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(cDWaveMatrix);	

		

	}//end CPACS READ VARIABLE

	public void exportToXML(File file) {
		// TODO 
		
	}	

} //end main

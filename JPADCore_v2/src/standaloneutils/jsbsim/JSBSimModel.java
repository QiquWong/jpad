package standaloneutils.jsbsim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import de.dlr.sc.tigl.TiglException;
import javaslang.Tuple;
import javaslang.Tuple4;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.cpacs.CPACSReader;
import standaloneutils.cpacs.CPACSUtils;
import writers.AircraftSaveDirectives;
import writers.JPADStaticWriteUtils;

public class JSBSimModel {

	String aircraftName=null;
	Amount<Area> wingArea = null;
	Amount<Length> wingSpan = null;
	Amount<Length> wingRootChord = null;
	Amount<Length> wingMAC = null;
	Amount<Area> htSurface = null;
	Amount<Length> hTArm = null;
	Amount<Area> vtSurface = null;
	Amount<Length> vtArm = null;
	List<Double> cl = new ArrayList<Double>();
	List<Double> clAlpha = new ArrayList<Double>();
	List<Double> cdAlpha = new ArrayList<Double>();
	List<Double> cyBeta = new ArrayList<Double>();
	List<Double> cd = new ArrayList<Double>();
	List<Double> cy = new ArrayList<Double>();
	List<Double> cRoll = new ArrayList<Double>();
	List<Double> cm = new ArrayList<Double>();
	List<Double> cn = new ArrayList<Double>();
	double[] aeroReferenceOrigin = new double[3];
	double[] cgPosition = new double[3];
	double[] eyePointPosition = new double[3];
	String kSpeedBreakerTable = new String();
	double ixx = 0.0; 
	double iyy = 0.0; 
	double izz = 0.0; 
	double ixy = 0.0; 
	double ixz = 0.0; 
	double iyz = 0.0; 
	double[][] tankMatrix = null;
	double[][] controlSurface = null;
	Amount<Mass> emptyWeight = null;
	List<Amount<Length>> noseGearPosition = new ArrayList<>();
	List<Amount<Length>> mainGearRightPosition = new ArrayList<>();
	Double[] coordinateRightLandingGear = new Double [3];
	Double[] coordinateLeftLandingGear = new Double [3];
	Double[] coordinateNoseLandingGear = new Double [3];
	List<Amount<Length>> mainGearLeftPosition = new ArrayList<>();
	List<Amount<Length>> enginePositionAmo = new ArrayList<>();
	List<Amount<Length>> engineRotationAmo = new ArrayList<>();
	List<Double> noseGear = new ArrayList<>();
	List<Double> mainRightGear = new ArrayList<>();
	List<Double> mainLeftGear = new ArrayList<>();
	List<Double> engineRight = new ArrayList<Double>();
	List<String> controlSurfaceList = new ArrayList<String>();
	List<Integer>controlSurfaceInt = new ArrayList<Integer>();
	double[] rightEnginePosition = new double [3];
	double[] rightEngineRotation = new double [3];
	double[] leftEnginePosition = new double [3];
	double millThrust = 0.0;
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
		
		aircraftName = _cpacsReader.aircraftName();
		

		cgPosition = _cpacsReader.getGravityCenterPosition();
		System.out.println("CG_X = " + cgPosition[0]);
		System.out.println("CG_Y = " + cgPosition[1]);
		System.out.println("CG_Z = " + cgPosition[2]);

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

		wingArea = Amount.valueOf(wingSurface,SI.SQUARE_METRE);
		System.out.println("wing area: " + wingArea);
		double wspan = _cpacsReader.getWingSpan(wingUID);
		wingSpan = Amount.valueOf(wspan,SI.METRE);
		System.out.println("wing span: " + wingSpan);

		Double wrootchrd = CPACSUtils.getWingChord(wingNode);
		wingRootChord = Amount.valueOf(wrootchrd,SI.METRE);
		System.out.println("wing root chord: " + wingRootChord);

		double macVectorWing[] = new double [4];
		macVectorWing = _cpacsReader.getWingMeanAerodynamicChord(wingUID);
		wingMAC = Amount.valueOf(macVectorWing[0],SI.METER);
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
		htSurface = Amount.valueOf(horizontalSurface, SI.SQUARE_METRE);
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
		double horizontalTailArm = horizontalTailAeroCenter-cgPosition[0];
		hTArm = Amount.valueOf(horizontalTailArm, SI.METER);

		//Vertical tail
		System.out.println("--------------------------------");
		System.out.println("Start VT");
		System.out.println("--------------------------------");
		Node verticalTailNode = wingsNodes.item(verticalTailIndex);
		Double[] verticalTailPosition = CPACSUtils.getWingPosition(verticalTailNode);
		double[] macVectorVT = new double [4];
		double verticalSurface = _cpacsReader.getWingReferenceArea(verticalTailIndex);
		vtSurface = Amount.valueOf(verticalSurface, SI.SQUARE_METRE);
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
		double verticalTailArm = verticalTailAeroCenter-cgPosition[0];
		vtArm =  Amount.valueOf(verticalTailArm, SI.METER);
		//AERO REFERENCE
		aeroReferenceOrigin[0] = wingPosition[0] +
				macVectorWing[1] + (0.25)*macVectorWing[0];	
		aeroReferenceOrigin[1] = 0;
		aeroReferenceOrigin[2] =  wingPosition[2];




		//Eyepoint
		eyePointPosition = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]");

		//Visual
		double[] visualReferenceCenter = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]"); //TO DO Ask where positioning visual
			
		//Start Mass Balance -->Mass and Inertia

		ixx = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxx")));
		iyy = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyy")));
		izz = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jzz")));
		ixy = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxy")));
		ixz =  (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxz")));
		iyz =  (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyz")));
		Double emptyW = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/massDescription/mass"));
		System.out.println("Empty weight = "+ emptyW);
		emptyWeight =  Amount.valueOf (emptyW,SI.KILOGRAM);
		//		cpacs/vehicles/aircraft/model/landingGear/noseGears/noseGear/fuselageAttachment/translation ->Path in the CPACS of the nose gear
		//		cpacs/vehicles/aircraft/model/landingGear/mainGears/mainGear/wingAttachment/positioning ->Path in the CPACS of the main gear
		
		//GroundReaction
		NodeList landingGearList = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear/contact");
		noseGear = _cpacsReader.getLandingGear(landingGearList.item(0));
		//Nose gear
		coordinateNoseLandingGear[0] = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear"
				+ "/noseGears/noseGear/fuselageAttachment/translation/x"));
		coordinateNoseLandingGear[1] = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear"
				+ "/noseGears/noseGear/fuselageAttachment/translation/y"));
		coordinateNoseLandingGear[2] = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear"
				+ "/noseGears/noseGear/fuselageAttachment/translation/z"));

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
		coordinateRightLandingGear = CPACSUtils.getPositionRelativeToEtaAndCsi
				(wingNode, etaRightLandingGear, csiRightLandingGear, wspan, realHeightRightLandingGear);
		coordinateRightLandingGear[0] = coordinateRightLandingGear[0] + wingPosition[0];
		coordinateRightLandingGear[1] = coordinateRightLandingGear[1] + wingPosition[1];
		coordinateRightLandingGear[2] = coordinateRightLandingGear[2] + wingPosition[2];
//		mainGearRightPosition.add(Amount.valueOf(xRightLandingGear,SI.METER));
//		mainGearRightPosition.add(Amount.valueOf(yRightLandingGear,SI.METER));
//		mainGearRightPosition.add(Amount.valueOf(zRightLandingGear,SI.METER));

		System.out.println("--------------------------------");
		System.out.println("X position of landing gear is : "+ coordinateRightLandingGear[0] );
		System.out.println("Y position of landing gear is : "+ coordinateRightLandingGear[1] );
		System.out.println("Z position of landing gear is : "+ coordinateRightLandingGear[2] );

		System.out.println("--------------------------------");

		mainRightGear = _cpacsReader.getLandingGear(landingGearList.item(2));
		
		//Left Main
		mainRightGear = _cpacsReader.getLandingGear(landingGearList.item(1));

		coordinateLeftLandingGear[0] = coordinateRightLandingGear[0];
		coordinateLeftLandingGear[1] = -coordinateRightLandingGear[1];
		coordinateLeftLandingGear[2] = coordinateRightLandingGear[2] ;
//		mainGearLeftPosition.add(Amount.valueOf(xRightLandingGear,SI.METER));
//		mainGearLeftPosition.add(Amount.valueOf(yRightLandingGear,SI.METER));
//		mainGearLeftPosition.add(Amount.valueOf(zRightLandingGear,SI.METER));
		//Propulsion
		//GeoData
		
		rightEnginePosition = _cpacsReader.getVectorPosition(
				"cpacs/vehicles/aircraft/model/engines/engine/transformation/translation");
		rightEngineRotation = _cpacsReader.getVectorPosition(
				"cpacs/vehicles/aircraft/model/engines/engine/transformation/rotation");
		leftEnginePosition[0] = rightEnginePosition[0];
		leftEnginePosition[1] = -rightEnginePosition[1];
		leftEnginePosition[2] = rightEnginePosition[2];

		for (int i = 0; i<3; i++){
		enginePositionAmo.add(Amount.valueOf(rightEnginePosition[i], SI.METER));
		engineRotationAmo.add(Amount.valueOf(rightEngineRotation[i], SI.METER));

		}

		//Propulsion Data -> need to write engine script in JSBSim
		
//		List<Double> flightLevelEngine = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/flightLevel");
//		List<Double> machNumberEngine = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/machNumber");
//		List<Double> idleThrust = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/IdleThrust");
		//Building Table for engine
//		double [][] idleThrustTable = new double [machNumberEngine.size()+1][flightLevelEngine.size()+1];
//		int k = 0;
//		for(int i= 0;i<machNumberEngine.size()+1;i++) {
//			for (int j = 0;j<flightLevelEngine.size()+1;j++) {
//				if ((i==0) && (j==0)) {
//					idleThrustTable[i][j]=0;
//				}
//				if (j==0 && i !=0 ){
//					idleThrustTable[i][j]=machNumberEngine.get(i-1);
//				}
//				if (i==0 && j !=0 ){
//					idleThrustTable[i][j]=flightLevelEngine.get(j-1);
//				}
//				else {
//					idleThrustTable[i][j]=idleThrust.get(k);
//					if (k<flightLevelEngine.size()*machNumberEngine.size()-1) {
//						k++;							
//					}
//				}
//			}						
//		}
//		String idleThrustTableString = CPACSUtils.matrixDoubleToJSBSimTable2D(idleThrustTable, "	");
//
//		//Build 2nd table for engine
//		List<Double> milThrust = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
//				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/MilThrust");
//		double [][] milThrustTable = new double [machNumberEngine.size()+1][flightLevelEngine.size()+1];
//		k = 0;
//		for(int i= 0;i<machNumberEngine.size()+1;i++) {
//			for (int j = 0;j<flightLevelEngine.size()+1;j++) {
//				if (i==0&&j==0) {
//					milThrustTable[i][j]=0;
//				}
//				if (j==0 && i !=0 ){
//					milThrustTable[i][j]=machNumberEngine.get(i-1);
//				}
//				if (i==0 && j !=0 ){
//					milThrustTable[i][j]=flightLevelEngine.get(j-1);
//				}
//				else {
//					milThrustTable[i][j]=idleThrust.get(k);
//					if (k<flightLevelEngine.size()*machNumberEngine.size()-1) {
//						k++;							
//					}
//				}
//			}						
//		}
//		String milThrustTableString = CPACSUtils.matrixDoubleToJSBSimTable2D(milThrustTable, "	");
		NodeList engineList = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/engines/engine");
		engineRight = _cpacsReader.getEngine(engineList.item(0));
		//thruster
		

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

		tankMatrix = _cpacsReader.getVectorPositionNodeTank(
				tankPositionList.item(1),cgPosition,emptyW);
		
						
		//Flight Control
//		List<String> systemCommadList = _cpacsReader.getSystemParameter(
//				"cpacs/vehicles/aircraft/model/systems/controlDistributors/controlDistributor");
		//TODO now is possible to have relative and absolute deflection of control surface, need integration with pilot command

		_cpacsReader.getControlSurfacePilotCommandIndexAndValue(wingNode, controlSurfaceList, controlSurfaceInt);
		_cpacsReader.getControlSurfacePilotCommandIndexAndValue(horizontalTailNode, controlSurfaceList, controlSurfaceInt);
		_cpacsReader.getControlSurfacePilotCommandIndexAndValue(verticalTailNode, controlSurfaceList, controlSurfaceInt);

		
		
		
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
		kSpeedBreakerTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(kSpeedBreakerMatrix);

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
//		List<String> controlSurfaceList = new ArrayList<String>();
//		NodeList controlSurfaceListToolspecific = MyXMLReaderUtils.getXMLNodeListByPath(
//				jpadXmlReader.getXmlDoc(),
//				"cpacs/toolspecific/UNINA_modules/input/wings/MainWing/controlSurfaces/controlSurface");
//
//
//				controlSurfaceIndexList.add(
//						_cpacsReader.getControlSurfacePilotCommand(
//								"D150_VAMP_W1_CompSeg1_aileron", wingNode, controlSurfaceList));

		NodeList aeroNodeList = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(),
				"cpacs/vehicles/aircraft/model/analyses/aeroPerformanceMap");
		
		double[][] aerodata =  _cpacsReader.getAeroPerformanceMap(aeroNodeList.item(0));
		for (int i = 0;i<aerodata[0].length;i++) {
			cd.add(aerodata[0][i]);
			cdAlpha.add(aerodata[1][i]);
			cy.add(aerodata[2][i]);
			cyBeta.add(aerodata[3][i]);
			cl.add(aerodata[4][i]);
			clAlpha.add(aerodata[5][i]);
			cm.add(aerodata[7][i]);
			cRoll.add(aerodata[6][i]);
			cn.add(aerodata[8][i]);

		}
	}//end CPACS READ VARIABLE

	public void exportToXML(String filePath, String dirPath) throws IOException {
		System.out.println("[JSBSimModel.exportToXML] ...");

		Document doc = makeXmlTree(dirPath);
		
		System.out.println("[JSBSimModel.exportToXML] writing file " + filePath + " ...");
		JPADStaticWriteUtils.writeDocumentToXml(doc, filePath);
		
		
	}	

	private Document makeXmlTree(String dirPath) throws IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();

		
			doc = docBuilder.newDocument();
			
			org.w3c.dom.Element rootElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"fdm_config",
					Tuple.of("name", aircraftName), // TODO: get aircraft name from _cpaceReader
					Tuple.of("version", "2.0"),
					Tuple.of("release", "BETA")
					);
			doc.appendChild(rootElement);
			
			// metrics
		org.w3c.dom.Element metricsElement = doc.createElement("metrics");
		rootElement.appendChild(metricsElement);
		
//		Amount<Area> wingArea = Amount.valueOf(100, SI.SQUARE_METRE);
		JPADStaticWriteUtils.writeSingleNode("wingarea", wingArea , metricsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("wingspan", wingSpan , metricsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("chord", wingMAC , metricsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("htailarea", htSurface , metricsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("htailarea", hTArm , metricsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("htailarm", vtSurface , metricsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("prova", kSpeedBreakerTable , metricsElement, doc);

		
		org.w3c.dom.Element locationElementAero = doc.createElement("location");
		locationElementAero.setAttribute("name", "AERORP");
		locationElementAero.setAttribute("unit", "M");
		metricsElement.appendChild(locationElementAero);
		JPADStaticWriteUtils.writeSingleNode("x",aeroReferenceOrigin[0],locationElementAero,doc);
		JPADStaticWriteUtils.writeSingleNode("y",aeroReferenceOrigin[1],locationElementAero,doc);
		JPADStaticWriteUtils.writeSingleNode("z",aeroReferenceOrigin[2],locationElementAero,doc);
		org.w3c.dom.Element locationElementEye = doc.createElement("location");
		locationElementEye.setAttribute("name", "EYEPOINT");
		locationElementEye.setAttribute("unit", "M");
		metricsElement.appendChild(locationElementEye);
		JPADStaticWriteUtils.writeSingleNode("x",eyePointPosition[0],locationElementEye,doc);
		JPADStaticWriteUtils.writeSingleNode("y",eyePointPosition[1],locationElementEye,doc);
		JPADStaticWriteUtils.writeSingleNode("z",eyePointPosition[2],locationElementEye,doc);

		org.w3c.dom.Element locationElementVRP = doc.createElement("location");
		locationElementVRP.setAttribute("name", "VRP");
		locationElementVRP.setAttribute("unit", "M");
		metricsElement.appendChild(locationElementVRP);
		JPADStaticWriteUtils.writeSingleNode("x",aeroReferenceOrigin[0],locationElementVRP,doc);
		JPADStaticWriteUtils.writeSingleNode("y",aeroReferenceOrigin[1],locationElementVRP,doc);
		JPADStaticWriteUtils.writeSingleNode("z",aeroReferenceOrigin[2],locationElementVRP,doc);
		// mass Balance
		org.w3c.dom.Element massBalance = doc.createElement("mass_balance");
		org.w3c.dom.Element ixxElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "ixx", ixx, 
				3, 6, Tuple.of("unit", "KG*M2"));
		massBalance.appendChild(ixxElem);
		org.w3c.dom.Element iyyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "iyy", iyy, 
				3, 6, Tuple.of("unit", "KG*M2"));
		massBalance.appendChild(iyyElem);
		org.w3c.dom.Element izzElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "izz", izz, 
				3, 6, Tuple.of("unit", "KG*M2"));
		massBalance.appendChild(izzElem);
		
		org.w3c.dom.Element ixyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "ixy", ixy, 
				3, 6, Tuple.of("unit", "KG*M2"));
		massBalance.appendChild(ixyElem);
		
		org.w3c.dom.Element ixzElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "ixz", ixz, 
				3, 6, Tuple.of("unit", "KG*M2"));
		massBalance.appendChild(ixzElem);
		org.w3c.dom.Element iyzElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				doc, "iyz", iyz, 
				3, 6, Tuple.of("unit", "KG*M2"));
		massBalance.appendChild(iyzElem);
		rootElement.appendChild(massBalance);

		org.w3c.dom.Element cgElement = doc.createElement("location");
		cgElement.setAttribute("name", "CG");
		cgElement.setAttribute("unit", "M");
		massBalance.appendChild(cgElement);
		JPADStaticWriteUtils.writeSingleNode("x",cgPosition[0],cgElement,doc);
		JPADStaticWriteUtils.writeSingleNode("y",cgPosition[1],cgElement,doc);
		JPADStaticWriteUtils.writeSingleNode("z",cgPosition[2],cgElement,doc);
		// ground reaction
		
		org.w3c.dom.Element groundReaction = doc.createElement("ground_reactions");
		rootElement.appendChild(groundReaction);
		JSBSimUtils.writeLandingGear(groundReaction, noseGear, coordinateNoseLandingGear, doc, "Nose Gear", "NONE");
		JSBSimUtils.writeLandingGear(groundReaction, mainRightGear, coordinateRightLandingGear, doc, "Right Main Gear", "RIGHT");
		JSBSimUtils.writeLandingGear(groundReaction, mainRightGear, coordinateLeftLandingGear, doc, "Left Main Gear", "LEFT");
		//Propulsion
		
		try {
			JSBSimUtils.createEngineXML(engineRight, "Prova", "turbine", dirPath+"/engine","JET");
		} catch (TransformerException e) {
			e.printStackTrace();
			System.out.println("Engine not created");
		}
		org.w3c.dom.Element propulsionElement = doc.createElement("propulsion");
		rootElement.appendChild(propulsionElement);
		JSBSimUtils.writeEngine
		        (propulsionElement, engineRight, rightEnginePosition, rightEngineRotation, 
				doc, "Prova", "JET");

		JSBSimUtils.writeTank(propulsionElement, tankMatrix, doc, "RIGHT");
		JSBSimUtils.writeTank(propulsionElement, tankMatrix, doc, "LEFT");
		//Flight Control
		org.w3c.dom.Element flightControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"flight_control",
				Tuple.of("name", "FCS :"+aircraftName)
				);
		rootElement.appendChild(flightControlElement);
		org.w3c.dom.Element rollControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"channel",
				Tuple.of("name", "Pitch")
				);
		flightControlElement.appendChild(rollControlElement);
		System.out.println(controlSurfaceList);
		JSBSimUtils.writeSymmetricalControl(
				rollControlElement, controlSurfaceList, doc, 
				controlSurfaceInt, "elevator", "Pitch", 3);
		org.w3c.dom.Element yawControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc,"channel",
				Tuple.of("name", "Yaw")
				);
		flightControlElement.appendChild(yawControlElement);
		JSBSimUtils.writeSymmetricalControl(
				yawControlElement, controlSurfaceList, doc, 
				controlSurfaceInt, "rudder", "Yaw", 4);
		
		rootElement.appendChild(flightControlElement);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return doc;
	}

} //end main

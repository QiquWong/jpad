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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	Double emptyW = null;
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
	List<String> cfxList =  new ArrayList<String>();
	List<String> cfyList =  new ArrayList<String>();
	List<String> cfzList =  new ArrayList<String>();
	List<String> cmxList =  new ArrayList<String>();
	List<String> cmyList =  new ArrayList<String>();
	List<String> cmzList =  new ArrayList<String>();
	List<String> dfxpList = new ArrayList<String>();
	List<String> dfxqList = new ArrayList<String>();
	List<String> dfxrList = new ArrayList<String>();
	List<String> dfypList = new ArrayList<String>();
	List<String> dfyqList = new ArrayList<String>();
	List<String> dfyrList = new ArrayList<String>();
	List<String> dfzpList = new ArrayList<String>();
	List<String> dfzqList = new ArrayList<String>();
	List<String> dfzrList = new ArrayList<String>();
	List<String> dmxpList = new ArrayList<String>();
	List<String> dmxqList = new ArrayList<String>();
	List<String> dmxrList = new ArrayList<String>();
	List<String> dmypList = new ArrayList<String>();
	List<String> dmyqList = new ArrayList<String>();
	List<String> dmyrList = new ArrayList<String>();
	List<String> dmzpList = new ArrayList<String>();
	List<String> dmzqList = new ArrayList<String>();
	List<String> dmzrList = new ArrayList<String>();
	List<String> dfxAileronList = new ArrayList<String>();
	List<String> dfyAileronList = new ArrayList<String>();
	List<String> dfzAileronList = new ArrayList<String>();
	List<String> dmxAileronList = new ArrayList<String>();
	List<String> dmyAileronList = new ArrayList<String>();
	List<String> dmzAileronList = new ArrayList<String>();
	List<String> dfxFlapList = new ArrayList<String>();
	List<String> dfyFlapList = new ArrayList<String>();
	List<String> dfzFlapList = new ArrayList<String>();
	List<String> dmxFlapList = new ArrayList<String>();
	List<String> dmyFlapList = new ArrayList<String>();
	List<String> dmzFlapList = new ArrayList<String>();
	List<String> dfxElevatorList = new ArrayList<String>();
	List<String> dfyElevatorList = new ArrayList<String>();
	List<String> dfzElevatorList = new ArrayList<String>();
	List<String> dmxElevatorList = new ArrayList<String>();
	List<String> dmyElevatorList = new ArrayList<String>();
	List<String> dmzElevatorList = new ArrayList<String>();
	List<String> dfxRudderList = new ArrayList<String>();
	List<String> dfyRudderList = new ArrayList<String>();
	List<String> dfzRudderList = new ArrayList<String>();
	List<String> dmxRudderList = new ArrayList<String>();
	List<String> dmyRudderList = new ArrayList<String>();
	List<String> dmzRudderList = new ArrayList<String>();


	double[] aileronDeflectionAero = null;
	double[] flapDeflectionAero = null;
	double[] elevatorDeflectionAero = null;
	double[] rudderDeflectionAero = null;
	double[] reynoldNumber = null;
	int reynoldDimension = 0;
	double[] machNumber = null;
	int machDimension = 0;




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

	public void readVariablesFromCPACS() throws TiglException, ParserConfigurationException {
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
		emptyW = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
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
		NodeList aeroNodeList = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(),
				"cpacs/vehicles/aircraft/model/analyses/aeroPerformanceMap");

		DocumentBuilderFactory factoryAero = DocumentBuilderFactory.newInstance();
		factoryAero.setNamespaceAware(true);
		DocumentBuilder builderAero;


		builderAero = factoryAero.newDocumentBuilder();
		Document docAero = builderAero.newDocument();
		Node importedNodeAero = docAero.importNode(aeroNodeList.item(0), true);
		docAero.appendChild(importedNodeAero);
		NodeList controlSurfaceAeroPerformanceList = MyXMLReaderUtils.getXMLNodeListByPath(
				docAero, 
				"//controlSurfaces/controlSurface");
		System.out.println("pippo123 = "+controlSurfaceAeroPerformanceList.getLength());

		reynoldNumber = CPACSReader.getReynoldsNumberFromAeroPerformanceMap(docAero);
		reynoldDimension = reynoldNumber.length;
		machNumber = CPACSReader.getMachNumberFromAeroPerformanceMap(docAero);
		machDimension = machNumber.length;
		String cfxPath = "//cfx/text()";
		String cfyPath = "//cfy/text()";
		String cfzPath = "//cfz/text()";
		String cmxPath = "//cmx/text()";
		String cmyPath = "//cmy/text()";
		String cmzPath = "//cmz/text()";
		//Moments and force coefficient in body axis
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cfxList, cfxPath, 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cfyList, cfyPath, 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cfzList, cfzPath, 1);
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cmxList, cmxPath, 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cmyList, cmyPath, 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cmzList, cmzPath, 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), dfxpList, cmzPath, 0);
		//Control surface aeroData
		//Aileron
		aileronDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
				controlSurfaceAeroPerformanceList.item(0));
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), dfxAileronList, "//dcfx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), dfyAileronList, "//dcfy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), dfzAileronList, "//dcfz/text()", aeroNodeList.item(0), 1);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), dmxAileronList, "//dcmx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), dmyAileronList, "//dcmy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), dmzAileronList, "//dcmz/text()", aeroNodeList.item(0), 0);
		//Elevator
		elevatorDeflectionAero = 
				CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
						controlSurfaceAeroPerformanceList.item(3));
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(3), dfxElevatorList, "//dcfx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(3), dfyElevatorList, "//dcfy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(3), dfzElevatorList, "//dcfz/text()", aeroNodeList.item(0), 1);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(3), dmxElevatorList, "//dcmx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(3), dmyElevatorList, "//dcmy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(3), dmzElevatorList, "//dcmz/text()", aeroNodeList.item(0), 0);
		//Rudder
		rudderDeflectionAero = 
				CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap
				(controlSurfaceAeroPerformanceList.item(4));
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(4), dfxRudderList, "//dcfx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(4), dfyRudderList, "//dcfy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(4), dfzRudderList, "//dcfz/text()", aeroNodeList.item(0), 1);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(4), dmxRudderList, "//dcmx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(4), dmyRudderList, "//dcmy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurface
		(controlSurfaceAeroPerformanceList.item(4), dmzRudderList, "//dcmz/text()", aeroNodeList.item(0), 0);
		//Flap
		flapDeflectionAero = 
				CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(controlSurfaceAeroPerformanceList.item(1));
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurfaceFlap
		(controlSurfaceAeroPerformanceList.item(1), controlSurfaceAeroPerformanceList.item(2),
				dfxFlapList, "//dcfx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurfaceFlap
		(controlSurfaceAeroPerformanceList.item(1), controlSurfaceAeroPerformanceList.item(2),
				dfyFlapList, "//dcfy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurfaceFlap
		(controlSurfaceAeroPerformanceList.item(1), controlSurfaceAeroPerformanceList.item(2),
				dfzFlapList, "//dcfz/text()", aeroNodeList.item(0), 1);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurfaceFlap
		(controlSurfaceAeroPerformanceList.item(1), controlSurfaceAeroPerformanceList.item(2),
				dmxFlapList, "//dcmx/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurfaceFlap
		(controlSurfaceAeroPerformanceList.item(1), controlSurfaceAeroPerformanceList.item(2),
				dmyFlapList, "//dcmy/text()", aeroNodeList.item(0), 0);
		CPACSReader.getCoefficientFromAeroPerformanceMapControlSurfaceFlap
		(controlSurfaceAeroPerformanceList.item(1), controlSurfaceAeroPerformanceList.item(2),
				dmzFlapList, "//dcmz/text()", aeroNodeList.item(0), 0);
		//Damping derivative
		//		String prova = "//dampingDerivatives/positiveRates/dcfxdpstar/text()";
		//p-rate
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfxpList, "//dampingDerivatives/positiveRates/dcfxdpstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfypList, "//dampingDerivatives/positiveRates/dcfydpstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfzpList, "//dampingDerivatives/positiveRates/dcfzdpstar/text()", 1);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmxpList, "//dampingDerivatives/positiveRates/dcmxdpstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmypList, "//dampingDerivatives/positiveRates/dcmydpstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmzpList, "//dampingDerivatives/positiveRates/dcmzdpstar/text()", 0);
		//q-rate
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfxqList, "//dampingDerivatives/positiveRates/dcfxdqstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfyqList, "//dampingDerivatives/positiveRates/dcfydqstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfzqList, "//dampingDerivatives/positiveRates/dcfzdqstar/text()", 1);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmxqList, "//dampingDerivatives/positiveRates/dcmxdqstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmyqList, "//dampingDerivatives/positiveRates/dcmydqstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmzqList, "//dampingDerivatives/positiveRates/dcmzdqstar/text()", 0);
		//r-rate
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfxrList, "//dampingDerivatives/positiveRates/dcfxdrstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfyrList, "//dampingDerivatives/positiveRates/dcfydrstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dfzrList, "//dampingDerivatives/positiveRates/dcfzdrstar/text()", 1);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmxrList, "//dampingDerivatives/positiveRates/dcmxdrstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmyrList, "//dampingDerivatives/positiveRates/dcmydrstar/text()", 0);
		CPACSReader.getCoefficientFromAeroPerformanceMap
		(aeroNodeList.item(0), dmzrList, "//dampingDerivatives/positiveRates/dcmzdrstar/text()", 0);



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


		//		
		//		double[][] aerodata =  _cpacsReader.getAeroPerformanceMap(aeroNodeList.item(0));
		//		for (int i = 0;i<aerodata[0].length;i++) {
		//			cd.add(aerodata[0][i]);
		//			cdAlpha.add(aerodata[1][i]);
		//			cy.add(aerodata[2][i]);
		//			cyBeta.add(aerodata[3][i]);
		//			cl.add(aerodata[4][i]);
		//			clAlpha.add(aerodata[5][i]);
		//			cm.add(aerodata[7][i]);
		//			cRoll.add(aerodata[6][i]);
		//			cn.add(aerodata[8][i]);
		//
		//		}
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

			// JPADStaticWriteUtils.writeSingleNode("wingarea", wingArea , metricsElement, doc);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "wingarea",
							wingArea.doubleValue(SI.SQUARE_METRE), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M2")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "wingspan",
							wingSpan.doubleValue(SI.METER), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "chord",
							wingMAC.doubleValue(SI.METER), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "htailarea",
							htSurface.doubleValue(SI.SQUARE_METRE), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M2")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "htailarm",
							hTArm.doubleValue(SI.METER), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "vtailarea",
							vtSurface.doubleValue(SI.SQUARE_METRE), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M2")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "htailarm",
							vtArm.doubleValue(SI.METER), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M2")
							)
					);
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
			org.w3c.dom.Element  emptyWtElement = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "emptywt", emptyW, 
					3, 6, Tuple.of("unit", "KG"));
			massBalance.appendChild(emptyWtElement);

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
//			groundReaction.appendChild(
//					buildLandingGearNodeContent(noseGear, coordinateNoseLandingGear, doc, "Nose Gear", "NONE")
//					);

			JSBSimUtils.writeLandingGear(groundReaction, mainRightGear, coordinateRightLandingGear, doc, "Right Main Gear", "RIGHT");
			JSBSimUtils.writeLandingGear(groundReaction, mainRightGear, coordinateLeftLandingGear, doc, "Left Main Gear", "LEFT");

			// Propulsion
			String engineName = aircraftName.replaceAll("\\s+","_") + "_engine";
			try {
				JSBSimUtils.createEngineXML(engineRight, engineName, "turbine", dirPath+"/engine","JET");
			} catch (TransformerException e) {
				e.printStackTrace();
				System.out.println("Engine not created");
			}
			org.w3c.dom.Element propulsionElement = doc.createElement("propulsion");
			rootElement.appendChild(propulsionElement);
			JSBSimUtils.writeEngine
			(propulsionElement, engineRight, rightEnginePosition, rightEngineRotation, 
					doc, engineName, "JET", tankMatrix, "right");
			JSBSimUtils.writeEngine
			(propulsionElement, engineRight, leftEnginePosition, rightEngineRotation, 
					doc, engineName, "JET", tankMatrix, "left");
			JSBSimUtils.writeTank(propulsionElement, tankMatrix, doc, "RIGHT");
			JSBSimUtils.writeTank(propulsionElement, tankMatrix, doc, "LEFT");
			// Flight Control
			org.w3c.dom.Element flightControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"flight_control",
					Tuple.of("name", "FCS :"+aircraftName)
					);
			rootElement.appendChild(flightControlElement);
			org.w3c.dom.Element pitchControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"channel",
					Tuple.of("name", "Pitch")
					);
			flightControlElement.appendChild(pitchControlElement);
			System.out.println(controlSurfaceList);
			JSBSimUtils.writeSymmetricalControl(
					pitchControlElement, controlSurfaceList, doc, 
					controlSurfaceInt, "elevator", "pitch", 3);
			org.w3c.dom.Element yawControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"channel",
					Tuple.of("name", "Yaw")
					);
			flightControlElement.appendChild(yawControlElement);
			JSBSimUtils.writeSymmetricalControl(
					yawControlElement, controlSurfaceList, doc, 
					controlSurfaceInt, "rudder", "yaw", 4);
			org.w3c.dom.Element RollControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"channel",
					Tuple.of("name", "Roll")
					);
			flightControlElement.appendChild(RollControlElement);
			JSBSimUtils.writeAlileron(
					RollControlElement, controlSurfaceList, doc, 
					controlSurfaceInt, "aileron", "roll", 2);
			org.w3c.dom.Element FlapControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"channel",
					Tuple.of("name", "Flaps")
					);
			flightControlElement.appendChild(FlapControlElement);
			rootElement.appendChild(flightControlElement);
			JSBSimUtils.writeFlap(FlapControlElement, controlSurfaceList, doc, controlSurfaceInt, "flap", 1);
			org.w3c.dom.Element aeroElement = doc.createElement("aerodynamics");
			rootElement.appendChild(aeroElement);

			org.w3c.dom.Element xAxisElement = doc.createElement("axis");
			xAxisElement.setAttribute("name", "X");
			aeroElement.appendChild(xAxisElement);

			org.w3c.dom.Element yAxisElement = doc.createElement("axis");
			yAxisElement.setAttribute("name", "Y");
			aeroElement.appendChild(yAxisElement);
			org.w3c.dom.Element zAxisElement = doc.createElement("axis");
			zAxisElement.setAttribute("name", "Z");
			aeroElement.appendChild(zAxisElement);

			org.w3c.dom.Element rollElement = doc.createElement("axis");
			rollElement.setAttribute("name", "ROLL");
			aeroElement.appendChild(rollElement);

			org.w3c.dom.Element pitchElement = doc.createElement("axis");
			pitchElement.setAttribute("name", "PITCH");
			aeroElement.appendChild(pitchElement);

			org.w3c.dom.Element yawElement = doc.createElement("axis");
			yawElement.setAttribute("name", "YAW");
			aeroElement.appendChild(yawElement);
			org.w3c.dom.Element outputElement = doc.createElement("output");
			outputElement.setAttribute("name", aircraftName+".csv");
			outputElement.setAttribute("rate", String.valueOf(10));
			outputElement.setAttribute("type", "CSV");
			rootElement.appendChild(outputElement);

			JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, cfxList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "axial", xAxisElement);
			JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, cfyList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", yAxisElement);
			JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, cfzList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "normal", zAxisElement);
			JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, cmxList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", rollElement);
			JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, cmyList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", pitchElement);
			JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, cmzList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", yawElement);
			//Aileron
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfxAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "axial", xAxisElement, aileronDeflectionAero, "aileron");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfyAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", yAxisElement, aileronDeflectionAero, "aileron");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfzAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "normal", zAxisElement, aileronDeflectionAero, "aileron");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmxAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", rollElement, aileronDeflectionAero, "aileron");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmyAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", pitchElement, aileronDeflectionAero, "aileron");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmzAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", yawElement, aileronDeflectionAero, "aileron");

			//Elevator
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfxElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "axial", xAxisElement, aileronDeflectionAero, "elevator");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfyElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", yAxisElement, aileronDeflectionAero, "elevator");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfzElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "normal", zAxisElement, aileronDeflectionAero, "elevator");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmxElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", rollElement, aileronDeflectionAero, "elevator");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmyElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", pitchElement, aileronDeflectionAero, "elevator");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmzElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", yawElement, aileronDeflectionAero, "elevator");

			//Rudder
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfxRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "axial", xAxisElement, aileronDeflectionAero, "rudder");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfyRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", yAxisElement, aileronDeflectionAero, "rudder");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dfzRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "normal", zAxisElement, aileronDeflectionAero, "rudder");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmxRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", rollElement, aileronDeflectionAero, "rudder");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmyRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", pitchElement, aileronDeflectionAero, "rudder");
			JSBSimUtils.writeAeroDataBodyAxisControlSurface(
					doc, outputElement,  dmzRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", yawElement, aileronDeflectionAero, "rudder");
			//Damping derivative
			//p-rate
			if (dfxpList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfxpList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "p-axial", xAxisElement);
			}
			if (dfypList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfypList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "p-side", yAxisElement);
			}
			if (dfzpList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfzpList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "p-normal", zAxisElement);
			}
			if (dmxpList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmxpList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "p-roll", rollElement);
			}
			if (dmypList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmypList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "p-pitch", pitchElement);
			}
			if (dmzpList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmzpList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "p-yaw", yawElement);
			}
			// q-rate
			if (dfxqList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfxqList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "q-axial", xAxisElement);
			}
			if (dfyqList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfyqList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "q-side", yAxisElement);
			}
			if (dfzqList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfzqList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "q-normal", zAxisElement);
			}
			if (dmxqList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmxqList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "q-roll", rollElement);
			}
			if (dmyqList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmyqList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "q-pitch", pitchElement);
			}
			if (dmzqList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmzqList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "q-yaw", yawElement);
			}

			//r-rate
			if (dfxrList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfxrList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "r-axial", xAxisElement);
			}
			if (dfyrList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfyrList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "r-side", yAxisElement);
			}
			if (dfzrList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dfzrList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "r-normal", zAxisElement);
			}
			if (dmxrList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmxrList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "r-roll", rollElement);
			}
			if (dmyrList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmyrList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "r-pitch", pitchElement);
			}
			if (dmzrList.size()>0) {
				JSBSimUtils.writeAeroDataBodyAxis(doc, outputElement, dmzrList, machDimension, machNumber, reynoldDimension,
						reynoldNumber, "r-yaw", yawElement);
			}
			JPADStaticWriteUtils.writeSingleNode("property","aero/alpha-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/beta-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/Re",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","velocities/mach",outputElement,doc);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return doc;
	}

	//Start JSBSim script
	public void startJSBSimSimulation(String dirPath, String simulationName) throws IOException, InterruptedException {
		List<String> commandList = new ArrayList<String>();
		String Simulation1 = "JSBSim.exe";
		String Simulation2 = "--script=scripts/"+simulationName;
		//		String Simulation3 = "C:\\Users\\Lenovo PC\\Documents\\Tesipeppe\\Prova\\JSBSim";
		commandList.add(Simulation1);
		commandList.add(Simulation2);
		JSBSimUtils.runJSBSIM(commandList, dirPath);

	}
	public void writeInitialConditionsFile(String initializePath, double ubody, double vbody, double wbody, double longitude,
			double latitude, double phi, double theta, double psi, double altitude, double elevation, double hwind) {
		Document doc = writeInitialCondition(ubody, vbody, wbody,
				longitude, latitude, phi, theta, psi, altitude, elevation, hwind);

		System.out.println("[JSBSimModel.exportToXML] writing file " + initializePath + " ...");
		JPADStaticWriteUtils.writeDocumentToXml(doc, initializePath);
	}
	public Document writeInitialCondition(double ubody, double vbody, double wbody, double longitude,
			double latitude, double phi, double theta, double psi, double altitude, double elevation, double hwind) {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document doc = null;

		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			org.w3c.dom.Element rootElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"initialize",
					Tuple.of("name", "initialCondition") // TODO: get aircraft name from _cpaceReader
					);
			doc.appendChild(rootElement);
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "ubody",
					ubody, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "M/SEC")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "vbody",
					vbody, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "M/SEC")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "wbody",
					wbody, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "M/SEC")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "longitude",
					longitude, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "DEG")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "latitude",
					latitude, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "DEG")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "phi",
					phi, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "DEG")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "theta",
					theta, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "DEG")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "psi",
					psi, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "DEG")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "altitude",
					altitude, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "M")
					));
			rootElement.appendChild(JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					doc, "elevation",
					elevation, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "M")
					));
			JPADStaticWriteUtils.writeSingleNode("hwind", hwind, rootElement, doc);
			return doc;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}





} //end main

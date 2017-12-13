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
	List<String> dmxFlapListInner = new ArrayList<String>();
	List<String> dmyFlapListInner = new ArrayList<String>();
	List<String> dmzFlapListInner = new ArrayList<String>();
	List<String> dmxFlapListOuter = new ArrayList<String>();
	List<String> dmyFlapListOuter = new ArrayList<String>();
	List<String> dmzFlapListOuter = new ArrayList<String>();
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
	List<String> cdList =  new ArrayList<String>();
	List<String> cyList =  new ArrayList<String>();
	List<String> clList =  new ArrayList<String>();
	List<String> cdListAileron =  new ArrayList<String>();
	List<String> cyListAileron =  new ArrayList<String>();
	List<String> clListAileron =  new ArrayList<String>();
	List<String> cdListFlapInner =  new ArrayList<String>();
	List<String> cyListFlapInner =  new ArrayList<String>();
	List<String> clListFlapInner =  new ArrayList<String>();
	List<String> cdListFlapOuter =  new ArrayList<String>();
	List<String> cyListFlapOuter =  new ArrayList<String>();
	List<String> clListFlapOuter =  new ArrayList<String>();
	List<String> cdListElevator =  new ArrayList<String>();
	List<String> cyListElevator =  new ArrayList<String>();
	List<String> clListElevator =  new ArrayList<String>();
	List<String> cdListRudder =  new ArrayList<String>();
	List<String> cyListRudder =  new ArrayList<String>();
	List<String> clListRudder =  new ArrayList<String>();
	List<String> cdListp =  new ArrayList<String>();
	List<String> cyListp =  new ArrayList<String>();
	List<String> clListp =  new ArrayList<String>();
	List<String> cdListq =  new ArrayList<String>();
	List<String> cyListq =  new ArrayList<String>();
	List<String> clListq =  new ArrayList<String>();
	List<String> cdListr =  new ArrayList<String>();
	List<String> cyListr =  new ArrayList<String>();
	List<String> clListr =  new ArrayList<String>();
	double[] aileronDeflectionAero = null;
	double[] flapDeflectionAero = null;
	double[] elevatorDeflectionAero = null;
	double[] rudderDeflectionAero = null;
	double[] reynoldNumber = null;
	int reynoldDimension = 0;
	double[] machNumber = null;
	double[] betaVector = null;
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
		aeroReferenceOrigin[0] = macVectorWing[1] + (0.25)*macVectorWing[0];	
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
						+ "mOEM/mEM/massDescription/mass"));
		System.out.println("Empty weight = "+ emptyW);
		emptyWeight =  Amount.valueOf (emptyW,SI.KILOGRAM);
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
		NodeList engineList = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/engines/engine");
		engineRight = _cpacsReader.getEngine(engineList.item(0));
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
		reynoldNumber = CPACSReader.getReynoldsNumberFromAeroPerformanceMap(docAero);
		reynoldDimension = reynoldNumber.length;
		machNumber = CPACSReader.getMachNumberFromAeroPerformanceMap(docAero);
		betaVector = CPACSReader.getYawFromAeroPerformanceMap(docAero);

		machDimension = machNumber.length;
		String cfxPath = "//CD/text()";
		String cfyPath = "//CY/text()";
		String cfzPath = "//CL/text()";
		String cmxPath = "//Cl/text()";
		String cmyPath = "//Cm/text()";
		String cmzPath = "//Cn/text()";
		//Moments and force coefficient in body axis
		cdList = _cpacsReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cfxPath, 0);
		cyList = _cpacsReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cfyPath, 0);
		clList = _cpacsReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cfzPath, 0);
		cmxList = _cpacsReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cmxPath, 0);
		cmyList = _cpacsReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0), cmyPath, 0);
		cmzList = _cpacsReader.getCoefficientFromAeroPerformanceMap(aeroNodeList.item(0),  cmzPath, 0);
		
//		cdList = _cpacsReader.getAeroDragCoefficientFromAeroPerformanceMap(aeroNodeList.item(0),cfxPath,cfyPath,cfzPath, 0);
//		cyList = _cpacsReader.getAeroSideCoefficientFromAeroPerformanceMap(aeroNodeList.item(0),cfxPath,cfyPath,cfzPath, 0);
//		clList = _cpacsReader.getAeroLiftCoefficientFromAeroPerformanceMap(aeroNodeList.item(0),cfxPath,cfyPath,cfzPath, 0);

		//Control surface aeroData
		String cfxPathDamping = "//dampingDerivatives/positiveRates/dcfx/text()";
		String cfyPathDamping = "//dampingDerivatives/positiveRates/dcfy/text()";
		String cfzPathDamping = "//dampingDerivatives/positiveRates/dcfz/text()";
		//Aileron
		aileronDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
				controlSurfaceAeroPerformanceList.item(0));
		cdListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), "//dCD/text()", aeroNodeList.item(0), 0);
		cyListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), "//dCY/text()", aeroNodeList.item(0), 0);
		clListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), "//dCL/text()", aeroNodeList.item(0), 0);
//		cdListAileron = _cpacsReader.getDragCoefficientFromAeroPerformanceMapControlSurface(
//				controlSurfaceAeroPerformanceList.item(0), aeroNodeList.item(0), 0);
//		clListAileron = _cpacsReader.getLiftCoefficientFromAeroPerformanceMapControlSurface(
//				controlSurfaceAeroPerformanceList.item(0), aeroNodeList.item(0), 0);
//		cyListAileron = _cpacsReader.getSideCoefficientFromAeroPerformanceMapControlSurface(
//				controlSurfaceAeroPerformanceList.item(0), aeroNodeList.item(0), 0);

		dmxAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), "//dCl/text()", aeroNodeList.item(0), 0);
		dmyAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), "//dCm/text()", aeroNodeList.item(0), 0);
		dmzAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(0), "//dCn/text()", aeroNodeList.item(0), 0);
		//Elevator
		elevatorDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
				controlSurfaceAeroPerformanceList.item(3));
		cdListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(3), "//dCD/text()", aeroNodeList.item(0), 0);
		clListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(3), "//dCL/text()", aeroNodeList.item(0), 0);
		cyListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(3), "//dCY/text()", aeroNodeList.item(0), 0);
		dmxElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(3), "//dCl/text()", aeroNodeList.item(0), 0);
		dmyElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(3), "//dCm/text()", aeroNodeList.item(0), 0);
		dmzElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(3), "//dCn/text()", aeroNodeList.item(0), 0);
		//Rudder
		rudderDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap
				(controlSurfaceAeroPerformanceList.item(4));
		cdListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(4), "//dCD/text()", aeroNodeList.item(0), 0);
		clListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(4), "//dCL/text()", aeroNodeList.item(0), 0);
		cyListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(4), "//dCY/text()", aeroNodeList.item(0), 0);
		dmxRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(4), "//dCl/text()", aeroNodeList.item(0), 0);
		dmyRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(4), "//dCm/text()", aeroNodeList.item(0), 0);
		dmzRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(4), "//dCn/text()", aeroNodeList.item(0), 0);
		//Flap
		flapDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
				controlSurfaceAeroPerformanceList.item(1));

		
		cdListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(1), "//dCD/text()", aeroNodeList.item(0), 0);
		clListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(1), "//dCL/text()", aeroNodeList.item(0), 0);
		cyListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(1), "//dCY/text()", aeroNodeList.item(0), 0);
		dmxFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(1), "//dCl/text()", aeroNodeList.item(0), 0);
		dmyFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(1), "//dCm/text()", aeroNodeList.item(0), 0);
		dmzFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(1), "//dCn/text()", aeroNodeList.item(0), 0);
		
		cdListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(2), "//dCD/text()", aeroNodeList.item(0), 0);
		clListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(2), "//dCL/text()", aeroNodeList.item(0), 0);
		cyListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(2), "//dCY/text()", aeroNodeList.item(0), 0);
		dmxFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(2), "//dCl/text()", aeroNodeList.item(0), 0);
		dmyFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(2), "//dCm/text()", aeroNodeList.item(0), 0);
		dmzFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
				controlSurfaceAeroPerformanceList.item(2), "//dCn/text()", aeroNodeList.item(0), 0);
		//Damping derivative
		//p-rate
		dfxpList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdpstar/text()", 0);
		dfypList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydpstar/text()", 0);
		dfzpList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdpstar/text()", 0);
		cdListp = _cpacsReader.getAeroDragCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		cyListp = _cpacsReader.getAeroSideCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		clListp = _cpacsReader.getAeroLiftCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		dmxpList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdpstar/text()", 0);
		dmypList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydpstar/text()", 0);
		dmzpList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdpstar/text()", 0);
		//q-rate
//		dfxqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdqstar/text()", 0);
//		dfyqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydqstar/text()", 0);
//		dfzqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdqstar/text()", 0);
		cdListq = _cpacsReader.getAeroDragCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		cyListq = _cpacsReader.getAeroSideCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		clListq = _cpacsReader.getAeroLiftCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		dmxqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdqstar/text()", 0);
		dmyqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydqstar/text()", 0);
		dmzqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdqstar/text()", 0);
		//r-rate
//		dfxrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdrstar/text()", 0);
//		dfyrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydrstar/text()", 0);
//		dfzrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdrstar/text()", 0);
		cdListr = _cpacsReader.getAeroDragCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		cyListr = _cpacsReader.getAeroSideCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		clListr = _cpacsReader.getAeroLiftCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0),cfxPathDamping,cfyPathDamping,cfzPathDamping, 0);
		dmxrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdrstar/text()", 0);
		dmyrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydrstar/text()", 0);
		dmzrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdrstar/text()", 0);



		_cpacsReader.getControlSurfacePilotCommandIndexAndValue(wingNode, controlSurfaceList, controlSurfaceInt);
		_cpacsReader.getControlSurfacePilotCommandIndexAndValue(horizontalTailNode, controlSurfaceList, controlSurfaceInt);
		_cpacsReader.getControlSurfacePilotCommandIndexAndValue(verticalTailNode, controlSurfaceList, controlSurfaceInt);
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
		Document docAero = null;

		try {
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
			docAero = docBuilder.newDocument();
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
			groundReaction.appendChild(
					JSBSimUtils.createLandingGearElement(noseGear, coordinateNoseLandingGear, doc, "Nose Gear", "NONE")
					);
			//			groundReaction.appendChild(
			//					buildLandingGearNodeContent(noseGear, coordinateNoseLandingGear, doc, "Nose Gear", "NONE")
			//					);
			groundReaction.appendChild(
					JSBSimUtils.createLandingGearElement(mainRightGear, coordinateRightLandingGear, doc, "Right Main Gear", "RIGHT")
					);
			groundReaction.appendChild(
					JSBSimUtils.createLandingGearElement(mainRightGear, coordinateLeftLandingGear, doc, "Left Main Gear", "LEFT")
					);

			rootElement.appendChild(groundReaction);
			// Propulsion
			String engineName = aircraftName.replaceAll("\\s+","_") + "_engine";
			try {
				JSBSimUtils.createEngineXML( engineRight, engineName, "turbine", dirPath+"/engine","JET");
			} catch (TransformerException e) {
				e.printStackTrace();
				System.out.println("Engine not created");
			}
			org.w3c.dom.Element propulsionElement = doc.createElement("propulsion");
			rootElement.appendChild(propulsionElement);
			propulsionElement.appendChild(
			JSBSimUtils.createEngineElement( engineRight, rightEnginePosition, rightEngineRotation, 
					doc, engineName, "JET", tankMatrix, "right"));
			
			propulsionElement.appendChild(
			JSBSimUtils.createEngineElement( engineRight, leftEnginePosition, rightEngineRotation
					,doc, engineName, "JET", tankMatrix, "left"));

			rootElement.appendChild(
					JSBSimUtils.createTankElement( tankMatrix, doc, "RIGHT", propulsionElement)
					);
			rootElement.appendChild(
					JSBSimUtils.createTankElement( tankMatrix, doc, "LEFT", propulsionElement)
					);
			// Flight Control
			org.w3c.dom.Element flightControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
					doc,"flight_control",
					Tuple.of("name", "FCS :"+aircraftName)
					);
			rootElement.appendChild(flightControlElement);

//			org.w3c.dom.Element pitchControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Pitch")
//					);
//			flightControlElement.appendChild(pitchControlElement);
			flightControlElement.appendChild(
			JSBSimUtils.createSymmetricalControl( controlSurfaceList, doc, 
					controlSurfaceInt, "elevator", "pitch", 3, "Pitch"));
//			org.w3c.dom.Element yawControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Yaw")
//					);
//			flightControlElement.appendChild(yawControlElement);
			flightControlElement.appendChild(
					JSBSimUtils.createSymmetricalControl(
							controlSurfaceList, doc,controlSurfaceInt, "rudder", "yaw", 4, "Yaw")
					);
//			org.w3c.dom.Element RollControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Roll")
//					);
//			flightControlElement.appendChild(RollControlElement);
			flightControlElement.appendChild(
					JSBSimUtils.createAlileronElement(
							controlSurfaceList, doc, controlSurfaceInt, "aileron", "roll", 2, "Roll")
					);
//			org.w3c.dom.Element FlapControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Flaps")
//					);
//			flightControlElement.appendChild(FlapControlElement);
			System.out.println("----------------------------------------------");
			flightControlElement.appendChild(
					JSBSimUtils.createFlapElement(
							controlSurfaceList, doc, controlSurfaceInt, "flap", 1, "Flap")
					);
			System.out.println("----------------------------------------------");

			
			rootElement.appendChild(flightControlElement);

			org.w3c.dom.Element aeroElement = doc.createElement("aerodynamics");
			rootElement.appendChild(aeroElement);

//			org.w3c.dom.Element xAxisElement = doc.createElement("axis");
//			xAxisElement.setAttribute("name", "X");
//			aeroElement.appendChild(xAxisElement);
//
//			org.w3c.dom.Element yAxisElement = doc.createElement("axis");
//			yAxisElement.setAttribute("name", "Y");
//			aeroElement.appendChild(yAxisElement);
//			org.w3c.dom.Element zAxisElement = doc.createElement("axis");
//			zAxisElement.setAttribute("name", "Z");
//			aeroElement.appendChild(zAxisElement);
//
//			org.w3c.dom.Element rollElement = doc.createElement("axis");
//			rollElement.setAttribute("name", "ROLL");
//			aeroElement.appendChild(rollElement);
//
//			org.w3c.dom.Element pitchElement = doc.createElement("axis");
//			pitchElement.setAttribute("name", "PITCH");
//			aeroElement.appendChild(pitchElement);
//
//			org.w3c.dom.Element yawElement = doc.createElement("axis");
//			yawElement.setAttribute("name", "YAW");
//			aeroElement.appendChild(yawElement);
			org.w3c.dom.Element outputElement = doc.createElement("output");
			outputElement.setAttribute("name", aircraftName+".csv");
			outputElement.setAttribute("rate", String.valueOf(10));
			outputElement.setAttribute("type", "CSV");
			rootElement.appendChild(outputElement);
			org.w3c.dom.Element axisElementDrag = doc.createElement("axis");
			axisElementDrag.setAttribute("name", "DRAG");
			aeroElement.appendChild(axisElementDrag);
			
			org.w3c.dom.Element axisElementSide = doc.createElement("axis");
			axisElementSide.setAttribute("name", "SIDE");
			aeroElement.appendChild(axisElementSide);
			
			org.w3c.dom.Element axisElementLift = doc.createElement("axis");
			axisElementLift.setAttribute("name", "LIFT");
			aeroElement.appendChild(axisElementLift);
			
			org.w3c.dom.Element axisElementRoll = doc.createElement("axis");
			axisElementRoll.setAttribute("name", "ROLL");
			aeroElement.appendChild(axisElementRoll);
			
			org.w3c.dom.Element axisElementPitch = doc.createElement("axis");
			axisElementPitch.setAttribute("name", "PITCH");
			aeroElement.appendChild(axisElementPitch);
			
			org.w3c.dom.Element axisElementYaw = doc.createElement("axis");
			axisElementYaw.setAttribute("name", "YAW");
			aeroElement.appendChild(axisElementYaw);
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
							doc, outputElement, cdList, machDimension, machNumber, 
							reynoldDimension,reynoldNumber, "drag", axisElementDrag, aeroElement));
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
							doc, outputElement, cyList, machDimension, machNumber,
							reynoldDimension, reynoldNumber, "side", axisElementSide, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, clList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "lift", axisElementLift, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cmxList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", axisElementRoll, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cmyList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", axisElementPitch, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cmzList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", axisElementYaw, aeroElement));
			//Aileron
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cdListAileron, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "drag", axisElementDrag, aileronDeflectionAero, "aileron", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cyListAileron, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", axisElementSide, aileronDeflectionAero, "aileron", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, clListAileron, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "lift", axisElementLift, aileronDeflectionAero, "aileron", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", axisElementRoll, aileronDeflectionAero, "aileron", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", axisElementPitch, aileronDeflectionAero, "aileron", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzAileronList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", axisElementYaw, aileronDeflectionAero, "aileron", outputElement,betaVector, aeroElement));

			//Elevator

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cdListElevator, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "drag", axisElementDrag, elevatorDeflectionAero, "elevator", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cyListElevator, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", axisElementSide, elevatorDeflectionAero, "elevator", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, clListElevator, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "lift", axisElementLift, elevatorDeflectionAero, "elevator", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", axisElementRoll, elevatorDeflectionAero, "elevator", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", axisElementPitch, elevatorDeflectionAero, "elevator", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzElevatorList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", axisElementYaw, elevatorDeflectionAero, "elevator", outputElement,betaVector, aeroElement));

			//Rudder

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cdListRudder, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "drag", axisElementDrag, rudderDeflectionAero, "rudder", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cyListRudder, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", axisElementSide, rudderDeflectionAero, "rudder", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, clListRudder, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "lift", axisElementLift, rudderDeflectionAero, "rudder", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", axisElementRoll, rudderDeflectionAero, "rudder", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", axisElementPitch, rudderDeflectionAero, "rudder", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzRudderList, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", axisElementYaw, rudderDeflectionAero, "rudder", outputElement,betaVector, aeroElement));

			//Flap Inner

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cdListFlapInner, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "drag", axisElementDrag, flapDeflectionAero, "flap_inner", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cyListFlapInner, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", axisElementSide, flapDeflectionAero, "flap_inner", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, clListFlapInner, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "lift", axisElementLift, flapDeflectionAero, "flap_inner", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxFlapListInner, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", axisElementRoll, flapDeflectionAero, "flap_inner", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyFlapListInner, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", axisElementPitch, flapDeflectionAero, "flap_inner", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzFlapListInner, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", axisElementYaw, flapDeflectionAero, "flap_inner", outputElement,betaVector, aeroElement));
			
			//Flap Outer

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cdListFlapOuter, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "drag", axisElementDrag, flapDeflectionAero, "flap_outer", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cyListFlapOuter, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "side", axisElementSide, flapDeflectionAero, "flap_outer", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, clListFlapOuter, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "lift", axisElementLift, flapDeflectionAero, "flap_outer", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxFlapListOuter, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "roll", axisElementRoll, flapDeflectionAero, "flap_outer", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyFlapListOuter, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "pitch", axisElementPitch, flapDeflectionAero, "flap_outer", outputElement,betaVector, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzFlapListOuter, machDimension, machNumber, reynoldDimension,
					reynoldNumber, "yaw", axisElementYaw, flapDeflectionAero, "flap_outer", outputElement,betaVector, aeroElement));
			//Damping derivative
//			//p-rate
//			if (cdListp.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, cdListp, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "p-drag", axisElementDrag));
//			}
//			if (cyListp.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, cyListp, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "p-side", axisElementSide));
//			}
//			if (clListp.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, clListp, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "p-lift", axisElementLift));
//			}
//			if (dmxpList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmxpList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "p-roll", axisElementRoll));
//			}
//			if (dmypList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmypList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "p-pitch", axisElementPitch));
//			}
//			if (dmzpList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmzpList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "p-yaw", axisElementYaw));
//			}
//			// q-rate
//			if (cdListq.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, cdListq, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "q-drag", axisElementDrag));
//			}
//			if (cyListq.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, cyListq, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "q-side", axisElementSide));
//			}
//			if (clListq.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, clListq, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "q-lift", axisElementLift));
//			}
//			if (dmxqList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmxqList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "q-roll", axisElementRoll));
//			}
//			if (dmyqList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmyqList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "q-pitch", axisElementPitch));
//			}
//			if (dmzqList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmzqList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "q-yaw", axisElementYaw));
//			}
//
//			//r-rate
//			if (cdListr.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, cdListr, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "r-drag", axisElementDrag));
//			}
//			if (cyListr.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, cyListr, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "r-side", axisElementSide));
//			}
//			if (clListr.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, clListr, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "r-lift", axisElementLift));
//			}
//			if (dmxrList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmxrList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "r-roll", axisElementRoll));
//			}
//			if (dmyrList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmyrList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "r-pitch", axisElementPitch));
//			}
//			if (dmzrList.size()>0) {
//				aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisElement(
//						doc, outputElement, dmzrList, machDimension, machNumber, reynoldDimension,
//						reynoldNumber, "r-yaw", axisElementYaw));
//			}
			JPADStaticWriteUtils.writeSingleNode("property","aero/alpha-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/beta-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","aero/Re",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","velocities/mach",outputElement,doc);
			
			/*

velocities/h-dot-fps (R)
velocities/v-north-fps (R)
velocities/v-east-fps (R)
velocities/v-down-fps (R)
velocities/u-fps (R)
velocities/v-fps (R)
velocities/w-fps (R)
velocities/p-rad_sec (R)
velocities/q-rad_sec (R)
velocities/r-rad_sec (R)
 
position/h-sl-ft (RW)
position/h-sl-meters (RW)

attitude/phi-deg (R)
attitude/theta-deg (R)
attitude/psi-deg (R)

velocities/vtrue-kts (R)

propulsion/engine/thrust-lbs (R)

			 */

			JPADStaticWriteUtils.writeSingleNode("property","propulsion/engine/thrust-lbs",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","propulsion/engine[1]/thrust-lbs",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","position/h-sl-meters",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","attitude/phi-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","attitude/theta-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","attitude/psi-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","velocities/vtrue-kts",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","velocities/h-dot-fps",outputElement,doc);
			
			JPADStaticWriteUtils.writeSingleNode("property","velocities/p-rad_sec",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","velocities/q-rad_sec",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","velocities/r-rad_sec",outputElement,doc);

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

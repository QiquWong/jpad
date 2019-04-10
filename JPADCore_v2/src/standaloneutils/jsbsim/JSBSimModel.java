package standaloneutils.jsbsim;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

import aircraft.Aircraft;
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
	String aircraftConfiguration = new String();
	Node wingNode = null;
	Node horizontalTailNode = null;
	Node verticalTailNode = null;
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
	double wspan = 0.0;
	double[] aeroReferenceOrigin = new double[3];
	double[] cgPosition = new double[3];
	double[] eyePointPosition = new double[3];
	double macVectorWing[] = new double [4];
	double[] macVectorHT = new double [4];
	double[] macVectorVT = new double [4];
	Double[] wingPosition = new Double [3];
	String kSpeedBreakerTable = new String();
	double ixx = 0.0; 
	double iyy = 0.0; 
	double izz = 0.0; 
	double ixy = 0.0; 
	double ixz = 0.0; 
	double iyz = 0.0; 
	double[][] tankMatrix = null;
	double tankVector[] = new double [4];
	int flagTank = 0;
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
	ArrayList<ArrayList<Double>> ListListFlightLevel = new ArrayList<ArrayList<Double>>();
	List<String> listFlightLevel =  new ArrayList<String>();
	ArrayList<ArrayList<String>> listListEngineData = new ArrayList<ArrayList<String>>();
	List<String> machEngineList = new ArrayList<String>();
	List<String> throttleEngineList = new ArrayList<String>();
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
	double[] machEngineIdle = null;
	double[] commandEngineIdle = null;
	double[] machEngineMilitary = null;
	double[] commandEngineMilitary = null;
	double[] flightLevelVectorIdle = null;
	double[] flightLevelVectorMil = null;
	int[] controlSurfaceVectorIndex = new int [5];
	List<String> enginePerformanceIdle =  new ArrayList<String>();
	List<String> enginePerformanceMilitaryThrust =  new ArrayList<String>();
	List<String> enginePerformanceMap =  new ArrayList<String>();
	List<String> cmxList =  new ArrayList<String>();
	List<String> cmyList =  new ArrayList<String>();
	List<String> cmzList =  new ArrayList<String>();
	List<String> dmxpList = new ArrayList<String>();
	List<String> dmxqList = new ArrayList<String>();
	List<String> dmxrList = new ArrayList<String>();
	List<String> dmypList = new ArrayList<String>();
	List<String> dmyqList = new ArrayList<String>();
	List<String> dmyrList = new ArrayList<String>();
	List<String> dmzpList = new ArrayList<String>();
	List<String> dmzqList = new ArrayList<String>();
	List<String> dmzrList = new ArrayList<String>();
	List<String> dmxAileronList = new ArrayList<String>();
	List<String> dmyAileronList = new ArrayList<String>();
	List<String> dmzAileronList = new ArrayList<String>();
	List<String> dmxFlapListInner = new ArrayList<String>();
	List<String> dmyFlapListInner = new ArrayList<String>();
	List<String> dmzFlapListInner = new ArrayList<String>();
	List<String> dmxFlapListOuter = new ArrayList<String>();
	List<String> dmyFlapListOuter = new ArrayList<String>();
	List<String> dmzFlapListOuter = new ArrayList<String>();
	List<String> dmxElevatorList = new ArrayList<String>();
	List<String> dmyElevatorList = new ArrayList<String>();
	List<String> dmzElevatorList = new ArrayList<String>();
	List<String> dmxRudderList = new ArrayList<String>();
	List<String> dmyRudderList = new ArrayList<String>();
	List<String> dmzRudderList = new ArrayList<String>();
	List<String> cfxList =  new ArrayList<String>();
	List<String> cfyList =  new ArrayList<String>();
	List<String> cfzList =  new ArrayList<String>();
	List<String> cfxListAileron =  new ArrayList<String>();
	List<String> cfyListAileron =  new ArrayList<String>();
	List<String> cfzListAileron =  new ArrayList<String>();
	List<String> cfxListFlapInner =  new ArrayList<String>();
	List<String> cfyListFlapInner =  new ArrayList<String>();
	List<String> cfzListFlapInner =  new ArrayList<String>();
	List<String> cfxListFlapOuter =  new ArrayList<String>();
	List<String> cfyListFlapOuter =  new ArrayList<String>();
	List<String> cfzListFlapOuter =  new ArrayList<String>();
	List<String> cfxListElevator =  new ArrayList<String>();
	List<String> cfyListElevator =  new ArrayList<String>();
	List<String> cfzListElevator =  new ArrayList<String>();
	List<String> cfxListRudder =  new ArrayList<String>();
	List<String> cfyListRudder =  new ArrayList<String>();
	List<String> cfzListRudder =  new ArrayList<String>();
	List<String> dcfxListp =  new ArrayList<String>();
	List<String> dcfyListp =  new ArrayList<String>();
	List<String> dcfzListp =  new ArrayList<String>();
	List<String> dcfxListq =  new ArrayList<String>();
	List<String> dcfyListq =  new ArrayList<String>();
	List<String> dcfzListq =  new ArrayList<String>();
	List<String> dcfxListr =  new ArrayList<String>();
	List<String> dcfyListr =  new ArrayList<String>();
	List<String> dcfzListr =  new ArrayList<String>();
	double[] aileronDeflectionAero = null;
	double[] flapDeflectionAeroInner = null;
	double[] flapDeflectionAeroOuter = null;
	double[] elevatorDeflectionAero = null;
	double[] rudderDeflectionAero = null;

	//BWB controlSurface
	List<String> dfxInnerElevatorList = new ArrayList<String>();
	List<String> dfyInnerElevatorList = new ArrayList<String>();
	List<String> dfzInnerElevatorList = new ArrayList<String>();
	List<String> dmxInnerElevatorList = new ArrayList<String>();
	List<String> dmyInnerElevatorList = new ArrayList<String>();
	List<String> dmzInnerElevatorList = new ArrayList<String>();
	List<String> dfxInnerElevonList = new ArrayList<String>();
	List<String> dfyInnerElevonList = new ArrayList<String>();
	List<String> dfzInnerElevonList = new ArrayList<String>();
	List<String> dmxInnerElevonList = new ArrayList<String>();
	List<String> dmyInnerElevonList = new ArrayList<String>();
	List<String> dmzInnerElevonList = new ArrayList<String>();
	List<String> dfxMiddleElevonList = new ArrayList<String>();
	List<String> dfyMiddleElevonList = new ArrayList<String>();
	List<String> dfzMiddleElevonList = new ArrayList<String>();
	List<String> dmxMiddleElevonList = new ArrayList<String>();
	List<String> dmyMiddleElevonList = new ArrayList<String>();
	List<String> dmzMiddleElevonList = new ArrayList<String>();
	List<String> dfxOuterElevonList = new ArrayList<String>();
	List<String> dfyOuterElevonList = new ArrayList<String>();
	List<String> dfzOuterElevonList = new ArrayList<String>();
	List<String> dmxOuterElevonList = new ArrayList<String>();
	List<String> dmyOuterElevonList = new ArrayList<String>();
	List<String> dmzOuterElevonList = new ArrayList<String>();
	List<String> dfxSpoilerList = new ArrayList<String>();
	List<String> dfySpoilerList = new ArrayList<String>();
	List<String> dfzSpoilerList = new ArrayList<String>();
	List<String> dmxSpoilerList = new ArrayList<String>();
	List<String> dmySpoilerList = new ArrayList<String>();
	List<String> dmzSpoilerList = new ArrayList<String>();
	List<String> dfxAllSpeedAileronList = new ArrayList<String>();
	List<String> dfyAllSpeedAileronList = new ArrayList<String>();
	List<String> dfzAllSpeedAileronList = new ArrayList<String>();
	List<String> dmxAllSpeedAileronList = new ArrayList<String>();
	List<String> dmyAllSpeedAileronList = new ArrayList<String>();
	List<String> dmzAllSpeedAileronList = new ArrayList<String>();
	List<String> dfxVTRudder1List = new ArrayList<String>();
	List<String> dfyVTRudder1List = new ArrayList<String>();
	List<String> dfzVTRudder1List = new ArrayList<String>();
	List<String> dmxVTRudder1List = new ArrayList<String>();
	List<String> dmyVTRudder1List = new ArrayList<String>();
	List<String> dmzVTRudder1List = new ArrayList<String>();
	List<String> dfxVTRudder2List = new ArrayList<String>();
	List<String> dfyVTRudder2List = new ArrayList<String>();
	List<String> dfzVTRudder2List = new ArrayList<String>();
	List<String> dmxVTRudder2List = new ArrayList<String>();
	List<String> dmyVTRudder2List = new ArrayList<String>();
	List<String> dmzVTRudder2List = new ArrayList<String>();
	List<String> dfxInnerElevatorSymmList = new ArrayList<String>();
	List<String> dfyInnerElevatorSymmList = new ArrayList<String>();
	List<String> dfzInnerElevatorSymmList = new ArrayList<String>();
	List<String> dmxInnerElevatorSymmList = new ArrayList<String>();
	List<String> dmyInnerElevatorSymmList = new ArrayList<String>();
	List<String> dmzInnerElevatorSymmList = new ArrayList<String>();
	List<String> dfxInnerElevonSymmList = new ArrayList<String>();
	List<String> dfyInnerElevonSymmList = new ArrayList<String>();
	List<String> dfzInnerElevonSymmList = new ArrayList<String>();
	List<String> dmxInnerElevonSymmList = new ArrayList<String>();
	List<String> dmyInnerElevonSymmList = new ArrayList<String>();
	List<String> dmzInnerElevonSymmList = new ArrayList<String>();
	List<String> dfxMiddleElevonSymmList = new ArrayList<String>();
	List<String> dfyMiddleElevonSymmList = new ArrayList<String>();
	List<String> dfzMiddleElevonSymmList = new ArrayList<String>();
	List<String> dmxMiddleElevonSymmList = new ArrayList<String>();
	List<String> dmyMiddleElevonSymmList = new ArrayList<String>();
	List<String> dmzMiddleElevonSymmList = new ArrayList<String>();
	List<String> dfxOuterElevonSymmList = new ArrayList<String>();
	List<String> dfyOuterElevonSymmList = new ArrayList<String>();
	List<String> dfzOuterElevonSymmList = new ArrayList<String>();
	List<String> dmxOuterElevonSymmList = new ArrayList<String>();
	List<String> dmyOuterElevonSymmList = new ArrayList<String>();
	List<String> dmzOuterElevonSymmList = new ArrayList<String>();
	List<String> dfxSpoilerSymmList = new ArrayList<String>();
	List<String> dfySpoilerSymmList = new ArrayList<String>();
	List<String> dfzSpoilerSymmList = new ArrayList<String>();
	List<String> dmxSpoilerSymmList = new ArrayList<String>();
	List<String> dmySpoilerSymmList = new ArrayList<String>();
	List<String> dmzSpoilerSymmList = new ArrayList<String>();
	List<String> dfxAllSpeedAileronSymmList = new ArrayList<String>();
	List<String> dfyAllSpeedAileronSymmList = new ArrayList<String>();
	List<String> dfzAllSpeedAileronSymmList = new ArrayList<String>();
	List<String> dmxAllSpeedAileronSymmList = new ArrayList<String>();
	List<String> dmyAllSpeedAileronSymmList = new ArrayList<String>();
	List<String> dmzAllSpeedAileronSymmList = new ArrayList<String>();
	double[] innerElevatorDeflectionAero = null;
	double[] innerElevonDeflectionAero = null;
	double[] middleElevonDeflectionAero = null;
	double[] outerElevonDeflectionAero = null;
	double[] spoilerDeflectionAero = null;
	double[] allSpeedAileronDeflectionAero = null;
	double[] rightRudderDeflectionAero = null;
	double[] reynoldNumber = null;
	int reynoldDimension = 0;
	double[] reynoldsVector = null;
	int reynoldsDimension = 0;
	double[] machNumber = null;
	double[] betaVector = null;
	double[] alphaVector = null;
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

	public void readVariablesFromCPACS(String dirPath) throws TiglException, ParserConfigurationException, IOException {
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
		aircraftConfiguration = jpadXmlReader.getXMLPropertyByPath(
				"/cpacs/toolspecific/UNINA_modules/configuration");
		
		NodeList wingsNodes = _cpacsReader.getWingList();
		if (aircraftConfiguration.equals("Traditional")) {
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
			wingNode = wingsNodes.item(wingIndex);
			wingPosition = CPACSUtils.getWingPosition(wingNode);
			double wingSurface = _cpacsReader.getWingReferenceArea(wingIndex);
			System.out.println("wingSurface: " + wingSurface);
			wingArea = Amount.valueOf(wingSurface,SI.SQUARE_METRE);
			System.out.println("wing area: " + wingArea);
			wspan = _cpacsReader.getWingSpan(wingUID);
			wingSpan = Amount.valueOf(wspan,SI.METRE);
			System.out.println("wing span: " + wingSpan);
			
			Double wrootchrd = CPACSUtils.getWingChord(wingNode);
			wingRootChord = Amount.valueOf(wrootchrd,SI.METRE);
			System.out.println("wing root chord: " + wingRootChord);

			macVectorWing = _cpacsReader.getWingMeanAerodynamicChord(wingUID);
			wingMAC = Amount.valueOf(macVectorWing[0],SI.METER);
			System.out.println("--------------------------------");
			System.out.println("MAC value = " +wingMAC.doubleValue(SI.METER));
			System.out.println("MACx value = " + macVectorWing[1]);
			System.out.println("MACy value = " + macVectorWing[2]);
			System.out.println("MACz value = " + macVectorWing[3]);
			System.out.println("--------------------------------");

			//Horizontal tail
			System.out.println("--------------------------------");
			System.out.println("Start HT");
			System.out.println("--------------------------------");
			horizontalTailNode = wingsNodes.item(horizontalTailIndex);
			Double[] horizontalTailPosition = CPACSUtils.getWingPosition(horizontalTailNode);

			double horizontalSurface = _cpacsReader.getWingReferenceArea(horizontalTailIndex);
			htSurface = Amount.valueOf(horizontalSurface, SI.SQUARE_METRE);
			macVectorHT = _cpacsReader.getWingMeanAerodynamicChord(horizontalTailUID);
			System.out.println("--------------------------------");
			System.out.println("MAC HT value = " + macVectorHT[0]);
			System.out.println("MACx HT value = " + macVectorHT[1]);
			System.out.println("MACy HT value = " + macVectorHT[2]);
			System.out.println("MACz HT value = " + macVectorHT[3]);
			System.out.println("--------------------------------");
			

			//Vertical tail
			System.out.println("--------------------------------");
			System.out.println("Start VT");
			System.out.println("--------------------------------");
			verticalTailNode = wingsNodes.item(verticalTailIndex);
			Double[] verticalTailPosition = CPACSUtils.getWingPosition(verticalTailNode);

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
		}
		
		if (aircraftConfiguration.equals("BWB")) {
			// Main wing
			String wingUID = jpadXmlReader.getXMLPropertyByPath(
					"/cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID");
			int wingIndex = _cpacsReader.getWingIndexZeroBased(wingUID);
			System.out.println("wingUID, idx: " + wingUID + ", " + wingIndex);

			// 1st Vertical tail
			String firstVerticalTailUID = jpadXmlReader.getXMLPropertyByPath(
					"/cpacs/toolspecific/UNINA_modules/input/wings/VerticalTail["+1+"]/HorizTailUID");
			int horizontalTailIndex = _cpacsReader.getWingIndexZeroBased(firstVerticalTailUID);
			System.out.println("1st vtailUID, idx: " + firstVerticalTailUID + ", " + horizontalTailIndex);

			// Vertical tail
			String secondVerticalTailUID = jpadXmlReader.getXMLPropertyByPath(
					"/cpacs/toolspecific/UNINA_modules/input/wings/VerticalTail["+2+"]/HorizTailUID");
			int verticalTailIndex = _cpacsReader.getWingIndexZeroBased(secondVerticalTailUID); 
			System.out.println("2nd vtailUID, idx: " + secondVerticalTailUID + ", " + verticalTailIndex);
			
			//Start wing 
			wingNode = wingsNodes.item(wingIndex);
			wingPosition = CPACSUtils.getWingPosition(wingNode);
			double wingSurface = _cpacsReader.getWingReferenceArea(wingIndex);
			System.out.println("wingSurface: " + wingSurface);
			wingArea = Amount.valueOf(wingSurface,SI.SQUARE_METRE);
			System.out.println("wing area: " + wingArea);
			wspan = _cpacsReader.getWingSpan(wingUID);
			wingSpan = Amount.valueOf(wspan,SI.METRE);
			System.out.println("wing span: " + wingSpan);

			Double wrootchrd = CPACSUtils.getWingChord(wingNode);
			wingRootChord = Amount.valueOf(wrootchrd,SI.METRE);
			System.out.println("wing root chord: " + wingRootChord);

			macVectorWing = _cpacsReader.getWingMeanAerodynamicChord(wingUID);
			wingMAC = Amount.valueOf(macVectorWing[0],SI.METER);
			System.out.println("--------------------------------");
			System.out.println("MAC value = " +wingMAC.doubleValue(SI.METER));
			System.out.println("MACx value = " + macVectorWing[1]);
			System.out.println("MACy value = " + macVectorWing[2]);
			System.out.println("MACz value = " + macVectorWing[3]);
			System.out.println("--------------------------------");

			//Vertical tail
			System.out.println("--------------------------------");
			System.out.println("Start VT");
			System.out.println("--------------------------------");
			verticalTailNode = wingsNodes.item(verticalTailIndex);
			Double[] verticalTailPosition = CPACSUtils.getWingPosition(verticalTailNode);
			double verticalSurface = _cpacsReader.getWingReferenceArea(verticalTailIndex);
			vtSurface = Amount.valueOf(verticalSurface, SI.SQUARE_METRE);
			//		double horizontalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(horizontalTailUID,"x");
			macVectorVT = _cpacsReader.getWingMeanAerodynamicChord(firstVerticalTailUID);
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
		}
		
		
		
		//AERO REFERENCE
		aeroReferenceOrigin[0] = macVectorWing[1] + (0.25)*macVectorWing[0];	
		aeroReferenceOrigin[1] = 0;
		aeroReferenceOrigin[2] =  wingPosition[2];
		//aeroReferenceOrigin = _cpacsReader.getVectorPosition("cpacs/vehicles/aircraft/model/reference/point");

		//Eyepoint
		eyePointPosition = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]");

		//Visual
		double[] visualReferenceCenter = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]"); //TO DO Ask where positioning visual

		//Start Mass Balance -->Mass and Inertia
		/*ixx = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
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
						+ "mOEM/mEM/massDescription/mass"));*/
		ixx = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/massDescription/massInertia/Jxx")));
		iyy = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/massDescription/massInertia/Jyy")));
		izz = (Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/massDescription/massInertia/Jzz")));
		emptyW = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/massDescription/mass"));
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
		String checkMainGearReference = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/landingGear/mainGears/"
						+ "mainGear/wingAttachment/positioning/eta");
		if (checkMainGearReference != null) {
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
		}
		else {
			coordinateRightLandingGear[0] = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
					"cpacs/vehicles/aircraft/model/landingGear"
							+ "/mainGears/mainGear/fuselageAttachment/translation/x"));
			coordinateRightLandingGear[1] = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
					"cpacs/vehicles/aircraft/model/landingGear"
							+ "/mainGears/mainGear/fuselageAttachment/translation/y"));
			coordinateRightLandingGear[2] = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
					"cpacs/vehicles/aircraft/model/landingGear"
							+ "/mainGears/mainGear/fuselageAttachment/translation/z"));
		}
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
				"cpacs/vehicles/engines/engine/analysis");
		engineRight = _cpacsReader.getEngine(engineList.item(0),"turbofan");
		
		//Propulsion performance
		
		NodeList engineNode = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(),
				"cpacs/toolspecific/CIAM_Engine_Deck_ED/output/performanceMap");
				
		DocumentBuilderFactory factoryCIAM = DocumentBuilderFactory.newInstance();
		factoryCIAM.setNamespaceAware(true);
		DocumentBuilder builderCIAM;
		builderCIAM = factoryCIAM.newDocumentBuilder();
		Document docCIAM = builderCIAM.newDocument();
		Node importedNodeCIAM = docCIAM.importNode(engineNode.item(0), true);
		docCIAM.appendChild(importedNodeCIAM);
		NodeList engineNodesList = MyXMLReaderUtils.getXMLNodeListByPath(
				docCIAM, "//flightSegments/flightSegment");
		
		DocumentBuilderFactory factoryData = DocumentBuilderFactory.newInstance();
		factoryData.setNamespaceAware(true);
		DocumentBuilder builderData;
		

		for (int i = 1; i<engineNodesList.getLength();i++) {		
			if(i != 5) {
				builderData = factoryData.newDocumentBuilder();
				Document docData = builderData.newDocument();
				Node dataNode = docData.importNode(engineNodesList.item(i), true);
				docData.appendChild(dataNode);
				listListEngineData.add((ArrayList<String>) _cpacsReader.getEngineDataFromCIAMToolFROMCPACS
						(engineNodesList.item(i),"//thrust/text()", engineRight.get(0)/0.224809, "CT"));
				machEngineList.add(CPACSUtils.doubleVectorToString
						(CPACSReader.getEngineMachNumberFromCPACSToolCiam
								(docData,CPACSReader.getEngineThrustCommandFromCPACSCIAMTool(docData).length)));
				
				throttleEngineList.add(CPACSUtils.doubleVectorToString
						(CPACSReader.getEngineThrustCommandFromCPACSCIAMTool(docData)));
				listFlightLevel.add(_cpacsReader.getEngineFlightLevelCommandFromCPACSCIAMTool(engineNodesList.item(i)));
			}
		}/*
		 //Idle last position
		listListEngineData.add((ArrayList<String>) _cpacsReader.getEngineDataFromCIAMToolFROMCPACS
				(engineNodesList.item(5),"//thrust/text()", engineRight.get(0)/0.224809, "CT"));	
		listFlightLevel.add(_cpacsReader.getEngineFlightLevelCommandFromCPACSCIAMTool(engineNodesList.item(5)));*/
		enginePerformanceIdle = _cpacsReader.getEngineDataFromCIAMToolFromCPACSWhitoutThrustCommad
		(engineNodesList.item(5),"//thrust/text()", engineRight.get(0)/0.224809, "CT");	 
		flightLevelVectorIdle = _cpacsReader.
				getEngineFlightLevelCommandFromCPACSCIAMToolVector(engineNodesList.item(5));
		
		DocumentBuilderFactory factoryDataIdle = DocumentBuilderFactory.newInstance();
		factoryData.setNamespaceAware(true);
		DocumentBuilder builderDataIdle;
		builderDataIdle = factoryDataIdle.newDocumentBuilder();
		Document docDataIdle = builderDataIdle.newDocument();
		Node dataNodeIdle = docDataIdle.importNode(engineNodesList.item(5), true);
		docDataIdle.appendChild(dataNodeIdle);
		machEngineIdle = CPACSReader.getEngineMachNumberFromCPACSTool(docDataIdle);
		commandEngineIdle = CPACSReader.getEngineThrustCommandFromCPACSCIAMTool(docDataIdle);
		//Military thrust 

		enginePerformanceMilitaryThrust = _cpacsReader.getEngineDataFromCIAMToolFromCPACSWhitoutThrustCommad
				(engineNodesList.item(4),"//thrust/text()", engineRight.get(0)/0.224809, "CT");	 //Conversione N->lb
		flightLevelVectorMil = _cpacsReader.
				getEngineFlightLevelCommandFromCPACSCIAMToolVector(engineNodesList.item(4));
		
		DocumentBuilderFactory factoryDataMilitary = DocumentBuilderFactory.newInstance();
		factoryDataMilitary.setNamespaceAware(true);
		DocumentBuilder builderDataMilitary;
		builderDataMilitary = factoryData.newDocumentBuilder();
		Document docDataMilitary = builderDataMilitary.newDocument();
		Node dataNodeMilitary = docDataMilitary.importNode(engineNodesList.item(4), true);
		docDataMilitary.appendChild(dataNodeMilitary);
		
		machEngineMilitary = CPACSReader.getEngineMachNumberFromCPACSTool(docDataIdle);
		commandEngineMilitary = CPACSReader.getEngineThrustCommandFromCPACSCIAMTool(docDataIdle);
		//Gravity Center
		cgPosition = _cpacsReader.getGravityCenterPosition(leftEnginePosition,coordinateNoseLandingGear,coordinateRightLandingGear);
		System.out.println("CG_X = " + cgPosition[0]);
		System.out.println("CG_Y = " + cgPosition[1]);
		System.out.println("CG_Z = " + cgPosition[2]);
		
		//ht Arm
		double horizontalTailAeroCenter = 
				macVectorHT[1] + (0.25)*macVectorHT[0];	
		System.out.println("--------------------------------");
		System.out.println("horizontalTailAeroCenter =  " + horizontalTailAeroCenter);
		System.out.println("--------------------------------");
		double horizontalTailArm = horizontalTailAeroCenter-cgPosition[0];
		hTArm = Amount.valueOf(horizontalTailArm, SI.METER);
		//VTArm 
		double verticalTailAeroCenter = 
				macVectorVT[1] + (0.25)*macVectorVT[0];	
		System.out.println("--------------------------------");
		System.out.println("verticalTailAeroCenter =  " + verticalTailAeroCenter);
		System.out.println("--------------------------------");
		double verticalTailArm = verticalTailAeroCenter-cgPosition[0];
		vtArm =  Amount.valueOf(verticalTailArm, SI.METER);
		
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
		if (tankNumberElement.getLength()>0) {
			tankMatrix = _cpacsReader.getVectorPositionNodeTank(
					tankPositionList.item(1),cgPosition,emptyW);
		}
		
		else {
			
			double massFuel = Double.parseDouble(jpadXmlReader.getXMLPropertyByPath(
					"cpacs/vehicles/aircraft/model/analyses/weightAndBalance/"
					+ "operationalCases/operationalCase/mFuel/mass"));
			
			tankVector = _cpacsReader.getVectorPositionNodeTankApproximated(wingPosition, massFuel, wspan);
			flagTank = 1;
			
		}
		//Flight Control
		
		
		//Aeromap
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
		reynoldsVector = CPACSReader.getReynoldsNumberFromAeroPerformanceMap(docAero);
		reynoldsDimension = reynoldsVector.length;
		machNumber = CPACSReader.getMachNumberFromAeroPerformanceMap(docAero);
		betaVector = CPACSReader.getYawFromAeroPerformanceMap(docAero);
		alphaVector = CPACSReader.getAlphaFromAeroPerformanceMap(docAero);
		machDimension = machNumber.length;
		String cfxPath = "//cfx/text()";
		String cfyPath = "//cfy/text()";
		String cfzPath = "//cfz/text()";
		String cmxPath = "//cmx/text()";
		String cmyPath = "//cmy/text()";
		String cmzPath = "//cmz/text()";
		//Moments and force coefficient in body axis
	//Control surface aeroData 
		if (aircraftConfiguration.equals("Traditional")) {
			
			
			int controlSurfacePositionAeroPerformaceMapVector[] = new int [controlSurfaceAeroPerformanceList.getLength()];
			controlSurfacePositionAeroPerformaceMapVector = _cpacsReader.defineControlSurfacePositionAeroPerformanceMap(controlSurfaceAeroPerformanceList,docAero);

			if (betaVector.length>1) {
				cfxList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
						aeroNodeList.item(0), cfxPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
				cfyList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
						aeroNodeList.item(0), cfyPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
				cfzList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
						aeroNodeList.item(0), cfzPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
				cmxList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
						aeroNodeList.item(0), cmxPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
				cmyList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
						aeroNodeList.item(0), cmyPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
				cmzList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
						aeroNodeList.item(0),  cmzPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
				
			aileronDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]));
			cfxListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcfx/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcfy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcfz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmxAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcmy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcmz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//Elevator
			elevatorDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]));
			cfxListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcfx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			cfyListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcfy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcfz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER) ,wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmyElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcmy/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcmz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			//Rudder
			rudderDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap
					(controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]));
			cfxListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcfx/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			cfyListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcfy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcfz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmyRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcmy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcmz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//Flap
			flapDeflectionAeroInner = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]));

			//Inner
			cfxListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcfx/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			cfyListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcfy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcfz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmyFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcmy/text()",
					aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcmz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//Outer
			flapDeflectionAeroOuter = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]));
			cfxListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcfx/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			cfyListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcfy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcfz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmyFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcmy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcmz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
		
			//p-rate
			
			//		dcfxListp = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//		aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dcfyListp = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//dcfzListp = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxpList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
//			dmypList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
//					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzpList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//q-rate
				dcfxListq = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdqstar/text()", 1,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			//dcfyListq = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//		aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dcfzListq = _cpacsReader.getCoefficientFromAeroPerformanceMap(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			//dmxqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			//dmzqList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//r-rate
			//dcfxListr = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dcfyListr = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//dcfzListr = _cpacsReader.getCoefficientFromAeroPerformanceMap(
			//		aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//dmyrList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzrList = _cpacsReader.getCoefficientFromAeroPerformanceMap(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);

		}
		
		else if(betaVector.length==1) {
			
			cfxList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle
					(aeroNodeList.item(0), cfxPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle
					(aeroNodeList.item(0), cfyPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle
					(aeroNodeList.item(0), cfzPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cmxList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle
					(aeroNodeList.item(0), cmxPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cmyList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle
					(aeroNodeList.item(0), cmyPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cmzList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle
					(aeroNodeList.item(0),  cmzPath, 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			aileronDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]));
			cfxListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcfx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcfy/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListAileron = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcfz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcmy/text()", aeroNodeList.item(0), 
					1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[0]), "//dcmz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//Elevator
			elevatorDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]));
			cfxListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcfx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcfy/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListElevator = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcfz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER) ,wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcmx/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcmy/text()", aeroNodeList.item(0),
					1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[1]), "//dcmz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			//Rudder
			rudderDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap
					(controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]));
			cfxListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcfx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcfy/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListRudder = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcfz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcmy/text()", aeroNodeList.item(0), 
					1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzRudderList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[2]), "//dcmz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//Flap
			flapDeflectionAeroInner = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]));

			//Inner
			cfxListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcfx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcfy/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListFlapInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcfz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcmx/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcmy/text()", aeroNodeList.item(0),
					1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzFlapListInner = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[3]), "//dcmz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//Outer
			flapDeflectionAeroOuter = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]));
			cfxListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcfx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			cfyListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcfy/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			cfzListFlapOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcfz/text()", aeroNodeList.item(0), 
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcmx/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcmy/text()", aeroNodeList.item(0),
					1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzFlapListOuter = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurfaceWithoutSideAngle(
					controlSurfaceAeroPerformanceList.item(controlSurfacePositionAeroPerformaceMapVector[4]), "//dcmz/text()", aeroNodeList.item(0),
					0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//Damping derivative
			//p-rate
			
			//		dcfxListp = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//		aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dcfyListp = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//dcfzListp = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxpList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
//			dmypList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
//					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzpList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdpstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//q-rate
				dcfxListq = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdqstar/text()", 1,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			//dcfyListq = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//		aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dcfzListq = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
				aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			//dmxqList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyqList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			//dmzqList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdqstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 1);
			//r-rate
			//dcfxListr = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfxdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dcfyListr = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfydrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//dcfzListr = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//		aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcfzdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxrList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmxdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 0);
			//dmyrList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
			//	aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmydrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzrList = _cpacsReader.getCoefficientFromAeroPerformanceMapWithoutSideSlipAngle(
					aeroNodeList.item(0), "//dampingDerivatives/positiveRates/dcmzdrstar/text()", 0,wingMAC.doubleValue(SI.METER),wingSpan.doubleValue(SI.METER), 0, 1, 1);

		}
		}
		if (aircraftConfiguration.equals("BWB")) {
			innerElevatorDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(0));
			dfxInnerElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(0), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyInnerElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(0), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dfzInnerElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(0), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 1);
			dmxInnerElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(0), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyInnerElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(0), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzInnerElevatorList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(0), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
		
			innerElevonDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(1));
			dfxInnerElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(1), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzInnerElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(1), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyInnerElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(1), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxInnerElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(1), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyInnerElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(1), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzInnerElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(1), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			middleElevonDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(2));
			dfxMiddleElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(2), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzMiddleElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(2), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyMiddleElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(2), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxMiddleElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(2), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyMiddleElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(2), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzMiddleElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(2), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			outerElevonDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(3));
			dfxOuterElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(3), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzOuterElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(3), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyOuterElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(3), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxOuterElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(3), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyOuterElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(3), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzOuterElevonList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(3), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			allSpeedAileronDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(4));
			dfxAllSpeedAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(4), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzAllSpeedAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(4), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyAllSpeedAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(4), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxAllSpeedAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(4), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyAllSpeedAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(4), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzAllSpeedAileronList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(4), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			innerElevatorDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(5));
			dfxInnerElevatorSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(5), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzInnerElevatorSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(5), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyInnerElevatorSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(5), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxInnerElevatorSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(5), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyInnerElevatorSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(5), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzInnerElevatorSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(5), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			dfxInnerElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(6), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzInnerElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(6), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyInnerElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(6), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxInnerElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(6), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyInnerElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(6), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzInnerElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(6), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			dfxMiddleElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(7), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzMiddleElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(7), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyMiddleElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(7), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxMiddleElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(7), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyMiddleElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(7), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzMiddleElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(7), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
		
			dfxOuterElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(8), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzOuterElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(8), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyOuterElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(8), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxOuterElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(8), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyOuterElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(8), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzOuterElevonSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(8), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			
			dfxAllSpeedAileronSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(9), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzAllSpeedAileronSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(9), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyAllSpeedAileronSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(9), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxAllSpeedAileronSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(9), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyAllSpeedAileronSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(9), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzAllSpeedAileronSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(9), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			rightRudderDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(10));
			dfxVTRudder1List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(10), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzVTRudder1List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(10), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyVTRudder1List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(10), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxVTRudder1List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(10), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyVTRudder1List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(10), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzVTRudder1List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(10), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			dfxVTRudder2List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(11), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzVTRudder2List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(11), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfyVTRudder2List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(11), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxVTRudder2List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(11), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmyVTRudder2List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(11), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzVTRudder2List = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(11), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

			spoilerDeflectionAero = CPACSReader.getControlSurfaceDeflectionFromAeroPerformanceMap(
					controlSurfaceAeroPerformanceList.item(12));
			dfxSpoilerList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(12), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzSpoilerList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(12), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfySpoilerList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(12), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxSpoilerList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(12), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmySpoilerList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(12), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzSpoilerList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(12), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
		

			dfxSpoilerSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(13), "//dcfx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfzSpoilerSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(13), "//dcfz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dfySpoilerSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(13), "//dcfy/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);
			dmxSpoilerSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(13), "//dcmx/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 0);
			dmySpoilerSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(13), "//dcmy/text()", aeroNodeList.item(0), 1,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 0, 0);
			dmzSpoilerSymmList = _cpacsReader.getCoefficientFromAeroPerformanceMapControlSurface(
					controlSurfaceAeroPerformanceList.item(13), "//dcmz/text()", aeroNodeList.item(0), 0,wingMAC.doubleValue(SI.METER), wingSpan.doubleValue(SI.METER), 0, 1, 1);

		}

		//Control surface system
		if (aircraftConfiguration.equals("Traditional")) {
			_cpacsReader.getControlSurfacePilotCommandIndexAndValue(wingNode, controlSurfaceList, controlSurfaceInt);
			_cpacsReader.getControlSurfacePilotCommandIndexAndValue(horizontalTailNode, controlSurfaceList, controlSurfaceInt);
			_cpacsReader.getControlSurfacePilotCommandIndexAndValue(verticalTailNode, controlSurfaceList, controlSurfaceInt);
		
			controlSurfaceVectorIndex[0] = CPACSReader.getControlSurfaceIndex(wingNode,"aileronUID");
			controlSurfaceVectorIndex[1] = CPACSReader.getControlSurfaceIndex(wingNode,"innerFlapUID");
			controlSurfaceVectorIndex[2] = CPACSReader.getControlSurfaceIndex(wingNode,"outerFlap1UID");
			controlSurfaceVectorIndex[3] = 3+CPACSReader.getControlSurfaceIndex(horizontalTailNode,"elevatorUID"); // 3 because there are 3 control surface in the wing
			controlSurfaceVectorIndex[4] = controlSurfaceVectorIndex[3]+CPACSReader.getControlSurfaceIndex(verticalTailNode,"rudderUID");// before there are wing and horizontal tail's control surface

		}
		
		if (aircraftConfiguration.equals("BWB")) {
			_cpacsReader.getControlSurfacePilotCommandIndexAndValue(wingNode, controlSurfaceList, controlSurfaceInt);
			_cpacsReader.getControlSurfacePilotCommandIndexAndValue(verticalTailNode, controlSurfaceList, controlSurfaceInt);
			_cpacsReader.getControlSurfacePilotCommandIndexAndValue(verticalTailNode, controlSurfaceList, controlSurfaceInt);

		}

		
		//Check File for Matlab
		try {
			//Clean
			JSBSimUtils.createCheckFileTXT(dirPath+"/Matlab/Data", cfxList, machNumber, reynoldsVector,alphaVector, betaVector, "Cf_x");
			JSBSimUtils.createCheckFileTXT(dirPath+"/Matlab/Data", cfyList, machNumber, reynoldsVector,alphaVector, betaVector, "Cf_y");
			JSBSimUtils.createCheckFileTXT(dirPath+"/Matlab/Data", cfzList, machNumber, reynoldsVector,alphaVector, betaVector, "Cf_z");
			JSBSimUtils.createCheckFileTXT(dirPath+"/Matlab/Data", cmxList, machNumber, reynoldsVector,alphaVector, betaVector, "Cm_x");
			JSBSimUtils.createCheckFileTXT(dirPath+"/Matlab/Data", cmyList, machNumber, reynoldsVector,alphaVector, betaVector, "Cm_y");
			JSBSimUtils.createCheckFileTXT(dirPath+"/Matlab/Data", cmzList, machNumber, reynoldsVector,alphaVector, betaVector, "Cm_z");
			//Aileron
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfxListAileron, machNumber, reynoldsVector,alphaVector, betaVector, aileronDeflectionAero,	 "Cf_x_Aileron");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfzListAileron, machNumber, reynoldsVector,alphaVector, betaVector, aileronDeflectionAero, "Cf_z_Aileron");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfyListAileron, machNumber, reynoldsVector,alphaVector, betaVector, aileronDeflectionAero, "Cf_y_Aileron");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmxAileronList, machNumber, reynoldsVector,alphaVector, betaVector, aileronDeflectionAero, "Cm_x_Aileron");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmyAileronList, machNumber, reynoldsVector,alphaVector, betaVector, aileronDeflectionAero, "Cm_y_Aileron");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmzAileronList, machNumber, reynoldsVector,alphaVector, betaVector, aileronDeflectionAero, "Cm_z_Aileron");
			//Rudder
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfxListRudder, machNumber, reynoldsVector,alphaVector, betaVector, rudderDeflectionAero,"Cf_x_Rudder");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfzListRudder, machNumber, reynoldsVector,alphaVector, betaVector, rudderDeflectionAero, "Cf_z_Rudder");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfyListRudder, machNumber, reynoldsVector,alphaVector, betaVector, rudderDeflectionAero, "Cf_y_Rudder");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmxRudderList, machNumber, reynoldsVector,alphaVector, betaVector, rudderDeflectionAero, "Cm_x_Rudder");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmyRudderList, machNumber, reynoldsVector,alphaVector, betaVector, rudderDeflectionAero, "Cm_y_Rudder");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmzRudderList, machNumber, reynoldsVector,alphaVector, betaVector, rudderDeflectionAero, "Cm_z_Rudder");
			//InnerFlap

			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfxListFlapInner, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroInner,"Cf_x_InnerFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfzListFlapInner, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroInner, "Cf_z_InnerFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfyListFlapInner, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroInner, "Cf_y_InnerFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmxFlapListInner, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroInner, "Cm_x_InnerFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmyFlapListInner, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroInner, "Cm_y_InnerFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmzFlapListInner, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroInner, "Cm_z_InnerFlap");

			//OuterFlap
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfxListFlapOuter, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroOuter,	 "Cf_x_OuterFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfzListFlapOuter, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroOuter, "Cf_z_OuterFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfyListFlapOuter, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroOuter, "Cf_y_OuterFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmxFlapListOuter, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroOuter, "Cm_x_OuterFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmyFlapListOuter, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroOuter, "Cm_y_OuterFlap");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmzFlapListOuter, machNumber, reynoldsVector,alphaVector, betaVector, flapDeflectionAeroOuter, "Cm_z_OuterFlap");
			//elevator
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfxListElevator, machNumber, reynoldsVector,alphaVector, betaVector, elevatorDeflectionAero, "Cf_x_Elevator");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfzListElevator, machNumber, reynoldsVector,alphaVector, betaVector, elevatorDeflectionAero, "Cf_z_Elevator");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", cfyListElevator, machNumber, reynoldsVector,alphaVector, betaVector, elevatorDeflectionAero, "Cf_y_Elevator");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmxElevatorList, machNumber, reynoldsVector,alphaVector, betaVector, elevatorDeflectionAero, "Cm_x_Elevator");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmyElevatorList, machNumber, reynoldsVector,alphaVector, betaVector, elevatorDeflectionAero, "Cm_y_Elevator");
			JSBSimUtils.createCheckFileControlSurfaceTXT(dirPath+"/Matlab/Data", dmzElevatorList, machNumber, reynoldsVector,alphaVector, betaVector, elevatorDeflectionAero, "Cm_z_Elevator");

			
			// Engine
			JSBSimUtils.createCheckFileTXTEngineIdleMilitary
			(dirPath+"/Matlab/Data", enginePerformanceIdle, machEngineIdle, flightLevelVectorIdle, "Idle");
			JSBSimUtils.createCheckFileTXTEngineIdleMilitary
			(dirPath+"/Matlab/Data", enginePerformanceMilitaryThrust, machEngineMilitary, flightLevelVectorMil, "Mil");
/*			int i = 1;
				JSBSimUtils.createCheckFileTXTEngineCIAM
				(dirPath+"/Matlab/Data", listListEngineData.get(i), machEngineList.get(i),
						listFlightLevel.get(i), throttleEngineList.get(i), "condition "+ Integer.toString(i));*/
			
			
			
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			if (aircraftConfiguration.equals("Traditional")) {
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "htailarea",
							htSurface.doubleValue(SI.SQUARE_METRE), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M2")
							)
					);
			}
			if (aircraftConfiguration.equals("Traditional")) {
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "htailarm",
							hTArm.doubleValue(SI.METER), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M")
							)
					);
			}
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "vtailarea",
							vtSurface.doubleValue(SI.SQUARE_METRE), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M2")
							)
					);
			metricsElement.appendChild(
					JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
							doc, "vtailarm",
							vtArm.doubleValue(SI.METER), 3, 6, // value, rounding-above, rounding-below
							Tuple.of("unit", "M")
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
			JSBSimUtils.createControlSurfaceSystemJSBSim( controlSurfaceList, doc, 
					controlSurfaceInt, "elevator", "pitch", controlSurfaceVectorIndex[3], "Pitch"));
//			org.w3c.dom.Element yawControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Yaw")
//					);
//			flightControlElement.appendChild(yawControlElement);
			flightControlElement.appendChild(
					JSBSimUtils.createControlSurfaceSystemJSBSim(
							controlSurfaceList, doc,controlSurfaceInt, "rudder", "yaw", controlSurfaceVectorIndex[4], "Yaw")
					);
//			org.w3c.dom.Element RollControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Roll")
//					);
//			flightControlElement.appendChild(RollControlElement);
			flightControlElement.appendChild(
					JSBSimUtils.createAlileronElement(
							controlSurfaceList, doc, controlSurfaceInt, "aileron", "roll", controlSurfaceVectorIndex[0], "Roll")
					);
//			org.w3c.dom.Element FlapControlElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
//					doc,"channel",
//					Tuple.of("name", "Flaps")
//					);
//			flightControlElement.appendChild(FlapControlElement);
			System.out.println("----------------------------------------------");
			flightControlElement.appendChild(
					JSBSimUtils.createFlapElement(
							controlSurfaceList, doc, controlSurfaceInt, "flap", 2, "Flap")
					);
			System.out.println("----------------------------------------------");

			
			rootElement.appendChild(flightControlElement);
 
			
			// Propulsion
						String engineName = aircraftName.replaceAll("\\s+","_") + "_engine";
						
						try {
							//JSBSimUtils.createEngineXML( engineRight, engineName, "turbine", 
							//		dirPath+"/engine","JET",listListEngineData, listFlightLevel);
							JSBSimUtils.createSimplifyEngineXML( engineRight, engineName, "turbine",
									dirPath+"/engine","JET",enginePerformanceIdle, enginePerformanceMilitaryThrust);
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
						if(flagTank == 0) {
						rootElement.appendChild(
								JSBSimUtils.createTankElement( tankMatrix, doc, "RIGHT", propulsionElement)
								);
						rootElement.appendChild(
								JSBSimUtils.createTankElement( tankMatrix, doc, "LEFT", propulsionElement)
								);
						}
						else {
							rootElement.appendChild(
									JSBSimUtils.createTankElementApproximated( tankVector, doc, propulsionElement)
									);
						} 
			
			
			//aerodynamics
			org.w3c.dom.Element aeroElement = doc.createElement("aerodynamics");
			rootElement.appendChild(aeroElement);


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
			org.w3c.dom.Element axisElementX = doc.createElement("axis");
			axisElementX.setAttribute("name", "X");
//			aeroElement.appendChild(axisElementDrag);
			
			org.w3c.dom.Element axisElementY = doc.createElement("axis");
			axisElementY.setAttribute("name", "Y");
//			aeroElement.appendChild(axisElementSide);
			
			org.w3c.dom.Element axisElementZ = doc.createElement("axis");
			axisElementZ.setAttribute("name", "Z");
			aeroElement.appendChild(axisElementZ);
			
			org.w3c.dom.Element axisElementRoll = doc.createElement("axis");
			axisElementRoll.setAttribute("name", "ROLL");
			aeroElement.appendChild(axisElementRoll);
			
			org.w3c.dom.Element axisElementPitch = doc.createElement("axis");
			axisElementPitch.setAttribute("name", "PITCH");
			aeroElement.appendChild(axisElementPitch);
			
			org.w3c.dom.Element axisElementYaw = doc.createElement("axis");
			axisElementYaw.setAttribute("name", "YAW");
			aeroElement.appendChild(axisElementYaw);
			// Start external function
			if(betaVector.length>1) {
			System.out.println("Number = " + cfxList.size());
			rootElement.appendChild(
					JSBSimUtils.createAeroDataExternalFunctionElement(
							doc, cfxList, machDimension, machNumber, reynoldsDimension, reynoldsVector, "x", aeroElement));
			rootElement.appendChild(
					JSBSimUtils.createAeroDataExternalFunctionElement(
							doc, cfyList, machDimension, machNumber, reynoldsDimension, reynoldsVector, "y", aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElement(
					doc, cfzList, machDimension, machNumber, reynoldsDimension, reynoldsVector, "z", aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElement(
					doc, cmxList, machDimension, machNumber, reynoldsDimension, reynoldsVector, "roll", aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElement(
					doc, cmyList, machDimension, machNumber, reynoldsDimension, reynoldsVector, "pitch", aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElement(
					doc, cmzList, machDimension, machNumber, reynoldsDimension, reynoldsVector, "yaw", aeroElement));
			//Flap Inner
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfxListFlapInner, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
					"x", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfyListFlapInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"y", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfzListFlapInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"z", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmxFlapListInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"roll", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmyFlapListInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"pitch", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmzFlapListInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"yaw", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
			
			//Flap Outer

			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfxListFlapOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
					"x", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfyListFlapOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"y", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfzListFlapOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"z", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmxFlapListOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"roll", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmyFlapListOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"pitch", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmzFlapListOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"yaw", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
			//Aileron
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfxListAileron, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
					"x", aileronDeflectionAero, "aileron", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfyListAileron, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"y", aileronDeflectionAero, "aileron", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfzListAileron, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"z", aileronDeflectionAero, "aileron", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmxAileronList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"roll", aileronDeflectionAero, "aileron", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmyAileronList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"pitch", aileronDeflectionAero, "aileron", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmzAileronList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"yaw", aileronDeflectionAero, "aileron", betaVector, aeroElement));

			//Elevator

			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfxListElevator, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
					"x", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfyListElevator, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"y", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfzListElevator, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"z", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmxElevatorList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"roll", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmyElevatorList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"pitch", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmzElevatorList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"yaw", elevatorDeflectionAero, "elevator", betaVector, aeroElement));

			//Rudder

			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfxListRudder, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
					"x", rudderDeflectionAero, "rudder", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfyListRudder, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"y", rudderDeflectionAero, "rudder", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, cfzListRudder, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"z", rudderDeflectionAero, "rudder", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmxRudderList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"roll", rudderDeflectionAero, "rudder", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmyRudderList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"pitch", rudderDeflectionAero, "rudder", betaVector, aeroElement));
			rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElement(
					doc, dmzRudderList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
					"yaw", rudderDeflectionAero, "rudder", betaVector, aeroElement));
			//Damping derivative
			//p-rate
			if (dcfxListp.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfxListp, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "p-x", aeroElement));
			}
			if (dcfyListp.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfyListp, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "p-y", aeroElement));
			}
			if (dcfzListp.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfzListp, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "p-z", aeroElement));
			}
			if (dmxpList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmxpList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "p-roll", aeroElement));
			}
			if (dmypList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmypList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "p-pitch", aeroElement));
			}
			if (dmzpList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmzpList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "p-yaw", aeroElement));
			}
			// q-rate
			if (dcfxListq.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfxListq, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "q-x", aeroElement));
			}
			if (dcfyListq.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfyListq, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "q-y", aeroElement));
			}
			if (dcfzListq.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfzListq, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "q-z", aeroElement));
			}
			if (dmxqList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmxqList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "q-roll", aeroElement));
			}
			if (dmyqList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmyqList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "q-pitch", aeroElement));
			}
			if (dmzqList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmzqList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "q-yaw", aeroElement));
			}

			//r-rate
			if (dcfxListr.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfxListr, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "r-x", aeroElement));
			}
			if (dcfyListr.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfyListr, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "r-y", aeroElement));
			}
			if (dcfzListr.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dcfzListr, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "r-z", aeroElement));
			}
			if (dmxrList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmxrList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "r-roll", aeroElement));
			}
			if (dmyrList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmyrList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "r-pitch", aeroElement));
			}
			if (dmzrList.size()>0) {
				rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElement(
						doc, dmzrList, machDimension, machNumber, reynoldsDimension,
						reynoldsVector, "r-yaw", aeroElement));
			}
			}
			
			else if(betaVector.length==1) {
				
				rootElement.appendChild(
						JSBSimUtils.createAeroDataExternalFunctionElementWithoutSideAngle(
								doc, cfxList, machDimension, machNumber, "x", aeroElement));
				rootElement.appendChild(
						JSBSimUtils.createAeroDataExternalFunctionElementWithoutSideAngle(
								doc, cfyList, machDimension, machNumber, "y", aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElementWithoutSideAngle(
						doc, cfzList, machDimension, machNumber, "z", aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElementWithoutSideAngle(
						doc, cmxList, machDimension, machNumber, "roll", aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElementWithoutSideAngle(
						doc, cmyList, machDimension, machNumber, "pitch", aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionElementWithoutSideAngle(
						doc, cmzList, machDimension, machNumber, "yaw", aeroElement));
				//Flap Inner
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfxListFlapInner, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
						"x", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfyListFlapInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"y", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfzListFlapInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"z", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmxFlapListInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"roll", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmyFlapListInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"pitch", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmzFlapListInner, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"yaw", flapDeflectionAeroInner, "flap_inner", betaVector, aeroElement));
				
				//Flap Outer

				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfxListFlapOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
						"x", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfyListFlapOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"y", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfzListFlapOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"z", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmxFlapListOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"roll", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmyFlapListOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"pitch", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmzFlapListOuter, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"yaw", flapDeflectionAeroInner, "flap_outer", betaVector, aeroElement));
				//Aileron
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfxListAileron, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
						"x", aileronDeflectionAero, "aileron", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfyListAileron, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"y", aileronDeflectionAero, "aileron", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfzListAileron, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"z", aileronDeflectionAero, "aileron", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmxAileronList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"roll", aileronDeflectionAero, "aileron", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmyAileronList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"pitch", aileronDeflectionAero, "aileron", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmzAileronList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"yaw", aileronDeflectionAero, "aileron", betaVector, aeroElement));

				//Elevator

				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfxListElevator, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
						"x", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfyListElevator, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"y", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfzListElevator, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"z", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmxElevatorList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"roll", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmyElevatorList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"pitch", elevatorDeflectionAero, "elevator", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmzElevatorList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"yaw", elevatorDeflectionAero, "elevator", betaVector, aeroElement));

				//Rudder

				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfxListRudder, machDimension, machNumber, reynoldsDimension, reynoldsVector, 
						"x", rudderDeflectionAero, "rudder", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfyListRudder, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"y", rudderDeflectionAero, "rudder", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, cfzListRudder, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"z", rudderDeflectionAero, "rudder", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmxRudderList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"roll", rudderDeflectionAero, "rudder", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmyRudderList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"pitch", rudderDeflectionAero, "rudder", betaVector, aeroElement));
				rootElement.appendChild(JSBSimUtils.createAeroDataExternalFunctionControlSurfaceElementSideAngle(
						doc, dmzRudderList, machDimension, machNumber, reynoldsDimension, reynoldsVector,
						"yaw", rudderDeflectionAero, "rudder", betaVector, aeroElement));
				//Damping derivative
				//p-rate
				if (dcfxListp.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfxListp, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "p-x", aeroElement));
				}
				if (dcfyListp.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfyListp, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "p-y", aeroElement));
				}
				if (dcfzListp.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfzListp, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "p-z", aeroElement));
				}
				if (dmxpList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmxpList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "p-roll", aeroElement));
				}
				if (dmypList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmypList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "p-pitch", aeroElement));
				}
				if (dmzpList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmzpList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "p-yaw", aeroElement));
				}
				// q-rate
				if (dcfxListq.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfxListq, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "q-x", aeroElement));
				}
				if (dcfyListq.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfyListq, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "q-y", aeroElement));
				}
				if (dcfzListq.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfzListq, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "q-z", aeroElement));
				}
				if (dmxqList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmxqList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "q-roll", aeroElement));
				}
				if (dmyqList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmyqList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "q-pitch", aeroElement));
				}
				if (dmzqList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmzqList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "q-yaw", aeroElement));
				}

				//r-rate
				if (dcfxListr.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfxListr, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "r-x", aeroElement));
				}
				if (dcfyListr.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfyListr, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "r-y", aeroElement));
				}
				if (dcfzListr.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dcfzListr, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "r-z", aeroElement));
				}
				if (dmxrList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmxrList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "r-roll", aeroElement));
				}
				if (dmyrList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmyrList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "r-pitch", aeroElement));
				}
				if (dmzrList.size()>0) {
					rootElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesExternalFunctionElementWithoutSideAngle(
							doc, dmzrList, machDimension, machNumber, reynoldsDimension,
							reynoldsVector, "r-yaw", aeroElement));
				}
				
			}
			//Function in axis Element
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
							doc, outputElement, cfxList, machDimension, machNumber, "x", axisElementX, aeroElement));
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
							doc, outputElement, cfyList, machDimension, machNumber, "y", axisElementY, aeroElement));
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cfzList, machDimension, machNumber, "z", axisElementZ, aeroElement));
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cmxList, machDimension, machNumber, "roll", axisElementRoll, aeroElement));
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cmyList, machDimension, machNumber, "pitch", axisElementPitch, aeroElement));
			aeroElement.appendChild(
					JSBSimUtils.createAeroDataBodyAxisElement(
					doc, outputElement, cmzList, machDimension, machNumber, "yaw", axisElementYaw, aeroElement));
						//Flap Inner
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfxListFlapInner, machDimension, machNumber, "x", axisElementX,
					flapDeflectionAeroInner, "flap_inner", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfyListFlapInner, machDimension, machNumber, "y", axisElementY,
					flapDeflectionAeroInner, "flap_inner", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfzListFlapInner, machDimension, machNumber, "z", axisElementZ,
					flapDeflectionAeroInner, "flap_inner", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxFlapListInner, machDimension, machNumber, "roll", axisElementRoll,
					flapDeflectionAeroInner, "flap_inner", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyFlapListInner, machDimension, machNumber, "pitch", axisElementPitch,
					flapDeflectionAeroInner, "flap_inner", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzFlapListInner, machDimension, machNumber, "yaw", axisElementYaw,
					flapDeflectionAeroInner, "flap_inner", outputElement, aeroElement));
			
			//Flap Outer

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfxListFlapOuter, machDimension, machNumber, "x", axisElementX,
					flapDeflectionAeroInner, "flap_outer", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfyListFlapOuter, machDimension, machNumber, "y", axisElementY,
					flapDeflectionAeroInner, "flap_outer", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfzListFlapOuter, machDimension, machNumber, "z", axisElementZ,
					flapDeflectionAeroInner, "flap_outer", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxFlapListOuter, machDimension, machNumber, "roll", axisElementRoll,
					flapDeflectionAeroInner, "flap_outer", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyFlapListOuter, machDimension, machNumber, "pitch", axisElementPitch,
					flapDeflectionAeroInner, "flap_outer", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzFlapListOuter, machDimension, machNumber, "yaw", axisElementYaw,
					flapDeflectionAeroInner, "flap_outer", outputElement, aeroElement));
			//Aileron
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfxListAileron, machDimension, machNumber, "x", axisElementX, 
					aileronDeflectionAero, "aileron", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfyListAileron, machDimension, machNumber, "y", axisElementY,
					aileronDeflectionAero, "aileron", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfzListAileron, machDimension, machNumber, "z", axisElementZ,
					aileronDeflectionAero, "aileron", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxAileronList, machDimension, machNumber, "roll", axisElementRoll,
					aileronDeflectionAero, "aileron", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyAileronList, machDimension, machNumber, "pitch", axisElementPitch,
					aileronDeflectionAero, "aileron", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzAileronList, machDimension, machNumber, "yaw", axisElementYaw,
					aileronDeflectionAero, "aileron", outputElement, aeroElement));

			//Elevator

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfxListElevator, machDimension, machNumber, "x", axisElementX,
					elevatorDeflectionAero, "elevator", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfyListElevator, machDimension, machNumber, "y", axisElementY, 
					elevatorDeflectionAero, "elevator", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfzListElevator, machDimension, machNumber, "z", axisElementZ,
					elevatorDeflectionAero, "elevator", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxElevatorList, machDimension, machNumber, "roll", axisElementRoll,
					elevatorDeflectionAero, "elevator", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyElevatorList, machDimension, machNumber, "pitch", axisElementPitch,
					elevatorDeflectionAero, "elevator", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzElevatorList, machDimension, machNumber, "yaw", axisElementYaw,
					elevatorDeflectionAero, "elevator", outputElement, aeroElement));

			//Rudder

			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfxListRudder, machDimension, machNumber, "x", axisElementX,
					rudderDeflectionAero, "rudder", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfyListRudder, machDimension, machNumber, "y", axisElementY, 
					rudderDeflectionAero, "rudder", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, cfzListRudder, machDimension, machNumber, "z", axisElementZ, 
					rudderDeflectionAero, "rudder", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmxRudderList, machDimension, machNumber, "roll", axisElementRoll,
					rudderDeflectionAero, "rudder", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmyRudderList, machDimension, machNumber, "pitch", axisElementPitch,
					rudderDeflectionAero, "rudder", outputElement, aeroElement));
			aeroElement.appendChild(JSBSimUtils.createAeroDataBodyAxisControlSurfaceElement(
					doc, dmzRudderList, machDimension, machNumber, "yaw", axisElementYaw,
					rudderDeflectionAero, "rudder", outputElement, aeroElement));
			
			//Damping derivative
			//p-rate
			
		if (dcfxListp.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfxListp, machDimension, machNumber, "p-x", axisElementX, "p", aeroElement));
			}
			if (dcfyListp.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfyListp, machDimension, machNumber, "p-y", axisElementY, "p", aeroElement));
			}
			if (dcfzListp.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfzListp, machDimension, machNumber, "p-z", axisElementZ, "p", aeroElement));
			}
			if (dmxpList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmxpList, machDimension, machNumber, "p-roll", axisElementRoll, "p", aeroElement));
			}
			if (dmypList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmypList, machDimension, machNumber, "p-pitch", axisElementPitch, "p", aeroElement));
			}
			if (dmzpList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmzpList, machDimension, machNumber, "p-yaw", axisElementYaw, "p", aeroElement));
			}
			// q-rate
			if (dcfxListq.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfxListq, machDimension, machNumber, "q-x", axisElementX, "q", aeroElement));
			}
			if (dcfyListq.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfyListq, machDimension, machNumber, "q-y", axisElementY, "q", aeroElement));
			}
			if (dcfzListq.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfzListq, machDimension, machNumber, "q-z", axisElementZ, "q", aeroElement));
			}
			if (dmxqList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmxqList, machDimension, machNumber, "q-roll", axisElementRoll, "q", aeroElement));
			}
			if (dmyqList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmyqList, machDimension, machNumber, "q-pitch", axisElementPitch, "q", aeroElement));
			}
			if (dmzqList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmzqList, machDimension, machNumber, "q-yaw", axisElementYaw, "q", aeroElement));
			}

			//r-rate
			if (dcfxListr.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfxListr, machDimension, machNumber, "r-x", axisElementX, "r", aeroElement));
			}
			if (dcfyListr.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfyListr, machDimension, machNumber, "r-y", axisElementY, "r", aeroElement));
			}
			if (dcfzListr.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dcfzListr, machDimension, machNumber, "r-z", axisElementZ, "r", aeroElement));
			}
			if (dmxrList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmxrList, machDimension, machNumber, "r-roll", axisElementRoll, "r", aeroElement));
			}
			if (dmyrList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmyrList, machDimension, machNumber, "r-pitch", axisElementPitch, "r", aeroElement));
			}
			if (dmzrList.size()>0) {
				aeroElement.appendChild(JSBSimUtils.createAeroDataDampingDerivativesBodyAxisElement(
						doc, outputElement, dmzrList, machDimension, machNumber, "r-yaw", axisElementYaw, "r", aeroElement));
			}
			

			
//			//Simplify
//			aeroElement.appendChild(
//					JSBSimUtils.createAeroDataBodyAxisElementSimplify(
//							doc, outputElement, cdList, machDimension, machNumber, "drag", axisElementDrag, aeroElement, "longitudinal"));
//			aeroElement.appendChild(
//					JSBSimUtils.createAeroDataBodyAxisElementSimplify(
//							doc, outputElement, cyList, machDimension, machNumber, "side", axisElementSide, aeroElement, "latero"));
//			aeroElement.appendChild(
//					JSBSimUtils.createAeroDataBodyAxisElementSimplify(
//					doc, outputElement, clList, machDimension, machNumber, "lift", axisElementLift, aeroElement, "longitudinal"));
//			aeroElement.appendChild(
//					JSBSimUtils.createAeroDataBodyAxisElementSimplify(
//					doc, outputElement, cmxList, machDimension, machNumber, "roll", axisElementRoll, aeroElement, "latero"));
//			aeroElement.appendChild(
//					JSBSimUtils.createAeroDataBodyAxisElementSimplify(
//					doc, outputElement, cmyList, machDimension, machNumber, "pitch", axisElementPitch, aeroElement, "longitudinal"));
//			aeroElement.appendChild(
//					JSBSimUtils.createAeroDataBodyAxisElementSimplify(
//					doc, outputElement, cmzList, machDimension, machNumber, "yaw", axisElementYaw, aeroElement, "latero"));
			
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

			JPADStaticWriteUtils.writeSingleNode("property","propulsion/engine[0]/thrust-lbs",outputElement,doc);
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
			JPADStaticWriteUtils.writeSingleNode("property","fcs/right-aileron-pos-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","fcs/flap-pos-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","fcs/elevator-pos-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","fcs/rudder-pos-deg",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","fcs/velocities/u-fps",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","fcs/velocities/v-fps",outputElement,doc);
			JPADStaticWriteUtils.writeSingleNode("property","fcs/velocities/w-fps",outputElement,doc);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return doc;
	}

	public void writeScriptFile(String scriptPath, 
			String aircraftName, String icFileNameBase, 
			double startTime, double endTime, double deltaTime,
			JSBSimScriptsTemplateEnums typeOfScript
			) throws ParserConfigurationException {
		
		DocumentBuilderFactory docScriptFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docScriptBuilder;
		Document docScript = null;
		docScriptBuilder = docScriptFactory.newDocumentBuilder();
		docScript = docScriptBuilder.newDocument();

		org.w3c.dom.Element runscriptElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				docScript,"runscript",
				Tuple.of("name", "Test of " + aircraftName)
				);
		docScript.appendChild(runscriptElement);
		JPADStaticWriteUtils.writeSingleNode("decription",
				"CPACS simulation with JSBSim through JPAD software", runscriptElement,docScript);
		org.w3c.dom.Element useElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				docScript, "use",
				Tuple.of("aircraft", aircraftName), // TODO: get aircraft name from _cpaceReader
				Tuple.of("initialize", icFileNameBase)
				);
		runscriptElement.appendChild(useElement);
		org.w3c.dom.Element runElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				docScript, "run",
				Tuple.of("start", Double.toString(startTime)), // TODO: get aircraft name from _cpaceReader
				Tuple.of("end", Double.toString(endTime)),
				Tuple.of("dt", Double.toString(deltaTime))
				);
		runscriptElement.appendChild(runElement);
		
		org.w3c.dom.Element propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				docScript, "property", "simulation/notify-time-trigger", 
				3, 6, Tuple.of("value", "0"));
		runElement.appendChild(propertyElem);
		
		propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
				docScript, "property", "simulation/run_id", 
				3, 6, Tuple.of("value", "1"));
		runElement.appendChild(propertyElem);
		
		//===============================================================================
		// the core part of the event sequence, according to the chosen template
		
		org.w3c.dom.Element eventElem = null;
		org.w3c.dom.Element setElem = null;
		org.w3c.dom.Element notifyElem = null;
		
		switch (typeOfScript) {
		case TAKEOFF: // TODO: check the sequence, parametrize
			propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					docScript, "property", "fcs/left-brake-cmd-norm", 
					3, 6, Tuple.of("value", "1"));
			runElement.appendChild(propertyElem);
			propertyElem = JPADStaticWriteUtils.createXMLElementWithValueAndAttributes(
					docScript, "property", "fcs/right-brake-cmd-norm", 
					3, 6, Tuple.of("value", "1"));
			runElement.appendChild(propertyElem);
			//Start Element
			eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript,"event",
					Tuple.of("name", "engine start") // TODO: get aircraft name from _cpaceReader
					);
			runElement.appendChild(eventElem);	
			JPADStaticWriteUtils.writeSingleNode("description", "Start the engine", eventElem, docScript);
			JPADStaticWriteUtils.writeSingleNode("condition"," simulation/sim-time-sec GE 0.2", eventElem, docScript);
			setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript, "set",
					Tuple.of("name", "fcs/throttle-cmd-norm"), 
					Tuple.of("value", "1.0"));
			eventElem.appendChild(setElem);	
			notifyElem = docScript.createElement("notify");
			eventElem.appendChild(notifyElem);
			JPADStaticWriteUtils.writeSingleNode("property"," position/h-agl-ft", notifyElem, docScript);
			JPADStaticWriteUtils.writeSingleNode("property"," velocities/vc-kts", notifyElem, docScript);

			// 2nd event
			eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript, "event",
					Tuple.of("name", "begin roll") // TODO: get aircraft name from _cpaceReader
					);
			runElement.appendChild(eventElem);
			JPADStaticWriteUtils.writeSingleNode("description", "Release brakes and get rolling with flaps at 30 degrees.", eventElem, docScript);
			JPADStaticWriteUtils.writeSingleNode("condition", "simulation/sim-time-sec GE 2.5", eventElem, docScript);
			setElem = JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript, "set",
					Tuple.of("name", "fcs/left-brake-cmd-norm"), 
					Tuple.of("value", "0"));
			eventElem.appendChild(setElem);	
			setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript, "set",
					Tuple.of("name", "fcs/right-brake-cmd-norm"), 
					Tuple.of("value", "0"));
			eventElem.appendChild(setElem);	
			setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript, "set",
					Tuple.of("name", "fcs/flap-cmd-norm"), 
					Tuple.of("value", "0.66"));
			eventElem.appendChild(setElem);	
			notifyElem = docScript.createElement("notify");
			eventElem.appendChild(notifyElem);
			JPADStaticWriteUtils.writeSingleNode("property", "position/h-agl-ft", notifyElem, docScript);
			JPADStaticWriteUtils.writeSingleNode("property", "velocities/vc-kts", notifyElem, docScript);

			// 3rd event
			eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript,"event",
					Tuple.of("name", "Remove flap") // TODO: get aircraft name from _cpaceReader
					);
			runElement.appendChild(eventElem);	
			JPADStaticWriteUtils.writeSingleNode("description", "at 1000 feet remove flap", eventElem, docScript);
			JPADStaticWriteUtils.writeSingleNode("condition", "position/h-agl-ft GE 1000", eventElem, docScript);
			setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
					docScript,"set",
					Tuple.of("name", "fcs/flap-cmd-norm"), 
					Tuple.of("value", "0.0"));
			eventElem.appendChild(setElem);	
			notifyElem = docScript.createElement("notify");
			eventElem.appendChild(notifyElem);
			JPADStaticWriteUtils.writeSingleNode("property", "position/h-agl-ft", notifyElem, docScript);
			JPADStaticWriteUtils.writeSingleNode("property", "velocities/vc-kts", notifyElem, docScript);
			break;
		case AIRBORNE: // TODO
			break;
		case AIRBORNE_TRIM: // TODO			
			break;
		default:
			break;
		}
		
		// triggered notify event
		eventElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
				docScript, "event",
				Tuple.of("name", "Repeating Notify"),
				Tuple.of("persistent", "true")
				);
		runElement.appendChild(eventElem);
		double deltaTimeNotify = 5.0; // TODO: parametrize this!
		JPADStaticWriteUtils.writeSingleNode(
				"description", 
				"Output message at fixed time intervals (" + deltaTimeNotify + " s)", 
				eventElem, docScript);
		notifyElem = docScript.createElement("notify");
		
		JPADStaticWriteUtils.writeSingleNode("property", "position/h-agl-ft", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "velocities/vt-fps", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "velocities/h-dot-fps", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "aero/alpha-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "aero/beta-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "aero/Re", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "velocities/mach", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "attitude/phi-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "attitude/theta-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "attitude/psi-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "velocities/p-rad_sec", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "velocities/q-rad_sec", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "velocities/r-rad_sec", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "propulsion/engine[0]/n2", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "propulsion/engine[1]/n2", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "propulsion/engine[0]/thrust-lbs", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "propulsion/engine[1]/thrust-lbs", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "fcs/elevator-pos-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "fcs/flap-pos-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "fcs/rudder-pos-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("property", "fcs/right-aileron-pos-deg", notifyElem, docScript);
		JPADStaticWriteUtils.writeSingleNode("condition","simulation/sim-time-sec GE simulation/notify-time-trigger", eventElem, docScript);
		eventElem.appendChild(notifyElem);
		setElem =  JPADStaticWriteUtils.createXMLElementWithAttributes(
				docScript,"set",
				Tuple.of("name", "simulation/notify-time-trigger"), 
				Tuple.of("value", Double.toString(deltaTimeNotify)),
				Tuple.of("type", "FG_DELTA")
				);
		eventElem.appendChild(setElem);	
		
		// Finally, write on file
		JPADStaticWriteUtils.writeDocumentToXml(docScript, scriptPath);

	}
	
	//Start JSBSim script
	public void startJSBSimSimulation(String dirPath, String simulationName) throws IOException, InterruptedException {
		List<String> commandList = new ArrayList<String>();
		String Simulation1 = "JSBSim.exe";
		String Simulation2 = "--script=scripts/"+simulationName;
		
		commandList.add(Simulation1);
		commandList.add(Simulation2);
		JSBSimUtils.runJSBSIM(commandList, dirPath);

	}
	public void writeInitialConditionsFile(String initializePath, double vt, double longitude,
			double latitude, double phi, double theta, double psi, double altitude, double elevation, double hwind) {
		Document doc = writeInitialCondition(vt,
				longitude, latitude, phi, theta, psi, altitude, elevation, hwind);

		System.out.println("[JSBSimModel.exportToXML] writing file " + initializePath + " ...");
		JPADStaticWriteUtils.writeDocumentToXml(doc, initializePath);
	}
	public Document writeInitialCondition(double vt, double longitude,
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
					doc, "vt",
					vt, 3, 6, // value, rounding-above, rounding-below
					Tuple.of("unit", "FT/SEC")
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

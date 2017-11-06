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
		
		double wingSurface = _cpacsReader.getWingReferenceArea(wingIndex);
		System.out.println("wingSurface: " + wingSurface);
//		double wingWettedArea = _cpacsReader.getWingWettedArea(wingUID);
//		System.out.println("wingWettedArea: " + wingWettedArea);

		Amount<Area> wingArea = Amount.valueOf(wingSurface,SI.SQUARE_METRE);
		System.out.println("wing area: " + wingArea);
		
		double wspan = _cpacsReader.getWingSpan(wingUID);
		Amount<Length> wingSpan = Amount.valueOf(wspan,SI.METRE);
		System.out.println("wing span: " + wingSpan);

		Node wingNode = wingsNodes.item(wingIndex);
		Double wrootchrd = CPACSUtils.getWingChord(wingNode);
		Amount<Length> wingRootChord = Amount.valueOf(wrootchrd,SI.METRE);
		System.out.println("wing root chord: " + wingRootChord);
		

		NodeList wingSectionElement = MyXMLReaderUtils.getXMLNodeListByPath(
				jpadXmlReader.getXmlDoc(), 
				"/cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/sections");
		int lastWingElementIndex = wingSectionElement.getLength()-1;//Vector start from 0
		
		
		//Horizontal tail
		double horizontalSurface = _cpacsReader.getWingReferenceArea(horizontalTailIndex);
		double horizontalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(horizontalTailUID,"x");
		double horizontalTailAeroCenter =_cpacsReader.getWingRootLeadingEdge(horizontalTailUID,"x") +
				horizontalTailMACLeadingEdge + (0.25)*_cpacsReader.getWingMeanAerodynamicChord(horizontalTailUID);	
		double horizontalTailArm = horizontalTailAeroCenter-GravityCenterPosition[0];


		//Vertical tail
		double verticalTailSurface = _cpacsReader.getWingReferenceArea(verticalTailIndex);
		double verticalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(verticalTailUID,"x");
		double sweepVerticalTail = _cpacsReader.getWingSweep(verticalTailUID);
		double verticalTailAeroCenter = _cpacsReader.getWingRootLeadingEdge(verticalTailUID,"x") + 
				verticalTailMACLeadingEdge + (0.25)*_cpacsReader.getWingMeanAerodynamicChord(verticalTailUID);	
		double verticalTailArmHorizontalDirection = verticalTailAeroCenter-GravityCenterPosition[0];
		double verticalTailArmVerticalDirection = _cpacsReader.getWingRootLeadingEdge(verticalTailUID,"z")
				+verticalTailMACLeadingEdge/sweepVerticalTail - _cpacsReader.getWingRootLeadingEdge(wingUID,"z");

		//AERO REFERENCE
		double[] aeroReferenceCenter = new double [3];
		aeroReferenceCenter[0] =_cpacsReader.getWingRootLeadingEdge(wingUID,"x") +
				horizontalTailMACLeadingEdge + (0.25)*_cpacsReader.getWingMeanAerodynamicChord(wingUID);	
		aeroReferenceCenter[1] = 0;
		aeroReferenceCenter[2] = _cpacsReader.getWingRootLeadingEdge(wingUID,"z");

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


		//Left Main
		String leftMainXPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/x");		
		String leftMainYPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/y");	
		String leftMainZPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/z");	
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


		//Right Main
		String rightMainXPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/x");		
		String rightMainYPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/y");	
		String rightMainZPosition = jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/z");			
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

		//Propulsion
		//GeoData
		double[] engineRotation = _cpacsReader.getVectorPosition(
				"pacs/vehicles/aircraft/model/engines/engine/transformation/rotation");
		double[] enginePosition = _cpacsReader.getVectorPosition(
				"pacs/vehicles/aircraft/model/engines/engine/transformation/translation");
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
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/TankFuel");
		double[][] tankMatrix = new double [tankNumberElement.getLength()][3];
		List<String> capacityTank = null;
		List<String> contentTank = null;
		List<String> fuelTypeTank = null;
		for(int i= 0;i<tankNumberElement.getLength();i++) {
			double[] VectorTankPosition = _cpacsReader.getVectorPosition(
					"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
							+ "TankFuel/tank["+i+"]/location");
			for (int j = 0;j<3;j++) {
				tankMatrix[i][j] = VectorTankPosition[j];
				capacityTank.add("cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "TankFuel/tank["+i+"]/capacity");
				contentTank.add("cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "TankFuel/tank["+i+"]/contents");
				fuelTypeTank.add("cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "TankFuel/tank["+i+"]/type");	
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

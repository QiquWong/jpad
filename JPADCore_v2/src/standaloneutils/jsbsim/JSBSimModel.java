package standaloneutils.jsbsim;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jna.ptr.IntByReference;

import de.dlr.sc.tigl.CpacsConfiguration;
import de.dlr.sc.tigl.Tigl;
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

	JPADXmlReader _jpadXmlReader;
	CPACSReader _cpacsReader;
	private String _cpacsFilePath;	
	private ReadStatus _status = null;
	CpacsConfiguration _config;

	public JSBSimModel(String filePath) {
		_cpacsFilePath = filePath;
		init();
	}


	private void init() {

		try {				

			_status = ReadStatus.OK;
			_jpadXmlReader = new JPADXmlReader(_cpacsFilePath);
			_config = Tigl.openCPACSConfiguration(_cpacsFilePath,"");
			_cpacsReader = new CPACSReader(_cpacsFilePath);
			_jpadXmlReader = new JPADXmlReader(_cpacsFilePath);
		} 
		catch (TiglException e) {
			_status = ReadStatus.ERROR;
			System.err.println(e.getMessage());
			System.err.println(e.getErrorCode());
		}		
	}


	public JSBSimModel(CPACSReader reader) {
		_cpacsReader = reader;	
	}

	public CPACSReader getCpacsReader() {
		return _cpacsReader;
	}

	public Document getXmlDoc() {
		return _jpadXmlReader.getXmlDoc();
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
		return _jpadXmlReader;
	}

	public void readVariablesFromCPACS(String cpacsFilePath) throws TiglException {
		double[] GravityCenterPosition;

		GravityCenterPosition = _cpacsReader.getGravityCenterPosition();
		NodeList wingsNodes = _cpacsReader.getWingList();
		String wingUID = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID", "uID");
		String horizontalTailUID =_jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/input/wings/HorizTail/HorizTailUID", "uID");
		String verticalTailUID =_jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/input/wings/HorizTail/HorizTailUID", "uID");
		int wingIndex = _cpacsReader.getWingIndex(wingUID);
		int horizontalTailIndex = _cpacsReader.getWingIndex(horizontalTailUID);
		int verticalTailIndex = _cpacsReader.getWingIndex(verticalTailUID); 
		//TO DO insert a pointer to the UID of the wing, in the cpacs wing Horizontal tail and verticall tail can have any name 
		//Start from wing
		double wingSurface = _cpacsReader.getWingReferenceArea(wingIndex); 
		double wingSpan = _cpacsReader.getWingSpan(wingUID);
		NodeList wingSectionElement = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]sections");
		int lastWingElementIndex = wingSectionElement.getLength()-1;//Vector start from 0
		String wingChordString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing/sections/section["+0+"]"
						+ "/elements/element/transformation/scaling/x");
		double wingRootChord = Double.parseDouble(wingChordString);			
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
		String ixx = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxx");
		String iyy = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyy");
		String izz = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jzz");
		String ixy = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxy");
		String ixz = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxz");
		String iyz = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyz");
		String emptyWeight = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/mass");
		//		cpacs/vehicles/aircraft/model/landingGear/noseGears/noseGear/fuselageAttachment/translation ->Path in the CPACS of the nose gear
		//		cpacs/vehicles/aircraft/model/landingGear/mainGears/mainGear/wingAttachment/positioning ->Path in the CPACS of the main gear

		//GroundReaction
		NodeList landingGearList = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear");
		//Nose gear
		String noseGearXPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/x");		
		String noseGearYPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/y");	
		String noseGearZPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/z");	
		String noseGearAttribute = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]", "type"); 
		String noseGearStatic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/static_friction");
		String noseGearDynamic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/dynamic_friction");
		String noseGearRolling_friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/rolling_friction");
		String noseGearSpring_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/spring_coeff"); //LBS/FT
		String noseGearDamping_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/damping_coeff");//LBS/FT
		String noseGearDamping_coeff_rebound = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/damping_coeff_rebound");//LBS/FT		

		String noseGearMax_steer = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/max_steer");//Deg
		String noseGearRetractable = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/retractable");//1->Retrectable 0 -> Fixed


		//Left Main
		String leftMainXPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/x");		
		String leftMainYPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/y");	
		String leftMainZPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/z");	
		String leftMainGearAttribute = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]", "type");
		String leftMainGearStatic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/static_friction");
		String leftMainGearDynamic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/dynamic_friction");
		String leftMainGearRolling_friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/rolling_friction");
		String leftMainGearSpring_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/spring_coeff"); //LBS/FT
		String leftMainGearDamping_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/damping_coeff");//LBS/FT
		String leftMainGearDamping_coeff_rebound = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/damping_coeff_rebound");//LBS/FT		

		String leftMainGearMax_steer = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/max_steer");//Deg
		String leftMainGearRetractable = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/retractable");//1->Retrectable 1 -> Fixed


		//Right Main
		String rightMainXPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/x");		
		String rightMainYPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/y");	
		String rightMainZPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/z");			
		String rightMainGearAttribute = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]", "type");	
		String rightMainGearStatic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/static_friction");
		String rightMainGearDynamic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/dynamic_friction");
		String rightMainGearRolling_friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/rolling_friction");
		String rightMainGearSpring_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/spring_coeff"); //LBS/FT
		String rightMainGearDamping_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/damping_coeff");//LBS/FT
		String rightMainGearDamping_coeff_rebound = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/damping_coeff_rebound");//LBS/FT		

		String rightMainGearMax_steer = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/max_steer");//Deg
		String rightMainGearRetractable = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/retractable");//2->Retrectable 2 -> Fixed	

		//Propulsion
		//GeoData
		double[] engineRotation = _cpacsReader.getVectorPosition(
				"pacs/vehicles/aircraft/model/engines/engine/transformation/rotation");
		double[] enginePosition = _cpacsReader.getVectorPosition(
				"pacs/vehicles/aircraft/model/engines/engine/transformation/translation");
		//Propulsion Data -> need to write engine script in JSBSim
		String milthrust =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/milthrust");
		String bypassratio =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bypassratio");
		String tsfc =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/tsfc");
		String bleed =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bleed");
		String idlen1 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/idlen1");
		String idlen2 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/idlen2");
		String maxn1 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/maxn1");
		String maxn2 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/maxn2");
		String augmented =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/augmented");
		String injected =  _jpadXmlReader.getXMLPropertyByPath(
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
				if (i==0&&j==0) {
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
		String thrust00 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/thrust00");
		String opr00 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/opr00");
		String bpr00 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bpr00");
		String fpr00 =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/fpr00");

		//tank
		NodeList tankNumberElement = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
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


	public ReadStatus getStatus() {
		return _status;
	}


	public void exportToXML(File file) {
		// TODO 
		
	}	

} //end main

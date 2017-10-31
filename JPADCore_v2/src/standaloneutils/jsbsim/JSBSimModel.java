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
		String WingUID = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/input/wings/MainWing/mainWingUID", "uID");
		String HorizontalTailUID =_jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/input/wings/HorizTail/HorizTailUID", "uID");
		String VerticalTailUID =_jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/input/wings/HorizTail/HorizTailUID", "uID");
		int wingIndex = _cpacsReader.getWingIndex(WingUID);
		int HorizontalTailIndex = _cpacsReader.getWingIndex(HorizontalTailUID);
		int VerticalTailIndex = _cpacsReader.getWingIndex(VerticalTailUID); 
		//TO DO insert a pointer to the UID of the wing, in the cpacs wing Horizontal tail and verticall tail can have any name 
		//Start from wing
		double WingSurface = _cpacsReader.getWingReferenceArea(wingIndex); 
		double WingSpan = _cpacsReader.getWingSpan(WingUID);
		NodeList WingSectionElement = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]sections");
		int LastWingElementIndex = WingSectionElement.getLength()-1;//Vector start from 0
		String WingChordString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing/sections/section["+0+"]"
						+ "/elements/element/transformation/scaling/x");
		double WingRootChord = Double.parseDouble(WingChordString);			
		//Horizontal tail
		double HorizontalSurface = _cpacsReader.getWingReferenceArea(HorizontalTailIndex);
		double HorizontalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(HorizontalTailUID,"x");
		double HorizontalTailAeroCenter =_cpacsReader.getWingRootLeadingEdge(HorizontalTailUID,"x") +
				HorizontalTailMACLeadingEdge + (0.25)*_cpacsReader.getWingMeanAerodynamicChord(HorizontalTailUID);	
		double HorizontalTailArm = HorizontalTailAeroCenter-GravityCenterPosition[0];


		//Vertical tail
		double VerticalTailSurface = _cpacsReader.getWingReferenceArea(VerticalTailIndex);
		double VerticalTailMACLeadingEdge = _cpacsReader.getMeanChordLeadingEdge(VerticalTailUID,"x");
		double SweepVerticalTail = _cpacsReader.getWingSweep(VerticalTailUID);
		double VerticalTailAeroCenter = _cpacsReader.getWingRootLeadingEdge(VerticalTailUID,"x") + 
				VerticalTailMACLeadingEdge + (0.25)*_cpacsReader.getWingMeanAerodynamicChord(VerticalTailUID);	
		double VerticalTailArmHorizontalDirection = VerticalTailAeroCenter-GravityCenterPosition[0];
		double VerticalTailArmVerticalDirection = _cpacsReader.getWingRootLeadingEdge(VerticalTailUID,"z")
				+VerticalTailMACLeadingEdge/SweepVerticalTail - _cpacsReader.getWingRootLeadingEdge(WingUID,"z");

		//AERO REFERENCE
		double[] AeroReferenceCenter = new double [3];
		AeroReferenceCenter[0] =_cpacsReader.getWingRootLeadingEdge(WingUID,"x") +
				HorizontalTailMACLeadingEdge + (0.25)*_cpacsReader.getWingMeanAerodynamicChord(WingUID);	
		AeroReferenceCenter[1] = 0;
		AeroReferenceCenter[2] = _cpacsReader.getWingRootLeadingEdge(WingUID,"z");

		//Eyepoint
		double[] EyePointReferenceCenter = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]");
		//		double[] EyePointReferenceCenter = new double [3];
		//		EyePointReferenceCenter[0] = Double.parseDouble( _jpadXmlReader.getXMLPropertyByPath(
		//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]/x"));		   ;	
		//		EyePointReferenceCenter[1] = Double.parseDouble( _jpadXmlReader.getXMLPropertyByPath(
		//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]/y"));
		//		EyePointReferenceCenter[2] = Double.parseDouble( _jpadXmlReader.getXMLPropertyByPath(
		//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+1+"]/z"));
		//Visual
		double[] VisualReferenceCenter = _cpacsReader.getVectorPosition(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]");
		//		double[] VisualReferenceCenter = new double [3];
		//		VisualReferenceCenter[0] = Double.parseDouble( _jpadXmlReader.getXMLPropertyByPath(
		//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]/x"));		   ;	
		//		VisualReferenceCenter[1] = Double.parseDouble( _jpadXmlReader.getXMLPropertyByPath(
		//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]/y"));
		//		VisualReferenceCenter[2] = Double.parseDouble( _jpadXmlReader.getXMLPropertyByPath(
		//				"cpacs/toolspecific/UNINA_modules/JSBSim_data/location["+2+"]/z"));				
		//Star Mass Balance -->Mass and Inertia
		String Ixx = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxx");
		String Iyy = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyy");
		String Izz = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jzz");
		String Ixy = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxy");
		String Ixz = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jxz");
		String Iyz = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/massInertia/Jyz");
		String EmptyWeight = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/"
						+ "mOEM/mEM/massDescription/mass");
		//		cpacs/vehicles/aircraft/model/landingGear/noseGears/noseGear/fuselageAttachment/translation ->Path in the CPACS of the nose gear
		//		cpacs/vehicles/aircraft/model/landingGear/mainGears/mainGear/wingAttachment/positioning ->Path in the CPACS of the main gear

		//GroundReaction
		NodeList LandingGearList = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear");
		//Nose gear
		String NoseGearXPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/x");		
		String NoseGearYPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/y");	
		String NoseGearZPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]/location/z");	
		String NoseGearAttribute = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+0+"]", "type"); 
		String NoseGearStatic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/static_friction");
		String NoseGearDynamic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/dynamic_friction");
		String NoseGearRolling_friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/rolling_friction");
		String NoseGearSpring_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/spring_coeff"); //LBS/FT
		String NoseGearDamping_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/damping_coeff");//LBS/FT
		String NoseGearDamping_coeff_rebound = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/damping_coeff_rebound");//LBS/FT		

		String NoseGearMax_steer = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/max_steer");//Deg
		String NoseGearRetractable = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+0+"]/retractable");//1->Retrectable 0 -> Fixed


		//Left Main
		String LeftMainXPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/x");		
		String LeftMainYPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/y");	
		String LeftMainZPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]/location/z");	
		String LeftMainGearAttribute = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+1+"]", "type");
		String LeftMainGearStatic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/static_friction");
		String LeftMainGearDynamic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/dynamic_friction");
		String LeftMainGearRolling_friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/rolling_friction");
		String LeftMainGearSpring_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/spring_coeff"); //LBS/FT
		String LeftMainGearDamping_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/damping_coeff");//LBS/FT
		String LeftMainGearDamping_coeff_rebound = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/damping_coeff_rebound");//LBS/FT		

		String LeftMainGearMax_steer = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/max_steer");//Deg
		String LeftMainGearRetractable = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+1+"]/retractable");//1->Retrectable 1 -> Fixed


		//Right Main
		String RightMainXPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/x");		
		String RightMainYPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/y");	
		String RightMainZPosition = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]/location/z");			
		String RightMainGearAttribute = _jpadXmlReader.getXMLAttributeByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "landingGear/contact["+2+"]", "type");	
		String RightMainGearStatic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/static_friction");
		String RightMainGearDynamic_Friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/dynamic_friction");
		String RightMainGearRolling_friction = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/rolling_friction");
		String RightMainGearSpring_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/spring_coeff"); //LBS/FT
		String RightMainGearDamping_coeff = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/damping_coeff");//LBS/FT
		String RightMainGearDamping_coeff_rebound = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/damping_coeff_rebound");//LBS/FT		

		String RightMainGearMax_steer = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/max_steer");//Deg
		String RightMainGearRetractable = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/landingGear"
						+ "/contact["+2+"]/retractable");//2->Retrectable 2 -> Fixed	

		//Propulsion
		//GeoData
		double[] EngineRotation = _cpacsReader.getVectorPosition(
				"pacs/vehicles/aircraft/model/engines/engine/transformation/rotation");
		double[] EnginePosition = _cpacsReader.getVectorPosition(
				"pacs/vehicles/aircraft/model/engines/engine/transformation/translation");
		//Propulsion Data -> need to write engine script in JSBSim
		String Milthrust =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/milthrust");
		String Bypassratio =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/bypassratio");
		String tsfc =  _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/engines/engine/analysis/tsfc");
		String Bleed =  _jpadXmlReader.getXMLPropertyByPath(
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
		List<Double> FlightLevelEngine = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/flightLevel");
		List<Double> MachNumberEngine = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/machNumber");
		List<Double> IdleThrust = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/IdleThrust");
		//Building Table for engine
		double [][] IdleThrustTable = new double [MachNumberEngine.size()+1][FlightLevelEngine.size()+1];
		int k = 0;
		for(int i= 0;i<MachNumberEngine.size()+1;i++) {
			for (int j = 0;j<FlightLevelEngine.size()+1;j++) {
				if (i==0&&j==0) {
					IdleThrustTable[i][j]=0;
				}
				if (j==0 && i !=0 ){
					IdleThrustTable[i][j]=MachNumberEngine.get(i-1);
				}
				if (i==0 && j !=0 ){
					IdleThrustTable[i][j]=FlightLevelEngine.get(j-1);
				}
				else {
					IdleThrustTable[i][j]=IdleThrust.get(k);
					if (k<FlightLevelEngine.size()*MachNumberEngine.size()-1) {
						k++;							
					}
				}
			}						
		}
		String IdleThrustTableString = CPACSUtils.matrixDoubleToJSBSimTable2D(IdleThrustTable, "	");

		//Build 2nd table for engine
		List<Double> MilThrust = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/vehicles/engines/engine/analysis/performanceMaps/performanceMap/MilThrust");
		double [][] MilThrustTable = new double [MachNumberEngine.size()+1][FlightLevelEngine.size()+1];
		k = 0;
		for(int i= 0;i<MachNumberEngine.size()+1;i++) {
			for (int j = 0;j<FlightLevelEngine.size()+1;j++) {
				if (i==0&&j==0) {
					MilThrustTable[i][j]=0;
				}
				if (j==0 && i !=0 ){
					MilThrustTable[i][j]=MachNumberEngine.get(i-1);
				}
				if (i==0 && j !=0 ){
					MilThrustTable[i][j]=FlightLevelEngine.get(j-1);
				}
				else {
					MilThrustTable[i][j]=IdleThrust.get(k);
					if (k<FlightLevelEngine.size()*MachNumberEngine.size()-1) {
						k++;							
					}
				}
			}						
		}
		String MilThrustTableString = CPACSUtils.matrixDoubleToJSBSimTable2D(MilThrustTable, "	");

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
		NodeList TankNumberElement = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/TankFuel");
		double[][] TankMatrix = new double [TankNumberElement.getLength()][3];
		List<String> capacityTank = null;
		List<String> contentTank = null;
		List<String> FuelTypeTank = null;
		for(int i= 0;i<TankNumberElement.getLength();i++) {
			double[] VectorTankPosition = _cpacsReader.getVectorPosition(
					"cpacs/toolspecific/UNINA_modules/JSBSim_data/"
							+ "TankFuel/tank["+i+"]/location");
			for (int j = 0;j<3;j++) {
				TankMatrix[i][j] = VectorTankPosition[j];
				capacityTank.add("cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "TankFuel/tank["+i+"]/capacity");
				contentTank.add("cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "TankFuel/tank["+i+"]/contents");
				FuelTypeTank.add("cpacs/toolspecific/UNINA_modules/JSBSim_data/"
						+ "TankFuel/tank["+i+"]/type");	
			}
		}						
		//Flight Control
		//"cpacs/vehicles/aircraft/model/systems/controlDistributors/controlDistributor"
		List<String> SystemCommadList = _cpacsReader.getSystemParameter(
				"cpacs/vehicles/aircraft/model/systems/controlDistributors/controlDistributor");

		//Ground Effect

		List<Double> HeightGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/Height");
		List<Double> KCLGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCL");
		List<Double> KCDGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCD");
		double[][] KCLMatrix = new double [KCLGroundEffect.size()][2];
		double[][] KCDMatrix = new double [KCLGroundEffect.size()][2];
		for (int i=0;i<KCLGroundEffect.size();i++) {
			KCLMatrix[i][0] = HeightGroundEffect.get(i); //-> effect on lift coefficient
			KCLMatrix[i][1] = KCLGroundEffect.get(i); 
			KCDMatrix[i][0] = HeightGroundEffect.get(i); //-> effect on drag coefficient
			KCDMatrix[i][1] = KCDGroundEffect.get(i);
		}
		String KCLTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(KCLMatrix);
		String KCDTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(KCDMatrix);

		//effect speed braker
		List<Double> KSpeedBreakerGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCLSB");
		List<Double> SpeedBreakerPosition = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/PositionSpeedBreaker");
		double[][] KSpeedBreakerMatrix = new double [KSpeedBreakerGroundEffect.size()][2];
		for (int i=0;i<KSpeedBreakerGroundEffect.size();i++) {
			KSpeedBreakerMatrix[i][0] = SpeedBreakerPosition.get(i); 
			KSpeedBreakerMatrix[i][1] = KSpeedBreakerGroundEffect.get(i); 
		}
		String KSpeedBreakerTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(KSpeedBreakerMatrix);

		//effect spoiler
		List<Double> KSpoilerGroundEffect = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCLSB");
		List<Double> SpoilerPosition = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/PositionSpeedBreaker");
		double[][] KSpoilerMatrix = new double [KSpoilerGroundEffect.size()][2];
		for (int i=0;i<KSpoilerGroundEffect.size();i++) {
			KSpoilerMatrix[i][0] = SpoilerPosition.get(i); 
			KSpoilerMatrix[i][1] = KSpoilerGroundEffect.get(i); 

		}
		String KSpoilerTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(KSpoilerMatrix);

		//Drag
		//1) CD0
		List<Double> AlphaCD0 = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/KCLSB");
		List<Double> CD0Value = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/GroundEffect/PositionSpeedBreaker");
		double[][] CD0Matrix = new double [CD0Value.size()][2];
		for (int i=0;i<CD0Value.size();i++) {
			CD0Matrix[i][0] = SpoilerPosition.get(i); 
			CD0Matrix[i][1] = KSpoilerGroundEffect.get(i); 
		}
		String CD0Table = CPACSUtils.matrixDoubleToJSBSimTableNx2(CD0Matrix);
		//2) Mach Effect
		List<Double> MachCDWave = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/Drag/CD_Mach/Mach");
		List<Double> CDWaveValue = _cpacsReader.getJpadXmlReader().readArrayDoubleFromXMLSplit(
				"cpacs/toolspecific/UNINA_modules/JSBSim_data/aerodynamics/Drag/CD_Mach/DeltaCD0");
		double[][] CDWaveMatrix = new double [CDWaveValue.size()][2];
		for (int i=0;i<CDWaveValue.size();i++) {
			CDWaveMatrix[i][0] = MachCDWave.get(i); 
			CDWaveMatrix[i][1] = CDWaveValue.get(i); 
		}
		String CDWaveTable = CPACSUtils.matrixDoubleToJSBSimTableNx2(CDWaveMatrix);	



	}//end CPACS READ VARIABLE


	public ReadStatus getStatus() {
		return _status;
	}


	public void exportToXML(File file) {
		// TODO 
		
	}	

} //end main

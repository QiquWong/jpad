package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class LandingGears {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private ILandingGear _theLandingGearInterface;
	private LandingGearsMountingPositionEnum _mountingPosition;
	private Amount<Length> _xApexConstructionAxesNoseGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxesNoseGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxesNoseGear = Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _xApexConstructionAxesMainGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxesMainGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxesMainGear = Amount.valueOf(0.0, SI.METER);
	
	// to be moved ... TODO: add methods to calculate main and nose gears masses separately (see EXCEL)
	private Amount<Mass> _overallMass, _mainMass, _noseMass;
	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private List<MethodEnum> _methodsList;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap;
	private double[] _percentDifference;
	private Amount<Mass> _referenceMass, _estimatedMass;
	private CenterOfGravity _cg;
	private Amount<Length> _xCG;

	//------------------------------------------------------------------------------------------
	// BUILDER
	public LandingGears (ILandingGear theLandingGearsInterface) {
		
		this._theLandingGearInterface = theLandingGearsInterface;
		
		this._massMap = new HashMap<>();
		this._xCGMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		this._methodsMap = new HashMap<>();
		
	}

	//------------------------------------------------------------------------------------------
	// METHODS
	
	public static LandingGears importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading landing gears data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// GLOBAL DATA
		Amount<Length> mainGearLegsLength = Amount.valueOf(0.0, SI.METER);
		Amount<Length> distanceBetweenWheels = Amount.valueOf(0.0, SI.METER);
		int numberOfFrontalWheels = 0;
		int numberOfRearWheels = 0;
		
		
		String mainGearLegsLengthProperty = reader.getXMLPropertyByPath("//global_data/main_gear_legs_length");
		if(mainGearLegsLengthProperty != null)
			mainGearLegsLength = reader.getXMLAmountLengthByPath("//global_data/main_gear_legs_length");
		
		String distanceBetweenWheelsProperty = reader.getXMLPropertyByPath("//global_data/distance_between_wheels");
		if(distanceBetweenWheelsProperty != null)
			distanceBetweenWheels = reader.getXMLAmountLengthByPath("//global_data/distance_between_wheels");
		
		String numberOfFrontalWheelsProperty = reader.getXMLPropertyByPath("//global_data/number_of_frontal_wheels");
		if(numberOfFrontalWheelsProperty != null)
			numberOfFrontalWheels = Integer.valueOf(numberOfFrontalWheelsProperty);
		
		String numberOfRearWheelsProperty = reader.getXMLPropertyByPath("//global_data/number_of_rear_wheels");
		if(numberOfRearWheelsProperty != null)
			numberOfRearWheels = Integer.valueOf(numberOfRearWheelsProperty);
		
		//---------------------------------------------------------------
		// FRONTAL WHEEL DATA
		Amount<Length> frontalWheelsHeight = Amount.valueOf(0.0, SI.METER);
		Amount<Length> frontalWheelsWidth = Amount.valueOf(0.0, SI.METER);
		
		String frontalWheelsHeightProperty = reader.getXMLPropertyByPath("//frontal_wheels_data/wheel_height");
		if(frontalWheelsHeightProperty != null)
			frontalWheelsHeight = reader.getXMLAmountLengthByPath("//frontal_wheels_data/wheel_height");
		
		String frontalWheelsWidthProperty = reader.getXMLPropertyByPath("//frontal_wheels_data/wheel_width");
		if(frontalWheelsWidthProperty != null)
			frontalWheelsWidth = reader.getXMLAmountLengthByPath("//frontal_wheels_data/wheel_width");
		
		//---------------------------------------------------------------
		// REAR WING DATA
		Amount<Length> rearWheelsHeight = Amount.valueOf(0.0, SI.METER);
		Amount<Length> rearWheelsWidth = Amount.valueOf(0.0, SI.METER);
		
		String rearWheelsHeightProperty = reader.getXMLPropertyByPath("//rear_wheels_data/wheel_height");
		if(rearWheelsHeightProperty != null)
			rearWheelsHeight = reader.getXMLAmountLengthByPath("//rear_wheels_data/wheel_height");
		
		String rearWheelsWidthProperty = reader.getXMLPropertyByPath("//rear_wheels_data/wheel_width");
		if(rearWheelsWidthProperty != null)
			rearWheelsWidth = reader.getXMLAmountLengthByPath("//rear_wheels_data/wheel_width");
		
		LandingGears landingGears = new LandingGears(
				new ILandingGear.Builder()
				.setId(id)
				.setMainLegsLenght(mainGearLegsLength)
				.setDistanceBetweenWheels(distanceBetweenWheels)
				.setNumberOfFrontalWheels(numberOfFrontalWheels)
				.setNumberOfRearWheels(numberOfRearWheels)
				.setFrontalWheelsHeight(frontalWheelsHeight)
				.setFrontalWheelsWidth(frontalWheelsWidth)
				.setRearWheelsHeight(rearWheelsHeight)
				.setRearWheelsWidth(rearWheelsWidth)
				.build()
				);
		
		return landingGears;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tLanding gears\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _theLandingGearInterface.getId() + "'\n")
				.append("\t·····································\n")
				.append("\tMain gear legs length: " + _theLandingGearInterface.getMainLegsLenght() + "\n")
				.append("\tDistance between wheels: " + _theLandingGearInterface.getDistanceBetweenWheels() + "\n")
				.append("\tNumber of frontal wheels: " + _theLandingGearInterface.getNumberOfFrontalWheels() + "\n")
				.append("\tNumber of rear wheels: " + _theLandingGearInterface.getNumberOfRearWheels() + "\n")
				.append("\t·····································\n")
				.append("\tFrontal wheels height: " + _theLandingGearInterface.getFrontalWheelsHeight() + "\n")
				.append("\tFrontal wheels width: " + _theLandingGearInterface.getFrontalWheelsWidth() + "\n")
				.append("\t·····································\n")
				.append("\tRear wheels height: " + _theLandingGearInterface.getRearWheelsHeight() + "\n")
				.append("\tRear wheels width: " + _theLandingGearInterface.getRearWheelsWidth() + "\n")
				.append("\t·····································\n")
				;
		
		return sb.toString();
		
	}
	
	public void calculateMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		calculateMass(aircraft, MethodEnum.ROSKAM);
		calculateMass(aircraft, MethodEnum.STANFORD);
		calculateMass(aircraft, MethodEnum.TORENBEEK_1982);
		calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		
		if (!methodsMapWeights.get(ComponentEnum.LANDING_GEAR).equals(MethodEnum.AVERAGE))
			_estimatedMass = _massMap.get(methodsMapWeights.get(ComponentEnum.LANDING_GEAR));
		else
			_estimatedMass = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_referenceMass, 
					_massMap,
					_percentDifference,
					25.).getMean(), SI.KILOGRAM);
		
	}

	public void calculateMass(Aircraft aircraft, MethodEnum method) {
		switch (method) {
		/* Average error > 30 %
		case JENKINSON : {
			_methodsList.add(method);
			_mass = aircraft.get_weights().get_MTOM().times(0.0445);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		 */
		case ROSKAM : { // Roskam page 97 (pdf) part V
			_methodsList.add(method);
			_overallMass = Amount.valueOf(
					62.21 * Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).times(1e-3).getEstimatedValue(), 0.84),
					NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case STANFORD : {
			_methodsList.add(method);
			_overallMass = aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().times(0.04);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_mainMass = Amount.valueOf(40 + 0.16 * 
					Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue(), 0.75) + 
					0.019 * aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue() + 
					1.5 * 1e-5 * Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue(), 1.5),
					NonSI.POUND).to(SI.KILOGRAM);
			_noseMass = Amount.valueOf(20 + 0.1 * 
					Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue(), 0.75) + 
					2 * 1e-5 * 
					Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue(), 1.5),
					NonSI.POUND).to(SI.KILOGRAM);

			_overallMass = _noseMass.plus(_mainMass);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_2013 : {
			_methodsList.add(method);
			_overallMass = aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().times(0.025).
					plus(aircraft.getTheAnalysisManager().getTheWeights().getMaximumLangingMass().times(0.016));
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case RAYMER : {
			//TODO
		} break;
		default : {} break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new double[_massMap.size()]; 

		_estimatedMass = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_referenceMass, 
				_massMap,
				_percentDifference,
				25.).getFilteredMean(), SI.KILOGRAM);

	}

	public void calculateCG(Aircraft aircraft) {
		calculateCG(aircraft, MethodEnum.SFORZA);
	}
	
	/** 
	 * Overload of the calculate CG method that evaluate the landing gear CG location in LRF.
	 */
	public void calculateCG(
			Aircraft aircraft, 
			MethodEnum method) {

		_cg = new CenterOfGravity();
		
		 // THESE VALUES WILL COME FROM THE AIRCRAFT CLASS
		_cg.setLRForigin(_xApexConstructionAxesMainGear,
						 _yApexConstructionAxesMainGear,
						 _zApexConstructionAxesMainGear
						 );
		_cg.setXLRFref(Amount.valueOf(0., SI.METER));
		_cg.setYLRFref(Amount.valueOf(0., SI.METER));
		_cg.setZLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		// page 337 (pdf) Sforza (2014) - Aircraft Design
		case SFORZA : { 
			_methodsList.add(method);
			
			double kFusLengthMainGear = 0.0;
			double kFusLengthNoseGear = 0.0;
			
			if((aircraft.getPowerPlant().getEngineList().get(0).getMountingPosition() == EngineMountingPositionEnum.WING)
					|| (aircraft.getPowerPlant().getEngineList().get(0).getMountingPosition() == EngineMountingPositionEnum.BURIED)) {
				kFusLengthMainGear = 0.55;
				kFusLengthNoseGear = 0.17;
			}
			else {
				kFusLengthMainGear = 0.6;
				kFusLengthNoseGear = 0.14;
			}
			_xCG = Amount.valueOf(
					(((aircraft.getLandingGears().getNoseMass()
							.times(aircraft.getFuselage().getFuselageLength().times(kFusLengthNoseGear)))
					.plus(aircraft.getLandingGears().getMainMass()
							.times(aircraft.getFuselage().getFuselageLength().times(kFusLengthMainGear))))
					.divide(aircraft.getLandingGears().getMassMap().get(MethodEnum.TORENBEEK_1982)))
					.getEstimatedValue(),
					SI.METER
					);		
			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);

		Amount<Length> zCGNoseLeg = aircraft.getLandingGears().getMainLegsLenght().divide(2);
		Amount<Length> zCGMainLeg = aircraft.getLandingGears().getMainLegsLenght().divide(2);
		Amount<Length> zCGNoseWheel = aircraft.getLandingGears().getFrontalWheelsHeight().divide(2)
										.plus(aircraft.getLandingGears().getMainLegsLenght());
		Amount<Length> zCGMainWheel = aircraft.getLandingGears().getRearWheelsHeight().divide(2)
										.plus(aircraft.getLandingGears().getMainLegsLenght());
		Amount<Length> noseLegLength = aircraft.getLandingGears().getMainLegsLenght();
		Amount<Length> mainLegLength = aircraft.getLandingGears().getMainLegsLenght();
		Amount<Length> noseWheelHeight = aircraft.getLandingGears().getFrontalWheelsHeight();
		Amount<Length> mainWheelHeight = aircraft.getLandingGears().getRearWheelsHeight();
		
		_cg.setXBRF(_xCGMap.get(MethodEnum.SFORZA));
		_cg.setZBRF(
				aircraft.getLandingGears().getZApexConstructionAxesMainGear().minus(
						Amount.valueOf(
								((zCGNoseLeg.times(noseLegLength))
										.plus((zCGNoseWheel).times(noseWheelHeight))
										.plus((zCGMainLeg).times(mainLegLength))
										.plus((zCGMainWheel).times(mainWheelHeight)))
								.divide(noseLegLength
										.plus(mainLegLength)
										.plus(noseWheelHeight)
										.plus(mainWheelHeight)
										)
								.getEstimatedValue(),
								SI.METER
								)
						)
				);

	}

	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public ILandingGear getTheLandingGearsInterface() {
		return _theLandingGearInterface;
	}
	
	public void setTheLandingGearsInterface (ILandingGear theLandingGearsInterface) {
		this._theLandingGearInterface = theLandingGearsInterface;
	}
	
	public Amount<Mass> getOverallMass() {
		return _overallMass;
	}

	public void setMass(Amount<Mass> mass) {
		this._overallMass = mass;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public void setMassMap(Map<MethodEnum, Amount<Mass>> massMap) {
		this._massMap = massMap;
	}

	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	public void setMethodsMap(
			Map<AnalysisTypeEnum, List<MethodEnum>> methodsMap) {
		this._methodsMap = methodsMap;
	}

	public double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(double[] percentDifference) {
		this._percentDifference = percentDifference;
	}

	public Amount<Mass> getMassEstimated() {
		return _estimatedMass;
	}

	public void setMassEstimated(Amount<Mass> estimatedMass) {
		this._estimatedMass = estimatedMass;
	}
	
	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<MethodEnum, Amount<Length>> xCGMap) {
		this._xCGMap = xCGMap;
	}

	public CenterOfGravity getCG() {
		return _cg;
	}

	public void setCg(CenterOfGravity cg) {
		this._cg = cg;
	}

	public String getId() {
		return _theLandingGearInterface.getId();
	}

	public void setId (String id) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setId(id).build());
	}
	
	public Amount<Length> getMainLegsLenght() {
		return _theLandingGearInterface.getMainLegsLenght();
	}

	public void setMainLegsLenght(Amount<Length> lenght) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setMainLegsLenght(lenght).build());
	}
	
	public int getNumberOfFrontalWheels() {
		return _theLandingGearInterface.getNumberOfFrontalWheels();
	}

	public int getNumberOfRearWheels() {
		return _theLandingGearInterface.getNumberOfRearWheels();
	}

	public Amount<Length> getFrontalWheelsHeight() {
		return _theLandingGearInterface.getFrontalWheelsHeight();
	}

	public Amount<Length> getFrontalWheelsWidth() {
		return _theLandingGearInterface.getFrontalWheelsWidth();
	}

	public Amount<Length> getRearWheelsHeight() {
		return _theLandingGearInterface.getRearWheelsHeight();
	}

	public Amount<Length> getRearWheelsWidth() {
		return _theLandingGearInterface.getRearWheelsWidth();
	}

	public void setNumberOfFrontalWheels(int _numberOfFrontalWheels) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setNumberOfFrontalWheels(_numberOfFrontalWheels).build());
	}

	public void setNumberOfRearWheels(int _numberOfRearWheels) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setNumberOfRearWheels(_numberOfRearWheels).build());
	}

	public void setFrontalWheelsHeight(Amount<Length> _frontalWheelsHeight) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setFrontalWheelsHeight(_frontalWheelsHeight).build());
	}

	public void setFrontalWheelsWidth(Amount<Length> _frontalWheelsWidth) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setFrontalWheelsWidth(_frontalWheelsWidth).build());
	}

	public void setRearWheelsHeight(Amount<Length> _rearWheelsHeight) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setRearWheelsHeight(_rearWheelsHeight).build());
	}

	public void setRearWheelsWidth(Amount<Length> _rearWheelsWidth) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setRearWheelsWidth(_rearWheelsWidth).build());
	}
	
	public Amount<Mass> getReferenceMass() {
		return _referenceMass;
	}

	public void setReferenceMass(Amount<Mass> referenceMass) {
		this._referenceMass = referenceMass;
	}
	
	public Amount<Length> getXApexConstructionAxesMainGear() {
		return _xApexConstructionAxesMainGear;
	};
	
	public void setXApexConstructionAxesMainGear (Amount<Length> xApexConstructionAxes) {
		this._xApexConstructionAxesMainGear = xApexConstructionAxes;
	};
	
	public Amount<Length> getYApexConstructionAxesMainGear() {
		return _yApexConstructionAxesMainGear;
	};
	
	public void setYApexConstructionAxesMainGear (Amount<Length> yApexConstructionAxes) {
		this._yApexConstructionAxesMainGear = yApexConstructionAxes; 
	};
	
	public Amount<Length> getZApexConstructionAxesMainGear() {
		return _zApexConstructionAxesMainGear;
	};
	
	public void setZApexConstructionAxesMainGear (Amount<Length> zApexConstructionAxes) {
		this._zApexConstructionAxesMainGear = zApexConstructionAxes;
	}

	public LandingGearsMountingPositionEnum getMountingPosition() {
		return _mountingPosition;
	}

	public Amount<Length> getXApexConstructionAxesNoseGear() {
		return _xApexConstructionAxesNoseGear;
	}

	public void setXApexConstructionAxesNoseGear(Amount<Length> _xApexConstructionAxesNoseGear) {
		this._xApexConstructionAxesNoseGear = _xApexConstructionAxesNoseGear;
	}

	public Amount<Length> getYApexConstructionAxesNoseGear() {
		return _yApexConstructionAxesNoseGear;
	}

	public void setYApexConstructionAxesNoseGear(Amount<Length> _yApexConstructionAxesNoseGear) {
		this._yApexConstructionAxesNoseGear = _yApexConstructionAxesNoseGear;
	}

	public Amount<Length> getZApexConstructionAxesNoseGear() {
		return _zApexConstructionAxesNoseGear;
	}

	public void setZApexConstructionAxesNoseGear(Amount<Length> _zApexConstructionAxesNoseGear) {
		this._zApexConstructionAxesNoseGear = _zApexConstructionAxesNoseGear;
	}

	public void setMountingPosition(LandingGearsMountingPositionEnum _mountingPosition) {
		this._mountingPosition = _mountingPosition;
	}

	public Amount<Mass> getMainMass() {
		return _mainMass;
	}

	public void setMainMass(Amount<Mass> _mainMass) {
		this._mainMass = _mainMass;
	}

	public Amount<Mass> getNoseMass() {
		return _noseMass;
	}

	public void setNoseMass(Amount<Mass> _noseMass) {
		this._noseMass = _noseMass;
	}

	public Amount<Length> getDistanceBetweenWheels() {
		return _theLandingGearInterface.getDistanceBetweenWheels();
	}

	public void setDistanceBetweenWheels(Amount<Length> _distanceBetweenWheels) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setDistanceBetweenWheels(_distanceBetweenWheels).build());
	}
}
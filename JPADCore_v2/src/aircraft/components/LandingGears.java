package aircraft.components;

import static java.lang.Math.round;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;


public class LandingGears implements ILandingGear {

	private String _id;
	
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private int _numberOfFrontalWheels,
				_numberOfRearWheels;
	private Amount<Length> _mainLegsLenght;
	private Amount<Length> _frontalWheelsHeight,
						   _frontalWheelsWidth,
						   _rearWheelsHeight,
						   _rearWheelsWidth;
	
	private Amount<Mass> _overallMass,
						 _mainMass, 
						 _noseMass;

	private Map <MethodEnum, Amount<Mass>> _massMap;
	private Map <MethodEnum, Amount<Length>> _xCGMap;
	private List<MethodEnum> _methodsList;
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap;

	private Double[] _percentDifference;
	private Amount<Mass> _referenceMass, _estimatedMass;
	private CenterOfGravity _cg;

	private Amount<Length> _xCG;

	private Double[] _percentDifferenceXCG;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class LandingGearsBuilder {

		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private int __numberOfFrontalWheels,
					__numberOfRearWheels;
		private Amount<Length> __mainLegsLenght;
		private Amount<Length> __frontalWheelsHeight,
							   __frontalWheelsWidth,
							   __rearWheelsHeight,
							   __rearWheelsWidth;

		private Amount<Mass> __referenceMass;
		
		private Map <MethodEnum, Amount<Mass>> __massMap = new TreeMap<MethodEnum, Amount<Mass>>();
		private Map <MethodEnum, Amount<Length>> __xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
		private List<MethodEnum> __methodsList = new ArrayList<MethodEnum>();
		private Map <AnalysisTypeEnum, List<MethodEnum>> __methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
		
		public LandingGearsBuilder (String id) {
			this.__id = id;
			this.initializeDefaultVariables(AircraftEnum.ATR72);
		}
		
		public LandingGearsBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultVariables(aircraftName);
		}
		
		public LandingGearsBuilder numberOfFrontalWheels (int numberOfFrontalWheels) {
			this.__numberOfFrontalWheels = numberOfFrontalWheels;
			return this;
		}
		
		public LandingGearsBuilder numberOfRearWheels (int numberOfRearWheels) {
			this.__numberOfRearWheels = numberOfRearWheels;
			return this;
		}
		
		public LandingGearsBuilder mainLegsLength (Amount<Length> mainLegsLength) {
			this.__mainLegsLenght = mainLegsLength;
			return this;
		}
		
		public LandingGearsBuilder frontalWheelsHeight (Amount<Length> frontalWheelsHeight) {
			this.__frontalWheelsHeight = frontalWheelsHeight;
			return this;
		}
		
		public LandingGearsBuilder frontalWheelsWidth (Amount<Length> frontalWheelsWidth) {
			this.__frontalWheelsWidth = frontalWheelsWidth;
			return this;
		}
		
		public LandingGearsBuilder rearWheelsHeight (Amount<Length> rearWheelsHeight) {
			this.__rearWheelsHeight = rearWheelsHeight;
			return this;
		}
		
		public LandingGearsBuilder rearWheelsWidth (Amount<Length> rearWheelsWidht) {
			this.__rearWheelsWidth = rearWheelsWidht;
			return this;
		}
		
		public LandingGearsBuilder referenceMass (Amount<Mass> referenceMass) {
			this.__referenceMass = referenceMass;
			return this;
		}
		
		public LandingGears build() {
			return new LandingGears(this);
		}
		
		/**
		 * Overload of the previous builder that recognize aircraft name and sets 
		 * it's landing gear data.
		 * 
		 * @author Vittorio Trifari
		 */
		private void initializeDefaultVariables (AircraftEnum aircraftName) {

			switch(aircraftName) {

			case ATR72:
				__numberOfFrontalWheels = 2;
				__numberOfRearWheels = 4;
				__mainLegsLenght = Amount.valueOf(0.66, SI.METER);
				__frontalWheelsHeight = Amount.valueOf(0.450, SI.METER);
				__frontalWheelsWidth = Amount.valueOf(0.190, SI.METER);
				__rearWheelsHeight = Amount.valueOf(0.8636, SI.METER);
				__rearWheelsWidth  = Amount.valueOf(0.254, SI.METER);
				__referenceMass = Amount.valueOf(675.8, SI.KILOGRAM);
				break;

			case B747_100B:
				__numberOfFrontalWheels = 2;
				__numberOfRearWheels = 8;
				__mainLegsLenght = Amount.valueOf(3.26, SI.METER);
				__frontalWheelsHeight = Amount.valueOf(1.245, SI.METER);
				__frontalWheelsWidth = Amount.valueOf(0.4829, SI.METER);
				__rearWheelsHeight = Amount.valueOf(1.245, SI.METER);
				__rearWheelsWidth = Amount.valueOf(0.4829, SI.METER);
				__referenceMass = Amount.valueOf(13900.0, SI.KILOGRAM);
				break;

			case AGILE_DC1:
				//__referenceMass = Amount.valueOf(1501.6, SI.KILOGRAM);
				// TODO
				break;
			}
		}
	}
	
	private LandingGears (LandingGearsBuilder builder) {
		
		this._id = builder.__id;
		this._mainLegsLenght = builder.__mainLegsLenght;
		this._numberOfFrontalWheels = builder.__numberOfFrontalWheels;
		this._numberOfRearWheels = builder.__numberOfRearWheels;
		this._frontalWheelsHeight = builder.__frontalWheelsHeight;
		this._frontalWheelsWidth = builder.__frontalWheelsWidth;
		this._rearWheelsHeight = builder.__rearWheelsHeight;
		this._rearWheelsWidth = builder.__rearWheelsWidth;
		this._referenceMass = builder.__referenceMass;
		
		this._massMap = builder.__massMap;
		this._xCGMap = builder.__xCGMap;
		this._methodsList = builder.__methodsList;
		this._methodsMap = builder.__methodsMap;
		
	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	public static LandingGears importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading landing gears data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// GLOBAL DATA
		Amount<Length> mainGearLegsLength = reader.getXMLAmountLengthByPath(
				"//global_data/main_gear_legs_length"
				);
		
		int numberOfFrontalWheels = Integer.valueOf(
				reader
				.getXMLPropertyByPath(
						"//global_data/number_of_frontal_wheels"
						)
				);
		
		int numberOfRearWheels = Integer.valueOf(
				reader
				.getXMLPropertyByPath(
						"//global_data/number_of_rear_wheels"
						)
				);
		
		//---------------------------------------------------------------
		// FRONTAL WHEEL DATA
		Amount<Length> frontalWheelsHeight = reader.getXMLAmountLengthByPath(
				"//frontal_wheels_data/wheel_height"
				);
		
		Amount<Length> frontalWheelsWidth = reader.getXMLAmountLengthByPath(
				"//frontal_wheels_data/wheel_width"
				);
		
		//---------------------------------------------------------------
		// REAR WING DATA
		Amount<Length> rearWheelsHeight = reader.getXMLAmountLengthByPath(
				"//rear_wheels_data/wheel_height"
				);
		
		Amount<Length> rearWheelsWidth = reader.getXMLAmountLengthByPath(
				"//rear_wheels_data/wheel_width"
				);
		
		//---------------------------------------------------------------
		// REFERENCE MASS
		Amount<Mass> massReference = Amount
				.valueOf(
						Double.valueOf(
								reader.getXMLPropertyByPath(
										"//reference_masses/overall_reference_mass"
										)
								),
						SI.KILOGRAM
						);
		
		LandingGears landingGears = new LandingGearsBuilder(id)
				.mainLegsLength(mainGearLegsLength)
				.numberOfFrontalWheels(numberOfFrontalWheels)
				.numberOfRearWheels(numberOfRearWheels)
				.frontalWheelsHeight(frontalWheelsHeight)
				.frontalWheelsWidth(frontalWheelsWidth)
				.rearWheelsHeight(rearWheelsHeight)
				.rearWheelsWidth(rearWheelsWidth)
				.referenceMass(massReference)
				.build();
		
		return landingGears;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tLanding gears\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\t·····································\n")
				.append("\tMain gear legs length: " + _mainLegsLenght + "\n")
				.append("\tNumber of frontal wheels: " + _numberOfFrontalWheels + "\n")
				.append("\tNumber of rear wheels: " + _numberOfRearWheels + "\n")
				.append("\t·····································\n")
				.append("\tFrontal wheels height: " + _frontalWheelsHeight + "\n")
				.append("\tFrontal wheels width: " + _frontalWheelsWidth + "\n")
				.append("\t·····································\n")
				.append("\tRear wheels height: " + _rearWheelsHeight + "\n")
				.append("\tRear wheels width: " + _rearWheelsWidth + "\n")
				.append("\t·····································\n")
				.append("\tOverall reference mass: " + _referenceMass + "\n")
				.append("\t·····································\n")
				;
		
		return sb.toString();
		
	}
	
	@Override
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions) {
		calculateMass(aircraft, conditions, MethodEnum.ROSKAM);
		calculateMass(aircraft, conditions, MethodEnum.STANFORD);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1982);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
	}

	@Override
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions, MethodEnum method) {
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
					62.21 * Math.pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).times(1e-3).getEstimatedValue(), 0.84),
					NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case STANFORD : {
			_methodsList.add(method);
			_overallMass = aircraft.getTheWeights().get_MTOM().times(0.04);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1982 : {
			_methodsList.add(method);
			_mainMass = Amount.valueOf(40 + 0.16 * 
					Math.pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.75) + 
					0.019 * aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue() + 
					1.5 * 1e-5 * Math.pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 1.5),
					NonSI.POUND).to(SI.KILOGRAM);
			_noseMass = Amount.valueOf(20 + 0.1 * 
					Math.pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.75) + 
					2 * 1e-5 * 
					Math.pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 1.5),
					NonSI.POUND).to(SI.KILOGRAM);

			_overallMass = _noseMass.plus(_mainMass);
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_2013 : {
			_methodsList.add(method);
			_overallMass = aircraft.getTheWeights().get_MTOM().times(0.025).
					plus(aircraft.getTheWeights().get_MLM().times(0.016));
			_massMap.put(method, Amount.valueOf(round(_overallMass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case RAYMER : {
			//TODO
		} break;
		default : {} break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_estimatedMass = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_referenceMass, 
				_massMap,
				_percentDifference,
				25.).getFilteredMean(), SI.KILOGRAM);

	}

	@Override
	public void calculateCG(Aircraft aircraft, OperatingConditions conditions) {
		calculateCG(aircraft, conditions, MethodEnum.SFORZA);
	}
	
	/** 
	 * Overload of the calculate CG method that evaluate the landing gear CG location in LRF.
	 */
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum method) {

		 // THESE VALUES WILL COME FROM THE AIRCRAFT CLASS
		_cg.setLRForigin(_xApexConstructionAxes,
						 _yApexConstructionAxes,
						 _zApexConstructionAxes
						 );
		_cg.set_xLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		// page 359 Sforza (2014) - Aircraft Design
		
		//TODO change this
		case SFORZA : { 
			_methodsList.add(method);
			_xCG = Amount.valueOf(0.0, SI.METER);		
			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}

	@Override
	public Amount<Mass> getMass() {
		return _overallMass;
	}

	@Override
	public void setMass(Amount<Mass> mass) {
		this._overallMass = mass;
	}

	@Override
	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	@Override
	public void setMassMap(Map<MethodEnum, Amount<Mass>> massMap) {
		this._massMap = massMap;
	}

	@Override
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	@Override
	public void setMethodsMap(
			Map<AnalysisTypeEnum, List<MethodEnum>> methodsMap) {
		this._methodsMap = methodsMap;
	}

	@Override
	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	@Override
	public void setPercentDifference(Double[] percentDifference) {
		this._percentDifference = percentDifference;
	}

	@Override
	public Amount<Mass> getMassEstimated() {
		return _estimatedMass;
	}

	@Override
	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	@Override
	public void setXCGMap(Map<MethodEnum, Amount<Length>> xCGMap) {
		this._xCGMap = xCGMap;
	}

	@Override
	public CenterOfGravity getCg() {
		return _cg;
	}

	@Override
	public void setCg(CenterOfGravity cg) {
		this._cg = cg;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public void setId (String id) {
		this._id = id;
	}
	
	@Override
	public Amount<Length> getMainLegsLenght() {
		return _mainLegsLenght;
	}

	@Override
	public void setMainLegsLenght(Amount<Length> lenght) {
		this._mainLegsLenght = lenght;
	}
	
	@Override
	public int getNumberOfFrontalWheels() {
		return _numberOfFrontalWheels;
	}

	@Override
	public int getNumberOfRearWheels() {
		return _numberOfRearWheels;
	}

	@Override
	public Amount<Length> getFrontalWheelsHeight() {
		return _frontalWheelsHeight;
	}

	@Override
	public Amount<Length> getFrontalWheelsWidth() {
		return _frontalWheelsWidth;
	}

	@Override
	public Amount<Length> getRearWheelsHeight() {
		return _rearWheelsHeight;
	}

	@Override
	public Amount<Length> getRearWheelsWidth() {
		return _rearWheelsWidth;
	}

	@Override
	public void setNumberOfFrontalWheels(int _numberOfFrontalWheels) {
		this._numberOfFrontalWheels = _numberOfFrontalWheels;
	}

	@Override
	public void setNumberOfRearWheels(int _numberOfRearWheels) {
		this._numberOfRearWheels = _numberOfRearWheels;
	}

	@Override
	public void setFrontalWheelsHeight(Amount<Length> _frontalWheelsHeight) {
		this._frontalWheelsHeight = _frontalWheelsHeight;
	}

	@Override
	public void setFrontalWheelsWidth(Amount<Length> _frontalWheelsWidth) {
		this._frontalWheelsWidth = _frontalWheelsWidth;
	}

	@Override
	public void setRearWheelsHeight(Amount<Length> _rearWheelsHeight) {
		this._rearWheelsHeight = _rearWheelsHeight;
	}

	@Override
	public void setRearWheelsWidth(Amount<Length> _rearWheelsWidth) {
		this._rearWheelsWidth = _rearWheelsWidth;
	}
	
	@Override
	public Amount<Mass> getReferenceMass() {
		return _referenceMass;
	}

	@Override
	public void setReferenceMass(Amount<Mass> referenceMass) {
		this._referenceMass = referenceMass;
	}
	
	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	};
	
	@Override
	public void setXApexConstructionAxes (Amount<Length> xApexConstructionAxes) {
		this._xApexConstructionAxes = xApexConstructionAxes;
	};
	
	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	};
	
	@Override
	public void setYApexConstructionAxes (Amount<Length> yApexConstructionAxes) {
		this._yApexConstructionAxes = yApexConstructionAxes; 
	};
	
	@Override 
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	};
	
	@Override
	public void setZApexConstructionAxes (Amount<Length> zApexConstructionAxes) {
		this._zApexConstructionAxes = zApexConstructionAxes;
	};
}
package aircraft.components.powerPlant;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

public class Engine implements IEngine {

	private String _id;
	private EngineTypeEnum _engineType;
	
	private EngineMountingPositionEnum _mountingPoint;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Angle> _tiltingAngle;
	private Amount<Length> _length;
	
	//------------------------------------------
	// only for propeller driven engines
	private Amount<Length> _propellerDiameter;
	private int _numberOfBlades;
	//------------------------------------------
	
	private int _numberOfCompressorStages, _numberOfShafts; 
	private double _overallPressureRatio;
	
	private Double _bpr;
	private Amount<Power> _p0;
	private Amount<Force> _t0;
	private Amount<Mass> _dryMassPublicDomain; 
	private Amount<Mass> _totalMass; // 1.5*dryMass (ref: Aircraft design - Kundu (pag.245) 

	private EngineWeightsManager _theWeights;
	private EngineBalanceManager _theBalance;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class EngineBuilder {

		// required parameters
		private String __id;
		private EngineTypeEnum __engineType;
		
		// optional parameters ... defaults
		// ...
		private EngineMountingPositionEnum __mountingPoint;
		private Amount<Length> __xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
		private Amount<Length> __yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
		private Amount<Length> __zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
		private Amount<Angle> __tiltingAngle;
		
		private Amount<Length> __length = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __propellerDiameter = Amount.valueOf(0.0, SI.METER);
		private int __numberOfBlades = 0;
		private Double __bpr = 0.0;
		private Amount<Power> __p0 = Amount.valueOf(0.0, SI.WATT);
		private Amount<Force> __t0 = Amount.valueOf(0.0, SI.NEWTON);
		private Amount<Mass> __dryMassPublicDomain = Amount.valueOf(0.0, SI.KILOGRAM);
		private int __numberOfCompressorStages = 0;
		private int __numberOfShafts = 0; 
		private double __overallPressureRatio = 0.0;
		
		public EngineBuilder id (String id) {
			this.__id = id;
			return this;
		}
		
		public EngineBuilder type (EngineTypeEnum type) {
			this.__engineType = type;
			return this;
		}
		
		public EngineBuilder length (Amount<Length> length) {
			this.__length = length;
			return this;
		}
		
		public EngineBuilder propellerDiameter (Amount<Length> propellerDiameter) {
			this.__propellerDiameter = propellerDiameter;
			return this;
		}
		
		public EngineBuilder numberOfBlades (int nBlades) {
			this.__numberOfBlades = nBlades;
			return this;
		}
		
		public EngineBuilder bpr (Double bpr) {
			this.__bpr = bpr;
			return this;
		}
		
		public EngineBuilder p0 (Amount<Power> p0) {
			this.__p0 = p0;
			return this;
		}
		
		public EngineBuilder t0 (Amount<Force> t0) {
			this.__t0 = t0;
			return this;
		}
		
		public EngineBuilder dryMass (Amount<Mass> dryMass) {
			this.__dryMassPublicDomain = dryMass;
			return this;
		}
		
		public EngineBuilder numberOfCompressorStages (int numberOfCompressorStages) {
			this.__numberOfCompressorStages = numberOfCompressorStages;
			return this;
		}
		
		public EngineBuilder numberOfShafts (int numberOfShafts) {
			this.__numberOfShafts = numberOfShafts;
			return this;
		}
		
		public EngineBuilder overallPressureRatio (double overallPressureRatio) {
			this.__overallPressureRatio = overallPressureRatio;
			return this;
		}
		
		public EngineBuilder mountingPoint (EngineMountingPositionEnum mountingPoint) {
			this.__mountingPoint = mountingPoint;
			return this;
		}
		
		public EngineBuilder xApexConstructionAxes (Amount<Length> xApex) {
			this.__xApexConstructionAxes = xApex;
			return this;
		}
		
		public EngineBuilder yApexConstructionAxes (Amount<Length> yApex) {
			this.__yApexConstructionAxes = yApex;
			return this;
		}
		
		public EngineBuilder zApexConstructionAxes (Amount<Length> zApex) {
			this.__zApexConstructionAxes = zApex;
			return this;
		}
		
		public EngineBuilder riggingAngle (Amount<Angle> muT) {
			this.__tiltingAngle = muT;
			return this;
		}
		
		public EngineBuilder (String id, EngineTypeEnum engineType) {
			this.__id = id;
			this.__engineType = engineType;
		}
		
		public EngineBuilder (String id, EngineTypeEnum engineType, AircraftEnum aircraftName) {
			this.__id = id;
			this.__engineType = engineType;
			this.initializeDefaultVariables(aircraftName);
		}
		
		/**
		 * Method that recognize aircraft name and initialize the correct engine.
		 * 
		 * @author Vittorio Trifari
		 */
		private void initializeDefaultVariables (AircraftEnum aircraftName) {
			
			switch(aircraftName) {
			
			case ATR72:
				// PW127 Data
				__engineType = EngineTypeEnum.TURBOPROP;
				__length = Amount.valueOf(2.13, SI.METER);
				__propellerDiameter = Amount.valueOf(3.93, SI.METER);
				__numberOfBlades = 6;
				__dryMassPublicDomain = Amount.valueOf(1064., NonSI.POUND).to(SI.KILOGRAM);
				__p0 = Amount.valueOf(2750., NonSI.HORSEPOWER).to(SI.WATT);
				__numberOfCompressorStages = 5;
				__numberOfShafts = 2;
				__overallPressureRatio = 15.;
				
				break;
				
			case B747_100B:
				// PWJT9D-7 Data
				__engineType = EngineTypeEnum.TURBOFAN;
				__length = Amount.valueOf(3.26, SI.METER);
				__bpr = 5.0;
				__dryMassPublicDomain = Amount.valueOf(3905.0, NonSI.POUND).to(SI.KILOGRAM);
				__t0 = Amount.valueOf(204000.0000, SI.NEWTON);
				__numberOfCompressorStages = 14;
				__numberOfShafts = 2;
				__overallPressureRatio = 23.4;
				
				break;
				
			case AGILE_DC1:
				//PW1700G
				__engineType = EngineTypeEnum.TURBOFAN;
				__length = Amount.valueOf(2.739, SI.METER);
				__bpr = 6.0;				
				__dryMassPublicDomain = Amount.valueOf(1162.6, NonSI.POUND).to(SI.KILOGRAM);
				__t0 = Amount.valueOf(7000*AtmosphereCalc.g0.getEstimatedValue(), SI.NEWTON);
				__numberOfCompressorStages = 5; // TODO: CHECK
				__numberOfShafts = 2;// TODO: CHECK
				__overallPressureRatio = 15.;// TODO: CHECK
				
				break;
			}
		}
		
		public Engine build() {
			return new Engine(this);
		}
	}
	
	private Engine (EngineBuilder builder) {
		
		this._id = builder.__id;
		this._engineType = builder.__engineType;
		this._length = builder.__length;
		this._propellerDiameter = builder.__propellerDiameter;
		this._numberOfBlades = builder.__numberOfBlades;
		this._bpr = builder.__bpr;
		this._p0 = builder.__p0;
		this._t0 = builder.__t0;
		this._dryMassPublicDomain = builder.__dryMassPublicDomain;
		this._totalMass = this._dryMassPublicDomain.times(1.5);
		this._numberOfCompressorStages = builder.__numberOfCompressorStages;
		this._numberOfShafts = builder.__numberOfShafts;
		this._overallPressureRatio = builder.__overallPressureRatio;
		this._mountingPoint = builder.__mountingPoint;
		this._xApexConstructionAxes = builder.__xApexConstructionAxes;
		this._yApexConstructionAxes = builder.__yApexConstructionAxes;
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._tiltingAngle = builder.__tiltingAngle;
		
		if((this._engineType == EngineTypeEnum.TURBOPROP)
				|| (this._engineType == EngineTypeEnum.PISTON)) {
			calculateT0FromP0();
		}
		
		this._theWeights = new EngineWeightsManager(this);
		this._theBalance = new EngineBalanceManager(this);
	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	public static Engine importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading systems data ...");
		
		Engine theEngine = null;
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		String typeProperty = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@type");
		EngineTypeEnum engineType = null;
		if(typeProperty.equalsIgnoreCase("TURBOJET"))
			engineType = EngineTypeEnum.TURBOJET;
		else if(typeProperty.equalsIgnoreCase("TURBOFAN"))
			engineType = EngineTypeEnum.TURBOFAN;
		else if(typeProperty.equalsIgnoreCase("PISTON"))
			engineType = EngineTypeEnum.PISTON;
		else if(typeProperty.equalsIgnoreCase("TURBOPROP"))
			engineType = EngineTypeEnum.TURBOPROP;
		else {
			System.err.println("\tINVALID ENGINE TYPE!!");
			return null;
		}
		
		// TODO : CHECK IF OTHER ENGINE TYPES ARE NECESSARIES
		
		if((engineType == EngineTypeEnum.TURBOJET)||(engineType == EngineTypeEnum.TURBOFAN)) {
			
			Amount<Length> length = reader.getXMLAmountLengthByPath("//dimensions/length");
			
			@SuppressWarnings("unchecked")
			Amount<Force> staticThrust = ((Amount<Force>) reader.getXMLAmountWithUnitByPath("//specifications/static_thrust"));
			@SuppressWarnings("unchecked")
			Amount<Mass> dryMass = ((Amount<Mass>) reader.getXMLAmountWithUnitByPath("//specifications/dry_mass")); 
			Double bpr = Double.valueOf(reader.getXMLPropertyByPath("//specifications/by_pass_ratio"));
			Integer numberOfCompressorStages = Integer.valueOf(reader.getXMLPropertyByPath("//specifications/number_of_compressor_stages"));
			Integer numberOfShafts = Integer.valueOf(reader.getXMLPropertyByPath("//specifications/number_of_shafts"));
			Double overallPressureRatio = Double.valueOf(reader.getXMLPropertyByPath("//specifications/overall_pressure_ratio"));
			
			theEngine = new EngineBuilder(id, engineType)
					.id(id)
					.length(length)
					.type(engineType)
					.t0(staticThrust)
					.bpr(bpr)
					.dryMass(dryMass)
					.numberOfCompressorStages(numberOfCompressorStages)
					.numberOfShafts(numberOfShafts)
					.overallPressureRatio(overallPressureRatio)
					.build();
			
		}
		else if(engineType == EngineTypeEnum.TURBOPROP) {

			Amount<Length> length = reader.getXMLAmountLengthByPath("//dimensions/length");
			Amount<Length> propellerDiameter = reader.getXMLAmountLengthByPath("//dimensions/propeller_diameter");
			
			@SuppressWarnings("unchecked")
			Amount<Power> staticPower = ((Amount<Power>) reader.getXMLAmountWithUnitByPath("//specifications/static_power"));
			@SuppressWarnings("unchecked")
			Amount<Mass> dryMass = ((Amount<Mass>) reader.getXMLAmountWithUnitByPath("//specifications/dry_mass")); 
			int numberOfPropellerBlades = Integer.valueOf(reader.getXMLPropertyByPath("//specifications/number_of_propeller_blades"));
			Integer numberOfCompressorStages = Integer.valueOf(reader.getXMLPropertyByPath("//specifications/number_of_compressor_stages"));
			Integer numberOfShafts = Integer.valueOf(reader.getXMLPropertyByPath("//specifications/number_of_shafts"));
			Double overallPressureRatio = Double.valueOf(reader.getXMLPropertyByPath("//specifications/overall_pressure_ratio"));
			
			theEngine = new EngineBuilder(id, engineType)
					.id(id)
					.type(engineType)
					.length(length)
					.propellerDiameter(propellerDiameter)
					.numberOfBlades(numberOfPropellerBlades)
					.p0(staticPower)
					.dryMass(dryMass)
					.numberOfCompressorStages(numberOfCompressorStages)
					.numberOfShafts(numberOfShafts)
					.overallPressureRatio(overallPressureRatio)
					.build();
		}
		else if(engineType == EngineTypeEnum.PISTON) {

			Amount<Length> length = reader.getXMLAmountLengthByPath("//dimensions/length");
			Amount<Length> propellerDiameter = reader.getXMLAmountLengthByPath("//dimensions/propeller_diameter");
			
			@SuppressWarnings("unchecked")
			Amount<Power> staticPower = ((Amount<Power>) reader.getXMLAmountWithUnitByPath("//specifications/static_power"));
			@SuppressWarnings("unchecked")
			Amount<Mass> dryMass = ((Amount<Mass>) reader.getXMLAmountWithUnitByPath("//specifications/dry_mass")); 
			int numberOfPropellerBlades = Integer.valueOf(reader.getXMLPropertyByPath("//specifications/number_of_propeller_blades"));
			
			theEngine = new EngineBuilder(id, engineType)
					.id(id)
					.type(engineType)
					.length(length)
					.propellerDiameter(propellerDiameter)
					.numberOfBlades(numberOfPropellerBlades)
					.p0(staticPower)
					.dryMass(dryMass)
					.build();
		}
		
		return theEngine;
	}
	
	/*************************************************
	 * This method applies the momentum theory 
	 * and Bernoulli’s equation for incompressible flow
	 * to the flow through the propeller in order to
	 * estimate the static thrust from the static power.
	 * 
	 * p0^(2/3)*(2*rhoSL*A)
	 * 
	 * with 
	 * 
	 * p0 in (lbf*ft/s) = 550*hp
	 * rhoSL in (slugs/ft^3) = 0.00237717
	 * A in ft^2
	 * 
	 * 
	 * The actual thrust would be lower since the momentum theory approach
	 * does not include any propellor blade drag or any losses at the tips
	 * of the propellor blades (like a 3-D wing).
	 *  
	 * We can estimate the actual static thrust is 95% of one calculated before
	 *  
	 * @see: thrustmodelsToFromP0.pdf in JPAD DOCS
	 */
	private void calculateT0FromP0 () {
		
		// this is the maximal static thrust 
		this._t0 = Amount.valueOf(
				Math.pow((this._p0.doubleValue(NonSI.HORSEPOWER)*550),(0.6666667))
				*2
				*0.00237717
				*Math.pow(this._propellerDiameter.doubleValue(NonSI.FOOT),2)
					*Math.PI/4,
				NonSI.POUND_FORCE);
		this._t0 = this._t0.times(0.95).to(SI.NEWTON);
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\tID: '" + _id + "'\n")
				.append("\tType: " + _engineType + "\n")
				.append("\t·····································\n")
				.append("\tLength: " + _length + "\n")
				;
		if((_engineType == EngineTypeEnum.TURBOFAN) || (_engineType == EngineTypeEnum.TURBOJET))
			sb.append("\tNumber of compressor stages: " + _numberOfCompressorStages + "\n")
			.append("\tNumber of shafts: " + _numberOfShafts + "\n")
			.append("\tOverall pressure ratio: " + _overallPressureRatio + "\n")
			;
		else if(_engineType == EngineTypeEnum.TURBOPROP) 
			sb.append("\tPropeller diameter: " + _propellerDiameter + "\n")
			.append("\tNumber of blades: " + _numberOfBlades + "\n")
			.append("\tNumber of compressor stages: " + _numberOfCompressorStages + "\n")
			.append("\tNumber of shafts: " + _numberOfShafts + "\n")
			.append("\tOverall pressure ratio: " + _overallPressureRatio + "\n")
			;
		else if(_engineType == EngineTypeEnum.PISTON) 
			sb.append("\tPropeller diameter: " + _propellerDiameter + "\n")
			.append("\tNumber of blades: " + _numberOfBlades + "\n")
			;
		
		sb.append("\t·····································\n");
		
		if((_engineType == EngineTypeEnum.TURBOFAN) || (_engineType == EngineTypeEnum.TURBOJET))
			sb.append("\tT0: " + _t0 + "\n")
			.append("\tBPR: " + _bpr + "\n")
			;
		else if((_engineType == EngineTypeEnum.PISTON) || (_engineType == EngineTypeEnum.TURBOPROP))
			sb.append("\tP0: " + _p0.to(NonSI.HORSEPOWER) + "\n")
			.append("\tT0: " + _t0.to(NonSI.POUND_FORCE) + "\n")
			;
		
		sb.append("\t·····································\n");
		
		sb.append("\tDry mass public domain: " + _dryMassPublicDomain + "\n")
		.append("\t·····································\n");
		;
		
		return sb.toString();
	}
	
	@Override
	public String getId() {
		return _id;
	}
	
	@Override
	public void setId(String id) {
		this._id = id;
	}

	@Override
	public EngineTypeEnum getEngineType() {
		return _engineType;
	}
	
	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	@Override
	public void setXApexConstructionAxes(Amount<Length> _X0) {
		this._xApexConstructionAxes = _X0;
	}

	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	@Override
	public void setYApexConstructionAxes(Amount<Length> _Y0) {
		this._yApexConstructionAxes = _Y0;
	}

	@Override
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	@Override
	public void setZApexConstructionAxes(Amount<Length> _Z0) {
		this._zApexConstructionAxes = _Z0;
	}

	@Override
	public Amount<Angle> getTiltingAngle() {
		return _tiltingAngle;
	}

	@Override
	public void setTiltingAngle(Amount<Angle> _muT) {
		this._tiltingAngle = _muT;
	}
	
	@Override
	public void setEngineType(EngineTypeEnum _engineType) {
		this._engineType = _engineType;
	}

	@Override
	public EngineMountingPositionEnum getMountingPosition() {
		return _mountingPoint;
	}

	@Override
	public void setMountingPosition(EngineMountingPositionEnum _position) {
		this._mountingPoint = _position;
	}
	
	@Override
	public Amount<Length> getLength() {
		return _length;
	}

	@Override
	public void setLength(Amount<Length> _length) {
		this._length = _length;
	}

	@Override
	public Amount<Power> getP0() {
		return _p0;
	}

	@Override
	public void setP0(Amount<Power> _p0) {
		this._p0 = _p0;
	}

	@Override
	public Amount<Force> getT0() {
		return _t0;
	}

	@Override
	public void setT0(Amount<Force> _t0) {
		this._t0 = _t0;
	}
	
	@Override
	public Double getBPR() {
		return _bpr;
	}

	@Override
	public void setBPR(Double _BPR) {
		this._bpr = _BPR;
	}

	@Override
	public Amount<Mass> getDryMassPublicDomain() {
		return _dryMassPublicDomain;
	}

	@Override
	public void setDryMassPublicDomain (Amount<Mass> dryMassPublicDomain) {
		this._dryMassPublicDomain = dryMassPublicDomain;
	}
	
	@Override
	public Amount<Mass> getTotalMass() {
		return _totalMass;
	}

	@Override
	public void setTotalMass(Amount<Mass> _totalMass) {
		this._totalMass = _totalMass;
	}

	@Override
	public Amount<Length> getPropellerDiameter() {
		return _propellerDiameter;
	}

	@Override
	public void setPropellerDiameter(Amount<Length> _propellerDiameter) {
		this._propellerDiameter = _propellerDiameter;
	}
	
	@Override
	public int getNumberOfBlades() {
		return _numberOfBlades;
	}

	@Override
	public void setNumberOfBlades(int _nBlades) {
		this._numberOfBlades = _nBlades;
	}

	@Override
	public int getNumberOfCompressorStages() {
		return _numberOfCompressorStages;
	}

	@Override
	public void setNumberOfCompressorStages(int _numberOfCompressorStages) {
		this._numberOfCompressorStages = _numberOfCompressorStages;
	}

	@Override
	public int getNumberOfShafts() {
		return _numberOfShafts;
	}

	@Override
	public void setNumberOfShafts(int _numberOfShafts) {
		this._numberOfShafts = _numberOfShafts;
	}
 
	@Override
	public double getOverallPressureRatio() {
		return _overallPressureRatio;
	}

	@Override
	public void setOverallPressureRatio(double _overallPressureRatio) {
		this._overallPressureRatio = _overallPressureRatio;
	}

	@Override
	public EngineWeightsManager getTheWeights() {
		return _theWeights;
	}

	@Override
	public void setTheWeights(EngineWeightsManager _theWeights) {
		this._theWeights = _theWeights;
	}

	@Override
	public EngineBalanceManager getTheBalance() {
		return _theBalance;
	}

	@Override
	public void setTheBalance(EngineBalanceManager _theBalance) {
		this._theBalance = _theBalance;
	}

}

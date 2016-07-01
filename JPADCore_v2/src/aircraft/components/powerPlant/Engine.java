package aircraft.components.powerPlant;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

public class Engine implements IEngine {

	private String _id;
	private EngineTypeEnum _engineType;
	
	private Amount<Length> _length;

	//-----------------------------------------
	// only for jet engines
	private Amount<Length> _diameter;
	//-----------------------------------------
	// only for propeller engines
	private Amount<Length> _width;
	private Amount<Length> _height;
	private Amount<Length> _propellerDiameter;
	private int _numberOfBlades;
	//------------------------------------------
	
	private Double _bpr;
	private Amount<Power> _p0;
	private Amount<Force> _t0;
	private Amount<Mass> _dryMassPublicDomain; 

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class EngineBuilder {

		// required parameters
		private String __id;
		private EngineTypeEnum __engineType;
		
		// optional parameters ... defaults
		// ...
		private Amount<Length> __length = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __diameter = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __width = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __height = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __propellerDiameter = Amount.valueOf(0.0, SI.METER);
		private int __numberOfBlades = 0;
		private Double __bpr = 0.0;
		private Amount<Power> __p0 = Amount.valueOf(0.0, SI.WATT);
		private Amount<Force> __t0 = Amount.valueOf(0.0, SI.NEWTON);
		private Amount<Mass> __dryMassPublicDomain = Amount.valueOf(0.0, SI.KILOGRAM);
		
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
		
		public EngineBuilder diameter (Amount<Length> diameter) {
			this.__diameter = diameter;
			return this;
		}
		
		public EngineBuilder width (Amount<Length> width) {
			this.__width = width;
			return this;
		}
		
		public EngineBuilder height (Amount<Length> height) {
			this.__height = height;
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
				__length = Amount.valueOf(2.413, SI.METER);
				__width = Amount.valueOf(0.762, SI.METER); 
				__height = Amount.valueOf(1.1176, SI.METER);
				__propellerDiameter = Amount.valueOf(3.93, SI.METER);
				__numberOfBlades = 6;
				__dryMassPublicDomain = Amount.valueOf(1064., NonSI.POUND).to(SI.KILOGRAM);
				__p0 = Amount.valueOf(2750., NonSI.HORSEPOWER).to(SI.WATT);

				break;
				
			case B747_100B:
				// PWJT9D-7 Data
				__engineType = EngineTypeEnum.TURBOFAN;
				__length = Amount.valueOf(3.26, SI.METER);
				__diameter = Amount.valueOf(2.34, SI.METER);
				__bpr = 5.0;
				__dryMassPublicDomain = Amount.valueOf(3905.0, NonSI.POUND).to(SI.KILOGRAM);
				__t0 = Amount.valueOf(204000.0000, SI.NEWTON);
				
				break;
				
			case AGILE_DC1:
				//PW1700G
				__engineType = EngineTypeEnum.TURBOFAN;
				__length = Amount.valueOf(2.739, SI.METER);
				__diameter = Amount.valueOf(1.442, SI.METER);
				__bpr = 6.0;				
				__dryMassPublicDomain = Amount.valueOf(1162.6, NonSI.POUND).to(SI.KILOGRAM);
				__t0 = Amount.valueOf(7000*AtmosphereCalc.g0.getEstimatedValue(), SI.NEWTON);
				
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
		this._diameter = builder.__diameter;
		this._width = builder.__width;
		this._height = builder.__height;
		this._propellerDiameter = builder.__propellerDiameter;
		this._numberOfBlades = builder.__numberOfBlades;
		this._bpr = builder.__bpr;
		this._p0 = builder.__p0;
		this._t0 = builder.__t0;
		this._dryMassPublicDomain = builder.__dryMassPublicDomain;
		
		if((this._engineType == EngineTypeEnum.TURBOPROP)
				|| (this._engineType == EngineTypeEnum.PISTON)) {
			calculateT0FromP0();
		}
	}
	
	//===================================================================================================
	// End of builder pattern
	//===================================================================================================
	
	//-------------------------------
	// TODO : IMPORT FROM XML
	//-------------------------------
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
			Amount<Length> diameter = reader.getXMLAmountLengthByPath("//dimensions/diameter");
			
			@SuppressWarnings("unchecked")
			Amount<Force> staticThrust = ((Amount<Force>) reader.getXMLAmountWithUnitByPath("//specifications/static_thrust"));
			@SuppressWarnings("unchecked")
			Amount<Mass> dryMass = ((Amount<Mass>) reader.getXMLAmountWithUnitByPath("//specifications/dry_mass")); 
			Double bpr = Double.valueOf(reader.getXMLPropertyByPath("//specifications/by_pass_ratio"));
			
			theEngine = new EngineBuilder(id, engineType)
					.id(id)
					.type(engineType)
					.length(length)
					.diameter(diameter)
					.t0(staticThrust)
					.bpr(bpr)
					.dryMass(dryMass)
					.build();
			
		}
		else if((engineType == EngineTypeEnum.PISTON)||(engineType == EngineTypeEnum.TURBOPROP)) {
		
			Amount<Length> length = reader.getXMLAmountLengthByPath("//dimensions/length");
			Amount<Length> width = reader.getXMLAmountLengthByPath("//dimensions/width");
			Amount<Length> height = reader.getXMLAmountLengthByPath("//dimensions/height");
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
					.width(width)
					.height(height)
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
				.append("\t-------------------------------------\n")
				.append("\tEngine\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _id + "'\n")
				.append("\tType: " + _engineType + "\n")
				.append("\t·····································\n")
				.append("\tLength: " + _length + "\n")
				;
		if((_engineType == EngineTypeEnum.TURBOFAN) || (_engineType == EngineTypeEnum.TURBOJET))
			sb.append("\tDiameter: " + _diameter + "\n");
		else if((_engineType == EngineTypeEnum.PISTON) || (_engineType == EngineTypeEnum.TURBOPROP)) 
			sb.append("\tWidth: " + _width + "\n")
			.append("\tHeight: " + _height + "\n")
			.append("\tPropeller diameter: " + _propellerDiameter + "\n")
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
	public void setEngineType(EngineTypeEnum _engineType) {
		this._engineType = _engineType;
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
	public Amount<Length> getLength() {
		return _length;
	}

	@Override
	public void setLength(Amount<Length> _length) {
		this._length = _length;
	}
	
	@Override
	public Amount<Length> getDiameter() {
		return _diameter;
	}

	@Override
	public void setDiameter(Amount<Length> _diameter) {
		this._diameter = _diameter;
	}
	
	@Override
	public Amount<Length> getWidth() {
		return _width;
	}

	@Override
	public void setWidth(Amount<Length> _width) {
		this._width = _width;
	}
	
	@Override
	public Amount<Length> getHeight() {
		return _height;
	}

	@Override
	public void setHeight(Amount<Length> _height) {
		this._height = _height;
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

}

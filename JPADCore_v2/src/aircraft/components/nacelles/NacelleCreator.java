package aircraft.components.nacelles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.powerplant.Engine;
import analyses.nacelles.NacelleAerodynamicsManager;
import analyses.nacelles.NacelleBalanceManager;
import analyses.nacelles.NacelleWeightsManager;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.EngineTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyInterpolatingFunction;
import standaloneutils.MyXMLReaderUtils;

/** 
 * The Nacelle is defined by a length and three diameters (inlet, mean, outlet).
 * The user can assign length, diameter mean (max) and two coefficients (K_inlet, K_outlet) which 
 * define the inlet and outlet diameters as a percentage of the maximum one.
 * Otherwise, if the user doesn't assign these data, the maximum diameter is calculated from a 
 * statistical chart as function of T0 or, in case of propeller driven engines, as the equivalent
 * diameter obtained from maximum height and with calculated as function of the shaft horse-power.
 * Concerning K_inlet and K_outlet, these data are initialized with default values in 
 * case the user doesn't assign them.
 * The user can also assign the position from the Nacelle apex of the maximum diameter as a percentage
 * of the Nacelle length (this through the variable kLenght). Moreover he can assign the z position
 * of the outlet diameter as a percentage of the outlet diameter (through the variable kDiameterOutlet).
 *  
 * @author Vittorio Trifari, Vincenzo Cusati
 *
 */
public class NacelleCreator implements INacelleCreator {

	public enum MountingPosition {
		WING,
		FUSELAGE,
		HTAIL,
		UNDERCARRIAGE_HOUSING
	}

	private String _id;
	private MountingPosition _mountingPosition;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private Amount<Length> _roughness;
	
	private Amount<Length> _length;
	private Amount<Length> _diameterInlet;
	private Amount<Length> _diameterMax;
	private Amount<Length> _diameterOutlet;
	private Double _kInlet;
	private Double _kOutlet;
	private Double _kLength;
	private Double _kDiameterOutlet;
	private Amount<Length> _xPositionMaximumDiameterLRF;
	private Amount<Length> _zPositionOutletDiameterLRF;
	
	// outlines
	private double[] _xCoordinatesOutlineDouble; 
	private double[] _zCoordinatesOutlineXZUpperDouble;
	private double[] _zCoordinatesOutlineXZLowerDouble;
	private double[] _yCoordinatesOutlineXYRightDouble;
	private double[] _yCoordinatesOutlineXYLeftDouble;
	private List<Amount<Length>> _xCoordinatesOutline;
	private List<Amount<Length>> _zCoordinatesOutlineXZUpper;
	private List<Amount<Length>> _zCoordinatesOutlineXZLower;
	private List<Amount<Length>> _yCoordinatesOutlineXYRight;
	private List<Amount<Length>> _yCoordinatesOutlineXYLeft;
	
	private Amount<Area> _surfaceWetted;
	private Amount<Mass> _massReference;
	
	private Engine _theEngine;
	
	private NacelleWeightsManager _theWeights;
	private NacelleBalanceManager _theBalance;
	private NacelleAerodynamicsManager _theAerodynamics;
	
	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class NacelleCreatorBuilder {
	
		// required parameters
		private String __id;
		
		// optional parameters ... defaults
		// ...
		
		// initialized to ATR-72 engine
		private Engine __theEngine = new Engine.EngineBuilder("ATR-72", EngineTypeEnum.TURBOPROP, AircraftEnum.ATR72).build();
		
		private Amount<Length> __roughness;
		private Amount<Length> __length = Amount.valueOf(0.0, SI.METER);
		private Amount<Length> __diameterMax = Amount.valueOf(0.0, SI.METER);
		private Double __kInlet = 0.8; // default value
		private Double __kOutlet = 0.2; // default value
		private Double __kLength = 0.35; // default value
		private Double __kDiameterOutlet = 0.0; // default value
		
		public NacelleCreatorBuilder id (String id) {
			this.__id = id;
			return this;
		}
	
		public NacelleCreatorBuilder engine (Engine theEngine) {
			this.__theEngine = theEngine;
			return this;
		}
		
		public NacelleCreatorBuilder roughness (Amount<Length> roughness) {
			this.__roughness = roughness;
			return this;
		}
		
		public NacelleCreatorBuilder lenght (Amount<Length> lenght) {
			this.__length = lenght;
			return this;
		}
		
		public NacelleCreatorBuilder maximumDiameter (Amount<Length> diameterMax) {
			this.__diameterMax = diameterMax;
			return this;
		}
		
		public NacelleCreatorBuilder kInlet (Double kInlet) {
			this.__kInlet = kInlet;
			return this;
		}
		
		public NacelleCreatorBuilder kOutlet (Double kOutlet) {
			this.__kOutlet = kOutlet;
		
			return this;
		}
		
		public NacelleCreatorBuilder kLength (Double kLength) {
			this.__kLength = kLength;
		
			return this;
		}
		
		public NacelleCreatorBuilder kDiameterOutlet (Double kDiameterOutlet) {
			this.__kDiameterOutlet = kDiameterOutlet;
		
			return this;
		}
		
		public NacelleCreatorBuilder(String id) {
			this.__id = id;
		}
		
		public NacelleCreatorBuilder(String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultVariables(aircraftName);
		}
		
		/**
		 * Method that recognize aircraft name and initialize the correct nacelle data.
		 * 
		 * @author Vittorio Trifari
		 */
		@SuppressWarnings("incomplete-switch")
		private void initializeDefaultVariables (AircraftEnum aircraftName) {
			
			switch(aircraftName) {
			
			case ATR72:
				__theEngine = new Engine.EngineBuilder("ATR-72 engine", EngineTypeEnum.TURBOPROP, AircraftEnum.ATR72).build();
				__length = Amount.valueOf(4.371,SI.METER);
				__diameterMax = Amount.valueOf(1.4,SI.METER);
				__kInlet = 0.857;
				__kOutlet = 0.143;
				__kLength = 0.4;
				__kDiameterOutlet = 1.5;
				__roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
				
				break;
				
			case B747_100B:
				__theEngine = new Engine.EngineBuilder("B747-100B engine", EngineTypeEnum.TURBOFAN, AircraftEnum.B747_100B).build();
				__length = Amount.valueOf(7.6,SI.METER);
				__diameterMax = Amount.valueOf(2.0,SI.METER);
				__kInlet = 0.6;
				__kOutlet = 0.1;
				__kLength = 0.35;
				__kDiameterOutlet = 0.0;
				__roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
				
				break;
				
			case AGILE_DC1:
				__theEngine = new Engine.EngineBuilder("AGILE DC-1 engine", EngineTypeEnum.TURBOFAN, AircraftEnum.AGILE_DC1).build();
				__length = Amount.valueOf(3.,SI.METER);
				__diameterMax = Amount.valueOf(1.816,SI.METER);
				__kInlet = 0.847;
				__kOutlet = 0.33;
				__kLength = 0.35;
				__kDiameterOutlet = 0.0;
				__roughness = Amount.valueOf(0.405 * Math.pow(10,-5), SI.METRE);
				
				break;
			}
		}
		
		public NacelleCreator build () {
			return new NacelleCreator(this);
		}
	}
	
	private NacelleCreator (NacelleCreatorBuilder builder) {
		
		this._id = builder.__id;
		this._theEngine = builder.__theEngine;
		this._roughness = builder.__roughness;
		this._length = builder.__length;
		this._diameterMax = builder.__diameterMax;
		this._kInlet = builder.__kInlet;
		this._kOutlet = builder.__kOutlet;
		this._kLength = builder.__kLength;
		this._kDiameterOutlet = builder.__kDiameterOutlet;
		
		if((_length.doubleValue(SI.METER) == 0.0)
				&& (_diameterMax.doubleValue(SI.METER) == 0.0)) {
			estimateDimensions(_theEngine);
		}
		
		this._diameterInlet = this._diameterMax.times(_kInlet);
		this._diameterOutlet = this._diameterMax.times(_kOutlet);
		this._xPositionMaximumDiameterLRF = _length.times(_kLength);
		this._zPositionOutletDiameterLRF = _diameterOutlet.times(_kDiameterOutlet);
		
		calculateGeometry();
	}
	
	//============================================================================================
	// End of builder pattern 
	//============================================================================================
	
	public static NacelleCreator importFromXML (String pathToXML, String engineDirectory) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading systems data ...");
		
		NacelleCreator theNacelle = null;
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		String engineFileName =
				MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@engine");
		
		// default engine. This will be updated with the engine from file (if present)
		Engine engine = new Engine
				.EngineBuilder("ATR-72 engine", EngineTypeEnum.TURBOPROP, AircraftEnum.ATR72)
					.build();
		
		if(engineFileName != null) {
			String enginePath = engineDirectory + File.separator + engineFileName;
			engine = Engine.importFromXML(enginePath);
		}
		
		Amount<Length> roughness = reader.getXMLAmountLengthByPath("//global_data/roughness");
		
		Amount<Length> length = Amount.valueOf(0.0, SI.METER);
		Amount<Length> diameterMax = Amount.valueOf(0.0, SI.METER);
		Double kInlet = 0.8;
		Double kOutlet = 0.2;
		Double kLength = 0.35;
		Double kDiameterOutlet = 0.0;
		
		if(reader.getXMLPropertyByPath("//geometry/length") != null)
			length = reader.getXMLAmountLengthByPath("//geometry/length");
		if(reader.getXMLPropertyByPath("//geometry/maximum_diameter") != null)
			diameterMax = reader.getXMLAmountLengthByPath("//geometry/maximum_diameter");
		if(reader.getXMLPropertyByPath("//geometry/k_inlet") != null)
			kInlet = Double.valueOf(reader.getXMLPropertyByPath("//geometry/k_inlet"));
		if(reader.getXMLPropertyByPath("//geometry/k_outlet") != null)
			kOutlet = Double.valueOf(reader.getXMLPropertyByPath("//geometry/k_outlet"));
		if(reader.getXMLPropertyByPath("//geometry/k_length") != null)
			kLength = Double.valueOf(reader.getXMLPropertyByPath("//geometry/k_length"));
		if(reader.getXMLPropertyByPath("//geometry/k_diameter_outlet") != null)
			kDiameterOutlet = Double.valueOf(reader.getXMLPropertyByPath("//geometry/k_diameter_outlet"));
		
		theNacelle = new NacelleCreatorBuilder(id)
				.engine(engine)
				.roughness(roughness)
				.lenght(length)
				.maximumDiameter(diameterMax)
				.kInlet(kInlet)
				.kOutlet(kOutlet)
				.kLength(kLength)
				.kDiameterOutlet(kDiameterOutlet)
				.build()
				;
		
		return theNacelle;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\tID: '" + _id + "'\n")
				.append("\t·····································\n")
				;
		
		if(_theEngine != null)
			sb.append("\tEngine ID: '" + _theEngine.getId() + "'\n")
			  .append("\tEngine type: " + _theEngine.getEngineType() + "\n")
			  ;
				
		sb.append("\tLength: " + _length + "\n")
		.append("\tDiameter max: " + _diameterMax + "\n")
		.append("\tK_inlet: " + _kInlet + "\n")
		.append("\tK_outlet: " + _kOutlet + "\n")
		.append("\tDiameter inlet: " + _diameterInlet + "\n")
		.append("\tDiameter outlet: " + _diameterOutlet + "\n")
		.append("\tK_length: " + _kLength + "\n")
		.append("\tX position of the max diameter in LRF: " + _xPositionMaximumDiameterLRF + "\n")
		.append("\tK_diameter_outlet: " + _kDiameterOutlet + "\n")
		.append("\tZ position of the outlet diameter in LRF: " + _zPositionOutletDiameterLRF + "\n")
		.append("\t·····································\n")
		.append("\tSurface roughness: " + _roughness + "\n")
		.append("\tSurface wetted: " + _surfaceWetted + "\n")
		.append("\t·····································\n")	
		.append("\tDiscretization\n")
		.append("\tOutline XY - X (m): " + getXCoordinatesOutline().stream().map(x -> x.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
		.append("\tOutline XY Left Top View - Y (m): " + getYCoordinatesOutlineXYLeft().stream().map(y -> y.doubleValue(SI.METER)).collect(Collectors.toList())+ "\n")
		.append("\tOutline XY Right Top View - Y (m): " + getYCoordinatesOutlineXYRight().stream().map(y -> y.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
		.append("\tOutline XZ Upper Side View - Z (m): " + getZCoordinatesOutlineXZUpper().stream().map(z -> z.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
		.append("\tOutline XZ Lower Side View - Z (m): " + getZCoordinatesOutlineXZLower().stream().map(z -> z.doubleValue(SI.METER)).collect(Collectors.toList()) + "\n")
		.append("\t·····································\n");
		;

		return sb.toString();
	}

	/***************************************************************************								
	 * This method estimates the nacelle dimensions in inches as function of 									
	 * the engine type. If is a jet engine it uses the static thrust in lbs; 									
	 * otherwise it uses the shaft-horsepower (hp).
	 * 
	 * @see: Behind ADAS - Nacelle Sizing
	 */
	@Override
	public void estimateDimensions (Engine theEngine) {
				
		Amount<Length> width;
		Amount<Length> height;
		
		if((theEngine.getEngineType() == EngineTypeEnum.TURBOFAN) 
				|| (theEngine.getEngineType() == EngineTypeEnum.TURBOJET)) {
			
			if (theEngine.getT0().doubleValue(NonSI.POUND_FORCE) > 100000) {
				System.err.println("WARNING (DIMENSIONS CALCULATION (JET DRIVEN) - NACELLE) THE STATIC THRUST VALUE IS OVER THE STATISTICAL RANGE ... RETURNING");
				return;
			}
			
			_length = Amount.valueOf(
					40 + (0.59 * Math.sqrt(theEngine.getT0().doubleValue(NonSI.POUND_FORCE))),
					NonSI.INCH)
					.to(SI.METER);
			_diameterMax = Amount.valueOf(
					5 + (0.39 * Math.sqrt(theEngine.getT0().doubleValue(NonSI.POUND_FORCE))),
					NonSI.INCH)
					.to(SI.METER); 
		}
		
		else if(theEngine.getEngineType() == EngineTypeEnum.PISTON) {
			
			if (theEngine.getP0().doubleValue(NonSI.HORSEPOWER) > 1200) {
				System.err.println("WARNING (DIMENSIONS CALCULATION (PISTON) - NACELLE) THE STATIC POWER VALUE IS OVER THE STATISTICAL RANGE ... RETURNING");
				return;
			}	
			
			_length = Amount.valueOf(
					4*10e-10*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),4)
					- 6*10e-7*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
					+ 8*10e-5*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					- 0.2193*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					+ 54.097,
					NonSI.INCH)
					.to(SI.METER);
			
			if(theEngine.getP0().doubleValue(NonSI.HORSEPOWER) <= 410)
				width = Amount.valueOf(
						- 3*10e-7*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
						- 0.0003*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
						+ 0.2196*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
						+ 7.3966,
						NonSI.INCH)
						.to(SI.METER); 
			else 
				width = Amount.valueOf(
						- 4.6563*Math.log(theEngine.getP0().doubleValue(NonSI.HORSEPOWER))
						+ 57.943,
						NonSI.INCH)
						.to(SI.METER);
			
			height = Amount.valueOf(
					12.595*Math.log(theEngine.getP0().doubleValue(NonSI.HORSEPOWER))
					- 43.932,
					NonSI.INCH)
					.to(SI.METER);
			
			_diameterMax = Amount.valueOf(
					Math.sqrt(
							(width.times(height).times(4).divide(Math.PI).getEstimatedValue())
							),
					SI.METER
					);
			}
		
		else if(theEngine.getEngineType() == EngineTypeEnum.TURBOPROP) {
			
			if (theEngine.getP0().doubleValue(NonSI.HORSEPOWER) > 5000) {
				System.err.println("WARNING (DIMENSIONS CALCULATION (TURBOPROP) - NACELLE) THE STATIC POWER VALUE IS OVER THE STATISTICAL RANGE ... RETURNING");
				return;
			}	
			
			_length = Amount.valueOf(
					-(1.28*0.00001*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2))
					+ (9.273*0.01*theEngine.getP0().doubleValue(NonSI.HORSEPOWER))
					- 8.3456,
					NonSI.INCH)
					.to(SI.METER);
			
			width = Amount.valueOf(
					- 0.95*10e-6*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					+ 0.0073*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					+ 25.3,
					NonSI.INCH)
					.to(SI.METER);
			
			height = Amount.valueOf(
					0.67*10e-11*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
					- 3.35*10e-6*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
					+ 0.029*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
					- 5.58425,
					NonSI.INCH)
					.to(SI.METER); 
			
			_diameterMax = Amount.valueOf(
					Math.sqrt(
							(width.times(height).times(4).divide(Math.PI).getEstimatedValue())
							),
					SI.METER
					);
		}
	}
	
	/**
	 * Wetted surface is considered as two times the external surface
	 * (the air flows both outside and inside)
	 * 
	 * BE CAREFUL! Only the external area of the nacelle is counted as wetted surface [Roskam] 
	 */
	private void calculateSurfaceWetted() {
		
		_surfaceWetted = _length.times(_diameterMax.times(Math.PI)).to(SI.SQUARE_METRE); 
	}

	@Override
	public void initializeWeights(Aircraft theAircraft) {
		if (_theWeights == null) 
			_theWeights = new NacelleWeightsManager(this, theAircraft);
	}

	@Override
	public void initializeBalance() {
		if (_theBalance == null)
			_theBalance = new NacelleBalanceManager(this);
	}

	@Override
	/**
	 * @author Vittorio Trifari
	 */
	public void calculateGeometry() {
		calculateGeometry(20);
	}
	
	@Override
	/**
	 * This method evaluates the Nacelle wetted surface and the Nacelle outlines for a 3-view
	 * representation. The X-Z and X-Y outlines are calculated using a cubic spline interpolation
	 * of the known points of the Nacelle; these latter obtained from the diameters at the three 
	 * main stations.
	 * 
	 * @author Vittorio Trifari
	 * @param nPoints the number of points used to model the Nacelle shape
	 */
	public void calculateGeometry(int nPoints){
	
		calculateSurfaceWetted();
		
		//----------------------------------------------------------------------------------------
		// comparison between the engine length and the nacelle length
		if(_theEngine.getLength() != null)
			if(_theEngine.getLength().doubleValue(SI.METER) <= this._length.doubleValue(SI.METER))
				System.out.println("[Nacelle] Nacelle length bigger than engine length \n\t CHECK PASSED --> proceding ...");
			else {
				System.err.println("[Nacelle] Nacelle length lower than engine length \n\t CHECK NOT PASSED --> returning ...");
				return;
			}
		
		//----------------------------------------------------------------------------------------
		// X-Z OUTLINE
		System.out.println("[Nacelle] calculating outlines ...");
		
		double[] trueXCoordinates = new double[3];
		double[] trueZCoordinatesUpper = new double[3];
		double[] trueZCoordinatesLower = new double[3];
		
		trueXCoordinates[0] = 0.0;
		trueXCoordinates[1] = this._xPositionMaximumDiameterLRF.doubleValue(SI.METER);
		trueXCoordinates[2] = this._length.doubleValue(SI.METER);
		
		trueZCoordinatesUpper[0] = this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueZCoordinatesUpper[1] = this._diameterMax.divide(2).doubleValue(SI.METER);
		trueZCoordinatesUpper[2] = (this._diameterOutlet.divide(2).doubleValue(SI.METER)) 
									+ this._zPositionOutletDiameterLRF.doubleValue(SI.METER);
		
		trueZCoordinatesLower[0] = - this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueZCoordinatesLower[1] = - this._diameterMax.divide(2).doubleValue(SI.METER);
		trueZCoordinatesLower[2] = (- this._diameterOutlet.divide(2).doubleValue(SI.METER)) 
									+ this._zPositionOutletDiameterLRF.doubleValue(SI.METER);
		
		_xCoordinatesOutlineDouble = MyArrayUtils
				.linspace(
						0.0,
						this._length.doubleValue(SI.METER),
						nPoints
						); 
		_xCoordinatesOutline = MyArrayUtils
				.convertDoubleArrayToListOfAmount(
						_xCoordinatesOutlineDouble,
						SI.METER
						);
		
		_zCoordinatesOutlineXZUpperDouble = new double[nPoints];
		_zCoordinatesOutlineXZLowerDouble = new double[nPoints];
		_zCoordinatesOutlineXZUpper = new ArrayList<Amount<Length>>();
		_zCoordinatesOutlineXZLower = new ArrayList<Amount<Length>>();
		
		MyInterpolatingFunction splineUpper = new MyInterpolatingFunction();
		splineUpper.interpolate(trueXCoordinates, trueZCoordinatesUpper);
		
		MyInterpolatingFunction splineLower = new MyInterpolatingFunction();
		splineLower.interpolate(trueXCoordinates, trueZCoordinatesLower);
		
		for(int i=0; i<nPoints; i++) {
			_zCoordinatesOutlineXZUpperDouble[i] = splineUpper.value(_xCoordinatesOutlineDouble[i]);
			_zCoordinatesOutlineXZLowerDouble[i] = splineLower.value(_xCoordinatesOutlineDouble[i]);
			_zCoordinatesOutlineXZUpper.add(
					Amount.valueOf(
							_zCoordinatesOutlineXZUpperDouble[i],
							SI.METER)
					);
			_zCoordinatesOutlineXZLower.add(
					Amount.valueOf(
							_zCoordinatesOutlineXZLowerDouble[i],
							SI.METER)
					);
		}
		
		//----------------------------------------------------------------------------------------
		// X-Y OUTLINE
		double[] trueYCoordinatesRight = new double[3];
		double[] trueYCoordinatesLeft = new double[3];
		
		trueYCoordinatesRight[0] = this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueYCoordinatesRight[1] = this._diameterMax.divide(2).doubleValue(SI.METER);
		trueYCoordinatesRight[2] = this._diameterOutlet.divide(2).doubleValue(SI.METER); 
		
		trueYCoordinatesLeft[0] = - this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueYCoordinatesLeft[1] = - this._diameterMax.divide(2).doubleValue(SI.METER);
		trueYCoordinatesLeft[2] = - this._diameterOutlet.divide(2).doubleValue(SI.METER); 
		
		_yCoordinatesOutlineXYRightDouble = new double[nPoints];
		_yCoordinatesOutlineXYLeftDouble = new double[nPoints];
		_yCoordinatesOutlineXYRight = new ArrayList<Amount<Length>>();
		_yCoordinatesOutlineXYLeft = new ArrayList<Amount<Length>>();
		
		MyInterpolatingFunction splineRight = new MyInterpolatingFunction();
		splineRight.interpolate(trueXCoordinates, trueYCoordinatesRight);
		
		MyInterpolatingFunction splineLeft = new MyInterpolatingFunction();
		splineLeft.interpolate(trueXCoordinates, trueYCoordinatesLeft);
		
		for(int i=0; i<nPoints; i++) {
			_yCoordinatesOutlineXYRightDouble[i] = splineRight.value(_xCoordinatesOutlineDouble[i]);
			_yCoordinatesOutlineXYLeftDouble[i] = splineLeft.value(_xCoordinatesOutlineDouble[i]);
			_yCoordinatesOutlineXYRight.add(
					Amount.valueOf(
							_yCoordinatesOutlineXYRightDouble[i],
							SI.METER)
					);
			_yCoordinatesOutlineXYLeft.add(
					Amount.valueOf(
							_yCoordinatesOutlineXYLeftDouble[i],
							SI.METER)
					);
		}
	}
	
	/**
	 * Invoke all the methods to evaluate 
	 * nacelle related quantities
	 * 
	 * @author Lorenzo Attanasio
	 */
	@Override
	public void calculateAll(Aircraft theAircraft) {
		initializeWeights(theAircraft);
		initializeBalance();
		
		_theWeights.calculateAll();
		_theBalance.calculateAll();
	}

	@Override
	public Double calculateFormFactor(){
		//matlab file ATR72
		return (1 + 0.165 
				+ 0.91/(_length.getEstimatedValue()/_diameterMax.getEstimatedValue())); 	
	}
	
	@Override
	public Amount<Length> getLength() {
		return _length;
	}

	@Override
	public void setLength(Amount<Length> _lenght) {
		this._length = _lenght;
	}

	@Override
	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}

	@Override
	public void setSurfaceWetted(Amount<Area> _sWet) {
		this._surfaceWetted = _sWet;
	}

	@Override
	public Amount<Length> getDiameterInlet() {
		return _diameterInlet;
	}

	@Override
	public void setDiameterInlet(Amount<Length> _diameterInlet) {
		this._diameterInlet = _diameterInlet;
	}

	@Override
	public Amount<Length> getDiameterMax() {
		return _diameterMax;
	}

	@Override
	public void setDiameterMean(Amount<Length> _diameter) {
		this._diameterMax = _diameter;
	}

	@Override
	public Amount<Length> getDiameterOutlet() {
		return _diameterOutlet;
	}

	@Override
	public void setDiameterOutlet(Amount<Length> _exitDiameter) {
		this._diameterOutlet = _exitDiameter;
	}

	@Override
	public Double getKInlet() {
		return _kInlet;
	}

	@Override
	public void setKInlet(Double _kInlet) {
		this._kInlet = _kInlet;
	}

	@Override
	public Double getKOutlet() {
		return _kOutlet;
	}

	@Override
	public void setKOutlet(Double _kOutlet) {
		this._kOutlet = _kOutlet;
	}

	@Override
	public Double getKLength() {
		return _kLength;
	}

	@Override
	public void setKLength(Double _kLength) {
		this._kLength = _kLength;
	}
	
	@Override
	public Double getKDiameterOutlet() {
		return _kDiameterOutlet;
	}

	@Override
	public void setKDiameterOutlet(Double _kDiameterOutlet) {
		this._kDiameterOutlet = _kDiameterOutlet;
	}

	@Override
	public Amount<Length> getXPositionMaximumDiameterLRF() {
		return _xPositionMaximumDiameterLRF;
	}

	@Override
	public void setXPositionMaximumDiameterLRF(Amount<Length> _xPositionMaximumDiameterLRF) {
		this._xPositionMaximumDiameterLRF = _xPositionMaximumDiameterLRF;
	}

	@Override
	public Amount<Length> getZPositionOutletDiameterLRF() {
		return _zPositionOutletDiameterLRF;
	}

	@Override
	public void setZPositionOutletDiameterLRF(Amount<Length> _zPositionOutletDiameterLRF) {
		this._zPositionOutletDiameterLRF = _zPositionOutletDiameterLRF;
	}

	@Override
	public Amount<Length> getRoughness() {
		return _roughness;
	}

	@Override
	public void setRoughness(Amount<Length> _roughness) {
		this._roughness = _roughness;
	}

	@Override
	public MountingPosition getMountingPosition() {
		return _mountingPosition;
	}

	@Override
	public void setMountingPosition(MountingPosition _mounting) {
		this._mountingPosition = _mounting;
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
	public Engine getTheEngine() {
		return _theEngine;
	}

	@Override
	public void setTheEngine(Engine _theEngine) {
		this._theEngine = _theEngine;
	}

	@Override
	public NacelleWeightsManager getWeights() {
		return _theWeights;
	}

	@Override
	public NacelleAerodynamicsManager getAerodynamics() {
		return _theAerodynamics;
	}

	@Override
	public NacelleBalanceManager getBalance() {
		return _theBalance;
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
	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	@Override
	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	@Override
	public double[] getXCoordinatesOutlineDouble() {
		return _xCoordinatesOutlineDouble;
	}

	@Override
	public void setXCoordinatesOutlineDouble(double[] xCoordinatesOutlineDouble) {
		_xCoordinatesOutlineDouble = xCoordinatesOutlineDouble;
	}

	@Override
	public double[] getZCoordinatesOutlineXZUpperDouble() {
		return _zCoordinatesOutlineXZUpperDouble;
	}

	@Override
	public void setZCoordinatesOutlineXZUpperDouble(double[] zCoordinatesOutlineXZUpperDouble) {
		_zCoordinatesOutlineXZUpperDouble = zCoordinatesOutlineXZUpperDouble;
	}

	@Override
	public double[] getZCoordinatesOutlineXZLowerDouble() {
		return _zCoordinatesOutlineXZLowerDouble;
	}

	@Override
	public void setZCoordinatesOutlineXZLowerDouble(double[] zCoordinatesOutlineXZLowerDouble) {
		_zCoordinatesOutlineXZLowerDouble = zCoordinatesOutlineXZLowerDouble;
	}

	@Override
	public double[] getYCoordinatesOutlineXYRightDouble() {
		return _yCoordinatesOutlineXYRightDouble;
	}

	@Override
	public void setYCoordinatesOutlineXYRightDouble(double[] _yCoordinatesOutlineXYRightDouble) {
		this._yCoordinatesOutlineXYRightDouble = _yCoordinatesOutlineXYRightDouble;
	}

	@Override
	public double[] getYCoordinatesOutlineXYLeftDouble() {
		return _yCoordinatesOutlineXYLeftDouble;
	}

	@Override
	public void setYCoordinatesOutlineXYLeftDouble(double[] _yCoordinatesOutlineXYLeftDouble) {
		this._yCoordinatesOutlineXYLeftDouble = _yCoordinatesOutlineXYLeftDouble;
	}

	@Override
	public List<Amount<Length>> getXCoordinatesOutline() {
		return _xCoordinatesOutline;
	}

	@Override
	public void setXCoordinatesOutline(List<Amount<Length>> xCoordinatesOutline) {
		_xCoordinatesOutline = xCoordinatesOutline;
	}

	@Override
	public List<Amount<Length>> getZCoordinatesOutlineXZUpper() {
		return _zCoordinatesOutlineXZUpper;
	}
	
	@Override
	public void setZCoordinatesOutlineXZUpper(List<Amount<Length>> zCoordinatesOutlineXZUpper) {
		_zCoordinatesOutlineXZUpper = zCoordinatesOutlineXZUpper;
	}

	@Override
	public List<Amount<Length>> getZCoordinatesOutlineXZLower() {
		return _zCoordinatesOutlineXZLower;
	}

	@Override
	public void setZCoordinatesOutlineXZLower(List<Amount<Length>> zCoordinatesOutlineXZLower) {
		_zCoordinatesOutlineXZLower = zCoordinatesOutlineXZLower;
	}

	@Override
	public List<Amount<Length>> getYCoordinatesOutlineXYRight() {
		return _yCoordinatesOutlineXYRight;
	}

	@Override
	public void setYCoordinatesOutlineXYRight(List<Amount<Length>> _yCoordinatesOutlineXYRight) {
		this._yCoordinatesOutlineXYRight = _yCoordinatesOutlineXYRight;
	}

	@Override
	public List<Amount<Length>> getYCoordinatesOutlineXYLeft() {
		return _yCoordinatesOutlineXYLeft;
	}

	@Override
	public void setYCoordinatesOutlineXYLeft(List<Amount<Length>> _yCoordinatesOutlineXYLeft) {
		this._yCoordinatesOutlineXYLeft = _yCoordinatesOutlineXYLeft;
	}
	
}

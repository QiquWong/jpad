package aircraft.components.nacelles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
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
public class NacelleCreator {

	//----------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private INacelleCreator _theNacelleCreatorInterface;
	private Engine _theEngine;
	
	// derived input
	private NacelleMountingPositionEnum _mountingPosition;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _diameterInlet;
	private Amount<Length> _diameterOutlet;
	private Amount<Length> _xPositionMaximumDiameterLRF;
	private Amount<Length> _zPositionOutletDiameterLRF;
	private Amount<Area> _surfaceWetted;
	
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
	
	//----------------------------------------------------------------------------------------------
	// BUILDER
	public NacelleCreator (Engine theEngine, INacelleCreator theNacelleCreatorInterface) {
		
		this._theNacelleCreatorInterface = theNacelleCreatorInterface;
		this._theEngine = theEngine;
		
		if(( (_theNacelleCreatorInterface.getLength() == null) || (_theNacelleCreatorInterface.getLength().doubleValue(SI.METER) == 0.0))
				|| ( (_theNacelleCreatorInterface.getDiameterMax() == null) || (_theNacelleCreatorInterface.getDiameterMax().doubleValue(SI.METER) == 0.0) ) ) {
			estimateDimensions(_theEngine);
		}
		
		this._diameterInlet = _theNacelleCreatorInterface.getDiameterMax().times(_theNacelleCreatorInterface.getKInlet());
		this._diameterOutlet = _theNacelleCreatorInterface.getDiameterMax().times(_theNacelleCreatorInterface.getKOutlet());
		this._xPositionMaximumDiameterLRF = _theNacelleCreatorInterface.getLength().times(_theNacelleCreatorInterface.getKLength());
		this._zPositionOutletDiameterLRF = _theNacelleCreatorInterface.getDiameterMax().times(_theNacelleCreatorInterface.getKDiameterOutlet());
		
		calculateGeometry();
		
	}
	
	//----------------------------------------------------------------------------------------------
	// METHODS	
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
		
		Engine engine = null;
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
		
		theNacelle = new NacelleCreator(
				engine,
				new INacelleCreator.Builder()
				.setId(id)
				.setRoughness(roughness)
				.setLength(length)
				.setDiameterMax(diameterMax)
				.setKInlet(kInlet)
				.setKOutlet(kOutlet)
				.setKLength(kLength)
				.setKDiameterOutlet(kDiameterOutlet)
				.build()
				);
		
		return theNacelle;
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\tID: '" + _theNacelleCreatorInterface.getId() + "'\n")
				.append("\t·····································\n")
				;
		
		if(_theEngine != null)
			sb.append("\tEngine ID: '" + _theEngine.getId() + "'\n")
			  .append("\tEngine type: " + _theEngine.getEngineType() + "\n")
			  ;
				
		sb.append("\tLength: " + _theNacelleCreatorInterface.getLength() + "\n")
		.append("\tDiameter max: " + _theNacelleCreatorInterface.getDiameterMax() + "\n")
		.append("\tK_inlet: " + _theNacelleCreatorInterface.getKInlet() + "\n")
		.append("\tK_outlet: " + _theNacelleCreatorInterface.getKOutlet() + "\n")
		.append("\tDiameter inlet: " + _diameterInlet + "\n")
		.append("\tDiameter outlet: " + _diameterOutlet + "\n")
		.append("\tK_length: " + _theNacelleCreatorInterface.getKLength() + "\n")
		.append("\tX position of the max diameter in LRF: " + _xPositionMaximumDiameterLRF + "\n")
		.append("\tK_diameter_outlet: " + _theNacelleCreatorInterface.getKDiameterOutlet() + "\n")
		.append("\tZ position of the outlet diameter in LRF: " + _zPositionOutletDiameterLRF + "\n")
		.append("\t·····································\n")
		.append("\tSurface roughness: " + _theNacelleCreatorInterface.getRoughness() + "\n")
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
	public void estimateDimensions (Engine theEngine) {
				
		Amount<Length> width;
		Amount<Length> height;
		
		if((theEngine.getEngineType() == EngineTypeEnum.TURBOFAN) 
				|| (theEngine.getEngineType() == EngineTypeEnum.TURBOJET)) {
			
			if (theEngine.getT0().doubleValue(NonSI.POUND_FORCE) > 100000) {
				System.err.println("WARNING (DIMENSIONS CALCULATION (JET DRIVEN) - NACELLE) THE STATIC THRUST VALUE IS OVER THE STATISTICAL RANGE ... RETURNING");
				return;
			}
			
			setLength(
					Amount.valueOf(
							40 + (0.59 * Math.sqrt(theEngine.getT0().doubleValue(NonSI.POUND_FORCE))),
							NonSI.INCH)
					.to(SI.METER)
					);
			setDiameterMax(
					Amount.valueOf(
							5 + (0.39 * Math.sqrt(theEngine.getT0().doubleValue(NonSI.POUND_FORCE))),
							NonSI.INCH)
					.to(SI.METER)
					); 
		}
		
		else if(theEngine.getEngineType() == EngineTypeEnum.PISTON) {
			
			if (theEngine.getP0().doubleValue(NonSI.HORSEPOWER) > 1200) {
				System.err.println("WARNING (DIMENSIONS CALCULATION (PISTON) - NACELLE) THE STATIC POWER VALUE IS OVER THE STATISTICAL RANGE ... RETURNING");
				return;
			}	
			
			setLength(
					Amount.valueOf(
							4*10e-10*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),4)
							- 6*10e-7*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),3)
							+ 8*10e-5*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2)
							- 0.2193*theEngine.getP0().doubleValue(NonSI.HORSEPOWER)
							+ 54.097,
							NonSI.INCH)
					.to(SI.METER)
					);
			
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
			
			setDiameterMax(
					Amount.valueOf(
							Math.sqrt(
									(width.times(height).times(4).divide(Math.PI).getEstimatedValue())
									),
							SI.METER
							)
					);
			}
		
		else if(theEngine.getEngineType() == EngineTypeEnum.TURBOPROP) {
			
			if (theEngine.getP0().doubleValue(NonSI.HORSEPOWER) > 5000) {
				System.err.println("WARNING (DIMENSIONS CALCULATION (TURBOPROP) - NACELLE) THE STATIC POWER VALUE IS OVER THE STATISTICAL RANGE ... RETURNING");
				return;
			}	
			
			setLength(
					Amount.valueOf(
							-(1.28*0.00001*Math.pow(theEngine.getP0().doubleValue(NonSI.HORSEPOWER),2))
							+ (9.273*0.01*theEngine.getP0().doubleValue(NonSI.HORSEPOWER))
							- 8.3456,
							NonSI.INCH)
					.to(SI.METER)
					);
			
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
			
			setDiameterMax(
					Amount.valueOf(
							Math.sqrt(
									(width.times(height).times(4).divide(Math.PI).getEstimatedValue())
									),
							SI.METER
							)
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
		
		_surfaceWetted = _theNacelleCreatorInterface.getLength()
				.times(_theNacelleCreatorInterface.getDiameterMax().times(Math.PI))
				.to(SI.SQUARE_METRE); 
	}

	/**
	 * @author Vittorio Trifari
	 */
	public void calculateGeometry() {
		calculateGeometry(20);
	}
	
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
			if(_theEngine.getLength().doubleValue(SI.METER) <= this._theNacelleCreatorInterface.getLength().doubleValue(SI.METER))
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
		trueXCoordinates[2] = this._theNacelleCreatorInterface.getLength().doubleValue(SI.METER);
		
		trueZCoordinatesUpper[0] = this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueZCoordinatesUpper[1] = this._theNacelleCreatorInterface.getDiameterMax().divide(2).doubleValue(SI.METER);
		trueZCoordinatesUpper[2] = (this._diameterOutlet.divide(2).doubleValue(SI.METER)) 
									+ this._zPositionOutletDiameterLRF.doubleValue(SI.METER);
		
		trueZCoordinatesLower[0] = - this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueZCoordinatesLower[1] = - this._theNacelleCreatorInterface.getDiameterMax().divide(2).doubleValue(SI.METER);
		trueZCoordinatesLower[2] = (- this._diameterOutlet.divide(2).doubleValue(SI.METER)) 
									+ this._zPositionOutletDiameterLRF.doubleValue(SI.METER);
		
		_xCoordinatesOutlineDouble = MyArrayUtils
				.linspace(
						0.0,
						this._theNacelleCreatorInterface.getLength().doubleValue(SI.METER),
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
		trueYCoordinatesRight[1] = this._theNacelleCreatorInterface.getDiameterMax().divide(2).doubleValue(SI.METER);
		trueYCoordinatesRight[2] = this._diameterOutlet.divide(2).doubleValue(SI.METER); 
		
		trueYCoordinatesLeft[0] = - this._diameterInlet.divide(2).doubleValue(SI.METER);
		trueYCoordinatesLeft[1] = - this._theNacelleCreatorInterface.getDiameterMax().divide(2).doubleValue(SI.METER);
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
	
	//----------------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public INacelleCreator getTheNacelleCreatorInterface() {
		return _theNacelleCreatorInterface;
	}
	
	public void setTheNacelleCreatorInterface (INacelleCreator theNacelleCreatorInterface) {
		this._theNacelleCreatorInterface = theNacelleCreatorInterface;
	}
	
	public Amount<Length> getLength() {
		return _theNacelleCreatorInterface.getLength();
	}
	
	public void setLength(Amount<Length> _lenght) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setLength(_lenght).build());
	}

	public Amount<Area> getSurfaceWetted() {
		return _surfaceWetted;
	}
	
	public void setSurfaceWetted(Amount<Area> _sWet) {
		this._surfaceWetted = _sWet;
	}

	public Amount<Length> getDiameterInlet() {
		return _diameterInlet;
	}
	
	public void setDiameterInlet(Amount<Length> _diameterInlet) {
		this._diameterInlet = _diameterInlet;
	}
	
	public Amount<Length> getDiameterMax() {
		return _theNacelleCreatorInterface.getDiameterMax();
	}
	
	public void setDiameterMax(Amount<Length> _diameter) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setDiameterMax(_diameter).build());
	}

	public Amount<Length> getDiameterOutlet() {
		return _diameterOutlet;
	}
	
	public void setDiameterOutlet(Amount<Length> _exitDiameter) {
		this._diameterOutlet = _exitDiameter;
	}
	
	public double getKInlet() {
		return _theNacelleCreatorInterface.getKInlet();
	}
	
	public void setKInlet(double _kInlet) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setKInlet(_kInlet).build());
	}

	public double getKOutlet() {
		return _theNacelleCreatorInterface.getKOutlet();
	}
	
	public void setKOutlet(double _kOutlet) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setKOutlet(_kOutlet).build());
	}
	
	public double getKLength() {
		return _theNacelleCreatorInterface.getKLength();
	}
	
	public void setKLength(double _kLength) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setKLength(_kLength).build());
	}
	
	public double getKDiameterOutlet() {
		return _theNacelleCreatorInterface.getKDiameterOutlet();
	}
	
	public void setKDiameterOutlet(double _kDiameterOutlet) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setKDiameterOutlet(_kDiameterOutlet).build());
	}

	public Amount<Length> getXPositionMaximumDiameterLRF() {
		return _xPositionMaximumDiameterLRF;
	}
	
	public void setXPositionMaximumDiameterLRF(Amount<Length> _xPositionMaximumDiameterLRF) {
		this._xPositionMaximumDiameterLRF = _xPositionMaximumDiameterLRF;
	}
	
	public Amount<Length> getZPositionOutletDiameterLRF() {
		return _zPositionOutletDiameterLRF;
	}
	
	public void setZPositionOutletDiameterLRF(Amount<Length> _zPositionOutletDiameterLRF) {
		this._zPositionOutletDiameterLRF = _zPositionOutletDiameterLRF;
	}
	
	public Amount<Length> getRoughness() {
		return _theNacelleCreatorInterface.getRoughness();
	}

	public void setRoughness(Amount<Length> _roughness) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setRoughness(_roughness).build());
	}

	public NacelleMountingPositionEnum getMountingPosition() {
		return _mountingPosition;
	}
	
	public void setMountingPosition(NacelleMountingPositionEnum _mounting) {
		this._mountingPosition = _mounting;
	}
	
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}
	
	public void setXApexConstructionAxes(Amount<Length> _X0) {
		this._xApexConstructionAxes = _X0;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _Y0) {
		this._yApexConstructionAxes = _Y0;
	}
	
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}
	
	public void setZApexConstructionAxes(Amount<Length> _Z0) {
		this._zApexConstructionAxes = _Z0;
	}
	
	public Engine getTheEngine() {
		return _theEngine;
	}
	
	public void setTheEngine(Engine _theEngine) {
		this._theEngine = _theEngine;
	}
	
	public String getId() {
		return _theNacelleCreatorInterface.getId();
	}

	public void setId(String id) {
		setTheNacelleCreatorInterface(INacelleCreator.Builder.from(_theNacelleCreatorInterface).setId(id).build());
	}
	
	public double[] getXCoordinatesOutlinedouble() {
		return _xCoordinatesOutlineDouble;
	}

	public void setXCoordinatesOutlinedouble(double[] xCoordinatesOutlinedouble) {
		_xCoordinatesOutlineDouble = xCoordinatesOutlinedouble;
	}

	public double[] getZCoordinatesOutlineXZUpperdouble() {
		return _zCoordinatesOutlineXZUpperDouble;
	}

	public void setZCoordinatesOutlineXZUpperdouble(double[] zCoordinatesOutlineXZUpperdouble) {
		_zCoordinatesOutlineXZUpperDouble = zCoordinatesOutlineXZUpperdouble;
	}

	public double[] getZCoordinatesOutlineXZLowerdouble() {
		return _zCoordinatesOutlineXZLowerDouble;
	}

	public void setZCoordinatesOutlineXZLowerdouble(double[] zCoordinatesOutlineXZLowerdouble) {
		_zCoordinatesOutlineXZLowerDouble = zCoordinatesOutlineXZLowerdouble;
	}

	public double[] getYCoordinatesOutlineXYRightdouble() {
		return _yCoordinatesOutlineXYRightDouble;
	}

	public void setYCoordinatesOutlineXYRightdouble(double[] _yCoordinatesOutlineXYRightdouble) {
		this._yCoordinatesOutlineXYRightDouble = _yCoordinatesOutlineXYRightdouble;
	}

	public double[] getYCoordinatesOutlineXYLeftdouble() {
		return _yCoordinatesOutlineXYLeftDouble;
	}

	public void setYCoordinatesOutlineXYLeftdouble(double[] _yCoordinatesOutlineXYLeftdouble) {
		this._yCoordinatesOutlineXYLeftDouble = _yCoordinatesOutlineXYLeftdouble;
	}

	public List<Amount<Length>> getXCoordinatesOutline() {
		return _xCoordinatesOutline;
	}

	public void setXCoordinatesOutline(List<Amount<Length>> xCoordinatesOutline) {
		_xCoordinatesOutline = xCoordinatesOutline;
	}

	public List<Amount<Length>> getZCoordinatesOutlineXZUpper() {
		return _zCoordinatesOutlineXZUpper;
	}
	
	public void setZCoordinatesOutlineXZUpper(List<Amount<Length>> zCoordinatesOutlineXZUpper) {
		_zCoordinatesOutlineXZUpper = zCoordinatesOutlineXZUpper;
	}
	
	public List<Amount<Length>> getZCoordinatesOutlineXZLower() {
		return _zCoordinatesOutlineXZLower;
	}

	public void setZCoordinatesOutlineXZLower(List<Amount<Length>> zCoordinatesOutlineXZLower) {
		_zCoordinatesOutlineXZLower = zCoordinatesOutlineXZLower;
	}

	public List<Amount<Length>> getYCoordinatesOutlineXYRight() {
		return _yCoordinatesOutlineXYRight;
	}

	public void setYCoordinatesOutlineXYRight(List<Amount<Length>> _yCoordinatesOutlineXYRight) {
		this._yCoordinatesOutlineXYRight = _yCoordinatesOutlineXYRight;
	}

	public List<Amount<Length>> getYCoordinatesOutlineXYLeft() {
		return _yCoordinatesOutlineXYLeft;
	}

	public void setYCoordinatesOutlineXYLeft(List<Amount<Length>> _yCoordinatesOutlineXYLeft) {
		this._yCoordinatesOutlineXYLeft = _yCoordinatesOutlineXYLeft;
	}
	
}
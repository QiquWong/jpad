package aircraft.auxiliary.airfoil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.AuxiliaryComponentCalculator;
import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ComponentEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import processing.core.PVector;
import standaloneutils.customdata.MyArray;

public class Geometry extends AuxiliaryComponentCalculator{

	private String _id = ""; 
	public static int idCounter = 0;
	public static int nGeo = 0;

	// Non-dimensional coordinates
	private List<Double> _cornerPointsX = new ArrayList<Double>();
	private List<Double> _cornerPointsZ = new ArrayList<Double>();

	private Double _thicknessOverChordUnit, _deltaYPercent; 
	
	private Double _camberRatio;
	private Double _maximumThicknessOverChord; 
	private Double _radiusLE; 
	private Amount<Angle> _anglePhiTE = Amount.valueOf(0,SI.RADIAN);
	private Amount<Length> _chord;
	
	/** Twist relative to root chord */
	private Amount<Angle> _twist;

	Double[] _xCoords, _yCoords, _zCoords;
	MyArray xCoord;
	MyArray yCoord;
	MyArray zCoord;

	private List<PVector> _coordinatesRight = new ArrayList<PVector>();
	private List<PVector> _coordinatesLeft = new ArrayList<PVector>();

	// Non dimensional coordinate of the airfoil along the semi-span
	private Double _yStation = 0.;
	private Airfoil _theAirfoil;
	private boolean isMirrored;
	private ComponentEnum liftingSurfaceType;
	private double _etaStation;
	private double iw;

	/*
	 * This constructor builds the airfoil geometry using the AirfoilCreator class 
	 */
	public Geometry(AirfoilCreator airfoilCreator, AerodynamicDatabaseReader reader) {

		int airfoilFamilyIndex = 0;
		//recognizing airfoil family
		if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.NACA_4_Digit)) 
			airfoilFamilyIndex = 1;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.NACA_5_Digit))
			airfoilFamilyIndex = 2;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.NACA_63_Series))
			airfoilFamilyIndex = 3;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.NACA_64_Series))
			airfoilFamilyIndex = 4;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.NACA_65_Series))
			airfoilFamilyIndex = 5;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.NACA_66_Series))
			airfoilFamilyIndex = 6;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.BICONVEX))
			airfoilFamilyIndex = 7;
		else if(airfoilCreator.getFamily().equals(AirfoilFamilyEnum.DOUBLE_WEDGE))
			airfoilFamilyIndex = 8;

		this._deltaYPercent = reader.getDeltaYvsThickness(
				airfoilCreator.getThicknessToChordRatio(),
				airfoilFamilyIndex
				);
		this._anglePhiTE = airfoilCreator.getAngleAtTrailingEdge();
		this._maximumThicknessOverChord = airfoilCreator.getThicknessToChordRatio();
		this._camberRatio = airfoilCreator.getCamberRatio();
		this._radiusLE = airfoilCreator.getRadiusLeadingEdgeNormalized();
		this._xCoords = airfoilCreator.getXCoords();
		this._zCoords = airfoilCreator.getZCoords();
		this._chord = airfoilCreator.getChord(); 
	}
	
	public Geometry(Airfoil airfoil) {	

		_id = airfoil.getId() + "0" + idCounter + "99";
		idCounter++;

		isMirrored = airfoil.get_theLiftingSurface().isMirrored();
		liftingSurfaceType = airfoil.get_theLiftingSurface().getType();
		_theAirfoil = airfoil;
		

		// NACA 66(3)-418
		_xCoords = new Double[]{1.00000, 0.95060, 0.90120, 0.85162, 0.80185, 0.75191, 0.70178, 0.65149, 0.60107, 0.55056, 0.50000, 0.44943, 0.39885, 0.34829, 0.29775, 0.24726, 0.19683, 0.14651, 0.09636, 0.07140, 0.04656, 0.02194, 0.00981, 0.00509, 0.00280, 0.00000, 0.00720, 0.00991, 0.01519, 0.02806, 0.05344, 0.07860, 0.10364, 0.15349, 0.20317, 0.25274, 0.30225, 0.35171, 0.40115, 0.45057, 0.50000, 0.54944, 0.59893, 0.64851, 0.69822, 0.74809, 0.79815, 0.84838, 0.89880, 0.94940, 1.00000}; 
		_zCoords = new Double[]{0.00000, 0.01275, 0.02744, 0.04276, 0.05794, 0.07238, 0.08539, 0.09639, 0.10464, 0.10923, 0.11148, 0.11188, 0.11059, 0.10759, 0.10287, 0.09633, 0.08773, 0.07669, 0.06231, 0.05347, 0.04306, 0.03000, 0.02147, 0.01692, 0.01405, 0.00000, -0.01205, -0.01412, -0.01719, -0.02256, -0.03042, -0.03651, -0.04163, -0.04977, -0.05589, -0.06053, -0.06399, -0.06639, -0.06775, -0.06808, -0.06736, -0.06543, -0.06180, -0.05519, -0.04651, -0.03658, -0.02610, -0.01584, -0.00676, -0.00011, 0.00000};

		if (liftingSurfaceType.equals(ComponentEnum.HORIZONTAL_TAIL)
				|| liftingSurfaceType.equals(ComponentEnum.VERTICAL_TAIL)) {

			// NACA 0012
			_xCoords = new Double[]{1.000000, 0.993720, 0.982766, 0.969978, 0.955648, 0.940249, 0.924212, 0.907834, 0.891280, 0.874637, 0.857947, 0.841230, 0.824498, 0.807755, 0.791005, 0.774250, 0.757492, 0.740731, 0.723970, 0.707209, 0.690451, 0.673695, 0.656944, 0.640199, 0.623460, 0.606731, 0.590011, 0.573303, 0.556607, 0.539927, 0.523262, 0.506616, 0.489990, 0.473385, 0.456805, 0.440251, 0.423726, 0.407233, 0.390775, 0.374354, 0.357976, 0.341643, 0.325361, 0.309135, 0.292972, 0.276878, 0.260863, 0.244936, 0.229110, 0.213401, 0.197828, 0.182416, 0.167201, 0.152227, 0.137561, 0.123289, 0.109532, 0.096442, 0.084194, 0.072956, 0.062850, 0.053918, 0.046123, 0.039362, 0.033509, 0.028432, 0.024011, 0.020146, 0.016754, 0.013769, 0.011142, 0.008834, 0.006819, 0.005076, 0.003597, 0.002376, 0.001412, 0.000703, 0.000245, 0.000026, 0.000026, 0.000245, 0.000703, 0.001412, 0.002376, 0.003597, 0.005076, 0.006819, 0.008834, 0.011142, 0.013769, 0.016754, 0.020146, 0.024011, 0.028432, 0.033509, 0.039363, 0.046123, 0.053919, 0.062850, 0.072956, 0.084194, 0.096442, 0.109532, 0.123289, 0.137561, 0.152228, 0.167201, 0.182417, 0.197828, 0.213401, 0.229110, 0.244936, 0.260863, 0.276879, 0.292972, 0.309136, 0.325361, 0.341643, 0.357976, 0.374355, 0.390775, 0.407233, 0.423727, 0.440251, 0.456805, 0.473386, 0.489990, 0.506616, 0.523263, 0.539927, 0.556608, 0.573303, 0.590011, 0.606731, 0.623461, 0.640199, 0.656944, 0.673695, 0.690451, 0.707210, 0.723970, 0.740731, 0.757492, 0.774250, 0.791005, 0.807755, 0.824498, 0.841230, 0.857947, 0.874637, 0.891280, 0.907834, 0.924213, 0.940249, 0.955649, 0.969978, 0.982766, 0.993720, 1.000000};
			_zCoords = new Double[]{0.001260, 0.002138,  0.003653,  0.005396,  0.007317,  0.009346,  0.011419,  0.013497,  0.015557,  0.017589,  0.019588,  0.021551,  0.023478,  0.025368,  0.027222,  0.029039,  0.030819,  0.032562,  0.034267,  0.035934,  0.037562,  0.039149,  0.040696,  0.042200,  0.043661,  0.045077,  0.046446,  0.047766,  0.049036,  0.050253,  0.051414,  0.052518,  0.053561,  0.054540,  0.055453,  0.056295,  0.057063,  0.057753,  0.058361,  0.058882,  0.059311,  0.059644,  0.059875,  0.059998,  0.060007,  0.059894,  0.059654,  0.059277,  0.058757,  0.058084,  0.057248,  0.056241,  0.055054,  0.053676,  0.052101,  0.050328,  0.048360,  0.046216,  0.043928,  0.041542,  0.039116,  0.036701,  0.034339,  0.032051,  0.029846,  0.027721,  0.025669,  0.023677,  0.021733,  0.019823,  0.017936,  0.016059,  0.014185,  0.012303,  0.010409,  0.008502,  0.006586,  0.004670,  0.002769,  0.000909, -0.000909, -0.002769, -0.004670, -0.006586, -0.008502, -0.010409, -0.012303, -0.014185, -0.016059, -0.017936, -0.019823, -0.021733, -0.023677, -0.025669, -0.027721, -0.029846, -0.032051, -0.034339, -0.036701, -0.039116, -0.041542, -0.043928, -0.046216, -0.048360, -0.050328, -0.052101, -0.053676, -0.055054, -0.056241, -0.057248, -0.058084, -0.058757, -0.059277, -0.059654, -0.059894, -0.060007, -0.059998, -0.059875, -0.059644, -0.059311, -0.058882, -0.058361, -0.057753, -0.057063, -0.056295, -0.055453, -0.054540, -0.053561, -0.052518, -0.051414, -0.050253, -0.049036, -0.047766, -0.046446, -0.045077, -0.043661, -0.042200, -0.040696, -0.039149, -0.037562, -0.035934, -0.034267, -0.032562, -0.030819, -0.029039, -0.027222, -0.025368, -0.023478, -0.021551, -0.019588, -0.017589, -0.015557, -0.013497, -0.011419, -0.009346, -0.007317, -0.005396, -0.003653, -0.002138, -0.001260};

		}
		_yCoords = new Double[_xCoords.length];
		
		set_cornerPointsX(new ArrayList<Double>(Arrays.asList(_xCoords)));

		Double[] vz = {
				0.04351, 0.03982, 0.03522, 0.02925, 0.02074, 0.01438, 0.00000,-0.01438,-0.02074,-0.02925,-0.03522,
				-0.03982,-0.04351,-0.04655,-0.05121,-0.05454,-0.05740,-0.05924,-0.06033,-0.06087,-0.06100,-0.06084,
				-0.06048,-0.06002,-0.05951,-0.05808,-0.05588,-0.05294,-0.04952,-0.04563,-0.04133,-0.03664,-0.03160,
				-0.02623,-0.02053,-0.01448,-0.00807,-0.00126
		};
		set_cornerPointsZ(new ArrayList<Double>(Arrays.asList(vz)));

		_maximumThicknessOverChord = 0.15;
		_thicknessOverChordUnit = 0.12; 
		_radiusLE = 0.030195; 
		_deltaYPercent =3.5;
		_anglePhiTE = Amount.valueOf(0,SI.RADIAN); 
		_twist = Amount.valueOf(0.0,SI.RADIAN);

	}

	/*
	 * Overload of the previous builder which allocates the airfoil at a certain station on the wing
	 */
	public Geometry(Airfoil airfoil, Double yLoc) {	

		_id = airfoil.getId() + "0" + idCounter + "99";
		idCounter++;

		isMirrored = airfoil.get_theLiftingSurface().isMirrored();
		liftingSurfaceType = airfoil.get_theLiftingSurface().getType();
		_theAirfoil = airfoil;
		

		// NACA 66(3)-418
		_xCoords = new Double[]{1.00000, 0.95060, 0.90120, 0.85162, 0.80185, 0.75191, 0.70178, 0.65149, 0.60107, 0.55056, 0.50000, 0.44943, 0.39885, 0.34829, 0.29775, 0.24726, 0.19683, 0.14651, 0.09636, 0.07140, 0.04656, 0.02194, 0.00981, 0.00509, 0.00280, 0.00000, 0.00720, 0.00991, 0.01519, 0.02806, 0.05344, 0.07860, 0.10364, 0.15349, 0.20317, 0.25274, 0.30225, 0.35171, 0.40115, 0.45057, 0.50000, 0.54944, 0.59893, 0.64851, 0.69822, 0.74809, 0.79815, 0.84838, 0.89880, 0.94940, 1.00000}; 
		_zCoords = new Double[]{0.00000, 0.01275, 0.02744, 0.04276, 0.05794, 0.07238, 0.08539, 0.09639, 0.10464, 0.10923, 0.11148, 0.11188, 0.11059, 0.10759, 0.10287, 0.09633, 0.08773, 0.07669, 0.06231, 0.05347, 0.04306, 0.03000, 0.02147, 0.01692, 0.01405, 0.00000, -0.01205, -0.01412, -0.01719, -0.02256, -0.03042, -0.03651, -0.04163, -0.04977, -0.05589, -0.06053, -0.06399, -0.06639, -0.06775, -0.06808, -0.06736, -0.06543, -0.06180, -0.05519, -0.04651, -0.03658, -0.02610, -0.01584, -0.00676, -0.00011, 0.00000};

		if (liftingSurfaceType.equals(ComponentEnum.HORIZONTAL_TAIL)
				|| liftingSurfaceType.equals(ComponentEnum.VERTICAL_TAIL)) {

			// NACA 0012
			_xCoords = new Double[]{1.000000, 0.993720, 0.982766, 0.969978, 0.955648, 0.940249, 0.924212, 0.907834, 0.891280, 0.874637, 0.857947, 0.841230, 0.824498, 0.807755, 0.791005, 0.774250, 0.757492, 0.740731, 0.723970, 0.707209, 0.690451, 0.673695, 0.656944, 0.640199, 0.623460, 0.606731, 0.590011, 0.573303, 0.556607, 0.539927, 0.523262, 0.506616, 0.489990, 0.473385, 0.456805, 0.440251, 0.423726, 0.407233, 0.390775, 0.374354, 0.357976, 0.341643, 0.325361, 0.309135, 0.292972, 0.276878, 0.260863, 0.244936, 0.229110, 0.213401, 0.197828, 0.182416, 0.167201, 0.152227, 0.137561, 0.123289, 0.109532, 0.096442, 0.084194, 0.072956, 0.062850, 0.053918, 0.046123, 0.039362, 0.033509, 0.028432, 0.024011, 0.020146, 0.016754, 0.013769, 0.011142, 0.008834, 0.006819, 0.005076, 0.003597, 0.002376, 0.001412, 0.000703, 0.000245, 0.000026, 0.000026, 0.000245, 0.000703, 0.001412, 0.002376, 0.003597, 0.005076, 0.006819, 0.008834, 0.011142, 0.013769, 0.016754, 0.020146, 0.024011, 0.028432, 0.033509, 0.039363, 0.046123, 0.053919, 0.062850, 0.072956, 0.084194, 0.096442, 0.109532, 0.123289, 0.137561, 0.152228, 0.167201, 0.182417, 0.197828, 0.213401, 0.229110, 0.244936, 0.260863, 0.276879, 0.292972, 0.309136, 0.325361, 0.341643, 0.357976, 0.374355, 0.390775, 0.407233, 0.423727, 0.440251, 0.456805, 0.473386, 0.489990, 0.506616, 0.523263, 0.539927, 0.556608, 0.573303, 0.590011, 0.606731, 0.623461, 0.640199, 0.656944, 0.673695, 0.690451, 0.707210, 0.723970, 0.740731, 0.757492, 0.774250, 0.791005, 0.807755, 0.824498, 0.841230, 0.857947, 0.874637, 0.891280, 0.907834, 0.924213, 0.940249, 0.955649, 0.969978, 0.982766, 0.993720, 1.000000};
			_zCoords = new Double[]{0.001260, 0.002138,  0.003653,  0.005396,  0.007317,  0.009346,  0.011419,  0.013497,  0.015557,  0.017589,  0.019588,  0.021551,  0.023478,  0.025368,  0.027222,  0.029039,  0.030819,  0.032562,  0.034267,  0.035934,  0.037562,  0.039149,  0.040696,  0.042200,  0.043661,  0.045077,  0.046446,  0.047766,  0.049036,  0.050253,  0.051414,  0.052518,  0.053561,  0.054540,  0.055453,  0.056295,  0.057063,  0.057753,  0.058361,  0.058882,  0.059311,  0.059644,  0.059875,  0.059998,  0.060007,  0.059894,  0.059654,  0.059277,  0.058757,  0.058084,  0.057248,  0.056241,  0.055054,  0.053676,  0.052101,  0.050328,  0.048360,  0.046216,  0.043928,  0.041542,  0.039116,  0.036701,  0.034339,  0.032051,  0.029846,  0.027721,  0.025669,  0.023677,  0.021733,  0.019823,  0.017936,  0.016059,  0.014185,  0.012303,  0.010409,  0.008502,  0.006586,  0.004670,  0.002769,  0.000909, -0.000909, -0.002769, -0.004670, -0.006586, -0.008502, -0.010409, -0.012303, -0.014185, -0.016059, -0.017936, -0.019823, -0.021733, -0.023677, -0.025669, -0.027721, -0.029846, -0.032051, -0.034339, -0.036701, -0.039116, -0.041542, -0.043928, -0.046216, -0.048360, -0.050328, -0.052101, -0.053676, -0.055054, -0.056241, -0.057248, -0.058084, -0.058757, -0.059277, -0.059654, -0.059894, -0.060007, -0.059998, -0.059875, -0.059644, -0.059311, -0.058882, -0.058361, -0.057753, -0.057063, -0.056295, -0.055453, -0.054540, -0.053561, -0.052518, -0.051414, -0.050253, -0.049036, -0.047766, -0.046446, -0.045077, -0.043661, -0.042200, -0.040696, -0.039149, -0.037562, -0.035934, -0.034267, -0.032562, -0.030819, -0.029039, -0.027222, -0.025368, -0.023478, -0.021551, -0.019588, -0.017589, -0.015557, -0.013497, -0.011419, -0.009346, -0.007317, -0.005396, -0.003653, -0.002138, -0.001260};

		}
		_yCoords = new Double[_xCoords.length];
		
		set_cornerPointsX(new ArrayList<Double>(Arrays.asList(_xCoords)));

		Double[] vz = {
				0.04351, 0.03982, 0.03522, 0.02925, 0.02074, 0.01438, 0.00000,-0.01438,-0.02074,-0.02925,-0.03522,
				-0.03982,-0.04351,-0.04655,-0.05121,-0.05454,-0.05740,-0.05924,-0.06033,-0.06087,-0.06100,-0.06084,
				-0.06048,-0.06002,-0.05951,-0.05808,-0.05588,-0.05294,-0.04952,-0.04563,-0.04133,-0.03664,-0.03160,
				-0.02623,-0.02053,-0.01448,-0.00807,-0.00126
		};
		set_cornerPointsZ(new ArrayList<Double>(Arrays.asList(vz)));

		_maximumThicknessOverChord = 0.15;
		_thicknessOverChordUnit = 0.12; 
		_radiusLE = 0.030195; 
		_deltaYPercent =3.5;
		_anglePhiTE = Amount.valueOf(0,SI.RADIAN); 
		_twist = Amount.valueOf(0.0,SI.RADIAN);

	}
	
	public void update(double yLoc) {
		Arrays.fill(_yCoords, yLoc);
		_yStation = yLoc;
		_etaStation = yLoc/_theAirfoil.get_theLiftingSurface().getSemiSpan().getEstimatedValue();
		iw = _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getAngleOfIncidence().getEstimatedValue();
		populateCoordinateList(_theAirfoil.get_theLiftingSurface().getChordAtYActual(yLoc));
	}

	private void populateCoordinateList(double chord) {

		float c = (float) chord;
		float x, y, z;

		for (int i=0; i<_xCoords.length; i++) {

			// Scale to actual dimensions
			x = _xCoords[i].floatValue()*c;
			y = _yCoords[i].floatValue();
			z = _zCoords[i].floatValue()*c;

			// Rotation due to twist
			if (liftingSurfaceType.equals(ComponentEnum.WING)) {
				float r = (float) Math.sqrt(x*x + z*z);
				x = (float) (x - r*(1-Math.cos(-_twist.to(SI.RADIAN).getEstimatedValue() - iw)));
				z = (float) (z + r*Math.sin(-_twist.to(SI.RADIAN).getEstimatedValue() - iw));
			}

			// Actual location
			x = x + (float) _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getXLEAtYActual(_yStation).getEstimatedValue()
					+ (float) _theAirfoil.get_theLiftingSurface().get_X0().getEstimatedValue();
			y = _yCoords[i].floatValue();
			z = z + (float) _theAirfoil.get_theLiftingSurface().get_Z0().getEstimatedValue()
					+ (float) (_yStation
							* Math.tan(_theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getDihedralAtYActual(_yStation).getEstimatedValue()));

			if (isMirrored) {
				_coordinatesLeft.add(new PVector(x, -y, z));
			}	

			if (liftingSurfaceType.equals(ComponentEnum.VERTICAL_TAIL)) {
				_coordinatesRight.add( 
						new PVector(
								x,
								_zCoords[i].floatValue()*c, 
								_yCoords[i].floatValue()
								+ (float) _theAirfoil.get_theLiftingSurface().get_Z0().getEstimatedValue()));

			} else {
				_coordinatesRight.add(new PVector(x, y, z));
			}

		}
	}

	public PVector getCentralPoint() {
		float x,y,z;

		int nPan = _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getPanels().size(); 
		
		if (_theAirfoil.get_theLiftingSurface().getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			x = (float) (_theAirfoil.get_theLiftingSurface().get_X0().getEstimatedValue()
					+ _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getDiscretizedXle().get(_theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getDiscretizedXle().size()-1).getEstimatedValue()
					+ _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getPanels().get(nPan - 1).getChordTip().getEstimatedValue()/2);
			z = (float) (_theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getSpan().getEstimatedValue())*1.005f 
					+ (float) _theAirfoil.get_theLiftingSurface().get_Z0().getEstimatedValue();
			y = 0.0f;

		} else {
			x = (float) (_theAirfoil.get_theLiftingSurface().get_X0().getEstimatedValue()
					+ _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getDiscretizedXle().get(_theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getDiscretizedXle().size()-1).getEstimatedValue()
					+ _theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getPanels().get(nPan - 1).getChordTip().getEstimatedValue()/2);
			y = (float) (_theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getSpan().getEstimatedValue()/2.)*1.005f;
			z = (float) (_theAirfoil.get_theLiftingSurface().get_Z0().getEstimatedValue()
					+ _yStation
					* Math.tan(_theAirfoil.get_theLiftingSurface().getLiftingSurfaceCreator().getDihedralAtYActual(_yStation).getEstimatedValue())); //TODO: add dihedral
		}

		return new PVector(x, y, z);
	}

	public Double get_thicknessOverChordUnit() {
		return _thicknessOverChordUnit;
	}

	public void set_thicknessOverChordUnit(Double _thicknessOverChordUnit) {
		this._thicknessOverChordUnit = _thicknessOverChordUnit;
	}

	public Double get_maximumThicknessOverChord() {
		return _maximumThicknessOverChord;
	}

	public void set_maximumThicknessOverChord(Double _maximumThicknessOverChord) {
		this._maximumThicknessOverChord = _maximumThicknessOverChord;
	}

	public Double get_radiusLE() {
		return _radiusLE;
	}

	public void set_radiusLE(Double _radiusLE) {
		this._radiusLE = _radiusLE;
	}

	public Amount<Angle> get_anglePhiTE() {
		return _anglePhiTE;
	}

	public void set_anglePhiTE(Amount<Angle> _anglePhiTE) {
		this._anglePhiTE = _anglePhiTE;
	}

	public Amount<Angle> get_twist() {
		return _twist;
	}

	public void set_twist(Amount<Angle> _twist) {
		this._twist = _twist;
	}

	public Double[] get_xCoords() {
		return _xCoords;
	}

	public void set_xCoords(Double[] _xCoords) {
		this._xCoords = _xCoords;
	}

	public Double[] get_yCoords() {
		return _yCoords;
	}

	public void set_yCoords(Double[] _yCoords) {
		this._yCoords = _yCoords;
	}

	public Double[] get_zCoords() {
		return _zCoords;
	}

	public void set_zCoords(Double[] _zCoords) {
		this._zCoords = _zCoords;
	}

	public Double get_yStation() {
		return _yStation;
	}

	public void set_etaLocation(Double _etaLocation) {
		this._yStation = _etaLocation;
	}

	public Double get_deltaYPercent() {
		return _deltaYPercent;
	}

	public void set_deltaYPercent(Double _deltaYPercent) {
		this._deltaYPercent = _deltaYPercent;
	}
	
	public List<PVector> get_coordinatesRight() {
		return _coordinatesRight;
	}

	@Override
	public String getId() {
		return _id;
	}
	
	public String getIdNew() {
		String id = _theAirfoil.getId() + "geo" + nGeo;
		nGeo++;
		return id;
	}

	public List<PVector> get_coordinatesLeft() {
		return _coordinatesLeft;
	}

	public double get_etaStation() {
		return _etaStation;
	}

	public List<Double> get_cornerPointsX() {
		return _cornerPointsX;
	}

	public void set_cornerPointsX(List<Double> _cornerPointsX) {
		this._cornerPointsX = _cornerPointsX;
	}

	public List<Double> get_cornerPointsZ() {
		return _cornerPointsZ;
	}

	public void set_cornerPointsZ(List<Double> _cornerPointsZ) {
		this._cornerPointsZ = _cornerPointsZ;
	}

	public Double get_camberRatio() {
		return _camberRatio;
	}

	public void set_camberRatio(Double _camberRatio) {
		this._camberRatio = _camberRatio;
	}

	public Amount<Length> get_chord() {
		return _chord;
	}

	public void set_chord(Amount<Length> _chord) {
		this._chord = _chord;
	}

}

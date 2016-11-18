package calculators.aerodynamics;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.atmosphere.SpeedCalc;
import standaloneutils.customdata.MyArray;
import standaloneutils.customdata.MyPoint;
import writers.JPADStaticWriteUtils;

/** 
 * The reference frame in the NASA TN is such
 * that the longitudinal axis, p, is positive from rear to
 * front, the lateral axis, q, is positive from left to
 * right wing and the transverse axis is positive 
 * in the direction of pilot's head to feet.
 * 
 * In this code x = -p and z = -r <---> p = -x and 
 * r = -z. For this reasons the sign of the arguments 
 * of fu, fv, fw functions is changed.
 * 
 * The method is unable to predict lifting surface
 * maximum lift coefficient.
 * 
 * THE CODE IS WRITTEN ASSUMING THAT EVERY LIFTING 
 * SURFACE IS SYMMETRIC WITH RESPECT TO THE pr (xz)
 * PLANE.
 * 
 * @author Lorenzo Attanasio
 * @see Nasa technical note D-5335
 * 
 */
public class NasaBlackwell {

	private double mach, altitude, surface, semispan, meanGeometricChord, vortexSemiSpan, vortexSemiSpanToSemiSpanRatio;
	private int nPointsSemispanWise;
	private List<MyPoint> _listVortexPoints;
	private List<MyPoint> _listControlPoints;
	private DecompositionSolver _linearSystemSolver;
	private RealMatrix _influenceMatrix;

	private MyArray yStationsNB, 
	_gammaDistribution = new MyArray(), 
	_ccLDistribution = new MyArray(), 
	_clAdditionalDistribution = new MyArray(), 
	_clTotalDistribution = new MyArray(),
	_gammaSignedDistribution = new MyArray(),
	_alphaDistribution = new MyArray();
	List<MyPoint> controlPoints, vortexPoints;

	private double[] yStations, dihedral, twist, alpha0l, yStationsActual, chordsVsYActual, xLEvsYActual;
	private double _cLCurrent;
	private double alphaCurrent;
	private double [][] influenceFactor;
	private double [] gamma;
	double [][] influenceMatrix;

	Amount<Angle> alphaInitial;




	public NasaBlackwell(
			double semispan, 
			double surface,
			double[] yStationsActual,
			double[] chordsVsYActual,
			double[] xLEvsYActual,
			double[] dihedral,
			double[] twist,
			double[] alpha0l,
			double vortexSemiSpanToSemiSpanRatio,
			double alpha,
			double mach,
			double altitude) {

		this.mach = mach;
		this.altitude = altitude;
		this.surface = surface;
		this.semispan = semispan;
		this.yStationsActual = yStationsActual;
		this.chordsVsYActual = chordsVsYActual;
		this.xLEvsYActual = xLEvsYActual;
		this.alpha0l = alpha0l;

		this.meanGeometricChord = surface/(2*semispan);
		this.vortexSemiSpanToSemiSpanRatio = vortexSemiSpanToSemiSpanRatio;
		this.nPointsSemispanWise = (int) (1./(2*vortexSemiSpanToSemiSpanRatio));

		// TODO change the following ifs
		if (twist.length != nPointsSemispanWise) this.twist = new double[nPointsSemispanWise];
		else this.twist = twist;

		if (dihedral.length != nPointsSemispanWise) this.dihedral = new double[nPointsSemispanWise];
		else this.dihedral = dihedral;

		vortexSemiSpan = vortexSemiSpanToSemiSpanRatio * semispan;
		yStations = MyArrayUtils.linspace(0., semispan, nPointsSemispanWise);
		yStationsNB = MyArray.createArray(MyArrayUtils.linspace(
				vortexSemiSpan,
				semispan - vortexSemiSpan,
				nPointsSemispanWise));

		prepareDiscreteSurface();
	}



	/**
	 * Evaluate influence coefficient for downwash velocity, w
	 * 
	 * @param xs
	 * @param y
	 * @param z
	 * @param s
	 * @param phi
	 */
	private double fw(double xs, double y, double z, double s, double phi) {

		return (-xs*cos(phi)/
				(pow(xs,2) + pow(z*cos(phi) - y*sin(phi),2)))
				* (
						((y + s*cos(phi))*cos(phi) + (z + s*sin(phi))*sin(phi))/
						sqrt( pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2) )

						- ((y - s*cos(phi))*cos(phi) + (z - s*sin(phi))*sin(phi))/
						sqrt( pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2) )
						)

				- ((y - s*cos(phi))/( pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)) )
				* (
						1 - ( xs/pow(
								pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)
								, 0.5) )
						)

				+ ((y + s*cos(phi))/( pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)) )
				* (
						1 - (xs/pow(
								pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)
								, 0.5))
						);
	}

	/**
	 * Evaluate influence coefficient for sidewash velocity, v
	 * 
	 * @param xs
	 * @param y
	 * @param z
	 * @param s
	 * @param phi
	 * @return
	 */
	private double fv(double xs, double y, double z, double s, double phi) {

		return (
				xs*sin(phi)/
				(pow(xs,2) + pow((z*cos(phi) - y*sin(phi)),2))
				)
				* (
						((y + s*cos(phi))*cos(phi) + (z + s*sin(phi))*sin(phi))/
						pow(
								( pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2) )
								,0.5)

						- ((y - s*cos(phi))*cos(phi) + (z - s*sin(phi))*sin(phi))/
						pow(
								( pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2) )
								,0.5)
						)

				- ((z - s*sin(phi))/(pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)))
				* (
						1 - xs
						/pow(
								pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)
								,0.5)
						)

				+ ((z + s*sin(phi))/(pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)))
				* (
						1 - xs
						/pow(
								pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)
								,0.5)
						);

	}

	/**
	 * 
	 * @param control
	 * @param vortex
	 * @param sn
	 * @param phin
	 * @return
	 */
	private double fv_ni_n(MyPoint control, MyPoint vortex, 
			double sn, double phin) {

		return fv(-(control.getX() - vortex.getX()), 
				control.getY() - vortex.getY(), 
				control.getZ() -vortex.getZ(), 
				sn, phin);
	}

	/**
	 * 
	 * @param control
	 * @param vortex
	 * @param sn
	 * @param phin
	 * @return
	 */
	private double fw_ni_n(MyPoint control, MyPoint vortex, 
			double sn, double phin) {

		return fw(-(control.getX() - vortex.getX()), 
				control.getY() - vortex.getY(), 
				control.getZ() - vortex.getZ(), 
				sn, phin);
	}

	/**
	 * Wrapper function for populating easily the influence
	 * matrix considering the wing is symmetric with respect
	 * to the xz plane
	 * 
	 * @param pni
	 * @param pn
	 * @param qni
	 * @param qn
	 * @param rni
	 * @param rn
	 * @param sn
	 * @param phin
	 * @return
	 */
	private double fvSigned_ni_n(double pni, double pn,
			double qni, double qn,
			double rni, double rn,
			double sn, double phin) {

		return fv(-(pni-pn), qni-qn, rni-rn, sn, phin) 
				+ fv(-(pni-pn), qni+qn, rni-rn, sn, -phin); 
	}

	/*
	 * Wrapper function for populating easily the influence
	 * matrix considering the wing is symmetric with respect
	 * to the xz plane
	 */
	private double fwSigned_ni_n(double pni, double pn, 
			double qni, double qn, 
			double rni, double rn, 
			double sn, double phin) {

		return fw(-(pni-pn), qni-qn, rni-rn, sn, phin) 
				+ fw(-(pni-pn), qni+qn, rni-rn, sn, -phin); 

	}

	/** 
	 * 
	 * @param control
	 * @param vortex
	 * @param sn
	 * @param phin
	 * @return
	 */
	private double fvSigned_ni_n(MyPoint control, MyPoint vortex, 
			double sn, double phin){

		return fvSigned_ni_n(control.getX(), vortex.getX(), 
				control.getY(), vortex.getY(), 
				control.getZ(), vortex.getZ(), 
				sn, phin);
	}

	/**
	 * 
	 * @param control
	 * @param vortex
	 * @param sn
	 * @param phin
	 * @return
	 */
	private double fwSigned_ni_n(MyPoint control, MyPoint vortex, 
			double sn, double phin){

		return fwSigned_ni_n(control.getX(), vortex.getX(), 
				control.getY(), vortex.getY(), 
				control.getZ(), vortex.getZ(), 
				sn, phin);
	}

	/* Locate points where vortex are placed along the semispan */
	private List<MyPoint> getVortexPoints() {

		List<MyPoint> list = new ArrayList<MyPoint>();

		for (int i=0; i < nPointsSemispanWise; i++) {
			MyPoint point = new MyPoint();
			point.setX((GeometryCalc.getXLEAtYActual(yStationsActual, xLEvsYActual, yStationsNB.get(i)) 
					+ 0.25*GeometryCalc.getChordAtYActual(yStationsActual, chordsVsYActual, yStationsNB.get(i)))
					/sqrt(1 - pow(mach,2.)));

			point.setY(yStationsNB.get(i));
			point.setZ(tan(dihedral[i])*yStationsNB.get(i));
			list.add(point);
		}

		return list; 
	}

	/* Locate points where control points are placed along the semispan */
	private List<MyPoint> getControlPoints() {

		List<MyPoint> list = new ArrayList<MyPoint>();

		for (int i=0; i<nPointsSemispanWise; i++) {
			MyPoint point = new MyPoint();
			point.setX((GeometryCalc.getXLEAtYActual(yStationsActual, xLEvsYActual, yStationsNB.get(i)) 
					+ 0.75*GeometryCalc.getChordAtYActual(yStationsActual, chordsVsYActual, yStationsNB.get(i)))
					/sqrt(1 - pow(mach,2.)));

			point.setY(yStationsNB.get(i));

			point.setZ(tan(dihedral[i])*yStationsNB.get(i));
			list.add(point);
		}

		return list; 
	}

	private void prepareDiscreteSurface() {
		_listVortexPoints = getVortexPoints();
		_listControlPoints = getControlPoints();
	}

	/** 
	 * Populate the influence matrix 
	 * 
	 * @return
	 */
	private void buildInfluenceMatrix() {

		influenceMatrix = new double[nPointsSemispanWise][nPointsSemispanWise];

		// i = row counter; in a row the control point is fixed
		for(int i=0; i < nPointsSemispanWise; i++) {

			// j = column counter; in a column the vortex point is fixed 
			for(int j=0; j < nPointsSemispanWise; j++) {
				if (_listControlPoints == null){
					_listControlPoints = controlPoints;
				};
				if (_listVortexPoints == null){
					_listVortexPoints  = vortexPoints;
				};
				influenceMatrix[i][j] = 
						fwSigned_ni_n(_listControlPoints.get(i), _listVortexPoints.get(j), 
								vortexSemiSpan, 
								dihedral[j])
						- fvSigned_ni_n(_listControlPoints.get(i), _listVortexPoints.get(j), 
								vortexSemiSpan, 
								dihedral[j])*tan(dihedral[i]);
			}
		}

		_influenceMatrix = new Array2DRowRealMatrix(influenceMatrix);
	}

	private void solveSystem() {
		_linearSystemSolver = new LUDecomposition(_influenceMatrix).getSolver();

		// Solve linear system
		_gammaSignedDistribution.setRealVector(
				_linearSystemSolver.solve(_alphaDistribution.getRealVector()));
		_gammaSignedDistribution.add(0.0);
		_gammaSignedDistribution.toArray();

		// Scale for actual airfoil mean Clalpha
		//				_gammaSignedCurrent = MyArray.createArray(
		//						_gammaSignedCurrent.times(calculateCLAlpha.integralMean2D()/(2.*Math.PI)));
	}

	/** */
	private void prepareSystemSolution() {
		buildInfluenceMatrix();
		solveSystem();
	}


	private double calculateCLOverall() {
		double sum = 0.;

		for(int i=0; i < nPointsSemispanWise+1; i++) {
			sum = sum + _gammaSignedDistribution.get(i)*2.*vortexSemiSpan;
		}

		_cLCurrent = (16*Math.PI/surface) * sum;

		//JPADStaticWriteUtils.logToConsole("\nCL lifting surface: " + _cLCurrent + "\n");

		return _cLCurrent;
	}

	private double[] calculateLoadingDistribution() {

		double sum = 0.;

		_gammaDistribution.clear();
		_ccLDistribution.clear();
		_clAdditionalDistribution.clear();
		_clTotalDistribution.clear();
		MyArray yy = yStationsNB.clone();
		yy.add(semispan);
		yy.toArray();

		for(int i=0; i < nPointsSemispanWise+1; i++) {
			sum = sum + _gammaSignedDistribution.get(i)*2*vortexSemiSpan;
		}

		//				for(int i=0; i < nPointsSemispanWise; i++) {
		//					_gammaCurrent.add(
		//							4*Math.PI
		//							*operatingConditions.get_tas().getEstimatedValue()
		//							*_gammaSignedCurrent.get(i));
		//					_ccLCurrent.add(_gammaCurrent.get(i)*2/operatingConditions.get_tas().getEstimatedValue());
		//					_clAdditionalDistributionCurrent.add(_ccLCurrent.get(i)/liftingSurface.getChordAtYActual(yStations[i]));
		//				}

		for(int i=0; i < nPointsSemispanWise+1; i++) {
			_gammaDistribution.add(
					4 * Math.PI * SpeedCalc.calculateTAS(mach, altitude)
					* _gammaSignedDistribution.get(i));

			_ccLDistribution.add(
					_cLCurrent * meanGeometricChord * semispan * _gammaSignedDistribution.get(i)
					/sum);

			_clAdditionalDistribution.add(
					_ccLDistribution.get(i)/
					GeometryCalc.getChordAtYActual(yStationsActual, chordsVsYActual, yy.get(i)));
		}

		_gammaDistribution = _gammaDistribution.interpolate(yy.toArray(), yStations).clone();
		_ccLDistribution = _ccLDistribution.interpolate(yy.toArray(), yStations).clone();
		_clAdditionalDistribution = _clAdditionalDistribution.interpolate(yy.toArray(), yStations).clone();

		_clTotalDistribution = _clAdditionalDistribution.clone();

		//TODO uncomment

		//		JPADStaticWriteUtils.logToConsole("\nCl distribution: " + Arrays.toString(_clAdditionalDistribution.toArray())
		//				+ "\nc*cl distribution: " + Arrays.toString(_ccLDistribution.toArray()) 
		//				+ "\nLoad distribution: " + Arrays.toString(_gammaSignedDistribution.times(semispan/sum)) 
		//				+ "\n");

		return _gammaSignedDistribution.times(semispan/sum);
	}

	public void calculate(Amount<Angle> alpha) {
		// prepareDiscreteSurface();
		alphaCurrent = alpha.doubleValue(NonSI.DEGREE_ANGLE);
		_alphaDistribution = new MyArray(AnglesCalc.getAlphaDistribution(
				alpha.doubleValue(SI.RADIAN),
				twist, 
				alpha0l));

		prepareSystemSolution();
		calculateCLOverall();
		calculateLoadingDistribution();
	}
	/**
	 * This method evaluates Gamma and influence factor which are useful in order to evaluate the effective
	 * angle of attack in n control point among semispan due to downwash.
	 * 
	 * @author Manuela Ruocco
	 * @param Amount<Angle> alpha : Initial angle of attack in radians 
	 */
	public void calculateVerticalVelocity(Amount<Angle> alpha) {

		influenceFactor = new double [nPointsSemispanWise] [nPointsSemispanWise];
		gamma = new double [nPointsSemispanWise];

		controlPoints = get_listVortexPoints();
		vortexPoints = get_listVortexPoints();
		for ( int i=0 ; i<nPointsSemispanWise; i++){
			for(int j=0 ; j<nPointsSemispanWise; j++){
				influenceFactor[i][j] = fwSigned_ni_n(
						_listControlPoints.get(i), _listVortexPoints.get(j), 
						vortexSemiSpan, 
						dihedral[j]);
				
			}
		}

		calculate(alpha);
		
		gamma = getGammaDistribution().toArray();
//		System.out.println(" gamma " + Arrays.toString(gamma));


	}

	public double[][] getInfluenceFactor() {
		return influenceFactor;
	}

	public double[] getGamma() {
		return gamma;
	}

	public double get_cLEvaluated() {
		return _cLCurrent;
	}

	public double getMach() {
		return mach;
	}

	public void setMach(double mach) {
		this.mach = mach;
	}

	public int getnPointsSemispanWise() {
		return nPointsSemispanWise;
	}

	public double[] getyStations() {
		return yStations;
	}

	public List<MyPoint> get_listVortexPoints() {
		return _listVortexPoints;
	}

	public List<MyPoint> get_listControlPoints() {
		return _listControlPoints;
	}

	public double getCLCurrent() {
		return _cLCurrent;
	}

	public double getAlphaCurrent() {
		return alphaCurrent;
	}

	public MyArray get_alphaDistribution() {
		return _alphaDistribution;
	}

	public MyArray get_gammaSignedDistribution() {
		return _gammaSignedDistribution;
	}

	public MyArray getGammaDistribution() {
		return _gammaDistribution;
	}

	public MyArray get_ccLDistribution() {
		return _ccLDistribution;
	}

	public MyArray getClAdditionalDistribution() {
		return _clAdditionalDistribution;
	}

	public MyArray getClTotalDistribution() {
		return _clTotalDistribution;
	}



	public double[][] getInfluenceMatrix() {
		return influenceMatrix;
	}



	public void setInfluenceMatrix(double[][] influenceMatrix) {
		this.influenceMatrix = influenceMatrix;
	}

} // end of NasaBlackwell

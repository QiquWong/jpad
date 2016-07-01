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
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.poi.util.ArrayUtil;
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
	double [] sweepAngle;

	Amount<Angle> alphaInitial;

	List<Double> fwUno = new ArrayList<Double>();
	List<Double> fwDue = new ArrayList<Double>();
	List<Double> fwTre = new ArrayList<Double>();
	List<Double> fwQuattro = new ArrayList<Double>(); 
	List<Double> fwCinque= new ArrayList<Double>(); 
	List<Double>fwSei= new ArrayList<Double>();
	List<Double>fwSette =new ArrayList<Double>();



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
		this.alphaCurrent = alpha;
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
		
		sweepAngle= new double [nPointsSemispanWise];
		
		for (int i=0; i<nPointsSemispanWise; i++){
			sweepAngle[i] =- Math.atan(xLEvsYActual[i]/yStationsActual[i]);
		}
		sweepAngle[0] = sweepAngle[1];

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
		
		double fwUno = (xs*cos(phi)/
				(pow(xs,2) + pow(z*cos(phi) - y*sin(phi),2)));
		
		double due = 	((y + s*cos(phi))*cos(phi) + (z + s*sin(phi))*sin(phi))/
				sqrt( pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2) );
		
		double tre = ((y - s*cos(phi))*cos(phi) + (z - s*sin(phi))*sin(phi))/
				sqrt( pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2) )
				
				;
				
		double quattro = 	((y - s*cos(phi))/( pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)) );
		
		double cinque = ( xs/pow(
				pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)
				, 0.5) );
		
		double sei = ((y + s*cos(phi))/( pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)) );
		
		double sette = (xs/pow(
				pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)
				, 0.5));
		
		if ( phi <0)
		{ this.fwUno.add(fwUno);
		this.fwDue.add(due);
		this.fwTre.add(tre);
		this.fwQuattro.add(quattro);
		this.fwCinque.add(cinque);
		this.fwSei.add(sei);
		this.fwSette.add(sette);
		
		}
		
		
//			System.out.println("\nuno " + uno);
//			System.out.println("due " + due );
//			System.out.println("tre " + tre);
//			System.out.println("quattro " + quattro);
//			System.out.println("cinque " + cinque);
//			System.out.println("sei " + sei );
//			System.out.println("sette " + sette);
		
//		System.out.println(" FW UNO " + fwUno);

		
		double fwFactor = (-xs*cos(phi)/
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
		
//		System.out.println("\n Fw " + fwFactor);

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

		double uno = (
				xs*sin(phi)/
				(pow(xs,2) + pow((z*cos(phi) - y*sin(phi)),2))
				);
		
		double due = 	(
				((y + s*cos(phi))*cos(phi) + (z + s*sin(phi))*sin(phi))/
				pow(
						( pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2) )
						,0.5));
		
		double tre = ((y - s*cos(phi))*cos(phi) + (z - s*sin(phi))*sin(phi))/
				pow(
						( pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2) )
						,0.5)
				;
				
		double quattro = 	((z - s*sin(phi))/(pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)));
		
		double cinque = (xs
				/pow(
						pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)
						,0.5));
		
		double sei = ((z + s*sin(phi))/(pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)));
		
		double sette = (xs
				/pow(
						pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)
						,0.5));
		
		
//			System.out.println("\nuno " + uno);
//			System.out.println("due " + due );
//			System.out.println("tre " + tre);
//			System.out.println("quattro " + quattro);
//			System.out.println("cinque " + cinque);
//			System.out.println("sei " + sei );
//			System.out.println("sette " + sette);
//			
			double fvfinal = (
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

					+ ((z - s*sin(phi))/(pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)))
					* (
							1 - (xs
							/pow(
									pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)
									,0.5))
							) 

					- ((z + s*sin(phi))/(pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)))
					* (
							1 - (xs
							/pow(
									pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)
									,0.5))
							);
			
//			System.out.println(" FV " + fvfinal);
		
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

				+ ((z - s*sin(phi))/(pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)))
				* (
						1 - (xs
						/pow(
								pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2)
								,0.5))
						) 

				- ((z + s*sin(phi))/(pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)))
				* (
						1 - (xs
						/pow(
								pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2)
								,0.5))
						);
		

	}

	
private double fu(double xs, double y, double z, double s, double phi) {
		
		double fuFactor = ((z*cos(phi)-y*sin(phi))/
				(pow(xs,2) + pow(z*cos(phi) - y*sin(phi),2)))
				* (
						((y + s*cos(phi))*cos(phi) + (z + s*sin(phi))*sin(phi))/
						sqrt( pow(xs,2) + pow(y + s*cos(phi),2) + pow(z + s*sin(phi),2) )

						- ((y - s*cos(phi))*cos(phi) + (z - s*sin(phi))*sin(phi))/
						sqrt( pow(xs,2) + pow(y - s*cos(phi),2) + pow(z - s*sin(phi),2) )
						);
		
//		System.out.println("\n FU " + fuFactor);
		
		return fuFactor ;
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

//		System.out.println("\n\nfv ");
		
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
	
	/*
	 * Wrapper function for populating easily the influence
	 * matrix considering the wing is symmetric with respect
	 * to the xz plane
	 */
	private double fuSigned_ni_n(double pni, double pn, 
			double qni, double qn, 
			double rni, double rn, 
			double sn, double phin) {

		return fu(-(pni-pn), qni-qn, rni-rn, sn, phin) 
				+ fu(-(pni-pn), qni+qn, rni-rn, sn, -phin); 

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

	

	/**
	 * 
	 * @param control
	 * @param vortex
	 * @param sn
	 * @param phin
	 * @return
	 */
	private double fuSigned_ni_n(MyPoint control, MyPoint vortex, 
			double sn, double phin){

		return fuSigned_ni_n(control.getX(), vortex.getX(), 
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
			
//			System.out.println(" chord at " + i + " " +  GeometryCalc.getChordAtYActual(yStationsActual, chordsVsYActual, yStationsNB.get(i)));
//			System.out.println(" xle d at " + i + " " + GeometryCalc.getXLEAtYActual(yStationsActual, xLEvsYActual, yStationsNB.get(i)));
			
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
//			System.out.println("-------------------------------------------");
//			System.out.println("\n control point num " + i);

			// j = column counter; in a column the vortex point is fixed 
			for(int j=0; j < nPointsSemispanWise; j++) {
//				System.out.println("\n vortex point num " + j );
//				System.out.println("-------------------------------------------");
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
		
//		System.out.println("\n\n CONTROL POINT X");
//		for (int i=0; i<_listControlPoints.size(); i++){
//			System.out.println(_listControlPoints.get(i).getX());
//		}
//		System.out.println("\n\n CONTROL POINT Y");
//		for (int i=0; i<_listControlPoints.size(); i++){
//			System.out.println(_listControlPoints.get(i).getY());
//		}
//		System.out.println("\n\n CONTROL POINT z");
//		for (int i=0; i<_listControlPoints.size(); i++){
//			System.out.println(_listControlPoints.get(i).getZ());
//		}
//		
//		
//		System.out.println("\n\n VORTEX POINT X");
//		for (int i=0; i<_listControlPoints.size(); i++){
//			System.out.println(_listVortexPoints.get(i).getX());
//		}
//		System.out.println("\n\n VORTEX POINT Y");
//		for (int i=0; i<_listControlPoints.size(); i++){
//			System.out.println(_listVortexPoints.get(i).getY());
//		}
//		System.out.println("\n\n VORTEX POINT z");
//		for (int i=0; i<_listControlPoints.size(); i++){
//			System.out.println(_listVortexPoints.get(i).getZ());
//		}
		
		System.out.println();
	}
	
	private void buildInfluenceMatrixFu() {

		influenceMatrix = new double[nPointsSemispanWise][nPointsSemispanWise];

		System.out.println(" sweep length " + sweepAngle.length);
		System.out.println(" sweep angle " + Arrays.toString(sweepAngle));
		
		// i = row counter; in a row the control point is fixed
		for(int i=0; i < nPointsSemispanWise; i++) {
//			System.out.println("-------------------------------------------");
//			System.out.println("\n control point num " + i);

			// j = column counter; in a column the vortex point is fixed 
			for(int j=0; j < nPointsSemispanWise; j++) {
//				System.out.println("\n vortex point num " + j );
//				System.out.println("-------------------------------------------");
				if (_listControlPoints == null){
					_listControlPoints = controlPoints;
				};
				if (_listVortexPoints == null){
					_listVortexPoints  = vortexPoints;
				};
				influenceMatrix[i][j] = 
						fuSigned_ni_n(_listControlPoints.get(i), _listVortexPoints.get(j), 
								vortexSemiSpan, 
								dihedral[j])*Math.sin(sweepAngle[i])
						- fvSigned_ni_n(_listControlPoints.get(i), _listVortexPoints.get(j), 
								vortexSemiSpan, 
								dihedral[j])*Math.cos(sweepAngle[i]);
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
//		System.out.println("GAMMA SIGNED ");
		for (int i=0; i<_gammaDistribution.size(); i++){
//			System.out.println(_gammaSignedDistribution.get(i));
		}

		// Scale for actual airfoil mean Clalpha
		//				_gammaSignedCurrent = MyArray.createArray(
		//						_gammaSignedCurrent.times(calculateCLAlpha.integralMean2D()/(2.*Math.PI)));
	}
	
	private void solveSystemFu() {
		_linearSystemSolver = new LUDecomposition(_influenceMatrix).getSolver();

		double [] sweepAngleWithSin = new double [nPointsSemispanWise];
		
		for (int i=0; i<nPointsSemispanWise; i++){
			sweepAngleWithSin[i] = Math.sin(sweepAngle[i]);
		}
		// Solve linear system
		
		_gammaSignedDistribution.setRealVector(
				_linearSystemSolver.solve(MatrixUtils.createRealVector(sweepAngleWithSin)));
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

//		System.out.println(" ccl distr ");
//		for (int i=0; i<_ccLDistribution.size();i++){
//			System.out.println(_ccLDistribution.get(i));
//		}
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

		_alphaDistribution = new MyArray(AnglesCalc.getAlphaDistribution(
				alpha.doubleValue(SI.RADIAN),
				twist, 
				alpha0l));
		
//		_alphaDistribution = new MyArray();
		
//		for (int i=0; i<twist.length; i++){
//			_alphaDistribution.set(i, alpha.doubleValue(SI.RADIAN)+twist[i]);
//		}
		
		
//		System.out.println( "alpha distribution " + _alphaDistribution.toString());

		
		List<Double> fwUno = new ArrayList<Double>();
		List<Double> fwDue = new ArrayList<Double>();
		List<Double> fwTre = new ArrayList<Double>();
		List<Double> fwQuattro = new ArrayList<Double>(); 
		List<Double> fwCinque= new ArrayList<Double>(); 
		List<Double>fwSei= new ArrayList<Double>();
		List<Double>fwSette =new ArrayList<Double>();
		
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
	public void calculateDownwash(Amount<Angle> alpha) {

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
		
		gamma = get_gammaDistribution().toArray();
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

	public double get_cLCurrent() {
		return _cLCurrent;
	}

	public double get_alphaCurrent() {
		return alphaCurrent;
	}

	public MyArray get_alphaDistribution() {
		return _alphaDistribution;
	}

	public MyArray get_gammaSignedDistribution() {
		return _gammaSignedDistribution;
	}

	public MyArray get_gammaDistribution() {
		return _gammaDistribution;
	}

	public MyArray get_ccLDistribution() {
		return _ccLDistribution;
	}

	public MyArray get_clAdditionalDistribution() {
		return _clAdditionalDistribution;
	}

	public MyArray get_clTotalDistribution() {
		return _clTotalDistribution;
	}



	public double[][] getInfluenceMatrix() {
		return influenceMatrix;
	}



	public void setInfluenceMatrix(double[][] influenceMatrix) {
		this.influenceMatrix = influenceMatrix;
	}



	public MyArray getyStationsNB() {
		return yStationsNB;
	}



	public void setyStationsNB(MyArray yStationsNB) {
		this.yStationsNB = yStationsNB;
	}



	public List<Double> getFwUno() {
		return fwUno;
	}



	public List<Double> getFwDue() {
		return fwDue;
	}



	public List<Double> getFwTre() {
		return fwTre;
	}



	public List<Double> getFwQuattro() {
		return fwQuattro;
	}



	public List<Double> getFwCinque() {
		return fwCinque;
	}



	public List<Double> getFwSei() {
		return fwSei;
	}



	public List<Double> getFwSette() {
		return fwSette;
	}

} // end of NasaBlackwell

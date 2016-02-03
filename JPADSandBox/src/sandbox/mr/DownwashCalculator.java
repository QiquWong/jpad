package sandbox.mr;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import configuration.MyConfiguration;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

public class DownwashCalculator {


	// VARIABLE DECLARATION

	private double bRLineZeroLiftLine;
	private double distTrailingEdgeWingXACH;
	Aircraft aircraft;
	double zHTailAC, cRootExposedWing , angleOfIncidenceExposed, zWing, distAerodynamicCenter,
	alphaZeroLiftRootExposed, xACLRF, xACRootExposed, angleOfIncidenceExposedDeg,
	alphaZeroLiftRootExposedDeg;
	private double[] downwashArray;
	private double[] alphaArray ;
	private double[] downwashGradientArray;
	private double[] zDistanceArray;
	private String subfolderPath;
	private boolean subfolderPathCeck = true;
	private boolean plotEpsilonCeck = false;
	private boolean plotDeltaEpsilonCeck = false;
	private boolean plotZCeck = false;
	String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
	private int nValuePlot;
	private double deltaAlpha;


	//BUILDER

	public DownwashCalculator(
			Aircraft aircraft
			) {

		this.aircraft = aircraft;


		zHTailAC = aircraft.get_HTail().get_Z0().getEstimatedValue();
		cRootExposedWing = aircraft.get_exposedWing().get_theAirfoilsListExposed()
				.get(0).get_chordLocal();
		angleOfIncidenceExposed = aircraft.get_wing().get_iw().getEstimatedValue()
				+ aircraft.get_exposedWing().get_twistDistributionExposed().get(0);
		angleOfIncidenceExposedDeg = Amount.valueOf(
				Math.toDegrees(angleOfIncidenceExposed), SI.RADIAN).getEstimatedValue();

		zWing = aircraft.get_wing().get_Z0().getEstimatedValue();
		distAerodynamicCenter = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		alphaZeroLiftRootExposed = aircraft.get_exposedWing().get_alpha0lDistributionExposed().get(0);
		alphaZeroLiftRootExposedDeg = Amount.valueOf(
				Math.toDegrees(alphaZeroLiftRootExposed), SI.RADIAN).getEstimatedValue();

		LSAerodynamicsManager.CalcXAC theXACCalculator = aircraft.get_wing().getAerodynamics().new CalcXAC();
		xACLRF = theXACCalculator.deYoungHarper() + aircraft.get_wing().get_xLEMacActualLRF().getEstimatedValue();
		xACRootExposed = xACLRF - aircraft.get_wing().getXLEAtYActual(aircraft.get_fuselage().getWidthAtX(
				aircraft.get_wing().get_xLEMacActualBRF().getEstimatedValue()).doubleValue());
	}


	/**
	 * This method calculates the downwash gradient using Delft formula. The downwash gradient is
	 * considered as constant. The distances considered in the formula are geometric and fixed.
	 *
	 * Distance along X axis -- > Distance between the points at c/4 of the mean
	 *	                          aerodynamic chord of the wing and the same point
	 *	                          of the horizontal tail.
	 *
	 * Distance along Z axis -- >Distance between the horizontal tail the vortex
	 *                           shed plane, which can be approximated with the plane
	 *                           from the wing root chord.
	 *
	 * @param Aircraft
	 * @param distVortexPlane Distance between the horizontal tail the vortex
	 * shed plane, which can be approximated with the plane from the wing root chord.
	 *
	 * @author  Manuela Ruocco
	 */

	//	public double calculateDownwashDelft(Aircraft aircraft, double distAerodynamicCenter,double distVortexPlane,
	//			double clAlfa, double wingSpan, double sweepQuarterChordEq ){}


	public double calculateDownwashGradientLinearDelft(double distVortexPlane){

		double downwashGradientLinear;
		double distAerodynamicCenter; // Distance between the points at c/4 of the mean
		// aerodynamic chord of the wing and the same point
		// of the horizontal tail.

		distAerodynamicCenter = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		double semiWingSpan = aircraft.get_exposedWing().get_semispan().getEstimatedValue();
		double clAlfa = aircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();
		double sweepQuarterChordEq = aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue();
		double aspectRatio = aircraft.get_exposedWing().get_aspectRatio();

		double keGamma, keGammaZero;

		double r=distAerodynamicCenter/semiWingSpan;
		double rPow=Math.pow(r,2);
		double m=distVortexPlane/semiWingSpan;
		double mpow=Math.pow(m, 2);

		keGamma=(0.1124+0.1265*sweepQuarterChordEq+0.1766*Math.pow(sweepQuarterChordEq,2))
				/rPow+0.1024/r+2;
		keGammaZero=0.1124/rPow+0.1024/r+2;

		double kFraction=keGamma/keGammaZero;
		double first= (r/(rPow+ mpow))*(0.4876/Math.sqrt(rPow+0.6319+mpow));
		double second= 1+Math.pow(rPow/(rPow+0.7915+5.0734*mpow),0.3113);
		double third = 1-Math.sqrt(mpow/(1+mpow));

		downwashGradientLinear=kFraction*(first+second*third)*(clAlfa/(Math.PI*aspectRatio));

		return downwashGradientLinear;

	}

	/**
	 * This method calculates the downwash gradient using Delft formula. The downwash gradient is
	 * considered variable in alpha absolute. The distance along x considered in the formula
	 * is geometric and fixed. Conversely the distance along z is variable and it is considered as
	 * the Distance between the horizontal tail the vortex shed plane.
	 *
	 * Distance along X axis -- > Distance between the points at c/4 of the mean
	 *	                          aerodynamic chord of the wing and the same point
	 *	                          of the horizontal tail.
	 *
	 * Distance along Z axis -- >Distance between the horizontal tail the vortex
	 *                           shed plane.
	 *
	 * @param Amount<Angle> alpha Body in degree or radians
	 *
	 *
	 * @author  Manuela Ruocco
	 */


	// This method evaluates the downwash at alpha body considering a variable downwash gradient.
	// In order to evaluate this value it's necessary to implement an iterative process when the
	// value of downwash at alpha depends on the value at previous step.
	// This method accepts an alpha body as input and it calculates the downwash gradient and
	// the relative value of downwash for a variable number of step until alpha actual is equal to
	// alpha body, starting from an alpha such that alpha absolute = 0.
	// When alpha absolute =0, in fact, the value of downwash is assumed zero, but the downwash gradient
	// is not null.
	// This value of downwash gradien is used in the following step as effort value in order to
	// calculate a new distance along z axis and a new value of downwash gradient.


	public double calculateDownwashNonLinearDelftAtAlpha( Amount<Angle> alphaBody){

		if (alphaBody.getUnit() == SI.RADIAN)
			alphaBody = alphaBody.to(NonSI.DEGREE_ANGLE);

		// Variable Declaration

		List<Double> alphaAbsoluteList = new ArrayList<>();
		List<Double> alphaBodyList = new ArrayList<>();
		List<Double> zDistanceList = new ArrayList<>();
		List<Double> downwashGradientList = new ArrayList<>();
		List<Double> downwashList = new ArrayList<>();

		double alphaBodyActual, alphaAbsoluteActual = 0.0, downwashActual, downwashGradientActual,
				downwashGradientTemp, zDistanceActual, downwashRadian;

		// Let's define an array of alpha absolute, starting form alpha =0
		double alphaFirst = 0.0;
		double alphaLast = 20.0;
		int nValue = (int) Math.ceil(( alphaLast - alphaFirst ) * 4); //0.25 deg

		double [] alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue);
		deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1]), SI.RADIAN).getEstimatedValue();

		// First step
		alphaAbsoluteActual = alphaAbsoluteArray[0];
		alphaAbsoluteList.add(0, 0.0);
		//		System.out.println("\n\n---------------");
		//		System.out.println(" alpha body real " + alphaBody);
		//		System.out.println(" angle of incidence " + angleOfIncidenceExposedDeg);
		//		System.out.println(" alpha zero Lift " + alphaZeroLiftRootExposedDeg);
		alphaBodyActual = - (angleOfIncidenceExposedDeg - alphaZeroLiftRootExposedDeg);
		//		System.out.println(" alpha body actual " + alphaBodyActual);
		alphaBodyList.add(0, alphaBodyActual);
		double distVortexPlane = distanceZeroLiftLineACHorizontalTail();
		zDistanceList.add(0, distVortexPlane);
		downwashGradientTemp = calculateDownwashGradientLinearDelft(distVortexPlane);
		downwashGradientList.add(0, downwashGradientTemp);
		downwashList.add(0, 0.0);

		int i=1;

		// Other step

		while (alphaBodyActual <= alphaBody.getEstimatedValue() ){

			alphaAbsoluteActual = alphaAbsoluteArray[i];
			alphaAbsoluteList.add(i, alphaAbsoluteActual);

			alphaBodyActual = alphaAbsoluteActual - (angleOfIncidenceExposedDeg - alphaZeroLiftRootExposedDeg);
			alphaBodyList.add(i, alphaBodyActual);

			downwashGradientTemp =  downwashGradientList.get(i-1);
			downwashActual = downwashGradientTemp  * alphaAbsoluteActual; //* deltaAlpha;
//			downwashActual = downwashList.get(i-1) +
//					downwashGradientTemp  * deltaAlpha; // alphaAbsoluteActual; //* deltaAlpha;
			//alphaAbsoluteActual;
			downwashRadian = Amount.valueOf(
					Math.toRadians(downwashActual), SI.RADIAN).getEstimatedValue();

			zDistanceActual = zDistanceList.get(i-1) -
					distTrailingEdgeWingXACH * Math.tan(
							angleOfIncidenceExposed + alphaZeroLiftRootExposed - deltaAlpha + downwashRadian);
			zDistanceList.add(i, zDistanceActual);

			downwashGradientActual = calculateDownwashGradientLinearDelft(zDistanceActual);
			downwashGradientList.add(i, downwashGradientActual);

			downwashActual = downwashGradientActual * alphaAbsoluteActual;
//			downwashActual = downwashList.get(i-1) +
//					downwashGradientActual  * deltaAlpha; // alphaAbsoluteActual; //* deltaAlpha;
			downwashList.add(i, downwashActual);

			i=i+1;
		}


		//System.out.println("Results ------------------");
		if(plotEpsilonCeck == false ){
			System.out.println("\nAlpha Absolute (deg) " + alphaAbsoluteList.toString());
			System.out.println("Alpha Body (deg)" + alphaBodyList.toString());
			System.out.println("Z Distance Absolute (m) " + zDistanceList.toString());
			System.out.println("DownwashGradient " + downwashGradientList.toString());
			System.out.println("Downwash Angle (deg) " + downwashList.toString());
		}

		if(plotEpsilonCeck == true ){
			for (int j=0; j<nValuePlot ; j++ ){
				alphaArray[j] = alphaBodyList.get(j);
				downwashArray[j] = downwashList.get(j);

			}}


		if(plotDeltaEpsilonCeck == true ){
			for (int j=0; j<nValuePlot ; j++ ){
				alphaArray[j] = alphaBodyList.get(j);
				downwashGradientArray [j] = downwashGradientList.get(j);
			}}


		if(plotZCeck == true ){
			for (int j=0; j<nValuePlot ; j++ ){
				alphaArray[j] = alphaBodyList.get(j);
				zDistanceArray [j] = zDistanceList.get(j);
			}}

		return downwashList.get(downwashList.size()-1);
	}


	public void plotDownwashDelft(){
		Amount<Angle> alpha = Amount.valueOf(Math.toRadians(15), SI.RADIAN);
		double alphaDouble = alpha.to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		nValuePlot = (int) Math.ceil(( alphaDouble - 0.0) * 4); //0.25 deg
		downwashArray = new double [nValuePlot];
		alphaArray = new double [nValuePlot];
		downwashGradientArray =new double [nValuePlot];
		zDistanceArray = new double [nValuePlot];

		plotEpsilonCeck = true;

		calculateDownwashNonLinearDelftAtAlpha(alpha);

		plotEpsilonCeck = false;

		double [] epsilonNonLinearArray = getDownwashArray();
		double [] alphaArray = getAlphaArray();

		double [] epsilonLinearArray = new double [alphaArray.length];
		double distanceZeroLiftPlane = distanceZeroLiftLineACHorizontalTail();
		double downwashGradientLinear = calculateDownwashGradientLinearDelft(distanceZeroLiftPlane);
		double xInitialLinear = - (angleOfIncidenceExposedDeg - alphaZeroLiftRootExposedDeg);
		double q = - downwashGradientLinear * xInitialLinear;

		for (int i=0 ; i<epsilonLinearArray.length ; i++){
			epsilonLinearArray[i] = downwashGradientLinear * alphaArray [i]+ q;
		}

		double [][] epsilonMatrix ={epsilonLinearArray,epsilonNonLinearArray};



		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaArray, epsilonMatrix,
				null, null, null, null,
				"alpha_Body", "epsilon",
				" deg ", "deg",
				legend,subfolderPath,
				"Epsilon vs Alpha Body");


	}


	public void plotDownwashDelftWithPath(String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotDownwashDelft();
		subfolderPathCeck = true;
	}


	public void plotDownwashGradientDelft(){
		Amount<Angle> alpha = Amount.valueOf(Math.toRadians(15), SI.RADIAN);
		double alphaDouble = alpha.to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		nValuePlot = (int) Math.ceil(( alphaDouble - 0.0) * 4); //0.25 deg
		alphaArray = new double [nValuePlot];
		downwashGradientArray =new double [nValuePlot];

		plotDeltaEpsilonCeck = true;

		calculateDownwashNonLinearDelftAtAlpha(alpha);

		plotDeltaEpsilonCeck = false;

		double [] downwashGradientNonLinearArray = getDownwashGradientArray();
		double [] alphaArray = getAlphaArray();

		double [] downwashGradientLinearArray = new double [alphaArray.length];
		double distanceZeroLiftPlane = distanceZeroLiftLineACHorizontalTail();
		double downwashGradientLinear = calculateDownwashGradientLinearDelft(distanceZeroLiftPlane);

		for (int i=0 ; i<downwashGradientLinearArray.length ; i++){
			downwashGradientLinearArray[i] = downwashGradientLinear;
		}

		double [][] downwashGradientMatrix ={downwashGradientLinearArray, downwashGradientNonLinearArray};


		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaArray, downwashGradientMatrix,
				null, null, null, null,
				"alpha_Body", "d epsilon/d alpha",
				" deg ", "",
				legend,subfolderPath,
				"Downwash Gradient vs Alpha Body");


	}

	public void plotDownwashGradientDelftWithPath( String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotDownwashGradientDelft();
		subfolderPathCeck = true;
	}


	public void plotZDistance(){
		Amount<Angle> alpha = Amount.valueOf(Math.toRadians(15), SI.RADIAN);
		double alphaDouble = alpha.to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		nValuePlot = (int) Math.ceil(( alphaDouble - 0.0) * 4); //0.25 deg
		alphaArray = new double [nValuePlot];
		zDistanceArray = new double [nValuePlot];

		plotZCeck = true;

		calculateDownwashNonLinearDelftAtAlpha(alpha);

		plotZCeck = false;

		double [] zDistanceNonConstantArray = getzDistanceArray();
		double [] alphaArray = getAlphaArray();

		double [] zDistanceConstantArray = new double [alphaArray.length];
		double distanceZeroLiftPlane = distanceZeroLiftLineACHorizontalTail();

		for (int i=0 ; i<alphaArray.length; i++){
			zDistanceConstantArray[i] = distanceZeroLiftPlane;
		}

		double [][] zDistanceMatrix = { zDistanceConstantArray, zDistanceNonConstantArray} ;

		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaArray, zDistanceMatrix,
				null, null, null, null,
				"alpha_Body", "z",
				" deg ", "m",
				legend,subfolderPath,
				"Z Distance vs Alpha Body");


	}

	public void plotZDistanceWithPath( String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotZDistance();
		subfolderPathCeck = true;
	}

	/**
	 * This method calculates the distance along Z axis between the AC of the Htail and the
	 * zero-lift line of the exposed wing.
	 *
	 * @param Aircraft
	 *
	 * @author  Manuela Ruocco
	 */


	// 1
	//	 This method calculates the distance along Z axis between the AC of the Htail and the
	//	 zero-lift line of the exposed wing. The following treatment is referred to an exposed wing
	//	 so it will called more briefly wing.
	//	 This distance is the sum of the Z coordinate of the Htail and the vertical component between body reference line and the zero lift line of the wing.
	//	 In order to evaluate this contribution it is necessary to translate the body reference line
	//	 of a x quantity until the trailing edge of the root airfoil.
	//
	//  	fig. complete

	//	 It is possible to evaluate this quantity starting from the knowledge of wing root chord, the
	//	 angle of incidence of the wing, and the origin of LRF.
	//
	//      first particular
	//
	//     formula
	//
	//   Now it's possible to evaluate the distance --NOME-- between the new body reference line and the
	//  zero-lift wing line that is easy obtainable known the position of the aerodynamic center and the root chord
	//	of the wing
	//
	//  second particular
	//
	//  formula



	public double distanceZeroLiftLineACHorizontalTail(){


		double newBRLine = zWing - cRootExposedWing*Math.sin(angleOfIncidenceExposed);
		double hTailNewBRLine = zHTailAC - newBRLine;

		distTrailingEdgeWingXACH = distAerodynamicCenter - (cRootExposedWing - xACRootExposed);
		bRLineZeroLiftLine = distTrailingEdgeWingXACH * Math.tan(angleOfIncidenceExposed + alphaZeroLiftRootExposed);


		double distance = hTailNewBRLine + bRLineZeroLiftLine;
		return distance;

	}


	/**
	 * This method calculates the distance along Z axis between the AC of the Htail and the
	 * direction of flow not considering the deflection due to downwash.
	 *
	 * @param Aircraft
	 *
	 * @author  Manuela Ruocco
	 */


	// 2
	//	 In order to evaluate the effective distance between the horizontal tail an the vortex shed plane
	// it's necessary to evaluate the same distance not considering the deflection due to downwash.
	// This deflection, in fact, is obtainable with an iterative process in witch the starting
	// value of downwash is obtained from the previous step.
	//
	//  graph
	// particular

	public double distanceVortexShedPlaneACHTailNoDownwash( Amount<Angle> alphaAbsolute){

		if (alphaAbsolute.getUnit() == NonSI.DEGREE_ANGLE)
			alphaAbsolute = alphaAbsolute.to(SI.RADIAN);

		double zeroLiftDistance = distanceZeroLiftLineACHorizontalTail();
		double bRLineVelocityDirection = getDistTrailingEdgeWingXACH() * Math.tan(
				angleOfIncidenceExposed + alphaZeroLiftRootExposed - alphaAbsolute.getEstimatedValue());

		double distance = zeroLiftDistance - (getbRLineZeroLiftLine()-bRLineVelocityDirection);
		return distance;
	}



	public double getbRLineZeroLiftLine() {
		return bRLineZeroLiftLine;
	}



	public double getDistTrailingEdgeWingXACH() {
		return distTrailingEdgeWingXACH;
	}


	public double[] getDownwashArray() {
		return downwashArray;
	}


	public double[] getAlphaArray() {
		return alphaArray;
	}


	public double[] getzDistanceArray() {
		return zDistanceArray;
	}


	public double[] getDownwashGradientArray() {
		return downwashGradientArray;
	}

}

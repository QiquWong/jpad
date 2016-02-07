package sandbox.mr;

import java.io.File;
import java.util.Arrays;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import configuration.MyConfiguration;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import writers.JPADStaticWriteUtils;

public class DownwashCalculator {

	// VARIABLE DECLARATION--------------------------------------
	

	Aircraft aircraft;
	
	double zHTailAC, cRootExposedWing , angleOfIncidenceExposed, zWing, distAerodynamicCenter,
	alphaZeroLiftRootExposed, xACLRF, xACRootExposed, angleOfIncidenceExposedDeg, semiWingSpan,
	alphaZeroLiftRootExposedDeg, clAlfa, sweepQuarterChordEq, aspectRatio, zWingAC, zTipEdgeWingRootChord,
	zDistanceACHTailTEWing, xDistanceACHTailTEWing, xDistanceACHTailTEWingTemp;
	
	private double[] downwashArray;
	private double[] alphaAbsoluteArray ;
	private double[] alphaBodyArray ;
	private double[] downwashGradientArray;
	private double[] zDistanceArray;
	private double[] downwashLinearArray;
	
	private String subfolderPath;
	private boolean subfolderPathCeck = true;
	private double deltaAlpha;
	int nValue;
	
	String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
	
	
	//BUILDER--------------------------------------

	public DownwashCalculator(
			Aircraft aircraft
			) {

		this.aircraft = aircraft;

		
		distAerodynamicCenter = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();  
			// Distance between the points at c/4 of the mean
			// aerodynamic chord of the wing and the same point
			// of the horizontal tail.
		semiWingSpan = aircraft.get_exposedWing().get_semispan().getEstimatedValue();
		clAlfa = aircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();
		sweepQuarterChordEq = aircraft.get_wing().get_sweepQuarterChordEq().getEstimatedValue();
		aspectRatio = aircraft.get_exposedWing().get_aspectRatio();

		zHTailAC = aircraft.get_HTail().get_Z0().getEstimatedValue();
		zWingAC = aircraft.get_wing().get_aerodynamicCenterZ().getEstimatedValue();
		
		cRootExposedWing = aircraft.get_exposedWing().get_theAirfoilsListExposed()
				.get(0).get_chordLocal();
		angleOfIncidenceExposed = aircraft.get_wing().get_iw().getEstimatedValue()
				+ aircraft.get_exposedWing().get_twistDistributionExposed().get(0);
		angleOfIncidenceExposedDeg = Amount.valueOf(
				Math.toDegrees(angleOfIncidenceExposed), SI.RADIAN).getEstimatedValue();

		zWing = aircraft.get_wing().get_Z0().getEstimatedValue();
		alphaZeroLiftRootExposed = aircraft.get_exposedWing().get_alpha0lDistributionExposed().get(0);
		alphaZeroLiftRootExposedDeg = Amount.valueOf(
				Math.toDegrees(alphaZeroLiftRootExposed), SI.RADIAN).getEstimatedValue();

		LSAerodynamicsManager.CalcXAC theXACCalculator = aircraft.get_wing().getAerodynamics().new CalcXAC();
		xACLRF = theXACCalculator.deYoungHarper() + aircraft.get_wing().get_xLEMacActualLRF().getEstimatedValue();
		xACRootExposed = xACLRF - aircraft.get_wing().getXLEAtYActual(aircraft.get_fuselage().getWidthAtX(
				aircraft.get_wing().get_xLEMacActualBRF().getEstimatedValue()).doubleValue());
		
		zTipEdgeWingRootChord = zWingAC - ((cRootExposedWing - xACLRF)* Math.sin(angleOfIncidenceExposed- alphaZeroLiftRootExposed));
		xDistanceACHTailTEWingTemp = distAerodynamicCenter - ((cRootExposedWing - xACLRF)* Math.cos(angleOfIncidenceExposed- alphaZeroLiftRootExposed));
		xDistanceACHTailTEWing = xDistanceACHTailTEWingTemp * Math.tan(angleOfIncidenceExposed - alphaZeroLiftRootExposed);
		
		
		if ( zTipEdgeWingRootChord < 0 )
			zDistanceACHTailTEWing =  zHTailAC ;
		else
		zDistanceACHTailTEWing =  zHTailAC - zTipEdgeWingRootChord;
		
	}
	
	
	
	//METHODS--------------------------------------
	
	
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


	public double calculateDownwashGradientConstantDelft(double distVortexPlane){
	
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

		double downwashGradientLinearatZ=kFraction*(first+second*third)*(clAlfa/(Math.PI*aspectRatio));

		return downwashGradientLinearatZ;

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
	 * Distance along Z axis -- >Distance between the horizontal tail andthe vortex
	 *                           shed plane.
	 *
	 *
	 * @author  Manuela Ruocco
	 */


	// This method evaluates the downwash considering a variable downwash gradient.
	// In order to evaluate this value it's necessary to implement an iterative process when the
	// value of downwash at alpha depends on the value at previous step.
	// This method fills the fields of downwash angle, downwash gradient, the distance along z axis between
	// the horizontal tail and the vortex shed plane and body-relative angle of attack.
	
	// This method creates an array of absolute angle of attack starting from 0 deg until 15 deg.
	
	// the relative value of downwash for a variable number of step until alpha actual is equal to
	// alpha body, starting from an alpha such that alpha absolute = 0.
	// When alpha absolute =0, in fact, the value of downwash is assumed zero, but the downwash gradient
	// is not null.
	// This value of downwash gradien is used in the following step as effort value in order to
	// calculate a new distance along z axis, absolute angle of attack and a new value of downwash gradient.
	
	
	public void calculateDownwashNonLinearDelft(){

		double epsilonTemp, zDistTemp, downwashRad, epsilonTempRad, zApp, downwashGradientArrayTemp;
		
		// Alpha Absolute array 
		
		double alphaFirst = 0.0;
		double alphaLast = 20.0;
		nValue = (int) Math.ceil(( alphaLast - alphaFirst ) * 4); //0.25 deg

		alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue);
		
		deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1]), SI.RADIAN).getEstimatedValue();
		
		double alphaRefRad = angleOfIncidenceExposed - alphaZeroLiftRootExposed;
		
		// Initialize Array
		
		downwashArray = new double [nValue];
		downwashGradientArray = new double [nValue];
		alphaBodyArray = new double [nValue];
		zDistanceArray = new double [nValue];
		
		
		// First step
		
		downwashArray[0] = 0.0;
		zDistanceArray[0] = calculateZDistanceZeroLift();
		downwashGradientArray[0] = calculateDownwashGradientConstantDelft(zDistanceArray[0]);
		alphaBodyArray[0] = alphaAbsoluteArray[0]- angleOfIncidenceExposedDeg + alphaZeroLiftRootExposedDeg;
		
		
		// Other step
		
		for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){
			
			epsilonTemp = downwashGradientArray[i-1] * alphaAbsoluteArray[i]; // epsilon in deg, using gradient at previous step
			epsilonTempRad = Amount.valueOf(
					Math.toRadians(epsilonTemp), SI.RADIAN).getEstimatedValue();
			zApp = xDistanceACHTailTEWingTemp * Math.tan(alphaRefRad - i*deltaAlpha + epsilonTempRad);
			zDistTemp = (zDistanceACHTailTEWing + zApp)*Math.cos(alphaRefRad - i*deltaAlpha + epsilonTempRad);
			downwashGradientArrayTemp = calculateDownwashGradientConstantDelft(zDistTemp);
			downwashArray[i] = downwashGradientArrayTemp * alphaAbsoluteArray[i]; 
			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
			zDistanceArray[i] = (zDistanceACHTailTEWing + xDistanceACHTailTEWingTemp * Math.tan(
					alphaRefRad - i*deltaAlpha + downwashRad))*
					Math.cos(alphaRefRad - i*deltaAlpha + downwashRad);
			downwashGradientArray[i] = calculateDownwashGradientConstantDelft(zDistanceArray[i]);
			alphaBodyArray[i] = alphaAbsoluteArray[i] - angleOfIncidenceExposedDeg + alphaZeroLiftRootExposedDeg;
			
		}
		System.out.println("\n Results -----");
		System.out.println("DownwashGradient " + Arrays.toString(downwashGradientArray));
		System.out.println("Downwash Angle (deg) " + Arrays.toString(downwashArray));
		System.out.println("Alpha Absolute (deg) " + Arrays.toString(alphaAbsoluteArray));
		System.out.println("Alpha Body (deg)" + Arrays.toString(alphaBodyArray));
		System.out.println("Z Distance Absolute (m) " + Arrays.toString(zDistanceArray));	
		
	}
	
	
	/**
	 * This method gets the downwash angle at alpha body. its interpolate the value of downwash gradient
	 * which field must be filled before. 
	 *
	 * @param Amount<Angle> alpha Body in degree or radians
	 *
	 *
	 * @author  Manuela Ruocco
	 */
	
	// In order to relieve the calculations, the evaluation of downwash angle and downwash gradient should be
	// done only one time. To obtain the value of epsilon at alpha body it's possible to call the following method
	// that interpolates the value of downwash angle and angle of attack which field must be filled before.
	
	
	public double getDownwashAtAlphaBody(Amount<Angle> alphaBody){
		if (alphaBody.getUnit() == SI.RADIAN)
			alphaBody = alphaBody.to(NonSI.DEGREE_ANGLE);
			double alphaBodyDouble = alphaBody.getEstimatedValue();
		double downwashAtAlpha = MyMathUtils.getInterpolatedValue1DLinear(alphaBodyArray, downwashArray, alphaBodyDouble);
		return downwashAtAlpha;
	}
	
	/**
	 * This method calculates the distance along Z axis between the AC of the Htail and the
	 * direction of flow.
	 *
	 * @param Downwash Angle. IN DEGREE. If this is zero, the distance is between the AC of the H tail and the wing zero lift
	 *                        line. 
	 *
	 * @author  Manuela Ruocco
	 */

	// During the iterative process to evaluate the downwash it's necessary to calculate recursively the 
	// distance between the AC of horizontal tail and the vortex shed plane. So this calculation is assigned  
	// to another method that accepts in input the value of downwash angle. In the iteration this distance is 
	// evaluated preliminarily using the value of downwash gradient of the previous step.
	// The calculation of the distance between the AC of horizontal tail and the vortex shed plane is 
	// geometric and is explained in the fig. DELETE THIS
	// FIG E FORMULE
	
	
	public double calculateZDistanceZeroLift(){
		
		double zDistance;
		double zFirst;
		double zSecond ;
		zFirst = zDistanceACHTailTEWing;
		zSecond = xDistanceACHTailTEWingTemp * Math.tan(angleOfIncidenceExposed - alphaZeroLiftRootExposed);
		zDistance = (zFirst + zSecond) * Math.cos(angleOfIncidenceExposed - alphaZeroLiftRootExposed);
		return zDistance;
	}
	
	

	public void plotDownwashDelft(){

		downwashLinearArray  = new double [nValue] ;
		
		double vortexDistance = calculateZDistanceZeroLift();
		double downwashGradientLinear = calculateDownwashGradientConstantDelft(vortexDistance);
		double xInitialLinear = - angleOfIncidenceExposedDeg + alphaZeroLiftRootExposedDeg;
		double qValue = - downwashGradientLinear * xInitialLinear;		
		for (int i=0 ; i<alphaAbsoluteArray.length ; i++){
			downwashLinearArray[i] = downwashGradientLinear * alphaBodyArray [i]+ qValue;
		}

		double [][] epsilonMatrix ={downwashLinearArray,downwashArray};



		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaBodyArray, epsilonMatrix,
				null, null, null, null,
				"alpha_Body", "epsilon",
				" deg ", "deg",
				legend,subfolderPath,
				"Epsilon vs Alpha Body NEW");


	}


	public void plotDownwashDelftWithPath(String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotDownwashDelft();
		subfolderPathCeck = true;
	}
	
	public void plotDownwashGradientDelft(){

		downwashLinearArray  = new double [nValue] ;
		
		double[] downwashGradientLinearArray = new double [alphaAbsoluteArray.length];
		
		for (int i=0 ; i<alphaAbsoluteArray.length ; i++){
			downwashGradientLinearArray[i] = downwashGradientArray[0];
		}

		double [][] epsilonMatrix ={downwashGradientLinearArray,downwashGradientArray};



		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaBodyArray, epsilonMatrix,
				null, null, null, null,
				"alpha_Body", "d epsilon/ d alpha",
				" deg ", "",
				legend,subfolderPath,
				"Downwash gradient vs Alpha Body NEW");


	}


	public void plotDownwashGradientDelftWithPath(String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotDownwashGradientDelft();
		subfolderPathCeck = true;
	}
	
	public void plotZDistance(){

		downwashLinearArray  = new double [nValue] ;
		
		double vortexDistance = calculateZDistanceZeroLift();
		double[] zDistConstantArray = new double [alphaAbsoluteArray.length];
		
		for (int i=0 ; i<alphaAbsoluteArray.length ; i++){
			zDistConstantArray[i] = vortexDistance;
		}

		double [][] epsilonMatrix ={zDistConstantArray,zDistanceArray};



		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaBodyArray, epsilonMatrix,
				null, null, null, null,
				"alpha_Body", "Z Distance",
				" deg ", "m",
				legend,subfolderPath,
				"Disytance AC z vs Alpha Body NEW");


	}


	public void plotZDistanceWithPath(String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotZDistance();
		subfolderPathCeck = true;
	}
	
	
	
	
  // GETTERS ANS SETTERS	

	public double getDownwashGradientLinear() {
		double vortexDistance = calculateZDistanceZeroLift();
		double downwashGradientLinear = calculateDownwashGradientConstantDelft(vortexDistance);
		return downwashGradientLinear;
	}


	
}

package sandbox.mr;

import java.io.File;
import java.util.Arrays;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLvsAlphaCurve;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import writers.JPADStaticWriteUtils;

public class DownwashCalculator {

	// VARIABLE DECLARATION--------------------------------------
	

	Aircraft aircraft;
	
//	double zHTailAC, cRootExposedWing , angleOfIncidenceExposed, zWing, distAerodynamicCenter,
//	alphaZeroLiftRootExposed, xACLRF, xACRootExposed, angleOfIncidenceExposedDeg, semiWingSpan,
//	alphaZeroLiftRootExposedDeg, clAlfa, sweepQuarterChordEq, aspectRatio, zWingAC, zTipEdgeWingRootChord,
//	zDistanceACHTailTEWing, xDistanceACHTailTEWing, xDistanceACHTailTEWingTemp, alphaStar;
	
	double dValue, m0Value, x0Value, cRoot, zACWing, zACHtail, deltaZ, phiAngle,iw,alphaZeroLift,angle, zTemp, xTemp,
	zDistanceZero, xDistanceZero, alphaStar, semiWingSpan, sweepQuarterChordEq, aspectRatio;
	
	private double[] downwashArray;
	private double[] alphaAbsoluteArray ;
	private double[] alphaBodyArray ;
	private double[] downwashGradientArray;
	private double[] zDistanceArray;
	private double[] xDistanceArray;
	private double[] downwashLinearArray;
	

	
	private String subfolderPath;
	private boolean subfolderPathCeck = true;
	private double deltaAlpha;
	double alphaRefRad;
	
	int nValue;
	
	String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;

	private Double[] cLAlphaArray;
	private double[] cLArray;
	
	
	//BUILDER--------------------------------------

	public DownwashCalculator(
			Aircraft aircraft,
			double[] clAlphaArrayTemp,
			double[] alphaCLArray
			) {

		this.aircraft = aircraft;

		LiftingSurface theWing = aircraft.get_wing();
		
		cRoot = theWing.get_chordRoot().getEstimatedValue();
		semiWingSpan = theWing.get_semispan().getEstimatedValue();
		sweepQuarterChordEq = theWing.get_sweepQuarterChordEq().getEstimatedValue();
		double sweepqa = theWing.get_sweepQuarterChordEq().getEstimatedValue();
		aspectRatio = theWing.get_aspectRatio();
		
		
		x0Value = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		
		zACWing = theWing.get_Z0().getEstimatedValue();
		zACHtail = aircraft.get_HTail().get_Z0().getEstimatedValue()
				;
		iw = theWing.get_iw().getEstimatedValue(); //rad 
		alphaZeroLift = theWing.getAerodynamics().get_alpha0L().getEstimatedValue();
		
		
		deltaZ = 0.75 * cRoot * Math.sin(iw);
		
		double zApp;
		
		
		if ( (zACWing >0 & zACHtail > 0) || (zACWing < 0 & zACHtail < 0) ){
			zApp = Math.abs(Math.abs(zACWing) - Math.abs(zACHtail));
		}
		else{
			zApp = Math.abs(Math.abs(zACWing) + Math.abs(zACHtail));
		}
		
		if ( zACWing < zACHtail ) {
			m0Value = Math.abs(zApp + deltaZ);
		}
		
		if ( zACWing > zACHtail ) {
			m0Value = Math.abs(zApp - deltaZ);
		}
		
		if ( zACHtail < zACWing)
			m0Value = -m0Value;
		
		
		dValue = Math.sqrt((Math.pow(m0Value, 2)) 
				+ (Math.pow(x0Value - ((0.75) * cRoot * Math.cos(theWing.get_iw().getEstimatedValue())), 2)));
		
		phiAngle = Math.atan(( m0Value)/(x0Value - ((0.75) * cRoot * Math.cos(theWing.get_iw().getEstimatedValue()))));
		
		
		angle = phiAngle + iw - alphaZeroLift;
		
		zDistanceZero = dValue * Math.sin(angle);
		xDistanceZero = (dValue * Math.cos(angle)) + ( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift)));
		
		alphaStar = theWing.getAerodynamics().get_alphaStar().getEstimatedValue();
		// Alpha Absolute array 
		
		double alphaFirst = 0.0;
		double alphaLast = 20.0;
		nValue = (int) Math.ceil(( alphaLast - alphaFirst ) * 4); //0.25 deg

		alphaAbsoluteArray =  MyArrayUtils.linspace(alphaFirst, alphaLast, nValue);
		double [] alphaWingArray =  new double [alphaAbsoluteArray.length];
		for(int i=0; i< alphaAbsoluteArray.length; i++){
			alphaWingArray[i] = alphaAbsoluteArray[i]+alphaZeroLift*57.3;
		}
		deltaAlpha = Amount.valueOf(
				Math.toRadians(alphaAbsoluteArray[1] - alphaAbsoluteArray[0]), SI.RADIAN).getEstimatedValue();
		
	
		cLAlphaArray = new Double[alphaAbsoluteArray.length];
		cLArray = new double[alphaAbsoluteArray.length];
		
		LSAerodynamicsManager theLSAnalysis = aircraft.get_wing().getAerodynamics();
		LSAerodynamicsManager.CalcCLvsAlphaCurve theCLArrayCalculator = theLSAnalysis.new CalcCLvsAlphaCurve();
	
		
//		cLArray = theCLArrayCalculator.nasaBlackwellCompleteCurve(Amount.valueOf(alphaFirst + 
//				Math.toDegrees(alphaZeroLift), NonSI.DEGREE_ANGLE), 
//				Amount.valueOf(alphaLast + Math.toDegrees(alphaZeroLift), NonSI.DEGREE_ANGLE), nValue, false);
//	
//		
//		
//		
//		// cL alpha Array
//		
//		for (int i=0 ; i<alphaAbsoluteArray.length-1; i++){
//			if((alphaAbsoluteArray[i] + 
//					Math.toDegrees(alphaZeroLift))< Math.toDegrees(alphaStar)){
//			cLAlphaArray[i] = aircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();}
//			else{
//			cLAlphaArray[i]=Math.toDegrees((((cLArray[i+1] - cLArray[i])/(alphaAbsoluteArray[i+1] - alphaAbsoluteArray [i]))+((cLArray[i+1] - cLArray[i])/
//					(alphaAbsoluteArray[i+1] - alphaAbsoluteArray [i])))/2);
////				cLAlphaArray[i]=Math.toDegrees((cLArray[i+1] - cLArray[i])/(alphaAbsoluteArray[i+1] - alphaAbsoluteArray [i]));
//			}
//		}
//		cLAlphaArray[cLAlphaArray.length-1] = cLAlphaArray[cLAlphaArray.length-2];
//		
//		System.out.println(" cl alpha array = " + Arrays.toString(cLAlphaArray));
		
		cLAlphaArray = MyMathUtils.getInterpolatedValue1DLinear( alphaCLArray, clAlphaArrayTemp, alphaWingArray);
	
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


	public double calculateDownwashGradientConstantDelft(double distVertical, double distHorizontal, double clAlpha){
	
		double keGamma, keGammaZero;

		double r=distHorizontal/semiWingSpan;
		double rPow=Math.pow(r,2);
		double m=distVertical/semiWingSpan;
		double mpow=Math.pow(m, 2);


		keGamma=(0.1124+0.1265*sweepQuarterChordEq+0.1766*Math.pow(sweepQuarterChordEq,2))
				/rPow+0.1024/r+2;
		keGammaZero=0.1124/rPow+0.1024/r+2;

		double kFraction=keGamma/keGammaZero;
		double first= (r/(rPow+ mpow))*(0.4876/Math.sqrt(rPow+0.6319+mpow));
		double second= 1+Math.pow(rPow/(rPow+0.7915+5.0734*mpow),0.3113);
		double third = 1-Math.sqrt(mpow/(1+mpow));

		double downwashGradientLinearatZ=kFraction*(first+second*third)*((clAlpha)/(Math.PI*aspectRatio));

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
		
		// Initialize Array
		
		downwashArray = new double [nValue];
		downwashGradientArray = new double [nValue];
		alphaBodyArray = new double [nValue];
		zDistanceArray = new double [nValue];
		xDistanceArray = new double [nValue];
		
		
		// First step
		
		downwashArray[0] = 0.0;
		zDistanceArray[0] = zDistanceZero;
		xDistanceArray[0] = xDistanceZero;
		downwashGradientArray[0] = calculateDownwashGradientConstantDelft(zDistanceArray[0], xDistanceArray[0], cLAlphaArray[0]);
		alphaBodyArray[0] = alphaAbsoluteArray[0]- Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
		
		
		// Other step
		
		for ( int i = 1 ; i<alphaAbsoluteArray.length ; i++){
			
			epsilonTemp = downwashGradientArray[i-1] * alphaAbsoluteArray[i]; // epsilon in deg, using gradient at previous step
			epsilonTempRad = Amount.valueOf(
					Math.toRadians(epsilonTemp), SI.RADIAN).getEstimatedValue();
			
			
			
			zTemp = dValue * Math.sin(angle - i * deltaAlpha + epsilonTempRad);
			xTemp = (dValue * Math.cos(angle - i*deltaAlpha + epsilonTempRad)) + 
					( (0.74) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + epsilonTempRad)));
			
			
			downwashGradientArrayTemp = calculateDownwashGradientConstantDelft(zTemp, xTemp, cLAlphaArray[i]);
			
//			downwashArray[i] = downwashGradientArrayTemp * alphaAbsoluteArray[i]; 
			downwashArray[i] = downwashArray[i-1] + downwashGradientArrayTemp * deltaAlpha*57.3; 
			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
			
			zDistanceArray[i] = dValue * Math.sin(angle - i * deltaAlpha + downwashRad);
			xDistanceArray[i] = (dValue * Math.cos(angle - i*deltaAlpha +downwashRad)) + 
					( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + downwashRad)));
			
			downwashGradientArray[i] = calculateDownwashGradientConstantDelft(zDistanceArray[i], xDistanceArray[i], cLAlphaArray[i]);
//			downwashArray[i] = downwashGradientArray[i] * alphaAbsoluteArray[i]; 
			downwashArray[i] = downwashArray[i-1] + downwashGradientArray[i] * deltaAlpha*57.3; 
			downwashRad = Amount.valueOf(
					Math.toRadians(downwashArray[i]), SI.RADIAN).getEstimatedValue();
			
			zDistanceArray[i] = dValue * Math.sin(angle - i * deltaAlpha + downwashRad);
			xDistanceArray[i] = (dValue * Math.cos(angle - i*deltaAlpha +downwashRad)) + 
					( (0.75) * cRoot * Math.cos(Math.abs(alphaZeroLift - i*deltaAlpha + downwashRad)));
			
			downwashGradientArray[i] = calculateDownwashGradientConstantDelft(zDistanceArray[i], xDistanceArray[i], cLAlphaArray[i]);
//			downwashArray[i] = downwashGradientArray[i] * alphaAbsoluteArray[i]; 
			downwashArray[i] = downwashArray[i-1] + downwashGradientArray[i] * deltaAlpha*57.3; 
			
			alphaBodyArray[i] = alphaAbsoluteArray[i] - Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
			
		}
		System.out.println("\n Downwash Arrays");
		System.out.println("DownwashGradient " + Arrays.toString(downwashGradientArray));
		System.out.println("Downwash Angle (deg) " + Arrays.toString(downwashArray));
		System.out.println("Alpha Absolute (deg) " + Arrays.toString(alphaAbsoluteArray));
		System.out.println("Alpha Body (deg)" + Arrays.toString(alphaBodyArray));
		System.out.println("m Distances  (m) " + Arrays.toString(zDistanceArray));	
		System.out.println("x Distances (m) " + Arrays.toString(xDistanceArray));	
		
//double[] alphatry ={0,
//		1,
//		2,
//		3,
//		4,
//		5,
//		6,
//		7,
//		8,
//		9,
//		10,
//		11,
//		12,
//		13,
//		14,
//		15,
//		16,
//		17,
//		18,
//		19,
//		20
//};
//
//	Double [] alphatemp = MyMathUtils.getInterpolatedValue1DLinear(alphaAbsoluteArray, downwashArray, alphatry);
//		for( int i=0; i<alphatemp.length;i++){
//			System.out.println(alphatemp[i]);
//		}
		

	}
	
	
	/**
	 * This method gets the downwash angle at alpha body. its interpolate the value of downwash gradient
	 * which field must be filled before. 
	 *
	 * @param Amount<Angle> alpha Body in degree or radians
	 *
	 * @return downwash angle as double in deg
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
	
	
//	public double calculateZDistanceZeroLift(){
//		
//		double zDistance;
//		double zFirst;
//		double zSecond ;
//		zFirst = zDistanceACHTailTEWing;
//		zSecond = xDistanceACHTailTEWing;
//		zDistance = (zFirst + zSecond) * Math.cos(angleOfIncidenceExposed - alphaZeroLiftRootExposed);
////		System.out.println("Zdistance " + zDistance);
//		return zDistance;
//		
//	}
	
	

	public void plotDownwashDelft(){

		downwashLinearArray  = new double [nValue] ;
		
		double downwashGradientLinear = calculateDownwashGradientConstantDelft(zDistanceZero, xDistanceZero, cLAlphaArray[0]);
		double xInitialLinear = - Math.toDegrees(iw) + Math.toDegrees(alphaZeroLift);
		double qValue = - downwashGradientLinear * xInitialLinear;		
		for (int i=0 ; i<alphaAbsoluteArray.length ; i++){
			downwashLinearArray[i] = downwashGradientLinear * alphaAbsoluteArray [i];
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
		

		double[] zDistConstantArray = new double [alphaAbsoluteArray.length];
		
		for (int i=0 ; i<alphaAbsoluteArray.length ; i++){
			zDistConstantArray[i] = zDistanceZero;
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
				"alpha_Body", "m Distance",
				" deg ", "m",
				legend,subfolderPath,
				"Disytance m Alpha Body ");


	}


	public void plotZDistanceWithPath(String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotZDistance();
		subfolderPathCeck = true;
	}
	
	
	public void plotXDistance(){

		downwashLinearArray  = new double [nValue] ;
		

		double[] xDistConstantArray = new double [alphaAbsoluteArray.length];
		
		for (int i=0 ; i<alphaAbsoluteArray.length ; i++){
			xDistConstantArray[i] = xDistanceZero;
		}

		double [][] epsilonMatrix ={xDistConstantArray,xDistanceArray};



		if(subfolderPathCeck)
			subfolderPath = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		String [] legend = new String [2];
		legend[0] = "Downwash gradient: costant";
		legend[1] = "Downwash gradient: non costant";


		MyChartToFileUtils.plot(
				alphaBodyArray, epsilonMatrix,
				null, null, null, null,
				"alpha_Body", "x Distance",
				" deg ", "m",
				legend,subfolderPath,
				"Disytance x vs Alpha Body");


	}


	public void plotXDistanceWithPath(String subfolderPath){
		this.subfolderPath = subfolderPath;
		subfolderPathCeck = false;
		plotXDistance();
		subfolderPathCeck = true;
	}
	
	
	
	
  // GETTERS ANS SETTERS	

	public double getDownwashGradientLinear() {
		double downwashGradientLinear = calculateDownwashGradientConstantDelft(zDistanceZero,xDistanceZero, cLAlphaArray[0]);
		return downwashGradientLinear;
	}



	public double[] getAlphaAbsoluteArray() {
		return alphaAbsoluteArray;
	}



	public void setAlphaAbsoluteArray(double[] alphaAbsoluteArray) {
		this.alphaAbsoluteArray = alphaAbsoluteArray;
	}



	public double[] getDownwashArray() {
		return downwashArray;
	}



	public void setDownwashArray(double[] downwashArray) {
		this.downwashArray = downwashArray;
	}



	public double[] getAlphaBodyArray() {
		return alphaBodyArray;
	}



	public void setAlphaBodyArray(double[] alphaBodyArray) {
		this.alphaBodyArray = alphaBodyArray;
	}


	
}

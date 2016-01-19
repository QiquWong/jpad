package sandbox.mr;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import calculators.aerodynamics.AnglesCalc;
import calculators.aerodynamics.NasaBlackwell;
import configuration.enumerations.MethodEnum;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;
import standaloneutils.customdata.MyPoint;

// This class evaluates the effective angle of attack introducing the induced alpha by downwash.
// The effective angle of attack il the difference between the angle of attack and induced alpha. 
// This alpha_i is evaluates as the tg^-1 (w/u) where u is asymptotic velocity and w is the downwash.
// In order to evaluate the downwash this class use some methods, some of which are taken from the class 
// NasaBlackwell.
//
// @author Manuela Ruocco

public class AlphaEffective {



	int numberOfPoints = 30; //the same of static final  _numberOfPointsChordDistribution
	OperatingConditions theOperatingConditions;
	LSAerodynamicsManager theLSManager;
	LiftingSurface theWing;

	double vortexSemiSpan, vortexSemiSpanToSemiSpanRatio, surface, semispan, mach, altitude ; 
	double [] yStationsActual, dihedral,  twist, alpha0l, xLEvsYActual, chordsVsYActual, alpha0lArray,
			  yStationsAirfoil, yStationsAlpha;	
	MyArray yStationsNB;
	List<MyPoint> controlPoint, vortexPoint;

	//builder
	public AlphaEffective(LSAerodynamicsManager theLSManager, LiftingSurface theWing,
			OperatingConditions theOperatingConditions){
		this.theLSManager = theLSManager;
		this.theWing = theWing;
		this.theOperatingConditions = theOperatingConditions;

		vortexSemiSpanToSemiSpanRatio = theLSManager.get_vortexSemiSpanToSemiSpanRatio();
		vortexSemiSpan = vortexSemiSpanToSemiSpanRatio * theWing.get_semispan().getEstimatedValue();
		mach = theOperatingConditions.get_machCurrent();
		semispan = theWing.get_semispan().getEstimatedValue();
		
		dihedral = theWing.get_dihedral().toArray();
		alpha0lArray = theWing.get_alpha0VsY().toArray();
		twist = theWing.get_twistVsY().toArray();
		chordsVsYActual = theWing.get_chordsVsYActual().toArray();
		yStationsActual = MyArrayUtils.linspace(0., semispan, numberOfPoints);
		yStationsAlpha = MyArrayUtils.linspace(0., semispan, 50);
		yStationsAirfoil = theWing.get_yStationsAirfoil().toArray();
		xLEvsYActual = theWing.get_xLEvsYActual().toArray();
		surface = theWing.get_surface().getEstimatedValue();
		altitude = theOperatingConditions.get_altitude().getEstimatedValue();
		//nPointsSemispanWise = theLSManager.get_nPointsSemispanWise();

		
		alpha0l = theLSManager.get_alpha0lDistribution().toArray();
		
//		alpha0l = new MyArray(
//				MyMathUtils.getInterpolatedValue1DLinear(
//						yStationsAirfoil, 
//						alpha0lArray,
//						yStationsAlpha)).toArray();
//		
	}


	public double[] calculateAlphaEffective(
			Amount<Angle> alphaInitial){
		double velocity;
		double[] alphaEffective = new double[numberOfPoints];
		double [] addend = new double[numberOfPoints]; 
		double [][] influenceFactor = new double [numberOfPoints][numberOfPoints];
		double [] gamma = new double [numberOfPoints];
		double [] alphaInduced = new double [numberOfPoints];
		double [] verticalVelocity = new double [numberOfPoints];
		double [] summ = new double [numberOfPoints];
		int lowerLimit = 0, upperLimit=numberOfPoints-1;
		
		NasaBlackwell calculator = new NasaBlackwell(
				semispan, surface, yStationsActual, 
				chordsVsYActual, xLEvsYActual, 
				dihedral, twist,alpha0l, vortexSemiSpanToSemiSpanRatio,
				mach, altitude, alphaInitial);
		
//		double semispan, 
//		double surface,
//		double[] yStationsActual,
//		double[] chordsVsYActual,
//		double[] xLEvsYActual,
//		double[] dihedral,
//		double[] twist,
//		double[] alpha0l,
//		double vortexSemiSpanToSemiSpanRatio,
//		double mach,
//		double altitude,
//		Amount<Angle> alphaInitial
//		calculator.calculateDownwash(); 
//		influenceFactor = calculator.getInfluenceFactor();
//		gamma = calculator.getGamma();
		

		velocity = theOperatingConditions.get_tas().getEstimatedValue(); //meters per second

		for (int i=0 ; i<numberOfPoints; i++){
			for (int j = 0; j<numberOfPoints; j++){
				
		addend[j] =  gamma [j] * influenceFactor [i][j]; // devo costruire influence factor ad hoc

		summ[j] = MyMathUtils.summation(lowerLimit, upperLimit, addend);	
		verticalVelocity [i]= (1/(4*Math.PI)) * summ[j]; 
		alphaInduced [i] = Math.atan(verticalVelocity [i] /velocity);
		alphaEffective[i] = (alphaInitial.getEstimatedValue() - alphaInduced[i]);
		}
		}
		
		return alphaEffective;

	}



}

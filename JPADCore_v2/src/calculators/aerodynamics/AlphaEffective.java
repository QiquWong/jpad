package calculators.aerodynamics;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Velocity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import analyses.OperatingConditions;
import analyses.liftingsurface.LiftingSurfaceAerodynamicsManager;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;
import standaloneutils.customdata.MyPoint;

// This class evaluates the effective angle of attack introducing the induced alpha by downwash.
// The effective angle of attack is the difference between the angle of attack and induced alpha.
// This alpha_i is evaluates as the tg^-1 (w/u) where u is asymptotic velocity and w is the downwash.
// In order to evaluate the downwash this class use some methods, some of which are taken from the class
// NasaBlackwell.
//
// @author Manuela Ruocco

public class AlphaEffective {

	int numberOfPoints = 30; //the same of static final  _numberOfPointsChordDistribution
	OperatingConditions theOperatingConditions;
	LiftingSurfaceAerodynamicsManager theLSManager;
	LiftingSurface theWing;

	double vortexSemiSpan, vortexSemiSpanToSemiSpanRatio, mach;
	Amount<Area> surface;
	Amount<Length> semispan, altitude;
	Amount<Temperature> deltaTemperature;
	double [] yStationsActual, dihedral,  twist, alpha0l, xLEvsYActual, chordsVsYActual, alpha0lArray,
	yStationsAirfoil, yStationsAlpha;
	double [] alphaInduced;
	MyArray yStationsNB;
	List<MyPoint> controlPoint, vortexPoint;


	//--------------------------------------------------------
	//builder

	public AlphaEffective(LiftingSurfaceAerodynamicsManager theLSManager, LiftingSurface theWing,
			OperatingConditions theOperatingConditions){
		this.theLSManager = theLSManager;
		this.theWing = theWing;
		this.theOperatingConditions = theOperatingConditions;

		vortexSemiSpan = vortexSemiSpanToSemiSpanRatio * theWing.getSemiSpan().getEstimatedValue();
		mach = theOperatingConditions.getMachCruise();
		semispan = theWing.getSemiSpan();

		dihedral = MyArrayUtils
				.convertListOfAmountTodoubleArray(
						theWing.getDihedralsBreakPoints()
						);
		alpha0lArray = MyArrayUtils
				.convertListOfAmountTodoubleArray(
						theWing.getAlpha0VsY()
						);
		twist = MyArrayUtils
				.convertListOfAmountTodoubleArray(
						theWing.getTwistsBreakPoints()
						);
		chordsVsYActual = MyArrayUtils
				.convertListOfAmountTodoubleArray(
						theWing.getChordsBreakPoints()
						);
		yStationsActual = MyArrayUtils.linspace(0., semispan.doubleValue(SI.METER), numberOfPoints);
		yStationsAlpha = MyArrayUtils.linspace(0., semispan.doubleValue(SI.METER), 50);
		yStationsAirfoil = MyArrayUtils
				.convertListOfAmountTodoubleArray(
						theWing.getYBreakPoints()
						);
		xLEvsYActual = MyArrayUtils
				.convertListOfAmountTodoubleArray(
						theWing.getXLEBreakPoints()
						);
		surface = theWing.getSurfacePlanform();
		altitude = theOperatingConditions.getAltitudeCruise();
		deltaTemperature = theOperatingConditions.getDeltaTemperatureCruise();

	}


	public double[] getyStationsActual() {
		return yStationsActual;
	}


	public double[] calculateAlphaEffective(
			Amount<Angle> alphaInitial,
			Amount<Velocity> vTAS){
		double velocity;
		double[] alphaEffective = new double[numberOfPoints];
		double [] addend = new double[numberOfPoints];
		double [][] influenceFactor = new double [numberOfPoints][numberOfPoints];
		double [] gamma = new double [numberOfPoints];
		alphaInduced = new double [numberOfPoints];
		double [] verticalVelocity = new double [numberOfPoints];
		double summ =0.0 ;
		int lowerLimit = 0, upperLimit=(numberOfPoints-1);

		NasaBlackwell theCalculator = new NasaBlackwell(
				semispan, surface, yStationsActual,
				chordsVsYActual, xLEvsYActual,
				dihedral, theWing.getTwistsBreakPoints(),
				theWing.getAlpha0VsY(), vortexSemiSpanToSemiSpanRatio,
				alphaInitial, mach, altitude, deltaTemperature);

		theCalculator.calculateVerticalVelocity(alphaInitial);
		influenceFactor = theCalculator.getInfluenceFactor();
		gamma = theCalculator.getGamma();

		velocity = vTAS.getEstimatedValue(); //meters per second

		Double[] twistDistribution = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(
						theWing.getYBreakPoints()
						),
				twist,
				yStationsActual
				);


		for (int i=0 ; i<numberOfPoints; i++){
			for (int j = 0; j<numberOfPoints; j++){

				addend[j] =  gamma [j] * influenceFactor [i][j];

				summ = MyMathUtils.summation(lowerLimit, upperLimit, addend);
			}
			verticalVelocity [i]= (1/(4*Math.PI)) * (summ*0.3048);
//			System.out.println("\n \n------------------------------------------- ");
//			System.out.println("\nVertical velocity " + verticalVelocity[i] );
//			System.out.println("Velocity " + velocity);

			alphaInduced [i] = Math.atan(verticalVelocity [i] /velocity)/2;

//			System.out.println( alphaInduced[i]);
		//	System.out.println( yStationsActual[i]/semispan);
			
//			System.out.println(" alpha actual " + alphaInitial.getEstimatedValue());


			alphaEffective[i] = alphaInitial.getEstimatedValue() - alphaInduced[i] + twistDistribution[i];
		}

		return alphaEffective;

	}
	
	


	public double[] getAlphaInduced() {
		return alphaInduced;
	}



}


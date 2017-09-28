package Calculator;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.quantity.Velocity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

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
	
	static List<Amount<Angle>> alphaInduced = new ArrayList<>();

	public static List<Amount<Angle>> calculateAlphaEffective(
			NasaBlackwell theCalculator,
			int numberOfPoints,
			Amount<Angle> alphaInitial,
			Amount<Velocity> vTAS,
			List<Amount<Angle>> twistDistribution){
		
		double velocity;
		List<Amount<Angle>> alphaEffective = new ArrayList<>();
		double [] addend = new double[numberOfPoints];
		double [][] influenceFactor = new double [numberOfPoints][numberOfPoints];
		double [] gamma = new double [numberOfPoints];
		double [] verticalVelocity = new double [numberOfPoints];
		double summ =0.0 ;
		int lowerLimit = 0, upperLimit=(numberOfPoints-1);
		
		alphaInduced = new ArrayList<>();

		theCalculator.calculateVerticalVelocity(alphaInitial);
		influenceFactor = theCalculator.getInfluenceFactor();
		gamma = theCalculator.getGamma();

		velocity = vTAS.getEstimatedValue(); //meters per second
		//velocity = SpeedCalc.calculateTAS(mach, altitude);


		for (int i=0 ; i<numberOfPoints; i++){
			for (int j = 0; j<numberOfPoints; j++){

				addend[j] =  gamma [j] * influenceFactor [i][j];

				summ = MyMathUtils.summation(lowerLimit, upperLimit, addend);
			}
			verticalVelocity [i]= (1/(4*Math.PI)) * (summ*0.3048);
//			System.out.println("\n \n------------------------------------------- ");
//			System.out.println("\nVertical velocity " + verticalVelocity[i] );
//			System.out.println("Velocity " + velocity);

			alphaInduced.add(Amount.valueOf(
					Math.toDegrees(Math.atan(verticalVelocity [i] /velocity)/2),
					NonSI.DEGREE_ANGLE));


			alphaEffective.add(
					Amount.valueOf(
					alphaInitial.doubleValue(NonSI.DEGREE_ANGLE) - 
					alphaInduced.get(i).doubleValue(NonSI.DEGREE_ANGLE) +
					twistDistribution.get(i).doubleValue(NonSI.DEGREE_ANGLE),
					NonSI.DEGREE_ANGLE));
		}

		System.out.println(" alpha induced AT ALPHA " + alphaInitial);
		for(int i=0; i<numberOfPoints;i++) {
			System.out.print(alphaInduced.get(i).doubleValue(NonSI.DEGREE_ANGLE) + " , ");
		}
		return alphaEffective;

	}

	public List<Amount<Angle>> getAlphaInduced() {
		return alphaInduced;
	}



}


package sandbox.mr;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import com.sun.org.apache.xml.internal.utils.ThreadControllerWrapper;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class StabilityCalculator {

	public double calculateTauIndex(double chordRatio,
			Aircraft aircraft,
			Amount<Angle> deflection
			)
	{
		double deflectionAngleDeg;

		if (deflection.getUnit() == SI.RADIAN){
			deflection = deflection.to(NonSI.DEGREE_ANGLE);
		}
		if(deflection.getEstimatedValue()<0){
			deflectionAngleDeg = -deflection.getEstimatedValue();}

		else{
			deflectionAngleDeg = deflection.getEstimatedValue();}


		double aspectratioHorizontalTail = aircraft.get_HTail().get_aspectRatio();

		double etaDelta = aircraft
				.get_theAerodynamics()
				.get_highLiftDatabaseReader()
				.getEtaDeltaVsDeltaFlapPlain(deflectionAngleDeg, chordRatio);
		//System.out.println(" eta delta = " + etaDelta );

		double deltaAlpha2D = aircraft
				.get_theAerodynamics()
				.get_aerodynamicDatabaseReader()
				.getD_Alpha_d_Delta_2d_VS_cf_c(chordRatio);
		//System.out.println(" delta alfa 2d = " + deltaAlpha2D );

		double deltaAlpha2D3D = aircraft
				.get_theAerodynamics()
				.get_aerodynamicDatabaseReader()
				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
						aspectratioHorizontalTail,
						deltaAlpha2D
						);
		//System.out.println(" delta alfa 3d/2d = " + deltaAlpha2D3D );

		double tauIndex = deltaAlpha2D3D * deltaAlpha2D * etaDelta;


		return tauIndex;
	}


	public class CalcCLHTail{

		// VARIABLE DECLARATION--------------------------------------

		LiftingSurface theLiftingSurface;
		OperatingConditions theConditions;

		double [] alphaArrayWithTau, clHTailDeflected;
		Double [] cLHtailCleanExtended;


		public double[] cLHtailWithElevatorDeflection(LiftingSurface hTail,
				OperatingConditions theOperatingCondition, 
				double deltaE, double tauValue, double[] cLCleanArray, double[] alphaTailArray){

			LSAerodynamicsManager.MeanAirfoil theMeanAirfoil = hTail.getAerodynamics().new MeanAirfoil();
			MyAirfoil meanAirfoil = theMeanAirfoil.calculateMeanAirfoil(hTail);

			int nPoints = 60;


			List<Double[]> deltaFlap = new ArrayList<Double[]>();
			List<FlapTypeEnum> flapType = new ArrayList<FlapTypeEnum>();
			List<Double> etaInFlap = new ArrayList<Double>();
			List<Double> etaOutFlap = new ArrayList<Double>();
			List<Double> cfc = new ArrayList<Double>();

			Double[] deltaFlapDouble =  new Double [1];

			if(deltaE<0){
				deltaFlapDouble[0] = -deltaE;}
			else
				deltaFlapDouble[0] = deltaE;

			deltaFlap.add(deltaFlapDouble);
			flapType.add(FlapTypeEnum.PLAIN);
			etaInFlap.add(hTail.get_etaIn());
			etaOutFlap.add(hTail.get_etaOut());
			cfc.add(hTail.get_CeCt());

			LSAerodynamicsManager.CalcHighLiftDevices theHighLiftCalculator = hTail.getAerodynamics().new
					CalcHighLiftDevices(hTail, theOperatingCondition,
							deltaFlap, flapType, null,
							etaInFlap, etaOutFlap, null,
							null, cfc, null, null, 
							null);

			theHighLiftCalculator.calculateHighLiftDevicesEffects();

			// cl alpha clean

			double clAlphaClean = ((cLCleanArray[2] - cLCleanArray[1])/(alphaTailArray[2]-alphaTailArray[1]));

			
			// alphaZeroLift clean
			
			double alphaZeroLiftWingClean = hTail.getAerodynamics().getAlphaZeroLiftWingClean();
			

			// alpha zero lift 
			double alphaZeroLift = alphaZeroLiftWingClean -(tauValue * deltaE);


			// cl alpha new 

			double clAlphaDeltaE = theHighLiftCalculator.getcLalpha_new();

			// q value

			double qValue = - clAlphaDeltaE*alphaZeroLift;


			// alpha Star


			double alphaStarClean = meanAirfoil.getAerodynamics().get_alphaStar().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
			double clStarClean = clAlphaClean  * alphaStarClean;
			double alphaStarNew = (clStarClean - qValue)/clAlphaDeltaE;

			double alphaStarElevator = alphaStarClean + (-tauValue*deltaE);
			double cLstarElevator = clAlphaDeltaE * alphaStarElevator + qValue;


			// alpha max and cl max

			double alphaMaxClean = hTail.getAerodynamics().get_alphaMaxClean().to(NonSI.DEGREE_ANGLE).getEstimatedValue();
			double cLMaxClean =hTail.getAerodynamics().get_cLMaxClean();




			double deltaAlphaMax;

		
			deltaAlphaMax = theHighLiftCalculator.getDeltaAlphaMaxFlap();

			
			double deltaCLMax;

			if (deltaE<0)
				deltaCLMax = -theHighLiftCalculator.getDeltaCLmax_flap();

			else
				deltaCLMax = theHighLiftCalculator.getDeltaCLmax_flap();

			double cLMaxElevator = cLMaxClean + deltaCLMax;
			double alphaStallElevator = ((cLMaxElevator - qValue)/clAlphaDeltaE)+ deltaAlphaMax;


			alphaArrayWithTau = MyArrayUtils.linspace(alphaZeroLift-10 , alphaStallElevator+1.9 , nPoints);
			clHTailDeflected = new double [alphaArrayWithTau.length];

			
			// curve 

			double alpha;

			double[][] matrixData = {
					{Math.pow(alphaStarElevator, 4), Math.pow(alphaStarElevator, 3),Math.pow(alphaStarElevator, 2), alphaStarElevator,1.0},
					{4* Math.pow(alphaStarElevator, 3), 3*Math.pow(alphaStarElevator, 2), 2*alphaStarElevator,1.0, 0.0},
					{12 *Math.pow(alphaStarElevator, 2), 6* alphaStarElevator, 2.0, 0.0, 0.0},
					{Math.pow(alphaStallElevator, 4), Math.pow(alphaStallElevator, 3),Math.pow(alphaStallElevator, 2), alphaStallElevator,1.0},
					{4* Math.pow(alphaStallElevator, 3), 3*Math.pow(alphaStallElevator, 2), 2*alphaStallElevator,1.0, 0.0}
			};
				
				

			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);


			double [] vector = {cLstarElevator,clAlphaDeltaE, 0,cLMaxElevator,0};

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			double a = solSystem[0];
			double b = solSystem[1];
			double c = solSystem[2];
			double d = solSystem[3];
			double e = solSystem[4];

			for (int i=0; i<alphaArrayWithTau.length ; i++){

				alpha = alphaArrayWithTau[i];
				if (alpha < alphaStarElevator){
					clHTailDeflected[i] = clAlphaDeltaE * alpha + qValue;
				}

				else{
					clHTailDeflected[i] = a *Math.pow(alpha, 4)+ b * Math.pow(alpha, 3) + 
							c * Math.pow(alpha, 2) + 
							d * alpha + e;

				}
			}
			
			
					

						
			return clHTailDeflected;
		}


		public double[] getAlphaArrayWithTau() {
			return alphaArrayWithTau;
		}

	}
}


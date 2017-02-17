package calculators.aerodynamics;

import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.ComponentEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class AirfoilCalc {

	// Methods useful for case:  INPUT values---> curve
	/**
	 * This static method evaluates the lift curve of an airfoil on a given array of angle of attack
	 * 
	 * @author Manuela Ruocco
	 */

	public static void calculateClCurve(
			List<Amount<Angle>> alphaArray,
			AirfoilCreator theAirfoilCreator
			) {

		Double[] cLArray = new Double[alphaArray.size()];

		double cL0 = theAirfoilCreator.getClAtAlphaZero();
		double cLmax = theAirfoilCreator.getClMax();
		Amount<Angle> alphaStar = theAirfoilCreator.getAlphaEndLinearTrait();
		Amount<Angle> alphaStall = theAirfoilCreator.getAlphaStall();
		Amount<?> cLAlpha = theAirfoilCreator.getClAlphaLinearTrait();

		// fourth order interpolation for non linear trait
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;
		double e = 0.0;

		double cLStar = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
				* alphaStar.doubleValue(NonSI.DEGREE_ANGLE))
				+ cL0;

		for(int i=0; i<alphaArray.size(); i++) {
			if(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) <= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)) {
				cLArray[i] = (cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
						* alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE))
						+ cL0;
			}
			else {
				double[][] matrixData = { 
						{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 4),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
							Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
							alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
							1.0},
						{4* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
								3* Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
								2*alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
								1.0,
								0.0},
						{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 4),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
									Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
									alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
									1.0},
						{4* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
										3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
										2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
										1.0,
										0.0},
						{12* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
											6*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
											2.0,
											0.0,
											0.0},
				};

				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);

				double [] vector = {
						cLmax,
						0,
						cLStar,
						cLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(),
						0
				};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];
				e = solSystem[4];

				cLArray[i] = a * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 4) + 
						b * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 3) + 
						c * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 2) +
						d * alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) +
						e;
			}
		}
		theAirfoilCreator.setClCurve(MyArrayUtils.convertDoubleArrayToListDouble(cLArray));
	}

	/**
	 * Evaluate Cd using a parabolic polar curve
	 * 
	 * @author Manuela Ruocco
	 */

	public static void calculateCdvsClCurve(
			Double [] clCurveAirfoil,
			AirfoilCreator theAirfoilCreator
			) {

		Double [] cdCurve = new Double [clCurveAirfoil.length];

		double cdMin = theAirfoilCreator.getCdMin();
		double clAtCdMin = theAirfoilCreator.getClAtCdMin();
		double kFctorDragPolar = theAirfoilCreator.getKFactorDragPolar();
		double laminarBucketDept = theAirfoilCreator.getLaminarBucketDepth();
		double laminarBucketSemiExtension = theAirfoilCreator.getLaminarBucketSemiExtension();
		
		for (int i=0; i<clCurveAirfoil.length; i++){
			if((clCurveAirfoil[i] >= (clAtCdMin + laminarBucketSemiExtension)) || (clCurveAirfoil[i] <= (clAtCdMin - laminarBucketSemiExtension))){
			cdCurve[i] = (
					cdMin +
					Math.pow(( clCurveAirfoil[i] - clAtCdMin), 2)*kFctorDragPolar)+
			        laminarBucketDept;
			}
			else{
				cdCurve[i] = (
						cdMin +
						Math.pow(( clCurveAirfoil[i] - clAtCdMin), 2)*kFctorDragPolar);
			}
			
		
		}
		theAirfoilCreator.setCdCurve(MyArrayUtils.convertDoubleArrayToListDouble(cdCurve));
	}

	/**
	 * Evaluate Cm using a linear curve until the end of linearity angle, and a parabolic interpolation until the stall
	 * 
	 * @author Manuela Ruocco
	 */

	@SuppressWarnings("null")
	public static void calculateCmvsAlphaCurve(
			List<Amount<Angle>> alphaArray,
			AirfoilCreator theAirfoilCreator
			) {

		Double [] cmCurve = new Double[alphaArray.size()];

		double cmAC = theAirfoilCreator.getCmAC();
		double cmAlpha = theAirfoilCreator.getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
		double cmACStall = theAirfoilCreator.getCmACAtStall();
		Amount<Angle> alphaStar = theAirfoilCreator.getAlphaEndLinearTrait();
		Amount<Angle> alphaStall = theAirfoilCreator.getAlphaStall();

		// parabolic interpolation for non linear trait
		double a = 0.0;
		double b = 0.0;
		double c = 0.0;
		double d = 0.0;

		// last linear value
		double cmMaxLinear = cmAlpha *alphaStar.doubleValue(NonSI.DEGREE_ANGLE) + cmAC;

		for (int i=0; i<alphaArray.size(); i++){
			if(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE)<= alphaStar.doubleValue(NonSI.DEGREE_ANGLE)){
				cmCurve[i] = cmAlpha *alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) + cmAC;
			}
			else{
				double[][] matrixData = { 
						{Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 3),
							Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
							alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
							1.0},
						{3* Math.pow(alphaStar.doubleValue(NonSI.DEGREE_ANGLE), 2),
								2*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
								1.0,
								0.0},
						{6*alphaStar.doubleValue(NonSI.DEGREE_ANGLE),
									2.0,
									0.0,
									0.0},
						{Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 3),
										Math.pow(alphaStall.doubleValue(NonSI.DEGREE_ANGLE), 2),
										alphaStall.doubleValue(NonSI.DEGREE_ANGLE),
										1.0},


				};

				RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
				double [] vector = {
						cmMaxLinear,
						cmAlpha,
						0,
						cmACStall,

				};

				double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

				a = solSystem[0];
				b = solSystem[1];
				c = solSystem[2];
				d = solSystem[3];

				cmCurve[i] = a * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 3) + 
						b * Math.pow(alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE), 2) + 
						c * alphaArray.get(i).doubleValue(NonSI.DEGREE_ANGLE) +
						d;
			}				
		}		
		theAirfoilCreator.setCmCurve(MyArrayUtils.convertDoubleArrayToListDouble(cmCurve));
	}	


	// Methods useful for case:  INPUT curve---> values
	//LIFT
	//-------------------------------------------------------

	public static void extractLiftCharacteristicsfromCurve(
			Double[] clLiftCurve,
			List<Amount<Angle>> alphaArrayforClCurve,
			AirfoilCreator theAirfoilCreator
			){

		double clAlpha, clZero, clStar, clMax;
		Amount<Angle> alphaZeroLift, alphaStar, alphaStall;

		// cl alpha

		clAlpha = ((clLiftCurve[1] - clLiftCurve[0])/
				(alphaArrayforClCurve.get(1).doubleValue(NonSI.DEGREE_ANGLE)- alphaArrayforClCurve.get(0).doubleValue(NonSI.DEGREE_ANGLE)) + 
				(clLiftCurve[2] - clLiftCurve[1])/
				(alphaArrayforClCurve.get(2).doubleValue(NonSI.DEGREE_ANGLE) - alphaArrayforClCurve.get(1).doubleValue(NonSI.DEGREE_ANGLE)))/2;

		theAirfoilCreator.setClAlphaLinearTrait(Amount.valueOf(
				clAlpha,
				NonSI.DEGREE_ANGLE.inverse()));

		// cl zero

		clZero = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(alphaArrayforClCurve),
				MyArrayUtils.convertToDoublePrimitive(clLiftCurve),
				0.0
				);

		theAirfoilCreator.setClAtAlphaZero(clZero);

		// alpha zero lift

		alphaZeroLift = Amount.valueOf(-clZero/clAlpha, 
				NonSI.DEGREE_ANGLE
				);

		theAirfoilCreator.setAlphaZeroLift(alphaZeroLift);

		// cl max

		clMax = MyArrayUtils.getMax(clLiftCurve);
		int indexOfMax = MyArrayUtils.getIndexOfMax(clLiftCurve);

		theAirfoilCreator.setClMax(clMax);

		// alpha stall

		alphaStall = alphaArrayforClCurve.get(indexOfMax);

		theAirfoilCreator.setAlphaStall(alphaStall);

		// cl star 

		int j=0;
		double clLinear = clAlpha*alphaArrayforClCurve.get(j).doubleValue(NonSI.DEGREE_ANGLE)+clZero;

		while (Math.abs(clLiftCurve[j]-clLinear) < 0.01) {
			j++;
			clLinear = clAlpha*alphaArrayforClCurve.get(j).doubleValue(NonSI.DEGREE_ANGLE)+clZero;	
		}

		clStar = clLiftCurve[j];

		theAirfoilCreator.setClEndLinearTrait(clStar);

		// alpha star

		alphaStar = alphaArrayforClCurve.get(j);

		theAirfoilCreator.setAlphaLinearTrait(alphaStar);

	}	

	//MOMENT
	//-------------------------------------------------------

	public static void extractMomentCharacteristicsfromCurve(
			Double[] cmCurve,
			List<Amount<Angle>> alphaArrayforCmCurve,
			AirfoilCreator theAirfoilCreator
			){

		double cmAC;
		
		// cm0
		
		cmAC = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(alphaArrayforCmCurve),
				MyArrayUtils.convertToDoublePrimitive(cmCurve),
				0.0
				);

		theAirfoilCreator.setCmAC(cmAC);		

	}

	//GEOMETRY---------------------------------------------------------------------------
	/************************************************************************************
	 * This method calculates the t/c of the airfoil at a given non dimensional station
	 * using a formula obtained from the comparison of the thickness ratio law (along x) of 
	 * different type of airfoils families at fixed t/c max. The equation obtained is a 6th
	 * order polynomial regression curve. 
	 * 
	 * The polynomial formula is built using a t/c max of 0.12. The result has to be scaled
	 * in order to obtain the real t/c.
	 * 
	 * @author Vittorio Trifari
	 * @param x the non-dimensional station at which the user wants to calculate the 
	 * 	        thickness ratio.
	 * @return the thickness ratio t/c
	 */
	
	public static Double calculateThicknessRatioAtXNormalizedStation (
			Double x,
			Double tcMaxActual
			) {
			
		return (tcMaxActual/0.12)*((-5.9315*Math.pow(x, 6)) + (20.137*Math.pow(x, 5)) - (26.552*Math.pow(x, 4))
				+ (17.414*Math.pow(x, 3)) - (6.3277*Math.pow(x, 2)) + 1.2469*x + 0.0136);
		
	}
	
	public static Double calculateDeltaYPercent(
			double tc,
			AirfoilFamilyEnum family,
			AerodynamicDatabaseReader reader){
		
		return reader.getDeltaYvsThickness(
				tc,
				family
				);
	}
	
	public static PVector getCentralPoint(
			LiftingSurface liftingSurface,
			double yAdimensionalAirfoilStation
			) {
		float x,y,z;

		int nPan = liftingSurface.getLiftingSurfaceCreator().getPanels().size(); 
		
		if (liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			x = (float) (liftingSurface.getXApexConstructionAxes().getEstimatedValue()
					+ liftingSurface.getLiftingSurfaceCreator().getDiscretizedXle().get(liftingSurface.getLiftingSurfaceCreator().getDiscretizedXle().size()-1).getEstimatedValue()
					+ liftingSurface.getLiftingSurfaceCreator().getPanels().get(nPan - 1).getChordTip().getEstimatedValue()/2);
			z = (float) (liftingSurface.getLiftingSurfaceCreator().getSpan().getEstimatedValue())*1.005f 
					+ (float) liftingSurface.getZApexConstructionAxes().getEstimatedValue();
			y = 0.0f;

		} else {
			x = (float) (liftingSurface.getXApexConstructionAxes().getEstimatedValue()
					+ liftingSurface.getLiftingSurfaceCreator().getDiscretizedXle().get(liftingSurface.getLiftingSurfaceCreator().getDiscretizedXle().size()-1).getEstimatedValue()
					+ liftingSurface.getLiftingSurfaceCreator().getPanels().get(nPan - 1).getChordTip().getEstimatedValue()/2);
			y = (float) (liftingSurface.getLiftingSurfaceCreator().getSpan().getEstimatedValue()/2.)*1.005f;
			z = (float) (liftingSurface.getZApexConstructionAxes().getEstimatedValue()
					+ yAdimensionalAirfoilStation
					* Math.tan(liftingSurface.getLiftingSurfaceCreator().getDihedralAtYActual(yAdimensionalAirfoilStation).getEstimatedValue())); //TODO: add dihedral
		}

		return new PVector(x, y, z);
	}
	
	public static void populateCoordinateList(
			double yStation,
			AirfoilCreator theCreator,
			LiftingSurface theLiftingSurface
			) {

		float c = (float) theLiftingSurface.getChordAtYActual(yStation);
		float x, y, z;

		for (int i=0; i<theCreator.getXCoords().length; i++) {

			// Scale to actual dimensions
			x = theCreator.getXCoords()[i].floatValue()*c;
			y = (float) 0.0;
			z = theCreator.getZCoords()[i].floatValue()*c;

			double twistAtY = MyMathUtils.getInterpolatedValue1DLinear(
					MyArrayUtils.convertListOfAmountTodoubleArray(theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedYs()),
					MyArrayUtils.convertToDoublePrimitive(
							theLiftingSurface.getLiftingSurfaceCreator().getDiscretizedTwists().stream()
							.map(t -> t.doubleValue(SI.RADIAN))
							.collect(Collectors.toList())),							
					yStation
					);
			
			// Rotation due to twist
			if (theLiftingSurface.getType().equals(ComponentEnum.WING)) {
				float r = (float) Math.sqrt(x*x + z*z);
				x = (float) (x - r*(1-Math.cos(-twistAtY - theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN))));
				z = (float) (z + r*Math.sin(-twistAtY - theLiftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
			}

			// Actual location
			x = x + (float) theLiftingSurface.getLiftingSurfaceCreator().getXLEAtYActual(yStation).doubleValue(SI.METER)
					+ (float) theLiftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
			y = (float) yStation;
			z = z + (float) theLiftingSurface.getZApexConstructionAxes().doubleValue(SI.METER)
					+ (float) (yStation
							* Math.tan(theLiftingSurface.getLiftingSurfaceCreator().getDihedralAtYActual(yStation).doubleValue(SI.RADIAN)));

			if (theLiftingSurface.getLiftingSurfaceCreator().isMirrored()) {
				theCreator.getCoordinatesLeft().add(new PVector(x, -y, z));
			}	

			if (theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				theCreator.getCoordinatesRight().add( 
						new PVector(
								x,
								theCreator.getZCoords()[i].floatValue()*c, 
								(float) (yStation
								+ (float) theLiftingSurface.getZApexConstructionAxes().doubleValue(SI.METER))));

			} else {
				theCreator.getCoordinatesRight().add(new PVector(x, y, z));
			}

		}
	}

	public static void calculateMachCrShevell(
			AirfoilCreator theAirfoilCreator, 
			double cl
			) {
		// Page 409 Sforza
		theAirfoilCreator.setMachCritical(
				(0.9 - theAirfoilCreator.getThicknessToChordRatio())
				- ((0.17 + 0.016)*cl)
				); 
	}

	public static void calculateMachCrKorn(
			AirfoilCreator theAirfoilCreator,
			double cl
			) {
		
		double k;
		if (theAirfoilCreator.getType().equals(AirfoilTypeEnum.CONVENTIONAL)) k = 0.87;
		else k = 0.95; 

		theAirfoilCreator.setMachCritical((k - 0.108) - theAirfoilCreator.getThicknessToChordRatio() - 0.1*cl);
	}
	
	/** Page 410 Sforza */
	public static double calculateCdWaveLockShevell(
			AirfoilCreator theAirfoilCreator,
			double cl, 
			double mach
			) {

		calculateMachCrShevell(theAirfoilCreator, cl);
		
		double diff = mach - theAirfoilCreator.getMachCritical();

		double cdWave = 0.0;
		
		if (diff > 0)
			cdWave = 20*Math.pow((diff),4);
		return cdWave;
	}

	public double calculateCdWaveLockKorn(
			AirfoilCreator theAirfoilCreator,
			double cl, 
			double mach
			) {

		calculateMachCrKorn(theAirfoilCreator, cl);
		
		double diff = mach - theAirfoilCreator.getMachCritical();

		double cdWave = 0.0;
		
		if (diff > 0)
			cdWave = 20*Math.pow((diff),4);
		return cdWave;
	}
	
}

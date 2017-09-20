package calculators.aerodynamics;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import calculators.geometry.FusNacGeometryCalc;
import calculators.stability.StabilityCalculators;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ComponentEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
//import databasesIO.vedscdatabase.VeDSCDatabaseCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

/**
 * A group of static functions for evaluating aerodynamic moment/moment coefficients.
 * 
 * @author Lorenzo Attanasio
 *
 */
public class MomentCalc {

	private MomentCalc() {}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param mach
	 * @param ar
	 * @param semispan
	 * @param sweepHalfChordEq
	 * @param acToCgDistance
	 * @param wingMAC
	 * @param alpha0L
	 * @param cLalpha
	 * @param yStations
	 * @param clAlphaVsY
	 * @param chordsVsY
	 * @return
	 */
	public static double calcCM0LS(
			double mach, double ar, double semispan, double sweepHalfChordEq, 
			double acToCgDistance, double wingMAC, double alpha0L, double cLalpha,
			double[] yStations, double[] clAlphaVsY, double[] chordsVsY) {

		return cLalpha
				+ LiftCalc.calculateLiftCoefficientAtAlpha0(alpha0L, cLalpha)
				* (acToCgDistance/wingMAC);
	}

	
	public static double calcCM0LS(
			double acToCgDistance, double wingMAC, double cL0, double cLalpha) {

		return cLalpha
				+ cL0
				* (acToCgDistance/wingMAC);
	}

	/**
	 * 
	 * @author Lorenzo Attanasio
	 *
	 * @param cLalpha
	 * @param aircraftXcg
	 * @param xACpercentMAC
	 * @param xLEMacBRF x-coordinate of the Leading Edge of the Mean Aerodynamic Chord 
	 * in the Body Reference Frame
	 * @param meanAerodChord
	 * @return
	 */
	public static double calcCMalphaLS(double cLalpha, double aircraftXcg, 
			double xACpercentMAC, double xLEMacBRF, double meanAerodChord) {

		return cLalpha * (aircraftXcg - (xACpercentMAC + xLEMacBRF)) / meanAerodChord;
	}

	public static double calculateCMACAdditional(
			Amount<Area> surface,
			Amount<Length> semiSpan,
			Amount<Length> meanAerodynamicChord,
			List<Amount<Length>> yDistribution,
			List<Amount<Length>> chordDistribution,
			List<Double> cmACDistribution
			) {
		return (2/(surface.doubleValue(SI.SQUARE_METRE)*meanAerodynamicChord.doubleValue(SI.METER)))
				*MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								yDistribution
								), 
						MyArrayUtils.convertToDoublePrimitive(
								cmACDistribution.stream()
								.map(cm -> cm*Math.pow(chordDistribution.get(chordDistribution.indexOf(cm)).doubleValue(SI.METER),2))
								.collect(Collectors.toList())
								),
						0., 
						semiSpan.doubleValue(SI.METER)
						);
	}
	
	public static double calculateCMACBasic(
			Amount<Area> surface,
			Amount<Length> semiSpan,
			Amount<Length> meanAerodynamicChord,
			List<Amount<Length>> yDistribution,
			List<Amount<Length>> chordDistribution,
			List<Amount<Length>> xLEDistribution,
			List<Amount<Angle>> dihedralDistribution,
			List<Amount<Angle>> twistDistribution,
			List<Amount<Angle>> alphaZeroLiftDistribution,
			List<Amount<Length>> airfoilACToWingACDistribution,
			Double vortexSemiSpanToSemiSpanRatio,
			Double mach,
			Amount<Length> altitude,
			Amount<Angle> alphaZeroLift
			) {
		
		NasaBlackwell theNasaBlackwellCalculatorAlphaZeroLift = new NasaBlackwell(
				semiSpan.doubleValue(SI.METER),
				surface.doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(yDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(xLEDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(dihedralDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(twistDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(alphaZeroLiftDistribution),
				vortexSemiSpanToSemiSpanRatio,
				0.0,
				mach,
				altitude.doubleValue(SI.METER)
				);
		
		theNasaBlackwellCalculatorAlphaZeroLift.calculate(alphaZeroLift);
		
		return (2/(surface.doubleValue(SI.SQUARE_METRE)*meanAerodynamicChord.doubleValue(SI.METER)))
				*MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(
								yDistribution
								),
						theNasaBlackwellCalculatorAlphaZeroLift.getClTotalDistribution()
						.times(new MyArray(MyArrayUtils.convertListOfAmountToDoubleArray(chordDistribution)))
						.times(new MyArray(MyArrayUtils.convertListOfAmountTodoubleArray(airfoilACToWingACDistribution)))
						.toArray(),
						0., 
						semiSpan.doubleValue(SI.METER)
						);
	}
	
	public static double calculateCMACIntegralMean (
			Amount<Area> surface,
			Amount<Length> semiSpan,
			Amount<Length> meanAerodynamicChord,
			List<Amount<Length>> yDistribution,
			List<Amount<Length>> chordDistribution,
			List<Double> cmACDistribution
			) {
		
		return (2/(surface.doubleValue(SI.SQUARE_METRE)*meanAerodynamicChord.doubleValue(SI.METER)))
				* MyMathUtils.integrate1DSimpsonSpline(
						MyArrayUtils.convertListOfAmountTodoubleArray(yDistribution),
						MyArrayUtils.convertToDoublePrimitive(
								cmACDistribution.stream()
								.map(cmac -> cmac
										*chordDistribution.get(cmACDistribution.indexOf(cmac)).doubleValue(SI.METER)
										*chordDistribution.get(cmACDistribution.indexOf(cmac)).doubleValue(SI.METER)
										)
								.collect(Collectors.toList())
								),
						0.,
						semiSpan.doubleValue(SI.METER)
						);
		
	}
	/**
	 * 
	 * @param cL_alpha_v
	 * @param kFv
	 * @param kWv
	 * @param kHv
	 * @param armVertical distance of the AC of the vertical tail MAC from the MAC/4 point of the wing
	 * @param sVertical
	 * @param sWing
	 * @param wingSpan
	 * @return
	 */
	public static double calcCNbetaVerticalTail(double cL_alpha_v, double kFv, double kWv, double kHv, 
			double armVertical, double sVertical, double sWing, double wingSpan) {

		if (wingSpan == 0. || sWing == 0.) return 0.; 

		double  cNBeta = cL_alpha_v * kFv * kWv * kHv * (sVertical/sWing)  * (armVertical/wingSpan);
		
		return cNBeta;
	}

	/**
	 * 
	 * @param wingAr
	 * @param verticalAr
	 * @param surfaceWing
	 * @param surfaceVertical
	 * @param sweepC2vertical
	 * @param clAlphaVertical
	 * @param mach
	 * @param kFv
	 * @param kWv
	 * @param kHv
	 * @return
	 */
	public static double calcCNbetaVerticalTail(
			double wingAr, double verticalAr, 
			double armVertical, double wingSpan,
			double surfaceWing, double surfaceVertical, 
			double sweepC2vertical, double clAlphaVertical, 
			double mach, double kFv, double kWv, double kHv) {


		return calcCNbetaVerticalTail(
				LiftCalc.calculateCLalphaHelmboldDiederich(verticalAr, clAlphaVertical, sweepC2vertical,mach), 
				kFv, kWv, kHv, armVertical, surfaceVertical, surfaceWing, wingSpan)/(180/Math.PI);
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageMainMomentContributionCoefficient
	 * @param fuselageNoseMomentCorrectionCoefficient
	 * @param fuselageTailMomentCorrectionCoefficient
	 * @return the fuselage contribution of pitching moment estimated with the FusDes method
	 * 
	 * (see also "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 */

	public static double calcCM0Fuselage(double fuselageMainMomentContributionCoefficient, double fuselageNoseMomentCorrectionCoefficient,
			double fuselageTailMomentCorrectionCoefficient){

		double cM0FR = fuselageMainMomentContributionCoefficient;
		double dCMn = fuselageNoseMomentCorrectionCoefficient;
		double dCMt = fuselageTailMomentCorrectionCoefficient;

		return cM0FR + dCMn + dCMt;
	}

	/**
	 * @author Vincenzo Cusati
	 *  
	 * @param fuselageMainMomentDerivativeContributionCoefficient
	 * @param fuselageNoseMomentDerivativeCorrectionCoefficient
	 * @param fuselageTailMomentDerivativeCorrectionCoefficient
	 * @return the fuselage contribution of pitching moment derivative estimated with the FusDes method
	 * 
	 * (see also "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 */

	public static double calcCMAlphaFuselage(double fuselageMainMomentDerivativeContributionCoefficient, 
			double fuselageNoseMomentDerivativeCorrectionCoefficient,
			double fuselageTailMomentDerivativeCorrectionCoefficient){

		double cMalphaFR = fuselageMainMomentDerivativeContributionCoefficient;
		double dCMalphaNose = fuselageNoseMomentDerivativeCorrectionCoefficient;
		double dCMalphaTail = fuselageTailMomentDerivativeCorrectionCoefficient;

		return cMalphaFR  + dCMalphaNose + dCMalphaTail;
	}

	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param fuselageMainYawingMomentDerivativeCoefficient
	 * @param fuselageNoseYawingMomentDerivativeCoefficient
	 * @param fuselageTailYawingMomentDerivativeCoefficient
	 * @return the fuselage contribution of yawing moment estimated with the FusDes method
	 * 
	 * (see also "Fuselage aerodynamic prediction methods", Nicolosi, Della Vecchia, 
	 * Ciliberti, Cusati, Attanasio, 33rd AIAA Applied Aerodynamics 
	 * Conference, Aviation Forum 2015, Dallas (Texas, USA)).
	 */

	public static double calcCNBetaFuselage(
			FusDesDatabaseReader fusDesDatabaseReader,
			VeDSCDatabaseReader veDSCDatabaseReader,
			double finenessRatio,
			double noseFinenessRatio,
			double tailFinenessRatio,
			double xPositionPole,
			Amount<Length> fusDiameter,
			Amount<Area> wingSurface,
			Amount<Length> wingSpan,
			Amount<Length> verticalTailSpan,
			Amount<Length> fuselageDiameterAtVerticalMAC,
			double tailconeShape,
			double horizontalTailPositionOverVertical,
			double verticalTailAr,
			double wingPosition
			){

		fusDesDatabaseReader.runAnalysisCNbeta(
				noseFinenessRatio,
				finenessRatio,
				tailFinenessRatio,
				xPositionPole
				);
		
		double kVf = veDSCDatabaseReader.get_KVf_vs_zw_over_dfv(
				verticalTailSpan.doubleValue(SI.METER), 
				fuselageDiameterAtVerticalMAC.doubleValue(SI.METER), 
				tailconeShape
				);
		double kHf = veDSCDatabaseReader.get_KHf_vs_zh_over_bv1(
				horizontalTailPositionOverVertical, 
				verticalTailAr, 
				tailconeShape, 
				wingPosition
				);
		double kWf = veDSCDatabaseReader.get_KWf_vs_zw_over_rf(
				wingPosition,
				Math.pow(wingSpan.doubleValue(SI.METER), 2)/wingSurface.doubleValue(SI.SQUARE_METRE),
				tailconeShape
				);
		double surfaceRatio = FusNacGeometryCalc.calculateSfront(fusDiameter)/wingSurface.doubleValue(SI.SQUARE_METRE);
		
		return (fusDesDatabaseReader.getCNbFR() +
				fusDesDatabaseReader.getdCNbn() +
				fusDesDatabaseReader.getdCNbt())
				*surfaceRatio
				*fusDiameter.doubleValue(SI.METER)
				/wingSpan.doubleValue(SI.METER)
				*kVf
				*kHf
				*kWf;
	}
	
	
	public static List<Double> calcNonLinearCNFuselage(double cNbetaFuselage, List<Amount<Angle>> betaList){
		
		return betaList.stream()
				.map(b -> 0.2362 + 1.0178*b.doubleValue(NonSI.DEGREE_ANGLE) - 0.0135*Math.pow(b.doubleValue(NonSI.DEGREE_ANGLE),2))
				.map(b-> cNbetaFuselage*b)
				.collect(Collectors.toList());
	}
	
	public static List<Double>	calcCNWing(double cNbetaWing, List<Amount<Angle>> betaList){
		
		return betaList.stream()
				.map(b-> cNbetaWing*b.doubleValue(NonSI.DEGREE_ANGLE))
				.collect(Collectors.toList());
	}
	
	public static List<Double>	calcNonLinearCNVTail(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Amount<Angle> sweepLEVTail,
			double tcVTailMeanAirfoil,
			AirfoilFamilyEnum vTailMeanAirfoilFamily,
			double cNbetaVTail,
			double cYMaxVTail,
			Amount<Area> vTailSurface,
			Amount<Area> wingSurface,
			Amount<Length> vTailACToXcgDistance,
			Amount<Length> wingSpan,
			Amount<Angle> betaStar,
			List<Amount<Angle>> betaList){
		
		double volumetricRatio = 
				(vTailSurface.doubleValue(SI.SQUARE_METRE)/wingSurface.doubleValue(SI.SQUARE_METRE))
				*(vTailACToXcgDistance.doubleValue(SI.METER)/wingSpan.doubleValue(SI.METER));
		
		double cNMax = cYMaxVTail*volumetricRatio;
		
		double deltaYPercent = aeroDatabaseReader
				.getDeltaYvsThickness(
						tcVTailMeanAirfoil,
						vTailMeanAirfoilFamily
						);
		
		Amount<Angle> betaMaxLinear = Amount.valueOf(cNMax/cNbetaVTail, NonSI.DEGREE_ANGLE);
		Amount<Angle> deltaBetaMax = 
				Amount.valueOf(
						aeroDatabaseReader.getDAlphaVsLambdaLEVsDy(
								sweepLEVTail.doubleValue(NonSI.DEGREE_ANGLE),
								deltaYPercent
								),
						NonSI.DEGREE_ANGLE
						);
		Amount<Angle> betaStall = betaMaxLinear.plus(deltaBetaMax);
		
		List<Double> result = new ArrayList<>();
		betaList.stream()
			.filter(b -> b.doubleValue(NonSI.DEGREE_ANGLE) <= betaStar.doubleValue(NonSI.DEGREE_ANGLE))
				.forEach(b -> result.add(b.doubleValue(NonSI.DEGREE_ANGLE)*cNbetaVTail));
		
		betaList.stream()
			.filter(b -> b.doubleValue(NonSI.DEGREE_ANGLE) > betaStar.doubleValue(NonSI.DEGREE_ANGLE))
				.forEach(b -> result.add(
						LiftCalc.calculateCLAtAlphaNonLinearTrait(
								b, 
								Amount.valueOf(cNbetaVTail, NonSI.DEGREE_ANGLE.inverse()), 
								cNbetaVTail*betaStar.doubleValue(NonSI.DEGREE_ANGLE), 
								betaStar, 
								cYMaxVTail*volumetricRatio, 
								betaStall
								)
						));
		
		return result;
	}
	
	
	public static List<Double>	calcTotalCN(List<Double> cNFuselageList, List<Double> cNWingList, List<Double> cNVerticalList){
		
		return cNVerticalList.stream()
				.map(cNv-> cNv 
						+ cNWingList.get(cNVerticalList.indexOf(cNv)) 
						+ cNFuselageList.get(cNVerticalList.indexOf(cNv))
						)
				.collect(Collectors.toList());
	}
	
	public static double calcCNdr(
			double cNbVTail,
			Amount<Angle> dr, 
			double rudderChordRatio,
			double aspectRatioHTail,
			AerodynamicDatabaseReader aeroDatabaseReader,
			HighLiftDatabaseReader highLiftDatabaseReader
			){
		
		return cNbVTail*StabilityCalculators.calculateTauIndex(
				rudderChordRatio,
				aspectRatioHTail,
				aeroDatabaseReader, 
				highLiftDatabaseReader, 
				dr
				);
		
	}

	public static List<Double> calcCNDueToDeltaRudder(
			List<Amount<Angle>> betaList,
			List<Double> cNVTail,
			double cNbVTail,
			Amount<Angle> dr, 
			double tauRudder
			){
	
		// CN = CNb_v*(beta - tau*dr) 
		
		List<Double> result = new ArrayList<>(); 
		betaList.stream()
		.forEach(b -> result.add(
				cNVTail.get(betaList.indexOf(b))
				- (tauRudder*dr.doubleValue(NonSI.DEGREE_ANGLE)*cNbVTail)
				)
				);
		
		return result;
	}
	
	/**
	 * @author Vincenzo Cusati
	 * 
	 * @param sweepAngle (referred to c/4 line)
	 * @return the yawing moment coefficient derivative (CN_beta) of the wing 
	 */

	public static double calcCNBetaWing(Amount<Angle> sweepQuarterChord){

		return 0.00006*Math.sqrt(sweepQuarterChord.doubleValue(NonSI.DEGREE_ANGLE));

	}	


	/**
	 * 
	 * @param liftCoeff is the lift coefficient at a specific flight condition
	 * @param sweepAngle computed with respect to c/4 line
	 * @param aspectRatio
	 * @param xACwMACratio is the ratio between the x-position of the wing aerodynamic center and the mean aerodynamic chord
	 * @param xCGMACratio is the ratio between the x-position of the center of gravity and the mean aerodynamic chord
	 * @return the contribution of the wing to yawing moment derivative coefficent [1/deg]
	 * 
	 * see USAF Stability and Control DATCOM (Design Reference) - Finck 1978, 5.1.3 (pdf pag. 1576-1581)
	 * 
	 * @author Vincenzo Cusati
	 */

	public static double calcCNBetaWing(
			double liftCoeff, 
			Amount<Angle> sweepQuarterChord, 
			double aspectRatio,
			double xACwMACratio,  
			double xCGMACratio
			){

		double cL = liftCoeff;
		double sA = sweepQuarterChord.doubleValue(SI.RADIAN);
		double aR = aspectRatio;
		double xACw = xACwMACratio;
		double xCG = xCGMACratio;
		
		double cNbWing = Math.pow(cL, 2)*( ( 1/(4*Math.PI*aR) ) - ( Math.tan(Math.toRadians(sA))/(Math.PI*aR*(aR + Math.cos(Math.toRadians(sA)))) ) *
																  ( Math.cos(Math.toRadians(sA)) - aR/2 - Math.pow(aR, 2)/(8*Math.cos(Math.toRadians(sA))) +
																		  						   (6*(xACw-xCG)*Math.sin(Math.toRadians(sA))/aR) )
										 )/(180/Math.PI);

		return cNbWing; 
	}


	/**
	 * 
	 * This method calculates the yawing moment coefficient derivative (CN_beta) of 
	 * an aircraft as the sum of vertical tail, fuselage, wing contributions. 
	 * 
	 * @param verticalYawingMomDerCoeff
	 * @param fuselageYawingMomDerCoeff
	 * @param wingYawingMomentDerCoeff
	 * @return
	 */

	public static double calcCNBetaAC(double verticalYawingMomDerCoeff,
			double fuselageYawingMomDerCoeff, double wingYawingMomentDerCoeff){

		double cNbetaV    = verticalYawingMomDerCoeff;
		double cNbetaFus  = fuselageYawingMomDerCoeff;
		double cNbetaWing = wingYawingMomentDerCoeff;

		return cNbetaV + cNbetaFus + cNbetaWing;
	}

//-----------------------------------------------------------------------------------	
	public static List<Double> calcCMLiftingSurfaceWithIntegral(
			NasaBlackwell theNasaBlackwellCalculator,
			List<Amount<Angle>> anglesOfAttack,
			Amount<Length> liftingSurfaceMAC,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			Amount<Area> liftingSurfaceArea,
			Amount<Length> momentumPole  //referred to the origin of LRF
			){

		List<Double> liftingSurfaceMomentCoefficient = new ArrayList<>();

		double[] distancesArrayAC, clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew, cmDistribution, cCm,
		xcPfracC;

		int numberOfAlphas = anglesOfAttack.size();
		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

		for (int i=0; i<numberOfAlphas; i++){

			double alphaCheck = anglesOfAttack.get(i).doubleValue(NonSI.DEGREE_ANGLE);
			
			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];
			cmDistribution = new double[numberOfPointSemiSpanWise];
			cCm = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(anglesOfAttack.get(i));
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<anglesOfAttack.get(i).doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
				}
				else{
					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
							MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											airfoilClMatrix.get(ii))),
							alphaDistribution[ii]
							);
				}

				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution [ii] = clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+ liftingSurfaceCmC4Distribution.get(ii);

				cCm[ii] = cmDistribution [ii] * liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) *
						liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) ;
			}
			
//			System.out.println(" distance " +  Arrays.toString(distancesArrayAC));
//			System.out.println(" xcp " +  Arrays.toString(xcPfracC));
//			System.out.println(" cl " +  Arrays.toString(clDistribution));
//			System.out.println(" cl new " +  Arrays.toString(clInducedDistributionAtAlphaNew));
//			System.out.println(" cm " + Arrays.toString(cmDistribution) );
//			System.out.println(" ccm " + Arrays.toString(cCm));
			
			cCm[numberOfPointSemiSpanWise-1] = 0;

			liftingSurfaceMomentCoefficient.add(
					i,
					((2/(liftingSurfaceArea.doubleValue(SI.SQUARE_METRE)*liftingSurfaceMAC.doubleValue(SI.METER)))
					* MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCm))
					);
			
		}
		return liftingSurfaceMomentCoefficient;
	}
	
	public static List<Double> calcCMLiftingSurfaceWithIntegralACVariable(
			NasaBlackwell theNasaBlackwellCalculator,
			List<Amount<Angle>> anglesOfAttack,
			Amount<Length> liftingSurfaceMAC,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCLforCMMatrix,
			List<List<Double>> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			Amount<Area> liftingSurfaceArea,
			Amount<Length> momentumPole  //referred to the origin of LRF
			){

		List<Double> liftingSurfaceMomentCoefficient = new ArrayList<>();

		double[] distancesArrayAC, clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew, cmDistribution, cCm,
		xcPfracC;
		double cmActual;
		int numberOfAlphas = anglesOfAttack.size();
		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

		for (int i=0; i<numberOfAlphas; i++){

			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];
			cmDistribution = new double[numberOfPointSemiSpanWise];
			cCm = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(anglesOfAttack.get(i));
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				
				if(Double.isNaN(clDistribution[ii]))
					clDistribution[ii] = 0.0;
					
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<anglesOfAttack.get(i).doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
				}
				else{
					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
							MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											airfoilClMatrix.get(ii))),
							alphaDistribution[ii]
							);
				}

//				cmActual = MyMathUtils.getInterpolatedValue1DLinear(
//						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCLforCMMatrix),
//						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCmC4Distribution.get(ii)),
//						clInducedDistributionAtAlphaNew[ii]
//						);
				cmActual = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttack.subList(0, liftingSurfaceCmC4Distribution.get(ii).size())),
						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCmC4Distribution.get(ii)),
						anglesOfAttack.get(i).doubleValue(NonSI.DEGREE_ANGLE)
						);
				
				
				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution [ii] = clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+cmActual;
				
//				cmDistribution[ii] = clInducedDistributionAtAlphaNew[ii] * 
//						(distancesArrayAC[ii]/
//								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)) + liftingSurfaceCmC4Distribution.get(ii).get(i);

				cCm[ii] = cmDistribution [ii] * liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) *
						liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER) ;
			}
//			System.out.println(" distance " +  Arrays.toString(distancesArrayAC));
//			System.out.println(" xcp " +  Arrays.toString(xcPfracC));
//			System.out.println(" cl " +  Arrays.toString(clDistribution));
//			System.out.println(" cl new " +  Arrays.toString(clInducedDistributionAtAlphaNew));
//			System.out.println(" cm " + Arrays.toString(cmDistribution) );
//			System.out.println(" ccm " + Arrays.toString(cCm));
			
			cCm[numberOfPointSemiSpanWise-1] = 0;

			liftingSurfaceMomentCoefficient.add(
					i,
					((2/(liftingSurfaceArea.doubleValue(SI.SQUARE_METRE)*liftingSurfaceMAC.doubleValue(SI.METER)))
					* MyMathUtils.integrate1DSimpsonSpline(
							MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurfaceDimensionalY),
							cCm))
					);
			
		}
		return liftingSurfaceMomentCoefficient;
	}
	
	public static List<Double> calcCmDistributionLiftingSurfaceWithIntegral(
			NasaBlackwell theNasaBlackwellCalculator,
			Amount<Angle> angleOfAttack,
			List<Amount<Length>> liftingSurfaceDimensionalY,
			List<Double> liftingSurfaceCl0Distribution, // all distributions must have the same length!!
			List<Double> liftingSurfaceCLAlphaDegDistribution,
			List<Double> liftingSurfaceCmC4Distribution,
			List<Amount<Length>> liftingSurfaceChordDistribution,
			List<Amount<Length>> liftingSurfaceXLEDistribution,
			List<List<Double>> airfoilClMatrix, //this is a list of list. each list is referred to an airfoil along the semispan
			List<Amount<Angle>> anglesOfAttackClMatrix, // references angle of attack of the list of list airfoilClMatrix
			Amount<Length> momentumPole  //referred to the origin of LRF
			){
		
		List<Double> cmDistribution = new ArrayList<>();
		
		double[] distancesArrayAC, clDistribution, alphaDistribution, clInducedDistributionAtAlphaNew, xcPfracC;
		int numberOfPointSemiSpanWise = liftingSurfaceCl0Distribution.size();

			clDistribution = new double[numberOfPointSemiSpanWise];
			alphaDistribution = new double[numberOfPointSemiSpanWise];
			clInducedDistributionAtAlphaNew = new double[numberOfPointSemiSpanWise];
			distancesArrayAC = new double[numberOfPointSemiSpanWise];

			theNasaBlackwellCalculator.calculate(angleOfAttack);
			clDistribution = theNasaBlackwellCalculator.getClTotalDistribution().toArray();
			
			for (int ii=0; ii<numberOfPointSemiSpanWise; ii++){
				
				if(Double.isNaN(clDistribution[ii]))
					clDistribution[ii] = 0.0;
				
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<angleOfAttack.doubleValue(NonSI.DEGREE_ANGLE)){
					clInducedDistributionAtAlphaNew[ii] =
							liftingSurfaceCLAlphaDegDistribution.get(ii)*
							alphaDistribution[ii]+
							liftingSurfaceCl0Distribution.get(ii);
				}
				else{
					clInducedDistributionAtAlphaNew[ii] = MyMathUtils.getInterpolatedValue1DLinear(
							MyArrayUtils.convertListOfAmountTodoubleArray(anglesOfAttackClMatrix),
							MyArrayUtils.convertToDoublePrimitive(
									MyArrayUtils.convertListOfDoubleToDoubleArray(
											airfoilClMatrix.get(ii))),
							alphaDistribution[ii]
							);
				}


				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution.add(ii, clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+ liftingSurfaceCmC4Distribution.get(ii));

			}
			cmDistribution.set(numberOfPointSemiSpanWise-1,0.);
		return cmDistribution;
	}
	
	public static Double calculateCM0Multhopp(
			Amount<Length> startingPoint,
			Amount<Length> length,
			Double k2k1,
			Amount<Angle> wingRiggingAngle,
			Amount<Angle> wingAlphaZeroLift,
			Amount<Area> wingSurface,
			Amount<Length> wingRootChord,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Length> wingXApex,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ
			) {
		
		int nSecBeforeWing = 8;
		int nSecWing = 4;
		int nSecAfterWing = 8;
		
		//-----------------------------------------------------------------
		// X STATIONS
		Double[] xStationsBeforeWing = new Double[nSecBeforeWing];
		Double[] xStationsWing = new Double[nSecWing];
		Double[] xStationsAfterWing = new Double[nSecAfterWing];
		
		if(startingPoint.doubleValue(SI.METER) >= wingXApex.doubleValue(SI.METER)) 
			for(int i=0; i<nSecBeforeWing; i++)
				xStationsBeforeWing[i] = 0.0;
		else
			xStationsBeforeWing = MyArrayUtils.linspaceDouble(
					startingPoint.doubleValue(SI.METER),
					wingXApex.doubleValue(SI.METER),
					nSecBeforeWing
					);
		
		if((wingXApex.doubleValue(SI.METER) + wingRootChord.doubleValue(SI.METER)) > 
				(startingPoint.doubleValue(SI.METER) + length.doubleValue(SI.METER)))
			xStationsWing = MyArrayUtils.linspaceDouble(
					wingXApex.doubleValue(SI.METER),
					startingPoint.doubleValue(SI.METER) + length.doubleValue(SI.METER),
					nSecWing
					);
		else
			xStationsWing = MyArrayUtils.linspaceDouble(
					wingXApex.doubleValue(SI.METER),
					wingXApex.doubleValue(SI.METER) + wingRootChord.doubleValue(SI.METER),
					nSecWing
					);
			
		
		if((wingXApex.doubleValue(SI.METER) + wingRootChord.doubleValue(SI.METER)) >= 
				length.doubleValue(SI.METER) + startingPoint.doubleValue(SI.METER)) 
			for(int i=0; i<nSecAfterWing; i++)
				xStationsAfterWing[i] = 0.0;
		else
			xStationsAfterWing = MyArrayUtils.linspaceDouble(
					wingXApex.doubleValue(SI.METER) + wingRootChord.doubleValue(SI.METER),
					length.doubleValue(SI.METER),
					nSecAfterWing
					);
		
		//-----------------------------------------------------------------
		// Delta Xi 
		Double deltaXiBeforeWing = 0.0;
		Double deltaXiWing = 0.0;
		Double deltaXiAfterWing = 0.0; 
		
		for (int i=1; i<xStationsBeforeWing.length; i++) 
				deltaXiBeforeWing = xStationsBeforeWing[i] - xStationsBeforeWing[i-1];
		
		for (int i=2; i<xStationsWing.length; i++) 
				deltaXiWing = xStationsWing[i] - xStationsWing[i-1];
		
		for (int i=2; i<xStationsAfterWing.length; i++) 
				deltaXiAfterWing = xStationsAfterWing[i] - xStationsAfterWing[i-1];
		
		//-----------------------------------------------------------------
		// Wf^2 AT Xi STATIONS
		Double[] wfSquareBeforeWing = new Double[xStationsBeforeWing.length-1];
		Double[] wfSquareWing = new Double[xStationsWing.length-1];
		Double[] wfSquareAfterWing = new Double[xStationsAfterWing.length-1];
		
		for(int i=0; i<wfSquareBeforeWing.length; i++)
			wfSquareBeforeWing[i] = pow(
					FusNacGeometryCalc.getWidthAtX(
							xStationsBeforeWing[i] + (deltaXiBeforeWing/2),
							outlineXYSideRCurveX,
							outlineXYSideRCurveY
							),
					2);
		
		for(int i=0; i<wfSquareWing.length; i++)
			wfSquareWing[i] = pow(
					FusNacGeometryCalc.getWidthAtX(
							xStationsWing[i] + (deltaXiWing/2),
							outlineXYSideRCurveX,
							outlineXYSideRCurveY
							),
					2);

		for(int i=0; i<wfSquareAfterWing.length; i++)
			wfSquareAfterWing[i] = pow(
					FusNacGeometryCalc.getWidthAtX(
							xStationsAfterWing[i] + (deltaXiAfterWing/2),
							outlineXYSideRCurveX,
							outlineXYSideRCurveY
							),
					2);

		//-----------------------------------------------------------------
		// ANGLE SUM AT X STATIONS (in deg)
		Double[] angleSumBeforeWing = new Double[xStationsBeforeWing.length-1];
		Double[] angleSumWing = new Double[xStationsWing.length-1];
		Double[] angleSumAfterWing = new Double[xStationsAfterWing.length-1];
		
		for(int i=0; i<angleSumBeforeWing.length; i++) {
			angleSumBeforeWing[i] = FusNacGeometryCalc.getCamberAngleAtXFuselage(
					xStationsBeforeWing[i] + (deltaXiBeforeWing/2),
					outlineXZUpperCurveX,
					outlineXZUpperCurveZ,
					outlineXZLowerCurveX,
					outlineXZLowerCurveZ
					) 
					- wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
					+ wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE);
		}

		for(int i=0; i<angleSumWing.length; i++) {
			angleSumWing[i] = FusNacGeometryCalc.getCamberAngleAtXFuselage(
					xStationsWing[i] + (deltaXiWing/2),
					outlineXZUpperCurveX,
					outlineXZUpperCurveZ,
					outlineXZLowerCurveX,
					outlineXZLowerCurveZ
					) 
					- wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
					+ wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE);
		}
		
		for(int i=0; i<angleSumAfterWing.length; i++) {
			angleSumAfterWing[i] = FusNacGeometryCalc.getCamberAngleAtXFuselage(
					xStationsAfterWing[i] + (deltaXiAfterWing/2),
					outlineXZUpperCurveX,
					outlineXZUpperCurveZ,
					outlineXZLowerCurveX,
					outlineXZLowerCurveZ
					) 
					- wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
					+ wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE);
		}
		
		//-----------------------------------------------------------------
		// SUM
		Double sumBeforeWing = 0.0;
		Double sumWing = 0.0;
		Double sumAfterWing = 0.0;
		Double totalSum = 0.0;
		
		for(int i=0; i<angleSumBeforeWing.length; i++)
			sumBeforeWing += wfSquareBeforeWing[i]*angleSumBeforeWing[i]*deltaXiBeforeWing;
		for(int i=0; i<angleSumWing.length; i++)
			sumWing += wfSquareWing[i]*angleSumWing[i]*deltaXiWing;
		for(int i=0; i<angleSumAfterWing.length; i++)
			sumAfterWing += wfSquareAfterWing[i]*angleSumAfterWing[i]*deltaXiAfterWing;
		
		totalSum = sumBeforeWing + sumAfterWing;
		
		//-----------------------------------------------------------------
		// CMalpha CALCULATION
		Double cM0 = k2k1
				/(36.5
						*wingSurface.doubleValue(SI.SQUARE_METRE)
						*wingMeanAerodynamicChord.doubleValue(SI.METER)
						) 
				* totalSum;
		
		return cM0;
		
	}
	
//	public static Double calculateCM0FuselageMulthopp(
//			Amount<Length> fuselageLength,
//			Double k2k1,
//			Amount<Area> wingSurface,
//			Amount<Angle> wingRiggingAngle,
//			Amount<Angle> wingAlphaZeroLift,
//			Amount<Length> wingMeanAerodynamicChord,
//			List<Double> outlineXZUpperCurveX,
//			List<Double> outlineXZUpperCurveZ,
//			List<Double> outlineXZLowerCurveX,
//			List<Double> outlineXZLowerCurveZ,
//			List<Double> outlineXYSideRCurveX,
//			List<Double> outlineXYSideRCurveY
//			) {
//
//		Double cM0 = 0.0;
//		Double sum = 0.0;
//		double[] x = MyArrayUtils.linspace(
//				0.,
//				fuselageLength.doubleValue(SI.METER)*(1-0.0001),
//				100
//				);
//
//		try {
//			for(int i=1; i<x.length; i++){
//				sum = sum 
//						+ pow(FusNacGeometryCalc.getWidthAtX(x[i], outlineXYSideRCurveX, outlineXYSideRCurveY),2)
//						*(FusNacGeometryCalc.getCamberAngleAtXFuselage(
//								x[i],
//								outlineXZUpperCurveX,
//								outlineXZUpperCurveZ,
//								outlineXZLowerCurveX,
//								outlineXZLowerCurveZ
//								) 
//								- wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
//								+ wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE)
//								)
//						* (x[i] - x[i-1]);
//			}
//
//			cM0 = k2k1
//					/(36.5
//							*wingSurface.doubleValue(SI.SQUARE_METRE)
//							*wingMeanAerodynamicChord.doubleValue(SI.METER)
//							) 
//					* sum;
//
//		} catch (NullPointerException e) {
//			cM0 = 0.0;
//		}
//
//		return cM0;
//	}
//
//	public static Double calculateCM0NacelleMulthopp(
//			Amount<Length> nacelleLength,
//			Double k2k1,
//			Amount<Area> wingSurface,
//			Amount<Angle> wingRiggingAngle,
//			Amount<Angle> wingAlphaZeroLift,
//			Amount<Length> wingMeanAerodynamicChord,
//			List<Double> outlineXZUpperCurveX,
//			List<Double> outlineXZUpperCurveZ,
//			List<Double> outlineXZLowerCurveX,
//			List<Double> outlineXZLowerCurveZ,
//			List<Double> outlineXYSideRCurveX,
//			List<Double> outlineXYSideRCurveY
//			) {
//
//		Double cM0 = 0.0;
//		Double sum = 0.0;
//		double[] x = MyArrayUtils.linspace(
//				0.,
//				nacelleLength.doubleValue(SI.METER)*(1-0.0001),
//				100
//				);
//
//		try {
//			for(int i=1; i<x.length; i++){
//				sum = sum 
//						+ pow(FusNacGeometryCalc.getWidthAtX(x[i], outlineXYSideRCurveX, outlineXYSideRCurveY),2)
//						*(FusNacGeometryCalc.getCamberAngleAtXNacelle(
//								x[i],
//								outlineXZUpperCurveX,
//								outlineXZUpperCurveZ,
//								outlineXZLowerCurveX,
//								outlineXZLowerCurveZ
//								) 
//								- wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
//								+ wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE)
//								)
//						* (x[i] - x[i-1]);
//			}
//
//			cM0 = k2k1
//					/(36.5
//							*wingSurface.doubleValue(SI.SQUARE_METRE)
//							*wingMeanAerodynamicChord.doubleValue(SI.METER)
//							) 
//					* sum;
//
//		} catch (NullPointerException e) {
//			cM0 = 0.0;
//		}
//
//		return cM0;
//	}
	
	public static Amount<?> calculateCMAlphaFuselageGilruth(
			Amount<Length> length,
			Amount<Length> maxWidth,
			double[] positionOfC4ToFuselageLength,
			double[] kF,
			Amount<Area> wingSurface,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Length> wingXApex,
			Amount<Length> wingRootChord
			) {

		Double cMAlpha = 0.0;

		double kf = MyMathUtils
				.interpolate1DLinear(positionOfC4ToFuselageLength, kF)
				.value(
						(wingXApex.doubleValue(SI.METER) 
								+ 0.25*wingRootChord.doubleValue(SI.METER)
								)
						/length.doubleValue(SI.METER)
						);

		cMAlpha = kf*pow(maxWidth.doubleValue(SI.METER), 2)*length.doubleValue(SI.METER)
				/(wingSurface.doubleValue(SI.SQUARE_METRE)
						*wingMeanAerodynamicChord.doubleValue(SI.METER)
						);

		return Amount.valueOf(cMAlpha, NonSI.DEGREE_ANGLE.inverse());
		
	}

	/**
	 * see Roskam (Part 6) chapter 8 pag 326
	 * 
	 * @param type ComponentEnum
	 * @param length of Fuselage/Nacelle
	 * @param downwashGradient 
	 * @param wingAspectRatio
	 * @param wingSurface
	 * @param wingRootChord
	 * @param wingCLAlpha
	 * @param wingXApex
	 * @param aeroDatabaseReader
	 * @param outlineXYSideRCurveX
	 * @param outlineXYSideRCurveY
	 * @return
	 */
	public static Amount<?> calculateCMAlphaFuselageOrNacelleMulthopp(
			Amount<Length> startingPoint,
			Amount<Length> length,
			double downwashGradientRoskamConstant,
			double wingAspectRatio,
			Amount<Area> wingSurface,
			Amount<Length> wingRootChord,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<?> wingCLAlpha,
			Amount<Length> wingXApex,
			Amount<Length> wingTrailingEdgeToHTailQuarterChordDistance,
			AerodynamicDatabaseReader aeroDatabaseReader,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {

		int nSecBeforeWing = 8;
		int nSecAfterWing = 8;
		
		//-----------------------------------------------------------------
		// X STATIONS
		Double[] xStationsBeforeWing = new Double[nSecBeforeWing];
		Double[] xStationsAfterWing = new Double[nSecAfterWing];
		
		if(startingPoint.doubleValue(SI.METER) >= wingXApex.doubleValue(SI.METER)) 
			for(int i=0; i<nSecBeforeWing; i++)
				xStationsBeforeWing[i] = 0.0;
		else
			xStationsBeforeWing = MyArrayUtils.linspaceDouble(
					startingPoint.doubleValue(SI.METER),
					wingXApex.doubleValue(SI.METER),
					nSecBeforeWing
					);
		
		if((wingXApex.doubleValue(SI.METER) + wingRootChord.doubleValue(SI.METER)) >= 
				length.doubleValue(SI.METER) + startingPoint.doubleValue(SI.METER)) 
			for(int i=0; i<nSecAfterWing; i++)
				xStationsAfterWing[i] = 0.0;
		else
			xStationsAfterWing = MyArrayUtils.linspaceDouble(
					wingXApex.doubleValue(SI.METER) + wingRootChord.doubleValue(SI.METER),
					length.doubleValue(SI.METER),
					nSecAfterWing
					);
		
		//-----------------------------------------------------------------
		// Delta Xi AND Xi STATIONS
		Double deltaXiBeforeWing = 0.0;
		Double deltaXiAfterWing = 0.0; 
		
		Double[] xiBeforeWing = new Double[xStationsBeforeWing.length-1];
		Double[] xiAfterWing = new Double[xStationsAfterWing.length-2];
		
		for (int i=1; i<xStationsBeforeWing.length; i++) {
			if(i==1) {
				deltaXiBeforeWing = xStationsBeforeWing[i] - xStationsBeforeWing[i-1];
				xiBeforeWing[i-1] = wingXApex.doubleValue(SI.METER) - (deltaXiBeforeWing/2);
			}
			else {
				deltaXiBeforeWing = xStationsBeforeWing[i] - xStationsBeforeWing[i-1];
				xiBeforeWing[i-1] = xiBeforeWing[i-2] - (deltaXiBeforeWing);
			}
		}
		for (int i=2; i<xStationsAfterWing.length; i++) {
			if (i==2) {
				deltaXiAfterWing = xStationsAfterWing[i] - xStationsAfterWing[i-1];
				xiAfterWing[i-2] = deltaXiAfterWing/2;
			}
			else {
				deltaXiAfterWing = xStationsAfterWing[i] - xStationsAfterWing[i-1];
				xiAfterWing[i-2] = xiAfterWing[i-3] + (deltaXiAfterWing);
			}
		}
		
		//-----------------------------------------------------------------
		// Wf^2 AT Xi STATIONS
		Double[] wfSquareBeforeWing = new Double[xiBeforeWing.length];
		Double[] wfSquareAfterWing = new Double[xiAfterWing.length];
		
		for(int i=0; i<xiBeforeWing.length; i++)
				wfSquareBeforeWing[i] = pow(
						FusNacGeometryCalc.getWidthAtX(
								xStationsBeforeWing[i] + (deltaXiBeforeWing/2),
								outlineXYSideRCurveX,
								outlineXYSideRCurveY
								),
						2);
		for(int i=0; i<xiAfterWing.length; i++)
				wfSquareAfterWing[i] = pow(
						FusNacGeometryCalc.getWidthAtX(
								xStationsAfterWing[i] + (deltaXiAfterWing/2),
								outlineXYSideRCurveX,
								outlineXYSideRCurveY
								),
						2);

		//-----------------------------------------------------------------
		// Depsilon\DAlpha AT Xi STATIONS
		Double[] dEpsilonDAlphaBeforeWing = new Double[xiBeforeWing.length];
		Double[] dEpsilonDAlphaAfterWing = new Double[xiAfterWing.length];
		
		for(int i=0; i<xiBeforeWing.length; i++) {
			if(i<xiBeforeWing.length-1)
				dEpsilonDAlphaBeforeWing[i] = aeroDatabaseReader.getCmAlphaBodyUpwashVsXiOverRootChord(
						wingRootChord, 
						Amount.valueOf(
								xiBeforeWing[i],
								SI.METER
								)
						);
			else
				dEpsilonDAlphaBeforeWing[i] = aeroDatabaseReader.getCmAlphaBodyNearUpwashVsXiOverRootChord(
						wingRootChord, 
						Amount.valueOf(
								xiBeforeWing[i],
								SI.METER
								)
						);
		}

		for(int i=0; i<xiAfterWing.length; i++) 
				dEpsilonDAlphaAfterWing[i] = (
						(xiAfterWing[i]/wingTrailingEdgeToHTailQuarterChordDistance.doubleValue(SI.METER))
						*(1-downwashGradientRoskamConstant)
						);
		
		//-----------------------------------------------------------------
		// SUM
		Double sumBeforeWing = 0.0;
		Double sumAfterWing = 0.0;
		Double totalSum = 0.0;
		
		for(int i=0; i<xiBeforeWing.length; i++)
			sumBeforeWing += wfSquareBeforeWing[i]*(dEpsilonDAlphaBeforeWing[i])*deltaXiBeforeWing;
		for(int i=0; i<xiAfterWing.length; i++)
			sumAfterWing += wfSquareAfterWing[i]*(dEpsilonDAlphaAfterWing[i])*deltaXiAfterWing;
		
		totalSum = sumBeforeWing + sumAfterWing;
		
		//-----------------------------------------------------------------
		// CMalpha CALCULATION
		Amount<?> cMAlpha = Amount.valueOf(
				(1/(36.5*wingSurface.doubleValue(SI.SQUARE_METRE)*wingMeanAerodynamicChord.doubleValue(SI.METER)))
				*(wingCLAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()/0.0785)
				*totalSum,
				NonSI.DEGREE_ANGLE.inverse()
				);
		
		return cMAlpha;
		
	}
	
	public static Double calculateCMAtAlphaFuselage(
			Amount<Angle> alphaBody,
			Amount<?> cMAlpha,
			Double cM0			
			) {
		
		return alphaBody.doubleValue(NonSI.DEGREE_ANGLE)
				*cMAlpha.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
				+ cM0;				
		
	}
	
	/**
	 * This method allows to calculate the total moment coefficient of an aircraft with respect to the center of gravity
	 * which is an input, using the balance equation.
	 * NB. All List<Double> of components aerodynamic coefficients MUST BE referred to the same vector of alpha body.
	 *
	 * @param  the dimensional position along the X axis of the center of gravity in BRF
	 * @param  the dimensional position along the Z axis of the center of gravity (pendular stability) in BRF
	 * @param  the dimensional position along the X axis of the wing aerodynamic center in BRF
	 * @param  the dimensional position along the Z axis of the wing aerodynamic center in BRF
	 * @param  the dimensional position along the Z axis of the application point of the drag in the fuselage in BRF
	 * @param  the dimensional position along the X axis of the horizontal tail aerodynamic center in BRF
	 * @param  the dimensional position along the Z axis of the horizontal tail aerodynamic center in BRF
	 * @param  the dimensional position along the Z axis of the landing gear FROM X AXIS BRF (nb. it must be a negative value).
	 * @param  the dimensional value of the Wing MAC (used to the nondimensionalization)
	 * @param  the dimensional value of the horizontal tail MAC 
	 * @param  the wing surface
	 * @param  the horizontal tail surface
	 * @param  the wing lift coefficient list with fuselage effect if required NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the wing drag coefficient list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the wing moment coefficient with respect to aerodynamic center list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the fuselage moment coefficient list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the fuselage drag coefficient list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the horizontal tail lift coefficient list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the horizontal tail drag coefficient list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the horizontal tail moment coefficient with respect to c/4 list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the landing gear drag coefficient list NB. REFERRED TO A CERTAIN ALPHA BODY VECTOR THAT MUST BE THE SAME FOR EVERY LIST
	 * @param  the horizontal tail-wing dynamic pressure ratio
	 * @param  the list of alpha body angles. 
	 * @param  true if consider pendular stability
	 * @return     
	 */
	
	public static List<Double> calculateCMTotalCurveWithBalanceEquation(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xACWing,
			Amount<Length> zACWing,
			Amount<Length> xACHorizontalTail,
			Amount<Length> zACHorizontalTail,
			Amount<Length> xCGFuselage,
			Amount<Length> zCGFuselage,
			Amount<Length> xCGLandingGears,
			Amount<Length> zCGLandingGears,
			Amount<Length> xCGNacelle,
			Amount<Length> zCGNacelle,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Length> horizontalTailMeanAerodynamicChord,
			Amount<Area> wingSurface,
			Amount<Area> horizontalTailSurface,
			List<Double> wingFuselageLiftCoefficient,
			List<Double> wingDragCoefficient,
			List<Double> wingMomentCoefficient,
			List<Double> fuselageMomentCoefficient,
			List<Double> fuselageDragCoefficient,
			List<Double> nacelleMomentCoefficient,
			List<Double> nacelleDragCoefficient,
			List<Double> horizontalTailLiftCoefficient,
			List<Double> horizontalTailDragCoefficient,
			List<Double> horizontalTailMomentCoefficient,
			Double landingGearDragCoefficient,
			Double horizontalTailDynamicPressureRatio,
			List<Amount<Angle>> alphaBodyList,
			boolean pendularStability
			) {

		List<Double> totalMomentCoefficient = new ArrayList<>();
		List<Double> wingMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> fuselageMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> nacelleMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> horizontalTailMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> landingGearMomentCoefficientWithRespectToCG = new ArrayList<>();

		wingMomentCoefficientWithRespectToCG = calculateCMWingCurveWithBalanceEquation(
				xCGPosition, 
				zCGPosition, 
				xACWing, 
				zACWing, 
				wingMeanAerodynamicChord, 
				wingSurface, 
				wingFuselageLiftCoefficient, 
				wingDragCoefficient, 
				wingMomentCoefficient, 
				alphaBodyList, 
				pendularStability
				);
		
	
			//FUSELAGE----------------------------
			// moment with respect to CG
			fuselageMomentCoefficientWithRespectToCG = calculateCMFuselageCurveWithBalanceEquation(
					xCGPosition,
					zCGPosition,
					xCGFuselage,
					zCGFuselage,
					wingMeanAerodynamicChord, 
					wingSurface, 
					fuselageMomentCoefficient, 
					fuselageDragCoefficient, 
					alphaBodyList,
					pendularStability
					);

			//NACELLE----------------------------
			// moment with respect to CG
			nacelleMomentCoefficientWithRespectToCG = calculateCMNacelleCurveWithBalanceEquation(
					xCGPosition,
					zCGPosition,
					xCGNacelle,
					zCGNacelle,
					wingMeanAerodynamicChord, 
					wingSurface, 
					fuselageMomentCoefficient, 
					fuselageDragCoefficient, 
					alphaBodyList,
					pendularStability
					);
			
			
			//HORIZONTAL TAIL----------------------------
			// moment with respect to CG
			horizontalTailMomentCoefficientWithRespectToCG = calculateCMHTailCurveWithBalanceEquation(
					xCGPosition,
					zCGPosition, 
					xACHorizontalTail, 
					zACHorizontalTail, 
					wingMeanAerodynamicChord, 
					horizontalTailMeanAerodynamicChord, 
					wingSurface, 
					horizontalTailSurface, 
					horizontalTailLiftCoefficient, 
					horizontalTailDragCoefficient, 
					horizontalTailMomentCoefficient, 
					horizontalTailDynamicPressureRatio, 
					alphaBodyList,
					pendularStability
					);

			//LANDING GEAR----------------------------
			// moment with respect to CG
			landingGearMomentCoefficientWithRespectToCG = calculateCMLandingGearCurveWithBalanceEquation(
					xCGPosition,
					zCGPosition,
					xCGLandingGears,
					zCGLandingGears,
					wingMeanAerodynamicChord, 
					wingSurface, 
					landingGearDragCoefficient,
					alphaBodyList,
					pendularStability
					);
			

				for (int i=0; i<alphaBodyList.size(); i++) {

			//TOTAL MOMENT COEFFICIENT
			totalMomentCoefficient.add(
					wingMomentCoefficientWithRespectToCG.get(i)+
					fuselageMomentCoefficientWithRespectToCG.get(i)+
					horizontalTailMomentCoefficientWithRespectToCG.get(i)+
					landingGearMomentCoefficientWithRespectToCG.get(i)+ 
					nacelleMomentCoefficientWithRespectToCG.get(i)
					);
		}


		return totalMomentCoefficient;
	}
	
	public static List<Double> calculateCMWingCurveWithBalanceEquation(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xACWing,
			Amount<Length> zACWing,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Area> wingSurface,
			List<Double> wingFuselageLiftCoefficient,
			List<Double> wingDragCoefficient,
			List<Double> wingMomentCoefficient,
//			List<Amount<Angle>> alphaBodyList,
			List<Amount<Angle>> alphaWingList,
			boolean pendularStability
			) {

		List<Double> wingNormalCoefficient = new ArrayList<>();
		List<Double> wingHorizontalCoeffient = new ArrayList<>();
		List<Double> wingMomentCoefficientWithRespectToCG = new ArrayList<>();

		//DISTANCES--------
		//Wing	
		Amount<Length> wingHorizontalDistanceACtoCG = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xACWing.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalWingHorizontalDistance = 
				wingHorizontalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		Amount<Length> wingVerticalDistanceACtoCG = Amount.valueOf(
				zACWing.doubleValue(SI.METER) - zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalWingVerticalDistance = 
				wingVerticalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		//MOMENT CALCULATION
		alphaWingList.stream().forEach( aw-> {

			int i = alphaWingList.indexOf(aw);

			// WING -----------------------------
			// forces
			wingNormalCoefficient.add(
					wingFuselageLiftCoefficient.get(i)*Math.cos(aw.doubleValue(SI.RADIAN))+
					wingDragCoefficient.get(i)*Math.sin(aw.doubleValue(SI.RADIAN))
					);

			wingHorizontalCoeffient.add(
					wingDragCoefficient.get(i)*Math.cos(aw.doubleValue(SI.RADIAN)) - 
					wingFuselageLiftCoefficient.get(i)*Math.sin(aw.doubleValue(SI.RADIAN)));		

			// moment with respect to CG
			if(pendularStability == true){
				wingMomentCoefficientWithRespectToCG.add(
						wingNormalCoefficient.get(i)* nondimensionalWingHorizontalDistance+
						wingHorizontalCoeffient.get(i)* nondimensionalWingVerticalDistance+
						wingMomentCoefficient.get(i)
						);
			}
			if(pendularStability == false){
				wingMomentCoefficientWithRespectToCG.add(
						wingNormalCoefficient.get(i)* nondimensionalWingHorizontalDistance+
						wingMomentCoefficient.get(i)
						);
			}
		});
			return wingMomentCoefficientWithRespectToCG;
		}
	
	public static List<Double> calculateCMHTailCurveWithBalanceEquation(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xACHorizontalTail,
			Amount<Length> zACHorizontalTail,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Length> horizontalTailMeanAerodynamicChord,
			Amount<Area> wingSurface,
			Amount<Area> horizontalTailSurface,
			List<Double> horizontalTailLiftCoefficient,
			List<Double> horizontalTailDragCoefficient,
			List<Double> horizontalTailMomentCoefficient,
			Double horizontalTailDynamicPressureRatio,
//			List<Amount<Angle>> alphaBodyList
			List<Amount<Angle>> alphaHTailList,
			boolean pendularStability
			) {

		List<Double> horizontalTailMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> horizontalTailNormalCoefficient = new ArrayList<>();
		List<Double> horizontalTailHorizontalCoeffient = new ArrayList<>();
		
		Double horizontalTailWingSurfaceRatio;
		Double horizontalTailWingMeanAerodynamicChordRatio;

		//DISTANCES--------


		//Horizontal tail
		Amount<Length> horizontalTailHorizontalDistanceACtoCG = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xACHorizontalTail.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalHorizontalTailHorizontalDistance = 
				horizontalTailHorizontalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		Amount<Length> horizontalTailVerticalDistanceACtoCG = Amount.valueOf(
				zACHorizontalTail.doubleValue(SI.METER) - zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalHorizontalTailVerticalDistance = 
				horizontalTailVerticalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		// surface ratio
		horizontalTailWingSurfaceRatio = horizontalTailSurface.doubleValue(SI.SQUARE_METRE)/
				wingSurface.doubleValue(SI.SQUARE_METRE);

		//chords ratio
		horizontalTailWingMeanAerodynamicChordRatio = horizontalTailMeanAerodynamicChord.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		//MOMENT CALCULATION
		alphaHTailList.stream().forEach( ah-> {

			int i = alphaHTailList.indexOf(ah);

			horizontalTailNormalCoefficient.add(
					horizontalTailLiftCoefficient.get(i)*Math.cos(ah.doubleValue(SI.RADIAN))+
					horizontalTailDragCoefficient.get(i)*Math.sin(ah.doubleValue(SI.RADIAN))
					);

			horizontalTailHorizontalCoeffient.add(
					horizontalTailDragCoefficient.get(i)*Math.cos(ah.doubleValue(SI.RADIAN)) - 
					horizontalTailLiftCoefficient.get(i)*Math.sin(ah.doubleValue(SI.RADIAN)));	
			
			//HORIZONTAL TAIL----------------------------
			// moment with respect to CG
			if(pendularStability == true){
				horizontalTailMomentCoefficientWithRespectToCG.add(
						(horizontalTailNormalCoefficient.get(i)*
								nondimensionalHorizontalTailHorizontalDistance*
								horizontalTailDynamicPressureRatio*
								horizontalTailWingSurfaceRatio)
						+ (horizontalTailHorizontalCoeffient.get(i)*
								nondimensionalHorizontalTailVerticalDistance*
								horizontalTailDynamicPressureRatio*
								horizontalTailWingSurfaceRatio)
						+ (horizontalTailMomentCoefficient.get(i)*
								horizontalTailDynamicPressureRatio*
								horizontalTailWingSurfaceRatio*
								horizontalTailWingMeanAerodynamicChordRatio)
						);
			}
			else {
				horizontalTailMomentCoefficientWithRespectToCG.add(
						(horizontalTailNormalCoefficient.get(i)*
								nondimensionalHorizontalTailHorizontalDistance*
								horizontalTailDynamicPressureRatio*
								horizontalTailWingSurfaceRatio)
						+ (horizontalTailMomentCoefficient.get(i)*
								horizontalTailDynamicPressureRatio*
								horizontalTailWingSurfaceRatio*
								horizontalTailWingMeanAerodynamicChordRatio)
						);
			}

		});


		return horizontalTailMomentCoefficientWithRespectToCG;
	}
	
	public static List<Double> calculateCMFuselageCurveWithBalanceEquation(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xCGFuselage,
			Amount<Length> zCGFuselage,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Area> wingSurface,
			List<Double> fuselageMomentCoefficient,
			List<Double> fuselageDragCoefficient,
			List<Amount<Angle>> alphaBodyList,
			boolean pendularStability
			) {

		List<Double> fuselageMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> fuselageNormalCoefficient = new ArrayList<>();
		List<Double> fuselageHorizontalCoeffient = new ArrayList<>();

		//DISTANCES--------
		
		Amount<Length> fuselageHorizontalDistance = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xCGFuselage.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalFuselageHorizontalDistance = 
				fuselageHorizontalDistance.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		Amount<Length> fuselageVerticalDistance = Amount.valueOf(
				zCGFuselage.doubleValue(SI.METER) - zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalFuselageVerticalDistance = 
				fuselageVerticalDistance.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		
		//MOMENT CALCULATION
		alphaBodyList.stream().forEach( ab-> {

			int i = alphaBodyList.indexOf(ab);

			fuselageNormalCoefficient.add(
					fuselageDragCoefficient.get(i)*Math.sin(ab.doubleValue(SI.RADIAN))
					);

			fuselageHorizontalCoeffient.add(
					fuselageDragCoefficient.get(i)*Math.cos(ab.doubleValue(SI.RADIAN))
					); 

			//FUSELAGE----------------------------
			// moment with respect to CG
			if(pendularStability == true)
				fuselageMomentCoefficientWithRespectToCG.add(
						fuselageMomentCoefficient.get(i) 
						+ fuselageNormalCoefficient.get(i)*nondimensionalFuselageHorizontalDistance
						+ fuselageHorizontalCoeffient.get(i)*nondimensionalFuselageVerticalDistance
						);
			else
				fuselageMomentCoefficientWithRespectToCG.add(
						fuselageMomentCoefficient.get(i) 
						+ fuselageNormalCoefficient.get(i)*nondimensionalFuselageHorizontalDistance
						);

		});

		return fuselageMomentCoefficientWithRespectToCG;
	}
	
	public static List<Double> calculateCMNacelleCurveWithBalanceEquation(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xCGNacelle,
			Amount<Length> zCGNacelle,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Area> wingSurface,
			List<Double> nacelleMomentCoefficient,
			List<Double> nacelleDragCoefficient,
			List<Amount<Angle>> alphaNacelleList,
			boolean pendularStability
			) {

		List<Double> nacelleMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> nacelleNormalCoefficient = new ArrayList<>();
		List<Double> nacelleHorizontalCoeffient = new ArrayList<>();

		//DISTANCES--------
		
		Amount<Length> nacelleHorizontalDistance = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xCGNacelle.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalNacelleHorizontalDistance = 
				nacelleHorizontalDistance.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		Amount<Length> nacelleVerticalDistance = Amount.valueOf(
				zCGNacelle.doubleValue(SI.METER) - zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalNacelleVerticalDistance = 
				nacelleVerticalDistance.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);


		//MOMENT CALCULATION
		alphaNacelleList.stream().forEach( an-> {

			int i = alphaNacelleList.indexOf(an);

			nacelleNormalCoefficient.add(
					nacelleDragCoefficient.get(i)*Math.sin(an.doubleValue(SI.RADIAN))
					);

			nacelleHorizontalCoeffient.add(
					nacelleDragCoefficient.get(i)*Math.cos(an.doubleValue(SI.RADIAN))
					); 
			
			//NACELLE----------------------------
			// moment with respect to CG
			if(pendularStability == true)
				nacelleMomentCoefficientWithRespectToCG.add(
						nacelleMomentCoefficient.get(i) 
						+ nacelleNormalCoefficient.get(i)*nondimensionalNacelleHorizontalDistance
						+ nacelleHorizontalCoeffient.get(i)*nondimensionalNacelleVerticalDistance
						);
			else
				nacelleMomentCoefficientWithRespectToCG.add(
						nacelleMomentCoefficient.get(i) 
						+ nacelleNormalCoefficient.get(i)*nondimensionalNacelleHorizontalDistance
						);
		});

		return nacelleMomentCoefficientWithRespectToCG;
	}
	
	public static List<Double> calculateCMLandingGearCurveWithBalanceEquation(
			Amount<Length> xCGPosition,
			Amount<Length> zCGPosition,
			Amount<Length> xCGLandingGear,
			Amount<Length> zCGLandingGear,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Area> wingSurface,
			Double landingGearDragCoefficient,
			List<Amount<Angle>> alphaBodyList,
			boolean pendularStability
			) {

		List<Double> landingGearsMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> landingGearsNormalCoefficient = new ArrayList<>();
		List<Double> landingGearsHorizontalCoeffient = new ArrayList<>();

		//DISTANCES--------
		// landing gear
		Amount<Length> landingGearsHorizontalDistance = Amount.valueOf(
				xCGPosition.doubleValue(SI.METER) - xCGLandingGear.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalLandingGearsHorizontalDistance = 
				landingGearsHorizontalDistance.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);
		Amount<Length> landingGearsVerticalDistance = Amount.valueOf(
				zCGLandingGear.doubleValue(SI.METER) - zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalLandingGearsVerticalDistance = 
				landingGearsVerticalDistance.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		//MOMENT CALCULATION
		alphaBodyList.stream().forEach( ab-> {

			int i = alphaBodyList.indexOf(ab);

			landingGearsNormalCoefficient.add(
					landingGearDragCoefficient*Math.sin(ab.doubleValue(SI.RADIAN))
					);

			landingGearsHorizontalCoeffient.add(
					landingGearDragCoefficient*Math.cos(ab.doubleValue(SI.RADIAN))
					); 
			
			//LANDING GEAR----------------------------
			// moment with respect to CG
			if(pendularStability == true)
				landingGearsMomentCoefficientWithRespectToCG.add(
						landingGearsNormalCoefficient.get(i)*nondimensionalLandingGearsHorizontalDistance + 
						landingGearsHorizontalCoeffient.get(i)*nondimensionalLandingGearsVerticalDistance
						);
			else
				landingGearsMomentCoefficientWithRespectToCG.add(
						landingGearsNormalCoefficient.get(i)*nondimensionalLandingGearsHorizontalDistance  
						);
		}
				);
		return landingGearsMomentCoefficientWithRespectToCG;
	}
}







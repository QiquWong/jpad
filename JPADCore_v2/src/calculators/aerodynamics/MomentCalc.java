package calculators.aerodynamics;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.sun.javafx.geom.transform.BaseTransform.Degree;

import aircraft.components.fuselage.Fuselage;
import analyses.liftingsurface.LSAerodynamicsManager.CalcAlpha0L;
import calculators.geometry.FusNacGeometryCalc;
import calculators.geometry.LSGeometryCalc;
import calculators.stability.StabilityCalculators;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.DirStabEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import database.databasefunctions.aerodynamics.vedsc.VeDSCDatabaseReader;
import jahuwaldt.tools.units.Degrees;
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
										*chordDistribution.get(chordDistribution.indexOf(cmac)).doubleValue(SI.METER)
										*chordDistribution.get(chordDistribution.indexOf(cmac)).doubleValue(SI.METER)
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

				if (alphaDistribution[ii]<anglesOfAttack.get(0).doubleValue(NonSI.DEGREE_ANGLE)){
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
				alphaDistribution [ii] = (clDistribution[ii] - liftingSurfaceCl0Distribution.get(ii))/
						liftingSurfaceCLAlphaDegDistribution.get(ii);

				if (alphaDistribution[ii]<anglesOfAttack.get(0).doubleValue(NonSI.DEGREE_ANGLE)){
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

				cmActual = MyMathUtils.getInterpolatedValue1DLinear(
						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCLforCMMatrix),
						MyArrayUtils.convertToDoublePrimitive(liftingSurfaceCmC4Distribution.get(ii)),
						clInducedDistributionAtAlphaNew[ii]
						);
				

				distancesArrayAC[ii] =
						momentumPole.doubleValue(SI.METER) - 
						(liftingSurfaceXLEDistribution.get(ii).doubleValue(SI.METER) +
								(liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER)/4));

				cmDistribution [ii] = clInducedDistributionAtAlphaNew[ii] * 
						(distancesArrayAC[ii]/
								liftingSurfaceChordDistribution.get(ii).doubleValue(SI.METER))+cmActual;

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
	
	public static Double calculateCM0FuselageMulthopp(
			Amount<Length> fuselageLength,
			Amount<Length> noseLength,
			Amount<Length> cabinLength,
			Double k2k1,
			Amount<Area> wingSurface,
			Amount<Angle> wingRiggingAngle,
			Amount<Angle> wingAlphaZeroLift,
			Amount<Length> wingMeanAerodynamicChord,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {

		Double cM0 = 0.0;
		Double sum = 0.0;
		double[] x = MyArrayUtils.linspace(
				0.,
				fuselageLength.doubleValue(SI.METER)*(1-0.0001),
				100
				);

		try {
			for(int i=1; i<x.length; i++){
				sum = sum 
						+ pow(FusNacGeometryCalc.getWidthAtX(x[i], outlineXYSideRCurveX, outlineXYSideRCurveY),2)
						*(FusNacGeometryCalc.getCamberAngleAtXFuselage(
								x[i],
								outlineXZUpperCurveX,
								outlineXZUpperCurveZ,
								outlineXZLowerCurveX,
								outlineXZLowerCurveZ,
								noseLength,
								cabinLength
								) 
								+ wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
								+ Math.abs(wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE))
								)
						* (x[i] - x[i-1]);
			}

			cM0 = k2k1
					/(36.5
							*wingSurface.doubleValue(SI.SQUARE_METRE)
							*wingMeanAerodynamicChord.doubleValue(SI.METER)
							) 
					* sum;

		} catch (NullPointerException e) {
			cM0 = 0.0;
		}

		return cM0;
	}

	public static Double calculateCM0NacelleMulthopp(
			Amount<Length> nacelleLength,
			Double k2k1,
			Amount<Area> wingSurface,
			Amount<Angle> wingRiggingAngle,
			Amount<Angle> wingAlphaZeroLift,
			Amount<Length> wingMeanAerodynamicChord,
			List<Double> outlineXZUpperCurveX,
			List<Double> outlineXZUpperCurveZ,
			List<Double> outlineXZLowerCurveX,
			List<Double> outlineXZLowerCurveZ,
			List<Double> outlineXYSideRCurveX,
			List<Double> outlineXYSideRCurveY
			) {

		Double cM0 = 0.0;
		Double sum = 0.0;
		double[] x = MyArrayUtils.linspace(
				0.,
				nacelleLength.doubleValue(SI.METER)*(1-0.0001),
				100
				);

		try {
			for(int i=1; i<x.length; i++){
				sum = sum 
						+ pow(FusNacGeometryCalc.getWidthAtX(x[i], outlineXYSideRCurveX, outlineXYSideRCurveY),2)
						*(FusNacGeometryCalc.getCamberAngleAtXNacelle(
								x[i],
								outlineXZUpperCurveX,
								outlineXZUpperCurveZ,
								outlineXZLowerCurveX,
								outlineXZLowerCurveZ
								) 
								+ wingRiggingAngle.doubleValue(NonSI.DEGREE_ANGLE)
								+ Math.abs(wingAlphaZeroLift.doubleValue(NonSI.DEGREE_ANGLE))
								)
						* (x[i] - x[i-1]);
			}

			cM0 = k2k1
					/(36.5
							*wingSurface.doubleValue(SI.SQUARE_METRE)
							*wingMeanAerodynamicChord.doubleValue(SI.METER)
							) 
					* sum;

		} catch (NullPointerException e) {
			cM0 = 0.0;
		}

		return cM0;
	}
	
	public static Amount<?> calculateCMAlphaFuselageOrNacelleGilruth(
			Amount<Length> length,
			Amount<Length> maxWidth,
			double[] positionOfC4ToFuselageOrNacelleLength,
			double[] kF,
			Amount<Area> wingSurface,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Length> wingXApex,
			Amount<Length> wingRootChord
			) {

		Double cMAlpha = 0.0;

		double kf = MyMathUtils
				.interpolate1DLinear(positionOfC4ToFuselageOrNacelleLength, kF)
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
			Amount<Length> zLandingGear,
			Amount<Length> wingMeanAerodynamicChord,
			Amount<Length> horizontalTailMeanAerodynamicChord,
			Amount<Area> wingSurface,
			Amount<Area> horizontalTailSurface,
			List<Double> wingFuselageLiftCoefficient,
			List<Double> wingDragCoefficient,
			List<Double> wingMomentCoefficient,
			List<Double> fuselageMomentCoefficient,
			List<Double> fuselageDragCoefficient,
			List<Double> horizontalTailLiftCoefficient,
			List<Double> horizontalTailDragCoefficient,
			List<Double> horizontalTailMomentCoefficient,
			Double landingGearDragCoefficient,
			Double horizontalTailDynamicPressureRatio,
			List<Amount<Angle>> alphaBodyList,
			boolean pendularStability
			) {

		List<Double> totalMomentCoefficient = new ArrayList<>();
		List<Double> wingNormalCoefficient = new ArrayList<>();
		List<Double> wingHorizontalCoeffient = new ArrayList<>();
		List<Double> wingMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> fuselageMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> horizontalTailMomentCoefficientWithRespectToCG = new ArrayList<>();
		List<Double> landingGearMomentCoefficientWithRespectToCG = new ArrayList<>();

		Double horizontalTailWingSurfaceRatio;
		Double horizontalTailWingMeanAerodynamicChordRatio;

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

		//Fuselage
		Amount<Length> fuselageVerticalDistanceACtoCG = Amount.valueOf(
				- zCGPosition.doubleValue(SI.METER),
				SI.METER);
		Double nondimensionalFuselageVerticalDistance = 
				fuselageVerticalDistanceACtoCG.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

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

		// landing gear
		Double nonDimensionalLandingGearArm = (zLandingGear.doubleValue(SI.METER)-zCGPosition.doubleValue(SI.METER))/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		// surface ratio
		horizontalTailWingSurfaceRatio = horizontalTailSurface.doubleValue(SI.SQUARE_METRE)/
				wingSurface.doubleValue(SI.SQUARE_METRE);

		//chords ratio
		horizontalTailWingMeanAerodynamicChordRatio = horizontalTailMeanAerodynamicChord.doubleValue(SI.METER)/
				wingMeanAerodynamicChord.doubleValue(SI.METER);

		//MOMENT CALCULATION
		alphaBodyList.stream().forEach( ab-> {

			int i = alphaBodyList.indexOf(ab);

			// WING -----------------------------
			// forces
			wingNormalCoefficient.add(
					wingFuselageLiftCoefficient.get(i)*Math.cos(ab.doubleValue(SI.RADIAN))+
					wingDragCoefficient.get(i)*Math.sin(ab.doubleValue(SI.RADIAN))
					);

			wingHorizontalCoeffient.add(
					wingDragCoefficient.get(i)*Math.cos(ab.doubleValue(SI.RADIAN)) - 
					wingFuselageLiftCoefficient.get(i)*Math.sin(ab.doubleValue(SI.RADIAN)));		

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

			//FUSELAGE----------------------------
			// moment with respect to CG
			fuselageMomentCoefficientWithRespectToCG.add(
					fuselageMomentCoefficient.get(i) + fuselageDragCoefficient.get(i)*nondimensionalFuselageVerticalDistance
					);

			//HORIZONTAL TAIL----------------------------
			// moment with respect to CG
			horizontalTailMomentCoefficientWithRespectToCG.add(
					horizontalTailLiftCoefficient.get(i)*
					nondimensionalHorizontalTailHorizontalDistance*
					horizontalTailDynamicPressureRatio*
					horizontalTailWingSurfaceRatio+
					horizontalTailDragCoefficient.get(i)*
					nondimensionalHorizontalTailVerticalDistance*
					horizontalTailDynamicPressureRatio*
					horizontalTailWingSurfaceRatio+
					horizontalTailMomentCoefficient.get(i)*
					horizontalTailDynamicPressureRatio*
					horizontalTailWingSurfaceRatio*
					horizontalTailWingMeanAerodynamicChordRatio
					);

			//LANDING GEAR----------------------------
			// moment with respect to CG
			landingGearMomentCoefficientWithRespectToCG.add(
					landingGearDragCoefficient* nonDimensionalLandingGearArm
					);

			//TOTAL MOMENT COEFFICIENT
			totalMomentCoefficient.add(
					wingMomentCoefficientWithRespectToCG.get(i)+
					fuselageMomentCoefficientWithRespectToCG.get(i)+
					horizontalTailMomentCoefficientWithRespectToCG.get(i)+
					landingGearMomentCoefficientWithRespectToCG.get(i)
					);
		}
				);


		return totalMomentCoefficient;
	}
}







package optimization;

import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLmax;
import analyses.liftingsurface.LSAerodynamicsManager.CalcPolar;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;

public class CostFunctions {

	public static Double cDMinWing (LSAerodynamicsManager wingAerodynamicsManager) {
		
		CalcPolar calcPolar = wingAerodynamicsManager.new CalcPolar();
		calcPolar.fromCdDistribution(
				wingAerodynamicsManager.getCurrentMachNumber(),
				wingAerodynamicsManager.getCurrentAltitude()
				);
		
		return MyArrayUtils.getMin(wingAerodynamicsManager.getPolar3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION)); 
		
	}
	
	public static Double cLMaxWing (LSAerodynamicsManager wingAerodynamicsManager) {
		
		CalcCLmax calcCLmax = wingAerodynamicsManager.new CalcCLmax();
		calcCLmax.nasaBlackwell();
		
		// THE SIGN (-) IS FOR A MINIMIZATION PROBLEM
		return -wingAerodynamicsManager.getCLMax().get(MethodEnum.NASA_BLACKWELL); 
		
	}

	public static Double cLmaxVsCDminWing (
			double cLmaxWeight,
			double cDMinWeight,
			double scaleFactorCDMin,
			LSAerodynamicsManager wingAerodynamicsManager
			) {
		
		CalcPolar calcPolar = wingAerodynamicsManager.new CalcPolar();
		calcPolar.fromCdDistribution(
				wingAerodynamicsManager.getCurrentMachNumber(),
				wingAerodynamicsManager.getCurrentAltitude()
				);
		
		Double cDmin = MyArrayUtils.getMin(wingAerodynamicsManager.getPolar3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION));
		
		CalcCLmax calcCLmax = wingAerodynamicsManager.new CalcCLmax();
		calcCLmax.nasaBlackwell();
		
		Double cLmax = wingAerodynamicsManager.getCLMax().get(MethodEnum.NASA_BLACKWELL);
		
		return (-cLmax*cLmaxWeight) + (cDmin*cDMinWeight*scaleFactorCDMin);
		
	}
}

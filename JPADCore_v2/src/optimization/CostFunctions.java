package optimization;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager.CalcCLmax;
import analyses.liftingsurface.LSAerodynamicsManager.CalcPolar;
import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyUnits;
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
			double referenceCDMin,
			LSAerodynamicsManager wingAerodynamicsManager
			) {
		
		//..................................................................................................................
		CalcPolar calcPolar = wingAerodynamicsManager.new CalcPolar();
		calcPolar.fromCdDistribution(
				wingAerodynamicsManager.getCurrentMachNumber(),
				wingAerodynamicsManager.getCurrentAltitude()
				);
		
		Double cDmin = MyArrayUtils.getMin(wingAerodynamicsManager.getPolar3DCurve().get(MethodEnum.AIRFOIL_DISTRIBUTION));
		
		//..................................................................................................................
		CalcCLmax calcCLmax = wingAerodynamicsManager.new CalcCLmax();
		calcCLmax.nasaBlackwell();
		
		Double cLmax = wingAerodynamicsManager.getCLMax().get(MethodEnum.NASA_BLACKWELL);
		
		return (-cLmax*cLmaxWeight) + (cDmin*cDMinWeight/referenceCDMin);
		
	}
	
	public static Double cLmaxVsMassWing (
			double cLmaxWeight,
			double massWeight,
			double referenceMass,
			double ultimateLoadFactor,
			Amount<Mass> maxTakeOffMass,
			Amount<Mass> maxZeroFuelMass,
			LSAerodynamicsManager wingAerodynamicsManager
			) {
		
		//..................................................................................................................
		CalcCLmax calcCLmax = wingAerodynamicsManager.new CalcCLmax();
		calcCLmax.nasaBlackwell();
		
		Double cLmax = wingAerodynamicsManager.getCLMax().get(MethodEnum.NASA_BLACKWELL);

		//..................................................................................................................
		Amount<Area> wingSurface = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSurfacePlanform();
		Amount<Length> wingSpan = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSpan();
		Double taperRatio = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> sweepQuarterChord = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord();
		Double thicknessMean = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getThicknessMean();
		
		Amount<Mass> wingMass = Amount.valueOf(
				(4.22*wingSurface.doubleValue(MyUnits.FOOT2) 
						+ 1.642e-6
						* (ultimateLoadFactor
								* Math.pow(wingSpan.doubleValue(NonSI.FOOT),3)
								* Math.sqrt(maxTakeOffMass.doubleValue(NonSI.POUND)*maxZeroFuelMass.doubleValue(NonSI.POUND))
								* (1 + 2*taperRatio)
								)
						/(thicknessMean
								* Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),2)
								* wingSurface.doubleValue(MyUnits.FOOT2)
								* (1 + taperRatio)
								)
						),
				NonSI.POUND).to(SI.KILOGRAM);
		
		return (-cLmax*cLmaxWeight) + (wingMass.doubleValue(SI.KILOGRAM)*massWeight/referenceMass);
		
	}
	
	public static Double wingMass (
			double ultimateLoadFactor,
			Amount<Mass> maxTakeOffMass,
			Amount<Mass> maxZeroFuelMass,
			LSAerodynamicsManager wingAerodynamicsManager
			) {
		
		Amount<Area> wingSurface = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSurfacePlanform();
		Amount<Length> wingSpan = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getSpan();
		Double taperRatio = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio();
		Amount<Angle> sweepQuarterChord = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord();
		Double thicknessMean = wingAerodynamicsManager.getTheLiftingSurface().getLiftingSurfaceCreator().getThicknessMean();
		
		Amount<Mass> wingMass = Amount.valueOf(
				(4.22*wingSurface.doubleValue(MyUnits.FOOT2) 
						+ 1.642e-6
						* (ultimateLoadFactor
								* Math.pow(wingSpan.doubleValue(NonSI.FOOT),3)
								* Math.sqrt(maxTakeOffMass.doubleValue(NonSI.POUND)*maxZeroFuelMass.doubleValue(NonSI.POUND))
								* (1 + 2*taperRatio)
								)
						/(thicknessMean
								* Math.pow(Math.cos(sweepQuarterChord.doubleValue(SI.RADIAN)),2)
								* wingSurface.doubleValue(MyUnits.FOOT2)
								* (1 + taperRatio)
								)
						),
				NonSI.POUND).to(SI.KILOGRAM);
		
		return wingMass.doubleValue(SI.KILOGRAM);
		
	}
	
}

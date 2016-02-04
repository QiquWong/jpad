package sandbox.mr;

import java.io.File;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcXAC;
import configuration.MyConfiguration;

public class DownwashCalculator {

	// VARIABLE DECLARATION
	
	private double bRLineZeroLiftLine;
	private double distTrailingEdgeWingXACH;
	Aircraft aircraft;
	double zHTailAC, cRootExposedWing , angleOfIncidenceExposed, zWing, distAerodynamicCenter,
	alphaZeroLiftRootExposed, xACLRF, xACRootExposed, angleOfIncidenceExposedDeg,
	alphaZeroLiftRootExposedDeg;
	private double[] downwashArray;
	private double[] alphaArray ;
	private double[] downwashGradientArray;
	private double[] zDistanceArray;
	private String subfolderPath;
	private boolean subfolderPathCeck = true;
	private boolean plotEpsilonCeck = false;
	private boolean plotDeltaEpsilonCeck = false;
	private boolean plotZCeck = false;
	String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
	private int nValuePlot;
	private double deltaAlpha;
	
	//BUILDER

	public DownwashCalculator(
			Aircraft aircraft
			) {

		this.aircraft = aircraft;


		zHTailAC = aircraft.get_HTail().get_Z0().getEstimatedValue();
		cRootExposedWing = aircraft.get_exposedWing().get_theAirfoilsListExposed()
				.get(0).get_chordLocal();
		angleOfIncidenceExposed = aircraft.get_wing().get_iw().getEstimatedValue()
				+ aircraft.get_exposedWing().get_twistDistributionExposed().get(0);
		angleOfIncidenceExposedDeg = Amount.valueOf(
				Math.toDegrees(angleOfIncidenceExposed), SI.RADIAN).getEstimatedValue();

		zWing = aircraft.get_wing().get_Z0().getEstimatedValue();
		distAerodynamicCenter = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		alphaZeroLiftRootExposed = aircraft.get_exposedWing().get_alpha0lDistributionExposed().get(0);
		alphaZeroLiftRootExposedDeg = Amount.valueOf(
				Math.toDegrees(alphaZeroLiftRootExposed), SI.RADIAN).getEstimatedValue();

		LSAerodynamicsManager.CalcXAC theXACCalculator = aircraft.get_wing().getAerodynamics().new CalcXAC();
		xACLRF = theXACCalculator.deYoungHarper() + aircraft.get_wing().get_xLEMacActualLRF().getEstimatedValue();
		xACRootExposed = xACLRF - aircraft.get_wing().getXLEAtYActual(aircraft.get_fuselage().getWidthAtX(
				aircraft.get_wing().get_xLEMacActualBRF().getEstimatedValue()).doubleValue());
	}

}

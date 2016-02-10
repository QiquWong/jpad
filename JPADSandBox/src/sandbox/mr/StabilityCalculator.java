package sandbox.mr;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;

public class StabilityCalculator {

	/**
	 * This method evaluates the tau factor reading external databases. 
	 * 
	 * @param chordRatio --> cf/c
	 * @param aircraft
	 * @param angle of deflection of the elevator in deg or radians
	 *
	 * @author  Manuela Ruocco
	 */
	
	public double calculateTauIndex(double chordRatio, 
			Aircraft aircraft, 
			Amount<Angle> deflection
			)
	{
		if (deflection.getUnit() == SI.RADIAN){
			deflection = deflection.to(NonSI.DEGREE_ANGLE);
		}
		double deflectionAngleDeg = deflection.getEstimatedValue();
		
		double aspectratioHorizontalTail = aircraft.get_HTail().get_aspectRatio();
		
		double etaDelta = aircraft
				.get_theAerodynamics()
				.get_highLiftDatabaseReader()
				.get_eta_delta_vs_delta_flap_plain(deflectionAngleDeg, chordRatio);
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

	/**
	 * This method evaluates the lift coefficient of the entire aircraft at alpha= alpha body. 
	 * 
	 * @param aircraft
	 * @param angle of attack between the flow direction and the fuselage reference line
	 * @param angle of deflection of the elevator in deg or radians
	 * @param MyAirfoil the mean airfoil of the wing
	 * @param chord ratio of elevator
	 * @param eta the pressure ratio. For T tail is 1.
	 * 
	 *@return CL 
	 *
	 * @author  Manuela Ruocco
	 */
	public double claculateCLCompleteAircraft (Aircraft aircraft,
			Amount<Angle> alphaBody,
			MyAirfoil meanAirfoil,
			Amount<Angle> deflection,
			double chordRatio,
			double etaRatio
			)
	{
		
		LSAerodynamicsManager.CalcCLAtAlpha theCLWingCalculator = 
				aircraft.get_wing().getAerodynamics()
				.new CalcCLAtAlpha();
		double alphaWingAngle = alphaBody.getEstimatedValue()+ aircraft.get_wing().get_iw().getEstimatedValue();
		Amount<Angle> alphaWing = Amount.valueOf(alphaWingAngle, SI.RADIAN);
		
		double cLWing = theCLWingCalculator.nasaBlackwellCompleteCurve(alphaWing);
		
		System.out.println("the CL of wing at alpha body =(deg)" + 
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ " is " + cLWing);
		
		double cLWingBody = aircraft.get_theAerodynamics().calculateCLAtAlphaWingBody(
				alphaBody,
				meanAirfoil,
				false
				);
		
		System.out.println("the CL of wing body at alpha body =(deg)" + 
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue()
				+ " is " + cLWingBody);
		
		LSAerodynamicsManager.CalcCLAtAlpha theCLHorizontalTailCalculator = 
				aircraft.get_HTail().getAerodynamics()
				.new CalcCLAtAlpha();
		
		DownwashCalculator theDownwashCalculator = new DownwashCalculator(aircraft);
		theDownwashCalculator.calculateDownwashNonLinearDelft();
		double downwash = theDownwashCalculator.getDownwashAtAlphaBody(alphaBody);
		Amount<Angle> downwashAmount = Amount.valueOf(downwash, NonSI.DEGREE_ANGLE);
		
		double cLHTail = theCLHorizontalTailCalculator.getCLHTailatAlphaBodyWithElevator(chordRatio, alphaBody, deflection, downwashAmount);
		
		System.out.println("the CL of aircraft at alpha body =(deg)" + 
				alphaBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue() +
				" for delta = (deg) "
				+ deflection.getEstimatedValue() 
				+ " is " + cLHTail);
		
		double hTailSurface = aircraft.get_HTail().get_surface().getEstimatedValue();
		double wingSurface = aircraft.get_wing().get_surface().getEstimatedValue();
		
		double cLTotal = cLWingBody + cLHTail * (hTailSurface / wingSurface ) * etaRatio;
		return cLTotal;
	}
}

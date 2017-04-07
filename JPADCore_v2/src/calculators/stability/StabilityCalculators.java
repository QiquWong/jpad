package calculators.stability;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;

public class StabilityCalculators {

	/**
	 * This method evaluates the tau factor reading external databases. 
	 * 
	 * @param chordRatio --> cf/c
	 * @param aircraft
	 * @param angle of deflection of the elevator in deg or radians
	 *
	 * @author  Manuela Ruocco
	 */
	
	public static double calculateTauIndex(
			double chordRatio,
			double aspectRatioHTail,
			AerodynamicDatabaseReader aeroDatabaseReader,
			HighLiftDatabaseReader highLiftDatabaseReader,
			Amount<Angle> deflection
			)
	{
		if (deflection.getUnit() == SI.RADIAN){
			deflection = deflection.to(NonSI.DEGREE_ANGLE);
		}
		double deflectionAngleDeg = deflection.getEstimatedValue();
		
		double etaDelta = 
				highLiftDatabaseReader
				.getEtaDeltaVsDeltaFlapPlain(deflectionAngleDeg, chordRatio);
		//System.out.println(" eta delta = " + etaDelta );
		
		double deltaAlpha2D = 
				aeroDatabaseReader
				.getD_Alpha_d_Delta_2d_VS_cf_c(chordRatio);
		//System.out.println(" delta alfa 2d = " + deltaAlpha2D );
		
		double deltaAlpha2D3D = 
				aeroDatabaseReader
				.getD_Alpha_d_Delta_2d_d_Alpha_d_Delta_3D_VS_aspectRatio(
						aspectRatioHTail,
						deltaAlpha2D
						);
		//System.out.println(" delta alfa 3d/2d = " + deltaAlpha2D3D );
		
		double tauIndex = deltaAlpha2D3D * deltaAlpha2D * etaDelta;
		return tauIndex;
	}
	
	/**
	 * This method evaluates CL for an alpha array having the elevator deflection as input. 
	 * 
	 * @param angle of deflection of the elevator in deg or radians
	 *
	 * @author  Manuela Ruocco
	 */
	
	// The calculation of the lift coefficient with a deflection of the elevator is made by the
	// method 
	public void calculateCLWithElevatorDeflection (Amount<Angle> deflection){
		
		
	}

	
	
	
	
	
	
	
	
}

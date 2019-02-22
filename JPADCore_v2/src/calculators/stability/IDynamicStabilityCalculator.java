package calculators.stability;

import org.inferred.freebuilder.FreeBuilder;


@FreeBuilder
public interface IDynamicStabilityCalculator {

	double getDensity0();       	// air density
	double getReferenceArea();		// wing area
	double getReferenceChord(); 	// mean aerodynamic chord
	double getMass(); 				// total mass
	double getWingSpan(); 			// wingspan
	double getSpeed0(); 			// speed of the aircraft
	double getDynamicPressure0();   // dynamic pressure
	double getMach0();              // Mach number
	double getFlightPathAngle0();   // ramp angle
	double getElevationAngle0(); 	// Euler angle [rad] (assuming gamma0 = theta0)
	double getXCGPercentMAC();
	double getIXX(); 				// lateral-directional moment of inertia (IXX)
	double getIYY(); 				// longitudinal moment of inertia  (IYY)
	double getIZZ(); 				// lateral-directional moment of inertia (IZZ)
	double getIXZ(); 				// lateral-directional product of inertia (IXZ)
	double getCDrag0(); 			// drag coefficient at null incidence (Cd�) of the aircraft
	double getCDragAlpha0(); 		// linear drag gradient (CdAlpha�) of the aircraft
	double getCDragMach0(); 		// drag coefficient with respect to Mach (CdM�) of the aircraft
	double getCLift0(); 			// lift coefficient at null incidence (Cl�) of the aircraft
	double getCLiftAlpha0(); 		// linear lift gradient (ClAlpha�) of the aircraft
	double getCLiftAlphaDot0(); 	// linear lift gradient time derivative (ClAlpha_dot�) of the aircraft
	double getCLiftMach0();			// lift coefficient with respect to Mach (ClM�) of the aircraft
	double getCLiftQ0(); 			// lift coefficient with respect to q (ClQ�) of the aircraft
	double getCLiftDeltaE(); 		// lift coefficient with respect to delta_E (ClDelta_E�) of the aircraft
	double getCLiftDeltaT(); 		// lift coefficient with respect to delta_T (ClDelta_T�) of the aircraft
	double getCPitchAlpha0(); 		// pitching moment coefficient with respect to Alpha (CmAlpha�) of the aircraft
	double getCPitchAlphaDot0(); 	// pitching moment coefficient time derivative (CmAlpha_dot�) of the aircraft
	double getCPitchMach0(); 		// pitching moment coefficient with respect to Mach number
	double getCPitchQ0(); 			// pitching moment coefficient with respect to q
	double getCPitchDeltaE();		// pitching moment coefficient with respect to delta_E (CMDelta_E�) of the aircraft
	double getCPitchDeltaT(); 		// pitching moment coefficient with respect to delta_T (CMDelta_T�) of the aircraft
	double getCThrustFix(); 		// thrust coefficient at a fixed point ( U0 = u , delta_T = 1 )
	double getKVThrust(); 			// scale factor of the effect on the propulsion due to the speed
	double getCSideBeta0(); 			// lateral force coefficient with respect to beta (CyBeta) of the aircraft
	double getCSideP0(); 			// lateral force coefficient with respect to p (CyP) of the aircraft
	double getCSideR0(); 			// lateral force coefficient with respect to r (CyR) of the aircraft
	double getCSideDeltaA(); 		// lateral force coefficient with respect to delta_A (CyDelta_A) of the aircraft
	double getCSideDeltaR(); 		// lateral force coefficient with respect to delta_R (CyDelta_R) of the aircraft
	double getCRollBeta0(); 		// rolling moment coefficient with respect to beta (CLBeta) of the aircraft
	double getCRollP0(); 			// rolling moment coefficient with respect to a p (CLP) of the aircraft
	double getCRollR0(); 			// rolling moment coefficient with respect to a r (CLR) of the aircraft
	double getCRollDeltaA(); 		// rolling moment coefficient with respect to a delta_A (CLDelta_A) of the aircraft
	double getCRollDeltaR(); 		// rolling moment coefficient with respect to a delta_R (CLDelta_R) of the aircraft
	double getCYawBeta0(); 			// yawing moment coefficient with respect to a beta (CNBeta) of the aircraft
	double getCYawP0(); 			// yawing moment coefficient with respect to p (CNP) of the aircraft
	double getCYawR0(); 			// yawing moment coefficient with respect to r (CNR) of the aircraft
	double getCYawDeltaA(); 		// yawing moment coefficient with respect to delta_A (CNDelta_A) of the aircraft
	double getCYawDeltaR(); 		// yawing moment coefficient with respect to delta_R (CNDelta_R) of the aircraft

	class Builder extends IDynamicStabilityCalculator_Builder {
		public Builder() {

		}
	}
}

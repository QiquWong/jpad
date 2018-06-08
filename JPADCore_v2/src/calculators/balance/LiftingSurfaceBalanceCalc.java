package calculators.balance;

import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;

public class LiftingSurfaceBalanceCalc {

	/*
	 * page 359 Sforza (2014) - Aircraft Design
	 */
	public static Amount<Length> calculateWingXCGSforza (Aircraft aircraft) {
		
		Amount<Length> yCG = calculateWingYCGSforza(aircraft).times(2);
		
		return Amount.valueOf(
				(aircraft.getWing().getChordEquivalentAtY(yCG.doubleValue(SI.METER))/2)
				+ aircraft.getWing().getXLEAtYEquivalent(yCG.doubleValue(SI.METER))
				, SI.METER);
		
	}
	
	/*
	 * page 359 Sforza (2014) - Aircraft Design
	 */
	public static Amount<Length> calculateWingYCGSforza (Aircraft aircraft) {
		
		return Amount.valueOf(
				(aircraft.getWing().getSpan().doubleValue(SI.METER)/6)
				* ( (aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
						+ aircraft.getWing().getPanels().get(aircraft.getWing().getPanels().size()-1).getChordTip().times(2).doubleValue(SI.METER))
						/(aircraft.getWing().getPanels().get(0).getChordRoot().doubleValue(SI.METER)
								- aircraft.getWing().getPanels().get(aircraft.getWing().getPanels().size()-1).getChordTip().doubleValue(SI.METER))
						)
				/2,
				SI.METER
				); 
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 313
	 */
	public static Amount<Length> calculateWingXCGTorenbeek (Aircraft aircraft) {
		
		Amount<Length> xCG = Amount.valueOf(0.0, SI.METER);
		
		if (aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(NonSI.DEGREE_ANGLE) <= 5.0) {

			xCG = Amount.valueOf(
					0.4*aircraft.getWing().getChordEquivalentAtY(
							calculateWingYCGTorenbeek(aircraft).doubleValue(SI.METER)
							)
					+ aircraft.getWing().getXLEAtYEquivalent(
							calculateWingYCGTorenbeek(aircraft).doubleValue(SI.METER)
							), 
					SI.METER
					);

		}
		else {
			
			double xFrontSpar = aircraft.getWing().getMainSparDimensionlessPosition()
					* aircraft.getWing().getChordEquivalentAtY(
							calculateWingYCGTorenbeek(aircraft).doubleValue(SI.METER)
							);
			double xRearSpar = aircraft.getWing().getSecondarySparDimensionlessPosition()
					* aircraft.getWing().getChordEquivalentAtY(
							calculateWingYCGTorenbeek(aircraft).doubleValue(SI.METER)
							);

			xCG = Amount.valueOf(
					0.7*(xRearSpar - xFrontSpar)
					+ xFrontSpar
					+ aircraft.getWing().getXLEAtYEquivalent(
							calculateWingYCGTorenbeek(aircraft).doubleValue(SI.METER)
							), 
					SI.METER
					);
			
		}
		
		return xCG;
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 313
	 */
	public static Amount<Length> calculateWingYCGTorenbeek (Aircraft aircraft) {
		
		Amount<Length> yCG = Amount.valueOf(0.0, SI.METER);
		
		if (aircraft.getWing().getEquivalentWing().getPanels().get(0).getSweepHalfChord().doubleValue(NonSI.DEGREE_ANGLE) <= 5.0) {
			yCG = aircraft.getWing().getSemiSpan().to(SI.METER).times(0.40);
		}
		else {
			yCG = aircraft.getWing().getSemiSpan().to(SI.METER).times(0.35);
		}
		
		return yCG;
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 313
	 */
	public static Amount<Length> calculateHTailXCGTorenbeek (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.42*aircraft.getHTail().getChordEquivalentAtY(
						calculateHTailYCGTorenbeek(aircraft).doubleValue(SI.METER)
						)
				+ aircraft.getHTail().getXLEAtYEquivalent(
						calculateHTailYCGTorenbeek(aircraft).doubleValue(SI.METER)
						),
				SI.METER
				);
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 313
	 */
	public static Amount<Length> calculateHTailYCGTorenbeek (Aircraft aircraft) {
		
		return aircraft.getHTail().getSemiSpan().to(SI.METER).times(0.38);
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 313
	 */
	public static Amount<Length> calculateVTailXCGTorenbeek (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.42*aircraft.getVTail().getChordEquivalentAtY(
						calculateVTailYCGTorenbeek(aircraft).doubleValue(SI.METER)
						)
				+ aircraft.getVTail().getXLEAtYEquivalent(
						calculateVTailYCGTorenbeek(aircraft).doubleValue(SI.METER)
						),
				SI.METER
				);
		
	}
	
	/*
	 * Torenbeek: SYNTHESIS OF SUBSONIC AIRPLANE DESIGN pag. 313
	 */
	public static Amount<Length> calculateVTailYCGTorenbeek (Aircraft aircraft) {
		
		Amount<Length> yCG = Amount.valueOf(0.0, SI.METER);
		
		if (aircraft.getHTail().getPositionRelativeToAttachment() > 0.8) {
			yCG = aircraft.getVTail().getSemiSpan().to(SI.METER).times(0.55);
		}
		else {
			yCG = aircraft.getVTail().getSemiSpan().to(SI.METER).times(0.38);
		}
		
		return yCG;
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Length> calculateCanardXCGTorenbeek (Aircraft aircraft) {
		
		return Amount.valueOf(
				0.42*aircraft.getCanard().getChordEquivalentAtY(
						calculateCanardYCGTorenbeek(aircraft).doubleValue(SI.METER)
						)
				+ aircraft.getCanard().getXLEAtYEquivalent(
						calculateCanardYCGTorenbeek(aircraft).doubleValue(SI.METER)
						),
				SI.METER
				);
		
	}
	
	/*
	 * Same as HTail
	 */
	public static Amount<Length> calculateCanardYCGTorenbeek (Aircraft aircraft) {
		
		return aircraft.getCanard().getSemiSpan().to(SI.METER).times(0.38);
		
	}
	
}

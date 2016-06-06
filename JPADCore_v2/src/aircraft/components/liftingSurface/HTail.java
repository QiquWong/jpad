package aircraft.components.liftingSurface;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;

public class HTail extends LiftingSurface2Panels{


	public HTail(ComponentEnum type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	public HTail(String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			ComponentEnum type, 
			Fuselage theFuselage,
			Nacelle theNacelle,
			LiftingSurface2Panels ... liftingSurface) {
		
		super(name, 
				description, 
				x, y, z, 
				type, 
				theFuselage,
				theNacelle,
				liftingSurface);
		
		id = getId();

		// ATR 72 Data (matlab file)
		initializeDefaultSurface();

	}
	
	/**
	 * Overload of the previous builder that recognize aircraft name and initialize H-Tail with it's values. 
	 * 
	 *@author Vittorio Trifari 
	 */
	public HTail(AircraftEnum aircraftName,
			String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			ComponentEnum type, 
			Fuselage theFuselage,
			Nacelle theNacelle,
			LiftingSurface2Panels ... liftingSurface) {
		
		super(name, 
				description, 
				x, y, z, 
				type, 
				theFuselage,
				theNacelle,
				liftingSurface);
		
		id = getId();

		initializeDefaultSurface(aircraftName);

	}

	private void initializeDefaultSurface() {
		
		_type = ComponentEnum.HORIZONTAL_TAIL;
		_positionRelativeToAttachment = 1.0;
		_surface = Amount.valueOf(11.73, SI.SQUARE_METRE) ;
		_aspectRatio = 4.555;
		_taperRatioEquivalent = 0.57;
		// ATR72
		// _tipChord = 1.167 m;
		// _tipRoot = 2.047 m;
		_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(0.),SI.RADIAN);

		// if _spanStationKink=1.0 the wing has no crank (simply tapered wing)
		_spanStationKink = 1.0;  
		_extensionLERootChordLinPanel = 0.;
		_extensionTERootChordLinPanel = 0.;
		_iw = Amount.valueOf(0., SI.RADIAN);
		_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_CeCt = 0.3; // Elevator to tail chord ratio
		_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
		//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
		_xTransitionU = 0.12;
		_xTransitionL = 0.12;
		_roughness = Amount.valueOf(0.052e-5, SI.METER);

		//  INPUT DATA
		// Thickness of 3 section
		_tc_root = .12;               // value in %
		_tc_kink = .12;               // value in %
		_tc_tip = .12;              // value in %

		// Variable incidence horizontal tail ?
		_variableIncidence = false;

		_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
		_massReference = Amount.valueOf(192.6, SI.KILOGRAM);
		
		initializeAirfoils(this);
	}
	
	/**
	 * Overload of the previous method that recognize aircraft name and initialize surface data to the relative ones.
	 * 
	 * @author Vittorio Trifari
	 */
	private void initializeDefaultSurface(AircraftEnum aircraftName) {
		
		switch(aircraftName) {
		
		case ATR72:
			_type = ComponentEnum.HORIZONTAL_TAIL;
			_positionRelativeToAttachment = 1.0;
			_surface = Amount.valueOf(11.73, SI.SQUARE_METRE) ;
			_aspectRatio = 4.555;
			_taperRatioEquivalent = 0.57;
			// ATR72
			// _tipChord = 1.167 m;
			// _tipRoot = 2.047 m;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(12.5),SI.RADIAN);

			// if _spanStationKink=1.0 the wing has no crank (simply tapered wing)
			_spanStationKink = 1.0;  
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(Math.toRadians(0.0), SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3; // Elevator to tail chord ratio
			_etaIn = 0.1;
			_etaOut = 0.9;
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);

			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .12;               // value in %
			_tc_kink = .12;               // value in %
			_tc_tip = .12;              // value in %

			// Variable incidence horizontal tail ?
			_variableIncidence = false;

			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(192.6, SI.KILOGRAM);
			
			initializeAirfoils(aircraftName, this);
			break;
			
		case B747_100B:
			_type = ComponentEnum.HORIZONTAL_TAIL;
			_positionRelativeToAttachment = 0.0;
			_surface = Amount.valueOf(136.0, SI.SQUARE_METRE) ;
			_aspectRatio = 3.57;
			_taperRatioEquivalent = 0.265;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(32.0003),SI.RADIAN);

			// if _spanStationKink=1.0 the wing has no crank (simply tapered wing)
			_spanStationKink = 1.0;  
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(Math.toRadians(-1.0), SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(2.0002),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3; // Elevator to tail chord ratio
			_etaIn = 0.1;
			_etaOut = 0.9;
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);

			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .11;               // value in %
			_tc_kink = .11;               // value in %
			_tc_tip = .11;              // value in %

			// Variable incidence horizontal tail ?
			_variableIncidence = false;

			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(3628.0, SI.KILOGRAM);
			
			initializeAirfoils(aircraftName, this);
			break;
			
		case AGILE_DC1:
			_type = ComponentEnum.HORIZONTAL_TAIL;
			_positionRelativeToAttachment = 0.0;
			_surface = Amount.valueOf(19.10, SI.SQUARE_METRE) ;
			_aspectRatio = 4.97;
			_taperRatioEquivalent = 0.31;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(28.),SI.RADIAN);

			// if _spanStationKink=1.0 the wing has no crank (simply tapered wing)
			_spanStationKink = 1.0;  
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3; // Elevator to tail chord ratio
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);

			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .11;               // value in %
			_tc_kink = .11;               // value in %
			_tc_tip = .11;              // value in %

			// Variable incidence horizontal tail ?
			_variableIncidence = false;

			_surfaceCS = Amount.valueOf(_surface.times(0.14).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(497.6, SI.KILOGRAM);
			
			initializeAirfoils(aircraftName, this);
			break;
		}
	}

	public static String getId() {
		return "2";
	}

	@Override
	public String getObjectId() {
		return id;
	}

	
}

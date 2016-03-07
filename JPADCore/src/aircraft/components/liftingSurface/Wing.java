package aircraft.components.liftingSurface;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;

public class Wing extends LiftingSurface{

	public Wing(ComponentEnum type) {
		super(type);
	}

	public Wing(
			String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			ComponentEnum type, 
			Fuselage theFuselage,
			Nacelle theNacelle,
			LiftingSurface ... liftingSurface) {

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
	 * Overload of the default builder that recognize the aircraft name and sets it's value.
	 * 
	 * @author Vittorio Trifari 
	 */
	public Wing(
			AircraftEnum aircraftName,
			String name, 
			String description, 
			Double x, 
			Double y, 
			Double z, 
			ComponentEnum type, 
			Fuselage theFuselage,
			Nacelle theNacelle,
			LiftingSurface ... liftingSurface) {

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

	private void initializeDefaultSurface(){

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		_type = ComponentEnum.WING;
		
		//////////////////////////////
		// Input data
		//////////////////////////////
		_surface = Amount.valueOf(61, SI.SQUARE_METRE);
		_aspectRatio = 12.0;
		_taperRatioEquivalent = 0.636;
		_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(1.4),SI.RADIAN);
		_spanStationKink = 0.3478;
		_iw = Amount.valueOf(0., SI.RADIAN);
		_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_twistTip = Amount.valueOf(Math.toRadians(-2.0),SI.RADIAN);
		_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);

		// distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point
		_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);  
		_roughness = Amount.valueOf(0.152e-5, SI.METER);
		_xTransitionU = 0.15;
		_xTransitionL = 0.12;

		// Thickness of 3 section
		_tc_root = 0.18;               
		_tc_kink = 0.18;               
		_tc_tip = 0.135;        

		// Z position relative to the height of component to which this one is attached
		_positionRelativeToAttachment = 1.0; 

		// Extension of control surfaces in percent of total surface
		_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);

		// Additional chord extension at root (LE) with respect to equivalent wing
		_extensionLERootChordLinPanel = 0.;

		// Additional chord extension at root (TE) with respect to equivalent wing
		_extensionTERootChordLinPanel = 0.;

		// Percent of composite material used for wing structure
		_compositeCorretionFactor = 0.1;

		// A reference value chosen by the user
		_massReference = Amount.valueOf(2263.4, SI.KILOGRAM);

		// Calibration constant (to account for slight weight changes due to composites etc...)
		_massCorrectionFactor = 1.;

		initializeAirfoils(this);

	}
	
	private void initializeDefaultSurface(AircraftEnum aircraftName){

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		_type = ComponentEnum.WING;
		
		//////////////////////////////
		// Input data
		//////////////////////////////
		switch(aircraftName) {
		case ATR72:
			_surface = Amount.valueOf(61, SI.SQUARE_METRE);
			_aspectRatio = 12.0;
			_taperRatioEquivalent = 0.636;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(1.4),SI.RADIAN);
			_spanStationKink = 0.3478;
			_iw = Amount.valueOf(Math.toRadians(1.5), SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(-2.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);

			// distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);  
			_roughness = Amount.valueOf(0.152e-5, SI.METER);
			_xTransitionU = 0.15;
			_xTransitionL = 0.12;

			// Thickness of 3 section
			_tc_root = 0.18;               
			_tc_kink = 0.18;               
			_tc_tip = 0.135;        

			// Z position relative to the height of component to which this one is attached
			_positionRelativeToAttachment = 1.0; 

			// Extension of control surfaces in percent of total surface
			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);

			// Additional chord extension at root (LE) with respect to equivalent wing
			_extensionLERootChordLinPanel = 0.;

			// Additional chord extension at root (TE) with respect to equivalent wing
			_extensionTERootChordLinPanel = 0.;

			// Percent of composite material used for wing structure
			_compositeCorretionFactor = 0.1;

			// A reference value chosen by the user
			_massReference = Amount.valueOf(2080.6, SI.KILOGRAM);

			// Calibration constant (to account for slight weight changes due to composites etc...)
			_massCorrectionFactor = 1.;

			initializeAirfoils(aircraftName, this);
			break;
			
		case B747_100B:
			_surface = Amount.valueOf(511, SI.SQUARE_METRE);
			_aspectRatio = 6.96;
			_taperRatioEquivalent = 0.284;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(35.5),SI.RADIAN);
			_spanStationKink = 0.431;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(-0.5),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(-4.5),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(2.002),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(3.0),SI.RADIAN);

			// distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);  
			_roughness = Amount.valueOf(0.152e-5, SI.METER);
			_xTransitionU = 0.15;
			_xTransitionL = 0.12;

			// Thickness of 3 section
			_tc_root = 0.15;               
			_tc_kink = 0.11;               
			_tc_tip = 0.09;        

			// Z position relative to the height of component to which this one is attached
			_positionRelativeToAttachment = 0.0; 

			// Extension of control surfaces in percent of total surface
			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);

			// Additional chord extension at root (LE) with respect to equivalent wing
			_extensionLERootChordLinPanel = 0.;

			// Additional chord extension at root (TE) with respect to equivalent wing
			_extensionTERootChordLinPanel = 0.18;

			// Percent of composite material used for wing structure
			_compositeCorretionFactor = 0.0;

			// A reference value chosen by the user
			_massReference = Amount.valueOf(40159.0, SI.KILOGRAM);

			// Calibration constant (to account for slight weight changes due to composites etc...)
			_massCorrectionFactor = 1.;

			initializeAirfoils(aircraftName, this);
			break;
			
		case AGILE_DC1:
			_surface = Amount.valueOf(75, SI.SQUARE_METRE);
			_aspectRatio = 9.5;
			_taperRatioEquivalent = 0.25;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(25.),SI.RADIAN);
			_spanStationKink = 0.35;
			_iw = Amount.valueOf(Math.toRadians(2.0), SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(-1.5),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(-5),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(3.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(3.0),SI.RADIAN);

			// distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);  
			_roughness = Amount.valueOf(0.152e-5, SI.METER);
			_xTransitionU = 0.15;
			_xTransitionL = 0.12;

			// Thickness of 3 section
			_tc_root = 0.15; // from ADAS              
			_tc_kink = 0.09; // from ADAS              
			_tc_tip = 0.085; // from ADAS

			// Z position relative to the height of component to which this one is attached
			_positionRelativeToAttachment = 0.0; 

			// Extension of control surfaces in percent of total surface
			_surfaceCS = Amount.valueOf(_surface.times(0.31).getEstimatedValue(), SI.SQUARE_METRE);

			// Additional chord extension at root (LE) with respect to equivalent wing
			_extensionLERootChordLinPanel = 0.;

			// Additional chord extension at root (TE) with respect to equivalent wing
			_extensionTERootChordLinPanel = 0.;

			// Percent of composite material used for wing structure
			_compositeCorretionFactor = 0.1;

			// A reference value chosen by the user
			_massReference = Amount.valueOf(3433., SI.KILOGRAM);

			// Calibration constant (to account for slight weight changes due to composites etc...)
			_massCorrectionFactor = 1.;

			initializeAirfoils(aircraftName, this);
			break;
		}
	}
	
	public static String getId() {
		return "1";
	}

	@Override
	public String getObjectId() {
		return id;
	}
	
}

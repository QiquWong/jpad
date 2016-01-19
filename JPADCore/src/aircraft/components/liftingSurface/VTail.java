package aircraft.components.liftingSurface;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.ComponentEnum;

public class VTail extends LiftingSurface{

	public VTail(ComponentEnum type) {
		super(type);
		// TODO Auto-generated constructor stub
	}

	public VTail(String name, 
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
	 * Overload of the default builder that recognize aircraft name and generates the lifting surface with it's data.
	 * 
	 * @author Vittorio Trifari
	 */
	public VTail(String aircraftName,
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
	
	private void initializeDefaultSurface() {
		
		_type = ComponentEnum.VERTICAL_TAIL;
		_positionRelativeToAttachment = 1.0;
		_surface = Amount.valueOf(12.48, SI.SQUARE_METRE) ;
		_aspectRatio = 1.66;
		_taperRatioEquivalent = 0.32;
		// ATR72
		// _tipChord = 1.796 m;
		// _tipRoot = 5.55 m;
		_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(28.6),SI.RADIAN);
		_spanStationKink = 0.3;
		_extensionLERootChordLinPanel = 0.;
		_extensionTERootChordLinPanel = 0.;
		_iw = Amount.valueOf(0., SI.RADIAN);
		_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_CeCt = 0.3;
		_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
		//			_deltaXWingFus = Amount.valueOf(21.9,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
		_xTransitionU = 0.12;
		_xTransitionL = 0.12;
		_roughness = Amount.valueOf(0.052e-5, SI.METER);
		//  INPUT DATA
		// Thickness of 3 section
		_tc_root = .12;               // value in %
		_tc_kink = .12;               // value in %
		_tc_tip = .12;              // value in %

		_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
		_massReference = Amount.valueOf(271.7, SI.KILOGRAM);

		setMirrored(false);
		
		initializeAirfoils(this);
	}
	
	/**
	 * Overload of the previous method that recognize aircraft name and initialize the V-tail with it's value. 
	 * 
	 * @author Vittorio Trifari
	 */
	private void initializeDefaultSurface(String aircraftName) {
		
		switch(aircraftName) {
		case "ATR-72":
			_type = ComponentEnum.VERTICAL_TAIL;
			_positionRelativeToAttachment = 1.0;
			_surface = Amount.valueOf(12.48, SI.SQUARE_METRE) ;
			_aspectRatio = 1.66;
			_taperRatioEquivalent = 0.32;
			// ATR72
			// _tipChord = 1.796 m;
			// _tipRoot = 5.55 m;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(28.6),SI.RADIAN);
			_spanStationKink = 0.3;
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3;
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(21.9,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);
			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .12;               // value in %
			_tc_kink = .12;               // value in %
			_tc_tip = .12;              // value in %

			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(271.7, SI.KILOGRAM);

			setMirrored(false);
			
			initializeAirfoils(aircraftName, this);
			break;
			
		case "B747-100B":
			_type = ComponentEnum.VERTICAL_TAIL;
			_positionRelativeToAttachment = 0.0;
			_surface = Amount.valueOf(77.1, SI.SQUARE_METRE) ;
			_aspectRatio = 1.34;
			_taperRatioEquivalent = 0.33;
			_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(45.0001),SI.RADIAN);
			_spanStationKink = 1.0;
			_extensionLERootChordLinPanel = 0.;
			_extensionTERootChordLinPanel = 0.;
			_iw = Amount.valueOf(0., SI.RADIAN);
			_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_twistTip = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
			_CeCt = 0.3;
			_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
			//			_deltaXWingFus = Amount.valueOf(21.9,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
			_xTransitionU = 0.12;
			_xTransitionL = 0.12;
			_roughness = Amount.valueOf(0.052e-5, SI.METER);
			//  INPUT DATA
			// Thickness of 3 section
			_tc_root = .12;               // value in %
			_tc_kink = .12;               // value in %
			_tc_tip = .12;              // value in %

			_surfaceCS = Amount.valueOf(_surface.times(0.25).getEstimatedValue(), SI.SQUARE_METRE);
			_massReference = Amount.valueOf(1785.8, SI.KILOGRAM);

			setMirrored(false);
			
			initializeAirfoils(aircraftName, this);
			break;
		}
	}
	
	public static String getId() {
		return "3";
	}
	
	@Override
	public String getObjectId() {
		return id;
	}
	
}

package aircraft.components.liftingSurface;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.ComponentEnum;

public class Canard extends LiftingSurface2Panels{

	public Canard(ComponentEnum type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	public Canard(String name, 
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
	
	private void initializeDefaultSurface() {
		
		_type = ComponentEnum.CANARD;
		
		_positionRelativeToAttachment = 0.5;
		_surface = Amount.valueOf(61, SI.SQUARE_METRE) ;
		_aspectRatio = 12.0;
		_taperRatioEquivalent = 0.636;
		_sweepQuarterChordEq = Amount.valueOf(Math.toRadians(1.4),SI.RADIAN);
		_spanStationKink = 1.0;
		_extensionLERootChordLinPanel = 0.;
		_extensionTERootChordLinPanel = 0.;
		_iw = Amount.valueOf(0., SI.RADIAN);
		_twistKink = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_twistTip = Amount.valueOf(Math.toRadians(-2.0),SI.RADIAN);
		_dihedralInnerPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_dihedralOuterPanel = Amount.valueOf(Math.toRadians(0.0),SI.RADIAN);
		_deltaXWingFus = Amount.valueOf(_X0.getEstimatedValue(),SI.METER);
		//			_deltaXWingFus = Amount.valueOf(24.6,SI.METER); // distance of wing apex (LE of xz plane chord) from fuselage nose = (0,0,0) point 
		_xTransitionU = 0.1;
		_xTransitionL = 0.1;
		_roughness = Amount.valueOf(0.052e-5, SI.METER);
		//  INPUT DATA
		// Thickness of 3 section
		_tc_root = .12;               // value in %
		_tc_kink = .12;               // value in %
		_tc_tip = .12;              // value in %

		_massReference = Amount.valueOf(100., SI.KILOGRAM);
		
		initializeAirfoils(this);
	}

	public static String getId() {
		return "4";
	}
	
	@Override
	public String getObjectId() {
		return id;
	}
}

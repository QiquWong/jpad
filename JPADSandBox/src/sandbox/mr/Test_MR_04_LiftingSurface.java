package sandbox.mr;

import javax.measure.quantity.Area;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LiftingSurface2Panels;
import configuration.enumerations.ComponentEnum;
import standaloneutils.customdata.CenterOfGravity;

public class Test_MR_04_LiftingSurface {

	public static void main(String[] args) {
		// see: 
		//  aircraft.calculators.
		//    ACAnalysisManager.updateGeometry(Aircraft aircraft)
		//

		Fuselage theFuselage = new Fuselage(
				"Fuselage", // name
				"Data from AC_ATR_72_REV05.pdf", // description
				0.0, 0.0, 0.0 // Fuselage apex (x,y,z)-coordinates in construction axes
				);

		double xAw = 11.0; //meter 
		double yAw = 0.0;
		double zAw = 1.6;
		double iw = 0.0;

		LiftingSurface2Panels theWing = new LiftingSurface2Panels(
				"Wing", // name
				"Data from AC_ATR_72_REV05.pdf", 
				xAw, yAw, zAw, iw, 
				ComponentEnum.WING,
				theFuselage // let her see the fuselage
				); 

		theWing.calculateGeometry();
		theWing.getGeometry().calculateAll();
		//Amount<Area> sWetted = theWing.get_surfaceWettedExposed();
		
		double xCgLocal= 1.5; // meter 
		double yCgLocal= 0;
		double zCgLocal= 0;
		
		CenterOfGravity cg = new CenterOfGravity(
				Amount.valueOf(xCgLocal, SI.METER), // coordinates in LRF
				Amount.valueOf(yCgLocal, SI.METER),
				Amount.valueOf(zCgLocal, SI.METER),
				Amount.valueOf(xAw, SI.METER), // origin of LRF in BRF 
				Amount.valueOf(yAw, SI.METER),
				Amount.valueOf(zAw, SI.METER),
				Amount.valueOf(0.0, SI.METER),// origin of BRF
				Amount.valueOf(0.0, SI.METER),
				Amount.valueOf(0.0, SI.METER)
				);
	
		
		cg.calculateCGinBRF();
		theWing.set_cg(cg);
		


		// Default operating conditions
		OperatingConditions theOperatingConditions = new OperatingConditions();

		theOperatingConditions.set_machCurrent(0.53);
		theOperatingConditions.set_altitude(Amount.valueOf(7000, NonSI.FOOT));
		theOperatingConditions.set_alphaCurrent(Amount.valueOf(iw, NonSI.DEGREE_ANGLE));

		System.out.println("Operating condition");
		System.out.println("\tMach: " + theOperatingConditions.get_machCurrent());
		System.out.println("----------------------");

		
		// allocate manager
		
		LSAerodynamicsManager manager = new LSAerodynamicsManager ( 
				theOperatingConditions,
				theWing
				); 
		manager.calculateAllIsolatedWing(
				theOperatingConditions.get_machCurrent(),
				theOperatingConditions.get_alphaCurrent());
		
		
		
	} // end main


}

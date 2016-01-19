package sandbox.adm;

import javax.measure.unit.SI;

import java.io.File;

import javax.measure.quantity.Area;

import org.jcae.opencascade.jni.BRepOffsetAPI_ThruSections;
import org.jcae.opencascade.jni.BRep_Builder;
import org.jcae.opencascade.jni.GProp_GProps;
import org.jcae.opencascade.jni.TopoDS_Compound;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import cad.aircraft.MyAircraftBuilder;
import cad.aircraft.MyFuselageBuilder;
import cad.aircraft.MyLiftingSurfaceBuilder;
import configuration.MyConfiguration;

public class MyTest_ADM_03dCAD {

	Aircraft _theAircraft = null;
	
	// Measurements
	private Amount<Area> _wettedArea = Amount.valueOf(0.0, SI.SQUARE_METRE); 
	
	public MyTest_ADM_03dCAD(Aircraft aircraft) {

		_theAircraft = aircraft;
		
		// ATTENTION: Need this call to update derived geometric parameters
		MyTest_ADM_03dCAD.updateGeometry(_theAircraft);
		
		MyAircraftBuilder aircraftBuilder = new MyAircraftBuilder();
		aircraftBuilder.buildCAD(_theAircraft);


		/* 
		// specific examples

		MyFuselageBuilder fuselageBuilder = new MyFuselageBuilder(_theAircraft);
		System.out.println("\nMyTest_ADM_03dCAD :: exporting FUSELAGE\n");
		fuselageBuilder.buildAndWriteCAD(
				false, // solid
				true, // ruled
				true, // closedSplines
				MyConfiguration.currentDirectory.getAbsolutePath() + File.separator + "cad",
				"Fuselage"
				);

		MyLiftingSurfaceBuilder wingBuilder 
			= new MyLiftingSurfaceBuilder(_theAircraft.get_wing());

		wingBuilder.buildAndWriteCAD(
				false, // solid
				true, // ruled
				true, // closedSplines
				MyConfiguration.currentDirectory.getAbsolutePath() + File.separator + "cad",
				"Wing"
				);
		 */
	}
	
	public static void updateGeometry(Aircraft aircraft) {

		if (aircraft == null) return;

		// Fuselage
		if(aircraft.get_fuselage() != null){
			System.out.println("Updating fuselage geometry ...");
			aircraft.get_fuselage().calculateGeometry();
			aircraft.get_fuselage().checkGeometry();
			aircraft.set_sWetTotal(aircraft.get_fuselage().get_sWet().getEstimatedValue());
		}

		// Wing
		if(aircraft.get_wing() != null){
			System.out.println("Updating wing geometry ...");
			aircraft.get_wing().calculateGeometry();
			//			aircraft.get_wing().updateAirfoilsGeometry();
			aircraft.get_wing().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_wing().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Htail
		if(aircraft.get_HTail() != null){
			System.out.println("Updating HTail geometry ...");
			aircraft.get_HTail().calculateGeometry();
			//			aircraft.get_HTail().updateAirfoilsGeometry();
			aircraft.get_HTail().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_HTail().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Vtail
		if(aircraft.get_VTail() != null){
			System.out.println("Updating VTail geometry ...");
			aircraft.get_VTail().calculateGeometry();
			//			aircraft.get_VTail().updateAirfoilsGeometry();
			aircraft.get_VTail().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_VTail().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Canard
		if(aircraft.get_Canard() != null){
			System.out.println("Updating Canard geometry ...");
			aircraft.get_Canard().calculateGeometry();
			//			aircraft.get_Canard().updateAirfoilsGeometry();
			aircraft.get_Canard().getGeometry().calculateAll();
			aircraft.set_sWetTotal(aircraft.get_Canard().get_surfaceWettedExposed().getEstimatedValue());
		}

		// Nacelle
		if(aircraft.get_theNacelles() != null){
			aircraft.get_theNacelles().calculateSurfaceWetted();
			aircraft.set_sWetTotal(aircraft.get_theNacelles().get_surfaceWetted().getEstimatedValue());
		}

		// Fuel tank
		if(aircraft.get_theFuelTank() != null){
			aircraft.get_theFuelTank().calculateGeometry(aircraft);
		}

		// Evaluate thrust output
		if(aircraft.get_powerPlant() != null){
			aircraft.get_powerPlant().calculateDerivedVariables();
		}
	}
	
}// end of class

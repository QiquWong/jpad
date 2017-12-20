package it.unina.daf.jpadcadsandbox;

import java.util.List;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.TopoDS_Compound;

public class Test16 {

	public static Aircraft theAircraft;
	
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Test16.theAircraft = AircraftUtils.importAircraft(args);

		Fuselage fuselage = theAircraft.getFuselage();
		
//		System.out.println("========== [main] The aircraft:");
//		System.out.println(theAircraft);

//		System.out.println("========== [main] The fuselage geometric parameters:");
//		System.out.println(fuselage.getFuselageCreator());
//
//		System.out.println("========== [main] The wing geometric parameters:");
//		System.out.println(theAircraft.getWing().getLiftingSurfaceCreator());
		
		boolean exportLofts = true;
		boolean exportSupportShapes = false;
		
//		OCCShell.setDefaultMakeSolid(true);
//		System.out.println(">>>>>> OCCShell default-make-solid: " + OCCShell.isDefaultMakeSolid());

		fuselage.getFuselageCreator().calculateGeometry(40, 3, 40, 20, 20);

		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(
				fuselage, 
				0.15, 1.0, 3, 13, 7, 1.0, 0.10, 3, 
				exportLofts, exportSupportShapes);

		// Write to a file
		String fileName = "test16.brep";
		
		if (OCCUtils.write(fileName, fuselageShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		

		// === Experimental, extract solids from fuselage shapes
		String fileNameSolids = fileName.replace(".brep", "_solids.brep");
		System.out.println("========== [main] Exporting solids in : " + fileNameSolids);
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		fuselageShapes.stream()
			.filter(s -> s instanceof CADSolid)
			.forEach(s -> {
				System.out.println(">>>>>> Solid");
				builder.Add(compound, s.getShape());
			});
		long resultSolids = BRepTools.Write(compound, fileNameSolids);
		if (resultSolids == 1)
			System.out.println("========== [OCCUtils::write] Solids written on file: " + fileNameSolids);
		
	}

}

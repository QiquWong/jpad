package it.unina.daf.jpadcadsandbox;

import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;

public class Test17 {

	public static void main(String[] args) {
		
		OCCUtils.initCADShapeFactory(); // theFactory now non-null
		
		CADFace f0 = OCCUtils.theFactory.newFacePlanar(
				new double[]{0.0, 0.0, 0.0}, 
				new double[]{1.0, 0.0, 0.0}, 
				new double[]{0.0, 1.0, 0.0});

		CADFace f1 = OCCUtils.theFactory.newFacePlanar(
				new double[]{0.0, 0.0, 0.0}, 
				new double[]{1.0, 0.0, 0.0}, 
				new double[]{0.0, 0.0, 0.5});

		CADFace f2 = OCCUtils.theFactory.newFacePlanar(
				new double[]{1.0, 0.0, 0.0}, 
				new double[]{0.0, 1.0, 0.0}, 
				new double[]{0.0, 0.0, 0.5});
		
		CADFace f3 = OCCUtils.theFactory.newFacePlanar(
				new double[]{0.0, 0.0, 0.0}, 
				new double[]{0.0, 1.0, 0.0}, 
				new double[]{0.0, 0.0, 0.5});
		
		CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(f0, f1, f2, f3);
		if (shell != null) {
			System.out.println("========== [main] new shell ok");
			System.out.println(OCCUtils.reportOnShape(((OCCShell)shell).getShape(), "Sewed faces"));
		}
		
		CADSolid solid = OCCUtils.theFactory.newSolidFromAdjacentFaces(f0, f1, f2, f3);
		if (solid != null) {
			System.out.println("========== [main] new solid ok");
			System.out.println(OCCUtils.reportOnShape(((OCCSolid)solid).getShape(), "Sewed faces ==> Solid entity"));
		}
		
		// Write to a file
		String fileName = "test17.brep";

		if (OCCUtils.write(fileName, 
				/* f0, f1, f2, f3  */ 
				/* shell */ 
				solid)
			)
			System.out.println("========== [main] Output written on file: " + fileName);
		
		
		
		

//		CADEdge e1 = (CADEdge) OCCUtils.theFactory.newShape(em1.Edge());
//		CADGeomCurve3D c1 = OCCUtils.theFactory.newCurve3D(e1);
		
//		GeomPlate_BuildPlateSurface geomPlateBuilder = 
//				// new GeomPlate_BuildPlateSurface(3, 2, 10000, 1.e-4, 1.e-5, 1.e-2, 1.e-1, 0); 
//				new GeomPlate_BuildPlateSurface();
//		geomPlateBuilder.Add(em1.Edge().);

	
		
		
		
	}

}

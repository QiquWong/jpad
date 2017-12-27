package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.Adaptor3d_HCurve;
import opencascade.BRepAdaptor_Curve;
import opencascade.BRepAdaptor_HCurve;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRep_Tool;
import opencascade.GeomPlate_BuildPlateSurface;
import opencascade.GeomPlate_CurveConstraint;
import opencascade.Geom_Surface;
import opencascade.TopoDS;
import processing.core.PVector;

public class Test19 {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		OCCUtils.initCADShapeFactory();
		
		System.out.println("========== [main] Constructing the sections of Patch-1");
		// section 1
		List<double[]> ptsSec1 = new ArrayList<double[]>();
		ptsSec1.add(new double[]{ 0.50, 0.00, 1.50});
		ptsSec1.add(new double[]{ 0.50, 1.00, 1.30});
		ptsSec1.add(new double[]{ 0.50, 1.50, 0.00});
		CADGeomCurve3D cadSec1 = OCCUtils.theFactory.newCurve3D(ptsSec1, false);
		
		// section 2
		List<double[]> ptsSec2 = new ArrayList<double[]>();
		ptsSec2.add(new double[]{ 2.00, 0.00, 2.50});
		ptsSec2.add(new double[]{ 2.00, 2.00, 1.45});
		ptsSec2.add(new double[]{ 2.00, 2.50, 0.00});
		CADGeomCurve3D cadSec2 = OCCUtils.theFactory.newCurve3D(ptsSec2, false);

		// section 3
		List<double[]> ptsSec3 = new ArrayList<double[]>();
		ptsSec3.add(new double[]{ 4.00, 0.00, 4.00});
		ptsSec3.add(new double[]{ 4.00, 3.50, 3.00});
		ptsSec3.add(new double[]{ 4.00, 4.00, 0.00});
		CADGeomCurve3D cadSec3 = OCCUtils.theFactory.newCurve3D(ptsSec3, false);

		System.out.println("========== [main] Constructing the patch, Patch-1");
		List<CADGeomCurve3D> cadSections = new ArrayList<>();
		cadSections.add(cadSec1);
		cadSections.add(cadSec2);
		cadSections.add(cadSec3);
		OCCShape patch1 = OCCUtils.makePatchThruSections( // -> OCCShell
				new OCCVertex(0.0, 0.0, 0.0),
				cadSections
				);		
		
		// guide-curve-1
		System.out.println("========== [main] Constructing the guide curve to deform Patch-1");
		// guide curve in plane XZ
		List<double[]> ptsGC1 = new ArrayList<double[]>();
		ptsGC1.add(new double[]{ 0.00, 0.00, 0.00});
		ptsGC1.add(ptsSec1.get(0));
		ptsGC1.add(ptsSec2.get(0));
		ptsGC1.add(new double[]{ 
				0.1*ptsSec2.get(0)[0] + 0.9*ptsSec3.get(0)[0], // pick an X-station between sec-2 & sec-3
				0.00, 
				0.98*ptsSec3.get(0)[2]}); // tweak the Z
		ptsGC1.add(ptsSec3.get(0));		
		CADGeomCurve3D cadGC1 = OCCUtils.theFactory.newCurve3D(ptsGC1, false);
		
		// plating: constrain patch-1 to guide-curve-1

		System.out.println("========== [main] Preparing the plate surface to deform Patch-1");

		// plate maker
		GeomPlate_BuildPlateSurface plateMaker = new GeomPlate_BuildPlateSurface(
				BRep_Tool.Surface(
						TopoDS.ToFace(patch1.getShape())
				));
		plateMaker.Init(); // Resets all constraints
		Geom_Surface geomSurface0 = BRep_Tool.Surface(TopoDS.ToFace(patch1.getShape()));
		plateMaker.LoadInitSurface(geomSurface0);
		
		// curve constraint
		BRepAdaptor_Curve adCrv1 = new BRepAdaptor_Curve(
				( (OCCEdge)((OCCGeomCurve3D)cadGC1).edge() ).getShape()
				);
		BRepAdaptor_HCurve adHCrv1 = new BRepAdaptor_HCurve(adCrv1);
		GeomPlate_CurveConstraint crvConstraint1 = new GeomPlate_CurveConstraint(
				adHCrv1, 0, 20, 0.01
				);
		// add the constraint curve to the plate maker
		plateMaker.Add(crvConstraint1);

//		plateMaker.Perform(); // FIXME: crashes
		
		// Export shapes
		List<OCCShape> shapes = new ArrayList<>();
		cadSections.stream()
			.forEach(s -> shapes.add((OCCEdge)((OCCGeomCurve3D)s).edge()));
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC1).edge());
		shapes.add(patch1);
		
		// Write to a file
		String fileName = "test19.brep";
		
		if (OCCUtils.write(fileName, shapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}

}

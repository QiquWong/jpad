package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;

public class Test19 {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		OCCUtils.initCADShapeFactory();
		
		// section 1
		List<double[]> ptsSec1 = new ArrayList<double[]>();
		ptsSec1.add(new double[]{ 0.50, 0.00, 1.50});
		ptsSec1.add(new double[]{ 0.50, 0.50, 0.30});
		ptsSec1.add(new double[]{ 0.50, 0.50, 0.00});
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

		// guide curve 1
		List<double[]> ptsGC1 = new ArrayList<double[]>();
		ptsGC1.add(new double[]{ 0.00, 0.00, 0.00});
		ptsGC1.add(ptsSec1.get(0));
		ptsGC1.add(ptsSec2.get(0));
		ptsGC1.add(new double[]{ 
				0.1*ptsSec2.get(0)[0] + 0.9*ptsSec3.get(0)[0], 
				0.00, 
				1.02*ptsSec3.get(0)[2]});
		ptsGC1.add(ptsSec3.get(0));		
		CADGeomCurve3D cadGC1 = OCCUtils.theFactory.newCurve3D(ptsGC1, false);

		
		// Export shapes
		List<OCCShape> shapes = new ArrayList<>();
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadSec1).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadSec2).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadSec3).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC1).edge());
		
		// Write to a file
		String fileName = "test19.brep";
		
		if (OCCUtils.write(fileName, shapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
		
	}

}

package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;

public class Test6 {

	public static void main(String[] args) {
		System.out.println("Testing Java Wrapper of OCCT v7.0.0");
		System.out.println("Classes in package it.unina.daf.jpadcad.occ");
		
		// create the cad factory
		CADShapeFactory.setFactory(new OCCShapeFactory());
		CADShapeFactory factory = CADShapeFactory.getFactory();

		// list of points belonging to the desired curve
		List<double[]> points = new ArrayList<double[]>();
		points.add(new double[]{ 0,  0,  0});
		points.add(new double[]{ 0, 10,  5});
		points.add(new double[]{ 0, 15, -5});
		points.add(new double[]{ 0, 20,  3});

		boolean isPeriodic = false;
		CADGeomCurve3D cadGeomCurve3D = factory.newCurve3D(points, isPeriodic);
		System.out.println("Cad geom curve non-null?: " + (cadGeomCurve3D != null));
		System.out.println("Cad geom curve range: " + Arrays.toString(cadGeomCurve3D.getRange()));
		
		cadGeomCurve3D.discretize(20);
		System.out.println("Cad edge discretization arcs: " + cadGeomCurve3D.nbPoints());

		double val = 10.0;
		System.out.println(
				"Cad edge discretization value at v=" 
						+ val + " : " + Arrays.toString(cadGeomCurve3D.value(val))
						);
		
		// Write to a file
		String fileName = "test06.brep";
		CADEdge cadEdge = cadGeomCurve3D.edge();
		cadEdge.writeNative(fileName);
		System.out.println("Output written on file: " + fileName);

	}

}

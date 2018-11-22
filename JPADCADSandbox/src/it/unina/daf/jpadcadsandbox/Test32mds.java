package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;

public class Test32mds {

	public static void main(String[] args) {
		System.out.println("---------------------------------------------");
		System.out.println("---------------- JPADCAD Test ---------------");
		System.out.println("---------------------------------------------");
		
		if (OCCUtils.theFactory == null) {
			OCCUtils.initCADShapeFactory();
		}
		
		List<double[]> pts1 = new ArrayList<>();
		pts1.add(new double[] {0.0, 0.0, 0.0});
		pts1.add(new double[] {0.0, 1.0, 0.0});
		pts1.add(new double[] {1.0, 1.0, 0.0});
		pts1.add(new double[] {2.0, 0.5, 0.0});
		pts1.add(new double[] {2.0, 0.0, 0.0});
		pts1.add(new double[] {0.0, 0.0, 0.0});
		
		List<double[]> pts2 = new ArrayList<>();
		pts2.add(new double[] {0.0, 0.0, 4.0});
		pts2.add(new double[] {0.0, 1.0, 4.0});
		pts2.add(new double[] {1.0, 1.0, 4.0});
		pts2.add(new double[] {2.0, 0.5, 4.0});
		pts2.add(new double[] {2.0, 0.0, 4.0});
		pts2.add(new double[] {0.0, 0.0, 4.0});
		
		List<CADEdge> edgs1 = new ArrayList<>();
		List<CADEdge> edgs2 = new ArrayList<>();
		for (int i = 1; i < pts1.size(); i++) {
			edgs1.add(OCCUtils.theFactory.newCurve3D(pts1.get(i), pts1.get(i-1)).edge());
			edgs2.add(OCCUtils.theFactory.newCurve3D(pts2.get(i), pts2.get(i-1)).edge());
		}
		
		CADWire wire1 = OCCUtils.theFactory.newWireFromAdjacentEdges(edgs1);
		CADWire wire2 = OCCUtils.theFactory.newWireFromAdjacentEdges(edgs2);
		
		OCCShape shell = OCCUtils.makePatchThruSections(wire1, wire2);
		
		String filename = "Test32mds.brep";
		
		if (OCCUtils.write(filename, shell, (OCCShape) wire1, (OCCShape) wire2))
			System.out.println("[Test32mds] CAD shapes correctly written to file (" + filename + ")");
	}

}

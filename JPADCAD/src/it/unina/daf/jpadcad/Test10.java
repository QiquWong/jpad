package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import it.unina.daf.jpadcad.occ.OCCShell;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.TopoDS_Compound;

public class Test10 {

	public static void main(String[] args) {
		System.out.println("Testing Java Wrapper of OCCT v7.0.0");
		System.out.println("Classes in package it.unina.daf.jpadcad.occ");

		// create the cad factory
		CADShapeFactory.setFactory(new OCCShapeFactory());
		CADShapeFactory factory = CADShapeFactory.getFactory();
		
		System.out.println("========== Constructing a fuselage nose ==========");
		
		System.out.println("Section 1 ...");
		
		// section 1, main dimensions
		Amount<Length> x1  = Amount.valueOf( 1.0, SI.METER);
		Amount<Length> y1  = Amount.valueOf( 1.0, SI.METER);
		Amount<Length> z1u = Amount.valueOf( 1.5, SI.METER);
		Amount<Length> z1l = Amount.valueOf(-0.8, SI.METER);
		
		// list of points belonging to the desired curve-1
		List<double[]> points1 = new ArrayList<double[]>();
		points1.add(new double[]{ x1.doubleValue(SI.METER),                        0, z1u.doubleValue(SI.METER)});
		points1.add(new double[]{ x1.doubleValue(SI.METER), y1.doubleValue(SI.METER),                         0});
		points1.add(new double[]{ x1.doubleValue(SI.METER),                        0, z1l.doubleValue(SI.METER)});
		
		// curve-1
		boolean isPeriodic = false;
		CADGeomCurve3D cadGeomCurve3D1 = factory.newCurve3D(points1, isPeriodic);
		
		System.out.println("Orientation forward: " + 
				(
					(OCCEdge)((OCCGeomCurve3D)cadGeomCurve3D1).edge()
				).isOrientationForward()
			);

		System.out.println("Section 2 ...");
		
		// section 2, main dimensions
		Amount<Length> x2  = Amount.valueOf( 6.0, SI.METER);
		Amount<Length> y2  = Amount.valueOf( 3.5, SI.METER);
		Amount<Length> z2u = Amount.valueOf( 4.0, SI.METER);
		Amount<Length> z2l = Amount.valueOf(-2.0, SI.METER);
		
		// list of points belonging to the desired curve-1
		List<double[]> points2 = new ArrayList<double[]>();
		points2.add(new double[]{ x2.doubleValue(SI.METER),                        0, z2l.doubleValue(SI.METER)});
		points2.add(new double[]{ x2.doubleValue(SI.METER), y2.doubleValue(SI.METER),                         0});
		points2.add(new double[]{ x2.doubleValue(SI.METER),                        0, z2u.doubleValue(SI.METER)});
		
		// curve-2
		isPeriodic = false;
		CADGeomCurve3D cadGeomCurve3D2 = factory.newCurve3D(points2, isPeriodic);
		
		System.out.println("Curve-2, Orientation forward: " + 
				(
					(OCCEdge)((OCCGeomCurve3D)cadGeomCurve3D2).edge()
				).isOrientationForward()
			);
		
		OCCShape e2 = (
				 (OCCEdge)((OCCGeomCurve3D)cadGeomCurve3D2).edge()
			).reversed();
		System.out.println("Reversed-Curve-2, Orientation forward: " + e2.isOrientationForward());
		
//		CADGeomCurve3D cadGeomCurve3D2r = factory.newCurve3D((CADEdge)e2);
		
				
		//---------------------------------------------------------------------------
		// Make a loft surface without low-level OCCT data structures
		
		System.out.println("Surfacing ...");
		
		List<CADGeomCurve3D> cadGeomCurveList = new ArrayList<CADGeomCurve3D>();
		cadGeomCurveList.add(cadGeomCurve3D1);
		cadGeomCurveList.add(cadGeomCurve3D2);
		
		// The CADShell object
		CADShell cadShell = factory.newShell(cadGeomCurveList);

		System.out.println("Is cadShell null?: " + (cadShell == null));
		
		System.out.println("Shape class: " + cadShell.getClass());
		System.out.println("Is cadShape class CADShell?: " + (cadShell instanceof CADShell));
		System.out.println("Is cadShape class OCCShell?: " + (cadShell instanceof OCCShell));

		//---------------------------------------------------------------------------
		// Put everything in a compound
		BRep_Builder _builder = new BRep_Builder();
		TopoDS_Compound _compound = new TopoDS_Compound();
		_builder.MakeCompound(_compound);
		
		cadGeomCurveList.stream()
						.map(c3d -> (OCCEdge)((OCCGeomCurve3D)c3d).edge())
						.forEach(e -> _builder.Add(_compound,e.getShape()));

		_builder.Add(_compound, ((OCCShape)cadShell).getShape());
		
		// Write to a file
		String fileName = "test10.brep";

		BRepTools.Write(_compound, fileName);
		
		System.out.println("Output written on file: " + fileName);
		
	}

}

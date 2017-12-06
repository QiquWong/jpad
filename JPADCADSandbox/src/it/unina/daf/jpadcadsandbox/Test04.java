package it.unina.daf.jpadcadsandbox;

import java.util.Arrays;

import it.unina.daf.jpadcad.occ.CADExplorer;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepTools;
import opencascade.GeomAPI_Interpolate;
import opencascade.Geom_BSplineCurve;
import opencascade.Precision;
import opencascade.TColgp_HArray1OfPnt;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;

public class Test04 {

	public static void main(String[] args) {
		System.out.println("Testing Java Wrapper of OCCT v7.0.0");
		System.out.println("Classes in package it.unina.daf.jpadcad.occ");
		
		CADShapeFactory.setFactory(new OCCShapeFactory());
		CADShapeFactory factory = CADShapeFactory.getFactory();

		//-----------------------------------------------------------
		// Create a low-level object using OCCT JavaWrapper classes
		// curve #1
		TColgp_HArray1OfPnt points1 = new TColgp_HArray1OfPnt(1, 3);
		points1.SetValue(1, new gp_Pnt( 0,  0, 0));
		points1.SetValue(2, new gp_Pnt( 0, 10, 5));
		points1.SetValue(3, new gp_Pnt( 0, 20, 0));
		long isPeriodic = 0;
		GeomAPI_Interpolate aNoPeriodInterpolate1 = 
				new GeomAPI_Interpolate(points1, isPeriodic, Precision.Confusion());
		aNoPeriodInterpolate1.Perform();
		Geom_BSplineCurve anInterpolationCurve1 = aNoPeriodInterpolate1.Curve();
		TopoDS_Edge spline1 = new BRepBuilderAPI_MakeEdge(anInterpolationCurve1).Edge();

		//-----------------------------------------------------------
		// Wrap the low-level object into a CADShape
		CADShape cadEdge = factory.newShape(spline1);
		
		//-----------------------------------------------------------
		// Explore the shape in search of vertices 
		
		// count
		CADExplorer expV = CADShapeFactory.getFactory().newExplorer();
		int nodes = 0;
		for (expV.init(cadEdge, CADShapeTypes.VERTEX); expV.more(); expV.next())
			nodes++;	

		System.out.println("Vertices: " + nodes);
		
		// fill the array
		CADVertex[] vnodelist = new CADVertex[nodes];
		int kNode = 0;
		for (expV.init(cadEdge, CADShapeTypes.VERTEX); expV.more(); expV.next()) {
			vnodelist[kNode] = (CADVertex) expV.current();
			System.out.println(
				"Vertex, V(" + kNode + "): " 
				+ Arrays.toString((vnodelist[kNode]).pnt())
			);
			kNode++;
		}
		
		// make a wire and measure...
		BRepBuilderAPI_MakeWire makeWire1 = new BRepBuilderAPI_MakeWire();
		makeWire1.Add(spline1);
		TopoDS_Wire wire1 = makeWire1.Wire();
		CADShape cadWire = factory.newShape(wire1);
		double length = ((CADWire) cadWire).length();
		System.out.println("Curve (wire) length: " + length);
		
		// Write to a file
		String fileName = "test04.brep";
		cadEdge.writeNative(fileName);		
		
		System.out.println("Output written on file: " + fileName);
		

	}

}

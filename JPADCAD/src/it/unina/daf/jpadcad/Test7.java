package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADGeomSurface;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.GProp_GProps;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;

public class Test7 {

	public static void main(String[] args) {
		System.out.println("Testing Java Wrapper of OCCT v7.0.0");
		System.out.println("Classes in package it.unina.daf.jpadcad.occ");

		// create the cad factory
		CADShapeFactory.setFactory(new OCCShapeFactory());
		CADShapeFactory factory = CADShapeFactory.getFactory();

		// list of points belonging to the desired curve-1
		List<double[]> points1 = new ArrayList<double[]>();
		points1.add(new double[]{ 0,  0,  0});
		points1.add(new double[]{ 0, 10,  5});
		points1.add(new double[]{ 0, 15, -5});
		points1.add(new double[]{ 0, 20,  3});
		
		// curve-1
		boolean isPeriodic = false;
		CADGeomCurve3D cadGeomCurve3D1 = factory.newCurve3D(points1, isPeriodic);
		
		// list of points belonging to the desired curve-2
		List<double[]> points2 = new ArrayList<double[]>();
		points2.add(new double[]{10,  0,  0});
		points2.add(new double[]{10, 10,  5});
		points2.add(new double[]{10, 15,  0});
		points2.add(new double[]{10, 20,  0});
		
		// curve-2
		isPeriodic = false;
		CADGeomCurve3D cadGeomCurve3D2 = factory.newCurve3D(points2, isPeriodic);

		// list of points belonging to the desired curve-3
		List<double[]> points3 = new ArrayList<double[]>();
		points3.add(new double[]{20,  0,  0});
		points3.add(new double[]{20, 10,  5});
		points3.add(new double[]{20, 15,  0});
		points3.add(new double[]{20, 20,  0});
		
		// curve-3
		isPeriodic = false;
		CADGeomCurve3D cadGeomCurve3D3 = factory.newCurve3D(points3, isPeriodic);
		
		//---------------------------------------------------------------------------
		// Make a loft surface with low-level OCCT data structures
		// NOTE: _<var-name> means a low-level OCCT object
		BRepOffsetAPI_ThruSections _loft = new BRepOffsetAPI_ThruSections();

		OCCShape edge1 = (OCCShape)cadGeomCurve3D1.edge();
		TopoDS_Edge _edge1 = TopoDS.ToEdge(edge1.getShape());
		OCCShape edge2 = (OCCShape)cadGeomCurve3D2.edge();
		TopoDS_Edge _edge2 = TopoDS.ToEdge(edge2.getShape());
		OCCShape edge3 = (OCCShape)cadGeomCurve3D3.edge();
		TopoDS_Edge _edge3 = TopoDS.ToEdge(edge3.getShape());

		// make wires
		BRepBuilderAPI_MakeWire _wire1 = new BRepBuilderAPI_MakeWire();
		_wire1.Add(_edge1);
		BRepBuilderAPI_MakeWire _wire2 = new BRepBuilderAPI_MakeWire();
		_wire2.Add(_edge2);
		BRepBuilderAPI_MakeWire _wire3 = new BRepBuilderAPI_MakeWire();
		_wire3.Add(_edge3);
		
		// add wires to loft structure
		_loft.AddWire(_wire1.Wire());
		_loft.AddWire(_wire2.Wire());
		_loft.AddWire(_wire3.Wire());
		
		_loft.Build();
		
		// System.out.println("Loft non-null?: " + (_loft != null));

		// Display various other properties
		GProp_GProps _property = new GProp_GProps(); // store measurements
		BRepGProp.SurfaceProperties(_loft.Shape(), _property);
		System.out.println("Loft surface area = " + _property.Mass());

		//---------------------------------------------------------------------------
		// Put everything in a compound
		BRep_Builder _builder = new BRep_Builder();
		TopoDS_Compound _compound = new TopoDS_Compound();
		_builder.MakeCompound(_compound);
		_builder.Add(_compound, _edge1);
		_builder.Add(_compound, _edge2);
		_builder.Add(_compound, _edge3);
		_builder.Add(_compound, _loft.Shape());
		
		// Write to a file
		String fileName = "test07.brep";

//		CADFace cadSurface = null; // TODO
//		cadSurface.writeNative(fileName);
		
		BRepTools.Write(_compound, fileName);
		
		System.out.println("Output written on file: " + fileName);
		
	}

}

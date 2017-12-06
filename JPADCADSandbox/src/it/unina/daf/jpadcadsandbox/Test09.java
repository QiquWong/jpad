package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADExplorer;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCFace;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAbs_Shape;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Pnt;

public class Test09 {

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
		// Make a loft surface without low-level OCCT data structures
		
		List<CADGeomCurve3D> cadGeomCurveList = new ArrayList<CADGeomCurve3D>();
		cadGeomCurveList.add(cadGeomCurve3D1);
		cadGeomCurveList.add(cadGeomCurve3D2);
		cadGeomCurveList.add(cadGeomCurve3D3);
		
		// The CADShell object
		CADShell cadShell = factory.newShell(cadGeomCurveList);
		
		System.out.println("Is cadShell null?: " + (cadShell == null));
		
		System.out.println("Shape class: " + cadShell.getClass());
		System.out.println("Is cadShape class CADShell?: " + (cadShell instanceof CADShell));
		System.out.println("Is cadShape class OCCShell?: " + (cadShell instanceof OCCShell));

		//---------------------------------------------------------------------------
		// Deform the original loft forcing a constraint

		// Display various other properties
		System.out.println("Loft surface area = " + ((OCCShell)cadShell).getArea());

		// try a filling surface
		BRepOffsetAPI_MakeFilling _filled = new BRepOffsetAPI_MakeFilling();
		TopExp_Explorer _faceExplorer = new TopExp_Explorer(((OCCShape)cadShell).getShape(), TopAbs_ShapeEnum.TopAbs_FACE);
		TopoDS_Face _loftFace = TopoDS.ToFace(_faceExplorer.Current());
		System.out.println("loftFace - Shape type: " + _loftFace.ShapeType());
		
		// finds Edges in Face
		List<TopoDS_Edge> _edgeList = new ArrayList<>();
		TopExp_Explorer _edgeExplorer = new TopExp_Explorer(_loftFace, TopAbs_ShapeEnum.TopAbs_EDGE);
		for ( ; _edgeExplorer.More()!=0; _edgeExplorer.Next()) {
			TopoDS_Edge _anEdge = TopoDS.ToEdge(_edgeExplorer.Current());
			if (BRep_Tool.Degenerated(_anEdge)==0) {
				System.out.println("REGULAR Edge!");
				_filled.Add(_anEdge, GeomAbs_Shape.GeomAbs_C0);
				_edgeList.add(_anEdge);
			}
			else {
				System.out.println("DEGENERATE Edge!");				
			}
		}

		
		// Constrain to a point off-the initial surface
		CADVertex vertexConstraint1 = factory.newVertex( 5, 12, -5);
		gp_Pnt _pointConstraint1 =  BRep_Tool.Pnt( ((OCCVertex)vertexConstraint1).getShape() );

		//System.out.println("Vertex = " + _pointConstraint1.X() + ", " + _pointConstraint1.Y() + ", " + _pointConstraint1.Z());
		
		CADVertex vertexConstraint2 = factory.newVertex(10, 12, -5);
		gp_Pnt _pointConstraint2 =  BRep_Tool.Pnt( ((OCCVertex)vertexConstraint2).getShape() );

		CADVertex vertexConstraint3 = factory.newVertex(15, 12, -5);
		gp_Pnt _pointConstraint3 =  BRep_Tool.Pnt( ((OCCVertex)vertexConstraint3).getShape() );
		
		_filled.Add(_pointConstraint1); // constraint
		_filled.Add(_pointConstraint2); // constraint
		_filled.Add(_pointConstraint3); // constraint

		_filled.LoadInitSurface(_loftFace);
		_filled.Build();
		System.out.println("Deformed surface is done? = " + _filled.IsDone());
		

		//---------------------------------------------------------------------------
		// Put everything in a compound
		BRep_Builder _builder = new BRep_Builder();
		TopoDS_Compound _compound = new TopoDS_Compound();
		_builder.MakeCompound(_compound);
		
		_edgeList.stream()
				 .forEach(_e -> _builder.Add(_compound, _e));
		
		cadGeomCurveList.stream()
						.map(c3d -> (OCCEdge)((OCCGeomCurve3D)c3d).edge())
						.forEach(e -> _builder.Add(_compound,e.getShape()));

		_builder.Add(_compound, ((OCCVertex)vertexConstraint1).getShape());
		_builder.Add(_compound, ((OCCVertex)vertexConstraint2).getShape());
		
		_builder.Add(_compound, ((OCCVertex)vertexConstraint3).getShape());
		
		_builder.Add(_compound, ((OCCShape)cadShell).getShape());
		_builder.Add(_compound, _filled.Shape());
		
		// Write to a file
		String fileName = "test09.brep";

		BRepTools.Write(_compound, fileName);
		
		System.out.println("Output written on file: " + fileName);
		
	}

}

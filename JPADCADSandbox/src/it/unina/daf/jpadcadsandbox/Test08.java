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
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import it.unina.daf.jpadcad.occ.OCCShell;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.GProp_GProps;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;

public class Test08 {

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

		if (cadShell instanceof OCCShell) {
			//-----------------------------------------------------------
			// Explore the shell in search of faces 

			// count
			CADExplorer expF = CADShapeFactory.getFactory().newExplorer();
			int faces = 0;
			for (expF.init((CADShape)cadShell, CADShapeTypes.FACE); expF.more(); expF.next())
				faces++;	
			System.out.println("Face count in cadShape: " + faces);
			
			// fill the array of faces
			CADFace[] vFaceList = new CADFace[faces];
			int kFace = 0;
			for (expF.init(cadShell, CADShapeTypes.FACE); expF.more(); expF.next()) {
				vFaceList[kFace] = (CADFace) expF.current();
				System.out.println(
					"Face, F(" + kFace + ") bounding box: " 
					+ Arrays.toString((vFaceList[kFace]).boundingBox())
				);
				kFace++;
			}
		}
		
		
		//---------------------------------------------------------------------------
		// Put everything in a compound
		BRep_Builder _builder = new BRep_Builder();
		TopoDS_Compound _compound = new TopoDS_Compound();
		_builder.MakeCompound(_compound);
		_builder.Add(_compound, ((OCCShape)cadShell).getShape());
		
		// Write to a file
		String fileName = "test08.brep";
		BRepTools.Write(_compound, fileName);
		
		System.out.println("Output written on file: " + fileName);
		
	}

}

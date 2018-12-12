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

public class Test05as {

	public static void main(String[] args) {

		System.out.println("Testing Java Wrapper of OCCT v7.0.0");
		System.out.println("Classes in package it.unina.daf.jpadcad.occ");

		CADShapeFactory.setFactory(new OCCShapeFactory());
		CADShapeFactory factory = CADShapeFactory.getFactory();

		// Creation of 1st curve

		List<double[]> points1 = new ArrayList<double[]>();
		points1.add(new double[] {1,0,0});
		points1.add(new double[]{0.949951,0.009887,0});
		points1.add(new double[]{0.899893,0.020773,0});
		points1.add(new double[]{0.799755,0.044542,0});
		points1.add(new double[]{0.699606,0.069510,0});
		points1.add(new double[]{0.599466,0.093480,0});
		points1.add(new double[]{0.499371,0.113054,0});
		points1.add(new double[]{0.399349,0.125336,0});
		points1.add(new double[]{0.299431,0.127230,0});
		points1.add(new double[]{0.249524,0.123033,0});
		points1.add(new double[]{0.199656,0.114940,0});
		points1.add(new double[]{0.149833,0.102351,0});
		points1.add(new double[]{0.100057,0.085069,0});
		points1.add(new double[]{0.075194,0.073930,0});
		points1.add(new double[]{0.050355,0.060394,0});
		points1.add(new double[]{0.025560,0.042563,0});
		points1.add(new double[]{0.013200,0.029852,0});
		points1.add(new double[]{0.008271,0.023309,0});
		points1.add(new double[]{0,0,0});
		points1.add(new double[]{0.006114,-0.020897,0});
		points1.add(new double[]{0.006219,-0.021067,0});
		points1.add(new double[]{0.008753,-0.024738,0});
		points1.add(new double[]{0.013809,-0.030781,0});
		points1.add(new double[]{0.026405,-0.041643,0});
		points1.add(new double[]{0.051513,-0.054877,0});
		points1.add(new double[]{0.076576,-0.063716,0});
		points1.add(new double[]{0.101618,-0.070358,0});
		points1.add(new double[]{0.151665,-0.080145,0});
		points1.add(new double[]{0.201683,-0.086936,0});
		points1.add(new double[]{0.251673,-0.091029,0});
		points1.add(new double[]{0.301639,-0.092625,0});
		points1.add(new double[]{0.401516,-0.090524,0});
		points1.add(new double[]{0.501344,-0.083427,0});
		points1.add(new double[]{0.601136,-0.072835,0});
		points1.add(new double[]{0.700898,-0.059146,0});
		points1.add(new double[]{0.800633,-0.042860,0});
		points1.add(new double[]{0.900338,-0.023578,0});
		points1.add(new double[]{0.950174,-0.012288,0});

		boolean isPeriodic = true;
		CADGeomCurve3D firstCurve = factory.newCurve3D(points1,isPeriodic);

		List<double[]> points2 = new ArrayList<double[]>();
		points2.add(new double[]{1,0,10});
		points2.add(new double[]{0.949992,0.003656,10});
		points2.add(new double[]{0.899981,0.008212,10});
		points2.add(new double[]{0.799952,0.019824,10});
		points2.add(new double[]{0.699918,0.033135,10});
		points2.add(new double[]{0.599884,0.046547,10});
		points2.add(new double[]{0.499855,0.058058,10});
		points2.add(new double[]{0.399837,0.065870,10});
		points2.add(new double[]{0.299837,0.067283,10});
		points2.add(new double[]{0.249846,0.065039,10});
		points2.add(new double[]{0.199862,0.060395,10});
		points2.add(new double[]{0.149884,0.053352,10});
		points2.add(new double[]{0.099914,0.043709,10});
		points2.add(new double[]{0.074933,0.037637,10});
		points2.add(new double[]{0.049955,0.030165,10});
		points2.add(new double[]{0.024984,0.020494,10});
		points2.add(new double[]{0.012505,0.013658,10});
		points2.add(new double[]{0.007515,0.010144,10});
		points2.add(new double[]{0,0,10});
		points2.add(new double[]{0.005009,-0.008223,10});
		points2.add(new double[]{0.005068,-0.008262,10});
		points2.add(new double[]{0.007572,-0.009555,10});
		points2.add(new double[]{0.012576,-0.011240,10});
		points2.add(new double[]{0.025084,-0.014104,10});
		points2.add(new double[]{0.050092,-0.017232,10});
		points2.add(new double[]{0.075095,-0.018860,10});
		points2.add(new double[]{0.100098,-0.019988,10});
		points2.add(new double[]{0.150099,-0.021444,10});
		points2.add(new double[]{0.200100,-0.022300,10});
		points2.add(new double[]{0.250099,-0.022656,10});
		points2.add(new double[]{0.300096,-0.022713,10});
		points2.add(new double[]{0.400092,-0.022625,10});
		points2.add(new double[]{0.500087,-0.022437,10});
		points2.add(new double[]{0.600080,-0.021550,10});
		points2.add(new double[]{0.700070,-0.019662,10});
		points2.add(new double[]{0.800055,-0.015974,10});
		points2.add(new double[]{0.900033,-0.009787,10});
		points2.add(new double[]{0.950018,-0.005543,10});

		CADGeomCurve3D secondCurve = factory.newCurve3D(points2,true);

		List<CADGeomCurve3D> cadCurves = new ArrayList<CADGeomCurve3D>();
		cadCurves.add(firstCurve);
		cadCurves.add(secondCurve);

		CADShell cadShell = factory.newShell(cadCurves);
		BRepOffsetAPI_MakeFilling _filled = new BRepOffsetAPI_MakeFilling();
		TopExp_Explorer _faceExplorer = new TopExp_Explorer(((OCCShape)cadShell).getShape(), TopAbs_ShapeEnum.TopAbs_FACE);
		TopoDS_Face _loftFace = TopoDS.ToFace(_faceExplorer.Current());

		//		
		//	
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

		_filled.LoadInitSurface(_loftFace);
		_filled.Build();
		System.out.println("Deformed surface is done? = " + _filled.IsDone());


		BRep_Builder _builder = new BRep_Builder();
		TopoDS_Compound _compound = new TopoDS_Compound();
		_builder.MakeCompound(_compound);

		_edgeList.stream()
		.forEach(_e -> _builder.Add(_compound, _e));

		cadCurves.stream()
		.map(c3d -> (OCCEdge)((OCCGeomCurve3D)c3d).edge())
		.forEach(e -> _builder.Add(_compound,e.getShape()));

		_builder.Add(_compound, ((OCCShape)cadShell).getShape());
		_builder.Add(_compound, _filled.Shape());

		// Write to a file
		String fileName = "test05as.brep";

		BRepTools.Write(_compound, fileName);

		System.out.println("Output written on file: " + fileName);


	}




}

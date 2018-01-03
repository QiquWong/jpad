 package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.BOPAlgo_PaveFiller;
import opencascade.BOPDS_DS;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepBuilderAPI_MakeVertex;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GeomAPI_ProjectPointOnCurve;
import opencascade.GeomAbs_Shape;
import opencascade.ShapeFix_SplitTool;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Pnt;

public class Test19a {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		OCCUtils.initCADShapeFactory();
		
		System.out.println("========== [main] Constructing sections (parallel to YZ)");
		// section 1
		List<double[]> ptsSec1 = new ArrayList<double[]>();
		ptsSec1.add(new double[]{ 0.50, 0.00, 1.50});
		ptsSec1.add(new double[]{ 0.50, 1.00, 1.30});
		ptsSec1.add(new double[]{ 0.50, 1.50, 0.00});
		CADGeomCurve3D cadSec1 = OCCUtils.theFactory.newCurve3D(ptsSec1, false);
		System.out.println(">> Section-1 parallel to plane YZ, at x = " + ptsSec1.get(0)[0]);
		
		// section 2
		List<double[]> ptsSec2 = new ArrayList<double[]>();
		ptsSec2.add(new double[]{ 2.00, 0.00, 2.50});
		ptsSec2.add(new double[]{ 2.00, 2.00, 1.45});
		ptsSec2.add(new double[]{ 2.00, 2.50, 0.00});
		CADGeomCurve3D cadSec2 = OCCUtils.theFactory.newCurve3D(ptsSec2, false);
		System.out.println(">> Section-2 parallel to plane YZ, at x = " + ptsSec2.get(0)[0]);

		// section 3
		List<double[]> ptsSec3 = new ArrayList<double[]>();
		ptsSec3.add(new double[]{ 4.00, 0.00, 4.00});
		ptsSec3.add(new double[]{ 4.00, 3.50, 3.00});
		ptsSec3.add(new double[]{ 4.00, 4.00, 0.00});
		CADGeomCurve3D cadSec3 = OCCUtils.theFactory.newCurve3D(ptsSec3, false);
		System.out.println(">> Section-3 parallel to plane YZ, at x = " + ptsSec3.get(0)[0]);

		System.out.println("========== [main] Constructing some guide curves");
		// guide-curve-1
		// guide curve in plane XZ
		List<double[]> ptsGC1 = new ArrayList<double[]>();
		ptsGC1.add(new double[]{ 0.00, 0.00, 0.00});
		ptsGC1.add(ptsSec1.get(0));
		ptsGC1.add(ptsSec2.get(0));
		double[] vX0 = new double[]{ 
				0.1*ptsSec2.get(0)[0] + 0.9*ptsSec3.get(0)[0], // pick an X-station between sec-2 & sec-3
				0.00, 
				1.01*ptsSec3.get(0)[2]};
		ptsGC1.add(vX0); // tweak the Z
		ptsGC1.add(ptsSec3.get(0));		
		CADGeomCurve3D cadGC1 = OCCUtils.theFactory.newCurve3D(ptsGC1, false);
		System.out.println(">> Guide-Curve-1, in plane XZ, connecting (0,0,0) and initial points on sections 1-2-3.");

		// guide-curve-2
		// guide curve in plane XY
		List<double[]> ptsGC2 = new ArrayList<double[]>();
		ptsGC2.add(new double[]{ 0.00, 0.00, 0.00});
		ptsGC2.add(ptsSec1.get(2));
		ptsGC2.add(ptsSec2.get(2));
		double[] vX1 = new double[]{ 
				0.1*ptsSec2.get(2)[0] + 0.9*ptsSec3.get(2)[0], // pick an X-station between sec-2 & sec-3
				1.00*ptsSec3.get(2)[1], 
				0.00};
		ptsGC2.add(vX1); // tweak the Z
		ptsGC2.add(ptsSec3.get(2));
		CADGeomCurve3D cadGC2 = OCCUtils.theFactory.newCurve3D(ptsGC2, false);
		System.out.println(">> Guide-Curve-2, in plane XY, connecting (0,0,0) and final points on sections 1-2-3.");
		
		// 3 POINTS ALONG SECTIONS 1-2-3
		double[] rangeS1 = cadSec1.getRange();
		System.out.println(">> Sec-1 range: " + Arrays.toString(rangeS1));
		double[] vPntS1a = ((OCCGeomCurve3D)cadSec1).value(0.25*(rangeS1[1] - rangeS1[0]));
		double[] vPntS1b = ((OCCGeomCurve3D)cadSec1).value(0.50*(rangeS1[1] - rangeS1[0]));
		double[] vPntS1c = ((OCCGeomCurve3D)cadSec1).value(0.75*(rangeS1[1] - rangeS1[0]));

		double[] rangeS2 = cadSec2.getRange();
		System.out.println(">> Sec-2 range: " + Arrays.toString(rangeS2));
		double[] vPntS2a = ((OCCGeomCurve3D)cadSec2).value(0.25*(rangeS2[1] - rangeS2[0]));
		double[] vPntS2b = ((OCCGeomCurve3D)cadSec2).value(0.50*(rangeS2[1] - rangeS2[0]));
		double[] vPntS2c = ((OCCGeomCurve3D)cadSec2).value(0.75*(rangeS2[1] - rangeS2[0]));

		double[] rangeS3 = cadSec3.getRange();
		System.out.println(">> Sec-3 range: " + Arrays.toString(rangeS3));		
		double[] vPntS3a = ((OCCGeomCurve3D)cadSec3).value(0.25*(rangeS3[1] - rangeS3[0]));
		double[] vPntS3b = ((OCCGeomCurve3D)cadSec3).value(0.50*(rangeS3[1] - rangeS3[0]));
		double[] vPntS3c = ((OCCGeomCurve3D)cadSec3).value(0.75*(rangeS3[1] - rangeS3[0]));

		List<double[]> ptsGCm = new ArrayList<double[]>();
		ptsGCm.add(new double[]{ 0.00, 0.00, 0.00});
		ptsGCm.add(vPntS1b);
		ptsGCm.add(vPntS2b);
		double[] vPntS3m = new double[]{ // extra-point on mid-guide-curve
				0.8*vPntS3b[0] + 0.2*vPntS2b[0] + 0.0,
				0.8*vPntS3b[1] + 0.2*vPntS2b[1] + 0.25, 
				0.8*vPntS3b[2] + 0.2*vPntS2b[2] + 0.25};
		ptsGCm.add(vPntS3m);
		ptsGCm.add(vPntS3b);

		CADGeomCurve3D cadGCm = OCCUtils.theFactory.newCurve3D(ptsGCm, false);
		System.out.println(">> Guide-Curve-M, connecting (0,0,0) and middle points on sections 1-2-3 plus an extra-point betewen sec-2 & -3.");
		
		double[] rangeGCm = cadGCm.getRange();
		System.out.println(">> Guide-Curve-M range: " + Arrays.toString(rangeGCm));		
		double[] vPntGCMa = ((OCCGeomCurve3D)cadGCm).value(0.50*(rangeGCm[1] - rangeGCm[0]));
		double[] vPntGCMb = ((OCCGeomCurve3D)cadGCm).value(0.70*(rangeGCm[1] - rangeGCm[0]));
		double[] vPntGCMc = ((OCCGeomCurve3D)cadGCm).value(0.90*(rangeGCm[1] - rangeGCm[0]));

		System.out.println("========== [main] Splitting curves");
				
		System.out.println(">> Split Guide-Curve-1 ...");
		List<OCCEdge> subEdgesGC1 = OCCUtils.splitEdge(cadGC1, ptsSec1.get(0));
		System.out.println(">> Subedges: " + subEdgesGC1.size());

		System.out.println(">> Split Guide-Curve-2 ...");
		List<OCCEdge> subEdgesGC2 = OCCUtils.splitEdge(cadGC2, ptsSec1.get(2));
		System.out.println(">> Subedges: " + subEdgesGC2.size());

		// Export shapes
		List<OCCShape> shapes = new ArrayList<>();
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadSec1).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadSec2).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadSec3).edge());
		//shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC1).edge());
		shapes.add(subEdgesGC1.get(0));
		shapes.add(subEdgesGC1.get(1));
		//shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC2).edge());
		shapes.add(subEdgesGC2.get(0));
		//shapes.add((OCCEdge)((OCCGeomCurve3D)cadGCm).edge());
		
		System.out.println("========== [main] Discretizing section-1");
		int nPointsSec1d = 10;
		cadSec1.discretize(nPointsSec1d);
		System.out.println(">> N. points: " + cadSec1.nbPoints());
		List<gp_Pnt> gpPntSec1 = ((OCCGeomCurve3D)cadSec1).getDiscretizedCurve().getPoints();
		gpPntSec1.stream()
			.forEach(gpPnt ->
				System.out.println(">> (" + gpPnt.X() +", "+ gpPnt.Y() +", "+ gpPnt.Z() + ")")
				);

		System.out.println("========== [main] Loft filling sub-edges of guide-curves 1-2 and points on section-1");
		// Filler approach, that works
		BRepOffsetAPI_MakeFilling fillMaker = new BRepOffsetAPI_MakeFilling();
		
		fillMaker.Add(
				subEdgesGC1.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillMaker.Add(
				subEdgesGC2.get(0).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		// adjust first and last point of discretized section-1
		OCCVertex vtxGC1 = OCCUtils.getVertexFromEdge((OCCEdge)subEdgesGC1.get(0), 1);
		// tweak first point
		gpPntSec1.set(0, BRep_Tool.Pnt(vtxGC1.getShape()));
		
		OCCVertex vtxGC2 = OCCUtils.getVertexFromEdge((OCCEdge)subEdgesGC2.get(0), 1);
		// tweak last point
		gpPntSec1.set(nPointsSec1d - 1, BRep_Tool.Pnt(vtxGC2.getShape()));
		
		System.out.println(">> Points on section-curve-1 (re-discretized):");
		gpPntSec1.stream()
			.forEach(gpPnt ->
				System.out.println(">> (" + gpPnt.X() +", "+ gpPnt.Y() +", "+ gpPnt.Z() + ")")
			);
		
		CADGeomCurve3D cadSec1d = OCCUtils.theFactory.newCurve3DGP(
				gpPntSec1, 
				false);
		// third boundary curve for filling
		fillMaker.Add(
				((OCCEdge)((OCCGeomCurve3D)cadSec1d).edge()).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		System.out.println("========== [main] Additional constraints to filling loft");
		
		System.out.println(">> Split Guide-Curve-M ...");
		List<OCCEdge> subEdgesGCm = OCCUtils.splitEdge(cadGCm, vPntS1b);
		System.out.println(">> Subedges: " + subEdgesGCm.size());

		// export first sub-edge
		shapes.add(subEdgesGCm.get(0));

		// get a point on the new cadedge
		CADGeomCurve3D cadGCms = OCCUtils.theFactory.newCurve3D(subEdgesGCm.get(0));
		double[] rangeGCms = cadGCms.getRange();
		double[] vPntGCM_1 = ((OCCGeomCurve3D)cadGCms).value(0.50*(rangeGCms[1] - rangeGCms[0]));
		
		// constrain loft to this point
		System.out.println(">> constraining filler surface to point (" + vPntGCM_1[0] +", "+ vPntGCM_1[1] +", "+ vPntGCM_1[2] + ")");
		
		fillMaker.Add(new gp_Pnt(vPntGCM_1[0], vPntGCM_1[1], vPntGCM_1[2]));
		
		// finally, build the filling loft
		fillMaker.Build();
		System.out.println("Deformed surface is done? = " + fillMaker.IsDone());
		System.out.println("Deformed surface shape  type: " + fillMaker.Shape().ShapeType());
		
		// Add loft to the export list
		shapes.add((OCCShape)OCCUtils.theFactory.newShape(fillMaker.Shape()));
		
		System.out.println("========== [main] Try a curve with tangency constraints");

		List<double[]> ptsGC2t = new ArrayList<double[]>();
		ptsGC2t.add(new double[]{ 0.00, 0.00, 0.00});
		ptsGC2t.add(ptsSec1.get(2));
		ptsGC2t.add(ptsSec2.get(2));
		CADGeomCurve3D cadGC2t = OCCUtils.theFactory.newCurve3D(
				ptsGC2t, false, // closed curve?
				new double[] {0.0, 4.0, 0.0}, // initial tangent vector
				new double[] {1.0, 0.0, 0.0}, // initial tangent vector
				true // scale tangents?
				);
		System.out.println(">> Guide-Curve-1-T, in plane XY.");
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC2t).edge());
		
		// Write to a file
		String fileName = "test19a.brep";
		
		if (OCCUtils.write(fileName, shapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
	}

}

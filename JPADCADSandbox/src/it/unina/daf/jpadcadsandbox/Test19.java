package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.GeomAbs_Shape;
import opencascade.TopoDS;
import opencascade.TopoDS_Face;
import opencascade.gp_Pnt;

public class Test19 {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");

		OCCUtils.initCADShapeFactory();
		
		System.out.println("========== [main] Constructing the sections of Patch-1");
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
		

		System.out.println("========== [main] Constructing the patch, Patch-1");
		List<CADGeomCurve3D> cadSections = new ArrayList<>();
		cadSections.add(cadSec1);
		cadSections.add(cadSec2);
		cadSections.add(cadSec3);
		OCCShape patch1 = OCCUtils.makePatchThruCurveSections( // -> OCCShell
				new OCCVertex(0.0, 0.0, 0.0),
				cadSections
				);
		System.out.println(">> loft through point (0,0,0) and sections 1-2-3. Done.");
		
		// guide-curve-1
		System.out.println("========== [main] Constructing the guide curves to build a filler patch");
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
				0.8*vPntS3b[1] + 0.2*vPntS2b[1] + 0.5, 
				0.8*vPntS3b[2] + 0.2*vPntS2b[2] + 0.5};
		ptsGCm.add(vPntS3m);
		ptsGCm.add(vPntS3b);

		CADGeomCurve3D cadGCm = OCCUtils.theFactory.newCurve3D(ptsGCm, false);
		System.out.println(">> Guide-Curve-M, connecting (0,0,0) and middle points on sections 1-2-3 plus an extra-point betewen sec-2 & -3.");
		
		double[] rangeGCm = cadGCm.getRange();
		System.out.println(">> Guide-Curve-M range: " + Arrays.toString(rangeGCm));		
		double[] vPntGCMa = ((OCCGeomCurve3D)cadGCm).value(0.50*(rangeGCm[1] - rangeGCm[0]));
		double[] vPntGCMb = ((OCCGeomCurve3D)cadGCm).value(0.70*(rangeGCm[1] - rangeGCm[0]));
		double[] vPntGCMc = ((OCCGeomCurve3D)cadGCm).value(0.90*(rangeGCm[1] - rangeGCm[0]));

		
		System.out.println("========== [main] Preparing the filler surface to deform Patch-1");
		
/*
 * this approach with a plating surface does not work
 * 
		// plate maker
		GeomPlate_BuildPlateSurface plateMaker = new GeomPlate_BuildPlateSurface(
				BRep_Tool.Surface(
						TopoDS.ToFace(patch1.getShape())
				));
		plateMaker.Init(); // Resets all constraints
		Geom_Surface geomSurface0 = BRep_Tool.Surface(TopoDS.ToFace(patch1.getShape()));
		plateMaker.LoadInitSurface(geomSurface0);
		
		// curve constraint
		BRepAdaptor_Curve adCrv1 = new BRepAdaptor_Curve(
				( (OCCEdge)((OCCGeomCurve3D)cadGC1).edge() ).getShape()
				);
		BRepAdaptor_HCurve adHCrv1 = new BRepAdaptor_HCurve(adCrv1);
		GeomPlate_CurveConstraint crvConstraint1 = new GeomPlate_CurveConstraint(
				adHCrv1, 0, 20, 0.01
				);
		// add the constraint curve to the plate maker
		plateMaker.Add(crvConstraint1);
		
//		plateMaker.Perform(); // FIXME: crashes

*/
		// Filler approach, that works
		BRepOffsetAPI_MakeFilling fillMaker = new BRepOffsetAPI_MakeFilling();
		
		TopoDS_Face loftFace = TopoDS.ToFace( patch1.getShape() );
		
		fillMaker.Add(
				( (OCCEdge)((OCCGeomCurve3D)cadGC1).edge() ).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		fillMaker.Add(
				( (OCCEdge)((OCCGeomCurve3D)cadGC2).edge() ).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
// 
// Constraining filler to pass through sections 2 & 3 doesn't work
//
//		fillMaker.Add(
//				( (OCCEdge)((OCCGeomCurve3D)cadSec1).edge() ).getShape(),
//				GeomAbs_Shape.GeomAbs_C0
//				);
//		fillMaker.Add(
//				( (OCCEdge)((OCCGeomCurve3D)cadSec2).edge() ).getShape(),
//				GeomAbs_Shape.GeomAbs_C0
//				);

		fillMaker.Add(
				( (OCCEdge)((OCCGeomCurve3D)cadSec3).edge() ).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);

		System.out.println(">> constraining filler surface to guide curves 1-2, and section 3.");
		
		fillMaker.Add(new gp_Pnt(vPntS1a[0], vPntS1a[1], vPntS1a[2]));

		System.out.println(">> constraining filler surface to 1 point along section 1.");

		fillMaker.Add(new gp_Pnt(vPntS2a[0], vPntS2a[1], vPntS2a[2]));
		fillMaker.Add(new gp_Pnt(vPntS2b[0], vPntS2b[1], vPntS2b[2]));
		fillMaker.Add(new gp_Pnt(vPntS2c[0], vPntS2c[1], vPntS2c[2]));

		System.out.println(">> constraining filler surface to 3 points along section 2.");
		
		fillMaker.Add(new gp_Pnt(vPntGCMa[0], vPntGCMa[1], vPntGCMa[2]));
		fillMaker.Add(new gp_Pnt(vPntGCMb[0], vPntGCMb[1], vPntGCMb[2]));
		fillMaker.Add(new gp_Pnt(vPntGCMc[0], vPntGCMc[1], vPntGCMc[2]));
		System.out.println(">> constraining filler surface to 3 points along Guide-Curve-M.");
		
		
		// fillMaker.LoadInitSurface(loftFace);
		// DO NOT init, let OCC initialize the fillMaker surface automatically
		
		fillMaker.Build();
		System.out.println("Deformed surface is done? = " + fillMaker.IsDone());
		System.out.println("Deformed surface shape  type: " + fillMaker.Shape().ShapeType());
		
		// Export shapes
		List<OCCShape> shapes = new ArrayList<>();
		cadSections.stream()
			.forEach(s -> shapes.add((OCCEdge)((OCCGeomCurve3D)s).edge()));
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC1).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadGC2).edge());
		shapes.add((OCCEdge)((OCCGeomCurve3D)cadGCm).edge());
		
		// uncomment to export patch passing through sections 1-2-3 
		//shapes.add(patch1);
		
		shapes.add((OCCShape)OCCUtils.theFactory.newShape(fillMaker.Shape()));
		
		// Write to a file
		String fileName = "test19.brep";
		
		if (OCCUtils.write(fileName, shapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}

}

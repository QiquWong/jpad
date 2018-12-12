package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.occ.OCCWire;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepTools_WireExplorer;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.Geom2d_BSplineCurve;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TopoDS;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax1;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test32mds {

	public static void main(String[] args) {
		System.out.println("---------------------------------------------");
		System.out.println("---------------- JPADCAD Test ---------------");
		System.out.println("---------------------------------------------");
		
		if (OCCUtils.theFactory == null) {
			OCCUtils.initCADShapeFactory();
		}
		
		List<gp_Pnt> guideCrvPts = new ArrayList<>();
		
		gp_Pnt pt1 = new gp_Pnt(0.0, 0.0, 0.0);
		gp_Pnt pt3 = new gp_Pnt(0.1, 0.5, 1.5);
		
		double yPt2 = 0.7;
		double xPt2 = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {0.0, 1.0}, new double[] {0.0, 0.1}, yPt2);
		
		gp_Pnt pt2 = new gp_Pnt(xPt2, yPt2, 0.05);
		
		guideCrvPts.add(pt1);
		guideCrvPts.add(pt2);
		guideCrvPts.add(pt3);
		
		CADGeomCurve3D guideCrv = OCCUtils.theFactory.newCurve3DGP(guideCrvPts, false);
		
		List<double[]> pts = new ArrayList<>();
		double[] ptA = new double[] {0.0, 0.0, 0.0};
		double[] ptB = new double[] {0.1, 0.0, 0.1};
		double[] ptC = new double[] {0.2, 0.0, 0.0};
		pts.add(ptA);
		pts.add(ptB);
		pts.add(ptC);
		
		double sumX = 0;
		double sumY = 0;
		double sumZ = 0;
		for (int i = 0; i < pts.size(); i++) {
			sumX += pts.get(i)[0];
			sumY += pts.get(i)[1];
			sumZ += pts.get(i)[2];
		}
		double[] cg = new double[] {sumX/pts.size(), sumY/pts.size(), sumZ/pts.size()};
		
		CADWire wire = OCCUtils.theFactory.newWireFromAdjacentEdges(
				OCCUtils.theFactory.newCurve3D(ptA, ptB).edge(),
				OCCUtils.theFactory.newCurve3D(ptB, ptC).edge(),
				OCCUtils.theFactory.newCurve3D(ptC, ptA).edge()
				);
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface horizontal = aircraft.getHTail();
		
		Airfoil airfoil = horizontal.getAirfoilList().get(0);
		double[] xCoords = airfoil.getXCoords();
		double[] zCoords = airfoil.getZCoords();
		
		List<double[]> airfoilPts = new ArrayList<>();
		for (int i = 0; i < xCoords.length; i++) 			
			airfoilPts.add(new double[] {xCoords[i], 0.0, zCoords[i]});
		
		double sumXAirfoil = 0;
		double sumYAirfoil = 0;
		double sumZAirfoil = 0;
		for (int i = 0; i < airfoilPts.size(); i++) {
			sumXAirfoil += airfoilPts.get(i)[0];
			sumYAirfoil += airfoilPts.get(i)[1];
			sumZAirfoil += airfoilPts.get(i)[2];
		}
		
		double[] cgAirfoil = new double[] {
				sumXAirfoil/airfoilPts.size(), 
				sumYAirfoil/airfoilPts.size(), 
				sumZAirfoil/airfoilPts.size()};
		
		CADWire airfoilWire = OCCUtils.theFactory.newWireFromAdjacentEdges(
				OCCUtils.theFactory.newCurve3D(airfoilPts, false).edge(),
				OCCUtils.theFactory.newCurve3D(
						airfoilPts.get(0), 
						airfoilPts.get(airfoilPts.size()-1)).edge()
				);
		
		gp_Trsf preScale = new gp_Trsf();
		preScale.SetScale(new gp_Pnt(cgAirfoil[0], cgAirfoil[1], cgAirfoil[2]), 0.6);
		
		airfoilWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
				new BRepBuilderAPI_Transform(
						((OCCWire) airfoilWire).getShape(), preScale, 0).Shape()));
		
		gp_Trsf preTranslate = new gp_Trsf();
		preTranslate.SetTranslation(getFirstGPWire(airfoilWire), guideCrvPts.get(0));
		
		airfoilWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
				new BRepBuilderAPI_Transform(
						((OCCWire) airfoilWire).getShape(), preTranslate, 0).Shape()));
		
		guideCrv.discretize(15);
		
		double[] guideCrvParams = ((OCCGeomCurve3D) guideCrv).getDiscretizedCurve().getParams();
		List<gp_Pnt> guideCrvDPts = ((OCCGeomCurve3D) guideCrv).getDiscretizedCurve().getPoints();
		
		double[] scaleFactors = MyArrayUtils.linspace(1.0, 0.3, guideCrvDPts.size());
		
		List<CADWire> transfWires = new ArrayList<>();
		transfWires.add(wire);
		for (int i = 1; i < guideCrvDPts.size(); i++) {
			
			gp_Trsf translate = new gp_Trsf();
			gp_Trsf scale = new gp_Trsf();
			gp_Trsf rotate = new gp_Trsf();
			
			// SCALING		
			scale.SetScale(new gp_Pnt(cg[0], cg[1], cg[2]), scaleFactors[i]);
			
			CADWire scaledWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) wire).getShape(), scale, 0).Shape()));
			
			// ROTATION
			gp_Ax1 rotAxis = new gp_Ax1(
					new gp_Pnt(0.0, 0.0, 0.0), 
					new gp_Dir(new gp_Vec(1.0, 0.0, 0.0)));
			
			gp_Vec guideCrvTang = new gp_Vec();
			((OCCGeomCurve3D) guideCrv).getAdaptorCurve().D1(guideCrvParams[i], new gp_Pnt(), guideCrvTang);
			
			gp_Vec bNormal = new gp_Vec(0.0, 1.0, 0.0);
			gp_Vec iNormal = new gp_Vec(0.0, guideCrvTang.Y(), guideCrvTang.Z());
			
			double rotAngle = bNormal.AngleWithRef(iNormal, new gp_Vec(1.0, 0.0, 0.0));
			
			rotate.SetRotation(rotAxis, rotAngle);
			
			CADWire rotatedWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) scaledWire).getShape(), rotate, 0).Shape()));
			
			// TRANSLATION
			translate.SetTranslation(getFirstGPWire(rotatedWire), guideCrvDPts.get(i));
			
			CADWire translatedWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) rotatedWire).getShape(), translate, 0).Shape()));
					
			transfWires.add(translatedWire);		
		}
		
		List<CADWire> transfAirfoilWires = new ArrayList<>();
		transfAirfoilWires.add(airfoilWire);
		for (int i = 1; i < guideCrvDPts.size(); i++) {
			
			gp_Trsf translate = new gp_Trsf();
			gp_Trsf scale = new gp_Trsf();
			gp_Trsf rotate = new gp_Trsf();
			
			// SCALING		
			scale.SetScale(new gp_Pnt(cgAirfoil[0], cgAirfoil[1], cgAirfoil[2]), scaleFactors[i]);
			
			CADWire scaledWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) airfoilWire).getShape(), scale, 0).Shape()));
			
			// ROTATION
			gp_Ax1 rotAxis = new gp_Ax1(
					new gp_Pnt(0.0, 0.0, 0.0), 
					new gp_Dir(new gp_Vec(1.0, 0.0, 0.0)));
			
			gp_Vec guideCrvTang = new gp_Vec();
			((OCCGeomCurve3D) guideCrv).getAdaptorCurve().D1(guideCrvParams[i], new gp_Pnt(), guideCrvTang);
			
			gp_Vec bNormal = new gp_Vec(0.0, 1.0, 0.0);
			gp_Vec iNormal = new gp_Vec(0.0, guideCrvTang.Y(), guideCrvTang.Z());
			
			double rotAngle = bNormal.AngleWithRef(iNormal, new gp_Vec(1.0, 0.0, 0.0));
			
			rotate.SetRotation(rotAxis, rotAngle);
			
			CADWire rotatedWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) scaledWire).getShape(), rotate, 0).Shape()));
			
			// TRANSLATION
			translate.SetTranslation(getFirstGPWire(rotatedWire), guideCrvDPts.get(i));
			
			CADWire translatedWire = (CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(
					new BRepBuilderAPI_Transform(
							((OCCWire) rotatedWire).getShape(), translate, 0).Shape()));
					
			transfAirfoilWires.add(translatedWire);		
		}
		
		// THRU SECTIONS
		OCCShape pipe = OCCUtils.makePatchThruSections(transfWires);	
		OCCShape airfoilPipe = OCCUtils.makePatchThruSections(transfAirfoilWires);
		
		// TRANSLATE THE AIRFOIL PIPE
		gp_Trsf postTranslate = new gp_Trsf();
		postTranslate.SetTranslation(new gp_Vec(3.0, 0.0, 0.0));
		
		airfoilPipe = (OCCShape) OCCUtils.theFactory.newShape(
				new BRepBuilderAPI_Transform(
						airfoilPipe.getShape(), postTranslate, 0).Shape());
			
		String filename = "Test32mds.brep";
		
		if (OCCUtils.write(filename, pipe, airfoilPipe))
			System.out.println("[Test32mds] CAD shapes correctly written to file (" + filename + ")");
	}

	private static CADVertex getFirstVtxWire(CADWire wire) {
		
		BRepTools_WireExplorer wireExplorer = new BRepTools_WireExplorer(
				TopoDS.ToWire(((OCCWire) wire).getShape()));
		
		return (CADVertex) OCCUtils.theFactory.newShape(wireExplorer.CurrentVertex());
	}
	
	private static double[] getFirstPtWire(CADWire wire) {
		
		return getFirstVtxWire(wire).pnt();
	}
	
	private static gp_Pnt getFirstGPWire(CADWire wire) {
		
		return BRep_Tool.Pnt(
				TopoDS.ToVertex(
						((OCCVertex) getFirstVtxWire(wire)).getShape()));
	}
}

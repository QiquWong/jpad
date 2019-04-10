package it.unina.daf.jpadcadsandbox;

import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TopAbs_Orientation;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax1;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;


public class Test06as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test06as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface horizontalTail = aircraft.getHTail();

		double wingSemiSpan = horizontalTail.getSemiSpan().doubleValue(horizontalTail.getSemiSpan().getUnit());
		System.out.println(wingSemiSpan);
		double flapInnerSection = 1.0;
		double flapOuterSection = 4;
		double flapChordRatio = 0.25;
		System.out.println("Flap data: ");
		System.out.println("Inner section: "+flapInnerSection+" m");
		System.out.println("Outer section: "+flapOuterSection+" m");
		System.out.println("Flap chord ratio: "+ flapChordRatio);

		// Getting airfoil coordinates
		Airfoil airfoil = horizontalTail.getAirfoilList().get(0);
		double[] airfoilXCoords = airfoil.getXCoords();
		double[] airfoilZCoords = airfoil.getZCoords();

		List<double[]> airfoilPoints = new ArrayList<>(); 
		for(int i = 0; i < airfoilXCoords.length; i++) 
			airfoilPoints.add(new double[] {airfoilXCoords[i], 0.0, airfoilZCoords[i]});

		OCCUtils.initCADShapeFactory();
		BRepBuilderAPI_MakeWire airfoilWire = new BRepBuilderAPI_MakeWire();
		airfoilWire.Add(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(airfoilPoints, false).edge()).getShape()));

		// Adding Edge on trailing edge if airfoil is opened
		if((airfoilPoints.get(0)[2] - airfoilPoints.get(airfoilPoints.size()-1)[2]) > 1e-5)
			airfoilWire.Add(
					new BRepBuilderAPI_MakeEdge(
							new gp_Pnt(
									airfoilPoints.get(airfoilPoints.size()-1)[0],
									airfoilPoints.get(airfoilPoints.size()-1)[1],
									airfoilPoints.get(airfoilPoints.size()-1)[2]
									), 
							new gp_Pnt(
									airfoilPoints.get(0)[0],
									airfoilPoints.get(0)[1],
									airfoilPoints.get(0)[2]
									)).Edge());

		TopoDS_Face airfoilFace = new BRepBuilderAPI_MakeFace(airfoilWire.Wire()).Face();
		gp_Vec wingAxis = new gp_Vec(0.0, flapInnerSection, 0.0);
		System.out.println("Building first wing section ...");
		TopoDS_Shape firstWingSection = new BRepPrimAPI_MakePrism(airfoilFace, wingAxis, 1, 0).Shape();



		Double[] airfoilCutZCoords  = AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio + 0.05);
		Double[] airfoilCutZCoords1 = AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio + 0.05 - 0.01);


		List<gp_Pnt> airfoilCutPoints = new ArrayList<>();
		airfoilCutPoints.add(new gp_Pnt(1-flapChordRatio+0.05, flapInnerSection, airfoilCutZCoords[0]));
		airfoilCutPoints.add(new gp_Pnt(1-flapChordRatio,flapInnerSection, Math.abs((AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio)[1]+AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio)[0]))/2));
		airfoilCutPoints.add(new gp_Pnt(1-flapChordRatio+0.05,flapInnerSection, airfoilCutZCoords[1]));

		TopoDS_Edge cutEdge = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(airfoilCutPoints, false).edge()).getShape());

		List<gp_Pnt> airfoilGapPoints = new ArrayList<>();
		airfoilGapPoints.add(new gp_Pnt(1-flapChordRatio+0.05-0.01, flapInnerSection, airfoilCutZCoords1[0]));
		airfoilGapPoints.add(new gp_Pnt(1-flapChordRatio-0.01,flapInnerSection, Math.abs((AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio-0.01)[1]+AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio-0.01)[0]))/2));
		airfoilGapPoints.add(new gp_Pnt(1-flapChordRatio+0.05-0.01,flapInnerSection, airfoilCutZCoords1[1]));

		TopoDS_Edge cutEdge1 = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(airfoilGapPoints, false).edge()).getShape());





		double[] firstFlapPointCut = { airfoilCutPoints.get(0).X(), airfoilCutPoints.get(0).Y(), airfoilCutPoints.get(0).Z() };
		List<OCCEdge> cutsAirfoil1 = OCCUtils.splitEdge( OCCUtils.theFactory.newCurve3D(airfoilPoints, false), firstFlapPointCut);
		TopoDS_Edge cutAirfoil_1 = TopoDS.ToEdge(cutsAirfoil1.get(0).getShape());
		TopoDS_Edge cutAirfoil_2 = TopoDS.ToEdge(cutsAirfoil1.get(1).getShape());

		double[] secondFlapPointCut = { airfoilCutPoints.get(airfoilCutPoints.size()-1).X(), airfoilCutPoints.get(airfoilCutPoints.size()-1).Y(), airfoilCutPoints.get(airfoilCutPoints.size()-1).Z() };
		List<OCCEdge> cutsAirfoil2 = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(cutAirfoil_2)),
				secondFlapPointCut
				);
		TopoDS_Edge cutAirfoil_3 = TopoDS.ToEdge(cutsAirfoil2.get(0).getShape());
		TopoDS_Edge cutAirfoil_4 = TopoDS.ToEdge(cutsAirfoil2.get(1).getShape());



		double refLength = 0;
		int index = 0;

		for(int j = 0; j < cutsAirfoil2.size(); j++) {
			TopoDS_Edge edge = TopoDS.ToEdge(cutsAirfoil2.get(j).getShape());
			double length = calcLength(edge);
			if(length > refLength) {
				refLength = length;
				index = j;
			}

		}

		TopoDS_Edge cutAirfoil_5 =  TopoDS.ToEdge(cutsAirfoil2.get(index).getShape());;

		double[] thirdFlapPointCut = { airfoilGapPoints.get(airfoilGapPoints.size()-1).X(), airfoilGapPoints.get(airfoilGapPoints.size()-1).Y(), airfoilGapPoints.get(airfoilGapPoints.size()-1).Z() };
		List<OCCEdge> cutsAirfoil3 = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(cutAirfoil_5)),
				thirdFlapPointCut
				);


		double refLength_2 = 0;
		int index_2 = 0;

		for(int j = 0; j < cutsAirfoil3.size(); j++) {
			TopoDS_Edge edge = TopoDS.ToEdge(cutsAirfoil3.get(j).getShape());
			double length = calcLength(edge);
			if(length > refLength_2) {
				refLength_2 = length;
				index_2 = j;
			}

		}

		TopoDS_Edge cutAirfoil_6 =  TopoDS.ToEdge(cutsAirfoil3.get(index_2).getShape());;

		double[] fourthFlapPointCut = { airfoilGapPoints.get(0).X(), airfoilGapPoints.get(0).Y(), airfoilGapPoints.get(0).Z() };
		List<OCCEdge> cutsAirfoil4 = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(cutAirfoil_6)),
				fourthFlapPointCut
				);

		double refLength_3 = 0;
		int index_3 = 0;

		for(int j = 0; j < cutsAirfoil4.size(); j++) {
			TopoDS_Edge edge = TopoDS.ToEdge(cutsAirfoil4.get(j).getShape());
			double length = calcLength(edge);
			if(length > refLength_3) {
				refLength_3 = length;
				index_3 = j;
			}

		}

		TopoDS_Edge cutAirfoil_7 =  TopoDS.ToEdge(cutsAirfoil4.get(index_3).getShape());
		gp_Trsf trasl = new gp_Trsf();
		trasl.SetTranslation(new gp_Pnt(0,0,0),new gp_Pnt(0,1,0));
		TopoDS_Edge cutAirfoil_8 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutAirfoil_7,trasl).Shape());



		// Wing 2nd section

		TopoDS_Wire wireSection2 = new BRepBuilderAPI_MakeWire(cutAirfoil_8,cutEdge1).Wire();
		TopoDS_Face airfoilFace2 = new BRepBuilderAPI_MakeFace(wireSection2).Face();
		gp_Vec wingAxis2 = new gp_Vec(0.0, flapOuterSection - flapInnerSection, 0.0);
		System.out.println("Building second wing section ...");
		TopoDS_Shape secondWingSection = new BRepPrimAPI_MakePrism(airfoilFace2, wingAxis2, 1, 0).Shape();

		System.out.println("New wing section created");

		// Flap 

		// cutEdge + cutAirfoil_1 + cutAirfoil_4
		gp_Trsf flapTrasl = new gp_Trsf();
		flapTrasl.SetTranslation(new gp_Pnt(0,0,0),new gp_Pnt(0,1+0.025,0));
		gp_Trsf flapTrasl2 = new gp_Trsf();
		flapTrasl2.SetTranslation(new gp_Pnt(0,0,0),new gp_Pnt(0,0.025,0));
		TopoDS_Edge cutAirfoil_9 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutAirfoil_1,flapTrasl).Shape());
		TopoDS_Edge cutAirfoil_10 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutAirfoil_4,flapTrasl).Shape());
		TopoDS_Edge cutEdgeTrasl = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutEdge,flapTrasl2).Shape());
		//TopoDS_Wire wireSectionFlap = new BRepBuilderAPI_MakeWire(cutAirfoil_9,cutEdgeTrasl,cutAirfoil_10).Wire();

		BRepBuilderAPI_MakeWire flapWire = new BRepBuilderAPI_MakeWire();
		flapWire.Add(cutAirfoil_9);
		flapWire.Add(cutEdgeTrasl);
		flapWire.Add(cutAirfoil_10);

		// Adding Edge on trailing edge if airfoil is opened
		// questo edge anche deve traslare!!!
		if((airfoilPoints.get(0)[2] - airfoilPoints.get(airfoilPoints.size()-1)[2]) > 1e-5)
			flapWire.Add(TopoDS.ToEdge(new BRepBuilderAPI_Transform(
					new BRepBuilderAPI_MakeEdge(
							new gp_Pnt(
									airfoilPoints.get(airfoilPoints.size()-1)[0],
									airfoilPoints.get(airfoilPoints.size()-1)[1],
									airfoilPoints.get(airfoilPoints.size()-1)[2]
									), 
							new gp_Pnt(
									airfoilPoints.get(0)[0],
									airfoilPoints.get(0)[1],
									airfoilPoints.get(0)[2]
									)).Edge(),flapTrasl).Shape()));


		TopoDS_Face flapFace = new BRepBuilderAPI_MakeFace(flapWire.Wire()).Face();
		gp_Vec flapAxis = new gp_Vec(0.0 , flapOuterSection - 0.025 - 0.025  - flapInnerSection, 0.0);
		System.out.println("Building first wing section ...");
		TopoDS_Shape flapSection = new BRepPrimAPI_MakePrism(flapFace, flapAxis, 1, 0).Shape();

		// Last wing section

		gp_Trsf firstSectionTrasl = new gp_Trsf();
		firstSectionTrasl.SetTranslation(new gp_Pnt(0,0,0),new gp_Pnt(0,flapOuterSection,0));

		TopoDS_Face airfoilFace3 = TopoDS.ToFace(new BRepBuilderAPI_Transform(airfoilFace,firstSectionTrasl).Shape());
		System.out.println("Building last wing section ...");
		gp_Vec lastSectionAxis = new gp_Vec(0.0 ,1.0 , 0.0);
		TopoDS_Shape lastWingSection = new BRepPrimAPI_MakePrism(airfoilFace3, lastSectionAxis, 1, 0).Shape();
		
		List<OCCShape> exportShapes = new ArrayList<>();
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstWingSection));
		//				exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutEdgeTrasl));
		//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutEdge1));
		//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_10));
		// exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_3));
		//exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_8));
		//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_9));
		//exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(wireSection2));
		//exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(newAirfoil));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(secondWingSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(lastWingSection));

		String fileName = "Test06as.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);




	}

	// Length calculation

	public static double calcLength(TopoDS_Edge edge) {
		GProp_GProps prop = new GProp_GProps();
		BRepGProp.LinearProperties(edge,prop);
		return prop.Mass();

	}
}


package it.unina.daf.jpadcadsandbox;

import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import java.util.ArrayList;
import java.util.List;
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
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.gp_Pnt;
import opencascade.gp_Vec;


public class Test06as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test06as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface horizontalTail = aircraft.getHTail();

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



		Double[] airfoilCutZCoords = AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio+0.05);
		Double[] airfoilCutZCoords1 = AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio+0.05 -0.01);

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

		double[] firstFlapPointCut = {airfoilCutPoints.get(0).X(),airfoilCutPoints.get(0).Y(),airfoilCutPoints.get(0).Z()};
		List<OCCEdge> cutAirfoil1 = OCCUtils.splitEdge( OCCUtils.theFactory.newCurve3D(airfoilPoints, false),firstFlapPointCut);
		TopoDS_Edge cutAirfoil_1 = TopoDS.ToEdge(cutAirfoil1.get(0).getShape());
		TopoDS_Edge cutAirfoil_2 = TopoDS.ToEdge(cutAirfoil1.get(1).getShape());
		double[] secondFlapPointCut = {airfoilCutPoints.get(airfoilCutPoints.size()-1).X(),airfoilCutPoints.get(airfoilCutPoints.size()-1).Y(),airfoilCutPoints.get(airfoilCutPoints.size()-1).Z()};
		//		
		//		List<double[]> pointsCut = new ArrayList<>();
		//		pointsCut.add(firstFlapPointCut);
		//		pointsCut.add(secondFlapPointCut);
		//		List<OCCEdge> cutAirfoil = OCCUtils.splitEdgeByPntsList(OCCUtils.theFactory.newCurve3D(airfoilPoints, false),pointsCut);

	//	List<OCCEdge> cutAirfoil2 = OCCUtils.splitEdge( OCCUtils.theFactory.newCurve3D((CADEdge) cutAirfoil_2),secondFlapPointCut);
//		TopoDS_Edge cutAirfoil_3 = TopoDS.ToEdge(cutAirfoil1.get(0).getShape());
//		TopoDS_Edge cutAirfoil_4 = TopoDS.ToEdge(cutAirfoil1.get(1).getShape());


		List<OCCShape> exportShapes = new ArrayList<>();
		//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstWingSection));
		//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutEdge));
		//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutEdge1));
				exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_1));
				exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_2));
		//exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_3));
		//exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutAirfoil_4));


		String fileName = "Test06as.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);




	}


}

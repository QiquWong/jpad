package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
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

public class TestBoolean02mds {

	// Testing OpenCascade boolean operations
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		OCCUtils.initCADShapeFactory();
		
		// Generate a simple wing
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface horizontalTail = aircraft.getHTail();
		
		Airfoil airfoil = horizontalTail.getAirfoilList().get(0);
		double[] airfoilXCoords = airfoil.getXCoords();
		double[] airfoilZCoords = airfoil.getZCoords();
		
		List<double[]> airfoilPoints = new ArrayList<>(); 
		for(int i = 0; i < airfoilXCoords.length; i++) 
			airfoilPoints.add(new double[] {airfoilXCoords[i], 0.0, airfoilZCoords[i]});
		
		OCCUtils.initCADShapeFactory();
		BRepBuilderAPI_MakeWire airfoilWire = new BRepBuilderAPI_MakeWire();
		airfoilWire.Add(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(airfoilPoints, false).edge()).getShape()));
		
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
		
		double wingLength = 5.0;
		gp_Vec wingAxis = new gp_Vec(0.0, wingLength, 0.0);
		TopoDS_Shape wing = new BRepPrimAPI_MakePrism(airfoilFace, wingAxis, 1, 0).Shape();
		
		// Generate an auxiliary solid figure
		Double[] airfoilCutZCoords = AircraftUtils.getThicknessAtX(airfoil, 0.75);
		
		TColgp_Array1OfPnt airfoilCutPoints = new TColgp_Array1OfPnt(1, 6);		
		airfoilCutPoints.SetValue(1, new gp_Pnt(0.75, +1.0, airfoilCutZCoords[1]      ));
		airfoilCutPoints.SetValue(2, new gp_Pnt(0.75, +1.0, airfoilCutZCoords[1] - 0.2));
		airfoilCutPoints.SetValue(3, new gp_Pnt(1.25, +1.0, airfoilCutZCoords[1] - 0.2));
		airfoilCutPoints.SetValue(4, new gp_Pnt(1.25, +1.0, airfoilCutZCoords[0] + 0.2));
		airfoilCutPoints.SetValue(5, new gp_Pnt(0.75, +1.0, airfoilCutZCoords[0] + 0.2));
		airfoilCutPoints.SetValue(6, new gp_Pnt(0.75, +1.0, airfoilCutZCoords[0]      ));
		
		List<gp_Pnt> cutEdgePoints = new ArrayList<>();
		cutEdgePoints.add(airfoilCutPoints.Last());
		cutEdgePoints.add(new gp_Pnt(0.73, +1.0, (airfoilCutZCoords[0] + airfoilCutZCoords[1])/2.0));
		cutEdgePoints.add(airfoilCutPoints.First());
		TopoDS_Edge cutEdge = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(cutEdgePoints, false).edge()).getShape());
		
		BRepBuilderAPI_MakeWire airfoilCutWire = new BRepBuilderAPI_MakeWire();
		for(int i = 1; i <= 5; i++) {
			BRepBuilderAPI_MakeEdge airfoilCutEdge = new BRepBuilderAPI_MakeEdge(
					airfoilCutPoints.Value(i), 
					airfoilCutPoints.Value(i+1)
					);
			airfoilCutWire.Add(airfoilCutEdge.Edge());
		}
		airfoilCutWire.Add(cutEdge);
		
		TopoDS_Face airfoilCutFace = new BRepBuilderAPI_MakeFace(airfoilCutWire.Wire()).Face();
		
		double cutLength = 3.0;
		gp_Vec cutAxis = new gp_Vec(0.0, cutLength, 0.0);
		TopoDS_Shape cutSolid = new BRepPrimAPI_MakePrism(airfoilCutFace, cutAxis, 1, 0).Shape();
		
		// Subtract the second solid from the wing
		TopoDS_Shape finalShape = new BRepAlgoAPI_Cut(wing, cutSolid).Shape();
//		BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut();
//		TopTools_ListOfShape losArguments = new TopTools_ListOfShape();
//		losArguments.Append(wing);
//		TopTools_ListOfShape losTools = new TopTools_ListOfShape();
//		losTools.Append(cutSolid);
//		cutter.SetArguments(losArguments);
//		cutter.SetTools(losTools);
//		cutter.SetNonDestructive(1);
//		cutter.Build();
		
//		System.out.println("Has deleted: " + cutter.HasDeleted());
//		
//		TopTools_ListOfShape losGenerated = cutter.Generated(cutSolid);
//		System.out.println("Generated shapes: " + losGenerated.Size());
	
		// Export shapes to CAD file
		List<OCCShape> exportShapes = new ArrayList<>();
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(wing));
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutSolid));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(finalShape));
		
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutter.Shape() ));
		
		String fileName = "testBoolean02mds.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}

}

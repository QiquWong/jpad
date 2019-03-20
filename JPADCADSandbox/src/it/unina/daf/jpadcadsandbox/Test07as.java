package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.TestBoolean03mds.SideSelector;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.Geom_Curve;
import opencascade.Precision;
import opencascade.ShapeAnalysis_Curve;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Builder;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;


public class Test07as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test06as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface horizontalTail = aircraft.getHTail();

		// Wing and flap data

		double wingSemiSpan = horizontalTail.getSemiSpan().doubleValue( horizontalTail.getSemiSpan().getUnit() );
		System.out.println(wingSemiSpan + " m");
		double etaInnerSection = 0.25;
		double etaOuterSection = 0.75;
		double flapChordRatio = 0.25;
		double flapGap = 0.05;
		double flapLateralGap = 0.025;

		System.out.println("Flap data: ");
		System.out.println("Inner section: " + etaInnerSection * wingSemiSpan + " m");
		System.out.println("Outer section: "+ etaOuterSection * wingSemiSpan + " m");
		System.out.println("Flap chord ratio: "+ flapChordRatio);
		System.out.println("Flap gap: " + flapGap + "m");
		System.out.println("Flap lateral gap: " + flapLateralGap + " m");

		// Setting Airfoil coordinates 

		Airfoil airfoil = horizontalTail.getAirfoilList().get(0);
		double[] airfoilXCoords = airfoil.getXCoords();
		double[] airfoilZCoords = airfoil.getZCoords();

		List<double[]> airfoilPoints = new ArrayList<>(); 
		for(int i = 0; i < airfoilXCoords.length; i++) 
			airfoilPoints.add(new double[] {airfoilXCoords[i], 0.0, airfoilZCoords[i]});

		// Creation of first wing section

		OCCUtils.initCADShapeFactory();
		BRepBuilderAPI_MakeWire firstSectionWire = new BRepBuilderAPI_MakeWire();
		firstSectionWire.Add(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(airfoilPoints, false).edge()).getShape()));

		if((airfoilPoints.get(0)[2] - airfoilPoints.get(airfoilPoints.size()-1)[2]) > 1e-5) {

			firstSectionWire.Add(closingAirfoilTE(airfoilPoints));

		}

		TopoDS_Face firstSectionFace = new BRepBuilderAPI_MakeFace(firstSectionWire.Wire()).Face();
		gp_Vec firstSectionWingAxis = new gp_Vec(0.0, etaInnerSection * wingSemiSpan, 0.0);
		System.out.println("Building first wing section ...");
		TopoDS_Shape firstWingSection = new BRepPrimAPI_MakePrism(firstSectionFace, firstSectionWingAxis, 1, 0).Shape();
		
		// Creation of flap leading edge and second wing section trailing edge
		// Flap leading edge 
		Double[] airfoilCutZCoords  = AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio + 0.05);
		
		List<gp_Pnt> flapLEPoints = new ArrayList<>();
		flapLEPoints.add(new gp_Pnt(1 - flapChordRatio + 0.05, etaInnerSection * wingSemiSpan, airfoilCutZCoords[0]));
		flapLEPoints.add(new gp_Pnt(1 - flapChordRatio, 
				etaInnerSection * wingSemiSpan, 
				Math.abs((AircraftUtils.getThicknessAtX(airfoil, 1-flapChordRatio)[1] + AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio)[0])) / 2));
		flapLEPoints.add(new gp_Pnt(1 - flapChordRatio + 0.05, etaInnerSection * wingSemiSpan, airfoilCutZCoords[1]));
		// Prova TANGENTI per BSpline
		
		Geom_Curve airfoilGeomCrv = BRep_Tool.Curve(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(airfoilPoints, false).edge()).getShape()), new double[1], new double[1]);
		gp_Vec firstTan = new gp_Vec();
		gp_Pnt firstPoint = new gp_Pnt();
		gp_Pnt proj = new gp_Pnt();
		double[] d = new double[1];
		long l = 0;
		ShapeAnalysis_Curve shapeAn = new ShapeAnalysis_Curve();
		shapeAn.Project(airfoilGeomCrv,new gp_Pnt(1 - flapChordRatio + 0.05, etaInnerSection * wingSemiSpan, airfoilCutZCoords[0]), Precision.Confusion(), proj, d,l);
			
		System.out.println("Parametro : " + Arrays.toString(d));
		airfoilGeomCrv.D1(d[0],firstPoint, firstTan);
		System.out.println("Coordinate : " + firstTan.Coord(1) + "  " + firstTan.Coord(2) + "  " + firstTan.Coord(3));
		
		gp_Vec secTan = new gp_Vec();
		gp_Pnt secPoint = new gp_Pnt();
		gp_Pnt proj2 = new gp_Pnt();
		double[] d2 = new double[1];
		ShapeAnalysis_Curve shapeAn2 = new ShapeAnalysis_Curve();
		shapeAn2.Project(airfoilGeomCrv,new gp_Pnt(1 - flapChordRatio + 0.05, etaInnerSection * wingSemiSpan, airfoilCutZCoords[1]), Precision.Confusion(), proj2, d2,l);
		airfoilGeomCrv.D1(d2[0],secPoint, secTan);
		List<double[]> flapPoints = new ArrayList<>();
		flapPoints.add(new double[]{flapLEPoints.get(0).Coord(1),flapLEPoints.get(0).Coord(2),flapLEPoints.get(0).Coord(3)});
		flapPoints.add(new double[]{flapLEPoints.get(1).Coord(1),flapLEPoints.get(1).Coord(2),flapLEPoints.get(1).Coord(3)});
		flapPoints.add(new double[]{flapLEPoints.get(2).Coord(1),flapLEPoints.get(2).Coord(2),flapLEPoints.get(2).Coord(3)});
		
		TopoDS_Edge flapLEEdgeNew = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(flapPoints, 
				false, 
				new double[] {firstTan.Coord(1), firstTan.Coord(2), firstTan.Coord(3)}, 
				new double[] {secTan.Coord(1), secTan.Coord(2), secTan.Coord(3)},
				false
				).edge()).getShape());
//		
//		// fine prova tang
		TopoDS_Edge flapLEEdge = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(flapLEPoints, false).edge()).getShape());
		
		// New airfoil trailing edge

		Double[] airfoilCutZCoords1 = AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio + 0.05 - flapGap);
		
		List<gp_Pnt> airfoilTEPoints = new ArrayList<>();
		airfoilTEPoints.add(new gp_Pnt(1 - flapChordRatio + 0.05 - flapGap, etaInnerSection * wingSemiSpan, airfoilCutZCoords1[0]));
		airfoilTEPoints.add(new gp_Pnt(1 - flapChordRatio - flapGap, 
				etaInnerSection * wingSemiSpan, 
				Math.abs((AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio - flapGap)[1]+AircraftUtils.getThicknessAtX(airfoil, 1 - flapChordRatio - flapGap)[0])) / 2));
		airfoilTEPoints.add(new gp_Pnt(1 - flapChordRatio + 0.05 - flapGap, etaInnerSection * wingSemiSpan, airfoilCutZCoords1[1]));

		TopoDS_Edge newAirfoilTE = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(airfoilTEPoints, false).edge()).getShape());
		
		// Cutting operations on edges

		double[] firstPointCut = { airfoilTEPoints.get(0).X(), airfoilTEPoints.get(0).Y(), airfoilTEPoints.get(0).Z() };
		List<OCCEdge> firstCutsAirfoilEdges = OCCUtils.splitEdge( 
				OCCUtils.theFactory.newCurve3D(airfoilPoints, false), 
				firstPointCut
				);
		TopoDS_Edge cutAirfoil1 = getLongestEdge(firstCutsAirfoilEdges);
		
		double[] secondPointCut = { airfoilTEPoints.get(airfoilTEPoints.size() - 1).X(), 
				airfoilTEPoints.get(airfoilTEPoints.size() - 1).Y(), 
				airfoilTEPoints.get(airfoilTEPoints.size() - 1).Z() };
		List<OCCEdge> secondCutsAirfoilEdges = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(cutAirfoil1)),
				secondPointCut
				);
		TopoDS_Edge finalCutAirfoil = getLongestEdge(secondCutsAirfoilEdges);
		
		double[] thirdPointCut = { flapLEPoints.get(flapLEPoints.size() - 1).X(), flapLEPoints.get(flapLEPoints.size() - 1).Y(), flapLEPoints.get(flapLEPoints.size() - 1).Z() };
		List<OCCEdge> thirdCutsAirfoilEdges = OCCUtils.splitEdge(OCCUtils.theFactory.newCurve3D(airfoilPoints, false), 
				thirdPointCut
				);			
		TopoDS_Edge cutFlap1 = getShortestEdge(thirdCutsAirfoilEdges);
		
		double[] fourthPointCut = { flapLEPoints.get(0).X(), flapLEPoints.get(0).Y(), flapLEPoints.get(0).Z() };
		List<OCCEdge> fourthCutsAirfoilEdges = OCCUtils.splitEdge(OCCUtils.theFactory.newCurve3D(airfoilPoints, false), 
				fourthPointCut
				);		
		TopoDS_Edge cutFlap2 = getShortestEdge(fourthCutsAirfoilEdges);	
		
		// Wing 2nd section

		gp_Trsf trasl = new gp_Trsf();
		trasl.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, etaInnerSection * wingSemiSpan, 0));
		TopoDS_Edge secondSectionAirfoil = TopoDS.ToEdge(new BRepBuilderAPI_Transform(finalCutAirfoil, trasl).Shape());	
		TopoDS_Wire secondSectionWire = new BRepBuilderAPI_MakeWire(secondSectionAirfoil,newAirfoilTE).Wire();
		TopoDS_Face secondSectionFace = new BRepBuilderAPI_MakeFace(secondSectionWire).Face();
		gp_Vec secondSectionWingAxis = new gp_Vec(0.0, (etaOuterSection * wingSemiSpan) - (etaInnerSection * wingSemiSpan), 0.0);
		System.out.println("Building second wing section ...");
		TopoDS_Shape secondWingSection = new BRepPrimAPI_MakePrism(secondSectionFace, secondSectionWingAxis, 1, 0).Shape();
		
		// Flap

		gp_Trsf flapTrasl = new gp_Trsf();
		flapTrasl.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, etaInnerSection * wingSemiSpan + flapLateralGap, 0));
		gp_Trsf flapTrasl2 = new gp_Trsf();
		flapTrasl2.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, flapLateralGap, 0));
		TopoDS_Edge finalFlap1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutFlap1, flapTrasl).Shape());
		TopoDS_Edge finalFlap2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutFlap2, flapTrasl).Shape());
		TopoDS_Edge finalFlapLEEdge = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapLEEdgeNew, flapTrasl2).Shape());

		BRepBuilderAPI_MakeWire flapWire = new BRepBuilderAPI_MakeWire();
		flapWire.Add(finalFlap1);
		flapWire.Add(finalFlapLEEdge);
		flapWire.Add(finalFlap2);

		if((airfoilPoints.get(0)[2] - airfoilPoints.get(airfoilPoints.size()-1)[2]) > 1e-5) {

			flapWire.Add(TopoDS.ToEdge(new BRepBuilderAPI_Transform(closingAirfoilTE(airfoilPoints), flapTrasl).Shape()));

		}

		TopoDS_Face flapFace = new BRepBuilderAPI_MakeFace(flapWire.Wire()).Face();
		gp_Vec flapAxis = new gp_Vec(0.0, etaOuterSection * wingSemiSpan - (flapLateralGap * 2) - etaInnerSection * wingSemiSpan, 0.0);
		System.out.println("Building flap ...");
		TopoDS_Shape flapSection = new BRepPrimAPI_MakePrism(flapFace, flapAxis, 1, 0).Shape();

		// Last wing section

		gp_Trsf trasl2 = new gp_Trsf();
		trasl2.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, etaOuterSection * wingSemiSpan, 0));
		TopoDS_Face thirdSectionFace = TopoDS.ToFace(new BRepBuilderAPI_Transform(firstSectionFace,trasl2).Shape());
		System.out.println("Building last wing section ...");
		gp_Vec thirdSectionAxis = new gp_Vec(0.0, wingSemiSpan - etaOuterSection * wingSemiSpan, 0.0);
		TopoDS_Shape thirdWingSection = new BRepPrimAPI_MakePrism(thirdSectionFace, thirdSectionAxis, 1, 0).Shape();
	
		BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
		sewing.Add(firstWingSection);
		sewing.Add(secondWingSection);
		sewing.Add(thirdWingSection);
		sewing.Perform();
		
		TopoDS_Shape finalShape = sewing.SewedShape();
		
		TopExp_Explorer exp = new TopExp_Explorer(finalShape, TopAbs_ShapeEnum.TopAbs_SOLID);
		int counter = 1;
		while(exp.More() > 0) {
			counter = counter + 1;
			exp.Next();
		}
		System.out.println("Number of shapes in finalShape: " + counter);
		
		List<OCCShape> exportShapes = new ArrayList<>();
	//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstWingSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(secondWingSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapSection));
	//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(thirdWingSection));
	//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(finalShape));
	//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapLEEdgeNew));
	//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapLEEdge));
	//	exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstSectionWire.Wire()));
		
		String fileName = "Test07as.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);

	}


	// Edge length calculation
	public static double calcLength(TopoDS_Edge edge) {
		GProp_GProps prop = new GProp_GProps();
		BRepGProp.LinearProperties(edge,prop);
		return prop.Mass();

	}

	// Get shortest Edge from a list
	public static TopoDS_Edge getShortestEdge(List<OCCEdge> edgeList) {
		int i = 0;
		double refLength = 10^6;
		for (int j = 0; j < edgeList.size(); j++) {
			TopoDS_Edge edge = TopoDS.ToEdge(edgeList.get(j).getShape());
			double length = calcLength(edge);
			if(length < refLength) {
				refLength = length;
				i = j;
			}
		}
		return TopoDS.ToEdge(edgeList.get(i).getShape());

	}
	
	// Get longest Edge from a list
	public static TopoDS_Edge getLongestEdge(List<OCCEdge> edgeList) {
		int i = 0;
		double refLength = 0;
		for (int j = 0; j < edgeList.size(); j++) {
			TopoDS_Edge edge = TopoDS.ToEdge(edgeList.get(j).getShape());
			double length = calcLength(edge);
			if(length > refLength) {
				refLength = length;
				i = j;
			}
		}
		return TopoDS.ToEdge(edgeList.get(i).getShape());
	}

	// Add trailing edge on opened airfoil
	public static TopoDS_Edge closingAirfoilTE(List<double[]> airfoilCoord) {

		TopoDS_Edge edge = new TopoDS_Edge();

		edge = new BRepBuilderAPI_MakeEdge(
				new gp_Pnt(
						airfoilCoord.get(airfoilCoord.size()-1)[0],
						airfoilCoord.get(airfoilCoord.size()-1)[1],
						airfoilCoord.get(airfoilCoord.size()-1)[2]
						), 
				new gp_Pnt(
						airfoilCoord.get(0)[0],
						airfoilCoord.get(0)[1],
						airfoilCoord.get(0)[2]
						)).Edge();	

		return edge;
	}
	
	public static double[] getParamsIntersectionPntsOnAirfoil(
			Geom_Curve airfoil, 
			Geom_Curve chord,
			double chordLength,
			double flapChord, 		
			double[] teCoords, 
			ComponentEnum lsType,
			SideSelector side
			) {
		
		double[] params = new double[2];
		
		gp_Dir lsAxis = lsType.equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Dir(0, 0, 1) : new gp_Dir(0, 1, 0);			
		Geom_Curve circle = BRep_Tool.Curve(
				new BRepBuilderAPI_MakeEdge(
						new gp_Circ(
								new gp_Ax2(
										new gp_Pnt(
												teCoords[0],
												teCoords[1],
												teCoords[2]),
										lsAxis),
								flapChord)).Edge(), 
				new double[1], 
				new double[1]
				);
				
		double[] chorPar = getParamIntersectionPnts(chord, circle);
		if(chorPar != null) {
			gp_Vec chorDir = new gp_Vec();
			chord.D1(chorPar[0], new gp_Pnt(), chorDir);
			
			List<gp_Vec> normals = new ArrayList<>();
			switch(side) {			
			case UPPER_SIDE:
				normals.add(new gp_Vec(lsAxis).Crossed(chorDir).Multiplied(chordLength));
				break;
			case LOWER_SIDE:
				normals.add(chorDir.Crossed(new gp_Vec(lsAxis)).Multiplied(chordLength));
				break;
			case BOTH_SIDES:
				normals.add(new gp_Vec(lsAxis).Crossed(chorDir).Multiplied(chordLength)); // upper point first
				normals.add(chorDir.Crossed(new gp_Vec(lsAxis)).Multiplied(chordLength)); // then the lower one
				break;
			}
			
			for(int i = 0; i < normals.size(); i++) {
				Geom_Curve interceptor = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
						convertGpPntToDoubleArray(chord.Value(chorPar[0])), 
						convertGpPntToDoubleArray(new gp_Pnt(normals.get(i).Added(new gp_Vec(chord.Value(chorPar[0]).Coord())).XYZ()))
						)).getAdaptorCurve().Curve();
				
				double[] airfPar = getParamIntersectionPnts(airfoil, interceptor);
				if(airfPar != null) {
					params[i] = airfPar[0];
				} else 
					return null;
			}
		} 
		return params;
	}
	
	public static double[] getParamIntersectionPnts(Geom_Curve curve1, Geom_Curve curve2) {
		GeomAPI_ExtremaCurveCurve extrema = new GeomAPI_ExtremaCurveCurve(curve1, curve2);
		int nExtrema = extrema.NbExtrema();
		if(nExtrema == 1) {
			gp_Pnt p1 = new gp_Pnt();
			gp_Pnt p2 = new gp_Pnt();
			double[] par = new double[] {0};
			extrema.Points(1, p1, p2);		
			if(p1.IsEqual(p2, 1e-5) == 0) {
				System.out.println("Warning: error occurred during intersections calculation...");
				return null;
			}			
			extrema.Parameters(1, par, new double[] {0});
			return par;
		} else {
			System.out.println("Warning: error occurred during intersections calculation...");
			return null;
		}
	}
	
	public static double[] convertGpPntToDoubleArray(gp_Pnt gpPnt) {
		return new double[] {gpPnt.X(), gpPnt.Y(), gpPnt.Z()};
	}

}


package it.unina.daf.jpadcadsandbox;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.TestBoolean03mds.SideSelector;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.Geom_Curve;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test11as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test11as Non-symmetric procedure");
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
		double flapLateralGap = 0.025;
		// Single slotted non sym flap
//		double cFlap = 0.34; // flap chord ratio
//		double cLeap = 0.176; // c_leap chord ratio
//		double k1 = 0.48; // gap factor
//		double k2 = 0.1; //0.3; // airfoil trailing edge factor
//		double k3 = 0.115; // 2.3; // flap leading edge factor
//		double k4 = 0.02; // airfoil 
//		double k5 = 0.38;//0.3;
		
		double cFlap = 0.30; // flap chord ratio
		double cLeap = 0.12; // c_leap chord ratio
		double k1 = 0.15; // gap factor
		double k2 = 0.4; //0.3; // airfoil trailing edge factor
		double k3 = 0.05; // 2.3; // flap leading edge factor
		double k4 = 0.01; // airfoil 
		double k5 = 0.5;//0.3;
		
		// Fowler Flap
//		double cFlap = 0.34; // flap chord ratio
//		double cLeap = 0.05; // c_leap chord ratio
//		double k1 = 0.5; // gap factor
//		double k2 = 0.04; //0.3; // airfoil trailing edge factor
//		double k3 = 0.1; // 2.3; // flap leading edge factor
//		double k4 = 0.02; // airfoil 
//		double k5 = 0.05;//0.3;
		// Upper gap and delta for point P7
		double cGap = k1 * cLeap; // flap gap on upper side 
		double deltaCGap2 = k4 * cGap; // airfoil TE for point P7
		// Lower gap for point P4
		double deltaCGap3 = k3 * cFlap; // k3 * c_gap; // flap lower gap definition, point P4
		// P6-P5 factor
		double deltaCGap1 = k2 * deltaCGap3;//k2 * c_gap; // flap gap on lower side for point P3 and P6
		// P8 factor
		double deltaCGap4 = k5 * deltaCGap3; // flap leading edge point P8

		// Setting Airfoil coordinates 

		Airfoil airfoil = horizontalTail.getAirfoilList().get(0);
		double[] airfoilXCoords = airfoil.getXCoords();
		double[] airfoilZCoords = airfoil.getZCoords();

		List<double[]> airfoilPoints = new ArrayList<>(); 
		for(int i = 0; i < airfoilXCoords.length; i++) 
			airfoilPoints.add(new double[] {airfoilXCoords[i], 0.0, airfoilZCoords[i]});

		// Creation of non-symmetric flap

		Airfoil airfoilCoords = horizontalTail.getAirfoilList().get(0);
		List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
				horizontalTail.getYBreakPoints().get(0).doubleValue(SI.METER), 
				airfoilCoords, 
				horizontalTail
				);
		CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
		CADEdge edgeAirfoil = cadCurveAirfoil.edge();

		CADGeomCurve3D chord = getChordSegmentAtYActual(0.0, horizontalTail);
		double chordLength = chord.length();
		OCCEdge chord_edge = (OCCEdge) chord.edge();
		OCCEdge edgeAirfoil_1 = (OCCEdge) edgeAirfoil;

		Geom_Curve airfoil_GeomCurve = BRep_Tool.Curve(edgeAirfoil_1.getShape(), new double[1], new double[1]);
		Geom_Curve chord_GeomCurve = BRep_Tool.Curve(chord_edge.getShape(), new double[1], new double[1]);

		// Creation of point P1 and P2
		// P1
		double cLeapPar = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				cLeap * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.UPPER_SIDE
				)[0];

		gp_Pnt P1 = new gp_Pnt(airfoil_GeomCurve.Value(cLeapPar).X(),
				airfoil_GeomCurve.Value(cLeapPar).Y(),
				airfoil_GeomCurve.Value(cLeapPar).Z());
		System.out.println("Point P1 coordinates : " + P1.X() + " " + P1.Y() + " " + P1.Z());

		// P2
		double cFlapParP2 = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(cLeap + cGap) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.UPPER_SIDE
				)[0];

		gp_Pnt P2 = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParP2).X(),
				airfoil_GeomCurve.Value(cFlapParP2).Y(),
				airfoil_GeomCurve.Value(cFlapParP2).Z());
		System.out.println("Point P2 coordinates : " + P2.X() + " " + P2.Y() + " " + P2.Z());

		
		// Creation of arc of circle of radius cf

		double cFlapPar = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				cFlap * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.UPPER_SIDE
				)[0];

		gp_Pnt A = new gp_Pnt(airfoil_GeomCurve.Value(cFlapPar).X(),
				airfoil_GeomCurve.Value(cFlapPar).Y(),
				airfoil_GeomCurve.Value(cFlapPar).Z());
		
		double cFlapPar2 = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				cFlap * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];

		gp_Pnt B = new gp_Pnt(airfoil_GeomCurve.Value(cFlapPar2).X(),
				airfoil_GeomCurve.Value(cFlapPar2).Y(),
				airfoil_GeomCurve.Value(cFlapPar2).Z());
		gp_Pnt P3 = B;
		System.out.println("Point P3 coordinates : " + P3.X() + " " + P3.Y() + " " + P3.Z());

		
		// Tangent point P2 and normal point P3
		gp_Vec zyDir = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
				new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
		gp_Vec yzDir = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
				new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);
		
		gp_Vec tangPntP2 = new gp_Vec();
		gp_Pnt PntP2= new gp_Pnt();
		airfoil_GeomCurve.D1(cFlapParP2, PntP2, tangPntP2);
		tangPntP2.Normalize();
		tangPntP2 = tangPntP2.Reversed();
		gp_Vec normPntP2 = tangPntP2.Crossed(zyDir).Normalized();
		
		gp_Vec tangPntP3 = new gp_Vec();
		gp_Pnt pntP3 = new gp_Pnt();
		airfoil_GeomCurve.D1(cFlapPar2, pntP3, tangPntP3);
		tangPntP3.Normalize();
		gp_Vec normPntP3 = tangPntP3.Crossed(zyDir).Normalized();
		
		// Curve TE airfoil
		
		List<double[]> TEPoints = new ArrayList<>();
		TEPoints.add(new double[]{P2.Coord(1),P2.Coord(2),P2.Coord(3)});
		TEPoints.add(new double[]{P3.Coord(1),P3.Coord(2),P3.Coord(3)});
		
		CADEdge airfoilTE = OCCUtils.theFactory.newCurve3D(TEPoints,
				false, 
				new double[] {-tangPntP2.X(), -tangPntP2.Y(), -tangPntP2.Z()}, 
				new double[] {-normPntP3.X(), -normPntP3.Y(), -normPntP3.Z()},
				false).edge();
		
		// Creation of point P5 and P6
		
		double cFlapParP5 = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(cFlap + deltaCGap1) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];
		
		gp_Pnt P5 = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParP5).X(),
				airfoil_GeomCurve.Value(cFlapParP5).Y(),
				airfoil_GeomCurve.Value(cFlapParP5).Z());
		System.out.println("Point P5 coordinates : " + P5.X() + " " + P5.Y() + " " + P5.Z());


		gp_Vec tangPntP5 = new gp_Vec();
		gp_Pnt PntP5 = new gp_Pnt();
		airfoil_GeomCurve.D1(cFlapParP5, PntP5, tangPntP5);
		tangPntP5.Normalize();
		
		gp_Pnt P6 = new gp_Pnt(normPntP3.Scaled(deltaCGap1 * chordLength).Added(new gp_Vec(P3.Coord())).XYZ());
		System.out.println("Point P6 coordinates : " + P6.X() + " " + P6.Y() + " " + P6.Z());

		
		// Creation of lower LE (P5-P6)
		
		List<double[]> lowerTEPoints = new ArrayList<>();
		lowerTEPoints.add(new double[]{P5.Coord(1),P5.Coord(2),P5.Coord(3)});
		lowerTEPoints.add(new double[]{P6.Coord(1),P6.Coord(2),P6.Coord(3)});
		
		CADEdge lowerAirfoilTE = OCCUtils.theFactory.newCurve3D(lowerTEPoints,
				false, 
				new double[] {tangPntP5.X(), tangPntP5.Y(), tangPntP5.Z()}, 
				new double[] {normPntP3.X(), normPntP3.Y(), normPntP3.Z()},
				false).edge();
		
		// Creation of point P7
		
		gp_Pnt P7 = new gp_Pnt(normPntP2.Scaled(-deltaCGap2 * chordLength).Added(new gp_Vec(P2.Coord())).XYZ());
		System.out.println("Point P7 coordinates : " + P7.X() + " " + P7.Y() + " " + P7.Z());

		
		// Creation of upper LE (P2-P7)
		
		List<double[]> upperTEPoints = new ArrayList<>();
		upperTEPoints.add(new double[]{P2.Coord(1),P2.Coord(2),P2.Coord(3)});
		upperTEPoints.add(new double[]{P7.Coord(1),P7.Coord(2),P7.Coord(3)});
		
		CADEdge upperAirfoilTE = OCCUtils.theFactory.newCurve3D(upperTEPoints,
				false, 
				new double[] {-normPntP2.X(), -normPntP2.Y(), -normPntP2.Z()}, 
				new double[] {-tangPntP2.X(), -tangPntP2.Y(), -tangPntP2.Z()},
				false).edge();
		
		// Creation of middle LE (P6-P7)
		
		List<double[]> middleTEPoints = new ArrayList<>();
		middleTEPoints.add(new double[]{P6.Coord(1),P6.Coord(2),P6.Coord(3)});
		middleTEPoints.add(new double[]{P7.Coord(1),P7.Coord(2),P7.Coord(3)});
		
		CADEdge middleAirfoilTE = OCCUtils.theFactory.newCurve3D(middleTEPoints,
				false, 
				new double[] {normPntP3.X(), normPntP3.Y(), normPntP3.Z()}, 
				new double[] {tangPntP2.X(), tangPntP2.Y(), tangPntP2.Z()},
				false).edge();
		
		// Splitting airfoil in point P2 and P5
		
		double[] pntP2 = new double[] {P2.X(), P2.Y(), P2.Z()};
		double[] pntP5 = new double[] {P5.X(), P5.Y(), P5.Z()};
		CADGeomCurve3D airfoil_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntP2).get(0));
		CADGeomCurve3D airfoil_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntP2).get(1));
		CADEdge edge_airfoil_1 = airfoil_1.edge();
		CADEdge edge_airfoil_2 = airfoil_2.edge();
		List<OCCEdge> airfoil_edges = new ArrayList<>();
		airfoil_edges.add((OCCEdge) edge_airfoil_1);
		airfoil_edges.add((OCCEdge) edge_airfoil_2);

		TopoDS_Edge airfoilFirstCut = getLongestEdge(airfoil_edges);

		List<OCCEdge> splitAirfoil = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut)),
				pntP5
				);
		TopoDS_Edge finalAirfoilCut = getLongestEdge(splitAirfoil);
		
		// Creation of Flap leading edge
		
		// Creation of point P4 and tangent vector
		
		double cFlapParP4 = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(cFlap - deltaCGap3) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];

		gp_Pnt P4 = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParP4).X(),
				airfoil_GeomCurve.Value(cFlapParP4).Y(),
				airfoil_GeomCurve.Value(cFlapParP4).Z());
		System.out.println("Point P4 coordinates : " + P4.X() + " " + P4.Y() + " " + P4.Z());

		
		gp_Vec tangPntP4 = new gp_Vec();
		gp_Pnt PntP4 = new gp_Pnt();
		airfoil_GeomCurve.D1(cFlapParP4, PntP4, tangPntP4);
		tangPntP4.Normalize();
		
		// Creation of point P8 and normal vector
		
		double cFlapParP8 = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(cFlap - deltaCGap3 + deltaCGap4) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];

		gp_Pnt P8 = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParP8).X(), 
				airfoil_GeomCurve.Value(cFlapParP8).Y(),
				airfoil_GeomCurve.Value(cFlapParP8).Z());
		System.out.println("Point P8 coordinates : " + P8.X() + " " + P8.Y() + " " + P8.Z());

		
		gp_Vec tangPntP8 = new gp_Vec();
		gp_Pnt PntP8 = new gp_Pnt();
		airfoil_GeomCurve.D1(cFlapParP8, PntP8, tangPntP8);
		tangPntP8.Normalize();
		gp_Vec normPntP8 = tangPntP8.Crossed(zyDir).Normalized();
		
		// Creation of point P9
		
		gp_Pnt P9 = new gp_Pnt(normPntP8.Scaled(deltaCGap4 * chordLength).Added(new gp_Vec(P8.Coord())).XYZ());
		System.out.println("Point P9 coordinates : " + P9.X() + " " + P9.Y() + " " + P9.Z());

		
		// Creation of P1 tangent vector
		
		gp_Vec tangPntP1 = new gp_Vec();
		gp_Pnt PntP1= new gp_Pnt();
		airfoil_GeomCurve.D1(cLeapPar, PntP1, tangPntP1);
		tangPntP1.Normalize();
		tangPntP1 = tangPntP1.Reversed();
		
		// Rotation of P1 tangent
		gp_Dir dirRot = new gp_Dir ( new gp_Vec(0.0, 1.0, 0.0) );
		gp_Ax1 axRot = new gp_Ax1(P1, dirRot);
		tangPntP1.Rotate (axRot, -0.034);

		// Creation of Flap leading edge curve
		
		List<double[]> upperLEPoints = new ArrayList<>();
		upperLEPoints.add(new double[]{P1.Coord(1),P1.Coord(2),P1.Coord(3)});
		upperLEPoints.add(new double[]{P9.Coord(1),P9.Coord(2),P9.Coord(3)});
		
		CADEdge upperFlapLE = OCCUtils.theFactory.newCurve3D(upperLEPoints,
				false, 
				new double[] {-tangPntP1.X(), -tangPntP1.Y(), -tangPntP1.Z()}, 
				new double[] {-normPntP8.X(), -normPntP8.Y(), -normPntP8.Z()},
				false).edge();

		
		List<double[]> lowerLEPoints = new ArrayList<>();
		lowerLEPoints.add(new double[]{P4.Coord(1),P4.Coord(2),P4.Coord(3)});
		lowerLEPoints.add(new double[]{P9.Coord(1),P9.Coord(2),P9.Coord(3)});
		
		CADEdge lowerFlapLE = OCCUtils.theFactory.newCurve3D(lowerLEPoints,
				false, 
				new double[] {-tangPntP4.X(), -tangPntP4.Y(), -tangPntP4.Z()}, 
				new double[] {normPntP8.X(), normPntP8.Y(), normPntP8.Z()},
				false).edge();
		
		// Splitting airfoil in point P1 and P4
		double[] pntP1 = new double[] {P1.X(), P1.Y(), P1.Z()};
		double[] pntP4 = new double[] {P4.X(), P4.Y(), P4.Z()};
		CADGeomCurve3D flapUpper_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntP1).get(0));
		CADGeomCurve3D flapUpper_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntP1).get(1));
		CADEdge edge_flapUpper_1 = flapUpper_1.edge();
		CADEdge edge_flapUpper_2 = flapUpper_2.edge();
		List<OCCEdge> flapUpper_edges = new ArrayList<>();
		flapUpper_edges.add((OCCEdge) edge_flapUpper_1);
		flapUpper_edges.add((OCCEdge) edge_flapUpper_2);

		TopoDS_Edge flapFirstCut = getShortestEdge(flapUpper_edges);

		CADGeomCurve3D flapLower_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntP4).get(0));
		CADGeomCurve3D flapLower_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntP4).get(1));
		CADEdge edge_flapLower_1 = flapLower_1.edge();
		CADEdge edge_flapLower_2 = flapLower_2.edge();
		List<OCCEdge> flapLower_edges = new ArrayList<>();
		flapLower_edges.add((OCCEdge) edge_flapLower_1);
		flapLower_edges.add((OCCEdge) edge_flapLower_2);

		TopoDS_Edge flapSecondCut = getShortestEdge(flapLower_edges);

		TopoDS_Edge flapUpperLE_Edge = TopoDS.ToEdge(((OCCShape) upperFlapLE).getShape());
		TopoDS_Edge flapLowerLE_Edge = TopoDS.ToEdge(((OCCShape) lowerFlapLE).getShape());			
		TopoDS_Edge airfoilUpperTE_Edge = TopoDS.ToEdge(((OCCShape) upperAirfoilTE).getShape());
		TopoDS_Edge airfoilLowerTE_Edge = TopoDS.ToEdge(((OCCShape) lowerAirfoilTE).getShape());
		TopoDS_Edge airfoilMiddleTE_Edge = TopoDS.ToEdge(((OCCShape) middleAirfoilTE).getShape());

		// Wing 1st section

		OCCUtils.initCADShapeFactory();
		BRepBuilderAPI_MakeWire firstSectionWire = new BRepBuilderAPI_MakeWire();
		firstSectionWire.Add(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(ptsAirfoil, false).edge()).getShape()));

		if((ptsAirfoil.get(0)[2] - ptsAirfoil.get(ptsAirfoil.size()-1)[2]) > 1e-5) {

			firstSectionWire.Add(closingAirfoilTE(ptsAirfoil));

		}

		TopoDS_Face firstSectionFace = new BRepBuilderAPI_MakeFace(firstSectionWire.Wire()).Face();
		gp_Vec firstSectionWingAxis = new gp_Vec(0.0, etaInnerSection * wingSemiSpan, 0.0);
		System.out.println("Building first wing section ...");
		TopoDS_Shape firstWingSection = new BRepPrimAPI_MakePrism(firstSectionFace, firstSectionWingAxis, 1, 0).Shape();

		// Wing 2nd section

		gp_Trsf trasl = new gp_Trsf();
		trasl.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, etaInnerSection * wingSemiSpan, 0));
		TopoDS_Edge secondSectionAirfoil = TopoDS.ToEdge(new BRepBuilderAPI_Transform(finalAirfoilCut, trasl).Shape());	
		TopoDS_Edge secondSectionAirfoil1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilUpperTE_Edge, trasl).Shape());
		TopoDS_Edge secondSectionAirfoil2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilLowerTE_Edge, trasl).Shape());	
		TopoDS_Edge secondSectionAirfoil3 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilMiddleTE_Edge, trasl).Shape());	

		BRepBuilderAPI_MakeWire secondSectionWireMaker = new BRepBuilderAPI_MakeWire(secondSectionAirfoil);
		secondSectionWireMaker.Add(secondSectionAirfoil1);
		secondSectionWireMaker.Add(secondSectionAirfoil2);
		secondSectionWireMaker.Add(secondSectionAirfoil3);
		TopoDS_Wire secondSectionWire = secondSectionWireMaker.Wire();
		TopoDS_Face secondSectionFace = new BRepBuilderAPI_MakeFace(secondSectionWire).Face();
		gp_Vec secondSectionWingAxis = new gp_Vec(0.0, (etaOuterSection * wingSemiSpan) - (etaInnerSection * wingSemiSpan), 0.0);
		System.out.println("Building second wing section ...");
		TopoDS_Shape secondWingSection = new BRepPrimAPI_MakePrism(secondSectionFace, secondSectionWingAxis, 1, 0).Shape();
		
		// Flap

		gp_Trsf flapTrasl = new gp_Trsf();
		flapTrasl.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, etaInnerSection * wingSemiSpan + flapLateralGap, 0));
		gp_Trsf flapTrasl2 = new gp_Trsf();
		flapTrasl2.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, flapLateralGap, 0));
		TopoDS_Edge finalFlap1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapFirstCut, flapTrasl).Shape());
		TopoDS_Edge finalFlap2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapSecondCut, flapTrasl).Shape());
		TopoDS_Edge finalFlapLEEdge1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapUpperLE_Edge, flapTrasl).Shape());
		TopoDS_Edge finalFlapLEEdge2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapLowerLE_Edge, flapTrasl).Shape());


		BRepBuilderAPI_MakeWire flapWire = new BRepBuilderAPI_MakeWire();
		flapWire.Add(finalFlap1);
		flapWire.Add(finalFlapLEEdge1);
		flapWire.Add(finalFlapLEEdge2);
		flapWire.Add(finalFlap2);

		if((ptsAirfoil.get(0)[2] - ptsAirfoil.get(ptsAirfoil.size()-1)[2]) > 1e-7) {

			flapWire.Add(TopoDS.ToEdge(new BRepBuilderAPI_Transform(closingAirfoilTE(ptsAirfoil), flapTrasl).Shape()));

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
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(finalAirfoilCut));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapFirstCut));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapSecondCut));
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstWingSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(secondWingSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(thirdWingSection));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapSection));
	
		String fileName = "Test11as.brep";
		if(OCCUtils.write(fileName,(OCCShape) OCCUtils.theFactory.newShape(flapSecondCut),(OCCShape) OCCUtils.theFactory.newShape(flapFirstCut),(OCCShape) OCCUtils.theFactory.newShape(finalAirfoilCut),lowerAirfoilTE,upperAirfoilTE,middleAirfoilTE,upperFlapLE,lowerFlapLE))
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

	public static CADGeomCurve3D getChordSegmentAtYActual(double yStation, LiftingSurface liftingSurface) {

		List<double[]> actualChordPoints = new ArrayList<>();

		double[] baseChordXCoords = new double[] {1, 0};
		double[] baseChordZCoords = new double[] {0, 0};

		double x, y, z;

		double c = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getChordsBreakPoints()), 
				yStation
				);
		double twist = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertToDoublePrimitive(
						liftingSurface.getTwistsBreakPoints().stream()
						.map(t -> t.doubleValue(SI.RADIAN))
						.collect(Collectors.toList())
						),
				yStation
				);
		double xLE = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getXLEBreakPoints()), 
				yStation
				);

		for(int i = 0; i < 2; i++) {

			// scale to actual dimension
			x = baseChordXCoords[i]*c;
			y = 0.0;
			z = baseChordZCoords[i]*c;

			// rotation due to twist
			if(!liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				double r = Math.sqrt(x*x + z*z);
				x = x - r*(1-Math.cos(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
				z = z - r*Math.sin(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN));				
			}

			// actual location
			x = x + xLE + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
			y = yStation;
			z = z + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER)
					+ (yStation*Math.tan(AircraftUtils.getDihedralAtYActual(liftingSurface, yStation).doubleValue(SI.RADIAN)));

			if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				actualChordPoints.add(
						new double[] {
								x,
								-baseChordZCoords[i]*c,
								(yStation + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER))
						});
			} else {
				actualChordPoints.add(new double[] {x, y, z});
			}
		}		

		if(OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();

		CADGeomCurve3D chordCurve = OCCUtils.theFactory.newCurve3D(
				actualChordPoints.get(0), 
				actualChordPoints.get(1)
				);

		return chordCurve;
	}

}

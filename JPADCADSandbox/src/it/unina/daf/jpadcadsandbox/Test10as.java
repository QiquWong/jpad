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
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test10as {

		public static void main(String[] args) {

			System.out.println("Starting JPADCADSandbox Test10as Symmetric procedure");
			System.out.println("Inizializing Factory...");
			OCCUtils.initCADShapeFactory();
			System.out.println("Importing Aircraft...");

			Aircraft aircraft = AircraftUtils.importAircraft(args);
			LiftingSurface horizontalTail = aircraft.getHTail();

			// Wing and flap data

			double wingSemiSpan = horizontalTail.getSemiSpan().doubleValue( horizontalTail.getSemiSpan().getUnit() );
			System.out.println("WingSpan : " + wingSemiSpan + " m ");
			double etaInnerSection = 0.25;
			double etaOuterSection = 0.75;
			double flapLateralGap = 0.025;
			
			double cFlap = 0.25; // flap chord ratio
			double k1 = 0.02; // gap factor
			double k2 = 0.448; // airfoil trailing edge factor
			double k3 = 16.8; // flap leading edge factor
			double cGap = k1 * cFlap; // flap gap 
			double deltaCGap1 = k2 * cGap; // airfoil TE
			double deltaCGap2 = k3 * cGap; // flap LE
			
//			double cFlap = 0.25; // flap chord ratio
//			double k1 = 0.02; // gap factor
//			double k2 = 0.009; // airfoil trailing edge factor
//			double k3 = 0.34; // flap leading edge factor
//			double cGap = k1 * cFlap; // flap gap 
//			double deltaCGap1 = k2 * cFlap; // airfoil TE
//			double deltaCGap2 = k3 * cFlap; // flap LE
						
			// Creation of symmetric flap
			System.out.println("Starting symmetric procedure...");
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
			
			// Creation of point C
			double cFlapParC = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(cFlap + cGap - deltaCGap1) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.UPPER_SIDE
					)[0];
			
			gp_Pnt C = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParC).X(),
					airfoil_GeomCurve.Value(cFlapParC).Y(),
					airfoil_GeomCurve.Value(cFlapParC).Z());
			System.out.println("Point C coordinates : " + C.X() + " " + C.Y() + " " + C.Z());

			// Creation of point D
			double cFlapParD = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(cFlap + cGap - deltaCGap1) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.LOWER_SIDE
					)[0];
			
			gp_Pnt D = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParD).X(),
					airfoil_GeomCurve.Value(cFlapParD).Y(),
					airfoil_GeomCurve.Value(cFlapParD).Z());
			System.out.println("Point D coordinates : " + D.X() + " " + D.Y() + " " + D.Z());

			// Creation of point E
			double cFlapParE = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(cFlap + cGap) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.UPPER_SIDE
					)[0];

			gp_Pnt E = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParE).X(),
					airfoil_GeomCurve.Value(cFlapParE).Y(),
					airfoil_GeomCurve.Value(cFlapParE).Z());
			System.out.println("Point E coordinates : " + E.X() + " " + E.Y() + " " + E.Z());

			// Creation of point F
			double cFlapParF = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(cFlap + cGap) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.LOWER_SIDE
					)[0];

			gp_Pnt F = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParF).X(),
					airfoil_GeomCurve.Value(cFlapParF).Y(),
					airfoil_GeomCurve.Value(cFlapParF).Z());
			System.out.println("Point F coordinates : " + F.X() + " " + F.Y() + " " + F.Z());

			// Splitting airfoil in point E and F
			double[] pntE = new double[] {E.X(), E.Y(), E.Z()};
			double[] pntF = new double[] {F.X(), F.Y(), F.Z()};
			CADGeomCurve3D airfoil_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntE).get(0));
			CADGeomCurve3D airfoil_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntE).get(1));
			CADEdge edge_airfoil_1 = airfoil_1.edge();
			CADEdge edge_airfoil_2 = airfoil_2.edge();
			List<OCCEdge> airfoil_edges = new ArrayList<>();
			airfoil_edges.add((OCCEdge) edge_airfoil_1);
			airfoil_edges.add((OCCEdge) edge_airfoil_2);
			
			TopoDS_Edge airfoilFirstCut = getLongestEdge(airfoil_edges);
			
			List<OCCEdge> splitAirfoil = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut)),
					pntF
					);
			TopoDS_Edge finalAirfoilCut = getLongestEdge(splitAirfoil);
			
			// Get normal vectors in point C and D
			gp_Vec zyDir = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
					new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
			gp_Vec yzDir = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
					new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);
					
			gp_Vec tangPntC = new gp_Vec();
			gp_Pnt PntC = new gp_Pnt();
			airfoil_GeomCurve.D1(cFlapParC, PntC, tangPntC);
			tangPntC.Normalize();
			gp_Vec normPntC = tangPntC.Crossed(zyDir).Normalized();
		
			gp_Vec tangPntD = new gp_Vec();
			gp_Pnt PntD = new gp_Pnt();
			airfoil_GeomCurve.D1(cFlapParD, PntD, tangPntD);
			tangPntD.Normalize();
			gp_Vec normPntD = tangPntD.Crossed(zyDir).Normalized();
			
			// Get tangent vector in point E and F
			
			gp_Vec tangPntE = new gp_Vec();
			gp_Pnt PntE= new gp_Pnt();
			airfoil_GeomCurve.D1(cFlapParE, PntE, tangPntE);
			tangPntE.Normalize();
			tangPntE = tangPntE.Reversed();
			
			gp_Vec tangPntF = new gp_Vec();
			gp_Pnt PntF = new gp_Pnt();
			airfoil_GeomCurve.D1(cFlapParF, PntF, tangPntF);
			tangPntF.Normalize();
			
			// Get point G and H
			
			gp_Pnt G = new gp_Pnt(normPntC.Scaled(deltaCGap1 * chordLength).Added(new gp_Vec(C.Coord())).XYZ());
			gp_Pnt H = new gp_Pnt(normPntD.Scaled(deltaCGap1 * chordLength).Added(new gp_Vec(D.Coord())).XYZ());
			System.out.println("Point G coordinates : " + G.X() + " " + G.Y() + " " + G.Z());
			System.out.println("Point H coordinates : " + H.X() + " " + H.Y() + " " + H.Z());

			// Curves creation
			
			List<double[]> upperTEPoints = new ArrayList<>();
			upperTEPoints.add(new double[]{E.Coord(1),E.Coord(2),E.Coord(3)});
			upperTEPoints.add(new double[]{G.Coord(1),G.Coord(2),G.Coord(3)});
			
			CADEdge airfoilUpperTE = OCCUtils.theFactory.newCurve3D(upperTEPoints,
					false, 
					new double[] {tangPntE.X(), tangPntE.Y(), tangPntE.Z()}, 
					new double[] {normPntC.X(), normPntC.Y(), normPntC.Z()},
					false).edge();
			
			List<double[]> lowerTEPoints = new ArrayList<>();
			lowerTEPoints.add(new double[]{F.Coord(1),F.Coord(2),F.Coord(3)});
			lowerTEPoints.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
			
			CADEdge airfoilLowerTE = OCCUtils.theFactory.newCurve3D(lowerTEPoints,
					false, 
					new double[] {tangPntF.X(), tangPntF.Y(), tangPntF.Z()}, 
					new double[] {normPntD.X(), normPntD.Y(), normPntD.Z()},
					false).edge();
			
			// Creation of point I
			
			gp_Dir lsAxis = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Dir(0, 0, 1) : new gp_Dir(0, 1, 0);			
			Geom_Curve circle = BRep_Tool.Curve(
					new BRepBuilderAPI_MakeEdge(
							new gp_Circ(
									new gp_Ax2(
											new gp_Pnt(
													chord_edge.vertices()[0].pnt()[0],
													chord_edge.vertices()[0].pnt()[1],
													chord_edge.vertices()[0].pnt()[2]),
											lsAxis),
									(cFlap + cGap ) * chordLength)).Edge(), 
					new double[1], 
					new double[1]
					);
			
			double[] chorPar = getParamIntersectionPnts(chord_GeomCurve, circle);
			gp_Vec chorDir = new gp_Vec();
			gp_Pnt I = new gp_Pnt();
			chord_GeomCurve.D1(chorPar[0], I, chorDir);
			System.out.println("Point I coordinates : " + I.X() + " " + I.Y() + " " + I.Z());
			gp_Vec normPntI = chorDir.Crossed(zyDir).Normalized();
			
			// Center curves creation
			
			List<double[]> MiddleUpperTEPoints = new ArrayList<>();
			MiddleUpperTEPoints.add(new double[]{G.Coord(1),G.Coord(2),G.Coord(3)});
			MiddleUpperTEPoints.add(new double[]{I.Coord(1),I.Coord(2),I.Coord(3)});
			
			CADEdge airfoilMiddleUpperTE = OCCUtils.theFactory.newCurve3D(MiddleUpperTEPoints,
					false, 
					new double[] {normPntC.X(), normPntC.Y(), normPntC.Z()}, 
					new double[] {normPntI.X(), normPntI.Y(), normPntI.Z()},
					false).edge();
			
			List<double[]> MiddleLowerTEPoints = new ArrayList<>();
			MiddleLowerTEPoints.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
			MiddleLowerTEPoints.add(new double[]{I.Coord(1),I.Coord(2),I.Coord(3)});
			
			CADEdge airfoilMiddleLowerTE = OCCUtils.theFactory.newCurve3D(MiddleLowerTEPoints,
					false, 
					new double[] {normPntD.X(), normPntD.Y(), normPntD.Z()}, 
					new double[] {-normPntI.X(), -normPntI.Y(), -normPntI.Z()},
					false).edge();
			
			// New flap leading edge creation
			
			// Creation point A and B
			
			double cFlapParA = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					cFlap * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.UPPER_SIDE
					)[0];

			gp_Pnt A = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParA).X(),
					airfoil_GeomCurve.Value(cFlapParA).Y(),
					airfoil_GeomCurve.Value(cFlapParA).Z());
			System.out.println("Point A coordinates : " + A.X() + " " + A.Y() + " " + A.Z());

			double cFlapParB = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					cFlap * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.LOWER_SIDE
					)[0];

			gp_Pnt B = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParB).X(),
					airfoil_GeomCurve.Value(cFlapParB).Y(),
					airfoil_GeomCurve.Value(cFlapParB).Z());
			System.out.println("Point B coordinates : " + B.X() + " " + B.Y() + " " + B.Z());

			// Creation of point L and M
			double cFlapParL = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(cFlap - deltaCGap2) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.UPPER_SIDE
					)[0];

			gp_Pnt L = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParL).X(),
					airfoil_GeomCurve.Value(cFlapParL).Y(),
					airfoil_GeomCurve.Value(cFlapParL).Z());
			System.out.println("Point L coordinates : " + L.X() + " " + L.Y() + " " + L.Z());

			double cFlapParM = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(cFlap - deltaCGap2) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					horizontalTail.getType(), 
					SideSelector.LOWER_SIDE
					)[0];

			gp_Pnt M = new gp_Pnt(airfoil_GeomCurve.Value(cFlapParM).X(),
					airfoil_GeomCurve.Value(cFlapParM).Y(),
					airfoil_GeomCurve.Value(cFlapParM).Z());
			System.out.println("Point M coordinates : " + M.X() + " " + M.Y() + " " + M.Z());

			// Splitting airfoil in point L and M
			double[] pntL = new double[] {L.X(), L.Y(), L.Z()};
			double[] pntM = new double[] {M.X(), M.Y(), M.Z()};
			CADGeomCurve3D flapUpper_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntL).get(0));
			CADGeomCurve3D flapUpper_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntL).get(1));
			CADEdge edge_flapUpper_1 = flapUpper_1.edge();
			CADEdge edge_flapUpper_2 = flapUpper_2.edge();
			List<OCCEdge> flapUpper_edges = new ArrayList<>();
			flapUpper_edges.add((OCCEdge) edge_flapUpper_1);
			flapUpper_edges.add((OCCEdge) edge_flapUpper_2);
			
			TopoDS_Edge flapFirstCut = getShortestEdge(flapUpper_edges);
			
			CADGeomCurve3D flapLower_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntM).get(0));
			CADGeomCurve3D flapLower_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntM).get(1));
			CADEdge edge_flapLower_1 = flapLower_1.edge();
			CADEdge edge_flapLower_2 = flapLower_2.edge();
			List<OCCEdge> flapLower_edges = new ArrayList<>();
			flapLower_edges.add((OCCEdge) edge_flapLower_1);
			flapLower_edges.add((OCCEdge) edge_flapLower_2);
			
			TopoDS_Edge flapSecondCut = getShortestEdge(flapLower_edges);
			
			// Get tangent vector in point L and M
			
			gp_Vec tangPntL = new gp_Vec();
			gp_Pnt PntL = new gp_Pnt();
			airfoil_GeomCurve.D1(cFlapParL, PntL, tangPntL);
			tangPntL.Normalize();
			tangPntL = tangPntL.Reversed();

			gp_Vec tangPntM = new gp_Vec();
			gp_Pnt PntM = new gp_Pnt();
			airfoil_GeomCurve.D1(cFlapParM, PntM, tangPntM);
			tangPntM.Normalize();
			
			// Creation of point P
			
			Geom_Curve circleFlap = BRep_Tool.Curve(
					new BRepBuilderAPI_MakeEdge(
							new gp_Circ(
									new gp_Ax2(
											new gp_Pnt(
													chord_edge.vertices()[0].pnt()[0],
													chord_edge.vertices()[0].pnt()[1],
													chord_edge.vertices()[0].pnt()[2]),
											lsAxis),
									 cFlap * chordLength)).Edge(), 
					new double[1], 
					new double[1]
					);
			
			double[] chorParFlap = getParamIntersectionPnts(chord_GeomCurve, circleFlap);
			gp_Vec chorDirFlap = new gp_Vec();
			gp_Pnt P = new gp_Pnt();
			chord_GeomCurve.D1(chorParFlap[0], P, chorDirFlap);
			System.out.println("Point P coordinates : " + P.X() + " " + P.Y() + " " + P.Z());
			gp_Vec normPntP= chorDirFlap.Crossed(zyDir).Normalized();
			
			// Flap LE curves creation
			
			List<double[]> upperLEPoints = new ArrayList<>();
			upperLEPoints.add(new double[]{L.Coord(1),L.Coord(2),L.Coord(3)});
			upperLEPoints.add(new double[]{P.Coord(1),P.Coord(2),P.Coord(3)});
			
			CADEdge flapUpperLE = OCCUtils.theFactory.newCurve3D(upperLEPoints,
					false, 
					new double[] {-tangPntL.X(), -tangPntL.Y(), -tangPntL.Z()}, 
					new double[] {normPntP.X(), normPntP.Y(), normPntP.Z()},
					false).edge();
						
			List<double[]> lowerLEPoints = new ArrayList<>();
			lowerLEPoints.add(new double[]{M.Coord(1),M.Coord(2),M.Coord(3)});
			lowerLEPoints.add(new double[]{P.Coord(1),P.Coord(2),P.Coord(3)});
						
			CADEdge flapLowerLE = OCCUtils.theFactory.newCurve3D(lowerLEPoints,
					false, 
					new double[] {-tangPntM.X(), -tangPntM.Y(), -tangPntM.Z()}, 
					new double[] {-normPntP.X(), -normPntP.Y(), -normPntP.Z()},
					false).edge();			
		
			
			TopoDS_Edge flapUpperLE_Edge = TopoDS.ToEdge(((OCCShape) flapUpperLE).getShape());
			TopoDS_Edge flapLowerLE_Edge = TopoDS.ToEdge(((OCCShape) flapLowerLE).getShape());			
			TopoDS_Edge airfoilUpperTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilUpperTE).getShape());
			TopoDS_Edge airfoilLowerTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLowerTE).getShape());
			TopoDS_Edge airfoilMiddleUpperTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilMiddleUpperTE).getShape());
			TopoDS_Edge airfoilMiddleLowerTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilMiddleLowerTE).getShape());

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
			TopoDS_Edge secondSectionAirfoil3 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilMiddleUpperTE_Edge, trasl).Shape());	
			TopoDS_Edge secondSectionAirfoil4 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilMiddleLowerTE_Edge, trasl).Shape());	
			
			BRepBuilderAPI_MakeWire secondSectionWireMaker = new BRepBuilderAPI_MakeWire(secondSectionAirfoil);
			secondSectionWireMaker.Add(secondSectionAirfoil1);
			secondSectionWireMaker.Add(secondSectionAirfoil2);
			secondSectionWireMaker.Add(secondSectionAirfoil3);
			secondSectionWireMaker.Add(secondSectionAirfoil4);
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
			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstWingSection));
			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(secondWingSection));
			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(thirdWingSection));
			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(flapSection));

			String fileName = "Test10as.brep";
			if(OCCUtils.write(fileName,flapLowerLE, flapUpperLE, (OCCShape) OCCUtils.theFactory.newShape(finalAirfoilCut),(OCCShape) OCCUtils.theFactory.newShape(flapFirstCut), (OCCShape) OCCUtils.theFactory.newShape(flapSecondCut), airfoilUpperTE,airfoilLowerTE,airfoilMiddleUpperTE,airfoilMiddleLowerTE))
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




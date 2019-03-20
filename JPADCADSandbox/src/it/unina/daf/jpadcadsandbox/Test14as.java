package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import org.jscience.physics.amount.Amount;
import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.TestBoolean03mds.SideSelector;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.Geom_Curve;
import opencascade.TopExp;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test14as {
	
	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test14as slat procedure");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface horizontalTail = aircraft.getHTail();

		// Wing and flap data

		double wingSemiSpan = horizontalTail.getSemiSpan().doubleValue( horizontalTail.getSemiSpan().getUnit() );
		System.out.println("WingSpan : " + wingSemiSpan + " m ");
		double etaInnerSection = 0.25;
		double etaOuterSection = 0.70;
		double flapLateralGap = 0.025;
		
		double cSlat = 0.15; // 0.17 slat chord ratio
		double k1 = 0.53; // 0.70 lower slat factor
		double k2 = 0.13; // 0.25
		double k3 = 0.09; //
		double k4 = 0.32;
		double k5 = 0.02; //0.08;
		double cSlatLower = k1 * cSlat; // flap gap 
		double cSlatMiddle = k4 * cSlat;
		double deltaSlat1 = k2 * cSlat; // airfoil TE
		double deltaSlat2 = k3 * cSlatLower; // flap LE
		double slatGap = k5 * cSlat;
		
		// Creation of slat
		System.out.println("Starting slat procedure...");
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
		
		// Creation of point A
		double cSlatParA = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(1 - cSlat) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.UPPER_SIDE
				)[0];

		gp_Pnt A = new gp_Pnt(airfoil_GeomCurve.Value(cSlatParA).X(),
				airfoil_GeomCurve.Value(cSlatParA).Y(),
				airfoil_GeomCurve.Value(cSlatParA).Z());
		System.out.println("Point A coordinates : " + A.X() + " " + A.Y() + " " + A.Z());

		// Creation of point B
		double cSlatParB = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(1 - cSlatLower) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];

		gp_Pnt B = new gp_Pnt(airfoil_GeomCurve.Value(cSlatParB).X(),
				airfoil_GeomCurve.Value(cSlatParB).Y(),
				airfoil_GeomCurve.Value(cSlatParB).Z());
		System.out.println("Point B coordinates : " + B.X() + " " + B.Y() + " " + B.Z());
		
		// Splitting airfoil in point A and B
		double[] pntA = new double[] {A.X(), A.Y(), A.Z()};
		double[] pntB = new double[] {B.X(), B.Y(), B.Z()};
		CADGeomCurve3D airfoil_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntA).get(0));
		CADGeomCurve3D airfoil_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntA).get(1));
		CADEdge edge_airfoil_1 = airfoil_1.edge();
		CADEdge edge_airfoil_2 = airfoil_2.edge();
		List<OCCEdge> airfoil_edges = new ArrayList<>();
		airfoil_edges.add((OCCEdge) edge_airfoil_1);
		airfoil_edges.add((OCCEdge) edge_airfoil_2);

		TopoDS_Edge airfoilFirstCut = getShortestEdge(airfoil_edges);

		CADGeomCurve3D airfoil_3 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntB).get(0));
		CADGeomCurve3D airfoil_4 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntB).get(1));
		CADEdge edge_airfoil_3 = airfoil_3.edge();
		CADEdge edge_airfoil_4 = airfoil_4.edge();
		List<OCCEdge> airfoil_edges_2 = new ArrayList<>();
		airfoil_edges_2.add((OCCEdge) edge_airfoil_3);
		airfoil_edges_2.add((OCCEdge) edge_airfoil_4);
		
		TopoDS_Edge airfoilSecondCut = getShortestEdge(airfoil_edges_2);

		// Get tangent vectors in A and B
		
		gp_Vec tangPntA = new gp_Vec();
		gp_Pnt PntA = new gp_Pnt();
		airfoil_GeomCurve.D1(cSlatParA, PntA, tangPntA);
		tangPntA.Normalize();
		tangPntA = tangPntA.Reversed();

		gp_Vec tangPntB = new gp_Vec();
		gp_Pnt PntB = new gp_Pnt();
		airfoil_GeomCurve.D1(cSlatParB, PntB, tangPntB);
		tangPntB.Normalize();
		
		// Creation of point C
		gp_Vec zyDir = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
				new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
				gp_Vec yzDir = horizontalTail.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
						new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);


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
								(1 - cSlatMiddle) * chordLength)).Edge(), 
				new double[1], 
				new double[1]
				);

		double[] chorPar = getParamIntersectionPnts(chord_GeomCurve, circle);
		gp_Vec chorDir = new gp_Vec();
		gp_Pnt C = new gp_Pnt();
		chord_GeomCurve.D1(chorPar[0], C, chorDir);
		System.out.println("Point C coordinates : " + C.X() + " " + C.Y() + " " + C.Z());
		gp_Vec normPntC = chorDir.Crossed(zyDir).Normalized();
		

		// Curve LE airfoil

		List<double[]> LEPointsUpper = new ArrayList<>();
		LEPointsUpper.add(new double[]{A.Coord(1),A.Coord(2),A.Coord(3)});
		LEPointsUpper.add(new double[]{C.Coord(1),C.Coord(2),C.Coord(3)});
		
		List<double[]> LEPointsLower = new ArrayList<>();
		LEPointsLower.add(new double[]{C.Coord(1),C.Coord(2),C.Coord(3)});
		LEPointsLower.add(new double[]{B.Coord(1),B.Coord(2),B.Coord(3)});
		

		CADEdge airfoilLEUpper = OCCUtils.theFactory.newCurve3D(LEPointsUpper,
				false, 
				new double[] {-tangPntA.X()*1, -tangPntA.Y()*1, -tangPntA.Z()*1}, 
				new double[] {normPntC.X(), normPntC.Y(), normPntC.Z()},
				false).edge();
		
		CADEdge airfoilLELower = OCCUtils.theFactory.newCurve3D(LEPointsLower,
				false, 
				new double[] {normPntC.X(), normPntC.Y(), normPntC.Z()},
				new double[] {tangPntB.X()*1, tangPntB.Y()*1, tangPntB.Z()*1}, 
				false).edge();
		
		// Slat TE creation
		
		// Point D and E
		double cSlatParD = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(1 - cSlat + deltaSlat1) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.UPPER_SIDE
				)[0];

		gp_Pnt D = new gp_Pnt(airfoil_GeomCurve.Value(cSlatParD).X(),
				airfoil_GeomCurve.Value(cSlatParD).Y(),
				airfoil_GeomCurve.Value(cSlatParD).Z());
		System.out.println("Point D coordinates : " + D.X() + " " + D.Y() + " " + D.Z());
		
		double cSlatParE = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(1 - cSlatLower + deltaSlat2) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];

		gp_Pnt E = new gp_Pnt(airfoil_GeomCurve.Value(cSlatParE).X(),
				airfoil_GeomCurve.Value(cSlatParE).Y(),
				airfoil_GeomCurve.Value(cSlatParE).Z());
		System.out.println("Point E coordinates : " + E.X() + " " + E.Y() + " " + E.Z());
		
		// Splitting airfoil in point D and E
		double[] pntD = new double[] {D.X(), D.Y(), D.Z()};
		double[] pntE = new double[] {E.X(), E.Y(), E.Z()};
		CADGeomCurve3D airfoil_5 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntD).get(0));
		CADGeomCurve3D airfoil_6 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(edgeAirfoil, pntD).get(1));
		CADEdge edge_airfoil_5 = airfoil_5.edge();
		CADEdge edge_airfoil_6 = airfoil_6.edge();
		List<OCCEdge> airfoil_edges_3 = new ArrayList<>();
		airfoil_edges_3.add((OCCEdge) edge_airfoil_5);
		airfoil_edges_3.add((OCCEdge) edge_airfoil_6);

		TopoDS_Edge slatFirstCut = getLongestEdge(airfoil_edges_3);

//		OCCEdge airfoil_7 = OCCUtils.splitEdge(
//				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(slatFirstCut)),
//				pntE
//				).get(0);
//		OCCEdge airfoil_8 = OCCUtils.splitEdge(
//				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(slatFirstCut)),
//				pntE
//				).get(1);
		
		// Slat detail
		// Point G
		double deltaSlat3 = 0.011;
		double cSlatParG = getParamsIntersectionPntsOnAirfoil(
				airfoil_GeomCurve, 
				chord_GeomCurve, 
				chordLength, 
				(1 - cSlatLower + deltaSlat2 + deltaSlat3) * chordLength, 
				chord_edge.vertices()[0].pnt(), 
				horizontalTail.getType(), 
				SideSelector.LOWER_SIDE
				)[0];

		gp_Pnt G = new gp_Pnt(airfoil_GeomCurve.Value(cSlatParG).X(),
				airfoil_GeomCurve.Value(cSlatParG).Y(),
				airfoil_GeomCurve.Value(cSlatParG).Z());
		System.out.println("Point G coordinates : " + G.X() + " " + G.Y() + " " + G.Z());
		double[] pntG = new double[] {G.X(), G.Y(), G.Z()};
		
		OCCEdge airfoil_7 = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(slatFirstCut)),
				pntG
				).get(0);
		OCCEdge airfoil_8 = OCCUtils.splitEdge(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(slatFirstCut)),
				pntG
				).get(1);
		
		List<OCCEdge> airfoil_edges_4 = new ArrayList<>();
		airfoil_edges_4.add(airfoil_7);
		airfoil_edges_4.add(airfoil_8);

		TopoDS_Edge slatFinalCut = getShortestEdge(airfoil_edges_4);
		
		// Get tangent vectors in D and E
		
		gp_Vec tangPntD = new gp_Vec();
		gp_Pnt PntD = new gp_Pnt();
		airfoil_GeomCurve.D1(cSlatParD, PntD, tangPntD);
		tangPntD.Normalize();
		tangPntD = tangPntD.Reversed();

		gp_Vec tangPntE = new gp_Vec();
		gp_Pnt PntE = new gp_Pnt();
		airfoil_GeomCurve.D1(cSlatParE, PntE, tangPntE);
		tangPntE.Normalize();
		gp_Vec normPntE = tangPntE.Crossed(zyDir).Normalized();

		
		// Creation of point F
		
		Geom_Curve circle2 = BRep_Tool.Curve(
				new BRepBuilderAPI_MakeEdge(
						new gp_Circ(
								new gp_Ax2(
										new gp_Pnt(
												chord_edge.vertices()[0].pnt()[0],
												chord_edge.vertices()[0].pnt()[1],
												chord_edge.vertices()[0].pnt()[2]),
										lsAxis),
								(1 - cSlatMiddle + slatGap) * chordLength)).Edge(), 
				new double[1], 
				new double[1]
				);

		double[] chorPar2 = getParamIntersectionPnts(chord_GeomCurve, circle2);
		gp_Vec chorDir2 = new gp_Vec();
		gp_Pnt F = new gp_Pnt();
		chord_GeomCurve.D1(chorPar2[0], F, chorDir2);
		System.out.println("Point F coordinates : " + F.X() + " " + F.Y() + " " + F.Z());
		gp_Vec normPntF = chorDir2.Crossed(zyDir).Normalized();
				
		// Curve TE slat

		List<double[]> TEPointsUpper = new ArrayList<>();
		TEPointsUpper.add(new double[]{D.Coord(1),D.Coord(2),D.Coord(3)});
		TEPointsUpper.add(new double[]{F.Coord(1),F.Coord(2),F.Coord(3)});

		CADEdge slatTEUpper = OCCUtils.theFactory.newCurve3D(TEPointsUpper,
				false, 
				new double[] {-tangPntD.X()*1.0, -tangPntD.Y()*1.0, -tangPntD.Z()*1.0}, 
				new double[] {normPntF.X()*1.0, normPntF.Y()*1.0, normPntF.Z()*1.0},
				false).edge();
		
		List<double[]> TEPointsLower = new ArrayList<>();
		TEPointsLower.add(new double[]{F.Coord(1),F.Coord(2),F.Coord(3)});
		TEPointsLower.add(new double[]{E.Coord(1),E.Coord(2),E.Coord(3)});
		
		CADEdge slatTELower = OCCUtils.theFactory.newCurve3D(TEPointsLower,
				false, 
				new double[] {normPntF.X()*1.0, normPntF.Y()*1.0, normPntF.Z()*1.0}, 
				new double[] {tangPntE.X(), tangPntE.Y(), tangPntE.Z()},
				false).edge();
		
		
		// Point H	

		Geom_Curve slatTELow = BRep_Tool.Curve(((OCCEdge) slatTELower).getShape(), new double[1], new double[1]);
		gp_Pnt p2 = new gp_Pnt(G.X(),
		G.Y(),
		G.Z() + 0.1 );
		TopoDS_Edge constructionLine = new BRepBuilderAPI_MakeEdge(G,p2).Edge();
		Geom_Curve constructionCurve = BRep_Tool.Curve(constructionLine,new double[1], new double[1]);
		double[] cSlatParH = getParamIntersectionPnts(slatTELow, constructionCurve);
		System.out.println("Dim cSlatParH : " + cSlatParH.length);
		gp_Pnt H = new gp_Pnt(slatTELow.Value(cSlatParH[0]).X(),
				slatTELow.Value(cSlatParH[0]).Y(),
				slatTELow.Value(cSlatParH[0]).Z());
		System.out.println("Point H coordinates : " + H.X() + " " + H.Y() + " " + H.Z());
		
		// Tangent vectors in G and H
		
		gp_Vec tangPntG = new gp_Vec();
		gp_Pnt PntG = new gp_Pnt();
		airfoil_GeomCurve.D1(cSlatParG, PntG, tangPntG);
		tangPntG.Normalize();
		tangPntG = tangPntG.Reversed();
		
		gp_Vec tangPntH = new gp_Vec();
		gp_Pnt PntH = new gp_Pnt();
		slatTELow.D1(cSlatParH[0], PntH, tangPntH);
		tangPntH.Normalize();
		tangPntH = tangPntH.Reversed();
		
		// Point I
		
		gp_Pnt I = new gp_Pnt(E.X() ,
				G.Y(),
				(G.Z() + H.Z()) * 0.5);
		System.out.println("Point I coordinates : " + I.X() + " " + I.Y() + " " + I.Z());

//		gp_Vec normPntE = tangPntE.Crossed(zyDir).Normalized();
		
		// Curves creation
		
//		List<double[]> TEPointsLower1 = new ArrayList<>();
//		TEPointsLower1.add(new double[]{G.Coord(1),G.Coord(2),G.Coord(3)});
//		TEPointsLower1.add(new double[]{I.Coord(1),I.Coord(2),I.Coord(3)});
//		
//		CADEdge slatTELower1 = OCCUtils.theFactory.newCurve3D(TEPointsLower1,
//				false, 
//				new double[] {-tangPntG.X()*1.0, -tangPntG.Y()*1.0, -tangPntG.Z()*1.0}, 
//				new double[] {normPntE.X(), normPntE.Y(), normPntE.Z()},
//				false).edge();
//		
//		List<double[]> TEPointsLower2 = new ArrayList<>();
//		TEPointsLower2.add(new double[]{I.Coord(1),I.Coord(2),I.Coord(3)});
//		TEPointsLower2.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
//		
//		CADEdge slatTELower2 = OCCUtils.theFactory.newCurve3D(TEPointsLower2,
//				false, 
//				new double[] {normPntE.X()*1.0, normPntE.Y()*1.0, normPntE.Z()*1.0}, 
//				new double[] {tangPntH.X(), tangPntH.Y(), tangPntH.Z()},
//				false).edge();
		
		List<double[]> TEPointsLower3 = new ArrayList<>();
		TEPointsLower3.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
		TEPointsLower3.add(new double[]{F.Coord(1),F.Coord(2),F.Coord(3)});
		
		CADEdge slatTELower3 = OCCUtils.theFactory.newCurve3D(TEPointsLower3,
				false, 
				new double[] {tangPntH.X()*1.0, tangPntH.Y()*1.0, tangPntH.Z()*1.0}, 
				new double[] {-normPntF.X(), -normPntF.Y(), -normPntF.Z()},
				false).edge();
		
		List<double[]> TEPointsLower4 = new ArrayList<>();
		TEPointsLower4.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
		TEPointsLower4.add(new double[]{G.Coord(1),G.Coord(2),G.Coord(3)});
		
		CADEdge slatTELower4 = OCCUtils.theFactory.newCurve3D(TEPointsLower4,
				false, 
				new double[] {-tangPntH.X()*2.0, -tangPntH.Y()*2.0, -tangPntH.Z()*2.0}, 
				new double[] {tangPntG.X()*2.0, tangPntG.Y()*2.0, tangPntG.Z()*2.0},
				false).edge();
		
		
		String fileName = "Test14as.brep";
		if(OCCUtils.write(fileName,airfoilLELower,slatTELower3,slatTELower4, airfoilLEUpper,slatTEUpper, (CADEdge) OCCUtils.theFactory.newShape(slatFinalCut), (CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut),(CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut)))
//			slatTELower, 
			System.out.println("========== [main] Output written on file: " + fileName);

		
	}
	
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

	public static double calcLength(TopoDS_Edge edge) {
		GProp_GProps prop = new GProp_GProps();
		BRepGProp.LinearProperties(edge,prop);
		return prop.Mass();

	}
	
	// Get longest Edge from a list
	public static TopoDS_Edge getLongestEdge(List<OCCEdge> edgeList) {
		System.out.println("Dim : "	+ edgeList.size());
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
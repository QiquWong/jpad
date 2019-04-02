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
import aircraft.components.liftingSurface.creator.SlatCreator;
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
import javaslang.Tuple2;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.GeomAbs_Shape;
import opencascade.Geom_Curve;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test15as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test15as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface liftingSurface = aircraft.getWing();

		// Wing and flap data
		double slatLateralGap = 0.025;
		double wingSemiSpan = liftingSurface.getSemiSpan().doubleValue(SI.METER);

		List<double[]> slatStations = new ArrayList<>();
		List<double[]> slatChordRatio = new ArrayList<>();
		
		int numSlat = 2;
		slatStations.add(new double[] {
				0.08,
				0.31
		});
		slatStations.add(new double[] {
		0.33,
		0.85
		});
		
		slatChordRatio.add(new double[] {
				0.17,
				0.17
		});
		slatChordRatio.add(new double[] {
				0.17,
				0.17
		});

		System.out.println("Wing semispan" + wingSemiSpan + " m");
		System.out.println("Flap lateral gap: " + slatLateralGap + " m");
		System.out.println("Number of flaps: " + liftingSurface.getSymmetricFlaps().size());
		System.out.println("Symmetric flaps spanwise position:");
		slatStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Symmetric flaps chord ratios:");
		slatChordRatio.forEach(d -> System.out.println(Arrays.toString(d)));

		double adjustmentPar = 0.01;
		List<Double> etaBreakPnts = liftingSurface.getEtaBreakPoints();
		System.out.println("Lifting surface eta breakpoints: " + 
				Arrays.toString(etaBreakPnts.toArray(new Double[etaBreakPnts.size()])));

		for(int i = 0; i < numSlat; i++) {
			double etaInn = slatStations.get(i)[0];
			double etaOut = slatStations.get(i)[1];
			double innChordRatio = slatChordRatio.get(i)[0];
			double outChordRatio = slatChordRatio.get(i)[1];

			for(int j = 0; j < etaBreakPnts.size()-1; j++) {

				if(etaInn >= etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatio.get(i), 
								etaInn
								);
					}

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatio.get(i), 
								etaOut
								);
					}

					slatStations.set(i, new double[] {etaInn, etaOut});
					slatChordRatio.set(i, new double[] {innChordRatio, outChordRatio});				
				}

				if((etaInn >= etaBreakPnts.get(j) && etaInn < etaBreakPnts.get(j+1)) 
						&& etaOut > etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatio.get(i), 
								etaInn
								);
					}

					etaOut = etaBreakPnts.get(j+1) - adjustmentPar;
					outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							slatStations.get(i), 
							slatChordRatio.get(i), 
							etaOut
							);

					slatStations.set(i, new double[] {etaInn, etaOut});
					slatChordRatio.set(i, new double[] {innChordRatio, outChordRatio});

					double[] nextStations = slatStations.get(i+1);
					double[] nextChordRatios = slatChordRatio.get(i+1);

					double etaInnNext = etaBreakPnts.get(j+1) + adjustmentPar; 
					double innChordRatioNext = MyMathUtils.getInterpolatedValue1DLinear(
							nextStations, 
							nextChordRatios, 
							etaInnNext
							);

					slatStations.set(i+1, new double[] {etaInnNext, nextStations[1]});
					slatChordRatio.set(i+1, new double[] {innChordRatioNext, nextChordRatios[1]});
				}

				if(etaInn < etaBreakPnts.get(j) && 
						(etaOut > etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1))) {

					etaInn = etaBreakPnts.get(j) + adjustmentPar;
					innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							slatStations.get(i), 
							slatChordRatio.get(i), 
							etaInn
							);

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatio.get(i), 
								etaOut
								);
					}

					slatStations.set(i, new double[] {etaInn, etaOut});
					slatChordRatio.set(i, new double[] {innChordRatio, outChordRatio});

					double[] prevStations = slatStations.get(i-1);
					double[] prevChordRatios = slatChordRatio.get(i-1);

					double etaOutPrev = etaBreakPnts.get(j) - adjustmentPar; 
					double outChordRatioPrev = MyMathUtils.getInterpolatedValue1DLinear(
							prevStations, 
							prevChordRatios, 
							etaOutPrev
							);

					slatStations.set(i-1, new double[] {prevStations[0], etaOutPrev});
					slatChordRatio.set(i-1, new double[] {prevChordRatios[0], outChordRatioPrev});
				}
			}
		}

		System.out.println("Slats adjusted spanwise position:");
		slatStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Slats adjusted chord ratios:");
		slatChordRatio.forEach(d -> System.out.println(Arrays.toString(d)));

		// Wing wire-frame construction

		ComponentEnum typeLS = liftingSurface.getType();
		List<OCCShape> extraShapes = new ArrayList<>();

		int nPanels = liftingSurface.getPanels().size();
		System.out.println(">>> n. panels: " + nPanels);

		Amount<Length> xApex = liftingSurface.getXApexConstructionAxes();
		Amount<Length> zApex = liftingSurface.getZApexConstructionAxes();
		Amount<Angle> riggingAngle = liftingSurface.getRiggingAngle();

		// Build the leading edge
		List<double[]> ptsLE = new ArrayList<double[]>();

		// calculate FIRST breakpoint coordinates
		ptsLE.add(new double[] {xApex.doubleValue(SI.METER), 0.0, zApex.doubleValue(SI.METER)});

		// calculate breakpoints coordinates
		for (int kBP = 1; kBP < liftingSurface.getXLEBreakPoints().size(); kBP++) {
			double xbp = liftingSurface.getXLEBreakPoints().get(kBP).plus(xApex).doubleValue(SI.METER);
			double ybp = liftingSurface.getYBreakPoints().get(kBP).doubleValue(SI.METER);
			double zbp = zApex.doubleValue(SI.METER);
			double spanPanel = liftingSurface.getPanels().get(kBP - 1).getSpan().doubleValue(SI.METER);
			double dihedralPanel = liftingSurface.getPanels().get(kBP - 1).getDihedral().doubleValue(SI.RADIAN);
			zbp = zbp + ybp*Math.tan(dihedralPanel);
			if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				ptsLE.add(new double[] {
						xbp,
						0,
						ybp + zApex.doubleValue(SI.METER)
				});
			} else {
				ptsLE.add(new double[] {xbp, ybp, zbp});
			}
			System.out.println("span #" + kBP + ": " + spanPanel);
			System.out.println("dihedral #" + kBP + ": " + dihedralPanel);
		}	

		// make a wire for the leading edge
		List<TopoDS_Edge> tdsEdgesLE = new ArrayList<>();
		for (int kPts = 1; kPts < ptsLE.size(); kPts++) {
			BRepBuilderAPI_MakeEdge em = new BRepBuilderAPI_MakeEdge(
					new gp_Pnt(ptsLE.get(kPts - 1)[0], ptsLE.get(kPts - 1)[1], ptsLE.get(kPts - 1)[2]),
					new gp_Pnt(ptsLE.get(kPts    )[0], ptsLE.get(kPts    )[1], ptsLE.get(kPts    )[2])
					);
			em.Build();
			if (em.IsDone() == 1)
				tdsEdgesLE.add(em.Edge());
		}

		// export
		tdsEdgesLE.forEach(e -> extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(e)));

		// Add chord segments & build the trailing edge
		List<double[]> ptsTE = new ArrayList<double[]>();
		List<Double> chords = new ArrayList<>();
		List<Double> twists = new ArrayList<>();
		for (int kPts = 0; kPts < ptsLE.size(); kPts++) {
			double ybp = liftingSurface.getYBreakPoints().get(kPts).doubleValue(SI.METER); 
			double chord = liftingSurface.getChordsBreakPoints().get(kPts).doubleValue(SI.METER);    
			chords.add(chord);
			double twist = liftingSurface.getTwistsBreakPoints().get(kPts).doubleValue(SI.RADIAN);		
			twists.add(twist);
			System.out.println(">>> ybp:   " + ybp);
			System.out.println(">>> chord: " + chord);
			System.out.println(">>> twist: " + twist);
			if(typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
				ptsTE.add(new double[] {
						ptsLE.get(kPts)[0] + chord, 
						0, 
						ptsLE.get(kPts)[2]
				});
			} else {
				ptsTE.add(new double[] {
						ptsLE.get(kPts)[0] + chord*Math.cos(twist + riggingAngle.doubleValue(SI.RADIAN)), 
						ybp, 
						ptsLE.get(kPts)[2] - chord*Math.sin(twist + riggingAngle.doubleValue(SI.RADIAN)) 		
				});
			}
			System.out.println(">>> ptsLE: " + Arrays.toString(ptsLE.get(kPts)) );
			System.out.println(">>> ptsTE: " + Arrays.toString(ptsTE.get(kPts)) );
		}

		List<TopoDS_Edge> tdsChords = new ArrayList<>();
		for (int kPts = 0; kPts < ptsLE.size(); kPts++) {
			BRepBuilderAPI_MakeEdge em = new BRepBuilderAPI_MakeEdge(
					new gp_Pnt(ptsLE.get(kPts)[0], ptsLE.get(kPts)[1], ptsLE.get(kPts)[2]),
					new gp_Pnt(ptsTE.get(kPts)[0], ptsTE.get(kPts)[1], ptsTE.get(kPts)[2])
					);
			em.Build();
			if (em.IsDone() == 1)
				tdsChords.add(em.Edge());
		}
		// export
		tdsChords.forEach(e -> extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(e)));

		List<TopoDS_Edge> tdsEdgesTE = new ArrayList<>();
		for (int kPts = 1; kPts < ptsTE.size(); kPts++) {
			BRepBuilderAPI_MakeEdge em = new BRepBuilderAPI_MakeEdge(
					new gp_Pnt(ptsTE.get(kPts - 1)[0], ptsTE.get(kPts - 1)[1], ptsTE.get(kPts - 1)[2]),
					new gp_Pnt(ptsTE.get(kPts    )[0], ptsTE.get(kPts    )[1], ptsTE.get(kPts    )[2])
					);
			em.Build();
			if (em.IsDone() == 1)
				tdsEdgesTE.add(em.Edge());
		}
		// export
		tdsEdgesTE.forEach(e -> extraShapes.add((OCCShape)OCCUtils.theFactory.newShape(e)));

		// Create airfoils at specific y stations
		
		// List<List<CADGeomCurve3D>> cadCurveAirfoilList = new ArrayList<List<CADGeomCurve3D>>();
		// airfoils at breakpoints
		List<CADEdge> airfoils = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList = new ArrayList<CADGeomCurve3D>();
		cadCurveAirfoilBPList = IntStream.range(0, liftingSurface.getYBreakPoints().size())
				.mapToObj(i -> {
					Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(i);
					List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
							liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER), 
							airfoilCoords, 
							liftingSurface
							);
					CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
					return cadCurveAirfoil;
				})
				.collect(Collectors.toList());

		cadCurveAirfoilBPList.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
//		cadCurveAirfoilList.add(cadCurveAirfoilBPList);

		// Airfoils at slat breakpoints

		List<Amount<Length>> slatYBreakPoints = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList_Flap = new ArrayList<CADGeomCurve3D>();

		for(int j = 0; j < numSlat; j++) {
			double etaInnerFlap = slatStations.get(j)[0];
			double etaOuterFlap = slatStations.get(j)[1];
			// for breakpoints
			int innerPanel = 0;
			int outerPanel = 0;
			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

				if(etaInnerFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
						etaInnerFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					innerPanel = n;
				}

				if(etaOuterFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
						etaOuterFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}
			int[] intArray = new int[] {innerPanel, outerPanel};
			slatYBreakPoints.add(Amount.valueOf(slatStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
			slatYBreakPoints.add(Amount.valueOf(slatStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
			
			for(int k = 0; k < slatYBreakPoints.size(); k++) {

				Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[k]);
				List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
						slatYBreakPoints.get(k).doubleValue(SI.METER), 
						airfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
				cadCurveAirfoilBPList_Flap.add(cadCurveAirfoil);
			}

			cadCurveAirfoilBPList_Flap.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
			cadCurveAirfoilBPList_Flap.clear();
//			cadCurveAirfoilList.add(cadCurveAirfoilBPList_Flap);
			slatYBreakPoints.clear();
		}
		
		// Cut airfoils at flap breakpoints
		
		List<Amount<Length>> cutslatYBreakPoints = new ArrayList<>();
		List<OCCShape> exportInnerCutSlat = new ArrayList<>();
		List<OCCShape> exportSlat = new ArrayList<>();
		List<CADWire> slatCutWire = new ArrayList<>();
		List<CADWire> slatCutWire_Slat = new ArrayList<>();
		List<OCCShape> slatSolids = new ArrayList<>();
		List<OCCShape> solidsSectionWing = new ArrayList<>();
		
		// Inner sections
	for( int i = 0; i < 2; i++ ) {
		for( int j = 0; j < numSlat; j++ ) {
			double etaInnerSlat = slatStations.get(j)[0];
			double etaOuterSlat = slatStations.get(j)[1];
			double ChordRatio = slatChordRatio.get(j)[i];
			
			double cSlat = 0.17; // slat chord ratio
			double k1 = 0.70; // lower slat factor
			double k2 = 0.25; // 
			double k3 = 0.30; //
			double k4 = 0.32;
			double k5 = 0.08;
			double cSlatLower = k1 * ChordRatio; // flap gap 
			double cSlatMiddle = k4 * ChordRatio;
			double deltaSlat1 = k2 * cSlat; // airfoil TE
			double deltaSlat2 = k3 * cSlatLower; // flap LE
			double slatGap = k5 * cSlat;
			double deltaSlat3 = 0.011; // Slat TE lower detail
			
			int innerPanel = 0;
			int outerPanel = 0;
			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

				if(etaInnerSlat > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && etaInnerSlat < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					innerPanel = n;
				}

				if(etaOuterSlat > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && etaOuterSlat < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}
			int[] intArray = new int[] {innerPanel, outerPanel};
			cutslatYBreakPoints.add(Amount.valueOf(slatStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
			cutslatYBreakPoints.add( Amount.valueOf(slatStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));

			Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[i]);
			System.out.println("Starting Non-symmetric procedure...");
			List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
					cutslatYBreakPoints.get(i).doubleValue(SI.METER), 
					airfoilCoords, 
					liftingSurface
					);
			
			CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
			CADEdge edgeAirfoil = cadCurveAirfoil.edge();
			CADGeomCurve3D chord = null;
			if (i == 0 ) {
					chord = getChordSegmentAtYActual(etaInnerSlat*wingSemiSpan, liftingSurface);
			}
			else {
				chord = getChordSegmentAtYActual(etaOuterSlat*wingSemiSpan, liftingSurface);
			}
			
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
					(1 - ChordRatio) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					liftingSurface.getType(), 
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
					liftingSurface.getType(), 
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
			gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
					new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
					gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
							new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);


			gp_Dir lsAxis = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Dir(0, 0, 1) : new gp_Dir(0, 1, 0);			
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
					(1 - ChordRatio + deltaSlat1) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					liftingSurface.getType(), 
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
					liftingSurface.getType(), 
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

			// Slat detail
			// Point G
			double cSlatParG = getParamsIntersectionPntsOnAirfoil(
					airfoil_GeomCurve, 
					chord_GeomCurve, 
					chordLength, 
					(1 - cSlatLower + deltaSlat2 + deltaSlat3) * chordLength, 
					chord_edge.vertices()[0].pnt(), 
					liftingSurface.getType(), 
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
			
			List<double[]> TEPointsLower1 = new ArrayList<>();
			TEPointsLower1.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
			TEPointsLower1.add(new double[]{F.Coord(1),F.Coord(2),F.Coord(3)});
			
			CADEdge slatTELower1 = OCCUtils.theFactory.newCurve3D(TEPointsLower1,
					false, 
					new double[] {tangPntH.X()*1.0, tangPntH.Y()*1.0, tangPntH.Z()*1.0}, 
					new double[] {-normPntF.X(), -normPntF.Y(), -normPntF.Z()},
					false).edge();
			
			List<double[]> TEPointsLower2 = new ArrayList<>();
			TEPointsLower2.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
			TEPointsLower2.add(new double[]{G.Coord(1),G.Coord(2),G.Coord(3)});
			
			CADEdge slatTELower2 = OCCUtils.theFactory.newCurve3D(TEPointsLower2,
					false, 
					new double[] {-tangPntH.X()*2.0, -tangPntH.Y()*2.0, -tangPntH.Z()*2.0}, 
					new double[] {tangPntG.X()*2.0, tangPntG.Y()*2.0, tangPntG.Z()*2.0},
					false).edge();
			
			
			TopoDS_Edge airfoilTE = new TopoDS_Edge();
			gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(airfoilFirstCut));
			gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(airfoilSecondCut));
			BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
			airfoilTE = buildFlapTE.Edge();
			
			gp_Trsf flapTrasl = new gp_Trsf();
			if ( i == 0 ) {
				
				flapTrasl.SetTranslation(new gp_Pnt(0, etaInnerSlat * wingSemiSpan, 0), new gp_Pnt(0, etaInnerSlat * wingSemiSpan + slatLateralGap, 0));
			
			}
			else {
				
				flapTrasl.SetTranslation(new gp_Pnt(0, etaOuterSlat*wingSemiSpan, 0), new gp_Pnt(0, etaOuterSlat * wingSemiSpan - slatLateralGap, 0));

			}
			
			TopoDS_Edge slatUpperTE_Edge = TopoDS.ToEdge(((OCCShape) slatTEUpper).getShape());
			TopoDS_Edge slatLowerLE1_Edge = TopoDS.ToEdge(((OCCShape) slatTELower1).getShape());			
			TopoDS_Edge slatLowerLE2_Edge = TopoDS.ToEdge(((OCCShape) slatTELower2).getShape());			
			TopoDS_Edge airfoilUpperLE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLEUpper).getShape());
			TopoDS_Edge airfoilLowerLE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLELower).getShape());
			
			TopoDS_Edge finalSlat = TopoDS.ToEdge(new BRepBuilderAPI_Transform(slatFinalCut, flapTrasl).Shape());
			TopoDS_Edge finalSlatUpperLE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(slatUpperTE_Edge, flapTrasl).Shape());
			TopoDS_Edge finalSlatLowerLE1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(slatLowerLE1_Edge, flapTrasl).Shape());
			TopoDS_Edge finalSlatLowerLE2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(slatLowerLE2_Edge, flapTrasl).Shape());

			
			slatCutWire_Slat.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalSlatUpperLE),
					(CADEdge) OCCUtils.theFactory.newShape(finalSlat), 
					(CADEdge) OCCUtils.theFactory.newShape(finalSlatLowerLE1),
					(CADEdge) OCCUtils.theFactory.newShape(finalSlatLowerLE2)));
			
			exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlat));
			exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatUpperLE));
			exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatLowerLE1));
			exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatLowerLE2));



			slatCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges(
					(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge),
					(CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut),
					(CADEdge) OCCUtils.theFactory.newShape(airfoilTE),
					(CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut),
					(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge)
					));
			exportInnerCutSlat.add((OCCShape) OCCUtils.theFactory.newShape(airfoilSecondCut));
			exportInnerCutSlat.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge));
			exportInnerCutSlat.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge));		
			exportInnerCutSlat.add((OCCShape) OCCUtils.theFactory.newShape(airfoilFirstCut));
			exportInnerCutSlat.add((OCCShape) OCCUtils.theFactory.newShape(airfoilTE));
			
			cutslatYBreakPoints.clear();

		}
	}
	
    System.out.println("Prima di wing shell creation Dim airfoils : " + airfoils.size());
    System.out.println("Dim innerCutSlat : " + exportInnerCutSlat.size());

		// Wing shell creation 
		
		int n_section = liftingSurface.getYBreakPoints().size() + 2 * numSlat;
		System.out.println("Number of sections : " + n_section);
		
		double[] y_array = new double[airfoils.size()] ;
		double[] flap_array = new double[2 * numSlat];
		int k = 0;
		for(int i = 0; i < numSlat; i++) {

			for(int j = 0; j < 2; j++) {

				flap_array[k] = slatStations.get(i)[j]*liftingSurface.getSemiSpan().doubleValue(SI.METER);
				k++;
			}

		}

		for ( int j = 0; j < airfoils.size(); j++ ) {

			double[] box = airfoils.get(j).boundingBox();
			y_array[j] = box[1];

		}
		
		Arrays.sort(y_array);
		System.out.println("Sorted y_array : " + Arrays.toString(y_array));

		List<OCCShape> exportShapes = new ArrayList<>();
		List<OCCShape> exportShapes2 = new ArrayList<>();
		List<OCCShape> patchWing = new ArrayList<>();
		List<OCCShape> exportClosedShapes = new ArrayList<>();
		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		List<CADEdge> supportAirfoils = new ArrayList<>();
        List<CADWire> supportSlatCutWire = new ArrayList<>();
       
        System.out.println("Numero di sezioni : " + n_section);
		for( int i = 0; i < n_section-1; i++ ) {

			double y_1 = y_array[i];
			double y_2 = y_array[i+1];
			System.out.println("y_1 :" + y_1);
            System.out.println("y_2 :" + y_2);
            
			CADEdge Edge_Inner = null;
			CADEdge Edge_Outer = null;
			CADWire Wire_Inner = null;
			CADWire Wire_Outer = null;

			List<CADEdge> inner_airfoils = new ArrayList<>();
			List<CADEdge> outer_airfoils = new ArrayList<>(); 

			for(int n = 0; n < airfoils.size(); n++) {

				CADEdge expEdge = airfoils.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[1]) - y_1) < 1.0E-5) {
					inner_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < exportInnerCutSlat.size(); n++) {

				CADEdge expEdge = (CADEdge) exportInnerCutSlat.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[1]) - y_1) < 1.0E-5) {
					inner_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < airfoils.size(); n++) {

				CADEdge expEdge = airfoils.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[1]) - Math.abs(y_2)) < 1.0E-5) {
					outer_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < exportInnerCutSlat.size(); n++) {

				CADEdge expEdge = (CADEdge) exportInnerCutSlat.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[1]) - Math.abs(y_2)) < 1.0E-5) {
					outer_airfoils.add(expEdge);
				}	

			}

			if( inner_airfoils.size() + outer_airfoils.size() < 12) {

				for(int n = 0; n < airfoils.size(); n++) {

					CADEdge expEdge = airfoils.get(n);
					double[] box = expEdge.boundingBox();
					if (Math.abs(Math.abs(box[1]) - y_1) < 1.0E-5) {
						Edge_Inner =  expEdge;
					}	

				}

				for(int n = 0; n < airfoils.size(); n++) {

					CADEdge expEdge = airfoils.get(n);
					double[] box = expEdge.boundingBox();
					if (Math.abs(Math.abs(box[1]) - y_2) < 1.0E-5) {
						Edge_Outer =  expEdge;
					}	

				}
				
				 // Add section between inner and outer sections
				 
                int numInterAirfoil = 1;
                CADGeomCurve3D supportAirfoil = null;
                double[] secVec = new double[numInterAirfoil +2];
                secVec = MyArrayUtils.linspace(
                        y_1, 
                        y_2, 
                        numInterAirfoil + 2);
 
                for(int i1 = 0; i1 < numInterAirfoil; i1++) {
 
                    double y_station = secVec[i1 + 1];
 
                    int panel = 0;
                    for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {
 
                        if(y_station > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
                                y_1 < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
                            panel = n;
                        }
 
                    }
                    int[] intArray = new int[] {panel};
 
                    Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[0]);
                    List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
                            y_station, 
                            airfoilCoords, 
                            liftingSurface
                            );
                    supportAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
 
                    supportAirfoils.add((OCCEdge)((OCCGeomCurve3D) supportAirfoil).edge());
 
                }

				List<CADGeomCurve3D> selectedCurves = new ArrayList<CADGeomCurve3D>();
				CADWire selectedWiresInner = null;
				CADWire selectedWiresOuter = null;
				CADGeomCurve3D inner_Curve = OCCUtils.theFactory.newCurve3D(Edge_Inner);
				CADGeomCurve3D outer_Curve = OCCUtils.theFactory.newCurve3D(Edge_Outer);
				selectedCurves.add(inner_Curve);
				if(Math.abs(Math.abs(y_2) - Math.abs(y_1)) > 3) {
                    selectedCurves.add(supportAirfoil);
                    System.out.println("Using support airfoils");
                }
				selectedCurves.add(outer_Curve);

				OCCShape Shape = OCCUtils.makePatchThruCurveSections(selectedCurves);
				exportShapes.add(Shape);			
				List<OCCShape> patchProva = new ArrayList<>();
				List<OCCShape> patchProvaTE = new ArrayList<>();	

				patchProva.addAll(selectedCurves.stream()
		                   .map(OCCUtils::makePatchThruCurveSections)
		                   .collect(Collectors.toList()));
				
				double[] crv1 = inner_Curve.getRange();
				double[] crv2 = outer_Curve.getRange();
				double[] first_point_inn = inner_Curve.value(crv1[0]);
				double[] last_point_inn = inner_Curve.value(crv1[1]);
				double[] first_point_out = outer_Curve.value(crv2[0]);
				double[] last_point_out = outer_Curve.value(crv2[1]);
				
				selectedWiresInner = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
						Edge_Inner);

				selectedWiresOuter = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_out,last_point_out).edge(),
						Edge_Outer);
				
				CADFace innerFace = OCCUtils.theFactory.newFacePlanar(selectedWiresInner);
				CADFace outerFace = OCCUtils.theFactory.newFacePlanar(selectedWiresOuter);
				exportShapes.add((OCCShape) innerFace);
				exportShapes.add((OCCShape) outerFace);
				
				// Closing the trailing edge
				List<CADShape> cadShapeList = new ArrayList<>();
				cadShapeList.add((OCCShape) innerFace);
				cadShapeList.add((OCCShape) outerFace);
				cadShapeList.add((OCCShape) Shape);
				
				for(int j1 = 1; j1 < selectedCurves.size(); j1++) {

					double[] crvR1 = selectedCurves.get(j1-1).getRange();
					double[] crvR2 = selectedCurves.get(j1).getRange();
					CADFace face1 = OCCUtils.theFactory.newFacePlanar(
							selectedCurves.get(j1-1).value(crvR1[0]),
							selectedCurves.get(j1-1).value(crvR1[1]),
							selectedCurves.get(j1).value(crvR2[0])
							);

					CADFace face2 = OCCUtils.theFactory.newFacePlanar(
							selectedCurves.get(j1).value(crvR2[0]),
							selectedCurves.get(j1).value(crvR2[1]),
							selectedCurves.get(j1-1).value(crvR1[1])
							);
					CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(face1, face2);
					exportShapes.add((OCCShape) shell);
					
					// Prova shell chiusa
					
					cadShapeList.add((OCCShape) shell);					
					//
					patchWing.add((OCCShape)shell);
					patchProvaTE.add((OCCShape) shell);

					sewMakerWing.Init();						
					sewMakerWing.Add(((OCCShape) shell).getShape());
					sewMakerWing.Add(Shape.getShape());
					sewMakerWing.Add(((OCCShape)innerFace).getShape());
                    sewMakerWing.Add(((OCCShape)outerFace).getShape());
					sewMakerWing.Perform();
					TopoDS_Shape sewedSection = sewMakerWing.SewedShape();

					System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMakerWing.IsNull());	
					System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Building the solid");
					CADSolid solidWing = null;
					BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
					solidMaker.Add(TopoDS.ToShell(sewedSection));
					solidMaker.Build();
					System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
					solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
					solidsSectionWing.add((OCCShape) solidWing);					

				}
				
				CADShell sectionShell = OCCUtils.theFactory.newShellFromAdjacentShapes(cadShapeList);
				exportClosedShapes.add((OCCShape) sectionShell);

			}

			else {

				for(int n = 0; n < slatCutWire.size(); n++) {


					CADWire expWire = slatCutWire.get(n);
					double[] box = expWire.boundingBox();
					if (Math.abs(Math.abs(box[1]) - y_1) < 1.0E-5) {
						Wire_Inner =  expWire;
					}	

				}

				for(int n = 0; n < slatCutWire.size(); n++) {

					CADWire expWire = slatCutWire.get(n);
					double[] box = expWire.boundingBox();
					if (Math.abs(Math.abs(box[1]) - y_2) < 1.0E-5) {
						Wire_Outer =  expWire;
					}	

				}
				
				// Add sections
                int numInterAirfoil = 1;
                CADWire supportWire = null;
                double cSlat = 0.17; // slat chord ratio
    			double k1 = 0.70; // lower slat factor
    			double k2 = 0.25; // 
    			double k3 = 0.30; //
    			double k4 = 0.32;
    			double k5 = 0.08;
    			double cSlatLower = k1 * cSlat; // flap gap 
    			double cSlatMiddle = k4 * cSlat;
    			double deltaSlat1 = k2 * cSlat; // airfoil TE
    			double deltaSlat2 = k3 * cSlatLower; // flap LE
    			double slatGap = k5 * cSlat;
    			double deltaSlat3 = 0.011;
 
                double[] secVec = new double[numInterAirfoil +2];
                secVec = MyArrayUtils.linspace(
                        y_1, 
                        y_2, 
                        numInterAirfoil + 2);
                int flapIndex = 0;
                for (int i1 = 0; i1 < numSlat; i1++) {
 
                    double flapInnerSec = slatStations.get(i1)[0];
                    if( Math.abs( Math.abs(flapInnerSec) - Math.abs(y_1) ) < 1e-5) {
                        flapIndex = i1;
                    }
 
                }
 
                double[] chordVec = new double[numInterAirfoil +2];
                chordVec = MyArrayUtils.linspace(
                        slatChordRatio.get(flapIndex)[0], 
                        slatChordRatio.get(flapIndex)[1], 
                        numInterAirfoil + 2);
 
                for(int i1 = 0; i1 < numInterAirfoil; i1++) {
 
                    double y_station = secVec[i1 + 1];
                    double chordRatio = chordVec[i1 + 1];
 
                    int panel = 0;
                    for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {
 
                        if(y_station > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
                                y_1 < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
                            panel = n;
                        }
 
                    }
                    int[] intArray = new int[] {panel};
 
                    Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[0]);
                    List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
                            y_station, 
                            airfoilCoords, 
                            liftingSurface
                            );
 
                    CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
                    CADEdge edgeAirfoil = cadCurveAirfoil.edge();
                    CADGeomCurve3D chord = getChordSegmentAtYActual(y_station, liftingSurface);
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
        					liftingSurface.getType(), 
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
        					liftingSurface.getType(), 
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
        			gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
        					new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
        					gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
        							new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);


        			gp_Dir lsAxis = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Dir(0, 0, 1) : new gp_Dir(0, 1, 0);			
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
        					liftingSurface.getType(), 
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
        					liftingSurface.getType(), 
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

        			// Slat detail
        			// Point G
        			double cSlatParG = getParamsIntersectionPntsOnAirfoil(
        					airfoil_GeomCurve, 
        					chord_GeomCurve, 
        					chordLength, 
        					(1 - cSlatLower + deltaSlat2 + deltaSlat3) * chordLength, 
        					chord_edge.vertices()[0].pnt(), 
        					liftingSurface.getType(), 
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
        			
        			List<double[]> TEPointsLower1 = new ArrayList<>();
        			TEPointsLower1.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
        			TEPointsLower1.add(new double[]{F.Coord(1),F.Coord(2),F.Coord(3)});
        			
        			CADEdge slatTELower1 = OCCUtils.theFactory.newCurve3D(TEPointsLower1,
        					false, 
        					new double[] {tangPntH.X()*1.0, tangPntH.Y()*1.0, tangPntH.Z()*1.0}, 
        					new double[] {-normPntF.X(), -normPntF.Y(), -normPntF.Z()},
        					false).edge();
        			
        			List<double[]> TEPointsLower2 = new ArrayList<>();
        			TEPointsLower2.add(new double[]{H.Coord(1),H.Coord(2),H.Coord(3)});
        			TEPointsLower2.add(new double[]{G.Coord(1),G.Coord(2),G.Coord(3)});
        			
        			CADEdge slatTELower2 = OCCUtils.theFactory.newCurve3D(TEPointsLower2,
        					false, 
        					new double[] {-tangPntH.X()*2.0, -tangPntH.Y()*2.0, -tangPntH.Z()*2.0}, 
        					new double[] {tangPntG.X()*2.0, tangPntG.Y()*2.0, tangPntG.Z()*2.0},
        					false).edge();
        			
        			TopoDS_Edge airfoilTE = new TopoDS_Edge();
        			gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(airfoilFirstCut));
        			gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(airfoilSecondCut));
        			BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
        			airfoilTE = buildFlapTE.Edge();
        			
        			TopoDS_Edge slatUpperTE_Edge = TopoDS.ToEdge(((OCCShape) slatTEUpper).getShape());
        			TopoDS_Edge slatLowerLE1_Edge = TopoDS.ToEdge(((OCCShape) slatTELower1).getShape());			
        			TopoDS_Edge slatLowerLE2_Edge = TopoDS.ToEdge(((OCCShape) slatTELower2).getShape());			
        			TopoDS_Edge airfoilUpperLE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLEUpper).getShape());
        			TopoDS_Edge airfoilLowerLE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLELower).getShape());
        					
        			supportSlatCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut),
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge), 
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge),
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut),
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilTE)));
        			

        			
        			supportWire = (OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut),
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut), 
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge),
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge),
        					(CADEdge) OCCUtils.theFactory.newShape(airfoilTE)));

                }
                // Fine Prova 

				List<CADWire> selectedWires = new ArrayList<CADWire>();
				CADWire inner_Wire = Wire_Inner;
				CADWire outer_Wire = Wire_Outer;
				
				selectedWires.add(inner_Wire);
				if(Math.abs(Math.abs(y_2) - Math.abs(y_1)) > 10) {
                    selectedWires.add(supportWire);
                    System.out.println("Using support airfoils");
                }
				selectedWires.add(outer_Wire);

				OCCShape Shape = OCCUtils.makePatchThruSections(selectedWires);
				exportShapes.add(Shape);

				CADFace innerFace = OCCUtils.theFactory.newFacePlanar(inner_Wire);
				CADFace outerFace = OCCUtils.theFactory.newFacePlanar(outer_Wire);
				exportShapes.add((OCCShape) innerFace);
				exportShapes.add((OCCShape) outerFace);
				
				sewMakerWing.Init();                        
                sewMakerWing.Add(Shape.getShape());
                sewMakerWing.Add(((OCCShape)innerFace).getShape());
                sewMakerWing.Add(((OCCShape)outerFace).getShape());
                sewMakerWing.Perform();
                TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
				
                System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMakerWing.IsNull());   
                System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Building the solid wing");
                CADSolid solidWing = null;
                BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
                solidMaker.Add(TopoDS.ToShell(sewedSection));
                solidMaker.Build();
                System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
                solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
                solidsSectionWing.add((OCCShape) solidWing);

			}
			
			inner_airfoils.clear();
			outer_airfoils.clear();
					
		}
		
		 // Prova Tip alare
		 
        // Closing the tip using a filler surface
        List<OCCShape> patchWingTip = new ArrayList<>();
        int iTip = cadCurveAirfoilBPList.size() - 1;                      // tip airfoil index 
        CADGeomCurve3D airfoilTip = cadCurveAirfoilBPList.get(iTip);      // airfoil CAD curve
        CADGeomCurve3D airfoilPreTip = cadCurveAirfoilBPList.get(iTip-1); // second to last airfoil CAD curve
 
        Double rTh = liftingSurface.getAirfoilList().get(iTip).getThicknessToChordRatio(); 
        Double eTh = rTh*chords.get(iTip); // effective airfoil thickness
        // creating the tip chord edge
        OCCEdge tipChord = (OCCEdge) OCCUtils.theFactory.newShape(tdsChords.get(iTip)); 
 
        // creating the second to last chord edge
        OCCEdge preTipChord = (OCCEdge) OCCUtils.theFactory.newShape(tdsChords.get(iTip-1));    
 
        // splitting the tip airfoil curve using the first vertex of the tip chord
        List<OCCEdge> airfoilTipCrvs = OCCUtils.splitEdge(
                airfoilTip, 
                OCCUtils.getVertexFromEdge(tipChord, 0).pnt()
                );
 
        // adjusting points for the tip airfoil
        int nPnts = 200;
        CADGeomCurve3D airfoilUpp = OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(0));
        CADGeomCurve3D airfoilLow = OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(1));
        airfoilUpp.discretize(nPnts);
        airfoilLow.discretize(nPnts);
        List<gp_Pnt> gpPntAirfoilUpp = ((OCCGeomCurve3D)airfoilUpp).getDiscretizedCurve().getPoints();
        List<gp_Pnt> gpPntAirfoilLow = ((OCCGeomCurve3D)airfoilLow).getDiscretizedCurve().getPoints();
        gpPntAirfoilUpp.set(nPnts - 1, BRep_Tool.Pnt(OCCUtils.getVertexFromEdge(tipChord, 0).getShape()));
        gpPntAirfoilLow.set(0, BRep_Tool.Pnt(OCCUtils.getVertexFromEdge(tipChord, 0).getShape()));
        CADGeomCurve3D airfoilUppMod = OCCUtils.theFactory.newCurve3DGP(gpPntAirfoilUpp, false);
        CADGeomCurve3D airfoilLowMod = OCCUtils.theFactory.newCurve3DGP(gpPntAirfoilLow, false);
        airfoilTipCrvs.clear();
        airfoilTipCrvs.add((OCCEdge) airfoilUppMod.edge());
        airfoilTipCrvs.add((OCCEdge) airfoilLowMod.edge());
        // splitting the second to last airfoil curve using its chord first vertex
        List<OCCEdge> airfoilPreTipCrvs = OCCUtils.splitEdge(
                airfoilPreTip, 
                OCCUtils.getVertexFromEdge(preTipChord, 0).pnt()
                );
 
        // creating a drawing plane next to the tip airfoil
        PVector le1 = new PVector(
                (float) ptsLE.get(iTip-1)[0],
                (float) ptsLE.get(iTip-1)[1],
                (float) ptsLE.get(iTip-1)[2]  // coordinates of the second to last leading edge BP
                );
 
        PVector le2 = new PVector(
                (float) ptsLE.get(iTip)[0],
                (float) ptsLE.get(iTip)[1],
                (float) ptsLE.get(iTip)[2]    // coordinates of the last leading edge BP
                );
 
        PVector te1 = new PVector(
                (float) ptsTE.get(iTip-1)[0],
                (float) ptsTE.get(iTip-1)[1],
                (float) ptsTE.get(iTip-1)[2]  // coordinates of the second to last trailing edge BP
                );
 
        PVector te2 = new PVector(
                (float) ptsTE.get(iTip)[0],
                (float) ptsTE.get(iTip)[1],
                (float) ptsTE.get(iTip)[2]    // coordinates of the last trailing edge BP
                );
 
        PVector leVector = PVector.sub(le2, le1); // vector representation of the last panel leading edge 
        float leLength = leVector.mag();
        float aLength = eTh.floatValue()/leLength;      
        PVector aVector = PVector.mult(leVector, aLength);
        PVector aPnt = PVector.add(le2, aVector);
 
        PVector teVector = PVector.sub(te2, te1); // vector representation of the last panel trailing edge 
        if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
            teVector.z = leVector.z; // slightly modified in order to obtain a plane
        }   
        float teLength = teVector.mag();
        float bLength = eTh.floatValue()/leLength;      
        PVector bVector = PVector.mult(teVector, bLength);
        PVector bPnt = PVector.add(te2, bVector);
 
        // creating the edge a
        List<PVector> aPnts = new ArrayList<>();
        aPnts.add(le2);
        aPnts.add(aPnt);
        CADGeomCurve3D segmentA = OCCUtils.theFactory.newCurve3DP(aPnts, false);
        extraShapes.add((OCCEdge)((OCCGeomCurve3D)segmentA).edge());
 
        // creating the edge b
        List<PVector> bPnts = new ArrayList<>();
        bPnts.add(te2);
        bPnts.add(bPnt);
        CADGeomCurve3D segmentB = OCCUtils.theFactory.newCurve3DP(bPnts, false);
        extraShapes.add((OCCEdge)((OCCGeomCurve3D)segmentB).edge());
 
        // creating the edge c
        List<PVector> cPnts = new ArrayList<>();
        cPnts.add(aPnt);
        cPnts.add(bPnt);
        CADGeomCurve3D segmentC = OCCUtils.theFactory.newCurve3DP(cPnts, false);
        extraShapes.add((OCCEdge)((OCCGeomCurve3D)segmentC).edge());
 
        // creating vertical splitting vectors for the tip airfoil curve, orthogonal to the chord
        PVector chordTipVector = PVector.sub(te2, le2); // vector in the airfoil plane  
        PVector chordTipNVector = new PVector();
        PVector.cross(chordTipVector, aVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
 
        // creating vertical splitting vectors for the second to last airfoil curve
        PVector chordPreTipVector = PVector.sub(te1, le1);  
        PVector chordPreTipNVector = new PVector();
        PVector.cross(chordPreTipVector, aVector, chordPreTipNVector).normalize(); 
 
        // creating points for the guide curves in the construction plane formed by the segments a, b and c
        double[] mainVSecVector = {0.25, 0.75};
        PVector cPnt = PVector.lerp(aPnt, bPnt, (float) mainVSecVector[0]);
        PVector dPnt = PVector.lerp(aPnt, bPnt, (float) mainVSecVector[1]); 
        PVector ePnt = PVector.lerp(le2, te2, (float) mainVSecVector[1]);
        PVector fPnt = PVector.lerp(dPnt, ePnt, 0.10f); // slightly modified D point    
        PVector gPnt = PVector.lerp(te2, bPnt, 0.75f);
 
        // creating the guide curves in the construction plane
        List<double[]> constrPlaneGuideCrv1Pnts = new ArrayList<>();
        constrPlaneGuideCrv1Pnts.add(new double[] {le2.x, le2.y, le2.z});
        constrPlaneGuideCrv1Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
 
        List<double[]> constrPlaneGuideCrv2Pnts = new ArrayList<>();
        constrPlaneGuideCrv2Pnts.add(new double[] {cPnt.x, cPnt.y, cPnt.z});
        constrPlaneGuideCrv2Pnts.add(new double[] {fPnt.x, fPnt.y, fPnt.z});
        constrPlaneGuideCrv2Pnts.add(new double[] {gPnt.x, gPnt.y, gPnt.z});
 
        // creating the tangent vectors for the guide curves in the construction plane  
        //              PVector cVector = PVector.sub(bPnt, aPnt);
        PVector cVector = PVector.sub(cPnt, aPnt);
        PVector gVector = PVector.sub(gPnt, fPnt);              
        //              double tanAFac = 4*aVector.mag(); // wing and hTail
        //              double tanCFac = 5*aVector.mag();
        //              double tanGFac = 1*aVector.mag();   
        //              double tanAFac = 7*aVector.mag(); // these parameters work fine for the IRON canard
        //              double tanCFac = 7*aVector.mag();
        //              double tanGFac = 1*aVector.mag();   
        double tanAFac = 1;
        double tanCFac = (cVector.mag()/aVector.mag())*0.75;
        double tanGFac = tanCFac;
        aVector.normalize();
        cVector.normalize();
        gVector.normalize();            
        double[] tanAConstrPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {aVector.x, aVector.y, aVector.z}, tanAFac);
        double[] tanCConstrPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {cVector.x, cVector.y, cVector.z}, tanCFac);
        double[] tanGConstrPlaneGuideCrv = MyArrayUtils.scaleArray(new double[] {gVector.x, gVector.y, gVector.z}, tanGFac);    
 
        System.out.println(">>>> Tangent Vector a: " + Arrays.toString(aVector.array()));
        System.out.println(">>>> Tangent Vector c: " + Arrays.toString(cVector.array()));   
        System.out.println(">>>> Tangent Vector g: " + Arrays.toString(gVector.array()));       
        CADGeomCurve3D constrPlaneGuideCrv1 = OCCUtils.theFactory.newCurve3D(
                constrPlaneGuideCrv1Pnts, 
                false, 
                tanAConstrPlaneGuideCrv, 
                tanCConstrPlaneGuideCrv, 
                false
                );  
        CADGeomCurve3D constrPlaneGuideCrv2_0 = OCCUtils.theFactory.newCurve3D(
                constrPlaneGuideCrv2Pnts, 
                false, 
                tanCConstrPlaneGuideCrv, 
                tanGConstrPlaneGuideCrv, 
                false
                );
 
        // splitting constrPlaneGuideCrv2_0 for further manipulations
        List<OCCEdge> constrPlaneGuideCrvs2 = OCCUtils.splitEdge(
                constrPlaneGuideCrv2_0, 
                new double[] {fPnt.x, fPnt.y, fPnt.z}
                );      
        CADGeomCurve3D constrPlaneGuideCrv2 = OCCUtils.theFactory.newCurve3D(constrPlaneGuideCrvs2.get(0));
        CADGeomCurve3D constrPlaneGuideCrv3 = OCCUtils.theFactory.newCurve3D(constrPlaneGuideCrvs2.get(1));
 
        List<CADGeomCurve3D> constrPlaneGuideCrvs = new ArrayList<CADGeomCurve3D>();
        constrPlaneGuideCrvs.add(constrPlaneGuideCrv1);
        constrPlaneGuideCrvs.add(constrPlaneGuideCrv2);
        constrPlaneGuideCrvs.add(constrPlaneGuideCrv3);     
        constrPlaneGuideCrvs.forEach(crv -> extraShapes.add((OCCEdge)((OCCGeomCurve3D)crv).edge())); // export
 
        // creating main vertical sections #1, #2 and #3
        PVector[] secPnts = {cPnt, fPnt};
        List<CADGeomCurve3D[]> mainVSections = new ArrayList<>();
        for(int i = 0; i < 2; i++) {
            CADGeomCurve3D[] mainVSec = createVerCrvsForTipClosure(
                    liftingSurface, 
                    airfoilTipCrvs,
                    airfoilPreTipCrvs,
                    new PVector[] {le1, le2},
                    new PVector[] {te1, te2},
                    mainVSecVector[i],
                    new double[] {secPnts[i].x, secPnts[i].y, secPnts[i].z}
                    );
            mainVSections.add(mainVSec);
        }
 
        CADGeomCurve3D[] mainVSec3 = createVerCrvsForTipClosure( 
                airfoilTipCrvs,
                airfoilPreTipCrvs,
                new PVector[] {le1, le2},
                new PVector[] {te1, te2},
                new double[] {gPnt.x, gPnt.y, gPnt.z}
                );
        mainVSections.add(mainVSec3); // trailing edge vertical section curve
 
        mainVSections.forEach(crvs -> { // export
            extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[0]).edge());
            extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[1]).edge());
        });
 
        // creating sub vertical sections       
        double[] subVSecP1Vector = {0.10, 0.15, 0.40, 0.55, 0.60, 0.75, 0.90}; 
        double[] subVSecP2Vector = {0.25, 0.50, 0.75};
        double[] subVSecP3Vector = {0.25, 0.50, 0.75};
        List<double[]> subVSecVector = new ArrayList<>();
        subVSecVector.add(subVSecP1Vector);
        subVSecVector.add(subVSecP2Vector);
        subVSecVector.add(subVSecP3Vector);
 
        List<CADGeomCurve3D[]> subVSecP1 = new ArrayList<>();
        List<CADGeomCurve3D[]> subVSecP2 = new ArrayList<>();
        List<CADGeomCurve3D[]> subVSecP3 = new ArrayList<>();   
        List<List<CADGeomCurve3D[]>> subVSec = new ArrayList<List<CADGeomCurve3D[]>>();
 
        for(int i = 0; i < 3; i++) {
            int idx = i;
            subVSec.add(Arrays
                    .stream(subVSecVector.get(i))
                    .mapToObj(f -> {
                        double[] crvRange = constrPlaneGuideCrvs.get(idx).getRange();
                        double[] pntOnGuideCurve = constrPlaneGuideCrvs.get(idx).value(f*(crvRange[1] - crvRange[0]) + crvRange[0]);
                        double interpCoord;
                        double chordFraction;
                        double x = pntOnGuideCurve[0];
                        if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
                            interpCoord = pntOnGuideCurve[1];
                            double xLE = MyMathUtils.getInterpolatedValue1DLinear(
                                    new double[] {le2.y, aPnt.y}, 
                                    new double[] {le2.x, aPnt.x}, 
                                    interpCoord
                                    );
                            double xTE = MyMathUtils.getInterpolatedValue1DLinear(
                                    new double[] {te2.y, bPnt.y}, 
                                    new double[] {te2.x, bPnt.x}, 
                                    interpCoord
                                    );
                            chordFraction = (x - xLE)/(xTE - xLE);
                        } else {
                            interpCoord = pntOnGuideCurve[2];
                            double xLE = MyMathUtils.getInterpolatedValue1DLinear(
                                    new double[] {le2.z, aPnt.z}, 
                                    new double[] {le2.x, aPnt.x}, 
                                    interpCoord
                                    );
                            double xTE = MyMathUtils.getInterpolatedValue1DLinear(
                                    new double[] {te2.z, bPnt.z}, 
                                    new double[] {te2.x, bPnt.x}, 
                                    interpCoord
                                    );
                            chordFraction = (x - xLE)/(xTE - xLE);
                        }
                        CADGeomCurve3D[] subVSecCrvs = createVerCrvsForTipClosure(
                                liftingSurface, 
                                airfoilTipCrvs,
                                airfoilPreTipCrvs,
                                new PVector[] {le1, le2},
                                new PVector[] {te1, te2},
                                chordFraction,
                                pntOnGuideCurve
                                );
                        return subVSecCrvs;
                    })
                    .collect(Collectors.toList())
                    );
        }
        subVSecP1.addAll(subVSec.get(0));
        subVSecP2.addAll(subVSec.get(1));
        subVSecP3.addAll(subVSec.get(2));
 
        subVSec.forEach(list -> list.forEach(crvs -> { // export
            extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[0]).edge());
            extraShapes.add((OCCEdge)((OCCGeomCurve3D)crvs[1]).edge());
        }));
 
        // splitting the tip airfoil curves and the construction curve #1 in order to fill the wing tip LE correctly
        List<OCCEdge> airfoilUpperCrvs = new ArrayList<>();
        List<OCCEdge> airfoilLowerCrvs = new ArrayList<>();
 
        airfoilUpperCrvs.addAll(OCCUtils.splitEdge(
                OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(0)), 
                subVSecP1.get(1)[0].edge().vertices()[0].pnt()
                ));
        airfoilLowerCrvs.addAll(OCCUtils.splitEdge(
                OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(1)), 
                subVSecP1.get(1)[1].edge().vertices()[1].pnt()
                ));
        airfoilUpperCrvs.forEach(crv -> extraShapes.add(crv));
        airfoilLowerCrvs.forEach(crv -> extraShapes.add(crv));
 
        List<OCCEdge> constPlaneGuideCrvs1 = new ArrayList<>();
        constPlaneGuideCrvs1.addAll(OCCUtils.splitEdge(
                constrPlaneGuideCrv1, 
                subVSecP1.get(1)[0].edge().vertices()[1].pnt()
                ));
 
        System.out.println(Arrays.toString(airfoilUpperCrvs.get(1).vertices()[0].pnt()) + ", " + 
                Arrays.toString(subVSecP1.get(1)[0].edge().vertices()[0].pnt()));
 
        System.out.println(Arrays.toString(subVSecP1.get(1)[0].edge().vertices()[1].pnt()) + ", " + 
                Arrays.toString(constPlaneGuideCrvs1.get(0).vertices()[1].pnt()));
 
        System.out.println(Arrays.toString(constPlaneGuideCrvs1.get(0).vertices()[0].pnt()) + ", " + 
                Arrays.toString(airfoilUpperCrvs.get(1).vertices()[1].pnt()));
 
        // creating a filler surface at the wing tip leading edge, upper        
        double[] contrCrvUppRng = subVSecP1.get(0)[0].getRange();
        double[] contrPntUpp1 = subVSecP1.get(0)[0].value(0.25*(contrCrvUppRng[1] - contrCrvUppRng[0]) + contrCrvUppRng[0]);
        double[] contrPntUpp2 = subVSecP1.get(0)[0].value(0.50*(contrCrvUppRng[1] - contrCrvUppRng[0]) + contrCrvUppRng[0]);
        double[] contrPntUpp3 = subVSecP1.get(0)[0].value(0.75*(contrCrvUppRng[1] - contrCrvUppRng[0]) + contrCrvUppRng[0]);
 
        BRepOffsetAPI_MakeFilling fillerP1Upp = new BRepOffsetAPI_MakeFilling();
 
        fillerP1Upp.Add(
                airfoilUpperCrvs.get(1).getShape(),
                GeomAbs_Shape.GeomAbs_C0
                );
        fillerP1Upp.Add(
                ((OCCEdge)((OCCGeomCurve3D)subVSecP1.get(1)[0]).edge()).getShape(),
                GeomAbs_Shape.GeomAbs_C0
                );
        fillerP1Upp.Add(
                constPlaneGuideCrvs1.get(0).getShape(),
                GeomAbs_Shape.GeomAbs_C0
                );      
 
        fillerP1Upp.Add(new gp_Pnt(contrPntUpp1[0], contrPntUpp1[1], contrPntUpp1[2]));
        fillerP1Upp.Add(new gp_Pnt(contrPntUpp2[0], contrPntUpp2[1], contrPntUpp2[2]));
        fillerP1Upp.Add(new gp_Pnt(contrPntUpp3[0], contrPntUpp3[1], contrPntUpp3[2]));
 
        fillerP1Upp.Build();
        System.out.println("Deformed surface P1 Upp is done? = " + fillerP1Upp.IsDone());
        System.out.println("Deformed surface P1 Upp shape type: " + fillerP1Upp.Shape().ShapeType());
 
        patchWingTip.add((OCCShape)(OCCUtils.theFactory.newShape(fillerP1Upp.Shape())));
 
        // creating a filler surface at the wing tip leading edge, lower
        double[] contrCrvLowRng = subVSecP1.get(0)[1].getRange();
        double[] contrPntLow1 = subVSecP1.get(0)[1].value(0.25*(contrCrvLowRng[1] - contrCrvLowRng[0]) + contrCrvLowRng[0]);
        double[] contrPntLow2 = subVSecP1.get(0)[1].value(0.50*(contrCrvLowRng[1] - contrCrvLowRng[0]) + contrCrvLowRng[0]);
        double[] contrPntLow3 = subVSecP1.get(0)[1].value(0.75*(contrCrvLowRng[1] - contrCrvLowRng[0]) + contrCrvLowRng[0]);
 
        BRepOffsetAPI_MakeFilling fillerP1Low = new BRepOffsetAPI_MakeFilling();
 
        fillerP1Low.Add(
                constPlaneGuideCrvs1.get(0).getShape(),
                GeomAbs_Shape.GeomAbs_C0
                );
        fillerP1Low.Add(
                ((OCCEdge)((OCCGeomCurve3D)subVSecP1.get(1)[1]).edge()).getShape(),
                GeomAbs_Shape.GeomAbs_C0
                );
        fillerP1Low.Add(
                airfoilLowerCrvs.get(0).getShape(),
                GeomAbs_Shape.GeomAbs_C0
                );
 
        fillerP1Low.Add(new gp_Pnt(contrPntLow1[0], contrPntLow1[1], contrPntLow1[2]));
        fillerP1Low.Add(new gp_Pnt(contrPntLow2[0], contrPntLow2[1], contrPntLow2[2]));
        fillerP1Low.Add(new gp_Pnt(contrPntLow3[0], contrPntLow3[1], contrPntLow3[2]));
 
        fillerP1Low.Build();
        System.out.println("Deformed surface P1 Low is done? = " + fillerP1Low.IsDone());
        System.out.println("Deformed surface P1 Low shape type: " + fillerP1Low.Shape().ShapeType());
 
        patchWingTip.add((OCCShape)(OCCUtils.theFactory.newShape(fillerP1Low.Shape())));
 
        // patching through patch #1 vertical sections
        List<CADGeomCurve3D> sectionsListP1Upp = new ArrayList<>();
        List<CADGeomCurve3D> sectionsListP1Low = new ArrayList<>();
 
        sectionsListP1Upp.addAll(subVSecP1.stream()
                .skip(1)    
                .map(crvs -> crvs[0])
                .collect(Collectors.toList())       
                );
        sectionsListP1Upp.add(mainVSections.get(0)[0]);
 
        sectionsListP1Low.addAll(subVSecP1.stream() 
                .skip(1)
                .map(crvs -> crvs[1])
                .collect(Collectors.toList())       
                );
        sectionsListP1Low.add(mainVSections.get(0)[1]);
 
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP1Upp));
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP1Low));
 
        // patching through patch #2 vertical sections, first step
        List<CADGeomCurve3D> sectionsListP2_1Upp = new ArrayList<>();
        List<CADGeomCurve3D> sectionsListP2_1Low = new ArrayList<>();
 
        sectionsListP2_1Upp.add(mainVSections.get(0)[0]);
        sectionsListP2_1Upp.addAll(subVSecP2.stream()
                .limit(2)
                .map(crvs -> crvs[0])
                .collect(Collectors.toList())       
                );
 
        sectionsListP2_1Low.add(mainVSections.get(0)[1]);
        sectionsListP2_1Low.addAll(subVSecP2.stream()
                .limit(2)   
                .map(crvs -> crvs[1])
                .collect(Collectors.toList())       
                );
 
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP2_1Upp));
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP2_1Low));
 
        // patching through patch #2 vertical sections, second step
        List<CADGeomCurve3D> sectionsListP2_2Upp = new ArrayList<>();
        List<CADGeomCurve3D> sectionsListP2_2Low = new ArrayList<>();
 
        sectionsListP2_2Upp.addAll(subVSecP2.stream()
                .skip(1)
                .map(crvs -> crvs[0])
                .collect(Collectors.toList())       
                );
        sectionsListP2_2Upp.add(mainVSections.get(1)[0]);
 
        sectionsListP2_2Low.addAll(subVSecP2.stream()
                .skip(1)    
                .map(crvs -> crvs[1])
                .collect(Collectors.toList())       
                );
        sectionsListP2_2Low.add(mainVSections.get(1)[1]);
 
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP2_2Upp));
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP2_2Low));
 
        // patching through patch #3 vertical sections
        List<CADGeomCurve3D> sectionsListP3Upp = new ArrayList<>();
        List<CADGeomCurve3D> sectionsListP3Low = new ArrayList<>();
 
        sectionsListP3Upp.add(mainVSections.get(1)[0]);
        sectionsListP3Upp.addAll(subVSecP3.stream()
                .map(crvs -> crvs[0])
                .collect(Collectors.toList())       
                );
        sectionsListP3Upp.add(mainVSections.get(2)[0]);
 
        sectionsListP3Low.add(mainVSections.get(1)[1]);
        sectionsListP3Low.addAll(subVSecP3.stream()
                .map(crvs -> crvs[1])
                .collect(Collectors.toList())       
                );
        sectionsListP3Low.add(mainVSections.get(2)[1]);
 
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP3Upp));
        patchWingTip.add(OCCUtils.makePatchThruCurveSections(sectionsListP3Low));
 
        // filling the wing tip trailing edge       
        CADShape wingTipTE = OCCUtils.makeFilledFace(
                mainVSections.get(2)[0],
                mainVSections.get(2)[1],
                OCCUtils.theFactory.newCurve3D(
                        airfoilUpperCrvs.get(0).vertices()[0].pnt(), 
                        airfoilLowerCrvs.get(1).vertices()[1].pnt()
                        )
                );
 
        patchWingTip.add((OCCShape)wingTipTE);
 
        BRepBuilderAPI_Sewing sewMakerTip = new BRepBuilderAPI_Sewing();
 
        sewMakerTip.Init(); 
        sewMakerTip.SetTolerance(1e-5);
        patchWingTip.forEach(s -> sewMakerTip.Add(s.getShape()));
        sewMakerTip.Perform();
 
        System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Tip sewing step successful? " + !sewMakerTip.IsNull());
 
        TopoDS_Shape tds_shape = sewMakerTip.SewedShape();
 
        // Prova sewing tip alare alla wing
 
 
 
        System.out.println("Numero flap : " + numSlat);
        System.out.println("Stazione primo flap [0] : " + (slatStations.get(0)[0]*wingSemiSpan + slatLateralGap + 1.3));
        System.out.println("Stazione primo flap [1] : " + (slatStations.get(0)[1]*wingSemiSpan - slatLateralGap + 1.3));
 
		//Flap shell creation
		CADWire Wire_Inner_Flap = null;
		CADWire Wire_Outer_Flap = null;
		List<CADWire> selectedWires_Flap = new ArrayList<CADWire>();		

		for(int i = 0; i < numSlat; i++) {
			
			double innerFlapStat = slatStations.get(i)[0] * wingSemiSpan + slatLateralGap + y_array[0];
			double outerFlapStat = slatStations.get(i)[1] * wingSemiSpan - slatLateralGap + y_array[0];
			
			for(int n = 0; n < slatCutWire_Slat.size(); n++) {

				CADWire expWire = slatCutWire_Slat.get(n);
				double[] box = expWire.boundingBox();
				if (Math.abs(Math.abs(box[1]) - innerFlapStat) < 1.0E-5) {
					Wire_Inner_Flap =  expWire;
				}	

			}
			for(int n = 0; n < slatCutWire_Slat.size(); n++) {

				CADWire expWire = slatCutWire_Slat.get(n);
				double[] box = expWire.boundingBox();
				if (Math.abs(Math.abs(box[1]) - outerFlapStat) < 1.0E-5) {
					Wire_Outer_Flap =  expWire;
				}
			}
			
			CADWire inner_Wire = Wire_Inner_Flap;
			CADWire outer_Wire = Wire_Outer_Flap;
			selectedWires_Flap.add(inner_Wire);
			selectedWires_Flap.add(outer_Wire);
			
			OCCShape Flap = OCCUtils.makePatchThruSections(selectedWires_Flap);
			exportShapes2.add(Flap);

			CADFace innerFace = OCCUtils.theFactory.newFacePlanar(inner_Wire);
			CADFace outerFace = OCCUtils.theFactory.newFacePlanar(outer_Wire);
			exportShapes.add((OCCShape) innerFace);
			exportShapes.add((OCCShape) outerFace);
			exportShapes.add((OCCShape) Flap);
			
			List<CADShape> cadShapeList = new ArrayList<>();
			cadShapeList.add((OCCShape) innerFace);
			cadShapeList.add((OCCShape) outerFace);
			cadShapeList.add((OCCShape) Flap);
			
			selectedWires_Flap.clear();
			 
            sewMakerWing.Init();                        
            sewMakerWing.Add(Flap.getShape());
            sewMakerWing.Add(((OCCShape)innerFace).getShape());
            sewMakerWing.Add(((OCCShape)outerFace).getShape());
            sewMakerWing.Perform();
            TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
 
            System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMakerWing.IsNull());   
            System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Building the solid flap");
            CADSolid solidFlap = null;
            BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
            solidMaker.Add(TopoDS.ToShell(sewedSection));
            solidMaker.Build();
            System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
            solidFlap = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
            slatSolids.add((OCCShape) solidFlap);

			}
			
		BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
        for(int i = 0; i < solidsSectionWing.size(); i++) {
            sewing.Add(solidsSectionWing.get(i).getShape());
        }
        sewing.Add(tds_shape);
        sewing.Perform();
 
        TopoDS_Shape finalWingShape = sewing.SewedShape();
 
        TopExp_Explorer exp = new TopExp_Explorer(finalWingShape, TopAbs_ShapeEnum.TopAbs_SOLID);
        int counter = 1;
        while(exp.More() > 0) {
            counter = counter + 1;
            exp.Next();
        }
        System.out.println("Number of solids in finalWingShape: " + counter);
        System.out.println("Number of solids Flaps : " + slatSolids.size());
        
        // Prova rotazione
        OCCShape rotatedSlat = symFlapRotation(liftingSurface, -0.349, 
				(OCCShape) OCCUtils.theFactory.newShape(finalWingShape), slatSolids.get(0));

     // Mirroring
        List<OCCShape> leftSideFlaps = new ArrayList<>();
        List<OCCShape> leftSideWing = new ArrayList<>();
 
        if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {
            System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Mirroring rigth lofts.");
            gp_Trsf mirrorTransform = new gp_Trsf();
            gp_Ax2 mirrorPointPlane = new gp_Ax2(
                    new gp_Pnt(0.0, 0.0, 0.0),
                    new gp_Dir(0.0, 1.0, 0.0), // Y direction normal to reflection plane XZ
                    new gp_Dir(1.0, 0.0, 0.0)
                    );
            mirrorTransform.SetMirror(mirrorPointPlane);                
            BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
 
            slatSolids.stream()
            .map(occshape -> occshape.getShape())
            .forEach(s -> {
                mirrorBuilder.Perform(s, 1);
                TopoDS_Shape sMirrored = mirrorBuilder.Shape();
                leftSideFlaps.add(
                        (OCCShape)OCCUtils.theFactory.newShape(sMirrored)
                        );
            });
            mirrorBuilder.Perform(((OCCShape) OCCUtils.theFactory.newShape(finalWingShape)).getShape(),1);
            TopoDS_Shape mirroredWing = mirrorBuilder.Shape();
            leftSideWing.add(
                    (OCCShape)OCCUtils.theFactory.newShape(mirroredWing)
                    );
 
            System.out.println("Mirrored shapes: " + leftSideFlaps.size());
            System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Exporting mirrored sewed loft.");
        }           
        
		String fileName = "Test15as.brep";
		if(OCCUtils.write(fileName, exportInnerCutSlat))
			System.out.println("========== [main] Output written on file: " + fileName);
		if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {  
//          OCCUtils.write(fileName, Flapsolids.get(1),(OCCShape) OCCUtils.theFactory.newShape(finalWingShape),leftSideFlaps,leftSideWing,(OCCShape) OCCUtils.theFactory.newShape(rotatedFlap));
            OCCUtils.write(fileName,rotatedSlat,(OCCShape) OCCUtils.theFactory.newShape(finalWingShape), slatSolids.get(1));
//          OCCUtils.write(fileName, (OCCShape) OCCUtils.theFactory.newShape(auxCutWing));
 
 
        }
		else {
			OCCUtils.write(fileName,exportClosedShapes);
        }



	}
	
	public static OCCShape symFlapRotation(LiftingSurface liftingSurface, double deflection, 
			OCCShape wing, OCCShape flap)	{
			
		OCCShape rotatedFlap = null;
				
		CADGeomCurve3D innerChord =  getChordSegmentAtYActual(flap.boundingBox()[1], liftingSurface);
		double innerChordLength = innerChord.length();
		CADGeomCurve3D outerChord =  getChordSegmentAtYActual(flap.boundingBox()[4], liftingSurface);
		double outerChordLength = outerChord.length();

		
		gp_Pnt innerHingePos = new gp_Pnt();
		gp_Pnt outerHingePos = new gp_Pnt();
		
		double innerHingePntx = 0.15;
		double innerHingePntz = -0.15;
		
		double outerHingePntx = 0.15;
		double outerHingePntz = -0.15;
		
		innerHingePos.SetX((innerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
		innerHingePos.SetY(flap.boundingBox()[1]);
		innerHingePos.SetZ((innerChordLength * innerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

		outerHingePos.SetX((outerChordLength * outerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[4]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
		outerHingePos.SetY(flap.boundingBox()[4]);
		outerHingePos.SetZ((outerChordLength * outerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));
		
		System.out.println("Coordinate cerniera interna : " + innerHingePos.X() + " " + innerHingePos.Y() + " " + innerHingePos.Z());
		System.out.println("Coordinate cerniera esterna : " + outerHingePos.X() + " " + outerHingePos.Y() + " " + outerHingePos.Z());

		gp_Vec hingeVec = new gp_Vec(innerHingePos, outerHingePos);
		gp_Dir hingeDir = new gp_Dir(hingeVec);
		gp_Ax1 hingeAxis = new gp_Ax1(innerHingePos, hingeDir);
		gp_Trsf rotation = new gp_Trsf();
		
		rotation.SetRotation(hingeAxis, deflection);
		BRepBuilderAPI_Transform transf = new BRepBuilderAPI_Transform(flap.getShape(), rotation);
		TopoDS_Shape rotatedFlapShape = transf.Shape();
		
		rotatedFlap = (OCCShape) OCCUtils.theFactory.newShape(rotatedFlapShape);
		
		return rotatedFlap;
		
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
	
    private static CADGeomCurve3D[] createVerCrvsForTipClosure( 
            List<OCCEdge> tipAirfoil,
            List<OCCEdge> preTipAirfoil,
            PVector[] leVec,
            PVector[] teVec,
            double[] guideCrvPnt
            ) {     
 
        CADGeomCurve3D[] verSecCrvsList = new CADGeomCurve3D[2];
 
        PVector le1 = leVec[0];
        PVector le2 = leVec[1];
        PVector te1 = teVec[0];
        PVector te2 = teVec[1];
 
        // getting points on the tip airfoil    
        double[] tipAirfoilUppVtx = tipAirfoil.get(0).vertices()[0].pnt();          
        double[] tipAirfoilLowVtx = tipAirfoil.get(1).vertices()[1].pnt();
 
        // tangent vectors calculation
        double[] preTipAirfoilUppVtx = preTipAirfoil.get(0).vertices()[0].pnt();                
        double[] preTipAirfoilLowVtx = preTipAirfoil.get(1).vertices()[1].pnt();
 
        PVector[] tanVecs = new PVector[2];
        tanVecs[0] = PVector.sub(
                new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2]),
                new PVector((float) preTipAirfoilUppVtx[0], (float) preTipAirfoilUppVtx[1], (float) preTipAirfoilUppVtx[2])
                ).normalize();
        tanVecs[1] = PVector.sub(
                new PVector((float) tipAirfoilLowVtx[0], (float) tipAirfoilLowVtx[1], (float) tipAirfoilLowVtx[2]),
                new PVector((float) preTipAirfoilLowVtx[0], (float) preTipAirfoilLowVtx[1], (float) preTipAirfoilLowVtx[2])
                ).normalize();
 
        // weights for the tangent constraints
        PVector thickness = PVector.sub(
                new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2]),
                new PVector((float) tipAirfoilLowVtx[0], (float) tipAirfoilLowVtx[1], (float) tipAirfoilLowVtx[2])
                );
 
        double thickUpp = thickness.mag()/2;
        double thickLow = thickUpp;
        double crvHeight = PVector.sub(
                new PVector((float) guideCrvPnt[0], (float) guideCrvPnt[1], (float) guideCrvPnt[2]), 
                new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2])
                ).mag();
 
        double tanUppVSecCrvFac = 1;
        double tanLowVSecCrvFac = 1*(-1);       
        double tanUppHalfVSecCrvFac = Math.pow(thickUpp/crvHeight, 0.60)*(-1); //TODO: eventually make this a parameter
        double tanLowHalfVSecCrvFac = Math.pow(thickLow/crvHeight, 0.60)*(-1);
 
        // section curves apex tangent vector
        PVector chordTipVector = PVector.sub(te2, le2);
        PVector leVector = PVector.sub(le2, le1);
        PVector constrCrvApexTan = new PVector();
        PVector.cross(chordTipVector, leVector, constrCrvApexTan).normalize();
 
        // vertical section curves creation
        List<double[]> verSecCrvUppPnts = new ArrayList<>();
        List<double[]> verSecCrvLowPnts = new ArrayList<>();
 
        thickness.normalize();
 
        verSecCrvUppPnts.add(tipAirfoilUppVtx);
        verSecCrvUppPnts.add(guideCrvPnt);
        verSecCrvLowPnts.add(guideCrvPnt);
        verSecCrvLowPnts.add(tipAirfoilLowVtx);
 
        double[] tanUppVSecCrv = MyArrayUtils.scaleArray(
                new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, 
                tanUppVSecCrvFac
                );  
        double[] tanUppHalfVSecCrv = MyArrayUtils.scaleArray(
                new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
                tanUppHalfVSecCrvFac
                );
        double[] tanLowHalfVSecCrv = MyArrayUtils.scaleArray(
                new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
                tanLowHalfVSecCrvFac
                );
        double[] tanLowVSecCrv = MyArrayUtils.scaleArray(
                new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z},
                tanLowVSecCrvFac
                );
 
        CADGeomCurve3D verSecCrvUpp = OCCUtils.theFactory.newCurve3D(
                verSecCrvUppPnts, 
                false, 
                tanUppVSecCrv, 
                tanUppHalfVSecCrv, 
                false
                );
        CADGeomCurve3D verSecCrvLow = OCCUtils.theFactory.newCurve3D(
                verSecCrvLowPnts, 
                false, 
                tanLowHalfVSecCrv, 
                tanLowVSecCrv, 
                false
                );
 
        verSecCrvsList[0] = verSecCrvUpp;
        verSecCrvsList[1] = verSecCrvLow;
 
        return verSecCrvsList;
    }
	
    private static CADGeomCurve3D[] createVerCrvsForTipClosure(
            LiftingSurface theLiftingSurface, 
            List<OCCEdge> tipAirfoil,
            List<OCCEdge> preTipAirfoil,
            PVector[] leVec,
            PVector[] teVec,
            double chordFrac,
            double[] guideCrvPnt
            ) {
 
        CADGeomCurve3D[] verSecCrvsList = new CADGeomCurve3D[2];
 
        int iTip = theLiftingSurface.getAirfoilList().size() - 1;
        float tipChordLength = (float) theLiftingSurface.getPanels()
                .get(theLiftingSurface.getPanels().size()-1).getChordTip().doubleValue(SI.METER);
        float preTipChordLength = (float) theLiftingSurface
                .getChordsBreakPoints().get(iTip-1).doubleValue(SI.METER);
 
        PVector le1 = leVec[0];
        PVector le2 = leVec[1];
        PVector te1 = teVec[0];
        PVector te2 = teVec[1];
 
        // creating vertical splitting vectors for the tip airfoil curve, orthogonal to the chord
        PVector leVector = PVector.sub(le2, le1);
        PVector axisVector; 
        PVector chordTipVector = PVector.sub(te2, le2); // vector in the airfoil plane  
        PVector chordTipNVector = new PVector();
        PVector constrCrvApexTan = new PVector();
        PVector.cross(chordTipVector, leVector, constrCrvApexTan).normalize();
        //      PVector.cross(chordTipVector, leVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
        if(!theLiftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL))
            axisVector = new PVector(0, 1, 0);
        else
            axisVector = new PVector(0, 0, 1);
        PVector.cross(chordTipVector, axisVector, chordTipNVector).normalize(); // vector in the airfoil plane, normal to the chord, normalized
 
        // creating vertical splitting vectors for the second to last airfoil curve
        PVector chordPreTipVector = PVector.sub(te1, le1);  
        PVector chordPreTipNVector = new PVector();
        //      PVector.cross(chordPreTipVector, leVector, chordPreTipNVector).normalize(); 
        PVector.cross(chordPreTipVector, axisVector, chordPreTipNVector).normalize();
 
        // getting points onto the tip airfoil
        PVector pntOnTipChord = PVector.lerp(le2, te2, (float) chordFrac); // tip chord fraction point
 
        Double[] tipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
                theLiftingSurface.getAirfoilList().get(iTip),
                chordFrac
                );
 
        PVector pntOnTipAirfoilUCrv = PVector.add(
                pntOnTipChord, 
                PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[0].floatValue())*tipChordLength)
                );          
        PVector pntOnTipAirfoilLCrv = PVector.add(
                pntOnTipChord, 
                PVector.mult(chordTipNVector, (tipAirfoilThickAtPnt[1].floatValue())*tipChordLength)
                );
 
        double[] tipAirfoilUppVtx = OCCUtils.pointProjectionOnCurve0(
                OCCUtils.theFactory.newCurve3D(tipAirfoil.get(0)), 
                new double[] {pntOnTipAirfoilUCrv.x, pntOnTipAirfoilUCrv.y, pntOnTipAirfoilUCrv.z}
                ).pnt();            
        double[] tipAirfoilLowVtx = OCCUtils.pointProjectionOnCurve0(
                OCCUtils.theFactory.newCurve3D(tipAirfoil.get(1)),
                new double[] {pntOnTipAirfoilLCrv.x, pntOnTipAirfoilLCrv.y, pntOnTipAirfoilLCrv.z}
                ).pnt();
 
        // tangent vectors calculation
        PVector pntOnPreTipChord = PVector.lerp(le1, te1, (float) chordFrac);
 
        Double[] preTipAirfoilThickAtPnt = AircraftUtils.getThicknessAtX(
                theLiftingSurface.getAirfoilList().get(iTip-1), 
                chordFrac
                );
 
        PVector pntOnPreTipAirfoilUCrv = PVector.add(
                pntOnPreTipChord, 
                PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[0].floatValue())*preTipChordLength)
                );          
        PVector pntOnPreTipAirfoilLCrv = PVector.add(
                pntOnPreTipChord, 
                PVector.mult(chordPreTipNVector, (preTipAirfoilThickAtPnt[1].floatValue())*preTipChordLength)
                );
 
        double[] preTipAirfoilUppVtx = OCCUtils.pointProjectionOnCurve0(
                (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(preTipAirfoil.get(0)),
                new double[] {pntOnPreTipAirfoilUCrv.x, pntOnPreTipAirfoilUCrv.y, pntOnPreTipAirfoilUCrv.z}
                ).pnt();            
        double[] preTipAirfoilLowVtx = OCCUtils.pointProjectionOnCurve0(
                (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(preTipAirfoil.get(1)),
                new double[] {pntOnPreTipAirfoilLCrv.x, pntOnPreTipAirfoilLCrv.y, pntOnPreTipAirfoilLCrv.z}
                ).pnt();
 
        PVector[] tanVecs = new PVector[2];
        tanVecs[0] = PVector.sub(
                new PVector((float) tipAirfoilUppVtx[0], (float) tipAirfoilUppVtx[1], (float) tipAirfoilUppVtx[2]),
                new PVector((float) preTipAirfoilUppVtx[0], (float) preTipAirfoilUppVtx[1], (float) preTipAirfoilUppVtx[2])
                ).normalize();
        tanVecs[1] = PVector.sub(
                new PVector((float) tipAirfoilLowVtx[0], (float) tipAirfoilLowVtx[1], (float) tipAirfoilLowVtx[2]),
                new PVector((float) preTipAirfoilLowVtx[0], (float) preTipAirfoilLowVtx[1], (float) preTipAirfoilLowVtx[2])
                ).normalize();
 
        // weights for the tangent constraints
        double thickUpp = PVector.sub(pntOnTipAirfoilUCrv, pntOnTipChord).mag();
        double thickLow = PVector.sub(pntOnTipAirfoilLCrv, pntOnTipChord).mag();
        double crvHeight = PVector.sub(
                new PVector(
                        (float) guideCrvPnt[0], 
                        (float) guideCrvPnt[1], 
                        (float) guideCrvPnt[2]), 
                pntOnTipChord).mag(); 
 
        double tanUppVSecCrvFac = 1;
        double tanLowVSecCrvFac = 1*(-1);       
        double tanUppHalfVSecCrvFac = Math.pow(thickUpp/crvHeight, 0.60)*(-1); //TODO: eventually make this a parameter
        double tanLowHalfVSecCrvFac = Math.pow(thickLow/crvHeight, 0.60)*(-1);
 
        // vertical section curves creation
        List<double[]> verSecCrvUppPnts = new ArrayList<>();
        List<double[]> verSecCrvLowPnts = new ArrayList<>();
 
        verSecCrvUppPnts.add(tipAirfoilUppVtx);
        verSecCrvUppPnts.add(guideCrvPnt);
        verSecCrvLowPnts.add(guideCrvPnt);
        verSecCrvLowPnts.add(tipAirfoilLowVtx);
 
        double[] tanUppVSecCrv = MyArrayUtils.scaleArray(
                new double[] {tanVecs[0].x, tanVecs[0].y, tanVecs[0].z}, 
                tanUppVSecCrvFac
                );  
        double[] tanUppHalfVSecCrv = MyArrayUtils.scaleArray(
                new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
                tanUppHalfVSecCrvFac
                );
        double[] tanLowHalfVSecCrv = MyArrayUtils.scaleArray(
                new double[] {constrCrvApexTan.x, constrCrvApexTan.y, constrCrvApexTan.z}, 
                tanLowHalfVSecCrvFac
                );
        double[] tanLowVSecCrv = MyArrayUtils.scaleArray(
                new double[] {tanVecs[1].x, tanVecs[1].y, tanVecs[1].z},
                tanLowVSecCrvFac
                );
 
        CADGeomCurve3D verSecCrvUpp = OCCUtils.theFactory.newCurve3D(
                verSecCrvUppPnts, 
                false, 
                tanUppVSecCrv, 
                tanUppHalfVSecCrv, 
                false
                );
        CADGeomCurve3D verSecCrvLow = OCCUtils.theFactory.newCurve3D(
                verSecCrvLowPnts, 
                false, 
                tanLowHalfVSecCrv, 
                tanLowVSecCrv, 
                false
                );
 
        verSecCrvsList[0] = verSecCrvUpp;
        verSecCrvsList[1] = verSecCrvLow;       
 
        return verSecCrvsList;
    }
     
    public static List<TopoDS_Solid> performBooleanCutOperation(
            Tuple2<TopoDS_Solid, Boolean> firstSolid, 
            Tuple2<TopoDS_Solid, Boolean> secondSolid) {
 
        List<TopoDS_Solid> cutSolid = new ArrayList<>();
 
        TopoDS_Solid solid1 = firstSolid._2()? 
                TopoDS.ToSolid(firstSolid._1().Complemented()) : firstSolid._1();
        TopoDS_Solid solid2 = secondSolid._2()? 
                TopoDS.ToSolid(secondSolid._1().Complemented()) : secondSolid._1();
 
        BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(solid1, solid2);   
//      cutter.SetFuzzyValue(1e-5);
        TopoDS_Shape cutResult = cutter.Shape();                                            
        TopExp_Explorer exp = new TopExp_Explorer(cutResult, TopAbs_ShapeEnum.TopAbs_SOLID);
        while(exp.More() > 0) {
            cutSolid.add(TopoDS.ToSolid(exp.Current()));
            exp.Next();
        }
 
        return cutSolid;
    }

}
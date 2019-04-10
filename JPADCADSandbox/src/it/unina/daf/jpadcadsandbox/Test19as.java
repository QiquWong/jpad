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
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCCompSolid;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
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
import opencascade.ShapeAnalysis_FreeBounds;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.TopoDS_Solid;
import opencascade.TopoDS_Wire;
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

public class Test19as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test19as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface liftingSurface = aircraft.getWing();
		// Wing and movable surfaces data

		double lateralGap = 0.03;
		boolean horn = false;
		double wingSemiSpan = liftingSurface.getSemiSpan().doubleValue(SI.METER);

		List<double[]> nonSymFlapStations = new ArrayList<>();
		List<double[]> nonSymFlapChordRatios = new ArrayList<>();
		int numNonSymFlap = 2;
		nonSymFlapStations.add(new double[] {
				0.08,
				0.31
		});
		nonSymFlapStations.add(new double[] {
				0.35,
				0.75
		});
		nonSymFlapChordRatios.add(new double[] {
				0.35,
				0.31
		});
		nonSymFlapChordRatios.add(new double[] {
				0.35,
				0.31
		});

		List<double[]> symFlapStations = new ArrayList<>();
		List<double[]> symFlapChordRatios = new ArrayList<>();
		int numSymFlap = 0;
		//		symFlapStations.add(new double[] {
		//				0.05,
		//				0.91
		//		});
		//		symFlapChordRatios.add(new double[] {
		//				0.40,
		//				0.40
		//		});

		List<double[]> slatStations = new ArrayList<>();
		List<double[]> slatChordRatios = new ArrayList<>();
		int numSlat = 1;
		slatStations.add(new double[] {
				0.08,
				0.31
		});
		slatChordRatios.add(new double[] {
				0.30,
				0.26
		});

		System.out.println("Wing semispan" + wingSemiSpan + " m");
		System.out.println("Flap lateral gap: " + lateralGap + " m");
		System.out.println("Number of flaps: " + liftingSurface.getSymmetricFlaps().size());
		System.out.println("Symmetric flaps spanwise position:");
		nonSymFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Symmetric flaps chord ratios:");
		nonSymFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));

		double adjustmentPar = 0.01;
		List<Double> etaBreakPnts = liftingSurface.getEtaBreakPoints();
		System.out.println("Lifting surface eta breakpoints: " + 
				Arrays.toString(etaBreakPnts.toArray(new Double[etaBreakPnts.size()])));

		for(int i = 0; i < numNonSymFlap; i++) {
			double etaInn = nonSymFlapStations.get(i)[0];
			double etaOut = nonSymFlapStations.get(i)[1];
			double innChordRatio = nonSymFlapChordRatios.get(i)[0];
			double outChordRatio = nonSymFlapChordRatios.get(i)[1];

			for(int j = 0; j < etaBreakPnts.size()-1; j++) {

				if(etaInn >= etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								nonSymFlapStations.get(i), 
								nonSymFlapChordRatios.get(i), 
								etaInn
								);
					}

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								nonSymFlapStations.get(i), 
								nonSymFlapChordRatios.get(i), 
								etaOut
								);
					}

					nonSymFlapStations.set(i, new double[] {etaInn, etaOut});
					nonSymFlapChordRatios.set(i, new double[] {innChordRatio, outChordRatio});				
				}

				if((etaInn >= etaBreakPnts.get(j) && etaInn < etaBreakPnts.get(j+1)) 
						&& etaOut > etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								nonSymFlapStations.get(i), 
								nonSymFlapChordRatios.get(i), 
								etaInn
								);
					}

					etaOut = etaBreakPnts.get(j+1) - adjustmentPar;
					outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							nonSymFlapStations.get(i), 
							nonSymFlapChordRatios.get(i), 
							etaOut
							);

					nonSymFlapStations.set(i, new double[] {etaInn, etaOut});
					nonSymFlapChordRatios.set(i, new double[] {innChordRatio, outChordRatio});

					double[] nextStations = nonSymFlapStations.get(i+1);
					double[] nextChordRatios = nonSymFlapChordRatios.get(i+1);

					double etaInnNext = etaBreakPnts.get(j+1) + adjustmentPar; 
					double innChordRatioNext = MyMathUtils.getInterpolatedValue1DLinear(
							nextStations, 
							nextChordRatios, 
							etaInnNext
							);

					nonSymFlapStations.set(i+1, new double[] {etaInnNext, nextStations[1]});
					nonSymFlapChordRatios.set(i+1, new double[] {innChordRatioNext, nextChordRatios[1]});
				}

				if(etaInn < etaBreakPnts.get(j) && 
						(etaOut > etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1))) {

					etaInn = etaBreakPnts.get(j) + adjustmentPar;
					innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							nonSymFlapStations.get(i), 
							nonSymFlapChordRatios.get(i), 
							etaInn
							);

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								nonSymFlapStations.get(i), 
								nonSymFlapChordRatios.get(i), 
								etaOut
								);
					}

					nonSymFlapStations.set(i, new double[] {etaInn, etaOut});
					nonSymFlapChordRatios.set(i, new double[] {innChordRatio, outChordRatio});

					double[] prevStations = nonSymFlapStations.get(i-1);
					double[] prevChordRatios = nonSymFlapChordRatios.get(i-1);

					double etaOutPrev = etaBreakPnts.get(j) - adjustmentPar; 
					double outChordRatioPrev = MyMathUtils.getInterpolatedValue1DLinear(
							prevStations, 
							prevChordRatios, 
							etaOutPrev
							);

					nonSymFlapStations.set(i-1, new double[] {prevStations[0], etaOutPrev});
					nonSymFlapChordRatios.set(i-1, new double[] {prevChordRatios[0], outChordRatioPrev});
				}
			}
		}

		System.out.println("Non symmetric flaps adjusted spanwise position:");
		nonSymFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Non symmetric flaps adjusted chord ratios:");
		nonSymFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));

		for(int i = 0; i < numSymFlap; i++) {
			double etaInn = symFlapStations.get(i)[0];
			double etaOut = symFlapStations.get(i)[1];
			double innChordRatio = symFlapChordRatios.get(i)[0];
			double outChordRatio = symFlapChordRatios.get(i)[1];

			for(int j = 0; j < etaBreakPnts.size()-1; j++) {

				if(etaInn >= etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								symFlapStations.get(i), 
								symFlapChordRatios.get(i), 
								etaInn
								);
					}

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								symFlapStations.get(i), 
								symFlapChordRatios.get(i), 
								etaOut
								);
					}

					symFlapStations.set(i, new double[] {etaInn, etaOut});
					symFlapChordRatios.set(i, new double[] {innChordRatio, outChordRatio});				
				}

				if((etaInn >= etaBreakPnts.get(j) && etaInn < etaBreakPnts.get(j+1)) 
						&& etaOut > etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								symFlapStations.get(i), 
								symFlapChordRatios.get(i), 
								etaInn
								);
					}

					etaOut = etaBreakPnts.get(j+1) - adjustmentPar;
					outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							symFlapStations.get(i), 
							symFlapChordRatios.get(i), 
							etaOut
							);

					symFlapStations.set(i, new double[] {etaInn, etaOut});
					symFlapChordRatios.set(i, new double[] {innChordRatio, outChordRatio});

					double[] nextStations = symFlapStations.get(i+1);
					double[] nextChordRatios = symFlapChordRatios.get(i+1);

					double etaInnNext = etaBreakPnts.get(j+1) + adjustmentPar; 
					double innChordRatioNext = MyMathUtils.getInterpolatedValue1DLinear(
							nextStations, 
							nextChordRatios, 
							etaInnNext
							);

					symFlapStations.set(i+1, new double[] {etaInnNext, nextStations[1]});
					symFlapChordRatios.set(i+1, new double[] {innChordRatioNext, nextChordRatios[1]});
				}

				if(etaInn < etaBreakPnts.get(j) && 
						(etaOut > etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1))) {

					etaInn = etaBreakPnts.get(j) + adjustmentPar;
					innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							symFlapStations.get(i), 
							symFlapChordRatios.get(i), 
							etaInn
							);

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								symFlapStations.get(i), 
								symFlapChordRatios.get(i), 
								etaOut
								);
					}

					symFlapStations.set(i, new double[] {etaInn, etaOut});
					symFlapChordRatios.set(i, new double[] {innChordRatio, outChordRatio});

					double[] prevStations = symFlapStations.get(i-1);
					double[] prevChordRatios = symFlapChordRatios.get(i-1);

					double etaOutPrev = etaBreakPnts.get(j) - adjustmentPar; 
					double outChordRatioPrev = MyMathUtils.getInterpolatedValue1DLinear(
							prevStations, 
							prevChordRatios, 
							etaOutPrev
							);

					symFlapStations.set(i-1, new double[] {prevStations[0], etaOutPrev});
					symFlapChordRatios.set(i-1, new double[] {prevChordRatios[0], outChordRatioPrev});
				}
			}
		}

		System.out.println("Symmetric flaps adjusted spanwise position:");
		symFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Symmetric flaps adjusted chord ratios:");
		symFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));

		for(int i = 0; i < numSlat; i++) {
			double etaInn = slatStations.get(i)[0];
			double etaOut = slatStations.get(i)[1];
			double innChordRatio = slatChordRatios.get(i)[0];
			double outChordRatio = slatChordRatios.get(i)[1];

			for(int j = 0; j < etaBreakPnts.size()-1; j++) {

				if(etaInn >= etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatios.get(i), 
								etaInn
								);
					}

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatios.get(i), 
								etaOut
								);
					}

					slatStations.set(i, new double[] {etaInn, etaOut});
					slatChordRatios.set(i, new double[] {innChordRatio, outChordRatio});				
				}

				if((etaInn >= etaBreakPnts.get(j) && etaInn < etaBreakPnts.get(j+1)) 
						&& etaOut > etaBreakPnts.get(j+1)) {

					if(Math.abs(etaInn - etaBreakPnts.get(j)) < 1e-5) {
						etaInn += adjustmentPar;
						innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatios.get(i), 
								etaInn
								);
					}

					etaOut = etaBreakPnts.get(j+1) - adjustmentPar;
					outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							slatStations.get(i), 
							slatChordRatios.get(i), 
							etaOut
							);

					slatStations.set(i, new double[] {etaInn, etaOut});
					slatChordRatios.set(i, new double[] {innChordRatio, outChordRatio});

					double[] nextStations = slatStations.get(i+1);
					double[] nextChordRatios = slatChordRatios.get(i+1);

					double etaInnNext = etaBreakPnts.get(j+1) + adjustmentPar; 
					double innChordRatioNext = MyMathUtils.getInterpolatedValue1DLinear(
							nextStations, 
							nextChordRatios, 
							etaInnNext
							);

					slatStations.set(i+1, new double[] {etaInnNext, nextStations[1]});
					slatChordRatios.set(i+1, new double[] {innChordRatioNext, nextChordRatios[1]});
				}

				if(etaInn < etaBreakPnts.get(j) && 
						(etaOut > etaBreakPnts.get(j) && etaOut <= etaBreakPnts.get(j+1))) {

					etaInn = etaBreakPnts.get(j) + adjustmentPar;
					innChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
							slatStations.get(i), 
							slatChordRatios.get(i), 
							etaInn
							);

					if(Math.abs(etaOut - etaBreakPnts.get(j+1)) < 1e-5) {
						etaOut -= adjustmentPar;
						outChordRatio = MyMathUtils.getInterpolatedValue1DLinear(
								slatStations.get(i), 
								slatChordRatios.get(i), 
								etaOut
								);
					}

					slatStations.set(i, new double[] {etaInn, etaOut});
					slatChordRatios.set(i, new double[] {innChordRatio, outChordRatio});

					double[] prevStations = slatStations.get(i-1);
					double[] prevChordRatios = slatChordRatios.get(i-1);

					double etaOutPrev = etaBreakPnts.get(j) - adjustmentPar; 
					double outChordRatioPrev = MyMathUtils.getInterpolatedValue1DLinear(
							prevStations, 
							prevChordRatios, 
							etaOutPrev
							);

					slatStations.set(i-1, new double[] {prevStations[0], etaOutPrev});
					slatChordRatios.set(i-1, new double[] {prevChordRatios[0], outChordRatioPrev});
				}
			}
		}

		System.out.println("Slats adjusted spanwise position:");
		slatStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Slats adjusted chord ratios:");
		slatChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));

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

		// Airfoils at symmetric flap breakpoints

		List<Amount<Length>> symFlapBreakPoints = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList_SymFlap = new ArrayList<CADGeomCurve3D>();

		for(int j = 0; j < numSymFlap; j++) {
			double etaInnerFlap = symFlapStations.get(j)[0];
			double etaOuterFlap = symFlapStations.get(j)[1];
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
			symFlapBreakPoints.add(Amount.valueOf(symFlapStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
			symFlapBreakPoints.add(Amount.valueOf(symFlapStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));

			for(int k = 0; k < symFlapBreakPoints.size(); k++) {

				Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[k]);
				List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
						symFlapBreakPoints.get(k).doubleValue(SI.METER), 
						airfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
				cadCurveAirfoilBPList_SymFlap.add(cadCurveAirfoil);
			}

			cadCurveAirfoilBPList_SymFlap.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
			cadCurveAirfoilBPList_SymFlap.clear();
			//			cadCurveAirfoilList.add(cadCurveAirfoilBPList_Flap);
			symFlapBreakPoints.clear();
		}

		// Airfoils at non symmetric flap breakpoints

		List<Amount<Length>> nonSymFlapBreakPoints = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList_NonSymFlap = new ArrayList<CADGeomCurve3D>();

		for(int j = 0; j < numNonSymFlap; j++) {
			double etaInnerFlap = nonSymFlapStations.get(j)[0];
			double etaOuterFlap = nonSymFlapStations.get(j)[1];
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
			nonSymFlapBreakPoints.add(Amount.valueOf(nonSymFlapStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
			nonSymFlapBreakPoints.add(Amount.valueOf(nonSymFlapStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));

			for(int k = 0; k < nonSymFlapBreakPoints.size(); k++) {

				Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[k]);
				List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
						nonSymFlapBreakPoints.get(k).doubleValue(SI.METER), 
						airfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
				cadCurveAirfoilBPList_NonSymFlap.add(cadCurveAirfoil);
			}

			cadCurveAirfoilBPList_NonSymFlap.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
			cadCurveAirfoilBPList_NonSymFlap.clear();
			nonSymFlapBreakPoints.clear();
		}
		// Airfoils at slat breakpoints
		//
		//				List<Amount<Length>> slatBreakPoints = new ArrayList<>();
		//				List<CADGeomCurve3D> cadCurveAirfoilBPList_Slat = new ArrayList<CADGeomCurve3D>();
		//
		//				for(int j = 0; j < numSlat; j++) {
		//					double etaInnerFlap = slatStations.get(j)[0];
		//					double etaOuterFlap = slatStations.get(j)[1];
		//					// for breakpoints
		//					int innerPanel = 0;
		//					int outerPanel = 0;
		//					for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {
		//
		//						if(etaInnerFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
		//								etaInnerFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
		//							innerPanel = n;
		//						}
		//
		//						if(etaOuterFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
		//								etaOuterFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
		//							outerPanel = n;
		//						}
		//					}
		//					int[] intArray = new int[] {innerPanel, outerPanel};
		//					slatBreakPoints.add(Amount.valueOf(slatStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
		//					slatBreakPoints.add(Amount.valueOf(slatStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
		//
		//					for(int k = 0; k < slatBreakPoints.size(); k++) {
		//
		//						Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[k]);
		//						List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
		//								slatBreakPoints.get(k).doubleValue(SI.METER), 
		//								airfoilCoords, 
		//								liftingSurface
		//								);
		//						CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
		//						cadCurveAirfoilBPList_Slat.add(cadCurveAirfoil);
		//					}
		//
		//					cadCurveAirfoilBPList_Slat.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
		//					cadCurveAirfoilBPList_Slat.clear();
		//					slatBreakPoints.clear();
		//				}

		List<Amount<Length>> cutSymFlapBreakPoints = new ArrayList<>();
		List<OCCShape> exportCutSymFlap = new ArrayList<>();
		List<OCCShape> exportSymFlap = new ArrayList<>();
		List<CADWire> symFlapCutWire = new ArrayList<>();
		List<CADWire> symFlapCutWire_Flap = new ArrayList<>();
		List<OCCShape> Flapsolids = new ArrayList<>();
		List<OCCShape> hornFlap = new ArrayList<>();
		List<OCCShape> solidsSectionWing = new ArrayList<>();

		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < numSymFlap; j++) {

				double etaInnerFlap = symFlapStations.get(j)[0];
				double etaOuterFlap = symFlapStations.get(j)[1];
				double ChordRatio = symFlapChordRatios.get(j)[i];
				double k1 = 0.05; // gap factor
				double k2 = 0.08; // airfoil trailing edge factor
				double k3 = 3.0; // flap leading edge factor
				double cGap = k1 * ChordRatio; // flap gap 
				double deltaCGap1 = k2 * cGap; // airfoil TE
				double deltaCGap2 = k3 * cGap; // flap LE

				int innerPanel = 0;
				int outerPanel = 0;
				for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

					if(etaInnerFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && etaInnerFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
						innerPanel = n;
					}

					if(etaOuterFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && etaOuterFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
						outerPanel = n;
					}
				}
				int[] intArray = new int[] {innerPanel, outerPanel};
				cutSymFlapBreakPoints.add(Amount.valueOf(symFlapStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
				cutSymFlapBreakPoints.add( Amount.valueOf(symFlapStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));

				Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[i]);
				System.out.println("Starting symmetric procedure...");
				List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
						cutSymFlapBreakPoints.get(i).doubleValue(SI.METER), 
						airfoilCoords, 
						liftingSurface
						);

				CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
				CADEdge edgeAirfoil = cadCurveAirfoil.edge();
				CADGeomCurve3D chord = null;
				if(i == 0) {
					chord = getChordSegmentAtYActual(etaInnerFlap*wingSemiSpan, liftingSurface);
				}
				else {
					chord = getChordSegmentAtYActual(etaOuterFlap*wingSemiSpan, liftingSurface);
				}

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
						(ChordRatio + cGap - deltaCGap1) * chordLength, 
						chord_edge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
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
						(ChordRatio + cGap - deltaCGap1) * chordLength, 
						chord_edge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
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
						(ChordRatio + cGap) * chordLength, 
						chord_edge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
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
						(ChordRatio + cGap) * chordLength, 
						chord_edge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
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
				gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
						new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
						gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
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
														(ChordRatio + cGap ) * chordLength)).Edge(), 
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
										ChordRatio * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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
										ChordRatio * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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
										(ChordRatio - deltaCGap2) * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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
										(ChordRatio - deltaCGap2) * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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
														ChordRatio * chordLength)).Edge(), 
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

								TopoDS_Edge flapTE = new TopoDS_Edge();
								gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(flapFirstCut));
								gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(flapSecondCut));
								BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
								flapTE = buildFlapTE.Edge();

								gp_Trsf flapTrasl = new gp_Trsf();

								if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {

									if( i == 0 ) {
										flapTrasl.SetTranslation(new gp_Pnt(0, 0, etaInnerFlap*wingSemiSpan), new gp_Pnt(0, 0, etaInnerFlap * wingSemiSpan + lateralGap));
									}
									else	{

										if( j != numSymFlap-1) {
											flapTrasl.SetTranslation(new gp_Pnt(0, 0, etaOuterFlap*wingSemiSpan), new gp_Pnt(0, 0, etaOuterFlap * wingSemiSpan - lateralGap));
										}

										if (j == numSymFlap-1) {
											if( horn == true) {
												flapTrasl.SetTranslation(new gp_Pnt(0, 0,etaOuterFlap*wingSemiSpan), new gp_Pnt(0, 0,etaOuterFlap * wingSemiSpan));

											}

											else {
												flapTrasl.SetTranslation(new gp_Pnt(0,0,etaOuterFlap*wingSemiSpan), new gp_Pnt(0, 0,etaOuterFlap * wingSemiSpan - lateralGap));
											}				
										}
									}

								}	
								else {
									if ( i == 0 ) {

										flapTrasl.SetTranslation(new gp_Pnt(0, etaInnerFlap*wingSemiSpan, 0), new gp_Pnt(0, etaInnerFlap * wingSemiSpan + lateralGap, 0));

									}


									else {
										if( j != numSymFlap-1) {
											flapTrasl.SetTranslation(new gp_Pnt(0, etaOuterFlap*wingSemiSpan, 0), new gp_Pnt(0, etaOuterFlap * wingSemiSpan - lateralGap, 0));
										}

										if (j == numSymFlap-1) {
											if( horn == true) {
												flapTrasl.SetTranslation(new gp_Pnt(0, etaOuterFlap*wingSemiSpan,0), new gp_Pnt(0, etaOuterFlap * wingSemiSpan,0));

											}

											else {
												flapTrasl.SetTranslation(new gp_Pnt(0,etaOuterFlap*wingSemiSpan,0), new gp_Pnt(0, etaOuterFlap * wingSemiSpan - lateralGap,0));
											}				
										}

									}
								}

								TopoDS_Edge flapUpperLE_Edge = TopoDS.ToEdge(((OCCShape) flapUpperLE).getShape());
								TopoDS_Edge flapLowerLE_Edge = TopoDS.ToEdge(((OCCShape) flapLowerLE).getShape());			
								TopoDS_Edge airfoilUpperTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilUpperTE).getShape());
								TopoDS_Edge airfoilLowerTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLowerTE).getShape());
								TopoDS_Edge airfoilMiddleUpperTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilMiddleUpperTE).getShape());
								TopoDS_Edge airfoilMiddleLowerTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilMiddleLowerTE).getShape());

								TopoDS_Edge finalFlap1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapFirstCut, flapTrasl).Shape());
								TopoDS_Edge finalFlap2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapSecondCut, flapTrasl).Shape());
								TopoDS_Edge finalFlapUpperLE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapUpperLE_Edge, flapTrasl).Shape());
								TopoDS_Edge finalFlapLowerLE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapLowerLE_Edge, flapTrasl).Shape());
								TopoDS_Edge finalFlapTE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapTE, flapTrasl).Shape());

								symFlapCutWire_Flap.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalFlap1),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapUpperLE), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapLowerLE),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlap2), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapTE)));

								//								exportSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap1));
								//								exportSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapUpperLE));
								//								exportSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapLowerLE));
								//								exportSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap2));
								//								exportSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapTE));

								symFlapCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge)));
								exportCutSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalAirfoilCut));
								exportCutSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge));
								exportCutSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge));
								exportCutSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge));
								exportCutSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge));

								cutSymFlapBreakPoints.clear();
			}

		}

		// Cut airfoils at non sym flap breakpoints

		List<Amount<Length>> cutNonSymFlapBreakPoints = new ArrayList<>();
		List<OCCShape> exportCutNonSymFlap = new ArrayList<>();
		List<OCCShape> exportNonSymFlap = new ArrayList<>();
		List<CADWire> nonSymFlapCutWire = new ArrayList<>();
		List<CADWire> nonSymFlapCutWire_Flap = new ArrayList<>();

		for( int i = 0; i < 2; i++ ) {
			for( int j = 0; j < numNonSymFlap; j++ ) {

				double etaInnerFlap = nonSymFlapStations.get(j)[0];
				double etaOuterFlap = nonSymFlapStations.get(j)[1];
				double ChordRatio = nonSymFlapChordRatios.get(j)[i]; 		
				double cLeap = 0.176; // c_leap chord ratio
				double k1 = 0.48; // gap factor
				double k2 = 0.1; //0.3; // airfoil trailing edge factor
				double k3 = 0.115; // 2.3; // flap leading edge factor
				double k4 = 0.02; // airfoil 
				double k5 = 0.38;//0.3;
				// Upper gap and delta for point P7
				double cGap = k1 * cLeap; // flap gap on upper side 
				double deltaCGap2 = k4 * cGap; // airfoil TE for point P7
				// Lower gap for point P4
				double deltaCGap3 = k3 * ChordRatio; // k3 * c_gap; // flap lower gap definition, point P4
				// P6-P5 factor
				double deltaCGap1 = k2 * deltaCGap3;//k2 * c_gap; // flap gap on lower side for point P3 and P6
				// P8 factor
				double deltaCGap4 = k5 * deltaCGap3; // flap leading edge point P8

				int innerPanel = 0;
				int outerPanel = 0;
				for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

					if(etaInnerFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && etaInnerFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
						innerPanel = n;
					}

					if(etaOuterFlap > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && etaOuterFlap < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
						outerPanel = n;
					}
				}
				int[] intArray = new int[] {innerPanel, outerPanel};
				cutNonSymFlapBreakPoints.add(Amount.valueOf(nonSymFlapStations.get(j)[0],SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));
				cutNonSymFlapBreakPoints.add( Amount.valueOf(nonSymFlapStations.get(j)[1], SI.METER).times(liftingSurface.getSemiSpan().doubleValue(SI.METER)));

				Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[i]);
				System.out.println("Starting Non-symmetric procedure...");
				List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
						cutNonSymFlapBreakPoints.get(i).doubleValue(SI.METER), 
						airfoilCoords, 
						liftingSurface
						);

				CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
				CADEdge edgeAirfoil = cadCurveAirfoil.edge();
				CADGeomCurve3D chord = null;
				if (i == 0 ) {
					chord = getChordSegmentAtYActual(etaInnerFlap*wingSemiSpan, liftingSurface);
				}
				else {
					chord = getChordSegmentAtYActual(etaOuterFlap*wingSemiSpan, liftingSurface);
				}

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
						liftingSurface.getType(), 
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
						liftingSurface.getType(), 
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
						ChordRatio * chordLength, 
						chord_edge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt A = new gp_Pnt(airfoil_GeomCurve.Value(cFlapPar).X(),
						airfoil_GeomCurve.Value(cFlapPar).Y(),
						airfoil_GeomCurve.Value(cFlapPar).Z());

				double cFlapPar2 = getParamsIntersectionPntsOnAirfoil(
						airfoil_GeomCurve, 
						chord_GeomCurve, 
						chordLength, 
						ChordRatio * chordLength, 
						chord_edge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.LOWER_SIDE
						)[0];

				gp_Pnt B = new gp_Pnt(airfoil_GeomCurve.Value(cFlapPar2).X(),
						airfoil_GeomCurve.Value(cFlapPar2).Y(),
						airfoil_GeomCurve.Value(cFlapPar2).Z());
				gp_Pnt P3 = B;
				System.out.println("Point P3 coordinates : " + P3.X() + " " + P3.Y() + " " + P3.Z());


				// Tangent point P2 and normal point P3
				gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
						new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
						gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
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
										(ChordRatio + deltaCGap1) * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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
										(ChordRatio - deltaCGap3) * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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
										(ChordRatio - deltaCGap3 + deltaCGap4) * chordLength, 
										chord_edge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
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

								TopoDS_Edge flapTE = new TopoDS_Edge();
								gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(flapFirstCut));
								gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(flapSecondCut));
								BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
								flapTE = buildFlapTE.Edge();

								gp_Trsf flapTrasl = new gp_Trsf();
								if ( i == 0 ) {

									flapTrasl.SetTranslation(new gp_Pnt(0, etaInnerFlap*wingSemiSpan, 0), new gp_Pnt(0, etaInnerFlap * wingSemiSpan + lateralGap, 0));

								}
								else {

									flapTrasl.SetTranslation(new gp_Pnt(0, etaOuterFlap*wingSemiSpan, 0), new gp_Pnt(0, etaOuterFlap * wingSemiSpan - lateralGap, 0));

								}

								TopoDS_Edge flapUpperLE_Edge = TopoDS.ToEdge(((OCCShape) upperFlapLE).getShape());
								TopoDS_Edge flapLowerLE_Edge = TopoDS.ToEdge(((OCCShape) lowerFlapLE).getShape());			
								TopoDS_Edge airfoilUpperTE_Edge = TopoDS.ToEdge(((OCCShape) upperAirfoilTE).getShape());
								TopoDS_Edge airfoilLowerTE_Edge = TopoDS.ToEdge(((OCCShape) lowerAirfoilTE).getShape());
								TopoDS_Edge airfoilMiddleTE_Edge = TopoDS.ToEdge(((OCCShape) middleAirfoilTE).getShape());

								TopoDS_Edge finalFlap1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapFirstCut, flapTrasl).Shape());
								TopoDS_Edge finalFlap2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapSecondCut, flapTrasl).Shape());
								TopoDS_Edge finalFlapUpperLE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapUpperLE_Edge, flapTrasl).Shape());
								TopoDS_Edge finalFlapLowerLE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapLowerLE_Edge, flapTrasl).Shape());
								TopoDS_Edge finalFlapTE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapTE, flapTrasl).Shape());

								nonSymFlapCutWire_Flap.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalFlap1),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapUpperLE), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapLowerLE),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlap2), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapTE)));
								//					exportNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap1));
								//					exportNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapUpperLE));
								//					exportNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapLowerLE));
								//					exportNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap2));
								//					exportNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapTE));

								nonSymFlapCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleTE_Edge)));
								exportCutNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalAirfoilCut));
								exportCutNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge));
								exportCutNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge));
								exportCutNonSymFlap.add((OCCShape) OCCUtils.theFactory.newShape(airfoilMiddleTE_Edge));


								cutNonSymFlapBreakPoints.clear();

			}
		}

		int n_section = liftingSurface.getYBreakPoints().size() +  ((numSymFlap +  numNonSymFlap) * 2) ;
		System.out.println("Number of sections : " + n_section);

		int boxIndex = 1;
		if (liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			boxIndex = 2;
		}	

		double[] y_array = new double[airfoils.size()] ;

		for ( int j = 0; j < airfoils.size(); j++ ) {

			double[] box = airfoils.get(j).boundingBox();
			y_array[j] = box[boxIndex];

		}

		Arrays.sort(y_array);
		System.out.println("Sorted y_array : " + Arrays.toString(y_array));

		List<OCCShape> exportShapes = new ArrayList<>();
		List<OCCShape> newExport = new ArrayList<>();

		List<OCCShape> exportShapes2 = new ArrayList<>();
		List<OCCShape> patchWing = new ArrayList<>();
		List<OCCShape> exportClosedShapes = new ArrayList<>();
		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		List<CADEdge> supportAirfoils = new ArrayList<>();
		List<CADWire> supportFlapCutWire = new ArrayList<>();


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
				if (Math.abs(Math.abs(box[boxIndex]) - y_1) < 1.0E-5) {
					inner_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < exportCutSymFlap.size(); n++) {

				CADEdge expEdge = (CADEdge) exportCutSymFlap.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[boxIndex]) - y_1) < 1.0E-5) {
					inner_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < exportCutNonSymFlap.size(); n++) {

				CADEdge expEdge = (CADEdge) exportCutNonSymFlap.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[boxIndex]) - y_1) < 1.0E-5) {
					inner_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < airfoils.size(); n++) {

				CADEdge expEdge = airfoils.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[boxIndex]) - Math.abs(y_2)) < 1.0E-5) {
					outer_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < exportCutSymFlap.size(); n++) {

				CADEdge expEdge = (CADEdge) exportCutSymFlap.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[boxIndex]) - Math.abs(y_2)) < 1.0E-5) {
					outer_airfoils.add(expEdge);
				}	

			}

			for(int n = 0; n < exportCutNonSymFlap.size(); n++) {

				CADEdge expEdge = (CADEdge) exportCutNonSymFlap.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[boxIndex]) - Math.abs(y_2)) < 1.0E-5) {
					outer_airfoils.add(expEdge);
				}	

			}


			if( (inner_airfoils.size() + outer_airfoils.size() != 10) && (inner_airfoils.size() + outer_airfoils.size() != 12)) {

				for(int n = 0; n < airfoils.size(); n++) {

					CADEdge expEdge = airfoils.get(n);
					double[] box = expEdge.boundingBox();
					if (Math.abs(Math.abs(box[boxIndex]) - y_1) < 1.0E-5) {
						Edge_Inner =  expEdge;
					}	

				}

				for(int n = 0; n < airfoils.size(); n++) {

					CADEdge expEdge = airfoils.get(n);
					double[] box = expEdge.boundingBox();
					if (Math.abs(Math.abs(box[boxIndex]) - y_2) < 1.0E-5) {
						Edge_Outer =  expEdge;
					}	

				}

				List<CADGeomCurve3D> selectedCurves = new ArrayList<CADGeomCurve3D>();
				CADWire selectedWiresInner = null;
				CADWire selectedWiresOuter = null;
				CADGeomCurve3D inner_Curve = OCCUtils.theFactory.newCurve3D(Edge_Inner);
				CADGeomCurve3D outer_Curve = OCCUtils.theFactory.newCurve3D(Edge_Outer);
				selectedCurves.add(inner_Curve);
				selectedCurves.add(outer_Curve);

				OCCShape Shape = OCCUtils.makePatchThruCurveSections(selectedCurves);
				exportShapes.add(Shape);
				//					List<OCCShape> patchProva = new ArrayList<>();
				List<OCCShape> patchProvaTE = new ArrayList<>();	



				double[] crv1 = inner_Curve.getRange();
				double[] crv2 = outer_Curve.getRange();
				double[] first_point_inn = inner_Curve.value(crv1[0]);
				double[] last_point_inn = inner_Curve.value(crv1[1]);
				double[] first_point_out = outer_Curve.value(crv2[0]);
				double[] last_point_out = outer_Curve.value(crv2[1]);

				selectedWiresInner = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
						Edge_Inner);

				selectedWiresOuter = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_out, last_point_out).edge(),
						Edge_Outer);
				List<CADWire> listSelectedWire = new ArrayList<>();
				listSelectedWire.add(selectedWiresInner);
				listSelectedWire.add(selectedWiresOuter);
				System.out.println("Dimensioni listSelectedWire :	 " + listSelectedWire.size());
				
//				OCCShape newShape = (OCCShape) OCCUtils.theFactory.newShell(listSelectedWire); da problemi per sezioni troppo vicine!!
				OCCShape newShape = OCCUtils.makePatchThruCurveSections(selectedCurves);
				
				double[] crvR1TE = selectedCurves.get(0).getRange();
				double[] crvR2TE = selectedCurves.get(1).getRange();
				CADFace face1TE = OCCUtils.theFactory.newFacePlanar(
						selectedCurves.get(0).value(crvR1TE[0]),
						selectedCurves.get(0).value(crvR1TE[1]),
						selectedCurves.get(1).value(crvR2TE[0])
						);

				CADFace face2TE = OCCUtils.theFactory.newFacePlanar(
						selectedCurves.get(1).value(crvR2TE[0]),
						selectedCurves.get(1).value(crvR2TE[1]),
						selectedCurves.get(0).value(crvR1TE[1])
						);
				CADShell newShellTE = OCCUtils.theFactory.newShellFromAdjacentFaces(face1TE, face2TE);
				
				BRepBuilderAPI_Sewing sewingSectionCurves = new BRepBuilderAPI_Sewing();
				sewingSectionCurves.Init();
				sewingSectionCurves.Add(newShape.getShape());
				sewingSectionCurves.Add(((OCCShape) newShellTE).getShape());
				sewingSectionCurves.Perform();
				TopoDS_Shape sewedShape = sewingSectionCurves.SewedShape();

				newExport.add((OCCShape) OCCUtils.theFactory.newShape(sewedShape));
//				newExport.add(newShape);
//				newExport.add((OCCShape) newShellTE);
//				newExport.add((OCCShape) listSelectedWire.get(0));
//				newExport.add((OCCShape) listSelectedWire.get(1));

				

				CADFace innerFace = OCCUtils.theFactory.newFacePlanar(selectedWiresInner);

//				newExport.add((OCCShape) innerFace);
				CADFace outerFace = OCCUtils.theFactory.newFacePlanar(selectedWiresOuter);

				exportShapes.add((OCCShape) innerFace);
				exportShapes.add((OCCShape) outerFace);
				// Closing the trailing edge
				//					List<CADShape> cadShapeList = new ArrayList<>();
				//					cadShapeList.add((OCCShape) innerFace);
				//					cadShapeList.add((OCCShape) outerFace);
				//					cadShapeList.add((OCCShape) Shape);

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

					//						cadShapeList.add((OCCShape) shell);					
					//
					//						patchWing.add((OCCShape)shell);
					//						patchProvaTE.add((OCCShape) shell);

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
					if(horn == true && i == n_section - 2) {
						System.out.println("Non devo entrare qui!!");
						//							Flapsolids.add((OCCShape) solidWing);
						hornFlap.add((OCCShape) solidWing);

					}
					else {
						solidsSectionWing.add((OCCShape) solidWing);
					}

				}

				//					CADShell sectionShell = OCCUtils.theFactory.newShellFromAdjacentShapes(cadShapeList);
				//					exportClosedShapes.add((OCCShape) sectionShell);

			}

			else {

				if( inner_airfoils.size() + outer_airfoils.size() == 10) { // qua caso non sym

					for(int n = 0; n < nonSymFlapCutWire.size(); n++) {


						CADWire expWire = nonSymFlapCutWire.get(n);
						double[] box = expWire.boundingBox();
						if (Math.abs(Math.abs(box[1]) - y_1) < 1.0E-5) {
							Wire_Inner =  expWire;
						}	

					}

					for(int n = 0; n < nonSymFlapCutWire.size(); n++) {

						CADWire expWire = nonSymFlapCutWire.get(n);
						double[] box = expWire.boundingBox();
						if (Math.abs(Math.abs(box[1]) - y_2) < 1.0E-5) {
							Wire_Outer =  expWire;
						}	

					}

					// Add sections
					int numInterAirfoil = 1;
					CADWire supportWire = null;

					double[] secVec = new double[numInterAirfoil +2];
					secVec = MyArrayUtils.linspace(
							y_1, 
							y_2, 
							numInterAirfoil + 2);
					int flapIndex = 0;
					for (int i1 = 0; i1 < numNonSymFlap; i1++) {

						double flapInnerSec = nonSymFlapStations.get(i1)[0];
						if( Math.abs( Math.abs(flapInnerSec) - Math.abs(y_1) ) < 1e-5) {
							flapIndex = i1;
						}

					}

					double[] chordVec = new double[numInterAirfoil +2];
					chordVec = MyArrayUtils.linspace(
							nonSymFlapChordRatios.get(flapIndex)[0], 
							nonSymFlapChordRatios.get(flapIndex)[1], 
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

						double cLeap = 0.176; // c_leap chord ratio
						double k1 = 0.48; // gap factor
						double k2 = 0.1; //0.3; // airfoil trailing edge factor
						double k3 = 0.115; // 2.3; // flap leading edge factor
						double k4 = 0.02; // airfoil 
						double k5 = 0.38;//0.3;
						// Upper gap and delta for point P7
						double cGap = k1 * cLeap; // flap gap on upper side 
						double deltaCGap2 = k4 * cGap; // airfoil TE for point P7
						// Lower gap for point P4
						double deltaCGap3 = k3 * chordRatio; // k3 * c_gap; // flap lower gap definition, point P4
						// P6-P5 factor
						double deltaCGap1 = k2 * deltaCGap3;//k2 * c_gap; // flap gap on lower side for point P3 and P6
						// P8 factor
						double deltaCGap4 = k5 * deltaCGap3; // flap leading edge point P8

						// Creation of point P1 and P2
						// P1
						double cLeapPar = getParamsIntersectionPntsOnAirfoil(
								airfoil_GeomCurve, 
								chord_GeomCurve, 
								chordLength, 
								cLeap * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
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
								liftingSurface.getType(), 
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
								chordRatio * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
								SideSelector.UPPER_SIDE
								)[0];

						gp_Pnt A = new gp_Pnt(airfoil_GeomCurve.Value(cFlapPar).X(),
								airfoil_GeomCurve.Value(cFlapPar).Y(),
								airfoil_GeomCurve.Value(cFlapPar).Z());

						double cFlapPar2 = getParamsIntersectionPntsOnAirfoil(
								airfoil_GeomCurve, 
								chord_GeomCurve, 
								chordLength, 
								chordRatio * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
								SideSelector.LOWER_SIDE
								)[0];

						gp_Pnt B = new gp_Pnt(airfoil_GeomCurve.Value(cFlapPar2).X(),
								airfoil_GeomCurve.Value(cFlapPar2).Y(),
								airfoil_GeomCurve.Value(cFlapPar2).Z());
						gp_Pnt P3 = B;
						System.out.println("Point P3 coordinates : " + P3.X() + " " + P3.Y() + " " + P3.Z());


						// Tangent point P2 and normal point P3
						gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
								new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
								gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
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
												(chordRatio + deltaCGap1) * chordLength, 
												chord_edge.vertices()[0].pnt(), 
												liftingSurface.getType(), 
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
												(chordRatio - deltaCGap3) * chordLength, 
												chord_edge.vertices()[0].pnt(), 
												liftingSurface.getType(), 
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
												(chordRatio - deltaCGap3 + deltaCGap4) * chordLength, 
												chord_edge.vertices()[0].pnt(), 
												liftingSurface.getType(), 
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

										TopoDS_Edge flapTE = new TopoDS_Edge();
										gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(flapFirstCut));
										gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(flapSecondCut));
										BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
										flapTE = buildFlapTE.Edge();

										TopoDS_Edge flapUpperLE_Edge = TopoDS.ToEdge(((OCCShape) upperFlapLE).getShape());
										TopoDS_Edge flapLowerLE_Edge = TopoDS.ToEdge(((OCCShape) lowerFlapLE).getShape());			
										TopoDS_Edge airfoilUpperTE_Edge = TopoDS.ToEdge(((OCCShape) upperAirfoilTE).getShape());
										TopoDS_Edge airfoilLowerTE_Edge = TopoDS.ToEdge(((OCCShape) lowerAirfoilTE).getShape());
										TopoDS_Edge airfoilMiddleTE_Edge = TopoDS.ToEdge(((OCCShape) middleAirfoilTE).getShape());

										supportFlapCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
												(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
												(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleTE_Edge),
												(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge)));


										supportWire = (OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
												(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
												(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge),
												(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleTE_Edge)));

					}
					// Fine Prova 

					List<CADWire> selectedWires = new ArrayList<CADWire>();
					CADWire inner_Wire = Wire_Inner;
					CADWire outer_Wire = Wire_Outer;

					selectedWires.add(inner_Wire);
					if(Math.abs(Math.abs(y_2) - Math.abs(y_1)) > 3) {
						selectedWires.add(supportWire);
						System.out.println("Using support airfoils");
					}
					selectedWires.add(outer_Wire);

					OCCShape Shape = OCCUtils.makePatchThruSections(selectedWires);
					exportShapes.add(Shape);

					List<CADWire> listSelectedWire = new ArrayList<>();
					listSelectedWire.add(inner_Wire);
					listSelectedWire.add(outer_Wire);
					OCCShape newShape = (OCCShape) OCCUtils.theFactory.newShell(listSelectedWire);
					newExport.add(newShape);
					
					

					CADFace innerFace = OCCUtils.theFactory.newFacePlanar(inner_Wire);
					CADFace outerFace = OCCUtils.theFactory.newFacePlanar(outer_Wire);
//					newExport.add((OCCShape) outerFace);

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

				else { // qua caso sym

					for(int n = 0; n < symFlapCutWire.size(); n++) {


						CADWire expWire = symFlapCutWire.get(n);
						double[] box = expWire.boundingBox();
						if (Math.abs(Math.abs(box[boxIndex]) - y_1) < 1.0E-5) {
							Wire_Inner =  expWire;
						}	

					}

					for(int n = 0; n < symFlapCutWire.size(); n++) {

						CADWire expWire = symFlapCutWire.get(n);
						double[] box = expWire.boundingBox();
						if (Math.abs(Math.abs(box[boxIndex]) - y_2) < 1.0E-5) {
							Wire_Outer =  expWire;
						}	

					}

					System.out.println("n_section : " + n_section);
					System.out.println(" i " + i);
					if(horn == true && i == 1) {
						System.out.println("Non devo entrare qui!!");
						gp_Trsf hornTrsf = new gp_Trsf();
						if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
							hornTrsf.SetTranslation(new gp_Pnt(0, 0,Wire_Outer.boundingBox()[boxIndex]), new gp_Pnt(0,  0,Wire_Outer.boundingBox()[boxIndex] - lateralGap));

						}
						else {
							hornTrsf.SetTranslation(new gp_Pnt(0, Wire_Outer.boundingBox()[1], 0), new gp_Pnt(0,  Wire_Outer.boundingBox()[1] - lateralGap, 0));
						}
						List<CADEdge> edgesWire_Outer = Wire_Outer.edges();
						System.out.println("dim edgesWire_Outer : " + edgesWire_Outer.size());
						OCCShape provaShape = (OCCShape) Wire_Outer;
						TopoDS_Shape provaTopoDSShape = provaShape.getShape();
						TopoDS_Wire provaTransWire = TopoDS.ToWire(new BRepBuilderAPI_Transform(provaTopoDSShape, hornTrsf).Shape());
						Wire_Outer = (CADWire) ((OCCShape) OCCUtils.theFactory.newShape(provaTransWire));
					}

					// Add sections
					int numInterAirfoil = 1;
					CADWire supportWire = null;

					double[] secVec = new double[numInterAirfoil +2];
					secVec = MyArrayUtils.linspace(
							y_1, 
							y_2, 
							numInterAirfoil + 2);
					int flapIndex = 0;
					for (int i1 = 0; i1 < numSymFlap; i1++) {

						double flapInnerSec = symFlapStations.get(i1)[0];
						if( Math.abs( Math.abs(flapInnerSec) - Math.abs(y_1) ) < 1e-5) {
							flapIndex = i1;
						}

					}

					double[] chordVec = new double[numInterAirfoil +2];
					chordVec = MyArrayUtils.linspace(
							symFlapChordRatios.get(flapIndex)[0], 
							symFlapChordRatios.get(flapIndex)[1], 
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
						double k1 = 0.05; // gap factor
						double k2 = 0.08; // airfoil trailing edge factor
						double k3 = 3.0; // flap leading edge factor
						double cGap = k1 * chordRatio; // flap gap 
						double deltaCGap1 = k2 * cGap; // airfoil TE
						double deltaCGap2 = k3 * cGap; // flap LE

						// Creation of point C
						double cFlapParC = getParamsIntersectionPntsOnAirfoil(
								airfoil_GeomCurve, 
								chord_GeomCurve, 
								chordLength, 
								(chordRatio + cGap - deltaCGap1) * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
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
								(chordRatio + cGap - deltaCGap1) * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
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
								(chordRatio + cGap) * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
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
								(chordRatio + cGap) * chordLength, 
								chord_edge.vertices()[0].pnt(), 
								liftingSurface.getType(), 
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
						gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
								new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
								gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
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
																(chordRatio + cGap ) * chordLength)).Edge(), 
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

										TopoDS_Edge airfoilUpperTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilUpperTE).getShape());
										TopoDS_Edge airfoilLowerTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilLowerTE).getShape());
										TopoDS_Edge airfoilMiddleUpperTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilMiddleUpperTE).getShape());
										TopoDS_Edge airfoilMiddleLowerTE_Edge = TopoDS.ToEdge(((OCCShape) airfoilMiddleLowerTE).getShape());

										supportFlapCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
												(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), (CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
												(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge), 
												(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge)));		
										supportWire = OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
												(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), (CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
												(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge), 
												(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge));

					}
					// Fine Prova 

					List<CADWire> selectedWires = new ArrayList<CADWire>();
					CADWire inner_Wire = Wire_Inner;
					CADWire outer_Wire = Wire_Outer;

					selectedWires.add(inner_Wire);
					if(Math.abs(Math.abs(y_2) - Math.abs(y_1)) > 3) {
						selectedWires.add(supportWire);
						System.out.println("Using support airfoils");
					}
					selectedWires.add(outer_Wire);

					OCCShape Shape = OCCUtils.makePatchThruSections(selectedWires);
					exportShapes.add(Shape);
					
					List<CADWire> listSelectedWire = new ArrayList<>();
					listSelectedWire.add(inner_Wire);
					listSelectedWire.add(outer_Wire);
					OCCShape newShape = (OCCShape) OCCUtils.theFactory.newShell(listSelectedWire);
					newExport.add(newShape);

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
		//				PVector cVector = PVector.sub(bPnt, aPnt);
		PVector cVector = PVector.sub(cPnt, aPnt);
		PVector gVector = PVector.sub(gPnt, fPnt);				
		//				double tanAFac = 4*aVector.mag(); // wing and hTail
		//				double tanCFac = 5*aVector.mag();
		//				double tanGFac = 1*aVector.mag();	
		//				double tanAFac = 7*aVector.mag(); // these parameters work fine for the IRON canard
		//				double tanCFac = 7*aVector.mag();
		//				double tanGFac = 1*aVector.mag();	
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

		CADGeomCurve3D lastAirfoilCurve = cadCurveAirfoilBPList.get(cadCurveAirfoilBPList.size()-1);
		//			cadCurveAirfoilBPList = IntStream.range(0, liftingSurface.getYBreakPoints().size())
		//					.mapToObj(i -> {
		//						Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(i);
		//						List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
		//								liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER), 
		//								airfoilCoords, 
		//								liftingSurface
		//								);
		//						CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
		//						return cadCurveAirfoil;
		//					})
		//					.collect(Collectors.toList());

		//			cadCurveAirfoilBPList.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));

		//			double[] crv1 = lastAirfoilCurve.getRange();
		//			double[] first_point_inn = lastAirfoilCurve.value(crv1[0]);
		//			double[] last_point_inn = lastAirfoilCurve.value(crv1[1]);
		double[] crv1 = airfoilTip.getRange();
		double[] first_point_inn = airfoilTip.value(crv1[0]);
		double[] last_point_inn = airfoilTip.value(crv1[1]);
		CADWire wireTip = null;

		//			airfoilTipCrvs.add((OCCEdge) airfoilUppMod.edge());
		//			airfoilTipCrvs.add((OCCEdge) airfoilLowMod.edge());
		System.out.println("Dimensioni airfoilTipCrvs : " + airfoilTipCrvs.size());

		//			wireTip = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
		//					lastAirfoilCurve.edge());
		wireTip = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
				airfoilTipCrvs.get(0), airfoilTipCrvs.get(1));
		CADFace faceTip = OCCUtils.theFactory.newFacePlanar(wireTip);



		BRepBuilderAPI_Sewing sewMakerTip2 = new BRepBuilderAPI_Sewing();
		sewMakerTip2.Init();	
		sewMakerTip2.SetTolerance(1e-3);
		patchWingTip.forEach(s -> sewMakerTip2.Add(s.getShape()));
		sewMakerTip2.Add( ((OCCShape) faceTip).getShape() );
		sewMakerTip2.Perform();

		TopoDS_Shape tds_shape2 = sewMakerTip2.SewedShape();
		
//		newExport.add((OCCShape) OCCUtils.theFactory.newShape(tds_shape));

		// Prova sewing tip alare alla wing

		//Non sym Flap shell creation
		CADWire Wire_Inner_Flap = null;
		CADWire Wire_Outer_Flap = null;
		List<CADWire> selectedWires_Flap = new ArrayList<CADWire>();		

		for(int i = 0; i < numNonSymFlap; i++) {

			double innerFlapStat = nonSymFlapStations.get(i)[0] * wingSemiSpan + lateralGap + y_array[0];
			double outerFlapStat = nonSymFlapStations.get(i)[1] * wingSemiSpan - lateralGap + y_array[0];

			for(int n = 0; n < nonSymFlapCutWire_Flap.size(); n++) {

				CADWire expWire = nonSymFlapCutWire_Flap.get(n);
				double[] box = expWire.boundingBox();
				if (Math.abs(Math.abs(box[1]) - innerFlapStat) < 1.0E-5) {
					Wire_Inner_Flap =  expWire;
				}	

			}
			for(int n = 0; n < nonSymFlapCutWire_Flap.size(); n++) {

				CADWire expWire = nonSymFlapCutWire_Flap.get(n);
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
			Flapsolids.add((OCCShape) solidFlap);

		}

		BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
		for(int i = 0; i < solidsSectionWing.size(); i++) {
			sewing.Add(solidsSectionWing.get(i).getShape());
		}
		if(horn == false) {
			sewing.Add(tds_shape2);
		}
		sewing.Perform();

		TopoDS_Shape finalWingShape = sewing.SewedShape();

		TopExp_Explorer exp = new TopExp_Explorer(finalWingShape, TopAbs_ShapeEnum.TopAbs_SOLID);
		int counter = 1;
		while(exp.More() > 0) {
			counter = counter + 1;
			exp.Next();
		}
		System.out.println("Number of solids in finalWingShape: " + counter);
		System.out.println("Number of solids Flaps : " + Flapsolids.size());

		//SymFlap shell creation

		CADWire Wire_Inner_SymFlap = null;
		CADWire Wire_Outer_SymFlap = null;
		List<CADWire> selectedWires_FlapSym = new ArrayList<CADWire>();		
		for(int i = 0; i < numSymFlap; i++) {
			double outerFlapStat = 0;
			double innerFlapStat = symFlapStations.get(i)[0] * wingSemiSpan + lateralGap + y_array[0];
			System.out.println("Coord giusta : " + (symFlapStations.get(i)[1] * wingSemiSpan - lateralGap + y_array[0]));

			if (horn == true && i == numSymFlap-1) {
				System.out.println("Non devo entrare qui!!");
				outerFlapStat = symFlapStations.get(i)[1] * wingSemiSpan + y_array[0];
			}
			else {
				System.out.println("Devo entrare qui rigo 3327");
				outerFlapStat = symFlapStations.get(i)[1] * wingSemiSpan - lateralGap + y_array[0];
			}

			System.out.println("Stazione primo flap inner : " + innerFlapStat);
			System.out.println("Stazione primo flap outer : " + outerFlapStat);
			System.out.println("Stazione primo profilo : " + y_array[0]);

			for(int n = 0; n < symFlapCutWire_Flap.size(); n++) {

				CADWire expWire = symFlapCutWire_Flap.get(n);
				double[] box = expWire.boundingBox();
				System.out.println("Stazioni flapCutWire_Flap " + n + " : " + box[boxIndex] );
				if (Math.abs(Math.abs(box[boxIndex]) - innerFlapStat) < 1.0E-5) {
					System.out.println("Ho trovato Wire_Inner");
					Wire_Inner_SymFlap =  expWire;
				}	

			}

			for(int n = 0; n < symFlapCutWire_Flap.size(); n++) {

				CADWire expWire = symFlapCutWire_Flap.get(n);
				double[] box = expWire.boundingBox();
				if (Math.abs(Math.abs(box[boxIndex]) - outerFlapStat) < 1.0E-5) {
					System.out.println("Ho trovato Wire_Outer");
					Wire_Outer_SymFlap =  expWire;
				}
			}

			CADWire inner_Wire = Wire_Inner_SymFlap;
			CADWire outer_Wire = Wire_Outer_SymFlap;
			selectedWires_FlapSym.add(inner_Wire);
			selectedWires_FlapSym.add(outer_Wire);
			System.out.println("Prova senza horn");
			OCCShape Flap = OCCUtils.makePatchThruSections(selectedWires_FlapSym);
			exportShapes2.add(Flap);
			CADFace innerFace = OCCUtils.theFactory.newFacePlanar(inner_Wire);
			CADFace outerFace = OCCUtils.theFactory.newFacePlanar(outer_Wire);
			exportShapes.add((OCCShape) innerFace);
			exportShapes.add((OCCShape) outerFace);
			exportShapes.add((OCCShape) Flap);

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
			if(horn == false || (horn == true && i != numSymFlap-1)) {
				System.out.println("Devo entrare qui!! rigo 3384");
				solidFlap = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
				Flapsolids.add((OCCShape) solidFlap);
			}
			if(horn == true && i == numSymFlap-1) {
				System.out.println("Non devo entrare qui!!");

				hornFlap.add((OCCShape) OCCUtils.theFactory.newShape(solidMaker.Solid()));
				CADSolid solid1 = (CADSolid) hornFlap.get(0);
				CADSolid solid2 = (CADSolid) hornFlap.get(1);
				BRepBuilderAPI_Sewing sewMakerHorn = new BRepBuilderAPI_Sewing();
				sewMakerHorn.Init();						
				sewMakerHorn.Add(((OCCShape)solid1).getShape());
				sewMakerHorn.Add(((OCCShape)solid2).getShape());
				sewMakerHorn.Add(tds_shape2);
				sewMakerHorn.Perform();
				TopoDS_Shape sewedHorn = sewMakerHorn.SewedShape();
				OCCShape provaHorn = (OCCShape) OCCUtils.theFactory.newShape(sewedHorn);
				//					CADShape provaCADShapeHorn = OCCUtils.theFactory.newShape(sewedHorn);
				Flapsolids.add(provaHorn);		
			}


		} 

		OCCShape rotatedSymFlapHorn = symFlapRotation(liftingSurface, -0.261, 
				(OCCShape) OCCUtils.theFactory.newShape(finalWingShape), Flapsolids.get(0));

		BRepBuilderAPI_Sewing sewing2 = new BRepBuilderAPI_Sewing();
		sewing2.Init();	
//		sewing2.Add(newExport.get(0).getShape());
//		sewing2.Add(newExport.get(1).getShape());		
		newExport.forEach(s -> sewing2.Add(s.getShape()));
		
//		sewing2.Add(newExport.get(2).getShape());
//		sewing2.Add(newExport.get(3).getShape());
		sewing2.Perform();
		TopoDS_Shape sewedSection = sewing2.SewedShape();
		
		// Tip sewing
		
		BRepBuilderAPI_Sewing sewingTip = new BRepBuilderAPI_Sewing();
		sewingTip.Init();		
		sewingTip.SetTolerance(1E-3);
		sewingTip.Add(tds_shape);
		sewingTip.Add(sewedSection);
		sewingTip.Perform();
		TopoDS_Shape sewedSectionAndTip = sewingTip.SewedShape();
		

		TopoDS_Shape shape = sewedSectionAndTip;
		ShapeAnalysis_FreeBounds shapeAnalayzer = new ShapeAnalysis_FreeBounds(shape);
		TopExp_Explorer wireExplorer = new TopExp_Explorer(shapeAnalayzer.GetClosedWires(), TopAbs_ShapeEnum.TopAbs_WIRE);
		List<CADWire> wireList = new ArrayList<>();
		while( wireExplorer.More() == 1)	{

			wireList.add((CADWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(wireExplorer.Current())));
			wireExplorer.Next();

		}
//
////		CADFace face = OCCUtils.theFactory.newFacePlanar(wireList.get(0));
////		CADFace face1 = OCCUtils.theFactory.newFacePlanar(wireList.get(1));
////		CADFace face2 = OCCUtils.theFactory.newFacePlanar(wireList.get(2));

		List<CADFace> faces = new ArrayList<>();
		wireList.forEach(w -> faces.add(OCCUtils.theFactory.newFacePlanar(w)));
//
////		newExport.add((OCCShape) face);
//		
//		

		BRepBuilderAPI_Sewing sewing3 = new BRepBuilderAPI_Sewing();
		sewing3.Init();
//		sewing3.SetTolerance(1E-4);
		sewing3.Add(shape);
		faces.forEach(f -> sewing3.Add(((OCCShape) f).getShape()));
////		sewing3.Add(((OCCShape)face).getShape());		
////		sewing3.Add(((OCCShape)face1).getShape());	
////		sewing3.Add(((OCCShape)face2).getShape());		
		sewing3.Perform();
		TopoDS_Shape sewedSectionFinal = sewing3.SewedShape();
		
		BRepBuilderAPI_MakeSolid makeSolid = new BRepBuilderAPI_MakeSolid();
		makeSolid.Add(TopoDS.ToShell(sewedSectionFinal));
		makeSolid.Build();
		TopoDS_Shape solidShape = makeSolid.Shape();

		List<OCCShape> shapeList = new ArrayList<>();
//		shapeList.add((OCCShape) OCCUtils.theFactory.newShape(sewedSectionFinal));
		shapeList.add((OCCShape) OCCUtils.theFactory.newShape(solidShape));


		System.out.println("Dim Flapsolids : " + Flapsolids.size());

		String fileName = "Test19as";
		if(OCCUtils.write(fileName, airfoils))
			System.out.println("========== [main] Output written on file: " + fileName);
		if(!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {  
			//			OCCUtils.write(fileName,(OCCShape) OCCUtils.theFactory.newShape(finalWingShape),rotatedSymFlapHorn);
//			OCCUtils.write(fileName, shapeList);
			OCCUtils.write(fileName, FileExtension.STEP, shapeList);
		}
		else {
			OCCUtils.write(fileName,(OCCShape) OCCUtils.theFactory.newShape(finalWingShape),Flapsolids);
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

		double innerHingePntx = 0.62;
		double innerHingePntz = 0;

		double outerHingePntx = 0.66;
		double outerHingePntz = 0;

		ComponentEnum typeLS = liftingSurface.getType();
		if (!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {

			innerHingePos.SetX((innerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			innerHingePos.SetY(flap.boundingBox()[1]);
			innerHingePos.SetZ((innerChordLength * innerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

			outerHingePos.SetX((outerChordLength * outerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[4]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			outerHingePos.SetY(flap.boundingBox()[4]);
			outerHingePos.SetZ((outerChordLength * outerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

		} else {

			//			innerHingePos.SetX((innerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			innerHingePos.SetX(24.109);
			innerHingePos.SetY(0);
			//			innerHingePos.SetZ((innerChordLength * innerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));
			//			innerHingePos.SetZ(flap.boundingBox()[2]);
			outerHingePos.SetZ(1.541);



			//			outerHingePos.SetX((outerChordLength * outerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[4]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			outerHingePos.SetX(26.504);
			outerHingePos.SetY(0);
			outerHingePos.SetZ(5.686);


		}

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

}

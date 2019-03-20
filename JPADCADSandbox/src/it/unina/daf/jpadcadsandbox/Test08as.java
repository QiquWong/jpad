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
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.Geom_Curve;
import opencascade.Precision;
import opencascade.ShapeAnalysis_Curve;
import opencascade.TopExp;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyMathUtils;

public class Test08as {

	public static void main(String[] args) {

		System.out.println("Starting JPADCADSandbox Test08as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface wing = aircraft.getWing();

		// Wing and flap data
		double flapGap = 0.05;
		double flapLateralGap = 0.025;
		double wingSemiSpan = wing.getSemiSpan().doubleValue(SI.METER);

		List<double[]> symFlapStations = new ArrayList<>();
		List<double[]> symFlapChordRatios = new ArrayList<>();

		int numSymFlap = wing.getSymmetricFlaps().size();
		for(int i = 0; i < numSymFlap; i++) {
			SymmetricFlapCreator flap = wing.getSymmetricFlaps().get(i);
			symFlapStations.add(new double[] {
					flap.getInnerStationSpanwisePosition(),
					flap.getOuterStationSpanwisePosition()
			});
			symFlapChordRatios.add(new double[] {
					flap.getInnerChordRatio(),
					flap.getOuterChordRatio()
			});
		}

		System.out.println("Wing semispan" + wingSemiSpan + " m");
		System.out.println("Flap gap: " + flapGap + "m");
		System.out.println("Flap lateral gap: " + flapLateralGap + " m");
		System.out.println("Number of flaps: " + wing.getSymmetricFlaps().size());
		System.out.println("Symmetric flaps spanwise position:");
		symFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Symmetric flaps chord ratios:");
		symFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));

		double adjustmentPar = 0.01;
		List<Double> etaBreakPnts = wing.getEtaBreakPoints();
		System.out.println("Lifting surface eta breakpoints: " + 
				Arrays.toString(etaBreakPnts.toArray(new Double[etaBreakPnts.size()])));

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

		// Wing wireframe construction

		ComponentEnum typeLS = wing.getType();
		List<OCCShape> extraShapes = new ArrayList<>();

		int nPanels = wing.getPanels().size();
		System.out.println(">>> n. panels: " + nPanels);

		Amount<Length> xApex = wing.getXApexConstructionAxes();
		Amount<Length> zApex = wing.getZApexConstructionAxes();
		Amount<Angle> riggingAngle = wing.getRiggingAngle();

		// Build the leading edge
		List<double[]> ptsLE = new ArrayList<double[]>();

		// calculate FIRST breakpoint coordinates
		ptsLE.add(new double[] {xApex.doubleValue(SI.METER), 0.0, zApex.doubleValue(SI.METER)});

		// calculate breakpoints coordinates
		for (int kBP = 1; kBP < wing.getXLEBreakPoints().size(); kBP++) {
			double xbp = wing.getXLEBreakPoints().get(kBP).plus(xApex).doubleValue(SI.METER);
			double ybp = wing.getYBreakPoints().get(kBP).doubleValue(SI.METER);
			double zbp = zApex.doubleValue(SI.METER);
			double spanPanel = wing.getPanels().get(kBP - 1).getSpan().doubleValue(SI.METER);
			double dihedralPanel = wing.getPanels().get(kBP - 1).getDihedral().doubleValue(SI.RADIAN);
			zbp = zbp + ybp*Math.tan(dihedralPanel);
			if(wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
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
			double ybp = wing.getYBreakPoints().get(kPts).doubleValue(SI.METER); 
			double chord = wing.getChordsBreakPoints().get(kPts).doubleValue(SI.METER);    
			chords.add(chord);
			double twist = wing.getTwistsBreakPoints().get(kPts).doubleValue(SI.RADIAN);		
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
		
//		List<List<CADGeomCurve3D>> cadCurveAirfoilList = new ArrayList<List<CADGeomCurve3D>>();

		// airfoils at breakpoints
		List<CADEdge> airfoils = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList = new ArrayList<CADGeomCurve3D>();
		cadCurveAirfoilBPList = IntStream.range(0, wing.getYBreakPoints().size())
				.mapToObj(i -> {
					Airfoil airfoilCoords = wing.getAirfoilList().get(i);
					List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
							wing.getYBreakPoints().get(i).doubleValue(SI.METER), 
							airfoilCoords, 
							wing
							);
					CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
					return cadCurveAirfoil;
				})
				.collect(Collectors.toList());

		cadCurveAirfoilBPList.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
//		cadCurveAirfoilList.add(cadCurveAirfoilBPList);


		// Airfoils at flap breakpoints

		List<Amount<Length>> flapYBreakPoints = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList_Flap = new ArrayList<CADGeomCurve3D>();

		for(int j = 0; j < numSymFlap; j++) {
			double etaInnerFlap = symFlapStations.get(j)[0];
			double etaOuterFlap = symFlapStations.get(j)[1];
			// for breakpoints
			int innerPanel = 0;
			int outerPanel = 0;
			for(int n = 0; n < wing.getYBreakPoints().size()-1; n++) {

				if(etaInnerFlap > wing.getYBreakPoints().get(n).doubleValue(SI.METER) && 
						etaInnerFlap < wing.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					innerPanel = n;
				}

				if(etaOuterFlap > wing.getYBreakPoints().get(n).doubleValue(SI.METER) && 
						etaOuterFlap < wing.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}
			int[] intArray = new int[] {innerPanel, outerPanel};
			flapYBreakPoints.add(Amount.valueOf(symFlapStations.get(j)[0],SI.METER).times(wing.getSemiSpan().doubleValue(SI.METER)));
			flapYBreakPoints.add(Amount.valueOf(symFlapStations.get(j)[1], SI.METER).times(wing.getSemiSpan().doubleValue(SI.METER)));
			
			for(int k = 0; k < flapYBreakPoints.size(); k++) {

				Airfoil airfoilCoords = wing.getAirfoilList().get(intArray[k]);
				List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
						flapYBreakPoints.get(k).doubleValue(SI.METER), 
						airfoilCoords, 
						wing
						);
				CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
				cadCurveAirfoilBPList_Flap.add(cadCurveAirfoil);
			}

			cadCurveAirfoilBPList_Flap.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
			cadCurveAirfoilBPList_Flap.clear();
//			cadCurveAirfoilList.add(cadCurveAirfoilBPList_Flap);
			flapYBreakPoints.clear();
		}
		
		// Cut airfoils at flap breakpoints
		
		List<Amount<Length>> cutflapYBreakPoints = new ArrayList<>();
		List<OCCShape> exportInnerCutFlap = new ArrayList<>();
		List<OCCShape> exportFlap = new ArrayList<>();
		List<CADWire> flapCutWire = new ArrayList<>();
		List<CADWire> flapCutWire_Flap = new ArrayList<>();
		List<OCCShape> solids = new ArrayList<>();
		
		// Inner sections
	
		for(int j = 0; j < numSymFlap; j++) {
			double etaInnerFlap = symFlapStations.get(j)[0];
			double etaOuterFlap = symFlapStations.get(j)[1];
			double innerChordRatio = symFlapChordRatios.get(j)[0];
			int innerPanel = 0;
			int outerPanel = 0;
			for(int n = 0; n < wing.getYBreakPoints().size()-1; n++) {

				if(etaInnerFlap > wing.getYBreakPoints().get(n).doubleValue(SI.METER) && etaInnerFlap < wing.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					innerPanel = n;
				}

				if(etaOuterFlap > wing.getYBreakPoints().get(n).doubleValue(SI.METER) && etaOuterFlap < wing.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}
			int[] intArray = new int[] {innerPanel, outerPanel};
			cutflapYBreakPoints.add(Amount.valueOf(symFlapStations.get(j)[0],SI.METER).times(wing.getSemiSpan().doubleValue(SI.METER)));
			cutflapYBreakPoints.add( Amount.valueOf(symFlapStations.get(j)[1], SI.METER).times(wing.getSemiSpan().doubleValue(SI.METER)));

			Airfoil airfoilCoords = wing.getAirfoilList().get(intArray[0]);
			List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
					cutflapYBreakPoints.get(0).doubleValue(SI.METER), 
					airfoilCoords, 
					wing
					);

			double min = 100;
			double max = 0;
			int LEindex = 0;
			for(int i = 0; i < ptsAirfoil.size(); i++) {

				double min2 = ptsAirfoil.get(i)[0];
				if(min2 < min) {
					min = min2;
					LEindex = i;
				}

			}
			for(int i = 0; i < ptsAirfoil.size(); i++) {

				double max2 = ptsAirfoil.get(i)[0];
				if(max2 > max) {
					max = max2;
				}

			}

			double chord = max - min;
			double flapXStation = min + chord*(1 - innerChordRatio + 0.05 - flapGap);		
			double xmin1 = 1;
			int indexZ1 = 0;
			double xmin2 = 1;
			int indexZ2 = 0;
			for(int i = 0; i < LEindex+1; i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation);
				if(diff < xmin1) {
					xmin1 = diff;
					indexZ1 = i;
				}

			}
			for(int i = LEindex; i < ptsAirfoil.size(); i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation);
				if(diff < xmin2) {
					xmin2 = diff;
					indexZ2 = i;
				}
			}

			Double[] airfoilCutZCoordsNEW = {ptsAirfoil.get(indexZ1)[2], ptsAirfoil.get(indexZ2)[2]};
			
			// New trailing edge inner airfoil
			List<gp_Pnt> airfoilTEPoints = new ArrayList<>();
			airfoilTEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ1)[0], etaInnerFlap * wingSemiSpan, airfoilCutZCoordsNEW[0]));
			airfoilTEPoints.add(new gp_Pnt(min + chord*(1 - innerChordRatio - flapGap), 
					etaInnerFlap * wingSemiSpan, 
					(Math.abs(airfoilCutZCoordsNEW[1]) + Math.abs(airfoilCutZCoordsNEW[0]))/2));
			airfoilTEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ2)[0], etaInnerFlap * wingSemiSpan, airfoilCutZCoordsNEW[1]));
			
			// New flap leading edge  
			double flapXStation_LEFlap = min + chord*(1 - innerChordRatio + 0.1);
			double xmin1_f = 1;
			int indexZ1_f = 0;
			double xmin2_f = 1;
			int indexZ2_f = 0;
			for(int i = 0; i < LEindex+1; i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation_LEFlap);
				if(diff < xmin1_f) {
					xmin1_f = diff;
					indexZ1_f = i;
				}

			}
			for(int i = LEindex; i < ptsAirfoil.size(); i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation_LEFlap);
				if(diff < xmin2_f) {
					xmin2_f = diff;
					indexZ2_f = i;
				}
			}
			Double[] airfoilCutZCoords_flap  = {ptsAirfoil.get(indexZ1_f)[2], ptsAirfoil.get(indexZ2_f)[2]};
			
			List<gp_Pnt> flapLEPoints = new ArrayList<>();
			flapLEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ1_f)[0], etaInnerFlap * wingSemiSpan, airfoilCutZCoords_flap[0]));
			flapLEPoints.add(new gp_Pnt(min + chord*(1 - innerChordRatio), 
					etaInnerFlap * wingSemiSpan, 
					(Math.abs(airfoilCutZCoords_flap[1]) + Math.abs(airfoilCutZCoords_flap[0]))/2));
			flapLEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ2_f)[0], etaInnerFlap * wingSemiSpan, airfoilCutZCoords_flap[1]));

			Geom_Curve airfoilGeomCrv = BRep_Tool.Curve(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(ptsAirfoil, false).edge()).getShape()), new double[1], new double[1]);
			gp_Vec firstTan = new gp_Vec();
			gp_Pnt firstPoint = new gp_Pnt();
			gp_Pnt proj = new gp_Pnt();
			double[] d = new double[1];
			long l = 0;
			ShapeAnalysis_Curve shapeAn = new ShapeAnalysis_Curve();
			shapeAn.Project(airfoilGeomCrv,new gp_Pnt(ptsAirfoil.get(indexZ1_f)[0], etaInnerFlap * wingSemiSpan, airfoilCutZCoords_flap[0]), Precision.Confusion(), proj, d,l);
			airfoilGeomCrv.D1(d[0],firstPoint, firstTan);
			
			gp_Vec secTan = new gp_Vec();
			gp_Pnt secPoint = new gp_Pnt();
			gp_Pnt proj2 = new gp_Pnt();
			double[] d2 = new double[1];
			ShapeAnalysis_Curve shapeAn2 = new ShapeAnalysis_Curve();
			shapeAn2.Project(airfoilGeomCrv,new gp_Pnt(ptsAirfoil.get(indexZ2_f)[0], etaInnerFlap * wingSemiSpan, airfoilCutZCoords_flap[1]), Precision.Confusion(), proj2, d2,l);
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
			
			double[] firstPointCut_Flap = { flapLEPoints.get(0).X(), flapLEPoints.get(0).Y(), flapLEPoints.get(0).Z() };
			List<OCCEdge> firstCutsAirfoilEdges_Flap = OCCUtils.splitEdge( 
					OCCUtils.theFactory.newCurve3D(ptsAirfoil, false), 
					firstPointCut_Flap
					);
			
			TopoDS_Edge cutFlap1 = getShortestEdge(firstCutsAirfoilEdges_Flap);
			
			double[] secondPointCut_Flap = { flapLEPoints.get(flapLEPoints.size() - 1).X(), 
					flapLEPoints.get(flapLEPoints.size() - 1).Y(), 
					flapLEPoints.get(flapLEPoints.size() - 1).Z() };
			List<OCCEdge> secondCutsAirfoilEdges_Flap = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(ptsAirfoil, false),
					secondPointCut_Flap
					);
			TopoDS_Edge cutFlap2 = getShortestEdge(secondCutsAirfoilEdges_Flap);
			
			TopoDS_Edge flapTE = new TopoDS_Edge();
			gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(cutFlap1));
			gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(cutFlap2));
			BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
			flapTE = buildFlapTE.Edge();
			System.out.println("Coord startPnt1 : " + startPnt1.X() + " " + startPnt1.Y() + " " + startPnt1.Z());
			System.out.println("Coord endPnt1 : " + endPnt1.X() + " " + endPnt1.Y() + " " + endPnt1.Z());

			gp_Trsf flapTrasl = new gp_Trsf();
			flapTrasl.SetTranslation(new gp_Pnt(0, etaInnerFlap*wingSemiSpan, 0), new gp_Pnt(0, etaInnerFlap * wingSemiSpan + flapLateralGap, 0));
			gp_Trsf flapTrasl2 = new gp_Trsf();
			flapTrasl2.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, flapLateralGap, 0));
			TopoDS_Edge finalFlap1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutFlap1, flapTrasl).Shape());
			TopoDS_Edge finalFlap2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutFlap2, flapTrasl).Shape());
			TopoDS_Edge finalFlapLEEdge = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapLEEdgeNew, flapTrasl).Shape());
			TopoDS_Edge finalFlapTE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapTE, flapTrasl).Shape());
			
			flapCutWire_Flap.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalFlap1),(CADEdge) OCCUtils.theFactory.newShape(finalFlapLEEdge),(CADEdge) OCCUtils.theFactory.newShape(finalFlap2), (CADEdge) OCCUtils.theFactory.newShape(finalFlapTE)));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap1));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapLEEdge));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap2));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapTE));
//			OCCUtils.theFactory.newFacePlanar(wire);
//			OCCUtils.theFactory.newShellFromAdjacentFaces(cadFaces)
			

			TopoDS_Edge newAirfoilTE = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(airfoilTEPoints, false).edge()).getShape());
			double[] firstPointCut = { airfoilTEPoints.get(0).X(), airfoilTEPoints.get(0).Y(), airfoilTEPoints.get(0).Z() };
			List<OCCEdge> firstCutsAirfoilEdges = OCCUtils.splitEdge( 
					OCCUtils.theFactory.newCurve3D(ptsAirfoil, false), 
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

			flapCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalCutAirfoil),(CADEdge) OCCUtils.theFactory.newShape(newAirfoilTE)));
			exportInnerCutFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalCutAirfoil));
			exportInnerCutFlap.add((OCCShape) OCCUtils.theFactory.newShape(newAirfoilTE));
			cutflapYBreakPoints.clear();

		}

		// Outer sections
		for(int j = 0; j < numSymFlap; j++) {
			double etaInnerFlap = symFlapStations.get(j)[0];
			double etaOuterFlap = symFlapStations.get(j)[1];
			double outerChordRatio = symFlapChordRatios.get(j)[1];
			int innerPanel = 0;
			int outerPanel = 0;
			for(int n = 0; n < wing.getYBreakPoints().size()-1; n++) {

				if(etaInnerFlap*wingSemiSpan > wing.getYBreakPoints().get(n).doubleValue(SI.METER) && etaInnerFlap*wingSemiSpan < wing.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					innerPanel = n;
				}

				if(etaOuterFlap*wingSemiSpan > wing.getYBreakPoints().get(n).doubleValue(SI.METER) && etaOuterFlap*wingSemiSpan < wing.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}
			int[] intArray = new int[] {innerPanel, outerPanel};
			Amount<Length> mFlap = Amount.valueOf(symFlapStations.get(j)[0],SI.METER).times(wing.getSemiSpan().doubleValue(SI.METER)); 
			Amount<Length> m2Flap = Amount.valueOf(symFlapStations.get(j)[1], SI.METER).times(wing.getSemiSpan().doubleValue(SI.METER));
			cutflapYBreakPoints.add(mFlap);
			cutflapYBreakPoints.add(m2Flap);

			Airfoil airfoilCoords = wing.getAirfoilList().get(intArray[1]);
			List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
					cutflapYBreakPoints.get(1).doubleValue(SI.METER), 
					airfoilCoords, 
					wing
					);

			double min = 100;
			double max = 0;
			int LEindex = 0;
			for(int i = 0; i < ptsAirfoil.size(); i++) {

				double min2 = ptsAirfoil.get(i)[0];
				if(min2 < min) {
					min = min2;
					LEindex = i;
				}

			}
			for(int i = 0; i < ptsAirfoil.size(); i++) {

				double max2 = ptsAirfoil.get(i)[0];
				if(max2 > max) {
					max = max2;
				}

			}

			double chord = max - min;
			double flapXStation = min + chord*(1 - outerChordRatio + 0.05 - flapGap);
			double xmin1 = 1;
			int indexZ1 = 0;
			double xmin2 = 1;
			int indexZ2 = 0;
			for(int i = 0; i < LEindex+1; i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation);
				if(diff < xmin1) {
					xmin1 = diff;
					indexZ1 = i;
				}

			}
			for(int i = LEindex; i < ptsAirfoil.size(); i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation);
				if(diff < xmin2) {
					xmin2 = diff;
					indexZ2 = i;
				}
			}
			Double[] airfoilCutZCoordsNEW = {ptsAirfoil.get(indexZ1)[2], ptsAirfoil.get(indexZ2)[2]};
			// New trailing edge outer airfoil
			List<gp_Pnt> airfoilTEPoints = new ArrayList<>();
			airfoilTEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ1)[0], etaOuterFlap * wingSemiSpan, airfoilCutZCoordsNEW[0]));
			airfoilTEPoints.add(new gp_Pnt(min + chord*(1 - outerChordRatio - flapGap), 
					etaOuterFlap * wingSemiSpan, 
					(Math.abs(airfoilCutZCoordsNEW[1]) + Math.abs(airfoilCutZCoordsNEW[0]))/2));
			airfoilTEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ2)[0], etaOuterFlap * wingSemiSpan, airfoilCutZCoordsNEW[1]));
			
			// New leading edge outer flap
			double flapXStation_LEFlap = min + chord*(1 - outerChordRatio + 0.1);
			double xmin1_f = 1;
			int indexZ1_f = 0;
			double xmin2_f = 1;
			int indexZ2_f = 0;
			for(int i = 0; i < LEindex+1; i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation_LEFlap);
				if(diff < xmin1_f) {
					xmin1_f = diff;
					indexZ1_f = i;
				}

			}
			for(int i = LEindex; i < ptsAirfoil.size(); i++) {
				double xCoord = ptsAirfoil.get(i)[0];
				double diff = Math.abs(xCoord-flapXStation_LEFlap);
				if(diff < xmin2_f) {
					xmin2_f = diff;
					indexZ2_f = i;
				}
			}
			Double[] airfoilCutZCoords_flap  = {ptsAirfoil.get(indexZ1_f)[2], ptsAirfoil.get(indexZ2_f)[2]};
			
			List<gp_Pnt> flapLEPoints = new ArrayList<>();
			flapLEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ1_f)[0], etaOuterFlap * wingSemiSpan, airfoilCutZCoords_flap[0]));
			flapLEPoints.add(new gp_Pnt(min + chord*(1 - outerChordRatio), 
					etaOuterFlap * wingSemiSpan, 
					(Math.abs(airfoilCutZCoords_flap[1]) + Math.abs(airfoilCutZCoords_flap[0]))/2));
			flapLEPoints.add(new gp_Pnt(ptsAirfoil.get(indexZ2_f)[0], etaOuterFlap * wingSemiSpan, airfoilCutZCoords_flap[1]));

			Geom_Curve airfoilGeomCrv = BRep_Tool.Curve(TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3D(ptsAirfoil, false).edge()).getShape()), new double[1], new double[1]);
			gp_Vec firstTan = new gp_Vec();
			gp_Pnt firstPoint = new gp_Pnt();
			gp_Pnt proj = new gp_Pnt();
			double[] d = new double[1];
			long l = 0;
			ShapeAnalysis_Curve shapeAn = new ShapeAnalysis_Curve();
			shapeAn.Project(airfoilGeomCrv,new gp_Pnt(ptsAirfoil.get(indexZ1_f)[0], etaOuterFlap * wingSemiSpan, airfoilCutZCoords_flap[0]), Precision.Confusion(), proj, d,l);
			airfoilGeomCrv.D1(d[0],firstPoint, firstTan);
			
			gp_Vec secTan = new gp_Vec();
			gp_Pnt secPoint = new gp_Pnt();
			gp_Pnt proj2 = new gp_Pnt();
			double[] d2 = new double[1];
			ShapeAnalysis_Curve shapeAn2 = new ShapeAnalysis_Curve();
			shapeAn2.Project(airfoilGeomCrv,new gp_Pnt(ptsAirfoil.get(indexZ1_f)[0], etaOuterFlap * wingSemiSpan, airfoilCutZCoords_flap[1]), Precision.Confusion(), proj2, d2,l);
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
			
			double[] firstPointCut_Flap = { flapLEPoints.get(0).X(), flapLEPoints.get(0).Y(), flapLEPoints.get(0).Z() };
			List<OCCEdge> firstCutsAirfoilEdges_Flap = OCCUtils.splitEdge( 
					OCCUtils.theFactory.newCurve3D(ptsAirfoil, false), 
					firstPointCut_Flap
					);
			
			TopoDS_Edge cutFlap1 = getShortestEdge(firstCutsAirfoilEdges_Flap);
			
			double[] secondPointCut_Flap = { flapLEPoints.get(flapLEPoints.size() - 1).X(), 
					flapLEPoints.get(flapLEPoints.size() - 1).Y(), 
					flapLEPoints.get(flapLEPoints.size() - 1).Z() };
			List<OCCEdge> secondCutsAirfoilEdges_Flap = OCCUtils.splitEdge(
					OCCUtils.theFactory.newCurve3D(ptsAirfoil, false),
					secondPointCut_Flap
					);
			TopoDS_Edge cutFlap2 = getShortestEdge(secondCutsAirfoilEdges_Flap);
			
			TopoDS_Edge flapTE = new TopoDS_Edge();
			gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(cutFlap1));
			gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(cutFlap2));
			BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
			flapTE = buildFlapTE.Edge();
			System.out.println("Coord startPnt1 : " + startPnt1.X() + " " + startPnt1.Y() + " " + startPnt1.Z());
			System.out.println("Coord endPnt1 : " + endPnt1.X() + " " + endPnt1.Y() + " " + endPnt1.Z());

			gp_Trsf flapTrasl = new gp_Trsf();
			flapTrasl.SetTranslation(new gp_Pnt(0, etaOuterFlap*wingSemiSpan, 0), new gp_Pnt(0, etaOuterFlap * wingSemiSpan - flapLateralGap, 0));
			gp_Trsf flapTrasl2 = new gp_Trsf();
			flapTrasl2.SetTranslation(new gp_Pnt(0, 0, 0), new gp_Pnt(0, flapLateralGap, 0));
			TopoDS_Edge finalFlap1 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutFlap1, flapTrasl).Shape());
			TopoDS_Edge finalFlap2 = TopoDS.ToEdge(new BRepBuilderAPI_Transform(cutFlap2, flapTrasl).Shape());
			TopoDS_Edge finalFlapLEEdge = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapLEEdgeNew, flapTrasl).Shape());
			TopoDS_Edge finalFlapTE = TopoDS.ToEdge(new BRepBuilderAPI_Transform(flapTE, flapTrasl).Shape());
			
			flapCutWire_Flap.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalFlap1),(CADEdge) OCCUtils.theFactory.newShape(finalFlapLEEdge),(CADEdge) OCCUtils.theFactory.newShape(finalFlap2), (CADEdge) OCCUtils.theFactory.newShape(finalFlapTE)));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap1));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapLEEdge));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap2));
			exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapTE));
			
			TopoDS_Edge newAirfoilTE = TopoDS.ToEdge(((OCCShape) OCCUtils.theFactory.newCurve3DGP(airfoilTEPoints, false).edge()).getShape());
			double[] firstPointCut = { airfoilTEPoints.get(0).X(), airfoilTEPoints.get(0).Y(), airfoilTEPoints.get(0).Z() };
			List<OCCEdge> firstCutsAirfoilEdges = OCCUtils.splitEdge( 
					OCCUtils.theFactory.newCurve3D(ptsAirfoil, false), 
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

			flapCutWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalCutAirfoil),(CADEdge) OCCUtils.theFactory.newShape(newAirfoilTE)));
			exportInnerCutFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalCutAirfoil));
			exportInnerCutFlap.add((OCCShape) OCCUtils.theFactory.newShape(newAirfoilTE));
			cutflapYBreakPoints.clear();
		}

		// Wing shell creation 
		
		int n_section = wing.getYBreakPoints().size() + 2 * wing.getSymmetricFlaps().size();
		System.out.println("Numero di sezioni : " + n_section);
		System.out.println("Dimensioni airfoils : " + airfoils.size());
		System.out.println("Dimensioni exportInnerCutFlap : " + exportInnerCutFlap.size());
		double[] y_array = new double[airfoils.size()] ;
		double[] flap_array = new double[2*wing.getSymmetricFlaps().size()];
		int k = 0;
		for(int i = 0; i < numSymFlap; i++) {


			for(int j = 0; j < 2; j++) {

				flap_array[k] = symFlapStations.get(i)[j]*wing.getSemiSpan().doubleValue(SI.METER);;
				k++;
			}

		}

		for ( int j = 0; j < airfoils.size(); j++ ) {

			double[] box = airfoils.get(j).boundingBox();
			y_array[j] = box[1];

		}
		
		Arrays.sort(y_array);

		List<OCCShape> exportShapes = new ArrayList<>();
		List<OCCShape> exportShapes2 = new ArrayList<>();
		List<OCCShape> patchWing = new ArrayList<>();
		List<OCCShape> patchFlap = new ArrayList<>();
		List<OCCShape> exportClosedShapes = new ArrayList<>();
		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();


		for( int i = 0; i < n_section-1; i++ ) {

			double y_1 = y_array[i];
			double y_2 = y_array[i+1];
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

			for(int n = 0; n < exportInnerCutFlap.size(); n++) {

				CADEdge expEdge = (CADEdge) exportInnerCutFlap.get(n);
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

			for(int n = 0; n < exportInnerCutFlap.size(); n++) {

				CADEdge expEdge = (CADEdge) exportInnerCutFlap.get(n);
				double[] box = expEdge.boundingBox();
				if (Math.abs(Math.abs(box[1]) - Math.abs(y_2)) < 1.0E-5) {
					outer_airfoils.add(expEdge);
				}	

			}

			if( inner_airfoils.size() + outer_airfoils.size() < 6) {

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

				List<CADGeomCurve3D> selectedCurves = new ArrayList<CADGeomCurve3D>();
				CADWire selectedWiresInner = null;
				CADWire selectedWiresOuter = null;

				CADGeomCurve3D inner_Curve = OCCUtils.theFactory.newCurve3D(Edge_Inner);
				CADGeomCurve3D outer_Curve = OCCUtils.theFactory.newCurve3D(Edge_Outer);
				selectedCurves.add(inner_Curve);
				selectedCurves.add(outer_Curve);
				List<OCCShape> patchWingFirstSection = new ArrayList<>();
				patchWingFirstSection.addAll(selectedCurves.stream()
						.map(OCCUtils::makePatchThruCurveSections)
						.collect(Collectors.toList()));

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
				
				selectedWiresInner = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),Edge_Inner);

				selectedWiresOuter = (OCCUtils.theFactory.newWireFromAdjacentEdges(Edge_Outer,OCCUtils.theFactory.newCurve3D(first_point_out,last_point_out).edge()));
				
				CADFace innerFace = OCCUtils.theFactory.newFacePlanar(selectedWiresInner);
				CADFace outerFace = OCCUtils.theFactory.newFacePlanar(selectedWiresOuter);
				exportShapes.add((OCCShape) innerFace);
				exportShapes.add((OCCShape) outerFace);
//				OCCUtils.theFactory.newShellFromAdjacentShapes(1e-5, cadShapes);
				//exportShapes.add((OCCShape)OCCUtils.theFactory.newShellFromAdjacentFaces(innerFace,outerFace));
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
					// Prova shell chiusa
					
					cadShapeList.add((OCCShape) shell);					
					//
					patchWing.add((OCCShape)shell);
					patchProvaTE.add((OCCShape) shell);

					sewMakerWing.Init();						
					sewMakerWing.Add(((OCCShape) shell).getShape());
					sewMakerWing.Add(Shape.getShape());
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
					solids.add((OCCShape) solidWing);					

				}
				
				CADShell sectionShell = OCCUtils.theFactory.newShellFromAdjacentShapes(cadShapeList);
				exportClosedShapes.add((OCCShape) sectionShell);

			}

			else {

				for(int n = 0; n < flapCutWire.size(); n++) {


					CADWire expWire = flapCutWire.get(n);
					double[] box = expWire.boundingBox();
					if (Math.abs(Math.abs(box[1]) - y_1) < 1.0E-5) {
						Wire_Inner =  expWire;
					}	

				}

				for(int n = 0; n < flapCutWire.size(); n++) {

					CADWire expWire = flapCutWire.get(n);
					double[] box = expWire.boundingBox();
					if (Math.abs(Math.abs(box[1]) - y_2) < 1.0E-5) {
						Wire_Outer =  expWire;
					}	

				}

				List<CADWire> selectedWires = new ArrayList<CADWire>();
				CADWire inner_Wire = Wire_Inner;
				CADWire outer_Wire = Wire_Outer;
				
				selectedWires.add(inner_Wire);
				selectedWires.add(outer_Wire);
				List<OCCShape> patchWingFirstSection = new ArrayList<>();
				patchWingFirstSection.addAll(selectedWires.stream()
						.map(OCCUtils::makePatchThruSections)
						.collect(Collectors.toList()));

				OCCShape Shape = OCCUtils.makePatchThruSections(selectedWires);
				exportShapes.add(Shape);

				CADFace innerFace = OCCUtils.theFactory.newFacePlanar(inner_Wire);
				CADFace outerFace = OCCUtils.theFactory.newFacePlanar(outer_Wire);
				exportShapes.add((OCCShape) innerFace);
				exportShapes.add((OCCShape) outerFace);
				
				List<CADShape> cadShapeList = new ArrayList<>();
				cadShapeList.add((OCCShape) innerFace);
				cadShapeList.add((OCCShape) outerFace);
				cadShapeList.add((OCCShape) Shape);
				
				// Closing the trailing edge
				for(int j1 = 1; j1 < selectedWires.size(); j1++) {

					CADFace face1 = OCCUtils.theFactory.newFacePlanar(selectedWires.get(j1-1));	
					CADFace face2 = OCCUtils.theFactory.newFacePlanar(selectedWires.get(j1));

					CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(face1, face2);
					patchWing.add((OCCShape)shell);
					
					cadShapeList.add((OCCShape) shell);

				}
				
				CADShell sectionShell = OCCUtils.theFactory.newShellFromAdjacentShapes(cadShapeList);
				exportClosedShapes.add((OCCShape) sectionShell);

			}
			
			inner_airfoils.clear();
			outer_airfoils.clear();
					
		}
		
		//Flap shell creation
		CADWire Wire_Inner_Flap = null;
		CADWire Wire_Outer_Flap = null;
		List<CADWire> selectedWires_Flap = new ArrayList<CADWire>();		
		List<OCCShape> provaFlapPatch = new ArrayList<>();
		for(int i = 0; i < numSymFlap; i++) {
			
			double innerFlapStat = symFlapStations.get(i)[0] * wingSemiSpan + flapLateralGap;
			double outerFlapStat = symFlapStations.get(i)[1] * wingSemiSpan - flapLateralGap;
			
			for(int n = 0; n < flapCutWire_Flap.size(); n++) {

				CADWire expWire = flapCutWire_Flap.get(n);
				double[] box = expWire.boundingBox();
				if (Math.abs(Math.abs(box[1]) - innerFlapStat) < 1.0E-5) {
					Wire_Inner_Flap =  expWire;
				}	

			}

			for(int n = 0; n < flapCutWire_Flap.size(); n++) {

				CADWire expWire = flapCutWire_Flap.get(n);
				double[] box = expWire.boundingBox();
				if (Math.abs(Math.abs(box[1]) - outerFlapStat) < 1.0E-5) {
					Wire_Outer_Flap =  expWire;
				}
			}
				
			CADWire inner_Wire = Wire_Inner_Flap;
			CADWire outer_Wire = Wire_Outer_Flap;
			selectedWires_Flap.add(inner_Wire);
			selectedWires_Flap.add(outer_Wire);
	
			List<OCCShape> patchWingFirstSection_Flap = new ArrayList<>();
			patchWingFirstSection_Flap.addAll(selectedWires_Flap.stream()
					.map(OCCUtils::makePatchThruSections)
					.collect(Collectors.toList()));
			
			OCCShape Flap = OCCUtils.makePatchThruSections(selectedWires_Flap);
			exportShapes2.add(Flap);

			CADFace innerFace = OCCUtils.theFactory.newFacePlanar(inner_Wire);
			CADFace outerFace = OCCUtils.theFactory.newFacePlanar(outer_Wire);
			exportShapes.add((OCCShape) innerFace);
			exportShapes.add((OCCShape) outerFace);
			
			List<CADShape> cadShapeList = new ArrayList<>();
			cadShapeList.add((OCCShape) innerFace);
			cadShapeList.add((OCCShape) outerFace);
			cadShapeList.add((OCCShape) Flap);
			
			// Closing the trailing edge
			for(int j1 = 1; j1 < selectedWires_Flap.size(); j1++) {

				CADFace face1 = OCCUtils.theFactory.newFacePlanar(selectedWires_Flap.get(j1-1));	
				CADFace face2 = OCCUtils.theFactory.newFacePlanar(selectedWires_Flap.get(j1));

				CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(face1, face2);
				patchFlap.add((OCCShape)shell);
				
				cadShapeList.add((OCCShape) shell);

			}
			
			CADShell sectionShell = OCCUtils.theFactory.newShellFromAdjacentShapes(cadShapeList);
			exportClosedShapes.add((OCCShape) sectionShell);
			
			selectedWires_Flap.clear();
					
		} 

		//		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		//
		//		sewMakerWing.Init();	
		//		double tipTolerance = 1e-3;
		//		sewMakerWing.SetTolerance(tipTolerance);
		//
		//		patchWingFirstSection.forEach(p -> sewMakerWing.Add(p.getShape()));
		//		patchTEFirstSection.forEach(p -> sewMakerWing.Add(p.getShape()));
		//		sewMakerWing.Perform();
		//		
		//		System.out.println("========== [AircraftUtils::getLiftingSurfaceCAD] Sewing step successful? " + !sewMakerWing.IsNull());	

		//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(finalCutAirfoil));
		//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(newAirfoilTE));

		//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstEdge));
		//		List<OCCShape> exportShapes = new ArrayList<>();
		//exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(firstEdge));
		//exportShapes.add((OCCShape)OCCUtils.theFactory.newShape(cadCurveAirfoilBPListFlapCut));

		String fileName = "Test08as.brep";
		if(OCCUtils.write(fileName, exportInnerCutFlap))
			System.out.println("========== [main] Output written on file: " + fileName);

//		OCCUtils.write(fileName, exportShapes2,exportShapes,patchFlap,patchWing);
		OCCUtils.write(fileName, exportClosedShapes);
		
//		OCCUtils.write(fileName, solids);
//		flapCutWire_Flap
//		OCCUtils.write(fileName, exportFlap,exportShapes2);

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

}
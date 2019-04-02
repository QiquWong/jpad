package it.unina.daf.jpadcadsandbox;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.CADVertex;
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
import opencascade.BRepCheck_Shell;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.GeomAbs_Shape;
import opencascade.Geom_Curve;
import opencascade.Interface_Static;
import opencascade.ShapeFix_Shape;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
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

public class Test18as {

	public static void main(String[] args) {
		
		Instant before = Instant.now();
		
		System.out.println("Starting JPADCADSandbox Test18as");
		System.out.println("Inizializing Factory...");
		OCCUtils.initCADShapeFactory();
		System.out.println("Importing Aircraft...");

		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface liftingSurface = aircraft.getHTail();
		// Wing and movable surfaces data

		double lateralGap = 0.025;
		boolean horn = true;
		double wingSemiSpan = liftingSurface.getSemiSpan().doubleValue(SI.METER);

		List<double[]> nonSymFlapStations = new ArrayList<>();
		List<double[]> nonSymFlapChordRatios = new ArrayList<>();
		int numNonSymFlap = 0;
		nonSymFlapStations.add(new double[] {
				0.15,
				0.31
		});
		nonSymFlapStations.add(new double[] {
				0.33,
				0.72
		});
		nonSymFlapChordRatios.add(new double[] {
				0.30,
				0.30
		});
		nonSymFlapChordRatios.add(new double[] {
				0.30,
				0.30
		});

		List<double[]> symFlapStations = new ArrayList<>();
		List<double[]> symFlapChordRatios = new ArrayList<>();
		int numSymFlap = 1;
		symFlapStations.add(new double[] {
				0.25,
				0.75
		});
		symFlapChordRatios.add(new double[] {
				0.26,
				0.26
		});

		List<double[]> slatStations = new ArrayList<>();
		List<double[]> slatChordRatios = new ArrayList<>();
		int numSlat = 0;
		slatStations.add(new double[] {
				0.15,
				0.31
		});
		slatChordRatios.add(new double[] {
				0.26,
				0.26
		});

		System.out.println("Wing semispan" + wingSemiSpan + " m");
		System.out.println("Flap lateral gap: " + lateralGap + " m");
		System.out.println("Number of flaps: " + liftingSurface.getSymmetricFlaps().size());
		System.out.println("Non-Symmetric flaps spanwise position:");
		nonSymFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Non-Symmetric flaps chord ratios:");
		nonSymFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Non-Symmetric flaps spanwise position:");
		symFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Symmetric flaps chord ratios:");
		symFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Slat spanwise position:");
		slatStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("Slat flaps chord ratios:");
		slatChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));

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

		//		double[] y_array = new double[ liftingSurface.getYBreakPoints().size() + 2 * numSymFlap + 2 * numNonSymFlap] ;
		List<Double> ySections = new ArrayList<>();
		liftingSurface.getYBreakPoints().forEach(a -> ySections.add( (a.doubleValue(SI.METER) / wingSemiSpan)));

		for(int j = 0; j < numSymFlap; j++) {

			ySections.add(symFlapStations.get(j)[0]);
			ySections.add(symFlapStations.get(j)[1]);

		}

		for(int j = 0; j < numNonSymFlap; j++) {

			ySections.add(nonSymFlapStations.get(j)[0]);
			ySections.add(nonSymFlapStations.get(j)[1]);

		}

		System.out.println("Unsorted List : ");
		ySections.forEach(y -> System.out.println((y)));
		Collections.sort(ySections);
		System.out.println("Sorted List : ");
		ySections.forEach(y -> System.out.println((y)));


		List<Double> yDim = new ArrayList<>();
		ySections.forEach(y -> yDim.add(y * liftingSurface.getSemiSpan().doubleValue(SI.METER)));
		List<Double> deltaY = new ArrayList<>();

		for(int i = 1; i < yDim.size(); i++) {
			deltaY.add(yDim.get(i) - yDim.get(i-1));
		}
		System.out.println("Delta Y : ");
		deltaY.forEach(y -> System.out.println(y));

		int numSections = (liftingSurface.getYBreakPoints().size() - 1) +  ((numSymFlap +  numNonSymFlap) * 2) ;
		System.out.println("Number of sections : " + numSections);

		List<CADEdge> allCleanAirfoils = new ArrayList<>();
		List<List<CADWire>> allAirfoilsCutAndFlap = new ArrayList<>();
		//		List<List<OCCShape>> exportAirfoilsCutAndFlap = new ArrayList<>();
		//		List<List<OCCShape>> exportAirfoilsCutAndSlat = new ArrayList<>();

		Map<SolidType, List<OCCShape>> solidsMap = new HashMap<>();
		List<OCCShape> solidCleanSection = new ArrayList<>();
		solidsMap.put(SolidType.WING_CLEAN, new ArrayList<OCCShape>());
		solidsMap.put(SolidType.WING_CUT, new ArrayList<OCCShape>());
		solidsMap.put(SolidType.FLAP, new ArrayList<OCCShape>());
		solidsMap.put(SolidType.SLAT, new ArrayList<OCCShape>());		

		for( int i = 0; i < numSections ; i++ ) {
			boolean doMakeHorn = false;
			FlapType flapType = null;
			boolean isFlapped = false;
			boolean isSlatted = false;
			double yInner = ySections.get(i);
			double yOuter = ySections.get(i + 1);
			double innerChordRatio = 0;
			double outerChordRatio = 0;
			double innerChordRatioSlat = 0;
			double outerChordRatioSlat = 0;
			System.out.println("Inner section : " + yInner);
			System.out.println("Outer section : " + yOuter);

			for ( int j = 0; j < numSymFlap; j++) {

				if( (Math.abs(Math.abs(symFlapStations.get(j)[0] - yInner))  < 1.0E-5)  &&
						(Math.abs(Math.abs(symFlapStations.get(j)[1] - yOuter))  < 1.0E-5)) {
					isFlapped = true;
					flapType = FlapType.SYMMETRIC;
					innerChordRatio = symFlapChordRatios.get(j)[0];
					outerChordRatio = symFlapChordRatios.get(j)[1];
				}
			}

			for ( int j = 0; j < numNonSymFlap; j++) {

				if( (Math.abs(Math.abs(nonSymFlapStations.get(j)[0] - yInner))  < 1.0E-5)  &&
						(Math.abs(Math.abs(nonSymFlapStations.get(j)[1] - yOuter))  < 1.0E-5)) {
					isFlapped = true;
					flapType = FlapType.NON_SYMMETRIC;
					innerChordRatio = nonSymFlapChordRatios.get(j)[0];
					outerChordRatio = nonSymFlapChordRatios.get(j)[1];

				}
			}

			if( isFlapped == false 	) {

				isSlatted = false;

			}

			else {

				for ( int k = 0; k < numSlat; k++) {

					if( (Math.abs(Math.abs(slatStations.get(k)[0] - yInner))  < 1.0E-5)  &&
							(Math.abs(Math.abs(slatStations.get(k)[1] - yOuter))  < 1.0E-5)) {

						isSlatted = true;
						innerChordRatioSlat = slatChordRatios.get(k)[0];
						outerChordRatioSlat = slatChordRatios.get(k)[1];

					}

					else {

						isSlatted = false;

					}
				}
			}

			if (i == numSections - 2 && horn == true) {

				doMakeHorn = true;

			}

			System.out.println("Section " + i + " : " + isFlapped + " " + isSlatted);
			if(isFlapped) {
				System.out.println("Chord ratio inner FLAP: " + innerChordRatio);
				System.out.println("Chord ratio outer FLAP: " + outerChordRatio);
			}
			if(isSlatted) {
				System.out.println("Chord ratio inner SLAT: " + innerChordRatioSlat);
				System.out.println("Chord ratio outer SLAT: " + outerChordRatioSlat);
			}

			getSolids(solidsMap, liftingSurface, yInner, yOuter, isFlapped, flapType, innerChordRatio, outerChordRatio, isSlatted, innerChordRatioSlat, 
					outerChordRatioSlat, lateralGap, doMakeHorn);

			//			List<CADEdge> cleanAirfoilSection = makeAirfoilsClean(liftingSurface, yInner, yOuter);
			//			makeSolidClean(solidsMap, cleanAirfoilSection);


			//			allCleanAirfoils.addAll(cleanAirfoilSection);	
			//			
			//			if(flapType == FlapType.NON_SYMMETRIC || flapType == FlapType.SYMMETRIC) {
			////			List<List<CADWire>> airfoilsCutAndFlap = makeAirfoilsCutAndFlap(liftingSurface, cleanAirfoilSection, yInner, yOuter, innerChordRatio, 
			////					outerChordRatio, flapType, doMakeHorn, lateralGap);
			//			List<List<CADWire>> airfoilsCutAndFlap = makeAirfoilsCutAndFlap(liftingSurface, cleanAirfoilSection, yInner, yOuter, innerChordRatio, 
			//					outerChordRatio, flapType, doMakeHorn, lateralGap);
			//			allAirfoilsCutAndFlap.addAll(airfoilsCutAndFlap);
			//			System.out.println("Dim airfoilsCutAndFlap flapped section : " + airfoilsCutAndFlap.size());
			//			System.out.println("Dim airfoilsCutAndFlap.get(0) flapped section : " + airfoilsCutAndFlap.get(0).size());
			//			System.out.println("Dim airfoilsCutAndFlap.get(1) flapped section : " + airfoilsCutAndFlap.get(1).size());
			////			System.out.println("Dim airfoilsCutAndFlap.get(2) flapped section : " + airfoilsCutAndFlap.get(2).size());
			////			System.out.println("Dim airfoilsCutAndFlap.get(3) flapped section : " + airfoilsCutAndFlap.get(3).size());
			//
			////			exportAirfoilsCutAndFlap.addAll(airfoilsCutAndFlap);
			//			
			//			}
			//						
			//			if( flapType == FlapType.NON_SYMMETRIC && isSlatted) {
			//				
			//				List<List<CADWire>> airfoilsCutAndFlap = makeAirfoilsCutAndFlap(liftingSurface, cleanAirfoilSection, yInner, yOuter, innerChordRatio, 
			//						outerChordRatio, flapType, doMakeHorn, lateralGap);
			////				exportAirfoilsCutAndSlat = makeAirfoilsSlat(liftingSurface, cleanAirfoilSection, airfoilsCutAndFlap, yInner, 
			////						yOuter, innerChordRatioSlat, outerChordRatioSlat, isFlapped, flapType, lateralGap);
			//
			//				
			//			}


			//			Map<SolidType, CADSolid> solidMapSection = getSolids(liftingSurface, yInner, yOuter, isFlapped, flapType, innerChordRatio, outerChordRatio, isSlatted, innerChordRatioSlat, outerChordRatioSlat, lateralGap, doMakeHorn);
			//			solidMap.putAll(getSolids(liftingSurface, yInner, yOuter, isFlapped, flapType, innerChordRatio, outerChordRatio, isSlatted, innerChordRatioSlat, outerChordRatioSlat, lateralGap, doMakeHorn));

			System.out.println("DoMakeHorn : " + doMakeHorn);
		}


		//		System.out.println("Dim allAirfoilsCutAndFlap : " + allAirfoilsCutAndFlap.size());
		//		System.out.println("Dim allCleanAirfoils : " + allCleanAirfoils.size());
		//		System.out.println("Dim exportAirfoilsCutAndFlap : " + exportAirfoilsCutAndFlap.size());
		//		List<OCCShape> export = new ArrayList<>();
		//		exportAirfoilsCutAndSlat.forEach(crv -> export.addAll(crv));

		// Sewing of Wing sections
		List<OCCShape> sectionToSew = new ArrayList<>();
		sectionToSew.addAll(solidsMap.get(SolidType.WING_CLEAN));
		sectionToSew.addAll(solidsMap.get(SolidType.WING_CUT));
		BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
		sewing.SetTolerance(1E-8);
		sectionToSew.forEach(s -> sewing.Add(s.getShape()));
		sewing.Perform();
		TopoDS_Shape sewedWing = sewing.SewedShape();
		System.out.println(	"Is shape closed : " +	sewedWing.Closed());
//		ShapeFix_Shape fix = new ShapeFix_Shape();
//		fix.Init(sewedWing);
////		fix.FixSolidTool();
//		fix.SetMaxTolerance(1E-5); 
//		fix.FixEdgeTool();
//		fix.FixFaceTool();
//		fix.FixWireTool();
//		fix.Perform();
//		TopoDS_Shape fixedWing = fix.Shape();


		// Rotate Slat/Aileron/Flap
//		List<OCCShape> slatsList = solidsMap.get(SolidType.SLAT);
//		OCCShape slat = slatsList.get(0);
//
//		List<OCCShape> flapList	 = solidsMap.get(SolidType.FLAP);
//		OCCShape aileron = flapList.get(flapList.size() - 1);
//		OCCShape innerFlap = flapList.get(0);
//		OCCShape outerFlap = flapList.get(1);
//
//		double aileronDeflection = -0.4;
//		double innerHingePntxAileron = 0.75;
//		double innerHingePntzAileron = -0.015;
//		double outerHingePntxAileron = 0.75;
//		double outerHingePntzAileron = -0.015;
//		
//		OCCShape rotatedAileron = symFlapRotation(liftingSurface, aileronDeflection, aileron, innerHingePntxAileron, innerHingePntzAileron,
//				outerHingePntxAileron, outerHingePntzAileron);
//
//		double slatDeflection = -0.4;
//		double innerHingePntxSlat = 0.25;
//		double innerHingePntzSlat = -0.15;
//		double outerHingePntxSlat = 0.25;
//		double outerHingePntzSlat = -0.15;
//
//		OCCShape rotatedSlat = symFlapRotation(liftingSurface, slatDeflection, slat, innerHingePntxSlat, innerHingePntzSlat,
//				outerHingePntxSlat, outerHingePntzSlat);
//
//		double innerFlapDeflection = 0.575;
//		double innerHingePntxInnerFlap = 0.7115;
//		double innerHingePntzInnerFlap = -0.0761;
//		
//		double outerHingePntxInnerFlap = 0.7115;
//		double outerHingePntzInnerFlap = -0.0761;
//		
//		CADGeomCurve3D innerChord =  getChordSegmentAtYActual(innerFlap.boundingBox()[1], liftingSurface);
//		double innerChordLength = innerChord.length();
//		System.out.println("Lunghezza corda : " + innerChordLength);
//		System.out.println("Xapex : " + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
//		System.out.println("XLEatYActual " + liftingSurface.getXLEAtYActual(innerFlap.boundingBox()[1]).doubleValue(SI.METER));
//		System.out.println("Bounding box flap :" + innerFlap.boundingBox()[0] + " " + innerFlap.boundingBox()[1] + " " + innerFlap.boundingBox()[2] +
//				" " + innerFlap.boundingBox()[3] + " " + innerFlap.boundingBox()[4] + " " + innerFlap.boundingBox()[5]);
//		
//		double[] coordinates3DInner = new double[3];
//		coordinates3DInner[0] = (innerChordLength * innerHingePntxInnerFlap) + liftingSurface.getXLEAtYActual(innerFlap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
//		coordinates3DInner[1] = innerFlap.boundingBox()[1] - 0.01;
//		coordinates3DInner[2] = (innerChordLength * innerHingePntzInnerFlap) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER);
//		CADVertex innerHinge = OCCUtils.theFactory.newVertex(coordinates3DInner);
//		
//		double[] coordinates3DOuter = new double[3];
//		CADGeomCurve3D outerChord =  getChordSegmentAtYActual(innerFlap.boundingBox()[4], liftingSurface);
//		double outerChordLength = outerChord.length();
//		coordinates3DOuter[0] = (outerChordLength * outerHingePntxInnerFlap) + liftingSurface.getXLEAtYActual(innerFlap.boundingBox()[4]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
//		coordinates3DOuter[1] = innerFlap.boundingBox()[4] + 0.1;
//		coordinates3DOuter[2] = (outerChordLength * outerHingePntzInnerFlap) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER);
//		CADVertex outerHinge = OCCUtils.theFactory.newVertex(coordinates3DOuter);
//
//		OCCShape rotatedInnerFlap = nonSymRotation(liftingSurface, innerFlapDeflection, (OCCShape) OCCUtils.theFactory.newShape(sewedWing),
//				innerFlap, nonSymFlapChordRatios.get(0)[0], nonSymFlapChordRatios.get(0)[1], innerHingePntxInnerFlap, innerHingePntzInnerFlap,
//				outerHingePntxInnerFlap, outerHingePntzInnerFlap);
//
//
//		double outerFlapDeflection = 0.575;
//		double innerHingePntxOuterFlap = 0.7115;
//		double innerHingePntzOuterFlap = -0.0761;
//		double outerHingePntxOuterFlap =0.7115;
//		double outerHingePntzOuterFlap = -0.0761;
//
//		OCCShape rotatedOuterFlap = nonSymRotation(liftingSurface, outerFlapDeflection, (OCCShape) OCCUtils.theFactory.newShape(sewedWing),
//				outerFlap, nonSymFlapChordRatios.get(1)[0], nonSymFlapChordRatios.get(1)[1], innerHingePntxOuterFlap, innerHingePntzOuterFlap,
//				outerHingePntxOuterFlap, outerHingePntzOuterFlap);
//
//		//		List<OCCShape> export = new ArrayList<>();
//		//		export.addAll(solidsMap.get(SolidType.WING_CLEAN));
//		//		export.addAll(solidsMap.get(SolidType.WING_CUT));
//		//		export.addAll(solidsMap.get(SolidType.FLAP));
//		//		export.addAll(solidsMap.get(SolidType.SLAT));
//		
//		double[] coordinates3DFinalPosition = new double[3];
//		coordinates3DFinalPosition[0] = 13.0166;
//		coordinates3DFinalPosition[1] = 1.93298 - 0.1;
//		coordinates3DFinalPosition[2] = 1.38372;
//		CADVertex extrPoint = OCCUtils.theFactory.newVertex(coordinates3DFinalPosition);
//

		List<OCCShape> export = new ArrayList<>();
//				export.addAll(solidsMap.get(SolidType.FLAP));
//				export.addAll(solidsMap.get(SolidType.SLAT));
//				export.add((OCCShape) innerHinge);
//				export.add((OCCShape) outerHinge);
//				export.add((OCCShape) extrPoint);


//				export.add(rotatedAileron);
//				export.add(rotatedSlat);
//				export.add(rotatedInnerFlap);
//				export.add(rotatedOuterFlap);
		export.add((OCCShape) OCCUtils.theFactory.newShape(sewedWing));
		
//		export.add((OCCShape) OCCUtils.theFactory.newShape(fixedWing));

//				export.add(solidsMap.get(SolidType.WING_CLEAN).get(0));
//				export.addAll(solidsMap.get(SolidType.WING_CUT));

		solidsMap.get(SolidType.WING_CLEAN).size();
//		solidsMap.get(SolidType.WING_CLEAN).get(solidsMap.get(SolidType.WING_CLEAN).size()-1);
		//		System.out.println("Size export : " + export.size());
		//		System.out.println("Numero di airfoils : " + liftingSurface.getAirfoilList().size());
		//		System.out.println("Numero di breakpoints : " + liftingSurface.getYBreakPoints().size());
		
		String fileName = "Test18as";
		System.out.println("========== [main] Output written on file: " + fileName);
//		OCCUtils.write(fileName, export);
		OCCUtils.write(fileName, FileExtension.STEP, export);
		// do stuff
		Instant after = Instant.now();
		long delta = Duration.between(before, after).toMillis(); // .toWhatsoever()
		System.out.println("Elapsed time : "  + delta );

	}

	public enum FlapType {
		SYMMETRIC,
		NON_SYMMETRIC,
		FOWLER;
	}

	public enum SolidType {
		WING_CLEAN,
		WING_CUT,
		FLAP,
		SLAT;
	}

	public static void getSolids(Map<SolidType, List<OCCShape>> solidsMap, LiftingSurface liftingSurface, double yInnerPct, double yOuterPct, Boolean isFlapped, FlapType flapType, 
			double innerChordRatio, double outerChordRatio, Boolean isSlatted, double innerChordRatioSlat, 
			double outerChordRatioSlat, double lateralGap, Boolean doMakeHorn) {

		//		Map<SolidType, List<CADSolid>> solidsMap = new HashMap<>();

		List<CADEdge> airfoilsClean = makeAirfoilsClean(liftingSurface, yInnerPct, yOuterPct);
		List<List<CADWire>> airfoilsCutAndFlap = null;
		List<List<CADWire>> airfoilsCutAndSlat = null;

		if(!isFlapped && doMakeHorn == false) {

			makeSolidClean(solidsMap, airfoilsClean, liftingSurface, yInnerPct, yOuterPct, doMakeHorn);
			double deltaX = airfoilsClean.get(1).boundingBox()[0] - airfoilsClean.get(0).boundingBox()[0];
			System.out.println("Delta x : " + deltaX);
			//			if (yOuterPct == 1) {
			//				
			//				TopoDS_Shape tip = makeTip(liftingSurface);
			//				OCCShape solidSection = solidsMap.get(SolidType.WING_CLEAN).get(solidsMap.get(SolidType.WING_CLEAN).size()-1);
			//				TopoDS_Shape solidSectionTopoDS = solidSection.getShape();
			//				BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
			//				sewing.Add(solidSectionTopoDS);
			//				sewing.Add(tip);
			//				sewing.Perform();			 
			//		        TopoDS_Shape sewedSection = sewing.SewedShape();
			//		        OCCShape sewedSectionOCCShape = (OCCShape) OCCUtils.theFactory.newShape(sewedSection);
			//				
			//
			//			}

		}

		if(!isFlapped && doMakeHorn == true) {

			doMakeHorn(solidsMap, liftingSurface, airfoilsClean);
			double deltaX = airfoilsClean.get(1).boundingBox()[0] - airfoilsClean.get(0).boundingBox()[0];
			System.out.println("Delta x : " + deltaX);

		}

		if (isFlapped) {

			airfoilsCutAndFlap = makeAirfoilsCutAndFlap(liftingSurface, airfoilsClean, yInnerPct, yOuterPct, innerChordRatio, 
					outerChordRatio, flapType, doMakeHorn, lateralGap);
			System.out.println("Dim airfoilsCutAndFlap prima di slatted : " + airfoilsCutAndFlap.size());
			//			List<List<OCCShape>> airfoilsCutAndFlap = makeAirfoilsCutAndFlap(liftingSurface, airfoilsClean, yInnerPct, yOuterPct, innerChordRatio, 
			//					outerChordRatio, flapType, doMakeHorn, lateralGap);
			makeSolidFlap(solidsMap, airfoilsCutAndFlap);

			if (isSlatted) {
				//				makeAirfoilsSlat();
				//				List<List<CADWire>> airfoilsSlat = makeAirfoilsSlat(liftingSurface, airfoilsClean, airfoilsCutAndFlap, yInnerPct, yOuterPct, innerChordRatioSlat, 
				//						outerChordRatioSlat, isFlapped, lateralGap);
				airfoilsCutAndSlat = makeAirfoilsSlat(liftingSurface, airfoilsClean, airfoilsCutAndFlap, yInnerPct, yOuterPct, innerChordRatioSlat, 
						outerChordRatioSlat, isFlapped, flapType, lateralGap);

				makeSolidSlat(solidsMap, airfoilsCutAndSlat);
				makeSolidCut(solidsMap, airfoilsCutAndSlat, yInnerPct, yOuterPct, liftingSurface);
				double deltaX = airfoilsCutAndSlat.get(0).get(1).boundingBox()[0] - airfoilsCutAndSlat.get(0).get(0).boundingBox()[0];
				System.out.println("Delta x : " + deltaX);

			} 

			if (!isSlatted) {

				makeSolidCut(solidsMap, airfoilsCutAndFlap, yInnerPct, yOuterPct, liftingSurface);
				double deltaX = airfoilsCutAndFlap.get(0).get(1).boundingBox()[0] - airfoilsCutAndFlap.get(0).get(0).boundingBox()[0];
				System.out.println("Delta x : " + deltaX);

			}

		} 

		//		return solidsMap;		
	}

	public static void doMakeHorn(Map<SolidType, List<OCCShape>> solidsMap, LiftingSurface liftingSurface, List<CADEdge> airfoilsClean) {

		CADEdge edgeInner = airfoilsClean.get(0);
		CADEdge edgeOuter = airfoilsClean.get(1);

		List<CADGeomCurve3D> airfoilCurve = new ArrayList<CADGeomCurve3D>();
		airfoilCurve = IntStream.range(0, airfoilsClean.size())
				.mapToObj(i -> {
					CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(airfoilsClean.get(i));
					return cadCurveAirfoil;
				})
				.collect(Collectors.toList());

		OCCShape Shape = OCCUtils.makePatchThruCurveSections(airfoilCurve);

		double[] crv1 = airfoilCurve.get(0).getRange();
		double[] crv2 = airfoilCurve.get(1).getRange();
		double[] first_point_inn = airfoilCurve.get(0).value(crv1[0]);
		double[] last_point_inn = airfoilCurve.get(0).value(crv1[1]);
		double[] first_point_out = airfoilCurve.get(1).value(crv2[0]);
		double[] last_point_out = airfoilCurve.get(1).value(crv2[1]);

		CADWire wireInner = null;
		CADWire wireOuter = null;

		wireInner = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
				edgeInner);

		wireOuter = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_out, last_point_out).edge(),
				edgeOuter);

		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		CADFace outerFace = OCCUtils.theFactory.newFacePlanar(wireOuter);

		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		sewMakerWing.Init();						
		sewMakerWing.Add(Shape.getShape());
		sewMakerWing.Add(((OCCShape)innerFace).getShape());
		sewMakerWing.Add(((OCCShape)outerFace).getShape());
		sewMakerWing.Perform();
		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
		System.out.println("========== Sewing step successful? " + !sewMakerWing.IsNull());	
		System.out.println("========== Building the solid");

		CADSolid solidWing = null;
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		solidMaker.Add(TopoDS.ToShell(sewedSection));
		solidMaker.Build();
		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		//			solidsMap.put(SolidType.FLAP, solidWing);
		OCCShape solidWingShape = (OCCShape) solidWing;
		TopoDS_Shape tip = makeTip(liftingSurface);
		TopoDS_Shape solidSectionTopoDS = solidWingShape.getShape();
		BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
		sewing.Add(solidSectionTopoDS);
		sewing.Add(tip);
		sewing.Perform();			 
		TopoDS_Shape sewedSectionAndTip = sewing.SewedShape();
		OCCShape sewedSectionAndTipOCCShape = (OCCShape) OCCUtils.theFactory.newShape(sewedSectionAndTip);
		solidsMap.get(SolidType.FLAP).add(sewedSectionAndTipOCCShape);
		//			solidsMap.get(SolidType.FLAP).add(solidWing);

	}

	public static TopoDS_Shape makeTip(LiftingSurface liftingSurface) {

		List<OCCShape> patchWingTip = new ArrayList<>();

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
		
//		double[] crv1 = cadCurveAirfoilBPList.get(cadCurveAirfoilBPList.size()-1).getRange();
//		double[] first_point_inn = cadCurveAirfoilBPList.get(cadCurveAirfoilBPList.size()-1).value(crv1[0]);
//		double[] last_point_inn = cadCurveAirfoilBPList.get(cadCurveAirfoilBPList.size()-1).value(crv1[1]);
//		CADWire wireInner = null;
		double[] crv1 = airfoilTip.getRange();
		double[] first_point_inn = airfoilTip.value(crv1[0]);
		double[] last_point_inn = airfoilTip.value(crv1[1]);
		CADWire wireTip = null;

//		wireInner = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
//				cadCurveAirfoilBPList.get(cadCurveAirfoilBPList.size()-1).edge());
//		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		
		wireTip = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
		airfoilTipCrvs.get(0), airfoilTipCrvs.get(1));
		CADFace faceTip = OCCUtils.theFactory.newFacePlanar(wireTip);
//		BRepBuilderAPI_Sewing sewMakerTip2 = new BRepBuilderAPI_Sewing();
//
//		sewMakerTip2.Init();	
//		sewMakerTip2.SetTolerance(1e-3);
//		sewMakerTip2.Add(tds_shape);
//		sewMakerTip2.Add(((OCCShape) innerFace).getShape());
//		sewMakerTip2.Perform();
//		
//		TopoDS_Shape tds_shape2 = sewMakerTip.SewedShape();
		
		BRepBuilderAPI_Sewing sewMakerTip2 = new BRepBuilderAPI_Sewing();
		sewMakerTip2.Init();	
		sewMakerTip2.SetTolerance(1e-3);
		patchWingTip.forEach(s -> sewMakerTip2.Add(s.getShape()));
		sewMakerTip2.Add( ((OCCShape) faceTip).getShape() );
		sewMakerTip2.Perform();
		
		TopoDS_Shape tds_shape2 = sewMakerTip2.SewedShape();

		return tds_shape2;
	}

	public static List<CADEdge> makeAirfoilsClean(LiftingSurface liftingSurface, double yInnerPct, double yOuterPct) {

		List<CADEdge> airfoils = new ArrayList<>();
		List<CADGeomCurve3D> cadCurveAirfoilBPList = new ArrayList<CADGeomCurve3D>();


		List<Double> sectionsPct = new ArrayList<>();
		sectionsPct.add(yInnerPct);
		sectionsPct.add(yOuterPct);
		List<Double> wingBP = new ArrayList<>();
		for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {

			wingBP.add(liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER)/ liftingSurface.getSemiSpan().doubleValue(SI.METER));

		}
		List<CADGeomCurve3D> cadCurveAirfoilBP = new ArrayList<CADGeomCurve3D>();
		cadCurveAirfoilBP = IntStream.range(0, liftingSurface.getYBreakPoints().size())
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

		// Inner Airfoil check
		//		for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {
		//
		//			double diff = Math.abs(yInnerPct - wingBP.get(i));
		//			if(diff < 1e-5) {
		//
		//				airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveAirfoilBP.get(i)).edge());
		//				break;
		//			}
		//
		//		}




		//		List<Double> wingBP = new ArrayList<>();
		//		for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {
		//			
		//			wingBP.add(liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER)/ liftingSurface.getSemiSpan().doubleValue(SI.METER));
		//			
		//		}
		//		
		boolean[] isBP = {false, false};
		for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {

			double diff = Math.abs(yInnerPct - wingBP.get(i));
			if(diff < 1e-5) {
				isBP[0] = true;
				break;
			}

		}

		for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {

			double diff = Math.abs(yOuterPct - wingBP.get(i));
			if(diff < 1e-5) {
				isBP[1] = true;
				break;
			}

		}

		System.out.println("Boolean array : " + isBP[0] + " " + isBP[1]);

		if (isBP[0] && isBP[1]) {

			if( yInnerPct == 0 ) {

				Airfoil innerAirfoilCoords = liftingSurface.getAirfoilList().get(0);
				List<double[]> ptsAirfoilInner = AircraftUtils.populateCoordinateList(
						sectionsPct.get(0) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
						innerAirfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveInnerAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilInner, false);

				Airfoil outerAirfoilCoords = liftingSurface.getAirfoilList().get(1);
				List<double[]> ptsAirfoilOuter = AircraftUtils.populateCoordinateList(
						sectionsPct.get(1) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
						outerAirfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveOuterAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilOuter, false);

				airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveInnerAirfoil).edge());
				airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveOuterAirfoil).edge());

			}

			else {

				Airfoil innerAirfoilCoords = liftingSurface.getAirfoilList().get(1);
				List<double[]> ptsAirfoilInner = AircraftUtils.populateCoordinateList(
						sectionsPct.get(0) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
						innerAirfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveInnerAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilInner, false);

				Airfoil outerAirfoilCoords = liftingSurface.getAirfoilList().get(2);
				List<double[]> ptsAirfoilOuter = AircraftUtils.populateCoordinateList(
						sectionsPct.get(1) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
						outerAirfoilCoords, 
						liftingSurface
						);
				CADGeomCurve3D cadCurveOuterAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoilOuter, false);

				airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveInnerAirfoil).edge());
				airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveOuterAirfoil).edge());

			}
		}

		if (isBP[0] && !isBP[1]) {

			for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {

				double diff = Math.abs(yInnerPct - wingBP.get(i));
				if(diff < 1e-5) {

					airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveAirfoilBP.get(i)).edge());
					break;
				}

			}

			int outerPanel = 0;
			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

				if(yOuterPct >= liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER) && 
						yOuterPct <= liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}

			Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(outerPanel);
			List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
					sectionsPct.get(1) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
					airfoilCoords, 
					liftingSurface
					);
			CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
			airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveAirfoil).edge());

		}

		if (!isBP[0] && isBP[1]) {

			int innerPanel = 0;
			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

				if(yInnerPct >= liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER) && 
						yOuterPct <= liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER)) {
					innerPanel = n;
				}
			}

			Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(innerPanel);
			List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
					sectionsPct.get(0) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
					airfoilCoords, 
					liftingSurface
					);
			CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
			airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveAirfoil).edge());

			for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {

				double diff = Math.abs(yOuterPct - wingBP.get(i));
				if(diff < 1e-5) {

					airfoils.add((OCCEdge)((OCCGeomCurve3D)cadCurveAirfoilBP.get(i)).edge());
					break;
				}

			}

		}

		if(!isBP[0] && !isBP[1]) {
			int innerPanel = 0;
			int outerPanel = 0;
			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

				if(yInnerPct >= liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER) && 
						yOuterPct <= liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER)) {
					innerPanel = n;
				}

				if(yOuterPct >= liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER) && 
						yOuterPct <= liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER)) {
					outerPanel = n;
				}
			}
			int[] intArray = new int[] {innerPanel, outerPanel};		
			cadCurveAirfoilBPList = IntStream.range(0, sectionsPct.size())
					.mapToObj(i -> {
						Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[i]);
						List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
								sectionsPct.get(i) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
								airfoilCoords, 
								liftingSurface
								);
						CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
						return cadCurveAirfoil;
					})
					.collect(Collectors.toList());

			cadCurveAirfoilBPList.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));

		}
		//		
		//		int innerPanel = 0;
		//		int outerPanel = 0;
		//		for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {
		//
		//			if(yInnerPct >= liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER) && 
		//					yOuterPct <= liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER)) {
		//				innerPanel = n;
		//			}
		//
		//			if(yOuterPct >= liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER) && 
		//					yOuterPct <= liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER) / liftingSurface.getSemiSpan().doubleValue(SI.METER)) {
		//				outerPanel = n;
		//			}
		//		}
		//		int[] intArray = new int[] {innerPanel, outerPanel};		
		//		cadCurveAirfoilBPList = IntStream.range(0, sectionsPct.size())
		//				.mapToObj(i -> {
		//					Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[i]);
		//					List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
		//							sectionsPct.get(i) * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
		//							airfoilCoords, 
		//							liftingSurface
		//							);
		//					CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
		//					return cadCurveAirfoil;
		//				})
		//				.collect(Collectors.toList());
		//
		//		cadCurveAirfoilBPList.forEach(crv -> airfoils.add((OCCEdge)((OCCGeomCurve3D)crv).edge()));
		//		
		//		

		// Prova support airfoils
		List<CADEdge> supportAirfoils = new ArrayList<>();
		int numInterAirfoil = 1;
		CADGeomCurve3D supportAirfoil = null;
		double[] secVec = new double[numInterAirfoil +2];
		secVec = MyArrayUtils.linspace(
				yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
				yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
				numInterAirfoil + 2);

		for(int i1 = 0; i1 < numInterAirfoil; i1++) {

			double y_station = secVec[i1 + 1];

			int panel = 0;
			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

				if(y_station > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
						yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) < 
						liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
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
		supportAirfoils.forEach(s -> airfoils.add(s));
		System.out.println("Dimensioni airfoils makeAirfoilsClean : " + airfoils.size());
		return airfoils;	
	}

	public static void makeSolidClean(Map<SolidType, List<OCCShape>> solidsMap, List<CADEdge> airfoilsClean, LiftingSurface liftingSurface, double yInnerPct, double yOuterPct, boolean doMakeHorn) {

		CADEdge edgeInner = airfoilsClean.get(0);
		CADEdge edgeOuter = airfoilsClean.get(1);

		List<CADGeomCurve3D> airfoilCurve = new ArrayList<CADGeomCurve3D>();
		airfoilCurve = IntStream.range(0, airfoilsClean.size())
				.mapToObj(i -> {
					CADGeomCurve3D cadCurveAirfoil = OCCUtils.theFactory.newCurve3D(airfoilsClean.get(i));
					return cadCurveAirfoil;
				})
				.collect(Collectors.toList());

		// Prova con support sections VA BENE!!
		//		List<CADEdge> supportAirfoils = new ArrayList<>();
		//		int numInterAirfoil = 1;
		//		CADGeomCurve3D supportAirfoil = null;
		//		double[] secVec = new double[numInterAirfoil +2];
		//		secVec = MyArrayUtils.linspace(
		//				yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
		//				yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
		//				numInterAirfoil + 2);
		//
		//		for(int i1 = 0; i1 < numInterAirfoil; i1++) {
		//
		//			double y_station = secVec[i1 + 1];
		//
		//			int panel = 0;
		//			for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {
		//
		//				if(y_station > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
		//						yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) < 
		//						liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
		//					panel = n;
		//				}
		//
		//			}
		//			int[] intArray = new int[] {panel};
		//
		//			Airfoil airfoilCoords = liftingSurface.getAirfoilList().get(intArray[0]);
		//			List<double[]> ptsAirfoil = AircraftUtils.populateCoordinateList(
		//					y_station, 
		//					airfoilCoords, 
		//					liftingSurface
		//					);
		//			supportAirfoil = OCCUtils.theFactory.newCurve3D(ptsAirfoil, false);
		//
		//			supportAirfoils.add((OCCEdge)((OCCGeomCurve3D) supportAirfoil).edge());
		//
		//		}
		//		
		//		List<CADGeomCurve3D> selectedCurves = new ArrayList<CADGeomCurve3D>();
		//		selectedCurves.add(airfoilCurve.get(0));
		//		if(Math.abs(Math.abs(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)) - Math.abs(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER))) > 1.0) {
		//			selectedCurves.add(supportAirfoil);
		//			System.out.println("Using support airfoils");
		//		}
		//		selectedCurves.add(airfoilCurve.get(1));


		List<CADGeomCurve3D> selectedCurves = new ArrayList<CADGeomCurve3D>();
		selectedCurves.add(airfoilCurve.get(0));
		if(Math.abs(Math.abs(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)) - Math.abs(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER))) > 1.0) {

			for(int j = 2; j < airfoilCurve.size(); j++) {
				selectedCurves.add(airfoilCurve.get(j));
			}
			System.out.println("Using support airfoils");
		}
		selectedCurves.add(airfoilCurve.get(1));

		//		OCCShape Shape = OCCUtils.makePatchThruCurveSections(airfoilCurve);
		OCCShape Shape = OCCUtils.makePatchThruCurveSections(selectedCurves);


		double[] crv1 = airfoilCurve.get(0).getRange();
		double[] crv2 = airfoilCurve.get(1).getRange();
		double[] first_point_inn = airfoilCurve.get(0).value(crv1[0]);
		double[] last_point_inn = airfoilCurve.get(0).value(crv1[1]);
		double[] first_point_out = airfoilCurve.get(1).value(crv2[0]);
		double[] last_point_out = airfoilCurve.get(1).value(crv2[1]);

		CADWire wireInner = null;
		CADWire wireOuter = null;

		wireInner = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_inn, last_point_inn).edge(),
				edgeInner);

		wireOuter = OCCUtils.theFactory.newWireFromAdjacentEdges(OCCUtils.theFactory.newCurve3D(first_point_out, last_point_out).edge(),
				edgeOuter);

		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		CADFace outerFace = OCCUtils.theFactory.newFacePlanar(wireOuter);

		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		sewMakerWing.Init();
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
			sewMakerWing.Add(((OCCShape) shell).getShape());
		}

		sewMakerWing.Add(((OCCShape)innerFace).getShape());
		sewMakerWing.Add(((OCCShape)outerFace).getShape());
		sewMakerWing.Add(Shape.getShape());
		sewMakerWing.Perform();
		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();

		CADSolid solidWing = null;
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		solidMaker.Add(TopoDS.ToShell(sewedSection));
		solidMaker.Build();
		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		System.out.println("Bounding box : " + solidWing.boundingBox()[0] + " " + solidWing.boundingBox()[1] + " " + solidWing.boundingBox()[2] + " " + 
				solidWing.boundingBox()[3] + " " + solidWing.boundingBox()[4] + " " + solidWing.boundingBox()[5]);
		OCCShape solidWingShape = (OCCShape) solidWing;

		if (yOuterPct == 1 && doMakeHorn == false) {
			TopoDS_Shape tip = makeTip(liftingSurface);
			TopoDS_Shape solidSectionTopoDS = solidWingShape.getShape();
			BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
			sewing.Add(solidSectionTopoDS);
			sewing.Add(tip);
			sewing.Perform();			 
			TopoDS_Shape sewedSectionAndTip = sewing.SewedShape();
			OCCShape sewedSectionAndTipOCCShape = (OCCShape) OCCUtils.theFactory.newShape(sewedSectionAndTip);
			solidsMap.get(SolidType.WING_CLEAN).add(sewedSectionAndTipOCCShape);

		}
		else {

			solidsMap.get(SolidType.WING_CLEAN).add(solidWingShape);

		}

		//		CADShell shell = OCCUtils.theFactory.newShellFromAdjacentFaces(innerFace, outerFace);

		//		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		//		sewMakerWing.Init();		
		//		sewMakerWing.Add(Shape.getShape());
		//		sewMakerWing.Add(((OCCShape)innerFace).getShape());
		//		sewMakerWing.Add(((OCCShape)outerFace).getShape());
		//		sewMakerWing.Perform();
		//		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
		System.out.println("========== Sewing step successful? " + !sewMakerWing.IsNull());	
		System.out.println("========== Building the solid");

		//		CADSolid solidWing = null;
		//		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		//		solidMaker.Add(TopoDS.ToShell(sewedSection));
		//		solidMaker.Build();
		//		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		//		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		//		System.out.println("Bounding box : " + solidWing.boundingBox()[0] + " " + solidWing.boundingBox()[1] + " " + solidWing.boundingBox()[2] + " " + 
		//				solidWing.boundingBox()[3] + " " + solidWing.boundingBox()[4] + " " + solidWing.boundingBox()[5]);
		//		System.out.println("Volume : " + solidWing.getVolume());
		//		OCCShape solidWingShape = (OCCShape) solidWing;


		//		if (yOuterPct == 1 && doMakeHorn == false) {
		//			TopoDS_Shape tip = makeTip(liftingSurface);
		//			TopoDS_Shape solidSectionTopoDS = solidWingShape.getShape();
		//			BRepBuilderAPI_Sewing sewing = new BRepBuilderAPI_Sewing();
		//			sewing.Add(solidSectionTopoDS);
		//			sewing.Add(tip);
		//			sewing.Perform();			 
		//			TopoDS_Shape sewedSectionAndTip = sewing.SewedShape();
		//			OCCShape sewedSectionAndTipOCCShape = (OCCShape) OCCUtils.theFactory.newShape(sewedSectionAndTip);
		//			solidsMap.get(SolidType.WING_CLEAN).add(sewedSectionAndTipOCCShape);
		//
		//		}
		//		else {
		//			
		//			solidsMap.get(SolidType.WING_CLEAN).add(solidWingShape);
		//			
		//		}

	}

	public static List<List<CADWire>> makeAirfoilsCutAndFlap(LiftingSurface liftingSurface, List<CADEdge> airfoilsClean, double yInnerPct, double yOuterPct, 
			double innerChordRatio, double outerChordRatio, FlapType flapType, boolean doMakeHorn, double lateralGap){
		// TODO: exportAirfoilsCutAndFlap, same method but return List<List<OCCShape>>
		//				List<List<OCCShape>> exportAirfoilsCutAndFlap = new ArrayList<>();

		//				List<OCCShape> exportFlap = new ArrayList<>();
		//				exportFlap .add((OCCShape) OCCUtils.theFactory.newShape(finalFlap1));
		//				exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapUpperLE));
		//				exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapLowerLE));
		//				exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlap2));
		//				exportFlap.add((OCCShape) OCCUtils.theFactory.newShape(finalFlapTE));
		//		List<OCCShape> exportAirfoilsCut = new ArrayList<>();
		//		exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(finalAirfoilCut));
		//		exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge));
		//		exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge));
		//		exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge));
		//		exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge));
		//		
		//		exportAirfoilsCutAndFlap.add(exportFlap);
		//		exportAirfoilsCutAndFlap.add(exportAirfoilsCut);

		List<List<CADWire>> airfoilsCutAndFlap = new ArrayList<>();
		List<CADWire> airfoilsCut = new ArrayList<>();
		List<CADWire> flap	 = new ArrayList<>();
		int numInterAirfoil = 1;
		List<CADWire> supportWire = new ArrayList<>();
		double[] secVec = new double[numInterAirfoil +2];
		double[] chordVec = new double[numInterAirfoil +2];

		switch (flapType) {

		case SYMMETRIC:
			for(int i = 0; i < 2; i++) {

				double ChordRatio = 0;
				if( i == 0) {
					ChordRatio = innerChordRatio;		
				}
				else {	
					ChordRatio = outerChordRatio;	
				}

				double k1 = 0.1; // gap factor
				double k2 = 0.08; // airfoil trailing edge factor
				double k3 = 3.0; // flap leading edge factor
				double cGap = k1 * ChordRatio; // flap gap 
				double deltaCGap1 = k2 * cGap; // airfoil TE
				double deltaCGap2 = k3 * cGap; // flap LE

				CADGeomCurve3D chord = null;
				if(i == 0) {
					chord = getChordSegmentAtYActual(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}
				else {
					chord = getChordSegmentAtYActual(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}

				double chordLength = chord.length();
				OCCEdge chordEdge = (OCCEdge) chord.edge();
				OCCEdge airfoilEdge = (OCCEdge) airfoilsClean.get(i);

				Geom_Curve chordGeomCurve = BRep_Tool.Curve(chordEdge.getShape(), new double[1], new double[1]);
				Geom_Curve airfoilGeomCurve = BRep_Tool.Curve(airfoilEdge.getShape(), new double[1], new double[1]);

				// Creation of point C
				double cFlapParC = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(ChordRatio + cGap - deltaCGap1) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt C = new gp_Pnt(airfoilGeomCurve.Value(cFlapParC).X(),
						airfoilGeomCurve.Value(cFlapParC).Y(),
						airfoilGeomCurve.Value(cFlapParC).Z());
				System.out.println("Point C coordinates : " + C.X() + " " + C.Y() + " " + C.Z());
				// Creation of point D
				double cFlapParD = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(ChordRatio + cGap - deltaCGap1) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.LOWER_SIDE
						)[0];

				gp_Pnt D = new gp_Pnt(airfoilGeomCurve.Value(cFlapParD).X(),
						airfoilGeomCurve.Value(cFlapParD).Y(),
						airfoilGeomCurve.Value(cFlapParD).Z());
				System.out.println("Point D coordinates : " + D.X() + " " + D.Y() + " " + D.Z());

				// Creation of point E
				double cFlapParE = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(ChordRatio + cGap) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt E = new gp_Pnt(airfoilGeomCurve.Value(cFlapParE).X(),
						airfoilGeomCurve.Value(cFlapParE).Y(),
						airfoilGeomCurve.Value(cFlapParE).Z());
				System.out.println("Point E coordinates : " + E.X() + " " + E.Y() + " " + E.Z());

				// Creation of point F
				double cFlapParF = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(ChordRatio + cGap) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.LOWER_SIDE
						)[0];

				gp_Pnt F = new gp_Pnt(airfoilGeomCurve.Value(cFlapParF).X(),
						airfoilGeomCurve.Value(cFlapParF).Y(),
						airfoilGeomCurve.Value(cFlapParF).Z());
				System.out.println("Point F coordinates : " + F.X() + " " + F.Y() + " " + F.Z());

				// Splitting airfoil in point E and F
				double[] pntE = new double[] {E.X(), E.Y(), E.Z()};
				double[] pntF = new double[] {F.X(), F.Y(), F.Z()};
				CADGeomCurve3D airfoil1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntE).get(0));
				CADGeomCurve3D airfoil2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntE).get(1));
				CADEdge edgeAirfoil1 = airfoil1.edge();
				CADEdge edgeAirfoil2 = airfoil2.edge();
				List<OCCEdge> airfoilEdges = new ArrayList<>();
				airfoilEdges.add((OCCEdge) edgeAirfoil1);
				airfoilEdges.add((OCCEdge) edgeAirfoil2);

				TopoDS_Edge airfoilFirstCut = getLongestEdge(airfoilEdges);

				List<OCCEdge> splitAirfoil = OCCUtils.splitEdge(
						OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut)),
						pntF
						);
				TopoDS_Edge finalAirfoilCut = getLongestEdge(splitAirfoil);

				// Get normal vectors in point C and D
				gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
						new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
						gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
								new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);

								gp_Vec tangPntC = new gp_Vec();
								gp_Pnt PntC = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParC, PntC, tangPntC);
								tangPntC.Normalize();
								gp_Vec normPntC = tangPntC.Crossed(zyDir).Normalized();

								gp_Vec tangPntD = new gp_Vec();
								gp_Pnt PntD = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParD, PntD, tangPntD);
								tangPntD.Normalize();
								gp_Vec normPntD = tangPntD.Crossed(zyDir).Normalized();

								// Get tangent vector in point E and F

								gp_Vec tangPntE = new gp_Vec();
								gp_Pnt PntE= new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParE, PntE, tangPntE);
								tangPntE.Normalize();
								tangPntE = tangPntE.Reversed();

								gp_Vec tangPntF = new gp_Vec();
								gp_Pnt PntF = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParF, PntF, tangPntF);
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
																		chordEdge.vertices()[0].pnt()[0],
																		chordEdge.vertices()[0].pnt()[1],
																		chordEdge.vertices()[0].pnt()[2]),
																lsAxis),
														(ChordRatio + cGap ) * chordLength)).Edge(), 
										new double[1], 
										new double[1]
										);

								double[] chorPar = getParamIntersectionPnts(chordGeomCurve, circle);
								gp_Vec chorDir = new gp_Vec();
								gp_Pnt I = new gp_Pnt();
								chordGeomCurve.D1(chorPar[0], I, chorDir);
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
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										ChordRatio * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.UPPER_SIDE
										)[0];

								gp_Pnt A = new gp_Pnt(airfoilGeomCurve.Value(cFlapParA).X(),
										airfoilGeomCurve.Value(cFlapParA).Y(),
										airfoilGeomCurve.Value(cFlapParA).Z());
								System.out.println("Point A coordinates : " + A.X() + " " + A.Y() + " " + A.Z());

								double cFlapParB = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										ChordRatio * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt B = new gp_Pnt(airfoilGeomCurve.Value(cFlapParB).X(),
										airfoilGeomCurve.Value(cFlapParB).Y(),
										airfoilGeomCurve.Value(cFlapParB).Z());
								System.out.println("Point B coordinates : " + B.X() + " " + B.Y() + " " + B.Z());

								// Creation of point L and M
								double cFlapParL = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(ChordRatio - deltaCGap2) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.UPPER_SIDE
										)[0];

								gp_Pnt L = new gp_Pnt(airfoilGeomCurve.Value(cFlapParL).X(),
										airfoilGeomCurve.Value(cFlapParL).Y(),
										airfoilGeomCurve.Value(cFlapParL).Z());
								System.out.println("Point L coordinates : " + L.X() + " " + L.Y() + " " + L.Z());

								double cFlapParM = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(ChordRatio - deltaCGap2) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt M = new gp_Pnt(airfoilGeomCurve.Value(cFlapParM).X(),
										airfoilGeomCurve.Value(cFlapParM).Y(),
										airfoilGeomCurve.Value(cFlapParM).Z());
								System.out.println("Point M coordinates : " + M.X() + " " + M.Y() + " " + M.Z());

								// Splitting airfoil in point L and M
								double[] pntL = new double[] {L.X(), L.Y(), L.Z()};
								double[] pntM = new double[] {M.X(), M.Y(), M.Z()};
								CADGeomCurve3D flapUpper_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntL).get(0));
								CADGeomCurve3D flapUpper_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntL).get(1));
								CADEdge edge_flapUpper_1 = flapUpper_1.edge();
								CADEdge edge_flapUpper_2 = flapUpper_2.edge();
								List<OCCEdge> flapUpper_edges = new ArrayList<>();
								flapUpper_edges.add((OCCEdge) edge_flapUpper_1);
								flapUpper_edges.add((OCCEdge) edge_flapUpper_2);

								TopoDS_Edge flapFirstCut = getShortestEdge(flapUpper_edges);

								CADGeomCurve3D flapLower_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntM).get(0));
								CADGeomCurve3D flapLower_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntM).get(1));
								CADEdge edge_flapLower_1 = flapLower_1.edge();
								CADEdge edge_flapLower_2 = flapLower_2.edge();
								List<OCCEdge> flapLower_edges = new ArrayList<>();
								flapLower_edges.add((OCCEdge) edge_flapLower_1);
								flapLower_edges.add((OCCEdge) edge_flapLower_2);

								TopoDS_Edge flapSecondCut = getShortestEdge(flapLower_edges);

								// Get tangent vector in point L and M

								gp_Vec tangPntL = new gp_Vec();
								gp_Pnt PntL = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParL, PntL, tangPntL);
								tangPntL.Normalize();
								tangPntL = tangPntL.Reversed();

								gp_Vec tangPntM = new gp_Vec();
								gp_Pnt PntM = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParM, PntM, tangPntM);
								tangPntM.Normalize();

								// Creation of point P

								Geom_Curve circleFlap = BRep_Tool.Curve(
										new BRepBuilderAPI_MakeEdge(
												new gp_Circ(
														new gp_Ax2(
																new gp_Pnt(
																		chordEdge.vertices()[0].pnt()[0],
																		chordEdge.vertices()[0].pnt()[1],
																		chordEdge.vertices()[0].pnt()[2]),
																lsAxis),
														ChordRatio * chordLength)).Edge(), 
										new double[1], 
										new double[1]
										);

								double[] chorParFlap = getParamIntersectionPnts(chordGeomCurve, circleFlap);
								gp_Vec chorDirFlap = new gp_Vec();
								gp_Pnt P = new gp_Pnt();
								chordGeomCurve.D1(chorParFlap[0], P, chorDirFlap);
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
										flapTrasl.SetTranslation(new gp_Pnt(0, 0, yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)), 
												new gp_Pnt(0, 0, yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) + lateralGap));
									}
									else	{
										if( doMakeHorn == true) {
											flapTrasl.SetTranslation(new gp_Pnt(0, 0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)), 
													new gp_Pnt(0, 0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)));

										}

										else {
											flapTrasl.SetTranslation(new gp_Pnt(0, 0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)), 
													new gp_Pnt(0, 0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap));
										}				
									}

								}	
								else {
									if ( i == 0 ) {

										flapTrasl.SetTranslation(new gp_Pnt(0, yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
												new gp_Pnt(0, yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) + lateralGap, 0));

									}


									else {
										if( doMakeHorn == true) {
											flapTrasl.SetTranslation(new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER),0), 
													new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER),0));

										}

										else {
											flapTrasl.SetTranslation(new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
													new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap,0));
										}			
									}
								}

								gp_Trsf wingTraslHorn = new gp_Trsf();

								if (doMakeHorn) {

									if(liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {

										wingTraslHorn.SetTranslation(new gp_Pnt(0, 0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)), 
												new gp_Pnt(0, 0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap));			

									} else {

										wingTraslHorn.SetTranslation(new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
												new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap, 0));

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

								flap.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalFlap1),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapUpperLE), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapLowerLE),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlap2), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapTE)));

								if (doMakeHorn && i == 1) {

									TopoDS_Edge finalAirfoilCutTrasl = TopoDS.ToEdge(new BRepBuilderAPI_Transform(finalAirfoilCut, wingTraslHorn).Shape());
									TopoDS_Edge airfoilUpperTE_EdgeTrasl = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilUpperTE_Edge, wingTraslHorn).Shape());
									TopoDS_Edge airfoilLowerTE_EdgeTrasl = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilLowerTE_Edge, wingTraslHorn).Shape());
									TopoDS_Edge airfoilMiddleUpperTE_EdgeTrasl = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilMiddleUpperTE_Edge, wingTraslHorn).Shape());
									TopoDS_Edge airfoilMiddleLowerTE_EdgeTrasl = TopoDS.ToEdge(new BRepBuilderAPI_Transform(airfoilMiddleLowerTE_Edge, wingTraslHorn).Shape());								
									airfoilsCut.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCutTrasl),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_EdgeTrasl), 
											(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_EdgeTrasl), 
											(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_EdgeTrasl), 
											(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_EdgeTrasl)));

								}
								else {

									airfoilsCut.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
											(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
											(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge), 
											(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge)));

								}



			}
			airfoilsCutAndFlap.add(airfoilsCut);
			airfoilsCutAndFlap.add(flap);

			// Add sections

			secVec = MyArrayUtils.linspace(
					yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
					yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER),  
					numInterAirfoil + 2);

			chordVec = MyArrayUtils.linspace(
					innerChordRatio, 
					outerChordRatio, 
					numInterAirfoil + 2);

			for(int i1 = 0; i1 < numInterAirfoil; i1++) {

				double y_station = secVec[i1 + 1];
				double chordRatio = chordVec[i1 + 1];

				int panel = 0;
				for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

					if(y_station > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
							yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
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
				double k1 = 0.1; // gap factor
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

								supportWire.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleUpperTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleLowerTE_Edge)));

			}
			// Fine Prova 
			airfoilsCutAndFlap.add(supportWire);

			break;

		case NON_SYMMETRIC:
			for( int i = 0; i < 2; i++ ) {

				double ChordRatio = 0;
				if( i == 0) {
					ChordRatio = innerChordRatio;		
				}
				else {	
					ChordRatio = outerChordRatio;	
				}

//				double cLeap = 0.176; // c_leap chord ratio
//				double k1 = 0.48; // gap factor
//				double k2 = 0.1; //0.3; // airfoil trailing edge factor
//				double k3 = 0.115; // 2.3; // flap leading edge factor
//				double k4 = 0.02; // airfoil 
//				double k5 = 0.38;//0.3;
				double cLeap = 0.12; // c_leap chord ratio
				double k1 = 0.15; // gap factor
				double k2 = 0.4; //0.3; // airfoil trailing edge factor
				double k3 = 0.05; // 2.3; // flap leading edge factor
				double k4 = 0.01; // airfoil 
				double k5 = 0.5;//0.3;
				// Upper gap and delta for point P7
				double cGap = k1 * cLeap; // flap gap on upper side 
				double deltaCGap2 = k4 * cGap; // airfoil TE for point P7
				// Lower gap for point P4
				double deltaCGap3 = k3 * ChordRatio; // k3 * c_gap; // flap lower gap definition, point P4
				// P6-P5 factor
				double deltaCGap1 = k2 * deltaCGap3;//k2 * c_gap; // flap gap on lower side for point P3 and P6
				// P8 factor
				double deltaCGap4 = k5 * deltaCGap3; // flap leading edge point P8

				CADGeomCurve3D chord = null;
				if(i == 0) {
					chord = getChordSegmentAtYActual(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}
				else {
					chord = getChordSegmentAtYActual(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}

				double chordLength = chord.length();
				OCCEdge chordEdge = (OCCEdge) chord.edge();
				OCCEdge airfoilEdge = (OCCEdge) airfoilsClean.get(i);

				Geom_Curve chordGeomCurve = BRep_Tool.Curve(chordEdge.getShape(), new double[1], new double[1]);
				Geom_Curve airfoilGeomCurve = BRep_Tool.Curve(airfoilEdge.getShape(), new double[1], new double[1]);

				// Creation of point P1 and P2
				// P1
				double cLeapPar = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						cLeap * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt P1 = new gp_Pnt(airfoilGeomCurve.Value(cLeapPar).X(),
						airfoilGeomCurve.Value(cLeapPar).Y(),
						airfoilGeomCurve.Value(cLeapPar).Z());
				System.out.println("Point P1 coordinates : " + P1.X() + " " + P1.Y() + " " + P1.Z());

				// P2
				double cFlapParP2 = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(cLeap + cGap) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt P2 = new gp_Pnt(airfoilGeomCurve.Value(cFlapParP2).X(),
						airfoilGeomCurve.Value(cFlapParP2).Y(),
						airfoilGeomCurve.Value(cFlapParP2).Z());
				System.out.println("Point P2 coordinates : " + P2.X() + " " + P2.Y() + " " + P2.Z());

				// Creation of arc of circle of radius cf

				double cFlapPar = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						ChordRatio * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt A = new gp_Pnt(airfoilGeomCurve.Value(cFlapPar).X(),
						airfoilGeomCurve.Value(cFlapPar).Y(),
						airfoilGeomCurve.Value(cFlapPar).Z());

				double cFlapPar2 = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						ChordRatio * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.LOWER_SIDE
						)[0];

				gp_Pnt B = new gp_Pnt(airfoilGeomCurve.Value(cFlapPar2).X(),
						airfoilGeomCurve.Value(cFlapPar2).Y(),
						airfoilGeomCurve.Value(cFlapPar2).Z());
				gp_Pnt P3 = B;
				System.out.println("Point P3 coordinates : " + P3.X() + " " + P3.Y() + " " + P3.Z());


				// Tangent point P2 and normal point P3
				gp_Vec zyDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
						new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
						gp_Vec yzDir = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
								new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);

								gp_Vec tangPntP2 = new gp_Vec();
								gp_Pnt PntP2= new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParP2, PntP2, tangPntP2);
								tangPntP2.Normalize();
								tangPntP2 = tangPntP2.Reversed();
								gp_Vec normPntP2 = tangPntP2.Crossed(zyDir).Normalized();

								gp_Vec tangPntP3 = new gp_Vec();
								gp_Pnt pntP3 = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapPar2, pntP3, tangPntP3);
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
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(ChordRatio + deltaCGap1) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt P5 = new gp_Pnt(airfoilGeomCurve.Value(cFlapParP5).X(),
										airfoilGeomCurve.Value(cFlapParP5).Y(),
										airfoilGeomCurve.Value(cFlapParP5).Z());
								System.out.println("Point P5 coordinates : " + P5.X() + " " + P5.Y() + " " + P5.Z());


								gp_Vec tangPntP5 = new gp_Vec();
								gp_Pnt PntP5 = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParP5, PntP5, tangPntP5);
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

								gp_Pnt P7 = new gp_Pnt(normPntP2.Scaled(- deltaCGap2 * chordLength).Added(new gp_Vec(P2.Coord())).XYZ());
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
								CADGeomCurve3D airfoil1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntP2).get(0));
								CADGeomCurve3D airfoil2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntP2).get(1));
								CADEdge edgeAirfoil1 = airfoil1.edge();
								CADEdge edgeAirfoil2 = airfoil2.edge();
								List<OCCEdge> airfoil_edges = new ArrayList<>();
								airfoil_edges.add((OCCEdge) edgeAirfoil1);
								airfoil_edges.add((OCCEdge) edgeAirfoil2);

								TopoDS_Edge airfoilFirstCut = getLongestEdge(airfoil_edges);

								List<OCCEdge> splitAirfoil = OCCUtils.splitEdge(
										OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut)),
										pntP5
										);
								TopoDS_Edge finalAirfoilCut = getLongestEdge(splitAirfoil);

								// Creation of Flap leading edge
								// Creation of point P4 and tangent vector

								double cFlapParP4 = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(ChordRatio - deltaCGap3) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt P4 = new gp_Pnt(airfoilGeomCurve.Value(cFlapParP4).X(),
										airfoilGeomCurve.Value(cFlapParP4).Y(),
										airfoilGeomCurve.Value(cFlapParP4).Z());
								System.out.println("Point P4 coordinates : " + P4.X() + " " + P4.Y() + " " + P4.Z());

								gp_Vec tangPntP4 = new gp_Vec();
								gp_Pnt PntP4 = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParP4, PntP4, tangPntP4);
								tangPntP4.Normalize();

								// Creation of point P8 and normal vector

								double cFlapParP8 = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(ChordRatio - deltaCGap3 + deltaCGap4) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt P8 = new gp_Pnt(airfoilGeomCurve.Value(cFlapParP8).X(), 
										airfoilGeomCurve.Value(cFlapParP8).Y(),
										airfoilGeomCurve.Value(cFlapParP8).Z());
								System.out.println("Point P8 coordinates : " + P8.X() + " " + P8.Y() + " " + P8.Z());


								gp_Vec tangPntP8 = new gp_Vec();
								gp_Pnt PntP8 = new gp_Pnt();
								airfoilGeomCurve.D1(cFlapParP8, PntP8, tangPntP8);
								tangPntP8.Normalize();
								gp_Vec normPntP8 = tangPntP8.Crossed(zyDir).Normalized();

								// Creation of point P9

								gp_Pnt P9 = new gp_Pnt(normPntP8.Scaled(deltaCGap4 * chordLength).Added(new gp_Vec(P8.Coord())).XYZ());
								System.out.println("Point P9 coordinates : " + P9.X() + " " + P9.Y() + " " + P9.Z());

								// Creation of P1 tangent vector

								gp_Vec tangPntP1 = new gp_Vec();
								gp_Pnt PntP1= new gp_Pnt();
								airfoilGeomCurve.D1(cLeapPar, PntP1, tangPntP1);
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
								CADGeomCurve3D flapUpper_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntP1).get(0));
								CADGeomCurve3D flapUpper_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntP1).get(1));
								CADEdge edge_flapUpper_1 = flapUpper_1.edge();
								CADEdge edge_flapUpper_2 = flapUpper_2.edge();
								List<OCCEdge> flapUpper_edges = new ArrayList<>();
								flapUpper_edges.add((OCCEdge) edge_flapUpper_1);
								flapUpper_edges.add((OCCEdge) edge_flapUpper_2);

								TopoDS_Edge flapFirstCut = getShortestEdge(flapUpper_edges);

								CADGeomCurve3D flapLower1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntP4).get(0));
								CADGeomCurve3D flapLower2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntP4).get(1));
								CADEdge edgeFlapLower1 = flapLower1.edge();
								CADEdge edgeFlapLower2 = flapLower2.edge();
								List<OCCEdge> flapLowerEdges = new ArrayList<>();
								flapLowerEdges.add((OCCEdge) edgeFlapLower1);
								flapLowerEdges.add((OCCEdge) edgeFlapLower2);

								TopoDS_Edge flapSecondCut = getShortestEdge(flapLowerEdges);

								TopoDS_Edge flapTE = new TopoDS_Edge();
								gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(flapFirstCut));
								gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(flapSecondCut));
								BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
								flapTE = buildFlapTE.Edge();

								gp_Trsf flapTrasl = new gp_Trsf();
								if ( i == 0 ) {

									flapTrasl.SetTranslation(new gp_Pnt(0, yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
											new gp_Pnt(0, yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) + lateralGap, 0));

								}
								else {

									flapTrasl.SetTranslation(new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
											new gp_Pnt(0, yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap, 0));

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

								flap.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalFlap1),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapUpperLE), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapLowerLE),
										(CADEdge) OCCUtils.theFactory.newShape(finalFlap2), 
										(CADEdge) OCCUtils.theFactory.newShape(finalFlapTE)));

								airfoilsCut.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleTE_Edge)));

			}				
			airfoilsCutAndFlap.add(airfoilsCut);
			airfoilsCutAndFlap.add(flap);

			// Prova per aggiungere support airfoils

			secVec = MyArrayUtils.linspace(
					yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
					yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER),  
					numInterAirfoil + 2);

			chordVec = MyArrayUtils.linspace(
					innerChordRatio, 
					outerChordRatio, 
					numInterAirfoil + 2);

			for(int i1 = 0; i1 < numInterAirfoil; i1++) {

				double y_station = secVec[i1 + 1];
				double chordRatio = chordVec[i1 + 1];

				int panel = 0;
				for(int n = 0; n < liftingSurface.getYBreakPoints().size()-1; n++) {

					if(y_station > liftingSurface.getYBreakPoints().get(n).doubleValue(SI.METER) && 
							yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER) < liftingSurface.getYBreakPoints().get(n+1).doubleValue(SI.METER)) {
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

//				double cLeap = 0.176; // c_leap chord ratio
//				double k1 = 0.48; // gap factor
//				double k2 = 0.1; //0.3; // airfoil trailing edge factor
//				double k3 = 0.115; // 2.3; // flap leading edge factor
//				double k4 = 0.02; // airfoil 
//				double k5 = 0.38;//0.3;
				double cLeap = 0.12; // c_leap chord ratio
				double k1 = 0.15; // gap factor
				double k2 = 0.4; //0.3; // airfoil trailing edge factor
				double k3 = 0.05; // 2.3; // flap leading edge factor
				double k4 = 0.01; // airfoil 
				double k5 = 0.5;//0.3;
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

								supportWire.add((OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalAirfoilCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperTE_Edge), 
										(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerTE_Edge),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilMiddleTE_Edge))));

			}
			airfoilsCutAndFlap.add(supportWire);
			// Fine Prova

			break;
		case FOWLER:
			// TODO
			break;
		default:
			break;
		}

		return airfoilsCutAndFlap;	

	}

	public static void makeSolidCut(Map<SolidType, List<OCCShape>> solidsMap, List<List<CADWire>> airfoilsCutAndFlap, double yInnerPct, double yOuterPct, LiftingSurface liftingSurface) {
		// put in solidsMap the solid of a clean wing segment --> WING_CUT
		// solidsMap.put(SolidType.WING_CUT, value)
		CADWire wireInner = airfoilsCutAndFlap.get(0).get(0);
		CADWire wireOuter = airfoilsCutAndFlap.get(0).get(1);

		List<CADWire> selectedWires = new ArrayList<>();

		selectedWires.add(wireInner);
		if(airfoilsCutAndFlap.size() == 3) {
			if (Math.abs(Math.abs(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER)) - Math.abs(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER))) > 5.0) {
				for(int j = 0; j < 1; j++) {
					selectedWires.add(airfoilsCutAndFlap.get(2).get(j));
				}
				System.out.println("Using support airfoils");
			}
		}

		selectedWires.add(wireOuter);

		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		CADFace outerFace = OCCUtils.theFactory.newFacePlanar(wireOuter);

		OCCShape Shape = OCCUtils.makePatchThruSections(selectedWires);
		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		sewMakerWing.Init();                        
		sewMakerWing.Add(Shape.getShape());
		sewMakerWing.Add(((OCCShape)innerFace).getShape());
		sewMakerWing.Add(((OCCShape)outerFace).getShape());
		sewMakerWing.Perform();
		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();

		CADSolid solidWing = null;
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		solidMaker.Add(TopoDS.ToShell(sewedSection));
		solidMaker.Build();
		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		OCCShape solidWingShape = (OCCShape) solidWing;
		solidsMap.get(SolidType.WING_CUT).add(solidWingShape);

		//		OCCShape Shape = OCCUtils.makePatchThruSections(airfoilsCutAndFlap.get(0));
		//		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		//		CADFace outerFace = OCCUtils.theFactory.newFacePlanar(wireOuter);
		//		
		//		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		//		sewMakerWing.Init();						
		//		sewMakerWing.Add(Shape.getShape());
		//		sewMakerWing.Add(( (OCCShape) innerFace).getShape());
		//		sewMakerWing.Add(( (OCCShape) outerFace).getShape());
		//		sewMakerWing.Perform();
		//		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
		//		System.out.println("========== Sewing step successful? " + !sewMakerWing.IsNull());	
		//		System.out.println("========== Building the solid");
		//		
		//		CADSolid solidWing = null;
		//		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		//		solidMaker.Add(TopoDS.ToShell(sewedSection));
		//		solidMaker.Build();
		//		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		//		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		//		OCCShape solidWingShape = (OCCShape) solidWing;
		//		solidsMap.get(SolidType.WING_CUT).add(solidWingShape);

	}

	public static void makeSolidFlap(Map<SolidType, List<OCCShape>> solidsMap, List<List<CADWire>> airfoilsCutAndFlap) {
		// put in solidsMap the solid of a clean wing segment --> FLAP
		// solidsMap.put(SolidType.FLAP, value)

		CADWire wireInner = airfoilsCutAndFlap.get(1).get(0);
		CADWire wireOuter = airfoilsCutAndFlap.get(1).get(1);

		OCCShape Shape = OCCUtils.makePatchThruSections(airfoilsCutAndFlap.get(1));
		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		CADFace outerFace = OCCUtils.theFactory.newFacePlanar(wireOuter);

		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		sewMakerWing.Init();						
		sewMakerWing.Add(Shape.getShape());
		sewMakerWing.Add(( (OCCShape) innerFace).getShape());
		sewMakerWing.Add(( (OCCShape) outerFace).getShape());
		sewMakerWing.Perform();
		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
		System.out.println("========== Sewing step successful? " + !sewMakerWing.IsNull());	
		System.out.println("========== Building the solid");

		CADSolid solidWing = null;
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		solidMaker.Add(TopoDS.ToShell(sewedSection));
		solidMaker.Build();
		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		//		solidsMap.put(SolidType.FLAP, solidWing);
		OCCShape solidWingShape = (OCCShape) solidWing;
		solidsMap.get(SolidType.FLAP).add(solidWingShape);
		//		solidsMap.get(SolidType.WING_CUT).add(solidWing);

	}

	public static  List<List<CADWire>> makeAirfoilsSlat(LiftingSurface liftingSurface, List<CADEdge> airfoilsClean, List<List<CADWire>> airfoilsCutAndFlap, 
			double yInnerPct, double yOuterPct, double innerChordRatioSlat, double outerChordRatioSlat, boolean isFlapped, FlapType flapType, double lateralGap) {


		List<List<OCCShape>> exportAirfoilsCutAndFlap = new ArrayList<>();
		List<OCCShape> exportAirfoilsCut = new ArrayList<>();
		List<OCCShape> exportSlat = new ArrayList<>();

		List<List<CADWire>> airfoilsCutAndSlat = new ArrayList<>();
		List<CADWire> airfoilsCut = new ArrayList<>();
		List<CADWire> slat	 = new ArrayList<>();

		if (isFlapped) {
			// construct slat shape from the airfoilsCut list
			for( int i = 0; i < 2; i++ ) {

				double ChordRatio = 0;
				if( i == 0) {
					ChordRatio = innerChordRatioSlat;		
				}
				else {	
					ChordRatio = outerChordRatioSlat;	
				}

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

				CADGeomCurve3D chord = null;
				if(i == 0) {
					chord = getChordSegmentAtYActual(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}
				else {
					chord = getChordSegmentAtYActual(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}

				double chordLength = chord.length();
				OCCEdge chordEdge = (OCCEdge) chord.edge();
				//				OCCEdge airfoilEdge = (OCCEdge) airfoilsCutAndFlap.get(0).get(i);

				List<CADEdge> airfoilsCutEdges = airfoilsCutAndFlap.get(0).get(i).edges();
				System.out.println("Dimensioni edges per slat : " + airfoilsCutEdges.size());
				List<OCCEdge> airfoilsCutOCCEdges = new ArrayList<>();
				airfoilsCutEdges.forEach(e -> airfoilsCutOCCEdges.add((OCCEdge) e ));			
				TopoDS_Edge airfoilTopoDS_Edge = getLongestEdge(airfoilsCutOCCEdges);
				CADEdge airfoilCADEdge = (CADEdge) OCCUtils.theFactory.newShape(airfoilTopoDS_Edge);
				OCCEdge airfoilEdge = (OCCEdge) airfoilCADEdge;

				//				List<Geom_Curve> airfoilsCutCurves = new ArrayList<>();
				//				airfoilsCutEdges.forEach(e -> airfoilsCutCurves.add(BRep_Tool.Curve( ((OCCEdge) e).getShape(), new double[1], new double[1]) ));			


				Geom_Curve chordGeomCurve = BRep_Tool.Curve(chordEdge.getShape(), new double[1], new double[1]);
				Geom_Curve airfoilGeomCurve = BRep_Tool.Curve(airfoilEdge.getShape(), new double[1], new double[1]);

				// Creation of point A
				double cSlatParA = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(1 - ChordRatio) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt A = new gp_Pnt(airfoilGeomCurve.Value(cSlatParA).X(),
						airfoilGeomCurve.Value(cSlatParA).Y(),
						airfoilGeomCurve.Value(cSlatParA).Z());
				System.out.println("Point A coordinates : " + A.X() + " " + A.Y() + " " + A.Z());

				// Creation of point B
				double cSlatParB = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(1 - cSlatLower) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.LOWER_SIDE
						)[0];

				gp_Pnt B = new gp_Pnt(airfoilGeomCurve.Value(cSlatParB).X(),
						airfoilGeomCurve.Value(cSlatParB).Y(),
						airfoilGeomCurve.Value(cSlatParB).Z());
				System.out.println("Point B coordinates : " + B.X() + " " + B.Y() + " " + B.Z());

				// Splitting airfoil in point A and B
				double[] pntA = new double[] {A.X(), A.Y(), A.Z()};
				double[] pntB = new double[] {B.X(), B.Y(), B.Z()};
				CADGeomCurve3D airfoil_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntA).get(0));
				CADGeomCurve3D airfoil_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntA).get(1));
				CADEdge edge_airfoil_1 = airfoil_1.edge();
				CADEdge edge_airfoil_2 = airfoil_2.edge();
				List<OCCEdge> airfoil_edges = new ArrayList<>();
				airfoil_edges.add((OCCEdge) edge_airfoil_1);
				airfoil_edges.add((OCCEdge) edge_airfoil_2);

				TopoDS_Edge airfoilFirstCut = getShortestEdge(airfoil_edges);

				CADGeomCurve3D airfoil_3 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntB).get(0));
				CADGeomCurve3D airfoil_4 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntB).get(1));
				CADEdge edge_airfoil_3 = airfoil_3.edge();
				CADEdge edge_airfoil_4 = airfoil_4.edge();
				List<OCCEdge> airfoil_edges_2 = new ArrayList<>();
				airfoil_edges_2.add((OCCEdge) edge_airfoil_3);
				airfoil_edges_2.add((OCCEdge) edge_airfoil_4);

				TopoDS_Edge airfoilSecondCut = getShortestEdge(airfoil_edges_2);

				// Get tangent vectors in A and B

				gp_Vec tangPntA = new gp_Vec();
				gp_Pnt PntA = new gp_Pnt();
				airfoilGeomCurve.D1(cSlatParA, PntA, tangPntA);
				tangPntA.Normalize();
				tangPntA = tangPntA.Reversed();

				gp_Vec tangPntB = new gp_Vec();
				gp_Pnt PntB = new gp_Pnt();
				airfoilGeomCurve.D1(cSlatParB, PntB, tangPntB);
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
																		chordEdge.vertices()[0].pnt()[0],
																		chordEdge.vertices()[0].pnt()[1],
																		chordEdge.vertices()[0].pnt()[2]),
																lsAxis),
														(1 - cSlatMiddle) * chordLength)).Edge(), 
										new double[1], 
										new double[1]
										);

								double[] chorPar = getParamIntersectionPnts(chordGeomCurve, circle);
								gp_Vec chorDir = new gp_Vec();
								gp_Pnt C = new gp_Pnt();
								chordGeomCurve.D1(chorPar[0], C, chorDir);
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
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(1 - ChordRatio + deltaSlat1) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.UPPER_SIDE
										)[0];

								gp_Pnt D = new gp_Pnt(airfoilGeomCurve.Value(cSlatParD).X(),
										airfoilGeomCurve.Value(cSlatParD).Y(),
										airfoilGeomCurve.Value(cSlatParD).Z());
								System.out.println("Point D coordinates : " + D.X() + " " + D.Y() + " " + D.Z());

								double cSlatParE = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(1 - cSlatLower + deltaSlat2) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt E = new gp_Pnt(airfoilGeomCurve.Value(cSlatParE).X(),
										airfoilGeomCurve.Value(cSlatParE).Y(),
										airfoilGeomCurve.Value(cSlatParE).Z());
								System.out.println("Point E coordinates : " + E.X() + " " + E.Y() + " " + E.Z());

								// Splitting airfoil in point D and E
								double[] pntD = new double[] {D.X(), D.Y(), D.Z()};
								double[] pntE = new double[] {E.X(), E.Y(), E.Z()};
								CADGeomCurve3D airfoil_5 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntD).get(0));
								CADGeomCurve3D airfoil_6 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntD).get(1));
								CADEdge edge_airfoil_5 = airfoil_5.edge();
								CADEdge edge_airfoil_6 = airfoil_6.edge();
								List<OCCEdge> airfoil_edges_3 = new ArrayList<>();
								airfoil_edges_3.add((OCCEdge) edge_airfoil_5);
								airfoil_edges_3.add((OCCEdge) edge_airfoil_6);

								TopoDS_Edge slatFirstCut = getLongestEdge(airfoil_edges_3);

								// Slat detail
								// Point G
								double cSlatParG = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(1 - cSlatLower + deltaSlat2 + deltaSlat3) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt G = new gp_Pnt(airfoilGeomCurve.Value(cSlatParG).X(),
										airfoilGeomCurve.Value(cSlatParG).Y(),
										airfoilGeomCurve.Value(cSlatParG).Z());
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
								airfoilGeomCurve.D1(cSlatParD, PntD, tangPntD);
								tangPntD.Normalize();
								tangPntD = tangPntD.Reversed();

								gp_Vec tangPntE = new gp_Vec();
								gp_Pnt PntE = new gp_Pnt();
								airfoilGeomCurve.D1(cSlatParE, PntE, tangPntE);
								tangPntE.Normalize();

								// Creation of point F

								Geom_Curve circle2 = BRep_Tool.Curve(
										new BRepBuilderAPI_MakeEdge(
												new gp_Circ(
														new gp_Ax2(
																new gp_Pnt(
																		chordEdge.vertices()[0].pnt()[0],
																		chordEdge.vertices()[0].pnt()[1],
																		chordEdge.vertices()[0].pnt()[2]),
																lsAxis),
														(1 - cSlatMiddle + slatGap) * chordLength)).Edge(), 
										new double[1], 
										new double[1]
										);

								double[] chorPar2 = getParamIntersectionPnts(chordGeomCurve, circle2);
								gp_Vec chorDir2 = new gp_Vec();
								gp_Pnt F = new gp_Pnt();
								chordGeomCurve.D1(chorPar2[0], F, chorDir2);
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
								airfoilGeomCurve.D1(cSlatParG, PntG, tangPntG);
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


								//				TopoDS_Edge airfoilTE = new TopoDS_Edge();
								//				gp_Pnt startPnt1 = BRep_Tool.Pnt(TopExp.FirstVertex(airfoilFirstCut));
								//				gp_Pnt endPnt1 = BRep_Tool.Pnt(TopExp.LastVertex(airfoilSecondCut));
								//				BRepBuilderAPI_MakeEdge buildFlapTE = new BRepBuilderAPI_MakeEdge(startPnt1,endPnt1);
								//				airfoilTE = buildFlapTE.Edge();

								gp_Trsf flapTrasl = new gp_Trsf();
								if ( i == 0 ) {

									flapTrasl.SetTranslation(new gp_Pnt(0, yInnerPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
											new gp_Pnt(0, yInnerPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER) + lateralGap, 0));

								}
								else {

									flapTrasl.SetTranslation(new gp_Pnt(0, yOuterPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
											new gp_Pnt(0, yOuterPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap, 0));

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


								slat.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalSlatUpperLE),
										(CADEdge) OCCUtils.theFactory.newShape(finalSlat), 
										(CADEdge) OCCUtils.theFactory.newShape(finalSlatLowerLE1),
										(CADEdge) OCCUtils.theFactory.newShape(finalSlatLowerLE2)));

								exportSlat .add((OCCShape) OCCUtils.theFactory.newShape(finalSlat));
								exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatUpperLE));
								exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatLowerLE1));
								exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatLowerLE2));

								if(flapType == FlapType.NON_SYMMETRIC) {
									airfoilsCut.add(OCCUtils.theFactory.newWireFromAdjacentEdges(
											(CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut),
											(CADEdge) airfoilsCutOCCEdges.get(3),
											(CADEdge) airfoilsCutOCCEdges.get(2),
											(CADEdge) airfoilsCutOCCEdges.get(1)
											));




									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilSecondCut));
									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge));
									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge));
									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilFirstCut));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(1));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(2));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(3));
								}
								if(flapType == FlapType.SYMMETRIC) {

									airfoilsCut.add(OCCUtils.theFactory.newWireFromAdjacentEdges(
											(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut),
											(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge),
											(CADEdge) airfoilsCutOCCEdges.get(1),
											(CADEdge) airfoilsCutOCCEdges.get(2),
											(CADEdge) airfoilsCutOCCEdges.get(3),
											(CADEdge) airfoilsCutOCCEdges.get(4)
											));

									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilSecondCut));
									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge));
									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge));
									exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilFirstCut));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(1));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(2));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(3));
									exportAirfoilsCut.add((OCCShape) airfoilsCutOCCEdges.get(4));
								}

								//				exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilTE));
								//				List<CADEdge> airfoilsCutEdges = airfoilsCutAndFlap.get(0).get(i).edges();

			}
			airfoilsCutAndFlap.add(airfoilsCut);
			airfoilsCutAndFlap.add(slat);
			airfoilsCutAndSlat.add(airfoilsCut);
			airfoilsCutAndSlat.add(slat);
			exportAirfoilsCutAndFlap.add(exportSlat);
			exportAirfoilsCutAndFlap.add(exportAirfoilsCut);

		} else {
			for( int i = 0; i < 2; i++ ) {

				double ChordRatio = 0;
				if( i == 0) {
					ChordRatio = innerChordRatioSlat;		
				}
				else {	
					ChordRatio = outerChordRatioSlat;	
				}

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

				CADGeomCurve3D chord = null;
				if(i == 0) {
					chord = getChordSegmentAtYActual(yInnerPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}
				else {
					chord = getChordSegmentAtYActual(yOuterPct * liftingSurface.getSemiSpan().doubleValue(SI.METER), liftingSurface);
				}

				double chordLength = chord.length();
				OCCEdge chordEdge = (OCCEdge) chord.edge();
				OCCEdge airfoilEdge = (OCCEdge) airfoilsClean.get(i);

				Geom_Curve chordGeomCurve = BRep_Tool.Curve(chordEdge.getShape(), new double[1], new double[1]);
				Geom_Curve airfoilGeomCurve = BRep_Tool.Curve(airfoilEdge.getShape(), new double[1], new double[1]);

				// Creation of point A
				double cSlatParA = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(1 - ChordRatio) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.UPPER_SIDE
						)[0];

				gp_Pnt A = new gp_Pnt(airfoilGeomCurve.Value(cSlatParA).X(),
						airfoilGeomCurve.Value(cSlatParA).Y(),
						airfoilGeomCurve.Value(cSlatParA).Z());
				System.out.println("Point A coordinates : " + A.X() + " " + A.Y() + " " + A.Z());

				// Creation of point B
				double cSlatParB = getParamsIntersectionPntsOnAirfoil(
						airfoilGeomCurve, 
						chordGeomCurve, 
						chordLength, 
						(1 - cSlatLower) * chordLength, 
						chordEdge.vertices()[0].pnt(), 
						liftingSurface.getType(), 
						SideSelector.LOWER_SIDE
						)[0];

				gp_Pnt B = new gp_Pnt(airfoilGeomCurve.Value(cSlatParB).X(),
						airfoilGeomCurve.Value(cSlatParB).Y(),
						airfoilGeomCurve.Value(cSlatParB).Z());
				System.out.println("Point B coordinates : " + B.X() + " " + B.Y() + " " + B.Z());

				// Splitting airfoil in point A and B
				double[] pntA = new double[] {A.X(), A.Y(), A.Z()};
				double[] pntB = new double[] {B.X(), B.Y(), B.Z()};
				CADGeomCurve3D airfoil_1 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntA).get(0));
				CADGeomCurve3D airfoil_2 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntA).get(1));
				CADEdge edge_airfoil_1 = airfoil_1.edge();
				CADEdge edge_airfoil_2 = airfoil_2.edge();
				List<OCCEdge> airfoil_edges = new ArrayList<>();
				airfoil_edges.add((OCCEdge) edge_airfoil_1);
				airfoil_edges.add((OCCEdge) edge_airfoil_2);

				TopoDS_Edge airfoilFirstCut = getShortestEdge(airfoil_edges);

				CADGeomCurve3D airfoil_3 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntB).get(0));
				CADGeomCurve3D airfoil_4 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntB).get(1));
				CADEdge edge_airfoil_3 = airfoil_3.edge();
				CADEdge edge_airfoil_4 = airfoil_4.edge();
				List<OCCEdge> airfoil_edges_2 = new ArrayList<>();
				airfoil_edges_2.add((OCCEdge) edge_airfoil_3);
				airfoil_edges_2.add((OCCEdge) edge_airfoil_4);

				TopoDS_Edge airfoilSecondCut = getShortestEdge(airfoil_edges_2);

				// Get tangent vectors in A and B

				gp_Vec tangPntA = new gp_Vec();
				gp_Pnt PntA = new gp_Pnt();
				airfoilGeomCurve.D1(cSlatParA, PntA, tangPntA);
				tangPntA.Normalize();
				tangPntA = tangPntA.Reversed();

				gp_Vec tangPntB = new gp_Vec();
				gp_Pnt PntB = new gp_Pnt();
				airfoilGeomCurve.D1(cSlatParB, PntB, tangPntB);
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
																		chordEdge.vertices()[0].pnt()[0],
																		chordEdge.vertices()[0].pnt()[1],
																		chordEdge.vertices()[0].pnt()[2]),
																lsAxis),
														(1 - cSlatMiddle) * chordLength)).Edge(), 
										new double[1], 
										new double[1]
										);

								double[] chorPar = getParamIntersectionPnts(chordGeomCurve, circle);
								gp_Vec chorDir = new gp_Vec();
								gp_Pnt C = new gp_Pnt();
								chordGeomCurve.D1(chorPar[0], C, chorDir);
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
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(1 - ChordRatio + deltaSlat1) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.UPPER_SIDE
										)[0];

								gp_Pnt D = new gp_Pnt(airfoilGeomCurve.Value(cSlatParD).X(),
										airfoilGeomCurve.Value(cSlatParD).Y(),
										airfoilGeomCurve.Value(cSlatParD).Z());
								System.out.println("Point D coordinates : " + D.X() + " " + D.Y() + " " + D.Z());

								double cSlatParE = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(1 - cSlatLower + deltaSlat2) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt E = new gp_Pnt(airfoilGeomCurve.Value(cSlatParE).X(),
										airfoilGeomCurve.Value(cSlatParE).Y(),
										airfoilGeomCurve.Value(cSlatParE).Z());
								System.out.println("Point E coordinates : " + E.X() + " " + E.Y() + " " + E.Z());

								// Splitting airfoil in point D and E
								double[] pntD = new double[] {D.X(), D.Y(), D.Z()};
								double[] pntE = new double[] {E.X(), E.Y(), E.Z()};
								CADGeomCurve3D airfoil_5 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntD).get(0));
								CADGeomCurve3D airfoil_6 = OCCUtils.theFactory.newCurve3D(OCCUtils.splitCADCurve(airfoilEdge, pntD).get(1));
								CADEdge edge_airfoil_5 = airfoil_5.edge();
								CADEdge edge_airfoil_6 = airfoil_6.edge();
								List<OCCEdge> airfoil_edges_3 = new ArrayList<>();
								airfoil_edges_3.add((OCCEdge) edge_airfoil_5);
								airfoil_edges_3.add((OCCEdge) edge_airfoil_6);

								TopoDS_Edge slatFirstCut = getLongestEdge(airfoil_edges_3);

								// Slat detail
								// Point G
								double cSlatParG = getParamsIntersectionPntsOnAirfoil(
										airfoilGeomCurve, 
										chordGeomCurve, 
										chordLength, 
										(1 - cSlatLower + deltaSlat2 + deltaSlat3) * chordLength, 
										chordEdge.vertices()[0].pnt(), 
										liftingSurface.getType(), 
										SideSelector.LOWER_SIDE
										)[0];

								gp_Pnt G = new gp_Pnt(airfoilGeomCurve.Value(cSlatParG).X(),
										airfoilGeomCurve.Value(cSlatParG).Y(),
										airfoilGeomCurve.Value(cSlatParG).Z());
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
								airfoilGeomCurve.D1(cSlatParD, PntD, tangPntD);
								tangPntD.Normalize();
								tangPntD = tangPntD.Reversed();

								gp_Vec tangPntE = new gp_Vec();
								gp_Pnt PntE = new gp_Pnt();
								airfoilGeomCurve.D1(cSlatParE, PntE, tangPntE);
								tangPntE.Normalize();

								// Creation of point F

								Geom_Curve circle2 = BRep_Tool.Curve(
										new BRepBuilderAPI_MakeEdge(
												new gp_Circ(
														new gp_Ax2(
																new gp_Pnt(
																		chordEdge.vertices()[0].pnt()[0],
																		chordEdge.vertices()[0].pnt()[1],
																		chordEdge.vertices()[0].pnt()[2]),
																lsAxis),
														(1 - cSlatMiddle + slatGap) * chordLength)).Edge(), 
										new double[1], 
										new double[1]
										);

								double[] chorPar2 = getParamIntersectionPnts(chordGeomCurve, circle2);
								gp_Vec chorDir2 = new gp_Vec();
								gp_Pnt F = new gp_Pnt();
								chordGeomCurve.D1(chorPar2[0], F, chorDir2);
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
								airfoilGeomCurve.D1(cSlatParG, PntG, tangPntG);
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

									flapTrasl.SetTranslation(new gp_Pnt(0, yInnerPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
											new gp_Pnt(0, yInnerPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER) + lateralGap, 0));

								}
								else {

									flapTrasl.SetTranslation(new gp_Pnt(0, yOuterPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER), 0), 
											new gp_Pnt(0, yOuterPct *  liftingSurface.getSemiSpan().doubleValue(SI.METER) - lateralGap, 0));

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


								slat.add(OCCUtils.theFactory.newWireFromAdjacentEdges((CADEdge) OCCUtils.theFactory.newShape(finalSlatUpperLE),
										(CADEdge) OCCUtils.theFactory.newShape(finalSlat), 
										(CADEdge) OCCUtils.theFactory.newShape(finalSlatLowerLE1),
										(CADEdge) OCCUtils.theFactory.newShape(finalSlatLowerLE2)));

								exportSlat .add((OCCShape) OCCUtils.theFactory.newShape(finalSlat));
								exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatUpperLE));
								exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatLowerLE1));
								exportSlat.add((OCCShape) OCCUtils.theFactory.newShape(finalSlatLowerLE2));

								airfoilsCut.add(OCCUtils.theFactory.newWireFromAdjacentEdges(
										(CADEdge) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilFirstCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilTE),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilSecondCut),
										(CADEdge) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge)
										));
								exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilSecondCut));
								exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilUpperLE_Edge));
								exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilLowerLE_Edge));
								exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilFirstCut));
								exportAirfoilsCut.add((OCCShape) OCCUtils.theFactory.newShape(airfoilTE));

			}
			airfoilsCutAndFlap.add(airfoilsCut);
			airfoilsCutAndFlap.add(slat);
			airfoilsCutAndSlat.add(airfoilsCut);
			airfoilsCutAndSlat.add(slat);
			exportAirfoilsCutAndFlap.add(exportSlat);
			exportAirfoilsCutAndFlap.add(exportAirfoilsCut);
		}
		return airfoilsCutAndSlat;
	}

	public static void makeSolidSlat(Map<SolidType, List<OCCShape>> solidsMap, List<List<CADWire>> airfoilsCutAndFlap) {
		// put in solidsMap the solid of a clean wing segment --> SLAT
		// solidsMap.put(SolidType.SLAT, value)
		CADWire wireInner = airfoilsCutAndFlap.get(1).get(0);
		CADWire wireOuter = airfoilsCutAndFlap.get(1).get(1);

		OCCShape Shape = OCCUtils.makePatchThruSections(airfoilsCutAndFlap.get(1));
		CADFace innerFace = OCCUtils.theFactory.newFacePlanar(wireInner);
		CADFace outerFace = OCCUtils.theFactory.newFacePlanar(wireOuter);

		BRepBuilderAPI_Sewing sewMakerWing = new BRepBuilderAPI_Sewing();
		sewMakerWing.Init();						
		sewMakerWing.Add(Shape.getShape());
		sewMakerWing.Add(( (OCCShape) innerFace).getShape());
		sewMakerWing.Add(( (OCCShape) outerFace).getShape());
		sewMakerWing.Perform();
		TopoDS_Shape sewedSection = sewMakerWing.SewedShape();
		System.out.println("========== Sewing step successful? " + !sewMakerWing.IsNull());	
		System.out.println("========== Building the solid");

		CADSolid solidWing = null;
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		solidMaker.Add(TopoDS.ToShell(sewedSection));
		solidMaker.Build();
		System.out.println("Solid is done? " + (solidMaker.IsDone() == 1));
		solidWing = (CADSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		//		solidsMap.put(SolidType.SLAT, solidWing);
		OCCShape solidWingShape = (OCCShape) solidWing;
		solidsMap.get(SolidType.SLAT).add(solidWingShape);
		//		solidsMap.get(SolidType.SLAT).add(solidWing);

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

	public static double calcLength(TopoDS_Edge edge) {
		GProp_GProps prop = new GProp_GProps();
		BRepGProp.LinearProperties(edge,prop);
		return prop.Mass();

	}

	// Get shortest Edge from a list
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

	public static OCCShape symFlapRotation(LiftingSurface liftingSurface, double deflection, OCCShape flap, double innerHingePntx, double innerHingePntz,
			double outerHingePntx, double outerHingePntz)	{

		OCCShape rotatedFlap = null;
		double innerChordLength;
		double outerChordLength;

		ComponentEnum typeLS = liftingSurface.getType();


		if(typeLS != ComponentEnum.VERTICAL_TAIL) {
			CADGeomCurve3D innerChord =  getChordSegmentAtYActual(flap.boundingBox()[1], liftingSurface);
			innerChordLength = innerChord.length();
			CADGeomCurve3D outerChord =  getChordSegmentAtYActual(flap.boundingBox()[4], liftingSurface);
			outerChordLength = outerChord.length();
		} 
		else {

			CADGeomCurve3D innerChord =  getChordSegmentAtYActual(flap.boundingBox()[2], liftingSurface);
			innerChordLength = innerChord.length();
			CADGeomCurve3D outerChord =  getChordSegmentAtYActual(flap.boundingBox()[5], liftingSurface);
			outerChordLength = outerChord.length();

		}

		gp_Pnt innerHingePos = new gp_Pnt();
		gp_Pnt outerHingePos = new gp_Pnt();


		if (!typeLS.equals(ComponentEnum.VERTICAL_TAIL)) {

			//			double innerHingePntx = 0.62;
			//			double innerHingePntz = 0;
			//
			//			double outerHingePntx = 0.66;
			//			double outerHingePntz = 0;

			innerHingePos.SetX((innerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			innerHingePos.SetY(flap.boundingBox()[1]);
			innerHingePos.SetZ((innerChordLength * innerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

			outerHingePos.SetX((outerChordLength * outerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[4]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			outerHingePos.SetY(flap.boundingBox()[4]);
			outerHingePos.SetZ((outerChordLength * outerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

		} else {

			//			innerHingePos.SetX(24.109);
			//			innerHingePos.SetY(0);
			//			outerHingePos.SetZ(1.541);
			//
			//			outerHingePos.SetX(26.504);
			//			outerHingePos.SetY(0);
			//			outerHingePos.SetZ(5.686);

			innerHingePos.SetX((innerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			innerHingePos.SetY((innerChordLength * innerHingePntz));
			innerHingePos.SetZ(flap.boundingBox()[2]);

			outerHingePos.SetX((outerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
			outerHingePos.SetY((outerChordLength * innerHingePntz));
			outerHingePos.SetZ(flap.boundingBox()[5]);	
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

	public static OCCShape nonSymRotation(LiftingSurface liftingSurface, double deflection, OCCShape wing, OCCShape flap,
			double innerChordRatio, double outerChordRatio, double innerHingePntx, 
			double innerHingePntz, double outerHingePntx, double outerHingePntz) {

		OCCShape rotatedFlap = null;

		List<double[]> symFlapChordRatios = new ArrayList<>();
		symFlapChordRatios.add(new double[] {
				innerChordRatio,
				outerChordRatio
		});

		CADGeomCurve3D innerChord =  getChordSegmentAtYActual(flap.boundingBox()[1], liftingSurface);
		double innerChordLength = innerChord.length();
		CADGeomCurve3D outerChord =  getChordSegmentAtYActual(flap.boundingBox()[4], liftingSurface);
		double outerChordLength = outerChord.length();


		gp_Pnt innerHingePos = new gp_Pnt();
		gp_Pnt outerHingePos = new gp_Pnt();

		innerHingePos.SetX((innerChordLength * innerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[1]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
		innerHingePos.SetY(flap.boundingBox()[1]);
		innerHingePos.SetZ((innerChordLength * innerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

		outerHingePos.SetX((outerChordLength * outerHingePntx) + liftingSurface.getXLEAtYActual(flap.boundingBox()[4]).doubleValue(SI.METER) + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER));
		outerHingePos.SetY(flap.boundingBox()[4]);
		outerHingePos.SetZ((outerChordLength * outerHingePntz) + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER));

		System.out.println("Coord inner hinge : " + innerHingePos.X() + " " + innerHingePos.Y() + " " + innerHingePos.Z());
		System.out.println("Coord outer hinge : " + outerHingePos.X() + " " + outerHingePos.Y() + " " + outerHingePos.Z());

		// hinge axis rotation
		gp_Vec hingeVec = new gp_Vec(innerHingePos, outerHingePos);
		gp_Dir hingeDir = new gp_Dir(hingeVec);
		gp_Ax1 hingeAxis = new gp_Ax1(innerHingePos, hingeDir);
		gp_Trsf rotation0 = new gp_Trsf();
		rotation0.SetRotation(hingeAxis,deflection);

		
		gp_Pnt innerRefPnt = innerHingePos;
		gp_Pnt outerRefPnt = outerHingePos;

		gp_Pnt middleRefPnt = new gp_Pnt(
				(outerRefPnt.X() + innerRefPnt.X() ) * 0.5,
				(outerRefPnt.Y() + innerRefPnt.Y() ) * 0.5,
				(outerRefPnt.Z() + innerRefPnt.Z() ) * 0.5);


		System.out.println("Coordinate innerRefPnt finale : " + innerRefPnt.X() + " " + innerRefPnt.Y() + " " + innerRefPnt.Z());
		System.out.println("Coordinate outerRefPnt finale : " + outerRefPnt.X() + " " + outerRefPnt.Y() + " " + outerRefPnt.Z());
		System.out.println("Coordinate middleRefPnt finale : " + middleRefPnt.X() + " " + middleRefPnt.Y() + " " + middleRefPnt.Z());

		gp_Vec refVec = new gp_Vec(innerRefPnt, outerRefPnt);
		gp_Dir refDir = new gp_Dir(refVec);
		gp_Ax1 refAxis = new gp_Ax1(innerRefPnt, refDir);

		// Rotation0New --> Hinge axis and reference line are the same
		gp_Trsf rotation0New = new gp_Trsf();
		rotation0New.SetRotation(refAxis,deflection);

		//  reference point, flap extracted

		double innerGap =  -0.007; // 2.8%
		double innerOverlap = 0.26;  // 4%
		double outerGap = -0.007;
		double outerOverlap = 0.26;

		gp_Pnt innerRefPntExtr = new gp_Pnt(
				innerRefPnt.X() + innerOverlap,
				innerRefPnt.Y(),
				innerRefPnt.Z() + innerGap);

		gp_Pnt outerRefPntExtr = new gp_Pnt(
				outerRefPnt.X() + outerOverlap,
				outerRefPnt.Y(),
				outerRefPnt.Z() + outerGap);

		gp_Pnt middleRefPntExtr = new gp_Pnt(
				(outerRefPntExtr.X() + innerRefPntExtr.X() ) * 0.5,
				(outerRefPntExtr.Y() + innerRefPntExtr.Y() ) * 0.5,
				(outerRefPntExtr.Z() + innerRefPntExtr.Z() ) * 0.5);

		System.out.println("Coordinate innerRefPnt extr : " + innerRefPntExtr.X() + " " + innerRefPntExtr.Y() + " " + innerRefPntExtr.Z());
		System.out.println("Coordinate outerRefPnt extr : " + outerRefPntExtr.X() + " " + outerRefPntExtr.Y() + " " + outerRefPntExtr.Z());
		System.out.println("Coordinate middleRefPnt extr : " + middleRefPntExtr.X() + " " + middleRefPntExtr.Y() + " " + middleRefPntExtr.Z());

		// flap translation

		gp_Trsf translation = new gp_Trsf();
		translation.SetTranslation(middleRefPnt, middleRefPntExtr);

		// creation of roll and yaw axes

		gp_Vec rollVec = new gp_Vec(middleRefPntExtr, 
				new gp_Pnt(
						middleRefPntExtr.X() + 1,
						middleRefPntExtr.Y(),
						middleRefPntExtr.Z()));
		gp_Dir rollDir = new gp_Dir(rollVec);
		gp_Ax1 rollAxis = new gp_Ax1(middleRefPntExtr, rollDir);

		gp_Vec yawVec = new gp_Vec(middleRefPntExtr, 
				new gp_Pnt(
						middleRefPntExtr.X(),
						middleRefPntExtr.Y(),
						middleRefPntExtr.Z() + 1 ));
		gp_Dir yawDir = new gp_Dir(yawVec);
		gp_Ax1 yawAxis = new gp_Ax1(middleRefPntExtr, yawDir);

		// roll fix rotation

		gp_Pnt P1 = new gp_Pnt(
				middleRefPntExtr.X(),
				innerRefPnt.Y(),
				innerRefPnt.Z()
				);

		gp_Pnt P2 = new gp_Pnt(
				middleRefPntExtr.X(),
				outerRefPnt.Y(),
				outerRefPnt.Z()
				);

		gp_Pnt P3 = new gp_Pnt(
				middleRefPntExtr.X(),
				innerRefPntExtr.Y(),
				innerRefPntExtr.Z()
				);

		gp_Pnt P4 = new gp_Pnt(
				middleRefPntExtr.X(),
				outerRefPntExtr.Y(),
				outerRefPntExtr.Z()
				);

		double m1 = (P1.Z() - P2.Z()) / (P1.Y() - P2.Y());
		double m2 = (P3.Z() - P4.Z()) / (P3.Y() - P4.Y());

		double tanGamma = (m1-m2)/(1+m1*m2);
		double gamma = Math.atan(tanGamma);
		System.out.println("Angolo rotazione di roll : " + gamma);

		gp_Trsf rollRotation = new gp_Trsf();
		rollRotation.SetRotation(rollAxis,-gamma);

		// yaw fix rotation

		gp_Pnt P5 = new gp_Pnt(
				innerRefPnt.X(),
				innerRefPnt.Y(),
				middleRefPntExtr.Z()
				);

		gp_Pnt P6 = new gp_Pnt(
				outerRefPnt.X(),
				outerRefPnt.Y(),
				middleRefPntExtr.Z()
				);

		gp_Pnt P7 = new gp_Pnt(
				innerRefPntExtr.X(),
				innerRefPntExtr.Y(),
				middleRefPntExtr.Z()
				);

		gp_Pnt P8 = new gp_Pnt(
				outerRefPntExtr.X(),
				outerRefPntExtr.Y(),
				middleRefPntExtr.Z()
				);

		double m3 = (P5.Y() - P6.Y()) / (P5.X() - P6.X());
		double m4 = (P7.Y() - P8.Y()) / (P7.X() - P8.X());

		double tanBeta = (m3 - m4) / (1 + m3 * m4);
		double beta = Math.atan(tanBeta);
		if (Double.isNaN(beta)) {
			beta = 0;
		}

		gp_Trsf yawRotation = new gp_Trsf();
		yawRotation.SetRotation(yawAxis,-beta);
		System.out.println("Angolo rotazione di yaw : " + beta);

		// flap movment

		BRepBuilderAPI_Transform transf0 = new BRepBuilderAPI_Transform(flap.getShape(), rotation0New);
		TopoDS_Shape flapRotation0 = transf0.Shape();
		BRepBuilderAPI_Transform transf1 = new BRepBuilderAPI_Transform(flapRotation0, translation);
		TopoDS_Shape flapTranslation = transf1.Shape();
		BRepBuilderAPI_Transform transf2 = new BRepBuilderAPI_Transform(flapTranslation, rollRotation);
		TopoDS_Shape flapRollRotation = transf2.Shape();
		BRepBuilderAPI_Transform transf3 = new BRepBuilderAPI_Transform(flapRollRotation, yawRotation);
		TopoDS_Shape flapYawRotation = transf3.Shape();

		rotatedFlap = (OCCShape) OCCUtils.theFactory.newShape(flapYawRotation);

		return rotatedFlap;		
	}

}
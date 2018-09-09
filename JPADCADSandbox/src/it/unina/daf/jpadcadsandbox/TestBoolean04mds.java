package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.TestBoolean03mds.SideSelector;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import javaslang.Tuple2;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Section;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepCheck_Analyzer;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRep_Tool;
import opencascade.GC_MakePlane;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.Geom_Curve;
import opencascade.Geom_Surface;
import opencascade.ShapeFix_Shape;
import opencascade.TopAbs_Orientation;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
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
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class TestBoolean04mds {

	// Testing OpenCascade boolean operations
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		OCCUtils.initCADShapeFactory();
		
		// Import a solid wing		
		LiftingSurface wing = AircraftUtils.importAircraft(args).getWing();
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(
				wing, 
				wing.getType(), 
				1e-3, 
				false, 
				true, 
				false
				);
		
		if(wingShapes.size() == 1) {
			if(wingShapes.get(0).getShape().ShapeType().equals(TopAbs_ShapeEnum.TopAbs_SOLID))
				System.out.println("========== 1 solid found in generated wing shapes...");
			else {
				System.out.println("Error: shape contained in wing shapes is not a solid...");
				return;
			}
		}
		else {
			System.out.println("========== Error: more than 1 solid found in wing shapes...");
			return;
		}		
		
		TopoDS_Solid wingSolid = TopoDS.ToSolid(wingShapes.get(0).getShape());
		BRepCheck_Analyzer analyzer = new BRepCheck_Analyzer(wingSolid);
		System.out.println("========== Is solid wing valid? " + (analyzer.IsValid() == 1));
		
		// Acquire control surfaces data
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
		System.out.println("========== Symmetric flaps spanwise position:");
		symFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("========== Symmetric flaps chord ratios:");
		symFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));
		
		// Adjust control surfaces positions (whether necessary)
		double adjustmentPar = 0.01;
		List<Double> etaBreakPnts = wing.getEtaBreakPoints();
		System.out.println("========== Lifting surface eta breakpoints: " + 
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
		System.out.println("========== Symmetric flaps adjusted spanwise position:");
		symFlapStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("========== Symmetric flaps adjusted chord ratios:");
		symFlapChordRatios.forEach(d -> System.out.println(Arrays.toString(d)));
		
		// Prepare cutting sections lists
		List<double[]> symFlapYStations = new ArrayList<>();
		List<double[]> symFlapChords = new ArrayList<>();	
		List<double[]> chordLengths = new ArrayList<>();
		
		for(int i = 0; i < numSymFlap; i++) {
			
			double[] stations = symFlapStations.get(i);
			double[] chordRatios = symFlapChordRatios.get(i);
			
			double flapRelLength = stations[1] - stations[0];
			
			int numInterStations = (flapRelLength < 0.3) ? 4 : 7;
			
			double[] yStations = MyArrayUtils.linspace(
					stations[0]*wingSemiSpan, 
					stations[1]*wingSemiSpan, 
					numInterStations
					);
			
			double[] flapChords = MyArrayUtils.linspace(
					chordRatios[0]*wing.getChordAtYActual(stations[0]*wingSemiSpan), 
					chordRatios[1]*wing.getChordAtYActual(stations[1]*wingSemiSpan), 
					numInterStations
					);
			
			double[] chords = MyArrayUtils.linspace(
					wing.getChordAtYActual(stations[0]*wingSemiSpan), 
					wing.getChordAtYActual(stations[1]*wingSemiSpan), 
					numInterStations
					);
			
			symFlapYStations.add(yStations);
			symFlapChords.add(flapChords);
			chordLengths.add(chords);
		}
		System.out.println("========== Symmetric flaps y stations:");
		symFlapYStations.forEach(d -> System.out.println(Arrays.toString(d)));
		System.out.println("========== Symmetric flaps chord distribution at y stations:");
		symFlapChords.forEach(d -> System.out.println(Arrays.toString(d)));
		
		// Get airfoils at calculated y stations
		List<TopoDS_Shape[]> wingSections = new ArrayList<>();

		gp_Dir sectionNormalAxis = (wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) ? 
				new gp_Dir(0.0, 0.0, 1.0) : new gp_Dir(0.0, 1.0, 0.0);
		System.out.println("========== getSymmetricFlaps: lifting surface sections creation...");		
		for(int i = 0; i < numSymFlap; i++) {			
			int numYs = symFlapYStations.get(i).length; 
			
			TopoDS_Shape[] shapesArray = new TopoDS_Shape[numYs];		
			for(int j = 0; j < numYs; j++) {	
				System.out.println("Flap #" + (i+1) + " section #" + (j+1) + " ...");
				
				BRepAlgoAPI_Section sectionMaker = new BRepAlgoAPI_Section();
				gp_Pnt sectionOrigin = (wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) ?
						new gp_Pnt(0.0, 0.0, symFlapYStations.get(i)[j] + wing.getZApexConstructionAxes().doubleValue(SI.METER)) : 
						new gp_Pnt(0.0, symFlapYStations.get(i)[j] + wing.getYApexConstructionAxes().doubleValue(SI.METER), 0.0);
				sectionMaker.Init1(wingSolid);
				sectionMaker.Init2(makeIntersectionPlane(sectionOrigin, sectionNormalAxis));
				sectionMaker.Build();
				shapesArray[j] = sectionMaker.Shape();
				
				System.out.println("Section done!");
			}			
			wingSections.add(shapesArray);
		}
		
		// Manage sections in order to extract desired edges
		List<TopoDS_Edge[]> mainAirfoils = new ArrayList<>();
		List<TopoDS_Edge[]> trailingEdges = new ArrayList<>();
		
		for(int i = 0; i < numSymFlap; i++) {
			int numYs = symFlapYStations.get(i).length;
			
			TopoDS_Edge[] airfs = new TopoDS_Edge[numYs];
			TopoDS_Edge[] tEdgs = new TopoDS_Edge[numYs];		
			for(int j = 0; j < numYs; j++) {
				TopExp_Explorer explorer = new TopExp_Explorer(wingSections.get(i)[j], TopAbs_ShapeEnum.TopAbs_EDGE);
				while(explorer.More() > 0) {
					TopoDS_Edge edge = TopoDS.ToEdge(explorer.Current());
					if(calculateEdgeLength(edge) > symFlapChords.get(i)[j]) 
						airfs[j] = edge.Orientation().equals(TopAbs_Orientation.TopAbs_REVERSED) ?
								getReversedEdge(edge) : edge;
					else
						tEdgs[j] = edge;
					explorer.Next();
				}							
			}
			mainAirfoils.add(airfs);
			trailingEdges.add(tEdgs);
		}
		
		// Generate airfoil chords		
		List<OCCEdge[]> chordEdges = new ArrayList<>();
		for(int i = 0; i < numSymFlap; i++) {
			int numYs = symFlapYStations.get(i).length;
			
			OCCEdge[] chordsArray = new OCCEdge[numYs];
			for(int j = 0; j < numYs; j++) {
				chordsArray[j] = (OCCEdge) getChordSegmentAtYActual(
						symFlapYStations.get(i)[j], 
						wing
						).edge();
			}
			chordEdges.add(chordsArray);
		}
		
		// Generate wires at y station
		double leapFactor = 0.55;
		
		List<TopoDS_Wire[]> wingCutWires = new ArrayList<>();
		List<TopoDS_Wire[]> flapCutWires = new ArrayList<>();
		
		List<gp_Pnt[]> flapRotationPnts = new ArrayList<>();
		
		System.out.println("========== getSymmetricFlaps: wires creation...");
		for(int i = 0; i < numSymFlap; i++) {
			int numYs = symFlapYStations.get(i).length;
			
			TopoDS_Wire[] wireWingArray = new TopoDS_Wire[numYs];
			TopoDS_Wire[] wireFlapArray = new TopoDS_Wire[numYs];
			
			gp_Pnt[] rotPntsArray = new gp_Pnt[numYs];
			
			for(int j = 0; j < numYs; j++) {
				
				double cWing = chordLengths.get(i)[j];
				double cFlap = symFlapChords.get(i)[j];
				double cLeap = cFlap*leapFactor;
				double[] teCoords = chordEdges.get(i)[j].vertices()[0].pnt();
				
				Geom_Curve airfoil = BRep_Tool.Curve(mainAirfoils.get(i)[j], new double[1], new double[1]);
				Geom_Curve chord = BRep_Tool.Curve(chordEdges.get(i)[j].getShape(), new double[1], new double[1]);
				
				double cFlapPar = getParamsIntersectionPntsOnAirfoil(
						airfoil, 
						chord, 
						cWing, 
						cFlap, 
						teCoords, 
						wing.getType(), 
						SideSelector.LOWER_SIDE
						)[0];
				double cLeapPar = getParamsIntersectionPntsOnAirfoil(
						airfoil, 
						chord, 
						cWing, 
						cLeap, 
						teCoords, 
						wing.getType(), 
						SideSelector.UPPER_SIDE
						)[0];
				
				System.out.println("Flap #" + (i+1) + " section #" + (j+1) + " (y = " + symFlapYStations.get(i)[j] + "m), "
						           + "cFlap cut on airfoil parameter: [" + cFlapPar + "]; "
						           + "cLeap cut on airfoil parameter: [" + cLeapPar + "]");
				
				gp_Vec cFlapTang = new gp_Vec();
				gp_Vec cLeapTang = new gp_Vec();
				gp_Pnt cFlapPnt = new gp_Pnt();
				gp_Pnt cLeapPnt = new gp_Pnt();
				airfoil.D1(cFlapPar, cFlapPnt, cFlapTang);
				airfoil.D1(cLeapPar, cLeapPnt, cLeapTang);
				cFlapTang.Normalize();
				cLeapTang.Normalize();
				
				cLeapTang.Add(new gp_Vec(0.0, 0.0, Math.abs(cLeapTang.Z())*0.25));
				cLeapTang.Normalize();
				
				rotPntsArray[j] = cFlapPnt;
				
				gp_Vec zyDir = wing.getType().equals(ComponentEnum.VERTICAL_TAIL) ? // auxiliary vectors, useful for wire construction
						new gp_Vec(0.0, 0.0, 1.0) : new gp_Vec(0.0, 1.0, 0.0);
				gp_Vec yzDir = wing.getType().equals(ComponentEnum.VERTICAL_TAIL) ? 
						new gp_Vec(0.0, -1.0, 0.0) : new gp_Vec(0.0, 0.0, 1.0);
						
				// generate wires for wing cutting
				List<gp_Pnt> wirePointsWing = new ArrayList<>();		

				gp_Vec normUppWing = cLeapTang.Crossed(zyDir).Normalized();
				gp_Vec normLowWing = cFlapTang.Crossed(zyDir).Normalized();
				
				gp_Pnt p1W = cLeapPnt;	
				gp_Pnt p6W = cFlapPnt;
				gp_Pnt p2W = new gp_Pnt(normUppWing.Multiplied(cFlap).Added(new gp_Vec(p1W.Coord())).XYZ());				
				gp_Pnt p3W = new gp_Pnt(new gp_Vec(1, 0, 0).Multiplied(2*cFlap).Added(new gp_Vec(p2W.Coord())).XYZ());
				gp_Pnt p4W = new gp_Pnt(yzDir.Multiplied(-2*cFlap).Added(new gp_Vec(p3W.Coord())).XYZ());
				gp_Pnt p5W = new gp_Pnt(normLowWing.Multiplied(cFlap).Added(new gp_Vec(p6W.Coord())).XYZ()); 
				p5W.SetZ(p4W.Z());
				
				wirePointsWing.add(p1W);
				wirePointsWing.add(p2W);
				wirePointsWing.add(p3W);
				wirePointsWing.add(p4W);
				wirePointsWing.add(p5W);
				wirePointsWing.add(p6W);
				
				BRepBuilderAPI_MakeWire wireWingMaker = new BRepBuilderAPI_MakeWire();
				for(int k = 0; k < 5; k++) {
					BRepBuilderAPI_MakeEdge wireWingEdge = new BRepBuilderAPI_MakeEdge(
							wirePointsWing.get(k), 
							wirePointsWing.get(k+1)
							);
					wireWingMaker.Add(wireWingEdge.Edge());
				}
				
				List<double[]> wingFlapCurvePnts = new ArrayList<>();
				wingFlapCurvePnts.add(convertGpPntToDoubleArray(p6W));
				wingFlapCurvePnts.add(convertGpPntToDoubleArray(p1W));
				Geom_Curve wingFlapCurve = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
						wingFlapCurvePnts, 
						false, 
						new double[] {yzDir.X(), yzDir.Y(), yzDir.Z()}, 
						new double[] {cLeapTang.X(), cLeapTang.Y(), cLeapTang.Z()},
						false
						)).getAdaptorCurve().Curve();
				wireWingMaker.Add(new BRepBuilderAPI_MakeEdge(wingFlapCurve).Edge());			
				wireWingArray[j] = wireWingMaker.Wire();
				
				// generate wires for flap cutting
				List<gp_Pnt> wirePointsFlap = new ArrayList<>();
				
				double wingFlapCurvePercent = 0.10;
				double pntOnWingFlapCurvePar = wingFlapCurve.FirstParameter() + 
						(wingFlapCurve.LastParameter() - wingFlapCurve.FirstParameter())*wingFlapCurvePercent;
				gp_Pnt pntOnWingFlapCurve = new gp_Pnt();
				gp_Vec tngOnWingFlapCurve = new gp_Vec();
				wingFlapCurve.D1(pntOnWingFlapCurvePar, pntOnWingFlapCurve, tngOnWingFlapCurve);
				
				double flapChordPercent = 0.95;
				double pntOnAirfoilParam = getParamsIntersectionPntsOnAirfoil(
						airfoil, chord, cWing, cFlap*flapChordPercent, teCoords, wing.getType(), SideSelector.LOWER_SIDE)[0];
				gp_Pnt pntOnAirfoil = new gp_Pnt();
				gp_Vec tngOnAirfoil = new gp_Vec();
				airfoil.D1(pntOnAirfoilParam, pntOnAirfoil, tngOnAirfoil);
				
				tngOnWingFlapCurve.Normalize();
				tngOnAirfoil.Normalize();
				
				gp_Vec normUppFlap = tngOnWingFlapCurve.Crossed(zyDir).Normalized();
				gp_Vec normLowFlap = tngOnAirfoil.Crossed(zyDir).Normalized();
				
				gp_Pnt p1F = pntOnWingFlapCurve;
				gp_Pnt p6F = pntOnAirfoil;
				gp_Pnt p2F = new gp_Pnt(normUppFlap.Multiplied(cFlap).Added(new gp_Vec(p1F.Coord())).XYZ());	
				gp_Pnt p3F = new gp_Pnt(new gp_Vec(1, 0, 0).Multiplied(-cFlap).Added(new gp_Vec(p2F.Coord())).XYZ());
				gp_Pnt p4F = new gp_Pnt(yzDir.Multiplied(-2*cFlap).Added(new gp_Vec(p3F.Coord())).XYZ());
				gp_Pnt p5F = new gp_Pnt(normLowFlap.Multiplied(cFlap).Added(new gp_Vec(p6F.Coord())).XYZ());
				p5F.SetZ(p4F.Z());
				
				wirePointsFlap.add(p1F);
				wirePointsFlap.add(p2F);
				wirePointsFlap.add(p3F);
				wirePointsFlap.add(p4F);
				wirePointsFlap.add(p5F);
				wirePointsFlap.add(p6F);
				
				BRepBuilderAPI_MakeWire wireFlapMaker = new BRepBuilderAPI_MakeWire();
				for(int k = 0; k < 5; k++) {
					BRepBuilderAPI_MakeEdge wireFlapEdge = new BRepBuilderAPI_MakeEdge(
							wirePointsFlap.get(k), 
							wirePointsFlap.get(k+1)
							);
					wireFlapMaker.Add(wireFlapEdge.Edge());
				}
				
				List<double[]> flapFlapCurvePnts = new ArrayList<>();
				flapFlapCurvePnts.add(convertGpPntToDoubleArray(p6F));
				flapFlapCurvePnts.add(convertGpPntToDoubleArray(p1F));
				
				Geom_Curve flapCurveF = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
						flapFlapCurvePnts, 
						false, 
						new double[] {tngOnAirfoil.X(), tngOnAirfoil.Y(), tngOnAirfoil.Z()}, 
						new double[] {tngOnWingFlapCurve.X(), tngOnWingFlapCurve.Y(), tngOnWingFlapCurve.Z()},
						false
						)).getAdaptorCurve().Curve();
				wireFlapMaker.Add(new BRepBuilderAPI_MakeEdge(flapCurveF).Edge());
				wireFlapArray[j] = wireFlapMaker.Wire();
			}
			wingCutWires.add(wireWingArray);
			flapCutWires.add(wireFlapArray);
			flapRotationPnts.add(rotPntsArray);
		}
		
		// Create solids for wing and flap cutting
		List<TopoDS_Solid> wingCuttingSolids = new ArrayList<>();
		List<TopoDS_Solid> flapCuttingSolids = new ArrayList<>();
		
		System.out.println("========== getSymmetricFlaps: cutting solids creation...");
		for(int i = 0; i < numSymFlap; i++) {			
			wingCuttingSolids.add(generateSolidFromWires(wingCutWires.get(i)));
			flapCuttingSolids.add(generateSolidFromWires(flapCutWires.get(i)));
		}
		
		// Mirror solids whether necessary
		if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			System.out.println("========== getSymmetricFlaps: mirroring cutting solids...");
			
			wingCuttingSolids.addAll(wingCuttingSolids.stream()
					.map(s -> TopoDS.ToSolid(mirrorShapeRespectACSymPlane(s)))
					.collect(Collectors.toList())
					);
			flapCuttingSolids.addAll(flapCuttingSolids.stream()
					.map(s -> TopoDS.ToSolid(mirrorShapeRespectACSymPlane(s)))
					.collect(Collectors.toList())
					);
		}
		
		// Wing cut execution
		TopoDS_Solid cutWing = wingSolid;
		
		System.out.println("========== getSymmetricFlaps: wing cutting execution...");
		for(int i = 0; i < wingCuttingSolids.size(); i++) {
			cutWing = TopoDS.ToSolid(performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(wingCuttingSolids.get(i), false), 
					new Tuple2<TopoDS_Solid, Boolean>(cutWing, false)
					).get(0).Complemented());
			System.out.println("Wing cut #" + (i+1) + " succesfully executed.");
		}
		cutWing.Complement();
		
		// Flaps creation
		List<TopoDS_Solid> flaps = new ArrayList<>();
		
		System.out.println("========== getSymmetricFlaps: flaps creation...");
		for(int i = 0; i < numSymFlap; i++) {		
			System.out.println("- Flap #" + (i+1) + " cutting execution, please wait...");
			
			TopoDS_Solid auxCutWing = performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(wingCuttingSolids.get(i), false), 
					new Tuple2<TopoDS_Solid, Boolean>(wingSolid, false)
					).get(0);
			System.out.println("Auxiliary wing cutting solid created.");
			
			TopoDS_Solid flap = performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(wingSolid, true), 
					new Tuple2<TopoDS_Solid, Boolean>(auxCutWing, false)
					).get(0);
			System.out.println("Flap succesfully created.");
			
			flaps.add(performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(flap, false), 
					new Tuple2<TopoDS_Solid, Boolean>(flapCuttingSolids.get(i), false)
					).get(0));
			System.out.println("Flap leading edge adjustment executed correctly.");
		}
		
		if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			for(int i = 0; i < numSymFlap; i++) 
				flaps.add(TopoDS.ToSolid(mirrorShapeRespectACSymPlane(flaps.get(i))));
		}
		
		// Translate and rotate flaps
		gp_Trsf translation = new gp_Trsf();
		gp_Trsf pitchRotation = new gp_Trsf();
		
		double pitchRotDeg = 35;

		// create rotation axis	
		List<gp_Ax1> rotAxes = new ArrayList<>();
		List<gp_Ax1> mirRotAxes = new ArrayList<>();
		
		for(int i = 0; i < numSymFlap; i++) {
			int numYs = flapRotationPnts.get(i).length;
			
			gp_Pnt innRotPnt = flapRotationPnts.get(i)[0];
			gp_Pnt outRotPnt = flapRotationPnts.get(i)[numYs-1];
			
			TopoDS_Edge rotEdge = new BRepBuilderAPI_MakeEdge(innRotPnt, outRotPnt).Edge();
			rotAxes.add(convertEdgeToGpAx1(rotEdge, false));
			
			if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				TopoDS_Edge mirRotEdge = TopoDS.ToEdge(mirrorShapeRespectACSymPlane(rotEdge));
				mirRotAxes.add(convertEdgeToGpAx1(mirRotEdge, true));
			}
		}
		rotAxes.addAll(mirRotAxes);
	
		if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL))
			symFlapChords.addAll(symFlapChords);
		
		// move flaps
		List<TopoDS_Solid> movedFlaps = new ArrayList<>();
		
		System.out.println("========== getSymmetricFlaps: flaps rotation and translation execution...");
		System.out.println("Rotation angle is set to " + pitchRotDeg + "°.");
		for(int i = 0; i < flaps.size(); i++) {
			double tx = MyMathUtils.arithmeticMean(
					symFlapChords.get(i)[0],
					symFlapChords.get(i)[symFlapChords.get(i).length-1]		
					);

			translation.SetTranslation(new gp_Vec(0.3*tx, 0.0, 0.0*tx));
			pitchRotation.SetRotation(
					rotAxes.get(i),
					Math.toRadians(pitchRotDeg)
					);

			movedFlaps.add(TopoDS.ToSolid(new BRepBuilderAPI_Transform(
					TopoDS.ToSolid(
							new BRepBuilderAPI_Transform(
									flaps.get(i), 
									pitchRotation, 
									0).Shape()), 
					translation, 
					0).Shape()));	
		}
		
		// Export shapes to CAD file
		List<OCCShape> exportShapes = new ArrayList<>();
		
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(wingSolid));
//		wingSections.forEach(sa -> Arrays.asList(sa).forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s))));
//		wingCutWires.forEach(sa -> Arrays.asList(sa).forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s))));
//		flapCutWires.forEach(sa -> Arrays.asList(sa).forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s))));
//		wingCuttingSolids.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));
//		flapCuttingSolids.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutWing));
		movedFlaps.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));

		String fileName = "testBoolean04mds.brep";
		if(OCCUtils.write(fileName, exportShapes)) {
			System.out.println("========== [main] ... exporting ...");
			System.out.println("========== [main] Output written on file: " + fileName);
		}
		
	}

	public static Geom_Surface makeIntersectionPlane(gp_Pnt planeOrigin, gp_Dir planeNormalAxis) {
		GC_MakePlane plane = new GC_MakePlane(planeOrigin, planeNormalAxis);
		return plane.Value();
	}
	
	public static double calculateEdgeLength(TopoDS_Edge edge) {		
		GProp_GProps props = new GProp_GProps();
		BRepGProp.LinearProperties(edge, props);	
		return props.Mass();
	}
	
	public static TopoDS_Edge getReversedEdge(TopoDS_Edge edge) {
		Geom_Curve curve = BRep_Tool.Curve(edge, new double[1], new double[1]);	
		curve.Reverse();
		return new BRepBuilderAPI_MakeEdge(curve).Edge();
	}
	
	public static double[] convertGpPntToDoubleArray(gp_Pnt gpPnt) {
		return new double[] {gpPnt.X(), gpPnt.Y(), gpPnt.Z()};
	}
	
	public static gp_Ax1 convertEdgeToGpAx1(TopoDS_Edge edge, boolean reversed) {
		gp_Ax1 axis = new gp_Ax1();

		double[] par1 = new double[1];
		double[] par2 = new double[1];
		Geom_Curve axisCurve = BRep_Tool.Curve(edge, par1, par2);
		gp_Pnt vtx1 = axisCurve.Value(par1[0]);
		gp_Pnt vtx2 = axisCurve.Value(par2[0]);
		gp_Dir aDir = (reversed) ? 
				new gp_Dir(new gp_Vec(vtx2, vtx1)) : new gp_Dir(new gp_Vec(vtx1, vtx2));
		
		axis.SetLocation(vtx1);
		axis.SetDirection(aDir);
		
		return axis;	
	}
	
	public static TopoDS_Shape mirrorShapeRespectACSymPlane(TopoDS_Shape shape) {
		gp_Trsf mirroring = new gp_Trsf();
		gp_Ax2 mirrorPlane = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, 0.0), 
				new gp_Dir(0.0, 1.0, 0.0),
				new gp_Dir(1.0, 0.0, 0.0)
				);
		mirroring.SetMirror(mirrorPlane);
	
		return new BRepBuilderAPI_Transform(shape, mirroring, 1).Shape();
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
//		cutter.SetFuzzyValue(1e-5);
		TopoDS_Shape cutResult = cutter.Shape();                                            
		TopExp_Explorer exp = new TopExp_Explorer(cutResult, TopAbs_ShapeEnum.TopAbs_SOLID);
		while(exp.More() > 0) {
			cutSolid.add(TopoDS.ToSolid(exp.Current()));
			exp.Next();
		}

		return cutSolid;
	}
	
	public static TopoDS_Solid generateSolidFromWires(TopoDS_Wire[] wiresArray) {
		return generateSolidFromWires(Arrays.asList(wiresArray));		
	}
	
	public static TopoDS_Solid generateSolidFromWires(List<TopoDS_Wire> wiresList) {
		
		List<TopoDS_Solid> generatedSolids = new ArrayList<>();
		
		BRepOffsetAPI_ThruSections shellMaker = new BRepOffsetAPI_ThruSections();
		shellMaker.Init(0, 0);		
		wiresList.forEach(shellMaker::AddWire);
		TopoDS_Shell shell = TopoDS.ToShell(shellMaker.Shape());
		
		TopoDS_Face face1 = new BRepBuilderAPI_MakeFace(wiresList.get(0)).Face();
		TopoDS_Face face2 = new BRepBuilderAPI_MakeFace(wiresList.get(wiresList.size()-1)).Face();
		
		BRepBuilderAPI_Sewing sewer = new BRepBuilderAPI_Sewing();
		sewer.Add(face1);
		sewer.Add(shell);
		sewer.Add(face2);
		sewer.Perform();
		TopoDS_Shape sewedShape = sewer.SewedShape();
		
		TopExp_Explorer exp = new TopExp_Explorer(sewedShape, TopAbs_ShapeEnum.TopAbs_SHELL);
		while(exp.More() > 0) {
			TopoDS_Shell sewedShell = TopoDS.ToShell(exp.Current());
			generatedSolids.add(new BRepBuilderAPI_MakeSolid(sewedShell).Solid());
			exp.Next();
		}		
		
		if(generatedSolids.size() == 1) 
			return generatedSolids.get(0);
		else {
			System.out.println("generateSolidFromWires: more than one solid or no solid has been created! Null result has been returned.");
			return null;
		}
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

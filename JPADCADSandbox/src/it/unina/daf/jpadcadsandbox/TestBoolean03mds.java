package it.unina.daf.jpadcadsandbox;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.SI;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BOPAlgo_Operation;
import opencascade.BRepAlgoAPI_Common;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepAlgoAPI_Section;
import opencascade.BRepBuilderAPI;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_MakeEvolved;
import opencascade.BRepOffsetAPI_MakePipe;
import opencascade.BRepOffsetAPI_MakePipeShell;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GC_MakePlane;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.Geom_Curve;
import opencascade.Geom_Surface;
import opencascade.TopAbs_Orientation;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_ListOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.TopoDS_Solid;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax2;
import opencascade.gp_Circ;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import opencascade.gp_XYZ;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class TestBoolean03mds {

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
				ComponentEnum.WING, 
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
		
		// Generate cutting solids
		double wingSemiSpan = wing.getSemiSpan().doubleValue(SI.METER);
		
		List<double[]> symFlapExtrema = new ArrayList<>();
		List<double[]> symFlapChords = new ArrayList<>();
		
		int numSymFlap = wing.getSymmetricFlaps().size();
		for(int i = 0; i < numSymFlap; i++) {
			SymmetricFlapCreator flap = wing.getSymmetricFlaps().get(i);
			symFlapExtrema.add(new double[] {
					flap.getInnerStationSpanwisePosition(),
					flap.getOuterStationSpanwisePosition()
			});
			symFlapChords.add(new double[] {
					flap.getInnerChordRatio(),
					flap.getOuterChordRatio()
			});
		}
		System.out.println("========== Symmetric flaps spanwise position:");
		symFlapExtrema.forEach(d -> System.out.println("========== " + Arrays.toString(d)));
		System.out.println("========== Symmetric flaps chords:");
		symFlapChords.forEach(d -> System.out.println("========== " + Arrays.toString(d)));
		
		// adjust flap stations
		List<double[]> symFlapActualExtrema = new ArrayList<>();
		List<double[]> symFlapActualChords = new ArrayList<>();
		List<Double> wingEtaBreakpoints = wing.getEtaBreakPoints();
		
		for(int i = 0; i < symFlapExtrema.size(); i++) {
			double yInner = symFlapExtrema.get(i)[0];
			double yOuter = symFlapExtrema.get(i)[1];
			double innChord = symFlapChords.get(i)[0];
			double outChord = symFlapChords.get(i)[1];
			
			for(int j = 0; j < wingEtaBreakpoints.size()-1; j++) {
				if(yInner >= wingEtaBreakpoints.get(j) && yOuter <= wingEtaBreakpoints.get(j+1)) {
					symFlapActualExtrema.add(new double[] {yInner, yOuter});
					symFlapActualChords.add(new double[] {innChord, outChord});
				} else if((yInner >= wingEtaBreakpoints.get(j) && yInner < wingEtaBreakpoints.get(j+1)) 
						&& yOuter > wingEtaBreakpoints.get(j+1)) {
					double etaBP = (double) Math.round(wingEtaBreakpoints.get(j+1)*100d)/100d;
					
					symFlapActualExtrema.add(new double[] {yInner, etaBP});
					symFlapActualExtrema.add(new double[] {etaBP, yOuter});
					
					double midChord = MyMathUtils.getInterpolatedValue1DLinear(
							symFlapExtrema.get(i), 
							symFlapChords.get(i), 
							etaBP
							);
					
					symFlapActualChords.add(new double[] {innChord, midChord});
					symFlapActualChords.add(new double[] {midChord, outChord});
				}
			}
		}	
		
		
		
		for(int i = 1; i < symFlapActualExtrema.size(); i++) {
			if((symFlapActualExtrema.get(i)[0] - symFlapActualExtrema.get(i-1)[1]) < 1e-5) {
				double[] temp = symFlapActualExtrema.get(i);
				symFlapActualExtrema.set(i, new double[] {(double) Math.round((temp[0]-0.01)*100d)/100d, temp[1]});
			}			
		}
		
//		symFlapActualExtrema.set(0, new double[] {0.08, 0.32}); // TODO it's better, whenever two different flap stations coincide,
//		symFlapActualExtrema.set(1, new double[] {0.32, 0.36}); //      to set the second one a little bit back. Besides, make sure
//		symFlapActualExtrema.set(2, new double[] {0.36, 0.80}); //      stations do not precisely coincide with eta breakpoints
		
		symFlapActualExtrema.clear();
		symFlapActualExtrema = symFlapExtrema;
		symFlapActualChords.clear();
		symFlapActualChords = symFlapChords;
		
//		symFlapActualExtrema.remove(0);
//		symFlapActualExtrema.set(0, new double[] {0.08, 0.30});
//		symFlapActualExtrema.set(1, new double[] {0.35, 0.80});
//		symFlapActualChords.remove(0);
//		symFlapActualChords.set(0, new double[] {0.35, 0.32});
//		symFlapActualChords.set(1, new double[] {0.35, 0.32});
		System.out.println("========== Actual symmetric flaps spanwise position:");
		symFlapActualExtrema.forEach(d -> System.out.println("========== " + Arrays.toString(d)));
		System.out.println("========== Actual symmetric flaps chords:");
		symFlapActualChords.forEach(d -> System.out.println("========== " + Arrays.toString(d)));
		
		int numSymFlapActual = symFlapActualExtrema.size();
		
		// calculate list of chord lengths
		List<double[]> chordLengths = new ArrayList<>();
		for(int i = 0; i < numSymFlapActual; i++) {
			double[] chordsAtY = new double[2];
			for(int j = 0; j < 2; j++) 
				chordsAtY[j] = wing.getChordAtYActual(symFlapActualExtrema.get(i)[j]*wingSemiSpan);
			chordLengths.add(chordsAtY);
		}
		
		// prepare sketching boolean maps
		List<boolean[]> airfoilBool = new ArrayList<>();
		List<boolean[]> cuttingBool = new ArrayList<>();
		int index = 0;
		while(index < numSymFlapActual) {
			airfoilBool.add(new boolean[] {true, true});
			cuttingBool.add(new boolean[] {true, true});
			index++;
		}
		for(int i = 1; i < numSymFlapActual; i++) {
			if(symFlapActualExtrema.get(i)[0] - symFlapActualExtrema.get(i-1)[1] < 1e-4) 
				airfoilBool.set(i, new boolean[] {false, true});
			if(symFlapActualChords.get(i)[0] - symFlapActualChords.get(i-1)[1] < 1e-4) 
				cuttingBool.set(i, new boolean[] {false, true});		
		}
		airfoilBool.forEach(b -> System.out.println(Arrays.toString(b)));
		cuttingBool.forEach(b -> System.out.println(Arrays.toString(b)));
		
		// get sections airfoils 
		List<TopoDS_Shape> secAirfoils = new ArrayList<>();
		List<TopoDS_Shape[]> wingSections = new ArrayList<>();
		
		gp_Dir sectionNormalAxis = new gp_Dir(0, 1, 0);
//		for(int i = 0; i < numSymFlapActual; i++) {
//			for(int j = 0; j < 2; j++) {
//				BRepAlgoAPI_Section sectionMaker = new BRepAlgoAPI_Section();
//				gp_Pnt sectionOrigin = new gp_Pnt(0, symFlapActualExtrema.get(i)[j]*wingSemiSpan, 0);
//				sectionMaker.Init1(wingSolid);
//				sectionMaker.Init2(makeIntersectionPlane(sectionOrigin, sectionNormalAxis));
//				sectionMaker.Build();
//				secAirfoils.add(sectionMaker.Shape());
//			}		
//		}
		
		for(int i = 0; i < numSymFlapActual; i++) {
			TopoDS_Shape[] shapesArray = new TopoDS_Shape[2];
			for(int j = 0; j < 2; j++) {
				if(j == 0 && !airfoilBool.get(i)[j])
					shapesArray[0] = wingSections.get(i-1)[1];
				else {
					BRepAlgoAPI_Section sectionMaker = new BRepAlgoAPI_Section();
					gp_Pnt sectionOrigin = new gp_Pnt(0, symFlapActualExtrema.get(i)[j]*wingSemiSpan, 0);
					sectionMaker.Init1(wingSolid);
					sectionMaker.Init2(makeIntersectionPlane(sectionOrigin, sectionNormalAxis));
					sectionMaker.Build();
					shapesArray[j] = sectionMaker.Shape();
				}
			}
			wingSections.add(shapesArray);
		}
		
		// manage sections in order to extract desired edges
//		List<TopoDS_Edge> sectionEdges = new ArrayList<>();
//		for(int i = 0; i < secAirfoils.size(); i++) {
//			TopExp_Explorer explorer = new TopExp_Explorer(secAirfoils.get(i), TopAbs_ShapeEnum.TopAbs_EDGE);
//			while(explorer.More() > 0) {
//				sectionEdges.add(TopoDS.ToEdge(explorer.Current()));
//				explorer.Next();
//			}
//		}
//		System.out.println("========== Number of TopoDS_Edges found exploring sections compounds: " + sectionEdges.size());
//		
//		List<TopoDS_Edge> airfoilEdges = new ArrayList<>();
//		List<TopoDS_Edge> teEdges = new ArrayList<>();
//		double meanLength = sectionEdges.stream()
//									    .mapToDouble(TestBoolean03mds::calculateEdgeLength)
//				                        .sum()/(sectionEdges.size());
//		for(int i = 0; i < sectionEdges.size(); i++) {
//			TopoDS_Edge edge = sectionEdges.get(i);
//			if(calculateEdgeLength(edge) < meanLength) 
//				teEdges.add(edge);
//			else
//				airfoilEdges.add(edge);
//		}
//		System.out.println("========== Number of TopoDS_Edges in airfoilEdges list: " + airfoilEdges.size());
//		System.out.println("========== Number of TopoDS_Edges in teEdges list: " + teEdges.size());
		
		List<TopoDS_Edge[]> mainAirfoils = new ArrayList<>();
		List<TopoDS_Edge[]> trailingEdges = new ArrayList<>();
		for(int i = 0; i < numSymFlapActual; i++) {
			TopoDS_Edge[] airfs = new TopoDS_Edge[2];
			TopoDS_Edge[] tEdgs = new TopoDS_Edge[2];
			for(int j = 0; j < 2; j++) {
				if(j == 0 && !airfoilBool.get(i)[j]) {
					airfs[0] = mainAirfoils.get(i-1)[1];
					tEdgs[0] = trailingEdges.get(i-1)[1];
				} else {
					TopExp_Explorer explorer = new TopExp_Explorer(wingSections.get(i)[j], TopAbs_ShapeEnum.TopAbs_EDGE);
					while(explorer.More() > 0) {
						TopoDS_Edge edge = TopoDS.ToEdge(explorer.Current());
						if(calculateEdgeLength(edge) > chordLengths.get(i)[0]) 
							airfs[j] = edge.Orientation().equals(TopAbs_Orientation.TopAbs_REVERSED) ? // reversed respect section plane orientation (?)
									getReversedEdge(edge) : edge;
						else
							tEdgs[j] = edge;
						explorer.Next();
					}
				}			
			}
			mainAirfoils.add(airfs);
			trailingEdges.add(tEdgs);
		}	
		
		// generate airfoil chords		
//		List<OCCEdge> chords = new ArrayList<>();
//		symFlapActualExtrema.forEach(d -> {
//			chords.add((OCCEdge) getChordSegmentAtYActual(d[0]*wingSemiSpan, wing).edge());
//			chords.add((OCCEdge) getChordSegmentAtYActual(d[1]*wingSemiSpan, wing).edge());
//		});
		
		List<OCCEdge[]> chordEdges = new ArrayList<>();
		for(int i = 0; i < numSymFlapActual; i++) {
			OCCEdge[] chordsArray = new OCCEdge[2];
			for(int j = 0; j < 2; j++) {
				if(j == 0 && !airfoilBool.get(i)[j])
					chordsArray[0] = chordEdges.get(i-1)[1];
				else {
					chordsArray[j] = (OCCEdge) getChordSegmentAtYActual(
							symFlapActualExtrema.get(i)[j]*wingSemiSpan, 
							wing
							).edge();
				}
			}
			chordEdges.add(chordsArray);
		}
		
		// generate wires at each station
//		double leapFactor = 0.70;
//		List<TopoDS_Wire> cuttingWires = new ArrayList<>();
//		
//		for(int i = 1; i <= numSymFlapActual; i++) {
//			for(int j = 1; j <= 2; j++) {
//				
//				double c = wing.getChordAtYActual(symFlapActualExtrema.get(i-1)[j-1]*wingSemiSpan);
//				double cf = symFlapActualChords.get(i-1)[j-1]*c;
//				double cl = cf*leapFactor;
//				double[] te = chords.get(i*j-1).vertices()[0].pnt();
//				
//				Geom_Curve airfoil = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//						(CADEdge) OCCUtils.theFactory.newShape(airfoilEdges.get(i*j-1)))).getAdaptorCurve().Curve();
//				Geom_Curve chord = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//						chords.get(i*j-1))).getAdaptorCurve().Curve();
//				
//				double[] cfPar = getIntersecPntsOnAirfoilParam(airfoil, chord, c, cf, te, wing.getType(), SideSelector.LOWER_SIDE);
//				double[] clPar = getIntersecPntsOnAirfoilParam(airfoil, chord, c, cl, te, wing.getType(), SideSelector.UPPER_SIDE);	
//				
//				System.out.println("========== cfPar: " + Arrays.toString(cfPar));
//				System.out.println("========== clPar: " + Arrays.toString(clPar));
//				
//				gp_Vec cfTang = new gp_Vec();
//				gp_Vec clTang = new gp_Vec();
//				gp_Pnt cfPnt = new gp_Pnt();
//				gp_Pnt clPnt = new gp_Pnt();
//				airfoil.D1(cfPar[0], cfPnt, cfTang);
//				airfoil.D1(clPar[0], clPnt, clTang);
//				
//				// generate wire points
//				List<gp_Pnt> wirePoints = new ArrayList<>();
//				
//				gp_Vec zyAxis = wing.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Vec(0, 0, 1) : new gp_Vec(0, 1, 0);
//				gp_Vec yzAxis = wing.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Vec(0, 1, 0) : new gp_Vec(0, 0, 1);
//				gp_Vec normUpp = clTang.Crossed(zyAxis).Normalized();
//				gp_Vec normLow = cfTang.Crossed(zyAxis).Normalized();
//				
//				gp_Pnt p1 = clPnt;	
//				gp_Pnt p6 = cfPnt;
//				gp_Pnt p2 = new gp_Pnt(normUpp.Multiplied(cf).Added(new gp_Vec(p1.Coord())).XYZ());				
//				gp_Pnt p3 = new gp_Pnt(new gp_Vec(1, 0, 0).Multiplied(2*cf).Added(new gp_Vec(p2.Coord())).XYZ());
//				gp_Pnt p4 = new gp_Pnt(yzAxis.Multiplied(-2*cf).Added(new gp_Vec(p3.Coord())).XYZ());
//				gp_Pnt p5 = new gp_Pnt(normLow.Multiplied(cf).Added(new gp_Vec(p6.Coord())).XYZ()); 
//				p5.SetZ(p4.Z());
//				
//				wirePoints.add(p1);
//				wirePoints.add(p2);
//				wirePoints.add(p3);
//				wirePoints.add(p4);
//				wirePoints.add(p5);
//				wirePoints.add(p6);
//				
//				// generate wire edges
//				BRepBuilderAPI_MakeWire wire = new BRepBuilderAPI_MakeWire();
//				for(int k = 0; k < 5; k++) {
//					BRepBuilderAPI_MakeEdge wireEdge = new BRepBuilderAPI_MakeEdge(
//							wirePoints.get(k), 
//							wirePoints.get(k+1)
//							);
//					wire.Add(wireEdge.Edge());
//				}
//				List<double[]> flapCurvePoints = new ArrayList<>();
//				flapCurvePoints.add(convertGpPntToDoubleArray(p6));
//				flapCurvePoints.add(convertGpPntToDoubleArray(p1));
//				wire.Add(new BRepBuilderAPI_MakeEdge(((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//						flapCurvePoints, 
//						false, 
//						new double[] {yzAxis.X(), yzAxis.Y(), yzAxis.Z()}, 
//						new double[] {1, 0, 0}, 
//						false
//						)).getAdaptorCurve().Curve()).Edge());
//				
//				cuttingWires.add(wire.Wire());
//			}
//		}
		
		double leapFactor = 0.75;
		List<TopoDS_Wire[]> cuttingWires = new ArrayList<>();
		
		for(int i = 0; i < numSymFlapActual; i++) {
			TopoDS_Wire[] wiresArray = new TopoDS_Wire[2];	
			for(int j = 0; j < 2; j++) {
				if(j == 0 && (!airfoilBool.get(i)[j] && !cuttingBool.get(i)[j])) {
					wiresArray[0] = cuttingWires.get(i-1)[1];
				} else {
					double c = chordLengths.get(i)[j];
					double cf = symFlapActualChords.get(i)[j]*c;
					double cl = cf*leapFactor;
					double[] te = chordEdges.get(i)[j].vertices()[0].pnt();
					
					Geom_Curve airfoil = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							(CADEdge) OCCUtils.theFactory.newShape(mainAirfoils.get(i)[j]))).getAdaptorCurve().Curve();
					Geom_Curve chord = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							chordEdges.get(i)[j])).getAdaptorCurve().Curve();

					double[] cfPar = getIntersecPntsOnAirfoilParam(airfoil, chord, c, cf, te, wing.getType(), SideSelector.LOWER_SIDE);
					double[] clPar = getIntersecPntsOnAirfoilParam(airfoil, chord, c, cl, te, wing.getType(), SideSelector.UPPER_SIDE);	

					System.out.println("========== cfPar: " + Arrays.toString(cfPar));
					System.out.println("========== clPar: " + Arrays.toString(clPar));
					
					gp_Vec cfTang = new gp_Vec();
					gp_Vec clTang = new gp_Vec();
					gp_Pnt cfPnt = new gp_Pnt();
					gp_Pnt clPnt = new gp_Pnt();
					airfoil.D1(cfPar[0], cfPnt, cfTang);
					airfoil.D1(clPar[0], clPnt, clTang);
					
					// generate wire points
					List<gp_Pnt> wirePoints = new ArrayList<>();
					
					gp_Vec zyAxis = wing.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Vec(0, 0, 1) : new gp_Vec(0, 1, 0);
					gp_Vec yzAxis = wing.getType().equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Vec(0, 1, 0) : new gp_Vec(0, 0, 1);
					gp_Vec normUpp = clTang.Crossed(zyAxis).Normalized();
					gp_Vec normLow = cfTang.Crossed(zyAxis).Normalized();
					
					gp_Pnt p1 = clPnt;	
					gp_Pnt p6 = cfPnt;
					gp_Pnt p2 = new gp_Pnt(normUpp.Multiplied(cf).Added(new gp_Vec(p1.Coord())).XYZ());				
					gp_Pnt p3 = new gp_Pnt(new gp_Vec(1, 0, 0).Multiplied(2*cf).Added(new gp_Vec(p2.Coord())).XYZ());
					gp_Pnt p4 = new gp_Pnt(yzAxis.Multiplied(-2*cf).Added(new gp_Vec(p3.Coord())).XYZ());
					gp_Pnt p5 = new gp_Pnt(normLow.Multiplied(cf).Added(new gp_Vec(p6.Coord())).XYZ()); 
					p5.SetZ(p4.Z());
					
					wirePoints.add(p1);
					wirePoints.add(p2);
					wirePoints.add(p3);
					wirePoints.add(p4);
					wirePoints.add(p5);
					wirePoints.add(p6);
					
					// generate wire edges
					BRepBuilderAPI_MakeWire wire = new BRepBuilderAPI_MakeWire();
					for(int k = 0; k < 5; k++) {
						BRepBuilderAPI_MakeEdge wireEdge = new BRepBuilderAPI_MakeEdge(
								wirePoints.get(k), 
								wirePoints.get(k+1)
								);
						wire.Add(wireEdge.Edge());
					}
					List<double[]> flapCurvePoints = new ArrayList<>();
					flapCurvePoints.add(convertGpPntToDoubleArray(p6));
					flapCurvePoints.add(convertGpPntToDoubleArray(p1));
					wire.Add(new BRepBuilderAPI_MakeEdge(((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							flapCurvePoints, 
							false, 
							new double[] {yzAxis.X(), yzAxis.Y(), yzAxis.Z()}, 
							new double[] {1, 0, 0}, 
							false
							)).getAdaptorCurve().Curve()).Edge());
					
					wiresArray[j] = wire.Wire();
				}
			}
			cuttingWires.add(wiresArray);
		}
		
		// patching through wires
		List<TopoDS_Shell> cuttingShells = new ArrayList<>();
		List<TopoDS_Solid> cuttingSolids = new ArrayList<>();
		for(int i = 0; i < cuttingWires.size(); i++) {
			TopoDS_Wire wire1 = cuttingWires.get(i)[0];
			TopoDS_Wire wire2 = cuttingWires.get(i)[1];
			
			BRepOffsetAPI_ThruSections shellMaker = new BRepOffsetAPI_ThruSections();
			shellMaker.Init(0, 0);
			shellMaker.AddWire(wire1);
			shellMaker.AddWire(wire2);
			TopoDS_Shell shell = TopoDS.ToShell(shellMaker.Shape());
			
			TopoDS_Face face1 = new BRepBuilderAPI_MakeFace(wire1).Face();
			TopoDS_Face face2 = new BRepBuilderAPI_MakeFace(wire2).Face();
			
			BRepBuilderAPI_Sewing sewer = new BRepBuilderAPI_Sewing();
			sewer.Add(face1);
			sewer.Add(shell);
			sewer.Add(face2);
			sewer.Perform();
			TopoDS_Shape sewedShape = sewer.SewedShape();
			
			System.out.println(OCCUtils.reportOnShape(sewedShape, "Shapes report on cutting shell sewed shape: "));
			TopExp_Explorer exp = new TopExp_Explorer(sewedShape, TopAbs_ShapeEnum.TopAbs_SHELL);
			while(exp.More() > 0) {
				TopoDS_Shell sewedShell = TopoDS.ToShell(exp.Current());
				cuttingShells.add(sewedShell);
				cuttingSolids.add(new BRepBuilderAPI_MakeSolid(sewedShell).Solid());
				exp.Next();
			}			
		}	
		
		// mirroring cutting solids when necessary
		if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			List<TopoDS_Solid> mirroredCS = new ArrayList<>();
			gp_Trsf mirrorTransform = new gp_Trsf();
			gp_Ax2 mirrorPointPlane = new gp_Ax2(
					new gp_Pnt(0.0, 0.0, 0.0),
					new gp_Dir(0.0, 1.0, 0.0), // Y direction normal to reflection plane XZ
					new gp_Dir(1.0, 0.0, 0.0)
					);
			mirrorTransform.SetMirror(mirrorPointPlane);
			BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
			for(int i = 0; i < cuttingSolids.size(); i++) {
				TopoDS_Solid cs = cuttingSolids.get(i);
				mirrorBuilder.Perform(cs, 1);
				mirroredCS.add(TopoDS.ToSolid(mirrorBuilder.Shape()));
			}
			cuttingSolids.addAll(mirroredCS);
		}
		
		// cut the wing
//		TopoDS_Solid cutWing = wingSolid;
//		for(int i = 0; i < cuttingSolids.size(); i++) {
//			TopoDS_Shape cutShape = new BRepAlgoAPI_Cut(cutWing, cuttingSolids.get(i)).Shape();
//			TopExp_Explorer exp = new TopExp_Explorer(cutShape, TopAbs_ShapeEnum.TopAbs_SOLID);
//			while(exp.More() > 0) {
//				cutWing = TopoDS.ToSolid(exp.Current());
//				exp.Next();
//			}
//		}
		
//		TopoDS_Compound cuttingSolidsCompound = new TopoDS_Compound();
//		BRep_Builder builder = new BRep_Builder();
//		builder.MakeCompound(cuttingSolidsCompound);
//		cuttingSolids.forEach(cs -> builder.Add(cuttingSolidsCompound, cs));
		
//		BRepAlgoAPI_Common cutter = new BRepAlgoAPI_Common();
//		TopTools_ListOfShape arguments = new TopTools_ListOfShape();
//		TopTools_ListOfShape tools = new TopTools_ListOfShape();
//		
//		arguments.Append(wingSolid);
//		cuttingSolids.forEach(tools::Append);
//		
//		cutter.SetOperation(BOPAlgo_Operation.BOPAlgo_CUT);
//		cutter.SetArguments(arguments);
//		cutter.SetTools(tools);
//		cutter.Build();
//		
//		TopoDS_Shape resultShape = cutter.Shape();
//		System.out.println(OCCUtils.reportOnShape(resultShape, "Shapes report on cut solid: "));
//		TopExp_Explorer exp0 = new TopExp_Explorer(resultShape, TopAbs_ShapeEnum.TopAbs_COMPOUND);
//		List<TopoDS_Solid> cutSolids = new ArrayList<>();
//		while(exp0.More() > 0) {
//			TopoDS_Compound comp = TopoDS.ToCompound(exp0.Current());
//			System.out.println(OCCUtils.reportOnShape(comp, "Shapes report on compound found in cut shapes: "));
//			TopExp_Explorer exp1 = new TopExp_Explorer(comp, TopAbs_ShapeEnum.TopAbs_SOLID);
//			while(exp1.More() > 0) {
//				cutSolids.add(TopoDS.ToSolid(exp1.Current()));
//				exp1.Next();
//			}
//			exp0.Next();
//		}
//		System.out.println(cutSolids.size());
		
		TopoDS_Solid cutResult = TopoDS.ToSolid(wingSolid);
		for(int i = 0; i < cuttingSolids.size(); i++) {
			BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(cuttingSolids.get(i), cutResult); //TODO why? cut algorithm seems to work fine in this case 
			TopoDS_Shape cut = cutter.Shape();                                             //     putting tools first and complementing cut result
			TopExp_Explorer exp = new TopExp_Explorer(cut, TopAbs_ShapeEnum.TopAbs_SOLID);
			while(exp.More() > 0) {
				cutResult = TopoDS.ToSolid(exp.Current().Complemented());
				System.out.println("Cut solid orientation: " + cutResult.Orientation().toString());
				exp.Next();
			}
		}
	
//		// generate circles for curve cutting
//		List<TopoDS_Edge> cfCircles = new ArrayList<>();
//		List<TopoDS_Edge> clCircles = new ArrayList<>();
//		double leapFactor = 0.60;
//		
//		gp_Dir circleAxis = new gp_Dir(0, 1, 0);
//		for(int i = 1; i <= symFlapExtrema.size(); i++) {
//			for(int j = 1; j <= 2; j++) {
//				double c = wing.getChordAtYActual(symFlapExtrema.get(i-1)[j-1]*wingSemiSpan);
//				double cf = symFlapChords.get(i-1)[j-1]*c;
//				double cl = cf*leapFactor;
//				double[] origin = chords.get(i*j-1).vertices()[0].pnt();
//				gp_Circ cfCircle = new gp_Circ(new gp_Ax2(new gp_Pnt(origin[0], origin[1], origin[2]), circleAxis), cf);
//				gp_Circ clCircle = new gp_Circ(new gp_Ax2(new gp_Pnt(origin[0], origin[1], origin[2]), circleAxis), cl);
//				cfCircles.add(new BRepBuilderAPI_MakeEdge(cfCircle).Edge());
//				clCircles.add(new BRepBuilderAPI_MakeEdge(clCircle).Edge());
//			}			
//		}
//		
//		// calculate chord-circles and segment-airfoil intersections
//		List<gp_Pnt> chordIntersec = new ArrayList<>();
//		List<gp_Pnt> airfoilIntersec = new ArrayList<>();
//		List<CADGeomCurve3D> intersecSegments = new ArrayList<>();
//		
//		for(int i = 0; i < chords.size(); i++) {	
//			Geom_Curve airfoil = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//					(CADEdge) OCCUtils.theFactory.newShape(airfoilEdges.get(i)))).getAdaptorCurve().Curve();
//			Geom_Curve circle = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//					(CADEdge) OCCUtils.theFactory.newShape(cfCircles.get(i)))).getAdaptorCurve().Curve();
//			Geom_Curve chord = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
//					chords.get(i))).getAdaptorCurve().Curve();
//			
//			gp_Vec yDir = new gp_Vec(0, 1, 0);
//			
//			GeomAPI_ExtremaCurveCurve extrema1 = new GeomAPI_ExtremaCurveCurve(chord, circle);
//			int nExtrema1 = extrema1.NbExtrema();
//			System.out.println("========== Number of extrema found for check N°" + (i+1) + ": " + nExtrema1);
//			
//			if(nExtrema1 > 0) {
//				gp_Pnt pointA = new gp_Pnt();
//				gp_Pnt pointB = new gp_Pnt();
//				double[] chordPar = new double[] {0};
//				double[] circlePar = new double[] {0};
//				for(int j = 1; j <= nExtrema1; j++) {
//					extrema1.Points(j, pointA, pointB);
//					if(pointA.IsEqual(pointB, 1e-5) == 1) {
//						System.out.println("========== Intersection found! Point coordinates: ");
//						System.out.println("========== " + Arrays.toString(
//								convertGpPntToDoubleArray(pointA)));
//						chordIntersec.add(pointA);
//						extrema1.Parameters(j, chordPar, circlePar);
//						System.out.println("========== Chord segment parameter at intersection: " + Arrays.toString(chordPar));
//						System.out.println("========== Check on parameter validity:"); 
//						System.out.println("========== " + Arrays.toString(
//								convertGpPntToDoubleArray(chord.Value(chordPar[0]))));
//						gp_Vec chordDir = new gp_Vec();
//						chord.D1(chordPar[0], new gp_Pnt(), chordDir);
//						System.out.println("========== Chord versor coordinates:");
//						System.out.println("========== " + Arrays.toString(new double[] {
//								chordDir.X(),
//								chordDir.Y(),
//								chordDir.Z()
//						}));
//						gp_Vec chordNormalU = yDir.Crossed(chordDir).Multiplied(chordsL.get(i));
//						gp_Vec chordNormalD = chordDir.Crossed(yDir).Multiplied(chordsL.get(i));
//						
//						gp_Pnt pointC = new gp_Pnt(chordNormalD.Added(new gp_Vec(pointA.Coord())).XYZ());
//						CADGeomCurve3D intersecSegment = OCCUtils.theFactory.newCurve3D(
//								convertGpPntToDoubleArray(pointA), 
//								convertGpPntToDoubleArray(pointC)
//								);
//						intersecSegments.add(intersecSegment);
//						
//						GeomAPI_ExtremaCurveCurve extrema2 = new GeomAPI_ExtremaCurveCurve(
//								airfoil, 
//								((OCCGeomCurve3D) intersecSegment).getAdaptorCurve().Curve());	
//						int nExtrema2 = extrema2.NbExtrema();
//						System.out.println("========== Number of extrema found for check N°" + (i+1) + " (airfoil case): " + nExtrema1);
//						
//						if(nExtrema2 > 0) {
//							gp_Pnt pointD = new gp_Pnt();
//							gp_Pnt pointE = new gp_Pnt();
//							double[] airfoilPar = new double[] {0};
//							double[] segmentPar = new double[] {0};
//							for(int k = 1; k <= nExtrema2; k++) {
//								extrema2.Points(k, pointD, pointE);
//								if(pointD.IsEqual(pointE, 1e-5) == 1) {
//									System.out.println("========== Intersection found (airfoil case)! Point coordinates: ");
//									System.out.println("========== " + Arrays.toString(
//											convertGpPntToDoubleArray(pointD)));
//									airfoilIntersec.add(pointD);
//									extrema2.Parameters(k, airfoilPar, segmentPar);
//									System.out.println("========== Airfoil curve parameter at intersection: " + Arrays.toString(airfoilPar));
//									System.out.println("========== Check on parameter validity:"); 
//									System.out.println("========== " + Arrays.toString(
//											convertGpPntToDoubleArray(airfoil.Value(airfoilPar[0]))));
//								}
//							}						
//						}
//					}
//				}	
//			}		
//		}	
//		List<CADGeomCurve3D> testCADCurves = new ArrayList<>();
//		for(int i = 0; i < airfoilIntersec.size() - 1; i++) {
//			testCADCurves.add(OCCUtils.theFactory.newCurve3D(
//					convertGpPntToDoubleArray(airfoilIntersec.get(i)), 
//					convertGpPntToDoubleArray(airfoilIntersec.get(i+1))
//					));
//		}
								
		// Export shapes to CAD file
		List<OCCShape> exportShapes = new ArrayList<>();

//		chords.forEach(c -> exportShapes.add((OCCShape) c));
//		cfCircles.forEach(c -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(c)));
//		clCircles.forEach(c -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(c)));
//		airfoilEdges.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));	
//		teEdges.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));
//		intersecSegments.forEach(s -> exportShapes.add((OCCShape) ((OCCEdge) s.edge())));
//		testCADCurves.forEach(c -> exportShapes.add((OCCShape) ((OCCEdge) c.edge())));
//		cuttingWires.forEach(w -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(w)));
//		wingShapes.forEach(s -> exportShapes.add(s));
//		wingSections.forEach(sa -> {
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[0]));
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[1]));
//			});
//		mainAirfoils.forEach(sa -> {
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[0]));
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[1]));
//			});
//		trailingEdges.forEach(sa -> {
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[0]));
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[1]));
//			});
//		chordEdges.forEach(sa -> {
//			exportShapes.add(sa[0]);
//			exportShapes.add(sa[1]);
//			});
//		cuttingWires.forEach(sa -> {
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[0]));
//			exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(sa[1]));
//			});
//		cuttingSolids.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));
		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutResult));
//		cutSolids.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));
			
		String fileName = "testBoolean03mds.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}
	
	public static TopoDS_Edge getReversedEdge(TopoDS_Edge edge) {
		Geom_Curve curve = BRep_Tool.Curve(edge, new double[1], new double[1]);	
		curve.Reverse();
		return new BRepBuilderAPI_MakeEdge(curve).Edge();
	}

	public static Geom_Surface makeIntersectionPlane(gp_Pnt planeOrigin, gp_Dir planeNormalAxis) {
		GC_MakePlane plane = new GC_MakePlane(planeOrigin, planeNormalAxis);
		return plane.Value();
	}
	
	public static double[] convertGpPntToDoubleArray(gp_Pnt gpPnt) {
		return new double[] {gpPnt.X(), gpPnt.Y(), gpPnt.Z()};
	}
	
	public static double[] getIntersecPntsOnAirfoilParam(
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
		Geom_Curve circle = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
				(CADEdge) OCCUtils.theFactory.newShape(
						new BRepBuilderAPI_MakeEdge(
								new gp_Circ(
										new gp_Ax2(
												new gp_Pnt(
														teCoords[0],
														teCoords[1],
														teCoords[2]),
												lsAxis),
										flapChord)).Edge())
				)).getAdaptorCurve().Curve();
		
		double[] chorPar = getIntersecPntsParam(chord, circle);
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
				
				double[] airfPar = getIntersecPntsParam(airfoil, interceptor);
				if(airfPar != null) {
					params[i] = airfPar[0];
				}
			}
		} 
		return params;
	}
	
	public static double[] getIntersecPntsOnAirfoilParams(
			Geom_Curve airfoil, 
			Geom_Curve chord,
			double chordLength,
			double flapChord, 		
			double[] teCoords, 
			ComponentEnum lsType
			) {
		
		double[] params = new double[2]; 
		gp_Dir lsAxis = lsType.equals(ComponentEnum.VERTICAL_TAIL) ? new gp_Dir(0, 0, 1) : new gp_Dir(0, 1, 0);		
		Geom_Curve circle = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
				(CADEdge) OCCUtils.theFactory.newShape(
						new BRepBuilderAPI_MakeEdge(
								new gp_Circ(
										new gp_Ax2(
												new gp_Pnt(
														teCoords[0],
														teCoords[1],
														teCoords[2]),
												lsAxis),
										flapChord)).Edge())
				)).getAdaptorCurve().Curve();
		
		double[] chorPar = getIntersecPntsParam(chord, circle);
		if(chorPar != null) {
			gp_Vec chorDir = new gp_Vec();
			chord.D1(chorPar[0], new gp_Pnt(), chorDir);
			
			List<gp_Vec> normals = new ArrayList<>();
			gp_Vec chorNormalUpp = new gp_Vec(lsAxis).Crossed(chorDir).Multiplied(chordLength);
			gp_Vec chorNormalLow = chorDir.Crossed(new gp_Vec(lsAxis)).Multiplied(chordLength);
			normals.add(chorNormalUpp);
			normals.add(chorNormalLow);
			
			for(int i = 0; i < 2; i++) {
				Geom_Curve interceptor = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
						convertGpPntToDoubleArray(chord.Value(chorPar[0])), 
						convertGpPntToDoubleArray(new gp_Pnt(normals.get(i).Added(new gp_Vec(chord.Value(chorPar[0]).Coord())).XYZ()))
						)).getAdaptorCurve().Curve();
				
				double[] airfPar = getIntersecPntsParam(airfoil, interceptor);
				if(airfPar != null) {
					params[i] = airfPar[0];
				}
			}
		}
		return params;
	}
	
	public static double[] getIntersecPntsParam(Geom_Curve curve1, Geom_Curve curve2) {
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
	
	public static double calculateEdgeLength(TopoDS_Edge edge) {
			
		GProp_GProps props = new GProp_GProps();
		BRepGProp.LinearProperties(edge, props);	
		return props.Mass();
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
	
	public enum SideSelector {
		UPPER_SIDE,
		LOWER_SIDE,
		BOTH_SIDES
	}
}

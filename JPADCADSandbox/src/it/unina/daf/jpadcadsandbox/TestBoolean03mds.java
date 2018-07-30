package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

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
import javaslang.Tuple2;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Section;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepExtrema_DistShapeShape;
import opencascade.BRepFilletAPI_MakeChamfer;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.BRepGProp;
import opencascade.BRepOffsetAPI_ThruSections;
import opencascade.BRep_Tool;
import opencascade.ChFi3d_FilletShape;
import opencascade.GC_MakePlane;
import opencascade.GProp_GProps;
import opencascade.GeomAPI_ExtremaCurveCurve;
import opencascade.Geom_Curve;
import opencascade.Geom_Surface;
import opencascade.Precision;
import opencascade.TColgp_Array1OfPnt2d;
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
		
		System.out.println(Arrays.toString(wingEtaBreakpoints.toArray(new Double[wingEtaBreakpoints.size()])));
		
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
		List<TopoDS_Shape[]> wingSections = new ArrayList<>();
		
		gp_Dir sectionNormalAxis = new gp_Dir(0, 1, 0);
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
		double leapFactor = 0.75;
		List<TopoDS_Wire[]> cuttingWires = new ArrayList<>();
		List<TopoDS_Wire[]> cuttingWiresF = new ArrayList<>();
		List<gp_Pnt[]> flapRotationPnts = new ArrayList<>();
		List<double[]> flapChords = new ArrayList<>();
		
		for(int i = 0; i < numSymFlapActual; i++) {
			TopoDS_Wire[] wiresArray = new TopoDS_Wire[2];	
			TopoDS_Wire[] wiresArrayF = new TopoDS_Wire[2];
			gp_Pnt[] rotPntsArray = new gp_Pnt[2];
			double[] cfArray = new double[2];
			for(int j = 0; j < 2; j++) {
				if(j == 0 && (!airfoilBool.get(i)[j] && !cuttingBool.get(i)[j])) {
					wiresArray[0] = cuttingWires.get(i-1)[1];
					rotPntsArray[0] = flapRotationPnts.get(i-1)[1];
					cfArray[0] = flapChords.get(i-1)[1];
				} else {
					double c = chordLengths.get(i)[j];
					double cf = symFlapActualChords.get(i)[j]*c;
					double cl = cf*leapFactor;
					double[] te = chordEdges.get(i)[j].vertices()[0].pnt();
					
					cfArray[j] = cf;
					
					Geom_Curve airfoil = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							(CADEdge) OCCUtils.theFactory.newShape(mainAirfoils.get(i)[j]))).getAdaptorCurve().Curve();
					Geom_Curve chord = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							chordEdges.get(i)[j])).getAdaptorCurve().Curve();

					double[] cfPar = getParamsIntersectionPntsOnAirfoil(airfoil, chord, c, cf, te, wing.getType(), SideSelector.LOWER_SIDE);
					double[] clPar = getParamsIntersectionPntsOnAirfoil(airfoil, chord, c, cl, te, wing.getType(), SideSelector.UPPER_SIDE);	

					System.out.println("========== cfPar: " + Arrays.toString(cfPar));
					System.out.println("========== clPar: " + Arrays.toString(clPar));
					
					gp_Vec cfTang = new gp_Vec();
					gp_Vec clTang = new gp_Vec();
					gp_Pnt cfPnt = new gp_Pnt();
					gp_Pnt clPnt = new gp_Pnt();
					airfoil.D1(cfPar[0], cfPnt, cfTang);
					airfoil.D1(clPar[0], clPnt, clTang);
					cfTang.Normalize();
					clTang.Normalize();
					
					rotPntsArray[j] = cfPnt;
					
					// generate wire points (wing)
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
					
					// generate wire edges (wing)
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
					clTang.Normalize();
					Geom_Curve flapCurve = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							flapCurvePoints, 
							false, 
							new double[] {yzAxis.X(), yzAxis.Y(), yzAxis.Z()}, 
							new double[] {clTang.X(), clTang.Y(), clTang.Z()},
							false
							)).getAdaptorCurve().Curve();
					wire.Add(new BRepBuilderAPI_MakeEdge(flapCurve).Edge());			
					wiresArray[j] = wire.Wire();
					
					// generate wire points (flap)	
					List<gp_Pnt> wirePointsF = new ArrayList<>();
					
					double pntOnFlapCurveParam = (flapCurve.LastParameter() - flapCurve.FirstParameter())*0.65;
					gp_Pnt pntOnFlapCurve = new gp_Pnt();
					gp_Vec tangOnFlapCurve = new gp_Vec();
					flapCurve.D1(pntOnFlapCurveParam, pntOnFlapCurve, tangOnFlapCurve);
					
					double pntOnAirfoilParam = getParamsIntersectionPntsOnAirfoil(
							airfoil, chord, c, cf*0.85, te, wing.getType(), SideSelector.LOWER_SIDE)[0];
					gp_Pnt pntOnAirfoil = new gp_Pnt();
					gp_Vec tangOnAirfoil = new gp_Vec();
					airfoil.D1(pntOnAirfoilParam, pntOnAirfoil, tangOnAirfoil);
					
					gp_Vec normUppF = tangOnFlapCurve.Crossed(zyAxis).Normalized();
					gp_Vec normLowF = tangOnAirfoil.Crossed(zyAxis).Normalized();
					
					gp_Pnt p1F = pntOnFlapCurve;
					gp_Pnt p6F = pntOnAirfoil;
					gp_Pnt p2F = new gp_Pnt(normUppF.Multiplied(cf).Added(new gp_Vec(p1F.Coord())).XYZ());	
					gp_Pnt p3F = new gp_Pnt(new gp_Vec(1, 0, 0).Multiplied(-cf).Added(new gp_Vec(p2F.Coord())).XYZ());
					gp_Pnt p4F = new gp_Pnt(yzAxis.Multiplied(-2*cf).Added(new gp_Vec(p3F.Coord())).XYZ());
					gp_Pnt p5F = new gp_Pnt(normLowF.Multiplied(cf).Added(new gp_Vec(p6F.Coord())).XYZ());
					p5F.SetZ(p4F.Z());
					
					wirePointsF.add(p1F);
					wirePointsF.add(p2F);
					wirePointsF.add(p3F);
					wirePointsF.add(p4F);
					wirePointsF.add(p5F);
					wirePointsF.add(p6F);
					
					// generate wire edges (flap)
					BRepBuilderAPI_MakeWire wireF = new BRepBuilderAPI_MakeWire();
					for(int k = 0; k < 5; k++) {
						BRepBuilderAPI_MakeEdge wireEdgeF = new BRepBuilderAPI_MakeEdge(
								wirePointsF.get(k), 
								wirePointsF.get(k+1)
								);
						wireF.Add(wireEdgeF.Edge());
					}
					List<double[]> flapCurvePointsF = new ArrayList<>();
					flapCurvePointsF.add(convertGpPntToDoubleArray(p6F));
					flapCurvePointsF.add(convertGpPntToDoubleArray(p1F));
					tangOnFlapCurve.Normalize();
					tangOnAirfoil.Normalize();
					Geom_Curve flapCurveF = ((OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(
							flapCurvePointsF, 
							false, 
							new double[] {tangOnAirfoil.X(), tangOnAirfoil.Y(), tangOnAirfoil.Z()}, 
							new double[] {tangOnFlapCurve.X(), tangOnFlapCurve.Y(), tangOnFlapCurve.Z()},
							false
							)).getAdaptorCurve().Curve();
					wireF.Add(new BRepBuilderAPI_MakeEdge(flapCurveF).Edge());
					wiresArrayF[j] = wireF.Wire();
				}
			}
			cuttingWires.add(wiresArray);
			cuttingWiresF.add(wiresArrayF);
			flapRotationPnts.add(rotPntsArray);
			flapChords.add(cfArray);
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
			
			TopExp_Explorer exp = new TopExp_Explorer(sewedShape, TopAbs_ShapeEnum.TopAbs_SHELL);
			while(exp.More() > 0) {
				TopoDS_Shell sewedShell = TopoDS.ToShell(exp.Current());
				cuttingShells.add(sewedShell);
				cuttingSolids.add(new BRepBuilderAPI_MakeSolid(sewedShell).Solid());
				exp.Next();
			}			
		}	
		
		// patching through wires (flap)
		List<TopoDS_Shell> cuttingShellsF = new ArrayList<>();
		List<TopoDS_Solid> cuttingSolidsF = new ArrayList<>();		
		for(int i = 0; i < cuttingWiresF.size(); i++) {
			TopoDS_Wire wire1 = cuttingWiresF.get(i)[0];
			TopoDS_Wire wire2 = cuttingWiresF.get(i)[1];

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

			TopExp_Explorer exp = new TopExp_Explorer(sewedShape, TopAbs_ShapeEnum.TopAbs_SHELL);
			while(exp.More() > 0) {
				TopoDS_Shell sewedShell = TopoDS.ToShell(exp.Current());
				cuttingShellsF.add(sewedShell);
				cuttingSolidsF.add(new BRepBuilderAPI_MakeSolid(sewedShell).Solid());
				exp.Next();
			}			
		}	

		// create mirror transformation
		gp_Trsf mirroring = new gp_Trsf();
		gp_Ax2 mirrorPlane = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, 0.0), 
				new gp_Dir(0.0, 1.0, 0.0),
				new gp_Dir(1.0, 0.0, 0.0)
				);
		mirroring.SetMirror(mirrorPlane);
		
		// mirroring cutting solids whether necessary
		if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			List<TopoDS_Solid> mirroredCS = new ArrayList<>();
			for(int i = 0; i < cuttingSolids.size(); i++) {
				TopoDS_Solid cs = cuttingSolids.get(i);
				mirroredCS.add(TopoDS.ToSolid(new BRepBuilderAPI_Transform(cs, mirroring, 1).Shape()));
			}
			cuttingSolids.addAll(mirroredCS);
		}
		
		// mirroring cutting solids whether necessary (flap)
		if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			List<TopoDS_Solid> mirroredCS = new ArrayList<>();
			for(int i = 0; i < cuttingSolidsF.size(); i++) {
				TopoDS_Solid cs = cuttingSolidsF.get(i);
				mirroredCS.add(TopoDS.ToSolid(new BRepBuilderAPI_Transform(cs, mirroring, 1).Shape()));
			}
			cuttingSolidsF.addAll(mirroredCS);
		}
		
//		//---------------------------------------------------------------------
//		// Wing cut
//		TopoDS_Solid cutResultW = TopoDS.ToSolid(wingSolid);
//		for(int i = 0; i < cuttingSolids.size(); i++) {
//			
////			BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(cutResultW, cuttingSolids.get(i)); 
//			BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(cuttingSolids.get(i), cutResultW); //TODO why? cut algorithm seems to work fine in this case
//			
//			TopoDS_Shape cut = cutter.Shape();                                             //     putting tools first and complementing cut result
//			TopExp_Explorer exp = new TopExp_Explorer(cut, TopAbs_ShapeEnum.TopAbs_SOLID);
//			System.out.println(OCCUtils.reportOnShape(cut, "Cut report: " + i));
//			System.out.println("Cutting solid orientation: " + cuttingSolids.get(i).Orientation().toString());
//			while(exp.More() > 0) {
//				cutResultW = TopoDS.ToSolid(exp.Current().Complemented());
////				cutResultW = TopoDS.ToSolid(exp.Current());
//				System.out.println("Cut solid orientation: " + cutResultW.Orientation().toString());
//				exp.Next();
//			}
//		}
//		
//		//---------------------------------------------------------------------
//		// Flap cut
//		List<TopoDS_Solid> flaps_ = new ArrayList<>();
//		for(int i = 0; i < cuttingSolids.size(); i++) {
//			
//			BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(cuttingSolids.get(i), wingSolid); 
//			
//			TopoDS_Shape cut = cutter.Shape();                                             //     putting tools first and complementing cut result
//			TopExp_Explorer exp = new TopExp_Explorer(cut, TopAbs_ShapeEnum.TopAbs_SOLID);
//			System.out.println(OCCUtils.reportOnShape(cut, "Cut report: " + i));
//			System.out.println("Cutting solid orientation: " + cuttingSolids.get(i).Orientation().toString());
//			while(exp.More() > 0) {
//				TopoDS_Solid flap_ = TopoDS.ToSolid(exp.Current());
//				flaps_.add(flap_);
//				System.out.println("Cut solid orientation: " + flap_.Orientation().toString());
//				exp.Next();
//			}
//		}
//		List<TopoDS_Solid> flaps = new ArrayList<>();
//		for(int i = 0; i < flaps_.size(); i++) {
//			
//			BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(wingSolid.Complemented(), flaps_.get(i)); 
//			
//			TopoDS_Shape cut = cutter.Shape();                                             //     putting tools first and complementing cut result
//			TopExp_Explorer exp = new TopExp_Explorer(cut, TopAbs_ShapeEnum.TopAbs_SOLID);
//			System.out.println(OCCUtils.reportOnShape(cut, "Cut report: " + i));
//			System.out.println("Cutting solid orientation: " + flaps_.get(i).Orientation().toString());
//			while(exp.More() > 0) {
//				
//				TopoDS_Solid flap_ = TopoDS.ToSolid(exp.Current());
//				
////				gp_Trsf move = new gp_Trsf();
////				double c = chordLengths.get(0)[0];
////				move.SetTranslation(new gp_Vec(0.5*c,0.0,-0.01*c));
////				TopoDS_Shape movedFlap = new BRepBuilderAPI_Transform(flap_, move, 0).Shape();
//				
////				flaps.add(TopoDS.ToSolid(movedFlap));
//				flaps.add(TopoDS.ToSolid(flap_));
//				System.out.println("Cut solid orientation: " + flap_.Orientation().toString());
//				
//				exp.Next();
//			}
//		}
//		
//		//---------------------------------------------------------------------
//		// Flap LE adjustment
//		List<TopoDS_Solid> flapsRounded = new ArrayList<>();
//		
//		for(int i = 0; i < flaps.size(); i++) {
//			BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(flaps.get(i), cuttingSolidsF.get(i));
//			TopoDS_Shape cut = cutter.Shape(); 
//			TopExp_Explorer exp = new TopExp_Explorer(cut, TopAbs_ShapeEnum.TopAbs_SOLID);
//			System.out.println(OCCUtils.reportOnShape(cut, "Cut report: " + i));
//			System.out.println("Cutting solid orientation: " + flaps.get(i).Orientation().toString());
//			
//			while(exp.More() > 0) {
//				TopoDS_Solid flapR = TopoDS.ToSolid(exp.Current());
//				flapsRounded.add(TopoDS.ToSolid(flapR));
//				System.out.println("Cut solid orientation: " + flapR.Orientation().toString());
//				exp.Next();
//			}
//		}
		
		// Wing cut
		TopoDS_Solid cutWing = wingSolid;
		for(int i = 0; i < cuttingSolids.size(); i++) {
			cutWing = TopoDS.ToSolid(performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(cuttingSolids.get(i), false), 
					new Tuple2<TopoDS_Solid, Boolean>(cutWing, false)
					).get(0).Complemented());
		}
		cutWing.Complement();
		
		// Flaps
		List<TopoDS_Solid> flaps = new ArrayList<>();
		for(int i = 0; i < cuttingSolids.size(); i++) {
			TopoDS_Solid auxCutWing = performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(cuttingSolids.get(i), false), 
					new Tuple2<TopoDS_Solid, Boolean>(wingSolid, false)
					).get(0);
			TopoDS_Solid flap = performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(wingSolid, true), 
					new Tuple2<TopoDS_Solid, Boolean>(auxCutWing, false)
					).get(0);
			flaps.add(performBooleanCutOperation(
					new Tuple2<TopoDS_Solid, Boolean>(flap, false), 
					new Tuple2<TopoDS_Solid, Boolean>(cuttingSolidsF.get(i), false)
					).get(0));
		}
		
		// Translate and rotate flaps
		gp_Trsf translation = new gp_Trsf();
		gp_Trsf rotation = new gp_Trsf();
		
		// create rotation axis	
		List<gp_Ax1> rotAxes = new ArrayList<>();
		List<gp_Ax1> mirRotAxes = new ArrayList<>();
		for(int i = 0; i < flapRotationPnts.size(); i++) {					
			TopoDS_Edge rotEdge = new BRepBuilderAPI_MakeEdge(flapRotationPnts.get(i)[0], flapRotationPnts.get(i)[1]).Edge();
			rotAxes.add(convertEdgeToGpAx1(rotEdge, false));
			if(!wing.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				TopoDS_Edge mirRotEdge = TopoDS.ToEdge(new BRepBuilderAPI_Transform(rotEdge, mirroring, 1).Shape());
				mirRotAxes.add(convertEdgeToGpAx1(mirRotEdge, true));
			}
		}
		rotAxes.addAll(mirRotAxes);
		
		flapChords.addAll(flapChords);
		List<TopoDS_Solid> movedFlaps = new ArrayList<>();
		for(int i = 0; i < flaps.size(); i++) {
			double tx = MyMathUtils.arithmeticMean(
					flapChords.get(i)[0],
					flapChords.get(i)[1]		
					);
			
			translation.SetTranslation(new gp_Vec(0.2*tx, 0.0, -0.01*tx));
			rotation.SetRotation(
					rotAxes.get(i),
					Math.toRadians(35)
					);
			
			movedFlaps.add(TopoDS.ToSolid(new BRepBuilderAPI_Transform(
					TopoDS.ToSolid(
							new BRepBuilderAPI_Transform(
									flaps.get(i), 
									rotation, 
									0).Shape()), 
					translation, 
					0).Shape()));	
		}
							
		// Export shapes to CAD file
		List<OCCShape> exportShapes = new ArrayList<>();

		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(cutWing));
		movedFlaps.forEach(f -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(f)));
			
		String fileName = "testBoolean03mds2.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
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
		TopoDS_Shape cutResult = cutter.Shape();                                            
		TopExp_Explorer exp = new TopExp_Explorer(cutResult, TopAbs_ShapeEnum.TopAbs_SOLID);
		while(exp.More() > 0) {
			cutSolid.add(TopoDS.ToSolid(exp.Current()));
			System.out.println("Cut solid orientation: " + cutResult.Orientation().toString());
			exp.Next();
		}
		
		return cutSolid;
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
				}
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

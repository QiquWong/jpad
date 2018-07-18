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
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepAlgoAPI_Section;
import opencascade.GC_MakePlane;
import opencascade.Geom_Surface;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Solid;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
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
		
		// Generate cutting solid
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
		
		// get sections airfoils
		List<OCCShape> secAirfoils = new ArrayList<>();
		
		gp_Dir sectionNormalAxis = new gp_Dir(0, 1, 0);
		for(int i = 0; i < numSymFlap; i++) {
			for(int j = 0; j < 2; j++) {
				BRepAlgoAPI_Section sectionMaker = new BRepAlgoAPI_Section();
				gp_Pnt sectionOrigin = new gp_Pnt(0, symFlapExtrema.get(i)[j]*wingSemiSpan, 0);
				sectionMaker.Init1(wingSolid);
				sectionMaker.Init2(makeIntersectionPlane(sectionOrigin, sectionNormalAxis));
				sectionMaker.Build();
				secAirfoils.add((OCCShape) OCCUtils.theFactory.newShape(sectionMaker.Shape()));
			}		
		}
		
		// manage sections in order to extract desired edges
		List<TopoDS_Edge> sectionEdges = new ArrayList<>();
		for(int i = 0; i < secAirfoils.size(); i++) {
			TopExp_Explorer explorer = new TopExp_Explorer(secAirfoils.get(i).getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
			while(explorer.More() > 0) {
				sectionEdges.add(TopoDS.ToEdge(explorer.Current()));
				explorer.Next();
			}
		}
		System.out.println("========== Number of TopoDS_Edges found exploring sections compounds: " + sectionEdges.size());
		
		List<TopoDS_Edge> airfoilEdges = new ArrayList<>();
		List<TopoDS_Edge> teEdges = new ArrayList<>();
		double meanRange = sectionEdges.stream()
									   .mapToDouble(e -> 
									   		((OCCEdge) OCCUtils.theFactory.newShape(e)).range()[1] - 
									   		((OCCEdge) OCCUtils.theFactory.newShape(e)).range()[0]
									   		)
				                       .sum()/(sectionEdges.size());
		for(int i = 0; i < sectionEdges.size(); i++) {
			TopoDS_Edge edge = sectionEdges.get(i);
			if(((OCCEdge) OCCUtils.theFactory.newShape(edge)).range()[1] < meanRange) 
				teEdges.add(edge);
			else
				airfoilEdges.add(edge);
		}
		System.out.println("========== Number of TopoDS_Edges in airfoilEdges list: " + airfoilEdges.size());
		System.out.println("========== Number of TopoDS_Edges in teEdges list: " + teEdges.size());
		
		// generate airfoil chords		
		List<OCCShape> chords = new ArrayList<>();
		symFlapExtrema.forEach(d -> {
			chords.add((OCCShape) ((OCCEdge) getChordSegmentAtYActual(d[0]*wingSemiSpan, wing).edge()));
			chords.add((OCCShape) ((OCCEdge) getChordSegmentAtYActual(d[1]*wingSemiSpan, wing).edge()));
		});
							
		// Export shapes to CAD file
		List<OCCShape> exportShapes = new ArrayList<>();

		airfoilEdges.forEach(s -> exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(s)));
		chords.forEach(c -> exportShapes.add(c));
		
		String fileName = "testBoolean03mds.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}

	public static Geom_Surface makeIntersectionPlane(gp_Pnt planeOrigin, gp_Dir planeNormalAxis) {
		GC_MakePlane plane = new GC_MakePlane(planeOrigin, planeNormalAxis);
		return plane.Value();
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
			
			// Scale to actual dimension
			x = baseChordXCoords[i]*c;
			y = 0.0;
			z = baseChordZCoords[i]*c;
			
			// Rotation due to twist
			if(!liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
				double r = Math.sqrt(x*x + z*z);
				x = x - r*(1-Math.cos(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
				z = z - r*Math.sin(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN));				
			}
			
			// Actual location
			x = x + xLE + liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER);
			y = yStation;
			z = z + liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER)
					+ (yStation* Math.tan(AircraftUtils.getDihedralAtYActual(liftingSurface, yStation).doubleValue(SI.RADIAN)));

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

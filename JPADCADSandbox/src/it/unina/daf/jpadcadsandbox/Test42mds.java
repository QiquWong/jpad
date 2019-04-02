package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeEdge;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_MakeWire;
import opencascade.BRepFilletAPI_MakeFillet2d;
import opencascade.BSplCLib;
import opencascade.ChFi2d_ConstructionError;
import opencascade.GeomAPI_PointsToBSpline;
import opencascade.GeomAbs_Shape;
import opencascade.GeomAdaptor_Curve;
import opencascade.Geom_BSplineCurve;
import opencascade.TColStd_Array1OfReal;
import opencascade.TColgp_Array1OfPnt;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Vertex;
import opencascade.TopoDS_Wire;
import opencascade.gp_Pnt;
import processing.core.PVector;
import sun.management.GcInfoCompositeData;

public class Test42mds {

	public static void main(String[] args) {
		System.out.println("-----------------------------------------------------------");
		System.out.println("----------- Testing OCC BSpline curve potential -----------");
		System.out.println("-----------------------------------------------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		LiftingSurface liftingSurface = aircraft.getWing();
		
		// Initialize the shape factory
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();
		
		// Generate airfoil curves at breakpoints
		List<List<double[]>> airfoilPts = generateAirfoilPtsAtBPs(liftingSurface);
		List<CADEdge> airfoilEdges = airfoilPts.stream()
				                               .map(pts -> OCCUtils.theFactory.newCurve3D(pts, false).edge())
				                               .collect(Collectors.toList());
		
		List<CADWire> airfoilWires = new ArrayList<>();
		airfoilEdges.forEach(edge -> airfoilWires.add(OCCUtils.theFactory.newWireFromAdjacentEdges(
				edge, 
				OCCUtils.theFactory.newCurve3D(
						edge.vertices()[1].pnt(), 
						edge.vertices()[0].pnt()
						).edge())
				));
		
		// Generate a shell for each panel
		List<OCCShell> panelShells = new ArrayList<>();
		for (int i = 0; i < liftingSurface.getPanels().size(); i++) {
			panelShells.add((OCCShell) OCCUtils.makePatchThruSections(
					airfoilWires.get(i), 
					airfoilWires.get(i+1)
					));
		}
		
		// Generate a generic face
		gp_Pnt pt1 = new gp_Pnt(0.0, 0.0, 0.0);
		gp_Pnt pt2 = new gp_Pnt(0.0, 0.3, 0.0);
		gp_Pnt pt3 = new gp_Pnt(0.0, 0.3, 1.0);
		gp_Pnt pt4 = new gp_Pnt(0.0, 0.0, 1.0);
			
		TopoDS_Wire wire = new BRepBuilderAPI_MakeWire(
				new BRepBuilderAPI_MakeEdge(pt1, pt2).Edge(),
				new BRepBuilderAPI_MakeEdge(pt2, pt3).Edge(),
				new BRepBuilderAPI_MakeEdge(pt3, pt4).Edge(),
				new BRepBuilderAPI_MakeEdge(pt4, pt1).Edge()
				).Wire();
		
		TopoDS_Face face = new BRepBuilderAPI_MakeFace(wire, 1).Face();
		
		// Apply a fillet to some of its vertices
		BRepFilletAPI_MakeFillet2d filletMaker = new BRepFilletAPI_MakeFillet2d(face);
		
		List<TopoDS_Vertex> vertices = new ArrayList<>();
		TopExp_Explorer exp = new TopExp_Explorer();
		exp.Init(face, TopAbs_ShapeEnum.TopAbs_VERTEX);
		while (exp.More() == 1) {
			vertices.add(TopoDS.ToVertex(exp.Current()));
			exp.Next();
		}
		vertices.forEach(v -> System.out.println(Arrays.toString(((OCCVertex) OCCUtils.theFactory.newShape(v)).pnt())));
		
		filletMaker.AddFillet(vertices.get(3), 0.3);
		filletMaker.Build();
		ChFi2d_ConstructionError filletError = filletMaker.Status();
		System.out.println(filletError.name());
		
		TopoDS_Face modFace = TopoDS.ToFace(filletMaker.Shape());
		List<TopoDS_Wire> modWires = new ArrayList<>();
		TopExp_Explorer exp1 = new TopExp_Explorer();
		exp1.Init(modFace, TopAbs_ShapeEnum.TopAbs_WIRE);
		while (exp1.More() == 1) {
			modWires.add(TopoDS.ToWire(exp1.Current()));
			exp1.Next();
		}
		System.out.println("Number of mod wires: " + modWires.size());
		
		// Export shapes
		List<OCCShape> exportShapes = new ArrayList<>();
//		exportShapes.addAll(airfoilWires.stream().map(w -> (OCCShape) w).collect(Collectors.toList()));
		exportShapes.addAll(panelShells);	
//		exportShapes.add((OCCShape) OCCUtils.theFactory.newShape(modWires.get(0)));
		
		OCCUtils.write("Test42mds", FileExtension.STEP, exportShapes);
	}
	
	private static List<List<double[]>> generateAirfoilPtsAtBPs(LiftingSurface liftingSurface) {
		
		List<List<double[]>> airfoilPts = new ArrayList<>();
		
		IntStream.range(0, liftingSurface.getYBreakPoints().size())
				 .forEach(index -> airfoilPts.add(generateAirfoilPtsAtBP(index, liftingSurface))
						 );
		
		return airfoilPts;
	}
	
	private static List<double[]> generateAirfoilPtsAtBP(int index, LiftingSurface liftingSurface) {
		
		List<double[]> airfoilPts = new ArrayList<>();
		
		Airfoil airfoil = liftingSurface.getAirfoilList().get(index);
		double yStation = liftingSurface.getYBreakPoints().get(index).doubleValue(SI.METER);
		
		// -----------------------------------------------------------------
		// Delete duplicates in the airfoil points list, whether necessary
		List<PVector> airfoilPvs = new ArrayList<>();
		for (int i = 0; i < airfoil.getXCoords().length; i++) {
			airfoilPvs.add(new PVector (
					(float) airfoil.getXCoords()[i],
					0.0f,
					(float) airfoil.getZCoords()[i]
					));
		}
		
		Set<PVector> uniqueEntries = new HashSet<>();
		for (Iterator<PVector> iter = airfoilPvs.listIterator(1); iter.hasNext(); ) {
			PVector point = (PVector) iter.next();
			if (!uniqueEntries.add(point))
				iter.remove();
		}
		
		// ---------------------------------
		// Check the airfoil trailing edge
		int nPts = airfoilPvs.size();
		
		if (Math.abs(airfoilPvs.get(0).z - airfoilPvs.get(nPts - 1).z) < 1e-5) {
			
			airfoilPvs.get(0).set(
					airfoilPvs.get(0).x,
					airfoilPvs.get(0).y,
					airfoilPvs.get(0).z + 5e-4f
					);
			
			airfoilPvs.get(nPts - 1).set(
					airfoilPvs.get(nPts - 1).x,
					airfoilPvs.get(nPts - 1).y,
					airfoilPvs.get(nPts - 1).z - 5e-4f
					);
		}
		
		// --------------------------------------
		// Calculate airfoil actual coordinates
		List<double[]> basePts = new ArrayList<>();
		basePts.addAll(airfoilPvs.stream()
				.map(pv -> new double[] {pv.x, pv.y, pv.z})
				.collect(Collectors.toList()));
		
		airfoilPts = AircraftCADUtils.generatePtsAtY(basePts, yStation, liftingSurface);
		
		return airfoilPts;
	}
	
	private static List<Double> getCurveKnots(Geom_BSplineCurve bSpline) {
		
		TColStd_Array1OfReal knotsArray = bSpline.Knots();

		List<Double> knots = new ArrayList<>();
		for (int i = 0; i < knotsArray.Length(); i++) {
			knots.add(knotsArray.Value(i+1));
		}

		return knots;
	}
	
	private static List<double[]> getCurvePoles(Geom_BSplineCurve bSpline) {
		
		TColgp_Array1OfPnt polesArray = bSpline.Poles();

		List<double[]> poles = new ArrayList<>();
		for (int i = 0; i < polesArray.Length(); i++) {
			poles.add(new double[] {
					polesArray.Value(i+1).X(), 
					polesArray.Value(i+1).Y(), 
					polesArray.Value(i+1).Z()}
					);
		}
		
		return poles;
	}
	
	private static List<Double> getCurveWeights(Geom_BSplineCurve bSpline) {
		
		TColStd_Array1OfReal weightsArray = new TColStd_Array1OfReal();
		bSpline.Weights(weightsArray);	

		List<Double> weights = new ArrayList<>();
		for (int i = 0; i < weightsArray.Length(); i++) {
			weights.add(weightsArray.Value(i+1));
		}
		
		return weights;
	}
	
	private static void printCurveData(Geom_BSplineCurve bSpline) {
		
		TColStd_Array1OfReal knotsArray = bSpline.Knots();
		TColgp_Array1OfPnt polesArray = bSpline.Poles();
		TColStd_Array1OfReal weightsArray = new TColStd_Array1OfReal();
		bSpline.Weights(weightsArray);	

		System.out.println("\n\tIs weights array empty? " + (weightsArray.Length() == 0));

		System.out.println("\n\tNumber of knots: " + knotsArray.Length());	
		List<Double> knots = new ArrayList<>();
		System.out.println("\tKnots:");
		for (int i = 0; i < knotsArray.Length(); i++) {
			knots.add(knotsArray.Value(i+1));
			System.out.print("\t\tknot #" + (i+1) + " " + knots.get(i) + "\n");
		}

		System.out.println("\n\tNumber of poles: " + polesArray.Length());	
		List<double[]> poles = new ArrayList<>();
		System.out.println("\tPoles:");
		for (int i = 0; i < polesArray.Length(); i++) {
			poles.add(new double[] {
					polesArray.Value(i+1).X(), 
					polesArray.Value(i+1).Y(), 
					polesArray.Value(i+1).Z()}
					);
			System.out.print("\t\tpole #" + (i+1) + " " + Arrays.toString(poles.get(i)) + "\n");
		}

		System.out.println("\n\tNumber of weights: " + weightsArray.Length());	
		List<Double> weights = new ArrayList<>();
		System.out.println("\tWeights:");
		for (int i = 0; i < weightsArray.Length(); i++) {
			weights.add(weightsArray.Value(i+1));
			System.out.print("\t\tweights #" + (i+1) + " " + weights.get(i) + "\n");
		}
	}
}

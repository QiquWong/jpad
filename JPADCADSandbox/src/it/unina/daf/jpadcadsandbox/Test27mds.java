package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shell;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test27mds {
	
	// Create a lifting surface - fuselage fairing in case the lifting surface root 
	// airfoil top point z coordinate is lower than the fuselage camber z coordinate
	
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();

		// Fairing parameters
		double frontLengthFactor = 1.00;
		double backLengthFactor = 1.00;  
		double sideSizeFactor = 0.95; 

		double heigthAboveRootFactor = 0.90;
		double zBelowFairingMaximumYFactor = 0.35;
		double zBelowFuselageMinimumZFactor = 0.15;
		
		// Geometric data collection
		double liftingSurfaceYApex = wing.getYApexConstructionAxes().doubleValue(SI.METER);

		Airfoil baseAirfoil = wing.getAirfoilList().get(0);

		double rootAirfoilChord = wing.getChordAtYActual(liftingSurfaceYApex);
		double rootAirfoilThickness = baseAirfoil.getThicknessToChordRatio()*rootAirfoilChord;

		List<double[]> rootAirfoilPoints = AircraftUtils.populateCoordinateList(
				liftingSurfaceYApex, 
				baseAirfoil, 
				wing
				);

		double[] topPointRoot = rootAirfoilPoints.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		double[] leadingEdgeRoot = rootAirfoilPoints.stream()
				.min(Comparator.comparing(coord -> coord[0])).get();
		double[] trailingEdgeRoot = rootAirfoilPoints.stream()
				.max(Comparator.comparing(coord -> coord[0])).get();

		List<PVector> fuselageSideCurveMiddle = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(topPointRoot[0], SI.METER));	
		List<PVector> fuselageSideCurveFront = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(leadingEdgeRoot[0] - frontLengthFactor*rootAirfoilChord, SI.METER));
		List<PVector> fuselageSideCurveBack = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(trailingEdgeRoot[0] + backLengthFactor*rootAirfoilChord, SI.METER));
		
		double xAtMiddle = topPointRoot[0];
		double widthYAtMiddle = fuselage.getWidthAtX(xAtMiddle)/2;
		double camberZAtMiddle = fuselage.getCamberZAtX(xAtMiddle);
				
		int nPnts = fuselageSideCurveMiddle.size();
		int nPntsInterp = 100;
		
		List<Double> fuselageSideCurveMiddleLowerZCoords = new ArrayList<>();
		List<Double> fuselageSideCurveMiddleLowerYCoords = new ArrayList<>();
		
		fuselageSideCurveMiddleLowerZCoords.add(camberZAtMiddle);
		fuselageSideCurveMiddleLowerYCoords.add(widthYAtMiddle);
		
		fuselageSideCurveMiddleLowerZCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z < camberZAtMiddle)
				.map(pv -> (double) pv.z)
				.collect(Collectors.toList()));
		fuselageSideCurveMiddleLowerYCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z < camberZAtMiddle)
				.map(pv -> (double) pv.y)
				.collect(Collectors.toList()));
		
		Collections.reverse(fuselageSideCurveMiddleLowerZCoords);
		Collections.reverse(fuselageSideCurveMiddleLowerYCoords);
		
//		double[] fuselageSideCurveMiddleYCoordsInterp = MyArrayUtils.convertToDoublePrimitive(
//				MyMathUtils.getInterpolatedValue1DLinear(
//						MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleZCoords), 
//						MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleYCoords),
//						MyArrayUtils.linspace(
//								fuselageSideCurveMiddleZCoords.get(0), 
//								fuselageSideCurveMiddleZCoords.get(nPnts-1),
//								nPntsInterp
//								)
//						)
//				); 

		double[] fuselageBottomPoint = new double[] {
				(float) fuselageSideCurveMiddle.get(fuselageSideCurveMiddle.size()-1).x,
				(float) fuselageSideCurveMiddle.get(fuselageSideCurveMiddle.size()-1).y,
				(float) fuselageSideCurveMiddle.get(fuselageSideCurveMiddle.size()-1).z
		};		
		double[] fuselageMaximumYPointFront = fuselageSideCurveFront.stream()
				.map(pv -> new double[] {pv.x, pv.y, pv.z})
				.max(Comparator.comparing(coord -> coord[1])).get();
		double[] fuselageMaximumYPointBack = fuselageSideCurveBack.stream()
				.map(pv -> new double[] {pv.x, pv.y, pv.z})
				.max(Comparator.comparing(coord -> coord[1])).get();
//		double[] fuselageMaximumYPointMiddle = Arrays.asList(fuselageSideCurveMiddleYCoordsInterp).stream()
//				.max(Comparator.comparing(coord -> coord[1])).get();
		
		double zMaxAdmissibleFuselageWidth;
		double[] fuselageSidePoint;
//		if(fuselageMaximumWidthPointFront[1] < fuselageMaximumWidthPointBack[2]) {
//
//			zMaxAdmissibleFuselageWidth = fuselageMaximumWidthPointFront[2];
//			fuselageSidePoint = fuselageSideCurveFront.stream()
//					.map(pv -> new double[] {pv.x, pv.y, pv.z})
//					.filter(coord -> coord[1] > fuselageMaximumWidthPointFront[1]*sideSizeFactor)
//					.min(Comparator.comparing(coord -> coord[2])).get();		
//		} else {
//
//			zMaxAdmissibleFuselageWidth = fuselageMaximumWidthPointBack[2];
//			fuselageSidePoint = fuselageSideCurveBack.stream()
//					.map(pv -> new double[] {pv.x, pv.y, pv.z})
//					.filter(coord -> coord[1] > fuselageMaximumWidthPointBack[1]*sideSizeFactor)
//					.min(Comparator.comparing(coord -> coord[2])).get();
//		}
//		fuselageSidePoint = Arrays.asList(fuselageSideCurveMiddleYCoordsInterp).stream()
//				.filter(coord -> coord[1] > fuselageMaximumYPointMiddle[1]*sideSizeFactor)
//				.min(Comparator.comparing(coord -> coord[2])).get();
		
		double fuselageSidePointZ = MyMathUtils.getInterpolatedValue1DSpline(
				MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleLowerYCoords), 
				MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleLowerZCoords), 
				widthYAtMiddle*sideSizeFactor
				);
		
		double fairingMaximumY = widthYAtMiddle*sideSizeFactor;		
		double fairingMinimumZ = fuselageBottomPoint[2] - (fuselageSidePointZ - fuselageBottomPoint[2])*zBelowFuselageMinimumZFactor;

		List<double[]> sideAirfoilPoints = AircraftUtils.populateCoordinateList(
				fairingMaximumY, 
				baseAirfoil, 
				wing
				);

		double[] topPointSide = sideAirfoilPoints.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		
		if(topPointSide[2] > camberZAtMiddle) return;

		double fairingMaximumZ = Math.max(topPointRoot[2], topPointSide[2]) + rootAirfoilThickness*heigthAboveRootFactor;
		
		// Create points and curves for sketching (XZ plane)
		OCCUtils.initCADShapeFactory();

//		CADGeomCurve3D fusCurveeee = OCCUtils.theFactory.newCurve3D(fuselageSideCurveMiddleInterp, false);
		CADGeomCurve3D rootAirfoil = OCCUtils.theFactory.newCurve3D(rootAirfoilPoints, false);
		CADGeomCurve3D sideAirfoil = OCCUtils.theFactory.newCurve3D(sideAirfoilPoints, false);

		double[] pointA = new double[] {
				fuselageMaximumYPointFront[0], 
				topPointRoot[1], 
				fuselageSidePointZ - (fuselageSidePointZ - fuselageBottomPoint[2])*zBelowFairingMaximumYFactor
		};

		double[] pointB = new double[] {
				topPointRoot[0], 
				topPointRoot[1], 
				fairingMaximumZ
		};

		double[] pointC = new double[] {
				fuselageMaximumYPointBack[0], 
				topPointRoot[1], 
				fuselageSidePointZ - (fuselageSidePointZ - fuselageBottomPoint[2])*zBelowFairingMaximumYFactor
		};

		double[] pointD = new double[] {
				topPointRoot[0], 
				topPointRoot[1], 
				fairingMinimumZ
		};

		double[] pointE = new double[] {
				leadingEdgeRoot[0],
				leadingEdgeRoot[1],
				pointB[2] - (pointB[2] - leadingEdgeRoot[2])*0.15
		};

		double[] pointF = new double[] {
				trailingEdgeRoot[0],
				trailingEdgeRoot[1],
				pointB[2] - (pointB[2] - trailingEdgeRoot[2])*0.15
		};

		double[] pointA2 = new double[] {
				fuselageMaximumYPointFront[0],
				topPointRoot[1],
				fuselageSidePointZ
		};

		double[] pointC2 = new double[] {
				fuselageMaximumYPointBack[0],
				topPointRoot[1],
				fuselageSidePointZ
		};

		CADGeomCurve3D upperCurve = OCCUtils.theFactory.newCurve3D(false, pointA2, pointE, pointB, pointF, pointC2);
		CADGeomCurve3D lowerCurve = OCCUtils.theFactory.newCurve3D(false, pointA, pointD, pointC);

		// Create points and curves for sketching (XY)
		double[] pointA3 = new double[] {
				pointA[0],
				pointA[1],
				pointA[2] + (pointA2[2] - pointA[2])/2
		};

		double[] pointG = new double[] {
				topPointRoot[0],
				fairingMaximumY,
				pointA3[2]
		};

		double[] pointH = new double[] {
				leadingEdgeRoot[0],
				fairingMaximumY - (fairingMaximumY - topPointRoot[1])*0.01,
				pointA3[2]
		};

		double[] pointI = new double[] {
				trailingEdgeRoot[0],
				fairingMaximumY - (fairingMaximumY - topPointRoot[1])*0.01,
				pointA3[2]
		};

		double[] pointC3 = new double[] {
				pointC[0],
				pointC[1],
				pointC[2] + (pointC2[2] - pointC[2])/2
		};

		CADGeomCurve3D sideCurve = OCCUtils.theFactory.newCurve3D(false, pointA3, pointH, pointG, pointI, pointC3);
		
		// Create curves to patch through
		int nUppPnts = 12; // number of profile curves
		upperCurve.discretize(nUppPnts);
		
		List<double[]> upperCurvePoints = ((OCCGeomCurve3D) upperCurve).getDiscretizedCurve().getPoints().stream()
				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
				.collect(Collectors.toList());
		
		int nLowPnts = 25; // number of points for interpolation
		lowerCurve.discretize(nLowPnts);
		
		List<double[]> lowerCurvePoints = ((OCCGeomCurve3D) lowerCurve).getDiscretizedCurve().getPoints().stream()
				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
				.collect(Collectors.toList());
		
		double[] lowerCurveXCoords = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(
						lowerCurvePoints.stream()
										 .map(a -> a[0])
										 .collect(Collectors.toList())
										 ));
		double[] lowerCurveZCoords = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(
						lowerCurvePoints.stream()
									     .map(a -> a[2])
									     .collect(Collectors.toList())
									     ));
		
		int nSidePnts = 25; // number of points for interpolation
		sideCurve.discretize(nSidePnts);
		
		List<double[]> sideCurvePoints = ((OCCGeomCurve3D) sideCurve).getDiscretizedCurve().getPoints().stream()
				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
				.collect(Collectors.toList());
		
		double[] sideCurveXCoords = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(
						sideCurvePoints.stream()
										 .map(a -> a[0])
										 .collect(Collectors.toList())
										 ));
		double[] sideCurveYCoords = MyArrayUtils.convertToDoublePrimitive(
				MyArrayUtils.convertListOfDoubleToDoubleArray(
						sideCurvePoints.stream()
									     .map(a -> a[1])
									     .collect(Collectors.toList())
									     ));
		
		List<CADGeomCurve3D> upperCurves = new ArrayList<>();
		List<CADGeomCurve3D> lowerCurves = new ArrayList<>();
		List<CADGeomCurve3D> sideCurves = new ArrayList<>();
		
		for(int i = 1; i < nUppPnts-1; i++) {
			
			double[] upperCurvePoint = upperCurvePoints.get(i);
			double[] upperCurve2Point = new double[] {
					upperCurvePoint[0],
					MyMathUtils.getInterpolatedValue1DLinear(
							sideCurveXCoords, 
							sideCurveYCoords, 
							upperCurvePoint[0]
							),
					upperCurvePoint[2]
			};
			upperCurves.add(OCCUtils.theFactory.newCurve3D(upperCurvePoint, upperCurve2Point));
			
			double[] lowerCurve2Point = new double[] {
					upperCurvePoint[0],
					MyMathUtils.getInterpolatedValue1DLinear(
							sideCurveXCoords,
							sideCurveYCoords,
							upperCurvePoint[0]
							),
					MyMathUtils.getInterpolatedValue1DLinear(
							lowerCurveXCoords,
							lowerCurveZCoords,
							upperCurvePoint[0]
							),
			};				
			double[] interpolatedLowerCurvePoint = new double[] {
					upperCurvePoint[0],
					upperCurvePoint[1],
					MyMathUtils.getInterpolatedValue1DLinear(
							lowerCurveXCoords,
							lowerCurveZCoords,
							upperCurvePoint[0]
							),
			};
			lowerCurves.add(OCCUtils.theFactory.newCurve3D(lowerCurve2Point, interpolatedLowerCurvePoint));
			
			double[] interpolatedSideCurvePoint = new double[] {
					upperCurvePoint[0],
					MyMathUtils.getInterpolatedValue1DLinear(
							sideCurveXCoords,
							sideCurveYCoords,
							upperCurvePoint[0]
							),
					sideCurvePoints.get(0)[2]
			};		
			sideCurves.add(OCCUtils.theFactory.newCurve3D(
					false, 
					upperCurve2Point, 
					interpolatedSideCurvePoint, 
					lowerCurve2Point
					));
		}

		// Create patches
		OCCShape upperPatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newVertex(pointA2[0], pointA2[1], pointA2[2]), 
				upperCurves, 
				OCCUtils.theFactory.newVertex(pointC2[0], pointC2[1], pointC2[2])
				);	
		
		OCCShape lowerPatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newVertex(pointA[0], pointA[1], pointA[2]), 
				lowerCurves, 
				OCCUtils.theFactory.newVertex(pointC[0], pointC[1], pointC[2])
				);	
		
		List<TopoDS_Edge> upperEdges = new ArrayList<>();
		TopExp_Explorer explorerUpp = new TopExp_Explorer(upperPatch.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
		while(explorerUpp.More() > 0) {
			upperEdges.add(TopoDS.ToEdge(explorerUpp.Current()));
			explorerUpp.Next();
		}	
		
		List<TopoDS_Edge> lowerEdges = new ArrayList<>();
		TopExp_Explorer explorerLow = new TopExp_Explorer(lowerPatch.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
		while(explorerLow.More() > 0) {
			lowerEdges.add(TopoDS.ToEdge(explorerLow.Current()));
			explorerLow.Next();
		}	
		
		OCCShape sidePatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(upperEdges.get(1))),
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(lowerEdges.get(3)))
				);
		
		// Create a shell from adjacent patches
		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
				(CADFace) OCCUtils.theFactory.newShape(upperPatch.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(sidePatch.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(lowerPatch.getShape())
				);
		
		// Apply a fillet to specific edges
		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());
		double filletRadius = (pointA2[2] - pointA[2])*1.00;
		
		List<TopoDS_Edge> shellEdges = new ArrayList<>();
		TopExp_Explorer shellExplorer = new TopExp_Explorer(rightShell.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
		while(shellExplorer.More() > 0) {
			shellEdges.add(TopoDS.ToEdge(shellExplorer.Current()));
			shellExplorer.Next();
		}
		filletMaker.Add(filletRadius, shellEdges.get(6));
		
		List<TopoDS_Shell> filletShells = new ArrayList<>();
		TopExp_Explorer filletShellExplorer = new TopExp_Explorer(filletMaker.Shape(), TopAbs_ShapeEnum.TopAbs_SHELL);
		while(filletShellExplorer.More() > 0) {
			filletShells.add(TopoDS.ToShell(filletShellExplorer.Current()));
			filletShellExplorer.Next();
		}
		OCCShell rightShellFillet = (OCCShell) OCCUtils.theFactory.newShape(filletShells.get(0));
		
		// Mirroring and creating the solid	
		gp_Trsf mirrorTransform = new gp_Trsf();
		gp_Ax2 mirrorPointPlane = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, 0.0),
				new gp_Dir(0.0, 1.0, 0.0),
				new gp_Dir(1.0, 0.0, 0.0)
				);
		mirrorTransform.SetMirror(mirrorPointPlane);	
		BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
		mirrorBuilder.Perform(rightShellFillet.getShape(), 1);
		OCCShell leftShell = (OCCShell) OCCUtils.theFactory.newShape(mirrorBuilder.Shape());
		
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
		solidMaker.Add(TopoDS.ToShell(rightShellFillet.getShape()));
		solidMaker.Add(TopoDS.ToShell(leftShell.getShape()));
		solidMaker.Build();
		OCCSolid solidFairing = (OCCSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		
		// Export shapes to CAD file
		List<OCCShape> shapes = new ArrayList<>();
		
//		shapes.add((OCCShape) ((OCCEdge) rootAirfoil.edge()));
//		shapes.add((OCCShape) ((OCCEdge) sideAirfoil.edge()));
//		shapes.add((OCCShape) ((OCCEdge) upperCurve.edge()));
//		shapes.add((OCCShape) ((OCCEdge) lowerCurve.edge()));
//		shapes.add((OCCShape) ((OCCEdge) sideCurve.edge()));
//		shapes.add(upperPatch);
//		shapes.add(lowerPatch);
//		shapes.add(sidePatch);
		shapes.add(solidFairing);
		
//		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
//		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
//		
//		shapes.addAll(fuselageShapes);
//		shapes.addAll(wingShapes);
		
		String fileName = "Test27mds.brep";

		if (OCCUtils.write(fileName, shapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}
}

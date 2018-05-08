package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.sun.javafx.image.impl.IntArgb.ToIntArgbPreConv;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.FileExtension;
import opencascade.BRepBuilderAPI_MakeSolid;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopLoc_Location;
import opencascade.TopoDS;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test25mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface canard = aircraft.getCanard();
		
		// Fairing parameters
		double frontLengthFactor = 1.25; // 0.75
		double backLengthFactor = 1.25;  // 0.75
		double sideSizeFactor = 0.75;    // 0.75
		
		double heigthAboveRootFactor = 0.10;
		double heigthAvoveMinimumFactor = 0.30;
		double heigthBelowMinimumFactor = 0.20;
		
		// Geometric data collection
		double liftingSurfaceXApex = canard.getXApexConstructionAxes().doubleValue(SI.METER);
		double liftingSurfaceYApex = canard.getYApexConstructionAxes().doubleValue(SI.METER);
		double liftingSurfaceZApex = canard.getZApexConstructionAxes().doubleValue(SI.METER);
		
		Airfoil baseAirfoil = canard.getAirfoilList().get(0);
		
		double rootAirfoilChord = canard.getChordAtYActual(liftingSurfaceYApex);
		double rootAirfoilThickness = baseAirfoil.getThicknessToChordRatio()*rootAirfoilChord;
		
		List<double[]> rootAirfoilPoints = AircraftUtils.populateCoordinateList(
				liftingSurfaceYApex, 
				baseAirfoil, 
				canard
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
		
		double[] fuselageTopPoint = new double[] {
				fuselageSideCurveMiddle.get(0).x,
				fuselageSideCurveMiddle.get(0).y,
				fuselageSideCurveMiddle.get(0).z
		};		
		double[] fuselageMaximumYPointFront = fuselageSideCurveFront.stream()
					.map(pv -> new double[] {pv.x, pv.y, pv.z})
					.max(Comparator.comparing(coord -> coord[1])).get();
		double[] fuselageMaximumYPointBack = fuselageSideCurveBack.stream()
					.map(pv -> new double[] {pv.x, pv.y, pv.z})
					.max(Comparator.comparing(coord -> coord[1])).get();
		
		double zMaxAdmissibleFuselageWidth;
		double[] fuselageSidePoint;
		if(fuselageMaximumYPointFront[1] < fuselageMaximumYPointBack[2]) {
			
			zMaxAdmissibleFuselageWidth = fuselageMaximumYPointFront[1];
			fuselageSidePoint = fuselageSideCurveFront.stream()
						.map(pv -> new double[] {pv.x, pv.y, pv.z})
						.filter(coord -> coord[1] > fuselageMaximumYPointFront[1]*sideSizeFactor)
						.max(Comparator.comparing(coord -> coord[2])).get();		
		} else {
			
			zMaxAdmissibleFuselageWidth = fuselageMaximumYPointBack[2];
			fuselageSidePoint = fuselageSideCurveBack.stream()
					.map(pv -> new double[] {pv.x, pv.y, pv.z})
					.filter(coord -> coord[1] > fuselageMaximumYPointBack[1]*sideSizeFactor)
					.max(Comparator.comparing(coord -> coord[2])).get();
		}
		double fairingMaximumWidth = fuselageSidePoint[1];		
		double fairingMinimumHeigth = fuselageSidePoint[2];
		
		List<double[]> sideAirfoilPoints = AircraftUtils.populateCoordinateList(
				fairingMaximumWidth, 
				baseAirfoil, 
				canard
				);
		
		double[] topPointSide = sideAirfoilPoints.stream()
					.max(Comparator.comparing(coord -> coord[2])).get();
		
		double fairingMaximumHeigth = Math.max(topPointRoot[2], topPointSide[2]) + heigthAboveRootFactor*rootAirfoilThickness;
		
		// Create points and curves for sketching (XZ plane)
		OCCUtils.initCADShapeFactory();
		
		CADGeomCurve3D rootAirfoil = OCCUtils.theFactory.newCurve3D(rootAirfoilPoints, false);
		CADGeomCurve3D sideAirfoil = OCCUtils.theFactory.newCurve3D(sideAirfoilPoints, false);
		
		double[] pointA = new double[] {
				fuselageMaximumYPointFront[0], 
				topPointRoot[1], 
				fairingMinimumHeigth
		};
		
		double[] pointB = new double[] {
				topPointRoot[0], 
				topPointRoot[1], 
				fairingMaximumHeigth
		};
		
		double[] pointC = new double[] {
				fuselageMaximumYPointBack[0], 
				topPointRoot[1], 
				fairingMinimumHeigth
		};
		
		double[] pointD = new double[] {
				topPointRoot[0], 
				topPointRoot[1], 
				fairingMinimumHeigth - (fairingMinimumHeigth - zMaxAdmissibleFuselageWidth)*heigthBelowMinimumFactor
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
				fairingMinimumHeigth + (fuselageSideCurveFront.get(0).z - fairingMinimumHeigth)*heigthAvoveMinimumFactor
		};
		
		double[] pointC2 = new double[] {
				fuselageMaximumYPointBack[0],
				topPointRoot[1],
				fairingMinimumHeigth + (fuselageSideCurveBack.get(0).z - fairingMinimumHeigth)*heigthAvoveMinimumFactor
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
				fuselageSidePoint[1],
				pointA3[2]
		};
		
		double[] pointH = new double[] {
				leadingEdgeRoot[0],
				fuselageSidePoint[1] - (fuselageSidePoint[1] - topPointRoot[1])*0.01,
				pointA3[2]
		};
		
		double[] pointI = new double[] {
				trailingEdgeRoot[0],
				fuselageSidePoint[1] - (fuselageSidePoint[1] - topPointRoot[1])*0.01,
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
		double filletRadius = (pointA2[2] - pointA[2])*0.8;
		
		List<TopoDS_Edge> shellEdges = new ArrayList<>();
		TopExp_Explorer shellExplorer = new TopExp_Explorer(rightShell.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
		while(shellExplorer.More() > 0) {
			shellEdges.add(TopoDS.ToEdge(shellExplorer.Current()));
			shellExplorer.Next();
		}
		filletMaker.Add(filletRadius, shellEdges.get(1));
		
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
//		fairingGuideCurves.forEach(crv -> shapes.add((OCCShape) ((OCCEdge) crv.edge())));
//		upperCurves.forEach(crv -> shapes.add((OCCShape) ((OCCEdge) crv.edge())));
//		lowerCurves.forEach(crv -> shapes.add((OCCShape) ((OCCEdge) crv.edge())));
//		sideCurves.forEach(crv -> shapes.add((OCCShape) ((OCCEdge) crv.edge())));
//		fairingGuideCurves.forEach(crv -> shapes.add((OCCShape) ((OCCEdge) crv.edge())));
//		shapes.add(upperPatch);
//		shapes.add(lowerPatch);
//		shapes.add(sidePatch);
//		shapes.add(rightShell);
//		shellEdges.stream().limit(3).forEach(edge -> shapes.add((OCCShape) OCCUtils.theFactory.newShape(edge)));
		shapes.add(solidFairing);
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(canard, ComponentEnum.CANARD, 1e-3, false, true, false);
		
		shapes.addAll(fuselageShapes);
		shapes.addAll(canardShapes);
		
		String fileName = "Test25mds.brep";

		if (OCCUtils.write(fileName, shapes))
			System.out.println("========== [main] Output written on file: " + fileName);
		
// ------------------------------------------------------------------------------------------------------------------------------		
// ------------------------------------------------------------------------------------------------------------------------------	
// ------------------------------------------------------------------------------------------------------------------------------
		
		
//		// Importing the aircraft and needed components
//		Aircraft aircraft = AircraftUtils.importAircraft(args);			
//		Fuselage fuselage = aircraft.getFuselage();		
//		LiftingSurface wing = aircraft.getWing();
//		
//		// Getting needed quantities
//		double wingXApex = wing.getXApexConstructionAxes().doubleValue(SI.METER);
//		double wingYApex = wing.getYApexConstructionAxes().doubleValue(SI.METER);
//		double wingZApex = wing.getZApexConstructionAxes().doubleValue(SI.METER);
//		double fuselageXApex = fuselage.getXApexConstructionAxes().doubleValue(SI.METER);
//		double fuselageYApex = fuselage.getYApexConstructionAxes().doubleValue(SI.METER);
//		double fuselageZApex = fuselage.getZApexConstructionAxes().doubleValue(SI.METER);
//		double fuselageCylinderWidth = fuselage.getSectionCylinderWidth().doubleValue(SI.METER);
//		
//		double rootChord = wing.getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER);
//		double secondChord = wing.getLiftingSurfaceCreator().getChordAtYActual(fuselageCylinderWidth/2);
//		
//		Airfoil baseRootAirfoil = wing.getLiftingSurfaceCreator().getAirfoilList().get(0);
//		
//		// Creating points for the fairing profile
//		List<double[]> rootAirfoil = AircraftUtils.populateCoordinateList(
//				wingYApex, 
//				baseRootAirfoil, 
//				wing
//				);
//		
//		List<double[]> secondAirfoil = AircraftUtils.populateCoordinateList(
//				wingYApex + fuselageCylinderWidth/2, 
//				baseRootAirfoil, 
//				wing
//				);
//		
//		double[] highestPointRoot = rootAirfoil.stream()
//				.max(Comparator.comparing(coord -> coord[2])).get();
//		double[] highestPointSecond = secondAirfoil.stream()
//				.max(Comparator.comparing(coord -> coord[2])).get();
//			
//		System.out.println(Arrays.toString(highestPointRoot) + ", " + Arrays.toString(highestPointSecond));
//		
//		double rootAirfoilXLE = rootAirfoil.stream()
//				.map(coord -> coord[0])
//				.min(Comparator.comparing(x -> x)).get();
//		double rootAirfoilXTE = rootAirfoil.stream()
//				.map(coord -> coord[0])
//				.max(Comparator.comparing(x -> x)).get();
//		
//		double secondAirfoilXLE = secondAirfoil.stream()
//				.map(coord -> coord[0])
//				.min(Comparator.comparing(x -> x)).get();
//		double secondAirfoilXTE = secondAirfoil.stream()
//				.map(coord -> coord[0])
//				.max(Comparator.comparing(x -> x)).get();
//		
//		System.out.println("[" + rootAirfoilXLE + ", " + rootAirfoilXTE + "]");
//		System.out.println("[" + secondAirfoilXLE + ", " + secondAirfoilXTE + "]");
//		
//		PVector pVectorASecond = fuselage.getUniqueValuesYZSideRCurve(
//				Amount.valueOf(rootAirfoilXLE - 1.5*rootChord, SI.METER)).stream()
//				.max(Comparator.comparing(vec -> vec.y)).get();
//		PVector pVectorBSecond = fuselage.getUniqueValuesYZSideRCurve(
//				Amount.valueOf(rootAirfoilXTE + 1.0*rootChord, SI.METER)).stream()
//				.max(Comparator.comparing(vec -> vec.y)).get();
//		double[] pointASecond = new double[] {
//				pVectorASecond.x,
//				pVectorASecond.y,
//				pVectorASecond.z
//		};
//		double[] pointBSecond = new double[] {
//				pVectorBSecond.x,
//				pVectorBSecond.y,
//				pVectorBSecond.z
//		};
//		
//		double[] pointARoot = new double[] {
//				pointASecond[0],
//				fuselageYApex,
//				pointASecond[2]
//		};
//		double[] pointBRoot = new double[] {
//				pointBSecond[0],
//				fuselageYApex,
//				pointBSecond[2]
//		};
//		
//		// Creating tangent vectors for the fairing profile
//		double tangentARadiansAngle = 55/(57.3); 
//		double tangentBRadiansAngle = -55/(57.3);
//		
//		double[] airfoilTangent = new double[] {1.0, 0.0, 0.0};
//		double[] pointATangent = new double[] {
//				Math.cos(tangentARadiansAngle), 
//				0.0, 
//				Math.sin(tangentARadiansAngle)
//		};
//		double[] pointBTangent = new double[] {
//				Math.cos(tangentBRadiansAngle), 
//				0.0, 
//				Math.sin(tangentBRadiansAngle)
//		};
//		
//		// Creating curves for the fairing profile
//		OCCUtils.initCADShapeFactory();
//		
//		CADGeomCurve3D rootAirfoilCurve = OCCUtils.theFactory.newCurve3D(rootAirfoil, false);
//		CADGeomCurve3D secondAirfoilCurve = OCCUtils.theFactory.newCurve3D(secondAirfoil, false);
//		
//		List<double[]> curve1RootPoints = new ArrayList<>();
//		curve1RootPoints.add(pointARoot);
//		curve1RootPoints.add(highestPointRoot);
//		CADGeomCurve3D curve1Root = OCCUtils.theFactory.newCurve3D(
//				curve1RootPoints, 
//				false, 
//				pointATangent, 
//				airfoilTangent,
//				false
//				);	
//		List<double[]> curve2RootPoints = new ArrayList<>();
//		curve2RootPoints.add(highestPointRoot);
//		curve2RootPoints.add(pointBRoot);
//		CADGeomCurve3D curve2Root = OCCUtils.theFactory.newCurve3D(
//				curve2RootPoints, 
//				false, 
//				airfoilTangent, 
//				pointBTangent,
//				false
//				);
//		
//		List<double[]> curve1SecondPoints = new ArrayList<>();
//		curve1SecondPoints.add(pointASecond);
//		curve1SecondPoints.add(highestPointSecond);
//		CADGeomCurve3D curve1Second = OCCUtils.theFactory.newCurve3D(
//				curve1SecondPoints, 
//				false, 
//				pointATangent, 
//				airfoilTangent,
//				false
//				);	
//		List<double[]> curve2SecondPoints = new ArrayList<>();
//		curve2SecondPoints.add(highestPointSecond);
//		curve2SecondPoints.add(pointBSecond);
//		CADGeomCurve3D curve2Second = OCCUtils.theFactory.newCurve3D(
//				curve2SecondPoints, 
//				false, 
//				airfoilTangent, 
//				pointBTangent,
//				false
//				);
//		
//		CADGeomCurve3D curveBARoot = OCCUtils.theFactory.newCurve3D(pointBRoot, pointARoot);
//		CADGeomCurve3D curveBASecond = OCCUtils.theFactory.newCurve3D(pointBSecond, pointASecond);
//		
//		// Creating shell from curves
//		OCCShape upperSurface1 = OCCUtils.makePatchThruSections(curve1Root, curve1Second);
//		OCCShape upperSurface2 = OCCUtils.makePatchThruSections(curve2Root, curve2Second);
//		OCCShape lowerSurface = OCCUtils.makePatchThruSections(curveBARoot, curveBASecond);
//		OCCShape sideSurface = (OCCShape) OCCUtils.makeFilledFace(curveBASecond, curve1Second, curve2Second);
//		
//		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
//				(CADFace) OCCUtils.theFactory.newShape(upperSurface1.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(upperSurface2.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(lowerSurface.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(sideSurface.getShape())
//				);
//		
//		// Testing fillet capabilities
//		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());
//		
//		TopExp_Explorer explorer = new TopExp_Explorer(rightShell.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
//		while(explorer.More() > 0) {
//			filletMaker.Add(rootChord/10, TopoDS.ToEdge(explorer.Current()));
//			explorer.Next();
//		}
//		System.out.println(filletMaker.Shape().ShapeType().equals(TopAbs_ShapeEnum.TopAbs_COMPOUND));
//		List<TopoDS_Shell> rightFilletedShells = new ArrayList<>();
//		TopoDS_Shape rightFilletedCompound = filletMaker.Shape();
//		TopExp_Explorer exp = new TopExp_Explorer(rightFilletedCompound, TopAbs_ShapeEnum.TopAbs_SHELL);
//		while(exp.More() > 0) {
//			rightFilletedShells.add(TopoDS.ToShell(exp.Current()));
//			exp.Next();
//		}
//		System.out.println(rightFilletedShells.size());
//		OCCShell rightShellFilleted = (OCCShell) OCCUtils.theFactory.newShape(rightFilletedShells.get(0));
//		
//		// Creating the solid	
//		gp_Trsf mirrorTransform = new gp_Trsf();
//		gp_Ax2 mirrorPointPlane = new gp_Ax2(
//				new gp_Pnt(0.0, 0.0, 0.0),
//				new gp_Dir(0.0, 1.0, 0.0),
//				new gp_Dir(1.0, 0.0, 0.0)
//				);
//		mirrorTransform.SetMirror(mirrorPointPlane);	
//		BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
////		mirrorBuilder.Perform(rightShell.getShape(), 1);
//		mirrorBuilder.Perform(rightShellFilleted.getShape(), 1);
//		OCCShell leftShell = (OCCShell) OCCUtils.theFactory.newShape(mirrorBuilder.Shape());
//		
//		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
////		solidMaker.Add(TopoDS.ToShell(rightShell.getShape()));
//		solidMaker.Add(TopoDS.ToShell(rightShellFilleted.getShape()));
//		solidMaker.Add(TopoDS.ToShell(leftShell.getShape()));
//		solidMaker.Build();
//		OCCSolid solidFairing = (OCCSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
//		
//		// Export shapes
//		List<OCCShape> shapes = new ArrayList<>();
//		
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve1Root).edge());
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve2Root).edge());
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve1Second).edge());
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve2Second).edge());
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) rootAirfoilCurve).edge());
////		shapes.add((OCCEdge) ((OCCGeomCurve3D) secondAirfoilCurve).edge());
//		
//		shapes.add(solidFairing);
//		
////		shapes.add(rightShellFilleted);
//		
////		String fileName = "Test25mds.brep";
////		
////		if (OCCUtils.write(fileName, shapes))
////			System.out.println("========== [main] Output written on file: " + fileName);
//		
//		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
//		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
//		
//		shapes.addAll(fuselageShapes);
//		shapes.addAll(wingShapes);
//		
//		AircraftUtils.getAircraftSolidFile(shapes, "Test25mds", FileExtension.STEP);

		
// ------------------------------------------------------------------------------------------------------------------------------		
// ------------------------------------------------------------------------------------------------------------------------------	
// ------------------------------------------------------------------------------------------------------------------------------
		
		
//		PVector highestPointFuselageA = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
//				Amount.valueOf(rootAirfoilXLE - 1.5*rootChord, SI.METER)).get(0);		
//		PVector highestPointFuselageB = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
//				Amount.valueOf(rootAirfoilXTE + 1.0*rootChord, SI.METER)).get(0);
		
//		PVector pointASecond = new PVector(
//				(float) (secondAirfoilXLE - 1.5*secondChord),
//				(float) fuselageCylinderWidth/2,
//				highestPointFuselageA.z
//				);
//		PVector pointBSecond = new PVector(
//				(float) (secondAirfoilXTE + 1.0*secondChord),
//				(float) fuselageCylinderWidth/2,
//				highestPointFuselageA.z
//				);
		
//		PVector pointDSecond = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
//				Amount.valueOf(rootAirfoilXLE - 1.5*rootChord, SI.METER)).stream()
//				.max(Comparator.comparing(vec -> vec.y)).get();
//		PVector pointCSecond = fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
//				Amount.valueOf(rootAirfoilXTE + 1.0*rootChord, SI.METER)).stream()
//				.max(Comparator.comparing(vec -> vec.y)).get();
//		
//		PVector pointDRoot = new PVector(
//				highestPointFuselageA.x,
//				highestPointFuselageA.y,
//				pointDSecond.z
//				);
//		PVector pointCRoot = new PVector(
//				highestPointFuselageB.x,
//				highestPointFuselageB.y,
//				pointCSecond.z
//				);
		
//		System.out.println(highestPointFuselageA.toString());
//		System.out.println(highestPointFuselageB.toString());
//		System.out.println(pointASecond.toString());
//		System.out.println(pointBSecond.toString());
		
		// Creating tangent vectors for the fairing profile
//		PVector horTangent = new PVector(1, 0, 0);
		
		// Creating curves for the fairing profile
//		OCCUtils.initCADShapeFactory();
		
//		List<double[]> pointsCurve1Root = new ArrayList<>();
//		pointsCurve1Root.add(new double[] {highestPointFuselageA.x, highestPointFuselageA.y, highestPointFuselageA.z});
//		pointsCurve1Root.add(highestPointRoot);
//		List<double[]> pointsCurve2Root = new ArrayList<>();
//		pointsCurve2Root.add(highestPointRoot);
//		pointsCurve2Root.add(new double[] {highestPointFuselageB.x, highestPointFuselageB.y, highestPointFuselageB.z});
//		
//		List<double[]> pointsCurve1Second = new ArrayList<>();
//		pointsCurve1Second.add(new double[] {pointASecond.x, pointASecond.y, pointASecond.z});
//		pointsCurve1Second.add(highestPointSecond);
//		List<double[]> pointsCurve2Second = new ArrayList<>();
//		pointsCurve2Second.add(highestPointSecond);
//		pointsCurve2Second.add(new double[] {pointBSecond.x, pointBSecond.y, pointBSecond.z});
	
//		CADGeomCurve3D curve1Root = OCCUtils.theFactory.newCurve3D(
//				pointsCurve1Root, 
//				false, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				false
//				);		
//		CADGeomCurve3D curve2Root = OCCUtils.theFactory.newCurve3D(
//				pointsCurve2Root, 
//				false, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				false
//				);
		
//		CADGeomCurve3D curve1Second = OCCUtils.theFactory.newCurve3D(
//				pointsCurve1Second, 
//				false, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				false
//				);		
//		CADGeomCurve3D curve2Second = OCCUtils.theFactory.newCurve3D(
//				pointsCurve2Second, 
//				false, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				new double[] {horTangent.x, horTangent.y, horTangent.z}, 
//				false
//				);
		
//		CADGeomCurve3D rootAirfoilCurve = OCCUtils.theFactory.newCurve3D(rootAirfoil, false);
//		CADGeomCurve3D secondAirfoilCurve = OCCUtils.theFactory.newCurve3D(secondAirfoil, false);
		
//		List<double[]> pointsCurveDARoot = new ArrayList<>();
//		pointsCurveDARoot.add(new double[] {pointDRoot.x, pointDRoot.y, pointDRoot.z});
//		pointsCurveDARoot.add(new double[] {highestPointFuselageA.x, highestPointFuselageA.y, highestPointFuselageA.z});
//		List<double[]> pointsCurveCBRoot = new ArrayList<>();
//		pointsCurveCBRoot.add(new double[] {pointCRoot.x, pointCRoot.y, pointCRoot.z});
//		pointsCurveCBRoot.add(new double[] {highestPointFuselageB.x, highestPointFuselageB.y, highestPointFuselageB.z});
//		List<double[]> pointsCurveCDRoot = new ArrayList<>();
//		pointsCurveCDRoot.add(new double[] {pointCRoot.x, pointCRoot.y, pointCRoot.z});
//		pointsCurveCDRoot.add(new double[] {pointDRoot.x, pointDRoot.y, pointDRoot.z});
		
//		List<double[]> pointsCurveDASecond = new ArrayList<>();
//		pointsCurveDASecond.add(new double[] {pointDSecond.x, pointDSecond.y, pointDSecond.z});
//		pointsCurveDASecond.add(new double[] {pointASecond.x, pointASecond.y, pointASecond.z});
//		List<double[]> pointsCurveCBSecond = new ArrayList<>();
//		pointsCurveCBSecond.add(new double[] {pointCSecond.x, pointCSecond.y, pointCSecond.z});
//		pointsCurveCBSecond.add(new double[] {pointBSecond.x, pointBSecond.y, pointBSecond.z});
//		List<double[]> pointsCurveCDSecond = new ArrayList<>();
//		pointsCurveCDSecond.add(new double[] {pointCSecond.x, pointCSecond.y, pointCSecond.z});
//		pointsCurveCDSecond.add(new double[] {pointDSecond.x, pointDSecond.y, pointDSecond.z});
		
//		CADGeomCurve3D curveDARoot = OCCUtils.theFactory.newCurve3D(pointsCurveDARoot, false);
//		CADGeomCurve3D curveCBRoot = OCCUtils.theFactory.newCurve3D(pointsCurveCBRoot, false);
//		CADGeomCurve3D curveCDRoot = OCCUtils.theFactory.newCurve3D(pointsCurveCDRoot, false);
		
//		CADGeomCurve3D curveDASecond = OCCUtils.theFactory.newCurve3D(pointsCurveDASecond, false);
//		CADGeomCurve3D curveCBSecond = OCCUtils.theFactory.newCurve3D(pointsCurveCBSecond, false);
//		CADGeomCurve3D curveCDSecond = OCCUtils.theFactory.newCurve3D(pointsCurveCDSecond, false);
		
		// Creating shell from curves
//		OCCShape upperSurfaceFront = OCCUtils.makePatchThruSections(curve1Root, curve1Second);
//		OCCShape upperSurfaceBack = OCCUtils.makePatchThruSections(curve2Root, curve2Second);
//		OCCShape frontSurface = OCCUtils.makePatchThruSections(curveDARoot, curveDASecond);
//		OCCShape backSurface = OCCUtils.makePatchThruSections(curveCBRoot, curveCBSecond);
//		OCCShape lowerSurface = OCCUtils.makePatchThruSections(curveCDRoot, curveCDSecond);
//		
//		OCCShape sideSurface = (OCCShape) OCCUtils.makeFilledFace(
//				curveDASecond,
//				curve1Second,
//				curve2Second,
//				curveCBSecond,
//				curveCDSecond
//				);
//		
//		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
//				(CADFace) OCCUtils.theFactory.newShape(upperSurfaceFront.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(upperSurfaceBack.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(frontSurface.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(backSurface.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(lowerSurface.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(sideSurface.getShape())
//				);
		
		// Creating solid from shell
//		gp_Trsf mirrorTransform = new gp_Trsf();
//		gp_Ax2 mirrorPointPlane = new gp_Ax2(
//				new gp_Pnt(0.0, 0.0, 0.0),
//				new gp_Dir(0.0, 1.0, 0.0), // Y direction normal to reflection plane XZ
//				new gp_Dir(1.0, 0.0, 0.0)
//				);
//		mirrorTransform.SetMirror(mirrorPointPlane);	
//		BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
//		mirrorBuilder.Perform(rightShell.getShape(), 1);
//		OCCShell leftShell = (OCCShell) OCCUtils.theFactory.newShape(mirrorBuilder.Shape());
//		
//		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
//		solidMaker.Add(TopoDS.ToShell(rightShell.getShape()));
//		solidMaker.Add(TopoDS.ToShell(leftShell.getShape()));
//		solidMaker.Build();
//		OCCSolid solidFairing = (OCCSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		
		// Export shapes
//		List<OCCShape> shapes = new ArrayList<>();
		
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curve1Root).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curve2Root).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curve1Second).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curve2Second).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) rootAirfoilCurve).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) secondAirfoilCurve).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curveDARoot).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curveCBRoot).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curveCDRoot).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curveDASecond).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curveCBSecond).edge());
//		shapes.add((OCCShape) ((OCCGeomCurve3D) curveCDSecond).edge());
		
//		String fileName = "Test25mds.brep";
//		
//		if (OCCUtils.write(fileName, shapes))
//			System.out.println("========== [main] Output written on file: " + fileName);
		
//		shapes.add(upperSurfaceFront);
//		shapes.add(upperSurfaceBack);
//		shapes.add(frontSurface);
//		shapes.add(backSurface);
//		shapes.add(lowerSurface);
//		shapes.add(sideSurface);
		
//		shapes.add(rightShell);
//		shapes.add(leftShell);
		
//		shapes.add(solidFairing);
		
//		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
//		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
//		
//		shapes.addAll(fuselageShapes);
//		shapes.addAll(wingShapes);
//		
//		AircraftUtils.getAircraftSolidFile(shapes, "Test25mds", FileExtension.STEP);
	}
}

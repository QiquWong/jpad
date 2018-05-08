package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import com.sun.javafx.image.impl.IntArgb.ToIntArgbPreConv;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
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
import opencascade.TopoDS;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;

public class Test25mds {

	// Wing-Fuselage fairing test
	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Importing the aircraft and needed components
		Aircraft aircraft = AircraftUtils.importAircraft(args);			
		Fuselage fuselage = aircraft.getFuselage();		
		LiftingSurface wing = aircraft.getWing();
		
		// Getting needed variables
		double wingXApex = wing.getXApexConstructionAxes().doubleValue(SI.METER);
		double wingYApex = wing.getYApexConstructionAxes().doubleValue(SI.METER);
		double wingZApex = wing.getZApexConstructionAxes().doubleValue(SI.METER);
		double fuselageXApex = fuselage.getXApexConstructionAxes().doubleValue(SI.METER);
		double fuselageYApex = fuselage.getYApexConstructionAxes().doubleValue(SI.METER);
		double fuselageZApex = fuselage.getZApexConstructionAxes().doubleValue(SI.METER);
		double fuselageCylinderWidth = fuselage.getSectionCylinderWidth().doubleValue(SI.METER);
		
		double rootChord = wing.getChordsBreakPoints().get(0).doubleValue(SI.METER);
		double secondChord = wing.getChordAtYActual(fuselageCylinderWidth/2);
		
		Airfoil baseRootAirfoil = wing.getAirfoilList().get(0);
		
		// Creating points for the fairing profile
		List<double[]> rootAirfoil = AircraftUtils.populateCoordinateList(
				wingYApex, 
				baseRootAirfoil, 
				wing
				);
		
		List<double[]> secondAirfoil = AircraftUtils.populateCoordinateList(
				wingYApex + fuselageCylinderWidth/2, 
				baseRootAirfoil, 
				wing
				);
		
		double[] highestPointRoot = rootAirfoil.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		double[] highestPointSecond = secondAirfoil.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
			
		System.out.println(Arrays.toString(highestPointRoot) + ", " + Arrays.toString(highestPointSecond));
		
		double rootAirfoilXLE = rootAirfoil.stream()
				.map(coord -> coord[0])
				.min(Comparator.comparing(x -> x)).get();
		double rootAirfoilXTE = rootAirfoil.stream()
				.map(coord -> coord[0])
				.max(Comparator.comparing(x -> x)).get();
		
		double secondAirfoilXLE = secondAirfoil.stream()
				.map(coord -> coord[0])
				.min(Comparator.comparing(x -> x)).get();
		double secondAirfoilXTE = secondAirfoil.stream()
				.map(coord -> coord[0])
				.max(Comparator.comparing(x -> x)).get();
		
		System.out.println("[" + rootAirfoilXLE + ", " + rootAirfoilXTE + "]");
		System.out.println("[" + secondAirfoilXLE + ", " + secondAirfoilXTE + "]");
		
		PVector pVectorASecond = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootAirfoilXLE - 1.5*rootChord, SI.METER)).stream()
				.max(Comparator.comparing(vec -> vec.y)).get();
		PVector pVectorBSecond = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootAirfoilXTE + 1.0*rootChord, SI.METER)).stream()
				.max(Comparator.comparing(vec -> vec.y)).get();
		double[] pointASecond = new double[] {
				pVectorASecond.x,
				pVectorASecond.y,
				pVectorASecond.z
		};
		double[] pointBSecond = new double[] {
				pVectorBSecond.x,
				pVectorBSecond.y,
				pVectorBSecond.z
		};
		
		double[] pointARoot = new double[] {
				pointASecond[0],
				fuselageYApex,
				pointASecond[2]
		};
		double[] pointBRoot = new double[] {
				pointBSecond[0],
				fuselageYApex,
				pointBSecond[2]
		};
		
		// Creating tangent vectors for the fairing profile
		double tangentARadiansAngle = 55/(57.3); 
		double tangentBRadiansAngle = -55/(57.3);
		
		double[] airfoilTangent = new double[] {1.0, 0.0, 0.0};
		double[] pointATangent = new double[] {
				Math.cos(tangentARadiansAngle), 
				0.0, 
				Math.sin(tangentARadiansAngle)
		};
		double[] pointBTangent = new double[] {
				Math.cos(tangentBRadiansAngle), 
				0.0, 
				Math.sin(tangentBRadiansAngle)
		};
		
		// Creating curves for the fairing profile
		OCCUtils.initCADShapeFactory();
		
		CADGeomCurve3D rootAirfoilCurve = OCCUtils.theFactory.newCurve3D(rootAirfoil, false);
		CADGeomCurve3D secondAirfoilCurve = OCCUtils.theFactory.newCurve3D(secondAirfoil, false);
		
		List<double[]> curve1RootPoints = new ArrayList<>();
		curve1RootPoints.add(pointARoot);
		curve1RootPoints.add(highestPointRoot);
		CADGeomCurve3D curve1Root = OCCUtils.theFactory.newCurve3D(
				curve1RootPoints, 
				false, 
				pointATangent, 
				airfoilTangent,
				false
				);	
		List<double[]> curve2RootPoints = new ArrayList<>();
		curve2RootPoints.add(highestPointRoot);
		curve2RootPoints.add(pointBRoot);
		CADGeomCurve3D curve2Root = OCCUtils.theFactory.newCurve3D(
				curve2RootPoints, 
				false, 
				airfoilTangent, 
				pointBTangent,
				false
				);
		
		List<double[]> curve1SecondPoints = new ArrayList<>();
		curve1SecondPoints.add(pointASecond);
		curve1SecondPoints.add(highestPointSecond);
		CADGeomCurve3D curve1Second = OCCUtils.theFactory.newCurve3D(
				curve1SecondPoints, 
				false, 
				pointATangent, 
				airfoilTangent,
				false
				);	
		List<double[]> curve2SecondPoints = new ArrayList<>();
		curve2SecondPoints.add(highestPointSecond);
		curve2SecondPoints.add(pointBSecond);
		CADGeomCurve3D curve2Second = OCCUtils.theFactory.newCurve3D(
				curve2SecondPoints, 
				false, 
				airfoilTangent, 
				pointBTangent,
				false
				);
		
		CADGeomCurve3D curveBARoot = OCCUtils.theFactory.newCurve3D(pointBRoot, pointARoot);
		CADGeomCurve3D curveBASecond = OCCUtils.theFactory.newCurve3D(pointBSecond, pointASecond);
		
		// Creating shell from curves
		OCCShape upperSurface1 = OCCUtils.makePatchThruSections(curve1Root, curve1Second);
		OCCShape upperSurface2 = OCCUtils.makePatchThruSections(curve2Root, curve2Second);
		OCCShape lowerSurface = OCCUtils.makePatchThruSections(curveBARoot, curveBASecond);
		OCCShape sideSurface = (OCCShape) OCCUtils.makeFilledFace(curveBASecond, curve1Second, curve2Second);
		
		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
				(CADFace) OCCUtils.theFactory.newShape(upperSurface1.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(upperSurface2.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(lowerSurface.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(sideSurface.getShape())
				);
		
		// Testing fillet capabilities
		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());
		
		TopExp_Explorer explorer = new TopExp_Explorer(rightShell.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
		while(explorer.More() > 0) {
			filletMaker.Add(rootChord/10, TopoDS.ToEdge(explorer.Current()));
			explorer.Next();
		}
		System.out.println(filletMaker.Shape().ShapeType().equals(TopAbs_ShapeEnum.TopAbs_COMPOUND));
		List<TopoDS_Shell> rightFilletedShells = new ArrayList<>();
		TopoDS_Shape rightFilletedCompound = filletMaker.Shape();
		TopExp_Explorer exp = new TopExp_Explorer(rightFilletedCompound, TopAbs_ShapeEnum.TopAbs_SHELL);
		while(exp.More() > 0) {
			rightFilletedShells.add(TopoDS.ToShell(exp.Current()));
			exp.Next();
		}
		System.out.println(rightFilletedShells.size());
		OCCShell rightShellFilleted = (OCCShell) OCCUtils.theFactory.newShape(rightFilletedShells.get(0));
		
		// Creating the solid	
		gp_Trsf mirrorTransform = new gp_Trsf();
		gp_Ax2 mirrorPointPlane = new gp_Ax2(
				new gp_Pnt(0.0, 0.0, 0.0),
				new gp_Dir(0.0, 1.0, 0.0),
				new gp_Dir(1.0, 0.0, 0.0)
				);
		mirrorTransform.SetMirror(mirrorPointPlane);	
		BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
//		mirrorBuilder.Perform(rightShell.getShape(), 1);
		mirrorBuilder.Perform(rightShellFilleted.getShape(), 1);
		OCCShell leftShell = (OCCShell) OCCUtils.theFactory.newShape(mirrorBuilder.Shape());
		
		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
//		solidMaker.Add(TopoDS.ToShell(rightShell.getShape()));
		solidMaker.Add(TopoDS.ToShell(rightShellFilleted.getShape()));
		solidMaker.Add(TopoDS.ToShell(leftShell.getShape()));
		solidMaker.Build();
		OCCSolid solidFairing = (OCCSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
		
		// Export shapes
		List<OCCShape> shapes = new ArrayList<>();
		
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve1Root).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve2Root).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve1Second).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) curve2Second).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) rootAirfoilCurve).edge());
//		shapes.add((OCCEdge) ((OCCGeomCurve3D) secondAirfoilCurve).edge());
		
		shapes.add(solidFairing);
		
//		shapes.add(rightShellFilleted);
		
//		String fileName = "Test25mds.brep";
//		
//		if (OCCUtils.write(fileName, shapes))
//			System.out.println("========== [main] Output written on file: " + fileName);
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		
		shapes.addAll(fuselageShapes);
		shapes.addAll(wingShapes);
		
		AircraftUtils.getAircraftSolidFile(shapes, "Test25mds", FileExtension.STEP);
		
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

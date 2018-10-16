package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
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
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Shell;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test28mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface canard = aircraft.getCanard();
		LiftingSurface horTail = aircraft.getHTail();
		LiftingSurface verTail = aircraft.getVTail();
		
		// Geometric data collection
		double[] wingApex = new double[] {
				wing.getXApexConstructionAxes().doubleValue(SI.METER),
				wing.getYApexConstructionAxes().doubleValue(SI.METER),
				wing.getZApexConstructionAxes().doubleValue(SI.METER)
		};
		
		double[] canardApex = new double[] {
				canard.getXApexConstructionAxes().doubleValue(SI.METER),
				canard.getYApexConstructionAxes().doubleValue(SI.METER),
				canard.getZApexConstructionAxes().doubleValue(SI.METER)
		};
		
		Airfoil wingBaseAirfoil = wing.getAirfoilList().get(0);
		Airfoil canardBaseAirfoil = canard.getAirfoilList().get(0);
		
		double fuselageWidthAtWing = fuselage.getWidthAtX(wingApex[0])*0.5;
		double fuselageWidthAtCanard = fuselage.getWidthAtX(canardApex[0])*0.5;
		
		List<double[]> wingRootAirfoilPnts = AircraftUtils.populateCoordinateList(
				wingApex[1], 
				wingBaseAirfoil, 
				wing
				);
		List<double[]> wingSideAirfoilPnts = AircraftUtils.populateCoordinateList(
				fuselageWidthAtWing, 
				wingBaseAirfoil, 
				wing
				);
		List<double[]> canardRootAirfoilPnts = AircraftUtils.populateCoordinateList(
				canardApex[1], 
				canardBaseAirfoil, 
				canard
				);
		List<double[]> canardSideAirfoilPnts = AircraftUtils.populateCoordinateList(
				fuselageWidthAtCanard, 
				canardBaseAirfoil, 
				canard
				);
		
		AttachmentType wingAttType = AttachmentType.getAttachmentType(
				wingRootAirfoilPnts, wingSideAirfoilPnts, fuselage);
		AttachmentType canardAttType = AttachmentType.getAttachmentType(
				canardRootAirfoilPnts, canardSideAirfoilPnts, fuselage);
		
		System.out.println("---------------------------------------------------------------------------------------");
		System.out.println("Wing attachment type: " + wingAttType.name());
		System.out.println("Canard attachment type: " + canardAttType.name());
		System.out.println("---------------------------------------------------------------------------------------");
		
		// Generate fairing
		List<OCCShape> wingFairing = generateFairing(
				wingAttType, 
				fuselage, 
				wing
				);
		
		List<OCCShape> canardFairing = generateFairing(
				canardAttType, 
				fuselage, 
				canard
				);
		
		// Export shapes to CAD file
		List<OCCShape> exportShapes = new ArrayList<>();
		
		exportShapes.addAll(wingFairing);
		exportShapes.addAll(canardFairing);
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 10, 7, true, true, false);
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(canard, ComponentEnum.CANARD, 1e-3, false, true, false);
		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false);
		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false);
		
		exportShapes.addAll(fuselageShapes);
		exportShapes.addAll(wingShapes);
		exportShapes.addAll(canardShapes);
		exportShapes.addAll(horTailShapes);
		exportShapes.addAll(verTailShapes);
		
//		String fileName = "Test28mds.brep";
//		String fileName = "CS300.brep";
//		String fileName = "CS300.step";
		
		String fileName = aircraft.getId() + ".step";
		
		AircraftUtils.getAircraftSolidFile(exportShapes, fileName, FileExtension.STEP);
		
		if (OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}
	
	public static List<OCCShape> generateFairing(
			AttachmentType attType,
			Fuselage fuselage,
			LiftingSurface wing) {
		
		List<OCCShape> returnShapes = new ArrayList<>();
		
		switch(attType) {
		
		case DETACHED_UP:
			returnShapes.addAll(
					generateDetachedUpFairingShapes(
							fuselage,
							wing
							)
					);
			break;
			
		case ATTACHED_UP:
			break;
			
		case MIDDLE:
			break;
			
		case ATTACHED_DOWN:
			returnShapes.addAll(
					generateAttachedDownFairingShapes(
							fuselage, 
							wing
							)
					);		
			break;
			
		case DETACHED_DOWN:
			break;
		}
		
		return returnShapes;
	}
	
	public static List<OCCShape> generateDetachedUpFairingShapes(
			Fuselage fuselage, 
			LiftingSurface wing) {
		
		List<OCCShape> shapes = new ArrayList<>();
		
		// Fairing parameters definition
		double frontLengthFactor = 1.25;
		double backLengthFactor = 1.25;
		double sideSizeFactor = 0.40;              // This needs to be less than one, otherwise it will exceed fuselage diameter		
		double heightAboveAirfoilTopFactor = 0.10;
		double heightBelowContactFactor = 0.10;    // Again, needs to be less than one, in order to not exceed fuselage limits
		double heightAboveContactFactor = 0.80;    // """"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
		double filletRadiusFactor = 0.40;
		
		// Geometric data collection
		Airfoil genAirfoil = wing.getAirfoilList().get(0);
		
		double rootYCoord = wing.getYApexConstructionAxes().doubleValue(SI.METER);	
		double rootChord = wing.getChordAtYActual(rootYCoord);
		double rootThickness = genAirfoil.getThicknessToChordRatio()*rootChord;
		
		List<double[]> rootAirfoilPnts = AircraftUtils.populateCoordinateList(
				rootYCoord, 
				genAirfoil, 
				wing
				);
		
		double[] rootTopPnt = rootAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		double[] rootLeadingEdge = rootAirfoilPnts.stream()
				.min(Comparator.comparing(coord -> coord[0])).get();
		double[] rootTrailingEdge = rootAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[0])).get();
		
		List<PVector> fuselageSideCurveMiddle = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootTopPnt[0], SI.METER));	
		List<PVector> fuselageSideCurveFront = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootLeadingEdge[0] - frontLengthFactor*rootChord, SI.METER));
		List<PVector> fuselageSideCurveBack = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootTrailingEdge[0] + backLengthFactor*rootChord, SI.METER));
		
		double[] fuselageSCFrontTopPnt = new double[] {
				fuselageSideCurveFront.get(0).x,
				fuselageSideCurveFront.get(0).y,
				fuselageSideCurveFront.get(0).z
		};
		double[] fuselageSCFrontBottomPnt = new double[] {
				fuselageSideCurveFront.get(fuselageSideCurveFront.size()-1).x,
				fuselageSideCurveFront.get(fuselageSideCurveFront.size()-1).y,
				fuselageSideCurveFront.get(fuselageSideCurveFront.size()-1).z
		};
		double[] fuselageSCBackTopPnt = new double[] {
				fuselageSideCurveBack.get(0).x,
				fuselageSideCurveBack.get(0).y,
				fuselageSideCurveBack.get(0).z
		};
		double[] fuselageSCBackBottomPnt = new double[] {
				fuselageSideCurveBack.get(fuselageSideCurveBack.size()-1).x,
				fuselageSideCurveBack.get(fuselageSideCurveBack.size()-1).y,
				fuselageSideCurveBack.get(fuselageSideCurveBack.size()-1).z
		};
		
		double fusWidthAtMiddle = fuselage.getWidthAtX(rootTopPnt[0])*.5;
		double fusCamberZAtMiddle = fuselage.getCamberZAtX(rootTopPnt[0]);
		
		List<Double> fuselageSCMiddleUpperZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleUpperYCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerYCoords = new ArrayList<>();
		
		fuselageSCMiddleLowerZCoords.add(fusCamberZAtMiddle);
		fuselageSCMiddleLowerYCoords.add(fusWidthAtMiddle);
		
		fuselageSCMiddleUpperZCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z > fusCamberZAtMiddle)
				.map(pv -> (double) pv.z)
				.collect(Collectors.toList()));
		fuselageSCMiddleUpperYCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z > fusCamberZAtMiddle)
				.map(pv -> (double) pv.y)
				.collect(Collectors.toList()));
		fuselageSCMiddleLowerZCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z < fusCamberZAtMiddle)
				.map(pv -> (double) pv.z)
				.collect(Collectors.toList()));
		fuselageSCMiddleLowerYCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z < fusCamberZAtMiddle)
				.map(pv -> (double) pv.y)
				.collect(Collectors.toList()));
		
		fuselageSCMiddleUpperZCoords.add(fusCamberZAtMiddle);
		fuselageSCMiddleUpperYCoords.add(fusWidthAtMiddle);
		
		Collections.reverse(fuselageSCMiddleLowerZCoords);
		Collections.reverse(fuselageSCMiddleLowerYCoords);
		
		double fairingWidth = fusWidthAtMiddle*sideSizeFactor;
		
		List<double[]> sideAirfoilPoints = AircraftUtils.populateCoordinateList(
				fairingWidth, 
				genAirfoil, 
				wing
				);
		
		double[] sideTopPnt = sideAirfoilPoints.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		
		double[] fuselageUppContactPnt = new double[] {
				rootTopPnt[0],
				fairingWidth,
				MyMathUtils.getInterpolatedValue1DSpline(
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleUpperYCoords), 
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleUpperZCoords), 
						fairingWidth
						)
		};
		double[] fuselageLowContactPnt = new double[] {
				rootTopPnt[0],
				fairingWidth,
				MyMathUtils.getInterpolatedValue1DSpline(
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerYCoords), 
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerZCoords), 
						fairingWidth
						)
		};
		
		double fairingMinimumZ = MyArrayUtils.getMax(new double[] {
				fuselageSCFrontBottomPnt[2],
				fuselageLowContactPnt[2],
				fuselageSCBackBottomPnt[2]
		});
		double fuselageMaximumZ = Math.min(
				fuselageSCFrontTopPnt[2], 
				fuselageSCBackTopPnt[2]
				);
		double fairingMaximumZ = Math.max(rootTopPnt[2], sideTopPnt[2]) + rootThickness*heightAboveAirfoilTopFactor;
		
		// Create points for curve sketching	
		double[] pntG = new double[] {
				rootLeadingEdge[0] - frontLengthFactor*rootChord,
				rootLeadingEdge[1],
				fuselageUppContactPnt[2] - (fuselageUppContactPnt[2] - fairingMinimumZ)*heightBelowContactFactor
		};
		
		double[] pntA = new double[] {
				rootLeadingEdge[0] - frontLengthFactor*rootChord,
				rootLeadingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*heightAboveContactFactor
		};
		
		double[] pntB = new double[] {
				rootLeadingEdge[0],
				rootLeadingEdge[1],
				fairingMaximumZ - (fairingMaximumZ - rootLeadingEdge[2])*0.15
		};
		
		double[] pntC = new double[] {
				rootTopPnt[0],
				rootTopPnt[1],
				fairingMaximumZ
		};
		
		double[] pntD = new double[] {
				rootTrailingEdge[0],
				rootTrailingEdge[1],
				fairingMaximumZ - (fairingMaximumZ - rootTrailingEdge[2])*0.75
		};
		
		double[] pntE = new double[] {
				rootTrailingEdge[0] + backLengthFactor*rootChord,
				rootTrailingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*heightAboveContactFactor
		};
		
		double[] pntF = new double[] {
				rootTrailingEdge[0] + backLengthFactor*rootChord,
				rootTrailingEdge[1],
				fuselageUppContactPnt[2] - (fuselageUppContactPnt[2] - fairingMinimumZ)*heightBelowContactFactor
		};
		
		double[] pntI = new double[] {
				rootLeadingEdge[0],
				fuselageUppContactPnt[1] - (fuselageUppContactPnt[1] - rootLeadingEdge[1])*0.05,
				pntA[2]
		};
		
		double[] pntL = new double[] {
				rootTopPnt[0],
				fuselageUppContactPnt[1],
				pntA[2]
		};
		
		double[] pntM = new double[] {
				rootTrailingEdge[0],
				fuselageUppContactPnt[1] - (fuselageUppContactPnt[1] - rootTrailingEdge[1])*0.05,
				pntA[2]
		};
		
		// Create sketching curves
		OCCUtils.initCADShapeFactory();
		
		CADGeomCurve3D mainCurve = OCCUtils.theFactory.newCurve3D(false, pntA, pntB, pntC, pntD, pntE);
		CADGeomCurve3D sideCurve = OCCUtils.theFactory.newCurve3D(false, pntA, pntI, pntL, pntM, pntE);
		
		// Create curves to patch through
		int nMain = 15; // number of discretization points for the main curve
		mainCurve.discretize(nMain);
		
		List<double[]> mainCurvePnts = ((OCCGeomCurve3D) mainCurve).getDiscretizedCurve().getPoints().stream()
				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
				.collect(Collectors.toList());
		
		int nSide = 25; // number of interpolation points
		sideCurve.discretize(nSide);
		
		List<Double> sideCurveXCoordsD = new ArrayList<>();
		List<Double> sideCurveYCoordsD = new ArrayList<>();
		
		((OCCGeomCurve3D) sideCurve).getDiscretizedCurve().getPoints()
				.forEach(gp -> {
					sideCurveXCoordsD.add(gp.X());
					sideCurveYCoordsD.add(gp.Y());
				});
		
		double[] sideCurveXCoords = MyArrayUtils.convertToDoublePrimitive(sideCurveXCoordsD);
		double[] sideCurveYCoords = MyArrayUtils.convertToDoublePrimitive(sideCurveYCoordsD);
		
		List<CADGeomCurve3D> mainSegments = new ArrayList<>();
		List<CADGeomCurve3D> bottomSegments = new ArrayList<>();
		
		for(int i = 1; i < nMain-1; i++) {
			double[] mainPnt = mainCurvePnts.get(i);
			double[] bottomPnt = new double[] {mainPnt[0], pntG[1], pntG[2]};
			
			double ySideCoord = MyMathUtils.getInterpolatedValue1DLinear(
					sideCurveXCoords, 
					sideCurveYCoords, 
					mainPnt[0]
					);
			
			double[] mainSidePnt = new double[] {
					mainPnt[0],
					ySideCoord,
					mainPnt[2]
			};
			mainSegments.add(OCCUtils.theFactory.newCurve3D(mainPnt, mainSidePnt));
			
			double[] bottomSidePnt = new double[] {
					mainPnt[0],
					ySideCoord,
					pntG[2]
			};
			bottomSegments.add(OCCUtils.theFactory.newCurve3D(bottomPnt, bottomSidePnt));
		}
		
		// Create patches
		OCCShape upperPatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newVertex(pntA[0], pntA[1], pntA[2]), 
				mainSegments, 
				OCCUtils.theFactory.newVertex(pntE[0], pntE[1], pntE[2])
				);	
		
		OCCShape lowerPatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newVertex(pntG[0], pntG[1], pntG[2]), 
				bottomSegments, 
				OCCUtils.theFactory.newVertex(pntF[0], pntF[1], pntF[2])
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
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(lowerEdges.get(1)))
				);
		
		// Create a shell from adjacent patches
		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
				(CADFace) OCCUtils.theFactory.newShape(upperPatch.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(sidePatch.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(lowerPatch.getShape())
				);

		// Apply a fillet to specific edges
		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());
		double filletRadius = (pntA[2] - pntG[2])*filletRadiusFactor;

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
		
		// Export shapes
		shapes.add(solidFairing);
		
		return shapes;
	}
	
	public static List<OCCShape> generateAttachedDownFairingShapes(
			Fuselage fuselage,
			LiftingSurface wing
			) {
		
		List<OCCShape> shapes = new ArrayList<>();
		
		// Fairing parameters definition
		double frontLengthFactor = 0.60;
		double backLengthFactor = 0.70;
		double sideSizeFactor = 0.95;              // This needs to be less than one, otherwise fairing will exceed fuselage diameter		
		double heightBelowFuselageFactor = 0.25;
		double heightBelowContactFactor = 0.95;    // Again, needs to be less than one, in order to not exceed fuselage limits
		double heightAboveContactFactor = 0.10;    // """"""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""""
		double filletRadiusFactor = 0.50;
		
		// Geometric data collection
		Airfoil genAirfoil = wing.getAirfoilList().get(0);
		
		double rootYCoord = wing.getYApexConstructionAxes().doubleValue(SI.METER);	
		double rootChord = wing.getChordAtYActual(rootYCoord);
		double rootThickness = genAirfoil.getThicknessToChordRatio()*rootChord;
		
		List<double[]> rootAirfoilPnts = AircraftUtils.populateCoordinateList(
				rootYCoord, 
				genAirfoil, 
				wing
				);
		
		double[] rootTopPnt = rootAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		double[] rootLeadingEdge = rootAirfoilPnts.stream()
				.min(Comparator.comparing(coord -> coord[0])).get();
		double[] rootTrailingEdge = rootAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[0])).get();
		
		List<PVector> fuselageSideCurveMiddle = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootTopPnt[0], SI.METER));	
		List<PVector> fuselageSideCurveFront = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootLeadingEdge[0] - frontLengthFactor*rootChord, SI.METER));
		List<PVector> fuselageSideCurveBack = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootTrailingEdge[0] + backLengthFactor*rootChord, SI.METER));
		
		double[] fuselageSCFrontTopPnt = new double[] {
				fuselageSideCurveFront.get(0).x,
				fuselageSideCurveFront.get(0).y,
				fuselageSideCurveFront.get(0).z
		};
		double[] fuselageSCFrontBottomPnt = new double[] {
				fuselageSideCurveFront.get(fuselageSideCurveFront.size()-1).x,
				fuselageSideCurveFront.get(fuselageSideCurveFront.size()-1).y,
				fuselageSideCurveFront.get(fuselageSideCurveFront.size()-1).z
		};
		double[] fuselageSCBackTopPnt = new double[] {
				fuselageSideCurveBack.get(0).x,
				fuselageSideCurveBack.get(0).y,
				fuselageSideCurveBack.get(0).z
		};
		double[] fuselageSCBackBottomPnt = new double[] {
				fuselageSideCurveBack.get(fuselageSideCurveBack.size()-1).x,
				fuselageSideCurveBack.get(fuselageSideCurveBack.size()-1).y,
				fuselageSideCurveBack.get(fuselageSideCurveBack.size()-1).z
		};
		
		double fusWidthAtMiddle = fuselage.getWidthAtX(rootTopPnt[0])*.5;
		double fusCamberZAtMiddle = fuselage.getCamberZAtX(rootTopPnt[0]);
		
		double[] sideTopPnt = AircraftUtils.populateCoordinateList(
				fusWidthAtMiddle, 
				genAirfoil, 
				wing
				).stream()
				 .max(Comparator.comparing(coord -> coord[2])).get();
		
		List<Double> fuselageSCMiddleUpperZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleUpperYCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerYCoords = new ArrayList<>();
		
		fuselageSCMiddleLowerZCoords.add(fusCamberZAtMiddle);
		fuselageSCMiddleLowerYCoords.add(fusWidthAtMiddle);
		
		fuselageSCMiddleUpperZCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z > fusCamberZAtMiddle)
				.map(pv -> (double) pv.z)
				.collect(Collectors.toList()));
		fuselageSCMiddleUpperYCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z > fusCamberZAtMiddle)
				.map(pv -> (double) pv.y)
				.collect(Collectors.toList()));
		fuselageSCMiddleLowerZCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z < fusCamberZAtMiddle)
				.map(pv -> (double) pv.z)
				.collect(Collectors.toList()));
		fuselageSCMiddleLowerYCoords.addAll(fuselageSideCurveMiddle.stream()
				.filter(pv -> pv.z < fusCamberZAtMiddle)
				.map(pv -> (double) pv.y)
				.collect(Collectors.toList()));
		
		fuselageSCMiddleUpperZCoords.add(fusCamberZAtMiddle);
		fuselageSCMiddleUpperYCoords.add(fusWidthAtMiddle);
		
		Collections.reverse(fuselageSCMiddleLowerZCoords);
		Collections.reverse(fuselageSCMiddleLowerYCoords);
		
		double[] fusWingContactPnt = new double[] {
				sideTopPnt[0],
				MyMathUtils.getInterpolatedValue1DSpline(
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerZCoords), 
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerYCoords), 
						sideTopPnt[2]
						),
				sideTopPnt[2]
		};
		
		double fairingWidth = fusWingContactPnt[1] + (fusWidthAtMiddle - fusWingContactPnt[1])*sideSizeFactor;
		
		double[] fuselageLowContactPnt = new double[] {
				sideTopPnt[0],
				fairingWidth,
				MyMathUtils.getInterpolatedValue1DSpline(
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerYCoords), 
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerZCoords), 
						fairingWidth
						)
		};
		double[] fuselageUppContactPnt = new double[] {
				sideTopPnt[0],
				fairingWidth,
				MyMathUtils.getInterpolatedValue1DSpline(
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleUpperYCoords), 
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleUpperZCoords), 
						fairingWidth
						)
		};
		
		double fairingMaximumZ = MyArrayUtils.getMin(new double[] {
				fuselageSCFrontTopPnt[2],
				fuselageUppContactPnt[2],
				fuselageSCBackTopPnt[2]
		});
		double fuselageMinimumZ = Math.max(
				fuselageSCFrontBottomPnt[2], 
				fuselageSCBackBottomPnt[2]
				);
		double fairingMinimumZ = Math.min(fuselageSCFrontBottomPnt[2], fuselageSCBackBottomPnt[2]) - 
				rootThickness*heightBelowFuselageFactor;
		
		// Create points for curve sketching
		double[] pntG = new double[] {
				rootLeadingEdge[0] - rootChord*frontLengthFactor,
				rootLeadingEdge[1],
				fuselageLowContactPnt[2] + (fairingMaximumZ - fuselageLowContactPnt[2])*heightAboveContactFactor
		};
		
		double[] pntA = new double[] {
				rootLeadingEdge[0] - rootChord*frontLengthFactor,
				rootLeadingEdge[1],
				fuselageLowContactPnt[2] - (fuselageLowContactPnt[2] - fuselageMinimumZ)*heightBelowContactFactor
		};
		
		double[] pntB = new double[] {
				rootLeadingEdge[0],
				rootLeadingEdge[1],
				fairingMinimumZ + (fuselageMinimumZ - fairingMinimumZ)*0.15
		};
		
		double[] pntC = new double[] {
				rootTopPnt[0],
				rootTopPnt[1],
				fairingMinimumZ
		};
		
		double[] pntD = new double[] {
				rootTrailingEdge[0],
				rootTrailingEdge[1],
				fairingMinimumZ + (fuselageMinimumZ - fairingMinimumZ)*0.15
		};
		
		double[] pntE = new double[] {
				rootTrailingEdge[0] + rootChord*backLengthFactor,
				rootTrailingEdge[1],
				fuselageLowContactPnt[2] - (fuselageLowContactPnt[2] - fuselageMinimumZ)*heightBelowContactFactor
		};
		
		double[] pntF = new double[] {
				rootTrailingEdge[0] + rootChord*backLengthFactor,
				rootTrailingEdge[1],
				fuselageLowContactPnt[2] + (fairingMaximumZ - fuselageLowContactPnt[2])*heightAboveContactFactor
		};
		
		double[] pntI = new double[] {
				rootLeadingEdge[0],
				fuselageLowContactPnt[1] - (fuselageLowContactPnt[1] - rootLeadingEdge[1])*0.10,
				pntA[2]
		};
		
		double[] pntL = new double[] {
				rootTopPnt[0],
				fuselageLowContactPnt[1] - (fuselageLowContactPnt[1] - rootTopPnt[1])*0.05,
				pntA[2]
		};
		
		double[] pntM = new double[] {
				rootTrailingEdge[0],
				fuselageLowContactPnt[1] - (fuselageLowContactPnt[1] - rootTrailingEdge[1])*0.1,
				pntA[2]
		};
		
		// Create sketching curves
		OCCUtils.initCADShapeFactory();

		CADGeomCurve3D mainCurve = OCCUtils.theFactory.newCurve3D(false, pntA, pntB, pntC, pntD, pntE);
		CADGeomCurve3D sideCurve = OCCUtils.theFactory.newCurve3D(false, pntA, pntI, pntL, pntM, pntE);

		// Create curves to patch through
		int nMain = 15; // number of discretization points for the main curve
		mainCurve.discretize(nMain);

		List<double[]> mainCurvePnts = ((OCCGeomCurve3D) mainCurve).getDiscretizedCurve().getPoints().stream()
				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
				.collect(Collectors.toList());

		int nSide = 25; // number of interpolation points
		sideCurve.discretize(nSide);

		List<Double> sideCurveXCoordsD = new ArrayList<>();
		List<Double> sideCurveYCoordsD = new ArrayList<>();

		((OCCGeomCurve3D) sideCurve).getDiscretizedCurve().getPoints()
		.forEach(gp -> {
			sideCurveXCoordsD.add(gp.X());
			sideCurveYCoordsD.add(gp.Y());
		});

		double[] sideCurveXCoords = MyArrayUtils.convertToDoublePrimitive(sideCurveXCoordsD);
		double[] sideCurveYCoords = MyArrayUtils.convertToDoublePrimitive(sideCurveYCoordsD);

		List<CADGeomCurve3D> mainSegments = new ArrayList<>();
		List<CADGeomCurve3D> bottomSegments = new ArrayList<>();

		for(int i = 1; i < nMain-1; i++) {
			double[] mainPnt = mainCurvePnts.get(i);
			double[] bottomPnt = new double[] {mainPnt[0], pntG[1], pntG[2]};

			double ySideCoord = MyMathUtils.getInterpolatedValue1DLinear(
					sideCurveXCoords, 
					sideCurveYCoords, 
					mainPnt[0]
					);

			double[] mainSidePnt = new double[] {
					mainPnt[0],
					ySideCoord,
					mainPnt[2]
			};
			mainSegments.add(OCCUtils.theFactory.newCurve3D(mainPnt, mainSidePnt));

			double[] bottomSidePnt = new double[] {
					mainPnt[0],
					ySideCoord,
					pntG[2]
			};
			bottomSegments.add(OCCUtils.theFactory.newCurve3D(bottomPnt, bottomSidePnt));
		}
		
		// Create patches
		OCCShape upperPatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newVertex(pntA[0], pntA[1], pntA[2]), 
				mainSegments, 
				OCCUtils.theFactory.newVertex(pntE[0], pntE[1], pntE[2])
				);	

		OCCShape lowerPatch = OCCUtils.makePatchThruSections(
				OCCUtils.theFactory.newVertex(pntG[0], pntG[1], pntG[2]), 
				bottomSegments, 
				OCCUtils.theFactory.newVertex(pntF[0], pntF[1], pntF[2])
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
				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(lowerEdges.get(1)))
				);

		// Create a shell from adjacent patches
		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
				(CADFace) OCCUtils.theFactory.newShape(upperPatch.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(sidePatch.getShape()),
				(CADFace) OCCUtils.theFactory.newShape(lowerPatch.getShape())
				);
		
		// Apply a fillet to specific edges
		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());
		double filletRadius = (pntG[2] - pntA[2])*filletRadiusFactor;

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
				
		// Export shapes
		shapes.add(solidFairing);
		
		return shapes;
	}

	public enum AttachmentType {
		
		DETACHED_UP,
		DETACHED_DOWN,	
		ATTACHED_UP,	
		ATTACHED_DOWN, 	
		MIDDLE;
		
		public static AttachmentType getAttachmentType(
				List<double[]> rootAirfoilPoints,
				List<double[]> sideAirfoilPoints,
				Fuselage fuselage) {
			
			double[] rootAirfoilTopPoint = rootAirfoilPoints.stream()
					.max(Comparator.comparing(pnt -> pnt[2])).get();
			double[] sideAirfoilTopPoint = sideAirfoilPoints.stream()
					.max(Comparator.comparing(pnt -> pnt[2])).get();
			double[] rootAirfoilBottomPoint = rootAirfoilPoints.stream()
					.min(Comparator.comparing(pnt -> pnt[2])).get();
			double[] sideAirfoilBottomPoint = sideAirfoilPoints.stream()
					.min(Comparator.comparing(pnt -> pnt[2])).get();
			
			List<PVector> fuselageSideCurveAtTopX = fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf(sideAirfoilTopPoint[0], SI.METER));
			List<PVector> fuselageSideCurveAtBottomX = fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf(sideAirfoilBottomPoint[0], SI.METER));
			
			double fuselageZTopAtTopX = fuselageSideCurveAtTopX.get(0).z;
			double fuselageCamberZAtTopX = fuselage.getCamberZAtX(sideAirfoilTopPoint[0]);
			double fuselageCamberZAtBottomX = fuselage.getCamberZAtX(sideAirfoilBottomPoint[0]);
			double fuselageZBottomAtBottomX = fuselageSideCurveAtBottomX.get(fuselageSideCurveAtBottomX.size()-1).z;
			
			if(rootAirfoilTopPoint[2] > fuselageZTopAtTopX) {
				return DETACHED_UP;
				
			} else if(sideAirfoilTopPoint[2] < fuselageZTopAtTopX && sideAirfoilBottomPoint[2] > fuselageCamberZAtBottomX) {
				return ATTACHED_UP;
			}
			
			if(rootAirfoilBottomPoint[2] < fuselageZBottomAtBottomX) {
				return DETACHED_DOWN;
				
			} else if(sideAirfoilTopPoint[2] < fuselageCamberZAtTopX && sideAirfoilBottomPoint[2] > fuselageZBottomAtBottomX) {
				return ATTACHED_DOWN;
			}		
			
			return MIDDLE;
		}
	}
}

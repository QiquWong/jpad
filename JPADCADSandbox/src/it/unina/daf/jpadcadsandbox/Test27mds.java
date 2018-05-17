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
import it.unina.daf.jpadcadsandbox.Test28mds.AttachmentType;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepAlgoAPI_Section;
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
		
		// Get fairing shapes
		List<OCCShape> canardFairingShapes = getFairingShapes(fuselage, canard,
				1.00, 1.50, 0.40, 0.20, 0.10, 1.00, 0.40);	
		List<OCCShape> wingFairingShapes = getFairingShapes(fuselage, wing,
				1.00, 1.00, 0.90, 0.10, 0.70, 0.10, 0.50);
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
//		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
//		List<OCCShape> canardShapes = AircraftUtils.getLiftingSurfaceCAD(canard, ComponentEnum.CANARD, 1e-3, false, true, false);
//		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false);
//		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false);
		
		BRepAlgoAPI_Section sectionMaker = new BRepAlgoAPI_Section();
		sectionMaker.Init1(fuselageShapes.get(0).getShape());
		sectionMaker.Init2(canardFairingShapes.get(0).getShape());
		sectionMaker.Build();
		OCCShape intersection = (OCCShape) OCCUtils.theFactory.newShape(sectionMaker.Shape());
		
		// Generate CAD file
		String fileName = "fairingTest.brep";
		
		List<OCCShape> exportShapes = new ArrayList<>();
		exportShapes.addAll(fuselageShapes);
//		exportShapes.addAll(wingShapes);
//		exportShapes.addAll(canardShapes);
//		exportShapes.addAll(horTailShapes);
//		exportShapes.addAll(verTailShapes);
//		exportShapes.addAll(wingFairingShapes);
//		exportShapes.addAll(canardFairingShapes);
		exportShapes.add(intersection);

		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);
	}
	
	public static List<OCCShape> getFairingShapes(
			Fuselage fuselage, 
			LiftingSurface wing,
			double frontLengthFactor,
			double backLengthFactor,
			double sideSizeFactor,
			double fairingHeightFactor,
			double heightBelowContactFactor,
			double heightAboveContactFactor,
			double filletRadiusFactor
			) {
		List<OCCShape> fairingShapes = new ArrayList<>();
		
		// Geometric data collection
		Airfoil genAirfoil = wing.getAirfoilList().get(0);
		
		double rootYCoord = wing.getYApexConstructionAxes().doubleValue(SI.METER);	
		double rootChord = wing.getChordAtYActual(rootYCoord);
		double rootThickness = genAirfoil.getThicknessToChordRatio()*rootChord;
		
		double fairingFrontLength = frontLengthFactor*rootChord;
		double fairingBackLength = backLengthFactor*rootChord;
		
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
		
		double fusWidthAtMiddle = fuselage.getWidthAtX(rootTopPnt[0])*.5;
		double fusCamberZAtMiddle = fuselage.getCamberZAtX(rootTopPnt[0]);
		
		List<double[]> tipAirfoilPnts = AircraftUtils.populateCoordinateList(
				fusWidthAtMiddle, 
				genAirfoil, 
				wing
				);
		
		double[] tipTopPnt = tipAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		
		List<PVector> fuselageSideCurveMiddle = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootTopPnt[0], SI.METER));	
		List<PVector> fuselageSideCurveFront = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootLeadingEdge[0] - fairingFrontLength, SI.METER));
		List<PVector> fuselageSideCurveBack = fuselage.getUniqueValuesYZSideRCurve(
				Amount.valueOf(rootTrailingEdge[0] + fairingBackLength, SI.METER));
		
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
		
		List<Double> fuselageSCMiddleUpperZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleUpperYCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerZCoords = new ArrayList<>();
		List<Double> fuselageSCMiddleLowerYCoords = new ArrayList<>();
		
		fuselageSCMiddleLowerZCoords.add(fusCamberZAtMiddle);
		fuselageSCMiddleLowerYCoords.add(fusWidthAtMiddle);
		
//		fuselageSCMiddleUpperZCoords.addAll(fuselageSideCurveMiddle.stream()
//				.filter(pv -> pv.z > fusCamberZAtMiddle)
//				.map(pv -> (double) pv.z)
//				.collect(Collectors.toList()));
//		fuselageSCMiddleUpperYCoords.addAll(fuselageSideCurveMiddle.stream()
//				.filter(pv -> pv.z > fusCamberZAtMiddle)
//				.map(pv -> (double) pv.y)
//				.collect(Collectors.toList()));
//		fuselageSCMiddleLowerZCoords.addAll(fuselageSideCurveMiddle.stream()
//				.filter(pv -> pv.z < fusCamberZAtMiddle)
//				.map(pv -> (double) pv.z)
//				.collect(Collectors.toList()));
//		fuselageSCMiddleLowerYCoords.addAll(fuselageSideCurveMiddle.stream()
//				.filter(pv -> pv.z < fusCamberZAtMiddle)
//				.map(pv -> (double) pv.y)
//				.collect(Collectors.toList()));
		
		for(int i = 0; i < fuselageSideCurveMiddle.size()-1; i++) {
			PVector pv = fuselageSideCurveMiddle.get(i);
			
			if(pv.z > fusCamberZAtMiddle) {
				fuselageSCMiddleUpperZCoords.add((double) pv.z);
				fuselageSCMiddleUpperYCoords.add((double) pv.y);
			} else if(pv.z < fusCamberZAtMiddle) {
				fuselageSCMiddleLowerZCoords.add((double) pv.z);
				fuselageSCMiddleLowerYCoords.add((double) pv.y);
			}
		}
		
		fuselageSCMiddleUpperZCoords.add(fusCamberZAtMiddle);
		fuselageSCMiddleUpperYCoords.add(fusWidthAtMiddle);
		
		Collections.reverse(fuselageSCMiddleLowerZCoords);
		Collections.reverse(fuselageSCMiddleLowerYCoords);
		
		// Call to specific fairing generator method
		AttachmentType attachmentType = getAttachmentType(rootAirfoilPnts, tipAirfoilPnts, fuselage);
		double fairingWidth;
		
		switch(attachmentType) {
		
		case DETACHED_UP:
			fairingWidth = fusWidthAtMiddle*sideSizeFactor;
			
			List<double[]> sideAirfoilPoints = AircraftUtils.populateCoordinateList(
					fairingWidth, 
					genAirfoil, 
					wing
					);
			
			double[] sideTopPnt = sideAirfoilPoints.stream()
					.max(Comparator.comparing(coord -> coord[2])).get();
			
			fairingShapes.addAll(generateDetachedUpFairingShapes(
					fairingHeightFactor,
					heightBelowContactFactor,
					heightAboveContactFactor,
					filletRadiusFactor,
					rootThickness,
					rootChord,
					fairingFrontLength,
					fairingBackLength,
					fairingWidth,
					rootTopPnt,
					rootLeadingEdge,
					rootTrailingEdge,
					sideTopPnt,
					fuselageSCFrontTopPnt,
					fuselageSCFrontBottomPnt,
					fuselageSCBackTopPnt,
					fuselageSCBackBottomPnt,
					fuselageSCMiddleUpperYCoords,
					fuselageSCMiddleUpperZCoords,
					fuselageSCMiddleLowerYCoords,
					fuselageSCMiddleLowerZCoords
					));
			
			break;
			
		case ATTACHED_UP:
			
			break;
			
		case MIDDLE:
			
			break;
			
		case ATTACHED_DOWN:
			double[] fusWingContactPnt = new double[] {
					tipTopPnt[0],
					MyMathUtils.getInterpolatedValue1DSpline(
							MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerZCoords), 
							MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerYCoords), 
							tipTopPnt[2]
							),
					tipTopPnt[2]
			};
			
			fairingWidth = fusWingContactPnt[1] + (fusWidthAtMiddle - fusWingContactPnt[1])*sideSizeFactor;
			
			fairingShapes.addAll(generateAttachedDownFairingShapes(
					fairingHeightFactor,
					heightBelowContactFactor,
					heightAboveContactFactor,
					filletRadiusFactor,
					rootThickness,
					rootChord,
					fairingFrontLength,
					fairingBackLength,
					fairingWidth,
					rootTopPnt,
					rootLeadingEdge,
					rootTrailingEdge,
					tipTopPnt,
					fuselageSCFrontTopPnt,
					fuselageSCFrontBottomPnt,
					fuselageSCBackTopPnt,
					fuselageSCBackBottomPnt,
					fuselageSCMiddleUpperYCoords,
					fuselageSCMiddleUpperZCoords,
					fuselageSCMiddleLowerYCoords,
					fuselageSCMiddleLowerZCoords
					));
			
			break;
			
		case DETACHED_DOWN:
			
			break;
		};
		
		return fairingShapes;
	}
	
	public static List<OCCShape> generateDetachedUpFairingShapes(
			double fairingHeightFactor,
			double heightBelowContactFactor,
			double heightAboveContactFactor,
			double filletRadiusFactor,
			double rootThickness,
			double rootChord,
			double fairingFrontLength,
			double fairingBackLength,
			double fairingWidth,
			double[] rootTopPnt,
			double[] rootLeadingEdge,
			double[] rootTrailingEdge,
			double[] sideTopPnt,
			double[] fuselageSCFrontTopPnt,
			double[] fuselageSCFrontBottomPnt,
			double[] fuselageSCBackTopPnt,
			double[] fuselageSCBackBottomPnt,
			List<Double> fuselageSCMiddleUpperYCoords,
			List<Double> fuselageSCMiddleUpperZCoords,
			List<Double> fuselageSCMiddleLowerYCoords,
			List<Double> fuselageSCMiddleLowerZCoords
			) {
		List<OCCShape> fairingShapes = new ArrayList<>();
		
		// Generate fairing sketching points
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
		double fairingMaximumZ = Math.max(rootTopPnt[2], sideTopPnt[2]) + rootThickness*fairingHeightFactor;
		
		double[] pntG = new double[] {
				rootLeadingEdge[0] - fairingFrontLength,
				rootLeadingEdge[1],
				fuselageUppContactPnt[2] - (fuselageUppContactPnt[2] - fairingMinimumZ)*heightBelowContactFactor
		};
		
		double[] pntA = new double[] {
				rootLeadingEdge[0] - fairingFrontLength,
				rootLeadingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*fairingHeightFactor
		};
		
		double[] pntB = new double[] {
				rootLeadingEdge[0],
				rootLeadingEdge[1],
				fairingMaximumZ - (fairingMaximumZ - rootLeadingEdge[2])*0.75
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
				rootTrailingEdge[0] + fairingBackLength,
				rootTrailingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*fairingHeightFactor
		};
		
		double[] pntF = new double[] {
				rootTrailingEdge[0] + fairingBackLength,
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
		
		// Generate fairing right patches
		List<double[]> mainCurvePnts = new ArrayList<>();
		List<double[]> sideCurvePnts = new ArrayList<>();
		List<double[]> subSegmentPnts = new ArrayList<>();
		
		mainCurvePnts.add(pntA);
		mainCurvePnts.add(pntB);
		mainCurvePnts.add(pntC);
		mainCurvePnts.add(pntD);
		mainCurvePnts.add(pntE);
		
		sideCurvePnts.add(pntA);
		sideCurvePnts.add(pntI);
		sideCurvePnts.add(pntL);
		sideCurvePnts.add(pntM);
		sideCurvePnts.add(pntE);
		
		subSegmentPnts.add(pntG);
		subSegmentPnts.add(pntF);
		
		List<OCCShape> fairingRightPatches = generateFairingRightPatches(
				mainCurvePnts, 
				sideCurvePnts, 
				subSegmentPnts
				);
		
		// Generate fairing solid
		double filletRadius = Math.abs(pntA[2] - pntG[2])*filletRadiusFactor;
		fairingShapes.add(generateFairingSolid(fairingRightPatches, filletRadius));
		
		return fairingShapes;		
	}
	
	public static List<OCCShape> generateAttachedDownFairingShapes(
			double fairingHeightFactor,
			double heightBelowContactFactor,
			double heightAboveContactFactor,
			double filletRadiusFactor,
			double rootThickness,
			double rootChord,
			double fairingFrontLength,
			double fairingBackLength,
			double fairingWidth,
			double[] rootTopPnt,
			double[] rootLeadingEdge,
			double[] rootTrailingEdge,
			double[] tipTopPnt,
			double[] fuselageSCFrontTopPnt,
			double[] fuselageSCFrontBottomPnt,
			double[] fuselageSCBackTopPnt,
			double[] fuselageSCBackBottomPnt,
			List<Double> fuselageSCMiddleUpperYCoords,
			List<Double> fuselageSCMiddleUpperZCoords,
			List<Double> fuselageSCMiddleLowerYCoords,
			List<Double> fuselageSCMiddleLowerZCoords
			) {
		List<OCCShape> fairingShapes = new ArrayList<>();
		
		// Generate fairing sketching points
		double[] fuselageLowContactPnt = new double[] {
				tipTopPnt[0],
				fairingWidth,
				MyMathUtils.getInterpolatedValue1DSpline(
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerYCoords), 
						MyArrayUtils.convertToDoublePrimitive(fuselageSCMiddleLowerZCoords), 
						fairingWidth
						)
		};
		double[] fuselageUppContactPnt = new double[] {
				tipTopPnt[0],
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
				rootThickness*fairingHeightFactor;
		
		double[] pntG = new double[] {
				rootLeadingEdge[0] - fairingFrontLength,
				rootLeadingEdge[1],
				fuselageLowContactPnt[2] + (fairingMaximumZ - fuselageLowContactPnt[2])*heightAboveContactFactor
		};
		
		double[] pntA = new double[] {
				rootLeadingEdge[0] - fairingFrontLength,
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
				rootTrailingEdge[0] + fairingBackLength,
				rootTrailingEdge[1],
				fuselageLowContactPnt[2] - (fuselageLowContactPnt[2] - fuselageMinimumZ)*heightBelowContactFactor
		};
		
		double[] pntF = new double[] {
				rootTrailingEdge[0] + fairingBackLength,
				rootTrailingEdge[1],
				fuselageLowContactPnt[2] + (fairingMaximumZ - fuselageLowContactPnt[2])*heightAboveContactFactor
		};
		
		double[] pntI = new double[] {
				rootLeadingEdge[0],
				fuselageLowContactPnt[1] - (fuselageLowContactPnt[1] - rootLeadingEdge[1])*0.05,
				pntA[2]
		};
		
		double[] pntL = new double[] {
				rootTopPnt[0],
				fuselageLowContactPnt[1] - (fuselageLowContactPnt[1] - rootTopPnt[1])*0.05,
				pntA[2]
		};
		
		double[] pntM = new double[] {
				rootTrailingEdge[0],
				fuselageLowContactPnt[1] - (fuselageLowContactPnt[1] - rootTrailingEdge[1])*0.05,
				pntA[2]
		};
		
		// Generate fairing right patches
		List<double[]> mainCurvePnts = new ArrayList<>();
		List<double[]> sideCurvePnts = new ArrayList<>();
		List<double[]> subSegmentPnts = new ArrayList<>();

		mainCurvePnts.add(pntA);
		mainCurvePnts.add(pntB);
		mainCurvePnts.add(pntC);
		mainCurvePnts.add(pntD);
		mainCurvePnts.add(pntE);

		sideCurvePnts.add(pntA);
		sideCurvePnts.add(pntI);
		sideCurvePnts.add(pntL);
		sideCurvePnts.add(pntM);
		sideCurvePnts.add(pntE);

		subSegmentPnts.add(pntG);
		subSegmentPnts.add(pntF);

		List<OCCShape> fairingRightPatches = generateFairingRightPatches(
				mainCurvePnts, 
				sideCurvePnts, 
				subSegmentPnts
				);

		// Generate fairing solid
		double filletRadius = Math.abs(pntA[2] - pntG[2])*filletRadiusFactor;
		fairingShapes.add(generateFairingSolid(fairingRightPatches, filletRadius));
		
		return fairingShapes;
	}
	
	public static List<OCCShape> generateFairingRightPatches(
			List<double[]> mainCurvePnts, 
			List<double[]> sideCurvePnts,
			List<double[]> subSegmentPnts
			) {
		List<OCCShape> rightPatches = new ArrayList<>();
		
		if(OCCUtils.theFactory == null) 
			OCCUtils.initCADShapeFactory();
		
		double[] pntA = mainCurvePnts.get(0);
		double[] pntE = mainCurvePnts.get(mainCurvePnts.size()-1);
		double[] pntG = subSegmentPnts.get(0);
		double[] pntF = subSegmentPnts.get(subSegmentPnts.size()-1);
		
		// Create sketching curves
		CADGeomCurve3D mainCurve = OCCUtils.theFactory.newCurve3D(mainCurvePnts, false);
		CADGeomCurve3D sideCurve = OCCUtils.theFactory.newCurve3D(sideCurvePnts, false);
		
		// Create curves to patch through
		int nMain = 15; // number of discretization points for the main curve
		mainCurve.discretize(nMain);
		
		List<double[]> mainCurveNewPnts = ((OCCGeomCurve3D) mainCurve).getDiscretizedCurve().getPoints().stream()
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
			double[] mainPnt = mainCurveNewPnts.get(i);
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
		
		rightPatches.add(upperPatch);
		rightPatches.add(sidePatch);
		rightPatches.add(lowerPatch);
				
		return rightPatches;
	}
	
	public static OCCSolid generateFairingSolid(List<OCCShape> fairingRightPatches, double filletRadius) {
		
		// Create a shell from adjacent patches
		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
				(CADFace) OCCUtils.theFactory.newShape(fairingRightPatches.get(0).getShape()),
				(CADFace) OCCUtils.theFactory.newShape(fairingRightPatches.get(1).getShape()),
				(CADFace) OCCUtils.theFactory.newShape(fairingRightPatches.get(2).getShape())
				);

		// Apply a fillet to specific edges
		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());

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
		
		return solidFairing;
	}
	
	public static AttachmentType getAttachmentType(
			List<double[]> rootAirfoilPnts,
			List<double[]> tipAirfoilPnts,
			Fuselage fuselage) {

		double[] rootAirfoilTopPoint = rootAirfoilPnts.stream()
				.max(Comparator.comparing(pnt -> pnt[2])).get();
		double[] sideAirfoilTopPoint = tipAirfoilPnts.stream()
				.max(Comparator.comparing(pnt -> pnt[2])).get();
		double[] rootAirfoilBottomPoint = rootAirfoilPnts.stream()
				.min(Comparator.comparing(pnt -> pnt[2])).get();
		double[] sideAirfoilBottomPoint = tipAirfoilPnts.stream()
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
			return AttachmentType.DETACHED_UP;

		} else if(sideAirfoilTopPoint[2] < fuselageZTopAtTopX && sideAirfoilBottomPoint[2] > fuselageCamberZAtBottomX) {
			return AttachmentType.ATTACHED_UP;
		}

		if(rootAirfoilBottomPoint[2] < fuselageZBottomAtBottomX) {
			return AttachmentType.DETACHED_DOWN;

		} else if(sideAirfoilTopPoint[2] < fuselageCamberZAtTopX && sideAirfoilBottomPoint[2] > fuselageZBottomAtBottomX) {
			return AttachmentType.ATTACHED_DOWN;
		}		

		return AttachmentType.MIDDLE;
	}
	
	public enum AttachmentType {
		DETACHED_UP,
		DETACHED_DOWN,	
		ATTACHED_UP,	
		ATTACHED_DOWN, 	
		MIDDLE
	}
	
//	// Create a lifting surface - fuselage fairing in case the lifting surface root 
//	// airfoil top point z coordinate is lower than the fuselage camber z coordinate
//	
//	public static void main(String[] args) {
//		System.out.println("-------------------");
//		System.out.println("JPADCADSandbox Test");
//		System.out.println("-------------------");
//		
//		// Import the aircraft
//		Aircraft aircraft = AircraftUtils.importAircraft(args);
//		Fuselage fuselage = aircraft.getFuselage();
//		LiftingSurface wing = aircraft.getWing();
//
//		// Fairing parameters
//		double frontLengthFactor = 1.00;
//		double backLengthFactor = 1.00;  
//		double sideSizeFactor = 0.95; 
//
//		double heigthAboveRootFactor = 0.90;
//		double zBelowFairingMaximumYFactor = 0.35;
//		double zBelowFuselageMinimumZFactor = 0.15;
//		
//		// Geometric data collection
//		double liftingSurfaceYApex = wing.getYApexConstructionAxes().doubleValue(SI.METER);
//
//		Airfoil baseAirfoil = wing.getAirfoilList().get(0);
//
//		double rootAirfoilChord = wing.getChordAtYActual(liftingSurfaceYApex);
//		double rootAirfoilThickness = baseAirfoil.getThicknessToChordRatio()*rootAirfoilChord;
//
//		List<double[]> rootAirfoilPoints = AircraftUtils.populateCoordinateList(
//				liftingSurfaceYApex, 
//				baseAirfoil, 
//				wing
//				);
//
//		double[] topPointRoot = rootAirfoilPoints.stream()
//				.max(Comparator.comparing(coord -> coord[2])).get();
//		double[] leadingEdgeRoot = rootAirfoilPoints.stream()
//				.min(Comparator.comparing(coord -> coord[0])).get();
//		double[] trailingEdgeRoot = rootAirfoilPoints.stream()
//				.max(Comparator.comparing(coord -> coord[0])).get();
//
//		List<PVector> fuselageSideCurveMiddle = fuselage.getUniqueValuesYZSideRCurve(
//				Amount.valueOf(topPointRoot[0], SI.METER));	
//		List<PVector> fuselageSideCurveFront = fuselage.getUniqueValuesYZSideRCurve(
//				Amount.valueOf(leadingEdgeRoot[0] - frontLengthFactor*rootAirfoilChord, SI.METER));
//		List<PVector> fuselageSideCurveBack = fuselage.getUniqueValuesYZSideRCurve(
//				Amount.valueOf(trailingEdgeRoot[0] + backLengthFactor*rootAirfoilChord, SI.METER));
//		
//		double xAtMiddle = topPointRoot[0];
//		double widthYAtMiddle = fuselage.getWidthAtX(xAtMiddle)/2;
//		double camberZAtMiddle = fuselage.getCamberZAtX(xAtMiddle);
//				
//		int nPnts = fuselageSideCurveMiddle.size();
//		int nPntsInterp = 100;
//		
//		List<Double> fuselageSideCurveMiddleLowerZCoords = new ArrayList<>();
//		List<Double> fuselageSideCurveMiddleLowerYCoords = new ArrayList<>();
//		
//		fuselageSideCurveMiddleLowerZCoords.add(camberZAtMiddle);
//		fuselageSideCurveMiddleLowerYCoords.add(widthYAtMiddle);
//		
//		fuselageSideCurveMiddleLowerZCoords.addAll(fuselageSideCurveMiddle.stream()
//				.filter(pv -> pv.z < camberZAtMiddle)
//				.map(pv -> (double) pv.z)
//				.collect(Collectors.toList()));
//		fuselageSideCurveMiddleLowerYCoords.addAll(fuselageSideCurveMiddle.stream()
//				.filter(pv -> pv.z < camberZAtMiddle)
//				.map(pv -> (double) pv.y)
//				.collect(Collectors.toList()));
//		
//		Collections.reverse(fuselageSideCurveMiddleLowerZCoords);
//		Collections.reverse(fuselageSideCurveMiddleLowerYCoords);
//		
////		double[] fuselageSideCurveMiddleYCoordsInterp = MyArrayUtils.convertToDoublePrimitive(
////				MyMathUtils.getInterpolatedValue1DLinear(
////						MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleZCoords), 
////						MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleYCoords),
////						MyArrayUtils.linspace(
////								fuselageSideCurveMiddleZCoords.get(0), 
////								fuselageSideCurveMiddleZCoords.get(nPnts-1),
////								nPntsInterp
////								)
////						)
////				); 
//
//		double[] fuselageBottomPoint = new double[] {
//				(float) fuselageSideCurveMiddle.get(fuselageSideCurveMiddle.size()-1).x,
//				(float) fuselageSideCurveMiddle.get(fuselageSideCurveMiddle.size()-1).y,
//				(float) fuselageSideCurveMiddle.get(fuselageSideCurveMiddle.size()-1).z
//		};		
//		double[] fuselageMaximumYPointFront = fuselageSideCurveFront.stream()
//				.map(pv -> new double[] {pv.x, pv.y, pv.z})
//				.max(Comparator.comparing(coord -> coord[1])).get();
//		double[] fuselageMaximumYPointBack = fuselageSideCurveBack.stream()
//				.map(pv -> new double[] {pv.x, pv.y, pv.z})
//				.max(Comparator.comparing(coord -> coord[1])).get();
////		double[] fuselageMaximumYPointMiddle = Arrays.asList(fuselageSideCurveMiddleYCoordsInterp).stream()
////				.max(Comparator.comparing(coord -> coord[1])).get();
//		
//		double zMaxAdmissibleFuselageWidth;
//		double[] fuselageSidePoint;
////		if(fuselageMaximumWidthPointFront[1] < fuselageMaximumWidthPointBack[2]) {
////
////			zMaxAdmissibleFuselageWidth = fuselageMaximumWidthPointFront[2];
////			fuselageSidePoint = fuselageSideCurveFront.stream()
////					.map(pv -> new double[] {pv.x, pv.y, pv.z})
////					.filter(coord -> coord[1] > fuselageMaximumWidthPointFront[1]*sideSizeFactor)
////					.min(Comparator.comparing(coord -> coord[2])).get();		
////		} else {
////
////			zMaxAdmissibleFuselageWidth = fuselageMaximumWidthPointBack[2];
////			fuselageSidePoint = fuselageSideCurveBack.stream()
////					.map(pv -> new double[] {pv.x, pv.y, pv.z})
////					.filter(coord -> coord[1] > fuselageMaximumWidthPointBack[1]*sideSizeFactor)
////					.min(Comparator.comparing(coord -> coord[2])).get();
////		}
////		fuselageSidePoint = Arrays.asList(fuselageSideCurveMiddleYCoordsInterp).stream()
////				.filter(coord -> coord[1] > fuselageMaximumYPointMiddle[1]*sideSizeFactor)
////				.min(Comparator.comparing(coord -> coord[2])).get();
//		
//		double fuselageSidePointZ = MyMathUtils.getInterpolatedValue1DSpline(
//				MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleLowerYCoords), 
//				MyArrayUtils.convertToDoublePrimitive(fuselageSideCurveMiddleLowerZCoords), 
//				widthYAtMiddle*sideSizeFactor
//				);
//		
//		double fairingMaximumY = widthYAtMiddle*sideSizeFactor;		
//		double fairingMinimumZ = fuselageBottomPoint[2] - (fuselageSidePointZ - fuselageBottomPoint[2])*zBelowFuselageMinimumZFactor;
//
//		List<double[]> sideAirfoilPoints = AircraftUtils.populateCoordinateList(
//				fairingMaximumY, 
//				baseAirfoil, 
//				wing
//				);
//
//		double[] topPointSide = sideAirfoilPoints.stream()
//				.max(Comparator.comparing(coord -> coord[2])).get();
//		
//		if(topPointSide[2] > camberZAtMiddle) return;
//
//		double fairingMaximumZ = Math.max(topPointRoot[2], topPointSide[2]) + rootAirfoilThickness*heigthAboveRootFactor;
//		
//		// Create points and curves for sketching (XZ plane)
//		OCCUtils.initCADShapeFactory();
//
////		CADGeomCurve3D fusCurveeee = OCCUtils.theFactory.newCurve3D(fuselageSideCurveMiddleInterp, false);
//		CADGeomCurve3D rootAirfoil = OCCUtils.theFactory.newCurve3D(rootAirfoilPoints, false);
//		CADGeomCurve3D sideAirfoil = OCCUtils.theFactory.newCurve3D(sideAirfoilPoints, false);
//
//		double[] pointA = new double[] {
//				fuselageMaximumYPointFront[0], 
//				topPointRoot[1], 
//				fuselageSidePointZ - (fuselageSidePointZ - fuselageBottomPoint[2])*zBelowFairingMaximumYFactor
//		};
//
//		double[] pointB = new double[] {
//				topPointRoot[0], 
//				topPointRoot[1], 
//				fairingMaximumZ
//		};
//
//		double[] pointC = new double[] {
//				fuselageMaximumYPointBack[0], 
//				topPointRoot[1], 
//				fuselageSidePointZ - (fuselageSidePointZ - fuselageBottomPoint[2])*zBelowFairingMaximumYFactor
//		};
//
//		double[] pointD = new double[] {
//				topPointRoot[0], 
//				topPointRoot[1], 
//				fairingMinimumZ
//		};
//
//		double[] pointE = new double[] {
//				leadingEdgeRoot[0],
//				leadingEdgeRoot[1],
//				pointB[2] - (pointB[2] - leadingEdgeRoot[2])*0.15
//		};
//
//		double[] pointF = new double[] {
//				trailingEdgeRoot[0],
//				trailingEdgeRoot[1],
//				pointB[2] - (pointB[2] - trailingEdgeRoot[2])*0.15
//		};
//
//		double[] pointA2 = new double[] {
//				fuselageMaximumYPointFront[0],
//				topPointRoot[1],
//				fuselageSidePointZ
//		};
//
//		double[] pointC2 = new double[] {
//				fuselageMaximumYPointBack[0],
//				topPointRoot[1],
//				fuselageSidePointZ
//		};
//
//		CADGeomCurve3D upperCurve = OCCUtils.theFactory.newCurve3D(false, pointA2, pointE, pointB, pointF, pointC2);
//		CADGeomCurve3D lowerCurve = OCCUtils.theFactory.newCurve3D(false, pointA, pointD, pointC);
//
//		// Create points and curves for sketching (XY)
//		double[] pointA3 = new double[] {
//				pointA[0],
//				pointA[1],
//				pointA[2] + (pointA2[2] - pointA[2])/2
//		};
//
//		double[] pointG = new double[] {
//				topPointRoot[0],
//				fairingMaximumY,
//				pointA3[2]
//		};
//
//		double[] pointH = new double[] {
//				leadingEdgeRoot[0],
//				fairingMaximumY - (fairingMaximumY - topPointRoot[1])*0.01,
//				pointA3[2]
//		};
//
//		double[] pointI = new double[] {
//				trailingEdgeRoot[0],
//				fairingMaximumY - (fairingMaximumY - topPointRoot[1])*0.01,
//				pointA3[2]
//		};
//
//		double[] pointC3 = new double[] {
//				pointC[0],
//				pointC[1],
//				pointC[2] + (pointC2[2] - pointC[2])/2
//		};
//
//		CADGeomCurve3D sideCurve = OCCUtils.theFactory.newCurve3D(false, pointA3, pointH, pointG, pointI, pointC3);
//		
//		// Create curves to patch through
//		int nUppPnts = 12; // number of profile curves
//		upperCurve.discretize(nUppPnts);
//		
//		List<double[]> upperCurvePoints = ((OCCGeomCurve3D) upperCurve).getDiscretizedCurve().getPoints().stream()
//				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
//				.collect(Collectors.toList());
//		
//		int nLowPnts = 25; // number of points for interpolation
//		lowerCurve.discretize(nLowPnts);
//		
//		List<double[]> lowerCurvePoints = ((OCCGeomCurve3D) lowerCurve).getDiscretizedCurve().getPoints().stream()
//				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
//				.collect(Collectors.toList());
//		
//		double[] lowerCurveXCoords = MyArrayUtils.convertToDoublePrimitive(
//				MyArrayUtils.convertListOfDoubleToDoubleArray(
//						lowerCurvePoints.stream()
//										 .map(a -> a[0])
//										 .collect(Collectors.toList())
//										 ));
//		double[] lowerCurveZCoords = MyArrayUtils.convertToDoublePrimitive(
//				MyArrayUtils.convertListOfDoubleToDoubleArray(
//						lowerCurvePoints.stream()
//									     .map(a -> a[2])
//									     .collect(Collectors.toList())
//									     ));
//		
//		int nSidePnts = 25; // number of points for interpolation
//		sideCurve.discretize(nSidePnts);
//		
//		List<double[]> sideCurvePoints = ((OCCGeomCurve3D) sideCurve).getDiscretizedCurve().getPoints().stream()
//				.map(gp -> new double[] {gp.X(), gp.Y(), gp.Z()})
//				.collect(Collectors.toList());
//		
//		double[] sideCurveXCoords = MyArrayUtils.convertToDoublePrimitive(
//				MyArrayUtils.convertListOfDoubleToDoubleArray(
//						sideCurvePoints.stream()
//										 .map(a -> a[0])
//										 .collect(Collectors.toList())
//										 ));
//		double[] sideCurveYCoords = MyArrayUtils.convertToDoublePrimitive(
//				MyArrayUtils.convertListOfDoubleToDoubleArray(
//						sideCurvePoints.stream()
//									     .map(a -> a[1])
//									     .collect(Collectors.toList())
//									     ));
//		
//		List<CADGeomCurve3D> upperCurves = new ArrayList<>();
//		List<CADGeomCurve3D> lowerCurves = new ArrayList<>();
//		List<CADGeomCurve3D> sideCurves = new ArrayList<>();
//		
//		for(int i = 1; i < nUppPnts-1; i++) {
//			
//			double[] upperCurvePoint = upperCurvePoints.get(i);
//			double[] upperCurve2Point = new double[] {
//					upperCurvePoint[0],
//					MyMathUtils.getInterpolatedValue1DLinear(
//							sideCurveXCoords, 
//							sideCurveYCoords, 
//							upperCurvePoint[0]
//							),
//					upperCurvePoint[2]
//			};
//			upperCurves.add(OCCUtils.theFactory.newCurve3D(upperCurvePoint, upperCurve2Point));
//			
//			double[] lowerCurve2Point = new double[] {
//					upperCurvePoint[0],
//					MyMathUtils.getInterpolatedValue1DLinear(
//							sideCurveXCoords,
//							sideCurveYCoords,
//							upperCurvePoint[0]
//							),
//					MyMathUtils.getInterpolatedValue1DLinear(
//							lowerCurveXCoords,
//							lowerCurveZCoords,
//							upperCurvePoint[0]
//							),
//			};				
//			double[] interpolatedLowerCurvePoint = new double[] {
//					upperCurvePoint[0],
//					upperCurvePoint[1],
//					MyMathUtils.getInterpolatedValue1DLinear(
//							lowerCurveXCoords,
//							lowerCurveZCoords,
//							upperCurvePoint[0]
//							),
//			};
//			lowerCurves.add(OCCUtils.theFactory.newCurve3D(lowerCurve2Point, interpolatedLowerCurvePoint));
//			
//			double[] interpolatedSideCurvePoint = new double[] {
//					upperCurvePoint[0],
//					MyMathUtils.getInterpolatedValue1DLinear(
//							sideCurveXCoords,
//							sideCurveYCoords,
//							upperCurvePoint[0]
//							),
//					sideCurvePoints.get(0)[2]
//			};		
//			sideCurves.add(OCCUtils.theFactory.newCurve3D(
//					false, 
//					upperCurve2Point, 
//					interpolatedSideCurvePoint, 
//					lowerCurve2Point
//					));
//		}
//
//		// Create patches
//		OCCShape upperPatch = OCCUtils.makePatchThruSections(
//				OCCUtils.theFactory.newVertex(pointA2[0], pointA2[1], pointA2[2]), 
//				upperCurves, 
//				OCCUtils.theFactory.newVertex(pointC2[0], pointC2[1], pointC2[2])
//				);	
//		
//		OCCShape lowerPatch = OCCUtils.makePatchThruSections(
//				OCCUtils.theFactory.newVertex(pointA[0], pointA[1], pointA[2]), 
//				lowerCurves, 
//				OCCUtils.theFactory.newVertex(pointC[0], pointC[1], pointC[2])
//				);	
//		
//		List<TopoDS_Edge> upperEdges = new ArrayList<>();
//		TopExp_Explorer explorerUpp = new TopExp_Explorer(upperPatch.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
//		while(explorerUpp.More() > 0) {
//			upperEdges.add(TopoDS.ToEdge(explorerUpp.Current()));
//			explorerUpp.Next();
//		}	
//		
//		List<TopoDS_Edge> lowerEdges = new ArrayList<>();
//		TopExp_Explorer explorerLow = new TopExp_Explorer(lowerPatch.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
//		while(explorerLow.More() > 0) {
//			lowerEdges.add(TopoDS.ToEdge(explorerLow.Current()));
//			explorerLow.Next();
//		}	
//		
//		OCCShape sidePatch = OCCUtils.makePatchThruSections(
//				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(upperEdges.get(1))),
//				OCCUtils.theFactory.newCurve3D((CADEdge) OCCUtils.theFactory.newShape(lowerEdges.get(3)))
//				);
//		
//		// Create a shell from adjacent patches
//		OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentFaces(
//				(CADFace) OCCUtils.theFactory.newShape(upperPatch.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(sidePatch.getShape()),
//				(CADFace) OCCUtils.theFactory.newShape(lowerPatch.getShape())
//				);
//		
//		// Apply a fillet to specific edges
//		BRepFilletAPI_MakeFillet filletMaker = new BRepFilletAPI_MakeFillet(rightShell.getShape());
//		double filletRadius = (pointA2[2] - pointA[2])*1.00;
//		
//		List<TopoDS_Edge> shellEdges = new ArrayList<>();
//		TopExp_Explorer shellExplorer = new TopExp_Explorer(rightShell.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
//		while(shellExplorer.More() > 0) {
//			shellEdges.add(TopoDS.ToEdge(shellExplorer.Current()));
//			shellExplorer.Next();
//		}
//		filletMaker.Add(filletRadius, shellEdges.get(6));
//		
//		List<TopoDS_Shell> filletShells = new ArrayList<>();
//		TopExp_Explorer filletShellExplorer = new TopExp_Explorer(filletMaker.Shape(), TopAbs_ShapeEnum.TopAbs_SHELL);
//		while(filletShellExplorer.More() > 0) {
//			filletShells.add(TopoDS.ToShell(filletShellExplorer.Current()));
//			filletShellExplorer.Next();
//		}
//		OCCShell rightShellFillet = (OCCShell) OCCUtils.theFactory.newShape(filletShells.get(0));
//		
//		// Mirroring and creating the solid	
//		gp_Trsf mirrorTransform = new gp_Trsf();
//		gp_Ax2 mirrorPointPlane = new gp_Ax2(
//				new gp_Pnt(0.0, 0.0, 0.0),
//				new gp_Dir(0.0, 1.0, 0.0),
//				new gp_Dir(1.0, 0.0, 0.0)
//				);
//		mirrorTransform.SetMirror(mirrorPointPlane);	
//		BRepBuilderAPI_Transform mirrorBuilder = new BRepBuilderAPI_Transform(mirrorTransform);
//		mirrorBuilder.Perform(rightShellFillet.getShape(), 1);
//		OCCShell leftShell = (OCCShell) OCCUtils.theFactory.newShape(mirrorBuilder.Shape());
//		
//		BRepBuilderAPI_MakeSolid solidMaker = new BRepBuilderAPI_MakeSolid();
//		solidMaker.Add(TopoDS.ToShell(rightShellFillet.getShape()));
//		solidMaker.Add(TopoDS.ToShell(leftShell.getShape()));
//		solidMaker.Build();
//		OCCSolid solidFairing = (OCCSolid) OCCUtils.theFactory.newShape(solidMaker.Solid());
//		
//		// Export shapes to CAD file
//		List<OCCShape> shapes = new ArrayList<>();
//		
////		shapes.add((OCCShape) ((OCCEdge) rootAirfoil.edge()));
////		shapes.add((OCCShape) ((OCCEdge) sideAirfoil.edge()));
////		shapes.add((OCCShape) ((OCCEdge) upperCurve.edge()));
////		shapes.add((OCCShape) ((OCCEdge) lowerCurve.edge()));
////		shapes.add((OCCShape) ((OCCEdge) sideCurve.edge()));
////		shapes.add(upperPatch);
////		shapes.add(lowerPatch);
////		shapes.add(sidePatch);
//		shapes.add(solidFairing);
//		
////		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 7, 7, true, true, false);
////		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
////		
////		shapes.addAll(fuselageShapes);
////		shapes.addAll(wingShapes);
//		
//		String fileName = "Test27mds.brep";
//
//		if (OCCUtils.write(fileName, shapes))
//			System.out.println("========== [main] Output written on file: " + fileName);
//	}
}

package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepAlgoAPI_Cut;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class Test30mds {

	public static void main(String[] args) {		
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface canard = aircraft.getCanard();
		
		// Set fairing parameters (assuming wing is detached from fuselage)
		double frontLengthFactor = 1.25;
		double backLengthFactor = 1.25;
		double sideSizeFactor = 0.50;
		double heightBelowContactFactor = 0.50;
		double heightAboveContactFactor = 0.10;
		double filletRadiusFactor = 0.40;
		
		// Geometric data collection
		Airfoil genAirfoil = canard.getAirfoilList().get(0);
		
		double rootYCoord = canard.getYApexConstructionAxes().doubleValue(SI.METER);	
		double rootChord = canard.getChordAtYActual(rootYCoord);
		double rootThickness = genAirfoil.getThicknessToChordRatio()*rootChord;
		
		double fairingFrontLength = frontLengthFactor*rootChord;
		double fairingBackLength = backLengthFactor*rootChord;
		
		List<double[]> rootAirfoilPnts = AircraftUtils.populateCoordinateList(
				rootYCoord, 
				genAirfoil, 
				canard
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
				canard
				);
		
		double[] tipTopPnt = tipAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		double[] tipLeadingEdge = tipAirfoilPnts.stream()
				.min(Comparator.comparing(coord -> coord[0])).get();
		double[] tipTrailingEdge = tipAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[0])).get();
		
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
		
		double fairingWidth = fusWidthAtMiddle*sideSizeFactor;	
		double sideChord = canard.getChordAtYActual(fairingWidth);
		
		List<double[]> sideAirfoilPnts = AircraftUtils.populateCoordinateList(
				fairingWidth, 
				genAirfoil, 
				canard
				);
		
		double[] sideTopPnt = sideAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[2])).get();
		double[] sideLeadingEdge = sideAirfoilPnts.stream()
				.min(Comparator.comparing(coord -> coord[0])).get();
		double[] sideTrailingEdge = sideAirfoilPnts.stream()
				.max(Comparator.comparing(coord -> coord[0])).get();
		
		// Limit airfoil top curves 
		int maxZIndexRoot = IntStream.range(0, rootAirfoilPnts.size())
				.reduce((i, j) -> rootAirfoilPnts.get(i)[2] < rootAirfoilPnts.get(j)[2] ? j : i)
				.getAsInt();
		
		int maxZIndexSide = IntStream.range(0, sideAirfoilPnts.size())
				.reduce((i, j) -> sideAirfoilPnts.get(i)[2] < sideAirfoilPnts.get(j)[2] ? j : i)
				.getAsInt();
		
		List<double[]> rootAirfoilUpperPnts = rootAirfoilPnts.subList(0, maxZIndexRoot + 1);
		List<double[]> sideAirfoilUpperPnts = sideAirfoilPnts.subList(0, maxZIndexSide + 1);
		
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
		double fairingMaximumZ = Math.max(
				rootTopPnt[2], 
				sideTopPnt[2]
				);
		
		// root fairing sketching points
		double[] pntA = new double[] {
				rootTrailingEdge[0] + fairingBackLength,
				rootTrailingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*heightAboveContactFactor
		};
		double[] pntB = new double[] {
				rootLeadingEdge[0],
				rootLeadingEdge[1],
				rootLeadingEdge[2] + (rootTopPnt[2] - rootLeadingEdge[2])*0.75 
		};
		double[] pntC = new double[] {
				rootLeadingEdge[0] - fairingFrontLength,
				rootLeadingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*heightAboveContactFactor
		};		
		
		List<double[]> fairingRootSketchingPnts = new ArrayList<>();
		fairingRootSketchingPnts.add(pntA);
		fairingRootSketchingPnts.addAll(rootAirfoilUpperPnts);
		fairingRootSketchingPnts.add(pntB);
		fairingRootSketchingPnts.add(pntC);
		
		// side fairing sketching points
		double[] pntD = new double[] {
				sideTrailingEdge[0] + fairingBackLength/rootChord*sideChord,
				sideTrailingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*heightAboveContactFactor
		};
		double[] pntE = new double[] {
				sideLeadingEdge[0],
				sideLeadingEdge[1],
				sideLeadingEdge[2] + (sideTopPnt[2] - sideLeadingEdge[2])*0.75 
		};
		double[] pntF = new double[] {
				sideLeadingEdge[0] - fairingFrontLength/rootChord*sideChord,
				sideLeadingEdge[1],
				fuselageUppContactPnt[2] + (fuselageMaximumZ - fuselageUppContactPnt[2])*heightAboveContactFactor
		};	
		
		List<double[]> fairingSideSketchingPnts = new ArrayList<>();
		fairingSideSketchingPnts.add(pntD);
		fairingSideSketchingPnts.addAll(sideAirfoilUpperPnts);
		fairingSideSketchingPnts.add(pntE);
		fairingSideSketchingPnts.add(pntF);
		
		// Generate fairing sketching curves
		OCCUtils.initCADShapeFactory();	
		CADGeomCurve3D rootAirfoilCurve = OCCUtils.theFactory.newCurve3D(rootAirfoilPnts, false);
		CADGeomCurve3D sideAirfoilCurve = OCCUtils.theFactory.newCurve3D(sideAirfoilPnts, false);
		CADGeomCurve3D rootAirfoilUpperCurve = OCCUtils.theFactory.newCurve3D(rootAirfoilUpperPnts, false);
		CADGeomCurve3D sideAirfoilUpperCurve = OCCUtils.theFactory.newCurve3D(sideAirfoilUpperPnts, false);
		CADGeomCurve3D fairingRootSketchingCurve = OCCUtils.theFactory.newCurve3D(
				fairingRootSketchingPnts, 
				false, 
				new double[] {-1, 0, 0}, 
				new double[] {-1, 0, 0}, 
				false
				);
		CADGeomCurve3D fairingSideSketchingCurve = OCCUtils.theFactory.newCurve3D(
				fairingSideSketchingPnts, 
				false, 
				new double[] {-1, 0, 0}, 
				new double[] {-1, 0, 0}, 
				false
				);
			
		// Export CAD shapes
		List<OCCShape> exportShapes = new ArrayList<>();
		exportShapes.add((OCCShape) ((OCCEdge) rootAirfoilCurve.edge()));
		exportShapes.add((OCCShape) ((OCCEdge) sideAirfoilCurve.edge()));
		exportShapes.add((OCCShape) ((OCCEdge) rootAirfoilUpperCurve.edge()));
		exportShapes.add((OCCShape) ((OCCEdge) sideAirfoilUpperCurve.edge()));
		exportShapes.add((OCCShape) ((OCCEdge) fairingRootSketchingCurve.edge()));
		exportShapes.add((OCCShape) ((OCCEdge) fairingSideSketchingCurve.edge()));
		
		String fileName = "test30mds.brep";
		if(OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);		
	}

}

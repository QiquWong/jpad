package it.unina.daf.jpadcad.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.airfoils.Airfoil;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCFace;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCWire;
import javaslang.Tuple2;
import javaslang.Tuple3;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class AircraftCADUtils {

	public static OCCShape getFuselageSolidCAD(Fuselage fuselage,
			int numberNoseTrunkSections, int numberTailTrunkSections) {
		
		return getFuselageCAD(fuselage,
				0.15, 1.0, 3,
				numberNoseTrunkSections, XSpacingType.COSINUS,
				numberTailTrunkSections, XSpacingType.COSINUS,
				1.0, 0.15, 3,
				false, false, true
				).get(0);
	}
	
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage,
			int numberNoseTrunkSections, int numberTailTrunkSections,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
		
		return getFuselageCAD(fuselage,
				0.15, 1.0, 3,
				numberNoseTrunkSections, XSpacingType.COSINUS, 
				numberTailTrunkSections, XSpacingType.COSINUS,
				1.0, 0.15, 3,
				exportSupportShapes, exportShells, exportSolids
				);
	}
	
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage,
			int numberNoseTrunkSections, XSpacingType spacingTypeNoseTrunk,
			int numberTailTrunkSections, XSpacingType spacingTypeTailTrunk,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
		
		return getFuselageCAD(fuselage,
				0.15, 1.0, 3,
				numberNoseTrunkSections, spacingTypeNoseTrunk,
				numberTailTrunkSections, spacingTypeTailTrunk,
				1.0, 0.15, 3,
				exportSupportShapes, exportShells, exportSolids
				);
	}
	
	public static List<OCCShape> getFuselageCAD(Fuselage fuselage,
			double noseCapSectionFactor1, double noseCapSectionFactor2, int numberNoseCapSections, 
			int numberNoseTrunkSections, XSpacingType spacingTypeNoseTrunk, 
			int numberTailTrunkSections, XSpacingType spacingTypeTailTrunk, 
			double tailCapSectionFactor1, double tailCapSectionFactor2, int numberTailCapSections,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
		
		// ----------------------------------------------------------
		// Check whether continuing with the method
		// ----------------------------------------------------------
		if (fuselage == null) {
			System.out.println("========== [AircraftCADUtils::getFuselageCAD] The fuselage object passed to the "
					+ "getFuselageCAD method is null! Exiting the method ...");
			return null;
		}
		
		if (!exportSupportShapes && !exportShells && !exportSolids) {
			System.out.println("========== [AircraftCADUtils::getFuselageCAD] No shapes to export! Exiting the method ...");
			return null;
		}
		
		System.out.println("========== [AircraftCADUtils::getFuselageCAD]");
		
		// ----------------------------------------------------------
		// Check the factory
		// ----------------------------------------------------------
		if (OCCUtils.theFactory == null) {
			System.out.println("========== [AircraftCADUtils::getFuselageCAD] Initialize CAD shape factory");
			OCCUtils.initCADShapeFactory();
		}
		
		// ----------------------------------------------------------
		// Initialize patches and shape lists
		// ----------------------------------------------------------
		OCCShape noseCapPatch = null,
				 noseTrunkPatch = null,
				 cylinderPatch = null,
				 tailTrunkPatch = null,
				 tailCapPatch = null;
		
		List<OCCShape> requestedShapes = new ArrayList<>();
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// ----------------------------------------------------------
		// FUSELAGE CAD CREATION
		// ----------------------------------------------------------	
		Amount<Length> xApex = fuselage.getXApexConstructionAxes();
		Amount<Length> yApex = fuselage.getYApexConstructionAxes();
		Amount<Length> zApex = fuselage.getZApexConstructionAxes();
		
		PVector deltaApex = new PVector(
				(float) xApex.doubleValue(SI.METER),
				(float) yApex.doubleValue(SI.METER),
				(float) zApex.doubleValue(SI.METER)
				);
		
		// ----------------------------------------------------------
		// NOSE CAP AND NOSE TRUNK CREATION
		// ----------------------------------------------------------		
		Amount<Length> noseLength = fuselage.getNoseLength();
		Double xbarNoseCap = fuselage.getNoseCapOffsetPercent(); // normalized with respect to the nose length
			
		double zNoseTip = fuselage.getZOutlineXZLowerAtX(0.0);
		
		System.out.println("========== [AircraftCADUtils::getFuselageCAD] Nose Cap creation");
		System.out.println("getting selected nose cap sections ...");
		
		List<Double> xbarsNoseCapPatch = Arrays.asList(
				MyArrayUtils.halfCosine2SpaceDouble(
						noseCapSectionFactor1*xbarNoseCap, 
						noseCapSectionFactor2*xbarNoseCap,
						numberNoseCapSections
						));
		
		List<List<PVector>> sectionsNoseCapPatch = new ArrayList<>();
		xbarsNoseCapPatch.forEach(xbar -> 
			sectionsNoseCapPatch.add(fuselage.getUniqueValuesYZSideRCurve(noseLength.times(xbar)))
			);
		
		sectionsNoseCapPatch.stream()
							.flatMap(Collection::stream)
							.forEach(pv -> pv.add(deltaApex));
		
		if (exportShells || exportSolids) {
			noseCapPatch = OCCUtils.makePatchThruSectionsP(
							new PVector(
									0.0f, 
									0.0f, 
									(float) zNoseTip
									).add(deltaApex),
							sectionsNoseCapPatch
							);
		}
		
		if (exportSupportShapes) {
			List<OCCShape> supportShapesNoseCap = new ArrayList<>();
			
			sectionsNoseCapPatch.stream()
				.map(pts -> OCCUtils.theFactory.newCurve3DP(pts, false))
				.forEach(crv -> supportShapesNoseCap.add((OCCShape) crv.edge()));
			
			supportShapes.addAll(supportShapesNoseCap);
		}
		
		System.out.println("========== [AircraftCADUtils::getFuselageCAD] Nose Trunk creation");
		System.out.println("getting selected nose trunk sections ...");
		
		List<Double> xbarsNoseTrunkPatch = Arrays.asList(
				spacingTypeNoseTrunk.calculateSpacing(
						noseCapSectionFactor2*xbarNoseCap, 
						1.0, 
						numberNoseTrunkSections
						));
		
		List<List<PVector>> sectionsNoseTrunkPatch = new ArrayList<>();
		xbarsNoseTrunkPatch.forEach(xbar -> 
			sectionsNoseTrunkPatch.add(fuselage.getUniqueValuesYZSideRCurve(noseLength.times(xbar)))
			);
		
		sectionsNoseTrunkPatch.stream()
							.flatMap(Collection::stream)
							.forEach(pv -> pv.add(deltaApex));
		
		if (exportShells || exportSolids) {
			noseTrunkPatch = OCCUtils.makePatchThruSectionsP(sectionsNoseTrunkPatch);
		}
		
		if (exportSupportShapes) {
			List<OCCShape> supportShapesNoseTrunk = new ArrayList<>();
			
			sectionsNoseTrunkPatch.stream()
				.map(pts -> OCCUtils.theFactory.newCurve3DP(pts, false))
				.forEach(crv -> supportShapesNoseTrunk.add((OCCShape) crv.edge()));
		
			supportShapes.addAll(supportShapesNoseTrunk);
		}
		
		// ----------------------------------------------------------
		// CYLINDER CREATION
		// ----------------------------------------------------------
		Amount<Length> cylinderLength = fuselage.getCylinderLength();
		
		System.out.println("========== [AircraftCADUtils::getFuselageCAD] Cylinder creation");
		System.out.println("generating cylinder sections ...");
		
		List<Double> xsCylinderPatch = new ArrayList<>();
		xsCylinderPatch.add(noseLength.doubleValue(SI.METER));
		xsCylinderPatch.add(noseLength.plus(cylinderLength.times(0.5)).doubleValue(SI.METER));
		xsCylinderPatch.add(noseLength.plus(cylinderLength).doubleValue(SI.METER));
		
		List<List<PVector>> sectionsCylinderPatch = xsCylinderPatch.stream()
				.map(x -> fuselage.getUniqueValuesYZSideRCurve(Amount.valueOf(x, SI.METER)))
				.collect(Collectors.toList());
		
		sectionsCylinderPatch.stream()
							 .flatMap(Collection::stream)
							 .forEach(pv -> pv.add(deltaApex));
		
		if (exportShells || exportSolids) {
			cylinderPatch = OCCUtils.makePatchThruSectionsP(sectionsCylinderPatch);
		}
		
		if (exportSupportShapes) {
			List<OCCShape> supportShapesCylinder = new ArrayList<>();
			
			sectionsCylinderPatch.stream()
				.map(pts -> OCCUtils.theFactory.newCurve3DP(pts, false))
				.forEach(crv -> supportShapesCylinder.add((OCCShape) crv.edge()));
			
			supportShapes.addAll(supportShapesCylinder);
		}
			
		// ----------------------------------------------------------
		// TAIL TRUNK AND TAIL CAP CREATION
		// ----------------------------------------------------------
		Amount<Length> tailLength = fuselage.getTailLength();
		Amount<Length> fuselageLength = fuselage.getFuselageLength();
		Double xbarTailCap = fuselage.getTailCapOffsetPercent();
		
		double zTailTip = fuselage.getZOutlineXZLowerAtX(fuselageLength.doubleValue(SI.METER));
		
		System.out.println("========== [AircraftCADUtils::getFuselageCAD] Tail trunk creation");
		System.out.println("getting selected tail trunk sections ...");
		
		List<Double> xbarsTailTrunkPatch = Arrays.asList(
				spacingTypeTailTrunk.calculateSpacing(
						0.0, 
						1.0 - xbarTailCap*tailCapSectionFactor1, 
						numberTailTrunkSections
						));
		
		List<List<PVector>> sectionsTailTrunkPatch = new ArrayList<>();
		xbarsTailTrunkPatch.forEach(xbar -> 
			sectionsTailTrunkPatch.add(
					fuselage.getUniqueValuesYZSideRCurve(noseLength.plus(cylinderLength)
																   .plus(tailLength.times(xbar))))
			);
		
		sectionsTailTrunkPatch.stream()
		 					  .flatMap(Collection::stream)
		 					  .forEach(pv -> pv.add(deltaApex));
		
		if (exportShells || exportSolids) {
			tailTrunkPatch = OCCUtils.makePatchThruSectionsP(sectionsTailTrunkPatch);
		}
		
		if (exportSupportShapes) {
			List<OCCShape> supportShapesTailTrunk = new ArrayList<>();
			
			sectionsTailTrunkPatch.stream()
				.map(pts -> OCCUtils.theFactory.newCurve3DP(pts, false))
				.forEach(crv -> supportShapesTailTrunk.add((OCCShape) crv.edge()));
		
			supportShapes.addAll(supportShapesTailTrunk);
		}
		
		List<Double> xbarsTailCapPatch = Arrays.asList(
				MyArrayUtils.halfCosine1SpaceDouble(
						1.0 - xbarTailCap*tailCapSectionFactor1, 
						1.0 - xbarTailCap*tailCapSectionFactor2, 
						numberTailCapSections
						));
		
		List<List<PVector>> sectionsTailCapPatch = new ArrayList<>();
		xbarsTailCapPatch.forEach(xbar -> 
			sectionsTailCapPatch.add(
					fuselage.getUniqueValuesYZSideRCurve(noseLength.plus(cylinderLength)
																   .plus(tailLength.times(xbar))))
			);
		
		sectionsTailCapPatch.stream()
							.flatMap(Collection::stream)
							.forEach(pv -> pv.add(deltaApex));
		
		if (exportShells || exportSolids) {
			tailCapPatch = OCCUtils.makePatchThruSectionsP(
							sectionsTailCapPatch,
							new PVector(
									(float) fuselageLength.doubleValue(SI.METER), 
									0.0f, 
									(float) zTailTip
							).add(deltaApex));
		}
		
		if (exportSupportShapes) {
			List<OCCShape> supportShapesTailCap = new ArrayList<>();
			
			sectionsTailCapPatch.stream()
				.map(pts -> OCCUtils.theFactory.newCurve3DP(pts, false))
				.forEach(crv -> supportShapesTailCap.add((OCCShape) crv.edge()));
			
			supportShapes.addAll(supportShapesTailCap);
		}
		
		// ----------------------------------------------------------
		// Generate outline curves whether necessary
		// ----------------------------------------------------------
		if (exportSupportShapes) {
			System.out.println("========== [AircraftCADUtils::getFuselageCAD] Outline curves creation");
			
			// --------------------------
			// Nose cap outline curves
			// --------------------------
			double[] noseCapTip = new double[] {deltaApex.x, deltaApex.y, zNoseTip + deltaApex.z};
			
			List<double[]> noseCapOutlineUpperPts = new ArrayList<>();
			noseCapOutlineUpperPts.add(noseCapTip);
			sectionsNoseCapPatch.forEach(pts -> 
					noseCapOutlineUpperPts.add(new double[] {
							pts.get(0).x,
							pts.get(0).y,
							pts.get(0).z
					}));
			
			List<double[]> noseCapOutlineLowerPts = new ArrayList<>();
			noseCapOutlineLowerPts.add(noseCapTip);
			sectionsNoseCapPatch.forEach(pts -> 
					noseCapOutlineLowerPts.add(new double[] {
							pts.get(pts.size()-1).x,
							pts.get(pts.size()-1).y,
							pts.get(pts.size()-1).z
					}));
			
			List<double[]> noseCapOutlineSidePts = new ArrayList<>();
			noseCapOutlineSidePts.add(noseCapTip);
			xbarsNoseCapPatch.forEach(xbar -> {
					double x = xbar*noseLength.doubleValue(SI.METER);
					noseCapOutlineSidePts.add(new double[] {
							x + deltaApex.x,		
							fuselage.getYOutlineXYSideRAtX(x) + deltaApex.y,
							fuselage.getCamberZAtX(x) + deltaApex.z
					});				
			});
			
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(noseCapOutlineUpperPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(noseCapOutlineLowerPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(noseCapOutlineSidePts, false).edge());
			
			// ---------------------------
			// Nose trunk outline curves
			// ---------------------------
			List<double[]> noseTrunkOutlineUpperPts = new ArrayList<>();
			sectionsNoseTrunkPatch.forEach(pts -> 
					noseTrunkOutlineUpperPts.add(new double[] {
							pts.get(0).x,
							pts.get(0).y,
							pts.get(0).z
					}));

			List<double[]> noseTrunkOutlineLowerPts = new ArrayList<>();
			sectionsNoseTrunkPatch.forEach(pts -> 
					noseTrunkOutlineLowerPts.add(new double[] {
							pts.get(pts.size()-1).x,
							pts.get(pts.size()-1).y,
							pts.get(pts.size()-1).z
					}));

			List<double[]> noseTrunkOutlineSidePts = new ArrayList<>();
			xbarsNoseTrunkPatch.forEach(xbar -> {
					double x = xbar*noseLength.doubleValue(SI.METER);
					noseTrunkOutlineSidePts.add(new double[] {
							x + deltaApex.x,		
							fuselage.getYOutlineXYSideRAtX(x) + deltaApex.y,
							fuselage.getCamberZAtX(x) + deltaApex.z
					});				
			});

			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(noseTrunkOutlineUpperPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(noseTrunkOutlineLowerPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(noseTrunkOutlineSidePts, false).edge());
			
			// ---------------------------
			// Cylinder outline curves
			// ---------------------------
			List<double[]> cylinderOutlineUpperPts = new ArrayList<>();
			sectionsCylinderPatch.forEach(pts -> 
					cylinderOutlineUpperPts.add(new double[] {
							pts.get(0).x,
							pts.get(0).y,
							pts.get(0).z
					}));
			
			List<double[]> cylinderOutlineLowerPts = new ArrayList<>();
			sectionsCylinderPatch.forEach(pts ->
					cylinderOutlineLowerPts.add(new double[] {
							pts.get(pts.size()-1).x,
							pts.get(pts.size()-1).y,
							pts.get(pts.size()-1).z
					}));
			
			List<double[]> cylinderOutlineSidePts = new ArrayList<>();
			xsCylinderPatch.forEach(x -> 
					cylinderOutlineSidePts.add(new double[] {
							x + deltaApex.x,
							fuselage.getYOutlineXYSideRAtX(x) + deltaApex.y,
							fuselage.getCamberZAtX(x) + deltaApex.z
					}));
			
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(cylinderOutlineUpperPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(cylinderOutlineLowerPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(cylinderOutlineSidePts, false).edge());
			
			// ---------------------------
			// Tail trunk outline curves
			// ---------------------------
			List<double[]> tailTrunkOutlineUpperPts = new ArrayList<>();
			sectionsTailTrunkPatch.forEach(pts -> 
					tailTrunkOutlineUpperPts.add(new double[] {
							pts.get(0).x,
							pts.get(0).y,
							pts.get(0).z
					}));
			
			List<double[]> tailTrunkOutlineLowerPts = new ArrayList<>();
			sectionsTailTrunkPatch.forEach(pts -> 
					tailTrunkOutlineLowerPts.add(new double[] {
							pts.get(pts.size()-1).x,
							pts.get(pts.size()-1).y,
							pts.get(pts.size()-1).z
					}));
			
			List<double[]> tailTrunkOutlineSidePts = new ArrayList<>();
			xbarsTailTrunkPatch.forEach(xbar -> {
					double x = noseLength.plus(cylinderLength).plus(tailLength.times(xbar)).doubleValue(SI.METER);
					tailTrunkOutlineSidePts.add(new double[] {
							x + deltaApex.x,
							fuselage.getYOutlineXYSideRAtX(x) + deltaApex.y,
							fuselage.getCamberZAtX(x) + deltaApex.z
					});			
			});
			
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(tailTrunkOutlineUpperPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(tailTrunkOutlineLowerPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(tailTrunkOutlineSidePts, false).edge());
			
			// ---------------------------
			// Tail cap outline curves
			// ---------------------------
			double[] tailCapTip = new double[] {
					fuselageLength.doubleValue(SI.METER) + deltaApex.x, 
					deltaApex.y, 
					zTailTip + deltaApex.z
					};

			List<double[]> tailCapOutlineUpperPts = new ArrayList<>();		
			sectionsTailCapPatch.forEach(pts -> 
					tailCapOutlineUpperPts.add(new double[] {
							pts.get(0).x,
							pts.get(0).y,
							pts.get(0).z
					}));
			tailCapOutlineUpperPts.add(tailCapTip);
			
			List<double[]> tailCapOutlineLowerPts = new ArrayList<>();
			sectionsTailCapPatch.forEach(pts -> 
					tailCapOutlineLowerPts.add(new double[] {
							pts.get(pts.size()-1).x,
							pts.get(pts.size()-1).y,
							pts.get(pts.size()-1).z
					}));
			tailCapOutlineLowerPts.add(tailCapTip);
			
			List<double[]> tailCapOutlineSidePts = new ArrayList<>();	
			xbarsTailCapPatch.forEach(xbar -> {
					double x = noseLength.plus(cylinderLength).plus(tailLength.times(xbar)).doubleValue(SI.METER);
					tailCapOutlineSidePts.add(new double[] {
							x + deltaApex.x,		
							fuselage.getYOutlineXYSideRAtX(x) + deltaApex.y,
							fuselage.getCamberZAtX(x) + deltaApex.z
					});				
			});
			tailCapOutlineSidePts.add(tailCapTip);
			
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(tailCapOutlineUpperPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(tailCapOutlineLowerPts, false).edge());
			supportShapes.add((OCCShape) OCCUtils.theFactory.newCurve3D(tailCapOutlineSidePts, false).edge());
			
		}
		
		if (exportShells || exportSolids) {		
			// ----------------------------------------------------------
			// Sew together all the faces generated till now
			// ----------------------------------------------------------
			System.out.println("========== [AircraftCADUtils::getFuselageCAD] Sewing fuselage right faces together");
			
			List<CADFace> fuselageRightFaces = new ArrayList<>();
			fuselageRightFaces.add((CADFace) OCCUtils.theFactory.newShape(noseCapPatch.getShape()));
			fuselageRightFaces.add((CADFace) OCCUtils.theFactory.newShape(noseTrunkPatch.getShape()));
			fuselageRightFaces.add((CADFace) OCCUtils.theFactory.newShape(cylinderPatch.getShape()));
			fuselageRightFaces.add((CADFace) OCCUtils.theFactory.newShape(tailTrunkPatch.getShape()));
			fuselageRightFaces.add((CADFace) OCCUtils.theFactory.newShape(tailCapPatch.getShape()));
			
			OCCShape fuselageRightShell = (OCCShape) OCCUtils.theFactory.newShellFromAdjacentFaces(fuselageRightFaces);
			
			// --------------------------------------------------------------------------
			// Mirroring the right shell with respect to the symmetry plane
			// --------------------------------------------------------------------------
			System.out.println("========== [AircraftCADUtils::getFuselageCAD] Mirroring the fuselage right shell "
					+ "with respect to the symmetry plane");
			
			OCCShape fuselageLeftShell = OCCUtils.getShapeMirrored(
					fuselageRightShell, 
					deltaApex, 
					new PVector(0.0f, 1.0f, 0.0f), 
					new PVector(1.0f, 0.0f, 0.0f)
					);
			
			if (exportSolids) {
				// -----------------------------------------------------------------------
				// Sew together the right and left shell and generate the solid
				// -----------------------------------------------------------------------
				System.out.println("========== [AircraftCADUtils::getFuselageCAD] Sewing the right and left shell "
						+ "in order to obtain the solid of the fuselage");
				
				// TODO: check this method
				OCCSolid fuselageSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
						OCCUtils.theFactory.newShellFromAdjacentShells(
								(CADShell) fuselageRightShell, 
								(CADShell) fuselageLeftShell
								));
				
				solidShapes.add(fuselageSolid);
			} 
			
			if (exportShells) {
				// -----------------------------------------
				// Return just the left and right shell
				// -----------------------------------------
				shellShapes.add(fuselageLeftShell);
				shellShapes.add(fuselageRightShell);
			}
		} 
		
		// -------------------------------------------
		// Mirroring support shapes whether necessary
		// -------------------------------------------
		if (exportSupportShapes) {
			List<OCCShape> mirrSupportShapes = new ArrayList<>();
			supportShapes.forEach(s -> mirrSupportShapes.add(OCCUtils.getShapeMirrored(s, 
					deltaApex, 
					new PVector(0.0f, 1.0f, 0.0f), 
					new PVector(1.0f, 0.0f, 0.0f)
					)));

			supportShapes.addAll(mirrSupportShapes);
		}
		
		requestedShapes.addAll(supportShapes);
		requestedShapes.addAll(shellShapes);
		requestedShapes.addAll(solidShapes);
		
		return requestedShapes;
				 
	}
	
	public static List<OCCShape> getLiftingSurfaceCAD(LiftingSurface liftingSurface, 
			WingTipType wingTip, List<Double> tipParams,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
		
		// ----------------------------------------------------------
		// Check whether continuing with the method
		// ----------------------------------------------------------
		if (liftingSurface == null) {
			System.out.println("========== [AircraftCADUtils::getLiftingSurfaceCAD] The lifting surface object passed to the "
					+ "getLiftingSurfaceCAD method is null! Exiting the method ...");
			return null;
		}
		
		if (!exportSupportShapes && !exportShells && !exportSolids) {
			System.out.println("========== [AircraftCADUtils::getLiftingSurfaceCAD] No shapes to export! Exiting the method ...");
			return null;
		}
		
		System.out.println("========== [AircraftCADUtils::getLiftingSurfaceCAD]");
		
		// ----------------------------------------------------------
		// Check the factory
		// ----------------------------------------------------------
		if (OCCUtils.theFactory == null) {
			System.out.println("========== [AircraftCADUtils::getLiftingSurfaceCAD] Initialize CAD shape factory");
			OCCUtils.initCADShapeFactory();
		}
		
		// ----------------------------------------------------------
		// Initialize patches and shape lists
		// ----------------------------------------------------------	
		List<OCCShape> panelPatches = new ArrayList<>();
		
		List<OCCShape> requestedShapes = new ArrayList<>();
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// ---------------------------
		// Collect main geometry data 
		// ---------------------------
		int nPanels = liftingSurface.getPanels().size();
		
		Amount<Length> xApex = liftingSurface.getXApexConstructionAxes();
		Amount<Length> yApex = liftingSurface.getYApexConstructionAxes();
		Amount<Length> zApex = liftingSurface.getZApexConstructionAxes();
		Amount<Angle> riggingAngle = liftingSurface.getRiggingAngle();
		
		PVector deltaApex = new PVector(
				(float) xApex.doubleValue(SI.METER),
				(float) yApex.doubleValue(SI.METER),
				(float) zApex.doubleValue(SI.METER)
				);
		
		// ------------------------------------------------
		// Build the leading and trailing edges
		// ------------------------------------------------
		List<double[]> ptsLE = new ArrayList<>();
		List<double[]> ptsTE = new ArrayList<>();
		
		for (int iBP = 0; iBP < liftingSurface.getXLEBreakPoints().size(); iBP++) {
				
			double xLE = xApex.plus(liftingSurface.getXLEBreakPoints().get(iBP)).doubleValue(SI.METER);
			
			double yLE = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					yApex.plus(liftingSurface.getZLEBreakPoints().get(iBP)).doubleValue(SI.METER) :
					yApex.plus(liftingSurface.getYBreakPoints().get(iBP)).doubleValue(SI.METER);
					
			double zLE = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					zApex.plus(liftingSurface.getYBreakPoints().get(iBP)).doubleValue(SI.METER) :
					zApex.plus(liftingSurface.getZLEBreakPoints().get(iBP)).doubleValue(SI.METER);
			
			double chord = liftingSurface.getChordsBreakPoints().get(iBP).doubleValue(SI.METER);
			double twist = liftingSurface.getTwistsBreakPoints().get(iBP).doubleValue(SI.RADIAN);
			
			double[] ptLE = new double[] {xLE, yLE, zLE};
					
			double[] ptTE = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					new double[] {
							xLE + chord*Math.cos(twist + riggingAngle.doubleValue(SI.RADIAN)),
							yLE - chord*Math.sin(twist + riggingAngle.doubleValue(SI.RADIAN)), 
							zLE} :
					new double[] {
							xLE + chord*Math.cos(twist + riggingAngle.doubleValue(SI.RADIAN)),
							yLE,
							zLE - chord*Math.sin(twist + riggingAngle.doubleValue(SI.RADIAN))};
			
			ptsLE.add(ptLE);
			ptsTE.add(ptTE);
		}
		
		if (exportSupportShapes) {
			// ------------------------------------------------
			// Generate a wire for leading and trailing edges
			// ------------------------------------------------
			List<CADEdge> leSegs = new ArrayList<>();
			List<CADEdge> teSegs = new ArrayList<>();
			List<CADEdge> bpChords = new ArrayList<>();
			
			for (int i = 1; i <= nPanels; i++) {
				CADEdge segLE = OCCUtils.theFactory.newCurve3D(ptsLE.get(i), ptsLE.get(i-1)).edge();
				CADEdge segTE = OCCUtils.theFactory.newCurve3D(ptsTE.get(i), ptsTE.get(i-1)).edge();
				leSegs.add(segLE);
				teSegs.add(segTE);
			}
			
			for (int i = 0; i < liftingSurface.getYBreakPoints().size(); i++) {
				CADEdge chord = OCCUtils.theFactory.newCurve3D(ptsTE.get(i), ptsLE.get(i)).edge();
				bpChords.add(chord);
			}
			
			supportShapes.add((OCCWire) OCCUtils.theFactory.newWireFromAdjacentEdges(leSegs));
			supportShapes.add((OCCWire) OCCUtils.theFactory.newWireFromAdjacentEdges(teSegs));
			bpChords.forEach(cadEdge -> supportShapes.add((OCCShape) cadEdge)); 
		}
		
		// ---------------------------------
		// Wing tip check
		// ---------------------------------
		if ((wingTip.equals(WingTipType.WINGLET) && !liftingSurface.getType().equals(ComponentEnum.WING)) || 
			(wingTip.equals(WingTipType.WINGLET) && liftingSurface.getType().equals(ComponentEnum.WING) 
					&& liftingSurface.getWingletHeight().doubleValue(SI.METER) == 0.0)) {
			
			wingTip = WingTipType.ROUNDED;
			System.out.println("Winglet cannot be applied to the selected lifting surface. " + 
							   "Wing tip type automatically set to ROUNDED.");
		}
		
		// ---------------------------------
		// Generate span wise airfoils
		// ---------------------------------
		List<List<CADGeomCurve3D>> airfoilCrvs = new ArrayList<List<CADGeomCurve3D>>();
		List<Double[]> airfoilYStations = new ArrayList<>();
		
		int[] nAirfoils = new int[nPanels];
		for (int i = 1; i <= liftingSurface.getPanels().size(); i++) {
			
			// Take into account an adequate tip offset, according to the type of tip to be realized
			double tipOffset = 0.0;
			if (liftingSurface.getEtaBreakPoints().get(i) == 1.0) {
				
				switch(wingTip) {
				
				case CUTOFF:				
					break;
					
				case ROUNDED:
					tipOffset = liftingSurface.getAirfoilList().get(nPanels).getThicknessToChordRatio() * 
								liftingSurface.getChordsBreakPoints().get(nPanels).doubleValue(SI.METER) /
								liftingSurface.getSemiSpan().doubleValue(SI.METER);
					break;
					
				case WINGLET:
					double wingletYOffsetFactor = (tipParams == null || tipParams.size() == 0) ? 0.50 : tipParams.get(0);
					
					tipOffset = liftingSurface.getWingletHeight().doubleValue(SI.METER) * wingletYOffsetFactor /
								liftingSurface.getSemiSpan().doubleValue(SI.METER);
					
					break;
				}
			}
			
			double etaOut = liftingSurface.getEtaBreakPoints().get(i) - tipOffset;		
			Double diff = (etaOut - liftingSurface.getEtaBreakPoints().get(i-1)) * 10;	
			
			// Automatically select an adequate number of airfoils between breakpoints					
			if (diff >= 3.0 && diff <= 7.0) {			
				nAirfoils[i-1] = Double.valueOf(Math.rint(diff)).intValue();
			} else {				
				if (diff < 3.0) {
					nAirfoils[i-1] = 3;
				} else {
					if (diff > 7.0 && diff < 10.0) {
						nAirfoils[i-1] = 7;
					} else {
						nAirfoils[i-1] = 5;
					}
				}
			}
					
			Double[] yStations = MyArrayUtils.linspaceDouble(
					liftingSurface.getYBreakPoints().get(i-1).doubleValue(SI.METER), 
					etaOut * liftingSurface.getSemiSpan().doubleValue(SI.METER), 
					nAirfoils[i-1]);
			
			airfoilCrvs.add(Arrays.asList(yStations).stream()
					.map(y -> generateAirfoilAtY(y, liftingSurface))
					.map(pts -> OCCUtils.theFactory.newCurve3D(pts, false))
					.collect(Collectors.toList()));
			
			airfoilYStations.add(yStations);
		}
		
		List<List<CADWire>> airfoilWires = new ArrayList<List<CADWire>>();
		for (int i = 0; i < nPanels; i++) {		
			airfoilWires.add(airfoilCrvs.get(i).stream()
					.map(crv -> OCCUtils.theFactory.newWireFromAdjacentEdges(
							crv.edge(),
							OCCUtils.theFactory.newCurve3D(
									crv.edge().vertices()[1].pnt(), 
									crv.edge().vertices()[0].pnt()).edge()
							))
					.collect(Collectors.toList()));
		}
					
		if (exportSupportShapes) {	
			airfoilWires.forEach(wrs -> supportShapes.addAll(wrs.stream()
					.map(w -> (OCCShape) OCCUtils.theFactory.newShape(((OCCShape) w).getShape()))
					.collect(Collectors.toList())
					));
			
			airfoilYStations.forEach(yl -> 
					Arrays.asList(yl).forEach(y -> {
							supportShapes.add(
									(OCCShape) OCCUtils.theFactory.newCurve3D(
											generateChordAtY(y, liftingSurface), false).edge());
							supportShapes.add(
									(OCCShape) OCCUtils.theFactory.newCurve3D(
											generateCamberAtY(y, liftingSurface), false).edge());
					}));
		}
		
		
		// ---------------------------------
		// Wing tip construction
		// ---------------------------------
		OCCShape wingTipShell = null;
		
		switch (wingTip) {
		
		case CUTOFF:
			if (exportShells || exportSolids) {
				// ---------------------------------------------------
				// Just generate a face element closing the wing tip
				// ---------------------------------------------------
				wingTipShell = (OCCShape) OCCUtils.theFactory.newFacePlanar(
						airfoilWires.get(nPanels-1).get(nAirfoils[nPanels-1]-1));
			}
			
			break;
			
		case ROUNDED:
			Tuple2<OCCShell, List<OCCShape>> roundedTipShapes = null;
			
			roundedTipShapes = generateRoundedWingTip(liftingSurface, 
					airfoilCrvs.get(nPanels - 1).get(nAirfoils[nPanels - 1] - 1),
					airfoilCrvs.get(nPanels - 1).get(nAirfoils[nPanels - 1] - 2),
					ptsLE, ptsTE, 
					exportSupportShapes, (exportShells || exportSolids));
			
			if (exportSupportShapes) {		
				supportShapes.addAll(roundedTipShapes._2());
			} 
			
			if (exportShells || exportSolids) {
				wingTipShell = roundedTipShapes._1();
			}
					
			break;
			
		case WINGLET:
			Tuple2<OCCShell, List<OCCShape>> wingletShapes = null;
			
			List<double[]> wingletPtsTE = new ArrayList<>();
			wingletPtsTE.add(airfoilCrvs.get(nPanels - 1).get(nAirfoils[nPanels - 1] - 2).edge().vertices()[0].pnt());
			wingletPtsTE.add(airfoilCrvs.get(nPanels - 1).get(nAirfoils[nPanels - 1] - 1).edge().vertices()[0].pnt());
			
			wingletShapes = generateWinglet(liftingSurface, tipParams,
					airfoilWires.get(nPanels - 1).get(nAirfoils[nPanels - 1] - 1), wingletPtsTE,				
					exportSupportShapes, (exportShells || exportSolids));
			
			if (exportSupportShapes) {
				supportShapes.addAll(wingletShapes._2());
			}
			
			if (exportShells || exportSolids) {
				wingTipShell = wingletShapes._1();
			}
		}
			
		if (exportShells || exportSolids) {
			// ------------------------------------------------
			// Generate a loft (shell element) for each panel
			// ------------------------------------------------		
			List<CADShape> rightShells = new ArrayList<>();
			
			panelPatches.addAll(airfoilWires.stream()
					.map(wrs -> OCCUtils.makePatchThruSections(wrs))
					.collect(Collectors.toList()));	
			
			rightShells.addAll(panelPatches);
			rightShells.add(wingTipShell);
			
			// -----------------------------------
			// Sew all the right shells together
			// -----------------------------------
			OCCShape rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(rightShells);
			
			// ------------------------------------
			// Mirroring shells whether necessary
			// ------------------------------------
			OCCShape leftShell = null;
			
			if (!liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {	
				leftShell = OCCUtils.getShapeMirrored(
						rightShell, 
						deltaApex, 
						new PVector(0.0f, 1.0f, 0.0f), 
						new PVector(1.0f, 0.0f, 0.0f)
						);
			} else {
				// Closing the lower wire of the object with a planar face
				leftShell = (OCCFace) OCCUtils.theFactory.newFacePlanar(airfoilWires.get(0).get(0));		
			}
			
			if (exportSolids) {
				// -----------------------------------------------------------------------
				// Sew together the right and left shell and generate the solid
				// -----------------------------------------------------------------------
				double sewTol = (wingTip.equals(WingTipType.ROUNDED)) ? 1.0e-03 : 1.0e-06;
				
				// TODO: check this method
				OCCSolid liftingSurfaceSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
						OCCUtils.theFactory.newShellFromAdjacentShapes(sewTol,
								(CADShape) rightShell,
								(CADShape) leftShell
								));
				
				solidShapes.add(liftingSurfaceSolid);
			} 
			
			if (exportShells) {
				// -----------------------------------------
				// Return just the left and right shell
				// -----------------------------------------
				shellShapes.add(rightShell);
				shellShapes.add(leftShell);
			}
		}
		
		// -------------------------------------------
		// Mirroring support shapes whether necessary
		// -------------------------------------------
		if (exportSupportShapes && !liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL)) {
			List<OCCShape> mirrSupportShapes = new ArrayList<>();
			supportShapes.forEach(s -> mirrSupportShapes.add(OCCUtils.getShapeMirrored(s, 
					deltaApex, 
					new PVector(0.0f, 1.0f, 0.0f), 
					new PVector(1.0f, 0.0f, 0.0f)
					)));
			
			supportShapes.addAll(mirrSupportShapes);
		}
		
		requestedShapes.addAll(supportShapes);
		requestedShapes.addAll(shellShapes);
		requestedShapes.addAll(solidShapes);
		
		return requestedShapes;
	}
	
	public static List<OCCShape> getLiftingSurfaceCAD(LiftingSurface liftingSurface,
			WingTipType wingTip,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
			) {
		
		return getLiftingSurfaceCAD(liftingSurface,
				wingTip, null,
				exportSupportShapes, exportShells, exportSolids
				);
	}
	
	public static List<OCCShape> getLiftingSurfaceWingletCAD(LiftingSurface liftingSurface,
			double tipYOffsetFactor, double tipXOffsetFactor, double taperRatio,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids 
			) {
		
		List<Double> wingletParams = new ArrayList<>();
		wingletParams.add(tipYOffsetFactor);
		wingletParams.add(tipXOffsetFactor);
		wingletParams.add(taperRatio);
		
		return getLiftingSurfaceCAD(liftingSurface, 
				WingTipType.WINGLET, wingletParams, 
				exportSupportShapes, exportShells, exportSolids);
	}
	
	public static List<OCCShape> filterAircraftPartSolids(List<OCCShape> shapes) {
		
		List<OCCShape> solid = new ArrayList<>();
		
		solid.addAll(shapes.stream()
			  .filter(s -> s instanceof OCCSolid)
			  .collect(Collectors.toList()));
		
		return solid;
	}
	
	public static List<OCCShape> getFairingShapes(Fuselage fuselage, LiftingSurface liftingSurface,
			double frontLengthFactor, double backLengthFactor, double widthFactor, double heightFactor,
			double heightBelowReferenceFactor, double heightAboveReferenceFactor,
			double filletRadiusFactor,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids		
			) {
		
		// ----------------------------------------------------------
		// Initialize the CAD shapes factory
		// ----------------------------------------------------------
		if (OCCUtils.theFactory == null)
			OCCUtils.initCADShapeFactory();
		
		// ----------------------------------------------------------
		// Initialize shape lists
		// ----------------------------------------------------------
		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> fairingShapes = null;
		
		List<OCCShape> requestedShapes = new ArrayList<>();
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// ----------------------------------------------------------
		// Geometric data collection
		// ----------------------------------------------------------
		FairingDataCollection fairingData = new FairingDataCollection(fuselage, liftingSurface, 
				frontLengthFactor, backLengthFactor, widthFactor, heightFactor, 
				heightBelowReferenceFactor, heightAboveReferenceFactor, 
				filletRadiusFactor);
		
		// ----------------------------------------------------------
		// Invoke a specific fairing generator method
		// ----------------------------------------------------------
		FairingPosition fairingPosition = fairingData.getFairingPosition();
		System.out.println(fairingPosition.toString());
		
		switch (fairingPosition) {
		
		case DETACHED_UP:
			fairingShapes = generateDetachedUpFairingShapes(fairingData,
					exportSupportShapes, exportShells, exportSolids);
			
			break;
			
		case ATTACHED_UP:
			fairingShapes = generateAttachedUpFairingShapes(fairingData,
					exportSupportShapes, exportShells, exportSolids);
			
			break;
			
		case MIDDLE:
			fairingShapes = generateMiddleFairingShapes(fairingData,
					exportSupportShapes, exportShells, exportSolids);
			
			break;
			
		case ATTACHED_DOWN:
			fairingShapes = generateAttachedDownFairingShapes(fairingData,
					exportSupportShapes, exportShells, exportSolids);
			
			break;
			
		case DETACHED_DOWN:
			fairingShapes = generateDetachedDownFairingShapes(fairingData,
					exportSupportShapes, exportShells, exportSolids);
			
			break;
		}
		
		supportShapes.addAll(fairingShapes._1());
		shellShapes.addAll(fairingShapes._2());
		solidShapes.addAll(fairingShapes._3());
		
		requestedShapes.addAll(supportShapes);
		requestedShapes.addAll(shellShapes);
		requestedShapes.addAll(solidShapes);
		
		return requestedShapes;
	}
	
	private static Tuple2<OCCShell, List<OCCShape>> generateRoundedWingTip(LiftingSurface liftingSurface, 
			CADGeomCurve3D tipCurve, CADGeomCurve3D preTipCurve,
			List<double[]> ptsLE, List<double[]> ptsTE, 
			boolean exportSupportShapes, boolean exportShells) {
		
		OCCShell wingTipShell = null;
		List<OCCShape> wingTipShapes = new ArrayList<>();
		Tuple2<OCCShell, List<OCCShape>> retShapes = null;
		
		// ---------------------------------
		// Determine wing tip width
		// ---------------------------------		 
		int nPanels = liftingSurface.getPanels().size();
		double tipWidth = liftingSurface.getAirfoilList().get(nPanels).getThicknessToChordRatio() * 
						  liftingSurface.getChordsBreakPoints().get(nPanels).doubleValue(SI.METER);
		
		// -------------------------------------------------
		// Split the tip airfoil curve at the leading edge
		// -------------------------------------------------
		double[] ptA = OCCUtils.theFactory.newCurve3D(
				generateChordAtY(liftingSurface.getSemiSpan().doubleValue(SI.METER) - tipWidth, liftingSurface), 
				false
				).edge().vertices()[1].pnt();
		
		List<OCCEdge> airfoilTipCrvs = OCCUtils.splitCADCurve(tipCurve, ptA);
		
		// ------------------------------------------------------------
		// Split the second to last airfoil curve at the leading edge
		// ------------------------------------------------------------
		double preTipYStation = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
				preTipCurve.edge().vertices()[0].pnt()[2] - liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER):
				preTipCurve.edge().vertices()[0].pnt()[1] - liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER);
				
		double[] ptA2 = OCCUtils.theFactory.newCurve3D(
				generateChordAtY(preTipYStation, liftingSurface), 
				false
				).edge().vertices()[1].pnt();
				
		List<OCCEdge> airfoilPreTipCrvs = OCCUtils.splitCADCurve(preTipCurve, ptA2);
		
		// ---------------------------------------------------
		// Create a sketching plane next to the tip airfoil
		// ---------------------------------------------------
		double[] iTipCurve = tipCurve.edge().vertices()[0].pnt();
		double[] fTipCurve = tipCurve.edge().vertices()[1].pnt();
	
		PVector teVec = PVector.sub(
				new PVector(
						(float) ptsTE.get(nPanels)[0], 
						(float) ptsTE.get(nPanels)[1],
						(float) ptsTE.get(nPanels)[2]),
				new PVector(
						(float) ptsTE.get(nPanels-1)[0], 
						(float) ptsTE.get(nPanels-1)[1],
						(float) ptsTE.get(nPanels-1)[2]));
		
		PVector pvA = new PVector(
				(float) ptA[0], 
				(float) ptA[1], 
				(float) ptA[2]);
		
		PVector pvB = new PVector(
				(float) ptsLE.get(nPanels)[0], 
				(float) ptsLE.get(nPanels)[1], 
				(float) ptsLE.get(nPanels)[2]);
		
		PVector pvD = PVector.lerp(
				new PVector((float) iTipCurve[0], (float) iTipCurve[1], (float) iTipCurve[2]), 
				new PVector((float) fTipCurve[0], (float) fTipCurve[1], (float) fTipCurve[2]), 
				0.5f);
		
		// Generate PVectors for the second to last airfoil
		double[] iPreTipCurve = preTipCurve.edge().vertices()[0].pnt();
		double[] fPreTipCurve = preTipCurve.edge().vertices()[1].pnt();
		
		PVector pvA2 = new PVector(
				(float) ptA2[0],
				(float) ptA2[1],
				(float) ptA2[2]
				);
		
		PVector pvD2 = PVector.lerp(
				new PVector((float) iPreTipCurve[0], (float) iPreTipCurve[1], (float) iPreTipCurve[2]), 
				new PVector((float) fPreTipCurve[0], (float) fPreTipCurve[1], (float) fPreTipCurve[2]), 
				0.5f);
		
		// Assign to the point C the same y coordinate of point B
		PVector pvC = PVector.add(pvD, PVector.mult(teVec, (float) tipWidth/teVec.mag()));
		if (liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL))
			pvC.x = (float) ptsTE.get(nPanels)[0];
		else
			pvC.y = (float) ptsTE.get(nPanels)[1];
		
		// Calculate the basis tangent vector (at the apex) for the tip supporting curves
		PVector tipApexPVNormal = PVector.sub(pvA, pvB).cross(PVector.sub(pvA, pvD)).normalize();
		double[] tipApexNormal = new double[] {tipApexPVNormal.x, tipApexPVNormal.y, tipApexPVNormal.z};
		
		// -----------------------------------------------------------
		// Eventually export the sketching plane for the wing tip
		// -----------------------------------------------------------
		if (exportSupportShapes) {
			wingTipShapes.add((OCCShape) OCCUtils.theFactory.newWireFromAdjacentEdges(
					OCCUtils.theFactory.newCurve3DP(Arrays.asList(pvA, pvB), false).edge(),
					OCCUtils.theFactory.newCurve3DP(Arrays.asList(pvB, pvC), false).edge(),
					OCCUtils.theFactory.newCurve3DP(Arrays.asList(pvC, pvD), false).edge()
					));
		}
		
		// --------------------------------------
		// Generate the tip airfoil camber line
		// --------------------------------------
		List<double[]> tipCamberPts = generateCamberAtY(
				liftingSurface.getSemiSpan().doubleValue(SI.METER) - tipWidth,
				liftingSurface);
		
		CADGeomCurve3D tipCamberCrv = OCCUtils.theFactory.newCurve3D(tipCamberPts, false);
		
		// --------------------------------------------
		// Generate sketching plane supporting curves
		// --------------------------------------------
		PVector pvE = PVector.lerp(pvB, pvC, 0.25f);
		PVector pvG = PVector.lerp(pvC, pvD, 0.25f);
		PVector pvF = PVector.lerp(
				PVector.lerp(pvB, pvC, (float) 0.75f), 
				PVector.lerp(pvA, pvD, (float) 0.75f), 
				0.10f);
		
		List<double[]> sketchPlaneCrv1Pts = new ArrayList<>();
		sketchPlaneCrv1Pts.add(new double[] {pvA.x, pvA.y, pvA.z});
		sketchPlaneCrv1Pts.add(new double[] {pvE.x, pvE.y, pvE.z});
		
		List<double[]> sketchPlaneCrv2Pts = new ArrayList<>();
		sketchPlaneCrv2Pts.add(new double[] {pvE.x, pvE.y, pvE.z});
		sketchPlaneCrv2Pts.add(new double[] {pvF.x, pvF.y, pvF.z});
		sketchPlaneCrv2Pts.add(new double[] {pvG.x, pvG.y, pvG.z});
		
		PVector pvTangA = PVector.sub(pvB, pvA);
		PVector pvTangE = PVector.sub(pvE, pvB);
		PVector pvTangG = PVector.sub(pvG, pvF);
		
		double tangAFactor = 1;
		double tangEFactor = pvTangE.mag()/pvTangA.mag() * 0.75;
		double tangGFactor = tangEFactor;
		
		pvTangA.normalize();
		pvTangE.normalize();
		pvTangG.normalize();
		
		double[] tangA = MyArrayUtils.scaleArray(new double[] {pvTangA.x, pvTangA.y, pvTangA.z}, tangAFactor);
		double[] tangE = MyArrayUtils.scaleArray(new double[] {pvTangE.x, pvTangE.y, pvTangE.z}, tangEFactor);
		double[] tangG = MyArrayUtils.scaleArray(new double[] {pvTangG.x, pvTangG.y, pvTangG.z}, tangGFactor);
		
		CADGeomCurve3D sketchPlaneCrv1 = OCCUtils.theFactory.newCurve3D(
				sketchPlaneCrv1Pts, false, tangA, tangE, false);	
		CADGeomCurve3D sketchPlaneCrv2 = OCCUtils.theFactory.newCurve3D(
				sketchPlaneCrv2Pts, false, tangE, tangG, false);
		
		if (exportSupportShapes) {
			wingTipShapes.add((OCCShape) sketchPlaneCrv1.edge());
			wingTipShapes.add((OCCShape) sketchPlaneCrv2.edge());
		}
		
		// ----------------------------------------------
		// Generate vertical supporting sections points
		// ----------------------------------------------
		int nVSec1 = 10;
//		int nVSec2 = 10;	
		int nVSec2 = 13;
		double mainVSecStation = 0.25;
		
		Double[] vec1 = MyArrayUtils.halfCosine1SpaceDouble(0.0, 1.0, nVSec1);	
		Double[] vec2 = MyArrayUtils.linspaceDouble(0.0, 1.0, nVSec2);
		
		double[] vSecVec1 = MyArrayUtils.convertToDoublePrimitive(
				Arrays.asList(vec1).stream()
								   .skip(1)
								   .collect(Collectors.toList()));
		
		double[] vSecVec2 = MyArrayUtils.convertToDoublePrimitive(
				Arrays.asList(vec2).stream()
								   .limit(nVSec2 - 1)
								   .collect(Collectors.toList()));
				
		// Generate normal vectors for airfoil and camber line splitting operations
		PVector tipChordNormal = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
				(new PVector(0.0f, 0.0f, 1.0f)).cross(PVector.sub(pvA, pvD)).normalize():
				(new PVector(0.0f, 1.0f, 0.0f)).cross(PVector.sub(pvA, pvD)).normalize();
				
		tipChordNormal.mult(PVector.sub(pvA, pvD).mag() * 0.5f);
		
		// Generate splitting segments for the construction plane supporting curve
		List<CADGeomCurve3D> inSPSplitSegms1 = new ArrayList<>();
		List<CADGeomCurve3D> inSPSplitSegms2 = new ArrayList<>();
		
		for (int i = 0; i < vSecVec1.length; i++) {
			List<PVector> pvList = new ArrayList<>();
			
			PVector pv1 = PVector.lerp(pvA, PVector.lerp(pvA, pvD, (float) mainVSecStation), (float) vSecVec1[i]);
			PVector pv2 = PVector.lerp(pvB, pvE, (float) vSecVec1[i]);
			
			pvList.add(pv1);
			pvList.add(pv2);
			
			inSPSplitSegms1.add(OCCUtils.theFactory.newCurve3DP(pvList, false));
		}
		
		for (int i = 0; i < vSecVec2.length; i++) {
			List<PVector> pvList = new ArrayList<>();
			
			PVector pv1 = PVector.lerp(PVector.lerp(pvA, pvD, (float) mainVSecStation), pvD, (float) vSecVec2[i]);
			PVector pv2 = PVector.lerp(pvE, pvC, (float) vSecVec2[i]);
			
			pvList.add(pv1);
			pvList.add(pv2);
			
			inSPSplitSegms2.add(OCCUtils.theFactory.newCurve3DP(pvList, false));
		}	
		
		// Generate tip airfoil upper points for the supporting curves
		List<double[]> tipAirfoilUpperPts1 = new ArrayList<>();
		List<double[]> tipAirfoilUpperPts2 = new ArrayList<>();
		
		tipAirfoilUpperPts1.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(0)), 
				tipChordNormal, 
				pvA, 
				PVector.lerp(pvA, pvD, (float) mainVSecStation), 
				vSecVec1
				));
		
		tipAirfoilUpperPts2.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(0)), 
				tipChordNormal, 
				PVector.lerp(pvA, pvD, (float) mainVSecStation), 
				pvD, 
				vSecVec2
				));
		
		tipAirfoilUpperPts2.add(iTipCurve);
		
		// Generate tip airfoil lower points for the supporting curves
		List<double[]> tipAirfoilLowerPts1 = new ArrayList<>();
		List<double[]> tipAirfoilLowerPts2 = new ArrayList<>();
		
		tipAirfoilLowerPts1.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(1)), 
				tipChordNormal, 
				pvA, 
				PVector.lerp(pvA, pvD, (float) mainVSecStation), 
				vSecVec1
				));
		
		tipAirfoilLowerPts2.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilTipCrvs.get(1)), 
				tipChordNormal, 
				PVector.lerp(pvA, pvD, (float) mainVSecStation), 
				pvD, 
				vSecVec2
				));
		
		tipAirfoilLowerPts2.add(fTipCurve);
		
		// Generate points on the camber line
		List<double[]> camberLinePts1 = new ArrayList<>();
		List<double[]> camberLinePts2 = new ArrayList<>();
		
		camberLinePts1.addAll(getCrvIntersectionWithNormalSegments(
				tipCamberCrv, 
				tipChordNormal, 
				pvA, 
				PVector.lerp(pvA, pvD, (float) mainVSecStation), 
				vSecVec1
				));
		
		camberLinePts2.addAll(getCrvIntersectionWithNormalSegments(
				tipCamberCrv, 
				tipChordNormal, 
				PVector.lerp(pvA, pvD, (float) mainVSecStation), 
				pvD, 
				vSecVec2
				));
		
		camberLinePts2.add(new double[] {pvD.x, pvD.y, pvD.z});
		
		// Split the construction plane supporting
		List<PVector> camberLinePvs1 = new ArrayList<>();
		List<PVector> camberLinePvs2 = new ArrayList<>();
		
		camberLinePvs1.addAll(inSPSplitSegms1.stream()
				.map(s -> {
					double[] start = s.edge().vertices()[0].pnt();
					double[] intersect = getCrvIntersectionWithSegment(sketchPlaneCrv1, s);
					PVector pv1 = new PVector((float) intersect[0], (float) intersect[1], (float) intersect[2]);
					PVector pv0 = new PVector((float) start[0], (float) start[1], (float) start[2]);					
					return PVector.sub(pv1, pv0);
				})
				.collect(Collectors.toList()));
		
		camberLinePvs2.addAll(inSPSplitSegms2.stream()
				.map(s -> {
					double[] start = s.edge().vertices()[0].pnt();
					double[] intersect = getCrvIntersectionWithSegment(sketchPlaneCrv2, s);
					PVector pv1 = new PVector((float) intersect[0], (float) intersect[1], (float) intersect[2]);
					PVector pv0 = new PVector((float) start[0], (float) start[1], (float) start[2]);					
					return PVector.sub(pv1, pv0);
					})
				.collect(Collectors.toList()));
		
		camberLinePvs2.add(PVector.sub(pvG, pvD));
		
		// Generate vertical supporting curves apexes
		List<double[]> verticalCrvsApexes1 = new ArrayList<>();
		List<double[]> verticalCrvsApexes2 = new ArrayList<>();
	
		for (int i = 0; i < camberLinePts1.size(); i++) {
			PVector pv0 = new PVector(
					(float) camberLinePts1.get(i)[0], 
					(float) camberLinePts1.get(i)[1], 
					(float) camberLinePts1.get(i)[2]);
			
			PVector pv1 = pv0.add(camberLinePvs1.get(i));
			
			verticalCrvsApexes1.add(new double[] {pv1.x, pv1.y, pv1.z});
		}
		
		for (int i = 0; i < camberLinePts2.size(); i++) {
			PVector pv0 = new PVector(
					(float) camberLinePts2.get(i)[0], 
					(float) camberLinePts2.get(i)[1], 
					(float) camberLinePts2.get(i)[2]);
			
			PVector pv1 = pv0.add(camberLinePvs2.get(i));
			
			verticalCrvsApexes2.add(new double[] {pv1.x, pv1.y, pv1.z});
		}
		
		// ----------------------------------------------------
		// Generate vertical supporting curves tangent vectors
		// ----------------------------------------------------
		
		// Generate normal vectors for airfoil and camber line splitting operations
		PVector preTipChordNormal = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
				(new PVector(0.0f, 0.0f, 1.0f)).cross(PVector.sub(pvA2, pvD2)).normalize():
				(new PVector(0.0f, 1.0f, 0.0f)).cross(PVector.sub(pvA2, pvD2)).normalize();

		preTipChordNormal.mult(PVector.sub(pvA2, pvD2).mag() * 0.5f);
		
		// Generate second to last airfoil points for the tangent vector
		List<double[]> preTipAirfoilUpperPts1 = new ArrayList<>();
		List<double[]> preTipAirfoilUpperPts2 = new ArrayList<>();
		List<double[]> preTipAirfoilLowerPts1 = new ArrayList<>();
		List<double[]> preTipAirfoilLowerPts2 = new ArrayList<>();
		
		preTipAirfoilUpperPts1.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(0)), 
				preTipChordNormal, 
				pvA2, 
				PVector.lerp(pvA2, pvD2, (float) mainVSecStation), 
				vSecVec1
				));
		
		preTipAirfoilUpperPts2.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(0)), 
				preTipChordNormal, 
				PVector.lerp(pvA2, pvD2, (float) mainVSecStation), 
				pvD2, 
				vSecVec2
				));
		
		preTipAirfoilUpperPts2.add(iPreTipCurve);
		
		preTipAirfoilLowerPts1.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(1)), 
				preTipChordNormal, 
				pvA2, 
				PVector.lerp(pvA2, pvD2, (float) mainVSecStation), 
				vSecVec1
				));
		
		preTipAirfoilLowerPts2.addAll(getCrvIntersectionWithNormalSegments(
				OCCUtils.theFactory.newCurve3D(airfoilPreTipCrvs.get(1)), 
				preTipChordNormal, 
				PVector.lerp(pvA2, pvD2, (float) mainVSecStation), 
				pvD2, 
				vSecVec2
				));
		
		preTipAirfoilLowerPts2.add(fPreTipCurve);
		
		// Generate tangent vectors for the supporting curves
		List<double[]> verCrvTngsUpper1 = new ArrayList<>();
		List<double[]> verCrvTngsUpper2 = new ArrayList<>();
		List<double[]> verCrvTngsLower1 = new ArrayList<>();
		List<double[]> verCrvTngsLower2 = new ArrayList<>();
		
		List<double[]> verCrvTngsApexUpper1 = new ArrayList<>();
		List<double[]> verCrvTngsApexUpper2 = new ArrayList<>();
		List<double[]> verCrvTngsApexLower1 = new ArrayList<>();
		List<double[]> verCrvTngsApexLower2 = new ArrayList<>();
		
		verCrvTngsUpper1.addAll(IntStream.range(0, tipAirfoilUpperPts1.size())
				 .mapToObj(i -> PVector.sub(
						 new PVector(
								 (float) tipAirfoilUpperPts1.get(i)[0],
								 (float) tipAirfoilUpperPts1.get(i)[1],
								 (float) tipAirfoilUpperPts1.get(i)[2]),
						 new PVector(
								 (float) preTipAirfoilUpperPts1.get(i)[0], 
								 (float) preTipAirfoilUpperPts1.get(i)[1], 
								 (float) preTipAirfoilUpperPts1.get(i)[2])					 
						 ).normalize())
				 .map(pv -> new double[] {pv.x, pv.y, pv.z})
				 .collect(Collectors.toList()));
		
		verCrvTngsUpper2.addAll(IntStream.range(0, tipAirfoilUpperPts2.size())
				 .mapToObj(i -> PVector.sub(
						 new PVector(
								 (float) tipAirfoilUpperPts2.get(i)[0],
								 (float) tipAirfoilUpperPts2.get(i)[1],
								 (float) tipAirfoilUpperPts2.get(i)[2]),
						 new PVector(
								 (float) preTipAirfoilUpperPts2.get(i)[0], 
								 (float) preTipAirfoilUpperPts2.get(i)[1], 
								 (float) preTipAirfoilUpperPts2.get(i)[2])					 
						 ).normalize())
				 .map(pv -> new double[] {pv.x, pv.y, pv.z})
				 .collect(Collectors.toList()));
		
		verCrvTngsLower1.addAll(IntStream.range(0, tipAirfoilLowerPts1.size())
				 .mapToObj(i -> PVector.sub(
						 new PVector(
								 (float) preTipAirfoilLowerPts1.get(i)[0], 
								 (float) preTipAirfoilLowerPts1.get(i)[1], 
								 (float) preTipAirfoilLowerPts1.get(i)[2]),
						 new PVector(
								 (float) tipAirfoilLowerPts1.get(i)[0],
								 (float) tipAirfoilLowerPts1.get(i)[1],
								 (float) tipAirfoilLowerPts1.get(i)[2])
						 ).normalize())
				 .map(pv -> new double[] {pv.x, pv.y, pv.z})
				 .collect(Collectors.toList()));
		
		verCrvTngsLower2.addAll(IntStream.range(0, tipAirfoilLowerPts2.size())
				 .mapToObj(i -> PVector.sub(
						 new PVector(
								 (float) preTipAirfoilLowerPts2.get(i)[0], 
								 (float) preTipAirfoilLowerPts2.get(i)[1], 
								 (float) preTipAirfoilLowerPts2.get(i)[2]),
						 new PVector(
								 (float) tipAirfoilLowerPts2.get(i)[0],
								 (float) tipAirfoilLowerPts2.get(i)[1],
								 (float) tipAirfoilLowerPts2.get(i)[2])
						 ).normalize())
				 .map(pv -> new double[] {pv.x, pv.y, pv.z})
				 .collect(Collectors.toList()));
		
		for (int i = 0; i < tipAirfoilUpperPts1.size(); i++) {
			PVector pvUpper = new PVector(
					(float) tipAirfoilUpperPts1.get(i)[0], 
					(float) tipAirfoilUpperPts1.get(i)[1],
					(float) tipAirfoilUpperPts1.get(i)[2]
					);
			PVector pvCamber = new PVector(
					(float) camberLinePts1.get(i)[0],
					(float) camberLinePts1.get(i)[1],
					(float) camberLinePts1.get(i)[2]
					);
			PVector pvLower = new PVector(
					(float) tipAirfoilLowerPts1.get(i)[0], 
					(float) tipAirfoilLowerPts1.get(i)[1],
					(float) tipAirfoilLowerPts1.get(i)[2]
					);
			
			 double upperThick = PVector.sub(pvCamber, pvUpper).mag();
			 double lowerThick = PVector.sub(pvCamber, pvLower).mag();
			 double curveWidth = camberLinePvs1.get(i).mag(); 
			 
			 verCrvTngsApexUpper1.add(MyArrayUtils.scaleArray(tipApexNormal, Math.pow(upperThick/curveWidth, 0.60)));
			 verCrvTngsApexLower1.add(MyArrayUtils.scaleArray(tipApexNormal, Math.pow(lowerThick/curveWidth, 0.60)));
		}
		
		for (int i = 0; i < tipAirfoilUpperPts2.size(); i++) {
			PVector pvUpper = new PVector(
					(float) tipAirfoilUpperPts2.get(i)[0], 
					(float) tipAirfoilUpperPts2.get(i)[1],
					(float) tipAirfoilUpperPts2.get(i)[2]
					);
			PVector pvCamber = new PVector(
					(float) camberLinePts2.get(i)[0],
					(float) camberLinePts2.get(i)[1],
					(float) camberLinePts2.get(i)[2]
					);
			PVector pvLower = new PVector(
					(float) tipAirfoilLowerPts2.get(i)[0], 
					(float) tipAirfoilLowerPts2.get(i)[1],
					(float) tipAirfoilLowerPts2.get(i)[2]
					);
			
			 double upperThick = PVector.sub(pvCamber, pvUpper).mag();
			 double lowerThick = PVector.sub(pvCamber, pvLower).mag();
			 double curveWidth = camberLinePvs2.get(i).mag(); 
			 
			 verCrvTngsApexUpper2.add(MyArrayUtils.scaleArray(tipApexNormal, Math.pow(upperThick/curveWidth, 0.60)));
			 verCrvTngsApexLower2.add(MyArrayUtils.scaleArray(tipApexNormal, Math.pow(lowerThick/curveWidth, 0.60)));
		}
		
		// -------------------------------------
		// Generate vertical supporting curves
		// -------------------------------------
		List<CADWire> vertCrvs1 = new ArrayList<>();
		List<CADWire> vertCrvs2 = new ArrayList<>();
		
		vertCrvs1.addAll(generateRoundedTipSuppCrvs(
				tipAirfoilUpperPts1, 
				verticalCrvsApexes1, 
				tipAirfoilLowerPts1, 
				verCrvTngsUpper1, 
				verCrvTngsLower1, 
				verCrvTngsApexUpper1,
				verCrvTngsApexLower1,
				liftingSurface.getType()
				));
		
		vertCrvs2.addAll(generateRoundedTipSuppCrvs(
				tipAirfoilUpperPts2, 
				verticalCrvsApexes2, 
				tipAirfoilLowerPts2, 
				verCrvTngsUpper2, 
				verCrvTngsLower2, 
				verCrvTngsApexUpper2,
				verCrvTngsApexLower2,
				liftingSurface.getType()
				));
		
		if (exportSupportShapes) {
			vertCrvs1.forEach(w -> wingTipShapes.add((OCCShape) w));
			vertCrvs2.forEach(w -> wingTipShapes.add((OCCShape) w));
		}
		
		// ---------------------------------------------------------
		// ROUNDED WING TIP SHELL CREATION
		// ---------------------------------------------------------
		if (exportShells) {
			
			// ---------------------------------------------------------
			// Generate a guide curve passing through the apex points
			// ---------------------------------------------------------
			List<double[]> apexCrvPts1 = new ArrayList<>();
			apexCrvPts1.add(airfoilTipCrvs.get(0).vertices()[1].pnt());
			apexCrvPts1.addAll(verticalCrvsApexes1);
			
			// Split the guide curve
			CADEdge apexCrv1 = OCCUtils.splitCADCurve(
					OCCUtils.theFactory.newCurve3D(apexCrvPts1, false), 
					vertCrvs1.get(1).vertices().get(1).pnt()
					).get(0);
			
			// ---------------------------------------------------------------------
			// Patching through the first two sections, creating a filler surface
			// ---------------------------------------------------------------------
			
			// Generate a closed wire for filler surface (upper)
			List<CADEdge> fillerEdgesUpp = new ArrayList<>(); 
			
			fillerEdgesUpp.add(OCCUtils.splitCADCurve(
					airfoilTipCrvs.get(0), 
					vertCrvs1.get(1).vertices().get(0).pnt()
					).get(1));
			
			fillerEdgesUpp.add(vertCrvs1.get(1).edges().get(0));
			
			fillerEdgesUpp.add(apexCrv1);
			
			// Generate a closed wire for filler surface (lower)
			List<CADEdge> fillerEdgesLow = new ArrayList<>(); 
			
			fillerEdgesLow.add(apexCrv1);
			
			fillerEdgesLow.add(vertCrvs1.get(1).edges().get(1));

			fillerEdgesLow.add(OCCUtils.splitCADCurve(
					airfoilTipCrvs.get(1), 
					vertCrvs1.get(1).vertices().get(2).pnt()
					).get(0));	
			
			// Generate control points for the filler surface
			List<double[]> controlCrvUppPts = new ArrayList<>();
			List<double[]> controlCrvLowPts = new ArrayList<>();
			
			CADGeomCurve3D controlCrvUpp = OCCUtils.theFactory.newCurve3D(vertCrvs1.get(0).edges().get(0));
			CADGeomCurve3D controlCrvLow = OCCUtils.theFactory.newCurve3D(vertCrvs1.get(0).edges().get(1));
			double[] controlCrvUppRng = controlCrvUpp.getRange();
			double[] controlCrvLowRng = controlCrvLow.getRange();
			Double[] controlCrvPntVec = new Double[] {0.25, 0.50, 0.75};
			
			Arrays.asList(controlCrvPntVec).forEach(d -> {
					controlCrvUppPts.add(controlCrvUpp.value(d*(controlCrvUppRng[1] - controlCrvUppRng[0]) + controlCrvUppRng[0]));
					controlCrvLowPts.add(controlCrvLow.value(d*(controlCrvLowRng[1] - controlCrvLowRng[0]) + controlCrvLowRng[0]));
			});
			
			// Generate the filler surface in two steps
			CADFace wingTipFillerFaceUpp = OCCUtils.makeFilledFace(fillerEdgesUpp, controlCrvUppPts);
			CADFace wingTipFillerFaceLow = OCCUtils.makeFilledFace(fillerEdgesLow, controlCrvLowPts);
			
			// ---------------------------------------------------------------------
			// Patching through the remaining sections of the first list
			// ---------------------------------------------------------------------
			OCCShell wingTipShell1 = (OCCShell) OCCUtils.makePatchThruSections(vertCrvs1.stream()
					.skip(1)
					.collect(Collectors.toList())
					);
			
			// ------------------------------------------------------------
			// Patching through the sections defined by the second list
			// ------------------------------------------------------------
			OCCShell wingTipShell2 = (OCCShell) OCCUtils.makePatchThruSections(vertCrvs2.stream()
//					.limit(4)
					.limit(5)
					.collect(Collectors.toList())
					);
			
			OCCShell wingTipShell3 = (OCCShell) OCCUtils.makePatchThruSections(vertCrvs2.stream()
//					.skip(3)
//					.limit(4)
					.skip(4)
					.limit(5)
					.collect(Collectors.toList())
					);
			
			OCCShell wingTipShell4 = (OCCShell) OCCUtils.makePatchThruSections(vertCrvs2.stream()
//					.skip(6)
					.skip(8)
					.collect(Collectors.toList())
					);
			
			// ------------------------------------------------------------
			// Generate a face at the trailing edge of the wing tip
			// ------------------------------------------------------------
			List<CADEdge> teEdgeList = new ArrayList<>();
			teEdgeList.addAll(vertCrvs2.get(vertCrvs2.size() - 1).edges());
			teEdgeList.add(OCCUtils.theFactory.newCurve3D(
					vertCrvs2.get(vertCrvs2.size() - 1).vertices().get(2).pnt(), 
					vertCrvs2.get(vertCrvs2.size() - 1).vertices().get(0).pnt()
					).edge());
					
			// TODO: check this function
			CADFace teTipFace = OCCUtils.makeFilledFace(teEdgeList, new ArrayList<double[]>());
			
			// -------------------------------------
			// Sewing all the tip patches together
			// -------------------------------------
			wingTipShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(1e-5,
					wingTipFillerFaceUpp,
					wingTipFillerFaceLow,
					wingTipShell1,
					wingTipShell2,
					wingTipShell3,
					wingTipShell4,
					teTipFace
					);
		}
		
		// ----------------------------------------
		// Arrange the results
		// ----------------------------------------
		retShapes = new Tuple2<OCCShell, List<OCCShape>>(wingTipShell, wingTipShapes);
		
		return retShapes;	
	}
	
	private static Tuple2<OCCShell, List<OCCShape>> generateWinglet(LiftingSurface liftingSurface, 
			List<Double> tipParams,
			CADWire tipAirfoilWire, List<double[]> wingletPtsTE,
			boolean exportSupportShapes, boolean exportShells
			) {
		
		OCCShell wingTipShell = null;
		List<OCCShape> wingTipShapes = new ArrayList<>();
		Tuple2<OCCShell, List<OCCShape>> retShapes = null;
		
		// ---------------------------------
		// Fix WINGLET parameters
		// ---------------------------------	
		int wingletWiresNumber = 15;
		
		double backFactor = (tipParams == null || tipParams.size() == 0) ? 0.75 : tipParams.get(1);
		double sideFactor = 0.85;
		double airfoilScaling = (tipParams == null || tipParams.size() == 0) ? 0.20 : tipParams.get(2);
		
		// -----------------------------------------------------------------
		// Generate points and tangent vectors for the WINGLET guide curve
		// -----------------------------------------------------------------
		List<double[]> guideCrvPts = new ArrayList<>();
		
		double[] ptA = wingletPtsTE.get(1);
		
		PVector guideCrvInTng = PVector.sub(
				new PVector(
						(float) wingletPtsTE.get(1)[0], 
						(float) wingletPtsTE.get(1)[1],
						(float) wingletPtsTE.get(1)[2]),
				new PVector(
						(float) wingletPtsTE.get(0)[0], 
						(float) wingletPtsTE.get(0)[1],
						(float) wingletPtsTE.get(0)[2])).normalize();
		
		double[] ptB = new double[] {
				ptA[0] + backFactor*liftingSurface.getWingletHeight().doubleValue(SI.METER),
				liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) + liftingSurface.getSemiSpan().doubleValue(SI.METER),
				ptA[2] + liftingSurface.getWingletHeight().doubleValue(SI.METER)
		};
		
		PVector guideCrvFiTng = PVector.sub(
				new PVector(
						(float) ptB[0],
						(float) ptB[1],
						(float) ptB[2]), 
				new PVector(
						(float) ptA[0],
						(float) ptA[1],
						(float) ptA[2]));
		
		PVector finalTngAdjVector = new PVector(
				0.0f,
				(float) (ptA[1] - ptB[1]),
				0.0f);
		
		guideCrvFiTng.add(finalTngAdjVector.mult((float) sideFactor)).normalize();
		
		guideCrvPts.add(ptA);
		guideCrvPts.add(ptB);
		
		// -----------------------------------
		// Generate the WINGLET guide curve
		// -----------------------------------
		CADGeomCurve3D guideCrv = OCCUtils.theFactory.newCurve3D(
				guideCrvPts, 
				false, 
				new double[] {guideCrvInTng.x, guideCrvInTng.y, guideCrvInTng.z}, 
				new double[] {guideCrvFiTng.x, guideCrvFiTng.y, guideCrvFiTng.z}, 
				false);
		
		if (exportSupportShapes) {
			wingTipShapes.add((OCCShape) guideCrv.edge());
		}
		
		// -----------------------------------
		// Get the airfoil wire CG
		// -----------------------------------
		OCCGeomCurve3D tipAirfoilCurve = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3D(tipAirfoilWire.edges().get(0));

		tipAirfoilCurve.discretize(25);
		List<double[]> tipAirfoilCurveDiscret = tipAirfoilCurve.getDiscretizedCurve().getDoublePoints();

		double sumXAirfoil = 0;
		double sumYAirfoil = 0;
		double sumZAirfoil = 0;
		for (int i = 0; i < tipAirfoilCurveDiscret.size(); i++) {
			sumXAirfoil += tipAirfoilCurveDiscret.get(i)[0];
			sumYAirfoil += tipAirfoilCurveDiscret.get(i)[1];
			sumZAirfoil += tipAirfoilCurveDiscret.get(i)[2];
		}

		double[] cgAirfoil = new double[] {
				sumXAirfoil/tipAirfoilCurveDiscret.size(), 
				sumYAirfoil/tipAirfoilCurveDiscret.size(), 
				sumZAirfoil/tipAirfoilCurveDiscret.size()
		};
		
		// -------------------------------------------------
		// Generate the WINGLET supporting and main shapes
		// -------------------------------------------------
		List<CADWire> wingletWires = OCCUtils.revolveWireAroundGuideCurve(
				tipAirfoilWire, cgAirfoil, guideCrv, 
				airfoilScaling, wingletWiresNumber);
		
		if (exportSupportShapes) {
			wingTipShapes.addAll(wingletWires.stream().map(w -> (OCCWire) w).collect(Collectors.toList()));
		}
		
		if (exportShells) {			
			wingTipShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
					OCCUtils.makePatchThruSections(wingletWires),
					OCCUtils.theFactory.newFacePlanar(wingletWires.get(wingletWires.size() - 1))
					);		
		}
		
		// ----------------------------------------
		// Arrange the results
		// ----------------------------------------
		retShapes = new Tuple2<OCCShell, List<OCCShape>>(wingTipShell, wingTipShapes);
		
		return retShapes;
	}
	
	// TODO: make this private again after generateFairingShapes completion
	public static List<double[]> generateAirfoilAtY(double yStation, LiftingSurface liftingSurface) {
		
		// ---------------------------------------
		// Determine the airfoil to be modified
		// ---------------------------------------
		Airfoil baseAirfoil = null;
		double tci = 0.15;
		double tcf = 0.15;
		double scaleFactor = 1.0;
		for (int i = 1; i <= liftingSurface.getPanels().size(); i++) {
			
			double yIn = liftingSurface.getYBreakPoints().get(i-1).doubleValue(SI.METER);
			double yOut = liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER);
			
			if (yStation > yIn && yStation < yOut || 
				Math.abs(yStation - yIn) <= 1e-5 || 
				Math.abs(yStation - yOut) <= 1e-5) {			
				baseAirfoil = liftingSurface.getAirfoilList().get(i-1);
				tci = liftingSurface.getAirfoilList().get(i-1).getThicknessToChordRatio();
				tcf = liftingSurface.getAirfoilList().get(i).getThicknessToChordRatio();
				scaleFactor = MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yIn, yOut}, 
						new double[] {1, tcf/tci}, 
						yStation);
				
				break;
			}
		}
		
		// -----------------------------------------------------------------
		// Delete duplicates in the airfoil points list, whether necessary
		// -----------------------------------------------------------------
		List<PVector> noDuplicatesCoords = new ArrayList<>();
		for (int i = 0; i < baseAirfoil.getXCoords().length; i++) {
			noDuplicatesCoords.add(new PVector (
					(float) baseAirfoil.getXCoords()[i],
					0.0f,
					(float) baseAirfoil.getZCoords()[i]
					));
		}

		Set<PVector> uniqueEntries = new HashSet<>();
		for (Iterator<PVector> iter = noDuplicatesCoords.listIterator(1); iter.hasNext(); ) {
			PVector point = (PVector) iter.next();
			if (!uniqueEntries.add(point))
				iter.remove();
		}
		
		// ----------------------------------------------------------------
		// Scale the inner airfoil according to thickness-to-chord ratios
		// ----------------------------------------------------------------
		for (int i = 0; i < noDuplicatesCoords.size(); i++)
			noDuplicatesCoords.get(i).z = (float) ((float) noDuplicatesCoords.get(i).z * scaleFactor);
		
		// ---------------------------
		// Check the trailing edge
		// ---------------------------
		int nPts = noDuplicatesCoords.size();
		
		if (Math.abs(noDuplicatesCoords.get(0).z - noDuplicatesCoords.get(nPts - 1).z) < 1e-5) {
			
			noDuplicatesCoords.get(0).set(
					noDuplicatesCoords.get(0).x,
					noDuplicatesCoords.get(0).y,
					noDuplicatesCoords.get(0).z + 5e-4f
					);
			
			noDuplicatesCoords.get(nPts - 1).set(
					noDuplicatesCoords.get(nPts - 1).x,
					noDuplicatesCoords.get(nPts - 1).y,
					noDuplicatesCoords.get(nPts - 1).z - 5e-4f
					);
		}
		
		// ---------------------------------------
		// Calculate airfoil actual coordinates
		// ---------------------------------------
		List<double[]> basePts = new ArrayList<>();
		basePts.addAll(noDuplicatesCoords.stream()
				.map(pv -> new double[] {pv.x, pv.y, pv.z})
				.collect(Collectors.toList()));
		
		return generatePtsAtY(basePts, yStation, liftingSurface);
	}
	
	private static List<double[]> generateCamberAtY(double yStation, LiftingSurface liftingSurface) {

		// -----------------------
		// Determine the airfoil 
		// -----------------------
		Airfoil airfoil = null;
		for (int i = 1; i <= liftingSurface.getPanels().size(); i++) {

			double yIn = liftingSurface.getYBreakPoints().get(i-1).doubleValue(SI.METER);
			double yOut = liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER);

			if (yStation > yIn && yStation < yOut || 
				Math.abs(yStation - yIn) <= 1e-5 || 
				Math.abs(yStation - yOut) <= 1e-5) {			
				airfoil = liftingSurface.getAirfoilList().get(i-1);
				break;
			}
		}

		// -----------------------------------------------
		// Return the camber line at the actual location
		// -----------------------------------------------
		return generatePtsAtY(getAirfoilCamberPts(airfoil), yStation, liftingSurface);
	}

	private static List<double[]> generateChordAtY(double yStation, LiftingSurface liftingSurface) {

		List<double[]> pts = new ArrayList<>();
		pts.add(new double[] {1.0, 0.0, 0.0});
		pts.add(new double[] {0.0, 0.0, 0.0});

		return generatePtsAtY(pts, yStation, liftingSurface);
	}
	
	private static List<double[]> generatePtsAtY(List<double[]> pts, 
			double yStation, LiftingSurface liftingSurface) {
		
		List<double[]> ptsAtY = new ArrayList<>();
		
		// ---------------------------
		// Get interpolated values
		// ---------------------------		
		double xLE = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getXLEBreakPoints()), 
				yStation);
		
		double zLE = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getZLEBreakPoints()), 
				yStation);
		
		double chord = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getChordsBreakPoints()), 
				yStation);
		
		double twist = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getYBreakPoints()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(liftingSurface.getTwistsBreakPoints().stream()
						.map(a -> a.to(SI.RADIAN))
						.collect(Collectors.toList())), 
				yStation);
		
		// -------------------------------
		// Calculate actual coordinates
		// -------------------------------
		double x, y, z;
		
		for (int i = 0; i < pts.size(); i++) {
			
			// Scale to actual dimensions
			x = pts.get(i)[0] * chord;
			y = 0.0;
			z = pts.get(i)[2] * chord;
			
			// Set the rotation due to the twist and the rigging angle			
//			double r = Math.sqrt(x*x + z*z);
//			x = x - r * (1 - Math.cos(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
//			z = z - r * Math.sin(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN));			
			double[] rotPts = rotatePoint2D(
					new double[] {0.0, 0.0}, 
					twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN), 
					new double[] {x, z}
					);
			
			x = rotPts[0];
			z = rotPts[1];
			
			// Set the actual location
			x = liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER) + xLE + x;
			
			y = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) - z :
					liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) + yStation;
			
			z = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER) + yStation :
					liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER) + zLE + z;

			ptsAtY.add(new double[] {x, y, z});
		}		
		
		return ptsAtY;	
	}
	
	private static List<double[]> getAirfoilCamberPts(Airfoil airfoil) {
		
		List<double[]> camberLinePts = new ArrayList<>();
		
		// -------------------------------------------------------------------------------------
		// Split airfoil points in two separate lists, one for the upper and one for the lower
		// -------------------------------------------------------------------------------------
		List<Double> xUpper = new ArrayList<>();
		List<Double> zUpper = new ArrayList<>();
		List<Double> xLower = new ArrayList<>(); 
		List<Double> zLower = new ArrayList<>();
		
		double[] x = airfoil.getXCoords();
		double[] z = airfoil.getZCoords();
		
		int nPts = x.length;
		int iMin = MyArrayUtils.getIndexOfMin(x);
		
		IntStream.range(0, iMin + 1).forEach(i -> {
			xUpper.add(x[i]);
			zUpper.add(z[i]);
			});
		
		// Making the lower list start from the last point of the upper one, whether necessary
		if(Math.abs(x[iMin + 1] - x[iMin]) > 1e-5) { 
			xLower.add(x[iMin]);
			zLower.add(z[iMin]);
		}
		
		IntStream.range(iMin + 1, nPts).forEach(i -> {
			xLower.add(x[i]);
			zLower.add(z[i]);
			});
		
		Collections.reverse(xUpper);
		Collections.reverse(zUpper);
		
		// -----------------------------------------------------
		// Generate points for the camber line of the airfoil
		// -----------------------------------------------------
		List<Double> zCamber = new ArrayList<>();
		
		List<Double> xCamber = MyArrayUtils.convertArrayDoublePrimitiveToList(
				MyArrayUtils.linspace(xUpper.get(iMin), xUpper.get(0), nPts));
		
		xCamber.forEach(xc -> {
			double zUpp = MyMathUtils.getInterpolatedValue1DSpline(
					MyArrayUtils.convertToDoublePrimitive(xUpper), 
					MyArrayUtils.convertToDoublePrimitive(zUpper), 
					xc);
			double zLow = MyMathUtils.getInterpolatedValue1DSpline(
					MyArrayUtils.convertToDoublePrimitive(xLower), 
					MyArrayUtils.convertToDoublePrimitive(zLower), 
					xc);
			zCamber.add((zUpp + zLow) * 0.5); 
		});
		
		IntStream.range(0, nPts).forEach(i -> 
				camberLinePts.add(new double[] {
						xCamber.get(i),
						0.0,
						zCamber.get(i)
				}));
	
		return camberLinePts;
	}
	
	private static double[] rotatePoint2D(double[] orig, double angle, double[] pnt) {
		
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		
		// translate point back to the origin
		double x = pnt[0] - orig[0];
		double y = pnt[1] - orig[1];
		
		// rotate point
		double xNew = x*c + y*s; 
		double yNew = y*c - x*s;
		
		// translate point back
		x = xNew + orig[0];
		y = yNew + orig[1];
		
		return new double[] {x, y};
	}
	
//	public static List<double[]> interpolateAirfoils(Airfoil airfoil1, Airfoil airfoil2, double w) {
//		
//		List<double[]> interpPts = new ArrayList<>();
//		
//		// -----------------------------------------------------------------
//		// Delete duplicates in the airfoil points list, whether necessary
//		// -----------------------------------------------------------------
//		List<PVector> noDuplicatesCoords1 = new ArrayList<>();
//		for (int i = 0; i < airfoil1.getXCoords().length; i++) {
//			noDuplicatesCoords1.add(new PVector (
//					(float) airfoil1.getXCoords()[i],
//					0.0f,
//					(float) airfoil1.getZCoords()[i]
//					));
//		}
//
//		Set<PVector> uniqueEntries1 = new HashSet<>();
//		for (Iterator<PVector> iter = noDuplicatesCoords1.listIterator(1); iter.hasNext(); ) {
//			PVector point = (PVector) iter.next();
//			if (!uniqueEntries1.add(point))
//				iter.remove();
//		}
//		
//		List<PVector> noDuplicatesCoords2 = new ArrayList<>();
//		for (int i = 0; i < airfoil2.getXCoords().length; i++) {
//			noDuplicatesCoords2.add(new PVector (
//					(float) airfoil2.getXCoords()[i],
//					0.0f,
//					(float) airfoil2.getZCoords()[i]
//					));
//		}
//
//		Set<PVector> uniqueEntries2 = new HashSet<>();
//		for (Iterator<PVector> iter = noDuplicatesCoords2.listIterator(1); iter.hasNext(); ) {
//			PVector point = (PVector) iter.next();
//			if (!uniqueEntries2.add(point))
//				iter.remove();
//		}
//		
//		// Split airfoil points in two separated lists
//		
//		// airfoil1
//		List<Double> xUpper1 = new ArrayList<>();
//		List<Double> zUpper1 = new ArrayList<>();
//		List<Double> xLower1 = new ArrayList<>(); 
//		List<Double> zLower1 = new ArrayList<>();
//		
//		List<Double> x1 = new ArrayList<>();
//		List<Double> z1 = new ArrayList<>();
//		
//		noDuplicatesCoords1.forEach(pv -> {
//			x1.add((double) pv.x);
//			z1.add((double) pv.z);
//		});
//		
//		int nPts1 = x1.size();
//		int iMin1 = MyArrayUtils.getIndexOfMin(MyArrayUtils.convertToDoublePrimitive(x1));
//		
//		IntStream.range(0, iMin1 + 1).forEach(i -> {
//			xUpper1.add(x1.get(i));
//			zUpper1.add(z1.get(i));
//			});
//		
//		IntStream.range(iMin1 + 1, nPts1).forEach(i -> {
//			xLower1.add(x1.get(i));
//			zLower1.add(z1.get(i));
//		});
//
//		Collections.reverse(xUpper1);
//		Collections.reverse(zUpper1);
//		
//		// airfoil2
//		List<Double> xUpper2 = new ArrayList<>();
//		List<Double> zUpper2 = new ArrayList<>();
//		List<Double> xLower2 = new ArrayList<>(); 
//		List<Double> zLower2 = new ArrayList<>();
//		
//		List<Double> x2 = new ArrayList<>();
//		List<Double> z2 = new ArrayList<>();
//		
//		noDuplicatesCoords2.forEach(pv -> {
//			x2.add((double) pv.x);
//			z2.add((double) pv.z);
//		});
//		
//		int nPts2 = x2.size();
//		int iMin2 = MyArrayUtils.getIndexOfMin(MyArrayUtils.convertToDoublePrimitive(x2));
//		
//		IntStream.range(0, iMin2 + 1).forEach(i -> {
//			xUpper2.add(x2.get(i));
//			zUpper2.add(z2.get(i));
//			});
//
//		IntStream.range(iMin2 + 1, nPts2).forEach(i -> {
//			xLower2.add(x2.get(i));
//			zLower2.add(z2.get(i));
//			});
//		
//		Collections.reverse(xUpper2);
//		Collections.reverse(zUpper2);
//		
//		// Interpolate 2
//		List<Double> interpZUpper2 = new ArrayList<>();
//		List<Double> interpZLower2 = new ArrayList<>();
//		
//		interpZUpper2.addAll(xUpper1.stream()
//			   .map(x -> MyMathUtils.getInterpolatedValue1DSpline(
//					   MyArrayUtils.convertToDoublePrimitive(xUpper2), 
//					   MyArrayUtils.convertToDoublePrimitive(zUpper2), 
//					   x))
//			   .collect(Collectors.toList()));
//		
//		interpZLower2.addAll(xLower1.stream()
//				   .map(x -> MyMathUtils.getInterpolatedValue1DSpline(
//						   MyArrayUtils.convertToDoublePrimitive(xLower2), 
//						   MyArrayUtils.convertToDoublePrimitive(zLower2), 
//						   x))
//				   .collect(Collectors.toList()));
//		
//		// Generate new airfoil
//		List<Double> zUpper = new ArrayList<>();
//		List<Double> zLower = new ArrayList<>();
//		
//		for (int i = 0; i < xUpper1.size(); i++)			
//			zUpper.add(zUpper1.get(i)*(1-w) + interpZUpper2.get(i)*w);
//		
//		for (int i = 0; i < xLower1.size(); i++)
//			zLower.add(zLower1.get(i)*(1-w) + interpZLower2.get(i)*w);
//		
//		// Generate the double[] list of points
//		Collections.reverse(xUpper1);
//		Collections.reverse(zUpper);
//
//		List<Double> x = new ArrayList<>();
//		List<Double> z = new ArrayList<>();
//
//		x.addAll(xUpper1);
//		x.addAll(xLower1);
//
//		z.addAll(zUpper);
//		z.addAll(zLower);
//
//		for (int i = 0; i < x.size(); i++) 
//			interpPts.add(new double[] {x.get(i), 0.0, z.get(i)});
		
		
		
//		// Generate a spline curve for the first airfoil
//		OCCGeomCurve3D airfoil1Curve = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords1, false);
//		airfoil1Curve.discretize(350);
//
//		List<double[]> airfoil1Pts = airfoil1Curve.getDiscretizedCurve().getDoublePoints();
//		
//		// Generate a spline curve for the second airfoil
//		OCCGeomCurve3D airfoil2Curve = (OCCGeomCurve3D) OCCUtils.theFactory.newCurve3DP(noDuplicatesCoords2, false);
//		airfoil2Curve.discretize(350);
//		
//		List<double[]> airfoil2Pts = airfoil2Curve.getDiscretizedCurve().getDoublePoints();
//		
//		// Split the airfoil pts in two separated lists, one for the upper and one for the lower
//		
//		// airfoil2
//		List<Double> xUpper2 = new ArrayList<>();
//		List<Double> zUpper2 = new ArrayList<>();
//		List<Double> xLower2 = new ArrayList<>(); 
//		List<Double> zLower2 = new ArrayList<>();
//		
//		List<Double> x2 = new ArrayList<>();
//		List<Double> z2 = new ArrayList<>();
//		
//		airfoil2Pts.forEach(pt -> {
//			x2.add(pt[0]);
//			z2.add(pt[2]);
//		});
//		
//		int nPts2 = x2.size();
//		int iMin2 = MyArrayUtils.getIndexOfMin(MyArrayUtils.convertToDoublePrimitive(x2));
//		
//		IntStream.range(0, iMin2 + 1).forEach(i -> {
//			xUpper2.add(x2.get(i));
//			zUpper2.add(z2.get(i));
//			});
//		
//		// Making the lower list start from the last point of the upper one, whether necessary
////		if(Math.abs(x2.get(iMin2 + 1) - x2.get(iMin2)) > 1e-5) { 
////			xLower2.add(x2.get(iMin2));
////			zLower2.add(z2.get(iMin2));
////		}
//		
//		IntStream.range(iMin2 + 1, nPts2).forEach(i -> {
//			xLower2.add(x2.get(i));
//			zLower2.add(z2.get(i));
//			});
//		
//		Collections.reverse(xUpper2);
//		Collections.reverse(zUpper2);
//		
//		// airfoil1
//		List<Double> xUpper1 = new ArrayList<>();
//		List<Double> zUpper1 = new ArrayList<>();
//		List<Double> xLower1 = new ArrayList<>(); 
//		List<Double> zLower1 = new ArrayList<>();
//		
//		List<Double> x1 = new ArrayList<>();
//		List<Double> z1 = new ArrayList<>();
//		
//		airfoil1Pts.forEach(pt -> {
//			x1.add(pt[0]);
//			z1.add(pt[2]);
//		});
//		
//		int nPts1 = x1.size();
//		int iMin1 = MyArrayUtils.getIndexOfMin(MyArrayUtils.convertToDoublePrimitive(x1));
//		
//		IntStream.range(0, iMin1 + 1).forEach(i -> {
//			xUpper1.add(x1.get(i));
//			zUpper1.add(z1.get(i));
//			});
//		
//		// Making the lower list start from the last point of the upper one, whether necessary
////		if(Math.abs(x1.get(iMin1 + 1) - x1.get(iMin1)) > 1e-5) { 
////			xLower1.add(x1.get(iMin1));
////			zLower1.add(z1.get(iMin1));
////		}
//		
//		IntStream.range(iMin1 + 1, nPts1).forEach(i -> {
//			xLower1.add(x1.get(i));
//			zLower1.add(z1.get(i));
//			});
//		
//		Collections.reverse(xUpper1);
//		Collections.reverse(zUpper1);
//		
//		// Generate new xUpper1 and xLower1
//		List<Double> finerXUpper1 = MyArrayUtils.convertDoubleArrayToListDouble(
//				MyArrayUtils.halfCosine1SpaceDouble(
//						xUpper1.get(0), 
//						xUpper1.get(xUpper1.size()-1), 
//						75));
//		
//		List<Double> finerXLower1 = MyArrayUtils.convertDoubleArrayToListDouble(
//				MyArrayUtils.halfCosine1SpaceDouble(
//						xLower1.get(0), 
//						xLower1.get(xLower1.size()-1), 
//						75));
//		
//		// Interpolate 1
//		List<Double> interpZUpper1 = new ArrayList<>();
//		List<Double> interpZLower1 = new ArrayList<>();
//		
//		interpZUpper1.addAll(finerXUpper1.stream()
//			   .map(x -> MyMathUtils.getInterpolatedValue1DSpline(
//					   MyArrayUtils.convertToDoublePrimitive(xUpper1), 
//					   MyArrayUtils.convertToDoublePrimitive(zUpper1), 
//					   x))
//			   .collect(Collectors.toList()));
//		
//		interpZLower1.addAll(finerXLower1.stream()
//				   .map(x -> MyMathUtils.getInterpolatedValue1DSpline(
//						   MyArrayUtils.convertToDoublePrimitive(xLower1), 
//						   MyArrayUtils.convertToDoublePrimitive(zLower1), 
//						   x))
//				   .collect(Collectors.toList()));
//		
//		// Interpolate 2
//		List<Double> interpZUpper2 = new ArrayList<>();
//		List<Double> interpZLower2 = new ArrayList<>();
//		
//		interpZUpper2.addAll(finerXUpper1.stream()
//			   .map(x -> MyMathUtils.getInterpolatedValue1DSpline(
//					   MyArrayUtils.convertToDoublePrimitive(xUpper2), 
//					   MyArrayUtils.convertToDoublePrimitive(zUpper2), 
//					   x))
//			   .collect(Collectors.toList()));
//		
//		interpZLower2.addAll(finerXLower1.stream()
//				   .map(x -> MyMathUtils.getInterpolatedValue1DSpline(
//						   MyArrayUtils.convertToDoublePrimitive(xLower2), 
//						   MyArrayUtils.convertToDoublePrimitive(zLower2), 
//						   x))
//				   .collect(Collectors.toList()));
//		
//		// Generate new airfoil
//		List<Double> zUpper = new ArrayList<>();
//		List<Double> zLower = new ArrayList<>();
//		
//		for (int i = 0; i < finerXUpper1.size(); i++)			
//			zUpper.add(interpZUpper1.get(i)*(1-w) + interpZUpper2.get(i)*w);
//		
//		for (int i = 0; i < finerXLower1.size(); i++)
//			zLower.add(interpZLower1.get(i)*(1-w) + interpZLower2.get(i)*w);
//		
//		// Generate the double[] list of points
////		finerXUpper1.remove(0);
////		zUpper.remove(0);
//		
//		Collections.reverse(finerXUpper1);
//		Collections.reverse(zUpper);
//		
//		List<Double> x = new ArrayList<>();
//		List<Double> z = new ArrayList<>();
//		
//		x.addAll(finerXUpper1);
//		x.addAll(finerXLower1);
//		
//		z.addAll(zUpper);
//		z.addAll(zLower);
//		
//		for (int i = 0; i < x.size(); i++) 
//			interpPts.add(new double[] {x.get(i), 0.0, z.get(i)});
//		
//		return interpPts;
//	}
	
	private static List<double[]> getCrvIntersectionWithNormalSegments(
			CADGeomCurve3D curve,
			PVector normalVector,
			PVector pv0,
			PVector pv1,
			double[] xs
			) {
		
		List<double[]> intersections = new ArrayList<>();
		
		// -----------------------------------------
		// Generate mid points between the extrema
		// -----------------------------------------
		List<double[]> midPts = Arrays.stream(xs)		
				.mapToObj(d -> PVector.lerp(pv0, pv1, (float) d))
				.map(pv -> new double[] {pv.x, pv.y, pv.z})
				.collect(Collectors.toList());	
			
		// ---------------------------
		// Generate splitting vectors
		// ---------------------------
		List<CADGeomCurve3D> vertSegments = new ArrayList<>();
		
		midPts.forEach(pt0 -> {		
			PVector pt1 = new PVector((float) pt0[0], (float) pt0[1], (float) pt0[2]).add(normalVector);	
			PVector pt2 = new PVector((float) pt0[0], (float) pt0[1], (float) pt0[2]).add(PVector.mult(normalVector, -1));
			
			vertSegments.add(OCCUtils.theFactory.newCurve3D( 
					new double[] {pt1.x, pt1.y, pt1.z},
					new double[] {pt2.x, pt2.y, pt2.z}));
			});
		
		// -------------------------
		// Calculate intersections
		// -------------------------
		intersections.addAll(vertSegments.stream()
					.map(s -> getCrvIntersectionWithSegment(curve, s))
					.collect(Collectors.toList()));	
		
		return intersections;
	}
	
	private static double[] getCrvIntersectionWithSegment(
			CADGeomCurve3D curve,
			CADGeomCurve3D segment	
			) {
		
		double[] intersection = null;
		
		// ---------------------------------------
		// Calculate the intersection, if exists
		// ---------------------------------------
		List<double[]> intersecPts = OCCUtils.getIntersectionPts(curve, segment, 1e-2);

		if (intersecPts.size() == 1) {
			intersection = intersecPts.get(0);
		} else {
			System.out.println("========== [AircraftCADUtils::getLiftingSurfaceCAD::generateRoundedWingTip - "
					+ "Warning: the number of intersection points found is incorrect! Returning a null ...");
		}
		
		return intersection;
 	}
	
	private static List<CADWire> generateRoundedTipSuppCrvs(
			List<double[]> airfoilUpperPts,
			List<double[]> suppCrvsApexPts,
			List<double[]> airfoilLowerPts,	
			List<double[]> upperTngs,
			List<double[]> lowerTngs,
			List<double[]> apexUpperTngs,
			List<double[]> apexLowerTngs,
			ComponentEnum lsType
			) {
		
		List<CADWire> suppCrvs = new ArrayList<>();
		int nPts = airfoilUpperPts.size();
		
		// -------------------------------
		// Generate the supporting wires
		// -------------------------------
		suppCrvs.addAll(IntStream.range(0, nPts)
				 .mapToObj(i -> {
					 List<double[]> upperPts = new ArrayList<>();
					 List<double[]> lowerPts = new ArrayList<>();
					 
					 upperPts.add(airfoilUpperPts.get(i));
					 upperPts.add(suppCrvsApexPts.get(i));
					 
					 lowerPts.add(suppCrvsApexPts.get(i));
					 lowerPts.add(airfoilLowerPts.get(i));
					 
					 return generateRoundedTipSuppCrv(
							 upperPts, 
							 lowerPts, 
							 upperTngs.get(i), 
							 lowerTngs.get(i), 
							 apexUpperTngs.get(i),
							 apexLowerTngs.get(i),
							 lsType);
				 })
				 .collect(Collectors.toList()));
		
		return suppCrvs;
		
	}
	
	private static CADWire generateRoundedTipSuppCrv(
			List<double[]> upperPts,
			List<double[]> lowerPts,
			double[] upperTng,
			double[] lowerTng,
			double[] apexUpperTng,
			double[] apexLowerTng,
			ComponentEnum lsType
			) {
		
		CADWire suppCrv = null;
		
		// ------------------------------------
		// Generate the upper and lower curve
		// ------------------------------------
		CADGeomCurve3D upperCrv = OCCUtils.theFactory.newCurve3D(
				upperPts, 
				false, 
				upperTng, 
				apexUpperTng, 
				false);
		
		CADGeomCurve3D lowerCrv = OCCUtils.theFactory.newCurve3D(
				lowerPts, 
				false, 
				apexLowerTng,
				lowerTng,  
				false);
		
		// ------------------------------------
		// Generate the supporting wire
		// ------------------------------------
		suppCrv = OCCUtils.theFactory.newWireFromAdjacentEdges(
				upperCrv.edge(), 
				lowerCrv.edge());
		
		return suppCrv;
	}
	
	private static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateDetachedUpFairingShapes(
			FairingDataCollection fairingData,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
			) {
		
		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
		
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// -------------------------------------
		// FAIRING sketching points generation
		// -------------------------------------
		double[] pntA = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntB = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilLE()[2])*0.15
		};
		
		double[] pntC = new double[] {
				fairingData.getRootAirfoilTop()[0],
				fairingData.getRootAirfoilTop()[1],
				fairingData.getFairingMaximumZ()
		};
		
		double[] pntD = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilTE()[2])*0.90
		};
		
		double[] pntE = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntF = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntG = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntH = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntI = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntJ = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntK = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntL = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntM = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() 
		};
		
		// ------------------------------------------------------
		// FAIRING curves tangent vectors creation
		// ------------------------------------------------------
		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
		float height      = (float) Math.abs(pntC[2] - pntA[2]);
		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
			
		PVector upperCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
		
		upperCurveInTng.z = upperCurveInTng.z - 1.50f*upperCurveInTng.z;
		upperCurveInTng.normalize();
		
		PVector upperCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
		
		upperCurveFiTng.z = upperCurveFiTng.z + 0.10f*upperCurveFiTng.z;
		upperCurveFiTng.normalize();
		
		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
		
		upperCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
		upperCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
			
		// ------------------------------------------------------
		// FAIRING supporting curves and right patches creation
		// ------------------------------------------------------
		List<double[]> upperCurvePts = new ArrayList<>();
		List<double[]> sideCurvePts = new ArrayList<>();
		List<double[]> lowerCurvePts = new ArrayList<>();
		
		upperCurvePts.add(pntA);
		upperCurvePts.add(pntB);
		upperCurvePts.add(pntC);
		upperCurvePts.add(pntD);
		upperCurvePts.add(pntE);
		
		sideCurvePts.add(pntJ);
		sideCurvePts.add(pntK);
		sideCurvePts.add(pntL);
		sideCurvePts.add(pntM);
			
		lowerCurvePts.add(pntI);
		lowerCurvePts.add(pntH);
		lowerCurvePts.add(pntG);
		lowerCurvePts.add(pntF);
		
		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
				upperCurvePts, sideCurvePts, lowerCurvePts, 
				new PVector[] {upperCurveInTng, upperCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
				exportSupportShapes, exportShells || exportSolids);
		
		supportShapes.addAll(fairingShapes._1());
		
		if (exportShells) {
			shellShapes.addAll(fairingShapes._2());
		}
		
		if (exportSolids) {
			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
					OCCUtils.theFactory.newShellFromAdjacentShells(
							(CADShell) fairingShapes._2().get(0), 
							(CADShell) fairingShapes._2().get(1)
							));
			
			solidShapes.add(fairingSolid);	
		}
		
		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
		
		return retShapes;	
	}
	
	private static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateAttachedUpFairingShapes(
			FairingDataCollection fairingData,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
			) {
		
		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
		
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// -------------------------------------
		// FAIRING sketching points generation
		// -------------------------------------
		double[] pntA = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntB = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilLE()[2])*0.15
		};
		
		double[] pntC = new double[] {
				fairingData.getRootAirfoilTop()[0],
				fairingData.getRootAirfoilTop()[1],
				fairingData.getFairingMaximumZ()
		};
		
		double[] pntD = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingMaximumZ() - (fairingData.getFairingMaximumZ() - fairingData.getRootAirfoilTE()[2])*0.15
		};
		
		double[] pntE = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFuselageMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntF = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntG = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntH = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntI = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFairingMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntJ = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntK = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntL = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntM = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() 
		};
		
		// ------------------------------------------------------
		// FAIRING curves tangent vectors creation
		// ------------------------------------------------------
		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
		float height      = (float) Math.abs(pntC[2] - pntA[2]);
		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
			
		PVector upperCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
		
		upperCurveInTng.z = upperCurveInTng.z - 1.50f*upperCurveInTng.z;
		upperCurveInTng.normalize();
		
		PVector upperCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
		
		upperCurveFiTng.z = upperCurveFiTng.z + 0.10f*upperCurveFiTng.z;
		upperCurveFiTng.normalize();
		
		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
		
		upperCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
		upperCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
			
		// ------------------------------------------------------
		// FAIRING supporting curves and right patches creation
		// ------------------------------------------------------
		List<double[]> upperCurvePts = new ArrayList<>();
		List<double[]> sideCurvePts = new ArrayList<>();
		List<double[]> lowerCurvePts = new ArrayList<>();
		
		upperCurvePts.add(pntA);
		upperCurvePts.add(pntB);
		upperCurvePts.add(pntC);
		upperCurvePts.add(pntD);
		upperCurvePts.add(pntE);
		
		sideCurvePts.add(pntJ);
		sideCurvePts.add(pntK);
		sideCurvePts.add(pntL);
		sideCurvePts.add(pntM);
			
		lowerCurvePts.add(pntI);
		lowerCurvePts.add(pntH);
		lowerCurvePts.add(pntG);
		lowerCurvePts.add(pntF);
		
		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
				upperCurvePts, sideCurvePts, lowerCurvePts, 
				new PVector[] {upperCurveInTng, upperCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
				exportSupportShapes, exportShells || exportSolids);
		
		supportShapes.addAll(fairingShapes._1());
		
		if (exportShells) {
			shellShapes.addAll(fairingShapes._2());
		}
		
		if (exportSolids) {
			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
					OCCUtils.theFactory.newShellFromAdjacentShells(
							(CADShell) fairingShapes._2().get(0), 
							(CADShell) fairingShapes._2().get(1)
							));
			
			solidShapes.add(fairingSolid);	
		}
		
		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
		
		return retShapes;		
	}
	
	private static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateMiddleFairingShapes(
			FairingDataCollection fairingData,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
			) {
		
		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
		
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
		
		return retShapes;
	}
	
	private static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateAttachedDownFairingShapes(
			FairingDataCollection fairingData,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
			) {
		
		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
		
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// -------------------------------------
		// FAIRING sketching points generation
		// -------------------------------------
		double[] pntA = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntB = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.65
		};
		
		double[] pntC = new double[] {
				fairingData.getRootAirfoilTop()[0],
				fairingData.getRootAirfoilTop()[1],
				fairingData.getFairingMinimumZ()
		};
		
		double[] pntD = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.65
		};
		
		double[] pntE = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntF = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntG = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntH = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntI = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntJ = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntK = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntL = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntM = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() 
		};
		
		// ------------------------------------------------------
		// FAIRING curves tangent vectors creation
		// ------------------------------------------------------
		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
		float height      = (float) Math.abs(pntC[2] - pntA[2]);
		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
			
		PVector lowerCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
		
		lowerCurveInTng.z = lowerCurveInTng.z + 1.50f*lowerCurveInTng.z;
		lowerCurveInTng.normalize();
		
		PVector lowerCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
		
		lowerCurveFiTng.z = lowerCurveFiTng.z - 0.10f*lowerCurveFiTng.z;
		lowerCurveFiTng.normalize();
		
		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
		
		lowerCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
		lowerCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
			
		// ------------------------------------------------------
		// FAIRING supporting curves and right patches creation
		// ------------------------------------------------------
		List<double[]> lowerCurvePts = new ArrayList<>();
		List<double[]> sideCurvePts = new ArrayList<>();
		List<double[]> upperCurvePts = new ArrayList<>();
		
		lowerCurvePts.add(pntA);
		lowerCurvePts.add(pntB);
		lowerCurvePts.add(pntC);
		lowerCurvePts.add(pntD);
		lowerCurvePts.add(pntE);
		
		sideCurvePts.add(pntJ);
		sideCurvePts.add(pntK);
		sideCurvePts.add(pntL);
		sideCurvePts.add(pntM);
			
		upperCurvePts.add(pntI);
		upperCurvePts.add(pntH);
		upperCurvePts.add(pntG);
		upperCurvePts.add(pntF);
		
		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
				lowerCurvePts, sideCurvePts, upperCurvePts, 
				new PVector[] {lowerCurveInTng, lowerCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
				exportSupportShapes, exportShells || exportSolids);
		
		supportShapes.addAll(fairingShapes._1());
		
		if (exportShells) {
			shellShapes.addAll(fairingShapes._2());
		}
		
		if (exportSolids) {
			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
					OCCUtils.theFactory.newShellFromAdjacentShells(
							(CADShell) fairingShapes._2().get(0), 
							(CADShell) fairingShapes._2().get(1)
							));
			
			solidShapes.add(fairingSolid);	
		}
		
		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
		
		return retShapes;
	}
	
	private static Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> generateDetachedDownFairingShapes(
			FairingDataCollection fairingData,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids
			) {
		
		Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>> retShapes = null;
		
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// -------------------------------------
		// FAIRING sketching points generation
		// -------------------------------------
		double[] pntA = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntB = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.65
		};
		
		double[] pntC = new double[] {
				fairingData.getRootAirfoilTop()[0],
				fairingData.getRootAirfoilTop()[1],
				fairingData.getFairingMinimumZ()
		};
		
		double[] pntD = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingMinimumZ() + (fairingData.getFuselageMinimumZ() - fairingData.getFairingMinimumZ())*0.65
		};
		
		double[] pntE = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() - 
					(fairingData.getFairingReferenceZ() - fairingData.getFuselageMinimumZ())*fairingData.getHeightBelowReferenceFactor()
		};
		
		double[] pntF = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntG = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntH = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntI = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ() + 
					(fairingData.getFairingMaximumZ() - fairingData.getFairingReferenceZ())*fairingData.getHeightAboveReferenceFactor()
		};
		
		double[] pntJ = new double[] {
				fairingData.getRootAirfoilLE()[0] - fairingData.getFairingFrontLength(),
				fairingData.getRootAirfoilLE()[1],
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntK = new double[] {
				fairingData.getRootAirfoilLE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntL = new double[] {
				fairingData.getRootAirfoilTE()[0],
				fairingData.getRootAirfoilLE()[1] + fairingData.getFairingWidth(),
				fairingData.getFairingReferenceZ()
		};
		
		double[] pntM = new double[] {
				fairingData.getRootAirfoilTE()[0] + fairingData.getFairingBackLength(),
				fairingData.getRootAirfoilTE()[1],
				fairingData.getFairingReferenceZ() 
		};
		
		// ------------------------------------------------------
		// FAIRING curves tangent vectors creation
		// ------------------------------------------------------
		float width       = (float) Math.abs(pntK[1] - pntJ[1]);
		float height      = (float) Math.abs(pntC[2] - pntA[2]);
		float frontLength = (float) Math.abs(pntC[0] - pntA[0]);
		float backLength  = (float) Math.abs(pntE[0] - pntC[0]);
			
		PVector lowerCurveInTng = new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]).sub(
				new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]));
		
		lowerCurveInTng.z = lowerCurveInTng.z + 1.50f*lowerCurveInTng.z;
		lowerCurveInTng.normalize();
		
		PVector lowerCurveFiTng = new PVector((float) pntE[0], (float) pntE[1], (float) pntE[2]).sub(
				new PVector((float) pntC[0], (float) pntC[1], (float) pntC[2]));
		
		lowerCurveFiTng.z = lowerCurveFiTng.z - 0.10f*lowerCurveFiTng.z;
		lowerCurveFiTng.normalize();
		
		PVector sideCurveInTng = new PVector(0.20f,  0.98f, 0.00f).normalize();			
		PVector sideCurveFiTng = new PVector(0.20f, -0.98f, 0.00f).normalize();
		
		lowerCurveInTng.mult((float) Math.pow(height/frontLength, 0.60));
		lowerCurveFiTng.mult((float) Math.pow(height/backLength, 0.60));
		sideCurveInTng.mult((float) Math.pow(width/frontLength, 0.30));
		sideCurveFiTng.mult((float) Math.pow(width/backLength, 0.10));
			
		// ------------------------------------------------------
		// FAIRING supporting curves and right patches creation
		// ------------------------------------------------------
		List<double[]> lowerCurvePts = new ArrayList<>();
		List<double[]> sideCurvePts = new ArrayList<>();
		List<double[]> upperCurvePts = new ArrayList<>();
		
		lowerCurvePts.add(pntA);
		lowerCurvePts.add(pntB);
		lowerCurvePts.add(pntC);
		lowerCurvePts.add(pntD);
		lowerCurvePts.add(pntE);
		
		sideCurvePts.add(pntJ);
		sideCurvePts.add(pntK);
		sideCurvePts.add(pntL);
		sideCurvePts.add(pntM);
			
		upperCurvePts.add(pntI);
		upperCurvePts.add(pntH);
		upperCurvePts.add(pntG);
		upperCurvePts.add(pntF);
		
		Tuple2<List<OCCShape>, List<OCCShape>> fairingShapes = generateFairingShapes(fairingData,
				lowerCurvePts, sideCurvePts, upperCurvePts, 
				new PVector[] {lowerCurveInTng, lowerCurveFiTng}, new PVector[] {sideCurveInTng, sideCurveFiTng}, 
				exportSupportShapes, exportShells || exportSolids);
		
		supportShapes.addAll(fairingShapes._1());
		
		if (exportShells) {
			shellShapes.addAll(fairingShapes._2());
		}
		
		if (exportSolids) {
			OCCSolid fairingSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
					OCCUtils.theFactory.newShellFromAdjacentShells(
							(CADShell) fairingShapes._2().get(0), 
							(CADShell) fairingShapes._2().get(1)
							));
			
			solidShapes.add(fairingSolid);	
		}
		
		retShapes = new Tuple3<List<OCCShape>, List<OCCShape>, List<OCCShape>>(supportShapes, shellShapes, solidShapes);
		
		return retShapes;
	}
	
	private static Tuple2<List<OCCShape>, List<OCCShape>> generateFairingShapes(FairingDataCollection fairingData,
			List<double[]> mainCurvePts, List<double[]> sideCurvePts, List<double[]> supSegmPts,
			PVector[] mainCurveTngs, PVector[] sideCurveTngs,
			boolean exportSupportShapes, boolean exportShells
			) {
		
		Tuple2<List<OCCShape>, List<OCCShape>> retShapes = null;
		
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> fairingShells = new ArrayList<>();
		
		double[] pntA = mainCurvePts.get(0);
		double[] pntE = mainCurvePts.get(mainCurvePts.size() - 1);
		double[] pntI = supSegmPts.get(0);
		double[] pntF = supSegmPts.get(supSegmPts.size() - 1);
		
		// Generate main supporting curves	
		CADGeomCurve3D mainCurve = OCCUtils.theFactory.newCurve3D(
				mainCurvePts, false, 
				new double[] {mainCurveTngs[0].x, mainCurveTngs[0].y, mainCurveTngs[0].z}, 
				new double[] {mainCurveTngs[1].x, mainCurveTngs[1].y, mainCurveTngs[1].z}, 
				false);
		
		List<OCCEdge> mainCurves1 = OCCUtils.splitCADCurve(mainCurve, mainCurvePts.get(1));	
		List<OCCEdge> mainCurves2 = OCCUtils.splitCADCurve(mainCurves1.get(1), mainCurvePts.get(3));
		
		CADGeomCurve3D mainCurve1 = OCCUtils.theFactory.newCurve3D(mainCurves1.get(0));
		CADGeomCurve3D mainCurve2 = OCCUtils.theFactory.newCurve3D(mainCurves2.get(0));
		CADGeomCurve3D mainCurve3 = OCCUtils.theFactory.newCurve3D(mainCurves2.get(1));
		
		CADGeomCurve3D sideCurve1 = OCCUtils.theFactory.newCurve3D(
				sideCurvePts.stream().limit(2).collect(Collectors.toList()), false, 
				new double[] {sideCurveTngs[0].x, sideCurveTngs[0].y, sideCurveTngs[0].z}, 
				new double[] {1.0, 0.0, 0.0}, 
				false);
		
		CADGeomCurve3D sideCurve2 = OCCUtils.theFactory.newCurve3D(
				sideCurvePts.stream().skip(1).limit(2).collect(Collectors.toList()), false);
		
		CADGeomCurve3D sideCurve3 = OCCUtils.theFactory.newCurve3D(
				sideCurvePts.stream().skip(2).collect(Collectors.toList()), false, 
				new double[] {1.0, 0.0, 0.0},
				new double[] {sideCurveTngs[1].x, sideCurveTngs[1].y, sideCurveTngs[1].z}, 			 
				false);
		
		CADGeomCurve3D supSegm1 = OCCUtils.theFactory.newCurve3D(
				supSegmPts.stream().limit(2).collect(Collectors.toList()), false);
		
		CADGeomCurve3D supSegm2 = OCCUtils.theFactory.newCurve3D(
				supSegmPts.stream().skip(1).limit(2).collect(Collectors.toList()), false);
		
		CADGeomCurve3D supSegm3 = OCCUtils.theFactory.newCurve3D(
				supSegmPts.stream().skip(2).collect(Collectors.toList()), false);
		
		// Generate vertical supporting curves
		int nMain = 10; // number of supporting curves for first and last shell 
		int nSide = 15; // number of interpolation point on side curves #1 and #3
		
		mainCurve1.discretize(nMain);
		mainCurve2.discretize(nMain);
		mainCurve3.discretize(nMain);
		
		sideCurve1.discretize(nSide);
		sideCurve3.discretize(nSide);
		
		List<double[]> mainCurve1Pts = ((OCCGeomCurve3D) mainCurve1).getDiscretizedCurve().getDoublePoints();
		List<double[]> mainCurve2Pts = ((OCCGeomCurve3D) mainCurve2).getDiscretizedCurve().getDoublePoints();
		List<double[]> mainCurve3Pts = ((OCCGeomCurve3D) mainCurve3).getDiscretizedCurve().getDoublePoints();
		
		List<Double> sideCurve1XCoords = new ArrayList<>();
		List<Double> sideCurve1YCoords = new ArrayList<>();
		List<Double> sideCurve3XCoords = new ArrayList<>();
		List<Double> sideCurve3YCoords = new ArrayList<>();
		
		((OCCGeomCurve3D) sideCurve1).getDiscretizedCurve().getDoublePoints()
			.forEach(da -> {
				sideCurve1XCoords.add(da[0]);
				sideCurve1YCoords.add(da[1]);
			});
		
		double sideCurve2YCoord = sideCurve2.edge().vertices()[1].pnt()[1];

		((OCCGeomCurve3D) sideCurve3).getDiscretizedCurve().getDoublePoints()
			.forEach(da -> {
				sideCurve3XCoords.add(da[0]);
				sideCurve3YCoords.add(da[1]);
			});
		
		List<CADGeomCurve3D> mainSegms = new ArrayList<>();
		List<CADGeomCurve3D> sideSegms = new ArrayList<>();
		List<CADGeomCurve3D> subSegms = new ArrayList<>();
		
		sideSegms.add(OCCUtils.theFactory.newCurve3D(pntA, pntI));
		sideSegms.add(OCCUtils.theFactory.newCurve3D(pntE, pntF));
		
		for (int i = 1; i < nMain; i++) {
			
			double[] mainPnt = mainCurve1Pts.get(i);		
			double[] subPnt = new double[] {mainPnt[0], pntI[1], pntI[2]};
			
			Tuple2<double[], double[]> sidePts = getFairingSidePts(
					mainPnt, subPnt, sideCurve1XCoords, sideCurve1YCoords);
			
			mainSegms.add(OCCUtils.theFactory.newCurve3D(mainPnt, sidePts._1()));				
			subSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._2(), subPnt));					
			sideSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._1(), sidePts._2()));		
		}
		
		for (int i = 0; i < nMain; i++) {
			
			double[] mainPnt = mainCurve2Pts.get(i);
			double[] subPnt = new double[] {mainPnt[0], pntI[1], pntI[2]};
			
			Tuple2<double[], double[]> sidePts = getFairingSidePts(
					mainPnt, subPnt, sideCurve2YCoord);
			
			mainSegms.add(OCCUtils.theFactory.newCurve3D(mainPnt, sidePts._1()));		
			subSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._2(), subPnt));		
			sideSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._1(), sidePts._2()));
		}
		
		for (int i = 0; i < nMain - 1; i++) {
			
			double[] mainPnt = mainCurve3Pts.get(i);
			double[] subPnt = new double[] {mainPnt[0], pntF[1], pntF[2]};
			
			Tuple2<double[], double[]> sidePts = getFairingSidePts(
					mainPnt, subPnt, sideCurve3XCoords, sideCurve3YCoords);
			
			mainSegms.add(OCCUtils.theFactory.newCurve3D(mainPnt, sidePts._1()));				
			subSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._2(), subPnt));					
			sideSegms.add(OCCUtils.theFactory.newCurve3D(sidePts._1(), sidePts._2()));
		}
				
		if (exportSupportShapes) {
			supportShapes.add((OCCShape) mainCurve1.edge());
			supportShapes.add((OCCShape) mainCurve2.edge());
			supportShapes.add((OCCShape) mainCurve3.edge());

			supportShapes.add((OCCShape) sideCurve1.edge());
			supportShapes.add((OCCShape) sideCurve2.edge());
			supportShapes.add((OCCShape) sideCurve3.edge());

			supportShapes.add((OCCShape) supSegm1.edge());
			supportShapes.add((OCCShape) supSegm2.edge());
			supportShapes.add((OCCShape) supSegm3.edge());
			
			mainSegms.forEach(s -> supportShapes.add((OCCShape) s.edge()));
			sideSegms.forEach(s -> supportShapes.add((OCCShape) s.edge()));
			subSegms.forEach(s -> supportShapes.add((OCCShape) s.edge()));
			
			List<OCCShape> mirrSupportShapes = new ArrayList<>();
			supportShapes.forEach(s -> mirrSupportShapes.add(
					OCCUtils.getShapeMirrored(s, 
							new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]), 
							new PVector(0.0f, 1.0f, 0.0f), 
							new PVector(1.0f, 0.0f, 0.0f))
					));
			
			supportShapes.addAll(mirrSupportShapes);
		}
		
		if (exportShells) {
			
			OCCShape mainPatch = OCCUtils.makePatchThruCurveSections( 
					OCCUtils.theFactory.newVertex(pntA),
					mainSegms, 
					OCCUtils.theFactory.newVertex(pntE));	
			
			OCCShape subPatch = OCCUtils.makePatchThruCurveSections(
					OCCUtils.theFactory.newVertex(pntI), 
					subSegms, 
					OCCUtils.theFactory.newVertex(pntF));	
			
			List<CADEdge> mainEdges = new ArrayList<>();
			OCCExplorer expMain = new OCCExplorer();
			expMain.init(mainPatch, CADShapeTypes.EDGE);
			while (expMain.more()) {
				mainEdges.add((CADEdge) expMain.current());
				expMain.next();
			}
			
			List<CADEdge> subEdges = new ArrayList<>();
			OCCExplorer expSub = new OCCExplorer();
			expSub.init(subPatch, CADShapeTypes.EDGE);
			while (expSub.more()) {
				subEdges.add((CADEdge) expSub.current());
				expSub.next();
			}
			
			OCCShape sidePatch = OCCUtils.makePatchThruCurveSections(
					OCCUtils.theFactory.newCurve3D(mainEdges.get(1)),
					OCCUtils.theFactory.newCurve3D(subEdges.get(3))
			);
			
			OCCShell rightShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
					mainPatch,
					sidePatch,
					subPatch);
			
			double filletRadius = Math.abs(
					mainCurvePts.get(0)[2] - supSegmPts.get(supSegmPts.size() - 1)[2])
						* 0.45 * fairingData.getFilletRadiusFactor();  
			
			int[] edgeIndexes = (fairingData.getWidthFactor() > 1.00) ?
					new int[] {1, 6}:
					new int[] {1};
			
			OCCShell filletRightShell = OCCUtils.applyFilletOnShell(
					rightShell, edgeIndexes, filletRadius);
			
			OCCShell filletLeftShell = (OCCShell) OCCUtils.getShapeMirrored(
					filletRightShell, 
					new PVector((float) pntA[0], (float) pntA[1], (float) pntA[2]), 
					new PVector(0.0f, 1.0f, 0.0f), 
					new PVector(1.0f, 0.0f, 0.0f));
			
			fairingShells.add(filletRightShell);
			fairingShells.add(filletLeftShell);
		}
		
		retShapes = new Tuple2<List<OCCShape>, List<OCCShape>>(supportShapes, fairingShells);
		
		return retShapes;
	}
	
	private static Tuple2<double[], double[]> getFairingSidePts(
			double[] mainPnt, double[] subPnt,
			List<Double> xSide, List<Double> ySide) {
		
		double ySideCoord = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(xSide), 
				MyArrayUtils.convertToDoublePrimitive(ySide), 
				mainPnt[0]
				);
		
		double[] mainSidePnt = new double[] {
				mainPnt[0],
				ySideCoord,
				mainPnt[2]
		};
		
		double[] subSidePnt = new double[] {
				mainPnt[0],
				ySideCoord,
				subPnt[2]
		};
		
		return new Tuple2<double[], double[]>(mainSidePnt, subSidePnt);
	}
	
	private static Tuple2<double[], double[]> getFairingSidePts(
			double[] mainPnt, double[] subPnt,
			double ySideCoord) {
		
		double[] mainSidePnt = new double[] {
				mainPnt[0],
				ySideCoord,
				mainPnt[2]
		};
		
		double[] subSidePnt = new double[] {
				mainPnt[0],
				ySideCoord,
				subPnt[2]
		};
		
		return new Tuple2<double[], double[]>(mainSidePnt, subSidePnt);
	}
	
	public enum XSpacingType {
		UNIFORM {
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.linspaceDouble(x1, x2, n);
				return xSpacing;
			}
		},
		COSINUS {
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.cosineSpaceDouble(x1, x2, n);
				return xSpacing;
			}
		},
		HALFCOSINUS1 { // finer spacing close to x1
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.halfCosine1SpaceDouble(x1, x2, n);
				return xSpacing;
			}
		}, 
		HALFCOSINUS2 { // finer spacing close to x2
			@Override
			public Double[] calculateSpacing(double x1, double x2, int n) {
				Double[] xSpacing = MyArrayUtils.halfCosine2SpaceDouble(x1, x2, n);
				return xSpacing;
			}
		}; 

		public abstract Double[] calculateSpacing(double x1, double x2, int n);
	}
	
	public enum WingTipType {
		CUTOFF,
		ROUNDED,
		WINGLET;
	}
	
	public enum FairingPosition {
		DETACHED_UP,
		ATTACHED_UP,
		MIDDLE,
		ATTACHED_DOWN,
		DETACHED_DOWN;
	}
	
	public static class FairingDataCollection {
		
		private Fuselage _fuselage = null;
		private LiftingSurface _liftingSurface = null;
		
		private double _frontLengthFactor = 0.0;
		private double _backLengthFactor = 0.0;
		private double _widthFactor = 0.0;
		private double _heightFactor = 0.0;
		private double _heightBelowReferenceFactor = 0.0;
		private double _heightAboveReferenceFactor = 0.0;
		private double _filletRadiusFactor = 0.0;
		
		private double _rootChord = 0.0;
		private double _rootThickness = 0.0;
		
		private List<double[]> _rootAirfoilPts = new ArrayList<>();
		private List<double[]> _sideAirfoilPts = new ArrayList<>(); // LS airfoil points at FUSELAGE max width
		private List<double[]> _tipAirfoilPts = new ArrayList<>();  // LS airfoil points at FAIRING max width
		private double[] _rootAirfoilTop = new double[3];
		private double[] _rootAirfoilBottom = new double[3];
		private double[] _rootAirfoilLE = new double[3];
		private double[] _rootAirfoilTE = new double[3];
		private double[] _sideAirfoilTop = new double[3];
		private double[] _sideAirfoilBottom = new double[3];
		private double[] _tipAirfoilTop = new double[3];
		private double[] _tipAirfoilBottom = new double[3];

		private PVector _fusDeltaApex = null;
		private double[] _fuselageSCMiddleTopPnt = new double[3];
		private double[] _fuselageSCMiddleBottomPnt = new double[3];
		private double[] _fuselageSCFrontTopPnt = new double[3];
		private double[] _fuselageSCFrontBottomPnt = new double[3];
		private double[] _fuselageSCBackTopPnt = new double[3];
		private double[] _fuselageSCBackBottomPnt = new double[3];
		private Tuple2<List<Double>, List<Double>> _fuselageSCMiddleUpperYZCoords = null;
		private Tuple2<List<Double>, List<Double>> _fuselageSCMiddleLowerYZCoords = null;
		private double _fuselageMinimumZ = 0.0;
		private double _fuselageMaximumZ = 0.0;
		
		private double[] _fusLSContactPnt = new double[3];
		private double[] _fusFairingUppContactPnt = new double[3];
		private double[] _fusFairingLowContactPnt = new double[3];
		
		private double _fairingMinimumZ = 0.0;
		private double _fairingMaximumZ = 0.0;
		private double _fairingReferenceZ = 0.0;	
			
		private double _frontLength = 0.0;
		private double _backLength = 0.0;
		private double _width = 0.0;
		
		private FairingPosition _fairingPosition;	
		
		public FairingDataCollection (Fuselage fuselage, LiftingSurface liftingSurface, 
				double frontLengthFactor, double backLengthFactor, double widthFactor, double heightFactor,
				double heightBelowReferenceFactor, double heightAboveContactFactor,
				double filletRadiusFactor
				) {
			
			this._fuselage = fuselage;
			this._liftingSurface = liftingSurface;
			
			// -------------------------------
			// FAIRING parameters assignment
			// -------------------------------
			this._frontLengthFactor = frontLengthFactor;
			this._backLengthFactor = backLengthFactor;
			this._widthFactor = widthFactor;
			this._heightFactor = heightFactor;
			this._heightBelowReferenceFactor = heightBelowReferenceFactor;
			this._heightAboveReferenceFactor = heightAboveContactFactor;
			this._filletRadiusFactor = filletRadiusFactor;
			
			// -------------------------------
			// FAIRING reference lengths
			// -------------------------------
			this._rootChord = liftingSurface.getChordsBreakPoints().get(0).doubleValue(SI.METER);
			this._rootThickness = liftingSurface.getAirfoilList().get(0).getThicknessToChordRatio()*_rootChord;
			
			// -------------------------------
			// FUSELAGE delta position
			// -------------------------------
			this._fusDeltaApex = new PVector(
					(float) fuselage.getXApexConstructionAxes().doubleValue(SI.METER),
					(float) fuselage.getYApexConstructionAxes().doubleValue(SI.METER),
					(float) fuselage.getZApexConstructionAxes().doubleValue(SI.METER)
					);		
			
			// ------------------------
			// Root reference airfoil
			// ------------------------
			this._rootAirfoilPts = AircraftCADUtils.generateAirfoilAtY(0, liftingSurface);
			
			this._rootAirfoilTop = getAirfoilTop(_rootAirfoilPts);
			this._rootAirfoilBottom = getAirfoilBottom(_rootAirfoilPts);
			this._rootAirfoilLE = getAirfoilLE(_rootAirfoilPts);
			this._rootAirfoilTE = getAirfoilTE(_rootAirfoilPts);
			
			// --------------------------------------
			// FUSELAGE reference points and curves
			// --------------------------------------
			double fusWidthAtRootAirfoilTopX = fuselage.getWidthAtX(_rootAirfoilTop[0])*0.5;
			double fusCamberZAtRootAirfoilTopX = fuselage.getCamberZAtX(_rootAirfoilTop[0] - _fusDeltaApex.x);
			
			List<PVector> fuselageSCMiddle = fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf(_rootAirfoilTop[0] - _fusDeltaApex.x, SI.METER));	
			List<PVector> fuselageSCFront = fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf((_rootAirfoilLE[0] - _frontLength) - _fusDeltaApex.x, SI.METER));
			List<PVector> fuselageSCBack = fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf((_rootAirfoilTE[0] + _backLength) - _fusDeltaApex.x, SI.METER));

			fuselageSCMiddle.forEach(pv -> pv.add(_fusDeltaApex));

			this._fuselageSCMiddleTopPnt = new double[] {
					fuselageSCMiddle.get(0).x,
					fuselageSCMiddle.get(0).y,
					fuselageSCMiddle.get(0).z
			};
			this._fuselageSCMiddleBottomPnt = new double[] {
					fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).x,
					fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).y,
					fuselageSCMiddle.get(fuselageSCMiddle.size() - 1).z
			};
			this._fuselageSCFrontTopPnt = new double[] {
					fuselageSCFront.get(0).x + _fusDeltaApex.x,
					fuselageSCFront.get(0).y + _fusDeltaApex.y,
					fuselageSCFront.get(0).z + _fusDeltaApex.z
			};
			this._fuselageSCFrontBottomPnt = new double[] {
					fuselageSCFront.get(fuselageSCFront.size() - 1).x + _fusDeltaApex.x,
					fuselageSCFront.get(fuselageSCFront.size() - 1).y + _fusDeltaApex.y,
					fuselageSCFront.get(fuselageSCFront.size() - 1).z + _fusDeltaApex.z
			};
			this._fuselageSCBackTopPnt = new double[] {
					fuselageSCBack.get(0).x + _fusDeltaApex.x,
					fuselageSCBack.get(0).y + _fusDeltaApex.y,
					fuselageSCBack.get(0).z + _fusDeltaApex.z
			};
			this._fuselageSCBackBottomPnt = new double[] {
					fuselageSCBack.get(fuselageSCBack.size() - 1).x + _fusDeltaApex.x,
					fuselageSCBack.get(fuselageSCBack.size() - 1).y + _fusDeltaApex.y,
					fuselageSCBack.get(fuselageSCBack.size() - 1).z + _fusDeltaApex.z
			};

			this._fuselageMaximumZ = Math.min(_fuselageSCFrontTopPnt[2], _fuselageSCBackTopPnt[2]);
			this._fuselageMinimumZ = Math.max(_fuselageSCFrontBottomPnt[2], _fuselageSCBackBottomPnt[2]);

			List<Double> fuselageSCMiddleUpperZCoords = new ArrayList<>();
			List<Double> fuselageSCMiddleUpperYCoords = new ArrayList<>();
			List<Double> fuselageSCMiddleLowerZCoords = new ArrayList<>();
			List<Double> fuselageSCMiddleLowerYCoords = new ArrayList<>();

			fuselageSCMiddleLowerZCoords.add(fusCamberZAtRootAirfoilTopX);
			fuselageSCMiddleLowerYCoords.add(fusWidthAtRootAirfoilTopX);

			for (int i = 0; i < fuselageSCMiddle.size() - 1; i++) {
				PVector pv = fuselageSCMiddle.get(i);

				if (pv.z > fusCamberZAtRootAirfoilTopX) {
					fuselageSCMiddleUpperZCoords.add((double) pv.z);
					fuselageSCMiddleUpperYCoords.add((double) pv.y);
				} else if (pv.z < fusCamberZAtRootAirfoilTopX) {
					fuselageSCMiddleLowerZCoords.add((double) pv.z);
					fuselageSCMiddleLowerYCoords.add((double) pv.y);
				}
			}

			fuselageSCMiddleUpperZCoords.add(fusCamberZAtRootAirfoilTopX);
			fuselageSCMiddleUpperYCoords.add(fusWidthAtRootAirfoilTopX);

			this._fuselageSCMiddleUpperYZCoords = obtainMonotonicSequence(
					fuselageSCMiddleUpperYCoords, fuselageSCMiddleUpperZCoords, true);

			this._fuselageSCMiddleLowerYZCoords = obtainMonotonicSequence(
					fuselageSCMiddleLowerYCoords, fuselageSCMiddleLowerZCoords, false);
			
			// ------------------------
			// Side reference airfoil
			// ------------------------
			this._sideAirfoilPts = AircraftCADUtils.generateAirfoilAtY(fusWidthAtRootAirfoilTopX, liftingSurface);
			
			this._sideAirfoilTop = getAirfoilTop(_sideAirfoilPts);
			this._sideAirfoilBottom = getAirfoilBottom(_sideAirfoilPts);
			
			// ------------------------------------------------------
			// Check FAIRING position
			// ------------------------------------------------------
			_fairingPosition = checkFairingPosition();
			
			// ------------------------------------------------------
			// LIFTING-SURFACE / FUSELAGE contact point calculation and 
			// ------------------------------------------------------
			switch (_fairingPosition) {

			case ATTACHED_UP:
				_fusLSContactPnt = new double[] {
						_sideAirfoilBottom[0],
						MyMathUtils.getInterpolatedValue1DSpline(
								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleUpperYZCoords._2())), 
								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleUpperYZCoords._1())), 
								_sideAirfoilBottom[2]),
						_sideAirfoilBottom[2]
				};

				break;

			case ATTACHED_DOWN:
				_fusLSContactPnt = new double[] {
						_sideAirfoilTop[0],
						MyMathUtils.getInterpolatedValue1DSpline(
								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._2())), 
								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._1())), 
								_sideAirfoilTop[2]),
						_sideAirfoilTop[2]
				};
				
				break;
				
			default:

				break;
			}
			
			// ----------------------------------------
			// Calculate fairing principal dimensions
			// ----------------------------------------
			this._frontLength = _frontLengthFactor*_rootChord;
			this._backLength = _backLengthFactor*_rootChord;
			
			if ((_fairingPosition.equals(FairingPosition.ATTACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) 
					&& _widthFactor < 1.0) {
				
				this._width = widthFactor*(fusWidthAtRootAirfoilTopX - (_fusLSContactPnt[1] - _fusDeltaApex.y)) + 
						(_fusLSContactPnt[1] - _fusDeltaApex.y);
				
			} else {
				
				this._width = _widthFactor*fusWidthAtRootAirfoilTopX;
			}
			
			// ------------------------
			// Tip reference airfoil
			// ------------------------
			this._tipAirfoilPts = AircraftCADUtils.generateAirfoilAtY(_width, liftingSurface);				
			
			this._tipAirfoilTop = getAirfoilTop(_tipAirfoilPts);
			this._tipAirfoilBottom = getAirfoilBottom(_tipAirfoilPts);
	
			// --------------------------------------------------------------------------------
			// FUSELAGE / FAIRING contact point and maximum/minimum z coordinates calculation
			// --------------------------------------------------------------------------------
			if (_widthFactor < 1.0) {
				
				_fusFairingUppContactPnt = new double[] {
						_tipAirfoilTop[0],
						_width,
						MyMathUtils.getInterpolatedValue1DSpline(
								MyArrayUtils.convertToDoublePrimitive(_fuselageSCMiddleUpperYZCoords._1()), 
								MyArrayUtils.convertToDoublePrimitive(_fuselageSCMiddleUpperYZCoords._2()), 
								_width
								)
				};
				
				_fusFairingLowContactPnt = new double[] {
						_tipAirfoilTop[0],
						_width,
						MyMathUtils.getInterpolatedValue1DSpline(
								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._1())), 
								MyArrayUtils.convertToDoublePrimitive(reverseList(_fuselageSCMiddleLowerYZCoords._2())), 
								_width
								)
				};
				
				if (_fairingPosition.equals(FairingPosition.DETACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
					
					_fairingReferenceZ = _fusFairingUppContactPnt[2];
					
					_fairingMinimumZ = MyArrayUtils.getMax(
							new double[] {
									_fuselageSCFrontBottomPnt[2],
									_fusFairingLowContactPnt[2],
									_fuselageSCBackBottomPnt[2]
							});
					
					_fuselageMaximumZ = Math.min(
							_fuselageSCFrontTopPnt[2], 
							_fuselageSCBackTopPnt[2]
							);
					
					if (_fairingPosition.equals(FairingPosition.DETACHED_UP)) {
						
						_fairingMaximumZ = Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]) + _rootThickness*_heightFactor;
						
					} else if (_fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
						
						_fairingMaximumZ = _rootAirfoilTop[2] + (_fuselageSCMiddleTopPnt[2] - _rootAirfoilTop[2])*_heightFactor;
						
					}			
					
				} else if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN) || _fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
					
					_fairingReferenceZ = _fusFairingLowContactPnt[2];
					
					_fairingMaximumZ = MyArrayUtils.getMin(
							new double[] {
									_fuselageSCFrontTopPnt[2],
									_fusFairingUppContactPnt[2],
									_fuselageSCBackTopPnt[2]
							});
					
					_fuselageMinimumZ = Math.max(
							_fuselageSCFrontBottomPnt[2], 
							_fuselageSCBackBottomPnt[2]
							);
					
					if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) {
						
						_fairingMinimumZ = _fuselageSCMiddleBottomPnt[2] - _rootThickness*_heightFactor;
						
					} else if (_fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
						
						_fairingMinimumZ = Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - _rootThickness*_heightFactor;
						
					}				
				}
				
			} else {
				
				if (_fairingPosition.equals(FairingPosition.DETACHED_UP) || _fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
					
					_fairingReferenceZ = fusCamberZAtRootAirfoilTopX + 
							(Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - fusCamberZAtRootAirfoilTopX)*0.5;
					
					_fairingMinimumZ = MyArrayUtils.getMax(
							new double[] {
									_fuselageSCFrontBottomPnt[2],
									_fuselageSCBackBottomPnt[2]
							});
					
					_fuselageMaximumZ = Math.min(
							_fuselageSCFrontTopPnt[2], 
							_fuselageSCBackTopPnt[2]
							);
					
					if (_fairingPosition.equals(FairingPosition.DETACHED_UP)) {
						
						_fairingMaximumZ = Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]) + _rootThickness*_heightFactor;
						
					} else if (_fairingPosition.equals(FairingPosition.ATTACHED_UP)) {
						
						_fairingMaximumZ = _rootAirfoilTop[2] + (_fuselageSCMiddleTopPnt[2] - _rootAirfoilTop[2])*_heightFactor;
						
					}	
					
				} else if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN) || _fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
					
					_fairingReferenceZ = fusCamberZAtRootAirfoilTopX - 
							(fusCamberZAtRootAirfoilTopX - Math.max(_rootAirfoilTop[2], _tipAirfoilTop[2]))*0.5;
					
					_fairingMaximumZ = MyArrayUtils.getMin(
							new double[] {
									_fuselageSCFrontTopPnt[2],
									_fuselageSCBackTopPnt[2]
							});
					
					_fuselageMinimumZ = Math.max(
							_fuselageSCFrontBottomPnt[2], 
							_fuselageSCBackBottomPnt[2]
							);
					
					if (_fairingPosition.equals(FairingPosition.ATTACHED_DOWN)) {
						
						_fairingMinimumZ = _fuselageSCMiddleBottomPnt[2] - _rootThickness*_heightFactor;
						
					} else if (_fairingPosition.equals(FairingPosition.DETACHED_DOWN)) {
						
						_fairingMinimumZ = Math.min(_rootAirfoilBottom[2], _tipAirfoilBottom[2]) - _rootThickness*_heightFactor;
						
					}				
				}
			}		
		}
		
		private FairingPosition checkFairingPosition() {

			List<PVector> fuselageSCAtTopX = _fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf(_sideAirfoilTop[0] - _fusDeltaApex.x, SI.METER));
			List<PVector> fuselageSCAtBottomX = _fuselage.getUniqueValuesYZSideRCurve(
					Amount.valueOf(_sideAirfoilBottom[0] - _fusDeltaApex.x, SI.METER));

			double fuselageZTopAtTopX = fuselageSCAtTopX.get(0).add(_fusDeltaApex).z;
			double fuselageCamberZAtTopX = _fuselage.getCamberZAtX(_sideAirfoilTop[0] - _fusDeltaApex.x) + _fusDeltaApex.z;
			double fuselageCamberZAtBottomX = _fuselage.getCamberZAtX(_sideAirfoilBottom[0] - _fusDeltaApex.x) + _fusDeltaApex.z;
			double fuselageZBottomAtBottomX = fuselageSCAtBottomX.get(fuselageSCAtBottomX.size() - 1).add(_fusDeltaApex).z;

			if (_rootAirfoilTop[2] > fuselageZTopAtTopX) {
				return FairingPosition.DETACHED_UP;

			} else if (_sideAirfoilTop[2] < fuselageZTopAtTopX && _sideAirfoilBottom[2] > fuselageCamberZAtBottomX) {
				return FairingPosition.ATTACHED_UP;
			}

			if (_rootAirfoilBottom[2] < fuselageZBottomAtBottomX) {
				return FairingPosition.DETACHED_DOWN;

			} else if (_sideAirfoilTop[2] < fuselageCamberZAtTopX && _sideAirfoilBottom[2] > fuselageZBottomAtBottomX) {
				return FairingPosition.ATTACHED_DOWN;
			}		

			return FairingPosition.MIDDLE;
		}
		
		private double[] getAirfoilTop(List<double[]> airfoilPts) {
			return airfoilPts.stream().max(Comparator.comparing(pnt -> pnt[2])).get();
		}
		
		private double[] getAirfoilBottom(List<double[]> airfoilPts) {
			return airfoilPts.stream().min(Comparator.comparing(pnt -> pnt[2])).get();
		}
		
		private double[] getAirfoilLE(List<double[]> airfoilPts) {
			return airfoilPts.stream().min(Comparator.comparing(pnt -> pnt[0])).get();
		}
		
		private double[] getAirfoilTE(List<double[]> airfoilPts) {
			return airfoilPts.stream().max(Comparator.comparing(pnt -> pnt[0])).get();
		}
		
		private static <T> List<T> reverseList(List<T> list) {
			return IntStream.range(0, list.size())
					.mapToObj(i -> list.get(list.size() - 1 - i))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		
		private static Tuple2<List<Double>, List<Double>> obtainMonotonicSequence(
				List<Double> y, List<Double> z, boolean strictlyIncreasing) {		
			
			List<Double> ym = new ArrayList<>();
			List<Double> zm = new ArrayList<>();
			
			int n = y.size() - 1;
			ym.add(y.get(0));
			zm.add(z.get(0));
			
			int j = 0;
			for (int i = 1; i <= n; i++) {
				Double yt_p = y.get(i);
				Double zt_p = z.get(i);
				Double yt_m = ym.get(j);
				
				if (strictlyIncreasing) {
					if (yt_p > yt_m) {
						ym.add(yt_p);
						zm.add(zt_p);
						j++;
					}
				} else {
					if (yt_p < yt_m) {
						ym.add(yt_p);
						zm.add(zt_p);
						j++;
					}
				}				
			}
			
			return new Tuple2<List<Double>, List<Double>>(ym, zm);
		}
		
		public Fuselage getFuselage() {
			return _fuselage;
		}
		
		public LiftingSurface getLiftingSurface() {
			return _liftingSurface;
		}
		
		public double getFrontLengthFactor() {
			return _frontLengthFactor;
		}
		
		public double getBackLengthFactor() {
			return _backLengthFactor;
		}
		
		public double getWidthFactor() {
			return _widthFactor;
		}
		
		public double getHeightFactor() {
			return _heightFactor;
		}
		
		public double getHeightBelowReferenceFactor() {
			return _heightBelowReferenceFactor;
		}
		
		public double getHeightAboveReferenceFactor() {
			return _heightAboveReferenceFactor;
		}
		
		public double getFilletRadiusFactor() {
			return _filletRadiusFactor;
		}
		
		public double getRootChord() {
			return _rootChord;
		}
		
		public double getRootThickness() {
			return _rootThickness;
		}
		
		public List<double[]> getRootAirfoilPts() {
			return _rootAirfoilPts;
		}
		
		public List<double[]> getSideAirfoilPts() {
			return _sideAirfoilPts;
		}
		
		public List<double[]> getTipAirfoilPts() {
			return _tipAirfoilPts;
		}
		
		public double[] getRootAirfoilTop() {
			return _rootAirfoilTop;
		}
		
		public double[] getRootAirfoilBottom() {
			return _rootAirfoilBottom;
		}
		
		public double[] getRootAirfoilLE() {
			return _rootAirfoilLE;
		}
		
		public double[] getRootAirfoilTE() {
			return _rootAirfoilTE;
		}
		
		public double[] getSideAirfoilTop() {
			return _sideAirfoilTop;
		}
		
		public double[] getSideAirfoilBottom() {
			return _sideAirfoilBottom;
		}
		
		public double[] getTipAirfoilTop() {
			return _tipAirfoilTop;
		}
		
		public double[] getTipAirfoilBottom() {
			return _tipAirfoilBottom;
		}
		
		public PVector getFusDeltaApex() {
			return _fusDeltaApex;
		}
		
		public double[] getFuselageSCMiddleTop() {
			return _fuselageSCMiddleTopPnt;
		}
		
		public double[] getFuselageSCMiddleBottom() {
			return _fuselageSCMiddleBottomPnt;
		}
		
		public double[] getFuselageSCFrontTop() {
			return _fuselageSCFrontTopPnt;
		}
		
		public double[] getFuselageSCFrontBottom() {
			return _fuselageSCFrontBottomPnt;
		}
		
		public double[] getFuselageSCBackTop() {
			return _fuselageSCBackTopPnt;
		}
		
		public double[] getFuselageSCBackBottom() {
			return _fuselageSCBackBottomPnt;
		}
		
		public Tuple2<List<Double>, List<Double>> getFuselageSCMiddleUpperYZCoords() {
			return _fuselageSCMiddleUpperYZCoords;
		}
		
		public Tuple2<List<Double>, List<Double>> getFuselageSCMiddleLowerYZCoords() {
			return _fuselageSCMiddleLowerYZCoords;
		}
		
		public double getFuselageMinimumZ() {
			return _fuselageMinimumZ;
		}
		
		public double getFuselageMaximumZ() {
			return _fuselageMaximumZ;
		}

		public double[] getFusLSContactPnt() {
			return _fusLSContactPnt;
		}
		
		public double[] getFusFairingUppContactPnt() {
			return _fusFairingUppContactPnt;
		}
		
		public double[] getFusFairingLowContactPnt() {
			return _fusFairingLowContactPnt;
		} 
		
		public double getFairingMinimumZ() {
			return _fairingMinimumZ;
		}
		
		public double getFairingMaximumZ() {
			return _fairingMaximumZ;
		}
		
		public double getFairingReferenceZ() {
			return _fairingReferenceZ;
		}
		
		public double getFairingFrontLength() {
			return _frontLength;
		}
		
		public double getFairingBackLength() {
			return _backLength;
		}
		
		public double getFairingWidth() {
			return _width;
		}
		
		public FairingPosition getFairingPosition() {
			return _fairingPosition;
		}		
	}
}

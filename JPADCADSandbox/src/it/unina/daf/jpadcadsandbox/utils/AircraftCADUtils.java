package it.unina.daf.jpadcadsandbox.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import it.unina.daf.jpadcad.occ.CADExplorer;
import it.unina.daf.jpadcad.occ.CADFace;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCWire;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils.XSpacingType;
import opencascade.TopoDS;
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
						+ "in order to obtain the solid");
				OCCSolid fuselageSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(
						(CADShell) OCCUtils.theFactory.newShape(fuselageRightShell.getShape()), 
						(CADShell) OCCUtils.theFactory.newShape(fuselageLeftShell.getShape()));
				
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
		
		requestedShapes.addAll(supportShapes);
		requestedShapes.addAll(shellShapes);
		requestedShapes.addAll(solidShapes);
		
		return requestedShapes;
				 
	}
	
	public static List<OCCShape> getLiftingSurfaceCAD(LiftingSurface liftingSurface, 
			WingTipType wingTip, 
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
		List<OCCShape> wingTipPatches = new ArrayList<>();
		
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
		// Generate span wise airfoils
		// ---------------------------------
		List<List<CADGeomCurve3D>> airfoilCrvs = new ArrayList<List<CADGeomCurve3D>>();
		
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
		}
		
		List<OCCShape> rightFaces = new ArrayList<>();		
		if (exportShells || exportSolids) {
			// ------------------------------------------------
			// Generate a loft (face element) for each panel
			// ------------------------------------------------		
			rightFaces.addAll(airfoilWires.stream()
					.map(wrs -> OCCUtils.makePatchThruSections(wrs))
					.collect(Collectors.toList()));				   
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
			List<OCCShape> roundedTipShapes = new ArrayList<>();
			
			roundedTipShapes = generateRoundedWingTip(liftingSurface, 
					airfoilCrvs.get(nPanels-1).get(nAirfoils[nPanels-1]-1),
					airfoilCrvs.get(nPanels-1).get(nAirfoils[nPanels-1]-2),
					ptsLE, ptsTE, exportSupportShapes);
			
			// -------------------------------------------------------------
			// Separate the shell from the other shapes, whether necessary
			// -------------------------------------------------------------
			if (exportSupportShapes) {			
				supportShapes.addAll(roundedTipShapes.stream()
						.filter(s -> !(s instanceof OCCShell))
						.collect(Collectors.toList()));
				
//				wingTipShell = roundedTipShapes.stream()
//						.filter(s -> s instanceof OCCShell)
//						.collect(Collectors.toList()).get(0);
			} else {
//				wingTipShell = roundedTipShapes.get(0);
			} //TODO: add a check inside generateRoundedWingTip: in case of exportSupportShapes is false, 
			  //      the function must return a list consisting just of the wing tip shell.
					
			break;
		}
		
		shellShapes.addAll(rightFaces);
		
		requestedShapes.addAll(supportShapes);
		requestedShapes.addAll(shellShapes);
		requestedShapes.addAll(solidShapes);
		
		return requestedShapes;
	}
	
	private static List<OCCShape> generateRoundedWingTip(LiftingSurface liftingSurface, 
			CADGeomCurve3D tipCurve, CADGeomCurve3D preTipCurve,
			List<double[]> ptsLE, List<double[]> ptsTE, boolean exportSupportShapes) {
		
		List<OCCShape> wingTipShapes = new ArrayList<>();
		
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
		
		List<OCCEdge> airfoilTipCrvs = OCCUtils.splitEdge(tipCurve, ptA);
		
		// TODO: to cancel after function completion
		if (exportSupportShapes) {		
			wingTipShapes.addAll(airfoilTipCrvs);
		}
		
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
		
		PVector pvC = PVector.add(pvD, PVector.mult(teVec, (float) tipWidth/teVec.mag()));
		if (liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL))
			pvC.x = (float) ptsTE.get(nPanels)[0];
		else
			pvC.y = (float) ptsTE.get(nPanels)[1];
		
		if (exportSupportShapes) {
			wingTipShapes.add((OCCShape) OCCUtils.theFactory.newWireFromAdjacentEdges(
					OCCUtils.theFactory.newCurve3DP(Arrays.asList(pvA, pvB), false).edge(),
					OCCUtils.theFactory.newCurve3DP(Arrays.asList(pvB, pvC), false).edge(),
					OCCUtils.theFactory.newCurve3DP(Arrays.asList(pvC, pvD), false).edge()
					));
		}
		
		return wingTipShapes;
	}
	
	private static List<double[]> generateAirfoilAtY(double yStation, LiftingSurface liftingSurface) {
		
		List<double[]> airfoilPtsAtY = new ArrayList<>();
		
		// ---------------------------------------
		// Determine the airfoil to be modified
		// ---------------------------------------
		Airfoil baseAirfoil = null;
		for (int i = 1; i <= liftingSurface.getPanels().size(); i++) {
			
			double yIn = liftingSurface.getYBreakPoints().get(i-1).doubleValue(SI.METER);
			double yOut = liftingSurface.getYBreakPoints().get(i).doubleValue(SI.METER);
			
			if (yStation > yIn && yStation < yOut || 
				Math.abs(yStation - yIn) <= 1e-5 || 
				Math.abs(yStation - yOut) <= 1e-5) {			
				baseAirfoil = liftingSurface.getAirfoilList().get(i-1);
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
		
		// ---------------------------------------
		// Calculate airfoil actual coordinates
		// ---------------------------------------
		double x, y, z;
		
		for (int i = 0; i < nPts; i++) {
			
			// Scale to actual dimensions
			x = noDuplicatesCoords.get(i).x * chord;
			y = 0.0;
			z = noDuplicatesCoords.get(i).z * chord;
			
			// Set the rotation due to the twist and the rigging angle			
			double r = Math.sqrt(x*x + z*z);
			x = x - r * (1 - Math.cos(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
			z = z - r * Math.sin(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN));		
			
			// Set the actual location
			x = liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER) + xLE + x;
			
			y = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) - z :
					liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) + yStation;
			
			z = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER) + yStation :
					liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER) + zLE + z;

			airfoilPtsAtY.add(new double[] {x, y, z});
		}
		
		return airfoilPtsAtY;
	}
	
	private static List<double[]> generateChordAtY(double yStation, LiftingSurface liftingSurface) {
		
		List<double[]> chordPtsAtY = new ArrayList<>();
		
		// ---------------------------------------
		// Generate unitary chord points
		// ---------------------------------------		
		double[] unitaryChordXs = new double[] {1, 0};
		double[] unitaryChordZs = new double[] {0, 0};
		
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
		
		// -------------------------------------------
		// Calculate chord points actual coordinates
		// -------------------------------------------
		double x, y, z;
		
		for (int i = 0; i < 2; i++) {
			
			// Scale to actual dimensions
			x = unitaryChordXs[i] * chord;
			y = 0.0;
			z = unitaryChordZs[i] * chord;
			
			// Set the rotation due to the twist and the rigging angle			
			double r = Math.sqrt(x*x + z*z);
			x = x - r * (1 - Math.cos(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN)));
			z = z - r * Math.sin(twist + liftingSurface.getRiggingAngle().doubleValue(SI.RADIAN));		
			
			// Set the actual location
			x = liftingSurface.getXApexConstructionAxes().doubleValue(SI.METER) + xLE + x;
			
			y = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) - z :
					liftingSurface.getYApexConstructionAxes().doubleValue(SI.METER) + yStation;
			
			z = liftingSurface.getType().equals(ComponentEnum.VERTICAL_TAIL) ?
					liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER) + yStation :
					liftingSurface.getZApexConstructionAxes().doubleValue(SI.METER) + zLE + z;

			chordPtsAtY.add(new double[] {x, y, z});
		}		
		
		return chordPtsAtY;
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

	public enum FileExtension {
		BREP,
		STEP,
		IGES,
		STL;
	}
	
	public enum WingTipType {
		CUTOFF,
		ROUNDED;
	}

}


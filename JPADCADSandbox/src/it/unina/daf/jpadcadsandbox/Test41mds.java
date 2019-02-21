package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.log4j.net.SyslogAppender;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.enums.EngineCADComponentsEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.OCCCompSolid;
import it.unina.daf.jpadcad.occ.OCCCompound;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import javaslang.Tuple2;
import javaslang.Tuple3;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRep_Builder;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

public class Test41mds {

	public static void main(String[] args) {
		System.out.println("-------------------------------------------------------------");
		System.out.println("------------------ CAD engine modeling test -----------------");
		System.out.println("-------------------------------------------------------------");
		
		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		List<NacelleCreator> nacelles = aircraft.getNacelles().getNacellesList();
		List<Engine> engines = aircraft.getPowerPlant().getEngineList();
		
		// ---------------------------------
		// Generate the CAD of the aircraft
		// ---------------------------------
		List<OCCShape> exportShapes = new ArrayList<>();
		
		// Generate engine CAD objects
		Map<EngineCADComponentsEnum, String> tpTemplatesMap = new HashMap<>();
		Map<EngineCADComponentsEnum, String> tfTemplatesMap = new HashMap<>();	
		tpTemplatesMap.put(EngineCADComponentsEnum.NACELLE, "TP_nacelle_01.step");
		tpTemplatesMap.put(EngineCADComponentsEnum.BLADE, "TP_blade_02.step");
		tfTemplatesMap.put(EngineCADComponentsEnum.NACELLE, "TF_complete_01.step");
		
		List<Map<EngineCADComponentsEnum, String>> templatesMapList = new ArrayList<>();
		templatesMapList.add(tfTemplatesMap);
		templatesMapList.add(tfTemplatesMap);
		
		boolean exportWires = false;
		boolean exportShells = false;
		boolean exportSolids = true;
		
		List<OCCShape> engineShapes = AircraftCADUtils.getEnginesCAD(
				nacelles, engines, templatesMapList, exportWires, exportShells, exportSolids);
		
		// Generate CAD for the remaining components
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
				fuselage, 7, 7, exportWires, exportShells, exportSolids);
		
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceWingletCAD(
				wing, 1.00, 0.35, 0.25, exportWires, exportShells, exportSolids);
		
		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				hTail, WingTipType.ROUNDED, exportWires, exportShells, exportSolids);
		
		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				vTail, WingTipType.ROUNDED, exportWires, exportShells, exportSolids);
		
		List<OCCShape> fairingShapes = AircraftCADUtils.getFairingShapes(
				fuselage, wing, 
				0.50, 0.50, 1.05, 0.10, 
				0.85, 0.10, 0.85, 
				exportWires, exportShells, exportSolids);
		
		// ---------------------------------
		// Write the results to file
		// ---------------------------------
		exportShapes.addAll(fuselageShapes);
		exportShapes.addAll(wingShapes);
		exportShapes.addAll(hTailShapes);
		exportShapes.addAll(vTailShapes);
		exportShapes.addAll(fairingShapes);
		exportShapes.addAll(engineShapes);
			
		OCCUtils.write("Test41mds", FileExtension.STEP, exportShapes);
	}
	
//	public static List<OCCShape> getEnginesCAD(List<NacelleCreator> nacelles, List<Engine> engines,
//			List<Map<EngineCADComponentsEnum, String>> templateMapsList,
//			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
//		
//		List<OCCShape> supportShapes = new ArrayList<>();
//		List<OCCShape> shellShapes = new ArrayList<>();
//		List<OCCShape> solidShapes = new ArrayList<>();
//		List<OCCShape> requestedShapes = new ArrayList<>();	
//		
//		// ----------------------------------------------------------
//		// Check whether continuing with the method
//		// ----------------------------------------------------------
//		if ((nacelles.isEmpty() || nacelles.stream().anyMatch(n -> n == null)) || 
//			(engines.isEmpty() || engines.stream().anyMatch(e -> e == null))) {			
//			System.out.println("========== [AircraftCADUtils::getEnginesCAD] One or more engine/nacelle object passed to the "
//					+ "getEnginesCAD method is null! Exiting the method ...");
//			return null;
//		}
//		
//		if (!exportSupportShapes && !exportShells && !exportSolids) {
//			System.out.println("========== [AircraftCADUtils::getEnginesCAD] No shapes to export! Exiting the method ...");
//			return null;
//		}
//		
//		System.out.println("========== [AircraftCADUtils::getEnginesCAD]");
//		
//		// ----------------------------------------------------------
//		// Generate engine CAD shapes
//		// ----------------------------------------------------------	
//		List<EngineCAD> engineCADList = new ArrayList<>();
//		IntStream.range(0, engines.size())
//				 .forEach(i -> engineCADList.add(
//						 new EngineCAD(nacelles.get(i), engines.get(i), templateMapsList.get(i))
//						 ));
//		
//		// Check the indexes of the engines that can be mirrored, based on:
//		// - sharing the same y apex coordinate (absolute value);
//		// - sharing the same type, templates and dimensions.
//		// First, it is necessary to sort the engines.
//		List<EngineCAD> sortedEngineCADList = engineCADList.stream()
//				.sorted(Comparator.comparing(e -> e.getEngineYApex()))
//				.collect(Collectors.toList());
//		
//		int[] symmEngines = new int[engines.size()];
//		Arrays.fill(symmEngines, 0);
//		if ((engines.size() & 1) == 0) { // even number of engines		
//			for (int i = 0; i < engines.size()/2; i++) {				
//				if (sortedEngineCADList.get(i).symmetrical(sortedEngineCADList.get(engines.size()-1-i))) {				
//					symmEngines[i] = i + 1;
//					symmEngines[engines.size()-1-i] = i + 1;
//				}					
//			}		
//		} else { // odd	number of engines
//			for (int i = 0; i < (engines.size()-1)/2; i++) {
//				if (sortedEngineCADList.get(i).symmetrical(sortedEngineCADList.get(engines.size()-1-i))) {
//					symmEngines[i] = i + 1;
//					symmEngines[engines.size()-1-i] = i + 1;
//				}
//			}
//		}
//		
//		for (int i = 0; i < engines.size(); i++) {
//			
//			if (symmEngines[i] != 0 && i < engines.size()/2) {
//				
//				List<OCCShape> engineShapes = getEngineCAD(sortedEngineCADList.get(i));
//				
//				List<OCCShape> symmEngineShapes = OCCUtils.getShapesTranslated(
//						engineShapes, 
//						new double[] {
//								sortedEngineCADList.get(i).getEngineXApex(),
//								sortedEngineCADList.get(i).getEngineYApex(),
//								sortedEngineCADList.get(i).getEngineZApex()
//						}, 
//						new double[] {
//								sortedEngineCADList.get(i).getEngineXApex(),
//							    sortedEngineCADList.get(i).getEngineYApex()*(-1),
//								sortedEngineCADList.get(i).getEngineZApex()
//						});
//				
//				
//				solidShapes.addAll(engineShapes);
//				solidShapes.addAll(symmEngineShapes);
//				
//			} else if (symmEngines[i] == 0) {
//				
//				solidShapes.addAll(getEngineCAD(sortedEngineCADList.get(i)));
//			}
//		}
//		
//		OCCCompound solidsCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				solidShapes.stream().map(s -> (OCCShape) s).collect(Collectors.toList()));
//		
//		if (exportSolids) {
//			requestedShapes.addAll(solidShapes);
//		}
//		
//		if (exportSupportShapes) {
//			OCCExplorer wireExp = new OCCExplorer();
//			wireExp.init(solidsCompound, CADShapeTypes.WIRE);
//			while (wireExp.more()) {
//				supportShapes.add((OCCShape) wireExp.current());
//				wireExp.next();
//			}
//		}
//		
//		if (exportShells) {
//			OCCExplorer shellExp = new OCCExplorer();
//			shellExp.init(solidsCompound, CADShapeTypes.SHELL);
//			while (shellExp.more()) {
//				shellShapes.add((OCCShape) shellExp.current());
//				shellExp.next();
//			}
//		}
//	
//		requestedShapes.addAll(supportShapes);
//		requestedShapes.addAll(shellShapes);
//		
//		return requestedShapes;
//	}
//	
//	public static List<OCCShape> getEngineCAD(EngineCAD engineCAD) {
//		
//		List<OCCShape> requestedShapes = new ArrayList<>();	
//		
//		// -----------------------------------
//		// Call to specific CAD engine method
//		// -----------------------------------
//		switch (engineCAD.getEngineType()) {
//		
//		case TURBOPROP:
//			requestedShapes.addAll(getTurbopropEngineCAD(engineCAD));
//			
//			break;
//			
//		case TURBOFAN:
//			requestedShapes.addAll(getTurbofanEngineCAD(engineCAD));
//			
//			break;
//			
//		default:
//			System.err.println("No CAD templates are currently available for " +  engineCAD.getEngineType() + " engines. "
//					+ "No engine CAD shapes will be produced!");
//			
//			break;
//		}
//		
//		return requestedShapes;
//	} 
//	
//	public static List<OCCShape> getTurbopropEngineCAD(EngineCAD engineCAD) {
//		
//		// ----------------------------------------------------------
//		// Check the factory
//		// ----------------------------------------------------------
//		if (OCCUtils.theFactory == null) {
//			System.out.println("========== [AircraftCADUtils::getEngineCAD] Initialize CAD shape factory");
//			OCCUtils.initCADShapeFactory();
//		}
//		
//		// ----------------------------------------------------------
//		// Initialize shape lists
//		// ----------------------------------------------------------	
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// ----------------------------------------------------------
//		// Importing the templates
//		// ----------------------------------------------------------	
//		MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
//		String inputFolderPath = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + 
//				 				 "CAD_engine_templates" + File.separator + 
//				                 "turboprop_templates" + File.separator;
//		
//		// Reading the NACELLE
//		OCCShape nacelleShapes = ((OCCShape) OCCUtils.theFactory.newShape(
//				inputFolderPath + engineCAD.getEngineCADTemplates().get(EngineCADComponentsEnum.NACELLE), "M"));
//		
//		// Reading the BLADE
//		OCCShape bladeShapes = ((OCCShape) OCCUtils.theFactory.newShape(
//				inputFolderPath + engineCAD.getEngineCADTemplates().get(EngineCADComponentsEnum.BLADE), "M"));
//		
//		// ------------------------------------------------------------------------
//		// Instantiate necessary translations, rotations, scalings, and affinities
//		// ------------------------------------------------------------------------		
////		nacelleShapes.Reverse(); // TODO: add this correction to the templates
////		bladeShapes.Reverse();		
//		
//		double[] xDir = new double[] {1.0, 0.0, 0.0};	
//		double[] yDir = new double[] {0.0, 1.0, 0.0};
//		double[] zDir = new double[] {0.0, 0.0, 1.0};
//		
//		double[] nacelleCG = OCCUtils.getShapeCG(nacelleShapes);
//		double[] bladeCG = OCCUtils.getShapeCG(bladeShapes);
//		
//		// ------------------------------------------------------------------------
//		// Apply transformation to imported templates
//		// ------------------------------------------------------------------------	
//		
//		// NACELLE	
//		OCCCompound nacelleRefCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				(OCCShape) OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0),
//				nacelleShapes
//				);
//		
//		double nacelleLengthStretchingFactor = engineCAD.getNacelleLength()/engineCAD.getTurbopropTemplateNacelleLength();
//		double nacelleHeightStretchingFactor = engineCAD.getNacelleMaxDiameter()/engineCAD.getTurbopropTemplateNacelleMaxDiameter();
//		
//		OCCShape xStretchedNacelle = OCCUtils.getShapeStretched(nacelleRefCompound, nacelleCG, xDir, nacelleLengthStretchingFactor);
//		OCCShape xzStretchedNacelle = OCCUtils.getShapeStretched(xStretchedNacelle, nacelleCG, zDir, nacelleHeightStretchingFactor);
//		OCCShape xyzStretchedNacelle = OCCUtils.getShapeStretched(xzStretchedNacelle, nacelleCG, yDir, nacelleHeightStretchingFactor);
//		
//		OCCExplorer nacelleRefCompoundExp = new OCCExplorer();
//		
//		nacelleRefCompoundExp.init(xyzStretchedNacelle, CADShapeTypes.VERTEX);	
//		double[] refPnt = ((OCCVertex) nacelleRefCompoundExp.current()).pnt();
//	
//		nacelleRefCompoundExp.init(xyzStretchedNacelle, CADShapeTypes.SOLID);		
//		OCCShape stretchedNacelle = (OCCShape) nacelleRefCompoundExp.current();
//		
//		OCCShape finalNacelle = OCCUtils.getShapeTranslated(
//				stretchedNacelle, refPnt, new double[] {
//						engineCAD.getEngineXApex(), 
//						engineCAD.getEngineYApex(), 
//						engineCAD.getEngineZApex()
//						});
//		
//		// BLADE	
//		OCCCompound bladeRefCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0),
//				bladeShapes
//				);
//		
//		double[] bladeRotVec = MyArrayUtils.linspace(0, 2*Math.PI, engineCAD.getNumberOfBlades() + 1);
//		
//		double scaledHubRadius = nacelleHeightStretchingFactor*engineCAD.getTurbopropTemplateHubDiameter()/2;
//		double bladeScalingFactor = engineCAD.getPropellerDiameter()/(2*(engineCAD.getTurbopropTemplateBladeLength() + engineCAD.getTurbopropTemplateHubDiameter()/2));
//		double scaledHubLength = nacelleLengthStretchingFactor*engineCAD.getTurbopropTemplateHubLengthRatio()*engineCAD.getTurbopropTemplateNacelleLength();
//		double scaledHubZCoord = nacelleHeightStretchingFactor*engineCAD.getTurbopropTemplateHubCenterZCoord();
//		
//		OCCShape scaledBladeRefCompound = OCCUtils.getShapeScaled(bladeRefCompound, bladeCG, bladeScalingFactor);
//		
//		OCCExplorer bladeRefCompoundExp = new OCCExplorer();
//		
//		bladeRefCompoundExp.init(scaledBladeRefCompound, CADShapeTypes.VERTEX);	
//		double[] bladeRefPnt = ((OCCVertex) bladeRefCompoundExp.current()).pnt();
//		
//		bladeRefCompoundExp.init(scaledBladeRefCompound, CADShapeTypes.SOLID);	
//		OCCShape scaledBlade = (OCCShape) bladeRefCompoundExp.current();
//			
//		// Blade stretching, whether it is necessary
//		double bladeLengthScaled = bladeScalingFactor*engineCAD.getTurbopropTemplateBladeMaxBaseDiameter();
//		double totalBladeBaseLength = engineCAD.getNumberOfBlades()*bladeLengthScaled;
//		double scaledHubCircle = 2*Math.PI*scaledHubRadius;
//		
//		if (totalBladeBaseLength > 1.10*scaledHubCircle) {
//			System.out.println("... Stretching the blade in order to fit it on the hub ...");
//			
//			double bladeLengthScalingFactor = 0.95*(scaledHubCircle/engineCAD.getNumberOfBlades())/
//					engineCAD.getTurbopropTemplateBladeMaxBaseDiameter();
//			
//			OCCShape bladeXStretched = OCCUtils.getShapeStretched(scaledBlade, bladeCG, xDir, bladeLengthScalingFactor);
//			OCCShape bladeYStretched = OCCUtils.getShapeStretched(bladeXStretched, bladeCG, yDir, bladeLengthScalingFactor);
//			
//			scaledBlade = bladeYStretched;			
//		}
//		
//		OCCShape hubTranslatedBlade = OCCUtils.getShapeTranslated(scaledBlade, bladeRefPnt, new double[] {0.0, 0.0, scaledHubRadius});
//			
//		List<OCCShape> rotatedBlades = new ArrayList<>();
//		rotatedBlades.add(hubTranslatedBlade);
//		for (int j = 1; j < bladeRotVec.length - 1; j++) 
//			rotatedBlades.add(OCCUtils.getShapeRotated(
//					hubTranslatedBlade, 
//					new double[] {0.0, 0.0, 0.0}, 
//					xDir, 
//					bladeRotVec[j])
//					);
//		
//		OCCCompound bladesCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				rotatedBlades.stream().map(b -> (CADShape) b).collect(Collectors.toList()));
//		
//		double[] propellerRefPnt = new double[] {
//				engineCAD.getEngineXApex() - scaledHubLength/2,
//				engineCAD.getEngineYApex(),
//				engineCAD.getEngineZApex() + scaledHubZCoord
//		};
//		
//		OCCShape translatedBladeCompound = OCCUtils.getShapeTranslated(
//				bladesCompound, new double[] {0.0, 0.0, 0.0}, propellerRefPnt);
//		
//		OCCExplorer bladesCompoundExp = new OCCExplorer();
//		bladesCompoundExp.init(translatedBladeCompound, CADShapeTypes.SOLID);
//		
//		List<OCCShape> translatedBlades = new ArrayList<>();
//		while (bladesCompoundExp.more()) {
//			translatedBlades.add((OCCShape) bladesCompoundExp.current());
//			bladesCompoundExp.next();
//		}
//			
//		// Add all produced shapes to a compound of solids		
//		OCCCompSolid turbopropCompSolid = (OCCCompSolid) OCCUtils.theFactory.newCompSolid(
//				translatedBlades.stream().map(s -> (CADShape) s).collect(Collectors.toList()));		
//		turbopropCompSolid.add(finalNacelle);
//		
//		// Rotate the compound of solids according to the tilting angle
//		OCCShape rotatedTurbopropCompSolid = OCCUtils.getShapeRotated(
//				turbopropCompSolid, 
//				new double[] {engineCAD.getEngineXApex(), engineCAD.getEngineYApex(), engineCAD.getEngineZApex()}, 
//				yDir, 
//				engineCAD.getTiltingAngle()
//				);
//		
//		// ------------------------------------------------------------------------
//		// Export solid shapes
//		// ------------------------------------------------------------------------			
//		OCCExplorer tpSolidExp = new OCCExplorer();
//		tpSolidExp.init(rotatedTurbopropCompSolid, CADShapeTypes.SOLID);
//		while (tpSolidExp.more()) {
//			solidShapes.add((OCCShape) tpSolidExp.current());
//			tpSolidExp.next();
//		}
//		
//		return solidShapes;
//	}
//	
//	public static List<OCCShape> getTurbofanEngineCAD(EngineCAD engineCAD) {
//		
//		// ----------------------------------------------------------
//		// Check the factory
//		// ----------------------------------------------------------
//		if (OCCUtils.theFactory == null) {
//			System.out.println("========== [AircraftCADUtils::getEngineCAD] Initialize CAD shape factory");
//			OCCUtils.initCADShapeFactory();
//		}
//		
//		// ----------------------------------------------------------
//		// Initialize shape lists
//		// ----------------------------------------------------------	
//		List<OCCShape> solidShapes = new ArrayList<>();
//		
//		// ----------------------------------------------------------
//		// Initialize directions
//		// ----------------------------------------------------------	
//		double[] xDir = new double[] {1.0, 0.0, 0.0};	
//		double[] yDir = new double[] {0.0, 1.0, 0.0};
//		double[] zDir = new double[] {0.0, 0.0, 1.0};
//		
//		// ----------------------------------------------------------
//		// Importing the template
//		// ----------------------------------------------------------	
//		MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
//		String inputFolderPath = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + 
//				 				 "CAD_engine_templates" + File.separator + 
//				                 "turbofan_templates" + File.separator;
//		
//		OCCShape engineShapes = (OCCShape) OCCUtils.theFactory.newShape(
//				inputFolderPath + engineCAD.engineCADTemplates.get(EngineCADComponentsEnum.NACELLE), "M");
//		
//		OCCExplorer engineShapesExp = new OCCExplorer();
//		engineShapesExp.init(engineShapes, CADShapeTypes.SOLID);
//		
//		List<OCCShape> engineSolids = new ArrayList<>();
//		while (engineShapesExp.more()) {
//			engineSolids.add((OCCShape) engineShapesExp.current());
//			engineShapesExp.next();
//		} 
//		
//		// ------------------------------------------------------------------------
//		// Apply transformation to the imported template
//		// ------------------------------------------------------------------------	
//		double[] engineCG = OCCUtils.getShapeCG(engineShapes);
//		
//		OCCCompound engineRefCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0),
//				engineShapes
//				);
//		
//		double engineInnerCasingRadiusRatio = 1 - engineCAD.getTurbofanTemplateInnerOuterCasingCoeff()*engineCAD.getByPassRatio();
//		
//		double engineLengthStretchingFactor = engineCAD.getNacelleLength()/engineCAD.getTurbofanTemplateNacelleLength();
//		double engineHeightStretchingFactor = engineCAD.getNacelleMaxDiameter()/engineCAD.getTurbofanTemplateNacelleMaxDiameter();
//		double engineInnerCasingHeightStretchingFactor = engineInnerCasingRadiusRatio*engineHeightStretchingFactor;
//		
//		OCCShape xStretchedEngine = OCCUtils.getShapeStretched(engineRefCompound, engineCG, xDir, engineLengthStretchingFactor);
//		OCCShape xzStretchedEngine = OCCUtils.getShapeStretched(xStretchedEngine, engineCG, zDir, engineHeightStretchingFactor);
//		OCCShape xyzStretchedEngine = OCCUtils.getShapeStretched(xzStretchedEngine, engineCG, yDir, engineHeightStretchingFactor);
//		
//		OCCExplorer engineRefCompoundExp = new OCCExplorer();
//		
//		engineRefCompoundExp.init(xyzStretchedEngine, CADShapeTypes.VERTEX);
//		OCCVertex newEngineApex = (OCCVertex) engineRefCompoundExp.current();
//		
//		engineRefCompoundExp.init(xyzStretchedEngine, CADShapeTypes.SOLID);	
//		List<OCCShape> engineStretchedSolids = new ArrayList<>();
//		while (engineRefCompoundExp.more()) {
//			engineStretchedSolids.add((OCCShape) engineRefCompoundExp.current());
//			engineRefCompoundExp.next();
//		}	
//		
//		OCCCompound engineInnerCasingCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				engineStretchedSolids.get(1),
//				engineStretchedSolids.get(2)
//				);
//		
//		double[] engineInnerCasingCG = OCCUtils.getShapeCG(engineInnerCasingCompound);
//		
//		OCCShape yStretchedEngineInnerCasing = OCCUtils.getShapeStretched(
//				engineInnerCasingCompound, engineInnerCasingCG, yDir, engineInnerCasingHeightStretchingFactor);
//		OCCShape yzStretchedEngineInnerCasing = OCCUtils.getShapeStretched(
//				yStretchedEngineInnerCasing, engineInnerCasingCG, zDir, engineInnerCasingHeightStretchingFactor);
//		
//		OCCExplorer engineInnerCasingCompoundExp = new OCCExplorer();
//		engineInnerCasingCompoundExp.init(yzStretchedEngineInnerCasing, CADShapeTypes.SOLID);
//		
//		List<OCCShape> stretchedEngineInnerCasingSolids = new ArrayList<>();
//		while (engineInnerCasingCompoundExp.more()) {
//			stretchedEngineInnerCasingSolids.add((OCCShape) engineInnerCasingCompoundExp.current());
//			engineInnerCasingCompoundExp.next();
//		}
//		
//		List<OCCShape> stretchedEngineSolids = new ArrayList<>();
//		stretchedEngineSolids.add(engineStretchedSolids.get(0));
//		stretchedEngineSolids.addAll(stretchedEngineInnerCasingSolids);
//		stretchedEngineSolids.add(engineStretchedSolids.get(3));
//		
//		OCCCompound stretchedEngineCompound = (OCCCompound) OCCUtils.theFactory.newCompound(
//				stretchedEngineSolids.stream().map(s -> (CADShape) s).collect(Collectors.toList()));
//		
//		OCCShape translatedEngineCompound = OCCUtils.getShapeTranslated(
//				stretchedEngineCompound, 
//				newEngineApex.pnt(), 
//				new double[] {
//						engineCAD.getEngineXApex(),
//						engineCAD.getEngineYApex(),
//						engineCAD.getEngineZApex()
//				});
//		
//		OCCShape rotatedEngineCompound = OCCUtils.getShapeRotated(
//				translatedEngineCompound, 
//				new double[] {
//						engineCAD.getEngineXApex(),
//						engineCAD.getEngineYApex(),
//						engineCAD.getEngineZApex()
//				}, 
//				yDir, 
//				engineCAD.getTiltingAngle()
//				);
//		
//		OCCExplorer finalEngineCompoundExp = new OCCExplorer();
//		finalEngineCompoundExp.init(rotatedEngineCompound, CADShapeTypes.SOLID);
//		
//		List<OCCShape> finalEngineSolids = new ArrayList<>();
//		while (finalEngineCompoundExp.more()) {
//			finalEngineSolids.add((OCCShape) finalEngineCompoundExp.current());
//			finalEngineCompoundExp.next();
//		}
//		
//		// ------------------------------------------------------------------------
//		// Export requested shapes
//		// ------------------------------------------------------------------------	
//		solidShapes.addAll(finalEngineSolids);
//		
//		return solidShapes;
//	}
//	
//	public static class EngineCAD {
//		
//		// ---------------------------
//		// Available engine templates
//		// ---------------------------
//		private String engineTemplatesDataFilePath;
//		private JPADXmlReader templatesDataReader;
//		private Map<EngineCADComponentsEnum, String> defEngineCADTemplates = new HashMap<>();
//		
//		// TURBOPROP
//		private List<String> tpNacelleTemplates = new ArrayList<>();	
//		private List<String> tpBladeTemplates = new ArrayList<>();
//		
//		// TURBOFAN
//		private List<String> tfNacelleTemplates = new ArrayList<>();
//		
//		// ---------------
//		// Templates data
//		// ---------------
//		
//		// TURBOFAN
//		private double tfTemplateNacelleLength = 0;
//		private double tfTemplateNacelleMaxDiameter = 0;
//		private double tfTemplateInnerOuterCasingCoeff = 0;
//		
//		// TURBOPROP
//		private double tpTemplateNacelleLength = 0;
//		private double tpTemplateNacelleMaxDiameter = 0;
//		
//		private double tpTemplateHubDiameter = 0;
//		private double tpTemplateHubCenterZCoord = 0;
//		private double tpTemplateHubLengthRatio = 0;
//		
//		private double tpTemplateBladeMaxBaseDiameter = 0;
//		private double tpTemplateBladeLength = 0;
//		
//		// -----------
//		// Attributes
//		// -----------
//		private Map<EngineCADComponentsEnum, String> engineCADTemplates = new HashMap<>();
//		
//		private EngineTypeEnum engineType;
//		
//		private double engineXApex = 0;
//		private double engineYApex = 0;
//		private double engineZApex = 0;
//		
//		private double tiltingAngle = 0;
//		
//		private double nacelleLength = 0;
//		private double nacelleMaxDiameter = 0;
//		
//		private int numberOfBlades = 0;
//		private double propellerDiameter = 0;
//		
//		private double byPassRatio = 0;
//		
//		// ------------
//		// Constructor
//		// ------------
//		public EngineCAD(NacelleCreator nacelle, Engine engine, Map<EngineCADComponentsEnum, String> templateFilenames) {
//			
//			if (checkTemplateListsEmptiness()) {			
//				setTemplatesLists();
//			}			
//			
//			this.engineType = engine.getEngineType();
//			setDefaultTemplates(engineType);
//			
//			Set<EngineCADComponentsEnum> tpTemplateSet = new HashSet<>();
//			switch (engineType) {
//
//			case TURBOPROP:			
//				tpTemplateSet.add(EngineCADComponentsEnum.BLADE);
//				tpTemplateSet.add(EngineCADComponentsEnum.NACELLE);
//				
//				if (templateFilenames.keySet().equals(tpTemplateSet)) {
//					
//					if (tpBladeTemplates.contains(templateFilenames.get(EngineCADComponentsEnum.BLADE)) &&
//						tpNacelleTemplates.contains(templateFilenames.get(EngineCADComponentsEnum.NACELLE))) {
//						
//						this.engineCADTemplates = templateFilenames;
//						
//					} else {
//						System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");
//
//						this.engineCADTemplates = defEngineCADTemplates;
//					}
//					
//				} else {				
//					System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");
//					
//					this.engineCADTemplates = defEngineCADTemplates;
//				}
//				
//				break;
//
//			case TURBOFAN:
//				tpTemplateSet.add(EngineCADComponentsEnum.NACELLE);
//				
//				if (templateFilenames.keySet().equals(tpTemplateSet)) {
//					
//					if (tfNacelleTemplates.contains(templateFilenames.get(EngineCADComponentsEnum.NACELLE))) {
//						
//						this.engineCADTemplates = templateFilenames;
//						
//					} else {
//						System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");
//
//						this.engineCADTemplates = defEngineCADTemplates;				
//					}
//
//				} else {	
//					System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");
//
//					this.engineCADTemplates = defEngineCADTemplates;
//				}
//
//				break;
//
//			default:			
//				System.err.println("No CAD templates are currently available for " +  engineType + " engines. "
//						+ "No engine CAD shapes will be produced!");
//
//				return;
//			}
//			
//			this.engineXApex = engine.getXApexConstructionAxes().doubleValue(SI.METER);
//			this.engineYApex = engine.getYApexConstructionAxes().doubleValue(SI.METER);
//			this.engineZApex = engine.getZApexConstructionAxes().doubleValue(SI.METER);
//			
//			this.tiltingAngle = engine.getTiltingAngle().doubleValue(SI.RADIAN);
//			
//			this.nacelleLength = nacelle.getLength().doubleValue(SI.METER);
//			this.nacelleMaxDiameter = nacelle.getDiameterMax().doubleValue(SI.METER);		
//			
//			if (engineType.equals(EngineTypeEnum.PISTON) || 
//				engineType.equals(EngineTypeEnum.PROPFAN) || 
//				engineType.equals(EngineTypeEnum.TURBOPROP)) {
//				
//				this.numberOfBlades = engine.getNumberOfBlades();
//				this.propellerDiameter = engine.getPropellerDiameter().doubleValue(SI.METER);
//			}
//
//			if (engineType.equals(EngineTypeEnum.TURBOFAN)) {
//				
//				this.byPassRatio = engine.getBPR();
//			}
//			
//			readTemplatesData(engineType, engineCADTemplates);
//		}
//		
//		private boolean checkTemplateListsEmptiness() {
//			List<List<String>> templateLists = new ArrayList<List<String>>();
//			templateLists.add(tpNacelleTemplates);
//			templateLists.add(tpBladeTemplates);
//			templateLists.add(tfNacelleTemplates);
//			
//			return templateLists.stream()
//								.map(l -> l.isEmpty())
//								.collect(Collectors.toList())
//								.contains(true);
//		}
//		
//		private void setTemplatesLists() {
//			MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
//			engineTemplatesDataFilePath = MyConfiguration.inputDirectory + 
//					"CAD_Engine_Templates" + File.separator + 
//					"engine_templates_data.xml";
//			
//			templatesDataReader = new JPADXmlReader(engineTemplatesDataFilePath);
//			
//			tfNacelleTemplates = MyXMLReaderUtils.getXMLPropertiesByPath(
//					templatesDataReader.getXmlDoc(), templatesDataReader.getXpath(), "//turbofan/nacelle/@file");
//			tpNacelleTemplates = MyXMLReaderUtils.getXMLPropertiesByPath(
//					templatesDataReader.getXmlDoc(), templatesDataReader.getXpath(), "//turboprop/nacelle/@file");
//			tpBladeTemplates = MyXMLReaderUtils.getXMLPropertiesByPath(
//					templatesDataReader.getXmlDoc(), templatesDataReader.getXpath(), "//turboprop/blade/@file");
//		}
//		
//		private void setDefaultTemplates(EngineTypeEnum engineType) {
//			
//			switch (engineType) {
//
//			case TURBOPROP:
//				this.defEngineCADTemplates.put(EngineCADComponentsEnum.NACELLE, tpNacelleTemplates.get(0));
//				this.defEngineCADTemplates.put(EngineCADComponentsEnum.BLADE, tpBladeTemplates.get(0));	
//				
//				break;
//
//			case TURBOFAN:
//				this.defEngineCADTemplates.put(EngineCADComponentsEnum.NACELLE, tfNacelleTemplates.get(0));
//
//				break;
//
//			default:
//				System.err.println("No CAD templates are currently available for " +  engineType + " engines. "
//						+ "No engine CAD shapes will be produced!");
//
//				return;
//			}
//				
//		}
//		
//		private void readTemplatesData(EngineTypeEnum engineType, 
//				Map<EngineCADComponentsEnum, String> templatesMap) {
//			
//			switch (engineType) {
//
//			case TURBOPROP:
//				NodeList tpNacelleTemplatesNodelist = MyXMLReaderUtils.getXMLNodeListByPath(
//						templatesDataReader.getXmlDoc(), "//turboprop/nacelle");
//				
//				NodeList tpBladeTemplatesNodelist = MyXMLReaderUtils.getXMLNodeListByPath(
//						templatesDataReader.getXmlDoc(), "//turboprop/blade");
//				
//				for (int i = 0; i < tpNacelleTemplatesNodelist.getLength(); i++) {				
//					if (((Element) tpNacelleTemplatesNodelist.item(i)).getAttribute("file")
//							.equals(templatesMap.get(EngineCADComponentsEnum.NACELLE))) {
//						Element selectedNacelle = (Element) tpNacelleTemplatesNodelist.item(i);
//						
//						this.tpTemplateNacelleLength = getLengthToMeters(
//								Double.parseDouble(selectedNacelle.getElementsByTagName("length").item(0).getTextContent()),
//								selectedNacelle.getElementsByTagName("length").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tpTemplateNacelleMaxDiameter = getLengthToMeters(
//								Double.parseDouble(selectedNacelle.getElementsByTagName("max_diameter").item(0).getTextContent()), 
//								selectedNacelle.getElementsByTagName("max_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tpTemplateHubDiameter = getLengthToMeters(
//								Double.parseDouble(selectedNacelle.getElementsByTagName("hub_diameter").item(0).getTextContent()), 
//								selectedNacelle.getElementsByTagName("hub_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tpTemplateHubCenterZCoord = getLengthToMeters(
//								Double.parseDouble(selectedNacelle.getElementsByTagName("hub_center_z_coord").item(0).getTextContent()), 
//								selectedNacelle.getElementsByTagName("hub_center_z_coord").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tpTemplateHubLengthRatio = Double.parseDouble(selectedNacelle
//								.getElementsByTagName("hub_length_ratio").item(0).getTextContent());
//					}
//				}
//				
//				for (int i = 0; i < tpBladeTemplatesNodelist.getLength(); i++) {
//					if (((Element) tpBladeTemplatesNodelist.item(i)).getAttribute("file")
//							.equals(templatesMap.get(EngineCADComponentsEnum.BLADE))) {						
//						Element selectedBlade = (Element) tpBladeTemplatesNodelist.item(i);
//						
//						this.tpTemplateBladeMaxBaseDiameter = getLengthToMeters(
//								Double.parseDouble(selectedBlade.getElementsByTagName("base_max_diameter").item(0).getTextContent()), 
//								selectedBlade.getElementsByTagName("base_max_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tpTemplateBladeLength = getLengthToMeters(
//								Double.parseDouble(selectedBlade.getElementsByTagName("length").item(0).getTextContent()), 
//								selectedBlade.getElementsByTagName("length").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//					}
//				}
//
//				break;
//
//			case TURBOFAN:
//				NodeList tfNacelleTemplatesNodelist = MyXMLReaderUtils.getXMLNodeListByPath(
//						templatesDataReader.getXmlDoc(), "//turbofan/nacelle");
//				
//				for (int i = 0; i < tfNacelleTemplatesNodelist.getLength(); i++) {
//					if (((Element) tfNacelleTemplatesNodelist.item(i)).getAttribute("file")
//							.equals(templatesMap.get(EngineCADComponentsEnum.NACELLE))) {
//						Element selectedNacelle = (Element) tfNacelleTemplatesNodelist.item(i);
//						
//						this.tfTemplateNacelleLength = getLengthToMeters(
//								Double.parseDouble(selectedNacelle.getElementsByTagName("length").item(0).getTextContent()),
//								selectedNacelle.getElementsByTagName("length").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tfTemplateNacelleMaxDiameter = getLengthToMeters(
//								Double.parseDouble(selectedNacelle.getElementsByTagName("max_diameter").item(0).getTextContent()), 
//								selectedNacelle.getElementsByTagName("max_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
//								);
//						
//						this.tfTemplateInnerOuterCasingCoeff = Double.parseDouble(selectedNacelle
//								.getElementsByTagName("inn_out_casing_coeff").item(0).getTextContent());
//					}
//				}
//
//				break;
//
//			default:
//				System.err.println("No CAD templates are currently available for " +  engineType + " engines. "
//						+ "No engine CAD shapes will be produced!");
//
//				return;
//			}
//		}
//		
//		private double getLengthToMeters(double length, String units) {
//			
//			if (units.equalsIgnoreCase("m") || units.equalsIgnoreCase("meter") || units.equalsIgnoreCase("meters") ||
//				units.equalsIgnoreCase("metre") || units.equalsIgnoreCase("metres")) {
//				return length;
//				
//			} else if (units.equalsIgnoreCase("mm") || units.equalsIgnoreCase("millimeter") || units.equalsIgnoreCase("millimeters") ||
//					   units.equalsIgnoreCase("millimetre") || units.equalsIgnoreCase("millimetres")) {
//				return Amount.valueOf(length, SI.MILLIMETER).doubleValue(SI.METER);
//				
//			} else if (units.equalsIgnoreCase("ft") || units.equalsIgnoreCase("feet") || units.equalsIgnoreCase("foot")) {
//				return Amount.valueOf(length, NonSI.FOOT).doubleValue(SI.METER);
//				
//			} else if (units.equalsIgnoreCase("in") || units.equalsIgnoreCase("inch") || units.equalsIgnoreCase("inches")) {
//				return Amount.valueOf(length, NonSI.INCH).doubleValue(SI.METER);
//				
//			} else {
//				System.err.println("Warning: select appropriate units for the template input file");
//				return 0;
//			}
//		}
//		
//		public boolean symmetrical(Object obj) {			
//			if (obj == null) 
//				return false;
//			
//			if (!EngineCAD.class.isAssignableFrom(obj.getClass())) 
//				return false;
//			
//			final EngineCAD otherEngine = (EngineCAD) obj;
//			if (!this.engineCADTemplates.equals(otherEngine.getEngineCADTemplates()) || 
//				!this.engineType.equals(otherEngine.getEngineType()) || 
//				!(Double.valueOf(this.engineXApex).equals(Double.valueOf(otherEngine.getEngineXApex()))) || 
//				!(Double.valueOf(Math.abs(this.engineYApex)).equals(Double.valueOf(Math.abs(otherEngine.getEngineYApex()))) || 
//				!(Double.valueOf(this.engineZApex).equals(Double.valueOf(otherEngine.getEngineZApex())))) ||
//				!(Double.valueOf(this.tiltingAngle).equals(Double.valueOf(otherEngine.getTiltingAngle()))) ||
//				!(Double.valueOf(this.nacelleLength).equals(Double.valueOf(otherEngine.getNacelleLength()))) ||
//				!(Double.valueOf(this.nacelleMaxDiameter).equals(Double.valueOf(otherEngine.getNacelleMaxDiameter()))) ||
//				!(Double.valueOf(this.numberOfBlades).equals(Double.valueOf(otherEngine.getNumberOfBlades()))) ||
//				!(Double.valueOf(this.propellerDiameter).equals(Double.valueOf(otherEngine.getPropellerDiameter()))) ||
//				!(Double.valueOf(this.byPassRatio).equals(Double.valueOf(otherEngine.getByPassRatio())))) {
//				return false;
//			}
//							
//			return true;
//			
//		}
//		
//		public double getTurbofanTemplateNacelleLength() {
//			return this.tfTemplateNacelleLength;
//		}
//		
//		public void setTurbofanTemplateNacelleLength(double tfTemplateNacelleLength) {
//			this.tfTemplateNacelleLength = tfTemplateNacelleLength;
//		}
//		
//		public double getTurbofanTemplateNacelleMaxDiameter() {
//			return this.tfTemplateNacelleMaxDiameter;
//		}
//		
//		public void setTurbofanTemplateNacelleMaxDiameter(double tfTemplateNacelleMaxDiameter) {
//			this.tfTemplateNacelleMaxDiameter = tfTemplateNacelleMaxDiameter;
//		}
//		
//		public double getTurbofanTemplateInnerOuterCasingCoeff() {
//			return this.tfTemplateInnerOuterCasingCoeff;
//		}
//		
//		public void setTurbofanTemplateInnerOuterCasingCoeff(double tfTemplateInnerOuterCasingCoeff) {
//			this.tfTemplateInnerOuterCasingCoeff = tfTemplateInnerOuterCasingCoeff;
//		}
//		
//		public double getTurbopropTemplateNacelleLength() {
//			return this.tpTemplateNacelleLength;
//		}
//		
//		public void setTurbopropTemplateNacelleLength(double tpTemplateNacelleLength) {
//			this.tpTemplateNacelleLength = tpTemplateNacelleLength;
//		}
//		
//		public double getTurbopropTemplateNacelleMaxDiameter() {
//			return this.tpTemplateNacelleMaxDiameter;
//		}
//		
//		public void setTurbopropTemplateNacelleMaxDiameter(double tpTemplateNacelleMaxDiameter) {
//			this.tpTemplateNacelleMaxDiameter = tpTemplateNacelleMaxDiameter;
//		}
//		
//		public double getTurbopropTemplateHubDiameter() {
//			return this.tpTemplateHubDiameter;
//		}
//		
//		public void setTurbopropTemplateHubDiameter(double tpTemplateHubDiameter) {
//			this.tpTemplateHubDiameter = tpTemplateHubDiameter;
//		}
//		
//		public double getTurbopropTemplateHubCenterZCoord() {
//			return this.tpTemplateHubCenterZCoord;
//		}
//		
//		public void setTurbopropTemplateHubCenterZCoord(double tpTemplateHubCenterZCoord) {
//			this.tpTemplateHubCenterZCoord = tpTemplateHubCenterZCoord;
//		}
//		
//		public double getTurbopropTemplateHubLengthRatio() {
//			return this.tpTemplateHubLengthRatio;
//		}
//		
//		public void setTurbopropTemplateHubLengthRatio(double tpTemplateHubLengthRatio) {
//			this.tpTemplateHubLengthRatio = tpTemplateHubLengthRatio;
//		}
//		
//		public double getTurbopropTemplateBladeMaxBaseDiameter() {
//			return this.tpTemplateBladeMaxBaseDiameter;
//		}
//		
//		public void setTurbopropTemplateBladeMaxBaseDiameter(double tpTemplateBladeMaxBaseDiameter) {
//			this.tpTemplateBladeMaxBaseDiameter = tpTemplateBladeMaxBaseDiameter;
//		}
//		
//		public double getTurbopropTemplateBladeLength() {
//			return this.tpTemplateBladeLength;
//		}
//		
//		public void setTurbopropTemplateBladeLength(double tpTemplateBladeLength) {
//			this.tpTemplateBladeLength = tpTemplateBladeLength;
//		}
//		
//		public Map<EngineCADComponentsEnum, String> getDefEngineCADTemplates() {
//			return this.defEngineCADTemplates;
//		}
//		
//		public void setDefEngineCADTemplates(Map<EngineCADComponentsEnum, String> defEngineCADTemplates) {
//			this.defEngineCADTemplates = defEngineCADTemplates;
//		}
//		
//		public Map<EngineCADComponentsEnum, String> getEngineCADTemplates() {
//			return this.engineCADTemplates;
//		}
//		
//		public void setEngineCADTemplates(Map<EngineCADComponentsEnum, String> engineCADTemplates) {
//			this.engineCADTemplates = engineCADTemplates;
//		} 
//		
//		public EngineTypeEnum getEngineType() {
//			return this.engineType;
//		}
//		
//		public void setEngineType(EngineTypeEnum engineType) {
//			this.engineType = engineType;
//		}
//		
//		public double getEngineXApex() {
//			return this.engineXApex;
//		}
//		
//		public void setEngineXApex(double engineXApex) {
//			this.engineXApex = engineXApex;
//		}
//		
//		public double getEngineYApex() {
//			return this.engineYApex;
//		}
//		
//		public void setEngineYApex(double engineYApex) {
//			this.engineYApex = engineYApex;
//		}
//		
//		public double getEngineZApex() {
//			return this.engineZApex;
//		}
//		
//		public void setEngineZApex(double engineZApex) {
//			this.engineZApex = engineZApex;
//		}
//		
//		public double getTiltingAngle() {
//			return this.tiltingAngle;
//		}
//		
//		public void setTiltingAngle(double tiltingAngle) {
//			this.tiltingAngle = tiltingAngle;
//		}
//		
//		public double getNacelleLength() {
//			return this.nacelleLength;
//		}
//		
//		public void setNacelleLenght(double nacelleLength) {
//			this.nacelleLength = nacelleLength;
//		}
//		
//		public double getNacelleMaxDiameter() {
//			return this.nacelleMaxDiameter;
//		}
//		
//		public void setNacelleMaxDiameter(double nacellemaxDiameter) {
//			this.nacelleMaxDiameter = nacellemaxDiameter;
//		}
//		
//		public int getNumberOfBlades() {
//			return this.numberOfBlades;
//		}
//		
//		public void setNumberOfBlades(int numberOfBlades) {
//			this.numberOfBlades = numberOfBlades;
//		}
//		
//		public double getPropellerDiameter() {
//			return this.propellerDiameter;
//		}
//		
//		public void setPropellerDiameter(double propellerDiameter) {
//			this.propellerDiameter = propellerDiameter;
//		}
//		
//		public double getByPassRatio() {
//			return this.byPassRatio;
//		}
//		
//		public void setByPassRatio(double byPassRatio) {
//			this.byPassRatio = byPassRatio;
//		}
//		
//		public enum EngineCADComponentsEnum {
//			NACELLE,
//			BLADE;
//		}
//	}

}

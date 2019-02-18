package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import javafx.scene.shape.TriangleMesh;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRep_Builder;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;

public class Test40mds {
	
	private static final String tfTemplateFilename = "TF_complete_01.step";
	
	private static final double tfTemplateEngineLength = 7.381834824743633;
	private static final double tfTemplateEngineWidth = 3.9981461955761004;
	private static final double tfTemplateEngineHeight = 3.9985672207375087;
	
	private static final int tfTemplateInnerCasingIdx = 1;
	
	private static final double tfTemplateInnerCasingLength = 2.333664233072999;
	private static final double tfTemplateInnerCasingWidth = 2.4920056992322026;
	private static final double tfTemplateInnerCasingHeight = 2.4989933915980003;
	
	private static final double tfTemplateInnerCasingMaxRadius = 1.2494966957990001;
	private static final double tfTemplateInnerCasingXApex = 5.048170591670634;
	
	private static final double tfTemplateOuterCasingMaxRadius = 1.487090530948;
	
	private static final double tfTemplateInnerOuterCasingCoeff = -0.01777777778;  

	public static void main(String[] args) {
		System.out.println("-------------------------------------------------------------");
		System.out.println("------------------ Testing engine templates -----------------");
		System.out.println("--- Importing OCC generated aircraft components in JavaFX ---");	
		System.out.println("-------------------------------------------------------------");
		
		gp_Dir xDir = new gp_Dir(1.0, 0.0, 0.0);	
		gp_Dir yDir = new gp_Dir(0.0, 1.0, 0.0);
		gp_Dir zDir = new gp_Dir(0.0, 0.0, 1.0);
		
		List<OCCShape> exportShapes = new ArrayList<>();
		List<OCCSolid> aircraftSolids = new ArrayList<>();
		
		// ------------------------
		// Initialize the factory
		// ------------------------
		if (OCCUtils.theFactory == null) 
			OCCUtils.initCADShapeFactory();
				
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
		
		// ------------------
		// Import templates
		// ------------------
		MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
		String inputFolderPath = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + 
				 				 "CAD_engine_templates" + File.separator + 
				                 "turbofan_templates" + File.separator;
		
		// Reading the engine
		STEPControl_Reader engineReader = new STEPControl_Reader();
		engineReader.ReadFile(inputFolderPath + tfTemplateFilename);
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		System.out.println("Turbofan engine STEP reader problems:");
		engineReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		engineReader.TransferRoots();
		TopoDS_Shape engineShapes = engineReader.OneShape();
		
		System.out.println("Engine imported shapes type: " + engineShapes.ShapeType().toString());
		
		// -------------------------------------------
		// Generate the list of solids for the engine
		// -------------------------------------------
		TopExp_Explorer engineShapesExp = new TopExp_Explorer();
		engineShapesExp.Init(engineShapes, TopAbs_ShapeEnum.TopAbs_SOLID);
		
		List<OCCShape> engineSolids = new ArrayList<>();
		while (engineShapesExp.More() > 0) {
			engineSolids.add((OCCShape) OCCUtils.theFactory.newShape(TopoDS.ToSolid(engineShapesExp.Current())));
			engineShapesExp.Next();
		}
		
		// --------------------------
		// Iterate through engines
		// --------------------------
		gp_Pnt engineCG = OCCUtils.getShapeCG((OCCShape) OCCUtils.theFactory.newShape(engineShapes));
		
		gp_GTrsf engineLengthStretching = new gp_GTrsf();
		gp_GTrsf engineHeightStretching = new gp_GTrsf();
		gp_GTrsf engineWidthStretching = new gp_GTrsf();
		
		gp_Trsf engineTranslate = new gp_Trsf();
		
		gp_GTrsf innerCasingWidthStretching = new gp_GTrsf();
		gp_GTrsf innercasingHeightStretching = new gp_GTrsf();
		
		gp_Ax2 engineLengthStretchingRS = new gp_Ax2(engineCG, xDir);
		gp_Ax2 engineWidthStretchingRS = new gp_Ax2(engineCG, yDir);
		gp_Ax2 engineHeightStretchingRS = new gp_Ax2(engineCG, zDir);
		
		List<List<OCCShape>> moddedEngines = new ArrayList<List<OCCShape>>();
		for (int i = 0; i < nacelles.size(); i++) {
			NacelleCreator nacelle = nacelles.get(i);
			Engine engine = engines.get(i);
			
			BRep_Builder engineCompoundBuilder = new BRep_Builder();
			TopoDS_Compound engineCompound = new TopoDS_Compound();
			engineCompoundBuilder.MakeCompound(engineCompound);
			
			engineCompoundBuilder.Add(engineCompound, ((OCCShape) OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0)).getShape());
			engineCompoundBuilder.Add(engineCompound, engineShapes);		
			
			double xPosition = engine.getXApexConstructionAxes().doubleValue(SI.METER);
			double yPosition = engine.getYApexConstructionAxes().doubleValue(SI.METER);
			double zPosition = engine.getZApexConstructionAxes().doubleValue(SI.METER);
			
			System.out.println("Engine #" + (i+1) + " position: " + Arrays.toString(new double[] {xPosition, yPosition, zPosition}));
			
			double nacelleLength = nacelle.getLength().doubleValue(SI.METER);
			double nacelleMaxDiameter = nacelle.getDiameterMax().doubleValue(SI.METER);
			
			double engineBPR = engine.getBPR();
			double innerCasingRadiusRatio = 1 - tfTemplateInnerOuterCasingCoeff*engineBPR;
			
			double lengthStretchingFactor = nacelleLength/tfTemplateEngineLength;
			double heightStretchingFactor = nacelleMaxDiameter/tfTemplateEngineHeight;
			double innerCasingHeightStretchingFactor = innerCasingRadiusRatio*heightStretchingFactor;
			
			engineLengthStretching.SetAffinity(engineLengthStretchingRS, lengthStretchingFactor);
			engineHeightStretching.SetAffinity(engineHeightStretchingRS, heightStretchingFactor);
			engineWidthStretching.SetAffinity(engineWidthStretchingRS, heightStretchingFactor);
			
			OCCShape xStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(engineCompound, engineLengthStretching, 0).Shape()
							));			
			OCCShape xzStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(xStretchedEngine.getShape(), engineHeightStretching, 0).Shape()
							));
			OCCShape xyzStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(xzStretchedEngine.getShape(), engineWidthStretching, 0).Shape()
							));	
			
			OCCExplorer stretchedEngineExp = new OCCExplorer();
			stretchedEngineExp.init(xyzStretchedEngine, CADShapeTypes.SOLID);
			
			List<OCCShape> stretchedSolids = new ArrayList<>();
			while (stretchedEngineExp.more()) {
				stretchedSolids.add((OCCShape) stretchedEngineExp.current());
				stretchedEngineExp.next();
			}
			
			stretchedEngineExp.init(xyzStretchedEngine, CADShapeTypes.VERTEX);
			OCCVertex newEngineApex = (OCCVertex) stretchedEngineExp.current();
			
			System.out.println("Template engine #" + (i+1) + " position: " + Arrays.toString(newEngineApex.pnt()));
			
			BRep_Builder innerCasingCompoundBuilder = new BRep_Builder();
			TopoDS_Compound innerCasingCompound = new TopoDS_Compound();
			innerCasingCompoundBuilder.MakeCompound(innerCasingCompound);
			
			engineCompoundBuilder.Add(innerCasingCompound, stretchedSolids.get(1).getShape());
			engineCompoundBuilder.Add(innerCasingCompound, stretchedSolids.get(2).getShape());
			
			gp_Pnt innerCasingCG = OCCUtils.getShapeCG((OCCShape) OCCUtils.theFactory.newShape(innerCasingCompound));
			
			gp_Ax2 innerCasingWidthStretchingRS = new gp_Ax2(innerCasingCG, yDir);
			gp_Ax2 innercasingHeightStretchingRS = new gp_Ax2(innerCasingCG, zDir);
			
			innerCasingWidthStretching.SetAffinity(innerCasingWidthStretchingRS, innerCasingHeightStretchingFactor);
			innercasingHeightStretching.SetAffinity(innercasingHeightStretchingRS, innerCasingHeightStretchingFactor);
			
			OCCShape yStretchedInnerCasing = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(innerCasingCompound, innerCasingWidthStretching, 0).Shape()
							));			
			OCCShape yzStretchedInnerCasing = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(yStretchedInnerCasing.getShape(), innercasingHeightStretching, 0).Shape()
							));
			
			OCCExplorer stretchedInnerCasingExp = new OCCExplorer();
			stretchedInnerCasingExp.init(yzStretchedInnerCasing, CADShapeTypes.SOLID);
			
			List<OCCShape> stretchedInnerCasingSolids = new ArrayList<>();
			while (stretchedInnerCasingExp.more()) {
				stretchedInnerCasingSolids.add((OCCShape) stretchedInnerCasingExp.current());
				stretchedInnerCasingExp.next();
			}
			
			List<OCCShape> stretchedEngineSolids = new ArrayList<>();
			stretchedEngineSolids.add(stretchedSolids.get(0));
			stretchedEngineSolids.addAll(stretchedInnerCasingSolids);
			stretchedEngineSolids.add(stretchedSolids.get(3));
			
			engineTranslate.SetTranslation(
					new gp_Pnt(newEngineApex.pnt()[0], newEngineApex.pnt()[1], newEngineApex.pnt()[2]), 
					new gp_Pnt(xPosition, yPosition, zPosition)
					);
			
			BRep_Builder finalEngineCompoundBuilder = new BRep_Builder();
			TopoDS_Compound finalEngineCompound = new TopoDS_Compound();
			finalEngineCompoundBuilder.MakeCompound(finalEngineCompound);
			
			stretchedEngineSolids.forEach(s -> finalEngineCompoundBuilder.Add(
					finalEngineCompound, 
					s.getShape()
					));
			
			OCCShape translatedEngineCompound = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_Transform(finalEngineCompound, engineTranslate, 0).Shape()
							));
			
			OCCExplorer finalEngineExp = new OCCExplorer();
			finalEngineExp.init(translatedEngineCompound, CADShapeTypes.SOLID);
			
			List<OCCShape> finalEngineShapes = new ArrayList<>();
			while (finalEngineExp.more()) {
				finalEngineShapes.add((OCCShape) finalEngineExp.current());
				finalEngineExp.next();
			}
				
			moddedEngines.add(finalEngineShapes);
			
		}
		
		// -----------------------------
		// Generate remaining CAD parts
		// -----------------------------
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
				fuselage, 8, 7, false, false, true);
		
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceWingletCAD(
				wing, 0.75, 0.25, 0.25, false, false, true);
		
		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				hTail, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				vTail, WingTipType.ROUNDED, false, false, true);
		
		exportShapes.addAll(fuselageShapes);
		exportShapes.addAll(wingShapes);
		exportShapes.addAll(hTailShapes);
		exportShapes.addAll(vTailShapes);	
		moddedEngines.forEach(el -> exportShapes.addAll(el));
		OCCUtils.write("Test40mds", FileExtension.STEP, exportShapes);
		
//		// ----------------------------------------
//		// Generate the list and the map of solids
//		// ----------------------------------------
//		aircraftSolids.add((OCCSolid) fuselageShapes.get(0));
//		aircraftSolids.add((OCCSolid) wingShapes.get(0));
//		aircraftSolids.add((OCCSolid) hTailShapes.get(0));
//		aircraftSolids.add((OCCSolid) vTailShapes.get(0));
//		moddedEngines.forEach(sl -> sl.forEach(s -> aircraftSolids.add((OCCSolid) s)));
//		
//		// --------------------
//		// Extract the meshes
//		// --------------------
//		List<List<TriangleMesh>> triangleMeshes = aircraftSolids.stream()
//				.map(s -> (new OCCFXMeshExtractor(s.getShape())).getFaces().stream()
//						.map(f -> {
//							OCCFXMeshExtractor.FaceData faceData = new OCCFXMeshExtractor.FaceData(f, true);
//							faceData.load();
//							return faceData.getTriangleMesh();
//						})
//						.collect(Collectors.toList())
//						)
//				.collect(Collectors.toList());
//	
//		mesh = triangleMeshes;
	}

}

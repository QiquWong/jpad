package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.PowerPlant;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcadsandbox.Test23mds.Cam;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import opencascade.BRepBndLib;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.Bnd_Box;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyArrayUtils;

public class Test39mds {
	
//	private static final String tpNacelleTemplateName = "TP_nacelle_01.step";
//	private static final String tpBladeTemplateName = "TP_blade_02.step";
//	
//	private static final double tpNacelleTemplateLenght = 12.974006064637578;
//	private static final double tpNacelleTemplateWidth = 3.7866430441810013;
//	private static final double tpNacelleTemplateHeight = 4.311167784689999;
//	
//	private static final double tpHubDiameter = 1.3338673091839994;
//	private static final double tpHubCenterZCoord = 1.1258474162988725;
//	private static final double tpHubLength = tpNacelleTemplateLenght/25;
//	
//	private static final double tpBladeTemplateLenght = 0.2793191625149239;
//	private static final double tpBladeTemplateWidth = 0.16429455302182747;
//	private static final double tpBladeTemplateHeight = 1.5860730719635527;
//	
//	public static List<List<TriangleMesh>> mesh;
//	
//	// mouse positions
//	double mousePosX;
//	double mousePosY;
//	double mouseOldX;
//	double mouseOldY;
//	double mouseDeltaX;
//	double mouseDeltaY;
//
//	// allocating new camera
//	final Cam camOffset = new Cam();
//	final Cam cam = new Cam();
//	
//	@Override
//	public void start(final Stage primaryStage) {
//		camOffset.getChildren().add(cam);
//		resetCam();
//		
//		// creating the parent node and the main scene		
//		final Scene scene = new Scene(camOffset, 800, 800, true);
//		scene.setFill(new RadialGradient(225, 0.85, 300, 300, 500, false,
//                CycleMethod.NO_CYCLE, new Stop[]
//                { new Stop(0f, Color.WHITE),
//                  new Stop(1f, Color.LIGHTBLUE) }));
//		scene.setCamera(new PerspectiveCamera());
//		
//		Group components = new Group();
//		components.setDepthTest(DepthTest.ENABLE);
//		
//		// creating the mesh view		
//		mesh.forEach(mL -> mL.forEach(m -> {
//			MeshView face = new MeshView(m);
//			face.setDrawMode(DrawMode.FILL);
//			components.getChildren().add(face);
//		}));	
//		cam.getChildren().add(components);
//		
//		double halfSceneWidth = scene.getWidth()/2;
//		double halfSceneHeigth = scene.getHeight()/2;
//		cam.p.setX(halfSceneWidth);
//		cam.ip.setX(-halfSceneWidth);
//		cam.p.setY(halfSceneHeigth);
//		cam.ip.setY(-halfSceneHeigth);
//		
//		frameCam(primaryStage, scene);
//		
//		// scale, rotate and translate using the mouse
//		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
//            public void handle(MouseEvent me) {
//            	
//                mouseOldX = mousePosX;
//                mouseOldY = mousePosY;
//                mousePosX = me.getX();
//                mousePosY = me.getY();
//                mouseDeltaX = mousePosX - mouseOldX;
//                mouseDeltaY = mousePosY - mouseOldY;
//                
//                if (me.isAltDown() && me.isShiftDown() && me.isPrimaryButtonDown()) {
//                    double rzAngle = cam.rz.getAngle();
//                    cam.rz.setAngle(rzAngle - mouseDeltaX);
//                }
//                else if (me.isAltDown() && me.isPrimaryButtonDown()) {
//                    double ryAngle = cam.ry.getAngle();
//                    cam.ry.setAngle(ryAngle - mouseDeltaX);
//                    double rxAngle = cam.rx.getAngle();
//                    cam.rx.setAngle(rxAngle + mouseDeltaY);
//                }
//                else if (me.isAltDown() && me.isSecondaryButtonDown()) {
//                    double scale = cam.s.getX();
//                    double newScale = scale + mouseDeltaX*0.1;
//                    cam.s.setX(newScale);
//                    cam.s.setY(newScale);
//                    cam.s.setZ(newScale);
//                }
//                else if (me.isAltDown() && me.isMiddleButtonDown()) {
//                    double tx = cam.t.getX();
//                    double ty = cam.t.getY();
//                    cam.t.setX(tx + mouseDeltaX);
//                    cam.t.setY(ty + mouseDeltaY);
//                }                
//            }
//        });
//
//		// showing the stage
//		primaryStage.setScene(scene);
//		primaryStage.setTitle("Aircraft test");
//		primaryStage.show();
//	}
//
//	public static void main(String[] args) {
//		System.out.println("-------------------------------------------------------------");
//		System.out.println("------------------ Testing engine templates -----------------");
//		System.out.println("--- Importing OCC generated aircraft components in JavaFX ---");	
//		System.out.println("-------------------------------------------------------------");
//		
//		gp_Dir xDir = new gp_Dir(1.0, 0.0, 0.0);	
//		gp_Dir yDir = new gp_Dir(0.0, 1.0, 0.0);
//		gp_Dir zDir = new gp_Dir(0.0, 0.0, 1.0);
//		
//		List<OCCShape> exportShapes = new ArrayList<>();
//		List<OCCSolid> aircraftSolids = new ArrayList<>();
//		
//		// ------------------------
//		// Initialize the factory
//		// ------------------------
//		if (OCCUtils.theFactory == null) 
//			OCCUtils.initCADShapeFactory();
//				
//		// ----------------------
//		// Import the aircraft
//		// ----------------------
//		Aircraft aircraft = AircraftUtils.importAircraft(args);
//		
//		Fuselage fuselage = aircraft.getFuselage();
//		LiftingSurface wing = aircraft.getWing();
//		LiftingSurface hTail = aircraft.getHTail();
//		LiftingSurface vTail = aircraft.getVTail();
//		
//		List<NacelleCreator> nacelles = aircraft.getNacelles().getNacellesList();
//		List<Engine> engines = aircraft.getPowerPlant().getEngineList();
//		
//		// ------------------
//		// Import templates
//		// ------------------
//		MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
//		String inputFolderPath = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + 
//				 				 "CAD_engine_templates" + File.separator + 
//				                 "turboprop_templates" + File.separator;
//		
//		// Reading the nacelle
//		STEPControl_Reader nacelleReader = new STEPControl_Reader();
//		nacelleReader.ReadFile(inputFolderPath + tpNacelleTemplateName);
//		
//		Interface_Static.SetCVal("xstep.cascade.unit", "M");
//		
//		System.out.println("Turboprop nacelle STEP reader problems:");
//		nacelleReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
//		
//		nacelleReader.TransferRoots();
//		TopoDS_Shape nacelleShapes = nacelleReader.OneShape();
//		
//		System.out.println("Nacelle imported shapes type: " + nacelleShapes.ShapeType().toString());
//		
//		// Reading the blade
//		STEPControl_Reader bladeReader = new STEPControl_Reader();
//		bladeReader.ReadFile(inputFolderPath + tpBladeTemplateName);
//		
//		Interface_Static.SetCVal("xstep.cascade.unit", "M");
//		
//		System.out.println("Turboprop blade STEP reader problems:");
//		bladeReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
//		
//		bladeReader.TransferRoots();
//		TopoDS_Shape bladeShapes = bladeReader.OneShape();
//		
//		System.out.println("Nacelle imported shapes type: " + bladeShapes.ShapeType().toString());
//		
//		// --------------------------
//		// Iterate through engines
//		// --------------------------
//		nacelleShapes.Reverse();
//		bladeShapes.Reverse();
//		
//		gp_Pnt nacelleCG = OCCUtils.getShapeCG((OCCShape) OCCUtils.theFactory.newShape(nacelleShapes));
//		gp_Pnt bladeCG = OCCUtils.getShapeCG((OCCShape) OCCUtils.theFactory.newShape(bladeShapes));
//		
//		gp_GTrsf nacelleLengthStretching = new gp_GTrsf();
//		gp_GTrsf nacelleHeightStretching = new gp_GTrsf();
//		gp_GTrsf nacelleWidthStretching = new gp_GTrsf();
//		gp_Trsf nacelleTranslate = new gp_Trsf();	
//		
//		gp_Trsf bladeScaling = new gp_Trsf();
//		gp_GTrsf bladeXStretch = new gp_GTrsf();
//		gp_GTrsf bladeYStretch = new gp_GTrsf();
//		gp_Trsf bladeTranslate0 = new gp_Trsf();
//		gp_Trsf bladeTranslate1 = new gp_Trsf();
//		
//		gp_Ax2 nacelleLengthStretchingRS = new gp_Ax2(nacelleCG, xDir);
//		gp_Ax2 nacelleWidthStretchingRS = new gp_Ax2(nacelleCG, yDir);
//		gp_Ax2 nacelleHeightStretchingRS = new gp_Ax2(nacelleCG, zDir);
//		
//		gp_Ax2 bladeLengthStretchingRS = new gp_Ax2(bladeCG, xDir);
//		gp_Ax2 bladeWidthStretchingRS = new gp_Ax2(bladeCG, yDir);
//		
//		List<OCCShape> moddedNacelles = new ArrayList<>();	
//		List<List<OCCShape>> moddedBlades = new ArrayList<List<OCCShape>>();
//		for (int i = 0; i < nacelles.size(); i++) {
//			NacelleCreator nacelle = nacelles.get(i);
//			Engine engine = engines.get(i);
//			
//			// NACELLE		
//			BRep_Builder nacelleCompoundBuilder = new BRep_Builder();
//			TopoDS_Compound nacelleCompound = new TopoDS_Compound();
//			nacelleCompoundBuilder.MakeCompound(nacelleCompound);
//			
//			nacelleCompoundBuilder.Add(nacelleCompound, ((OCCShape) OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0)).getShape());
//			nacelleCompoundBuilder.Add(nacelleCompound, nacelleShapes);
//			
//			double xPosition = nacelle.getXApexConstructionAxes().doubleValue(SI.METER);
//			double yPosition = nacelle.getYApexConstructionAxes().doubleValue(SI.METER);
//			double zPosition = nacelle.getZApexConstructionAxes().doubleValue(SI.METER);
//			
//			double nacelleLength = nacelle.getLength().doubleValue(SI.METER);
//			double nacelleMaxDiameter = nacelle.getDiameterMax().doubleValue(SI.METER);
//			
//			double lengthStretchingFactor = nacelleLength/tpNacelleTemplateLenght;
//			double heightStretchingFactor = nacelleMaxDiameter/tpNacelleTemplateHeight;
//			
//			nacelleLengthStretching.SetAffinity(nacelleLengthStretchingRS, lengthStretchingFactor);
//			nacelleHeightStretching.SetAffinity(nacelleHeightStretchingRS, heightStretchingFactor);
//			nacelleWidthStretching.SetAffinity(nacelleWidthStretchingRS, heightStretchingFactor);
//			
//			OCCShape xStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToCompound(
//							new BRepBuilderAPI_GTransform(nacelleCompound, nacelleLengthStretching, 0).Shape()
//							));			
//			OCCShape xzStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToCompound(
//							new BRepBuilderAPI_GTransform(xStretchedNacelle.getShape(), nacelleHeightStretching, 0).Shape()
//							));
//			OCCShape xyzStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToCompound(
//							new BRepBuilderAPI_GTransform(xzStretchedNacelle.getShape(), nacelleWidthStretching, 0).Shape()
//							));		
//			
//			TopExp_Explorer shapesCompoundExp = new TopExp_Explorer();
//			shapesCompoundExp.Init(xyzStretchedNacelle.getShape(), TopAbs_ShapeEnum.TopAbs_VERTEX);
//			
//			double[] refPntD = ((OCCVertex) OCCUtils.theFactory.newShape(TopoDS.ToVertex(shapesCompoundExp.Current()))).pnt();		
//			gp_Pnt refPoint = new gp_Pnt(refPntD[0], refPntD[1], refPntD[2]);
//			
//			shapesCompoundExp.Clear();
//			shapesCompoundExp.Init(xyzStretchedNacelle.getShape(), TopAbs_ShapeEnum.TopAbs_SOLID);
//			
//			OCCShape modNacelle0 = (OCCShape) OCCUtils.theFactory.newShape(TopoDS.ToSolid(shapesCompoundExp.Current()));
//			
//			nacelleTranslate.SetTranslation(refPoint, 
//					new gp_Pnt(xPosition, yPosition, zPosition));
//			
//			OCCShape modNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToSolid(
//							new BRepBuilderAPI_Transform(modNacelle0.getShape(), nacelleTranslate, 0).Shape()
//							));		
//			
//			moddedNacelles.add(modNacelle);
//			
//			// BLADE		
//			BRep_Builder bladeCompoundBuilder = new BRep_Builder();
//			TopoDS_Compound bladeCompound = new TopoDS_Compound();
//			bladeCompoundBuilder.MakeCompound(bladeCompound);
//			
//			bladeCompoundBuilder.Add(bladeCompound, ((OCCShape) OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0)).getShape());
//			bladeCompoundBuilder.Add(bladeCompound, bladeShapes);
//			
//			int numberOfBlades = engine.getNumberOfBlades();
//			double propellerDiameter = engine.getPropellerDiameter().doubleValue(SI.METER);
//			double[] bladeRotVec = MyArrayUtils.linspace(0, 2*Math.PI, numberOfBlades + 1);
//			
//			double scaledHubRadius = (heightStretchingFactor*tpHubDiameter)/2;
//			double bladeScalingFactor = propellerDiameter/(2*(tpBladeTemplateHeight + tpHubDiameter/2));
//			double scaledHubLength = lengthStretchingFactor*tpHubLength;
//			double scaledHubZCoord = heightStretchingFactor*tpHubCenterZCoord;
//			
//			bladeScaling.SetScale(bladeCG, bladeScalingFactor);
//			
//			OCCShape scaledBladeCompound = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToCompound(
//							new BRepBuilderAPI_Transform(bladeCompound, bladeScaling, 0).Shape()
//							));	
//			
//			TopExp_Explorer bladeCompoundExp = new TopExp_Explorer();
//			bladeCompoundExp.Init(scaledBladeCompound.getShape(), TopAbs_ShapeEnum.TopAbs_SOLID);
//			OCCShape scaledBlade = (OCCShape) OCCUtils.theFactory.newShape(TopoDS.ToSolid(bladeCompoundExp.Current()));
//			
//			bladeCompoundExp.Clear();
//			bladeCompoundExp.Init(scaledBladeCompound.getShape(), TopAbs_ShapeEnum.TopAbs_VERTEX);
//			double[] bladeRefPntD = ((OCCVertex) OCCUtils.theFactory.newShape(TopoDS.ToVertex(bladeCompoundExp.Current()))).pnt();
//			gp_Pnt bladeRefPnt = new gp_Pnt(bladeRefPntD[0], bladeRefPntD[1], bladeRefPntD[2]);
//			
//			bladeTranslate0.SetTranslation(bladeRefPnt, new gp_Pnt(0.0, 0.0, scaledHubRadius));
//			
//			OCCShape translatedBlade0 = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToSolid(
//							new BRepBuilderAPI_Transform(scaledBlade.getShape(), bladeTranslate0, 0).Shape()
//							));	
//			
//			// blade stretching
//			double bladeLengthScaled = bladeScalingFactor*tpBladeTemplateLenght;
//			double totalBladeBaseLength = numberOfBlades*bladeLengthScaled;
//			double scaledHubCircle = 2*Math.PI*scaledHubRadius;
//			
//			System.out.println("Propeller hub radius: " + scaledHubRadius);
//			System.out.println("Propeller hub circle: " + scaledHubCircle);
//			
//			if (totalBladeBaseLength > scaledHubCircle) {
//				System.out.println("... Stretching the blade in order to fit it on the hub ...");
//				
//				double bladeLengthScalingFactor = 0.95*(scaledHubCircle/numberOfBlades)/tpBladeTemplateLenght;
//				
//				System.out.println("Blade stretching factor: " + bladeLengthScalingFactor);
//				
//				bladeXStretch.SetAffinity(bladeLengthStretchingRS, bladeLengthScalingFactor);
//				bladeYStretch.SetAffinity(bladeWidthStretchingRS, bladeLengthScalingFactor);
//				
//				OCCShape bladeXStretched = (OCCShape) OCCUtils.theFactory.newShape(
//						TopoDS.ToSolid(
//								new BRepBuilderAPI_GTransform(translatedBlade0.getShape(), bladeXStretch, 0).Shape()
//								));	
//				
//				OCCShape bladeYStretched = (OCCShape) OCCUtils.theFactory.newShape(
//						TopoDS.ToSolid(
//								new BRepBuilderAPI_GTransform(bladeXStretched.getShape(), bladeYStretch, 0).Shape()
//								));	
//				
//				translatedBlade0 = bladeYStretched;			
//			}
//			
//			gp_Trsf axialMirror = new gp_Trsf();
//			gp_Ax1 symmAxis = new gp_Ax1(new gp_Pnt(0.0, 0.0, 0.0), xDir);
//			
//			List<OCCShape> rotatedBlades = new ArrayList<>();
//			rotatedBlades.add(translatedBlade0);
//			for (int j = 1; j < bladeRotVec.length - 1; j++) {
//				axialMirror.SetRotation(symmAxis, bladeRotVec[j]);
//				rotatedBlades.add((OCCShape) OCCUtils.theFactory.newShape(
//						TopoDS.ToSolid(
//								new BRepBuilderAPI_Transform(translatedBlade0.getShape(), axialMirror, 0).Shape())
//						));
//			}
//			
//			BRep_Builder bladesCompoundBuilder = new BRep_Builder();
//			TopoDS_Compound bladesCompound = new TopoDS_Compound();
//			bladesCompoundBuilder.MakeCompound(bladesCompound);
//			
//			rotatedBlades.forEach(b -> bladesCompoundBuilder.Add(bladesCompound, b.getShape()));
//			
//			gp_Pnt propellerRefPnt = new gp_Pnt(
//					xPosition - scaledHubLength/2,
//					yPosition,
//					zPosition + scaledHubZCoord
//					);
//			bladeTranslate1.SetTranslation(new gp_Pnt(0.0, 0.0, 0.0), propellerRefPnt);
//			
//			OCCShape translatedBladeCompound = (OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToCompound(
//							new BRepBuilderAPI_Transform(bladesCompound, bladeTranslate1, 0).Shape()
//							));	
//			
//			TopExp_Explorer bladesCompoundExp = new TopExp_Explorer();
//			bladesCompoundExp.Init(translatedBladeCompound.getShape(), TopAbs_ShapeEnum.TopAbs_SOLID);
//			List<OCCShape> translatedBlades = new ArrayList<>();
//			while (bladesCompoundExp.More() > 0) {
//				translatedBlades.add((OCCShape) OCCUtils.theFactory.newShape(TopoDS.ToSolid(bladesCompoundExp.Current())));
//				bladesCompoundExp.Next();
//			}
//			
//			moddedBlades.add(translatedBlades);
//		}	
//		
//		// -----------------------------
//		// Generate remaining CAD parts
//		// -----------------------------
//		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
//				fuselage, 7, 7, false, false, true);
//		
//		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				wing, WingTipType.ROUNDED, false, false, true);
//		
//		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				hTail, WingTipType.ROUNDED, false, false, true);
//		
//		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				vTail, WingTipType.ROUNDED, false, false, true);
//		
//		exportShapes.addAll(fuselageShapes);
//		exportShapes.addAll(wingShapes);
//		exportShapes.addAll(hTailShapes);
//		exportShapes.addAll(vTailShapes);
//		
//		exportShapes.addAll(moddedNacelles);
//		moddedBlades.forEach(bl -> exportShapes.addAll(bl));
//		OCCUtils.write("Test39mds", FileExtension.STEP, exportShapes);
//		
//		// ----------------------------------------
//		// Generate the list and the map of solids
//		// ----------------------------------------
//		aircraftSolids.add((OCCSolid) fuselageShapes.get(0));
//		aircraftSolids.add((OCCSolid) wingShapes.get(0));
//		aircraftSolids.add((OCCSolid) hTailShapes.get(0));
//		aircraftSolids.add((OCCSolid) vTailShapes.get(0));
//		moddedNacelles.forEach(n -> aircraftSolids.add((OCCSolid) n));
//		moddedBlades.forEach(bl -> bl.forEach(b -> aircraftSolids.add((OCCSolid) b)));
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
//		
//		launch();
//	}
//	
//	public void frameCam(final Stage stage, final Scene scene) {
//        setCamOffset(camOffset, scene);
//        setCamPivot(cam);
//        setCamTranslate(cam);
//        setCamScale(cam, scene);
//    }
//	
//	public void setCamOffset(final Cam camOffset, final Scene scene) {
//        double width = scene.getWidth();
//        double height = scene.getHeight();
//        camOffset.t.setX(width/2.0);
//        camOffset.t.setY(height/2.0);
//    }
//	
//	public void setCamScale(final Cam cam, final Scene scene) {
//        final Bounds bounds = cam.getBoundsInLocal();
//
//        double width = scene.getWidth();
//        double height = scene.getHeight();
//
//        double scaleFactor = 1.0;
//        double scaleFactorY = 1.0;
//        double scaleFactorX = 1.0;
//        if (bounds.getWidth() > 0.0001) {
//            scaleFactorX = width / bounds.getWidth() / 2.0; // / 2.0;
//        }
//        if (bounds.getHeight() > 0.0001) {
//            scaleFactorY = height / bounds.getHeight() / 2.0; //  / 1.5;
//        }
//        if (scaleFactorX > scaleFactorY) {
//            scaleFactor = scaleFactorY;
//        } else {
//            scaleFactor = scaleFactorX;
//        }
//        cam.s.setX(scaleFactor);
//        cam.s.setY(scaleFactor);
//        cam.s.setZ(scaleFactor);
//    }
//	
//	public void setCamPivot(final Cam cam) {
//        final Bounds bounds = cam.getBoundsInLocal();
//        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
//        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
//        final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;
//        cam.p.setX(pivotX);
//        cam.p.setY(pivotY);
//        cam.p.setZ(pivotZ);
//        cam.ip.setX(-pivotX);
//        cam.ip.setY(-pivotY);
//        cam.ip.setZ(-pivotZ);
//    }
//	
//	public void setCamTranslate(final Cam cam) {
//        final Bounds bounds = cam.getBoundsInLocal();
//        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
//        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
//        cam.t.setX(-pivotX);
//        cam.t.setY(-pivotY);
//    }
//	
//	public void resetCam() {
//		cam.t.setX(0.0);
//		cam.t.setY(0.0);
//		cam.t.setZ(0.0);
//		
//		cam.rx.setAngle(135.0);
//		cam.ry.setAngle(-15.0);
//		cam.rz.setAngle(10.0);
//		
//		cam.s.setX(1.25);
//		cam.s.setY(1.25);
//		cam.s.setZ(1.25);
//
//		cam.p.setX(0.0);
//		cam.p.setY(0.0);
//		cam.p.setZ(0.0);
//
//		cam.ip.setX(0.0);
//		cam.ip.setY(0.0);
//		cam.ip.setZ(0.0);
//
//		final Bounds bounds = cam.getBoundsInLocal();
//		final double pivotX = bounds.getMinX() + bounds.getWidth() / 2;
//		final double pivotY = bounds.getMinY() + bounds.getHeight() / 2;
//		final double pivotZ = bounds.getMinZ() + bounds.getDepth() / 2;
//
//		cam.p.setX(pivotX);
//		cam.p.setY(pivotY);
//		cam.p.setZ(pivotZ);
//
//		cam.ip.setX(-pivotX);
//		cam.ip.setY(-pivotY);
//		cam.ip.setZ(-pivotZ);
//	}
//	
//	public PhongMaterial setComponentColor(ComponentEnum component) {
//
//		PhongMaterial material = new PhongMaterial();
//
//		switch(component) {
//
//		case FUSELAGE: 
//			material.setDiffuseColor(Color.BLUE);
//			material.setSpecularColor(Color.LIGHTBLUE);
//
//			break;
//
//		case WING:
//			material.setDiffuseColor(Color.RED);
//			material.setSpecularColor(Color.MAGENTA);
//
//			break;
//
//		case HORIZONTAL_TAIL:
//			material.setDiffuseColor(Color.DARKGREEN);
//			material.setSpecularColor(Color.GREEN);
//
//			break;
//
//		case VERTICAL_TAIL:
//			material.setDiffuseColor(Color.GOLD);
//			material.setSpecularColor(Color.YELLOW);
//
//			break;
//
//		case CANARD:
//			material.setDiffuseColor(Color.BLUEVIOLET);
//			material.setSpecularColor(Color.VIOLET);
//
//			break;
//			
//		case NACELLE:
//			material.setDiffuseColor(Color.BROWN);
//			material.setSpecularColor(Color.CRIMSON);
//			
//			break;
//			
//		case ENGINE:
//			material.setDiffuseColor(Color.THISTLE);
//			material.setSpecularColor(Color.BEIGE);
//			
//			break;
//
//		default:
//
//			break;
//		}
//		
//		return material;
//	}
//	
//	public class Cam extends Group {
//		Translate t  = new Translate();
//		Translate p  = new Translate();
//		Translate ip = new Translate();
//		Rotate rx = new Rotate();
//		{rx.setAxis(Rotate.X_AXIS);}
//		Rotate ry = new Rotate();
//		{ry.setAxis(Rotate.Y_AXIS);}
//		Rotate rz = new Rotate();
//		{rz.setAxis(Rotate.Z_AXIS);}
//		Scale s = new Scale();
//		public Cam() {super(); getTransforms().addAll(t, p, ip, rx, ry, rz, s); }
//	}	

}

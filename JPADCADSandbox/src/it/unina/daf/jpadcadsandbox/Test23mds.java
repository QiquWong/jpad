package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadfx.OCCFXMeshExtractor;
import it.unina.daf.jpadcadsandbox.Test22mds.Cam;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;

public class Test23mds extends Application {
	
	public static List<TriangleMesh> triangles;
	
	// mouse positions
	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;

	// allocating new camera
	final Cam camOffset = new Cam();
	final Cam cam = new Cam();
	
	public static void main(String[] args) {
		
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test: importing OCC generated aircraft components in JavaFX");	
		System.out.println("-------------------");
		
		System.out.println("========== [main] Getting the aircraft ...");
		Aircraft theAircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = theAircraft.getFuselage();
		LiftingSurface wing = theAircraft.getWing();
		LiftingSurface horTail = theAircraft.getHTail();
		LiftingSurface verTail = theAircraft.getVTail();
		
		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, 12, 5, true, true, false);	
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		List<OCCShape> horTailShapes = AircraftUtils.getLiftingSurfaceCAD(horTail, ComponentEnum.HORIZONTAL_TAIL, 1e-3, false, true, false);
		List<OCCShape> verTailShapes = AircraftUtils.getLiftingSurfaceCAD(verTail, ComponentEnum.VERTICAL_TAIL, 1e-3, false, true, false);
		
		List<OCCShape> allShapes = new ArrayList<>();
		allShapes.addAll(fuselageShapes);
		allShapes.addAll(wingShapes);
		allShapes.addAll(horTailShapes);
		allShapes.addAll(verTailShapes);
		
		// filter the solids
		List<TopoDS_Shape> solids = allShapes.stream()
				  .filter(s -> s.getShape().ShapeType().equals(TopAbs_ShapeEnum.TopAbs_SOLID))
				  .map(s -> s.getShape())
			      .collect(Collectors.toList());
		
		// pass each solid to the mesh extractor
		List<OCCFXMeshExtractor> meshExtractors = solids.stream()
				.map(s -> new OCCFXMeshExtractor(s))
				.collect(Collectors.toList());
		
		// process each face of the solids
		List<TriangleMesh> triangleMeshes = meshExtractors.stream()
				.map(m -> m.getFaces())
				.flatMap(Collection::stream)
				.map(f -> {
					OCCFXMeshExtractor.FaceData faceData = new OCCFXMeshExtractor.FaceData(f, true);
					faceData.load();
					return faceData.getTriangleMesh();
				})
				.collect(Collectors.toList());
		
		System.out.println("========== Number of triangulations executed:" + triangleMeshes.size());
		
		triangles = triangleMeshes;

		launch();
	}
	
	@Override 
	public void start(final Stage stage) {	
		camOffset.getChildren().add(cam);
		resetCam();
		
		// creating the parent node and the main scene		
		final Scene scene = new Scene(camOffset, 800, 800, true);
		scene.setFill(new RadialGradient(225, 0.85, 300, 300, 500, false,
                CycleMethod.NO_CYCLE, new Stop[]
                { new Stop(0f, Color.BLUE),
                  new Stop(1f, Color.LIGHTBLUE) }));
		scene.setCamera(new PerspectiveCamera());
		
		Group objects = new Group();
		objects.setDepthTest(DepthTest.ENABLE);

		// creating a new material
		PhongMaterial blueStuff = new PhongMaterial();
		blueStuff.setDiffuseColor(Color.LIGHTBLUE);
		blueStuff.setSpecularColor(Color.BLUE);
		
		// creating the aircraft
		triangles.forEach(tm -> {
				MeshView face = new MeshView(tm);
				face.setDrawMode(DrawMode.FILL);
				face.setMaterial(blueStuff);
				face.setCullFace(CullFace.BACK);
				objects.getChildren().add(face);
				});
		cam.getChildren().add(objects);
		
		double halfSceneWidth = scene.getWidth()/2;
		double halfSceneHeigth = scene.getHeight()/2;
		cam.p.setX(halfSceneWidth);
		cam.ip.setX(-halfSceneWidth);
		cam.p.setY(halfSceneHeigth);
		cam.ip.setY(-halfSceneHeigth);
		
		frameCam(stage, scene);

		// adding a light source
//		PointLight light = new PointLight(Color.WHITE);
//		light.setTranslateX(-10);
//		light.setTranslateY(-1);
//		light.setTranslateZ(-10);
//		cam.getChildren().add(light);
		
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                mousePosX = me.getX();
                mousePosY = me.getY();
                mouseOldX = me.getX();
                mouseOldY = me.getY();
                //System.out.println("scene.setOnMousePressed " + me);
            }
        });
		
		// scale and/or rotate using the mouse
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getX();
                mousePosY = me.getY();
                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;
                if (me.isAltDown() && me.isShiftDown() && me.isPrimaryButtonDown()) {
                    double rzAngle = cam.rz.getAngle();
                    cam.rz.setAngle(rzAngle - mouseDeltaX);
                }
                else if (me.isAltDown() && me.isPrimaryButtonDown()) {
                    double ryAngle = cam.ry.getAngle();
                    cam.ry.setAngle(ryAngle - mouseDeltaX);
                    double rxAngle = cam.rx.getAngle();
                    cam.rx.setAngle(rxAngle + mouseDeltaY);
                }
                else if (me.isAltDown() && me.isSecondaryButtonDown()) {
                    double scale = cam.s.getX();
                    double newScale = scale + mouseDeltaX*1;
                    cam.s.setX(newScale);
                    cam.s.setY(newScale);
                    cam.s.setZ(newScale);
                }
                else if (me.isAltDown() && me.isMiddleButtonDown()) {
                    double tx = cam.t.getX();
                    double ty = cam.t.getY();
                    cam.t.setX(tx + mouseDeltaX);
                    cam.t.setY(ty + mouseDeltaY);
                }                
            }
        });

		// showing the stage
		stage.setScene(scene);
		stage.setTitle("Quarter Dome Surface");
		stage.show();
	}

	public void frameCam(final Stage stage, final Scene scene) {
        setCamOffset(camOffset, scene);
        // cam.resetTSP();
        setCamPivot(cam);
        setCamTranslate(cam);
        setCamScale(cam, scene);
    }
	
	public void setCamOffset(final Cam camOffset, final Scene scene) {
        double width = scene.getWidth();
        double height = scene.getHeight();
        camOffset.t.setX(width/2.0);
        camOffset.t.setY(height/2.0);
    }
	
	public void setCamScale(final Cam cam, final Scene scene) {
        final Bounds bounds = cam.getBoundsInLocal();

        double width = scene.getWidth();
        double height = scene.getHeight();

        double scaleFactor = 1.0;
        double scaleFactorY = 1.0;
        double scaleFactorX = 1.0;
        if (bounds.getWidth() > 0.0001) {
            scaleFactorX = width / bounds.getWidth(); // / 2.0;
        }
        if (bounds.getHeight() > 0.0001) {
            scaleFactorY = height / bounds.getHeight(); //  / 1.5;
        }
        if (scaleFactorX > scaleFactorY) {
            scaleFactor = scaleFactorY;
        } else {
            scaleFactor = scaleFactorX;
        }
        cam.s.setX(scaleFactor);
        cam.s.setY(scaleFactor);
        cam.s.setZ(scaleFactor);
    }
	
	public void setCamPivot(final Cam cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;
        cam.p.setX(pivotX);
        cam.p.setY(pivotY);
        cam.p.setZ(pivotZ);
        cam.ip.setX(-pivotX);
        cam.ip.setY(-pivotY);
        cam.ip.setZ(-pivotZ);
    }
	
	public void setCamTranslate(final Cam cam) {
        final Bounds bounds = cam.getBoundsInLocal();
        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
        cam.t.setX(-pivotX);
        cam.t.setY(-pivotY);
    }
	
	public void resetCam() {
		cam.t.setX(0.0);
		cam.t.setY(0.0);
		cam.t.setZ(0.0);
		
		cam.rx.setAngle(45.0);
		cam.ry.setAngle(-7.0);
		cam.rz.setAngle(0.0);
		
		cam.s.setX(1.25);
		cam.s.setY(1.25);
		cam.s.setZ(1.25);

		cam.p.setX(0.0);
		cam.p.setY(0.0);
		cam.p.setZ(0.0);

		cam.ip.setX(0.0);
		cam.ip.setY(0.0);
		cam.ip.setZ(0.0);

		final Bounds bounds = cam.getBoundsInLocal();
		final double pivotX = bounds.getMinX() + bounds.getWidth() / 2;
		final double pivotY = bounds.getMinY() + bounds.getHeight() / 2;
		final double pivotZ = bounds.getMinZ() + bounds.getDepth() / 2;

		cam.p.setX(pivotX);
		cam.p.setY(pivotY);
		cam.p.setZ(pivotZ);

		cam.ip.setX(-pivotX);
		cam.ip.setY(-pivotY);
		cam.ip.setZ(-pivotZ);
	}
	
	public class Cam extends Group {
		Translate t  = new Translate();
		Translate p  = new Translate();
		Translate ip = new Translate();
		Rotate rx = new Rotate();
		{rx.setAxis(Rotate.X_AXIS);}
		Rotate ry = new Rotate();
		{ry.setAxis(Rotate.Y_AXIS);}
		Rotate rz = new Rotate();
		{rz.setAxis(Rotate.Z_AXIS);}
		Scale s = new Scale();
		public Cam() {super(); getTransforms().addAll(t, p, ip, rx, ry, rz, s); }
	}
}

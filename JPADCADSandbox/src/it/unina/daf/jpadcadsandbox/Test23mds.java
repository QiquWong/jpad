package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import aircraft.Aircraft;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.occ.OCCShape;
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
import opencascade.TopoDS_Solid;

public class Test23mds extends Application {
	
	public static HashMap<ComponentEnum, List<TriangleMesh>> meshMap;
	
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
		
		List<ComponentEnum> comps = new ArrayList<>();
		comps = Arrays.asList(new ComponentEnum[] {
				ComponentEnum.FUSELAGE, 
				ComponentEnum.WING, 
				ComponentEnum.HORIZONTAL_TAIL, 
				ComponentEnum.VERTICAL_TAIL, 
				ComponentEnum.CANARD
		});
		
		System.out.println(comps.size());
		
		// getting the selected shapes
		List<OCCShape> allShapes = AircraftUtils.getAircraftShapes(theAircraft, comps);
		
		// filter the solids
		List<TopoDS_Solid> solids = AircraftUtils.getAircraftSolid(allShapes);
		
		// extract the meshes
		List<List<TriangleMesh>> triangleMeshes = solids.stream()
				.map(s -> (new OCCFXMeshExtractor(s)).getFaces().stream()
						.map(f -> {
							OCCFXMeshExtractor.FaceData faceData = new OCCFXMeshExtractor.FaceData(f, true);
							faceData.load();
							return faceData.getTriangleMesh();
						})
						.collect(Collectors.toList())
						)
				.collect(Collectors.toList());
		
		HashMap<ComponentEnum, List<TriangleMesh>> map = new HashMap<>(); 
		for(int i = 0; i < triangleMeshes.size(); i++) {
			map.put(comps.get(i), triangleMeshes.get(i));
		}
		meshMap = map;

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
                { new Stop(0f, Color.WHITE),
                  new Stop(1f, Color.LIGHTBLUE) }));
		scene.setCamera(new PerspectiveCamera());
		
		Group components = new Group();
		components.setDepthTest(DepthTest.ENABLE);
		
		// creating the mesh view		
		meshMap.forEach((e, mL) -> {
			mL.forEach(m -> {
				MeshView face = new MeshView(m);
				face.setDrawMode(DrawMode.FILL);
				face.setMaterial(setComponentColor(e));
				components.getChildren().add(face);
			});
		});
		cam.getChildren().add(components);
		
		double halfSceneWidth = scene.getWidth()/2;
		double halfSceneHeigth = scene.getHeight()/2;
		cam.p.setX(halfSceneWidth);
		cam.ip.setX(-halfSceneWidth);
		cam.p.setY(halfSceneHeigth);
		cam.ip.setY(-halfSceneHeigth);
		
		frameCam(stage, scene);
		
		// scale, rotate and translate using the mouse
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
                    double newScale = scale + mouseDeltaX*0.1;
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
		stage.setTitle("Aircraft test");
		stage.show();
	}

	public void frameCam(final Stage stage, final Scene scene) {
        setCamOffset(camOffset, scene);
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
            scaleFactorX = width / bounds.getWidth() / 2.0; // / 2.0;
        }
        if (bounds.getHeight() > 0.0001) {
            scaleFactorY = height / bounds.getHeight() / 2.0; //  / 1.5;
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
		
		cam.rx.setAngle(135.0);
		cam.ry.setAngle(-15.0);
		cam.rz.setAngle(10.0);
		
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
	
	public PhongMaterial setComponentColor(ComponentEnum component) {

		PhongMaterial material = new PhongMaterial();

		switch(component) {

		case FUSELAGE: 
			material.setDiffuseColor(Color.BLUE);
			material.setSpecularColor(Color.LIGHTBLUE);

			break;

		case WING:
			material.setDiffuseColor(Color.RED);
			material.setSpecularColor(Color.MAGENTA);

			break;

		case HORIZONTAL_TAIL:
			material.setDiffuseColor(Color.DARKGREEN);
			material.setSpecularColor(Color.GREEN);

			break;

		case VERTICAL_TAIL:
			material.setDiffuseColor(Color.GOLD);
			material.setSpecularColor(Color.YELLOW);

			break;

		case CANARD:
			material.setDiffuseColor(Color.BLUEVIOLET);
			material.setSpecularColor(Color.VIOLET);

			break;

		default:

			break;
		}
		
		return material;
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

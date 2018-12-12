package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCDataProvider;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCFXMeshExtractor;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Shear;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRep_Builder;
import opencascade.GeomAbs_Shape;
import opencascade.StlAPI_Writer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Vertex;
import opencascade.gp_Pnt;

public class Test22mds extends Application{
	
	public static TriangleMesh triangleMesh;
	
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
		System.out.println("JPADCADSandbox Test: importing an OCC generated quarter-dome in JavaFX");	
		System.out.println("-------------------");
		
		OCCUtils.initCADShapeFactory();
			
		System.out.println("========== [main] Constructing support curves");
		// generate curve in the XY plane
		List<double[]> xyCrvPnts = new ArrayList<>();
		xyCrvPnts.add(new double[] {1.0, 0.0, 0.0});
		xyCrvPnts.add(new double[] {0.7, 0.7, 0.0});
		xyCrvPnts.add(new double[] {0.0, 1.0, 0.0});
		CADGeomCurve3D xyCrv = OCCUtils.theFactory.newCurve3D(xyCrvPnts, false);
		
		// generate curve in the YZ plane
		List<double[]> yzCrvPnts = new ArrayList<>();
		yzCrvPnts.add(new double[] {0.0, 1.0, 0.0});
		yzCrvPnts.add(new double[] {0.0, 0.7, 0.7});
		yzCrvPnts.add(new double[] {0.0, 0.0, 1.0});
		CADGeomCurve3D yzCrv = OCCUtils.theFactory.newCurve3D(yzCrvPnts, false);
		
		// generate curve in the XZ plane
		List<double[]> xzCrvPnts = new ArrayList<>();
		xzCrvPnts.add(new double[] {0.0, 0.0, 1.0});
		xzCrvPnts.add(new double[] {0.7, 0.0, 0.7});
		xzCrvPnts.add(new double[] {1.0, 0.0, 0.0});
		CADGeomCurve3D xzCrv = OCCUtils.theFactory.newCurve3D(xzCrvPnts, false);
		
		// generate the dome as a filler surface, making it pass through the curve and points defined above
		System.out.println("========== [main] Generating the quarter dome");
		BRepOffsetAPI_MakeFilling quarterDome = new BRepOffsetAPI_MakeFilling();
		
		quarterDome.Add(
				((OCCEdge)((OCCGeomCurve3D)xyCrv).edge()).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		quarterDome.Add(
				((OCCEdge)((OCCGeomCurve3D)yzCrv).edge()).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		quarterDome.Add(
				((OCCEdge)((OCCGeomCurve3D)xzCrv).edge()).getShape(),
				GeomAbs_Shape.GeomAbs_C0
				);
		
		quarterDome.Build();
		TopoDS_Shape tdsQD = ((OCCShape)(OCCUtils.theFactory.newShape(quarterDome.Shape()))).getShape();
		System.out.println(OCCUtils.reportOnShape(tdsQD, "Quarter Dome"));
		
		OCCFXMeshExtractor meshExtractor = new OCCFXMeshExtractor(tdsQD);
		List<TopoDS_Face> faces = new ArrayList<>();
		faces.addAll(meshExtractor.getFaces());
		
		OCCFXMeshExtractor.FaceData faceData = new OCCFXMeshExtractor.FaceData(faces.get(0), true);
		faceData.load();
		// report on faceData
		System.out.println("Face Triangulation Report: ...\n" + 
				"Number of polys ------> " + faceData.getNbrOfPolys()         + "\n" + 
				"Polys length ---------> " + faceData.getPolys().length       + "\n" + 
				"Polys:\n" + Arrays.toString(faceData.getPolys())             + "\n" +
				"Triangles length -----> " +  faceData.getITriangles().length + "\n" + 
				"Triangles:\n" + Arrays.toString(faceData.getITriangles())    + "\n"
				);
		triangleMesh = faceData.getTriangleMesh();	
		
		launch(args);
	}
	
	@Override
	public void start(final Stage primaryStage) {	
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
		
		// creating the quarter dome mesh
		TriangleMesh quarterDomeMesh = triangleMesh;		
		MeshView quarterDome = new MeshView(quarterDomeMesh);
		quarterDome.setDrawMode(DrawMode.FILL);
		quarterDome.setMaterial(blueStuff);
		objects.getChildren().add(quarterDome);
		cam.getChildren().add(objects);
		
		double halfSceneWidth = scene.getWidth()/2;
		double halfSceneHeigth = scene.getHeight()/2;
		cam.p.setX(halfSceneWidth);
		cam.ip.setX(-halfSceneWidth);
		cam.p.setY(halfSceneHeigth);
		cam.ip.setY(-halfSceneHeigth);
		
		frameCam(primaryStage, scene);

		// adding a light source
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateX(-10);
		light.setTranslateY(-1);
		light.setTranslateZ(-10);
		cam.getChildren().add(light);
		
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
		primaryStage.setScene(scene);
		primaryStage.setTitle("Quarter Dome Surface");
		primaryStage.show();
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
//        final double pivotX = bounds.getMinX() + bounds.getWidth()/2;
//        final double pivotY = bounds.getMinY() + bounds.getHeight()/2;
//        final double pivotZ = bounds.getMinZ() + bounds.getDepth()/2;

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

package cad.occ;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.fxyz.shapes.primitives.BezierMesh;
import org.fxyz.shapes.primitives.helper.InterpolateBezier;
import org.jcae.opencascade.jni.TopoDS_Edge;
import org.jcae.opencascade.jni.TopoDS_Face;
import org.jcae.opencascade.jni.TopoDS_Shape;
import org.jcae.opencascade.jni.TopoDS_Vertex;

import javafx.geometry.BoundingBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * A Viewable specilized to display CAD scenes.
 * The selected elements are highlighted.
 * @author Agodemar
 * @todo all methods must be implemented.
 */

public class OCCFXViewableCAD extends OCCFXViewable {

	private final OCCFXMeshExtractor _meshExtractor;

	private String _prefixName = "";

	public static final String XFORM_FACES_ID = new String("XFORM:FACES");
	public static final String XFORM_SUB_FACES_ID = new String("XFORM-SUB:FACES");
	public static final String XFORM_SUB_BACKFACES_ID = new String("XFORM-SUB:BACKFACES");
	public static final String MESHVIEW_FRONT_ID = new String("MESHVIEW:FRONT:");
	public static final String MESHVIEW_BACK_ID = new String("MESHVIEW:BACK:");
	public static final String VIEWABLECAD_ID = new String("VIEWABLECAD:");
	
	/** Specify which type of object is selectable */
	private ShapeType _shapeType = ShapeType.FACE;
	
	private final Color _frontFaceColor = Color.GREENYELLOW;
	private final Color _backFaceColor = Color.LIGHTGRAY; // new Color(255 / 2, 255, 255 / 2);
	private final Color _vertexColor = Color.BLUE;
	private Color _edgeColor = Color.WHITE;
	private Color _freeEdgeColor = Color.GREEN;

	public static final PhongMaterial FACE_FRONT_MATERIAL = new PhongMaterial(Color.GREENYELLOW);
	public static final PhongMaterial FACE_BACK_MATERIAL = new PhongMaterial(Color.LIGHTGREY);
	public static final PhongMaterial FACE_FRONT_SELECTED_MATERIAL = new PhongMaterial(Color.CHOCOLATE);
	public static final PhongMaterial FACE_BACK_SELECTED_MATERIAL = new PhongMaterial(Color.DARKCYAN);
	
	private final HashMap<TopoDS_Vertex, OCCFXLeafNode> _topoToNodeVertex = new HashMap<TopoDS_Vertex, OCCFXLeafNode>();
	private final HashMap<TopoDS_Edge, OCCFXLeafNode> _topoToNodeEdge = new HashMap<TopoDS_Edge, OCCFXLeafNode>();
	private final HashMap<TopoDS_Face, OCCFXLeafNode> _topoToNodeFaceFront = new HashMap<TopoDS_Face, OCCFXLeafNode>();
	private final HashMap<TopoDS_Face, OCCFXLeafNode> _topoToNodeFaceBack = new HashMap<TopoDS_Face, OCCFXLeafNode>();
	private final HashMap<OCCFXLeafNode, TopoDS_Shape> _nodeToTopo = new HashMap<OCCFXLeafNode, TopoDS_Shape>();
	private OCCFXForm _facesXform = null;
	private OCCFXForm _edgesXform = null;
	private OCCFXForm _verticesXform = null;
	private int _vertexSize = 4;
	private int _edgeSize = 2;
	private boolean _onlyFreeEdges = false;
	private OCCFXForm _facesFrontXform, _facesBackXform;

	private List<TriangleMesh> _theTriangleMeshFrontList;
	private List<TriangleMesh> _theTriangleMeshBackList;
	private List<MeshView> _theMeshViewFrontList;
	private List<MeshView> _theMeshViewBackList;
	
	private BoundingBox _theBoundingBoxInLocal = null;
	
	private int _pointsBetweenTwoConsecutiveKnots = 3;
	private int _pointsPerCurveSection = 4;

	
	public static enum ShapeType
	{
		VERTEX,
		EDGE,
		FACE,
		BEZIER_MESH
	}
	
	private OCCFXViewableCAD(
			OCCFX3DView aircraft3DView, OCCFXForm viewableNode,
			OCCFXMeshExtractor meshExtractor, ShapeType shapeType, boolean onlyFreeEdges
			) {
		super(aircraft3DView, viewableNode);
		this._meshExtractor = meshExtractor;
		this._onlyFreeEdges = onlyFreeEdges; // TODO: use it?
		this._shapeType = shapeType;

		System.out.println("ViewableCAD !!!");
		
        refresh();
	}

	public void refresh() {
		
		System.out.println("ViewableCAD :: refresh()");

		// TODO
		theViewableNode.getChildren().clear();
		computeGeometry();
//		render();
	}

	// optional arguments, see:
	// http://stackoverflow.com/questions/997482/does-java-support-default-parameter-values
	public OCCFXViewableCAD(
			OCCFX3DView aircraft3DView, OCCFXForm viewableNode,
			TopoDS_Shape shape, ShapeType shapeType,
			Object ... optObj
			) {
		// call the private constructor
		this(aircraft3DView, viewableNode, new OCCFXMeshExtractor(shape), shapeType, false);
		
		//------------------------------------------------------------
		// process optional arguments
		if (optObj.length  > 0) {
			if (optObj[0] == null) {
				_prefixName = "";
			}			
			if (optObj[0] instanceof String) {
				_prefixName = (String)optObj[0];
			}
		} else {
			// do nothing
		}
		// in case you wanna grab successive parameters
		//if (optObj.length  > 1) {}
		//------------------------------------------------------------
		
		// assign ID to Viewable
		theViewableNode.setId(_prefixName + VIEWABLECAD_ID);
		
	}

	public OCCFXViewableCAD(
			OCCFX3DView aircraft3DView, OCCFXForm viewableNode,
			TopoDS_Shape shape, ShapeType shapeType, boolean onlyFreeEdges
			) {
		this(aircraft3DView, viewableNode, new OCCFXMeshExtractor(shape), shapeType, onlyFreeEdges);
	}

	// TODO: check if reading from file works
//	public ViewableCAD(
//			MyFXAircraft3DView aircraft3DView, Xform viewableNode, ShapeType shapeType,
//			String filename
//			) {
//		this(aircraft3DView, viewableNode, new OCCMeshExtractor(filename), shapeType, false);
//	}

	// manage BezierMesh for fuselage sections, outlines, wing sections, etc.
	public OCCFXViewableCAD(
			OCCFX3DView aircraft3DView, OCCFXForm viewableNode,
			OCCFXMeshExtractor meshExtractor, // pass a null
			InterpolateBezier interpolate,
			boolean showKnots,
			boolean showControlPoints,
			double pointSize,
			Object ... optObj
			) {
		
		this(aircraft3DView, viewableNode, meshExtractor, ShapeType.BEZIER_MESH, false);
		
		//------------------------------------------------------------
		// process optional arguments
		if (optObj.length  > 0) {
			if (optObj[0] == null) {
				_prefixName = "";
			}
			if (optObj[0] instanceof String) {
				_prefixName = (String)optObj[0];
			}
		} else { /* do nothing */ }
		if (optObj.length  > 1) {
			if ( (optObj[1] != null) && (optObj[1] instanceof Integer) ) { // TODO: Autoboxing?
				_pointsBetweenTwoConsecutiveKnots = (int)optObj[1];
			}
		} else { /* do nothing */ }
		if (optObj.length  > 2) {
			if ( (optObj[2] != null) && (optObj[2] instanceof Integer) ) {  // TODO: Autoboxing?
				_pointsPerCurveSection = (int)optObj[2];
			}
		}
		
		//------------------------------------------------------------
		
		// assign ID to Viewable
		theViewableNode.setId(_prefixName + ":SECTIONS:" + VIEWABLECAD_ID);
		
		// process bezier points
		
		OCCFXForm knotsControlPointsXform = new OCCFXForm();
		viewableNode.getChildren().add(knotsControlPointsXform);
		knotsControlPointsXform.setId("KNOTS:CONTROL:POINTS");
		
		double multiplier = 0.4d;
		if(showKnots || showControlPoints){
			interpolate.getSplines().forEach(spline->{ // <===================== LAMBDA
				org.fxyz.geometry.Point3D k0=spline.getPoints().get(0);
				org.fxyz.geometry.Point3D k1=spline.getPoints().get(1);
				org.fxyz.geometry.Point3D k2=spline.getPoints().get(2);
				org.fxyz.geometry.Point3D k3=spline.getPoints().get(3);
				if(showKnots){
					Sphere s=new Sphere(pointSize);
					s.getTransforms().add(new Translate(k0.x, k0.y, k0.z));
					s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
					knotsControlPointsXform.getChildren().add(s);
					s=new Sphere(pointSize);
					s.getTransforms().add(new Translate(k3.x, k3.y, k3.z));
					s.setMaterial(new PhongMaterial(Color.GREENYELLOW));
					knotsControlPointsXform.getChildren().add(s);
				}
				if(showControlPoints){
					org.fxyz.geometry.Point3D dir=k1.substract(k0).crossProduct(new org.fxyz.geometry.Point3D(0,-1,0));
					double angle=Math.acos(k1.substract(k0).normalize().dotProduct(new org.fxyz.geometry.Point3D(0,-1,0)));
					double h1=k1.substract(k0).magnitude();
					Cylinder c=new Cylinder(pointSize*multiplier,h1);
					c.getTransforms().addAll(new Translate(k0.x, k0.y-h1/2d, k0.z),
							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
					c.setMaterial(new PhongMaterial(Color.GREEN));
					knotsControlPointsXform.getChildren().add(c);

					dir=k2.substract(k1).crossProduct(new org.fxyz.geometry.Point3D(0,-1,0));
					angle=Math.acos(k2.substract(k1).normalize().dotProduct(new org.fxyz.geometry.Point3D(0,-1,0)));
					h1=k2.substract(k1).magnitude();
					c=new Cylinder(pointSize*multiplier,h1);
					c.getTransforms().addAll(new Translate(k1.x, k1.y-h1/2d, k1.z),
							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
					c.setMaterial(new PhongMaterial(Color.GREEN));
					knotsControlPointsXform.getChildren().add(c);

					dir=k3.substract(k2).crossProduct(new org.fxyz.geometry.Point3D(0,-1,0));
					angle=Math.acos(k3.substract(k2).normalize().dotProduct(new org.fxyz.geometry.Point3D(0,-1,0)));
					h1=k3.substract(k2).magnitude();
					c=new Cylinder(pointSize*multiplier,h1);
					c.getTransforms().addAll(new Translate(k2.x, k2.y-h1/2d, k2.z),
							new Rotate(-Math.toDegrees(angle), 0d,h1/2d,0d,
									new javafx.geometry.Point3D(dir.x,-dir.y,dir.z)));
					c.setMaterial(new PhongMaterial(Color.GREEN));
					knotsControlPointsXform.getChildren().add(c);

					Sphere s=new Sphere(pointSize);
					s.getTransforms().add(new Translate(k1.x, k1.y, k1.z));
					s.setMaterial(new PhongMaterial(Color.RED));
					knotsControlPointsXform.getChildren().add(s);
					s=new Sphere(pointSize);
					s.getTransforms().add(new Translate(k2.x, k2.y, k2.z));
					s.setMaterial(new PhongMaterial(Color.RED));
					knotsControlPointsXform.getChildren().add(s);
				}
			});
		}// end-of-if showKnots || showControlPoints
		//		long time=System.currentTimeMillis();
		List<BezierMesh> beziersInSection = new ArrayList<BezierMesh>();
		interpolate.getSplines().stream().forEach(spline->{ // <===================== LAMBDA
			BezierMesh bezier = new BezierMesh(
					spline, // BezierHelper spline
					pointSize*multiplier, // double wireRadius
					_pointsBetweenTwoConsecutiveKnots, // int rDivs (n. points between two consecutive knots)
					_pointsPerCurveSection, // int tDivs (n. points per curve section)
					0, // int lengthCrop
					0 // int wireCrop
					);
			
			// bezier.setDrawMode(DrawMode.LINE);
			bezier.setCullFace(CullFace.NONE);
			//          bezier.setSectionType(SectionType.TRIANGLE);
			// NONE
			bezier.setTextureModeNone(
					Color.CRIMSON
					// Color.hsb(360d*sp.getAndIncrement()/interpolate.getSplines().size(), 1, 1)
					);
			// IMAGE
			//          bezier.setTextureModeImage(getClass().getResource("res/LaminateSteel.jpg").toExternalForm());
			// PATTERN
			// bezier.setTextureModePattern(3d);
			// FUNCTION
			// bezier.setTextureModeVertices1D(256*256,t->spline.getKappa(t)); // t -> Math.cos( 2.0 * Math.PI * spline.getKappa(t) )
			// DENSITY
			//          bezier.setTextureModeVertices3D(256*256,dens);
			// FACES
			// bezier.setTextureModeFaces(256*256);

			Rotate rotateY = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
			bezier.getTransforms().addAll(new Rotate(0,Rotate.X_AXIS),rotateY);
			beziersInSection.add(bezier);
		});
		//		System.out.println("time: "+(System.currentTimeMillis()-time)); //43.815->25.606->15

		// add bezier to group member variable
		viewableNode.getChildren().addAll(beziersInSection);
		
	}
	
	private void computeGeometry() {
		
		if (_shapeType.equals(ShapeType.BEZIER_MESH)) return; // no need to compute mesh for BezierMesh objects
		
		System.out.println("ViewableCAD :: computeGeometry() _shapeType: " + _shapeType);
		
		System.out.println("ViewableCAD :: computeGeometry()");

		_facesXform = new OCCFXForm();
		_facesXform.setId(_prefixName + XFORM_FACES_ID + OCCFX3DView.SELECTABLE_ID_GENERIC_FACE);
		
		theViewableNode.getChildren().add(_facesXform);
		
		_facesFrontXform = new OCCFXForm();
		_facesFrontXform.setId(_prefixName + XFORM_SUB_FACES_ID);
		_facesXform.getChildren().add(_facesFrontXform);

		_facesBackXform = new OCCFXForm();
		_facesBackXform.setId(_prefixName + XFORM_SUB_BACKFACES_ID + OCCFX3DView.SELECTABLE_ID_GENERIC_FACE);
		_facesXform.getChildren().add(_facesBackXform);
		
		// Add _facesXform
		
		_theTriangleMeshFrontList = new ArrayList<TriangleMesh>();
		_theTriangleMeshBackList = new ArrayList<TriangleMesh>();
		
		for (TopoDS_Face face : _meshExtractor.getFaces())
		{
			OCCFXMeshExtractor.FaceData faceDataNotReversed = 
					new OCCFXMeshExtractor.FaceData(
							face,
							false // faceReversed
					);

			OCCFXLeafNode faceNode = new OCCFXLeafNode(
					_facesFrontXform, // parent node 
					faceDataNotReversed, // what to add
					this._frontFaceColor
			);
			// call refreshData() --> load() on (DataProvider/FaceData) objects inside LeafNode
			faceNode.refresh();
			
			// populate data structures
			_topoToNodeFaceFront.put(face, faceNode);
			_nodeToTopo.put(faceNode, face);

			OCCFXMeshExtractor.FaceData faceDataReversed = 
					new OCCFXMeshExtractor.FaceData(
							face,
							true // faceReversed
					);
			
			OCCFXLeafNode backFaceNode = new OCCFXLeafNode(
					_facesBackXform, // parent node 
					faceDataReversed, // what to add
					this._backFaceColor
			);
			// call refreshData() --> load() on (DataProvider/FaceData) objects inside LeafNode
			backFaceNode.refresh();
			
			_topoToNodeFaceBack.put(face, backFaceNode);
			_nodeToTopo.put(backFaceNode, face);
			
			// build list of TriangleMesh
			TriangleMesh mesh = faceDataNotReversed.getTriangleMesh();
			_theTriangleMeshFrontList.add(
					faceDataNotReversed.getTriangleMesh()
					);
			_theTriangleMeshBackList.add(
					faceDataReversed.getTriangleMesh()
					);
		}
		
		// finally add MeshView objects to the group
		
		_theMeshViewFrontList = 
				_theTriangleMeshFrontList // the TriangleMesh list FaceData objects
				.stream()
				.map(mesh -> { // <------------------ LAMBDA
					MeshView meshView = new MeshView(mesh); // get a MeshView for each TriangleMesh
					meshView.setMaterial(FACE_FRONT_MATERIAL);
					// meshView.setCullFace(CullFace.NONE); // set to CullFace.NONE (remove culling) to show back lines
					UUID uniqueKey = UUID.randomUUID();
					meshView.setId(
							// MyFXAircraft3DView.SELECTABLE_ID_GENERIC_FACE
							MESHVIEW_FRONT_ID + uniqueKey.toString()
							); // TODO: parametrize MeshView Id
					return meshView;
				})
				.collect(Collectors.toList());
		
		theViewableNode.getChildren().addAll(_theMeshViewFrontList);
		// TODO: make this step of mesh-views granular, i.e. put
		// a TriangleMesh and a MeshView into LeafNode class and
		// build a list from the Maps

		_theMeshViewBackList = 
				_theTriangleMeshBackList // the TriangleMesh list FaceData objects
				.stream()
				.map(mesh -> { // <------------------ LAMBDA
					MeshView meshView = new MeshView(mesh); // get a MeshView for each TriangleMesh
					meshView.setMaterial(FACE_BACK_MATERIAL);
					// meshView.setCullFace(CullFace.NONE); // set to CullFace.NONE (remove culling) to show back lines
					UUID uniqueKey = UUID.randomUUID();
					meshView.setId(
							// MyFXAircraft3DView.SELECTABLE_ID_GENERIC_FACE
							MESHVIEW_BACK_ID + uniqueKey.toString()
							); // TODO: parametrize MeshView Id
					return meshView;
				})
				.collect(Collectors.toList());
		
		theViewableNode.getChildren().addAll(_theMeshViewBackList);
		// TODO: make this step of mesh-views granular, i.e. put
		// a TriangleMesh and a MeshView into LeafNode class and
		// build a list from the Maps
		
		// measure your-self for correct fit view
		calculateViewableDiameter();
		theAircraft3DView.setSceneDiameter(_viewableDiameter);
		
	}
	
	private void calculateViewableDiameter() {
		_theBoundingBoxInLocal = (BoundingBox)theViewableNode.getBoundsInLocal();
		_viewableDiameter = Math.sqrt(
				Math.pow(_theBoundingBoxInLocal.getWidth(), 2) 
				+ Math.pow(_theBoundingBoxInLocal.getHeight(), 2) 
				+ Math.pow(_theBoundingBoxInLocal.getDepth(), 2)
				);
	}
	
	public List<TriangleMesh> getTriangleMeshFrontList() {
		return _theTriangleMeshFrontList;
	}
	public List<TriangleMesh> getTriangleMeshBackList() {
		return _theTriangleMeshBackList;
	}
	public List<TriangleMesh> getTriangleMeshList() { // Java 8 solution
		return ListUtils.union(_theTriangleMeshFrontList, _theTriangleMeshBackList);
	}
	
	public List<MeshView> getMeshViewFrontList() {
		return _theMeshViewFrontList;
	}
	public List<MeshView> getMeshViewBacktList() {
		return _theMeshViewFrontList;
	}

	public String getPrefixName() {
		return _prefixName;
	}

	public void setPrefixName(String _prefixName) {
		this._prefixName = _prefixName;
	}

}// end-of-class

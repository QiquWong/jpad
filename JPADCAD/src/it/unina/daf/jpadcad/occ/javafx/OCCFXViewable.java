package it.unina.daf.jpadcad.occ.javafx;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Logger;
//
//import gnu.trove.set.hash.TIntHashSet;
//import javafx.geometry.BoundingBox;
//import javafx.scene.Group;
//import javafx.scene.SubScene;
//
///**
// * This class represents viewable object of a View
// * @author Agodemar
// */
////public interface Viewable
////{
////	/** @param map map domain id (Integer) to visible state (Boolean) */
////	void setDomainVisible(Map<Integer, Boolean> map);
////	
////	/** Return the JavaFX Node associated to this Viewable */
////	Node getJavaFXSheneGraphNode();
////	
////	void pick(PickResult result);
////	void unselectAll();
////}
//
//public abstract class OCCFXViewable
//{
//	private final static Logger LOGGER = Logger.getLogger(OCCFXViewable.class.getName());
//	/** The position of the mouse when the press event occurs */
//	private String name;
//	protected final double tolerance = 0.002; // 0.2% of tolerance in function of the (far-near) distance
//	
//	protected final OCCFX3DView theAircraft3DView;
//	
//	protected final SubScene subScene;
//	/** The parent node of the viewable */
//	protected final Group parentNode;
//	/** The rootNode node of the viewable */
//	protected final OCCFXForm theViewableNode;
//	/** Set of selected nodes */
//	protected Set<OCCFXLeafNode> selectionNode = new HashSet<OCCFXLeafNode>();
//	/** Map of selected cells */
//	protected final Map<OCCFXLeafNode, TIntHashSet> selectionCell = new HashMap<OCCFXLeafNode, TIntHashSet>();
//	/** Flag to know if selection has changed */
//	protected boolean selectionChanged;
//	/** Flag to set selection in append or replace mode */
//	protected boolean appendSelection;
//	private SelectionType selectionType = SelectionType.NODE;
//	private int pixelTolerance;
//
//	protected BoundingBox _theBoundingBoxInLocal = null;
//	protected double _viewableDiameter =  0;
//
//	public enum SelectionType
//	{
//		NODE,
//		CELL,
//		POINT
//	}
//
//	public OCCFXViewable(OCCFX3DView aircraft3DView, OCCFXForm node)
//	{
//		theAircraft3DView = aircraft3DView;
//		subScene = theAircraft3DView.getSubScene();
//		parentNode = theAircraft3DView.get_subSceneRoot(); // always add the viewable to the root of 3D view
//		theViewableNode = node;
//		parentNode.getChildren().add(theViewableNode);
//	}
//
//	public double getViewableDiameter() {
//		return _viewableDiameter;
//	}
//
//	public void setViewableDiameter(double diameter) {
//		this._viewableDiameter = diameter;
//	}
//
//}
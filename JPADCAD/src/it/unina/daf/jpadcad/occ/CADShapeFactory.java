package it.unina.daf.jpadcad.occ;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import processing.core.PVector;

/**
 * Class to provide factory methods
 */
public abstract class CADShapeFactory {
	private static final Logger LOGGER=Logger.getLogger(CADShapeFactory.class.getName());
	private static CADShapeFactory factory;
	
	protected CADShapeFactory() {
	}

	public static void setFactory(CADShapeFactory f) {
		if (factory == null)
			factory = f;
		else
			throw new IllegalStateException("Factory already set to "+factory+".");
	}

	/**
	 * Return factory instance
	 * @return factory instance
	 */
	public static CADShapeFactory getFactory() {
		if (factory == null) {
			String cadType = System.getProperty("it.unina.daf.jpadcad.occ");
			if (cadType == null) {
				cadType = "it.unina.daf.jpadcad.occ.OCCShapeFactory";
				System.setProperty("it.unina.daf.jpadcad.occ", cadType);
			}
			try {
				factory = (CADShapeFactory) Class.forName(cadType).newInstance();
			}
			catch (Exception e) {
				LOGGER.severe("Class "+cadType+" not found");
				LOGGER.log(Level.SEVERE, null, e);
				System.exit(1);
			}
		}
		return factory;
	}
	
	/**
	 * Create a new CADShape wrapping an object of the underlying implementation
	 * @param o An object of the underlying implementation
	 * @return The created CADShape
	 */
	public abstract CADShape newShape (Object o);
	
	/**
	 * Create a new CADShape with boolean operation on 2 shapes.
	 * @param s1 First shape
	 * @param s2 Second shape
	 * @param op Boolean operator
	 * @return The created CADShape
	 */
	public abstract CADShape newShape (CADShape s1, CADShape s2, char op);
	
	/**
	 * Create a new CADShape by loading it from a file
	 * @param fileName The file to read
	 * @return The created CADShape
	 */
	public abstract CADShape newShape (String fileName);
	
	/**
	 * Create a new CADExplorer
	 * @return The created CADExplorer
	 */
	public abstract CADExplorer newExplorer ();
	
	/**
	 * Create a new CADWireExplorer
	 * @return The created CADWireExplorer
	 */
	public abstract CADWireExplorer newWireExplorer ();
	
	/*
	 * This method is needed to initialize CADShapeEnum
	 */
	protected abstract CADShapeTypes getShapeEnumInstance(String name);
	protected abstract Iterator<CADShapeTypes> newShapeEnumIterator(CADShapeTypes start, CADShapeTypes end);

	/**
	 * Create a new CADIterator
	 * @return The created CADIterator
	 */
	public abstract CADIterator newIterator ();
	
	/**
	 * Create a new CADGeomCurve2D
	 * @param E The edge owning the curve
	 * @param F The face owning the curve
	 * @return The created CADGeomCurve2D
	 */
	public abstract CADGeomCurve2D newCurve2D(CADEdge E, CADFace F);
	
	/**
	 * Create a new CADGeomCurve3D
	 * @param E The edge owning this curve
	 * @return The created CADGeomCurve3D
	 */
	public abstract CADGeomCurve3D newCurve3D(CADEdge E);

	/**
	 * Create a new CADGeomCurve3D
	 * @param two points in space
	 * @return The created CADGeomCurve3D, a segment
	 */
	public abstract CADGeomCurve3D newCurve3D(double[] pt1, double[] pt2);

	/**
	 * Create a new CADGeomCurve3D
	 * @param at least two points in space
	 * @return The created CADGeomCurve3D, a segment
	 */
	public abstract CADGeomCurve3D newCurve3D(boolean isPeriodic, double[] ... pts);
	
	/**
	 * Create a new CADGeomCurve3D
	 * @param pointList The list of points belonging to the curve (a BSpline)
	 * @return The created CADGeomCurve3D
	 */
	public abstract CADGeomCurve3D newCurve3D(List<double[]> pointList, boolean isPeriodic);

	/**
	 * Create a new CADGeomCurve3D
	 * @param pointList The list of PVector points belonging to the curve (a BSpline)
	 * @return The created CADGeomCurve3D
	 */
	public abstract CADGeomCurve3D newCurve3DP(List<PVector> pointList, boolean isPeriodic);
	
	/**
	 * Create a new CADShell by constructing a loft surface (see BRepOffsetAPI_ThruSections)
	 * @param cadGeomCurveList The list of CADGeomCurve3D through which the loft passes
	 * @return The created CADShell
	 */
	public abstract CADShell newShell(List<CADGeomCurve3D> cadGeomCurveList);

	public abstract CADShell newShell(List<CADGeomCurve3D> cadGeomCurveList, long isSolid, long ruled, double pres3d);	
	public abstract CADShell newShell(List<CADGeomCurve3D> cadGeomCurveList, long isSolid, long ruled);	

	public abstract CADShell newShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1);
	public abstract CADShell newShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1, long isSolid, long ruled, double pres3d);
	public abstract CADShell newShell(OCCVertex v0, List<CADGeomCurve3D> cadGeomCurveList, OCCVertex v1, long isSolid, long ruled);
	
	/**
	 * Create a new CADFace as a planar triangle connecting three vertices 
	 * @param The three vertices v0, v1, v2 
	 * @return The created CADFace
	 */
	public abstract CADFace newFacePlanar(double[] v0, double[] v1, double[] v2);
	public abstract CADFace newFacePlanar(CADVertex v0, CADVertex v1, CADVertex v2);

	/**
	 * Create a shell from adjacent faces
	 * @param cadFaces adjacent faces
	 * @return the created CADShell
	 */
	public abstract CADShell newShellFromAdjacentFaces(CADFace ... cadFaces);
	public abstract CADShell newShellFromAdjacentFaces(List<CADFace> cadFaces);
	
	/**
	 * Create a solid from adjacent faces
	 * @param cadFaces adjacent faces
	 * @return the created CADSolid
	 */
	public abstract CADSolid newSolidFromAdjacentFaces(CADFace ... cadFaces);
	public abstract CADSolid newSolidFromAdjacentFaces(List<CADFace> cadFaces);
	
	/**
	 * Create a new CADVertex from a triplet of coordinates
	 * @param x, y, z the cartesian coordinates
	 * @return The created CADVertex
	 */
	public abstract CADVertex newVertex(double x, double y, double z);

	/**
	 * Create a new CADVertex from a triplet of coordinates
	 * @param coordinates3D the array of cartesian coordinates
	 * @return The created CADVertex
	 */
	public abstract CADVertex newVertex(double [] coordinates3D);

}

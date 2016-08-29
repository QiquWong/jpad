package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological shape with an orientation, a location and a
 * geometry
 * @author Agostino De Marco
 *
 */
public interface CADShape
{
	/**
	 * Return the bounding box of this shape in an array like {Xmin, Ymin, Zmin, Xmax,
	 * Ymax, Zmax}.
	 */    
	public double [] boundingBox();
	/** Return a reversed instance of this shape */
	public CADShape reversed();
	/** Return the orientation of the shape */
	public int orientation();
	/** Return true if and only if the face is forward oriented*/
	public boolean isOrientationForward();
	/** Return true if o have same orientation and geometry as this object */
	public boolean equals(Object o);
	/** Return true if o have same geometry as this object */
	public boolean isSame(Object o);
	/** Write shape into the native format */
	public void writeNative(String filename);
	/** Return a hash code matching the equals method */
	public int hashCode();
}

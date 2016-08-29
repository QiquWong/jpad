package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological edge.
 * @author Agostino De Marco
 *
 */
public interface CADEdge extends CADShape
{
	/** Return true if this edge have only one vertex */
	public boolean isDegenerated();
	/** Return an array containing min and max value of the parameter
	 * describing the geometry of this edge */
	public double [] range();
	/** Return the extremities of this edge */
	public CADVertex [] vertices();
}

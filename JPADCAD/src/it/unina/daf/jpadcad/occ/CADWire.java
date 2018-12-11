package it.unina.daf.jpadcad.occ;

import java.util.List;

/**
 * Describe a topological wire, that is a list of connected edges.
 */
public interface CADWire extends CADShape
{
	/** Return the length of this wire */
	public double length();
	
	/** Return all the vertices of this wire */
	public List<CADVertex> vertices();
	
	/** Return all the edges belonging to this wire */
	public List<CADEdge> edges();
}

package it.unina.daf.jpadcad.occ;

/**
 * Explores edges of a wire
 */
public interface CADWireExplorer
{
    /**
     * Initialize the explorer
     * @param wire The wire to explore
     * @param face The underlying face (used for 2D analysis)
     */    
	public void init(CADWire wire, CADFace face);
    /**
     * Return true if there are more edges to explore
     * @return true if there are more edges to explore
     */    
	public boolean more();
    /**
     * Moves on to the next edge in the exploration
     */    
	public void next();
    /**
     * Return the current edge in the exploration
     * @return The current edge in the exploration
     */    
	public CADEdge current();
}

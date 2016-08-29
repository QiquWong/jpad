package it.unina.daf.jpadcad.occ;

/**
 * Explores a topological data structure. An explorer is built with the shape
 * to explore, the type of shape to find
 * @author Agostino De Marco
 *
 */
public interface CADExplorer
{
    /**
     * Initialize this explorer
     * @param shape The shape to explore
     * @param type The type of shape to find: FACE, WIRE, EDGE or VERTEX
     */    
	public void init(CADShape shape, CADShapeTypes type);
    /**
     * Return true if there are more shapes to explore
     * @return true if there are more shapes to explore
     */    
	public boolean more();
    /**
     * Move on to the next shape in the exploration
     */    
	public void next();
    /**
     * Return the current shape in the exploration
     * @return the current shape in the exploration
     */    
	public CADShape current();
}

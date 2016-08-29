package it.unina.daf.jpadcad.occ;

/**
 * Iterator on sub-shapes.
 * @author Agostino De Marco
 *
 */
public interface CADIterator
{
    /**
     * Initialize this explorer
     * @param shape The shape to explore
     */    
	public void initialize(CADShape shape);
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
	public CADShape value();
}

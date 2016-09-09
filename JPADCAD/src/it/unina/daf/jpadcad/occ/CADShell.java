package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological shell
 */
public interface CADShell extends CADShape
{
	/**
	 * Returns the area this shell, as a sum of the areas of all faces
	 * @return the area of this shell
	 */    
	public double getArea();
	
}

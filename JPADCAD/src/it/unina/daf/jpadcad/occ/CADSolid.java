package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological solid
 */
public interface CADSolid extends CADShape
{
	/**
	 * Returns the volume of this solid
	 * @return the volume of this solid
	 */    
	public double getVolume();
}

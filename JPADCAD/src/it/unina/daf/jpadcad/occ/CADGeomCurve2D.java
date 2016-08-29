package it.unina.daf.jpadcad.occ;

/**
 * Describe a geometrical 2D curve
 */
public interface CADGeomCurve2D
{
    /**
     * Return a point on this curve
     * @param p The paramater
     * @return an array {u, v}
     */    
	public double [] value(double p);
	
    /**
     * Return the range of the parametrization of this curve
     * @return an array {pMin, pMax}
     */    
	public double [] getRange();
}

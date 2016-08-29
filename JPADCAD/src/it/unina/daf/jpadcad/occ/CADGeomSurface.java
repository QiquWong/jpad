package it.unina.daf.jpadcad.occ;

/**
 * Describe a geometrical surface
 */
public interface CADGeomSurface
{
	/**
	 * Initialize the degree of the surface
	 * @param degree The degree of the surface
	 */    
	public void dinit(int degree);
    
	/**
	 * Set the u, v coordinates used for d1U, d1V and curvature operation
	 * @param u The u coordinate
	 * @param v The v coordinate
	 */    
	public void setParameter(double u, double v);
    
	/**
	 * Return the u first derivative vector at the coordinates set by setParameter method
	 * @return the u first derivative
	 */    
	public double [] d1U();
    
	/**
	 * Return the v first derivative vector at the coordinates set by setParameter method
	 * @return the v first derivative
	 */    
	public double [] d1V();
	public double [] d2U();
	public double [] d2V();
	public double [] dUV();

	/**
	 * Return the normal to the surface 
	 */    
	public double [] normal();
    
	/**
	 * Get 3D coordinates from (u, v) coordinates
	 * @param u The u coordinate
	 * @param v The v coordinate
	 * @return A array {x, y, z}
	 */    
	public double [] value(double u, double v);
	/**
	 * Return the minimum curvature at the current point
	 * @return the minimum curvature at the current point
	 */    
	public double minCurvature();
	/**
	 * Return the maximum curvature at the current point
	 * @return The maximum curvature at the current point
	 */    
	public double maxCurvature();
	/**
	 * Return the Gaussian curvature at the current point
	 * @return The Gaussian curvature at the current point
	 */    
	public double gaussianCurvature();
	/**
	 * Return the mean curvature at the current point
	 * @return The mean curvature at the current point
	 */    
	public double meanCurvature();
	/**
	 * Return the direction of maximum and minimum curvature at the current point
	 * @return An array: {Xmax, Ymax, Zmax, Xmin, Ymin, Zmin}
	 */    
	public double [] curvatureDirections();
	/**
	 * Return distance of a point to this surface
	 * @param p  3-d point
	 * @return distance between this point and the surface
	 */    
	public double lowerDistance(double [] p);
}

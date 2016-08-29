package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological face.
 */
public interface CADFace extends CADShape
{
	/**
	 * Returns the bounding box of this face in an array like {Xmin, Ymin, Zmin, Xmax,
	 * Ymax, Zmax}.
	 * @return the bounding box of this face
	 */    
	public double [] boundingBox();
	/**
	 * Returns the geometry of this face
	 * @return the geometry of this face
	 */    
	public CADGeomSurface getGeomSurface();
}

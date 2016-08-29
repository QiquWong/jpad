package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological vertex
 */
public interface CADVertex extends CADShape
{
    /**
     * Return the 2D coordinates (u, v) of this vertex on a face
     * @return An array: {u, v}
     * @param that The face
     */    
	public double [] parameters(CADFace that);
    /**
     * Return the 3D coordinates of this vertex
     * @return An array: {x, y, z}
     */    
	public double [] pnt();
}

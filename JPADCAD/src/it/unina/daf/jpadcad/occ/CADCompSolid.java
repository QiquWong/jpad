package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological compsolid
 */
public interface CADCompSolid extends CADShape
{
	public boolean add(CADShape s);
}

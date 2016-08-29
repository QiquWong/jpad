package it.unina.daf.jpadcad.occ;

/**
 * Describe a topological solid
 */
public interface CADCompound extends CADShape
{
	public boolean add(CADShape s);
}

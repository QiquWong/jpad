package it.unina.daf.jpadcad.occ;

import java.util.Iterator;

/**
 * Typesafe enum of CAD types
 */
public abstract class CADShapeTypes
{
	private final String name;
	protected final int ordinal;
	private static int nextOrdinal = 0;
	protected CADShapeTypes(String name)
	{
		this.name = name;
		this.ordinal = nextOrdinal++;
	}
	public abstract Class<? extends CADShape> asClass();
	@Override
	public final String toString()
	{
		return name;
	}

	public static final CADShapeTypes VERTEX = CADShapeFactory.getFactory().getShapeEnumInstance("vertex");
	public static final CADShapeTypes EDGE = CADShapeFactory.getFactory().getShapeEnumInstance("edge");
	public static final CADShapeTypes WIRE = CADShapeFactory.getFactory().getShapeEnumInstance("wire");
	public static final CADShapeTypes FACE = CADShapeFactory.getFactory().getShapeEnumInstance("face");
	public static final CADShapeTypes SHELL = CADShapeFactory.getFactory().getShapeEnumInstance("shell");
	public static final CADShapeTypes SOLID = CADShapeFactory.getFactory().getShapeEnumInstance("solid");
	public static final CADShapeTypes COMPSOLID = CADShapeFactory.getFactory().getShapeEnumInstance("compsolid");
	public static final CADShapeTypes COMPOUND = CADShapeFactory.getFactory().getShapeEnumInstance("compound");
	
	@Deprecated
	public static Iterator<CADShapeTypes> iterator(CADShapeTypes start, CADShapeTypes end)
	{
		return CADShapeFactory.getFactory().newShapeEnumIterator(start, end);
	}

	public static Iterable<CADShapeTypes> iterable(final CADShapeTypes start, final CADShapeTypes end)
	{
		return new Iterable<CADShapeTypes>() {
			public Iterator<CADShapeTypes> iterator()
			{
				return CADShapeFactory.getFactory().newShapeEnumIterator(start, end);
			}
		};
	}

}

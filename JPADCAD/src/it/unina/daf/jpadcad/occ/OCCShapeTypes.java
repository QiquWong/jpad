package it.unina.daf.jpadcad.occ;

import java.util.Iterator;
import java.util.NoSuchElementException;

import opencascade.TopAbs_ShapeEnum;

/**
 * Typesafe enum of CAD types
 */
public abstract class OCCShapeTypes extends CADShapeTypes
{
	// occExplorerEnum must be identical to TopAbs_ShapeEnum.
	private final TopAbs_ShapeEnum occExplorerEnum;
	private OCCShapeTypes(String name, int t)
	{
		super(name);
		occExplorerEnum = TopAbs_ShapeEnum.swigToEnum(t);
	}
	final TopAbs_ShapeEnum asType()
	{
		return occExplorerEnum;
	}

	private static final OCCShapeTypes OCC_VERTEX = new OCCShapeTypes("vertex", 7) {
		@Override
		public Class<CADVertex> asClass() { return CADVertex.class; }
	};
	private static final OCCShapeTypes OCC_EDGE = new OCCShapeTypes("edge", 6) {
		@Override
		public Class<CADEdge> asClass() { return CADEdge.class; }
	};
	private static final OCCShapeTypes OCC_WIRE = new OCCShapeTypes("wire", 5) {
		@Override
		public Class<CADWire> asClass() { return CADWire.class; }
	};
	private static final OCCShapeTypes OCC_FACE = new OCCShapeTypes("face", 4) {
		@Override
		public Class<CADFace> asClass() { return CADFace.class; }
	};
	private static final OCCShapeTypes OCC_SHELL = new OCCShapeTypes("shell", 3) {
		@Override
		public Class<CADShell> asClass() { return CADShell.class; }
	};
	private static final OCCShapeTypes OCC_SOLID = new OCCShapeTypes("solid", 2) {
		@Override
		public Class<CADSolid> asClass() { return CADSolid.class; }
	};
	private static final OCCShapeTypes OCC_COMPSOLID = new OCCShapeTypes("compsolid", 1) {
		@Override
		public Class<CADCompSolid> asClass() { return CADCompSolid.class; }
	};
	private static final OCCShapeTypes OCC_COMPOUND = new OCCShapeTypes("compound", 0) {
		@Override
		public Class<CADCompound> asClass() { return CADCompound.class; }
	};
	// Note: VALUES must contain items in the same order as they
	// have been declared in this file!
	private static final OCCShapeTypes [] VALUES = {
		OCC_VERTEX,
		OCC_EDGE,
		OCC_WIRE,
		OCC_FACE,
		OCC_SHELL,
		OCC_SOLID,
		OCC_COMPSOLID,
		OCC_COMPOUND };

	// Export ordinal to newShapeEnumIterator
	final int getOrdinal()
	{
		return ordinal;
	}
	static Iterator<CADShapeTypes> newShapeEnumIterator(final OCCShapeTypes start, final OCCShapeTypes end)
	{
		final int iStep;
		if (start.getOrdinal() > end.getOrdinal())
			iStep = -1;
		else
			iStep = 1;
		return new Iterator<CADShapeTypes>()
		{
			OCCShapeTypes current = null;
			public boolean hasNext()
			{
				return current != end;
			}
			public CADShapeTypes next()
			{
				if (!hasNext())
					throw new NoSuchElementException();
				if (current == null)
					current = start;
				else
					current = VALUES[current.getOrdinal()+iStep];
				return current;
			}
			public void remove()
			{
			}
		};
	}
	static OCCShapeTypes getSingleton(String name)
	{
		for (int i = 0; i < VALUES.length; i++)
			if (VALUES[i].toString().equals(name))
				return VALUES[i];
		throw new IllegalArgumentException();
	}

}

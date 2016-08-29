package it.unina.daf.jpadcad.occ;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import opencascade.BRepAlgoAPI_BooleanOperation;
import opencascade.BRepAlgoAPI_Common;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.IGESControl_Reader;
import opencascade.STEPControl_Reader;
import opencascade.TopoDS_Shape;

/*
 * Note: this class is used only by reflection, see CADShapeFactory#factory
 * initialization.
 */
public class OCCShapeFactory extends CADShapeFactory
{
	private static final Logger logger=Logger.getLogger(OCCShapeFactory.class.getName());
	
	@Override
	public final CADShape newShape(Object o)
	{
		if (!(o instanceof TopoDS_Shape))
			throw new IllegalArgumentException();
		TopoDS_Shape ts = (TopoDS_Shape) o;
		OCCShape shape;
		switch (ts.ShapeType())
		{
			case TopAbs_COMPOUND:
				shape = new OCCCompound();
				break;
			case TopAbs_SOLID:
				shape = new OCCSolid();
				break;
			case TopAbs_SHELL:
				shape = new OCCShell();
				break;
			case TopAbs_FACE:
				shape = new OCCFace();
				break;
			case TopAbs_WIRE:
				shape = new OCCWire();
				break;
			case TopAbs_EDGE:
				shape = new OCCEdge();
				break;
			case TopAbs_VERTEX:
				shape = new OCCVertex();
				break;
			default:
				shape = new OCCShape();
				break;
		}
		shape.setShape(ts);
		return shape;
	}
	
	@Override
	public CADShape newShape (String fileName)
	{
		TopoDS_Shape brepShape;
		if (fileName.endsWith(".step") || fileName.endsWith(".stp"))
		{
			logger.fine("Read STEP file: "+fileName);
			STEPControl_Reader aReader = new STEPControl_Reader();
			aReader.ReadFile(fileName);
			logger.fine("Transfer roots into shape...");
			aReader.NbRootsForTransfer();
			aReader.TransferRoots();
			brepShape = aReader.OneShape();
			logger.fine("... done");
		}
		else if (fileName.endsWith(".igs"))
		{
			logger.fine("Read IGES file: "+fileName);
			IGESControl_Reader aReader = new IGESControl_Reader();
			aReader.ReadFile(fileName);
			logger.fine("Transfer roots into shape...");
			aReader.NbRootsForTransfer();
			aReader.TransferRoots();
			brepShape = aReader.OneShape();
			logger.fine("... done");
		}
		else
		{
			logger.fine("Read BREP file: "+fileName);
			//brepShape = BRepTools.read(fileName, new BRep_Builder());
			TopoDS_Shape shape = new TopoDS_Shape();
			BRepTools.Read(shape, fileName, new BRep_Builder());
			brepShape = shape;
			logger.fine("... done");
		}
		return newShape(brepShape);
	}
	
	/**
	 * @param type 'u'=fuse 'n'=common '\\'=cut
	 */
	@Override
	public CADShape newShape(CADShape o1, CADShape o2, char type)
	{
		CADShape res = null;
		TopoDS_Shape s1 = ((OCCShape) o1).getShape();
		TopoDS_Shape s2 = ((OCCShape) o2).getShape();
/* With libOccJava
		short t = -1;
		if (type == 'u')
			t = 0;
		else if (type == 'n')
			t = 1;
		else if (type == '\\')
			t = 2;
		BRepAlgoAPI_BooleanOperation op = new BRepAlgoAPI_BooleanOperation(s1, s2, t);
		TopoDS_Shape s = op.shape();
		if (s != null)
			res = newShape(s);
*/
		BRepAlgoAPI_BooleanOperation op = null;
		try
		{
			if (type == 'u')
				op = new BRepAlgoAPI_Fuse(s1, s2);
			else if (type == 'n')
				op = new BRepAlgoAPI_Common(s1, s2);
			else if (type == '\\')
				op = new BRepAlgoAPI_Cut(s1, s2);
			else
				throw new IllegalArgumentException();
			TopoDS_Shape s = op.Shape();
			if (s != null)
				res = newShape(s);
		}
		catch (RuntimeException ex)
		{
		}
		return res;
	}
	
	@Override
	public CADExplorer newExplorer()
	{
		return new OCCExplorer();
	}
	
	@Override
	public CADWireExplorer newWireExplorer()
	{
		return new OCCWireExplorer();
	}

	@Override
	public CADIterator newIterator()
	{
		return new OCCIterator();
	}
	
	@Override
	protected Iterator<CADShapeTypes> newShapeEnumIterator(CADShapeTypes start, CADShapeTypes end)
	{
		return OCCShapeTypes.newShapeEnumIterator((OCCShapeTypes) start, (OCCShapeTypes) end);
	}

	@Override
	protected CADShapeTypes getShapeEnumInstance(String name)
	{
		return OCCShapeTypes.getSingleton(name);
	}

	@Override
	public CADGeomCurve2D newCurve2D(CADEdge E, CADFace F)
	{
		CADGeomCurve2D curve = null;
		try
		{
			curve = new OCCGeomCurve2D(E, F);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}
	
	@Override
	public CADGeomCurve3D newCurve3D(CADEdge E)
	{
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(E);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}

	@Override
	public CADGeomCurve3D newCurve3D(List<double[]> pointList, boolean isPeriodic) {
		CADGeomCurve3D curve = null;
		try
		{
			curve = new OCCGeomCurve3D(pointList, isPeriodic);
		}
		catch (RuntimeException ex)
		{
		}
		return curve;
	}
	
}

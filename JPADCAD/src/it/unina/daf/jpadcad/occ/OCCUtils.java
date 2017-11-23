package it.unina.daf.jpadcad.occ;

import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.TopoDS_Compound;

public final class OCCUtils {

	public static CADShapeFactory theFactory;

	static public void initCADShapeFactory() {
		CADShapeFactory.setFactory(new OCCShapeFactory());
		theFactory = CADShapeFactory.getFactory();
	}
	
	public static boolean write(String fileName, OCCShape ... shapes) {
		
		// Put everything in a compound
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		
		for(int k = 0; k < shapes.length; k++)
			builder.Add(compound, shapes[k].getShape());
		
		long result = BRepTools.Write(compound, fileName);
		return (result == 1);
	}
	
}

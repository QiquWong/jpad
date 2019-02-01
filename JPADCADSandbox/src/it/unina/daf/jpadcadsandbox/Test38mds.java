package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.occ.OCCWire;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepGProp;
import opencascade.GProp_GProps;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.ShapeAnalysis_FreeBounds;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;

public class Test38mds {

	public static void main(String[] args) {
		System.out.println("-----------------------------------------");
		System.out.println("--- Turbofan engine template creation ---");
		System.out.println("-----------------------------------------");
		
		// -------------------
		// Set the factory
		// -------------------
		if (OCCUtils.theFactory == null) {
			OCCUtils.initCADShapeFactory();
		}
		
		List<OCCShape> exportShapes = new ArrayList<>();

		gp_Dir xDir = new gp_Dir(1.0, 0.0, 0.0);	
		gp_Dir yDir = new gp_Dir(0.0, 1.0, 0.0);
		gp_Dir zDir = new gp_Dir(0.0, 0.0, 1.0);
		
		// -------------------------
		// Import necessary parts
		// -------------------------
		STEPControl_Reader engineReader = new STEPControl_Reader();
		engineReader.ReadFile("C:\\Users\\Mario\\Desktop\\Turbofan_parts\\turbofan_engine.step");
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		System.out.println("Turbofan engine STEP reader problems:");
		engineReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		engineReader.TransferRoots();
		TopoDS_Shape engineShapes = engineReader.OneShape();
		
		System.out.println("Turbofan engine imported shapes type: " + engineShapes.ShapeType().toString());
		
		// -----------------------------------
		// Explore the TURBOFAN ENGINE shapes
		// -----------------------------------
		List<OCCShape> engineSolids = new ArrayList<>();
		
		TopExp_Explorer engineExp = new TopExp_Explorer();
		engineExp.Init(engineShapes, TopAbs_ShapeEnum.TopAbs_SOLID);
		while (engineExp.More() > 0) {
			engineSolids.add((OCCSolid) OCCUtils.theFactory.newShape(TopoDS.ToSolid(engineExp.Current())));
			engineExp.Next();
		}
		System.out.println("Number of solid elements found for the turbofan engine: " + engineSolids.size());
		
		// Check for the presence of free bounds
		List<OCCWire> engineFreeWires = new ArrayList<>();
	
		for (Iterator<OCCShape> iter = engineSolids.iterator(); iter.hasNext(); ) {
			OCCShape solid = iter.next();
			ShapeAnalysis_FreeBounds shapeAnalyzer = new ShapeAnalysis_FreeBounds(solid.getShape());
				
			TopExp_Explorer expCW = new TopExp_Explorer(
					shapeAnalyzer.GetClosedWires(), 
					TopAbs_ShapeEnum.TopAbs_WIRE
					);
			
			TopExp_Explorer expOW = new TopExp_Explorer(
					shapeAnalyzer.GetOpenWires(),
					TopAbs_ShapeEnum.TopAbs_WIRE
					);
			
			while (expCW.More() > 0) {
				engineFreeWires.add(
						(OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(expCW.Current())));
				expCW.Next();
			}
			
			while (expOW.More() > 0) {
				engineFreeWires.add(
						(OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(expOW.Current())));
				expOW.Next();
			}
		}	
		System.out.println("Number of free wires found for the turbofan engine elements: " + engineFreeWires.size());
		
		// ------------------------------
		// Calculate the whole engine CG
		// ------------------------------
		gp_Pnt engineCG = getCG(engineShapes);
		
		// -----------------
		// Stretching test
		// -----------------
		long start = System.currentTimeMillis(); 
		
		gp_GTrsf stretchingX = new gp_GTrsf();	
		gp_GTrsf stretchingY = new gp_GTrsf();
		gp_GTrsf stretchingZ = new gp_GTrsf();
		
		gp_Ax2 xStretchingRS = new gp_Ax2(engineCG, xDir);
		stretchingX.SetAffinity(xStretchingRS, 1.15);
		
		OCCShape xStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_GTransform(engineShapes, stretchingX, 0).Shape()
						));
		
		gp_Ax2 yStretchingRS = new gp_Ax2(engineCG, yDir);
		gp_Ax2 zStretchingRS = new gp_Ax2(engineCG, zDir);
		stretchingY.SetAffinity(yStretchingRS, 1.25);
		stretchingZ.SetAffinity(zStretchingRS, 1.25);
		
		OCCShape yStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_GTransform(xStretchedEngine.getShape(), stretchingY, 0).Shape()
						));
		OCCShape zStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_GTransform(yStretchedEngine.getShape(), stretchingZ, 0).Shape()
						));
		
		long elapsedTime = System.currentTimeMillis() - start;	
		System.out.println("Stretching operations elapsed time (seconds): " + elapsedTime / 1.0e03);
		
		exportShapes.add(zStretchedEngine);
		
		// ----------------
		// Export to file
		// ----------------
		OCCUtils.write("Test38mds", FileExtension.STEP, exportShapes);
	}
	
	private static gp_Pnt getCG(TopoDS_Shape shape) {

		GProp_GProps gProp = new GProp_GProps();
		BRepGProp.LinearProperties(shape, gProp);	
		gp_Pnt cg = gProp.CentreOfMass();	

		return cg;
	}

}

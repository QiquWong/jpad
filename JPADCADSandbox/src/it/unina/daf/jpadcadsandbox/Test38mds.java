package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occfx.OCCFXMeshExtractor;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import it.unina.daf.jpadcad.occ.OCCWire;
import opencascade.BRepAlgoAPI_Fuse;
import opencascade.BRepBndLib;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Sewing;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRep_Builder;
import opencascade.Bnd_Box;
import opencascade.GProp_GProps;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.ShapeAnalysis_FreeBounds;
import opencascade.ShapeExtend_Explorer;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;

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
		
		// -------------------------------------------------
		// Translate to the origin of the reference system
		// -------------------------------------------------
		OCCShape caseLip = engineSolids.get(1);
		TopExp_Explorer caseLipExp = new TopExp_Explorer();
		caseLipExp.Init(caseLip.getShape(), TopAbs_ShapeEnum.TopAbs_WIRE);
		
		List<OCCWire> lipWires = new ArrayList<>();
		while (caseLipExp.More() > 0) {
			lipWires.add((OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(caseLipExp.Current())));
			caseLipExp.Next();
		}	
		System.out.println("Engine case lip z coordinate: " + lipWires.get(1).vertices().get(0).pnt()[0]);
		
		// Get lip dimensions
		Bnd_Box caseLipBB = new Bnd_Box();
		new BRepMesh_IncrementalMesh(caseLip.getShape(), 7.0e-03);
		BRepBndLib.Add(caseLip.getShape(), caseLipBB, 1);
		caseLipBB.SetGap(0.0);
		double[] xMinCL = new double[1];
		double[] yMinCL = new double[1]; 
		double[] zMinCL = new double[1];
		double[] xMaxCL = new double[1];
		double[] yMaxCL = new double[1];
		double[] zMaxCL = new double[1];
		caseLipBB.Get(xMinCL, yMinCL, zMinCL, xMaxCL, yMaxCL, zMaxCL);
		
		System.out.println("Case lip bounding box coordinates:");
		System.out.println("X min = " + xMinCL[0] + ", X max = " + xMaxCL[0]);
		System.out.println("Y min = " + yMinCL[0] + ", Y max = " + yMaxCL[0]);
		System.out.println("Z min = " + zMinCL[0] + ", Z max = " + zMaxCL[0]);
		
		double refCaseLipLength = xMaxCL[0] - xMinCL[0];
		double refCaseLipWidth = yMaxCL[0] - yMinCL[0];
		double refCaseLipHeight = zMaxCL[0] - zMinCL[0];
		
		System.out.println("Case lip reference dimensions:");
		System.out.println("Lenght = " + refCaseLipLength);
		System.out.println("Width = " + refCaseLipWidth);
		System.out.println("Height = " + refCaseLipHeight);
		
		// Translate the whole engine to the origin of the reference system	
		gp_Trsf translate0 = new gp_Trsf();
		translate0.SetTranslation(
				new gp_Pnt(xMinCL[0], 0.0, 0.0), 
				new gp_Pnt(0.0, 0.0, 0.0)
				);
		
		OCCShape translatedEngine0 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_Transform(engineShapes, translate0, 0).Shape()
						));
		
		// -----------------------------------------
		// Rotate with respect to the symmetry axis
		// -----------------------------------------
		gp_Trsf rotate0 = new gp_Trsf();
		rotate0.SetRotation(new gp_Ax1(new gp_Pnt(0.0, 0.0, 0.0), xDir), -0.5*Math.PI);
		
		OCCShape rotatedEngine0 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_Transform(translatedEngine0.getShape(), rotate0, 0).Shape()
						));
		
		// -----------------------------------------
		// Fuse the first and second solid together
		// -----------------------------------------
		OCCExplorer engineExp1 = new OCCExplorer();
		engineExp1.init(rotatedEngine0, CADShapeTypes.SOLID);
		
		List<OCCShape> engineSolid = new ArrayList<>();
		while (engineExp1.more()) {
			engineSolid.add((OCCSolid) engineExp1.current());
			engineExp1.next();
		}
		
		List<OCCShape> lipFaces = new ArrayList<>();
		List<OCCShape> caseFaces = new ArrayList<>();
		OCCExplorer lipFaceExp = new OCCExplorer();
		OCCExplorer caseFaceExp = new OCCExplorer();
		lipFaceExp.init(engineSolid.get(1), CADShapeTypes.FACE);
		caseFaceExp.init(engineSolid.get(0), CADShapeTypes.FACE);
		
		while (lipFaceExp.more()) {
			lipFaces.add((OCCShape) lipFaceExp.current());
			lipFaceExp.next();
		}
		
		while (caseFaceExp.more()) {
			caseFaces.add((OCCShape) caseFaceExp.current());
			caseFaceExp.next();
		}
		
		lipFaces.remove(2);
		caseFaces.remove(4);
		caseFaces.addAll(lipFaces);
		
		OCCShell caseShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
				caseFaces.toArray(new OCCShape[caseFaces.size()])
				);
		
		OCCShape caseSolid = (OCCShape) OCCUtils.theFactory.newSolidFromAdjacentShells(caseShell);
		
		engineSolid.remove(0);
		engineSolid.remove(0);
		engineSolid.add(0, caseSolid);
		
		// ----------------------------------------------------
		// Revert engine shapes
		// ----------------------------------------------------
		System.out.println("Orientation before: " + engineSolid.get(0).orientation());
		System.out.println("Orientation after: " + engineSolid.get(0).reversed().orientation());
		
		// ----------------------------------------------------
		// Get engine reference dimensions, points, and edges
		// ----------------------------------------------------
		BRep_Builder engineCompSolidBuilder = new BRep_Builder();
		TopoDS_CompSolid engineCompSolid = new TopoDS_CompSolid();
		engineCompSolidBuilder.MakeCompSolid(engineCompSolid);
		
		engineCompSolidBuilder.Add(engineCompSolid, engineSolid.get(0).getShape());
		engineCompSolidBuilder.Add(engineCompSolid, engineSolid.get(1).getShape());
		engineCompSolidBuilder.Add(engineCompSolid, engineSolid.get(3).getShape());
		
		Bnd_Box engineBB = new Bnd_Box();
		new BRepMesh_IncrementalMesh(engineCompSolid, 7.0e-03);
		BRepBndLib.Add(engineCompSolid, engineBB, 1);
		engineBB.SetGap(0.0);
		double[] xMin = new double[1];
		double[] yMin = new double[1]; 
		double[] zMin = new double[1];
		double[] xMax = new double[1];
		double[] yMax = new double[1];
		double[] zMax = new double[1];
		engineBB.Get(xMin, yMin, zMin, xMax, yMax, zMax);
		
		System.out.println("Engine bounding box coordinates:");
		System.out.println("X min = " + xMin[0] + ", X max = " + xMax[0]);
		System.out.println("Y min = " + yMin[0] + ", Y max = " + yMax[0]);
		System.out.println("Z min = " + zMin[0] + ", Z max = " + zMax[0]);
		
		double refEngineLength = xMax[0] - xMin[0];
		double refEngineWidth = yMax[0] - yMin[0];
		double refEngineHeight = zMax[0] - zMin[0];
		
		System.out.println("Engine reference dimensions:");
		System.out.println("Lenght = " + refEngineLength);
		System.out.println("Width = " + refEngineWidth);
		System.out.println("Height = " + refEngineHeight);
		
		OCCShape outerCasing = engineSolid.get(0);
		
		OCCExplorer outerCasingExp = new OCCExplorer();
		outerCasingExp.init(outerCasing, CADShapeTypes.EDGE);
		
		List<OCCEdge> outerCasingEdges = new ArrayList<>();
		while (outerCasingExp.more()) {
			outerCasingEdges.add((OCCEdge) outerCasingExp.current());
			outerCasingExp.next();
		}
		
		double outerCasingZOut = outerCasingEdges.get(18).vertices()[1].pnt()[2];
		System.out.println("Outer casing exit radius: " + outerCasingZOut);
		
		// ---------------------------------------------------------
		// Get inner casing reference dimensions, points, and edges
		// ---------------------------------------------------------
		OCCShape innerCasing = engineSolid.get(1);
		
		Bnd_Box innerCasingBB = new Bnd_Box();
		new BRepMesh_IncrementalMesh(innerCasing.getShape(), 7.0e-03);
		BRepBndLib.Add(innerCasing.getShape(), innerCasingBB, 1);
		innerCasingBB.SetGap(0.0);
		double[] xMinIC = new double[1];
		double[] yMinIC = new double[1]; 
		double[] zMinIC = new double[1];
		double[] xMaxIC = new double[1];
		double[] yMaxIC = new double[1];
		double[] zMaxIC = new double[1];
		innerCasingBB.Get(xMinIC, yMinIC, zMinIC, xMaxIC, yMaxIC, zMaxIC);
		
		System.out.println("Engine inner casing bounding box coordinates:");
		System.out.println("X min = " + xMinIC[0] + ", X max = " + xMaxIC[0]);
		System.out.println("Y min = " + yMinIC[0] + ", Y max = " + yMaxIC[0]);
		System.out.println("Z min = " + zMinIC[0] + ", Z max = " + zMaxIC[0]);
		
		double refInnerCasingLength = xMaxIC[0] - xMinIC[0];
		double refInnerCasingWidth = yMaxIC[0] - yMinIC[0];
		double refInnerCasingHeight = zMaxIC[0] - zMinIC[0];
		
		System.out.println("Engine inner casing dimensions:");
		System.out.println("Lenght = " + refInnerCasingLength);
		System.out.println("Width = " + refInnerCasingWidth);
		System.out.println("Height = " + refInnerCasingHeight);
		
		OCCExplorer innerCasingExp = new OCCExplorer();
		innerCasingExp.init(innerCasing, CADShapeTypes.EDGE);
		
		List<OCCEdge> innerCasingEdges = new ArrayList<>();
		while (innerCasingExp.more()) {
			innerCasingEdges.add((OCCEdge) innerCasingExp.current());
			innerCasingExp.next();
		}
		
		double innerCasingZOut = innerCasingEdges.get(2).vertices()[0].pnt()[2];
		System.out.println("Inner casing exit radius: " + innerCasingZOut);
		
		// ----------------------
		// Generate the template
		// ----------------------
		MyConfiguration.setDir(FoldersEnum.OUTPUT_DIR, MyConfiguration.inputDirectory);
		String outputFolderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + 
				  "Template_CADEngines" + File.separator + 
				  "turbofan_templates" + File.separator;
		
//		OCCUtils.write(outputFolderPath + "TF_complete_01", FileExtension.STEP, engineSolid);
		OCCUtils.write("Test38mds", FileExtension.STEP, engineSolid);
		
//		// -----------------
//		// Stretching test
//		// -----------------
//		long start = System.currentTimeMillis(); 
//		
//		gp_GTrsf stretchingX = new gp_GTrsf();	
//		gp_GTrsf stretchingY = new gp_GTrsf();
//		gp_GTrsf stretchingZ = new gp_GTrsf();
//		
//		gp_Ax2 xStretchingRS = new gp_Ax2(engineCG, xDir);
//		stretchingX.SetAffinity(xStretchingRS, 1.15);
//		
//		OCCShape xStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_GTransform(engineShapes, stretchingX, 0).Shape()
//						));
//		
//		gp_Ax2 yStretchingRS = new gp_Ax2(engineCG, yDir);
//		gp_Ax2 zStretchingRS = new gp_Ax2(engineCG, zDir);
//		stretchingY.SetAffinity(yStretchingRS, 1.25);
//		stretchingZ.SetAffinity(zStretchingRS, 1.25);
//		
//		OCCShape yStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_GTransform(xStretchedEngine.getShape(), stretchingY, 0).Shape()
//						));
//		OCCShape zStretchedEngine = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_GTransform(yStretchedEngine.getShape(), stretchingZ, 0).Shape()
//						));
//		
//		long elapsedTime = System.currentTimeMillis() - start;	
//		System.out.println("Stretching operations elapsed time (seconds): " + elapsedTime / 1.0e03);
//		
//		exportShapes.add(zStretchedEngine);
		
	}
	
}

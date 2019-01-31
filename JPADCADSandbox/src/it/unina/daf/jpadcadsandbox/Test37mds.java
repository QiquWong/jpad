package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.java_cup.internal.runtime.Symbol;

import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCFace;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.occ.OCCWire;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.GProp_GProps;
import opencascade.GeomLProp_SLProps;
import opencascade.Geom_Surface;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.ShapeAnalysis_FreeBounds;
import opencascade.ShapeAnalysis_Surface;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopExp_Explorer;
import opencascade.TopTools_IndexedMapOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Edge;
import opencascade.TopoDS_Face;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Shell;
import opencascade.TopoDS_Wire;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Pnt2d;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;
import standaloneutils.MyArrayUtils;

public class Test37mds {

	public static void main(String[] args) {
		System.out.println("------------------------------------------");
		System.out.println("--- Turboprop engine template creation ---");
		System.out.println("------------------------------------------");
		
		// Set the factory
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
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		// NACELLE
		STEPControl_Reader nacelleReader = new STEPControl_Reader();	
		nacelleReader.ReadFile("C:\\Users\\Mario\\Desktop\\Turboprop_parts\\nacelle_01.step");
		
		System.out.println("Nacelle STEP reader problems:");
		nacelleReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		System.out.println("\n");
		
		nacelleReader.TransferRoots();
		TopoDS_Shape nacelleShapes = nacelleReader.OneShape();
		
		System.out.println("Nacelle imported shapes type: " + nacelleShapes.ShapeType().toString());
		
		// BLADE
		STEPControl_Reader bladeReader = new STEPControl_Reader();	
		bladeReader.ReadFile("C:\\Users\\Mario\\Desktop\\Turboprop_parts\\blade_01.step");
		
		System.out.println("Blade STEP reader problems:");
		bladeReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		System.out.println("\n");
		
		bladeReader.TransferRoots();
		TopoDS_Shape bladeShapes = bladeReader.OneShape();
		
		System.out.println("Blade imported shapes type: " + bladeShapes.ShapeType().toString());
		
		// ----------------------------
		// Explore the NACELLE shapes
		// ----------------------------
		List<OCCShape> nacelleShells = new ArrayList<>();
		List<OCCShape> nacelleSolids = new ArrayList<>();
		
		OCCExplorer nacelleExp = new OCCExplorer();
		nacelleExp.init(OCCUtils.theFactory.newShape(nacelleShapes), CADShapeTypes.SHELL);
		while (nacelleExp.more()) {
			nacelleShells.add((OCCShape) nacelleExp.current());
			nacelleExp.next();
		}
		System.out.println("Number of shell elements found for the nacelle: " + nacelleShells.size());
		OCCShell intakeShell = (OCCShell) nacelleShells.get(1);
		nacelleShells.remove(1);
		
		OCCShell nacelleShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
				1.0e-03, nacelleShells.stream().map(s -> (CADShape) s).collect(Collectors.toList()));	
		
		OCCSolid nacelleSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(nacelleShell);
		OCCSolid intakeSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells(intakeShell);
		
		nacelleSolids.add(nacelleSolid);
		nacelleSolids.add(intakeSolid);
		
		BRep_Builder nacelleCompoundBuilder = new BRep_Builder();
		TopoDS_Compound nacelleCompound = new TopoDS_Compound();
		nacelleCompoundBuilder.MakeCompound(nacelleCompound);
		
		nacelleSolids.forEach(s -> nacelleCompoundBuilder.Add(nacelleCompound, s.getShape()));
		
		// Get the NACELLE right orientation in space
		gp_Pnt nacelleCG = getCG(nacelleCompound);
		
		double nacelleRotAngle = 1.5708;

		gp_Trsf nacelleRotation = new gp_Trsf();
		gp_Ax1 nacelleRotAxis = new gp_Ax1(nacelleCG, new gp_Dir(0.0, 0.0, -1.0));
		nacelleRotation.SetRotation(nacelleRotAxis, nacelleRotAngle);
		
		OCCShape rotatedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_Transform(nacelleCompound, nacelleRotation, 0).Shape()
						));	
		
		// ----------------------------------------------------
		// Get NACELLE reference dimensions, points, and edges
		// ----------------------------------------------------
		double[] nacelleBoundBox = rotatedNacelle.boundingBox();
		double refNacelleLength = nacelleBoundBox[3] - nacelleBoundBox[0];
		double refNacelleWidth = nacelleBoundBox[4] - nacelleBoundBox[1];
		double refNacelleHeight = nacelleBoundBox[5] - nacelleBoundBox[2];
		
		System.out.println("Nacelle reference dimensions:");
		System.out.println("Lenght = " + refNacelleLength);
		System.out.println("Width = " + refNacelleWidth);
		System.out.println("Height = " + refNacelleHeight);
		
		// ----------------------------
		// Explore the BLADE shapes
		// ----------------------------
		List<OCCShape> bladeShells = new ArrayList<>();
		List<OCCSolid> bladeSolids = new ArrayList<>();
		
		OCCExplorer bladeExp = new OCCExplorer();
		bladeExp.init(OCCUtils.theFactory.newShape(bladeShapes), CADShapeTypes.SHELL);
		while (bladeExp.more()) {
			bladeShells.add((OCCShape) bladeExp.current());
			bladeExp.next();
		}
		System.out.println("Number of shell elements found for the blade: " + bladeShells.size());
		
		List<OCCWire> bladeTipFreeWires = new ArrayList<>();
		TopoDS_Compound bladeTipFBs = new ShapeAnalysis_FreeBounds(bladeShells.get(4).getShape()).GetClosedWires();
		TopExp_Explorer bladeTipFBExp = new TopExp_Explorer();
		bladeTipFBExp.Init(bladeTipFBs, TopAbs_ShapeEnum.TopAbs_WIRE);
		while (bladeTipFBExp.More() > 0) {
			bladeTipFreeWires.add((OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(bladeTipFBExp.Current())));
			bladeTipFBExp.Next();
		}
		System.out.println("Number of closed free wires found for the blade tip element: " + bladeTipFreeWires.size());
		
		OCCShape bladeTipClosingFace = (OCCShape) OCCUtils.theFactory.newFacePlanar(bladeTipFreeWires.get(1));
		OCCShape bladeTipClosed = (OCCShape) OCCUtils.theFactory.newShellFromAdjacentShapes(
				1.0e-06, bladeTipClosingFace, bladeShells.get(4));
		
		bladeShells.remove(4);
		bladeShells.add(bladeTipClosed);
		
		OCCShell openBladeShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
				1.0e-03, bladeShells.stream().map(s -> (CADShape) s).collect(Collectors.toList()));
		
		List<OCCWire> bladeFreeWires = new ArrayList<>();
		TopoDS_Compound bladeFBs = new ShapeAnalysis_FreeBounds(openBladeShell.getShape()).GetClosedWires();
		TopExp_Explorer bladeFBExp = new TopExp_Explorer();
		bladeFBExp.Init(bladeFBs, TopAbs_ShapeEnum.TopAbs_WIRE);
		while (bladeFBExp.More() > 0) {
			bladeFreeWires.add((OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(bladeFBExp.Current())));
			bladeFBExp.Next();
		}
		System.out.println("Number of closed free wires found for the blade element: " + bladeFreeWires.size());
		
		OCCShape bladeClosingFace = (OCCShape) OCCUtils.theFactory.newFacePlanar(bladeFreeWires.get(0));
		OCCShape bladeShell = (OCCShape) OCCUtils.theFactory.newShellFromAdjacentShapes(
				1.0e-06, bladeClosingFace, openBladeShell);
		
		OCCSolid bladeSolid = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells((CADShell) bladeShell);
		
		bladeSolids.add(bladeSolid);
		
		// Get the BLADE correct orientation in space
		gp_Pnt bladeClosingFaceCG = getCG(bladeClosingFace.getShape());
		
		double[] uMin = new double[1];
		double[] uMax = new double[1];
		double[] vMin = new double[1];
		double[] vMax = new double[1];	
		BRepTools.UVBounds(TopoDS.ToFace(bladeClosingFace.getShape()), uMin, uMax, vMin, vMax);
		
		Geom_Surface bladeClosingSurface = BRep_Tool.Surface(TopoDS.ToFace(bladeClosingFace.getShape()));
		GeomLProp_SLProps bladeClosingSurfaceProps = new GeomLProp_SLProps(
				bladeClosingSurface,
				(uMax[0] - uMin[0])/2,
				(vMax[0] - vMin[0])/2,
				1, 0.01);
		gp_Dir bladeClosingSurfaceNormal = bladeClosingSurfaceProps.Normal();
		
		System.out.println("Blade base orientation: [ " + 
				bladeClosingSurfaceNormal.X() + ", " + 
				bladeClosingSurfaceNormal.Y() + ", " + 
				bladeClosingSurfaceNormal.Z() + "]"
				);	
		
		double bladeRotAngle1 = bladeClosingSurfaceNormal.Angle(zDir);
		System.out.println("Blade first rotation angle (deg): " + bladeRotAngle1 * 57.3);
		
		gp_Trsf bladeRotation1 = new gp_Trsf();
		gp_Ax1 bladeRotAxis1 = new gp_Ax1(bladeClosingFaceCG, yDir);
		bladeRotation1.SetRotation(bladeRotAxis1, bladeRotAngle1);
		
		OCCShape rotatedBlade1 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToSolid(
						new BRepBuilderAPI_Transform(bladeSolid.getShape(), bladeRotation1, 0).Shape()
						));	
		
		double bladeRotAngle2 = 1.5708;
		
		gp_Trsf bladeRotation2 = new gp_Trsf();
		gp_Ax1 bladeRotAxis2 = new gp_Ax1(bladeClosingFaceCG, zDir);
		bladeRotation2.SetRotation(bladeRotAxis2, bladeRotAngle2);
		
		OCCShape rotatedBlade2 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToSolid(
						new BRepBuilderAPI_Transform(rotatedBlade1.getShape(), bladeRotation2, 0).Shape()
						));	
		
		// ----------------------------
		// Test the BLADE mirroring 
		// ---------------------------- 
//		int numOfBlades = 4;
//		double[] rotVec = MyArrayUtils.linspace(0, 2*Math.PI, numOfBlades + 1);
//		
//		System.out.println("Blades rotation angles (deg): " + Arrays.toString(rotVec));
//		
//		gp_Trsf axialMirror = new gp_Trsf();	
//		gp_Ax1 symmAxis = new gp_Ax1(bladeClosingFaceCG, xDir);
//		
//		List<OCCShape> rotatedBlades = new ArrayList<>();
//		rotatedBlades.add(rotatedBlade2);
//		for (int i = 0; i < numOfBlades; i++) {
//			axialMirror.SetRotation(symmAxis, rotVec[i]);
//			rotatedBlades.add((OCCShape) OCCUtils.theFactory.newShape(
//					TopoDS.ToSolid(
//							new BRepBuilderAPI_Transform(rotatedBlade2.getShape(), axialMirror, 1).Shape())
//					));
//		}
		
		// ----------------------------
		// Generate the BLADES cap
		// ----------------------------
		
		
//		// Rotate the NACELLE
//		TopTools_IndexedMapOfShape nacelleMapOfShells = new TopTools_IndexedMapOfShape();
//		TopExp.MapShapes(nacelleShapes, TopAbs_ShapeEnum.TopAbs_SHELL, nacelleMapOfShells);
//		
//		System.out.println("Number of shell entities found: " + nacelleMapOfShells.Size());
//		
//		TopoDS_Shell mainNacelleShell = TopoDS.ToShell(nacelleMapOfShells.FindKey(3));
//		ShapeAnalysis_FreeBounds safb = new ShapeAnalysis_FreeBounds(mainNacelleShell);
//		TopoDS_Compound mainNacelleFreeBounds = safb.GetClosedWires();
//		
//		TopExp_Explorer mainNacelleExp = new TopExp_Explorer();
//		mainNacelleExp.Init(mainNacelleFreeBounds, TopAbs_ShapeEnum.TopAbs_WIRE);
//		List<TopoDS_Wire> mainNacelleWires = new ArrayList<>();
//		while (mainNacelleExp.More() > 0) {
//			mainNacelleWires.add(TopoDS.ToWire(mainNacelleExp.Current()));
//			mainNacelleExp.Next();
//		}
//		
//		System.out.println("Number of freebounds found for the nacelle: " + mainNacelleWires.size());
//		
//		OCCWire frontWire = (OCCWire) OCCUtils.theFactory.newShape(mainNacelleWires.get(2));	 
//		TopoDS_Face frontWireFace = ((OCCFace) OCCUtils.theFactory.newFacePlanar(frontWire)).getShape();
//		
//		double[] uMin = new double[1];
//		double[] uMax = new double[1];
//		double[] vMin = new double[1];
//		double[] vMax = new double[1];	
//		BRepTools.UVBounds(frontWireFace, uMin, uMax, vMin, vMax);
//		
//		GProp_GProps gProp = new GProp_GProps();
//		BRepGProp.LinearProperties(frontWireFace, gProp);	
//		gp_Pnt cg = gProp.CentreOfMass();	
//		
//		Geom_Surface frontWireSurface = BRep_Tool.Surface(frontWireFace);
//		GeomLProp_SLProps fronWireFaceProps = new GeomLProp_SLProps(
//				frontWireSurface,
//				(uMax[0] - uMin[0])/2,
//				(vMax[0] - vMin[0])/2,
//				1, 0.01);
//		
//		gp_Dir frontWireFaceNormal = fronWireFaceProps.Normal();
//		double nacelleRotAngle = frontWireFaceNormal.Angle(new gp_Dir(1.0, 0.0, 0.0));
//		System.out.println("Nacelle tilt angle (deg): " + nacelleRotAngle*57.3);
//		
//		System.out.println("Front wire face normal array: " + 
//				Arrays.toString(new double[] {
//						frontWireFaceNormal.X(), 
//						frontWireFaceNormal.Y(), 
//						frontWireFaceNormal.Z()
//						}));
//		
//		gp_Pnt normalVtx = cg.Translated(new gp_Vec(frontWireFaceNormal));
//		
//		List<gp_Pnt> normalPts = new ArrayList<>();
//		normalPts.add(cg);
//		normalPts.add(normalVtx);
//		
//		gp_Pnt nacelleCG = getCG(nacelleShapes);
//		gp_Trsf rotation = new gp_Trsf();
//		gp_Ax1 nacelleRotAxis = new gp_Ax1(
//				nacelleCG,
//				new gp_Dir(0.0, 1.0, 0.0)
//				);
//		rotation.SetRotation(nacelleRotAxis, nacelleRotAngle);
//		
//		OCCShape rotatedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_Transform(nacelleShapes, rotation, 0).Shape()
//						));	
//		
//		// ------------------------------
//		// Testing stretching functions
//		// ------------------------------
//		long start = System.currentTimeMillis();    
//		
//		gp_GTrsf stretchingX = new gp_GTrsf();	
//		gp_GTrsf stretchingY = new gp_GTrsf();
//		gp_GTrsf stretchingZ = new gp_GTrsf();
//		
//		gp_Dir xDir = new gp_Dir(1.0, 0.0, 0.0);
//		gp_Ax2 xStretchingRS = new gp_Ax2(nacelleCG, xDir);
//		stretchingX.SetAffinity(xStretchingRS, 1.00);
//		
//		OCCShape xStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_GTransform(rotatedNacelle.getShape(), stretchingX, 0).Shape()
//						));
//		
//		gp_Dir yDir = new gp_Dir(0.0, 1.0, 0.0);
//		gp_Dir zDir = new gp_Dir(0.0, 0.0, 1.0);
//		gp_Ax2 yStretchingRS = new gp_Ax2(nacelleCG, yDir);
//		gp_Ax2 zStretchingRS = new gp_Ax2(nacelleCG, zDir);
//		stretchingY.SetAffinity(yStretchingRS, 1.00);
//		stretchingZ.SetAffinity(zStretchingRS, 1.00);
//		
//		OCCShape yStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_GTransform(xStretchedNacelle.getShape(), stretchingY, 0).Shape()
//						));
//		OCCShape zStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
//				TopoDS.ToCompound(
//						new BRepBuilderAPI_GTransform(yStretchedNacelle.getShape(), stretchingZ, 0).Shape()
//						));
//		
//		long elapsedTime = System.currentTimeMillis() - start;
//		
//		System.out.println("Stretching operations elapsed time (seconds): " + elapsedTime/1.0e03);
//		exportShapes.add(zStretchedNacelle);
				
		// Export to file
		OCCUtils.write("Test37mds", FileExtension.STEP, exportShapes);
	}
	
	private static gp_Pnt getCG(TopoDS_Shape shape) {

		GProp_GProps gProp = new GProp_GProps();
		BRepGProp.LinearProperties(shape, gProp);	
		gp_Pnt cg = gProp.CentreOfMass();	

		return cg;
	}
	
}

package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.java_cup.internal.runtime.Symbol;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.CADWire;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCFace;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCSolid;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcad.occ.OCCWire;
import opencascade.BRepAlgoAPI_Cut;
import opencascade.BRepBndLib;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_MakeFace;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepFilletAPI_MakeFillet;
import opencascade.BRepGProp;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.BRepPrimAPI_MakeBox;
import opencascade.BRepPrimAPI_MakePrism;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.BRep_Tool;
import opencascade.Bnd_Box;
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
		
		// -------------------
		// Set the factory
		// -------------------
		if (OCCUtils.theFactory == null) {
			OCCUtils.initCADShapeFactory();
		}
		
		List<OCCShape> tpNacelleShapes = new ArrayList<>();
		List<OCCShape> tpBladeShapes = new ArrayList<>();
			
		gp_Dir yDir = new gp_Dir(0.0, 1.0, 0.0);
		gp_Dir zDir = new gp_Dir(0.0, 0.0, 1.0);
		
		// -------------------------
		// Import necessary parts
		// -------------------------
		
		// NACELLE
		STEPControl_Reader nacelleReader = new STEPControl_Reader();	
		nacelleReader.ReadFile("C:\\Users\\Mario\\Desktop\\Turboprop_parts\\nacelle_01.step");
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		System.out.println("Nacelle STEP reader problems:");
		nacelleReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		nacelleReader.TransferRoots();
		TopoDS_Shape nacelleShapes = nacelleReader.OneShape();
		
		System.out.println("Nacelle imported shapes type: " + nacelleShapes.ShapeType().toString());
		
		// BLADE
		STEPControl_Reader bladeReader = new STEPControl_Reader();	
		bladeReader.ReadFile("C:\\Users\\Mario\\Desktop\\Turboprop_parts\\blade_01.step");
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		System.out.println("Blade STEP reader problems:");
		bladeReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		bladeReader.TransferRoots();
		TopoDS_Shape bladeShapes = bladeReader.OneShape();
		
		System.out.println("Blade imported shapes type: " + bladeShapes.ShapeType().toString());
		
		// ----------------------------
		// Explore the NACELLE shapes
		// ----------------------------
		List<OCCShape> nacelleShells = new ArrayList<>();
		
		OCCExplorer nacelleExp = new OCCExplorer();
		nacelleExp.init(OCCUtils.theFactory.newShape(nacelleShapes), CADShapeTypes.SHELL);
		while (nacelleExp.more()) {
			nacelleShells.add((OCCShape) nacelleExp.current());
			nacelleExp.next();
		}
		System.out.println("Number of shell elements found for the nacelle: " + nacelleShells.size());
		
		OCCShell nacelleShell = (OCCShell) OCCUtils.theFactory.newShellFromAdjacentShapes(
				1.0e-03, nacelleShells.stream().map(s -> (CADShape) s).collect(Collectors.toList()));	
		
		// Get the NACELLE right orientation in space
		double nacelleRotAngle = 1.5708;

		gp_Trsf nacelleRotation = new gp_Trsf();
		gp_Ax1 nacelleRotAxis = new gp_Ax1(OCCUtils.getShapeCG(nacelleShell), new gp_Dir(0.0, 0.0, -1.0));
		nacelleRotation.SetRotation(nacelleRotAxis, nacelleRotAngle);
		
		OCCShape rotatedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToShell(
						new BRepBuilderAPI_Transform(nacelleShell.getShape(), nacelleRotation, 0).Shape()
						));	
		
		// ------------------------------------------------------------
		// Translate the NACELLE to the origin of the reference system
		// ------------------------------------------------------------
		
		// Get the free boundary at the tip
		ShapeAnalysis_FreeBounds nacelleFBAnalyzer0 = new ShapeAnalysis_FreeBounds(rotatedNacelle.getShape());
		TopExp_Explorer nacelleFreeWiresExp0 = new TopExp_Explorer(
				nacelleFBAnalyzer0.GetClosedWires(), TopAbs_ShapeEnum.TopAbs_WIRE);
		
		List<OCCWire> nacelleFreeWires0 = new ArrayList<>();
		while (nacelleFreeWiresExp0.More() > 0) {
			nacelleFreeWires0.add((OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(nacelleFreeWiresExp0.Current())));
			nacelleFreeWiresExp0.Next();
		}
		System.out.println("Number of free boundary closed wires found for the nacelle: " + nacelleFreeWires0.size());
		
		List<OCCWire> sortedNacelleFreeWires0 = nacelleFreeWires0.stream()
				.sorted(Comparator.comparing(w -> w.vertices().get(0).pnt()[0]))
				.collect(Collectors.toList());		
		
		OCCWire nacelleTipWire0 = sortedNacelleFreeWires0.get(0);
		gp_Pnt nacelleTipWireCG0 = OCCUtils.getShapeCG(nacelleTipWire0);
		
		// Translate the NACELLE
		gp_Trsf nacelleTranslate = new gp_Trsf();
		nacelleTranslate.SetTranslation(nacelleTipWireCG0, new gp_Pnt(0.0, 0.0, 0.0));
		
		OCCShape translatedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToShell(
						new BRepBuilderAPI_Transform(rotatedNacelle.getShape(), nacelleTranslate, 0).Shape()
						));
		
		// ----------------------------------------------------
		// Get NACELLE reference dimensions, points, and edges
		// ----------------------------------------------------
		
		// Get NACELLE max dimensions
		Bnd_Box nacelleBB = new Bnd_Box();
		new BRepMesh_IncrementalMesh(translatedNacelle.getShape(), 7.0e-03);
		BRepBndLib.Add(translatedNacelle.getShape(), nacelleBB, 1);
		nacelleBB.SetGap(0.0);
		double[] xMin = new double[1];
		double[] yMin = new double[1]; 
		double[] zMin = new double[1];
		double[] xMax = new double[1];
		double[] yMax = new double[1];
		double[] zMax = new double[1];
		nacelleBB.Get(xMin, yMin, zMin, xMax, yMax, zMax);
		
		System.out.println("Nacelle bounding box coordinates:");
		System.out.println("X min = " + xMin[0] + ", X max = " + xMax[0]);
		System.out.println("Y min = " + yMin[0] + ", Y max = " + yMax[0]);
		System.out.println("Z min = " + zMin[0] + ", Z max = " + zMax[0]);
		
		double refNacelleLength = xMax[0] - xMin[0];
		double refNacelleWidth = yMax[0] - yMin[0];
		double refNacelleHeight = zMax[0] - zMin[0];
		
		System.out.println("Nacelle reference dimensions:");
		System.out.println("Lenght = " + refNacelleLength);
		System.out.println("Width = " + refNacelleWidth);
		System.out.println("Height = " + refNacelleHeight);
		
		// Get NACELLE reference free bounds	
		ShapeAnalysis_FreeBounds nacelleFBAnalyzer = new ShapeAnalysis_FreeBounds(translatedNacelle.getShape());
		TopExp_Explorer nacelleFreeWiresExp = new TopExp_Explorer(
				nacelleFBAnalyzer.GetClosedWires(), TopAbs_ShapeEnum.TopAbs_WIRE);
		
		List<OCCWire> nacelleFreeWires = new ArrayList<>();
		while (nacelleFreeWiresExp.More() > 0) {
			nacelleFreeWires.add((OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(nacelleFreeWiresExp.Current())));
			nacelleFreeWiresExp.Next();
		}
		
		List<OCCWire> sortedNacelleFreeWires = nacelleFreeWires.stream()
				.sorted(Comparator.comparing(w -> w.vertices().get(0).pnt()[0]))
				.collect(Collectors.toList());		
		
		OCCWire nacelleTipWire = sortedNacelleFreeWires.get(0);		
		OCCWire nacelleIntakeWire = sortedNacelleFreeWires.get(1);	
		OCCWire nacelleRearWire = sortedNacelleFreeWires.get(2);	
		
		gp_Pnt nacelleTipWireCG = OCCUtils.getShapeCG(nacelleTipWire);
		gp_Pnt nacelleIntakeWireCG = OCCUtils.getShapeCG(nacelleIntakeWire);
		gp_Pnt nacelleRearWireCG = OCCUtils.getShapeCG(nacelleRearWire); 
		
		// Get the hub diameter
		List<CADVertex> zSortedTipWireVtx = nacelleTipWire.vertices().stream()
				.sorted(Comparator.comparing(v -> v.pnt()[2]))
				.collect(Collectors.toList());	
		
		double hubWireDiameter = zSortedTipWireVtx.get(nacelleTipWire.vertices().size() - 1).pnt()[2] - 
								  zSortedTipWireVtx.get(0).pnt()[2];
		
		System.out.println("Hub wire diameter: " + hubWireDiameter);
		
		// ----------------------------
		// Generate the propeller hub
		// ----------------------------
		double propHubLength = refNacelleLength / 25;
		
		gp_Vec hubVec = new gp_Vec(-1.0 * propHubLength, 0.0, 0.0);
		BRepPrimAPI_MakePrism propellerHub = new BRepPrimAPI_MakePrism(nacelleTipWire.getShape(), hubVec, 0, 0);
		propellerHub.Build();
		OCCShell hubShell = (OCCShell) OCCUtils.theFactory.newShape(TopoDS.ToShell(propellerHub.Shape()));
		
		OCCWire hubLastWire = (OCCWire) OCCUtils.theFactory.newShape(TopoDS.ToWire(propellerHub.LastShape()));
		gp_Pnt hubLastWireCG = OCCUtils.getShapeCG(hubLastWire);
		
		// ----------------------------
		// Generate the propeller cap
		// ----------------------------
		double propCapLength = refNacelleLength / 12;
		
		// Translate scaled shapes in order to generate the supporting shapes for the cap
		gp_Trsf hubWireTrans = new gp_Trsf();
		
		hubWireTrans.SetScale(hubLastWireCG, 0.85);	
		OCCWire propCapScaledSuppWire1 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(hubLastWire.getShape(), hubWireTrans, 0).Shape()
						));
		hubWireTrans.SetTranslation(hubLastWireCG, new gp_Pnt(-(propCapLength*3/10 + propHubLength), 0.0, 0.0));
		OCCWire propCapSuppWire1 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(propCapScaledSuppWire1.getShape(), hubWireTrans, 0).Shape()
						));
		
		hubWireTrans.SetScale(hubLastWireCG, 0.60);	
		OCCWire propCapScaledSuppWire2 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(hubLastWire.getShape(), hubWireTrans, 0).Shape()
						));	
		hubWireTrans.SetTranslation(hubLastWireCG, new gp_Pnt(-(propCapLength*6/10 + propHubLength), 0.0, 0.0));
		OCCWire propCapSuppWire2 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(propCapScaledSuppWire2.getShape(), hubWireTrans, 0).Shape()
						));	
		
		CADVertex propCapTipVtx = OCCUtils.theFactory.newVertex(new double[] {-(propCapLength + propHubLength), 0.0, 0.0});	
		
		// Generate the shell for the cap
		List<CADWire> propCapWires = new ArrayList<>();
		propCapWires.add(hubLastWire);
		propCapWires.add(propCapSuppWire1);
		propCapWires.add(propCapSuppWire2);
		OCCShape capShell = OCCUtils.makePatchThruSections(propCapWires, propCapTipVtx);
		
		// ---------------------------------
		// Generate the NACELLE outlet cap
		// ---------------------------------
		double outletCapLength = refNacelleLength / 7;
		
		// Translate scaled shapes in order to generate the supporting shapes for the cap
		gp_Trsf outletWireTrans = new gp_Trsf();
		
		outletWireTrans.SetScale(nacelleRearWireCG, 0.85);	
		OCCWire outletCapScaledSuppWire1 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleRearWire.getShape(), outletWireTrans, 0).Shape()
						));
		outletWireTrans.SetTranslation(nacelleRearWireCG, new gp_Pnt(nacelleRearWireCG.X() + outletCapLength*3/10, 0.0, nacelleRearWireCG.Z()));
		OCCWire outletCapSuppWire1 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(outletCapScaledSuppWire1.getShape(), outletWireTrans, 0).Shape()
						));
		
		outletWireTrans.SetScale(nacelleRearWireCG, 0.60);	
		OCCWire outletCapScaledSuppWire2 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleRearWire.getShape(), outletWireTrans, 0).Shape()
						));	
		outletWireTrans.SetTranslation(nacelleRearWireCG, new gp_Pnt(nacelleRearWireCG.X() + outletCapLength*6/10, 0.0, nacelleRearWireCG.Z()));
		OCCWire outletCapSuppWire2 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(outletCapScaledSuppWire2.getShape(), outletWireTrans, 0).Shape()
						));	
		
		CADVertex outletCapTipVtx = OCCUtils.theFactory.newVertex(new double[] {
				nacelleRearWireCG.X() + outletCapLength, 0.0, nacelleRearWireCG.Z()});	
		
		// Generate the shell for the cap
		List<CADWire> outletCapWires = new ArrayList<>();	
		outletCapWires.add(nacelleRearWire);
		outletCapWires.add(outletCapSuppWire1);
		outletCapWires.add(outletCapSuppWire2);
		OCCShape outletShell = OCCUtils.makePatchThruSections(outletCapWires, outletCapTipVtx);
		
		// -------------------
		// Close the NACELLE 
		// -------------------
		OCCShape nacelleIntakeFace = (OCCFace) OCCUtils.makeFilledFace(nacelleIntakeWire.edges(), new ArrayList<>());
		
		OCCShape nacelleShellNoDuct = (OCCShape) OCCUtils.theFactory.newShellFromAdjacentShapes(1.0e-05,
				(CADShape) translatedNacelle,
				(CADShape) hubShell,
				(CADShape) capShell,
				(CADShape) outletShell,
				(CADShape) nacelleIntakeFace
				);
		
		OCCShape nacelleSolidNoDuct = (OCCShape) OCCUtils.theFactory.newSolidFromAdjacentShells((CADShell) nacelleShellNoDuct);
		
		// -------------------------
		// Generate the inner duct
		// -------------------------
		
		// Generate the inner intake wire
		System.out.println("Number of vertices found for the intake wire: " + nacelleIntakeWire.vertices().size());
		
		List<CADVertex> zSortedIntakeWireVtx = nacelleIntakeWire.vertices().stream()
				.sorted(Comparator.comparing(v -> v.pnt()[2]))
				.collect(Collectors.toList());		
		List<CADVertex> ySortedIntakeWireVtx = nacelleIntakeWire.vertices().stream()
				.sorted(Comparator.comparing(v -> v.pnt()[1]))
				.collect(Collectors.toList());
		
		double intakeWireWidth = ySortedIntakeWireVtx.get(nacelleIntakeWire.vertices().size() - 1).pnt()[1] - 
								 ySortedIntakeWireVtx.get(0).pnt()[1];
		double intakeWireHeight = zSortedIntakeWireVtx.get(nacelleIntakeWire.vertices().size() - 1).pnt()[2] - 
				 				  zSortedIntakeWireVtx.get(0).pnt()[2];
		
		System.out.println("Intake wire width: " + intakeWireWidth);
		System.out.println("Intake wire height: " + intakeWireHeight);
		
		gp_GTrsf stretchingZ = new gp_GTrsf();
		gp_Ax2 zStretchingRS = new gp_Ax2(nacelleIntakeWireCG, zDir);
		
		stretchingZ.SetAffinity(zStretchingRS, 0.85);
		OCCWire nacelleIntakeZStretchedWire = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_GTransform(nacelleIntakeWire.getShape(), stretchingZ, 0).Shape()
						));
		
		List<CADVertex> zSortedIntakeZStretchedWireVtx = nacelleIntakeZStretchedWire.vertices().stream()
				.sorted(Comparator.comparing(v -> v.pnt()[2]))
				.collect(Collectors.toList());
		
		double intakeZStretchedWireHeight = zSortedIntakeZStretchedWireVtx.get(nacelleIntakeZStretchedWire.vertices().size() - 1).pnt()[2] - 
											zSortedIntakeZStretchedWireVtx.get(0).pnt()[2];	
		double intakeZStretchedWireOffset = (intakeWireHeight - intakeZStretchedWireHeight)/2;
		
		gp_GTrsf stretchingY = new gp_GTrsf();
		gp_Ax2 yStretchingRS = new gp_Ax2(nacelleIntakeWireCG, yDir);
		
		double yStretchingFactor = ((ySortedIntakeWireVtx.get(nacelleIntakeWire.vertices().size() - 1).pnt()[1] - intakeZStretchedWireOffset) - 
								    (ySortedIntakeWireVtx.get(0).pnt()[1] + intakeZStretchedWireOffset))/intakeWireWidth;
		stretchingY.SetAffinity(yStretchingRS, yStretchingFactor);
		
		OCCWire nacelleIntakeStretchedWire = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_GTransform(nacelleIntakeZStretchedWire.getShape(), stretchingY, 0).Shape()
						));
		
		// Translate scaled shapes in order to generate the supporting shapes for the inner duct
		gp_Trsf innerDuctWireTrans = new gp_Trsf();
		
		innerDuctWireTrans.SetTranslation(nacelleIntakeWireCG, 
				new gp_Pnt(nacelleIntakeWireCG.X() - refNacelleLength/20, 0.0, nacelleIntakeWireCG.Z()));
		OCCWire innerDuctSuppWire1 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleIntakeStretchedWire.getShape(), innerDuctWireTrans, 0).Shape()
						));
		
		innerDuctWireTrans.SetTranslation(nacelleIntakeWireCG, 
				new gp_Pnt(nacelleIntakeWireCG.X() + 2*refNacelleLength/5, 0.0, nacelleIntakeWireCG.Z()));
		OCCWire innerDuctSuppWire2 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleIntakeStretchedWire.getShape(), innerDuctWireTrans, 0).Shape()
						));
		
		innerDuctWireTrans.SetScale(nacelleIntakeWireCG, 1.15);
		OCCWire innerDuctScaledSuppWire3 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleIntakeStretchedWire.getShape(), innerDuctWireTrans, 0).Shape()
						));
		innerDuctWireTrans.SetTranslation(nacelleIntakeWireCG, 
				new gp_Pnt(nacelleIntakeWireCG.X() + 3*refNacelleLength/5, 0.0, nacelleIntakeWireCG.Z() - refNacelleHeight/20));
		OCCWire innerDuctSuppWire3 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(innerDuctScaledSuppWire3.getShape(), innerDuctWireTrans, 0).Shape()
						));
		
		innerDuctWireTrans.SetScale(nacelleIntakeWireCG, 1.30);
		OCCWire innerDuctScaledSuppWire4 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleIntakeStretchedWire.getShape(), innerDuctWireTrans, 0).Shape()
						));
		innerDuctWireTrans.SetTranslation(nacelleIntakeWireCG, 
				new gp_Pnt(nacelleIntakeWireCG.X() + 4*refNacelleLength/5, 0.0, nacelleIntakeWireCG.Z() - refNacelleHeight/7));
		OCCWire innerDuctSuppWire4 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(innerDuctScaledSuppWire4.getShape(), innerDuctWireTrans, 0).Shape()
						));
		
		innerDuctWireTrans.SetScale(nacelleIntakeWireCG, 1.45);
		OCCWire innerDuctScaledSuppWire5 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(nacelleIntakeStretchedWire.getShape(), innerDuctWireTrans, 0).Shape()
						));
		innerDuctWireTrans.SetTranslation(nacelleIntakeWireCG, 
				new gp_Pnt(nacelleIntakeWireCG.X() + refNacelleLength, 0.0, nacelleIntakeWireCG.Z() - refNacelleHeight/3));
		OCCWire innerDuctSuppWire5 = (OCCWire) OCCUtils.theFactory.newShape(
				TopoDS.ToWire(
						new BRepBuilderAPI_Transform(innerDuctScaledSuppWire5.getShape(), innerDuctWireTrans, 0).Shape()
						));
		
		// Generate the shell for the inner duct
		List<CADWire> innerDuctWires = new ArrayList<>();	
		innerDuctWires.add(innerDuctSuppWire1);
		innerDuctWires.add(nacelleIntakeStretchedWire);
		innerDuctWires.add(innerDuctSuppWire2);
		innerDuctWires.add(innerDuctSuppWire3);
		innerDuctWires.add(innerDuctSuppWire4);
		innerDuctWires.add(innerDuctSuppWire5);
		OCCShape innerDuctShell = OCCUtils.makePatchThruSections(innerDuctWires);
		
		// ---------------------------
		// Close the inner duct shell
		// ---------------------------
		OCCShape frontInnerDuctFace = (OCCShape) OCCUtils.makeFilledFace(innerDuctSuppWire1.edges(), new ArrayList<>());		 
		OCCShape rearInnerDuctFace = (OCCShape) OCCUtils.makeFilledFace(innerDuctSuppWire5.edges(), new ArrayList<>());
		
		OCCShape innerDuctClosedShell = (OCCShape) OCCUtils.theFactory.newShellFromAdjacentShapes(1.0e-06,
				(CADShape) innerDuctShell,
				(CADShape) frontInnerDuctFace,
				(CADShape) rearInnerDuctFace
				);
		
		OCCShape innerDuctClosedSolid = (OCCShape) OCCUtils.theFactory.newSolidFromAdjacentShells((CADShell) innerDuctClosedShell);
		
		// -----------------------------------------------------------
		// Generate the final NACELLE by means of a boolean operation
		// -----------------------------------------------------------
		BRepAlgoAPI_Cut cutter = new BRepAlgoAPI_Cut(
				innerDuctClosedSolid.getShape(),
				nacelleSolidNoDuct.getShape()		
				);
		
		OCCShape innerDuctNacelle = (OCCShape) OCCUtils.theFactory.newShape(cutter.Shape());
		
		// ------------------------------
		// Apply a fillet to the intake
		// ------------------------------

		// Explore the nacelle shapes in search of intake edges
		TopExp_Explorer nacelleEdgeExp = new TopExp_Explorer();
		nacelleEdgeExp.Init(innerDuctNacelle.getShape(), TopAbs_ShapeEnum.TopAbs_EDGE);
		
		List<OCCEdge> nacelleEdges = new ArrayList<>();
		while (nacelleEdgeExp.More() > 0) {
			nacelleEdges.add((OCCEdge) OCCUtils.theFactory.newShape(TopoDS.ToEdge(nacelleEdgeExp.Current())));
			nacelleEdgeExp.Next();
		}
		
		List<OCCEdge> intakeInnerEdges = new ArrayList<>();
		List<OCCEdge> intakeOuterEdges = new ArrayList<>();
		
		intakeInnerEdges.add(nacelleEdges.get(5));
		intakeInnerEdges.add(nacelleEdges.get(6));
		intakeInnerEdges.add(nacelleEdges.get(41));
		intakeInnerEdges.add(nacelleEdges.get(35));
		
		intakeOuterEdges.add(nacelleEdges.get(36));
		intakeOuterEdges.add(nacelleEdges.get(37));
		intakeOuterEdges.add(nacelleEdges.get(39));
		intakeOuterEdges.add(nacelleEdges.get(38));
		
		// Generate the fillet
		double intakeFilletRadius = 3*intakeZStretchedWireOffset/7;
		
		BRepFilletAPI_MakeFillet filletMaker1 = new BRepFilletAPI_MakeFillet(innerDuctNacelle.getShape());
		intakeInnerEdges.forEach(e -> filletMaker1.Add(intakeFilletRadius, e.getShape()));	
		filletMaker1.Build();
		
		OCCShape nacelleFilleted0 = (OCCShape) OCCUtils.theFactory.newShape(filletMaker1.Shape());
		
		BRepFilletAPI_MakeFillet filletMaker2 = new BRepFilletAPI_MakeFillet(nacelleFilleted0.getShape());
		intakeOuterEdges.forEach(e -> filletMaker2.Add(intakeFilletRadius, e.getShape()));	
		filletMaker1.Build();
		
		// Explore the filleted shape, in search of solids
		TopExp_Explorer filletExp = new TopExp_Explorer();
		filletExp.Init(filletMaker2.Shape(), TopAbs_ShapeEnum.TopAbs_SOLID);
		
		OCCSolid solidNacelle0 = (OCCSolid) OCCUtils.theFactory.newShape(TopoDS.ToSolid(filletExp.Current()));	
		
		// ------------------
		// Final translation
		// ------------------
		gp_Trsf finalTranslate = new gp_Trsf();
		finalTranslate.SetTranslation(nacelleTipWireCG, new gp_Pnt(0.0, 0.0, zMin[0] + refNacelleHeight/2));
		
		OCCSolid soliNacelle = (OCCSolid) OCCUtils.theFactory.newShape(
				TopoDS.ToSolid(
						new BRepBuilderAPI_Transform(solidNacelle0.getShape(), finalTranslate, 0).Shape()
						));
		
		tpNacelleShapes.add(soliNacelle);
		
		// ----------------------------
		// Explore the BLADE shapes
		// ----------------------------
		List<OCCShape> bladeShells = new ArrayList<>();
		
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
		
		// -------------------------------------------
		// Get the BLADE correct orientation in space
		// -------------------------------------------
		gp_Pnt bladeClosingFaceCG = OCCUtils.getShapeCG(bladeClosingFace);
		
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
				TopoDS.ToShell(
						new BRepBuilderAPI_Transform(bladeShell.getShape(), bladeRotation1, 0).Shape()
						));	
		
		double bladeRotAngle2 = 1.5708;
		
		gp_Trsf bladeRotation2 = new gp_Trsf();
		gp_Ax1 bladeRotAxis2 = new gp_Ax1(bladeClosingFaceCG, zDir);
		bladeRotation2.SetRotation(bladeRotAxis2, bladeRotAngle2);
		
		OCCShape rotatedBlade2 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToShell(
						new BRepBuilderAPI_Transform(rotatedBlade1.getShape(), bladeRotation2, 0).Shape()
						));	
		
		// ------------------------------------------------------------
		// Translate the NACELLE to the origin of the reference system
		// ------------------------------------------------------------
		gp_Trsf bladeTranslate = new gp_Trsf();
		bladeTranslate.SetTranslation(bladeClosingFaceCG, new gp_Pnt(0.0, 0.0, 0.0));
		
		OCCShape translatedBlade = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToShell(
						new BRepBuilderAPI_Transform(rotatedBlade2.getShape(), bladeTranslate, 0).Shape()
						));
		
		// ----------------------------------------------------
		// Get BLADE reference dimensions, points, and edges
		// ----------------------------------------------------
		Bnd_Box bladeBB = new Bnd_Box();
		new BRepMesh_IncrementalMesh(translatedBlade.getShape(), 7.0e-03);
		BRepBndLib.Add(translatedBlade.getShape(), bladeBB, 1);
		bladeBB.SetGap(0.0);
		double[] xMinB = new double[1];
		double[] yMinB = new double[1]; 
		double[] zMinB = new double[1];
		double[] xMaxB = new double[1];
		double[] yMaxB = new double[1];
		double[] zMaxB = new double[1];
		bladeBB.Get(xMinB, yMinB, zMinB, xMaxB, yMaxB, zMaxB);
		
		System.out.println("Blade bounding box coordinates:");
		System.out.println("X min = " + xMinB[0] + ", X max = " + xMaxB[0]);
		System.out.println("Y min = " + yMinB[0] + ", Y max = " + yMaxB[0]);
		System.out.println("Z min = " + zMinB[0] + ", Z max = " + zMaxB[0]);
		
		double refBladeLength = xMaxB[0] - xMinB[0];
		double refBladeWidth = yMaxB[0] - yMinB[0];
		double refBladeHeight = zMaxB[0] - zMinB[0];
		
		System.out.println("Blade reference dimensions:");
		System.out.println("Lenght = " + refBladeLength);
		System.out.println("Width = " + refBladeWidth);
		System.out.println("Height = " + refBladeHeight);
		
		// --------------------------------
		// Generate the solid of the blade
		// --------------------------------
		OCCSolid solidBlade = (OCCSolid) OCCUtils.theFactory.newSolidFromAdjacentShells((OCCShell) translatedBlade);
		
		tpBladeShapes.add(solidBlade);
		
		// -----------------------
		// Generate the templates
		// -----------------------
		MyConfiguration.setDir(FoldersEnum.OUTPUT_DIR, MyConfiguration.inputDirectory);
		String outputFolderPath = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR) + 
								  "CAD_engine_templates" + File.separator + 
								  "turboprop_templates" + File.separator;

		OCCUtils.write(outputFolderPath + "TP_nacelle_01", FileExtension.STEP, tpNacelleShapes);
		OCCUtils.write(outputFolderPath + "TP_blade_01", FileExtension.STEP, tpBladeShapes);
			
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
		
		// --------------------
		// Import the aircraft
		// --------------------
//		Aircraft aircraft = AircraftUtils.importAircraft(args);
//		
//		Fuselage fuselage = aircraft.getFuselage();
//		LiftingSurface wing = aircraft.getWing();
//		LiftingSurface hTail = aircraft.getHTail();
//		LiftingSurface vTail = aircraft.getVTail();
//		
//		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
//				fuselage, 7, 7, false, false, true);
//		
//		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				wing, WingTipType.ROUNDED, false, false, true);
//		
//		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				hTail, WingTipType.ROUNDED, false, false, true);
//		
//		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				vTail, WingTipType.ROUNDED, false, false, true);
//		
//		exportShapes.addAll(fuselageShapes);
//		exportShapes.addAll(wingShapes);
//		exportShapes.addAll(hTailShapes);
//		exportShapes.addAll(vTailShapes);
	}
	
}

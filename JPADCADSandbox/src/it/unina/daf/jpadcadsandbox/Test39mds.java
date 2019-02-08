package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerplant.Engine;
import aircraft.components.powerplant.PowerPlant;
import configuration.MyConfiguration;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import javafx.application.Application;
import javafx.stage.Stage;
import opencascade.BRepBndLib;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepMesh_IncrementalMesh;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.Bnd_Box;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import opencascade.gp_Vec;

public class Test39mds {
	
	private static final String tpNacelleTemplateName = "TP_nacelle_01.step";
	private static final String tpBladeTemplateName = "TP_blade_01.step";
	
	private static final double tpNacelleTemplateLenght = 12.974006064637578;
	private static final double tpNacelleTemplateWidth = 3.7866430441810013;
	private static final double tpNacelleTemplateHeight = 4.311167784689999;
	
	private static final double tpHubDiameter = 1.3338673091839994;
	
	private static final double tpBladeTemplateLenght = 0.44109813120858654;
	private static final double tpBladeTemplateWidth = 0.7632667546769745;
	private static final double tpBladeTemplateHeight = 4.669436571890001;

	public static void main(String[] args) {
		System.out.println("--------------------------------");
		System.out.println("--- Testing engine templates ---");
		System.out.println("--------------------------------");
		
		gp_Dir xDir = new gp_Dir(1.0, 0.0, 0.0);	
		gp_Dir yDir = new gp_Dir(0.0, 1.0, 0.0);
		gp_Dir zDir = new gp_Dir(0.0, 0.0, 1.0);
		
		List<OCCShape> exportShapes = new ArrayList<>();
		
		// ------------------------
		// Initialize the factory
		// ------------------------
		if (OCCUtils.theFactory == null) 
			OCCUtils.initCADShapeFactory();
				
		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		List<NacelleCreator> nacelles = aircraft.getNacelles().getNacellesList();
		List<Engine> engines = aircraft.getPowerPlant().getEngineList();
		
		// ------------------
		// Import templates
		// ------------------
		MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
		String inputFolderPath = MyConfiguration.getDir(FoldersEnum.INPUT_DIR) + 
				 				 "CAD_engine_templates" + File.separator + 
				                 "turboprop_templates" + File.separator;
		
		// Reading the nacelle
		STEPControl_Reader nacelleReader = new STEPControl_Reader();
		nacelleReader.ReadFile(inputFolderPath + tpNacelleTemplateName);
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		System.out.println("Turboprop nacelle STEP reader problems:");
		nacelleReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		nacelleReader.TransferRoots();
		TopoDS_Shape nacelleShapes = nacelleReader.OneShape();
		
		System.out.println("Nacelle imported shapes type: " + nacelleShapes.ShapeType().toString());
		
		// Reading the blade
		STEPControl_Reader bladeReader = new STEPControl_Reader();
		bladeReader.ReadFile(inputFolderPath + tpBladeTemplateName);
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		System.out.println("Turboprop blade STEP reader problems:");
		bladeReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		bladeReader.TransferRoots();
		TopoDS_Shape bladeShapes = bladeReader.OneShape();
		
		System.out.println("Nacelle imported shapes type: " + bladeShapes.ShapeType().toString());
		
		// --------------------------
		// Iterate through nacelles
		// --------------------------
		gp_Pnt nacelleCG = OCCUtils.getShapeCG((OCCShape) OCCUtils.theFactory.newShape(nacelleShapes));
		
		gp_GTrsf nacelleLengthStretching = new gp_GTrsf();
		gp_GTrsf nacelleHeightStretching = new gp_GTrsf();
		gp_GTrsf nacelleWidthStretching = new gp_GTrsf();
		gp_Trsf nacelleTranslate = new gp_Trsf();	
		
		gp_Ax2 lengthStretchingRS = new gp_Ax2(nacelleCG, xDir);
		gp_Ax2 heightStretchingRS = new gp_Ax2(nacelleCG, zDir);
		gp_Ax2 widthStretchingRS = new gp_Ax2(nacelleCG, yDir);
		
		List<OCCShape> moddedNacelles = new ArrayList<>();	
		for (Iterator<NacelleCreator> iter = nacelles.iterator(); iter.hasNext(); ) {
			NacelleCreator nacelle = iter.next();
			
			BRep_Builder shapeBuilder = new BRep_Builder();
			TopoDS_Compound shapesCompound = new TopoDS_Compound();
			shapeBuilder.MakeCompound(shapesCompound);
			
			shapeBuilder.Add(shapesCompound, ((OCCShape) OCCUtils.theFactory.newVertex(0.0, 0.0, 0.0)).getShape());
			shapeBuilder.Add(shapesCompound, nacelleShapes);
			
			double xPosition = nacelle.getXApexConstructionAxes().doubleValue(SI.METER);
			double yPosition = nacelle.getYApexConstructionAxes().doubleValue(SI.METER);
			double zPosition = nacelle.getZApexConstructionAxes().doubleValue(SI.METER);
			
			double nacelleLength = nacelle.getLength().doubleValue(SI.METER);
			double nacelleMaxDiameter = nacelle.getDiameterMax().doubleValue(SI.METER);
			
			double lengthStretchingFactor = nacelleLength/tpNacelleTemplateLenght;
			double heightStretchingFactor = nacelleMaxDiameter/tpNacelleTemplateHeight;
			
			nacelleLengthStretching.SetAffinity(lengthStretchingRS, lengthStretchingFactor);
			nacelleHeightStretching.SetAffinity(heightStretchingRS, heightStretchingFactor);
			nacelleWidthStretching.SetAffinity(widthStretchingRS, heightStretchingFactor);
			
			OCCShape xStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(shapesCompound, nacelleLengthStretching, 0).Shape()
							));			
			OCCShape xzStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(xStretchedNacelle.getShape(), nacelleHeightStretching, 0).Shape()
							));
			OCCShape xyzStretchedNacelle = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToCompound(
							new BRepBuilderAPI_GTransform(xzStretchedNacelle.getShape(), nacelleWidthStretching, 0).Shape()
							));		
			
			TopExp_Explorer shapesCompoundExp = new TopExp_Explorer();
			shapesCompoundExp.Init(xyzStretchedNacelle.getShape(), TopAbs_ShapeEnum.TopAbs_VERTEX);
			
			double[] refPntD = ((OCCVertex) OCCUtils.theFactory.newShape(TopoDS.ToVertex(shapesCompoundExp.Current()))).pnt();		
			gp_Pnt refPoint = new gp_Pnt(refPntD[0], refPntD[1], refPntD[2]);
			
			shapesCompoundExp.Clear();
			shapesCompoundExp.Init(xyzStretchedNacelle.getShape(), TopAbs_ShapeEnum.TopAbs_SOLID);
			
			OCCShape modNacelle0 = (OCCShape) OCCUtils.theFactory.newShape(TopoDS.ToSolid(shapesCompoundExp.Current()));
			
			nacelleTranslate.SetTranslation(refPoint, 
					new gp_Pnt(xPosition, yPosition, zPosition));
			
			OCCShape modNacelle = (OCCShape) OCCUtils.theFactory.newShape(
					TopoDS.ToSolid(
							new BRepBuilderAPI_Transform(modNacelle0.getShape(), nacelleTranslate, 0).Shape()
							));		
			
			moddedNacelles.add(modNacelle);
		}
		
		// -----------------------------
		// Generate remaining CAD parts
		// -----------------------------
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
				fuselage, 7, 7, false, false, true);
		
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				wing, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				hTail, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				vTail, WingTipType.ROUNDED, false, false, true);
		
		exportShapes.addAll(fuselageShapes);
		exportShapes.addAll(wingShapes);
		exportShapes.addAll(hTailShapes);
		exportShapes.addAll(vTailShapes);
		
		exportShapes.addAll(moddedNacelles);
		OCCUtils.write("Test39mds", FileExtension.STEP, exportShapes);
	}

}

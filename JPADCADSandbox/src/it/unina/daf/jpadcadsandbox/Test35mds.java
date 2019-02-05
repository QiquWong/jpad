package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCWire;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRep_Builder;
import opencascade.GProp_GProps;
import opencascade.IFSelect_PrintCount;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopTools_IndexedMapOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;

public class Test35mds {

	public static void main(String[] args) {
		System.out.println("-------------------------------------");
		System.out.println("--- Nacelle STEP file import test ---");
		System.out.println("-------------------------------------");
		
		if (OCCUtils.theFactory == null) {
			OCCUtils.initCADShapeFactory();
		}
		
		STEPControl_Reader stepReader = new STEPControl_Reader();
		stepReader.ReadFile("C:\\Users\\Mario\\Desktop\\Engine_CAD\\engine_rebuild.STEP");
		
		IFSelect_PrintCount mode = IFSelect_PrintCount.IFSelect_ListByItem; 
		stepReader.PrintCheckLoad(1, mode);
		
		stepReader.TransferRoots();
		TopoDS_Shape transferredShape = stepReader.OneShape();
		
		System.out.println(transferredShape.ShapeType().toString());
		
		List<OCCShape> shapesList = new ArrayList<>();
		
		TopTools_IndexedMapOfShape mapOfShapes = new TopTools_IndexedMapOfShape();
		TopExp.MapShapes(transferredShape, TopAbs_ShapeEnum.TopAbs_SOLID, mapOfShapes);
		
		TopoDS_Solid solid1 = TopoDS.ToSolid(mapOfShapes.FindKey(1));
		TopoDS_Solid solid2 = TopoDS.ToSolid(mapOfShapes.FindKey(2));
		TopoDS_Solid solid3 = TopoDS.ToSolid(mapOfShapes.FindKey(3));
		
		gp_Pnt cg1 = getCG(solid1);
		gp_Pnt cg2 = getCG(solid2);
		gp_Pnt cg3 = getCG(solid3);
		
		gp_Pnt cg = new gp_Pnt((cg1.X() + cg2.X() + cg3.X())/3, 0.0, 0.0);
		
		BRep_Builder builder = new BRep_Builder();
		TopoDS_Compound compound = new TopoDS_Compound();
		builder.MakeCompound(compound);
		
		builder.Add(compound, solid1);
		builder.Add(compound, solid2);
		builder.Add(compound, solid3);
		
		OCCShape scaledEngine = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(scale(compound, cg, 6.5e-04)));
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface canard = aircraft.getCanard();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
//		vTail.setZApexConstructionAxes(vTail.getZApexConstructionAxes().plus(Amount.valueOf(-0.10, SI.METER)));
		
		double engineXApex = aircraft.getNacelles().getNacellesList().get(0).getXApexConstructionAxes().doubleValue(SI.METER) + 3.75;
		double engineYApex = aircraft.getNacelles().getNacellesList().get(0).getYApexConstructionAxes().doubleValue(SI.METER);
		double engineZApex = aircraft.getNacelles().getNacellesList().get(0).getZApexConstructionAxes().doubleValue(SI.METER) - 0.40;
		
		gp_Pnt enginePosition = new gp_Pnt(engineXApex, engineYApex, engineZApex);
		
		gp_Trsf translate = new gp_Trsf();
		translate.SetTranslation(cg, enginePosition);
		
		scaledEngine = (OCCShape) OCCUtils.theFactory.newShape(TopoDS.ToCompound(
				new BRepBuilderAPI_Transform(
						scaledEngine.getShape(), translate, 0).Shape()));
		
		OCCShape scaledEngineMirrored = OCCUtils.getShapeMirrored(scaledEngine, 
				new PVector(0.0f, 0.0f, 0.0f), 
				new PVector(0.0f, 1.0f, 0.0f), 
				new PVector(1.0f, 0.0f, 0.0f));
		
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(fuselage, 7, 7, false, false, true);
//		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(wing, WingTipType.WINGLET, false, false, true);
		List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(canard, WingTipType.ROUNDED, false, false, true);
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(wing, WingTipType.ROUNDED, false, false, true);
		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(hTail, WingTipType.ROUNDED, false, false, true);
		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(vTail, WingTipType.ROUNDED, false, false, true);
		
		shapesList.addAll(fuselageShapes);
//		shapesList.addAll(canardShapes);
//		shapesList.addAll(wingShapes);
//		shapesList.addAll(hTailShapes);
//		shapesList.addAll(vTailShapes);
//		shapesList.add(scaledEngine);
//		shapesList.add(scaledEngineMirrored);
		
		OCCUtils.write("Test35mds", FileExtension.STEP, shapesList);
	}
	
	private static gp_Pnt getCG(TopoDS_Shape shape) {
		
		GProp_GProps gProp = new GProp_GProps();
		BRepGProp.LinearProperties(shape, gProp);	
		gp_Pnt cg = gProp.CentreOfMass();	
		
		return cg;
	}
	
	private static TopoDS_Shape scale(TopoDS_Shape shape, gp_Pnt pnt, double scaleFactor) {
		
		gp_Trsf scale = new gp_Trsf();
		scale.SetScale(pnt, scaleFactor);
		return new BRepBuilderAPI_Transform(shape, scale, 0).Shape();
	}

}

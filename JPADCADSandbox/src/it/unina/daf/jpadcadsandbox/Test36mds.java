package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.unit.SI;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCUtils.FileExtension;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcad.utils.AircraftCADUtils.WingTipType;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRepGProp;
import opencascade.BRep_Builder;
import opencascade.GProp_GProps;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp;
import opencascade.TopTools_IndexedMapOfShape;
import opencascade.TopoDS;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;

public class Test36mds {

	public static void main(String[] args) {
		System.out.println("-------------------------------------");
		System.out.println("--- Turboprop engine import test ----");
		System.out.println("-------------------------------------");
		
		if (OCCUtils.theFactory == null) {
			OCCUtils.initCADShapeFactory();
		}
		
		STEPControl_Reader stepReader = new STEPControl_Reader();
		stepReader.ReadFile("C:\\Users\\Mario\\Desktop\\ATR-42\\atr42_engine.step");
		
		Interface_Static.SetCVal("xstep.cascade.unit", "M");
		
		// Check whether the .step file can be read
		stepReader.PrintCheckLoad(1, IFSelect_PrintCount.IFSelect_ListByItem);
		
		stepReader.TransferRoots();
		TopoDS_Shape transferredShape = stepReader.OneShape();
		
		System.out.println(transferredShape.ShapeType().toString());
		
		List<OCCShell> engineShapes = new ArrayList<>();
		
		TopTools_IndexedMapOfShape mapOfShells = new TopTools_IndexedMapOfShape();
		TopExp.MapShapes(transferredShape, TopAbs_ShapeEnum.TopAbs_SHELL, mapOfShells);
		
		System.out.println("Number of shell entities found: " + mapOfShells.Size());
		
		IntStream.range(1, mapOfShells.Size() + 1)
				 .forEach(i -> engineShapes.add(
						 (OCCShell) OCCUtils.theFactory.newShape(
								 TopoDS.ToShell(mapOfShells.FindKey(i)))
						 ));
		
		System.out.println("Number of transferred shell entities: " + engineShapes.size());	
		
		// Mirroring the engine with respect to the aircraft symmetry plane and YZ plane
		List<OCCShape> mirrEngineShapes = new ArrayList<>();
		mirrEngineShapes.addAll(engineShapes.stream()
				.map(shape -> OCCUtils.getShapeMirrored(shape, 
						new PVector(0.0f, 0.0f, 0.0f), 
						new PVector(0.0f, 1.0f, 0.0f), 
						new PVector(1.0f, 0.0f, 0.0f)
						))
				.collect(Collectors.toList())
				);
		
		List<OCCShape> rightEngineShapes = new ArrayList<>();
		rightEngineShapes.addAll(mirrEngineShapes.stream()
				.map(shape -> OCCUtils.getShapeMirrored(shape, 
						new PVector(0.0f, 0.0f, 0.0f), 
						new PVector(1.0f, 0.0f, 0.0f), 
						new PVector(0.0f, 1.0f, 0.0f)
						))
				.collect(Collectors.toList())
				);	
		
		double[] boundBoxNacelle = rightEngineShapes.get(2).boundingBox();
		System.out.println("Original nacelle bounding box: " + Arrays.toString(boundBoxNacelle));
		
		double nacelleXRef = boundBoxNacelle[0];
		double nacelleYRef = boundBoxNacelle[1] + (boundBoxNacelle[4] - boundBoxNacelle[1])/2;
		double nacelleZRef = boundBoxNacelle[2] + (boundBoxNacelle[5] - boundBoxNacelle[2])/2;
		
		BRep_Builder compoundBuilder = new BRep_Builder();
		TopoDS_Compound engineCompound = new TopoDS_Compound();
		compoundBuilder.MakeCompound(engineCompound);
		
		rightEngineShapes.forEach(shape -> compoundBuilder.Add(engineCompound, shape.getShape()));
		
		OCCShape engineCompoundRight = (OCCShape) OCCUtils.theFactory.newShape(
				scale(engineCompound, getCG(engineCompound), 0.75));
		
		OCCExplorer exp = new OCCExplorer();
		exp.init(engineCompoundRight, CADShapeTypes.SHELL);
		exp.next();
		exp.next();
		OCCShape nacelle = (OCCShape) exp.current();
		double[] boundBoxNacelleScaled = nacelle.boundingBox();
		System.out.println("Scaled nacelle bounding box: " + Arrays.toString(boundBoxNacelleScaled));
		
		nacelleXRef = boundBoxNacelleScaled[0];
		nacelleYRef = boundBoxNacelleScaled[1] + (boundBoxNacelleScaled[4] - boundBoxNacelleScaled[1])/2;
		nacelleZRef = boundBoxNacelleScaled[2] + (boundBoxNacelleScaled[5] - boundBoxNacelleScaled[2])/2;
		
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		double nacelleXApex = aircraft.getNacelles().getNacellesList().get(0).getXApexConstructionAxes().doubleValue(SI.METER);
		double nacelleYApex = aircraft.getNacelles().getNacellesList().get(0).getYApexConstructionAxes().doubleValue(SI.METER);
		double nacelleZApex = aircraft.getNacelles().getNacellesList().get(0).getZApexConstructionAxes().doubleValue(SI.METER);
		
		double semispan = aircraft.getWing().getSemiSpan().doubleValue(SI.METER);
		
		List<OCCShape> enginesShapes = new ArrayList<>();
		
		gp_Trsf translate1 = new gp_Trsf();
		translate1.SetTranslation(
				new gp_Pnt(nacelleXRef, nacelleYRef, nacelleZRef), 
				new gp_Pnt(
						nacelleXApex + 0.5, 
						nacelleYApex, 
						nacelleZApex + 0.25)
				);
			
		OCCShape engineCompoundRight1 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_Transform(engineCompoundRight.getShape(), translate1, 0).Shape()
						));	
		
		gp_Trsf translate2 = new gp_Trsf();
		translate2.SetTranslation( 
				new gp_Pnt(nacelleXRef, nacelleYRef, nacelleZRef),
				new gp_Pnt(
						nacelleXRef + 0.7, 
						nacelleYRef + (semispan - nacelleYRef)/2, 
						nacelleZRef - 0.10)
				);
		
		OCCShape engineCompoundRight2 = (OCCShape) OCCUtils.theFactory.newShape(
				TopoDS.ToCompound(
						new BRepBuilderAPI_Transform(engineCompoundRight1.getShape(), translate2, 0).Shape()
						));
		
		enginesShapes.add(engineCompoundRight1);
		enginesShapes.add(engineCompoundRight2);
		
		enginesShapes.add(OCCUtils.getShapeMirrored(
				engineCompoundRight1, 
				new PVector(0.0f, 0.0f, 0.0f), 
				new PVector(0.0f, 1.0f, 0.0f), 
				new PVector(1.0f, 0.0f, 0.0f)));
		
		enginesShapes.add(OCCUtils.getShapeMirrored(
				engineCompoundRight2, 
				new PVector(0.0f, 0.0f, 0.0f), 
				new PVector(0.0f, 1.0f, 0.0f), 
				new PVector(1.0f, 0.0f, 0.0f)));
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		List<OCCShape> aircraftShapes = new ArrayList<>();
		
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
				fuselage, 7, 7, false, false, true);
		
		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				wing, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				hTail, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				vTail, WingTipType.ROUNDED, false, false, true);
		
		List<OCCShape> fairingShapes = AircraftCADUtils.getFairingShapes(
				fuselage, wing, 0.60, 0.75, 0.85, 0.05, 0.75, 0.65, 0.75, false, false, true);
		
		aircraftShapes.addAll(fuselageShapes);
		aircraftShapes.addAll(wingShapes);
		aircraftShapes.addAll(hTailShapes);
		aircraftShapes.addAll(vTailShapes);
		aircraftShapes.addAll(fairingShapes);
	    aircraftShapes.addAll(enginesShapes);
		
		OCCUtils.write("Test36mds", FileExtension.STEP, aircraftShapes);

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

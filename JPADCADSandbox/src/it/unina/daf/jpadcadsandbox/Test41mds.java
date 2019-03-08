package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.log4j.net.SyslogAppender;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.enums.EngineCADComponentsEnum;
import it.unina.daf.jpadcad.enums.FileExtension;
import it.unina.daf.jpadcad.enums.WingTipType;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADShapeTypes;
import it.unina.daf.jpadcad.occ.CADSolid;
import it.unina.daf.jpadcad.occ.OCCCompSolid;
import it.unina.daf.jpadcad.occ.OCCCompound;
import it.unina.daf.jpadcad.occ.OCCExplorer;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.occ.OCCVertex;
import it.unina.daf.jpadcad.utils.AircraftCADUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import javaslang.Tuple2;
import javaslang.Tuple3;
import opencascade.BRepBuilderAPI_GTransform;
import opencascade.BRepBuilderAPI_Transform;
import opencascade.BRep_Builder;
import opencascade.IFSelect_PrintCount;
import opencascade.Interface_Static;
import opencascade.STEPControl_Reader;
import opencascade.TopAbs_ShapeEnum;
import opencascade.TopExp_Explorer;
import opencascade.TopoDS;
import opencascade.TopoDS_CompSolid;
import opencascade.TopoDS_Compound;
import opencascade.TopoDS_Shape;
import opencascade.TopoDS_Solid;
import opencascade.gp_Ax1;
import opencascade.gp_Ax2;
import opencascade.gp_Dir;
import opencascade.gp_GTrsf;
import opencascade.gp_Pnt;
import opencascade.gp_Trsf;
import processing.core.PVector;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;

public class Test41mds {

	public static void main(String[] args) {
		System.out.println("--------------------------------------------------------------------");
		System.out.println("------------------ AircraftCADUtils report testing -----------------");
		System.out.println("--------------------------------------------------------------------");
		
		String inputDirectory = MyConfiguration.inputDirectory;
		
		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		LiftingSurface canard = aircraft.getCanard();
		List<NacelleCreator> nacelles = aircraft.getNacelles().getNacellesList();
		List<Engine> engines = aircraft.getPowerPlant().getEngineList();
		
		// ---------------------------------
		// Generate the CAD of the aircraft
		// ---------------------------------
		List<OCCShape> exportShapes = new ArrayList<>();
		
		Map<EngineCADComponentsEnum, String> tpTemplatesMap = new HashMap<>();
		Map<EngineCADComponentsEnum, String> tfTemplatesMap = new HashMap<>();	
		tpTemplatesMap.put(EngineCADComponentsEnum.NACELLE, "TP_nacelle_01.step");
		tpTemplatesMap.put(EngineCADComponentsEnum.BLADE, "TP_blade_02.step");
		tfTemplatesMap.put(EngineCADComponentsEnum.NACELLE, "TF_complete_01.step");
		
		List<Map<EngineCADComponentsEnum, String>> templatesMapList = new ArrayList<>();
		templatesMapList.add(tfTemplatesMap);
		templatesMapList.add(tfTemplatesMap);	
		
		List<Amount<Angle>> bladePitchAngleList = new ArrayList<>();
		bladePitchAngleList.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		bladePitchAngleList.add(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		
		boolean exportWires = false;
		boolean exportShells = false;
		boolean exportSolids = true;
		
		// Generate CAD components and measure elapsed times	
		List<OCCShape> fuselageShapes = AircraftCADUtils.getFuselageCAD(
				fuselage, 7, 7, exportWires, exportShells, exportSolids);

		List<OCCShape> wingShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				wing, WingTipType.WINGLET, exportWires, exportShells, exportSolids);

		List<OCCShape> hTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				hTail, WingTipType.ROUNDED, exportWires, exportShells, exportSolids);

		List<OCCShape> vTailShapes = AircraftCADUtils.getLiftingSurfaceCAD(
				vTail, WingTipType.ROUNDED, exportWires, exportShells, exportSolids);
		
//		List<OCCShape> canardShapes = AircraftCADUtils.getLiftingSurfaceCAD(
//				canard, WingTipType.ROUNDED, exportWires, exportShells, exportSolids);

		List<OCCShape> wingFairingShapes = AircraftCADUtils.getFairingShapes(
				fuselage, wing, 
				0.60, 0.75, 0.85, 0.05, 
				0.75, 0.65, 0.75, 
				exportWires, exportShells, exportSolids);
				
//		List<OCCShape> canardFairingShapes = AircraftCADUtils.getFairingShapes(
//				fuselage, canard, 
//				0.85, 0.85, 0.55, 0.20, 
//				0.15, 0.95, 0.50, 
//				exportWires, exportShells, exportSolids);

//		List<OCCShape> engineShapes = AircraftCADUtils.getEnginesCAD(inputDirectory,
//				nacelles, engines, templatesMapList, bladePitchAngleList, exportWires, exportShells, exportSolids);
		
		// ---------------------------------
		// Write the results to file
		// ---------------------------------
		exportShapes.addAll(fuselageShapes);
		exportShapes.addAll(wingShapes);
		exportShapes.addAll(hTailShapes);
		exportShapes.addAll(vTailShapes);
//		exportShapes.addAll(canardShapes);
		exportShapes.addAll(wingFairingShapes);
//		exportShapes.addAll(canardFairingShapes);
//		exportShapes.addAll(engineShapes);
			
		OCCUtils.write("Test41mds", FileExtension.STEP, exportShapes);
	}

}

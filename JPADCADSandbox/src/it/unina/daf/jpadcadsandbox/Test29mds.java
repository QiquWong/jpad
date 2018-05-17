package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;

import aircraft.Aircraft;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcadsandbox.utils.AircraftUtils;
import opencascade.BRepAlgoAPI_Section;

public class Test29mds {

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCADSandbox Test");
		System.out.println("-------------------");
		
		// Import the aircraft
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		LiftingSurface wing = aircraft.getWing();
		
		OCCUtils.initCADShapeFactory();
		
		OCCShape plane = (OCCShape) OCCUtils.makeFilledFace(
				OCCUtils.theFactory.newCurve3D(
						new double[] {0, 3, 10}, 
						new double[] {40, 3, 10}
						),
				OCCUtils.theFactory.newCurve3D(
						new double[] {40, 3, 10}, 
						new double[] {40, 3, -10}
						),
				OCCUtils.theFactory.newCurve3D(
						new double[] {40, 3, -10}, 
						new double[] {0, 3, -10}
						),
				OCCUtils.theFactory.newCurve3D(
						new double[] {0, 3, -10}, 
						new double[] {0, 3, 10}
						)
				);
		
		List<OCCShape> exportShapes = new ArrayList<>();
		
		List<OCCShape> wingShapes = AircraftUtils.getLiftingSurfaceCAD(wing, ComponentEnum.WING, 1e-3, false, true, false);
		
		BRepAlgoAPI_Section sectionMaker = new BRepAlgoAPI_Section();
		sectionMaker.Init1(wingShapes.get(0).getShape());
		sectionMaker.Init2(plane.getShape());
		sectionMaker.Build();
		OCCShape airfoil = (OCCShape) OCCUtils.theFactory.newShape(sectionMaker.Shape());
		
//		exportShapes.addAll(wingShapes);
//		exportShapes.add(plane);
		exportShapes.add(airfoil);
		
		String fileName = "Test29mds.brep";

		if (OCCUtils.write(fileName, exportShapes))
			System.out.println("========== [main] Output written on file: " + fileName);

	}

}

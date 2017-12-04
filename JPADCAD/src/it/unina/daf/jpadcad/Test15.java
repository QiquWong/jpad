package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import it.unina.daf.jpadcad.occ.CADEdge;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShape;
import it.unina.daf.jpadcad.occ.CADVertex;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCFace;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShell;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import opencascade.BRepOffsetAPI_MakeFilling;
import opencascade.GeomAbs_Shape;

public class Test15 {

	//-------------------------------------------------------------

	public static Aircraft theAircraft;

	//-------------------------------------------------------------

	public static void main(String[] args) {
		System.out.println("-------------------");
		System.out.println("JPADCAD Test");
		System.out.println("-------------------");

		System.out.println("========== [main] Getting the aircraft and the fuselage ...");
		Test15.theAircraft = AircraftUtils.importAircraft(args);

		Fuselage fuselage = theAircraft.getFuselage();
		
		// System.out.println(theAircraft);
		
		System.out.println("========== [main] Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		boolean supportShapes = true;
		OCCShell.setDefaultMakeSolid(true);
		System.out.println(">>>>>> OCCShell default-make-solid: " + OCCShell.isDefaultMakeSolid());

//		List<OCCShape> fuselageShapes = AircraftUtils.getFuselageCAD(fuselage, supportShapes);
		
		Amount<Length> noseLength = fuselage.getFuselageCreator().getLengthNoseTrunk();
		System.out.println("Nose length: " + noseLength);
		Amount<Length> cylinderLength = fuselage.getFuselageCreator().getLengthCylindricalTrunk();
		System.out.println("Cylinder length: " + cylinderLength);
		
		// Cylindrical trunk initial section
		CADGeomCurve3D cadCrvCylinderInitialSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength), false);
		// Cylindrical trunk terminal section
		CADGeomCurve3D cadCrvCylinderTerminalSection = OCCUtils.theFactory
				.newCurve3DP(fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(
						noseLength.plus(cylinderLength)), false);
		CADEdge e1 = cadCrvCylinderInitialSection.edge();
		System.out.println("e1 >>>>> length: " + cadCrvCylinderInitialSection.length());
		
		CADVertex[] v12 = e1.vertices();
		System.out.println("e1 >>>>> n. vertices: " + v12.length);
		List<double[]> p12 = new ArrayList<double[]>();
		p12.add(v12[1].pnt());
		p12.add(v12[0].pnt()); // reversed order
		CADGeomCurve3D cadCrvE1 = OCCUtils.theFactory.newCurve3D(p12, false);
		System.out.println("e2 >>>>> length: " + cadCrvE1.length());
		CADEdge e2 = cadCrvE1.edge();
		
		CADShape face1 = OCCUtils.makeFilledFace(
				cadCrvCylinderInitialSection, cadCrvE1);
		
		// Write to a file
		String fileName = "test15.brep";
		
		if (OCCUtils.write(fileName, face1))
			System.out.println("========== [main] Output written on file: " + fileName);

	}

}

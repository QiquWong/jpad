package it.unina.daf.jpadcadsandbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.FuselageSection;

public class Test11 {

//	public static OCCShape makeFuselagePatch() {
//		
//		System.out.println("Section 1 ...");
//		FuselageSection<Amount<Length>> sec1 = FuselageSection.of(
//				Amount.valueOf( 1.0, SI.METER), // xStation
//				Amount.valueOf( 1.0, SI.METER), // semiWidth
//				Amount.valueOf( 0.0, SI.METER), // zBaseline
//				Amount.valueOf( 1.5, SI.METER), // aboveBaseline
//				Amount.valueOf( 0.8, SI.METER)  // belowBaseline				
//				);
//		System.out.println(sec1);
//
//		System.out.println("Section 2 ...");
//		FuselageSection<Amount<Length>> sec2 = FuselageSection.of(
//				Amount.valueOf( 6.0, SI.METER), // xStation
//				Amount.valueOf( 3.5, SI.METER), // semiWidth
//				Amount.valueOf( 0.0, SI.METER), // zBaseline
//				Amount.valueOf( 4.5, SI.METER), // aboveBaseline
//				Amount.valueOf( 2.8, SI.METER)  // belowBaseline				
//				);
//		System.out.println(sec2);
//		
//		List<CADGeomCurve3D> cadGeomCurveList = new ArrayList<CADGeomCurve3D>();
//		cadGeomCurveList.add(sec1.makeCADGeomCurve());
//		cadGeomCurveList.add(sec2.makeCADGeomCurve());
//		
//		// The CADShell object
//		System.out.println("Surfacing ...");
//		CADShell cadShell = OCCUtils.theFactory
//				                    .newShell(
//										cadGeomCurveList.stream() // purge the null objects
//									      				.filter(Objects::nonNull)
//									      				.collect(Collectors.toList())
//									);
//		return (OCCShape)cadShell;
//	}
//	
//	public static void main(String[] args) {
//		
//		MyConfiguration.customizeAmountOutput();
//		
//		System.out.println("Testing Java Wrapper of OCCT >= v7.0.0");
//		System.out.println("Classes in package it.unina.daf.jpadcad");
//		
//		System.out.println("========== Initialize CAD shape factory");
//		OCCUtils.initCADShapeFactory();
//		
//		System.out.println("========== Construct a fuselage nose");
//		OCCShape patch1 = Test11.makeFuselagePatch();
//
//		// Write to a file
//		String fileName = "test11.brep";
//
//		if (OCCUtils.write(fileName, ((OCCShape)patch1)))
//			System.out.println("Output written on file: " + fileName);
//		
//	}

}

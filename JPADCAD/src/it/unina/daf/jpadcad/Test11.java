package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import it.unina.daf.jpadcad.Test11.FuselageSection;
import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.CADShapeFactory;
import it.unina.daf.jpadcad.occ.CADShell;
import it.unina.daf.jpadcad.occ.OCCEdge;
import it.unina.daf.jpadcad.occ.OCCGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCShapeFactory;
import it.unina.daf.jpadcad.occ.OCCUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import opencascade.BRepTools;
import opencascade.BRep_Builder;
import opencascade.TopoDS_Compound;

public class Test11 {

	
	@Getter
	@Setter
	@RequiredArgsConstructor(staticName = "of")
	@EqualsAndHashCode(of = {"xStation", "semiWidth", "zBaseline", "aboveBaseline", "belowBaseline"})
	@ToString
	public static class FuselageSection<T> { // T as Amount<Length>
		@NonNull private T xStation;
		@NonNull private T semiWidth;
		@NonNull private T zBaseline;
		@NonNull private T aboveBaseline;
		@NonNull private T belowBaseline;
		
		public CADGeomCurve3D makeCADGeomCurve() {
			
			Amount<Length> xS, sW, z0, zU, zL;
			xS = (Amount<Length>)xStation;
			sW = (Amount<Length>)semiWidth;
			z0 = (Amount<Length>)zBaseline;
			zU = z0.plus((Amount<Length>)aboveBaseline);
			zL = z0.minus((Amount<Length>)aboveBaseline);
			
			// list of points belonging to the desired curve-1
			List<double[]> points = new ArrayList<double[]>();
			points.add(new double[]{ xS.doubleValue(SI.METER),                        0, zU.doubleValue(SI.METER)});
			points.add(new double[]{ xS.doubleValue(SI.METER), sW.doubleValue(SI.METER),                         0});
			points.add(new double[]{ xS.doubleValue(SI.METER),                        0, zL.doubleValue(SI.METER)});

			// 
			if (OCCUtils.theFactory == null)
				return null;
			
			boolean isPeriodic = false;
			CADGeomCurve3D cadGeomCurve3D = OCCUtils.theFactory.newCurve3D(points, isPeriodic);
			return cadGeomCurve3D;
		}

	}	
	
	public static OCCShape makeFuselagePatch() {
		
		System.out.println("Section 1 ...");
		FuselageSection<Amount<Length>> sec1 = FuselageSection.of(
				Amount.valueOf( 1.0, SI.METER), // xStation
				Amount.valueOf( 1.0, SI.METER), // semiWidth
				Amount.valueOf( 0.0, SI.METER), // zBaseline
				Amount.valueOf( 1.5, SI.METER), // aboveBaseline
				Amount.valueOf( 0.8, SI.METER)  // belowBaseline				
				);
		System.out.println(sec1);

		System.out.println("Section 2 ...");
		FuselageSection<Amount<Length>> sec2 = FuselageSection.of(
				Amount.valueOf( 6.0, SI.METER), // xStation
				Amount.valueOf( 3.5, SI.METER), // semiWidth
				Amount.valueOf( 0.0, SI.METER), // zBaseline
				Amount.valueOf( 4.5, SI.METER), // aboveBaseline
				Amount.valueOf( 2.8, SI.METER)  // belowBaseline				
				);
		System.out.println(sec2);
		
		List<CADGeomCurve3D> cadGeomCurveList = new ArrayList<CADGeomCurve3D>();
		cadGeomCurveList.add(sec1.makeCADGeomCurve());
		cadGeomCurveList.add(sec2.makeCADGeomCurve());
		
		// The CADShell object
		System.out.println("Surfacing ...");
		CADShell cadShell = OCCUtils.theFactory.newShell(cadGeomCurveList);
		
		return (OCCShape)cadShell;
	}
	
	public static void main(String[] args) {
		
		MyConfiguration.customizeAmountOutput();
		
		System.out.println("Testing Java Wrapper of OCCT >= v7.0.0");
		System.out.println("Classes in package it.unina.daf.jpadcad");
		
		System.out.println("========== Initialize CAD shape factory");
		OCCUtils.initCADShapeFactory();
		
		System.out.println("========== Construct a fuselage nose");
		OCCShape patch1 = Test11.makeFuselagePatch();

		// Write to a file
		String fileName = "test11.brep";

		if (OCCUtils.write(fileName, ((OCCShape)patch1)))
			System.out.println("Output written on file: " + fileName);
		
	}

}

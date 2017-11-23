package it.unina.daf.jpadcad;

import java.util.ArrayList;
import java.util.List;

import javax.measure.converter.ConversionException;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import it.unina.daf.jpadcad.occ.CADGeomCurve3D;
import it.unina.daf.jpadcad.occ.OCCUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@RequiredArgsConstructor(staticName = "of")
@EqualsAndHashCode(of = {"xStation", "semiWidth", "zBaseline", "aboveBaseline", "belowBaseline"})
@ToString
public class FuselageSection<T> { // T as Amount<Length>
	@NonNull private T xStation;
	@NonNull private T semiWidth;
	@NonNull private T zBaseline;
	@NonNull private T aboveBaseline;
	@NonNull private T belowBaseline;
	
	public CADGeomCurve3D makeCADGeomCurve() {
		
		if (!(xStation instanceof Amount<?>))
			return null;

		Amount<Length> xS, sW, z0, zU, zL;
		try {
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
			
			// the global factory variable must be non-null
			if (OCCUtils.theFactory == null)
				return null;
			
			boolean isPeriodic = false;
			CADGeomCurve3D cadGeomCurve3D = OCCUtils.theFactory.newCurve3D(points, isPeriodic);
			return cadGeomCurve3D;
			
		} catch (ConversionException e) {
			e.printStackTrace();
			System.err.println("FuselageSection::makeCADGeomCurve accepts Amount<Length> values");
			return null;
		}
		
	}

}	

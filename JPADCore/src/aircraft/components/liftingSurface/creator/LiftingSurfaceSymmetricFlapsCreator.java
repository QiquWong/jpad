package aircraft.components.liftingSurface.creator;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jscience.physics.amount.Amount;

import configuration.enumerations.FlapTypeEnum;

public class LiftingSurfaceSymmetricFlapsCreator implements ILiftingSurfaceSymmetricFlapCreator {

	String _id;
	
	private FlapTypeEnum _type;
	private Double _innerStationSpanwisePosition,
				   _outerStationSpanwisePosition,
				   _chordRatio;
	private Amount<Angle> _deflection;

	@Override
	public Double getInnerStationSpanwisePosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getOuterStationSpanwisePosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double getChordRatio() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Amount<Angle> getDeflection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlapTypeEnum getType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//=================================================================
	// Builder pattern via a nested public static class
	
//	public static class LiftingSurfaceSymmetricFlapBuilder {
//		// required parameters
//		private String __id;
//		private FlapTypeEnum __type;
//		private Double __innerStationSpanwisePosition;
//		private Double __outerStationSpanwisePosition;
//		private Double __chordRatio;
//		private Amount<Angle> __deflection;
//
//		// optional parameters ... defaults
//		// ...
//
//		public LiftingSurfaceSymmetricFlapBuilder(
//				String id,
//				FlapTypeEnum type,
//				Double innerStationSpanwisePosition,
//				Double outerStationSpanwisePosition,
//				Double chordRatio,
//				Amount<Angle> deflection
//				){
//			this.__id = id;
//			this.__type = type;
//			this.__innerStationSpanwisePosition = innerStationSpanwisePosition;
//			this.__outerStationSpanwisePosition = outerStationSpanwisePosition;
//			this.__chordRatio = chordRatio;
//			this.__deflection = deflection;
//		}
//
//		public LiftingSurfaceSymmetricFlapsCreator build() {
//			return new LiftingSurfaceSymmetricFlapBuilder(this);
//		}
//
//	}
//	
}

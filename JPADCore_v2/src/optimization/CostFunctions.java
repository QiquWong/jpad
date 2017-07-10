package optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.liftingSurface.LiftingSurface;
import calculators.aerodynamics.DragCalc;
import calculators.aerodynamics.LiftCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class CostFunctions {

	public static Double cD0Wing (
			LiftingSurface w,
			double mach,
			double machTransonicThreshold,
			Amount<Length> altitude
			) {
		
		Double kExcr = w.getKExcr();
		
		Double cD0Parasite = DragCalc.calculateCD0ParasiteLiftingSurface(
				w,
				machTransonicThreshold,
				mach,
				altitude
				);
		Double cD0Gap = DragCalc.calculateCDGap(w);
		
		return cD0Parasite*(1+kExcr) + cD0Gap; 
		
	}
	
	public static Double cLmaxWing (
			LiftingSurface w,
			int numberOfPointsSemiSpanwise,
			double mach,
			Amount<Length> altitude
			) {
		
		List<Amount<Length>> _yStationDistribution = new ArrayList<Amount<Length>>();
		List<Amount<Angle>> _alphaZeroLiftDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Angle>> _twistDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Length>> _chordDistribution = new ArrayList<Amount<Length>>();
		List<Amount<Angle>> _dihedralDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Length>> _xLEDistribution = new ArrayList<Amount<Length>>();
		List<Double> _clMaxDistribution = new ArrayList<Double>();
		
		//----------------------------------------------------------------------------------------------------------------------
		// CALCULATING WING PARAMETERS DISTRIBUTION
		//......................................................................................................................
		// ETA STATIONS AND Y STATIONS
		double[] _yStationDistributionArray = MyArrayUtils.linspace(
				0,
				w.getSemiSpan().doubleValue(SI.METER),
				numberOfPointsSemiSpanwise
				);
		for(int i=0; i<_yStationDistributionArray.length; i++)
			_yStationDistribution.add(Amount.valueOf(_yStationDistributionArray[i], SI.METER));
		//......................................................................................................................
		// ALPHA ZERO LIFT
		Double[] _alphaZeroLiftDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getAlpha0VsY()),
				_yStationDistributionArray
				);
		for(int i=0; i<_alphaZeroLiftDistributionArray.length; i++)
			_alphaZeroLiftDistribution.add(Amount.valueOf(_alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// TWIST 
		Double[] _twistDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_twistDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getTwistsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_twistDistributionArray.length; i++)
			_twistDistribution.add(Amount.valueOf(_twistDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// CHORDS
		Double[] _chordDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_chordDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getChordsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_chordDistributionArray.length; i++)
			_chordDistribution.add(Amount.valueOf(_chordDistributionArray[i], SI.METER));
		//......................................................................................................................
		// DIHEDRAL
		Double[] _dihedralDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getDihedralsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_dihedralDistributionArray.length; i++)
			_dihedralDistribution.add(Amount.valueOf(_dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// XLE DISTRIBUTION
		Double[] _xLEDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_xLEDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getXLEBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_xLEDistributionArray.length; i++)
			_xLEDistribution.add(Amount.valueOf(_xLEDistributionArray[i], SI.METER));
		//......................................................................................................................
		// Clmax DISTRIBUTION
		Double[] _clMaxDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(w.getClMaxVsY()),
				_yStationDistributionArray
				);
		for(int i=0; i<_clMaxDistributionArray.length; i++)
			_clMaxDistribution.add(_clMaxDistributionArray[i]);
		
		return LiftCalc.calculateCLMax(
				MyArrayUtils.convertToDoublePrimitive(_clMaxDistribution),
				w.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
				w.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())),
				MyArrayUtils.convertListOfAmountTodoubleArray(_twistDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())),
				MyArrayUtils.convertListOfAmountTodoubleArray(_alphaZeroLiftDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())), 
				(1./(2*numberOfPointsSemiSpanwise)), 
				0.0,
				mach,
				altitude.doubleValue(SI.METER)
				);
		
	}
	
	public static Double cLmaxVsCD0Wing (
			LiftingSurface w,
			int numberOfPointsSemiSpanwise,
			double mach,
			double machTransonicThreshold,
			Amount<Length> altitude,
			double cLmaxWeight,
			double cD0Weight
			) {
		
		List<Amount<Length>> _yStationDistribution = new ArrayList<Amount<Length>>();
		List<Amount<Angle>> _alphaZeroLiftDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Angle>> _twistDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Length>> _chordDistribution = new ArrayList<Amount<Length>>();
		List<Amount<Angle>> _dihedralDistribution = new ArrayList<Amount<Angle>>();
		List<Amount<Length>> _xLEDistribution = new ArrayList<Amount<Length>>();
		List<Double> _clMaxDistribution = new ArrayList<Double>();
		
		//----------------------------------------------------------------------------------------------------------------------
		// CALCULATING WING PARAMETERS DISTRIBUTION
		//......................................................................................................................
		// ETA STATIONS AND Y STATIONS
		double[] _yStationDistributionArray = MyArrayUtils.linspace(
				0,
				w.getSemiSpan().doubleValue(SI.METER),
				numberOfPointsSemiSpanwise
				);
		for(int i=0; i<_yStationDistributionArray.length; i++)
			_yStationDistribution.add(Amount.valueOf(_yStationDistributionArray[i], SI.METER));
		//......................................................................................................................
		// ALPHA ZERO LIFT
		Double[] _alphaZeroLiftDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_alphaZeroLiftDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getAlpha0VsY()),
				_yStationDistributionArray
				);
		for(int i=0; i<_alphaZeroLiftDistributionArray.length; i++)
			_alphaZeroLiftDistribution.add(Amount.valueOf(_alphaZeroLiftDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// TWIST 
		Double[] _twistDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_twistDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getTwistsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_twistDistributionArray.length; i++)
			_twistDistribution.add(Amount.valueOf(_twistDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// CHORDS
		Double[] _chordDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_chordDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getChordsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_chordDistributionArray.length; i++)
			_chordDistribution.add(Amount.valueOf(_chordDistributionArray[i], SI.METER));
		//......................................................................................................................
		// DIHEDRAL
		Double[] _dihedralDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_dihedralDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getDihedralsBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_dihedralDistributionArray.length; i++)
			_dihedralDistribution.add(Amount.valueOf(_dihedralDistributionArray[i], NonSI.DEGREE_ANGLE));
		//......................................................................................................................
		// XLE DISTRIBUTION
		Double[] _xLEDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_xLEDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getXLEBreakPoints()),
				_yStationDistributionArray
				);
		for(int i=0; i<_xLEDistributionArray.length; i++)
			_xLEDistribution.add(Amount.valueOf(_xLEDistributionArray[i], SI.METER));
		//......................................................................................................................
		// Clmax DISTRIBUTION
		Double[] _clMaxDistributionArray = new Double[numberOfPointsSemiSpanwise];
		_clMaxDistributionArray = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertListOfAmountTodoubleArray(w.getLiftingSurfaceCreator().getYBreakPoints()),
				MyArrayUtils.convertToDoublePrimitive(w.getClMaxVsY()),
				_yStationDistributionArray
				);
		for(int i=0; i<_clMaxDistributionArray.length; i++)
			_clMaxDistribution.add(_clMaxDistributionArray[i]);
		
		
		//----------------------------------------------------------------------------------------------------------------------
		// CALCULATING CL MAX
		//......................................................................................................................
		double cLmaxWing = LiftCalc.calculateCLMax(
				MyArrayUtils.convertToDoublePrimitive(_clMaxDistribution),
				w.getLiftingSurfaceCreator().getSemiSpan().doubleValue(SI.METER),
				w.getLiftingSurfaceCreator().getSurfacePlanform().doubleValue(SI.SQUARE_METRE),
				MyArrayUtils.convertListOfAmountTodoubleArray(_yStationDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_chordDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_xLEDistribution),
				MyArrayUtils.convertListOfAmountTodoubleArray(_dihedralDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())),
				MyArrayUtils.convertListOfAmountTodoubleArray(_twistDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())),
				MyArrayUtils.convertListOfAmountTodoubleArray(_alphaZeroLiftDistribution.stream().map(x -> x.to(SI.RADIAN)).collect(Collectors.toList())), 
				(1./(2*numberOfPointsSemiSpanwise)), 
				0.0,
				mach,
				altitude.doubleValue(SI.METER)
				);
		
		//----------------------------------------------------------------------------------------------------------------------
		// CALCULATING CD0 (semiempirical)
		//......................................................................................................................
		Double kExcr = w.getKExcr();
		
		Double cD0Parasite = DragCalc.calculateCD0ParasiteLiftingSurface(
				w,
				machTransonicThreshold,
				mach,
				altitude
				);
		Double cD0Gap = DragCalc.calculateCDGap(w);
		
		double cD0Wing = cD0Parasite*(1+kExcr) + cD0Gap; 
		
		
		//----------------------------------------------------------------------------------------------------------------------
		// CALCULATING COST FUNCTION
		//......................................................................................................................
		return (-cLmaxWing*cLmaxWeight) + (cD0Wing*cD0Weight*1000); // TODO: SCALE FACTOR ??
//		return -cLmaxWing;
//		return cD0Wing;
		
	}
	
}

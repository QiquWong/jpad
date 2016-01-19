package aircraft.components.liftingSurface;

import java.util.Map;
import java.util.TreeMap;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import configuration.enumerations.MethodEnum;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.MyArray;

/**
 * This class holds all geometry calculations
 * necessary to describe the lifting surface
 * 
 * @author Lorenzo Attanasio
 */
public class LSGeometryManager extends aircraft.componentmodel.componentcalcmanager.GeometryManager{

	private LiftingSurface liftingSurface;
	
	private MyArray
	_chordsVsY = new MyArray(SI.METER), 
	_maxThicknessAirfoils;
	
	private CalculateThickness calculateThickness;
	
	private int _nPointsSemispanWise = 0;

	private MyArray _yStations;

	public LSGeometryManager(LiftingSurface liftingSurf) {
		liftingSurface = liftingSurf;
		initializeDependentData();
		initializeInnerCalculators();
	}
	
	@Override
	public void initializeDependentData() {
		
		_nPointsSemispanWise = liftingSurface._eta.size();
		_maxThicknessAirfoils = MyArray.createArray(liftingSurface.get_maxThicknessVsY().toArray())
				.interpolate(liftingSurface._etaAirfoil.toArray(), liftingSurface._eta.toArray());
		
		_yStations = new MyArray(MyArrayUtils.linspace(
				0.,
				liftingSurface._semispan.getEstimatedValue(),
				liftingSurface._eta.size()));
		
		for (int i=0; i<_nPointsSemispanWise; i++){
			_chordsVsY.add(liftingSurface.getChordAtYActual(_yStations.get(i)));
		}
		_chordsVsY.toArray();
		
	}
	
	@Override
	public void initializeInnerCalculators() {
		calculateThickness = new CalculateThickness();		
	}

	public void calculateAll() {
		calculateThickness.allMethods();
		calculateEllipticChordDistribution();
	}

	/**
	 * Equivalent elliptic chord distribution
	 * evaluated from eta = 0 to eta = 1
	 * 
	 * @author Lorenzo Attanasio
	 */
	public void calculateEllipticChordDistribution() {
		MyArray ones = new MyArray(Unit.ONE);
		ones.fillOnes(LiftingSurface._numberOfPointsChordDistribution);

		liftingSurface._ellChordVsY.setDouble(
				MyArray.createArray(
						MyArrayUtils.sqrt(ones.minus(liftingSurface._eta.pow(2.))))
						.times(
								(2*liftingSurface._surface.getEstimatedValue()
										/(Math.PI * liftingSurface._span.getEstimatedValue()))));
	}

	/**
	 * Evaluate mean maximum thickness over chord of
	 * the lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 */
	public class CalculateThickness {

		public CalculateThickness() {

		}

		private Map<MethodEnum, Double> _methodsMap = 
				new TreeMap<MethodEnum, Double>();

		public double integralMean() {
			double maxTcMean = 0.;
			
			maxTcMean = Double.valueOf((2/liftingSurface._surface.getEstimatedValue())
					* MyMathUtils.integrate1DSimpsonSpline(
							_yStations.toArray(), 
							_maxThicknessAirfoils.times(_chordsVsY).toArray(), 
							0., liftingSurface._semispan.times(0.99999999).getEstimatedValue()));
			_methodsMap.put(MethodEnum.INTEGRAL_MEAN, maxTcMean);
			liftingSurface._maxThicknessMean = maxTcMean;
			return maxTcMean;
		}

		public void allMethods() {
			integralMean();
		}

		public Map<MethodEnum, Double> get_methodsMap() {
			return _methodsMap;
		}

	}

	public CalculateThickness getCalculateThickness() {
		return calculateThickness;
	}

}




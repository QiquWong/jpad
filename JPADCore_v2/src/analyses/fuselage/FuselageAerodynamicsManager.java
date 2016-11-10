package analyses.fuselage;

import static java.lang.Math.pow;

import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import analyses.OperatingConditions;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;

public class FuselageAerodynamicsManager extends analyses.analysismodel.analysiscalcmanager.AerodynamicsManager{

	private Fuselage _theFuselage;
	private Aircraft _theAircraft;
	
	private final double[] _positionOfC4ToFuselageLength = {.1,.2,.3,.4,.5,.6,.7};
	private final double[] _kF = {.115, .172, .344, .487, .688, .888, 1.146};

	private double length;
	private double maxWidth;
	private Double cLAlphaW;
	private double mac;
	private double surfaceW;

	private Double _cD0Upsweep, _cDWindshield, _cF,
	_cD0Parasite, _cD0Total, _cD0Base, 
	_equivalentDiameterBase, _reynolds;

	private double _cMAlpha, _cMCL, _cm0;
	private CalculateCm0 calculateCm0;
	private CalculateCmAlpha calculateCmAlpha;
	private CalculateCmCL calculateCmCL;
	private Amount<Length> _len_F;
	private Amount<Length> _roughness;
	private Amount<Length> _len_T;
	private Amount<Area> _sWet;
	private Amount<Area> _area_C;
	private String databaseFolderPath;
	private String databaseFileName;

	public FuselageAerodynamicsManager(OperatingConditions ops, Aircraft aircraft) {

		_theAircraft = aircraft;
		_theFuselage = aircraft.getFuselage();
		 aircraft.getFuselage().setAerodynamics(this);
		_theOperatingConditions = ops;
   
		initializeDependentData();
		initializeInnerCalculators();
	}

	@Override
	public void initializeDependentData() {
		length = _theFuselage.getFuselageCreator().getLenF().doubleValue(SI.METER);
		maxWidth = _theFuselage.getFuselageCreator().getSectionCylinderWidth().doubleValue(SI.METER);

		try {
			cLAlphaW = _theAircraft.getWing().getAerodynamics().getCalculateCLAlpha().andersonSweptCompressibleSubsonic();
		} catch (NullPointerException e) {
			cLAlphaW = 6.28;	
		}

		mac = _theAircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().doubleValue(SI.METER);
		surfaceW = _theAircraft.getWing().getSurface().doubleValue(SI.SQUARE_METRE);

		_len_F = _theFuselage.getFuselageCreator().getLenF();
		_roughness = _theFuselage.getFuselageCreator().getRoughness();
		_sWet = _theFuselage.getsWet();
		_len_T = _theFuselage.getFuselageCreator().getLenT();
		_area_C = _theFuselage.getFuselageCreator().getAreaC();
	}

	@Override
	public void initializeInnerCalculators() {
		calculateCm0 = new CalculateCm0();
		calculateCmAlpha = new CalculateCmAlpha();
		calculateCmCL = new CalculateCmCL();		
	}

	public void calculateAll() {
		calculateCD0Total();
		calculateCm0.allMethods();
		calculateCmAlpha.allMethods();
		calculateCmCL.allMethods();
	}

	public void calculateCD0Total() {
		calculateCD0Total(_theOperatingConditions, _theAircraft, MethodEnum.EMPIRICAL);
	}

	public double calculateCD0Total(
			OperatingConditions conditions,
			Aircraft aircraft, 
			MethodEnum method) {

		_theOperatingConditions = conditions;
		_theAircraft = aircraft;

		//		double x = 26.62; //value chosen to match matlab file base drag //lenF - dxTailCap;
		_equivalentDiameterBase = _theFuselage.getFuselageCreator().getEquivalentDiameterAtX(_len_F.getEstimatedValue()*0.9995);

		double kExcr = DragCalc.calculateKExcrescences(_theAircraft.getSWetTotal().doubleValue(SI.SQUARE_METRE));

		// TODO : (Vittorio) GIVE THE POSSIBILITY TO ASSIGN MACH AND ALTITUDE IN DIFFERENT CONDITIONS !!
		_cF = AerodynamicCalc.calculateCf(
				AerodynamicCalc.calculateReynolds(
						_theOperatingConditions.getAltitude().getEstimatedValue(),
						_theOperatingConditions.getMachCurrent(),
						_len_F.getEstimatedValue()
						), 
				_theOperatingConditions.getMachCurrent().doubleValue(), 
				0.
				);

//		_cD0Parasite = DragCalc.calculateCd0Parasite(_theFuselage.getFuselageCreator().getFormFactor(), 
//				_cF, _sWet.getEstimatedValue(), _theAircraft.getWing().getSurface().getEstimatedValue());

		_cD0Base = DragCalc.calculateCd0Base(MethodEnum.MATLAB, 
				_cD0Parasite, _theAircraft.getWing().getSurface().getEstimatedValue(), 
				_equivalentDiameterBase, _theFuselage.getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue());

		calculateCdUpsweep();
		calculateWindshield(method);
		_cD0Total = _cD0Parasite*(1 + kExcr) + _cD0Upsweep + _cD0Base + _cDWindshield;
		return _cD0Total;
	}

	public double calculateCdUpsweep() { // page 67 Behind ADAS (Stanford)

		double zCamber75 = _theFuselage.getFuselageCreator().getCamberAngleAtX(_len_F.getEstimatedValue() - 0.25*_len_T.getEstimatedValue());

		_cD0Upsweep =  0.075 * (_area_C.getEstimatedValue()/
				_theAircraft.getWing().getSurface().getEstimatedValue()) *
				(zCamber75/(0.75*_len_T.getEstimatedValue()));

		return _cD0Upsweep;
	}


	/** 
	 * Calculate windshield drag; accepted methods are Empirical, NACA, Roskam.
	 * 
	 * @author Lorenzo Attanasio
	 * @param
	 */
	public double calculateWindshield(MethodEnum method) {
		double deltaCd = 0.0;

		switch(method){ // Behind ADAS page 101
		case EMPIRICAL : {
			_cDWindshield = 0.07*_theFuselage.getFuselageCreator().getWindshieldArea().getEstimatedValue()/
					_theAircraft.getWing().getSurface().getEstimatedValue();
		} break;
		case NACA : { // NACA report 730, poor results
			_cDWindshield = 0.08*_theFuselage.getFuselageCreator().getAreaC().getEstimatedValue()/
					_theAircraft.getWing().getSurface().getEstimatedValue();
		} break;
		case ROSKAM : { // page 134 Roskam, part VI
			switch(_theFuselage.getFuselageCreator().getWindshieldType()){
			case FLAT_PROTRUDING : {deltaCd = .016;}; break;
			case FLAT_FLUSH : {deltaCd = .011;}; break;
			case SINGLE_ROUND : {deltaCd = .002;}; break;
			case SINGLE_SHARP : {deltaCd = .005;}; break;
			case DOUBLE : {deltaCd = .002;}; break;
			default : {deltaCd = 0.0;}; break;
			}
			_cDWindshield = (deltaCd*_theFuselage.getFuselageCreator().getAreaC().getEstimatedValue())/
					_theAircraft.getWing().getSurface().getEstimatedValue();
		} break;
		default : {
			_cDWindshield = 0.0;
			System.out.println("Inside default branch of calculateWindshield method, class MyFuselage");
		} break;
		}

		return _cDWindshield;
	}

	public String getDatabaseFolderPath() {
		return databaseFolderPath;
	}

	public String getDatabaseFileName() {
		return databaseFileName;
	}

	public class CalculateCm0 {

		private AerodynamicDatabaseReader _aerodynamicDatabaseReader;
		private double k2k1;

		public CalculateCm0() {
			_aerodynamicDatabaseReader = _theAircraft.getTheAnalysisManager().getTheAerodynamics().get_aerodynamicDatabaseReader();
			k2k1 = _aerodynamicDatabaseReader.get_C_m0_b_k2_minus_k1_vs_FFR(
					_theFuselage.getFuselageCreator().getLenF().doubleValue(SI.METER), 
					_theFuselage.getFuselageCreator().getEquivalentDiameterGM().doubleValue(SI.METER)); 
		}

		private Map<MethodEnum, Double> _methodMap = 
				new TreeMap<MethodEnum, Double>();

		public double multhopp() {

			double sum=0.;
			double[] x = MyArrayUtils.linspace(
					0., _theFuselage.getFuselageCreator().getLenF().getEstimatedValue()*(1-0.0001),
					100);

			try {
				for(int i=1; i<x.length; i++){
					sum = sum + pow(_theFuselage.getFuselageCreator().getWidthAtX(x[i]),2)
					*(_theFuselage.getFuselageCreator().getCamberAngleAtX(x[i]) 
							+ _theAircraft.getWing().getRiggingAngle().getEstimatedValue()
							+ _theAircraft.getWing().getAerodynamics()
							.getCalculateAlpha0L().integralMeanWithTwist().getEstimatedValue())
					* (x[i] - x[i-1]);
				}

				_cm0 = k2k1/(36.5*surfaceW*mac) * sum;
				_methodMap.put(MethodEnum.MULTHOPP, _cm0);

			} catch (NullPointerException e) {
				_cm0 = 0.0;
			}

			return _cm0;
		}


		public void allMethods(){
			multhopp();
		}

		public Map<MethodEnum, Double> get_methodMap() {
			return _methodMap;
		}


	}// end of CalculateCM0 class 

	public class CalculateCmAlpha {

		private Map<MethodEnum, Double> _methodMap = 
				new TreeMap<MethodEnum, Double>();

		public double gilruth() {

			if (_theAircraft.getWing() == null) return 0.;

			double kf = MyMathUtils
					.interpolate1DLinear(_positionOfC4ToFuselageLength, _kF)
					.value((_theAircraft.getWing().getXApexConstructionAxes().getEstimatedValue() 
							+ 0.25*_theAircraft.getWing().getChordRoot().getEstimatedValue())
							/_theAircraft.getFuselage().getFuselageCreator().getLenF().getEstimatedValue());

			_cMAlpha = kf*pow(maxWidth,2) * length
					/ (surfaceW*mac);

			_methodMap.put(MethodEnum.GILRUTH, _cMAlpha);
			return _cMAlpha;
		}

		public void allMethods(){
			gilruth();
		}

		public Map<MethodEnum, Double> get_methodMap() {
			return _methodMap;
		}

	}

	public class CalculateCmCL {

		private Map<MethodEnum, Double> _methodMap = 
				new TreeMap<MethodEnum, Double>();

		public double gilruth() {

			if (_theAircraft.getWing() == null) return 0.;

			double kf = MyMathUtils
					.interpolate1DLinear(_positionOfC4ToFuselageLength, _kF)
					.value((_theAircraft.getWing().getXApexConstructionAxes().getEstimatedValue() 
							+ 0.25*_theAircraft.getWing().getChordRoot().getEstimatedValue())
							/_theAircraft.getFuselage().getFuselageCreator().getLenF().getEstimatedValue());

			_cMCL = kf*pow(maxWidth,2) * length
					/ (surfaceW*mac*cLAlphaW);
			_methodMap.put(MethodEnum.GILRUTH, _cMCL);
			return _cMCL;
		}

		public void allMethods(){
			gilruth();
		}

		public Map<MethodEnum, Double> get_methodMap() {
			return _methodMap;
		}


	}


	/**
	 * This class evaluates slope of the linear trait of CL vs Alpha curve of wing-body. 
	 * see--> Sforza p.64
	 * 
	 * @author Manuela Ruocco
	 */

	public  double calculateCLAlphaFuselage(double cLAlphaWing){ //Sforza p64
		double d = _theFuselage.getFuselageCreator().getEquivalentDiameterAtX(
				_theAircraft
				.getWing()
				.getLiftingSurfaceCreator()
				.getMeanAerodynamicChordLeadingEdgeX().getEstimatedValue()
				);
		double b = _theAircraft.getWing().getSpan().getEstimatedValue();
		double cLAlphaFuselage = (1.0+((1/4.0)*(d/b))-((1/40.0)*Math.pow((d/b), 2)))*cLAlphaWing;
	
		return cLAlphaFuselage;
	}

	public CalculateCm0 getCalculateCm0() {
		return calculateCm0;
	}

	public CalculateCmAlpha getCalculateCmAlpha() {
		return calculateCmAlpha;
	}

	public CalculateCmCL getCalculateCmCL() {
		return calculateCmCL;
	}

	public Double get_cF() {
		return _cF;
	}


	public Double get_equivalentDiameterBase() {
		return _equivalentDiameterBase;
	}


	public Double get_cD0Parasite() {
		return _cD0Parasite;
	}


	public Double get_cD0Total() {
		return _cD0Total;
	}


	public Double get_cD0Base() {
		return _cD0Base;
	}


	public Double get_reynolds() {
		return _reynolds;
	}

	public Double get_cD0Upsweep() {
		return _cD0Upsweep;
	}


	public Double get_cDWindshield() {
		return _cDWindshield;
	}


}

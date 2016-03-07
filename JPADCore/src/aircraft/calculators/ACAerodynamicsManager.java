package aircraft.calculators;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcAlpha0L;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcCLAtAlpha;
import aircraft.components.liftingSurface.LSAerodynamicsManager.MeanAirfoil;
import calculators.aerodynamics.AerodynamicCalc;
import calculators.aerodynamics.DragCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.customdata.DragPolarPoint;
import standaloneutils.customdata.MyArray;
import writers.JPADStaticWriteUtils;

/** 
 * Evaluate and store aerodynamic parameters relative to the whole aircraft.
 * Calculations are handled through static libraries which are properly called in this class;
 * other methods are instead used to get the aerodynamic parameters from each component
 * in order to obtain quantities relative to the whole aircraft. 
 * 
 * @author Lorenzo Attanasio
 *
 */
public class ACAerodynamicsManager extends ACCalculatorManager {

	private final String id = "24";
	private AnalysisTypeEnum _type;
	private String _name;
	
	private AerodynamicDatabaseReader _aerodynamicDatabaseReader;
	private HighLiftDatabaseReader _highLiftDatabaseReader;
	
	private Aircraft _theAircraft;
	private OperatingConditions _theOperatingConditions;

	// TODO: add a ProjectData object that stores data and calculation results
	// ...

	private Map<Double, Double[]> _cDMap = new HashMap<Double, Double[]>();

	private String _independentVariable = "cl";
	private Double[] _eWhole , _cD, _cL, _cY;
	private MyArray _machDragPolar = new MyArray();
	private Double _cLcurrent;

	private Double 
		_alpha, _cD0,
		_cD0Parasite, 
		_kExcr, _cDRough, _cDCool, 
		_cDWindshield, _kFactorPolar, _cDTotalCurrent,
		_oswald;

	MyArray _cDWaveList = new MyArray(Unit.ONE);

	private int ne;
	private double 
		lambdaW, arW, bW, 
		mach, phi25, tc, 
		dihedral, wingletHeight, _e, f,
		cLAlphaW, cLAlphaHT, macW,
		sW, sHT, depsdalpha = 0.,
		dzH, dxH, niHT, etaHT,
		xacWPercentMAC, xacWBRF, xa;

	private double _cLAlphaFixed;
	private double _neutralPointXCoordinateMRF;
	private double _cMCLFixed;
	private double _cMAlphaFixed;
	private Map<ComponentEnum, Double> _cD0Map = new HashMap<ComponentEnum, Double>();
	//	private Map<DragPolarPoint, Double> _maxEfficiencyPoint = new HashMap<DragPolarPoint, Double>();
	//	private Map<DragPolarPoint, Double> _minPowerPoint = new HashMap<DragPolarPoint, Double>();
	//	private Map<DragPolarPoint, Double> _maxRangePoint = new HashMap<DragPolarPoint, Double>();

	private double eMax, eP, eA;
	private double cLE, cLP, cLA;
	private double cDE, cDP, cDA;
	private double vE, vP, vA;
	private double pE, pP, pA;
	private double dE, dP, dA;
	private DragPolarPoint maxEfficiencyPoint;
	private DragPolarPoint minPowerPoint;
	private DragPolarPoint maxRangePoint;
	private double _cD0Total;
	private double[] [] alphaArrayPlotWingBody;
	private double[] [] cLArrayPlotWingBody;
	private String subfolderPathCLAlpha;
	
	private boolean subfolderPathCeck = true;

	public ACAerodynamicsManager() {
		_type = AnalysisTypeEnum.AERODYNAMIC;
		_name = "Aerodynamics";
	}

	public ACAerodynamicsManager(Aircraft aircraft) {
		this();
		_theAircraft = aircraft;
	}

	public ACAerodynamicsManager(
			Aircraft aircraft,
			OperatingConditions operatingConditions) {
		this(aircraft);
		initialize(operatingConditions);
	}

	public void initialize(OperatingConditions operatingConditions) {

		_theOperatingConditions = operatingConditions;

		_eWhole = new Double[_theOperatingConditions.get_cL().length];
		_cD = new Double[_theOperatingConditions.get_cL().length];
		_cL = new Double[_theOperatingConditions.get_cL().length];
		_cY = new Double[_theOperatingConditions.get_cL().length];
		
		updateVariables(_theAircraft);
	}
	
	public void updateVariables(Aircraft aircraft) {

		initializeComponentsAerodynamics(_theOperatingConditions, aircraft);

		ne = aircraft.get_powerPlant().get_engineNumber();
		lambdaW = aircraft.get_wing().get_taperRatioEquivalent().doubleValue();
		arW = aircraft.get_wing().get_aspectRatio().doubleValue();
		bW = aircraft.get_wing().get_span().getEstimatedValue();
		phi25 = aircraft.get_wing().get_sweepQuarterChordEq().doubleValue(SI.RADIAN);
		tc = aircraft.get_wing().getGeometry().getCalculateThickness().get_methodsMap().get(MethodEnum.INTEGRAL_MEAN);
		dihedral = aircraft.get_wing().get_dihedralMean().getEstimatedValue();

		// TODO: remove this
		aircraft.get_wing().setHasWinglet(true);
		aircraft.get_wing().set_wingletHeight(Amount.valueOf(0.6, SI.METER));
		wingletHeight = aircraft.get_wing().get_wingletHeight().getEstimatedValue();

		macW = aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue();
		sW = aircraft.get_wing().get_surface().getEstimatedValue();
		sHT = aircraft.get_HTail().get_surface().getEstimatedValue();
		dzH = aircraft.get_HTail().get_Z0().minus(aircraft.get_wing().get_Z0()).getEstimatedValue();
		dxH = aircraft.get_HTail().get_ACw_ACdistance().getEstimatedValue();
		niHT = aircraft.get_HTail().get_volumetricRatio();

		mach = _theOperatingConditions.get_machCurrent();
		_machDragPolar.add(mach - 0.1);
		_machDragPolar.add(mach + (1-Math.pow(mach, 0.37)));
		_machDragPolar.add(mach);

		try {
			etaHT = aircraft.get_HTail().getAerodynamics().get_dynamicPressureRatio();
			cLAlphaW = aircraft.get_wing().getAerodynamics().getCalculateCLAlpha().get_methodsMap().get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);
			cLAlphaHT = aircraft.get_HTail().getAerodynamics().getCalculateCLAlpha().get_methodsMap().get(MethodEnum.ANDERSON_COMPRESSIBLE_SUBSONIC);

			xacWPercentMAC = aircraft.get_wing().getAerodynamics()
					.getCalculateXAC().get_methodMapMRF().get(MethodEnum.DEYOUNG_HARPER).getEstimatedValue()
					/ aircraft.get_wing().get_meanAerodChordActual().getEstimatedValue();
			xacWBRF = aircraft.get_wing().getAerodynamics()
					.getCalculateXAC().get_methodMapLRF().get(MethodEnum.DEYOUNG_HARPER).getEstimatedValue()
					+ aircraft.get_wing().get_X0().getEstimatedValue();

			xa = (aircraft.get_theBalance().get_cgMTOM().get_xBRF().getEstimatedValue() - xacWBRF)
					/macW;

		} catch (NullPointerException e) { }
	}

	/** 
	 * Evaluate drag polar (lift coefficient is an independent variable)
	 * 
	 * @author Lorenzo Attanasio
	 */
	public void calculateDragPolar() {

		Double[] cD = new Double[_cL.length];
		_oswald = calculateOswald(0., MethodEnum.HOWE);

		// Iterate over mach numbers
		for (int j=0; j < _machDragPolar.size(); j++) {

			// Iterate over lift coefficients
			for(int i=0; i < _theOperatingConditions.get_cL().length; i++) {

				_cL[i] = _theOperatingConditions.get_cL()[i];
				_cLcurrent = _cL[i];

				_eWhole[i] = calculateOswald(_machDragPolar.get(j), MethodEnum.HOWE);
				cD[i] = calculateCD(_cD0Total, _cL[i], _eWhole[i], _machDragPolar.get(j));
			}

			if (_machDragPolar.get(j) == mach) {
				_cD = cD.clone();
			}

			_cDMap.put(_machDragPolar.get(j), cD.clone());
		}
	}

	/** 
	 * Evaluate drag coefficient of the whole aircraft
	 * 
	 * @author Lorenzo Attanasio
	 * @param cL
	 * @param eWhole
	 * @return
	 */
	public Double calculateCD(Double cD0, Double cL, Double eWhole, Double mach){

		if (_cD0Total == 0.0) calculateCD0Total();
		double cDWave = _theAircraft.get_wing().getAerodynamics().getCalculateCdWaveDrag().lockKorn(cL,mach);
		_cDWaveList.add(cDWave);
		_kFactorPolar = (Math.PI*arW*eWhole);

		// Total drag
		_cDTotalCurrent = DragCalc.calculateCDTotal(_cD0Total, cL, arW, eWhole, mach, cDWave);

		return _cDTotalCurrent;

	}

	public double calculateCD0Parasite() {

//		double d = _theAircraft.get_fuselage().getAerodynamics().get_cD0Parasite().doubleValue();
//		d += _theAircraft.get_wing().getAerodynamics().get_cD0Parasite().doubleValue();
//		d += _theAircraft.get_theNacelles().get_cD0Parasite().doubleValue();
//		d += _theAircraft.get_HTail().getAerodynamics().get_cD0Parasite().doubleValue();
//		d += _theAircraft.get_VTail().getAerodynamics().get_cD0Parasite().doubleValue();
//		_cD0Parasite = Double.valueOf(d); 
				
		_cD0Parasite =  _theAircraft.get_fuselage().getAerodynamics().get_cD0Parasite()
				+ _theAircraft.get_wing().getAerodynamics().get_cD0Parasite()
				+ _theAircraft.get_theNacelles().get_cD0Parasite()
				+ _theAircraft.get_HTail().getAerodynamics().get_cD0Parasite()
				+ _theAircraft.get_VTail().getAerodynamics().get_cD0Parasite();
		
		return _cD0Parasite.doubleValue();
		
	}

	public double calculateCD0Total() {
		_kExcr = DragCalc.calculateKExcrescences(_theAircraft.get_sWetTotal()); 

		calculateCD0Parasite();

		_cD0 = _theAircraft.get_fuselage().getAerodynamics().get_cD0Total() +
				_theAircraft.get_wing().getAerodynamics().get_cD0Total() +
				_theAircraft.get_theNacelles().get_cD0Total() +
				_theAircraft.get_HTail().getAerodynamics().get_cD0Total() +
				_theAircraft.get_VTail().getAerodynamics().get_cD0Total();

		_cDRough = AerodynamicCalc.calculateRoughness(_cD0);
		_cDCool = AerodynamicCalc.calculateCoolings(_cD0Parasite);

		_cD0Total = (_cD0 + _cDRough + _cDCool);

		if (_theOperatingConditions != null 
				&& mach == _theOperatingConditions.get_machCurrent()) {
			_cD0Map.put(ComponentEnum.FUSELAGE, _theAircraft.get_fuselage().getAerodynamics().get_cD0Total());
			_cD0Map.put(ComponentEnum.WING, _theAircraft.get_wing().getAerodynamics().get_cD0Total());
			_cD0Map.put(ComponentEnum.NACELLE, _theAircraft.get_theNacelles().get_cD0Total());
			_cD0Map.put(ComponentEnum.HORIZONTAL_TAIL, _theAircraft.get_HTail().getAerodynamics().get_cD0Total());
			_cD0Map.put(ComponentEnum.VERTICAL_TAIL, _theAircraft.get_VTail().getAerodynamics().get_cD0Total());
			_cD0Map.put(ComponentEnum.ALL, _cD0Total);
		}

		return _cD0Total; 
	}

	/**
	 * Locate E,P,A points on drag polar
	 * 
	 * @param arW
	 * @param e
	 * @param cD0
	 * @param rho
	 * @param W
	 * @param S
	 */
	public void calculateDragPolarPoints(double arW, double e, double cD0, double rho, double W, double S) {
		calculateMaximumEfficiency(arW, e, cD0, rho, W, S);
		calculateMinimumPower(eMax, cLE, cD0, vE, W);
		calculateMaximumRange(eMax, cLE, cD0, vE);
		maxEfficiencyPoint = new DragPolarPoint(eMax, cLE, cDE, vE, pE, dE);
		minPowerPoint = new DragPolarPoint(eP, cLP, cDP, vP, pP, dE);
		maxRangePoint = new DragPolarPoint(eA, cLA, cDA, vA, pA, dA);
	}

	private void calculateMaximumEfficiency(double ar, double e, double cD0, double rho, double W, double S) {
		eMax = Math.sqrt(Math.PI*ar*e/(4*cD0) );
		cLE = Math.sqrt(Math.PI*ar*e*cD0);
		cDE = 2*cD0;
		dE = W/eMax;
		vE = Math.sqrt(2*W/(rho*S*cLE));
	}

	private void calculateMinimumPower(double eMax, double cLE, double cD0, double vE, double W) {
		eP = Math.sqrt(0.75)*eMax;
		cLP = Math.sqrt(3)*cLE;
		cDP = 4*cD0;
		dP = dE*2./Math.sqrt(3);
		vP = vE/Math.pow(3, 0.25);
		pP = W*vP/eP;
		pE = pP*Math.pow(27, 0.25)/2.;
	}

	private void calculateMaximumRange(double eMax, double cLE, double cD0, double vE) {
		eA = Math.sqrt(0.75)*eMax;
		cLA = cLE/Math.sqrt(3);
		cDA = (4./3.)*cD0;
		dA = dE*2./Math.sqrt(3);
		vA = vE*Math.pow(3, 0.25);
		pA = Math.sqrt(3)*pP;
	}

	/** 
	 * Methods for Oswald factor evaluation refer to whole aircraft
	 * 
	 * @author Lorenzo Attanasio
	 * @param method
	 * @return
	 */
	public Double calculateOswald(double mach, MethodEnum method) {

		switch(method){
		case HOWE : { // page 7 DLR pdf
			f = 0.005 * (1 + 1.5*Math.pow(lambdaW-0.6, 2));

			return 1/
					( (1+0.12*Math.pow(mach,2)) 
							* (1 + (0.142 + f * arW * Math.pow(10*tc,0.33))/Math.pow(Math.cos(phi25),2) +
									(0.1*(3*ne + 1))/Math.pow(4+arW,0.8) ));
		}

		case DLR_NITA_SCHOLZ :  { // page 9 DLR pdf, Good results

			double ae = -0.001521, be = 10.82, 
					kef, e_theo, keD0, 
					lambda_opt,
					delta_lambda, keM;

			lambda_opt = _theAircraft.get_wing().get_taperRatioOpt();
			delta_lambda = -0.357 + lambda_opt;
			//			_f = 0.0524*Math.pow(_lambda - delta_lambda,4) 
			//					- 0.15*Math.pow(_lambda - delta_lambda,3) 
			//					+ 0.1659*Math.pow(_lambda - delta_lambda, 2) 
			//					- 0.0706*(_lambda - delta_lambda) + 0.0119;
			f = 0.0524*Math.pow(1 - delta_lambda,4) 
					- 0.15*Math.pow(1 - delta_lambda,3) 
					+ 0.1659*Math.pow(1 - delta_lambda, 2) 
					- 0.0706*(1 - delta_lambda) + 0.0119;

			e_theo = 1/(1 + f*arW);

			kef = 1 - 2*Math.pow(
					_theAircraft.get_fuselage().get_sectionCylinderHeight().getEstimatedValue()/bW
					, 2);

			switch(_theAircraft.get_typeVehicle()) {
			case JET : keD0 = 0.873; break;
			case BUSINESS_JET : keD0 = 0.864; break;
			case TURBOPROP: keD0 = 0.804; break;
			case GENERAL_AVIATION: keD0 = 0.804; break;
			case FIGHTER: keD0 = 0.8; break; // ???
			default: keD0 = 0.8; break;
			}

			if (mach > 0.3) {
				keM = ae*Math.pow((mach/0.3 - 1), be) + 1;
			} else {
				keM = 1.;
			}

			_e = e_theo*kef*keD0*keM;

			// Kroo method: needs whole aircraft CD0 
			//			double Q = 1/(e_theo*kef), P = 0.38*CD0;
			//			double eKroo = keM/(Q + P*_AR);

			double kWL = 2.83;

			if (_theAircraft.get_wing().isHasWinglet() == true) {
				_e = _e*Math.pow(1+(2/kWL)*(wingletHeight/bW),2);
			}

			double keGamma = Math.pow(
					Math.cos(_theAircraft.get_wing().get_dihedralMean().to(SI.RADIAN).getEstimatedValue()),
					-2);
			//			double keGamma = Math.pow((1 + (1/kWL)*(1/Math.cos(_dihedral) - 1)),2);
			double eWingletGamma = _e*keGamma;
			return eWingletGamma;
		} 

		case GROSU : { // page 3 DLR pdf
			return 1/(1.08 + (0.028*tc/Math.pow(_cLcurrent,2))*Math.PI*arW);
		}

		case RAYMER : { // Raymer page 298 (157 pdf)
			if (_theAircraft.get_wing().get_sweepLEEquivalent().getEstimatedValue()> 5*Math.PI/180.){
				return 4.61*(1 - 0.045
						*Math.pow(_theAircraft.get_wing().get_aspectRatio(),0.68))*
						Math.pow(
								Math.cos(_theAircraft.get_wing().get_sweepLEEquivalent().getEstimatedValue())
								,0.15) - 3.1;
			} else {
				return 1.78*(1 - 0.045
						*Math.pow(_theAircraft.get_wing().get_aspectRatio().doubleValue()
								,0.68)) - 0.64;
			}
		}

		default: return 0.0;

		}
	} // end of calculateOswald

	public double calculateDepsDalpha(Aircraft aircraft) {

		double karW = 1./arW - 1./(1.+pow(arW,1.7));
		double kLambdaW = (10.-3.*lambdaW)/7.;
		double kMAC4 = (1.-dzH/bW)
				/Math.pow(2.*dxH/bW, 1/3);

		depsdalpha = 4.44*pow(
				karW*kLambdaW*kMAC4*
				sqrt(cos(phi25))
				, 1.19);

		return depsdalpha;
	}

	public double calculateCLAlphaFixed(Aircraft aircraft) {
		_cLAlphaFixed = cLAlphaW
				* (1 + etaHT*(cLAlphaHT*sHT/(cLAlphaW*sW))
						*(1-depsdalpha));

		return _cLAlphaFixed;
	}

	public double calculateNeutralPointXCoordinateMRF(Aircraft aircraft) {
		_neutralPointXCoordinateMRF = xacWPercentMAC 
				+ etaHT*(cLAlphaHT/cLAlphaW)*niHT*(1-depsdalpha);
		return _neutralPointXCoordinateMRF;
	}

	//	public double calculateCLq(){
	//
	//	}
	//

	//	public double calculateCM0(MyAircraft aircraft) {
	//		updateVariables(aircraft);
	//		
	//		_cM0 = aircraft.get_wing().getAerodynamics().getCalculateCm0();
	//				aircraft.get_fuselage().getAerodynamics().getCalculateCm0().get_methodMap().get(MyMethodEnum.MULTHOPP);
	//	}

	public double calculateCMCLFixed(Aircraft aircraft) {
		_cMCLFixed = xa 
				- etaHT*(cLAlphaHT/cLAlphaW)
				* (1-depsdalpha)*niHT;

		return _cMCLFixed;
	}

	public double calculateCMAlphaFixed(Aircraft aircraft) {
		_cMAlphaFixed = (xa - etaHT*(cLAlphaHT/cLAlphaW)*(1-depsdalpha)*niHT)
				*calculateCLAlphaFixed(aircraft);

		return _cMAlphaFixed;
	}
	
	/**
	 * This class evaluates the CL vs Alpha curve both in linear and non linear trait starting from 
	 * the CL vs Alpha curve of isolated wing and introducing an influence factor from fuselage.
	 * see--> Sforza p.64
	 * 
	 * WARNING --> it is necessary to call LSAerodynamicsManager.CalcCLAtAlpha first
	 * 
	 * @param Amount<Angle> alphaBody. It is the angle between the direction of asimptotic 
     * velocity and the reference line of fuselage.
	 * @author Manuela Ruocco
	 */

	public double calculateCLAtAlphaWingBody(Amount<Angle> alphaBody, MyAirfoil meanAirfoil, boolean printCheck){
		if (alphaBody.getUnit() == NonSI.DEGREE_ANGLE) 
			alphaBody = alphaBody.to(SI.RADIAN);

		double cLAlphaWingBody, cLAlphaWing, cLMaxWingClean, cLZeroWing, alphaZeroLift,
			cLZeroWingBody,alphaStarDouble, cLActual = 0, cLStar;
		double a, b, c, d;
		Amount<Angle> alphaMaxWingClean, alphaStar, alphaMaxWingBody;
		
		alphaMaxWingClean =  _theAircraft.get_wing().getAerodynamics().get_alphaMaxClean();
		cLMaxWingClean = _theAircraft.get_wing().getAerodynamics().get_cLMaxClean();
		cLAlphaWing = _theAircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();
		//alphaStar = meanAirfoil.getAerodynamics().get_alphaStar();
		cLStar = _theAircraft.get_wing().getAerodynamics().getcLStarWing();
		LSAerodynamicsManager theManager = _theAircraft.get_wing().getAerodynamics();
		LSAerodynamicsManager.CalcAlpha0L theAlphaZeroLiftCalculator = theManager.new CalcAlpha0L();
		alphaZeroLift = theAlphaZeroLiftCalculator.integralMeanExposedWithTwist().getEstimatedValue();
		//alphaZeroLift = _theAircraft.get_wing().getAerodynamics().getAlphaZeroLiftWingClean();
		cLZeroWing = _theAircraft.get_wing().getAerodynamics().getcLAlphaZero();

		cLAlphaWingBody = _theAircraft
				.get_fuselage()
				.getAerodynamics()
				.calculateCLAlphaFuselage(cLAlphaWing);

		cLZeroWingBody = -cLAlphaWingBody * alphaZeroLift;

		double alphaTempWing = (cLMaxWingClean - cLZeroWing)/cLAlphaWing;
		double alphaTempWingBody = (cLMaxWingClean - cLZeroWingBody)/cLAlphaWingBody;
		double deltaAlphaTemp = alphaTempWing - alphaTempWingBody;
		alphaMaxWingBody = Amount.valueOf(alphaMaxWingClean.getEstimatedValue() - Math.abs(deltaAlphaTemp), SI.RADIAN);
		alphaStarDouble = (cLStar-cLZeroWingBody)/cLAlphaWingBody;

		//System.out.println(" alpha max clean " + alphaMaxWingClean.to(NonSI.DEGREE_ANGLE).getEstimatedValue());
		//System.out.println(" cl max wing clean " + cLMaxWingClean);
		//System.out.println(" cl alpha wing " + cLAlphaWing);
		if (printCheck ==true){
		System.out.println("\n -----------WING BODY-------------- ");
		System.out.println(" alpha max " + alphaMaxWingBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue());
		System.out.println(" alpha star " + alphaStarDouble*57.3);
		System.out.println(" cL max " + cLMaxWingClean);
		System.out.println(" cL alpha wing body " + cLAlphaWingBody);
		System.out.println(" alpha zero lift " + alphaZeroLift*57.3);
		printCheck=false;
		}

		double alphaWing = alphaBody.getEstimatedValue() +
				_theAircraft.get_wing().get_iw().getEstimatedValue();

	
		
		if ( alphaWing < alphaStarDouble ){
			cLActual = cLAlphaWingBody * alphaWing + cLZeroWingBody;	
			return cLActual;
		}

		else{
			double[][] matrixData = { {Math.pow(alphaMaxWingBody.getEstimatedValue(), 3),
				Math.pow(alphaMaxWingBody.getEstimatedValue(), 2),
				alphaMaxWingBody.getEstimatedValue(),1.0},
					{3* Math.pow(alphaMaxWingBody.getEstimatedValue(), 2),
					2*alphaMaxWingBody.getEstimatedValue(), 1.0, 0.0},
					{3* Math.pow(alphaStarDouble, 2), 
						2*alphaStarDouble, 1.0, 0.0},
					{Math.pow(alphaStarDouble, 3), 
							Math.pow(alphaStarDouble, 2),
							alphaStarDouble,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
			double [] vector = {cLMaxWingClean, 0,cLAlphaWingBody, cLStar};

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			a = solSystem[0];
			b = solSystem[1];
			c = solSystem[2];
			d = solSystem[3];

			double clActual = a * Math.pow(alphaWing, 3) + 
					b * Math.pow(alphaWing, 2) + 
					c * alphaWing+ d;

			return clActual;

		}

	}


	/** 
	 * This function plot CL vs Alpha curve (wing-body) using 30 value of alpha between alpha=- 2 deg and
	 * alphaMax+2. 
	 * 
	 * @author Manuela Ruocco
	 */
	public void PlotCLvsAlphaCurve(MyAirfoil meanAirfoil){
		
		double alphaFirst = -2.0 ;
		Amount<Angle> alphaActual;
		Amount<Angle> alphaTemp = Amount.valueOf(0.0, SI.RADIAN);
		int nPoints = 40;
		double cLAlphaWingBody, cLAlphaWing, cLMaxWingClean, cLZeroWing, alphaZeroLift, cLZeroWingBody,
		cLActual = 0, cLStar;
		double aWB, bWB, cWB, dWB, a, b, c, d;
		Amount<Angle> alphaMaxWingClean, alphaStar, alphaMaxWingBody;

		alphaMaxWingClean =  _theAircraft.get_wing().getAerodynamics().get_alphaMaxClean();
		cLMaxWingClean = _theAircraft.get_wing().getAerodynamics().get_cLMaxClean();
		cLAlphaWing = _theAircraft.get_wing().getAerodynamics().getcLLinearSlopeNB();
		alphaStar = meanAirfoil.getAerodynamics().get_alphaStar();
		alphaZeroLift = _theAircraft.get_wing().getAerodynamics().getAlphaZeroLiftWingClean();
		cLZeroWing = _theAircraft.get_wing().getAerodynamics().getcLAlphaZero();

		cLAlphaWingBody = _theAircraft
				.get_fuselage()
				.getAerodynamics()
				.calculateCLAlphaFuselage(cLAlphaWing);

		cLZeroWingBody = -cLAlphaWingBody * alphaZeroLift;

		double alphaTempWing = (cLMaxWingClean - cLZeroWing)/cLAlphaWing;
		double alphaTempWingBody = (cLMaxWingClean - cLZeroWingBody)/cLAlphaWingBody;
		double deltaAlphaTemp = alphaTempWing - alphaTempWingBody;
		alphaMaxWingBody = Amount.valueOf(alphaMaxWingClean.getEstimatedValue() - Math.abs(deltaAlphaTemp), SI.RADIAN);
		double alphaMaxDeg = alphaMaxWingBody.to(NonSI.DEGREE_ANGLE).getEstimatedValue();
		cLStar = cLAlphaWing * alphaStar .getEstimatedValue() + cLZeroWing;	
		
		alphaArrayPlotWingBody = new double [2][nPoints];
		double [] alphaArray = MyArrayUtils.linspace(alphaFirst,alphaMaxDeg + 2, nPoints);
		
		for (int i=0 ; i < nPoints ; i++){
			alphaArrayPlotWingBody [0][i] = alphaArray[i];
			alphaArrayPlotWingBody [1][i] = alphaArray[i];
		}
		cLArrayPlotWingBody = new double [2][nPoints];
		double alphaStarWingBody =(cLStar - cLZeroWingBody)/cLAlphaWingBody;
		
		double[][] matrixDataWB = { {Math.pow(alphaMaxWingBody.getEstimatedValue(), 3),
			Math.pow(alphaMaxWingBody.getEstimatedValue(), 2),
			alphaMaxWingBody.getEstimatedValue(),1.0},
				{3* Math.pow(alphaMaxWingBody.getEstimatedValue(), 2),
				2*alphaMaxWingBody.getEstimatedValue(), 1.0, 0.0},
				{3* Math.pow(alphaStarWingBody, 2), 2*alphaStarWingBody, 1.0, 0.0},
				{Math.pow(alphaStarWingBody, 3),Math.pow(alphaStarWingBody, 2),alphaStarWingBody,1.0}};
		RealMatrix mWB = MatrixUtils.createRealMatrix(matrixDataWB);
		double [] vectorWB = {cLMaxWingClean, 0,cLAlphaWingBody, cLStar};

		double [] solSystemWB = MyMathUtils.solveLinearSystem(mWB, vectorWB);

		aWB = solSystemWB[0];
		bWB = solSystemWB[1];
		cWB = solSystemWB[2];
		dWB = solSystemWB[3];
		
		double[][] matrixData = { {Math.pow(alphaMaxWingClean.getEstimatedValue(), 3),
			Math.pow(alphaMaxWingClean.getEstimatedValue(), 2),
			alphaMaxWingClean.getEstimatedValue(),1.0},
				{3* Math.pow(alphaMaxWingClean.getEstimatedValue(), 2),
				2*alphaMaxWingClean.getEstimatedValue(), 1.0, 0.0},
				{3* Math.pow(alphaStar.getEstimatedValue(), 2), 
					2*alphaStar.getEstimatedValue(), 1.0, 0.0},
				{Math.pow(alphaStar.getEstimatedValue(), 3), 
						Math.pow(alphaStar.getEstimatedValue(), 2),
						alphaStar.getEstimatedValue(),1.0}};
		RealMatrix m = MatrixUtils.createRealMatrix(matrixData);
		double [] vector = {cLMaxWingClean, 0,cLAlphaWing, cLStar};

		double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

		a = solSystem[0];
		b = solSystem[1];
		c = solSystem[2];
		d = solSystem[3];
		
		
		String folderPath = MyConfiguration.currentDirectoryString + File.separator + "out" + File.separator;
		if(subfolderPathCeck)
		subfolderPathCLAlpha = JPADStaticWriteUtils.createNewFolder(folderPath + "CL alpha WingBody " + File.separator);

		for ( int i=0 ; i< nPoints ; i++){
			alphaActual = Amount.valueOf(toRadians(alphaArrayPlotWingBody[0][i]), SI.RADIAN);
			if ( alphaActual.getEstimatedValue() < alphaStar.getEstimatedValue() ){ 
				cLArrayPlotWingBody[1][i] = cLAlphaWingBody * alphaActual.getEstimatedValue() + cLZeroWingBody;
				cLArrayPlotWingBody[0][i] = cLAlphaWing * alphaActual.getEstimatedValue() + cLZeroWing;
			}
			else {
				cLArrayPlotWingBody[1][i] = aWB * Math.pow(alphaActual.getEstimatedValue(), 3) + 
						bWB * Math.pow(alphaActual.getEstimatedValue(), 2) + 
						cWB * alphaActual.getEstimatedValue() + dWB;
				cLArrayPlotWingBody[0][i] = a * Math.pow(alphaActual.getEstimatedValue(), 3) + 
						b * Math.pow(alphaActual.getEstimatedValue(), 2) + 
						c * alphaActual.getEstimatedValue() + d;
			}
				
		}
			
		String [] legend = new String [2];
		legend[0] = "CL vs Alpha Isolated Wing";
		legend[1] = "CL vs Alpha Wing Body";
		
		MyChartToFileUtils.plot(
				alphaArrayPlotWingBody,	cLArrayPlotWingBody, 
				null, null , null , null ,					    // axis with limits
				"alpha_W", "CL", "deg", "",legend, 	   				
				subfolderPathCLAlpha, "CL vs Alpha wing body");
	}

	/** 
	 * This function plot CL vs Alpha curve (wing-body) using 30 value of alpha between alpha=- 2 deg and
	 * alphaMax+2. This function accepts in input subfolder path name. If you want to use the default folder
	 * path ("CL alpha WingBody") you can use PlotCLvsAlphaCurve(MyAirfoil meanAirfoil).
	 * 
	 * @author Manuela Ruocco
	 */
	public void PlotCLvsAlphaCurve(MyAirfoil meanAirfoil, String subfolderPath){
		this.subfolderPathCLAlpha = subfolderPath;
		subfolderPathCeck = false;
		PlotCLvsAlphaCurve(meanAirfoil);
		
	};


	private void initializeComponentsAerodynamics(
			OperatingConditions conditions, 
			Aircraft aircraft) {

		try {
			aircraft.get_fuselage().initializeAerodynamics(conditions,aircraft);
			aircraft.get_wing().initializeAerodynamics(conditions, aircraft);
			aircraft.get_HTail().initializeAerodynamics(conditions, aircraft);
			aircraft.get_VTail().initializeAerodynamics(conditions, aircraft);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Evaluate Fuselage, Wing, HTail, VTail parameters
	 * TO DO: update this documentation 
	 */
	public void calculateComponentsParameters(Aircraft aircraft, Amount<Angle> alphaRoot) {

		// Evaluate all fuselage parameters
		if (aircraft.get_fuselage() != null)
			aircraft.get_fuselage().getAerodynamics().calculateAll();

		// Evaluate all wing parameters
		if (aircraft.get_wing() != null) {
			aircraft.get_wing().getAerodynamics().set_cLCurrent(aircraft.get_performances().get_cruiseCL());
			aircraft.get_wing().getAerodynamics().calculateAll(_theOperatingConditions.get_machCurrent(), alphaRoot);
		} 

		// Evaluate all Htail parameters
		if (aircraft.get_HTail() != null) {
			// TODO: change Htail CL
			aircraft.get_HTail().getAerodynamics().set_cLCurrent(aircraft.get_performances().get_cruiseCL());
			aircraft.get_HTail().getAerodynamics().calculateAll(_theOperatingConditions.get_machCurrent(),alphaRoot);
		}

		// Evaluate all Vtail parameters
		if (aircraft.get_VTail() != null) {
			aircraft.get_VTail().getAerodynamics().set_cLCurrent(0.0);
			aircraft.get_VTail().getAerodynamics().calculateAll(_theOperatingConditions.get_machCurrent(),alphaRoot);
		}

		// Evaluate all canard parameters
		if (aircraft.get_Canard() != null) {
			aircraft.get_Canard().getAerodynamics().set_cLCurrent(aircraft.get_performances().get_cruiseCL());
			aircraft.get_Canard().getAerodynamics().calculateAll(_theOperatingConditions.get_machCurrent(),alphaRoot);
		}

		// Evaluate all nacelle parameters
		if (aircraft.get_theNacelles() != null) {
			aircraft.get_theNacelles().calculateAerodynamics();
		}
	}

	/*
	 * TO DO: write me
	 */
	public void calculateAll(OperatingConditions conditions) {
		updateVariables(_theAircraft);
		calculateComponentsParameters(_theAircraft, conditions.get_alphaCurrent());
		calculateCD0Total();
		calculateDragPolar();
		calculateDragPolarPoints(arW, _eWhole[0], _cD0, 
				conditions.get_densityCurrent().getEstimatedValue(), 
				_theAircraft.get_weights().get_MTOW().getEstimatedValue(), 
				_theAircraft.get_wing().get_surface().getEstimatedValue());

		calculateDepsDalpha(_theAircraft);
		calculateCLAlphaFixed(_theAircraft);
		calculateNeutralPointXCoordinateMRF(_theAircraft);
		calculateCMCLFixed(_theAircraft);
		calculateCMAlphaFixed(_theAircraft);
	}

	public Double[] get_eWhole() {
		return _eWhole;
	}

	public void set_eWhole(Double[] _eWhole) {
		this._eWhole = _eWhole;
	}

	public Double get_cD0() {
		return _cD0;
	}

	public Double get_cDCool() {
		return _cDCool;
	}

	public Double get_cDRough() {
		return _cDRough;
	}

	public Double get_cDWindshield() {
		return _cDWindshield;
	}

	public Double[] get_cD() {
		return _cD;
	}


	public Double[] get_cL() {
		return _cL;
	}


	public void setConfiguration(Aircraft aircraft) {
		_theAircraft = aircraft;
	}

	public Double get_kExcr() {
		return _kExcr;
	}


	public Double get_cDTotalCurrent() {
		return _cDTotalCurrent;
	}


	@Override
	public AnalysisTypeEnum get_type() {
		return _type;
	}

	public void set_type(AnalysisTypeEnum _type) {
		this._type = _type;
	}


	@Override
	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}


	public MyArray get_cDWaveList() {
		return _cDWaveList;
	}


	public Double get_kFactorPolar() {
		return _kFactorPolar;
	}


	public void set_kFactorPolar(Double _kFactorPolar) {
		this._kFactorPolar = _kFactorPolar;
	}

	public Double getDepsdalpha() {
		return Double.valueOf(depsdalpha);
	}

	public Double get_cLAlphaFixed() {
		return Double.valueOf(_cLAlphaFixed);
	}

	public Double get_neutralPointXCoordinateMRF() {
		return Double.valueOf(_neutralPointXCoordinateMRF);
	}

	public Double get_cMCLFixed() {
		return Double.valueOf(_cMCLFixed);
	}

	public Double get_cMAlphaFixed() {
		return Double.valueOf(_cMAlphaFixed);
	}

	public String getId() {
		return id;
	}

	public Map<Double, Double[]> get_cDMap() {
		return _cDMap;
	}

	public Map<ComponentEnum, Double> get_cD0Map() {
		return _cD0Map;
	}

	public Double getcDA() {
		return Double.valueOf(cDA);
	}

	public void setcDA(double cDA) {
		this.cDA = cDA;
	}

	public Double geteMax() {
		return Double.valueOf(eMax);
	}

	public Double geteP() {
		return Double.valueOf(eP);
	}

	public Double geteA() {
		return Double.valueOf(eA);
	}

	public Double getcLE() {
		return Double.valueOf(cLE);
	}

	public Double getcLP() {
		return Double.valueOf(cLP);
	}

	public Double getcLA() {
		return Double.valueOf(cLA);
	}

	public Double getcDE() {
		return Double.valueOf(cDE);
	}

	// TODO: convert return types in Double
	public double getcDP() {
		return cDP;
	}

	public double getvE() {
		return vE;
	}

	public double getvP() {
		return vP;
	}

	public double getvA() {
		return vA;
	}

	public double getpE() {
		return pE;
	}

	public double getpP() {
		return pP;
	}

	public double getpA() {
		return pA;
	}

	public double getdE() {
		return dE;
	}

	public double getdP() {
		return dP;
	}

	public double getdA() {
		return dA;
	}

	public DragPolarPoint getMaxEfficiencyPoint() {
		return maxEfficiencyPoint;
	}

	public DragPolarPoint getMinPowerPoint() {
		return minPowerPoint;
	}

	public DragPolarPoint getMaxRangePoint() {
		return maxRangePoint;
	}

	public Double get_oswald() {
		return _oswald;
	}
	
	public AerodynamicDatabaseReader get_aerodynamicDatabaseReader() {
		return _aerodynamicDatabaseReader;
	}

	public void set_aerodynamicDatabaseReader(AerodynamicDatabaseReader _aerodynamicDatabaseReader) {
		this._aerodynamicDatabaseReader = _aerodynamicDatabaseReader;
	}

	public HighLiftDatabaseReader get_highLiftDatabaseReader() {
		return _highLiftDatabaseReader;
	}

	public void set_highLiftDatabaseReader(HighLiftDatabaseReader _highLiftDatabaseReader) {
		this._highLiftDatabaseReader = _highLiftDatabaseReader;
	}
	
} // end of class

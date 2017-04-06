package aircraft.components.liftingSurface;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sqrt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import analyses.OperatingConditions;
import analyses.liftingsurface.LSAerodynamicsManager;
import analyses.liftingsurface.LSAerodynamicsManager;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.ConditionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.GeometryCalc;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class LiftingSurface implements ILiftingSurface {

	private String _id = null;
	private ComponentEnum _type;

	private LiftingSurface _exposedWing;
	private Double _kExcr = 0.0; 
	private int _numberOfEngineOverTheWing = 0; 
	
	private Map<ConditionEnum, LSAerodynamicsManager> _theAerodynamicsCalculatorMap;
	// THIS HAS TO BE CHANGED IN LSAerodynamicCalculator
	private LSAerodynamicsManager _theAerodynamics;
	
	private Double _positionRelativeToAttachment;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Angle> _riggingAngle = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
	
	private LiftingSurfaceCreator _liftingSurfaceCreator;

	private AerodynamicDatabaseReader _aeroDatabaseReader;
	private HighLiftDatabaseReader _highLiftDatabaseReader;
	
	private Double _thicknessMean;
	private Double _formFactor;
	private Double _massCorrectionFactor = 1.0;
	private Amount<Mass> _mass, _massReference, _massEstimated;
	private CenterOfGravity _cg;
	private Amount<Length> _xCG, _yCG, _zCG;
	Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	Double[] _percentDifferenceXCG;
	Double[] _percentDifferenceYCG;
	Double[] _percentDifference;
	
	// airfoil span-wise characteristics : 
	private List<Airfoil> _airfoilList;
	private List<Double> _maxThicknessVsY = new ArrayList<>();
	private List<Amount<Length>> _radiusLEVsY = new ArrayList<>();
	private List<Double> _camberRatioVsY = new ArrayList<>();
	private List<Amount<Angle>> _alpha0VsY = new ArrayList<>();
	private List<Amount<Angle>> _alphaStarVsY = new ArrayList<>();
	private List<Amount<Angle>>_alphaStallVsY = new ArrayList<>();
	private List<Amount<?>> _clAlphaVsY = new ArrayList<>(); 
	private List<Double> _cdMinVsY = new ArrayList<>();
	private List<Double> _clAtCdMinVsY = new ArrayList<>();
	private List<Double> _cl0VsY = new ArrayList<>();
	private List<Double> _clStarVsY = new ArrayList<>();
	private List<Double> _clMaxVsY = new ArrayList<>();
	private List<Double> _clMaxSweepVsY = new ArrayList<>();
	private List<Double> _kFactorDragPolarVsY = new ArrayList<>();
	private List<Double> _mExponentDragPolarVsY = new ArrayList<>();
	private List<Double> _cmAlphaQuarteChordVsY = new ArrayList<>();
	private List<Double> _xAcAirfoilVsY = new ArrayList<>();
	private List<Double> _cmACVsY = new ArrayList<>();
	private List<Double> _cmACStallVsY = new ArrayList<>();
	private List<Double> _criticalMachVsY = new ArrayList<>();
	
	//================================================
	// Builder pattern via a nested public static class
	public static class LiftingSurfaceBuilder {

		private String __id = null;
		private ComponentEnum __type;
		private Amount<Length> __xApexConstructionAxes = null; 
		private Amount<Length> __yApexConstructionAxes = null; 
		private Amount<Length> __zApexConstructionAxes = null;
		private LiftingSurfaceCreator __liftingSurfaceCreator;
		private List<Airfoil> __airfoilList;
		private AerodynamicDatabaseReader __aeroDatabaseReader;
		private HighLiftDatabaseReader __highLiftDatabaseReader;
		Map <MethodEnum, Amount<Length>> __xCGMap;
		Map <MethodEnum, Amount<Length>> __yCGMap;
		Map <AnalysisTypeEnum, List<MethodEnum>> __methodsMap;
		Map <MethodEnum, Amount<Mass>> __massMap;
		
		public LiftingSurfaceBuilder(
				String id,
				ComponentEnum type,
				AerodynamicDatabaseReader aeroDatabaseReader,
				HighLiftDatabaseReader highLiftDatabaseReader
				) {
			// required parameter
			this.__id = id;
			this.__type = type;
			this.__aeroDatabaseReader = aeroDatabaseReader;
			this.__highLiftDatabaseReader = highLiftDatabaseReader;
			
			// optional parameters ...
			this.__airfoilList = new ArrayList<Airfoil>(); 
			this.__xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
			this.__yCGMap = new TreeMap<MethodEnum, Amount<Length>>();
			this.__methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
			this.__massMap = new TreeMap<MethodEnum, Amount<Mass>>();
		}

		public LiftingSurfaceBuilder liftingSurfaceCreator(LiftingSurfaceCreator lsc) {
			this.__liftingSurfaceCreator = lsc;
			return this;
		}
		
		public LiftingSurface build() {
			return new LiftingSurface(this);
		}

	}

	private LiftingSurface(LiftingSurfaceBuilder builder) {
		this._id = builder.__id; 
		this._type = builder.__type;
		this._xApexConstructionAxes = builder.__xApexConstructionAxes; 
		this._yApexConstructionAxes = builder.__yApexConstructionAxes; 
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._liftingSurfaceCreator = builder.__liftingSurfaceCreator;
		this._aeroDatabaseReader = builder.__aeroDatabaseReader;
		this._highLiftDatabaseReader = builder.__highLiftDatabaseReader;
		this._airfoilList = builder.__airfoilList;
		this._xCGMap = builder.__xCGMap;
		this._yCGMap = builder.__yCGMap;
		this._methodsMap = builder.__methodsMap;
		this._massMap = builder.__massMap;
		
	}

	public void initializeAerodynamics(OperatingConditions conditions, Aircraft aircraft) {
//		_theAerodynamics = new LSAerodynamicsManager(conditions, this, aircraft);
	}
	
	@Override
	public void calculateThicknessMean() {
		Airfoil meanAirfoil = new Airfoil(calculateMeanAirfoil(this));
		
		_thicknessMean = meanAirfoil.getAirfoilCreator().getThicknessToChordRatio();
	}
	
	@Override
	public void calculateFormFactor(double compressibilityFactor) {

		if(this._type == ComponentEnum.WING)
			// Wing Form Factor (ADAS pag 93 graphic or pag 9 meccanica volo appunti)
			_formFactor = ((1 + 1.2*getThicknessMean()*
					Math.cos(getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN))+
					100*Math.pow(compressibilityFactor,3)*
					(Math.pow(Math.cos(getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN)),2))*
					Math.pow(getThicknessMean(),4)));
		else if(this._type == ComponentEnum.HORIZONTAL_TAIL)
			// HTail form factor from Giovanni Nardone thesis pag.86
			_formFactor = (1.03 
					+ (1.85*getThicknessMean()) 
					+ (80*Math.pow(getThicknessMean(),4))
					);
		else if(this._type == ComponentEnum.VERTICAL_TAIL)
			// VTail form factor from Giovanni Nardone thesis pag.86
			_formFactor = (1.03 
					+ (2*getThicknessMean()) 
					+ (60*Math.pow(getThicknessMean(),4))
					);
		else if(this._type == ComponentEnum.CANARD)
			// Canard assumed as HTail
			_formFactor = (1.03 
					+ (1.85*getThicknessMean()) 
					+ (80*Math.pow(getThicknessMean(),4))
					);
	}
	
	@Override
	public void calculateMass(Aircraft aircraft, ComponentEnum liftingSurfaceType, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		calculateMass(aircraft, MethodEnum.KROO);
		calculateMass(aircraft, MethodEnum.JENKINSON);
		calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, MethodEnum.TORENBEEK_1982);
		calculateMass(aircraft, MethodEnum.RAYMER);
//		calculateMass(aircraft, MethodEnum.NICOLAI_2013);
//		calculateMass(aircraft, MethodEnum.HOWE);
//		calculateMass(aircraft, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, MethodEnum.SADRAY);
		calculateMass(aircraft, MethodEnum.ROSKAM);
		
		if(!methodsMapWeights.get(liftingSurfaceType).equals(MethodEnum.AVERAGE))
			_massEstimated = _massMap.get(methodsMapWeights.get(liftingSurfaceType));
		else
			_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					this.getMassReference(), 
					_massMap,
					_percentDifference,
					20.).getMean(), SI.KILOGRAM);
		
	}
	
	/** 
	 * Calculate mass of the generic lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 * @param method
	 */
	@SuppressWarnings("unused")
	private void calculateMass(
			Aircraft aircraft, 
			MethodEnum method) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		Double surface = this.getSurface().to(MyUnits.FOOT2).getEstimatedValue();
		Double surfaceExposed = aircraft.getExposedWing().getSurface().to(MyUnits.FOOT2).getEstimatedValue();

		Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(aircraft.getWing()));
		double thicknessMean = meanAirfoil.getAirfoilCreator().getThicknessToChordRatio();
		
		Amount<Angle> sweepStructuralAxis;
		if(this._type == ComponentEnum.WING)
			sweepStructuralAxis = Amount.valueOf(
					Math.atan(
							Math.tan(this.getSweepLEEquivalent().doubleValue(SI.RADIAN))
							- (4./this.getAspectRatio())*
							(getLiftingSurfaceCreator().getMainSparNonDimensionalPosition()
									*(1 - this.getTaperRatioEquivalent())
									/(1 + this.getTaperRatioEquivalent()))
							),
					1e-9, // precision
					SI.RADIAN);
		else
			sweepStructuralAxis = Amount.valueOf(
					Math.atan(
							Math.tan(this.getSweepLEEquivalent().doubleValue(SI.RADIAN))
							- (4./this.getAspectRatio())*
							(0.25
									*(1 - this.getTaperRatioEquivalent())
									/(1 + this.getTaperRatioEquivalent()))
							),
					1e-9, // precision
					SI.RADIAN);
		
		switch(_type) {
		case WING : {
			switch (method) {

			/* This method poor results
			 * */
			case ROSKAM : { // Roskam page 85 (pdf) part V
				methodsList.add(method);

				System.out.println("---" + this._liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN));
				_mass = Amount.valueOf(
						Amount.valueOf(2*(0.00428*
								Math.pow(surface, 0.48)*this.getAspectRatio()*
								Math.pow(aircraft.getTheAnalysisManager().getMachDive0(), 0.43)*
								Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().to(NonSI.POUND_FORCE).
										times(aircraft.getTheAnalysisManager().getNUltimate()).
										getEstimatedValue(),0.84)*
								Math.pow(this._liftingSurfaceCreator.getEquivalentWing().getTaperRatio(), 0.14))/
								(Math.pow(100*this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(),0.76)*
										Math.pow(Math.cos(this.getSweepHalfChordEquivalent().to(SI.RADIAN).getEstimatedValue()), 1.54)),
								NonSI.POUND_FORCE).to(NonSI.KILOGRAM_FORCE).getEstimatedValue(),
						SI.KILOGRAM);

				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			//			
			case KROO : { // page 430 Aircraft design synthesis
				methodsList.add(method);

				//				if (aircraft.get_powerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {
				_mass = Amount.valueOf((4.22*surface +
						1.642e-6*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*
								Math.sqrt(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()*
										aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().to(NonSI.POUND).getEstimatedValue())*
								(1 + 2*this._liftingSurfaceCreator.getEquivalentWing().getTaperRatio()))/
						(thicknessMean*Math.pow(Math.cos(this._liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN).getEstimatedValue()),2)*
								surface*(1 + this.getLiftingSurfaceCreator().getEquivalentWing().getTaperRatio()))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				//				} else {
				//					_mass = null;
				//					_massMap.put(method, null);
				//				}
			} break;

			case JENKINSON : { // page 134 Jenkinson - Civil Jet Aircraft Design

				methodsList.add(method);

				if (!aircraft.getPowerPlant().getEngineType().equals(EngineTypeEnum.TURBOPROP)) {

					double R, kComp;

					if (getLiftingSurfaceCreator().getCompositeCorrectioFactor() != null) {
						kComp = getLiftingSurfaceCreator().getCompositeCorrectioFactor();
					} else {
						kComp = 0.;					
					}

					for (int i = 0; i < 10; i++) {

						try {
							R = _mass.getEstimatedValue() + aircraft.getFuelTank().getFuelMass().getEstimatedValue() +
									((2*(aircraft.getNacelles().getTotalMass().getEstimatedValue() + 
											aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().getEstimatedValue())*
											aircraft.getNacelles().getDistanceBetweenInboardNacellesY().getEstimatedValue())/
											(0.4*this.getSpan().getEstimatedValue())) + 
									((2*(aircraft.getNacelles().getTotalMass().getEstimatedValue() + 
											aircraft.getPowerPlant().getEngineList().get(0).getDryMassPublicDomain().getEstimatedValue())*
											aircraft.getNacelles().getDistanceBetweenOutboardNacellesY().getEstimatedValue())/
											(0.4*this.getSpan().getEstimatedValue()));
						} catch(NullPointerException e) {R = 0.;}

						_mass = Amount.valueOf(
								(1 - kComp) * 0.021265*
								(pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue()*
										aircraft.getTheAnalysisManager().getNUltimate(),0.4843)*
										pow(this.getSurface().getEstimatedValue(),0.7819)*
										pow(this.getAspectRatio(),0.993)*
										pow(1 + this.getLiftingSurfaceCreator().getEquivalentWing().getTaperRatio(),0.4)*
										pow(1 - R/aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue(),0.4))/
								(cos(this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN).getEstimatedValue())*
										pow(thicknessMean,0.4)), 
								SI.KILOGRAM);
					}
					_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				} else {
					_mass = null;
					_massMap.put(method, null);
				}
			} break;

			/* This method gives poor results
			 * 
			 * */
			case RAYMER : { // page 403 (211 pdf) Raymer 
				methodsList.add(method);
				_mass = Amount.valueOf(0.0051 * pow(aircraft.getTheAnalysisManager().getTheWeights().
						getMaximumTakeOffWeight().to(NonSI.POUND_FORCE).times(aircraft.getTheAnalysisManager().
								getNUltimate()).getEstimatedValue(),
						0.557)*
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(),0.649)*
						pow(this.getAspectRatio(), 0.5)*
						pow(this.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(), -0.4)*
						pow(1+this.getLiftingSurfaceCreator().getEquivalentWing().getTaperRatio(), 0.1)*
						pow(cos(this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN).getEstimatedValue()), -1)*
						pow(this._liftingSurfaceCreator.getControlSurfaceArea().to(MyUnits.FOOT2).getEstimatedValue(), 0.1), NonSI.POUND).
						to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case SADRAY : { // page 583 pdf Sadray Aircraft Design System Engineering Approach
				// results very similar to Jenkinson
				methodsList.add(method);
				Double _kRho = 0.0035;
				_mass = Amount.valueOf(
						this.getSurface().getEstimatedValue()*
						//_meanAerodChordCk.getEstimatedValue()*
						aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()* //
						(this.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio())
						*aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().getEstimatedValue()*
						_kRho*
						pow((this.getAspectRatio()*aircraft.getTheAnalysisManager().getNUltimate())/
								cos(this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN).getEstimatedValue()),0.6)*
						pow(this.getLiftingSurfaceCreator().getEquivalentWing().getTaperRatio(), 0.04), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* The method gives an average 20 percent difference from real value 
			 */
			case TORENBEEK_1982 : {
				methodsList.add(method);
				_mass = Amount.valueOf(
						0.0017*
						aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight().to(NonSI.POUND_FORCE).getEstimatedValue()*
						Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue()/
								Math.cos(this.getSweepHalfChordEquivalent().to(SI.RADIAN).getEstimatedValue()),0.75)*
						(1 + Math.pow(6.3*Math.cos(this.getSweepHalfChordEquivalent().to(SI.RADIAN).getEstimatedValue())/
								this.getSpan().to(NonSI.FOOT).getEstimatedValue(), 0.5))*
						Math.pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.55)*
						Math.pow(
								this.getSpan().to(NonSI.FOOT).getEstimatedValue()*surface/
								(this.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
										*this.getChordRoot().to(NonSI.FOOT).getEstimatedValue()*
										aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight().to(NonSI.POUND_FORCE).getEstimatedValue()*
										Math.cos(this.getSweepHalfChordEquivalent().to(SI.RADIAN).getEstimatedValue())), 0.3)
						, NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case TORENBEEK_2013 : { // page 253 pdf
				methodsList.add(method);

				//				if (aircraft.get_powerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {
				_mass = Amount.valueOf(
						(0.0013*
								aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight()
										.times(aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelWeight()).getEstimatedValue(), 
										0.5)*
								0.36*Math.pow(1 + this.getLiftingSurfaceCreator().getEquivalentWing().getTaperRatio(), 0.5)*
								(this.getSpan().getEstimatedValue()/100)*
								(this.getAspectRatio()/
										(thicknessMean
												*Math.pow(
														Math.cos(this.getSweepHalfChordEquivalent().to(SI.RADIAN).getEstimatedValue())
														, 2))) +
								210*this.getSurface().getEstimatedValue())/
						AtmosphereCalc.g0.getEstimatedValue()
						, SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				//				} else {
				//					_mass = null;
				//					_massMap.put(method, null);
				//				}

			}

			default : { }
			}
		} break;
		////////////////////////////////////
		////////////////////////////////////
		case HORIZONTAL_TAIL : {
			switch (method) {

			/*
			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				_methodsList.add(method);
				_mass = Amount.valueOf(0.047*
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue()*
						pow(_surface.getEstimatedValue(), 1.24), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case JENKINSON : { // Jenkinson page 149 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(22*this.getSurface().getEstimatedValue(), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case NICOLAI_2013 : {
				methodsList.add(method);
				double gamma = pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()*
						aircraft.getTheAnalysisManager().getNUltimate(), 0.813)*
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 0.584)*
						pow(this.getSpan().getEstimatedValue()/
								(this.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
										*this.getChordRoot().getEstimatedValue()), 0.033) * 
						pow(aircraft.getWing().getLiftingSurfaceCreator().getMeanAerodynamicChord().getEstimatedValue()/
								this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().getEstimatedValue(), 0.28);

				_mass = Amount.valueOf(0.0034 * 
						pow(gamma, 0.915), NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case RAYMER : { // Raymer page 211 pdf
				methodsList.add(method);
				_mass = Amount.valueOf(0.0379 * 
						pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue(), 0.639)*
						pow(aircraft.getTheAnalysisManager().getNUltimate(), 0.1) * 
						pow(this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(NonSI.FOOT).getEstimatedValue(), -1.) *
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 0.75) * 
						pow(0.3*this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(NonSI.FOOT).getEstimatedValue(), 0.704) * 
						pow(cos(this.getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN).getEstimatedValue()), -1) *
						pow(this.getAspectRatio(), 0.166) * 
						pow(1 + aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().to(NonSI.FOOT).getEstimatedValue()/
								this.getSpan().to(NonSI.FOOT).getEstimatedValue(), -0.25) * 
						pow(1 + this._liftingSurfaceCreator.getControlSurfaceArea().to(MyUnits.FOOT2).getEstimatedValue()/
								this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 0.1),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((5.25*aircraft.getExposedWing().getSurface().getEstimatedValue() +
						0.8e-6*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*
								aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()*
								this.getLiftingSurfaceCreator().getMeanAerodynamicChord().to(NonSI.FOOT).getEstimatedValue()*
								Math.sqrt(aircraft.getExposedWing().getSurface().getEstimatedValue()))/
						(thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.to(SI.RADIAN).getEstimatedValue()),2)*
								this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(NonSI.FOOT).getEstimatedValue()*Math.pow(surface,1.5))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.0275;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.3)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			/*
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				_methodsList.add(method);
				double kh = 1.;
				if (_variableIncidence == true) { kh = 1.1;}

				_mass = Amount.valueOf(kh*3.81*
						aircraft.get_performances().get_vDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			default : { } break;
			}
		} break;
		////////////////////////////////////
		////////////////////////////////////
		case VERTICAL_TAIL : {
			switch (method) {

			case HOWE : { // page 381 Howe Aircraft Conceptual Design Synthesis
				methodsList.add(method);
				double k = 0.;
				if (_positionRelativeToAttachment == 1.0) {
					k = 1.5;
				} else {
					k = 1.;
				}
				_mass = Amount.valueOf(0.065*k*
						aircraft.getTheAnalysisManager().getVDiveEAS().getEstimatedValue()*
						pow(this.getSurface().getEstimatedValue(), 1.15), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;


			case JENKINSON : {
				methodsList.add(method);
				_mass = Amount.valueOf(22*this.getSurface().getEstimatedValue(), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case RAYMER : { // Raymer page 211 pdf
				_methodsList.add(method);
				_mass = Amount.valueOf(0.0026 * 
						pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.556)*
						pow(aircraft.get_performances().get_nUltimate(), 0.536) * 
						pow(_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), -0.5) *
						pow(_surface.to(MyUnits.FOOT2).getEstimatedValue(), 0.5) * 
						pow(0.3*_ACw_ACdistance.to(NonSI.FOOT).getEstimatedValue(), 0.875) * 
						pow(cos(_sweepQuarterChordEq.to(SI.RADIAN).getEstimatedValue()), -1.) *
						pow(_aspectRatio, 0.35) * 
						pow(_tc_root, -0.5) *
						pow(1 + _positionRelativeToAttachment, 0.225),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;
			 */
			case TORENBEEK_1976 : { // Roskam page 90 (pdf) part V
				methodsList.add(method);
				double kv = 1.;
				if (_positionRelativeToAttachment == 1.) { 
					kv = 1 + 0.15*
							(aircraft.getHTail().getSurface().getEstimatedValue()/
									this.getSurface().getEstimatedValue());}
				_mass = Amount.valueOf(kv*3.81*
						aircraft.getTheAnalysisManager().getVDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(this.getSweepHalfChordEquivalent().to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((2.62*surface +
						1.5e-5*
						(aircraft.getTheAnalysisManager().getNUltimate()*
								Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*(
										8.0 + 0.44*aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffWeight().to(NonSI.POUND_FORCE).getEstimatedValue()/
										aircraft.getWing().getSurface().to(MyUnits.FOOT2).getEstimatedValue())/
								(thicknessMean*Math.pow(Math.cos(sweepStructuralAxis.getEstimatedValue()),2)))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* Average error > 50 %
			case SADRAY : { // page 584 pdf Sadray Aircraft Design System Engineering Approach
				_methodsList.add(method);
				// TODO ADD kRho table
				Double _kRho = 0.05;
				_mass = Amount.valueOf(
						_surface.getEstimatedValue()*
						_meanAerodChordCk.getEstimatedValue()*
						(_tc_root)*aircraft.get_weights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow(_aspectRatio/
								cos(_sweepQuarterChordEq.getEstimatedValue()),0.6)*
								pow(_taperRatioEquivalent, 0.04)*
								pow(_volumetricRatio, 0.2)*
								pow(_CeCt, 0.4), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			}break;
			 */

			default : { } break;
			}
		} break;
		case CANARD : {

			// TODO
			
		} break;
		
		default:
			break;
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				20.).getFilteredMean(), SI.KILOGRAM);

		if ((_massCorrectionFactor != null) && (_massEstimated != null)) {
			_massEstimated = _massEstimated.times(_massCorrectionFactor);
		}

		_mass = Amount.valueOf(_massEstimated.getEstimatedValue(), SI.KILOGRAM);

	}

	public void calculateCG(ComponentEnum type, Map<ComponentEnum, MethodEnum> methodsMap) {
//		calculateCG(MethodEnum.SFORZA, type);
		calculateCG(MethodEnum.TORENBEEK_1982, type);
		
		if(!methodsMap.get(type).equals(MethodEnum.AVERAGE)) { 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(type)));
			_cg.setYLRF(_yCGMap.get(methodsMap.get(type)));
		}
		else {
			_percentDifferenceXCG = new Double[_xCGMap.size()];
			_percentDifferenceYCG = new Double[_yCGMap.size()];

			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					_percentDifferenceXCG,
					100.).getFilteredMean(), SI.METER));

			_cg.setYLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getYLRFref(), 
					_yCGMap,
					_percentDifferenceYCG,
					100.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(type);
	}
	
	public void calculateCG(MethodEnum method, ComponentEnum type) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(_xApexConstructionAxes,
						 _yApexConstructionAxes,
						 _zApexConstructionAxes
				);

		_cg.set_xLRFref(getChordRoot().times(0.4));
		_cg.set_yLRFref(getSpan().times(0.5*0.4));
		_cg.set_zLRFref(Amount.valueOf(0., SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		methodsList = new ArrayList<MethodEnum>();

		_xCG = Amount.valueOf(0., SI.METER);
		_yCG = Amount.valueOf(0., SI.METER);
		_zCG = Amount.valueOf(0., SI.METER);

		@SuppressWarnings("unused")
		Double lambda = _liftingSurfaceCreator.getEquivalentWing().getTaperRatio(),
				span = getSpan().getEstimatedValue(),
				xRearSpar = _liftingSurfaceCreator.getSecondarySparNonDimensionalPosition(),
				xFrontSpar = _liftingSurfaceCreator.getMainSparNonDimensionalPosition();

		switch (type) {
		case WING : {
			switch(method) {

//			//		 Bad results ...
//			case SFORZA : { // page 359 Sforza (2014) - Aircraft Design
//				methodsList.add(method);
//				_yCG = Amount.valueOf(
//						(span/6) * 
//						((1+2*lambda)/(1-lambda)),
//						SI.METER);
//
//				_xCG = Amount.valueOf(
//						(_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())/2)
//						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
//						, SI.METER);
//				_xCGMap.put(method, _xCG);
//				_yCGMap.put(method, _yCG);
//			} break;

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.35*(span/2) 
						, SI.METER);

				xRearSpar = 0.6*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue());
				xFrontSpar = 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue());

				_xCG = Amount.valueOf(
						0.7*(xRearSpar - xFrontSpar)
						+ 0.25*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);

				//				System.out.println("x: " + _xCG 
				//				+ ", y: " + _yCG 
				//				+ ", xLE: " + getXLEAtYEquivalent(_yCG.getEstimatedValue()));
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;

			}

		} break;

		case HORIZONTAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);
				_yCG = Amount.valueOf(
						0.38*(span/2) 
						, SI.METER);

				_xCG = Amount.valueOf(
						(0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue()))
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case VERTICAL_TAIL : {

			switch(method) {

			// page 359 Sforza (2014) - Aircraft Design
			// page 313 Torenbeek (1982)
			case TORENBEEK_1982 : { 
				methodsList.add(method);

				if (_positionRelativeToAttachment > 0.8) {
					_yCG = Amount.valueOf(
							0.55*(span) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				} else {
					_yCG = Amount.valueOf(
							0.38*(span) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				}

				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

			default : break;
			}
		} break;

		case CANARD : {

		} break;

		default : {} break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, methodsList);

	}
	
	@Override
	public List<Airfoil> populateAirfoilList(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Boolean equivalentWingFlag
			) {	
		
		int nPanels = this._liftingSurfaceCreator.getPanels().size();

		if(!equivalentWingFlag) {
			Airfoil airfoilRoot = new Airfoil(this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot());
			this._airfoilList.add(airfoilRoot);

			for(int i=0; i<nPanels - 1; i++) {

				Airfoil innerAirfoil = new Airfoil(this._liftingSurfaceCreator.getPanels().get(i).getAirfoilTip()); 
				this._airfoilList.add(innerAirfoil);
			}

			Airfoil airfoilTip = new Airfoil(this._liftingSurfaceCreator.getPanels().get(nPanels - 1).getAirfoilTip());
			this._airfoilList.add(airfoilTip);
		}

		else{
			Airfoil airfoilRoot = new Airfoil(this._liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getAirfoilRoot());
			this._airfoilList.add(airfoilRoot);

			Airfoil airfoilTip = new Airfoil(this._liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getAirfoilTip());
			this._airfoilList.add(airfoilTip);
		}

		discretizeAirfoilCharacteristics();
		calculateTransitionPoints();
		
		return this._airfoilList;
	}
	
	private void discretizeAirfoilCharacteristics () {
		
		for(int i=0; i<_airfoilList.size(); i++) {
		
			this._maxThicknessVsY.add(_airfoilList.get(i).getAirfoilCreator().getThicknessToChordRatio());
			this._radiusLEVsY.add(_airfoilList.get(i).getAirfoilCreator().getRadiusLeadingEdge());
			this._alpha0VsY.add(_airfoilList.get(i).getAirfoilCreator().getAlphaZeroLift());
			this._alphaStarVsY.add(_airfoilList.get(i).getAirfoilCreator().getAlphaEndLinearTrait());
			this._alphaStallVsY.add(_airfoilList.get(i).getAirfoilCreator().getAlphaStall());
			this._clAlphaVsY.add(_airfoilList.get(i).getAirfoilCreator().getClAlphaLinearTrait());
			this._cdMinVsY.add(_airfoilList.get(i).getAirfoilCreator().getCdMin());
			this._clAtCdMinVsY.add(_airfoilList.get(i).getAirfoilCreator().getClAtCdMin());
			this._cl0VsY.add(_airfoilList.get(i).getAirfoilCreator().getClAtAlphaZero());
			this._clStarVsY.add(_airfoilList.get(i).getAirfoilCreator().getClEndLinearTrait());
			this._clMaxVsY.add(_airfoilList.get(i).getAirfoilCreator().getClMax());
			this._clMaxSweepVsY.add(this._clMaxVsY.get(i)*Math.pow(Math.cos(this.getSweepLEEquivalent().doubleValue(SI.RADIAN)),2));
			this._kFactorDragPolarVsY.add(_airfoilList.get(i).getAirfoilCreator().getKFactorDragPolar());
			this._cmAlphaQuarteChordVsY.add(_airfoilList.get(i).getAirfoilCreator().getCmAlphaQuarterChord().getEstimatedValue());
			this._xAcAirfoilVsY.add(_airfoilList.get(i).getAirfoilCreator().getXACNormalized());
			this._cmACVsY.add(_airfoilList.get(i).getAirfoilCreator().getCmAC());
			this._cmACStallVsY.add(_airfoilList.get(i).getAirfoilCreator().getCmACAtStall());
			this._criticalMachVsY.add(_airfoilList.get(i).getAirfoilCreator().getMachCritical());
			
		}
	}
	
	private void calculateTransitionPoints() {
				
		Double xTransitionUpper = 0.0;
		Double xTransitionLower = 0.0;
		
		for(int i=0; i<_airfoilList.size(); i++) {
			xTransitionUpper += _airfoilList.get(i).getAirfoilCreator().getXTransitionUpper();
			xTransitionLower += _airfoilList.get(i).getAirfoilCreator().getXTransitionLower();
		}
		
		xTransitionUpper = xTransitionUpper/_airfoilList.size();
		xTransitionLower = xTransitionLower/_airfoilList.size();
		
		this._liftingSurfaceCreator.setXTransitionUpper(xTransitionUpper);
		this._liftingSurfaceCreator.setXTransitionLower(xTransitionLower);
		
	}
	
	public static AirfoilCreator calculateAirfoilAtY (LiftingSurface theWing, double yLoc) {

		// initializing variables ... 
		AirfoilTypeEnum type = null;
		AirfoilFamilyEnum family = null;
		Double yInner = 0.0;
		Double yOuter = 0.0;
		Double thicknessRatioInner = 0.0;
		Double thicknessRatioOuter = 0.0;
		Double leadingEdgeRadiusInner = 0.0;
		Double leadingEdgeRadiusOuter = 0.0;
		Amount<Angle> alphaZeroLiftInner = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaZeroLiftOuter = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaEndLinearityInner = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaEndLinearityOuter = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaStallInner = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaStallOuter = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Double clAlphaInner = 0.0;
		Double clAlphaOuter = 0.0;
		Double cdMinInner = 0.0;
		Double cdMinOuter = 0.0;
		Double clAtCdMinInner = 0.0;
		Double clAtCdMinOuter = 0.0;
		Double cl0Inner = 0.0;
		Double cl0Outer = 0.0;
		Double clEndLinearityInner = 0.0;
		Double clEndLinearityOuter = 0.0;
		Double clMaxInner = 0.0;
		Double clMaxOuter = 0.0;
		Double kFactorDragPolarInner = 0.0;
		Double kFactorDragPolarOuter = 0.0;
		Double laminarBucketSemiExtensionInner = 0.0;
		Double laminarBucketSemiExtensionOuter = 0.0;
		Double laminarBucketDepthInner = 0.0;
		Double laminarBucketDepthOuter = 0.0;
		Double cmAlphaQuarterChordInner = 0.0;
		Double cmAlphaQuarterChordOuter = 0.0;
		Double normalizedXacInner = 0.0;
		Double normalizedXacOuter = 0.0;
		Double cmACInner = 0.0;
		Double cmACOuter = 0.0;
		Double cmACStallInner = 0.0;
		Double cmACStallOuter = 0.0;
		Double criticalMachInner = 0.0;
		Double criticalMachOuter = 0.0;
		Double xTransitionUpperInner = 0.0;
		Double xTransitionUpperOuter = 0.0;
		Double xTransitionLowerInner = 0.0;
		Double xTransitionLowerOuter = 0.0;
		
		if(yLoc < 0.0) {
			System.err.println("\n\tINVALID Y STATION FOR THE INTERMEDIATE AIRFOIL!!");
			return null;
		}
		
		for(int i=1; i<theWing.getLiftingSurfaceCreator().getYBreakPoints().size(); i++) {
			
			if((yLoc > theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).doubleValue(SI.METER))
					&& (yLoc < theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER))) {
				
				type = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getType();
				family = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getFamily();
				yInner = theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).doubleValue(SI.METER);
				yOuter = theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER);
				thicknessRatioInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getThicknessToChordRatio();
				thicknessRatioOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getThicknessToChordRatio();
				leadingEdgeRadiusInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getRadiusLeadingEdge().doubleValue(SI.METER);
				leadingEdgeRadiusOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getRadiusLeadingEdge().doubleValue(SI.METER);
				alphaZeroLiftInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaZeroLift();
				alphaZeroLiftOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaZeroLift();
				alphaEndLinearityInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaEndLinearTrait();
				alphaEndLinearityOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaEndLinearTrait();
				alphaStallInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaStall();
				alphaStallOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaStall();
				clAlphaInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue(); 
				clAlphaOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAlphaLinearTrait().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				cdMinInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCdMin();
				cdMinOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCdMin();
				clAtCdMinInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAtCdMin();
				clAtCdMinOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAtCdMin();
				cl0Inner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAtAlphaZero();
				cl0Outer = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAtAlphaZero();
				clEndLinearityInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClEndLinearTrait(); 
				clEndLinearityOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClEndLinearTrait();
				clMaxInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClMax();
				clMaxOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClMax();
				kFactorDragPolarInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getKFactorDragPolar();
				kFactorDragPolarOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getKFactorDragPolar();
				laminarBucketSemiExtensionInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getLaminarBucketSemiExtension();
				laminarBucketSemiExtensionOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getLaminarBucketSemiExtension();
				laminarBucketDepthInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getLaminarBucketDepth();
				laminarBucketDepthOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getLaminarBucketDepth();
				cmAlphaQuarterChordInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				cmAlphaQuarterChordOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmAlphaQuarterChord().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue();
				normalizedXacInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXACNormalized();
				normalizedXacOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXACNormalized();
				cmACInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmAC();
				cmACOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmAC();
				cmACStallInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmACAtStall();
				cmACStallOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmACAtStall();
				criticalMachInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getMachCritical();
				criticalMachOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getMachCritical();
				xTransitionUpperInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXTransitionUpper();
				xTransitionUpperOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXTransitionUpper();
				xTransitionLowerInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXTransitionLower();
				xTransitionLowerOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXTransitionLower();
				
			}	
		}

		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE THICKNESS RATIO
		Double intermediateAirfoilThicknessRatio = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {thicknessRatioInner, thicknessRatioOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE LEADING EDGE RADIUS
		Amount<Length> intermediateAirfoilLeadingEdgeRadius = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {leadingEdgeRadiusInner, leadingEdgeRadiusOuter},
						yLoc
						),
				SI.METER
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE ALPHA ZERO LIFT
		Amount<Angle> intermediateAirfoilAlphaZeroLift = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {alphaZeroLiftInner.doubleValue(NonSI.DEGREE_ANGLE), alphaZeroLiftOuter.doubleValue(NonSI.DEGREE_ANGLE)},
						yLoc
						),
				NonSI.DEGREE_ANGLE
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE ALPHA STAR
		Amount<Angle> intermediateAirfoilAlphaEndLinearity = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {alphaEndLinearityInner.doubleValue(NonSI.DEGREE_ANGLE), alphaEndLinearityOuter.doubleValue(NonSI.DEGREE_ANGLE)},
						yLoc
						),
				NonSI.DEGREE_ANGLE
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE ALPHA STALL
		Amount<Angle> intermediateAirfoilAlphaStall = Amount.valueOf(
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {alphaStallInner.doubleValue(NonSI.DEGREE_ANGLE), alphaStallOuter.doubleValue(NonSI.DEGREE_ANGLE)},
						yLoc
						),
				NonSI.DEGREE_ANGLE
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl ALPHA
		Amount<?> intermediateAirfoilClAlpha = Amount.valueOf( 
				MyMathUtils.getInterpolatedValue1DLinear(
						new double[] {yInner, yOuter},
						new double[] {clAlphaInner, clAlphaOuter},
						yLoc
						),
				NonSI.DEGREE_ANGLE.inverse()
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cd MIN
		Double intermediateAirfoilCdMin = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cdMinInner, cdMinOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl AT Cd MIN
		Double intermediateAirfoilClAtCdMin = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {clAtCdMinInner, clAtCdMinOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl0
		Double intermediateAirfoilCl0 = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cl0Inner, cl0Outer},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl END LINEARITY
		Double intermediateAirfoilClEndLinearity = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {clEndLinearityInner, clEndLinearityOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cl MAX
		Double intermediateAirfoilClMax = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {clMaxInner, clMaxOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE K FACTOR DRAG POLAR
		Double intermediateAirfoilKFactorDragPolar = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {kFactorDragPolarInner, kFactorDragPolarOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE LAMINAR BUCKET SEMI-EXTENSION
		Double intermediateLaminarBucketSemiExtension = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {laminarBucketSemiExtensionInner, laminarBucketSemiExtensionOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE LAMINAR BUCKET DEPTH
		Double intermediateLaminarBucketDepth = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {laminarBucketDepthInner, laminarBucketDepthOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm ALPHA c/4
		Amount<?> intermediateAirfoilCmAlphaQuaterChord = 
				Amount.valueOf(
						MyMathUtils.getInterpolatedValue1DLinear(
								new double[] {yInner, yOuter},
								new double[] {cmAlphaQuarterChordInner, cmAlphaQuarterChordOuter},
								yLoc
								),
						NonSI.DEGREE_ANGLE.inverse()
						);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Xac
		Double intermediateAirfoilXac = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {normalizedXacInner, normalizedXacOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm_ac
		Double intermediateAirfoilCmAC = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cmACInner, cmACOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm_ac STALL
		Double intermediateAirfoilCmACStall = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cmACStallInner, cmACStallOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm_ac STALL
		Double intermediateAirfoilCriticalMach = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {criticalMachInner, criticalMachOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE xTransition UPPER
		Double intermediateAirfoilTransitionXUpper = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {xTransitionUpperInner, xTransitionUpperOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE xTransition LOWER
		Double intermediateAirfoilTransitionXLower = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {xTransitionLowerInner, xTransitionLowerOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// AIRFOIL CREATION
		AirfoilCreator intermediateAirfoilCreator = new AirfoilCreator.AirfoilBuilder()
				.name("Intermediate Airfoil")
				.type(type)
				.family(family)
				.thicknessToChordRatio(intermediateAirfoilThicknessRatio)
				.radiusLeadingEdge(intermediateAirfoilLeadingEdgeRadius)
				.alphaZeroLift(intermediateAirfoilAlphaZeroLift)
				.alphaEndLinearTrait(intermediateAirfoilAlphaEndLinearity)
				.alphaStall(intermediateAirfoilAlphaStall)
				.clAlphaLinearTrait(intermediateAirfoilClAlpha)
				.cdMin(intermediateAirfoilCdMin)
				.clAtCdMin(intermediateAirfoilClAtCdMin)
				.clAtAlphaZero(intermediateAirfoilCl0)
				.clEndLinearTrait(intermediateAirfoilClEndLinearity)
				.clMax(intermediateAirfoilClMax)
				.kFactorDragPolar(intermediateAirfoilKFactorDragPolar)
				.laminarBucketSemiExtension(intermediateLaminarBucketSemiExtension)
				.laminarBucketDepth(intermediateLaminarBucketDepth)
				.cmAlphaQuarterChord(intermediateAirfoilCmAlphaQuaterChord)
				.xACNormalized(intermediateAirfoilXac)
				.cmAC(intermediateAirfoilCmAC)
				.cmACAtStall(intermediateAirfoilCmACStall)
				.machCritical(intermediateAirfoilCriticalMach)
				.xTransitionUpper(intermediateAirfoilTransitionXUpper)
				.xTransitionLower(intermediateAirfoilTransitionXLower)
				.build();
		
		return intermediateAirfoilCreator;

	}
	
	public static AirfoilCreator calculateMeanAirfoil (
			LiftingSurface theWing
			) {
		
		List<Double> influenceCoefficients = LSGeometryCalc.calculateInfluenceCoefficients(
				theWing.getLiftingSurfaceCreator().getChordsBreakPoints(),
				theWing.getLiftingSurfaceCreator().getYBreakPoints(), 
				theWing.getSurface()
				);
	
		//----------------------------------------------------------------------------------------------
		// MEAN AIRFOIL DATA CALCULATION:

		//----------------------------------------------------------------------------------------------
		// Maximum thickness:
		double maximumThicknessMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			maximumThicknessMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getThicknessToChordRatio();

		//----------------------------------------------------------------------------------------------
		// Leading edge radius:
		Amount<Length> leadingEdgeRadiusMeanAirfoil = Amount.valueOf(0.0, SI.METER);

		for(int i=0; i<influenceCoefficients.size(); i++)
			leadingEdgeRadiusMeanAirfoil = leadingEdgeRadiusMeanAirfoil
			.plus(theWing.getAirfoilList().get(i).getAirfoilCreator().getRadiusLeadingEdge()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha zero lift:
		Amount<Angle> alphaZeroLiftMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaZeroLiftMeanAirfoil = alphaZeroLiftMeanAirfoil
			.plus(theWing.getAirfoilList().get(i).getAirfoilCreator().getAlphaZeroLift()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha star lift:
		Amount<Angle> alphaStarMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaStarMeanAirfoil = alphaStarMeanAirfoil
			.plus(theWing.getAirfoilList().get(i).getAirfoilCreator().getAlphaEndLinearTrait()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Alpha stall lift:
		Amount<Angle> alphaStallMeanAirfoil = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);

		for(int i=0; i<influenceCoefficients.size(); i++)
			alphaStallMeanAirfoil = alphaStallMeanAirfoil
			.plus(theWing.getAirfoilList().get(i).getAirfoilCreator().getAlphaStall()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Cl alpha:
		Amount<?> clAlphaMeanAirfoil = Amount.valueOf(0.0, SI.RADIAN.inverse());

		for(int i=0; i<influenceCoefficients.size(); i++)
			clAlphaMeanAirfoil = clAlphaMeanAirfoil
			.plus(theWing.getAirfoilList().get(i).getAirfoilCreator().getClAlphaLinearTrait()
					.times(influenceCoefficients.get(i)
							)
					);

		//----------------------------------------------------------------------------------------------
		// Cd min:
		double cdMinMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cdMinMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getCdMin();

		//----------------------------------------------------------------------------------------------
		// Cl at Cd min:
		double clAtCdMinMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clAtCdMinMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getClAtCdMin();

		//----------------------------------------------------------------------------------------------
		// Cl0:
		double cl0MeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cl0MeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getClAtAlphaZero();	

		//----------------------------------------------------------------------------------------------
		// Cl star:
		double clStarMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clStarMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getClEndLinearTrait();	

		//----------------------------------------------------------------------------------------------
		// Cl max:
		double clMaxMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			clMaxMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getClMax();	

		//----------------------------------------------------------------------------------------------
		// K factor drag polar:
		double kFactorDragPolarMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			kFactorDragPolarMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getKFactorDragPolar();	

		//----------------------------------------------------------------------------------------------
		// Cm quarter chord:
		double cmAlphaQuarteChordMeanAirfoil = 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmAlphaQuarteChordMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getCmAlphaQuarterChord().getEstimatedValue();	

		//----------------------------------------------------------------------------------------------
		// x ac:
		double xACMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			xACMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getXACNormalized();	

		//----------------------------------------------------------------------------------------------
		// cm ac:
		double cmACMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmACMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getCmAC();	

		//----------------------------------------------------------------------------------------------
		// cm ac stall:
		double cmACStallMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			cmACStallMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getCmACAtStall();	

		//----------------------------------------------------------------------------------------------
		// critical Mach number:
		double criticalMachMeanAirfoil= 0;

		for(int i=0; i<influenceCoefficients.size(); i++)
			criticalMachMeanAirfoil += influenceCoefficients.get(i)
			*theWing.getAirfoilList().get(i).getAirfoilCreator().getMachCritical();	

		//----------------------------------------------------------------------------------------------
		// MEAN AIRFOIL CREATION:

		AirfoilCreator meanAirfoilCreator = new AirfoilCreator.AirfoilBuilder()
				.name("Mean Airfoil")
				.type(theWing.getAirfoilList().get(0).getAirfoilCreator().getType())
				.family(theWing.getAirfoilList().get(0).getAirfoilCreator().getFamily())
				.thicknessToChordRatio(maximumThicknessMeanAirfoil)
				.radiusLeadingEdge(leadingEdgeRadiusMeanAirfoil)
				.alphaZeroLift(alphaZeroLiftMeanAirfoil)
				.alphaEndLinearTrait(alphaStarMeanAirfoil)
				.alphaStall(alphaStallMeanAirfoil)
				.clAlphaLinearTrait(clAlphaMeanAirfoil)
				.cdMin(cdMinMeanAirfoil)
				.clAtCdMin(clAtCdMinMeanAirfoil)
				.clAtAlphaZero(cl0MeanAirfoil)
				.clEndLinearTrait(clStarMeanAirfoil)
				.clMax(clMaxMeanAirfoil)
				.kFactorDragPolar(kFactorDragPolarMeanAirfoil)
				.cmAlphaQuarterChord(Amount.valueOf(cmAlphaQuarteChordMeanAirfoil, NonSI.DEGREE_ANGLE.inverse()))
				.xACNormalized(xACMeanAirfoil)
				.cmAC(cmACMeanAirfoil)
				.cmACAtStall(cmACStallMeanAirfoil)
				.machCritical(criticalMachMeanAirfoil)
				.build();

		return meanAirfoilCreator;

	}
	
	public static double[] calculateInfluenceFactorsMeanAirfoilFlap(
			double etaIn,
			double etaOut,
			List<Double> etaBreakPoints,
			List<Amount<Length>> chordBreakPoints,
			Amount<Length> semiSpan
			) throws InstantiationException, IllegalAccessException{

		double [] influenceAreas = new double [2];
		double [] influenceFactors = new double [2];
		
		double chordIn = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						etaBreakPoints
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						chordBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
						),
				etaIn
				);

		double chordOut = MyMathUtils.getInterpolatedValue1DLinear(
				MyArrayUtils.convertToDoublePrimitive(
						etaBreakPoints
						),
				MyArrayUtils.convertListOfAmountTodoubleArray(
						chordBreakPoints.stream().map(x -> x.to(SI.METER)).collect(Collectors.toList())
						),
				etaOut
				);
		
		influenceAreas[0] = (chordIn * ((etaOut - etaIn)*semiSpan.doubleValue(SI.METER)))/2;
		influenceAreas[1] = (chordOut * ((etaOut - etaIn)*semiSpan.doubleValue(SI.METER)))/2;
		
		// it returns the influence coefficient
		
		influenceFactors[0] = influenceAreas[0]/(influenceAreas[0] + influenceAreas[1]);
		influenceFactors[1] = influenceAreas[1]/(influenceAreas[0] + influenceAreas[1]);
		
		return influenceFactors;
}
	
	@Override
	public List<Airfoil> getAirfoilList() {	
		return this._airfoilList;
	}
	
	@Override
	public void setAirfoilList(List<Airfoil> airfoilList) {	
		this._airfoilList = airfoilList;
	}
	
	@Override
	public double getChordAtYActual(Double y) {
		return GeometryCalc.getChordAtYActual(
				MyArrayUtils.convertListOfAmountTodoubleArray(_liftingSurfaceCreator.getDiscretizedYs()), 
				MyArrayUtils.convertListOfAmountTodoubleArray(_liftingSurfaceCreator.getDiscretizedChords()),
				y
				);
	}

	@Override
	public Amount<Area> getSurface() {
		return _liftingSurfaceCreator.getSurfacePlanform();
	}

	@Override
	public double getAspectRatio() {
		return _liftingSurfaceCreator.getAspectRatio();
	}

	@Override
	public Amount<Length> getSpan() {
		return _liftingSurfaceCreator.getSpan();
	}

	@Override
	public Amount<Length> getSemiSpan() {
		return _liftingSurfaceCreator.getSemiSpan();
	}

	@Override
	public double getTaperRatio() {
		return _liftingSurfaceCreator.getTaperRatio();
	}

	@Override
	public double getTaperRatioEquivalent() {
		return _liftingSurfaceCreator.getEquivalentWing().getTaperRatio();
	}
	
//	public double getTaperRatioEquivalent(Boolean recalculate) {
//		if(recalculate)
//			_liftingSurfaceCreator.getEquivalentWing(recalculate);
//		return _liftingSurfaceCreator.getEquivalentWing().getTaperRatio();
//	}

	@Override
	public LiftingSurfaceCreator getEquivalentWing() {
		return _liftingSurfaceCreator.getEquivalentWing();
	}
	
//	public LiftingSurfaceCreator getEquivalentWing(Boolean recalculate) {
//		return _liftingSurfaceCreator.getEquivalentWing(recalculate);
//	}

	@Override
	public Amount<Length> getChordRootEquivalent() {
		return _liftingSurfaceCreator
					.getEquivalentWing()
						.getPanels()
							.get(0)
								.getChordRoot();	
	}
	
//	public Amount<Length> getChordRootEquivalent(Boolean recalculate) {
//		if(recalculate) 
//			_liftingSurfaceCreator.getEquivalentWing(recalculate);
//		return _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getChordRoot();
//	}

	@Override
	public Amount<Length> getChordRoot() {
		return _liftingSurfaceCreator.getPanels().get(0).getChordRoot();
	}

	@Override
	public Amount<Length> getChordTip() {
		return _liftingSurfaceCreator.getPanels().get(
				_liftingSurfaceCreator.getPanels().size()-1
				)
				.getChordTip();
	}

	@Override
	public Amount<Angle> getSweepLEEquivalent() {
		return _liftingSurfaceCreator
					.getEquivalentWing()
						.getPanels()
							.get(0)
								.getSweepLeadingEdge()
									.to(NonSI.DEGREE_ANGLE);
	}
	
//	public Amount<Angle> getSweepLEEquivalent(Boolean recalculate) {
//		if(recalculate)
//			_liftingSurfaceCreator.getEquivalentWing(recalculate);
//		return LSGeometryCalc.calculateSweep(
//				_liftingSurfaceCreator.getEquivalentWing().getAspectRatio(),
//				_liftingSurfaceCreator.getEquivalentWing().getTaperRatio(),
//				_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN),
//				0.0,
//				0.25
//				).to(NonSI.DEGREE_ANGLE);
//				
//	}

	@Override
	public Amount<Angle> getSweepHalfChordEquivalent() {
		return _liftingSurfaceCreator
				.getEquivalentWing()
					.getPanels()
						.get(0)
							.getSweepHalfChord()
								.to(NonSI.DEGREE_ANGLE);
	}
//	public Amount<Angle> getSweepHalfChordEquivalent(Boolean recalculate) {
//		if(recalculate) 
//			_liftingSurfaceCreator.getEquivalentWing(recalculate);
//		return LSGeometryCalc.calculateSweep(
//				_liftingSurfaceCreator.getEquivalentWing().getAspectRatio(),
//				_liftingSurfaceCreator.getEquivalentWing().getTaperRatio(),
//				_liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().doubleValue(SI.RADIAN),
//				0.0,
//				0.5
//				).to(NonSI.DEGREE_ANGLE);
//	}

	@Override
	public Amount<Angle> getSweepQuarterChordEquivalent() {
		return _liftingSurfaceCreator
				.getEquivalentWing()
					.getPanels()
						.get(0)
							.getSweepQuarterChord()
								.to(NonSI.DEGREE_ANGLE);
	}
	
//	public Amount<Angle> getSweepQuarterChordEquivalent(Boolean recalculate) {
//		if(recalculate)
//			_liftingSurfaceCreator.getEquivalentWing(recalculate);
//		return _liftingSurfaceCreator.getEquivalentWing().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN);
//	}

	@Override
	public LiftingSurfaceCreator getLiftingSurfaceCreator() {
		return _liftingSurfaceCreator;
	}

	@Override
	public void calculateGeometry(ComponentEnum type, Boolean mirrored) {
		_liftingSurfaceCreator.calculateGeometry(type, mirrored);
	}

	@Override
	public void calculateGeometry(int nSections, ComponentEnum type, Boolean mirrored) {
		_liftingSurfaceCreator.calculateGeometry(nSections, type, mirrored);
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public ComponentEnum getType() {
		return _type;
	}

	@Override
	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}
	
	@Override
	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	@Override
	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setId(String _id) {
		this._id = _id;
	}

	public void setType(ComponentEnum _type) {
		this._type = _type;
	}

	@Override
	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	@Override
	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	@Override
	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	@Override
	public void setLiftingSurfaceCreator(LiftingSurfaceCreator _liftingSurfaceCreator) {
		this._liftingSurfaceCreator = _liftingSurfaceCreator;
	}

	@Override
	public AerodynamicDatabaseReader getAerodynamicDatabaseReader() {
		return this._aeroDatabaseReader;
	}
	
	@Override
	public void setAerodynamicDatabaseReader(AerodynamicDatabaseReader aeroDatabaseReader) {
		this._aeroDatabaseReader = aeroDatabaseReader;
	}

	public HighLiftDatabaseReader getHighLiftDatabaseReader() {
		return _highLiftDatabaseReader;
	}

	public void setHighLiftDatabaseReader(HighLiftDatabaseReader _highLiftDatabaseReader) {
		this._highLiftDatabaseReader = _highLiftDatabaseReader;
	}

	@Override
	public Amount<Angle> getRiggingAngle() {
		return this._riggingAngle;
	}
	
	@Override
	public void setRiggingAngle (Amount<Angle> iW) {
		this._riggingAngle = iW;
	}

	@Override
	public CenterOfGravity getCG() {
		return _cg;
	}

	@Override
	public Amount<Length> getXCG() {
		return _xCG;
	}

	@Override
	public Amount<Length> getYCG() {
		return _yCG;
	}

	@Override
	public Amount<Length> getZCG() {
		return _zCG;
	}

	@Override
	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	@Override
	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	@Override
	public void setYCG(Amount<Length> _yCG) {
		this._yCG = _yCG;
	}

	@Override
	public void setZCG(Amount<Length> _zCG) {
		this._zCG = _zCG;
	}

	@Override
	public Double getPositionRelativeToAttachment() {
		return _positionRelativeToAttachment;
	}

	@Override
	public void setPositionRelativeToAttachment(Double _positionRelativeToAttachment) {
		this._positionRelativeToAttachment = _positionRelativeToAttachment;
	}

	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Amount<Mass> getMass() {
		return _mass;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public void setMassEstimated(Amount<Mass> massEstimated) {
		this._massEstimated = massEstimated;
	}
	
	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public LSAerodynamicsManager getAerodynamics() {
		return _theAerodynamics;
	}

	public void setAerodynamics(LSAerodynamicsManager theAerodynamics) {
		this._theAerodynamics = theAerodynamics;
	}
	
	public Map<ConditionEnum, LSAerodynamicsManager> getTheAerodynamicsCalculatorMap() {
		return _theAerodynamicsCalculatorMap;
	}

	public void setTheAerodynamicsCalculatorMap(Map<ConditionEnum, LSAerodynamicsManager> _theAerodynamicsCalculatorMap) {
		this._theAerodynamicsCalculatorMap = _theAerodynamicsCalculatorMap;
	}

	public List<Double> getMaxThicknessVsY() {
		return _maxThicknessVsY;
	}

	public List<Amount<Length>> getRadiusLEVsY() {
		return _radiusLEVsY;
	}

	public List<Double> getCamberRatioVsY() {
		return _camberRatioVsY;
	}

	public List<Amount<Angle>> getAlpha0VsY() {
		return _alpha0VsY;
	}

	public List<Amount<Angle>> getAlphaStarVsY() {
		return _alphaStarVsY;
	}

	public List<Amount<Angle>> getAlphaStallVsY() {
		return _alphaStallVsY;
	}

	public List<Amount<?>> getClAlphaVsY() {
		return _clAlphaVsY;
	}

	public List<Double> getCdMinVsY() {
		return _cdMinVsY;
	}

	public List<Double> getClAtCdMinVsY() {
		return _clAtCdMinVsY;
	}

	public List<Double> getCl0VsY() {
		return _cl0VsY;
	}

	public List<Double> getClStarVsY() {
		return _clStarVsY;
	}

	public List<Double> getClMaxVsY() {
		return _clMaxVsY;
	}

	public List<Double> getClMaxSweepVsY() {
		return _clMaxSweepVsY;
	}

	public List<Double> getKFactorDragPolarVsY() {
		return _kFactorDragPolarVsY;
	}

	public List<Double> getMExponentDragPolarVsY() {
		return _mExponentDragPolarVsY;
	}

	public List<Double> getCmAlphaQuarteChordVsY() {
		return _cmAlphaQuarteChordVsY;
	}

	public List<Double> getXAcAirfoilVsY() {
		return _xAcAirfoilVsY;
	}

	public List<Double> getCmACVsY() {
		return _cmACVsY;
	}

	public List<Double> getCmACStallVsY() {
		return _cmACStallVsY;
	}

	public List<Double> getCriticalMachVsY() {
		return _criticalMachVsY;
	}

	public Double getMassCorrectionFactor() {
		return _massCorrectionFactor;
	}

	public void setMassCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}

	public Double getFormFactor() {
		return _formFactor;
	}

	public void setFormFactor(Double _formFactor) {
		this._formFactor = _formFactor;
	}

	public Double getThicknessMean() {
		return _thicknessMean;
	}

	public void setThicknessMean(Double _thicknessMean) {
		this._thicknessMean = _thicknessMean;
	}

	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	public void set_percentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<MethodEnum, Amount<Length>> getYCGMap() {
		return _yCGMap;
	}

	public void setYCGMap(Map<MethodEnum, Amount<Length>> _yCGMap) {
		this._yCGMap = _yCGMap;
	}

	public Double[] getPercentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public void setPercentDifferenceXCG(Double[] _percentDifferenceXCG) {
		this._percentDifferenceXCG = _percentDifferenceXCG;
	}

	public Double[] getPercentDifferenceYCG() {
		return _percentDifferenceYCG;
	}

	public void setPercentDifferenceYCG(Double[] _percentDifferenceYCG) {
		this._percentDifferenceYCG = _percentDifferenceYCG;
	}

	public LSAerodynamicsManager getTheAerodynamics() {
		return _theAerodynamics;
	}

	public void setTheAerodynamics(LSAerodynamicsManager _theAerodynamics) {
		this._theAerodynamics = _theAerodynamics;
	}

	/**
	 * @return the _exposedWing
	 */
	public LiftingSurface getExposedWing() {
		return _exposedWing;
	}

	/**
	 * @param _exposedWing the _exposedWing to set
	 */
	public void setExposedWing(LiftingSurface _exposedWing) {
		this._exposedWing = _exposedWing;
	}

	/**
	 * @return the _kExcr
	 */
	public Double getKExcr() {
		return _kExcr;
	}

	/**
	 * @param _kExcr the _kExcr to set
	 */
	public void setKExcr(Double _kExcr) {
		this._kExcr = _kExcr;
	}

	public int getNumberOfEngineOverTheWing() {
		return _numberOfEngineOverTheWing;
	}

	public void setNumberOfEngineOverTheWing(int _numberOfEngineOverTheWing) {
		this._numberOfEngineOverTheWing = _numberOfEngineOverTheWing;
	}
	
}

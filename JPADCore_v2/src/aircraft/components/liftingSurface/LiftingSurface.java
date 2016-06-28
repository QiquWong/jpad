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

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.auxiliary.airfoil.creator.AirfoilCreator;
import aircraft.components.Aircraft;
import aircraft.components.liftingSurface.LSAerodynamicsManager.CalcHighLiftDevices;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import aircraft.components.liftingSurface.creator.LiftingSurfacePanelCreator;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.PositionRelativeToAttachmentEnum;
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

	LSAerodynamicsManager theAerodynamics;
	CalcHighLiftDevices _highLiftCalculator;
	
	private PositionRelativeToAttachmentEnum _positionRelativeToAttachment;
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Angle> _riggingAngle = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
	
	private LiftingSurfaceCreator _liftingSurfaceCreator;

	private AerodynamicDatabaseReader _aeroDatabaseReader;
	private HighLiftDatabaseReader _highLiftDatabaseReader;
	
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
	
	private List<Airfoil> _airfoilList;
	private List<Double> _maxThicknessVsY;
	private List<Amount<Length>> _radiusLEVsY;
	private List<Double> _camberRatioVsY;
	private List<Amount<Angle>> _alpha0VsY; 
	private List<Amount<Angle>> _alphaStarVsY;
	private List<Amount<Angle>>_alphaStallVsY;
	private List<Amount<?>> _clAlphaVsY; 
	private List<Double> _cdMinVsY;
	private List<Double> _clAtCdMinVsY;
	private List<Double> _cl0VsY;
	private List<Double> _clStarVsY;
	private List<Double> _clMaxVsY;
	private List<Double> _clMaxSweepVsY;
	private List<Double> _kFactorDragPolarVsY;
	private List<Double> _mExponentDragPolarVsY;
	private List<Double> _cmAlphaQuarteChordVsY;
	private List<Double> _xAcAirfoilVsY;
	private List<Double> _cmACVsY;
	private List<Double> _cmACStallVsY;
	private List<Double> _criticalMachVsY;
	
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

	@Override
	public void calculateMass(Aircraft aircraft, OperatingConditions conditions) {
		calculateMass(aircraft, conditions, MethodEnum.KROO);
		calculateMass(aircraft, conditions, MethodEnum.JENKINSON);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1982);
		calculateMass(aircraft, conditions, MethodEnum.RAYMER);
//		calculateMass(aircraft, conditions, MethodEnum.NICOLAI_2013);
//		calculateMass(aircraft, conditions, MethodEnum.HOWE);
//		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, conditions, MethodEnum.SADRAY);
		calculateMass(aircraft, conditions, MethodEnum.ROSKAM);
	}
	
	/** 
	 * Calculate mass of the generic lifting surface
	 * 
	 * @author Lorenzo Attanasio
	 * @param aircraft
	 * @param method
	 */
	private void calculateMass(
			Aircraft aircraft, 
			OperatingConditions conditions, 
			MethodEnum method) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

		Double surface = this.getSurface().to(MyUnits.FOOT2).getEstimatedValue();
		Double surfaceExposed = aircraft.getExposedWing().getSurface().to(MyUnits.FOOT2).getEstimatedValue();

		Airfoil meanAirfoil = new Airfoil(
				LiftingSurface.calculateMeanAirfoil(aircraft.getWing()),
				aircraft.getWing().getAerodynamicDatabaseReader()
				);
		double _thicknessMean = meanAirfoil.getAirfoilCreator().getThicknessToChordRatio();
		
		switch(_type) {
		case WING : {
			switch (method) {

			/* This method poor results
			 * */
			case ROSKAM : { // Roskam page 85 (pdf) part V
				methodsList.add(method);

				// FIXME : WHO IS MACH DIVE 0 ??
				
				System.out.println("---" + this._liftingSurfaceCreator.getSweepQuarterChordEquivalentWing().to(SI.RADIAN));
				_mass = Amount.valueOf(
						Amount.valueOf(2*(0.00428*
								Math.pow(surface, 0.48)*this.getAspectRatio()*
								Math.pow(aircraft.getThePerformance().getMachDive0(), 0.43)*
								Math.pow(aircraft.getTheWeights().get_MTOW().to(NonSI.POUND_FORCE).
										times(aircraft.getThePerformance().getNUltimate()).
										getEstimatedValue(),0.84)*
								Math.pow(this._liftingSurfaceCreator.getTaperRatioEquivalentWing(), 0.14))/
								(Math.pow(100*this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(),0.76)*
										Math.pow(Math.cos(this.getSweepHalfChordEquivalent(false).to(SI.RADIAN).getEstimatedValue()), 1.54)),
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
						(aircraft.getThePerformance().getNUltimate()*
								Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*
								Math.sqrt(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue()*
										aircraft.getTheWeights().get_MZFM().to(NonSI.POUND).getEstimatedValue())*
								(1 + 2*this._liftingSurfaceCreator.getTaperRatioEquivalentWing()))/
						(_thicknessMean*Math.pow(Math.cos(this._liftingSurfaceCreator.getSweepQuarterChordEquivalentWing().to(SI.RADIAN).getEstimatedValue()),2)*
								surface*(1 + this.getLiftingSurfaceCreator().getTaperRatioEquivalentWing()))),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
				//				} else {
				//					_mass = null;
				//					_massMap.put(method, null);
				//				}
			} break;

			case JENKINSON : { // page 134 Jenkinson - Civil Jet Aircraft Design

				methodsList.add(method);

				if (!aircraft.getPowerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {

					double R, kComp;

					if (compositeCorretionFactor != null) {
						kComp = _compositeCorretionFactor;
					} else {
						kComp = 0.;					
					}

					for (int i = 0; i < 10; i++) {

						try {
							R = _mass.getEstimatedValue() + aircraft.getFuelTank().getFuelMass().getEstimatedValue() +
									((2*(aircraft.getNacelles().get_totalMass().getEstimatedValue() + 
											aircraft.getPowerPlant().get_massDryEngineActual().getEstimatedValue())*
											aircraft.getNacelles().get_distanceBetweenInboardNacellesY())/
											(0.4*this.getSpan().getEstimatedValue())) + 
									((2*(aircraft.getNacelles().get_totalMass().getEstimatedValue() + 
											aircraft.getPowerPlant().get_massDryEngineActual().getEstimatedValue())*
											aircraft.getNacelles().get_distanceBetweenOutboardNacellesY())/
											(0.4*this.getSpan().getEstimatedValue()));
						} catch(NullPointerException e) {R = 0.;}

						_mass = Amount.valueOf(
								(1 - kComp) * 0.021265*
								(pow(aircraft.getTheWeights().get_MTOM().getEstimatedValue()*
										aircraft.getThePerformance().getNUltimate(),0.4843)*
										pow(this.getSurface().getEstimatedValue(),0.7819)*
										pow(this.getAspectRatio(),0.993)*
										pow(1 + this.getLiftingSurfaceCreator().getTaperRatioEquivalentWing(),0.4)*
										pow(1 - R/aircraft.getTheWeights().get_MTOM().getEstimatedValue(),0.4))/
								(cos(this.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().to(SI.RADIAN).getEstimatedValue())*
										pow(_thicknessMean,0.4)), 
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
				_mass = Amount.valueOf(0.0051 * pow(aircraft.getTheWeights().
						get_MTOW().to(NonSI.POUND_FORCE).times(aircraft.getThePerformance().
								getNUltimate()).getEstimatedValue(),
						0.557)*
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(),0.649)*
						pow(this.getAspectRatio(), 0.5)*
						pow(this.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio(), -0.4)*
						pow(1+this.getLiftingSurfaceCreator().getTaperRatioEquivalentWing(), 0.1)*
						pow(cos(this.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().to(SI.RADIAN).getEstimatedValue()), -1)*
						pow(_surfaceCS.to(MyUnits.FOOT2).getEstimatedValue(), 0.1), NonSI.POUND).
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
						*aircraft.getTheWeights().get_materialDensity().getEstimatedValue()*
						_kRho*
						pow((this.getAspectRatio()*aircraft.getThePerformance().getNUltimate())/
								cos(this.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().to(SI.RADIAN).getEstimatedValue()),0.6)*
						pow(this.getLiftingSurfaceCreator().getTaperRatioEquivalentWing(), 0.04), SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			/* The method gives an average 20 percent difference from real value 
			 */
			case TORENBEEK_1982 : {
				methodsList.add(method);
				_mass = Amount.valueOf(
						0.0017*
						aircraft.getTheWeights().get_MZFW().to(NonSI.POUND_FORCE).getEstimatedValue()*
						Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue()/
								Math.cos(this.getSweepHalfChordEquivalent(false).to(SI.RADIAN).getEstimatedValue()),0.75)*
						(1 + Math.pow(6.3*Math.cos(this.getSweepHalfChordEquivalent(false).to(SI.RADIAN).getEstimatedValue())/
								this.getSpan().to(NonSI.FOOT).getEstimatedValue(), 0.5))*
						Math.pow(aircraft.getThePerformance().getNUltimate(), 0.55)*
						Math.pow(
								this.getSpan().to(NonSI.FOOT).getEstimatedValue()*surface/
								(this.getLiftingSurfaceCreator().getPanels().get(0).getAirfoilRoot().getThicknessToChordRatio()
										*this.getChordRoot().to(NonSI.FOOT).getEstimatedValue()*
										aircraft.getTheWeights().get_MZFW().to(NonSI.POUND_FORCE).getEstimatedValue()*
										Math.cos(this.getSweepHalfChordEquivalent(false).to(SI.RADIAN).getEstimatedValue())), 0.3)
						, NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case TORENBEEK_2013 : { // page 253 pdf
				methodsList.add(method);

				//				if (aircraft.get_powerPlant().get_engineType().equals(EngineTypeEnum.TURBOPROP)) {
				_mass = Amount.valueOf(
						(0.0013*
								aircraft.getThePerformance().getNUltimate()*
								Math.pow(aircraft.getTheWeights().get_MTOW()
										.times(aircraft.getTheWeights().get_MZFW()).getEstimatedValue(), 
										0.5)*
								0.36*Math.pow(1 + this.getLiftingSurfaceCreator().getTaperRatioEquivalentWing(), 0.5)*
								(this.getSpan().getEstimatedValue()/100)*
								(this.getAspectRatio()/
										(_thicknessMean
												*Math.pow(
														Math.cos(this.getSweepHalfChordEquivalent(false).to(SI.RADIAN).getEstimatedValue())
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
				double gamma = pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue()*
						aircraft.getThePerformance().getNUltimate(), 0.813)*
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
						pow(aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue(), 0.639)*
						pow(aircraft.getThePerformance().getNUltimate(), 0.1) * 
						pow(this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(NonSI.FOOT).getEstimatedValue(), -1.) *
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 0.75) * 
						pow(0.3*this.getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().to(NonSI.FOOT).getEstimatedValue(), 0.704) * 
						pow(cos(this.getLiftingSurfaceCreator().getSweepQuarterChordEquivalentWing().to(SI.RADIAN).getEstimatedValue()), -1) *
						pow(this.getAspectRatio(), 0.166) * 
						pow(1 + aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().to(NonSI.FOOT).getEstimatedValue()/
								this.getSpan().to(NonSI.FOOT).getEstimatedValue(), -0.25) * 
						pow(1 + _surfaceCS.to(MyUnits.FOOT2).getEstimatedValue()/
								this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 0.1),
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((5.25*surfaceExposed +
						0.8e-6*
						(aircraft.getThePerformance().getNUltimate()*
								Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*
								aircraft.getTheWeights().get_MTOM().to(NonSI.POUND).getEstimatedValue()*
								this.getLiftingSurfaceCreator().getMeanAerodynamicChord().to(NonSI.FOOT).getEstimatedValue()*
								Math.sqrt(surfaceExposed))/
						(_thicknessMean*Math.pow(Math.cos(_sweepStructuralAxis.to(SI.RADIAN).getEstimatedValue()),2)*
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
				if (aircraft.getHTail().getPositionRelativeToAttachment() == PositionRelativeToAttachmentEnum.T_TAIL) {
					k = 1.5;
				} else {
					k = 1.;
				}
				_mass = Amount.valueOf(0.05*k*
						aircraft.getThePerformance().getVDiveEAS().getEstimatedValue()*
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
				if (aircraft.getHTail().getPositionRelativeToAttachment() == PositionRelativeToAttachmentEnum.T_TAIL) { 
					kv = 1 + 0.15*
							(aircraft.getHTail().getSurface().getEstimatedValue()/
									this.getSurface().getEstimatedValue());}
				_mass = Amount.valueOf(kv*3.81*
						aircraft.getThePerformance().getVDiveEAS().to(NonSI.KNOT).getEstimatedValue()*
						pow(this.getSurface().to(MyUnits.FOOT2).getEstimatedValue(), 1.2)/
						(1000*sqrt(cos(this.getSweepHalfChordEquivalent(false).to(SI.RADIAN).getEstimatedValue()))) - 0.287,
						NonSI.POUND).to(SI.KILOGRAM);
				_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
			} break;

			case KROO : {
				methodsList.add(method);
				_mass = Amount.valueOf((2.62*surface +
						1.5e-5*
						(aircraft.getThePerformance().getNUltimate()*
								Math.pow(this.getSpan().to(NonSI.FOOT).getEstimatedValue(),3)*(
										8.0 + 0.44*aircraft.getTheWeights().get_MTOW().to(NonSI.POUND_FORCE).getEstimatedValue()/
										aircraft.getWing().getSurface().to(MyUnits.FOOT2).getEstimatedValue())/
								(_thicknessMean*Math.pow(Math.cos(_sweepStructuralAxis.getEstimatedValue()),2)))),
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

		if (_massCorrectionFactor != null && _massEstimated != null) {
			_massEstimated = _massEstimated.times(_massCorrectionFactor);
		}

		_mass = Amount.valueOf(_massEstimated.getEstimatedValue(), SI.KILOGRAM);

	}

	public void calculateCG(MethodEnum method, ComponentEnum type) {

		List<MethodEnum> methodsList = new ArrayList<MethodEnum>();

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

		Double lambda = _liftingSurfaceCreator.getTaperRatioEquivalentWing(),
				span = getSpan().getEstimatedValue(),
				xRearSpar,
				xFrontSpar;

		switch (type) {
		case WING : {
			switch(method) {

			//		 Bad results ...
			case SFORZA : { // page 359 Sforza (2014) - Aircraft Design
				methodsList.add(method);
				_yCG = Amount.valueOf(
						(span/6) * 
						((1+2*lambda)/(1-lambda)),
						SI.METER);

				_xCG = Amount.valueOf(
						_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
						+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
						, SI.METER);
				_xCGMap.put(method, _xCG);
				_yCGMap.put(method, _yCG);
			} break;

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
						0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
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

				if (_positionRelativeToAttachment
						.equals(PositionRelativeToAttachmentEnum.T_TAIL)) {
					_yCG = Amount.valueOf(
							0.55*(span/2) 
							, SI.METER);
					_xCG = Amount.valueOf(
							0.42*_liftingSurfaceCreator.getChordEquivalentAtY(_yCG.getEstimatedValue())
							+ _liftingSurfaceCreator.getXLEAtYEquivalent(_yCG.getEstimatedValue())
							, SI.METER);
				} else if (
						(_positionRelativeToAttachment
								.equals(PositionRelativeToAttachmentEnum.CONVENTIONAL))
						|| 
						(_positionRelativeToAttachment
								.equals(PositionRelativeToAttachmentEnum.CROSS))
						){
					_yCG = Amount.valueOf(
							0.38*(span/2) 
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
		_percentDifferenceXCG = new Double[_xCGMap.size()];
		_percentDifferenceYCG = new Double[_yCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.set_yLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_yLRFref(), 
				_yCGMap,
				_percentDifferenceYCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}
	
	@Override
	public List<Airfoil> populateAirfoilList(
			AerodynamicDatabaseReader aeroDatabaseReader,
			Boolean equivalentWingFlag
			) {	
		
		int nPanels = this._liftingSurfaceCreator.getPanels().size();

		if(!equivalentWingFlag) {
			Airfoil airfoilRoot = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilRoot);

			for(int i=0; i<nPanels - 1; i++) {

				Airfoil innerAirfoil = new Airfoil(
						this._liftingSurfaceCreator.getPanels().get(i).getAirfoilTip(),
						aeroDatabaseReader
						); 
				this._airfoilList.add(innerAirfoil);
			}

			Airfoil airfoilTip = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(nPanels - 1).getAirfoilTip(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilTip);
		}

		else{
			Airfoil airfoilRoot = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(0).getAirfoilRoot(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilRoot);

			Airfoil airfoilKink = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(0).getAirfoilTip(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilKink);

			Airfoil airfoilTip = new Airfoil(
					this._liftingSurfaceCreator.getPanels().get(1).getAirfoilTip(),
					aeroDatabaseReader
					);
			this._airfoilList.add(airfoilTip);
		}

		discretizeAirfoilCharacteristics(this._airfoilList);
		
		return this._airfoilList;
	}
	
	private void discretizeAirfoilCharacteristics (List<Airfoil> airfoilList) {
		
		for(int i=0; i<airfoilList.size(); i++) {
		
			this._maxThicknessVsY.add(airfoilList.get(i).getAirfoilCreator().getThicknessToChordRatio());
			this._radiusLEVsY.add(airfoilList.get(i).getAirfoilCreator().getRadiusLeadingEdge());
			this._camberRatioVsY.add(airfoilList.get(i).getAirfoilCreator().getCamberRatio());
			this._alpha0VsY.add(airfoilList.get(i).getAirfoilCreator().getAlphaZeroLift());
			this._alphaStarVsY.add(airfoilList.get(i).getAirfoilCreator().getAlphaEndLinearTrait());
			this._alphaStallVsY.add(airfoilList.get(i).getAirfoilCreator().getAlphaStall());
			this._clAlphaVsY.add(airfoilList.get(i).getAirfoilCreator().getClAlphaLinearTrait());
			this._cdMinVsY.add(airfoilList.get(i).getAirfoilCreator().getCdMin());
			this._clAtCdMinVsY.add(airfoilList.get(i).getAirfoilCreator().getClAtCdMin());
			this._cl0VsY.add(airfoilList.get(i).getAirfoilCreator().getClAtAlphaZero());
			this._clStarVsY.add(airfoilList.get(i).getAirfoilCreator().getClEndLinearTrait());
			this._clMaxVsY.add(airfoilList.get(i).getAirfoilCreator().getClMax());
			this._clMaxSweepVsY.add(this._clMaxVsY.get(i)*Math.pow(Math.cos(this.getSweepLEEquivalent(false).doubleValue(SI.RADIAN)),2));
			this._kFactorDragPolarVsY.add(airfoilList.get(i).getAirfoilCreator().getKFactorDragPolar());
			this._mExponentDragPolarVsY.add(airfoilList.get(i).getAirfoilCreator().getMExponentDragPolar());
			this._cmAlphaQuarteChordVsY.add(airfoilList.get(i).getAirfoilCreator().getCmAlphaQuarterChord());
			this._xAcAirfoilVsY.add(airfoilList.get(i).getAirfoilCreator().getXACNormalized());
			this._cmACVsY.add(airfoilList.get(i).getAirfoilCreator().getCmAC());
			this._cmACStallVsY.add(airfoilList.get(i).getAirfoilCreator().getCmACAtStall());
			this._criticalMachVsY.add(airfoilList.get(i).getAirfoilCreator().getMachCritical());
			
		}
	}
	
	public static AirfoilCreator calculateAirfoilAtY (LiftingSurface theWing, double yLoc) {

		// initializing variables ... 
		AirfoilTypeEnum type = null;
		Double yInner = 0.0;
		Double yOuter = 0.0;
		Double thicknessRatioInner = 0.0;
		Double thicknessRatioOuter = 0.0;
		Double camberRatioInner = 0.0;
		Double camberRatioOuter = 0.0;
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
		Double mExponentDragPolarInner = 0.0;
		Double mExponentDragPolarOuter = 0.0;
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
		
		if(yLoc < 0.0) {
			System.err.println("\n\tINVALID Y STATION FOR THE INTERMEDIATE AIRFOIL!!");
			return null;
		}
		
		for(int i=1; i<theWing.getLiftingSurfaceCreator().getYBreakPoints().size(); i++) {
			
			if((yLoc > theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).doubleValue(SI.METER))
					&& (yLoc < theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER))) {
				
				type = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getType();
				yInner = theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1).doubleValue(SI.METER);
				yOuter = theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i).doubleValue(SI.METER);
				thicknessRatioInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getThicknessToChordRatio();
				thicknessRatioOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getThicknessToChordRatio();
				camberRatioInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCamberRatio();
				camberRatioOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCamberRatio();
				leadingEdgeRadiusInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getRadiusLeadingEdge().doubleValue(SI.METER);
				leadingEdgeRadiusOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getRadiusLeadingEdge().doubleValue(SI.METER);
				alphaZeroLiftInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaZeroLift();
				alphaZeroLiftOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaZeroLift();
				alphaEndLinearityInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaEndLinearTrait();
				alphaEndLinearityOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaEndLinearTrait();
				alphaStallInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getAlphaStall();
				alphaStallOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getAlphaStall();
				clAlphaInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getClAlphaLinearTrait().getEstimatedValue(); 
				clAlphaOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getClAlphaLinearTrait().getEstimatedValue();
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
				mExponentDragPolarInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getMExponentDragPolar();
				mExponentDragPolarOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getMExponentDragPolar();
				cmAlphaQuarterChordInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmAlphaQuarterChord();
				cmAlphaQuarterChordOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmAlphaQuarterChord();
				normalizedXacInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getXACNormalized();
				normalizedXacOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getXACNormalized();
				cmACInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmAC();
				cmACOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmAC();
				cmACStallInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getCmACAtStall();
				cmACStallOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getCmACAtStall();
				criticalMachInner = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilRoot().getMachCritical();
				criticalMachOuter = theWing.getLiftingSurfaceCreator().getPanels().get(i-1).getAirfoilTip().getMachCritical();
				
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
		// INTERMEDIATE CAMBER RATIO
		Double intermediateAirfoilCamberRatio = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {camberRatioInner, camberRatioOuter},
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
				SI.METER
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
		// INTERMEDIATE m EXPONENT DRAG POLAR
		Double intermediateAirfoilMExponentDragPolar = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {mExponentDragPolarInner, mExponentDragPolarOuter},
				yLoc
				);
		
		//------------------------------------------------------------------------------------------------
		// INTERMEDIATE Cm ALPHA c/4
		Double intermediateAirfoilCmAlphaQuaterChord = MyMathUtils.getInterpolatedValue1DLinear(
				new double[] {yInner, yOuter},
				new double[] {cmAlphaQuarterChordInner, cmAlphaQuarterChordOuter},
				yLoc
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
		// AIRFOIL CREATION
		AirfoilCreator intermediateAirfoilCreator = new AirfoilCreator.AirfoilBuilder("Intermediate Airfoil")
				.type(type)
				.thicknessToChordRatio(intermediateAirfoilThicknessRatio)
				.camberRatio(intermediateAirfoilCamberRatio)
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
				.mExponentDragPolar(intermediateAirfoilMExponentDragPolar)
				.cmAlphaQuarterChord(intermediateAirfoilCmAlphaQuaterChord)
				.xACNormalized(intermediateAirfoilXac)
				.cmAC(intermediateAirfoilCmAC)
				.cmACAtStall(intermediateAirfoilCmACStall)
				.machCritical(intermediateAirfoilCriticalMach)
				.build();
		
		return intermediateAirfoilCreator;

	}
	
	public static AirfoilCreator calculateMeanAirfoil (LiftingSurface theWing) {
		
		List<Amount<Area>> influenceAreas = new ArrayList<Amount<Area>>();
		List<Double> influenceCoefficients = new ArrayList<Double>();
		
		int nSections = theWing.getAirfoilList().size();
		
		//----------------------------------------------------------------------------------------------
		// calculation of the first influence area ...
		influenceAreas.add(
				Amount.valueOf(
						0.5
						*theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(0).doubleValue(SI.METER)
						*(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(1)
									.minus(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(0))
										.getEstimatedValue()),
						SI.SQUARE_METRE)
				);

		influenceCoefficients.add(
				influenceAreas.get(0)
				.times(2)
				.divide(theWing.getSurface())
				.getEstimatedValue()
				);

		//----------------------------------------------------------------------------------------------
		// calculation of the inner influence areas ... 
		for(int i=1; i<theWing.getAirfoilList().size()-1; i++) {

			influenceAreas.add(
					Amount.valueOf(
							(0.5
									*theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)
									*(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i)
												.minus(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i-1))
													.getEstimatedValue())
									)
							+(0.5
									*theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(i).doubleValue(SI.METER)
									*(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i+1)
												.minus(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(i))
													.getEstimatedValue())
									),
							SI.SQUARE_METRE)
					);

			influenceCoefficients.add(
					influenceAreas.get(i)
					.times(2)
					.divide(theWing.getSurface())
					.getEstimatedValue()
					);

		}
	
	//----------------------------------------------------------------------------------------------
	// calculation of the last influence area ...
	influenceAreas.add(
			Amount.valueOf(
					0.5
					*theWing.getLiftingSurfaceCreator().getChordsBreakPoints().get(nSections-1).doubleValue(SI.METER)
					*(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(nSections-1)
							.minus(theWing.getLiftingSurfaceCreator().getYBreakPoints().get(nSections-2))
							.getEstimatedValue()),
					SI.SQUARE_METRE)
			);
	
	influenceCoefficients.add(
			influenceAreas.get(nSections-1)
			.times(2)
			.divide(theWing.getSurface())
			.getEstimatedValue()
			);
	
	double totalInfluenceArea = 0; 
	for(int i=0; i<influenceAreas.size(); i++)
		totalInfluenceArea += influenceAreas.get(i).getEstimatedValue();
	
	if(theWing.getSurface().getEstimatedValue() - (totalInfluenceArea*2) < 0.001) {
		System.out.println("\tTotal influence area equals the semi-surface. CHECK PASSED!!\n");
		System.out.println("\tTotal inluence area = " + totalInfluenceArea);
		System.out.println("\tWing semi-surface = " + theWing.getSurface().divide(2).getEstimatedValue() + "\n");
	}
	else {
		System.err.println("\n\tTotal influence area differs from the semi-surface. CHECK NOT PASSED!!\n");
		return null;
	}
	
	//----------------------------------------------------------------------------------------------
	// MEAN AIRFOIL DATA CALCULATION:
	
	//----------------------------------------------------------------------------------------------
	// Maximum thickness:
	double maximumThicknessMeanAirfoil = 0;

	for(int i=0; i<influenceCoefficients.size(); i++)
		maximumThicknessMeanAirfoil += influenceCoefficients.get(i)
		*theWing.getAirfoilList().get(i).getAirfoilCreator().getThicknessToChordRatio();

	//----------------------------------------------------------------------------------------------
	// Camber ratio:
	double camberRatioMeanAirfoil = 0;

	for(int i=0; i<influenceCoefficients.size(); i++)
		camberRatioMeanAirfoil += influenceCoefficients.get(i)
		*theWing.getAirfoilList().get(i).getAirfoilCreator().getCamberRatio();
	
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
	// m exponent drag polar:
	double mExponentDragPolarMeanAirfoil = 0;

	for(int i=0; i<influenceCoefficients.size(); i++)
		mExponentDragPolarMeanAirfoil += influenceCoefficients.get(i)
		*theWing.getAirfoilList().get(i).getAirfoilCreator().getMExponentDragPolar();	
	
	//----------------------------------------------------------------------------------------------
	// m exponent drag polar:
	double cmAlphaQuarteChordMeanAirfoil = 0;

	for(int i=0; i<influenceCoefficients.size(); i++)
		cmAlphaQuarteChordMeanAirfoil += influenceCoefficients.get(i)
		*theWing.getAirfoilList().get(i).getAirfoilCreator().getCmAlphaQuarterChord();	
	
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
	
	AirfoilCreator meanAirfoilCreator = new AirfoilCreator.AirfoilBuilder("Mean Airfoil")
			.type(theWing.getAirfoilList().get(0).getType())
			.thicknessToChordRatio(maximumThicknessMeanAirfoil)
			.camberRatio(camberRatioMeanAirfoil)
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
			.mExponentDragPolar(mExponentDragPolarMeanAirfoil)
			.cmAlphaQuarterChord(cmAlphaQuarteChordMeanAirfoil)
			.xACNormalized(xACMeanAirfoil)
			.cmAC(cmACMeanAirfoil)
			.cmACAtStall(cmACStallMeanAirfoil)
			.machCritical(criticalMachMeanAirfoil)
			.build();
	
	return meanAirfoilCreator;
	
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
	public double getTaperRatioEquivalent(Boolean recalculate) {
		if(recalculate)
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return _liftingSurfaceCreator.getTaperRatioEquivalentWing();
	}

	@Override
	public LiftingSurfaceCreator getEquivalentWing(Boolean recalculate) {
		return _liftingSurfaceCreator.getEquivalentWing(recalculate);
	}

	@Override
	public Amount<Length> getChordRootEquivalent(Boolean recalculate) {
		if(recalculate) 
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return _liftingSurfaceCreator.getRootChordEquivalentWing();
	}

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
	public Amount<Angle> getSweepLEEquivalent(Boolean recalculate) {
		if(recalculate)
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return LSGeometryCalc.calculateSweep(
				_liftingSurfaceCreator.getEquivalentWingAspectRatio(),
				_liftingSurfaceCreator.getTaperRatioEquivalentWing(),
				_liftingSurfaceCreator.getSweepQuarterChordEquivalentWing().doubleValue(SI.RADIAN),
				0.0,
				0.25
				).to(NonSI.DEGREE_ANGLE);
				
	}

	@Override
	public Amount<Angle> getSweepHalfChordEquivalent(Boolean recalculate) {
		if(recalculate) 
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return LSGeometryCalc.calculateSweep(
				_liftingSurfaceCreator.getEquivalentWingAspectRatio(),
				_liftingSurfaceCreator.getTaperRatioEquivalentWing(),
				_liftingSurfaceCreator.getSweepQuarterChordEquivalentWing().doubleValue(SI.RADIAN),
				0.0,
				0.5
				).to(NonSI.DEGREE_ANGLE);
	}

	@Override
	public Amount<Angle> getSweepQuarterChordEquivalent(Boolean recalculate) {
		if(recalculate)
			_liftingSurfaceCreator.getEquivalentWing(recalculate);
		return _liftingSurfaceCreator.getSweepQuarterChordEquivalentWing();
	}

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
	public PositionRelativeToAttachmentEnum getPositionRelativeToAttachment() {
		return _positionRelativeToAttachment;
	}

	@Override
	public void setPositionRelativeToAttachment(PositionRelativeToAttachmentEnum _positionRelativeToAttachment) {
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

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public LSAerodynamicsManager getAerodynamics() {
		return theAerodynamics;
	}

	public void setAerodynamics(LSAerodynamicsManager theAerodynamics) {
		this.theAerodynamics = theAerodynamics;
	}
	
	public CalcHighLiftDevices getHigLiftCalculator() {
		return _highLiftCalculator;
	}

	public void setHigLiftCalculator(CalcHighLiftDevices higLiftCalculator) {
		this._highLiftCalculator = higLiftCalculator;
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
}

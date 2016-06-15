package aircraft.components.fuselage;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.calculators.ACPerformanceManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.creator.FuselageCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.nacelles.Nacelle;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.WindshieldType;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class Fuselage implements IFuselage {

	AerodynamicDatabaseReader _aerodynamicDatabaseReader;
	
	private String _id, _description;
	private Amount<Length> _X0, _Y0, _Z0;

	//-----------------------------------------------------------------------
	// DESIGN PARAMETERS
	//-----------------------------------------------------------------------

	// GM = Geometric Mean, RMS = Root Mean Square, AM = Arithmetic Mean
	private Amount<Length> _equivalentDiameterCylinderGM;

	private Amount<Mass> _mass, _massEstimated, _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <MethodEnum, Amount<Length>> _yCGMap = new TreeMap<MethodEnum, Amount<Length>>();

	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Double[] _percentDifference;

	private Boolean _pressurized;
	private Double _massCorrectionFactor = 1.;
	private Amount<Length> _xCG, _xCGReference, 
	_xCGEstimated, _zCGEstimated;
	private Double[] _percentDifferenceXCG;

	private CenterOfGravity _cg;
	private FuselageAerodynamicsManager aerodynamics;

	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);

	private FuselageCreator _fuselageCreator;
	
	//**********************************************************************
	// Builder pattern via a nested public static class
	public static class FuselageBuilder {
		
		private String __id = null;
		private Amount<Length> __xApexConstructionAxes = null; 
		private Amount<Length> __yApexConstructionAxes = null; 
		private Amount<Length> __zApexConstructionAxes = null;
		private FuselageCreator __fuselageCreator;
		
		public FuselageBuilder(String id) {
			// required parameter
			this.__id = id;

			// optional parameters ...

		}

		public FuselageBuilder fuselageCreator(FuselageCreator fuselage) {
			this.__fuselageCreator = fuselage;
			return this;
		}
		
		public Fuselage build() {
			return new Fuselage(this);
		}

	}
	
	//**********************************************************************
	
	private Fuselage(FuselageBuilder builder) {
		super();
		this._id = builder.__id; 
		this._xApexConstructionAxes = builder.__xApexConstructionAxes; 
		this._yApexConstructionAxes = builder.__yApexConstructionAxes; 
		this._zApexConstructionAxes = builder.__zApexConstructionAxes;
		this._fuselageCreator = builder.__fuselageCreator;
	}
	
	@Override
	public int getDeckNumber() {
		return _fuselageCreator.getDeckNumber();
	}

	@Override
	public Amount<Length> getLength() {
		return _fuselageCreator.getLength();
	}

	@Override
	public Amount<Mass> getReferenceMass() {
		return _fuselageCreator.getMassReference();
	}

	@Override
	public Amount<Length> getRoughness() {
		return _fuselageCreator.getRoughness();
	}

	@Override
	public Double getNoseLengthRatio() {
		return _fuselageCreator.getLenRatioNF();
	}

	@Override
	public Double getFinesseRatio() {
		return _fuselageCreator.getLambdaN();
	}

	@Override
	public Amount<Length> getNoseTipHeightOffset() {
		return _fuselageCreator.getHeightN();
	}

	@Override
	public Double getNoseDxCapPercent() {
		return _fuselageCreator.getDxNoseCapPercent();
	}

	@Override
	public WindshieldType getWindshieldType() {
		return _fuselageCreator.getWindshieldType();
	}

	@Override
	public Amount<Length> getWindshieldWidht() {
		return _fuselageCreator.getWindshieldWidth();
	}

	@Override
	public Amount<Length> getWindshieldHeight() {
		return _fuselageCreator.getWindshieldHeight();
	}

	@Override
	public Double getNoseMidSectionLowerToTotalHeightRatio() {
		return _fuselageCreator.getSectionNoseMidLowerToTotalHeightRatio();
	}

	@Override
	public Double getNoseMidSectionRhoUpper() {
		return _fuselageCreator.getSectionMidNoseRhoUpper();
	}

	@Override
	public Double getNoseMidSectionRhoLower() {
		return _fuselageCreator.getSectionMidNoseRhoLower();
	}

	@Override
	public Double getCylindricalLengthRatio() {
		return _fuselageCreator.getLenRatioCF();
	}

	@Override
	public Amount<Length> getSectionWidht() {
		return _fuselageCreator.getSectionCylinderWidth();
	}

	@Override
	public Amount<Length> getSectionHeight() {
		return _fuselageCreator.getSectionCylinderHeight();
	}

	@Override
	public Amount<Length> getHeightFromGround() {
		return _fuselageCreator.getHeightFromGround();
	}

	@Override
	public Double getSectionLowerToTotalHeightRatio() {
		return _fuselageCreator.getSectionCylinderLowerToTotalHeightRatio();
	}

	@Override
	public Double getSectionRhoUpper() {
		return _fuselageCreator.getSectionCylinderRhoUpper();
	}

	@Override
	public Double getSectionRhoLower() {
		return _fuselageCreator.getSectionCylinderRhoLower();
	}

	@Override
	public Amount<Length> getTailTipHeightOffset() {
		return _fuselageCreator.getHeightT();
	}

	@Override
	public Double getTailDxCapPercent() {
		return _fuselageCreator.getDxTailCapPercent();
	}

	@Override
	public Double getTailMidSectionLowerToTotalHeightRatio() {
		return _fuselageCreator.getSectionTailMidLowerToTotalHeightRatio();
	}

	@Override
	public Double getTailMidSectionRhoUpper() {
		return _fuselageCreator.getSectionMidTailRhoUpper();
	}

	@Override
	public Double getTailMidSectionRhoLower() {
		return _fuselageCreator.getSectionMidTailRhoLower();
	}

	@Override
	public List<SpoilerCreator> getSpoilers() {
		return _fuselageCreator.getSpoilers();
	}

	@Override
	public void calculateGeometry(
			int np_N, int np_C, int np_T, // no. points @ Nose/Cabin/Tail
			int np_SecUp, int np_SecLow   // no. points @ Upper/Lower section
			) {
		_fuselageCreator.calculateGeometry(np_N, np_C, np_T, np_SecUp, np_SecLow);
	}

	@Override
	public Amount<Area> getSurfaceWetted(Boolean recalculate) {
		return _fuselageCreator.getSurfaceWetted(recalculate);
	}

	// TODO : ADD OTHER GETTERS VIA _fuselageCreator
	
	@Override
	public FuselageCreator getFuselageCreator() {
		return _fuselageCreator;
	}
	
	//=========================================================================================================================================
	// 	END CONSTRUCTOR VIA BUILDER PATTERN
	//=========================================================================================================================================


	public FuselageAerodynamicsManager initializeAerodynamics(OperatingConditions ops, Aircraft aircraft) {
		_aerodynamicDatabaseReader = aircraft.get_theAerodynamics().get_aerodynamicDatabaseReader();
		aerodynamics = new FuselageAerodynamicsManager(ops, aircraft);
		return aerodynamics;
	}
	
	
	
	

	///////////////////////////////////////////////////////////////////
	// Methods for evaluation of derived quantities (mass, cd...)
	///////////////////////////////////////////////////////////////////

	public void calculateStructure(OperatingConditions conditions,
			Aircraft configuration, 
			ACPerformanceManager performances,
			MethodEnum method) {

	}

	public void calculateMass(Aircraft aircraft, 
			OperatingConditions conditions) {
		calculateMass(aircraft, conditions, MethodEnum.RAYMER);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, conditions, MethodEnum.JENKINSON);
		calculateMass(aircraft, conditions, MethodEnum.KROO);
		calculateMass(aircraft, conditions, MethodEnum.SADRAY);
		calculateMass(aircraft, conditions, MethodEnum.NICOLAI_1984);
		calculateMass(aircraft, conditions, MethodEnum.ROSKAM);
	}


	@SuppressWarnings("unchecked")
	@Override
	public void calculateMass(Aircraft aircraft, 
			OperatingConditions conditions, 
			MethodEnum method) {

		switch (method){

		/* 80 percent difference from true mass for some aircraft 
		 * */
		case JENKINSON : { // page 150 Jenkinson - Civil Jet Aircraft Design
			_methodsList.add(method);

			double k = 0.;

			if (_pressurized == true) {
				k = k + 0.08;
			}

			if (aircraft.get_theNacelles().get_nacellesList().get(0).get_mounting() == Nacelle.MountingPosition.FUSELAGE) {
				k = k + 0.04;
			}

			if (aircraft.get_landingGear().get_mounting() == LandingGear.LandingGears.FUSELAGE) {
				k = k + 0.07;
			}

			_mass = Amount.valueOf(0.039*
					Math.pow((1 + k) * 
							2*_fuselageCreator.getLenF().getEstimatedValue()*
							_equivalentDiameterCylinderGM.getEstimatedValue()*
							Math.pow(aircraft.get_performances().get_vDiveEAS().getEstimatedValue(),0.5),
							1.5), SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 *//*
		case NICOLAI_1984 : {
			_methodsList.add(method);
			_mass = Amount.valueOf(
					0.0737*
					pow(2*_equivalentDiameterCylinderGM.getEstimatedValue()*
							pow(aircraft.get_performances().get_vDiveEAS().getEstimatedValue(), 0.338) * 
							pow(_fuselageCreator.getLenF().getEstimatedValue(), 0.857)*
							pow(aircraft.get_weights().get_MTOM().getEstimatedValue()*
									aircraft.get_performances().get_nUltimate(), 0.286)
									, 1.1)
									, SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		  *//*
		case ROSKAM : { // page 92 Roskam page 92 (pdf) part V (Nicolai 2013 is the same)
			// TODO
			double Kinlet = 1.0;
			_methodsList.add(method);
			_mass = Amount.valueOf(2*10.43*
					pow(Kinlet, 1.42)*
					pow(
							aircraft.get_performances().get_maxDynamicPressure().to(MyUnits.LB_FT2).getEstimatedValue()/100,
							0.283)*
							pow(aircraft.get_weights().get_MTOM().to(NonSI.POUND).getEstimatedValue()/1000, 0.95)*
							pow(_fuselageCreator.getLenF().divide(_fuselageCreator.getSectionCylinderHeight()).getEstimatedValue(), 0.71), 
							NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), _mass.getUnit()));
		} break;
//		   */
		case RAYMER : { // page 403 Raymer - Aircraft Design a conceptual approach
			_mass = calculateMassRaymer(aircraft);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
		/* 18 % average difference from actual value
		 * */
		case SADRAY : { // page 585 Sadray Aircraft Design System Engineering Approach
			_methodsList.add(method);
			double Kinlet = 1.;
			double kRho = 0.0032;
			_mass = Amount.valueOf(_fuselageCreator.getLenF().getEstimatedValue()*
					pow(_equivalentDiameterCylinderGM.getEstimatedValue(),2)*
					aircraft.get_weights().get_materialDensity().getEstimatedValue()*
					kRho*
					pow(aircraft.get_performances().get_nUltimate(),0.25)*
					Kinlet,
					SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 */
		//		 The method gives poor results
		/*
		 * */
		case KROO : { // page 432 Stanford University pdf
			_methodsList.add(method);
			double Ifuse;
			double Ip = 1.5e-3 * 
					conditions.get_maxDeltaP().to(MyUnits.LB_FT2).getEstimatedValue()*
					_fuselageCreator.getSectionCylinderWidth().to(NonSI.FOOT).getEstimatedValue();

			double Ib = 1.91e-4 * aircraft.get_performances().get_nLimitZFW() * 
					(aircraft.get_weights().get_MZFM().to(NonSI.POUND).getEstimatedValue() - 
//							_liftingSurface.get_wing().get_mass().to(NonSI.POUND).getEstimatedValue()
							aircraft.getWing().get_mass().to(NonSI.POUND).getEstimatedValue()
//												- aircraft.get_nacelle().get_mass().getEstimatedValue()*aircraft.get_propulsion().get_engineNumber()) TODO ADD!
		 * _fuselageCreator.getLenF().minus(aircraft.getWing().get_chordRoot().divide(2.)).to(NonSI.FOOT).getEstimatedValue()/
							pow(_fuselageCreator.getSectionCylinderHeight().to(NonSI.FOOT).getEstimatedValue(),2));

			if (Ip > Ib) {
				Ifuse = Ip;
			} else {
				Ifuse = (Math.pow(Ip,2) + Math.pow(Ib,2))/(2*Ib); 
			}

			_mass = Amount.valueOf((1.051 + 0.102*Ifuse)*
					_fuselageCreator.getsWet().to(MyUnits.FOOT2).getEstimatedValue(), NonSI.POUND).to(SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 */
		case TORENBEEK_2013 : {
			_mass = calculateMassTorenbeek2013(aircraft.get_performances().get_nUltimate());
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		case TORENBEEK_1976 : { // page 302 Synthesis 1976
			_mass = calculateMassTorenbeek1976(aircraft);
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

		default : { } break;

		}

		if (_massCorrectionFactor != null) {
			_mass = _mass.times(_massCorrectionFactor);
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference =  new Double[_massMap.size()]; 

		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_massReference, 
				_massMap,
				_percentDifference,
				100.).getFilteredMean(), SI.KILOGRAM);

		_mass = Amount.valueOf(_massEstimated.getEstimatedValue(), SI.KILOGRAM); 
	}

	private Amount<Mass> calculateMassRaymer(Aircraft aircraft) {
		double Kdoor = 1.0;
		double Klg = 1.12;
		double Kws = 0.75*
				((1+2*aircraft.getWing().get_taperRatioEquivalent())/
						(1+aircraft.getWing().get_taperRatioEquivalent()))*
						aircraft.getWing().get_span().to(NonSI.FOOT).getEstimatedValue()*
						tan(aircraft.getWing().get_sweepQuarterChordEq().to(SI.RADIAN).getEstimatedValue())/
						_fuselageCreator.getLenF().to(NonSI.FOOT).getEstimatedValue();

		return Amount.valueOf(0.328*
				Kdoor*Klg*
				pow(aircraft.get_weights().
						get_MTOM().to(NonSI.POUND).times(aircraft.get_performances().
								get_nUltimate()).getEstimatedValue(),
								0.5)*
								pow(_fuselageCreator.getLenF().to(NonSI.FOOT).getEstimatedValue(),0.25)*
								pow(_fuselageCreator.getsWet().to(MyUnits.FOOT2).getEstimatedValue(), 0.302)*
								pow(1+Kws, 0.04)*
								pow(_fuselageCreator.getLenF().to(NonSI.FOOT).
										divide(_equivalentDiameterCylinderGM.to(NonSI.FOOT)).getEstimatedValue(), 0.1), 
										NonSI.POUND).to(SI.KILOGRAM);
	}

	private Amount<Mass> calculateMassTorenbeek2013(double nUltimate) {
		return Amount.valueOf((60*
				pow(_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue(),2)*
				(_fuselageCreator.getLenF().getEstimatedValue() + 1.5)+
				160*pow(nUltimate, 0.5)*
				_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue()*
				_fuselageCreator.getLenF().getEstimatedValue()),
				SI.NEWTON).divide(AtmosphereCalc.g0).to(SI.KILOGRAM);

	}

	private Amount<Mass> calculateMassTorenbeek1976(Aircraft aircraft) {
		double k = 0.;
		if (_pressurized) {k = k + 0.08;}
		if (aircraft.get_landingGear().get_mounting() == LandingGear.LandingGears.FUSELAGE){
			k = k + 0.07;
		}

		return Amount.valueOf((1 + k) * 0.23 * 
				Math.sqrt(
						aircraft.get_performances().get_vDiveEAS().getEstimatedValue() *
						aircraft.getHTail().get_ACw_ACdistance().getEstimatedValue()/
						(2*_equivalentDiameterCylinderGM.getEstimatedValue())) *
						Math.pow(_fuselageCreator.getsWet().getEstimatedValue(), 1.2),
						SI.KILOGRAM);
	}

	public void calculateCG(Aircraft aircraft, OperatingConditions conditions) {
		calculateCG(aircraft, conditions, MethodEnum.SFORZA);
		calculateCG(aircraft, conditions, MethodEnum.TORENBEEK_1982);
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			OperatingConditions conditions,
			MethodEnum method) {

		_cg.setLRForigin(_X0, _Y0, _Z0);
		_cg.set_xLRFref(_fuselageCreator.getLenF().times(0.45));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(_Z0.getEstimatedValue(), SI.METER));

		// Initialize _methodsList again to clear it
		// from old entries
		_methodsList = new ArrayList<MethodEnum>();

		switch(method) {

		// page 359 Sforza (2014) - Aircraft Design
		case SFORZA : { 
			_methodsList.add(method);

			_xCG = Amount.valueOf(
					_fuselageCreator.getLenF().divide(_fuselageCreator.getLambdaF()).getEstimatedValue()*
					(_fuselageCreator.getLambdaN()+ (_fuselageCreator.getLambdaF() - 5.)/1.8)
					, SI.METER);
			_xCGMap.put(method, _xCG);
		} break;

		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);

			if (aircraft.get_powerPlant().get_engineNumber() == 1 && 
					(aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.PISTON |
					aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP)) {

				_xCG = _fuselageCreator.getLenF().times(0.335);
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.WING) {
				if ((aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.PISTON |
						aircraft.get_powerPlant().get_engineType() == EngineTypeEnum.TURBOPROP)) {
					_xCG = _fuselageCreator.getLenF().times(0.39); 
				} else {
					_xCG = _fuselageCreator.getLenF().times(0.435);
				}
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.REAR_FUSELAGE) {
				_xCG = _fuselageCreator.getLenF().times(0.47);
			}

			if (aircraft.get_powerPlant().get_position() == EngineMountingPositionEnum.BURIED) {
				_xCG = _fuselageCreator.getLenF().times(0.45);
			}

			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);
		_percentDifferenceXCG = new Double[_xCGMap.size()];

		_cg.set_xLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
				_cg.get_xLRFref(), 
				_xCGMap,
				_percentDifferenceXCG,
				30.).getFilteredMean(), SI.METER));

		_cg.calculateCGinBRF();

	}


		public String get_name() {
		return _name;
	}

	public void set_name(String n) {
		this._name = n;
	}

	public String get_description() {
		return _description;
	}


	public Amount<Mass> get_mass() {
		return _mass;
	}

	public void set_mass(Amount<Mass> _mass) {
		this._mass = _mass;
	}


	public Map<MethodEnum, Amount<Mass>> get_massMap() {
		return _massMap;
	}

	@Override
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}


	@Override
	public Amount<Length> get_X0() { return _X0; }

	@Override
	public void set_X0(Amount<Length> x) { _X0 = x; };

	@Override
	public Amount<Length> get_Y0() { return _Y0; }

	@Override
	public void set_Y0(Amount<Length> y) { _Y0 = y; };

	@Override
	public Amount<Length> get_Z0() { return _Z0; }

	@Override
	public void set_Z0(Amount<Length> z) { _Z0 = z; }

	
	public Double get_massCorrectionFactor() {
		return _massCorrectionFactor;
	}


	public void set_massCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}


	public Amount<Mass> get_massEstimated() {
		return _massEstimated;
	}


	public Amount<Length> get_xCGReference() {
		return _xCGReference;
	}
	

	public void set_xCGReference(Amount<Length> _xCGReference) {
		this._xCGReference = _xCGReference;
	}


	public Map<MethodEnum, Amount<Length>> get_xCGMap() {
		return _xCGMap;
	}


	public Amount<Length> get_xCGEstimated() {
		return _xCGEstimated;
	}


	public Double[] get_percentDifferenceXCG() {
		return _percentDifferenceXCG;
	}


	public Amount<Length> get_zCGEstimated() {
		return _zCGEstimated;
	}


	public CenterOfGravity get_cg() {
		return _cg;
	}


	public void set_cg(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	@Override
	public ComponentEnum getType() {
		return _type;
	}


	public FuselageAerodynamicsManager getAerodynamics() {
		return aerodynamics;
	}

	public void setAerodynamics(FuselageAerodynamicsManager aerodynamics) {
		this.aerodynamics = aerodynamics;
	}

	
	// --------------------------- Already done --------------------------------------

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setId(String _id) {
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public void setFuselageCreator(FuselageCreator _fuselageCreator) {
		this._fuselageCreator = _fuselageCreator;
	}
	// -----------------------------------------------------------------


} // end of class

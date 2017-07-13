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
import javax.xml.bind.annotation.XmlTransient;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.LandingGears;
import aircraft.components.fuselage.creator.FuselageCreator;
import aircraft.components.liftingSurface.creator.SpoilerCreator;
import aircraft.components.nacelles.NacelleCreator;
import analyses.fuselage.FuselageAerodynamicsManager;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.WindshieldTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

public class Fuselage implements IFuselage {

	AerodynamicDatabaseReader _aerodynamicDatabaseReader;
	
	private String _id;
	private Amount<Length> _X0;
	private Amount<Length> _Y0;
	private Amount<Length> _Z0;
	private ComponentEnum _type = ComponentEnum.FUSELAGE; 
	private Double _kExcr = 0.0;
	
	//-----------------------------------------------------------------------
	// DESIGN PARAMETERS
	//-----------------------------------------------------------------------

	// GM = Geometric Mean, RMS = Root Mean Square, AM = Arithmetic Mean
	private Amount<Mass> _mass, _massEstimated;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();

	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>();
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();
	private Double[] _percentDifference;

	private Double _massCorrectionFactor = 1.;
	private Amount<Length> _xCG, _xCGReference, _xCGEstimated, _zCGEstimated;
	private Double[] _percentDifferenceXCG;

	private CenterOfGravity _cg;
	private FuselageAerodynamicsManager aerodynamics;

	private FusDesDatabaseReader _fusDesDatabaseReader;
	
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
		private FusDesDatabaseReader __fusDesDatabaseReader;
		
		public FuselageBuilder(
				String id,
				FusDesDatabaseReader fusDesDatabaseReader) {
			// required parameter
			this.__id = id;
			this.__fusDesDatabaseReader = fusDesDatabaseReader;

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
		this._fusDesDatabaseReader =builder.__fusDesDatabaseReader;
	}
	
	
	
	@Override
	public int getDeckNumber() {
		return _fuselageCreator.getDeckNumber();
	}

	@Override
	public Amount<Length> getLength() {
		return _fuselageCreator.getLenF();
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
	public WindshieldTypeEnum getWindshieldType() {
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
	public Amount<Area> getsWet() {
		return _fuselageCreator.getsWet();
	}
	
	@Override
	@XmlTransient
	public FuselageCreator getFuselageCreator() {
		return _fuselageCreator;
	}
	
	//=========================================================================================================================================
	// 	END CONSTRUCTOR VIA BUILDER PATTERN
	//=========================================================================================================================================


//	public FuselageAerodynamicsManager initializeAerodynamics(OperatingConditions ops, Aircraft aircraft) {
//		_aerodynamicDatabaseReader = aircraft.getWing().getAerodynamicDatabaseReader();
//		aerodynamics = new FuselageAerodynamicsManager(ops, aircraft);
//		return aerodynamics;
//	}
	
	public void calculateMass(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMapWeights) {
		calculateMass(aircraft, MethodEnum.RAYMER);
		calculateMass(aircraft, MethodEnum.TORENBEEK_1976);
		calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		calculateMass(aircraft, MethodEnum.JENKINSON);
		calculateMass(aircraft, MethodEnum.KROO);
		calculateMass(aircraft, MethodEnum.SADRAY);
		calculateMass(aircraft, MethodEnum.NICOLAI_1984);
		calculateMass(aircraft, MethodEnum.ROSKAM);
		
		if(!methodsMapWeights.get(ComponentEnum.FUSELAGE).equals(MethodEnum.AVERAGE)) { 
			_massEstimated = _massMap.get(methodsMapWeights.get(ComponentEnum.FUSELAGE));
			_percentDifference =  new Double[_massMap.size()];
		}
		else {
			_percentDifference =  new Double[_massMap.size()];
			_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					this._fuselageCreator.getMassReference(), 
					_massMap,
					_percentDifference,
					100.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}


	@Override
	public void calculateMass(Aircraft aircraft,  
			MethodEnum method
			) {

		switch (method){

		/* 80 percent difference from true mass for some aircraft 
		 * */
		case JENKINSON : { // page 150 Jenkinson - Civil Jet Aircraft Design
			_methodsList.add(method);

			double k = 0.;

			if (_fuselageCreator.getPressurized() == true) {
				k = k + 0.08;
			}

			// NACELLE ON THE H-TAIL IS ASSUMED TO INCREASE THE AIRCRAFT WEIGHT AS THEY ARE MOUNTED ON THE FUSELAGE
			if (aircraft.getNacelles().getNacellesList().get(0).getMountingPosition() == NacelleCreator.MountingPosition.FUSELAGE
					|| aircraft.getNacelles().getNacellesList().get(0).getMountingPosition() == NacelleCreator.MountingPosition.HTAIL) {
				k = k + 0.04;
			}

			if (aircraft.getLandingGears().getMountingPosition() == LandingGears.MountingPosition.FUSELAGE) {
				k = k + 0.07;
			}

			_mass = Amount.valueOf(0.039*
					Math.pow((1 + k) * 
							2*_fuselageCreator.getLenF().getEstimatedValue()*
							getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue()*
							Math.pow(aircraft.getTheAnalysisManager().getVDiveEAS().getEstimatedValue(),0.5),
							1.5), SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 *//*
		case NICOLAI_1984 : {
			_methodsList.add(method);
			_mass = Amount.valueOf(
					0.0737*
					pow(2*getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue()*
							pow(aircraft.getTheAnalysisManager().getVDiveEAS().getEstimatedValue(), 0.338) * 
							pow(_fuselageCreator.getLenF().getEstimatedValue(), 0.857)*
							pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().getEstimatedValue()*
									aircraft.getTheAnalysisManager().getNUltimate(), 0.286)
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
							aircraft.getTheAnalysisManager().getMaxDynamicPressure().to(MyUnits.LB_FT2).getEstimatedValue()/100,
							0.283)*
							pow(aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().to(NonSI.POUND).getEstimatedValue()/1000, 0.95)*
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
					pow(getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue(),2)*
					aircraft.getTheAnalysisManager().getTheWeights().getMaterialDensity().getEstimatedValue()*
					kRho*
					pow(aircraft.getTheAnalysisManager().getNUltimate(),0.25)*
					Kinlet,
					SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 
//		-------------------------------
//		 The method gives poor results
//	    -------------------------------
//
//		case KROO : { // page 432 Stanford University pdf
//			_methodsList.add(method);
//			double Ifuse;
//			double Ip = 1.5e-3 * 
//					conditions.get_maxDeltaPressure().to(MyUnits.LB_FT2).getEstimatedValue()*
//					_fuselageCreator.getSectionCylinderWidth().to(NonSI.FOOT).getEstimatedValue();
//
//			double Ib = 1.91e-4 * aircraft.getThePerformance().getNLimitZFW() * 
//					(aircraft.getTheWeights().getMaximumZeroFuelMass().to(NonSI.POUND).getEstimatedValue() - 
////							_liftingSurface.get_wing().get_mass().to(NonSI.POUND).getEstimatedValue()
//							aircraft.getWing().getMass().to(NonSI.POUND).getEstimatedValue()
////												- aircraft.get_nacelle().get_mass().getEstimatedValue()*aircraft.get_propulsion().get_engineNumber()) TODO ADD!
//		 * _fuselageCreator.getLenF().minus(aircraft.getWing().getChordRoot().divide(2.)).to(NonSI.FOOT).getEstimatedValue()/
//							pow(_fuselageCreator.getSectionCylinderHeight().to(NonSI.FOOT).getEstimatedValue(),2));
//
//			if (Ip > Ib) {
//				Ifuse = Ip;
//			} else {
//				Ifuse = (Math.pow(Ip,2) + Math.pow(Ib,2))/(2*Ib); 
//			}
//
//			_mass = Amount.valueOf((1.051 + 0.102*Ifuse)*
//					_fuselageCreator.getsWet().to(MyUnits.FOOT2).getEstimatedValue(), NonSI.POUND).to(SI.KILOGRAM);
//			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
//		} break;
//		 */
		case TORENBEEK_2013 : {
			_mass = calculateMassTorenbeek2013(aircraft.getTheAnalysisManager().getNUltimate());
			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;

//		case TORENBEEK_1976 : { // page 302 Synthesis 1976
//			_mass = calculateMassTorenbeek1976(aircraft);
//			_methodsList.add(method);
//			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
//		} break;

		default : { } break;

		}

		if (_massCorrectionFactor != null) {
			_mass = _mass.times(_massCorrectionFactor);
		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
//		_percentDifference =  new Double[_massMap.size()]; 
//
//		_massEstimated = Amount.valueOf(JPADStaticWriteUtils.compareMethods(
//				this._fuselageCreator.getMassReference(), 
//				_massMap,
//				_percentDifference,
//				100.).getFilteredMean(), SI.KILOGRAM);
//
//		_mass = Amount.valueOf(_massEstimated.getEstimatedValue(), SI.KILOGRAM); 
	}

	private Amount<Mass> calculateMassRaymer(Aircraft aircraft) {
		double Kdoor = 1.0;
		double Klg = 1.12;
		double Kws = 0.75*
				((1+2*aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio())/
						(1+aircraft.getWing().getLiftingSurfaceCreator().getEquivalentWing().getPanels().get(0).getTaperRatio()))*
						aircraft.getWing().getSpan().to(NonSI.FOOT).getEstimatedValue()*
						tan(aircraft.getWing().getLiftingSurfaceCreator().getPanels().get(0).getSweepQuarterChord().to(SI.RADIAN).getEstimatedValue())/
						_fuselageCreator.getLenF().to(NonSI.FOOT).getEstimatedValue();

		return Amount.valueOf(0.328*
				Kdoor*Klg*
				pow(aircraft.getTheAnalysisManager().getTheWeights().
						getMaximumTakeOffMass().to(NonSI.POUND).times(aircraft.getTheAnalysisManager().
								getNUltimate()).getEstimatedValue(),
								0.5)*
								pow(_fuselageCreator.getLenF().to(NonSI.FOOT).getEstimatedValue(),0.25)*
								pow(_fuselageCreator.getsWet().to(MyUnits.FOOT2).getEstimatedValue(), 0.302)*
								pow(1+Kws, 0.04)*
								pow(_fuselageCreator.getLenF().to(NonSI.FOOT).
										divide(getFuselageCreator().getEquivalentDiameterCylinderGM().to(NonSI.FOOT)).getEstimatedValue(), 0.1), 
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

//	private Amount<Mass> calculateMassTorenbeek1976(Aircraft aircraft) {
//		double k = 0.;
//		if (_fuselageCreator.getPressurized()) {k = k + 0.08;}
//		if (aircraft.getLandingGears().getMountingPosition() == LandingGears.MountingPosition.FUSELAGE){
//			k = k + 0.07;
//		}
//
//		return Amount.valueOf((1 + k) * 0.23 * 
//				Math.sqrt(
//						aircraft.getThePerformance().getVDiveEAS().getEstimatedValue() *
//						aircraft.getHTail().getLiftingSurfaceCreator().getLiftingSurfaceACToWingACdistance().getEstimatedValue()/
//						(2*getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue())) *
//						Math.pow(_fuselageCreator.getsWet().getEstimatedValue(), 1.2),
//						SI.KILOGRAM);
//	}

	public void calculateCG(Aircraft aircraft, Map<ComponentEnum, MethodEnum> methodsMap) {
		calculateCG(aircraft, MethodEnum.SFORZA);
		calculateCG(aircraft, MethodEnum.TORENBEEK_1982);
		
		if(!methodsMap.get(ComponentEnum.FUSELAGE).equals(MethodEnum.AVERAGE)) 
			_cg.setXLRF(_xCGMap.get(methodsMap.get(ComponentEnum.FUSELAGE)));
		else {
			_percentDifferenceXCG = new Double[_xCGMap.size()];
			_cg.setXLRF(Amount.valueOf(JPADStaticWriteUtils.compareMethods(
					_cg.getXLRFref(), 
					_xCGMap,
					_percentDifferenceXCG,
					30.).getFilteredMean(), SI.METER));
		}
		_cg.calculateCGinBRF(ComponentEnum.FUSELAGE);
	}
	
	@Override
	public void calculateCG(
			Aircraft aircraft, 
			MethodEnum method) {

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);
		_cg.set_xLRFref(_fuselageCreator.getLenF().times(0.45));
		_cg.set_yLRFref(Amount.valueOf(0., SI.METER));
		_cg.set_zLRFref(Amount.valueOf(_zApexConstructionAxes.getEstimatedValue(), SI.METER));

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

			if (aircraft.getPowerPlant().getEngineNumber() == 1 && 
					(aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
					aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {

				_xCG = _fuselageCreator.getLenF().times(0.335);
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.WING) {
				if ((aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
						aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {
					_xCG = _fuselageCreator.getLenF().times(0.39); 
				} else {
					_xCG = _fuselageCreator.getLenF().times(0.435);
				}
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.REAR_FUSELAGE) {
				_xCG = _fuselageCreator.getLenF().times(0.47);
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.BURIED) {
				_xCG = _fuselageCreator.getLenF().times(0.45);
			}

			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);

	}

	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}


	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	@Override
	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}


	@Override
	public Amount<Length> getX0() { return _X0; }

	@Override
	public void setX0(Amount<Length> x) { _X0 = x; };

	@Override
	public Amount<Length> getY0() { return _Y0; }

	@Override
	public void setY0(Amount<Length> y) { _Y0 = y; };

	@Override
	public Amount<Length> getZ0() { return _Z0; }

	@Override
	public void setZ0(Amount<Length> z) { _Z0 = z; }

	
	public Double getMassCorrectionFactor() {
		return _massCorrectionFactor;
	}


	public void set_massCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}


	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public void setMassEstimated(Amount<Mass> massEstimated) {
		this._massEstimated = massEstimated;
	}
	
	public Amount<Length> get_xCGReference() {
		return _xCGReference;
	}
	

	public void set_xCGReference(Amount<Length> _xCGReference) {
		this._xCGReference = _xCGReference;
	}


	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}


	public Amount<Length> get_xCGEstimated() {
		return _xCGEstimated;
	}


	public Double[] getPercentDifferenceXCG() {
		return _percentDifferenceXCG;
	}


	public Amount<Length> get_zCGEstimated() {
		return _zCGEstimated;
	}


	public CenterOfGravity getCG() {
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

	@Override
	public String getId() {
		return _fuselageCreator.getId();
	}
	
	@Override
	public void setId(String _id) {
		_fuselageCreator.setId(_id);
	}

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
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



	public Double[] getPercentDifference() {
		return _percentDifference;
	}



	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
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



	public FusDesDatabaseReader getFusDesDatabaseReader() {
		return _fusDesDatabaseReader;
	}



	public void setFusDesDatabaseReader(FusDesDatabaseReader _fusDesDatabaseReader) {
		this._fusDesDatabaseReader = _fusDesDatabaseReader;
	}


} // end of class

package aircraft.components.fuselage;

import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.creator.FuselageCreator;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.EngineMountingPositionEnum;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.NacelleMountingPositionEnum;
import database.databasefunctions.aerodynamics.fusDes.FusDesDatabaseReader;
import standaloneutils.MyUnits;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.CenterOfGravity;
import writers.JPADStaticWriteUtils;

/**
 * The Fuselage class is in charge of handling the component position in BRF (body reference frame) as well as
 * the component mass and center of gravity. Furthermore, it contains the FuselageCreator object which manages 
 * all the geometrical parameters. 
 * 
 * @author Vittorio Trifari
 *
 */
public class Fuselage {

	//----------------------------------------------------------------------
	// VARIABLE DECLARATIONS
	//----------------------------------------------------------------------
	private FusDesDatabaseReader _fusDesDatabaseReader;
	
	private Amount<Length> _xApexConstructionAxes = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _zApexConstructionAxes = Amount.valueOf(0.0, SI.METER);
	
	private FuselageCreator _fuselageCreator;
	
	private Amount<Mass> _mass, _massEstimated, _massReference;
	private Map <MethodEnum, Amount<Mass>> _massMap = new TreeMap<MethodEnum, Amount<Mass>>();
	private Map <MethodEnum, Amount<Length>> _xCGMap = new TreeMap<MethodEnum, Amount<Length>>();
	private Map <AnalysisTypeEnum, List<MethodEnum>> _methodsMap = new HashMap<AnalysisTypeEnum, List<MethodEnum>>(); 
	private List<MethodEnum> _methodsList = new ArrayList<MethodEnum>();  
	private Double[] _percentDifference;       
	private Double _massCorrectionFactor = 1.; 
	
	private Amount<Length> _xCG, _xCGReference, _xCGEstimated, _zCGEstimated;
	private CenterOfGravity _cg;
	private Double[] _percentDifferenceXCG;

	//------------------------------------------------------------------------------------
	// BUILDER
	//------------------------------------------------------------------------------------
	public Fuselage(FuselageCreator theFuselageCreator) {
		
		this._fuselageCreator = theFuselageCreator;
		
	}
	
	//------------------------------------------------------------------------------------
	// METHODS
	//------------------------------------------------------------------------------------
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
					_massReference, 
					_massMap,
					_percentDifference,
					100.).getFilteredMean(), SI.KILOGRAM);
		}
		
	}
	
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
			if (aircraft.getNacelles().getNacellesList().get(0).getMountingPosition() == NacelleMountingPositionEnum.FUSELAGE
					|| aircraft.getNacelles().getNacellesList().get(0).getMountingPosition() == NacelleMountingPositionEnum.HTAIL) {
				k = k + 0.04;
			}

			if (aircraft.getLandingGears().getMountingPosition() == LandingGearsMountingPositionEnum.FUSELAGE) {
				k = k + 0.07;
			}

			_mass = Amount.valueOf(0.039*
					Math.pow((1 + k) * 
							2*_fuselageCreator.getFuselageLength().getEstimatedValue()*
							_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue()*
							Math.pow(aircraft.getTheAnalysisManager().getVDiveEAS().getEstimatedValue(),0.5),
							1.5), SI.KILOGRAM);
			_massMap.put(method, Amount.valueOf(round(_mass.getEstimatedValue()), SI.KILOGRAM));
		} break;
//		 *//*
		case NICOLAI_1984 : {
			_methodsList.add(method);
			_mass = Amount.valueOf(
					0.0737*
					pow(2*_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue()*
							pow(aircraft.getTheAnalysisManager().getVDiveEAS().getEstimatedValue(), 0.338) * 
							pow(_fuselageCreator.getFuselageLength().getEstimatedValue(), 0.857)*
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
							pow(_fuselageCreator.getFuselageLength().divide(_fuselageCreator.getSectionCylinderHeight()).getEstimatedValue(), 0.71), 
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
			_mass = Amount.valueOf(_fuselageCreator.getFuselageLength().getEstimatedValue()*
					pow(_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue(),2)*
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
						_fuselageCreator.getFuselageLength().to(NonSI.FOOT).getEstimatedValue();

		return Amount.valueOf(0.328*
				Kdoor*Klg*
				pow(aircraft.getTheAnalysisManager().getTheWeights().
						getMaximumTakeOffMass().to(NonSI.POUND).times(aircraft.getTheAnalysisManager().
								getNUltimate()).getEstimatedValue(),
								0.5)*
								pow(_fuselageCreator.getFuselageLength().to(NonSI.FOOT).getEstimatedValue(),0.25)*
								pow(_fuselageCreator.getSWetTotal().to(MyUnits.FOOT2).getEstimatedValue(), 0.302)*
								pow(1+Kws, 0.04)*
								pow(_fuselageCreator.getFuselageLength().to(NonSI.FOOT).
										divide(_fuselageCreator.getEquivalentDiameterCylinderGM().to(NonSI.FOOT)).getEstimatedValue(), 0.1), 
										NonSI.POUND).to(SI.KILOGRAM);
	}

	private Amount<Mass> calculateMassTorenbeek2013(double nUltimate) {
		return Amount.valueOf((60*
				pow(_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue(),2)*
				(_fuselageCreator.getFuselageLength().getEstimatedValue() + 1.5)+
				160*pow(nUltimate, 0.5)*
				_fuselageCreator.getEquivalentDiameterCylinderGM().getEstimatedValue()*
				_fuselageCreator.getFuselageLength().getEstimatedValue()),
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
	
	public void calculateCG(
			Aircraft aircraft, 
			MethodEnum method) {

		_cg = new CenterOfGravity();
		
		_cg.setLRForigin(_xApexConstructionAxes, _yApexConstructionAxes, _zApexConstructionAxes);
		_cg.set_xLRFref(_fuselageCreator.getFuselageLength().times(0.45));
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
					_fuselageCreator.getFuselageLength().divide(_fuselageCreator.getFuselageFinenessRatio()).getEstimatedValue()*
					(_fuselageCreator.getNoseFinenessRatio()+ (_fuselageCreator.getFuselageFinenessRatio() - 5.)/1.8)
					, SI.METER);
			_xCGMap.put(method, _xCG);
		} break;

		// page 313 Torenbeek (1982)
		case TORENBEEK_1982 : { 
			_methodsList.add(method);

			if (aircraft.getPowerPlant().getEngineNumber() == 1 && 
					(aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
					aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {

				_xCG = _fuselageCreator.getFuselageLength().times(0.335);
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.WING) {
				if ((aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.PISTON |
						aircraft.getPowerPlant().getEngineType() == EngineTypeEnum.TURBOPROP)) {
					_xCG = _fuselageCreator.getFuselageLength().times(0.39); 
				} else {
					_xCG = _fuselageCreator.getFuselageLength().times(0.435);
				}
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.REAR_FUSELAGE) {
				_xCG = _fuselageCreator.getFuselageLength().times(0.47);
			}

			if (aircraft.getPowerPlant().getMountingPosition() == EngineMountingPositionEnum.BURIED) {
				_xCG = _fuselageCreator.getFuselageLength().times(0.45);
			}

			_xCGMap.put(method, _xCG);
		} break;

		default : break;

		}

		_methodsMap.put(AnalysisTypeEnum.BALANCE, _methodsList);

	}
	
	//------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	//------------------------------------------------------------------------------------
	public FusDesDatabaseReader getFusDesDatabaseReader() {
		return _fusDesDatabaseReader;
	}

	public void setFusDesDatabaseReader(FusDesDatabaseReader _fusDesDatabaseReader) {
		this._fusDesDatabaseReader = _fusDesDatabaseReader;
	}

	public Amount<Length> getXApexConstructionAxes() {
		return _xApexConstructionAxes;
	}

	public void setXApexConstructionAxes(Amount<Length> _xApexConstructionAxes) {
		this._xApexConstructionAxes = _xApexConstructionAxes;
	}

	public Amount<Length> getYApexConstructionAxes() {
		return _yApexConstructionAxes;
	}

	public void setYApexConstructionAxes(Amount<Length> _yApexConstructionAxes) {
		this._yApexConstructionAxes = _yApexConstructionAxes;
	}

	public Amount<Length> getZApexConstructionAxes() {
		return _zApexConstructionAxes;
	}

	public void setZApexConstructionAxes(Amount<Length> _zApexConstructionAxes) {
		this._zApexConstructionAxes = _zApexConstructionAxes;
	}

	public FuselageCreator getFuselageCreator() {
		return _fuselageCreator;
	}

	public void setFuselageCreator(FuselageCreator _fuselageCreator) {
		this._fuselageCreator = _fuselageCreator;
	}

	public Amount<Mass> getMass() {
		return _mass;
	}

	public void setMass(Amount<Mass> _mass) {
		this._mass = _mass;
	}

	public Amount<Mass> getMassEstimated() {
		return _massEstimated;
	}

	public void setMassEstimated(Amount<Mass> _massEstimated) {
		this._massEstimated = _massEstimated;
	}

	public Amount<Mass> getMassReference() {
		return _massReference;
	}

	public void setMassReference(Amount<Mass> _massReference) {
		this._massReference = _massReference;
	}

	public Map<MethodEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public void setMassMap(Map<MethodEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}

	public Map<MethodEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<MethodEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<AnalysisTypeEnum, List<MethodEnum>> getMethodsMap() {
		return _methodsMap;
	}

	public void setMethodsMap(Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap) {
		this._methodsMap = _methodsMap;
	}

	public List<MethodEnum> getMethodsList() {
		return _methodsList;
	}

	public void setMethodsList(List<MethodEnum> _methodsList) {
		this._methodsList = _methodsList;
	}

	public Double[] getPercentDifference() {
		return _percentDifference;
	}

	public void setPercentDifference(Double[] _percentDifference) {
		this._percentDifference = _percentDifference;
	}

	public Double getMassCorrectionFactor() {
		return _massCorrectionFactor;
	}

	public void setMassCorrectionFactor(Double _massCorrectionFactor) {
		this._massCorrectionFactor = _massCorrectionFactor;
	}

	public Amount<Length> getXCG() {
		return _xCG;
	}

	public void setXCG(Amount<Length> _xCG) {
		this._xCG = _xCG;
	}

	public Amount<Length> getXCGReference() {
		return _xCGReference;
	}

	public void setXCGReference(Amount<Length> _xCGReference) {
		this._xCGReference = _xCGReference;
	}

	public Amount<Length> getXCGEstimated() {
		return _xCGEstimated;
	}

	public void setXCGEstimated(Amount<Length> _xCGEstimated) {
		this._xCGEstimated = _xCGEstimated;
	}

	public Amount<Length> getZCGEstimated() {
		return _zCGEstimated;
	}

	public void setZCGEstimated(Amount<Length> _zCGEstimated) {
		this._zCGEstimated = _zCGEstimated;
	}

	public CenterOfGravity getCG() {
		return _cg;
	}

	public void setCG(CenterOfGravity _cg) {
		this._cg = _cg;
	}

	public Double[] getPercentDifferenceXCG() {
		return _percentDifferenceXCG;
	}

	public void setPercentDifferenceXCG(Double[] _percentDifferenceXCG) {
		this._percentDifferenceXCG = _percentDifferenceXCG;
	}
	
} 

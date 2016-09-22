package aircraft.calculators;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Force;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.OperatingConditions;
import aircraft.components.Aircraft;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.atmosphere.AtmosphereCalc;

/**
 * Manage components weight calculations
 * 
 * @author Lorenzo Attanasio
 */
public class ACWeightsManager extends ACCalculatorManager {

	private static final String id = "20";
	private AnalysisTypeEnum _type;
	private String _name;

	private Amount<Force> _MTOW, _TOW, _MZFW, _TFOW, 
	_OEW, _emptyWeight, _fuelWeight,
	_actualOEW, _MLW, _manufacturerEmptyWeight;

	private Amount<Mass> _MTOM, _fuselageMass, _MZFM, 
	_paxMass, _crewMass, _paxMassMax, _TFOM, 
	_OEM, _ZFM, _emptyMass, 
	_actualOEM, _actualEmptyMass, _structuralMass,
	_MLM, _manufacturerEmptyMass,
	_OIM, _TOM, _paxSingleMass;

	private Amount<VolumetricDensity> _materialDensity;

	private List<Amount<Mass>> _MTOMList = new ArrayList<Amount<Mass>>();
	private List<Amount<Mass>> _massStructureList = new ArrayList<Amount<Mass>>();

	public ACWeightsManager() {

		super();

		_name = "Weights";
		_type = AnalysisTypeEnum.WEIGHTS;

		_MTOM = Amount.valueOf(22000., SI.KILOGRAM); // ATR72 MTOM, Jane's page 294

		//		_mtomIter = MyMathUtils.linspace(.*);
		//		_mtomResult = new double[_mtomIter.length];

		_MZFM = Amount.valueOf(20000., SI.KILOGRAM); // ATR72 MZFM, Jane's page 294

		_MLM = Amount.valueOf(21850., SI.KILOGRAM);
		_MLW = _MLM.times(AtmosphereCalc.g0).to(SI.NEWTON);

		_OEM = Amount.valueOf(12950., SI.KILOGRAM);

		// Actuators oils
		_TFOM = Amount.valueOf(0., SI.KILOGRAM);

		// Aluminum density
		_materialDensity = Amount.valueOf(2711., VolumetricDensity.UNIT);

		// 84 kg assumed for each passenger + 15 kg baggage (EASA 2008.C.06) 
		_paxSingleMass = Amount.valueOf(99.0, SI.KILOGRAM);

	}

	/**
	 * Overload of the standard builder that recognize the aircraft and sets it's relative 
	 * values.
	 * 
	 * @author Vittorio Trifari
	 */
	public ACWeightsManager(AircraftEnum aircraftName) {

		super();

		switch(aircraftName) {
		case ATR72:
			_name = "Weights";
			_type = AnalysisTypeEnum.WEIGHTS;
			
			_MTOM = Amount.valueOf(23063.5789, SI.KILOGRAM); // ATR72 MTOM, REPORT_ATR72
			
			//		_mtomIter = MyMathUtils.linspace(.*);
			//		_mtomResult = new double[_mtomIter.length];
			
			_MZFM = Amount.valueOf(20063.5789, SI.KILOGRAM); // ATR72 MZFM, REPORT_ATR72
			
			_MLM = Amount.valueOf(20757.2210, SI.KILOGRAM);
			_MLW = _MLM.times(AtmosphereCalc.g0).to(SI.NEWTON);
			
			_OEM = Amount.valueOf(12935.5789, SI.KILOGRAM);
			
			// Actuators oils
			_TFOM = Amount.valueOf(0., SI.KILOGRAM);
			
			// Aluminum density
			_materialDensity = Amount.valueOf(2711., VolumetricDensity.UNIT);
			
			// 84 kg assumed for each passenger + 15 kg baggage (EASA 2008.C.06) 
			_paxSingleMass = Amount.valueOf(99.0, SI.KILOGRAM);
			break;
			
		case B747_100B:
			_name = "Weights";
			_type = AnalysisTypeEnum.WEIGHTS;
			
			_MTOM = Amount.valueOf(354991.5060, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
			
			//		_mtomIter = MyMathUtils.linspace(.*);
			//		_mtomResult = new double[_mtomIter.length];
			
			_MZFM = Amount.valueOf(207581.9860, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
			
			_MLM = Amount.valueOf(319517.5554, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
			_MLW = _MLM.times(AtmosphereCalc.g0).to(SI.NEWTON); 
			
			_OEM = Amount.valueOf(153131.9860, SI.KILOGRAM);
			
			// Actuators oils
			_TFOM = Amount.valueOf(0.005*(_MTOM.getEstimatedValue()), SI.KILOGRAM);
			
			// Aluminum density
			_materialDensity = Amount.valueOf(2711., VolumetricDensity.UNIT);
			
			// 84 kg assumed for each passenger + 15 kg baggage (EASA 2008.C.06) 
			_paxSingleMass = Amount.valueOf(99.0, SI.KILOGRAM);
			break;
			
		case AGILE_DC1:
			_name = "Weights";
			_type = AnalysisTypeEnum.WEIGHTS;
			
			_MTOM = Amount.valueOf(36336, SI.KILOGRAM); // ADAS project
			
			//		_mtomIter = MyMathUtils.linspace(.*);
			//		_mtomResult = new double[_mtomIter.length];
			
			_MZFM = Amount.valueOf(29716, SI.KILOGRAM); // 
			
			_MLM = Amount.valueOf(32702.4, SI.KILOGRAM);
			_MLW = _MLM.times(AtmosphereCalc.g0).to(SI.NEWTON);
			
			_OEM = Amount.valueOf(20529, SI.KILOGRAM);
			
			// Actuators oils
			_TFOM = Amount.valueOf(0., SI.KILOGRAM);
			
			// Aluminum density
			_materialDensity = Amount.valueOf(2711., VolumetricDensity.UNIT);
			
			// 102 kg (225 lbs) assumed for each passenger (AGILE_2.5 doc) 
			// Into ADAS 159lbs + 66lbs = 225 lbs 
			_paxSingleMass = Amount.valueOf(102.0, SI.KILOGRAM);
			break;
			
		case IRON:
			_name = "Weights";
			_type = AnalysisTypeEnum.WEIGHTS;
			
			_MTOM = Amount.valueOf(54037, SI.KILOGRAM); // ATR72 MTOM, REPORT_ATR72
			
			//		_mtomIter = MyMathUtils.linspace(.*);
			//		_mtomResult = new double[_mtomIter.length];
			
			_MZFM = Amount.valueOf(45990, SI.KILOGRAM); // ATR72 MZFM, REPORT_ATR72
			
			// 90% MTOM
			_MLM = Amount.valueOf(48633.3, SI.KILOGRAM);
			_MLW = _MLM.times(AtmosphereCalc.g0).to(SI.NEWTON);
			
			_OEM = Amount.valueOf(33076, SI.KILOGRAM);
			
			// Actuators oils
			_TFOM = Amount.valueOf(270., SI.KILOGRAM);
			
			// Aluminum density
			_materialDensity = Amount.valueOf(2711., VolumetricDensity.UNIT);
			
			// 79 kg assumed for each passenger + 14 kg baggage (EASA 2008.C.06) 
			_paxSingleMass = Amount.valueOf(93.0, SI.KILOGRAM);
			break;
		}
	}

	public void calculateDependentVariables(Aircraft aircraft) {

		// Passengers and crew mass
		// 76.5 kg for each crew member + baggage
		_paxMass = _paxSingleMass.times(aircraft.get_configuration().get_nPax());
		_crewMass = Amount.valueOf(aircraft.get_configuration().get_nCrew() * 76.5145485, SI.KILOGRAM); 

		// Passengers and crew mass
		_paxMassMax = _paxSingleMass.times(aircraft.get_configuration().get_maxPax());

		// Operating items mass
		if (aircraft.get_performances().get_range().getEstimatedValue() < 2000) { 
			_OIM = Amount.valueOf(8.617*aircraft.get_configuration().get_nPax(), SI.KILOGRAM);
		} else {
			_OIM = Amount.valueOf(14.97*aircraft.get_configuration().get_nPax(), SI.KILOGRAM);
		}

		_actualEmptyMass =_OEM.minus(_crewMass).minus(_TFOM);

		_MTOW = _MTOM.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_MZFW = _MZFM.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_OEW = _OEM.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_TFOW = _TFOM.times(AtmosphereCalc.g0).to(SI.NEWTON);

	}


	public void calculate() {

		_emptyWeight = _emptyMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_OEM = _TFOM.plus(_crewMass).plus(_emptyMass);
		_OEW = _TFOM.times(AtmosphereCalc.g0).to(SI.NEWTON);

	}


	/** 
	 * Calculate mass of selected configuration. When comparing some/all available methods 
	 * for the selected component the iterative procedure is done using the first selected method.
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param methodsMap
	 */
	public void calculateAllMasses(Aircraft aircraft, 
			OperatingConditions conditions,
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		System.out.println("----- WEIGHT ESTIMATION PROCEDURE STARTED -----");

		calculateFirstGuessMTOM(aircraft);

		aircraft.get_theFuelTank().calculateFuel();

		int i=0;
		_MTOMList.add(Amount.valueOf(0.0, SI.KILOGRAM));

		Amount<Mass> sum = Amount.valueOf(0., SI.KILOGRAM);

		// Evaluate MTOM 5 times and then take the mean value to avoid
		// an infinite loop due to MTOM estimate oscillation 
		while (i < 5) {

			_MTOMList.add(_MTOM);

			aircraft.get_weights().calculateDependentVariables(aircraft);

			//////////////////////////////////////////////////////////////////
			// Evaluate weights with more than one method for each component
			//////////////////////////////////////////////////////////////////

			// --- STRUCTURE MASS-----------------------------------

			calculateStructuralMass(conditions, aircraft, methodsMap);

			// --- END OF STRUCTURE MASS-----------------------------------

//			aircraft.get_powerPlant().calculateMass(aircraft, conditions,
//					MyMethodEnum.HARRIS, MyMethodEnum.TORENBEEK_2013);
			
			aircraft.get_powerPlant().calculateMass();

			// --- END OF POWER PLANT MASS-----------------------------------

			calculateManufacturerEmptyMass(aircraft, conditions);

			// --- END OF MANUFACTURER EMPTY MASS-----------------------------------

			aircraft.get_weights().set_OEM(
					aircraft.get_weights().get_manufacturerEmptyMass().plus(
							aircraft.get_weights().get_OIM()).plus(
									aircraft.get_weights().get_crewMass()));

			// --- END OF OPERATING EMPTY MASS-----------------------------------

			// Zero fuel mass
			aircraft.get_weights().set_ZFM(
					aircraft.get_weights().get_OEM().plus(
							_paxMass));

			// Maximum zero fuel mass
			aircraft.get_weights().set_MZFM(
					aircraft.get_weights().get_OEM().plus(
							_paxMassMax));

			// --- END ZERO FUEL MASS-----------------------------------

			// Actual passenger mass
			aircraft.get_weights().set_TOM(
					aircraft.get_weights().get_ZFM().plus(
							aircraft.get_theFuelTank().get_fuelMass()));

			// Maximum passenger mass
			aircraft.get_weights().set_MTOM(
					aircraft.get_weights().get_MZFM().plus(
							aircraft.get_theFuelTank().get_fuelMass()));

			// Maximum landing mass
			aircraft.get_weights().set_MLM(_MTOM.times(0.9));

			System.out.println("Iteration " + (i) + 
					", Structure mass: " + aircraft.get_weights().get_structuralMass() + 
					" , MTOM: " + _MTOM);

			sum = sum.plus(_MTOM);
			i++;
			_MTOM = sum.divide(i);

			aircraft.get_weights().calculateDependentVariables(aircraft);
		}

		_massStructureList.add(aircraft.get_fuselage().get_massEstimated());
		_massStructureList.add(aircraft.get_wing().get_massEstimated());
		_massStructureList.add(aircraft.get_HTail().get_massEstimated());
		_massStructureList.add(aircraft.get_VTail().get_massEstimated());
		_massStructureList.addAll(aircraft.get_theNacelles().get_massList());
		_massStructureList.add(aircraft.get_landingGear().get_massEstimated());

		System.out.println("----- WEIGHT ESTIMATION PROCEDURE FINISHED -----\n");

	}

	public void calculateStructuralMass(
			OperatingConditions conditions,
			Aircraft aircraft, 
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		aircraft.get_fuselage().calculateMass(aircraft, conditions);

		aircraft.get_wing().calculateMass(aircraft, conditions);
		aircraft.get_HTail().calculateMass(aircraft, conditions);
		aircraft.get_VTail().calculateMass(aircraft, conditions);

		aircraft.get_theNacelles().calculateMass();
//		aircraft.          get_theNacelles().calculateMass();

		aircraft.get_landingGear().calculateMass(aircraft, conditions);

		aircraft.get_systems().calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);

		aircraft.get_weights().set_structuralMass(
				aircraft.get_fuselage().get_massEstimated().plus(
						aircraft.get_wing().get_massEstimated()).plus(
								aircraft.get_HTail().get_massEstimated()).plus(
										aircraft.get_VTail().get_massEstimated()).plus(
												aircraft.get_theNacelles().get_totalMass()).plus(
//												aircraft.get_theNacelles().  getWeights()).plus(
														aircraft.get_landingGear().get_massEstimated()));

	}

	public void calculateManufacturerEmptyMass(Aircraft aircraft, OperatingConditions conditions) {
		aircraft.get_systems().calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
		aircraft.get_configuration().calculateMass(aircraft, conditions, MethodEnum.TORENBEEK_2013);
		aircraft.get_weights().set_manufacturerEmptyMass(
				aircraft.get_powerPlant().get_totalMass().plus(
						aircraft.get_weights().get_structuralMass()).plus(
								aircraft.get_systems().get_mass()).plus(
										aircraft.get_configuration().get_massEstimatedFurnishingsAndEquipment()));
	}


	public void calculateFirstGuessMTOM(Aircraft aircraft) {

		aircraft.get_fuselage().set_mass(aircraft.get_weights().get_MZFM().times(.15));
		aircraft.get_wing().set_mass(aircraft.get_weights().get_MZFM().times(.1));
		aircraft.get_HTail().set_mass(aircraft.get_weights().get_MZFM().times(.015));
		aircraft.get_VTail().set_mass(aircraft.get_weights().get_MZFM().times(.015));
		aircraft.get_powerPlant().set_mass(aircraft.get_weights().get_MZFM().times(.05));
		aircraft.get_theNacelles().set_totalMass(aircraft.get_weights().get_MZFM().times(.015));
		aircraft.get_theFuelTank().set_fuelMass(aircraft.get_weights().get_MZFM().times(.015));
		aircraft.get_landingGear().set_mass(aircraft.get_weights().get_MZFM().times(.04));
		aircraft.get_systems().set_mass(aircraft.get_weights().get_MZFM().times(.04));

		aircraft.get_weights().set_structuralMass(
				aircraft.get_fuselage().get_mass().plus(
						aircraft.get_wing().get_mass()).plus(
								aircraft.get_HTail().get_mass()).plus(
										aircraft.get_VTail().get_mass()).plus(
												aircraft.get_theNacelles().get_totalMass()).plus(
														aircraft.get_landingGear().get_mass()));

		System.out.println("First guess value:" + aircraft.get_weights().get_structuralMass().getEstimatedValue());
	}

	public Amount<Mass> get_MTOM() {
		return _MTOM;
	}

	public void set_MTOM(Amount<Mass> _MTOM) {
		this._MTOM = _MTOM;
	}

	public Amount<Mass> get_MZFM() {
		return _MZFM;
	}

	public Amount<Force> get_MTOW() {
		return _MTOW;
	}


	public Amount<Force> get_MZFW() {
		return _MZFW;
	}


	public Amount<Mass> get_fuselageMass() {
		return _fuselageMass;
	}


	public Amount<Mass> get_paxMass() {
		return _paxMass;
	}


	public Amount<Mass> get_crewMass() {
		return _crewMass;
	}


	public Amount<Mass> get_emptyMass() {
		return _emptyMass;
	}


	public void set_emptyMass(Amount<Mass> _emptyMass) {
		this._emptyMass = _emptyMass;
	}


	public Amount<Force> get_emptyWeight() {
		return _emptyWeight;
	}


	public void set_emptyWeight(Amount<Force> _emptyWeight) {
		this._emptyWeight = _emptyWeight;
	}


	public Amount<Force> get_fuelWeight() {
		return _fuelWeight;
	}


	public void set_fuelWeight(Amount<Force> _fuelWeight) {
		this._fuelWeight = _fuelWeight;
	}


	public Amount<Force> get_actualOEW() {
		return _actualOEW;
	}


	public void set_actualOEW(Amount<Force> _actualOEW) {
		this._actualOEW = _actualOEW;
	}


	public Amount<Mass> get_actualOEM() {
		return _actualOEM;
	}


	public Amount<Mass> get_actualEmptyMass() {
		return _actualEmptyMass;
	}


	public Amount<Mass> get_structuralMass() {
		return _structuralMass;
	}


	public void set_structuralMass(Amount<Mass> _structureMass) {
		this._structuralMass = _structureMass;
	}


	public Amount<Force> get_structuralWeight() {
		return _structuralMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
	}


	public Amount<VolumetricDensity> get_materialDensity() {
		return _materialDensity;
	}


	public void set_materialDensity(Amount<VolumetricDensity> _materialDensity) {
		this._materialDensity = _materialDensity;
	}


	public Amount<Force> get_MLW() {
		return _MLW;
	}


	public void set_MLW(Amount<Force> _MLW) {
		this._MLW = _MLW;
	}


	public Amount<Mass> get_MLM() {
		return _MLM;
	}


	public void set_MLM(Amount<Mass> _MLM) {
		this._MLM = _MLM;
	}


	public Amount<Mass> get_OIM() {
		return _OIM;
	}


	public void set_OIM(Amount<Mass> _OIM) {
		this._OIM = _OIM;
	}


	public Amount<Mass> get_manufacturerEmptyMass() {
		return _manufacturerEmptyMass;
	}


	public void set_manufacturerEmptyMass(Amount<Mass> _manufacturerEmptyMass) {
		this._manufacturerEmptyMass = _manufacturerEmptyMass;
	}


	public Amount<Force> get_manufacturerEmptyWeight() {
		return _manufacturerEmptyWeight;
	}


	public void set_manufacturerEmptyWeight(Amount<Force> _manufacturerEmptyWeight) {
		this._manufacturerEmptyWeight = _manufacturerEmptyWeight;
	}


	public Amount<Mass> get_OEM() {
		return _OEM;
	}


	public void set_OEM(Amount<Mass> _OEM) {
		this._OEM = _OEM;
	}


	public Amount<Mass> get_ZFM() {
		return _ZFM;
	}


	public void set_ZFM(Amount<Mass> _ZFM) {
		this._ZFM = _ZFM;
	}


	public Amount<Mass> get_paxMassMax() {
		return _paxMassMax;
	}


	public void set_MZFM(Amount<Mass> _MZFM) {
		this._MZFM = _MZFM;
	}


	public Amount<Mass> get_TOM() {
		return _TOM;
	}


	public void set_TOM(Amount<Mass> _TOM) {
		this._TOM = _TOM;
	}


	public Amount<Force> get_TOW() {
		return _TOW;
	}


	public void set_TOW(Amount<Force> TOW) {
		_TOW = TOW;
	}

	public Amount<Mass> get_paxSingleMass() {
		return _paxSingleMass;
	}


	public void set_paxSingleMass(Amount<Mass> _paxSingleMass) {
		this._paxSingleMass = _paxSingleMass;
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


	public List<Amount<Mass>> get_massStructureList() {
		return _massStructureList;
	}


	public void set_massStructureList(List<Amount<Mass>> _massStructureList) {
		this._massStructureList = _massStructureList;
	}


	public static String getId() {
		return id;
	}


}


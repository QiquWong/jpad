package analyses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

/**
 * Manage components weight calculations
 * 
 * @author Lorenzo Attanasio
 */
public class ACWeightsManager extends ACCalculatorManager {

	private String _id;
	private static final AnalysisTypeEnum _type = AnalysisTypeEnum.WEIGHTS;
	
	// Aluminum density
	public static Amount<VolumetricDensity> _materialDensity = 
			Amount.valueOf(2711.0,VolumetricDensity.UNIT);

	// 84 kg assumed for each passenger + 15 kg baggage (EASA 2008.C.06) 
	public static Amount<Mass> _paxSingleMass = Amount.valueOf(99.0, SI.KILOGRAM);
	
	//---------------------------------------------------------------------------------
	// INPUT DATA : 
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _maximumLandingMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _trappedFuelOilMass;
	private Amount<Length> _referenceRange;
	
	//---------------------------------------------------------------------------------
	// OUTPUT DATA : 
	private Amount<Mass> _paxMass;
	private Amount<Mass> _paxMassMax;
	private Amount<Mass> _crewMass;
	private Amount<Mass> _operatingItemMass;
	private Amount<Force> _operatingItemWeight;
	private Amount<Mass> _emptyMass;
	private Amount<Force> _emptyWeight;
	private Amount<Force> _maximumTakeOffWeight;
	private Amount<Force> _maximumZeroFuelWeight;
	private Amount<Force> _operatingEmptyWeight;
	private Amount<Force> _trappedFuelOilWeight;
	private Amount<Force> _maximumLandingWeight;
	private Amount<Force> _manufacturerEmptyWeight;
	private Amount<Mass> _structuralMass;
	private Amount<Mass> _manufacturerEmptyMass;
	private Amount<Mass> _zeroFuelMass;
	private Amount<Mass> _takeOffMass;
	private Amount<Force> _zeroFuelWeight;
	private Amount<Force> _takeOffWeight;

	private List<Amount<Mass>> _maximumTakeOffMassList;
	private List<Amount<Mass>> _massStructureList;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACWeightsManagerBuilder {
		
		// required parameters
		private String __id;

		// optional parameters ... defaults
		// ...
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __maximumLandingMass;
		private Amount<Mass> __operatingEmptyMass;
		private Amount<Mass> __trappedFuelOilMass;
		private Amount<Length> __referenceRange;

		private List<Amount<Mass>> __maximumTakeOffMassList = new ArrayList<Amount<Mass>>();
		private List<Amount<Mass>> __massStructureList = new ArrayList<Amount<Mass>>();
		
		public ACWeightsManagerBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumZeroFuelMass(Amount<Mass> maximumZeroFuelMass) {
			this.__maximumZeroFuelMass = maximumZeroFuelMass;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumLandingMass(Amount<Mass> maximumLandingMass) {
			this.__maximumLandingMass = maximumLandingMass;
			return this;
		}
		
		public ACWeightsManagerBuilder operatingEmptyMass(Amount<Mass> operatingEmptyMass) {
			this.__operatingEmptyMass = operatingEmptyMass;
			return this;
		}
		
		public ACWeightsManagerBuilder trappedFuelOilMass(Amount<Mass> trappedFuelOilMass) {
			this.__trappedFuelOilMass = trappedFuelOilMass;
			return this;
		}
		
		public ACWeightsManagerBuilder referenceRange(Amount<Length> referenceRange) {
			this.__referenceRange = referenceRange;
			return this;
		}
		
		public ACWeightsManagerBuilder (String id) {
			this.__id = id;
			this.initializeDefaultData(AircraftEnum.ATR72);
		}
		
		public ACWeightsManagerBuilder (String id, AircraftEnum aircraftName) {
			this.__id = id;
			this.initializeDefaultData(aircraftName);
		}
		
		private void initializeDefaultData(AircraftEnum aircraftName) {

			switch(aircraftName) {
			case ATR72:
				__maximumTakeOffMass = Amount.valueOf(23063.5789, SI.KILOGRAM); // ATR72 MTOM, REPORT_ATR72
				__maximumZeroFuelMass = Amount.valueOf(20063.5789, SI.KILOGRAM); // ATR72 MZFM, REPORT_ATR72
				__maximumLandingMass = Amount.valueOf(20757.2210, SI.KILOGRAM);
				__operatingEmptyMass = Amount.valueOf(12935.5789, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
				__referenceRange = Amount.valueOf(1528.0, SI.KILOMETER);
				break;
				
			case B747_100B:
				__maximumTakeOffMass = Amount.valueOf(354991.5060, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__maximumZeroFuelMass = Amount.valueOf(207581.9860, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__maximumLandingMass = Amount.valueOf(319517.5554, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__operatingEmptyMass = Amount.valueOf(153131.9860, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0.005*(__maximumTakeOffMass.getEstimatedValue()), SI.KILOGRAM);
				__referenceRange = Amount.valueOf(9800., SI.KILOMETER);
				break;
				
			case AGILE_DC1:
				__maximumTakeOffMass = Amount.valueOf(36336, SI.KILOGRAM); // ADAS project
				__maximumZeroFuelMass = Amount.valueOf(29716, SI.KILOGRAM); // 
				__maximumLandingMass = Amount.valueOf(32702.4, SI.KILOGRAM);
				__operatingEmptyMass = Amount.valueOf(20529, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0., SI.KILOGRAM);
				__referenceRange = Amount.valueOf(3500., SI.KILOMETER);
				break;
			}
		}
		
		public ACWeightsManager build() {
			return new ACWeightsManager(this);
		}
	}
	
	private ACWeightsManager(ACWeightsManagerBuilder builder) {
		
		this._id = builder.__id;
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._maximumZeroFuelMass = builder.__maximumZeroFuelMass;
		this._maximumLandingMass = builder.__maximumLandingMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._trappedFuelOilMass = builder.__trappedFuelOilMass;
		this._referenceRange = builder.__referenceRange;
		
		this._maximumTakeOffMassList = builder.__maximumTakeOffMassList;
		this._massStructureList = builder.__massStructureList;
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	@SuppressWarnings("unchecked")
	public static ACWeightsManager importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading weights analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// MAXIMUM TAKE-OFF MASS
		Amount<Mass> maximumTakeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//weights/maximum_take_off_mass");
		if(maximumTakeOffMassProperty != null)
			maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_take_off_mass");
		else {
			System.err.println("MAXIMUM TAKE-OFF MASS REQUIRED !! \n ... returning ");
			return null; 
		}
			
		//---------------------------------------------------------------
		// MAXIMUM LANDING MASS
		Amount<Mass> maximumLandingMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumLandingMassProperty = reader.getXMLPropertyByPath("//weights/maximum_landing_mass");
		if(maximumLandingMassProperty != null)
			maximumLandingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_landing_mass");
		else {
			System.err.println("MAXIMUM LANDING MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// MAXIMUM ZERO FUEL MASS
		Amount<Mass> maximumZeroFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumZeroFuelMassProperty = reader.getXMLPropertyByPath("//weights/maximum_zero_fuel_mass");
		if(maximumZeroFuelMassProperty != null)
			maximumZeroFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/maximum_zero_fuel_mass");
		else {
			System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// OPERATING EMPTY MASS
		Amount<Mass> operatingEmptyMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//weights/operating_empty_mass");
		if(operatingEmptyMassProperty != null)
			operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/operating_empty_mass");
		else {
			System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// TRAPPED FUEL OIL MASS
		Amount<Mass> trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String trappedFuelOilMassProperty = reader.getXMLPropertyByPath("//weights/trapped_fuel_oil_mass");
		if(trappedFuelOilMassProperty != null)
			trappedFuelOilMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/trapped_fuel_oil_mass");
		else {
			System.err.println("TRAPPED FUEL OIL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// REFERENCE RANGE
		Amount<Length> referenceRange = Amount.valueOf(0.0, SI.KILOMETER);
		String referenceRangeProperty = reader.getXMLPropertyByPath("//weights/reference_range");
		if(referenceRangeProperty != null)
			referenceRange = (Amount<Length>) reader.getXMLAmountLengthByPath("//weights/reference_range").to(SI.KILOMETER);
		else {
			System.err.println("REFERENCE RANGE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		ACWeightsManager theWeigths = new ACWeightsManagerBuilder(id)
				.maximumTakeOffMass(maximumTakeOffMass)
				.maximumLandingMass(maximumLandingMass)
				.maximumZeroFuelMass(maximumZeroFuelMass)
				.operatingEmptyMass(operatingEmptyMass)
				.trappedFuelOilMass(trappedFuelOilMass)
				.referenceRange(referenceRange)
				.build()
				;
		
		return theWeigths;

	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tWeights Analysis\n")
				.append("\t-------------------------------------\n")
				.append("\tReference Range: " + _referenceRange + "\n")
				.append("\tMaterial Density (Alluminuim): " + _materialDensity + "\n")
				.append("\tPax Single Mass: " + _paxSingleMass + "\n")
				.append("\t-------------------------------------\n")
				.append("\tMaximum Take-Off Mass: " + _maximumTakeOffMass + "\n")
				.append("\tMaximum Take-Off Weight: " + _maximumTakeOffWeight + "\n")
				.append("\t·····································\n")
				.append("\tTake-Off Mass: " + _takeOffMass + "\n")
				.append("\tTake-Off Weight: " + _takeOffWeight + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Landing Mass: " + _maximumLandingMass + "\n")
				.append("\tMaximum Landing Weight: " + _maximumLandingWeight + "\n")
				.append("\t·····································\n")
				.append("\tMaximum Passenger Mass: " + _paxMassMax + "\n")
				.append("\tMaximum Passenger Weight: " + _paxMassMax.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")			
				.append("\t·····································\n")
				.append("\tPassenger Mass: " + _paxMass + "\n")
				.append("\tPassenger Weight: " + _paxMass.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")			
				.append("\t·····································\n")
				.append("\tCrew Mass: " + _crewMass + "\n")
				.append("\tCrew Weight: " + _crewMass.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")			
				.append("\t·····································\n")
				.append("\tMaximum Zero Fuel Mass: " + _maximumZeroFuelMass + "\n")
				.append("\tMaximum Zero Fuel Weight: " + _maximumZeroFuelWeight + "\n")			
				.append("\t·····································\n")
				.append("\tZero Fuel Mass: " + _zeroFuelMass + "\n")
				.append("\tZero Fuel Weight: " + _zeroFuelWeight + "\n")
				.append("\t·····································\n")
				.append("\tOperating Empty Mass: " + _operatingEmptyMass + "\n")
				.append("\tOperating Empty Weight: " + _operatingEmptyWeight + "\n")
				.append("\t·····································\n")
				.append("\tEmpty Mass: " + _emptyMass + "\n")
				.append("\tEmpty Weight: " + _emptyWeight + "\n")
				.append("\t·····································\n")
				.append("\tManufacturer Empty Mass: " + _manufacturerEmptyMass + "\n")
				.append("\tManufacturer Empty Weight: " + _manufacturerEmptyWeight + "\n")
				.append("\t·····································\n")
				.append("\tStructural Mass: " + _structuralMass + "\n")
				.append("\tStructural Mass: " + _structuralMass.times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")
				.append("\t·····································\n")
				.append("\tOperating Item Mass: " + _operatingItemMass + "\n")
				.append("\tOperating Item Weight: " + _operatingItemWeight + "\n")
				.append("\t·····································\n")
				.append("\tTrapped Fuel Oil Mass: " + _trappedFuelOilMass + "\n")
				.append("\tTrapped Fuel Oil Weight: " + _trappedFuelOilWeight + "\n")
				.append("\t·····································\n")
				.append("\tOperating Empty Mass: " + _operatingEmptyMass + "\n")
				.append("\tOperating Empty Weight: " + _operatingEmptyWeight + "\n")
				.append("\t·····································\n")
				;
				
		return sb.toString();
		
	}
	
	public void calculateDependentVariables(Aircraft aircraft) {

		// Passengers and crew mass
		// 76.5 kg for each crew member + baggage
		_paxMass = _paxSingleMass.times(aircraft.getCabinConfiguration().getNPax());
		_crewMass = Amount.valueOf(aircraft.getCabinConfiguration().getNCrew() * 76.5145485, SI.KILOGRAM); 

		// Passengers and crew mass
		_paxMassMax = _paxSingleMass.times(aircraft.getCabinConfiguration().getMaxPax());

		// Operating items mass
		if (_referenceRange.getEstimatedValue() < 2000) { 
			_operatingItemMass = Amount.valueOf(8.617*aircraft.getCabinConfiguration().getNPax(), SI.KILOGRAM);
			_operatingItemWeight = _operatingItemMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		} else {
			_operatingItemMass = Amount.valueOf(14.97*aircraft.getCabinConfiguration().getNPax(), SI.KILOGRAM);
			_operatingItemWeight = _operatingItemMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		}

		_emptyMass =_operatingEmptyMass.minus(_crewMass).minus(_trappedFuelOilMass);
		_emptyWeight = _emptyMass.times(AtmosphereCalc.g0).to(SI.NEWTON);

		_maximumTakeOffWeight = _maximumTakeOffMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_maximumZeroFuelWeight = _maximumZeroFuelMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_operatingEmptyWeight = _operatingEmptyMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_trappedFuelOilWeight = _trappedFuelOilMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		_maximumLandingWeight = _maximumLandingMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		
	}

	/** 
	 * Calculate mass of selected configuration. When comparing some/all available methods 
	 * for the selected component the iterative procedure is done using the first selected method.
	 * 
	 * @param aircraft
	 * @param methodsMap
	 */
	public void calculateAllMasses(Aircraft aircraft, 
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		System.out.println("\n-----------------------------------------------");
		System.out.println("----- WEIGHT ESTIMATION PROCEDURE STARTED -----");
		System.out.println("-----------------------------------------------\n");
		calculateFirstGuessMTOM(aircraft);

		aircraft.getFuelTank().calculateFuelMass();

		int i=0;
		_maximumTakeOffMassList.add(Amount.valueOf(0.0, SI.KILOGRAM));

		Amount<Mass> sum = Amount.valueOf(0., SI.KILOGRAM);

		// Evaluate MTOM 5 times and then take the mean value to avoid
		// an infinite loop due to MTOM estimate oscillation 
		while (i < 5) {

			_maximumTakeOffMassList.add(_maximumTakeOffMass);

			aircraft.getTheWeights().calculateDependentVariables(aircraft);

			//////////////////////////////////////////////////////////////////
			// Evaluate weights with more than one method for each component
			//////////////////////////////////////////////////////////////////

			// --- STRUCTURE MASS-----------------------------------

			calculateStructuralMass(aircraft, methodsMap);

			// --- END OF STRUCTURE MASS-----------------------------------

			aircraft.getPowerPlant().calculateMass();

			// --- END OF POWER PLANT MASS-----------------------------------

			calculateManufacturerEmptyMass(aircraft);

			// --- END OF MANUFACTURER EMPTY MASS-----------------------------------

			aircraft.getTheWeights().setOperatingEmptyMass(
					aircraft.getTheWeights().getManufacturerEmptyMass().plus(
							aircraft.getTheWeights().getOperatingItemMass()).plus(
									aircraft.getTheWeights().getCrewMass()));

			// --- END OF OPERATING EMPTY MASS-----------------------------------

			// Zero fuel mass
			aircraft.getTheWeights().setZeroFuelMass(
					aircraft.getTheWeights().getOperatingEmptyMass().plus(
							_paxMass));

			aircraft.getTheWeights().setZeroFuelWeight(
					aircraft.getTheWeights().getZeroFuelMass().times(
							AtmosphereCalc.g0).to(SI.NEWTON));
			
			// Maximum zero fuel mass
			aircraft.getTheWeights().setMaximumZeroFuelMass(
					aircraft.getTheWeights().getOperatingEmptyMass().plus(
							_paxMassMax));

			// --- END ZERO FUEL MASS-----------------------------------

			// Take-off mass
			aircraft.getTheWeights().setTakeOffMass(
					aircraft.getTheWeights().getZeroFuelMass().plus(
							aircraft.getFuelTank().getFuelMass()));

			aircraft.getTheWeights().setTakeOffWeight(
					aircraft.getTheWeights().getTakeOffMass().times(
							AtmosphereCalc.g0).to(SI.NEWTON));
			
			// Maximum take-off mass
			aircraft.getTheWeights().setMaximumTakeOffMass(
					aircraft.getTheWeights().getMaximumZeroFuelMass().plus(
							aircraft.getFuelTank().getFuelMass()));

			// Maximum landing mass
			aircraft.getTheWeights().setMaximumLandingMass(_maximumTakeOffMass.times(0.9));

			System.out.println("Iteration " + (i+1) + 
					", Structure mass: " + aircraft.getTheWeights().getStructuralMass() + 
					" , Maximum Take-Off Mass: " + _maximumTakeOffMass);

			sum = sum.plus(_maximumTakeOffMass);
			i++;
			_maximumTakeOffMass = sum.divide(i);

			aircraft.getTheWeights().calculateDependentVariables(aircraft);
		}

		_massStructureList.add(aircraft.getFuselage().getMassEstimated());
		_massStructureList.add(aircraft.getWing().getMassEstimated());
		_massStructureList.add(aircraft.getHTail().getMassEstimated());
		_massStructureList.add(aircraft.getVTail().getMassEstimated());
		_massStructureList.addAll(aircraft.getNacelles().getMassList());
		_massStructureList.add(aircraft.getLandingGears().getMassEstimated());

		System.out.println("\n-----------------------------------------------");
		System.out.println("--- WEIGHT ESTIMATION PROCEDURE COMPLETED -----");
		System.out.println("-----------------------------------------------\n");

	}

	public void calculateStructuralMass(
			Aircraft aircraft, 
			Map <ComponentEnum, List<MethodEnum>> methodsMap) {

		aircraft.getFuselage().calculateMass(aircraft);

		aircraft.getWing().calculateMass(aircraft);
		aircraft.getHTail().calculateMass(aircraft);
		aircraft.getVTail().calculateMass(aircraft);

		aircraft.getNacelles().calculateMass(aircraft);

		aircraft.getLandingGears().calculateMass(aircraft);

		aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);

		aircraft.getTheWeights().setStructuralMass(
				aircraft.getFuselage().getMassEstimated().plus(
						aircraft.getWing().getMassEstimated()).plus(
								aircraft.getHTail().getMassEstimated()).plus(
										aircraft.getVTail().getMassEstimated()).plus(
												aircraft.getNacelles().getTotalMass()).plus(
														aircraft.getLandingGears().getMassEstimated()));

	}

	public void calculateManufacturerEmptyMass(Aircraft aircraft) {
		aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		aircraft.getCabinConfiguration().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		aircraft.getTheWeights().setManufacturerEmptyMass(
				aircraft.getPowerPlant().getTotalMass().plus(
						aircraft.getTheWeights().getStructuralMass()).plus(
								aircraft.getSystems().getOverallMass()).plus(
										aircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment()));
	}


	public void calculateFirstGuessMTOM(Aircraft aircraft) {

		aircraft.getFuselage().setMass(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.15));
		aircraft.getWing().setMassReference(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.1));
		aircraft.getHTail().setMassReference(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.015));
		aircraft.getVTail().setMassReference(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.015));
		aircraft.getPowerPlant().setTotalMass(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.05));
		aircraft.getNacelles().setTotalMass(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.015));
		aircraft.getFuelTank().setFuelMass(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.015));
		aircraft.getLandingGears().setMass(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.04));
		aircraft.getSystems().setOverallMass(aircraft.getTheWeights().getMaximumZeroFuelMass().times(.04));

		aircraft.getTheWeights().setStructuralMass(
				aircraft.getFuselage().getMass().plus(
						aircraft.getWing().getMassReference()).plus(
								aircraft.getHTail().getMassReference()).plus(
										aircraft.getVTail().getMassReference()).plus(
												aircraft.getNacelles().getTotalMass()).plus(
														aircraft.getLandingGears().getMass()));

		System.out.println("First guess value:" + aircraft.getTheWeights().getStructuralMass().getEstimatedValue());
	}

	public Amount<Mass> getMaximumTakeOffMass() {
		return _maximumTakeOffMass;
	}

	public void setMaximumTakeOffMass(Amount<Mass> _MTOM) {
		this._maximumTakeOffMass = _MTOM;
	}

	public Amount<Mass> getMaximumZeroFuelMass() {
		return _maximumZeroFuelMass;
	}

	public Amount<Force> getMaximumTakeOffWeight() {
		return _maximumTakeOffWeight;
	}

	public Amount<Force> getMaximumZeroFuelWeight() {
		return _maximumZeroFuelWeight;
	}

	public Amount<Mass> getPaxMass() {
		return _paxMass;
	}

	public Amount<Mass> getCrewMass() {
		return _crewMass;
	}

	public Amount<Mass> getEmptyMass() {
		return _emptyMass;
	}

	public void setEmptyMass(Amount<Mass> _emptyMass) {
		this._emptyMass = _emptyMass;
	}

	public Amount<Force> get_emptyWeight() {
		return _emptyWeight;
	}

	public void setEmptyWeight(Amount<Force> _emptyWeight) {
		this._emptyWeight = _emptyWeight;
	}

	public Amount<Mass> getStructuralMass() {
		return _structuralMass;
	}

	public void setStructuralMass(Amount<Mass> _structureMass) {
		this._structuralMass = _structureMass;
	}

	public Amount<Force> getStructuralWeight() {
		return _structuralMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
	}

	public Amount<VolumetricDensity> getMaterialDensity() {
		return _materialDensity;
	}

	public void setMaterialDensity(Amount<VolumetricDensity> _materialDensity) {
		ACWeightsManager._materialDensity = _materialDensity;
	}

	public Amount<Force> getMaximumLandingWeight() {
		return _maximumLandingWeight;
	}

	public void setMaximumLandingWeight(Amount<Force> _MLW) {
		this._maximumLandingWeight = _MLW;
	}

	public Amount<Mass> getMaximumLangingMass() {
		return _maximumLandingMass;
	}

	public void setMaximumLandingMass(Amount<Mass> _MLM) {
		this._maximumLandingMass = _MLM;
	}

	public Amount<Mass> getOperatingItemMass() {
		return _operatingItemMass;
	}

	public void setOperatingItemMass(Amount<Mass> _OIM) {
		this._operatingItemMass = _OIM;
	}

	public Amount<Force> getOperatingItemWeight() {
		return _operatingItemWeight;
	}

	public void setOperatingItemWeight(Amount<Force> _operatingItemWeight) {
		this._operatingItemWeight = _operatingItemWeight;
	}

	public Amount<Mass> getManufacturerEmptyMass() {
		return _manufacturerEmptyMass;
	}

	public void setManufacturerEmptyMass(Amount<Mass> _manufacturerEmptyMass) {
		this._manufacturerEmptyMass = _manufacturerEmptyMass;
	}

	public Amount<Force> getManufacturerEmptyWeight() {
		return _manufacturerEmptyWeight;
	}

	public void setManufacturerEmptyWeight(Amount<Force> _manufacturerEmptyWeight) {
		this._manufacturerEmptyWeight = _manufacturerEmptyWeight;
	}

	public Amount<Mass> getOperatingEmptyMass() {
		return _operatingEmptyMass;
	}

	public void setOperatingEmptyMass(Amount<Mass> _OEM) {
		this._operatingEmptyMass = _OEM;
	}

	public Amount<Force> getOperatingEmptyWeight() {
		return _operatingEmptyWeight;
	}

	public void setOperatingEmptyWeight(Amount<Force> _operatingEmptyWeight) {
		this._operatingEmptyWeight = _operatingEmptyWeight;
	}

	public Amount<Mass> getTrappedFuelOilMass() {
		return _trappedFuelOilMass;
	}

	public void setTrappedFuelOilMass(Amount<Mass> _trappedFuelOilMass) {
		this._trappedFuelOilMass = _trappedFuelOilMass;
	}

	public Amount<Force> getTrappedFuelOilWeight() {
		return _trappedFuelOilWeight;
	}

	public void setTrappedFuelOilWeight(Amount<Force> _trappedFuelOilWeight) {
		this._trappedFuelOilWeight = _trappedFuelOilWeight;
	}

	public Amount<Mass> getZeroFuelMass() {
		return _zeroFuelMass;
	}

	public void setZeroFuelMass(Amount<Mass> _ZFM) {
		this._zeroFuelMass = _ZFM;
	}

	public Amount<Force> getZeroFuelWeight() {
		return _zeroFuelWeight;
	}

	public void setZeroFuelWeight(Amount<Force> _zeroFuelWeight) {
		this._zeroFuelWeight = _zeroFuelWeight;
	}

	public Amount<Mass> getPaxMassMax() {
		return _paxMassMax;
	}

	public void setMaximumZeroFuelMass(Amount<Mass> _MZFM) {
		this._maximumZeroFuelMass = _MZFM;
	}

	public Amount<Mass> getTakeOffMass() {
		return _takeOffMass;
	}

	public void setTakeOffMass(Amount<Mass> _TOM) {
		this._takeOffMass = _TOM;
	}

	public Amount<Force> getTakeOffWeight() {
		return _takeOffWeight;
	}

	public void setTakeOffWeight(Amount<Force> _takeOffWeight) {
		this._takeOffWeight = _takeOffWeight;
	}

	public Amount<Mass> getPaxSingleMass() {
		return _paxSingleMass;
	}

	public void setPaxSingleMass(Amount<Mass> _paxSingleMass) {
		ACWeightsManager._paxSingleMass = _paxSingleMass;
	}

	public List<Amount<Mass>> getMassStructureList() {
		return _massStructureList;
	}

	public void setMassStructureList(List<Amount<Mass>> _massStructureList) {
		this._massStructureList = _massStructureList;
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public static AnalysisTypeEnum getType() {
		return _type;
	}

	public Amount<Length> getRange() {
		return _referenceRange;
	}

	public void setRange(Amount<Length> _range) {
		this._referenceRange = _range;
	}
}


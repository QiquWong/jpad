package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Force;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.VolumetricDensity;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import configuration.MyConfiguration;
import configuration.enumerations.AircraftEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;

/**
 * Manage components weight calculations
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 */
public class ACWeightsManager implements IACWeightsManager {

	private String _id;
	private static Aircraft _theAircraft;
	
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
	private Amount<Mass> _fuselageReferenceMass;
	private Amount<Mass> _wingReferenceMass;
	private Amount<Mass> _horizontalTailReferenceMass;
	private Amount<Mass> _verticalTailReferenceMass;
	private Amount<Mass> _canardReferenceMass;
	private Amount<Mass> _nacelleReferenceMass;
	private Amount<Mass> _engineReferenceMass;
	private Amount<Mass> _fuelTankReferenceMass;
	private Amount<Mass> _landingGearsReferenceMass;
	private Amount<Mass> _systemsReferenceMass;
	
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
		private Aircraft __theAircraft;
		
		// optional parameters ... defaults
		// ...
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __maximumZeroFuelMass;
		private Amount<Mass> __maximumLandingMass;
		private Amount<Mass> __operatingEmptyMass;
		private Amount<Mass> __trappedFuelOilMass;
		private Amount<Mass> __fuselageReferenceMass;
		private Amount<Mass> __wingReferenceMass;
		private Amount<Mass> __horizontalTailReferenceMass;
		private Amount<Mass> __verticalTailReferenceMass;
		private Amount<Mass> __canardReferenceMass;
		private Amount<Mass> __nacelleReferenceMass;
		private Amount<Mass> __engineReferenceMass;
		private Amount<Mass> __fuelTankReferenceMass;
		private Amount<Mass> __landingGearsReferenceMass;
		private Amount<Mass> __systemsReferenceMass;
		
		private List<Amount<Mass>> __maximumTakeOffMassList = new ArrayList<Amount<Mass>>();
		private List<Amount<Mass>> __massStructureList = new ArrayList<Amount<Mass>>();
		
		public ACWeightsManagerBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public ACWeightsManagerBuilder aircraft(Aircraft theAircraft) {
			this.__theAircraft = theAircraft;
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
		
		public ACWeightsManagerBuilder fuselageReferenceMass(Amount<Mass> fuselageReferenceMass) {
			this.__fuselageReferenceMass = fuselageReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder wingReferenceMass(Amount<Mass> wingReferenceMass) {
			this.__wingReferenceMass = wingReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder horizontalTailReferenceMass(Amount<Mass> horizontalTailReferenceMass) {
			this.__horizontalTailReferenceMass = horizontalTailReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder verticalTailReferenceMass(Amount<Mass> verticalTailReferenceMass) {
			this.__verticalTailReferenceMass = verticalTailReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder canardReferenceMass(Amount<Mass> canardReferenceMass) {
			this.__canardReferenceMass = canardReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder nacelleReferenceMass(Amount<Mass> nacelleReferenceMass) {
			this.__nacelleReferenceMass = nacelleReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder engineReferenceMass(Amount<Mass> engineReferenceMass) {
			this.__engineReferenceMass = engineReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder fuelTankReferenceMass(Amount<Mass> fuelTankReferenceMass) {
			this.__fuelTankReferenceMass = fuelTankReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder landingGearsReferenceMass(Amount<Mass> landingGearsReferenceMass) {
			this.__landingGearsReferenceMass = landingGearsReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder systemsReferenceMass(Amount<Mass> systemsReferenceMass) {
			this.__systemsReferenceMass = systemsReferenceMass;
			return this;
		}
		
		public ACWeightsManagerBuilder (String id, Aircraft theAircraft) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			this.initializeDefaultData(AircraftEnum.ATR72);
		}
		
		public ACWeightsManagerBuilder (String id, Aircraft theAircraft, AircraftEnum aircraftName) {
			this.__id = id;
			this.__theAircraft = theAircraft;
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
				break;
				
			case B747_100B:
				__maximumTakeOffMass = Amount.valueOf(354991.5060, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__maximumZeroFuelMass = Amount.valueOf(207581.9860, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__maximumLandingMass = Amount.valueOf(319517.5554, SI.KILOGRAM); // B747-100B MTOM, see REPORT_B747_100B in database
				__operatingEmptyMass = Amount.valueOf(153131.9860, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0.005*(__maximumTakeOffMass.getEstimatedValue()), SI.KILOGRAM);
				break;
				
			case AGILE_DC1:
				__maximumTakeOffMass = Amount.valueOf(36336, SI.KILOGRAM); // ADAS project
				__maximumZeroFuelMass = Amount.valueOf(29716, SI.KILOGRAM); // 
				__maximumLandingMass = Amount.valueOf(32702.4, SI.KILOGRAM);
				__operatingEmptyMass = Amount.valueOf(20529, SI.KILOGRAM);
				__trappedFuelOilMass = Amount.valueOf(0., SI.KILOGRAM);
				break;
			}
		}
		
		public ACWeightsManager build() {
			return new ACWeightsManager(this);
		}
	}
	
	private ACWeightsManager(ACWeightsManagerBuilder builder) {
		
		this._id = builder.__id;
		ACWeightsManager._theAircraft = builder.__theAircraft;
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._maximumZeroFuelMass = builder.__maximumZeroFuelMass;
		this._maximumLandingMass = builder.__maximumLandingMass;
		this._operatingEmptyMass = builder.__operatingEmptyMass;
		this._trappedFuelOilMass = builder.__trappedFuelOilMass;
		this._referenceRange = _theAircraft.getTheAnalysisManager().getReferenceRange();
		
		this._fuselageReferenceMass = builder.__fuselageReferenceMass;
		this._wingReferenceMass = builder.__wingReferenceMass;
		this._horizontalTailReferenceMass = builder.__horizontalTailReferenceMass;
		this._verticalTailReferenceMass = builder.__verticalTailReferenceMass;
		this._canardReferenceMass = builder.__canardReferenceMass;
		this._engineReferenceMass = builder.__engineReferenceMass;
		this._nacelleReferenceMass = builder.__nacelleReferenceMass;
		this._fuelTankReferenceMass = builder.__fuelTankReferenceMass;
		this._landingGearsReferenceMass = builder.__landingGearsReferenceMass;
		this._systemsReferenceMass = builder.__systemsReferenceMass;
		
		this._maximumTakeOffMassList = builder.__maximumTakeOffMassList;
		this._massStructureList = builder.__massStructureList;
		
		// ESTIMATION OF THE REFERENCE MASS OF EACH COMPONENT (if not assigned)
		if(_theAircraft.getFuselage() != null) 
				if(this._fuselageReferenceMass == null)
					this._fuselageReferenceMass = _maximumZeroFuelMass.times(.15);
		if(_theAircraft.getWing() != null)
			if(this._wingReferenceMass == null)
				this._wingReferenceMass = _maximumZeroFuelMass.times(.1);
		if(_theAircraft.getHTail() != null) 
			if(this._horizontalTailReferenceMass == null)
				this._horizontalTailReferenceMass = _maximumZeroFuelMass.times(.015);
		if(_theAircraft.getVTail() != null)
			if(this._verticalTailReferenceMass == null)
				this._verticalTailReferenceMass = _maximumZeroFuelMass.times(.015);
		if(_theAircraft.getCanard() != null)
			if(this._canardReferenceMass == null)
				this._canardReferenceMass = _maximumZeroFuelMass.times(.015);
		if(_theAircraft.getPowerPlant() != null)
			if(this._engineReferenceMass == null)
				this._engineReferenceMass = _maximumZeroFuelMass.times(.05);
		if(_theAircraft.getNacelles() != null)
			if(this._nacelleReferenceMass == null)
				this._nacelleReferenceMass = _maximumZeroFuelMass.times(.015);
		if(_theAircraft.getFuelTank() != null)
			if(this._fuelTankReferenceMass == null)
				this._fuelTankReferenceMass = _maximumZeroFuelMass.times(.015);
		if(_theAircraft.getLandingGears() != null) 
			if(this._landingGearsReferenceMass == null)
				this._landingGearsReferenceMass = _maximumZeroFuelMass.times(.04);
		if(_theAircraft.getSystems() != null)
			if(this._systemsReferenceMass == null)
				_systemsReferenceMass = _maximumZeroFuelMass.times(.04);
		
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	@SuppressWarnings("unchecked")
	public static ACWeightsManager importFromXML (String pathToXML, Aircraft theAircraft) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading weights analysis data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// MAXIMUM TAKE-OFF MASS
		Amount<Mass> maximumTakeOffMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumTakeOffMassProperty = reader.getXMLPropertyByPath("//weights/global_data/maximum_take_off_mass");
		if(maximumTakeOffMassProperty != null)
			maximumTakeOffMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/maximum_take_off_mass");
		else {
			System.err.println("MAXIMUM TAKE-OFF MASS REQUIRED !! \n ... returning ");
			return null; 
		}
			
		//---------------------------------------------------------------
		// MAXIMUM LANDING MASS
		Amount<Mass> maximumLandingMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumLandingMassProperty = reader.getXMLPropertyByPath("//weights/global_data/maximum_landing_mass");
		if(maximumLandingMassProperty != null)
			maximumLandingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/maximum_landing_mass");
		else {
			System.err.println("MAXIMUM LANDING MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// MAXIMUM ZERO FUEL MASS
		Amount<Mass> maximumZeroFuelMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String maximumZeroFuelMassProperty = reader.getXMLPropertyByPath("//weights/global_data/maximum_zero_fuel_mass");
		if(maximumZeroFuelMassProperty != null)
			maximumZeroFuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/maximum_zero_fuel_mass");
		else {
			System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// OPERATING EMPTY MASS
		Amount<Mass> operatingEmptyMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//weights/global_data/operating_empty_mass");
		if(operatingEmptyMassProperty != null)
			operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/operating_empty_mass");
		else {
			System.err.println("MAXIMUM ZERO FUEL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// TRAPPED FUEL OIL MASS
		Amount<Mass> trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String trappedFuelOilMassProperty = reader.getXMLPropertyByPath("//weights/global_data/trapped_fuel_oil_mass");
		if(trappedFuelOilMassProperty != null)
			trappedFuelOilMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/trapped_fuel_oil_mass");
		else {
			System.err.println("TRAPPED FUEL OIL MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// FUSELAGE REFERENCE MASS
		Amount<Mass> fuselageReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String fuselageReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/fuselage_reference_mass");
		if(fuselageReferenceMassProperty != null)
			fuselageReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/fuselage_reference_mass");
		else {
			fuselageReferenceMass = null;
		}

		//---------------------------------------------------------------
		// WING REFERENCE MASS
		Amount<Mass> wingReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String wingReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/wing_reference_mass");
		if(wingReferenceMassProperty != null)
			wingReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/wing_reference_mass");
		else {
			wingReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// HORIZONTAL TAIL REFERENCE MASS
		Amount<Mass> horizontalTailReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String horizontalTailReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/horizontal_tail_reference_mass");
		if(horizontalTailReferenceMassProperty != null)
			horizontalTailReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/horizontal_tail_reference_mass");
		else {
			horizontalTailReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// VERTICAL TAIL REFERENCE MASS
		Amount<Mass> verticalTailReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String verticalTailReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/vertical_tail_reference_mass");
		if(verticalTailReferenceMassProperty != null)
			verticalTailReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/vertical_tail_reference_mass");
		else {
			verticalTailReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// CANARD REFERENCE MASS
		Amount<Mass> canardReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String canardReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/canard_reference_mass");
		if(canardReferenceMassProperty != null)
			canardReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/canard_reference_mass");
		else {
			canardReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// ENGINE REFERENCE MASS
		Amount<Mass> engineReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String engineReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/power_plant_reference_mass");
		if(engineReferenceMassProperty != null)
			engineReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/power_plant_reference_mass");
		else {
			engineReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// NACELLE REFERENCE MASS
		Amount<Mass> nacelleReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String nacelleReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/nacelles_reference_mass");
		if(nacelleReferenceMassProperty != null)
			nacelleReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/nacelles_reference_mass");
		else {
			nacelleReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// FUEL TANK REFERENCE MASS
		Amount<Mass> fuelTankReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String fuelTankReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/fuel_tank_reference_mass");
		if(fuelTankReferenceMassProperty != null)
			fuelTankReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/fuel_tank_reference_mass");
		else {
			fuelTankReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// LANDING GEARS REFERENCE MASS
		Amount<Mass> landingGearsReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String landingGearsReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/landing_gears_reference_mass");
		if(landingGearsReferenceMassProperty != null)
			landingGearsReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/landing_gears_reference_mass");
		else {
			landingGearsReferenceMass = null;
		}
		
		//---------------------------------------------------------------
		// SYSTEMS REFERENCE MASS
		Amount<Mass> systemsReferenceMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String systemsReferenceMassProperty = reader.getXMLPropertyByPath("//weights/component_data/systems_reference_mass");
		if(systemsReferenceMassProperty != null)
			systemsReferenceMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/component_data/systems_reference_mass");
		else {
			systemsReferenceMass = null;
		}
		
		ACWeightsManager theWeigths = new ACWeightsManagerBuilder(id, theAircraft)
				.maximumTakeOffMass(maximumTakeOffMass)
				.maximumLandingMass(maximumLandingMass)
				.maximumZeroFuelMass(maximumZeroFuelMass)
				.operatingEmptyMass(operatingEmptyMass)
				.trappedFuelOilMass(trappedFuelOilMass)
				.fuselageReferenceMass(fuselageReferenceMass)
				.wingReferenceMass(wingReferenceMass)
				.horizontalTailReferenceMass(horizontalTailReferenceMass)
				.verticalTailReferenceMass(verticalTailReferenceMass)
				.canardReferenceMass(canardReferenceMass)
				.engineReferenceMass(engineReferenceMass)
				.nacelleReferenceMass(nacelleReferenceMass)
				.fuelTankReferenceMass(fuelTankReferenceMass)
				.landingGearsReferenceMass(landingGearsReferenceMass)
				.systemsReferenceMass(systemsReferenceMass)
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
				.append("\tFuel Mass: " + _theAircraft.getFuelTank().getFuelMass() + "\n")
				.append("\tFuel Weight: " + _theAircraft.getFuelTank().getFuelWeight() + "\n")			
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
				;
				
		return sb.toString();
		
	}
	
	public void toXLSFile(String filenameWithPathAndExt) throws InvalidFormatException, IOException {
		
		Workbook wb;
		File outputFile = new File(filenameWithPathAndExt + ".xlsx");
		if (outputFile.exists()) { 
			outputFile.delete();		
			System.out.println("Deleting the old .xls file ...");
		} 
		
		if (outputFile.getName().endsWith(".xls")) {
			wb = new HSSFWorkbook();
		}
		else if (outputFile.getName().endsWith(".xlsx")) {
			wb = new XSSFWorkbook();
		}
		else {
			throw new IllegalArgumentException("I don't know how to create that kind of new file");
		}
		
		//--------------------------------------------------------------------------------
		// GLOBAL ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		Sheet sheet = wb.createSheet("GLOBAL RESULTS");
		List<Object[]> dataListGlobal = new ArrayList<>();
		dataListGlobal.add(new Object[] {"Description","Unit","Value"});
		dataListGlobal.add(new Object[] {"Reference Range","nmi",_referenceRange.doubleValue(NonSI.NAUTICAL_MILE)});
		dataListGlobal.add(new Object[] {"Material density","kg/m³",_materialDensity.getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Single passenger Mass","kg",_paxSingleMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Mass","kg",_maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Take-Off Mass","kg",_takeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Landing Mass","kg",_maximumLandingMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Mass","kg",_paxMassMax.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Fuel Mass","kg",_theAircraft.getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Crew Mass","kg",_crewMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Mass","kg",_maximumZeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Zero Fuel Mass","kg",_zeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Empty Mass","kg",_operatingEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Empty Mass","kg",_emptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Mass","kg",_manufacturerEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Item Mass","kg",_operatingItemMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Trapped Fuel Oil Mass","kg",_trappedFuelOilMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Weight","N",_maximumTakeOffWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Take-Off Weight","N",_takeOffWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Maximum Landing Weight","N",_maximumLandingWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Weight","N",(_paxMassMax.times(AtmosphereCalc.g0)).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Fuel Weight","N",_theAircraft.getFuelTank().getFuelWeight().doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Crew Weight","N",_crewMass.times(AtmosphereCalc.g0).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Weight","N",_maximumZeroFuelWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Zero Fuel Weight","N",_zeroFuelWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Operating Empty Weight","N",_operatingEmptyWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Empty Weight","N",_emptyWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Weight","N",_manufacturerEmptyMass.times(AtmosphereCalc.g0).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Operating Item Weight","N",_operatingItemWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Trapped Fuel Oil Weight","N",_trappedFuelOilWeight.doubleValue(SI.NEWTON)});
		
		CellStyle styleHead = wb.createCellStyle();
		styleHead.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
	    styleHead.setFillPattern(CellStyle.SOLID_FOREGROUND);
	    Font font = wb.createFont();
	    font.setFontHeightInPoints((short) 20);
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        styleHead.setFont(font);
        
        Row row = sheet.createRow(0);
		Object[] objArr = dataListGlobal.get(0);
		int cellnum = 0;
		for (Object obj : objArr) {
			Cell cell = row.createCell(cellnum++);
			cell.setCellStyle(styleHead);
			if (obj instanceof Date) {
				cell.setCellValue((Date) obj);
			} else if (obj instanceof Boolean) {
				cell.setCellValue((Boolean) obj);
			} else if (obj instanceof String) {
				cell.setCellValue((String) obj);
			} else if (obj instanceof Double) {
				cell.setCellValue((Double) obj);
			}
			sheet.setDefaultColumnWidth(25);
		}
	
		int rownum = 1;
		for (int i = 1; i < dataListGlobal.size(); i++) {
			objArr = dataListGlobal.get(i);
			row = sheet.createRow(rownum++);
			cellnum = 0;
			for (Object obj : objArr) {
				Cell cell = row.createCell(cellnum++);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
			}
		}

		//--------------------------------------------------------------------------------
		// FUSELAGE WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getFuselage() != null) {
			Sheet sheetFuselage = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();
			dataListFuselage.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListFuselage.add(new Object[] {"Reference Mass","kg", _fuselageReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListFuselage.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getFuselage().getMassCorrectionFactor()});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexFuselage=0;
			for(MethodEnum methods : _theAircraft.getFuselage().getMassMap().keySet()) {
				if(_theAircraft.getFuselage().getMassMap().get(methods) != null) 
					dataListFuselage.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getFuselage().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getFuselage().getPercentDifference()[indexFuselage]
							}
							);
				indexFuselage++;
			}
			dataListFuselage.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getFuselage().getMassEstimated().getEstimatedValue(),
							_theAircraft.getFuselage().getMassEstimated().
								minus(_theAircraft.getFuselage().getReferenceMass()).
								divide(_theAircraft.getFuselage().getReferenceMass()).
								getEstimatedValue()*100
					});

			Row rowFuselage = sheetFuselage.createRow(0);
			Object[] objArrFuselage = dataListFuselage.get(0);
			int cellnumFuselage = 0;
			for (Object obj : objArrFuselage) {
				Cell cell = rowFuselage.createCell(cellnumFuselage++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetFuselage.setDefaultColumnWidth(25);
			}

			int rownumFuselage = 1;
			for (int j = 1; j < dataListFuselage.size(); j++) {
				objArrFuselage = dataListFuselage.get(j);
				rowFuselage = sheetFuselage.createRow(rownumFuselage++);
				cellnumFuselage = 0;
				for (Object obj : objArrFuselage) {
					Cell cell = rowFuselage.createCell(cellnumFuselage++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		//--------------------------------------------------------------------------------
		// WING WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getWing() != null) {
			Sheet sheetWing = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();
			dataListWing.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListWing.add(new Object[] {"Reference Mass","kg", _wingReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListWing.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getWing().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListWing.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getWing().getMassCorrectionFactor()});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexWing=0;
			for(MethodEnum methods : _theAircraft.getWing().getMassMap().keySet()) {
				if(_theAircraft.getWing().getMassMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getWing().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getWing().getPercentDifference()[indexWing]
							}
							);
				indexWing++;
			}
			dataListWing.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getWing().getMassEstimated().getEstimatedValue(),
							_theAircraft.getWing().getMassEstimated().
								minus(_theAircraft.getWing().getMassReference()).
								divide(_theAircraft.getWing().getMassReference()).
								getEstimatedValue()*100
					});

			Row rowWing = sheetWing.createRow(0);
			Object[] objArrWing = dataListWing.get(0);
			int cellnumWing = 0;
			for (Object obj : objArrWing) {
				Cell cell = rowWing.createCell(cellnumWing++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetWing.setDefaultColumnWidth(25);
			}

			int rownumWing = 1;
			for (int j = 1; j < dataListWing.size(); j++) {
				objArrWing = dataListWing.get(j);
				rowWing = sheetWing.createRow(rownumWing++);
				cellnumWing = 0;
				for (Object obj : objArrWing) {
					Cell cell = rowWing.createCell(cellnumWing++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		//--------------------------------------------------------------------------------
		// HORIZONTAL TAIL WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getHTail() != null) {
			Sheet sheetHTail = wb.createSheet("HORIZONTAL TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();
			dataListHTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListHTail.add(new Object[] {"Reference Mass","kg", _horizontalTailReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListHTail.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getHTail().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListHTail.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getHTail().getMassCorrectionFactor()});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexHTail=0;
			for(MethodEnum methods : _theAircraft.getHTail().getMassMap().keySet()) {
				if(_theAircraft.getHTail().getMassMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getHTail().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getHTail().getPercentDifference()[indexHTail]
							}
							);
				indexHTail++;
			}
			dataListHTail.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getHTail().getMassEstimated().getEstimatedValue(),
							_theAircraft.getHTail().getMassEstimated().
								minus(_theAircraft.getHTail().getMassReference()).
								divide(_theAircraft.getHTail().getMassReference()).
								getEstimatedValue()*100
					});

			Row rowHTail = sheetHTail.createRow(0);
			Object[] objArrHTail = dataListHTail.get(0);
			int cellnumHTail = 0;
			for (Object obj : objArrHTail) {
				Cell cell = rowHTail.createCell(cellnumHTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetHTail.setDefaultColumnWidth(25);
			}

			int rownumHTail = 1;
			for (int j = 1; j < dataListHTail.size(); j++) {
				objArrHTail = dataListHTail.get(j);
				rowHTail = sheetHTail.createRow(rownumHTail++);
				cellnumHTail = 0;
				for (Object obj : objArrHTail) {
					Cell cell = rowHTail.createCell(cellnumHTail++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// VERTICAL TAIL WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getVTail() != null) {
			Sheet sheetVTail = wb.createSheet("VERTICAL TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();
			dataListVTail.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListVTail.add(new Object[] {"Reference Mass","kg", _verticalTailReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListVTail.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getVTail().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListVTail.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getVTail().getMassCorrectionFactor()});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexVTail=0;
			for(MethodEnum methods : _theAircraft.getVTail().getMassMap().keySet()) {
				if(_theAircraft.getVTail().getMassMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getVTail().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getVTail().getPercentDifference()[indexVTail]
							}
							);
				indexVTail++;
			}
			dataListVTail.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getVTail().getMassEstimated().getEstimatedValue(),
							_theAircraft.getVTail().getMassEstimated().
								minus(_theAircraft.getVTail().getMassReference()).
								divide(_theAircraft.getVTail().getMassReference()).
								getEstimatedValue()*100
					});

			Row rowVTail = sheetVTail.createRow(0);
			Object[] objArrVTail = dataListVTail.get(0);
			int cellnumVTail = 0;
			for (Object obj : objArrVTail) {
				Cell cell = rowVTail.createCell(cellnumVTail++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetVTail.setDefaultColumnWidth(25);
			}

			int rownumVTail = 1;
			for (int j = 1; j < dataListVTail.size(); j++) {
				objArrVTail = dataListVTail.get(j);
				rowVTail = sheetVTail.createRow(rownumVTail++);
				cellnumVTail = 0;
				for (Object obj : objArrVTail) {
					Cell cell = rowVTail.createCell(cellnumVTail++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// CANARD WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getCanard() != null) {
			Sheet sheetCanard = wb.createSheet("CANARD");
			List<Object[]> dataListCanard = new ArrayList<>();
			dataListCanard.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListCanard.add(new Object[] {"Reference Mass","kg", _canardReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListCanard.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getCanard().getLiftingSurfaceCreator().getCompositeCorrectioFactor()});
			dataListCanard.add(new Object[] {"Mass Correction Factor"," ",_theAircraft.getCanard().getMassCorrectionFactor()});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexCanard=0;
			for(MethodEnum methods : _theAircraft.getCanard().getMassMap().keySet()) {
				if(_theAircraft.getCanard().getMassMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"Kg",
									_theAircraft.getCanard().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getCanard().getPercentDifference()[indexCanard]
							}
							);
				indexCanard++;
			}
			dataListCanard.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getCanard().getMassEstimated().getEstimatedValue(),
							_theAircraft.getCanard().getMassEstimated().
								minus(_theAircraft.getCanard().getMassReference()).
								divide(_theAircraft.getCanard().getMassReference()).
								getEstimatedValue()*100
					});

			Row rowCanard = sheetCanard.createRow(0);
			Object[] objArrCanard = dataListCanard.get(0);
			int cellnumCanard = 0;
			for (Object obj : objArrCanard) {
				Cell cell = rowCanard.createCell(cellnumCanard++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetCanard.setDefaultColumnWidth(25);
			}

			int rownumCanard = 1;
			for (int j = 1; j < dataListCanard.size(); j++) {
				objArrCanard = dataListCanard.get(j);
				rowCanard = sheetCanard.createRow(rownumCanard++);
				cellnumCanard = 0;
				for (Object obj : objArrCanard) {
					Cell cell = rowCanard.createCell(cellnumCanard++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// NACELLES WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getNacelles() != null) {
			Sheet sheetNacelles = wb.createSheet("NACELLES");
			List<Object[]> dataListNacelles = new ArrayList<>();
			dataListNacelles.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListNacelles.add(new Object[] {"Total Reference Mass","kg", _nacelleReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListNacelles.add(new Object[] {"Total mass estimated","kg",_theAircraft.getNacelles().getTotalMass().getEstimatedValue(),_theAircraft.getNacelles().getPercentTotalDifference()});
			dataListNacelles.add(new Object[] {" "});
			dataListNacelles.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON FOR EACH NACELLE"});
			dataListNacelles.add(new Object[] {" "});
			for(int iNacelle = 0; iNacelle < _theAircraft.getNacelles().getNacellesNumber(); iNacelle++) {
				dataListNacelles.add(new Object[] {"NACELLE " + (iNacelle+1)});
				dataListNacelles.add(new Object[] {"Reference Mass","kg", _nacelleReferenceMass.divide(_theAircraft.getNacelles().getNacellesNumber())});
				int indexNacelles=0;
				for(MethodEnum methods : _theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassMap().keySet()) {
					if(_theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassMap().get(methods) != null) 
						dataListNacelles.add(
								new Object[] {
										methods.toString(),
										"Kg",
										_theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassMap().get(methods).getEstimatedValue(),
										_theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getPercentDifference()[indexNacelles]
								}
								);
					indexNacelles++;
				}
				dataListNacelles.add(new Object[] {"Estimated Mass ","kg", _theAircraft.getNacelles().getNacellesList().get(iNacelle).getWeights().getMassEstimated().getEstimatedValue()});
				dataListNacelles.add(new Object[] {" "});
				}
			
			Row rowNacelles = sheetNacelles.createRow(0);
			Object[] objArrNacelles = dataListNacelles.get(0);
			int cellnumNacelles = 0;
			for (Object obj : objArrNacelles) {
				Cell cell = rowNacelles.createCell(cellnumNacelles++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetNacelles.setDefaultColumnWidth(25);
			}

			int rownumNacelles = 1;
			for (int j = 1; j < dataListNacelles.size(); j++) {
				objArrNacelles = dataListNacelles.get(j);
				rowNacelles = sheetNacelles.createRow(rownumNacelles++);
				cellnumNacelles = 0;
				for (Object obj : objArrNacelles) {
					Cell cell = rowNacelles.createCell(cellnumNacelles++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// POWER PLANT WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getPowerPlant() != null) {
			Sheet sheetPowerPlant = wb.createSheet("POWER PLANT");
			List<Object[]> dataListPowerPlant = new ArrayList<>();
			dataListPowerPlant.add(new Object[] {"Description","Unit","Value"});
			dataListPowerPlant.add(new Object[] {"Total Dry Mass","kg", _theAircraft.getPowerPlant().getDryMassPublicDomainTotal().doubleValue(SI.KILOGRAM)});
			dataListPowerPlant.add(new Object[] {"Total mass estimated","kg",_theAircraft.getPowerPlant().getTotalMass().getEstimatedValue()});
			dataListPowerPlant.add(new Object[] {" "});
			dataListPowerPlant.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON FOR EACH ENGINE"});
			dataListPowerPlant.add(new Object[] {" "});
			for(int iEngine = 0; iEngine < _theAircraft.getPowerPlant().getEngineNumber(); iEngine++) {
				dataListPowerPlant.add(new Object[] {"ENGINE " + (iEngine+1)});
				dataListPowerPlant.add(new Object[] {"Dry Mass","kg", _theAircraft.getPowerPlant().getEngineList().get(iEngine).getDryMassPublicDomain().doubleValue(SI.KILOGRAM)});
				dataListPowerPlant.add(new Object[] {"Total Mass","kg", _theAircraft.getPowerPlant().getEngineList().get(iEngine).getTotalMass().getEstimatedValue()});			
				dataListPowerPlant.add(new Object[] {" "});
				}
			
			Row rowEngines = sheetPowerPlant.createRow(0);
			Object[] objArrEngines = dataListPowerPlant.get(0);
			int cellnumEngines = 0;
			for (Object obj : objArrEngines) {
				Cell cell = rowEngines.createCell(cellnumEngines++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetPowerPlant.setDefaultColumnWidth(25);
			}

			int rownumEngines = 1;
			for (int j = 1; j < dataListPowerPlant.size(); j++) {
				objArrEngines = dataListPowerPlant.get(j);
				rowEngines = sheetPowerPlant.createRow(rownumEngines++);
				cellnumEngines = 0;
				for (Object obj : objArrEngines) {
					Cell cell = rowEngines.createCell(cellnumEngines++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// LANDING GEARS WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getLandingGears() != null) {
			Sheet sheetLandingGears = wb.createSheet("LANDING GEARS");
			List<Object[]> dataListLandingGears = new ArrayList<>();
			dataListLandingGears.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListLandingGears.add(new Object[] {"Reference Mass","kg", _landingGearsReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListLandingGears.add(new Object[] {" "});
			dataListLandingGears.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexLandingGears=0;
			for(MethodEnum methods : _theAircraft.getLandingGears().getMassMap().keySet()) {
				if(_theAircraft.getLandingGears().getMassMap().get(methods) != null) 
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theAircraft.getLandingGears().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getLandingGears().getPercentDifference()[indexLandingGears]
							}
							);
				indexLandingGears++;
			}
			dataListLandingGears.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getLandingGears().getMassEstimated().getEstimatedValue(),
							_theAircraft.getLandingGears().getMassEstimated().
								minus(_theAircraft.getLandingGears().getReferenceMass()).
								divide(_theAircraft.getLandingGears().getReferenceMass()).
								getEstimatedValue()*100
					});

			Row rowLandingGears = sheetLandingGears.createRow(0);
			Object[] objArrLandingGears = dataListLandingGears.get(0);
			int cellnumLandingGears = 0;
			for (Object obj : objArrLandingGears) {
				Cell cell = rowLandingGears.createCell(cellnumLandingGears++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetLandingGears.setDefaultColumnWidth(25);
			}

			int rownumLandingGears = 1;
			for (int j = 1; j < dataListLandingGears.size(); j++) {
				objArrLandingGears = dataListLandingGears.get(j);
				rowLandingGears = sheetLandingGears.createRow(rownumLandingGears++);
				cellnumLandingGears = 0;
				for (Object obj : objArrLandingGears) {
					Cell cell = rowLandingGears.createCell(cellnumLandingGears++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// SYSTEMS WEIGHTS ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theAircraft.getSystems() != null) {
			Sheet sheetSystems = wb.createSheet("SYSTEMS");
			List<Object[]> dataListSystems = new ArrayList<>();
			dataListSystems.add(new Object[] {"Description","Unit","Value","Percent Error"});
			dataListSystems.add(new Object[] {"Reference Mass","kg", _systemsReferenceMass.doubleValue(SI.KILOGRAM)});
			dataListSystems.add(new Object[] {" "});
			dataListSystems.add(new Object[] {"WEIGHT ESTIMATION METHODS COMPARISON"});
			int indexSystems=0;
			for(MethodEnum methods : _theAircraft.getSystems().getMassMap().keySet()) {
				if(_theAircraft.getSystems().getMassMap().get(methods) != null) 
					dataListSystems.add(
							new Object[] {
									methods.toString(),
									"kg",
									_theAircraft.getSystems().getMassMap().get(methods).getEstimatedValue(),
									_theAircraft.getSystems().getPercentDifference()[indexSystems]
							}
							);
				indexSystems++;
			}
			dataListSystems.add(new Object[] 
					{"Estimated Mass ",
							"kg",
							_theAircraft.getSystems().getOverallMass().getEstimatedValue(),
							_theAircraft.getSystems().getOverallMass().
								minus(_theAircraft.getSystems().getReferenceMass()).
								divide(_theAircraft.getSystems().getReferenceMass()).
								getEstimatedValue()*100
					});

			Row rowSystems = sheetSystems.createRow(0);
			Object[] objArrSystems = dataListSystems.get(0);
			int cellnumSystems = 0;
			for (Object obj : objArrSystems) {
				Cell cell = rowSystems.createCell(cellnumSystems++);
				cell.setCellStyle(styleHead);
				if (obj instanceof Date) {
					cell.setCellValue((Date) obj);
				} else if (obj instanceof Boolean) {
					cell.setCellValue((Boolean) obj);
				} else if (obj instanceof String) {
					cell.setCellValue((String) obj);
				} else if (obj instanceof Double) {
					cell.setCellValue((Double) obj);
				}
				sheetSystems.setDefaultColumnWidth(25);
			}

			int rownumSystems = 1;
			for (int j = 1; j < dataListSystems.size(); j++) {
				objArrSystems = dataListSystems.get(j);
				rowSystems = sheetSystems.createRow(rownumSystems++);
				cellnumSystems = 0;
				for (Object obj : objArrSystems) {
					Cell cell = rowSystems.createCell(cellnumSystems++);
					if (obj instanceof Date) {
						cell.setCellValue((Date) obj);
					} else if (obj instanceof Boolean) {
						cell.setCellValue((Boolean) obj);
					} else if (obj instanceof String) {
						cell.setCellValue((String) obj);
					} else if (obj instanceof Double) {
						cell.setCellValue((Double) obj);
					}
				}
			}
		}
		
		//--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
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
			Map <ComponentEnum, MethodEnum> methodsMap) {

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

			aircraft.getTheAnalysisManager().getTheWeights().calculateDependentVariables(aircraft);

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

			aircraft.getTheAnalysisManager().getTheWeights().setOperatingEmptyMass(
					aircraft.getTheAnalysisManager().getTheWeights().getManufacturerEmptyMass().plus(
							aircraft.getTheAnalysisManager().getTheWeights().getOperatingItemMass()).plus(
									aircraft.getTheAnalysisManager().getTheWeights().getCrewMass()));

			// --- END OF OPERATING EMPTY MASS-----------------------------------

			// Zero fuel mass
			aircraft.getTheAnalysisManager().getTheWeights().setZeroFuelMass(
					aircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().plus(
							_paxMass));

			aircraft.getTheAnalysisManager().getTheWeights().setZeroFuelWeight(
					aircraft.getTheAnalysisManager().getTheWeights().getZeroFuelMass().times(
							AtmosphereCalc.g0).to(SI.NEWTON));

			// Maximum zero fuel mass
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumZeroFuelMass(
					aircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().plus(
							_paxMassMax));

			// --- END ZERO FUEL MASS-----------------------------------

			// Take-off mass
			aircraft.getTheAnalysisManager().getTheWeights().setTakeOffMass(
					aircraft.getTheAnalysisManager().getTheWeights().getZeroFuelMass().plus(
							aircraft.getFuelTank().getFuelMass()));

			aircraft.getTheAnalysisManager().getTheWeights().setTakeOffWeight(
					aircraft.getTheAnalysisManager().getTheWeights().getTakeOffMass().times(
							AtmosphereCalc.g0).to(SI.NEWTON));

			// Maximum take-off mass
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumTakeOffMass(
					aircraft.getTheAnalysisManager().getTheWeights().getMaximumZeroFuelMass().plus(
							aircraft.getFuelTank().getFuelMass()));

			// Maximum landing mass
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumLandingMass(_maximumTakeOffMass.times(0.9));

			System.out.println("Iteration " + (i+1) + 
					", Structure mass: " + aircraft.getTheAnalysisManager().getTheWeights().getStructuralMass() + 
					" , Maximum Take-Off Mass: " + _maximumTakeOffMass);

			sum = sum.plus(_maximumTakeOffMass);
			i++;
			if(i<4)
				_maximumTakeOffMass = sum.divide(i);

			aircraft.getTheAnalysisManager().getTheWeights().calculateDependentVariables(aircraft);
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
			Map <ComponentEnum, MethodEnum> methodsMap) {

		if(aircraft.getFuselage() != null)
			aircraft.getFuselage().calculateMass(aircraft, methodsMap);

		if(aircraft.getWing() != null)
			aircraft.getWing().calculateMass(aircraft, ComponentEnum.WING, methodsMap);
		if(aircraft.getHTail() != null)
			aircraft.getHTail().calculateMass(aircraft, ComponentEnum.HORIZONTAL_TAIL, methodsMap);
		if(aircraft.getVTail() != null)
			aircraft.getVTail().calculateMass(aircraft, ComponentEnum.VERTICAL_TAIL, methodsMap);
		if(aircraft.getCanard() != null)
			aircraft.getCanard().calculateMass(aircraft, ComponentEnum.CANARD, methodsMap);
		
		if(aircraft.getNacelles() != null)
			aircraft.getNacelles().calculateMass(aircraft, methodsMap);

		if(aircraft.getLandingGears() != null)
			aircraft.getLandingGears().calculateMass(aircraft, methodsMap);

		if(aircraft.getSystems() != null)
			aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);

		if(aircraft.getCanard() != null)
			aircraft.getTheAnalysisManager().getTheWeights().setStructuralMass(
					aircraft.getFuselage().getMassEstimated().plus(
							aircraft.getWing().getMassEstimated()).plus(
									aircraft.getHTail().getMassEstimated()).plus(
											aircraft.getVTail().getMassEstimated()).plus(
													aircraft.getCanard().getMassEstimated()).plus(
															aircraft.getNacelles().getTotalMass()).plus(
																	aircraft.getLandingGears().getMassEstimated()));
		else
			aircraft.getTheAnalysisManager().getTheWeights().setStructuralMass(
					aircraft.getFuselage().getMassEstimated().plus(
							aircraft.getWing().getMassEstimated()).plus(
									aircraft.getHTail().getMassEstimated()).plus(
											aircraft.getVTail().getMassEstimated()).plus(
													aircraft.getNacelles().getTotalMass()).plus(
															aircraft.getLandingGears().getMassEstimated()));

	}

	public void calculateManufacturerEmptyMass(Aircraft aircraft) {
		if(aircraft.getSystems() != null)
			aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		if(aircraft.getCabinConfiguration() != null)
			aircraft.getCabinConfiguration().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		
		aircraft.getTheAnalysisManager().getTheWeights().setManufacturerEmptyMass(
				aircraft.getPowerPlant().getTotalMass().plus(
						aircraft.getTheAnalysisManager().getTheWeights().getStructuralMass()).plus(
								aircraft.getSystems().getOverallMass()).plus(
										aircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment()));
	}


	public void calculateFirstGuessMTOM(Aircraft aircraft) {

		if(aircraft.getFuselage() != null)
			aircraft.getFuselage().getFuselageCreator().setMassReference(_fuselageReferenceMass);
		if(aircraft.getWing() != null)
			aircraft.getWing().setMassReference(_wingReferenceMass);
		if(aircraft.getHTail() != null)
			aircraft.getHTail().setMassReference(_horizontalTailReferenceMass);
		if(aircraft.getVTail() != null)
			aircraft.getVTail().setMassReference(_verticalTailReferenceMass);
		if(aircraft.getCanard() != null)
			aircraft.getCanard().setMassReference(_canardReferenceMass);
		if(aircraft.getPowerPlant() != null)
			aircraft.getPowerPlant().setDryMassPublicDomainTotal(_engineReferenceMass);
		if(aircraft.getNacelles() != null)
			aircraft.getNacelles().setMassReference(_nacelleReferenceMass);
		if(aircraft.getFuelTank() != null)
			aircraft.getFuelTank().setMassReference(_fuelTankReferenceMass);
		if(aircraft.getLandingGears() != null)
			aircraft.getLandingGears().setReferenceMass(_landingGearsReferenceMass);
		if(aircraft.getSystems() != null)
			aircraft.getSystems().setReferenceMass(_systemsReferenceMass);

		if(aircraft.getCanard() != null)
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumTakeOffMass(
					aircraft.getFuselage().getFuselageCreator().getMassReference().plus(
							aircraft.getWing().getMassReference()).plus(
									aircraft.getHTail().getMassReference()).plus(
											aircraft.getVTail().getMassReference()).plus(
													aircraft.getCanard().getMassReference()).plus(
															aircraft.getNacelles().getMassReference()).plus(
																	aircraft.getPowerPlant().getDryMassPublicDomainTotal()).plus(
																			aircraft.getFuelTank().getMassReference()).plus(
																					aircraft.getSystems().getReferenceMass()).plus(
																							aircraft.getLandingGears().getReferenceMass()));
		else
			aircraft.getTheAnalysisManager().getTheWeights().setMaximumTakeOffMass(
					aircraft.getFuselage().getFuselageCreator().getMassReference().plus(
							aircraft.getWing().getMassReference()).plus(
									aircraft.getHTail().getMassReference()).plus(
											aircraft.getVTail().getMassReference()).plus(
															aircraft.getNacelles().getMassReference()).plus(
																	aircraft.getPowerPlant().getDryMassPublicDomainTotal()).plus(
																			aircraft.getFuelTank().getMassReference()).plus(
																					aircraft.getSystems().getReferenceMass()).plus(
																							aircraft.getLandingGears().getReferenceMass()));
			

		System.out.println("First guess value:" + aircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM));
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

	public Amount<Force> getEmptyWeight() {
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

	public Aircraft getTheAircraft() {
		return _theAircraft;
	}

	public void setTheAircraft(Aircraft _theAircraft) {
		ACWeightsManager._theAircraft = _theAircraft;
	}

	public Amount<Length> getRange() {
		return _referenceRange;
	}

	public void setRange(Amount<Length> _range) {
		this._referenceRange = _range;
	}

	public Amount<Mass> getFuselageReferenceMass() {
		return _fuselageReferenceMass;
	}

	public void setFuselageReferenceMass(Amount<Mass> _fuselageReferenceMass) {
		this._fuselageReferenceMass = _fuselageReferenceMass;
	}

	public Amount<Mass> getWingReferenceMass() {
		return _wingReferenceMass;
	}

	public void setWingReferenceMass(Amount<Mass> _wingReferenceMass) {
		this._wingReferenceMass = _wingReferenceMass;
	}

	public Amount<Mass> getHorizontalTailReferenceMass() {
		return _horizontalTailReferenceMass;
	}

	public void setHorizontalTailReferenceMass(Amount<Mass> _horizontalTailReferenceMass) {
		this._horizontalTailReferenceMass = _horizontalTailReferenceMass;
	}

	public Amount<Mass> getVerticalTailReferenceMass() {
		return _verticalTailReferenceMass;
	}

	public void setVerticalTailReferenceMass(Amount<Mass> _verticalTailReferenceMass) {
		this._verticalTailReferenceMass = _verticalTailReferenceMass;
	}

	public Amount<Mass> getCanardReferenceMass() {
		return _canardReferenceMass;
	}

	public void setCanardReferenceMass(Amount<Mass> _canardReferenceMass) {
		this._canardReferenceMass = _canardReferenceMass;
	}

	public Amount<Mass> getNacelleReferenceMass() {
		return _nacelleReferenceMass;
	}

	public void setNacelleReferenceMass(Amount<Mass> _nacelleReferenceMass) {
		this._nacelleReferenceMass = _nacelleReferenceMass;
	}

	public Amount<Mass> getEngineReferenceMass() {
		return _engineReferenceMass;
	}

	public void setEngineReferenceMass(Amount<Mass> _engineReferenceMass) {
		this._engineReferenceMass = _engineReferenceMass;
	}

	/**
	 * @return the _fuelTankReferenceMass
	 */
	public Amount<Mass> getFuelTankReferenceMass() {
		return _fuelTankReferenceMass;
	}

	/**
	 * @param _fuelTankReferenceMass the _fuelTankReferenceMass to set
	 */
	public void setFuelTankReferenceMass(Amount<Mass> _fuelTankReferenceMass) {
		this._fuelTankReferenceMass = _fuelTankReferenceMass;
	}

	public Amount<Mass> getLandingGearsReferenceMass() {
		return _landingGearsReferenceMass;
	}

	public void setLandingGearsReferenceMass(Amount<Mass> _landingGearsReferenceMass) {
		this._landingGearsReferenceMass = _landingGearsReferenceMass;
	}

	public Amount<Mass> getSystemsReferenceMass() {
		return _systemsReferenceMass;
	}

	public void setSystemsReferenceMass(Amount<Mass> _systemsReferenceMass) {
		this._systemsReferenceMass = _systemsReferenceMass;
	}
}
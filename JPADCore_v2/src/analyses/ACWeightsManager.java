package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Duration;
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
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.DatabaseManager;
import database.databasefunctions.FuelFractionDatabaseReader;
import ncsa.hdf.hdf5lib.exceptions.HDF5LibraryException;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.atmosphere.SpeedCalc;

/**
 * Manage components weight calculations
 * 
 * @author Lorenzo Attanasio, Vittorio Trifari
 */
public class ACWeightsManager implements IACWeightsManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	private String _id;
	private static Aircraft _theAircraft;
	private static OperatingConditions _theOperatingConditions;
	private static int _maxIteration = 20;  
	
	// Aluminum density
	public static Amount<VolumetricDensity> _materialDensity = 
			Amount.valueOf(2711.0,VolumetricDensity.UNIT);

	//---------------------------------------------------------------------------------
	// INPUT DATA : 
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _paxSingleMass;
	private Amount<Length> _cruiseRange;
	private Double _cruiseSFC;
	private Double _cruiseEfficiency;
	private Amount<Length> _alternateRange;
	private Double _alternateMach;
	private Amount<Length> _alternateAltitude;
	private Double _alternateSFC;
	private Double _alternateEfficiency;
	private Amount<Duration> _holdingDuration;
	private Amount<Length> _holdingAltitude;
	private Double _holdingMach;
	private Double _holdingSFC;
	private Double _holdingEfficiency;
	
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
	
	private Amount<Length> _referenceRange;
	private FuelFractionDatabaseReader _fuelFractionDatabaseReader;
	//---------------------------------------------------------------------------------
	// OUTPUT DATA : 
	private Amount<Mass> _paxMass;
	private Amount<Mass> _paxMassMax;
	private Amount<Mass> _crewMass;
	private Amount<Mass> _structuralMass;
	private Amount<Mass> _operatingItemMass;
	private Amount<Mass> _operatingEmptyMass;
	private Amount<Mass> _emptyMass;
	private Amount<Mass> _maximumZeroFuelMass;
	private Amount<Mass> _maximumLandingMass;
	private Amount<Mass> _manufacturerEmptyMass;
	private Amount<Mass> _zeroFuelMass;
	private Amount<Mass> _takeOffMass;
	private Amount<Mass> _trappedFuelOilMass;
	private Amount<Mass> _fuelMass;

	private Amount<Force> _paxWeight;
	private Amount<Force> _paxWeightMax;
	private Amount<Force> _creWeight;
	private Amount<Force> _structuralWeight;
	private Amount<Force> _operatingItemWeight;
	private Amount<Force> _operatingEmptyWeight;
	private Amount<Force> _emptyWeight;
	private Amount<Force> _maximumZeroFuelWeight;
	private Amount<Force> _maximumLandingWeight;
	private Amount<Force> _manufacturerEmptyWeight;
	private Amount<Force> _zeroFuelWeight;
	private Amount<Force> _takeOffWeight;
	private Amount<Force> _maximumTakeOffWeight;
	private Amount<Force> _trappedFuelOilWeight;
	private Amount<Force> _fuelWeight;
	
	private List<Amount<Mass>> _maximumTakeOffMassList;

	//============================================================================================
	// Builder pattern 
	//============================================================================================
	public static class ACWeightsManagerBuilder {
		
		// required parameters
		private String __id;
		private Aircraft __theAircraft;
		private OperatingConditions __theOperatingConditions;
		
		// optional parameters ... defaults
		// ...
		private Amount<Mass> __maximumTakeOffMass;
		private Amount<Mass> __paxSingleMass;
		private Amount<Length> __cruiseRange;
		private Double __cruiseSFC;
		private Double __cruiseEfficiency;
		private Amount<Length> __alternateRange;
		private Double __alternateMach;
		private Amount<Length> __alternateAltitude;
		private Double __alternateSFC;
		private Double __alternateEfficiency;
		private Amount<Duration> __holdingDuration;
		private Amount<Length> __holdingAltitude;
		private Double __holdingMach;
		private Double __holdingSFC;
		private Double __holdingEfficiency;
		
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
		
		public ACWeightsManagerBuilder id(String id) {
			this.__id = id;
			return this;
		}
		
		public ACWeightsManagerBuilder aircraft(Aircraft theAircraft) {
			this.__theAircraft = theAircraft;
			return this;
		}
		
		public ACWeightsManagerBuilder operatingConditions(OperatingConditions theOperatingConditions) {
			this.__theOperatingConditions = theOperatingConditions;
			return this;
		}
		
		public ACWeightsManagerBuilder maximumTakeOffMass(Amount<Mass> maximumTakeOffMass) {
			this.__maximumTakeOffMass = maximumTakeOffMass;
			return this;
		}
		
		public ACWeightsManagerBuilder singlePassengerMass(Amount<Mass> singlePassengerMass) {
			this.__paxSingleMass = singlePassengerMass;
			return this;
		}
		
		public ACWeightsManagerBuilder cruiseRange(Amount<Length> cruiseRange) {
			this.__cruiseRange = cruiseRange;
			return this;
		}
		
		public ACWeightsManagerBuilder cruiseSFC(Double cruiseSFC) {
			this.__cruiseSFC = cruiseSFC;
			return this;
		}
		
		public ACWeightsManagerBuilder cruiseEfficiency(Double cruiseEfficiency) {
			this.__cruiseEfficiency = cruiseEfficiency;
			return this;
		}
		
		public ACWeightsManagerBuilder alternateRange(Amount<Length> alternateRange) {
			this.__alternateRange = alternateRange;
			return this;
		}
		
		public ACWeightsManagerBuilder alternateAltitude(Amount<Length> alternateAltitude) {
			this.__alternateAltitude = alternateAltitude;
			return this;
		}
		
		public ACWeightsManagerBuilder alternateMach(Double alternateMach) {
			this.__alternateMach = alternateMach;
			return this;
		}
		
		public ACWeightsManagerBuilder alternateSFC(Double alternateSFC) {
			this.__alternateSFC = alternateSFC;
			return this;
		}
		
		public ACWeightsManagerBuilder alternateEfficiency(Double alternateEfficiency) {
			this.__alternateEfficiency = alternateEfficiency;
			return this;
		}
		
		public ACWeightsManagerBuilder holdingDuration(Amount<Duration> holdingDuration) {
			this.__holdingDuration = holdingDuration;
			return this;
		}
		
		public ACWeightsManagerBuilder holdingAltitude(Amount<Length> holdingAltitude) {
			this.__holdingAltitude = holdingAltitude;
			return this;
		}
		
		public ACWeightsManagerBuilder holdingMach(Double holdingMach) {
			this.__holdingMach = holdingMach;
			return this;
		}
		
		public ACWeightsManagerBuilder holdingSFC(Double holdingSFC) {
			this.__holdingSFC = holdingSFC;
			return this;
		}
		
		public ACWeightsManagerBuilder holdingEfficiency(Double holdingEfficiency) {
			this.__holdingEfficiency = holdingEfficiency;
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
		
		public ACWeightsManagerBuilder (String id, Aircraft theAircraft, OperatingConditions theOperatingConditions) {
			this.__id = id;
			this.__theAircraft = theAircraft;
			this.__theOperatingConditions = theOperatingConditions;
		}
		
		public ACWeightsManager build() throws HDF5LibraryException {
			return new ACWeightsManager(this);
		}
	}
	
	private ACWeightsManager(ACWeightsManagerBuilder builder) throws HDF5LibraryException {
		
		this._id = builder.__id;
		ACWeightsManager._theAircraft = builder.__theAircraft;
		ACWeightsManager._theOperatingConditions = builder.__theOperatingConditions;
		this._maximumTakeOffMass = builder.__maximumTakeOffMass;
		this._paxSingleMass = builder.__paxSingleMass;
		this._cruiseRange = builder.__cruiseRange;
		this._cruiseSFC = builder.__cruiseSFC;
		this._cruiseEfficiency = builder.__cruiseEfficiency;
		this._alternateRange = builder.__alternateRange;
		this._alternateAltitude = builder.__alternateAltitude;
		this._alternateMach = builder.__alternateMach;
		this._alternateSFC = builder.__alternateSFC;
		this._alternateEfficiency = builder.__alternateEfficiency;
		this._holdingDuration = builder.__holdingDuration;
		this._holdingAltitude = builder.__holdingAltitude;
		this._holdingMach = builder.__holdingMach;
		this._holdingSFC = builder.__holdingSFC;
		this._holdingEfficiency = builder.__holdingEfficiency;
		
		this._referenceRange = this._cruiseRange;
		
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
		
//		this._fuelFractionDatabaseReader = DatabaseManager.initializeFuelFractionDatabase(
//				new FuelFractionDatabaseReader(
//						MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
//						"FuelFractions.h5"
//						),
//				MyConfiguration.getDir(FoldersEnum.DATABASE_DIR)
//				);
		this._fuelFractionDatabaseReader = new FuelFractionDatabaseReader(
						MyConfiguration.getDir(FoldersEnum.DATABASE_DIR), 
						"FuelFractions.h5"
						);
		
	}
	
	//============================================================================================
	// End of the builder pattern 
	//============================================================================================
	
	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	public static ACWeightsManager importFromXML (String pathToXML, Aircraft theAircraft, OperatingConditions theOperatingConditions) throws HDF5LibraryException {
		
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
		// SINGLE PASSENGER MASS
		Amount<Mass> singlePassengerMass = Amount.valueOf(0.0, SI.KILOGRAM);
		String singlePassengerMassProperty = reader.getXMLPropertyByPath("//weights/global_data/single_passenger_mass");
		if(singlePassengerMassProperty != null)
			singlePassengerMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/global_data/single_passenger_mass");
		else {
			System.err.println("SINGLE PASSENGER MASS REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// CRUISE RANGE
		Amount<Length> cruiseRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		String cruiseRangeProperty = reader.getXMLPropertyByPath("//cruise_phase/range");
		if(cruiseRangeProperty != null)
			cruiseRange = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//cruise_phase/range");
		else {
			System.err.println("CRUISE RANGE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// CRUISE SFC
		Double cruiseSFC = 0.0;
		String cruiseSFCProperty = reader.getXMLPropertyByPath("//cruise_phase/sfc");
		if(cruiseSFCProperty != null)
			cruiseSFC = Double.valueOf(cruiseSFCProperty);
		else {
			System.err.println("CRUISE SFC REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// CRUISE EFFICIENCY
		Double cruiseEfficiency = 0.0;
		String cruiseEfficiencyProperty = reader.getXMLPropertyByPath("//cruise_phase/efficiency");
		if(cruiseEfficiencyProperty != null)
			cruiseEfficiency = Double.valueOf(cruiseEfficiencyProperty);
		else {
			System.err.println("CRUISE EFFICIENCY REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// ALTERNATE RANGE
		Amount<Length> alternateRange = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		String alternateRangeProperty = reader.getXMLPropertyByPath("//alternate_phase/range");
		if(alternateRangeProperty != null)
			alternateRange = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//alternate_phase/range");
		else {
			System.err.println("ALTERNATE RANGE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// ALTERNATE ALTITUDE
		Amount<Length> alternateAltitude = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		String alternateAltitudeProperty = reader.getXMLPropertyByPath("//alternate_phase/altitude");
		if(alternateAltitudeProperty != null)
			alternateAltitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//alternate_phase/altitude");
		else {
			System.err.println("ALTERNATE ALTITUDE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// ALTERNATE MACH
		Double alternateMach = 0.0;
		String alternateMachProperty = reader.getXMLPropertyByPath("//alternate_phase/mach");
		if(alternateMachProperty != null)
			alternateMach = Double.valueOf(alternateMachProperty);
		else {
			System.err.println("ALTERNATE MACH REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// ALTERNATE SFC
		Double alternateSFC = 0.0;
		String alternateSFCProperty = reader.getXMLPropertyByPath("//alternate_phase/sfc");
		if(alternateSFCProperty != null)
			alternateSFC = Double.valueOf(alternateMachProperty);
		else {
			System.err.println("ALTERNATE SFC REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// ALTERNATE EFFICIENCY
		Double alternateEfficiency = 0.0;
		String alternateEfficiencyProperty = reader.getXMLPropertyByPath("//alternate_phase/efficiency");
		if(alternateEfficiencyProperty != null)
			alternateEfficiency = Double.valueOf(alternateEfficiencyProperty);
		else {
			System.err.println("ALTERNATE EFFICIENCY REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// HOLDING DURATION
		Amount<Duration> holdingDuration = Amount.valueOf(0.0, NonSI.MINUTE);
		String holdingDurationProperty = reader.getXMLPropertyByPath("//holding_phase/duration");
		if(holdingDurationProperty != null)
			holdingDuration = (Amount<Duration>) reader.getXMLAmountWithUnitByPath("//holding_phase/duration");
		else {
			System.err.println("HOLDING DURATION REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// HOLDING ALTITUDE
		Amount<Length> holdingAltitude = Amount.valueOf(0.0, NonSI.NAUTICAL_MILE);
		String holdingAltitudeProperty = reader.getXMLPropertyByPath("//holding_phase/altitude");
		if(holdingAltitudeProperty != null)
			holdingAltitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//holding_phase/altitude");
		else {
			System.err.println("HOLDING ALTITUDE REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// HOLDING MACH
		Double holdingMach = 0.0;
		String holdingMachProperty = reader.getXMLPropertyByPath("//holding_phase/mach");
		if(holdingMachProperty != null)
			holdingMach = Double.valueOf(holdingMachProperty);
		else {
			System.err.println("HOLDING MACH REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// HOLDING SFC
		Double holdingSFC = 0.0;
		String holdingSFCProperty = reader.getXMLPropertyByPath("//holding_phase/sfc");
		if(holdingSFCProperty != null)
			holdingSFC = Double.valueOf(holdingSFCProperty);
		else {
			System.err.println("HOLDING SFC REQUIRED !! \n ... returning ");
			return null; 
		}
		
		//---------------------------------------------------------------
		// HOLDING EFFICIENCY
		Double holdingEfficiency = 0.0;
		String holdingEfficiencyProperty = reader.getXMLPropertyByPath("//holding_phase/efficiency");
		if(holdingEfficiencyProperty != null)
			holdingEfficiency = Double.valueOf(holdingEfficiencyProperty);
		else {
			System.err.println("HOLDING EFFICIENCY REQUIRED !! \n ... returning ");
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
		
		ACWeightsManager theWeigths = new ACWeightsManagerBuilder(id, theAircraft, theOperatingConditions)
				.maximumTakeOffMass(maximumTakeOffMass)
				.singlePassengerMass(singlePassengerMass)
				.cruiseRange(cruiseRange)
				.cruiseSFC(cruiseSFC)
				.cruiseEfficiency(cruiseEfficiency)
				.alternateRange(alternateRange)
				.alternateAltitude(alternateAltitude)
				.alternateMach(alternateMach)
				.alternateSFC(alternateSFC)
				.alternateEfficiency(alternateEfficiency)
				.holdingDuration(holdingDuration)
				.holdingAltitude(holdingAltitude)
				.holdingMach(holdingMach)
				.holdingSFC(holdingSFC)
				.holdingEfficiency(holdingEfficiency)
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
				.build();
		
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
				.append("\tMax Fuel Mass: " + _theAircraft.getFuelTank().getFuelMass() + "\n")
				.append("\tMax Fuel Weight: " + _theAircraft.getFuelTank().getFuelWeight() + "\n")	
				.append("\t·····································\n")
				.append("\tFuel Mass: " + _fuelMass + "\n")
				.append("\tFuel Weight: " + _fuelMass + "\n")	
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
				.append("\tMass furnishings and equipments: " + _theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment() + "\n")
				.append("\tWeight furnishings and equipments: " + _theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment().times(AtmosphereCalc.g0).to(SI.NEWTON) + "\n")
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
		dataListGlobal.add(new Object[] {"Single passenger Mass","kg",getPaxSingleMass().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Mass","kg",_maximumTakeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Take-Off Mass","kg",_takeOffMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Landing Mass","kg",_maximumLandingMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Mass","kg",_paxMassMax.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Actual Passengers Mass","kg",_paxMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Fuel Mass","kg",_theAircraft.getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Fuel Mass","kg", _fuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Crew Mass","kg",_crewMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Mass","kg",_maximumZeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Zero Fuel Mass","kg",_zeroFuelMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Empty Mass","kg",_operatingEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Empty Mass","kg",_emptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Mass","kg",_manufacturerEmptyMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Operating Item Mass","kg",_operatingItemMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Furnishings and Equipments Mass","kg",_theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment().doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {"Trapped Fuel Oil Mass","kg",_trappedFuelOilMass.doubleValue(SI.KILOGRAM)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Maximum Take-Off Weight","N",_maximumTakeOffWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Take-Off Weight","N",_takeOffWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Maximum Landing Weight","N",_maximumLandingWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Maximum Passengers Weight","N",(_paxMassMax.times(AtmosphereCalc.g0)).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Actual Passengers Weight","N",(_paxMass.times(AtmosphereCalc.g0)).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Fuel Weight","N",_theAircraft.getFuelTank().getFuelWeight().doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Crew Weight","N",_crewMass.times(AtmosphereCalc.g0).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Maximum Zero Fuel Weight","N",_maximumZeroFuelWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Zero Fuel Weight","N",_zeroFuelWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Operating Empty Weight","N",_operatingEmptyWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Empty Weight","N",_emptyWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Manufacturer Empty Weight","N",_manufacturerEmptyMass.times(AtmosphereCalc.g0).getEstimatedValue()});
		dataListGlobal.add(new Object[] {"Operating Item Weight","N",_operatingItemWeight.doubleValue(SI.NEWTON)});
		dataListGlobal.add(new Object[] {"Furnishings and Equipments Weight","N",_theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment().times(AtmosphereCalc.g0).getEstimatedValue()});
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
			sheet.setDefaultColumnWidth(35);
    		sheet.setColumnWidth(1, 2048);
    		sheet.setColumnWidth(2, 3840);
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
				sheetFuselage.setDefaultColumnWidth(35);
				sheetFuselage.setColumnWidth(1, 2048);
				sheetFuselage.setColumnWidth(2, 3840);
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
			dataListWing.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getWing().getLiftingSurfaceCreator().getCompositeCorrectionFactor()});
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
				sheetWing.setDefaultColumnWidth(35);
				sheetWing.setColumnWidth(1, 2048);
				sheetWing.setColumnWidth(2, 3840);
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
			dataListHTail.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getHTail().getLiftingSurfaceCreator().getCompositeCorrectionFactor()});
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
				sheetHTail.setDefaultColumnWidth(35);
				sheetHTail.setColumnWidth(1, 2048);
				sheetHTail.setColumnWidth(2, 3840);
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
			dataListVTail.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getVTail().getLiftingSurfaceCreator().getCompositeCorrectionFactor()});
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
				sheetVTail.setDefaultColumnWidth(35);
				sheetVTail.setColumnWidth(1, 2048);
				sheetVTail.setColumnWidth(2, 3840);
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
			dataListCanard.add(new Object[] {"Composite Correction Factor"," ",_theAircraft.getCanard().getLiftingSurfaceCreator().getCompositeCorrectionFactor()});
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
				sheetCanard.setDefaultColumnWidth(35);
				sheetCanard.setColumnWidth(1, 2048);
				sheetCanard.setColumnWidth(2, 3840);
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
				sheetNacelles.setDefaultColumnWidth(35);
				sheetNacelles.setColumnWidth(1, 2048);
				sheetNacelles.setColumnWidth(2, 3840);
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
				dataListPowerPlant.add(new Object[] {"Total Mass","kg", _theAircraft.getPowerPlant().getEngineList().get(iEngine).getTheWeights().getTotalMass().getEstimatedValue()});			
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
				sheetPowerPlant.setDefaultColumnWidth(35);
				sheetPowerPlant.setColumnWidth(1, 2048);
				sheetPowerPlant.setColumnWidth(2, 3840);
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
				sheetLandingGears.setDefaultColumnWidth(35);
				sheetLandingGears.setColumnWidth(1, 2048);
				sheetLandingGears.setColumnWidth(2, 3840);
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
				sheetSystems.setDefaultColumnWidth(35);
				sheetSystems.setColumnWidth(1, 2048);
				sheetSystems.setColumnWidth(2, 3840);
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

	public void plotWeightBreakdown(String weightsFolderPath) {
		
		List<String> labels = new ArrayList<>();
		List<Double> values = new ArrayList<>();
		
		Double fuselageMass = 0.0;
		Double wingMass = 0.0;
		Double hTailMass = 0.0;
		Double vTailMass = 0.0;
		Double canardMass = 0.0;
		Double nacellesMass = 0.0;
		Double landingGearsMass = 0.0;
		
		Double powerPlantMass = 0.0;
		Double systemsMass = 0.0;
		Double cabinConfigurationMass = 0.0;
		
		Double operatingItemMass = 0.0;
		Double crewMass = 0.0;
		
		Double maxPassengersMass = 0.0;
		
		Double maxFuelMass = 0.0;
		
		Double maxTakeOffMass = _theAircraft.getTheAnalysisManager().getTheWeights().getMaximumTakeOffMass().doubleValue(SI.KILOGRAM);
		
		if(_theAircraft.getFuselage() != null)
			if(_theAircraft.getFuselage().getMassEstimated() != null) {
				labels.add("Fuselage");
				fuselageMass = _theAircraft.getFuselage().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(fuselageMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getWing() != null)
			if(_theAircraft.getWing().getMassEstimated() != null) {
				labels.add("Wing");
				wingMass = _theAircraft.getWing().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(wingMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getHTail() != null)
			if(_theAircraft.getHTail().getMassEstimated() != null) {
				labels.add("Horizontal Tail");
				hTailMass = _theAircraft.getHTail().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(hTailMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getVTail() != null)
			if(_theAircraft.getVTail().getMassEstimated() != null) {
				labels.add("Vertical Tail");
				vTailMass = _theAircraft.getVTail().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(vTailMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getCanard() != null)
			if(_theAircraft.getCanard().getMassEstimated() != null) {
				labels.add("Canard");
				canardMass = _theAircraft.getCanard().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(canardMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getNacelles() != null)
			if(_theAircraft.getNacelles().getTotalMass() != null) {
				labels.add("Nacelles");
				nacellesMass = _theAircraft.getNacelles().getTotalMass().doubleValue(SI.KILOGRAM);
				values.add(nacellesMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getLandingGears() != null)
			if(_theAircraft.getLandingGears().getMassEstimated() != null) {
				labels.add("Landing Gears");
				landingGearsMass = _theAircraft.getLandingGears().getMassEstimated().doubleValue(SI.KILOGRAM);
				values.add(landingGearsMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getPowerPlant() != null)
			if(_theAircraft.getPowerPlant().getTotalMass() != null) {
				labels.add("Power Plant");
				powerPlantMass = _theAircraft.getPowerPlant().getTotalMass().doubleValue(SI.KILOGRAM);
				values.add(powerPlantMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getSystems() != null)
			if(_theAircraft.getSystems().getOverallMass() != null) {
				labels.add("Systems");
				systemsMass = _theAircraft.getSystems().getOverallMass().doubleValue(SI.KILOGRAM);
				values.add(systemsMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getCabinConfiguration() != null)
			if(_theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment() != null) {
				labels.add("Furnishings and Equipment");
				cabinConfigurationMass = _theAircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment().doubleValue(SI.KILOGRAM);
				values.add(cabinConfigurationMass/maxTakeOffMass*100.0);
			}
		if(_theAircraft.getTheAnalysisManager().getTheWeights().getOperatingItemMass() != null) {
			labels.add("Operating Items");
			operatingItemMass = _theAircraft.getTheAnalysisManager().getTheWeights().getOperatingItemMass().doubleValue(SI.KILOGRAM);
			values.add(operatingItemMass/maxTakeOffMass*100.0);
		}
		if(_theAircraft.getTheAnalysisManager().getTheWeights().getCrewMass() != null) {
			labels.add("Crew");
			crewMass = _theAircraft.getTheAnalysisManager().getTheWeights().getCrewMass().doubleValue(SI.KILOGRAM);
			values.add(crewMass/maxTakeOffMass*100.0);
		}
		if(_theAircraft.getTheAnalysisManager().getTheWeights().getPaxMassMax() != null) {
			labels.add("Passengers");
			maxPassengersMass = _theAircraft.getTheAnalysisManager().getTheWeights().getPaxMassMax().doubleValue(SI.KILOGRAM);
			values.add(maxPassengersMass/maxTakeOffMass*100.0);
		}
		if(_theAircraft.getFuelTank() != null)
			if(_theAircraft.getFuelTank().getFuelMass() != null) {
				labels.add("Fuel");
				maxFuelMass = _theAircraft.getFuelTank().getFuelMass().doubleValue(SI.KILOGRAM);
				values.add(maxFuelMass/maxTakeOffMass*100.0);
			}

		MyChartToFileUtils.plotPieChart(
				labels, 
				values, 
				"Weights Breakdown", 
				false, 
				weightsFolderPath, 
				"Weights_Breakdown"
				);
		
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
		
		// calculating all the dependent variables
		calculateDependentVariables(aircraft);
		
		/*
		 * Initialization of the _maximumZeroFuelMass. For the first iteration a value equal to 0.75% of the_maximumTakeOffMass has been chosen.
		 * From the second iteration on, the estimated value of the _maximumZeroFuelMass (at iteration i-1) will be used. 
		 * (_maximumZeroFuelWeight, _maximumTakeOffWeight and _maximumLandingMass must be calculated since they are used by
		 *  LiftingSurface and LandingGears to estimate their masses)
		 */
		_maximumZeroFuelMass = _maximumTakeOffMass.to(SI.KILOGRAM).times(0.75);
		_maximumZeroFuelWeight = Amount.valueOf( 
				_maximumTakeOffMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_maximumTakeOffWeight = Amount.valueOf( 
				_maximumTakeOffMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_maximumLandingMass = _maximumTakeOffMass.to(SI.KILOGRAM).times(0.97);
		
		/*
		 * The maximum take-off mass will be stored in a dedicated list to evaluate the while condition. 
		 */
		_maximumTakeOffMassList.add(Amount.valueOf(0.0, SI.KILOGRAM));
		_maximumTakeOffMassList.add(_maximumTakeOffMass.to(SI.KILOGRAM));
		
		/*
		 * The while loop will evaluate all the aircraft masses until the maximum take-off changes less then the threshold value 
		 */
		int i=1;
		while ((Math.abs((_maximumTakeOffMassList.get(i).minus(_maximumTakeOffMassList.get(i-1)))
				.divide(_maximumTakeOffMassList.get(i))
				.getEstimatedValue())
				>= 0.01)
				|| (_maxIteration >= i)
				) {
			
			/*
			 * The method estimateComponentsReferenceMasses is used to estimate all the components reference masses.
			 */
			estimateComponentsReferenceMasses(aircraft);
			
			/*
			 * All the following methods are use to estimate all the aircraft masses.
			 */
			calculateStructuralMass(aircraft, methodsMap);
			// --- END OF STRUCTURE MASS-----------------------------------
			
			aircraft.getPowerPlant().calculateMass(aircraft);
			// --- END OF POWER PLANT MASS-----------------------------------
			
			calculateManufacturerEmptyMass(aircraft);
			// --- END OF MANUFACTURER EMPTY MASS-----------------------------------
			
			calculateOperatingEmptyMass(aircraft);
			// --- END OF OPERATING EMPTY MASS-----------------------------------
			
			calculateEmptyMass();
			// --- END OF EMPTY MASS-----------------------------------
			
			calculateMaximumZeroFuelMass(aircraft);
			// --- END ZERO FUEL MASS-----------------------------------
			
			calculateMaximumTakeOffMass(aircraft);
			// --- END TAKE-OFF MASS-----------------------------------
			
			calculateMaximumLandingMass();
			// --- END LANDING MASS-----------------------------------
			
			_maximumTakeOffMassList.add(_maximumTakeOffMass.to(SI.KILOGRAM));
			System.out.println("Iteration " + (i) + " --> Maximum Take-Off Mass: " + _maximumTakeOffMass);
			i++;
			
		}
		
		/*
		 * Once the while loop has ended and all the masses have been estimated, 
		 * the method calculateAllWeights will generate all the weights.
		 */
		calculateAllWeights();
		
		System.out.println("\n-----------------------------------------------");
		System.out.println("--- WEIGHT ESTIMATION PROCEDURE COMPLETED -----");
		System.out.println("-----------------------------------------------\n");
		
	}

	private void calculateDependentVariables(Aircraft aircraft) {

		// Passengers and crew mass
		// 76.5 kg for each crew member + baggage
		_paxMass = _paxSingleMass.times(aircraft.getCabinConfiguration().getNPax());
		_crewMass = Amount.valueOf(aircraft.getCabinConfiguration().getNCrew() * 76.5145485, SI.KILOGRAM); 
		_paxMassMax = getPaxSingleMass().times(aircraft.getCabinConfiguration().getMaxPax());

		// Operating items mass
		if (_referenceRange.doubleValue(NonSI.NAUTICAL_MILE) < 2000) { 
			_operatingItemMass = Amount.valueOf(8.617*aircraft.getCabinConfiguration().getMaxPax(), SI.KILOGRAM);
			_operatingItemWeight = _operatingItemMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		} else {
			_operatingItemMass = Amount.valueOf(14.97*aircraft.getCabinConfiguration().getMaxPax(), SI.KILOGRAM);
			_operatingItemWeight = _operatingItemMass.times(AtmosphereCalc.g0).to(SI.NEWTON);
		}

	}
	
	private void calculateStructuralMass(
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

		if(aircraft.getCanard() != null)
			_structuralMass = 
					aircraft.getFuselage().getMassEstimated().to(SI.KILOGRAM)
					.plus(aircraft.getWing().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getHTail().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getVTail().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getCanard().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getNacelles().getTotalMass().to(SI.KILOGRAM))
					.plus(aircraft.getLandingGears().getMassEstimated().to(SI.KILOGRAM));
		else
			_structuralMass = 
					aircraft.getFuselage().getMassEstimated().to(SI.KILOGRAM)
					.plus(aircraft.getWing().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getHTail().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getVTail().getMassEstimated().to(SI.KILOGRAM))
					.plus(aircraft.getNacelles().getTotalMass().to(SI.KILOGRAM))
					.plus(aircraft.getLandingGears().getMassEstimated().to(SI.KILOGRAM));

	}

	private void calculateManufacturerEmptyMass(Aircraft aircraft) {
		
		if(aircraft.getSystems() != null)
			aircraft.getSystems().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		if(aircraft.getCabinConfiguration() != null)
			aircraft.getCabinConfiguration().calculateMass(aircraft, MethodEnum.TORENBEEK_2013);
		
		_manufacturerEmptyMass = 
				aircraft.getPowerPlant().getTotalMass().to(SI.KILOGRAM)
				.plus(_structuralMass.to(SI.KILOGRAM))
				.plus(aircraft.getSystems().getOverallMass())
				.plus(aircraft.getCabinConfiguration().getMassEstimatedFurnishingsAndEquipment());
	}
	
	private void calculateOperatingEmptyMass(Aircraft aircraft) {
		
		_operatingEmptyMass = 
				_manufacturerEmptyMass.to(SI.KILOGRAM)
				.plus(_operatingItemMass.to(SI.KILOGRAM))
				.plus(_crewMass.to(SI.KILOGRAM));
		
	}

	private void calculateMaximumZeroFuelMass(Aircraft aircraft) {
		
		// Zero fuel mass
		_zeroFuelMass = 
				_operatingEmptyMass.to(SI.KILOGRAM)
				.plus(_paxMass.to(SI.KILOGRAM));

		// Maximum zero fuel mass
		aircraft.getTheAnalysisManager().getTheWeights().setMaximumZeroFuelMass(
				aircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().plus(
						_paxMassMax));
		
	}
	
	private void calculateMaximumTakeOffMass(Aircraft aircraft) {
		
		calculateFuelMasses(aircraft);
		
		// Take-off mass
		_takeOffMass = 
				_zeroFuelMass.to(SI.KILOGRAM)
				.plus(_fuelMass.to(SI.KILOGRAM));

		// Maximum take-off mass
		_maximumTakeOffMass = 
				_maximumZeroFuelMass.to(SI.KILOGRAM)
				.plus(_fuelMass.to(SI.KILOGRAM));
		
	}
	
	private void calculateMaximumLandingMass() {
		
		_maximumLandingMass = _maximumTakeOffMass.times(0.97);
		
	}
	
	private void calculateFuelMasses(Aircraft aircraft) {
		
		//..........................................................
		// MAX FUEL MASS
		//..........................................................
		aircraft.getFuelTank().calculateFuelMass();
		
		//..........................................................
		// MISSION FUEL MASS
		//..........................................................
		
		// calculating the mass ratios from statistical values...
		double[][] fuelFractionTable = null;
		double mffRatio = 1.0;
		
		try {
			fuelFractionTable = _fuelFractionDatabaseReader.getFuelFractionTable("FuelFractions_Roskam");
		} catch (HDF5LibraryException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		switch (aircraft.getTypeVehicle()){

		case TURBOPROP: int indexTurboprop = 5;
		for (int i=0; i<6; i++) 
			mffRatio *= fuelFractionTable[indexTurboprop][i];
		break;

		case JET: int indexJet = 6;
		for (int i=0; i<6; i++)
			mffRatio *= fuelFractionTable[indexJet][i]; 
		break;

		case BUSINESS_JET: int indexBusinessJet = 4;
		for (int i=0; i<6; i++)
			mffRatio *= fuelFractionTable[indexBusinessJet][i]; 
		break;

		case FIGHTER: int indexFighter = 8;
		for (int i=0; i<6; i++)
			mffRatio *= fuelFractionTable[indexFighter][i]; 
		break;

		case ACROBATIC: int indexAcrobatic = 7;
		for (int i=0; i<6; i++)
			mffRatio *= fuelFractionTable[indexAcrobatic][i]; 
		break;

		case COMMUTER: int indexCommuter = 5;
		for (int i=0; i<6; i++)
			mffRatio *= fuelFractionTable[indexCommuter][i]; 
		break;

		case GENERAL_AVIATION:
		if(aircraft.getPowerPlant().getEngineNumber() == 1) {
			int indexSingleEngine = 1;	
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexSingleEngine][i];
		}
		else if(aircraft.getPowerPlant().getEngineNumber() == 2) {
			int indexTwinEngine = 2;
			for (int i=0; i<6; i++)
				mffRatio *= fuelFractionTable[indexTwinEngine][i];
		}
		break;
		}
		
		// calculating the mass ratios of cruise, alternate and holding phases ...
		double massRatioCruise = 1.0;
		double massRatioAlternate = 1.0;
		double massRatioHolding = 1.0;
		
		switch (aircraft.getPowerPlant().getEngineType()) {
		case PISTON:
			massRatioCruise = 1/Math.exp(
					(_cruiseRange.doubleValue(SI.KILOMETER)*_cruiseSFC)
					/(603.5*aircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller()*_cruiseEfficiency)
					);
			massRatioAlternate = 1/Math.exp(
					(_alternateRange.doubleValue(SI.KILOMETER)*_alternateSFC)
					/(603.5*aircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller()*_alternateEfficiency)
					);
			massRatioHolding = 1/Math.exp(
					(_holdingDuration.doubleValue(NonSI.HOUR)*_holdingSFC*
							Amount.valueOf(
									SpeedCalc.calculateTAS(_holdingMach, _holdingAltitude.doubleValue(SI.METER)),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KILOMETERS_PER_HOUR)
							)
					/(603.5*aircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller()*_holdingEfficiency)
					);
			break;
		case TURBOPROP:
			massRatioCruise = 1/Math.exp(
					(_cruiseRange.doubleValue(SI.KILOMETER)*_cruiseSFC)
					/(603.5*aircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller()*_cruiseEfficiency)
					);
			massRatioAlternate = 1/Math.exp(
					(_alternateRange.doubleValue(SI.KILOMETER)*_alternateSFC)
					/(603.5*aircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller()*_alternateEfficiency)
					);
			massRatioHolding = 1/Math.exp(
					(_holdingDuration.doubleValue(NonSI.HOUR)*_holdingSFC*
							Amount.valueOf(
									SpeedCalc.calculateTAS(_holdingMach, _holdingAltitude.doubleValue(SI.METER)),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KILOMETERS_PER_HOUR)
							)
					/(603.5*aircraft.getPowerPlant().getEngineList().get(0).getEtaPropeller()*_holdingEfficiency)
					);
			break;
		case TURBOFAN:
			massRatioCruise = 1/Math.exp(
					(_cruiseRange.doubleValue(NonSI.NAUTICAL_MILE)*_cruiseSFC)
					/(_cruiseEfficiency*
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											_theOperatingConditions.getMachCruise(),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KNOT)
							)
					);
			massRatioAlternate = 1/Math.exp(
					(_alternateRange.doubleValue(NonSI.NAUTICAL_MILE)*_alternateSFC)
					/(_alternateEfficiency*
							Amount.valueOf(
									SpeedCalc.calculateTAS(_alternateMach, _alternateAltitude.doubleValue(SI.METER)),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KNOT)
							)
					);
			massRatioHolding = 1/Math.exp(
					(_holdingDuration.doubleValue(NonSI.HOUR)*_holdingSFC)
					/(_holdingEfficiency)
					);
			break;
		case TURBOJET:
			massRatioCruise = 1/Math.exp(
					(_cruiseRange.doubleValue(NonSI.NAUTICAL_MILE)*_cruiseSFC)
					/(_cruiseEfficiency*
							Amount.valueOf(
									SpeedCalc.calculateTAS(
											_theOperatingConditions.getMachCruise(),
											_theOperatingConditions.getAltitudeCruise().doubleValue(SI.METER)
											),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KNOT)
							)
					);
			massRatioAlternate = 1/Math.exp(
					(_alternateRange.doubleValue(NonSI.NAUTICAL_MILE)*_alternateSFC)
					/(_alternateEfficiency*
							Amount.valueOf(
									SpeedCalc.calculateTAS(_alternateMach, _alternateAltitude.doubleValue(SI.METER)),
									SI.METERS_PER_SECOND
									).doubleValue(NonSI.KNOT)
							)
					);
			massRatioHolding = 1/Math.exp(
					(_holdingDuration.doubleValue(NonSI.HOUR)*_holdingSFC)
					/(_holdingEfficiency)
					);
			break;
		default:
			break;
		}
		
		// calculating the mission fuel mass ...
		mffRatio *= massRatioCruise*massRatioAlternate*massRatioHolding;
		
		_fuelMass = _maximumTakeOffMass.times(1-mffRatio);
		
	}
	
	private void calculateEmptyMass() {
		
		if(_maximumTakeOffMass.doubleValue(NonSI.POUND) < 100000)
			_trappedFuelOilMass = Amount.valueOf(0.0, SI.KILOGRAM);
		else
			_trappedFuelOilMass = _maximumTakeOffMass.times(0.005);
			
		_emptyMass =_operatingEmptyMass.minus(_crewMass).minus(_trappedFuelOilMass);
		
	}
	
	private void calculateAllWeights() {
		
		_paxWeight = Amount.valueOf( 
				_paxMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_paxWeightMax = Amount.valueOf( 
				_paxMassMax.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_creWeight = Amount.valueOf( 
				_crewMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_structuralWeight = Amount.valueOf( 
				_structuralMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_manufacturerEmptyWeight = Amount.valueOf( 
				_manufacturerEmptyMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_operatingItemWeight = Amount.valueOf( 
				_operatingItemMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_operatingEmptyWeight = Amount.valueOf( 
				_operatingEmptyMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_trappedFuelOilWeight = Amount.valueOf( 
				_trappedFuelOilMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_emptyWeight = Amount.valueOf( 
				_emptyMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_zeroFuelWeight = Amount.valueOf( 
				_zeroFuelMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_maximumZeroFuelWeight = Amount.valueOf( 
				_maximumZeroFuelMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_takeOffWeight = Amount.valueOf( 
				_takeOffMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_maximumTakeOffWeight = Amount.valueOf( 
				_maximumTakeOffMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		_maximumLandingWeight = Amount.valueOf( 
				_maximumLandingMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				);
		setFuelWeight(Amount.valueOf( 
				_fuelMass.doubleValue(SI.KILOGRAM)*AtmosphereCalc.g0.doubleValue(SI.METERS_PER_SQUARE_SECOND),
				SI.NEWTON
				));
		
	}
	
	private void estimateComponentsReferenceMasses(Aircraft aircraft) {
		
		// ESTIMATION OF THE REFERENCE MASS OF EACH COMPONENT (if not assigned) 
		if(aircraft.getFuselage() != null) 
			if(this._fuselageReferenceMass == null) {
				this._fuselageReferenceMass = _maximumZeroFuelMass.times(.15);
				aircraft.getFuselage().getFuselageCreator().setMassReference(_fuselageReferenceMass);
			}
		if(aircraft.getWing() != null)
			if(this._wingReferenceMass == null) {
				this._wingReferenceMass = _maximumZeroFuelMass.times(.1);
				aircraft.getWing().setMassReference(_wingReferenceMass);
			}
		if(aircraft.getHTail() != null) 
			if(this._horizontalTailReferenceMass == null) {
				this._horizontalTailReferenceMass = _maximumZeroFuelMass.times(.015);
				aircraft.getHTail().setMassReference(_horizontalTailReferenceMass);
			}
		if(aircraft.getVTail() != null)
			if(this._verticalTailReferenceMass == null) {
				this._verticalTailReferenceMass = _maximumZeroFuelMass.times(.015);
				aircraft.getVTail().setMassReference(_verticalTailReferenceMass);
			}
		if(aircraft.getCanard() != null)
			if(this._canardReferenceMass == null) {
				this._canardReferenceMass = _maximumZeroFuelMass.times(.015);
				aircraft.getCanard().setMassReference(_canardReferenceMass);
			}
		if(aircraft.getPowerPlant() != null)
			if(this._engineReferenceMass == null) {
				this._engineReferenceMass = _maximumZeroFuelMass.times(.05);
				aircraft.getPowerPlant().setDryMassPublicDomainTotal(_engineReferenceMass);
			}
		if(aircraft.getNacelles() != null)
			if(this._nacelleReferenceMass == null) {
				this._nacelleReferenceMass = _maximumZeroFuelMass.times(.015);
				aircraft.getNacelles().setMassReference(_nacelleReferenceMass);
			}
		if(aircraft.getFuelTank() != null)
			if(this._fuelTankReferenceMass == null) {
				this._fuelTankReferenceMass = _maximumZeroFuelMass.times(.015);
				aircraft.getFuelTank().setMassReference(_fuelTankReferenceMass);
			}
		if(aircraft.getLandingGears() != null) 
			if(this._landingGearsReferenceMass == null) {
				this._landingGearsReferenceMass = _maximumZeroFuelMass.times(.04);
				aircraft.getLandingGears().setReferenceMass(_landingGearsReferenceMass);
			}
		if(aircraft.getSystems() != null)
			if(this._systemsReferenceMass == null) {
				_systemsReferenceMass = _maximumZeroFuelMass.times(.04);
				aircraft.getSystems().setReferenceMass(_systemsReferenceMass);
			}
		
	}
	
	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
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

	public Amount<Mass> getFuelMass() {
		return _fuelMass;
	}

	public void setFuelMass(Amount<Mass> _fuelMass) {
		this._fuelMass = _fuelMass;
	}

	public Amount<Mass> getPaxSingleMass() {
		return _paxSingleMass;
	}

	public void setPaxSingleMass(Amount<Mass> _paxSingleMass) {
		this._paxSingleMass = _paxSingleMass;
	}

	public Double getCruiseSFC() {
		return _cruiseSFC;
	}

	public void setCruiseSFC(Double _cruiseSFC) {
		this._cruiseSFC = _cruiseSFC;
	}

	public Amount<Length> getCruiseRange() {
		return _cruiseRange;
	}

	public void setCruiseRange(Amount<Length> _cruiseRange) {
		this._cruiseRange = _cruiseRange;
	}

	public Amount<Length> getAlternateRange() {
		return _alternateRange;
	}

	public void setAlternateRange(Amount<Length> _alternateRange) {
		this._alternateRange = _alternateRange;
	}

	public Double getAlternateMach() {
		return _alternateMach;
	}

	public void setAlternateMach(Double _alternateMach) {
		this._alternateMach = _alternateMach;
	}

	public Amount<Length> getAlternateAltitude() {
		return _alternateAltitude;
	}

	public void setAlternateAltitude(Amount<Length> _alternateAltitude) {
		this._alternateAltitude = _alternateAltitude;
	}

	public Double getAlternateSFC() {
		return _alternateSFC;
	}

	public void setAlternateSFC(Double _alternateSFC) {
		this._alternateSFC = _alternateSFC;
	}

	public Amount<Duration> getHoldingDuration() {
		return _holdingDuration;
	}

	public void setHoldingDuration(Amount<Duration> _holdingDuration) {
		this._holdingDuration = _holdingDuration;
	}

	public Amount<Length> getHoldingAltitude() {
		return _holdingAltitude;
	}

	public void setHoldingAltitude(Amount<Length> _holdingAltitude) {
		this._holdingAltitude = _holdingAltitude;
	}

	public Double getHoldingMach() {
		return _holdingMach;
	}

	public void setHoldingMach(Double _holdingMach) {
		this._holdingMach = _holdingMach;
	}

	public Double getHoldingSFC() {
		return _holdingSFC;
	}

	public void setHoldingSFC(Double _holdingSFC) {
		this._holdingSFC = _holdingSFC;
	}

	public static OperatingConditions getTheOperatingConditions() {
		return _theOperatingConditions;
	}

	public static void setTheOperatingConditions(OperatingConditions _theOperatingConditions) {
		ACWeightsManager._theOperatingConditions = _theOperatingConditions;
	}

	public FuelFractionDatabaseReader getFuelFractionDatabaseReader() {
		return _fuelFractionDatabaseReader;
	}

	public void setFuelFractionDatabaseReader(FuelFractionDatabaseReader _fuelFractionDatabaseReader) {
		this._fuelFractionDatabaseReader = _fuelFractionDatabaseReader;
	}

	public Double getCruiseEfficiency() {
		return _cruiseEfficiency;
	}

	public void setCruiseEfficiency(Double _cruiseEfficiency) {
		this._cruiseEfficiency = _cruiseEfficiency;
	}

	public Double getAlternateEfficiency() {
		return _alternateEfficiency;
	}

	public void setAlternateEfficiency(Double _alternateEfficiency) {
		this._alternateEfficiency = _alternateEfficiency;
	}

	public Double getHoldingEfficiency() {
		return _holdingEfficiency;
	}

	public void setHoldingEfficiency(Double _holdingEfficiency) {
		this._holdingEfficiency = _holdingEfficiency;
	}

	public Amount<Force> getPaxWeight() {
		return _paxWeight;
	}

	public void setPaxWeight(Amount<Force> _paxWeight) {
		this._paxWeight = _paxWeight;
	}

	public Amount<Force> getPaxWeightMax() {
		return _paxWeightMax;
	}

	public void setPaxWeightMax(Amount<Force> _paxWeightMax) {
		this._paxWeightMax = _paxWeightMax;
	}

	public Amount<Force> getCreWeight() {
		return _creWeight;
	}

	public void setCreWeight(Amount<Force> _creWeight) {
		this._creWeight = _creWeight;
	}

	public Amount<Force> getStructuralWeight() {
		return _structuralWeight;
	}

	public void setStructuralWeight(Amount<Force> _structuralWeight) {
		this._structuralWeight = _structuralWeight;
	}

	public Amount<Force> getFuelWeight() {
		return _fuelWeight;
	}

	public void setFuelWeight(Amount<Force> _fuelWeight) {
		this._fuelWeight = _fuelWeight;
	}

	public static int getMaxIteration() {
		return _maxIteration;
	}

	public static void setMaxIteration(int _maxIteration) {
		ACWeightsManager._maxIteration = _maxIteration;
	}

}
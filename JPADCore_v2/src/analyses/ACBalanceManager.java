package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
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

import aircraft.Aircraft;
import calculators.balance.InertiaContributionsCalc;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyUnits;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.CenterOfGravity;

/**
 * Manage the calculations for estimating the aircraft balance.
 * 
 * @author Vittorio Trifari
 *
 */
public class ACBalanceManager {

	//------------------------------------------------------------------------------
	// VARIABLES DECLARATION:
	//------------------------------------------------------------------------------
	// INPUT DATA (IMPORTED AND CALCULATED)
	private IACBalanceManager _theBalanceManagerInterface;
	private Amount<Mass> _maximumTakeOffMass;
	private Amount<Mass> _payloadMass;
	private Amount<Mass> _maximumZeroFuelMass;

	//---------------------------------------------------------------------------------
	// OUTPUT DATA : 
	private CenterOfGravity _cgStructure;
	private CenterOfGravity _cgStructureAndPower;
	private CenterOfGravity _cgManufacturerEmptuMass;
	private CenterOfGravity _cgOperatingEmptyMass;
	private CenterOfGravity _cgMaximumZeroFuelMass;
	private CenterOfGravity _cgMaximumTakeOffMass;
	private Map<ComponentEnum, Amount<Length>> _xCGMap;
	private Map<ComponentEnum, Amount<Length>> _zCGMap;
	private Map<ComponentEnum, Amount<Mass>> _massMap;
	private Double _maxForwardOperativeCG;
	private Double _maxAftCG;
	private Double _maxForwardCG;
	
	private Amount<?> _aircraftInertiaMomentIxx;
	private Amount<?> _aircraftInertiaMomentIyy;
	private Amount<?> _aircraftInertiaMomentIzz;
	
	private Amount<?> _aircraftInertiaProductIxy;
	private Amount<?> _aircraftInertiaProductIyz;
	private Amount<?> _aircraftInertiaProductIxz;

	//------------------------------------------------------------------------------
	// METHODS:
	//------------------------------------------------------------------------------
	/**
	 * ImportFromXML is the only way to create a ACBalanceManger object. In this way the possibility
	 * to create an object with null input data is avoided.
	 * 
	 * @throws IOException 
	 */
	@SuppressWarnings({ "unchecked" })
	public static ACBalanceManager importFromXML (String pathToXML, Aircraft theAircraft) throws IOException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading balance analysis data ...");

		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");

		Boolean weightsFromPreviousAnalysisFlag;
		String weightsFromPreviousAnalysisString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@weights_from_previous_analysis");
		if(weightsFromPreviousAnalysisString.equalsIgnoreCase("true"))
			weightsFromPreviousAnalysisFlag = Boolean.TRUE;
		else {
			weightsFromPreviousAnalysisFlag = Boolean.FALSE;
			if(theAircraft.getTheAnalysisManager().getTheAnalysisManagerInterface().isIterativeLoop() == true) {
				System.err.println("WARNING (IMPORT BALANCE DATA): IF THE ITERATIVE LOOP FLAG IS 'TRUE', THE 'weights_from_previous_analysis' FLAG MUST BE TRUE. TERMINATING ...");
				System.exit(1);
			}
		}

		Amount<Mass> operatingEmptyMass = null;
		Amount<Mass> fuelMass = null;
		Amount<Mass> passengersSingleMass = null;
		Amount<Mass> fuselageMass = null;
		Amount<Mass> wingMass = null;
		Amount<Mass> horizontalTailMass = null;
		Amount<Mass> verticalTailMass = null;
		Amount<Mass> canardMass = null;
		Amount<Mass> nacellesMass = null;
		Amount<Mass> powerPlantMass = null;
		Amount<Mass> landingGearsMass = null;
		Amount<Mass> apuMass = null;
		Amount<Mass> airConditioningAndAntiIcingSystemMass = null;
		Amount<Mass> instrumentsAndNavigationSystemMass = null;
		Amount<Mass> hydraulicAndPneumaticSystemsMass = null;
		Amount<Mass> electricalSystemsMass = null;
		Amount<Mass> controlSurfacesMass = null;
		Amount<Mass> furnishingsAndEquipmentsMass = null;

		/********************************************************************************************
		 * If the boolean flag is true, the method reads from the xls file and ignores the assigned
		 * data inside the xlm file.
		 * Otherwise it ignores the xls file and reads the input data from the xml.
		 */
		if(weightsFromPreviousAnalysisFlag == Boolean.TRUE) {

			if(theAircraft.getTheAnalysisManager() != null) {
				if(theAircraft.getTheAnalysisManager().getTheWeights() != null) {

					//---------------------------------------------------------------
					// OPERATING EMPTY MASS
					operatingEmptyMass = theAircraft.getTheAnalysisManager().getTheWeights().getOperatingEmptyMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// FUEL MASS
					fuelMass = theAircraft.getTheAnalysisManager().getTheWeights().getFuelMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// PASSENGERS SINGLE MASS (From ACWeightsManager)
					passengersSingleMass = theAircraft.getTheAnalysisManager().getTheWeights().getTheWeightsManagerInterface().getSinglePassengerMass();

					//---------------------------------------------------------------
					// FUSELAGE MASS
					if(theAircraft.getFuselage() != null)
							fuselageMass = theAircraft.getTheAnalysisManager().getTheWeights().getFuselageMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// WING MASS
					if(theAircraft.getWing() != null)
							wingMass = theAircraft.getTheAnalysisManager().getTheWeights().getWingMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// HORIZONTAL TAIL MASS
					if(theAircraft.getHTail() != null)
							horizontalTailMass = theAircraft.getTheAnalysisManager().getTheWeights().getHTailMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// VERTICAL TAIL MASS
					if(theAircraft.getVTail() != null)
							verticalTailMass = theAircraft.getTheAnalysisManager().getTheWeights().getVTailMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// CANARD MASS
					if(theAircraft.getCanard() != null)
							canardMass = theAircraft.getTheAnalysisManager().getTheWeights().getCanardMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------1
					// NACELLES MASS
					if(theAircraft.getNacelles() != null)
						nacellesMass = theAircraft.getTheAnalysisManager().getTheWeights().getNacellesMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// ENGINES MASS
					if(theAircraft.getPowerPlant() != null)
						powerPlantMass = theAircraft.getTheAnalysisManager().getTheWeights().getPowerPlantMass().to(SI.KILOGRAM);

					//---------------------------------------------------------------
					// LANDING GEARS MASS
					if(theAircraft.getLandingGears() != null) {
							landingGearsMass = theAircraft.getTheAnalysisManager().getTheWeights().getLandingGearsMass().to(SI.KILOGRAM);
					}
					
					//---------------------------------------------------------------
					// SYSTEMS MASSES
					if(theAircraft.getSystems() != null) {
							apuMass = theAircraft.getTheAnalysisManager().getTheWeights().getAPUMass().to(SI.KILOGRAM);
							airConditioningAndAntiIcingSystemMass = theAircraft.getTheAnalysisManager().getTheWeights().getAirConditioningAndAntiIcingSystemMass().to(SI.KILOGRAM);
							instrumentsAndNavigationSystemMass = theAircraft.getTheAnalysisManager().getTheWeights().getInstrumentsAndNavigationSystemMass().to(SI.KILOGRAM);
							hydraulicAndPneumaticSystemsMass = theAircraft.getTheAnalysisManager().getTheWeights().getHydraulicAndPneumaticSystemMass().to(SI.KILOGRAM);
							electricalSystemsMass = theAircraft.getTheAnalysisManager().getTheWeights().getElectricalSystemsMass().to(SI.KILOGRAM);
							controlSurfacesMass = theAircraft.getTheAnalysisManager().getTheWeights().getControlSurfaceMass().to(SI.KILOGRAM);
							furnishingsAndEquipmentsMass = theAircraft.getTheAnalysisManager().getTheWeights().getFurnishingsAndEquipmentsMass().to(SI.KILOGRAM);
					}
					
				}
				else {
					System.err.println("WARNING!! THE WEIGHTS ANALYSIS HAS NOT BEEN CARRIED OUT ... TERMINATING");
					System.exit(1);
				}
			}
			else {
				System.err.println("WARNING!! THE ANALYSIS MANAGER DOES NOT EXIST ... TERMINATING");
				System.exit(1);
			}
		}
		else {

			//---------------------------------------------------------------
			// OPERATING EMPTY MASS
			String operatingEmptyMassProperty = reader.getXMLPropertyByPath("//weights/operating_empty_mass");
			if(operatingEmptyMassProperty != null)
				operatingEmptyMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/operating_empty_mass");

			//---------------------------------------------------------------
			// FUEL MASS
			String fuelMassProperty = reader.getXMLPropertyByPath("//weights/fuel_mass");
			if(fuelMassProperty != null)
				fuelMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/fuel_mass");

			//---------------------------------------------------------------
			// PASSENGERS SINGLE MASS
			String passengersSingleMassProperty = reader.getXMLPropertyByPath("//weights/passengers_single_mass");
			if(passengersSingleMassProperty != null)
				passengersSingleMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/passengers_single_mass");

			//---------------------------------------------------------------
			// FUSELAGE MASS
			String fuselageMassProperty = reader.getXMLPropertyByPath("//weights/fuselage_mass");
			if(fuselageMassProperty != null)
				fuselageMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/fuselage_mass");

			//---------------------------------------------------------------
			// WING MASS
			String wingMassProperty = reader.getXMLPropertyByPath("//weights/wing_mass");
			if(wingMassProperty != null)
				wingMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/wing_mass");

			//---------------------------------------------------------------
			// HORIZONTAL TAIL MASS
			String horizontalTailMassProperty = reader.getXMLPropertyByPath("//weights/horizontal_tail_mass");
			if(horizontalTailMassProperty != null)
				horizontalTailMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/horizontal_tail_mass");

			//---------------------------------------------------------------
			// VERTICAL TAIL MASS
			String verticalTailMassProperty = reader.getXMLPropertyByPath("//weights/vertical_tail_mass");
			if(verticalTailMassProperty != null)
				verticalTailMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/vertical_tail_mass");

			//---------------------------------------------------------------
			// CANARD MASS
			String canardMassProperty = reader.getXMLPropertyByPath("//weights/canard_mass");
			if(canardMassProperty != null)
				canardMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/canard_mass");

			//---------------------------------------------------------------
			// NACELLES MASS
			String nacellesMassProperty = reader.getXMLPropertyByPath("//weights/nacelles_mass");
			if(nacellesMassProperty != null)
				nacellesMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/nacelles_mass");

			//---------------------------------------------------------------
			// POWER PLANT MASS
			String powerPlantMassProperty = reader.getXMLPropertyByPath("//weights/power_plant_mass");
			if(powerPlantMassProperty != null)
				powerPlantMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/power_plant_mass");

			//---------------------------------------------------------------
			// LANDING GEARS MASS
			String landingGearsMassProperty = reader.getXMLPropertyByPath("//weights/landing_gears_mass");
			if(landingGearsMassProperty != null)
				landingGearsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/landing_gears_mass");
			
			//---------------------------------------------------------------
			// APU MASS
			String apuMassProperty = reader.getXMLPropertyByPath("//weights/APU_mass");
			if(apuMassProperty != null)
				apuMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/APU_mass");
			
			//---------------------------------------------------------------
			// AIR CONDITIONING AND ANTI-ICING SYSTEM MASS
			String airConditioningAndAntiIcingSystemMassProperty = reader.getXMLPropertyByPath("//weights/air_conditioning_and_anti_icing_system_mass");
			if(airConditioningAndAntiIcingSystemMassProperty != null)
				airConditioningAndAntiIcingSystemMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/air_conditioning_and_anti_icing_system_mass");
			
			//---------------------------------------------------------------
			// INSTRUMENTS AND NAVIGATION SYSTEM MASS
			String instrumentsAndNavigationSystemMassProperty = reader.getXMLPropertyByPath("//weights/instruments_and_navigation_system_mass");
			if(instrumentsAndNavigationSystemMassProperty != null)
				instrumentsAndNavigationSystemMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/instruments_and_navigation_system_mass");
			
			//---------------------------------------------------------------
			// HYDRAULIC AND PNEUMATIC SYSTEMS MASS
			String hydraulicAndPneumaticSystemsMassProperty = reader.getXMLPropertyByPath("//weights/hydraulic_and_pneumatic_systems_mass");
			if(hydraulicAndPneumaticSystemsMassProperty != null)
				hydraulicAndPneumaticSystemsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/hydraulic_and_pneumatic_systems_mass");
			
			//---------------------------------------------------------------
			// ELECTRICAL SYSTEMS MASS
			String electricalSystemsMassProperty = reader.getXMLPropertyByPath("//weights/electrical_systems_mass");
			if(electricalSystemsMassProperty != null)
				electricalSystemsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/electrical_systems_mass");
			
			//---------------------------------------------------------------
			// CONTROL SURFACES MASS
			String controlSurfacesMassProperty = reader.getXMLPropertyByPath("//weights/control_surfaces_mass");
			if(controlSurfacesMassProperty != null)
				controlSurfacesMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/control_surfaces_mass");
			
			//---------------------------------------------------------------
			// FURNISHINGS AND EQUIPMENTS MASS
			String furnishingsAndEquipmentsMassProperty = reader.getXMLPropertyByPath("//weights/furnishings_and_equipments_mass");
			if(furnishingsAndEquipmentsMassProperty != null)
				furnishingsAndEquipmentsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/furnishings_and_equipments_mass");
			
		}

		boolean standardSystemsPositionFlag = true;
		boolean includeSystemsPositionFlag = true;
		Amount<Length> apuPositionX = null;
		Amount<Length> apuPositionZ = null;
		Amount<Length> airConditioningAndAntiIcingSystemPositionX = null;
		Amount<Length> airConditioningAndAntiIcingSystemPositionZ = null;
		Amount<Length> instrumentsAndNavigationSystemPositionX = null;
		Amount<Length> instrumentsAndNavigationSystemPositionZ = null;
		Amount<Length> hydraulicAndPneumaticSystemsPositionX = null;
		Amount<Length> hydraulicAndPneumaticSystemsPositionZ = null;
		Amount<Length> electricalSystemsPositionX = null;
		Amount<Length> electricalSystemsPositionZ = null;
		Amount<Length> controlSurfacesPositionX = null;
		Amount<Length> controlSurfacesPositionZ = null;
		Amount<Length> furnishingsAndEquipmentsPositionX = null;
		Amount<Length> furnishingsAndEquipmentsPositionZ = null;
		
		//---------------------------------------------------------------
		// INCLUDE SYSTEMS POSITION FLAG
		String includeSystemsPositionFlagString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@include_systems");
		if(includeSystemsPositionFlagString.equalsIgnoreCase("true"))
			includeSystemsPositionFlag = Boolean.TRUE;
		else
			includeSystemsPositionFlag = Boolean.FALSE;

		if(includeSystemsPositionFlag == true) {

			//---------------------------------------------------------------
			// STANDARD SYSTEMS POSITION FLAG
			String standardSystemsPositionFlagString = MyXMLReaderUtils
					.getXMLPropertyByPath(
							reader.getXmlDoc(), reader.getXpath(),
							"//@use_standard_positions");
			if(standardSystemsPositionFlagString.equalsIgnoreCase("true"))
				standardSystemsPositionFlag = Boolean.TRUE;
			else
				standardSystemsPositionFlag = Boolean.FALSE;

			/*
			 * If the "standardSystemsPositionFlag" is true, standard systems positions will be assigned 
			 * according to Sforza-Aircraft Design suggestions (pag.343, Fig. 8.38 and 8.39) 
			 */
			if(standardSystemsPositionFlag == true) {

				if(theAircraft.getFuselage() != null) {
					apuPositionX = theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER))
							.plus(theAircraft.getFuselage().getCylinderLength().to(SI.METER))
							.plus(theAircraft.getFuselage().getTailLength().to(SI.METER).times(0.25)
									);
					apuPositionZ = theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER);
				}

				if(theAircraft.getWing() != null) {
					airConditioningAndAntiIcingSystemPositionX = theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.25));
					airConditioningAndAntiIcingSystemPositionZ = theAircraft.getWing().getZApexConstructionAxes().to(SI.METER); 
				}

				if(theAircraft.getFuselage() != null) {
					instrumentsAndNavigationSystemPositionX = theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER).times(0.5));
					instrumentsAndNavigationSystemPositionZ = theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER);
				}

				if(theAircraft.getWing() != null && theAircraft.getHTail() != null && theAircraft.getVTail() != null && theAircraft.getCanard() != null) {
					hydraulicAndPneumaticSystemsPositionX = (
							( (theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getHTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getVTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getVTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(verticalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getCanard().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getCanard().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(canardMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									+ canardMass.doubleValue(SI.KILOGRAM)
									);
					hydraulicAndPneumaticSystemsPositionZ = (
							( (theAircraft.getWing().getZApexConstructionAxes().to(SI.METER) )
									.times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getZApexConstructionAxes().to(SI.METER) )
									.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getVTail().getZApexConstructionAxes().to(SI.METER) )
									.times(verticalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getCanard().getZApexConstructionAxes().to(SI.METER) )
									.times(canardMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									+ canardMass.doubleValue(SI.KILOGRAM)
									);
				}
				else if(theAircraft.getWing() != null && theAircraft.getHTail() != null && theAircraft.getVTail() != null && theAircraft.getCanard() == null) {
					hydraulicAndPneumaticSystemsPositionX = (
							( (theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getHTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getVTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getVTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(verticalTailMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									);
					hydraulicAndPneumaticSystemsPositionZ = (
							( (theAircraft.getWing().getZApexConstructionAxes().to(SI.METER) )
									.times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getZApexConstructionAxes().to(SI.METER) )
									.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getVTail().getZApexConstructionAxes().to(SI.METER) )
									.times(verticalTailMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									);
				}
				else if(theAircraft.getWing() != null && theAircraft.getHTail() != null && theAircraft.getVTail() == null && theAircraft.getCanard() == null) {
					hydraulicAndPneumaticSystemsPositionX = (
							( (theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getHTail().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
									).times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									);
					hydraulicAndPneumaticSystemsPositionZ = (
							( (theAircraft.getWing().getZApexConstructionAxes().to(SI.METER) )
									.times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getZApexConstructionAxes().to(SI.METER) )
									.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									);
				}
				else if(theAircraft.getWing() != null && theAircraft.getHTail() == null && theAircraft.getVTail() == null && theAircraft.getCanard() == null) {
					hydraulicAndPneumaticSystemsPositionX = theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5));
					hydraulicAndPneumaticSystemsPositionZ = theAircraft.getWing().getZApexConstructionAxes().to(SI.METER);
				}
				else if (theAircraft.getFuselage() != null) {
					hydraulicAndPneumaticSystemsPositionX = theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER))
							.plus(theAircraft.getFuselage().getCylinderLength().to(SI.METER).times(0.5));
					hydraulicAndPneumaticSystemsPositionZ = theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER); 
				}

				if(theAircraft.getFuselage() != null && theAircraft.getWing() != null) {
					electricalSystemsPositionX = (
							(theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER))
									)
							.plus(theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.25))
									)
							).divide(2);
					electricalSystemsPositionZ = ( 
							(theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER)
									.minus(theAircraft.getFuselage().getSectionCylinderHeight().to(SI.METER).times(0.25))
									)
							.plus(theAircraft.getWing().getZApexConstructionAxes().to(SI.METER))
							).divide(2); 
				}
				else if(theAircraft.getFuselage() != null && theAircraft.getWing() == null) {
					electricalSystemsPositionX = 
							theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER));
					electricalSystemsPositionZ = 
							theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER)
							.minus(theAircraft.getFuselage().getSectionCylinderHeight().to(SI.METER).times(0.25));
				}
				else if(theAircraft.getFuselage() == null && theAircraft.getWing() != null) {
					electricalSystemsPositionX = 
							theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getWing().getPanels().get(0).getChordRoot().to(SI.METER).times(0.25));
					electricalSystemsPositionZ = theAircraft.getWing().getZApexConstructionAxes().to(SI.METER);
				} 

				if(theAircraft.getWing() != null && theAircraft.getHTail() != null && theAircraft.getVTail() != null && theAircraft.getCanard() != null) {
					controlSurfacesPositionX = (
							( (theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getWing().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									).times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( (theAircraft.getHTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getHTail().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getHTail().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									) )
							.plus( (theAircraft.getVTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getVTail().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getVTail().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									) )
							.plus( (theAircraft.getCanard().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getCanard().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getCanard().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									+ canardMass.doubleValue(SI.KILOGRAM)
									);
					controlSurfacesPositionZ = (
							( (theAircraft.getWing().getZApexConstructionAxes().to(SI.METER) )
									.times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getZApexConstructionAxes().to(SI.METER) )
									.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getVTail().getXApexConstructionAxes().to(SI.METER) )
									.times(verticalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getCanard().getXApexConstructionAxes().to(SI.METER) )
									.times(canardMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									+ canardMass.doubleValue(SI.KILOGRAM)
									);
				}
				else if(theAircraft.getWing() != null && theAircraft.getHTail() != null && theAircraft.getVTail() != null && theAircraft.getCanard() == null) {
					controlSurfacesPositionX = (
							( (theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getWing().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									).times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( (theAircraft.getHTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getHTail().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getHTail().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									) )
							.plus( (theAircraft.getVTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getVTail().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getVTail().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									);
					controlSurfacesPositionZ = (
							( (theAircraft.getWing().getZApexConstructionAxes().to(SI.METER) )
									.times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getZApexConstructionAxes().to(SI.METER) )
									.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							.plus( ( (theAircraft.getVTail().getXApexConstructionAxes().to(SI.METER) )
									.times(verticalTailMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									+ verticalTailMass.doubleValue(SI.KILOGRAM)
									);
				}
				else if(theAircraft.getWing() != null && theAircraft.getHTail() != null && theAircraft.getVTail() == null && theAircraft.getCanard() == null) {
					controlSurfacesPositionX = (
							( (theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getWing().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									).times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( (theAircraft.getHTail().getXApexConstructionAxes().to(SI.METER)
									.plus(theAircraft.getHTail().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
									.plus(theAircraft.getHTail().getMeanAerodynamicChord().to(SI.METER).times(0.7))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									);
					controlSurfacesPositionZ = (
							( (theAircraft.getWing().getZApexConstructionAxes().to(SI.METER) )
									.times(wingMass.doubleValue(SI.KILOGRAM))
									)
							.plus( ( (theAircraft.getHTail().getZApexConstructionAxes().to(SI.METER) )
									.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
									) )
							).divide(
									wingMass.doubleValue(SI.KILOGRAM)
									+ horizontalTailMass.doubleValue(SI.KILOGRAM)
									);
				}
				else if(theAircraft.getWing() != null && theAircraft.getHTail() == null && theAircraft.getVTail() == null && theAircraft.getCanard() == null) {
					controlSurfacesPositionX = theAircraft.getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
							.plus(theAircraft.getWing().getMeanAerodynamicChord().to(SI.METER).times(0.7));
					controlSurfacesPositionZ = theAircraft.getWing().getZApexConstructionAxes().to(SI.METER);
				}
				else if (theAircraft.getFuselage() != null) {
					controlSurfacesPositionX = theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER))
							.plus(theAircraft.getFuselage().getCylinderLength().to(SI.METER).times(0.5));
					controlSurfacesPositionZ = theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER); 
				}

				if (theAircraft.getFuselage() != null) {
					furnishingsAndEquipmentsPositionX = theAircraft.getFuselage().getXApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getNoseLength().to(SI.METER))
							.plus(theAircraft.getFuselage().getCylinderLength().to(SI.METER).times(0.5));
					furnishingsAndEquipmentsPositionZ = theAircraft.getFuselage().getZApexConstructionAxes().to(SI.METER)
							.plus(theAircraft.getFuselage().getSectionCylinderHeight().to(SI.METER).times(0.25));
				}

			}
			else {

				//---------------------------------------------------------------
				// APU POSITIONS
				String apuPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/APU/x");
				String apuPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/APU/z");
				if(apuPositionXProperty != null)
					apuPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/APU/x");
				if(apuPositionZProperty != null)
					apuPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/APU/z");

				//---------------------------------------------------------------
				// AIR CONDITIONING AND ANTI-ICING SYSTEM POSITIONS
				String airConditioningAndAntiIcingSystemPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/air_conditioning_and_anti_icing_system/x");
				String airConditioningAndAntiIcingSystemPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/air_conditioning_and_anti_icing_system/z");
				if(airConditioningAndAntiIcingSystemPositionXProperty != null)
					airConditioningAndAntiIcingSystemPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/air_conditioning_and_anti_icing_system/x");
				if(airConditioningAndAntiIcingSystemPositionZProperty != null)
					airConditioningAndAntiIcingSystemPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/air_conditioning_and_anti_icing_system/z");

				//---------------------------------------------------------------
				// INSTRUMENTS AND NAVIGATION SYSTEMS POSITIONS
				String instrumentsAndNavigationSystemPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/instruments_and_navigation_system/x");
				String instrumentsAndNavigationSystemPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/instruments_and_navigation_system/z");
				if(instrumentsAndNavigationSystemPositionXProperty != null)
					instrumentsAndNavigationSystemPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/instruments_and_navigation_system/x");
				if(instrumentsAndNavigationSystemPositionZProperty != null)
					instrumentsAndNavigationSystemPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/instruments_and_navigation_system/z");

				//---------------------------------------------------------------
				// HYDRAULIC AND PNEUMATIC SYSTEMS POSITIONS
				String hydraulicAndPneumaticSystemsPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/hydraulic_and_pneumatic_systems/x");
				String hydraulicAndPneumaticSystemsPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/hydraulic_and_pneumatic_systems/z");
				if(hydraulicAndPneumaticSystemsPositionXProperty != null)
					hydraulicAndPneumaticSystemsPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/hydraulic_and_pneumatic_systems/x");
				if(hydraulicAndPneumaticSystemsPositionZProperty != null)
					hydraulicAndPneumaticSystemsPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/hydraulic_and_pneumatic_systems/z");

				//---------------------------------------------------------------
				// ELECTRICAL SYSTEMS POSITIONS
				String electricalSystemsPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/electrical_systems/x");
				String electricalSystemsPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/electrical_systems/z");
				if(electricalSystemsPositionXProperty != null)
					electricalSystemsPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/electrical_systems/x");
				if(electricalSystemsPositionZProperty != null)
					electricalSystemsPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/electrical_systems/z");

				//---------------------------------------------------------------
				// CONTROL SURFACES POSITIONS
				String controlSurfacesPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/control_surfaces/x");
				String controlSurfacesPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/control_surfaces/z");
				if(controlSurfacesPositionXProperty != null)
					controlSurfacesPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/control_surfaces/x");
				if(controlSurfacesPositionZProperty != null)
					controlSurfacesPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/control_surfaces/z");

				//---------------------------------------------------------------
				// FURNISHINGS AND EQUIPMENTS POSITIONS
				String furnishingsAndEquipmentsPositionXProperty = reader.getXMLPropertyByPath("//systems_positions/furnishings_and_equipments/x");
				String furnishingsAndEquipmentsPositionZProperty = reader.getXMLPropertyByPath("//systems_positions/furnishings_and_equipments/z");
				if(furnishingsAndEquipmentsPositionXProperty != null)
					furnishingsAndEquipmentsPositionX = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/furnishings_and_equipments/x");
				if(furnishingsAndEquipmentsPositionZProperty != null)
					furnishingsAndEquipmentsPositionZ = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//systems_positions/furnishings_and_equipments/z");

			}

		}
		
		/********************************************************************************************
		 * Once the data are ready, it's possible to create the ACBalanceManager object can be created
		 * using the builder pattern.
		 */
		IACBalanceManager theBalanceManagerInterface = new IACBalanceManager.Builder()
				.setId(id)
				.setTheAircraft(theAircraft)
				.setOperatingEmptyMass(operatingEmptyMass)
				.setDesignFuelMass(fuelMass)
				.setSinglePassengerMass(passengersSingleMass)
				.setFuselageMass(fuselageMass)
				.setWingMass(wingMass)
				.setHTailMass(horizontalTailMass)
				.setVTailMass(verticalTailMass)
				.setCanardMass(canardMass)
				.setNacellesMass(nacellesMass)
				.setPowerPlantMass(powerPlantMass)
				.setLandingGearMass(landingGearsMass)
				.setAPUMass(apuMass)
				.setAirConditioningAndAntiIcingMass(airConditioningAndAntiIcingSystemMass)
				.setInstrumentsAndNavigationSystemMass(instrumentsAndNavigationSystemMass)
				.setHydraulicAndPneumaticSystemsMass(hydraulicAndPneumaticSystemsMass)
				.setElectricalSystemsMass(electricalSystemsMass)
				.setControlSurfacesMass(controlSurfacesMass)
				.setFurnishingsAndEquipmentsMass(furnishingsAndEquipmentsMass)
				.setStandardSystemsPositionFlag(standardSystemsPositionFlag)
				.setIncludeSystemsPosition(includeSystemsPositionFlag)
				.setAPUPositionX(apuPositionX)
				.setAPUPositionZ(apuPositionZ)
				.setAirConditioningAndAntiIcingSystemPositionX(airConditioningAndAntiIcingSystemPositionX)
				.setAirConditioningAndAntiIcingSystemPositionZ(airConditioningAndAntiIcingSystemPositionZ)
				.setInstrumentsAndNavigationSystemPositionX(instrumentsAndNavigationSystemPositionX)
				.setInstrumentsAndNavigationSystemPositionZ(instrumentsAndNavigationSystemPositionZ)
				.setHydraulicAndPneumaticSystemsPositionX(hydraulicAndPneumaticSystemsPositionX)
				.setHydraulicAndPneumaticSystemsPositionZ(hydraulicAndPneumaticSystemsPositionZ)
				.setElectricalSystemsPositionX(electricalSystemsPositionX)
				.setElectricalSystemsPositionZ(electricalSystemsPositionZ)
				.setControlSurfacesPositionX(controlSurfacesPositionX)
				.setControlSurfacesPositionZ(controlSurfacesPositionZ)
				.setFurnishingsAndEquipmentsPositionX(furnishingsAndEquipmentsPositionX)
				.setFurnishingsAndEquipmentsPositionZ(furnishingsAndEquipmentsPositionZ)
				.build();

		ACBalanceManager theBalanceManager = new ACBalanceManager();
		theBalanceManager.setTheBalanceManagerInterface(theBalanceManagerInterface);
		
		return theBalanceManager;
	}
		
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\n\t-------------------------------------\n")
				.append("\tBalance Analysis\n")
				.append("\t-------------------------------------\n")
				.append("\tXcg Structure MAC: " + _cgStructure.getXMAC()*100 + "\n")
				.append("\tXcg Structure BRF: " + _cgStructure.getXBRF().to(SI.METER) + "\n")
				.append("\tYcg Structure MAC: " + _cgStructure.getYMAC()*100 + "\n")
				.append("\tYcg Structure BRF: " + _cgStructure.getYBRF().to(SI.METER) + "\n")
				.append("\tZcg Structure MAC: " + _cgStructure.getZMAC()*100 + "\n")
				.append("\tZcg Structure BRF: " + _cgStructure.getZBRF().to(SI.METER) + "\n")
				.append("\t·····································\n")
				.append("\tXcg Structure and engines MAC: " + _cgStructureAndPower.getXMAC()*100 + "\n")
				.append("\tXcg Structure and engines BRF: " + _cgStructureAndPower.getXBRF().to(SI.METER) + "\n")
				.append("\tYcg Structure and engines MAC: " + _cgStructureAndPower.getYMAC()*100 + "\n")
				.append("\tYcg Structure and engines BRF: " + _cgStructureAndPower.getYBRF().to(SI.METER) + "\n")
				.append("\tZcg Structure and engines MAC: " + _cgStructureAndPower.getZMAC()*100 + "\n")
				.append("\tZcg Structure and engines BRF: " + _cgStructureAndPower.getZBRF().to(SI.METER) + "\n")
				.append("\t·····································\n")
				.append("\tXcg Manufacturer Empty Mass MAC: " + _cgManufacturerEmptuMass.getXMAC()*100 + "\n")
				.append("\tXcg Manufacturer Empty Mass BRF: " + _cgManufacturerEmptuMass.getXBRF().to(SI.METER) + "\n")
				.append("\tYcg Manufacturer Empty Mass MAC: " + _cgManufacturerEmptuMass.getYMAC()*100 + "\n")
				.append("\tYcg Manufacturer Empty Mass BRF: " + _cgManufacturerEmptuMass.getYBRF().to(SI.METER) + "\n")			
				.append("\tZcg Manufacturer Empty Mass MAC: " + _cgManufacturerEmptuMass.getZMAC()*100 + "\n")
				.append("\tZcg Manufacturer Empty Mass BRF: " + _cgManufacturerEmptuMass.getZBRF().to(SI.METER) + "\n")
				.append("\t·····································\n")
				.append("\tXcg Operating empty mass MAC: " + _cgOperatingEmptyMass.getXMAC()*100 + "\n")
				.append("\tXcg Operating empty mass BRF: " + _cgOperatingEmptyMass.getXBRF().to(SI.METER) + "\n")
				.append("\tYcg Operating empty mass MAC: " + _cgOperatingEmptyMass.getYMAC()*100 + "\n")
				.append("\tYcg Operating empty mass BRF: " + _cgOperatingEmptyMass.getYBRF().to(SI.METER) + "\n")
				.append("\tZcg Operating empty mass MAC: " + _cgOperatingEmptyMass.getZMAC()*100 + "\n")
				.append("\tZcg Operating empty mass BRF: " + _cgOperatingEmptyMass.getZBRF().to(SI.METER) + "\n")
				.append("\t·····································\n")
				.append("\tXcg Maximum zero fuel mass MAC: " + _cgMaximumZeroFuelMass.getXMAC()*100 + "\n")
				.append("\tXcg Maximum zero fuel mass BRF: " + _cgMaximumZeroFuelMass.getXBRF().to(SI.METER) + "\n")
				.append("\tYcg Maximum zero fuel mass MAC: " + _cgMaximumZeroFuelMass.getYMAC()*100 + "\n")
				.append("\tYcg Maximum zero fuel mass BRF: " + _cgMaximumZeroFuelMass.getYBRF().to(SI.METER) + "\n")
				.append("\tZcg Maximum zero fuel mass MAC: " + _cgMaximumZeroFuelMass.getZMAC()*100 + "\n")
				.append("\tZcg Maximum zero fuel mass BRF: " + _cgMaximumZeroFuelMass.getZBRF().to(SI.METER) + "\n")
				.append("\t·····································\n")
				.append("\tXcg Maximum take-off mass MAC: " + _cgMaximumTakeOffMass.getXMAC()*100 + "\n")
				.append("\tXcg Maximum take-off mass BRF: " + _cgMaximumTakeOffMass.getXBRF().to(SI.METER) + "\n")
				.append("\tYcg Maximum take-off mass MAC: " + _cgMaximumTakeOffMass.getYMAC()*100 + "\n")
				.append("\tYcg Maximum take-off mass BRF: " + _cgMaximumTakeOffMass.getYBRF().to(SI.METER) + "\n")
				.append("\tZcg Maximum take-off mass MAC: " + _cgMaximumTakeOffMass.getZMAC()*100 + "\n")
				.append("\tZcg Maximum take-off mass BRF: " + _cgMaximumTakeOffMass.getZBRF().to(SI.METER) + "\n")
				.append("\t·····································\n")
				.append("\tMax aft Xcg MAC: " + (_maxAftCG*100) + " %\n")
				.append("\tMax forward Xcg MAC: " + (_maxForwardCG*100) + " %\n")
				.append("\tOperative max forward Xcg MAC: " + (_maxForwardOperativeCG*100) + " %\n")
				.append("\t-------------------------------------\n")
				.append("\tCOMPONENTS:\n")
				.append("\t-------------------------------------\n");
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			sb.append("\t\tXcg Fuselage MAC: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Fuselage BRF: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Fuselage MAC: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Fuselage BRF: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) {
			sb.append("\t\tXcg Wing MAC: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Wing BRF: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Wing MAC: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Wing BRF: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) {
			sb.append("\t\tXcg Horizontal Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Horizontal Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Horizontal Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Horizontal Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) {
			sb.append("\t\tXcg Vertical Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Vertical Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Vertical Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Vertical Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) {
			sb.append("\t\tXcg Canard MAC: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Canard BRF: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Canard MAC: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Canard BRF: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) {
			sb.append("\t\tXcg Nacelles MAC: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Nacelles BRF: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Nacelles MAC: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Nacelles BRF: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant() != null) {
			sb.append("\t\tXcg Power Plant MAC: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Power Plant BRF: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Power Plant MAC: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Power Plant BRF: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) {
			sb.append("\t\tXcg Total Landing Gear MAC: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Total Landing Gear BRF: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Total Landing Gear MAC: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Total Landing Gear BRF: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\t·····································\n");
		}
		if(_theBalanceManagerInterface.getIncludeSystemsPosition() == true) {
			if(_theBalanceManagerInterface.getTheAircraft().getSystems() != null) {
				sb.append("\t\tSYSTEMS AND EQUIPMENTS:\n")
				.append("\t\t·····································\n")
				.append("\t\t\tXcg APU BRF: " + _theBalanceManagerInterface.getAPUPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg APU MAC: " + _theBalanceManagerInterface.getAPUPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg APU BRF: " + _theBalanceManagerInterface.getAPUPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg APU MAC: " + _theBalanceManagerInterface.getAPUPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Air Conditioning And Anti-Icing System BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Air Conditioning And Anti-Icing System MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg APU BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg APU MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Air Conditioning And Anti-Icing System BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Air Conditioning And Anti-Icing System MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg Air Conditioning And Anti-Icing System BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg Air Conditioning And Anti-Icing System MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Instruments And Navigation System BRF: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Instruments And Navigation System MAC: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg Instruments And Navigation System BRF: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg Instruments And Navigation System MAC: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Hydraulic And Pneumatic Systems BRF: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Hydraulic And Pneumatic Systems MAC: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg Hydraulic And Pneumatic Systems BRF: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg Hydraulic And Pneumatic Systems MAC: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Electrical Systems BRF: " + _theBalanceManagerInterface.getElectricalSystemsPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Electrical Systems MAC: " + _theBalanceManagerInterface.getElectricalSystemsPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg Electrical Systems BRF: " + _theBalanceManagerInterface.getElectricalSystemsPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg Electrical Systems MAC: " + _theBalanceManagerInterface.getElectricalSystemsPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Control Surfaces BRF: " + _theBalanceManagerInterface.getControlSurfacesPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Control Surfaces MAC: " + _theBalanceManagerInterface.getControlSurfacesPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg Control Surfaces BRF: " + _theBalanceManagerInterface.getControlSurfacesPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg Control Surfaces MAC: " + _theBalanceManagerInterface.getControlSurfacesPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n")
				.append("\t\t\tXcg Furnishings And Equipments BRF: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionX().to(SI.METER) + "\n")
				.append("\t\t\tXcg Furnishings And Equipments MAC: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionX().to(SI.METER)
						.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
								.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\tZcg Furnishings And Equipments BRF: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionZ().to(SI.METER) + "\n")
				.append("\t\t\tZcg Furnishings And Equipments MAC: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionZ().to(SI.METER)
						.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
				.append("\t\t\t·····································\n");
			}
		}
						
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
		dataListGlobal.add(new Object[] {"STRUCTURAL MASS CG"});
		dataListGlobal.add(new Object[] {"Xcg Structure BRF","m", _cgStructure.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Ycg Structure BRF","m", _cgStructure.getYBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg Structure BRF","m", _cgStructure.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg Structure MAC","%", _cgStructure.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Ycg Structure MAC","%", _cgStructure.getYMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg Structure MAC","%", _cgStructure.getZMAC()*100});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"STRUCTURAL MASS PLUS POWER PLANT CG"});
		dataListGlobal.add(new Object[] {"Xcg Structure and engines BRF","m", _cgStructureAndPower.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Ycg Structure and engines BRF","m", _cgStructureAndPower.getYBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg Structure and engines BRF","m", _cgStructureAndPower.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg Structure and engines MAC","%", _cgStructureAndPower.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Ycg Structure and engines MAC","%", _cgStructureAndPower.getYMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg Structure and engines MAC","%", _cgStructureAndPower.getZMAC()*100});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"MANUFACTURER EMPTY MASS CG"});
		dataListGlobal.add(new Object[] {"Xcg Manufacturer empty mass BRF","m", _cgManufacturerEmptuMass.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Ycg Manufacturer empty mass BRF","m", _cgManufacturerEmptuMass.getYBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg Manufacturer empty mass BRF","m", _cgManufacturerEmptuMass.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg Manufacturer empty mass MAC","%", _cgManufacturerEmptuMass.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Ycg Manufacturer empty mass MAC","%", _cgManufacturerEmptuMass.getYMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg Manufacturer empty mass MAC","%", _cgManufacturerEmptuMass.getZMAC()*100});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"OPERATING EMPTY MASS CG"});
		dataListGlobal.add(new Object[] {"Xcg Operating empty mass BRF","m", _cgOperatingEmptyMass.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Ycg Operating empty mass BRF","m", _cgOperatingEmptyMass.getYBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg Operating empty mass BRF","m", _cgOperatingEmptyMass.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg Operating empty mass MAC","%", _cgOperatingEmptyMass.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Ycg Operating empty mass MAC","%", _cgOperatingEmptyMass.getYMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg Operating empty mass MAC","%", _cgOperatingEmptyMass.getZMAC()*100});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"MAXIMUM ZERO-FUEL MASS CG"});
		dataListGlobal.add(new Object[] {"Xcg Maximum zero fuel mass BRF","m",_cgMaximumZeroFuelMass.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Ycg Maximum zero fuel mass BRF","m",_cgMaximumZeroFuelMass.getYBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg Maximum zero fuel mass BRF","m", _cgMaximumZeroFuelMass.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg Maximum zero fuel mass MAC","%",_cgMaximumZeroFuelMass.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Ycg Maximum zero fuel mass MAC","%",_cgMaximumZeroFuelMass.getYMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg Maximum zero fuel mass MAC","%",_cgMaximumZeroFuelMass.getZMAC()*100});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"MAXIMUM TAKE-OFF MASS CG"});
		dataListGlobal.add(new Object[] {"Xcg Maximum take-off mass BRF","m",_cgMaximumTakeOffMass.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Ycg Maximum take-off mass BRF","m",_cgMaximumTakeOffMass.getYBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg Maximum take-off mass BRF","m", _cgMaximumTakeOffMass.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg Maximum take-off mass MAC","%",_cgMaximumTakeOffMass.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Ycg Maximum take-off mass MAC","%",_cgMaximumTakeOffMass.getYMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg Maximum take-off mass MAC","%",_cgMaximumTakeOffMass.getZMAC()*100});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Max forward Xcg MAC","%",_maxForwardCG*100});
		dataListGlobal.add(new Object[] {"Operative max forward Xcg MAC","%",_maxForwardOperativeCG*100});
		dataListGlobal.add(new Object[] {"Max aft Xcg MAC","%",_maxAftCG*100});
		
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
		// FUSELAGE BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			Sheet sheetFuselage = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();
			dataListFuselage.add(new Object[] {"Description","Unit","Value"});
			dataListFuselage.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Ycg LRF","m", _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getYLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Ycg BRF","m", _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getYBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getXCGMap().get(methods) != null) 
					dataListFuselage.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}

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
		// WING BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) {
			Sheet sheetWing = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();
			dataListWing.add(new Object[] {"Description","Unit","Value"});
			dataListWing.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Ycg LRF (semi-wing)","m", _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Ycg BRF (semi-wing)","m", _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getYCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			
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
		// FUEL TANK BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getFuelTank() != null) {
			Sheet sheetFuelTank = wb.createSheet("FUEL TANK");
			List<Object[]> dataListFuelTank = new ArrayList<>();
			dataListFuelTank.add(new Object[] {"Description","Unit","Value"});
			dataListFuelTank.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getFuelTank().getXCGLRF().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Ycg LRF","m", _theBalanceManagerInterface.getTheAircraft().getFuelTank().getYCGLRF().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getFuelTank().getZCGLRF().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {" "});
			dataListFuelTank.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getFuelTank().getXCG().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Ycg BRF","m", _theBalanceManagerInterface.getTheAircraft().getFuelTank().getYCG().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getFuelTank().getZCG().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {" "});
			
			Row rowFuelTank = sheetFuelTank.createRow(0);
			Object[] objArrFuelTank = dataListFuelTank.get(0);
			int cellnumFuelTank = 0;
			for (Object obj : objArrFuelTank) {
				Cell cell = rowFuelTank.createCell(cellnumFuelTank++);
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
				sheetFuelTank.setDefaultColumnWidth(35);
				sheetFuelTank.setColumnWidth(1, 2048);
				sheetFuelTank.setColumnWidth(2, 3840);
			}

			int rownumFuelTank = 1;
			for (int j = 1; j < dataListFuelTank.size(); j++) {
				objArrFuelTank = dataListFuelTank.get(j);
				rowFuelTank = sheetFuelTank.createRow(rownumFuelTank++);
				cellnumFuelTank = 0;
				for (Object obj : objArrFuelTank) {
					Cell cell = rowFuelTank.createCell(cellnumFuelTank++);
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
		// HORIZONTAL TAIL BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) {
			Sheet sheetHTail = wb.createSheet("HORIZONTAL TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();
			dataListHTail.add(new Object[] {"Description","Unit","Value"});
			dataListHTail.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Ycg LRF (semi-tail)","m", _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Ycg BRF (semi-tail)","m", _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getYCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			
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
		// VERTICAL TAIL BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) {
			Sheet sheetVTail = wb.createSheet("VERTICAL TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();
			dataListVTail.add(new Object[] {"Description","Unit","Value"});
			dataListVTail.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Ycg LRF (semi-tail)","m", _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Ycg BRF (semi-tail)","m", _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getYCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			
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
		// CANARD BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) {
			Sheet sheetCanard = wb.createSheet("CANARD");
			List<Object[]> dataListCanard = new ArrayList<>();
			dataListCanard.add(new Object[] {"Description","Unit","Value"});
			dataListCanard.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Ycg LRF (semi-canard)","m", _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Ycg BRF (semi-canard)","m", _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getYCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			
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
		// NACELLES BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) {
			Sheet sheetNacelles = wb.createSheet("NACELLES");
			List<Object[]> dataListNacelles = new ArrayList<>();
			dataListNacelles.add(new Object[] {"Description","Unit","Value"});
			dataListNacelles.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXLRF().doubleValue(SI.METER)});
			dataListNacelles.add(new Object[] {"Ycg LRF","m", _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getYLRF().doubleValue(SI.METER)});
			dataListNacelles.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZLRF().doubleValue(SI.METER)});
			dataListNacelles.add(new Object[] {" "});
			dataListNacelles.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXBRF().doubleValue(SI.METER)});
			dataListNacelles.add(new Object[] {"Ycg BRF","m", _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getYBRF().doubleValue(SI.METER)});
			dataListNacelles.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZBRF().doubleValue(SI.METER)});
			dataListNacelles.add(new Object[] {" "});
			dataListNacelles.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getXCGMap().get(methods) != null) 
					dataListNacelles.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
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
		// POWER PLANT BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant() != null) {
			Sheet sheetPowerPlant = wb.createSheet("POWER PLANT");
			List<Object[]> dataListPowerPlant = new ArrayList<>();
			dataListPowerPlant.add(new Object[] {"Description","Unit","Value"});
			dataListPowerPlant.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXLRF().doubleValue(SI.METER)});
			dataListPowerPlant.add(new Object[] {"Ycg LRF","m", _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getYLRF().doubleValue(SI.METER)});
			dataListPowerPlant.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZLRF().doubleValue(SI.METER)});
			dataListPowerPlant.add(new Object[] {" "});
			dataListPowerPlant.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXBRF().doubleValue(SI.METER)});
			dataListPowerPlant.add(new Object[] {"Ycg BRF","m", _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getYBRF().doubleValue(SI.METER)});
			dataListPowerPlant.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZBRF().doubleValue(SI.METER)});
			dataListPowerPlant.add(new Object[] {" "});
			dataListPowerPlant.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getXCGMap().get(methods) != null) 
					dataListPowerPlant.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
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
		// LANDING GEARS BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) {
			Sheet sheetLandingGears = wb.createSheet("LANDING GEARS");
			List<Object[]> dataListLandingGears = new ArrayList<>();
			dataListLandingGears.add(new Object[] {"Description","Unit","Value"});
			dataListLandingGears.add(new Object[] {"Xcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXLRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {"Ycg LRF","m", _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getYLRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {"Zcg LRF","m", _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZLRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {" "});
			dataListLandingGears.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXBRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {"Ycg BRF","m", _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getYBRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZBRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {" "});
			dataListLandingGears.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getXCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getXCGMap().get(methods) != null) 
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListLandingGears.add(new Object[] {" "});
			dataListLandingGears.add(new Object[] {"Zcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getZCGMap().keySet()) {
				if(_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getZCGMap().get(methods) != null) 
					dataListLandingGears.add(
							new Object[] {
									methods.toString(),
									"m",
									_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getZCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}

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
		// SYSTEMS BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(_theBalanceManagerInterface.getIncludeSystemsPosition() == true) {
			if(_theBalanceManagerInterface.getTheAircraft().getSystems() != null) {
				Sheet sheetSystems = wb.createSheet("SYSTEMS");
				List<Object[]> dataListSystems = new ArrayList<>();
				dataListSystems.add(new Object[] {"Description","Unit","Value"});
				dataListSystems.add(new Object[] {"APU"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getAPUPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getAPUPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});
				dataListSystems.add(new Object[] {"AIR CONDITIONING AND ANTI-ICING SYSTEM"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});
				dataListSystems.add(new Object[] {"INSTRUMENTS AND NAVIGATION SYSTEM"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});
				dataListSystems.add(new Object[] {"HYDRAULIC AND PNEUMATIC SYSTEMS"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});
				dataListSystems.add(new Object[] {"ELECTRICAL SYSTEMS"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getElectricalSystemsPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getElectricalSystemsPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});
				dataListSystems.add(new Object[] {"CONTROL SURFACES"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getControlSurfacesPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getControlSurfacesPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});
				dataListSystems.add(new Object[] {"FURNISHINGS AND EQUIPMENTS"});
				dataListSystems.add(new Object[] {"Xcg BRF","m", _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionX().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {"Ycg BRF","m", 0.0});
				dataListSystems.add(new Object[] {"Zcg BRF","m", _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionZ().doubleValue(SI.METER)});
				dataListSystems.add(new Object[] {" "});

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
		}
		
		//--------------------------------------------------------------------------------
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
	}
	
	private void initializeData() {
		
		_xCGMap = new HashMap<>();
		_zCGMap = new HashMap<>();
		_massMap = new HashMap<>();
		
		_payloadMass = _theBalanceManagerInterface.getSinglePassengerMass().times(
				_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getActualPassengerNumber()
				);
		_maximumZeroFuelMass = _theBalanceManagerInterface.getOperatingEmptyMass().to(SI.KILOGRAM)
				.plus(
						_theBalanceManagerInterface.getSinglePassengerMass().to(SI.KILOGRAM).times(
								_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getMaximumPassengerNumber()
								)
						);
		_maximumTakeOffMass = _theBalanceManagerInterface.getOperatingEmptyMass().to(SI.KILOGRAM)
				.plus(_theBalanceManagerInterface.getDesignFuelMass().to(SI.KILOGRAM))
				.plus(_payloadMass.to(SI.KILOGRAM));
		
	}
	
	public void createCharts(String balanceOutputFolderPath) {

		int indexFrontToRear = getTheBalanceManagerInterface().getTheAircraft()
				.getCabinConfiguration()
				.getSeatsCoGFrontToRear().size();
		int indexRearToFront = getTheBalanceManagerInterface().getTheAircraft()
				.getCabinConfiguration()
				.getSeatsCoGRearToFront().size();
		
		Amount<Length> meanAerodynamicChordXle = getTheBalanceManagerInterface().getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX()
				.plus(getTheBalanceManagerInterface().getTheAircraft().getWing().getXApexConstructionAxes());
		Amount<Length> meanAerodynamicChord = getTheBalanceManagerInterface().getTheAircraft().getWing().getMeanAerodynamicChord();
		
		Double[] seatCoGFrontToRearReferToMAC = new Double[indexFrontToRear];
		Double[] seatCoGRearToFrontReferToMAC = new Double[indexRearToFront];
		Double[] fuelCoGBeforeBoardingReferToMAC = new Double[2];
		Double[] massWithFuelBeforeBoarding = new Double[2];
		Double[] fuelCoGAfterBoardingReferToMAC = new Double[2];
		Double[] massWithFuelAfterBoarding = new Double[2];
		
		seatCoGFrontToRearReferToMAC[0] = (_cgOperatingEmptyMass.getXMAC()*100);
		seatCoGRearToFrontReferToMAC[0] = (_cgOperatingEmptyMass.getXMAC()*100);  
		
		for (int i=0; i<getTheBalanceManagerInterface().getTheAircraft()
				.getCabinConfiguration()
				.getSeatsCoGFrontToRear().size(); i++) {
			seatCoGFrontToRearReferToMAC[i] = 
					getTheBalanceManagerInterface().getTheAircraft()
						.getCabinConfiguration()
							.getSeatsCoGFrontToRear().get(i)
								.to(SI.METER)
					.minus(meanAerodynamicChordXle.to(SI.METER))
					.divide(meanAerodynamicChord.to(SI.METER))
					.times(100)
					.getEstimatedValue();
		}
		for (int i=0; i<getTheBalanceManagerInterface().getTheAircraft()
				.getCabinConfiguration()
				.getSeatsCoGRearToFront().size(); i++) {
			seatCoGRearToFrontReferToMAC[i] = 
					seatCoGRearToFrontReferToMAC[i] = 
					getTheBalanceManagerInterface().getTheAircraft()
						.getCabinConfiguration()
							.getSeatsCoGRearToFront().get(i)
							.to(SI.METER)
					.minus(meanAerodynamicChordXle.to(SI.METER))
					.divide(meanAerodynamicChord.to(SI.METER))
					.times(100)
					.getEstimatedValue();
		}
		
		// FUEL BEFORE BOARDING
		fuelCoGBeforeBoardingReferToMAC[0] = _cgOperatingEmptyMass.getXMAC()*100;
		massWithFuelBeforeBoarding[0] = _theBalanceManagerInterface.getOperatingEmptyMass().doubleValue(SI.KILOGRAM);
		fuelCoGBeforeBoardingReferToMAC[1] = ((((_cgOperatingEmptyMass.getXBRF().to(SI.METER).times(getTheBalanceManagerInterface().getOperatingEmptyMass().to(SI.KILOGRAM)).getEstimatedValue())
				+ (getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getXCG().to(SI.METER)
						.times(getTheBalanceManagerInterface().getDesignFuelMass().to(SI.KILOGRAM)).getEstimatedValue()))
				/(getTheBalanceManagerInterface().getOperatingEmptyMass().to(SI.KILOGRAM).plus(getTheBalanceManagerInterface().getDesignFuelMass().to(SI.KILOGRAM)).getEstimatedValue()))
				- meanAerodynamicChordXle.doubleValue(SI.METER))
				/ (meanAerodynamicChord.doubleValue(SI.METER)/100
						);
		massWithFuelBeforeBoarding[1] = getTheBalanceManagerInterface().getOperatingEmptyMass().to(SI.KILOGRAM)
				.plus(getTheBalanceManagerInterface().getDesignFuelMass().to(SI.KILOGRAM))
				.doubleValue(SI.KILOGRAM); 
				
		
		// FUEL AFTER BOARDING
		fuelCoGAfterBoardingReferToMAC[0] = _cgMaximumZeroFuelMass.getXMAC()*100;
		massWithFuelAfterBoarding[0] = _maximumZeroFuelMass.doubleValue(SI.KILOGRAM);
		fuelCoGAfterBoardingReferToMAC[1] = (_cgMaximumTakeOffMass.getXMAC()*100); 
		massWithFuelAfterBoarding[1] = getTheBalanceManagerInterface().getOperatingEmptyMass().doubleValue(SI.KILOGRAM)
				+ getTheBalanceManagerInterface().getSinglePassengerMass().times(
						getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getMaximumPassengerNumber()
						).doubleValue(SI.KILOGRAM)
				+ getTheBalanceManagerInterface().getDesignFuelMass().doubleValue(SI.KILOGRAM);
				
		
		List<Double[]> xList = new ArrayList<>();
		List<Double[]> yList = new ArrayList<>();
		List<String> legend = new ArrayList<>();
		
		xList.add(seatCoGFrontToRearReferToMAC);
		xList.add(seatCoGRearToFrontReferToMAC);
		xList.add(fuelCoGBeforeBoardingReferToMAC);
		xList.add(fuelCoGAfterBoardingReferToMAC);
		
		yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getCurrentMassList()));
		yList.add(MyArrayUtils.convertListOfAmountToDoubleArray(getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getCurrentMassList()));
		yList.add(massWithFuelBeforeBoarding);
		yList.add(massWithFuelAfterBoarding);
		
		legend.add("Front to Rear");
		legend.add("Rear to Front");
		legend.add("Fuel before boarding");
		legend.add("Fuel after boarding");
		
		try {
			MyChartToFileUtils.plot(
					xList, yList,
					"Loading Cycle", "Xcg/c", "Mass",
					null, null, null, null,
					"%", "Kg",
					true, legend,
					balanceOutputFolderPath, "Loading Cycle",
					getTheBalanceManagerInterface().getTheAircraft().getTheAnalysisManager().getCreateCSVBalance()
					);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * Evaluate center of gravity location
	 * of each component.
	 * 
	 * @param aircraft
	 * @param conditions
	 * @param methodsMap
	 */
	public void calculate(Map<ComponentEnum, MethodEnum> _methodsMapBalance){

		initializeData();
		
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().calculateCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.FUSELAGE, _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.FUSELAGE, _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.FUSELAGE, _theBalanceManagerInterface.getFuselageMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.WING, _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.WING, _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.WING, _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.WING, _theBalanceManagerInterface.getWingMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getFuelTank() != null) {
			_theBalanceManagerInterface.getTheAircraft().getFuelTank().calculateCG();
			_theBalanceManagerInterface.getTheAircraft().getFuelTank().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.FUEL_TANK, _theBalanceManagerInterface.getTheAircraft().getFuelTank().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.FUEL_TANK, _theBalanceManagerInterface.getTheAircraft().getFuelTank().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.FUEL_TANK, _theBalanceManagerInterface.getDesignFuelMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.HORIZONTAL_TAIL, _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.HORIZONTAL_TAIL, _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.HORIZONTAL_TAIL, _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.HORIZONTAL_TAIL, _theBalanceManagerInterface.getHTailMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.VERTICAL_TAIL, _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.VERTICAL_TAIL, _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.VERTICAL_TAIL, _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.VERTICAL_TAIL, _theBalanceManagerInterface.getVTailMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.CANARD, _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.CANARD, _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.CANARD, _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.CANARD, _theBalanceManagerInterface.getCanardMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().calculateTotalCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.NACELLE, _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.NACELLE, _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.NACELLE, _theBalanceManagerInterface.getNacellesMass().to(SI.KILOGRAM));
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().calculateTotalCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.POWER_PLANT, _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.POWER_PLANT, _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.POWER_PLANT, _theBalanceManagerInterface.getPowerPlantMass().to(SI.KILOGRAM));
		}
			
		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) { 
			_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().calculateCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);
			_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().calculateCGinMAC(
					(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
					Amount.valueOf(0., SI.METER), 
					_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER)
					);
			_xCGMap.put(ComponentEnum.LANDING_GEAR, _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXBRF().to(SI.METER));
			_zCGMap.put(ComponentEnum.LANDING_GEAR, _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZBRF().to(SI.METER));
			_massMap.put(ComponentEnum.LANDING_GEAR, _theBalanceManagerInterface.getLandingGearMass().to(SI.KILOGRAM));
		}

		if(_theBalanceManagerInterface.getIncludeSystemsPosition() == true) {
			if(_theBalanceManagerInterface.getTheAircraft().getSystems() != null) {

				_xCGMap.put(ComponentEnum.APU, _theBalanceManagerInterface.getAPUPositionX().to(SI.METER));
				_zCGMap.put(ComponentEnum.APU, _theBalanceManagerInterface.getAPUPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.APU, _theBalanceManagerInterface.getAPUMass().to(SI.KILOGRAM));

				_xCGMap.put(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING, _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER));
				_zCGMap.put(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING, _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING, _theBalanceManagerInterface.getAirConditioningAndAntiIcingMass().to(SI.KILOGRAM));

				_xCGMap.put(ComponentEnum.INSTRUMENTS_AND_NAVIGATION, _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionX().to(SI.METER));
				_zCGMap.put(ComponentEnum.INSTRUMENTS_AND_NAVIGATION, _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.INSTRUMENTS_AND_NAVIGATION, _theBalanceManagerInterface.getInstrumentsAndNavigationSystemMass().to(SI.KILOGRAM));

				_xCGMap.put(ComponentEnum.HYDRAULIC_AND_PNEUMATICS, _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionX().to(SI.METER));
				_zCGMap.put(ComponentEnum.HYDRAULIC_AND_PNEUMATICS, _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.HYDRAULIC_AND_PNEUMATICS, _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsMass().to(SI.KILOGRAM));

				_xCGMap.put(ComponentEnum.ELECTRICAL_SYSTEMS, _theBalanceManagerInterface.getElectricalSystemsPositionX().to(SI.METER));
				_zCGMap.put(ComponentEnum.ELECTRICAL_SYSTEMS, _theBalanceManagerInterface.getElectricalSystemsPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.ELECTRICAL_SYSTEMS, _theBalanceManagerInterface.getElectricalSystemsMass().to(SI.KILOGRAM));

				_xCGMap.put(ComponentEnum.CONTROL_SURFACES, _theBalanceManagerInterface.getControlSurfacesPositionZ().to(SI.METER));
				_zCGMap.put(ComponentEnum.CONTROL_SURFACES, _theBalanceManagerInterface.getControlSurfacesPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.CONTROL_SURFACES, _theBalanceManagerInterface.getControlSurfacesMass().to(SI.KILOGRAM));

				_xCGMap.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionX().to(SI.METER));
				_zCGMap.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionZ().to(SI.METER));
				_massMap.put(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS, _theBalanceManagerInterface.getFurnishingsAndEquipmentsMass().to(SI.KILOGRAM));

			}
		}

		//.............................................................................................................................
		// Structural CG
		_cgStructure = new CenterOfGravity();
		
		double prodX = 0., prodZ = 0., sum = 0.;
		
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			prodX += _xCGMap.get(ComponentEnum.FUSELAGE).doubleValue(SI.METER)*_massMap.get(ComponentEnum.FUSELAGE).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.FUSELAGE).doubleValue(SI.METER)*_massMap.get(ComponentEnum.FUSELAGE).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.FUSELAGE).doubleValue(SI.KILOGRAM);
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) {
			prodX += _xCGMap.get(ComponentEnum.WING).doubleValue(SI.METER)*_massMap.get(ComponentEnum.WING).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.WING).doubleValue(SI.METER)*_massMap.get(ComponentEnum.WING).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.WING).doubleValue(SI.KILOGRAM);
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) {
			prodX += _xCGMap.get(ComponentEnum.HORIZONTAL_TAIL).doubleValue(SI.METER)*_massMap.get(ComponentEnum.HORIZONTAL_TAIL).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.HORIZONTAL_TAIL).doubleValue(SI.METER)*_massMap.get(ComponentEnum.HORIZONTAL_TAIL).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.HORIZONTAL_TAIL).doubleValue(SI.KILOGRAM);
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) {
			prodX += _xCGMap.get(ComponentEnum.VERTICAL_TAIL).doubleValue(SI.METER)*_massMap.get(ComponentEnum.VERTICAL_TAIL).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.VERTICAL_TAIL).doubleValue(SI.METER)*_massMap.get(ComponentEnum.VERTICAL_TAIL).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.VERTICAL_TAIL).doubleValue(SI.KILOGRAM);
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) {
			prodX += _xCGMap.get(ComponentEnum.CANARD).doubleValue(SI.METER)*_massMap.get(ComponentEnum.CANARD).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.CANARD).doubleValue(SI.METER)*_massMap.get(ComponentEnum.CANARD).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.CANARD).doubleValue(SI.KILOGRAM);
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) {
			prodX += _xCGMap.get(ComponentEnum.NACELLE).doubleValue(SI.METER)*_massMap.get(ComponentEnum.NACELLE).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.NACELLE).doubleValue(SI.METER)*_massMap.get(ComponentEnum.NACELLE).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.NACELLE).doubleValue(SI.KILOGRAM);
		}
		
		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) {
			prodX += _xCGMap.get(ComponentEnum.LANDING_GEAR).doubleValue(SI.METER)*_massMap.get(ComponentEnum.LANDING_GEAR).doubleValue(SI.KILOGRAM);
			prodZ += _zCGMap.get(ComponentEnum.LANDING_GEAR).doubleValue(SI.METER)*_massMap.get(ComponentEnum.LANDING_GEAR).doubleValue(SI.KILOGRAM);
			sum += _massMap.get(ComponentEnum.LANDING_GEAR).doubleValue(SI.KILOGRAM);
		}

		_cgStructure.setXBRF(
				Amount.valueOf(prodX/sum, SI.METER));
		
		_cgStructure.setZBRF(
				Amount.valueOf(prodZ/sum, SI.METER)
				);
		
		_cgStructure.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));

		//.............................................................................................................................
		// Structure + engines CG
		_cgStructureAndPower = new CenterOfGravity();
		
		_cgStructureAndPower.setXBRF(
				Amount.valueOf(
						( (_xCGMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.METER)*_massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM))
								+ (sum*_cgStructure.getXBRF().doubleValue(SI.METER))
								)
						/(sum + _massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM)),
						SI.METER)
				);

		_cgStructureAndPower.setZBRF(
				Amount.valueOf(
						( (_zCGMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.METER)*_massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM))
								+ (sum*_cgStructure.getZBRF().doubleValue(SI.METER))
								)
						/(sum + _massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM)),
						SI.METER)
				);

		_cgStructureAndPower.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));
		
		//.............................................................................................................................
		// Manufacturer Empty Mass CG location
		_cgManufacturerEmptuMass = new CenterOfGravity();
		
		if(_theBalanceManagerInterface.getIncludeSystemsPosition() == true) {
			
			_cgManufacturerEmptuMass.setXBRF(
					Amount.valueOf(
							( (_xCGMap.get(ComponentEnum.APU).doubleValue(SI.METER)*_massMap.get(ComponentEnum.APU).doubleValue(SI.KILOGRAM))
									+ (_xCGMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).doubleValue(SI.METER)*_massMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).doubleValue(SI.KILOGRAM))
									+ (_xCGMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).doubleValue(SI.METER)*_massMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).doubleValue(SI.KILOGRAM))
									+ (_xCGMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).doubleValue(SI.METER)*_massMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).doubleValue(SI.KILOGRAM))
									+ (_xCGMap.get(ComponentEnum.ELECTRICAL_SYSTEMS).doubleValue(SI.METER)*_massMap.get(ComponentEnum.ELECTRICAL_SYSTEMS).doubleValue(SI.KILOGRAM))
									+ (_xCGMap.get(ComponentEnum.CONTROL_SURFACES).doubleValue(SI.METER)*_massMap.get(ComponentEnum.CONTROL_SURFACES).doubleValue(SI.KILOGRAM))
									+ (_xCGMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).doubleValue(SI.METER)*_massMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).doubleValue(SI.KILOGRAM))
									+ (_cgStructureAndPower.getXBRF().doubleValue(SI.METER)*(_massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM) + sum))
									)
							/(sum + _massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.APU).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.ELECTRICAL_SYSTEMS).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.CONTROL_SURFACES).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).doubleValue(SI.KILOGRAM)
									),
							SI.METER)
					);

			_cgManufacturerEmptuMass.setZBRF(
					Amount.valueOf(
							( (_zCGMap.get(ComponentEnum.APU).doubleValue(SI.METER)*_massMap.get(ComponentEnum.APU).doubleValue(SI.KILOGRAM))
									+ (_zCGMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).doubleValue(SI.METER)*_massMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).doubleValue(SI.KILOGRAM))
									+ (_zCGMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).doubleValue(SI.METER)*_massMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).doubleValue(SI.KILOGRAM))
									+ (_zCGMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).doubleValue(SI.METER)*_massMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).doubleValue(SI.KILOGRAM))
									+ (_zCGMap.get(ComponentEnum.ELECTRICAL_SYSTEMS).doubleValue(SI.METER)*_massMap.get(ComponentEnum.ELECTRICAL_SYSTEMS).doubleValue(SI.KILOGRAM))
									+ (_zCGMap.get(ComponentEnum.CONTROL_SURFACES).doubleValue(SI.METER)*_massMap.get(ComponentEnum.CONTROL_SURFACES).doubleValue(SI.KILOGRAM))
									+ (_zCGMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).doubleValue(SI.METER)*_massMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).doubleValue(SI.KILOGRAM))
									+ (_cgStructureAndPower.getZBRF().doubleValue(SI.METER)*(_massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM) + sum))
									)
							/(sum + _massMap.get(ComponentEnum.POWER_PLANT).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.APU).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.ELECTRICAL_SYSTEMS).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.CONTROL_SURFACES).doubleValue(SI.KILOGRAM)
									+ _massMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS).doubleValue(SI.KILOGRAM)
									),
							SI.METER)
					);

		}
		else {
			
			_cgManufacturerEmptuMass.setXBRF(_cgStructureAndPower.getXBRF().to(SI.METER));
			_cgManufacturerEmptuMass.setZBRF(_cgStructureAndPower.getZBRF().to(SI.METER));

		}

		_cgManufacturerEmptuMass.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));

		//.............................................................................................................................
		// Operating Empty Mass CG location
		/**
		 * AT THIS POINT THE SUM OF ALL STRUCTURAL, ENGINES AND SYSTEMS MASSES IS NOT EQUAL TO THE 
		 * OPERATING EMPTY MASS. THE ASSUMPTION MADE IS THAT ALL THE COMPONENTS THAT HAVE TO BE 
		 * CONSEDERED IN ORDER TO REACH THE OPERATING EMPTY MASS (OPERATING ITEM MASS, ETC...)
		 * DO NOT AFFECT THE CG LOCATION.
		 */
		_cgOperatingEmptyMass = new CenterOfGravity();
		_cgOperatingEmptyMass.setXBRF(_cgManufacturerEmptuMass.getXBRF().to(SI.METER));
		_cgOperatingEmptyMass.setZBRF(_cgManufacturerEmptuMass.getZBRF().to(SI.METER));
		_cgOperatingEmptyMass.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));
		
		_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().buildSimpleLayout(_theBalanceManagerInterface.getTheAircraft());
		
		//.............................................................................................................................
		// Maximum Zero Fuel Mass CG location
		_cgMaximumZeroFuelMass = new CenterOfGravity();

		_cgMaximumZeroFuelMass.setXBRF(
				_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getSeatsCoGFrontToRear().get(
						_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getSeatsCoGFrontToRear().size()-1
						)
				);
		
		_cgMaximumZeroFuelMass.setZBRF(
				Amount.valueOf(
						(_cgOperatingEmptyMass.getZBRF().doubleValue(SI.METER)*_theBalanceManagerInterface.getOperatingEmptyMass().doubleValue(SI.KILOGRAM))
						/(_payloadMass.doubleValue(SI.KILOGRAM)	+ _theBalanceManagerInterface.getOperatingEmptyMass().doubleValue(SI.KILOGRAM)),
						SI.METER)
				);
		
		_cgMaximumZeroFuelMass.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));

		//.............................................................................................................................
		// Maximum Take-Off Mass CG location
		_cgMaximumTakeOffMass = new CenterOfGravity();

		_cgMaximumTakeOffMass.setXBRF(
				Amount.valueOf(
						( (_cgMaximumZeroFuelMass.getXBRF().doubleValue(SI.METER)* _maximumZeroFuelMass.doubleValue(SI.KILOGRAM))
								+ (_theBalanceManagerInterface.getDesignFuelMass().doubleValue(SI.KILOGRAM)* _theBalanceManagerInterface.getTheAircraft().getFuelTank().getXCG().doubleValue(SI.METER))
								)
						/ this._maximumTakeOffMass.doubleValue(SI.KILOGRAM),
						SI.METER));

		_cgMaximumTakeOffMass.setZBRF(
				Amount.valueOf(
						( (_cgMaximumZeroFuelMass.getZBRF().doubleValue(SI.METER)* _maximumZeroFuelMass.doubleValue(SI.KILOGRAM))
								+ (_theBalanceManagerInterface.getDesignFuelMass().doubleValue(SI.KILOGRAM)* _theBalanceManagerInterface.getTheAircraft().getFuelTank().getZCG().doubleValue(SI.METER))
								)
								/ this._maximumTakeOffMass.doubleValue(SI.KILOGRAM),
								SI.METER));
		
		_cgMaximumTakeOffMass.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));

		// MAX AFT AND FWD CG
		int index = _theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getSeatsCoGFrontToRear().size();
		Amount<Length> meanAerodynamicChordXle = _theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
				.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER));
		Amount<Length> meanAerodynamicChord = _theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER);
		
		List<Double> cgExcursionRefToMAC = new ArrayList<>();
		
		cgExcursionRefToMAC.add(_cgOperatingEmptyMass.getXMAC()*100);
		cgExcursionRefToMAC.add(_cgOperatingEmptyMass.getXMAC()*100);  
		
		for (int i=0; i<index; i++) {
			cgExcursionRefToMAC.add( 
					_theBalanceManagerInterface.getTheAircraft()
						.getCabinConfiguration()
							.getSeatsCoGFrontToRear().get(i)
					.minus(meanAerodynamicChordXle)
					.divide(meanAerodynamicChord)
					.times(100)
					.getEstimatedValue()
					);
		}
			for (int i=0; i<_theBalanceManagerInterface.getTheAircraft()
					.getCabinConfiguration()
					.getSeatsCoGRearToFront().size(); i++) {			
			cgExcursionRefToMAC.add(  
					_theBalanceManagerInterface.getTheAircraft()
						.getCabinConfiguration()
							.getSeatsCoGRearToFront().get(i)
					.minus(meanAerodynamicChordXle)
					.divide(meanAerodynamicChord)
					.times(100)
					.getEstimatedValue()
					);
		}
		
		cgExcursionRefToMAC.add(((((_cgOperatingEmptyMass.getXBRF().times(_theBalanceManagerInterface.getOperatingEmptyMass()).getEstimatedValue())
				+ (_theBalanceManagerInterface.getTheAircraft().getFuelTank().getXCG().to(SI.METER)
						.times(_theBalanceManagerInterface.getDesignFuelMass().doubleValue(SI.KILOGRAM)).getEstimatedValue()))
				/(_theBalanceManagerInterface.getOperatingEmptyMass().to(SI.KILOGRAM).plus(_theBalanceManagerInterface.getDesignFuelMass().to(SI.KILOGRAM)).getEstimatedValue()))
				- meanAerodynamicChordXle.doubleValue(SI.METER))
				/ (meanAerodynamicChord.doubleValue(SI.METER)/100
						)
				);
		cgExcursionRefToMAC.add((_cgMaximumTakeOffMass.getXMAC()*100));
		
		/*
		 * These are the real boundaries of the boarding diagram
		 */
		_maxForwardCG = (MyArrayUtils.getMin(cgExcursionRefToMAC)/100);
		_maxAftCG = (MyArrayUtils.getMax(cgExcursionRefToMAC)/100);
		
		/*
		 * This is the opereative max forward CG position (CG at max payload with design fuel)
		 * This will be used to compute all the max forward CG polar curves.
		 */
		_maxForwardOperativeCG = _cgMaximumTakeOffMass.getXMAC(); 
		
		/*
		 * Once all components CG and the Aircraft CG are known, total inertia moments and products
		 * can be calculated using each calculated (or assigned) component mass. 
		 */
		calculateAircraftInertiaMoments();
		calculateAircraftInertiaProducts();
		
	}

	@SuppressWarnings("unchecked")
	private void calculateAircraftInertiaMoments() {

		//------------------------------------------------------------------------------------------------
		// DATA INITIALIZATION
		Amount<?> fuselageInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuselageInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuselageInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> wingInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> wingInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> wingInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hTailInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hTailInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hTailInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> vTailInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> vTailInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> vTailInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> canardInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> canardInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> canardInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> nacellesInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> nacellesInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> nacellesInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> powerPlantInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> powerPlantInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> powerPlantInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> landingGearsInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> landingGearsInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> landingGearsInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> apuInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> apuInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> apuInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> airConditioningAndAntiIcingSystemInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> airConditioningAndAntiIcingSystemInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> airConditioningAndAntiIcingSystemInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> instrumentsAndNavigationSystemInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> instrumentsAndNavigationSystemInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> instrumentsAndNavigationSystemInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> electricalSystemInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> electricalSystemInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> electricalSystemInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hydraulicAndPneumaticSystemsInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hydraulicAndPneumaticSystemsInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hydraulicAndPneumaticSystemsInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> controlSurfacesInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> controlSurfacesInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> controlSurfacesInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> furnishingsAndEquipmentsInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> furnishingsAndEquipmentsInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> furnishingsAndEquipmentsInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> payloadInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> payloadInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> payloadInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuelTankInertiaMomentIxx = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuelTankInertiaMomentIyy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuelTankInertiaMomentIzz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		
		//------------------------------------------------------------------------------------------------
		// FUSELAGE
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			fuselageInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getFuselageMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getZBRF()
					);
			fuselageInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getFuselageMass(),
					_zCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getXBRF()
					);
			fuselageInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getFuselageMass(),
					_xCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
		
		//------------------------------------------------------------------------------------------------
		// WING
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) {
			wingInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getWingMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.WING), _cgMaximumTakeOffMass.getZBRF()
					);
			wingInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getWingMass(),
					_zCGMap.get(ComponentEnum.WING), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.WING), _cgMaximumTakeOffMass.getXBRF()
					);
			wingInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getWingMass(),
					_xCGMap.get(ComponentEnum.WING), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
		
		//------------------------------------------------------------------------------------------------
		// HORIZONTAL TAIL
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) {
			hTailInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getHTailMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.HORIZONTAL_TAIL), _cgMaximumTakeOffMass.getZBRF()
					);
			hTailInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getHTailMass(),
					_zCGMap.get(ComponentEnum.HORIZONTAL_TAIL), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.HORIZONTAL_TAIL), _cgMaximumTakeOffMass.getXBRF()
					);
			hTailInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getHTailMass(),
					_xCGMap.get(ComponentEnum.HORIZONTAL_TAIL), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
		
		//------------------------------------------------------------------------------------------------
		// VERTICAL TAIL
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) {
			vTailInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getVTailMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.VERTICAL_TAIL), _cgMaximumTakeOffMass.getZBRF()
					);
			vTailInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getVTailMass(),
					_zCGMap.get(ComponentEnum.VERTICAL_TAIL), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.VERTICAL_TAIL), _cgMaximumTakeOffMass.getXBRF()
					);
			vTailInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getVTailMass(),
					_xCGMap.get(ComponentEnum.VERTICAL_TAIL), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}

		//------------------------------------------------------------------------------------------------
		// CANARD
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) {
			canardInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getCanardMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.CANARD), _cgMaximumTakeOffMass.getZBRF()
					);
			canardInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getCanardMass(),
					_zCGMap.get(ComponentEnum.CANARD), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.CANARD), _cgMaximumTakeOffMass.getXBRF()
					);
			canardInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getCanardMass(),
					_xCGMap.get(ComponentEnum.CANARD), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
		
		//------------------------------------------------------------------------------------------------
		// NACELLES
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) {
			nacellesInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getNacellesMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.NACELLE), _cgMaximumTakeOffMass.getZBRF()
					);
			nacellesInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getNacellesMass(),
					_zCGMap.get(ComponentEnum.NACELLE), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.NACELLE), _cgMaximumTakeOffMass.getXBRF()
					);
			nacellesInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getNacellesMass(),
					_xCGMap.get(ComponentEnum.NACELLE), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
		
		//------------------------------------------------------------------------------------------------
		// POWER PLANT
		if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant() != null) {
			powerPlantInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getPowerPlantMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.POWER_PLANT), _cgMaximumTakeOffMass.getZBRF()
					);
			powerPlantInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getPowerPlantMass(),
					_zCGMap.get(ComponentEnum.POWER_PLANT), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.POWER_PLANT), _cgMaximumTakeOffMass.getXBRF()
					);
			powerPlantInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getPowerPlantMass(),
					_xCGMap.get(ComponentEnum.POWER_PLANT), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
		
		//------------------------------------------------------------------------------------------------
		// LANDING GEARS
		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) {
			landingGearsInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getLandingGearMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.LANDING_GEAR), _cgMaximumTakeOffMass.getZBRF()
					);
			landingGearsInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getLandingGearMass(),
					_zCGMap.get(ComponentEnum.LANDING_GEAR), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.LANDING_GEAR), _cgMaximumTakeOffMass.getXBRF()
					);
			landingGearsInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getLandingGearMass(),
					_xCGMap.get(ComponentEnum.LANDING_GEAR), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}

		if(_theBalanceManagerInterface.getTheAircraft().getSystems() != null) {
			
			//------------------------------------------------------------------------------------------------
			// APU
			apuInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getAPUMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.APU), _cgMaximumTakeOffMass.getZBRF()
					);
			apuInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getAPUMass(),
					_zCGMap.get(ComponentEnum.APU), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.APU), _cgMaximumTakeOffMass.getXBRF()
					);
			apuInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getAPUMass(),
					_xCGMap.get(ComponentEnum.APU), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			
			//------------------------------------------------------------------------------------------------
			// AIR CONDITIONIN AND ANTI-ICING SYSTEM
			airConditioningAndAntiIcingSystemInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getAirConditioningAndAntiIcingMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING), _cgMaximumTakeOffMass.getZBRF()
					);
			airConditioningAndAntiIcingSystemInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getAirConditioningAndAntiIcingMass(),
					_zCGMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING), _cgMaximumTakeOffMass.getXBRF()
					);
			airConditioningAndAntiIcingSystemInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getAirConditioningAndAntiIcingMass(),
					_xCGMap.get(ComponentEnum.AIR_CONDITIONING_AND_ANTI_ICING), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			
			//------------------------------------------------------------------------------------------------
			// INSTRUMENTS AND NAVIGATION SYSTEM
			instrumentsAndNavigationSystemInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getInstrumentsAndNavigationSystemMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION), _cgMaximumTakeOffMass.getZBRF()
					);
			instrumentsAndNavigationSystemInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getInstrumentsAndNavigationSystemMass(),
					_zCGMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION), _cgMaximumTakeOffMass.getXBRF()
					);
			instrumentsAndNavigationSystemInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getInstrumentsAndNavigationSystemMass(),
					_xCGMap.get(ComponentEnum.INSTRUMENTS_AND_NAVIGATION), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			
			//------------------------------------------------------------------------------------------------
			// ELECTRICAL SYSTEM
			electricalSystemInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getElectricalSystemsMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.ELECTRICAL_SYSTEMS), _cgMaximumTakeOffMass.getZBRF()
					);
			electricalSystemInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getElectricalSystemsMass(),
					_zCGMap.get(ComponentEnum.ELECTRICAL_SYSTEMS), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.ELECTRICAL_SYSTEMS), _cgMaximumTakeOffMass.getXBRF()
					);
			electricalSystemInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getElectricalSystemsMass(),
					_xCGMap.get(ComponentEnum.ELECTRICAL_SYSTEMS), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			
			//------------------------------------------------------------------------------------------------
			// HYDRAULIC AND PNEUMATIC SYSTEMS
			hydraulicAndPneumaticSystemsInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getHydraulicAndPneumaticSystemsMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS), _cgMaximumTakeOffMass.getZBRF()
					);
			hydraulicAndPneumaticSystemsInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getHydraulicAndPneumaticSystemsMass(),
					_zCGMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS), _cgMaximumTakeOffMass.getXBRF()
					);
			hydraulicAndPneumaticSystemsInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getHydraulicAndPneumaticSystemsMass(),
					_xCGMap.get(ComponentEnum.HYDRAULIC_AND_PNEUMATICS), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			
			//------------------------------------------------------------------------------------------------
			// CONTROL SURFACES
			controlSurfacesInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getControlSurfacesMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.CONTROL_SURFACES), _cgMaximumTakeOffMass.getZBRF()
					);
			controlSurfacesInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getControlSurfacesMass(),
					_zCGMap.get(ComponentEnum.CONTROL_SURFACES), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.CONTROL_SURFACES), _cgMaximumTakeOffMass.getXBRF()
					);
			controlSurfacesInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getControlSurfacesMass(),
					_xCGMap.get(ComponentEnum.CONTROL_SURFACES), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			
			//------------------------------------------------------------------------------------------------
			// FURNISHNGS AND EQUIPMENTS
			furnishingsAndEquipmentsInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
					_theBalanceManagerInterface.getFurnishingsAndEquipmentsMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS), _cgMaximumTakeOffMass.getZBRF()
					);
			furnishingsAndEquipmentsInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
					_theBalanceManagerInterface.getFurnishingsAndEquipmentsMass(),
					_zCGMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS), _cgMaximumTakeOffMass.getZBRF(),
					_xCGMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS), _cgMaximumTakeOffMass.getXBRF()
					);
			furnishingsAndEquipmentsInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
					_theBalanceManagerInterface.getFurnishingsAndEquipmentsMass(),
					_xCGMap.get(ComponentEnum.FURNISHINGS_AND_EQUIPMENTS), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
		}
	
		//------------------------------------------------------------------------------------------------
		// PAYLOAD
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			if(_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration() != null) {
				payloadInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
						_theBalanceManagerInterface.getSinglePassengerMass().times(
								_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getActualPassengerNumber()
								),
						Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
						Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getZBRF()
						);
				payloadInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
						_theBalanceManagerInterface.getSinglePassengerMass().times(
								_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getActualPassengerNumber()
								),
						Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getZBRF(),
						_theBalanceManagerInterface.getTheAircraft().getFuselage().getNoseLength().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getFuselage().getCylinderLength().to(SI.METER).divide(2)),
						_cgMaximumTakeOffMass.getXBRF()
						);
				payloadInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
						_theBalanceManagerInterface.getSinglePassengerMass().times(
								_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getActualPassengerNumber()
								),
						_theBalanceManagerInterface.getTheAircraft().getFuselage().getNoseLength().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getFuselage().getCylinderLength().to(SI.METER).divide(2)),
						_cgMaximumTakeOffMass.getXBRF(),
						Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
						);
			}
		}
		
		//------------------------------------------------------------------------------------------------
		// FUEL TANK
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) {
			if(_theBalanceManagerInterface.getTheAircraft().getFuelTank() != null) {
				fuelTankInertiaMomentIxx = InertiaContributionsCalc.calculateComponentInertiaMomentIxx(
						_theBalanceManagerInterface.getDesignFuelMass(),
						Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
						_zCGMap.get(ComponentEnum.FUEL_TANK), _cgMaximumTakeOffMass.getZBRF()
						);
				fuelTankInertiaMomentIyy = InertiaContributionsCalc.calculateComponentInertiaMomentIyy(
						_theBalanceManagerInterface.getDesignFuelMass(),
						_zCGMap.get(ComponentEnum.FUEL_TANK), _cgMaximumTakeOffMass.getZBRF(),
						_xCGMap.get(ComponentEnum.FUEL_TANK), _cgMaximumTakeOffMass.getXBRF()
						);
				fuelTankInertiaMomentIzz = InertiaContributionsCalc.calculateComponentInertiaMomentIzz(
						_theBalanceManagerInterface.getDesignFuelMass(),
						_xCGMap.get(ComponentEnum.FUEL_TANK), _cgMaximumTakeOffMass.getXBRF(),
						Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
						);
			}
		}
		
		//------------------------------------------------------------------------------------------------
		// GLOBAL INERTIA MOMENT CALCULATION
		_aircraftInertiaMomentIxx = 
				fuselageInertiaMomentIxx
				.plus(wingInertiaMomentIxx)
				.plus(hTailInertiaMomentIxx)
				.plus(vTailInertiaMomentIxx)
				.plus(canardInertiaMomentIxx)
				.plus(nacellesInertiaMomentIxx)
				.plus(powerPlantInertiaMomentIxx)
				.plus(landingGearsInertiaMomentIxx)
				.plus(apuInertiaMomentIxx)
				.plus(airConditioningAndAntiIcingSystemInertiaMomentIxx)
				.plus(instrumentsAndNavigationSystemInertiaMomentIxx)
				.plus(electricalSystemInertiaMomentIxx)
				.plus(hydraulicAndPneumaticSystemsInertiaMomentIxx)
				.plus(controlSurfacesInertiaMomentIxx)
				.plus(furnishingsAndEquipmentsInertiaMomentIxx)
				.plus(payloadInertiaMomentIxx)
				.plus(fuelTankInertiaMomentIxx);
				
		_aircraftInertiaMomentIyy =
				fuselageInertiaMomentIyy
				.plus(wingInertiaMomentIyy)
				.plus(hTailInertiaMomentIyy)
				.plus(vTailInertiaMomentIyy)
				.plus(canardInertiaMomentIyy)
				.plus(nacellesInertiaMomentIyy)
				.plus(powerPlantInertiaMomentIyy)
				.plus(landingGearsInertiaMomentIyy)
				.plus(apuInertiaMomentIyy)
				.plus(airConditioningAndAntiIcingSystemInertiaMomentIyy)
				.plus(instrumentsAndNavigationSystemInertiaMomentIyy)
				.plus(electricalSystemInertiaMomentIyy)
				.plus(hydraulicAndPneumaticSystemsInertiaMomentIyy)
				.plus(controlSurfacesInertiaMomentIyy)
				.plus(furnishingsAndEquipmentsInertiaMomentIyy)
				.plus(payloadInertiaMomentIyy)
				.plus(fuelTankInertiaMomentIyy);
		
		_aircraftInertiaMomentIzz =
				fuselageInertiaMomentIzz
				.plus(wingInertiaMomentIzz)
				.plus(hTailInertiaMomentIzz)
				.plus(vTailInertiaMomentIzz)
				.plus(canardInertiaMomentIzz)
				.plus(nacellesInertiaMomentIzz)
				.plus(powerPlantInertiaMomentIzz)
				.plus(landingGearsInertiaMomentIzz)
				.plus(apuInertiaMomentIzz)
				.plus(airConditioningAndAntiIcingSystemInertiaMomentIzz)
				.plus(instrumentsAndNavigationSystemInertiaMomentIzz)
				.plus(electricalSystemInertiaMomentIzz)
				.plus(hydraulicAndPneumaticSystemsInertiaMomentIzz)
				.plus(controlSurfacesInertiaMomentIzz)
				.plus(furnishingsAndEquipmentsInertiaMomentIzz)
				.plus(payloadInertiaMomentIzz)
				.plus(fuelTankInertiaMomentIzz);
		
	}
	
	private void calculateAircraftInertiaProducts() {
		
		//------------------------------------------------------------------------------------------------
		// DATA INITIALIZATION
		Amount<?> fuselageInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuselageInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuselageInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> wingInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> wingInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> wingInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hTailInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hTailInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hTailInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> vTailInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> vTailInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> vTailInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> canardInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> canardInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> canardInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> nacellesInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> nacellesInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> nacellesInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> powerPlantInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> powerPlantInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> powerPlantInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> landingGearsInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> landingGearsInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> landingGearsInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> apuInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> apuInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> apuInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> airConditioningAndAntiIcingSystemInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> airConditioningAndAntiIcingSystemInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> airConditioningAndAntiIcingSystemInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> instrumentsAndNavigationSystemInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> instrumentsAndNavigationSystemInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> instrumentsAndNavigationSystemInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> electricalSystemInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> electricalSystemInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> electricalSystemInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hydraulicAndPneumaticSystemsInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hydraulicAndPneumaticSystemsInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> hydraulicAndPneumaticSystemsInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> controlSurfacesInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> controlSurfacesInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> controlSurfacesInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> furnishingsAndEquipmentsInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> furnishingsAndEquipmentsInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> furnishingsAndEquipmentsInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> payloadInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> payloadInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> payloadInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuelTankInertiaProductIxy = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuelTankInertiaProductIyz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		Amount<?> fuelTankInertiaProductIxz = Amount.valueOf(0.0, MyUnits.KILOGRAM_METER_SQUARED);
		
		//------------------------------------------------------------------------------------------------
		// FUSELAGE
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			fuselageInertiaProductIxy = InertiaContributionsCalc.calculateComponentInertiaProductIxy(
					_theBalanceManagerInterface.getFuselageMass(),
					_xCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getXBRF(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF()
					);
			fuselageInertiaProductIyz = InertiaContributionsCalc.calculateComponentInertiaProductIyz(
					_theBalanceManagerInterface.getFuselageMass(),
					Amount.valueOf(0.0, SI.METER), _cgMaximumTakeOffMass.getYBRF(),
					_zCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getZBRF()
					);
			fuselageInertiaProductIxz = InertiaContributionsCalc.calculateComponentInertiaProductIxz(
					_theBalanceManagerInterface.getFuselageMass(),
					_xCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getXBRF(),
					_zCGMap.get(ComponentEnum.FUSELAGE), _cgMaximumTakeOffMass.getZBRF()
					);
		}

		// TODO: CONTINUE
		
		//------------------------------------------------------------------------------------------------
		// GLOBAL INERTIA MOMENT CALCULATION
		_aircraftInertiaProductIxy = 
				fuselageInertiaProductIxy
				.plus(wingInertiaProductIxy)
				.plus(hTailInertiaProductIxy)
				.plus(vTailInertiaProductIxy)
				.plus(canardInertiaProductIxy)
				.plus(nacellesInertiaProductIxy)
				.plus(powerPlantInertiaProductIxy)
				.plus(landingGearsInertiaProductIxy)
				.plus(apuInertiaProductIxy)
				.plus(airConditioningAndAntiIcingSystemInertiaProductIxy)
				.plus(instrumentsAndNavigationSystemInertiaProductIxy)
				.plus(electricalSystemInertiaProductIxy)
				.plus(hydraulicAndPneumaticSystemsInertiaProductIxy)
				.plus(controlSurfacesInertiaProductIxy)
				.plus(furnishingsAndEquipmentsInertiaProductIxy)
				.plus(payloadInertiaProductIxy)
				.plus(fuelTankInertiaProductIxy);
				
		_aircraftInertiaProductIyz =
				fuselageInertiaProductIyz
				.plus(wingInertiaProductIyz)
				.plus(hTailInertiaProductIyz)
				.plus(vTailInertiaProductIyz)
				.plus(canardInertiaProductIyz)
				.plus(nacellesInertiaProductIyz)
				.plus(powerPlantInertiaProductIyz)
				.plus(landingGearsInertiaProductIyz)
				.plus(apuInertiaProductIyz)
				.plus(airConditioningAndAntiIcingSystemInertiaProductIyz)
				.plus(instrumentsAndNavigationSystemInertiaProductIyz)
				.plus(electricalSystemInertiaProductIyz)
				.plus(hydraulicAndPneumaticSystemsInertiaProductIyz)
				.plus(controlSurfacesInertiaProductIyz)
				.plus(furnishingsAndEquipmentsInertiaProductIyz)
				.plus(payloadInertiaProductIyz)
				.plus(fuelTankInertiaProductIyz);
		
		_aircraftInertiaProductIxz =
				fuselageInertiaProductIxz
				.plus(wingInertiaProductIxz)
				.plus(hTailInertiaProductIxz)
				.plus(vTailInertiaProductIxz)
				.plus(canardInertiaProductIxz)
				.plus(nacellesInertiaProductIxz)
				.plus(powerPlantInertiaProductIxz)
				.plus(landingGearsInertiaProductIxz)
				.plus(apuInertiaProductIxz)
				.plus(airConditioningAndAntiIcingSystemInertiaProductIxz)
				.plus(instrumentsAndNavigationSystemInertiaProductIxz)
				.plus(electricalSystemInertiaProductIxz)
				.plus(hydraulicAndPneumaticSystemsInertiaProductIxz)
				.plus(controlSurfacesInertiaProductIxz)
				.plus(furnishingsAndEquipmentsInertiaProductIxz)
				.plus(payloadInertiaProductIxz)
				.plus(fuelTankInertiaProductIxz);
	}
	
	//............................................................................
	// GETTERS & SETTERS:
	//............................................................................
	public IACBalanceManager getTheBalanceManagerInterface() {
		return _theBalanceManagerInterface;
	}

	public void setTheBalanceManagerInterface(IACBalanceManager _theBalanceManagerInterface) {
		this._theBalanceManagerInterface = _theBalanceManagerInterface;
	}

	public CenterOfGravity getCGStructure() {
		return _cgStructure;
	}

	public void setCGStructure(CenterOfGravity _cgStructure) {
		this._cgStructure = _cgStructure;
	}

	public CenterOfGravity getCGStructureAndPower() {
		return _cgStructureAndPower;
	}

	public void setCGStructureAndPower(CenterOfGravity _cgStructureAndPower) {
		this._cgStructureAndPower = _cgStructureAndPower;
	}

	public Double getMaxForwardOperativeCG() {
		return _maxForwardOperativeCG;
	}

	public void setMaxForwardOperativeCG(Double _maxForwardCG) {
		this._maxForwardOperativeCG = _maxForwardCG;
	}

	public Map<ComponentEnum, Amount<Length>> getXCGMap() {
		return _xCGMap;
	}

	public void setXCGMap(Map<ComponentEnum, Amount<Length>> _xCGMap) {
		this._xCGMap = _xCGMap;
	}

	public Map<ComponentEnum, Amount<Length>> getZCGMap() {
		return _zCGMap;
	}

	public void setZCGMap(Map<ComponentEnum, Amount<Length>> _zCGMap) {
		this._zCGMap = _zCGMap;
	}

	public Map<ComponentEnum, Amount<Mass>> getMassMap() {
		return _massMap;
	}

	public void setMassMap(Map<ComponentEnum, Amount<Mass>> _massMap) {
		this._massMap = _massMap;
	}

	public CenterOfGravity getCGManufacturerEmptuMass() {
		return _cgManufacturerEmptuMass;
	}

	public void setCGManufacturerEmptuMass(CenterOfGravity _cgManufacturerEmptuMass) {
		this._cgManufacturerEmptuMass = _cgManufacturerEmptuMass;
	}

	public CenterOfGravity getCGOperatingEmptyMass() {
		return _cgOperatingEmptyMass;
	}

	public void setCGOperatingEmptyMass(CenterOfGravity _cgOperatingEmptyMass) {
		this._cgOperatingEmptyMass = _cgOperatingEmptyMass;
	}

	public CenterOfGravity getCGMaximumZeroFuelMass() {
		return _cgMaximumZeroFuelMass;
	}

	public void setCGMaximumZeroFuelMass(CenterOfGravity _cgMaximumZeroFuelMass) {
		this._cgMaximumZeroFuelMass = _cgMaximumZeroFuelMass;
	}

	public CenterOfGravity getCGMaximumTakeOffMass() {
		return _cgMaximumTakeOffMass;
	}

	public void setCGMaximumTakeOffMass(CenterOfGravity _cgMaximumTakeOffMass) {
		this._cgMaximumTakeOffMass = _cgMaximumTakeOffMass;
	}

	public Double getMaxAftCG() {
		return _maxAftCG;
	}

	public void setMaxAftCG(Double _maxAftCG) {
		this._maxAftCG = _maxAftCG;
	}

	public Double getMaxForwardCG() {
		return _maxForwardCG;
	}

	public void setMaxForwardCG(Double _maxForwardCG) {
		this._maxForwardCG = _maxForwardCG;
	}

	public Amount<?> getAircraftInertiaMomentIxx() {
		return _aircraftInertiaMomentIxx;
	}

	public void setAircraftInertiaMomentIxx(Amount<?> _aircraftInertiaMomentIxx) {
		this._aircraftInertiaMomentIxx = _aircraftInertiaMomentIxx;
	}

	public Amount<?> getAircraftInertiaMomentIyy() {
		return _aircraftInertiaMomentIyy;
	}

	public void setAircraftInertiaMomentIyy(Amount<?> _aircraftInertiaMomentIyy) {
		this._aircraftInertiaMomentIyy = _aircraftInertiaMomentIyy;
	}

	public Amount<?> getAircraftInertiaMomentIzz() {
		return _aircraftInertiaMomentIzz;
	}

	public void setAircraftInertiaMomentIzz(Amount<?> _aircraftInertiaMomentIzz) {
		this._aircraftInertiaMomentIzz = _aircraftInertiaMomentIzz;
	}

	public Amount<?> getAircraftInertiaProductIxy() {
		return _aircraftInertiaProductIxy;
	}

	public void setAircraftInertiaProductIxy(Amount<?> _aircraftInertiaProductIxy) {
		this._aircraftInertiaProductIxy = _aircraftInertiaProductIxy;
	}

	public Amount<?> getAircraftInertiaProductIyz() {
		return _aircraftInertiaProductIyz;
	}

	public void setAircraftInertiaProductIyz(Amount<?> _aircraftInertiaProductIyz) {
		this._aircraftInertiaProductIyz = _aircraftInertiaProductIyz;
	}

	public Amount<?> getAircraftInertiaProductIxz() {
		return _aircraftInertiaProductIxz;
	}

	public void setAircraftInertiaProductIxz(Amount<?> _aircraftInertiaProductIxz) {
		this._aircraftInertiaProductIxz = _aircraftInertiaProductIxz;
	}
	
}
package analyses;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.MyConfiguration;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.customdata.CenterOfGravity;

/**
 * Manage the calculations for estimating the aircraft balance.
 * 
 * @author Vittorio Trifari
 *
 */
public class ACBalanceManager {

	/*
	 * TODO : FIX THE toXLS METHOD
	 * 		  FIX THE CALCULATE TOTOAL CG METHOD
	 */
	
	
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
	private CenterOfGravity _cgStructurePowerAndSystems;
	private CenterOfGravity _cgOEM;
	private CenterOfGravity _cgMZFM;
	private List<CenterOfGravity> _cgList;
	private CenterOfGravity _cgMTOM;
	private Double _maxAftCG;
	private Double _maxForwardCG;

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
		else
			weightsFromPreviousAnalysisFlag = Boolean.FALSE;

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
		Amount<Mass> frontLandingGearsMass = null;
		Amount<Mass> mainLandingGearsMass = null;
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
							frontLandingGearsMass = theAircraft.getLandingGears().getTheWeigths().getFrontGearMassEstimated().to(SI.KILOGRAM);
							mainLandingGearsMass = theAircraft.getLandingGears().getTheWeigths().getMainGearMassEstimated().to(SI.KILOGRAM);
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
			// FRONT LANDING GEARS MASS
			String frontLandingGearsMassProperty = reader.getXMLPropertyByPath("//weights/front_landing_gears_mass");
			if(frontLandingGearsMassProperty != null)
				frontLandingGearsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/front_landing_gears_mass");
			
			//---------------------------------------------------------------
			// MAIN LANDING GEARS MASS
			String mainLandingGearsMassProperty = reader.getXMLPropertyByPath("//weights/main_landing_gears_mass");
			if(mainLandingGearsMassProperty != null)
				mainLandingGearsMass = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//weights/main_landing_gears_mass");
			
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

			Optional<Fuselage> fuselage = Optional.of(theAircraft.getFuselage());
			Optional<LiftingSurface> wing = Optional.of(theAircraft.getWing());
			Optional<LiftingSurface> hTail = Optional.of(theAircraft.getHTail());
			Optional<LiftingSurface> vTail = Optional.of(theAircraft.getVTail());
			Optional<LiftingSurface> canard = Optional.of(theAircraft.getCanard());
			
			if(fuselage.isPresent()) {
				apuPositionX = fuselage.get().getXApexConstructionAxes().to(SI.METER)
					.plus(fuselage.get().getNoseLength().to(SI.METER))
					.plus(fuselage.get().getCylinderLength().to(SI.METER))
					.plus(fuselage.get().getTailLength().to(SI.METER).times(0.25)
					);
				apuPositionZ = fuselage.get().getZApexConstructionAxes().to(SI.METER);
			}

			if(wing.isPresent()) {
				airConditioningAndAntiIcingSystemPositionX = wing.get().getXApexConstructionAxes().to(SI.METER)
						.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.25));
				airConditioningAndAntiIcingSystemPositionZ = wing.get().getZApexConstructionAxes().to(SI.METER); 
			}
			
			if(fuselage.isPresent()) {
				instrumentsAndNavigationSystemPositionX = fuselage.get().getXApexConstructionAxes().to(SI.METER)
						.plus(fuselage.get().getNoseLength().to(SI.METER).times(0.5));
				instrumentsAndNavigationSystemPositionZ = fuselage.get().getZApexConstructionAxes().to(SI.METER);
			}

			if(wing.isPresent() && hTail.isPresent() && vTail.isPresent() && canard.isPresent()) {
				hydraulicAndPneumaticSystemsPositionX = (
						( (wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(hTail.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (vTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(vTail.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(verticalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (canard.get().getXApexConstructionAxes().to(SI.METER)
								.plus(canard.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(canardMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								+ canardMass.doubleValue(SI.KILOGRAM)
								);
				hydraulicAndPneumaticSystemsPositionZ = (
						( (wing.get().getZApexConstructionAxes().to(SI.METER) )
								.times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (vTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(verticalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (canard.get().getZApexConstructionAxes().to(SI.METER) )
								.times(canardMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								+ canardMass.doubleValue(SI.KILOGRAM)
								);
			}
			else if(wing.isPresent() && hTail.isPresent() && vTail.isPresent() && !canard.isPresent()) {
				hydraulicAndPneumaticSystemsPositionX = (
						( (wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(hTail.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (vTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(vTail.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(verticalTailMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								);
				hydraulicAndPneumaticSystemsPositionZ = (
						( (wing.get().getZApexConstructionAxes().to(SI.METER) )
								.times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (vTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(verticalTailMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								);
			}
			else if(wing.isPresent() && hTail.isPresent() && !vTail.isPresent() && !canard.isPresent()) {
				hydraulicAndPneumaticSystemsPositionX = (
						( (wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(hTail.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5))
								).times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								);
				hydraulicAndPneumaticSystemsPositionZ = (
						( (wing.get().getZApexConstructionAxes().to(SI.METER) )
								.times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								);
			}
			else if(wing.isPresent() && !hTail.isPresent() && !vTail.isPresent() && !canard.isPresent()) {
				hydraulicAndPneumaticSystemsPositionX = wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.5));
				hydraulicAndPneumaticSystemsPositionZ = wing.get().getZApexConstructionAxes().to(SI.METER);
			}
			else if (fuselage.isPresent()) {
				hydraulicAndPneumaticSystemsPositionX = fuselage.get().getXApexConstructionAxes().to(SI.METER)
						.plus(fuselage.get().getNoseLength().to(SI.METER))
						.plus(fuselage.get().getCylinderLength().to(SI.METER).times(0.5));
				hydraulicAndPneumaticSystemsPositionZ = fuselage.get().getZApexConstructionAxes().to(SI.METER); 
			}
			
			if(fuselage.isPresent() && wing.isPresent()) {
				electricalSystemsPositionX = (
						(fuselage.get().getXApexConstructionAxes().to(SI.METER)
								.plus(fuselage.get().getNoseLength().to(SI.METER))
								)
						.plus(wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.25))
								)
						).divide(2);
				electricalSystemsPositionZ = ( 
						(fuselage.get().getZApexConstructionAxes().to(SI.METER)
								.minus(fuselage.get().getSectionCylinderHeight().to(SI.METER).times(0.25))
								)
						.plus(wing.get().getZApexConstructionAxes().to(SI.METER))
						).divide(2); 
			}
			else if(fuselage.isPresent() && !wing.isPresent()) {
				electricalSystemsPositionX = 
						fuselage.get().getXApexConstructionAxes().to(SI.METER)
								.plus(fuselage.get().getNoseLength().to(SI.METER));
				electricalSystemsPositionZ = 
						fuselage.get().getZApexConstructionAxes().to(SI.METER)
								.minus(fuselage.get().getSectionCylinderHeight().to(SI.METER).times(0.25));
			}
			else if(!fuselage.isPresent() && wing.isPresent()) {
				electricalSystemsPositionX = 
						wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getPanels().get(0).getChordRoot().to(SI.METER).times(0.25));
				electricalSystemsPositionZ = wing.get().getZApexConstructionAxes().to(SI.METER);
			} 
			
			if(wing.isPresent() && hTail.isPresent() && vTail.isPresent() && canard.isPresent()) {
				controlSurfacesPositionX = (
						( (wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(wing.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								).times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( (hTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(hTail.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(hTail.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								) )
						.plus( (vTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(vTail.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(vTail.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								) )
						.plus( (canard.get().getXApexConstructionAxes().to(SI.METER)
								.plus(canard.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(canard.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								+ canardMass.doubleValue(SI.KILOGRAM)
								);
				controlSurfacesPositionZ = (
						( (wing.get().getZApexConstructionAxes().to(SI.METER) )
								.times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (vTail.get().getXApexConstructionAxes().to(SI.METER) )
								.times(verticalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (canard.get().getXApexConstructionAxes().to(SI.METER) )
								.times(canardMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								+ canardMass.doubleValue(SI.KILOGRAM)
								);
			}
			else if(wing.isPresent() && hTail.isPresent() && vTail.isPresent() && !canard.isPresent()) {
				controlSurfacesPositionX = (
						( (wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(wing.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								).times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( (hTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(hTail.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(hTail.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								) )
						.plus( (vTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(vTail.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(vTail.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								);
				controlSurfacesPositionZ = (
						( (wing.get().getZApexConstructionAxes().to(SI.METER) )
								.times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						.plus( ( (vTail.get().getXApexConstructionAxes().to(SI.METER) )
								.times(verticalTailMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								+ verticalTailMass.doubleValue(SI.KILOGRAM)
								);
			}
			else if(wing.isPresent() && hTail.isPresent() && !vTail.isPresent() && !canard.isPresent()) {
				controlSurfacesPositionX = (
						( (wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(wing.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								).times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( (hTail.get().getXApexConstructionAxes().to(SI.METER)
								.plus(hTail.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(hTail.get().getMeanAerodynamicChord().to(SI.METER).times(0.7))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								);
				controlSurfacesPositionZ = (
						( (wing.get().getZApexConstructionAxes().to(SI.METER) )
								.times(wingMass.doubleValue(SI.KILOGRAM))
								)
						.plus( ( (hTail.get().getZApexConstructionAxes().to(SI.METER) )
								.times(horizontalTailMass.doubleValue(SI.KILOGRAM))
								) )
						).divide(
								wingMass.doubleValue(SI.KILOGRAM)
								+ horizontalTailMass.doubleValue(SI.KILOGRAM)
								);
			}
			else if(wing.isPresent() && !hTail.isPresent() && !vTail.isPresent() && !canard.isPresent()) {
				controlSurfacesPositionX = wing.get().getXApexConstructionAxes().to(SI.METER)
								.plus(wing.get().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER))
								.plus(wing.get().getMeanAerodynamicChord().to(SI.METER).times(0.7));
				controlSurfacesPositionZ = wing.get().getZApexConstructionAxes().to(SI.METER);
			}
			else if (fuselage.isPresent()) {
				controlSurfacesPositionX = fuselage.get().getXApexConstructionAxes().to(SI.METER)
						.plus(fuselage.get().getNoseLength().to(SI.METER))
						.plus(fuselage.get().getCylinderLength().to(SI.METER).times(0.5));
				controlSurfacesPositionZ = fuselage.get().getZApexConstructionAxes().to(SI.METER); 
			}
			
			if (fuselage.isPresent()) {
				furnishingsAndEquipmentsPositionX = fuselage.get().getXApexConstructionAxes().to(SI.METER)
						.plus(fuselage.get().getNoseLength().to(SI.METER))
						.plus(fuselage.get().getCylinderLength().to(SI.METER).times(0.5));
				furnishingsAndEquipmentsPositionZ = fuselage.get().getZApexConstructionAxes().to(SI.METER)
						.plus(fuselage.get().getSectionCylinderHeight().to(SI.METER).times(0.25));
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
				.setFrontLandingGearMass(frontLandingGearsMass)
				.setMainLandingGearMass(mainLandingGearsMass)
				.setAPUMass(apuMass)
				.setAirConditioningAndAntiIcingMass(airConditioningAndAntiIcingSystemMass)
				.setInstrumentsAndNavigationSystemMass(instrumentsAndNavigationSystemMass)
				.setHydraulicAndPneumaticSystemsMass(hydraulicAndPneumaticSystemsMass)
				.setElectricalSystemsMass(electricalSystemsMass)
				.setControlSurfacesMass(controlSurfacesMass)
				.setFurnishingsAndEquipmentsMass(furnishingsAndEquipmentsMass)
				.setStandardSystemsPositionFlag(standardSystemsPositionFlag)
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
				.append("\tXcg structure MAC: " + _cgStructure.getXMAC()*100 + "\n")
				.append("\tXcg structure BRF: " + _cgStructure.getXBRF().to(SI.METER) + "\n")
				.append("\tZcg structure MAC: " + _cgStructure.getZMAC()*100 + "\n")
				.append("\tZcg structure BRF: " + _cgStructure.getZBRF().to(SI.METER) + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg structure and engines MAC: " + _cgStructureAndPower.getXMAC()*100 + "\n")
				.append("\tXcg structure and engines BRF: " + _cgStructureAndPower.getXBRF().to(SI.METER) + "\n")
				.append("\tZcg structure and engines MAC: " + _cgStructureAndPower.getZMAC()*100 + "\n")
				.append("\tZcg structure and engines BRF: " + _cgStructureAndPower.getZBRF().to(SI.METER) + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg structure, engines and systems MAC: " + _cgStructurePowerAndSystems.getXMAC()*100 + "\n")
				.append("\tXcg structure, engines and systems BRF: " + _cgStructurePowerAndSystems.getXBRF().to(SI.METER) + "\n")
				.append("\tZcg structure, engines and systems MAC: " + _cgStructurePowerAndSystems.getZMAC()*100 + "\n")
				.append("\tZcg structure, engines and systems BRF: " + _cgStructurePowerAndSystems.getZBRF().to(SI.METER) + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg operating empty mass MAC: " + _cgOEM.getXMAC()*100 + "\n")
				.append("\tXcg operating empty mass BRF: " + _cgOEM.getXBRF().to(SI.METER) + "\n")
				.append("\tZcg operating empty mass MAC: " + _cgOEM.getZMAC()*100 + "\n")
				.append("\tZcg operating empty mass BRF: " + _cgOEM.getZBRF().to(SI.METER) + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg maximum zero fuel mass MAC: " + _cgMZFM.getXMAC()*100 + "\n")
				.append("\tXcg maximum zero fuel mass BRF: " + _cgMZFM.getXBRF().to(SI.METER) + "\n")
				.append("\tZcg maximum zero fuel mass MAC: " + _cgMZFM.getZMAC()*100 + "\n")
				.append("\tZcg maximum zero fuel mass BRF: " + _cgMZFM.getZBRF().to(SI.METER) + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tXcg maximum take-off mass MAC: " + _cgMTOM.getXMAC()*100 + "\n")
				.append("\tXcg maximum take-off mass BRF: " + _cgMTOM.getXBRF().to(SI.METER) + "\n")
				.append("\tZcg maximum take-off mass MAC: " + _cgMTOM.getZMAC()*100 + "\n")
				.append("\tZcg maximum take-off mass BRF: " + _cgMTOM.getZBRF().to(SI.METER) + "\n")
				.append("\tиииииииииииииииииииииииииииииииииииии\n")
				.append("\tMax aft Xcg MAC: " + _maxAftCG + "\n")
				.append("\tMax forward Xcg MAC: " + _maxForwardCG + "\n")
				.append("\t-------------------------------------\n")
				.append("\tCOMPONENTS:\n")
				.append("\t-------------------------------------\n");
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) {
			sb.append("\t\tXcg Fuselage MAC: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Fuselage BRF: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Fuselage MAC: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Fuselage BRF: " + _theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) {
			sb.append("\t\tXcg Wing MAC: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Wing BRF: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Wing MAC: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Wing BRF: " + _theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) {
			sb.append("\t\tXcg Horizontal Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Horizontal Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Horizontal Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Horizontal Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) {
			sb.append("\t\tXcg Vertical Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Vertical Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Vertical Tail MAC: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Vertical Tail BRF: " + _theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) {
			sb.append("\t\tXcg Canard MAC: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Canard BRF: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Canard MAC: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Canard BRF: " + _theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) {
			sb.append("\t\tXcg Nacelles MAC: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Nacelles BRF: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Nacelles MAC: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Nacelles BRF: " + _theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getTotalCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant() != null) {
			sb.append("\t\tXcg Power Plant MAC: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Power Plant BRF: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Power Plant MAC: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Power Plant BRF: " + _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) {
			sb.append("\t\tXcg Total Landing Gear MAC: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXMAC()*100 + "\n")
			.append("\t\tXcg Total Landing Gear BRF: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getXBRF().to(SI.METER) + "\n")
			.append("\t\tZcg Total Landing Gear MAC: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZMAC()*100 + "\n")
			.append("\t\tZcg Total Landing Gear BRF: " + _theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG().getZBRF().to(SI.METER) + "\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n");
		}
		if(_theBalanceManagerInterface.getTheAircraft().getSystems() != null) {
			sb.append("\t\tSYSTEMS AND EQUIPMENTS:\n")
			.append("\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg APU BRF: " + _theBalanceManagerInterface.getAPUPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg APU MAC: " + _theBalanceManagerInterface.getAPUPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg APU BRF: " + _theBalanceManagerInterface.getAPUPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg APU MAC: " + _theBalanceManagerInterface.getAPUPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Air Conditioning And Anti-Icing System BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Air Conditioning And Anti-Icing System MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg APU BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg APU MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Air Conditioning And Anti-Icing System BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Air Conditioning And Anti-Icing System MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg Air Conditioning And Anti-Icing System BRF: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg Air Conditioning And Anti-Icing System MAC: " + _theBalanceManagerInterface.getAirConditioningAndAntiIcingSystemPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Instruments And Navigation System BRF: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Instruments And Navigation System MAC: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg Instruments And Navigation System BRF: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg Instruments And Navigation System MAC: " + _theBalanceManagerInterface.getInstrumentsAndNavigationSystemPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Hydraulic And Pneumatic Systems BRF: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Hydraulic And Pneumatic Systems MAC: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg Hydraulic And Pneumatic Systems BRF: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg Hydraulic And Pneumatic Systems MAC: " + _theBalanceManagerInterface.getHydraulicAndPneumaticSystemsPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Electrical Systems BRF: " + _theBalanceManagerInterface.getElectricalSystemsPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Electrical Systems MAC: " + _theBalanceManagerInterface.getElectricalSystemsPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg Electrical Systems BRF: " + _theBalanceManagerInterface.getElectricalSystemsPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg Electrical Systems MAC: " + _theBalanceManagerInterface.getElectricalSystemsPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Control Surfaces BRF: " + _theBalanceManagerInterface.getControlSurfacesPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Control Surfaces MAC: " + _theBalanceManagerInterface.getControlSurfacesPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg Control Surfaces BRF: " + _theBalanceManagerInterface.getControlSurfacesPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg Control Surfaces MAC: " + _theBalanceManagerInterface.getControlSurfacesPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n")
			.append("\t\t\tXcg Furnishings And Equipments BRF: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionX().to(SI.METER) + "\n")
			.append("\t\t\tXcg Furnishings And Equipments MAC: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionX().to(SI.METER)
					.minus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER)
							.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)))
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tZcg Furnishings And Equipments BRF: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionZ().to(SI.METER) + "\n")
			.append("\t\t\tZcg Furnishings And Equipments MAC: " + _theBalanceManagerInterface.getFurnishingsAndEquipmentsPositionZ().to(SI.METER)
					.divide(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().doubleValue(SI.METER)) + "\n")
			.append("\t\t\tиииииииииииииииииииииииииииииииииииии\n");
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
		dataListGlobal.add(new Object[] {"Xcg structure MAC","%", _cgStructure.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Xcg structure BRF","m", _cgStructure.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg structure MAC","%", _cgStructure.getZMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg structure BRF","m", _cgStructure.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg structure and engines MAC","%", _cgStructureAndPower.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Xcg structure and engines BRF","m", _cgStructureAndPower.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg structure and engines MAC","%", _cgStructureAndPower.getZMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg structure and engines BRF","m", _cgStructureAndPower.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg operating empty mass MAC","%", _cgOEM.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Xcg operating empty mass BRF","m", _cgOEM.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg operating empty mass MAC","%", _cgOEM.getZMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg operating empty mass BRF","m", _cgOEM.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg maximum zero fuel mass MAC","%",_cgMZFM.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Xcg maximum zero fuel mass BRF","m",_cgMZFM.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg maximum zero fuel mass MAC","%",_cgMZFM.getZMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg maximum zero fuel mass BRF","m", _cgMZFM.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Xcg maximum take-off mass MAC","%",_cgMTOM.getXMAC()*100});
		dataListGlobal.add(new Object[] {"Xcg maximum take-off mass BRF","m",_cgMTOM.getXBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {"Zcg maximum take-off mass MAC","%",_cgMTOM.getZMAC()*100});
		dataListGlobal.add(new Object[] {"Zcg maximum take-off mass BRF","m", _cgMTOM.getZBRF().doubleValue(SI.METER)});
		dataListGlobal.add(new Object[] {" "});
		dataListGlobal.add(new Object[] {"Max forward Xcg MAC","%",_maxForwardCG});
		dataListGlobal.add(new Object[] {"Max aft Xcg MAC","%",_maxAftCG});
		
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
		if(getTheBalanceManagerInterface().getTheAircraft().getFuselage() != null) {
			Sheet sheetFuselage = wb.createSheet("FUSELAGE");
			List<Object[]> dataListFuselage = new ArrayList<>();
			dataListFuselage.add(new Object[] {"Description","Unit","Value"});
			dataListFuselage.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getCG().getXLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Ycg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getCG().getYLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getCG().getZLRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getCG().getXBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Ycg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getCG().getYBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getCG().getZBRF().doubleValue(SI.METER)});
			dataListFuselage.add(new Object[] {" "});
			dataListFuselage.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getXCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getXCGMap().get(methods) != null) 
					dataListFuselage.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getFuselage().getTheBalance().getXCGMap().get(methods).doubleValue(SI.METER),
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
		if(getTheBalanceManagerInterface().getTheAircraft().getWing() != null) {
			Sheet sheetWing = wb.createSheet("WING");
			List<Object[]> dataListWing = new ArrayList<>();
			dataListWing.add(new Object[] {"Description","Unit","Value"});
			dataListWing.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Ycg LRF (semi-wing)","m", getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Ycg BRF (semi-wing)","m", getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getXCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListWing.add(new Object[] {" "});
			dataListWing.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getYCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListWing.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getWing().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
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
		if(getTheBalanceManagerInterface().getTheAircraft().getFuelTank() != null) {
			Sheet sheetFuelTank = wb.createSheet("FUEL TANK");
			List<Object[]> dataListFuelTank = new ArrayList<>();
			dataListFuelTank.add(new Object[] {"Description","Unit","Value"});
			dataListFuelTank.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getXCGLRF().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Ycg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getYCGLRF().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getZCGLRF().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {" "});
			dataListFuelTank.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getXCG().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Ycg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getYCG().doubleValue(SI.METER)});
			dataListFuelTank.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getFuelTank().getZCG().doubleValue(SI.METER)});
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
		if(getTheBalanceManagerInterface().getTheAircraft().getHTail() != null) {
			Sheet sheetHTail = wb.createSheet("HORIZONTAL TAIL");
			List<Object[]> dataListHTail = new ArrayList<>();
			dataListHTail.add(new Object[] {"Description","Unit","Value"});
			dataListHTail.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Ycg LRF (semi-tail)","m", getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Ycg BRF (semi-tail)","m", getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getXCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListHTail.add(new Object[] {" "});
			dataListHTail.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getYCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListHTail.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getHTail().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
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
		if(getTheBalanceManagerInterface().getTheAircraft().getVTail() != null) {
			Sheet sheetVTail = wb.createSheet("VERTICAL TAIL");
			List<Object[]> dataListVTail = new ArrayList<>();
			dataListVTail.add(new Object[] {"Description","Unit","Value"});
			dataListVTail.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Ycg LRF (semi-tail)","m", getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Ycg BRF (semi-tail)","m", getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getXCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListVTail.add(new Object[] {" "});
			dataListVTail.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getYCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListVTail.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getVTail().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
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
		if(getTheBalanceManagerInterface().getTheAircraft().getCanard() != null) {
			Sheet sheetCanard = wb.createSheet("CANARD");
			List<Object[]> dataListCanard = new ArrayList<>();
			dataListCanard.add(new Object[] {"Description","Unit","Value"});
			dataListCanard.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getCG().getXLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Ycg LRF (semi-canard)","m", getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getCG().getYLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getCG().getZLRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getCG().getXBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Ycg BRF (semi-canard)","m", getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getCG().getYBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getCG().getZBRF().doubleValue(SI.METER)});
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Xcg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getXCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getXCGMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getXCGMap().get(methods).doubleValue(SI.METER),
							}
							);
			}
			dataListCanard.add(new Object[] {" "});
			dataListCanard.add(new Object[] {"Ycg ESTIMATION METHOD COMPARISON"});
			for(MethodEnum methods : getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getYCGMap().keySet()) {
				if(getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getYCGMap().get(methods) != null) 
					dataListCanard.add(
							new Object[] {
									methods.toString(),
									"m",
									getTheBalanceManagerInterface().getTheAircraft().getCanard().getTheBalanceManager().getYCGMap().get(methods).doubleValue(SI.METER),
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
		if(getTheBalanceManagerInterface().getTheAircraft().getNacelles() != null) {
			Sheet sheetNacelles = wb.createSheet("NACELLES");
			List<Object[]> dataListNacelles = new ArrayList<>();
			dataListNacelles.add(new Object[] {"Description","Unit","Value"});
			dataListNacelles.add(new Object[] {"BALANCE ESTIMATION FOR EACH NACELLE"});
			dataListNacelles.add(new Object[] {" "});
			for(int iNacelle = 0; iNacelle < getTheBalanceManagerInterface().getTheAircraft().getNacelles().getNacellesNumber(); iNacelle++) {
				dataListNacelles.add(new Object[] {"NACELLE " + (iNacelle+1)});
				dataListNacelles.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getNacelles().getTheBalance().getCGList().get(iNacelle).getXLRF().doubleValue(SI.METER)});
				dataListNacelles.add(new Object[] {"Ycg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getNacelles().getTheBalance().getCGList().get(iNacelle).getYLRF().doubleValue(SI.METER)});
				dataListNacelles.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getNacelles().getTheBalance().getCGList().get(iNacelle).getZLRF().doubleValue(SI.METER)});
				dataListNacelles.add(new Object[] {" "});
				dataListNacelles.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getNacelles().getTheBalance().getCGList().get(iNacelle).getXBRF().doubleValue(SI.METER)});
				dataListNacelles.add(new Object[] {"Ycg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getNacelles().getTheBalance().getCGList().get(iNacelle).getYBRF().doubleValue(SI.METER)});
				dataListNacelles.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getNacelles().getTheBalance().getCGList().get(iNacelle).getZBRF().doubleValue(SI.METER)});
				dataListNacelles.add(new Object[] {" "});
				dataListNacelles.add(new Object[] {" "});
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
		// POWER PLANT BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(getTheBalanceManagerInterface().getTheAircraft().getPowerPlant() != null) {
			Sheet sheetPowerPlant = wb.createSheet("POWER PLANT");
			List<Object[]> dataListPowerPlant = new ArrayList<>();
			dataListPowerPlant.add(new Object[] {"Description","Unit","Value"});
			dataListPowerPlant.add(new Object[] {"BALANCE ESTIMATION FOR EACH ENGINE"});
			dataListPowerPlant.add(new Object[] {" "});
			for(int iEngines = 0; iEngines < getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getEngineNumber(); iEngines++) {
				dataListPowerPlant.add(new Object[] {"ENGINE " + (iEngines+1)});
				dataListPowerPlant.add(new Object[] {"Xcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getTheBalance().getCGList().get(iEngines).getXLRF().doubleValue(SI.METER)});
				dataListPowerPlant.add(new Object[] {"Ycg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getTheBalance().getCGList().get(iEngines).getYLRF().doubleValue(SI.METER)});
				dataListPowerPlant.add(new Object[] {"Zcg LRF","m", getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getTheBalance().getCGList().get(iEngines).getZLRF().doubleValue(SI.METER)});
				dataListPowerPlant.add(new Object[] {" "});
				dataListPowerPlant.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getTheBalance().getCGList().get(iEngines).getXBRF().doubleValue(SI.METER)});
				dataListPowerPlant.add(new Object[] {"Ycg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getTheBalance().getCGList().get(iEngines).getYBRF().doubleValue(SI.METER)});
				dataListPowerPlant.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getPowerPlant().getTheBalance().getCGList().get(iEngines).getZBRF().doubleValue(SI.METER)});
				dataListPowerPlant.add(new Object[] {" "});	
				dataListPowerPlant.add(new Object[] {" "});	
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
		// LANDING GEARS BALANCE ANALYSIS RESULTS:
		//--------------------------------------------------------------------------------
		if(getTheBalanceManagerInterface().getTheAircraft().getLandingGears() != null) {
			Sheet sheetLandingGears = wb.createSheet("LANDING GEARS");
			List<Object[]> dataListLandingGears = new ArrayList<>();
			dataListLandingGears.add(new Object[] {"Description","Unit","Value"});
			dataListLandingGears.add(new Object[] {"Xcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getLandingGears().getTheBalance().getCG().getXBRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {"Ycg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getLandingGears().getTheBalance().getCG().getYBRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {"Zcg BRF","m", getTheBalanceManagerInterface().getTheAircraft().getLandingGears().getTheBalance().getCG().getZBRF().doubleValue(SI.METER)});
			dataListLandingGears.add(new Object[] {" "});

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
		// XLS FILE CREATION:
		//--------------------------------------------------------------------------------
		FileOutputStream fileOut = new FileOutputStream(filenameWithPathAndExt + ".xlsx");
		wb.write(fileOut);
		fileOut.close();
		System.out.println("Your excel file has been generated!");
	}
	
	private void initializeData() {
		
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

		int index = getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getSeatsCoGFrontToRear().size();
		Amount<Length> meanAerodynamicChordXle = getTheBalanceManagerInterface().getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX()
				.plus(getTheBalanceManagerInterface().getTheAircraft().getWing().getXApexConstructionAxes());
		Amount<Length> meanAerodynamicChord = getTheBalanceManagerInterface().getTheAircraft().getWing().getMeanAerodynamicChord();
		
		Double[] seatCoGFrontToRearReferToMAC = new Double[index];
		Double[] seatCoGRearToFrontReferToMAC = new Double[index];
		Double[] fuelCoGBeforeBoardingReferToMAC = new Double[2];
		Double[] massWithFuelBeforeBoarding = new Double[2];
		Double[] fuelCoGAfterBoardingReferToMAC = new Double[2];
		Double[] massWithFuelAfterBoarding = new Double[2];
		
		seatCoGFrontToRearReferToMAC[0] = (_cgOEM.getXMAC()*100);
		seatCoGRearToFrontReferToMAC[0] = (_cgOEM.getXMAC()*100);  
		
		for (int i=0; i<index; i++) {
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
		fuelCoGBeforeBoardingReferToMAC[0] = MyArrayUtils.getMax(seatCoGRearToFrontReferToMAC);
		int indexOfMax = MyArrayUtils.getIndexOfMax(seatCoGRearToFrontReferToMAC);
		massWithFuelBeforeBoarding[0] = 
				MyArrayUtils.convertListOfAmountToDoubleArray(
						getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getCurrentMassList()
						)[indexOfMax];
		fuelCoGBeforeBoardingReferToMAC[1] = ((((_cgOEM.getXBRF().to(SI.METER).times(getTheBalanceManagerInterface().getOperatingEmptyMass().to(SI.KILOGRAM)).getEstimatedValue())
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
		/**
		 * finding the beginning of the last column during boarding 
		 * (the last zero in the xList of rowColumnCoordinate)
		 */
		List<Integer> indexOfZeroList = new ArrayList<>();
		for(int i=0; 
				i<getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getSeatsBlocksList().get(0).getXList().size();
				i++) {
			if(getTheBalanceManagerInterface().getTheAircraft()
					.getCabinConfiguration()
						.getSeatsBlocksList().get(0)
							.getXList().get(i)
									== 0.0)
				indexOfZeroList.add(i);
		}
		
		/**
		 * the current seatCoGFrontToRear is the one starting from the last zero found
		 */
		List<Double> currentSeatCoGFrontToRear = new ArrayList<>();
		for(int i=indexOfZeroList.get(indexOfZeroList.size()-1);
				i<seatCoGFrontToRearReferToMAC.length;
				i++)
			currentSeatCoGFrontToRear.add(seatCoGFrontToRearReferToMAC[i]);
			
		Double[] currentSeatCoGFrontToRearArray = MyArrayUtils.convertListOfDoubleToDoubleArray(currentSeatCoGFrontToRear);
		
		/**
		 * The minimum value and its index has to be searched in the current list. 
		 * Then the index of the last zero is added in order to retrieve the real index of min
		 * of the last part of the boarding diagram
		 */
		fuelCoGAfterBoardingReferToMAC[0] = MyArrayUtils.getMin(currentSeatCoGFrontToRearArray);
		int indexOfMin = 
				MyArrayUtils.getIndexOfMin(currentSeatCoGFrontToRearArray)
				+ indexOfZeroList.get(indexOfZeroList.size()-1);
		massWithFuelAfterBoarding[0] = 
				MyArrayUtils.convertListOfAmountToDoubleArray(
						getTheBalanceManagerInterface().getTheAircraft().getCabinConfiguration().getCurrentMassList()
						)[indexOfMin];
		fuelCoGAfterBoardingReferToMAC[1] = (_cgMTOM.getXMAC()*100); 
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
		
		if(_theBalanceManagerInterface.getTheAircraft().getFuselage() != null) 
			_theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().calculateCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);
		
		if(_theBalanceManagerInterface.getTheAircraft().getWing() != null) 
			_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.WING, _methodsMapBalance);
		
		if(_theBalanceManagerInterface.getTheAircraft().getFuelTank() != null)
			_theBalanceManagerInterface.getTheAircraft().getFuelTank().calculateCG();
		
		if(_theBalanceManagerInterface.getTheAircraft().getHTail() != null) 
			_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.HORIZONTAL_TAIL, _methodsMapBalance);
		
		if(_theBalanceManagerInterface.getTheAircraft().getVTail() != null) 
			_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.VERTICAL_TAIL, _methodsMapBalance);
		
		if(_theBalanceManagerInterface.getTheAircraft().getCanard() != null) 
			_theBalanceManagerInterface.getTheAircraft().getCanard().getTheBalanceManager().calculateCG(_theBalanceManagerInterface.getTheAircraft(), ComponentEnum.CANARD, _methodsMapBalance);
		
		if(_theBalanceManagerInterface.getTheAircraft().getNacelles() != null) 
			_theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().calculateTotalCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);
		
		if(_theBalanceManagerInterface.getTheAircraft().getPowerPlant() != null) 
			_theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().calculateTotalCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);

		if(_theBalanceManagerInterface.getTheAircraft().getLandingGears() != null) 
			_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().calculateCG(_theBalanceManagerInterface.getTheAircraft(), _methodsMapBalance);

		calculateTotalCG();
	}

	/**
	 * Evaluate overall CG
	 * 
	 * @param aircraft
	 */
	public void calculateTotalCG() {

		// Structural CG
		_cgStructure = new CenterOfGravity();

		_cgList.add(_theBalanceManagerInterface.getTheAircraft().getFuselage().getTheBalance().getCG());
		_cgList.add(_theBalanceManagerInterface.getTheAircraft().getWing().getTheBalanceManager().getCG());
		_cgList.add(_theBalanceManagerInterface.getTheAircraft().getHTail().getTheBalanceManager().getCG());
		_cgList.add(_theBalanceManagerInterface.getTheAircraft().getVTail().getTheBalanceManager().getCG());
		_cgList.add(_theBalanceManagerInterface.getTheAircraft().getLandingGears().getTheBalance().getCG());
		_cgList.addAll(_theBalanceManagerInterface.getTheAircraft().getNacelles().getTheBalance().getCGList());
		
		List<Amount<Mass>> massStructureList = new ArrayList<Amount<Mass>>();
		if(_theBalanceManagerInterface.getFuselageMass() != null)
			massStructureList.add(_theBalanceManagerInterface.getFuselageMass());
		if(_theBalanceManagerInterface.getWingMass() != null)
			massStructureList.add(_theBalanceManagerInterface.getWingMass());
		if(_theBalanceManagerInterface.getHTailMass() != null)
			massStructureList.add(_theBalanceManagerInterface.getHTailMass());
		if(_theBalanceManagerInterface.getVTailMass() != null)
			massStructureList.add(_theBalanceManagerInterface.getVTailMass());
		if(_theBalanceManagerInterface.getCanardMass() != null)
			massStructureList.add(_theBalanceManagerInterface.getCanardMass());
		if(_theBalanceManagerInterface.getMainLandingGearMass() != null)
			massStructureList.add(_theBalanceManagerInterface.getMainLandingGearMass());
		if(_theBalanceManagerInterface.getNacellesMass() != null) 
			massStructureList.add(_theBalanceManagerInterface.getNacellesMass());
		
		double prodX = 0., prodZ = 0., sum = 0.;
		for (int i=0; i < _cgList.size(); i++) {

			prodX += _cgList.get(i).getXBRF().doubleValue(SI.METER)*massStructureList.get(i).doubleValue(SI.KILOGRAM);
			prodZ += _cgList.get(i).getZBRF().doubleValue(SI.METER)*massStructureList.get(i).doubleValue(SI.KILOGRAM);			
			sum += massStructureList.get(i).doubleValue(SI.KILOGRAM);

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

		// Structure + engines CG
		_cgStructureAndPower = new CenterOfGravity();
		
		double xCGPowerPlantContribute = _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getXBRF().doubleValue(SI.METER)
				*_theBalanceManagerInterface.getPowerPlantMass().doubleValue(SI.KILOGRAM);
		double zCGPowerPlantContribute = _theBalanceManagerInterface.getTheAircraft().getPowerPlant().getTheBalance().getTotalCG().getZBRF().doubleValue(SI.METER)
				*_theBalanceManagerInterface.getPowerPlantMass().doubleValue(SI.KILOGRAM);
		Amount<Mass> powerPlantMass = _theBalanceManagerInterface.getPowerPlantMass().to(SI.KILOGRAM);
		
		_cgStructureAndPower.setXBRF(
				Amount.valueOf(
						(xCGPowerPlantContribute+
								sum*_cgStructure.getXBRF().doubleValue(SI.METER))/
						(sum + powerPlantMass.doubleValue(SI.KILOGRAM)),
						SI.METER)
				);

		_cgStructureAndPower.setZBRF(
				Amount.valueOf(
						(zCGPowerPlantContribute+
								sum*_cgStructure.getZBRF().doubleValue(SI.METER))/
						(sum + powerPlantMass.doubleValue(SI.KILOGRAM)),
						SI.METER)
				);

		_cgStructureAndPower.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));

		// OEM CG location
		/**
		 * AT THIS POINT THE SUM OF ALL STRUCTURAL MASSES AND ENGINE MASSES IS NOT EQUAL TO THE 
		 * OPERATING EMPTY MASS. THE ASSUMPTION MADE IS THAT ALL THE COMPONENTS THAT HAVE TO BE 
		 * CONSEDERED IN ORDER TO REACH THE OPERATING EMPTY MASS (OPERATING ITEM MASS, ETC...)
		 * DO NOT AFFECT THE CG LOCATION.
		 */
		_cgOEM = new CenterOfGravity();
		_cgOEM.setXBRF(_cgStructureAndPower.getXBRF().to(SI.METER));
		_cgOEM.setZBRF(_cgStructureAndPower.getZBRF().to(SI.METER));
		_cgOEM.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));
		
		_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().buildSimpleLayout(_theBalanceManagerInterface.getTheAircraft());
		
		// MZFW CG location
		_cgMZFM = new CenterOfGravity();

		_cgMZFM.setXBRF(
				_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getSeatsCoGFrontToRear().get(
						_theBalanceManagerInterface.getTheAircraft().getCabinConfiguration().getSeatsCoGFrontToRear().size()-1
						)
				);
		
		_cgMZFM.setZBRF(Amount.valueOf(
				_cgStructureAndPower.getZBRF().doubleValue(SI.METER)*_theBalanceManagerInterface.getOperatingEmptyMass().doubleValue(SI.KILOGRAM)
						/(_payloadMass.doubleValue(SI.KILOGRAM) 
								+ _theBalanceManagerInterface.getOperatingEmptyMass().doubleValue(SI.KILOGRAM))
						, SI.METER));
		
		_cgMZFM.calculateCGinMAC(
				(_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER)
						.plus(_theBalanceManagerInterface.getTheAircraft().getWing().getXApexConstructionAxes().to(SI.METER))), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChordLeadingEdgeX().to(SI.METER), 
				Amount.valueOf(0., SI.METER), 
				_theBalanceManagerInterface.getTheAircraft().getWing().getMeanAerodynamicChord().to(SI.METER));

		// MTOM CG location
		_cgMTOM = new CenterOfGravity();

		_cgMTOM.setXBRF(Amount.valueOf(
				(_cgMZFM.getXBRF().doubleValue(SI.METER)
						* _maximumZeroFuelMass.doubleValue(SI.KILOGRAM)
						+ _theBalanceManagerInterface.getDesignFuelMass().doubleValue(SI.KILOGRAM)
						* _theBalanceManagerInterface.getTheAircraft().getFuelTank().getXCG().doubleValue(SI.METER))
						/ this._maximumTakeOffMass.doubleValue(SI.KILOGRAM),
						SI.METER));

		_cgMTOM.setZBRF(Amount.valueOf(
				(_cgMZFM.getZBRF().doubleValue(SI.METER)
						* _maximumZeroFuelMass.doubleValue(SI.KILOGRAM)
						+ _theBalanceManagerInterface.getDesignFuelMass().doubleValue(SI.KILOGRAM)
						* _theBalanceManagerInterface.getTheAircraft().getFuelTank().getZCG().doubleValue(SI.METER))
						/ this._maximumTakeOffMass.doubleValue(SI.KILOGRAM),
						SI.METER));
		
		_cgMTOM.calculateCGinMAC(
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
		
		cgExcursionRefToMAC.add(_cgOEM.getXMAC()*100);
		cgExcursionRefToMAC.add(_cgOEM.getXMAC()*100);  
		
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
		
		cgExcursionRefToMAC.add(((((_cgOEM.getXBRF().times(_theBalanceManagerInterface.getOperatingEmptyMass()).getEstimatedValue())
				+ (_theBalanceManagerInterface.getTheAircraft().getFuelTank().getXCG().to(SI.METER)
						.times(_theBalanceManagerInterface.getDesignFuelMass().doubleValue(SI.KILOGRAM)).getEstimatedValue()))
				/(_theBalanceManagerInterface.getOperatingEmptyMass().to(SI.KILOGRAM).plus(_theBalanceManagerInterface.getDesignFuelMass().to(SI.KILOGRAM)).getEstimatedValue()))
				- meanAerodynamicChordXle.doubleValue(SI.METER))
				/ (meanAerodynamicChord.doubleValue(SI.METER)/100
						)
				);
		cgExcursionRefToMAC.add((_cgMTOM.getXMAC()*100));
		
		_maxForwardCG = (MyArrayUtils.getMin(cgExcursionRefToMAC)/100);
		_maxAftCG = (MyArrayUtils.getMax(cgExcursionRefToMAC)/100);
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

	public CenterOfGravity getCGStructurePowerAndSystems() {
		return _cgStructurePowerAndSystems;
	}

	public void setCGStructurePowerAndSystems(CenterOfGravity _cgStructurePowerAndSystems) {
		this._cgStructurePowerAndSystems = _cgStructurePowerAndSystems;
	}

	public CenterOfGravity getCGOEM() {
		return _cgOEM;
	}

	public void setCGOEM(CenterOfGravity _cgOEM) {
		this._cgOEM = _cgOEM;
	}

	public CenterOfGravity getCGMZFM() {
		return _cgMZFM;
	}

	public void setCGMZFM(CenterOfGravity _cgMZFM) {
		this._cgMZFM = _cgMZFM;
	}

	public List<CenterOfGravity> getCGList() {
		return _cgList;
	}

	public void setCGList(List<CenterOfGravity> _cgList) {
		this._cgList = _cgList;
	}

	public CenterOfGravity getCGMTOM() {
		return _cgMTOM;
	}

	public void setCgMTOM(CenterOfGravity _cgMTOM) {
		this._cgMTOM = _cgMTOM;
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
	
}
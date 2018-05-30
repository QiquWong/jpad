package it.unina.daf.test.highliftdeviceseffects;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import aircraft.components.liftingSurface.creator.ISlatCreator;
import aircraft.components.liftingSurface.creator.ISymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.HighLiftDeviceEffectEnum;
import database.DatabaseManager;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import javaslang.Tuple;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class HighLiftDevicesCalc {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	public static InputTree input;
	public static OutputTree output;
	
	
	/**************************************************************************************
	 * This method is in charge of reading data from a given XML input file and 
	 * put them inside an object of the InputTree class.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param pathToXML
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		input = new InputTree();
		
		MyConfiguration.customizeAmountOutput();
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// Initialize data:	
		boolean plotCharts = false;
		Amount<Angle> currentAngleOfAttack = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		double aspectRatio = 0.0;
		Amount<Area> surface = Amount.valueOf(0.0, SI.SQUARE_METRE);
		double taperRatioEq = 0.0;
		Amount<Angle> sweepQuarteChordEq = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Length> rootChordEquivalentWing = Amount.valueOf(0.0, SI.METER);
		Amount<Angle> alphaStallClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<Angle> alphaStarClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE);
		Amount<?> cLAlphaClean = Amount.valueOf(0.0, NonSI.DEGREE_ANGLE.inverse());
		double cL0Clean = 0.0;
		double cLstarClean = 0.0;
		double cLmaxClean = 0.0;
		List<Double> etaStations = new ArrayList<>();
		List<Amount<Length>> airfoilsChordDistribution = new ArrayList<>();
		List<Double> maxThicknessAirfoilsDistribution = new ArrayList<>();
		List<Amount<Length>> leadingEdgeRadiusAirfoilsDistribution = new ArrayList<>();
		List<Amount<?>> clAlphaAirfoilsDistribution = new ArrayList<>();
		List<Double> cl0AirfoilsDistribution = new ArrayList<>();
		AirfoilFamilyEnum airfoilsFamily = null;
		int flapsNumber = 0;
		int slatsNumber = 0;
		List<FlapTypeEnum> flapType = new ArrayList<>();
		List<Double> flapChordRatioList = new ArrayList<>();
		List<Double> slatChordRatioList = new ArrayList<>();
		List<Double> slatExtensionRatioList = new ArrayList<>();
		List<Double> etaInFlap = new ArrayList<>();
		List<Double> etaOutFlap = new ArrayList<>();
		List<Double> etaInSlat = new ArrayList<>();
		List<Double> etaOutSlat = new ArrayList<>();
		List<Amount<Angle>> deltaFlapList = new ArrayList<>();
		List<Amount<Angle>> deltaSlatList = new ArrayList<>();
		
		//---------------------------------------------------------------------------------
		// ATTRIBUTES:
		//---------------------------------------------------------------------------------------
		String plotChartsString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@plotCharts");
		if(plotChartsString != null)
			input.setPlotCharts(Boolean.valueOf(plotChartsString));
		
		//---------------------------------------------------------------------------------
		// FLIGHT CONDITION:
		//---------------------------------------------------------------------------------------
		String currentAngleOfAttackProperty = reader.getXMLPropertyByPath("//flight_condition/current_angle_of_attack");
		if(currentAngleOfAttackProperty != null)
			input.setCurrentAngleOfAttack(reader.getXMLAmountAngleByPath("//flight_condition/current_angle_of_attack"));

		//---------------------------------------------------------------------------------
		// WING:	
		//---------------------------------------------------------------------------------------
		// Geometry:
		String aspectRatioProperty = reader.getXMLPropertyByPath("//wing/geometry/aspect_ratio");
		if(aspectRatioProperty != null)
			input.setAspectRatio(Double.valueOf(aspectRatioProperty));
		
		String surfaceProperty = reader.getXMLPropertyByPath("//wing/geometry/surface");
		if(surfaceProperty != null)
			input.setSurface((Amount<Area>) reader.getXMLAmountWithUnitByPath("//wing/geometry/surface"));
		
		String rootChordEquivalentWingProperty = reader.getXMLPropertyByPath("//wing/geometry/root_chord_equivalent_wing");
		if(rootChordEquivalentWingProperty != null)
			input.setRootChordEquivalentWing(reader.getXMLAmountLengthByPath("//wing/geometry/root_chord_equivalent_wing"));
		
		String sweepQuarterChordEquivalentProperty = reader.getXMLPropertyByPath("//wing/geometry/sweep_quarter_chord_equivalent_wing");
		if(sweepQuarterChordEquivalentProperty != null)
			input.setSweepQuarteChordEq(reader.getXMLAmountAngleByPath("//wing/geometry/sweep_quarter_chord_equivalent_wing"));
		
		String taperRatioEquivalentProperty = reader.getXMLPropertyByPath("//wing/geometry/taper_ratio_equivalent_wing");
		if(taperRatioEquivalentProperty != null)
			input.setTaperRatioEq(Double.valueOf(taperRatioEquivalentProperty));
		
		//---------------------------------------------------------------------------------------
		// Clean wing parameters:
		String alphaStallCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/alpha_stall_clean");
		if(alphaStallCleanProperty != null)
			input.setAlphaStallClean(reader.getXMLAmountAngleByPath("//wing/clean_configuration_parameters/alpha_stall_clean"));
		
		String alphaStarCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/alpha_star_clean");
		if(alphaStarCleanProperty != null)
			input.setAlphaStarClean(reader.getXMLAmountAngleByPath("//wing/clean_configuration_parameters/alpha_star_clean"));
		
		String cLAlphaCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cLalpha_clean");
		if(cLAlphaCleanProperty != null)
			input.setCLAlphaClean(reader.getXMLAmountWithUnitByPath("//wing/clean_configuration_parameters/cLalpha_clean"));
		
		String cL0CleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cL0_clean");
		if(cL0CleanProperty != null)
			input.setCL0Clean(Double.valueOf(cL0CleanProperty));
		
		String cLStarCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cLStar_clean");
		if(cLStarCleanProperty != null)
			input.setCLstarClean(Double.valueOf(cLStarCleanProperty));
		
		String cLmaxCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cLmax_clean");
		if(cLmaxCleanProperty != null)
			input.setCLmaxClean(Double.valueOf(cLmaxCleanProperty));
		
		//---------------------------------------------------------------------------------------
		// Airfoils data:
		String etaStationsProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/eta_stations");
		if(etaStationsProperty != null)
			input.setEtaStations(reader.readArrayDoubleFromXML("//wing/airfoils_data/eta_stations"));
		
		String clAlphaAirfoilsDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/cl_alpha_distribution");
		if(clAlphaAirfoilsDistributionProperty != null)
			input.setClAlphaAirfoilsDistribution(reader.readArrayofUnknownAmountFromXML("//wing/airfoils_data/cl_alpha_distribution"));
		
		String cl0AirfoilDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/cl0_distribution");
		if(cl0AirfoilDistributionProperty != null)
			input.setCl0AirfoilsDistribution(reader.readArrayDoubleFromXML("//wing/airfoils_data/cl0_distribution"));
		
		String leadingEdgeRadiusDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/leading_edge_radius_distribution");
		if(leadingEdgeRadiusDistributionProperty != null)
			input.setLeadingEdgeRadiusAirfoilsDistribution(reader.readArrayDoubleFromXML("//wing/airfoils_data/leading_edge_radius_distribution"));
		
		String airfoilsChordDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/airfoil_chord_distribution");
		if(airfoilsChordDistributionProperty != null)
			input.setAirfoilsChordDistribution(reader.readArrayofAmountFromXML("//wing/airfoils_data/airfoil_chord_distribution"));
		
		String maxThicknessAirfoilsDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/max_thickness_distribution");
		if(maxThicknessAirfoilsDistributionProperty != null)
			input.setMaxThicknessAirfoilsDistribution(reader.readArrayDoubleFromXML("//wing/airfoils_data/max_thickness_distribution"));
		
		String airfoilFamilyDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/airfoils_family");
		if(airfoilFamilyDistributionProperty != null) 
			input.setAirfoilsFamily(  
					Arrays.stream(AirfoilFamilyEnum.values())
					.filter(a -> a.toString().equals(airfoilFamilyDistributionProperty))
					.findFirst()
					.orElseThrow(() -> {throw new IllegalStateException(String.format("Unsupported airfoil family", airfoilFamilyDistributionProperty));
					})
					);
		
		//---------------------------------------------------------------------------------------
		// Print data:
		System.out.println("\tPlot Charts = " + input.isPlotCharts() + "\n");
		System.out.println("\tCurrent Angle of Attack = " + input.getCurrentAngleOfAttack() + "\n");
		System.out.println("\tAspect Ratio = " + input.getAspectRatio());
		System.out.println("\tSurface = " + input.getSurface());
		System.out.println("\tSweep quarter chord equivalent wing = " + input.getSweepQuarteChordEq());
		System.out.println("\tTaper ratio equivalent wing = " + input.getTaperRatioEq());
		System.out.println("\tRoot chord equivalent wing = " + input.getRootChordEquivalentWing() + "\n");
		System.out.println("\tAlpha stall clean = " + input.getAlphaStallClean());
		System.out.println("\tAlpha star clean = " + input.getAlphaStarClean());
		System.out.println("\tCLalpha clean = " + input.getCLAlphaClean());
		System.out.println("\tCL0 clean = " + input.getCL0Clean());
		System.out.println("\tCLstar clean = " + input.getCLstarClean());
		System.out.println("\tCLmax clean = " + input.getCLmaxClean() + "\n");
		System.out.println("\tAirfoils adimensional stations = " + input.getEtaStations());
		System.out.println("\tAirfoils chord distribution = " + input.getAirfoilsChordDistribution());
		System.out.println("\tMax thickness airfoils distribution = " + input.getMaxThicknessAirfoilsDistribution());
		System.out.println("\tLeading edge radius airfoils distribution = " + input.getLeadingEdgeRadiusAirfoilsDistribution());
		System.out.println("\tClalpha airfoils distribution = " + input.getClAlphaAirfoilsDistribution());
		System.out.println("\tCl0 airfoils distribution = " + input.getCl0AirfoilsDistribution());
		System.out.println("\tAirfoils family distribution = " + input.getAirfoilsFamily() + "\n");
		
		//---------------------------------------------------------------------------------
		// FLAPS:
		//---------------------------------------------------------------------------------------
		NodeList nodelistFlaps = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//flaps/flap");
		
		input.setFlapsNumber(nodelistFlaps.getLength());
		System.out.println("Flaps found: " + input.getFlapsNumber());
		
		System.out.println("Reading flaps data...");
		
		List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//flap_type");
		// Recognizing flap type 
		flapTypeProperty.stream().forEach(
				x -> input.getFlapType().add( 
						Arrays.stream(FlapTypeEnum.values())
						.filter(a -> a.toString().equals(x))
						.findFirst()
						.orElseThrow(() -> {throw new IllegalStateException(String.format("Unsupported flap type", flapTypeProperty));}))
				);
				
		List<String> cfcProperty = reader.getXMLPropertiesByPath("//flap_chord_ratio");
		for(int i=0; i<cfcProperty.size(); i++)
			input.getCfc().add(Double.valueOf(cfcProperty.get(i)));
		
		List<String> deltaFlapProperty = reader.getXMLPropertiesByPath("//flap_deflection");
		for(int i=0; i<deltaFlapProperty.size(); i++)
			input.getDeltaFlap().add(Amount.valueOf(Double.valueOf(deltaFlapProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> etaInFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_inner_station");
		for(int i=0; i<etaInFlapProperty.size(); i++)
			input.getEtaInFlap().add(Double.valueOf(etaInFlapProperty.get(i)));
		
		List<String> etaOutFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_outer_station");
		for(int i=0; i<etaOutFlapProperty.size(); i++)
			input.getEtaOutFlap().add(Double.valueOf(etaOutFlapProperty.get(i)));

		System.out.println("\tFlaps Types = " +  input.getFlapType());
		System.out.println("\tFlaps chord ratios = " +  input.getCfc());
		System.out.print("\tFlaps deflections = [");
		for(int i=0; i<input.getDeltaFlap().size(); i++)
			System.out.print(" " +  input.getDeltaFlap().get(i).getEstimatedValue() + " " + input.getDeltaFlap().get(i).getUnit());
		System.out.print("]\n");
		System.out.println("\tFlaps inboard station = " +  input.getEtaInFlap());
		System.out.println("\tFlaps outboard station = " +  input.getEtaOutFlap() + "\n");
		
		//---------------------------------------------------------------------------------
		// SLATS:
		//---------------------------------------------------------------------------------------
		NodeList nodelistSlats = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//slats/slat");

		input.setSlatsNumber(nodelistSlats.getLength());
		System.out.println("Slats found: " + input.getSlatsNumber());
		
		System.out.println("Reading slats data...");
		
		List<String> deltaSlatProperty = reader.getXMLPropertiesByPath("//slat_deflection");
		for(int i=0; i<deltaSlatProperty.size(); i++)
			input.getDeltaSlat().add(Amount.valueOf(Double.valueOf(deltaSlatProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> cscProperty = reader.getXMLPropertiesByPath("//slat_chord_ratio");
		for(int i=0; i<cscProperty.size(); i++)
			input.getCsc().add(Double.valueOf(cscProperty.get(i)));
		
		List<String> cExtcSlatProperty = reader.getXMLPropertiesByPath("//slat_extension_ratio");
		for(int i=0; i<cExtcSlatProperty.size(); i++)
			input.getcExtCSlat().add(Double.valueOf(cExtcSlatProperty.get(i)));
		
		List<String> etaInSlatProperty = reader.getXMLPropertiesByPath("//slat_non_dimensional_inner_station");
		for(int i=0; i<etaInSlatProperty.size(); i++)
			input.getEtaInSlat().add(Double.valueOf(etaInSlatProperty.get(i)));
		
		List<String> etaOutSlatProperty = reader.getXMLPropertiesByPath("//slat_non_dimensional_outer_station");
		for(int i=0; i<etaOutSlatProperty.size(); i++)
			input.getEtaOutSlat().add(Double.valueOf(etaOutSlatProperty.get(i)));

		System.out.print("\tSlats deflections = [");
		for(int i=0; i<input.getDeltaSlat().size(); i++)
			System.out.print(" " +  input.getDeltaSlat().get(i).getEstimatedValue() + " " + input.getDeltaSlat().get(i).getUnit());
		System.out.print("]\n");
		System.out.println("\tSlats chord ratios = " +  input.getCsc());
		System.out.println("\tSlats extension ratios = " +  input.getcExtCSlat());
		System.out.println("\tSlats inboard stations = " +  input.getEtaInSlat());
		System.out.println("\tSlats outboard stations = " +  input.getEtaOutSlat() + "\n");
	
		System.out.println("--------------------DONE--------------------------");
	}

	@SuppressWarnings("unchecked")
	public static void executeStandAloneHighLiftDevicesCalc (
			String databaseFolderPath,
			String highLiftDatabaseFileName,
			String aerodynamicDatabaseFileName
			) {
		
		MyConfiguration.customizeAmountOutput();
		final PrintStream originalErr = System.err;
		PrintStream filterStream = new PrintStream(new OutputStream() {
		    public void write(int b) {
		         // write nothing
		    }
		});
		
		//------------------------------------------------------------------------------------
		// create an OutputTree object
		output = new OutputTree();
		
		//------------------------------------------------------------------------------------
		// Setup database(s)
		System.setErr(filterStream);
		
		AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(
				new AerodynamicDatabaseReader(
						databaseFolderPath,	aerodynamicDatabaseFileName
						),
				databaseFolderPath
				);
		HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(
				new HighLiftDatabaseReader(
						databaseFolderPath,	highLiftDatabaseFileName
						),
				databaseFolderPath
				);
		
		System.setErr(originalErr);
		
		//------------------------------------------------------------------------------------
		// Managing flaps types:
		List<Double> flapTypeIndex = new ArrayList<Double>();
		List<Double> deltaFlapRef = new ArrayList<Double>();
	
		for(int i=0; i<input.getFlapType().size(); i++) {
			if(input.getFlapType().get(i) == FlapTypeEnum.SINGLE_SLOTTED) {
				flapTypeIndex.add(1.0);
				deltaFlapRef.add(45.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.DOUBLE_SLOTTED) {
				flapTypeIndex.add(2.0);
				deltaFlapRef.add(50.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.PLAIN) {
				flapTypeIndex.add(3.0);
				deltaFlapRef.add(60.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.FOWLER) {
				flapTypeIndex.add(4.0);
				deltaFlapRef.add(40.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.TRIPLE_SLOTTED) {
				flapTypeIndex.add(5.0);
				deltaFlapRef.add(50.0);
			}
			else if(input.getFlapType().get(i) == FlapTypeEnum.OPTIMIZED_FOWLER) {
				flapTypeIndex.add(6.0);
				deltaFlapRef.add(40.0);
			}
		}
		
		//--------------------------------------------
		// CALCULATION OF HIGH LIFT DEVICES EFFECTS:
		System.out.println("\nCalculating high lift devices effects...");
		System.out.println("\n-----------HIGH LIFT DEVICES EFFECTS-------------- ");

		//....................................................................
		// Definition of the flap and slat list ...
		List<SymmetricFlapCreator> flapList = new ArrayList<>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			
			flapList.add(
					new SymmetricFlapCreator(
							new ISymmetricFlapCreator.Builder()
									.setId("Flap #" + i)
									.setType(input.getFlapType().get(i))
									.setInnerStationSpanwisePosition(input.getEtaInFlap().get(i))
									.setOuterStationSpanwisePosition(input.getEtaOutFlap().get(i))
									.setInnerChordRatio(input.getCfc().get(i))
									.setOuterChordRatio(input.getCfc().get(i))
									.setMinimumDeflection(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE))
									.setMaximumDeflection(Amount.valueOf(50.0, NonSI.DEGREE_ANGLE))
									.build()
							)
					);
			
		}
		
		List<SlatCreator> slatList = new ArrayList<>();
		for(int i=0; i<input.getSlatsNumber(); i++) {
			
			slatList.add(
					new SlatCreator (
							new ISlatCreator.Builder()
							.setId("Slat #" + i)
							.setInnerStationSpanwisePosition(input.getEtaInSlat().get(i))
							.setOuterStationSpanwisePosition(input.getEtaOutSlat().get(i))
							.setInnerChordRatio(input.getCsc().get(i))
							.setOuterChordRatio(input.getCsc().get(i))
							.setExtensionRatio(input.getcExtCSlat().get(i))
							.setMinimumDeflection(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE))
							.setMaximumDeflection(Amount.valueOf(35.0, NonSI.DEGREE_ANGLE))
							.build()
							)
					);
			
		}
		
		//....................................................................
		// Mean Airfoil Maximum thickness:
		List<Double> wingInfluenceCoefficients = LSGeometryCalc.calculateInfluenceCoefficients(
				input.getAirfoilsChordDistribution(), 
				input.getEtaStations().stream()
				.map(eta -> Amount.valueOf(
						eta*Math.sqrt(input.getAspectRatio()*input.getSurface().doubleValue(SI.SQUARE_METRE))/2,
						SI.METER)
						)
				.collect(Collectors.toList()), 
				input.getSurface(),
				true
				);
		
		double maximumThicknessMeanAirfoil = 0;

		for(int i=0; i<wingInfluenceCoefficients.size(); i++)
			maximumThicknessMeanAirfoil += wingInfluenceCoefficients.get(i)
			*input.getMaxThicknessAirfoilsDistribution().get(i);
		
		//....................................................................
		// Calculation of the high lift effects ...
		Map<HighLiftDeviceEffectEnum, Object> resultsMap = 
				LiftCalc.calculateHighLiftDevicesEffects(
						aeroDatabaseReader, 
						highLiftDatabaseReader, 
						flapList, 
						slatList, 
						input.getEtaStations(), 
						input.getClAlphaAirfoilsDistribution(),
						input.getCl0AirfoilsDistribution(),
						input.getMaxThicknessAirfoilsDistribution(),
						input.getLeadingEdgeRadiusAirfoilsDistribution(),
						input.getAirfoilsChordDistribution(), 
						input.getDeltaFlap().stream().map(x -> x.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()),
						input.getDeltaSlat().stream().map(x -> x.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()), 
						input.getCurrentAngleOfAttack(),
						input.getCLAlphaClean(),
						input.getSweepQuarteChordEq(),
						input.getTaperRatioEq(),
						input.getRootChordEquivalentWing(), 
						input.getAspectRatio(),
						input.getSurface(),
						maximumThicknessMeanAirfoil,
						input.getAirfoilsFamily(), 
						input.getCL0Clean(),
						input.getCLmaxClean(),
						input.getAlphaStarClean(),
						input.getAlphaStallClean()
						);

		output.setDeltaCl0FlapList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP_LIST));
		output.setDeltaCL0FlapList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP_LIST));
		output.setDeltaClmaxFlapList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP_LIST));
		output.setDeltaCLmaxFlapList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP_LIST));
		output.setDeltaCDList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CD_LIST));
		output.setDeltaCMC4List((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CM_c4_LIST));
		output.setDeltaCl0Flap((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl0_FLAP));
		output.setDeltaCL0Flap((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL0_FLAP));
		output.setDeltaClmaxFlap((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_FLAP));
		output.setDeltaCLmaxFlap((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_FLAP));
		output.setDeltaCD((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CD));
		output.setDeltaCMC4((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CM_c4));
		output.setcLalphaHighLift((Amount<?>) resultsMap.get(HighLiftDeviceEffectEnum.CL_ALPHA_HIGH_LIFT));
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST) != null)
			output.setDeltaClmaxSlatList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST));
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST) != null)
			output.setDeltaCLmaxSlatList((List<Double>) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST));
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT) != null)
			output.setDeltaClmaxSlat((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT));
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT) != null)
			output.setDeltaCLmaxSlat((Double) resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT));

		//---------------------------------------------------------------
		// PRINT HIGH LIFT DEVICES EFFECTS:
		//---------------------------------------------------------------
		System.out.println("\t\ndeltaCl0 flap list = ");
		for(int i=0; i<output.getDeltaCl0FlapList().size(); i++)
			System.out.println(output.getDeltaCl0FlapList().get(i));
	
		System.out.println("\t\ndeltaCl0 flap = \n" + output.getDeltaCl0Flap());
	
		System.out.println("\t\ndeltaCL0 flap list = ");
		for(int i=0; i<output.getDeltaCL0FlapList().size(); i++)
			System.out.println(output.getDeltaCL0FlapList().get(i));
	
		System.out.println("\t\ndeltaCL0 flap = \n" + output.getDeltaCL0Flap());
	
		System.out.println("\t\ndeltaClmax flap list = ");
		for(int i=0; i<output.getDeltaClmaxFlapList().size(); i++)
			System.out.println(output.getDeltaClmaxFlapList().get(i));
	
		System.out.println("\t\ndeltaClmax flap = \n" + output.getDeltaClmaxFlap());
	
		System.out.println("\t\ndeltaCLmax flap list = ");
		for(int i=0; i<output.getDeltaCLmaxFlapList().size(); i++)
			System.out.println(output.getDeltaCLmaxFlapList().get(i));
	
		System.out.println("\t\ndeltaCLmax flap = \n" + output.getDeltaCLmaxFlap());
		
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT_LIST) != null) {
			System.out.println("\t\ndeltaClmax slat list = ");
			for(int i=0; i<output.getDeltaClmaxSlatList().size(); i++)
				System.out.println(output.getDeltaClmaxSlatList().get(i));
		}
	
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_Cl_MAX_SLAT) != null)
			System.out.println("\t\ndeltaClmax slat = \n" + output.getDeltaClmaxSlat());
	
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT_LIST) != null) {
			System.out.println("\t\ndeltaCLmax slat list = ");
			for(int i=0; i<output.getDeltaCLmaxSlatList().size(); i++)
				System.out.println(output.getDeltaCLmaxSlatList().get(i));
		}
	
		if(resultsMap.get(HighLiftDeviceEffectEnum.DELTA_CL_MAX_SLAT) != null)
			System.out.println("\t\ndeltaCLmax slat = \n" + output.getDeltaCLmaxSlat());
	
		System.out.println("\t\nCLalpha high lift = \n" + output.getcLalphaHighLift());
	
		System.out.println("\t\ndeltaCD0 list = ");
		for(int i=0; i<output.getDeltaCDList().size(); i++)
			System.out.println(output.getDeltaCDList().get(i));
	
		System.out.println("\t\ndeltaCD0 = \n" + output.getDeltaCD());
	
		System.out.println("\t\ndeltaCMc4_list = ");
		for(int i=0; i<output.getDeltaCMC4List().size(); i++)
			System.out.println(output.getDeltaCMC4List().get(i));
	
		System.out.println("\t\ndeltaCMc4 = \n" + output.getDeltaCMC4());
		
		System.out.println("\t------------------DONE----------------------");
		
		//....................................................................
		// Plot the lift curve comparison ...
		if(input.isPlotCharts() == true ) {
			try {
				plotCurves(aeroDatabaseReader);
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	
	private static void plotCurves(AerodynamicDatabaseReader aeroDatabaseReader) throws InstantiationException, IllegalAccessException{ 

		String folderPathHL = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		
		//--------------------------------------------------------------------------------------
		// BUILDING CLEAN CURVE:
		//--------------------------------------------------------------------------------------
		double alphaCleanFirst = -10.0;
		int nPoints = 50;
		
		output.getAlphaListPlot().add(
				MyArrayUtils.linspaceDouble(
						alphaCleanFirst,
						input.getAlphaStallClean().to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 2,
						nPoints
						)
				);
		
		output.getcLListPlot().add(
				LiftCalc.calculateCLvsAlphaArray(
						input.getCL0Clean(),
						input.getCLmaxClean(),
						input.getAlphaStarClean().to(NonSI.DEGREE_ANGLE), 
						input.getAlphaStallClean().to(NonSI.DEGREE_ANGLE), 
						input.getCLAlphaClean().to(NonSI.DEGREE_ANGLE.inverse()), 
						output.getAlphaListPlot().get(0)
						)
				);

		//--------------------------------------------------------------------------------------
		// BUILDING HIGH LIFT CURVE:
		//--------------------------------------------------------------------------------------
		double alphaHighLiftFirst = -13.0;
		
		Amount<Length> span = Amount.valueOf(
				Math.sqrt(input.getAspectRatio()*input.getSurface().doubleValue(SI.SQUARE_METRE)), 
				SI.METER
				);
		
		List<Double> wingInfluenceCoefficients = LSGeometryCalc.calculateInfluenceCoefficients(
				input.getAirfoilsChordDistribution(), 
				input.getEtaStations()
					.stream()
						.map(x -> span.divide(2).times(x))
							.collect(Collectors.toList()), 
				input.getSurface(),
				true
				);
		
		double meanThicknessToChordRatio = 0.0;
		for(int i=0; i<wingInfluenceCoefficients.size(); i++)
			meanThicknessToChordRatio += wingInfluenceCoefficients.get(i)
			*input.getMaxThicknessAirfoilsDistribution().get(i);
				
		output.setcL0HighLift(input.getCL0Clean() + output.getDeltaCL0Flap());
		output.setcLmaxHighLift(input.getCLmaxClean() + output.getDeltaCLmaxFlap() + output.getDeltaCLmaxSlat());
		output.setAlphaStallHighLift(
				Amount.valueOf(
						((output.getcLmaxHighLift() - output.getcL0HighLift())
								/output.getcLalphaHighLift().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()) 
						+ aeroDatabaseReader.getDAlphaVsLambdaLEVsDy(
								LSGeometryCalc.calculateSweep(
										input.getAspectRatio(), 
										input.getTaperRatioEq(), 
										input.getSweepQuarteChordEq().getEstimatedValue(),
										0.0,
										0.25
										).getEstimatedValue(),
								aeroDatabaseReader.getDeltaYvsThickness(
										meanThicknessToChordRatio,
										input.getAirfoilsFamily()
										)
								),
						NonSI.DEGREE_ANGLE
						)
				);

		if(input.getSlatsNumber() == 0.0)
			output.setAlphaStarHighLift(
					Amount.valueOf(
							(((input.getCLstarClean() - output.getcL0HighLift())
									/ output.getcLalphaHighLift().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue())
									+ input.getAlphaStarClean().doubleValue(NonSI.DEGREE_ANGLE))/2,
							NonSI.DEGREE_ANGLE
							)
					);
		else
			output.setAlphaStarHighLift(
					output.getAlphaStallHighLift().to(NonSI.DEGREE_ANGLE).minus(
							input.getAlphaStallClean().to(NonSI.DEGREE_ANGLE)
							.minus(input.getAlphaStarClean().to(NonSI.DEGREE_ANGLE)
									)
							)
					);
				
		output.setcLStarHighLift(
				output.getcLalphaHighLift().to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()
				*output.getAlphaStarHighLift().doubleValue(NonSI.DEGREE_ANGLE) 
				+output.getcL0HighLift()
				);
		
		output.getAlphaListPlot().add(
				MyArrayUtils.linspaceDouble(
						alphaHighLiftFirst,
						output.getAlphaStallHighLift().to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 1,
						nPoints
						)
				);
		
		output.getcLListPlot().add(
				LiftCalc.calculateCLvsAlphaArray(
						output.getcL0HighLift(),
						output.getcLmaxHighLift(),
						output.getAlphaStarHighLift(), 
						output.getAlphaStallHighLift(), 
						output.getcLalphaHighLift(), 
						output.getAlphaListPlot().get(1)
						)
				);
		
		System.out.println("\t\n-----------CLEAN CONFIGURATION-------------- ");
		System.out.println("\tAlpha max = " + input.getAlphaStallClean());
		System.out.println("\tAlpha star = " + input.getAlphaStarClean());
		System.out.println("\tCL max = " + input.getCLmaxClean());
		System.out.println("\tCL star = " + input.getCLstarClean());
		System.out.println("\tCL0 = " + input.getCL0Clean());
		System.out.println("\tCLalpha = " + input.getCLAlphaClean());
		
		System.out.println("\t\n-----------HIGH LIFT DEVICES ON-------------- ");
		System.out.println("\tAlpha max = " + output.getAlphaStallHighLift());
		System.out.println("\tAlpha star = " + output.getAlphaStarHighLift());
		System.out.println("\tCL max = " + output.getcLmaxHighLift());
		System.out.println("\tCL star = " + output.getcLStarHighLift());
		System.out.println("\tCL0 = " + output.getcL0HighLift());
		System.out.println("\tCLalpha = " + output.getcLalphaHighLift());

		//--------------------------------------------------------------------------------------
		// PLOTTING CURVES:
		//--------------------------------------------------------------------------------------
		List<String> legend  = new ArrayList<>(); 
		legend.add("clean");
		legend.add("high lift");

		System.out.println("\t\n-----------WRITING CHART TO FILE-------------- ");
		
		MyChartToFileUtils.plot(
				output.getAlphaListPlot(), 
				output.getcLListPlot(),
				"CL vs alpha",
				"alpha", 
				"CL",
				null, null, null, null,
				"deg",
				"",
				true,
				legend,
				JPADStaticWriteUtils.createNewFolder(folderPathHL + "High Lift Charts" + File.separator),
				"CL curve high lift", true);

		System.out.println("\t\n-------------------DONE----------------------- ");
	}
	
	/*******************************************************************************************
	 * This method is in charge of writing all input data collected inside the object of the 
	 * OutputTree class on a XML file.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param output object of the OutputTree class which holds all output data
	 */
	public static void writeToXML(OutputTree output, String filenameWithPathAndExt) {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			defineXmlTree(doc, docBuilder);
			
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/*******************************************************************************************
	 * This method defines the XML tree structure and fill it with results form the OutputTree
	 * object
	 * 
	 * @author Vittorio Trifari
	 */
	private static void defineXmlTree(Document doc, DocumentBuilder docBuilder) {
		
		org.w3c.dom.Element rootElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				doc, 
				"High_Lift_Executable", 
				Tuple.of("plotCharts", String.valueOf(input.isPlotCharts()))
		);
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element flightConditionsElement = doc.createElement("flight_condition");
		inputRootElement.appendChild(flightConditionsElement);

		JPADStaticWriteUtils.writeSingleNode("current_lifting_coefficient", input.getCurrentAngleOfAttack(), flightConditionsElement, doc);
				
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		inputRootElement.appendChild(wingDataElement);
		
		org.w3c.dom.Element geometryDataElement = doc.createElement("geometry");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("root_chord_equivalent_wing", input.getRootChordEquivalentWing(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("sweep_quarter_chord_equivalent_wing", input.getSweepQuarteChordEq(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("taper_ratio_equivalent_wing", input.getTaperRatioEq(), geometryDataElement, doc);
		
		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("clean_configuration_parameters");
		wingDataElement.appendChild(cleanConfigurationDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_stall_clean", input.getAlphaStallClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star_clean", input.getAlphaStarClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL0_clean", input.getCL0Clean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLalpha_clean", input.getCLAlphaClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLmax_clean", input.getCLmaxClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLStar_clean", input.getCLstarClean(), cleanConfigurationDataElement, doc);
				
		org.w3c.dom.Element airfoilDataDistributionElement = doc.createElement("airfoils_data");
		wingDataElement.appendChild(airfoilDataDistributionElement);
		
		JPADStaticWriteUtils.writeSingleNode("airfoil_chord_distribution", input.getAirfoilsChordDistribution(), airfoilDataDistributionElement, doc);
		JPADStaticWriteUtils.writeSingleNode("max_thickness_distribution", input.getMaxThicknessAirfoilsDistribution(), airfoilDataDistributionElement, doc);
		JPADStaticWriteUtils.writeSingleNode("leading_edge_radius_distribution", input.getLeadingEdgeRadiusAirfoilsDistribution(), airfoilDataDistributionElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cl_alpha_distribution", input.getClAlphaAirfoilsDistribution(), airfoilDataDistributionElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cl0_distribution", input.getCl0AirfoilsDistribution(), airfoilDataDistributionElement, doc);
		JPADStaticWriteUtils.writeSingleNode("airfoils_family", input.getAirfoilsFamily(), airfoilDataDistributionElement, doc);

		org.w3c.dom.Element flapsDataElement = doc.createElement("flaps");
		inputRootElement.appendChild(flapsDataElement);
		
		for(int i=0; i<input.getFlapsNumber(); i++) {
			
			org.w3c.dom.Element currentFlapDataElement = doc.createElement("flap_" + (i+1));
			flapsDataElement.appendChild(currentFlapDataElement);
			
			JPADStaticWriteUtils.writeSingleNode("flap_type", input.getFlapType().get(i), currentFlapDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("flap_chord_ratio", input.getCfc().get(i), currentFlapDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("flap_deflection", input.getDeltaFlap().get(i), currentFlapDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("flap_non_dimensional_inner_station", input.getEtaInFlap().get(i), currentFlapDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("flap_non_dimensional_outer_station", input.getEtaOutFlap().get(i), currentFlapDataElement, doc);
		
		}
		
		org.w3c.dom.Element slatsDataElement = doc.createElement("slats");
		inputRootElement.appendChild(slatsDataElement);
		
		for(int i=0; i<input.getSlatsNumber(); i++) {

			org.w3c.dom.Element currentSlatDataElement = doc.createElement("slat_" + (i+1));
			slatsDataElement.appendChild(currentSlatDataElement);

			JPADStaticWriteUtils.writeSingleNode("slat_deflection", input.getDeltaSlat().get(i), currentSlatDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("slat_chord_ratio", input.getCsc().get(i), currentSlatDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("slat_extension_ratio", input.getcExtCSlat().get(i), currentSlatDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("slat_non_dimensional_inner_station", input.getEtaInSlat().get(i), currentSlatDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("slat_non_dimensional_outer_station", input.getEtaOutSlat().get(i), currentSlatDataElement, doc);
			
		}
		
		//--------------------------------------------------------------------------------------
		// OUTPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element outputRootElement = doc.createElement("OUTPUT");
		rootElement.appendChild(outputRootElement);
		
		org.w3c.dom.Element highLiftDevicesEffectsElement = doc.createElement("high_lift_devices_effects");
		outputRootElement.appendChild(highLiftDevicesEffectsElement);
		
		JPADStaticWriteUtils.writeSingleNode("delta_Cl0_of_each_flap", output.getDeltaCl0FlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_Cl0_total_due_to_flaps", output.getDeltaCl0Flap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_Clmax_of_each_flap", output.getDeltaClmaxFlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_Clmax_total_due_to_flaps", output.getDeltaClmaxFlap(), highLiftDevicesEffectsElement, doc);
		if(input.getSlatsNumber() > 0.0) {
			JPADStaticWriteUtils.writeSingleNode("delta_Clmax_of_each_slat", output.getDeltaClmaxSlatList(), highLiftDevicesEffectsElement, doc);
			JPADStaticWriteUtils.writeSingleNode("delta_Clmax_total_due_to_slats", output.getDeltaClmaxSlat(), highLiftDevicesEffectsElement, doc);
			JPADStaticWriteUtils.writeSingleNode("delta_CLmax_of_each_slat", output.getDeltaCLmaxSlatList(), highLiftDevicesEffectsElement, doc);
			JPADStaticWriteUtils.writeSingleNode("delta_CLmax_total_due_to_slats", output.getDeltaCLmaxSlat(), highLiftDevicesEffectsElement, doc);
		}
		JPADStaticWriteUtils.writeSingleNode("delta_CL0_of_each_flap", output.getDeltaCL0FlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CL0_total_due_to_flaps", output.getDeltaCL0Flap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_of_each_flap", output.getDeltaCLmaxFlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_total_due_to_flaps", output.getDeltaCLmaxFlap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CD0_of_each_flap", output.getDeltaCDList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CD0_total", output.getDeltaCD(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CM_c4_of_each_flap", output.getDeltaCMC4List(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CM_c4_total", output.getDeltaCMC4(), highLiftDevicesEffectsElement, doc);
		
		org.w3c.dom.Element highLiftGlobalDataElement = doc.createElement("global_high_lift_devices_effects");
		outputRootElement.appendChild(highLiftGlobalDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_max_high_lift", output.getAlphaStallHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star_high_lift", output.getAlphaStarHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha_high_lift", output.getcLalphaHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLmax_high_lift", output.getcLmaxHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_star_high_lift", output.getcLStarHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_zero_high_lift", output.getcL0HighLift(), highLiftGlobalDataElement, doc);
		
		if(input.isPlotCharts() == true) {
			org.w3c.dom.Element highLiftCurveDataElement = doc.createElement("high_lift_curve_point");
			outputRootElement.appendChild(highLiftCurveDataElement);

			JPADStaticWriteUtils.writeSingleNode("alpha_array_clean", Arrays.toString(output.getAlphaListPlot().get(0)), highLiftCurveDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("cL_array_clean", Arrays.toString(output.getcLListPlot().get(0)), highLiftCurveDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("alpha_array_high_lift", Arrays.toString(output.getAlphaListPlot().get(1)), highLiftCurveDataElement, doc);
			JPADStaticWriteUtils.writeSingleNode("cL_array_high_lift", Arrays.toString(output.getcLListPlot().get(1)), highLiftCurveDataElement, doc);
		}
		
	}
	
	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS:

	public static InputTree getInput() {
		return input;
	}

	public static void setInput(InputTree input) {
		HighLiftDevicesCalc.input = input;
	}

	public static OutputTree getOutput() {
		return output;
	}

	public static void setOutput(OutputTree output) {
		HighLiftDevicesCalc.output = output;
	}
}
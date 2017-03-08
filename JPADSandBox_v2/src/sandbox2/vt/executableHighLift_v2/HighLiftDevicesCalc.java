package sandbox2.vt.executableHighLift_v2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Area;
import javax.measure.unit.NonSI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.LiftingSurface.LiftingSurfaceBuilder;
import aircraft.components.liftingSurface.creator.LiftingSurfaceCreator;
import calculators.aerodynamics.LiftCalc;
import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.MethodEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
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
	public static LiftingSurface theWing;
	
	
	/**************************************************************************************
	 * This method is in charge of reading data from a given XML input file and 
	 * put them inside an object of the InputTree class.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param pathToXML
	 * @throws ParserConfigurationException
	 */
	@SuppressWarnings("unchecked")
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		input = new InputTree();
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// FLIGHT CONDITION:
		//---------------------------------------------------------------------------------------
		String currentLiftCoefficientProperty = reader.getXMLPropertyByPath("//flight_condition/current_lifting_coefficient");
		if(currentLiftCoefficientProperty != null)
			input.setCurrentLiftingCoefficient(Double.valueOf(currentLiftCoefficientProperty));

		//---------------------------------------------------------------------------------
		// WING:	
		//---------------------------------------------------------------------------------------
		// Geometry:
		String aspectRatioProperty = reader.getXMLPropertyByPath("//wing/geometry/aspect_ratio");
		if(aspectRatioProperty != null)
			input.setAspectRatio(Double.valueOf(aspectRatioProperty));
		
		String surfaceProperty = reader.getXMLPropertyByPath("//wing/geometry/surface");
		if(surfaceProperty != null)
			input.setSurface( (Amount<Area>) reader.getXMLAmountWithUnitByPath("//wing/geometry/surface"));
		
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
			input.setcLAlphaClean(reader.getXMLAmountWithUnitByPath("//wing/clean_configuration_parameters/cLalpha_clean"));
		
		String cL0CleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cL0_clean");
		if(cL0CleanProperty != null)
			input.setcL0Clean(Double.valueOf(cL0CleanProperty));
		
		String cLStarCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cLStar_clean");
		if(cLStarCleanProperty != null)
			input.setcLstarClean(Double.valueOf(cLStarCleanProperty));
		
		String cLmaxCleanProperty = reader.getXMLPropertyByPath("//wing/clean_configuration_parameters/cLmax_clean");
		if(cLmaxCleanProperty != null)
			input.setcLmaxClean(Double.valueOf(cLmaxCleanProperty));
		
		//---------------------------------------------------------------------------------------
		// Mean airfoil:
		String clAlphaAirfoilsDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/cl_alpha_distribution");
		if(clAlphaAirfoilsDistributionProperty != null)
			input.setClAlphaAirfoilsDistribution(reader.readArrayofUnknownAmountFromXML("//wing/airfoils_data/cl_alpha_distribution"));
		
		String cl0AirfoilDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/cl0_distribution");
		if(cl0AirfoilDistributionProperty != null)
			input.setCl0AirfoilsDistribution(reader.readArrayDoubleFromXML("//wing/airfoils_data/cl0_distribution"));
		
		String leadingEdgeRadiusDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/leading_edge_radius_distribution");
		if(leadingEdgeRadiusDistributionProperty != null)
			input.setLeadingEdgeRadiusAirfoilsDistribution(reader.readArrayofAmountFromXML("//wing/airfoils_data/leading_edge_radius_distribution"));
		
		String airfoilsChordDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/airfoil_chord_distribution");
		if(airfoilsChordDistributionProperty != null)
			input.setAirfoilsChordDistribution(reader.readArrayofAmountFromXML("//wing/airfoils_data/airfoil_chord_distribution"));
		
		String maxThicknessAirfoilsDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/max_thickness_distribution");
		if(maxThicknessAirfoilsDistributionProperty != null)
			input.setMaxThicknessAirfoilsDistribution(reader.readArrayDoubleFromXML("//wing/airfoils_data/max_thickness_distribution"));
		
		String airfoilFamilyDistributionProperty = reader.getXMLPropertyByPath("//wing/airfoils_data/airfoil_family_list");
		if(airfoilFamilyDistributionProperty != null) 
			input.setAirfoilsFamily( 
					Arrays.stream(AirfoilFamilyEnum.values())
					.filter(a -> a.equals(airfoilFamilyDistributionProperty))
					.findFirst()
					.orElseThrow(() -> {throw new IllegalStateException(String.format("Unsupported airfoil family", airfoilFamilyDistributionProperty));}));
		
		//---------------------------------------------------------------------------------------
		// Print data:
		System.out.println("\tCurrent lifting coefficient = " + input.getCurrentLiftingCoefficient() + "\n");
		System.out.println("\tAspect Ratio = " + input.getAspectRatio());
		System.out.println("\tSurface = " + input.getSurface());
		System.out.println("\tSweep quarter chord equivalent wing = " + input.getSweepQuarteChordEq());
		System.out.println("\tTaper ratio equivalent wing = " + input.getTaperRatioEq());
		System.out.println("\tRoot chord equivalent wing = " + input.getRootChordEquivalentWing() + "\n");
		System.out.println("\tAlpha stall clean = " + input.getAlphaStallClean());
		System.out.println("\tAlpha star clean = " + input.getAlphaStarClean());
		System.out.println("\tCLalpha clean = " + input.getcLAlphaClean());
		System.out.println("\tCL0 clean = " + input.getcL0Clean());
		System.out.println("\tCLstar clean = " + input.getcLstarClean());
		System.out.println("\tCLmax clean = " + input.getcLmaxClean() + "\n");
		System.out.println("\tAirfoils chord distribution = " + input.getAirfoilsChordDistribution());
		System.out.println("\tMax thickness airfoils distribution = " + input.getMaxThicknessAirfoilsDistribution());
		System.out.println("\tLeading edge radius airfoils distribution = " + input.getLeadingEdgeRadiusAirfoilsDistribution());
		System.out.println("\tClalpha airfoils distribution = " + input.getCl0AirfoilsDistribution());
		System.out.println("\tCl0 airfoils distribution = " + input.getCl0AirfoilsDistribution());
		System.out.println("\tAirfoils family distribution = " + input.getAirfoilsFamily() + "\n");
		
		//---------------------------------------------------------------------------------
		// FLAPS:
		//---------------------------------------------------------------------------------------
		NodeList nodelistFlaps = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//flaps/flap");
		
		input.setFlapsNumber(nodelistFlaps.getLength());
		System.out.println("Flaps found: " + input.getFlapsNumber());
		
		importFlapFromXML(input, reader);

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
		
		importSlatFromXML(input, reader);

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

	/***************************************************************************************
	 * This method reads all flaps data using a fixed structure for the XML tag. Each tag is
	 * repeated for every flap in order to simplify the collection of data of the same type.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param input
	 * @param reader
	 * @throws ParserConfigurationException
	 */
	private static void importFlapFromXML(InputTree input, JPADXmlReader reader) throws ParserConfigurationException {

		System.out.println("Reading flaps data...");
		
		List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//flap_type");
		// Recognizing flap type
		for(int i=0; i<flapTypeProperty.size(); i++) {
			if(flapTypeProperty.get(i).equals("SINGLE_SLOTTED"))
				input.getFlapType().add(FlapTypeEnum.SINGLE_SLOTTED);
			else if(flapTypeProperty.get(i).equals("DOUBLE_SLOTTED"))
				input.getFlapType().add(FlapTypeEnum.DOUBLE_SLOTTED);
			else if(flapTypeProperty.get(i).equals("PLAIN"))
				input.getFlapType().add(FlapTypeEnum.PLAIN);
			else if(flapTypeProperty.get(i).equals("FOWLER"))
				input.getFlapType().add(FlapTypeEnum.FOWLER);
			else if(flapTypeProperty.get(i).equals("TRIPLE_SLOTTED"))
				input.getFlapType().add(FlapTypeEnum.TRIPLE_SLOTTED);
			else {
				System.err.println("NO VALID FLAP TYPE!!");
				return;
			}
		}
		
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
		
	}
	
	/******************************************************************************************
	 * This method reads all slats data using a fixed structure for the XML tag. Each tag is
	 * repeated for every slat in order to simplify the collection of data of the same type.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param input
	 * @param reader
	 */
	private static void importSlatFromXML(InputTree input, JPADXmlReader reader) {
		
		System.out.println("Reading slats data...");
		
		List<String> delta_slat_property = reader.getXMLPropertiesByPath("//slat_deflection");
		for(int i=0; i<delta_slat_property.size(); i++)
			input.getDeltaSlat().add(Amount.valueOf(Double.valueOf(delta_slat_property.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> cs_c_property = reader.getXMLPropertiesByPath("//slat_chord_ratio");
		for(int i=0; i<cs_c_property.size(); i++)
			input.getCsc().add(Double.valueOf(cs_c_property.get(i)));
		
		List<String> cExt_c_slat_property = reader.getXMLPropertiesByPath("//slat_extension_ratio");
		for(int i=0; i<cExt_c_slat_property.size(); i++)
			input.getcExtCSlat().add(Double.valueOf(cExt_c_slat_property.get(i)));
		
		List<String> eta_in_slat_property = reader.getXMLPropertiesByPath("//slat_non_dimensional_inner_station");
		for(int i=0; i<eta_in_slat_property.size(); i++)
			input.getEtaInSlat().add(Double.valueOf(eta_in_slat_property.get(i)));
		
		List<String> eta_out_slat_property = reader.getXMLPropertiesByPath("//slat_non_dimensional_outer_station");
		for(int i=0; i<eta_out_slat_property.size(); i++)
			input.getEtaOutSlat().add(Double.valueOf(eta_out_slat_property.get(i)));

	}
	
	public static void executeStandAloneHighLiftDevicesCalc (
			String databaseFolderPath,
			String highLiftDatabaseFileName,
			String aerodynamicDatabaseFileName
			) {
		
		//------------------------------------------------------------------------------------
		// create an OutputTree object
		output = new OutputTree();
		
		//------------------------------------------------------------------------------------
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(
				databaseFolderPath,	
				aerodynamicDatabaseFileName
				);
	
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(
				databaseFolderPath,
				highLiftDatabaseFileName
				);
	
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

		///////////////////////////////////////////////////////////////
		// TODO: SET THE REQUIRED PARAMETERS INSIDE THE DEFAULT WING //
		///////////////////////////////////////////////////////////////
		
		theWing = new LiftingSurfaceBuilder("Wing", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
				.liftingSurfaceCreator(
						new LiftingSurfaceCreator
						.LiftingSurfaceCreatorBuilder("Wing", Boolean.TRUE, ComponentEnum.WING)
						.build()
				)
		.build();
		
		LiftCalc.calculateHighLiftDevicesEffects(
				theWing,
				input.getDeltaFlap().stream().map(x -> x.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()),
				input.getDeltaSlat().stream().map(x -> x.to(NonSI.DEGREE_ANGLE)).collect(Collectors.toList()),
				input.getCurrentLiftingCoefficient()
				);
		
		output.setDeltaCl0FlapList(theWing.getTheAerodynamicsCalculator().getDeltaCl0FlapList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCL0FlapList(theWing.getTheAerodynamicsCalculator().getDeltaCL0FlapList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaClmaxFlapList(theWing.getTheAerodynamicsCalculator().getDeltaClmaxFlapList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCLmaxFlapList(theWing.getTheAerodynamicsCalculator().getDeltaCLmaxFlapList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaClmaxSlatList(theWing.getTheAerodynamicsCalculator().getDeltaClmaxSlatList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCLmaxSlatList(theWing.getTheAerodynamicsCalculator().getDeltaCLmaxSlatList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCDList(theWing.getTheAerodynamicsCalculator().getDeltaCDList().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCMC4List(theWing.getTheAerodynamicsCalculator().getDeltaCMc4List().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCl0Flap(theWing.getTheAerodynamicsCalculator().getDeltaCl0Flap().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCL0Flap(theWing.getTheAerodynamicsCalculator().getDeltaCL0Flap().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaClmaxFlap(theWing.getTheAerodynamicsCalculator().getDeltaClmaxFlap().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCLmaxFlap(theWing.getTheAerodynamicsCalculator().getDeltaCLmaxFlap().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaClmaxSlat(theWing.getTheAerodynamicsCalculator().getDeltaClmaxSlat().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCLmaxSlat(theWing.getTheAerodynamicsCalculator().getDeltaCLmaxSlat().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCD(theWing.getTheAerodynamicsCalculator().getDeltaCD().get(MethodEnum.SEMPIEMPIRICAL));
		output.setDeltaCMC4(theWing.getTheAerodynamicsCalculator().getDeltaCMc4().get(MethodEnum.SEMPIEMPIRICAL));

		//---------------------------------------------------------------
		// PRINT HIGH LIFT DEVICES EFFECTS:
		//---------------------------------------------------------------
		System.out.println("\t\ndeltaCl0_flap_list = ");
		for(int i=0; i<output.getDeltaCl0FlapList().size(); i++)
			System.out.println(output.getDeltaCl0FlapList().get(i));
	
		System.out.println("\t\ndeltaCl0_flap = \n" + output.getDeltaCl0Flap());
	
		System.out.println("\t\ndeltaCL0_flap_list = ");
		for(int i=0; i<output.getDeltaCL0FlapList().size(); i++)
			System.out.println(output.getDeltaCL0FlapList().get(i));
	
		System.out.println("\t\ndeltaCL0_flap = \n" + output.getDeltaCL0Flap());
	
		System.out.println("\t\ndeltaClmax_flap_list = ");
		for(int i=0; i<output.getDeltaClmaxFlapList().size(); i++)
			System.out.println(output.getDeltaClmaxFlapList().get(i));
	
		System.out.println("\t\ndeltaClmax_flap = \n" + output.getDeltaClmaxFlap());
	
		System.out.println("\t\ndeltaCLmax_flap_list = ");
		for(int i=0; i<output.getDeltaCLmaxFlapList().size(); i++)
			System.out.println(output.getDeltaCLmaxFlapList().get(i));
	
		System.out.println("\t\ndeltaCLmax_flap = \n" + output.getDeltaCLmaxFlap());
		
		System.out.println("\t\ndeltaClmax_slat_list = ");
		for(int i=0; i<output.getDeltaClmaxSlatList().size(); i++)
			System.out.println(output.getDeltaClmaxSlatList().get(i));
	
		System.out.println("\t\ndeltaClmax_slat = \n" + output.getDeltaClmaxSlat());
	
		System.out.println("\t\ndeltaCLmax_slat_list = ");
		for(int i=0; i<output.getDeltaCLmaxSlatList().size(); i++)
			System.out.println(output.getDeltaCLmaxSlatList().get(i));
	
		System.out.println("\t\ndeltaCLmax_slat = \n" + output.getDeltaCLmaxSlat());
	
		System.out.println("\t\ncLalpha_new = \n" + output.getcLalphaHighLift().getEstimatedValue() + " " + output.getcLalphaHighLift().getUnit());
	
		System.out.println("\t\ndeltaCD_list = ");
		for(int i=0; i<output.getDeltaCDList().size(); i++)
			System.out.println(output.getDeltaCDList().get(i));
	
		System.out.println("\t\ndeltaCD = \n" + output.getDeltaCD());
	
		System.out.println("\t\ndeltaCMc_4_list = ");
		for(int i=0; i<output.getDeltaCMC4List().size(); i++)
			System.out.println(output.getDeltaCMC4List().get(i));
	
		System.out.println("\t\ndeltaCMc_4 = \n" + output.getDeltaCMC4());
		
		System.out.println("\t------------------DONE----------------------");
		
		try {
			plotCurves(aeroDatabaseReader);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private static void plotCurves(AerodynamicDatabaseReader aeroDatabaseReader) throws InstantiationException, IllegalAccessException{ 

		String folderPathHL = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		
		//--------------------------------------------------------------------------------------
		// BUILDING CLEAN CURVE:
		//--------------------------------------------------------------------------------------
		double alphaCleanFirst = -10.0;
		int nPoints = 40;
		
		output.getAlphaListPlot().add(
				MyArrayUtils.linspaceDouble(
						alphaCleanFirst,
						input.getAlphaStallClean().to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 2,
						nPoints
						)
				);
		
		output.getcLListPlot().add(
				LiftCalc.calculateCLvsAlphaArray(
						input.getcL0Clean(),
						input.getcLmaxClean(),
						input.getAlphaStarClean().to(NonSI.DEGREE_ANGLE), 
						input.getAlphaStallClean().to(NonSI.DEGREE_ANGLE), 
						input.getcLAlphaClean().to(NonSI.DEGREE_ANGLE.inverse()), 
						output.getAlphaListPlot().get(0)
						)
				);

		//--------------------------------------------------------------------------------------
		// BUILDING HIGH LIFT CURVE:
		//--------------------------------------------------------------------------------------
		double alphaHighLiftFirst = -13.0;
		
		Airfoil meanAirfoil = new Airfoil(LiftingSurface.calculateMeanAirfoil(theWing));
		
		output.getAlphaListPlot().add(
				MyArrayUtils.linspaceDouble(
						alphaHighLiftFirst,
						output.getAlphaStallHighLift().to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 2,
						nPoints
						)
				);
		
		output.setcL0HighLift(input.getcL0Clean() + output.getDeltaCL0Flap());
		output.setcLmaxHighLift(input.getcLmaxClean() + output.getDeltaCLmaxFlap() + output.getDeltaCLmaxSlat());
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
										meanAirfoil.getAirfoilCreator().getThicknessToChordRatio(),
										input.getAirfoilsFamily()
										)
								),
						NonSI.DEGREE_ANGLE
						)
				);

		if(input.getSlatsNumber() == 0.0)
			output.setAlphaStarHighLift(
					Amount.valueOf(
							(input.getAlphaStarClean().doubleValue(NonSI.DEGREE_ANGLE) 
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
		System.out.println("\tCL max = " + input.getcLmaxClean());
		System.out.println("\tCL star = " + input.getcLstarClean());
		System.out.println("\tCL0 = " + input.getcL0Clean());
		System.out.println("\tCL alpha = " + input.getcLAlphaClean());
		
		System.out.println("\t\n-----------HIGH LIFT DEVICES ON-------------- ");
		System.out.println("\tAlpha max = " + output.getAlphaStallHighLift());
		System.out.println("\tAlpha star = " + output.getAlphaStarHighLift());
		System.out.println("\tCL max = " + output.getcLmaxHighLift());
		System.out.println("\tCL star = " + output.getcLStarHighLift());
		System.out.println("\tCL0 = " + output.getcL0HighLift());
		System.out.println("\tCL alpha = " + output.getcLalphaHighLift());

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
				"CL curve high lift");

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
		
		org.w3c.dom.Element rootElement = doc.createElement("High_Lift_Executable");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element flightConditionsElement = doc.createElement("flight_condition");
		inputRootElement.appendChild(flightConditionsElement);

		JPADStaticWriteUtils.writeSingleNode("current_lifting_coefficient", input.getCurrentLiftingCoefficient(), flightConditionsElement, doc);
				
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
		JPADStaticWriteUtils.writeSingleNode("cL0_clean", input.getcL0Clean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLalpha_clean", input.getcLAlphaClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLmax_clean", input.getcLmaxClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLStar_clean", input.getcLstarClean(), cleanConfigurationDataElement, doc);
				
		org.w3c.dom.Element airfoilDataDistributionElement = doc.createElement("mean_airfoil");
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
		JPADStaticWriteUtils.writeSingleNode("delta_Clmax_of_each_slat", output.getDeltaClmaxSlatList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_Clmax_total_due_to_slats", output.getDeltaClmaxSlat(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CL0_of_each_flap", output.getDeltaCL0FlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CL0_total_due_to_flaps", output.getDeltaCL0Flap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_of_each_flap", output.getDeltaCLmaxFlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_total_due_to_flaps", output.getDeltaCLmaxFlap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_of_each_slat", output.getDeltaCLmaxSlatList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_total_due_to_slats", output.getDeltaCLmaxSlat(), highLiftDevicesEffectsElement, doc);
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
		
		org.w3c.dom.Element highLiftCurveDataElement = doc.createElement("high_lift_curve_point");
		outputRootElement.appendChild(highLiftCurveDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_array_clean", Arrays.toString(output.getAlphaListPlot().get(0)), highLiftCurveDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_array_clean", Arrays.toString(output.getcLListPlot().get(0)), highLiftCurveDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_array_high_lift", Arrays.toString(output.getAlphaListPlot().get(1)), highLiftCurveDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_array_high_lift", Arrays.toString(output.getcLListPlot().get(1)), highLiftCurveDataElement, doc);
		
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
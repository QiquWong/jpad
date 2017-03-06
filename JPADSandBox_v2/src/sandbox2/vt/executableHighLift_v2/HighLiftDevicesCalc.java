package sandbox2.vt.executableHighLift_v2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

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
import groovyjarjarasm.asm.commons.Method;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

//import static java.lang.Math.toRadians;
//import java.io.File;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import javax.measure.quantity.Angle;
//import javax.measure.unit.NonSI;
//import javax.measure.unit.SI;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import org.apache.commons.math3.linear.MatrixUtils;
//import org.apache.commons.math3.linear.RealMatrix;
//import org.jscience.physics.amount.Amount;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;
//import calculators.geometry.LSGeometryCalc;
//import configuration.MyConfiguration;
//import configuration.enumerations.AirfoilFamilyEnum;
//import configuration.enumerations.FlapTypeEnum;
//import configuration.enumerations.FoldersEnum;
//import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
//import database.databasefunctions.aerodynamics.DatabaseManager;
//import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
//import standaloneutils.JPADXmlReader;
//import standaloneutils.MyArrayUtils;
//import standaloneutils.MyChartToFileUtils;
//import standaloneutils.MyMathUtils;
//import standaloneutils.MyXMLReaderUtils;
//import writers.JPADStaticWriteUtils;

public class HighLiftDevicesCalc {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION:
	
	static InputTree input;
	static OutputTree output;
	
	/**************************************************************************************
	 * This method is in charge of reading data from a given XML input file and 
	 * put them inside an object of the InputTree class.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param pathToXML
	 * @throws ParserConfigurationException
	 */
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		input = new InputTree();
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// FLIGHT CONDITION:
		//---------------------------------------------------------------------------------------
//		List<String> alphaCurrentProperty = reader.getXMLPropertiesByPath("//flight_condition/alpha_current");
//		input.setAlphaCurrent(Amount.valueOf(Double.valueOf(alphaCurrentProperty.get(0)), NonSI.DEGREE_ANGLE));

		//---------------------------------------------------------------------------------
		// WING:	
		//---------------------------------------------------------------------------------------
		// Geometry:
		List<String> aspectRatioProperty = reader.getXMLPropertiesByPath("//aspect_ratio");
		input.setAspectRatio(Double.valueOf(aspectRatioProperty.get(0)));
		
		List<String> spanProperty = reader.getXMLPropertiesByPath("//span");
		input.setSpan(Amount.valueOf(Double.valueOf(spanProperty.get(0)), SI.METER));
		
		List<String> surfaceProperty = reader.getXMLPropertiesByPath("//surface");
		input.setSurface(Amount.valueOf(Double.valueOf(surfaceProperty.get(0)), SI.SQUARE_METRE));
		
		List<String> rootChordEquivalentWingProperty = reader.getXMLPropertiesByPath("//root_chord_equivalent_wing");
		input.setRootChordEquivalentWing(Amount.valueOf(Double.valueOf(rootChordEquivalentWingProperty.get(0)), SI.METER));
		
		List<String> sweepQuarterChordEquivalentProperty = reader.getXMLPropertiesByPath("//sweep_quarter_chord_equivalent_wing");
		input.setSweepQuarteChordEq(Amount.valueOf(Double.valueOf(sweepQuarterChordEquivalentProperty.get(0)), NonSI.DEGREE_ANGLE));
		
		List<String> taperRatioEquivalentProperty = reader.getXMLPropertiesByPath("//taper_ratio_equivalent_wing");
		input.setTaperRatioEq(Double.valueOf(taperRatioEquivalentProperty.get(0)));
		
		//---------------------------------------------------------------------------------------
		// Clean wing parameters:
		List<String> alphaStallCleanProperty = reader.getXMLPropertiesByPath("//alpha_stall_clean");
		input.setAlphaMaxClean(Amount.valueOf(Double.valueOf(alphaStallCleanProperty.get(0)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStarCleanProperty = reader.getXMLPropertiesByPath("//alpha_star_clean");
		input.setAlphaStarClean(Amount.valueOf(Double.valueOf(alphaStarCleanProperty.get(0)), NonSI.DEGREE_ANGLE));
		
		List<String> cL0CleanProperty = reader.getXMLPropertiesByPath("//cL0_clean");
		input.setcL0Clean(Double.valueOf(cL0CleanProperty.get(0)));
		
		List<String> cLAlphaCleanProperty = reader.getXMLPropertiesByPath("//cLalpha_clean");
		input.setcLAlphaClean(Amount.valueOf(Double.valueOf(cLAlphaCleanProperty.get(0)), NonSI.DEGREE_ANGLE.inverse()));
		
		List<String> cLmaxCleanProperty = reader.getXMLPropertiesByPath("//cLmax_clean");
		input.setcLmaxClean(Double.valueOf(cLmaxCleanProperty.get(0)));
		
		List<String> cLStarCleanProperty = reader.getXMLPropertiesByPath("//cLStar_clean");
		input.setcLstarClean(Double.valueOf(cLStarCleanProperty.get(0)));
		
		//---------------------------------------------------------------------------------------
		// Mean airfoil:
		List<String> clAlphaMeanAirfoilProperty = reader.getXMLPropertiesByPath("//cl_alpha_mean_airfoil");
		input.setClAlphaMeanAirfoil(Amount.valueOf(Double.valueOf(clAlphaMeanAirfoilProperty.get(0)), NonSI.DEGREE_ANGLE.inverse()));
		
		List<String> cl0MeanAirfoilProperty = reader.getXMLPropertiesByPath("//cl0_mean_airfoil");
		input.setCl0MeanAirfoil(Double.valueOf(cl0MeanAirfoilProperty.get(0)));
		
		List<String> leadingEdgeRadiusProperty = reader.getXMLPropertiesByPath("//leading_edge_radius_mean_airfoil");
		input.setLERadiusMeanAirfoil(Amount.valueOf(Double.valueOf(leadingEdgeRadiusProperty.get(0)), SI.METER));
		
		List<String> meanAirfoilChordProperty = reader.getXMLPropertiesByPath("//mean_airfoil_chord");
		input.setMeanAirfoilChord(Amount.valueOf(Double.valueOf(meanAirfoilChordProperty.get(0)), SI.METER));
		
		List<String> maxThicknessMeanAirfoilProperty = reader.getXMLPropertiesByPath("//max_thickness_mean_airfoil");
		input.setMaxthicknessMeanAirfoil(Double.valueOf(maxThicknessMeanAirfoilProperty.get(0)));
		
		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//airfoil_family");
		if(airfoilFamilyProperty.get(0).equals("NACA_4_DIGIT"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_4_Digit);
		else if(airfoilFamilyProperty.get(0).equals("NACA_5_DIGIT"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_5_Digit);
		else if(airfoilFamilyProperty.get(0).equals("NACA_63_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_63_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_64_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_64_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_65_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_65_Series);
		else if(airfoilFamilyProperty.get(0).equals("NACA_66_SERIES"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.NACA_66_Series);
		else if(airfoilFamilyProperty.get(0).equals("BICONVEX"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.BICONVEX);
		else if(airfoilFamilyProperty.get(0).equals("DOUBLE_WEDGE"))
			input.setMeanAirfoilFamily(AirfoilFamilyEnum.DOUBLE_WEDGE);
		else {
			System.err.println("NO VALID FAMILY TYPE!!");
			return;
		}
		
		//---------------------------------------------------------------------------------------
		// Print data:
//		System.out.println("\tAlpha current = " + input.getAlphaCurrent().getEstimatedValue() + " " + input.getAlphaCurrent().getUnit() + "\n");
		System.out.println("\tAspect Ratio = " + input.getAspectRatio());
		System.out.println("\tSpan = " + input.getSpan().getEstimatedValue() + " " + input.getSpan().getUnit());
		System.out.println("\tSurface = " + input.getSurface().getEstimatedValue() + " " + input.getSurface().getUnit());
		System.out.println("\tSweep quarter chord equivalent wing = " + input.getSweepQuarteChordEq().getEstimatedValue() + " " + input.getSweepQuarteChordEq().getUnit());
		System.out.println("\tTaper ratio equivalent wing = " + input.getTaperRatioEq());
		System.out.println("\n\tAlpha stall clean = " + input.getAlphaMaxClean().getEstimatedValue() + " " + input.getAlphaMaxClean().getUnit());
		System.out.println("\tAlpha star clean = " + input.getAlphaStarClean().getEstimatedValue() + " " + input.getAlphaStarClean().getUnit());
		System.out.println("\tCL0 clean = " + input.getcL0Clean());
		System.out.println("\tCLalpha clean = " + input.getcLAlphaClean().getEstimatedValue() + " " + input.getcLAlphaClean().getUnit());
		System.out.println("\tCLmax clean = " + input.getcLmaxClean());
		System.out.println("\tCLstar clean = " + input.getcLstarClean());
		System.out.println("\n\tClalpha mean airfoil = " + input.getClAlphaMeanAirfoil().getEstimatedValue() + " " + input.getClAlphaMeanAirfoil().getUnit());
		System.out.println("\n\tCl0 mean airfoil = " + input.getCl0MeanAirfoil());
		System.out.println("\tLeading edge radius mean airfoil = " + input.getLERadiusMeanAirfoil().getEstimatedValue() + " " + input.getLERadiusMeanAirfoil().getUnit());
		System.out.println("\tMax thickness mean airfoil = " + input.getMaxthicknessMeanAirfoil());
		System.out.println("\tMean airfoil family = " + input.getMeanAirfoilFamily() + "\n");
		
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

		// TODO: SET THE REQUIRED PARAMETERS INSIDE THE DEFAULT WING
		
		LiftingSurface theWing = new LiftingSurfaceBuilder("Wing", ComponentEnum.WING, aeroDatabaseReader, highLiftDatabaseReader)
				.liftingSurfaceCreator(
						new LiftingSurfaceCreator
						.LiftingSurfaceCreatorBuilder("Wing", Boolean.TRUE, ComponentEnum.WING)
						.build()
				)
		.build();
		
		LiftCalc.calculateHighLiftDevicesEffects(
				theWing,
				input.getDeltaFlap(),
				input.getDeltaSlat(),
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
		System.out.println("\ndeltaCl0_flap_list = ");
		for(int i=0; i<output.getDeltaCl0FlapList().size(); i++)
			System.out.println(output.getDeltaCl0FlapList().get(i));
	
		System.out.println("\ndeltaCl0_flap = \n" + output.getDeltaCl0Flap());
	
		System.out.println("\ndeltaCL0_flap_list = ");
		for(int i=0; i<output.getDeltaCL0FlapList().size(); i++)
			System.out.println(output.getDeltaCL0FlapList().get(i));
	
		System.out.println("\ndeltaCL0_flap = \n" + output.getDeltaCL0Flap());
	
		System.out.println("\ndeltaClmax_flap_list = ");
		for(int i=0; i<output.getDeltaClmaxFlapList().size(); i++)
			System.out.println(output.getDeltaClmaxFlapList().get(i));
	
		System.out.println("\ndeltaClmax_flap = \n" + output.getDeltaClmaxFlap());
	
		System.out.println("\ndeltaCLmax_flap_list = ");
		for(int i=0; i<output.getDeltaCLmaxFlapList().size(); i++)
			System.out.println(output.getDeltaCLmaxFlapList().get(i));
	
		System.out.println("\ndeltaCLmax_flap = \n" + output.getDeltaCLmaxFlap());
		
		System.out.println("\ndeltaClmax_slat_list = ");
		for(int i=0; i<output.getDeltaClmaxSlatList().size(); i++)
			System.out.println(output.getDeltaClmaxSlatList().get(i));
	
		System.out.println("\ndeltaClmax_slat = \n" + output.getDeltaClmaxSlat());
	
		System.out.println("\ndeltaCLmax_slat_list = ");
		for(int i=0; i<output.getDeltaCLmaxSlatList().size(); i++)
			System.out.println(output.getDeltaCLmaxSlatList().get(i));
	
		System.out.println("\ndeltaCLmax_slat = \n" + output.getDeltaCLmaxSlat());
	
		System.out.println("\ncLalpha_new = \n" + output.getcLalphaHighLift().getEstimatedValue() + " " + output.getcLalphaHighLift().getUnit());
	
		System.out.println("\ndeltaCD_list = ");
		for(int i=0; i<output.getDeltaCDList().size(); i++)
			System.out.println(output.getDeltaCDList().get(i));
	
		System.out.println("\ndeltaCD = \n" + output.getDeltaCD());
	
		System.out.println("\ndeltaCMc_4_list = ");
		for(int i=0; i<output.getDeltaCMC4List().size(); i++)
			System.out.println(output.getDeltaCMC4List().get(i));
	
		System.out.println("\ndeltaCMc_4 = \n" + output.getDeltaCMC4());
		
		System.out.println("------------------DONE----------------------");
		
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
						input.getAlphaMaxClean().to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 2,
						nPoints
						)
				);
		
		output.getcLListPlot().add(
				LiftCalc.calculateCLvsAlphaArray(
						input.getcL0Clean(),
						input.getcLmaxClean(),
						input.getAlphaStarClean(), 
						input.getAlphaMaxClean(), 
						input.getcLAlphaClean(), 
						output.getAlphaListPlot().get(0)
						)
				);

		//--------------------------------------------------------------------------------------
		// BUILDING HIGH LIFT CURVE:
		//--------------------------------------------------------------------------------------
		double alphaHighLiftFirst = -13.0;
		
		output.getAlphaListPlot().add(
				MyArrayUtils.linspaceDouble(
						alphaHighLiftFirst,
						output.getAlphaMaxHighLift().to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 2,
						nPoints
						)
				);
		
		output.setcL0HighLift(input.getcL0Clean() + output.getDeltaCL0Flap());
		output.setcLmaxHighLift(input.getcLmaxClean() + output.getDeltaCLmaxFlap() + output.getDeltaCLmaxSlat());
		output.setAlphaMaxHighLift(
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
										input.getMaxthicknessMeanAirfoil(),
										input.getMeanAirfoilFamily()
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
					output.getAlphaMaxHighLift().to(NonSI.DEGREE_ANGLE).minus(
							input.getAlphaMaxClean().to(NonSI.DEGREE_ANGLE)
							.minus(input.getAlphaStarClean().to(NonSI.DEGREE_ANGLE)
									)
							)
					);
									
		output.getcLListPlot().add(
				LiftCalc.calculateCLvsAlphaArray(
						output.getcL0HighLift(),
						output.getcLmaxHighLift(),
						output.getAlphaStarHighLift(), 
						output.getAlphaMaxHighLift(), 
						output.getcLalphaHighLift(), 
						output.getAlphaListPlot().get(1)
						)
				);
		
		System.out.println(" \n-----------CLEAN CONFIGURATION-------------- ");
		System.out.println(" Alpha max = " + input.getAlphaMaxClean().getEstimatedValue() + " " + input.getAlphaMaxClean().getUnit());
		System.out.println(" Alpha star = " + input.getAlphaStarClean().getEstimatedValue() + " " + input.getAlphaStarClean().getUnit());
		System.out.println(" CL max = " + input.getcLmaxClean());
		System.out.println(" CL star = " + input.getcLstarClean());
		System.out.println(" CL0 = " + input.getcL0Clean());
		System.out.println(" CL alpha = " + input.getcLAlphaClean().getEstimatedValue() + " " + input.getcLAlphaClean().getUnit());
		
		System.out.println(" \n-----------HIGH LIFT DEVICES ON-------------- ");
		System.out.println(" Alpha max = " + output.getAlphaMaxHighLift().getEstimatedValue() + " " + output.getAlphaMaxHighLift().getUnit());
		System.out.println(" Alpha star = " + output.getAlphaStarHighLift().getEstimatedValue() + " " + output.getAlphaStarHighLift().getUnit());
		System.out.println(" CL max = " + output.getcLmaxHighLift());
		System.out.println(" CL star = " + output.getcLStarHighLift());
		System.out.println(" CL0 = " + output.getcL0HighLift());
		System.out.println(" CL alpha = " + output.getcLalphaHighLift().getEstimatedValue() + " " + output.getcLalphaHighLift().getUnit());

		List<String> legend  = new ArrayList<>(); 
		legend.add("clean");
		legend.add("high lift");

		System.out.println(" \n-----------WRITING CHART TO FILE-------------- ");
		
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

		System.out.println(" \n-------------------DONE----------------------- ");
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

//		org.w3c.dom.Element flightConditionsElement = doc.createElement("flight_condition");
//		inputRootElement.appendChild(flightConditionsElement);
//
//		JPADStaticWriteUtils.writeSingleNode("alpha_current", input.getAlphaCurrent(), flightConditionsElement, doc);
				
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		inputRootElement.appendChild(wingDataElement);
		
		org.w3c.dom.Element geometryDataElement = doc.createElement("geometry");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("span", input.getSpan(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("root_chord_equivalent_wing", input.getRootChordEquivalentWing(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("sweep_quarter_chord_equivalent_wing", input.getSweepQuarteChordEq(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("taper_ratio_equivalent_wing", input.getTaperRatioEq(), geometryDataElement, doc);
		
		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("clean_configuration_parameters");
		wingDataElement.appendChild(cleanConfigurationDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_stall_clean", input.getAlphaMaxClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star_clean", input.getAlphaStarClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL0_clean", input.getcL0Clean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLalpha_clean", input.getcLAlphaClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLmax_clean", input.getcLmaxClean(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLStar_clean", input.getcLstarClean(), cleanConfigurationDataElement, doc);
				
		org.w3c.dom.Element meanAirfoilDataElement = doc.createElement("mean_airfoil");
		wingDataElement.appendChild(meanAirfoilDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("cl_alpha_mean_airfoil", input.getClAlphaMeanAirfoil(), meanAirfoilDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cl0_mean_airfoil", input.getCl0MeanAirfoil(), meanAirfoilDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("leading_edge_radius_mean_airfoil", input.getLERadiusMeanAirfoil(), meanAirfoilDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("mean_airfoil_chord", input.getMeanAirfoilChord(), meanAirfoilDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", input.getMaxthicknessMeanAirfoil(), meanAirfoilDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("airfoil_family", input.getMeanAirfoilFamily(), meanAirfoilDataElement, doc);

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
		
		JPADStaticWriteUtils.writeSingleNode("alpha_max_high_lift", output.getAlphaMaxHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star_high_lift", output.getAlphaStarHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLmax_high_lift", output.getcLmaxHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_star_high_lift", output.getcLStarHighLift(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha_high_lift", output.getcLalphaHighLift(), highLiftGlobalDataElement, doc);
		
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
package it.unina.daf.test.highliftdeviceseffects;

import static java.lang.Math.toRadians;

import java.io.File;
import java.util.ArrayList;
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

import calculators.geometry.LSGeometryCalc;
import configuration.MyConfiguration;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;
import configuration.enumerations.FoldersEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyChartToFileUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

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
		List<String> alphaCurrentProperty = reader.getXMLPropertiesByPath("//flight_condition/alpha_current");
		input.setAlphaCurrent(Amount.valueOf(Double.valueOf(alphaCurrentProperty.get(0)), NonSI.DEGREE_ANGLE));

		System.out.println("\tAlpha current = " + input.getAlphaCurrent().getEstimatedValue() + " " + input.getAlphaCurrent().getUnit() + "\n");

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
		AerodynamicDatabaseReader aeroDatabaseReader = DatabaseManager.initializeAeroDatabase(new AerodynamicDatabaseReader(
				databaseFolderPath,	aerodynamicDatabaseFileName),
				databaseFolderPath);
		
		HighLiftDatabaseReader highLiftDatabaseReader = DatabaseManager.initializeHighLiftDatabase(new HighLiftDatabaseReader(
				databaseFolderPath, highLiftDatabaseFileName),
				databaseFolderPath);
				
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
		}
		
		//--------------------------------------------
		// CALCULATION OF HIGH LIFT DEVICES EFFECTS:
		System.out.println("\nCalculating high lift devices effects...");
		System.out.println("\n-----------HIGH LIFT DEVICES EFFECTS-------------- ");
		
		//---------------------------------------------
		// deltaCl0 (flap)
		List<Double> thetaF = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) 
			thetaF.add(Math.acos((2*input.getCfc().get(i))-1));

		List<Double> alphaDelta = new ArrayList<Double>();
		for(int i=0; i<thetaF.size(); i++)
			alphaDelta.add(1-((thetaF.get(i)-Math.sin(thetaF.get(i)))/Math.PI));

		List<Double> etaDeltaFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlapPlain(input.getDeltaFlap().get(i).getEstimatedValue(), input.getCfc().get(i)));
			else
				etaDeltaFlap.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
						);
		}

		List<Double> deltaCl0First = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCl0First.add(
					alphaDelta.get(i).doubleValue()
					*etaDeltaFlap.get(i).doubleValue()
					*input.getDeltaFlap().get(i).getEstimatedValue()
					*input.getClAlphaMeanAirfoil().getEstimatedValue()
					);

		List<Double> deltaCCfFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCCfFlap.add(
					highLiftDatabaseReader
					.getDeltaCCfVsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
					);

		List<Double> cFirstCFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			cFirstCFlap.add(1+(deltaCCfFlap.get(i).doubleValue()*input.getCfc().get(i).doubleValue()));

		for(int i=0; i<input.getFlapsNumber(); i++)
			output.getDeltaCl0FlapList().add(
					(deltaCl0First.get(i).doubleValue()*cFirstCFlap.get(i).doubleValue())
					+(input.getCl0MeanAirfoil()*(cFirstCFlap.get(i).doubleValue()-1))
					);

		double deltaCl0FlapTemp = 0;
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCl0FlapTemp += output.getDeltaCl0FlapList().get(i);
		
		output.setDeltaCl0Flap(deltaCl0FlapTemp);

		//---------------------------------------------------------------
		// deltaClmax (flap)
		List<Double> deltaClmaxBase = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaClmaxBase.add(
					highLiftDatabaseReader
					.getDeltaCLmaxBaseVsTc(
							input.getMaxthicknessMeanAirfoil(),
							flapTypeIndex.get(i)
							)
					);

		List<Double> k1 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			if (input.getCfc().get(i) <= 0.30)
				k1.add(highLiftDatabaseReader
						.getK1vsFlapChordRatio(input.getCfc().get(i), flapTypeIndex.get(i))
						);
			else if ((input.getCfc().get(i) > 0.30) && ((flapTypeIndex.get(i) == 2) || (flapTypeIndex.get(i) == 4) || (flapTypeIndex.get(i) == 5)))
				k1.add(0.04*(input.getCfc().get(i)*100));
			else if ((input.getCfc().get(i) > 0.30) && ((flapTypeIndex.get(i) == 1) || (flapTypeIndex.get(i) == 3) ))
				k1.add((608.31*Math.pow(input.getCfc().get(i), 5))
						-(626.15*Math.pow(input.getCfc().get(i), 4))
						+(263.4*Math.pow(input.getCfc().get(i), 3))
						-(62.946*Math.pow(input.getCfc().get(i), 2))
						+(10.638*input.getCfc().get(i))
						+0.0064
						);

		List<Double> k2 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			k2.add(highLiftDatabaseReader
					.getK2VsDeltaFlap(input.getDeltaFlap().get(i).getEstimatedValue(), flapTypeIndex.get(i))
					);

		List<Double> k3 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			k3.add(highLiftDatabaseReader
					.getK3VsDfDfRef(
							input.getDeltaFlap().get(i).getEstimatedValue(),
							deltaFlapRef.get(i),
							flapTypeIndex.get(i)
							)
					);

		for(int i=0; i<input.getFlapsNumber(); i++) {
			output.getDeltaClmaxFlapList().add(k1.get(i).doubleValue()
					*k2.get(i).doubleValue()
					*k3.get(i).doubleValue()
					*deltaClmaxBase.get(i).doubleValue()
					);
		}
		
		double deltaClmaxFlapTemp = 0;
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaClmaxFlapTemp += output.getDeltaClmaxFlapList().get(i);
		
		output.setDeltaClmaxFlap(deltaClmaxFlapTemp);
	
		
		//---------------------------------------------------------------
		// deltaClmax (slat)
		if(input.getSlatsNumber() > 0.0) {

			List<Double> dCldDelta = new ArrayList<Double>();
			for(int i=0; i<input.getSlatsNumber(); i++)
				dCldDelta.add(highLiftDatabaseReader
						.getDCldDeltaVsCsC(input.getCsc().get(i))
						);

			List<Double> etaMaxSlat = new ArrayList<Double>();
			for(int i=0; i<input.getSlatsNumber(); i++)
				etaMaxSlat.add(highLiftDatabaseReader
						.getEtaMaxVsLEradiusTicknessRatio(
								(input.getLERadiusMeanAirfoil().divide(input.getMeanAirfoilChord())).getEstimatedValue(),
								input.getMaxthicknessMeanAirfoil())
						);

			List<Double> etaDeltaSlat = new ArrayList<Double>();
			for(int i=0; i<input.getSlatsNumber(); i++)
				etaDeltaSlat.add(
						highLiftDatabaseReader
						.getEtaDeltaVsDeltaSlat(input.getDeltaSlat().get(i).getEstimatedValue())
						);

			for(int i=0; i<input.getSlatsNumber(); i++)
				output.getDeltaClmaxSlatList().add(
						dCldDelta.get(i).doubleValue()
						*etaMaxSlat.get(i).doubleValue()
						*etaDeltaSlat.get(i).doubleValue()
						*input.getDeltaSlat().get(i).getEstimatedValue()
						*input.getcExtCSlat().get(i)
						);

			double deltaClmaxSlatTemp = 0.0;
			for(int i=0; i<input.getSlatsNumber(); i++)
				deltaClmaxSlatTemp += output.getDeltaClmaxSlatList().get(i);
			
			output.setDeltaClmaxSlat(deltaClmaxSlatTemp);
		}

		//---------------------------------------------------------------
		// deltaCL0 (flap)
		List<Double> kc = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			kc.add(highLiftDatabaseReader
					.getKcVsAR(
							input.getAspectRatio(),
							alphaDelta.get(i))	
					);

		List<Double> kb = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			kb.add(highLiftDatabaseReader
					.getKbVsFlapSpanRatio(
							input.getEtaInFlap().get(i),
							input.getEtaOutFlap().get(i),
							input.getTaperRatioEq())	
					);
		
		for(int i=0; i<input.getFlapsNumber(); i++)
			output.getDeltaCL0FlapList().add(
					kb.get(i).doubleValue()
					*kc.get(i).doubleValue()
					*output.getDeltaCl0FlapList().get(i)
					*(input.getcLAlphaClean().divide(input.getClAlphaMeanAirfoil())).getEstimatedValue()
					);

		double deltaCL0FlapTemp = 0.0;
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCL0FlapTemp += output.getDeltaCL0FlapList().get(i);
		
		output.setDeltaCL0Flap(deltaCL0FlapTemp);

		//---------------------------------------------------------------
		// deltaCLmax (flap)
		List<Double> flapSurface = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			flapSurface.add(
					Math.abs(
							input.getSpan().getEstimatedValue()							
							/2*input.getRootChordEquivalentWing().getEstimatedValue()
							*(2-((1-input.getTaperRatioEq())*(input.getEtaInFlap().get(i)+input.getEtaOutFlap().get(i))))
							*(input.getEtaOutFlap().get(i)-input.getEtaInFlap().get(i))
							)
					);
		
		List<Double> kLambdaFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			kLambdaFlap.add(
					Math.pow(Math.cos(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()),0.75)
					*(1-(0.08*Math.pow(Math.cos(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()), 2)))
					);

		for(int i=0; i<flapTypeIndex.size(); i++) {
			output.getDeltaCLmaxFlapList().add(output.getDeltaClmaxFlapList().get(i)
					*(flapSurface.get(i)/input.getSurface().getEstimatedValue())
					*kLambdaFlap.get(i)
					);
		}
		
		double deltaCLmaxFlapTemp = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCLmaxFlapTemp += output.getDeltaCLmaxFlapList().get(i);
		
		output.setDeltaCLmaxFlap(deltaCLmaxFlapTemp);

		//---------------------------------------------------------------
		// deltaCLmax (slat)
		if(input.getSlatsNumber() > 0) {

			List<Double> kLambdaSlat = new ArrayList<Double>();
			for(int i=0; i<input.getSlatsNumber(); i++)
				kLambdaSlat.add(
						Math.pow(Math.cos(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()),0.75)
						*(1-(0.08*Math.pow(Math.cos(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()), 2)))
						);

			List<Double> slatSurface = new ArrayList<Double>();
			for(int i=0; i<input.getSlatsNumber(); i++)
				slatSurface.add(
						Math.abs(input.getSpan().getEstimatedValue()
								/2*input.getRootChordEquivalentWing().getEstimatedValue()
								*(2-(1-input.getTaperRatioEq())*(input.getEtaInSlat().get(i)+input.getEtaOutSlat().get(i)))
								*(input.getEtaOutSlat().get(i)-input.getEtaInSlat().get(i))
								)
						);
			
			for(int i=0; i<input.getSlatsNumber(); i++)
				output.getDeltaCLmaxSlatList().add(output.getDeltaClmaxSlatList().get(i)
						*(slatSurface.get(i)/input.getSurface().getEstimatedValue())
						*kLambdaSlat.get(i));

			double deltaCLmaxSlatTemp = 0.0;
			for(int i=0; i<input.getSlatsNumber(); i++)
				deltaCLmaxSlatTemp += output.getDeltaCLmaxSlatList().get(i);
			
			output.setDeltaCLmaxSlat(deltaCLmaxSlatTemp);
		}
		//---------------------------------------------------------------
		// new CLalpha
		
		List<Double> swf = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			output.getcLalphaNewList().add(Amount.valueOf(
					(input.getcLAlphaClean().getEstimatedValue()
					*(1+((output.getDeltaCL0FlapList().get(i)/output.getDeltaCl0FlapList().get(i))
							*(cFirstCFlap.get(i)*(1-((input.getCfc().get(i))*(1/cFirstCFlap.get(i))
									*Math.pow(Math.sin(input.getDeltaFlap().get(i).to(SI.RADIAN).getEstimatedValue()), 2)))-1))))
					, NonSI.DEGREE_ANGLE.inverse())
					);
			swf.add(flapSurface.get(i)/input.getSurface().getEstimatedValue());
		}

		double swfTot = 0;
		for(int i=0; i<swf.size(); i++)
			swfTot += swf.get(i);

		double cLalphaNewTemp = 0.0;
		for(int i=0; i<input.getFlapsNumber(); i++)
			cLalphaNewTemp += output.getcLalphaNewList().get(i).getEstimatedValue()*swf.get(i);

		cLalphaNewTemp /= swfTot;
		
		output.setcLalphaNew(Amount.valueOf(cLalphaNewTemp, NonSI.DEGREE_ANGLE.inverse()));

		//---------------------------------------------------------------
		// deltaCD
		List<Double> delta1 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta1.add(
						highLiftDatabaseReader
						.getDelta1VsCfCPlain(input.getCfc().get(i), input.getMaxthicknessMeanAirfoil())
						);
			else
				delta1.add(
						highLiftDatabaseReader
						.getDelta1VsCfCSlotted(input.getCfc().get(i), input.getMaxthicknessMeanAirfoil())
						);
		}

		List<Double> delta2 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			if(flapTypeIndex.get(i) == 3.0)
				delta2.add(
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapPlain(input.getDeltaFlap().get(i).getEstimatedValue())
						);
			else
				delta2.add(
						highLiftDatabaseReader
						.getDelta2VsDeltaFlapSlotted(input.getDeltaFlap().get(i).getEstimatedValue(), input.getMaxthicknessMeanAirfoil())
						);
		}

		List<Double> delta3 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++) {
			delta3.add(
					highLiftDatabaseReader
					.getDelta3VsBfB(input.getEtaInFlap().get(i), input.getEtaOutFlap().get(i), input.getTaperRatioEq())
					);
		}

		for(int i=0; i<input.getFlapsNumber(); i++) {
			output.getDeltaCDList().add(delta1.get(i)*delta2.get(i)*delta3.get(i));
		}

		double deltaCDTemp = 0.0;
		for(int i=0; i<input.getFlapsNumber(); i++)
			deltaCDTemp += output.getDeltaCDList().get(i);
		
		output.setDeltaCD(deltaCDTemp);

		//---------------------------------------------------------------
		// deltaCM_c/4
		List<Double> mu1 = new ArrayList<Double>();
		for (int i=0; i<input.getFlapsNumber(); i++)
			if(flapTypeIndex.get(i) == 3.0)
				mu1.add(
						highLiftDatabaseReader
						.getMu1VsCfCFirstPlain(
								(input.getCfc().get(i))*(1/cFirstCFlap.get(i)),
								input.getDeltaFlap().get(i).getEstimatedValue()
								)
						);
			else
				mu1.add(highLiftDatabaseReader
						.getMu1VsCfCFirstSlottedFowler((input.getCfc().get(i))*(1/cFirstCFlap.get(i)))
						);

		List<Double> mu2 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			mu2.add(highLiftDatabaseReader
					.getMu2VsBfB(
							input.getEtaInFlap().get(i),
							input.getEtaOutFlap().get(i),
							input.getTaperRatioEq()
							)
					);

		List<Double> mu3 = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			mu3.add(highLiftDatabaseReader
					.getMu3VsBfB(
							input.getEtaInFlap().get(i),
							input.getEtaOutFlap().get(i),
							input.getTaperRatioEq()
							)
					);

		
		double cL = calcCLatAlphaHighLiftDevice(input.getAlphaCurrent(), aeroDatabaseReader);
		for(int i=0; i<input.getFlapsNumber(); i++)
			output.getDeltaCMC4List().add(
					(mu2.get(i)*(-(mu1.get(i)*output.getDeltaClmaxFlapList().get(i)
							*cFirstCFlap.get(i))-(cFirstCFlap.get(i)
									*((cFirstCFlap.get(i))-1)
									*(cL + (output.getDeltaClmaxFlapList().get(i)
											*(1-(flapSurface.get(i)/input.getSurface().getEstimatedValue()))))
									*(1/8)))) + (0.7*(input.getAspectRatio()/(1+(input.getAspectRatio()/2)))
											*mu3.get(i)*output.getDeltaClmaxFlapList().get(i)
											*Math.tan(input.getSweepQuarteChordEq().getEstimatedValue()))
					);

		double deltaCMC4Temp = 0.0;
		for(int i=0; i<flapTypeIndex.size(); i++)
			deltaCMC4Temp += output.getDeltaCMC4List().get(i);
		
		output.setDeltaCMC4(deltaCMC4Temp);
		
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

		System.out.println("\ncLalpha_new_list = ");
		for(int i=0; i<output.getcLalphaNewList().size(); i++)
			System.out.println(output.getcLalphaNewList().get(i).getEstimatedValue() + " " + output.getcLalphaNewList().get(i).getUnit());

		System.out.println("\ncLalpha_new = \n" + output.getcLalphaNew().getEstimatedValue() + " " + output.getcLalphaNew().getUnit());

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
	
	/********************************************************************************************
	 * This method calculates CL at alpha given as input for a wing with high lift devices.
	 * This method calculates both linear trait and non linear trait. 
	 * It use the NasaBlackwell method in order to evaluate the slope of the linear trait
	 * and it builds the non-linear trait using a cubic interpolation. 
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param alpha the actual angle of attack (radians or degree)
	 * @return
	 */
	private static double calcCLatAlphaHighLiftDevice(
			Amount<Angle> alpha,
			AerodynamicDatabaseReader aeroDatabaseReader){

		if (alpha.getUnit() == NonSI.DEGREE_ANGLE) 
			alpha = alpha.to(SI.RADIAN);

		Amount<?> cLAlphaFlap = output.getcLalphaNew().to(SI.RADIAN.inverse());
		Amount<Angle> alphaStarCleanAmount = input.getAlphaStarClean().to(SI.RADIAN);
		double cLStarClean = input.getcLstarClean();
		double cL0Clean =  input.getcL0Clean();
		double cL0HighLift = cL0Clean + output.getDeltaCL0Flap();
		double qValue = cL0HighLift;
		double alphaStar = (cLStarClean - qValue)/cLAlphaFlap.getEstimatedValue();
		double cLMaxClean = input.getcLmaxClean();
		Amount<Angle> alphaMax = input.getAlphaMaxClean().to(SI.RADIAN);	
		double cLMaxFlap = cLMaxClean + output.getDeltaCLmaxFlap() + output.getDeltaCLmaxSlat();

		output.setcLmaxFlapSlat(cLMaxFlap);
		
		double alphaMaxHighLift;

		//recognizing airfoil family
		int airfoilFamilyIndex = 0;
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit) 
			airfoilFamilyIndex = 1;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_5_Digit)
			airfoilFamilyIndex = 2;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_63_Series)
			airfoilFamilyIndex = 3;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_64_Series)
			airfoilFamilyIndex = 4;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_65_Series)
			airfoilFamilyIndex = 5;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_66_Series)
			airfoilFamilyIndex = 6;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.BICONVEX)
			airfoilFamilyIndex = 7;
		else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.DOUBLE_WEDGE)
			airfoilFamilyIndex = 8;
		
		double sharpnessParameterLE = aeroDatabaseReader.getDeltaYvsThickness(input.getMaxthicknessMeanAirfoil(), airfoilFamilyIndex);
		
		alphaMaxHighLift = ((cLMaxFlap-cL0HighLift)/cLAlphaFlap.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue()) 
				+ (aeroDatabaseReader.getD_Alpha_Vs_LambdaLE_VsDy(
						LSGeometryCalc.calculateSweep(input.getAspectRatio(), input.getTaperRatioEq(), input.getSweepQuarteChordEq().getEstimatedValue(), 0.0, 0.25).getEstimatedValue(),
						sharpnessParameterLE));

		Amount<Angle> alphaMaxHighLiftAmount = Amount.valueOf(alphaMaxHighLift, NonSI.DEGREE_ANGLE);

		output.setAlphaMaxFlapSlat(alphaMaxHighLiftAmount.to(NonSI.DEGREE_ANGLE));
		
		double alphaStarFlap; 

		if(input.getSlatsNumber() == 0.0)
			alphaStarFlap = (alphaStar + alphaStarCleanAmount.getEstimatedValue())/2;
		else
			alphaStarFlap = alphaMaxHighLift-(alphaMax.to(NonSI.DEGREE_ANGLE).getEstimatedValue()-alphaStarCleanAmount.to(NonSI.DEGREE_ANGLE).getEstimatedValue());

		double cLStarFlap = cLAlphaFlap.to(NonSI.DEGREE_ANGLE.inverse()).getEstimatedValue() * alphaStarFlap + qValue;	
		output.setcLStarFlapSlat(cLStarFlap);
		output.setAlphaStarFlapSlat(Amount.valueOf(alphaStarFlap,NonSI.DEGREE_ANGLE));

		if (alpha.getEstimatedValue() < alphaStarFlap ){ 
			double cLActual = cLAlphaFlap.getEstimatedValue() * alpha.getEstimatedValue() + qValue;	
			return cLActual;
		}
		else{
			double[][] matrixData = { {Math.pow(alphaMaxHighLift, 3), Math.pow(alphaMaxHighLift, 2)
				, alphaMaxHighLift,1.0},
					{3* Math.pow(alphaMaxHighLift, 2), 2*alphaMaxHighLift, 1.0, 0.0},
					{3* Math.pow(alphaStarFlap, 2), 2*alphaStarFlap, 1.0, 0.0},
					{Math.pow(alphaStarFlap, 3), Math.pow(alphaStarFlap, 2),alphaStarFlap,1.0}};
			RealMatrix m = MatrixUtils.createRealMatrix(matrixData);


			double [] vector = {cLMaxFlap, 0, cLAlphaFlap.getEstimatedValue(), cLStarFlap};

			double [] solSystem = MyMathUtils.solveLinearSystem(m, vector);

			double a = solSystem[0];
			double b = solSystem[1];
			double c = solSystem[2];
			double d = solSystem[3];

			double clActual = a * Math.pow(alpha.getEstimatedValue(), 3) + 
					b * Math.pow(alpha.getEstimatedValue(), 2) + 
					c * alpha.getEstimatedValue() + d;

			return clActual;
		}
	}
	
	private static void plotCurves(AerodynamicDatabaseReader aeroDatabaseReader) throws InstantiationException, IllegalAccessException{ 

		String folderPathHL = MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR);
		
		//--------------------------------------------------------------------------------------
		// BUILDING CLEAN CURVE:
		//--------------------------------------------------------------------------------------
		double alphaCleanFirst = -10.0;
		Amount<Angle> alphaActual;
		
		int nPoints = 40;
		
		Amount<Angle> alphaStarClean = input.getAlphaStarClean().to(SI.RADIAN);
		double cLStarClean = input.getcLstarClean();
		double cLalphaClean = input.getcLAlphaClean().to(SI.RADIAN.inverse()).getEstimatedValue();
		double cL0Clean = cLStarClean - cLalphaClean*alphaStarClean.getEstimatedValue();

		double cLMaxClean = input.getcLmaxClean();
		Amount<Angle> alphaMaxClean = input.getAlphaMaxClean().to(SI.RADIAN);
		
		Double[] alphaCleanArrayPlot = new Double[nPoints];
 		Double[] cLCleanArrayPlot = new Double[nPoints]; 
		
		alphaCleanArrayPlot = MyArrayUtils.linspaceDouble(alphaCleanFirst, alphaMaxClean.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 2, nPoints);
		cLCleanArrayPlot = new Double [nPoints];

		double[][] matrixDataClean = { {Math.pow(alphaMaxClean.getEstimatedValue(), 3),
			Math.pow(alphaMaxClean.getEstimatedValue(), 2),
			alphaMaxClean.getEstimatedValue(),1.0},
				{3* Math.pow(alphaMaxClean.getEstimatedValue(), 2),
				2*alphaMaxClean.getEstimatedValue(), 1.0, 0.0},
				{3* Math.pow(alphaStarClean.getEstimatedValue(), 2),
					2*alphaStarClean.getEstimatedValue(), 1.0, 0.0},
				{Math.pow(alphaStarClean.getEstimatedValue(), 3),
						Math.pow(alphaStarClean.getEstimatedValue(), 2),
						alphaStarClean.getEstimatedValue(),1.0}};
		
		RealMatrix mc = MatrixUtils.createRealMatrix(matrixDataClean);
		double [] vectorClean = {cLMaxClean, 0, cLalphaClean, cLStarClean};

		double [] solSystemC = MyMathUtils.solveLinearSystem(mc, vectorClean);

		double aC = solSystemC[0];
		double bC = solSystemC[1];
		double cC = solSystemC[2];
		double dC = solSystemC[3];

		for ( int i=0 ; i< alphaCleanArrayPlot.length ; i++){
			alphaActual = Amount.valueOf(toRadians(alphaCleanArrayPlot[i]), SI.RADIAN);
			if (alphaActual.getEstimatedValue() < alphaStarClean.getEstimatedValue()) { 
				cLCleanArrayPlot[i] = cLalphaClean*alphaActual.getEstimatedValue() + cL0Clean;}
			else {
				cLCleanArrayPlot[i] = aC * Math.pow(alphaActual.getEstimatedValue(), 3) + 
						bC * Math.pow(alphaActual.getEstimatedValue(), 2) + 
						cC * alphaActual.getEstimatedValue() + dC;
			}
		}

		//--------------------------------------------------------------------------------------
		// BUILDING HIGH LIFT CURVE:
		//--------------------------------------------------------------------------------------
		double alphaHighLiftFirst = -13.0;
		Amount<Angle> alphaHighLiftActual;
		
		Amount<Angle> alphaStarHighLift = output.getAlphaStarFlapSlat().to(SI.RADIAN);
		double cLStarHighLift = output.getcLStarFlapSlat();
		double cLalphaHighLift = output.getcLalphaNew().to(SI.RADIAN.inverse()).getEstimatedValue();
		double cL0HighLift = cLStarHighLift - cLalphaHighLift*alphaStarHighLift.getEstimatedValue();
		
		double cLMaxHighLift = output.getcLmaxFlapSlat();
		Amount<Angle> alphaMaxHighLift = output.getAlphaMaxFlapSlat().to(SI.RADIAN);
		
		Double[] alphaHighLiftArrayPlot = new Double[nPoints];
		Double[] cLArrayHighLiftPlot = new Double [nPoints];

		alphaHighLiftArrayPlot = MyArrayUtils.linspaceDouble(alphaHighLiftFirst, alphaMaxHighLift.to(NonSI.DEGREE_ANGLE).getEstimatedValue() + 4, nPoints);
		cLArrayHighLiftPlot = new Double [nPoints];
		
		double[][] matrixDataHighLift = { {Math.pow(alphaMaxHighLift.getEstimatedValue(), 3),
			Math.pow(alphaMaxHighLift.getEstimatedValue(), 2),
			alphaMaxHighLift.getEstimatedValue(),1.0},
				{3* Math.pow(alphaMaxHighLift.getEstimatedValue(), 2),
				2*alphaMaxHighLift.getEstimatedValue(), 1.0, 0.0},
				{3* Math.pow(alphaStarHighLift.getEstimatedValue(), 2),
					2*alphaStarHighLift.getEstimatedValue(), 1.0, 0.0},
				{Math.pow(alphaStarHighLift.getEstimatedValue(), 3),
						Math.pow(alphaStarHighLift.getEstimatedValue(), 2),
						alphaStarHighLift.getEstimatedValue(),1.0}};
		
		RealMatrix mhl = MatrixUtils.createRealMatrix(matrixDataHighLift);
		double [] vectorHighLift = {cLMaxHighLift, 0, cLalphaHighLift, cLStarHighLift};

		double [] solSystemHL = MyMathUtils.solveLinearSystem(mhl, vectorHighLift);

		double aHL = solSystemHL[0];
		double bHL = solSystemHL[1];
		double cHL = solSystemHL[2];
		double dHL = solSystemHL[3];

		for ( int i=0 ; i< alphaHighLiftArrayPlot.length ; i++){
			alphaHighLiftActual = Amount.valueOf(toRadians(alphaHighLiftArrayPlot[i]), SI.RADIAN);
			if (alphaHighLiftActual.getEstimatedValue() < alphaStarHighLift.getEstimatedValue()) { 
				cLArrayHighLiftPlot[i] = cLalphaHighLift*alphaHighLiftActual.getEstimatedValue() + cL0HighLift;}
			else {
				cLArrayHighLiftPlot[i] = aHL * Math.pow(alphaHighLiftActual.getEstimatedValue(), 3) + 
						bHL * Math.pow(alphaHighLiftActual.getEstimatedValue(), 2) + 
						cHL * alphaHighLiftActual.getEstimatedValue() + dHL;
			}
		}
		
		System.out.println(" \n-----------CLEAN CONFIGURATION-------------- ");
		System.out.println(" Alpha max = " + input.getAlphaMaxClean().getEstimatedValue() + " " + input.getAlphaMaxClean().getUnit());
		System.out.println(" Alpha star = " + input.getAlphaStarClean().getEstimatedValue() + " " + input.getAlphaStarClean().getUnit());
		System.out.println(" CL max = " + input.getcLmaxClean());
		System.out.println(" CL star = " + input.getcLstarClean());
		System.out.println(" CL alpha = " + input.getcLAlphaClean().getEstimatedValue() + " " + input.getcLAlphaClean().getUnit());
		
		System.out.println(" \n-----------HIGH LIFT DEVICES ON-------------- ");
		System.out.println(" Alpha max = " + output.getAlphaMaxFlapSlat().getEstimatedValue() + " " + output.getAlphaMaxFlapSlat().getUnit());
		System.out.println(" Alpha star = " + output.getAlphaStarFlapSlat().getEstimatedValue() + " " + output.getAlphaStarFlapSlat().getUnit());
		System.out.println(" CL max = " + output.getcLmaxFlapSlat());
		System.out.println(" CL star = " + output.getcLStarFlapSlat());
		System.out.println(" CL alpha = " + output.getcLalphaNew().getEstimatedValue() + " " + output.getcLalphaNew().getUnit());

		//--------------------------------------------------------------------------------------
		// Convert from double to Double in order to use JFreeChart to plot.
		
		output.getAlphaListPlot().add(alphaCleanArrayPlot);
		output.getAlphaListPlot().add(alphaHighLiftArrayPlot);

		output.getcLListPlot().add(cLCleanArrayPlot);
		output.getcLListPlot().add(cLArrayHighLiftPlot);

		List<String> legend  = new ArrayList<>(); 

		legend.add("clean");
		legend.add("high lift");

		System.out.println(" \n-----------WRITING CHART TO FILE-------------- ");
		
		MyChartToFileUtils.plotJFreeChart(
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
				JPADStaticWriteUtils.createNewFolder(folderPathHL + "high_lift_charts" + File.separator),
				"lift_curves");

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
		
		org.w3c.dom.Element rootElement = doc.createElement("HiLDE_Calculator");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element flightConditionsElement = doc.createElement("flight_condition");
		inputRootElement.appendChild(flightConditionsElement);

		JPADStaticWriteUtils.writeSingleNode("alpha_current", input.getAlphaCurrent(), flightConditionsElement, doc);
				
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
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_Cl0_of_each_flap", output.getDeltaCl0FlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_Cl0_total_due_to_flaps", output.getDeltaCl0Flap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_Clmax_of_each_flap", output.getDeltaClmaxFlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_Clmax_total_due_to_flaps", output.getDeltaClmaxFlap(), highLiftDevicesEffectsElement, doc);
		if(!(input.getSlatsNumber() == 0)) {
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_Clmax_of_each_slat", output.getDeltaClmaxSlatList(), highLiftDevicesEffectsElement, doc);
			JPADStaticWriteUtils.writeSingleNode("delta_Clmax_total_due_to_slats", output.getDeltaClmaxSlat(), highLiftDevicesEffectsElement, doc);
		}
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_CL0_of_each_flap", output.getDeltaCL0FlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CL0_total_due_to_flaps", output.getDeltaCL0Flap(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_CLmax_of_each_flap", output.getDeltaCLmaxFlapList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CLmax_total_due_to_flaps", output.getDeltaCLmaxFlap(), highLiftDevicesEffectsElement, doc);
		if(!(input.getSlatsNumber() == 0)) {
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_CLmax_of_each_slat", output.getDeltaCLmaxSlatList(), highLiftDevicesEffectsElement, doc);
			JPADStaticWriteUtils.writeSingleNode("delta_CLmax_total_due_to_slats", output.getDeltaCLmaxSlat(), highLiftDevicesEffectsElement, doc);
		}
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_CD0_of_each_flap", output.getDeltaCDList(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CD0_total", output.getDeltaCD(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("delta_CM_c4_of_each_flap", output.getDeltaCMC4List(), highLiftDevicesEffectsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("delta_CM_c4_total", output.getDeltaCMC4(), highLiftDevicesEffectsElement, doc);
		
		org.w3c.dom.Element highLiftGlobalDataElement = doc.createElement("global_high_lift_devices_effects");
		outputRootElement.appendChild(highLiftGlobalDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_max_high_lift", output.getAlphaMaxFlapSlat(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star_high_lift", output.getAlphaStarFlapSlat(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cLmax_high_lift", output.getcLmaxFlapSlat(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_star_high_lift", output.getcLStarFlapSlat(), highLiftGlobalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha_high_lift", output.getcLalphaNew(), highLiftGlobalDataElement, doc);
		
		org.w3c.dom.Element highLiftCurveDataElement = doc.createElement("high_lift_curve_point");
		outputRootElement.appendChild(highLiftCurveDataElement);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array_clean", output.getAlphaListPlot().get(0), highLiftCurveDataElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cL_array_clean", output.getcLListPlot().get(0), highLiftCurveDataElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array_high_lift", output.getAlphaListPlot().get(1), highLiftCurveDataElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cL_array_high_lift", output.getcLListPlot().get(1), highLiftCurveDataElement, doc);
		
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
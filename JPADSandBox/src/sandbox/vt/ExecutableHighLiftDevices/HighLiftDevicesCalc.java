package sandbox.vt.ExecutableHighLiftDevices;

import java.util.ArrayList;
import java.util.List;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.ParserConfigurationException;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.NodeList;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

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

		System.out.println("\tAlpha current = " + input.getAlphaCurrent() + "\n");

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
		List<String> clAlphaMeanAirfoilProperty = reader.getXMLPropertiesByPath("//clalpha_mean_airfoil");
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
		System.out.println("\tSpan = " + input.getSpan());
		System.out.println("\tSurface = " + input.getSurface());
		System.out.println("\tSweep quarter chord equivalent wing = " + input.getSweepQuarteChordEq());
		System.out.println("\tTaper ratio equivalent wing = " + input.getTaperRatioEq());
		System.out.println("\n\tAlpha stall clean = " + input.getAlphaMaxClean());
		System.out.println("\tAlpha star clean = " + input.getAlphaStarClean());
		System.out.println("\tCL0 clean = " + input.getcL0Clean());
		System.out.println("\tCLalpha clean = " + input.getcLAlphaClean());
		System.out.println("\tCLmax clean = " + input.getcLmaxClean());
		System.out.println("\tCLstar clean = " + input.getcLstarClean());
		System.out.println("\n\tClalpha mean airfoil = " + input.getClAlphaMeanAirfoil());
		System.out.println("\tLeading edge radius mean airfoil = " + input.getLERadiusMeanAirfoil());
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
		System.out.println("\tFlaps deflections = " +  input.getDeltaFlap());
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
		
		System.out.println("\tSlats deflections = " +  input.getDeltaSlat());
		System.out.println("\tSlats chord ratios = " +  input.getCsc());
		System.out.println("\tSlats extension ratios = " +  input.getcExtCSlat());
		System.out.println("\tSlats inboard stations = " +  input.getEtaInSlat());
		System.out.println("\tSlats outboard stations = " +  input.getEtaOutSlat() + "\n");
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
		
		List<String> etaOutFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_outern_station");
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
		
		List<String> eta_out_slat_property = reader.getXMLPropertiesByPath("//slat_non_dimensional_outern_station");
		for(int i=0; i<eta_out_slat_property.size(); i++)
			input.getEtaOutSlat().add(Double.valueOf(eta_out_slat_property.get(i)));

	}
	
	public static void executeStandAloneHighLiftDevicesCalc (
			InputTree input,
			String databaseFolderPath,
			String highLiftDatabaseFileName,
			String aerodynamicDatabaseFileName
			) {
		
		//------------------------------------------------------------------------------------
		// create an OutputTree object
		output = new OutputTree();
		
		//------------------------------------------------------------------------------------
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader = new AerodynamicDatabaseReader(databaseFolderPath,aerodynamicDatabaseFileName);
		HighLiftDatabaseReader highLiftDatabaseReader = new HighLiftDatabaseReader(databaseFolderPath, highLiftDatabaseFileName);

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
		System.out.println("\nCalculating high lift devices effects...\n");
		
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
						-(10.638*input.getCfc().get(i))
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

		for(int i=0; i<input.getFlapsNumber(); i++)
			output.getDeltaClmaxFlapList().add(k1.get(i).doubleValue()
					*k2.get(i).doubleValue()
					*k3.get(i).doubleValue()
					*deltaClmaxBase.get(i).doubleValue()
					);
		
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
								input.getLERadiusMeanAirfoil().divide(input.getMeanAirfoilChord()).getEstimatedValue(),
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
							*(2-((1-input.getTaperRatioEq())*(input.getEtaInFlap().get(i)-input.getEtaOutFlap().get(i))))
							*(input.getEtaInFlap().get(i)-input.getEtaOutFlap().get(i))
							)
					);
		
		List<Double> kLambdaFlap = new ArrayList<Double>();
		for(int i=0; i<input.getFlapsNumber(); i++)
			kLambdaFlap.add(
					Math.pow(Math.cos(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()),0.75)
					*(1-(0.08*Math.pow(Math.cos(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()), 2)))
					);

		for(int i=0; i<flapTypeIndex.size(); i++)
			output.getDeltaCLmaxFlapList().add(output.getDeltaClmaxFlapList().get(i)
					*(flapSurface.get(i)/input.getSurface().getEstimatedValue())
					*kLambdaFlap.get(i)
					);

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
								*(2-(1-input.getTaperRatioEq())*(input.getEtaInSlat().get(i)-input.getEtaOutSlat().get(i)))
								*(input.getEtaInSlat().get(i)-input.getEtaOutSlat().get(i))
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
			output.getcLalphaNewList().add(
					input.getcLAlphaClean().getEstimatedValue()
					*(1+((output.getDeltaCL0FlapList().get(i)/output.getDeltaCl0FlapList().get(i))
							*(cFirstCFlap.get(i)*(1-((input.getCfc().get(i))*(1/cFirstCFlap.get(i))
									*Math.pow(Math.sin(input.getDeltaFlap().get(i).to(SI.RADIAN).getEstimatedValue()), 2)))-1))));
			swf.add(flapSurface.get(i)/input.getSurface().getEstimatedValue());
		}

		double swfTot = 0;
		for(int i=0; i<swf.size(); i++)
			swfTot += swf.get(i);

		double cLalphaNewTemp = 0.0;
		for(int i=0; i<input.getFlapsNumber(); i++)
			cLalphaNewTemp += output.getcLalphaNewList().get(i)*swf.get(i);

		cLalphaNewTemp /= swfTot;
		
		output.setcLalphaNew(cLalphaNewTemp);

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
//		List<Double> mu1 = new ArrayList<Double>();
//		for (int i=0; i<input.getFlapsNumber(); i++)
//			if(flapTypeIndex.get(i) == 3.0)
//				mu1.add(
//						highLiftDatabaseReader
//						.getMu1VsCfCFirstPlain(
//								(input.getCfc().get(i))*(1/cFirstCFlap.get(i)),
//								input.getDeltaFlap().get(i).getEstimatedValue()
//								)
//						);
//			else
//				mu1.add(highLiftDatabaseReader
//						.getMu1VsCfCFirstSlottedFowler((input.getCfc().get(i))*(1/cFirstCFlap.get(i)))
//						);
//
//		List<Double> mu2 = new ArrayList<Double>();
//		for(int i=0; i<input.getFlapsNumber(); i++)
//			mu2.add(highLiftDatabaseReader
//					.getMu2VsBfB(
//							input.getEtaInFlap().get(i),
//							input.getEtaOutFlap().get(i),
//							input.getTaperRatioEq()
//							)
//					);
//
//		List<Double> mu3 = new ArrayList<Double>();
//		for(int i=0; i<input.getFlapsNumber(); i++)
//			mu3.add(highLiftDatabaseReader
//					.getMu3VsBfB(
//							input.getEtaInFlap().get(i),
//							input.getEtaOutFlap().get(i),
//							input.getTaperRatioEq()
//							)
//					);
//
//		// FIXME: method calcCLatAlphaHighLiftDevice 
//		
//		double cL = calcCLatAlphaHighLiftDevice(input.getAlphaCurrent().getEstimatedValue());
//		for(int i=0; i<input.getFlapsNumber(); i++)
//			output.getDeltaCMC4List().add(
//					(mu2.get(i)*(-(mu1.get(i)*output.getDeltaClmaxFlapList().get(i)
//							*cFirstCFlap.get(i))-(cFirstCFlap.get(i)
//									*((cFirstCFlap.get(i))-1)
//									*(cL + (output.getDeltaClmaxFlapList().get(i)
//											*(1-(flapSurface.get(i)/input.getSurface().getEstimatedValue()))))
//									*(1/8)))) + (0.7*(input.getAspectRatio()/(1+(input.getAspectRatio()/2)))
//											*mu3.get(i)*output.getDeltaClmaxFlapList().get(i)
//											*Math.tan(input.getSweepQuarteChordEq().to(SI.RADIAN).getEstimatedValue()))
//					);
//
//		double deltaCMC4Temp = 0.0;
//		for(int i=0; i<flapTypeIndex.size(); i++)
//			deltaCMC4Temp += output.getDeltaCMC4List().get(i);
//		
//		output.setDeltaCMC4(deltaCMC4Temp);
		
		//---------------------------------------------------------------
		// PRINT HIGH LIFT DEVICES EFFECTS:
		//---------------------------------------------------------------
		System.out.println("\ndeltaCl0_flap_list = ");
		for(int i=0; i<output.getDeltaCl0FlapList().size(); i++)
			System.out.print(output.getDeltaCl0FlapList().get(i) + " ");

		System.out.println("\n\ndeltaCl0_flap = \n" + output.getDeltaCL0Flap());

		System.out.println("\ndeltaCL0_flap_list = ");
		for(int i=0; i<output.getDeltaCL0FlapList().size(); i++)
			System.out.print(output.getDeltaCL0FlapList().get(i) + " ");

		System.out.println("\n\ndeltaCL0_flap = \n" + output.getDeltaCL0Flap());

		System.out.println("\ndeltaClmax_flap_list = ");
		for(int i=0; i<output.getDeltaClmaxFlapList().size(); i++)
			System.out.print(output.getDeltaClmaxFlapList().get(i) + " ");

		System.out.println("\n\ndeltaClmax_flap = \n" + output.getDeltaClmaxFlap());

		System.out.println("\ndeltaCLmax_flap_list = ");
		for(int i=0; i<output.getDeltaCLmaxFlapList().size(); i++)
			System.out.print(output.getDeltaCLmaxFlapList().get(i) + " ");

		System.out.println("\n\ndeltaCLmax_flap = \n" + output.getDeltaCLmaxFlap());
		
		System.out.println("\ndeltaClmax_slat_list = ");
		for(int i=0; i<output.getDeltaClmaxSlatList().size(); i++)
			System.out.print(output.getDeltaClmaxSlatList().get(i) + " ");

		System.out.println("\n\ndeltaClmax_slat = \n" + output.getDeltaClmaxSlat());

		System.out.println("\ndeltaCLmax_slat_list = ");
		for(int i=0; i<output.getDeltaCLmaxSlatList().size(); i++)
			System.out.print(output.getDeltaCLmaxSlatList().get(i) + " ");

		System.out.println("\n\ndeltaCLmax_slat = \n" + output.getDeltaCLmaxSlat());

		System.out.println("\ncLalpha_new_list = ");
		for(int i=0; i<output.getcLalphaNewList().size(); i++)
			System.out.print(output.getcLalphaNewList().get(i) + " ");

		System.out.println("\n\ncLalpha_new = \n" + output.getcLalphaNew());

		System.out.println("\ndeltaCD_list = ");
		for(int i=0; i<output.getDeltaCDList().size(); i++)
			System.out.print(output.getDeltaCDList().get(i) + " ");

		System.out.println("\n\ndeltaCD = \n" + output.getDeltaCD());

//		System.out.println("\ndeltaCMc_4_list = ");
//		for(int i=0; i<output.getDeltaCMC4List().size(); i++)
//			System.out.print(output.getDeltaCMC4List().get(i) + " ");
//
//		System.out.println("\n\ndeltaCMc_4 = \n" + output.getDeltaCMC4());
		
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
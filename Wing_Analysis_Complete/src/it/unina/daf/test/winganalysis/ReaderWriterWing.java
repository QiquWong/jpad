package it.unina.daf.test.winganalysis;

import java.util.Arrays;
import java.util.List;

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

import aircraft.components.liftingSurface.creator.SlatCreator;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator;
import aircraft.components.liftingSurface.creator.SlatCreator.SlatBuilder;
import aircraft.components.liftingSurface.creator.SymmetricFlapCreator.SymmetricFlapBuilder;
import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.AirfoilTypeEnum;
import configuration.enumerations.FlapTypeEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.HighLiftDatabaseReader;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyMathUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class ReaderWriterWing {
	
	static InputOutputTree input = new InputOutputTree();

	public void importFromXML(String pathToXML, String databaseFolderPath, String aerodynamicDatabaseFileName, String highLiftDatabaseFileName) throws ParserConfigurationException {


		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");
		
		//------------------------------------------------------------------------------------
		// Setup database(s)
		AerodynamicDatabaseReader aeroDatabaseReader = database.DatabaseManager.initializeAeroDatabase(new AerodynamicDatabaseReader(
				databaseFolderPath,	aerodynamicDatabaseFileName),
				databaseFolderPath);
		
		HighLiftDatabaseReader highLiftDatabaseReader = database.DatabaseManager.initializeHighLiftDatabase(new HighLiftDatabaseReader(
						databaseFolderPath, highLiftDatabaseFileName),
				databaseFolderPath);

		input.setTheAerodatabaseReader(aeroDatabaseReader);
		input.setTheHighLiftDatabaseReader(highLiftDatabaseReader);

		
		String plotString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@plot");
		
		if(plotString.equalsIgnoreCase("true"))
			input.setPlotFlag(Boolean.TRUE);
		
		if(plotString.equalsIgnoreCase("false"))
			input.setPlotFlag(Boolean.FALSE);
		
		String sysoutString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@verbosity");
		
		if(sysoutString.equalsIgnoreCase("true"))
			input.setVerbosityFlag(Boolean.TRUE);
		
		if(sysoutString.equalsIgnoreCase("false"))
			input.setVerbosityFlag(Boolean.FALSE);
		
		String highLiftString = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@highLift");
		
		if(highLiftString.equalsIgnoreCase("true"))
			input.setHighLiftFlag(Boolean.TRUE);
		
		if(highLiftString.equalsIgnoreCase("false"))
			input.setHighLiftFlag(Boolean.FALSE);
		
		
		//---------------------------------------------------------------------------------
		// OPERATING CONDITION:
				
		Amount<Length> altitude = reader.getXMLAmountWithUnitByPath("//altitude").to(SI.METER);
		input.setAltitude(altitude);
	
		double machNumber =  Double.parseDouble(reader.getXMLPropertiesByPath("//mach_number").get(0));
		input.setMachNumber(machNumber);

		int numberOfAlpha =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_alpha").get(0));
		input.setNumberOfAlpha(numberOfAlpha);
		
		Amount<Angle> alphaInitial = reader.getXMLAmountWithUnitByPath("//alpha_initial").to(NonSI.DEGREE_ANGLE);
		input.setAlphaInitial(alphaInitial);
		
		Amount<Angle> alphaFinal = reader.getXMLAmountWithUnitByPath("//alpha_final").to(NonSI.DEGREE_ANGLE);
		input.setAlphaFinal(alphaFinal);
		
		
		//----------------------------------------------------------------------------------
		// GLOBAL
		
		Amount<Area> surface = reader.getXMLAmountWithUnitByPath("//surface").to(SI.SQUARE_METRE);
		input.setSurface(surface);
		
		double aspectRatio = Double.parseDouble(reader.getXMLPropertiesByPath("//aspect_ratio").get(0));
		input.setAspectRatio(aspectRatio);
		
		int numberOfPointSemispan =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_point_semispan").get(0));
		input.setNumberOfPointSemispan(numberOfPointSemispan);
		
		
		double momentumPole = Double.parseDouble(reader.getXMLPropertiesByPath("//momentum_pole").get(0));
		input.setMomentumPole(momentumPole);
		
		
		//-------------------------------------------------------------------------------------
		//DISTRIBUTION
		
		int numberOfSection =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_given_sections").get(0));
		input.setNumberOfSections(numberOfSection);
		
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
		
		List<String> airfoilTypeProperty = reader.getXMLPropertiesByPath("//airfoil_type");
		if(airfoilTypeProperty.get(0).equals("CONVENTIONAL"))
			input.setMeanAirfoilType(AirfoilTypeEnum.CONVENTIONAL);
		else if(airfoilTypeProperty.get(0).equals("PEAKY"))
			input.setMeanAirfoilType(AirfoilTypeEnum.PEAKY);
		else if(airfoilTypeProperty.get(0).equals("SUPERCRITICAL"))
			input.setMeanAirfoilType(AirfoilTypeEnum.SUPERCRITICAL);
		else if(airfoilTypeProperty.get(0).equals("LAMINAR"))
			input.setMeanAirfoilType(AirfoilTypeEnum.LAMINAR);
		else if(airfoilTypeProperty.get(0).equals("MODERN_SUPERCRITICAL"))
			input.setMeanAirfoilType(AirfoilTypeEnum.MODERN_SUPERCRITICAL);
		else {
			System.err.println("NO VALID FAMILY TYPE!!");
			return;
			
		}
		
//		//recognizing airfoil family
//				int airfoilFamilyIndex = 0;
//				if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit) 
//					airfoilFamilyIndex = 1;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_5_Digit)
//					airfoilFamilyIndex = 2;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_63_Series)
//					airfoilFamilyIndex = 3;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_64_Series)
//					airfoilFamilyIndex = 4;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_65_Series)
//					airfoilFamilyIndex = 5;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_66_Series)
//					airfoilFamilyIndex = 6;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.BICONVEX)
//					airfoilFamilyIndex = 7;
//				else if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.DOUBLE_WEDGE)
//					airfoilFamilyIndex = 8;
				
				
		double sharpnessParameterLE = aeroDatabaseReader.getDeltaYvsThickness(input.getMeanThickness(), input.getMeanAirfoilFamily());
				
				
		
		List<String> chordDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//chord_distribution").get(0));
		for(int i=0; i<chordDistribution.size(); i++)
			input.getChordDistributionInput().add(Amount.valueOf(Double.valueOf(chordDistribution.get(i)), SI.METER));
		
		List<String> yAdimensionalStationIput = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//y_adimensional_stations").get(0));
		for(int i=0; i<yAdimensionalStationIput.size(); i++)
			input.getyAdimensionalStationInput().add(Double.valueOf(yAdimensionalStationIput.get(i)));
	
		List<String> xleDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//x_le_distribution").get(0));
		for(int i=0; i<xleDistribution.size(); i++)
			input.getxLEDistributionInput().add(Amount.valueOf(Double.valueOf(xleDistribution.get(i)), SI.METER));
		
		List<String> twistDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//twist_distribution").get(0));
		for(int i=0; i<twistDistribution.size(); i++)
			input.getTwistDistributionInput().add(Amount.valueOf(Double.valueOf(twistDistribution.get(i)), NonSI.DEGREE_ANGLE));
						
		List<String> alphaZeroLiftDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution").get(0));
		for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
			input.getAlphaZeroLiftDistributionInput().add(Amount.valueOf(Double.valueOf(alphaZeroLiftDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStarDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_star_distribution").get(0));
		for(int i=0; i<alphaStarDistribution.size(); i++)
			input.getAlphaStarDistributionInput().add(Amount.valueOf(Double.valueOf(alphaStarDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStallDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_stall_distribution").get(0));
		for(int i=0; i<alphaStallDistribution.size(); i++)
			input.getAlphaStallDistributionInput().add(Amount.valueOf(Double.valueOf(alphaStallDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> clMaxDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution").get(0));
		for(int i=0; i<clMaxDistribution.size(); i++)
			input.getMaximumliftCoefficientDistributionInput().add(Double.valueOf(clMaxDistribution.get(i)));
		
		List<String> clZeroDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//cl_zero_distribution").get(0));
		for(int i=0; i<clZeroDistribution.size(); i++)
			input.getClZeroDistributionInput().add(Double.valueOf(clZeroDistribution.get(i)));
		
		List<String> clAlphaDEGDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//cl_alpha_distribution").get(0));
		for(int i=0; i<clAlphaDEGDistribution.size(); i++)
			input.getClalphaDEGDistributionInput().add(Double.valueOf(clAlphaDEGDistribution.get(i)));
		
		List<String> cdMinDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//cd_min_distribution").get(0));
		for(int i=0; i<cdMinDistribution.size(); i++)
			input.getCdMinDistributionInput().add(Double.valueOf(cdMinDistribution.get(i)));
	
		List<String> clIdealDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//cl_ideal_distribution").get(0));
		for(int i=0; i<clIdealDistribution.size(); i++)
			input.getClIdealDistributionInput().add(Double.valueOf(clIdealDistribution.get(i)));

		List<String> kDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//k_distribution").get(0));
		for(int i=0; i<kDistribution.size(); i++)
			input.getkDistributionInput().add(Double.valueOf(kDistribution.get(i)));
		
		List<String> cmc4Distribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//cm_c4_distribution").get(0));
		for(int i=0; i<cmc4Distribution.size(); i++)
			input.getCmc4DistributionInput().add(Double.valueOf(cmc4Distribution.get(i)));
		
		List<String> thicknessMeanAirfoil = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//max_thickness_distribution").get(0));
		for(int i=0; i<thicknessMeanAirfoil.size(); i++)
			input.getMaxThicknessAirfoilsDistribution().add(Double.valueOf(thicknessMeanAirfoil.get(i)));
		
		List<String> leRadiusAirfoil = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//leading_edge_radius_distribution").get(0));
		for(int i=0; i<leRadiusAirfoil.size(); i++)
			input.getLeRadiusDistribution().add(Amount.valueOf(Double.valueOf(leRadiusAirfoil.get(i)), SI.METER));


	// WARNINGS
		
		if ( input.getNumberOfSections() != input.getChordDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of chords. ( number of section = " + input.getNumberOfSections()
			 + " ; number of chords = " + input.getChordDistributionInput().size() + " )");
		}
		
		if ( input.getNumberOfSections() != input.getxLEDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of XLE values. ( number of section = " + input.getNumberOfSections()
			 + " ; number of XLE values = " + input.getxLEDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getTwistDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of twist angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of twist angles = " + input.getTwistDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getAlphaZeroLiftDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of zero lift angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of zero lift angles = " + input.getAlphaZeroLiftDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getAlphaStarDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of end of linearity angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of end of linearity angles = " + input.getAlphaStarDistributionInput().size()+ " )");
		}
		
		
		if ( input.getNumberOfSections() != input.getAlphaStarDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of end of linearity angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of end of stall angles = " + input.getAlphaStallDistributionInput().size()+ " )");
		}
		
		
		if ( input.getNumberOfSections() != input.getMaximumliftCoefficientDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cl max. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cl max = " + input.getMaximumliftCoefficientDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getClalphaDEGDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cl alpha. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cl alpha = " + input.getClalphaDEGDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getClZeroDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cl zero. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cl zero = " + input.getMaximumliftCoefficientDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getCdMinDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cd min. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cd min = " + input.getCdMinDistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getClIdealDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cl ideal. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cl ideal = " + input.getClIdealDistributionInput().size()+ " )");
		}

		if ( input.getNumberOfSections() != input.getkDistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of k. ( number of section = " + input.getNumberOfSections()
			 + " ; number of k = " + input.getkDistributionInput().size()+ " )");
		}


		if ( input.getNumberOfSections() != input.getCmc4DistributionInput().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cmc4 distributions. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cm c4 = " + input.getCmc4DistributionInput().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getLeRadiusDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of le radius distributions. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cm t/c = " + input.getLeRadiusDistribution().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getLeRadiusDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of le max thickness distributions. ( number of section = " + input.getNumberOfSections()
			 + " ; number of le radius = " + input.getLeRadiusDistribution().size()+ " )");
		}
		
		//HIGH LIFT
		
		NodeList nodelistFlaps = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//flaps/flap");
		
		input.setFlapsNumber(nodelistFlaps.getLength());
		System.out.println("Flaps found: " + input.getFlapsNumber());
		
		List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//flap_type");
		
		// Recognizing flap type 
		flapTypeProperty.stream().forEach(
				x -> input.getFlapType().add( 
						Arrays.stream(FlapTypeEnum.values())
						.filter(a -> a.toString().equals(x))
						.findFirst()
						.orElseThrow(() -> {throw new IllegalStateException(String.format("Unsupported flap type", flapTypeProperty));}))
				);
				
		List<String> cfcPropertyInner = reader.getXMLPropertiesByPath("//flap_chord_inner_ratio");
		for(int i=0; i<cfcPropertyInner.size(); i++)
			input.getCfcInner().add(Double.valueOf(cfcPropertyInner.get(i)));
		
		List<String> cfcPropertyOuter = reader.getXMLPropertiesByPath("//flap_chord_outer_ratio");
		for(int i=0; i<cfcPropertyOuter.size(); i++)
			input.getCfcOuter().add(Double.valueOf(cfcPropertyOuter.get(i)));
		
		
		List<String> deltaFlapProperty = reader.getXMLPropertiesByPath("//flap_deflection");
		for(int i=0; i<deltaFlapProperty.size(); i++)
			input.getDeltaFlap().add(Amount.valueOf(Double.valueOf(deltaFlapProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> etaInFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_inner_station");
		for(int i=0; i<etaInFlapProperty.size(); i++)
			input.getEtaInFlap().add(Double.valueOf(etaInFlapProperty.get(i)));
		
		List<String> etaOutFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_outer_station");
		for(int i=0; i<etaOutFlapProperty.size(); i++)
			input.getEtaOutFlap().add(Double.valueOf(etaOutFlapProperty.get(i)));
		
		//slat
		NodeList nodelistSlats = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//slats/slat");

		input.setSlatsNumber(nodelistSlats.getLength());
		System.out.println("Slats found: " + input.getSlatsNumber());
		
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
		
		for(int i=0; i<input.getFlapsNumber(); i++) {
			String id = reader
					.getXMLPropertyByPath(
							"//flap/@id");
			
			SymmetricFlapCreator symmetricFlap =
					new SymmetricFlapBuilder(
							id,
							input.getFlapType().get(i),
							input.getEtaInFlap().get(i),
							input.getEtaOutFlap().get(i),
							input.getCfcInner().get(i),
							input.getCfcOuter().get(i),
							Amount.valueOf(0.0, NonSI.DEGREE_ANGLE),
							Amount.valueOf(40.0, NonSI.DEGREE_ANGLE)
							)
					.build();
			
			input.getSymmetricFlapCreatorList().add(symmetricFlap);
		}
		
		for(int i=0; i<input.getSlatsNumber(); i++) {
			String id = reader
					.getXMLPropertyByPath(
							"//slat/@id");
			
			SlatCreator slat =
					new SlatBuilder(
							id,
							input.getEtaInSlat().get(i),
							input.getEtaOutSlat().get(i),
							input.getCsc().get(i),
							input.getCsc().get(i),
							input.getcExtCSlat().get(i),
							Amount.valueOf(0.0, NonSI.DEGREE_ANGLE),
							Amount.valueOf(40.0, NonSI.DEGREE_ANGLE)
							)
					.build();
		
			
			input.getSlatCreatorList().add(slat);
		}
	
		
	// OTHER VALUES
		double span = Math.sqrt(input.getAspectRatio() * input.getSurface().getEstimatedValue());
		input.setSpan(Amount.valueOf(span, SI.METER));
		input.setSemiSpan(Amount.valueOf(span/2, SI.METER));
		
	// delta alpha	
		
		double tgAngle =input.getxLEDistributionInput().get(input.getNumberOfSections()-1).getEstimatedValue()/input.getSemiSpan().getEstimatedValue();
		double sweepLE =Math.toDegrees(Math.atan(tgAngle));
		double deltaAlpha = aeroDatabaseReader.getClmaxCLmaxVsLambdaLEVsDeltaY(sweepLE,sharpnessParameterLE);
		
		input.setDeltaAlpha(deltaAlpha);
		
		
	// PRINT
		
		if (input.isVerbosityFlag()) {
		if(input.getNumberOfSections() == input.getChordDistributionInput().size() &&
				input.getNumberOfSections() == input.getxLEDistributionInput().size() &&
				input.getNumberOfSections() == input.getTwistDistributionInput().size() &&
				input.getNumberOfSections() == input.getAlphaZeroLiftDistributionInput().size() &&
				input.getNumberOfSections() == input.getAlphaStarDistributionInput().size() &&
				input.getNumberOfSections() == input.getAlphaStallDistributionInput().size() &&
				input.getNumberOfSections() == input.getMaximumliftCoefficientDistributionInput().size() &&
				input.getNumberOfSections() == input.getCdMinDistributionInput().size()&&
				input.getNumberOfSections() == input.getClIdealDistributionInput().size() &&
				input.getNumberOfSections() == input.getkDistributionInput().size() &&
				input.getNumberOfSections() == input.getCmc4DistributionInput().size()){
			
		System.out.println("\n\nINPUT DATA\n\n");
		
		System.out.println("Operating Conditions");
		System.out.println("-------------------------------------");
		System.out.println("Altitude : " + input.getAltitude().getEstimatedValue()+ " " + input.getAltitude().getUnit());
		System.out.println("Mach Number : " + input.getMachNumber());
		
		System.out.println("\nAlpha Values");
		System.out.println("-------------------------------------");
		System.out.println("Number of Alpha : " + input.getNumberOfAlpha());
		System.out.println("Alpha Initial : " + input.getAlphaInitial().getEstimatedValue()+ " " + input.getAlphaInitial().getUnit());
		System.out.println("Alpha Final : " + input.getAlphaFinal().getEstimatedValue()+ " " + input.getAlphaFinal().getUnit());
		
		System.out.println("\nWing");
		System.out.println("-------------------------------------");
		System.out.println("Surface : " + input.getSurface().getEstimatedValue()+ " " + input.getSurface().getUnit());
		System.out.println("Aspect Ratio : " + input.getAspectRatio());
		System.out.println("Number of point along semi-span : " + input.getNumberOfPointSemispan());
		System.out.println("Span : " + input.getSpan().getEstimatedValue()+ " " + input.getSpan().getUnit());
		System.out.println("\nDistribution");
		System.out.println("-------------------------------------");
		System.out.println("Number of given stations : " + input.getNumberOfSections());
		
		System.out.println("\nMean airoil type : " + input.getMeanAirfoilFamily());
        System.out.println("Mean airfoil thickness : " + input.getMeanThickness());
		
		System.out.print("\nChord distribution : [");
		for(int i=0; i<input.getChordDistributionInput().size(); i++)
			System.out.print("  " +input.getChordDistributionInput().get(i).getEstimatedValue() + "  ");
			System.out.println("] " + input.getChordDistributionInput().get(0).getUnit() );
		
		System.out.print("X LE distribution : [");
		for(int i=0; i<input.getxLEDistributionInput().size(); i++)
			System.out.print("  " +input.getxLEDistributionInput().get(i).getEstimatedValue()+ " ");
		    System.out.println("] " + input.getxLEDistributionInput().get(0).getUnit() );
		
		System.out.print("Twist distribution : [");
		for(int i=0; i<input.getTwistDistributionInput().size(); i++)
			System.out.print("  " +input.getTwistDistributionInput().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getTwistDistributionInput().get(0).getUnit() );
			
		System.out.print("Alpha zero lift distribution : [");
		for(int i=0; i<input.getAlphaZeroLiftDistributionInput().size(); i++)
			System.out.print("  " +input.getAlphaZeroLiftDistributionInput().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getAlphaZeroLiftDistributionInput().get(0).getUnit() );
			
		System.out.print("Alpha Star distribution: [");
		for(int i=0; i<input.getAlphaStarDistributionInput().size(); i++)
			System.out.print("  " +input.getAlphaStarDistributionInput().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getAlphaStarDistributionInput().get(0).getUnit() );	
			
		System.out.print("Alpha Stall distribution: [");
			for(int i=0; i<input.getAlphaStallDistributionInput().size(); i++)
				System.out.print("  " +input.getAlphaStallDistributionInput().get(i).getEstimatedValue()+ " ");
				System.out.println("] " + input.getAlphaStallDistributionInput().get(0).getUnit() );
			
		System.out.print("Cl max distribution : ");
			System.out.println(input.getMaximumliftCoefficientDistributionInput());
			
		System.out.print("Cl zero distribution : ");
			System.out.println(input.getClZeroDistributionInput());
			
			System.out.print("Cl alpha distribution : ");
			System.out.println(input.getClalphaDEGDistributionInput());
			
		System.out.print("Cd min distribution : ");
			System.out.println(input.getCdMinDistributionInput());
			
		System.out.print("Cl ideal distribution : ");
			System.out.println(input.getClIdealDistributionInput());
			
		System.out.print("K distribution : ");
			System.out.println(input.getkDistributionInput());
			
		System.out.print("Cm c4 distribution : ");
			System.out.println(input.getCmc4DistributionInput());
			
		System.out.print("thickness distribution : ");
			System.out.println(input.getMaxThicknessAirfoilsDistribution());
			
		System.out.print("le radius distribution : ");
			System.out.println(input.getLeRadiusDistribution());
			
		System.out.print("\nAdimensional stations :");
		 	System.out.println(input.getyAdimensionalStationInput());
		}
		}
		
		double [] yInput = new double [input.getNumberOfSections()];
		double [] alphaStar = new double [input.getNumberOfSections()];
		double [] cLMax = new double [input.getNumberOfSections()];
		
		double [] yNew  = {0, 0.12, 0.13, 0.399, 0.4, 0.75, 0.76, 1 };
		
		for (int i=0; i<input.getNumberOfSections(); i++){
			yInput[i] = input.getyAdimensionalStationInput().get(i);
			alphaStar[i] = input.getAlphaStarDistributionInput().get(i).getEstimatedValue();
			cLMax[i] = (double)input.getMaximumliftCoefficientDistributionInput().get(i);
		}
		Double[] alphastar = MyMathUtils.getInterpolatedValue1DLinear(yInput,alphaStar, yNew);
		Double[] cLMaxext = MyMathUtils.getInterpolatedValue1DLinear(yInput,cLMax, yNew);

	}
	
	
	
	public static void writeToXML(String filenameWithPathAndExt) {
		
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
	
	
	private static void defineXmlTree(Document doc, DocumentBuilder docBuilder) {
		
		org.w3c.dom.Element rootElement = doc.createElement("Wing_aerodynamic_executable");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element flightConditionsElement = doc.createElement("operating_conditions");
		inputRootElement.appendChild(flightConditionsElement);

		JPADStaticWriteUtils.writeSingleNode("altitude", input.getAltitude(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("mach_number", input.getMachNumber(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_alpha", input.getNumberOfAlpha(), flightConditionsElement, doc);
		if (input.getNumberOfAlpha()!=0){
		JPADStaticWriteUtils.writeSingleNode("alpha_initial", input.getAlphaInitial(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_final", input.getAlphaFinal(), flightConditionsElement, doc);
		}
				
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		inputRootElement.appendChild(wingDataElement);
		
		org.w3c.dom.Element geometryDataElement = doc.createElement("global");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_point_semispan", input.getNumberOfPointSemispan(), geometryDataElement, doc);
		
		
		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("distibution");
		wingDataElement.appendChild(cleanConfigurationDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("number_of_given_sections", input.getNumberOfSections(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("airfoil_family", input.getMeanAirfoilFamily(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", input.getMeanThickness(), cleanConfigurationDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("momentum_pole", input.getMomentumPole(), cleanConfigurationDataElement, doc);
		
		org.w3c.dom.Element childDistribution = doc.createElement("geometry");
		cleanConfigurationDataElement.appendChild(childDistribution);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("y_adimensional_stations", input.getyAdimensionalStationInput(),childDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("chord_distribution", input.getChordDistributionInput(), childDistribution, doc, input.getChordDistributionInput().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("x_le_distribution", input.getxLEDistributionInput(), childDistribution, doc, input.getxLEDistributionInput().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("twist_distribution", input.getTwistDistributionInput(), childDistribution, doc, input.getTwistDistributionInput().get(0).getUnit().toString());
		
		org.w3c.dom.Element childDistributionNew = doc.createElement("aerodynamics");
		cleanConfigurationDataElement.appendChild(childDistributionNew);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_zero_lift_distribution", input.getAlphaZeroLiftDistributionInput(), childDistributionNew, doc,  input.getAlphaZeroLiftDistributionInput().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_star_distribution", input.getAlphaStarDistributionInput(), childDistributionNew, doc,  input.getAlphaZeroLiftDistributionInput().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_star_distribution", input.getAlphaStallDistributionInput(), childDistributionNew, doc,  input.getAlphaStallDistributionInput().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("maximum_lift_coefficient_distribution", input.getMaximumliftCoefficientDistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_zero_distribution", input.getClZeroDistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_alpha_distribution", input.getClalphaDEGDistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cd_min_distribution", input.getCdMinDistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_ideal_distribution", input.getClIdealDistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("k_distribution", input.getkDistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cm_c4_distribution", input.getCmc4DistributionInput(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNode("max_thickness_distribution", input.getMaxThicknessAirfoilsDistribution(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNode("leading_edge_radius_distribution", input.getLeRadiusDistribution(), childDistributionNew, doc);
		
		//--------------------------------------------------------------------------------------
		// OUTPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element outputRootElement = doc.createElement("OUTPUT");
		rootElement.appendChild(outputRootElement);
		
		org.w3c.dom.Element wingAerodynamicElement = doc.createElement("wing_aerodynamic_characteristics");
		outputRootElement.appendChild(wingAerodynamicElement);
		
		org.w3c.dom.Element wingCLEANAerodynamicElement = doc.createElement("CLEAN_configuration");
		wingAerodynamicElement.appendChild(wingCLEANAerodynamicElement);
		
		org.w3c.dom.Element cleanElement = doc.createElement("lift_characteristics");
		wingCLEANAerodynamicElement.appendChild(cleanElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift", input.getAlphaZeroLift(), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha", Amount.valueOf(Double.valueOf(input.getcLAlpha()), NonSI.DEGREE_ANGLE.inverse()), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_zero", input.getcLZero(), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_star", input.getcLStar(), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star", input.getAlphaStar(), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_max", input.getcLMax(), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_max_linear", input.getAlphaMaxLinear(), cleanElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_stall", input.getAlphaStall(), cleanElement, doc);
		
		org.w3c.dom.Element globalDataElement = doc.createElement("cL_vs_alpha_curve");
		cleanElement.appendChild(globalDataElement);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cL_array",input.getcLVsAlphaVector(), globalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array", input.getAlphaVector(), globalDataElement, doc, "°");
	
		
		org.w3c.dom.Element distributionElement = doc.createElement("distribution");
		cleanElement.appendChild(distributionElement);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("eta",input.getyAdimensionalStationActual(), distributionElement, doc);

		if (input.getNumberOfAlpha()!=0){
			for (int i=0; i<input.getNumberOfAlpha(); i++){
				JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_at_alpha_deg_"
					 + input.getAlphaDistributionArray().get(i).doubleValue(NonSI.DEGREE_ANGLE),
						input.getClVsEtaVectors().get(i), distributionElement, doc);
			}
		}

		org.w3c.dom.Element stallPathElement = doc.createElement("stall_path");
		cleanElement.appendChild(stallPathElement);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("eta",input.getyAdimensionalStationActual(), stallPathElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_max_airfoil",input.getMaximumliftCoefficientDistributionActual(), stallPathElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_distribution_at_stall",input.getClDistributionAtStall(), stallPathElement, doc);
		
		//DRAG
		org.w3c.dom.Element cleanDragElement = doc.createElement("drag_characteristics");
		wingCLEANAerodynamicElement.appendChild(cleanDragElement);
		
		org.w3c.dom.Element dragDistribution = doc.createElement("drag_distribution");
		cleanDragElement.appendChild(dragDistribution);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("eta",input.getyAdimensionalStationActual(), dragDistribution, doc);

		if (input.getNumberOfAlpha()!=0){
			for (int i=0; i<input.getNumberOfAlpha(); i++){
				JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cd_parasite_at_alpha_" 
			+ input.getAlphaDistributionArray().get(i).doubleValue(NonSI.DEGREE_ANGLE)
			,input.getParasiteDragDistribution().get(i), dragDistribution, doc);
			}
			
		org.w3c.dom.Element dragBreakdownDistribution = doc.createElement("drag_breakdown");
		cleanDragElement.appendChild(dragBreakdownDistribution);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl_clean",input.getcLVsAlphaVector(), dragBreakdownDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cd_parasite",input.getParasitePolar(), dragBreakdownDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cd_induced",input.getInducedPolar(), dragBreakdownDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cd_wave",input.getWawePolar(), dragBreakdownDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("total_polar_curve",input.getPolarClean(), dragBreakdownDistribution, doc);
		
		
		//MOMENT
		org.w3c.dom.Element cleanMomentElement = doc.createElement("moment_characteristics");
		wingCLEANAerodynamicElement.appendChild(cleanMomentElement);
		org.w3c.dom.Element cleanMomentDistributionElement = doc.createElement("moment_distribution");
		cleanMomentElement.appendChild(cleanMomentDistributionElement);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("eta",input.getyAdimensionalStationActual(), dragDistribution, doc);
		
		if (input.getNumberOfAlpha()!=0){
			for (int i=0; i<input.getNumberOfAlpha(); i++){
				JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cm_distribution_at_alpha_" 
			+ input.getAlphaDistributionArray().get(i).doubleValue(NonSI.DEGREE_ANGLE)
			,input.getCmVsEtaVectors().get(i), cleanMomentDistributionElement, doc);
			}
		}
		
		org.w3c.dom.Element cleanMomentCurveElement = doc.createElement("moment_curve");
		cleanMomentElement.appendChild(cleanMomentCurveElement);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array", input.getAlphaVector(), cleanMomentCurveElement, doc, "°");
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("moment_curve", input.getMomentCurveClean(), cleanMomentCurveElement, doc);
		
		}
		org.w3c.dom.Element wingHLAerodynamicElement = doc.createElement("HIGHLIFT_configuration");
		wingAerodynamicElement.appendChild(wingHLAerodynamicElement);

		org.w3c.dom.Element liftElement = doc.createElement("lift_characteristics");
		wingHLAerodynamicElement.appendChild(liftElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift", input.getAlphaZeroLiftHL(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha", input.get_cLAlphaHighLift().getEstimatedValue(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_zero", input.getcLZeroHL(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_star", input.getcLStarHL(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_star", input.getAlphaStarHL(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_max", input.getcLMaxHL(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_stall", input.getAlphaStallHL(), liftElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array", input.getAlphaVectorHL(), liftElement, doc, "°");
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("CL_curve_High_Lift", input.getClVsAlphaHighLift(), liftElement, doc);
		
		
		org.w3c.dom.Element dragElement = doc.createElement("drag_characteristics");
		wingHLAerodynamicElement.appendChild(dragElement);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("CD_curve_High_Lift", input.getPolarHighLift(), dragElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("CL_curve_High_Lift", input.getClVsAlphaHighLift(), dragElement, doc);
		
	
		org.w3c.dom.Element momentElement = doc.createElement("moment_characteristics");
		wingHLAerodynamicElement.appendChild(momentElement);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_array", input.getAlphaVectorHL(), momentElement, doc, "°");
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("CM_curve_High_Lift", input.getMomentCurveHighLift(), momentElement, doc);
		
	}

	public InputOutputTree getInput() {
		return input;
	}

	public void setInput(InputOutputTree input) {
		this.input = input;
	}
}

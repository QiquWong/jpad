package sandbox.mr.ExecutableWing;

import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.NodeList;

import configuration.enumerations.AirfoilFamilyEnum;
import sandbox.vt.ExecutableHighLiftDevices.InputTree;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class ReaderWriterWing {
	
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		InputOutputTree input = new InputOutputTree();

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// OPERATING CONDITION:
//		List<String> altitude = reader.getXMLPropertiesByPath("//altitude");
//		input.setAltitude(Amount.valueOf(Double.valueOf(altitude.get(0)), SI.METER));	
		
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
		
		double adimensionalKinkStation = Double.parseDouble(reader.getXMLPropertiesByPath("//adimensional_kink_station ").get(0));
		input.setAdimensionalKinkStation(adimensionalKinkStation);
		
		
		//-------------------------------------------------------------------------------------
		//DISTRIBUTION
		
		int numberOfSection =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_given_sections").get(0));
		input.setNumberOfSections(numberOfSection);
		
		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//mean_airfoil_type");
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
		
		List<String> chordDistribution = reader.getXMLPropertiesByPath("//chord_distribution");
		for(int i=0; i<chordDistribution.size(); i++)
			input.getChordDistribution().add(Amount.valueOf(Double.valueOf(chordDistribution.get(i)), SI.METER));
		
		List<String> xleDistribution = reader.getXMLPropertiesByPath("//x_le_distribution");
		for(int i=0; i<xleDistribution.size(); i++)
			input.getxLEDistribution().add(Amount.valueOf(Double.valueOf(xleDistribution.get(i)), SI.METER));
		
		List<String> twistDistribution = reader.getXMLPropertiesByPath("//twist_distribution");
		for(int i=0; i<twistDistribution.size(); i++)
			input.getxLEDistribution().add(Amount.valueOf(Double.valueOf(twistDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> dihedralDistribution  = reader.getXMLPropertiesByPath("//dihedral_distribution");
		for(int i=0; i<dihedralDistribution.size(); i++)
			input.getDihedralDistribution().add(Amount.valueOf(Double.valueOf(dihedralDistribution.get(i)), SI.METER));
		
		
		
		List<String> alphaZeroLiftDistribution = reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution");
		for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
			input.getAlphaZeroLiftDistribution().add(Amount.valueOf(Double.valueOf(alphaZeroLiftDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStarDistribution = reader.getXMLPropertiesByPath("//alpha_star_distribution");
		for(int i=0; i<alphaStarDistribution.size(); i++)
			input.getAlphaStarDistribution().add(Amount.valueOf(Double.valueOf(alphaStarDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> clMaxDistribution = reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution");
		for(int i=0; i<clMaxDistribution.size(); i++)
			input.getMaximumliftCoefficientDistribution().add(Double.valueOf(clMaxDistribution.get(i)));
	
		

	// PRINT
		
		System.out.println("INPUT DATA\n\n");
		
		System.out.println("Operating Conditions");
		System.out.println("-------------------------------------");
		System.out.println("Altitude: " + input.getAltitude());
		
		System.out.println("\nChord distribution: ");
		for(int i=0; i<input.getChordDistribution().size(); i++)
			System.out.print(input.getChordDistribution().get(i) + " ");

	}
}

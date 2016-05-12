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
		
		List<String> chordDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//chord_distribution").get(0));
		for(int i=0; i<chordDistribution.size(); i++)
			input.getChordDistribution().add(Amount.valueOf(Double.valueOf(chordDistribution.get(i)), SI.METER));
		
		List<String> yAdimensionalStationIput = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//y_adimensional_stations").get(0));
		for(int i=0; i<yAdimensionalStationIput.size(); i++)
			input.getyAdimensionalStationInput().add(Double.valueOf(yAdimensionalStationIput.get(i)));
	
		
		List<String> xleDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//x_le_distribution").get(0));
		for(int i=0; i<xleDistribution.size(); i++)
			input.getxLEDistribution().add(Amount.valueOf(Double.valueOf(xleDistribution.get(i)), SI.METER));
		
		List<String> twistDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//twist_distribution").get(0));
		for(int i=0; i<twistDistribution.size(); i++)
			input.getTwistDistribution().add(Amount.valueOf(Double.valueOf(twistDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> dihedralDistribution  = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//dihedral_distribution").get(0));
		for(int i=0; i<dihedralDistribution.size(); i++)
			input.getDihedralDistribution().add(Amount.valueOf(Double.valueOf(dihedralDistribution.get(i)), NonSI.DEGREE_ANGLE));
				
		List<String> alphaZeroLiftDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution").get(0));
		for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
			input.getAlphaZeroLiftDistribution().add(Amount.valueOf(Double.valueOf(alphaZeroLiftDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStarDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_star_distribution").get(0));
		for(int i=0; i<alphaStarDistribution.size(); i++)
			input.getAlphaStarDistribution().add(Amount.valueOf(Double.valueOf(alphaStarDistribution.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> clMaxDistribution = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution").get(0));
		for(int i=0; i<clMaxDistribution.size(); i++)
			input.getMaximumliftCoefficientDistribution().add(Double.valueOf(clMaxDistribution.get(i)));
	
		

	// WARNINGS
		
		if ( input.getNumberOfSections() != input.getChordDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of chords. ( number of section = " + input.getNumberOfSections()
			 + " ; number of chords = " + input.getChordDistribution().size() + " )");
		}
		
		if ( input.getNumberOfSections() != input.getxLEDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of XLE values. ( number of section = " + input.getNumberOfSections()
			 + " ; number of XLE values = " + input.getxLEDistribution().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getTwistDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of twist angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of twist angles = " + input.getTwistDistribution().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getDihedralDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of dihedral angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of dihedral angles = " + input.getDihedralDistribution().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getAlphaZeroLiftDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of zero lift angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of zero lift angles = " + input.getAlphaZeroLiftDistribution().size()+ " )");
		}
		
		if ( input.getNumberOfSections() != input.getAlphaStarDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of end of linearity angles. ( number of section = " + input.getNumberOfSections()
			 + " ; number of end of linearity angles = " + input.getAlphaStarDistribution().size()+ " )");
		}
		
		
		if ( input.getNumberOfSections() != input.getMaximumliftCoefficientDistribution().size()){
			 System.err.println("WARNING! the number of declared section differs from the number of cl max. ( number of section = " + input.getNumberOfSections()
			 + " ; number of cl max = " + input.getMaximumliftCoefficientDistribution().size()+ " )");
		}
	// PRINT
		
		if(input.getNumberOfSections() == input.getChordDistribution().size() &&
				input.getNumberOfSections() == input.getxLEDistribution().size() &&
				input.getNumberOfSections() == input.getTwistDistribution().size() &&
				input.getNumberOfSections() == input.getDihedralDistribution().size() &&
				input.getNumberOfSections() == input.getAlphaZeroLiftDistribution().size() &&
				input.getNumberOfSections() == input.getAlphaStarDistribution().size() &&
				input.getNumberOfSections() == input.getMaximumliftCoefficientDistribution().size() ){
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
		System.out.println("Adimensional kink station : " + input.getAdimensionalKinkStation());
		
		System.out.println("\nDistribution");
		System.out.println("-------------------------------------");
		System.out.println("Number of given stations : " + input.getNumberOfSections());
		
		System.out.println("\nMean airoil type : " + input.getMeanAirfoilFamily());

		System.out.print("\nChord distribution : [");
		for(int i=0; i<input.getChordDistribution().size(); i++)
			System.out.print("  " +input.getChordDistribution().get(i).getEstimatedValue() + "  ");
			System.out.println("] " + input.getChordDistribution().get(0).getUnit() );
		
		System.out.print("X LE distribution : [");
		for(int i=0; i<input.getxLEDistribution().size(); i++)
			System.out.print("  " +input.getxLEDistribution().get(i).getEstimatedValue()+ " ");
		    System.out.println("] " + input.getxLEDistribution().get(0).getUnit() );
		
		System.out.print("Twist distribution : [");
		for(int i=0; i<input.getTwistDistribution().size(); i++)
			System.out.print("  " +input.getTwistDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getTwistDistribution().get(0).getUnit() );
			
		System.out.print("Dihedral distribution : [");
		for(int i=0; i<input.getDihedralDistribution().size(); i++)
			System.out.print("  " +input.getDihedralDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getDihedralDistribution().get(0).getUnit() );
			
		System.out.print("Alpha zero lift distribution : [");
		for(int i=0; i<input.getAlphaZeroLiftDistribution().size(); i++)
			System.out.print("  " +input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getAlphaZeroLiftDistribution().get(0).getUnit() );
			
		System.out.print("Alpha Star distribution: [");
		for(int i=0; i<input.getAlphaStarDistribution().size(); i++)
			System.out.print("  " +input.getAlphaStarDistribution().get(i).getEstimatedValue()+ " ");
			System.out.println("] " + input.getAlphaStarDistribution().get(0).getUnit() );	
			
		System.out.print("Cl max distribution : ");
			System.out.print(input.getMaximumliftCoefficientDistribution());
		}
	}
}

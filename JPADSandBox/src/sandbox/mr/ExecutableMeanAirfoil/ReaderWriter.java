package sandbox.mr.ExecutableMeanAirfoil;

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

import calculators.geometry.LSGeometryCalc;
import configuration.enumerations.AirfoilFamilyEnum;
import database.databasefunctions.aerodynamics.AerodynamicDatabaseReader;
import database.databasefunctions.aerodynamics.DatabaseManager;
import sandbox.vt.ExecutableHighLiftDevices.InputTree;
import sandbox.vt.ExecutableHighLiftDevices.OutputTree;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class ReaderWriter {
	
	static InputOutputTree input = new InputOutputTree();

	public void importFromXML(String pathToXML, String databaseFolderPath, String aerodynamicDatabaseFileName) throws ParserConfigurationException {

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		
		List<String> numberOfSectionProperty =  reader.getXMLPropertiesByPath("//number_of_input_sections");
		input.setNumberOfSection(Integer.valueOf(numberOfSectionProperty.get(0)));

		//---------------------------------------------------------------------------------
		// GEOMETRY
		
		List<String> maximumThicknessProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/maximum_thickness").get(0));
		for(int i=0; i<maximumThicknessProperty.size(); i++)
			input.getMaximumThicknessArray().add(Double.valueOf(maximumThicknessProperty.get(i)));
		
		List<String> leadingEdgeRadiusProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/leading_edge_radius").get(0));
		for(int i=0; i<leadingEdgeRadiusProperty.size(); i++)
			input.getRadiusLEArray().add(Amount.valueOf(Double.valueOf(leadingEdgeRadiusProperty.get(i)),SI.METER));
		
		List<String> phiTrailingEdgeProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/trailing_edge_angle").get(0));
		for(int i=0; i<phiTrailingEdgeProperty.size(); i++)
			input.getPhiTEArray().add(Amount.valueOf(Double.valueOf(phiTrailingEdgeProperty.get(i)),NonSI.DEGREE_ANGLE));
		
		List<String> chordsProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/chords").get(0));
		for(int i=0; i<chordsProperty.size(); i++)
			input.getChordsArray().add(Amount.valueOf(Double.valueOf(chordsProperty.get(i)),SI.METER));
		
		//----------------------------------------------------------------------------------
		// AERODYNAMICS
		
		List<String> alphaZeroLiftProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/alpha_zero_lift").get(0));
		for(int i=0; i<alphaZeroLiftProperty.size(); i++)
			input.getAlphaZeroLiftArray().add(Amount.valueOf(Double.valueOf(alphaZeroLiftProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStarProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/angle_of_end_linearity").get(0));
		for(int i=0; i<alphaStarProperty.size(); i++)
			input.getAlphaStarArray().add(Amount.valueOf(Double.valueOf(alphaStarProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> alphaStllProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/angle_of_stall").get(0));
		for(int i=0; i<alphaStllProperty.size(); i++)
			input.getAngleOfStallArray().add(Amount.valueOf(Double.valueOf(alphaStllProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> clZeroProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_alpha_zero").get(0));
		for(int i=0; i<clZeroProperty.size(); i++)
			input.getCl0Array().add(Double.valueOf(clZeroProperty.get(i)));
		
		List<String> clStarProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_end_linearity").get(0));
		for(int i=0; i<clStarProperty.size(); i++)
			input.getClStarArray().add(Double.valueOf(clStarProperty.get(i)));
		
		List<String> clMaxProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/maximum_lift_coefficient").get(0));
		for(int i=0; i<clMaxProperty.size(); i++)
			input.getClmaxArray().add(Double.valueOf(clMaxProperty.get(i)));
		
		List<String> clAlphaProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//cl_zero").get(0));
		for(int i=0; i<clAlphaProperty.size(); i++)
			input.getClAlphaArray().add(Amount.valueOf(Double.valueOf(clAlphaProperty.get(i)), NonSI.DEGREE_ANGLE.inverse()));
		
		// TODO: COMPLETE ME !!
			

//	// WARNINGS
//		
//		if ( input.getNumberOfSections() != input.getChordDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of chords. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of chords = " + input.getChordDistribution().size() + " )");
//		}
//		
//		if ( input.getNumberOfSections() != input.getxLEDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of XLE values. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of XLE values = " + input.getxLEDistribution().size()+ " )");
//		}
//		
//		if ( input.getNumberOfSections() != input.getTwistDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of twist angles. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of twist angles = " + input.getTwistDistribution().size()+ " )");
//		}
//		
//		if ( input.getNumberOfSections() != input.getDihedralDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of dihedral angles. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of dihedral angles = " + input.getDihedralDistribution().size()+ " )");
//		}
//		
//		if ( input.getNumberOfSections() != input.getAlphaZeroLiftDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of zero lift angles. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of zero lift angles = " + input.getAlphaZeroLiftDistribution().size()+ " )");
//		}
//		
//		if ( input.getNumberOfSections() != input.getAlphaStarDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of end of linearity angles. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of end of linearity angles = " + input.getAlphaStarDistribution().size()+ " )");
//		}
//		
//		
//		if ( input.getNumberOfSections() != input.getMaximumliftCoefficientDistribution().size()){
//			 System.err.println("WARNING! the number of declared section differs from the number of cl max. ( number of section = " + input.getNumberOfSections()
//			 + " ; number of cl max = " + input.getMaximumliftCoefficientDistribution().size()+ " )");
//		}
//		
//	// OTHER VALUES
//		double span = Math.sqrt(input.getAspectRatio() * input.getSurface().getEstimatedValue());
//		input.setSpan(Amount.valueOf(span, SI.METER));
//		input.setSemiSpan(Amount.valueOf(span/2, SI.METER));
//		
//	// delta alpha	
//		
//		double tgAngle =input.getxLEDistribution().get(input.getNumberOfSections()-1).getEstimatedValue()/input.getSemiSpan().getEstimatedValue();
//		double sweepLE =Math.toDegrees(Math.atan(tgAngle));
//		double deltaAlpha = aeroDatabaseReader.getD_Alpha_Vs_LambdaLE_VsDy(sweepLE,sharpnessParameterLE);
//		
//		input.setDeltaAlpha(deltaAlpha);
//		
//		
//	// PRINT
//		
//		if(input.getNumberOfSections() == input.getChordDistribution().size() &&
//				input.getNumberOfSections() == input.getxLEDistribution().size() &&
//				input.getNumberOfSections() == input.getTwistDistribution().size() &&
//				input.getNumberOfSections() == input.getDihedralDistribution().size() &&
//				input.getNumberOfSections() == input.getAlphaZeroLiftDistribution().size() &&
//				input.getNumberOfSections() == input.getAlphaStarDistribution().size() &&
//				input.getNumberOfSections() == input.getMaximumliftCoefficientDistribution().size() ){
//		System.out.println("\n\nINPUT DATA\n\n");
//		
//		System.out.println("Operating Conditions");
//		System.out.println("-------------------------------------");
//		System.out.println("Altitude : " + input.getAltitude().getEstimatedValue()+ " " + input.getAltitude().getUnit());
//		System.out.println("Mach Number : " + input.getMachNumber());
//		
//		System.out.println("\nAlpha Values");
//		System.out.println("-------------------------------------");
//		System.out.println("Number of Alpha : " + input.getNumberOfAlpha());
//		System.out.println("Alpha Initial : " + input.getAlphaInitial().getEstimatedValue()+ " " + input.getAlphaInitial().getUnit());
//		System.out.println("Alpha Final : " + input.getAlphaFinal().getEstimatedValue()+ " " + input.getAlphaFinal().getUnit());
//		
//		System.out.println("\nWing");
//		System.out.println("-------------------------------------");
//		System.out.println("Surface : " + input.getSurface().getEstimatedValue()+ " " + input.getSurface().getUnit());
//		System.out.println("Aspect Ratio : " + input.getAspectRatio());
//		System.out.println("Number of point along semi-span : " + input.getNumberOfPointSemispan());
//		System.out.println("Adimensional kink station : " + input.getAdimensionalKinkStation());
//		System.out.println("Span : " + input.getSpan().getEstimatedValue()+ " " + input.getSpan().getUnit());
//		System.out.println("\nDistribution");
//		System.out.println("-------------------------------------");
//		System.out.println("Number of given stations : " + input.getNumberOfSections());
//		
//		System.out.println("\nMean airoil type : " + input.getMeanAirfoilFamily());
//        System.out.println("Mean airfoil thickness : " + input.getMeanThickness());
//		
//		System.out.print("\nChord distribution : [");
//		for(int i=0; i<input.getChordDistribution().size(); i++)
//			System.out.print("  " +input.getChordDistribution().get(i).getEstimatedValue() + "  ");
//			System.out.println("] " + input.getChordDistribution().get(0).getUnit() );
//		
//		System.out.print("X LE distribution : [");
//		for(int i=0; i<input.getxLEDistribution().size(); i++)
//			System.out.print("  " +input.getxLEDistribution().get(i).getEstimatedValue()+ " ");
//		    System.out.println("] " + input.getxLEDistribution().get(0).getUnit() );
//		
//		System.out.print("Twist distribution : [");
//		for(int i=0; i<input.getTwistDistribution().size(); i++)
//			System.out.print("  " +input.getTwistDistribution().get(i).getEstimatedValue()+ " ");
//			System.out.println("] " + input.getTwistDistribution().get(0).getUnit() );
//			
//		System.out.print("Dihedral distribution : [");
//		for(int i=0; i<input.getDihedralDistribution().size(); i++)
//			System.out.print("  " +input.getDihedralDistribution().get(i).getEstimatedValue()+ " ");
//			System.out.println("] " + input.getDihedralDistribution().get(0).getUnit() );
//			
//		System.out.print("Alpha zero lift distribution : [");
//		for(int i=0; i<input.getAlphaZeroLiftDistribution().size(); i++)
//			System.out.print("  " +input.getAlphaZeroLiftDistribution().get(i).getEstimatedValue()+ " ");
//			System.out.println("] " + input.getAlphaZeroLiftDistribution().get(0).getUnit() );
//			
//		System.out.print("Alpha Star distribution: [");
//		for(int i=0; i<input.getAlphaStarDistribution().size(); i++)
//			System.out.print("  " +input.getAlphaStarDistribution().get(i).getEstimatedValue()+ " ");
//			System.out.println("] " + input.getAlphaStarDistribution().get(0).getUnit() );	
//			
//		System.out.print("Cl max distribution : ");
//			System.out.print(input.getMaximumliftCoefficientDistribution());
//			
//		System.out.print("\nAdimentional stations :");
//		 	System.out.println(input.getyAdimensionalStationInput());
//		}
//	}
//	
//	
//	
//	public static void writeToXML(String filenameWithPathAndExt) {
//		
//		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
//		
//		try {
//			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
//			Document doc = docBuilder.newDocument();
//			
//			defineXmlTree(doc, docBuilder);
//			
//			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);
//
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	
//	private static void defineXmlTree(Document doc, DocumentBuilder docBuilder) {
//		
//		org.w3c.dom.Element rootElement = doc.createElement("Wing_aerodynamic_executable");
//		doc.appendChild(rootElement);
//		
//		//--------------------------------------------------------------------------------------
//		// INPUT
//		//--------------------------------------------------------------------------------------
//		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
//		rootElement.appendChild(inputRootElement);
//
//		org.w3c.dom.Element flightConditionsElement = doc.createElement("operating_conditions");
//		inputRootElement.appendChild(flightConditionsElement);
//
//		JPADStaticWriteUtils.writeSingleNode("altitude", input.getAltitude(), flightConditionsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("mach_number", input.getMachNumber(), flightConditionsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("number_of_alpha", input.getNumberOfAlpha(), flightConditionsElement, doc);
//		if (input.getNumberOfAlpha()!=0){
//		JPADStaticWriteUtils.writeSingleNode("alpha_initial", input.getAlphaInitial(), flightConditionsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("alpha_final", input.getAlphaFinal(), flightConditionsElement, doc);
//		}
//				
//		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
//		inputRootElement.appendChild(wingDataElement);
//		
//		org.w3c.dom.Element geometryDataElement = doc.createElement("global");
//		wingDataElement.appendChild(geometryDataElement);
//		
//		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("number_of_point_semispan", input.getNumberOfPointSemispan(), geometryDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("adimensional_kink_station", input.getAdimensionalKinkStation(), geometryDataElement, doc);
//		
//		
//		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("distibution");
//		wingDataElement.appendChild(cleanConfigurationDataElement);
//		
//		JPADStaticWriteUtils.writeSingleNode("number_of_given_sections", input.getNumberOfSections(), cleanConfigurationDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("airfoil_family", input.getMeanAirfoilFamily(), cleanConfigurationDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", input.getMeanThickness(), cleanConfigurationDataElement, doc);
//		
//		org.w3c.dom.Element childDistribution = doc.createElement("geometry");
//		cleanConfigurationDataElement.appendChild(childDistribution);
//		
//		JPADStaticWriteUtils.writeSingleNode("y_adimensional_stations", input.getyAdimensionalStationInput(),childDistribution, doc);
//		JPADStaticWriteUtils.writeSingleNode("chord_distribution", input.getChordDistribution(), childDistribution, doc, input.getChordDistribution().get(0).getUnit().toString());
//		JPADStaticWriteUtils.writeSingleNode("x_le_distribution", input.getxLEDistribution(), childDistribution, doc, input.getxLEDistribution().get(0).getUnit().toString());
//		JPADStaticWriteUtils.writeSingleNode("twist_distribution", input.getTwistDistribution(), childDistribution, doc, input.getTwistDistribution().get(0).getUnit().toString());
//		JPADStaticWriteUtils.writeSingleNode("dihedral_distribution", input.getDihedralDistribution(), childDistribution, doc,  input.getDihedralDistribution().get(0).getUnit().toString());
//		
//		org.w3c.dom.Element childDistributionNew = doc.createElement("aerodynamics");
//		cleanConfigurationDataElement.appendChild(childDistributionNew);
//		
//		JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift_distribution", input.getAlphaZeroLiftDistribution(), childDistributionNew, doc,  input.getAlphaZeroLiftDistribution().get(0).getUnit().toString());
//		JPADStaticWriteUtils.writeSingleNode("alpha_star_distribution", input.getAlphaStarDistribution(), childDistributionNew, doc,  input.getAlphaStarDistribution().get(0).getUnit().toString());
//		JPADStaticWriteUtils.writeSingleNode("maximum_lift_coefficient_distribution", input.getMaximumliftCoefficientDistribution(), childDistributionNew, doc);
//		
//		//--------------------------------------------------------------------------------------
//		// OUTPUT
//		//--------------------------------------------------------------------------------------
//		org.w3c.dom.Element outputRootElement = doc.createElement("OUTPUT");
//		rootElement.appendChild(outputRootElement);
//		
//		org.w3c.dom.Element highLiftDevicesEffectsElement = doc.createElement("wing_aerodynamic_characteristics");
//		outputRootElement.appendChild(highLiftDevicesEffectsElement);
//		
//		JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift", input.getAlphaZeroLift(), highLiftDevicesEffectsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("cl_alpha", Amount.valueOf(Double.valueOf(input.getClAlpha()), NonSI.DEGREE_ANGLE.inverse()), highLiftDevicesEffectsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("cl_star", input.getClStar(), highLiftDevicesEffectsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("alpha_star", input.getAlphaStar(), highLiftDevicesEffectsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("cl_max", input.getClMax(), highLiftDevicesEffectsElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("alpha_stall", input.getAlphaStall(), highLiftDevicesEffectsElement, doc);
//		
//		org.w3c.dom.Element highLiftGlobalDataElement = doc.createElement("cL_vs_alpha_curve");
//		outputRootElement.appendChild(highLiftGlobalDataElement);
//		
//		JPADStaticWriteUtils.writeSingleNode("cL_array", Arrays.toString(input.getcLVsAlphaVector()), highLiftGlobalDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("alpha_array", Arrays.toString(input.getAlphaVector()), highLiftGlobalDataElement, doc, "°");
//	
//		
//		org.w3c.dom.Element highLiftCurveDataElement = doc.createElement("distribution");
//		outputRootElement.appendChild(highLiftCurveDataElement);
//		
//		JPADStaticWriteUtils.writeSingleNode("eta", Arrays.toString(input.getyStationsAdimensional()), highLiftCurveDataElement, doc);
//		
//		if (input.getNumberOfAlpha()!=0){
//		for (int i=0; i<input.getNumberOfAlpha(); i++){
//		JPADStaticWriteUtils.writeSingleNode("cl_at_alpha_" + input.getAlphaDistributionArray()[i], Arrays.toString(input.getClVsEtaVectors().get(i)), highLiftCurveDataElement, doc);
//		}
//		}
//	}
//
//	public InputOutputTree getInput() {
//		return input;
//	}
//
//	public void setInput(InputOutputTree input) {
//		this.input = input;
	}
}

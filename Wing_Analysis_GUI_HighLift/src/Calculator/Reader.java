package Calculator;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Area;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;

import GUI.Views.VariablesInputData;
import configuration.enumerations.AirfoilFamilyEnum;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
import writers.JPADStaticWriteUtils;

public class Reader {
	
	
	public void readInputFromXML(VariablesInputData theVariables, String pathToXML) throws IOException{
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		Amount<Length> altitude = (Amount<Length>) reader.getXMLAmountWithUnitByPath("//altitude");
		Unit unitOfMeasurement = altitude.getUnit();
		theVariables.getAltitude().setText(Double.toString(altitude.doubleValue(unitOfMeasurement)));
		theVariables.getAltitudeUnits().setValue(unitOfMeasurement.toString());
		
		double machNumber =  Double.parseDouble(reader.getXMLPropertiesByPath("//mach_number").get(0));
		theVariables.getMachNumber().setText(Double.toString(machNumber));
		
		Amount<Area> surface = (Amount<Area>) reader.getXMLAmountWithUnitByPath("//surface");
		Unit unitOfMeasurementSurface = surface.getUnit();
		theVariables.getSurface().setText(Double.toString( surface.doubleValue(unitOfMeasurementSurface)));
		theVariables.getSurfaceUnits().setValue(unitOfMeasurementSurface.toString());

		double aspectRatio =  Double.parseDouble(reader.getXMLPropertiesByPath("//aspect_ratio").get(0));
		theVariables.getAspectRatio().setText(Double.toString(aspectRatio));
		
		int numberOfPointsSemiSpan =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_point_semispan").get(0));
		theVariables.getNumberOfPoints().setText(Double.toString(numberOfPointsSemiSpan));
		
		double adimensionalKinkStation =  Double.parseDouble(reader.getXMLPropertiesByPath("//adimensional_kink_station").get(0));
		theVariables.getAdimensionalKinkStation().setText(Double.toString(adimensionalKinkStation));
		
//		double thickenssMeanAirfoil =  Double.parseDouble(reader.getXMLPropertiesByPath("//max_thickness_mean_airfoil").get(0));
//		theVariables.getMaxThickness().setText(Double.toString(thickenssMeanAirfoil));
		
		List<String> airfoilFamilyProperty = reader.getXMLPropertiesByPath("//airfoil_family");
			theVariables.getAirfoilFamily().setValue(airfoilFamilyProperty.get(0));
		
	
			int numberOfSection =  (int)Double.parseDouble(reader.getXMLPropertiesByPath("//number_of_given_sections").get(0));
			String intNumberOfSection = String.valueOf(numberOfSection);
			theVariables.getNumberOfGivenSections().setValue(intNumberOfSection );
			
		theVariables.setNumberOfGivenSection();	
		
		
		// DISTRIBUTIONS
		
		List<String> stationsDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//y_adimensional_stations").get(0));
		for(int i=0; i<stationsDistribution.size(); i++)
			theVariables.getStationList().get(i).setText((stationsDistribution.get(i)));
		
		List<String> thicknessDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//max_thickness_distribution").get(0));
		for(int i=0; i<thicknessDistribution.size(); i++)
			theVariables.getThicknessList().get(i).setText((thicknessDistribution.get(i)));
		
		Unit unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//chord_distribution" + "/@unit"));
		List<String> chordDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//chord_distribution").get(0));
		for(int i=0; i<chordDistribution.size(); i++)
			theVariables.getChordList().get(i).setText((chordDistribution.get(i)));
		theVariables.getChordsUnits().setValue(unitOfMeas);
		
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//x_le_distribution" + "/@unit"));
		List<String> xleDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//x_le_distribution").get(0));
		for(int i=0; i<xleDistribution.size(); i++)
			theVariables.getXleList().get(i).setText((xleDistribution.get(i)));
		theVariables.getXleUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//twist_distribution" + "/@unit"));
		List<String> twistDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//twist_distribution").get(0));
		for(int i=0; i<twistDistribution.size(); i++)
			theVariables.getTwistList().get(i).setText((twistDistribution.get(i)));
		theVariables.getTwistUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//alpha_zero_lift_distribution" + "/@unit"));
		List<String> alphaZeroLiftDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_zero_lift_distribution").get(0));
		for(int i=0; i<alphaZeroLiftDistribution.size(); i++)
			theVariables.getAlphaZeroList().get(i).setText((alphaZeroLiftDistribution.get(i)));
		theVariables.getAlphaZeroLiftUnits().setValue(unitOfMeas);
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//alpha_star_distribution" + "/@unit"));
		List<String> alphaStarDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//alpha_star_distribution").get(0));
		for(int i=0; i<alphaStarDistribution.size(); i++)
			theVariables.getAlphaStarList().get(i).setText((alphaStarDistribution.get(i)));
		theVariables.getAlphaStarUnits().setValue(unitOfMeas);
		
		
		List<String> clMaxDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//maximum_lift_coefficient_distribution").get(0));
		for(int i=0; i<clMaxDistribution.size(); i++)
			theVariables.getClMaxList().get(i).setText((clMaxDistribution.get(i)));
		
		List<String> clZeroDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//cl0_distribution").get(0));
		for(int i=0; i<clZeroDistribution.size(); i++)
			theVariables.getcLZeroList().get(i).setText((clZeroDistribution.get(i)));
		
		unitOfMeas = Unit.valueOf(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//leading_edge_radius_distribution" + "/@unit"));
		List<String>leRadiusDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//leading_edge_radius_distribution").get(0));
		for(int i=0; i<leRadiusDistribution.size(); i++)
			theVariables.getLeRadiusList().get(i).setText((leRadiusDistribution.get(i)));
		theVariables.getLeRadiusUnits().setValue(unitOfMeas);
		
		System.out.println(MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//linear_slope_coefficient" + "/@unit"));
		
		if((MyXMLReaderUtils.getXMLPropertyByPath(reader.getXmlDoc(),reader.getXpath(),"//linear_slope_coefficient" + "/@unit"))=="1/rad")
			unitOfMeas = SI.RADIAN.inverse();
		else
		unitOfMeas = NonSI.DEGREE_ANGLE.inverse();
		
		List<String>clAlphaDistribution = reader.readArrayFromXML(reader.getXMLPropertiesByPath("//linear_slope_coefficient").get(0));
		for(int i=0; i<clAlphaDistribution.size(); i++)
			theVariables.getcLAlphaList().get(i).setText((clAlphaDistribution.get(i)));
		theVariables.getClAlphaUnits().setValue(unitOfMeas);
	
		// high lift
		
		int numberOfFlap = reader.getXMLPropertiesByPath("//flap_type").size();
		int numberOfSlat = reader.getXMLPropertiesByPath("//slat_deflection").size(); 
		
		if(numberOfFlap!=0 || numberOfSlat!= 0) {
		theVariables.getTheInputTree().setHighLiftInputTreeIsFilled(true);	
		theVariables.getTheInputTree().setNumberOfFlaps(numberOfFlap);
		theVariables.getTheInputTree().setNumberOfSlats(numberOfSlat);
		
		List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//flap_type");
		
		flapTypeProperty.stream().forEach(
				x -> theVariables.getTheInputTree().getFlapTypes().add( 
						Arrays.stream(FlapTypeEnum.values())
						.filter(a -> a.toString().equals(x))
						.findFirst()
						.orElseThrow(() -> {throw new IllegalStateException(String.format("Unsupported flap type", flapTypeProperty));}))
				);
		}

		List<String> cfcProperty = reader.getXMLPropertiesByPath("//flap_chord_ratio");
		for(int i=0; i<cfcProperty.size(); i++)
			theVariables.getTheInputTree().getFlapChordRatio().add(Double.valueOf(cfcProperty.get(i)));
		
		List<String> deltaFlapProperty = reader.getXMLPropertiesByPath("//flap_deflection");
		for(int i=0; i<deltaFlapProperty.size(); i++)
			theVariables.getTheInputTree().getFlapDeflection().add(Amount.valueOf(Double.valueOf(deltaFlapProperty.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> etaInFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_inner_station");
		for(int i=0; i<etaInFlapProperty.size(); i++)
			theVariables.getTheInputTree().getFlapInnerStation().add(Double.valueOf(etaInFlapProperty.get(i)));
		
		List<String> etaOutFlapProperty = reader.getXMLPropertiesByPath("//flap_non_dimensional_outer_station");
		for(int i=0; i<etaOutFlapProperty.size(); i++)
			theVariables.getTheInputTree().getFlapOuterStation().add(Double.valueOf(etaOutFlapProperty.get(i)));
	
		List<String> delta_slat_property = reader.getXMLPropertiesByPath("//slat_deflection");
		for(int i=0; i<delta_slat_property.size(); i++)
			theVariables.getTheInputTree().getSlatDeflection().add(Amount.valueOf(Double.valueOf(delta_slat_property.get(i)), NonSI.DEGREE_ANGLE));
		
		List<String> cs_c_property = reader.getXMLPropertiesByPath("//slat_chord_ratio");
		for(int i=0; i<cs_c_property.size(); i++)
			theVariables.getTheInputTree().getSlatChordRatio().add(Double.valueOf(cs_c_property.get(i)));
		
		List<String> cExt_c_slat_property = reader.getXMLPropertiesByPath("//slat_extension_ratio");
		for(int i=0; i<cExt_c_slat_property.size(); i++)
			theVariables.getTheInputTree().getSlatExtensionRatio().add(Double.valueOf(cExt_c_slat_property.get(i)));
		
		List<String> eta_in_slat_property = reader.getXMLPropertiesByPath("//slat_non_dimensional_inner_station");
		for(int i=0; i<eta_in_slat_property.size(); i++)
			theVariables.getTheInputTree().getSlatInnerStation().add(Double.valueOf(eta_in_slat_property.get(i)));
		
		List<String> eta_out_slat_property = reader.getXMLPropertiesByPath("//slat_non_dimensional_outer_station");
		for(int i=0; i<eta_out_slat_property.size(); i++)
			theVariables.getTheInputTree().getSlatOuterStation().add(Double.valueOf(eta_out_slat_property.get(i)));
	}
	
	public static void writeInputToXML(InputOutputTree theInputTree, String filenameWithPathAndExt) {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			defineXmlTree(doc, docBuilder, theInputTree);
			
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void defineXmlTree(Document doc, DocumentBuilder docBuilder, InputOutputTree input) {
		
		org.w3c.dom.Element rootElement = doc.createElement("Wing_analysis");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element flightConditionsElement = doc.createElement("operating_conditions");
		rootElement.appendChild(flightConditionsElement);
		
		JPADStaticWriteUtils.writeSingleNode("altitude", input.getAltitude(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("mach_number", input.getMachNumber(), flightConditionsElement, doc);
				
		org.w3c.dom.Element wingDataElement = doc.createElement("wing");
		rootElement.appendChild(wingDataElement);
		org.w3c.dom.Element geometryDataElement = doc.createElement("global");
		wingDataElement.appendChild(geometryDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("surface", input.getSurface(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("aspect_ratio", input.getAspectRatio(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("number_of_point_semispan", input.getNumberOfPointSemispan(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("adimensional_kink_station", input.getAdimensionalKinkStation(), geometryDataElement, doc);
		
		
		org.w3c.dom.Element cleanConfigurationDataElement = doc.createElement("distibution");
		wingDataElement.appendChild(cleanConfigurationDataElement);
		
		JPADStaticWriteUtils.writeSingleNode("number_of_given_sections", input.getNumberOfSections(), cleanConfigurationDataElement, doc);
		
		String airfoilFamily = null;

		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_4_Digit)
			airfoilFamily = "NACA_4_DIGIT";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_5_Digit)
			airfoilFamily = "NACA_5_DIGIT";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_63_Series)
			airfoilFamily = "NACA_63_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_64_Series)
			airfoilFamily = "NACA_64_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_65_Series)
			airfoilFamily = "NACA_65_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.NACA_66_Series)
			airfoilFamily = "NACA_66_SERIES";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.BICONVEX)
			airfoilFamily = "BICONVEX";
		if(input.getMeanAirfoilFamily() == AirfoilFamilyEnum.DOUBLE_WEDGE)
			airfoilFamily = "DOUBLE_WEDGE";

		JPADStaticWriteUtils.writeSingleNode("airfoil_family", airfoilFamily, cleanConfigurationDataElement, doc);
//		JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", input.getMeanThickness(), cleanConfigurationDataElement, doc);
		
		org.w3c.dom.Element childDistribution = doc.createElement("geometry");
		cleanConfigurationDataElement.appendChild(childDistribution);
		
		JPADStaticWriteUtils.writeSingleNode("y_adimensional_stations", input.getyAdimensionalStationInput(),childDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("chord_distribution", input.getChordDistribution(), childDistribution, doc, input.getChordDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("x_le_distribution", input.getxLEDistribution(), childDistribution, doc, input.getxLEDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("twist_distribution", input.getTwistDistribution(), childDistribution, doc, input.getTwistDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNode("max_thickness_distribution", input.getThicknessDistribution(),childDistribution, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("leading_edge_radius_distribution", input.getLeRadiusDistribution(), childDistribution, doc, input.getLeRadiusDistribution().get(0).getUnit().toString());
		
		org.w3c.dom.Element childDistributionNew = doc.createElement("aerodynamics");
		cleanConfigurationDataElement.appendChild(childDistributionNew);
		
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_zero_lift_distribution", input.getAlphaZeroLiftDistribution(), childDistributionNew, doc,  input.getAlphaZeroLiftDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_star_distribution", input.getAlphaStarDistribution(), childDistributionNew, doc,  input.getAlphaStarDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("cl0_distribution", input.getcLZeroDistribution(), childDistributionNew, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("linear_slope_coefficient", input.getClAlphaDistribution(), childDistributionNew, doc,  input.getClAlphaDistribution().get(0).getUnit().toString());
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("maximum_lift_coefficient_distribution", input.getMaximumliftCoefficientDistribution(), childDistributionNew, doc);
		
		if(input.isHighLiftInputTreeIsFilled()) {
			org.w3c.dom.Element highLiftwingDataElement = doc.createElement("high_lift_devices");
			rootElement.appendChild(highLiftwingDataElement);
			org.w3c.dom.Element flapElement = doc.createElement("flaps");
			highLiftwingDataElement.appendChild(flapElement);
			for(int i=0 ; i<input.getNumberOfFlaps(); i++) {
				org.w3c.dom.Element flapDeviceElement = doc.createElement("flap");
				flapElement.appendChild(flapDeviceElement);
				JPADStaticWriteUtils.writeSingleNode("flap_type", input.getFlapTypes().get(i).toString(), flapElement, doc);
				JPADStaticWriteUtils.writeSingleNode("flap_chord_ratio", input.getFlapChordRatio().get(i), flapElement, doc);
				JPADStaticWriteUtils.writeSingleNode("flap_deflection", input.getFlapDeflection().get(i), flapElement, doc);
				JPADStaticWriteUtils.writeSingleNode("flap_non_dimensional_inner_station", input.getFlapInnerStation().get(i), flapElement, doc);
				JPADStaticWriteUtils.writeSingleNode("flap_non_dimensional_outer_station", input.getFlapOuterStation().get(i), flapElement, doc);
			}
			
			org.w3c.dom.Element slatElement = doc.createElement("slats");
			highLiftwingDataElement.appendChild(slatElement);
			for(int i=0 ; i<input.getNumberOfSlats(); i++) {
				org.w3c.dom.Element slatDevicesElement = doc.createElement("slat");
				slatElement.appendChild(slatDevicesElement);
				JPADStaticWriteUtils.writeSingleNode("slat_deflection", input.getSlatDeflection().get(i), slatElement, doc);
				JPADStaticWriteUtils.writeSingleNode("slat_chord_ratio", input.getSlatChordRatio().get(i), slatElement, doc);
				JPADStaticWriteUtils.writeSingleNode("slat_extension_ratio", input.getSlatExtensionRatio().get(i), slatElement, doc);
				JPADStaticWriteUtils.writeSingleNode("slat_non_dimensional_inner_station", input.getSlatInnerStation().get(i), slatElement, doc);
				JPADStaticWriteUtils.writeSingleNode("slat_non_dimensional_outer_station", input.getSlatOuterStation().get(i), slatElement, doc);
			}
			
			
		}
	}
	
	public static void writeOutputToXML(InputOutputTree theInputTree, String filenameWithPathAndExt) {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			defineXmlTreeOutput(doc, docBuilder, theInputTree);
			
			JPADStaticWriteUtils.writeDocumentToXml(doc, filenameWithPathAndExt);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	private static void defineXmlTreeOutput(Document doc, DocumentBuilder docBuilder, InputOutputTree input) {
		
		org.w3c.dom.Element rootElement = doc.createElement("Wing_analysis");
		doc.appendChild(rootElement);
		
		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		if(input.performLiftAnalysis==true){
		org.w3c.dom.Element flightConditionsElement = doc.createElement("lift_curves_results");
		rootElement.appendChild(flightConditionsElement);
		
		JPADStaticWriteUtils.writeSingleNode("alpha_Zero_Lift", input.getAlphaZeroLift(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_Star", input.getAlphaStar(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_max_Linear", input.getAlphaMaxLinear(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("alpha_Stall", input.getAlphaStall(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha_1_deg", input.getcLAlphaDeg(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_alpha_1_rad", input.getcLAlphaRad(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_Zero", input.getcLZero(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_Star", input.getcLStar(), flightConditionsElement, doc);
		JPADStaticWriteUtils.writeSingleNode("cL_Max", input.getcLStall(), flightConditionsElement, doc);
		org.w3c.dom.Element liftCurve = doc.createElement("lift_curve");
		flightConditionsElement.appendChild(liftCurve);
		JPADStaticWriteUtils.writeSingleNode("alpha_angles", input.getAlphaArrayLiftCurve(), liftCurve, doc);
		JPADStaticWriteUtils.writeSingleNode("cL", input.getLiftCoefficientCurve(), liftCurve, doc);
		}
		
		if(input.performLoadAnalysis==true){
		org.w3c.dom.Element loadElement = doc.createElement("lift_coefficient_distribution");
		rootElement.appendChild(loadElement);
		JPADStaticWriteUtils.writeSingleNode("eta_stations", input.getyAdimensionalDistributionSemiSpan(), loadElement, doc);
		JPADStaticWriteUtils.writeSingleNode("y_stations", input.getyDimensionalDistributionSemiSpan(), loadElement, doc);
		for (int i=0; i<input.getAlphaArrayLiftDistribution().size(); i++){
		JPADStaticWriteUtils.writeSingleNode("cl_distribution_at_alpha_" + input.getAlphaArrayLiftDistribution().get(i).doubleValue(NonSI.DEGREE_ANGLE)+"_deg",
				input.getClDistributionCurves().get(i), loadElement, doc);
		}
		}
		
		if(input.performStallPathAnalysis==true){
		org.w3c.dom.Element stallPath = doc.createElement("stall_path");
		rootElement.appendChild(stallPath);
		JPADStaticWriteUtils.writeSingleNode("eta_stations", input.getyAdimensionalDistributionSemiSpan(), stallPath, doc);
		JPADStaticWriteUtils.writeSingleNode("cl_max_airfoils", input.getMaximumliftCoefficientDistributionSemiSpan(), stallPath, doc);
		JPADStaticWriteUtils.writeSingleNode("cl_max_at_alpha_" +  input.getAlphaMaxLinear().doubleValue(NonSI.DEGREE_ANGLE) +"_deg", input.getClMaxStallPath(), stallPath, doc);
	}
	}
	

}

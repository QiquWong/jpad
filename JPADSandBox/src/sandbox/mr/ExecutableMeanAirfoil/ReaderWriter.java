package sandbox.mr.ExecutableMeanAirfoil;

import java.util.List;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.w3c.dom.Document;
import standaloneutils.JPADXmlReader;
import writers.JPADStaticWriteUtils;

public class ReaderWriter {

	static InputOutputTree inputOutput = new InputOutputTree();

	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// GLOBAL DATA

		List<String> numberOfSectionProperty =  reader.getXMLPropertiesByPath("//global_data/number_of_input_sections");
		inputOutput.setNumberOfSection(Integer.valueOf(numberOfSectionProperty.get(0)));

		List<String> wingSpanProperty =  reader.getXMLPropertiesByPath("//global_data/wing_span");
		inputOutput.setWingSpan(Amount.valueOf(Double.valueOf(wingSpanProperty.get(0)), SI.METER));

		List<String> wingSurfaceProperty =  reader.getXMLPropertiesByPath("//global_data/wing_surface");
		inputOutput.setWingSurface(Amount.valueOf(Double.valueOf(wingSurfaceProperty.get(0)), SI.SQUARE_METRE));

		//---------------------------------------------------------------------------------
		// GEOMETRY

		List<String> etaStationsProperty =  JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/eta_stations").get(0));
		for(int i=0; i<etaStationsProperty.size(); i++)
			inputOutput.getEtaStations().add(Double.valueOf(etaStationsProperty.get(i)));

		List<String> chordsProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/chords").get(0));
		for(int i=0; i<chordsProperty.size(); i++)
			inputOutput.getChordsArray().add(Amount.valueOf(Double.valueOf(chordsProperty.get(i)),SI.METER));

		// this is used to evaluate if the tag is empty or not ...
		List<String> maximumThicknessTemp = reader.getXMLPropertiesByPath("//geometry/maximum_thickness");
		// this is used to read the tag value if it isn't empty ...
		if(!maximumThicknessTemp.isEmpty()) {
			List<String> maximumThicknessProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/maximum_thickness").get(0));
			for(int i=0; i<maximumThicknessProperty.size(); i++)
				inputOutput.getMaximumThicknessArray().add(Double.valueOf(maximumThicknessProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> leadingEdgeRadiusTemp = reader.getXMLPropertiesByPath("//geometry/leading_edge_radius");
		// this is used to read the tag value if it isn't empty ...
		if(!leadingEdgeRadiusTemp.isEmpty()) {
			List<String> leadingEdgeRadiusProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/leading_edge_radius").get(0));
			for(int i=0; i<leadingEdgeRadiusProperty.size(); i++)
				inputOutput.getRadiusLEArray().add(Amount.valueOf(Double.valueOf(leadingEdgeRadiusProperty.get(i)),SI.METER));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> phiTrailingEdgeTemp = reader.getXMLPropertiesByPath("//geometry/trailing_edge_angle");
		// this is used to read the tag value if it isn't empty ...
		if(!phiTrailingEdgeTemp.isEmpty()) {
			List<String> phiTrailingEdgeProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//geometry/trailing_edge_angle").get(0));
			for(int i=0; i<phiTrailingEdgeProperty.size(); i++)
				inputOutput.getPhiTEArray().add(Amount.valueOf(Double.valueOf(phiTrailingEdgeProperty.get(i)),NonSI.DEGREE_ANGLE));
		}

		//----------------------------------------------------------------------------------
		// AERODYNAMICS

		// this is used to evaluate if the tag is empty or not ...
		List<String> alphaZeroLiftTemp = reader.getXMLPropertiesByPath("//aerodynamic/alpha_zero_lift");
		// this is used to read the tag value if it isn't empty ...
		if(!alphaZeroLiftTemp.isEmpty()) {
			List<String> alphaZeroLiftProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/alpha_zero_lift").get(0));
			for(int i=0; i<alphaZeroLiftProperty.size(); i++)
				inputOutput.getAlphaZeroLiftArray().add(Amount.valueOf(Double.valueOf(alphaZeroLiftProperty.get(i)), NonSI.DEGREE_ANGLE));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> alphaStarTemp = reader.getXMLPropertiesByPath("//aerodynamic/angle_of_end_linearity");
		// this is used to read the tag value if it isn't empty ...
		if(!alphaStarTemp.isEmpty()) {
			List<String> alphaStarProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/angle_of_end_linearity").get(0));
			for(int i=0; i<alphaStarProperty.size(); i++)
				inputOutput.getAlphaStarArray().add(Amount.valueOf(Double.valueOf(alphaStarProperty.get(i)), NonSI.DEGREE_ANGLE));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> alphaStallTemp = reader.getXMLPropertiesByPath("//aerodynamic/angle_of_stall");
		// this is used to read the tag value if it isn't empty ...
		if(!alphaStallTemp.isEmpty()) {
			List<String> alphaStllProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/angle_of_stall").get(0));
			for(int i=0; i<alphaStllProperty.size(); i++)
				inputOutput.getAngleOfStallArray().add(Amount.valueOf(Double.valueOf(alphaStllProperty.get(i)), NonSI.DEGREE_ANGLE));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> clZeroTemp = reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_alpha_zero");
		// this is used to read the tag value if it isn't empty ...
		if(!clZeroTemp.isEmpty()) {
			List<String> clZeroProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_alpha_zero").get(0));
			for(int i=0; i<clZeroProperty.size(); i++)
				inputOutput.getCl0Array().add(Double.valueOf(clZeroProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> clStarTemp = reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_end_linearity");
		// this is used to read the tag value if it isn't empty ...
		if(!clStarTemp.isEmpty()) {
			List<String> clStarProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_end_linearity").get(0));
			for(int i=0; i<clStarProperty.size(); i++)
				inputOutput.getClStarArray().add(Double.valueOf(clStarProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> clMaxTemp = reader.getXMLPropertiesByPath("//aerodynamic/maximum_lift_coefficient");
		// this is used to read the tag value if it isn't empty ...
		if(!clMaxTemp.isEmpty()) {
			List<String> clMaxProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/maximum_lift_coefficient").get(0));
			for(int i=0; i<clMaxProperty.size(); i++)
				inputOutput.getClmaxArray().add(Double.valueOf(clMaxProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> clAlphaTemp = reader.getXMLPropertiesByPath("//aerodynamic/lift_curve_slope");
		// this is used to read the tag value if it isn't empty ...
		if(!clAlphaTemp.isEmpty()) {
			List<String> clAlphaProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/lift_curve_slope").get(0));
			for(int i=0; i<clAlphaProperty.size(); i++)
				inputOutput.getClAlphaArray().add(Amount.valueOf(Double.valueOf(clAlphaProperty.get(i)), NonSI.DEGREE_ANGLE.inverse()));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> cdMinTemp = reader.getXMLPropertiesByPath("//aerodynamic/minimum_drag_coefficient");
		// this is used to read the tag value if it isn't empty ...
		if(!cdMinTemp.isEmpty()) {
			List<String> cdMinProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/minimum_drag_coefficient").get(0));
			for(int i=0; i<cdMinProperty.size(); i++)
				inputOutput.getCdminArray().add(Double.valueOf(cdMinProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> clCdMinTemp = reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_at_minimum_drag");
		// this is used to read the tag value if it isn't empty ...
		if(!clCdMinTemp.isEmpty()) {
			List<String> clCdMinProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/lift_coefficient_at_minimum_drag").get(0));
			for(int i=0; i<clCdMinProperty.size(); i++)
				inputOutput.getClAtCdminArray().add(Double.valueOf(clCdMinProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> kFactorDragPolarTemp = reader.getXMLPropertiesByPath("//aerodynamic/k_factor_drag_polar");
		// this is used to read the tag value if it isn't empty ...
		if(!kFactorDragPolarTemp.isEmpty()) {
			List<String> kFactorDragPolarProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/k_factor_drag_polar").get(0));
			for(int i=0; i<kFactorDragPolarProperty.size(); i++)
				inputOutput.getkFactorDragPolarArray().add(Double.valueOf(kFactorDragPolarProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> xACTemp = reader.getXMLPropertiesByPath("//aerodynamic/aerodynamic_center");
		// this is used to read the tag value if it isn't empty ...
		if(!xACTemp.isEmpty()) {
			List<String> xACProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/aerodynamic_center").get(0));
			for(int i=0; i<xACProperty.size(); i++)
				inputOutput.getXacArray().add(Double.valueOf(xACProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> cmACTemp = reader.getXMLPropertiesByPath("//aerodynamic/pitching_moment_coefficient_aerodynamic_center");
		// this is used to read the tag value if it isn't empty ...
		if(!cmACTemp.isEmpty()) {
			List<String> cmACProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/pitching_moment_coefficient_aerodynamic_center").get(0));
			for(int i=0; i<cmACProperty.size(); i++)
				inputOutput.getCmACArray().add(Double.valueOf(cmACProperty.get(i)));
		}

		// this is used to evaluate if the tag is empty or not ...
		List<String> cmACStallTemp = reader.getXMLPropertiesByPath("//aerodynamic/stall_pitching_moment_coefficient_aerodynamic_center");
		// this is used to read the tag value if it isn't empty ...
		if(!cmACStallTemp.isEmpty()) {
			List<String> cmACStallProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//aerodynamic/stall_pitching_moment_coefficient_aerodynamic_center").get(0));
			for(int i=0; i<cmACStallProperty.size(); i++)
				inputOutput.getCmACstallArray().add(Double.valueOf(cmACStallProperty.get(i)));
		}

		//----------------------------------------------------------------------------------
		// OTHER

		// this is used to evaluate if the other values tag is empty or not ...
		List<String> otherValuesTemp = reader.getXMLPropertiesByPath("//other/other_values");
		// this is used to read the other values tag if it isn't empty ...
		if(!otherValuesTemp.isEmpty()) {
			List<String> otherValuesProperty = JPADXmlReader.readArrayFromXML(reader.getXMLPropertiesByPath("//other/other_values").get(0));
			for(int i=0; i<otherValuesProperty.size(); i++)
				inputOutput.getOtherValuesArray().add(Double.valueOf(otherValuesProperty.get(i)));
		}

		//----------------------------------------------------------------------------------
		// WARNINGS

		if ( inputOutput.getNumberOfSection() != inputOutput.getEtaStations().size()) {
			System.err.println("WARNING! number of sections is not the same as the eta station array length. ( number of sections = " + inputOutput.getNumberOfSection()
			+ " ; number of eta stations = " + inputOutput.getEtaStations().size() + " )");
			return;
		}

		if ( inputOutput.getNumberOfSection() != inputOutput.getChordsArray().size()) {
			System.err.println("WARNING! number of sections is not the same as the chords array length. ( number of sections = " + inputOutput.getNumberOfSection()
			+ " ; number of chords = " + inputOutput.getChordsArray().size() + " )");
			return;
		}

		if(!cmACStallTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getMaximumThicknessArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the maximum thickness array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of maximum thickness = " + inputOutput.getMaximumThicknessArray().size() + " )");
				return;
			}
		}

		if(!leadingEdgeRadiusTemp.isEmpty()){
			if ( inputOutput.getNumberOfSection() != inputOutput.getRadiusLEArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the leading edge radius array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of leading edge radius = " + inputOutput.getRadiusLEArray().size() + " )");
				return;
			}
		}

		if(!phiTrailingEdgeTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getPhiTEArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the trailing edge angle array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of trailing edge angles = " + inputOutput.getPhiTEArray().size() + " )");
				return;
			}
		}

		if(!alphaZeroLiftTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getAlphaZeroLiftArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the alpha zero lift array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of alpha zero lift = " + inputOutput.getAlphaZeroLiftArray().size() + " )");
				return;
			}
		}

		if(!alphaStarTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getAlphaStarArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the alpha star array length. ( number of section = " + inputOutput.getNumberOfSection()
				+ " ; number of alpha star = " + inputOutput.getAlphaStarArray().size()+ " )");
				return;
			}
		}

		if(!alphaStallTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getAngleOfStallArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the stall angles of attack array length. ( number of section = " + inputOutput.getNumberOfSection()
				+ " ; number of stall angles of attack = " + inputOutput.getAngleOfStallArray().size()+ " )");
				return;
			}
		}

		if(!clZeroTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getCl0Array().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the lift coefficient at alpha zero array. ( number of section = " + inputOutput.getNumberOfSection()
				+ " ; number of cl0 = " + inputOutput.getCl0Array().size()+ " )");
				return;
			}
		}

		if(!clStarTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getClStarArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the lift coefficient of end linearity array. ( number of section = " + inputOutput.getNumberOfSection()
				+ " ; number of cl star = " + inputOutput.getClStarArray().size()+ " )");
				return;
			}
		}

		if(!clMaxTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getClmaxArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the maximum lif coefficient array. ( number of section = " + inputOutput.getNumberOfSection()
				+ " ; number of cl max = " + inputOutput.getClmaxArray().size()+ " )");
				return;
			}
		}

		if(!clAlphaTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getClAlphaArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the lift curve slope array. ( number of section = " + inputOutput.getNumberOfSection()
				+ " ; number of cl alpha = " + inputOutput.getClAlphaArray().size()+ " )");
				return;
			}
		}

		if(!cdMinTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getCdminArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the minimum drag coefficient array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of cd min = " + inputOutput.getCdminArray().size() + " )");
				return;
			}
		}

		if(!clCdMinTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getClAtCdminArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the lift coefficient at minimum drag array. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of cl at cd min = " + inputOutput.getClAtCdminArray().size() + " )");
				return;
			}
		}

		if(!kFactorDragPolarTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getkFactorDragPolarArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the k factor drag polar array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of k factors = " + inputOutput.getkFactorDragPolarArray().size() + " )");
				return;
			}
		}

		if(!xACTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getXacArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the aerodynamic center array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of x_ac = " + inputOutput.getXacArray().size() + " )");
				return;
			}
		}

		if(!cmACTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getCmACArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the pitching moment coefficient at aerodynamic center array. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of cm_ac = " + inputOutput.getCmACArray().size() + " )");
				return;
			}
		}

		if(!cmACStallTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getCmACstallArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the length of the stall pitching moment coefficient at aerodynamic center array. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of cm_ac at stall = " + inputOutput.getCmACstallArray().size() + " )");
				return;
			}
		}

		if(!otherValuesTemp.isEmpty()) {
			if ( inputOutput.getNumberOfSection() != inputOutput.getOtherValuesArray().size()) {
				System.err.println("WARNING! number of sections is not the same as the other values array length. ( number of sections = " + inputOutput.getNumberOfSection()
				+ " ; number of other values = " + inputOutput.getOtherValuesArray().size() + " )");
				return;
			}
		}

		//-----------------------------------------------------------------------------------
		// PRINT VALUES:
		System.out.println("\n\t\tINPUT DATA");

		System.out.println("Global data :");
		System.out.println("\tNumber of sections = " + inputOutput.getNumberOfSection());
		System.out.println("\tWing span = " + inputOutput.getWingSpan());
		System.out.println("\tWing surface = " + inputOutput.getWingSurface());

		System.out.println("Geometry : ");
		System.out.println("\tEta stations = " + inputOutput.getEtaStations() + "\n");
		System.out.println("\tChords array = " + inputOutput.getChordsArray() + "\n");
		if(!cmACStallTemp.isEmpty())
			System.out.println("\tMaximum thickness array = " + inputOutput.getMaximumThicknessArray());
		if(!leadingEdgeRadiusTemp.isEmpty())
			System.out.println("\tLeading edge radius array = " + inputOutput.getRadiusLEArray());
		if(!phiTrailingEdgeTemp.isEmpty())
			System.out.println("\tTrailing edge angle array = " + inputOutput.getPhiTEArray());

		System.out.println("Aerodynamic : ");
		if(!alphaZeroLiftTemp.isEmpty())
			System.out.println("\tAlpha zero lift array = " + inputOutput.getAlphaZeroLiftArray());
		if(!alphaStarTemp.isEmpty())
			System.out.println("\tAlpha star array = " + inputOutput.getAlphaStarArray());
		if(!alphaStallTemp.isEmpty())
			System.out.println("\tAlpha stall array = " + inputOutput.getAngleOfStallArray());
		if(!clZeroTemp.isEmpty())
			System.out.println("\tLift coefficients at alpha zero = " + inputOutput.getCl0Array());
		if(!clStarTemp.isEmpty())
			System.out.println("\tLift coefficients at end linearity = " + inputOutput.getClStarArray());
		if(!clMaxTemp.isEmpty())
			System.out.println("\tMaximum lift coefficients = " + inputOutput.getClmaxArray());
		if(!clAlphaTemp.isEmpty())
			System.out.println("\tLift curve slopes = " + inputOutput.getClAlphaArray());
		if(!cdMinTemp.isEmpty())
			System.out.println("\tMinimum drag coefficients = " + inputOutput.getCdminArray());
		if(!clCdMinTemp.isEmpty())
			System.out.println("\tLift coefficients at minimum drag = " + inputOutput.getClAtCdminArray());
		if(!kFactorDragPolarTemp.isEmpty())
			System.out.println("\tK factors drag polar = " + inputOutput.getkFactorDragPolarArray());
		if(!xACTemp.isEmpty())
			System.out.println("\tAerodynamic centers = " + inputOutput.getXacArray());
		if(!cmACTemp.isEmpty())
			System.out.println("\tPitching moment coefficients at aerodynamic center = " + inputOutput.getCmACArray());
		if(!cmACStallTemp.isEmpty())
			System.out.println("\tStall pitching moment coefficients at aerodynamic center = " + inputOutput.getCmACstallArray() + "\n");

		if(!otherValuesTemp.isEmpty()) {
			System.out.println("Other values : ");
			System.out.println("\tOther values array = " + inputOutput.getOtherValuesArray() + "\n");
		}
	}

	public static void writeToXML(String filenameWithPathAndExt) {

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

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

		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());

		org.w3c.dom.Element rootElement = doc.createElement("MAGA_Calculator");
		doc.appendChild(rootElement);

		//--------------------------------------------------------------------------------------
		// INPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element inputRootElement = doc.createElement("INPUT");
		rootElement.appendChild(inputRootElement);

		org.w3c.dom.Element globalDataElement = doc.createElement("global_data");
		inputRootElement.appendChild(globalDataElement);

		JPADStaticWriteUtils.writeSingleNode("number_of_input_sections", inputOutput.getNumberOfSection(), globalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("wing_span", inputOutput.getWingSpan(), globalDataElement, doc);
		JPADStaticWriteUtils.writeSingleNode("wing_surface", inputOutput.getWingSurface(), globalDataElement, doc);

		org.w3c.dom.Element geometryDataElement = doc.createElement("geometry");
		inputRootElement.appendChild(geometryDataElement);

		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("eta_stations", inputOutput.getEtaStations(), geometryDataElement, doc);
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("chords", inputOutput.getChordsArray(), geometryDataElement, doc, "m");
		if(!inputOutput.getMaximumThicknessArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("maximum_thickness", inputOutput.getMaximumThicknessArray(), geometryDataElement, doc);
		if(!inputOutput.getRadiusLEArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("leading_edge_radius", inputOutput.getRadiusLEArray(), geometryDataElement, doc, "m");
		if(!inputOutput.getPhiTEArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("trailing_edge_angle", inputOutput.getPhiTEArray(), geometryDataElement, doc, "deg");

		org.w3c.dom.Element aerodynamicDataElement = doc.createElement("aerodynamic");
		inputRootElement.appendChild(aerodynamicDataElement);

		if(!inputOutput.getAlphaZeroLiftArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("alpha_zero_lift", inputOutput.getAlphaZeroLiftArray(), aerodynamicDataElement, doc, "deg");
		if(!inputOutput.getAlphaStarArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("angle_of_end_linearity", inputOutput.getAlphaStarArray(), aerodynamicDataElement, doc, "deg");
		if(!inputOutput.getAngleOfStallArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("angle_of_stall", inputOutput.getAngleOfStallArray(), aerodynamicDataElement, doc, "deg");
		if(!inputOutput.getCl0Array().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("lift_coefficient_alpha_zero", inputOutput.getCl0Array(), aerodynamicDataElement, doc);
		if(!inputOutput.getClStarArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("lift_coefficient_end_linearity", inputOutput.getClStarArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getClmaxArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("maximum_lift_coefficient", inputOutput.getClmaxArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getClAlphaArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("lift_curve_slope", inputOutput.getClAlphaArray(), aerodynamicDataElement, doc, "1/deg");
		if(!inputOutput.getCdminArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("minimum_drag_coefficient", inputOutput.getCdminArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getClAtCdminArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("lift_coefficient_at_minimum_drag", inputOutput.getClAtCdminArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getkFactorDragPolarArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("k_factor_drag_polar", inputOutput.getkFactorDragPolarArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getXacArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("aerodynamic_center", inputOutput.getXacArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getCmACArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("pitching_moment_coefficient_aerodynamic_center", inputOutput.getCmACArray(), aerodynamicDataElement, doc);
		if(!inputOutput.getCmACstallArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("stall_pitching_moment_coefficient_aerodynamic_center", inputOutput.getCmACstallArray(), aerodynamicDataElement, doc);

		if(!inputOutput.getOtherValuesArray().isEmpty()) {
			org.w3c.dom.Element otherValuesElement = doc.createElement("other");
			inputRootElement.appendChild(otherValuesElement);

			JPADStaticWriteUtils.writeSingleNodeCPASCFormat("other_values", inputOutput.getOtherValuesArray(), otherValuesElement, doc);
		}

		//--------------------------------------------------------------------------------------
		// OUTPUT
		//--------------------------------------------------------------------------------------
		org.w3c.dom.Element outputRootElement = doc.createElement("OUTPUT");
		rootElement.appendChild(outputRootElement);

		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("influence_aeras", inputOutput.getInfluenceAreas(), outputRootElement, doc, "m²");
		JPADStaticWriteUtils.writeSingleNodeCPASCFormat("influence_coefficients", inputOutput.getInfluenceCoefficients(), outputRootElement, doc);

		org.w3c.dom.Element meanAirfoilGeometryElement = doc.createElement("mean_airfoil_geometry_data");
		outputRootElement.appendChild(meanAirfoilGeometryElement);

		JPADStaticWriteUtils.writeSingleNode("mean_airfoil_chord", inputOutput.getChords(), meanAirfoilGeometryElement, doc);
		if(!inputOutput.getMaximumThicknessArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("max_thickness_mean_airfoil", inputOutput.getMaximumThickness(), meanAirfoilGeometryElement, doc);
		if(!inputOutput.getRadiusLEArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("leading_edge_radius_mean_airfoil", inputOutput.getRadiusLE(), meanAirfoilGeometryElement, doc);
		if(!inputOutput.getPhiTEArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("trailing_edge_angle_mean_airfoil", inputOutput.getPhiTE(), meanAirfoilGeometryElement, doc);

		org.w3c.dom.Element meanAirfoilAerodynamicElement = doc.createElement("mean_airfoil_aerodynamic_data");
		outputRootElement.appendChild(meanAirfoilAerodynamicElement);

		if(!inputOutput.getAlphaZeroLiftArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("alpha_zero_lift_mean_airfoil", inputOutput.getAlphaZeroLift(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getAlphaStarArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("alpha_star_mean_airfoil", inputOutput.getAlphaStar(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getAngleOfStallArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("alpha_stall_mean_airfoil", inputOutput.getAngleOfStall(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getCl0Array().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cl0_mean_airfoil", inputOutput.getCl0(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getClStarArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cl_star_mean_airfoil", inputOutput.getClStar(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getClmaxArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cl_max_mean_airfoil", inputOutput.getClmax(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getClAlphaArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cl_alpha_mean_airfoil", inputOutput.getClAlpha(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getCdminArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cd_min_mean_airfoil", inputOutput.getCdmin(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getClAtCdminArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cl_at_cd_min_mean_airfoil", inputOutput.getClAtCdmin(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getkFactorDragPolarArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("k_factor_drag_polar_mean_airfoil", inputOutput.getkFactorDragPolar(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getXacArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("x_ac_mean_airfoil", inputOutput.getXac(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getCmACArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cm_ac_mean_airfoil", inputOutput.getCmAC(), meanAirfoilAerodynamicElement, doc);
		if(!inputOutput.getCmACstallArray().isEmpty())
			JPADStaticWriteUtils.writeSingleNode("cm_ac_stall_mean_airfoil", inputOutput.getCmACstall(), meanAirfoilAerodynamicElement, doc);

		if(!inputOutput.getOtherValuesArray().isEmpty()) {
			org.w3c.dom.Element meanAirfoilOtherValuesElement = doc.createElement("mean_airfoil_other_values");
			outputRootElement.appendChild(meanAirfoilOtherValuesElement);

			JPADStaticWriteUtils.writeSingleNode("other_values_mean_airfoil", inputOutput.getOtherValues(), meanAirfoilOtherValuesElement, doc);
		}
	}

	public static InputOutputTree getInputOutput() {
		return inputOutput;
	}

	public static void setInputOutput(InputOutputTree input) {
		ReaderWriter.inputOutput = input;
	}
}

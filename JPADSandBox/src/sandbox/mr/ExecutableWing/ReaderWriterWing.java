package sandbox.mr.ExecutableWing;

import java.util.List;

import javax.measure.unit.NonSI;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.NodeList;

import sandbox.vt.ExecutableHighLiftDevices.InputTree;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class ReaderWriterWing {
	
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		InputTree input = new InputTree();

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// OPERATING CONDITION:

		List<String> altitude = reader.getXMLPropertiesByPath("//altitude");
		
		NodeList nodelistFlightCondition = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//flight_condition");

		input.setAlphaCurrent(Amount.valueOf(
				Double.valueOf(nodelistFlightCondition.item(0).getTextContent()),
				NonSI.DEGREE_ANGLE)
				);

		System.out.println("\tAlpha current = " + input.getAlphaCurrent() + "\n");

	}
}

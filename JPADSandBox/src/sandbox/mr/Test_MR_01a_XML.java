package sandbox.mr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;

import configuration.MyConfiguration;
import standaloneutils.JPADXmlReader;

public class Test_MR_01a_XML {

	public static void main(String[] args) {
		System.out.println("XML read test ...");

		//--------------------------------------------------------------
		// Build up input file name
		
		String xmlFileFolderName = "in";
		String xmlFileFolderPath = 
				MyConfiguration.currentDirectoryString + File.separator
				+ xmlFileFolderName;		
		String xmlFilePath = xmlFileFolderPath + File.separator + "inputMRa.xml";

		System.out.println("-------------------------------------------------------------");
		System.out.println("[main] INPUT FILE: " + xmlFilePath);

		//--------------------------------------------------------------
		// Use file via MyXMLReader class

		JPADXmlReader xmlReader = new JPADXmlReader(xmlFilePath);

		System.out.println("[main] Now search test");
		
		List<String> propertyNames = new ArrayList<>();
		String property = "";
		String expression = "";
		
		// Simply search via getXMLPropertiesByPath
		
		// #1
		expression = "//chord_root";
		propertyNames = xmlReader.getXMLPropertiesByPath(expression);
		System.out.println("[main]\t expression: \"" + expression + "\"");
		System.out.println("[main]\t result: " + propertyNames);
		
		// #2
		expression = "//wing/chord_root";
		property = xmlReader.getXMLPropertyByPath(expression);
		System.out.println("[main]\t expression: \"" + expression + "\"");
		System.out.println("[main]\t result (1st occurrence): " + property);
		
		// #3, unit must be present
		expression = "//wing/chord_root";
		Amount<Length> chord = xmlReader.getXMLAmountWithUnitByPath(expression).to(SI.METER);
		System.out.println("[main]\t expression: \"" + expression + "\"");
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		System.out.println("[main]\t result (Amount): " + chord);

		// #4, no unit test
		expression = "//wing_0/chord_root";
		Amount<Length> chord0 = (Amount<Length>) xmlReader.getXMLAmountWithUnitByPath(expression); //.to(SI.METER);
		System.out.println("[main]\t expression: \"" + expression + "\"");
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		System.out.println("[main]\t result (Amount): " + chord0);
		
		// #5
		expression = "//wing_0/chord_root";
		Amount<Length> chord1 = xmlReader.getXMLAmountLengthByPath(expression); //.to(SI.METER);
		System.out.println("[main]\t expression: \"" + expression + "\"");
		AmountFormat.setInstance(AmountFormat.getExactDigitsInstance());
		System.out.println("[main]\t result (Amount<Length>): " + chord1);
		
	}
}
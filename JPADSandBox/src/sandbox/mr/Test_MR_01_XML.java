package sandbox.mr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import configuration.MyConfiguration;

public class Test_MR_01_XML {

	public static void main(String[] args) {

		System.out.println("XML read test ...");

		String xmlFileFolderName = "in";
		String xmlFileFolderPath = 
				MyConfiguration.currentDirectoryString + File.separator
				+ xmlFileFolderName;		
		String xmlFilePath = xmlFileFolderPath + File.separator + "inputMR.xml";

		System.out.println("--------------------------------");
		System.out.println("FILE: " + xmlFilePath);

		File xmlFile = new File(xmlFilePath);

		if (xmlFile.exists()) {
			System.out.println("File found.");

			Document xmlDoc;

			DocumentBuilder builderDoc;
			DocumentBuilderFactory factoryBuilderDoc = DocumentBuilderFactory.newInstance();

			factoryBuilderDoc.setNamespaceAware(true);
			factoryBuilderDoc.setIgnoringComments(true);

			try {

				// Prepare a builder
				builderDoc = factoryBuilderDoc.newDocumentBuilder();
				// Finally, parse the file
				xmlDoc = builderDoc.parse(xmlFilePath);

				System.out.println("File "+ xmlFilePath + " parsed.");

				System.out.println("---------------------------------------------------------");
				System.out.println("Now search test");


				// XPath-related stuff
				XPathFactory xpathFactory;
				xpathFactory = XPathFactory.newInstance();
				XPath xpath = xpathFactory.newXPath();
				
				// Search expression (String)
				// String expression = "/data/horizontal_tail/span/text()";
				// String expression = "//wing/@name";
				// String expression = "//wing/span/@unit";
				
				String expression = "//chord_root/text()";
				String expressionUnit = "//chord_root/@unit";
				
				// Compiled search expression
				XPathExpression expr = xpath.compile(expression);
				XPathExpression exprUnit = xpath.compile(expressionUnit);
				
				// Evaluate expression result on XML document
				NodeList nodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
				NodeList nodesUnit = (NodeList) exprUnit.evaluate(xmlDoc, XPathConstants.NODESET);
				
				// Initialize a string container
				List<String> listResults = new ArrayList<>();
				List<String> listResultsUnit = new ArrayList<>();
				List<Double> listValues = new ArrayList<>();
				
				for (int i = 0; i < nodes.getLength(); i++) {
					
					System.out.println("Node value #" + i + ": " + nodes.item(i).getNodeValue() + " " + nodesUnit.item(i).getNodeValue());
					
					// populate string container
					listResults.add(nodes.item(i).getNodeValue());
					listResultsUnit.add(nodesUnit.item(i).getNodeValue()); // ATTENTION! must be the same size (nodes, nodesUnit)
					
					listValues.add(Double.parseDouble(listResults.get(listResults.size() - 1)));
					
					String unit = nodesUnit.item(i).getNodeValue();
					Double val = Double.parseDouble(nodes.item(i).getNodeValue());
							
					Amount<Length> chord = (Amount<Length>) Amount.valueOf(val, Unit.valueOf(unit));
					
					System.out.println("Amount: " + chord.to(SI.METER).getEstimatedValue() + " m");
					
				}
				
				System.out.println("Result (strings): " + listResults);
				System.out.println("Result (Double): " + listValues);
				if (nodes.getLength() == 0)
					System.out.println("(No result found)");
				

			} catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
				e.printStackTrace();
			}

		} else {
			System.out.println("File not found.");
		}
	}

}

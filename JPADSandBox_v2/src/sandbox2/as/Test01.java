package sandbox2.as;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class Test01 {

	public static void main(String[] args) {

		File file = new File("src/sandbox2/as/pippo.xml");

		//		System.out.format("Full name: %s\n", file.getAbsolutePath());
		//		System.out.println("Exists? " + file.exists());

		JPADXmlReader reader = new JPADXmlReader(file.getAbsolutePath());

		List<String> names = reader.getXMLPropertiesByPath("//persons/person/name"); // XPath technology
		List<String> surnames = reader.getXMLPropertiesByPath("//persons/person/surname"); // XPath technology
		
		System.out.println("Names count: " + names.size());
		
		if (names.size() == surnames.size()) {
			for(int i = 0; i < names.size(); i++) {
				System.out.format("Person: %s %s\n", names.get(i), surnames.get(i));
			} 
		}
		
		System.out.println("-------------------------------------");

		List<String> eyeColors = new ArrayList<>();
		
		// Reading nodes in a list, then exploring single nodes
		NodeList persons = MyXMLReaderUtils.getXMLNodeListByPath(reader.getXmlDoc(), "//persons/person");
		System.out.println("Persons count: " + persons.getLength());
		for(int ip = 0; ip < persons.getLength(); ip++) {
			Node nd = persons.item(ip);
			List<String> eyeColorsInNode = MyXMLReaderUtils.getXMLAttributesByPath(nd, "//eyes/eye", "color");
			System.out.println("eyeColorsInNode: " + eyeColorsInNode.size());
			List<String> eyeTypesInNode = MyXMLReaderUtils.getXMLAttributesByPath(nd, "//eyes/eye", "type");
			System.out.println("eyeTypesInNode: " + eyeTypesInNode.size());
			if (eyeColorsInNode.size() != 0) {
				if ((eyeTypesInNode.size() == 0)) {
					// attribute not found
					eyeColors.add(eyeColorsInNode.get(0));
				} else {
					if (eyeTypesInNode.contains("RIGHT"))
						eyeColors.add("Color-Right");
				}
			} else // color not found
				eyeColors.add("Undefined");
		}
		System.out.println("Eye colors: " + eyeColors.size());
		
		System.out.println("-------------------------------------");

		if ((names.size() == surnames.size()) && (names.size() == eyeColors.size())) {
			for(int i = 0; i < names.size(); i++) {
				System.out.format("Person: %s %s (Eyes: %s)\n", names.get(i), surnames.get(i), eyeColors.get(i));
			} 
		}
		
		System.out.println("-------------------------------------");
		
	}

}

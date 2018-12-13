package sandbox2.atsi;

import java.io.File;

import javax.measure.unit.SI;

import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class ATsiTestReadXml {

	public static void main(String[] args) {

		File file = new File("src/sandbox2/atsi/pippo.xml");
		
		JPADXmlReader reader = new JPADXmlReader(file.getAbsolutePath());
		
		System.out.println(
			"Name: " + reader.getXMLPropertyByPath("person/name") +
			"Family name: " + reader.getXMLPropertyByPath("person/surname") +
			"Height (m): " + reader.getXMLAmountAngleByPath("person/height").to(SI.METER)
		);
		
		String sex = MyXMLReaderUtils
				.getXMLPropertyByPath(reader.getXmlDoc(), reader.getXpath(), "/person/sex/@value");
		
		String sex2 = reader.getXMLAttributeByPath("/person/sex","value");
		
		System.out.println("Sex: " + sex);
		System.out.println("Sex: " + sex2);
	}

}

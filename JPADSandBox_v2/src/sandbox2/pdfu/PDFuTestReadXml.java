package sandbox2.pdfu;

import java.io.File;

import javax.measure.unit.SI;

import standaloneutils.JPADXmlReader;

public class PDFuTestReadXml {

	public static void main(String[] args) {

		File file = new File("src/sandbox2/pdfu/pippo.xml");
		
		JPADXmlReader reader = new JPADXmlReader(file.getAbsolutePath());
		
		System.out.println(
			"Name: " + reader.getXMLPropertyByPath("person/name") +
			"Family name: " + reader.getXMLPropertyByPath("person/surname") +
			"Height (m): " + reader.getXMLAmountAngleByPath("person/height").to(SI.METER)
		);
	}

}

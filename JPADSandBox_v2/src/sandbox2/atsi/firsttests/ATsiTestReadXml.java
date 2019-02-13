package sandbox2.atsi.firsttests;

import java.io.File;

import javax.measure.unit.SI;

import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class ATsiTestReadXml {

	public static void main(String[] args) {

		//File file = new File("src/sandbox2/atsi/pippo.xml");
		File file = new File(args[0]);
		
		System.out.println("args[0]: " + args[0]);
		System.out.println("File name: " + file.getAbsolutePath());
		System.out.println("-------------");
		
		JPADXmlReader reader = new JPADXmlReader(file.getAbsolutePath());
		
		System.out.println(
			"Name: " + reader.getXMLPropertyByPath("person/name") + "\n" +
			"Family name: " + reader.getXMLPropertyByPath("person/surname") + "\n" +
			"Height (m): " + reader.getXMLAmountAngleByPath("person/height").getEstimatedValue()
		);
		
		String sex = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), 
						reader.getXpath(), 
						"/person/sex/@value");
		
		String sex2 = reader.getXMLAttributeByPath("/person/sex","value");
		
		System.out.println("Sex: " + sex);
		System.out.println("Sex: " + sex2);
	}

}

package sandbox2.mds;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.measure.unit.SI;

import configuration.MyConfiguration;
import standaloneutils.JPADXmlReader;
import standaloneutils.JPADXmlReader.Status;

public class Test2 {

	public static void main(String[] args) {

		MyConfiguration.customizeAmountOutput();

		File file = new File("src/sandbox2/mds/pippo1.xml");

		System.out.println("File " + file.getAbsolutePath() + " exists? " + file.exists());

		if (file.exists()) {
			JPADXmlReader reader = new JPADXmlReader(file.getAbsolutePath());

			if (reader.getStatus() == Status.PARSED_OK) {

				System.out.println(
						"Name: " + reader.getXMLPropertyByPath("persons/person/name") + "\n" +
								"Family name: " + reader.getXMLPropertyByPath("//person/surname") + "\n" +
								"Height (m): " + reader.getXMLAmountLengthByPath("//person/height").to(SI.METER)
						);

				List<String> names = reader.getXMLPropertiesByPath("persons/person/name");
				List<String> surnames = reader.getXMLPropertiesByPath("persons/person/surname");
				
				System.out.println("Names found: " + names.size());
				System.out.println("Names: " + Arrays.toString(names.toArray()));
				
				System.out.println("Surnames found: " + surnames.size());
				System.out.println("Surnames: " + Arrays.toString(surnames.toArray()));
				
			}
		}

	}


}

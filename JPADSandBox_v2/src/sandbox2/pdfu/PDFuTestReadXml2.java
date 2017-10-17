package sandbox2.pdfu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.SI;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;


class MyArgumentsPDFuTestReadXml2 {
	@Option(name = "-f", aliases = { "--file" }, required = true,
			usage = "my input file")
	private File _inputFile;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
	
}

public class PDFuTestReadXml2 {

	// declaration necessary for Concrete Object usage
	public static CmdLineParser theCmdLineParser;

	public static void main(String[] args) {

//		File file = new File("src/sandbox2/pdfu/pippo.xml");
		
		MyArgumentsPDFuTestReadXml2 va = new MyArgumentsPDFuTestReadXml2();
		PDFuTestReadXml2.theCmdLineParser = new CmdLineParser(va);

		try {
			PDFuTestReadXml2.theCmdLineParser.parseArgument(args);
			
			String pathToXML = va.getInputFile().getAbsolutePath();
			JPADXmlReader reader = new JPADXmlReader(pathToXML);

			System.out.println(
				"Pos. load factor limit: " + reader.getXMLPropertyByPath("/jpad_config/global_data/positive_limit_load_factor") + "\n" +
				"Neg. load factor limit: " + reader.getXMLPropertyByPath("/jpad_config/global_data/negative_limit_load_factor")
			);
			
			String weightsFileName = reader
					.getXMLAttributeByPath("/jpad_config/analyses/weights","file");
			System.out.println("Analysis of weights, directive file: " + weightsFileName);

		} catch (CmdLineException e) {
			e.printStackTrace();
		}
		

//		JPADXmlReader reader = new JPADXmlReader(file.getAbsolutePath());
//		System.out.println(
//			"Name: " + reader.getXMLPropertyByPath("person/name") +
//			"Family name: " + reader.getXMLPropertyByPath("person/surname") +
//			"Height (m): " + reader.getXMLAmountAngleByPath("person/height").to(SI.METER)
//		);
//		
//		String sex = MyXMLReaderUtils
//				.getXMLPropertyByPath(reader.getXmlDoc(), reader.getXpath(), "/person/sex/@value");
//		
//		String sex2 = reader.getXMLAttributeByPath("/person/sex","value");
//		
//		System.out.println("Sex: " + sex);
//		System.out.println("Sex: " + sex2);
		
	}
	
}

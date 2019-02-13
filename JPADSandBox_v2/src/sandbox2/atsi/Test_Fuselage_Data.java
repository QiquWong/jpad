package sandbox2.atsi;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.measure.unit.SI;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import configuration.enumerations.ComponentEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

// Needs library: args4j
class MyArgumentsTest_Fuselage_Data {
	@Option(name = "-f", aliases = { "--file" }, required = true,
			usage = "Aircraft file to be read")
	private File _inputFile;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();

	public File getInputFile() {
		return _inputFile;
	}
}

public class Test_Fuselage_Data {

	public static CmdLineParser theCmdLineParser;
	
	public static void main(String[] args) {
	
		MyArgumentsTest_Fuselage_Data va= new MyArgumentsTest_Fuselage_Data();
		
		Test_Fuselage_Data.theCmdLineParser = new CmdLineParser(va);
		try
		{
	     Test_Fuselage_Data.theCmdLineParser.parseArgument(args);
	     //Path to XML mi da tutto il percorso 
	     String PathToXML= va.getInputFile().getAbsolutePath();
	  
	     
	     JPADXmlReader reader =new JPADXmlReader(PathToXML);
	     
	     
	     //How to acquire the fuselge file
	     String FuselageFile =reader.getXMLAttributeByPath(
	    		 "/jpad_config/aircraft/fuselages/fuselage", 
					"file" );
	    		 
	    		 System.out.println("The file found is:" +FuselageFile);
	    
	    		 String Fuselage_Path= "in/Template_Aircraft/fuselages/"
	 					+ FuselageFile;	
	    		 
	    		 File Fuselage_ =new File(Fuselage_Path);
	    		 
	    		 Fuselage Fus = Fuselage.importFromXML(Fuselage_.getAbsolutePath());
	    		 
	    		 System.out.println(Fus /* .toString() */ );
	    		 System.out.println("N° of decks: "+Fus.getDeckNumber());
	     
		} catch (CmdLineException e) {
			e.printStackTrace();
		}
		
	}

}

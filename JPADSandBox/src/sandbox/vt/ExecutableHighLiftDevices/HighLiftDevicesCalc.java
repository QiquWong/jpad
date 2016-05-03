package sandbox.vt.ExecutableHighLiftDevices;

import java.util.List;
import javax.measure.unit.NonSI;
import javax.xml.parsers.ParserConfigurationException;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.NodeList;
import configuration.enumerations.FlapTypeEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class HighLiftDevicesCalc {

	/**************************************************************************************
	 * This method is in charge of reading data from a given XML input file and 
	 * put them inside an object of the InputTree class.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param pathToXML
	 * @throws ParserConfigurationException
	 */
	public static void importFromXML(String pathToXML) throws ParserConfigurationException {

		InputTree input = new InputTree();

		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading input file data ...\n");

		//---------------------------------------------------------------------------------
		// FLIGHT CONDITION:

		NodeList nodelistFlightCondition = MyXMLReaderUtils
				.getXMLNodeListByPath(reader.getXmlDoc(), "//flight_condition");

		input.setAlphaCurrent(Amount.valueOf(
				Double.valueOf(nodelistFlightCondition.item(0).getTextContent()),
				NonSI.DEGREE_ANGLE)
				);

		System.out.println("\tAlpha current = " + input.getAlphaCurrent() + "\n");

		//---------------------------------------------------------------------------------
		// WING:	

		// TODO: Complete me!!

		//---------------------------------------------------------------------------------
		// FLAPS:

		importFlapFromXML(input, reader);

		System.out.println("\tFlaps Types = " +  input.getFlapType());
		System.out.println("\tFlaps chord ratios = " +  input.getCfc());
		System.out.println("\tFlaps deflections = " +  input.getDeltaFlap());
		System.out.println("\tFlaps inboard station = " +  input.getEtaInFlap());
		System.out.println("\tFlaps outboard station = " +  input.getEtaOutFlap() + "\n");
		
		//---------------------------------------------------------------------------------
		// SLATS:

		importSlatFromXML(input, reader);
		
		System.out.println("\tSlats deflections = " +  input.getDeltaSlat());
		System.out.println("\tSlats chord ratios = " +  input.getCsc());
		System.out.println("\tSlats extension ratios = " +  input.getcExtCSlat());
		System.out.println("\tSlats inboard stations = " +  input.getEtaInSlat());
		System.out.println("\tSlats outboard stations = " +  input.getEtaOutSlat() + "\n");
	}

	/***************************************************************************************
	 * This method reads all flaps data using a fixed structure for the XML tag. Each tag is
	 * repeated for every flap in order to simplify the collection of data of the same type.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param input
	 * @param reader
	 * @throws ParserConfigurationException
	 */
	private static void importFlapFromXML(InputTree input, JPADXmlReader reader) throws ParserConfigurationException {

		List<String> flapTypeProperty = reader.getXMLPropertiesByPath("//flap_type");
		// Recognizing flap type
		for(int i=0; i<flapTypeProperty.size(); i++) {
			if(flapTypeProperty.get(i).equals("SINGLE_SLOTTED"))
				input.getFlapType().add(FlapTypeEnum.SINGLE_SLOTTED);
			else if(flapTypeProperty.get(i).equals("DOUBLE_SLOTTED"))
				input.getFlapType().add(FlapTypeEnum.DOUBLE_SLOTTED);
			else if(flapTypeProperty.get(i).equals("PLAIN"))
				input.getFlapType().add(FlapTypeEnum.PLAIN);
			else if(flapTypeProperty.get(i).equals("FOWLER"))
				input.getFlapType().add(FlapTypeEnum.FOWLER);
			else if(flapTypeProperty.get(i).equals("TRIPLE_SLOTTED"))
				input.getFlapType().add(FlapTypeEnum.TRIPLE_SLOTTED);
			else {
				System.err.println("NO VALID FLAP TYPE!!");
				return;
			}
		}
		
		List<String> cfcProperty = reader.getXMLPropertiesByPath("//cf_c");
		for(int i=0; i<cfcProperty.size(); i++)
			input.getCfc().add(Double.valueOf(cfcProperty.get(i)));
		
		List<String> deltaFlapProperty = reader.getXMLPropertiesByPath("//delta_flap");
		for(int i=0; i<deltaFlapProperty.size(); i++)
			input.getDeltaFlap().add(Double.valueOf(deltaFlapProperty.get(i)));
		
		List<String> etaInFlapProperty = reader.getXMLPropertiesByPath("//flap_inboard_station");
		for(int i=0; i<etaInFlapProperty.size(); i++)
			input.getEtaInFlap().add(Double.valueOf(etaInFlapProperty.get(i)));
		
		List<String> etaOutFlapProperty = reader.getXMLPropertiesByPath("//flap_outboard_station");
		for(int i=0; i<etaOutFlapProperty.size(); i++)
			input.getEtaOutFlap().add(Double.valueOf(etaOutFlapProperty.get(i)));
		
	}
	
	/******************************************************************************************
	 * This method reads all slats data using a fixed structure for the XML tag. Each tag is
	 * repeated for every slat in order to simplify the collection of data of the same type.
	 * 
	 * @author Vittorio Trifari
	 * 
	 * @param input
	 * @param reader
	 */
	private static void importSlatFromXML(InputTree input, JPADXmlReader reader) {
		
		List<String> delta_slat_property = reader.getXMLPropertiesByPath("//delta_slat");
		for(int i=0; i<delta_slat_property.size(); i++)
			input.getDeltaSlat().add(Double.valueOf(delta_slat_property.get(i)));
		
		List<String> cs_c_property = reader.getXMLPropertiesByPath("//cs_c");
		for(int i=0; i<cs_c_property.size(); i++)
			input.getCsc().add(Double.valueOf(cs_c_property.get(i)));
		
		List<String> cExt_c_slat_property = reader.getXMLPropertiesByPath("//cExt_c");
		for(int i=0; i<cExt_c_slat_property.size(); i++)
			input.getcExtCSlat().add(Double.valueOf(cExt_c_slat_property.get(i)));
		
		List<String> eta_in_slat_property = reader.getXMLPropertiesByPath("//slat_inboard_station");
		for(int i=0; i<eta_in_slat_property.size(); i++)
			input.getEtaInSlat().add(Double.valueOf(eta_in_slat_property.get(i)));
		
		List<String> eta_out_slat_property = reader.getXMLPropertiesByPath("//slat_outboard_station");
		for(int i=0; i<eta_out_slat_property.size(); i++)
			input.getEtaOutSlat().add(Double.valueOf(eta_out_slat_property.get(i)));

	}
	
}
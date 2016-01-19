package standaloneutils.database.io;

import java.util.ArrayList;
import java.util.List;

import javax.measure.unit.Unit;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.util.SystemOutLogger;
import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import standaloneutils.MyXMLReaderUtils;

public class DatabaseFileReader<E extends Enum<E>> {

	private String filenameWithPathAndExt;
	
	public String getFilenameWithPathAndExt() {
		return filenameWithPathAndExt;
	}
	
	private List<E> tagList = new ArrayList<E>();
	// private List<VeDSCDatabaseEnum> tagList = new ArrayList<VeDSCDatabaseEnum>();
	private List<Amount> valueList = new ArrayList<Amount>();

	public DatabaseFileReader(String filenameWithPathAndExt, List<E> tagList) {
		this.filenameWithPathAndExt = filenameWithPathAndExt;
		this.tagList = tagList;
	}
	//	public DatabaseFileReader(String filenameWithPathAndExt, List<VeDSCDatabaseEnum> tagList) {
	//		this.filenameWithPathAndExt = filenameWithPathAndExt;
	//		this.tagList = tagList;
	//	}
	

	public List<Amount> readDatabase() {
		return readDatabase(filenameWithPathAndExt, tagList);
	}

	public static <E extends Enum<E>> List<Amount> readDatabase(String filenameWithPathAndExt, List<E> tagList) {

		String unitStr = "";
		NodeList childNodes;
		List<Amount> valueList = new ArrayList<Amount>();
		Document doc = MyXMLReaderUtils.importDocument(filenameWithPathAndExt);

		//		System.out.println("tagList.size is : "+tagList.size());

		for (int i=0; i<tagList.size(); i++) {
			//			System.out.println("Tag: "+tagList.get(i).name() );

			childNodes = doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes();
//						System.out.println("\t"+doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes());
//						System.out.println("Childs number: "+ childNodes.getLength());

			for (int j=0; j<childNodes.getLength(); j++) {
				//				System.out.println("Childs node name: " + childNodes.item(j).getNodeName());
				if(childNodes.item(j).getNodeName().equalsIgnoreCase("value")) {

					if (childNodes.item(j).hasAttributes()) {
						unitStr = childNodes.item(j).getAttributes().getNamedItem("unit").getNodeValue();
						//					System.out.println(childNodes.item(j).getNodeName() + " " + childNodes.item(j).getTextContent() + " " + unitStr);

						if (!unitStr.contains("/")) {
							valueList.add(i, 
									Amount.valueOf(Double.parseDouble(childNodes.item(j).getTextContent()), 
											Unit.valueOf(unitStr)));
							break;

						} else { 
							valueList.add(i, 
									Amount.valueOf(Double.parseDouble(childNodes.item(j).getTextContent()), 
											( Unit.valueOf(unitStr.substring(unitStr.indexOf("/") + 1))).inverse() ));
							break;
						}

					} else {
						valueList.add(i, 
								Amount.valueOf(Double.parseDouble(childNodes.item(j).getTextContent()), 
										Unit.ONE));
					}
				} 
			}
		}

		return valueList;
	}
	
	
	
//	/**
//	 * @author Vincenzo Cusati
//	 * @return
//	 */
//	public List<Object> readValueXML() {
//		return readValueXML(filenameWithPathAndExt, tagList);
//	}
//	
//	/**
//	 * @author Vincenzo Cusati
//	 * @param filenameWithPathAndExt
//	 * @param tagList
//	 * @return
//	 */
//	
//	public static <E extends Enum<E>> List<Object> readValueXML(String filenameWithPathAndExt, List<E> tagList) {
//
//		String unitStr = "";
//		NodeList childNodes;
//		List<Object> objValueList = new ArrayList<Object>();
//		Document doc = MyXMLReaderUtils.importDocument(filenameWithPathAndExt);
//
////				System.out.println("tagList.size is : "+tagList.size());
//
//		for (int i=0; i<tagList.size(); i++) {
////						System.out.println("Tag: "+tagList.get(i).name() );
//
//			childNodes = doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes();
////						System.out.println("\t"+doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes());
////						System.out.println("\t Childs number: "+ childNodes.getLength());
//
//			for (int j=0; j<childNodes.getLength(); j++) {
////								System.out.println("\t Childs node name: " + childNodes.item(j).getNodeName());
//				if(childNodes.item(j).getNodeName().equalsIgnoreCase("value")) {
//
//						unitStr = childNodes.item(j).getAttributes().getNamedItem("unit").getNodeValue();
////							System.out.println(childNodes.item(j).getNodeName() + " unitStr: " + childNodes.item(j).getTextContent() + " " + unitStr);
//
//						if (!unitStr.contains("/")) {
//							objValueList.add(i, childNodes.item(j).getTextContent());
//						}
//
//					} 
//				} 
//			}
//
//		return objValueList;
//	}
	


	public List<E> getTagList() {
		return tagList;
	}


	public List<Amount> getValueList() {
		return valueList;
	}

}

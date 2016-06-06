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

public class InputFileReader<E extends Enum<E>> {

	private String filenameWithPathAndExt;
	
	public String getFilenameWithPathAndExt() {
		return filenameWithPathAndExt;
	}
	
	private List<E> tagList = new ArrayList<E>();
	// private List<VeDSCDatabaseEnum> tagList = new ArrayList<VeDSCDatabaseEnum>();
	private List<Amount> valueList = new ArrayList<Amount>();

	public InputFileReader(String filenameWithPathAndExt, List<E> tagList) {
		this.filenameWithPathAndExt = filenameWithPathAndExt;
		this.tagList = tagList;
	}
	//	public DatabaseFileReader(String filenameWithPathAndExt, List<VeDSCDatabaseEnum> tagList) {
	//		this.filenameWithPathAndExt = filenameWithPathAndExt;
	//		this.tagList = tagList;
	//	}
	
	/********************************************************************************************
	 * 
	 * @return
	 */
	public List<Amount> readAmounts() {
		return readAmounts(filenameWithPathAndExt, tagList);
	}

	/********************************************************************************************
	 * 
	 * @param filenameWithPathAndExt
	 * @param tagList
	 * @return
	 */
	public static <E extends Enum<E>> List<Amount> readAmounts(String filenameWithPathAndExt, List<E> tagList) {

		String unitStr = "";
		NodeList childNodes;
		List<Amount> valueList = new ArrayList<Amount>();
		Document doc = MyXMLReaderUtils.importDocument(filenameWithPathAndExt);

				System.out.println("tagList.size is : "+tagList.size());

		for (int i=0; i<tagList.size(); i++) {
						System.out.println("Tag: "+tagList.get(i).name() );

			childNodes = doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes();
						System.out.println("\t"+doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes());
						System.out.println("Childs number: "+ childNodes.getLength());

			for (int j=0; j<childNodes.getLength(); j++) {
								System.out.println("Childs node name: " + childNodes.item(j).getNodeName());
				if(childNodes.item(j).getNodeName().equalsIgnoreCase("value")) {

					if (childNodes.item(j).hasAttributes()) {
						unitStr = childNodes.item(j).getAttributes().getNamedItem("unit").getNodeValue();
											System.out.println(childNodes.item(j).getNodeName() + " " + childNodes.item(j).getTextContent() + " " + unitStr);

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
	
	/********************************************************************************************
	 * @author Vittorio Trifari
	 * @return
	 */
	public List<String> readStrings() {
		return readStrings(filenameWithPathAndExt, tagList);
	}
	
	/********************************************************************************************
	 * @author Vittorio Trifari
	 * 
	 * @param filenameWithPathAndExt
	 * @param tagList
	 * @return
	 */
	public static <E extends Enum<E>> List<String> readStrings(String filenameWithPathAndExt, List<E> tagList) {
		
		NodeList childNodes;
		List<String> stringList = new ArrayList<String>();
		Document doc = MyXMLReaderUtils.importDocument(filenameWithPathAndExt);
		
		System.out.println("tagList.size is : "+tagList.size());
		
		for (int i=0; i<tagList.size(); i++) {
			
			System.out.println("Tag: "+tagList.get(i).name() );
			childNodes = doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes();
			
			System.out.println("\t"+doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes());
			System.out.println("Childs number: "+ childNodes.getLength());
			
			for (int j=0; j<childNodes.getLength(); j++) {
			
				System.out.println("Childs node name: " + childNodes.item(j).getNodeName());
				if(childNodes.item(j).getNodeName().equalsIgnoreCase("value")) {
					
					stringList.add(childNodes.item(j).getTextContent());
					
				}
			}
		}
		
		return stringList;
	}
	
	/********************************************************************************************
	 * @author Vittorio Trifari
	 * 
	 * @return
	 */
	public List<List<String>> readStringLists() {
		return readStringLists(filenameWithPathAndExt, tagList);
	}
	
	/********************************************************************************************
	 * @author Vittorio Trifari
	 * 
	 * @param filenameWithPathAndExt
	 * @param tagList
	 * @return
	 */
	public static <E extends Enum<E>> List<List<String>> readStringLists(String filenameWithPathAndExt, List<E> tagList) {
		
		NodeList childNodes;
		List<List<String>> stringListList = new ArrayList<List<String>>();
		List<String> stringListTemp = new ArrayList<String>();
		Document doc = MyXMLReaderUtils.importDocument(filenameWithPathAndExt);
		
		System.out.println("tagList.size is : "+tagList.size());
		
		for (int i=0; i<tagList.size(); i++) {
			
			stringListTemp.clear();
			
			System.out.println("Tag: "+tagList.get(i).name() );
			childNodes = doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes();
			
			System.out.println("\t"+doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes());
			System.out.println("Childs number: "+ childNodes.getLength());
			
			for (int j=0; j<childNodes.getLength(); j++) {
			
				if(childNodes.item(j).getNodeName().equalsIgnoreCase("value")) {
					
					System.out.println("Childs node name: " + childNodes.item(j).getNodeName());
					stringListTemp.add(childNodes.item(j).getTextContent());
				}
			}
			
			stringListList.add(i, stringListTemp);
		}
		
		return stringListList;
	}
	
	/********************************************************************************************
	 * @author Vittorio Trifari
	 * 
	 * @return
	 */
	public List<Double[]> readDoubleLists() {
		return readDoubleLists(filenameWithPathAndExt, tagList);
	}
	
	/********************************************************************************************
	 * @author Vittorio Trifari
	 * 
	 * @param filenameWithPathAndExt
	 * @param tagList
	 * @return
	 */
	public static <E extends Enum<E>> List<Double[]> readDoubleLists(String filenameWithPathAndExt, List<E> tagList) {
		
		NodeList childNodes;
		List<Double[]> doubleVecList = new ArrayList<>();
		List<String> stringListTemp = new ArrayList<String>();
		List<Double> valueTempList = new ArrayList<Double>();
		Document doc = MyXMLReaderUtils.importDocument(filenameWithPathAndExt);
		
		System.out.println("tagList.size is : "+tagList.size());
		
		for (int i=0; i<tagList.size(); i++) {
			
			stringListTemp.clear();
			valueTempList.clear();
			
			System.out.println("Tag: "+tagList.get(i).name() );
			childNodes = doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes();
			
			System.out.println("\t"+doc.getElementsByTagName(tagList.get(i).name()).item(0).getChildNodes());
			System.out.println("Childs number: "+ childNodes.getLength());
			
			for (int j=0; j<childNodes.getLength(); j++) {
			
				if(childNodes.item(j).getNodeName().equalsIgnoreCase("value")) {
					
					System.out.println("Childs node name: " + childNodes.item(j).getNodeName());
					stringListTemp.add(childNodes.item(j).getTextContent());
											
					};
			}
			
			Double[] tempValue = new Double[stringListTemp.size()];
			
			for(int k=0; k<tempValue.length; k++){
				
				tempValue[k] = Double.valueOf(stringListTemp.get(k));
			}
			
			doubleVecList.add(i, tempValue);
		}
		
		return doubleVecList;
	}
	
//	/********************************************************************************************
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

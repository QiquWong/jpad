package standaloneutils.database.io;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import writers.JPADStaticWriteUtils;


public class DatabaseFileWriter <E extends Enum<E>> {

	private List<E> tagListInput = new ArrayList<E>();
	private List<Amount> valueListInput = new ArrayList<Amount>();
	private List<String> descriptionListInput = new ArrayList<String>();
	private List<E> tagListOutput = new ArrayList<E>();
	private List<Amount> valueListOutput = new ArrayList<Amount>();
	private List<String> descriptionListOutput = new ArrayList<String>();
	
	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder;
	private Document doc;
	private Element _rootElement, _inputElement, _outputElement;
	private String filenameWithPathAndExt;

	public DatabaseFileWriter(String databaseName, String filenameWithPathAndExt, 
			DatabaseIOmanager<E> inputManager, DatabaseIOmanager<E> outputManager) {
		initializeDocument(databaseName, filenameWithPathAndExt);
		setInputLists(inputManager.getTagList(), inputManager.getValueList(), inputManager.getDescriptionList());
		setOutputLists(outputManager.getTagList(), outputManager.getValueList(), outputManager.getDescriptionList());
	}
	
	public DatabaseFileWriter(String databaseName, String filenameWithPathAndExt, 
			DatabaseIOmanager<E> inputManager) {
		initializeDocument(databaseName, filenameWithPathAndExt);
		setInputLists(inputManager.getTagList(), inputManager.getValueList(), inputManager.getDescriptionList());
	}
	
	private void initializeDocument(String databaseName, String filenameWithPathAndExt) {

		this.filenameWithPathAndExt = filenameWithPathAndExt;
		docFactory = DocumentBuilderFactory.newInstance();

		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// Create new document object
		doc = docBuilder.newDocument();
		
		// Create a new element in the document object
		_rootElement = doc.createElement(databaseName);
		_inputElement = doc.createElement("Input");
		_outputElement = doc.createElement("Output");
		
		/* Append each element to the current document object
		 * The document will appear like:
		 * <_rootElement>
		 *   <_inputElement>
		 *   ...
		 *   </_inputElement>
		 *   <_outputElement>
		 *   ...
		 *   </_outputElement>
		 * </_rootElement>
		 */
		doc.appendChild(_rootElement);
		_rootElement.appendChild(_inputElement);
		_rootElement.appendChild(_outputElement);
	}

	public void writeDocument() {
		writeInput(_inputElement);
		writeOutput(_outputElement);
	}
	
	public void writeInput(Element father) {
		JPADStaticWriteUtils.writeDatabaseNode(filenameWithPathAndExt, doc, father, 
				tagListInput, valueListInput, descriptionListInput,
				DatabaseIOmanager.value, DatabaseIOmanager.description);
	}
	
	public void writeOutput(Element father) {
		JPADStaticWriteUtils.writeDatabaseNode(filenameWithPathAndExt, doc, father, 
				tagListOutput, valueListOutput, descriptionListOutput,
				DatabaseIOmanager.value, DatabaseIOmanager.description);
	}

	public void setInputLists(List<E> tagList, List<Amount> list, List<String> descriptionList) {
		this.tagListInput = tagList;
		this.valueListInput = list;
		this.descriptionListInput = descriptionList;
	}
	
	public void setOutputLists(List<E> tagList, List<Amount> list, List<String> descriptionList) {
		this.tagListOutput = tagList;
		this.valueListOutput = list;
		this.descriptionListOutput = descriptionList;
	}

}

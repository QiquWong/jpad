package standaloneutils.cpacs;

import java.io.File;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import de.dlr.sc.tigl.CpacsConfiguration;
import javaslang.Tuple;
import writers.JPADStaticWriteUtils;

public class CPACSWriter {

	public static enum Status {
		OK,
		ERROR;
	}

	/**
	 * Central logger instance.
	 */
	protected static final Log LOGGER = LogFactory.getLog(CpacsConfiguration.class);	

	private Document _exportDoc;
	private File _cpacsFile;
	CpacsConfiguration _config;
	
	private Status _status = null;

	/**
	 * @param filePath
	 */
	public CPACSWriter(File file) {
		this._cpacsFile = file;
		if (this._cpacsFile.exists())
			System.out.println("[CPACSWriter] file " + this._cpacsFile.getAbsolutePath() + "already exists. It'll be ovewritten.");
		else
			System.out.println("[CPACSWriter] file " + this._cpacsFile.getAbsolutePath() + " will be created and written.");
	}
	
	public void setOutputFile(String filePath) {
		this._cpacsFile = new File(filePath);
		if (this._cpacsFile.exists())
			System.out.println("[CPACSWriter] file " + this._cpacsFile.getAbsolutePath() + "already exists. It'll be ovewritten.");
		else
			System.out.println("[CPACSWriter] file " + this._cpacsFile.getAbsolutePath() + " will be created and written.");
	}

	public void export(Object obj) throws ParserConfigurationException {

		if (this._cpacsFile.exists())
			System.out.println("[CPACSWriter.export] over writing file " + this._cpacsFile.getAbsolutePath() + " ...");
		else
			System.out.println("[CPACSWriter.export] creating file " + this._cpacsFile.getAbsolutePath() + " ...");

		// Create the skeleton of a CPACS file
		Document cpacsDoc = createSkeletonDoc();
		
		// Determine the kind of object to write, and where to write it
		if (obj instanceof Aircraft) {

		}

		if (obj instanceof Fuselage) {

		}
		
		if (obj instanceof LiftingSurface) {

		}
		
		JPADStaticWriteUtils.writeDocumentToXml(cpacsDoc, this._cpacsFile);


	}
	
	Document createSkeletonDoc() throws ParserConfigurationException {
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document cpacsDoc = null;
		docBuilder = docFactory.newDocumentBuilder();
		cpacsDoc = docBuilder.newDocument();
		
		org.w3c.dom.Element cpacsElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				cpacsDoc,"cpacs",
				Tuple.of("xsi:noNamespaceSchemaLocation", "CPACS_21_Schema.xsd"),
				Tuple.of("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
				);

		org.w3c.dom.Element headerElement = JPADStaticWriteUtils.createXMLElementWithAttributes(
				cpacsDoc,"header",
				Tuple.of("xsi:type","headerType")
				);
		// header.name
		headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(cpacsDoc, "name", "JPAD.CPACSWrite - Test")
		);
		// header.description
		headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(cpacsDoc, "description", "JPAD Aircraft converted to CPACS format")
		);
		// header.creator
		headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(cpacsDoc, "creator", "JPAD")
		);
		// header.timestamp
		Date date = new java.util.Date();
		headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(cpacsDoc, "timestamp", date.toString())
		);
		// header.version
		headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(cpacsDoc, "version", "0.1")
		);
		// header.cpacsversion
		headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(cpacsDoc, "cpacsversion", "3.0")
		);
		
		cpacsElement.appendChild(headerElement);
		
		
		cpacsDoc.appendChild(cpacsElement);
		
		return cpacsDoc;
	}
	

}

package standaloneutils.cpacs;

import java.io.File;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import javaslang.Tuple;
import writers.JPADStaticWriteUtils;

public class CPACSWriter {

	/**
	 * Central logger instance.
	 */
	protected static final Log LOGGER = LogFactory.getLog(CPACSWriter.class);	

	private File _cpacsFile;
	private org.w3c.dom.Document _cpacsDoc;
	private org.w3c.dom.Element _cpacsElement;
	private org.w3c.dom.Element _headerElement;
	private org.w3c.dom.Element _vehiclesElement;
	private org.w3c.dom.Element _aircraftElement;
	private org.w3c.dom.Element _modelElement;
	private org.w3c.dom.Element _fuselagesElement;
	private org.w3c.dom.Element _wingsElement;
	private org.w3c.dom.Element _enginesElement;
	private org.w3c.dom.Element _profilesElement;
	private org.w3c.dom.Element _fuselageProfilesElement;
	private org.w3c.dom.Element _wingAirfoilsElement;
	private org.w3c.dom.Element _toolspecificElement;

	/**
	 * @param filePath
	 * @throws ParserConfigurationException 
	 */
	public CPACSWriter(File file) throws ParserConfigurationException {
		reset(file);
	}
	
	public void setOutputFile(String filePath) {
		_cpacsFile = new File(filePath);
		if (_cpacsFile.exists())
			LOGGER.info("[setOutputFile] file " + this._cpacsFile.getAbsolutePath() + " already exists. It'll be ovewritten.");
		else
			LOGGER.info("[setOutputFile] file " + this._cpacsFile.getAbsolutePath() + " will be created and written.");
	}

	public void setOutputFile(File file) {
		if (file == null) {
			LOGGER.warn("[setOutputFile] could not set the output file. Pass a non-null value!");
		} else {
			_cpacsFile = file;
			if (_cpacsFile.exists())
				LOGGER.info("[setOutputFile] file " + this._cpacsFile.getAbsolutePath() + " already exists. It'll be ovewritten.");
			else
				LOGGER.info("[setOutputFile] file " + this._cpacsFile.getAbsolutePath() + " will be created and written.");
		}
	}
	
	public void reset() {
		_cpacsFile                     = null;
		_cpacsDoc                      = null;
		_cpacsElement                  = null;
		_headerElement                 = null;
		_vehiclesElement               = null;
		_aircraftElement               = null;
		_modelElement                  = null;
		_fuselagesElement              = null;
		_wingsElement                  = null;
		_enginesElement                = null;
		_profilesElement               = null;
		_fuselageProfilesElement       = null;
		_wingAirfoilsElement           = null;
		_toolspecificElement           = null;		
	}

	public void reset(File file) throws ParserConfigurationException {
		reset();
		setOutputFile(file);
		createSkeletonDoc();
	}

	public void reset(String filePath) throws ParserConfigurationException {
		reset();
		setOutputFile(filePath);
		createSkeletonDoc();
	}
	
	public void export(Object obj) throws ParserConfigurationException {

		if (this._cpacsFile == null) { // do nothing, warn the user
			LOGGER.warn("[export] could not write on file. Make sure you assigned an output file.");
			return;
		}
		
		if (this._cpacsFile.exists())
		    LOGGER.info("[export] overwriting file " + this._cpacsFile.getAbsolutePath() + " ...");		
		else
		    LOGGER.info("[export] creating file " + this._cpacsFile.getAbsolutePath() + " ...");		

		// Create the skeleton of a CPACS file. If not null, do nothing, use the current _cpacsDoc 
		createSkeletonDoc();
		
		// Determine the kind of object to write, and where to write it
		
		if (obj instanceof Aircraft) { // append to cpacs.vehicles.aircraft.model
			insertAircraft((Aircraft)obj);
		}

		if (obj instanceof Fuselage) { // append to cpacs.vehicles.aircraft.model.fuselages
			insertFuselage((Fuselage)obj);
		}
		
		if (obj instanceof LiftingSurface) { // append to cpacs.vehicles.aircraft.model.wings
			insertLiftingSurface((LiftingSurface)obj);
		}
		
		JPADStaticWriteUtils.writeDocumentToXml(this._cpacsDoc, this._cpacsFile);

	}
	
	void createSkeletonDoc() throws ParserConfigurationException {
		
		if (_cpacsDoc != null) return; // do nothing, use the old one
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		docBuilder = docFactory.newDocumentBuilder();
		_cpacsDoc = docBuilder.newDocument();
		
		_cpacsElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "cpacs",
				Tuple.of("xsi:noNamespaceSchemaLocation", "CPACS_21_Schema.xsd"),
				Tuple.of("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
				);

		_headerElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "header",
				Tuple.of("xsi:type","headerType")
				);
		// header.name
		_headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "name", "JPAD.CPACSWrite - Test")
		);
		// header.description
		_headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "description", "JPAD Aircraft converted to CPACS format")
		);
		// header.creator
		_headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "creator", "JPAD")
		);
		// header.timestamp
		Date date = new java.util.Date();
		_headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "timestamp", date.toString())
		);
		// header.version
		_headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "version", "0.1")
		);
		// header.cpacsversion
		_headerElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "cpacsversion", "3.0")
		);
		
		_cpacsElement.appendChild(_headerElement);  // cpacs <-- header

		_vehiclesElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"vehicles");

		_aircraftElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"aircraft");

		_modelElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"model",
				Tuple.of("uID", "ID_CPACSWRITE_TEST"), // TODO: make it a parameter coming from aircraft object
				Tuple.of("xsi:type", "aircraftModelType")
				);
		// vehicles.aircraft.model.name
		_modelElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "name", "[REPLACE: vehicles.aircraft.model.name]")
		);
		// vehicles.aircraft.model.description
		_modelElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "description", "[REPLACE: vehicles.aircraft.model.description]")
		);

		_fuselagesElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"fuselages");

		_wingsElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"wings");

		_enginesElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"engines");
		
		_modelElement.appendChild(_fuselagesElement); // cpacs.vehicles.aircraft.model <-- fuselages
		_modelElement.appendChild(_wingsElement);     // cpacs.vehicles.aircraft.model <-- wings
		_modelElement.appendChild(_enginesElement);   // cpacs.vehicles.aircraft.model <-- engines
		
		_aircraftElement.appendChild(_modelElement); // cpacs.vehicles.aircraft <-- model
		
		_vehiclesElement.appendChild(_aircraftElement); // cpacs.vehicles <-- model.aircraft

		_profilesElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"profiles");
		
		_fuselageProfilesElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"fuselageProfiles");

		_wingAirfoilsElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"wingAirfoils");
		
		_profilesElement.appendChild(_fuselageProfilesElement); // cpacs.vehicles.profiles <-- vehicles.profiles.fuselageProfiles
		_profilesElement.appendChild(_wingAirfoilsElement);     // cpacs.vehicles.profiles <-- vehicles.profiles.wingAirfoils
		
		_vehiclesElement.appendChild(_profilesElement); // cpacs.vehicles <-- model.aircraft
		
		_cpacsElement.appendChild(_vehiclesElement); // cpacs <-- vehicles
		
		_toolspecificElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc,"toolspecific");
		
		_cpacsElement.appendChild(_toolspecificElement); // cpacs <-- toolspecific

		// finally make the skeleton-tree a document 
		this._cpacsDoc.appendChild(_cpacsElement);
	}
	
	public void insertAircraft(Aircraft aircraft) {
		if (aircraft == null) {
			LOGGER.warn("[insertAircraft] trying to insert a null aircraft. Returning.");
			return;
		}
		if (_cpacsDoc == null) {
			LOGGER.warn("[insertAircraft] could not insert a aircraft in a null Document object. Returning.");
			return;
		}
		
		LOGGER.info("[insertAircraft] inserting liftingSurface into CPACS tree ...");
		
		LOGGER.warn("[insertAircraft] to be implemented. Nothing done. Returning.");
		
		// TODO: scan the aircraft components and call insertFuselage, insertLiftingSurface accordingly
		
		// TODO: implement similar functions for other components: e.g. nacelles, etc
		
		// TODO: implement similar functions to populate engines' thrust data and toolspecific aero data
		
	}
	
	public void insertFuselage(Fuselage fuselage) {
		
		if (fuselage == null) {
			LOGGER.warn("[insertFuselage] trying to insert a null fuselage. Returning.");
			return;
		}
		if (_cpacsDoc == null) {
			LOGGER.warn("[insertFuselage] could not insert a fuselage in a null Document object. Returning.");
			return;
		}
		if (_fuselagesElement == null) {
			LOGGER.warn("[insertFuselage] could not insert a fuselage in a null cpacs.vehicles.aircraft.model.fuselages element. Returning.");
			return;
		}		
		if (_fuselageProfilesElement == null) {
			LOGGER.warn("[insertFuselage] could not insert a fuselage profiles in a null cpacs.vehicles.aircraft.profiles.fuselageProfiles element. Returning.");
			return;
		}
		
		LOGGER.info("[insertFuselage] inserting fuselage into CPACS tree ...");
		
		org.w3c.dom.Element fuselageElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "fuselage");
		
		// fuselage.name
		fuselageElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "name", fuselage.getId())
		);
		// fuselage.description
		fuselageElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "description", "A fuselage created with JPAD")
		);
		// fuselage.transformation
		org.w3c.dom.Element  transformationElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "transformation",
				Tuple.of("xsi:type", "transformationType")
				);
		org.w3c.dom.Element scalingElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "scaling",
				Tuple.of("xsi:type","pointType")
				);
		// fuselage.transformation.scaling.x .y .z
		scalingElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "x", "1.0")
		);
		scalingElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "y", "1.0")
		);
		scalingElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "z", "1.0")
		);

		// fuselage.rotation
		org.w3c.dom.Element rotationElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "rotation",
				Tuple.of("xsi:type","pointType")
				);
		// fuselage.rotation.x .y .z
		rotationElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "x", "0.0")
		);
		rotationElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "y", "0.0")
		);
		rotationElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "z", "0.0")
		);

		// fuselage.translation
		org.w3c.dom.Element translationElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "translation",
				Tuple.of("refType","absGlobal"),
				Tuple.of("xsi:type","pointAbsRelType")
				);
		// fuselage.translation.x .y .z
		translationElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "x", "0.0")
		);
		translationElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "y", "0.0")
		);
		translationElement.appendChild(
				 JPADStaticWriteUtils.createXMLElementWithValue(_cpacsDoc, "z", "0.0")
		);
		
		transformationElement.appendChild(scalingElement);     // transformation <-- scaling
		transformationElement.appendChild(rotationElement);    // transformation <-- rotation
		transformationElement.appendChild(translationElement); // transformation <-- translation
		
		fuselageElement.appendChild(transformationElement); // fuselage <-- transformation
		
		// TODO
		
		// sections
		// sections.section ...
		// cpacs.vehicles.aircraft.profiles.fuselageProfiles
		// cpacs.vehicles.aircraft.profiles.fuselageProfiles.fuselageProfile ...

		// FINALLY: append the single fuselage data to the list of fuselages --- Mind the final "s" 
		_fuselagesElement.appendChild(fuselageElement); // cpacs.vehicles.aircraft.model.fuselages <-- fuselage
		
	}

	public void insertLiftingSurface(LiftingSurface liftingSurface) {
		if (liftingSurface == null) {
			LOGGER.warn("[insertLiftingSurface] trying to insert a null liftingSurface. Returning.");
			return;
		}
		if (_cpacsDoc == null) {
			LOGGER.warn("[insertLiftingSurface] could not insert a liftingSurface in a null Document object. Returning.");
			return;
		}
		if (_wingsElement == null) {
			LOGGER.warn("[insertLiftingSurface] could not insert a liftingSurface in a null cpacs.vehicles.aircraft.model.wings element. Returning.");
			return;
		}		
		if (_wingAirfoilsElement == null) {
			LOGGER.warn("[insertLiftingSurface] could not insert liftingSurface airfoils in a null cpacs.vehicles.aircraft.profiles._wingAirfoilsElement element. Returning.");
			return;
		}
		
		LOGGER.info("[insertLiftingSurface] inserting liftingSurface into CPACS tree ...");
		
		LOGGER.warn("[insertLiftingSurface] to be implemented. Nothing done. Returning.");
		
		// TODO
	}
	
}

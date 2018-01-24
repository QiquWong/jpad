package standaloneutils.cpacs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscience.physics.amount.Amount;

import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import javaslang.Tuple;
import processing.core.PVector;
import standaloneutils.MyArrayUtils;
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
		
		// fuselage.translation
		org.w3c.dom.Element sectionsElement = JPADStaticWriteUtils.createXMLElementWithAttributes(_cpacsDoc, "sections",
				Tuple.of("xsi:type","fuselageSectionsType")
				);
		
		// TODO
		// EXPERIMENTAL
		
		// sections.section ...
//		LOGGER.info("[insertFuselage] n. sections "+ fuselage.getFuselageCreator().getSectionsYZ().size());
//		LOGGER.info("[insertFuselage] n. x-stations "+ fuselage.getFuselageCreator().getSectionsYZStations().size());
//		int idx = 2;
//		LOGGER.info("SectionYZ idx = " + idx);
//		LOGGER.info("x-station = " + fuselage.getFuselageCreator().getSectionsYZStations().get(idx));
//		LOGGER.info("n. points, left = " + fuselage.getFuselageCreator().getSectionsYZ().get(idx).getSectionLeftPoints().size());
//		fuselage.getFuselageCreator().getSectionsYZ().get(idx).getSectionLeftPoints().stream()
//			.forEach(pv -> System.out.println(">> >>" + pv.x + ", " + pv.y + ", " + pv.z));
		
		LOGGER.info("-> getFuselageYZSections .............................................");
		List<List<PVector>> sectionsYZ = getFuselageYZSections(fuselage, 0.15, 1.0, 3, 9, 7, 1.0, 0.10, 3);		
		LOGGER.info("<- getFuselageYZSections .............................................");
		LOGGER.info("[insertFuselage] n. x-stations = " + sectionsYZ.size());
		
//		sectionsYZ.stream()
//			.forEach(lpv -> System.out.println(">>" + lpv.get(0).x));
		
		List<org.w3c.dom.Element> listSectionElements = new ArrayList<>();
		List<org.w3c.dom.Element> listFuselageProfileElements = new ArrayList<>();
		// populate the lists of elements: section AND fuselageProfile
		sectionsYZ.stream()
			.forEach(s -> {
				
			});
		
		listSectionElements.stream().forEach(el -> sectionsElement.appendChild(el)); // fuselage.sections <- section (i-th)
		fuselageElement.appendChild(sectionsElement); // fuselage <-- sections

		listFuselageProfileElements.stream().forEach(el -> _fuselageProfilesElement.appendChild(el)); // cpacs.vehicles.aircraft.profiles.fuselageProfiles <- fuselageProfile (i-th)
		
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

	/**
	 * Creates a list of shapes, mostly surfaces/shells, representing the fuselage.
	 * 
	 * Nose trunk patches. Patch-1, Patch-2:
	 * First, the nose patch is created, as the union of two patches: Patch-1, i.e. Nose Cap Patch, and Patch-2, i.e. from cap terminal section to
	 * the nose trunk terminal section. Patch-1 has numberNoseCapSections supporting section curves and is constrained to include the fuselage foremost tip vertex.
	 * Patch-1 passes thru: nose tip vertex, support-section-1, support-section-2, ... support-section-<numberNoseCapSections>. The last section of Patch-1 coincides
	 * with the first support section of Patch-2, which passes thru: support-section-<numberNoseCapSections>, support-section-<numberNoseCapSections + 1>, ... 
	 * support-section-<numberNoseCapSections + numberNosePatch2Sections>.
	 * 
	 * Cylindrical trunk patch:
	 * Patch-3
	 * 
	 * Tail cone trunk patch:
	 * Patch-4
	 * 
	 * Tail cap patch:
	 * Patch-5
	 * 
	 * @param fuselage 						the fuselage object, extracted from a Aircraft object
	 * @param noseCapSectionFactor1 		the factor multiplying xNoseCap/noseCapLength to obtain the first support section of Patch-1, e.g. 0.15
	 * @param noseCapSectionFactor2			the factor multiplying xNoseCap/noseCapLength to obtain the last support section of Patch-1, e.g. 1.0 (>1.0 means x > xNoseCap) 
	 * @param numberNoseCapSections			number of Patch-1 supporting sections, e.g. 3 
	 * @param numberNosePatch2Sections		number of Patch-2 supporting sections, e.g. 9
	 * @param numberTailPatchSections		number of Patch-4 supporting sections, e.g. 5
	 * @param tailCapSectionFactor1 		the factor multiplying (fuselageLength - xTailCap)/tailCapLength to obtain the first support section of Patch-5, e.g. 1.0 (>1.0 means x < xFusLength - tailCapLength)
	 * @param tailCapSectionFactor2 	    the factor multiplying (fuselageLength - xTailCap)/tailCapLength to obtain the last support section of Patch-5, e.g. 0.15
	 * @param numberTailCapSections			number of Patch-5 supporting sections, e.g. 3 
	 * @return a list of lists of points, one sub-list for each section
	 */
	public static List<List<PVector>> getFuselageYZSections(Fuselage fuselage,
			double noseCapSectionFactor1, double noseCapSectionFactor2, int numberNoseCapSections, 
			int numberNosePatch2Sections, int numberTailPatchSections, double tailCapSectionFactor1, double tailCapSectionFactor2, int numberTailCapSections
			) {
		if (fuselage == null)
			return null;
		
		LOGGER.info("[getFuselageYZSections] extracting " 
				+ (numberNoseCapSections + numberNosePatch2Sections -3 + numberTailPatchSections + numberTailCapSections) + " XY-type sections ...");

		List<List<PVector>> result = new ArrayList<>();
		
		Amount<Length> noseLength = fuselage.getFuselageCreator().getLengthNoseTrunk();
		System.out.println(">> Nose length: " + noseLength);
		Amount<Length> noseCapStation = fuselage.getFuselageCreator().getDxNoseCap();
		System.out.println(">> Nose cap x-station: " + noseCapStation);
		Double xbarNoseCap = fuselage.getNoseDxCapPercent(); // normalized with noseLength
		System.out.println(">> Nose cap x-station normalized: " + xbarNoseCap);
		Amount<Length> zNoseTip = Amount.valueOf( 
				fuselage.getFuselageCreator().getZOutlineXZLowerAtX(0.0),
				SI.METER);
		System.out.println(">> Nose tip z: " + zNoseTip);

		LOGGER.info("[getFuselageYZSections] Nose cap, from nose tip: x = 0 m to x=" + noseCapStation);
		
		System.out.println(">> Getting selected sections ...");
		// all xbar's are normalized with noseLength
		List<Double> xbars1 = Arrays.asList(
				MyArrayUtils
					// .linspaceDouble(
					.halfCosine2SpaceDouble(
					// .cosineSpaceDouble(
					noseCapSectionFactor1*xbarNoseCap, noseCapSectionFactor2*xbarNoseCap, 
					numberNoseCapSections) // n. points
				);
		
		List<List<PVector>> sections1 = new ArrayList<List<PVector>>();
		xbars1.stream()
			  .forEach(x -> sections1.add(
					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
			  );

		// x stations defining cap outlines
		List<Double> xmtPatch1 = new ArrayList<>();
		xmtPatch1.add(0.0); // nose tip
		xbars1.stream()
			  .forEach(x -> xmtPatch1.add(x*noseLength.doubleValue(SI.METER)));
		
		System.out.println(">> Nose-cap trunk selected x-stations (m), Patch-1: " + xmtPatch1.toString());
		
		LOGGER.info("[getFuselageYZSections] Nose trunk (no cap): x=" + noseCapStation + " to x=" + noseLength);
		
		System.out.println(">> Getting selected sections ...");

		// all xbar's are normalized with noseLength
		List<Double> xbars2 = Arrays.asList(
				MyArrayUtils
				// .linspaceDouble(
				// .halfCosine1SpaceDouble(
				.cosineSpaceDouble(
					noseCapSectionFactor2*xbarNoseCap, 1.0, 
					numberNosePatch2Sections) // n. points
				);

		// x stations defining nose outlines
		List<Double> xmtPatch2 = new ArrayList<>();
		xbars2.stream()
			  .forEach(x -> xmtPatch2.add(x*noseLength.doubleValue(SI.METER)));
		
		System.out.println(">> Nose trunk selected x-stations (m), Patch-2: " + xmtPatch2.toString());

//		List<List<PVector>> sections2 = new ArrayList<List<PVector>>();
//		xbars2.stream()
//			  .forEach(x -> sections2.add(
//					  fuselage.getFuselageCreator().getUniqueValuesYZSideRCurve(noseLength.times(x)))
//			  );

		xmtPatch2.stream()
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> result.add(fuselage.getFuselageCreator().getUniqueValuesYZSectionCurve(x)));
		
		// nose Patch-2 terminal section
//		result.add(fuselage.getFuselageCreator().getUniqueValuesYZSectionCurve(noseLength));
		// do not duplicate this section

		Amount<Length> cylinderLength = fuselage.getFuselageCreator().getLengthCylindricalTrunk();

		LOGGER.info("[getFuselageYZSections] Fuselage cylindrical trunk: x=" + noseLength + " to x=" + noseLength.plus(cylinderLength));

		// x stations defining cylinder outlines
		List<Double> xmtPatch3 = Arrays.asList(
				MyArrayUtils.linspaceDouble(
						noseLength.doubleValue(SI.METER), noseLength.plus(cylinderLength).doubleValue(SI.METER), 
						3) // n. points
				);
		
		System.out.println(">> Cylinder trunk selected x-stations (m), Patch-3: " + xmtPatch3.toString());
		
		// Cylindrical trunk mid section
		result.add(
				fuselage.getFuselageCreator().getUniqueValuesYZSectionCurve(
						noseLength.plus(cylinderLength.times(0.5))
						)
				);

		// Cylindrical trunk terminal section
		result.add(
				fuselage.getFuselageCreator().getUniqueValuesYZSectionCurve(
						noseLength.plus(cylinderLength)
						)
				);

		// Tail trunk
		Amount<Length> tailLength = fuselage.getFuselageCreator().getLengthTailTrunk();
		Amount<Length> tailCapLength = fuselage.getFuselageCreator().getDxTailCap();
		Amount<Length> fuselageLength = fuselage.getLength();

		LOGGER.info("[getFuselageYZSections] Tail trunk (no cap): x=" 
				+ noseLength.plus(cylinderLength) + " to x=" + fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)) + " (fus. length - tail cap length)"
				);

		// x stations defining cylinder outlines
		List<Double> xmtPatch4 = Arrays.asList(
				MyArrayUtils.halfCosine1SpaceDouble( // cosineSpaceDouble( // 
						noseLength.plus(cylinderLength).doubleValue(SI.METER), 
						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)).doubleValue(SI.METER),
						numberTailPatchSections) // n. points
				);
		
		System.out.println(">> Tail trunk selected x-stations (m), Patch-4: " + xmtPatch4.toString());
		
		xmtPatch4.stream()
		 		 .skip(1) // do not duplicate this section
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> result.add(fuselage.getFuselageCreator().getUniqueValuesYZSectionCurve(x)));
		
		// tail cap patch

		LOGGER.info("[getFuselageYZSections] Fuselage tail cap trunk: x=" 
				+ fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)) + " to x=" + fuselageLength + " (fus. total length)"
				);

		// x stations in tail cap
		List<Double> xmtPatch5 = Arrays.asList(
				MyArrayUtils.halfCosine2SpaceDouble(
						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor1)).doubleValue(SI.METER), 
						fuselageLength.minus(tailCapLength.times(tailCapSectionFactor2)).doubleValue(SI.METER), // tweak to avoid a degenerate section 
						numberTailCapSections) // n. points
				);

		System.out.println(">> Tail cap trunk selected x-stations (m), Patch-5: " + xmtPatch5.toString());
		
		Amount<Length> zTailTip = Amount.valueOf( 
				fuselage.getFuselageCreator().getZOutlineXZLowerAtX(fuselageLength.doubleValue(SI.METER)),
				SI.METER);
		
//		CADVertex vertexTailTip = OCCUtils.theFactory.newVertex(
//				fuselageLength.doubleValue(SI.METER), 0, zTailTip.doubleValue(SI.METER));
		
		xmtPatch5.stream()
		 .skip(1) // do not duplicate this section
				 .map(x -> Amount.valueOf(x, SI.METER))
				 .forEach(x -> result.add(fuselage.getFuselageCreator().getUniqueValuesYZSectionCurve(x)));

		//============================================================================================
		
//		// other nose cap entities (outline curves, vertices)
		
		PVector ptNoseTip = new PVector(0.0f, 0.0f, (float)zNoseTip.doubleValue(SI.METER));
		
		// points z's on nose outline curve, XZ, upper
		List<double[]> pointsNoseCapXZUpper = xmtPatch1.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
				})
				.collect(Collectors.toList());		
		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsNoseCapXZLower = xmtPatch1.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
				})
				.collect(Collectors.toList());
		// points y's on nose outline curve, XY, right
		List<double[]> pointsNoseCapSideRight = xmtPatch1.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
		
		
		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsNoseXZLower = xmtPatch2.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
				})
				.collect(Collectors.toList());
		
		List<double[]> pointsNoseSideRight = xmtPatch2.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
		
		// support sections of cylinder, patch-3
		
		// points z's on cylinder outline curve, XZ, upper
		List<double[]> pointsCylinderXZUpper = xmtPatch3.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
				})
				.collect(Collectors.toList());
		// points z's on cylinder outline curve, XZ, lower
		List<double[]> pointsCylinderXZLower = xmtPatch3.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
				})
				.collect(Collectors.toList());
		
		// cylinder side curve
		List<double[]> pointsCylinderSideRight = xmtPatch3.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
		
		// tail trunk
		// points z's on nose outline curve, XZ, upper
		List<double[]> pointsTailXZUpper = xmtPatch4.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
				})
				.collect(Collectors.toList());
		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsTailXZLower = xmtPatch4.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
				})
				.collect(Collectors.toList());
		
		// tail side curve
		List<double[]> pointsTailSideRight = xmtPatch4.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
		
		// points z's on tail cap outline curve, XZ, upper
		List<double[]> pointsTailCapXZUpper = xmtPatch5.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZUpperAtX(x)
				})
				.collect(Collectors.toList());
//		pointsTailCapXZUpper.add(vertexTailTip.pnt()); // add tail tip point

		// points z's on nose outline curve, XZ, lower
		List<double[]> pointsTailCapXZLower = xmtPatch5.stream()
				.map(x -> new double[]{
						x,
						0.0,
						fuselage.getFuselageCreator().getZOutlineXZLowerAtX(x)
				})
				.collect(Collectors.toList());
//		pointsTailCapXZLower.add(vertexTailTip.pnt()); // add tail tip point
		
		// tail side curve
		List<double[]> pointsTailCapSideRight = xmtPatch5.stream()
				.map(x -> new double[]{
						x,
						fuselage.getFuselageCreator().getYOutlineXYSideRAtX(x),
						fuselage.getFuselageCreator().getCamberZAtX(x)
				})
				.collect(Collectors.toList());
//		pointsTailCapSideRight.add(vertexTailTip.pnt()); // add tail tip point
		
		return result;
	}

	
	
}

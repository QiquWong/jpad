package standaloneutils.cpacs;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import de.dlr.sc.tigl.CpacsConfiguration;
import de.dlr.sc.tigl.Tigl;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;
import de.dlr.sc.tigl.TiglException;
import de.dlr.sc.tigl.TiglNativeInterface;
import de.dlr.sc.tigl.TiglReturnCode;
import de.dlr.sc.tigl.TiglSymmetryAxis;

/*
 * This class wraps the functionalities provided by de.dlr.sc.tigl.* classes
 */
public class CPACSReader {

	public static enum ReadStatus {
		OK,
		ERROR;
	}

	// reader object for low-level tasks
	JPADXmlReader _jpadXmlReader = null;

	/**
	 * Central logger instance.
	 */
	protected static final Log LOGGER = LogFactory.getLog(CpacsConfiguration.class);	

	private Document _importDoc;

	private String _cpacsFilePath;

	CpacsConfiguration _config;

	private int _fuselageCount;
	private int _wingCount;

	private ReadStatus _status = null;

	/*
	 * The object that manages the functionalities provided by de.dlr.sc.tigl.* classes
	 * 
	 * @param the path of the CPACS file (.xml)
	 */
	public CPACSReader(String filePath) {
		_cpacsFilePath = filePath;
		init();
	}

	private void init() {

		try {

			_config = Tigl.openCPACSConfiguration(_cpacsFilePath,"");

			IntByReference fuselageCount = new IntByReference(0);
			TiglNativeInterface.tiglGetFuselageCount(_config.getCPACSHandle(), fuselageCount);
			_fuselageCount = fuselageCount.getValue();

			IntByReference wingCount = new IntByReference(0);
			TiglNativeInterface.tiglGetWingCount(_config.getCPACSHandle(), wingCount);
			_wingCount = wingCount.getValue();

			_status = ReadStatus.OK;
			_jpadXmlReader = new JPADXmlReader(_cpacsFilePath);
			_importDoc = _jpadXmlReader.getXmlDoc();
		} catch (TiglException e) {
			_status = ReadStatus.ERROR;
			System.err.println(e.getMessage());
			System.err.println(e.getErrorCode());
		}		
	}

	/*
	 * @return CPACS configuration object
	 * @see de.dlr.sc.tigl.CpacsConfiguration
	 */
	public CpacsConfiguration getConfig() {
		return _config;
	}

	/*
	 * Closes the internal CPACS configuration object
	 */
	public void closeConfig() {
		_config.close();
	}

	/*
	 * Closes the current internal CPACS configuration object and opens the given CPACS file
	 * 
	 *  @param the path of the CPACS file (.xml)
	 */
	public void open(String filePath) {
		_config.close();
		_cpacsFilePath = filePath;
		init();
	}

	public String aircraftName() {
			String aircraftName =  _jpadXmlReader.getXMLPropertyByPath("cpacs/header/name");
		return aircraftName;
		
	}
	
	/**
	 * Returns the number of fuselages in CPACS file
	 * 
	 * @return Number of fuselages
	 */
	public int getFuselageCount() {
		return _fuselageCount;
	}

	/**
	 * Returns the number of wings in CPACS file
	 * 
	 * @return Number of wings
	 */	
	public int getWingCount() {
		return _wingCount;
	}

	/**
	 * Returns the ID string of fuselage no. idxFuselageBase1
	 * 
	 * @param idxFuselageBase1 - Index of the fuselage
	 * @return ID string
	 * @throws TiglException
	 */
	public String getFuselageID(int idxFuselageBase1) throws TiglException {
		if ( 
				(_config != null)
				&& (_fuselageCount > 0) 
				&& (idxFuselageBase1 >0)
				&& (idxFuselageBase1 <= _fuselageCount)
				) {
			/*
			// see: http://www.eshayne.com/jnaex/
			// see: www.eshayne.com/jnaex/example02.html

			// get string from C
			// allocate a void**
			PointerByReference uidNamePtr = new PointerByReference();
			// call the C function
			TiglNativeInterface
				.tiglFuselageGetUID(
						_config.getCPACSHandle(), idxFuselageBase1, uidNamePtr);
			// extract the void* that was allocated in C
			Pointer p = uidNamePtr.getValue();
			// extract the null-terminated string from the Pointer
			String uid = p.getString(0);
			return uid;
			 */
			return _config.fuselageGetUID(idxFuselageBase1);
		} else {
			return "";
		}
	}

	/**
	 * Returns the number of sections of the selected fuselage
	 * 
	 * @param idxFuselageBase1 - Index of the fuselage
	 * @return Number of fuselage sections
	 * @throws TiglException
	 */
	public int getFuselageSectionCount(final int idxFuselageBase1) throws TiglException {
		return _config.fuselageGetSectionCount(idxFuselageBase1);
	}	

	/**
	 * Returns the external surface area of selected fuselage
	 * 
	 * @param idxFuselageBase1 - Index of the fuselage
	 * @return area (double)
	 * @throws TiglException
	 */
	public double getFuselageSurfaceArea(int idxFuselageBase1) throws TiglException {
		if ( 
				(_config != null)
				&& (_fuselageCount > 0) 
				&& (idxFuselageBase1 >0)
				&& (idxFuselageBase1 <= _fuselageCount)
				) {
			/*			
			DoubleByReference fuselageSurfaceArea = new DoubleByReference(0.0);
			TiglNativeInterface
				.tiglFuselageGetSurfaceArea(
						_config.getCPACSHandle(), idxFuselageBase1, fuselageSurfaceArea);
			return fuselageSurfaceArea.getValue();
			 */
			return _config.fuselageGetSurfaceArea(idxFuselageBase1);
		} else {
			return 0.0;
		}
	}

	/**
	 * Returns the internal of selected fuselage
	 * 
	 * @param idxFuselageBase1 - Index of the fuselage
	 * @return volume (double)
	 * @throws TiglException
	 */
	public double getFuselageVolume(int idxFuselageBase1) throws TiglException {
		if ( 
				(_config != null)
				&& (_fuselageCount > 0) 
				&& (idxFuselageBase1 >0)
				&& (idxFuselageBase1 <= _fuselageCount)
				) {
			/*			
			DoubleByReference fuselageVolume = new DoubleByReference(0.0);
			TiglNativeInterface
				.tiglFuselageGetVolume(
						_config.getCPACSHandle(), idxFuselageBase1, fuselageVolume);
			return fuselageVolume.getValue();
			 */
			return _config.fuselageGetVolume(idxFuselageBase1);
		} else {
			return 0.0;
		}
	}

	/**
	 * Returns the length of selected fuselage
	 * 
	 * @param idxFuselageBase1 - Index of the fuselage
	 * @return length (double)
	 * @throws TiglException
	 */
	public double getFuselageLength(int idxFuselageBase1) throws TiglException {
		double length = 0.0;
		if ( 
				(_config != null)
				&& (_fuselageCount > 0) 
				&& (idxFuselageBase1 >0)
				&& (idxFuselageBase1 <= _fuselageCount)
				) {

			IntByReference fuselageSegmentCount = new IntByReference();
			int errorCode = TiglNativeInterface
					.tiglFuselageGetSegmentCount(
							_config.getCPACSHandle(), idxFuselageBase1, fuselageSegmentCount);
			throwIfError("tiglFuselageGetStartConnectedSegmentCount", errorCode);

			// if (errorCode == 0) 
			for (int kSegmentBase1 = 1; kSegmentBase1 <= fuselageSegmentCount.getValue(); kSegmentBase1++) {
				// System.out.println("Fuselage segment: " + (kSegmentBase1));
				String fuselageSectionUID;
				try {
					fuselageSectionUID = _config.fuselageGetSegmentUID(idxFuselageBase1, kSegmentBase1);
					System.out.println("\tFuselage segment UID: " + fuselageSectionUID);

					List<String> props = new ArrayList<>();
					props = MyXMLReaderUtils.getXMLPropertiesByPath(
							_importDoc, 
							"/cpacs/vehicles/aircraft/model/fuselages/fuselage["
									+ idxFuselageBase1 
									+"]/positionings/positioning["
									+ kSegmentBase1 +"]/length/text()"
							);

					System.out.println("\t...: " + props.get(0) + " size: "+ props.size());

					length = length + Double.parseDouble(props.get(0));

				} catch (TiglException e) {
					e.printStackTrace();
				}
			}		
			return length;
		} else {
			return 0.0;
		}
	}

	// taken from Tigl --> CpacsConfiguration.java
	private static void throwIfError(String methodname, int errorCode) throws TiglException {
		if (errorCode != TiglReturnCode.TIGL_SUCCESS.getValue()) {
			String message = " In TiGL function \"" + methodname + "."
					+ "\"";
			LOGGER.error("TiGL: Function " + methodname + " returned " + TiglReturnCode.getEnum(errorCode).toString() + ".");
			throw new TiglException(message, TiglReturnCode.getEnum(errorCode));
		}
	}

	public NodeList getWingList() {
		if (_jpadXmlReader != null )
			return MyXMLReaderUtils.getXMLNodeListByPath(
					_jpadXmlReader.getXmlDoc(), 
					"//vehicles/aircraft/model/wings/wing");
		else
			return null;
	}

	public JPADXmlReader getJpadXmlReader() {
		return _jpadXmlReader;
	}

	/**
	 * 
	 * @param WingUID Wing UID in the CPACS
	 * @return Wing Span (CPACS define wing also canard, horizontal tail, vertical tail)
	 * @throws TiglException
	 */
	public double getWingSpan(String WingUID) throws TiglException {

		return _config.wingGetSpan(WingUID);

	}
	/**
	 * 
	 * @param WingUID Wing UID in the CPACS
	 * @return  Wing wetted area (CPACS define wing also canard, horizontal tail, vertical tail)
	 * @throws TiglException
	 */
	public double getWingWettedArea(String WingUID) throws TiglException {

		return _config.wingGetWettedArea(WingUID);

	}	
	/**
	 * 
	 * @param wingIndexZeroBased Wing index in the CPACS, remember CPAC definition start from 0
	 * @return  Wing  area (CPACS define wing also canard, horizontal tail, vertical tail)
	 * @throws TiglException
	 */
	public double getWingReferenceArea(int wingIndexZeroBased) throws TiglException {
		if (TiglSymmetryAxis.TIGL_X_Z_PLANE != _config.wingGetSymmetry(wingIndexZeroBased + 1))
			System.err.println("getWingReferenceArea: wing " + (wingIndexZeroBased +1) + " not equal to TIGL_X_Z_PLANE");
		
		return _config.wingGetSurfaceArea(wingIndexZeroBased + 1);
	}		

	/**
	 * Get value and position of the mean aerodynamic chord of the desired wing 
	 * @param wingUID Main wing UID in the CPACS
	 * @return
	 * @throws TiglException
	 */
	public double[] getWingMeanAerodynamicChord(String wingUID) throws TiglException {
		DoubleByReference mac   = new DoubleByReference();
		DoubleByReference mac_x = new DoubleByReference();
		DoubleByReference mac_y = new DoubleByReference();
		DoubleByReference mac_z = new DoubleByReference();
		double[] macVector;
		macVector= new double [4];
		if (TiglNativeInterface.tiglWingGetMAC(_config.getCPACSHandle(), wingUID, mac, mac_x, mac_y, mac_z) == 0) {
			macVector[0]=mac.getValue();
			macVector[1]=mac_x.getValue();
			macVector[2]=mac_y.getValue();
			macVector[3]=mac_z.getValue();
			return macVector;
		}
		else
			return null;
	}		

	/**
	 * 
	 * @param uid Wing UID in the CPACS
	 * @return  Wing Index position in the CPACS (CPACS define wing also canard, horizontal tail, vertical tail)
	 * @throws TiglException
	 */
	public int getWingIndexZeroBased(String uid) throws TiglException {
		NodeList wingsNodes = getWingList();
		int idx = 0;
		for (int i = 0; i < wingsNodes.getLength(); i++) {
			Node nodeWing  = wingsNodes.item(i); // .getNodeValue();
			Element elementWing = (Element) nodeWing;
			if (elementWing.getAttribute("uID").equals(uid)) {
				idx = i;
				break;
			}
		}
		return idx;
	}	

	/**
	 * Searches (x,y,z) coordinates of empty-weight CG. By default in the path
	 * "cpacs/vehicles/aircraft/model/analyses/massBreakdown/mOEM/mEM/massDescription/location"
	 * @return Position of the Gravity center respect to empty weight, given as a vector where
	 * 0 --> x-position
	 * 1 --> y-position
	 * 3 --> z-position
	 * @throws TiglException
	 */
	public double[] getGravityCenterPosition(String... args) throws TiglException {
		String pathToCoords = "cpacs/vehicles/aircraft/model/analyses/massBreakdown/mOEM/mEM/massDescription/location";
		if (args.length > 0)
			pathToCoords = args[0];
		
		double[] gravityCenterPosition;
		gravityCenterPosition= new double [3]; 
		String xPositionGCString = _jpadXmlReader.getXMLPropertyByPath(pathToCoords+"/x");
			if (xPositionGCString != null) {
				gravityCenterPosition[0] = Double.parseDouble(xPositionGCString);	
			}
			else {
				gravityCenterPosition[0] = 0.0;
			}
					

		String yPositionGCString = _jpadXmlReader.getXMLPropertyByPath(pathToCoords+"/y");
					
		if (xPositionGCString != null) {
			gravityCenterPosition[1] = Double.parseDouble(yPositionGCString);	
		}
		else {
			gravityCenterPosition[1] = 0.0;
		}
		String zPositionGCString = _jpadXmlReader.getXMLPropertyByPath(pathToCoords+"/z");
		
		if (xPositionGCString != null) {
			gravityCenterPosition[2] = Double.parseDouble(zPositionGCString);		
		}
		else {
			gravityCenterPosition[2] = 0.0;
		}		

		return gravityCenterPosition;
	}	
	/**
	 * 
	 * @param pathInTheCpacs Path in the cpacs
	 * @return x,y,z value of the pathInTheCpacs parameter ( i.e scaling, rotation, translation)
	 * @throws TiglException
	 */

	public double[] getVectorPosition(String pathInTheCpacs) throws TiglException {
		double[] vectorCenterPosition;
		vectorCenterPosition= new double [3]; 
		System.out.println("--------------------------------");
		System.out.println("Vector Position of " + pathInTheCpacs );
		System.out.println("--------------------------------");
		String xPositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/x");
		
		if (xPositionGCString != null) {
			vectorCenterPosition[0] = Double.parseDouble(xPositionGCString);		
		}
		else {
			vectorCenterPosition[0] = 0.0;
		}
		
		
		String yPositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/y");
		if (xPositionGCString != null) {
			vectorCenterPosition[1] = Double.parseDouble(yPositionGCString);		
		}
		else {
			vectorCenterPosition[1] = 0.0;
		}
					
		String zPositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/z");
		if (xPositionGCString != null) {
			vectorCenterPosition[2] = Double.parseDouble(zPositionGCString);		
		}
		else {
			vectorCenterPosition[2] = 0.0;
		}
		return vectorCenterPosition;
	}	

	/**
	 * 
	 * @param wingUID Wing UID in the CPACS
	 * @return Tanget of the Sweep Angle
	 * @throws TiglException
	 */

	public double getWingSweep(String wingUID) throws TiglException {
		int wingIndex = getWingIndexZeroBased(wingUID);
		NodeList wingSectionElementPosition = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/positionings/positioning");		
		int lastWingElementPositionIndex = wingSectionElementPosition.getLength()-1;
		String tanSweepAngleString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/"
						+ "positionings/positioning["+lastWingElementPositionIndex+"]/sweepAngle");
		return Double.parseDouble(tanSweepAngleString);			
	}
	
	public double[][] getVectorPositionNodeTank(Node tankNode, double [] cgPosition, double massEW){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			String[] s1 = null; // for list to string
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(tankNode, true);
			doc.appendChild(importedNode);
			NodeList sectionsTank = MyXMLReaderUtils.getXMLNodeListByPath(doc,
					"//mFuel/fuelInTanks/fuelInTank");
			double[][] tankMatrix = new double [sectionsTank.getLength()][4];
			if (sectionsTank.getLength() == 0 ) {
				System.out.println("Error tank not found");
				return null;
			}
			for (int i = 0;i<sectionsTank.getLength();i++) {
				
					DocumentBuilderFactory factoryInt = DocumentBuilderFactory.newInstance();
					factoryInt.setNamespaceAware(true);
					DocumentBuilder builderInt;
					try {
						builderInt = factoryInt.newDocumentBuilder();
						Document docInt = builderInt.newDocument();
						Node importedNodeInt = docInt.importNode(sectionsTank.item(i), true);
						docInt.appendChild(importedNodeInt);
						System.out.println("Read tank : "+ MyXMLReaderUtils.getXMLPropertyByPath(
								importedNode,"//tankUID/text()"));
						List<String>tankMassFuel = MyXMLReaderUtils.getXMLPropertiesByPath(docInt,
								"//mass/text()");
						s1 = tankMassFuel.get(0).split(";");
						tankMatrix[i][3] = Double.parseDouble(s1[s1.length-1]);
						List<String>tankXPosition = MyXMLReaderUtils.getXMLPropertiesByPath(docInt,
								"//coG/x/text()");
						s1 = tankXPosition.get(0).split(";");
						double xTotalMass = Double.parseDouble(s1[s1.length-1]);
						tankMatrix[i][0] =xTotalMass;
						List<String>tankYPosition = MyXMLReaderUtils.getXMLPropertiesByPath(docInt,
								"//coG/y/text()");
						s1 = tankYPosition.get(0).split(";");
						double yTotalMass = Double.parseDouble(s1[s1.length-1]);
						tankMatrix[i][1] = yTotalMass;
						List<String>tankZPosition = MyXMLReaderUtils.getXMLPropertiesByPath(docInt,
								"//coG/z/text()");
						s1 = tankZPosition.get(0).split(";");
						double zTotalMass = Double.parseDouble(s1[s1.length-1]);
						tankMatrix[i][2] = zTotalMass;
					}
					catch (ParserConfigurationException e) {
						e.printStackTrace();
						return null;
					}
				}

			return tankMatrix;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void getControlSurfacePilotCommandIndexAndValue(
			 Node wingNode, List<String> controlSurfaceList, List<Integer> controlSurfaceInt){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;

			try {
				builder = factory.newDocumentBuilder();
				Document doc = builder.newDocument();
				Node importedNode = doc.importNode(wingNode, true);
				doc.appendChild(importedNode);
				NodeList sectionsControlSurfaceList = MyXMLReaderUtils.getXMLNodeListByPath(doc, 
						"//componentSegments/componentSegment/controlSurfaces/"
								+ "trailingEdgeDevices/trailingEdgeDevice");
				if (sectionsControlSurfaceList.getLength() == 0) {
					System.out.println("Error control surface not found");
				}
				for (int i = 0;i <sectionsControlSurfaceList.getLength();i++) {
					Node sectionsControlSurfaceNode = sectionsControlSurfaceList.item(i);
					String controlSurfaceUID = MyXMLReaderUtils.getXMLPropertyByPath(
							sectionsControlSurfaceNode,
							"//name/text()");
					System.out.println("Reading control surface : " + controlSurfaceUID);
					controlSurfaceInt.add(
							getControlSurfacePilotCommand
							(controlSurfaceUID, sectionsControlSurfaceNode, controlSurfaceList)
							);
				}
				
			}

			catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		
	}
	/**
	 * 
	 * @param controlSurfaceUID control surface UID
	 * @param sectionsControlSurfaceNode node of the control surface in the wing node of the previous function
	 * @return
	 */
	
	public int getControlSurfacePilotCommand(
			String controlSurfaceUID, Node sectionsControlSurfaceNode, List<String> controlSurfaceList){
		int controlSurfaceNumberOfStep = 0;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(sectionsControlSurfaceNode, true);
			doc.appendChild(importedNode);
			NodeList deflectionNode = MyXMLReaderUtils.getXMLNodeListByPath(doc, "//path/steps/step");
			for (int j = 0;j<deflectionNode.getLength();j++) {
				Node nodeControlSurface  = deflectionNode.item(j); // .getNodeValue();
				String relDeflection = MyXMLReaderUtils.getXMLPropertyByPath(
						nodeControlSurface,
						"//relDeflection/text()");
				String absDeflection = MyXMLReaderUtils.getXMLPropertyByPath(
						nodeControlSurface,
						"//hingeLineRotation/text()");	
				controlSurfaceList.add(absDeflection + "; " + relDeflection);
			}
			controlSurfaceNumberOfStep = deflectionNode.getLength();
			return controlSurfaceNumberOfStep;
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
			return 0;
		}

		 
	}
	/**
	 * 	Return as List landing gear characteristic. Position  
	 * 0)Static friction
	 * 1)Dynamic friction
	 * 2)Rolling friction
	 * 3)Spring friction
	 * 4)Damping coefficient
	 * 5)Damping coefficient rebound
	 * 6)max steer
	 * 7)Retractable
	 * @param landingGearNode landing gear node in the CPACS (tool specific)
	 * @return landing Gear characteristic as list 
	 */
	public List<Double> getLandingGear(Node landingGearNode){
		List<Double> landingGear = new ArrayList<Double>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(landingGearNode, true);
			doc.appendChild(importedNode);
			String staticFriction = MyXMLReaderUtils.getXMLPropertyByPath(
					landingGearNode,
					"//static_friction/text()");
			landingGear.add(Double.parseDouble(staticFriction));
			String dynamicFriction = MyXMLReaderUtils.getXMLPropertyByPath(
					landingGearNode,
					"//dynamic_friction/text()");
			landingGear.add(Double.parseDouble(dynamicFriction));
			String rollingFriction = MyXMLReaderUtils.getXMLPropertyByPath(
					landingGearNode,
					"//rolling_friction/text()");
			landingGear.add(Double.parseDouble(rollingFriction));
			XPath landingGearXpath = _jpadXmlReader.getXpath();

			String springCoefficient = MyXMLReaderUtils.getXMLPropertyByPath(
					doc, landingGearXpath, "//spring_coeff/text()");
			landingGear.add(Double.parseDouble(springCoefficient));
			String dampingCoefficient = MyXMLReaderUtils.getXMLPropertyByPath(
					doc, landingGearXpath,
					"//damping_coeff/text()");
			landingGear.add(Double.parseDouble(dampingCoefficient));
			String dampingCoefficientRebound = MyXMLReaderUtils.getXMLPropertyByPath(
					doc, landingGearXpath,
					"//damping_coeff_rebound/text()");
			landingGear.add(Double.parseDouble(dampingCoefficientRebound));
			String maxSteer = MyXMLReaderUtils.getXMLPropertyByPath(
					doc, landingGearXpath,
					"//max_steer/text()");
			landingGear.add(Double.parseDouble(maxSteer));
			String retractable = MyXMLReaderUtils.getXMLPropertyByPath(
					landingGearNode,
					"//retractable/text()");
			landingGear.add(Double.parseDouble(retractable));
			return landingGear;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 	Return as List engine characteristic. Position  
	 * 0)Thrust at takeoff
	 * 1)Fan pressure ratio at takeoff
	 * 2)Bypass ratio at takeoff
	 * 3)overall pressure ratio at takeoff
	 * 4)Mass
	 * @param engineNode engine in the CPACS 
	 * @return engine characteristic as list 
	 */
	public List<Double> getEngine(Node engineNode, String engineType){
		List<Double> engine = new ArrayList<Double>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(engineNode, true);
			doc.appendChild(importedNode);
			String thrust = MyXMLReaderUtils.getXMLPropertyByPath(
					engineNode,
					"//thrust00/text()");
			double millthrust = Double.parseDouble(thrust)*0.224809; //Conversion from Newton to lbf
			thrust = String.valueOf(millthrust);
			engine.add(Double.parseDouble(thrust));
			if (engineType.equals("turbofan")) {
			String fpr = MyXMLReaderUtils.getXMLPropertyByPath(
					engineNode,
					"//fpr00/text()");
			engine.add(Double.parseDouble(fpr));
			String bpr = MyXMLReaderUtils.getXMLPropertyByPath(
					engineNode,
					"//bpr00/text()");
			engine.add(Double.parseDouble(bpr));
			String opr = MyXMLReaderUtils.getXMLPropertyByPath(
					engineNode,
					"//opr00/text()");
			engine.add(Double.parseDouble(opr));
			}
			String mass = MyXMLReaderUtils.getXMLPropertyByPath(
					engineNode,
					"//mass/mass/text()");
			engine.add(Double.parseDouble(mass));

			return engine;

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * @param doc doc of the AeroPerformanceMap node
	 * @return as vector Mach number of the AeroPerformanceMap analysis
	 */
	public static double[] getMachNumberFromAeroPerformanceMap(Document doc){
				List<String>machNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
						"//machNumber/text()");
				return CPACSUtils.getDoubleArrayFromStringList(machNumberList);
	}
	
	public static Document createDOCFromNode(Node node) throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;

			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(node, true);
			doc.appendChild(importedNode);
			return doc;
	}
	
	/**
	 * 
	 * @param node node of the control surface in the AeroPerformanceMap element
	 * @return deflection, in degree, of the control surface in the aeroPerformance
	 * @throws ParserConfigurationException
	 */
	public static double[] getControlSurfaceDeflectionFromAeroPerformanceMap(Node node) throws ParserConfigurationException{
		Document doc = createDOCFromNode(node);
		List<String>deflectionList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
				"//relDeflection/text()");
		
		
		return CPACSUtils.getDoubleArrayFromStringList(deflectionList);
}
	/**
	 * 
	 * @param doc doc of the AeroPerformanceMap node
	 * @return as vector Reynolds number of the AeroPerformanceMap analysis
	 */
	public static double[] getReynoldsNumberFromAeroPerformanceMap(Document doc){
				List<String>reynoldsNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
						"//reynoldsNumber/text()");
				return CPACSUtils.getDoubleArrayFromStringList(reynoldsNumberList);
	}
	
	/**
	 * 
	 * @param betaVector vector of beta
	 * @return Return 0 if betaVector[0] != 0. Return 1 if betaVector[0] = 0  
	 * This check value is used to add negative value in the aeroPerformance map, because JSBSim don't extrapolate data.
	 */
	public static int checkBetaAngle(double[] betaVector){
		int check = 0 ;
		if (betaVector[0] == 0 && betaVector.length>1) {
			check = 1;
		}
		return check;

}
	
	/**
	 * 
	 * @param doc doc of the AeroPerformanceMap node
	 * @return as vector yaw angle, in degree, of the AeroPerformanceMap analysis
	 */
	public static double[] getYawFromAeroPerformanceMap(Document doc){
		List<String>yawAngleList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
				"//angleOfYaw/text()");
		double[] betaVector = CPACSUtils.getDoubleArrayFromStringList(yawAngleList);
		int check = checkBetaAngle(betaVector);
		if (check == 0) {
			double[] outputVector = CPACSUtils.getDoubleArrayFromStringList(yawAngleList);
			return outputVector;
		}
		else {
			double[] outputVector = new double[betaVector.length + 1];
			outputVector[0] = -betaVector[betaVector.length-1];
			for (int i = 1;i<outputVector.length;i++) {
				outputVector[i] = betaVector[i-1];
			}
			return outputVector;
		}				
	}


	
	/**
	 * 
	 * @param doc doc of the aeroperformance Map
	 * @return as a double vector altitude in feet
	 */
	
	public static double[] getAltitudeFromAeroPerformanceMap(Document doc){
		List<String>reynoldsNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
				"//altitude/text()");
		double [] altitude = null;
		altitude =  CPACSUtils.getDoubleArrayFromStringList(reynoldsNumberList);
		for (int i = 0;i<altitude.length;i++) {
			altitude[i] = altitude[i]*3.28084; // conversion because in the CPACS altitude vector is defined in meters, JSBSim need altitude in feet
		}
		return altitude;
	}
	/**
	 * 
	 * @param doc doc of the AeroPerformanceMap node
	 * @return as vector angle of attack, in degree, of the AeroPerformanceMap analysis
	 */
	public static double[] getAlphaFromAeroPerformanceMap(Document doc){
				List<String>alphaList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
						"//angleOfAttack/text()");
				double[] outputVector = CPACSUtils.getDoubleArrayFromStringList(alphaList);
				return outputVector;
	}
	
	

	/**
	 * 
	 * @param aeroNodeControlSurface Control surface node in the AeroPerformance map
	 * @param coefficientStringPath 
	 * @param aeroNode aeroPerformance map node
	 * @param correctionAxisDefinition for the pitch moments in the CPACS there is a different convention, 1 apply scale, 0 don't apply scale, for instances use 1 only for the pitch
	 * @param mac Mean aerodynamics chord of the wing needed to scale the pitch coefficient
	 * @param wingSpan wing span needed to scale the pitch coefficient
	 * @param shiftFactor The correction applied is the type coefficient = shiftFactor + (mac/wingSpan)*coefficient
	 * @param flagAxis 0 no  correction due to beta, 1 correction due to beta
	 * @return Return as a List<String> AeroPerformance map data of the selected control surface, each element of the list is a matrix in JSBSim format
	 * @throws ParserConfigurationException
	 */


	
	public List<String> getCoefficientFromAeroPerformanceMapControlSurface
	(Node aeroNodeControlSurface,  String coefficientStringPath, Node aeroNode, int correctionAxisDefinition, double mac, double wingSpan, double shiftFactor, double flagAxis)
			throws ParserConfigurationException{
		List<String> forceFixedMach = new ArrayList<String>();
		DocumentBuilderFactory factoryControlSurface = DocumentBuilderFactory.newInstance();
		factoryControlSurface.setNamespaceAware(true);
		DocumentBuilder builderAeroControlSurface;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(aeroNode, true);
			doc.appendChild(importedNode);

		try {
			builderAeroControlSurface = factoryControlSurface.newDocumentBuilder();
			Document docAeroControlSurface = builderAeroControlSurface.newDocument();
			Node importedNodeControlSurface = docAeroControlSurface.importNode(aeroNodeControlSurface, true);
			docAeroControlSurface.appendChild(importedNodeControlSurface);
			double [] alpha = getAlphaFromAeroPerformanceMap(doc);
			double [] yaw = getYawFromAeroPerformanceMap(doc);
			List<String>betaAngleList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//angleOfYaw/text()");
			double[] betaVector = CPACSUtils.getDoubleArrayFromStringList(betaAngleList);
			int check = checkBetaAngle(betaVector);
			List<String> deflectionList = MyXMLReaderUtils.getXMLPropertiesByPath(docAeroControlSurface,
					"//relDeflection/text()");
			double[] deflectionVector = CPACSUtils.getDoubleArrayFromStringList(deflectionList);
			double [][] matrix = new double[alpha.length+1][deflectionVector.length+1];
			for(int i = 0;i<alpha.length+1;i++) {
				for(int j = 0;j<deflectionVector.length+1;j++) {
					matrix[i][j]=0.0;
				}
			}	
			for(int i = 0;i<alpha.length+1;i++) {
				for(int j = 0;j<deflectionVector.length+1;j++) {
					
					if(i == 0 && j!=0) {
						matrix[i][j]=deflectionVector[j-1];
					}
					if (j == 0 && i!=0) {
						matrix[i][j]=alpha[i-1] ;
					}
				}	
			}
			
			List<String> coefficientList = MyXMLReaderUtils.getXMLPropertiesByPath(docAeroControlSurface,
					coefficientStringPath);
			

			
			if (coefficientList.size()>0) {
				
				
				double[] coefficientVector = null;

				if (correctionAxisDefinition==0) {
					coefficientVector = CPACSUtils.getDoubleArrayFromStringList(coefficientList);
				}
				else {
					coefficientVector = CPACSUtils.getDoubleArrayFromStringList(coefficientList);
					for (int s = 0;s<coefficientVector.length;s++) {
						coefficientVector[s] = shiftFactor + coefficientVector[s]*mac/wingSpan;
					}				
				}
//				double[] coefficientVector = CPACSUtils.shiftElementInTheAeroPerformanceMapControlSurface
//						   (coefficientList, alpha.length, yaw.length, deflectionVector.length);
//				int counter = 1;
				int i = 0;
				int j = 0; //counter matrix column index
				int flag = alpha.length*deflectionVector.length-1; 
				int k =  deflectionVector.length; //number of deflection of the control surface 
				int counterBeta = 0;
				for (int s = 0;s<coefficientVector.length;s++) {
					if (j == k) {
						j = 0;
						i = i + 1;
					}
					if (s>flag) {
						if(counterBeta == 0 && check == 1) {
							s = s - alpha.length*deflectionVector.length;
						}
						else {
						flag = flag + alpha.length*deflectionVector.length;
						}
						forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
						j = 0;
						i  = 0;
						counterBeta = counterBeta + 1;
						if (counterBeta == yaw.length) {
							counterBeta = 0;
						}
					}

					if (s<flag+1) {
						matrix[i+1][j+1]=coefficientVector[s];
						if(check == 1 && counterBeta == 0) {
							if(flagAxis == 0) {
								matrix[i+1][j+1]=coefficientVector[s + alpha.length*deflectionVector.length*(yaw.length - 2)];
							}
							if(flagAxis == 1) {
								matrix[i+1][j+1]=-coefficientVector[s + alpha.length*deflectionVector.length*(yaw.length - 2)];	
							}							
						}
						j = j +1;
					}

					if (s==coefficientVector.length-1) {
						i = 0;
						j = 0;
						for (int h = s+1-alpha.length*deflectionVector.length;h<s+1;h++) {
							if (j == k) {
								i = i + 1;
								j  = 0;
							}
							if (i<alpha.length) {
								matrix[i+1][j+1]=coefficientVector[h];
								j++;
							}

						}
						forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
					}
				}
			}
			return forceFixedMach;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	
	/**
	 * 
	 * @param aeroNode AeroPerformaceMap node in the CPACS
	 * @param coefficientStringPath path in the CPACS of the force or moments aerodynamics performances 
	 * @param correctionAxisDefinition for the pitch moments in the CPACS there is a different convention, 1 apply scale, 0 don't apply scale, for instances use 1 only for the pitch
	 * @param mac Mean aerodynamics chord of the wing needed to scale the pitch coefficient
	 * @param wingSpan wing span needed to scale the pitch coefficient
	 * @param shiftFactor The correction applied is the type coefficient = shiftFactor + (mac/wingSpan)*coefficient
	 * @return Return as a List<String> AeroPerformance map data, each element of the list is a matrix in JSBSim format
	 */
	public List<String> getCoefficientFromAeroPerformanceMap
	(Node aeroNode,  String coefficientStringPath, int correctionAxisDefinition, double mac, double wingSpan, double shiftFactor, double flagAxis){
		List<String> forceFixedMach = new ArrayList<String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(aeroNode, true);
			doc.appendChild(importedNode);
			double [] yaw = getYawFromAeroPerformanceMap(doc);
			double [] alpha = getAlphaFromAeroPerformanceMap(doc);
			double [][] matrix = new double[alpha.length+1][yaw.length+1];
			List<String>betaAngleList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//angleOfYaw/text()");
			double[] betaVector = CPACSUtils.getDoubleArrayFromStringList(betaAngleList);
			int check = checkBetaAngle(betaVector);
			
			for(int i = 0;i<alpha.length+1;i++) {
				for(int j = 0;j<yaw.length+1;j++) {
					matrix[i][j]=0.0;
				}
			}	
			for(int i = 0;i<alpha.length+1;i++) {
				for(int j = 0;j<yaw.length+1;j++) {
					
					if(i == 0 && j!=0) {
						matrix[i][j]=yaw[j-1];
					}
					if (j == 0 && i!=0) {
						matrix[i][j]=alpha[i-1] ;
					}
				}	
			}
			
			List<String> coefficientList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					coefficientStringPath);
			if (coefficientList.size()>0) {
				double[] coefficientVector = null;

				if (correctionAxisDefinition==0) {
					coefficientVector = CPACSUtils.getDoubleArrayFromStringList(coefficientList);
				}
				else {
					coefficientVector = CPACSUtils.getDoubleArrayFromStringList(coefficientList);
					for (int s = 0;s<coefficientVector.length;s++) {
						coefficientVector[s] = shiftFactor + coefficientVector[s]*mac/wingSpan;
					}				
				}
//				double[] coefficientVector = CPACSUtils.shiftElementInTheAeroPerformanceMap(
//						coefficientList, alpha.length, yaw.length);

//				int counter = 1;
				int i = 0;
				int j = 0; //counter matrix column index
				int flag = alpha.length-1; //flag need to 
				int flag1 = alpha.length*yaw.length-1;
				int k = 0; //counter Altitude -->3rd dimension (index List<String>)


				for (int s = 0;s<coefficientVector.length;s++) {

					if (s>flag) {
						if(j == 0 && check == 1) {
							s = s - alpha.length;
						}
						else {
							flag = flag+alpha.length;
						}
						j = j + 1;
						i  = 0;
					}

					if (j>yaw.length - 1) {
						flag1 = flag1 + alpha.length*yaw.length;
						forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "\t"));
						j = 0;
						k = k + 1;
					}
					if (s<flag+1) {
						matrix[i+1][j+1]=coefficientVector[s];			
						if (j == 0 && check == 1) {
							if(flagAxis == 0) {
								matrix[i+1][j+1]=coefficientVector[s+alpha.length*(yaw.length-2)];
							}
							if(flagAxis == 1) {
								matrix[i+1][j+1]=-coefficientVector[s+alpha.length*(yaw.length-2)];
							}	
						}

						i = i + 1;
					}

					if (s==coefficientVector.length-1) {
						i = 0;
						j = 0;
						for (int h = s+1-alpha.length*yaw.length;h<s+1;h++) {
							if (i == alpha.length) {
								j = j + 1;
								i  = 0;
							}
							if (i<alpha.length) {
								matrix[i+1][j+1]=coefficientVector[h];
								if (j == 0 && check == 1) {
									if(flagAxis == 0) {
										matrix[i+1][j+1]=coefficientVector[h+alpha.length*(yaw.length-1)];
									}
									if(flagAxis == 1) {
										matrix[i+1][j+1]=-coefficientVector[h+alpha.length*(yaw.length-1)];
									}	
								}
								i++;
							}

						}
						forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
					}
				}
			}
			return forceFixedMach;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getCpacsFilePath() {
		return _cpacsFilePath;
	}

	public ReadStatus getStatus() {
		return _status;
	}

} // end of class

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
		gravityCenterPosition[0] = Double.parseDouble(xPositionGCString);			

		String yPositionGCString = _jpadXmlReader.getXMLPropertyByPath(pathToCoords+"/y");
		gravityCenterPosition[1] = Double.parseDouble(yPositionGCString);			

		String zPositionGCString = _jpadXmlReader.getXMLPropertyByPath(pathToCoords+"/z");
		gravityCenterPosition[2] = Double.parseDouble(zPositionGCString);			

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
		vectorCenterPosition[0] = Double.parseDouble(xPositionGCString);	
		System.out.println("x =  " + xPositionGCString );

		String yPositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/y");
		vectorCenterPosition[1] = Double.parseDouble(yPositionGCString);			
		System.out.println("y =  " + yPositionGCString );

		String zPositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/z");
		System.out.println("z =  " + xPositionGCString );
		vectorCenterPosition[2] = Double.parseDouble(zPositionGCString);			


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
						System.out.println("Capacity = "+ tankMatrix[i][3]);
						List<String>tankXPosition = MyXMLReaderUtils.getXMLPropertiesByPath(docInt,
								"//coG/x/text()");
						s1 = tankXPosition.get(0).split(";");
						double xTotalMass = Double.parseDouble(s1[s1.length-1]);
						tankMatrix[i][0] =xTotalMass;
						System.out.println("Mew = "+ massEW);
						System.out.println("X position = "+ tankMatrix[i][0]);
						List<String>tankYPosition = MyXMLReaderUtils.getXMLPropertiesByPath(docInt,
								"//coG/y/text()");
						s1 = tankYPosition.get(0).split(";");
						double yTotalMass = Double.parseDouble(s1[s1.length-1]);
						tankMatrix[i][1] = yTotalMass;
						System.out.println("Y position = "+ tankMatrix[i][1]);
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
				System.out.println(absDeflection + "; " + relDeflection);
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
			String brakeGroup = MyXMLReaderUtils.getXMLPropertyByPath(
					landingGearNode,
					"//brake_group/text()");
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
	public List<Double> getEngine(Node engineNode){
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
			engine.add(Double.parseDouble(thrust));
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
			XPath engineXpath = _jpadXmlReader.getXpath();

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
	public static double[] getControlSurfaceDeflectionFromAeroPerformanceMap(Node node) throws ParserConfigurationException{
		Document doc = createDOCFromNode(node);
		List<String>machNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
				"//relDeflection/text()");
		return CPACSUtils.getDoubleArrayFromStringList(machNumberList);
}
	
	public static double[] getReynoldsNumberFromAeroPerformanceMap(Document doc){
				List<String>reynoldsNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
						"//reynoldsNumber/text()");
				return CPACSUtils.getDoubleArrayFromStringList(reynoldsNumberList);
	}
	
	public static double[] getYawFromAeroPerformanceMap(Document doc){
				List<String>yawAngleList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
						"//angleOfYaw/text()");
				return CPACSUtils.getDoubleArrayFromStringList(yawAngleList);
	}
	
	public static double[] getAlphaFromAeroPerformanceMap(Document doc){
				List<String>alphaList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
						"//angleOfAttack/text()");
				return CPACSUtils.getDoubleArrayFromStringList(alphaList);
	}
	
	
	
	public static void getCoefficientFromAeroPerformanceMapControlSurface
	(Node aeroNodeControlSurface, List<String> forceFixedMach, String coefficientStringPath, Node aeroNode)
			throws ParserConfigurationException{
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
			double [] reynolds = getReynoldsNumberFromAeroPerformanceMap(doc);
			double [] yaw = getYawFromAeroPerformanceMap(doc);
			double [] alpha = getAlphaFromAeroPerformanceMap(doc);
			double [][] matrix = new double[alpha.length+1][yaw.length+1];
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
			
			List<String> coefficientList = MyXMLReaderUtils.getXMLPropertiesByPath(docAeroControlSurface,
					coefficientStringPath);
			System.out.println(coefficientList);
			double[] coefficientVector = CPACSUtils.getDoubleArrayFromStringList(coefficientList);
			for (int s = 0;s<coefficientVector.length;s++) {
				System.out.println(coefficientVector[s] + ",");
			}

			int pippo = 0;
			int i = 0;
			int j = 0; //counter matrix column index
			int flag = alpha.length-1; //flag need to 
			int flag1 = alpha.length*yaw.length-1;
			int flag2 = alpha.length*yaw.length*reynolds.length-1;
			System.out.println(alpha.length+"	"+yaw.length + "	" + reynolds.length);
			int k = 0; //counter Reynolds -->3rd dimension (index List<String>)
			int l = 0; //counter Mach -->4th dimension (index List<List<String>>)
			for (int s = 0;s<coefficientVector.length;s++) {

				if (s>flag) {
//					System.out.println(flag);
					flag = flag+alpha.length;
					j = j + 1;
					i  = 0;
				}

				if (s>flag1) {
					flag1 = flag1 + alpha.length*yaw.length;
					j = 0;
					forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
					System.out.println(forceFixedMach.get(k));
					k = k + 1;
				}
				if (s>flag2) {
					flag2 = flag2 + alpha.length*yaw.length*reynolds.length;
					k = 0;
				}
				if (s<flag+1) {
					matrix[i+1][j+1]=coefficientVector[s];
					pippo = pippo+1;
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
						i++;
						}
					}
					for (int d = 0; d<alpha.length;d++) {
						for (int f = 0; f<yaw.length;f++) {
							System.out.println(matrix[d+1][f+1]+"	");
						}
						System.out.println("\n");
					}
					System.out.println("----------------------------------------------");
					forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
				}
			}			
			System.out.println("pippok is = "+pippo);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	
	public static void getCoefficientFromAeroPerformanceMapControlSurfaceFlap
	(Node aeroNodeInnerFlap, Node aeroNodeOuterFlap, List<String> forceFixedMach,
			String coefficientStringPath, Node aeroNode) 
			throws ParserConfigurationException{
		//DOC inner flap
		DocumentBuilderFactory factoryInnerFlap = DocumentBuilderFactory.newInstance();
		factoryInnerFlap.setNamespaceAware(true);
		DocumentBuilder builderInnerFlap;	
		//DOC outer flap
		DocumentBuilderFactory factoryOuterFlap = DocumentBuilderFactory.newInstance();
		factoryOuterFlap.setNamespaceAware(true);
		DocumentBuilder builderOuterFlap;		
		//DOC Aeroperformance
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(aeroNode, true);
			doc.appendChild(importedNode);
			//Inner
			builderInnerFlap = factoryInnerFlap.newDocumentBuilder();
			Document docInnerFlap = builderInnerFlap.newDocument();
			Node importedNodeInnerFlap = docInnerFlap.importNode(aeroNodeInnerFlap, true);
			docInnerFlap.appendChild(importedNodeInnerFlap);
		try {
			//Outer
			builderOuterFlap = factoryOuterFlap.newDocumentBuilder();
			Document docOuterFlap = builderOuterFlap.newDocument();
			Node importedNodeOuterFlap = docOuterFlap.importNode(aeroNodeOuterFlap, true);
			docOuterFlap.appendChild(importedNodeOuterFlap);
			double [] reynolds = getReynoldsNumberFromAeroPerformanceMap(doc);
			double [] yaw = getYawFromAeroPerformanceMap(doc);
			double [] alpha = getAlphaFromAeroPerformanceMap(doc);
			double [][] matrix = new double[alpha.length+1][yaw.length+1];
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
			//reading coefficient relative to inner flap
			List<String> coefficientListInner = MyXMLReaderUtils.getXMLPropertiesByPath(docInnerFlap,
					coefficientStringPath);
			System.out.println(coefficientListInner);
			double[] coefficientVectorInner = CPACSUtils.getDoubleArrayFromStringList(coefficientListInner);
			//reading coefficient relative to outer flap
			List<String> coefficientListOuter = MyXMLReaderUtils.getXMLPropertiesByPath(docOuterFlap,
					coefficientStringPath);
			System.out.println(coefficientListOuter);
			double[] coefficientVectorOuter = CPACSUtils.getDoubleArrayFromStringList(coefficientListOuter);
			for (int s = 0;s<coefficientVectorOuter.length;s++) {
				System.out.println(coefficientVectorOuter[s] + ",");
			}

			int pippo = 0;
			int i = 0;
			int j = 0; //counter matrix column index
			int flag = alpha.length-1; //flag need to 
			int flag1 = alpha.length*yaw.length-1;
			int flag2 = alpha.length*yaw.length*reynolds.length-1;
			System.out.println(alpha.length+"	"+yaw.length + "	" + reynolds.length);
			int k = 0; //counter Reynolds -->3rd dimension (index List<String>)
			int l = 0; //counter Mach -->4th dimension (index List<List<String>>)
			for (int s = 0;s<coefficientVectorInner.length;s++) {

				if (s>flag) {
//					System.out.println(flag);
					flag = flag+alpha.length;
					j = j + 1;
					i  = 0;
				}

				if (s>flag1) {
//					System.out.println(flag1);
					flag1 = flag1 + alpha.length*yaw.length;
//					for (int d = 0; d<alpha.length;d++) {
//						for (int f = 0; f<yaw.length;f++) {
//							System.out.println(matrix[d][f]+"	");
//						}
//						System.out.println("\n");
//					}
//					System.out.println("----------------------------------------------");

					j = 0;
					forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
					System.out.println(forceFixedMach.get(k));
					k = k + 1;
				}
				if (s>flag2) {
					flag2 = flag2 + alpha.length*yaw.length*reynolds.length;
					k = 0;
//					force.add(forceFixedMach);
//					forceFixedMach = null;
//					l = l +1;
				}
				if (s<flag+1) {
					matrix[i+1][j+1]=coefficientVectorInner[s]+coefficientVectorOuter[s];
					pippo = pippo+1;
//					System.out.println(matrix[i][j]);
					i = i + 1;
//					System.out.println("Flag value = "+j);
//					System.out.println("pippo value = "+i);

				}
				
				if (s==coefficientVectorInner.length-1) {
					i = 0;
					j = 0;
					for (int h = s+1-alpha.length*yaw.length;h<s+1;h++) {
						if (i == alpha.length) {
							j = j + 1;
							i  = 0;
						}
						if (i<alpha.length) {
						matrix[i+1][j+1]=coefficientVectorInner[h]+coefficientVectorOuter[h];
						i++;
						}

					}
					for (int d = 0; d<alpha.length;d++) {
						for (int f = 0; f<yaw.length;f++) {
							System.out.println(matrix[d+1][f+1]+"	");
						}
						System.out.println("\n");
					}
					System.out.println("----------------------------------------------");
					forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
//					force.add(forceFixedMach);
				}
			}			
			System.out.println("pippok is = "+pippo);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	
	public static void getCoefficientFromAeroPerformanceMap
	(Node aeroNode, List<String> forceFixedMach, String coefficientStringPath){
//		List<String> forceFixedMach = new ArrayList<String>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(aeroNode, true);
			doc.appendChild(importedNode);
			double [] mach = getMachNumberFromAeroPerformanceMap(doc);
			double [] reynolds = getReynoldsNumberFromAeroPerformanceMap(doc);
			double [] yaw = getYawFromAeroPerformanceMap(doc);
			double [] alpha = getAlphaFromAeroPerformanceMap(doc);
			double [][] matrix = new double[alpha.length+1][yaw.length+1];
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
				double[] coefficientVector = CPACSUtils.getDoubleArrayFromStringList(coefficientList);
				for (int s = 0;s<coefficientVector.length;s++) {
					System.out.println(coefficientVector[s] + ",");
				}
				int pippo = 0;
				int i = 0;
				int j = 0; //counter matrix column index
				int flag = alpha.length-1; //flag need to 
				int flag1 = alpha.length*yaw.length-1;
				int flag2 = alpha.length*yaw.length*reynolds.length-1;
				System.out.println(alpha.length+"	"+yaw.length + "	" + reynolds.length);
				int k = 0; //counter Reynolds -->3rd dimension (index List<String>)
				int l = 0; //counter Mach -->4th dimension (index List<List<String>>)
				for (int s = 0;s<coefficientVector.length;s++) {

					if (s>flag) {
						//					System.out.println(flag);
						flag = flag+alpha.length;
						j = j + 1;
						i  = 0;
					}

					if (s>flag1) {
						//					System.out.println(flag1);
						flag1 = flag1 + alpha.length*yaw.length;
						//					for (int d = 0; d<alpha.length;d++) {
						//						for (int f = 0; f<yaw.length;f++) {
						//							System.out.println(matrix[d][f]+"	");
						//						}
						//						System.out.println("\n");
						//					}
						//					System.out.println("----------------------------------------------");

						j = 0;
						forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
						System.out.println(forceFixedMach.get(k));
						k = k + 1;
					}
					if (s>flag2) {
						flag2 = flag2 + alpha.length*yaw.length*reynolds.length;
						k = 0;
						//					force.add(forceFixedMach);
						//					forceFixedMach = null;
						//					l = l +1;
					}
					if (s<flag+1) {
						matrix[i+1][j+1]=coefficientVector[s];
						pippo = pippo+1;
						//					System.out.println(matrix[i][j]);
						i = i + 1;
						//					System.out.println("Flag value = "+j);
						//					System.out.println("pippo value = "+i);

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
								i++;
							}

						}
						for (int d = 0; d<alpha.length;d++) {
							for (int f = 0; f<yaw.length;f++) {
								System.out.println(matrix[d+1][f+1]+"	");
							}
							System.out.println("\n");
						}
						System.out.println("----------------------------------------------");
						forceFixedMach.add(CPACSUtils.matrixDoubleToJSBSimTable2D(matrix, "	"));
						//					force.add(forceFixedMach);
					}
				}
				System.out.println("pippok is = "+pippo);

			}			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

	}
	

	public String getCpacsFilePath() {
		return _cpacsFilePath;
	}

	public ReadStatus getStatus() {
		return _status;
	}

} // end of class

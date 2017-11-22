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
	public double[][] getAeroPerformanceMap(Node aeroNode){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			String[] s1 = null; // for list to string
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			Node importedNode = doc.importNode(aeroNode, true);
			doc.appendChild(importedNode);
			List<String>machNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//machNumber/text()");
			double[] machNumber = CPACSUtils.getDoubleArrayFromStringList(machNumberList);
			List<String> reynoldsNumberList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//reynoldsNumber/text()");
			double[] reynoldsNumber = CPACSUtils.getDoubleArrayFromStringList(reynoldsNumberList);
			List<String> yawAngleList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//angleOfYaw/text()");
			double[] yawAngle = CPACSUtils.getDoubleArrayFromStringList(yawAngleList);
			List<String> angleOfAttackList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//angleOfAttack/text()");
			double[] angleOfAttack = CPACSUtils.getDoubleArrayFromStringList(angleOfAttackList);

			//Read data from aeroperformance map
			List<String> cfxList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//cfx/text()");
			double[] cfx = CPACSUtils.getDoubleArrayFromStringList(cfxList);
			List<String> cfyList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//cfy/text()");
			double[] cfy = CPACSUtils.getDoubleArrayFromStringList(cfyList);
			List<String> cfzList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//cfx/text()");
			double[] cfz = CPACSUtils.getDoubleArrayFromStringList(cfzList);
			List<String> cmxList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//cmx/text()");
			double[] cmx = CPACSUtils.getDoubleArrayFromStringList(cmxList);
			List<String> cmyList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//cmy/text()");
			double[] cmy = CPACSUtils.getDoubleArrayFromStringList(cmyList);
			List<String> cmzList = MyXMLReaderUtils.getXMLPropertiesByPath(doc,
					"//cmz/text()");
			double[] cmz = CPACSUtils.getDoubleArrayFromStringList(cmzList);
			int machNumberDimension = machNumber.length;
			int reynoldsNumberDimension = reynoldsNumber.length;
			int yawAngleDimension = yawAngle.length;
			int angleOfAttackDimension = angleOfAttack.length;
			int numberOfCalculation = machNumberDimension*reynoldsNumberDimension*yawAngleDimension*angleOfAttackDimension;
			int numberOfCalculation1 = reynoldsNumberDimension*yawAngleDimension*angleOfAttackDimension;
			int numberOfCalculation2 = yawAngleDimension*angleOfAttackDimension;
			int zeroAlphaPosition = 0;
			int zeroYawPosition = 0;
			double[][] aeroData = new double[9][numberOfCalculation];
//			double []cd = new double[numberOfCalculation];
//			double []cy = new double[numberOfCalculation];
//			double []cl = new double[numberOfCalculation];
//			double []cdAlpha = new double[numberOfCalculation];
//			double []cyBeta = new double[numberOfCalculation];
//			double []clAlpha = new double[numberOfCalculation];
			int aoaIndex = 0;
			int yawIndex = 0;
			for (int i = 0;i<numberOfCalculation;i++) {
				double a = cfx[i];
				double a1 = Math.cos(Math.toRadians(angleOfAttack[aoaIndex]));
				double a2 = Math.sin(Math.toRadians(angleOfAttack[aoaIndex]));
				double b = cfy[i];
				double b1 = Math.cos(Math.toRadians(yawAngle[yawIndex]));
				double b2 = Math.sin(Math.toRadians(yawAngle[yawIndex]));
				double c = cfz[i];
//				cd[i] = a1*b1*a - b2*b +a2*b1*c; 
//				cy[i] = a1*b2*a + b1*b +a2*b2*c;  
//				cl[i] = -a2*a + a1*c;
				aeroData[0][i] = a1*b1*a - b2*b +a2*b1*c; 
				aeroData[2][i] = a1*b2*a + b1*b +a2*b2*c;  
				aeroData[4][i] = -a2*a + a1*c;
				if (angleOfAttack[aoaIndex] != 0.0) {
//					cdAlpha[i] = cd[i]/(Math.toRadians(angleOfAttack[aoaIndex]));
//					clAlpha[i] = cl[i]/(Math.toRadians(angleOfAttack[aoaIndex]));
//					cmy[i] = cmy[i]/(Math.toRadians(angleOfAttack[aoaIndex]));
					aeroData[1][i] = aeroData[1][i]/(Math.toRadians(angleOfAttack[aoaIndex]));
					aeroData[5][i] = aeroData[5][i]/(Math.toRadians(angleOfAttack[aoaIndex]));
					aeroData[7][i] = cmy[i]/(Math.toRadians(angleOfAttack[aoaIndex]));
				}
				if (yawAngle[yawIndex] != 0.0) {
					aeroData[3][i] = aeroData[3][i]/(Math.toRadians(yawAngle[yawIndex]));
					aeroData[6][i] = cmx[i]/(Math.toRadians(yawAngle[yawIndex]));
					aeroData[8][i] = cmz[i]/(Math.toRadians(yawAngle[yawIndex]));
				}					
				aoaIndex++;
				if(aoaIndex == angleOfAttackDimension) {
					aoaIndex = 0;
					yawIndex++;
				}
				if(yawIndex == yawAngleDimension) {
					yawIndex = 0;
				}
			}
			return aeroData;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 
	 * @param controlSurfaceUID UID of the control surface
	 * @param pathInTheCPACS path in the CPACS of the aerodynamic analysis of the control surface
	 * @param typeControlSurface a--> latero-directional 
	 * 							 b--> longitudinal
	 * 									
	 * @return
	 */
	
	// TO DO
	public List<Double> getControlSurfaceDragCoefficientEffect(String controlSurfaceUID, String pathInTheCPACS, String typeControlSurface){
		List<Double> aerodynamicDerivativeList = null ; 
		double[] aerodynamicDerivativeVector = new double [6];
		List<Double> machNumber =  _jpadXmlReader.readArrayDoubleFromXMLSplit(
				pathInTheCPACS+"/machNumber");
		List<Double> reynoldsNumber =  _jpadXmlReader.readArrayDoubleFromXMLSplit(
				pathInTheCPACS+"/reynoldsNumber");
		List<Double> yawAngle =  _jpadXmlReader.readArrayDoubleFromXMLSplit(
				pathInTheCPACS+"/angleOfYaw");
		List<Double> angleOfAttack =  _jpadXmlReader.readArrayDoubleFromXMLSplit(
				pathInTheCPACS+"/angleOfAttack");
		List<Double> deflection =  _jpadXmlReader.readArrayDoubleFromXMLSplit(
				pathInTheCPACS+"/angleOfAttack");
		NodeList controlSurfaceList = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(),pathInTheCPACS+"/controlSurfaces/controlSurface");
		
		int machNumberDimension = machNumber.size();
		int reynoldsNumberDimension = reynoldsNumber.size();
		int yawAngleDimension = yawAngle.size();
		int angleOfAttackDimension = angleOfAttack.size();
		int numberOfCalculation = machNumberDimension*reynoldsNumberDimension*yawAngleDimension*angleOfAttackDimension;
		int numberOfCalculation1 = reynoldsNumberDimension*yawAngleDimension*angleOfAttackDimension;
		int numberOfCalculation2 = yawAngleDimension*angleOfAttackDimension;
		int controlSurfaceNumber = controlSurfaceList.getLength();
		int zeroAlphaPosition = 0;
		int zeroYawPosition = 0;
		for (int i = 0;i<angleOfAttackDimension;i++) {
			if (angleOfAttack.get(i)==0) {
				zeroAlphaPosition = i;
			}
		}
		for (int i = 0;i<yawAngleDimension;i++) {
			if (yawAngle.get(i)==0) {
				zeroYawPosition = i;
			}
		}
		
		for (int i=0;i<controlSurfaceList.getLength();i++) {
			
			Node nodeSystemControlSurface  = controlSurfaceList.item(i);
			Element systemElementControlSurface = (Element) nodeSystemControlSurface;

		}
		return aerodynamicDerivativeList;
	}

	public String getCpacsFilePath() {
		return _cpacsFilePath;
	}

	public ReadStatus getStatus() {
		return _status;
	}

} // end of class

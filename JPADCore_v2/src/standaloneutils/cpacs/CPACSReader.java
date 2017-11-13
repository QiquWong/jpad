package standaloneutils.cpacs;

import java.util.ArrayList;
import java.util.List;

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
	 * 
	 * @param wingUID Wing UID in the CPACS
	 * @param axis axis  which want evaluate distance
	 * @return Return the leading edge of the mean aerodynamic chord from aircraft nose
	 * @throws TiglException
	 */
//	public double getMeanChordLeadingEdge(String wingUID, String axis) throws TiglException {
//		int wingIndex = getWingIndexZeroBased(wingUID);
//		double wingSpan = getWingSpan(wingUID);		
//		String wingRootLeadingEdgeString = _jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/vehicles/aircraft/model/wings/wing["+ (wingIndex + 1) + "]/transformation/translation/"
//				+ axis);
//		double wingRootLeadingEdge = Double.parseDouble(wingRootLeadingEdgeString);	
//		NodeList wingSectionElements = MyXMLReaderUtils.getXMLNodeListByPath(
//				_jpadXmlReader.getXmlDoc(), 
//				"cpacs/vehicles/aircraft/model/wings/wing[" + (wingIndex) + "]/sections/section");		
//		int lastWingElementIndex = wingSectionElements.getLength();
//		String wingTipLeadingEdgeString = _jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/vehicles/aircraft/model/wings/wing[" + (wingIndex) + "]/sections/"
//						+ "section["+lastWingElementIndex+"]/elements/element/transformation/scaling/"
//						+ axis);
//		double wingTipLeadingEdge = Double.parseDouble(wingTipLeadingEdgeString);
//		//Definition Sweep Angle
//		NodeList wingSectionElementPosition = MyXMLReaderUtils.getXMLNodeListByPath(
//				_jpadXmlReader.getXmlDoc(), 
//				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/positionings/positioning");		
//		int lastWingElementPositionIndex = wingSectionElementPosition.getLength()-1;
//		String tanSweepAngleString = _jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/"
//						+ "positionings/positioning["+lastWingElementPositionIndex+"]/sweepAngle");
//		double tanSweepAngle = Double.parseDouble(tanSweepAngleString);			
//		double taperRatio = wingTipLeadingEdge/wingRootLeadingEdge;
//
//		return 2.0f/3.0f*wingSpan*(1 + 2*taperRatio)/(1 + taperRatio)*tanSweepAngle;
//	}
	
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
	 * @param axis axis  which want evaluate wing position
	 * @return Wing Position in the axis direction (i.e x y or z)
	 * @throws TiglException
	 */


//	public double getWingRootLeadingEdge(String uIDInput,String axis) throws TiglException {
//		int wingIndex = getWingIndexZeroBased(uIDInput);
//		System.out.println("wingUID is = "+uIDInput);
//		System.out.println("wingIndex is = "+wingIndex);
//		String wingRootLeadingEdgeString = _jpadXmlReader.getXMLPropertyByPath(
//				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/transformation/translation/x");
//		System.out.println("--------------------------------");
//		System.out.println("wingRootLeadingEdgeString = "+wingRootLeadingEdgeString);
//		System.out.println("--------------------------------");
//
//		return Double.parseDouble(wingRootLeadingEdgeString);	
//	}
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
	
	
	
	public Double[] getVectorPositionNodeTank(int i) throws TiglException {
		Double [] tankPositionVector = null;
		NodeList tankNumberElement = MyXMLReaderUtils.getXMLNodeListByPath(
				_importDoc, 
				"/cpacs/vehicles/aircraft/model/analyses/weightAndBalance/operationalCases/"
						+ "operationalCase[" + i + "]/mFuel/fuelInTanks/fuelInTank");
		for (int j = 1; j < tankNumberElement.getLength(); j++) {
				List<String> props = new ArrayList<>();
				props = MyXMLReaderUtils.getXMLPropertiesByPath(
						_importDoc, 
						"/cpacs/vehicles/aircraft/model/analyses/weightAndBalance/operationalCases"
								+ "/operationalCase["+ i +"]/mFuel/fuelInTanks/fuelInTank["
								+ j +"]/coG/x/text()"
						);

				System.out.println("\t...: " + props.get(0) + " size: "+ props.size());
		}		
		return tankPositionVector;
	} 



	
	
	
	
	
	

	
	

//	/**
//	 * 
//	 * @param pathInTheCpacs Path in the CPACS of the system
//	 * @return return as List : SystemUID, Comand Position and deflection value
//	 * 		   every system have 3 position in the list, for example :
//	 * 		   1->Aileron 2->elevator:
//	 * 		   AileronUID getSystemParameter.get(0)
//	 * 		   Aileron Comand Position getSystemParameter.get(1)
//	 * 		   Aileron deflection value getSystemParameter.get(2)
//	 * 		   ElevatorUID getSystemParameter.get(3)
//	 * 		   Elevator Comand Position getSystemParameter.get(4)
//	 * 		   Elevator deflection value getSystemParameter.get(5)     
//	 */
//	public List<String> getSystemParameter(String pathInTheCpacs){
//		NodeList SystemList = MyXMLReaderUtils.getXMLNodeListByPath(
//				_jpadXmlReader.getXmlDoc(),pathInTheCpacs);	
//		String UIDControlSurface;
//		List<String> ReturnSystem = new ArrayList<String>();
//		for (int i=1;i<SystemList.getLength()+1;i++) {
//			Node nodeSystem  = SystemList.item(i); // .getNodeValue();
//			Element SystemElement = (Element) nodeSystem;
//			UIDControlSurface = SystemElement.getAttribute("uID");
//
//			String InputSystem = _jpadXmlReader.getXMLPropertyByPath(
//					"cpacs/vehicles/aircraft/model/systems/controlDistributors"
//							+ "/controlDistributor["+i+"]/controlElements/controlElement/commandInputs");
//			String RelativeDeflection = _jpadXmlReader.getXMLPropertyByPath(
//					"cpacs/vehicles/aircraft/model/systems/controlDistributors"
//							+ "/controlDistributor["+i+"]/controlElements/controlElement/relativeDeflection");
//			ReturnSystem.add(UIDControlSurface);
//			ReturnSystem.add(InputSystem);					
//			ReturnSystem.add(RelativeDeflection);
//		}
//		return returnSystem;
//}
	public double[][] getControlSurfaceRotation(int wingIndex, String controlSurfaceUID, String controlSuraceSystemUID){
		double [][] matrix = new double [2][3];
		NodeList systemList = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(),
				"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+
				"]/componentSegments/componentSegment/"
				+ "controlSurfaces/trailingEdgeDevices/trailingEdgeDevice");	
		for (int i=1;i<systemList.getLength()+1;i++) {
			Node nodeSystem  = systemList.item(i); // .getNodeValue();
			Element systemElement = (Element) nodeSystem;
			if (systemElement.getAttribute("uID")==controlSurfaceUID) {
				NodeList systemDeflectionList = MyXMLReaderUtils.getXMLNodeListByPath(
						_jpadXmlReader.getXmlDoc(),
						"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/componentSegments"
						+ "/componentSegment/controlSurfaces/trailingEdgeDevices/trailingEdgeDevice/path/steps/step");
					String relativeDeflactionString =  _jpadXmlReader.getXMLPropertyByPath(
							"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/componentSegments/"
							+ "componentSegment/controlSurfaces/trailingEdgeDevices/trailingEdgeDevice/"
							+ "path/steps/step["+0+"]/relDeflection");
					matrix[0][0] = Double.parseDouble(relativeDeflactionString);

					String relativeDeflactionStringEnd =  _jpadXmlReader.getXMLPropertyByPath(
							"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/componentSegments/"
							+ "componentSegment/controlSurfaces/trailingEdgeDevices/trailingEdgeDevice/"
							+ "path/steps/step["+systemDeflectionList.getLength()+"]/relDeflection");
					matrix[1][0] = Double.parseDouble(relativeDeflactionStringEnd);

					String absoluteDeflactionString =  _jpadXmlReader.getXMLPropertyByPath(
							"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/componentSegments/"
							+ "componentSegment/controlSurfaces/trailingEdgeDevices/hingeLineRotation/"
							+ "path/steps/step["+0+"]/relDeflection");
					matrix[0][1] = Double.parseDouble(absoluteDeflactionString);

					String absoluteDeflactionStringEnd =  _jpadXmlReader.getXMLPropertyByPath(
							"cpacs/vehicles/aircraft/model/wings/wing["+wingIndex+"]/componentSegments/"
							+ "componentSegment/controlSurfaces/trailingEdgeDevices/trailingEdgeDevice/"
							+ "path/steps/step["+systemDeflectionList.getLength()+"]/hingeLineRotation");
					matrix[1][1] = Double.parseDouble(absoluteDeflactionStringEnd);	

					double[] pilotCommand = getControlSurfacePilotCommand(controlSurfaceUID, controlSuraceSystemUID);
					matrix[0][2] = pilotCommand[0];
				    matrix[1][2] = pilotCommand[1];

			}
		}
		return matrix;
}		
		
	public double[] getControlSurfacePilotCommand(String controlSurfaceUID, String controlSuraceSystemUID){
		double [] pilotCommand = new double [2]; 
		NodeList systemList = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(),"cpacs/vehicles/aircraft/model/systems/controlDistributors");	
		for (int i=1;i<systemList.getLength()+1;i++) {
			Node nodeSystem  = systemList.item(i); // .getNodeValue();
			Element systemElement = (Element) nodeSystem;
			String controlSurfaceUIDCheck = systemElement.getAttribute("uID");
			if(controlSuraceSystemUID == controlSurfaceUIDCheck) {
				NodeList systemListControlSurfaceUID = MyXMLReaderUtils.getXMLNodeListByPath(
						_jpadXmlReader.getXmlDoc(),"cpacs/vehicles/aircraft/model/systems/"
								+ "controlDistributors/controlDistributor["+i+"]/controlElements/controlElement");
				for (int j =1;j<systemListControlSurfaceUID.getLength()+1;j++) {
					Node nodeSystemControlSurface  = systemList.item(i);
					Element systemElementControlSurface = (Element) nodeSystem;
					String CheckControlSurfaceUID = systemElement.getAttribute("uID");
					if (controlSurfaceUID == CheckControlSurfaceUID) {
						List<Double> pilotCommandList = _jpadXmlReader.readArrayDoubleFromXMLSplit(
								"cpacs/vehicles/aircraft/model/systems/controlDistributors/controlDistributor["+i+"]"
										+ "/controlElements/controlElement["+j+"]/commandInputs");
						pilotCommand[0]=pilotCommandList.get(0);
						pilotCommand[1]=pilotCommandList.get(pilotCommandList.size());
					}			
				}
			}			
		}
		return pilotCommand;
	}
	
	public String getCpacsFilePath() {
		return _cpacsFilePath;
	}

	public ReadStatus getStatus() {
		return _status;
	}

} // end of class

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
	 * @param WingIndex Wing index in the CPACS, remember CPAC definition start from 0
	 * @return  Wing  area (CPACS define wing also canard, horizontal tail, vertical tail)
	 * @throws TiglException
	 */
	public double getWingReferenceArea(int WingIndex) throws TiglException {
		return _config.wingGetReferenceArea(WingIndex,_config.wingGetSymmetry(WingIndex));

	}		

	public double getWingMeanAerodynamicChord(String wingUID) throws TiglException {
		DoubleByReference mac   = new DoubleByReference();
		DoubleByReference mac_x = new DoubleByReference();
		DoubleByReference mac_y = new DoubleByReference();
		DoubleByReference mac_z = new DoubleByReference();
		if (TiglNativeInterface.tiglWingGetMAC(_config.getCPACSHandle(), wingUID, mac, mac_x, mac_y, mac_z) == 0) {
			return mac.getValue();
		}
		else
			return 0;
	}		

	/**
	 * 
	 * @param WingUID Wing UID in the CPACS
	 * @return  Wing Index position in the CPACS (CPACS define wing also canard, horizontal tail, vertical tail)
	 * @throws TiglException
	 */
	public int getWingIndex(String WingUID) throws TiglException {
		NodeList wingsNodes = getWingList();
		int WingIndex = 0;
		for (int i = 0; i< wingsNodes.getLength();i++) {
			Node nodeWing  = wingsNodes.item(i); // .getNodeValue();
			Element elementWing = (Element) nodeWing;
			if (elementWing.getAttribute("uID")==WingUID) {
				WingIndex = i; //Wing Index in the CPACS start from 1, in JPAD start from 0
			}
		}
		return WingIndex;

	}	
	/**
	 * 
	 * @param WingUID Wing UID in the CPACS
	 * @param axis axis  which want evaluate distance
	 * @return Return the leading edge of the mean aerodynamic chord from aircraft nose
	 * @throws TiglException
	 */
	public double getMeanChordLeadingEdge(String WingUID,String axis) throws TiglException {
		int WingIndex = getWingIndex(WingUID);
		double WingSpan = getWingSpan(WingUID);		
		String WingRootLeadingEdgeString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/transformation/translation/"+axis);
		double WingRootLeadingEdge = Double.parseDouble(WingRootLeadingEdgeString);	

		NodeList WingSectionElement = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/sections");		
		int LastWingElementIndex = WingSectionElement.getLength()-1;

		String WingTipLeadingEdgeString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/sections/"
						+ "section["+LastWingElementIndex+"]/elements/element/transformation/scaling/"+axis);
		double WingTipLeadingEdge = Double.parseDouble(WingTipLeadingEdgeString);
		//Definition Sweep Angle
		NodeList WingSectionElementPosition = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/positionings");		
		int LastWingElementPositionIndex = WingSectionElementPosition.getLength()-1;
		String TanSweepAngleString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/"
						+ "positionings/positioning["+LastWingElementPositionIndex+"]/sweepAngle");
		double TanSweepAngle = Double.parseDouble(TanSweepAngleString);			
		double TaperRatio = WingTipLeadingEdge/WingRootLeadingEdge;

		return 2/3*WingSpan*(1+2*TaperRatio)/(1+TaperRatio)*TanSweepAngle;
	}
	/**
	 * 
	 * @return Position of the Gravity center respect to empty weight, given as a vector where
	 * 0 --> x-position
	 * 1 --> y-position
	 * 3 --> z-position
	 * @throws TiglException
	 */
	public double[] getGravityCenterPosition() throws TiglException {
		double[] GravityCenterPosition;
		GravityCenterPosition= new double [3]; 
		String XpositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/mOEM/mEM/massDescription/location/x");
		GravityCenterPosition[0] = Double.parseDouble(XpositionGCString);			

		String YpositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/mOEM/mEM/massDescription/location/y");
		GravityCenterPosition[1] = Double.parseDouble(YpositionGCString);			

		String ZpositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/analyses/massBreakdown/mOEM/mEM/massDescription/location/z");
		GravityCenterPosition[2] = Double.parseDouble(ZpositionGCString);			


		return GravityCenterPosition;
	}	
	/**
	 * 
	 * @param pathInTheCpacs Path in the cpacs
	 * @return x,y,z value of the pathInTheCpacs parameter ( i.e scaling, rotation, translation)
	 * @throws TiglException
	 */

	public double[] getVectorPosition(String pathInTheCpacs) throws TiglException {
		double[] VectorCenterPosition;
		VectorCenterPosition= new double [3]; 
		String XpositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/x");
		VectorCenterPosition[0] = Double.parseDouble(XpositionGCString);			

		String YpositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/y");
		VectorCenterPosition[1] = Double.parseDouble(YpositionGCString);			

		String ZpositionGCString = _jpadXmlReader.getXMLPropertyByPath(
				pathInTheCpacs+"/z");
		VectorCenterPosition[2] = Double.parseDouble(ZpositionGCString);			


		return VectorCenterPosition;
	}	
	/**
	 * 
	 * @param WingUID Wing UID in the CPACS
	 * @param axis axis  which want evaluate wing position
	 * @return Wing Position in the axis direction (i.e x y or z)
	 * @throws TiglException
	 */


	public double getWingRootLeadingEdge(String WingUID,String axis) throws TiglException {
		int WingIndex = getWingIndex(WingUID);
		String WingRootLeadingEdgeString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/transformation/translation/"+axis);
		return Double.parseDouble(WingRootLeadingEdgeString);	
	}
	/**
	 * 
	 * @param WingUID Wing UID in the CPACS
	 * @return Tanget of the Sweep Angle
	 * @throws TiglException
	 */

	public double getWingSweep(String WingUID) throws TiglException {
		int WingIndex = getWingIndex(WingUID);
		NodeList WingSectionElementPosition = MyXMLReaderUtils.getXMLNodeListByPath(
				_jpadXmlReader.getXmlDoc(), 
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/positionings");		
		int LastWingElementPositionIndex = WingSectionElementPosition.getLength()-1;
		String TanSweepAngleString = _jpadXmlReader.getXMLPropertyByPath(
				"cpacs/vehicles/aircraft/model/wings/wing["+WingIndex+"]/"
						+ "positionings/positioning["+LastWingElementPositionIndex+"]/sweepAngle");
		return Double.parseDouble(TanSweepAngleString);			
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
	




	public ReadStatus getStatus() {
		return _status;
	}

} // end of class

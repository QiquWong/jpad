package standaloneutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import de.dlr.sc.tigl.CpacsConfiguration;
import de.dlr.sc.tigl.Tigl;
import de.dlr.sc.tigl.TiglException;
import de.dlr.sc.tigl.TiglNativeInterface;
import de.dlr.sc.tigl.TiglReturnCode;

/*
 * This class wraps the functionalities provided by de.dlr.sc.tigl.* classes
 */
public class CPACSReader {
	
	/**
     * Central logger instance.
     */
    protected static final Log LOGGER = LogFactory.getLog(CpacsConfiguration.class);	
	
	private Document _importDoc;
	private XPathFactory _xpathFactoryImport;

	private String _cpacsFilePath;
	CpacsConfiguration _config;
	
	private int _fuselageCount;
	private int _wingCount;
	
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
			
			_xpathFactoryImport = XPathFactory.newInstance();
			_importDoc = MyXMLReaderUtils.importDocument(_cpacsFilePath);

			_config = Tigl.openCPACSConfiguration(_cpacsFilePath,"");
			
			IntByReference fuselageCount = new IntByReference(0);
			TiglNativeInterface.tiglGetFuselageCount(_config.getCPACSHandle(), fuselageCount);
			_fuselageCount = fuselageCount.getValue();

			IntByReference wingCount = new IntByReference(0);
			TiglNativeInterface.tiglGetWingCount(_config.getCPACSHandle(), wingCount);
			_wingCount = wingCount.getValue();
			
			
		} catch (TiglException e) {
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
	
} // end of class

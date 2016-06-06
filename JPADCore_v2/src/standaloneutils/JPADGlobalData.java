package standaloneutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import aircraft.OperatingConditions;
import aircraft.calculators.ACAnalysisManager;
import aircraft.components.Aircraft;
import aircraft.components.fuselage.Fuselage;
import cad.occ.OCCFXViewableCAD;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import standaloneutils.customdata.MyXmlTree;

public class JPADGlobalData {

	public static MyXmlTree theXmlTree = null;
	public static List<ACAnalysisManager> _theAnalysisList = new ArrayList<ACAnalysisManager>();

	private static Aircraft theCurrentAircraft = null;
	public static List<Aircraft> _theAircraftList = new ArrayList<Aircraft>();
	private static String currentAircraftName = "AIRCRAFT";
	public static ObjectProperty<Aircraft> theCurrentAircraftProperty = new SimpleObjectProperty<Aircraft>();
	public static ObjectProperty<Fuselage> theCurrentFuselageProperty = new SimpleObjectProperty<Fuselage>();
	
	public static ObservableMap<OCCFXViewableCAD,String> viewablesMap = FXCollections.observableHashMap();
	
	public static OperatingConditions _theCurrentOperatingConditions;
	public static ACAnalysisManager theCurrentAnalysis = null;
	public static final Logger log = Logger.getLogger("log");
	
	public static final Map<Object, String> imagesMap = new HashMap<Object, String>();

	public static Map<Object, String> get_imagesMap() {
		return imagesMap;
	}
	
	public static String getCurrentAircraftName() {
		return currentAircraftName;
	}

	public static void setCurrentAircraftName(String currentAircraftName) {
		JPADGlobalData.currentAircraftName = currentAircraftName;
	}

	public static MyXmlTree getTheXmlTree() {
		return theXmlTree;
	}

	public static void setTheXmlTree(MyXmlTree xmlTree) {
		theXmlTree = xmlTree;
	}

	public static Aircraft getTheCurrentAircraft() {
		return theCurrentAircraft;
	}

	public static void setTheCurrentAircraft(Aircraft _theCurrentAircraft) {
		theCurrentAircraft = _theCurrentAircraft;
	}
	
	public static void deleteTheCurrentAircraft() {
		theCurrentAircraft = null;
	}

	public static void setTheCurrentFuselage(Fuselage fuselage) {
		//if ( ADOPT_GUI.aircraftInMemoryProperty.get() ) {
			JPADGlobalData.theCurrentFuselageProperty.set(fuselage);
			if (fuselage != null) {
				// ADOPT_GUI.fuselageInMemoryProperty.set(true);
			}
		//}
	}

	public static void setTheCurrentAircraftInMemory(Aircraft ac) {
		
		// The whole aircraft
		setTheCurrentAircraft(ac);
		JPADGlobalData.theCurrentAircraftProperty.set(getTheCurrentAircraft());
//		ADOPT_GUI.aircraftInMemoryProperty.set(getTheCurrentAircraft() != null);
//		
//		// The fuselage
//		if ( ADOPT_GUI.aircraftInMemoryProperty.get() ) {
//			JPADGlobalData.theCurrentFuselageProperty.set(getTheCurrentAircraft().get_fuselage());
//			ADOPT_GUI.fuselageInMemoryProperty.set(
//				JPADGlobalData.theCurrentFuselageProperty.get() != null
//			);
//		} else {
//			JPADGlobalData.theCurrentFuselageProperty = null;
//			ADOPT_GUI.fuselageInMemoryProperty.set(false);
//		}
	}

	public static ACAnalysisManager getTheCurrentAnalysis() {
		return theCurrentAnalysis;
	}

	public static void setTheCurrentAnalysis(ACAnalysisManager analysis) {
		theCurrentAnalysis = analysis;
	}

	public static List<Aircraft> get_theAircraftList() {
		return _theAircraftList;
	}

	public static void set_theAircraftList(List<Aircraft> theAircraftList) {
		_theAircraftList = theAircraftList;
	}

	public static List<ACAnalysisManager> get_theAnalysisList() {
		return _theAnalysisList;
	}

	public static void set_theAnalysisList(List<ACAnalysisManager> theAnalysisList) {
		_theAnalysisList = theAnalysisList;
	}

	public static OperatingConditions getTheCurrentOperatingConditions() {
		return _theCurrentOperatingConditions;
	}

	public static void set_theCurrentOperatingConditions(OperatingConditions theCurrentOperatingConditions) {
		_theCurrentOperatingConditions = theCurrentOperatingConditions;
	}

	public static Logger getLOG() {
		return log;
	}

}

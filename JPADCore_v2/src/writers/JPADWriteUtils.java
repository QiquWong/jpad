package writers;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.sun.corba.se.impl.oa.poa.AOMEntry;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.Airfoil;
import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACBalanceManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.calculators.ACWeightsManager;
import aircraft.calculators.costs.Costs;
import aircraft.components.Aircraft;
import aircraft.components.CabinConfiguration;
import aircraft.components.FuelTank;
import aircraft.components.LandingGears;
import aircraft.components.Systems;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.nacelles.Nacelles;
import aircraft.components.powerPlant.Engine;
import aircraft.components.powerPlant.PowerPlant;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADGlobalData;
import standaloneutils.customdata.MyXmlTree;

public class JPADWriteUtils {

	public static String createImagesFolder(String folderName) {

		String currentFolderImagesDirectory = MyConfiguration.currentDirectory
				+ File.separator
				+ folderName 
				+ File.separator;

		JPADStaticWriteUtils.createNewFolder(currentFolderImagesDirectory);

		return currentFolderImagesDirectory;
	}

	public static void buildXmlTree() {
		buildXmlTree(JPADGlobalData.getTheCurrentAircraft(), JPADGlobalData.getTheCurrentOperatingConditions());
	}


	/**
	 * This map MUST be initialized to handle 
	 * input from xml file using java reflection.
	 * The map associates components with the name of 
	 * their paragraph in the xml file
	 * 
	 * @author LA 
	 * @param aircraft
	 * @param conditions
	 */
	public static void buildXmlTree(Aircraft aircraft, OperatingConditions conditions) {

		File file = new File(MyConfiguration.currentDirectoryString + "xmlConfig.xml");

		try {

			JPADGlobalData.setTheXmlTree(new MyXmlTree());
			JPADGlobalData.getTheXmlTree().add(conditions, 1, "Operating_Conditions", OperatingConditions.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getCabinConfiguration(), 2, "Configuration", aircraft.getCabinConfiguration().getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getTheWeights(), 2, "Weights", ACWeightsManager.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getTheBalance(), 2, "Balance", ACBalanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getThePerformance(), 2, "Performances", ACPerformanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getTheCosts(), 2, "Costs", Costs.getId());
			
			JPADGlobalData.getTheXmlTree().add(aircraft.getFuselage(), 2, "Fuselage", aircraft.getFuselage().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getWing(), 2, "Wing", aircraft.getWing().getId());
			addAirfoilsToXML(aircraft.getWing(), aircraft.getWing().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getHTail(), 2, "HTail", aircraft.getHTail().getId());
			addAirfoilsToXML(aircraft.getHTail(), aircraft.getHTail().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getVTail(), 2, "VTail", aircraft.getVTail().getId());
			addAirfoilsToXML(aircraft.getVTail(), aircraft.getVTail().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getCanard(), 2, "Canard", aircraft.getCanard().getId());
			addAirfoilsToXML(aircraft.getCanard(), aircraft.getCanard().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getNacelles(), 2, "Nacelles", aircraft.getNacelles().getId());
			addNacellesToXML(aircraft.getNacelles(), aircraft.getNacelles().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getFuelTank(), 2, "Fuel_tank", aircraft.getFuelTank().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getPowerPlant(), 2, "PowerPlant", aircraft.getPowerPlant().getId());
			addEnginesToXML(aircraft.getPowerPlant(), aircraft.getPowerPlant().getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.getSystems(), 2, "Systems", aircraft.getSystems().getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getLandingGears(), 2, "LandingGear", aircraft.getLandingGears().getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.getTheAerodynamics(), 2, "Aircraft_Aerodynamics", "24");

			//			try {
			//				JAXBContext jaxbContext = JAXBContext.newInstance(MyXmlTree.class);
			//				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			//
			//				// output pretty printed
			//				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//				jaxbMarshaller.marshal(ADOPT_GUI.getTheXmlTree(), file);
			//
			//			} catch (JAXBException e) {
			//				e.printStackTrace();
			//			}

		} catch (NullPointerException ex) {

			//			JAXBContext jaxbContext;
			//			try {
			//				jaxbContext = JAXBContext.newInstance(MyXmlTree.class);
			//				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			//				ADOPT_GUI.setTheXmlTree((MyXmlTree) jaxbUnmarshaller.unmarshal(file));
			//			} catch (JAXBException e) {
			//				e.printStackTrace();
			//			}
		}

	}

	public static void buildXmlTreeNew() {

		File file = new File(MyConfiguration.currentDirectoryString + "xmlConfig.xml");

		try {

			JPADGlobalData.setTheXmlTree(new MyXmlTree());
			JPADGlobalData.getTheXmlTree().add(OperatingConditions.class, 1, "Operating_Conditions", OperatingConditions.getId());
			JPADGlobalData.getTheXmlTree().add(CabinConfiguration.class, 2, "Configuration", CabinConfiguration.getId());
			JPADGlobalData.getTheXmlTree().add(ACWeightsManager.class, 2, "Weights", ACWeightsManager.getId());
			JPADGlobalData.getTheXmlTree().add(ACBalanceManager.class, 2, "Balance", ACBalanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(ACPerformanceManager.class, 2, "Performances", ACPerformanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(Fuselage.class, 2, "Fuselage", Fuselage.getId());

			JPADGlobalData.getTheXmlTree().add(LiftingSurface.class, 2, "Wing", Wing.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().getWing(), Wing.getId());

			JPADGlobalData.getTheXmlTree().add(LiftingSurface.class, 2, "HTail", HTail.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().getHTail(), HTail.getId());

			JPADGlobalData.getTheXmlTree().add(LiftingSurface.class, 2, "VTail", VTail.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().getVTail(), VTail.getId());

			JPADGlobalData.getTheXmlTree().add(LiftingSurface.class, 2, "Canard", Canard.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().getCanard(), Canard.getId());	

			JPADGlobalData.getTheXmlTree().add(NacelleCreator.class, 2, "Nacelle", NacelleCreator.getId());
			JPADGlobalData.getTheXmlTree().add(FuelTank.class, 2, "Fuel_tank", FuelTank.getId());
			JPADGlobalData.getTheXmlTree().add(PowerPlant.class, 2, "PowerPlant", PowerPlant.getId());
			JPADGlobalData.getTheXmlTree().add(Systems.class, 2, "Systems", Systems.getId());
			JPADGlobalData.getTheXmlTree().add(LandingGears.class, 2, "LandingGear", LandingGears.getId());
			JPADGlobalData.getTheXmlTree().add(ACAerodynamicsManager.class, 2, "Aircraft_Aerodynamics", "24");

			//			try {
			//				JAXBContext jaxbContext = JAXBContext.newInstance(MyXmlTree.class);
			//				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			//
			//				// output pretty printed
			//				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			//				jaxbMarshaller.marshal(ADOPT_GUI.getTheXmlTree(), file);
			//
			//			} catch (JAXBException e) {
			//				e.printStackTrace();
			//			}

		} catch (NullPointerException ex) {

			//			JAXBContext jaxbContext;
			//			try {
			//				jaxbContext = JAXBContext.newInstance(MyXmlTree.class);
			//				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			//				ADOPT_GUI.setTheXmlTree((MyXmlTree) jaxbUnmarshaller.unmarshal(file));
			//			} catch (JAXBException e) {
			//				e.printStackTrace();
			//			}
		}

	}

	private static void addAirfoilsToXMLNew(LiftingSurface liftingSurface, String fatherId) {
		if (liftingSurface != null) {
			for (int k=0; k < liftingSurface.getAirfoilList().size(); k++) {
				Airfoil tempAirfoil = liftingSurface.getAirfoilList().get(k);
				JPADGlobalData.getTheXmlTree().add(tempAirfoil, 3, "Airfoil_" + (k+1), fatherId + "af" + k); //"1" + k + "99");
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getGeometry(), 4, "Airfoil_Geometry", fatherId + "af" + k + "geo" + k);
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getAerodynamics(), 4, "Airfoil_Aerodynamics", fatherId + "af" + k + "aero" + k);
			}
		}
	}

	private static void addAirfoilsToXML(LiftingSurface liftingSurface, String fatherId) {

		if (liftingSurface != null) {
			for (int k=0; k < liftingSurface.getAirfoilList().size(); k++) {
				Airfoil tempAirfoil = liftingSurface.getAirfoilList().get(k);
				JPADGlobalData.getTheXmlTree().add(tempAirfoil, 3, "Airfoil_" + (k+1), tempAirfoil.getId()); //"1" + k + "99");
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getGeometry(), 4, "Airfoil_Geometry", tempAirfoil.getGeometry().getId());
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getAerodynamics(), 4, "Airfoil_Aerodynamics", tempAirfoil.getAerodynamics().getId());
			}
		}
	}

	private static void addEnginesToXML(PowerPlant powerPlant, String fatherId) {

		if (powerPlant != null) {
			for (int k=0; k < powerPlant.getEngineNumber(); k++) {
				Engine engine = powerPlant.getEngineList().get(k);
				JPADGlobalData.getTheXmlTree().add(engine, 3, "Engine_" + (k+1), engine.getId()); //"1" + k + "99");
				//				JPADGlobalData.getTheXmlTree().add(engine.getGeometry(), 4, "Airfoil_Geometry", engine.getGeometry().getId());
				//				JPADGlobalData.getTheXmlTree().add(engine.getAerodynamics(), 4, "Airfoil_Aerodynamics", engine.getAerodynamics().getId());
			}
		}
	}

	private static void addNacellesToXML(Nacelles nacelles, String fatherId) {

		if (nacelles != null) {
			for (int k=0; k < nacelles.getNacellesNumber(); k++) {
				NacelleCreator nacelle = nacelles.getNacellesList().get(k);
				JPADGlobalData.getTheXmlTree().add(nacelle, 3, "Nacelle_" + (k+1), nacelle.getId()); //"1" + k + "99");
				//				JPADGlobalData.getTheXmlTree().add(engine.getGeometry(), 4, "Airfoil_Geometry", engine.getGeometry().getId());
				//				JPADGlobalData.getTheXmlTree().add(engine.getAerodynamics(), 4, "Airfoil_Aerodynamics", engine.getAerodynamics().getId());
			}
		}
	}

//	public static void logToGUI(String message) {
//		if ((ADOPT_GUI.getApp() != null) && ADOPT_GUI.getApp().isGUIMode)
//			ADOPT_GUI.getApp().getTopLevelComposite().getMessageTextWindow().append(message);
//	}

}

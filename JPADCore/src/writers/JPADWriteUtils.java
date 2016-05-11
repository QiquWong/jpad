package writers;

import java.io.File;
import java.util.List;
import java.util.Map;

import aircraft.OperatingConditions;
import aircraft.auxiliary.airfoil.MyAirfoil;
import aircraft.calculators.ACAerodynamicsManager;
import aircraft.calculators.ACBalanceManager;
import aircraft.calculators.ACPerformanceManager;
import aircraft.calculators.ACWeightsManager;
import aircraft.calculators.costs.MyCosts;
import aircraft.components.Aircraft;
import aircraft.components.Configuration;
import aircraft.components.FuelTank;
import aircraft.components.LandingGear;
import aircraft.components.Systems;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.Canard;
import aircraft.components.liftingSurface.HTail;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.liftingSurface.VTail;
import aircraft.components.liftingSurface.Wing;
import aircraft.components.nacelles.Nacelle;
import aircraft.components.nacelles.NacellesManager;
import aircraft.components.powerPlant.Engine;
import aircraft.components.powerPlant.PowerPlant;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ComponentEnum;
import configuration.enumerations.MethodEnum;
import standaloneutils.JPADGlobalData;
import standaloneutils.customdata.MyXmlTree;

public class JPADWriteUtils {

	/** 
	 * This method handles the type of analysis the user could choose.
	 * Within a type more than one method can be used to make a comparison
	 * and estimate a mean value.
	 * 
	 * @author LA
	 * @deprecated
	 */
	public static void methodHandler(
			Map <ComponentEnum, List<MethodEnum>> methodsMap, 
			ComponentEnum componentType, 
			AnalysisTypeEnum analysisType,
			Aircraft aircraft, 
			OperatingConditions conditions) {

		if (analysisType == AnalysisTypeEnum.WEIGHTS) {

			if (methodsMap.get(componentType).get(0) == MethodEnum.ALL){

				for(int j=0; j < MethodEnum.values().length; j++){
					aircraft.get_component(componentType).calculateMass(
							aircraft, 
							conditions,
							MethodEnum.values()[j]);
				}

			} else if (methodsMap.get(componentType).size() > 1) {
				for(int j=0; j < methodsMap.get(componentType).size(); j++){
					aircraft.get_component(componentType).calculateMass(
							aircraft, 
							conditions, 
							methodsMap.get(componentType).get(j));	
				}
			}
		}

		if (analysisType == AnalysisTypeEnum.BALANCE) {
			if (methodsMap.get(componentType).get(0) == MethodEnum.ALL){

				for(int j=0; j < MethodEnum.values().length; j++){
					aircraft.get_component(componentType).calculateCG(
							aircraft, 
							conditions,
							MethodEnum.values()[j]);
				}

			} else if (methodsMap.get(componentType).size() > 1) {
				for(int j=0; j < methodsMap.get(componentType).size(); j++){
					aircraft.get_component(componentType).calculateCG(
							aircraft, 
							conditions, 
							methodsMap.get(componentType).get(j));	
				}
			}
		}

		if (analysisType == AnalysisTypeEnum.AERODYNAMIC) {
			if (methodsMap.get(componentType).get(0) == MethodEnum.ALL){

				for(int j=0; j < MethodEnum.values().length; j++){
					aircraft.get_component(componentType).calculateCG(
							aircraft, 
							conditions,
							MethodEnum.values()[j]);
				}

			} else if (methodsMap.get(componentType).size() > 1) {
				for(int j=0; j < methodsMap.get(componentType).size(); j++){
					aircraft.get_component(componentType).calculateCG(
							aircraft, 
							conditions, 
							methodsMap.get(componentType).get(j));	
				}
			}
		}
	}

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
			JPADGlobalData.getTheXmlTree().add(aircraft.get_configuration(), 2, "Configuration", Configuration.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.get_weights(), 2, "Weights", ACWeightsManager.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.get_theBalance(), 2, "Balance", ACBalanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.get_performances(), 2, "Performances", ACPerformanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.get_theCosts(), 2, "Costs", MyCosts.getId());
			
			JPADGlobalData.getTheXmlTree().add(aircraft.get_fuselage(), 2, "Fuselage", Fuselage.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_wing(), 2, "Wing", Wing.getId());
			addAirfoilsToXML(aircraft.get_wing(), Wing.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_HTail(), 2, "HTail", HTail.getId());
			addAirfoilsToXML(aircraft.get_HTail(), HTail.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_VTail(), 2, "VTail", VTail.getId());
			addAirfoilsToXML(aircraft.get_VTail(), VTail.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_Canard(), 2, "Canard", Canard.getId());
			addAirfoilsToXML(aircraft.get_Canard(), Canard.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_theNacelles(), 2, "Nacelles", NacellesManager.getId());
			addNacellesToXML(aircraft.get_theNacelles(), NacellesManager.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_theFuelTank(), 2, "Fuel_tank", FuelTank.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_powerPlant(), 2, "PowerPlant", PowerPlant.getId());
			addEnginesToXML(aircraft.get_powerPlant(), PowerPlant.getId());

			JPADGlobalData.getTheXmlTree().add(aircraft.get_systems(), 2, "Systems", Systems.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.get_landingGear(), 2, "LandingGear", LandingGear.getId());
			JPADGlobalData.getTheXmlTree().add(aircraft.get_theAerodynamics(), 2, "Aircraft_Aerodynamics", "24");

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
			JPADGlobalData.getTheXmlTree().add(Configuration.class, 2, "Configuration", Configuration.getId());
			JPADGlobalData.getTheXmlTree().add(ACWeightsManager.class, 2, "Weights", ACWeightsManager.getId());
			JPADGlobalData.getTheXmlTree().add(ACBalanceManager.class, 2, "Balance", ACBalanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(ACPerformanceManager.class, 2, "Performances", ACPerformanceManager.getId());
			JPADGlobalData.getTheXmlTree().add(Fuselage.class, 2, "Fuselage", Fuselage.getId());

			JPADGlobalData.getTheXmlTree().add(Wing.class, 2, "Wing", Wing.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().get_wing(), Wing.getId());

			JPADGlobalData.getTheXmlTree().add(HTail.class, 2, "HTail", HTail.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().get_HTail(), HTail.getId());

			JPADGlobalData.getTheXmlTree().add(VTail.class, 2, "VTail", VTail.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().get_VTail(), VTail.getId());

			JPADGlobalData.getTheXmlTree().add(Canard.class, 2, "Canard", Canard.getId());
			addAirfoilsToXML(JPADGlobalData.getTheCurrentAircraft().get_Canard(), Canard.getId());	

			JPADGlobalData.getTheXmlTree().add(Nacelle.class, 2, "Nacelle", Nacelle.getId());
			JPADGlobalData.getTheXmlTree().add(FuelTank.class, 2, "Fuel_tank", FuelTank.getId());
			JPADGlobalData.getTheXmlTree().add(PowerPlant.class, 2, "PowerPlant", PowerPlant.getId());
			JPADGlobalData.getTheXmlTree().add(Systems.class, 2, "Systems", Systems.getId());
			JPADGlobalData.getTheXmlTree().add(LandingGear.class, 2, "LandingGear", LandingGear.getId());
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
			for (int k=0; k < liftingSurface.get_numberOfAirfoils(); k++) {
				MyAirfoil tempAirfoil = liftingSurface.get_theAirfoilsList().get(k);
				JPADGlobalData.getTheXmlTree().add(tempAirfoil, 3, "Airfoil_" + (k+1), fatherId + "af" + k); //"1" + k + "99");
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getGeometry(), 4, "Airfoil_Geometry", fatherId + "af" + k + "geo" + k);
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getAerodynamics(), 4, "Airfoil_Aerodynamics", fatherId + "af" + k + "aero" + k);
			}
		}
	}

	private static void addAirfoilsToXML(LiftingSurface liftingSurface, String fatherId) {

		if (liftingSurface != null) {
			for (int k=0; k < liftingSurface.get_numberOfAirfoils(); k++) {
				MyAirfoil tempAirfoil = liftingSurface.get_theAirfoilsList().get(k);
				JPADGlobalData.getTheXmlTree().add(tempAirfoil, 3, "Airfoil_" + (k+1), tempAirfoil.getId()); //"1" + k + "99");
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getGeometry(), 4, "Airfoil_Geometry", tempAirfoil.getGeometry().getId());
				JPADGlobalData.getTheXmlTree().add(tempAirfoil.getAerodynamics(), 4, "Airfoil_Aerodynamics", tempAirfoil.getAerodynamics().getId());
			}
		}
	}

	private static void addEnginesToXML(PowerPlant powerPlant, String fatherId) {

		if (powerPlant != null) {
			for (int k=0; k < powerPlant.get_engineNumber(); k++) {
				Engine engine = powerPlant.get_engineList().get(k);
				JPADGlobalData.getTheXmlTree().add(engine, 3, "Engine_" + (k+1), engine.get_id()); //"1" + k + "99");
				//				JPADGlobalData.getTheXmlTree().add(engine.getGeometry(), 4, "Airfoil_Geometry", engine.getGeometry().getId());
				//				JPADGlobalData.getTheXmlTree().add(engine.getAerodynamics(), 4, "Airfoil_Aerodynamics", engine.getAerodynamics().getId());
			}
		}
	}

	private static void addNacellesToXML(NacellesManager nacelles, String fatherId) {

		if (nacelles != null) {
			for (int k=0; k < nacelles.get_nacellesNumber(); k++) {
				Nacelle nacelle = nacelles.get_nacellesList().get(k);
				JPADGlobalData.getTheXmlTree().add(nacelle, 3, "Nacelle_" + (k+1), nacelle.get_id()); //"1" + k + "99");
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

package it.unina.daf.jpadcadsandbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import aircraft.Aircraft;
import aircraft.components.fuselage.Fuselage;
import aircraft.components.liftingSurface.LiftingSurface;
import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.occ.OCCShape;
import it.unina.daf.jpadcad.occ.OCCUtils;
import it.unina.daf.jpadcad.utils.AircraftUtils;
import it.unina.daf.jpadcadsandbox.Test41mds.EngineCAD.EngineCADComponentsEnum;
import javaslang.Tuple2;
import javaslang.Tuple3;
import opencascade.TopoDS_Solid;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class Test41mds {

	public static void main(String[] args) {
		System.out.println("-------------------------------------------------------------");
		System.out.println("------------------ CAD engine modeling test -----------------");
		System.out.println("-------------------------------------------------------------");
		
		// ------------------------
		// Initialize the factory
		// ------------------------
		if (OCCUtils.theFactory == null) 
			OCCUtils.initCADShapeFactory();
		
		// ----------------------
		// Import the aircraft
		// ----------------------
		Aircraft aircraft = AircraftUtils.importAircraft(args);
		
		Fuselage fuselage = aircraft.getFuselage();
		LiftingSurface wing = aircraft.getWing();
		LiftingSurface hTail = aircraft.getHTail();
		LiftingSurface vTail = aircraft.getVTail();
		
		List<NacelleCreator> nacelles = aircraft.getNacelles().getNacellesList();
		List<Engine> engines = aircraft.getPowerPlant().getEngineList();
		
		// ---------------------------------
		// Generate the CAD of the aircraft
		// ---------------------------------
		
//		// Generate engine CAD objects
//		Map<EngineCADComponentsEnum, String> templatesMap = new HashMap<>();
//		templatesMap.put(EngineCADComponentsEnum.NACELLE, "TF_complete_01.step");
//		
//		if ((engines.size() & 1) == 0) { // even			
//			if (engines.get)
//				
//		} else { // odd
//			
//		}
//		
//		List<EngineCAD> engineCADList = new ArrayList<>();
//		for (int i = 0; i < engines.size(); i++) 
//			engineCADList.add(new EngineCAD(nacelles.get(i), engines.get(i), templatesMap));
		
		
		
		

	}
	
	public static List<OCCShape> getEnginesCAD(List<NacelleCreator> nacelles, List<Engine> engines,
			List<Map<EngineCADComponentsEnum, String>> templateMapsList,
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
		
		// ----------------------------------------------------------
		// Check whether continuing with the method
		// ----------------------------------------------------------
		if ((nacelles.isEmpty() || nacelles.stream().anyMatch(n -> n == null)) || 
			(engines.isEmpty() || engines.stream().anyMatch(e -> e == null))) {			
			System.out.println("========== [AircraftCADUtils::getEnginesCAD] One or more engine/nacelle object passed to the "
					+ "getEnginesCAD method is null! Exiting the method ...");
			return null;
		}
		
		if (!exportSupportShapes && !exportShells && !exportSolids) {
			System.out.println("========== [AircraftCADUtils::getEnginesCAD] No shapes to export! Exiting the method ...");
			return null;
		}
		
		System.out.println("========== [AircraftCADUtils::getEnginesCAD]");
		
		// ----------------------------------------------------------
		// Generate CAD shapes
		// ----------------------------------------------------------	
		List<OCCShape> requestedShapes = new ArrayList<>();
		
		// Check the indexes of the engines that can be mirrored, based on:
		// - sharing the same yApex
		// - sharing the same template
		// - sharing the same type
		List<Double> yApexesEngines = new ArrayList<>();
		List<List<String>> engineTemplates = new ArrayList<List<String>>();
		List<EngineTypeEnum> engineTypes = new ArrayList<>();
		for (int i = 0; i < engines.size(); i++) {
			yApexesEngines.add(engines.get(i).getYApexConstructionAxes().doubleValue(SI.METER));
			engineTemplates.add((List<String>) templateMapsList.get(i).values());
			engineTypes.add(engines.get(i).getEngineType());
		} 
		
//		boolean[] yApexesBool = new boolean[] {};
//		boolean[] templBool = new boolean[] {};
//		boolean[] typeBool = new boolean[] {};
//		yApexesEngines.stream().
		
		return requestedShapes;
	}
	
	public static List<OCCShape> getEngineCAD(EngineCAD engineCAD, 
			boolean exportSupportShapes, boolean exportShells, boolean exportSolids) {
		
		// ----------------------------------------------------------
		// Check the factory
		// ----------------------------------------------------------
		if (OCCUtils.theFactory == null) {
			System.out.println("========== [AircraftCADUtils::getEngineCAD] Initialize CAD shape factory");
			OCCUtils.initCADShapeFactory();
		}
		
		// ----------------------------------------------------------
		// Initialize patches and shape lists
		// ----------------------------------------------------------	
		List<OCCShape> requestedShapes = new ArrayList<>();
		List<OCCShape> supportShapes = new ArrayList<>();
		List<OCCShape> shellShapes = new ArrayList<>();
		List<OCCShape> solidShapes = new ArrayList<>();
		
		// -----------------------
		// Import templates data
		// -----------------------
		
		return requestedShapes;
	} 
	
	public static class EngineCAD {
		
		// ---------------------------
		// Available engine templates
		// ---------------------------
		private String engineTemplatesDataFilePath;
		private JPADXmlReader templatesDataReader;
		private Map<EngineCADComponentsEnum, String> defEngineCADTemplates = new HashMap<>();
		
		// TURBOPROP
		private List<String> tpNacelleTemplates = new ArrayList<>();	
		private List<String> tpBladeTemplates = new ArrayList<>();
		
		// TURBOFAN
		private List<String> tfNacelleTemplates = new ArrayList<>();
		
		// ---------------
		// Templates data
		// ---------------
		
		// TURBOFAN
		private double tfTemplateNacelleLength = 0;
		private double tfTemplateNacelleMaxDiameter = 0;
		private double tfTemplateInnerOuterCasingCoeff = 0;
		
		// TURBOPROP
		private double tpTemplateNacelleLength = 0;
		private double tpTemplateNacelleMaxDiameter = 0;
		
		private double tpTemplateHubDiameter = 0;
		private double tpTemplateHubCenterZCoord = 0;
		private double tpTemplateHubLengthRatio = 0;
		
		private double tpTemplateBladeMaxBaseDiameter = 0;
		private double tpTemplateBladeLength = 0;
		
		// -----------
		// Attributes
		// -----------
		private Map<EngineCADComponentsEnum, String> engineCADTemplates = new HashMap<>();
		
		private EngineTypeEnum engineType;
		
		private double engineXApex = 0;
		private double engineYApex = 0;
		private double engineZApex = 0;
		
		private double nacelleLenght = 0;
		private double nacelleMaxDiameter = 0;
		
		private int numberOfBlades = 0;
		private double propellerDiameter = 0;
		
		private double byPassRatio = 0;
		
		// ------------
		// Constructor
		// ------------
		public EngineCAD(NacelleCreator nacelle, Engine engine, Map<EngineCADComponentsEnum, String> templateFilenames) {
			
			if (checkTemplateListsEmptiness()) {
				System.out.println("Templates lists are empty ...");
				System.out.println("... filling lists ...");
				
				setTemplatesLists();
			}			
			
			this.engineType = engine.getEngineType();
			setDefaultTemplates(engineType);
			
			Set<EngineCADComponentsEnum> tpTemplateSet = new HashSet<>();
			switch (engineType) {

			case TURBOPROP:			
				tpTemplateSet.add(EngineCADComponentsEnum.BLADE);
				tpTemplateSet.add(EngineCADComponentsEnum.NACELLE);
				
				if (templateFilenames.keySet().equals(tpTemplateSet)) {
					
					if (tpBladeTemplates.contains(templateFilenames.get(EngineCADComponentsEnum.BLADE)) &&
						tpNacelleTemplates.contains(templateFilenames.get(EngineCADComponentsEnum.NACELLE))) {
						
						this.engineCADTemplates = templateFilenames;
						
					} else {
						System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");

						this.engineCADTemplates = defEngineCADTemplates;
					}
					
				} else {				
					System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");
					
					this.engineCADTemplates = defEngineCADTemplates;
				}
				
				break;

			case TURBOFAN:
				tpTemplateSet.add(EngineCADComponentsEnum.NACELLE);
				
				if (templateFilenames.keySet().equals(tpTemplateSet)) {
					
					if (tfNacelleTemplates.contains(templateFilenames.get(EngineCADComponentsEnum.NACELLE))) {
						
						this.engineCADTemplates = templateFilenames;
						
					} else {
						System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");

						this.engineCADTemplates = defEngineCADTemplates;				
					}

				} else {	
					System.err.println("Error: the selected templates are incorrect. Default templates assigned ...");

					this.engineCADTemplates = defEngineCADTemplates;
				}

				break;

			default:			
				System.err.println("No CAD templates are currently available for " +  engineType + " engines. "
						+ "No engine CAD shapes will be produced!");

				return;
			}
			
			this.engineXApex = engine.getXApexConstructionAxes().doubleValue(SI.METER);
			this.engineYApex = engine.getYApexConstructionAxes().doubleValue(SI.METER);
			this.engineZApex = engine.getZApexConstructionAxes().doubleValue(SI.METER);
			
			this.nacelleLenght = nacelle.getLength().doubleValue(SI.METER);
			this.nacelleMaxDiameter = nacelle.getDiameterMax().doubleValue(SI.METER);
			
			if (engineType.equals(EngineTypeEnum.PISTON) || 
				engineType.equals(EngineTypeEnum.PROPFAN) || 
				engineType.equals(EngineTypeEnum.TURBOPROP)) {
				
				this.numberOfBlades = engine.getNumberOfBlades();
				this.propellerDiameter = engine.getPropellerDiameter().doubleValue(SI.METER);
			}

			if (engineType.equals(EngineTypeEnum.TURBOFAN)) {
				
				this.byPassRatio = engine.getBPR();
			}
			
			readTemplatesData(engineType, engineCADTemplates);
		}
		
		private boolean checkTemplateListsEmptiness() {
			List<List<String>> templateLists = new ArrayList<List<String>>();
			templateLists.add(tpNacelleTemplates);
			templateLists.add(tpBladeTemplates);
			templateLists.add(tfNacelleTemplates);
			
			return templateLists.stream()
								.map(l -> l.isEmpty())
								.collect(Collectors.toList())
								.contains(true);
		}
		
		private void setTemplatesLists() {
			MyConfiguration.setDir(FoldersEnum.INPUT_DIR, MyConfiguration.inputDirectory);
			engineTemplatesDataFilePath = MyConfiguration.inputDirectory + 
					"CAD_Engine_Templates" + File.separator + 
					"engine_templates_data.xml";
			
			templatesDataReader = new JPADXmlReader(engineTemplatesDataFilePath);
			
			tfNacelleTemplates = MyXMLReaderUtils.getXMLPropertiesByPath(
					templatesDataReader.getXmlDoc(), templatesDataReader.getXpath(), "//turbofan/nacelle/@file");
			tpNacelleTemplates = MyXMLReaderUtils.getXMLPropertiesByPath(
					templatesDataReader.getXmlDoc(), templatesDataReader.getXpath(), "//turboprop/nacelle/@file");
			tpBladeTemplates = MyXMLReaderUtils.getXMLPropertiesByPath(
					templatesDataReader.getXmlDoc(), templatesDataReader.getXpath(), "//turboprop/blade/@file");
		}
		
		private void setDefaultTemplates(EngineTypeEnum engineType) {
			
			switch (engineType) {

			case TURBOPROP:
				this.defEngineCADTemplates.put(EngineCADComponentsEnum.NACELLE, tpNacelleTemplates.get(0));
				this.defEngineCADTemplates.put(EngineCADComponentsEnum.BLADE, tpBladeTemplates.get(0));	
				
				break;

			case TURBOFAN:
				this.defEngineCADTemplates.put(EngineCADComponentsEnum.NACELLE, tfNacelleTemplates.get(0));

				break;

			default:
				System.err.println("No CAD templates are currently available for " +  engineType + " engines. "
						+ "No engine CAD shapes will be produced!");

				return;
			}
				
		}
		
		private void readTemplatesData(EngineTypeEnum engineType, 
				Map<EngineCADComponentsEnum, String> templatesMap) {
			
			switch (engineType) {

			case TURBOPROP:
				NodeList tpNacelleTemplatesNodelist = MyXMLReaderUtils.getXMLNodeListByPath(
						templatesDataReader.getXmlDoc(), "//turboprop/nacelle");
				
				NodeList tpBladeTemplatesNodelist = MyXMLReaderUtils.getXMLNodeListByPath(
						templatesDataReader.getXmlDoc(), "//turboprop/blade");
				
				for (int i = 0; i < tpNacelleTemplatesNodelist.getLength(); i++) {				
					if (((Element) tpNacelleTemplatesNodelist.item(i)).getAttribute("file")
							.equals(templatesMap.get(EngineCADComponentsEnum.NACELLE))) {
						Element selectedNacelle = (Element) tpNacelleTemplatesNodelist.item(i);
						
						this.tpTemplateNacelleLength = getLengthToMeters(
								Double.parseDouble(selectedNacelle.getElementsByTagName("length").item(0).getTextContent()),
								selectedNacelle.getElementsByTagName("length").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tpTemplateNacelleMaxDiameter = getLengthToMeters(
								Double.parseDouble(selectedNacelle.getElementsByTagName("max_diameter").item(0).getTextContent()), 
								selectedNacelle.getElementsByTagName("max_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tpTemplateHubDiameter = getLengthToMeters(
								Double.parseDouble(selectedNacelle.getElementsByTagName("hub_diameter").item(0).getTextContent()), 
								selectedNacelle.getElementsByTagName("hub_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tpTemplateHubCenterZCoord = getLengthToMeters(
								Double.parseDouble(selectedNacelle.getElementsByTagName("hub_center_z_coord").item(0).getTextContent()), 
								selectedNacelle.getElementsByTagName("hub_center_z_coord").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tpTemplateHubLengthRatio = Double.parseDouble(selectedNacelle
								.getElementsByTagName("hub_length_ratio").item(0).getTextContent());
					}
				}
				
				for (int i = 0; i < tpBladeTemplatesNodelist.getLength(); i++) {
					if (((Element) tpBladeTemplatesNodelist.item(i)).getAttribute("file")
							.equals(templatesMap.get(EngineCADComponentsEnum.BLADE))) {						
						Element selectedBlade = (Element) tpBladeTemplatesNodelist.item(i);
						
						this.tpTemplateBladeMaxBaseDiameter = getLengthToMeters(
								Double.parseDouble(selectedBlade.getElementsByTagName("base_max_diameter").item(0).getTextContent()), 
								selectedBlade.getElementsByTagName("base_max_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tpTemplateBladeLength = getLengthToMeters(
								Double.parseDouble(selectedBlade.getElementsByTagName("length").item(0).getTextContent()), 
								selectedBlade.getElementsByTagName("length").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
					}
				}

				break;

			case TURBOFAN:
				NodeList tfNacelleTemplatesNodelist = MyXMLReaderUtils.getXMLNodeListByPath(
						templatesDataReader.getXmlDoc(), "//turbofan/nacelle");
				
				for (int i = 0; i < tfNacelleTemplatesNodelist.getLength(); i++) {
					if (((Element) tfNacelleTemplatesNodelist.item(i)).getAttribute("file")
							.equals(templatesMap.get(EngineCADComponentsEnum.NACELLE))) {
						Element selectedNacelle = (Element) tfNacelleTemplatesNodelist.item(i);
						
						this.tfTemplateNacelleLength = getLengthToMeters(
								Double.parseDouble(selectedNacelle.getElementsByTagName("length").item(0).getTextContent()),
								selectedNacelle.getElementsByTagName("length").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tfTemplateNacelleMaxDiameter = getLengthToMeters(
								Double.parseDouble(selectedNacelle.getElementsByTagName("max_diameter").item(0).getTextContent()), 
								selectedNacelle.getElementsByTagName("max_diameter").item(0).getAttributes().getNamedItem("unit").getNodeValue()
								);
						
						this.tfTemplateInnerOuterCasingCoeff = Double.parseDouble(selectedNacelle
								.getElementsByTagName("inn_out_casing_coeff").item(0).getTextContent());
					}
				}

				break;

			default:
				System.err.println("No CAD templates are currently available for " +  engineType + " engines. "
						+ "No engine CAD shapes will be produced!");

				return;
			}
		}
		
		private double getLengthToMeters(double length, String units) {
			
			if (units.equalsIgnoreCase("m") || units.equalsIgnoreCase("meter") || units.equalsIgnoreCase("meters") ||
				units.equalsIgnoreCase("metre") || units.equalsIgnoreCase("metres")) {
				return length;
				
			} else if (units.equalsIgnoreCase("mm") || units.equalsIgnoreCase("millimeter") || units.equalsIgnoreCase("millimeters") ||
					   units.equalsIgnoreCase("millimetre") || units.equalsIgnoreCase("millimetres")) {
				return Amount.valueOf(length, SI.MILLIMETER).doubleValue(SI.METER);
				
			} else if (units.equalsIgnoreCase("ft") || units.equalsIgnoreCase("feet") || units.equalsIgnoreCase("foot")) {
				return Amount.valueOf(length, NonSI.FOOT).doubleValue(SI.METER);
				
			} else if (units.equalsIgnoreCase("in") || units.equalsIgnoreCase("inch") || units.equalsIgnoreCase("inches")) {
				return Amount.valueOf(length, NonSI.INCH).doubleValue(SI.METER);
				
			} else {
				System.err.println("Warning: select appropriate units for the template input file");
				return 0;
			}
		}
		
		public double getTurbofanTemplateNacelleLength() {
			return this.tfTemplateNacelleLength;
		}
		
		public void setTurbofanTemplateNacelleLength(double tfTemplateNacelleLength) {
			this.tfTemplateNacelleLength = tfTemplateNacelleLength;
		}
		
		public double getTurbofanTemplateNacelleMaxDiameter() {
			return this.tfTemplateNacelleMaxDiameter;
		}
		
		public void setTurbofanTemplateNacelleMaxDiameter(double tfTemplateNacelleMaxDiameter) {
			this.tfTemplateNacelleMaxDiameter = tfTemplateNacelleMaxDiameter;
		}
		
		public double getTurbofanTemplateInnerOuterCasingCoeff() {
			return this.tfTemplateInnerOuterCasingCoeff;
		}
		
		public void setTurbofanTemplateInnerOuterCasingCoeff(double tfTemplateInnerOuterCasingCoeff) {
			this.tfTemplateInnerOuterCasingCoeff = tfTemplateInnerOuterCasingCoeff;
		}
		
		public double getTurbopropTemplateNacelleLength() {
			return this.tpTemplateNacelleLength;
		}
		
		public void setTurbopropTemplateNacelleLength(double tpTemplateNacelleLength) {
			this.tpTemplateNacelleLength = tpTemplateNacelleLength;
		}
		
		public double getTurbopropTemplateNacelleMaxDiameter() {
			return this.tpTemplateNacelleMaxDiameter;
		}
		
		public void setTurbopropTemplateNacelleMaxDiameter(double tpTemplateNacelleMaxDiameter) {
			this.tpTemplateNacelleMaxDiameter = tpTemplateNacelleMaxDiameter;
		}
		
		public double getTurbopropTemplateHubDiameter() {
			return this.tpTemplateHubDiameter;
		}
		
		public void setTurbopropTemplateHubDiameter(double tpTemplateHubDiameter) {
			this.tpTemplateHubDiameter = tpTemplateHubDiameter;
		}
		
		public double getTurbopropTemplateHubCenterZCoord() {
			return this.tpTemplateHubCenterZCoord;
		}
		
		public void setTurbopropTemplateHubCenterZCoord(double tpTemplateHubCenterZCoord) {
			this.tpTemplateHubCenterZCoord = tpTemplateHubCenterZCoord;
		}
		
		public double getTurbopropTemplateHubLengthRatio() {
			return this.tpTemplateHubLengthRatio;
		}
		
		public void setTurbopropTemplateHubLengthRatio(double tpTemplateHubLengthRatio) {
			this.tpTemplateHubLengthRatio = tpTemplateHubLengthRatio;
		}
		
		public double getTurbopropTemplateBladeMaxBaseDiameter() {
			return this.tpTemplateBladeMaxBaseDiameter;
		}
		
		public void setTurbopropTemplateBladeMaxBaseDiameter(double tpTemplateBladeMaxBaseDiameter) {
			this.tpTemplateBladeMaxBaseDiameter = tpTemplateBladeMaxBaseDiameter;
		}
		
		public double getTurbopropTemplateBladeLength() {
			return this.tpTemplateBladeLength;
		}
		
		public void setTurbopropTemplateBladeLength(double tpTemplateBladeLength) {
			this.tpTemplateBladeLength = tpTemplateBladeLength;
		}
		
		public Map<EngineCADComponentsEnum, String> getDefEngineCADTemplates() {
			return this.defEngineCADTemplates;
		}
		
		public void setDefEngineCADTemplates(Map<EngineCADComponentsEnum, String> defEngineCADTemplates) {
			this.defEngineCADTemplates = defEngineCADTemplates;
		}
		
		public Map<EngineCADComponentsEnum, String> getEngineCADTemplates() {
			return this.engineCADTemplates;
		}
		
		public void setEngineCADTemplates(Map<EngineCADComponentsEnum, String> engineCADTemplates) {
			this.engineCADTemplates = engineCADTemplates;
		} 
		
		public EngineTypeEnum getEngineType() {
			return this.engineType;
		}
		
		public void setEngineType(EngineTypeEnum engineType) {
			this.engineType = engineType;
		}
		
		public double getEngineXApex() {
			return this.engineXApex;
		}
		
		public void setEngineXApex(double engineXApex) {
			this.engineXApex = engineXApex;
		}
		
		public double getEngineYApex() {
			return this.engineYApex;
		}
		
		public void setEngineYApex(double engineYApex) {
			this.engineYApex = engineYApex;
		}
		
		public double getEngineZApex() {
			return this.engineZApex;
		}
		
		public void setEngineZApex(double engineZApex) {
			this.engineZApex = engineZApex;
		}
		
		public double getNacelleLenght() {
			return this.nacelleLenght;
		}
		
		public void setNacelleLenght(double nacelleLenght) {
			this.nacelleLenght = nacelleLenght;
		}
		
		public double getNacelleMaxDiameter() {
			return this.nacelleMaxDiameter;
		}
		
		public void setNacelleMaxDiameter(double nacellemaxDiameter) {
			this.nacelleMaxDiameter = nacellemaxDiameter;
		}
		
		public int getNumberOfBlades() {
			return this.numberOfBlades;
		}
		
		public void setNumberOfBlades(int numberOfBlades) {
			this.numberOfBlades = numberOfBlades;
		}
		
		public double getPropellerDiameter() {
			return this.propellerDiameter;
		}
		
		public void setPropellerDiameter(double propellerDiameter) {
			this.propellerDiameter = propellerDiameter;
		}
		
		public double getByPassRatio() {
			return this.byPassRatio;
		}
		
		public void setByPassRatio(double byPassRatio) {
			this.byPassRatio = byPassRatio;
		}
		
		public enum EngineCADComponentsEnum {
			NACELLE,
			BLADE;
		}
	}

}

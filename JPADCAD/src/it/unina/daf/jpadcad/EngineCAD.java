package it.unina.daf.jpadcad;

import java.io.File;
import java.util.ArrayList;
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
import org.w3c.dom.NodeList;

import aircraft.components.nacelles.NacelleCreator;
import aircraft.components.powerplant.Engine;
import configuration.MyConfiguration;
import configuration.enumerations.EngineTypeEnum;
import configuration.enumerations.FoldersEnum;
import it.unina.daf.jpadcad.enums.EngineCADComponentsEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class EngineCAD {

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
	
	private double tiltingAngle = 0;
	
	private double nacelleLength = 0;
	private double nacelleMaxDiameter = 0;
	
	private int numberOfBlades = 0;
	private double propellerDiameter = 0;
	
	private double byPassRatio = 0;
	
	// ------------
	// Constructor
	// ------------
	public EngineCAD(NacelleCreator nacelle, Engine engine, Map<EngineCADComponentsEnum, String> templateFilenames) {
		
		if (checkTemplateListsEmptiness()) {			
			setTemplatesLists();
		}			
		
		this.engineType = engine.getEngineType();
		setDefaultTemplates(engineType);
		
		Set<EngineCADComponentsEnum> engineTemplateSet = new HashSet<>();
		switch (engineType) {

		case TURBOPROP:			
			engineTemplateSet.add(EngineCADComponentsEnum.BLADE);
			engineTemplateSet.add(EngineCADComponentsEnum.NACELLE);
			
			if (templateFilenames.keySet().equals(engineTemplateSet)) {
				
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
			engineTemplateSet.add(EngineCADComponentsEnum.NACELLE);
			
			if (templateFilenames.keySet().equals(engineTemplateSet)) {
				
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
		
		this.tiltingAngle = engine.getTiltingAngle().doubleValue(SI.RADIAN);
		
		this.nacelleLength = nacelle.getLength().doubleValue(SI.METER);
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
	
	public boolean symmetrical(Object obj) {			
		if (obj == null) 
			return false;
		
		if (!EngineCAD.class.isAssignableFrom(obj.getClass())) 
			return false;
		
		final EngineCAD otherEngine = (EngineCAD) obj;
		if (!this.engineCADTemplates.equals(otherEngine.getEngineCADTemplates()) || 
			!this.engineType.equals(otherEngine.getEngineType()) || 
			!(Double.valueOf(this.engineXApex).equals(Double.valueOf(otherEngine.getEngineXApex()))) || 
			!(Double.valueOf(Math.abs(this.engineYApex)).equals(Double.valueOf(Math.abs(otherEngine.getEngineYApex()))) || 
			!(Double.valueOf(this.engineZApex).equals(Double.valueOf(otherEngine.getEngineZApex())))) ||
			!(Double.valueOf(this.tiltingAngle).equals(Double.valueOf(otherEngine.getTiltingAngle()))) ||
			!(Double.valueOf(this.nacelleLength).equals(Double.valueOf(otherEngine.getNacelleLength()))) ||
			!(Double.valueOf(this.nacelleMaxDiameter).equals(Double.valueOf(otherEngine.getNacelleMaxDiameter()))) ||
			!(Double.valueOf(this.numberOfBlades).equals(Double.valueOf(otherEngine.getNumberOfBlades()))) ||
			!(Double.valueOf(this.propellerDiameter).equals(Double.valueOf(otherEngine.getPropellerDiameter()))) ||
			!(Double.valueOf(this.byPassRatio).equals(Double.valueOf(otherEngine.getByPassRatio())))) {
			return false;
		}
						
		return true;
		
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
	
	public double getTiltingAngle() {
		return this.tiltingAngle;
	}
	
	public void setTiltingAngle(double tiltingAngle) {
		this.tiltingAngle = tiltingAngle;
	}
	
	public double getNacelleLength() {
		return this.nacelleLength;
	}
	
	public void setNacelleLenght(double nacelleLength) {
		this.nacelleLength = nacelleLength;
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
	
}


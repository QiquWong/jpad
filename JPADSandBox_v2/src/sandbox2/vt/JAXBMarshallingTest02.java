package sandbox2.vt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.RegulationsEnum;
import writers.xmlAdapters.AmountAdapter;

/*
 *  reference: https://www.javacodegeeks.com/2014/12/jaxb-tutorial-xml-binding.html
 *  reference: https://examples.javacodegeeks.com/core-java/xml/bind/jaxb-marshal-example/
 *  reference: http://j2eesolution.blogspot.it/2012/10/jaxb-eample-with-child-elements.html
 */

public class JAXBMarshallingTest02 {

	//------------------------------------------------------------------------------------
	// CABIN CONFIGURATION PROTOTYPE CLASS
	@XmlRootElement()
	@XmlAccessorType (XmlAccessType.FIELD)
	@XmlType( propOrder = {  // this define the order of the XMLElements
			"_cabinConfigurationPath"
			} )
	public static class MyCabinConfiguration {
		
		//-----------------------------------------------
		// VARIABLE DECLARATION:
		@XmlTransient
		private String _id;
		@XmlAttribute( name = "file")
		private String _cabinConfigurationPath;
		
		//-----------------------------------------------
		// GETTER: 
		public String getId() {
			return _id;
		}
		public String getCabinConfigurationPath() {
			return _cabinConfigurationPath;
		}
		
		//-----------------------------------------------
		// SETTER: 
		public void setId(String _id) {
			this._id = _id;
		}
		public void setCabinConfigurationPath(String _cabinConfigurationPath) {
			this._cabinConfigurationPath = _cabinConfigurationPath;
		}
	}
	
	//------------------------------------------------------------------------------------
	// LIFTING SURFACE PROTOTYPE CLASS
	@XmlRootElement()
	@XmlAccessorType (XmlAccessType.FIELD)
	@XmlType( propOrder = {  // this define the order of the XMLElements
			"_path","_xApex","_yApex","_zApex","_riggingAngle"
			} )
	public static class MyLiftingSurface {
		
		//-----------------------------------------------
		// VARIABLE DECLARATION:
		@XmlTransient
		private String _id;
		@XmlAttribute( name = "file")
		private String _path;
		@XmlElement( name = "x")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _xApex;
		@XmlElement( name = "y")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _yApex;
		@XmlElement( name = "z")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _zApex;
		@XmlElement( name = "rigging_angle")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Angle> _riggingAngle;
		
		//-----------------------------------------------
		// GETTER: 
		public String getId() {
			return _id;
		}
		public String getPath() {
			return _path;
		}
		public Amount<Length> getXApex() {
			return _xApex;
		}
		public Amount<Length> getYApex() {
			return _yApex;
		}
		public Amount<Length> getZApex() {
			return _zApex;
		}
		public Amount<Angle> getRiggingAngle() {
			return _riggingAngle;
		}
		
		//-----------------------------------------------
		// SETTER: 
		public void setId(String _id) {
			this._id = _id;
		}
		public void setPath(String _path) {
			this._path = _path;
		}
		public void setXApex(Amount<Length> _xApex) {
			this._xApex = _xApex;
		}
		public void setYApex(Amount<Length> _yApex) {
			this._yApex = _yApex;
		}
		public void setZApex(Amount<Length> _zApex) {
			this._zApex = _zApex;
		}
		public void setRiggingAngle(Amount<Angle> _riggingAngleWing) {
			this._riggingAngle = _riggingAngleWing;
		}
	}
	
	//------------------------------------------------------------------------------------
	// FUSELAGE PROTOTYPE CLASS
	@XmlRootElement()
	@XmlAccessorType (XmlAccessType.FIELD)
	@XmlType( propOrder = {  // this define the order of the XMLElements
			"_fuselagePath","_xApexFuselage","_yApexFuselage","_zApexFuselage"
			} )
	public static class MyFuselage {
	
		//-----------------------------------------------
		// VARIABLE DECLARATION:
		@XmlTransient
		private String _id;
		@XmlAttribute( name = "file")
		private String _fuselagePath;
		@XmlElement( name = "x")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _xApexFuselage;
		@XmlElement( name = "y")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _yApexFuselage;
		@XmlElement( name = "z")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _zApexFuselage;

		//-----------------------------------------------
		// GETTER: 
		public String getId() {
			return _id;
		}
		public String getFuselagePath() {
			return _fuselagePath;
		}
		public Amount<Length> getXApexFuselage() {
			return _xApexFuselage;
		}
		public Amount<Length> getYApexFuselage() {
			return _yApexFuselage;
		}
		public Amount<Length> getZApexFuselage() {
			return _zApexFuselage;
		}
		
		//-----------------------------------------------
		// SETTER: 
		public void setId(String _id) {
			this._id = _id;
		}
		public void setFuselagePath(String _fuselagePath) {
			this._fuselagePath = _fuselagePath;
		}
		public void setXApexFuselage(Amount<Length> _xApexFuselage) {
			this._xApexFuselage = _xApexFuselage;
		}
		public void setYApexFuselage(Amount<Length> _yApexFuselage) {
			this._yApexFuselage = _yApexFuselage;
		}
		public void setZApexFuselage(Amount<Length> _zApexFuselage) {
			this._zApexFuselage = _zApexFuselage;
		}
	}
	
	//------------------------------------------------------------------------------------
	// AIRCRAFT PROTOTYPE CLASS
	@XmlRootElement( name = "aircraft" )
	@XmlAccessorType (XmlAccessType.FIELD)
	@XmlSeeAlso( {MyCabinConfiguration.class, MyLiftingSurface.class, MyFuselage.class})
	@XmlType( propOrder = {  // this define the order of the XMLElements
			"_id", "_typeVehicle", "_regulations",
			"_cabinConfigurationsList", "_liftingSurfacesList", "_fuselageLists"
			} )
	public static class MyAircraftProtoype {
		
		//-----------------------------------------------
		// VARIABLE DECLARATION:
		@XmlAttribute( name = "id")
		private String _id;
		@XmlAttribute( name = "type")
		private AircraftTypeEnum _typeVehicle;
		@XmlAttribute( name = "regulations")
		private RegulationsEnum _regulations;
		
		// global data
		@XmlElement(name = "cabin_configuration", type = MyCabinConfiguration.class)
		List<MyCabinConfiguration> _cabinConfigurationsList;
		
		// lifting surfaces
		@XmlElement(name = "lifting_surfaces", type = MyLiftingSurface.class)
		List<MyLiftingSurface> _liftingSurfacesList;

		// fuselages
		@XmlElement(name = "fuselages", type = MyFuselage.class)
		List<MyFuselage> _fuselageLists;
		
		//-----------------------------------------------
		// BUILDER:
		public MyAircraftProtoype() {}
		
		public MyAircraftProtoype(
				List<MyCabinConfiguration> _cabinConfigurations,
				List<MyLiftingSurface> _liftingSurfaces,
				List<MyFuselage> _fuselages
				) {
			
			this._cabinConfigurationsList = _cabinConfigurations;
			this._liftingSurfacesList = _liftingSurfaces;
			this._fuselageLists = _fuselages;
			
		}
		
		//-----------------------------------------------
		// GETTER: 
		public String getId() {
			return _id;
		}
		public AircraftTypeEnum getTypeVehicle() {
			return _typeVehicle;
		}
		public RegulationsEnum getRegulations() {
			return _regulations;
		}
		public List<MyCabinConfiguration> getCabinConfigurationsList() {
			return _cabinConfigurationsList;
		}
		
		public List<MyLiftingSurface> getLiftingSurfacesList() {
			return _liftingSurfacesList;
		}
		public List<MyFuselage> getFuselagesList() {
			return _fuselageLists;
		}
		
		//-----------------------------------------------
		// SETTER: 
		public void setId(String _id) {
			this._id = _id;
		}
		public void setTypeVehicle(AircraftTypeEnum _typeVehicle) {
			this._typeVehicle = _typeVehicle;
		}
		public void setRegulations(RegulationsEnum _regulations) {
			this._regulations = _regulations;
		}
		public void setCabinConfigurationsList (List<MyCabinConfiguration> _cabinConfigurationsList) {
			this._cabinConfigurationsList = _cabinConfigurationsList;
		}
		public void setLiftingSurfacesList (List<MyLiftingSurface> _liftingSurfacesList) {
			this._liftingSurfacesList = _liftingSurfacesList;
		}
		public void setFuselagesList (List<MyFuselage> _fuselagesList) {
			this._fuselageLists = _fuselagesList;
		}

	}
	
	public static class JAXBXMLHandler {
		 
	    // Export
	    public static void marshal(
	    		MyAircraftProtoype theAircraft,
	    		File selectedFile)
	            throws IOException, JAXBException {
	        JAXBContext context;
	        BufferedWriter writer = null;
	        writer = new BufferedWriter(new FileWriter(selectedFile));
	        context = JAXBContext.newInstance(
	        		MyAircraftProtoype.class,
	        		AmountAdapter.class
	        		);
	        Marshaller m = context.createMarshaller();
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        m.marshal(
	        		theAircraft,
	        		writer
	        		);
			m.marshal(
					theAircraft,
					System.out 
					);
	        writer.close();
	    }
	    
	}
	
	public static void main(String[] args) throws JAXBException {

		MyConfiguration.initWorkingDirectoryTree();
		
		/*
		 * SINCE THE INNER CLASS SETTERS HAVE THE DECORATION, 
		 * THESE ARE THE ONE THAT HAVE TO BE CALLEDIN ORDER TO POPULATE THE 
		 * XML FILE. 
		 * VERY USEFUL IN THE GUI, WHERE, EVERYTIME THE USER UPDATES THE AIRCRAFT,
		 * THERE IS A METHOD WHICH SETS ALL THE OBJECT FILEDS 
		 * (AND PERFORM A NEW CALCULATE GEOMETRY) WHICH CAN BE EASILY 
		 * WRITTEN ON FILE.
		 * 
		 * TODO : SEE HOW TO ADD TAG NOT LINKED WITH FIELDS
		 * TODO : POSSIBLE XML STRUCTURE CHANGE?? (DISCUSS WITH PROF.)
		 */
		
		// CABIN CONFIGURATIONS:
		MyCabinConfiguration cabinConfiguration = new MyCabinConfiguration();
		cabinConfiguration.setId("cabin_configuration");
		cabinConfiguration.setCabinConfigurationPath("cabin_configuration_IRON.xml");
		
		List<MyCabinConfiguration> cabinConfigurations = new ArrayList<>();
		cabinConfigurations.add(cabinConfiguration);
				
		// LIFTING SURFACES:
		MyLiftingSurface wing = new MyLiftingSurface();
		wing.setId("wing");
		wing.setPath("wing_IRON.xml");
		wing.setXApex(Amount.valueOf(19.09, SI.METER));
		wing.setYApex(Amount.valueOf(0.0, SI.METER));
		wing.setZApex(Amount.valueOf(-1.32, SI.METER));
		wing.setRiggingAngle(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
		
		MyLiftingSurface hTail = new MyLiftingSurface();
		hTail.setId("horizontal_tail");
		hTail.setPath("htail_IRON.xml");
		hTail.setXApex(Amount.valueOf(31.54, SI.METER));
		hTail.setYApex(Amount.valueOf(0.0, SI.METER));
		hTail.setZApex(Amount.valueOf(1.34, SI.METER));
		hTail.setRiggingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		
		MyLiftingSurface vTail = new MyLiftingSurface();
		vTail.setId("vertical_tail");
		vTail.setPath("vtail_IRON.xml");
		vTail.setXApex(Amount.valueOf(31.54, SI.METER));
		vTail.setYApex(Amount.valueOf(0.0, SI.METER));
		vTail.setZApex(Amount.valueOf(1.5, SI.METER));
		vTail.setRiggingAngle(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		
		List<MyLiftingSurface> liftingSurfaces = new ArrayList<>();
		liftingSurfaces.add(wing);
		liftingSurfaces.add(hTail);
		liftingSurfaces.add(vTail);
		
		// FUSELAGES:
		MyFuselage fuselage = new MyFuselage();
		fuselage.setId("fuselage");
		fuselage.setFuselagePath("fuselage_IRON.xml");
		fuselage.setXApexFuselage(Amount.valueOf(0.0, SI.METER));
		fuselage.setYApexFuselage(Amount.valueOf(0.0, SI.METER));
		fuselage.setZApexFuselage(Amount.valueOf(0.0, SI.METER));
		
		List<MyFuselage> fuselages = new ArrayList<>();
		fuselages.add(fuselage);
		
		// AIRCRAFT PROTOTYPE CREATION: 
		MyAircraftProtoype theAircraft = new MyAircraftProtoype(
				cabinConfigurations,
				liftingSurfaces,
				fuselages
				);
		theAircraft.setId("IRON");
		theAircraft.setTypeVehicle(AircraftTypeEnum.TURBOPROP);
		theAircraft.setRegulations(RegulationsEnum.FAR_25);
		
		// CORE OF THE TEST
		//Marshalling: Writing Java objects to XMl file
        try {
            JAXBXMLHandler.marshal(
            		theAircraft,
            		new File(
    						MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
    						+ File.separator
    						+ "MyAircraft_TestJAXB02.xml" 
    						)
            		);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }		
	}

}

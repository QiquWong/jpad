package sandbox2.vt;

import java.io.File;

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
 */

public class JAXBMarshallingTest01 {

	/*
	 * THE CLASS THAT HAVE TO BE PASSED TO JAXB MUST BE STATIC  (if INNER)!!
	 */
	@XmlRootElement( name = "aircraft" )
	@XmlAccessorType (XmlAccessType.FIELD)
	@XmlType( propOrder = {  // this define the order of the XMLElements
			"_id", "_typeVehicle", "_regulations",
			"_cabinConfigurationPath",
			"_wingPath","_xApexWing","_yApexWing","_zApexWing","_riggingAngleWing",
			"_hTailPath","_xApexHTail","_yApexHTail","_zApexHTail","_riggingAngleHTail",
			"_vTailPath","_xApexVTail","_yApexVTail","_zApexVTail", "_riggingAngleVTail",
			"_fuselagePath","_xApexFuselage","_yApexFuselage","_zApexFuselage"
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
		
		@XmlAttribute( name = "file")
		private String _cabinConfigurationPath;
		
		@XmlAttribute( name = "file")
		private String _wingPath;
		@XmlElement( name = "x")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _xApexWing;
		@XmlElement( name = "y")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _yApexWing;
		@XmlElement( name = "z")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _zApexWing;
		@XmlElement( name = "rigging_angle")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Angle> _riggingAngleWing;
		
		@XmlAttribute( name = "file")
		private String _hTailPath;
		@XmlElement( name = "x")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _xApexHTail;
		@XmlElement( name = "y")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _yApexHTail;
		@XmlElement( name = "z")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _zApexHTail;
		@XmlElement( name = "rigging_angle")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Angle> _riggingAngleHTail;
		
		@XmlAttribute( name = "file")
		private String _vTailPath;
		@XmlElement( name = "x")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _xApexVTail;
		@XmlElement( name = "y")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _yApexVTail;
		@XmlElement( name = "z")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Length> _zApexVTail;
		@XmlElement( name = "rigging_angle")
		@XmlJavaTypeAdapter(AmountAdapter.class)
		private Amount<Angle> _riggingAngleVTail;
		
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
		public AircraftTypeEnum getTypeVehicle() {
			return _typeVehicle;
		}
		public RegulationsEnum getRegulations() {
			return _regulations;
		}
		public String getCabinConfigurationPath() {
			return _cabinConfigurationPath;
		}
		public String getWingPath() {
			return _wingPath;
		}
		public Amount<Length> getXApexWing() {
			return _xApexWing;
		}
		public Amount<Length> getYApexWing() {
			return _yApexWing;
		}
		public Amount<Length> getZApexWing() {
			return _zApexWing;
		}
		public Amount<Angle> getRiggingAngleWing() {
			return _riggingAngleWing;
		}
		public String getHTailPath() {
			return _hTailPath;
		}
		public Amount<Length> getXApexHTail() {
			return _xApexHTail;
		}
		public Amount<Length> getYApexHTail() {
			return _yApexHTail;
		}
		public Amount<Length> getZApexHTail() {
			return _zApexHTail;
		}
		public Amount<Angle> getRiggingAngleHTail() {
			return _riggingAngleHTail;
		}
		public String getVTailPath() {
			return _vTailPath;
		}
		public Amount<Length> getXApexVTail() {
			return _xApexVTail;
		}
		public Amount<Length> getYApexVTail() {
			return _yApexVTail;
		}
		public Amount<Length> getZApexVTail() {
			return _zApexVTail;
		}
		public Amount<Angle> getRiggingAngleVTail() {
			return _riggingAngleVTail;
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
		public void setTypeVehicle(AircraftTypeEnum _typeVehicle) {
			this._typeVehicle = _typeVehicle;
		}
		public void setRegulations(RegulationsEnum _regulations) {
			this._regulations = _regulations;
		}
		public void setCabinConfigurationPath(String _cabinConfigurationPath) {
			this._cabinConfigurationPath = _cabinConfigurationPath;
		}
		public void setWingPath(String _wingPath) {
			this._wingPath = _wingPath;
		}
		public void setXApexWing(Amount<Length> _xApexWing) {
			this._xApexWing = _xApexWing;
		}
		public void setYApexWing(Amount<Length> _yApexWing) {
			this._yApexWing = _yApexWing;
		}
		public void setZApexWing(Amount<Length> _zApexWing) {
			this._zApexWing = _zApexWing;
		}
		public void setRiggingAngleWing(Amount<Angle> _riggingAngleWing) {
			this._riggingAngleWing = _riggingAngleWing;
		}
		public void setHTailPath(String _hTailPath) {
			this._hTailPath = _hTailPath;
		}
		public void setXApexHTail(Amount<Length> _xApexHTail) {
			this._xApexHTail = _xApexHTail;
		}
		public void setYApexHTail(Amount<Length> _yApexHTail) {
			this._yApexHTail = _yApexHTail;
		}
		public void setZApexHTail(Amount<Length> _zApexHTail) {
			this._zApexHTail = _zApexHTail;
		}
		public void setRiggingAngleHTail(Amount<Angle> _riggingAngleHTail) {
			this._riggingAngleHTail = _riggingAngleHTail;
		}
		public void setVTailPath(String _vTailPath) {
			this._vTailPath = _vTailPath;
		}
		public void setXApexVTail(Amount<Length> _xApexVTail) {
			this._xApexVTail = _xApexVTail;
		}
		public void setYApexVTail(Amount<Length> _yApexVTail) {
			this._yApexVTail = _yApexVTail;
		}
		public void setZApexVTail(Amount<Length> _zApexVTail) {
			this._zApexVTail = _zApexVTail;
		}
		public void setRiggingAngleVTail(Amount<Angle> _riggingAngleVTail) {
			this._riggingAngleVTail = _riggingAngleVTail;
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
		 * TODO : MANAGE COMPONENTS AS LISTS IN ORDER TO MAKE THE XML STRUCTURE SIMILAR TO THE ORGINAL XML
		 * TODO : SEE HOW TO PRINT AMOUNT FIELDS
		 */
		
		
		
		MyAircraftProtoype theAircraft = new MyAircraftProtoype();
		theAircraft.setId("IRON");
		theAircraft.setTypeVehicle(AircraftTypeEnum.TURBOPROP);
		theAircraft.setRegulations(RegulationsEnum.FAR_25);
		theAircraft.setCabinConfigurationPath("cabin_configuration_IRON.xml");
		theAircraft.setWingPath("wing_IRON.xml");
		theAircraft.setXApexWing(Amount.valueOf(19.09, SI.METER)); 
		theAircraft.setYApexWing(Amount.valueOf(0.0, SI.METER));
		theAircraft.setZApexWing(Amount.valueOf(-1.32, SI.METER));
		theAircraft.setRiggingAngleWing(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
		theAircraft.setHTailPath("htail_IRON.xml");
		theAircraft.setXApexHTail(Amount.valueOf(31.54, SI.METER)); 
		theAircraft.setYApexHTail(Amount.valueOf(0.0, SI.METER));
		theAircraft.setZApexHTail(Amount.valueOf(1.34, SI.METER));
		theAircraft.setRiggingAngleHTail(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		theAircraft.setVTailPath("vtail_IRON.xml");
		theAircraft.setXApexVTail(Amount.valueOf(31.54, SI.METER)); 
		theAircraft.setYApexVTail(Amount.valueOf(0.0, SI.METER));
		theAircraft.setZApexVTail(Amount.valueOf(1.5, SI.METER));
		theAircraft.setRiggingAngleVTail(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		theAircraft.setFuselagePath("fuselage_IRON.xml");
		theAircraft.setXApexFuselage(Amount.valueOf(0.0, SI.METER)); 
		theAircraft.setYApexFuselage(Amount.valueOf(0.0, SI.METER));
		theAircraft.setZApexFuselage(Amount.valueOf(0.0, SI.METER));

		// CORE OF THE TEST
		JAXBContext jaxbContext = JAXBContext.newInstance(
				MyAircraftProtoype.class,
				AmountAdapter.class
				);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );
		
		// this exports to xml
		jaxbMarshaller.marshal(
				theAircraft,
				new File(
						MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
						+ File.separator
						+ "MyAircraft_TestJAXB01.xml" 
						) 
				);
		
		// this prints the xml to console
		jaxbMarshaller.marshal(
				theAircraft,
				System.out 
				);
		
	}

}

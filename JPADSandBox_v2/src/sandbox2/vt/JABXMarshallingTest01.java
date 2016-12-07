package sandbox2.vt;

import java.io.File;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.jscience.physics.amount.Amount;

import configuration.MyConfiguration;
import configuration.enumerations.AircraftTypeEnum;
import configuration.enumerations.FoldersEnum;
import configuration.enumerations.RegulationsEnum;

/*
 *  reference: https://www.javacodegeeks.com/2014/12/jaxb-tutorial-xml-binding.html
 */

public class JABXMarshallingTest01 {

	/*
	 * THE CLASS THAT HAVE TO BE PASSED TO JAXB MUST BE STATIC !!
	 */
	@XmlRootElement( name = "aircraft" )
	@XmlType( propOrder = {  // this define the order of the XMLElements
			"",
			"",
			"",
			"" ,
			""
			} )
	public static class MyAircraftProtoype {
		
		//-----------------------------------------------
		// VARIABLE DECLARATION:
		private String _id;
		private AircraftTypeEnum _typeVehicle;
		private RegulationsEnum _regulations;
		
		private String _cabinConfigurationPath;
		
		private String _wingPath;
		private Amount<Length> _xApexWing;
		private Amount<Length> _yApexWing;
		private Amount<Length> _zApexWing;
		private Amount<Angle> _riggingAngleWing;
		
		private String _hTailPath;
		private Amount<Length> _xApexHTail;
		private Amount<Length> _yApexHTail;
		private Amount<Length> _zApexHTail;
		private Amount<Angle> _riggingAngleHTail;
		
		private String _vTailPath;
		private Amount<Length> _xApexVTail;
		private Amount<Length> _yApexVTail;
		private Amount<Length> _zApexVTail;
		private Amount<Angle> _riggingAngleVTail;
		
		private String _fuselagePath;
		private Amount<Length> _xApexFuselage;
		private Amount<Length> _yApexFuselage;
		private Amount<Length> _zApexFuselage;
		
		// TODO : TRY TO ADD POWER PLANT (List of enigne) 
				
		//-----------------------------------------------
		// GETTER: (remember to put setter and getters divided)
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
		// SETTER: (remember to put setter and getters divided)
		@XmlAttribute( name = "id")
		public void setId(String _id) {
			this._id = _id;
		}
		@XmlAttribute( name = "type")
		public void setTypeVehicle(AircraftTypeEnum _typeVehicle) {
			this._typeVehicle = _typeVehicle;
		}
		@XmlAttribute( name = "regulations")
		public void setRegulations(RegulationsEnum _regulations) {
			this._regulations = _regulations;
		}
		@XmlAttribute( name = "file")
		public void setCabinConfigurationPath(String _cabinConfigurationPath) {
			this._cabinConfigurationPath = _cabinConfigurationPath;
		}
		@XmlAttribute( name = "file")
		public void setWingPath(String _wingPath) {
			this._wingPath = _wingPath;
		}
		@XmlElement( name = "x")
		public void setXApexWing(Amount<Length> _xApexWing) {
			this._xApexWing = _xApexWing;
		}
		@XmlElement( name = "y")
		public void setYApexWing(Amount<Length> _yApexWing) {
			this._yApexWing = _yApexWing;
		}
		@XmlElement( name = "z")
		public void setZApexWing(Amount<Length> _zApexWing) {
			this._zApexWing = _zApexWing;
		}
		@XmlElement( name = "rigging_angle")
		public void setRiggingAngleWing(Amount<Angle> _riggingAngleWing) {
			this._riggingAngleWing = _riggingAngleWing;
		}
		@XmlAttribute( name = "file")
		public void setHTailPath(String _hTailPath) {
			this._hTailPath = _hTailPath;
		}
		@XmlElement( name = "x")
		public void setXApexHTail(Amount<Length> _xApexHTail) {
			this._xApexHTail = _xApexHTail;
		}
		@XmlElement( name = "y")
		public void setYApexHTail(Amount<Length> _yApexHTail) {
			this._yApexHTail = _yApexHTail;
		}
		@XmlElement( name = "z")
		public void setZApexHTail(Amount<Length> _zApexHTail) {
			this._zApexHTail = _zApexHTail;
		}
		@XmlElement( name = "rigging_angle")
		public void setRiggingAngleHTail(Amount<Angle> _riggingAngleHTail) {
			this._riggingAngleHTail = _riggingAngleHTail;
		}
		@XmlAttribute( name = "file")
		public void setVTailPath(String _vTailPath) {
			this._vTailPath = _vTailPath;
		}
		@XmlElement( name = "x")
		public void setXApexVTail(Amount<Length> _xApexVTail) {
			this._xApexVTail = _xApexVTail;
		}
		@XmlElement( name = "y")
		public void setYApexVTail(Amount<Length> _yApexVTail) {
			this._yApexVTail = _yApexVTail;
		}
		@XmlElement( name = "z")
		public void setZApexVTail(Amount<Length> _zApexVTail) {
			this._zApexVTail = _zApexVTail;
		}
		@XmlElement( name = "rigging_angle")
		public void setRiggingAngleVTail(Amount<Angle> _riggingAngleVTail) {
			this._riggingAngleVTail = _riggingAngleVTail;
		}
		@XmlAttribute( name = "file")
		public void setFuselagePath(String _fuselagePath) {
			this._fuselagePath = _fuselagePath;
		}
		@XmlElement( name = "x")
		public void setXApexFuselage(Amount<Length> _xApexFuselage) {
			this._xApexFuselage = _xApexFuselage;
		}
		@XmlElement( name = "y")
		public void setYApexFuselage(Amount<Length> _yApexFuselage) {
			this._yApexFuselage = _yApexFuselage;
		}
		@XmlElement( name = "z")
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
		 * TODO : SEE IF THIS WORK ALSO WITH GETTERS
		 * TODO : SEE HOW TO ADD TAG NOT LINKED WITH FIELDS
		 * TODO : DEFINE PROP ORDER
		 */
		
		MyAircraftProtoype theAricraft = new MyAircraftProtoype();
		theAricraft.setId("IRON");
		theAricraft.setTypeVehicle(AircraftTypeEnum.TURBOPROP);
		theAricraft.setRegulations(RegulationsEnum.FAR_25);
		theAricraft.setCabinConfigurationPath("cabin_configuration_IRON.xml");
		theAricraft.setWingPath("wing_IRON.xml");
		theAricraft.setXApexWing(Amount.valueOf(19.09, SI.METER)); 
		theAricraft.setYApexWing(Amount.valueOf(0.0, SI.METER));
		theAricraft.setZApexWing(Amount.valueOf(-1.32, SI.METER));
		theAricraft.setRiggingAngleWing(Amount.valueOf(2.0, NonSI.DEGREE_ANGLE));
		theAricraft.setHTailPath("htail_IRON.xml");
		theAricraft.setXApexHTail(Amount.valueOf(31.54, SI.METER)); 
		theAricraft.setYApexHTail(Amount.valueOf(0.0, SI.METER));
		theAricraft.setZApexHTail(Amount.valueOf(1.34, SI.METER));
		theAricraft.setRiggingAngleHTail(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		theAricraft.setVTailPath("vtail_IRON.xml");
		theAricraft.setXApexVTail(Amount.valueOf(31.54, SI.METER)); 
		theAricraft.setYApexVTail(Amount.valueOf(0.0, SI.METER));
		theAricraft.setZApexVTail(Amount.valueOf(1.5, SI.METER));
		theAricraft.setRiggingAngleVTail(Amount.valueOf(0.0, NonSI.DEGREE_ANGLE));
		theAricraft.setFuselagePath("fuselage_IRON.xml");
		theAricraft.setXApexFuselage(Amount.valueOf(0.0, SI.METER)); 
		theAricraft.setYApexFuselage(Amount.valueOf(0.0, SI.METER));
		theAricraft.setZApexFuselage(Amount.valueOf(0.0, SI.METER));

		// CORE OF THE TEST
		JAXBContext jaxbContext = JAXBContext.newInstance(MyAircraftProtoype.class );
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// this exports to xml
		jaxbMarshaller.marshal(
				theAricraft,
				new File(
						MyConfiguration.getDir(FoldersEnum.OUTPUT_DIR)
						+ File.separator
						+ "MyAircraft_TestJAXB01.xml" 
						) 
				);
		
		// this prints the xml to console
		jaxbMarshaller.marshal(
				theAricraft,
				System.out 
				);
		
	}

}

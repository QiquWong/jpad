package aircraft.components;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import analyses.landinggears.LandingGearsBalanceManager;
import analyses.landinggears.LandingGearsWeightManager;
import configuration.MyConfiguration;
import configuration.enumerations.LandingGearsMountingPositionEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyXMLReaderUtils;

public class LandingGears {

	//------------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private ILandingGear _theLandingGearInterface;
	private LandingGearsMountingPositionEnum _mountingPosition;
	private Amount<Length> _xApexConstructionAxesNoseGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxesNoseGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxesNoseGear = Amount.valueOf(0.0, SI.METER);
	private Amount<Length> _xApexConstructionAxesMainGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _yApexConstructionAxesMainGear = Amount.valueOf(0.0, SI.METER); 
	private Amount<Length> _zApexConstructionAxesMainGear = Amount.valueOf(0.0, SI.METER);
	
	private LandingGearsWeightManager _theWeigths;
	private LandingGearsBalanceManager _theBalance;
	
	//------------------------------------------------------------------------------------------
	// BUILDER
	public LandingGears (ILandingGear theLandingGearsInterface) {
		
		this._theLandingGearInterface = theLandingGearsInterface;
		this._theWeigths = new LandingGearsWeightManager();
		this._theBalance = new LandingGearsBalanceManager();
	}

	//------------------------------------------------------------------------------------------
	// METHODS
	
	public static LandingGears importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading landing gears data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//---------------------------------------------------------------
		// GLOBAL DATA
		Amount<Length> mainGearLegsLength = Amount.valueOf(0.0, SI.METER);
		Amount<Length> distanceBetweenWheels = Amount.valueOf(0.0, SI.METER);
		int numberOfFrontalWheels = 0;
		int numberOfRearWheels = 0;
		
		
		String mainGearLegsLengthProperty = reader.getXMLPropertyByPath("//global_data/main_gear_legs_length");
		if(mainGearLegsLengthProperty != null)
			mainGearLegsLength = reader.getXMLAmountLengthByPath("//global_data/main_gear_legs_length");
		
		String distanceBetweenWheelsProperty = reader.getXMLPropertyByPath("//global_data/distance_between_wheels");
		if(distanceBetweenWheelsProperty != null)
			distanceBetweenWheels = reader.getXMLAmountLengthByPath("//global_data/distance_between_wheels");
		
		String numberOfFrontalWheelsProperty = reader.getXMLPropertyByPath("//global_data/number_of_frontal_wheels");
		if(numberOfFrontalWheelsProperty != null)
			numberOfFrontalWheels = Integer.valueOf(numberOfFrontalWheelsProperty);
		
		String numberOfRearWheelsProperty = reader.getXMLPropertyByPath("//global_data/number_of_rear_wheels");
		if(numberOfRearWheelsProperty != null)
			numberOfRearWheels = Integer.valueOf(numberOfRearWheelsProperty);
		
		//---------------------------------------------------------------
		// FRONTAL WHEEL DATA
		Amount<Length> frontalWheelsHeight = Amount.valueOf(0.0, SI.METER);
		Amount<Length> frontalWheelsWidth = Amount.valueOf(0.0, SI.METER);
		
		String frontalWheelsHeightProperty = reader.getXMLPropertyByPath("//frontal_wheels_data/wheel_height");
		if(frontalWheelsHeightProperty != null)
			frontalWheelsHeight = reader.getXMLAmountLengthByPath("//frontal_wheels_data/wheel_height");
		
		String frontalWheelsWidthProperty = reader.getXMLPropertyByPath("//frontal_wheels_data/wheel_width");
		if(frontalWheelsWidthProperty != null)
			frontalWheelsWidth = reader.getXMLAmountLengthByPath("//frontal_wheels_data/wheel_width");
		
		//---------------------------------------------------------------
		// REAR WING DATA
		Amount<Length> rearWheelsHeight = Amount.valueOf(0.0, SI.METER);
		Amount<Length> rearWheelsWidth = Amount.valueOf(0.0, SI.METER);
		
		String rearWheelsHeightProperty = reader.getXMLPropertyByPath("//rear_wheels_data/wheel_height");
		if(rearWheelsHeightProperty != null)
			rearWheelsHeight = reader.getXMLAmountLengthByPath("//rear_wheels_data/wheel_height");
		
		String rearWheelsWidthProperty = reader.getXMLPropertyByPath("//rear_wheels_data/wheel_width");
		if(rearWheelsWidthProperty != null)
			rearWheelsWidth = reader.getXMLAmountLengthByPath("//rear_wheels_data/wheel_width");
		
		LandingGears landingGears = new LandingGears(
				new ILandingGear.Builder()
				.setId(id)
				.setMainLegsLenght(mainGearLegsLength)
				.setDistanceBetweenWheels(distanceBetweenWheels)
				.setNumberOfFrontalWheels(numberOfFrontalWheels)
				.setNumberOfRearWheels(numberOfRearWheels)
				.setFrontalWheelsHeight(frontalWheelsHeight)
				.setFrontalWheelsWidth(frontalWheelsWidth)
				.setRearWheelsHeight(rearWheelsHeight)
				.setRearWheelsWidth(rearWheelsWidth)
				.build()
				);
		
		return landingGears;
		
	}
	
	@Override
	public String toString() {
		
		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tLanding gears\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + _theLandingGearInterface.getId() + "'\n")
				.append("\t·····································\n")
				.append("\tMain gear legs length: " + _theLandingGearInterface.getMainLegsLenght() + "\n")
				.append("\tDistance between wheels: " + _theLandingGearInterface.getDistanceBetweenWheels() + "\n")
				.append("\tNumber of frontal wheels: " + _theLandingGearInterface.getNumberOfFrontalWheels() + "\n")
				.append("\tNumber of rear wheels: " + _theLandingGearInterface.getNumberOfRearWheels() + "\n")
				.append("\t·····································\n")
				.append("\tFrontal wheels height: " + _theLandingGearInterface.getFrontalWheelsHeight() + "\n")
				.append("\tFrontal wheels width: " + _theLandingGearInterface.getFrontalWheelsWidth() + "\n")
				.append("\t·····································\n")
				.append("\tRear wheels height: " + _theLandingGearInterface.getRearWheelsHeight() + "\n")
				.append("\tRear wheels width: " + _theLandingGearInterface.getRearWheelsWidth() + "\n")
				.append("\t·····································\n")
				;
		
		return sb.toString();
		
	}
	
	//------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public ILandingGear getTheLandingGearsInterface() {
		return _theLandingGearInterface;
	}
	
	public void setTheLandingGearsInterface (ILandingGear theLandingGearsInterface) {
		this._theLandingGearInterface = theLandingGearsInterface;
	}
	
	public String getId() {
		return _theLandingGearInterface.getId();
	}

	public void setId (String id) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setId(id).build());
	}
	
	public Amount<Length> getMainLegsLenght() {
		return _theLandingGearInterface.getMainLegsLenght();
	}

	public void setMainLegsLenght(Amount<Length> lenght) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setMainLegsLenght(lenght).build());
	}
	
	public int getNumberOfFrontalWheels() {
		return _theLandingGearInterface.getNumberOfFrontalWheels();
	}

	public int getNumberOfRearWheels() {
		return _theLandingGearInterface.getNumberOfRearWheels();
	}

	public Amount<Length> getFrontalWheelsHeight() {
		return _theLandingGearInterface.getFrontalWheelsHeight();
	}

	public Amount<Length> getFrontalWheelsWidth() {
		return _theLandingGearInterface.getFrontalWheelsWidth();
	}

	public Amount<Length> getRearWheelsHeight() {
		return _theLandingGearInterface.getRearWheelsHeight();
	}

	public Amount<Length> getRearWheelsWidth() {
		return _theLandingGearInterface.getRearWheelsWidth();
	}

	public void setNumberOfFrontalWheels(int _numberOfFrontalWheels) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setNumberOfFrontalWheels(_numberOfFrontalWheels).build());
	}

	public void setNumberOfRearWheels(int _numberOfRearWheels) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setNumberOfRearWheels(_numberOfRearWheels).build());
	}

	public void setFrontalWheelsHeight(Amount<Length> _frontalWheelsHeight) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setFrontalWheelsHeight(_frontalWheelsHeight).build());
	}

	public void setFrontalWheelsWidth(Amount<Length> _frontalWheelsWidth) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setFrontalWheelsWidth(_frontalWheelsWidth).build());
	}

	public void setRearWheelsHeight(Amount<Length> _rearWheelsHeight) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setRearWheelsHeight(_rearWheelsHeight).build());
	}

	public void setRearWheelsWidth(Amount<Length> _rearWheelsWidth) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setRearWheelsWidth(_rearWheelsWidth).build());
	}
	
	public Amount<Length> getXApexConstructionAxesMainGear() {
		return _xApexConstructionAxesMainGear;
	};
	
	public void setXApexConstructionAxesMainGear (Amount<Length> xApexConstructionAxes) {
		this._xApexConstructionAxesMainGear = xApexConstructionAxes;
	};
	
	public Amount<Length> getYApexConstructionAxesMainGear() {
		return _yApexConstructionAxesMainGear;
	};
	
	public void setYApexConstructionAxesMainGear (Amount<Length> yApexConstructionAxes) {
		this._yApexConstructionAxesMainGear = yApexConstructionAxes; 
	};
	
	public Amount<Length> getZApexConstructionAxesMainGear() {
		return _zApexConstructionAxesMainGear;
	};
	
	public void setZApexConstructionAxesMainGear (Amount<Length> zApexConstructionAxes) {
		this._zApexConstructionAxesMainGear = zApexConstructionAxes;
	}

	public LandingGearsMountingPositionEnum getMountingPosition() {
		return _mountingPosition;
	}

	public Amount<Length> getXApexConstructionAxesNoseGear() {
		return _xApexConstructionAxesNoseGear;
	}

	public void setXApexConstructionAxesNoseGear(Amount<Length> _xApexConstructionAxesNoseGear) {
		this._xApexConstructionAxesNoseGear = _xApexConstructionAxesNoseGear;
	}

	public Amount<Length> getYApexConstructionAxesNoseGear() {
		return _yApexConstructionAxesNoseGear;
	}

	public void setYApexConstructionAxesNoseGear(Amount<Length> _yApexConstructionAxesNoseGear) {
		this._yApexConstructionAxesNoseGear = _yApexConstructionAxesNoseGear;
	}

	public Amount<Length> getZApexConstructionAxesNoseGear() {
		return _zApexConstructionAxesNoseGear;
	}

	public void setZApexConstructionAxesNoseGear(Amount<Length> _zApexConstructionAxesNoseGear) {
		this._zApexConstructionAxesNoseGear = _zApexConstructionAxesNoseGear;
	}

	public void setMountingPosition(LandingGearsMountingPositionEnum _mountingPosition) {
		this._mountingPosition = _mountingPosition;
	}

	public Amount<Length> getDistanceBetweenWheels() {
		return _theLandingGearInterface.getDistanceBetweenWheels();
	}

	public void setDistanceBetweenWheels(Amount<Length> _distanceBetweenWheels) {
		setTheLandingGearsInterface(ILandingGear.Builder.from(_theLandingGearInterface).setDistanceBetweenWheels(_distanceBetweenWheels).build());
	}

	public LandingGearsWeightManager getTheWeigths() {
		return _theWeigths;
	}

	public void setTheWeigths(LandingGearsWeightManager _theWeigths) {
		this._theWeigths = _theWeigths;
	}

	public LandingGearsBalanceManager getTheBalance() {
		return _theBalance;
	}

	public void setTheBalance(LandingGearsBalanceManager _theBalance) {
		this._theBalance = _theBalance;
	}
}
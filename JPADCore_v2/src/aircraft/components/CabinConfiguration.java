package aircraft.components;

import static java.lang.Math.round;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.unit.SI;

import org.jscience.physics.amount.Amount;

import aircraft.auxiliary.ISeatBlock;
import aircraft.auxiliary.SeatsBlock;
import calculators.balance.CenterOfGravityCalcUtils;
import configuration.MyConfiguration;
import configuration.enumerations.AnalysisTypeEnum;
import configuration.enumerations.ClassTypeEnum;
import configuration.enumerations.MethodEnum;
import configuration.enumerations.RelativePositionEnum;
import standaloneutils.JPADXmlReader;
import standaloneutils.MyArrayUtils;
import standaloneutils.MyXMLReaderUtils;
import standaloneutils.atmosphere.AtmosphereCalc;
import standaloneutils.customdata.MyArray;

/**
 * Define the cabin configuration (full economy, economy + business) and current flight configuration
 * (in terms of number of passengers, number of crew members).
 * 
 * Each element in the following lists is relative to a class:
 * index 0: ECONOMY
 * index 1: BUSINESS
 * index 2: FIRST
 * 
 * If a class is missing indexes are decreased by 1, e.g. if first class is
 * missing:
 * index 0: ECONOMY
 * index 1: BUSINESS
 * 
 * The number of passengers and their location is necessary for estimating 
 * the Aircraft Center of Gravity position.
 * 
 * @author Vittorio Trifari
 *
 */
public class CabinConfiguration {

	//------------------------------------------------------------------------------------
	// VARIABLE DECLARATION
	private ICabinConfiguration _theCabinConfigurationBuilder;
	private File _cabinConfigurationPath;
	
	//....................................................................................
	// Derived Input
	private int _cabinCrewNumber, _totalCrewNumber, _aislesNumber;
	
	private Map<Integer, Amount<Length>> _breaksMap;
	private List<SeatsBlock> _seatsBlocksList;
	private SeatsBlock _seatsBlockRight, _seatsBlockLeft, _seatsBlockCenter;
	private List<Amount<Length>> _pitchList; 
	private List<Amount<Length>> _widthList; 
	private List<Amount<Length>> _distanceFromWallList;
	private List<Integer> _numberOfBreaksList;
	private List<Integer> _numberOfRowsList;
	private List<Integer[]> _numberOfColumnsList;
	private List<Integer[]> _missingSeatsRowList;
	private List<ClassTypeEnum> _typeList;
	private List<Map<Integer, Amount<Length>>> _breaksMapList;

	private Map<MethodEnum, Amount<?>> _massMap;
	private Map<AnalysisTypeEnum, List<MethodEnum>> _methodsMap;
	private List<MethodEnum> _methodsList;
	private Double[] _percentDifference;
	private Amount<Mass> _massFurnishingsAndEquipment, _massEstimatedFurnishingsAndEquipment;

	private Amount<Length> _seatsCG;
	private List<Amount<Length>> _seatsCoGFrontToRear;
	private List<Amount<Length>> _seatsCoGRearToFront;
	private List<Amount<Mass>> _currentMassList;
	private List<Amount<Length>> _currentXCoGfrontToRearWindow;
	private List<Amount<Length>> _currentXCoGrearToFrontWindow;
	private List<Amount<Length>> _currentXCoGfrontToRearAisle;
	private List<Amount<Length>> _currentXCoGrearToFrontAisle;
	private List<Amount<Length>> _currentXCoGfrontToRearOther;
	private List<Amount<Length>> _currentXCoGrearToFrontOther;
	private List<Amount<Length>> _currentXCoGfrontToRear;
	private List<Amount<Length>> _currentXCoGrearToFront;
	private MyArray _xLoading, _yLoading;
	
	//------------------------------------------------------------------------------------
	// BUILDER
	public CabinConfiguration (ICabinConfiguration theCabinConfigurationBuilder) {
		
		this.setTheCabinConfigurationBuilder(theCabinConfigurationBuilder);
		
		this._currentMassList = new ArrayList<>();
		this._seatsCoGFrontToRear = new ArrayList<>();
		this._seatsCoGRearToFront = new ArrayList<>();
		this._currentMassList = new ArrayList<>();
		this._currentXCoGfrontToRearWindow = new ArrayList<>();
		this._currentXCoGrearToFrontWindow = new ArrayList<>();
		this._currentXCoGfrontToRearAisle = new ArrayList<>();
		this._currentXCoGrearToFrontAisle = new ArrayList<>();
		this._currentXCoGfrontToRearOther = new ArrayList<>();
		this._currentXCoGrearToFrontOther = new ArrayList<>();
		this._currentXCoGfrontToRear = new ArrayList<>();
		this._currentXCoGrearToFront = new ArrayList<>();
		
		this._massMap = new HashMap<>();
		this._methodsMap = new HashMap<>();
		this._methodsList = new ArrayList<>();
		
		this._breaksMap = new HashMap<>();
		this._seatsBlocksList = new ArrayList<>();
		this._pitchList = new ArrayList<>(); 
		this._widthList = new ArrayList<>(); 
		this._distanceFromWallList = new ArrayList<>();
		this._numberOfBreaksList = new ArrayList<>();
		this._numberOfRowsList = new ArrayList<>();
		this._numberOfColumnsList = new ArrayList<>();
		this._missingSeatsRowList = new ArrayList<>();
		this._typeList = new ArrayList<>();
		this._breaksMapList = new ArrayList<>();
		
		this.calculateDependentVariables();
	}
	
	//------------------------------------------------------------------------------------
	// METHODS
	@SuppressWarnings("unchecked")
	public static CabinConfiguration importFromXML (String pathToXML) {
		
		JPADXmlReader reader = new JPADXmlReader(pathToXML);

		System.out.println("Reading configuration data ...");
		
		String id = MyXMLReaderUtils
				.getXMLPropertyByPath(
						reader.getXmlDoc(), reader.getXpath(),
						"//@id");
		
		//------------------------------------------------------------------
		// GLOBAL DATA
		Integer actualPassengerNumber = null;
		Integer maximumPassengerNumber = null; 
		Integer flightCrewNumber = null;
		Integer classesNumber = null; 
		List<ClassTypeEnum> classesType = new ArrayList<>();
		Integer aislesNumber = null; 
		Amount<Length> xCoordinatesFirstRow = null; 
		List<Integer[]> missingSeatsRow = new ArrayList<>();
		
		//..................................................................		
		String actualPassengerNumberProperty =  reader.getXMLPropertyByPath("//global_data/actual_passengers_number");
		if(actualPassengerNumberProperty != null) 
			actualPassengerNumber = Integer.valueOf(actualPassengerNumberProperty);
		//..................................................................
		String maximumPassengerNumberProperty =  reader.getXMLPropertyByPath("//global_data/maximum_passengers_number");
		if(actualPassengerNumberProperty != null) 
			maximumPassengerNumber = Integer.valueOf(maximumPassengerNumberProperty);
		//..................................................................
		String flightCrewNumberProperty =  reader.getXMLPropertyByPath("//global_data/flight_crew_number");
		if(flightCrewNumberProperty != null)
			flightCrewNumber = Integer.valueOf(flightCrewNumberProperty);
		//..................................................................
		String classesNumberProperty =  reader.getXMLPropertyByPath("//global_data/classes_number");
		if(classesNumberProperty != null)
			classesNumber = Integer.valueOf(classesNumberProperty);
		//..................................................................
		List<String> classesTypeProperty = reader.getXMLPropertiesByPath("//global_data/classes_type");
		if(!classesTypeProperty.isEmpty()) {
			List<String> classesTypeList = JPADXmlReader.readArrayFromXML(
					reader.getXMLPropertiesByPath("//global_data/classes_type")
					.get(0));
			classesType = new ArrayList<ClassTypeEnum>();
			for(int i=0; i<classesTypeList.size(); i++) {
				if(classesTypeList.get(i).equalsIgnoreCase("ECONOMY"))
					classesType.add(ClassTypeEnum.ECONOMY);
				else if(classesTypeList.get(i).equalsIgnoreCase("BUSINESS"))
					classesType.add(ClassTypeEnum.BUSINESS);
				else if(classesTypeList.get(i).equalsIgnoreCase("FIRST"))
					classesType.add(ClassTypeEnum.FIRST);
				else {
					System.err.println("\n\tERROR: INVALID CLASS TYPE !!\n");
					return null;
				}
			}
		}
		//..................................................................
		String aislesNumberProperty =  reader.getXMLPropertyByPath("//global_data/aisles_number");
		if(aislesNumberProperty != null)
		aislesNumber = Integer.valueOf(aislesNumberProperty);
		//..................................................................
		String xCoordinatesFirstRowProperty =  reader.getXMLPropertyByPath("//global_data/x_coordinates_first_row");
		if(xCoordinatesFirstRowProperty != null)
			xCoordinatesFirstRow = reader.getXMLAmountLengthByPath("//global_data/x_coordinates_first_row");
		//..................................................................
		List<String> missingSeatsRowProperty = reader.getXMLPropertiesByPath("//value");
		if(missingSeatsRowProperty.size() != classesNumber) {
			System.err.println("THE NUMBER OF MISSING SEAT ROW TAGS HAVE TO BE EQUAL TO THE CLASSES NUMBER !!");
			return null;
		}
		if((missingSeatsRowProperty.isEmpty())) {
			for(int i=0; i<classesNumber; i++)
				missingSeatsRow.add(new Integer[] {-1});
		}
		else {
			for(int i=0; i<classesNumber; i++){
				List<String> tempString = JPADXmlReader.readArrayFromXML(
						missingSeatsRowProperty.get(i)
						);
				Integer[] tempInt = new Integer[tempString.size()];
				for(int j=0; j<tempString.size(); j++) {
					tempInt[j] = Integer.valueOf(tempString.get(j));
				}
				missingSeatsRow.add(tempInt);
			}
		}
		if(missingSeatsRow.size() != classesNumber) {
			System.err.println("ERROR : MISSING SEATS ROW LIST MUST HAVE THE SAME SIZE OF CLASSES NUMBER");
			return null;
		}
		
		//---------------------------------------------------------------
		// DETAILED DATA
		Integer numberOfBreaksEconomyClass = null;
		Integer numberOfBreaksBusinessClass = null;
		Integer numberOfBreaksFirstClass = null;
		Integer numberOfRowsEconomyClass = null;
		Integer numberOfRowsBusinessClass = null;
		Integer numberOfRowsFirstClass = null;
		Integer[] numberOfColumnsEconomyClass = null;
		Integer[] numberOfColumnsBusinessClass = null;
		Integer[] numberOfColumnsFirstClass = null;
		Amount<Length> pitchEconomyClass = null;
		Amount<Length> pitchBusinessClass = null;
		Amount<Length> pitchFirstClass = null;
		Amount<Length> widthEconomyClass = null;
		Amount<Length> widthBusinessClass = null;
		Amount<Length> widthFirstClass = null;
		Amount<Length> distanceFromWallEconomyClass = null;
		Amount<Length> distanceFromWallBusinessClass = null;
		Amount<Length> distanceFromWallFirstClass = null;

		//..................................................................
		String numberOfBreaksEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_breaks_economy_class");
		if(numberOfBreaksEconomyClassProperty != null)
			numberOfBreaksEconomyClass = Integer.valueOf(numberOfBreaksEconomyClassProperty);
		//..................................................................
		String numberOfBreaksBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_breaks_business_class");
		if(numberOfBreaksBusinessClassProperty != null)
			numberOfBreaksBusinessClass = Integer.valueOf(numberOfBreaksBusinessClassProperty);
		//..................................................................
		String numberOfBreaksFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_breaks_first_class");
		if(numberOfBreaksFirstClassProperty != null)
			numberOfBreaksFirstClass = Integer.valueOf(numberOfBreaksFirstClassProperty);
		//..................................................................
		String numberOfRowsEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_rows_economy_class");
		if(numberOfRowsEconomyClassProperty != null)
			numberOfRowsEconomyClass = Integer.valueOf(numberOfRowsEconomyClassProperty);
		//..................................................................
		String numberOfRowsBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_rows_business_class");
		if(numberOfRowsBusinessClassProperty != null)
			numberOfRowsBusinessClass = Integer.valueOf(numberOfRowsBusinessClassProperty);
		//..................................................................
		String numberOfRowsFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/number_of_rows_first_class");
		if(numberOfRowsFirstClassProperty != null)
			numberOfRowsFirstClass = Integer.valueOf(numberOfRowsFirstClassProperty);
		//..................................................................
		List<String> numberOfColumnsEconomyClassProperty = reader.getXMLPropertiesByPath("//detailed_data/number_of_columns_economy_class");
		if(!numberOfColumnsEconomyClassProperty.isEmpty()) {
			List<String> numberOfColumnsEconomyClassArray = JPADXmlReader
					.readArrayFromXML(
							reader.getXMLPropertiesByPath(
									"//detailed_data/number_of_columns_economy_class"
									)
							.get(0)
							);
			numberOfColumnsEconomyClass = new Integer[numberOfColumnsEconomyClassArray.size()];
			for(int i=0; i<numberOfColumnsEconomyClassArray.size(); i++)
				numberOfColumnsEconomyClass[i] = Integer.valueOf(
						numberOfColumnsEconomyClassArray.get(i)
						);
		}
		//..................................................................
		List<String> numberOfColumnsBusinessClassProperty = reader.getXMLPropertiesByPath("//detailed_data/number_of_columns_business_class");
		if(!numberOfColumnsBusinessClassProperty.isEmpty()) {
			List<String> numberOfColumnsBusinessClassArray = JPADXmlReader
					.readArrayFromXML(
							reader.getXMLPropertiesByPath(
									"//detailed_data/number_of_columns_business_class"
									)
							.get(0)
							);
			numberOfColumnsBusinessClass = new Integer[numberOfColumnsBusinessClassArray.size()];
			for(int i=0; i<numberOfColumnsBusinessClassArray.size(); i++)
				numberOfColumnsBusinessClass[i] = Integer.valueOf(
						numberOfColumnsBusinessClassArray.get(i)
						);
		}
		//..................................................................
		List<String> numberOfColumnsFirstClassProperty = reader.getXMLPropertiesByPath("//detailed_data/number_of_columns_first_class");
		if(!numberOfColumnsFirstClassProperty.isEmpty()) {
			List<String> numberOfColumnsFirstClassArray = JPADXmlReader
					.readArrayFromXML(
							reader.getXMLPropertiesByPath(
									"//detailed_data/number_of_columns_first_class"
									)
							.get(0)
							);
			numberOfColumnsFirstClass = new Integer[numberOfColumnsFirstClassArray.size()];
			for(int i=0; i<numberOfColumnsFirstClassArray.size(); i++)
				numberOfColumnsFirstClass[i] = Integer.valueOf(
						numberOfColumnsFirstClassArray.get(i)
						);
		}
		//..................................................................
		String pitchEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/pitch_economy_class");
		if(pitchEconomyClassProperty != null)
		pitchEconomyClass = reader.getXMLAmountLengthByPath("//detailed_data/pitch_economy_class");
		//..................................................................
		String pitchBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/pitch_business_class");
		if(pitchBusinessClassProperty != null)
			pitchBusinessClass = reader.getXMLAmountLengthByPath("//detailed_data/pitch_business_class");
		//..................................................................
		String pitchFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/pitch_first_class");
		if(pitchFirstClassProperty != null)
			pitchFirstClass = reader.getXMLAmountLengthByPath("//detailed_data/pitch_first_class");
		//..................................................................
		String widthEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/width_economy_class");
		if(widthEconomyClassProperty != null)
			widthEconomyClass = reader.getXMLAmountLengthByPath("//detailed_data/width_economy_class");
		//..................................................................
		String widthBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/width_business_class");
		if(widthBusinessClassProperty != null)
			widthBusinessClass = reader.getXMLAmountLengthByPath("//detailed_data/width_business_class");
		//..................................................................
		String widthFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/width_first_class");
		if(widthFirstClassProperty != null)
			widthFirstClass = reader.getXMLAmountLengthByPath("//detailed_data/width_first_class");
		//..................................................................
		String distanceFromWallEconomyClassProperty = reader.getXMLPropertyByPath("//detailed_data/distance_from_wall_economy_class");
		if(distanceFromWallEconomyClassProperty != null)
			distanceFromWallEconomyClass = reader.getXMLAmountLengthByPath("//detailed_data/distance_from_wall_economy_class");
		//..................................................................
		String distanceFromWallBusinessClassProperty = reader.getXMLPropertyByPath("//detailed_data/distance_from_wall_business_class");
		if(distanceFromWallBusinessClassProperty != null)
			distanceFromWallBusinessClass = reader.getXMLAmountLengthByPath("//detailed_data/distance_from_wall_business_class");
		//..................................................................
		String distanceFromWallFirstClassProperty = reader.getXMLPropertyByPath("//detailed_data/distance_from_wall_first_class");
		if(distanceFromWallFirstClassProperty != null)
			distanceFromWallFirstClass = reader.getXMLAmountLengthByPath("//detailed_data/distance_from_wall_first_class");
		
		//---------------------------------------------------------------
		//REFERENCE MASS
		Amount<Mass> massFurnishingsAndEquipment = null;
		
		String massFurnishingsAndEquipmentProperty = reader.getXMLPropertyByPath("//reference_masses/mass_furnishings_and_equipment");
		if(massFurnishingsAndEquipmentProperty != null)
			massFurnishingsAndEquipment = (Amount<Mass>) reader.getXMLAmountWithUnitByPath("//reference_masses/mass_furnishings_and_equipment");
		else
			massFurnishingsAndEquipment = Amount.valueOf(0.0, SI.KILOGRAM);
		
		CabinConfiguration aircraftConfiguration = new CabinConfiguration(
				new ICabinConfiguration.Builder()
				.setId(id)
				.setActualPassengerNumber(actualPassengerNumber)
				.setMaximumPassengerNumber(maximumPassengerNumber)
				.setFlightCrewNumber(flightCrewNumber)
				.setClassesNumber(classesNumber)
				.addAllClassesType(classesType)
				.setAislesNumber(aislesNumber)
				.setXCoordinatesFirstRow(xCoordinatesFirstRow)
				.addAllMissingSeatsRow(missingSeatsRow)
				.setNumberOfBreaksEconomyClass(numberOfBreaksEconomyClass)
				.setNumberOfBreaksBusinessClass(numberOfBreaksBusinessClass)
				.setNumberOfBreaksFirstClass(numberOfBreaksFirstClass)
				.setNumberOfRowsEconomyClass(numberOfRowsEconomyClass)
				.setNumberOfRowsBusinessClass(numberOfRowsBusinessClass)
				.setNumberOfRowsFirstClass(numberOfRowsFirstClass)
				.setNumberOfColumnsEconomyClass(numberOfColumnsEconomyClass)
				.setNumberOfColumnsBusinessClass(numberOfColumnsBusinessClass)
				.setNumberOfColumnsFirstClass(numberOfColumnsFirstClass)
				.setPitchEconomyClass(pitchEconomyClass)
				.setPitchBusinessClass(pitchBusinessClass)
				.setPitchFirstClass(pitchFirstClass)
				.setWidthEconomyClass(widthEconomyClass)
				.setWidthBusinessClass(widthBusinessClass)
				.setWidthFirstClass(widthFirstClass)
				.setDistanceFromWallEconomyClass(distanceFromWallEconomyClass)
				.setDistanceFromWallBusinessClass(distanceFromWallBusinessClass)
				.setDistanceFromWallFirstClass(distanceFromWallFirstClass)
				.setMassFurnishingsAndEquipment(massFurnishingsAndEquipment)
				.build()
				);
		
		return aircraftConfiguration;
	}
	
	public List<Amount<Length>> getCurrentXCoGfrontToRear() {

		_currentXCoGfrontToRear.addAll(_currentXCoGfrontToRearWindow);
		_currentXCoGfrontToRear.addAll(_currentXCoGfrontToRearAisle);
		_currentXCoGfrontToRear.addAll(_currentXCoGfrontToRearOther);
		return _currentXCoGfrontToRear;
	}

	public List<Amount<Length>> getCurrentXCoGrearToFront() {

		_currentXCoGrearToFront.addAll(_currentXCoGrearToFrontWindow);
		_currentXCoGrearToFront.addAll(_currentXCoGrearToFrontAisle);
		_currentXCoGrearToFront.addAll(_currentXCoGrearToFrontOther);
		return _currentXCoGrearToFront;
	}
	
	public void updateConfiguration() {

		_pitchList = new ArrayList<Amount<Length>>();
		_pitchList.add(getTheCabinConfigurationBuilder().getPitchEconomyClass());
		_pitchList.add(getTheCabinConfigurationBuilder().getPitchBusinessClass());
		_pitchList.add(getTheCabinConfigurationBuilder().getPitchFirstClass());

		_widthList = new ArrayList<Amount<Length>>();
		_widthList.add(getTheCabinConfigurationBuilder().getWidthEconomyClass());
		_widthList.add(getTheCabinConfigurationBuilder().getWidthBusinessClass());
		_widthList.add(getTheCabinConfigurationBuilder().getWidthFirstClass());

		_distanceFromWallList = new ArrayList<Amount<Length>>();
		_distanceFromWallList.add(getTheCabinConfigurationBuilder().getDistanceFromWallEconomyClass());
		_distanceFromWallList.add(getTheCabinConfigurationBuilder().getDistanceFromWallBusinessClass());
		_distanceFromWallList.add(getTheCabinConfigurationBuilder().getDistanceFromWallFirstClass());

		_numberOfBreaksList = new ArrayList<Integer>();
		_numberOfBreaksList.add(getTheCabinConfigurationBuilder().getNumberOfBreaksEconomyClass());
		_numberOfBreaksList.add(getTheCabinConfigurationBuilder().getNumberOfBreaksBusinessClass());
		_numberOfBreaksList.add(getTheCabinConfigurationBuilder().getNumberOfBreaksFirstClass());

		_numberOfRowsList = new ArrayList<Integer>();
		_numberOfRowsList.add(getTheCabinConfigurationBuilder().getNumberOfRowsEconomyClass());
		_numberOfRowsList.add(getTheCabinConfigurationBuilder().getNumberOfRowsBusinessClass());
		_numberOfRowsList.add(getTheCabinConfigurationBuilder().getNumberOfRowsFirstClass());

		_numberOfColumnsList = new ArrayList<Integer[]>();
		_numberOfColumnsList.add(getTheCabinConfigurationBuilder().getNumberOfColumnsEconomyClass());
		_numberOfColumnsList.add(getTheCabinConfigurationBuilder().getNumberOfColumnsBusinessClass());
		_numberOfColumnsList.add(getTheCabinConfigurationBuilder().getNumberOfColumnsFirstClass());
		
	}

	public void calculateDependentVariables() {

		_cabinCrewNumber = (int) Math.ceil(getTheCabinConfigurationBuilder().getActualPassengerNumber()/35);
		_totalCrewNumber = _cabinCrewNumber + getTheCabinConfigurationBuilder().getFlightCrewNumber();
		updateConfiguration();

	}

	/**
	 * Build a simplified cabin layout: the user has to define the x coordinate
	 * at which the layout starts, the number of classes and, for each class,
	 * number of rows and of abreast, pitch, width, and eventually breaks
	 * (empty spaces between seats). The method does not consider missing seats
	 * and differences between two groups of seats (a group is such when 
	 * separated from another one by an aisle).
	 * 
	 * @param aircraft
	 * 
	 */
	public void buildSimpleLayout(Aircraft aircraft) {

		updateConfiguration();
		Amount<Length> length = Amount.valueOf(0., SI.METER);

		for (int i = 0; i < getTheCabinConfigurationBuilder().getClassesNumber(); i++) {

			_breaksMap.put(_numberOfBreaksList.get(i), Amount.valueOf(0., SI.METER));
			_breaksMapList.add(_breaksMap);
			
			_seatsBlockRight = new SeatsBlock(
					new ISeatBlock.Builder()
					.setPosition(RelativePositionEnum.LEFT)
					.setXStart(_theCabinConfigurationBuilder.getXCoordinatesFirstRow().plus(length))
					.setPitch(_pitchList.get(i))
					.setWidth(_widthList.get(i))
					.setDistanceFromWall(_distanceFromWallList.get(i))
					.putAllBreaksMap(_breaksMapList.get(i))
					.setRowsNumber(_numberOfRowsList.get(i))
					.setColumnsNumber(_numberOfColumnsList.get(i)[0])
					.setMissingSeatRow(_theCabinConfigurationBuilder.getMissingSeatsRow().get(i))
					.setType(_typeList.get(i))
					.build()
					);
//			_seatsBlockLeft.calculateCG(aircraft);
			
			_seatsBlockLeft = new SeatsBlock(
					new ISeatBlock.Builder()
					.setPosition(RelativePositionEnum.RIGHT)
					.setXStart(_theCabinConfigurationBuilder.getXCoordinatesFirstRow().plus(length))
					.setPitch(_pitchList.get(i))
					.setWidth(_widthList.get(i))
					.setDistanceFromWall(_distanceFromWallList.get(i))
					.putAllBreaksMap(_breaksMapList.get(i))
					.setRowsNumber(_numberOfRowsList.get(i))
					.setColumnsNumber(_numberOfColumnsList.get(i)[1])
					.setMissingSeatRow(_missingSeatsRowList.get(i))
					.setType(_typeList.get(i))
					.build()
					);
//			_seatsBlockRight.calculateCG(aircraft);

			if (_aislesNumber > 1) {

				_seatsBlockCenter = new SeatsBlock(
						new ISeatBlock.Builder()
						.setPosition(RelativePositionEnum.CENTER)
						.setXStart(_theCabinConfigurationBuilder.getXCoordinatesFirstRow().plus(length))
						.setPitch(_pitchList.get(i))
						.setWidth(_widthList.get(i))
						.setDistanceFromWall(_distanceFromWallList.get(i))
						.putAllBreaksMap(_breaksMapList.get(i))
						.setRowsNumber(_numberOfRowsList.get(i))
						.setColumnsNumber(_numberOfColumnsList.get(i)[2])
						.setMissingSeatRow(_missingSeatsRowList.get(i))
						.setType(_typeList.get(i))
						.build()
						);

//				_seatsBlockCenter.calculateCoG(aircraft);
				_seatsBlocksList.add(_seatsBlockLeft);
				_seatsBlocksList.add(_seatsBlockRight);
				_seatsBlocksList.add(_seatsBlockCenter);

			} else {
				_seatsBlocksList.add(_seatsBlockLeft);	
				_seatsBlocksList.add(_seatsBlockRight);
			}

			length = _seatsBlockRight.getLenghtOverall();

		}

		// CG variation during boarding procedure in aircraft reference frame
		_currentMassList = new ArrayList<>();
		_currentMassList.add(aircraft.getTheAnalysisManager().getTheBalance().getOperatingEmptyMass());
		
		_seatsCoGFrontToRear = new ArrayList<>();
		_seatsCoGFrontToRear.add(aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF());

		_seatsCoGRearToFront = new ArrayList<>();
		_seatsCoGRearToFront.add(aircraft.getTheAnalysisManager().getTheBalance().getCGOEM().getXBRF());
		
		CenterOfGravityCalcUtils.calculateCGBoarding(_seatsBlocksList, aircraft);
		_seatsCoGFrontToRear.addAll(_currentXCoGfrontToRear);
		_seatsCoGRearToFront.addAll(_currentXCoGrearToFront);

		_xLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_seatsCoGFrontToRear));
		_xLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_seatsCoGRearToFront));
		
		_yLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_currentMassList));
		_yLoading.concat(MyArrayUtils.convertListOfAmountToDoubleArray(_currentMassList));

//		_seatsCG = SeatsBlock.calculateTotalCoG(_seatsBlocksList).plus(_xCoordinateFirstRow);
//
//		System.out.println("Total CoG: " + _seatsCG);

		System.out.println("----- CABIN LAYOUT CREATION FINISHED -----");

	}

	public void calculateMass(Aircraft aircraft, MethodEnum method) {
		calculateMassFurnishings(aircraft, method);
	}

	public void calculateMassFurnishings(Aircraft aircraft,	MethodEnum method) {
		switch (method) {
		case TORENBEEK_2013: { // page 257 Torenbeek 2013

			_massFurnishingsAndEquipment = Amount.valueOf(
					(12
							* aircraft.getFuselage().getFuselageCreator().getFuselageLength().getEstimatedValue()
							* aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue() 
							* ( 3
									* aircraft.getFuselage().getFuselageCreator().getEquivalentDiameterCylinderGM().getEstimatedValue() 
									+ 0.5 * aircraft.getFuselage().getFuselageCreator().getDeckNumber() + 1) + 3500) /
					AtmosphereCalc.g0.getEstimatedValue(),
					SI.KILOGRAM);

			_methodsList.add(method);
			_massMap.put(method, Amount.valueOf(
					round(_massFurnishingsAndEquipment.getEstimatedValue()), SI.KILOGRAM));
		}

		default: {} break;

		}

		_methodsMap.put(AnalysisTypeEnum.WEIGHTS, _methodsList);
		_percentDifference = new Double[_massMap.size()];

		_massEstimatedFurnishingsAndEquipment = _massFurnishingsAndEquipment;
	}
	
	@Override
	public String toString() {

		MyConfiguration.customizeAmountOutput();

		StringBuilder sb = new StringBuilder()
				.append("\t-------------------------------------\n")
				.append("\tConfiguration\n")
				.append("\t-------------------------------------\n")
				.append("\tID: '" + getTheCabinConfigurationBuilder().getId() + "'\n")
				.append("\t.....................................\n")
				.append("\tActual number of passengers: " + getTheCabinConfigurationBuilder().getActualPassengerNumber() + "\n")
				.append("\tMaximum number of passengers: " + getTheCabinConfigurationBuilder().getMaximumPassengerNumber() + "\n")
				.append("\tFlight crew number: " + getTheCabinConfigurationBuilder().getFlightCrewNumber() + "\n")
				.append("\tCabin crew number: " + _cabinCrewNumber + "\n")
				.append("\tTotal crew number: " + _totalCrewNumber + "\n")
				.append("\tClasses number: " + getTheCabinConfigurationBuilder().getClassesNumber() + "\n")
				.append("\tClasses type: " + _typeList + "\n")
				.append("\tAisles number: " + _aislesNumber + "\n")
				.append("\tX coordinates first row: " + getTheCabinConfigurationBuilder().getXCoordinatesFirstRow() + "\n");

		if((_missingSeatsRowList.size() == 1) && (_missingSeatsRowList.get(0).equals(-1)))
			sb.append("\tMissing seats each row: " + 0 + "\n");
		else {
			sb.append("\tMissing seats each row: \n");
			for (int i=0; i<_missingSeatsRowList.size(); i++)
				sb.append("\t\t" + Arrays.toString(_missingSeatsRowList.get(i)) + " ");
		}
		
		sb.append("\n\t.....................................\n")
		.append("\tNumber of breaks economy class: " + getTheCabinConfigurationBuilder().getNumberOfBreaksEconomyClass() + "\n")
		.append("\tNumber of breaks business class: " + getTheCabinConfigurationBuilder().getNumberOfBreaksBusinessClass() + "\n")
		.append("\tNumber of breaks first class: " + getTheCabinConfigurationBuilder().getNumberOfBreaksFirstClass() + "\n")
		.append("\tNumber of rows economy class: " + getTheCabinConfigurationBuilder().getNumberOfRowsEconomyClass() + "\n")
		.append("\tNumber of rows business class: " + getTheCabinConfigurationBuilder().getNumberOfRowsBusinessClass() + "\n")
		.append("\tNumber of rows first class: " + getTheCabinConfigurationBuilder().getNumberOfRowsFirstClass() + "\n")
		.append("\tNumber of columns economy class: " + Arrays.toString(getTheCabinConfigurationBuilder().getNumberOfColumnsEconomyClass()) + "\n")
		.append("\tNumber of columns business class: " + Arrays.toString(getTheCabinConfigurationBuilder().getNumberOfColumnsBusinessClass()) + "\n")
		.append("\tNumber of columns first class: " + Arrays.toString(getTheCabinConfigurationBuilder().getNumberOfColumnsFirstClass()) + "\n")
		.append("\tPitch economy class: " + getTheCabinConfigurationBuilder().getPitchEconomyClass() + "\n")
		.append("\tPitch business class: " + getTheCabinConfigurationBuilder().getPitchBusinessClass() + "\n")
		.append("\tPitch first class: " + getTheCabinConfigurationBuilder().getPitchFirstClass() + "\n")
		.append("\tWidth economy class: " + getTheCabinConfigurationBuilder().getWidthEconomyClass() + "\n")
		.append("\tWidth business class: " + getTheCabinConfigurationBuilder().getWidthBusinessClass() + "\n")
		.append("\tWidth first class: " + getTheCabinConfigurationBuilder().getWidthFirstClass() + "\n")
		.append("\tDistance from wall economy class: " + getTheCabinConfigurationBuilder().getDistanceFromWallEconomyClass() + "\n")
		.append("\tDistance from wall business class: " + getTheCabinConfigurationBuilder().getDistanceFromWallBusinessClass() + "\n")
		.append("\tDistance from wall first class: " + getTheCabinConfigurationBuilder().getDistanceFromWallFirstClass() + "\n")
		.append("\t.....................................\n")
		.append("\tReference mass of furnishings and equipments: " + getTheCabinConfigurationBuilder().getMassFurnishingsAndEquipment() + "\n")
		.append("\t.....................................\n");

		return sb.toString();
	}

	//---------------------------------------------------------------------------------------------------
	// GETTERS & SETTERS
	
	public File getCabinConfigurationPath() {
		return _cabinConfigurationPath;
	}

	public void setCabinConfigurationPath(File _cabinConfigurationPath) {
		this._cabinConfigurationPath = _cabinConfigurationPath;
	}

	public ICabinConfiguration getTheCabinConfigurationBuilder() {
		return _theCabinConfigurationBuilder;
	}

	public void setTheCabinConfigurationBuilder(ICabinConfiguration _theCabinConfigurationBuilder) {
		this._theCabinConfigurationBuilder = _theCabinConfigurationBuilder;
	}

	public String getId() {
		return _theCabinConfigurationBuilder.getId();
	}
	
	public void setId (String id) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setId(id).build());
	}
	
	public int getActualPassengerNumber() {
		return _theCabinConfigurationBuilder.getActualPassengerNumber();
	}
	
	public void setActualPassengerNumber (int actualPassengerNumber) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setActualPassengerNumber(actualPassengerNumber).build());
	}
	
	public int getMaximumPassengerNumber(){
		return _theCabinConfigurationBuilder.getMaximumPassengerNumber();
	}
	
	public void setMaximumPassengerNumber (int maxPassengerNumber) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setMaximumPassengerNumber(maxPassengerNumber).build());
	}
	
	public int getFlightCrewNumber(){
		return _theCabinConfigurationBuilder.getFlightCrewNumber();
	}
	
	public void setFlightCrewNumber (int flightCrewNumber) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setFlightCrewNumber(flightCrewNumber).build());
	}
	
	public int getClassesNumber(){
		return _theCabinConfigurationBuilder.getClassesNumber();
	}
	
	public void setClassesNumber (int classesNumber) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setClassesNumber(classesNumber).build());
	}
	
	public List<ClassTypeEnum> getClassesType(){
		return _theCabinConfigurationBuilder.getClassesType();
	}
	
	public void setClassesType (List<ClassTypeEnum> classesType) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).addAllClassesType(classesType).build());
	}
	
	public int getAislesNumber(){
		return _theCabinConfigurationBuilder.getAislesNumber();
	}
	
	public void setAislesNumber (int aislesNumber) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setAislesNumber(aislesNumber).build());
	}
	
	public Amount<Length> getXCoordinatesFirstRow(){
		return _theCabinConfigurationBuilder.getXCoordinatesFirstRow();
	}
	
	public void setXCoordinatesFirstRow (Amount<Length> xCoordinateFirstRow) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setXCoordinatesFirstRow(xCoordinateFirstRow).build());
	}
	
	public List<Integer[]> getMissingSeatsRow(){
		return _theCabinConfigurationBuilder.getMissingSeatsRow();
	}
	
	public void setMissingSeatsRow (List<Integer[]> missingSeatRow) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).addAllMissingSeatsRow(missingSeatRow).build());
	}
	
	public int getNumberOfBreaksEconomyClass(){
		return _theCabinConfigurationBuilder.getNumberOfBreaksEconomyClass();
	}
	
	public void setNumberOfBreaksEconomyClass (int numberOfBrakesEconomyClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfBreaksEconomyClass(numberOfBrakesEconomyClass).build());
	}
	
	public int getNumberOfBreaksBusinessClass(){
		return _theCabinConfigurationBuilder.getNumberOfBreaksBusinessClass();
	}
	
	public void setNumberOfBreaksBusinessClass (int numberOfBrakesBusinessClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfBreaksBusinessClass(numberOfBrakesBusinessClass).build());
	}
	
	public int getNumberOfBreaksFirstClass(){
		return _theCabinConfigurationBuilder.getNumberOfBreaksFirstClass();
	}
	
	public void setNumberOfBreaksFirstClass (int numberOfBrakesFirstClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfBreaksFirstClass(numberOfBrakesFirstClass).build());
	}
	
	public int getNumberOfRowsEconomyClass(){
		return _theCabinConfigurationBuilder.getNumberOfRowsEconomyClass();
	}
	
	public void setNumberOfRowsEconomyClass (int numberOfRowsEconomyClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfRowsEconomyClass(numberOfRowsEconomyClass).build());
	}
	
	public int getNumberOfRowsBusinessClass(){
		return _theCabinConfigurationBuilder.getNumberOfRowsBusinessClass();
	}
	
	public void setNumberOfRowsBusinessClass (int numberOfRowsBusinessClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfRowsBusinessClass(numberOfRowsBusinessClass).build());
	}
	
	public int getNumberOfRowsFirstClass(){
		return _theCabinConfigurationBuilder.getNumberOfRowsFirstClass();
	}
	
	public void setNumberOfRowsFirstClass (int numberOfRowsFirstClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfRowsFirstClass(numberOfRowsFirstClass).build());
	}
	
	public 	Integer[] getNumberOfColumnsEconomyClass(){
		return _theCabinConfigurationBuilder.getNumberOfColumnsEconomyClass();
	}
	
	public void setNumberOfColumnsEconomyClass (Integer[] numberOfColumnsEconomyClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfColumnsEconomyClass(numberOfColumnsEconomyClass).build());
	}
	
	public Integer[] getNumberOfColumnsBusinessClass(){
		return _theCabinConfigurationBuilder.getNumberOfColumnsBusinessClass();
	}
	
	public void setNumberOfColumnsBusinessClass (Integer[] numberOfColumnsBusinessClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfColumnsBusinessClass(numberOfColumnsBusinessClass).build());
	}
	
	public Integer[] getNumberOfColumnsFirstClass(){
		return _theCabinConfigurationBuilder.getNumberOfColumnsFirstClass();
	}
	
	public void setNumberOfColumnsFirstClass (Integer[] numberOfColumnsFirstClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setNumberOfColumnsFirstClass(numberOfColumnsFirstClass).build());
	}
	
	public Amount<Length> getPitchEconomyClass(){
		return _theCabinConfigurationBuilder.getPitchEconomyClass();
	}
	
	public void setPitchEconomyClass (Amount<Length> pitchEconomyClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setPitchEconomyClass(pitchEconomyClass).build());
	}
	
	public Amount<Length> getPitchBusinessClass(){
		return _theCabinConfigurationBuilder.getPitchBusinessClass();
	}
	
	public void setPitchBusinessClass (Amount<Length> pitchBusinessClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setPitchBusinessClass(pitchBusinessClass).build());
	}
	
	public Amount<Length> getPitchFirstClass(){
		return _theCabinConfigurationBuilder.getPitchFirstClass();
	}
	
	public void setPitchFirstClass (Amount<Length> pitchFirstClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setPitchFirstClass(pitchFirstClass).build());
	}
	
	public Amount<Length> getWidthEconomyClass(){
		return _theCabinConfigurationBuilder.getWidthEconomyClass();
	}
	
	public void setWidthEconomyClass (Amount<Length> widthEconomyClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setWidthEconomyClass(widthEconomyClass).build());
	}
	
	public Amount<Length> getWidthBusinessClass(){
		return _theCabinConfigurationBuilder.getWidthBusinessClass();
	}
	
	public void setWidthBusinessClass (Amount<Length> widthBusinessClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setWidthBusinessClass(widthBusinessClass).build());
	}
	
	public Amount<Length> getWidthFirstClass(){
		return _theCabinConfigurationBuilder.getWidthFirstClass();
	}
	
	public void setWidthFirstClass (Amount<Length> widthFirstClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setWidthFirstClass(widthFirstClass).build());
	}
	
	public Amount<Length> getDistanceFromWallEconomyClass(){
		return _theCabinConfigurationBuilder.getDistanceFromWallEconomyClass();
	}
	
	public void setDistanceFromWallEconomyClass (Amount<Length> distanceFromWallEconomyClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setDistanceFromWallEconomyClass(distanceFromWallEconomyClass).build());
	}
	
	public Amount<Length> getDistanceFromWallBusinessClass(){
		return _theCabinConfigurationBuilder.getDistanceFromWallBusinessClass();
	}
	
	public void setDistanceFromWallBusinessClass (Amount<Length> distanceFromWallBusinessClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setDistanceFromWallBusinessClass(distanceFromWallBusinessClass).build());
	}
	
	public Amount<Length> getDistanceFromWallFirstClass(){
		return _theCabinConfigurationBuilder.getDistanceFromWallFirstClass();
	}
	
	public void setDistanceFromWallFirstClass (Amount<Length> distanceFromWallFirstClass) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setDistanceFromWallFirstClass(distanceFromWallFirstClass).build());
	}
	
	public Amount<Mass> getMassFurnishingsAndEquipmentReference(){
		return _theCabinConfigurationBuilder.getMassFurnishingsAndEquipment();
	}
	
	public void setMassFurnishingsAndEquipmentReference (Amount<Mass> massFurnishingsAndEquipmentReference) {
		setTheCabinConfigurationBuilder(ICabinConfiguration.Builder.from(_theCabinConfigurationBuilder).setMassFurnishingsAndEquipment(massFurnishingsAndEquipmentReference).build());
	}
	
	public List<Amount<Mass>> getCurrentMassList() {
		return _currentMassList;
	}

	public void setCurrentMassList(List<Amount<Mass>> currentMassList) {
		this._currentMassList = currentMassList;
	}

	public List<Amount<Length>> getCurrentXCoGfrontToRearWindow() {
		return _currentXCoGfrontToRearWindow;
	}

	public void setCurrentXCoGfrontToRearWindow(List<Amount<Length>> currentXCoGfrontToRearWindow) {
		this._currentXCoGfrontToRearWindow = currentXCoGfrontToRearWindow;
	}

	public List<Amount<Length>> getCurrentXCoGrearToFrontWindow() {
		return _currentXCoGrearToFrontWindow;
	}

	public void setCurrentXCoGrearToFrontWindow(List<Amount<Length>> currentXCoGrearToFrontWindow) {
		this._currentXCoGrearToFrontWindow = currentXCoGrearToFrontWindow;
	}

	public List<Amount<Length>> getCurrentXCoGfrontToRearAisle() {
		return _currentXCoGfrontToRearAisle;
	}

	public void setCurrentXCoGfrontToRearAisle(List<Amount<Length>> currentXCoGfrontToRearAisle) {
		this._currentXCoGfrontToRearAisle = currentXCoGfrontToRearAisle;
	}

	public List<Amount<Length>> getCurrentXCoGrearToFrontAisle() {
		return _currentXCoGrearToFrontAisle;
	}

	public void setCurrentXCoGrearToFrontAisle(List<Amount<Length>> currentXCoGrearToFrontAisle) {
		this._currentXCoGrearToFrontAisle = currentXCoGrearToFrontAisle;
	}

	public List<Amount<Length>> getCurrentXCoGfrontToRearOther() {
		return _currentXCoGfrontToRearOther;
	}

	public void setCurrentXCoGfrontToRearOther(List<Amount<Length>> currentXCoGfrontToRearOther) {
		this._currentXCoGfrontToRearOther = currentXCoGfrontToRearOther;
	}

	public List<Amount<Length>> getCurrentXCoGrearToFrontOther() {
		return _currentXCoGrearToFrontOther;
	}

	public void setCurrentXCoGrearToFrontOther(List<Amount<Length>> currentXCoGrearToFrontOther) {
		this._currentXCoGrearToFrontOther = currentXCoGrearToFrontOther;
	}

	public void setCurrentXCoGfrontToRear(List<Amount<Length>> currentXCoGfrontToRear) {
		this._currentXCoGfrontToRear = currentXCoGfrontToRear;
	}

	public void setCurrentXCoGrearToFront(List<Amount<Length>> currentXCoGrearToFront) {
		this._currentXCoGrearToFront = currentXCoGrearToFront;
	}

	
	
}